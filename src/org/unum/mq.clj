;   Copyright (c) Jason Whitlark. 2010 All rights reserved.
;
;   NOTE: Current code is very wasteful, (new connection every time), but works.
;   You'll need something like (def brk (create-simple-broker)) before you call send-msg and read-msg.

(ns org.unum.mq
  (:use org.unum.net)
  (:use clojure.contrib.import-static)

  (:import (org.apache.activemq.broker BrokerService)
	   (org.apache.activemq ActiveMQConnectionFactory))
)

(import-static javax.jms.Session AUTO_ACKNOWLEDGE)

;; ----- Connecting via STOMP & telnet
; $telnet localhost 61617
; CONNECT
; login:system
; passcode:manager
;
; ^@
; SEND
; destination:/queue/TEST.FOO
;
; hello, TEST.FOO queue
; ^@
;
; SUBSCRIBE
; destination:/queue/TEST.FOO
;
; ^@
; <Message prints here
; UNSUBSCRIBE
; destination:/queue/TEST.FOO
;
; ^@
; DISCONNECT
;
; ^@
; Connection closed by foreign host


; Don't forget to use *1, *2, etc. to get prev. results in the REPL

;Starts JMX by default, connect to
; service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi with JConsole to
; view/manipulate.  Very Useful & functional!

(defn create-simple-broker []
  (doto (BrokerService.)
    (.setBrokerName "fred")
    (.setUseShutdownHook false)
    (.addNetworkConnector "multicast://default")
    (.addConnector "tcp://localhost:61616") ;openwire (activeMQ native fmt)
    (.addConnector "stomp://localhost:61617")
    (.addConnector "xmpp://localhost:61618")
    (.start)))


(defn create-connection [user password url]
  (let [connectionFactory (ActiveMQConnectionFactory. user password url)
	connection (.createConnection connectionFactory)]
    connection))

(defn create-session [connection]
  (.start connection)
  (let [transacted false ; Not sure what this does...
	session (.createSession connection transacted AUTO_ACKNOWLEDGE)]
    session))

(defn create-consumer [session queue-name]
  (let [destination (.createQueue session queue-name)
	consumer (.createConsumer session destination)]
    consumer))

(defn create-producer [session queue-name]
  (let [destination (.createQueue session queue-name)
	producer (.createProducer session destination)]
    producer))

(defn send-msg [user password url queue-name message-text]
  (with-open [connection (create-connection user password url)
	      session (create-session connection)
	      producer (create-producer session queue-name)]
    (let [message (.createTextMessage session message-text)]
      (.send producer message))))

(defn read-msg [user password url queue-name timeout]
  (let [timeout 1000] ; In milli-seconds.
    (with-open [connection (create-connection user password url)
		session (create-session connection)
		consumer (create-consumer session queue-name)]
      (.getText (.receive consumer timeout))))) ; .getText Only works on TextMessage


;; ========== JAVA SAMPLES ==========
;; ----- Broker configuration in Java-----
;; BrokerService broker = new BrokerService();
;; broker.setBrokerName("fred");
;; broker.setUseShutdownHook(false);
;; //Add plugin
;; broker.setPlugins(new BrokerPlugin[]{new JaasAuthenticationPlugin()});
;; //Add a network connection
;; NetworkConnector connector = answer.addNetworkConnector("static://"+"tcp://somehost:61616");
;; connector.setDuplex(true);
;; broker.addConnector("tcp://localhost:61616");
;; broker.start();

;; BrokerService broker = new BrokerService();
;; // configure the broker
;; broker.setBrokerName("fred");  ; apparently optional
;; broker.addConnector("tcp://localhost:61616");
;; broker.start();


;; ----- Connection: producer in java
;; ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
;; connection = connectionFactory.createConnection();
;; connection.start();

;; Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);

;; Destination destination = session.createQueue("TEST.FOO");

;; MessageProducer producer = session.createProducer(destination);
;; TextMessage message = session.createTextMessage("hello");
;; producer.send(message);
;; ...
;; producer.close();
;; session.close();
;; connection.close();


;; ----- Connection: consumer in Java
;; ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
;; connection = connectionFactory.createConnection();
;; connection.start();

;; Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);

;; Destination destination = session.createQueue("TEST.FOO");
;; MessageConsumer consumer = session.createConsumer(destination);
;; Message message = consumer.receive(1000);
;; ...
;; consumer.close();
;; session.close();
;; connection.close();


