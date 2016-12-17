package com.steinschreiber.aws.mqlambda;

/**
 * Created by mdevhs on 11/27/16.
 */
public class MqLambdaEvent {

    public final static String STRUCID = "TM  ";
    public final static int VERSION = 1;

    private String strucId;
    private Integer version;
    private String qName = "";
    private String processName = "";
    private String triggerData = "";
    private Integer applType;
    private String applId = "";
    private String envData = "";
    private String userData = "";

    public String getStrucId() {
        return strucId;
    }

    public void setStrucId(String strucId) {
        this.strucId = strucId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getqName() {
        return qName;
    }

    public void setqName(String qName) {
        this.qName = qName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(String triggerData) {
        this.triggerData = triggerData;
    }

    public Integer getApplType() {
        return applType;
    }

    public void setApplType(Integer applType) {
        this.applType = applType;
    }

    public String getApplId() {
        return applId;
    }

    public void setApplId(String applId) {
        this.applId = applId;
    }

    public String getEnvData() {
        return envData;
    }

    public void setEnvData(String envData) {
        this.envData = envData;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return "MqLambdaEvent{" +
                "strucId='" + strucId + '\'' +
                ", version=" + version +
                ", qName='" + qName + '\'' +
                ", processName='" + processName + '\'' +
                ", triggerData='" + triggerData + '\'' +
                ", applType=" + applType +
                ", applId='" + applId + '\'' +
                ", envData='" + envData + '\'' +
                ", userData='" + userData + '\'' +
                '}';
    }
}
