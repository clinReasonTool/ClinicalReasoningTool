package controller;

import java.io.Serializable;
import java.util.*;

import application.AppBean;
import beans.scripts.*;
import beans.relation.Relation;
import beans.relation.RelationDiagnosis;
import beans.scoring.PeerBean;
import beans.scoring.ScoreBean;
import database.DBClinReason;
import database.DBScoring;
import properties.IntlConfiguration;
import util.CRTLogger;
import util.Encoder;
import util.StringUtilities;

/**
 * Helper class for creating and loading Illness scripts based on sessionId, userId, or id....
 * @author ingahege
 *
 */
public class IllnessScriptController implements Serializable{

	private static final long serialVersionUID = 1L;
	static private IllnessScriptController instance = new IllnessScriptController();
	static public IllnessScriptController getInstance() { return instance; }


	//TODO: we could also get the current script from the already loaded list -> reduces DB calls!
	public PatientIllnessScript loadPatIllScriptById(long id, long userId){
		if(id>0){
			PatientIllnessScript patillscript = new DBClinReason().selectLearnerPatIllScript(id, "id");
			return patillscript;
		}
		else{
			return null;
			//TODO error message?
		}
	}
	
	/**
	 * get all IllnessScripts for a parent id from the database. 
	 * @param parentId
	 * @return
	 */
	public PatientIllnessScript loadIllnessScriptsByVpId(long userId, String vpId, String extUId){
		if(vpId!=null && !vpId.equals("") && userId>0){
			PatientIllnessScript patillscript =new DBClinReason().selectPatIllScriptsByUserIdAndVpId(userId, vpId, extUId);			
			return patillscript;
		}
		return null;
	}
	
	
	/**
	 * We create a new PatientIllnessScript and save it. 
	 * At this point the userId is already the internal userId of the tool! 
	 * @param userId (crt userId)
	 * @param vpId
	 * @param systemId
	 * @param extUId (encyrpted)
	 * @return
	 */
	public PatientIllnessScript createAndSaveNewPatientIllnessScript(long userId, String vpId/*, int systemId*/, String extUId){
		if(vpId==null || userId<=0) return null;

		checkAndSetDeleteFlagOfOldSCriptsAndScore(userId, vpId);
		//better would be to get the language from the expert script in case the case language has been changed for any reason -> 
		//so we reset the locale when we have loaded the expert script!
		Locale loc = LocaleController.getInstance().getScriptLocale();
		PatientIllnessScript patillscript = new PatientIllnessScript( userId, vpId, loc/*, systemId*/);
		patillscript.setExtUId(extUId);
		patillscript.setSessionId(extUId);
		patillscript.save();

		CRTLogger.out("New PatIllScript created for vp_id: " + vpId, CRTLogger.LEVEL_PROD);
		addScriptCreationToPeerBean(patillscript);
		
		return patillscript;
	}
	
	/**
	 * Before we save the new patenIllnessscript we check whether the user has older scripts for the same vpId. If so, 
	 * we mark these scripts with deleteFlag=1 to make sure we do not load it into the scriptsOfUser container.
	 * We do the same with the ScoreBeans...
	 * @param userId
	 * @param vpId
	 */
	private void checkAndSetDeleteFlagOfOldSCriptsAndScore(long userId, String vpId/*, int systemId*/){
		DBClinReason dcr = new DBClinReason(); 
		List<PatientIllnessScript> scripts = dcr.selectPatIllScriptsByUserIdAndVpId(userId, vpId+"_"+2);
		if(scripts==null || scripts.isEmpty()) return;
		ArrayList<Long> scriptIds = new ArrayList();
		for(int i=0; i<scripts.size();i++){
			scripts.get(i).setDeleteFlag(true);
			scriptIds.add(new Long(scripts.get(i).getId()));
		}
		checkAndSetDeleteFlagOfOldScores(scriptIds);
		dcr.saveAndCommit(scripts);
		
	}
	
