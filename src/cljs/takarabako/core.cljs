(ns takarabako.core
  (:require [goog.dom :as gdom]
            [om.dom :as dom]
            [cljs.core.async :refer [<! >!]]
            [takarabako.components :refer [FinanceList]]
            [takarabako.parser :refer [read mutate]]
            [chord.client :refer [ws-ch]]
            [om.next :as om :refer-macros [defui]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def app-state
  (atom
   {:finances/collection []}))

(def reconciler
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read :mutate mutate})}))

(om/add-root!
 reconciler
 FinanceList
 (gdom/getElement "app"))
