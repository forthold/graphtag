(ns forthold.graphtag.test-followers
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships])
  (:use [clojure.test]
        [forthold.graphtag.test-common]
        [forthold.graphtag.followers]
        [forthold.graphtag.common]))

(def ^:const mentioning-user-id 123)

;; Set up test indexs and kill nodes after
(defn follower-fixture [f]
  (neorest/connect! "http://localhost:7474/db/data/")
  (set-up-neo4j-indexes)
  (f))

(use-fixtures :once follower-fixture)

(deftest test-new-follower
  (follower-handler mentioning-user-id)
  (is (user-id-exists mentioning-user-id))
  (delete-user-by-id mentioning-user-id)
  (is (false? (user-id-exists mentioning-user-id)))
  )
