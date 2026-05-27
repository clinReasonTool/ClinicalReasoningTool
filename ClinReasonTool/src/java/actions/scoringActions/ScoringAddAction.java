package actions.scoringActions;

import java.util.*;

import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.Relation;
import beans.scoring.ScoreBean;
import beans.scoring.ScoreContainer;
import controller.NavigationController;
import controller.ScoringController;
import database.DBScoring;
import beans.list.*;
/**
 * We score any action in which the learner adds an item (problem, ddx, text, mng item) to the list/concept map.
 * @author ingahege
 *
 */
public class ScoringAddAction implements ScoringAction{

	private boolean isChg = false; //true if scoring is repeated after user has changed an item
	
	public ScoringAddAction(){}
	public ScoringAddAction(boolean isChg){
		this.isChg = isChg;
	}

	/**
	 * We try to compare the users entry with the experts list of problems and score based on whether the 
	 * problem is in the list, the learner has used the correct term, etc. 
	 * We also have to take into account whether the learner has used any feedback before this action 
	 * @param patIllScript
	 * @param rel
	 */
	public void scoreAction(long vertexId, PatientIllnessScript patIllScript, boolean isJoker, int boxType){
		if(patIllScript.isExpScript()) return;
		Graph g = new NavigationController().getCRTFacesContext().getGraph();
		MultiVertex mvertex = g.getVertexByIdAndType(vertexId, boxType);
		ScoreContainer scoreContainer = new NavigationController().getCRTFacesContext().getScoreContainer();
		
		ScoreBean scoreBean = scoreContainer.getScoreBeanByTypeAndItemId(mvertex.getType(), vertexId);
		//if(scoreBean!=null && !isChg) return; //then this item has already been scored and we do not want a rescore
		if(scoreBean!=null) isChg = true;
		else scoreBean = new ScoreBean(patIllScript, mvertex.getVertexId(), mvertex.getType());
		if(g.getExpertPatIllScriptId()>0) //otherwise we do not have an experts' patIllScript to compare with				
			calculateAddActionScoreBasedOnExpert(mvertex, scoreBean, patIllScript, g, isJoker);				
					
		if(g.getPeerNums()>ScoringController.MIN_PEERS) //we have enough peers, so we can score based on this as well:
			calculateAddActionScoreBasedOnPeers(mvertex, scoreBean, g.getPeerNums());
		
		scoreContainer.addScore(scoreBean);
		//calculateOverallScore(scoreBean); 
		new DBScoring().saveAndCommit(scoreBean);			
	}
	
	/** TODO we could consider all components and calculate based on these an overall score.
	 * For now we just take the expertsScore.
	 * @param scoreBean
	 */
/*	private void calculateOverallScore(ScoreBean scoreBean){
		scoreBean.setOverallScore(scoreBean.getScoreBasedOnExp());
	}*/
	
	/**
	 * User has added an item to the list, we score it based on the experts list
	 * 1 = exact same problem is in experts' list (dark green check)
	 * 0.x = synonyma is in the experts' list (light green check -> learner can change to better term) -> see weight in synonym object
	 * 0.5 = part of a syndrome
	 * 0 = problem is not at all in the experts' list (no check) 
	 * TODO consider position? probably not possible on add, but on move! 
	 */	
	private void calculateAddActionScoreBasedOnExpert(MultiVertex mvertex, ScoreBean scoreBean, PatientIllnessScript patIllScript, Graph g, boolean isJoker){		
		Relation expRel = mvertex.getExpertVertex();
		Relation learnerRel = mvertex.getLearnerVertex();
		scoreBean.setScoreBasedOnExp(ScoringController.SCORE_NOEXP_BUT_LEARNER, isChg);
		if(learnerRel!=null && expRel!=null) scoreBean.setTiming(learnerRel.getStage(), expRel.getStage());
				
		if(expRel!=null){ //expert has chosen this item (not synonym)
			if(learnerRel!=null && learnerRel.getSynId()<=0){
				if(learnerRel.hasPrefix()==expRel.hasPrefix()){				
					if(expRel.getIsSyndrome()==Relation.IS_SYNDROME_PART){ //learner has chosen a part of a syndrome:
						scoreBean.setScoreBasedOnExp(ScoringController.HALF_SCORE, isChg);
						scoreBean.setDetails("part of syndrome");
					}
					else scoreBean.setScoreBasedOnExp(ScoringController.SCORE_EXP_SAMEAS_LEARNER, isChg);
				}
				else scoreBean.setScoreBasedOnExp(ScoringController.NO_SCORE, isChg);
				//return;
			}
			if(learnerRel!=null && learnerRel.getSynId()>0){//learner has chosen a synonym:
				Synonym learnerSyn = learnerRel.getSynonym();
				if(learnerRel.hasPrefix()==expRel.hasPrefix()){
					scoreBean.setScoreBasedOnExp(learnerSyn.getRatingWeight(), isChg);
				}
				else scoreBean.setScoreBasedOnExp(ScoringController.NO_SCORE, isChg);
				scoreBean.setExpItemId(expRel.getListItemId());
				//return;
			}
		}
		
		else{ //expert has not picked this item (nor a synonym), we check whether a more specific/general item has been chosen.
			scoreHierarchyBasedOnExp(g, scoreBean, mvertex, learnerRel);						
		}
		
		ScoringController.getInstance().setFeedbackInfo(scoreBean, isChg, isJoker);


	}

