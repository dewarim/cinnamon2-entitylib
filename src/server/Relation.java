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
import org.dom4j.Node;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.RelationType;
import server.dao.DAOFactory;
import server.dao.ObjectSystemDataDAO;
import server.dao.RelationDAO;
import server.dao.RelationTypeDAO;
import server.data.ObjectSystemData;
import server.exceptions.CinnamonException;
import server.global.Constants;
import utils.HibernateSession;
import utils.ParamParser;
import server.resolver.RelationSide;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;


@Entity
@Table(name = "relations",
		uniqueConstraints = {@UniqueConstraint(columnNames={"leftid", "rightid", "type_id"})}
)
public class Relation 
	implements Serializable {

	private static final long	serialVersionUID	= 1L;
	static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
    transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Id
    @GeneratedValue
	@Column(name = "id")
	private long id;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "type_id")	
	private RelationType type;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "leftid",
				nullable = false)	
	private ObjectSystemData leftOSD;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "rightid",
				nullable = false)	
	private ObjectSystemData rightOSD;

    @Column(name = "metadata",
			length = Constants.METADATA_SIZE,
			nullable = false
			)
	@Type(type="text")
	private String metadata = "<meta/>";

	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version;
	
	
	public Relation(){
		
	}
		
	public Relation(RelationType type, ObjectSystemData left, ObjectSystemData right, String metadata){
		this.type = type;
		this.leftOSD = left;
		this.rightOSD = right;
        setMetadata(metadata);
	}
	
	// TODO: change api to receive relationtype_id instead of name.
	public Relation(Map<String,String> cmd){
		EntityManager em = HibernateSession.getLocalEntityManager();

		String name = cmd.get("name");	
		RelationTypeDAO relationTypeDAO = daoFactory.getRelationTypeDAO(em);
		type = relationTypeDAO.findByName(name);
		Logger log= LoggerFactory.getLogger(this.getClass());if(type == null){
			log.debug("Could not find RelationType");
			throw new CinnamonException("error.relation_type.not_found");
		}

        if(cmd.containsKey("metadata")){
            setMetadata(cmd.get("metadata"));
        }

		Long leftId=  ParamParser.parseLong(cmd.get("leftid"), "error.param.leftid");
		Long rightId= ParamParser.parseLong(cmd.get("rightid"), "error.param.rightid");
		
		ObjectSystemDataDAO osdDAO = daoFactory.getObjectSystemDataDAO(em);
		rightOSD = osdDAO.get(Long.parseLong( cmd.get("rightid")));		
		if(rightOSD == null){
			log.debug("Could not find rightOSD with id " + rightId);
			throw new CinnamonException("error.param.rightid");
		}
		leftOSD = osdDAO.get(Long.parseLong( cmd.get("leftid")));
		if(leftOSD == null){
			log.debug("leftOSD with id " + leftId +"not found");
			throw new CinnamonException("error.param.leftid");
		}
	}
	
	public long getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}

	public ObjectSystemData getLeft() {
		return leftOSD;
	}

	public void setLeft(ObjectSystemData left) {
		this.leftOSD = left;
	}

	public ObjectSystemData getRight() {
		return rightOSD;
	}

	public void setRight(ObjectSystemData right) {
		this.rightOSD = right;
	}

	public RelationType getType() {
		return type;
	}

	public void setType(RelationType type) {
		this.type = type;
	}

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        if(metadata == null || metadata.trim().length() == 0){
			this.metadata = "<meta/>";
		}
		else{
			ParamParser.parseXmlToDocument(metadata, "error.param.metadata");
			this.metadata = metadata;
		}
    }

    public void toXmlElement(Element root, Boolean includeMetadata){
        Element relation = root.addElement("relation");
		relation.addElement("id").addText(String.valueOf(getId()) );
		relation.addElement("leftId").addText( String.valueOf(leftOSD.getId()));
		relation.addElement("rightId").addText( String.valueOf(rightOSD.getId()));
		relation.addElement("type").addText(type.getName());
        if(includeMetadata){
            Node metadata = ParamParser.parseXml(getMetadata(), null);
            relation.add(metadata);
        }
    }

    public void toXmlElement(Element root){
	    toXmlElement(root, false);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relation)) return false;

        Relation relation = (Relation) o;

        if (leftOSD != null ? !leftOSD.equals(relation.leftOSD) : relation.leftOSD != null) return false;
        if (rightOSD != null ? !rightOSD.equals(relation.rightOSD) : relation.rightOSD != null) return false;
        if (type != null ? !type.equals(relation.type) : relation.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (leftOSD != null ? leftOSD.hashCode() : 0);
        result = 31 * result + (rightOSD != null ? rightOSD.hashCode() : 0);
        return result;
    }

    /**
     * Update relations of all OSDs of this OSD's version tree.
     * After a part of a relation has been updated, all other relations which target
     * this object's version tree have to be updated also because the change to one object may
     * require other relations to change.<br>
     * Example:<br>
     * An image, which is referenced by several relations from documents which
     * use it, is updated and increases its version number. Now all relations which
     * use the LatestHeadResolver need to update their link to this new version.
     * @param changedOsd OSD which has already been updated (or does not need an update).
     */
    public static void updateRelations(ObjectSystemData changedOsd){
        EntityManager em = HibernateSession.getLocalEntityManager();
        ObjectSystemDataDAO oDao = daoFactory.getObjectSystemDataDAO(em);
        RelationDAO rDao = daoFactory.getRelationDAO(em);
        for(ObjectSystemData osd : oDao.findAllVersions(changedOsd)){
            LoggerFactory.getLogger(Relation.class).debug("update required for OSD # "+osd.getId());
            for(Relation relation : rDao.findAllByLeft(osd)){
                relation.leftOSD = relation.getType().findOsdVersion(relation, osd, RelationSide.LEFT);
            }
            for(Relation relation : rDao.findAllByRight(osd)){
                relation.rightOSD = relation.getType().findOsdVersion(relation, osd, RelationSide.RIGHT);
            }
        }
    }

    // get/set leftOSD/rightOSD added as Grails/Hibernate seems to need this.
    public ObjectSystemData getLeftOSD() {
        return leftOSD;
    }

    public void setLeftOSD(ObjectSystemData leftOSD) {
        this.leftOSD = leftOSD;
    }

    public ObjectSystemData getRightOSD() {
        return rightOSD;
    }

    public void setRightOSD(ObjectSystemData rightOSD) {
        this.rightOSD = rightOSD;
    }
}
