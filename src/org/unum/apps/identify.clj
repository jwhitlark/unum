;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum.apps.identify
  (:use [clojure.contrib.swing-utils])
  (:use clojure.test)

  (:use [org.unum.net])
  (:use [org.unum.mq])
  (:use [org.unum.notify :only (notify-send)])

  (:import [java.awt MenuItem Menu]))

(def app-queue "unum.events.apps.identify")

(defn requirements-met? []
  "Test if current environment has needed functionallity."
  true)

(defn- show-message []
  (notify-send hostname "Other Constellation members will also display their names"))

(defn- fire-event []
  (send-msg app-queue hostname))

(defn- show-identity [& args]
  (show-message)
  (fire-event))

(defn get-menu []
  (let [appMenu (Menu. "Identify")
	IdentifyItem (MenuItem. "Identify hosts visually.")]
    (doto appMenu
      (.add IdentifyItem))
    (add-action-listener IdentifyItem show-identity)
    appMenu))

(defn setup-listener []
  (create-msg-handler app-queue #(if-not (= (.getText %1) hostname)
				   (show-message))))