	/**
	 * We check whether the learner has entered something that is part of a syndrome entered by the expert, 
	 * e.g. if the learner has entered "hypotension" and the expert "shock", the learner gets credit for 
	 * "hypotension" as well. Calculation is (1/possible number of parts) - 0.05
	 * @param g
	 * @param scoreBean
	 * @param mvertex
	 * @param learnerRel
	 * @return
	 */
	/*private boolean checkForSyndromes(Graph g, ScoreBean scoreBean, MultiVertex mvertex, Relation learnerRel){
		List<MultiVertex> expSyndromes = g.getVerticesSyndromeExpOnly(Relation.IS_SYNDROME_PART);
		if(expSyndromes==null || expSyndromes.isEmpty()) return false; //no syndromes added by expert
		Iterator<MultiVertex> it = expSyndromes.iterator();
		while(it.hasNext()){ ////go through parts of the syndrome
			MultiVertex mv = it.next();
			
			
		}
	}*/
	/**
	 * Check and score if the learner has chosen a child or parent item of the expert's choice.
	 * if it is is child item (more specific) we do not reduce the score. If it is a parent item we reduce the score 
	 * depending on the distance to the expert's choice.
	 * We cannot score a parent choice, if the expert has chosen more than one child item (e.g. learner has chosen 
	 * "Respiratory disease", and expert has entered "Asthma" and "COPD".)
	 * @param g
	 * @param scoreBean
	 * @param mvertex
	 * @param learnerRel
	 */
	private void scoreHierarchyBasedOnExp(Graph g, ScoreBean scoreBean, MultiVertex mvertex,Relation learnerRel){
		MultiVertex expVertex = g.getExpParentVertex(mvertex); //check whether user has picked a more specific item than expert
		if(expVertex!=null){ //user was more specific than expert, we score 100%
			int distance = g.getHierarchyDistance(mvertex, expVertex); //in this case distance is not considered for scoring
			scoreBean.setScoreBasedOnExp(ScoringController.SCORE_LEARNER_MORE_SPECIFIC, isChg);
			scoreBean.setTiming(learnerRel.getStage(), expVertex.getExpertVertex().getStage());
			scoreBean.setExpItemId(expVertex.getVertexId());
			scoreBean.setDistance(distance);
			return;
		}
		//check whether learner has picked something more general than expert -> can be more than one item!
		List<MultiVertex>expVertices = g.getExpChildVertices(mvertex); 
		if(expVertices!=null && !expVertices.isEmpty() && expVertices.size()==1){ //user picked something more general
			int distance = g.getDistance(mvertex, expVertices.get(0)); //how far away is the learners choice from the experts?
			float score = (float) ScoringController.SCORE_EXP_SAMEAS_LEARNER/(distance+1);
			scoreBean.setScoreBasedOnExp(score, isChg);
			scoreBean.setExpItemId(expVertices.get(0).getVertexId());
			scoreBean.setDistance(-distance);
		}
		//we have more than one child of the expert:
		else if(expVertices!=null && !expVertices.isEmpty() && expVertices.size()>1){
			scoreBean.setScoreBasedOnExp(ScoringController.SCORE_LEARNER_MORE_GENERAL_MULT_EXP, isChg);
			scoreBean.setExpItemId(ScoringController.MULTIPLE_EXPERT_CHLDS);
		}
	}
	
	private void calculateAddActionScoreBasedOnPeers(MultiVertex mvertex, ScoreBean scoreBean, int overallPeers){
		Relation learnerRel = mvertex.getLearnerVertex();
		float peerRatio =  mvertex.getPeerNums()/overallPeers; 
		//we need a good algorithm here to score this based on current and overall peer nums
		//CAVE: do we have to consider the stage? 
		
	}	
}
