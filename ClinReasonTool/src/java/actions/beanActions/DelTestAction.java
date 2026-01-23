package actions.beanActions;

import java.util.*;

import actions.scoringActions.ScoringListAction;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.*;
import beans.scoring.ScoreBean;
import beans.scripts.IllnessScriptInterface;
import controller.NavigationController;
import database.DBClinReason;
import util.CRTLogger;

/**
 * @author ingahege
 * @deprecated
 */
public class DelTestAction /*implements DelAction*/{
	private PatientIllnessScript patIllScript;
	
	public DelTestAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object o) {
		new DBClinReason().deleteAndCommit((Relation)o);
		//new DBClinReason().saveAndCommit(patIllScript.getTests());
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Object o) {
		LogEntry le = new LogEntry(LogEntry.DELTEST_ACTION, patIllScript.getId(), ((Relation)o).getListItemId());
		le.save();		
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#delete(java.lang.String)
	 */
	/*public void delete(String id) {
		if(id==null || id.trim().equals("") || patIllScript==null || patIllScript.getTests()==null || patIllScript.getTests().isEmpty()){
			//todo error msg
			return;		
		}
		RelationTest rel = patIllScript.getTestById(Long.parseLong(id));
		patIllScript.getTests().remove(rel);
		updateGraph(rel);
		new ActionHelper().reOrderItems(patIllScript.getTests());
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		notifyLog(rel);
		save(rel);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_TEST_LIST, Relation.TYPE_TEST);

	}*/
	
	/* (non-Javadoc)
	 * @see actions.beanActions.DelAction#updateGraph(beans.relation.Relation)
	 */
	/*public void updateGraph(Relation rel){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_TEST);
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);
		//remove complete edge param for all these edges:
		if( patIllScript.getDiagnoses()!=null){
			for(int i=0; i < patIllScript.getDiagnoses().size(); i++){
				graph.removeEdgeWeight(patIllScript.getDiagnoses().get(i).getListItemId(), rel.getListItemId());
			}
		}
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}*/
}
