package server.audit;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ConfigEntry;
import server.User;
import server.dao.ConfigEntryDAO;
import server.dao.DAOFactory;
import server.data.ObjectSystemData;
import server.exceptions.CinnamonConfigurationException;
import server.lifecycle.LifeCycle;
import server.lifecycle.LifeCycleState;
import utils.HibernateSession;
import utils.ParamParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 *
 */
public class AuditService {

    Logger log = LoggerFactory.getLogger(this.getClass());
    
    Connection connection;
    
    public AuditService(Connection connection) {
        this.connection = connection;
        log.debug("audit-connection: "+connection);
    }

    public void insertLogEvent(LogEvent event) {
        try {
            if(connection == null){
                log.debug("no audit connection available");
                return;
            }
            if(event == null){
                log.debug("null event - filtered.");
                return;
            }
            PreparedStatement stmt =
                    connection.prepareStatement("INSERT INTO audit_log (repository, hibernate_id, username," +
                            " user_id, date_created, field_name, " +
                            " old_value, old_value_name, " +
                            " new_value, new_value_name, " +
                            " object_name, metadata, class_name, log_message) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            stmt.setString(1, event.repositoryName);
            stmt.setString(2, event.getHibernateId());            
            stmt.setString(3, event.getUsername());
            stmt.setString(4, event.getUserId());
            stmt.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
            stmt.setString(6, event.fieldName);
            stmt.setString(7, event.oldValue);
            stmt.setString(8, event.oldValueName);
            stmt.setString(9, event.newValue);
            stmt.setString(10, event.newValueName);
            stmt.setString(11, event.objectName);
            stmt.setString(12, event.metadata);
            stmt.setString(13, event.className);
            stmt.setString(14, event.logMessage);
            int rows = stmt.executeUpdate();
            if(rows != 1){
                log.error("Insert of audit entry changed more than one row.");
            }
            stmt.close();
            log.debug("inserted log event into database");            
        } catch (Exception e) {    
            log.debug("Failed to update audit table.",e);
            throw new RuntimeException(e);
        }
    }
    
    public LogEvent createLogEvent(ObjectSystemData osd, User user, LifeCycleState oldState, LifeCycleState nextState){
        if(oldState == null && nextState == null){
            log.debug("both lifecycle states are null - nothing to do.");
            return null;
        }
        // This would be much simpler with Groovy and ?. / ?:
        Long oldLc = 0L;
        Long newLc = 0L;        
        Long oldStateId = 0L;
        Long newStateId = 0L;
        if(oldState != null){
            oldStateId = oldState.getId();
            oldLc = oldState.getLifeCycle().getId();
        }
        if(nextState != null){
            newStateId = nextState.getLifeCycle().getId();
            newLc = nextState.getId();
        }        
        if(filterLogMessageByLifecycle(oldLc, newLc, oldStateId, newStateId)){
            log.debug("Lifecycle log message has been filtered.");
            return null;
        }
        
        LogEvent event = new LogEvent(osd);
        event.setRepositoryName(HibernateSession.getLocalRepositoryName());
        event.setFieldName("state");
        event.setUser(user);
        if(oldState != null){
            event.setOldValue(String.valueOf(oldState.getId()));
            event.setOldValueName(oldState.getName());
        }
        String lifecycleName = "";
        if(nextState != null){
            event.setNewValue(String.valueOf(nextState.getId()));
            event.setNewValueName(nextState.getName());
            LifeCycle nextLc = nextState.getLifeCycle();
           
            if(nextLc != null){ // contrary to IntelliJ's opinion, this *may* be null in a mis-configured repo.
                lifecycleName = nextLc.getName();
            }
        }        
        event.setLogMessage("lifecycle-state-changed");
        event.setMetadata("<meta>\n<path>"+osd.getParent().fetchPath()+"</path>\n"+
                "<lifecycleName>"+lifecycleName+"</lifecycleName>\n"+
                "</meta>");
        return event;
    }

    /**
     * Create log event for new osd.
     * @param osd
     * @return
     */
    public LogEvent createLogEvent(ObjectSystemData osd, User user, String fieldName, String oldVal, String newVal, String logMessage){
        LogEvent event = new LogEvent(osd);
        event.setRepositoryName(HibernateSession.getLocalRepositoryName());
        event.setFieldName(fieldName);
        event.setUser(user);
        event.setLogMessage(logMessage);
        if(oldVal != null){
            event.setOldValue(oldVal);
        }
        if(newVal != null){
            event.setNewValue(newVal);
        }
        event.setMetadata("<meta>\n<path>"+osd.getParent().fetchPath()+"</path>\n"+
                "</meta>");
        return event;
    }
    
    /**
     * Determine whether a log message should be filtered, depending on the ChangeTrigger's configuration.
     * A node "logEverything" with content "true" will approve all log messages.
     * @param oldLifecycleId the old lifecycle's id
     * @param newLifecycleId the new lifecycle's id
     * @param oldStateId the old lifecycle state id , may be null or empty
     * @param newStateId the new lifecycle state id, may be null or empty
     * @return true if the log message should be filtered (not stored in the database), false otherwise.
     */
    Boolean filterLogMessageByLifecycle(Long oldLifecycleId, Long newLifecycleId, Long oldStateId, Long newStateId ){
        DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
        ConfigEntryDAO ceDao = daoFactory.getConfigEntryDAO(HibernateSession.getLocalEntityManager());
        ConfigEntry config = ceDao.findByName("audit.trail.filter");
        if(config == null){
            throw new CinnamonConfigurationException("Configuration entry audit.trail.filter is missing.");
        }
        // only log configured changes:        
        Document filterConfig = ParamParser.parseXmlToDocument(config.getConfig());
        Node logAll = filterConfig.selectSingleNode("//logEverything[text()='true']");
        if(logAll != null){
            log.debug("Found logEverything directive: approve log message");
            return false;
        }
        
        Node lifecycle = filterConfig.selectSingleNode("//lifecycles/lifecycle[@id='"+oldLifecycleId+"' or @id='"+newLifecycleId+"']");
        if(lifecycle == null){
            log.debug("Lifecycle is not configured for logging: deny logging.");
            return true;
        }
        Node oldState = lifecycle.selectSingleNode("stateId[text()='"+oldStateId+"']");
        Node newState = lifecycle.selectSingleNode("stateId[text()='"+newStateId+"']");
        if(oldState != null || newState != null){
            log.debug("Lifecycle state change will be logged.");
            return false;
        }
        log.debug("Could not find a reason to approve this log event.");
        return true;
    }
}
