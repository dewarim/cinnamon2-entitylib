package server.references;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Acl;
import server.Folder;
import server.User;
import server.dao.*;
import server.data.ObjectSystemData;
import server.exceptions.CinnamonConfigurationException;
import server.exceptions.CinnamonException;
import utils.HibernateSession;
import utils.ParamParser;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 *
 */
public class LinkService {

    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    public Link createLink(ObjectSystemData osd, Folder parent, Acl acl, User owner, LinkResolver resolver) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        Query query = em.createQuery("select l from Link l where l.parent=:parent and l.osd=:osd");
        query.setParameter("osd", osd);
        query.setParameter("parent", parent);
        List<Link> results = query.getResultList();
        if (results.isEmpty()) {
            return new Link(LinkType.OBJECT, resolver, owner, parent, null, osd, acl);
        }
        else {
            return results.get(0);
        }
    }

    public Link createLink(Folder folder, Folder parent, Acl acl, User owner, LinkResolver resolver) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        Query query = em.createQuery("select l from Link l where l.parent=:parent and l.folder=:folder");
        query.setParameter("folder", folder);
        query.setParameter("parent", parent);
        List<Link> results = query.getResultList();
        if (results.isEmpty()) {
            return new Link(LinkType.FOLDER, resolver, owner, parent, folder, null, acl);
        }
        else {
            return results.get(0);
        }
    }

    public Link updateLink(Link link, Map<String, String> cmd) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
        if (cmd.containsKey("acl_id")) {
            AclDAO aDao = daoFactory.getAclDAO(em);
            Acl acl = aDao.get(ParamParser.parseLong(cmd.get("acl_id"), "error.param.acl_id"));
            link.setAcl(acl);
        }
        if (cmd.containsKey("parent_id")) {
            FolderDAO fDao = daoFactory.getFolderDAO(em);
            Folder parent = fDao.get(ParamParser.parseLong(cmd.get("parent_id"), "error.param.parent_id"));
            link.setParent(parent);
        }
        if (cmd.containsKey("owner_id")) {
            UserDAO uDao = daoFactory.getUserDAO(em);
            User owner = uDao.get(ParamParser.parseLong(cmd.get("owner_id"), "error.param.owner_id"));
            link.setOwner(owner);
        }
        if (cmd.containsKey("resolver")) {
            LinkResolver resolver = LinkResolver.valueOf(cmd.get("resolver"));
            link.setResolver(resolver);
        }
        if (cmd.containsKey("object_id") && link.getType() == LinkType.OBJECT){
            ObjectSystemDataDAO oDao = daoFactory.getObjectSystemDataDAO(em);
            ObjectSystemData newOsd = oDao.get(cmd.get("object_id"));            
            if(newOsd == null || newOsd.getRoot() != link.getOsd().getRoot()){
                throw new CinnamonException("error.param.object_id");
            }
            if(link.getResolver() == LinkResolver.LATEST_HEAD){
                // we cannot set an object on a link that is dynamically resolved
                // to return the latestHead object.
                throw new CinnamonException("error.cannot.set.latest.head");
            }
            link.setOsd(newOsd);
        }
        return link;
    }

    public void renderLinkWithinTarget(Link link, Document doc) {
        Element root = doc.addElement("link");
        Element linkParent;

        if (link.getType() == LinkType.FOLDER) {
            link.getFolder().toXmlElement(root);
            linkParent = (Element) root.selectSingleNode("folder");
        }
        else {
            link.getOsd().toXmlElement(root);
            linkParent = (Element) root.selectSingleNode("object");
        }
        addLinkToElement(link, linkParent);
    }

    public Collection<Link> findLinksTo(Folder folder) {
        return null;
    }

    public Collection<Link> findLinksTo(ObjectSystemData osd) {
        return null;
    }

    public Collection<Link> findLinksIn(Folder parent, LinkType linkType) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        Query query;
        Collection<Link> links;
        switch (linkType) {
            case OBJECT:
                query = em.createQuery("from Link l where l.parent=:parent and osd is not NULL");
                query.setParameter("parent", parent);
                links = updateObjectLinks(query.getResultList());
                break;
            case FOLDER:
                query = em.createQuery("from Link l where l.parent=:parent and folder is not NULL");
                query.setParameter("parent", parent);
                links = query.getResultList();
                break;
            default:
                throw new CinnamonConfigurationException("You tried to query for links of an unknown LinkType.");
        }
        return links;
    }

    Collection<Link> updateObjectLinks(Collection<Link> links) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        for (Link link : links) {
            if (link.getResolver() == LinkResolver.LATEST_HEAD) {
                ObjectSystemData osd = link.getOsd();
                if (!osd.getLatestHead()) {
                    Query query = em.createQuery("from ObjectSystemData o where o.root=:root and o.latestHead=true");
                    query.setParameter("root", osd.getRoot());
                    List<ObjectSystemData> results = query.getResultList();
                    if (results.size() != 1) {
                        log.error("Could not find exactly one latestHead object for #" + osd.getId());
                    }
                    // update osd to latestHead:
                    link.setOsd(results.get(0));
                }
            }
        }
        return links;
    }

    public void addLinkToElement(Link link, Element element) {
        element.add(Link.asElement("reference", link));
    }

}
