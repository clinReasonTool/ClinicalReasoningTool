package controller;

import java.io.Serializable;

//import javax.enterprise.context.RequestScoped;
import  javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.*;

import application.AppBean;
import beans.*;
import beans.scripts.*;
import util.*;
/**
 * handles the navigation between pages (e.g. overview page and single illnessscript page)
 * when a new page is opened we have to make sure that the CRTFacesContext is up-to-date
 * @author ingahege
 *
 */
@ManagedBean(name = "navController", eager = true)
@RequestScoped
public class NavigationController implements Serializable {
	private static final long serialVersionUID = 1L;
	
	static private NavigationController instance = new NavigationController();
	static public NavigationController getInstance() { return instance; }

	/**
	 * user has clicked to open the overview/portfolio page. We have to close the current patientIllnessScript
	 * @return
	 */
	public String openOverview(){
		removePatIllScript();	
		return "portfolio";
	}
		
	/**
	 * User has clicked on a link to open and edit an expert's patientIllnessScript
	 * called from AjaxController
	 * @return
	 */
	public String openExpPatIllScript(){
		AdminFacesContext context = getAdminFacesContext(); 
		if(context!=null){
			context.reset();
			context.initPatIllScript();
		}
		//TODO error handling
		return "exp_boxes";
	}
	
	public String openViewExpPatIllScript(){
		AdminFacesContext context = getAdminFacesContext(); 
		if(context!=null){
			context.reset();
			context.initPatIllScript();
		}
		//TODO error handling
		return "/src/html/view/exp_boxes_view";
	}
	
	
	public String logout(){
		//log the user out and remove all sessionScoped stuff.....
		return "todo"; 
	}
	
	
	/**
	 * TODO move to CRTFacesContext???? 
	 * we have to remove the current patIllScript (if there is one) and sessionId from the externalContext
	 */
	public void removePatIllScript(){
		MyFacesContext myFacesContext = getMyFacesContext();
		if(myFacesContext!=null && myFacesContext.getPatillscript()!=null){
			notifyLog(myFacesContext.getPatillscript());
			myFacesContext.reset();
		}
	}
	
	public CRTFacesContext getCRTFacesContext(){
		return (CRTFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(CRTFacesContext.CRT_FC_KEY);
	}
	
	public MyFacesContext getMyFacesContext(){
		if (FacesContext.getCurrentInstance() ==null) {
			return null;
		}
		
		MyFacesContext learnerContext = (MyFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(CRTFacesContext.CRT_FC_KEY);
		//More security for returning the correct context, if user simultaneously edits and views maps:
		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestServletPath();
		if(learnerContext!=null && (path==null || (!path.contains("edit") && !path.contains("exp_boxes_view"))))		
			return learnerContext;
		
		MyFacesContext adminContext = (MyFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(AdminFacesContext.CRT_FC_KEY);
		return adminContext;
	}
	
	public AdminFacesContext getAdminFacesContext(){
		AdminFacesContext cnxt =  (AdminFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(AdminFacesContext.CRT_FC_KEY);
		return cnxt;
	}
	
	
	/*public PatientIllnessScript getPatientIllnessScript(){
		CRTFacesContext cnxt = getCRTFacesContext();
		if(cnxt!=null) return cnxt.getPatillscript();
		return null;
	}*/
	
	/**
	 * Calls isExpEdit in CRTFacesContext to determine whether currently an expert script is edited
	 * @return
	 */
	/*public boolean isExpEdit(){
		CRTFacesContext cnxt =  (CRTFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(CRTFacesContext.CRT_FC_KEY);
		if(cnxt ==null) return false;
		return cnxt.isExpEdit();
 	}*/
	
	private void notifyLog(PatientIllnessScript patillscript){
		LogEntry le = new LogEntry(LogEntry.CLOSEPATILLSCRIPT_ACTION, patillscript.getId(), patillscript.getId());
		le.save();
	}
	
	public boolean proceedWithVPStoppedFinalDDX(){
		return proceedWithVPStopped(1);
	}
	/**
	 * A VP system might want to prevent the learner from proceeding with the VP if the learner has not yet 
	 * submitted a final diagnosis or summary statement etc. 
	 * @param type (todo definitions)
	 * @return true (=VP is stopped), false (default)
	 */
	private boolean proceedWithVPStopped(int type){
		CRTFacesContext cnxt =  (CRTFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(CRTFacesContext.CRT_FC_KEY);
		if(cnxt==null || cnxt.getPatillscript()==null) return false;
		PatientIllnessScript expScript = AppBean.getExpertPatIllScript(cnxt.getPatillscript().getVpId());
		if(expScript==null) return false;
		//1= final diagnosis sumbitted:
		if(type== 1){
			if(cnxt.getPatillscript().getSubmitted()) return true; //final ddx submitted -> proceed allowed
			if(cnxt.getPatillscript().getCurrentStage()<expScript.getMaxSubmittedStage()) return true; //final ddx not submitted, but maxStage not yet reached -> proceed allowed
		}
		return false;
	}
	
	public void redirect(String url){
		try{
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			//String tmp = externalContext.getRequestContextPath().;
			FacesContext.getCurrentInstance().getExternalContext().redirect(url);
		}
		catch(Exception e){
			CRTLogger.out("Exception: " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
}
