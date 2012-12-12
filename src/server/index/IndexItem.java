package server.index;

import org.apache.lucene.document.Document;
import org.dom4j.Element;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.dao.DAOFactory;
import server.dao.IndexGroupDAO;
import server.dao.IndexTypeDAO;
import server.global.Constants;
import server.i18n.LocalMessage;
import utils.HibernateSession;
import utils.ParamParser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name="index_items",
		uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}		
)
public class IndexItem implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
    static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);


	@Transient
	transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Id @GeneratedValue
	@Column(name = "id")
	long id;
	
	@Version
	@Column(name="obj_version",
			nullable = false)
	@SuppressWarnings("unused")
	private Long obj_version = 0L;
	
	@Column(name = "name",
			length = Constants.NAME_LENGTH,
			nullable = false)
//	@Type(type="text")
	String name;
	
	@Column(name = "search_string",
			length = Constants.XPATH_LENGTH,
			nullable = false)
	@Type(type="text")
	String searchString = "";
	
	/**
	 * searchCondition is an XPath expression that evaluates to true
	 * if the IndexItem should try to index the given document.<br/>
	 * The XPath will be applied successively to the metadata, system 
	 * metadata and content (depending on forContent, forMetadata and
	 * forSysMetadata)
	 * and if any of those returns true, the item
	 * will be indexed by using the search_string.
	 */
	@Column(name = "search_condition",
			length = Constants.XPATH_LENGTH,
			nullable = false)
	@Type(type="text")
	String searchCondition = "true()";
	
	@Column(name = "fieldname",
			length = 255,
			nullable = false)
