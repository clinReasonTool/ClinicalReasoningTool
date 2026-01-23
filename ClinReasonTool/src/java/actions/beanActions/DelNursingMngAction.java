package actions.beanActions;

import java.util.*;

import actions.scoringActions.ScoringListAction;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.RelationNursingManagement;
import beans.relation.Relation;
import beans.scoring.ScoreBean;
import controller.NavigationController;
import database.DBClinReason;
import util.CRTLogger;

/**
 * @author ingahege
 * @deprecated
 */
public class DelNursingMngAction /*implements DelAction*/{
	private PatientIllnessScript patIllScript;
	
	public DelNursingMngAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object o) {
		new DBClinReason().deleteAndCommit((Relation)o);
		//new DBClinReason().saveAndCommit(patIllScript.getNursingManagement());
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Object o) {
		LogEntry le = new LogEntry(LogEntry.DELNMNG_ACTION, patIllScript.getId(), ((Relation)o).getListItemId());
		le.save();		
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#delete(java.lang.String)
	 */
	/*public void delete(String id) {
		if(id==null || id.trim().equals("") || patIllScript==null || patIllScript.getNursingManagement()==null || patIllScript.getNursingManagement().isEmpty()){
			//todo error msg
			return;		
		}
		RelationNursingManagement rel = patIllScript.getNursingMngById(Long.parseLong(id));
		patIllScript.getNursingManagement().remove(rel);
		updateGraph(rel);
		new ActionHelper().reOrderItems(patIllScript.getNursingManagement());
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		notifyLog(rel);
		save(rel);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_NMNG_LIST, Relation.TYPE_NMNG);

	}*/
	
	/* (non-Javadoc)
	 * @see actions.beanActions.DelAction#updateGraph(beans.relation.Relation)
	 */
	/*public void updateGraph(Relation rel){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_NMNG);
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);
		//remove complete edge param for all these edges:
		
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}*/
}
