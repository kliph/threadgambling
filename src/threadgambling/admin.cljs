(ns threadgambling.admin
  (:require [reagent.core :as r]
            [goog.dom]))

(defn notice [message-atom]
  [:p @message-atom])

(defn admin-container []
  (let [message (r/atom "")]
    [:div
     [:a {:on-click #(reset! message "Scored the picks for this week and pulled next week's games.")} "Score week and start the next week"]
     [notice message]]))

(r/render-component [admin-container] (goog.dom.getElement "admin"))
