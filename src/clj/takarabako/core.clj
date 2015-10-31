(ns takarabako.core
  (:gen-class :main true)
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes GET]]
            [clojure.java.io :as io]
            [org.httpkit.server :refer [send! with-channel on-close on-receive run-server]]
            [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.core.async :refer [go <!!]])
  (:import datomic.Util))

;;--------------------------------------------------------------------------------
;; Database

(def db-uri-base "datomic:free://0.0.0.0:4334")

(defn scratch-conn
  "Create a connection to an anonymous, in-memory database."
  []
  (let [uri (str "datomic:mem://" (d/squuid))]
    (d/delete-database uri)
    (d/create-database uri)
    (d/connect uri)))


(defn persistent-conn
  "Create connection to persistent datomic database"
  []
  (let [uri (str db-uri-base "/haushalt")]
    (d/create-database uri)
    (d/connect uri)))


(defn initialize-db [conn path]
  (doseq [txd (-> path io/resource io/reader Util/readAll)]
    (d/transact conn txd))
  :done)


;;--------------------------------------------------------------------------------
;; Server

(defn now [] (new java.util.Date))

(defn create-socket-handler [state]
  (fn [request]
    (with-channel request channel
      (on-close channel (fn [status]))
      (on-receive channel (fn [data]
                            (let [action (read-string data)]
                              (println action)))))))
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
    (swap! state assoc :server (run-server #'all-routes {:port port}))
    state))

(defn -main [& args]
  (let [port (second args)]
    (start-server (if port (Integer/parseInt port) 8090))
    (println "Server startet at localhost:8080")))

(comment

  (def state (start-server 8090))
 
  (def conn (scratch-conn))

  (def db (d/db conn))
  
  (initialize-db conn "schema.edn")
 
  )

