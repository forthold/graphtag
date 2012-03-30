(ns forthold.graphtag.mentions 
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [twitter.api.restful :as twitter-rest]
            [twitter.callbacks.handlers :as twitter-handlers])
  (:use [forthold.graphtag.common]
        [clojure.string :only (replace-first)])
  (:import [twitter.callbacks.protocols SyncSingleCallback])
  )

(defn get-mention-text [mention] 
   (replace-first (:text  mention) #"@graphtag" ""))

;; Create relationship as user and mention nodes already exist
(defn new-mention-link [mention] 
    (let [username (:screen_name (:user  mention))
         mention_id (:id mention)
         user-node (nodes/find index-user-name :username username)
         from-node (nodes/get (first (map :id  user-node)))
         mention-node (nodes/find index-mention-id :mentionid mention_id )
         to-node (nodes/get (first (map :id  mention-node)))
         created-rel  (relationships/create from-node to-node :tweet)
         ] 
      (println "******* New Relationship " (:id created-rel) " from " username " to mention id: " mention_id )
))

(defn create-mention-data [mention]
    (let [username (:screen_name  (mention))
          id (:id (mention))
          text (get-mention-text mention)
          username (:screen_name (:user mention))]
          { :text text :id (:id mention) :username username}))

;; Create new node for mention
(defn create-new-mention [mention]
    (let [ mention-node (nodes/create (create-mention-data mention))]
          (println "******* new mention" (:id mention-node) )
          ;;; add node to index keyed by mentionid  
          (nodes/add-to-index (:id mention-node) index-mention-id "mentionid" (:id mention))
          (new-mention-link mention)))

(defn mention-id-exists [id] 
   (let [ids (set (map :id (nodes/find index-mention-id :mentionid id )))]
      (complement (empty? ids))))
 
;; Process a mention.
(defn mention-handler [mention] 
   (println "*** Processing mention" (:id mention)); mention)
   ;; See if mentioner is present in user index and if not create and add.
   (if (not (user-id-exists (:user mention))) 
          (create-new-user (:user mention))
          (println "***** User " (:screen_name(:user mention)) (:id (:user mention)) " already exists"))

   (if (not (mention-id-exists (:id mention)))
          (create-new-mention mention)
          (println "***** Mention " (:id mention) "already processed")))

(defrecord MentionJob []
  org.quartz.Job
  (execute [this ctx]
    (println "* Mention Job Executing" (get-current-iso-8601-date))

    ;; Get mentions and map mention-handler over them
    (let [ result (twitter-rest/mentions :oauth-creds *creds*
                                         :callbacks (SyncSingleCallback. twitter-handlers/response-return-body 
                                                                         twitter-handlers/response-throw-error 
                                                                         twitter-handlers/exception-rethrow) 
                                         :params {:trim_user "false"} )] 
      (doseq [mention (seq result)] (mention-handler mention)))))


