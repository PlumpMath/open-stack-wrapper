(ns open-stack-wrapper.core
  (:use [open-stack-wrapper.util]
        [open-stack-wrapper.mock])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  )


(defn manage-error [e]
  (let [get-error (let [data-ex (.getData e)]
                    (json/read-str (get-in data-ex [:object :body]) :key-fn keyword))
        error (:error get-error)]
    (str (:title error) ". " (:message error) " Code:" (:code error))))

(defn get-response-body
  "having a json-string response  obtain the body on json format"
  [response] (json/read-str (:body response) :key-fn keyword))

(defmacro adapt-call [body]
  `(try
    (get-response-body ~body)
    (catch clojure.lang.ExceptionInfo e# (manage-error e#))))

(defn tokens []
  (adapt-call (client/post "http://8.21.28.222:5000/v2.0/tokens"
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
  (adapt-call (client/get "http://8.21.28.222:5000/v2.0/tenants"
               {:headers {"X-Auth-Token" token}
                :content-type :json
                :socket-timeout 2000 ;; in milliseconds
                :conn-timeout 2000   ;; in milliseconds
                :accept :json}))
  )

(defn endpoints [tenant-name]
  (adapt-call (client/post "http://8.21.28.222:5000/v2.0/tokens"
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

(defn process-endpoints [eps-json]
  (let [access (:access eps-json)
        endpoints (:serviceCatalog access)]
    (map :endpoints endpoints))

  )



(defn get-token-id[response-body-json] (-> response-body-json :access :token :id))


(defn get-token-id [data]
  (get-in data [:access :token :id]))

(comment   "pprinting data"

  (pprint-json-scheme endpoints-mock))

(defn store-structured-endpoints [data]
  (let [services (get-in data [:access :serviceCatalog])]
    (reduce
     (fn [c it]
       (let [first-endpoint (first (:endpoints it))]
         (assoc c
           (keyword (:type  it))
           {:name (:name it)
            :id (:id first-endpoint)
            :publicURL (:publicURL first-endpoint)}))) {}  services)))

(comment "having endpoints give me 'compute' endpoints"
         (:compute (store-strutctured-endpoints endpoints-mock)))

(defn operation  [tenant-name service-type path]
  (let [
        ep2 (endpoints tenant-name)

        token-id (get-in ep2 [:access :token :id])
        publicURL (get-in  (store-structured-endpoints ep2) [service-type :publicURL] )
        url (str publicURL "/" (name path) )]

    (adapt-call (client/get url
                            {:headers {"X-Auth-Token" token-id}
                             :content-type :json
                             :socket-timeout 2000 ;; in milliseconds
                             :conn-timeout 2000   ;; in milliseconds
                             :accept :json}))


    )
  )

(comment "get :compute :images of _tenant_selected"

  (operation "facebook1428467850" :compute :images)
  )
