package server.helpers;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.*;
import server.dao.DAOFactory;
import server.dao.MetasetDAO;
import server.dao.MetasetTypeDAO;
import server.data.ObjectSystemData;
import server.data.Validator;
import server.exceptions.CinnamonException;
import server.global.PermissionName;
import server.interfaces.IMetasetJoin;
import server.interfaces.IMetasetOwner;
import utils.HibernateSession;
import utils.ParamParser;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Collected methods for metaset treatment
 */
public class MetasetService {

    Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * If more than one item references this metaset, create a new branch for the current owner.
     *
     * @param content the new content to set
     * @param metaset the Metaset to update
     * @param owner   if branching is necessary, this object will be the owner of the new branch.
     */
    public void updateWithBranch(String content, IMetasetOwner owner, Metaset metaset) {
        Collection<IMetasetOwner> owners = fetchOwners(owner, metaset.getType());
        if (owners.size() > 1) {
            // remove existing metaset:
            IMetasetJoin metasetJoin = owner.fetchMetasetJoin(metaset.getType());
            if(metasetJoin != null){
                metasetJoin.doDelete();
            }
            // create branch:
            Metaset branch = new Metaset(content, metaset.getType());
            EntityManager em = HibernateSession.getLocalEntityManager();
            em.persist(branch);
            owner.addMetaset(branch);
        } else {
            metaset.setContent(content);
        }
    }

    /**
     * Try to delete all references to this metaset, then this metaset itself. Throws a CinnamonException if there
     * are any ACL problems.
     *
     * @param type         the type of Metaset to delete
     * @param currentOwner an item that references this metaset.
     * @param deletePolicy the policy for deletions, determines for example if an exception is thrown if not all
     *                     references can be deleted.
     * @param validator    a validator object to check the ACLs if deletion is allowed.
     *                     Delete metaset requires WRITE_METADATA permission.
     */
    public Collection<IMetasetOwner> deleteMetaset(IMetasetOwner currentOwner, MetasetType type, Validator validator, DeletePolicy deletePolicy) {
        List<IMetasetOwner> affectedOwners = new ArrayList<IMetasetOwner>();
        for (IMetasetOwner owner : fetchOwners(currentOwner, type)) {
            Acl acl = owner.getAcl();
            try {
                if (currentOwner instanceof ObjectSystemData) {
                    validator.validatePermission(acl, PermissionName.WRITE_OBJECT_CUSTOM_METADATA);
                } else {
                    validator.validatePermission(acl, PermissionName.EDIT_FOLDER);
                }
            } catch (Exception e) {
                log.debug("Not allowed to remove metaset reference:" + e.getLocalizedMessage());
                if (deletePolicy.equals(DeletePolicy.COMPLETE)) {
                    throw new CinnamonException(e);
                } else {
                    // policy: ALLOWED == continue with next item.
                    continue;
                }
            }
            IMetasetJoin metasetJoin = owner.fetchMetasetJoin(type);
            metasetJoin.doDelete();
            affectedOwners.add(owner);
        }
        return affectedOwners;
    }

    public Collection<IMetasetOwner> fetchOwners(IMetasetOwner owner, MetasetType type) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        Metaset metaset = owner.fetchMetaset(type.getName());
        return fetchOwners(metaset);
    }

    public Collection<IMetasetOwner> fetchOwners(Metaset metaset) {
        EntityManager em = HibernateSession.getLocalEntityManager();

        // fetch OsdMetasets and FolderMetasets:
        TypedQuery<IMetasetOwner> osdQuery = em.createQuery("select o.osd from OsdMetaset o where o.metaset=:metaset", IMetasetOwner.class);
        osdQuery.setParameter("metaset", metaset);
        List<IMetasetOwner> osdMetasets = osdQuery.getResultList();
        TypedQuery<IMetasetOwner> folderQuery = em.createQuery("select fm.folder from FolderMetaset fm where fm.metaset=:metaset", IMetasetOwner.class);
        folderQuery.setParameter("metaset", metaset);

        // combine collections:
        List<IMetasetOwner> folderMetasets = folderQuery.getResultList();
        List<IMetasetOwner> owners = new ArrayList<>(osdMetasets.size() + folderMetasets.size());
        owners.addAll(folderMetasets);
        owners.addAll(osdMetasets);
        return owners;
    }

    public void unlinkMetaset(IMetasetOwner owner, Metaset metaset) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        IMetasetJoin metasetJoin = owner.fetchMetasetJoin(metaset.getType());
        log.debug("remove metasetJoin "+metasetJoin.getId()+" for object "+owner.toString()+" with id "+owner.getId());
        if(fetchOwners(metaset).size() == 1){
            metaset.getOsdMetasets().clear();
            metaset.getFolderMetasets().clear();            
            em.remove(metaset);
        }
        metasetJoin.doDelete();
//        metasetJoin.doDelete();
//        if (metasetOwners.size() <= 1) {
//            // if nothing links to this metaset, delete it.
//            // also, if metasetOwners.size == 1 and we just deleted a metasetJoin,
//            // it's safe to assume that the metaset would now be orphaned. 
//            log.debug("Remove Metaset "+metaset.getId()+" - it's no longer used.");
//            em.remove(metaset);
//        }
//        else{
//            log.debug("Keeping Metaset "+metaset.getId()+", it's still in use.");
//            for(IMetasetOwner currentOwner:metasetOwners){
//                log.debug("CurrentOwner: "+currentOwner);
//            }
//        }
    }

    public Metaset createOrUpdateMetaset(IMetasetOwner owner, MetasetType metasetType, String content, WritePolicy writePolicy) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        Metaset metaset = owner.fetchMetaset(metasetType.getName());
        if (metaset == null) {
            log.debug("metadata is: "+owner.getMetadata()+" and contains no "+metasetType.getName()+" metaset.");
            // create new metaset
            metaset = new Metaset(content, metasetType);
            if(! em.contains(owner)){
                // required, otherwise we cannot persist the metaset which requires a valid owner.id
                em.persist(owner);
            }
            em.persist(metaset);
            owner.addMetaset(metaset);
        } else {
            // update metaset
            log.debug("update metaset with writePolicy "+writePolicy.name());
            switch (writePolicy) {
                case WRITE:
                    metaset.setContent(content);
                    break;
                case IGNORE:
                    break; // do nothing, there already is content;
                case BRANCH:
                    updateWithBranch(content, owner, metaset);
                    break;
            }
            log.debug("setting metaset content to:\n" + content);
        }
        return metaset;
    }

    public void copyMetasets(IMetasetOwner source, IMetasetOwner target, String metasets) {
        if (metasets == null) {
            for (Metaset m : source.fetchMetasets()) {
                log.debug(String.format("Add Metaset %d %s from %d to %d", m.getId(), m.getType().getName(), source.getId(), target.getId()));
                target.addMetaset(m);
            }
        } else {
            for (String metasetName : metasets.split(",")) {
                Metaset metaset = source.fetchMetaset(metasetName);
                target.addMetaset(metaset);
            }
        }
    }

    public void initializeMetasets(IMetasetOwner owner, String metasets) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
        MetasetTypeDAO mtDao = daoFactory.getMetasetTypeDAO(em);
        if(metasets == null){
            // nothing to do.
            return;
        }
        for (String metasetName : metasets.split(",")) {
            MetasetType metasetType = mtDao.findByName(metasetName);
            Metaset metaset = new Metaset(metasetType.getConfig(), metasetType);
            em.persist(metaset);
            owner.addMetaset(metaset);
        }
    }
}
