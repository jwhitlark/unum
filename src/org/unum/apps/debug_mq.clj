;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

(ns org.unum.apps.debug-mq
  (:use [clojure.contrib.logging])
  (:use [clojure.contrib.swing-utils])
  (:use clojure.test)


  (:use [org.unum.net])
  (:use [org.unum.mq])
  (:use [org.unum.notify :only (notify-send)])

  (:import [javax.swing UIManager JOptionPane])
  (:import [java.awt MenuItem Menu]))


(defn- send-broadcast-announce-packet [& args]
  (do
    (info "sending broadcast announce udp packet.")
    (broadcast-unum-annonunce-udp (my-unum-broadcast-address) default-udp-announce-port)
    (notify-send "Debug MQ - Broadcast" "Unum Broadcast announcement UDP packet sent.")))


(defn- send-message-to-queue [& args]
  ;TODO: make a single dialog box
  (let [queue (JOptionPane/showInputDialog "Enter queue name.")
	msg (JOptionPane/showInputDialog "Enter message text.")]
    (send-msg queue msg)
    (notify-send (str "Debug MQ - Message sent to " queue) (str "Message text:\n" msg))))


(defn requirements-met? []
  "Test if current environment has needed functionallity."
  true)

(defn get-menu []
  (let [appMenu (Menu. "Debug MQ tools")
	appMenuItem (MenuItem. "Send broadcast UDP Announce packet.")
	appMenuItem2 (MenuItem. "Send MQ message.")]
    (doto appMenu
      (.add appMenuItem)
      (.add appMenuItem2))
    (add-action-listener appMenuItem send-broadcast-announce-packet)
    (add-action-listener appMenuItem2 send-message-to-queue)
    appMenu))
