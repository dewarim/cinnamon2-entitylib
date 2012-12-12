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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.annotations.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.dao.DAOFactory;
import server.data.ObjectSystemData;
import server.exceptions.CinnamonConfigurationException;
import server.exceptions.CinnamonException;
import server.global.Constants;
import server.Relation;
import server.helpers.XmlDumper;
import server.resolver.RelationSide;
import server.interfaces.IRelationResolver;
import server.interfaces.IXmlDumper;
import utils.ParamParser;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;


@Entity
@Table(name = "relation_resolvers",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})}
)
public class RelationResolver
        implements Serializable, IXmlDumper {

    private static final long serialVersionUID = 1L;
    static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
    private transient Logger log = LoggerFactory.getLogger(getClass());

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "config",
            length = Constants.METADATA_SIZE,
            nullable = false
    )
    @Type(type = "text")
    private String config = "<config />";

    @Column(name = "class_name",
            nullable = false
    )
    private Class<? extends IRelationResolver> resolverClass;

    @Column(name = "name",
            nullable = false
    )
    private String name = "";

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version;

    public RelationResolver() {

    }

    public RelationResolver(Map<String,String> fields){
        this(fields.get("config"), fields.get("class_name"), fields.get("name"));
    }

    public RelationResolver(String config, String className, String name) {
        if(className == null){
            throw new CinnamonException("error.param.classname");
        }
        setConfig(config);
        this.name = name;
        try {
            resolverClass = (Class<? extends IRelationResolver>) Class.forName(className);
            // test if we can instantiate the resolver class:
            IRelationResolver resolverClassTest = resolverClass.newInstance();
        } catch (InstantiationException e) {
            throw new CinnamonException("error.instantiating.class", className);
        } catch (IllegalAccessException e) {
            throw new CinnamonException("error.accessing.class", className);
        } catch (ClassNotFoundException e) {
            throw new CinnamonException("error.loading.class", className);
        }
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        if (config == null || config.trim().length() == 0) {
            this.config = "<config />";
        }
        else {
            ParamParser.parseXmlToDocument(config, "error.param.config");
            this.config = config;
        }
    }

    public Class<? extends IRelationResolver> getResolverClass() {
        return resolverClass;
    }

    public void setResolverClass(Class<? extends IRelationResolver> resolverClass) {
        this.resolverClass = resolverClass;
    }

    public void toXmlElement(Element root) {
        Element relation = root.addElement("relation");
        relation.addElement("id").addText(String.valueOf(getId()));
        relation.addElement("configuration").addText(config);
        relation.addElement("resolverClass").addText(resolverClass.getName());
        relation.addElement("name").addText(name);
    }

    public String toString() {
        // return XmlDumper.dumpPrettyXml(this); // not ready because of Grails in Admin tool.
        return super.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ObjectSystemData resolveOsdVersion(Relation relation, ObjectSystemData changedOsd, RelationSide relationSide) {
        try {
            IRelationResolver resolver = resolverClass.newInstance();
            return resolver.resolveVersion(relation, changedOsd, config, relationSide);
        } catch (IllegalAccessException e) {
            throw new CinnamonException("error.instantiating.class", e, resolverClass.getName());
        } catch (InstantiationException e) {
            throw new CinnamonException("error.instantiating.class", e, resolverClass.getName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationResolver)) return false;

        RelationResolver that = (RelationResolver) o;

        if (config != null ? !config.equals(that.config) : that.config != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (resolverClass != null ? !resolverClass.equals(that.resolverClass) : that.resolverClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = config != null ? config.hashCode() : 0;
        result = 31 * result + (resolverClass != null ? resolverClass.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
