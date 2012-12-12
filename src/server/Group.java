// cinnamon - the Open Enterprise CMS project
// Copyright (C) 2007-2009 Horner GmbH (http://www.horner-project.eu)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
// (or visit: http://www.gnu.org/licenses/lgpl.html)

package server;

import org.dom4j.Element;
import server.AclEntry;
import server.GroupUser;
import server.dao.DAOFactory;
import server.dao.GroupDAO;
import server.global.Constants;
import server.i18n.LocalMessage;
import utils.HibernateSession;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Groups serve to manage access to objects, folders and other system resources. For each new user,
 * a new Group is created, to which only this user belongs. 
 * Administrators may create ACLs and add groups to them (via addAclEntry) 
 * and they may add/delete users to/from groups.
 * 
 * param	name	The name of the group. Must not start with "_".
 * param	description	A description of this group.
 * param	is_user	Boolean value to determine if this is a group for a new user or a regular group<br>
 * 			(is_user allows GUI-tools to present the admin with a meaningful subset of all available groups,
 * because normally you will not want to change the is_user-groups.)
 *
 *
 */


@javax.persistence.Entity
@Table(name = "groups",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class Group implements Serializable
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Id @GeneratedValue
	@Column(name = "id")
	private long id;

	@Column(name = "name",
			length = Constants.NAME_LENGTH,
			nullable = false)
	private String name;
	
	@Column(name = "description",
			length = Constants.DESCRIPTION_SIZE,
			nullable = false)
	private String description;

	// TODO: rename to isUserGroup
	@Column(name = "is_user",
			nullable = false)
	private Boolean is_user;
	
	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;
	
	@ManyToOne
	@JoinColumn(
			name = "parent_id",
			nullable = true)
	private Group parent;
	
	@OneToMany
	(mappedBy = "group",
			cascade = { CascadeType.PERSIST, CascadeType.REMOVE}				
	)
	private Set<GroupUser> groupUsers = new HashSet<GroupUser>();
	
	@OneToMany(mappedBy = "group"
//			,cascade = { CascadeType.PERSIST, CascadeType.REMOVE }
	)
	private Set<AclEntry> aclEntries = new HashSet<AclEntry>();
	
	public static final String ALIAS_OWNER 		= "_owner";
	public static final String ALIAS_EVERYONE 	= "_everyone";
	public static final String[] defaultGroups = {ALIAS_EVERYONE, ALIAS_OWNER};
	
	public Group(){	

	}

	public Group(Map<String, String> cmd){
		name = cmd.get("name");
		description = cmd.get("description");
		is_user = Boolean.parseBoolean(cmd.get("is_user"));
		parent = null; // TODO: extend API to set parent here.
	}
	
	public Group(String name, String description, Boolean is_user, Group parent){
		this.name = name;
		this.description = description;
		this.is_user = is_user;
		this.parent = parent;
	}
	
	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}

	@Transient
	public Set<GroupUser> getGroupUsers() {
		return groupUsers;
	}

	public void setGroupUsers(Set<GroupUser> groupUsers) {
		this.groupUsers = groupUsers;
	}

	@Transient
	public Set<AclEntry> getAclEntries() {
		return aclEntries;
	}

	public void setAclEntries(Set<AclEntry> aclEntries) {
		this.aclEntries = aclEntries;
	}

	public Boolean getIs_user() {
		return is_user;
	}

	public void setIs_user(Boolean is_user) {
		this.is_user = is_user;
	}

	public Group getParent() {
		return parent;
	}

	public void setParent(Group parent) {
		this.parent = parent;
	}
	
	public Set<Group> findAncestors(){
		HashSet<Group> ancestors = new HashSet<Group>();
		while(getParent() != null && ! ancestors.contains(getParent())){
			ancestors.add(getParent());
		}
		return ancestors;
	}
	
	public void toXmlElement(Element root) {
		Element group = root.addElement( "group" );
		group.addElement("id").addText(String.valueOf(getId()) );
		group.addElement( "name").addText(  LocalMessage.loc(getName()) );
        group.addElement( "sysName").addText(getName());
		group.addElement( "description").addText(  LocalMessage.loc(getDescription()) );
		group.addElement( "is_user").addText( getIs_user().toString() );	
		group.addElement( "parent").addText(getParent() == null ? "null" : String.valueOf(getParent().getId())  );
        DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
        GroupDAO gDao = daoFactory.getGroupDAO(HibernateSession.getLocalEntityManager());
        Element subGroups= group.addElement("subGroups");
        for(Group g: gDao.findAllByParent(this)){
            Element subGroup = subGroups.addElement("subGroup");
            subGroup.addElement("id").addText(String.valueOf(g.getId()));
            subGroup.addElement("name").addText(LocalMessage.loc(g.getName()));
            subGroup.addElement("sysName").addText(g.getName());
        }
        Element users = group.addElement("users");
        for(User u: gDao.findAllUsers(this) ){
            users.add(User.asElement("user", u));
        }
	}

	/**
	 * Recursively find all Groups which are descendants of this group.
	 * (this method returns when there are no more groups to be found).
	 * @return Set<Group> a Set of all descendant groups.
	 */
	@SuppressWarnings("unchecked")
	public Set<Group> findChildren(){
		Set<Group> children = new HashSet<Group>();
		
		EntityManager em = HibernateSession.getLocalEntityManager();
		Query q = em.createQuery("select g from Group g where parent=:group");
		q.setParameter("group", this);
		
		for(Group g : (List<Group>) q.getResultList()){
			children.add(g);
			children.addAll(g.findChildren());
		}
		return children;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;

        Group group = (Group) o;

        if (description != null ? !description.equals(group.description) : group.description != null) return false;
        if (is_user != null ? !is_user.equals(group.is_user) : group.is_user != null) return false;
        if (name != null ? !name.equals(group.name) : group.name != null) return false;
        if (parent != null ? !parent.equals(group.parent) : group.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String toString(){
        return "Group #"+id+" "+name;
    }
}
