package actions.beanActions;

import java.util.*;

import actions.scoringActions.ScoringListAction;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.*;
import beans.relation.*;
import beans.scoring.ScoreBean;
import controller.NavigationController;
import controller.XAPIController;
import database.DBClinReason;
import util.CRTLogger;

public class DelRelationAction implements DelAction{
	private PatientIllnessScript patIllScript;
	private Box box;
	
	public DelRelationAction(PatientIllnessScript patIllScript, Box box){
		this.patIllScript = patIllScript;
		this.box = box;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object rel, List rels) {
		new DBClinReason().deleteAndCommit(rel);
		new DBClinReason().saveAndCommit(rels); //orderNrs have changed, so we have to save all
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
		List rels = this.patIllScript.getListByType(box.getBoxType(),box.getSubType());
		
		if(rels==null || rels.isEmpty()) return;
		Relation rel = null;
		for (int i=0;i<rels.size(); i++) {
			if(((Relation)rels.get(i)).getId() == Long.parseLong(id)) {
				rel = (Relation) rels.get(i);
				break;
			}
		}
		if(rel!=null) this.patIllScript.removeRelationFromList(rel, box.getBoxType());
		//new ActionHelper().reOrderItems(this.patIllScript.getListByType(box.getBoxType(),box.getSubType()));		
		notifyLog(rel);
		updateGraph(rel, rels);
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		if(!patIllScript.isExpScript()) XAPIController.getInstance().removeXAPIAddActionStatement(rel);
		save(rel, rels);
		if(!this.patIllScript.isExpScript()) new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_PROBLEM_LIST, box.getBoxType());

	}
	
	public void updateGraph(Relation rel, List rels){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), box.getBoxType());
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);
		//remove complete edge param for all these edges:
		if( rels!=null){
			for(int i=0; i < rels.size(); i++){
				graph.removeEdgeWeight(rel.getListItemId(), ((Relation)rels.get(i)).getListItemId());
			}
		}
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}
}
