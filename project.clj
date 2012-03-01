(defproject forthold.graphtag "0.1.0-SNAPSHOT"
            :description "GraphTag Twitter to Neo4j"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           ;[compojure "1.0.1"]
                           ;[noir "1.2.1"]
                           [ring/ring-jetty-adapter "1.1.0-SNAPSHOT"]
                           [ring "1.0.2"]
                           [forthold/neocons "1.0.2-SNAPSHOT"]
                           ;[clojurewerkz/neocons "1.0.1-SNAPSHOT"]
                           [twitter-api "0.6.4"]
                           [clojurewerkz/quartzite "1.0.0-beta1"]]
            :main forthold.graphtag.server
            :dev-dependencies [[clj-time                  "0.3.5" :exclusions [org.clojure/clojure]]
                     [org.clojure/tools.logging "0.2.3" :exclusions [org.clojure/clojure]]
                     [org.slf4j/slf4j-simple    "1.6.2"]
                     [org.slf4j/slf4j-api       "1.6.2"]
                     [log4j                     "1.2.16" :exclusions [javax.mail/mail
                                                                      javax.jms/jms
                                                                      com.sun.jdmk/jmxtools
                                                                      com.sun.jmx/jmxri]]]
 )
