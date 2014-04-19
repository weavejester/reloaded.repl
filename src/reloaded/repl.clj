(ns reloaded.repl
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [disable-reload! refresh]]))

(disable-reload!)

(def system nil)

(defn init []
  (binding [*out* *err*] (println "Warning: No system initializer function defined."))
  :ok)

(defn set-init! [init]
  (alter-var-root #'init (fn [_] (fn [] (alter-var-root #'system (fn [_] (init))) :ok))))

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
