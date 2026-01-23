package actions.beanActions;

import java.awt.Point;
import java.beans.Beans;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import actions.scoringActions.Scoreable;
import actions.scoringActions.ScoringAddAction;
import actions.scoringActions.ScoringListAction;
import application.ErrorMessageContainer;
import beans.LogEntry;
import beans.scripts.*;
import beans.graph.Graph;
import beans.helper.TypeAheadBean;
import beans.relation.Relation;
import beans.relation.RelationManagement;
import beans.relation.RelationTest;
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
public class AddTestAction /*implements AddAction, Scoreable*/{
	
	private PatientIllnessScript patIllScript;
	
	public AddTestAction() {}
	public AddTestAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	

	/* (non-Javadoc)
	 * @see beanActions.AddAction#save(java.beans.Beans)
	 */
	public void save(Beans b) { new DBClinReason().saveAndCommit(b);}

	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Relation rel) {
		new LogEntry(LogEntry.ADDTEST_ACTION, patIllScript.getId(), rel.getListItemId()).save();
		if(!patIllScript.isExpScript()) new TypeAheadBean(rel.getListItemId(), Relation.TYPE_TEST).save();

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
	/*public void add(String idStr, String name, String xStr, String yStr){ 
		new RelationController().initAdd(idStr, name, xStr, yStr, this, patIllScript.getLocale());
	}
	
	public void addRelation(ListItem li, int x, int y, long synId){		
		addRelation(li, x, y, synId, false);
	}*/
	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#addRelation(long, java.lang.String, int, int, long)
	 */
	/*public void addRelation(ListItem li, int x, int y, long synId, boolean isJoker){
		if(patIllScript.getTests()==null) patIllScript.setTests(new ArrayList<RelationTest>());
		RelationTest rel = new RelationTest(li.getItem_id(), patIllScript.getId(), synId);		
		if(patIllScript.getTests().contains(rel)){
			createErrorMessage(IntlConfiguration.getValue("tests.duplicate"),"optional details", FacesMessage.SEVERITY_WARN);
			return;
		}
		rel.setOrder(patIllScript.getTests().size());
		rel.setStage(patIllScript.getCurrentStage());
		if(patIllScript.isExpScript()){
			rel.setStage(patIllScript.getStage());
		}
		if(x<0 && y<0) rel.setXAndY(calculateNewItemPosInCanvas());		
		else rel.setXAndY(new Point(x,y)); //problem has been created from the concept map, therefore we have a position

		patIllScript.getTests().add(rel);
		//rel.setTest(new DBList().selectListItemById(id));
		rel.setTest(li);
		save(rel);
		updateGraph(rel);
		notifyLog(rel);
		triggerScoringAction(rel, isJoker);	
		if(!patIllScript.isExpScript()) updateXAPIStatement(rel);
		//((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute("tst", rel);

	}*/
	
	/**
	 * we calculate a position for the new item. 
	 * TODO: we could check whether the position is already taken,or others are vacant due to deleting of others
	 * @return
	 */
	/*private Point calculateNewItemPosInCanvas(){
		int size=0;
		if(patIllScript.getTests()!=null || !patIllScript.getTests().isEmpty()){
			size = patIllScript.getTests().size();
		}
		return calculateNewItemPosInCanvas(size, patIllScript.isExpScript());		
	}
	
	public Point calculateNewItemPosInCanvas(int size, boolean isExpert){
		int y = AddAction.MIN_Y;
		y = size * 26; //CAVE max y! 
		
		//if an expert script we have to position the item on the x axis to the left:
		if(isExpert){
			return new Point(RelationTest.DEFAULT_X+100,y);
		}
		return new Point(RelationTest.DEFAULT_X,y);
	}*/
	
	/*private Point calculateNewItemPosInCanvas(){
		int y = AddAction.MIN_Y;
		if(patIllScript.getTests()!=null || !patIllScript.getTests().isEmpty()){
			y = patIllScript.getTests().size() * 26; //CAVE max y! 
		}
		if(patIllScript.isExpScript()){
			return new Point(RelationTest.DEFAULT_X+100,y);
		}
		return new Point(RelationTest.DEFAULT_X,y);
	}*/

	/* (non-Javadoc)
	 * @see actions.scoringActions.Scoreable#triggerScoringAction(java.beans.Beans)
	 */
	public void triggerScoringAction(Beans rel, boolean isJoker) {
		new ScoringAddAction().scoreAction(((RelationTest) rel).getListItemId(), this.patIllScript, isJoker, Relation.TYPE_TEST);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_TEST_LIST, Relation.TYPE_TEST);

	}
	public void createErrorMessage(String summary, String details, Severity sev) {
		new ErrorMessageContainer().addErrorMessage("testform", summary, details, sev);		
	}

	
	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#updateGraph(beans.relation.Relation)
	 */
	/*public void updateGraph(Relation rel) {
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		graph.addVertex(rel, IllnessScriptInterface.TYPE_LEARNER_CREATED);
	
		// add implicit edges:
		if(patIllScript.getDiagnoses()!=null && patIllScript.getDiagnoses().size()>0){
			for(int i=0; i < patIllScript.getDiagnoses().size(); i++){
				graph.addImplicitEdge(patIllScript.getDiagnoses().get(i).getListItemId(), rel.getListItemId(), IllnessScriptInterface.TYPE_LEARNER_CREATED);
			}
		}
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}
	
	public void updateXAPIStatement(Relation rel){
		XAPIController.getInstance().addOrUpdateAddStatement(rel);
	}*/
}
