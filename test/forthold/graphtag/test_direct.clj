(ns forthold.graphtag.test-direct
  (:require [clojurewerkz.neocons.rest               :as neorest]
            [clojurewerkz.neocons.rest.nodes         :as nodes]
            [clojurewerkz.neocons.rest.relationships :as relationships])
  (:use  [clojure.test]
         [clojure.pprint]
         [twitter.oauth]
         [twitter.callbacks]
         [twitter.callbacks.handlers]
         [twitter.api.restful]
         [forthold.graphtag.test-common]
         [forthold.graphtag.direct]
         [forthold.graphtag.common]
         [forthold.graphtag.followers])
  (:import [twitter.callbacks.protocols SyncSingleCallback]))

;; Set up test indexs and kill nodes after
(defn fixture [f]
  (neorest/connect! "http://localhost:7474/db/data/")
  (set-up-neo4j-indexes)
  (f))

(use-fixtures :once fixture)

(deftest test-get-messages
  (let [ result (direct-messages :oauth-creds *creds*
                       :callbacks (SyncSingleCallback. response-return-body response-throw-error exception-rethrow))] 
        ;(dbg result)
        ;(println result)
        ;(doseq [mention (seq result)] (mention-handler mention))
        (is (not (empty? result)))
        (dbg (:text(first result)))
        (dbg (:text (nth result 0)))
        (doseq [dm (seq result)] (println (:text dm)))
    ))

(deftest test-process-tags-with-no-dsl
    (let [msg-node (create-new-message message)]
      (dbg msg-node))
  )

(deftest test-process-tags-with-dsl
    (let [msg-node (create-new-message message-with-dsl)]
      (dbg msg-node))
  )

;;[{:text Hey heres a little test DM, :id_str 200865282179334144, :sender {:profile_use_background_image true, :follow_request_sent false, :default_profile false, :profile_sidebar_fill_color efefef, :protected false, :following false, :profile_background_image_url http://a0.twimg.com/profile_background_images/299595203/Internet_traffic.png, :default_profile_image false, :contributors_enabled false, :favourites_count 3, :time_zone Hawaii, :name Aran Elkington, :id_str 307693455, :listed_count 0, :utc_offset -36000, :profile_link_color 009999, :profile_background_tile true, :location Brisbane, Australia, :statuses_count 72, :followers_count 35, :friends_count 81, :created_at Mon May 30 03:43:04 +0000 2011, :lang en, :profile_sidebar_border_color eeeeee, :url http://forthold.com, :notifications false, :profile_background_color 131516, :geo_enabled false, :show_all_inline_media false, :profile_image_url_https https://si0.twimg.com/profile_images/1396442108/avatar_normal.png, :is_translator false, :profile_image_url http://a0.twimg.com/profile_images/1396442108/avatar_normal.png, :verified false, :id 307693455, :profile_background_image_url_https https://si0.twimg.com/profile_background_images/299595203/Internet_traffic.png, :description Programmer .Net, Java, Clojure. Fan of Neo4j, Redis, vim, Proxmox, Archlinux, Xmonad, Functional Stuff and all things NoSql. Husband of 1 and father of 2., :profile_text_color 333333, :screen_name forthold}, :recipient_screen_name GraphTag, :sender_id 307693455, :recipient_id 491183182, :created_at Fri May 11 08:29:49 +0000 2012, :sender_screen_name forthold, :id 200865282179334144, :recipient {:profile_use_background_image true, :follow_request_sent false, :default_profile false, :profile_sidebar_fill_color DDEEF6, :protected false, :following true, :profile_background_image_url http://a0.twimg.com/profile_background_images/443095935/teidesky_casado_annotated_3000jpg60.jpg, :default_profile_image false, :contributors_enabled false, :favourites_count 0, :time_zone nil, :name Graph Tag, :id_str 491183182, :listed_count 0, :utc_offset nil, :profile_link_color 0084B4, :profile_background_tile false, :location , :statuses_count 0, :followers_count 3, :friends_count 8, :created_at Mon Feb 13 10:38:15 +0000 2012, :lang en, :profile_sidebar_border_color C0DEED, :url http://graphtag.herokuapps.com, :notifications false, :profile_background_color 403728, :geo_enabled false, :show_all_inline_media false, :profile_image_url_https https://si0.twimg.com/profile_images/1878371608/graphtag-hi_normal.png, :is_translator false, :profile_image_url http://a0.twimg.com/profile_images/1878371608/graphtag-hi_normal.png, :verified false, :id 491183182, :profile_background_image_url_https https://si0.twimg.com/profile_background_images/443095935/teidesky_casado_annotated_3000jpg60.jpg, :description , :profile_text_color 333333, :screen_name GraphTag}}]i
