(ns open-stack-wrapper.handler
   (:require [clj-http.client :as client]
             [clojure.data.json :as json]

)
(:use [slingshot.slingshot :only [throw+ try+]])   )

(defn manage-error [e]
  (let [get-error (let [data-ex (.getData e)]
                    (json/read-str (get-in data-ex [:object :body]) :key-fn keyword))
        error (:error get-error)]
    {:success false :title (:title error) :message (:message error) :code (:code error)})





  )
(defn get-response-body
    "having a json-string response  obtain the body on json format"
    [response]
    (json/read-str (:body response) :key-fn keyword))



(defmacro adapt-call-delete [body ]
  `(try+
    (let [r# (get-response-body ~body)]
      {:success true :title "delete operation did NOT return an exception. It means it worked ok!"})
    (catch Object e#
      (condp = (:status e#)
          401 {:success false :title "Unauthorized call"}
          404 {:success false :title "Item not found exception"}
          409 {:success false :title "Conflict"}
           {:success true :title "delete operation did NOT return an exception. It means it worked ok!"}
          )
     )

    )

  )

(defmacro adapt-call [body]
  `(try
    (assoc (get-response-body ~body) :success true)
    (catch java.net.MalformedURLException e# {:success false :title (type e#)})
    (catch java.net.UnknownHostException e# {:success false :title (type e#)})
    (catch clojure.lang.ExceptionInfo e# (manage-error e#))
    (catch Exception e# {:success false :ex (type e#)})


    )

  )
