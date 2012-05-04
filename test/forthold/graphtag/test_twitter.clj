(ns forthold.graphtag.test-twitter
  (:use  [clojure.test]
         [clojure.pprint]
         [twitter.oauth]
         [twitter.callbacks]
         [twitter.callbacks.handlers]
         [twitter.api.restful]
         [forthold.graphtag.common]
         [forthold.graphtag.followers])
  (:import [twitter.callbacks.protocols SyncSingleCallback]))

;;debugging parts of expressions

;; Define creds for twitter
;(def ^:dynamic *creds* (make-oauth-creds "GkqZgjg4QikY4lBt1G1A9A"
;                         "TOCOK6S2w3ytKIZ8gi3iZ8RFewU5tA7kYebhiToxE2U" 
;                         "491183182-M8HQywLDXLVVYuMPBNg03ZW58Ox6ysgHH9Bkc8QA"
;                         "bGnRSufusyVki3gSJbaXvhZK1MKqewVw8E00aSfT48"))

(deftest test-get-followers []
  (let [ result (show-followers :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) 
:params {:screen_name "GraphTag" })] 
    ;(dbg result)
    ;(println (result :body))
  ;(doseq [mention (seq result)] (mention-handler mention))
    (is (not (empty? result)))))

(deftest test-get-users []
  (let [ result (show-user ;:callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) 
:params {:screen_name "graphtag" })] 
;:params {:screen_name "graphtagtest" })] 
;:params {:user_id "307693455" })] 
    ;(dbg result)
    (pprint result)
  ;(doseq [mention (seq result)] (mention-handler mention))
    (is (not (empty? result)))))

(defn test-direct-message [] 
    (let [id 507201238
          result (send-direct-message :oauth-creds *creds*
                                      :params {:user_id id :text (create-message-text id)})]
    ;    (dbg result)
      ))

