package com.steinschreiber.aws.samples;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.steinschreiber.aws.mqlambda.AbstractMessageProcessor;
import com.steinschreiber.aws.mqlambda.MqLambdaEvent;
import org.apache.log4j.Logger;

import javax.jms.Message;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Created by mdevhs on 12/1/16.
 */
public class SampleMySqlMessageProcessor extends AbstractMessageProcessor {

    private static final Logger LOG = Logger.getLogger(SampleMySqlMessageProcessor.class.getName());
    private static final String configEnvPrefix = "MQMYSQLSAMPLE_";
    private static final Config config = new Config(configEnvPrefix);

    static {
        LOG.debug(config.toString());
    }

    private MqLambdaEvent event;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();

    private final static String deleteOrder = "DELETE FROM orders WHERE orderNumber = ?";
    private final static String insertOrder =
            "INSERT INTO orders " +
                    "(orderNumber, orderDate, requiredDate, shippedDate, status, comments, customerNumber) " +
                    "VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?)";
    private final static String insertOrderDetais =
            "INSERT INTO orderdetails" +
                    "(orderNumber, productCode, quantityOrdered, priceEach, orderLineNumber) " +
                    "VALUES " +
                    "(?, ?, ?, ?, ?)";

    private Connection connection;
    private PreparedStatement deleteOrderStmt;
    private PreparedStatement insertOrderStmt;
    private PreparedStatement insertOrderDetailsStmt;

    private long msgCount = 0;

    @Override
    public void init(MqLambdaEvent event) throws Exception {
        LOG.trace("init entry");

        this.event = event;
        connection = getConnection(config);
        deleteOrderStmt = connection.prepareStatement(deleteOrder);
        insertOrderStmt = connection.prepareStatement(insertOrder);
        insertOrderDetailsStmt = connection.prepareStatement(insertOrderDetais);
        msgCount = 0;

        LOG.trace("init exit");
    }

    @Override
    public void process(Message msg) throws Exception {
        LOG.trace("process entry");

        Order order = gson.fromJson(msg.getBody(String.class), Order.class);
        LOG.trace(order.toString());

        deleteOrderStmt.setInt(1, order.getOrderNumber());
        deleteOrderStmt.executeUpdate();

        insertOrderStmt.setInt(1, order.getOrderNumber());
        insertOrderStmt.setDate(2, order.getOrderDate());
        insertOrderStmt.setDate(3, order.getRequiredDate());
        insertOrderStmt.setDate(4, order.getShippedDate());
        insertOrderStmt.setString(5, order.getStatus());
        insertOrderStmt.setString(6, order.getComments());
        insertOrderStmt.setInt(7, order.getCustomerNumber());
        insertOrderStmt.executeUpdate();

        for (OrderDetails od : order.getOrderDetails()) {
            insertOrderDetailsStmt.setInt(1, order.getOrderNumber());
            insertOrderDetailsStmt.setString(2, od.getProductCode());
            insertOrderDetailsStmt.setInt(3, od.getQuantityOrdered());
            insertOrderDetailsStmt.setBigDecimal(4, od.getPriceEach());
            insertOrderDetailsStmt.setInt(5, od.getOrderLineNumber());
            insertOrderDetailsStmt.addBatch();
        }
        insertOrderDetailsStmt.executeBatch();

        msgCount++;

        LOG.trace("process exit");
    }

    @Override
    public void commit() throws Exception {
        LOG.trace("commit entry");
        connection.commit();
        LOG.trace("commit exit");
    }

    @Override
    public void finish() throws Exception {
        LOG.trace("finish entry");

        deleteOrderStmt.close();
        insertOrderStmt.close();
        insertOrderDetailsStmt.close();
        connection.close();

        LOG.debug(String.format("%d messages processed", msgCount));
        LOG.trace("finish exit");
    }

    private static Connection getConnection(Config config) throws SQLException {
        LOG.trace("getConnection entry");

        String dbUser = config.getDbUser();
        String dbPassword = config.getDbPassword();
        String dbHost = config.getDbHost();
        String dbName = config.getDbName();

        if (dbUser == null || dbPassword == null || dbHost == null || dbName == null) {
            LOG.error("database connection not fully configured");
            throw new IllegalArgumentException("database misconfiguration");
        }

        Connection conn = null;
        Properties connProps = new Properties();
        connProps.put("user", dbUser);
        connProps.put("password", dbPassword);
        String connUrl = "jdbc:mysql://" + dbHost + ":" + config.getDbPort() + "/" + dbName;

        LOG.debug(String.format("DriverManager.getConnection with %s", connUrl));
        conn = DriverManager.getConnection(connUrl, connProps);

        conn.setAutoCommit(false);

        LOG.trace("getConnection exit");

        return conn;
    }

    private static class Config {

        private String dbUser;
        private String dbPassword;
        private String dbHost;
        private String dbName = "";
        private int dbPort = 3306;

        public String getDbUser() {
            return dbUser;
        }

        public String getDbPassword() {
            return dbPassword;
        }

        public String getDbHost() {
            return dbHost;
        }

        public String getDbName() {
            return dbName;
        }

        public int getDbPort() {
            return dbPort;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "dbUser='" + dbUser + '\'' +
                    ", dbPassword='" + "********" + '\'' +
                    ", dbHost='" + dbHost + '\'' +
                    ", dbName='" + dbName + '\'' +
                    ", dbPort=" + dbPort +
                    '}';
        }

        private Config(String pref) {
            Map<String, String> env = System.getenv();

            Optional.ofNullable(env.get(pref + "DBUSER")).ifPresent(v -> dbUser = v);
            Optional.ofNullable(env.get(pref + "DBPASSWORD")).ifPresent(v -> dbPassword = v);
            Optional.ofNullable(env.get(pref + "DBHOST")).ifPresent(v -> dbHost = v);
            Optional.ofNullable(env.get(pref + "DBPORT")).ifPresent(v -> dbPort = Integer.valueOf(v));
            Optional.ofNullable(env.get(pref + "DBNAME")).ifPresent(v -> dbName = v);
        }
    }
}