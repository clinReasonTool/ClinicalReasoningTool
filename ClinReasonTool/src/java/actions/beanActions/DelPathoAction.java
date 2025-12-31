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
import database.DBClinReason;
import util.CRTLogger;

/**
 * @author ingahege
 * @deprecated
 */
public class DelPathoAction /*implements DelAction*/{
	private PatientIllnessScript patIllScript;
	
	public DelPathoAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object o) {
		new DBClinReason().deleteAndCommit((Relation)o);
		//new DBClinReason().saveAndCommit(patIllScript.getPatho());
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Object o) {
		LogEntry le = new LogEntry(LogEntry.DELPATHO_ACTION, patIllScript.getId(), ((Relation)o).getListItemId());
		le.save();		
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#delete(java.lang.String)
	 */
	/*public void delete(String id) {
		if(id==null || id.trim().equals("") || patIllScript==null || patIllScript.getPatho()==null || patIllScript.getPatho().isEmpty()){
			//todo error msg
			return;		
		}
		RelationPatho rel = patIllScript.getPathoById(Long.parseLong(id));
		patIllScript.getPatho().remove(rel);
		updateGraph(rel);
		new ActionHelper().reOrderItems(patIllScript.getPatho());
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		notifyLog(rel);
		save(rel);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_PATHO_LIST, Relation.TYPE_PATHO);

	}*/
	
	/* (non-Javadoc)
	 * @see actions.beanActions.DelAction#updateGraph(beans.relation.Relation)
	 */
/*	public void updateGraph(Relation rel){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_PATHO);
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);

		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}*/
}
