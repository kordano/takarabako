(ns takarabako.core
  (:gen-class :main true)
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes GET]]
            [org.httpkit.server :refer [send! with-channel on-close on-receive run-server]]
            [konserve.core :as k]
            [konserve.memory :refer [new-mem-store]]
            [konserve.filestore :refer [new-fs-store]]
            [clojure.java.io :as io]
            [clojure.core.async :refer [go <!!]]))

;;--------------------------------------------------------------------------------
;; Database

(defn initialize-store [store]
  (<!! (k/assoc-in store
                   [:finances/collection]
                   [{:type :outcome
                     :category "Rewe"
                     :date "2015-10-01"
                     :value 10.02}
                    {:type :outcome
                     :category "Aldi"
                     :date "2014-10-02"
                     :value 5.67}
                    {:type :income
                     :category "URZ"
                     :date "2015-10-01"
                     :value 500.00}
                    {:type :outcome
                     :category "Studentenwerk"
                     :date "2015-10-10"
                     :value 25.00}])))

(def local-store (<!! (new-mem-store)))

;;--------------------------------------------------------------------------------
;; Server

(defn now [] (new java.util.Date))

(defn dispatch [{:keys [type data meta] :as action}]
  (case type
    :init (assoc action :data (<!! (k/get-in local-store [:finances/collection])))
    :add (assoc action :data (<!! (k/update-in local-store [:finances/collection] #(conj % data))))
    :unrelated))

(defn create-socket-handler [state]
  (fn [request]
    (with-channel request channel
      (on-close channel (fn [status]))
      (on-receive channel
                  (fn [data]
                    (let [msg (read-string data)]
                      (send! channel (str (dispatch msg)))))))))

(defn create-routes
  "Create routes from server state"
  [state]
  (defroutes all-routes
    (route/resources "/")
    (GET "/" [] (io/resource "public/index.html"))
    (GET "/ws" [] (create-socket-handler state))
    (route/not-found "<h1>Page not found</h1>")))

(defn start-server
  "startAllServices™"
  [port]
  (let [state (atom {:server nil})]
    (create-routes @state)
    (initialize-store local-store)
    (swap! state assoc :server (run-server #'all-routes {:port port}))
    state))

(defn -main [& args]
  (let [port (if (second args)(Integer/parseInt (second args)) 8090)]
    (start-server port)
    (println (str "Server startet at localhost:" port))))

(comment

  (def state (start-server 8090))
  
  ((:server @state))

  (initialize-store (:store @state))
  
  (<!! (k/get-in (:store @state) [:finances/collection]))

  
  )

