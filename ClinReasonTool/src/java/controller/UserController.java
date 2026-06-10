package controller;

import beans.user.User;
import database.DBUser;

public class UserController {

	
	private User createAndSaveUser(/*int systemId,*/ String extUserId /* String groupId*/){
		User u = new User(/*systemId,*/ extUserId/*, groupId*/);
		u.getUserSetting().initNewUser();
		new DBUser().saveAndCommit(u);
		
		return u;
	}
	
	public User createAndSaveExpertUser(/*int systemId,*/ String extUserId/*, String groupId*/){
		User u = new User(/*systemId,*/ extUserId/*, groupId*/);
		u.setEditor(true);
		u.getUserSetting().initNewUser();
		new DBUser().saveAndCommit(u);
		
		return u;
	}
	
	public User getUser(/*int systemId, */String extUserId /*, String groupId*/){
		DBUser dbu = new DBUser();
		User u = dbu.selectUserByExternalId(extUserId/*, systemId*/);
		if(u!=null) return u;
		return createAndSaveUser(/*systemId,*/ extUserId/*, groupId*/);
 	}
	
}
