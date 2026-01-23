package actions.beanActions;

import java.util.*;

import actions.scoringActions.ScoringListAction;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.*;
import beans.scoring.ScoreBean;
import controller.NavigationController;
import controller.XAPIController;
import database.DBClinReason;
import util.CRTLogger;

/**
 * @author ingahege
 * @deprecated
 */
public class DelProblemAction /*implements DelAction*/{
	private PatientIllnessScript patIllScript;
	
	public DelProblemAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object rel) {
		new DBClinReason().deleteAndCommit(rel);
		//new DBClinReason().saveAndCommit(patIllScript.getProblems()); //orderNrs have changed, so we have to save all
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Object o) {
		LogEntry le = new LogEntry(LogEntry.DELPROBLEM_ACTION, patIllScript.getId(), ((Relation)o).getListItemId());
		le.save();		
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#delete(java.lang.String)
	 */
	public void delete(String id) {
	/*	if(id==null || id.trim().equals("") || patIllScript==null || patIllScript.getProblems()==null || patIllScript.getProblems().isEmpty()){
			//todo error msg
			return;		
		}
		RelationProblem rel = patIllScript.getProblemById(Long.parseLong(id));
		patIllScript.getProblems().remove(rel);
		new ActionHelper().reOrderItems(patIllScript.getProblems());		
		notifyLog(rel);
		updateGraph(rel);
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		if(!patIllScript.isExpScript()) XAPIController.getInstance().removeXAPIAddActionStatement(rel);
		save(rel);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_PROBLEM_LIST, Relation.TYPE_PROBLEM);*/

	}
	
	/*public void updateGraph(Relation rel){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_PROBLEM);
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);
		//remove complete edge param for all these edges:
		if( patIllScript.getProblems()!=null){
			for(int i=0; i < patIllScript.getProblems().size(); i++){
				graph.removeEdgeWeight(rel.getListItemId(), patIllScript.getProblems().get(i).getListItemId());
			}
		}
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}*/
}
