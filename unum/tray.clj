(ns org.unum.tray
  (:use [clojure.contrib.swing-utils])
  (:import [java.io File])
  (:import [javax.imageio ImageIO])
  (:import [java.awt TrayIcon Toolkit])
  )

(if-not (java.awt.SystemTray/isSupported)
  (do (println "System Tray is not supported")
      (System/exit 0)))

(def user-home (java.lang.System/getProperty "user.home"))
; Need a square image to deal with transparency issues.
(def tray-icon (TrayIcon. (.getScaledInstance (.getImage (Toolkit/getDefaultToolkit) "/home/jw/dev/unum/unum/icon.png") 24 24 1) "unum" ))
; rewrite the following to work and read cleanly
;(def tray-icon (.. (Toolkit/getDefaultToolkit) (.getImage  "/home/jw/dev/unum/unum/icon.png") (.getScaledInstance  24 24 1) (TrayIcon.  "unum" )))

(def tray (java.awt.SystemTray/getSystemTray))

(defn foo [& args]
  println "got click")

; Listening for a click does not work at the moment...
(add-action-listener tray-icon foo)

(.add tray tray-icon)

;; (println "sleeping for 3 secs")
;; (Thread/sleep 3000)
