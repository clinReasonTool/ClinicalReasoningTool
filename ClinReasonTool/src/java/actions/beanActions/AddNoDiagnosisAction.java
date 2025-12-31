package actions.beanActions;

import java.awt.Point;
import java.beans.Beans;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import actions.scoringActions.ScoringAddAction;
import actions.scoringActions.ScoringListAction;
import application.ErrorMessageContainer;
import beans.*;
import beans.scripts.*;
import beans.graph.Box;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.helper.TypeAheadBean;
import beans.relation.*;
import beans.scoring.ScoreBean;
import beans.scripts.IllnessScriptInterface;
import controller.NavigationController;
import controller.XAPIController;
import database.DBClinReason;
import database.DBList;
import beans.list.ListItem;
import properties.IntlConfiguration;
import util.CRTLogger;

public class AddNoDiagnosisAction{

	private PatientIllnessScript patIllScript;
	
	public AddNoDiagnosisAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/**
	 * @param idStr either an id or syn_id (for a synonym)
	 * @param name
	 * @param xStr (e.g. "199.989894") -> we have to convert it into int
	 * @param yStr
	 */
	public void add(List l, int box){ 
		
		if(l==null) l = new ArrayList();
		
		//if(patIllScript.getDiagnoses()==null) patIllScript.setDiagnoses(new ArrayList<RelationDiagnosis>());
		RelationDiagnosis ddx = new RelationDiagnosis(0,patIllScript.getId(), -1);
		if(l.contains(ddx)) {
		//if(patIllScript.getDiagnoses().contains(ddx)){
			createErrorMessage(IntlConfiguration.getValue("ddx.duplicate"),"optional details", FacesMessage.SEVERITY_WARN);
			return;
		}
		ddx.setFinalDiagnosis(patIllScript.getCurrentStage());
		ddx.setStage(patIllScript.getCurrentStage());
		if(patIllScript.isExpScript()) ddx.setStage(patIllScript.getStage());
		//ddx.setOrder(patIllScript.getDiagnoses().size());
		ddx.setOrder(l.size());
		ListItem li = new DBList().selectListItemById(0);
		ddx.setDiagnosis(li);
		
		ddx.setXAndY(calculateNewItemPosInCanvas(l));		
		save(ddx);
		patIllScript.getListByType(Box.BOXTYPE_DDX, -1).add(ddx);		
		

		notifyLog(ddx);
		updateGraph(ddx, box);
		//if(!patIllScript.isExpScript()) updateXAPIStatement(rel);
	}
	

	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#createErrorMessage(java.lang.String, java.lang.String, javax.faces.application.FacesMessage.Severity)
	 */
	public void createErrorMessage(String summary, String details, Severity sev){
		new ErrorMessageContainer().addErrorMessage("ddxform", summary, details, sev);
	}
	
	/**
	 * we calculate a position for the new item. 
	 * TODO: we could check whether the position is already taken,.....
	 * @return
	 */
	private Point calculateNewItemPosInCanvas(List rels){
		int y = AddAction.MIN_Y;
		if(rels!=null || !rels.isEmpty()){
			y = rels.size() * 26;//CAVE max y!
		}
		if(patIllScript.isExpScript()){
			return new Point(RelationDiagnosis.DEFAULT_X+100,y);
		}
		return new Point(RelationDiagnosis.DEFAULT_X,y);
	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#save(beans.relation.Relation)
	 */
	public void save(Beans b){
		//TODO save whole problems collection?
		new DBClinReason().saveAndCommit(b);
	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Relation rel){
		new LogEntry(LogEntry.ADDDIAGNOSIS_ACTION, patIllScript.getId(), rel.getListItemId()).save();
		if(!patIllScript.isExpScript())
			new TypeAheadBean(rel.getListItemId(), Relation.TYPE_DDX).save();

	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#initScoreCalc(beans.relation.Relation)
	 */
	public void triggerScoringAction(Beans relDDX, boolean isJoker){
		new ScoringAddAction().scoreAction(((RelationDiagnosis) relDDX).getListItemId(), this.patIllScript, isJoker, Relation.TYPE_DDX);
		new ScoringListAction(patIllScript).scoreList(ScoreBean.TYPE_DDX_LIST, ScoreBean.TYPE_ADD_DDX);
	}
	
	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#updateGraph(beans.relation.Relation)
	 */
	public void updateGraph(Relation rel, int box) {
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		//MultiVertex mv = g.addVertex(ddx, patIllScript.getType());	

		MultiVertex mv = graph.addVertex(rel, IllnessScriptInterface.TYPE_LEARNER_CREATED, box);
		mv.setExpertVertex(rel);
		
		// add implicit edges:
	/*	if(patIllScript.getTests()!=null){
			for(int i=0; i < patIllScript.getTests().size(); i++){
				graph.addImplicitEdge(rel.getListItemId(), patIllScript.getTests().get(i).getListItemId(), IllnessScriptInterface.TYPE_LEARNER_CREATED);
			}
		}
		if(patIllScript.getMngs()!=null){
			for(int i=0; i < patIllScript.getMngs().size(); i++){
				graph.addImplicitEdge(rel.getListItemId(), patIllScript.getMngs().get(i).getListItemId(), IllnessScriptInterface.TYPE_LEARNER_CREATED);
			}
		}
		if(patIllScript.getProblems()!=null){
			for(int i=0; i < patIllScript.getProblems().size(); i++){
				graph.addImplicitEdge(patIllScript.getProblems().get(i).getListItemId(), rel.getListItemId(), IllnessScriptInterface.TYPE_LEARNER_CREATED);
			}
		}*/
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}

	
	public void updateXAPIStatement(Relation rel){
		XAPIController.getInstance().addOrUpdateAddStatement(rel);
	}

}