	/**
	 * We check whether we have "undeleted" scoreBeans for the given scriptIds and set the delete flag to true/1
	 * if we find any.
	 * @param scriptsIds
	 */
	private void checkAndSetDeleteFlagOfOldScores(List<Long> scriptsIds){
		if(scriptsIds==null || scriptsIds.isEmpty()) return;
		DBScoring dbs = new DBScoring();
		List<ScoreBean> scores = dbs.selectScoreBeansByPatIllScriptIds(scriptsIds);
		if(scores==null || scores.isEmpty()) return;
		for(int i=0;i<scores.size(); i++){
			scores.get(i).setDeleteFlag(true);
		}
		dbs.saveAndCommit(scores);
		
	}
	
	/**
	 * We create a PeerBean for the creation of the script of increment the number in the peerBean.
	 * @param patillscript
	 */
	private void addScriptCreationToPeerBean(PatientIllnessScript patillscript){
		try{
			PeerBean peer = AppBean.getPeers().getPeerBeanByIllScriptCreationActionAndVpId(patillscript.getVpId());
			if(peer==null){
				peer = new PeerSyncController(AppBean.getPeers()).createNewPeerBean(ScoreBean.TYPE_SCRIPT_CREATION, patillscript.getVpId(), -1, /*0,*/ 0, 0, 0);
			}
			else{
				peer.incrPeerNum();
				//new DBScoring().saveAndCommit(peer);
			}
			new DBScoring().saveAndCommit(peer);
		}
		catch(Exception e){
			CRTLogger.out("IllnessScriptController.addScriptCreationToPeerBean:" + StringUtilities.stackTraceToString(e) , CRTLogger.LEVEL_ERROR);
		}
	}
	
	public Long[] getListItemsFromRelationList(List<Relation> rels){
		if(rels==null) return null;
		Long[] ids = new Long[rels.size()];
		for(int i=0; i<rels.size(); i++){
			ids[i] = new Long(((Relation) rels.get(i)).getListItemId());
		}
		return ids;
	}
	
	/**
	 * init the loading of VPScriptRef objects
	 * @return
	 */
	public Map<String, VPScriptRef> initVpScriptRefs(){
		List<VPScriptRef> vprefs = DBClinReason.getVPScriptRefs();
		Map<String, VPScriptRef> refs = new HashMap<String, VPScriptRef>();
		if(vprefs==null) return null;
		for(int i=0; i<vprefs.size(); i++){
			//refs.put(vprefs.get(i).getVpId()+"_"+vprefs.get(i).getSystemId(), vprefs.get(i));
			refs.put(vprefs.get(i).getParentId(), vprefs.get(i));

		}
		return refs;
	}
	
	/**
	 * Returns the final diagnoses of the expert in a string (comma-seperated) or an empty string if nothing can be
	 * found. 
	 * @param vpId
	 * @return
	 */
	public String getExpFinalsAsString(String vpId){
		PatientIllnessScript expScript = AppBean.getExpertPatIllScript(vpId);
		if(expScript==null) return "";
		StringBuffer expFinal = new StringBuffer();
		int counter = 0;
		if(expScript!=null && expScript.getFinalDiagnoses()!=null){
			Iterator<RelationDiagnosis> it =  expScript.getFinalDiagnoses().iterator();
			while(it.hasNext()){					
				expFinal.append(it.next().getLabelOrSynLabel());
				if(it.hasNext()) expFinal.append(", ");
				counter++;
			}
		}
		if(counter==1) return IntlConfiguration.getValue("submit.expfinal") + expFinal.toString();
		if(counter>1) return IntlConfiguration.getValue("submit.expfinals") + expFinal.toString();
		return "";
	}
	
	public void updateOrderNrSubmitted(PatientIllnessScript patillscript){
		if(patillscript==null) return;
		if(patillscript.getOrderNrSubmitted()>0) return; 
		
	}
	
	/**
	 * We store the CASUS session id for all maps in the database
	 */
	public static void addSessionIdToMaps() {
		DBClinReason dbc = new DBClinReason();
		List maps = dbc.selectTmpPatientIllScripts();
		if(maps == null) return; 
		for (int i=0; i< maps.size(); i++) {
			PatientIllnessScript pis = (PatientIllnessScript) maps.get(i);
			if(pis.getExtUId()!=null) {
				String s = Encoder.getInstance().decodeQueryParam(pis.getExtUId());
				pis.setSessionId(Long.valueOf(s).longValue());
				dbc.saveAndCommit(pis);
			}
			
		}
	}
}
