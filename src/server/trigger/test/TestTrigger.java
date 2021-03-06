package server.trigger.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.User;
import server.dao.DAOFactory;
import server.dao.HibernateDAOFactory;
import server.dao.ObjectSystemDataDAO;
import server.data.ObjectSystemData;
import server.helpers.PoBox;
import server.interfaces.Repository;
import server.interfaces.Response;
import server.response.XmlResponse;
import server.trigger.ChangeTrigger;
import server.trigger.ITrigger;
import server.trigger.TriggerResult;
import utils.HibernateSession;

import java.util.Map;

/**
 * Test class for testing the ChangeTrigger implementation of CinnamonServer.
 * It will change a given object with name "ChangeTriggerTestObject" 
 * by adding a "<testTrigger/>"-node to the metadata, if applicable.
 *
 */
public class TestTrigger implements ITrigger {
	
	private final transient Logger log = LoggerFactory.getLogger(this.getClass());
	static DAOFactory daoFactory = DAOFactory.instance(HibernateDAOFactory.class);
	
	public TestTrigger(){}

	@Override
	public PoBox executePostCommand(PoBox poBox, String config) {
	
		ObjectSystemDataDAO osdDao = daoFactory.getObjectSystemDataDAO(
				HibernateSession.getLocalEntityManager());
		if(! poBox.params.containsKey("id")){
			log.debug("no id found");
			return poBox;
		}
			
		/*
		 *  Note: in a production system, id could map to a folder-id or something else,
		 *  which only accidentally also maps to an object with the same id.
		 *  You should check if params contains an appropriate command (like setContent).  
		 */
		ObjectSystemData osd = osdDao.get((String) poBox.params.get("id"));
		if(osd != null){
			log.debug("found OSD for postCommandTrigger");
			if(osd.getName().equals("ChangeTriggerTestObject")){
				log.debug("changing metadata");
				String meta = osd.getMetadata();
				log.debug("meta before: '"+meta+"'");
				meta = meta.replace("<meta/>", "<meta><testTrigger/><name>SearchTestTriggerName</name></meta>");
				log.debug("meta after: '"+meta+"'");
				osd.setMetadata(meta);
				log.debug("updating changed object in index.");
			    poBox.repository.getLuceneBridge().updateObjectInIndex(osd);
                if(poBox.command.equals("setmeta")){
                    ((XmlResponse) poBox.response).addWarning("Last warning for test trigger!", "Run!");
                }
            }
        }
        return poBox;
	}

	@Override
	public PoBox executePreCommand(PoBox poBox, String config) {
		log.debug("nothing to do in TestTrigger.executePreCommand.");
        return poBox;
	}



}
