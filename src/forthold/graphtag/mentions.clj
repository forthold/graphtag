(ns forthold.graphtag.mentions 
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [twitter.api.restful :as twitter-rest]
            [twitter.callbacks.handlers :as twitter-handlers])
  (:use [forthold.graphtag.common]
        [clojure.string :only (replace-first)])
  (:import [twitter.callbacks.protocols SyncSingleCallback])
  )

(defn delete-mention-by-id [id]
    (nodes/delete-from-index id index-mention-id)
    (nodes/delete id))

(defn get-mention-text [mention] 
   (replace-first (:text  mention) #"@graphtag" ""))

;; Create relationships between:
;; Mentioner --tweeted-> Mention
(defn link-mention-and-user [mention mention-node]
   (println "******* New Relationship from " (:screen_name (:user mention)) " to mention id: " (:id mention))
   (relationships/create (get-user-node-or-create (:user mention)) mention-node :tweet))

(defn create-mention-data [mention]
    (let [username (:screen_name (:user  mention))
          id (:id mention)
          text (get-mention-text mention)]
          { :text text :id (:id mention) :username username}))

;; Create new node for mention
(defn create-new-mention [mention]
    (let [ mention-node (nodes/create (create-mention-data mention))]
          (println "******* New Mention" (:id mention-node))
          (nodes/add-to-index (:id mention-node) index-mention-id "mentionid" (:id mention))
          (link-mention-and-user mention mention-node)
          mention-node))

(defn mention-id-exists [id] 
   (not (nil? (:id (first (nodes/find index-mention-id :mentionid id))))))
 
;; Process a mention.
(defn mention-handler [mention] 
   (println "*** Processing mention" (:id mention)); mention)
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


