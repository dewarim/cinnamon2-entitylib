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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Acl;
import server.AclEntryPermission;
import server.Group;
import server.Permission;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "aclentries")
public class AclEntry implements Serializable{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Id	@GeneratedValue
	@Column(name = "id")
	private long id;
		
	@ManyToOne
	@JoinColumn(name = "acl_id",
				nullable = false
	)	
	private Acl acl;
	
	@ManyToOne
	@JoinColumn(name = "group_id",
			nullable = false
	)
	private Group group;

	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;
	
	@OneToMany(mappedBy = "aclentry",
			cascade = { CascadeType.PERSIST, CascadeType.REMOVE }
	)
	private Set<AclEntryPermission> aePermissions = new HashSet<AclEntryPermission>();
		
	public AclEntry(){	

	}

	public AclEntry(Acl acl, Group group){
		this.acl = acl;
		this.group = group;
		// Guarantee referential integrity
		acl.getAclEntries().add(this);
		group.getAclEntries().add(this);
	}
	
	public Group getGroup() {
		return group;
	}
	
	public Acl getAcl() {
		return acl;
	}

	public void setAcl(Acl acl) {
		this.acl = acl;
	}

	/**
	 * @return the permissions
	 */
	@Transient
	public Set<AclEntryPermission> getAePermissions() {
		return aePermissions;
	}

	/**
	 * @param aePermissions the AclEntryPermissions set
	 */
	public void setAePermissions(Set<AclEntryPermission> aePermissions) {
		this.aePermissions = aePermissions;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public long getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Check all Permissions of this AclEntry if one of them matches the required permission.
	 * @param permission the permission whose applicability to this AclEntry you want to check
	 * @return true if a matching Permission was found, false otherwise.
	 */
	public Boolean findPermission(Permission permission){
		log.debug("# of permissions in this aep: "+getAePermissions().size());
		for(AclEntryPermission aep : getAePermissions()){
			Permission p = aep.getPermission();
			// log.debug("check Permission "+p +" against "+permission);
			if(aep.getPermission().equals(permission)){
				return true;
			}
		}
		return false;
	}
		
	public void toXmlElement(Element root){
		Element aclEntry = root.addElement("aclEntry");
		addEntryElements(aclEntry);
	}

	public void addEntryElements(Element aclEntry){
		aclEntry.addElement("id").addText(String.valueOf(getId()));
		aclEntry.addElement("aclId").addText(String.valueOf(getAcl().getId()));
		aclEntry.addElement("groupId").addText(String.valueOf(getGroup().getId()));
	}
	
	public void toXmlElementWithPermissions(Element root){
		Element aclEntry = root.addElement("aclEntry");
		addEntryElements(aclEntry);
		Element permissions = aclEntry.addElement("permissions");		
		Set<AclEntryPermission> aepSet = getAePermissions();
		for(AclEntryPermission aep : aepSet){
			aep.getPermission().toXmlElement(permissions);
		}
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AclEntry)) return false;

        AclEntry aclEntry = (AclEntry) o;

        if (acl != null ? !acl.equals(aclEntry.acl) : aclEntry.acl != null) return false;
        if (group != null ? !group.equals(aclEntry.group) : aclEntry.group != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = acl != null ? acl.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        return result;
    }
}
