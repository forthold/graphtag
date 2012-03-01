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
           [java.util Calendar]
           [java.text SimpleDateFormat ]
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
(neorest/connect! "http://a9d1efcc4.hosted.neo4j.org:7057/db/data/" "0e5d0bb39" "2d1a471df")
;(neorest/connect! "http://localhost:7474/db/data/")


(defn get-mention-text [mention] 
   (replace-first (:text  mention) #"@graphtag" ""))

(defn get-username [mention] 
  (let [user (get mention :user) ]
    ;(dbg user) 
    (:screen_name user)))

(defn new-mention-link [mention] 
    (let [
          user_index_name "node-index-user"
          mention_index_name  "node-index-mention-id"
          username (get-username mention)
          mention_id (:id mention)
          user-node (nodes/find user_index_name :username username)
          from-node (nodes/get (first (map :id  user-node)))
          mention-node (nodes/find mention_index_name :mentionid mention_id )
          to-node (nodes/get (first (map :id  mention-node)))
          created-rel  (relationships/create from-node to-node :tweet)
          ] 
      (println "******* New Relationship" (:id created-rel) )
))

;(defn test-creating-and-immediately-accessing-a-relationship-without-properties [mention]
;  (let [from-node    (nodes/create)
;        to-node      (nodes/create)
;        created-rel  (relationships/create from-node to-node :links)
;        fetched-rel  (relationships/get (:id created-rel))]
;      (dbg created-rel)
;      (println "******* NEW Relationship" (:id created-rel) )
;))
;
(defn new-mention [mention]
    ;;; Create new node for mention
    (let [text (get-mention-text mention) 
          username (get-username mention)
          data { :text text :id (:id mention) :username username}
          mention-node (nodes/create data)]
          (println "******* NEW mention" (:id mention-node) )
          ;(dbg mention-node)
          ;;; Add node to index keyed by mentionid  
          (nodes/add-to-index (:id mention-node) "node-index-mention-id" "mentionid" (:id mention))
          ;;; create relationship to user root node
          ;(test-creating-and-immediately-accessing-a-relationship-without-properties mention)
          (new-mention-link mention)
            ))

(defn get-user-id [mention]
  (let [user (get mention :user) ]
    (:id user)))

(defn get-user-icon [mention]
  (let [user (get mention :user) ]
    (:profile_image_url user)))

(defn get-user-bg [mention]
  (let [user (get mention :user) ]
    (:profile_background_image_url user)))

(defn new-user [mention]
    ;;; Create new user node to link mention to
    (let [username (get-username mention)
          id (get-user-id mention)
          icon (get-user-icon mention)
          bg (get-user-bg mention)
          data { :id id :username username :icon icon :bg bg}
          user-node (nodes/create data)]
        ;;; Add user node to index keyed by name  
        (nodes/add-to-index (:id user-node) "node-index-user" "username" username)
        (println "******* New user" (:id user-node))
  ))

;
(defn mention-handler [mention] 
   (println "*** Processing mention" (:id mention)); mention)
   ;; See if mentioner is present in user index and if not add them
   (let [index_name "node-index-user"
         username (get-username mention)
         usersids (set (map :id (nodes/find index_name :username username)))]
      (if (empty? usersids)
          (new-user mention)))

   ;; If mention id is not in index then add new mention
   (let [index_name "node-index-mention-id"
         ids (set (map :id (nodes/find index_name :mentionid (:id mention))))]
      (if (empty? ids)
          (new-mention mention)
          (println "Mention already processed"))))

(defn get-current-iso-8601-date
  "Returns current ISO 8601 compliant date."
  [] (.format (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ") (.getTime (Calendar/getInstance))))

(defrecord JobA []
  org.quartz.Job
  (execute [this ctx]
    (println "* Job Executing" (get-current-iso-8601-date))
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
  ;(test-connection-and-discovery-using-connect-with-string-uri)
  
  ; If mention index does not exist create it
  (let [name "node-index-mention-id"
        list (nodes/all-indexes) ]
    (if (not (some (fn [i]
                (= name (:name i))) list))
    (nodes/create-index name)))

  ; If user index does not exist create it
  (let [user-index-name "node-index-user"
        list (nodes/all-indexes) ]
    (if (not (some (fn [i]
                (= user-index-name (:name i))) list))
    (nodes/create-index user-index-name)))

  ;; Schedule Quartz job and trigger
  (let [job     (j/build
                 (j/of-type forthold.graphtag.server.JobA)
                 (j/with-identity "forthold.graphtab.server.mention" "processMentions"))
        trigger  (t/build
                  (t/start-now)
                  (t/with-schedule (s/schedule
                                    ;(s/with-repeat-count 2)
                                    (s/repeat-forever)
                                    (s/with-interval-in-seconds 60))))]
    (sched/schedule job trigger))
  )
