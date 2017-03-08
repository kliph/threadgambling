(ns threadgambling.test-util
  (:require [cljs-react-material-ui.reagent :as rui]
            [cljs-react-material-ui.core :as ui]))

(defn test-container [component]
  [rui/mui-theme-provider
   {:mui-theme (ui/get-mui-theme)}
   component])
