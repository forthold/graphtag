(ns forthold.graphtag.common
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [twitter.callbacks.handlers :as twitter-handlers]
            [twitter.oauth :as oauth]
            [clojurewerkz.neocons.rest.relationships :as relationships]
            [twitter.api.restful :as twitter-rest])
  (:import [twitter.callbacks.protocols SyncSingleCallback]
           [java.util Calendar]
           [java.text SimpleDateFormat ])
  )

;; Neo4j Index constants
(def ^:const index-user-id "userid")
(def ^:const index-user-id-text "id")
(def ^:const index-user-id-key :id)

(def ^:const index-user-name "username")
(def ^:const index-user-name-text "name")
(def ^:const index-user-name-key :name)

(def ^:const index-mention-id "mentionid")
(def ^:const index-mention-id-text "id")
(def ^:const index-mention-id-key :id)

(def ^:const index-follower-id "followerid")
(def ^:const index-follower-id-text "id")
(def ^:const index-follower-id-key :id)

(def ^:const graphtag-screen-name "GraphTag")
(def ^:const graphtag-id 491183182)

;; Define creds for twitter
;; TODO get this from a non source controled file
(def ^:dynamic *creds* (oauth/make-oauth-creds "GkqZgjg4QikY4lBt1G1A9A"
                         "TOCOK6S2w3ytKIZ8gi3iZ8RFewU5tA7kYebhiToxE2U" 
                         "491183182-M8HQywLDXLVVYuMPBNg03ZW58Ox6ysgHH9Bkc8QA"
                         "bGnRSufusyVki3gSJbaXvhZK1MKqewVw8E00aSfT48"))

(defn set-up-neo4j-indexes [] 
    (let [list (nodes/all-indexes)]
      (if (not (some (fn [i]
          (= index-user-name (:name i))) list))
      (nodes/create-index index-user-name))

      (if (not (some (fn [i]
          (= index-follower-id (:name i))) list))
      (nodes/create-index index-follower-id))

      (if (not (some (fn [i]
          (= index-mention-id (:name i))) list))
      (nodes/create-index index-mention-id))

      (if (not (some (fn [i]
          (= index-user-id (:name i))) list))
      (nodes/create-index index-user-id))))

;;debugging parts of expressions
(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(defn user-id-exists [id] 
    (not (nil? (:id (first (nodes/find index-user-id index-user-id-key id))))))

(defn user-name-exists [username] 
    (string? (get-in (first (nodes/find index-user-name index-user-name-key username)) [:data :username])))


(defn create-user-data [user]
    (let [username (:screen_name user)
          id (:id user)
          realname (:name user)
          description (:description user)
          icon (:profile_image_url user)
          bg (:profile_background_image_url user)]
          { :id id :realname realname 
            :description description :username username 
            :icon icon :bg bg})
  )

;; Creates a new user adds them to indexes and returns node id.
(defn create-new-user [user]
    ;;; Create new user node to link mention to
    (let [user-node (nodes/create (create-user-data user))]
        (nodes/add-to-index (:id user-node) index-user-name index-user-name-text (:screen_name user))
        (nodes/add-to-index (:id user-node) index-user-id index-user-id-text (:id user))
        (println "******* New user created, node id = " (:id user-node))
        user-node))

(defn create-new-user-from-id [id]
    (let [ user (twitter-rest/show-user 
              :callbacks (SyncSingleCallback. twitter-handlers/response-return-body 
                                              twitter-handlers/response-throw-error 
                                              twitter-handlers/exception-rethrow) 
              :params {:user_id id })] 
       (create-new-user user)))

(defn get-user-node-or-create [user]
      (if (user-name-exists (:screen_name user))
      ;(if (false? (user-name-exists (:screen_name user)))
        (first (nodes/find index-user-name index-user-name-key (:screen_name user)))
        (create-new-user user)))

(defn get-user-node-or-create-from-id [id]
      (if (user-id-exists id)
      ;(if (false? (user-name-exists (:screen_name user)))
        (first (nodes/find index-user-id index-user-id-key id))
        (create-new-user-from-id id)
      )
  )
(defn delete-node-rels-by-id [id] 
 (let [node (nodes/get id) 
       rels (relationships/all-for node) ]
   ;(dbg node)
   ;(dbg rels)
      (if (not-empty rels)
         (doseq [r rels] (relationships/delete (:id r)))
         (println "*** No relationships to delete"))))

(defn delete-user-by-node-id [id] 
  (delete-node-rels-by-id id)
  (nodes/delete-from-index id index-user-id)
  (nodes/delete-from-index id index-user-name)
  (nodes/delete-from-index id index-follower-id)
  (nodes/delete id))

(defn get-current-iso-8601-date
  "Returns current ISO 8601 compliant date."
  [] (.format (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ") (.getTime (Calendar/getInstance))))


(def VALID-CHARS
  (map char (concat (range 48 58) ; 0-9
                    (range 66 91) ; A-Z
                    (range 97 123)))) ; a-z

(defn random-char []
  (rand-nth VALID-CHARS))

(defn random-str [length]
  (apply str (take length (repeatedly random-char))))

;; Gets or Creats Root GraphTag node
(defn get-root-node [] 
  (get-user-node-or-create-from-id graphtag-id))
