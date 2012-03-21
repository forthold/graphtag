(ns forthold.graphtag.twitter
  (:use  [clojure.test]
         [twitter.oauth]
         [twitter.callbacks]
         [twitter.callbacks.handlers]
         [twitter.api.restful])
  (:import [twitter.callbacks.protocols SyncSingleCallback]))

;;debugging parts of expressions
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

;; Define creds for twitter
;(def ^:dynamic *creds* (make-oauth-creds "GkqZgjg4QikY4lBt1G1A9A"
;                         "TOCOK6S2w3ytKIZ8gi3iZ8RFewU5tA7kYebhiToxE2U" 
;                         "491183182-M8HQywLDXLVVYuMPBNg03ZW58Ox6ysgHH9Bkc8QA"
;                         "bGnRSufusyVki3gSJbaXvhZK1MKqewVw8E00aSfT48"))

;(deftest test-get-followers []
;  (let [ result (show-followers :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) 
;:params {:screen_name "GraphTag" })] 
;    (dbg result)
;    (println (result :body))
;  ;(doseq [mention (seq result)] (mention-handler mention))
;    (is (not (empty? result)))))

(deftest test-get-users []
  (let [ result (show-user ;:callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) 
:params {:user_id "307693455" })] 
    (dbg result)
    (println (result :body))
  ;(doseq [mention (seq result)] (mention-handler mention))
    (is (not (empty? result)))))

