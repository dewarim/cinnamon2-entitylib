package server.index;

/**
 * Configuration items for the TikaParser class.
 * When content is set on an object, the parser checks if this should be handled by Tika.
 * It searches the TikaConfig table for an item matching the attributes of the object,
 * and if it finds one, TikaParser will try to extract the relevant information from the
 * content and add the extract to the custom metadata field as XML, so it can be indexed
 * by Lucene.
 */
import server.Format;
import server.ObjectType;
import server.global.Constants;

import javax.persistence.*;

@Entity
@Table(name = "tika_config",
        uniqueConstraints = {@UniqueConstraint(columnNames={"name"})}
)
public class TikaConfig {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id = 0;

    @Column(name = "name",
            length = Constants.NAME_LENGTH,
            nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "format_id",
            nullable = true
    )
    private Format format;

    @ManyToOne
    @JoinColumn(name = "object_type_id",
            nullable = true
    )
    private ObjectType objectType;

    public TikaConfig() {
    }

    public TikaConfig(String name, Format format, ObjectType objectType) {
        this.name = name;
        this.format = format;
        this.objectType = objectType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TikaConfig)) return false;

        TikaConfig that = (TikaConfig) o;

        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (objectType != null ? !objectType.equals(that.objectType) : that.objectType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
        return result;
    }
}
