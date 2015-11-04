(ns takarabako.components
  (:require [goog.dom :as gdom]
            [om.dom :as dom]
            [sablono.core :as html :refer-macros [html]]
            [takarabako.io :as io]
            [om.next :as om :refer-macros [defui]]))

(defn create-input-field
  "Create accout input field"
  [component param value]
  (let [title (name param)
        local (om/get-state component)]
    [:input {:placeholder title 
             :value value
             :onChange
             (fn [e]
               (om/set-state!
                component
                (assoc local param
                       (.. e -target -value))))}]))

(defn create-booking-input
  "Create account input fields and add button"
  [component]
  (let [{:keys [category date value] :as local} (om/get-state component)
        {:keys [type]} (om/get-params component)]
    (html
     [:div#booking-input
      (create-input-field component :category category)
      (create-input-field component :date date)
      (create-input-field component :value value)
      [:button {:onClick
                (fn [e]
                  (let [new-value (-> local
                                      (assoc :type type)
                                      (update :value js/parseFloat))]
                    (om/transact! component `[(finances/add ~new-value)])
                    (om/set-state! component {:category "" :date "" :value ""})))}
       "Add"]])))


(defui Booking
  static om/IQuery
  (query [this]
    [:category :date :value])
  Object
  (render [this]
    (let [{:keys [category date value]} (om/props this)]
      (html
       [:tr.booking [:td category] [:td.table-number date]  [:td.table-number value]]))))

(def booking (om/factory Booking))

(defui BookingList
  Object
  (render [this]
    (let [{:keys [finances/selected] :as props} (om/props this)]
      (html
       [:table
        [:thead
         [:tr [:th "Category"] [:th "Date"]  [:th "Value"]]]
        [:tbody (map booking selected)]]))))

(def booking-list (om/factory BookingList))

(defui NetValue
  Object
  (render [this]
    (let [{:keys [finances/net] :as props} (om/props this)]
      (html [:h2 (str "Net Value: " net)]))))

(def netvalue (om/factory NetValue))

(defn create-selector [component param]
  (let [title (name param)]
    [:div.selector-toggle
     [:input
      {:type "radio"
       :id (str title "-selector")
       :name :booking-selector
       :value name
       :onClick #(let [{:keys [type]} (om/get-params component)]
                   (om/set-params! component {:type param}))}]
     [:label {:for (str title "-selector")} title]]))

(defui Dashboard
  static om/IQueryParams
  (params [this]
    {:type :outcome})
  static om/IQuery
  (query [this]
    `[(:finances/selected {:type ?type}) :finances/net])
  Object
  (componentDidMount [this] (io/open-channel this))
  (render [this]
    (println (-> this om/props))
    (html
     [:div
      (create-booking-input this)
      [:div.container
       [:div#netvalue (netvalue (om/props this))]
       [:div#booking-list
        [:div.header
         [:h2.header-title (-> this om/get-params :type name) ]
         [:div#selectors (map (partial create-selector this) [:income :outcome])]]
        [:div.booking-table
         (booking-list (om/props this))]]]])))
