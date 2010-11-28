;   Copyright (c) Jason Whitlark. 2009 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns org.unum.gui
  (:use [clojure.contrib.logging])
  (:use [clojure.contrib.swing-utils])
  (:use [org.unum.common])
  (:use [org.unum.notify :only (notify-send)])
  (:use [org.unum.net])
  (:use [org.unum.mq])

  (:require [org.unum.apps.identify :as Identify])
  (:require [org.unum.apps.swank :as Swank])
  (:require [org.unum.apps.socketrepl :as Socketrepl])
  (:require [org.unum.apps.debug-mq :as Debug-mq])
;  (:require [org.unum.apps.synergy :as Synergy])

  (:import [javax.swing UIManager JOptionPane])
  (:import [javax.imageio ImageIO])
  (:import [java.awt TrayIcon Toolkit PopupMenu MenuItem Menu CheckboxMenuItem]))

(defn system-tray-available? []
  (java.awt.SystemTray/isSupported))

(defn- declare-gui-objects []
  (do (def toolkit (Toolkit/getDefaultToolkit))
      (def icon-image (.getImage toolkit (ClassLoader/getSystemResource "resources/icon.png")))
      (def scaled-icon (.getScaledInstance icon-image 24 24 1))
      (def tray-icon (TrayIcon. scaled-icon "Unum Constellation Manager" ))
      (def tray (java.awt.SystemTray/getSystemTray))
      (def popup (PopupMenu.))))

(defn send-tray-message [title msg]
  "Use swing tray to popup a notification box"
  (.displayMessage tray-icon title msg java.awt.TrayIcon$MessageType/INFO))

;; ---------- menu items ----------

(defn popup-listener-callback [& args]
  (info "popup called"))

(defn setup-menu []
  (let [exitItem (MenuItem. "Exit")
	appMenu (Menu. "Apps")
	configMenu (Menu. "Configuration")
	]

    (doto popup
      (.add appMenu)
      (.add configMenu)
      (.addSeparator)
      (.add exitItem))

    ;; ========== App specific ==========

    (doto appMenu
      (.add (Identify/get-menu))
      (.add (Socketrepl/get-menu))
      (.add (Swank/get-menu))
      (.add (Debug-mq/get-menu)))
;      (.add (Synergy/get-menu))

    (Identify/setup-listener)
;    (Synergy/setup-listener)

    ;; ----- action listeners -----

    (add-action-listener exitItem exit)
    (add-action-listener popup popup-listener-callback)

    (.setPopupMenu tray-icon popup)))


(defn create-gui []
  (if (system-tray-available?)
    (do (UIManager/setLookAndFeel "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
	(declare-gui-objects)
	(setup-menu)
	(.add tray tray-icon))
    (do (fatal "Tried to start GUI, but System Tray is not supported")
	(System/exit 0))))


