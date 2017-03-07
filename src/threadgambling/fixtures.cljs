(ns threadgambling.fixtures
  (:require [reagent.core :as r]
            [threadgambling.state :as s]
            [cljs-react-material-ui.core :as ui]))

(def previously-picked?
  (get-in @s/app-state [:account :picked] #{}))

(defn toggle-picked! [a]
  (if (= @a "picked")
    (reset! a "pickable")
    (reset! a "picked")))

(defn reset-other-picked! [table-state name]
  (doall
   (map
    (fn [x]
      (compare-and-set! (val x) "picked" "pickable"))
    (filter #(not= (key %) name) table-state))))

(defn disable-other-pickable! [table-state]
  (doall
   (map
    (fn [x]
      (compare-and-set! (val x) "pickable" ""))
    table-state)))

(defn confirm-pick! [table-state]
  (let [pick (first (filter #(= @(val %) "picked") table-state))]
    (when pick
      (js/console.log (str "Picked " (key pick)))
      (disable-other-pickable! table-state)
      (reset! (val pick) "confirmed"))))

(defn pick-item [props table-state]
  (let [{:keys [name]} props
        td-state (get table-state name)]
    (fn []
      (if (#{"pickable" "picked"} @td-state)
        [:td {:class @td-state
              :on-click #(do
                           (js/console.log table-state)
                           (reset-other-picked! table-state name)
                           (toggle-picked! td-state))}
         name]
        [:td {:class @td-state} name]))))

(defn pick-row [props table-state]
  (let [{:keys [home-club away-club]} props]
    [:tr
     [pick-item home-club table-state]
     [:td "vs"]
     [pick-item away-club table-state]
     ]))

(defn fixtures-page []
  (let [sorted-fixtures (sort-by :date (@s/app-state :fixtures))
        table-keys (-> (into [] (map #(get-in % [:home-club :name]) sorted-fixtures))
                       (into (map #(get-in % [:away-club :name]) sorted-fixtures))
)
        table-vals (map #(if (previously-picked? %)
                           (r/atom "disabled")
                           (r/atom "pickable"))
                        table-keys)
        table-state (zipmap table-keys table-vals)]
    (fn []
      [:div#fixtures
       [:h2 "Fixtures"]
       [:table
        [:thead>tr
         [:th "Home"]
         [:th]
         [:th "Away"]]
        [:tbody
         (map (fn [x] ^{:key (str (:home-club x) (:away-club x))} [pick-row x table-state])
              sorted-fixtures)]]
       [ui/raised-button
        {:label "Confirm"
         :full-width true
         :on-click #(confirm-pick! table-state)
         :className "pick-button"}]])))
