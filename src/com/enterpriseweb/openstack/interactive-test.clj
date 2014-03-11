(ns com.enterpriseweb.openstack.interactive-test
  (:use [com.enterpriseweb.openstack.OpenStackAPI]
        [open-stack-wrapper.util :as util]
        [open-stack-wrapper.core :as os-core])
    (:import [org.json JSONObject]))



(comment "test-get-eps"

  (def login-properties (util/load-config "./login.properties"))

  (-tokens (create-java-json login-properties :url :username :password))

  (def -tokens-response *1)

  (-tenants (-> (create-java-json login-properties :url)
                (assoc+ :token-id (get-in+ -tokens-response [:access :token :id]))))

  (def -tenants-response *1)

  (-endpoints (-> (create-java-json login-properties :url :password :username)
                  (assoc+ :tenant-name (get-in+ -tenants-response [:tenants 0 :name]))))

  (def -endpoints-response  *1)

  (def new-token-id (get-in+ -endpoints-response [:token-id]))

  (def endpoints-structured (get-in+ -endpoints-response [:eps]) )

  )




(comment

  (-serviceCall (clojure-json->java-json {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:compute :publicURL] ) :path "/images"}))

  (def images-response *1)


  (get-service-call {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:compute :publicURL] ) :path "/flavors"})

  (def flavors-response *1)


  #_(util/pprint-json-scheme images-response)

  #_(map (juxt :id :name #(:href (first (:links %)))) (:images  images-response))



  (get-service-call {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:network :publicURL] ) :path "v2.0/networks"})

  (def networks-response *1)

  (defn create-json-create-network [name]
    (doto (JSONObject.)
      (.put "token-id" new-token-id)
      (.put "quantum-url" (:publicURL (:network endpoints-structured)))
      (.put "network-name" name)
      ))

  (-createNetwork (create-json-create-network "juan-network-5"))


  (def response-create-network *1)

  (defn create-json-delete-quantum-entity [id]
    (doto (JSONObject.)
      (.put "eps-token-id" new-token-id)
      (.put "eps-url" (:publicURL (:network endpoints-structured)) )
      (.put "id" id)))

    (defn create-json-delete-nova-entity [id]
    (doto (JSONObject.)
      (.put "eps-token-id" new-token-id)
      (.put "eps-url" (:publicURL (:compute endpoints-structured)) )
      (.put "id" id)))

  (-deleteNetwork (create-json-delete-quantum-entity (get-in (java-json->clojure-json response-create-network) [:network :id])))


  (-deleteNetwork (create-json-delete-quantum-entity (get-in networks-response [:networks 2 :id])))
  (-deleteNetwork (create-json-delete-quantum-entity "6a1d29a2-4fd8-4ed6-a44e-3665e8862bfa" ))


  (defn create-json-create-subnet [network-id]
    (doto (JSONObject.)
      (.put "token-id" new-token-id)
      (.put "quantum-url" (:publicURL (:network endpoints-structured)))
      (.put "network-id" network-id)
      (.put "cidr" "192.168.198.0/24")
      (.put "start" "192.168.198.40")
      (.put "end" "192.168.198.50")
      )
    )
  (map (juxt :id :name ) (:networks networks-response))

  (-createSubnet (create-json-create-subnet (get-in networks-response [:networks 0 :id])))


  (def response-create-subnet (java-json->clojure-json *1))



  (-deleteSubnet (create-json-delete-quantum-entity (get-in response-create-subnet [:subnet :id])))


  (defn create-json-create-server []
    (doto (JSONObject.)
      (.put "token-id" new-token-id)
      (.put "nova-url" (:publicURL (:compute endpoints-structured)))

      (.put "server-name" "eeeeeeaaaaaa")
      (.put "flavor-href" (:href (first (:links (last (:flavors  flavors-response))))))
      (.put "image-href" (:href (first (:links (last (:images  images-response))))))
      (.put "network-id" (get-in networks-response [:networks 0 :id]))
      )
    )

  (-createServer (create-json-create-server))

  (def response-create-server (java-json->clojure-json *1))
  (-deleteServer (create-json-delete-nova-entity (get-in response-create-server [:server :id])))


  )





(comment "trash stock"

           (defn create-json-operation []
    (doto (JSONObject.)
      (.put "url" "http://192.168.1.26:5000")
      (.put "username" "admin")
      (.put "password" "password")
      (.put "tenant-name" "admin")
      (.put "service-type" "compute")
      (.put "path" "/images")))

  (defn create-json-endpoints []
    (doto (JSONObject.)
      (.put "url" "http://192.168.1.26:5000")
      (.put "username" "admin")
      (.put "password" "password")
      (.put "tenant-name" "admin")))


  (get-endpoints-structured (java-json->clojure-json
                             (create-json-endpoints)))

  (def eps *1)

  (get-service-call {:eps-token-id (:token-id eps)
                     :url (get-in eps [:eps :compute :publicURL])
                     :path "/images"} )

  (def service-call-response *1)


  (defn create-json-service-call []
    (doto (JSONObject.)
      (.put "eps-token-id" (:token-id eps))
      (.put "url" (get-in eps [:eps :compute :publicURL]))
      (.put "path" "/images")))

  (-serviceCall (create-json-service-call))

)
