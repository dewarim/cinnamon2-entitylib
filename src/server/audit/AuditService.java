package server.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.User;
import server.data.ObjectSystemData;
import server.lifecycle.LifeCycle;
import server.lifecycle.LifeCycleState;
import utils.HibernateSession;

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
           
            if(nextLc != null){
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
    
}
