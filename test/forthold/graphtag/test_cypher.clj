(ns forthold.graphtag.test-cypher
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as rel]
            [clojurewerkz.neocons.rest.paths         :as paths]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojurewerkz.neocons.rest.cypher        :as cy])
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

(deftest test-cypher 
    (let [id "307693455"
          data2 (cy/query "start f = node:userid(id={sid}) match f-[:USES_TAG]-c-[:TAG]-m where f-->m return c,m" {:sid id })
          {:keys [data columns]} 
            (cy/query "start f = node:userid(id={sid}) match f-[:USES_TAG]-c-[:TAG]-m where f-->m return c,m" {:sid id })
          row1  (map instantiate-node-from (first  data))
          row2  (map instantiate-node-from (second data))
          ;path   (instantiate-path-from (get row "c")) 
          ]
          (dbg data2)
          ;(is (= 2 (count data)))
          ;(is (= ["john" "fof"] columns))
          ;(is (same-node? john (first row1)))
          ;(is (same-node? maria (last row1)))
          ;(is (same-node? john (first row2)))
          ;(is (same-node? steve (last row2)))))
      (dbg row1)
      (dbg row2)
      ))

