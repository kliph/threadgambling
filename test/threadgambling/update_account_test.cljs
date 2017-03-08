(ns threadgambling.update-account-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljs-react-test.utils :as tu]
            [dommy.core :as dommy :refer-macros [sel1 sel]]
            [reagent.core :as r]
            [threadgambling.account :as account]
            [threadgambling.test-util :as test-util]
            [threadgambling.state :as s]))

(def ^:dynamic c)

(use-fixtures :each (fn [test-fn]
                      (with-redefs
                        [s/app-state
                         (r/atom {:account {:name "Colin Smith"
                                            :team "South Philly Kittens"
                                            :email "foo@example.com"}})]

                        (binding [c (tu/new-container!)]
                          (test-fn)
                          (tu/unmount! c)))))

(defn change-input! [input text]
  (dommy/set-value! input text)
  (let [e (js/Event. "input" #js {:bubbles true})]
    (.dispatchEvent input e))
  (r/flush))

(deftest change-name
  (testing "Changing name updates the state"
    (let [_ (r/render (test-util/test-container [account/update-page]) c)
          name-input (sel1 "input[name=\"Name\"]")]
      (is (= (dommy/value name-input) (-> @s/app-state
                                          :account
                                          :name)))
      (change-input! name-input "test")
      (is (= (dommy/value name-input) "test"))
      (is (= (-> @s/app-state
                 :account
                 :name) "test")))))

(deftest change-email)

(deftest change-team-name)
