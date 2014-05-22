(ns com.enterpriseweb.json.util-test
  (:require [com.enterpriseweb.json.util :refer :all]
            [clojure.data.json :as clj-json]
            [clojure.test :refer :all]
            [midje.sweet :refer :all]
            ))
#_(deftest test-json-object
  (testing "FIXME, I fail."
    (is (= (iterator-seq (.keys (org.json.JSONObject.))) (iterator-seq(.keys (create-java-json-object (clj-json/write-str {:a "1"}))))))
    ))



(comment facts "starting ew tests"
       (fact "mocking deps"
             10 => 10
             )
       )




#_(run-tests)
