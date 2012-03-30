(ns forthold.graphtag.test-common) 

(def mention {:user {:screen_name "graphtagtest",
                     :id "1234567890",
                     :name "test name",
                     :description "test",
                     :profile_image_url "profile image",
                     :profile_background_image_url "bg image"}})

(def userid (:id (:user mention)))

(def username (:screen_name (:user mention)))
