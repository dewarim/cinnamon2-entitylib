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
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.global.Constants;
import server.i18n.LocalMessage;
import utils.ParamParser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "config_entries",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class ConfigEntry implements Serializable {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

    private transient Logger log = LoggerFactory.getLogger(this.getClass());

    @Id
    @GeneratedValue
	@Column(name = "id")
	private long 	id;

	@Column(name = "name",
			length = Constants.NAME_LENGTH,
			nullable = false)
	private String name;

	@Column(name = "config",
			length = Constants.METADATA_SIZE,
			nullable = false)
	private String config = "<config />";

	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;

	public ConfigEntry(){

	}

	public ConfigEntry(Map<String, String> cmd){
		name    = cmd.get("name");
		setConfig(cmd.get("config"));
	}

	public ConfigEntry(String name, String config){
		this.name = name;
		this.config = config;
	}

    public Node parseConfig(){
        try{
            return ParamParser.parseXml(config, null);
        }
        catch (Exception e){
            log.debug("Failed to parse config: ",e);
            return null;
        }
    }


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        if(config == null || config.trim().length() == 0){
            this.config = "<config />";
        }
        else{
            ParamParser.parseXmlToDocument(config, "error.param.config");
            this.config = config;
        }
    }

    public long getId() {
		return id;
	}
	
	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}

	/**
	 * Add the ConfigEntry's fields as child-elements to a new element with the given name.
	 * If the entry parameter is null, simply return an empty element.
	 * @param elementName the name of the XML element into which the data is stored.
	 * @param entry the ConfigEntry to be converted to an Element
	 * @return the new element.
	 */
	public static Element asElement(String elementName, ConfigEntry entry){
        Element e = DocumentHelper.createElement(elementName);
		if(entry != null){
			e.addElement("id").addText(String.valueOf(entry.getId()));
			e.addElement("name").addText( LocalMessage.loc(entry.getName()));
            e.addElement("config").addText(entry.getConfig());
		}
		return e;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigEntry)) return false;

        ConfigEntry that = (ConfigEntry) o;

        if (config != null ? !config.equals(that.config) : that.config != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
