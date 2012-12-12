package server.interfaces;

import server.Acl;
import server.Metaset;
import server.MetasetType;
import server.index.Indexable;

import java.util.Set;

/**
 */
public interface IMetasetOwner extends Indexable{

    Set<Metaset> fetchMetasets();
    Metaset fetchMetaset(String name);
    void addMetaset(Metaset metaset);
    Acl getAcl();
    IMetasetJoin fetchMetasetJoin(MetasetType type);
    long getId();

}
