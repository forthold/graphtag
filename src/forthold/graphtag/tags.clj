(ns forthold.graphtag.tags
  (:require [clojurewerkz.neocons.rest.nodes :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [twitter.api.restful :as twitter-rest]
            [twitter.callbacks.handlers :as twitter-handlers])
  (:use [forthold.graphtag.common]
        [clojure.string :only (split)])
  (:import [twitter.callbacks.protocols SyncSingleCallback])
  )

(defn create-new-tag [tag]
  (let [tag-node (nodes/create {:tag tag})]
    ;(dbg tag-node)
    ;add to index
    (nodes/add-to-index (:id tag-node) index-tag-name index-tag-name-text tag)
    tag-node
  ))

(defn tag-name-exists [tag] 
    (not (nil? (:id (first (nodes/find index-tag-name index-tag-name-key tag))))))

(defn get-tag-node-or-create [tag]
      (if (tag-name-exists tag)
        (do (println (str "***** Tag: " tag " already exists"))
            (first (nodes/find index-tag-name index-tag-name-key tag)))
        (create-new-tag tag)))


;; Create relationships between:
;; sender --uses ->  tag
;; tag --tags ->  dsl-node
;; HANDLE TAG REL EXISTS
(defn link-tag [tag sender dsl]
   (let [ uses-rel (relationships/maybe-create sender tag :USES_TAG)
          tags-rel (relationships/maybe-create tag dsl :TAG)]
     (if (not (nil? uses-rel)) 
     (println "******* New Uses Relationship relid: " (:id uses-rel)
              " \n ********* From user '" (-> sender :data :username) "' nodeid: " (:id sender)
              " \n ********* To tag '" (-> tag :data :tag) "' nodeid: " (:id tag)))
     (if (not (nil? tags-rel ))
       (println "******* New Tags Relationship relid: " (:id tags-rel)
              " \n ********* From tag '" (-> tag :data :tag) "' nodeid: " (:id tag)
              " \n ********* To dsl nodeid: " (:id dsl)))
     ))

(defn process-tag [tag sender dsl] 
  (println (str "***** Processing '" tag "' tag"))
  (let [tag-node (get-tag-node-or-create tag)]
      (link-tag tag-node sender dsl)
    ))

(defn  tag-post-processing [message sender-node dsl-node]
  (let [tag-text (first-match (nth (re-find #"\((.*?)\)" (:text message)) 1))]
       (if (not (nil? tag-text))
          (doseq [tag (seq (split tag-text #" "))] (process-tag tag sender-node dsl-node))
          (println "******* No DSL in DM")
         )
    ))

