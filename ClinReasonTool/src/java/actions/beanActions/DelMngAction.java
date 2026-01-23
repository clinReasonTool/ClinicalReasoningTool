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
public class DelMngAction /*implements DelAction*/{
	private PatientIllnessScript patIllScript;
	
	public DelMngAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.DelAction#save(beans.relation.Relation)
	 */
	public void save(Object rel) {
		new DBClinReason().deleteAndCommit(rel);
	//	new DBClinReason().saveAndCommit(patIllScript.getMngs()); //orderNrs have changed, so we have to save all
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Object o) {
		LogEntry le = new LogEntry(LogEntry.DELMNG_ACTION, patIllScript.getId(), ((Relation)o).getListItemId());
		le.save();		
	}

	/* (non-Javadoc)
	 * @see beanActions.DelAction#delete(java.lang.String)
	 */
	/*public void delete(String id) {
		if(id==null || id.trim().equals("") || patIllScript==null || patIllScript.getMngs()==null || patIllScript.getMngs().isEmpty()){
			//todo error msg
			return;		
		}
		RelationManagement rel = patIllScript.getMngById(Long.parseLong(id));
		patIllScript.getMngs().remove(rel);
		updateGraph(rel);
		new ActionHelper().reOrderItems(patIllScript.getMngs());		
		notifyLog(rel);
		new DelConnectionAction(patIllScript).deleteConns(rel.getId());
		save(rel);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_MNG_LIST, Relation.TYPE_MNG);

	}*/
	
	/* (non-Javadoc)
	 * @see actions.beanActions.DelAction#updateGraph(beans.relation.Relation)
	 */
/*	public void updateGraph(Relation rel){
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex vertex = graph.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_MNG);
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
