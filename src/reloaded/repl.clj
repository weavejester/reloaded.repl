(ns reloaded.repl
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [disable-reload! refresh refresh-all]]
            [suspendable.core :as suspendable]))

(disable-reload!)

(def system nil)

(def ^:private initializer nil)

(defn set-init! [init]
  (alter-var-root #'initializer (constantly init)))

(defn- stop-system [s]
  (if s (component/stop s)))

(defn- init-error []
  (Error. "No system initializer function found."))

(defn init []
  (if-let [init initializer]
    (do (alter-var-root #'system #(do (stop-system %) (init))) :ok)
    (throw (init-error))))

(defn start []
  (alter-var-root #'system component/start)
  :started)

(defn stop []
  (alter-var-root #'system stop-system)
  :stopped)

(defn go []
  (init)
  (start))

(defn clear []
  (alter-var-root #'system #(do (stop-system %) nil))
  :ok)

(defn suspend []
  (alter-var-root #'system #(if % (suspendable/suspend %)))
  :suspended)

(defn resume []
  (if-let [init initializer]
    (do (alter-var-root #'system #(suspendable/resume (init) %)) :resumed)
    (throw (init-error))))

(defn reset []
  (suspend)
  (refresh :after 'reloaded.repl/resume))

(defn reset-all []
  (suspend)
  (refresh-all :after 'reloaded.repl/resume))
