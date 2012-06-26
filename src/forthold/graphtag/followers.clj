(ns forthold.graphtag.followers 
  (:use [forthold.graphtag.common])
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [twitter.callbacks.handlers :as twitter-handlers]
            [twitter.api.restful :as twitter-rest]
            [clojurewerkz.neocons.rest.relationships :as relationships])
  (:import [twitter.callbacks.protocols SyncSingleCallback])
)

(defn create-message-text [id] 
    ( str  "Please visit http://forthold.com/" id (random-str 5)))

(defn user-not-already-followed [id friends]
        (not (some #(= id %) (:ids friends)))
)

(defn following-logic-handler [id user-node friends]
  ; if a user is not already followed then follow them.
  (if (user-not-already-followed id friends)
      (do 
        ; Follow the user, it is possible a request has already been sent but not accepted so a 409 results
        (try
            (twitter-rest/create-friendship :oauth-creds *creds* :params {:user_id id })
          (catch Exception e 
            (println "**********************************************************************************\n Caught Exception: " 
                     (.getMessage e) 
                       "\n*********************************************************************************" )))

        ; Send a DM
        (println "***** Sending direct message to follower name: "
                 (-> user-node :data :username) 
                 " \n ****** Text is: " (create-message-text id))
        (twitter-rest/send-direct-message :oauth-creds *creds*
                                          :params {:user_id id :text (create-message-text id)})
        ))
  )

;; Create relationships between:
;; Mentioner --tweeted-> Mention
(defn handle-new-follower [id friends] 
    (let [ user-node (get-user-node-or-create-from-id id)
           root (get-root-node) ]
      (println "**** Creating new Follower twitterid: " id 
               " name: " (-> user-node :data :username)
               " nodeid " (:id user-node) 
               " root nodeid: " (:id root))
      (following-logic-handler id user-node friends)
      (nodes/add-to-index (:id user-node) index-follower-id index-follower-id-text id)
      (relationships/create user-node root :FOLLOWS)))

(defn follower-id-exists [id] 
    (not (nil? (:id (first (nodes/find index-follower-id index-follower-id-key id))))))

;; TODO Could  use Redis to check what we already have before you process all of them this goes for mentions too.
;; also could use redis to remove those who no londer follow.
(defn follower-handler [id friends] 
  (println "*** Processing Follower twitterid: " id)
  (if (follower-id-exists id)
    (println "***** Follower twitterid: " id " already exists")
    (handle-new-follower id friends)))

;; TODO could think about an existing mentioning user who then follows not recieving a message.
(defrecord FollowJob []
  org.quartz.Job
  (execute [this ctx]
    (println "**********************************************************************************\n" 
             " Follow Job Executing at: " 
             (get-current-iso-8601-date) 
             "\n*********************************************************************************" )
    ;; Get mentions and map mention-handler over them
    (try 
      (let [friends (twitter-rest/show-friends :oauth-creds *creds* 
                               :callbacks (SyncSingleCallback. twitter-handlers/response-return-body 
                                                                  twitter-handlers/response-throw-error 
                                                                  twitter-handlers/exception-rethrow) 
                               :params {:user_id (-> (get-root-node)  :data :id)})
            followers (twitter-rest/show-followers 
                         :callbacks (SyncSingleCallback. twitter-handlers/response-return-body 
                                                                  twitter-handlers/response-throw-error 
                                                                  twitter-handlers/exception-rethrow) 
                         :params {:screen-name graphtag-screen-name}) ] 
          (dbg friends)
          (doseq [id (:ids followers)] (follower-handler id friends)))
    (catch Exception e 
      (println "**********************************************************************************\n" 
               "Caught Exception Getting Followers and friends: "
               (.getMessage e) 
               "\n*********************************************************************************" )))))
