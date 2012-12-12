package server.lifecycle;

import org.dom4j.Element;
import server.exceptions.CinnamonException;
import server.global.Constants;
import server.i18n.LocalMessage;
import server.lifecycle.LifeCycleState;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**

 */
@Entity
@Table(name = "lifecycles",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class LifeCycle implements Serializable{

    @Id
    @GeneratedValue
	@Column(name = "id")
	private long 	id;

	@Column(name = "name",
			length = Constants.NAME_LENGTH,
			nullable = false)
	private String name;

    @OneToMany(mappedBy = "lifeCycle")
    private Set<LifeCycleState> states = new HashSet<LifeCycleState>();

    @ManyToOne
    @JoinColumn(name = "default_state_id",
            nullable = true)
    private LifeCycleState defaultState;

    @Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;

    public LifeCycle() {
    }

    public LifeCycle(String name, LifeCycleState defaultState) {
        this.name = name;
        this.defaultState = defaultState;
    }

    public void toXmlElement(Element root){
		Element lc = root.addElement("lifecycle");
		lc.addElement("id").addText(String.valueOf(id));
		lc.addElement("name").addText( LocalMessage.loc(name) );
        lc.addElement("sysName").addText(name);
        Element ds = lc.addElement("defaultState");
        if(defaultState != null){
            defaultState.toXmlElement(ds);
        }
        Element cycleStates = lc.addElement("states");
        for(LifeCycleState lcs : states){
            lcs.toXmlElement(cycleStates);
        }
	}

    public Set<LifeCycleState> getStates() {
        return states;
    }

    public void setStates(Set<LifeCycleState> states) {
        this.states = states;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Set the name of the LifeCycle, must not be null.
     * @param name the name of the LifeCycle.
     */
    public void setName(String name) {
        if(name == null){
            throw new CinnamonException("error.name.required");
        }
        this.name = name;
    }

    public LifeCycleState getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(LifeCycleState defaultState) {
        this.defaultState = defaultState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LifeCycle)) return false;

        LifeCycle lifeCycle = (LifeCycle) o;

        if (defaultState != null ){
            if(lifeCycle.getDefaultState() == null){
                return false;
            }
            // do not use defaultState.equals(lc.defaultState) because this may lead to endless recursion
            // as defaultState also may have a lifecycle reference to this object.
            if( defaultState.getId() != lifeCycle.getDefaultState().getId()){
                return false;
            }
        }
        if (name != null ? !name.equals(lifeCycle.name) : lifeCycle.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
