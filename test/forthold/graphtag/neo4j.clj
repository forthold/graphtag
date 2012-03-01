(ns forthold.graphtag.neo4j
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojurewerkz.neocons.rest.paths         :as paths]
            [clojurewerkz.neocons.rest.cypher        :as cypher]
            [slingshot.slingshot :as slingshot])
  (:import [slingshot ExceptionInfo])
  (:use [clojure.test]
        [clojure.set :only [subset?]]
        [clojure.pprint :only [pprint]]
        [clojurewerkz.neocons.rest.records :only [instantiate-node-from instantiate-rel-from instantiate-path-from]]))


(neorest/connect! "http://localhost:7474/db/data/")

;;debugging parts of expressions
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

;;
;; Connections/Discovery
;;

(deftest test-connection-and-discovery-using-connect-with-string-uri
  (let [endpoint (neorest/connect "http://localhost:7474/db/data/")]
    (println endpoint)
    (println (:version endpoint))

    (is (:version                endpoint))
    (is (:node-uri               endpoint))
    (is (:batch-uri              endpoint))
    (is (:relationship-types-uri endpoint))))

(deftest test-lazy-index 
  (println "**************************")    
  (let [username "graphtagtest" 
          user_index_name "node-index-user"
          user-node (nodes/find user_index_name :username username)]
        (println (map :id  user-node))
        (println (first (map :id  user-node)))
  ))


(defn mention-handler [mention] 
   (println "*** Processing mention" (:id mention)); mention)
   ;; See if mentioner is present in user index and if not add them
   (let [index_name "node-index-user"
         username (get-username mention)
         usersids (set (map :id (nodes/find index_name :username username)))]
      (if (empty? usersids)
          (new-user mention)))

   (println "*** Pasdfasdfrocessing mention" (:id mention)); mention)
   ;; If mention id is not in index then add new mention
   (let [index_name "node-index-mention-id"
         ids (set (map :id (nodes/find index_name :mentionid (:id mention))))]
      (if (empty? ids)
          (new-mention mention)
          (println "Mention already processed")))
  
   (println "*** 3333Processing mention" (:id mention)); mention)
  )

