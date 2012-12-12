package server;

import server.AclEntry;
import server.Permission;
import server.dao.AclEntryPermissionDAO;
import server.dao.DAOFactory;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "aclentry_permissions")
public class AclEntryPermission implements Serializable{

	static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Id	@GeneratedValue
	@Column(name = "id")
	private long id;

	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;
	
	@ManyToOne
	@JoinColumn(name = "aclentry_id",
				nullable = false
	)	
	private AclEntry aclentry;
	
	@ManyToOne
	@JoinColumn(name = "permission_id",
			nullable = false
	)
	private Permission permission;

	public AclEntryPermission(){}
	
	public AclEntryPermission(AclEntry aclentry, Permission permission) {
		this.aclentry = aclentry;
		this.permission = permission;
		
		aclentry.getAePermissions().add(this);
		permission.getAePermissions().add(this);
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
	 * @return the aclentry
	 */
	public AclEntry getAclentry() {
		return aclentry;
	}

	/**
	 * @param aclentry the aclentry to set
	 */
	public void setAclentry(AclEntry aclentry) {
		this.aclentry = aclentry;
	}

	/**
	 * @return the permission
	 */
	public Permission getPermission() {
		return permission;
	}

	/**
	 * @param permission the permission to set
	 */
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
	
	public Long persist(EntityManager em){		
		AclEntryPermissionDAO aepDAO = daoFactory.getAclEntryPermissionDAO(em);
		aepDAO.makePersistent(this);
		return getId();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AclEntryPermission)) return false;

        AclEntryPermission that = (AclEntryPermission) o;

        if (aclentry != null ? !aclentry.equals(that.aclentry) : that.aclentry != null) return false;
        if (permission != null ? !permission.equals(that.permission) : that.permission != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = aclentry != null ? aclentry.hashCode() : 0;
        result = 31 * result + (permission != null ? permission.hashCode() : 0);
        return result;
    }
}
