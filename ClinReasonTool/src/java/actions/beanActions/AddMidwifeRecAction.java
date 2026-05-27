package actions.beanActions;

import java.awt.Point;
import java.beans.Beans;
import java.util.ArrayList;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

import actions.scoringActions.Scoreable;
import actions.scoringActions.ScoringAddAction;
import actions.scoringActions.ScoringListAction;
import application.ErrorMessageContainer;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.Graph;
import beans.helper.TypeAheadBean;
import beans.relation.Relation;
import beans.relation.RelationRecommendation;
import beans.scoring.ScoreBean;
import beans.scripts.IllnessScriptInterface;
import controller.NavigationController;
import controller.RelationController;
import controller.XAPIController;
import database.DBClinReason;
import beans.list.ListItem;
import properties.IntlConfiguration;
import util.CRTLogger;

/**
 * @author ingahege
 * @deprecated
 */
public class AddMidwifeRecAction /*implements AddAction, Scoreable*/{
	
	private PatientIllnessScript patIllScript;
	
	public AddMidwifeRecAction() {}
	public AddMidwifeRecAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	

	/* (non-Javadoc)
	 * @see beanActions.AddAction#save(java.beans.Beans)
	 */
	public void save(Beans b) { new DBClinReason().saveAndCommit(b);}

	/* (non-Javadoc)
	 * @see beanActions.AddAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Relation rel) {
		new LogEntry(LogEntry.ADDMREC_ACTION, patIllScript.getId(), rel.getListItemId()).save();
		if(!patIllScript.isExpScript()) new TypeAheadBean(rel.getListItemId(), Relation.SUBTYPE_MREC).save();

	}

	/* (non-Javadoc)
	 * @see beanActions.AddAction#add(java.lang.String)
	 */
	//public void add(String idStr, String name){ add(idStr, name, "-1", "-1");}
	
	/**
	 * @param idStr either an id or syn_id (for a synonym)
	 * @param name
	 * @param xStr (e.g. "199.989894") -> we have to convert it into int
	 * @param yStr
	 */
	/*public void add(String idStr, String name, String xStr, String yStr){ new RelationController().initAdd(idStr, name, xStr, yStr, this, patIllScript.getLocale() );}
	
	public void addRelation(ListItem li, int x, int y, long synId){addRelation(li, x, y, synId, false);}
	
	public void addRelation(ListItem li, int x, int y, long synId, boolean isJoker){
		if(patIllScript.getMidwifeRecommendations()==null) patIllScript.setMidwifeRecommendations(new ArrayList<RelationMidwifeRecommendation>());
		RelationMidwifeRecommendation rel = new RelationMidwifeRecommendation(li.getItem_id(), patIllScript.getId(), synId);		
		if(patIllScript.getMidwifeRecommendations().contains(rel)){
			createErrorMessage(IntlConfiguration.getValue("info.duplicate"),"optional details", FacesMessage.SEVERITY_WARN);
			return;
		}
		rel.setOrder(patIllScript.getMidwifeRecommendations().size());
		rel.setStage(patIllScript.getCurrentStage());
		if(patIllScript.isExpScript()){
			rel.setStage(patIllScript.getStage());
		}
		if(x<0 && y<0) rel.setXAndY(calculateNewItemPosInCanvas());		
		else rel.setXAndY(new Point(x,y)); //problem has been created from the concept map, therefore we have a position

		patIllScript.getMidwifeRecommendations().add(rel);
		rel.setRecommendation(li);
		save(rel);
		updateGraph(rel);
		notifyLog(rel);
		triggerScoringAction(rel, isJoker);	
		if(!patIllScript.isExpScript()) updateXAPIStatement(rel);

	}*/
	

	/**
	 * we calculate a position for the new item. 
	 * TODO: we could check whether the position is already taken,or others are vacant due to deleting of others
	 * @return
	 */
	/*private Point calculateNewItemPosInCanvas(){
		int y = AddAction.MIN_Y;
		if(patIllScript.getMidwifeRecommendations()!=null || !patIllScript.getMidwifeRecommendations().isEmpty()){
			y = patIllScript.getMidwifeRecommendations().size() * 26; //CAVE max y! 
		}
		if(patIllScript.isExpScript()){
			return new Point(RelationMidwifeRecommendation.DEFAULT_X+100,y);
		}
		return new Point(RelationMidwifeRecommendation.DEFAULT_X,y);
	}*/

	/* (non-Javadoc)
	 * @see actions.scoringActions.Scoreable#triggerScoringAction(java.beans.Beans)
	 */
	public void triggerScoringAction(Beans rel, boolean isJoker) {
		new ScoringAddAction().scoreAction(((RelationRecommendation) rel).getListItemId(), this.patIllScript, isJoker, Relation.SUBTYPE_MREC);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_MREC_LIST, Relation.SUBTYPE_MREC);
	}
	
	public void createErrorMessage(String summary, String details, Severity sev) {
		new ErrorMessageContainer().addErrorMessage("infoform", summary, details, sev);		
	}
	
	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#updateGraph(beans.relation.Relation)
	 */
	/*public void updateGraph(Relation rel) {
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		graph.addVertex(rel, IllnessScriptInterface.TYPE_LEARNER_CREATED);

		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}
	
	public void updateXAPIStatement(Relation rel){
		XAPIController.getInstance().addOrUpdateAddStatement(rel);
	}*/
}
