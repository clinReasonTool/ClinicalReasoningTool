package controller;

import java.beans.Statement;
import java.io.*;
import java.util.*;
import javax.faces.context.*;
import javax.servlet.http.HttpServletRequest;

import application.AppBean;
import application.ErrorMessageContainer;
import application.Monitor;
import beans.*;
import beans.scripts.*;
import util.*;

public class AjaxController {
	public static final String REQPARAM_USER = "user_id";
	public static final String REQPARAM_USER_EXT = "userid_ext"; //an external userId (from another system)
	public static final String REQPARAM_SCRIPT = "script_id";
	public static final String REQPARAM_VP = "vp_id";
	public static final String REQPARAM_LOC = "locale";
	public static final String REQPARAM_SCRIPTLOC = "script_locale";
	public static final String REQPARAM_STAGE = "stage";
	public static final String REQPARAM_CHARTTYPE = "chart_type";	//see LearningAnalyticsContainer
	public static final String REQPARAM_CHARTSIZE = "chart_size"; //sm or lg
	public static final String REQPARAM_SEARCHTERM = "search";	//searchterm
	public static final String REQPARAM_TERM = "term";	//term to be addedv
	public static final String REQPARAM_CODE = "code";
	public static final String REQPARAM_SECRET = "secret";	//a shared secret is included in the query
	public static final String REQPARAM_ISSYN = "issyn";
	//public static final String REQPARAM_ENCODED = "encoded"; //are the query params encoded (true | false)
	//public static final String REQPARAM_SYSTEM = "system_id"; //are the query params encoded (true | false)
	public static final String REQPARAM_CHARTPEER = "chart_peer"; //are the query params encoded (true | false)
	public static final String REQPARAM_EXTUID = "uid"; //e.g. session_id or similar
	public static final String REQPARAM_REPORTS_VP = "r_vp_id"; //are the query params encoded (true | false)
	public static final String REQPARAM_REPORTS_SCRIPT_ID = "r_scriptid"; //are the query params encoded (true | false)
	public static final String REQPARAM_EXP_FB_MODE = "expfbmode";
	public static final String REQPARAM_PEER_FB_MODE = "peerfbmode";
	public static final String REQPARAM_DDX_MODE = "ddx_mode";
	public static final String REQPARAM_BOXES_P_MODE = "pm";
	public static final String REQPARAM_BOXES_D_MODE = "dm";
	public static final String REQPARAM_BOXES_T_MODE = "tm";
	public static final String REQPARAM_BOXES_M_MODE = "mm";
	public static final String REQPARAM_BOXES_PAT_MODE = "pat";
	public static final String REQPARAM_BOXES_ND_MODE = "nd"; // Nursing diagnoses
	public static final String REQPARAM_BOXES_NA_MODE = "na"; // Nursing aims
	public static final String REQPARAM_BOXES_NM_MODE = "nm"; // Nursing measures
	public static final String REQPARAM_BOXES_NI_MODE = "ni"; // Information


	
	public static final String REQPARAM_REPORT_ACCESS ="rep_acc"; //parameter indocating access from an educator to a specific learner map
	public static final String REQPARAM_API = "api";
	public static final String REQPARAM_MAXSTAGE = "maxstage";
	public static final String REQPARAM_VP_NAME = "vp_name";
	public static final String REQPARAM_REPORT_LEANER_ID = "learnerId"; //user_id is ALWAYS the person accessing the system, if an educator wants to access a learner script, we have the learner id here...
	public static final String REQPARAM_SEARCH_MODE = "mode";
	public static final String REQPARAM_REPORTS_DISPLAYMODE="repdm"	;

	public static final String REQPARAM_CHECKSUM = "checksum";
	public static final String REQPARAM_CHECKSUM_UID = "checksum_uid";
	public static final String REQPARAM_TS = "ts";

	static private AjaxController instance = new AjaxController();
	static public AjaxController getInstance() { return instance; }
	
