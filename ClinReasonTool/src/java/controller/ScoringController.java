package controller;

import java.util.*;

import application.AppBean;
import beans.CRTFacesContext;
import beans.scripts.*;
import beans.scoring.FeedbackBean;
import beans.scoring.PeerBean;
import beans.scoring.ScoreBean;
import beans.scoring.ScoreContainer;

/**
 * Calculates the scores of actions and items based on the Graph
 * we can uses this for helper actions???
 * @author ingahege
 *
 */
public class ScoringController {
	
	public static final float FULL_SCORE = (float) 1;
	public static final float HALF_SCORE = (float) 0.5; //e.g. a synonyma entered....
	public static final float NO_SCORE = (float) 0; //might be 0.25 if we want to give the learner credit for doing something...
	public static final float RED_SCORE_LATESTAGE = (float) 0;
	public static final float ADD_LISTITEM_RED_SCORE = (float) 0.05; //we deduct 5% of the overall list score for each additional item.
	public static final float ADD_CNX_RED_SCORE = (float) 0.01; //we deduct 1% of the overall cnxs score for each additional cnx.
	
	public static final float SCORE_EXP_SAMEAS_LEARNER = FULL_SCORE;
	public static final float SCORE_LEARNER_MORE_SPECIFIC = FULL_SCORE;
	public static final float SCORE_LEARNER_MORE_GENERAL_MULT_EXP = HALF_SCORE; //learner was more general, multiple childs by expert
	public static final float SCORE_NOEXP_BUT_LEARNER = NO_SCORE; //we score with 0 points, BUT the action itself will be honored in the LA part
	public static final float MIN_PEERS = 20;
	public static final int MULTIPLE_EXPERT_CHLDS = -2;
	public static final String ICON_PREFIX = "icon-ok";
	//define possible scoring algorithms:
	public static final int SCORING_ALGORITHM_BASIC = 1;
	/**
	 * if learner has less than this percentage for final ddx submission, we allow a re-submit
	 */
	public static final float scoreForAllowReSubmit = (float) 0.5; //of learner gets less than 50% correct on final diagnoses, we allow re-submit
	
	static private ScoringController instance = new ScoringController();
	static public ScoringController getInstance() { return instance; }

	
	public ScoringController(){}
	
	public String getIconForScore(int type, long itemId){
		ScoreContainer scoreContainer = new NavigationController().getCRTFacesContext().getScoreContainer();
		if (scoreContainer==null) return ""; //no icon
		ScoreBean scoreBean = scoreContainer.getScoreBeanByTypeAndItemId(type,itemId);
		if(scoreBean==null || scoreBean.getOverallScore()<=0) return "";
		if(scoreBean.getOverallScore()==1) return ICON_PREFIX+1;
		if(scoreBean.getOverallScore()<1) return ICON_PREFIX+2;
		return "";
	}

	/**
	 * @return 0 (incorrect), 1 (correct), or 2 (partly correct)
	 */
	public int getExpScore(int type, long itemId){
		ScoreContainer scoreContainer = NavigationController.getInstance().getMyFacesContext().getScoreContainer();
		if (scoreContainer==null) return 0; //no icon
		ScoreBean scoreBean = scoreContainer.getScoreBeanByTypeAndItemId(type,itemId);
		if(scoreBean==null || scoreBean.getScoreBasedOnExp()<=0) return 0;
		if(scoreBean.getScoreBasedOnExp()==1) return 1;
		if(scoreBean.getScoreBasedOnExp()<1) return 2;
		return 0;
	}
	
	public String getIconForFinalDDXScore(long itemId){
		ScoreContainer scoreContainer = new NavigationController().getCRTFacesContext().getScoreContainer();
		if (scoreContainer==null) return ""; //no icon
		ScoreBean scoreBean = scoreContainer.getScoreBeanByTypeAndItemId(ScoreBean.TYPE_FINAL_DDX,itemId);
		if(scoreBean==null || scoreBean.getOverallScore()<=0) return "";
		if(scoreBean.getOverallScore()==1) return ICON_PREFIX+1;
		if(scoreBean.getOverallScore()<1) return ICON_PREFIX+2;
		return "";
	}
	
