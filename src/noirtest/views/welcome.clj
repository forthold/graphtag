(ns noirtest.views.welcome
  (:require [noirtest.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to noirtest"]))
(defpage "/graphtag" []
    (common/graphtag-layout
           [:p "Graphtagist Welcome! to GraphTag!"]))
