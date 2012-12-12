package server.i18n;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.dao.DAOFactory;
import server.dao.UiLanguageDAO;
import utils.HibernateSession;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/*
 * The message class is used for localized messages of the user interface.
 */

@Entity
@Table(name = "messages",
		uniqueConstraints = {@UniqueConstraint(columnNames={"message", "ui_language_id"})}
)
public class Message implements Serializable{

	private static final long	serialVersionUID	= 1L;
	static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);

	@SuppressWarnings("unused")
	private transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Id	@GeneratedValue
	@Column(name = "id")
	private long id;
	
	@Column(name = "message",
			nullable = false)
	private String message;
	
	@ManyToOne
	@JoinColumn(name = "ui_language_id",
			nullable = false)
	private UiLanguage language;
	
	@Column(name = "translation",
			nullable=false)
	private String translation;
	
	
	public Message(){}

    public Message(Map<String,String> fields){
        message = fields.get("message");
        translation = fields.get("translation");
        UiLanguageDAO lDao = daoFactory.getUiLanguageDAO(HibernateSession.getLocalEntityManager());
        language = lDao.get(fields.get("ui_language_id"));
    }

	public Message(String message, UiLanguage language, String translation){
		this.message = message;
		this.language = language;
		this.translation = translation;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	@SuppressWarnings("unused")
	private void setId(long id) {
		this.id = id;
	}

    public UiLanguage getLanguage() {
        return language;
    }

    public void setLanguage(UiLanguage language) {
        this.language = language;
    }

    /**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the translation
	 */
	public String getTranslation() {
		return translation;
	}

	/**
	 * @param translation the translation to set
	 */
	public void setTranslation(String translation) {
		this.translation = translation;
	}
	
	public void toXmlElement(Element root){
		Element msg = root.addElement("message");
		msg.addElement("id").addText(String.valueOf(id));
		msg.addElement("message").addText( message);
		msg.addElement("languageId").addText( String.valueOf(language.getId()));
		msg.addElement("translation").addText( translation );
	}
	
}
