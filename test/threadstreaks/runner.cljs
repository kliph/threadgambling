(ns threadstreaks.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [goog.object :as gobj]
            [threadstreaks.fixtures-test]
            [threadstreaks.update-account-test]))

(doo-all-tests #"(threadstreaks)\..*-test")
