package com.steinschreiber.aws.mqlambda;

import java.util.Map;
import java.util.Optional;

/**
 * Created by mdevhs on 11/27/16.
 */
public class MqLambdaConfig {

    private String host = "";
    private Integer port = 1414;
    private String channel = "SYSTEM.DEF.SVRCONN";
    private String qmgr = "";
    private String userId;
    private String password;

    private int batchSize = 100;
    private int batchTimeout = 1000;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getQmgr() {
        return qmgr;
    }

    public void setQmgr(String qmgr) {
        this.qmgr = qmgr;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchTimeout() {
        return batchTimeout;
    }

    public void setBatchTimeout(int batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    @Override
    public String toString() {
        return "MqLambdaConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", channel='" + channel + '\'' +
                ", qmgr='" + qmgr + '\'' +
                ", userId='" + userId + '\'' +
                ", password='" + "********" + '\'' +
                ", batchSize=" + batchSize +
                ", batchTimeout=" + batchTimeout +
                '}';
    }

    public static MqLambdaConfig fromEnvVariables(String pref) {
        MqLambdaConfig co = new MqLambdaConfig();
        Map<String, String> env = System.getenv();

        Optional.ofNullable(env.get(pref + "HOST")).ifPresent(v -> co.host = v);
        Optional.ofNullable(env.get(pref + "PORT")).ifPresent(v -> co.port = Integer.valueOf(v));
        Optional.ofNullable(env.get(pref + "CHANNEL")).ifPresent(v -> co.channel = v);
        Optional.ofNullable(env.get(pref + "QMGR")).ifPresent(v -> co.qmgr = v);
        Optional.ofNullable(env.get(pref + "USERID")).ifPresent(v -> co.userId = v);
        Optional.ofNullable(env.get(pref + "PASSWORD")).ifPresent(v -> co.password = v);
        Optional.ofNullable(env.get(pref + "BATCH_SIZE")).ifPresent(v -> co.batchSize = Integer.valueOf(v));
        Optional.ofNullable(env.get(pref + "BATCH_TIMEOUT")).ifPresent(v -> co.batchTimeout = Integer.valueOf(v));

        return co;
    }
}
