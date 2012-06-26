(ns forthold.graphtag.views.welcome
  (:require [forthold.graphtag.views.common :as common]
            [forthold.graphtag.cypher :as dao] )
  (:use [noir.core :only [defpage]]
        [hiccup.element :only [javascript-tag]]
        [hiccup.core :only [html h]]))

(defn json-js [id]
    (str "var json= " (dao/get-user-data id) ";"))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to forthold.graphtag"]))


(defpage [:get ["/user/:id" :id #"\d+"]] {:keys [id]}
         (common/layout
           [:h1 "Welcome to GraphTag " id]
           (javascript-tag (json-js id))
           [:div {:id "chart"}]       
           ))
