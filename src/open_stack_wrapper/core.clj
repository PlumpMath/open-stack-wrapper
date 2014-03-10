(ns open-stack-wrapper.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
    (:use [open-stack-wrapper.util]
        [open-stack-wrapper.mock]
        [open-stack-wrapper.handler :as handler]
        [slingshot.slingshot :only [throw+ try+]]
        )
  )

; definitions rest api call
(def conn-timeout 10000)

(def socket-timeout 10000)

; utilities
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

; available calls

(defn tokens
  ([ {:keys [url username password]}]
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
                                       :accept :json}))))

(defn tenants
  ([{:keys [url token-id]}]
    (tenants url token-id))
  ([url token]
     (handler/adapt-call (client/get (str url "/v2.0/tenants")
                                     {:headers {"X-Auth-Token" token}
                                      :content-type :json
                                      :socket-timeout socket-timeout
                                      :conn-timeout conn-timeout
                                      :accept :json}))))

(defn endpoints [url username password tenant-name]
  (handler/adapt-call (client/post (str url "/v2.0/tokens")
                                   {
                                    :body (json/write-str {:auth  {:passwordCredentials {:username username
                                                                                         :password password}
                                                                   :tenantName tenant-name}})
                                    :content-type :json
                                    :socket-timeout socket-timeout
                                    :conn-timeout conn-timeout
                                    :accept :json})))


(defn delete [token-id url ]

  (handler/adapt-call-delete
   (client/delete url
                  {:headers {"X-Auth-Token" token-id}
                   :content-type :json
                   :socket-timeout socket-timeout
                   :conn-timeout conn-timeout
                   :accept :json
                   :throw-entire-message? true})


   )
  )
(defn seg-delete [token-id url ]

  (try+
   (client/delete url
                  {:headers {"X-Auth-Token" token-id}
                   :content-type :json
                   :socket-timeout socket-timeout
                   :conn-timeout conn-timeout
                   :accept :json
                   :throw-entire-message? true})

   (catch Object e
     (println (:status e))
     )
   )
  )

(defn service-call [token-id publicURL path]
  (let [path (if (keyword? path) (name path) path)
        url (str publicURL "/" path )]
    (handler/adapt-call (client/get url
                                    {:headers {"X-Auth-Token" token-id}
                                     :content-type :json
                                     :socket-timeout socket-timeout
                                     :conn-timeout conn-timeout
                                     :accept :json})))
  )

(defn operation  [login-url username password tenant-name service-type path]
  (let [eps (endpoints login-url username password tenant-name)
        token-id (get-in eps [:access :token :id])
        publicURL (get-in  (structured-endpoints eps) [service-type :publicURL] )
        ]
                                        ;    (println eps)(println token-id)(println service-type)
    (service-call token-id publicURL path)
    ))

(defn create-server [token-id nova-url server-name flavor-href image-href network-id ]
  (let [response (client/post (str nova-url "/servers")
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
                               :throw-entire-message? true})]

    (if (= (:status response) 202)
      (merge {:success true } (json/read-str (:body response) :key-fn keyword))
      {:success false :code (:status response) :body response}
      )

    )
  )

(defn create-network [token-id quantum-url network-name]
  (let [response (client/post (str quantum-url "v2.0/networks")
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
                               :accept :json})]

    (if (= (:status response) 201)
      (merge {:success true } (json/read-str (:body response) :key-fn keyword))
      {:success false :code (:status response) :body response}
      )

    )
  )

(defn create-subnet [token-id quantum-url network-id cidr start end]
  (let [response (client/post (str quantum-url "v2.0/subnets")
                              {
                               :body (json/write-str {:subnet
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
                               :accept :json})]

    (if (= (:status response) 201)
      (merge {:success true } (json/read-str (:body response) :key-fn keyword))
      {:success false :code (:status response) :body response}
      )

    )
  )

(comment "example operation  :compute :images of _tenant_selected"
         (def login-properties (load-config "./login.properties"))
         (def username (:username login-properties))
         (def password (:password login-properties))
         (def url (:url login-properties))

         (operation url username password username :compute :images)
         )

(comment "process create-network"

         (def login-properties (load-config "./login.properties"))
         (def username (:username login-properties))
         (def password (:password login-properties))
         (def url (:url login-properties))
         (endpoints url username password username)
         (def eps-res *1)
         (structured-endpoints eps-res)
         (def eps *1)
         (def token-id (get-in eps-res [:access :token :id]))

         (def quantum-url (:publicURL (:network eps)))

         (create-network token-id quantum-url "juanitoiii" )
         (def response-network *1)
         )

(comment "process create-subnet"

         (def login-properties (load-config "./login.properties"))
         (def username (:username login-properties))
         (def password (:password login-properties))
         (def url (:url login-properties))
         (endpoints url username password username)
         (def eps-res *1)
         (structured-endpoints eps-res)
         (def eps *1)
         (def token-id (get-in eps-res [:access :token :id]))

         (def quantum-url (:publicURL (:network eps)))

         (service-call token-id quantum-url "v2.0/networks")
         (def networks *1)
         (def network (:id (last (:networks networks))))


         (create-subnet token-id quantum-url
                        network
                        "192.168.199.0/24"
                        "192.168.199.2"
                        "192.168.199.20"
                        )
         (def response-subnet *1)
         )


(comment "process create-server"

         (def login-properties (load-config "./login.properties"))
         (def username (:username login-properties))
         (def password (:password login-properties))
         (def url (:url login-properties))
         (endpoints url username password username)
         (def eps-res *1)
         (structured-endpoints eps-res)
         (def eps *1)
         (def token-id (get-in eps-res [:access :token :id]))
         (def nova-url (:publicURL (:compute eps)))
         (def quantum-url (:publicURL (:network eps)))
         (service-call token-id nova-url :images)
         (def images *1)
         (def image (:href (first (:links (first (:images images))))))
         (service-call token-id nova-url :flavors)
         (def flavors *1)
         (def flavor (:href (first (:links (first (:flavors flavors))))))
         (service-call token-id quantum-url "v2.0/networks")
         (def networks *1)
         (def network "dd6f18e8-3be9-48df-9c0c-31730efddcf9")

         (create-server token-id nova-url "new-server-def" flavor image network)



         )
