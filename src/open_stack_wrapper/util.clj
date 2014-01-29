(ns open-stack-wrapper.util
  (:use [clojure.pprint])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  )

(defn pprint-json-scheme
  "this fn analyze the json structure thinking that in vectors the elements repeat the same scheme,
   that's to say the elements of the vectors are of the same 'class' from an OO point of view.
   But not the same prediction on maps where each element have his identity(class) on the map, that's the point of the dictionary elements they are (class)    definitions
"
  [the-json-data]
  (let [keywordize (fn [v]
                     (let [s (.getSimpleName (type v))
                           t (keyword (clojure.string/lower-case s))]
                       (condp = t
                         :long "N"
                         :string "S"
                         :persistentarraymap "{}"
                         t)))
        deep-read (fn deep-read [jsondata]
             (cond
              (map? jsondata) (into {} (vec
                                  (map (fn [[json-key json-value]]
                                         (cond
                                          (nil? json-value) [json-key "nil"]
                                          (map? json-value) (if (empty? json-value) [json-key (keywordize {})] [json-key   (into {} (deep-read json-value))])
                                          (vector? json-value) (if (empty? json-value) [json-key []] [json-key [(deep-read (first json-value))]])
                                          :else [json-key (keywordize json-value)])) jsondata)))
              (vector? jsondata)[(deep-read (first jsondata))]
              :else (keywordize jsondata)))
        result  (into {}  (deep-read the-json-data))
        ]
    (pprint result)
    result
    )

  )

(comment
  (let [example {:key1 1
                  :key2 {:key3 "hola" :key4 {}}
                  :key5 [[{:key6 [{:key7 1}]}]]
                 }]
    (pprint-json-scheme example))
  )
