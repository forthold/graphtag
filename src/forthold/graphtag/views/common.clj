(ns forthold.graphtag.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]]))

(defpartial layout [& content]
           (html5
              (include-js "/scripts/d3.v2.js")
              (include-js "/scripts/jquery-1.7.2.js")
              ;(include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js")
              (include-js "/scripts/bootstrap.min.js")
              (include-js "/scripts/user.js")
              (include-css "/css/graphtag.css")
              ;(include-css "/css/reset.css")
              [:head
               [:title "GraphTag"]
               (include-css "/css/bootstrap.css")
               (include-css "/css/bootstrap-responsive.css")
               [:style "body { padding-top: 60px; }"]]
              [:body
               (list
                [:div.navbar.navbar-fixed-top {"data-toggle" "collapse" "data-target" ".nav-collapse"}
                 [:div.navbar-inner
                  [:div.container
                   [:a {"href" "#", "class" "brand", "style" "padding-top:0px;padding-bottom:0px;"}
                     [:image {"src" "/images/graphtag-hi-grey-allwhite-50x50.png"}]
                    ]
                   [:a.btn.btn-navbar
                    [:span.icon-bar]]
                  [:a.brand "GraphTag"]
                   [:div.nav-collapse
                    [:ul.nav
                     [:li.active
                      [:a {"href" "#"} "Home"]]
                     [:li
                      [:a {"href" "http://www.webnoir.org/tutorials"} "Tutorials"]]
                     [:li
                      [:a {"href" "http://groups.google.com/group/clj-noir"} "Google Group"]]
                     [:li
                      [:a {"href" "http://www.webnoir.org/docs/"} "API"]]
                     [:li
                      [:a {"href" "https://github.com/ibdknox/noir"} "Source"]]]]]]]
                [:div.container content] 
                )]))
