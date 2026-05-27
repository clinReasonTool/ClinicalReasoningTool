package beans;

import java.io.*;
import java.util.*;

import javax.faces.bean.*;
import javax.faces.context.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import application.AppBean;
import application.Monitor;
import beans.graph.Graph;
import beans.list.ListInterface;
import beans.list.ListItem;
import beans.scoring.*;
import beans.scripts.*;
import beans.user.Auth;
import beans.user.SessionSetting;
import beans.user.User;
import controller.*;
import database.DBContext;
import database.DBList;
import net.casus.util.Utility;
import util.CRTLogger;
import util.Encoder;
import util.StringUtilities;

/**
 * The facesContext for a session....
 * We put the CRTFacesContext into the ExternalContext of the FacesContext, so that we can access it throughout the
 * users' session.
 * @author ingahege
 *
 */
@ManagedBean(name = "adminContext", eager = true)
@SessionScoped
public class AdminFacesContext extends FacesContextWrapper implements MyFacesContext, Serializable{
	private static final long serialVersionUID = 1L;

	public static final String CRT_FC_KEY = "adminContext";
	
	private User user;
	
	/**
	 * This is the locale of the navigation etc.
	 */
	private Locale locale;
	private ExpPortfolio adminPortfolio;
	private PatientIllnessScript patillscript;
	private Graph graph;
	private ReportBean reports;
	private FeedbackContainer feedbackContainer;
	private LearningAnalyticsBean labean;
	/**
	 * if context aspects are enabled / opened, we load and store the actors / factors in this container
	 */
	private ContextContainer contxts;
	/**
	 * Any specific settings for this session are stored here, e.g. display of expert feedback variants...
	 * CAVE: is null if no specific settings have been defined.
	 */
	private SessionSetting sessSetting = null;
	
	public String getTest(){return "hallo";}
	
	public AdminFacesContext(){
		//ExternalContext ec =  FacesContextWrapper.getCurrentInstance().getExternalContext();
		locale = LocaleController.setLocale();
		try{
			// CRTLogger.out(FacesContextWrapper.getCurrentInstance().getApplication().getStateManager().getViewState(FacesContext.getCurrentInstance()), CRTLogger.LEVEL_TEST);
			Monitor.addHttpSession((HttpSession)FacesContextWrapper.getCurrentInstance().getExternalContext().getSession(true));
		}
		catch(Exception e){
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}

	}
	
	/**
	 * we load or initiate the context factors the author has already added
	 */
	public boolean getInitContextContainer() {
		//setUser();
		this.contxts = ContextController.getInstance().initContextContainer(contxts, user, locale, ContextContainer.TYPE_AUTHOR);
		return true;
	}
	
	public void setUser(User u){this.user = u;}
	public User getUser(){return user;}
	public Graph getGraph(){
		return graph;}
	
	public long getUserId() {
		if(user!=null) return user.getUserId();
		NavigationController.getInstance().redirect("/crt/src/html/admin/login.xhtml");
		return -1;
	}
	public String getUserIdExt() {
		if(user!=null) return user.getExtUserId2();
		//NavigationController.getInstance().redirect("/crt/src/html/admin/login.xhtml");
		return "";
	}
	
	public boolean isView(){
		String path = FacesContextWrapper.getCurrentInstance().getExternalContext().getRequestServletPath();
		if(path!=null && path.contains("view")) return true;
		return false;
	}
	
	
	public ReportBean getReports() {
		if(reports==null)
			reports = new ReportBean();
		return reports;
	}
	
	public List<FeedbackBean> getFeedbackBeans(){
		if(feedbackContainer==null && this.patillscript==null) return null;
		if(feedbackContainer == null || feedbackContainer.getUserId()!=patillscript.getUserId()){
			feedbackContainer = new FeedbackContainer(patillscript.getId(), patillscript.getUserId());				
			feedbackContainer.initFeedbackContainer();
		}
		if(feedbackContainer==null) return null;
		return feedbackContainer.getFeedbackBeansList();
	}

	public void setReports(ReportBean reports) {
		this.reports = reports;
	}
	
