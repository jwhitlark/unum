;   Copyright (c) Jason Whitlark. 2010 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns org.unum.camel
  (:use [clojure.contrib.logging])
  (:use [clojure.java.shell])
  (:use clojure.contrib.import-static)

  (:import (org.apache.camel.impl DefaultCamelContext)
	   (org.apache.activemq.broker BrokerService)
	   (org.apache.activemq.camel.component ActiveMQComponent)
	   (org.apache.camel.builder RouteBuilder)
	   (org.apache.camel Processor))
  (:gen-class))

(import-static org.apache.activemq.camel.component.ActiveMQComponent activeMQComponent)

(def NotifyProcessor (proxy [Processor] []
		       (process [exchange]
				(let [msg (.getIn exchange)
				      bdy (.getBody msg)]
				  (sh "notify-send" bdy)))))

(defn simple-camel []
  (def brokerSvc (BrokerService.))
  (doto brokerSvc
    (.setBrokerName "unum")
    (.addConnector "tcp://localhost:61616")
;;    (.addConnector "tcp://10.17.74.8:61616")
    (.addConnector "stomp://localhost:61613?trace=true")
;;    (.addConnector "vm://localhost?broker.persistent=false")
    (.start))


  (def context (DefaultCamelContext.))
  (doto context
    (.addComponent "unum" (activeMQComponent "vm://unum?broker.persistent=false")) ;;use vm
					;		(.addRoutes(new SampleRoute())

    (.addRoutes (proxy [RouteBuilder] []
			  (configure []
				     (.. this (from "unum:queue:bayes.notifications")
					 (process NotifyProcessor)))))
    (.start)))

;; (defn -main [& args]
;; (let [context (DefaultCamelContext.)]
;;   (.addRoutes context (proxy [RouteBuilder] []
;;     (configure []
;;          (.. this (from "file:/home/jw/scratch/inbox?noop=true")
;;                   (to "file:/home/jw/scratch/outbox")))))
;;   (.start context)))
