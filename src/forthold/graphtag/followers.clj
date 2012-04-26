(ns forthold.graphtag.followers 
  (:use [forthold.graphtag.common])
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [twitter.callbacks.handlers :as twitter-handlers]
            [twitter.api.restful :as twitter-rest])
  (:import [twitter.callbacks.protocols SyncSingleCallback])
)

(defn create-message-text [id] 
    ("Please visit http://forthold.com/" id)
  )

(defn handle-new-follower [id] 
    (let [ user-node (get-user-node-or-create-from-id id)]
    (println "***** Follower Created twitterid: " id " nodeid: " (:id user-node)))
    (twitter-rest/send-direct-message :oauth-creds *creds*
                                      :params {:user_id id :text (create-message-text id)}))

(defn follower-id-exists [id] 
    (not (nil? (:id (first (nodes/find index-follower-id :id id))))))

;; TODO Could  use Redis to check what we already have before you process all of them this goes for mentions too.
;; also could use redis to remove those who no londer follow.
(defn follower-handler [id] 
  (println "*** Processing Follower twitterid: " id)
  (if (not (follower-id-exists id))
    (handle-new-follower id)
    (println "***** Follower twitterid: " id " already exists")))

;; TODO could think about an existing mentioning user who then follows not recieving a message.
(defrecord FollowJob []
  org.quartz.Job
  (execute [this ctx]
    (println "* Follow Job Executing" (get-current-iso-8601-date))
    ;; Get mentions and map mention-handler over them
    (let [ result (twitter-rest/show-followers :callbacks (SyncSingleCallback. twitter-handlers/response-return-body 
                                                                  twitter-handlers/response-throw-error 
                                                                  twitter-handlers/exception-rethrow) 
                                  :params {:screen-name graphtag}) ] 
      (doseq [id (:ids result)] (follower-handler id)))))
