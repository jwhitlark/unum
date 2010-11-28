;   Copyright (c) Jason Whitlark. 2009 All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


;   NOTE: Current code is very wasteful, (new connection every time), but works.
;   You'll need something like (def brk (create-simple-broker)) before you call send-msg and read-msg, or you'll need a separate activemq instance running.

(ns org.unum.mq
  (:use org.unum.net)
  (:use [clojure.contrib.logging])
  (:use clojure.contrib.import-static)

  (:import (org.apache.activemq.broker BrokerService)
	   (org.apache.activemq ActiveMQConnectionFactory)
	   (javax.jms MessageListener))
)

(import-static javax.jms.Session AUTO_ACKNOWLEDGE)

;; ========== State ==========

(def standard-bridge-port "61615")
(def local-broker (ref nil))
(def local-connection (ref nil))
(def local-session (ref nil))

;; ========== Broker API ==========

(defn add-connections-to-broker [brk connections]
  (if-not (nil? connections)
    (dorun (map #(.addConnector brk %) connections))))

;;"multicast://default") ;"static://"+"tcp://somehost:61616");

(defn add-bridge-connection-to-broker
  ([brk connection] (add-bridge-connection-to-broker brk "bridge" true true false connection))
  ([brk name duplex conduit-subscriptions decrease-priority connection]
     (if-not (nil? connection)
       (doto (.addNetworkConnector brk connection)
	 (.setName name)
	 (.setDuplex duplex)
	 (.setConduitSubscriptions conduit-subscriptions)
	 (.setDecreaseNetworkConsumerPriority decrease-priority)))))

(defn create-standard-broker [bname use-shutdown-hook net-connector connectors]
  (doto (BrokerService.)
    (.setBrokerName bname)
    (.setUseShutdownHook use-shutdown-hook)
    (add-bridge-connection-to-broker net-connector)
    (add-connections-to-broker connectors)
    (.start)))

;example call
;; (create-standard-broker "fred" false "static://(tcp://192.168.1.8:61615)"
;; 			["tcp://192.168.1.7:61615"
;; 			 "tcp://localhost:61616"
;; 			 "stomp://localhost:61617"
;; 			 "xmpp://localhost:61618"])

(defn init-local-broker []
  "Create a broker that listens on the unum address for external
connections, and allows openwire and stomp connections from localhost.
Network connections to other unum instances are added separately
later."
  (dosync (ref-set local-broker (create-standard-broker "Unum" false nil
							[(str "tcp://" (my-unum-ip-address) ":61615") ;TODO: ensure unum address exists!
							 "tcp://localhost:61616"
							 "stomp://localhost:61617"]))))

(defn close-local-broker []
  "Shut down activeMQ."
  (do
    (.stop @local-broker)
    (dosync
     (ref-set local-broker nil))))

;; ========== Client API ==========

(defn create-connection
  ([url] (create-connection "system" "manager" url))
  ([user password url]  (let [connectionFactory (ActiveMQConnectionFactory. user password url)
			       connection (.createConnection connectionFactory)]
			   connection)))

(defn init-local-connection []
  (dosync (ref-set local-connection (create-connection "tcp://localhost:61616"))))


(defn create-session
  ([connection] (create-session false AUTO_ACKNOWLEDGE connection))
  ([transacted ack-policy connection]
     (do (.start connection)
	 (let [session (.createSession connection transacted ack-policy)]
	   session))))

(defn init-local-session []
  (dosync (ref-set local-session (create-session @local-connection))))


(defn create-consumer [session queue-name]
  (let [destination (.createQueue session queue-name)
	consumer (.createConsumer session destination)]
    consumer))

(defn create-producer [session queue-name]
  (let [destination (.createQueue session queue-name)
	producer (.createProducer session destination)]
    producer))

(defn send-msg
  ([queue-name message-text]
     (let [producer (create-producer @local-session queue-name)
	   message (.createTextMessage @local-session message-text)]
       (.send producer message)))

  ([user password url queue-name message-text]
     (with-open [connection (create-connection user password url)
		 session (create-session connection)
		 producer (create-producer session queue-name)]
       (let [message (.createTextMessage session message-text)]
	 (.send producer message)))))


(defn read-msg
  ([queue-name]
     (read-msg queue-name 1000))

  ([queue-name timeout-in-ms]
     (let [consumer (create-consumer @local-session queue-name)]
       (.getText (.receive consumer timeout-in-ms)))) ;FIXME: needs to be able to handle other types of messages.

  ([user password url queue-name timeout-in-ms]
     (with-open [connection (create-connection user password url)
		 session (create-session connection)
		 consumer (create-consumer session queue-name)]
       (.getText (.receive consumer timeout-in-ms))))) ; .getText Only works on TextMessage

(defn create-msg-handler
"fn should be a function that expects a Message object of some type.  A simple example would be #(println (.getText %)) with will print the text of a text message, and crash on anything else ;-)"
  ([queue-name fn]
     (let [listener (proxy [MessageListener] []
		       (onMessage [msg]
				  (fn msg)))
	   consumer (create-consumer @local-session queue-name)]
       (.setMessageListener consumer listener)))

  ([user password url queue-name fn]
     (let [lstn (proxy [MessageListener] []
		  (onMessage [msg]
			     (fn msg)))
	   conn (create-connection user password url)
	   sess (create-session conn)
	   consum (create-consumer sess queue-name)]
       (.setMessageListener consum lstn))))


(defn initialize-local-message-queue []
  (do
    (init-local-broker)
    (init-local-connection)
    (init-local-session)))

(defn create-test-message-queue-and-handler []
  (create-msg-handler (str "unum." hostname ".testing") #(debug %1)))

(defn create-mq-admin-queue-and-handler []
  (create-msg-handler (str "unum." hostname ".mq.request-connection")
		      #(let [target-ip (.getText %1)
			     target-connection-string (str "static://(tcp://" target-ip ":" standard-bridge-port ")")]
			 (if-not (= target-ip my-unum-ip-address)
			   (add-bridge-connection-to-broker @local-broker target-connection-string)))))

(defn unum-announce-callback [msg]
  (let [target-connection-string (str "static://(tcp://" (:source-addr msg) ":" standard-bridge-port ")")
	target-hostname (.substring (:message msg) (count announce-msg))]
    (if-not (= target-hostname hostname)
      (do (add-bridge-connection-to-broker @local-broker target-connection-string)
	  (send-msg (str "unum." target-hostname ".mq.request-connection") (my-unum-ip-address))))))

(defn listen-and-add-transport-on-receipt-of-unum-announce-udp []
  (listen-for-unum-announce-udp unum-announce-callback))

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
