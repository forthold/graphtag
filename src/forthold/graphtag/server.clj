(ns forthold.graphtag.server
  (:use  [clojurewerkz.quartzite.conversion]
         [twitter.oauth]
         [twitter.callbacks]
         [twitter.callbacks.handlers]
         [twitter.api.restful]
         [clojure.set :only [subset?]]
         [clojure.pprint :only [pprint]]
         [clojure.string :only (replace-first)])
  (:require; [noir.server :as server]
            [clojurewerkz.quartzite.scheduler :as sched]
            [clojurewerkz.quartzite.jobs      :as j]
            [clojurewerkz.quartzite.triggers  :as t]
            [clojurewerkz.quartzite.schedule.simple :as s]
            [clojurewerkz.quartzite.schedule.calendar-interval :as calin]
            [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [clojurewerkz.neocons.rest.paths         :as paths]
            [clojurewerkz.neocons.rest.cypher        :as cypher]
            [slingshot.slingshot :as slingshot]
            [clj-http.client   :as http]
            )
  (:import [java.util.concurrent CountDownLatch]
           [org.quartz.impl.matchers GroupMatcher]
            (twitter.callbacks.protocols SyncSingleCallback)
           [slingshot ExceptionInfo]))

;(server/load-views "src/forthold/graphtag/views/")

;;debugging parts of expressions
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

;start quartz
(sched/initialize)
(sched/start)

;; Define creds for twitter
(def ^:dynamic *creds* (make-oauth-creds "GkqZgjg4QikY4lBt1G1A9A"
                         "TOCOK6S2w3ytKIZ8gi3iZ8RFewU5tA7kYebhiToxE2U" 
                         "491183182-M8HQywLDXLVVYuMPBNg03ZW58Ox6ysgHH9Bkc8QA"
                         "bGnRSufusyVki3gSJbaXvhZK1MKqewVw8E00aSfT48"))

;; Setup Neo4j connection
(neorest/connect! "http://localhost:7474/db/data/")


(defn test-connection-and-discovery-using-connect-with-string-uri []
  (let [endpoint (neorest/connect "http://localhost:7474/db/data/")]
    (println endpoint)
    (println (:version endpoint))
    ))


;; Handle a map of mentions keyed on metion id
;;; Will put id in neo4j index first checking if its there
;(defn mentionsMapHandler [map_of_mentions] 
;  ;work out what mentions havent been processed
;  (let [mention_ids (keys map_of_mentions)]
;      (println "mention ids =" mention_ids) 
;     )
;  )

;(defn test-if-in-index [index mentionid] 
;    (let [ids (set (map :id (nodes/find index :mentionid mentionid)))]
;         (dbg ids)
;        (if (empty? ids)
;            (nodes/add-to-index mentionid index "mentionid" url1)
;      )    
;  )
(defn get-mention-text [mention] 
   (replace-first (:text  mention) #"@graphtag" ""))

(defn get-username [mention] 
  (let [user (get mention :user) ]
    (dbg user) 
    (:screen_name user)))
;   (let [user (select-keys mention :user)]
;     (dbg user)
;     (:screen_name user)))
;
(defn new-mention [mention]
  (println "In new:")  
  (println "In new mention" (get-mention-text mention)  (get-username mention))
    ;;; Create new node for mention
    (let [text (get-mention-text mention) 
          username (get-username mention)
          data { :text text :id (:id mention) :username username}
          node1 (nodes/create data)]
        (dbg node1)
        ;;; Add node to index keyed by mentionid  
        (nodes/add-to-index (:id node1) "node-index-mention-id" "mentionid" (:id mention))
        ;;; create relationship to user root node
      )
  )
;
(defn mention-handler [mention] 
   (println "***** In Mentions Handler: mention = " mention)

   (let [index_name "node-index-mention-id"
         ids (set (map :id (nodes/find index_name :mentionid (:id mention))))]
      (dbg ids)
      (if (empty? ids)
          (new-mention mention))    
;   (test-if-in-index "node-index-mention-id" (:id value))
;   (let [created-node (nodes/create)]
;      (println "node**"  (:id created-node) ))
;    ;    (println "asd33" (:version  endpoint))
;    ;    (dbg endpoint))
     ))

(defrecord JobA []
  org.quartz.Job
  (execute [this ctx]
    (println "**** Job Executing")
    ;; Get mentions and map mention-handler over them
    (let [ result (mentions :oauth-creds *creds* 
                            :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) 
                            :params {:trim_user "false"} )] 
      ;(dbg result)   
      ;(dbg (seq result))
      (doseq [mention (seq result)] (mention-handler mention)))))

(defn -main [& m]
;  (let [mode (keyword (or (first m) :dev))
;        port (Integer. (get (System/getenv) "PORT" "8080"))]
;    (server/start port {:mode mode
;                        :ns 'forthold.graphtag}))
  (println "Welcome to Graphtag")

  ;;Neo4j config
  (test-connection-and-discovery-using-connect-with-string-uri)
  
  ; If index does not exist create it
  (let [name "node-index-mention-id"
        list (nodes/all-indexes) ]
    (if (not (some (fn [i]
                (= name (:name i))) list))
    (nodes/create-index name)))

  ;; Schedule Quartz job and trigger
  (let [job     (j/build
                 (j/of-type forthold.graphtag.server.JobA)
                 (j/with-identity "job1" "tests"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/with-repeat-count 2)
                                    (s/with-interval-in-milliseconds 2000))))]
    (sched/schedule job trigger))
  )
