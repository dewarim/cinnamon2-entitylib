# Changelog of Cinnamon-Entitylib

##2.4.0

+ Move to git repository.
+ Add Link class which enables references to Folder and OSD objects.
    You can find required the SQL migration code in Server/doc/migration/migration-2.3.1.sql

##2.3.0

+ Fixed Folder & OSD getMetadata
+ Fixed bug in MetasetService (would create another MetasetService when calling a certain method) 

## 2.2.2

+ Added LifecycleStateAuditTrigger to track changes to lifecycles.
+ Fix broken query which cause version number corruption.

## 2.2.1

Fixed: Creating a copy of an object (via translation API) could result in wrong latestHead / latestBranch information.
Fixed: A bug in Metaset handling could cause objects to have multiple metasets of the same type.

## 2.1.2

+ moved XhtmlToPdfTransformer to archive - this class should be placed into an optional plugin, as
  the XhtmlRenderer's dependency on itext-jar causes things to break in the server's build code
  (due to signed itext jar). At the moment, no one is using this anyway (except for the Foo2Pdf-Test).

## 2.1.1

+ createZippedFolder by default now uses Cp437 as encoding for filenames in zip archive.

## 2.1.0

+ UiLanguage.findByIsoCode now return null instead of throwing a NoResultException if no result was found.
  (this makes handling such cases easier, especially in Groovy with the ?.-Operator)
+ Added new constructor for ObjectSystemData(name, user, parentFolder) which creates a simple OSD with all the
  defaults set so it may be persisted without further changes.
+ New method to create filenames from osd.name: osd.createFilenameFromName(path)
+ createNewVersionLabel is now public.
+ Moved createZippedFolder-functionality from CmdInterpreter to Folder.
+ SessionDAO.findByTicket now returns null if session is not found (instead of throwing NoResultException)
+ Fixed bug in CinnamonIndexInitializer. Index items for folder.owner and folder.type would be created with
  string_indexer instead of integer_indexer. If you used the CII to create your initial index items, please update
  the two items by setting their indexer class to xpath.integer_indexer.
+ PermissionDAOHibernate.findByName(name) returns null instead of throwing an NRE if no Permission is found.

## 2.0.5

+ Removed groupDAO.findAllByID as there is no case where this would return more than one element.
+ Removed groupDAO.findID as there already is a method for this: GroupDAO.get().
+ Added groupDao.findByParent(group)
+ Added groupDao.findAllUsers(group)
+ group.toXmlElement(root) now lists the sub groups and users of the group.
+ Added osd.copyRelations(target)
+ Added boolean fields relationType.cloneOnLeftCopy and relationType.cloneOnRightCopy which determine if
  a relation should be instantiated for the copy of an object which has a given relation.

## 2.0.4

+ UiLanguage was missing equals() and caused User.equals() to fail.
+ Added Group.toString().
+ Validator now checks once for superuser status when verifying browse-permissions.

## 2.0.3

+ Fixed bug in findFolderByPath with autocreate, which would create the folders one level too high.
+ findFolderByPath(path, autocreate, validator) now takes a Validator to check if a user may actually
  automatically create a folder.

## 2.0.2

+ Serialized User objects now include sudoable / sudoer status.

