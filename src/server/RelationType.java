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
import server.data.ObjectSystemData;
import server.exceptions.CinnamonException;
import server.global.Constants;
import server.i18n.LocalMessage;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

import server.interfaces.IXmlDumper;
import server.resolver.RelationSide;
import server.dao.RelationResolverDAO;
import utils.HibernateSession;
import utils.ParamParser;

@Entity
@Table(name = "relationtypes",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})}
)
public class RelationType
        implements Serializable, IXmlDumper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private transient Logger log = LoggerFactory.getLogger(getClass());

    static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);

    @Id
    @GeneratedValue
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

    /**
     * If leftobjectprotected is true:
     * the left object of a relation of this type may not be deleted as long
     * as the relation exists.
     */
    @Column(name = "leftobjectprotected",
            nullable = false)
    private Boolean leftobjectprotected;

    /**
     * If rightobjectprotect is true:
     * the right object of a relation of this type may not be deleted as long
     * as the relation exists.
     */
    @Column(name = "rightobjectprotected",
            nullable = false)
    private Boolean rightobjectprotected;

    /**
     * If the right object of a relation of this type is copied,
     * the relation will also be copied if this field is true.
     */
    @Column(name = "clone_on_right_copy",
            nullable = false
    )
    private Boolean cloneOnRightCopy = false;

    /**
     * If the left object of a relation of this type is copied,
     * the relation will also be copied if this field is true.
     */
    @Column(name = "clone_on_left_copy",
            nullable = false
    )
    private Boolean cloneOnLeftCopy = false;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "left_resolver_id",
            nullable = false
    )
    private RelationResolver leftResolver;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "right_resolver_id",
            nullable = false
    )
    private RelationResolver rightResolver;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    public RelationType() {

    }

    public RelationType(String name, String description,
                        Boolean leftobjectprotected,
                        Boolean rightobjectprotected,
                        RelationResolver leftResolver,
                        RelationResolver rightResolver
    ) {
        this.name = name;
        this.description = description;
        this.leftobjectprotected = leftobjectprotected;
        this.rightobjectprotected = rightobjectprotected;
        this.leftResolver = leftResolver;
        this.rightResolver = rightResolver;
    }

    public RelationType(String name, String description,
                        Boolean leftobjectprotected,
                        Boolean rightobjectprotected,
                        RelationResolver leftResolver,
                        RelationResolver rightResolver,
                        Boolean cloneOnRightCopy,
                        Boolean cloneOnLeftCopy
    ) {
        this.name = name;
        this.description = description;
        this.leftobjectprotected = leftobjectprotected;
        this.rightobjectprotected = rightobjectprotected;
        this.leftResolver = leftResolver;
        this.rightResolver = rightResolver;
        this.cloneOnLeftCopy = cloneOnLeftCopy;
        this.cloneOnRightCopy = cloneOnRightCopy;
    }

    public RelationType(Map<String, String> cmd) {
        name = cmd.get("name");
        description = cmd.get("description");
        leftobjectprotected = cmd.get("leftobjectprotected").equals("true");
        rightobjectprotected = cmd.get("rightobjectprotected").equals("true");
        cloneOnLeftCopy = cmd.get("cloneOnLeftCopy").equals("true");
        cloneOnRightCopy = cmd.get("cloneOnRightCopy").equals("true");

        EntityManager em = HibernateSession.getLocalEntityManager();
        RelationResolverDAO rdd = daoFactory.getRelationResolverDAO(em);
        /*
         * Design note: resolve by name is intended to make it easier for testing
         * as you can create a test relation type without having to look up the id of the
         * default (or to-be-tested) relation resolver.
         */
        if (cmd.containsKey("right_resolver")) {
            rightResolver = rdd.findByName(cmd.get("right_resolver"));
        } else if (cmd.containsKey("right_resolver_id")) {
            rightResolver = rdd.get(ParamParser.parseLong(cmd.get("right_resolver_id"), "error.param.right_resolver_id"));
        } else {
            rightResolver = rdd.findByName(Constants.RELATION_RESOLVER_FIXED);
        }
        if (cmd.containsKey("left_resolver")) {
            leftResolver = rdd.findByName(cmd.get("left_resolver"));
        } else if (cmd.containsKey("left_resolver_id")) {
            rightResolver = rdd.get(ParamParser.parseLong(cmd.get("left_resolver_id"), "error.param.left_resolver_id"));
        } else {
            leftResolver = rdd.findByName(Constants.RELATION_RESOLVER_FIXED);
        }

        if (rightResolver == null) {
            throw new CinnamonException("error.param.right_resolver_id");
        }
        if (leftResolver == null){
            throw new CinnamonException("error.param.left_resolver_id");
        }

        log.debug("leftResolver: " + leftResolver);
        log.debug("rightResolver: " + rightResolver);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public Boolean isLeftobjectprotected() {
        return leftobjectprotected;
    }

    public void setLeftobjectprotected(Boolean leftobjectprotected) {
        this.leftobjectprotected = leftobjectprotected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isRightobjectprotected() {
        return rightobjectprotected;
    }

    public void setRightobjectprotected(Boolean rightobjectprotected) {
        this.rightobjectprotected = rightobjectprotected;
    }

    public RelationResolver getLeftResolver() {
        return leftResolver;
    }

    public void setLeftResolver(RelationResolver leftResolver) {
        this.leftResolver = leftResolver;
    }

    public RelationResolver getRightResolver() {
        return rightResolver;
    }

    public void setRightResolver(RelationResolver rightResolver) {
        this.rightResolver = rightResolver;
    }

    public void toXmlElement(Element root) {
        Element rt = root.addElement("relationType");
        rt.addElement("id").addText(String.valueOf(getId()));
        rt.addElement("name").addText(LocalMessage.loc(getName()));
        rt.addElement("sysName").addText(getName());
        rt.addElement("description").addText(LocalMessage.loc(getDescription()));
        rt.addElement("rightobjectprotected").addText(rightobjectprotected.toString());
        rt.addElement("leftobjectprotected").addText(leftobjectprotected.toString());
        rt.addElement("cloneOnLeftCopy").addText(cloneOnLeftCopy.toString());
        rt.addElement("cloneOnRightCopy").addText(cloneOnRightCopy.toString());
        rt.addElement("leftResolver").addText(leftResolver.getName());
        rt.addElement("rightResolver").addText(rightResolver.getName());
    }

    public String toString() {
        return "RelationResolver::name:" + getName();
    }

    public Boolean getCloneOnRightCopy() {
        return cloneOnRightCopy;
    }

    public void setCloneOnRightCopy(Boolean cloneOnRightCopy) {
        this.cloneOnRightCopy = cloneOnRightCopy;
    }

    public Boolean getCloneOnLeftCopy() {
        return cloneOnLeftCopy;
    }

    public void setCloneOnLeftCopy(Boolean cloneOnLeftCopy) {
        this.cloneOnLeftCopy = cloneOnLeftCopy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationType)) return false;

        RelationType that = (RelationType) o;

        if (id != that.id) return false;
        if (cloneOnLeftCopy != null ? !cloneOnLeftCopy.equals(that.cloneOnLeftCopy) : that.cloneOnLeftCopy != null)
            return false;
        if (cloneOnRightCopy != null ? !cloneOnRightCopy.equals(that.cloneOnRightCopy) : that.cloneOnRightCopy != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (leftResolver != null ? !leftResolver.equals(that.leftResolver) : that.leftResolver != null) return false;
        if (leftobjectprotected != null ? !leftobjectprotected.equals(that.leftobjectprotected) : that.leftobjectprotected != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (rightResolver != null ? !rightResolver.equals(that.rightResolver) : that.rightResolver != null)
            return false;
        if (rightobjectprotected != null ? !rightobjectprotected.equals(that.rightobjectprotected) : that.rightobjectprotected != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * Find the correct OSD version for a given relation side.
     *
     * @param relation     The relation for which the correct osd version is needed.
     * @param osd          the OSD whose version needs to be determined.
     * @param relationSide the side of the relation for which you need the OSD version to be resolved.
     * @return the version as found by the Resolver class.
     */
    ObjectSystemData findOsdVersion(Relation relation, ObjectSystemData osd, RelationSide relationSide) {
        switch (relationSide) {
            case LEFT:
                return leftResolver.resolveOsdVersion(relation, osd, relationSide);
            case RIGHT:
                return rightResolver.resolveOsdVersion(relation, osd, relationSide);
        }
        return null; // is never reached unless RelationSide is null. And then you are up a certain creek without a paddle anyway.
    }
}
