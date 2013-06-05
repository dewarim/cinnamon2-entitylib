package server.dao;

import server.index.IndexJob;

import java.util.List;

public interface IndexJobDAO extends GenericDAO<IndexJob, Long> {
	
	List<IndexJob> list(Integer max);
    
    void delete(IndexJob job);
    
}