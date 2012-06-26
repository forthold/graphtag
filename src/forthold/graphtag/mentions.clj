(ns forthold.graphtag.mentions 
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [twitter.api.restful :as twitter-rest]
            [twitter.callbacks.handlers :as twitter-handlers])
  (:use [forthold.graphtag.common]
        [forthold.graphtag.tags]
        [clojure.string :only (replace-first)])
  (:import [twitter.callbacks.protocols SyncSingleCallback])
  )

(defn delete-mention-by-id [id]
    ;(dbg id)
    (nodes/delete-from-index id index-mention-id)
    (delete-node-rels-by-id id)  
    (nodes/delete id))
    ;(nodes/destroy id))

(defn get-mention-text [mention] 
   (replace-first (:text  mention) #"@graphtag" ""))

;; Create relationships between:
;; Mentioner --tweeted-> Mention
;; Mwntion -mentions -> Mentionee(GraphTag)
(defn link-mention-and-user [mention user-node mention-node]
   (let [root-node (get-root-node) 
         rel-node (relationships/create mention-node root-node :MENTIONS)
         rel-root (relationships/create user-node mention-node :TWEETED)]
     (println "****** New Relationship relid: " (:id rel-node)
              " \n ******* From " (:screen_name (:user mention)) 
              " nodeid: " (:id user-node)
              " \n ******* To mention twitterid: " (:id mention) 
              " nodeid: " (:id mention-node))
     (println "******* New Relationship relid: " (:id rel-root)
              " \n ******* From mention nodeid: " (:id mention-node)
              " \n ******* To root Graphtag nodeid: " (:id root-node))
     ))

(defn create-mention-data [mention]
    (let [mentioner (:screen_name (:user  mention))
          id (:id mention)
          text (get-mention-text mention)]
          { :text text :id (:id mention) :mentioner mentioner}))

;; Create new node for mention
(defn create-new-mention [mention]
    (let [ mention-node (nodes/create (create-mention-data mention))
           user-node (get-user-node-or-create (:user mention)) ]
          ;(dbg mention-node)
          (println "***** Creating New Mention nodeid:" (:id mention-node))
          (nodes/add-to-index (:id mention-node) index-mention-id index-mention-id-key (:id mention))
          (link-mention-and-user mention user-node mention-node)
          (tag-post-processing mention user-node mention-node)
          mention-node))

(defn mention-id-exists [id] 
   (not (nil? (:id (first (nodes/find index-mention-id index-mention-id-key id))))))
 
;; Process a mention.
(defn mention-handler [mention] 
   (println "*** Processing mention twitterid:" (:id mention) "\n **** text: " (:text mention))
   (if (mention-id-exists (:id mention))
          (println "**** Mention twitterid:" (:id mention) "already processed")
          (create-new-mention mention)))

(defrecord MentionJob []
  org.quartz.Job
  (execute [this ctx]
    (println "**********************************************************************************\n Mention Job Executing at: " (get-current-iso-8601-date) "\n*********************************************************************************" )
    ;; Get mentions and map mention-handler over them
    (let [ result (twitter-rest/mentions :oauth-creds *creds*
                                         :callbacks (SyncSingleCallback. twitter-handlers/response-return-body 
                                                                         twitter-handlers/response-throw-error 
                                                                         twitter-handlers/exception-rethrow) 
                                         :params {:trim_user "false"} )] 
      (doseq [mention (seq result)] (mention-handler mention)))))


