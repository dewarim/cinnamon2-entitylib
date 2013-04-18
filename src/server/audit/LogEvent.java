package server.audit;

import org.hibernate.Hibernate;
import server.User;
import server.data.ObjectSystemData;

/**
 * 
 */
public class LogEvent {
    
    String repositoryName;
    ObjectSystemData osd;
    String hibernateId;
    String className;
    User user;
    String eventType = "generic-log-event";
    String fieldName;
    String oldValue;
    String newValue;
    String oldValueName;
    String newValueName;
    String logMessage = "";
    String metadata = "<meta/>";
    String objectName = "";


    public LogEvent() {
    }

    public LogEvent(ObjectSystemData osd) {
        this.osd = osd;
        this.hibernateId = osd.myId().toString();
        this.className = Hibernate.getClass(osd).getName();
        this.objectName = osd.getName();
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public ObjectSystemData getOsd() {
        return osd;
    }

    public void setOsd(ObjectSystemData osd) {
        this.osd = osd;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValueName() {
        return oldValueName;
    }

    public void setOldValueName(String oldValueName) {
        this.oldValueName = oldValueName;
    }

    public String getNewValueName() {
        return newValueName;
    }

    public void setNewValueName(String newValueName) {
        this.newValueName = newValueName;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getHibernateId() {
        return hibernateId;
    }

    public void setHibernateId(String hibernateId) {
        this.hibernateId = hibernateId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getUsername(){
        if(user == null){
            return "";
        }
        return user.getName();
    }
    
    public String getUserId(){
        if(user == null){
            return "";
        }
        return String.valueOf(user.getId());
    }
}
