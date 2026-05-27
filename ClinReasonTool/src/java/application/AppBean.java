package application;

import java.io.InputStream;
import java.util.*;

import javax.faces.application.*;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import beans.scripts.*;
import beans.ContextContainer;
import beans.graph.Graph;
import beans.relation.summary.SemanticQual;
import beans.scoring.PeerContainer;
import controller.*;
import controller.SummaryStatementController;
import database.DBClinReason;
import database.HibernateUtil;
import net.casus.util.StandaloneLibUtilities;
import net.casus.util.summarystatement.PerformantSpacyProcessor;
import util.CRTLogger;
import util.StringUtilities;
import properties.IntlConfiguration;

/**
 * We init here some application stuff, like hibernate,....
 * TODO: we could remove the scripts if no parent VP is open.
 * @author ingahege
 *
 */
@ManagedBean(name = "crtInit", eager = true)
@ApplicationScoped
public class AppBean extends ApplicationWrapper implements HttpSessionListener{

	public static final String DEFAULT_LOCALE="en"; //TODO get from properties!
	public static final String[] ACCEPTED_LOCALES = new String[]{"en", "de", "pl", "sv", "es", "pt", "fr", "uk"}; //TODO get from properties!
	public static List<Graph> graphs;
	public static final String APP_KEY = "AppBean";
	public static IntlConfiguration intlConf;
	/**
	 * Any application-wide properties, file is in WEB-INF/classes/globalsettings.properties
	 */
	private static final Properties properties = new Properties();
	
	/**
	 * we dynamically load the experts' scripts into the map if a learner opens a VP that has not been
	 * opened before by a learner.  
	 * 
	 */
	public static Map<String, PatientIllnessScript> expertPatIllScripts;

	/**
	 * we dynamically load the illness scripts into the map if a learner opens a VP that has not been
	 * opened before by a learner.  
	 * 
	 */
	public static Map<Long, List<IllnessScript>> ilnessScripts;

	public static Map<Long, Graph> graph;

	private static PeerContainer peers = new PeerContainer();
	
	/**
	 * key = parentId
	 */
	private static Map<String, VPScriptRef> vpScriptRefs;
	
	/**
	 * lists of semantic qualifiers, key = language ("en", "de"), value is list of SemanticQual objects
	 * for this language.
	 */
	private static Map<String, List<SemanticQual>> semanticQuals;
	/**
	 * called when the application is started... We init Hibernate and a ViewHandler (for Locale handling)
	 * We also put this AppBean into the ServletContext for later access to the applicationScoped scripts
	 * Loading any 
	 */
	
	/**
	 * key = vpId as Long and value is ContextContainer with list of actors and other factors.
	 */
	public static Map<Long, ContextContainer> expertContexts;
	
