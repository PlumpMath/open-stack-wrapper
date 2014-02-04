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

(comment   "pprinting data"

  (pprint-json-scheme endpoints-mock))
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

(comment "having endpoints give me 'compute' endpoints"
         (:compute (structured-endpoints endpoints-mock)))

(defn operation  [login-url username password tenant-name service-type path]
  (let [eps (login-url endpoints username password tenant-name)
        token-id (get-in eps [:access :token :id])
        publicURL (get-in  (structured-endpoints eps) [service-type :publicURL] )
        url (str publicURL "/" (name path) )]
    (handler/adapt-call (client/get url
                            {:headers {"X-Auth-Token" token-id}
                             :content-type :json
                             :socket-timeout 2000 ;; in milliseconds
                             :conn-timeout 2000   ;; in milliseconds
                             :accept :json}))))

(comment "get :compute :images of _tenant_selected"
  (operation url "facebook1428467850" "3a34gc72":compute :images)
)
