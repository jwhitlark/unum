(ns org.whitlark.test.mq
  (:use clojure.contrib.import-static)
  (:import (org.apache.activemq.broker BrokerService)
	   (org.apache.activemq ActiveMQConnectionFactory)
	   (javax.jms MessageListener))
)

(import-static javax.jms.Session AUTO_ACKNOWLEDGE)


