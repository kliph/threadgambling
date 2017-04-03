(ns threadgambling.fixtures-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljs-react-test.utils :as tu]
            [clojure.string]
            [dommy.core :as dommy :refer-macros [sel1 sel]]
            [reagent.core :as r]
            [threadgambling.test-util :as test-util]
            [threadgambling.fixtures :as fixtures]
            [threadgambling.state :as s]))

(def ^:dynamic c)

(use-fixtures :each (fn [test-fn]
                      (with-redefs [s/app-state
                                    (r/atom {:fixtures (r/atom {:fetched true
                                                                :fixtures [{:home-club {:name "Tottenham"
                                                                                        :goals 1}
                                                                            :away-club {:name "Everton"
                                                                                        :goals 1}
                                                                            :date "2017-Jan-1"}
                                                                           {:home-club {:name "Morecambe"
                                                                                        :goals nil}
                                                                            :away-club {:name "Handsome Pigeons"
                                                                                        :goals nil}
                                                                            :date "2017-Jan-1"}
                                                                           {:home-club {:name "Leicester"
                                                                                        :goals 1}
                                                                            :away-club {:name "Man City"
                                                                                        :goals 0}
                                                                            :date "2017-Jan-1"}]})
                                             :account {:picked #{"Tottenham" "Leicester"}}})
                                    fixtures/fetch-fixtures! (fn [_])]
                        (binding [c (tu/new-container!)]
                          (test-fn)
                          (tu/unmount! c)))))

(defn click [node]
  (.click node)
  (r/flush))

(deftest picking-pickable-test
  (testing "Clicking pickable toggles to picked and back"
    (let [_ (r/render (test-util/test-container [fixtures/fixtures-page]) c)
          pickable (sel1 c [:.pickable])]
      (is (= 0 (count (sel c [:.picked]))))
      (is (clojure.string/includes? (dommy/text pickable) "Everton"))
      (click pickable)
      (is (= "picked" (dommy/class pickable)))
      (is (= 1 (count (sel c [:.picked]))))
      (click pickable)
      (is (= "pickable" (dommy/class pickable))))))

(deftest picking-other-pickable
  (testing "Clicking other pickable when one is picked makes that one picked and the other pickable"
    (let [_ (r/render (test-util/test-container [fixtures/fixtures-page]) c)
          pickables (sel c [:.pickable])
          pickable (first pickables)
          other-pickable (second pickables)]
      (is (= 0 (count (sel c [:.picked]))))
      (click pickable)
      (is (= "picked" (dommy/class pickable)))
      (click other-pickable)
      (is (= "picked" (dommy/class other-pickable)))
      (is (= "pickable" (dommy/class pickable)))
      (is (= 1 (count (sel c [:.picked])))))))

(deftest clicking-disabled
  (testing "Clicking disabled does nothing"
    (let [_ (r/render (test-util/test-container [fixtures/fixtures-page]) c)
          disabled (sel1 c [:.disabled])]
      (click disabled)
      (is (= 2 (count (sel c [:.disabled]))))
      (is (= "disabled" (dommy/class disabled)))
      (is (= 0 (count (sel c [:.picked])))))))

(deftest confirm-button
  (testing "Confirm is disabled when nothing is selected"
    (let [table-state (r/atom {"Tottenham" (r/atom "disabled")
                               "Morecambe" (r/atom "pickable")
                               "Leciester" (r/atom "disabled")
                               "Everton" (r/atom "pickable")})
          confirm-disabled (r/atom true)
          _ (r/render (test-util/test-container [fixtures/table-and-confirm-button
                                                 table-state
                                                 {}
                                                 confirm-disabled]) c)
          button (sel1 c [:.pick-button :button])]
      (is (= 0 (count (sel c [:.confirmed]))))
      ;; "" is true
      ;; nil is false
      (is (= "" (dommy/attr button :disabled)))))
  (testing "Clicking confirm with something picked confirms it"
    (let [table-state (r/atom {"Tottenham" (r/atom "disabled")
                               "Morecambe" (r/atom "pickable")
                               "Leciester" (r/atom "disabled")
                               "Everton" (r/atom "picked")})
          confirm-disabled (r/atom false)
          _ (r/render (test-util/test-container [fixtures/table-and-confirm-button
                                                 table-state
                                                 {}
                                                 confirm-disabled]) c)
          button (sel1 c [:.pick-button])]
      (is (= 0 (count (sel c [:.confirmed]))))
      (click button)
      (is (= "confirmed"
              @(get @table-state "Everton"))))))

(deftest results-display
  (testing "Handles null"
    (let [_ (r/render (test-util/test-container [fixtures/fixtures-page]) c)
          goals (sel c [:.goals])]
      (println (dommy/text goals))
      (is (= 4 (count goals))))))
