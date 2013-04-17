package server.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Folder;
import server.dao.*;
import server.dao.DAOFactory;
import server.dao.FolderDAO;
import server.dao.MessageDAO;
import server.dao.ObjectSystemDataDAO;
import server.dao.UiLanguageDAO;
import server.data.ObjectSystemData;
import server.global.ConfThreadLocal;
import server.i18n.LocalMessage;
import server.i18n.UiLanguage;
import server.interfaces.Repository;
import utils.HibernateSession;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * The IndexServer is intended to run as a thread which will wake up every couple of seconds to
 * re-index all objects whose indexed-time is set to 0. It will update both folders and OSDs.
 * One IndexServer per Repository is required.
 */
@SuppressWarnings({"InfiniteLoopStatement", "InfiniteLoopStatement"})
public class IndexServer implements Runnable {

    static Properties luceneProperties = ConfThreadLocal.getConf().findProperties("lucene.properties");
    private Logger log = LoggerFactory.getLogger(this.getClass());

    EntityManager em;
    HibernateSession hibernateSession;
    static DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
    final LuceneBridge lucene;
    Long sleep = 5000L;
    Long runCounter = 0L;
    Integer itemsPerRun = 50;
    Integer logModulus = 1;
    Repository repository;

    public IndexServer(LuceneBridge lucene, Repository repository) {
        /*
           * Default values if luceneProperties are missing or incomplete:
           */
        if (!luceneProperties.containsKey("sleepBetweenRuns")) {
            log.info("No sleepBetweenRuns-Property found. Using default of 5000 milliseconds.");
            luceneProperties.setProperty("sleepBetweenRuns", "5000");
        } else {
            this.sleep = Long.parseLong((String) luceneProperties.get("sleepBetweenRuns"));
        }

        if (!luceneProperties.containsKey("itemsPerRun")) {
            log.info("No itemsPerRun-Property found. Using default of 50 items and folders.");
            luceneProperties.setProperty("itemsPerRun", "50");
        } else {
            this.itemsPerRun = Integer.parseInt((String) luceneProperties.get("itemsPerRun"));
        }

        if (!luceneProperties.containsKey("logModulus")) {
            log.info("No logModulus-Property found. Using default of 1.");
            luceneProperties.setProperty("logModulus", "1");
        } else {
            this.logModulus = Integer.parseInt((String) luceneProperties.get("logModulus"));
        }

        this.lucene = lucene;
        this.repository = repository;
    }

    public IndexServer(LuceneBridge lucene, Long sleepMilliseconds, Integer itemsPerRun, Repository repository) {
        this.lucene = lucene;
        this.sleep = sleepMilliseconds;
        this.itemsPerRun = itemsPerRun;
        this.repository = repository;
    }

