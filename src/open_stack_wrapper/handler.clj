(ns open-stack-wrapper.handler
   (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

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
