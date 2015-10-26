(ns takarabako.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as sec :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as sec :refer-macros [defroute]]
            [cljs.core.async :refer [<! >!]]
            [kioo.om :refer [content set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!)

(println "もしもし")

(om/root
 #(om/component
   (dom/h1 nil "Welcome!"))
 (atom {})
 {:target (.getElementById js/document "app")})