//	@Type(type="text")
	String fieldname = "content";
	
	@ManyToOne
	@JoinColumn(name = "index_type_id",
				nullable = false)
    IndexType indexType;

	@Column(name = "system_index",
			nullable = false)
	Boolean	systemic = false;
	
	@ManyToOne
	@JoinColumn(name = "index_group_id",
				nullable = false)
    IndexGroup indexGroup;
	
	@Column(name = "for_content",
			nullable = false)
	Boolean forContent = false;
	
	@Column(name = "for_metadata",
			nullable = false)
	Boolean forMetadata = false;
	
	@Column(name = "for_sysmeta",
			nullable = false)
	Boolean forSysMeta = false;
	
	/**
	 * If an indexItem can generate multiple distinct results, you should set this
	 * flag to true. For example, an XML structure containing a list of "filename"
	 * elements which is queries by an xpath statement of //filename will generate a
	 * list of nodes, which should all be indexed. On the other hand, a UUID search
	 * field will only contain 1 result. 
	 */
	@Column(name = "multiple_results",
			nullable = false)
	Boolean multipleResults = false;
	
	/**
	 * Each IndexType is connected to a specific ValueAssistanceProvider, which is called
	 * upon to provide a list of possible values for a search field on the client.<br/>
	 * For example, if you index a field "birthday", the acceptable values represent
	 * to a certain date in the past - the client should not submit search requests for
	 * values like "birthday=John" but rather tell the user to correct the input. 
	 */
	@Column(name = "va_params",
			length = Constants.XML_PARAMS,
			nullable = false)
	@Type(type="text")		
	String vaProviderParams = "<vaParams />";
	
	public IndexItem(){
		
	}

    public IndexItem(Map<String, String> fields){
        name = fields.get("name");
        searchString = fields.get("search_string");
        searchCondition = fields.get("search_condition");
        fieldname = fields.get("fieldname");
        vaProviderParams = fields.get("va_provider_params");
        EntityManager em = HibernateSession.getLocalEntityManager();
        IndexGroupDAO igDao = daoFactory.getIndexGroupDAO(em);
        indexGroup = igDao.get(fields.get("index_group_id"));
        IndexTypeDAO itDao = daoFactory.getIndexTypeDAO(em);
        indexType = itDao.get(fields.get("index_type_id"));
        multipleResults = fields.get("multiple_results").equals("true");
        systemic = fields.get("systemic").equals("true");
        forContent = fields.get("for_content").equals("true");
        forMetadata = fields.get("for_metadata").equals("true");
        forSysMeta = fields.get("for_sys_meta").equals("true");
    }

	public IndexItem(String name, String xpath, String searchCondition, String fieldname, IndexType indexType,
			Boolean multipleResults, String vaParams, Boolean systemic, IndexGroup indexGroup,
			Boolean forContent, Boolean forMetadata, Boolean forSysMeta){
		this.name = name;
		this.searchString = xpath;
		this.searchCondition = searchCondition;
		this.fieldname = fieldname;
		this.indexType = indexType;
		this.multipleResults = multipleResults;
		this.vaProviderParams = vaParams;
		this.systemic = systemic;
		this.indexGroup = indexGroup;
		this.forContent = forContent;
		this.forMetadata = forMetadata;
		this.forSysMeta = forSysMeta;
	}
	
	/*
	 * revision 1040
	 * A note on class design: this class tries to adhere to the principle of strict
	 * encapsulation, so access to fields inside the class is without
	 * getters/setters at the moment. It's an experiment, and you may refactor
	 * the class if you need to. An alternative would be to only use private getter/setters,
	 * but that will only be useful if this class gets validation logic on get/set
	 * or something like that.
	 */
	public void toXmlElement(Element root){
		Element item = root.addElement("indexItem");
		item.addElement("id").addText(String.valueOf(id));
		item.addElement("name").addText(name);
		item.addElement("searchString").addText(searchString);
		item.addElement("fieldname").addText(fieldname);
		item.addElement("fieldDisplayName").addText(LocalMessage.loc(fieldname));
		indexType.toXmlElement(item);
		item.addElement("multipleResults").addText(multipleResults.toString());		
		item.add(ParamParser.parseXml(vaProviderParams, "error.param.vaParams"));
		item.addElement("systemic").addText(systemic.toString());
		item.addElement("forMetadata").addText(forMetadata.toString());
		item.addElement("forContent").addText(forContent.toString());
		item.addElement("forSysMeta").addText(forSysMeta.toString());
		indexGroup.toXmlElement(item);
	}

    public void indexObject(ContentContainer content, ContentContainer metadata, ContentContainer systemMetadata, Document doc){
		ContentContainer[] params = {content, metadata, systemMetadata};
		if(! checkCondition(params)){
			log.debug("checkCondition returned false");
			return;
		}
        log.debug("checkCondition returned true");
        log.debug("searchString: "+searchString);
        log.debug("fieldname: "+fieldname);

		if(forContent){
//            log.trace("searching on content:\n"+content.asString());
			indexType.indexContent(content, doc, fieldname, searchString, multipleResults);
		}
		if(forSysMeta){
//            log.debug("sysMeta:\n "+systemMetadata.asString());
			indexType.indexSysMeta(systemMetadata, doc, fieldname, searchString, multipleResults);
		}
		if(forMetadata){            
			indexType.indexMetadata(metadata, doc, fieldname, searchString, multipleResults);
		}
	}
	
	/**
	 * Check if one of the given parameter Strings has a positive result for the
	 * xpath expression in searchCondition.
	 * @param params an array of strings which contain XML documents.
	 * @return true if one of the strings resulted in a positive match for searchCondition.
	 */
	public Boolean checkCondition(ContentContainer[] params){
		Boolean result = false;
		for(ContentContainer xml : params){
			try{
                // TODO: possibly define size limits.
				org.dom4j.Document indexObject = xml.asDocument();
				log.debug("checkCondition "+searchCondition+": "+indexObject.valueOf(searchCondition)) ;
				if(indexObject.valueOf(searchCondition).equals("true")){
					result = true;
					break;
				}				
			}
			catch (Exception e) {
				log.debug("checkCondition: ",e.getMessage());
			}
		}
		return result;
	}
	
	public Long getId(){
		return id;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchCondition() {
        return searchCondition;
    }

    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public Boolean getSystemic() {
        return systemic;
    }

    public void setSystemic(Boolean systemic) {
        this.systemic = systemic;
    }

    public IndexGroup getIndexGroup() {
        return indexGroup;
    }

    public void setIndexGroup(IndexGroup indexGroup) {
        this.indexGroup = indexGroup;
    }

    public Boolean getForContent() {
        return forContent;
    }

    public void setForContent(Boolean forContent) {
        this.forContent = forContent;
    }

    public Boolean getForMetadata() {
        return forMetadata;
    }

    public void setForMetadata(Boolean forMetadata) {
        this.forMetadata = forMetadata;
    }

    public Boolean getForSysMeta() {
        return forSysMeta;
    }

    public void setForSysMeta(Boolean forSysMeta) {
        this.forSysMeta = forSysMeta;
    }

    public Boolean getMultipleResults() {
        return multipleResults;
    }

    public void setMultipleResults(Boolean multipleResults) {
        this.multipleResults = multipleResults;
    }

    public String getVaProviderParams() {
        return vaProviderParams;
    }

    public void setVaProviderParams(String vaProviderParams) {
        if(vaProviderParams == null || vaProviderParams.trim().length() == 0){
            this.vaProviderParams = "<vaParams />";
        }
        else{
            ParamParser.parseXmlToDocument(vaProviderParams, "error.param.vaParams");
            this.vaProviderParams = vaProviderParams;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexItem)) return false;

        IndexItem indexItem = (IndexItem) o;

        if (fieldname != null ? !fieldname.equals(indexItem.fieldname) : indexItem.fieldname != null) return false;
        if (forContent != null ? !forContent.equals(indexItem.forContent) : indexItem.forContent != null) return false;
        if (forMetadata != null ? !forMetadata.equals(indexItem.forMetadata) : indexItem.forMetadata != null)
            return false;
        if (forSysMeta != null ? !forSysMeta.equals(indexItem.forSysMeta) : indexItem.forSysMeta != null) return false;
        if (indexGroup != null ? !indexGroup.equals(indexItem.indexGroup) : indexItem.indexGroup != null) return false;
        if (indexType != null ? !indexType.equals(indexItem.indexType) : indexItem.indexType != null) return false;
        if (multipleResults != null ? !multipleResults.equals(indexItem.multipleResults) : indexItem.multipleResults != null)
            return false;
        if (name != null ? !name.equals(indexItem.name) : indexItem.name != null) return false;
        if (searchCondition != null ? !searchCondition.equals(indexItem.searchCondition) : indexItem.searchCondition != null)
            return false;
        if (searchString != null ? !searchString.equals(indexItem.searchString) : indexItem.searchString != null)
            return false;
        if (systemic != null ? !systemic.equals(indexItem.systemic) : indexItem.systemic != null) return false;
        if (vaProviderParams != null ? !vaProviderParams.equals(indexItem.vaProviderParams) : indexItem.vaProviderParams != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
