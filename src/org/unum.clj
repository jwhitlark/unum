(ns org.unum
  (:use [clojure.contrib.swing-utils])
  (:use [clojure.contrib.shell-out :only (sh)])
  (:import [java.io File])
  (:import [javax.imageio ImageIO])
  (:import (java.net InetAddress))
  (:import [java.awt TrayIcon Toolkit PopupMenu MenuItem Menu CheckboxMenuItem])
  (:gen-class))

(if-not (java.awt.SystemTray/isSupported)
  (do (println "System Tray is not supported")
      (System/exit 0)))

(defn notify-send [subject body]
  (sh "notify-send" "-i" "/home/jw/dev/unum/unum/icon.png" subject body))

(def hostname (.getHostName (InetAddress/getLocalHost)))
(def user-home (java.lang.System/getProperty "user.home"))
; Need a square image to deal with transparency issues.
(def toolkit (Toolkit/getDefaultToolkit))
(def tray-icon (TrayIcon. (.getScaledInstance (.getImage toolkit "/home/jw/dev/unum/unum/icon.png") 24 24 1) "Unum Constellation Manager" ))
(def tray (java.awt.SystemTray/getSystemTray))
(def popup (PopupMenu.))


(defn get-screen-size []
  (let [sz (.getScreenSize toolkit)
	w (.getWidth sz)
	h (.getHeight sz)]
    (list w h)))

(defn get-mouse-location []
  (let [loc (.getLocation (java.awt.MouseInfo/getPointerInfo))
	X (.getX loc)
	Y (.getY loc)]
    (list X, Y)))

(defn select-screen-edge []
  (let [[max_x max_y] (get-screen-size)]
    (loop [[x y] (get-mouse-location)]
      ;TODO: profile this, see if we need a call to sleep
      (cond
	(= x 0)			:left
	(= x (dec max_x))	:right
	(= y 0)			:top
	(= y (dec max_y))	:bottom
	:default    (recur (get-mouse-location))))))

(def remote-edges {:left :right, :right :left, :top :bottom, :bottom :top})

(defn configure-synergy [edge remote-edge]
  ())

(defn About [& args]
  (notify-send "Synergy Configuration" "Select an edge with the mouse.")
  (let [edge (select-screen-edge)
	remote-edge (edge remote-edges)]
    (notify-send "Synergy Configuration" (str edge " selected, remote edge will be " remote-edge))
    (configure-synergy edge remote-edge)))

;; (defn Notify [& args]
;;   (do
;;     (notify-send "Notify" "Notify was clicked")
;;     (.remove popup 0)))

(defn Identify [& args]
  (notify-send hostname "Other Constellation members will also display their names"))

(defn exit [& args]
  (System/exit 0))



(defn setup-menu []
  (let [exitItem (MenuItem. "Exit")
	MouseItem (MenuItem. "Mouse")
	IdentifyItem (MenuItem. "Identify")
	bsItem (MenuItem. "NA")
	machineMenu (Menu. "Members")
	configMenu (Menu. "Configuration")
					;      cbItem (CheckboxMenuItem. "running")
	]
    (doto popup
					;    (.add cbItem)
      (.add MouseItem)
      (.add IdentifyItem)
      (.add machineMenu)
      (.add configMenu)
      (.addSeparator)
      (.add exitItem))
    (.add machineMenu bsItem)
    (.setEnabled configMenu false)
    (.setEnabled bsItem false)
    (add-action-listener IdentifyItem Identify)
    (add-action-listener exitItem exit)
    (add-action-listener MouseItem About)
    (.setPopupMenu tray-icon popup)))

; Should left click mean anything?
; Should this spin off in another thread?
(defn -main
  ([]
     (setup-menu)
     (.add tray tray-icon))
  ([& args]
     (println "in the wrong place?")))

