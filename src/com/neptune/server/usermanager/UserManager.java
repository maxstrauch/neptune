/**
 * (c) 2009 by Maximilian Strauch.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.neptune.server.usermanager;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.neptune.server.lib.NeptuneProtocol;

public class UserManager {

	private ArrayList<User> user;
	
	public UserManager() {
		this.user = new ArrayList<User>();
	}
	
	/**
	 * Adds a user
	 * @param usr the user-object
	 * @return -1: user name is not valid, 0: user cannot added to list, 1: all right
	 */
	public int addUser(User usr) {
		
		if (!this.validUser(usr.getName())) {
			return -1;
		}
		
		if (!this.user.add(usr)) {
			return 0;
		}
		
		return 1;
	}
	
	private boolean validUser(String username) {
		// check the appearance of the user name
		if (!Pattern.matches(NeptuneProtocol.REGEX_VALID_USERNAME, username)) {
			return false;
		}
		
		// check if the user name already exists in the list
		for (int i = 0; i < this.user.size(); i++) {
			if (this.user.get(i).getName().equals(username)) {
				return false;
			}
		}
		return true;
	}
	
	public void removeUser(User usr) {
		if (usr == null) {
			return;
		}
		this.user.remove(usr);
	}
	
	
	public User getUser(String name) {
		for (int i = 0; i < this.user.size(); i++) {
			if (this.user.get(i).getName().equals(name)) {
				return this.user.get(i);
			}
		}
		return null;
	}
	
	public User getUser(String ip, int port) {
		for (int i = 0; i < this.user.size(); i++) {
			if (this.user.get(i).getIp().equals(ip) &&
					this.user.get(i).getPort() == port) {
				return this.user.get(i);
			}
		}
		return null;
	}
	
	public User[] getAllUsers() {
		User[] users = new User[this.user.size()];
		this.user.toArray(users);
		return users;
	}
	
	public int getNumerOfUsers() {
		return this.user.size();
	}
	
}
