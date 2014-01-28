(ns open-stack-wrapper.core
  (:use [open-stack-wrapper.util]
        [open-stack-wrapper.mock])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  )

(def token-id
  "MIIDCwYJKoZIhvcNAQcCoIIC-DCCAvgCAQExCTAHBgUrDgMCGjCCAWEGCSqGSIb3DQEHAaCCAVIEggFOeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxNC0wMS0yOFQxMzozNzowMS43Mzc2NzkiLCAiZXhwaXJlcyI6ICIyMDE0LTAxLTI5VDEzOjM3OjAxWiIsICJpZCI6ICJwbGFjZWhvbGRlciJ9LCAic2VydmljZUNhdGFsb2ciOiBbXSwgInVzZXIiOiB7InVzZXJuYW1lIjogImZhY2Vib29rMTQyODQ2Nzg1MCIsICJyb2xlc19saW5rcyI6IFtdLCAiaWQiOiAiMmVhZWRhYzBkY2MwNDM1Y2JmZWM2OWRjYmQzMzkxYjQiLCAicm9sZXMiOiBbXSwgIm5hbWUiOiAiZmFjZWJvb2sxNDI4NDY3ODUwIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbXX19fTGCAYEwggF9AgEBMFwwVzELMAkGA1UEBhMCVVMxDjAMBgNVBAgMBVVuc2V0MQ4wDAYDVQQHDAVVbnNldDEOMAwGA1UECgwFVW5zZXQxGDAWBgNVBAMMD3d3dy5leGFtcGxlLmNvbQIBATAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASCAQCg60K56EfYPNo7LKVbtL5T51z+rp5VS4hC9NomR2skkGEzSyg6KGdIerwkH5UKmtgPo2zyBl1LWzVnjmn7zIJdVttBQ-FqbNCnBZYeXmkjNURTMyaqhltY-MzyF0qUkCYnErFIfULGqryOJr2oaUlASzIk2302LYb-MokJ8ha1KbUVCdR1p5svDwvOfRRjiOHQUn+RERMpkVi-Iglg4pLq8+JJZFJukgiCYND+GuqNWBw3BBa4hDaTxoVQUNDRBha+Q38Gcwx+A4faoa-yuoRcHZfS6hzj07mBeR30YWd2mok47pDndR7KV8gbGiJ7RT1A3GPfYknGSSnj2aXg2c7N")

(def tenant-data {:description "Auto created account", :enabled true, :id "da05a30dff7746b9a20027a68cfe6076", :name "facebook1428467850"})

(defn tokens []
  (client/post "http://8.21.28.222:5000/v2.0/tokens"
               {
                :body (json/write-str {:auth  {:passwordCredentials {:username "facebook1428467850"
                                                                     :password "3a34gc72"}}})
                                        ;   :body "{\"json\": \"input\"}"
                                        ;   :headers {"X-Api-Version" "2"}
                :content-type :json
                :socket-timeout 2000 ;; in milliseconds
                :conn-timeout 2000   ;; in milliseconds
                :accept :json}))

(defn tenants [token]
  (client/get "http://8.21.28.222:5000/v2.0/tenants"
              {:headers {"X-Auth-Token" token}
               :content-type :json
               :socket-timeout 2000 ;; in milliseconds
               :conn-timeout 2000   ;; in milliseconds
               :accept :json})
  )

(defn endpoints [tenant-name]
  (client/post "http://8.21.28.222:5000/v2.0/tokens"
               {
                :body (json/write-str {:auth  {:passwordCredentials {:username "facebook1428467850"
                                                                     :password "3a34gc72"}
                                               :tenantName tenant-name}})
                                        ;   :body "{\"json\": \"input\"}"
                                        ;   :headers {"X-Api-Version" "2"}
                :content-type :json
                :socket-timeout 2000 ;; in milliseconds
                :conn-timeout 2000   ;; in milliseconds
                :accept :json})
  )


(defn process-endpoints [eps-json]
  (let [access (:access eps-json)
        endpoints (:serviceCatalog access)]
    (map :endpoints endpoints))

  )

(defn get-response-body
  "having a json-string response  obtain the body on json format"
  [response] (json/read-str (:body response) :key-fn keyword))

(defn get-token-id[response-body-json] (-> response-body-json :access :token :id))


(defn get-token-id [data]
  (get-in data [:access :token :id]))

(comment
  (pprint-json-scheme endpoints-mock))

(defn store-strutctured-endpoints [data]
  (let [services (get-in data [:access :serviceCatalog])]
    (reduce
     (fn [c it]
       (let [first-endpoint (first (:endpoints it))]
         (assoc c
           (keyword (:type  it))
           {:name (:name it)
            :id (:id first-endpoint)
            :publicURL (:publicURL first-endpoint)}))) {}  services)))
(comment "having endpoints give me 'compute'"
         (:compute (store-strutctured-endpoints endpoints-mock)))

(client/get url
            {:headers {"X-Auth-Token" t1}
             :content-type :json
             :socket-timeout 2000              ;; in milliseconds
             :conn-timeout 2000                ;; in milliseconds
             :accept :json})

((fn [tenant-name service-type path]

   (let [
         ep (endpoints tenant-name)
         ep2 (get-response-body ep)
         token-id (get-in ep2 [:access :token :id])
         publicURL (get-in  (store-strutctured-endpoints ep2) [  :compute :publicURL] )
         url (str publicURL "/" (name path) )]


     (client/get url
                 {:headers {"X-Auth-Token" token-id}
                  :content-type :json
                  :socket-timeout 2000              ;; in milliseconds
                  :conn-timeout 2000                ;; in milliseconds
                  :accept :json})


     )
   ) "facebook1428467850" :compute :images)
