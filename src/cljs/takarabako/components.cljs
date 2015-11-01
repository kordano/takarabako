(ns takarabako.components
  (:require [goog.dom :as gdom]
            [om.dom :as dom]
            [takarabako.io :as io]
            [om.next :as om :refer-macros [defui]]))

(defn create-table [collection]
  (dom/table nil
             (dom/thead
              nil
              (dom/tr nil
                      (dom/th nil "Date")
                      (dom/th nil "Company")
                      (dom/th nil "Value")))
             (apply dom/tbody
                    nil
                    (map
                     (fn [{:keys [company date value]}]
                       (dom/tr nil
                               (dom/td nil date)
                               (dom/td nil company)
                               (dom/td nil value)))
                     collection))))

(defn create-input [component]
  (let [{:keys [company date value] :as local} (om/get-state component)]
    (dom/div nil
             (dom/input #js {:type :text
                             :placeholder "Company"
                             :value company
                             :onChange
                             (fn [e]
                               (om/set-state!
                                component
                                (assoc local :company
                                       (.. e -target -value))))})
             (dom/input #js {:type :date
                             :placeholder "Date"
                             :value date
                             :onChange
                             (fn [e]
                               (om/set-state!
                                component
                                (assoc local :date
                                       (.. e -target -value))))})
             (dom/input #js {:type :number
                             :placeholder "Value"
                             :value value
                             :onChange
                             (fn [e]
                               (om/set-state!
                                component
                                (assoc local :value
                                       (.. e -target -value))))})
             (dom/button #js {:onClick
                              (fn [e]
                                (let [new-value (-> local
                                                    (assoc :type :outcome)
                                                    (update :value js/parseFloat))]
                                  (om/transact! component `[(finances/add ~new-value)])
                                  (om/set-state! component {:company "" :date "" :value ""})))}
                         "Add"))))

(defui FinanceList
  static om/IQueryParams
  (params [this]
    {:type :outcome})
  static om/IQuery
  (query [this]
    '[(:finances/net nil) (:finances/collection {:type ?type}) ])
  Object
  (componentDidMount [this] (io/open-channel this))
  (render [this]
    (let [{:keys [finances/collection finances/net] :as props} (om/props this)]
      (dom/div nil
               (create-input this)
               (dom/h1 nil "Net value: " net)
               (create-table collection)))))