	/**
	 * We want to display all log entries for a map (e.g. to see whether something was deleted), so, we get the log entries from
	 * the database (if not yet done) and return them as a list.
	 * @return
	 */
	public List<LogEntry> getLogEntriesForLearnerScript(){
		if(this.patillscript==null) return null;
		if(this.patillscript.getLogEntries()==null)
			this.patillscript.setLogEntries(LogEntryController.getInstance().getLogEntriesForScript(this.patillscript.getId()));
		return this.patillscript.getLogEntries();
	}

	public AppBean getAppBean(){
	    ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	   return (AppBean) context.getAttribute(AppBean.APP_KEY);
		
	}
	

	/** 
	 * we land here from an ajax request for any actions concerning the patientIllnessScript....
	 **/
	public void ajaxResponseHandler() throws IOException {
		AjaxController.getInstance().receiveAjax(this.getPatillscript());
	}
	
	public void ajaxResponseHandlerReports() throws IOException{
		AjaxController.getInstance().receiveReportsAjax(getReports());
	}
	/* we land here from an ajax request for any actions concerning the facesContext....*/	
	public void ajaxResponseHandlerContext() throws IOException {
		AjaxController.getInstance().receiveAjax(this.getContxts());
	}

	public FacesContext getWrapped() {return FacesContext.getCurrentInstance();}
	
	public Locale getLocale(){return locale;}//LocaleController.getLocale(this).getLanguage();}	
	public String getLanguage(){return locale.getLanguage();}
	
	public ExpPortfolio getAdminPortfolio(){
		if(adminPortfolio==null && user!=null) initAdminPortfolio();
		if(adminPortfolio!=null) return adminPortfolio;
		return null;
	}
	
	private void initAdminPortfolio(){
		if(user==null) return;
		adminPortfolio = new ExpPortfolio(user);
	}
	
	/* (non-Javadoc)
	 * @see beans.MyFacesContext#initSession()
	 */
	public void initPatIllScript(){ 
		if(user==null) return;
		long id = AjaxController.getInstance().getLongRequestParamByKey(AjaxController.REQPARAM_SCRIPT);
		if(this.patillscript!=null && (id<0 || this.patillscript.getId()==id)){
			if(this.graph==null || this.graph.getPatIllScriptId()!=this.patillscript.getId()) initGraph();
			return; //current script already loaded....
		}
		if(id<=0) return;

		if(id>0 && adminPortfolio!=null) this.patillscript = adminPortfolio.getExpScriptById(id);
		
		if(this.patillscript!=null) {
			initGraph();		
		}
		
		if( this.patillscript!=null  && (sessSetting==null ))
			sessSetting = SessionSettingController.getInstance().initSessionSettings(patillscript.getVpId(), user.getUserId(), this.patillscript.getLocale());
	}

	public boolean getInitSession(){
		initPatIllScript();
		getInitContextContainer();
		return true;
	}
	
	public void reset(){
		this.patillscript = null;
		this.graph = null;
	}
	
	public void initGraph(){	  
		if(graph!=null && patillscript!=null && graph.isSameGraph(patillscript.getVpId(), patillscript.getId())) return; //nothing todo, graph already loaded
		graph = new Graph(patillscript.getVpId(), true, patillscript.getId());
		patillscript.initOrLoadBoxes();

	}
	public PatientIllnessScript getPatillscript() {return this.patillscript;}
	
	
	public void setPatillscript(PatientIllnessScript pi) {this.patillscript = pi;}
	public ScoreContainer getScoreContainer(){
		if((labean==null && patillscript!=null) || labean.getPatIllScriptId() != patillscript.getId())
			labean = new LearningAnalyticsBean(patillscript.getId(), patillscript.getUserId(), patillscript.getVpId());
		if(labean!=null) return labean.getScoreContainer();
		return null;
	}
	
