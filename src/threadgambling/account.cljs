(ns threadgambling.account
  (:require [threadgambling.state :as s]
            [cljs-react-material-ui.core :as ui]))

(defn atom-input [props]
  (let [{:keys [value-ks label]} props]
    [:label label
     [:input {:type "text"
              :value (get-in @s/app-state value-ks "")
              :on-change #(swap! s/app-state
                                 assoc-in
                                 value-ks
                                 (-> %
                                     .-target
                                     .-value))}]]))

(defn update-page []
  [:div#update-container
   [atom-input {:label "Name"
                :value-ks [:account :name]}]
   [atom-input {:label "Email"
                :value-ks [:account :email]}]
   [atom-input {:label "Team Name"
                :value-ks [:account :team]}]
   [ui/raised-button
    {:label "Save changes"
     :full-width true
     :className "update-button"}]])
