;   Copyright (c) Jason Whitlark. 2009 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum.notify
  (:use [clojure.contrib.shell-out :only (sh)])
  )

(defn notify-send [subject body]
  (sh "notify-send" subject body))
; TODO: add check that notify-send actually exists...


;; (defn Notify [& args]
;;   (do
;;     (notify-send "Notify" "Notify was clicked")
;;     (.remove popup 0))) ; example of removing menu entry