	public ScoreBean getOverallFinalDDXScore(){
		ScoreContainer scoreContainer = new NavigationController().getCRTFacesContext().getScoreContainer();
		if (scoreContainer==null) return null;//-1;
		ScoreBean bean = scoreContainer.getScoreByType(ScoreBean.TYPE_FINAL_DDX_LIST);
		if(bean==null) return null;//-1;
		return bean; //.getScoreBasedOnExp();
		/*if(beans.size()==1) return beans.get(0).getScoreBasedOnExp();
		int scoreSum = 0;
		for(int i=0; i<beans.size(); i++){
			scoreSum += beans.get(i).getScoreBasedOnExpPerc();
		}
		return (float) scoreSum/beans.size();*/
	}
	public ScoreBean getScoreBeanForItem(int type, long itemId){
		ScoreContainer scoreContainer = new NavigationController().getCRTFacesContext().getScoreContainer();
		if (scoreContainer==null) return null;
		return scoreContainer.getScoreBeanByTypeAndItemId(type,itemId);
	}
	
	public List<ScoreBean> getScoreBeansFortype(int type){
		ScoreContainer scoreContainer = new NavigationController().getCRTFacesContext().getScoreContainer();
		if (scoreContainer==null) return null;
		return scoreContainer.getScoresByType(type);
	}
	
	//public void setFeedbackInfo(ScoreBean scoreBean){ setFeedbackInfo(scoreBean, false);}
	
	/**
	 * We store in the ScoreBean whether at this time the learner has already seen the expert's/peer feedback for
	 * the item he has just added.
	 * @param scoreBean
	 */
	public void setFeedbackInfo(ScoreBean scoreBean, boolean isChg, boolean isJoker){
		CRTFacesContext crtContext = new NavigationController().getCRTFacesContext();
		boolean expFBOn = crtContext.getFeedbackContainer().isExpFeedbackOn(scoreBean.getType(), scoreBean.getStage());
		if(expFBOn) scoreBean.setFeedbackOn(FeedbackBean.FEEDBACK_EXP);
		if(isChg) scoreBean.setFeedbackOn(FeedbackBean.FEEDBACK_CHG);
		if(isJoker) scoreBean.setFeedbackOn(FeedbackBean.FEEDBACK_JOKER);
		
		if(isJoker || expFBOn) scoreBean.setOrgScoreBasedOnExp(ScoringController.NO_SCORE);

	}
	
	/**
	 * Before learner goes to the next stage (card) we score the lists he has created so far...
	 * @param patIllScript
	 */
	/*public void scoringListsForStage(PatientIllnessScript patIllScript, int stage){
		if(stage<=0 || patIllScript.isExpScript()) return;
		ScoringListAction scla = new ScoringListAction(patIllScript);
		scla.scoreList(ScoreBean.TYPE_DDX_LIST, ScoreBean.TYPE_ADD_DDX);
		scla.scoreList(ScoreBean.TYPE_PROBLEM_LIST, ScoreBean.TYPE_ADD_PROBLEM);
		scla.scoreList(ScoreBean.TYPE_TEST_LIST, ScoreBean.TYPE_ADD_TEST);
		scla.scoreList(ScoreBean.TYPE_MNG_LIST, ScoreBean.TYPE_ADD_MNG);
		//score all connections
		new ScoringCnxsAction(patIllScript).scoreConnections(stage);
		//score summaryStatement at this stage: 
		new ScoringSummStAction().scoreAction(patIllScript, stage);
	}*/
	
	/**
	 * Percentage of peers who have added the given item
	 * @param actionType
	 * @param patIllScriptId
	 * @param itemId
	 * @return
	 */
	public String getPeerPercentageForAction(int actionType, long patIllScriptId, long itemId){
		PatientIllnessScript patIllScript = NavigationController.getInstance().getMyFacesContext().getPatillscript();
		if(patIllScript==null || patIllScript.isExpScript()) return "";
		String vpId = patIllScript.getVpId();
		//String vpId = crtContext.getPatillscript().getVpId();
		PeerBean peer = AppBean.getPeers().getPeerBeanByActionVpIdAndItemId(actionType, vpId, itemId);
		PeerBean overallNum = AppBean.getPeers().getPeerBeanByIllScriptCreationActionAndVpId(vpId);
		if(peer==null || overallNum==null) return "0%";
		float percentage =  peer.getPeerPercentage(overallNum.getPeerNum());
		if(percentage<=0) return "";
		return (int)((percentage * 100)) + "%";
	}
}
