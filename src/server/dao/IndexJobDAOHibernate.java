package server.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.index.IndexJob;

import javax.persistence.Query;
import java.util.List;

public class IndexJobDAOHibernate extends GenericHibernateDAO<IndexJob, Long> implements IndexJobDAO {
	
	transient Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public IndexJob get(Long id) {
		return getSession().find(IndexJob.class, id);
	}

	/* (non-Javadoc)
	 * @see server.dao.PermissionDAO#list()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<IndexJob> list(Integer max) {
		Query q = getSession().createQuery("select i from IndexJob i order by i.id");
        q.setMaxResults(max);
		return q.getResultList();
	}

    /* (non-Javadoc)
 * @see server.dao.PermissionDAO#list()
 */
    @SuppressWarnings("unchecked")
    @Override
    public List<IndexJob> list() {
        Query q = getSession().createQuery("select i from IndexJob i order by i.id");
        return q.getResultList();
    }
    
	/* (non-Javadoc)
	 * @see server.dao.GenericDAO#delete(java.lang.Long)
	 */
	@Override
	public void delete(Long id) {
		IndexJob job = get(id);
		makeTransient(job);
	}
    
    @Override
    public void delete(IndexJob job){
        makeTransient(job);
    }
	
}
