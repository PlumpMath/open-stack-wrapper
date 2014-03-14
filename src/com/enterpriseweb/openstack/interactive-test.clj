(ns com.enterpriseweb.openstack.interactive-test
  (:use [com.enterpriseweb.openstack.OpenStackAPI]
        [com.enterpriseweb.json.util]
        [open-stack-wrapper.util :as util]
        [open-stack-wrapper.core :as os-core])
  (:import [org.json JSONObject]))



(comment

  (def login-properties (util/load-config "./login.properties"))

  (-makeCall (-> (create-java-json login-properties :url :username :password)
                (assoc+ :action :tokens)))

  (def -tokens-response *1)

  (-makeCall (-> (create-java-json login-properties :url)
                (assoc+ :token-id (get-in+ -tokens-response [:access :token :id]))
                (assoc+ :action :tenants)))

  (def -tenants-response *1)

  (-makeCall (-> (create-java-json login-properties :url :password :username)
                  (assoc+ :tenant-name "admin" #_(get-in+ -tenants-response [:tenants 0 :name]))
                  (assoc+ :action :endpoints)))

  (def -endpoints-response  *1)

  (def new-token-id (get-in+ -endpoints-response [:token-id]))

  (def endpoints-structured (get-in+ -endpoints-response [:eps]) )



  #_(map (juxt :name :publicURL) (vals endpoints-structured))

  "starting with endpoints stored"

  (-makeCall (clojure-json->java-json
                 {:eps-token-id new-token-id
                  :url (get-in  endpoints-structured  [:compute :publicURL] )
                  :action :list-images
                  }))



  (def images-response *1)

  (-makeCall (clojure-json->java-json
                 {:eps-token-id new-token-id
                  :url (get-in  endpoints-structured  [:compute :publicURL] )
                  :action :list-flavors}))

  (def flavors-response *1)

  ;(util/pprint-json-scheme (java-json->clojure-json images-response))

  ;(map (juxt :id :name #(:href (first (:links %)))) (:images  (java-json->clojure-json images-response)))

  (-makeCall (clojure-json->java-json
                 {:eps-token-id new-token-id
                  :url (get-in  endpoints-structured  [:network :publicURL])
                  :action :list-networks}))

  (def networks-response *1)

   ;(map (juxt :id :name ) (:networks (java-json->clojure-json networks-response)))

  (-makeCall (clojure-json->java-json
                   {:network-name "juan-network-7"
                    :quantum-url (:publicURL (:network endpoints-structured))
                    :token-id new-token-id
                    :action :create-network}))


  (def response-create-network *1)

  (-makeCall (clojure-json->java-json
                   {:eps-token-id new-token-id
                    :eps-url (get-in endpoints-structured [:network :publicURL])
                    :id (get-in+  networks-response [:networks 1 :id])
                    :action :delete-network}))


  (-makeCall (clojure-json->java-json
                  {:token-id new-token-id
                   :quantum-url (get-in endpoints-structured [:network :publicURL])
                   :network-id (get-in+ networks-response [:networks 0 :id])
                   :cidr "192.168.1.0/24" #_"192.168.198.0/24"
                   :start "192.168.198.40"
                   :end "192.168.198.50"
                   :action :create-subnet
                   }))


  (def response-create-subnet (java-json->clojure-json *1))

  (-makeCall (clojure-json->java-json
                 {:eps-token-id new-token-id
                  :url (get-in  endpoints-structured  [:network :publicURL] )
                  :action :list-subnets}))

  (def subnets-response *1)

  (-makeCall (clojure-json->java-json
                  {:eps-token-id new-token-id
                   :eps-url (get-in endpoints-structured [:network :publicURL])
                   :id (get-in+  subnets-response [:subnets 0 :id])
                   :action :delete-subnet}))

  (-makeCall (clojure-json->java-json
                  {:token-id new-token-id
                   :nova-url (get-in endpoints-structured [:compute :publicURL])
                   :server-name "the-server-name"
                   :flavor-href (get-in+ flavors-response [:flavors 0 :links 0 :href])
                   :image-href (get-in+ images-response [:images 0 :links 0 :href])
                   :network-id  (get-in+ networks-response [:networks 0 :id])
                   :action :create-server}))

  (def response-create-server *1)

  (-makeCall (clojure-json->java-json {:eps-token-id new-token-id
                                      :eps-url (get-in endpoints-structured [:compute :publicURL])
                                      :id (get-in+ response-create-server [:server :id])
                                      :action :delete-server}))
  )
