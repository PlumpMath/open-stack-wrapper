(ns open-stack-wrapper.core
  (:use [open-stack-wrapper.util]
        [open-stack-wrapper.mock]
        [open-stack-wrapper.handler :as handler])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  )

;; {:success :boolean,
;;  :access
;;  {:token {:issued_at "S", :expires "S", :id "S"},
;;   :serviceCatalog [],
;;   :user
;;   {:username "S", :roles_links [], :id "S", :roles [], :name "S"},
;;   :metadata {:is_admin "N", :roles []}}}
(defn tokens [url username password]
  (handler/adapt-call (client/post (str url "/v2.0/tokens")
                           {:body (json/write-str {:auth
                                                   {:passwordCredentials
                                                    {:username username
                                                     :password password}}})
                                        ;   :body "{\"json\": \"input\"}"
                                        ;   :headers {"X-Api-Version" "2"}
                            :content-type :json
                            :socket-timeout 2000 ;; in milliseconds
                            :conn-timeout 2000 ;; in milliseconds
                            :accept :json})))

;; {:success :boolean,
;;  :tenants_links [],
;;  :tenants [{:enabled :boolean, :name "S", :id "S", :description "S"}]}
;; {:success :boolean,
;;  :tenants_links [],
;;  :tenants [{:enabled :boolean, :name "S", :id "S", :description "S"}]}
(defn tenants [url token]
  (handler/adapt-call (client/get (str url "/v2.0/tenants")
               {:headers {"X-Auth-Token" token}
                :content-type :json
                :socket-timeout 2000 ;; in milliseconds
                :conn-timeout 2000   ;; in milliseconds
                :accept :json}))
  )
;; {:success :boolean,
;;  :access
;;  {:token
;;   {:issued_at "S",
;;    :expires "S",
;;    :id "S",
;;    :tenant {:description "S", :enabled :boolean, :id "S", :name "S"}},
;;   :serviceCatalog
;;   [{:endpoints
;;     [{:adminURL "S",
;;       :region "S",
;;       :internalURL "S",
;;       :id "S",
;;       :publicURL "S"}],
;;     :endpoints_links [],
;;     :type "S",
;;     :name "S"}],
;;   :user
;;   {:username "S",
;;    :roles_links [],
;;    :id "S",
;;    :roles [{:name "S"}],
;;    :name "S"},
;;   :metadata {:is_admin "N", :roles ["S"]}}}
(defn endpoints [url username password tenant-name]
  (handler/adapt-call (client/post (str url "/v2.0/tokens")
                {
                 :body (json/write-str {:auth  {:passwordCredentials {:username username
                                                                      :password password}
                                                :tenantName tenant-name}})
                                        ;   :body "{\"json\": \"input\"}"
                                        ;   :headers {"X-Api-Version" "2"}
                 :content-type :json
                 :socket-timeout 2000 ;; in milliseconds
                 :conn-timeout 2000   ;; in milliseconds
                 :accept :json}))
  )


;; {:network {:name "S", :id "S", :publicURL "S"},
;;  :compute {:name "S", :id "S", :publicURL "S"},
;;  :image {:name "S", :id "S", :publicURL "S"},
;;  :identity {:name "S", :id "S", :publicURL "S"},
;;  :ec2 {:name "S", :id "S", :publicURL "S"},
;;  :metering {:name "S", :id "S", :publicURL "S"},
;;  :object-store {:name "S", :id "S", :publicURL "S"},
;;  :s3 {:name "S", :id "S", :publicURL "S"},
;;  :volume {:name "S", :id "S", :publicURL "S"}}
(defn structured-endpoints [data]
  "in this development state we take the first endpoint available on each service"
  (let [services (get-in data [:access :serviceCatalog])]
    (reduce
     (fn [c service]
       (let [first-endpoint (first (:endpoints service))]
         (assoc c (keyword (:type  service))
           {:name (:name service)
            :id (:id first-endpoint)
            :publicURL (:publicURL first-endpoint)})))
     {}
     services)))

