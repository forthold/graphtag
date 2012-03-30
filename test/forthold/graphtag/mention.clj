(ns forthold.graphtag.mention
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships])
  (:use [clojure.test]
        [forthold.graphtag.mentions]
        [forthold.graphtag.test-common]
        [forthold.graphtag.common]))


;; Set up test indexs and kill nodes after
(defn mention-fixture [f]
  (neorest/connect! "http://localhost:7474//db/data/")
  (set-up-neo4j-indexes)
  (f))

(use-fixtures :once mention-fixture)

(deftest test-user-id-exists-and-create-new-user
  (is (nil? (user-id-exists userid)))
  (let [ id (create-new-user (:user mention))]
    (is (user-id-exists userid))
   ; (nodes/delete-from-index id index-user-id)
   ; (nodes/delete id)
    )) 

(deftest test-mention-handler  
    (let [mention { :user mention}]

     ) 
  )

;(defn user-id-exists [id] 
;    (complement (empty? (set (map :userid (nodes/find index-user-id :userid id)))) ))
;
;  
;(defn mention-handler [mention] 
;   (println "*** Processing mention" (:id mention)); mention)
;   ;; See if mentioner is present in user index and if not create and add.
;   (if (not (user-id-exists (:user mention))) 
;          (create-new-user (:user mention))
;          (println "***** User " (:screen_name(:user mention)) (:id (:user mention)) " already exists"))
;
;   (if (not (mention-id-exists (:id mention)))
;          (create-new-mention mention)
;          (println "***** Mention " (:id mention) "already processed")))
