package actions.beanActions;

import java.util.*;

import actions.scoringActions.ScoringListAction;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.RelationNursingDiagnosis;
import beans.relation.Relation;
import beans.scoring.ScoreBean;
import controller.NavigationController;
import database.DBClinReason;
import util.CRTLogger;
/**
 * @author ingahege
 * @deprecated
 */
public class DelNursingDiagnosisAction /*implements DelAction*/{
	private PatientIllnessScript patIllScript;
	
	public DelNursingDiagnosisAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object o) {
		new DBClinReason().deleteAndCommit((Relation)o);
		//new DBClinReason().saveAndCommit(patIllScript.getNursingDiagnoses());
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Object o) {
		LogEntry le = new LogEntry(LogEntry.DELNDDX_ACTION, patIllScript.getId(), ((Relation)o).getListItemId());
		le.save();		
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#delete(java.lang.String)
	 */
	/*public void delete(String id) {
		if(id==null || id.trim().equals("") || patIllScript==null || patIllScript.getNursingDiagnoses()==null || patIllScript.getNursingDiagnoses().isEmpty()){
			//todo error msg
			return;		
		}
		RelationNursingDiagnosis rel = patIllScript.getNursingDiagnosisById(Long.parseLong(id));
		patIllScript.getNursingDiagnoses().remove(rel);
		updateGraph(rel);
		new ActionHelper().reOrderItems(patIllScript.getNursingDiagnoses());
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		notifyLog(rel);
		save(rel);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_NDDX_LIST, Relation.TYPE_NDDX);

	}*/
	
	/* (non-Javadoc)
	 * @see actions.beanActions.DelAction#updateGraph(beans.relation.Relation)
	 */
	/*public void updateGraph(Relation rel){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_NDDX);
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);
		//remove complete edge param for all these edges:
		
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}*/
}
