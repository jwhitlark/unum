;   Copyright (c) Jason Whitlark. 2010 All rights reserved.
;
;   NOTE: Current code is very wasteful, (new connection every time), but works.
;   You'll need something like (def brk (create-simple-broker)) before you call send-msg and read-msg, or you'll need a separate activemq instance running.

(ns org.unum.mq
  (:use org.unum.net)
  (:use clojure.contrib.import-static)

  (:import (org.apache.activemq.broker BrokerService)
	   (org.apache.activemq ActiveMQConnectionFactory)
	   (javax.jms MessageListener))
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


(defn configure-log-to-console []
  "Will pipe logs to console, (where swank was started, if you're
using it.  Stops log4j from complaining about activemq; we will need
to do better in the future."
  (org.apache.log4j.BasicConfigurator/configure))

(defn create-simple-broker []
  (let [brk (BrokerService.)]
    (.setBrokerName brk "fred")
    (.setUseShutdownHook brk false)
    (.addNetworkConnector brk "static://(tcp://192.168.1.8:61615)") ;;"multicast://default") ;"static://"+"tcp://somehost:61616");
    (.addConnector brk "tcp://192.168.1.7:61616") ;openwire (activeMQ native fmt)
    (.addConnector brk "stomp://localhost:61617")
    (.addConnector brk "xmpp://localhost:61618")
    (.start brk)
    brk))


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

(defn read-msg [user password url queue-name]
  (let [timeout 1000] ; In milli-seconds.
    (with-open [connection (create-connection user password url)
		session (create-session connection)
		consumer (create-consumer session queue-name)]
      (.getText (.receive consumer timeout))))) ; .getText Only works on TextMessage

(defn create-msg-handler [user password url queue-name fn]
"fn should be a function that expects a Message object of some type.  A simple example would be #(println (.getText %)) with will print the text of a text message, and crash on anything else ;-)"
  (let [lstn (proxy [MessageListener] []
		       (onMessage [msg]
				  (fn msg)))
	conn (create-connection user password url)
	sess (create-session conn)
	consum (create-consumer sess queue-name)]
    (.setMessageListener consum lstn)))


; ----- Embedded web console -----
; TODO !?!
  ;; These are the imports for the embedded web console, if I ever figure it out...
  ;; (org.mortbay.jetty Server Connector)
  ;; (org.mortbay.jetty.nio SelectChannelConnector)
  ;; (org.mortbay.jetty.webapp WebAppContext))


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

;; ----- Embedding the web console in an application -----
;; package org.apache.activemq.web.tool;

;; import org.eclipse.jetty.server.Connector;
;; import org.eclipse.jetty.server.Server;
;; import org.eclipse.jetty.server.nio.SelectChannelConnector;
;; import org.eclipse.jetty.webapp.WebAppContext;

;; /**
;; * A simple bootstrap class for starting Jetty in your IDE using the local web
;; * application.
;; *
;; * @version $Revision$
;; */
;; public final class Main {

;; public static final int PORT = 8080;

;; public static final String WEBAPP_DIR = "src/main/webapp";

;; public static final String WEBAPP_CTX = "/";

;; private Main() {
;; }

;; public static void main(String[] args) throws Exception {
;; // now lets start the web server
;; int port = PORT;
;; if (args.length > 0) {
;; String text = args[0];
;; port = Integer.parseInt(text);
;; }
;; System.out.println("Starting Web Server on port: " + port);
;; Server server = new Server();
;; SelectChannelConnector connector = new SelectChannelConnector();
;; connector.setPort(port);
;; connector.setServer(server);
;; WebAppContext context = new WebAppContext();

;; context.setResourceBase(WEBAPP_DIR);
;; context.setContextPath(WEBAPP_CTX);
;; context.setServer(server);
;; server.setHandler(context);
;; server.setConnectors(new Connector[] {
;; connector
;; });
;; server.start();

;; System.out.println();
;; System.out.println("==============================================================================");
;; System.out.println("Started the ActiveMQ Console: point your web browser at http://localhost:" + port + "/");
;; System.out.println("==============================================================================");
;; System.out.println();
;; }
;; }
