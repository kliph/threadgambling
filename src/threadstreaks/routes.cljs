(ns ^:figwheel-no-load threadstreaks.routes
  (:require [secretary.core :as secretary :refer [defroute]]
            [threadstreaks.fixtures :as f]
            [threadstreaks.standings :as standings]
            [threadstreaks.team :as team]
            [threadstreaks.home :as home]
            [threadstreaks.account :as account]
            [threadstreaks.auth :as auth]
            [threadstreaks.state :as s]
            [goog.events :as events]
            [reagent.core :as r]
            [goog.history.EventType :as EventType])
  (:import goog.History))


(defmulti current-page #(@s/app-state :page))
(defmethod current-page :fixtures []
  [f/fixtures-page])
(defmethod current-page :standings []
  [standings/standings-page])
(defmethod current-page :team []
  [team/team-page])
(defmethod current-page :home []
  [home/home-page])
(defmethod current-page :update-account []
  [account/update-page])
(defmethod current-page :sign-in []
  [auth/sign-in-page])
(defmethod current-page :sign-out []
  [auth/sign-in-page])

(secretary/set-config! :prefix "#")

;;; TODO write a defroute-auth fn / macro

(defroute home-path "/" []
  (if (:signed-in @s/app-state)
    (swap! s/app-state assoc :page :home)
    (swap! s/app-state assoc :page :sign-in)))

(defroute fixtures-path "/fixtures" []
  (if (:signed-in @s/app-state)
    (swap! s/app-state assoc :page :fixtures)
    (swap! s/app-state assoc :page :sign-in)))

(defroute standings-path "/standings" []
  (if (:signed-in @s/app-state)
    (swap! s/app-state assoc :page :standings)
    (swap! s/app-state assoc :page :sign-in)))

(defroute update-account-path "/update-account" []
  (if (:signed-in @s/app-state)
    (swap! s/app-state assoc :page :update-account)
    (swap! s/app-state assoc :page :sign-in)))

(defroute team-path "/team/:team-user-id" [team-user-id]
  (if (:signed-in @s/app-state)
    (swap! s/app-state assoc :page :team :team-user-id team-user-id)
    (swap! s/app-state assoc :page :sign-in)))

(defroute sign-in-path "/sign-in" []
  (swap! s/app-state assoc :page :home))

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))
