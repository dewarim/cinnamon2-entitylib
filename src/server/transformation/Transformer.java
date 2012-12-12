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

package server.transformation;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Format;
import server.dao.DAOFactory;
import server.dao.FormatDAO;
import server.exceptions.CinnamonException;
import server.global.Constants;
import server.i18n.LocalMessage;
import server.transformation.ITransformer;
import utils.HibernateSession;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "transformers",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class Transformer implements Serializable {

	private static final long	serialVersionUID	= 1L;

	static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
	
	@Id @GeneratedValue
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
	
	@Column(name = "transformer_class",
			length = Constants.NAME_LENGTH,
			nullable = false)
	private Class<? extends ITransformer> transformerClass;
	
	@ManyToOne
	@JoinColumn(name = "source_format_id",
			nullable = false
	)
	private Format sourceFormat;
	
	@ManyToOne
	@JoinColumn(name = "target_format_id",
			nullable = false
	)
	private Format targetFormat;
	
	@Version
	@Column(name="obj_version")
	@SuppressWarnings("unused")
	private Long obj_version = 0L;
	
	public Transformer(){
		
	}
	
	@SuppressWarnings("unchecked")
	public Transformer(Map<String,String> cmd){
		Logger log=LoggerFactory.getLogger(this.getClass());log.debug("ctor");
		name		= cmd.get("name");
		description = cmd.get("description");
		try{
			transformerClass = (Class<? extends ITransformer>) Class.forName(cmd.get("transformer_class"));
		}
		catch (ClassNotFoundException e) {
			throw new CinnamonException("error.loading.class",e);
		}
		EntityManager em = HibernateSession.getLocalEntityManager();	
		FormatDAO fDao = daoFactory.getFormatDAO(em);
		sourceFormat = fDao.get(cmd.get("source_format_id"));
		targetFormat = fDao.get(cmd.get("target_format_id"));
	}

	public Transformer(String name, String description, Class<? extends ITransformer> transformerClass,
			Format sourceFormat, Format targetFormat){
		this.name = name;
		this.description = description;
		this.transformerClass = transformerClass;
		this.sourceFormat = sourceFormat;
		this.targetFormat = targetFormat;
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
	 * Add the Transformer's fields as child-elements to a new element with the given name. 
	 * If the type parameter is null, simply return an empty element.
	 * @param elementName
	 * @param transformer
	 * @return the new element.
	 */
	public static Element asElement(String elementName, Transformer transformer){
		Element e = DocumentHelper.createElement(elementName);
		if(transformer != null){
			e.addElement("id").addText(String.valueOf(transformer.getId()));
			e.addElement("name").addText( LocalMessage.loc(transformer.getName()));
			e.addElement("description").addText( LocalMessage.loc(transformer.getDescription()));
			e.addElement("transformerClass").addText(transformer.transformerClass.getName());
			e.add(Format.asElement("sourceFormat", transformer.getSourceFormat()));
			e.add(Format.asElement("targetFormat", transformer.getSourceFormat()));
		}
		return e;
	}

	/**
	 * @return the transformerClass
	 */
	public Class<? extends ITransformer> getTransformerClass() {
		return transformerClass;
	}

	/**
	 * @param transformerClass the transformerClass to set
	 */
	public void setTransformerClass(Class<? extends ITransformer> transformerClass) {
		this.transformerClass = transformerClass;
	}

	/**
	 * @return the sourceFormat
	 */
	public Format getSourceFormat() {
		return sourceFormat;
	}

	/**
	 * @param sourceFormat the sourceFormat to set
	 */
	public void setSourceFormat(Format sourceFormat) {
		this.sourceFormat = sourceFormat;
	}

	/**
	 * @return the targetFormat
	 */
	public Format getTargetFormat() {
		return targetFormat;
	}

	/**
	 * @param targetFormat the targetFormat to set
	 */
	public void setTargetFormat(Format targetFormat) {
		this.targetFormat = targetFormat;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transformer)) return false;

        Transformer that = (Transformer) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (sourceFormat != null ? !sourceFormat.equals(that.sourceFormat) : that.sourceFormat != null) return false;
        if (targetFormat != null ? !targetFormat.equals(that.targetFormat) : that.targetFormat != null) return false;
        if (transformerClass != null ? !transformerClass.equals(that.transformerClass) : that.transformerClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
