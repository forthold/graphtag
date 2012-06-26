(ns ^{:author "Aran C Elkington"
      :doc "Graphtag allows integration between Neo4j and twitter."}
    forthold.graphtag.server
  (:use  [clojurewerkz.quartzite.conversion]
         [clj-time.core :only [now minus plus days hours minutes secs]]
         [forthold.graphtag.common]
         [forthold.graphtag.followers]
         [forthold.graphtag.direct]
         [forthold.graphtag.mentions])
  (:require [noir.server :as server]
            [clojurewerkz.quartzite.scheduler :as sched]
            [clojurewerkz.quartzite.jobs      :as j]
            [clojurewerkz.quartzite.triggers  :as t]
            [clojurewerkz.quartzite.schedule.simple :as s]
            [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            )
  (:import [java.util Date]
           [org.joda.time DateTime]))
  

(defn setup-quartz-jobs [] 
  (let [mention-job     (j/build
                 (j/of-type forthold.graphtag.mentions.MentionJob)
                 (j/with-identity "forthold.graphtab.server.mention" "processMentions"))
        follow-job     (j/build
                 (j/of-type forthold.graphtag.followers.FollowJob)
                 (j/with-identity "forthold.graphtab.follow" "processFollows"))
        message-job     (j/build
                 (j/of-type forthold.graphtag.direct.MessageJob)
                 (j/with-identity "forthold.graphtab.direct" "processMessages"))

        mention-trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/repeat-forever)
                                    (s/with-interval-in-seconds 60))))
        
       follow-start-time  (.toDate ^DateTime (plus (now) (secs 15)))
       follow-trigger  (t/build
                  (t/start-at follow-start-time)
                  (t/with-schedule (s/schedule
                                    (s/repeat-forever)
                                    (s/with-interval-in-minutes 3))))
      
       message-start-time  (.toDate ^DateTime (plus (now) (secs 30)))
       message-trigger  (t/build
                  (t/start-at message-start-time)
                  (t/with-schedule (s/schedule
                                    (s/repeat-forever)
                                    (s/with-interval-in-minutes 1))))]

    (sched/schedule mention-job mention-trigger)
    (sched/schedule follow-job follow-trigger)
    (sched/schedule message-job message-trigger)))

(server/load-views "src/forthold/graphtag/views/")

(defn -main [& m]
  ;; Load webapps views
  (server/load-views "src/forthold/graphtag/views/")

  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))
        url (get (System/getenv) "NEO4J_REST_URL")
        user (get (System/getenv) "NEO4J_LOGIN")
        pass  (get (System/getenv) "NEO4J_PASSWORD")
        start-jobs  (get (System/getenv) "START_JOBS")
        redisurl (get (System/getenv) "REDISTOGO_URL") ]
  
    (println "Welcome to Graphtag: Neo4j url:" url " user: " user " pass:" pass)
    (println "Start Jobs = " start-jobs)
    ;; Start Noir
    (server/start port {:mode mode
                          :ns 'forthold.graphtag})
    ;; Setup Neo4j connection
    ;(neorest/connect! "http://a9d1efcc4.hosted.neo4j.org:7062/db/data")

    (neorest/connect! url user pass)

  ;; TODO Set up redis with neo4j traversal of mentions and followers
  ;; Redis will hold: followers, mentions, mentioners
  (set-up-neo4j-indexes)
  (get-root-node)
  (if (true? start-jobs)
    (do
      ;start quartz
      (sched/initialize)
      (sched/start)
      ;; Schedule Quartz jobs and trigger
      (setup-quartz-jobs))
    )))

