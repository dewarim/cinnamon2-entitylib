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
import server.global.Constants;
import server.i18n.LocalMessage;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "folder_types",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class FolderType implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

    @Id
    @GeneratedValue
	@Column(name = "id")
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
	
	public FolderType(){
		
	}
	
	public FolderType(Map<String,String> cmd){
		name		= cmd.get("name");
		description = cmd.get("description");		
	}

	public FolderType(String name, String description){
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
		
	public long getId() {
		return id;
	}
	
	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}

	/**
	 * Add the FolderType's fields as child-elements to a new element with the given name. 
	 * If the type parameter is null, simply return an empty element.
	 * @param elementName the parent element name for the folder type's elements.
	 * @param type the folder type that is going to be serialized to XML.
	 * @return the new element.
	 */
	public static Element asElement(String elementName, FolderType type){
		Element e = DocumentHelper.createElement(elementName);
		if(type != null){
			e.addElement("id").addText(String.valueOf(type.getId()));
			e.addElement("name").addText( LocalMessage.loc(type.getName()));
            e.addElement("sysName").addText(type.getName());
			e.addElement("description").addText( LocalMessage.loc(type.getDescription()));
		}
		return e;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FolderType)) return false;

        FolderType that = (FolderType) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String toString(){
        return "FolderType #"+id+" "+name+" ("+description+")";
    }
}
