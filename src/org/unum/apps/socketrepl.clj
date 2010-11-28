;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum.apps.socketrepl
  (:use [clojure.contrib.logging])
  (:use [clojure.contrib.swing-utils])
  (:use clojure.test)

  (:use [clojure.contrib.server-socket :only (create-repl-server)])

  (:use [org.unum.net])
  (:use [org.unum.mq])
  (:use [org.unum.notify :only (notify-send)])

  (:import [java.awt MenuItem Menu]))

(def port 9990)

(defn- start-repl-on-socket [& args]
  (do
    (info (str "starting repl on port " port))
    (create-repl-server port)
    (notify-send "Repl" (str "Repl socket server started on port " port))))

(defn requirements-met? []
  "Test if current environment has needed functionallity."
  true)

(defn get-menu []
  (let [appMenu (Menu. "Socket Repl")
	appMenuItem (MenuItem. "Start Repl Socket server")]
    (doto appMenu
      (.add appMenuItem))
    (add-action-listener appMenuItem start-repl-on-socket)
    appMenu))
