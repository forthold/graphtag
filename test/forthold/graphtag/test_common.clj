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


(def message {:id 200865282179334144,
              :text "Hey heres a little test DMidf" , 
              :sender {:name "Aran Elkington", :id 307693455 ,:screen_name "forthold"},
              :recipient { :name "Graph Tag",:id 491183182 ,:screen_name "GraphTag"}}
)

(def message-with-dsl {:id 200865282179334144,
              :text "Hey heres a little test DMidf @GraphTag (maths science port)", 
              :sender {:name "Aran Elkington", :id 307693455 ,:screen_name "forthold"},
              :recipient { :name "Graph Tag",:id 491183182 ,:screen_name "GraphTag"}}
)
;(def graphtag-id 491183182)
(def graphtagtest-id 507201238)
(def forthold-id 307693455)
