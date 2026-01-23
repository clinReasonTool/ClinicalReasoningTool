package beans;

import java.io.*;
import java.util.*;
import java.beans.*;

import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import application.AppBean;
import application.Monitor;
import beans.graph.Graph;
import beans.relation.Connection;
import beans.scoring.*;
import beans.scripts.*;
import beans.user.SessionSetting;
import beans.user.User;
import controller.*;
import database.DBContext;
import database.DBUser;
import database.HibernateSession;
import net.casus.util.Utility;
import util.*;
import util.Encoder;

/**
 * The facesContext for a session....
 * We put the CRTFacesContext into the ExternalContext of the FacesContext, so that we can access it throughout the
 * users' session.
 * @author ingahege
 *
 */
@ManagedBean(name = "crtContext", eager = true)
@SessionScoped
public class CRTFacesContext extends FacesContextWrapper implements MyFacesContext/*implements Serializable*/{
	public static final String CRT_FC_KEY = "crtContext";
	
	private User user;
	private User learner; //if access is from reports we store here the learner user object
	private IllnessScriptController isc = new IllnessScriptController();
	private PatientIllnessScript patillscript;
	private Graph graph;
	/**
	 * if context aspects are enabled / opened, we load and store the actors / factors in this container
	 */
	private ContextContainer contxts;	
		
	/**
	 * This is the locale of the navigation etc. script locale (for lists) is in PatientIllnessScript
	 */
	private Locale locale;

	/**
	 * all scripts of the user, needed for the overview/portfolio page to display a list. 
	 * them from view?
	 * We load it from the portfolio view and also here (in case user does not come from portfolio)
	 */
	private PatIllScriptContainer scriptContainer;
	
	/**
	 * This is only related to the current patIllScript and contains a FeedbackBean per stage
	 * We store the feedback Information in the ScoreBean, so, we do not need the feedbackContainer in the 
	 * LearningAnalyticsContainer.
	 */
	private FeedbackContainer feedbackContainer;

	/**
	 * a container for all LearningAnalyticsBeans of a user which contain all ScoreBeans in ScoreContainer objects.
	 */
	private LearningAnalyticsContainer analyticsContainer;
	
	/**
	 * Any specific settings for this session are stored here, e.g. display of expert feedback variants...
	 * CAVE: is null if no specific settings have been defined.
	 */
	private SessionSetting sessSetting = null;
	
	public CRTFacesContext(){
		long startms = System.currentTimeMillis();
		ExternalContext ec =  FacesContextWrapper.getCurrentInstance().getExternalContext();

	    CRTLogger.out("Start CRTFacesContext init:"  + startms + "ms", CRTLogger.LEVEL_PROD);

		locale = LocaleController.setLocale();
		setUser();
		if(user!=null){
		    FacesContextWrapper.getCurrentInstance().getExternalContext().getSessionMap().put(CRTFacesContext.CRT_FC_KEY, this);
		    initPatIllScript();		
		}
		try{
			CRTLogger.out(FacesContextWrapper.getCurrentInstance().getApplication().getStateManager().getViewState(FacesContext.getCurrentInstance()), CRTLogger.LEVEL_TEST);
			Monitor.addHttpSession((HttpSession)FacesContextWrapper.getCurrentInstance().getExternalContext().getSession(true));
		}
		catch(Exception e){}
	    CRTLogger.out("End CRTFacesContext init: "  + (System.currentTimeMillis()- startms) +"ms", CRTLogger.LEVEL_PROD);
	}
	
