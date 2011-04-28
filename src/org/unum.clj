;   Copyright (c) Jason Whitlark. 2009,2010 All rights reserved.
;   Copyright (c) Arthur Ulfeldt. 2010 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns org.unum
  (:use [clojure.contrib.command-line])
  (:use [clojure.contrib.logging])
  (:require clojure.test)

  (:use [org.unum.common])
  (:use [org.unum.hooks])
  (:use [org.unum.n2n])
  (:use [org.unum.net])
  (:use [org.unum.mq])
  (:use [org.unum.camel])
  (:use [org.unum.gui])
  (:gen-class))


(defn -main
  ([& args]
     (let [headless (contains? (set args) "--headless")
	   only-run-tests (contains? (set args) "--run-tests")]
       ; TODO: clean up logging output, default is a mess.
       (org.apache.log4j.BasicConfigurator/configure)
       (info "Starting up.")
       (if only-run-tests
	 (do
	   (clojure.test/run-all-tests)
	   (exit)))
       (do-hooks)
       (load-rc)
       ; init n2n (assume it's been already configured for the moment as n2n0)
       (if (empty? (running-edge-processes))
	 (warn "No n2n edge processes found.")
	 (info "Existing n2n edge process available."))
       ;; init message bus
       (simple-camel)
       ;; (initialize-local-message-queue)
       ;; (init-announce-listener-socket)
       ;; (create-test-message-queue-and-handler)
       ;; (create-mq-admin-queue-and-handler)
       ;; (listen-and-add-transport-on-receipt-of-unum-announce-udp)
       ;; (broadcast-unum-annonunce-udp (my-unum-broadcast-address) default-udp-announce-port)
       ; init datastore
       (when-not headless
	 (create-gui)
	 (prn "started"))
       )))

; Tricky, need this for running outside of jar?
;(-main)

;;TODO: add git pre-commit-hook to bump build number of pom, or
;;perhaps minor, with the pom updating its build number each compile

