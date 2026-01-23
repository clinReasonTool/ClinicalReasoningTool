package controller;

import java.util.*;

import actions.scoringActions.ScoringOverallAction;
import application.AppBean;
import beans.scripts.*;
import beans.relation.Relation;
import beans.scoring.*;
import database.DBClinReason;
import database.DBScoring;
import util.CRTLogger;

/**
 * We get all patIllScripts that have not yet been added to the peer table. We add these to the 
 * peers and set the sync flag to true.
 * @author ingahege
 *
 */
public class PeerSyncController {
	boolean sync1by1 = true;
	List<PatientIllnessScript> scripts;
	PeerContainer cont;
	int sync_max = 0;
	int sync_idx = 0;
	
	public PeerSyncController(){}
	public PeerSyncController(PeerContainer cont){
		this.cont = cont;
	}
	/**
	 * look for scripts and if there are new ones, add them to the peer table...
	 */
	public synchronized void sync(int max, Date startDate, Date endDate){
		long startms = System.currentTimeMillis();
		CRTLogger.out("Peer sync start: " + startms + "ms", CRTLogger.LEVEL_PROD);

		PeerContainer peerCont = AppBean.getPeers();
		//TODO only select scripts that are at the same stage as the expertscript?
		scripts = new DBClinReason().selectLearnerPatIllScriptsByPeerSync(max, startDate, endDate);
		if(scripts==null || scripts.isEmpty()){
			CRTLogger.out("PeerSyncController.sync - nothing to sync", CRTLogger.LEVEL_PROD);
		}
		sync_max = scripts.size();
		for(int i=0; i<sync_max; i++){
			sync_idx = i;
			PatientIllnessScript script = scripts.get(i);
			LearningAnalyticsBean lab = new LearningAnalyticsBean(script.getId(), script.getUserId(), script.getVpId());		
			syncItems(script.getBox1Relations()/*, peers*/, script.getVpId());
			syncItems(script.getBox2Relations()/*, peers*/, script.getVpId());
			syncItems(script.getBox3Relations()/*, peers*/, script.getVpId());
			syncItems(script.getBox4Relations()/*, peers*/, script.getVpId());
			if(lab!=null && lab.getScoreContainer()!=null){
				List<ScoreBean> scores = lab.getScoreContainer().getScores();
				syncSummSt(scores,script.getVpId());
				syncOverallScore(lab, script.getVpId());
				syncListActionScores(scores/*, peers*/, script.getVpId());
			}
			
			script.setPeerSync(true);
			if (sync1by1) {
				new DBClinReason().saveAndCommit(script); //save the changed sync status....
			}
			CRTLogger.out("Peer sync: " + script.getId() + " " + i + "/" + sync_max + " done", CRTLogger.LEVEL_PROD);
		}
		if (!sync1by1) {
			new DBClinReason().saveAndCommit(scripts); //save the changed sync status....
		}
		CRTLogger.out("Peer sync done: " + (System.currentTimeMillis() - startms) + "ms", CRTLogger.LEVEL_PROD);
	}
	
	/**
	 * We add up the scores of the list actions scores and nums in the PeerBeans...
	 * TODO: mark the bean on the last stage -> quicker access!
	 * @param scores
	 * @param peers
	 * @param parentId
	 */
	private void syncListActionScores(List<ScoreBean> scores, /*List<PeerBean> peers,*/ String vpId){
		if(scores==null) return;
		PeerContainer peerCont = AppBean.getPeers();
		if(peerCont==null) return;
		for(int i=0; i<scores.size(); i++){
			ScoreBean score = scores.get(i);
			if(score.isListScoreBean()){ //we only consider list scoring here!
				PeerBean peerBean = peerCont.getPeerBeanByVpIdActionAndStage(score.getType(), score.getStage(), vpId);
				if(peerBean==null){
					//	protected PeerBean createNewPeerBean(int action, String vpId, long itemId, float score, int stage, float expScore, float orgExpScore){

					peerBean = createNewPeerBean(score.getType(), vpId, -1, /*score.getOverallScore(),*/ score.getStage(), score.getScoreBasedOnExp(), score.getOrgScoreBasedOnExp());
					//peerCont.addPeerBean(peerBean, vpId);
				}
				else{
					peerBean.incrPeerNum();
					float orgScore = 0;
					if(score.getOrgScoreBasedOnExp() > 0) orgScore = score.getOrgScoreBasedOnExp();
					float scoreNot0 = 0;
					if(score.getScoreBasedOnExp()>0) scoreNot0 = score.getScoreBasedOnExp();
					
					peerBean.incrScoreSums(score.getOverallScore(), scoreNot0, orgScore);

					//peerBean.incrScoreSums(score.getOverallScore(), score.getScoreBasedOnExp(), score.getOrgScoreBasedOnExp());
					//peerBean.incrExpScoreSum(score.getScoreBasedOnExp());
					//new DBScoring().saveAndCommit(peerBean);
				}
				new DBScoring().saveAndCommit(peerBean);
			}
		}
	}
	

