(ns com.enterpriseweb.openstack.wrapper.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [com.enterpriseweb.openstack.wrapper.handler :as handler]))

;;;
;;; definitions
;;;
(def conn-timeout 10000)
(def socket-timeout 10000)
;;;
;;; utilities
;;;
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

;;;
;;; api calls
;;;
(defn tokens
  ([{:keys [url username password]}]
   (tokens url username password))
  ([url username password]
     (handler/adapt-call (client/post (str url "/v2.0/tokens")
                                      {:body (json/write-str {:auth
                                                              {:passwordCredentials
                                                               {:username username
                                                                :password password}}})
                                       :content-type :json
                                       :socket-timeout socket-timeout
                                       :conn-timeout conn-timeout
                                       :accept :json
                                       :throw-entire-message? true}))))

(defn tenants
  ([{:keys [url token-id]}]
    (tenants url token-id))
  ([url token]
     (handler/adapt-call (client/get (str url "/v2.0/tenants")
                                     {:headers {"X-Auth-Token" token}
                                      :content-type :json
                                      :socket-timeout socket-timeout
                                      :conn-timeout conn-timeout
                                      :accept :json
                                      :throw-entire-message? true}))))

(defn endpoints [url username password tenant-name]
  (handler/adapt-call
   (client/post
    (str url "/v2.0/tokens")
                {:body (json/write-str {:auth
                                        {:passwordCredentials
                                         {:username username
                                          :password password}
                                         :tenantName tenant-name}})
                 :content-type :json
                 :socket-timeout socket-timeout
                 :conn-timeout conn-timeout
                 :accept :json
                 :throw-entire-message? true})))

(defn endpoints-adaptated
  [{:keys  [url username password tenant-name]}]
  (let [raw-eps (endpoints url username password tenant-name)
        eps (structured-endpoints raw-eps)
        adapted-local-url-eps (->> (map (fn [i] {i (assoc (i eps) :publicURL (clojure.string/replace (get-in eps [i :publicURL]) #"192.168.0.113" "208.124.249.142"))}) (keys eps))
                                  (reduce into {})
                                  )
        #_(reduce
     #(update-in % [(first %2) :publicURL] (fnil (fn [a] (clojure.string/replace (get-in eps [(first %2) :publicURL]) #"192.168.0.113" "208.124.249.142") ) 0) ) {} eps)
        ]
    {:token-id (get-in raw-eps [:access :token :id])
     :eps adapted-local-url-eps}

    ))

(defn delete
  ([{:keys  [eps-token-id url ]}]
     (delete eps-token-id url))
  ([token-id url ]
     (handler/adapt-call-delete
      (client/delete url
                     {:headers {"X-Auth-Token" token-id}
                      :content-type :json
                      :socket-timeout socket-timeout
                      :conn-timeout conn-timeout
                      :accept :json
                      :throw-entire-message? true})


      ))
  )

(defn service-call
  ([{:keys  [eps-token-id url path]}]
     (service-call eps-token-id url path))
  ([token-id publicURL path]
     (let [path (if (keyword? path) (name path) path)
           url (str publicURL "/" path )]
       (handler/adapt-call (client/get url
                                       {:headers {"X-Auth-Token" token-id}
                                        :content-type :json
                                        :socket-timeout socket-timeout
                                        :conn-timeout conn-timeout
                                        :accept :json
                                        :throw-entire-message? true}))))
  )

(defn create-server
  ([{:keys  [token-id nova-url server-name flavor-href image-href network-id ]}]
     (create-server token-id nova-url server-name flavor-href image-href network-id))
  ([token-id nova-url server-name flavor-href image-href network-id ]
   (handler/adapt-call (client/post (str nova-url "/servers")
                                 {
                                  :body (json/write-str {:server
                                                         {:flavorRef flavor-href
                                                          :imageRef image-href
                                                          :name server-name
                                                          :networks [{:uuid network-id}]}
                                                         })
                                  :headers {"X-Auth-Token" token-id}
                                  :content-type :json
                                  :socket-timeout socket-timeout
                                  :conn-timeout conn-timeout
                                  :accept :json
                                  :throw-entire-message? true})))
  )

(defn create-network
  ([{:keys  [token-id quantum-url network-name]}]
     (create-network token-id quantum-url network-name))
  ([token-id quantum-url network-name]
     (handler/adapt-call (client/post (str quantum-url "v2.0/networks")
                                 {
                                  :body (json/write-str {:network
                                                         {:shared true
                                                          :admin_state_up false
                                                          :name network-name
                                                          }
                                                         })
                                  :headers {"X-Auth-Token" token-id}
                                  :content-type :json
                                  :socket-timeout socket-timeout
                                  :conn-timeout conn-timeout
                                  :throw-entire-message? true
                                  :accept :json}))
)
  )

(defn create-subnet
 ([{:keys  [token-id quantum-url network-id cidr start end]}]
     (create-subnet token-id quantum-url network-id cidr start end))
 ([token-id quantum-url network-id cidr start end]
    (handler/adapt-call (client/post (str quantum-url "v2.0/subnets")
                                                   {:body (json/write-str {:subnet
                                                                           {:network_id network-id
                                                                            :ip_version 4
                                                                            :cidr cidr
                                                                            :allocation_pools [{:start start :end end}]
                                                                            }
                                                                           })
                                                    :headers {"X-Auth-Token" token-id}
                                                    :content-type :json
                                                    :socket-timeout socket-timeout
                                                    :conn-timeout conn-timeout
                                                    :accept :json
                                                    :throw-entire-message? true}))
)
  )
