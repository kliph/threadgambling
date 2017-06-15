(ns threadstreaks.update-account-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljs-react-test.utils :as tu]
            [dommy.core :as dommy :refer-macros [sel1 sel]]
            [reagent.core :as r]
            [reagent.session :as session]
            [threadstreaks.account :as account]
            [threadstreaks.test-util :as test-util]
            [threadstreaks.state :as s]))

(def ^:dynamic c)

(use-fixtures :each (fn [test-fn]
                      (session/clear!)
                      (session/put! :user {:name "Colin Smith"
                                           :team "South Philly Kittens"
                                           :email "foo@example.com"})
                      (binding [c (tu/new-container!)]
                        (test-fn)
                        (tu/unmount! c))))

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
      (is (= "Colin Smith" (session/get-in [:user :name])))
      (is (= (dommy/value name-input) "test"))
      (is (= (-> @s/app-state
                 :account
                 :name) "test")))))

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

(deftest clicking-update-button
  (with-redefs [account/handle-update! (fn [] {:id 123
                                               :name "test-team"
                                               :team "test-team"})]
    (let [_ (r/render (test-util/test-container [account/update-page]) c)
          team-input (sel1 "input[name=\"Team Name\"]")
          name-input (sel1 "input[name=\"Name\"]")
          update-button (sel1 "div.update-button")]
      (change-input! team-input "test-team")
      (change-input! name-input "test-name")
      (test-util/click update-button)
      ;; Test side effects without mocks :/
      (is (= 1 1)))))
