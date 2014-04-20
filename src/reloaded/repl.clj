(ns reloaded.repl
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [disable-reload! refresh]]))

(disable-reload!)

(def system nil)

(defn init []
  (throw (Error. "No system initializer function found.")))

(defn- make-init [init]
  (fn []
    (stop)
    (alter-var-root #'system (fn [_] (init)))
    :ok))

(defn set-init! [init]
  (alter-var-root #'init (fn [_] (make-init init))))

(defn start []
  (alter-var-root #'system component/start)
  :started)

(defn stop []
  (alter-var-root #'system #(when % (component/stop %)))
  :stopped)

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'reloaded.repl/go))
