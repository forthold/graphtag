(ns forthold.graphtag.views.welcome
  (:require [forthold.graphtag.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to forthold.graphtag"]))
