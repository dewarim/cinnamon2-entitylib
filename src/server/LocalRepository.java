package server;

import server.index.IndexAction;
import server.index.Indexable;
import server.interfaces.Repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class LocalRepository {
    
    private static ThreadLocal<Repository> currentRepository = new ThreadLocal<Repository>();

    static public Repository getRepository(){
        return currentRepository.get();
    }
    
    static public void setRepository(Repository repository){
        currentRepository.set(repository);
    }
    
    private static ThreadLocal<HashMap<Indexable, IndexAction>> updatedObjects = new ThreadLocal<HashMap<Indexable, IndexAction>>(){
        @Override
        protected HashMap<Indexable,IndexAction> initialValue() {
            return new HashMap<>();
        }
    };
    
    public static void addIndexable(Indexable indexable, IndexAction action){
        updatedObjects.get().put(indexable, action);
    }
    
    public static Map<Indexable, IndexAction> getUpdatedObjects(){
        return updatedObjects.get();
    }
    
    public static void cleanUp(){
        getUpdatedObjects().clear();
    }

}
