(ns forthold.graphtag.test-redis
  (:use [forthold.graphtag.test-common]
        [forthold.graphtag.common]
        [clojure.test])
  (:require [redis.core :as redis])
)

(defn- server-version-local []
  (redis/with-server
   {:host "127.0.0.1"
    :port 6379
    :db 15
   }
   (Double/parseDouble (re-find  #"[0-9]\.[0-9]" ((redis/info) "redis_version")))))

(defn- server-version-heroku []
  (redis/with-server
   {:host "redis://redistogo:dcf09fe48c3d3534beaf940465fd4715@herring.redistogo.com:9509/"
   }
   (Double/parseDouble (re-find  #"[0-9]\.[0-9]" ((redis/info) "redis_version")))))

(deftest test-connection
  (let [version (server-version-local)
        heroku-version (server-version-heroku)]
  (dbg version)
  (dbg heroku-version)
  ))
