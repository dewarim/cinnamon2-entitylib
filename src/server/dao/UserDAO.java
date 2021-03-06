package server.dao;

import server.User;
import server.exceptions.CinnamonException;

public interface UserDAO extends GenericDAO<User, Long> {
	User findByName(String name);
	User findByNameAndPassword(String name, String password);
	
	/**
	 * Delete a User and his personal Group.
	 * @param id
	 */
	void delete(Long id);
	
	/**
	 * Add this user to a group. <em>This creates a GroupUser object!</em>  
	 * @param groupID
	 * @param userID
	 * @return id of the GroupUser object
	 */
	Long addToGroup(Long userID, Long groupID);
	
	/**
	 * Convenience method: given a String which represents a user-Id, this
	 * method returns the User.
	 * @param id - String parsable with Long.parseLong
	 * @return User
	 * @throws CinnamonException if String is not a valid Long.
	 */
	User get(String id);
}
