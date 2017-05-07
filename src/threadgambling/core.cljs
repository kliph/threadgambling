(ns threadgambling.core
  (:require [cljs-react-material-ui.core :as ui]
            [cljs-react-material-ui.icons :as ic]
            [cljs-react-material-ui.reagent :as rui]
            [reagent.core :as r]
            [threadgambling.routes :as routes]
            [threadgambling.state :as s]
            [threadgambling.color :as c]
            [goog.dom]))

(def by-id goog.dom.getElement)

(defn small-footer-nav []
  (if (:signed-in @s/app-state)
    [:div#small-footer-nav
     [:ul
      [:li
       [ui/flat-button
        {:href "/#/fixtures"
         :label "Fixtures"}]]
      [:li
       [ui/flat-button
        {:href "/#/standings"
         :label "Standings"}]]
      [:li
       [ui/flat-button
        {:href "/#/update-account"
         :label "Update Account"}]]
      [:li
       [ui/flat-button
        {:href "/#/sign-out"
         :label "Sign Out"}]]]]))

(defn footer []
  [:div#footer
   [:ul
    [:li
     [ui/flat-button
      {:style {:color c/white}
       :href "https://soundcloud.com/user-70053674"
       :label "Podcast"}]]
    [:li
     [ui/flat-button
      {:style {:color c/white}
       :href "https://twitter.com/soccerthread"
       :label "Twitter"}]]
    [:li
     [ui/flat-button
      {:style {:color c/white}
       :href "https://www.facebook.com/soccerthread/"
       :label "Facebook"}]]]])

(defn nav-links []
  [:div#nav-links
   [:span
    [:a {:href "/#/"} "threadgambling"]]
   (if (:signed-in @s/app-state)
     [:span.pull-right
      [ui/flat-button
       {:href "/#/fixtures"
        :style {:color c/white}
        :label "Fixtures"}]
      [ui/flat-button
       {:href "/#/standings"
        :style {:color c/white}
        :label "Standings"}]
      [ui/flat-button
       {:href "/#/sign-out"
        :style {:color c/white}
        :label "Sign Out"}]])])

(defn app-container []
  [rui/mui-theme-provider
   {:mui-theme (ui/get-mui-theme)}
   [:div
    [rui/app-bar {:style {:background-color c/purp}
                  :show-menu-icon-button false
                  :title (r/as-element (nav-links))}]
    [:div.app-container
     {}
     [:div.app-container-inner
      {}
      [routes/current-page]]]
    [small-footer-nav]
    [footer]]])

(r/render-component [app-container] (by-id "app"))
