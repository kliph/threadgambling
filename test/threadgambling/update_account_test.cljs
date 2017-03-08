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

(deftest change-email
  (testing "Changing email updates the state"
    (let [_ (r/render (test-util/test-container [account/update-page]) c)
          email-input (sel1 "input[name=\"Email\"]")]
      (is (= (dommy/value email-input) (-> @s/app-state
                                          :account
                                          :email)))
      (change-input! email-input "test")
      (is (= (dommy/value email-input) "test"))
      (is (= (-> @s/app-state
                 :account
                 :email) "test")))))

(deftest change-team-name
  (testing "Changing team name updates the state"
    (let [_ (r/render (test-util/test-container [account/update-page]) c)
          team-input (sel1 "input[name=\"Team Name\"]")]
      (is (= (dommy/value team-input) (-> @s/app-state
                                           :account
                                           :team)))
      (change-input! team-input "test")
      (is (= (dommy/value team-input) "test"))
      (is (= (-> @s/app-state
                 :account
                 :team) "test")))))
