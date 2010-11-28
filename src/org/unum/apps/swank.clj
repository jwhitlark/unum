;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


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
