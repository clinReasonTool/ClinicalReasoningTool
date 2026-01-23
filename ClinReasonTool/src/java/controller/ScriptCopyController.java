package controller;

import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import application.AppBean;
import beans.relation.*;
import beans.relation.summary.*;
import beans.scripts.PatientIllnessScript;
import beans.scripts.VPScriptRef;
import database.*;
import properties.IntlConfiguration;
import beans.list.*;
import util.CRTLogger;
import util.StringUtilities;

/**
 * Copies a script/map created by an expert including translation into a different language (e.g. from de to en). 
 * @author ingahege
 * TODO: adapt to new mechanism
 *
 */
@ManagedBean(name = "copyctrl", eager = true)
@RequestScoped
public class ScriptCopyController {
	
	private static PatientIllnessScript newScript;
	private static PatientIllnessScript orgScript;
	private static Properties idTable = new Properties();
	private static StringBuffer returnMsg = new StringBuffer();
	public boolean copyExpScript;
	
	/**
	 * We copy/duplicate a map into the same language
	 * @param orgVPId
	 * @param newVPId
	 */
	public static void initCopy(String orgVPId, String newVPId){
		if(orgVPId==null) return;
		if(orgVPId.indexOf("_")<=0) orgVPId = orgVPId+"_2";
		if(newVPId.indexOf("_")<=0) newVPId = newVPId+"_2";
		orgScript = new DBClinReason().selectExpertPatIllScriptByVPId(orgVPId);
		if(orgScript == null|| orgScript.getType()!=PatientIllnessScript.TYPE_EXPERT_CREATED) return;
		copyScript(newVPId);
	}
	
	
	/**
	 * Triggering of map copying via an API (edit/copyscript.xhtml is called)
	 */
	/*public boolean getCopyExpScriptViaAPI(){
		CRTLogger.out("Copy script", CRTLogger.LEVEL_PROD);
		String orgVpId = AjaxController.getInstance().getRequestParamByKey("org_vp_id");
		String newVPId = AjaxController.getInstance().getRequestParamByKey("new_vp_id");
		boolean validSharedSecret = AjaxController.getInstance().isValidSharedSecret();
		if(!validSharedSecret || orgVpId==null || newVPId==null) return false;
		ScriptCopyController.initCopy(orgVpId, newVPId);
		return true;
	}*/
	/**
	 * We trigger the copying and translating of an expert script... 
	 */
	public boolean getCopyExpScript(){
		CRTLogger.out("Copy script", CRTLogger.LEVEL_PROD);
		String orgVpId = AjaxController.getInstance().getRequestParamByKey("org_vp_id");
		String newVPId = AjaxController.getInstance().getRequestParamByKey("new_vp_id");
		String lang = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SCRIPTLOC);
		boolean validSharedSecret = AjaxController.getInstance().isValidSharedSecret();
		if(!validSharedSecret || orgVpId==null || newVPId==null) return false;
		ScriptCopyController.initCopyAndTranslate(orgVpId, newVPId, lang);
		return true;
	}
	public void copyExpScript() {
		getCopyExpScript();
	}
	
	/**
	 * We check whether all items are there and the case is ready for translation, if items are missing in the target language we return 
	 * a message.
	 * @return
	 */
	public String checkExpMap(){
		String orgVpId = AjaxController.getInstance().getRequestParamByKey("org_vp_id");
		String langNew = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SCRIPTLOC);
		return this.checkExpMap(orgVpId, langNew);
	}
	
	/**
	 * We check whether all items are there and the case is ready for translation, if items are missing in the target language we return 
	 * a message.
	 * @return
	 */
	public String checkExpMap(String orgVpId, String langNew){
		if(orgVpId.indexOf("_")<=0) orgVpId = orgVpId+"_2";

		orgScript = new DBClinReason().selectExpertPatIllScriptByVPId(orgVpId);
		if(orgScript == null|| orgScript.getType()!=PatientIllnessScript.TYPE_EXPERT_CREATED) return "Map is null";
		
		StringBuffer sb = new StringBuffer();
		String tmp = null;
		if(orgScript.getBox1Relations()!=null){
			for(int i=0;i<orgScript.getBox1Relations().size();i++){
				tmp = checkItemsTranslation(((Relation)orgScript.getBox1Relations().get(i)).getListItem().getFirstCode(), langNew);
				if (StringUtilities.isValidString(tmp)) {
					sb.append(tmp);
					sb.append("\n");
				}
			}
		}
		if(orgScript.getBox2Relations()!=null){
			for(int i=0;i<orgScript.getBox2Relations().size();i++){
				tmp = checkItemsTranslation(((Relation)orgScript.getBox2Relations().get(i)).getListItem().getFirstCode(), langNew);	
				if (StringUtilities.isValidString(tmp)) {
					sb.append(tmp);
					sb.append("\n");
				}
			}
		}
		if(orgScript.getBox3Relations()!=null){
			for(int i=0;i<orgScript.getBox3Relations().size();i++){
				tmp = checkItemsTranslation(((Relation)orgScript.getBox3Relations().get(i)).getListItem().getFirstCode(), langNew);
				if (StringUtilities.isValidString(tmp)) {
					sb.append(tmp);
					sb.append("\n");
				}		
			}
		}
		if(orgScript.getBox4Relations()!=null){
			for(int i=0;i<orgScript.getBox4Relations().size();i++){
				tmp = checkItemsTranslation(((Relation)orgScript.getBox4Relations().get(i)).getListItem().getFirstCode(), langNew);		
				if (StringUtilities.isValidString(tmp)) {
					sb.append(tmp);
					sb.append("\n");
				}
			}
		}
		if(sb.toString().trim().equals("")) return "check ok"; 
		return sb.toString();
		
	}
	
	private String checkItemsTranslation(String code, String lang){
		Locale loc = new Locale(lang);
		ListItem li = new DBList().selectListItemByCode(code, loc);//getListItem(rel.getProblem().getFirstCode());
		if(li==null) return "Item with code " + code + " missing; ";
		return "";
		
	}

	
	/**
	 * We duplicate the map with the org id and translate it into a different language 
	 * @param orgVPId
	 * @param newVPId
	 * @param lang language to translate into
	 */
	public static void initCopyAndTranslate(String orgVPId, String newVPId, String lang){
		if(orgVPId==null) return;
		if(orgVPId.indexOf("_")<=0) orgVPId = orgVPId+"_2";
		if(newVPId.indexOf("_")<=0) newVPId = newVPId+"_2";
		orgScript = new DBClinReason().selectExpertPatIllScriptByVPId(orgVPId);
		if(orgScript == null|| orgScript.getType()!=PatientIllnessScript.TYPE_EXPERT_CREATED) return;
		
		if(!orgScript.getLocale().getLanguage().equalsIgnoreCase(lang)){ //copy & translate
			copyAndTranslateScript(lang, newVPId);
		}
		else{ //copy only (same language
			copyScript(newVPId);
		}
		if(newScript!=null){
			try {
				NavigationController.getInstance().getAdminFacesContext().getAdminPortfolio().addExpertScript(newScript);
			}
			catch(Exception e) {
				CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			}
		}
	}
	
	/** A script it copied (without any translation)
	 * @param vpId
	 */
	private static void copyScript(String vpId){
		PatientIllnessScript newScript = createNewScript(orgScript.getLocale().getLanguage(), vpId);
		if(newScript==null) return;
		createVPScriptRef(newScript);
		copyBoxes();
		
		if(orgScript.getBox1Relations()!=null){
			if(newScript.getBox1Relations()==null) newScript.setProblems(new ArrayList());
			for(int i=0;i<orgScript.getBox1Relations().size();i++){
				if(newScript.getBox1Relations()==null) newScript.setProblems(new ArrayList());
				newScript.getBox1Relations().add(copyProblem((Relation)orgScript.getBox1Relations().get(i)));
			}
		if(orgScript.getBox2Relations()!=null){
			if(newScript.getBox2Relations()==null) newScript.setDiagnoses(new ArrayList());
			for(int i=0;i<orgScript.getBox2Relations().size();i++){
				newScript.getBox2Relations().add(copyDDX((RelationDiagnosis)orgScript.getBox2Relations().get(i)));
			}
			}
		if(orgScript.getBox3Relations()!=null){
			if(newScript.getBox3Relations()==null) newScript.setTests(new ArrayList());
			for(int i=0;i<orgScript.getBox3Relations().size();i++){
				newScript.getBox3Relations().add(copyTest((Relation)orgScript.getBox3Relations().get(i)));
			}
		}
		if(orgScript.getBox4Relations()!=null){
			if(newScript.getBox4Relations()==null) newScript.setMngs(new ArrayList());
			for(int i=0;i<orgScript.getBox4Relations().size();i++){
				newScript.getBox4Relations().add(copyManagement((Relation)orgScript.getBox4Relations().get(i)));
			}
		}						
			copyConnections();	
			copySummStatement();
		}
	}
	
	/**
	 * TODO!!!
	 */
	private static void copyBoxes() {
		
		
	}
	
	/**
	 * duplicate the script/map and translate all items (if possible) into the new language. 
	 * @param newLang
	 * @param vpId
	 */
	private static void copyAndTranslateScript(String newLang, String vpId){
		PatientIllnessScript newScript = createNewScript(newLang, vpId);
		if(newScript==null) return; //can happen if there is already an expert map!
		createVPScriptRef(newScript);
		if(orgScript.getBox1Relations()!=null){
			for(int i=0;i<orgScript.getBox1Relations().size();i++){
				copyAndTranslateProblem((Relation)orgScript.getBox1Relations().get(i), newLang);
			}
		if(orgScript.getBox2Relations()!=null){
			for(int i=0;i<orgScript.getBox2Relations().size();i++){
				copyAndTranslateDDX((RelationDiagnosis)orgScript.getBox2Relations().get(i));
			}
			}
		if(orgScript.getBox3Relations()!=null){
			for(int i=0;i<orgScript.getBox3Relations().size();i++){
				copyAndTranslateTests((Relation)orgScript.getBox3Relations().get(i));
			}
		}
		if(orgScript.getBox4Relations()!=null){
			for(int i=0;i<orgScript.getBox4Relations().size();i++){
				copyAndTranslateManagements((Relation)orgScript.getBox4Relations().get(i));
			}
		}		
		//TODO new items for nursing etc...
			copyConnections();	
			copySummStatement();
		}
	}
	
	/**
	 * We create and save the VPScriptRef object (which contains the VP name and id).
	 * @param newScript
	 */
	private static void createVPScriptRef(PatientIllnessScript newScript){
		VPScriptRef orgRef = (VPScriptRef) new DBClinReason().getVPScriptRef(orgScript.getVpId());
		if(orgRef==null) return;
		VPScriptRef newRef = (VPScriptRef) new DBClinReason().getVPScriptRef(newScript.getVpId());
		if(newRef!=null) return;//already there!
		
		newRef = new VPScriptRef(newScript.getVpId(), orgRef.getVpName(), orgRef.getSystemId(), "-1");
		new DBClinReason().saveAndCommit(newRef);
		AppBean.addVpScriptRef(newRef);
		
	}
	
	/**
	 * Creates a new map and stores it in the database, where necessary parameters from the orgScript are transferred.
	 * If we already have an emty map, we use this, if the map is not empty, we return null and we have to provide a warning 
	 * for the user. 
	 * @param orgScript
	 * @param newLang
	 * @param vpId
	 * @return
	 */
	private static PatientIllnessScript createNewScript(String newLang, String vpId){
		PatientIllnessScript pis = new DBClinReason().selectExpertPatIllScriptByVPId(vpId);
		if(pis !=null && pis.getIsEmptyScript()){
			newScript = pis;
			returnMsg.append("empty map used, " + pis.getId());
		}
		
		else if(pis==null){ //no script has been created yet... 
			newScript = new PatientIllnessScript(orgScript.getUserId(), vpId, new Locale(newLang));
			newScript.setFinalDDXType(orgScript.getFinalDDXType());
			newScript.setType(PatientIllnessScript.TYPE_EXPERT_CREATED);
			newScript.setCurrentStage(orgScript.getCurrentStage());
			newScript.setSubmittedStage(orgScript.getSubmittedStage());
			newScript.setMaxSubmittedStage(orgScript.getMaxSubmittedStage());
			new DBClinReason().saveAndCommit(newScript);
			returnMsg.append("new map created " + newScript.getId());
			//return newScript;
			
		}
		//map is not empty, so we do not try to override the map! 
		else if(pis!=null && !pis.getIsEmptyScript()){
			returnMsg.append("filled map already there " + pis.getId());
			newScript = null;
			//return null;
		}
		return newScript;
		
	}
	
	private static Relation copyAndTranslateProblem(Relation rel, String newLang){
		RelationProblem newRel = (RelationProblem) copyBasicData(new RelationProblem(), rel, newLang);
		
		ListItem li = getListItem(rel.getListItem().getFirstCode());
		
		if (li!=null){ //we found a match in the list
			newRel.setProblem(li);
			CRTLogger.out(rel.getListItem().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			newRel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(newRel);
			idTable.put(new Long(rel.getId()), newRel.getId());
			return newRel;
		}
		idTable.put(new Long(rel.getId()), new Long(-1));
		return null;
	}
	
	/*private static RelationProblem copyProblem(RelationProblem rel){
		RelationProblem newRel = (RelationProblem) copyBasicData(new RelationProblem(), rel);
		newRel.setListItemId(rel.getListItemId());
		new DBClinReason().saveAndCommit(newRel);
		idTable.put(new Long(rel.getId()), newRel.getId());
		return newRel;
	}*/
	
	private static Relation copyProblem(Relation rel){
		Relation newRel = (Relation) copyBasicData(new RelationProblem(), rel);
		newRel.setListItemId(rel.getListItemId());
		new DBClinReason().saveAndCommit(newRel);
		idTable.put(new Long(rel.getId()), newRel.getId());
		return newRel;
	}
	
	private static RelationDiagnosis copyDDX(RelationDiagnosis rel){
		RelationDiagnosis newRel = (RelationDiagnosis) copyBasicData(new RelationDiagnosis(), rel);
		newRel.setListItemId(rel.getListItemId());
		newRel.setFinalDiagnosis(rel.getFinalDiagnosis());
		newRel.setMnm(rel.getMnm());
		newRel.setWorkingDDX(rel.getWorkingDDX());
		newRel.setRuledOut(rel.getRuledOut());
		newRel.setPrevalence(rel.getPrevalence());
		new DBClinReason().saveAndCommit(newRel);
		idTable.put(new Long(rel.getId()), newRel.getId());
		return newRel;
	}
	
	private static RelationTest copyTest(Relation rel){
		RelationTest newRel = (RelationTest) copyBasicData(new RelationTest(), rel);
		newRel.setListItemId(rel.getListItemId());
		new DBClinReason().saveAndCommit(newRel);
		idTable.put(new Long(rel.getId()), newRel.getId());
		return newRel;
	}
	
	private static RelationManagement copyManagement(Relation rel){
		RelationManagement newRel = (RelationManagement) copyBasicData(new RelationManagement(), rel);
		newRel.setListItemId(rel.getListItemId());
		new DBClinReason().saveAndCommit(newRel);
		idTable.put(new Long(rel.getId()), newRel.getId());
		return newRel;
	}
	
	private static Relation copyAndTranslateDDX(RelationDiagnosis rel){
		RelationDiagnosis newRel = (RelationDiagnosis) copyBasicData(new RelationDiagnosis(), rel);	
		ListItem li = getListItem(rel.getDiagnosis().getFirstCode());
		newRel.setFinalDiagnosis(rel.getFinalDiagnosis());
		newRel.setMnm(rel.getMnm());
		newRel.setWorkingDDX(rel.getWorkingDDX());
		newRel.setRuledOut(rel.getRuledOut());
		newRel.setPrevalence(rel.getPrevalence());
		
		if (li!=null){ //we found a match in the list
			newRel.setDiagnosis(li);
			CRTLogger.out(rel.getDiagnosis().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			newRel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(newRel);
			idTable.put(new Long(rel.getId()), newRel.getId());
			return newRel;
		}
		idTable.put(new Long(rel.getId()), new Long(-1));
		return null;
	}
	
	private static Relation copyAndTranslateTests(Relation rel){
		RelationTest newRel = (RelationTest) copyBasicData(new RelationTest(), rel);	
		ListItem li = getListItem(rel.getListItem().getFirstCode());
		
		if (li!=null){ //we found a match in the list
			newRel.setTest(li);
			CRTLogger.out(rel.getListItem().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			newRel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(newRel);
			idTable.put(new Long(rel.getId()), newRel.getId());
			return newRel;
		}
		idTable.put(new Long(rel.getId()), new Long(-1));
		return null;
	}
	
	private static Relation copyAndTranslateManagements(Relation rel){
		RelationManagement newRel = (RelationManagement) copyBasicData(new RelationManagement(), rel);	
		ListItem li = getListItem(rel.getListItem().getFirstCode());
		
		if (li!=null){ //we found a match in the list
			newRel.setListItemId(li.getItem_id());
			newRel.setManagement(li);
			CRTLogger.out(rel.getListItem().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);

			new DBClinReason().saveAndCommit(newRel);
			idTable.put(new Long(rel.getId()), newRel.getId());
			return newRel;
		}
		idTable.put(new Long(rel.getId()), new Long(-1));
		return null;
	}
	
	private static void copyConnections(){
		if(idTable==null || idTable.isEmpty()) return;
		if(orgScript.getConns()==null || orgScript.getConns().isEmpty()) return; //no connections to copy!
		Iterator<Connection> it = orgScript.getConns().values().iterator();
		while(it.hasNext()){
			Connection orgCnx =it.next();
			//(long startId, long targetId, long illScriptId, int startType, int targetType, int stage)
			Connection newCnx = new Connection();
			newCnx.setIllScriptId(newScript.getId());
			newCnx.setStartType(orgCnx.getStartType());
			newCnx.setTargetType(orgCnx.getTargetType());
			newCnx.setStage(orgCnx.getStage());
			newCnx.setTargetEpIdx(orgCnx.getTargetEpIdx());
			newCnx.setStartEpIdx(orgCnx.getStartEpIdx());
			newCnx.setWeight(orgCnx.getWeight());
			Long newStartId = (Long) idTable.get(new Long(orgCnx.getStartId()));
			Long newTargetId = (Long) idTable.get(new Long(orgCnx.getTargetId()));
			if(newStartId!=null && newTargetId!=null && newStartId.longValue()>0 && newTargetId.longValue()>0){
				newCnx.setStartId(newStartId.longValue());
				newCnx.setTargetId(newTargetId.longValue());
				new DBClinReason().saveAndCommit(newCnx);
			}
					
		}
		
	}
	
	private static Relation copyBasicData(Relation newRel, Relation orgRel) {
		return copyBasicData(newRel, orgRel, null);
	}
	private static Relation copyBasicData(Relation newRel, Relation orgRel, String newLang){
		newRel.setDestId(newScript.getId());
		newRel.setX(orgRel.getX());
		newRel.setY(orgRel.getY());
		newRel.setStage(orgRel.getStage());
		newRel.setOrder(orgRel.getOrder());
		if(newLang == null) newRel.setPrefix(orgRel.getPrefix()); //just copy prefix, no translation needed here  
		else if(newLang!=null && orgRel.getPrefix()!=null && !orgRel.getPrefix().trim().equals("")) { //translate & copy prefix
			newRel.setPrefix(IntlConfiguration.getValue("prefix.negation", new Locale(newLang)));
		}
		return newRel;
	}
	
	
	
	/**
	 * select the ListItem with the given code in the language of the new map
	 * @param code
	 * @return
	 */
	private static ListItem getListItem(String code){	
		return getListItem(code, newScript.getLocale());
	}
	
	/**
	 * select the ListItem with the given code in the language of the new map
	 * @param code
	 * @return
	 */
	public static ListItem getListItem(String code, Locale loc){	
		ListItem li = new DBList().selectListItemByCode(code, loc);
		if(li==null){
			CRTLogger.out("No ListItem found for code " + code, CRTLogger.LEVEL_PROD);
			return null;
		}		 
		return li;
	}
	
	/**
	 * We cannot translate the summary statement, but we copy it, so that a translation in the user interface is easier. 
	 */
	private static void copySummStatement(){
		if(orgScript.getSummStId()>0){
			SummaryStatement summSt = orgScript.getSummSt(); //could that be null? lazy loading?
			
			if(summSt!=null){
				SummaryStatement newStatement = new SummaryStatement();
				newStatement.setText(summSt.getText());
				newStatement.setLang(newScript.getLocale().getLanguage());
				newStatement.setPatillscriptId(newScript.getId());
				newStatement.setType(PatientIllnessScript.TYPE_EXPERT_CREATED);
				newStatement.setStage(summSt.getStage());
				new DBClinReason().saveAndCommit(newStatement);
				newScript.setSummStId(newStatement.getId());
				new DBClinReason().saveAndCommit(newScript);
			}
		}
	}

	public static String getReturnMsg() {
		if(returnMsg==null) return "";
		return returnMsg.toString();
	}
	public static void resetReturnMsg() {
		returnMsg = new StringBuffer("");
	}
}
