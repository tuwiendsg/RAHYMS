package at.ac.tuwien.dsg.smartcom.broker.impl;

import at.ac.tuwien.dsg.smartcom.broker.CancelableListener;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.broker.utils.BrokerErrorUtils;
import at.ac.tuwien.dsg.smartcom.exception.CommunicationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Apache ActiveMQ Message Broker
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class ApacheActiveMQMessageBroker implements MessageBroker {
    private static final Logger log = LoggerFactory.getLogger(ApacheActiveMQMessageBroker.class);
    private static final String requestQueuePrefix = "SmartCom.request.";
    private static final String taskQueuePrefix = "SmartCom.task.";
    public static final int CONNECTIONS = 50;

    private Connection connection;
    private Session session;

    private ThreadLocal<Session> localSession;
    private ThreadLocal<MessageProducer> localProducer;
    private List<Session> sessions;

    private Queue inputQueue;
    private Queue controlQueue;
    private Queue authQueue;
    private Queue messageInfoQueue;
    private Queue metricsQueue;
    private Queue logQueue;

    private Connection[] connections = new Connection[CONNECTIONS];

    public ApacheActiveMQMessageBroker(String host, int port) throws CommunicationException {
        setUp(host, port);
    }

    /**
     * Initialize the Apache ActiveMQ Message Broker. It connects to the ActiveMQ instance
     * and creates the queues that are used by the various sessions
     *
     * @param host address of the ActiveMQ instance
     * @param port of the ActiveMQ instance
     * @throws CommunicationException
     */
    private void setUp(String host, int port) throws CommunicationException {
        try {
            log.debug("Initialising Apache ActiveMQ Message Broker!");

            //ConnectionFactory for the Apache ActiveMQ instance
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);

            //Connect to the instance
            connection = connectionFactory.createConnection();
            connection.start();

            for (int i = 0; i < connections.length; i++) {
                connections[i] = connectionFactory.createConnection();
                connections[i].start();
            }

            //since one connection per thread is allowed by AMQ
            sessions = Collections.synchronizedList(new ArrayList<Session>());

            //Sessions for this broker
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            //see down
            setUpDestinations(session);

            localSession = new ThreadLocal<>();
            localProducer = new ThreadLocal<>();
        } catch (JMSException e) {
            throw BrokerErrorUtils.createBrokerException(e);
        }
    }

    private void setUpDestinations(Session session) throws JMSException {
        //these will be shared by different threads, as the name of the queue is unique.
    	inputQueue = session.createQueue("SmartCom.input");
        controlQueue = session.createQueue("SmartCom.control");
        authQueue = session.createQueue("SmartCom.auth");
        messageInfoQueue = session.createQueue("SmartCom.messageInfo");
        metricsQueue = session.createQueue("SmartCom.metrics");
        logQueue = session.createQueue("SmartCom.log");
    }

    public void cleanUp() throws CommunicationException {
        try {
            for (Session session : sessions) {
                session.close();
            }

            session.close();
            connection.close();

            for (Connection connection1 : connections) {
                connection1.close();
            }
        } catch (JMSException e) {
            throw BrokerErrorUtils.createBrokerException(e);
        }
    }

    @Override
    public Message receiveInput() {
        return receiveMessage(inputQueue);
    }

    @Override
    public CancelableListener registerInputListener(final MessageListener listener) {
        return setListener(listener, inputQueue);
    }

    @Override
    public void publishInput(Message message) {
        sendMessage(message, inputQueue);
    }

    /**
     * Receive a message from a destination. Invoked by the consumer on the queue. Blocks until a msg is available.
     * @param destination of the message
     * @return the received message
     */
    private Message receiveMessage(Destination destination) {
        log.trace("Waiting for message in queue {}", destination);
        MessageConsumer consumer = null;
        try {
            initLocalSessionAndProducer();

            consumer = localSession.get().createConsumer(destination);

            Message msg =  (Message) ((ObjectMessage) consumer.receive()).getObject();

            consumer.close();

            return msg;
        } catch (JMSException e) {
            log.error("Error while receiving "+destination.toString(), e);
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (JMSException e1) {
                    throw BrokerErrorUtils.createRuntimeBrokerException(e);
                }
            }
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    /**
     * registers a listener on the destination. This should be the preferred way to receive messages from a queue, rather that calling receiveMessage(), 
     * also because it is cancelable,
     * @param listener that has to be registered
     * @param destination for the listener
     */
    private CancelableListener setListener(final MessageListener listener, final Destination destination) {
        try {
            log.trace("Setting listener for destination {}", destination);
            MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(new javax.jms.MessageListener() {
                @Override
                public void onMessage(javax.jms.Message message) {
                    try {
                        listener.onMessage((Message) ((ObjectMessage) message).getObject());
                    } catch (JMSException e) {
                        log.error("Error in message listener for "+destination.toString(), e);
                        throw BrokerErrorUtils.createRuntimeBrokerException(e);
                    }
                }
            });
            //differently than in receiveMessage, we now do not close the consumer, but rather return a cancellable listener.
            return new CancelableListenerImpl(consumer);
        } catch (JMSException e) {
            log.error("Error while setting "+destination.toString()+" listener", e);
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    /**
     * Send a message to a specific destination. It uses thread local sessions and
     * producers for the sending of messages and creates them if they are not present.
     *
     * @param message that should be sent
     * @param destination of the message
     */
    private void sendMessage(final Message message, final Destination destination) {
        log.trace("Sending message {} to queue {}", message, destination);
        try {
            initLocalSessionAndProducer();

            ObjectMessage msg = localSession.get().createObjectMessage(message);
            localProducer.get().send(destination, msg);
        } catch (JMSException e) {
            log.error("Error while sending " + destination.toString() + " message", e);
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    /**
     * initialize thread local session and producer if there are no such
     * @throws JMSException
     */
    private void initLocalSessionAndProducer() throws JMSException {
    	//methods like createSession and createProducer take the invoking thread, and assume it when creating the new sesion and producer instances.
        if (localSession.get() == null) {
            Connection connection = connections[((int) (Thread.currentThread().getId() % connections.length))];
            localSession.set(connection.createSession(false, Session.AUTO_ACKNOWLEDGE));
            sessions.add(localSession.get());

            MessageProducer producer = localSession.get().createProducer(null);
            //AMQ will automatically persist messages UNTIL delivered. This does not store msgs permanently.
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            localProducer.set(producer);
        }
    }

    @Override
    public Message receiveRequests(Identifier id) {
        return receiveMessage(createDestination(requestQueuePrefix, id));
    }

    private Destination createDestination(String prefix, Identifier id) {
        try {
            return session.createQueue(prefix + id.getId());
        } catch (JMSException e) {
            log.error("Error while sending message to queue '{}' with id {} message", prefix, id, e);
            throw BrokerErrorUtils.createRuntimeBrokerException(e);
        }
    }

    @Override
    public CancelableListener registerRequestListener(Identifier id, MessageListener listener) {
        return setListener(listener, createDestination(requestQueuePrefix, id));
    }

    @Override
    public void publishRequest(Identifier id, Message message) {
        sendMessage(message, createDestination(requestQueuePrefix, id));
    }

    @Override
    public Message receiveOutput(Identifier id) {
        return receiveMessage(createDestination(taskQueuePrefix, id));
    }

    @Override
    public CancelableListener registerOutputListener(Identifier id, MessageListener listener) {
        return setListener(listener, createDestination(taskQueuePrefix, id));
    }

    @Override
    public void publishOutput(Identifier id, Message message) {
        sendMessage(message, createDestination(taskQueuePrefix, id));
    }

    @Override
    public void publishControl(Message message) {
        sendMessage(message, controlQueue);
    }

    @Override
    public Message receiveControl() {
        return receiveMessage(controlQueue);
    }

    @Override
    public CancelableListener registerControlListener(MessageListener listener) {
        return setListener(listener, controlQueue);
    }

    @Override
    public void publishAuthRequest(Message message) {
        sendMessage(message, authQueue);
    }

    @Override
    public Message receiveAuthRequest() {
        return receiveMessage(authQueue);
    }

    @Override
    public CancelableListener registerAuthListener(MessageListener listener) {
        return setListener(listener, authQueue);
    }

    @Override
    public void publishMessageInfoRequest(Message message) {
        sendMessage(message, messageInfoQueue);
    }

    @Override
    public Message receiveMessageInfoRequest() {
        return receiveMessage(messageInfoQueue);
    }

    @Override
    public CancelableListener registerMessageInfoListener(MessageListener listener) {
        return setListener(listener, messageInfoQueue);
    }

    @Override
    public void publishMetricsRequest(Message message) {
        sendMessage(message, metricsQueue);
    }

    @Override
    public Message receiveMetricsRequest() {
        return receiveMessage(metricsQueue);
    }

    @Override
    public CancelableListener registerMetricsListener(MessageListener listener) {
        return setListener(listener, metricsQueue);
    }

    @Override
    public void publishLog(Message message) {
        sendMessage(message, logQueue);
    }

    @Override
    public Message receiveLog() {
        return receiveMessage(logQueue);
    }

    @Override
    public CancelableListener registerLogListener(MessageListener listener) {
        return setListener(listener, logQueue);
    }

    private class CancelableListenerImpl implements CancelableListener {

        private final MessageConsumer consumer;

        private CancelableListenerImpl(MessageConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void cancel() {
            try {
                this.consumer.close();
            } catch (JMSException e) {
                log.error("Could not close consumer", e);
            }
        }
    }
}
