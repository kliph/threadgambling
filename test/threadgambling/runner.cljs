(ns threadgambling.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [goog.object :as gobj]
            [threadgambling.fixtures-test]
            [threadgambling.update-account-test]))

(doo-all-tests #"(threadgambling)\..*-test")
