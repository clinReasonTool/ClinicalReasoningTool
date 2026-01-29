package controller;

import java.util.Locale;

import beans.relation.Relation;
import beans.user.SessionSetting;
import database.DBUser;
import util.CRTLogger;

/**
 * Handles everything around loading and applying SessionSetting objects. Each userId - vpId combination has a 
 * sessionSetting object...
 * @author ingahege
 *
 */
public class SessionSettingController {
	/**
	 * boxes can be on different positions and active/inactive. Max. 4 boxes!
	 */
	public static final boolean dynamicBoxesOn = true;

	static private SessionSettingController instance = new SessionSettingController();
	static public SessionSettingController getInstance() { return instance; }
	
	/**
	 * We check whether the parent VP system has included some session settings we might need to consider. 
	 * If not we use the default settings. 
	 * We try to get the sessionSetting from the database to make sure that if user has started a session, he can continue with 
	 * the same settings.
	 */
	public SessionSetting initSessionSettings(String vpId, long userId, Locale loc){
		DBUser dbu = new DBUser();
		SessionSetting sessSetting = dbu.selectSessionSettingByUserAndVPId(userId, vpId);
		if(sessSetting!=null){
			sessSetting.setExpHintDisplayed(false); //we set it here to false, to make sure that it is displayed even if learner has stopped the session before
			//for backward compatibility reasons we do the following two lines here:
			sessSetting.setListMode(loc);
			dbu.saveAndCommit(sessSetting);
			
			return sessSetting;
		}
		
		sessSetting = new SessionSetting(vpId , userId);
		initSessSetting(sessSetting);
		sessSetting.setListMode(loc);
		dbu.saveAndCommit(sessSetting);
		return sessSetting;
	}
	
	/**
	 * Check which settings are defined by a parent VP system and put them into the SessionSetting object.
	 * @param sessSetting
	 */
	private void initSessSetting(SessionSetting sessSetting){
		sessSetting.setExpFeedbackMode(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_EXP_FB_MODE, 0));
		sessSetting.setPeerFeedbackMode(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_PEER_FB_MODE, 0));
		sessSetting.setDdxMode(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_DDX_MODE, 0));
		initBoxesSettings(sessSetting);
	}
	
	/**
	 * Parameters from course manager about the display mode of each box (hidden, active, passive) are stored in the user session 
	 * @param sessSetting
	 */
	private void initBoxesSettings(SessionSetting sessSetting) {
		sessSetting.setBoxMode1(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_BOXES_P_MODE,1));
		sessSetting.setBoxMode2(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_BOXES_D_MODE,1));
		sessSetting.setBoxMode3(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_BOXES_T_MODE,1));
		sessSetting.setBoxMode4(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_BOXES_M_MODE,1));
	}
}	

