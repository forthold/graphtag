(ns ^{:author "Aran C Elkington"
      :doc "Graphtag allows integration between Neo4j and twitter."}
    forthold.graphtag.server
  (:use  [clojurewerkz.quartzite.conversion]
         [forthold.graphtag.common]
         [forthold.graphtag.followers]
         [forthold.graphtag.mentions])
  (:require [noir.server :as server]
            [clojurewerkz.quartzite.scheduler :as sched]
            [clojurewerkz.quartzite.jobs      :as j]
            [clojurewerkz.quartzite.triggers  :as t]
            [clojurewerkz.quartzite.schedule.simple :as s]
            [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            )
           )


;; Load webapps views
(server/load-views "src/forthold/graphtag/views/")

(defn setup-quartz-jobs [] 
  (let [mention-job     (j/build
                 (j/of-type forthold.graphtag.mentions.MentionJob)
                 (j/with-identity "forthold.graphtab.server.mention" "processMentions"))
        follow-job     (j/build
                 (j/of-type forthold.graphtag.followers.FollowJob)
                 (j/with-identity "forthold.graphtab.smentionserver.follow" "processFollows"))
        mention-trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/repeat-forever)
                                    (s/with-interval-in-seconds 60))))
       follow-trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/repeat-forever)
                                    (s/with-interval-in-minutes 3))))]
    (sched/schedule mention-job mention-trigger)
    (sched/schedule follow-job follow-trigger)))


(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
  
  (println "Welcome to Graphtag: Neo4j:" (get (System/getenv) "NEO4J_REST_URL"))
  ;; Start Noir
  (server/start port {:mode mode
                        :ns 'forthold.graphtag}))
  ;; Setup Neo4j connection
  ;(neorest/connect! "http://a9d1efcc4.hosted.neo4j.org:7062/db/data")

  (neorest/connect! "http://a9d1efcc4.hosted.neo4j.org:7062/db/data/" "0e5d0bb39" "2d1a471df")
  (set-up-neo4j-indexes)
  ;start quartz
  (sched/initialize)
  (sched/start)
  ;; Schedule Quartz jobs and trigger
  (setup-quartz-jobs)
  )

