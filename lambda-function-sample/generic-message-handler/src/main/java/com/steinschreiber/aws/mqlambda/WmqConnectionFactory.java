package com.steinschreiber.aws.mqlambda;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.apache.log4j.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * Created by mdevhs on 11/27/16.
 */
public class WmqConnectionFactory {

    private static final Logger LOG = Logger.getLogger(WmqConnectionFactory.class.getName());

    public static ConnectionFactory from(MqLambdaConfig config) {

        LOG.trace("from entry");
        LOG.debug(config.toString());

        JmsConnectionFactory cf = null;

        try {
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);

            cf = ff.createConnectionFactory();

            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, config.getHost());
            cf.setIntProperty(WMQConstants.WMQ_PORT, config.getPort());
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, config.getChannel());
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, config.getQmgr());
            if (config.getUserId() != null) {
                cf.setStringProperty(WMQConstants.USERID, config.getUserId());
                cf.setStringProperty(WMQConstants.PASSWORD, config.getPassword());
                cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            }
        } catch (JMSException e) {
            LOG.error(e.toString());
            throw new RuntimeException(e);
        }

        LOG.trace("from exit");
        return cf;
    }
}
