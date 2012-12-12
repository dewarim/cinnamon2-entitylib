package server;

import server.User;

/**
 * ObjectSystemData and Folder may have an owner. The interface ownable enables you to write methods which
 * check both types of objects. 
 *
 */
public interface Ownable {

	/**
	 * 
	 * @return the owner of this object. 
	 */
	User getOwner();
	
}
