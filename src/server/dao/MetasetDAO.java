package server.dao;

import server.Metaset;
import server.data.ObjectSystemData;

import java.util.Map;

public interface MetasetDAO extends GenericDAO<Metaset, Long> {

    void delete(Long id);
	
	void update(Long id, Map<String, String> cmd);
	
	Metaset get(String id);

}
