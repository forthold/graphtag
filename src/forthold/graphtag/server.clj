(ns forthold.graphtag.server
  (:require [noir.server :as server])
  (:use [clojurewerkz.quartzite.conversion]
        [twitter.api.restful])
  (:require [clojurewerkz.quartzite.scheduler :as sched]
            [clojurewerkz.quartzite.jobs      :as j]
            [clojurewerkz.quartzite.triggers  :as t]
            [clojurewerkz.quartzite.schedule.simple :as s]
            [clojurewerkz.quartzite.schedule.calendar-interval :as calin])
  (:import [java.util.concurrent CountDownLatch]
           [org.quartz.impl.matchers GroupMatcher]))

(server/load-views "src/forthold/graphtag/views/")

(sched/initialize)
(sched/start)

(defrecord JobA []
  org.quartz.Job
  (execute [this ctx]
    (println "aran")
   ;get followers
    ;see if they are new if so send DM
    ;get mentions
    ;; if they have code then process and put in neo4j
    ;; 
    ))

(def testQuartz1
  (let [job     (j/build
                 (j/of-type forthold.graphtag.server.JobA)
                 (j/with-identity "job1" "tests"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/with-repeat-count 10)
                                    (s/with-interval-in-milliseconds 200))))]
    (sched/schedule job trigger)
    ))


(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'forthold.graphtag}))
  (println "Hello World")
  (let [job     (j/build
                 (j/of-type forthold.graphtag.server.JobA)
                 (j/with-identity "job1" "tests"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    (s/with-repeat-count 10)
                                    (s/with-interval-in-milliseconds 200))))]
    (sched/schedule job trigger)
    ))
  ;(testQuartz1 ))
