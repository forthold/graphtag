(ns noirtest.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "noirtest"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                content]]))

(defpartial graphtag-layout [& content]
            (html5
              [:head
               [:title "GraphTag: Neo to Twit"]]
              [:body
               [:div#wrapper
                content]]))
