(ns forthold.graphtag.cypher
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as rel]
            [clojurewerkz.neocons.rest.paths         :as paths]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojure.data.json :as json]
            [clojurewerkz.neocons.rest.cypher        :as cy])
  (:use [forthold.graphtag.common]
        [clojurewerkz.neocons.rest.records :only [instantiate-node-from instantiate-rel-from instantiate-path-from]]))


(defn get-tags [] 
    (let [id (-> (get-root-node) :data :id)
          {:keys [data columns]} 
            (cy/query "start g = node:userid(id={sid}) 
                          match g-[FOLLOWS]-u-[:USES_TAG]-t 
                          return distinct t.tag as tag" {:sid id }) 
          [data2] (cy/tquery "start g = node:userid(id={sid}) 
                          match g-[FOLLOWS]-u-[:USES_TAG]-t 
                          return distinct t.tag as tag" {:sid id }) ]
          ;(dbg data)
          (println "\n 0*********** \n")
          (println data)
          (println "\n 1*********** \n")
          (println (json/json-str data))
          (println "\n 2*********** \n")
          (println (get data2 "tag")
          data 
      )
    )
  )

(defn handle-row [row]
   ; (if (vector? row)
   ;  (do 
   ;    (println "asdfaf")
   ;    (println (count row) (row 0) (row 1))
       ;{:name (row 0), :children [{:name (row 1)}]}
       {(keyword (row 0)) (row 1)}
   ;    )
   ;  (do 
   ;    (println "asdfafasdfadsf1")
   ;   (println (type row)))
   ;   )

  )

(defn kv [bag [k v]] 
    (update-in bag [k] conj v))

(defn mergeMatches [propertyMapList]
      (reduce #(reduce kv %1 %2) {} propertyMapList))

(defn single-entry [value]
  ;(println "444" (type value))
    {:name value})

(defn childrenify [entry]
    ;(println "000" (type entry))
    ;(println "111j" (type (val entry)))
    ;(println "2222" (val entry))
    (let [children (map single-entry (val entry))]
    ;(let [children (doseq [child-list (val entry)] (single-entry child-list))]
    ;  (println "0022l0" (type children))
    ;  (println "0022lasdfads0" children)
      {:name (key entry), :children children})
  )

(defn get-user-data [id] 
    (let [ {:keys [data columns]} 
            (cy/query "start f = node:userid(id={sid}) 
                          match f-[:USES_TAG]-c-[:TAG]-m 
                          where f-->m return c.tag,m.text" 
                      {:sid id }) 
          ;(dbg data)
          ;(println data)
          ;(println "\n 1 *********** \n")
          ;(println (json/json-str data))
          ;(println "\n 2 *********** \n")
          ;{:name id, children {}}
          ;(map println data)
          ;(doseq [r data]  (handle-row r))
           children (map handle-row data)
           merged (mergeMatches children) 
           d3ed (map childrenify merged)
          ]
      ;(println merged) 
      ;(json/json-str {:name (str id), :children children})
      ;(json/json-str d3ed)
     ; children
      (json/json-str {:name (str id), :children d3ed})
      ;d3ed
      ;merged
           
          ;data
      ))



(defn get-user-data-path [id] 
    (let [ [data] (cy/tquery "start f = node:userid(id={sid}) 
                          match path = f-[:USES_TAG]-c-[:TAG]-m 
                          where f-->m return path, f.username"
                      {:sid id }) ]
          ;(dbg data)
        ;  (println "\n *********** \n")
        ;  (println (get data "path"))
        ;  (println "\n *********** \n")
        ;  (println (first data))
          (println "\n 1*********** \n")
          (println first data)
          (println "\n 2*********** \n")
          (println (get data "path"))
          (println "\n 3*********** \n")
        ;  (println (instantiate-path-from (map instantiate-node-from (first  data))))
        ;  (println "\n *********** \n")
          (println (instantiate-path-from (get data "path")))
      data
      ))
