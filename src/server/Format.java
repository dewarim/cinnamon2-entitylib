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
import utils.ParamParser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "formats",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class Format implements Serializable{

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
	
	@Column(name = "extension",
			length = 64,
			nullable = false)
	private String extension;
	
	@Column(name = "contenttype",
			length = 128,
			nullable = false)
	private String contenttype;
	
	@Column(name = "description",
			length = Constants.DESCRIPTION_SIZE,
			nullable = false)
	private String description;
	
	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;
	
	public Format(){
		
	}
	
	public Format(String name, String extension, String contenttype, String description){
		this.name = name;
		this.extension = extension;
		this.contenttype = contenttype;
		this.description = description;
	}
	
	public Format(Map<String,String> cmd){
			name		= cmd.get("name");
			extension 	= cmd.get("extension");
			contenttype = cmd.get("contenttype");
			description = cmd.get("description");		
	}
		
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContenttype() {
		return contenttype;
	}
	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
		
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
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
	 * Add the Format's fields as child-elements to a new element with the given name. 
	 * If the format is null, simply return an empty element.
	 * @param elementName
	 * @param format
	 * @return the new element.
	 */
	public static Element asElement(String elementName, Format format){
		Element e = DocumentHelper.createElement(elementName);
		if(format != null){
			if(format.xmlNode != null){
				format.xmlNode.setName(elementName);
				return (Element) ParamParser.parseXml(format.xmlNode.asXML(), null);
			}
			e.addElement("id").addText(String.valueOf(format.getId()));
			e.addElement("name").addText( LocalMessage.loc(format.getName()));
            e.addElement("sysName").addText(format.getName());
			e.addElement("description").addText(  LocalMessage.loc(format.getDescription()));
			e.addElement("contentType").addText(format.getContenttype());
			e.addElement("extension").addText(format.getExtension());
			format.xmlNode = e;
			e = (Element) ParamParser.parseXml(e.asXML(), null);
		}
		return e;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Format)) return false;

        Format format = (Format) o;

        if (contenttype != null ? !contenttype.equals(format.contenttype) : format.contenttype != null) return false;
        if (description != null ? !description.equals(format.description) : format.description != null) return false;
        if (extension != null ? !extension.equals(format.extension) : format.extension != null) return false;
        if (name != null ? !name.equals(format.name) : format.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (contenttype != null ? contenttype.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
