(ns open-stack-wrapper.util-test
  (:require [open-stack-wrapper.util :refer :all]
    [clojure.test :refer :all]))


(deftest test-pprint-json-scheme
  (testing "some tries"
    (is (= {:key1 "N",
            :key2 {:key3 "S", :key4 "{}"},
            :key5 [[{:key6 [{:key7 "N"}]}]]}
           {:key1 "N",
            :key2 {:key3 "S", :key4 "{}"},
            :key5 [[{:key6 [{:key7 "N"}]}]]}
           (pprint-json-scheme {:key1 1 :key2 {:key3 "hola" :key4 {}} :key5 [[{:key6 [{:key7 1}]}]]})))))
