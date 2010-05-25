;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

(ns org.unum.apps.swank
  (:use [clojure.contrib.logging])
  (:use [clojure.contrib.swing-utils])
  (:use clojure.test)

  (:use [swank.swank :as swank])
  (:use [org.unum.net])
  (:use [org.unum.mq])
  (:use [org.unum.notify :only (notify-send)])

  (:import [java.awt MenuItem Menu]))

(def port 9998)

(defn- start-swank-on-socket [& args]
  (do
    (info (str "starting slime (swank) repl on port " port))
    (swank/start-repl port)
    (notify-send "Swank" (str "Swank repl socket server started on port " port))))

(defn requirements-met? []
  "Test if current environment has needed functionallity."
  true)

(defn get-menu []
  (let [appMenu (Menu. "Swank")
	appMenuItem (MenuItem. "Start Swank server")]
    (doto appMenu
      (.add appMenuItem))
    (add-action-listener appMenuItem start-swank-on-socket)
    appMenu))
