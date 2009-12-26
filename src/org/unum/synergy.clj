(ns org.unum.synergy
  (:use [org.unum.notify])
  )

(def toolkit (java.awt.Toolkit/getDefaultToolkit))

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

(defn synergy-command [& args]
  (notify-send "Synergy Configuration" "Select an edge with the mouse.")
  (let [edge (select-screen-edge)
	remote-edge (edge remote-edges)]
    (notify-send "Synergy Configuration" (str edge " selected, remote edge will be " remote-edge))
    (configure-synergy edge remote-edge)))