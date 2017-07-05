(ns reloaded.repl
  (:require [com.stuartsierra.component :as component]
            [clojure.stacktrace :refer [print-cause-trace]]
            [clojure.tools.namespace.repl :refer [disable-reload! refresh refresh-all]]
            [suspendable.core :as suspendable]))

(disable-reload!)

(def system nil)

(def initializer nil)

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
  (try
    (alter-var-root #'system component/start)
    :started
    (catch Exception e
      (println "Error whilst starting the system:")
      (print-cause-trace e)
      (println "Attempting to stop the failed system")
      (try
        (component/stop (:system (ex-data e)))
        :failed-with-recovery
        (catch Exception e
          (println "Error ocurred whilst stopping!")
          (print-cause-trace e)
          :failed-without-recovery)))))

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
    (try
      (alter-var-root #'system #(suspendable/resume (init) %))
      :resumed
      (catch Exception e
        (println "Error whilst resuming the system:")
        (print-cause-trace e)
        (println "Attempting to suspend the failed system")
        (try
          (suspendable/suspend (:system (ex-data e)))
          :failed-with-recovery
          (catch Exception e
            (println "Error ocurred whilst suspending!")
            (print-cause-trace e)
            :failed-without-recovery))))
    (throw (init-error))))

(defn reset []
  (suspend)
  (refresh :after 'reloaded.repl/resume))

(defn reset-all []
  (suspend)
  (refresh-all :after 'reloaded.repl/resume))
