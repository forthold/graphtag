(ns forthold.graphtag.direct
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [twitter.api.restful :as twitter-rest]
            [twitter.callbacks.handlers :as twitter-handlers])
  (:use [forthold.graphtag.common]
        [clojure.string :only (replace-first)])
  (:import [twitter.callbacks.protocols SyncSingleCallback])
  )


;; Create relationships between:
;; sender --sent ->  message
;; root --recieved ->  message
(defn link-sender-to-message [sender message]
   (let [ root (get-root-node) 
         sent-rel (relationships/create sender message :sent)
         recieved-rel (relationships/create root message :recieved)]
     (println "******* New Sent Relationship relid: " (:id sent-rel)
              " from user nodeid: " (:id sender)
              " to message nodeid: " (:id message))
     (println "******* New Recieved Relationship relid: " (:id recieved-rel)
              " from root nodeid: " (:id root)
              " to message nodeid: " (:id message))
     ))

(defn create-message-data [message]
    { :message (:text message) :id (:id message) })

(defn create-new-message [dm] 
  (let [dm-node (nodes/create (create-message-data dm)) 
        sender-node (get-user-node-or-create (:sender dm))]
          (nodes/add-to-index (:id dm-node) index-dm-id index-dm-id-key (:id dm))
          (println "******* Creating New Message nodeid:" (:id dm-node))
          (link-sender-to-message sender-node dm-node)
          dm-node ))

(defn message-id-exists [id] 
   (not (nil? (:id (first (nodes/find index-dm-id index-dm-id-key id))))))

;; Process a mention.
(defn message-handler [dm] 
   (println "*** Processing direct message twitterid:" (:id dm))
   (if (message-id-exists (:id dm))
          (println "***** Direct Message twitterid:" (:id dm) "already processed")
          (create-new-message dm)))

(defrecord MessageJob []
  org.quartz.Job
  (execute [this ctx]
    (println "* Direct Message Job Executing" (get-current-iso-8601-date))

    ;; Get mentions and map mention-handler over them
    (let [ result (twitter-rest/direct-messages :oauth-creds *creds*
                       :callbacks (SyncSingleCallback. 
                                   twitter-handlers/response-return-body 
                                   twitter-handlers/response-throw-error 
                                   twitter-handlers/exception-rethrow))]
      (doseq [message (seq result)] (message-handler message)))))


