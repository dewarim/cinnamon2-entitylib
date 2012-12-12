package server.index;

import org.apache.lucene.document.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.exceptions.CinnamonException;
import server.global.Constants;
import server.i18n.LocalMessage;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Distinguish multiple IndexItems by their type.
 */

@Entity
@Table(name = "index_types",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})}
)
public class IndexType implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Transient
    transient Logger log = LoggerFactory.getLogger(this.getClass());

    public enum DataType {
        STRING, BOOLEAN, DATE_TIME, INTEGER, DECIMAL, TIME, TEXT
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    long id;

    @Version
    @Column(name = "obj_version",
            nullable = false)
    @SuppressWarnings("unused")
    private Long obj_version = 0L;

    @Column(name = "name",
            length = Constants.NAME_LENGTH,
            nullable = false)
//	@Type(type="text")
            String name;

    @Column(name = "indexer_class",
            nullable = false)
    Class<? extends Indexer> indexerClass;

    @Column(name = "va_provider_class",
            nullable = false)
    Class<? extends ValueAssistanceProvider> vaProviderClass;

    @Column(name = "data_type",
            nullable = false)
    @Enumerated(EnumType.STRING)
    DataType dataType;

    public IndexType() {

    }

    public IndexType(Map<String, String> fields) {
        try {
            name = fields.get("name");
            indexerClass = (Class<Indexer>) Class.forName(fields.get("indexer_class"));
            vaProviderClass = (Class<ValueAssistanceProvider>) Class.forName(fields.get("va_provider_class"));
            dataType = DataType.valueOf(fields.get("data_type"));
        } catch (ClassNotFoundException e) {
            throw new CinnamonException("error.class.not.found", e.getMessage());
        }
    }

    public IndexType(String name, Class<? extends Indexer> indexerClass,
                     Class<? extends ValueAssistanceProvider> vaProviderClass,
                     DataType dataType) {
        this.name = name;
        this.indexerClass = indexerClass;
        this.dataType = dataType;
        this.vaProviderClass = vaProviderClass;
    }

    public void toXmlElement(Element root) {
        Element type = root.addElement("indexType");
        type.addElement("id").addText(String.valueOf(id));
        type.addElement("name").addText(LocalMessage.loc(name));
        type.addElement("indexerClass").addText(indexerClass.getName());
        type.addElement("dataType").addText(dataType.toString());
        type.addElement("vaProviderClass").addText(vaProviderClass.getName());
    }

    public Indexer getIndexer() {
        Indexer indexer;
        try {
            indexer = indexerClass.newInstance();
        }
        catch (InstantiationException e) {
            throw new CinnamonException("error.instantiating.class", e, indexerClass.getName());
        } catch (IllegalAccessException e) {
            throw new CinnamonException("error.accessing.class", e, indexerClass.getName());
        }
        return indexer;
    }


    public void indexContent(ContentContainer content, Document doc, String fieldname, String searchString, Boolean multipleResults) {
        getIndexer().indexObject(content, doc, fieldname, searchString, multipleResults);
    }

    public void indexSysMeta(ContentContainer sysMeta, Document doc, String fieldname, String searchString, Boolean multipleResults) {
        getIndexer().indexObject(sysMeta, doc, fieldname, searchString, multipleResults);
    }

    public void indexMetadata(ContentContainer metadata, Document doc, String fieldname, String searchString, Boolean multipleResults) {
        getIndexer().indexObject(metadata, doc, fieldname, searchString, multipleResults);
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setIndexerClass(String className) throws ClassNotFoundException {
        this.indexerClass = (Class<? extends Indexer>) Class.forName(className);
    }

    public void setVaProviderClass(String className) throws ClassNotFoundException {
        this.vaProviderClass = (Class<? extends ValueAssistanceProvider>) Class.forName(className);
    }

    public void setIndexerClass(Class<? extends Indexer> indexerClass) {
        this.indexerClass = indexerClass;
    }

    public void setVaProviderClass(Class<? extends ValueAssistanceProvider> vaProviderClass) {
        this.vaProviderClass = vaProviderClass;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Indexer> getIndexerClass() {
        return indexerClass;
    }

    public Class<? extends ValueAssistanceProvider> getVaProviderClass() {
        return vaProviderClass;
    }

    public DataType getDataType() {
        return dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexType)) return false;

        IndexType indexType = (IndexType) o;

        if (dataType != indexType.dataType) return false;
        if (indexerClass != null ? !indexerClass.equals(indexType.indexerClass) : indexType.indexerClass != null)
            return false;
        if (name != null ? !name.equals(indexType.name) : indexType.name != null) return false;
        if (vaProviderClass != null ? !vaProviderClass.equals(indexType.vaProviderClass) : indexType.vaProviderClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
