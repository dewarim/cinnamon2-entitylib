package server.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A Table of index-jobs for the Lucene Indexer.
 */
@Entity
@Table(name = "index_jobs")
public class IndexJob implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Transient
    transient Logger log = LoggerFactory.getLogger(this.getClass());

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "indexable_class",
            nullable = false)
    Class<? extends Indexable> indexableClass;

    
    @Column(name = "indexable_id",
            nullable = false)
    Long indexableId;

    @Column(name = "failed",
            nullable = false)
    Boolean failed = false;

    public IndexJob() {

    }

    public IndexJob(Indexable indexable){
        this.indexableClass = indexable.getClass();
        this.indexableId = indexable.myId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Class<? extends Indexable> getIndexableClass() {
        return indexableClass;
    }

    public void setIndexableClass(Class<? extends Indexable> indexableClass) {
        this.indexableClass = indexableClass;
    }

    public Long getIndexableId() {
        return indexableId;
    }

    public void setIndexableId(Long indexableId) {
        this.indexableId = indexableId;
    }

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexJob)) return false;

        IndexJob indexJob = (IndexJob) o;

        if (failed != null ? !failed.equals(indexJob.failed) : indexJob.failed != null) return false;
        if (indexableClass != null ? !indexableClass.equals(indexJob.indexableClass) : indexJob.indexableClass != null)
            return false;
        if (indexableId != null ? !indexableId.equals(indexJob.indexableId) : indexJob.indexableId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return indexableId != null ? indexableId.hashCode() : 0;
    }
}
