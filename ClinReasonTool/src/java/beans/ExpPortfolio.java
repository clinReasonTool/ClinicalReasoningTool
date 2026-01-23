package beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import database.DBClinReason;
import database.DBEditing;
import util.CRTLogger;
import beans.scripts.*;
import beans.user.User;
import controller.AjaxController;
import controller.NavigationController;
import controller.ScriptCopyController;
/**
 * all scripts of an expert, needed for the overview/portfolio page to display a list. 
 *
 */
@ManagedBean(name = "expport", eager = true)
@SessionScoped
public class ExpPortfolio implements Serializable{

	private static final long serialVersionUID = 1L;
	private User user;
	private List<PatientIllnessScript> expscripts;
	
	
	public ExpPortfolio(User u ){
		this.user =u;
		loadScripts();
	}
		
	/** expert maps the admin has access to
	 * @return
	 */
	public List<PatientIllnessScript> getExpscripts() {return expscripts;}

	
	public void addExpertScript(PatientIllnessScript script){
		if(script==null) return;
		if(expscripts==null) expscripts = new ArrayList<PatientIllnessScript>();
		expscripts.add(script);
	}

	/**
	 * if no scripts are available for this user, we display a message
	 * @return
	 */
	public String getNoscripts(){
		if(expscripts==null || expscripts.isEmpty()) return "No scripts available.";
		return "";
	}

	/**
	 * TODO: later on we have to consider the userId to load only scripts that are editable by the current user.
	 */
	private void loadScripts(){
		if(expscripts==null){
			//if(user.isAdmin()) expscripts = new DBEditing().selectAllExpertPatIllScripts(); -> too many scripts are loaded now, 
			
			/*else*/ expscripts = new DBEditing().selectAllExpertPatIllScriptsByUserId(user.getUserId());
		}
	}
	
	public PatientIllnessScript getExpScriptById(long id){
		if(expscripts==null) return null;
		Iterator<PatientIllnessScript> it = expscripts.iterator();
		while(it.hasNext()){
			PatientIllnessScript scr = it.next();
			if(scr.getId()==id) return scr;		
		}
		return null;
	}
	
	public PatientIllnessScript getExpScriptByVPId(String vpId){
		if(expscripts==null) return null;
		Iterator<PatientIllnessScript> it = expscripts.iterator();
		while(it.hasNext()){
			PatientIllnessScript scr = it.next();
			if(scr.getVpId().equalsIgnoreCase(vpId)) return scr;		
		}
		return null;
	}
	
	/**
	 * A new expert script is created and stored into the database
	 */
	public PatientIllnessScript createNewExpScript(){
		CRTLogger.out("Create new script", CRTLogger.LEVEL_PROD);
		String vpId = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_VP);
		String systemId = "2"; //AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SYSTEM);
		if(!vpId.contains("_")) vpId = vpId + "_" +systemId;
		String lang = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SCRIPTLOC);
		if(lang==null || lang.isEmpty()) lang = "en";
		PatientIllnessScript patillscript = new PatientIllnessScript(user.getUserId(), vpId, new Locale(lang));
		int maxStage = AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_MAXSTAGE, -1);
		int maxddxstage = AjaxController.getInstance().getIntRequestParamByKey("maxddxstage", -1);
		patillscript.iniExpertScript(maxStage, maxddxstage);
		new DBClinReason().saveAndCommit(patillscript);
		String vpName = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_VP_NAME);

		VPScriptRef ref= new VPScriptRef(patillscript.getVpId(), vpName, 2, vpId);
		new DBClinReason().saveAndCommit(ref);
		addExpertScript(patillscript);
		CRTLogger.out("Create new script: id= " + patillscript.getId(), CRTLogger.LEVEL_PROD);

		return patillscript;
	}
	
	/**
	 * We trigger the copying and translating of an expert script...
	 * @deprecated here... 
	 */
	public void copyExpScript(){
		CRTLogger.out("Copy script", CRTLogger.LEVEL_PROD);
		String orgVpId = AjaxController.getInstance().getRequestParamByKey("org_vp_id");
		String newVPId = AjaxController.getInstance().getRequestParamByKey("new_vp_id");
		String lang = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SCRIPTLOC);
		ScriptCopyController.initCopyAndTranslate(orgVpId, newVPId, lang);
	}
	
	/**
	 * We check whether an expert script for a vp_id has been created. If not we trigger the creation.
	 */
	public PatientIllnessScript getOrCreateExpScriptFromVPSystem(){
		CRTLogger.out("Create new script", CRTLogger.LEVEL_PROD);
		String vpId = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_VP);
		String systemId = "2"; //AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SYSTEM);
		int maxstage = AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_MAXSTAGE, -1);

		PatientIllnessScript patillscript = new DBClinReason().selectExpertPatIllScriptByVPId(vpId+"_"+systemId);
		if(patillscript!=null){
			if(maxstage>0 && patillscript.getStage()!=maxstage) patillscript.setCurrentStage(maxstage);
			patillscript.initOrLoadBoxes();
			
			patillscript.setLastAccessDate(new Timestamp(System.currentTimeMillis()));
			new DBClinReason().saveAndCommit(patillscript);
			
			return patillscript;
		}
		//not yet created
		return createNewExpScript();
		
	}
	
	/**
	 * called from the overview page to avoid caching issues
	 */
	public boolean getInit(){
		NavigationController.getInstance().getAdminFacesContext().setPatillscript(null);
		return true;
	}
}