	/**
	 * If the edit page is opened via an API from a VP authoring system, we get a paramater ("api=true")
	 * We then have to log the user in and load the script... 
	 * @return
	 */
	public boolean isOpenedViaAPI(){
		boolean isViaAPI =  AjaxController.getInstance().getBooleanRequestParamByKey(AjaxController.REQPARAM_API, false);
		String vpId = AjaxController.getInstance().getRequestParamByKey("vp_id");
		
		try {
			String checksum =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_CHECKSUM);
			String checksum_uid =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_CHECKSUM_UID);
			String ts = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_TS);
			String vp = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_VP);
			String cmp_checksum_src = "" + checksum_uid + "_" + vp +  "_" + ts;
			String cmp_checksum =  Encoder.getInstance().encodeQueryParam(cmp_checksum_src);
			//CRTLogger.out("checksum == cmp_checksum?" + checksum.equals(cmp_checksum), CRTLogger.LEVEL_PROD);
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(isViaAPI){
			try{
				if(this.user==null){ //load user:
					new Auth().loginAdminsViaAPI();
				}
				if(this.user!=null && vpId!=null && (this.patillscript==null || !this.patillscript.getVpIdCrop().trim().equals(vpId))){ //load script and init graph:
					this.patillscript = new ExpPortfolio(this.user).getOrCreateExpScriptFromVPSystem();
					if(this.patillscript!=null) initGraph();
				}
			}
			catch (Exception e){
				CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			}
		}
		return isViaAPI;
	}
	
	/**
	 * the MesH list is recreated from the database
	 */
	public List<ListInterface> getRecreateList(){
		ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String secret = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_SECRET);
		//if(AjaxController.getInstance().isValidSharedSecret(secret))
			new JsonCreator().initJsonExport(context);
		
		return null;
	}
	
	/**
	 * We search for a code and return terms which are in the database
	 * @return
	 */
	public List<ListItem> getSearchedListItems(){
		//ListController lc = new ListController();
		//String mode =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_SEARCH_MODE);
		//String lang =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_LOC);
		String searchterm =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_SEARCHTERM);
		String secret = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_SECRET);
		//if(AjaxController.getInstance().isValidSharedSecret(secret))
			return new DBList().selectListItemsByCode(searchterm);
		//return items;		 
	}
	
	/**
	 * We add a term in a new language for a given code to the database 
	 */
	public void getAddTermForCode() {
		String lang =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_LOC);
		String term =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_TERM);
		String code =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_CODE);
		String secret = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_SECRET);
		String isSyn = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_ISSYN);
		//if(AjaxController.getInstance().isValidSharedSecret(secret))
		if(isSyn!=null && isSyn.trim().equalsIgnoreCase("true"))
			new ListController().createSynonymForCode(lang, code, term);
		else
			new ListController().createItemForCode(lang, code, term);
		//return "";
	}
	
	public SessionSetting getSessSetting(){ 
		return sessSetting;
		}
	
	public PatientIllnessScript getExpertPatIllScript(){
		if(this.patillscript==null) return null;
		return AppBean.getExpertPatIllScript(patillscript.getVpId());
	}
	
	public String getReturnMsg(){
		String msg = ScriptCopyController.getReturnMsg();
		ScriptCopyController.resetReturnMsg();
		return msg;
		
	}
	
	/**
	 * Used for the indiv. map display in the admin area, where we want to display a few expert items (such as 
	 * final diagnosis)
	 * @return
	 */
	public PatientIllnessScript getExpScriptFromPortfolio(){
		if(this.patillscript==null || getAdminPortfolio()==null) return null;
		return adminPortfolio.getExpScriptByVPId(this.patillscript.getVpId());
		
	}
	
	public boolean getAdaptableBoxesEnabled() {return AppBean.getProperty("AdaptableBoxes", false);}
	public ContextContainer getContxts() {
		if(contxts==null) this.contxts = ContextController.getInstance().initContextContainer(contxts, user, locale, ContextContainer.TYPE_AUTHOR);
		return contxts;}
	public void setContxts(ContextContainer contxts) {this.contxts = contxts;}
	
	/**
	 * call e.g. with #{adminContext.getMyListUrl("standard",adminContext.patillscript.locale)}
	 * 
	 * @param type
	 * @param subtpye
	 * @param loc
	 * @return
	 */
	public String getMyListUrl(String type, String lang) {
		return JsonCreator.getDisplayListName("edit", type, lang);
	}
	
	/**
	 * call e.g. with #{adminContext.getMyListUrlByMode("view","standard",adminContext.patillscript.locale)}
	 * 
	 * @param type
	 * @param subtpye
	 * @param loc
	 * @return
	 */
	public String getMyListUrlByMode(String mode, String type, String lang) {
		return JsonCreator.getDisplayListName(mode, type, lang);
	}
}
