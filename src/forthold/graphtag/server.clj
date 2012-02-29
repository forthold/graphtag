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

(sched/initialize)
(sched/start)

;(def endpoint (neorest/connect! "http://localhost:7474/db/data/"))

;(neorest/connect! "http://localhost:7474/db/data/")

(def ^:dynamic *creds* (make-oauth-creds "GkqZgjg4QikY4lBt1G1A9A"
                         "TOCOK6S2w3ytKIZ8gi3iZ8RFewU5tA7kYebhiToxE2U" 
                         "491183182-M8HQywLDXLVVYuMPBNg03ZW58Ox6ysgHH9Bkc8QA"
                         "bGnRSufusyVki3gSJbaXvhZK1MKqewVw8E00aSfT48"))

; simply retrieves the user, authenticating with the above credentials
; note that anything in the :params map gets the -'s converted to _'s
;(show-followers :oauth-creds *creds* :params {:screen-name "graphtag"})

; shows the users friends, without using authentication
;(show-friends :params {:screen-name "graphtag"})

;(def response-error-throw (println "Error"))
;(def exception-rethrow (println "Exception"))

;; Make a OAuth consumer
;(def oauth-consumer (oauth/make-consumer "GkqZgjg4QikY4lBt1G1A9A"
;                                         "TOCOK6S2w3ytKIZ8gi3iZ8RFewU5tA7kYebhiToxE2U" 
;                                         "https://api.twitter.com/oauth/request_token"
;                                         "https://api.twitter.com/oauth/access_token"
;                                         "https://api.twitter.com/oauth/authorize"
;                                         :hmac-sha1))
;
;(def oauth-access-token )
;     ;; Look up an access token you've stored away after the user
;     ;; authorized a request token and you traded it in for an
;     ;; access token.  See clj-oauth (http://github.com/mattrepl/clj-oauth) for an example.)
;(def oauth-access-token-secret)
;     ;; The secret included with the access token)
;
;;; Post to twitter
;(twitter/with-oauth oauth-consumer 
;                    oauth-access-token
;                    oauth-access-token-secret
;                    (twitter/update-status "posting from #clojure with #oauth"))
;
; 
;; Find out who follows dons
;    (dbg (show-friends :oauth-creds *creds* :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) :params {:screen-name "graphtag"}))

(neorest/connect! "http://localhost:7474/db/data/")


;;
;; Connections/Discovery
;;

(defn test-connection-and-discovery-using-connect-with-string-uri []
  (let [endpoint (neorest/connect "http://localhost:7474/db/data/")]
    (println endpoint)
    (println (:version endpoint))
    ))

;(neorest/connect "http://localhost:7474/db/data/")

(defn mentionHandler [mentions] 
   (let [men_text (replace-first (:text (nth mentions 0)) #"@graphtag" "")]
      (println "adsfsdfasdfads" men_text) 
     )
  )

; Handle a map of mentions keyed on metion id
;; Will put id in neo4j index first checking if its there
(defn mentionsMapHandler [map_of_mentions] 
  ;work out what mentions havent been processed
  (let [mention_ids (keys map_of_mentions)]
      (println "mention ids =" mention_ids) 
     )
  )

;(defn GET
;  [^String uri & { :as options }]
;  (io!
;   (http/get uri (merge options { :accept :json }))))
(defn test-if-in-index [index mentionid] 
    (let [ids (set (map :id (nodes/find index :mentionid mentionid)))]
         (dbg ids))    
  )
;
(defn mentionsHandler22 [value] 
  ;(let [ urlNeo (GET "http://a9d1efcc4.hosted.neo4j.org:7057/db/data/" )]
    ;(println urlNeo ))
  ;(let [endpoint (neorest/connect! "http://a9d1efcc4.hosted.neo4j.org:7057/db/data/") ]
   (println "***** In Mentions Handler: mention = " value)
   (println "asdfsaf9")

   (test-if-in-index "node-index-mention-id" (:id value))
;   (let [created-node (nodes/create)]
;      (println "node**"  (:id created-node) ))
;    ;    (println "asd33" (:version  endpoint))
;    ;    (dbg endpoint))
     )

(defrecord JobA []
  org.quartz.Job
  (execute [this ctx]
    (println "**** Job Executing")
   ;get followers
    ;(println (show-followers :oauth-creds *creds* :params {:screen-name "graphtag"}))
;    (println (show-friends :oauth-creds *creds* :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) :params {:screen-name "graphtag"}))
        
;    (println (home-timeline :oauth-creds *creds* :params {:trim_user "false"} ))
;     (dbg  (mentions :oauth-creds *creds* :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) :params {:trim_user "false"} ))
       (let [ result (mentions :oauth-creds *creds* :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow) :params {:trim_user "false"} )] 
         ;convert vector to map of maps keyed on mention id
;         (println "*******************wwqqw*" result )
         ;(println "********************"(:text (nth result 0)))
         ;(mentionsMapHandler(#(zipmap (map :id %) %) result))
         ;(let [idv (map vector (iterate inc 0) result)]
         ;  (dbg idv)  
         ;(doseq [[value] result] (mentionsHandler22 value))
         (doseq [value (seq result)] (mentionsHandler22 value))
         ;(println "are we gettigng here")
         ;(doseq [value (seq result)] (println "1111111"  value))
         ;(println "********************2"(nth result 0))
         )
 
                                                                                                                                                                                          ;(get result :text)) )
     ;(dbg  (mentions :oauth-creds *creds* :params {:trim_user "false"} ))
;    (println (show-user :oauth-creds *creds* :params {:screen-name "graphtag"} ))
    ;see if they are new if so send DM
    ;get mentions
    ;; if they have code then process and put in neo4j
    ;; 
    ))

;(def testQuartz1
;  (let [job     (j/build
;                 (j/of-type forthold.graphtag.server.JobA)
;                 (j/with-identity "job1" "tests"))
;        trigger  (t/build
;                  (t/start-now)
;                  (t/with-schedule (s/schedule
;                                    (s/with-repeat-count 10)
;                                    (s/with-interval-in-milliseconds 200))))]
;    (sched/schedule job trigger)
;    ))


(defn -main [& m]
;  (let [mode (keyword (or (first m) :dev))
;        port (Integer. (get (System/getenv) "PORT" "8080"))]
;    (server/start port {:mode mode
;                        :ns 'forthold.graphtag}))
  (println "Welcome to Graphtag")

  ;;Neo4j config
  (test-connection-and-discovery-using-connect-with-string-uri)
  
  (let [name "node-index-mention-id"]
    (nodes/create-index name))

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
  ;(testQuartz1 ))
