;; ## A new way to think about programs
;;
;; What if your code and its documentation were one and the same?
;;
;; Much of the philosophy guiding literate programming is the realization of the answer to this question.
;; However, if literate programming stands as a comprehensive programming methodology at one of end of the
;; spectrum and no documentation stands as its antithesis, then Marginalia falls somewhere between. That is,
;; you should always aim for comprehensive documentation, but the shortest path to a useful subset is the
;; commented source code itself.
;;
;; ## The art of Marginalia
;;
;; If you’re fervently writing code that is heavily documented, then using Marginalia for your Clojure projects
;; is as simple as running it on your codebase. However, if you’re unaccustomed to documenting your source, then
;; the guidelines herein will help you make the most out of Marginalia for true-power documentation.
;;
;; Following the guidelines will work to make your code not only easier to follow – it will make it better.
;; The very process of using Marginalia will help to crystalize your understanding of problem and its solution(s).
;;
;; The quality of the prose in your documentation will often reflect the quality of the code itself thus highlighting
;; problem areas. The elimination of problem areas will solidify your code and its accompanying prose. Marginalia
;; provides a virtuous circle spiraling inward toward maximal code quality.
;;
;; ## The one true way
;;
;; 1. Start by running Marginalia against your code
;; 2. Cringe at the sad state of your code commentary
;; 3. Add docstrings and code comments as appropriate
;; 4. Generate the documentation again
;; 5. Read the resulting documentation
;; 6. Make changes to code and documentation so that the “dialog” flows sensibly
;; 7. Repeat from step #4 until complete
(ns open-stack-wrapper.rest
  (:use [open-stack-wrapper.core])
   (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY]]))
(defroutes app
  (ANY "/" [] (resource :available-media-types ["application/json"]
                        :handle-ok  {:success true}))
  (ANY "/tokens" [] (resource :available-media-types ["application/json"]
                              :handle-ok (fn [ctx]  {:success true :token-id (get-in (tokens) [:access :token :id])})))
  (ANY  "/endpoints/:tenant" [tenant]  (resource :allowed-methods [:post :get]
                                          :available-media-types ["application/json"]
                                          :handle-ok (fn [_]  {:success true :endpoints (structured-endpoints (endpoints tenant))}))))



(def handler
  (-> app
      (wrap-params)))
(comment
 (run-jetty #'handler {:port 3000}))

(defn -main [port]
   (run-jetty #'handler {:port (Integer. port) :join? false})
)