## Older changes:
    
    1.0.14
    
    + Fixed: while versioning objects, the lifecycle status was not set to the correct state on the copy
     - it always had state null.
    + Added findAllByPath(path, createMissingFolders)
    + Added customized toString() to Acl, FolderType and User.
    
    1.0.13
    
    + Added fieldDisplayName to XML-serialization of IndexItems, which contains the localized fieldname.
    + Fixed: delete language now works.
    + Fixed: prevent user from deleting lifeCycleStates that are still used by lifeCycleStateForCopy
      (normally, this would be done by a FK-relationship, but in case this is missing, we should check.
      And this way we can create a valid error message)
    + Fixed bug in acl-entry management (ConcurrentModificationException on delete)
    + Enhancement: fixed ugly error message when trying to create a duplicate message.id
    + Added pagination of message-id to list view in MessageController (with remote-pagination plugin)
    
    1.0.12
    
    + Added Language.MAX_ISO_CODE_LENGTH.
    + Fixed: possible endless recursion in LuceneBridge.deleteDocument(...)
    * LuceneBridge is now current with Lucene 3.1 API
    
    1.0.11
    
    + Unlock now no longer checks the object's ACL (not really needed there, and could lead to users being unable to
      cancel a checkout after changing the object's ACL)
    + Fixed bug in LifeCycleState which could lead to infinite recursion in equals().
    + Include lifecycle-id in lifecycle-state serialization.
    + Include lifecycle-state-id in OSD serialization instead of whole LCS-xml.
    + ChangeTrigger now have configuration parameters, just like lifecycles, to make ITrigger-classes more versatile.
    + Added LifeCycleDAO.findByName()
    + Added sysName field to serializations of LifeCycle and LifeCycleState (name contains the localized version for the
      current user)
    + Added LifeCycleStateDAO.findByName(name,lc).
    + Added LifeCycleState NopState, which does nothing (and can be used as default state for declarative LifeCycleStates).
    + Set ChangeTrigger.config to default "<config/>" if no value is given.
    
    1.0.10
    
    + Changed internal LifeCycleState-API (interface IState) to submit parameters of the LifeCycleState class to the state's
     implementation class. Previously, configuration parameters were not picked up by those classes.
    + Added ChangeAclState - LifeCycleStates with this class can now be configured to change an object's state upon entering it.
    + Extended TransformerDAO API
    
    1.0.9
    
    1.0.8
    
    + added ConfigEntry
    
    1.0.7
    
    (see Server/changelog for changes)
    
    1.0.6_RC1
    
    + OSD.latestbranches was renamed to OSD.latestbranch (both the field and the db-column)
    + Fixed bug: copyContent did not set the content format on the target object.
    + Added obj_version column (Long, NOT NULL, Default=0) to lifecycles and lifecycle_states. Please update your db-schema.
    + Added equals(o) and hashCode() to domain classes.
    + Added findAllByNameList(list of names) to ObjectTypeDAO which returns a set of object types.
    Changed serialization to XML in some classes:
    + Language objects, when serialized to XML, have a field name (=localized name) and sysName (isoCode field).
    + Added a new field sysName (which is the name field without translation) to the serialized form of several classes:
        Acl, FolderType, Format, Group, RelationType
    + Permission: changed the serialized element <rawName> to <sysName> for more consistency
    
    
    1.0.5
    
    + Serialized folder now contains element <hasChildren> with value true or false, depending on whether the folder has
    sub-folders.
    
    1.0.4
    
    see Server/changelog for changes.
    
    1.0.3
    
    + add total-results attribute to search result documents
    + interface XmlConvertable now extends Comparable (to enable sorted paged search results).
    
    1.0.2
    
    + Extended User class: update your database tables!
        Timestamp token_age (default: 1970-01-01T00:00:00)
        Integer tokens_today (default: 0)
        varchar token (length: 128) (nvarchar on MSSQL)
        varchar email (length: 255) (nvarchar on MSSQL)
    + Added Language.getDefaultLanguage() which returns the "und"-Language.
    
    1.0.1
    
    + Added Permission.CREATE_INSTANCE(name: "_create_instance") (for createWorkflow)
    + Added method findAllByNameAndType to ObjectSystemDataDAO
    + Removed unused method findByName from ObjectSystemDataDAO
    + Added getSearchResultsAsOSDs to ResultCollector
    + Added DateTimeIndexer
    
    1.0.0
    
    + Updated Lucene to version 3.0.1
    + Added IndexType.DataType.TEXT
    + Added field sysName to ObjectType serialization (because OT.name is localized and the client may need the "real"
    name of an ObjectType)
    + Removed RepositoryHelper
     
    0.7.1
    
    -
    
    0.7.0
    
    + Added several new Indexer-classes which should properly indexes child nodes:
        DescendingStringIndexer (from DefaultIndexer)
        DescendingCompleteStringIndexer
        DescendingReverseStringIndexer
        DescendingReverseCompleteStringIndexer
    + Fixed javaDoc.
    + Added IndexItem.search_condition. This is an XPath expression which has to evaluate to true on one of (sysmeta, content, metadata) before
    an item can be indexed. Please add a text column "search_condition" with the same size and parameters as index_items.search_string and (if possible) 
    default value "true()".
    
    0.6.9
    
    Further changes to class layout.
    Added Xhtml2PdfRenderer (Test class) and improved ITransformer interface.
    
    0.6.8
    
    Added get(String id) to FormatDAO.
    Moved some more classes to remove cyclic dependency.
    Added Transformer entity.
    
    Please update your persistence.xml by adding:
    <class>server.transformation.Transformer</class>
    in the appropriate places.
    
    Please update your database:
    
    CREATE TABLE transformers
    (
      id bigint NOT NULL,
      description character varying(255) NOT NULL,
      "name" character varying(128) NOT NULL,
      obj_version bigint,
      transformer_class character varying(128) NOT NULL,
      source_format_id bigint NOT NULL,
      target_format_id bigint NOT NULL,
      CONSTRAINT transformers_pkey PRIMARY KEY (id),
      CONSTRAINT fk9158567a37b70657 FOREIGN KEY (target_format_id)
          REFERENCES formats (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT fk9158567a5c95d861 FOREIGN KEY (source_format_id)
          REFERENCES formats (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT transformers_name_key UNIQUE (name)
    )
    WITH (
      OIDS=FALSE
    );
    ALTER TABLE transformers OWNER TO cinnamon;
    
    
    
    0.6.7
    
    Some classes from Server moved to Utils. There is now a cyclic dependency between EntityLib and Utils,
    so for the first build you may have to create a entitylib-source-jar and add it temporarily to Utils.
    
    0.6.6
    
    Deprecated RelationType.retainoncheckin (to be removed in the future).
    Added dependency to lib/cinnamon_interfaces.jar
    Added ChangeTriggers
    For Postgres:
    
    CREATE TABLE change_trigger_types
    (
      id bigint NOT NULL,
      description character varying(255) NOT NULL,
      "name" character varying(128) NOT NULL,
      obj_version bigint,
      trigger_class character varying(128) NOT NULL,
      CONSTRAINT change_trigger_types_pkey PRIMARY KEY (id),
      CONSTRAINT change_trigger_types_name_key UNIQUE (name)
    )
    WITH (
      OIDS=FALSE
    );
    ALTER TABLE change_trigger_types OWNER TO cinnamon;
    
    CREATE TABLE change_triggers
    (
      id bigint NOT NULL,
      active boolean NOT NULL,
      command character varying(255) NOT NULL,
      obj_version bigint,
      post_trigger boolean NOT NULL,
      pre_trigger boolean NOT NULL,
      ranking integer NOT NULL,
      trigger_type_id bigint NOT NULL,
      CONSTRAINT change_triggers_pkey PRIMARY KEY (id),
      CONSTRAINT fkd7e8a56aa16bc23a FOREIGN KEY (trigger_type_id)
          REFERENCES change_trigger_types (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE NO ACTION
    )
    WITH (
      OIDS=FALSE
    );
    ALTER TABLE change_triggers OWNER TO cinnamon;
    
    
    0.6.5
    
    + Suppress warnings for necessary object casts (due to JPA limitations in getResultList).
    + OSD-DAO: findAllVersions
    + OSD-DAO: findByRootAndVersion
    + OSD-DAO: findAllVersionsOrderLastToFirst
    
    + fixed namedQuery for LanguageDAO.findByIsoCode
    + fixed bug in IndexServer which by default set the sleep time between runs to 0 (instead of 5 seconds).
     This would cause the IndexServer-Thread to query the database as fast as possible and would
     grow the log files at an extreme rate, depending on how logging was configured. #1494
    
    0.6.4
    
    Added toXmlElement(Element e) to Language and Message.
    I18N: Names and descriptions are now returned localized by the server, if a translation exists.
    I18N: Added LocalMessage.loc(string) which translates Strings depending on the user session's language. 
    Added table messages:
        primary key: long id
        string message
        string translation
        foreign key language_id
        constraint: unique(language_id, message)
    Added foreign key language_id to session (default:NULL)
    
    Moved server.Language to server.i18n.Language. Update your persistence.xml!
    
    Moved the 3 Boolean-Columns for_content, for_metadata, for_sysmeta from table index_types to index_items.
    You should update your database layout accordingly.
    By limiting an IndexItem to a specific type of data (for example, only metadata), you may experience
    some speedup during indexing.
    
    Added Column Boolean objects.index_ok Default NULL.
    Added Column Boolean folders.index_ok Default NULL. 
    Behaviour of IndexServer can now be configured via lucene.properties:
    - itemsPerRun (new)
    - sleepBetweenRuns (new) - milliseconds
    - indexDir
    IndexServer is now more robust and should survive broken input data.
    
    API-Changes
    removed asXML with XStream from several classs to improve consistency.
    
    0.6.3
    
    Added RegexQueryBuilder to Lucene-XML-Query-Parser.
    Fixed ParentFolderPathIndexer
    Fixed bug in FolderDAOHibernate.getParentFolders().
    Fixed problematic behaviour in LuceneBridge (UTF-8-problem on Windows) 
    
    0.6.2
    
    Updated to Lucene 3.0.0
    Added WildcardQueryBuilder
    Added ReverseStringIndexer and ReverseCompleteStringIndexer
    Added ParentFolderPathIndexer
    
    0.6.1
    
    -
    
    0.6.0
    
    Changed API / Default settings:
    - OSD.setContentPath_ is gone, use setContentPath.
    - OSD.setContentPath(String name,String format_name) is now setContentPathAndFormat to
        signal its side effect of also setting the format.
    - OSD.contentPath and .contentSize default to null.
    Removed FormatDAO.findAllByName, as name is unique.
    FormatDAO.findByName will return null and not a NoResultException, if no Format was found.
    Added FolderDAO.getFolderContent, which will (optionally recursively) return
     all OSDs inside a Folder.
    
    
    0.5.9
    
    Changed a lot of columns to NOT NULL (where it makes sense).
    Added obj_version to tables (where it was missing).
    Fixed bug were Lucene prevented createFolder().
    
    0.5.8
    
    "_" is now considered a letter for the Lucene analyzer.
    Folder & OSD now implement interface XmlConvertable.  
    Added class IndexGroup + required DAO-classes.
    Added class FolderType + required DAO-classes.
    Implementing IndexServer, upgrading Lucene to 2.9.0
    Added some constraints on name-fields for certain object classes (Permission, IndexItem, IndexType).
    Added SQL-script examples for setting up the index_items and index_types tables.
    Added several Indexer-classes to index boolean, date, integer, time and decimal fields.
    moved DefaultIndexer to server.index.indexer.DefaultIndexer
    SearchResults are now filtered according to ACL-Permissions (BROWSE_OBJECT, BROWSE_FOLDER)
    Fixed bug in Folder.setMetadata, which could lead to unparsable metadata content.
    Added debug messages to Indexer classes.
    Added LuceneBridge.updateObjectInIndex()
     
    IndexItem.xpath is now IndexItem.searchString.
    Please change the column name index_items.xpath in the db to "index_items.search_string". 
    
    0.5.7
    
    Added Folder.generateQueryFolderResultDocument for CmdInterpreter.queryFolders.
    Folder.owner is no longer nullable, and it's parameterized constructor now requires
    a user object as parameter. The HashMap-constructor requires "ownerid" as parameter,
    otherwise it throws a CinnamonException.
    
    see Server/changelog for necessary database changes.
    fixed updateFolder to properly update owner.
    getSubfolders(rootFolder) now should no longer include the rootFolder in its results.
    
    0.5.6
    
    FolderDAO.getFolderByPath() now returns root folder. 
    added Folder.getDescendants()
    added Group.removeUserFromGroup()
    User.asElement now adds isSuperuser-element to XML document.
    The default content of OSD.meta and Folder.meta is now "<meta/>".
    OSD and Folder implement the new interface "Ownable". 
    
    0.5.5
    
    OSD now inherits parentFolder's ACL unless told otherwise.
    removed deprecated method Folder.findRootFolder and rewrote it for FolderDAO.
    improved NamedQueries in server.Acl
    Folder.toXmlElement(...) added.
    User.findAllGroups() added.
    Permission.toXmlElement(...) added.
    Groups.toXmlElement(...) now uses elements instead of attributes.
    added Folder.asXml(), which is not yet fully optimized. But it is currently the only way to extract
    a folder's owner.
    Relation.toXmlElement() now uses elements instead of attributes.
    
    Added static asElement-Methods to Format and ObjectType, which serialize the objects to DOM.
    User.asElement now serializes the complete user (without pwd) as DOM elements.
    OSD.toXML returns the same output (for individual objects) as 
    generateQueryObjectResultDocument.
    
    0.5.4
    
    All String fields now should have a length setting to help Hibernate to determine the correct column type.
    
    Added Language => please create a table
    languages (int id primary key auto_increment not null, varchar(32) iso_code unique)
    You need to insert 3 Language objects into the new table with the isocodes
    "mul" [for multiple languages], 
    "und" [for undetermined] and 
    "zxx" [for no language]
    
    update table objects by adding a column language with a ForeignKey to languages.id.
    The default should be "und". If objects.metadata already contains language information,
    you should run an update by script or client. 
    
    Add an integer column lang_id to table objects, which has a FK-restraint to languages.id	
    -----------------------------------------------------------
    
    0.5.3
    
    folders-table needs column owner_id (ForeignKey => users)
    
    0.5.2:
     
     fixed Session <=> User mapping (Hibernate)
     
    Addendum to 0.5.0: users table now needs a column "activated" with default=true
    
    0.5.1:
    
    OSD.toXML now returns <object>...</object> instead of <objects><ObjectSystemData>...
    because this is easier to handle when returning multiple objects.
    
    -----------------------------------------------------------
    0.5.0:
    AclDAO: removed findByID(id) - use get(id) instead.
            removed findAllByID(id) - use get(id) and list.add(obj) instead.
    AclEntryDAO: added List<AclEntry> list()
    CustomTableDAO: renamed findAll() to list()
                    removed findByID(id) - use get(id) instead
    
    FormatDAO: removed findByID(id) - use get(id) instead
                added update(id, fields)
    FolderDAOHibernate: update now updates Folder.metadata
    ObjectSystemDataDAO: removed findByID(id) - use get(id) instead
    ObjectTypeDAO: removed findByID(id) - use get(id) instead
                added list()
    PermissionDAO: added list()
    RelationDAO: added get(id)
                added list()
    RelationTypeDAO: added get(id)
                added list()
    SessionDAOHibernate: added get(id)
                added list()
    UserDAO: added list()
            removed findByID(id) - use get(id) instead
            
    Users are no longer deleted but set to activated==false because normally a user
    owns some objects in the database which would be orphaned if he were simply
    deleted.
    Added delete(id) to all *DAOHibernate-Classes. Note that currently, those do check whether
    it is okay to delete an item or not, they just implement the convenient feature of
    removing an object by id.
     
    -----------------------------------------------------------
    0.4.5:
    new: Permission.fetch(String name)
    -----------------------------------------------------------
    0.4.4
    
    Permissions added.
    Please create
    - a table "permissions"
        id bigint AUTO_INCREMENT Primary Key
        name varchar(255)
        description varchar(255)	
        
    - a table "aclentry_permissions"
        id bigint AUTO_INCREMENT Primary Key
        bigint aclentry_id FK acls
        bigint permission_id FK permissions
        unique index (aclentry_id, permission_id)
        
    drop column permitlevel from aclentries
    -----------------------------------------------------------
    
    0.4.3
    
    added listAclMembers(aclId)
    
    -----------------------------------------------------------
    Start of changelog with version 0.4.2 (corresponding to Server v0.4.2).
    -----------------------------------------------------------
    From Server/changelog:
    0.4.0:
    
    Database: customtables needs a varchar(255) column "jdbcdriver". 
    Database,API: CustomTable now uses ACLs instead of permissionId. This means that a new FK-constraint is 
    needed between CustomTable.acl_id and server.Acl.
