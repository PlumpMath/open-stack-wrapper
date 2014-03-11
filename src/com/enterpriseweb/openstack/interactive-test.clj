(ns com.enterpriseweb.openstack.interactive-test
  (:use [com.enterpriseweb.openstack.OpenStackAPI]
        [open-stack-wrapper.util :as util]
        [open-stack-wrapper.core :as os-core])
  (:import [org.json JSONObject]))

(comment "voy por aqui TODO: "
         el siguiente paso seria pasar en el jason object el action ejemplo
         :action tokens or :action tenants or :action servicecall)

(comment

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

  (-serviceCall (clojure-json->java-json {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:compute :publicURL] ) :path "/flavors"}))

  (def flavors-response *1)

  (util/pprint-json-scheme (java-json->clojure-json images-response))

  (map (juxt :id :name #(:href (first (:links %)))) (:images  (java-json->clojure-json images-response)))

  (-serviceCall (clojure-json->java-json {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:network :publicURL] ) :path "v2.0/networks"}))

  (def networks-response *1)
                                        ;logging  (map (juxt :id :name ) (:networks (java-json->clojure-json networks-response)))

  (-createNetwork (clojure-json->java-json
                   {:network-name "juan-network-6"
                    :quantum-url (:publicURL (:network endpoints-structured))
                    :token-id new-token-id}))


  (def response-create-network *1)





  (-deleteNetwork (clojure-json->java-json
                   {:eps-token-id new-token-id
                    :eps-url (get-in endpoints-structured [:network :publicURL])
                    :id (get-in+  networks-response [:networks 0 :id])}))


  (-createSubnet (clojure-json->java-json
                  {:token-id new-token-id
                   :quantum-url (get-in endpoints-structured [:network :publicURL])
                   :network-id (get-in+ networks-response [:networks 0 :id])
                   :cidr "192.168.198.0/24"
                   :start "192.168.198.40"
                   :end "192.168.198.50"
                   }))


  (def response-create-subnet (java-json->clojure-json *1))

  (-serviceCall (clojure-json->java-json {:eps-token-id new-token-id :url (get-in  endpoints-structured  [:network :publicURL] ) :path "v2.0/subnets"}))

  (def subnets-response *1)

  (-deleteSubnet (clojure-json->java-json
                  {:eps-token-id new-token-id
                   :eps-url (get-in endpoints-structured [:network :publicURL])
                   :id (get-in+  subnets-response [:subnets 0 :id])}))




  (-createServer (clojure-json->java-json {:token-id new-token-id
                   :nova-url (get-in endpoints-structured [:compute :publicURL])
                   :server-name "the-server-name"
                   :flavor-href (get-in+ flavors-response [:flavors 0 :links 0 :href])
                   :image-href (get-in+ images-response [:images 0 :links 0 :href])
                   :network-id  (get-in+ networks-response [:networks 0 :id])
                   }))

  (def response-create-server *1)

  (-deleteServer (clojure-json->java-json {:eps-token-id new-token-id
                                           :eps-url (get-in endpoints-structured [:compute :publicURL])
                                           :id (get-in+ response-create-server [:server :id])}))


  )
