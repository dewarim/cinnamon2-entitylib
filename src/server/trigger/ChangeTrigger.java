package server.trigger;

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.dao.ChangeTriggerTypeDAO;
import server.dao.DAOFactory;
import server.global.Constants;
import utils.HibernateSession;
import utils.ParamParser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * A ChangeTrigger is a class which decouples a CinnamonMethod and a Trigger-class. Every time an API-method
 * of the CinnamonServer is invoked which is marked with CinnamonMethod@trigger=true, the server checks
 * if a trigger condition is defined and executes the Trigger-classes before and after invoking the command.
 * <br/>
 * <h2>Purpose of ChangeTriggers</h2>
 * Add transformation functions for specific object types, for example have a PDF-renderer create a new
 * PDF rendition every time an object of ObjectType "document" is changed.
 */
@Entity
@Table(name = "change_triggers")
public class ChangeTrigger implements Serializable {

    private static final long serialVersionUID = 1L;
    static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);

    @Transient
    transient Logger log = LoggerFactory.getLogger(this.getClass());

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    @ManyToOne
    @JoinColumn(name = "trigger_type_id",
            nullable = false)
    private ChangeTriggerType triggerType;

    @Column(name = "ranking",
            nullable = false)
    private Integer ranking = 1;

    @Column(name = "command",
            nullable = false)
    private String command;

    @Column(name = "active",
            nullable = false)
    private Boolean active = false;

    @Column(name = "pre_trigger",
            nullable = false)
    private Boolean preTrigger = false;

    @Column(name = "post_trigger",
            nullable = false)
    private Boolean postTrigger = false;

    /**
     * This change trigger will be run in its own transaction,
     * after the main work of handling the request is done.
     * It may still alter the output.
     * It has to refresh all database objects it wants to access.
     */
    @Column(name = "after_work",
            nullable = false)
    private Boolean afterWork = false;

    @Column(name = "config",
            length = Constants.METADATA_SIZE,
            nullable = false)
    @Type(type = "text")
    String config = "<config />";

    public ChangeTrigger() {
    }

    public ChangeTrigger(String command, ChangeTriggerType ctt) {
        triggerType = ctt;
    }

    public ChangeTrigger(Map<String, String> fields) {
        ranking = Integer.parseInt(fields.get("ranking"));
        command = fields.get("command");
        active = Boolean.parseBoolean(fields.get("active"));
        preTrigger = Boolean.parseBoolean(fields.get("pre_trigger"));
        postTrigger = Boolean.parseBoolean(fields.get("post_trigger"));
        setConfig(fields.get("config"));
        ChangeTriggerTypeDAO cttDao = daoFactory.getChangeTriggerTypeDAO(HibernateSession.getLocalEntityManager());
        triggerType = cttDao.get(fields.get("trigger_type_id"));
    }

    public ChangeTrigger(String command, ChangeTriggerType ctt, Integer ranking,
                         Boolean active, Boolean preTrigger, Boolean postTrigger, String config) {
        this(command, ctt, ranking, active, preTrigger, postTrigger);
        setConfig(config);
    }

    public ChangeTrigger(String command, ChangeTriggerType ctt, Integer ranking,
                         Boolean active, Boolean preTrigger, Boolean postTrigger) {
        triggerType = ctt;
        this.ranking = ranking;
        this.command = command;
        this.active = active;
        this.preTrigger = preTrigger;
        this.postTrigger = postTrigger;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    /**
     * @return the triggerType
     */
    public ChangeTriggerType getTriggerType() {
        return triggerType;
    }

    /**
     * @param triggerType the triggerType to set
     */
    public void setTriggerType(ChangeTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    /**
     * @return the ranking
     */
    public Integer getRanking() {
        return ranking;
    }

    /**
     * If there are several ChangeTriggers for a command, ranking allows you to sort them according
     * to their importance. Items of ranking 1 are executed before items of ranking 2.
     *
     * @param ranking the ranking to set
     */
    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return the preTrigger
     */
    public Boolean getPreTrigger() {
        return preTrigger;
    }

    /**
     * @param preTrigger the preTrigger to set
     */
    public void setPreTrigger(Boolean preTrigger) {
        this.preTrigger = preTrigger;
    }

    /**
     * @return the postTrigger
     */
    public Boolean getPostTrigger() {
        return postTrigger;
    }

    /**
     * @param postTrigger the postTrigger to set
     */
    public void setPostTrigger(Boolean postTrigger) {
        this.postTrigger = postTrigger;
    }

    /**
     * @return the config for the ITrigger class
     */
    public String getConfig() {
        return config;
    }

    /**
     * Set the config for the ITrigger class.
     * This should be a string in XML format (depends on actual implementation).
     * For an example, see {@link server.lifecycle.state.ChangeAclState}.
     * If parameter config is null or an empty string, the default value "&gt;config /&lt;"
     * is used.
     *
     * @param config the configuration string.
     */
    public void setConfig(String config) {
        if (config == null || config.trim().length() == 0) {
            this.config = "<config />";
        } else {
            ParamParser.parseXmlToDocument(config, "error.param.config");
            this.config = config;
        }
    }

    public Boolean getAfterWork() {
        return afterWork;
    }

    public void setAfterWork(Boolean afterWorkTrigger) {
        this.afterWork = afterWorkTrigger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangeTrigger)) return false;

        ChangeTrigger that = (ChangeTrigger) o;

        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (command != null ? !command.equals(that.command) : that.command != null) return false;
        if (postTrigger != null ? !postTrigger.equals(that.postTrigger) : that.postTrigger != null) return false;
        if (preTrigger != null ? !preTrigger.equals(that.preTrigger) : that.preTrigger != null) return false;
        if (afterWork != null ? !afterWork.equals(that.afterWork) : that.afterWork != null) return false;
        if (ranking != null ? !ranking.equals(that.ranking) : that.ranking != null) return false;
        if (config != null ? !config.equals(that.config) : that.config != null) return false;
        if (triggerType != null ? !triggerType.equals(that.triggerType) : that.triggerType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = triggerType != null ? triggerType.hashCode() : 0;
        result = 31 * result + (ranking != null ? ranking.hashCode() : 0);
        result = 31 * result + (command != null ? command.hashCode() : 0);
        return result;
    }
}
