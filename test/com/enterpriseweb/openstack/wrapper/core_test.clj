(ns com.enterpriseweb.openstack.wrapper.core-test
  (:require [com.enterpriseweb.openstack.wrapper.core :refer :all]
            [com.enterpriseweb.openstack.wrapper.util :refer (load-config)]
            [midje.sweet :refer :all]))

(facts ""
       (fact ""
             1 => 1))
(comment
         (def login-properties (load-config "./login.properties"))
         (def username (:username login-properties))
         (def password (:password login-properties))
         (def url (:url login-properties))

         (tokens url username password)
         (def tokens-response *1)
         (def token-id (get-in tokens-response [:access :token :id]))
         (tenants url  token-id )
         (def tenants-response *1)
         (endpoints-adaptated {:url url :username "admin" :password "password" :tenant-name "admin"})
         (def endpoints-response *1)
         (def token-eps (:token-id endpoints-response))
         (def eps-structured (:eps endpoints-response))
         (service-call token-eps (get-in eps-structured [:compute :publicURL]) "/servers")
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
                        "juan-server"
                        (get-in flavors-response [:flavors 0 :links 0 :href])
                        (get-in images-response [:images 0 :links 0 :href])
                        (get-in networks-response [:networks 1 :id]))
         (service-call token-eps (get-in eps-structured [:compute :publicURL]) "/servers")
         (def servers-response *1)
         (delete token-eps (str (get-in eps-structured [:compute :publicURL])
                                "/servers/"
                                (get-in servers-response [:servers 0 :id])))
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