	/**
	 * Receive an ajax call and process it. 
	 * params: type = method to call, id=id to add/remove...
	 * @param patillscript
	 * @throws IOException
	 */
	public void receiveAjax(PatientIllnessScript patillscript) throws IOException {
		ExternalContext externalContext = FacesContextWrapper.getCurrentInstance().getExternalContext();

	    Map<String, String> reqParams = externalContext.getRequestParameterMap();
	    if(reqParams!=null){
	    	String patillscriptId = reqParams.get(REQPARAM_SCRIPT);
	    	if(patillscript==null && patillscriptId!=null){ //could be a timeout 
	    		//long patIllScriptId = Long.parseLong(patillscriptId);
	    		new NavigationController().getMyFacesContext().initPatIllScript();
	    	}
	    	if(patillscript==null || patillscriptId==null|| Long.parseLong(patillscriptId)!=patillscript.getId()){
	    		
	    		CRTLogger.out("Error: patillscriptId is:"+ patillscriptId + " is null", CRTLogger.LEVEL_ERROR);
	    		return; //TODO we need some more error handling here, how can this happen? What shall we do? 
	    	}
	    	String methodName = reqParams.get("type"); //deprecated
	    	String idStr = reqParams.get("id");
	    	String nameStr = reqParams.get("name"); //the list entry
	    	String orgNameStr = reqParams.get("orgname"); //what the learner has typed in
	    	String x = reqParams.get("x"); //x-position of an item or startEpId
	    	String y = reqParams.get("y"); //y-position of an item or targetEp
	    	String x1 = reqParams.get("x1"); //x-position of targetEp -> needed for more flexible target enpoints
	    	String box = reqParams.get("box");
	    	String bool = reqParams.get("bool");
	    	String prefix = reqParams.get("prefix");
	    	patillscript.updateStage(reqParams.get(REQPARAM_STAGE));
	    	String action = reqParams.get("action"); //what is performed (deleted, add, change)
	    	String type = reqParams.get("type"); //ddx, nddx, mng,...
	    	if(action!=null && !action.trim().equals("")) methodName = action+type;

	    	//String patIllScriptId = reqParams.get(REQPARAM_SCRIPT); //TODO check whether belongs to currently loaded script!
	    	Statement stmt; 
	    	if(methodName.startsWith("addBox")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, prefix, nameStr});
	    	
	    	else if(methodName.startsWith("moveItem")) stmt = new Statement(patillscript, methodName, new Object[]{idStr,x,y});
	    	else if(methodName.startsWith("addConnection")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr,x, x1,y});
	    	else if(methodName.startsWith("chgConnection")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr});
	    	else if(methodName.startsWith("chgStateOf")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr});
	    	else if (methodName.startsWith("changeTier")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr});
	    	else if (methodName.startsWith("chgSummStCard")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr});
	    	else if (methodName.startsWith("saveSummStatement")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr});
	    	else if (methodName.startsWith("chgBoxType")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr, bool, box});
	    	else if (methodName.startsWith("chgBoxListType")) stmt = new Statement(patillscript, methodName, new Object[]{idStr, box});
	    	else stmt = new Statement(patillscript, methodName, new Object[]{idStr});
	    	
	    	
	    	/*if(x1!=null && !x1.trim().equals("")){ //connection drawing with 
	    		stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr,x, x1,y});
	    	}
	    	else if(x!=null && !x.trim().equals("")){
	    		stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr,x,y});
	    	}  
	    	
	    	
	    	else if(nameStr!=null && !nameStr.trim().equals("") && box!=null && !box.trim().equals("") && bool!=null && !bool.trim().equals(""))
	    		stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr, bool, box});
	    		
	    	else if(nameStr!=null && !nameStr.trim().equals("")) 
	    		stmt = new Statement(patillscript, methodName, new Object[]{idStr, nameStr});
	    	else stmt = new Statement(patillscript, methodName, new Object[]{idStr});*/
	    	
	    	try {
				stmt.execute();				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_PROD);
			}
	    	patillscript.toString();
    		
	    	//use this if making a call that returns an object in a template:
	    	/*Object o = 	((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getAttribute("prob");
	    	if(o==null) o = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getAttribute("ddx");
	    	//if o is null, we assume that an error occured and we have to give back the error messages!!!
	    	if(methodName.startsWith("add") && o!=null){
	    		responseAjaxTemplate(externalContext, reqParams.get("id"), patillscript);
	    	}

	    	else*/ responseAjax(externalContext, reqParams.get("id"), action, type);
	    }
	}
	
	/**
	 * Receive an ajax call and process it. 
	 * params: type = method to call, id=id to add/remove...
	 * @param patillscript
	 * @throws IOException
	 */
	public void receiveAjax(ContextContainer contxt) throws IOException {
		ExternalContext externalContext = FacesContextWrapper.getCurrentInstance().getExternalContext();

	    Map<String, String> reqParams = externalContext.getRequestParameterMap();
	    if(reqParams!=null){
	    	String vpId = reqParams.get(REQPARAM_SCRIPT);
	    	/*if(vpId==null && contxt==null){ //could be a timeout 
	    		new NavigationController().getMyFacesContext().getInitContextContainer();
	    	}*/
	    	if(vpId==null /*|| Long.parseLong(vpId)!=contxt.getVpId()*/){	    		
	    		CRTLogger.out("Error: patillscriptId is:"+ vpId + " is null", CRTLogger.LEVEL_ERROR);
	    		return; //TODO we need some more error handling here, how can this happen? What shall we do? 
	    	}
	    	String type = reqParams.get("type");
	    	String idStr = reqParams.get("id"); // id of the listItem to add
	    	String name = reqParams.get("name"); //the list entry
	    	if(type.equals("addActor")) contxt.addActor(idStr, name);
	    	if(type.equals("delActor")) contxt.delActor(idStr);
	    	if(type.equals("addContext")) contxt.addContext(idStr, name);
	    	if(type.equals("delContext")) contxt.delContext(idStr);
	    	responseAjax(externalContext, reqParams.get("id"));
	    }
	}
	
	/**
	 * Called for chart ajax calls...
	 * @param context
	 */
	public void receiveChartAjax(CRTFacesContext context) throws IOException{
		ExternalContext externalContext = FacesContextWrapper.getCurrentInstance().getExternalContext();
		Map<String, String> reqParams = externalContext.getRequestParameterMap();
		String methodName = reqParams.get("type");
		String idStr = reqParams.get("id");
    	Statement stmt = new Statement(context.getLearningAnalyticsContainer(), methodName, new Object[]{idStr});
    	
    	try {
			stmt.execute();				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
    	responseAjax(externalContext, reqParams.get("id"));

	}
	
	public void receiveMonitorAjax(Monitor m) throws IOException{
		ExternalContext externalContext = FacesContextWrapper.getCurrentInstance().getExternalContext();
		Map<String, String> reqParams = externalContext.getRequestParameterMap();
		String methodName = reqParams.get("type");
		String idStr = reqParams.get("id");
    	Statement stmt = new Statement(m, methodName, new Object[]{idStr});
    	
    	try {
			stmt.execute();				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
    	responseAjax(externalContext, reqParams.get("id"));

	}
	
	public void receiveReportsAjax(ReportBean b) throws IOException{
		ExternalContext externalContext = FacesContextWrapper.getCurrentInstance().getExternalContext();
		Map<String, String> reqParams = externalContext.getRequestParameterMap();
		String methodName = reqParams.get("type");
		String idStr = reqParams.get(REQPARAM_REPORTS_VP);
		Statement stmt;
		if(idStr==null || idStr.trim().equals(""))
			stmt = new Statement(b, methodName, null);
		else stmt = new Statement(b, methodName, new Object[]{idStr});
    	
    	try {
			stmt.execute();				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
    	responseAjax(externalContext, reqParams.get(REQPARAM_REPORTS_VP));

	}
	
	/**
	 * Receive an ajax call and process it. 
	 * params: type = method to call, id=id to add/remove...
	 * @param patillscript
	 * @throws IOException
	 */
	public void receiveAjax(CRTFacesContext crContext) throws IOException {
		ExternalContext externalContext = FacesContextWrapper.getCurrentInstance().getExternalContext();
	    //ExternalContext externalContext = facesContext2.getExternalContext();
	    //ExternalContext ec = fcw.getExternalContext();
	   // Map<String, Object> sessionMap = externalContext.getSessionMap().get("patIll);
	    Map<String, String> reqParams = externalContext.getRequestParameterMap();
	    if(reqParams!=null){
	    	String patillscriptId = reqParams.get(REQPARAM_SCRIPT);
	    	if(crContext==null || patillscriptId==null) {
	    		CRTLogger.out("Error: receiveAjax", CRTLogger.LEVEL_PROD);
	    		return; //TODO we need some more error handling here, how can this happen? What shall we do? 
	    	}
	    	if(!patillscriptId.equals("-99") && (crContext.getPatillscript()==null || Long.parseLong(patillscriptId)!=crContext.getPatillscript().getId())){
	    		CRTLogger.out("Error: receiveAjax", CRTLogger.LEVEL_PROD);
	    		return;
	    	}
	    	String methodName = reqParams.get("type");  //deprecated
	    	String idStr = reqParams.get("id");
	    	String nameStr = reqParams.get("name");
	    	String x = reqParams.get("x");
	    	String y = reqParams.get("y");
	    	String action = reqParams.get("action"); //what is performed (deleted, add, change)
	    	String type = reqParams.get("type"); //ddx, nddx, mng,...
	    	if(action!=null && !action.trim().equals("")) methodName = action+type;
	    	
	    	//patillscript.updateStage(reqParams.get(REQPARAM_STAGE));

	    	//String patIllScriptId = reqParams.get(REQPARAM_SCRIPT); //TODO check whether belongs to currently loaded script!
	    	Statement stmt; 
	    	if(x!=null && !x.trim().equals("")){
	    		stmt = new Statement(crContext, methodName, new Object[]{idStr, nameStr,x,y});
	    	}
	    	else if(nameStr!=null && !nameStr.trim().equals("")) 
	    		stmt = new Statement(crContext, methodName, new Object[]{idStr, nameStr});
	    	else stmt = new Statement(crContext, methodName, new Object[]{idStr});
	    	
	    	try {
				stmt.execute();				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_PROD);
			}
	    	//patillscript.toString();
	    	responseAjax(externalContext, reqParams.get("id"));
	    }
	}
	
	/**
	 * handles the ajax response (xml)
	 * @param externalContext
	 * @throws IOException
	 */
	private void responseAjax(ExternalContext externalContext, String responseId) throws IOException{
		FacesContext facesContext = FacesContext.getCurrentInstance();
	    externalContext.setResponseContentType("text/xml");
	    externalContext.setResponseCharacterEncoding("UTF-8"); 
	    //for additional id info (e.g. when adding a new connection)
		String id2 = (String) ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getAttribute("id2");
		StringBuffer sb = new StringBuffer("<id>"+responseId+"</id>");
		if(id2!=null && !id2.trim().isEmpty())
				sb.append("<id2>"+id2+"</id2>");
	    externalContext.getResponseOutputWriter().write(createResponseXML(sb.toString()));
	    facesContext.responseComplete();
	}
	
	private void responseAjax(ExternalContext externalContext, String responseId, String action, String type) throws IOException{
		FacesContext facesContext = FacesContext.getCurrentInstance();
	    externalContext.setResponseContentType("text/xml");
	    externalContext.setResponseCharacterEncoding("UTF-8"); 
	    //for additional id info (e.g. when adding a new connection)
		String id2 = (String) ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getAttribute("id2");
		StringBuffer sb = new StringBuffer("<id>"+responseId+"</id>");
		if(id2!=null && !id2.trim().isEmpty())
				sb.append("<id2>"+id2+"</id2>");
		sb.append("<action>"+action+"</action>");
		sb.append("<type>"+type+"</type>");
	    externalContext.getResponseOutputWriter().write(createResponseXML(sb.toString()));
	    facesContext.responseComplete();
	}
	
	/**
	 * handles the ajax response (xml) and returns an xml file with the filled subtemplate for the 
	 * created object
	 * @param externalContext
	 * @throws IOException
	 */
	private void responseAjaxTemplate(ExternalContext externalContext, String responseId, PatientIllnessScript patillscript) throws IOException{
		//FacesContext facesContext = FacesContext.getCurrentInstance();
		//List msgs = null;
		//externalContext.setResponseContentType("text/xml");
	    //externalContext.setResponseCharacterEncoding("UTF-8");    
	    
	    try {
			////String template = "/src/html/probbox2.xhtml";
			
			//HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			//HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			//TemplateResponseWriter customResponse  = new TemplateResponseWriter(response);
			//facesContext.getExternalContext().dispatch(template);
			//msgs = facesContext.getMessageList();
			//request.getRequestDispatcher(template).forward(request, customResponse);

			//System.out.println(String.format("The output of %s is %s", template, customResponse.getOutput()));
			//String respXml = createResponseXML("", false);
			//String respXml = createResponseXML(customResponse.getOutput(), false);
			//FacesContext.getCurrentInstance().
			//externalContext.getResponseOutputWriter().write(respXml);
			//externalContext.getResponseOutputWriter().write("hallo");
			//FacesContext.getCurrentInstance().getExternalContext().getFlash().clear();
		} 
	    catch (Exception e) {
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}	    	   
	    //facesContext.responseComplete();
	}
	
	
	private String createResponseXML(String responseParam){
		return createResponseXML(responseParam, true);
	}
	/**
	 * assemble the xml response, include the id and if applicable a message.
	 * @param responseParam
	 * @return
	 */
	private String createResponseXML(String responseParam, boolean includeMsg){
		
		//TODO: we might want to add more info to the response....
		StringBuffer xmlResponse = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><response>");
		xmlResponse.append(responseParam);
		System.out.println(xmlResponse.toString());
		if(includeMsg) appendMessage(xmlResponse);
		xmlResponse.append("</response>");
		return xmlResponse.toString();
	}
	
	/**
	 * If e.g. an error has occurred we get the message from the FacesContext and include it into the xml response.
	 * @param xmlResponse
	 */
	private void appendMessage(StringBuffer xmlResponse){
		new ErrorMessageContainer().toXml(xmlResponse);
	}
	
	public String getRequestParamByKey(String key){
		if(key==null || key.equals("")) return null;
		key = key.trim();
		//key = key.toLowerCase();
		Map<String,String[]> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();
		String[] p1 = p.get(key);
		
		if (p1 != null && p1.length>0){
			String param = Encoder.getInstance().decodeQueryParam(p1[0]);
			return param;
		}
		return null;
	}
	
	public long getLongRequestParamByKeyDecrypt(String key){
		String s = getRequestParamByKey(key);
		if(s==null || s.trim().equals("")) return -1;
		try{
			return Long.parseLong(s);
		}
		catch(Exception e){ return -1;}
	}
	
	public String getRequestParamByKeyNoDecrypt(String key){
		if(key==null || key.equals("")) return null;
		key = key.trim();
		//key = key.toLowerCase();
		Map<String,String[]> p = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap();
		String[] p1 = p.get(key);
		if(p1==null || p1.length==0) return null;
		
			return p1[0];
			//return param;
		//}
		//return null;
	}
	
	public long getLongRequestParamByKey(String key){
		String val = getRequestParamByKey(key);
		if(val!=null && !val.trim().equals("")){
			return Long.parseLong(val);
		}
		return -1;
	}
	
	public boolean getBooleanRequestParamByKey(String key, boolean defVal){
		String val = getRequestParamByKey(key);
		if(val!=null && !val.trim().equals("")){
			return Boolean.parseBoolean(val);
		}
		return defVal;
	}
	
	public int getIntRequestParamByKey(String key, int defVal){
		String val = getRequestParamByKey(key);
		try{
			if(val!=null && !val.trim().equals("") && !val.equals("null"))
				return Integer.parseInt(val);
		}
		catch(Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
		return defVal;
	}
	
	
	/**
	 * Is the given sharedSecret the same as the application's (comparison is case insensitive!)
	 * @param secret
	 * @return
	 */
	public boolean isValidSharedSecret(String secret){
		if(secret==null || secret.trim().equals("")) return false;
		if(secret.equalsIgnoreCase(AppBean.getSharedSecret())) return true;
		return false;
	}

	public boolean isValidSharedSecret(){
		String sharedSecret = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SECRET);
		return isValidSharedSecret(sharedSecret);
	}

}
