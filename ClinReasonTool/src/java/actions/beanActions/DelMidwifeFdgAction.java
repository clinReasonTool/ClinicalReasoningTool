package actions.beanActions;

import java.util.*;

import actions.scoringActions.ScoringListAction;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.RelationMidwifeFinding;
import beans.relation.Relation;
import beans.scoring.ScoreBean;
import controller.NavigationController;
import database.DBClinReason;
import util.CRTLogger;
/**
 * @author ingahege
 * @deprecated
 */
public class DelMidwifeFdgAction /*implements DelAction*/{
	private PatientIllnessScript patIllScript;
	
	public DelMidwifeFdgAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object o) {
		new DBClinReason().deleteAndCommit((Relation)o);
		//new DBClinReason().saveAndCommit(patIllScript.getMidwifeFindings());
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Object o) {
		LogEntry le = new LogEntry(LogEntry.DELMFDG_ACTION, patIllScript.getId(), ((Relation)o).getListItemId());
		le.save();		
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#delete(java.lang.String)
	 */
	/*public void delete(String id) {
		if(id==null || id.trim().equals("") || patIllScript==null || patIllScript.getMidwifeFindings()==null || patIllScript.getMidwifeFindings().isEmpty()){
			//todo error msg
			return;		
		}
		RelationMidwifeFinding rel = patIllScript.getMidwifeFdgById(Long.parseLong(id));
		patIllScript.getMidwifeFindings().remove(rel);
		updateGraph(rel);
		new ActionHelper().reOrderItems(patIllScript.getMidwifeFindings());
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		notifyLog(rel);
		save(rel);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_MFDG_LIST, Relation.TYPE_MFDG);

	}*/
	
	/* (non-Javadoc)
	 * @see actions.beanActions.DelAction#updateGraph(beans.relation.Relation)
	 */
	/*public void updateGraph(Relation rel){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_MFDG);
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);
		//remove complete edge param for all these edges:
		
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}*/
}