	/**
	 * add the summary Statement score to the peerBeans.
	 * @param sumScore
	 * @param parentId
	 */
	private void syncSummSt(List<ScoreBean> scores, String vpId){
		PeerContainer peerCont = AppBean.getPeers();
		if(peerCont==null || scores==null) return;
		ScoreBean sumScore = null;
		//get the score for the summary Statement:
		for(int i=0; i<scores.size(); i++){
			sumScore = scores.get(i);
			if(sumScore.getType()==ScoreBean.TYPE_SUMMST) break;
		}
		if(sumScore==null) return; //can happen?
		List<PeerBean> pbs = peerCont.getPeerBeansByActionAndVpId(vpId, ScoreBean.TYPE_SUMMST);
		PeerBean peerBean = null;
		if(pbs==null || pbs.isEmpty()){
			peerBean = new PeerBean(ScoreBean.TYPE_SUMMST, vpId, 0, -1);			
		}
		else{
			peerBean = pbs.get(0);
		
			peerBean.incrPeerNum();
			//make sure that we do not have negative values:
			float orgScore = 0;
			if(sumScore.getOrgScoreBasedOnExp() > 0) orgScore = sumScore.getOrgScoreBasedOnExp();
			float score = 0;
			if(sumScore.getScoreBasedOnExp()>0) score = sumScore.getScoreBasedOnExp();
			peerBean.incrScoreSums(sumScore.getOverallScore(), score, orgScore);
		}
		new DBScoring().saveAndCommit(peerBean);
	}
	
	private void syncOverallScore(LearningAnalyticsBean lab, String vpId){
		PeerContainer peerCont = AppBean.getPeers();
		if(peerCont==null || lab==null) return;
		ScoreBean overallScore = lab.getOverallScore();

		if(overallScore==null){ //then calculate it here:
			overallScore = new ScoringOverallAction().scoreAction(lab);
		}
		if(overallScore==null) return;
		List<PeerBean> pbs = peerCont.getPeerBeansByActionAndVpId(vpId, ScoreBean.TYPE_OVERALL_SCORE);
		PeerBean peerBean = null;
		if(pbs==null || pbs.isEmpty()){
			peerBean = new PeerBean(ScoreBean.TYPE_OVERALL_SCORE, vpId, 0, -1);	
			peerCont.addPeerBean(peerBean, vpId);
		}
		else{
			peerBean = pbs.get(0);
			peerBean.incrPeerNum();
			float orgScore = 0;
			if(overallScore.getOrgScoreBasedOnExp() > 0) orgScore = overallScore.getOrgScoreBasedOnExp();
			float score = 0;
			if(overallScore.getScoreBasedOnExp()>0) score = overallScore.getScoreBasedOnExp();
			
			peerBean.incrScoreSums(overallScore.getOverallScore(), score, orgScore);
		}
		new DBScoring().saveAndCommit(peerBean);
		//peerBean.incrScoreSum(overallScore.getScoreBasedOnExp());	
	}
		
	/**
	 * We go thru the list of items and increase the peerNum for each item
	 * @param rels
	 * @param peers
	 * @param listAction
	 */
	private void syncItems(List rels, /*List<PeerBean> peers,*/ String vpId){
		if(rels==null || rels.isEmpty()) return;
		PeerContainer peerCont = AppBean.getPeers();
		if(peerCont==null) return;
		for(int i=0; i<rels.size(); i++){
			Relation rel = (Relation) rels.get(i);
			PeerBean peerBean = null;
			if(rel!=null) peerBean = peerCont.getPeerBeanByActionVpIdAndItemId(rel.getRelationType(), vpId, rel.getListItemId());
			if(rel!=null){
				if(peerBean==null){
					//createNewPeerBean(int action, String vpId, long itemId, float score, int stage, float expScore, float orgExpScore){
	
					peerBean = createNewPeerBean(rel.getRelationType(), vpId,  rel.getListItemId(), /*0,*/ -1, 0, 0);				
				}
				else{
					peerBean.incrPeerNum();
					//new DBScoring().saveAndCommit(peerBean);
				}
			}
			new DBScoring().saveAndCommit(peerBean);
		}
	}
	
	/**
	 * This action for this script has not yet have been performed by any peer, so we create 
	 * a new PeerBean and store it in the peerContainer and in the database (peerNum = 1).
	 * @param action
	 * @param patIllScriptId
	 * @return
	 */
	protected PeerBean createNewPeerBean(int action, String vpId, long itemId, /*float score,*/ int stage, float expScore, float orgExpScore){

		PeerBean pb = new PeerBean(action, vpId, 1, /*score,*/ stage, expScore, orgExpScore);
		pb.setItemId(itemId);		
		//new DBScoring().saveAndCommit(pb);
		cont.addPeerBean(pb, vpId);
		return pb;
	}
	
	public boolean isSync1by1() {
		return sync1by1;
	}
	
	public void setSync1by1(boolean sync1by1) {
		this.sync1by1 = sync1by1;
	}
	
	public int getSync_max() {
		return sync_max;
	}
	
	public void setSync_max(int sync_max) {
		this.sync_max = sync_max;
	}
	
	public int getSync_idx() {
		return sync_idx;
	}
	
	public void setSync_idx(int sync_idx) {
		this.sync_idx = sync_idx;
	}
	
	
	/*private PeerBean createNewPeerBean(int action, String vpId, long itemId, float score, float expScore, float orgExpScore){
		return createNewPeerBean(action, vpId, itemId, score, -1, expScore, orgExpScore);
	}*/
	
	
}
