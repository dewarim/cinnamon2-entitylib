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
import server.dao.DAOFactory;
import server.dao.PermissionDAO;
import server.exceptions.CinnamonException;
import server.global.Constants;
import server.global.PermissionName;
import server.i18n.LocalMessage;
import utils.HibernateSession;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "permissions",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}		
)
public class Permission implements Serializable{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
	
//	private transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Id @GeneratedValue
	@Column(name = "id")
	private long id = 0;

	@Column(name = "name",
			length = Constants.NAME_LENGTH,
			nullable = false)
	private String name;
	
	@Column(name = "description",
			length = Constants.DESCRIPTION_SIZE,
			nullable = false)
	private String description;
	
	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;

	@OneToMany(mappedBy = "permission",
			cascade = { CascadeType.PERSIST, CascadeType.REMOVE }
	)
	private Set<AclEntryPermission> aePermissions = new HashSet<AclEntryPermission>();

	
	// Object related permissions:


    // -------------- Folder related permissions: ----------------------------

    // permission affecting both folder and objects:

    // other permissions:

    public static final String[] defaultPermissions = {
			// object:
			PermissionName.WRITE_OBJECT_CONTENT, PermissionName.READ_OBJECT_CONTENT,
			PermissionName.WRITE_OBJECT_CUSTOM_METADATA, PermissionName.READ_OBJECT_CUSTOM_METADATA,
			PermissionName.WRITE_OBJECT_SYS_METADATA, PermissionName.READ_OBJECT_SYS_METADATA,
			PermissionName.VERSION_OBJECT, PermissionName.DELETE_OBJECT,
			PermissionName.BROWSE_OBJECT, PermissionName.LOCK,
			
			// folder:
			PermissionName.CREATE_FOLDER, PermissionName.DELETE_FOLDER, PermissionName.BROWSE_FOLDER,
			PermissionName.EDIT_FOLDER, PermissionName.CREATE_OBJECT,
			
			// misc:
			PermissionName.MOVE, PermissionName.SET_ACL,
			PermissionName.QUERY_CUSTOM_TABLE,
			PermissionName.CREATE_INSTANCE,
	};
	
	
	public Permission (){	

	}

	public Permission(Map<String, String> cmd){
		name = cmd.get("name");
		description = cmd.get("description");
	}

	public Permission(String name, String descripton){
		this.name = name;
		this.description = descripton;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
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

	
	/**
	 * @return the aePermissions
	 */
	@Transient
	public Set<AclEntryPermission> getAePermissions() {
		return aePermissions;
	}

	/**
	 * @param aePermissions the aePermissions to set
	 */
	public void setAePermissions(Set<AclEntryPermission> aePermissions) {
		this.aePermissions = aePermissions;
	}

	/**
	 * Given a name, fetch(String name) fetches the Permission from the database.
	 * If the requested object cannot be found, a CinnamonException is thrown
	 * @param name the name of the permission
	 * @return the Permission of the given name.
	 * @throws CinnamonException if there is no Permission by this name.
	 */
	public static Permission fetch(String name){
		EntityManager em = HibernateSession.getLocalEntityManager();
		PermissionDAO permDao = daoFactory.getPermissionDAO(em);
		try {
			return permDao.findByName(name);
		} catch (Exception e) {
            throw new CinnamonException("error.permission.not_found", name);
        }
	}
	
	public String toString(){
		return getName();
	}
	
	public void toXmlElement(Element root){
		Element permission = root.addElement("permission");
		permission.addElement("id").addText(String.valueOf(getId()) );
		permission.addElement("name").addText(  LocalMessage.loc(getName()));
        permission.addElement("sysName").addText(getName());
		permission.addElement("description").addText( LocalMessage.loc(getDescription()));
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;

        Permission that = (Permission) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
