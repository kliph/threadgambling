(ns threadgambling.fixtures-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljs-react-test.utils :as tu]
            [dommy.core :as dommy :refer-macros [sel1 sel]]
            [reagent.core :as r]
            [threadgambling.test-util :as test-util]
            [threadgambling.fixtures :as fixtures]
            [threadgambling.state :as s]))

(def ^:dynamic c)

(use-fixtures :each (fn [test-fn]
                      (with-redefs [s/app-state
                                    (r/atom {:fixtures (r/atom {:fetched true
                                                                :fixtures [{:home-club {:name "Tottenham"}
                                                                            :away-club {:name "Everton"}
                                                                            :date "2017-Jan-1"}
                                                                           {:home-club {:name "Morecambe"}
                                                                            :away-club {:name "Handsome Pigeons"}
                                                                            :date "2017-Jan-1"}
                                                                           {:home-club {:name "Leicester"}
                                                                            :away-club {:name "Man City"}
                                                                            :date "2017-Jan-1"}]})
                                             :account {:picked #{"Tottenham" "Leicester"}}})
                                    fixtures/fetch-fixtures! (fn [_])]
                        (binding [c (tu/new-container!)]
                          (test-fn)
                          (tu/unmount! c)))))

(defn click [node]
  (.click node)
  (r/flush))

(deftest displays-fixtures-when-delay-fetching)

(deftest picking-pickable-test
  (testing "Clicking pickable toggles to picked and back"
    (let [_ (r/render (test-util/test-container [fixtures/fixtures-page]) c)
          pickable (sel1 c [:.pickable])]
      (is (= 0 (count (sel c [:.picked]))))
      (is (= "Everton" (dommy/text pickable)))
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
  (testing "Clicking confirm does nothing"
    (let [_ (r/render (test-util/test-container [fixtures/fixtures-page]) c)
          button (sel1 c [:.pick-button :button])]
      (is (= 0 (count (sel c [:.confirmed]))))
      (click button)
      (is (= 0 (count (sel c [:.confirmed]))))))
  (testing "Clicking confirm with something picked confirms it"
    (let [_ (r/render (test-util/test-container [fixtures/fixtures-page]) c)
          pickable (sel1 c [:.pickable])
          button (sel1 c [:.pick-button :button])]
      (is (= 0 (count (sel c [:.confirmed]))))
      (click pickable)
      (click button)
      (is (= 1 (count (sel c [:.confirmed]))))
      (is (= (dommy/text pickable)
             (dommy/text (sel1 c [:.confirmed])))))))
