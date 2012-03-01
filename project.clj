(defproject forthold.graphtag "0.1.0-SNAPSHOT"
            :description "GraphTag Twitter to Neo4j"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [compojure "1.0.1"]
                           ;[noir "1.2.1"]
                           [clojurewerkz/neocons "1.0.1-SNAPSHOT"]
                           [twitter-api "0.6.4"]
                           [clojurewerkz/quartzite "1.0.0-beta1"]]
            :main forthold.graphtag.server)