;([:identity "keystone"] [:ec2 "ec2"] [:volume "cinder"] [:image "glance"] [:s3 "s3"] [:computev3 "novav3"] [:volumev2 "cinderv2"] [:network "neutron"] [:compute "nova"])
(comment "having endpoints give me 'compute' endpoints"
         (:compute (structured-endpoints endpoints-mock)))


;; {:success true, :flavors [{:id "2", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/flavors/2", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/flavors/2", :rel "bookmark"}], :name "m1.small"} {:id "3", :links [{:href "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/flavors/3", :rel "self"} {:href "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/flavors/3", :rel "bookmark"}], :name "m1.medium"}]}
(defn service-call [token-id publicURL path]
  (let [path (if (keyword? path) (name path) path)
        url (str publicURL "/" path )]
   (handler/adapt-call (client/get url
                                   {:headers {"X-Auth-Token" token-id}
                                    :content-type :json
                                    :socket-timeout 2000 ;; in milliseconds
                                    :conn-timeout 2000 ;; in milliseconds
                                    :accept :json})))
  )


(defn operation  [login-url username password tenant-name service-type path]
  (let [eps (endpoints login-url username password tenant-name)
        token-id (get-in eps [:access :token :id])
        publicURL (get-in  (structured-endpoints eps) [service-type :publicURL] )
]

    (service-call token-id publicURL path)
    ))
;(operation "http://192.168.1.23:5000" "admin" "password" "admin" :compute "/images" )
(comment "get :compute :images of _tenant_selected"
  (operation url "facebook1428467850" "3a34gc72":compute :images)
)

(defn create-server [token-id url-server server-name flavor image network-id ]
  (let [response (client/post url-server
                      {
                       :body (json/write-str {:server
                                              {:flavorRef flavor
                                               :imageRef image
                                               :name server-name
                                               :networks [{:uuid network-id}]}
                                              })
                       :headers {"X-Auth-Token" token-id}
                       :content-type :json
                       :socket-timeout 30000 ;; in milliseconds
                       :conn-timeout 30000   ;; in milliseconds
                       :accept :json})]
    (if (= (:status response) 202)
      (do
        ;{:server {:security_groups [{:name "default"}], :OS-DCF:diskConfig "MANUAL", :id "9596a7dc-8a35-4ff0-9b0d-7934b61579de", :links [{:href "http://192.168.1.16:8774/v2/21d1ae8a1ba941b1aadc49f4b521228b/servers/9596a7dc-8a35-4ff0-9b0d-7934b61579de", :rel "self"} {:href "http://192.168.1.16:8774/21d1ae8a1ba941b1aadc49f4b521228b/servers/9596a7dc-8a35-4ff0-9b0d-7934b61579de", :rel "bookmark"}], :adminPass "yxddXRQ9ZjuH"}}
        (merge {:success true } (json/read-str (:body response) :key-fn keyword))
        )
      {:success false :code (:status response) :body response}
      )
    )
  )




