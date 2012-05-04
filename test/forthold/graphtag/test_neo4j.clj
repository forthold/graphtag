(ns forthold.graphtag.test-neo4j
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojurewerkz.neocons.rest.paths         :as paths]
            [clojurewerkz.neocons.rest.cypher        :as cypher]
            [slingshot.slingshot :as slingshot])
  (:import [slingshot ExceptionInfo])
  (:use [clojure.test]
        [clojure.set :only [subset?]]
        [forthold.graphtag.common]
        [forthold.graphtag.test-common]
        [clojure.pprint :only [pprint]]
        [clojurewerkz.neocons.rest.records :only [instantiate-node-from instantiate-rel-from instantiate-path-from]]))


;  (neorest/connect! "http://localhost:7474/db/data/")
(defn neo4j-fixture [f]
  (neorest/connect! "http://localhost:7474/db/data/")
  (set-up-neo4j-indexes)
  (f)
  )

(use-fixtures :each neo4j-fixture)

(deftest test-connection-and-discovery-using-connect-with-string-uri
  (let [endpoint (neorest/connect "http://localhost:7474/db/data/")]
    ;(println endpoint)
    ;(println (:version endpoint))

    (is (:version                endpoint))
    (is (:node-uri               endpoint))
    (is (:batch-uri              endpoint))
    (is (:relationship-types-uri endpoint))))

(deftest test-lazy-index 
  (let [missing-username "graphtagtest2222"
        user_index_name "username"
        user-node ;(try 
                      (nodes/find index-user-name index-user-name-key missing-username) 
                  ;  (catch Exception e 
                      ;(  (is e )
                      ;   (println "In Exception"))))]
                  ;    ))]
          ]
        (dbg user-node)
        (is (empty? user-node))
  ))

(deftest test-get-vs-find-node 
  (println "Begin test-get-vs-find-node")
  (is (false? (user-name-exists username)))
  (let [created-user-id (:id (create-new-user (:user mention)))
        find-node (nodes/find index-user-name index-user-name-key username)
        get-node (nodes/get created-user-id)]
        (is (not-empty find-node))
        (is (not-empty get-node))
        (is (user-name-exists username))
        ;(println find-node)
        ;(println (first find-node))
        ;(println get-node)
        (is (= (first (map :id  find-node)) (:id  get-node)))
        (is (= (:id (first find-node))(first (map :id  find-node)) ))
        ;(println (:id  get-node))
        (delete-user-by-node-id created-user-id))
  (is (false? (user-name-exists username)))
  (println "End test-get-vs-find-node")
  )

