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
        [forthold.graphtag.common]
        [forthold.graphtag.test-common]
        [clojure.pprint :only [pprint]]
        [clojurewerkz.neocons.rest.records :only [instantiate-node-from instantiate-rel-from instantiate-path-from]]))


  (neorest/connect! "http://localhost:7474/db/data/")
;(defn neo4j-fixture [f]
;  (neorest/connect "http://localhost:7474/db/data/")
;  (f)
;  )
;
;(use-fixtures :each neo4j-fixture)

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
          user-node (nodes/find index-user-name :username missing-username)]
        (is (empty user-node))
  ))

(deftest test-get-vs-find-node 
  (println "Begin test-get-vs-find-node")
  (is (nil? (user-name-exists username)))
  (let [created-user-id (create-new-user (:user mention))
        find-node (nodes/find index-user-name :username username)
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
        (delete-user-by-id created-user-id))
  (is (nil? (user-name-exists username)))
  (println "End test-get-vs-find-node")
  )
;
;(deftest test-user-id-exists-and-create-new-user
;  (is (nil? (user-id-exists userid)))
;  (is (nil? (user-name-exists username)))
;  (let [ id (create-new-user (:user mention))]
;    (is (user-id-exists userid))
;    (is (user-name-exists username))
;    ;; TODO create a delete user function
;    (nodes/delete-from-index id index-user-id)
;    (nodes/delete-from-index id index-user-name)
;    (nodes/delete id)
;    ) 
;  (is (nil? (user-name-exists username)))
;  (is (nil? (user-id-exists userid))))
;(defn mention-handler [mention] 
;   (println "*** Processing mention" (:id mention)); mention)
;   ;; See if mentioner is present in user index and if not add them
;   (let [index_name "node-index-user"
;         username (get-username mention)
;         usersids (set (map :id (nodes/find index_name :username username)))]
;      (if (empty? usersids)
;          (new-user mention)))
;
;   (println "*** Pasdfasdfrocessing mention" (:id mention)); mention)
;   ;; If mention id is not in index then add new mention
;   (let [index_name "node-index-mention-id"
;         ids (set (map :id (nodes/find index_name :mentionid (:id mention))))]
;      (if (empty? ids)
;          (new-mention mention)
;          (println "Mention already processed")))
;  
;   (println "*** 3333Processing mention" (:id mention)); mention)
;  )
;