    @Override
    /**
     * Run every $sleep seconds and at most index $itemsPerRun folders
     * and objects. (If itemsPerRun is set to 10, it will index 10 folders
     * and 10 objects whose index has been invalidated).
     */
    public void run() {
        // initialize LocalMessage:
        hibernateSession = repository.createHibernateSession();
        em = hibernateSession.getEntityManager();
        try {
            log.debug("Initialize LocalMessage");
            MessageDAO messageDao = daoFactory.getMessageDAO(em);
            UiLanguageDAO languageDao = daoFactory.getUiLanguageDAO(em);
            UiLanguage language = languageDao.findByIsoCode("und");
            if (language == null) {
                log.error("UiLanguage for 'und' (undetermined language) was not found. Please fix your system configuration.");
            }
            LocalMessage.initializeLocalMessage(messageDao, language);
            log.debug("LocalMessage initialized.");
        } catch (Exception e) {
            log.warn("", e);
        }

        while (true) {
            runCounter++;
            if (HibernateSession.getLocalEntityManager() == null) {
                HibernateSession.setLocalEntityManager(em);
            }

            // necessary in case an exception corrupts or closes the EntityManager.
            if (em == null || !em.isOpen()) {
                // create a new EM with this repository's connection details:
                em = hibernateSession.getEntityManager();
                HibernateSession.setLocalEntityManager(em);
            }

            try {
                localDebug("sleep for " + sleep + " milliseconds");
                Thread.sleep(sleep);
            } catch (Exception e) {
                log.debug("Sleep of IndexServer was interrupted.");
            }
            localDebug(String.format("re-index run starting for %s with %d x2 items.",
                    lucene.getRepository(), itemsPerRun));
            EntityTransaction et = null;
            try {
                et = em.getTransaction();
                et.begin();
                indexOSDs(itemsPerRun);
                indexFolders(itemsPerRun);
                et.commit();
            } catch (Throwable e) {
                log.debug("Exception during indexing: ", e);
                try {
                    /*
                     *  try to rollback any changes to items,
                     *  for example invalid indexed-time column.
                     */
                    if (et != null && et.isActive()) {
                        et.rollback();
                    }
                } catch (Exception re) {
                    log.error("Failed to rollback; " + re.getMessage());
                }
            }
            finally {
                em.clear();
            }
            localDebug("re-index run for " + lucene.getRepository() + " finished.");
        }
    }

    void indexOSDs(Integer items) {
        ObjectSystemDataDAO oDao = daoFactory.getObjectSystemDataDAO(em);
        IndexJobDAO jobDao = daoFactory.getIndexJobDAO(em);
        List<IndexJob> jobs = oDao.findIndexTargets(items);
        localDebug("# of osds to reindex: " + jobs.size());
        Set<Long> seen = new HashSet<>(jobs.size());        
        for (IndexJob job : jobs) {
            Long id = job.indexableId;
            if(seen.contains(id)){
                jobDao.delete(job);
                continue;
            }
            ObjectSystemData osd = oDao.get(id);
            if(osd == null){
                localDebug("did not find osd #"+id);
                jobDao.delete(job);
                seen.add(id);
                continue;
            }
            try {
                localDebug("indexer working on OSD: " + osd.getId());
                lucene.updateObjectInIndex(osd);
                jobDao.delete(job);
            } catch (Exception e) {
                log.debug("indexing of object " + osd.getId() + "failed with:", e);
                job.failed = true;
            }
            seen.add(id);
        }
        /* This is the most expensive way to do it.
           * On the other hand, it can handle broken objects and defective index_items
           * without preventing one bad apple from spoiling the entire index.
           */
    }
    
    void indexFolders(Integer items) {
        FolderDAO fDao = daoFactory.getFolderDAO(em);
        IndexJobDAO jDao = daoFactory.getIndexJobDAO(em);
        List<IndexJob> jobs = fDao.findIndexTargets(items);
        localDebug("# of folders to reindex: " + jobs.size());
        Set<Long> seen = new HashSet<>(jobs.size());
        for (IndexJob job : jobs) {
            Long id = job.indexableId;
            if(seen.contains(id)){
                jDao.delete(job);
                continue;
            }
            Folder folder = fDao.get(id);
            if(folder == null){
                localDebug("Did not find folder #"+job.getIndexableId());
                jDao.delete(job);
                seen.add(id);
                continue;
            }
            try {
                localDebug("indexer working on folder: " + folder.getId());
                lucene.updateObjectInIndex(folder);
                jDao.delete(job);
            } catch (Exception e) {
                log.debug("indexing of object " + folder.getId() + "failed with:", e);
                job.failed = true;
            }
            seen.add(id);
        }
    }

    /**
     * A wrapper for log.debug, which only prints a message when the loop counter of the
     * IndexServer modulo the logModulus property is 0.
     * This serves to reduce log spam when nothing important happens.
     *
     * @param message the log message.
     */
    void localDebug(String message) {
        if (runCounter % logModulus == 0) {
            log.debug(message);
        }
    }
}
