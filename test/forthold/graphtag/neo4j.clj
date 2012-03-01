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
;;
;; Working with relationships
;;

(deftest test-creating-and-immediately-accessing-a-relationship-without-properties
  (let [from-node    (nodes/create)
          user_index_name "node-index-user"
          mention_index_name  "node-index-mention-id"
          username "graphtagtest"
         ; mention_id (:id mention)
         ; user-node (nodes/find user_index_name :username username)
          ;from-node (nodes/get (:id user-node))
         ; from-node (nodes/create)
         ; from-node2 (nodes/get (:id from-node))
         ; mention-node (nodes/find mention_index_name :mentionid mention_id )
        ;to-node      (nodes/create)
        to-node      (nodes/get 400)
        from-node      (nodes/get 402)
        created-rel  (relationships/create from-node to-node :links)
        fetched-rel  (relationships/get (:id created-rel))]
    (dbg to-node)
    (dbg from-node)
    (dbg created-rel)
    (dbg fetched-rel)
    (is (= (:id created-rel) (:id fetched-rel)))
    (is (= (:type created-rel) (:type fetched-rel)))))

(deftest test-lazy-index 
  (println "**************************")    
  (let [username "graphtagtest" 
          user_index_name "node-index-user"
          user-node (nodes/find user_index_name :username username)]
        (println "****************"  user-node)
        (println "****************"  (type user-node ))
        (doseq [node [user-node]] (println (type node)))
        (doseq [node [user-node]] (println  node))
        (println (map :id  user-node))
        (println (first (map :id  user-node)))
    ;(is (= (user-node) (user-node))) 
  ))

  
