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

package server.i18n;

import org.dom4j.Element;
import org.hibernate.annotations.Type;
import server.dao.DAOFactory;
import server.dao.LanguageDAO;
import server.exceptions.CinnamonConfigurationException;
import server.global.Constants;
import server.i18n.LocalMessage;
import utils.HibernateSession;
import utils.ParamParser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "languages",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"iso_code"})}
)
public class Language implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int MAX_ISO_CODE_LENGTH = 32;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "iso_code",
            length = MAX_ISO_CODE_LENGTH,
            nullable = false)
    private String isoCode;

    @Version
    @Column(name = "obj_version")
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    @Column(name = "metadata",
            length = Constants.METADATA_SIZE,
            nullable = false
    )
    @Type(type = "text")
    private String metadata = "<meta />";


    public Language() {

    }

    public Language(String isoCode) {
        this.isoCode = isoCode;
    }

    public Language(String isoCode, String metadata) {
        this.isoCode = isoCode;
        setMetadata(metadata);
    }

    public Language(Map<String, String> cmd) {
        isoCode = cmd.get("iso_code");
        setMetadata(cmd.get("metadata"));
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        if (metadata == null || metadata.trim().length() == 0) {
            this.metadata = "<meta/>";
        }
        else {
            ParamParser.parseXmlToDocument(metadata, "error.param.metadata");
            this.metadata = metadata;
        }
    }

    /**
     * @return the isoCode
     */
    public String getIsoCode() {
        return isoCode;
    }

    /**
     * @param isoCode the isoCode to set
     */
    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public long getId() {
        return id;
    }


    @SuppressWarnings("unused")
    private void setId(long id) {
        this.id = id;
    }

    public void toXmlElement(Element root) {
        Element lang = root.addElement("language");
        lang.addElement("id").addText(String.valueOf(id));
        lang.addElement("sysName").addText(isoCode);
        lang.addElement("name").addText(LocalMessage.loc(getIsoCode()));
        lang.add(ParamParser.parseXml(getMetadata(), null));
    }

    /**
     * Find the default language. At the moment, this is hardcoded to "und", the
     * undetermined language.
     * TODO: set default language by Config.
     *
     * @return the default language or null
     */
    public static Language getDefaultLanguage() {
        Language lang;
        try {
            DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
            EntityManager em = HibernateSession.getLocalEntityManager();
            LanguageDAO langDao = daoFactory.getLanguageDAO(em);

            lang = langDao.findByIsoCode("und");
        } catch (NoResultException e) {
            throw new CinnamonConfigurationException("No default language configured! You must at least configure 'und' for undetermined language.");
        }
        return lang;
    }

}
