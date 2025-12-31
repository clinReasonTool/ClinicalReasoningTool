package actions.beanActions;

import java.beans.Beans;

import beans.LogEntry;
import beans.scripts.*;
import beans.graph.MultiVertex;
import beans.relation.*;
import database.DBClinReason;


/**
 * A ProblemRelation is changed and a different Problem object attached, id remains the same, so, no
 * other changes necessary.
 * @author ingahege
 * @deprecated
 */
public class ChangeTestAction /* extends ChgAction*/{

	private PatientIllnessScript patIllScript;
	
	public ChangeTestAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	public void changeTest(String oldTestIdStr, String changeModeStr){
		/*long oldTestId = Long.valueOf(oldTestIdStr.trim());
		long newTestId = Long.valueOf(newTestIdStr.trim());
		changeTest(oldTestId, newTestId);*/
		long oldTestId = Long.valueOf(oldTestIdStr.trim());
		int changeMode = Integer.valueOf(changeModeStr.trim());
		//if(changeMode==1) toggleTest(oldTestId); //change prefix
	//	if(changeMode==2 || changeMode==3) changeItem(oldTestId, patIllScript, Relation.TYPE_TEST); //synonyma or hierarchy item
	}
	
	/**
	 * called from lists, where we only have the the current id, we change it to the new id, which is stored in the
	 * scoreBean.
	 * @param oldRelStr
	 */
	/*public void changeTest(String oldRelStr){
		if(oldRelStr==null || oldRelStr.equals("")) return;
		long oldRelId = Long.valueOf(oldRelStr).longValue();
		RelationTest relToChg = patIllScript.getTestById(oldRelId);
		ScoreBean score = new ScoringController().getScoreBeanForItem(Relation.TYPE_TEST, relToChg.getListItemId());
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
		RelationTest relToChg = (RelationTest) rel;
		notifyLog(relToChg, expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setTest(expVertex.getExpertVertex().getListItem());
		relToChg.setListItemId(expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setSynId(-1);	
		save(relToChg);		
	}
	
	/*private void changeTest(long newProbId, long probRel){
		RelationTest testToChg = patIllScript.getTestById(probRel);
		ListItem oldTest = new DBList().selectListItemById(testToChg.getListItemId());
		ListItem newTest = new DBList().selectListItemById(newProbId);
		if(testToChg!=null && newTest!=null && oldTest!=null){
			notifyLog(testToChg, newProbId);
			testToChg.setTest(newTest);
			testToChg.setListItemId(newTest.getItem_id());
			save(testToChg);		
		}
		//else -> error...
	}*/
	
	public void notifyLog(Beans testToChg, long newTestId){
		LogEntry le = new LogEntry(LogEntry.CHGTEST_ACTION, patIllScript.getId(), ((Relation)testToChg).getListItemId(), newTestId);
		le.save();
	}
	
	public void save(Beans rel){
		new DBClinReason().saveAndCommit(rel);
	}

	/*@Override
	public void triggerScoringAction(Beans beanToScore, boolean isJoker) {
		// TODO Auto-generated method stub
		
	}*/
}
