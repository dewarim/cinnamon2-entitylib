package server.interfaces;

import server.dao.CustomTableDAO;
import server.data.SqlCustomConn;
import server.index.LuceneBridge;
import server.interfaces.CommandRegistry;
import utils.HibernateSession;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Interface for Cinnamon repositories, which are a combination of
 * tables in the database (managed by Hibernate) and a file system data store.
 * Repositories may have custom SQL connections to other databases and
 * tables, as well as their own specific API which can be called
 * via the CommandRegistry.
 *
 */
public interface Repository {
	
	String getName();
	void setName(String name);

    /**
     * Return the HibernateSession that was created during the instantiation of this
     * repository object. You should not re-use this in another thread.
     * @return the repository's HibernateSession object
     */
	HibernateSession getHibernateSession();
	void setHibernateSession(HibernateSession session);

    HibernateSession getCustomHibernateSession();
    void setCustomHibernateSession(HibernateSession hs);

	/**
	 * Note: This is a shortcut to this Repository's HibernateSession.getEntityManager.
	 * It returns a new EntityManager, which is probably not what you need, unless you
	 * want to initialize the HibernateSessions' localEntityManager.
	 * @return a new EntityManager.
	 */
	EntityManager getEntityManager();

	Map<String, SqlCustomConn> getSqlCustomConns();
	void setSqlCustomConns(Map<String, SqlCustomConn> connections);
	
	CommandRegistry getCommandRegistry();
	void setCommandRegistry(CommandRegistry cmdReg);
	
	void reloadSqlCustomConnections(CustomTableDAO ctDao);
	void loadApiClasses(List<String> apiClasses);
	
	LuceneBridge getLuceneBridge();
	
	void startIndexServer();
	void startWorkflowServer();

    /**
     * Create a new HibernateSession configured for this repository.
     * @return a new HibernateSession
     */
    HibernateSession createHibernateSession();

    /**
     * @return SQL connection to the audit log database.
     */
    Connection getAuditConnection();
}
