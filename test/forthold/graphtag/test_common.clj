(ns forthold.graphtag.test-common) 

(def mention { :text "oy @graphtag come on get it working", 
              :id "1234",
              :user {:screen_name "graphtagtest",
                     :id "1234567890",
                     :name "test name",
                     :description "test",
                     :profile_image_url "profile image",
                     :profile_background_image_url "bg image"}})

(def userid (:id (:user mention)))
(def mentionid (:id mention))
(def username (:screen_name (:user mention)))
(def user(:user mention))
