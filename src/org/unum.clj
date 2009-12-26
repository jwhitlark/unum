(ns org.unum
  (:use [clojure.contrib.swing-utils])
  (:use [clojure.contrib.command-line])
  (:use [clojure.contrib.server-socket :only (create-repl-server)])
;  (:use [swank.swank])
  (:use [org.unum.synergy :only (synergy-command)])
  (:use [org.unum.notify :only (notify-send)])
  (:use [org.unum.hooks])
  (:import [java.io File])
  (:import [javax.swing UIManager])
  (:import [javax.imageio ImageIO])
  (:import (java.net InetAddress))
  (:import [java.awt TrayIcon Toolkit PopupMenu MenuItem Menu CheckboxMenuItem])
  (:gen-class))


;; Use the operating system native UI look and feel, do not use the Swing oriented look
(UIManager/setLookAndFeel "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")

(def hostname (.getHostName (InetAddress/getLocalHost)))

; Tell swank not to be picky, this should go somewhere else...
;(ignore-protocol-version "2009-03-09")

(def user-home (java.lang.System/getProperty "user.home"))
; Need a square image to deal with transparency issues.
(def toolkit (Toolkit/getDefaultToolkit))

(def icon-image (.getImage toolkit (ClassLoader/getSystemResource "icon.png")))
(def tray-icon (TrayIcon. (.getScaledInstance icon-image 24 24 1) "Unum Constellation Manager" ))
(def tray (java.awt.SystemTray/getSystemTray))
(def popup (PopupMenu.))

;; Stuff to hold the pointer to the machine menu
(declare machine-menu-obj)

(defn send-tray-message [title msg]
  "Use swing tray to popup a notification box"
  (.displayMessage tray-icon title msg java.awt.TrayIcon$MessageType/INFO))

(defn sleep [secs]
  (Thread/sleep (* secs 1000)))

(defn update-machine-menu []
  "")

;; ---------- menu items ----------
(defn Identify [& args]
  (notify-send hostname "Other Constellation members will also display their names"))

(defn exit [& args]
  (fire-event :kill-unum-hook)
  ;; FIXME: there should be a better way than just sleep, but it'll
  ;; also need a time out
  (sleep 2)
  (System/exit 0))

(defn popup-listener-callback [& args]
  (println "popup called"))

(defn start-repl-on-socket [& args]
  (do
    (println "starting socket repl on port 9999")
    (create-repl-server 9999)))

;; (defn start-swank-on-socket [& args]
;;   (do
;;     (println "starting slime (swank) repl on port 9998")
;;     (start-server "/tmp/slime.20900" :encoding "iso-latin-1-unix" :port 9998)))

(defn setup-menu []
  (let [exitItem (MenuItem. "Exit")
	synergyItem (MenuItem. "Synergy")
	IdentifyItem (MenuItem. "Identify")
	replServerItem (MenuItem. "REPL socket server")
;	swankServerItem (MenuItem. "SWANK socket server")
	bsItem (MenuItem. "NA")
	machineMenu (Menu. "Members")
	configMenu (Menu. "Configuration")
	;; cbItem (CheckboxMenuItem. "running")
	]
    ;link this up so we can manipulate it later.
    (def machine-menu-obj machineMenu)
    (doto popup
      ;; (.add cbItem)
      (.add synergyItem)
      (.add IdentifyItem)
      (.add machineMenu)
      (.add configMenu)
      (.addSeparator)
      (.add exitItem))

    (doto configMenu
      (.add replServerItem))
;      (.add swankServerItem))

;    (.add machineMenu bsItem)

    (.setEnabled bsItem false)
    ;; ----- action listeners -----
    (add-action-listener IdentifyItem Identify)
    (add-action-listener exitItem exit)
    (add-action-listener synergyItem synergy-command)
    (add-action-listener popup popup-listener-callback)
    ;; configMenu
    (add-action-listener replServerItem start-repl-on-socket)
;    (add-action-listener swankServerItem start-swank-on-socket)

    (.setPopupMenu tray-icon popup)))

(defn exit-if-no-system-tray []
  (if-not (java.awt.SystemTray/isSupported)
    (do (println "System Tray is not supported")
	(System/exit 0))))

(defn load-rc []
  (load-file (str user-home "/.unumrc")))

; add command line option to just run registry (zookeeper)?  Handy...
(defn -main
  ([& args]
     (let [registry? (contains? (set args) "--serve-registry")
	   headless? (or registry? (contains? (set args) "--headless"))]
       (if registry?
	 (do
	   ; This was for starting the registry server
	   (System/exit 0)))
       (when-not headless?
	 (exit-if-no-system-tray))
       (do-hooks)
       (load-rc)
       ;; Here is where we used to connect to zookeeper
       (when-not headless?
	 (setup-menu)
	 (update-machine-menu)
	 (.add tray tray-icon))
       )))

; Tricky, need this for running outside of jar?
;(-main)

;;TODO: add git pre-commit-hook to bump build number of pom, or
;;perhaps minor, with the pom updating its build number each compile
