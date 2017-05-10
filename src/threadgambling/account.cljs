(ns threadgambling.account
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [threadgambling.state :as s]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.session :as session]
            [threadgambling.color :as c]
            [cljs-react-material-ui.core :as ui]))

(defn atom-input [props]
  (let [{:keys [value-ks label disabled]} props]
    [ui/text-field {:type "text"
                    :underline-focus-style {:border-color c/purp}
                    :floating-label-focus-style {:color c/purp}
                    :floating-label-text label
                    :name label
                    :disabled disabled
                    :value (get-in @s/app-state value-ks "")
                    :on-change #(swap! s/app-state
                                       assoc-in
                                       value-ks
                                       (-> %
                                           .-target
                                           .-value))}]))
(defn post-update!
  [user-params]
  (go (let [response  (<! (http/post "/account"
                                     {:headers {"Accept" "application/json"}
                                      :form-params user-params}))])))


(defn handle-update! []
  (let [id (-> (session/get :user)
               :id)
        name (-> @s/app-state
                 :account
                 :name)
        team (-> @s/app-state
                 :account
                 :team)
        user-params {:id id
                     :name name
                     :team team}]
    (post-update! user-params)))

(defn update-page []
  (fn []
    (swap! s/app-state assoc :account (session/get :user))
    [:div#update-container
     [atom-input {:label "Name"
                  :value-ks [:account :name]}]
     [atom-input {:label "Email"
                  :value-ks [:account :email]
                  :disabled true}]
     [atom-input {:label "Team Name"
                  :value-ks [:account :team]}]
     [:div.spacer]
     [ui/raised-button
      {:label "Save changes"
       :href "/#/"
       :on-click handle-update!
       :full-width true
       :className "update-button"}]]))
