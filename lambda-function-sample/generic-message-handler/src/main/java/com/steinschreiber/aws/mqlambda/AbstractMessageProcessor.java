package com.steinschreiber.aws.mqlambda;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * Created by mdevhs on 11/27/16.
 */

public abstract class AbstractMessageProcessor {

    private static final Logger LOG = Logger.getLogger(AbstractMessageProcessor.class.getName());
    private static final String ConfigEnvPrefix = "MQLAMBDA_";
    protected static final MqLambdaConfig CONFIG = MqLambdaConfig.fromEnvVariables(ConfigEnvPrefix);
    private static ConnectionFactory connectionFactory = WmqConnectionFactory.from(CONFIG);

    public final void handler(MqLambdaEvent event, Context context) throws Exception {

        LOG.trace("handler entry");

        LOG.debug(event.toString());

        if (!MqLambdaEvent.STRUCID.equals(event.getStrucId()) || event.getVersion() == null)
            throw new IllegalArgumentException("invalid event object " + event.toString());

        if (event.getVersion() > MqLambdaEvent.VERSION)
            throw new IllegalArgumentException("incompatible event object version " + event.toString());

        init(event);

        try (JMSContext jmsContext = connectionFactory.createContext(Session.SESSION_TRANSACTED)) {

            LOG.trace("JMS context created");

            Queue queue = jmsContext.createQueue(event.getqName());

            LOG.trace("Queue created");

            try (JMSConsumer consumer = jmsContext.createConsumer(queue)) {

                LOG.trace("Consumer created");

                try {
                    int batch = 0;
                    boolean inTransaction = false;

                    while (context.getRemainingTimeInMillis() > 1000) {

                        Message msg = consumer.receive(Math.min(CONFIG.getBatchTimeout(),
                                context.getRemainingTimeInMillis() - 1000));

                        if (msg != null) {
                            LOG.trace("Msg received " + msg.toString());
                            if (msg instanceof TextMessage) {
                                LOG.trace(((TextMessage) msg).getText());
                            }
                            process(msg);
                            inTransaction = true;
                        }
                        if (inTransaction && (msg == null || ++batch >= CONFIG.getBatchSize())) {
                            commit();
                            jmsContext.commit();
                            inTransaction = false;
                            batch = 0;
                        }
                    }

                    if (inTransaction) {
                        commit();
                        jmsContext.commit();
                    }
                } finally {
                    finish();
                }
            }
        }
        catch (Exception e) {
            LOG.error(e.toString());
            throw e;
        }

        LOG.trace("handler exit");
    }

    protected abstract void init(MqLambdaEvent event) throws Exception;

    protected abstract void process(Message msg) throws Exception;

    protected abstract void commit() throws Exception;

    protected abstract void finish() throws Exception;
}