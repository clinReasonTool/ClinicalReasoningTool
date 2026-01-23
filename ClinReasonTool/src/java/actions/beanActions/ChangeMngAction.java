package actions.beanActions;

import java.beans.Beans;

import beans.LogEntry;
import beans.scripts.*;
import beans.graph.MultiVertex;
import beans.relation.Relation;
import beans.relation.RelationManagement;
import database.DBClinReason;


/**
 * A ProblemRelation is changed and a different Problem object attached, id remains the same, so, no
 * other changes necessary.
 * @author ingahege
 * @deprecated
 *
 */
public class ChangeMngAction /*extends ChgAction*/{

	private PatientIllnessScript patIllScript;
	
	public ChangeMngAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	public void changeMng(String oldMngIdStr, String changeModeStr){
		/*long oldProbId = Long.valueOf(oldProbIdStr.trim());
		long newProbId = Long.valueOf(newProbIdStr.trim());
		changeMng(oldProbId, newProbId);*/
		long oldMngId = Long.valueOf(oldMngIdStr.trim());
		int changeMode = Integer.valueOf(changeModeStr.trim());
		//if(changeMode==1) toggleProblem(oldMngId); //change prefix
		//if(changeMode==2 || changeMode==3) 
		//	changeItem(oldMngId, patIllScript, Relation.TYPE_MNG); //changeProblem(oldProbId); //synonyma or hierarchy item

	}
	
	/**
	 * called from lists, where we only have the the current id, we change it to the new id, which is stored in the
	 * scoreBean.
	 * @param oldRelStr
	 */
	/*public void changeMng(String oldRelStr){
		if(oldRelStr==null || oldRelStr.equals("")) return;
		long oldRelId = Long.valueOf(oldRelStr).longValue();
		RelationManagement relToChg = patIllScript.getMngById(oldRelId);
		ScoreBean score = new ScoringController().getScoreBeanForItem(Relation.TYPE_MNG, relToChg.getListItemId());
		//change in RelationProblem & Vertex:
		
		Graph g = new NavigationController().getCRTFacesContext().getGraph();
		MultiVertex expVertex = g.getVertexById(score.getExpItemId());
		MultiVertex learnerVertexOld = g.getVertexById(relToChg.getListItemId());
		if(!expVertex.equals(learnerVertexOld)){ //then it is NOT a synonyma, but a hierarchy node
				new GraphController(g).transferEdges(learnerVertexOld, expVertex);		
				g.removeVertex(learnerVertexOld);
				if(expVertex.getLearnerVertex()==null) expVertex.setLearnerVertex(relToChg);
		}
		changeRelation(expVertex, relToChg);	
		//we re-score the item:
		new ScoringAddAction(true).scoreAction(expVertex.getVertexId(), patIllScript, false);
	}*/
	
	public void changeRelation(MultiVertex expVertex, Relation rel){
		RelationManagement relToChg = (RelationManagement) rel;
		notifyLog(relToChg, expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setManagement(expVertex.getExpertVertex().getListItem());
		relToChg.setListItemId(expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setSynId(-1);	
		save(relToChg);		
	}
	
	/*private void changeMng(long newMngId, long mngRel){
		RelationManagement mngToChg = patIllScript.getMngById(mngRel);
		ListItem oldMng = new DBList().selectListItemById(mngToChg.getListItemId());
		ListItem newMng = new DBList().selectListItemById(newMngId);
		if(mngToChg!=null && newMng!=null && oldMng!=null){
			notifyLog(mngToChg, newMngId);
			mngToChg.setManagement(newMng);
			mngToChg.setListItemId(newMng.getItem_id());
			save(mngToChg);		
		}
		//else -> error...
	}*/
	
	public void notifyLog(Beans mngToChg, long newMngId){
		LogEntry le = new LogEntry(LogEntry.CHGMNG_ACTION, patIllScript.getId(), ((Relation)mngToChg).getListItemId(), newMngId);
		le.save();
	}
	
	public void save(Beans rel){
		new DBClinReason().saveAndCommit(rel);
	}

	/*
	public void triggerScoringAction(Beans beanToScore, boolean isJoker) {
		// TODO Auto-generated method stub
		
	}*/
}