	public AppBean(){  
		long startms = System.currentTimeMillis();
		CRTLogger.out("Start AppBean init:"  + startms + "ms", CRTLogger.LEVEL_PROD);
		HibernateUtil.initHibernate();
		CRTLogger.out("Hibernate init done:"  + (System.currentTimeMillis() - startms) + "ms", CRTLogger.LEVEL_PROD);
		intlConf = new IntlConfiguration();
		
		ServletContext context = null;
		if (FacesContext.getCurrentInstance() != null) {
			context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			context.setAttribute(APP_KEY, this);
		}
	    
		//IllnessScriptController.addSessionIdToMaps();
	    try{
	    	//load properties for the application(file is in WEB-INF/classes:
	    	InputStream input =  Thread.currentThread().getContextClassLoader().getResourceAsStream("properties/globalsettings.properties");
	    	properties.load(input);
	    }
	    catch(Exception e){}
	    

	    //does not have to be done on every restart:
	   // new JsonCreator().initJsonExport(context);
	    new JsonCreator().exportNursingChapterSeaList(context);

	   //doTestStuff(context);
	    startms = System.currentTimeMillis();
	    CRTLogger.out("Start Peer Container init:"  + startms + "ms", CRTLogger.LEVEL_PROD);
	    //peers.initPeerContainer();
	    CRTLogger.out("End Peer Container init:"  + (System.currentTimeMillis()-startms) + "ms", CRTLogger.LEVEL_PROD);

	    vpScriptRefs =  IllnessScriptController.getInstance().initVpScriptRefs();
	    
	    try{
	    	//new PeerSyncController(peers).sync();
	    }
	    catch(Exception e){
	    	CRTLogger.out("AppBean(): " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
	    }
	   
	    try{
	    	SummaryStatementController.setSIUnitAndTransformList();
	    	//we load the semantic qualifiers and analyze any summary statements that have not yet been analyzed.
	    	if(semanticQuals==null) semanticQuals = SummaryStatementController.loadSemanticQuals();
	    	//SummaryStatementController.testSummStRating();
	    
	    	
	    	//testing calculating case difficulty: 
	    	/*try {
	    		new CaseDifficultyCalculator().calculateDifficulty();
	    	}
	    	catch(Exception e) {
	    		CRTLogger.out("AppBean(): " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
	    	}	    	*/
	    }
	    catch(Exception e){
	    	CRTLogger.out("AppBean(): " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
    	
	    }
	    
	    // init for support lib and local spacy handling, night tabe long -> separate thread?
	    try {
			StandaloneLibUtilities.init();
			PerformantSpacyProcessor.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	    //MeshImporter.importMesh();
	    CRTLogger.out("End AppBean init:"  + (System.currentTimeMillis()-startms) + "ms", CRTLogger.LEVEL_PROD);
	    
	}
	
	/*private void recodeUsrIds(){
		DBUser dbu = new DBUser();
		List<User> users = dbu.selectUsers();
		if(users==null) return; 
		for(int i=0; i<users.size(); i++){
			User u = users.get(i);
			if(u.getExtUserId2().equals("-1")){
				String userId = Encoder.getInstance().decodeQueryParam(u.getExtUserId());
				u.setExtUserId2(userId);
				dbu.saveAndCommit(u);
			}
		}
	}*/
	/**
	 * mapping of longmenu lists with the mesh list
	 * @param context
	 */
	/*private void doTestStuff2(ServletContext context){
		JsonCreator jc = new JsonCreator();
	    jc.setContext(context);
		String[] strarr = jc.s.split(",");
		List<String> matchList = new ArrayList();
		List<String> nomatchList = new ArrayList();
		List<ListItem> list = new DBList().selectListItemByLang("de"); //SummaryStatementController.getListItemsByLang("de");
		for(int i=0; i<strarr.length; i++) {
			boolean isMatch = false;
			for(int j=0; j<list.size();j++){
				if(isMatch) break;
				isMatch = StringUtilities.similarStrings(strarr[i], list.get(j).getName(), new Locale("de"));
				if(isMatch) {
					CRTLogger.out("st liste:" + strarr[i] + ",  mesh " + list.get(j).getName(), CRTLogger.LEVEL_TEST);
					matchList.add(strarr[i]);
					saveMatch(list.get(j), strarr[i]);
					break;
				}
				if(list.get(j).getSynonyma()!=null){
					 Iterator it = list.get(j).getSynonyma().iterator();
					 while(it.hasNext()){
						 Synonym syn = (Synonym) it.next();
						isMatch = StringUtilities.similarStrings(strarr[i], syn.getName() , new Locale("de"));
						if(isMatch) {
							CRTLogger.out("st liste:" + strarr[i] + ",  mesh " + syn.getName(), CRTLogger.LEVEL_TEST);
							matchList.add(strarr[i]);
							saveMatch(syn, strarr[i]);
							break;
						}
					}
				}
			}
			if(!isMatch) {
				CRTLogger.out("st liste:" + strarr[i] + " no match", CRTLogger.LEVEL_TEST);
				nomatchList.add( strarr[i]);
				saveMatch(null, strarr[i]);
			}
			isMatch = false;

		}
		CRTLogger.out("done", CRTLogger.LEVEL_TEST);
	
	}*/
	
	/*private void saveMatch(ListInterface mesh, String lmStr){
			LMMeshMapping lmm = new LMMeshMapping();
			lmm.setLmName(lmStr);
			if(mesh!=null){
				lmm.setMeshId(mesh.getListItemId());
				lmm.setMeshName(mesh.getName());
			}
			new DBList().saveAndCommit(lmm);
		
	}*/
	
	/*private void doTestStuff(ServletContext context){
    	if(semanticQuals==null) semanticQuals = SummaryStatementController.loadSemanticQuals();

	    JsonCreator jc = new JsonCreator();
	    jc.setContext(context);

		List<SummaryStatement> statements = new DBClinReason().getSummaryStatementsByAnalyzed(true);
		if(statements==null || statements.isEmpty()) return;
		Map<Long, String> sumStVpIdLookup = new TreeMap(); //key = sumst id, value = vp_id
		
		Map<String, SummaryStatement> expStatements = new TreeMap(); //key=vp_id, value=expert statement
		Iterator it = statements.iterator();
		try{
			while(it.hasNext()){
				SummaryStatement sumst = (SummaryStatement) it.next();
				PatientIllnessScript pis = new DBClinReason().selectPatIllScriptById(sumst.getPatillscriptId());
				if(pis!=null){
					sumStVpIdLookup.put(new Long(sumst.getId()), pis.getVpId());
					if(sumst.getType()==2) expStatements.put(pis.getVpId(), sumst);		
				}
			}
		}
		catch(Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
		
		for(int i=0;i<statements.size();i++){
			SummaryStatement sumst = statements.get(i);
			String vpId = sumStVpIdLookup.get(new Long(sumst.getId()));
			SummaryStatement expSt = null;
			if(vpId!=null && expStatements!=null) expSt = expStatements.get(vpId);
			new ScoringSummStAction().calculateTransformation(expSt, statements.get(i), jc);
			
		}
		//new ScoringSummStAction().calculateTransformation(null, learnerSt, jc);
		System.out.println("done");
	}*/
	

	
	//public static Monitor getMonitor() {return monitor;}

	/**
	 * We have one experts' patientIllnessScript per parentId (=VPId). If it has not yet been loaded (by another 
	 * learner working on the same VP, we load it and put it into the expertPatIllScripts Map.
	 * @param parentId
	 */
	public synchronized PatientIllnessScript addExpertPatIllnessScriptForVpId(String vpId){
		if(expertPatIllScripts==null) expertPatIllScripts = new HashMap<String, PatientIllnessScript>();
		if(vpId!=null && !expertPatIllScripts.containsKey(vpId)){
			PatientIllnessScript expScript = (PatientIllnessScript) new DBClinReason().selectExpertPatIllScriptByVPId(vpId);
			expScript.initOrLoadBoxes();
			if(expScript!=null) expertPatIllScripts.put(vpId, expScript);
			return expScript;
			//if(graphs!=null && graphs.get(new Long(parentId)!=null)) return
			//todo init graphs?
		}
		if(vpId!=null && expertPatIllScripts.containsKey(vpId)) return expertPatIllScripts.get(vpId);
		return null;
	}
	
	/*public synchronized void addExpertPatIllnessScript(PatientIllnessScript expScript){
		if(expertPatIllScripts==null) expertPatIllScripts = new HashMap<String, PatientIllnessScript>();
		if(expScript!=null && !expertPatIllScripts.containsKey(expScript.getVpId())){
			expertPatIllScripts.put(expScript.getVpId(), expScript);
			
		}
	}*/
	
	public static synchronized void updateExpertPatIllnessScriptForVpId(String vpId){
		try{
			if(expertPatIllScripts==null) expertPatIllScripts = new HashMap<String, PatientIllnessScript>();
			PatientIllnessScript expScript = getExpertPatIllScript(vpId);
			if(expScript == null) return;
			expertPatIllScripts.remove(expScript);
			PatientIllnessScript newExpScript = (PatientIllnessScript) new DBClinReason().selectExpertPatIllScriptByVPId(vpId);
			if(newExpScript!=null) expertPatIllScripts.put(vpId, newExpScript);
		}
		catch (Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
	
	/**
	 * we add the ContextContainer of the expert for the give vpId to the HashMap (needed for scoring)
	 * @param vpId
	 */
	public static synchronized void addExpertContextsForVpId(long vpId) {
		if(expertContexts==null) expertContexts = new HashMap<Long, ContextContainer>();
		if(expertContexts.containsKey(new Long(vpId))) return; //ContextContainer for vpId already included
		expertContexts.put(new Long(vpId), ContextController.getInstance().initExpertContainer(vpId));
	}
	
	public static ContextContainer getExpertContexts(long vpId) {
		if(expertContexts==null) return null;
		return expertContexts.get(new Long(vpId));
	}
	
	public static PatientIllnessScript getExpertPatIllScript(String vpId) {
		if(expertPatIllScripts!=null)
			return expertPatIllScripts.get(vpId);
		return null;
	}
	
	public Map getExpertPatIllScripts() {
		return expertPatIllScripts;
	}

	
	public static List<IllnessScript> getIlnessScripts(String vpId) {
		if(ilnessScripts==null) return null;
		return ilnessScripts.get(vpId);
	}
	
	
	public static PeerContainer getPeers() {return peers;}

	/* (non-Javadoc)
	 * @see javax.faces.application.ApplicationWrapper#getWrapped()
	 */
	public Application getWrapped() {
		return FacesContext.getCurrentInstance().getApplication();
	}

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @return the sharedSecret of the application as defined in the globalsettings.properties file.
	 */
	public static String getSharedSecret(){
		if(properties==null) return null; 
		return properties.getProperty("SharedSecret");
	}
	
	public static boolean getProperty(String key, boolean defaultVal){
		if(properties==null) return defaultVal; 
		String val = properties.getProperty(key);
		try{
			return Boolean.parseBoolean(val);
		}
		catch(Exception e){return defaultVal;}
	}
	
	public static String getProperty(String key, String defaultVal){
		if(properties==null) return defaultVal; 
		try{
			String result = properties.getProperty(key);
			if (result == null) return defaultVal;
			return result;
		}
		catch(Exception e){return defaultVal;}
	}
	
	public static String getVPNameByVPId(String id){
		if(vpScriptRefs==null || vpScriptRefs.get(id)==null) return "";
		return vpScriptRefs.get(id).getVpName();
	}

	/**
	 * @param id
	 * @return
	 * @deprecated
	 */
	public static String getVPSystemByVPId(String id){
		if(vpScriptRefs==null || vpScriptRefs.get(id)==null) return "";
		int systemId = vpScriptRefs.get(id).getSystemId();
		if(systemId==2) return "CASUS";
		if(systemId==4) return "OpenLabyrinth";
		return "Unknown";
		//TODO get available systems from database...
	}
	
	public static String getVPOrgIdByVPId(String id){
		if(vpScriptRefs==null || vpScriptRefs.get(id)==null) return "";
		return vpScriptRefs.get(id).getParentId();
	}
	
	public static Map<String,VPScriptRef> getVpScriptRefs(){
		return vpScriptRefs;
	}
	
	/**
	 * @param ref
	 */
	public static void addVpScriptRef(VPScriptRef ref){
		if(vpScriptRefs==null) vpScriptRefs = new HashMap<String, VPScriptRef>();
		vpScriptRefs.put(ref.getParentId(), ref);
		
	}
	
	public static List<SemanticQual> getSemantiQualsByLang(String lang){
		if(semanticQuals==null) return null;
		return semanticQuals.get(lang);
	}
	
	public static SemanticQual getSemantiQualsByLangAndId(String lang, long id){
		List<SemanticQual> quals = getSemantiQualsByLang(lang);
		if(quals!=null){
			for (int i=0;i<quals.size();i++){
				if(quals.get(i).getId()==id) return quals.get(i);
			}
		}
		return null;
	}
}
