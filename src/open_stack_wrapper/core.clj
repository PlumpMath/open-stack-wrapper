(ns open-stack-wrapper.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
    (:use [open-stack-wrapper.util]
        [open-stack-wrapper.mock]
        [open-stack-wrapper.handler :as handler]
        [slingshot.slingshot :only [throw+ try+]]
        )
  )

(defn tester []
  (println "tester"))
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
  (handler/adapt-call (client/post (str url "/v2.0/tokens")
                                   {
                                    :body (json/write-str {:auth  {:passwordCredentials {:username username
                                                                                         :password password}
                                                                   :tenantName tenant-name}})
                                    :content-type :json
                                    :socket-timeout socket-timeout
                                    :conn-timeout conn-timeout
                                    :accept :json
                                    :throw-entire-message? true})))

(defn endpoints-adaptated
  [{:keys  [url username password tenant-name]}]
  (let [eps (endpoints url username password tenant-name)]
    {:token-id (get-in eps [:access :token :id])
     :eps (structured-endpoints eps)}))


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

(comment
         (def login-properties (load-config "./login.properties"))
         (def username (:username login-properties))
         (def password (:password login-properties))
         (def url "http://192.168.1.26:5000")

         (tokens url "admin" "password" )
         (def tokens-response *1)
         (def token-id (get-in tokens-response [:access :token :id]))
         (tenants url  token-id )
         (def tenants-response *1)
         (endpoints-adaptated {:url "http://192.168.1.26:5000" :username "admin" :password "password" :tenant-name "admin"})
         (def endpoints-response *1)
         (def token-eps (:token-id endpoints-response))
         (def eps-structured (:eps endpoints-response))
         (service-call token-eps (get-in eps-structured [:compute :publicURL]) "/images")
         (def images-response *1)
         (service-call token-eps (get-in eps-structured [:compute :publicURL]) "/flavors")
         (def flavors-response *1)
         (service-call token-eps (get-in eps-structured [:network :publicURL]) "v2.0/networks")
         (def networks-response *1)
         (create-network token-eps (get-in eps-structured [:network :publicURL]) "nueva-network")
         (delete token-eps (str (get-in eps-structured [:network :publicURL])
                                "v2.0/networks/"
                                (get-in networks-response [:networks 0 :id])))

         (service-call token-eps (get-in eps-structured [:network :publicURL]) "v2.0/subnets")
         (def subnets-response *1)
         (create-subnet token-eps (get-in eps-structured [:network :publicURL]) (get-in networks-response [:networks 0 :id]) "192.168.198.0/24" "192.168.198.40" "192.168.198.50")
         (def response-create-subnet *1)
         (delete token-eps (str (get-in eps-structured [:network :publicURL])
                                "v2.0/subnets/"
                                "e6066bc1-d716-4861-8b6f-1cdd238d3c39"))
         (create-server token-eps
                        (get-in eps-structured [:compute :publicURL])
                        "mi-server-name"
                        (get-in flavors-response [:flavors 0 :links 0 :href])
                        (get-in images-response [:images 0 :links 0 :href])
                        (get-in networks-response [:networks 0 :id]))
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
