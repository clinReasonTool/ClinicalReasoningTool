package controller;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContextWrapper;

import beans.*;

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

	/**
	 * user has clicked to open the overview/portfolio page. We have to close the current patientIllnessScript
	 * @return
	 */
	public String openOverview(){
		removePatIllScript();	
		return "portfolio";
	}
	
	/**
	 * User has clicked on a link to open a patientIllnessScript, a sessionId has to be included as a request param
	 * called from AjaxController
	 * @return
	 */
	public String openPatIllScript(){
		//long id = new AjaxController().getIdRequestParamByKey(AjaxController.REQPARAM_SCRIPT);
		CRTFacesContext context = getCRTFacesContext(); 
		if(context!=null) context.initSession();
		//TODO error handling
		return "prototype_fs";
	}
	
	public String openPatIllScript(String s){
		CRTFacesContext context = getCRTFacesContext(); 
		if(context!=null) context.initSession();
		//TODO error handling
		return "prototype_fs";
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
		CRTFacesContext crtFacesContext = getCRTFacesContext();
		if(crtFacesContext!=null && crtFacesContext.getPatillscript()!=null){
			//crtFacesContext.setSessionId(-1);
			notifyLog(crtFacesContext.getPatillscript());
			crtFacesContext.setPatillscript(null);
			crtFacesContext.resetGraph();
		}
	}
	
	public CRTFacesContext getCRTFacesContext(){
		CRTFacesContext cnxt =  (CRTFacesContext) FacesContextWrapper.getCurrentInstance().getExternalContext().getSessionMap().get(CRTFacesContext.CRT_FC_KEY);
		/*if(cnxt!=null)*/ return cnxt;
		//return new CRTFacesContext();
	}
	
	private void notifyLog(PatientIllnessScript patillscript){
		LogEntry le = new LogEntry(LogEntry.CLOSEPATILLSCRIPT_ACTION, patillscript.getId(), patillscript.getId());
		le.save();
	}
}
