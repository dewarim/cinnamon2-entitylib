package server.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Metaset;
import server.MetasetType;
import server.exceptions.CinnamonException;
import utils.ParamParser;

import javax.persistence.Query;
import java.util.List;
import java.util.Map;

public class MetasetDAOHibernate extends GenericHibernateDAO<Metaset, Long>
		implements MetasetDAO {

	private transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void delete(Long id) {
		Metaset moribundus = get(id);
		makeTransient(moribundus);
	}

	/* (non-Javadoc)
	 * @see server.dao.GenericDAO#get(java.lang.Long)
	 */
	@Override
	public Metaset get(Long id) {
		return getSession().find(Metaset.class, id);
	}

	/* (non-Javadoc)
	 * @see server.dao.GenericDAO#list()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Metaset> list() {
		Query q = getSession().createNamedQuery("select m from Metaset m");
		return q.getResultList();
	}

	public void update(Long id, Map<String, String> cmd) {
		Metaset metaset = get(id);
		if(metaset == null){
			throw new CinnamonException("error.metaset.not_found", String.valueOf(id));
		}	

		String content = cmd.get("content");
		if(content != null){
			metaset.setContent(content);
		}

        String typeName = cmd.get("type_name");
        if(typeName != null){
            Query q = getSession().createQuery("select m from MetasetType m where m.name=:name");
            q.setParameter("name", typeName);
            List<MetasetType> mtList = q.getResultList();
            if(mtList.size() != 1){
                throw new CinnamonException("error.metasettype.not_found");
            }
            MetasetType mt = mtList.get(0);
        }

	}

	@Override
	public Metaset get(String id) {
		Long metasetId = ParamParser.parseLong(id, "error.param.id");
		return get(metasetId);
	}
	
}
