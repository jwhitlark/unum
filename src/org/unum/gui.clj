
(ns org.unum.gui
  (:use [clojure.contrib.logging])
  (:use [clojure.contrib.swing-utils])
  (:use [clojure.contrib.server-socket :only (create-repl-server)])
  (:use [swank.swank :as swank])
  (:use [org.unum.common])
  (:use [org.unum.notify :only (notify-send)])
  (:use [org.unum.net])
  (:use [org.unum.mq])
  (:use [org.unum.synergy :only (synergy-command)])
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
      (def popup (PopupMenu.))

      ;; Stuff to hold the pointer to the machine menu
      (declare machine-menu-obj)))

(defn send-tray-message [title msg]
  "Use swing tray to popup a notification box"
  (.displayMessage tray-icon title msg java.awt.TrayIcon$MessageType/INFO))

(defn update-machine-menu []
  "")

;; ---------- menu items ----------
(defn Identify [& args]
  (notify-send hostname "Other Constellation members will also display their names"))

(defn popup-listener-callback [& args]
  (info "popup called"))

(defn start-repl-on-socket [& args]
  (do
    (info "starting socket repl on port 9999")
    (create-repl-server 9999)))

(defn start-swank-on-socket [& args]
  (do
    (info "starting slime (swank) repl on port 9998")
    (swank/start-repl 9998)))

(defn send-broadcast-announce-packet [& args]
  (do
    (info "sending broadcast announce udp packet.")
    (broadcast-unum-annonunce-udp (my-unum-broadcast-address) default-udp-announce-port)))


(defn send-message-to-queue [& args]
  ;TODO: make a single dialog box
  (let [queue (JOptionPane/showInputDialog "Enter queue name.")
	msg (JOptionPane/showInputDialog "Enter message text.")]
    (send-msg queue msg)))

(defn setup-menu []
  (let [exitItem (MenuItem. "Exit")
	synergyItem (MenuItem. "Synergy")
	IdentifyItem (MenuItem. "Identify")
	replServerItem (MenuItem. "REPL socket server")
	swankServerItem (MenuItem. "SWANK socket server")
	sendMsgItem (MenuItem. "Send a text message to a queue.")
	broadcastAnnounceItem (MenuItem. "Broadcast unum announcement packet.")
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
      (.add sendMsgItem)
      (.add broadcastAnnounceItem)
      (.add replServerItem)
      (.add swankServerItem))


;    (.add machineMenu bsItem)


    ;; ----- action listeners -----
    (add-action-listener IdentifyItem Identify)
    (add-action-listener exitItem exit)
    (add-action-listener synergyItem synergy-command)
    (add-action-listener popup popup-listener-callback)
    ;; configMenu
    (add-action-listener sendMsgItem send-message-to-queue)
    (add-action-listener replServerItem start-repl-on-socket)
    (add-action-listener swankServerItem start-swank-on-socket)
    (add-action-listener broadcastAnnounceItem send-broadcast-announce-packet)

    (.setPopupMenu tray-icon popup)))


(defn create-gui []
  (if (system-tray-available?)
    (do (UIManager/setLookAndFeel "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
	(declare-gui-objects)
	(setup-menu)
	(update-machine-menu)
	(.add tray tray-icon))
    (do (fatal "Tried to start GUI, but System Tray is not supported")
	(System/exit 0))))


