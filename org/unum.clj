(ns org.unum
  (:use [clojure.contrib.swing-utils])
  (:use [org.unum.synergy :only (synergy-command)])
  (:use [org.unum.notify :only (notify-send)])
  (:use [org.unum.zookeeper])
  (:import [java.io File])
  (:import [javax.imageio ImageIO])
  (:import (java.net InetAddress))
  (:import [java.awt TrayIcon Toolkit PopupMenu MenuItem Menu CheckboxMenuItem])
  (:gen-class))


;(def hostname (.getHostName (InetAddress/getLocalHost))) ; already defined in zookeeper
(def user-home (java.lang.System/getProperty "user.home"))
; Need a square image to deal with transparency issues.
(def toolkit (Toolkit/getDefaultToolkit))
(def image-url (ClassLoader/getSystemResource "images/icon.png"))
(def tray-icon (TrayIcon. (.getScaledInstance (.getImage toolkit image-url) 24 24 1) "Unum Constellation Manager" ))
(def tray (java.awt.SystemTray/getSystemTray))
(def popup (PopupMenu.))


;; ---------- menu items ----------
(defn Identify [& args]
  (notify-send hostname "Other Constellation members will also display their names"))

(defn exit [& args]
  (System/exit 0))

(defn setup-menu []
  (let [exitItem (MenuItem. "Exit")
	synergyItem (MenuItem. "Synergy")
	IdentifyItem (MenuItem. "Identify")
	bsItem (MenuItem. "NA")
	machineMenu (Menu. "Members")
	configMenu (Menu. "Configuration")
	;; cbItem (CheckboxMenuItem. "running")
	]
    (doto popup
      ;; (.add cbItem)
      (.add synergyItem)
      (.add IdentifyItem)
      (.add machineMenu)
      (.add configMenu)
      (.addSeparator)
      (.add exitItem))

    (.add machineMenu bsItem)

    (.setEnabled configMenu false)
    (.setEnabled bsItem false)
    ;; ----- action listeners -----
    (add-action-listener IdentifyItem Identify)
    (add-action-listener exitItem exit)
    (add-action-listener synergyItem synergy-command)

    (.setPopupMenu tray-icon popup)))

; Should left click mean anything?
; Should this spin off in another thread?
(defn -main
  ([& args]
     (if-not (java.awt.SystemTray/isSupported)
       (do (println "System Tray is not supported")
	   (System/exit 0)))

     (println "Connecting to zookeeper")
     (do-zookeeper "192.168.1.4:2181")))
     (println "Connected to zookeeper")
     (setup-menu)
     (.add tray tray-icon)


; Tricky, need this for running outside of jar?
(-main)
