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

package server.data;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.*;
import server.interfaces.XmlConvertable;
import server.dao.DAOFactory;
import server.dao.FolderDAO;
import server.dao.ObjectSystemDataDAO;
import server.exceptions.CinnamonException;
import server.index.Indexable;
import server.global.PermissionName;
import server.index.ResultValidator;
import utils.HibernateSession;

public class Validator
	implements ResultValidator {
	
	Map<String,Permission> permissionCache = new HashMap<String,Permission>();
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected User user;
	private EntityManager em;
	static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
	ObjectSystemDataDAO osdDAo;
	FolderDAO folderDao;
	
	public Validator(){
		init(HibernateSession.getLocalEntityManager());
	}
	
	public Validator(User user) {
        init(HibernateSession.getLocalEntityManager());
	    this.user=user;
	}

    public Validator(User user, EntityManager em) {
        init(em);
		this.user=user;

	}

    void init(EntityManager em){
        this.em = em;
        osdDAo = daoFactory.getObjectSystemDataDAO(em);
        folderDao = daoFactory.getFolderDAO(em);
    }

	public List<ObjectSystemData> filterUnbrowsableObjects(Collection<ObjectSystemData> objects){
		List<ObjectSystemData> allowedObjects = new ArrayList<ObjectSystemData>();
        if(user.verifySuperuserStatus(em)){
            allowedObjects.addAll(objects);
            return allowedObjects;
        }
	    Permission browseObject = fetchPermission(PermissionName.BROWSE_OBJECT);
	    for(ObjectSystemData osd : objects){
			if (check_acl_entries(osd.getAcl(), browseObject, osd)) {
				allowedObjects.add(osd);
			}
			else{
				log.debug(String.format("No browse permission found for object %d", osd.getId()));
			}	    	
	    }
	    return allowedObjects;
	}
	
	/**
	 * Filter a collection of OSDs depending on whether the user may read its custom metadata.
	 * @param objects a collection of objects to be filtered.
	 * @return List of OSDs the user has a READ_OBJECT_CUSTOM_METADATA-permission for.
	 */
	public List<ObjectSystemData> filterForCustomMetadata(Collection<ObjectSystemData> objects){
		List<ObjectSystemData> allowedObjects = new ArrayList<ObjectSystemData>();
	    Permission browseObject = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
	    for(ObjectSystemData osd : objects){
			if (check_acl_entries(osd.getAcl(), browseObject, osd)) {
				allowedObjects.add(osd);
			}
			else{
				log.debug(String.format("No read_object_custom_metadata permission found for OSD %d", osd.getId()));
			}	    	
	    }
	    return allowedObjects;
	}

    public List<Folder> filterUnbrowsableFolders(Collection<Folder> folders) {
        List<Folder> allowedFolders = new ArrayList<Folder>();
        if (user.verifySuperuserStatus(em)) {
            log.debug("User is admin - show all folders.");
            allowedFolders.addAll(folders);
            return allowedFolders;
        }
        Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
        for (Folder folder : folders) {
            if (check_acl_entries(folder.getAcl(), browseFolder, folder)) {
                allowedFolders.add(folder);
            } else {
                log.debug(String.format("No browse permission found for folder %d", folder.getId()));
            }
        }
        return allowedFolders;
    }
	
	public void validatePermissions(ObjectSystemData osd, String... permissions){
		for(String perm : permissions){
			validateAgainstAcl(osd, fetchPermission(perm));
		}
	}
	
	public void validateCreate(Folder folder) {
		Permission createFolderPermission = fetchPermission(PermissionName.CREATE_OBJECT);
		validateFolderAgainstAcl(folder, createFolderPermission);
	}
	
	public void validateCopy(ObjectSystemData sourceObject, Folder targetFolder) {
		Permission readContent = fetchPermission(PermissionName.READ_OBJECT_CONTENT);
		validateAgainstAcl(sourceObject, readContent);
		Permission readMeta = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
		validateAgainstAcl(sourceObject, readMeta);
		Permission readSys = fetchPermission(PermissionName.READ_OBJECT_SYS_METADATA);
		validateAgainstAcl(sourceObject, readSys);
		
		Permission writeToFolder = fetchPermission(PermissionName.CREATE_OBJECT);
		validateFolderAgainstAcl(targetFolder, writeToFolder);
	}
	
	public void validateCreateFolder(Folder parentFolder) {
		Permission createFolder = fetchPermission(PermissionName.CREATE_FOLDER);
		validateFolderAgainstAcl(parentFolder,createFolder);
	}
	
	public void validateDelete(ObjectSystemData osd) {
		Permission deleteObject = fetchPermission(PermissionName.DELETE_OBJECT);
		validateAgainstAcl(osd, deleteObject);
		log.debug("after validate");

		// TODO: use DAO
		log.debug("before set param");
		@SuppressWarnings({"JpaQueryApiInspection"})
        Query q = em.createNamedQuery("findProtectedRelations");
		q.setParameter("osd1", osd);
		q.setParameter("osd2", osd);
		
		log.debug("query is ready");
		if (q.getResultList().size()> 0) {
			String msg="Object " + osd.getId() + " cannot be deleted, it has protected relations.";
			throw new CinnamonException(msg);
		}
	}
	
	public void validateDeleteFolder(Folder folder){
		Permission deleteFolder = fetchPermission(PermissionName.DELETE_FOLDER);
		validateFolderAgainstAcl(folder, deleteFolder);
	}
	
	public void validateGetContent(ObjectSystemData osd){
		Permission readObject = fetchPermission(PermissionName.READ_OBJECT_CONTENT);
		validateAgainstAcl(osd,readObject);	
		
	}
	
	public void validateGetMeta(ObjectSystemData osd) {
		Permission readObject = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
		validateAgainstAcl(osd,readObject);	
	}
	
	public void validateGetFolderMeta(Folder folder) {
		Permission readObject = fetchPermission(PermissionName.READ_OBJECT_CUSTOM_METADATA);
		validateFolderAgainstAcl(folder,readObject);	
	}
	
	
	public void validateGetSysMeta(ObjectSystemData osd) {		
		Permission browseObject = fetchPermission(PermissionName.READ_OBJECT_SYS_METADATA);
		validateAgainstAcl(osd, browseObject);
	}
	
	public void validateGetSysMeta(Folder folder) {		
		Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
		validateFolderAgainstAcl(folder, browseFolder);
	}
	
	public void validateGetObjects(Folder folder) {
		// currently everybody may get objects, but validation 
		// per object can suppress those without browse permission
		Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
//		validateFolderAgainstAcl(cmd.get("parentid")), browseFolder);
		validateFolderAgainstAcl(folder, browseFolder);
	}
	
	public void validateSetObjectAcl(ObjectSystemData osd){
		Permission writeObject = fetchPermission(PermissionName.SET_ACL);
		validateAgainstAcl(osd, writeObject);
	}

	public void validateMoveObject(ObjectSystemData osd, Folder targetFolder){
		Permission writeObject = fetchPermission(PermissionName.MOVE);
		validateAgainstAcl(osd, writeObject);
        Permission writeToFolder = fetchPermission(PermissionName.CREATE_OBJECT);
		validateFolderAgainstAcl(targetFolder, writeToFolder);
	}
	
	public void validateGetFolder(Folder folder) {
		Permission browseFolder = fetchPermission(PermissionName.BROWSE_FOLDER);
		validateFolderAgainstAcl(folder, browseFolder);
	}
	
	public void validateLock(ObjectSystemData osd, User user) {
		if(osd.getLocked_by() != null && !osd.getLocked_by().equals(user)){
			throw new CinnamonException("Object " + osd.getId() + " is already locked.");
		}
		else{
			log.debug("about to validateAgainstAcl");
		}
		Permission lockPermission = fetchPermission(PermissionName.LOCK);
		validateAgainstAcl(osd,lockPermission);	
	}
	
	public void checkLockStatus(ObjectSystemData osd){
		User locker  = osd.getLocked_by();
		if(locker == null || ! locker.equals(user)){
			 throw new CinnamonException("Object " + 
						osd.getId() + " must be locked by session user for setting content.");
		}
	}
	
	public void validateSetContent(ObjectSystemData osd){
		Permission writeObject = fetchPermission(PermissionName.WRITE_OBJECT_CONTENT);
		validateAgainstAcl(osd, writeObject);
		checkLockStatus(osd);
	}
	
	public void validateSetMeta(ObjectSystemData osd) {
		Permission writeObject = fetchPermission(PermissionName.WRITE_OBJECT_CUSTOM_METADATA);
		validateAgainstAcl(osd, writeObject);	
		checkLockStatus(osd);
	}
	
	public void validateSetSysMeta(ObjectSystemData osd) {
		Permission writeObject = fetchPermission(PermissionName.WRITE_OBJECT_SYS_METADATA);
		validateAgainstAcl(osd, writeObject);
		checkLockStatus(osd);
	}

    /**
     * Note: this is the one Permission that does currently not depend on an ACL.
     * The logic is thus:
     * <ol>
     *     <li>If you are the lock owner, you may always remove the lock.</li>
     *     <li>If you are the superuser, you may always remove the lock.</li>
     *     <li>If you are not the lock owner and not a superuser,
     *     then this lock is none of your business (and you are not allowed to unlock it).</li>
     * </ol>
     * @param osd the OSD to unlock.
     */
	public void validateUnlock(ObjectSystemData osd) {
		User lockOwner= osd.getLocked_by();
        if(lockOwner == null){
            throw new CinnamonException("error.object.not.locked");
        }
        else if(lockOwner.equals(user)){
            // user may remove his own lock.
        }
        else if( user.verifySuperuserStatus(em)) {
            /*
             * owner is not null and not the user: this requires superuser status
             */
        }
        else{
            // owner is someone else and user is not superuser: forbidden.
            throw new CinnamonException(
                    String.format("Object %d can only be unlocked by the lock owner %s or a superuser.",
                    osd.getId(), lockOwner.getName())
            );
        }
	}
	
	public void validateVersion(ObjectSystemData osd) {
		Permission versionObject = fetchPermission(PermissionName.VERSION_OBJECT);
		validateAgainstAcl(osd, versionObject);	
		if(osd.getLocked_by() != null) throw new CinnamonException("Object " + 
				osd.getId() + " must be unlocked for versioning.");
		
	}

    /**
     * Validate a Permission against an object's ACL. Do not use this if a more specific
     * test exists (for example, validateDelete).
     * @param osd The object that the user wishes to modify.
     * @param permission The Permission you need to test
     */
	public void validateAgainstAcl(ObjectSystemData osd, Permission permission){
		if(osd == null){
			throw new CinnamonException("error.object.not.found");
		}
		log.debug("looking up acl");
		Acl acl = osd.getAcl();
		if (acl == null) {
			throw new CinnamonException("error.acl.invalid");
		}
		
		if (check_acl_entries(acl, permission, osd)) {
			log.debug("check_acl_entries returned true.");
			return;
		}
		    	
    	// no sufficient permission entry found
		throw new CinnamonException("error.missing.permission." + permission.getName());
	}
	
	public void validateFolderAgainstAcl(Folder folder, Permission permission){
		// get acl id		
		log.debug("permission needed: "+permission.getName());
		if (folder == null) {
			throw new CinnamonException("error.folder.not_found");
		}
		
		if(user.verifySuperuserStatus(em)){
			log.debug("Superusers are not subject to permissions ");
		    return;
		}
		
		Acl acl = folder.getAcl();
		if (acl == null) {
			throw new CinnamonException("error.acl.not_found");
		}
		else{
			log.debug(String.format("found acl: %s (%s)",acl.getId(), acl.getName()));
		}
			
		log.debug("Looking up AclEntries");
	    if (check_acl_entries(acl, permission, folder)) {
			return;
		}
	 
	    // no sufficient permission entry found
	    throw new CinnamonException("error.missing.permission."+ permission.getName());
	}
	
	public boolean check_acl_entries(Acl acl, Permission permission, Ownable ownable){
    	if(user.verifySuperuserStatus(em)){
    		log.debug("Superusers may do anything.");
    		return true; // Superuses are exempt from all Permission checks.
    	}
    	log.debug("Looking up AclEntries");
    	// create Union of Sets: user.groups and acl.groups => iterate over each group for permitlevel.

    	// 2. query acl for usergroup.
    	Set<AclEntry> direct_entries = new HashSet<AclEntry>();
    	direct_entries.addAll(getAclEntriesByUser(acl,user));
    	  	
    	Set<AclEntry> aclentries = new HashSet<AclEntry>();
    	
    	log.debug("descending into groupMatches2");
    	aclentries.addAll(getGroupMatches2(direct_entries, acl));
    	aclentries.addAll(findAliasEntries(acl, user, ownable));
    	
    	log.debug("checking all aclentries for permission");
    	// now browse all entries for the first one to permit the intended operation:
    	log.debug("# of aclentries: "+aclentries.size());
    	for (AclEntry entry : aclentries) {
    		log.debug("check aclentry with id "+entry.getId()+" for acl "+entry.getAcl().getName()
    			+" and group "+entry.getGroup().getName());
			if (entry.findPermission(permission)){
				log.debug("Found aclentry with required permission. id="+entry.getId());
				return true;
			}
		}
    	return false;
	}
	
	List<AclEntry> getAclEntriesByUser(Acl acl, User user){
		return acl.getUserEntries(user, em);
	}
	
	Set<AclEntry> findAliasEntries(Acl acl, User user, Ownable ownable){
		Set<AclEntry> aliasEntries = new HashSet<AclEntry>();
		
		/*
		 * If the ACL has an AclEntry for "everyone", add its AE to the
		 * set of AEs for permission checking.
		 */
		for(AclEntry ae : acl.getAclEntries()){
			Group g = ae.getGroup();
			if(g.getName().equals(Group.ALIAS_EVERYONE)){
				aliasEntries.add(ae);
				log.debug("Found Everyone-Group");
			}
			else{
				log.debug("ACL does not have an EVERYONE-AclEntry.");
			}
		}
		
		if(ownable == null){
			/* Without a valid object, the user cannot be its owner.
			 */
			log.debug("object is not ownable");
			return aliasEntries;
		}
		
		// check Alias::Owner:
		/*
		 * If the Acl has an AclEntry fron the "owner"-Group and the user
		 * _is_ the owner of the object, the AE is added to the set of AEs. 
		 */
		log.debug("checking owner");
		if(user.equals(ownable.getOwner())){
			log.debug("user == owner");
			for(AclEntry ae : acl.getAclEntries()){
				Group g = ae.getGroup();
				log.debug("group-name:"+g.getName());
				if(g.getName().equals(Group.ALIAS_OWNER)){
					aliasEntries.add(ae);
					log.debug("User is the owner.");
				}
				else{
					log.debug("User is not the owner");
				}
			}
		}
		
		return aliasEntries;
	}	
	
	public void validatePermission(Acl acl, Permission permission) {
		if(! check_acl_entries(acl, permission, null)){
			throw new CinnamonException("error.missing.permission." + permission.getName());
			// TODO: parameterize correctly
		}
	}
	
	public void validatePermission(Acl acl, String permissionName) {
		Permission permission = fetchPermission(permissionName);
		validatePermission(acl,permission);
	}

    Boolean containsOneOf(Map cmd, Object... alternatives){
        Boolean found = false;
        for(Object test : alternatives){
            if(cmd.containsKey(test)){
                found = true;
                break;
            }
        }
        return found;
    }

	public void validateUpdateFolder(Map<String,String> cmd, Folder folder) {
		if(user.verifySuperuserStatus(em)){
            return;	// superusers are not subject to permissions
        }
		
		if(cmd.containsKey("parentid")){
			Permission p = fetchPermission(PermissionName.MOVE);
			validateFolderAgainstAcl(folder, p);
            FolderDAO fDao = daoFactory.getFolderDAO(em);
            Folder parentFolder = fDao.get(cmd.get("parentid"));
			if (parentFolder == null) {
				// TODO: parametrize correctly
				throw new CinnamonException("error.parent_folder.not_found");
//				throw new CinnamonException( "Parentfolder with id "+value+" was not found.");
			}
            validateCreateFolder(parentFolder);
		}
		if(cmd.containsKey("aclid")){
			Permission p = fetchPermission(PermissionName.SET_ACL);
			validateFolderAgainstAcl(folder,p);
		}
		if(containsOneOf(cmd, "name", "metadata", "ownerid", "typeid")){
			Permission p = fetchPermission(PermissionName.EDIT_FOLDER);
			validateFolderAgainstAcl(folder,p);
		}
	}

	/**
     * @see server.index.ResultValidator#validateAccessPermissions(org.apache.lucene.document.Document, Class)
	 */
	public XmlConvertable validateAccessPermissions(Document doc, Class<? extends Indexable> filterClass){
//		log.debug("start::validateAccessPermissions");
		String javaClass = doc.get("javaClass");
		String hibernateId = doc.get("hibernateId");
//		log.debug(String.format("filterClass: %s - javaClass: %s - hibernateId: %s", 
//				filterClass, javaClass, hibernateId));
		try{
			if(javaClass.equals("server.data.ObjectSystemData")){
				log.debug("load OSD from database");
				ObjectSystemData osd = osdDAo.get(hibernateId);
				log.debug("...done");
				if(osd != null){
				    Permission permission = fetchPermission(PermissionName.BROWSE_OBJECT);
//				    log.debug("validatePermission");
				    validatePermission(osd.getAcl(), permission);
                    if(filterByClass(osd, filterClass)){
                        return osd;
                    }
				}
				else{
					log.debug("Object with id "+hibernateId+" was not found.");
				}
			}
			else if(javaClass.equals("server.Folder")){
				Folder folder = folderDao.get(hibernateId);
				if(folder != null){
				    Permission permission = fetchPermission(PermissionName.BROWSE_FOLDER);
				    validatePermission(folder.getAcl(), permission);
                    if(filterByClass(folder, filterClass)){
                        return folder;
                    }
				}
				else{
					log.debug("Folder with id "+hibernateId+" was not found.");
				}
			}
			else{
				log.debug("validateAccessPermissions does not know how to verify access to '"+javaClass+"'");
			}
		}
		catch (Exception e) {
			// skip stacktrace:
			log.debug("validateSearchResults: "+e.getMessage());
		}
        return null;
	}

    /**
     * @param indexable an object returned by a search.
     * @param filterClass the class by which the object will be filtered. May be null.
     * @return true if the indexable's class is equal to the filterClass or null.
     */
    public Boolean filterByClass(Indexable indexable, Class<? extends Indexable> filterClass){
        if(filterClass == null){
            return true;
        }
        if(indexable == null){ // should not happen.
            return false;
        }
        return indexable.getClass().equals(filterClass);
    }

	public void checkBrowsePermission(ObjectSystemData osd) {
		Permission browseObject = fetchPermission(PermissionName.BROWSE_OBJECT);
		checkBrowsePermission(osd, browseObject);
	}
	public void checkBrowsePermission(ObjectSystemData osd, Permission browseObject) {
		if (! check_acl_entries(osd.getAcl(), browseObject, osd)){
			throw new CinnamonException("error.missing.permission._browse");
		}
	}
	
	protected Permission fetchPermission(String permissionName){
		if(permissionCache.containsKey(permissionName)){
			return permissionCache.get(permissionName);
		}
		else{
			Permission permission = Permission.fetch(permissionName);
			permissionCache.put(permissionName, permission);
			return permission;
		}
	}

	public Set<AclEntry> getGroupMatches2(Set<AclEntry> direct_entries, Acl acl){
		Set<AclEntry> aclentries = new HashSet<AclEntry>();
    	for (AclEntry ae : direct_entries) {
			if (! aclentries.add(ae)){
				continue;
			}
			
			Group parent = ae.getGroup().getParent();
			while (parent != null) {
				// look if the parent has a relevant aclentry for this acl:
				// TODO: use DAO
				Query q = em.createQuery("select ae from AclEntries ae where group_id=:gid and acl_id=:aid");
				q.setParameter("gid", parent.getId());
				q.setParameter("aid", acl.getId());
				try {
					AclEntry a;
					a = (AclEntry) q.getSingleResult();
					if (! aclentries.add(a)){
						break; // break circular parent-child-parent relations.
					}
				} catch (Exception e) {
					// continue.
				}
				// continue with the parent's parent:
				parent = parent.getParent();
			}
    	}
    	return aclentries;
	}
}
