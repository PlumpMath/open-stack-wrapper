(ns open-stack-wrapper.core
  (:use [open-stack-wrapper.util]
        [open-stack-wrapper.mock]
        [open-stack-wrapper.handler :as handler])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  )


(defn tokens []
  (handler/adapt-call (client/post "http://8.21.28.222:5000/v2.0/tokens"
                           {:body (json/write-str {:auth
                                                   {:passwordCredentials
                                                    {:username "facebook1428467850"
                                                     :password "3a34gc72"}}})
                                        ;   :body "{\"json\": \"input\"}"
                                        ;   :headers {"X-Api-Version" "2"}
                            :content-type :json
                            :socket-timeout 2000 ;; in milliseconds
                            :conn-timeout 2000 ;; in milliseconds
                            :accept :json})))

(defn tenants [token]
  (handler/adapt-call (client/get "http://8.21.28.222:5000/v2.0/tenants"
               {:headers {"X-Auth-Token" token}
                :content-type :json
                :socket-timeout 2000 ;; in milliseconds
                :conn-timeout 2000   ;; in milliseconds
                :accept :json}))
  )

(defn endpoints [tenant-name]
  (handler/adapt-call (client/post "http://8.21.28.222:5000/v2.0/tokens"
                {
                 :body (json/write-str {:auth  {:passwordCredentials {:username "facebook1428467850"
                                                                      :password "3a34gc72"}
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

(defn store-structured-endpoints [data]
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
         (:compute (store-strutctured-endpoints endpoints-mock)))

(defn operation  [tenant-name service-type path]
  (let [eps (endpoints tenant-name)
        token-id (get-in eps [:access :token :id])
        publicURL (get-in  (store-structured-endpoints eps) [service-type :publicURL] )
        url (str publicURL "/" (name path) )]
    (handler/adapt-call (client/get url
                            {:headers {"X-Auth-Token" token-id}
                             :content-type :json
                             :socket-timeout 2000 ;; in milliseconds
                             :conn-timeout 2000   ;; in milliseconds
                             :accept :json}))))

(comment "get :compute :images of _tenant_selected"
  (operation "facebook1428467850" :compute :images)
  )
