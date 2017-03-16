(ns reloaded.repl-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.tools.namespace.repl :refer [disable-reload!]]
            [com.stuartsierra.component :as component]
            [reloaded.repl :as repl]
            [suspendable.core :as suspendable]))

(disable-reload!)

(defrecord TestSystem [connection]
  component/Lifecycle
  (start [component]
    (assoc component :connection (atom :connected)))
  (stop [component]
    (if connection
      (do (reset! connection :disconnected)
          (assoc component connection nil))
      component))
  suspendable/Suspendable
  (suspend [component]
    (when connection
      (reset! connection :suspended))
    component)
  (resume [component old-component]
    (let [old-connection (:connection old-component)]
      (reset! old-connection :resumed)
      (assoc component :connection old-connection))))

(defn- new-system []
  (->TestSystem nil))

(deftest set-init-test
  (repl/set-init! new-system)
  (is (= repl/initializer new-system))
  (repl/set-init! nil))

(defmacro with-init
  [& body]
  `(do (repl/set-init! new-system)
       ~@body
       (repl/set-init! nil)))

(deftest init-test
  (with-init
    (is (= (repl/init) :ok))
    (is (= repl/system (new-system)))
    (repl/clear)))

(deftest start-test
  (with-init
    (repl/init)
    (is (= (repl/start) :started))
    (is (= @(:connection repl/system) :connected))
    (repl/clear)))

(deftest stop-test
  (with-init
    (repl/init)
    (repl/start)
    (is (= (repl/stop) :stopped))
    (is (= @(:connection repl/system) :disconnected))
    (repl/clear)))

(deftest go-test
  (with-init
    (is (= (repl/go) :started))
    (is (instance? TestSystem repl/system))
    (is (= @(:connection repl/system) :connected))
    (repl/clear)))

(deftest clear-test
  (with-init
    (repl/go)
    (let [connection (:connection repl/system)]
      (is (= (repl/clear) :ok))
      (is (= @connection :disconnected))
      (is (= repl/system nil)))))

(deftest suspend-test
  (with-init
    (repl/go)
    (is (= :suspended (repl/suspend)))
    (is (= @(:connection repl/system) :suspended))
    (repl/clear)))

(deftest resume-test
  (with-init
    (repl/go)
    (repl/suspend)
    (let [old-system repl/system]
      (is (= (repl/resume) :resumed))
      (is (= repl/system old-system))
      (is (= @(:connection repl/system) :resumed)))
    (repl/clear)))

(deftest reset-test
  (with-init
    (repl/go)
    (is (= (repl/reset) :resumed))
    (is (= @(:connection repl/system) :resumed))
    (repl/clear)))

(deftest reset-all-test
  (with-init
    (repl/go)
    (is (= (repl/reset-all) :resumed))
    (is (= @(:connection repl/system) :resumed))
    (repl/clear)))
