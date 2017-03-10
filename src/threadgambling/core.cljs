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
    [:div
     {:style {:padding-top "1em"
              :padding-bottom "3em"
              :box-shadow "0 1px 6px rgba(0,0,0,0.12), 0 1px 4px rgba(0,0,0,0.12)"
              :position "relative"
              :z-index 1000}}
     [:div {:style {:margin "1em"}}
      [routes/current-page]]]
    [footer]]])

(r/render-component [app-container] (by-id "app"))
