// cinnamon - the Open Enterprise CMS project
// Copyright (C) 2007-2012 Horner GmbH
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

package server.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.MetasetType;

import javax.persistence.Query;
import java.util.List;

public class MetasetTypeDAOHibernate extends
		GenericHibernateDAO<MetasetType, Long> implements MetasetTypeDAO {
	
	private transient Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void delete(Long id) {
		MetasetType moribundus = get(id);
		makeTransient(moribundus);
		flush();
	}

	@Override
	public MetasetType get(Long id) {
		return getSession().find(MetasetType.class, id);
	}


	@Override
	public MetasetType findByName(String name) {
		log.debug("MetasetType.findByName => "+name);
		Query q = getSession().createQuery("select m from MetasetType m where m.name=:name");
		q.setParameter("name", name);
		if (q.getResultList().size() < 1){
			return null;
        }
		return (MetasetType) q.getSingleResult();
	}

	/* (non-Javadoc)
	 * @see server.dao.GenericDAO#list()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MetasetType> list() {
		Query q = getSession().createQuery("select m from MetasetType m");
		return q.getResultList();
	}
	
}
