(ns forthold.graphtag.mention
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships])
  (:use [clojure.test]
        [forthold.graphtag.mentions]
        [forthold.graphtag.test-common]
        [forthold.graphtag.common]))


;; Set up test indexs and kill nodes after
(defn mention-fixture [f]
  (neorest/connect! "http://localhost:7474/db/data/")
  (set-up-neo4j-indexes)
  (f))

(use-fixtures :once mention-fixture)

(deftest test-user-id-exists-and-create-new-user
  (is (false? (user-id-exists userid)))
  (is (false? (user-name-exists username)))
  (let [ id (:id (create-new-user (:user mention)))]
    (is (user-id-exists userid))
    (is (user-name-exists username))
    ;; TODO create a delete user function
    (nodes/delete-from-index id index-user-id)
    (nodes/delete-from-index id index-user-name)
    (nodes/delete id)
    ) 
  (is (false? (user-name-exists username)))
  (is (false? (user-id-exists userid))))

(deftest test-mention-id-exists-and-create-new-mention-and-user
  (is (false? (mention-id-exists mentionid)))
  (is (false? (user-name-exists username)))
  (let [ mention-node (create-new-mention mention)
         id (:id mention-node)
         ;user-node (get-user-node-or-create user)
         user-node (first (nodes/find index-user-name :username username))
         user-node-id (:id user-node)
         user-node-returned (get-user-node-or-create user)
         rel-id (:id (first (relationships/all-for mention-node)))]
    (is (= (:id user-node) (:id user-node-returned)))
    (is user-node)
    (is rel-id)
    (is (mention-id-exists mentionid))
    (is (user-name-exists username))
    (relationships/delete rel-id)
    (delete-user-by-id user-node-id)
    (nodes/delete-from-index id index-mention-id)
    (nodes/delete id)
    ) 
  (is (false? (user-name-exists username)))
  (is (false? (mention-id-exists mentionid))))

; Test that user and link are created
(deftest test-link-mention-and-user 
  (is (false? (user-id-exists userid)))
  (let [ mention-node (create-new-mention mention)
        ;This creates another link to be deleted
        ;rel (link-mention-and-user mention mention-node)
        user-node (first (nodes/find index-user-name :username username))
        user-node2 (nodes/find index-user-name :username username)
        user-node-id (:id user-node)
        tweets (relationships/all-for mention-node) ]
    (is not-empty tweets)
    (is (user-name-exists username))
    (is (= 1 (count tweets)))
    (relationships/delete (:id (first tweets)))
    (is (empty? (relationships/all-for mention-node)))
    (delete-mention-by-id (:id mention-node))
    (delete-user-by-id user-node-id)
    (is (false? (user-name-exists username)))
    (is (false? (mention-id-exists mentionid)))))


;;example response to statuses/mention
;;[
;;    {
;;        "id": 31,
;;        "created_at": "Sat Apr 17 12:13:37 +0000 2010",
;;        "text": "@marcosgdf abc",
;;        "source": "web",
;;        "truncated": false,
;;        "in_reply_to_status_id": "28",
;;        "in_reply_to_user_id": "1",
;;        "in_reply_to_screen_name": "marcosgdf",
;;        "geo": null,
;;        "favorited": false,
;;        "contributors": null,
;;        "coordinates": null,
;;        "user": {
;;            "id": 2,
;;            "name": "lorena",
;;            "screen_name": "lorena",
;;            "location": null,
;;            "description": "",
;;            "profile_image_url": "http:\/\/localhost\/trunk\/static\/img\/avatar\/default_note.png",
;;            "url": null,
;;            "protected": true,
;;            "followers_count": "0",
;;            "created_at": "Fri Apr 16 20:13:05 +0000 2010",
;;            "favourites_count": "1",
;;            "statuses_count": "7",
;;            "friends_count": "0",
;;            "following": false,
;;            "utc_offset": null,
;;            "time_zone": null,
;;            "profile_background_image_url": "http:\/\/localhost\/trunk\/themes\/transparency\/img\/bg.png",
;;            "profile_background_tile": false,
;;            "notifications": false,
;;            "verified": false,
;;            "lang": "default",
;;            "contributions_enabled": false,
;;            "geo_enabled": false
;;        }
;;    }
;;]