(comment
  (def token-id
    "MIIL6gYJKoZIhvcNAQcCoIIL2zCCC9cCAQExCTAHBgUrDgMCGjCCCkAGCSqGSIb3DQEHAaCCCjEEggoteyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wMi0xMVQwOTowODowNy4zNTMzODQiLCAiZXhwaXJlcyI6ICIyMDE0LTAyLTEyVDA5OjA4OjA3WiIsICJpZCI6ICJwbGFjZWhvbGRlciIsICJ0ZW5hbnQiOiB7ImRlc2NyaXB0aW9uIjogbnVsbCwgImVuYWJsZWQiOiB0cnVlLCAiaWQiOiAiMTI3MTFlMGNhMmFmNDFhMjk3NzI3NDZjZTdiNDM5NTMiLCAibmFtZSI6ICJhZG1pbiJ9fSwgInNlcnZpY2VDYXRhbG9nIjogW3siZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzE5Mi4xNjguMS4yNjo4Nzc0L3YyLzEyNzExZTBjYTJhZjQxYTI5NzcyNzQ2Y2U3YjQzOTUzIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE5Mi4xNjguMS4yNjo4Nzc0L3YyLzEyNzExZTBjYTJhZjQxYTI5NzcyNzQ2Y2U3YjQzOTUzIiwgImlkIjogIjNiNTU4N2MxOWJmZDQxM2E5YjlmNjgxNDMzMzExZGMyIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTkyLjE2OC4xLjI2Ojg3NzQvdjIvMTI3MTFlMGNhMmFmNDFhMjk3NzI3NDZjZTdiNDM5NTMifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiY29tcHV0ZSIsICJuYW1lIjogIm5vdmEifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTkyLjE2OC4xLjI2Ojk2OTYvIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzE5Mi4xNjguMS4yNjo5Njk2LyIsICJpZCI6ICIyYTkyY2IzNTc5OTA0OWFiOGQ5YWU5NTcyNjY0ODBlMSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE5Mi4xNjguMS4yNjo5Njk2LyJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJuZXR3b3JrIiwgIm5hbWUiOiAicXVhbnR1bSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6MzMzMyIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6MzMzMyIsICJpZCI6ICIwMzRlMTNkODAyOWI0NDdkYTM3ODU0MTE5NWQ0OWY3NyIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE5Mi4xNjguMS4yNjozMzMzIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogInMzIiwgIm5hbWUiOiAiczMifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTkyLjE2OC4xLjI2OjkyOTIiLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTkyLjE2OC4xLjI2OjkyOTIiLCAiaWQiOiAiMWRiNDA4YzcyZDk2NGY3Mjg4ZDM4ZDYyOTczNTgxZjciLCAicHVibGljVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6OTI5MiJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJpbWFnZSIsICJuYW1lIjogImdsYW5jZSJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6ODc3Ni92MS8xMjcxMWUwY2EyYWY0MWEyOTc3Mjc0NmNlN2I0Mzk1MyIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6ODc3Ni92MS8xMjcxMWUwY2EyYWY0MWEyOTc3Mjc0NmNlN2I0Mzk1MyIsICJpZCI6ICIxMWUxODE2ODA1MmI0OTgxYmRiMzk3NzdmZGU3ZmNjMyIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE5Mi4xNjguMS4yNjo4Nzc2L3YxLzEyNzExZTBjYTJhZjQxYTI5NzcyNzQ2Y2U3YjQzOTUzIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogInZvbHVtZSIsICJuYW1lIjogImNpbmRlciJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6ODc3My9zZXJ2aWNlcy9BZG1pbiIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6ODc3My9zZXJ2aWNlcy9DbG91ZCIsICJpZCI6ICIxOTg4NDc1MmY4YWI0ZGEwODZhM2QxMWIzNmQwM2EzNSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzE5Mi4xNjguMS4yNjo4NzczL3NlcnZpY2VzL0Nsb3VkIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogImVjMiIsICJuYW1lIjogImVjMiJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6MzUzNTcvdjIuMCIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xOTIuMTY4LjEuMjY6NTAwMC92Mi4wIiwgImlkIjogIjRlNzc3M2I4OTdmZjQ4YmU4MWY2OTkwY2UxZWFlODVhIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTkyLjE2OC4xLjI2OjUwMDAvdjIuMCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJpZGVudGl0eSIsICJuYW1lIjogImtleXN0b25lIn1dLCAidXNlciI6IHsidXNlcm5hbWUiOiAiYWRtaW4iLCAicm9sZXNfbGlua3MiOiBbXSwgImlkIjogIjZkY2E2OGI5NTRiZDQ0YjU5ODJhMmVhZGZiZWE1MzAxIiwgInJvbGVzIjogW3sibmFtZSI6ICJhZG1pbiJ9XSwgIm5hbWUiOiAiYWRtaW4ifSwgIm1ldGFkYXRhIjogeyJpc19hZG1pbiI6IDAsICJyb2xlcyI6IFsiZGY5ZjM3MDNlNTBhNDRhM2JjOTRiOTZmODVmNzhhNWQiXX19fTGCAYEwggF9AgEBMFwwVzELMAkGA1UEBhMCVVMxDjAMBgNVBAgTBVVuc2V0MQ4wDAYDVQQHEwVVbnNldDEOMAwGA1UEChMFVW5zZXQxGDAWBgNVBAMTD3d3dy5leGFtcGxlLmNvbQIBATAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASCAQCEIWu+8LG1SCs6kNffvEcIb4nDIbDD4zp3ANyUvW3F9VE0Zt9OlsXb7NjJHzGO8nktLfkPs3-Frne6vb1AufmrIjsS9rhcLXhZfBBI0wp5vd5sYYgJZ912v8JbR5hejbmAV6-dHDSMjE5LHUM67odYvAo08GE0B9NEvzZzdoGu0CcgHl+b2+3kKEoIviNLqrImzTJEIHOk-hVv+9seQw7tDcAqrEx5SDgt8nNRfiYQI1yzDnyy-PJ9-tjD0woum7TeH9O6nzC7-uscaFsUB2oBaQaMZciFq170VdnHBtF7CO9Ui2fXsmIs70wb+xP-x1jyJS7v0XzHeOWwluwsnFHN")
  (let [url-server "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/servers"
        flavor "http://8.21.28.222:8774/v2/da05a30dff7746b9a20027a68cfe6076/flavors/2"
        image "http://8.21.28.222:8774/da05a30dff7746b9a20027a68cfe6076/images/55ebdcd2-d6ce-4c24-8717-888dd01c551d"
        network-id "6c79a79f-8b75-4399-9fc2-72cabeb283a8"]
    (client/post url-server
                 {
                  :body (json/write-str {:server
                                         {:flavorRef flavor
                                          :imageRef image
                                          :name "other"
                                          :networks [{:uuid network-id}]}
                                         })
                  :headers {"X-Auth-Token" token-id}
                  :content-type :json
                  :socket-timeout 30000 ;; in milliseconds
                  :conn-timeout 30000 ;; in milliseconds
                  :accept :json}))
  {:orig-content-encoding nil, :trace-redirects ["http://192.168.1.26:8774/v2/12711e0ca2af41a29772746ce7b43953/servers"], :request-time 17972, :status 202, :headers {"location" "http://192.168.1.26:8774/v2/12711e0ca2af41a29772746ce7b43953/servers/e3abbd40-0c1e-42e7-a4c4-8be375468b16", "content-type" "application/json", "content-length" "440", "x-compute-request-id" "req-6a3180a0-e47a-4d43-a28d-70bea51b01d0", "date" "Tue, 11 Feb 2014 10:02:28 GMT", "connection" "close"}, :body "{\"server\": {\"security_groups\": [{\"name\": \"default\"}], \"OS-DCF:diskConfig\": \"MANUAL\", \"id\": \"e3abbd40-0c1e-42e7-a4c4-8be375468b16\", \"links\": [{\"href\": \"http://192.168.1.26:8774/v2/12711e0ca2af41a29772746ce7b43953/servers/e3abbd40-0c1e-42e7-a4c4-8be375468b16\", \"rel\": \"self\"}, {\"href\": \"http://192.168.1.26:8774/12711e0ca2af41a29772746ce7b43953/servers/e3abbd40-0c1e-42e7-a4c4-8be375468b16\", \"rel\": \"bookmark\"}], \"adminPass\": \"7mwvWP2N8jUh\"}}"}
  )
