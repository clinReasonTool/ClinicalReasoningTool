package database;

import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.*;

import beans.user.SessionSetting;
import beans.user.User;


/**
 * All statements concerning a User object.
 * @author ingahege
 *
 */
public class DBUser extends DBClinReason{

    public User selectUserByExternalId(String extUserId/*, int systemId*/){
    	if(extUserId==null || extUserId.trim().equals("")) return null;
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(User.class,"User");
    	criteria.add(Restrictions.eq("extUserId", extUserId));
    	//criteria.add(Restrictions.eq("systemId", new Integer(systemId)));

    	return (User) criteria.uniqueResult();
    }
    
    public List<User> selectUsers(){
    	//if(extUserId==null || extUserId.trim().equals("")) return null;
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(User.class,"User");
    	criteria.add(Restrictions.ilike("extUserId2", "-1"));
    	//criteria.add(Restrictions.eq("systemId", new Integer(systemId)));

    	return criteria.list();
    }
    
    
    public User selectUserById(long userId){
    	if(userId<0) return null;
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(User.class,"User");
    	criteria.add(Restrictions.eq("userId", new Long(userId)));
    	return (User) criteria.uniqueResult();
    }
    
    public User selectUserByLogin(String login){
    	if(login==null || login.trim().equals("")) return null;
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(User.class,"User");
    	criteria.add(Restrictions.eq("userName", login));
    	return (User) criteria.uniqueResult();
    }
    
    public SessionSetting selectSessionSettingByUserAndVPId(long userId, String vpId){
    	if(vpId==null || vpId.trim().equals("") || userId<=0) return null;
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(SessionSetting.class,"SessionSetting");
    	criteria.add(Restrictions.eq("vpId", vpId));
    	criteria.add(Restrictions.eq("userId", new Long(userId)));
    	return (SessionSetting) criteria.uniqueResult();
    	
    }

}
