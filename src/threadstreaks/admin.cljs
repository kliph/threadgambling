(ns threadstreaks.admin
  (:require [reagent.core :as r]
            [goog.dom]))

(defn notice [message-atom]
  [:p @message-atom])

(defn admin-container []
  (let [message (r/atom "")]
    [:div
     [:p [:a {:on-click #(reset! message "Scored the picks for this week and pulled next week's games.")} "Score week and start the next week"]]
     [:p [:a {:on-click #(reset! message "Reset the picks.")} "Reset picks"]]
     [:p [:a {:on-click #(reset! message "Refetched the games.")} "Refetch games"]]
     [notice message]]))

(r/render-component [admin-container] (goog.dom.getElement "admin"))
