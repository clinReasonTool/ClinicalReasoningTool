package actions.beanActions;

import java.beans.Beans;

import actions.scoringActions.Scoreable;
import actions.scoringActions.ScoringAddAction;
import beans.scripts.*;
import beans.LogEntry;
import beans.graph.Box;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.Relation;
import beans.relation.RelationDiagnosis;
import beans.relation.RelationProblem;
import beans.scoring.ScoreBean;
import controller.GraphController;
import controller.NavigationController;
import controller.ScoringController;
import database.DBClinReason;

public class ChangeRelationAction implements Scoreable{
	/**
	 * A log entry for the change action is created and saved in a Log object
	 */
	
	private PatientIllnessScript patIllScript;
	private Box box;
	
	public ChangeRelationAction(PatientIllnessScript patIllScript, Box box){
		this.patIllScript = patIllScript;
	}
	
	//abstract void notifyLog(Beans rel, long newId);
	
	//abstract void save(Beans rel);
	//abstract void changeRelation(MultiVertex v, Relation rel);
	/**
	 * called from lists, where we only have the the current id, we change it to the new id, which is stored in the
	 * scoreBean.
	 * @param oldProbIdStr
	 */
	public void changeRelation(String relIdStr){
		long relId = Long.valueOf(relIdStr.trim());
		Relation relToChg = patIllScript.getRelationByIdAndType(relId, box.getBoxType());
		
		ScoreBean score = new ScoringController().getScoreBeanForItem(box.getBoxType(), relToChg.getListItemId());
		//change in RelationProblem & Vertex:
		
		Graph g = NavigationController.getInstance().getCRTFacesContext().getGraph();
		MultiVertex expVertex = g.getVertexByIdAndType(score.getExpItemId(), box.getBoxType());
		MultiVertex learnerVertexOld = g.getVertexByIdAndType(relToChg.getListItemId(), box.getBoxType());
		if(!expVertex.equals(learnerVertexOld)){ //then it is NOT a synonyma, but a hierarchy node
				new GraphController(g).transferEdges(learnerVertexOld, expVertex);		
				g.removeVertex(learnerVertexOld);
				if(expVertex.getLearnerVertex()==null) expVertex.setLearnerVertex(relToChg);
		}
		changeRelation(expVertex, relToChg);	
		//we re-score the item:
		new ScoringAddAction(true).scoreAction(expVertex.getVertexId(), patIllScript, false, box.getBoxType());
	}
	
	private void changeRelation(MultiVertex expVertex, Relation relToChg){
		//Relation prob = (Relation) probToChg;
		notifyLog(relToChg, expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setListItem(expVertex.getExpertVertex().getListItem());
		relToChg.setListItemId(expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setSynId(-1);	
		
		save(relToChg);		
	}
	
	/**
	 * We toogle the Must-Not_miss flag and change the color of the rectangle in the concept map
	 * @param idStr
	 * @param newVal "0"|"1"
	 */
	public void toggleMnM(String idStr/*, String newVal*/){
		long id = Long.valueOf(idStr.trim());
		//int mnm = Integer.valueOf(newVal.trim());
		RelationDiagnosis ddxToChg = (RelationDiagnosis) patIllScript.getRelationByIdAndType(id, Box.BOXTYPE_DDX);
		if(ddxToChg.getMnm()==0) ddxToChg.setMnm(1);
		else ddxToChg.setMnm(0);
		//if(ddxToChg.isMnM()) ddxToChg.setColor(RelationDiagnosis.COLOR_RED);
		//else  ddxToChg.setColor(RelationDiagnosis.COLOR_DEFAULT);
		save(ddxToChg);
		notifyMnMLog(ddxToChg, ddxToChg.getMnm());
	}
	
	public void notifyLog(Beans probToChg, long newProbId){
		LogEntry le = new LogEntry(LogEntry.CHGPROBLEM_ACTION, patIllScript.getId(), ((Relation)probToChg).getListItemId(), newProbId);
		le.save();
	}
	
	public void save(Beans rel){
		new DBClinReason().saveAndCommit(rel);
	}

	@Override
	public void triggerScoringAction(Beans beanToScore, boolean isJoker) {
		// TODO Auto-generated method stub
		
	}
	
	private void notifyMnMLog(Beans ddxToChg, int newMnM){
		LogEntry le = new LogEntry(LogEntry.CHGDDXMNM_ACTION, patIllScript.getId(), ((Relation) ddxToChg).getListItemId(), newMnM);
		le.save();
	}

}
