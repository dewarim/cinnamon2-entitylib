package server.index;

public interface Indexable {

	/**
	 * An Indexable object should have a field with XML metadata, which is represented as a String.
	 * For example, an image object may have it's width and height recorded as metadata.<br>
	 * A PDF manual may have the type of engines it is applicable for stored in the metadata field.   
	 * @return XML metadata or an empty meta element: {@code <meta/>} 
	 */
	String getMetadata();
	
	/**
	 * <p>If an object contains indexable strings, it should return them according to a fixed XML
	 * Schema. For example, an XML Document should be returned verbatim. A folder may represent its
	 * content as an XML document. An image document may either return an empty XML document or a automatically
	 * generated OCR representation.
	 * </p>
	 * <p>Note: you should not return the same data as Metadata and as content unless it is really 
	 * necessary.</p> 
	 * @return an XML representation of the object's content, as appropriate, or an empty content
	 * element: {@code <content/>};
     * @param repository The repository where the indexable object is located.
	 */
	String getContent(String repository);

    byte[] getContentAsBytes(String repository);

	/**
	 * <p>Return an XML representation of the object's system metadata. This is whatever the system itself
	 * knows about the object. For example, who created the object, and when.</p>
	 * <p>The root element of the XML is "sysMeta" and it must have the following two attributes set:</p>
	 * <ol>
	 * <li>javaClass</li>
	 * <li>hibernateId</li>
	 * </li>
	 * <p>The LuceneBridge will generate a unique Id from this ($className+$hibernateId).</p> 
	 * @return an XML representation of the object's system metadata - 
	 * or an empty {@code <sysMeta/>} element with attributes javaClass and hibernateId.
	 */
	String getSystemMetadata();
	
//	/**
//	 * <p>If indexing was successful, set indexOk to true, otherwise to false.</p>
//	 * <p>If this field is null, the item in question has not been indexed yet.<p>
//	 * @return true if indexing was successful, false if unsuccessful, null if the object needs to be indexed.
//	 */
//	Boolean getIndexOk();

//    /**
//     * Set the indexOk-flag.
//     * @param indexOk set to true if index operation was successful, false if unsuccessful, null if the IndexServer
//     * should index this item again. Note: the IndexServer has to be configured to run asynchronously for the null
//     * parameter to work.
//     */
//	void setIndexOk(Boolean indexOk);

//    /**
//     * Set the date on which the object was last indexed.
//     * @param date Java date object which defines moment of the last update event.
//     */
//	void setIndexed(Date date);

    Boolean hasXmlContent();

    /**
     * Schedule the object to be updated by the background index thread at the next opportunity.
     */
    void updateIndex();

    /**
     * @return The id of the indexed object. 
     */
    // Note: did not use getId as the id may be added dynamically via GORM (in Cinnamon 3) and
    // it may confuse the IDE to have a class which does not (at compile time) have an id field
    // but only a getter for it.
    Long myId();

    /**
     * Load this indexable again from the database.
     */
    Indexable reload();

    /**
     * @return a String composed of the Java class name and the Hibernate id, for example "server.Folder@123"
     */
    String uniqueId();
}
