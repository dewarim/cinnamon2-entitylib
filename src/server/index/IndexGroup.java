package server.index;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.global.Constants;
import server.i18n.LocalMessage;
import server.index.IndexItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 
@Entity
@Table(name="index_groups",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}		
)
public class IndexGroup implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	@Transient
	transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Id @GeneratedValue
	@Column(name = "id")
	long id;
	
	@Version
	@Column(name="obj_version",
			nullable = false)
	@SuppressWarnings("unused")
	private Long obj_version = 0L;
	
	@Column(name = "name",
			length = Constants.NAME_LENGTH,
			nullable = false)
	String name;
	
	@OneToMany(mappedBy = "indexGroup")
	private Set<IndexItem> items = new HashSet<IndexItem>();
	
	public IndexGroup(){
		
	}
	
	public IndexGroup(String name){
		this.name = name;	
	}

    public IndexGroup(Map<String,String> fields){
        this.name = fields.get("name");
    }

	public void toXmlElement(Element root, Boolean includeIndexItems){
		Element group = root.addElement("indexGroup");
		group.addElement("id").addText(String.valueOf(id));
		group.addElement("name").addText(LocalMessage.loc(name));
		group.addElement("sysName").addText(name);
		Element itemList = group.addElement("indexItems");
		if(includeIndexItems){
			for(IndexItem item : items){
				item.toXmlElement(itemList);
			}
		}
	}
	
	public void toXmlElement(Element root){
		toXmlElement(root, false);
	}

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<IndexItem> getItems() {
        return items;
    }

    public void setItems(Set<IndexItem> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexGroup)) return false;

        IndexGroup that = (IndexGroup) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
