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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.global.Constants;
import server.i18n.LocalMessage;
import server.global.Constants;
import utils.ParamParser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "objtypes",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class ObjectType implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private transient Element xmlNode;
	private long 	id;
	
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
	
	public ObjectType(){
		
	}
	
	public ObjectType(Map<String,String> cmd){
		Logger log= LoggerFactory.getLogger("server.ObjectType");log.debug("ctor");
		
		name		= cmd.get("name");
		description = cmd.get("description");		
	}

	public ObjectType(String name, String description){
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
		
	
	@Id @GeneratedValue
	@Column(name = "id")
	public long getId() {
		return id;
	}
	
	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}

	/**
	 * Add the ObjectType's fields as child-elements to a new element with the given name. 
	 * If the type parameter is null, simply return an empty element.
	 * @param elementName
	 * @param type
	 * @return the new element.
	 */
	public static Element asElement(String elementName, ObjectType type){
		Element e = DocumentHelper.createElement(elementName);
		if(type != null){
			if(type.xmlNode != null){
				type.xmlNode.setName(elementName);
				return (Element) ParamParser.parseXml(type.xmlNode.asXML(), null);
			}
			e.addElement("id").addText(String.valueOf(type.getId()));
			e.addElement("name").addText( LocalMessage.loc(type.getName()));
			e.addElement("sysName").addText(type.getName());
			e.addElement("description").addText(  LocalMessage.loc(type.getDescription()));
			type.xmlNode = e;
			e = (Element) ParamParser.parseXml(e.asXML(), null);
		}
		return e;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectType)) return false;

        ObjectType that = (ObjectType) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String toString(){
        return "ObjectType #"+id+": name="+name+" description="+description;
    }
}
