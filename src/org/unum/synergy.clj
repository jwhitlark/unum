;   Copyright (c) Jason Whitlark. 2009 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum.synergy
  (:use [org.unum.notify])
  )

(def toolkit (java.awt.Toolkit/getDefaultToolkit))
(def edges {:left :right, :right :left, :top :bottom, :bottom :top})

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


(defn configure-synergy [edge remote-edge]
  ())

(defn synergy-command [& args]
  (notify-send "Synergy Configuration" "Select an edge with the mouse.")
  (let [edge (select-screen-edge)
	remote-edge (edge edges)]
    (notify-send "Synergy Configuration" (str edge " selected, remote edge will be " remote-edge))
    (configure-synergy edge remote-edge)))

(comment
(defn setup-and-start-synergy [remote-host local-edge]
  ((store-local-config remote-host local-edge)
   (store-remote-config remote-host (edges local-edge))
   (kill-local-and-remote-synergy)
   (start-local-and-remote-synergy)))
)
