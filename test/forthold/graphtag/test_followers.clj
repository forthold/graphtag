(ns forthold.graphtag.test-followers
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships])
  (:use [clojure.test]
        [forthold.graphtag.test-common]
        [forthold.graphtag.followers]
        [forthold.graphtag.common]))

(def ^:const following-user-id 507201238)

;; Set up test indexs and kill nodes after
(defn follower-fixture [f]
  (neorest/connect! "http://localhost:7474/db/data/")
  (set-up-neo4j-indexes)
  (f))

(use-fixtures :once follower-fixture)

(deftest test-new-follower
  (is (false? (user-id-exists following-user-id)))
  (let [ follower-node (follower-handler following-user-id)]
    (is (user-id-exists following-user-id))
    (let [follower-node2 (follower-handler following-user-id)
           rels (relationships/all-for follower-node2) ]
        (is (= (:id follower-node) (:id follower-node2)))
        ;test rel exists to root
        (is rels)
        (is (= 1 (count rels)))
        (delete-user-by-node-id (:id follower-node))
        (is (false? (user-id-exists following-user-id)))
        ;; run follower handler again and ensure another user is not created 
    )))

(deftest test-follower-exists 
    (is (false? (follower-id-exists following-user-id)))
    (let [user (follower-handler following-user-id)
          ]
      (is user)
      (is (follower-id-exists following-user-id))
      (delete-user-by-node-id (:id user))
      (is (false? (user-id-exists following-user-id)))
      ))


;; Example Request
;; GET  
;; https://api.twitter.com/1/followers/ids.json?cursor=-1&screen_name=twitterapi
;;{
;;   "previous_cursor": 0,
;;   "ids": [
;;               143206502,
;;               143201767,
;;               777925
;;             ],
;;   "previous_cursor_str": "0",
;;   "next_cursor": 0,
;;   "next_cursor_str": "0"
;; }
;;
;;
;;
;;
;;Example Request
;;GET 
;;https://api.twitter.com/1/users/show.json?screen_name=TwitterAPI&include_entities=true
;;
;;{
;;   "profile_sidebar_fill_color": "e0ff92",
;;   "name": "Twitter API",
;;   "profile_sidebar_border_color": "87bc44",
;;   "profile_background_tile": false,
;;   "created_at": "Wed May 23 06:01:13 +0000 2007",
;;   "profile_image_url": "http://a3.twimg.com/profile_images/689684365/api_normal.png",
;;   "location": "San Francisco, CA",
;;   "follow_request_sent": false,
;;   "id_str": "6253282",
;;   "profile_link_color": "0000ff",
;;   "is_translator": false,
;;   "contributors_enabled": true,
;;   "url": "http://dev.twitter.com",
;;   "favourites_count": 15,
;;   "utc_offset": -28800,
;;   "id": 6253282,
;;   "profile_use_background_image": true,
;;   "listed_count": 6868,
;;   "profile_text_color": "000000",
;;   "protected": false,
;;   "followers_count": 335343,
;;   "lang": "en",
;;   "notifications": false,
;;   "geo_enabled": true,
;;   "profile_background_color": "c1dfee",
;;   "verified": true,
;;   "description": "The Real Twitter API. I tweet about API changes, service issues and happily answer questions about Twitter and our API. Don't get an answer? It's on my website.",
;;   "time_zone": "Pacific Time (US & Canada)",
;;   "profile_background_image_url": "http://a3.twimg.com/profile_background_images/59931895/twitterapi-background-new.png",
;;   "friends_count": 20,
;;   "statuses_count": 2404,
;;   "status": {
;;                  "coordinates": null,
;;                  "created_at": "Wed Dec 22 20:08:02 +0000 2010",
;;                  "favorited": false,
;;                  "truncated": false,
;;                  "id_str": "17672734540570624",
;;                  "entities": {
;;                                     "urls": [
;;                                                      {
;;                                                                 "expanded_url": "http://tumblr.com/xnr140f9mi",
;;                                                                 "url": "http://t.co/37zl2jI",
;;                                                                 "indices": [
;;                                                                                         93,
;;                                                                                         112
;;                                                                                       ],
;;                                                                 "display_url": "tumblr.com/xnr140f9mi"
;;                                                               }
;;                                                    ],
;;                                     "hashtags": [
;;                                                   
;;                                                        ],
;;                                     "user_mentions": [
;;                                                        
;;                                                             ]
;;                                   },
;;                  "in_reply_to_user_id_str": null,
;;                  "contributors": null,
;;                  "text": "Twitter downtime - Twitter is currently down. We are aware of the problem and working on it. http://t.co/37zl2jI",
;;                  "id": 17672734540570624,
;;                  "retweet_count": 30,
;;                  "in_reply_to_status_id_str": null,
;;                  "geo": null,
;;                  "retweeted": false,
;;                  "in_reply_to_user_id": null,
;;                  "source": "<a href=\"http://www.tumblr.com/\" rel=\"nofollow\">Tumblr</a>",
;;                  "in_reply_to_screen_name": null,
;;                  "place": null,
;;                  "in_reply_to_status_id": null
;;                },
;;   "following": true,
;;   "screen_name": "twitterapi",
;;   "show_all_inline_media": false
;; }
