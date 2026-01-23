package actions.beanActions;

import java.beans.Beans;

import beans.LogEntry;
import beans.scripts.*;
import beans.graph.MultiVertex;
import beans.relation.*;

import database.DBClinReason;


/**
 * A DiagnosisRelation is changed and a different Diagnosis object attached, id remains the same, so, no
 * other changes necessary.
 * @author ingahege
 *
 */
public class ChangeDiagnosisAction /*extends ChgAction*/ {

	private PatientIllnessScript patIllScript;
	
	public ChangeDiagnosisAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	public void changeDiagnosis(String oldDDXIdStr, String changeModeStr){
		long oldDDXId = Long.valueOf(oldDDXIdStr.trim());
		int changeMode = Integer.valueOf(changeModeStr.trim());
		//if(changeMode==1) toggleDiagnosis(oldDDXId); //change prefix
		//if(changeMode==2 || changeMode==3) 
		//	changeItem(oldDDXId, patIllScript, Relation.TYPE_DDX);
		/*long oldDDXId = Long.valueOf(oldDDXIdStr.trim());
		long newDDXId = Long.valueOf(newDDXIdStr.trim());
		changeDiagnosis(oldDDXId, newDDXId);*/
	}
	
	/**
	 * called from lists, where we only have the the current id, we change it to the new id, which is stored in the
	 * scoreBean.
	 * @param oldProbIdStr
	 */
	/*public void changeDiagnosis(String oldDDXIdStr){
		if(oldDDXIdStr==null || oldDDXIdStr.equals("")) return;
		long oldDDXId = Long.valueOf(oldDDXIdStr).longValue();
		RelationDiagnosis ddxToChg = patIllScript.getDiagnosisById(oldDDXId);
		ScoreBean score = new ScoringController().getScoreBeanForItem(Relation.TYPE_DDX, ddxToChg.getListItemId());
		//change in RelationProblem & Vertex:
		
		Graph g = new NavigationController().getCRTFacesContext().getGraph();
		MultiVertex expVertex = g.getVertexById(score.getExpItemId());
		MultiVertex learnerVertexOld = g.getVertexById(ddxToChg.getListItemId());
		if(!expVertex.equals(learnerVertexOld)){ //then it is NOT a synonyma, but a hierarchy node
				new GraphController(g).transferEdges(learnerVertexOld, expVertex);		
				g.removeVertex(learnerVertexOld);
				if(expVertex.getLearnerVertex()==null) expVertex.setLearnerVertex(ddxToChg);
		}
		changeRelation(expVertex, ddxToChg);	
		//we re-score the item:
		new ScoringAddAction(true).scoreAction(expVertex.getVertexId(), patIllScript, false);
	}*/
	
	public void changeRelation(MultiVertex expVertex, Relation rel){
		RelationDiagnosis relToChg = (RelationDiagnosis) rel;
		notifyLog(relToChg, expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setDiagnosis(expVertex.getExpertVertex().getListItem());
		relToChg.setListItemId(expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setSynId(-1);	
		save(relToChg);		
	}
	
	/*private void changeDiagnosis(long newDDXId, long ddxRel){
		RelationDiagnosis ddxToChg = patIllScript.getDiagnosisById(ddxRel);
		ListItem oldDDX = new DBList().selectListItemById(ddxToChg.getListItemId());
		ListItem newDDX = new DBList().selectListItemById(newDDXId);
		if(ddxToChg!=null && newDDX!=null && oldDDX!=null){
			notifyLog(ddxToChg, newDDXId);
			ddxToChg.setDiagnosis(newDDX);
			ddxToChg.setListItemId(newDDX.getItem_id());
			save(ddxToChg);		
		}
		//else -> error...
	}*/
	
	public void notifyLog(Beans ddxToChg, long newDDXId){
		LogEntry le = new LogEntry(LogEntry.CHGDDX_ACTION, patIllScript.getId(), ((Relation) ddxToChg).getListItemId(), newDDXId);
		le.save();
	}

	private void notifyMnMLog(Relation ddxToChg, int newMnM){
		LogEntry le = new LogEntry(LogEntry.CHGDDXMNM_ACTION, patIllScript.getId(), ddxToChg.getListItemId(), newMnM);
		le.save();
	}
	
	public void save(Beans rel){
		new DBClinReason().saveAndCommit(rel);
	}
	
	/**
	 * We toogle the Must-Not_miss flag and change the color of the rectangle in the concept map
	 * @param idStr
	 * @param newVal "0"|"1"
	 */
	public void toggleMnM(String idStr){
		long id = Long.valueOf(idStr.trim());
		RelationDiagnosis ddxToChg = patIllScript.getDiagnosisById(id);
		if(ddxToChg.getMnm()==0) ddxToChg.setMnm(1);
		else ddxToChg.setMnm(0);
		//if(ddxToChg.isMnM()) ddxToChg.setColor(RelationDiagnosis.COLOR_RED);
		//else  ddxToChg.setColor(RelationDiagnosis.COLOR_DEFAULT);
		save(ddxToChg);
		notifyMnMLog(ddxToChg, ddxToChg.getMnm());
	}
/*
	@Override
	public void triggerScoringAction(Beans beanToScore, boolean isJoker) {
		// TODO Auto-generated method stub
		
	}*/
}