	/**
	 * Load and build the graph, which is the basis of all operations!
	 */
	private void initGraph(){	    
		if(graph!=null && patillscript!=null && graph.isSameGraph(patillscript.getVpId(), patillscript.getId())) return; //nothing todo, graph already loaded
		long startms = System.currentTimeMillis();
		CRTLogger.out("Start Graph init:"  + startms + "ms", CRTLogger.LEVEL_PROD);
		graph = new Graph(patillscript.getVpId(), patillscript.getId());
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);	
	    CRTLogger.out("End Graph init: "  + (System.currentTimeMillis() - startms) + "ms", CRTLogger.LEVEL_PROD);
	}

	/**
	 * Setting the userId, either directly from query param or from query param as an external userid (then we 
	 * have to get the User object and the userId from the database). If no user is found we create a new one. 
	 */
	private void setUser(){
		String setUserIdStr = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_USER);
		String extUserId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_USER_EXT);
		int systemId = 2; //AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_SYSTEM, -1);
		if(setUserIdStr==null && extUserId==null){
			return;
		}
		//userIdStr is same as userId of loaded user -> return
		if(user!=null && setUserIdStr!=null && user.getUserId()==Long.valueOf(setUserIdStr).longValue()) return; 
		//extUserId of loaded user is same as extUserId -> return
		if(user!=null && extUserId!=null && user.getExtUserId()!=null && user.getExtUserId().equals(extUserId)) return; 
		if(setUserIdStr!=null && !setUserIdStr.trim().equals("")){
			user = new DBUser().selectUserById(Long.valueOf(setUserIdStr).longValue());
			userHasChanged(); //user is different
		}

		else if(extUserId!=null && !extUserId.trim().equals("")){
			user =  new UserController().getUser(systemId, extUserId);
			userHasChanged(); //user is different
		}
		if(user==null){
			CRTLogger.out("Userid is null", CRTLogger.LEVEL_ERROR);
			userHasChanged(); //just to be sure...
			FacesContextWrapper.getCurrentInstance().addMessage("",new FacesMessage(FacesMessage.SEVERITY_ERROR, "userid is null",""));
		}
	}
	
	private void setUser(long userId){
		if(user!=null && user.getUserId()==userId) return;
		user =  new DBUser().selectUserById(userId);
	}

	public long getUserId() {
		if(user!=null) return user.getUserId();
		initPatIllScript();
		if(user!=null) return user.getUserId();
		return -1;
	}
	public User getUser(){return user;}
	public User getLearner() {return learner;}
	public Graph getGraph() {
		return graph;}

	/**
	 * Depending on the sessionSettings we hide/display the expert feedback. Default is always display....
	 * @return
	 */
	public boolean getDisplayExpFb(){
		try{
			if(sessSetting==null) return true;
			if(AppBean.getExpertPatIllScript(patillscript.getVpId())==null) return false; //do not display expert feedback if there is no expert VP script!
			return sessSetting.displayExpFeedback(patillscript.getStage(), AppBean.getExpertPatIllScript(patillscript.getVpId()).getCurrentStage());
		}
		catch(Exception e){
			CRTLogger.out("Exception + " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return true;
		}
	}
	
	/**
	 * If the learner comes to a stage where the expert feedback is available for the first time, we display a 
	 * hint for him/her. 
	 * @return true (display a hint), false (no hint)
	 */
	public boolean getChgDisplayExpFb(){
		try{
			if(sessSetting==null) return false;
			if(AppBean.getExpertPatIllScript(patillscript.getVpId())==null) return false;
			return sessSetting.displayExpFeedbackHint(patillscript.getStage(), AppBean.getExpertPatIllScript(patillscript.getVpId()).getCurrentStage());
		}
		catch(Exception e){
			CRTLogger.out("Exception + " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return false;
		}
	}
	
	/**
	 * We return the video url (from global properties) depending on the current interface language and 
	 * session settings (different video if feedback only available at the end)
	 * @return
	 */
	public String getToolVideoUrl(){
		String lang = LocaleController.getLocale().getLanguage(); 
		String url = null;
		if(sessSetting==null || sessSetting.getExpFeedbackMode()!=SessionSetting.EXPFEEDBACKMODE_END){
			if(lang!=null) url = AppBean.getProperty("helpvideo_url_"+lang, null);
		}
		else if(sessSetting.getExpFeedbackMode()==SessionSetting.EXPFEEDBACKMODE_END) 
			url =  AppBean.getProperty("helpvideo_url_"+lang +"endfb", null);
		if(url==null) url = "https://youtu.be/WgQxinhiw24"; //default
		return url;
	}
	
	public String getSumstVideoUrl(){
		String lang = LocaleController.getLocale().getLanguage();
		if(lang!=null) return AppBean.getProperty("sumstvideo_url_"+lang, null);
		return "https://youtu.be/zvlNSU2ys7k";
	}
	
	/**
	 * if the user is new and has added two different items for the first time, we display a hint that he/she can 
	 * draw connections between such elements.
	 * @return
	 */
	public boolean getDisplayCnxHint(){
		if(user==null && user.getUserSetting()==null) return false;
		if(!user.getUserSetting().getIsNewUser() || !user.getUserSetting().isDisplayCnxHint()) return false;
		if(new GraphController(graph).hasTwoDiffVertices()){
			user.getUserSetting().setDisplayCnxHint(false);
			return true;
		}		
		return false;
	}
	
	public SessionSetting getSessSetting(){ 
		return sessSetting;
		}

	public LearningAnalyticsBean getLearningAnalytics() {
		if(patillscript.isExpScript()) return null;
		if(analyticsContainer==null) return null;//analyticsContainer = new LearningAnalyticsContainer(userId);
		return analyticsContainer.getLearningAnalyticsBeanByPatIllScriptId(patillscript.getId(), patillscript.getVpId());
	}
	
	public LearningAnalyticsContainer getLearningAnalyticsContainer() {
		//if(user==null) setUser();
		if(analyticsContainer==null) initLearningAnalyticsContainer();

		return analyticsContainer;
	}

	public void initScriptContainer(){

		if(user==null) setUser();
		if(user==null) return;//not sure why this happens sometimes....
		//if not yet loaded or from a different user we set scriptContainer:
		if(scriptContainer==null || scriptContainer.getUserId()!=user.getUserId()){
			long startms =  System.currentTimeMillis();
		    CRTLogger.out("Start ScripConatainer init:"  + startms +"ms", CRTLogger.LEVEL_PROD);

			scriptContainer = new PatIllScriptContainer(user.getUserId());
			scriptContainer.loadScriptsOfUser();
		    CRTLogger.out("End ScripConatainer init:"  + (System.currentTimeMillis()-startms) + "ms", CRTLogger.LEVEL_PROD);
		}	
	}
	
	public PatIllScriptContainer getScriptContainer(){ 
		if(scriptContainer==null) initScriptContainer();
		return scriptContainer;
	}

	/**
	 * Init of the feedbackContainer (in LearningAnalytics), learningAnalytics object, and FeedbackBean container 
	 * @param parentId
	 */
	private void initFeedbackContainer(){		

		if(patillscript!=null) {
			long startms = System.currentTimeMillis();
			if(feedbackContainer==null || feedbackContainer.getUserId()!=user.getUserId()){
			    CRTLogger.out("Start FeedbackContainer init: "  + startms + "ms", CRTLogger.LEVEL_PROD);

				feedbackContainer = new FeedbackContainer(patillscript.getId(), user.getUserId());				
				feedbackContainer.initFeedbackContainer();
			    CRTLogger.out("End FeedbackContainer init:"  + (System.currentTimeMillis()-startms) + "ms", CRTLogger.LEVEL_PROD);

			}
			startms = System.currentTimeMillis();
		}

	}
	
	private void initLearningAnalyticsContainer(){
		if(user==null) setUser();
		if(user==null) return;
		long startms = System.currentTimeMillis();
		if(analyticsContainer == null || analyticsContainer.getUserId() != user.getUserId()){ //load learningAnalyticsContainer also if not script is edited -> needed for charts etc...
		    CRTLogger.out("Start LearningAnalyticsContainer init:"  + startms + "ms", CRTLogger.LEVEL_PROD);
			analyticsContainer = new LearningAnalyticsContainer(user.getUserId());
		    CRTLogger.out("End LearningAnalyticsContainer init:"  + (System.currentTimeMillis()-startms) + "ms", CRTLogger.LEVEL_PROD);

		}	
		if(patillscript!=null)
			analyticsContainer.addLearningAnalyticsBean(patillscript.getId(), patillscript.getVpId());

	}
	
	
	/**
	 * load PatientIllnessScript based on id or sessionId
	 */
	public void initPatIllScript(){ 
		long startms = System.currentTimeMillis();
		int reportsAccess = AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_REPORT_ACCESS, 0);

		try {
			String checksum =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_CHECKSUM);
			String checksum_uid =  AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_CHECKSUM_UID);
			String ts = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_TS);
			String vp = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_VP);
			String cmp_checksum_src = "" + checksum_uid + "_" + vp +  "_" + ts;
			String cmp_checksum =  Encoder.getInstance().encodeQueryParam(cmp_checksum_src);
			CRTLogger.out("checksum == cmp_checksum?" + checksum.equals(cmp_checksum), CRTLogger.LEVEL_PROD);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*if(user==null)*/ setUser();
		if(reportsAccess==1){ //then an educator tries to access a learner script e.g. from a reporting or dashboard area - we load the map in a view mode
			loadScriptForReportAccess();
			return;
		}
		initScriptContainer(); //this loads all scripts, needed for overview page and availability bias determination
		
		long id = AjaxController.getInstance().getLongRequestParamByKey(AjaxController.REQPARAM_SCRIPT);
		String vpId = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_VP);
		int systemId = 2; //AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_SYSTEM, -1);
		String extUId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_EXTUID);
		
		//current script already loaded....-> then return here....
		if(this.patillscript!=null && (id<0 || this.patillscript.getId()==id) && this.patillscript.getUserId()==this.user.getUserId() && (extUId==null || this.patillscript.getExtUId().equals(extUId))) return; 

		if(id<=0 && vpId==null)
			return; //then user has opened the overview page...

		if(id>0){ //open an created script
			this.patillscript = isc.loadPatIllScriptById(id, user.getUserId());
			if(this.patillscript!=null && user==null) 
				setUser(this.patillscript.getUserId()); //can happen if we have to re-init after timeout, there we do not get the userId, just the illscriptId

		}
		else if(vpId!=null && !vpId.equals("") && systemId>0 && user!=null){ //look whether script created, if not create it...
			this.patillscript = isc.loadIllnessScriptsByVpId(user.getUserId(), vpId+"_"+systemId, extUId);
			if(this.patillscript==null){
				this.patillscript = isc.createAndSaveNewPatientIllnessScript(user.getUserId(), vpId, extUId);
				//for debugging purposes we log the userAgent:
				if(patillscript!=null){
					String userAgent = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("User-Agent");
					CRTLogger.out("SCRIPT_CREATED: id= "+ patillscript.getId() + " user_id= " + this.getUserId() + " httpsess= " + userAgent , CRTLogger.LEVEL_PROD);
				}
			}
		}		
		//TODO error handling!!!!
		initLearningAnalyticsContainer();
		loadExpScripts(true);
		
		initFeedbackContainer();
		if(user!=null && this.patillscript!=null && vpId!=null && (sessSetting==null || !sessSetting.getVpId().equals(vpId))){
				sessSetting = SessionSettingController.getInstance().initSessionSettings(vpId, user.getUserId(), this.patillscript.getLocale());				
		}
		
		if(this.patillscript!=null){
			initGraph();		
		}
		
		long endms = System.currentTimeMillis();
	    CRTLogger.out("End Session init:"  + (endms-startms) + " ms", CRTLogger.LEVEL_PROD);
	    if(patillscript!=null){
	    	PatientIllnessScript pi = AppBean.getExpertPatIllScript(this.patillscript.getVpId());
	    	if(pi==null || pi.getConns()==null) CRTLogger.out("expscript: no cnxs",CRTLogger.LEVEL_PROD);
	    	else{
	    		Iterator it = pi.getConns().values().iterator();
	    		while(it.hasNext()){
	    			Connection cnx = (Connection) it.next();
	    			CRTLogger.out("expscript: cnx id: " + cnx.getId(), CRTLogger.LEVEL_PROD);
	    		}	    		
	    	}
	    }
	    //just to make sure user has not changed language we init it here again: 
	    locale = LocaleController.setLocale();
	}
	
	/**
	 * If an educator wants to access a learner map/script we load the requested script here, if we have a vpId and learnerId...
	 */
	private void loadScriptForReportAccess(){
		String vpId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_VP);
		int systemId = 2; //AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_SYSTEM, -1); 
		String extUId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_EXTUID); //encrypted session id
		String learnerId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_REPORT_LEANER_ID); //encrypted learner id 
		
		if(this.user!=null && this.patillscript!=null && this.patillscript.getExtUId().equals(extUId)) return; //then script has already been loaded
		long crtLearnerId = -1;
		if(learnerId!=null){
			 learner = new DBUser().selectUserByExternalId(learnerId, systemId);
			if(learner!=null) crtLearnerId = learner.getUserId();
		} 
		//TODO check for shared secret!
		if(crtLearnerId>0 && vpId!=null){
			patillscript = isc.loadIllnessScriptsByVpId(crtLearnerId, vpId, extUId);
		}
		if(this.patillscript!=null){
			loadExpScripts(false);
			initGraph();		
			analyticsContainer = new LearningAnalyticsContainer(learner.getUserId());
			analyticsContainer.addLearningAnalyticsBean(patillscript.getId(), patillscript.getVpId());
		}
	}
	
	/**
	 * we load or initiate the context factors the learner has already added and make sure that the expert context container is added 
	 * to the Map in the AppBean 
	 */
	public boolean getInitContextContainer() {
		try {
			setUser();
			this.contxts = ContextController.getInstance().initContextContainer(contxts, user, locale, ContextContainer.TYPE_PLAYER);
			return true;
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return false;
		}
	}
	
	/**
	 * TODO: we need an identifier which one to load and open
	 * @return
	 */
	public boolean getInitSession(){
		initPatIllScript();
		//TODO: identify if player or authoring for setting type
		getInitContextContainer();
		return true;
	}
	
	/**
	 * Loads the expert script and also sets the types of boxes and the language from it. 
	 * @param setMaxStage
	 */
	private void loadExpScripts(boolean setMaxStage){
		if(patillscript==null) return;
		AppBean app = getAppBean();
		PatientIllnessScript expScript = app.addExpertPatIllnessScriptForVpId(patillscript.getVpId());
		//we have to overtake the max stage in which the final ddx has to be submitted from the expert's script: 
		if(setMaxStage && patillscript.getMaxSubmittedStage()<=0 && expScript!=null){
			patillscript.setMaxSubmittedStage(expScript.getMaxSubmittedStage());
			
			//backward compatibility - make sure that expert script has the boxes set: 
			expScript.initOrLoadBoxes();
			patillscript.setLocale(expScript.getLocale());
			initMapBoxes(expScript);
			patillscript.save();
		}	
	}
	
	/**
	 * We init the boxes for the learner script from the expert script.
	 * @param expScript
	 */
	private void initMapBoxes(PatientIllnessScript expScript) {
		if(patillscript.getBox1()!=null) return; //Boxes are already loaded, we do not have to ini them again. 
		this.patillscript.setBoxes(expScript.getBox1(), expScript.getBox2(), expScript.getBox3(), expScript.getBox4());
	}
	
	public AppBean getAppBean(){
	    ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	   return (AppBean) context.getAttribute(AppBean.APP_KEY);
		
	}
	
	public PatientIllnessScript getPatillscript() { 
		LocaleController.setLocale();
		return patillscript;
	}
	
	public boolean isView(){
		int reportsAccess = AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_REPORT_ACCESS, 0);
		if(reportsAccess==1) return true;

		return false;
	}
		
	public void setPatillscript(PatientIllnessScript patillscript) { this.patillscript = patillscript;}	
	
	public ScoreContainer getScoreContainer() {
		LearningAnalyticsBean lab = getLearningAnalytics();
		if(lab==null) return null;
		return lab.getScoreContainer();
	}
	
	public FeedbackContainer getFeedbackContainer() {
		if(feedbackContainer==null) feedbackContainer = new FeedbackContainer(this.getPatillscript().getId(),user.getUserId());
		return feedbackContainer;
	}
	public void toogleExpBoxFeedback(String toggleStr, String taskStr){
		getFeedbackContainer().toogleExpBoxFeedback(toggleStr, taskStr);
	}
	
	public void toogleExpFeedback(String toggleStr){
		getFeedbackContainer().toogleExpFeedback(toggleStr, this.patillscript.getCurrentStage());
	}
	
	public void createClickLogEntry(String action, String item){
		getFeedbackContainer().createClickLogEntry(Integer.parseInt(action), Long.parseLong(item));
	}
	
	public void tooglePeerBoxFeedback(String toggleStr, String taskStr){
		getFeedbackContainer().tooglePeerBoxFeedback(toggleStr, taskStr, this.patillscript.getCurrentStage());
	}
	
	/** 
	 * we land here from an ajax request for any actions concerning the patientIllnessScript....
	 **/
	public void ajaxResponseHandler() throws IOException {
		AjaxController.getInstance().receiveAjax(this.getPatillscript());
	}
	
	public void ajaxChartResponseHandler() throws IOException {
		AjaxController.getInstance().receiveChartAjax(this);
	}
	
	/* we land here from an ajax request for any actions concerning the facesContext....*/	
	public void ajaxResponseHandler2() throws IOException {
		AjaxController.getInstance().receiveAjax(this);
	}
	/* we land here from an ajax request for any actions concerning the facesContext....*/	
	public void ajaxResponseHandlerContext() throws IOException {
		AjaxController.getInstance().receiveAjax(this.getContxts());
	}
	
	/**
	 * reset all objects related to the current patientIllnessScript. Call this when the user opens another 
	 * VP
	 */
	public void reset(){
		this.graph = null;
		setPatillscript(null);
		this.graph = null;
		this.feedbackContainer = null;
		this.sessSetting = null;
	}
	
	private void userHasChanged(){
		reset();
		this.analyticsContainer = null;
		this.scriptContainer = null;
	}

	public FacesContext getWrapped() {return FacesContext.getCurrentInstance();}
	
	/*public ExpViewPatientIllnessScript getExpPatIllScript(){
		int stage = 1;
		if(patillscript!=null) stage = patillscript.getCurrentStage();
		return new ExpViewPatientIllnessScript(patillscript, stage);
	}*/
	
	public PatientIllnessScript getExpPatIllScript(){
		if(this.patillscript==null) return null;
		return AppBean.getExpertPatIllScript(patillscript.getVpId());
	}
	
	/*public boolean isExpEdit(){
		if(this.patillscript!=null && this.patillscript.getType()==IllnessScriptInterface.TYPE_EXPERT_CREATED) return true;
		return false;
	}*/
	
	public Locale getLocale(){ return locale;}//LocaleController.getLocale(this).getLanguage();}	
	public String getLanguage(){return locale.getLanguage();}
	public float getScoreForAllowReSubmit(){return ScoringController.scoreForAllowReSubmit;}
	
	/**
	 * new version enabled in which author can choose from several box types 
	 * @return
	 */
	public boolean getAdaptableBoxesEnabled() {return AppBean.getProperty("AdaptableBoxes", false);}

	public ContextContainer getContxts() {return contxts;}
	public void setContxts(ContextContainer contxts) {this.contxts = contxts;}
	
	/**
	 * call e.g. with #{crtContext.getMyListUrl("standard",crtContext.patillscript.locale)}
	 * 
	 * @param type
	 * @param subtpye
	 * @param loc
	 * @return
	 */
	public String getMyListUrl(String type, String lang) {
		return JsonCreator.getDisplayListName("view", type, lang);
	}
}
