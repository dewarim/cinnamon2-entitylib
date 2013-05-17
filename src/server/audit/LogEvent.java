package server.audit;

import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Metaset;
import server.User;
import server.dao.DAOFactory;
import server.data.ObjectSystemData;
import server.global.Constants;
import utils.ParamParser;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 */
public class LogEvent {

    Logger log = LoggerFactory.getLogger(this.getClass());
    
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

    /**
     * Store this event in the object's action_log metaset.
     */
    public void saveToActionLog(){
        DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
        Metaset actionLog = osd.fetchMetaset(Constants.METASET_ACTION_LOG, false);
        if(actionLog == null){
            log.debug("Object "+osd.myId()+" does not have an action_log metaset - do not log.");
            return;
        }
        Date dateTime = new Date(); 
        Document meta = ParamParser.parseXmlToDocument(actionLog.getContent());
        Element root = meta.getRootElement();
        Element event = root.addElement("event");
        event.addElement("user").addText(String.valueOf(user.getId()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        event.addElement("dateTime").addText(sdf.format(dateTime));
        event.addElement("timeStamp").addText(String.valueOf(dateTime.getTime()));
        nullSafe(event, "fieldName", fieldName);
        nullSafe(event, "oldValue", oldValue);
        nullSafe(event, "oldValueName", oldValueName);
        nullSafe(event, "newValue", newValue);
        nullSafe(event, "newValueName", newValueName );
        nullSafe(event, "eventMetadata", metadata);
        nullSafe(event, "logMessage", logMessage);
        log.debug("Add action_log entry:\n" + meta.asXML());
        actionLog.setContent(meta.asXML());
    }
    
    Element nullSafe(Element root, String name, String data){
        Element newOne = root.addElement(name);
        if(data != null && data.length() > 0){
            newOne.addText(data);
        }
        return newOne;
    }
}
