(ns takarabako.core
  (:require [konserve.memory :refer [new-mem-store]]
            [replikativ.peer :refer [client-peer]]
            [replikativ.stage :refer [create-stage! connect! subscribe-crdts!]]
            [hasch.core :refer [uuid]]
            [replikativ.crdt.ormap.realize :refer [stream-into-identity!]]
            [replikativ.crdt.ormap.stage :as s]
            [cljs.core.async :refer [>! chan timeout]]
            [superv.async :refer [S] :as sasync]
            [om.next :as om :refer-macros [defui] :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]])
  (:require-macros [superv.async :refer [go-try <? go-loop-try]]
                   [cljs.core.async.macros :refer [go-loop]]))


(def user "mail:alice@replikativ.io")
(def ormap-id #uuid "7d274663-9396-4247-910b-409ae35fe98d")
(def uri "ws://127.0.0.1:31777")


(enable-console-print!)


                                        ; HELPERS

(defn pprint [s & args]
  (.log js/console (apply pr-str s args)))


(defn target-val [e]
  (.. e -target -value))

                                        ; REPLIKATIV

(def stream-eval-fns
  {'assoc (fn [a new]
            (swap! a assoc-in [:transactions (uuid new)] new)
            a)
   'dissoc (fn [a new]
             (swap! a update-in [:transactions] (fn [txs] (dissoc txs (uuid new))))
             a)})


(defonce val-atom (atom {}))


(defn setup-replikativ []
  (go-try S
          (let [local-store (<? S (new-mem-store))
                local-peer (<? S (client-peer S local-store))
                stage (<? S (create-stage! user local-peer))
                stream (stream-into-identity! stage
                                              [user ormap-id]
                                              stream-eval-fns
                                              val-atom)]
            (<? S (s/create-ormap! stage
                                   :description "messages"
                                   :id ormap-id))
            (connect! stage uri)
            {:store local-store
             :stage stage
             :stream stream
             :peer local-peer})))


(declare client-state)


(defn add-transaction! [app-state tx]
  (s/assoc! (:stage client-state)
            [user ormap-id]
            (uuid tx)
            [['assoc tx]]))

                                        ; QUERIES

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))




                                        ; VIEWS

(defn input-widget [component placeholder local-key type]
  [:input {:value (get (om/get-state component) local-key)
           :placeholder placeholder
           :type type
           :on-change (fn [e]
                        (om/update-state! component assoc local-key (target-val e)))}])


(defn transactions-widget [transactions]
  [:div
   [:h3 "Transactions"]
   [:table
    [:tr
     [:th "Description"]
     [:th "Date"]
     [:th "Type"]
     [:th "Value"]]
    (mapv
     (fn [{:keys [description value type date] :as tx}]
       [:tr {:key (uuid tx)}
        [:td description]
        [:td (str (js/Date. date))]
        [:td (str type)]
        [:td value]])
     (sort-by :date > transactions))]])


(defn transaction-add-button [component]
  (let [app-state (om/props component)
        {:keys [input-description input-value input-date input-type-toggle]} (om/get-state component)]
      [:button
       {:on-click (fn [e]
                    (do
                      (add-transaction!
                       app-state
                       {:description input-description
                        :created (.getTime (js/Date.))
                        :date (.getTime input-date)
                        :type (if input-type-toggle :expense :income)
                        :value input-value})
                      (om/update-state! component assoc :input-value nil)
                      (om/update-state! component assoc :input-date nil)
                      (om/update-state! component assoc :input-type-toggle true)
                      (om/update-state! component assoc :input-description "")))}
       "Add"]))


(defn type-toggle-widget [component]
  (let [toggle-state (:input-type-toggle (om/get-state component))]
    [:label.switch
     [:input {:type "checkbox"
              :checked toggle-state
              :on-click (fn [_] (om/update-state! component update :input-type-toggle not))}]
     (if toggle-state "Expense" "Income")]))


(defui App
  Object
  (componentWillMount [this]
    (om/set-state! this {:input-description ""
                         :input-type-toggle true
                         :input-date nil
                         :input-value nil}))
  (render [this]
    (let [app-state (om/props this)]
      (html
       [:div
        [:div
         [:h3 "New Transaction"]
         (input-widget this "Description" :input-description :text)
         (input-widget this "Value" :input-value :number)
         (input-widget this "Date" :input-date :time)
         (type-toggle-widget this)
         (transaction-add-button this)]
        (transactions-widget (vals (:transactions app-state)))]))))


(defn main [& args]
  (go-try S
          (def client-state (<? S (setup-replikativ)))
          (.error js/console "INITIATED")))


(def reconciler
  (om/reconciler {:state val-atom}))


(om/add-root! reconciler App (.getElementById js/document "app"))


(comment

  (get-in @val-atom [:input])
  (vals (get-in @val-atom [:transactions]))

  )
