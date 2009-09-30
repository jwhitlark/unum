(ns org.unum.notify
  (:use [clojure.contrib.shell-out :only (sh)])
  )

(defn notify-send [subject body]
  (sh "notify-send" subject body))


;; (defn Notify [& args]
;;   (do
;;     (notify-send "Notify" "Notify was clicked")
;;     (.remove popup 0))) ; example of removing menu entry

