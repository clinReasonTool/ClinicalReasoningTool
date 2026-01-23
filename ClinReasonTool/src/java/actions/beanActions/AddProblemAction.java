package actions.beanActions;

import java.awt.Point;
import java.beans.Beans;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

//import org.hibernate.tool.hbm2x.StringUtils;

import beans.*;
import beans.graph.Graph;
import beans.helper.TypeAheadBean;
import beans.relation.*;
import beans.scoring.ScoreBean;
import beans.scripts.*;
import controller.NavigationController;
import controller.RelationController;
import controller.XAPIController;
import database.DBClinReason;
import database.DBList;
import beans.list.ListItem;
import properties.IntlConfiguration;
import util.CRTLogger;
import actions.scoringActions.ScoringAddAction;
import actions.scoringActions.ScoringListAction;
import application.ErrorMessageContainer;
import actions.scoringActions.Scoreable;

/**
 * A problem is added to a PatientIllnessScript by picking an item from the list, either from the list view 
 * or from the concept map view.We add the new problem to the problems list in the PatientIllnessScript, save it, 
 * and trigger a scoring and feedback action.
 * @author ingahege
 * @deprecated
 */
public class AddProblemAction /*implements AddAction, Scoreable*/{

	private PatientIllnessScript patIllScript;
	private String prefix = null;
	
	public AddProblemAction() {}
	public AddProblemAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	public AddProblemAction(PatientIllnessScript patIllScript, String prefix){
		this.patIllScript = patIllScript;
		this.prefix = prefix;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#add(java.lang.String)
	 */
	public void add(String idStr, String name){ 
		//addProblem(idStr, name);
		//long id = Long.valueOf(idStr.trim());
		//add(idStr, name, "-1", "-1");
	}
	

	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#add(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	/*public void add(String idStr, String prefix, String xStr, String yStr){ 
		this.prefix = prefix;
		new RelationController().initAdd(idStr, prefix, xStr, yStr, this, patIllScript.getLocale());
	}
	
	public void addRelation(ListItem li, int x, int y, long synId){
		this.prefix = prefix;
		addRelation(li, x, y, synId, false);
	}*/
	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#addRelation(long, java.lang.String, int, int, long)
	 */
/*	public void addRelation(ListItem li, int x, int y, long synId, boolean isJoker){
		if(patIllScript.getProblems()==null) patIllScript.setProblems(new ArrayList<RelationProblem>());
		RelationProblem rel = new RelationProblem(li.getItem_id(), patIllScript.getId(), synId);		
		if(patIllScript.getProblems().contains(rel)){
			createErrorMessage(IntlConfiguration.getValue("findings.duplicate"),"optional details", FacesMessage.SEVERITY_WARN);
			return;
		}
		if(prefix!=null && !prefix.trim().equals("") ){ //check whether a prefix has been chosen
			//rel.setPrefix(Integer.valueOf(prefix).intValue());
			rel.setPrefix(prefix);
		}
		rel.setOrder(patIllScript.getProblems().size());
		rel.setStage(patIllScript.getCurrentStage());
		if(patIllScript.isExpScript()){
			rel.setStage(patIllScript.getStage());
		}
		if(x<0 && y<0) rel.setXAndY(calculateNewItemPosInCanvas());		
		else rel.setXAndY(new Point(x,y)); //problem has been created from the concept map, therefore we have a position
		patIllScript.getProblems().add(rel);
		//rel.setProblem(new DBList().selectListItemById(id));
		rel.setProblem(li);
		save(rel);
		notifyLog(rel);
		updateGraph(rel);
		triggerScoringAction(rel, isJoker);
		if(!patIllScript.isExpScript()) updateXAPIStatement(rel);
		//((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute("prob", rel);

	}*/
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#createErrorMessage(java.lang.String, java.lang.String, javax.faces.application.FacesMessage.Severity)
	 */
	public void createErrorMessage(String summary, String details, Severity sev){
		new ErrorMessageContainer().addErrorMessage("probform", summary, details, sev);
	}
	
	/**
	 * we calculate a position for the new item. 
	 * TODO: we could check whether the position is already taken,or others are vacant due to deleting of others
	 * @return
	 */
	/*private Point calculateNewItemPosInCanvas(){
		int size=0;
		if(patIllScript.getProblems()!=null || !patIllScript.getProblems().isEmpty()){
			size = patIllScript.getProblems().size();
		}
		return calculateNewItemPosInCanvas(size, patIllScript.isExpScript());
	}
	
	public Point calculateNewItemPosInCanvas(int size, boolean isExpert){
		int y = AddAction.MIN_Y;
		y = size * 26; //CAVE max y! 
		
		//if an expert script we have to position the item on the x axis to the left:
		if(isExpert){
			return new Point(RelationProblem.DEFAULT_X+100,y);
		}
		return new Point(RelationProblem.DEFAULT_X,y);
	}*/
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#save(beans.relation.Relation)
	 */
	public void save(Beans b){ new DBClinReason().saveAndCommit(b); }
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Relation relProb){
		new LogEntry(LogEntry.ADDPROBLEM_ACTION, patIllScript.getId(), relProb.getListItemId()).save();
		if(!patIllScript.isExpScript()) new TypeAheadBean(relProb.getListItemId(), Relation.TYPE_PROBLEM).save();
	}
	

	/* (non-Javadoc)
	 * @see actions.scoringActions.Scoreable#triggerScoringAction(java.beans.Beans)
	 */
	public void triggerScoringAction(Beans relProb, boolean isJoker){		
		new ScoringAddAction().scoreAction(((RelationProblem) relProb).getListItemId(), this.patIllScript, isJoker, Relation.TYPE_PROBLEM);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_PROBLEM_LIST, Relation.TYPE_PROBLEM);

	}

	
	/*public void triggerFeedbackAction() {
		// TODO Auto-generated method stub
		
	}*/

	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#updateGraph(beans.relation.Relation)
	 */
	/*public void updateGraph(Relation rel) {
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		graph.addVertex(rel, IllnessScriptInterface.TYPE_LEARNER_CREATED);
		if( patIllScript.getDiagnoses()!=null && patIllScript.getDiagnoses().size()>0){
			for(int i=0; i<patIllScript.getDiagnoses().size(); i++){
				graph.addImplicitEdge(rel.getListItemId(), patIllScript.getDiagnoses().get(i).getListItemId(), IllnessScriptInterface.TYPE_LEARNER_CREATED);
			}
		}
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}
	
	private void updateXAPIStatement(Relation rel){
		XAPIController.getInstance().addOrUpdateAddStatement(rel);
	}*/
}
