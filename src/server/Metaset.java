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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import server.dao.DAOFactory;
import server.dao.MetasetTypeDAO;
import server.global.Constants;
import utils.HibernateSession;
import utils.ParamParser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "metasets" )
public class Metaset implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);

    @Id @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "content",
            length = Constants.METADATA_SIZE,
            nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "type_id",
            nullable = false)
    private MetasetType type;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    @OneToMany(
            mappedBy = "metaset",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
    )
    private Set<OsdMetaset> osdMetasets = new HashSet<OsdMetaset>();

    @OneToMany(
            mappedBy = "metaset",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
    )
    private Set<FolderMetaset> folderMetasets = new HashSet<FolderMetaset>();


    public Metaset() {

    }

    public Metaset(String content, MetasetType type) {
        this.type = type;
        if(content == null){
            setContent("<metaset/>");
        }
        else{
            this.content = content;
        }
    }

    public Metaset(Map<String, String> cmd) {
        EntityManager em = HibernateSession.getLocalEntityManager();
        MetasetTypeDAO mtDao = daoFactory.getMetasetTypeDAO(em);
        type = mtDao.findByName(cmd.get("type"));
        setContent(cmd.get("content"));
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        // fix id and type, in case the content is simply copied from some other metaset.
        Element c = (Element) ParamParser.parseXml(content, null);
        if( !c.hasContent() ){
            c.addAttribute("status","empty");
        }
        c.addAttribute("id", String.valueOf(id));
        if(type != null){
            c.addAttribute("type", type.getName());
        }
        this.content = c.asXML();
    }

    public MetasetType getType() {
        return type;
    }

    public void setType(MetasetType type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }


    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public Set<OsdMetaset> getOsdMetasets() {
        return osdMetasets;
    }

    public void setOsdMetasets(Set<OsdMetaset> osdMetasets) {
        this.osdMetasets = osdMetasets;
    }

    public Set<FolderMetaset> getFolderMetasets() {
        return folderMetasets;
    }

    public void setFolderMetasets(Set<FolderMetaset> folderMetasets) {
        this.folderMetasets = folderMetasets;
    }

    /**
     * Add the Metaset's fields as child-elements to a new element with the given name.
     * If the metaset is null, simply return an empty element.
     *
     * @param elementName name of the element
     * @param metaset the metaset which will be serialized
     * @return the new element.
     */
    public static Element asElement(String elementName, Metaset metaset) {
        if(metaset == null){
            return DocumentHelper.createElement(elementName);

        }
        Element content = (Element) ParamParser.parseXml(metaset.getContent(), null);
        content.addAttribute("id", String.valueOf(metaset.getId()));
        content.addAttribute("type", metaset.getType().getName());
        return content;
    }


}
