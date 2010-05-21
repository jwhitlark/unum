;   Copyright (c) Jason Whitlark. 2009,2010 All rights reserved.
;   Copyright (c) Arthur Ulfeldt. 2010 All rights reserved.

(ns org.unum
  (:use [clojure.contrib.command-line])
  (:use [clojure.contrib.logging])
;  (:use [clojure.test])
  ;(:use [swank.swank])
;  (:require [swank.swank])
  (:use [org.unum.common])
  (:use [org.unum.hooks])
;  (:use [org.unum.net])
  (:use [org.unum.gui])
  (:gen-class))



(defn -main
  ([& args]
     (let [headless (contains? (set args) "--headless")]
       ; TODO: clean up logging output, default is a mess.
       (info "Starting up.")
       (do-hooks)
       (load-rc)
       ; init n2n
       ; init message bus
       ; init datastore
       (when-not headless
	 (create-gui))
       )))

; Tricky, need this for running outside of jar?
;(-main)

;;TODO: add git pre-commit-hook to bump build number of pom, or
;;perhaps minor, with the pom updating its build number each compile

