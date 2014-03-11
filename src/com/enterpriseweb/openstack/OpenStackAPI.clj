(ns com.enterpriseweb.openstack.OpenStackAPI
  (:use [com.enterpriseweb.json.util]
        [open-stack-wrapper.core :as os-core]
        [open-stack-wrapper.util :as util])
  (:gen-class :methods
              [#^{:static true} [makeCall [org.json.JSONObject] org.json.JSONObject]]))


(defn dispatch
  "MIDDLEWARE java-json-adapter: takes a java-json and returns a java-json"
  [json-java-object action-fn  ks]
  (let [-clj-object (java-json->clojure-json json-java-object)
        -selected-data (select-keys -clj-object ks)
        action-result  (action-fn -selected-data)
        to-json-java (clojure-json->java-json action-result)
        ]
                                        ;    (println  action-fn -selected-data)
                                        ;   action-result
    to-json-java
    ))

                                        ; helper
(defn json-delete-adapter [json-java-object path]
  (let [first-url (get-in+ json-java-object [:eps-url])
        id (get-in+ json-java-object [:id])
        modified-json (assoc+ json-java-object :url (str first-url path id))]
    modified-json))

(defn delete-entity [json-java-object path]
  (let [modified-json (json-delete-adapter json-java-object path)]
    (dispatch modified-json os-core/delete  :url :eps-token-id )))

                                        ; public API

                                        ;TODO throw exception if function is not evaluated!!
(defn mapping [option]
  (condp = option
    :tokens [os-core/tokens nil :url :username :password]
    :tenants [os-core/tenants nil :token-id :url]
    :endpoints [os-core/endpoints-adaptated nil  :url :username :password :tenant-name]
    :list-images [os-core/service-call
                  (fn [j]
                    (assoc+ j :path "/images"))
                  :url :eps-token-id :path]
    :list-flavors [os-core/service-call
                   (fn [j]
                     (assoc+ j :path "/flavors"))
                   :url :eps-token-id :path]
    :list-networks [os-core/service-call
                    (fn [j]
                      (assoc+ j :path "v2.0/networks"))
                    :url :eps-token-id :path]
    :list-subnets [os-core/service-call
                    (fn [j]
                      (assoc+ j :path "v2.0/subnets"))
                    :url :eps-token-id :path]
    :delete-network [os-core/delete
                     (fn [j]
                       (json-delete-adapter j "v2.0/networks/")
                       )
                     :url :eps-token-id]
    :delete-subnet [os-core/delete
                     (fn [j]
                       (json-delete-adapter j "v2.0/subnets/")
                       )
                    :url :eps-token-id]
    :delete-server [os-core/delete
                     (fn [j]
                       (json-delete-adapter j "/servers/")
                       )
                     :url :eps-token-id]

    :create-network [os-core/create-network
                     nil
                     :token-id :quantum-url :network-name]
    :create-subnet [os-core/create-subnet
                    nil
                    :token-id :quantum-url :network-id :cidr :start :end]
    :create-server [os-core/create-server
                    nil
                    :token-id :nova-url :server-name :flavor-href :image-href :network-id]


    ))


(defn -makeCall [json-java-object]
  (let [[fn data-adapter-fn & more] (mapping (keyword (get-in+ json-java-object [:action])))]
    (if (nil? data-adapter-fn)
      (dispatch json-java-object fn more)
      (dispatch (data-adapter-fn json-java-object) fn more))))
