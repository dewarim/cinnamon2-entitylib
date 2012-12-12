package server.references;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Acl;
import server.Folder;
import server.User;
import server.data.ObjectSystemData;

import javax.persistence.*;

/**
 * 
 */
@Entity
@Table(name = "links")
public class Link {

    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     *
     */
    private static final long	serialVersionUID	= 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Enumerated(EnumType.STRING)
    private LinkType type;
    
    @Enumerated(EnumType.STRING)
    private LinkResolver resolver = LinkResolver.FIXED;

    @ManyToOne
    @JoinColumn(name = "owner_id",
            nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "parent_id",
            nullable = true)
    private Folder parent;
    
    @ManyToOne
    @JoinColumn(name = "folder_id",
            nullable = true)
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "osd_id",
            nullable = true)
    private ObjectSystemData osd;
    
    @ManyToOne
    @JoinColumn(name = "acl_id",
            nullable = false)
    private Acl acl;

    public Link() {
        
    }

    public Link(LinkType type, LinkResolver resolver, User owner, Folder parent, Folder folder, ObjectSystemData osd, Acl acl) {
        this.type = type;
        this.resolver = resolver;
        this.owner = owner;
        this.parent = parent;
        this.folder = folder;
        this.osd = osd;
        this.acl = acl;
    }
    
    public static Element asElement(String rootName, Link link){
        Element e = DocumentHelper.createElement(rootName);
        if(link != null){
            e.addElement("linkId").addText(""+link.id);
            e.addElement("type").addText(link.type.name());
            e.addElement("aclId").addText(""+link.acl.getId());
            e.addElement("ownerId").addText(""+link.owner.getId());
            e.addElement("parentId").addText(""+link.parent.getId());
            e.addElement("resolver").addText(link.resolver.name());
            if(link.type == LinkType.FOLDER){
                e.addElement("id").addText(""+link.folder.getId());
            }
            else{
                e.addElement("id").addText(""+link.osd.getId());
            }
        }
        return e;
    }

    public LinkType getType() {
        return type;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    public LinkResolver getResolver() {
        return resolver;
    }

    public void setResolver(LinkResolver resolver) {
        this.resolver = resolver;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public ObjectSystemData getOsd() {
        return osd;
    }

    public void setOsd(ObjectSystemData osd) {
        this.osd = osd;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link)) return false;

        Link link = (Link) o;

        if (acl != null ? !acl.equals(link.acl) : link.acl != null) return false;
        if (folder != null ? !folder.equals(link.folder) : link.folder != null) return false;
        if (osd != null ? !osd.equals(link.osd) : link.osd != null) return false;
        if (owner != null ? !owner.equals(link.owner) : link.owner != null) return false;
        if (parent != null ? !parent.equals(link.parent) : link.parent != null) return false;
        if (resolver != link.resolver) return false;
        if (type != link.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (resolver != null ? resolver.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (folder != null ? folder.hashCode() : 0);
        result = 31 * result + (osd != null ? osd.hashCode() : 0);
        result = 31 * result + (acl != null ? acl.hashCode() : 0);
        return result;
    }
}
