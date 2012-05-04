(ns forthold.graphtag.test-followers
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships])
  (:use [clojure.test]
        [forthold.graphtag.test-common]
        [forthold.graphtag.followers]
        [forthold.graphtag.common]))

(def ^:const following-user-id 507201238)

;; Set up test indexs and kill nodes after
(defn follower-fixture [f]
  (neorest/connect! "http://localhost:7474/db/data/")
  (set-up-neo4j-indexes)
  (f))

(use-fixtures :once follower-fixture)

(deftest test-new-follower
  (is (false? (user-id-exists following-user-id)))
  (let [ follower-node (follower-handler following-user-id)]
    (is (user-id-exists following-user-id))
    (let [follower-node2 (follower-handler following-user-id)
           rels (relationships/all-for follower-node2) ]
        (is (= (:id follower-node) (:id follower-node2)))
        ;test rel exists to root
        (is rels)
        (is (= 1 (count rels)))
        (delete-user-by-node-id (:id follower-node))
        (is (false? (user-id-exists following-user-id)))
        ;; run follower handler again and ensure another user is not created 
    )))

(deftest test-follower-exists 
    (is (false? (follower-id-exists following-user-id)))
    (let [user (follower-handler following-user-id)
          ]
      (is user)
      (is (follower-id-exists following-user-id))
      (delete-user-by-node-id (:id user))
      (is (false? (user-id-exists following-user-id)))
      ))
