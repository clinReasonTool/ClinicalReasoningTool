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
import beans.graph.Box;
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
 * 
 * @author ingahege
 *
 */
public class AddRelationAction implements AddAction, Scoreable{

	private PatientIllnessScript patIllScript;
	private String prefix = null;
	private Box box;
	
	public AddRelationAction() {}
	public AddRelationAction(PatientIllnessScript patIllScript, Box box){
		this.patIllScript = patIllScript;
		this.box = box;
	}
	public AddRelationAction(PatientIllnessScript patIllScript, String prefix){
		this.patIllScript = patIllScript;
		this.prefix = prefix;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#add(java.lang.String)
	 */
	public void add(String idStr, String prefix, String name){ 
		//addProblem(idStr, name);
		//long id = Long.valueOf(idStr.trim());
		add(idStr, prefix, name, "-1", "-1");
	}
	

	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#add(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void add(String idStr, String prefix, String name, String xStr, String yStr){ 
		this.prefix = prefix;
		new RelationController().initAdd(idStr, name, prefix, xStr, yStr, this, patIllScript.getLocale());
	}
	
	public void addRelation(ListItem li, int x, int y, long synId){
		//this.prefix = prefix;
		List rels = this.patIllScript.getListByType(box.getBoxType(),box.getSubType());
		if(rels==null) rels = new ArrayList();
		Relation rel = createRelation(li, x, y, synId, rels.size());
		if(addRelation(rel,rels, false))
			this.patIllScript.addRelationToListByType(rel, box.getBoxType());
	}
	
	/**
	 * Creates a Relation object with all parameters.
	 * @param li
	 * @param x
	 * @param y
	 * @param synId
	 * @param pos
	 * @return
	 */
	private Relation createRelation(ListItem li, int x, int y, long synId, int pos) {
		Relation rel = getRelationByType();
		if(rel==null) return null; //should not happen! 
		rel.setStage(patIllScript.getCurrentStage());
		if(patIllScript.isExpScript())
			rel.setStage(patIllScript.getStage());
		
		if(x<0 && y<0) rel.setXAndY(calculateNewItemPosInCanvas(pos, patIllScript.isExpScript()));		
		else rel.setXAndY(new Point(x,y));
		if(prefix!=null && !prefix.trim().equals("")){ //check whether a prefix has been chosen
			rel.setPrefix(prefix);
		}
		rel.setListItem(li);
		rel.setDestId(this.patIllScript.getId());
		rel.setListItemId(li.getItem_id());
		if(synId>0) rel.setSynId(synId);
		rel.setDiscriminator(this.box.getBoxType());
		rel.setOrder(pos);
		return rel;
	}
	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#addRelation(long, java.lang.String, int, int, long)
	 */
	private boolean addRelation(Relation rel, List rels, boolean isJoker){
		//List relations = patIllScript.getBox1Relations(); 
		
		//if(relations==null) patIllScript.setProblems(new ArrayList<RelationProblem>());
		//Relation rel = new RelationProblem(li.getItem_id(), patIllScript.getId(), synId);		
		if(rels.contains(rel)){
			createErrorMessage(IntlConfiguration.getValue("findings.duplicate"),"optional details", FacesMessage.SEVERITY_WARN);
			return false;
		}
		rels.add(rel);
		save(rel);
		notifyLog(rel);
		updateGraph(rel, box.getIdx());
		
		if(!patIllScript.isExpScript()) {
			triggerScoringAction(rel, isJoker);
			updateXAPIStatement(rel);
		}
		return true;
	}
	
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
	/*private Point calculateNewItemPosInCanvas(int size){

		return calculateNewItemPosInCanvas(size, patIllScript.isExpScript());
	}*/
	
	public Point calculateNewItemPosInCanvas(int size, boolean isExpert){
		int y = AddAction.MIN_Y;
		y = size * 26; //CAVE max y! 
		
		//if an expert script we have to position the item on the x axis to the left:
		if(isExpert){
			return new Point(Box.BOX1_3_X +100,y);
		}
		return new Point(Box.BOX1_3_X,y);
	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#save(beans.relation.Relation)
	 */
	public void save(Beans b){ new DBClinReason().saveAndCommit(b); }
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Relation rel){
		new LogEntry(LogEntry.ADDPROBLEM_ACTION, patIllScript.getId(), rel.getListItemId()).save();
		if(!patIllScript.isExpScript()) new TypeAheadBean(rel.getListItemId(), Relation.TYPE_PROBLEM).save();
	}
	

	/* (non-Javadoc)
	 * @see actions.scoringActions.Scoreable#triggerScoringAction(java.beans.Beans)
	 */
	public void triggerScoringAction(Beans relProb, boolean isJoker){		
		new ScoringAddAction().scoreAction(((Relation) relProb).getListItemId(), this.patIllScript, isJoker, Relation.TYPE_PROBLEM);
		new ScoringListAction(this.patIllScript).scoreList(ScoreBean.TYPE_PROBLEM_LIST, Relation.TYPE_PROBLEM);

	}

	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#updateGraph(beans.relation.Relation)
	 */
	public void updateGraph(Relation rel, int box) {
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		graph.addVertex(rel, IllnessScriptInterface.TYPE_LEARNER_CREATED, box);
		//not sure what implicit edges are used for???
		/*if( patIllScript.getDiagnoses()!=null && patIllScript.getDiagnoses().size()>0){
			for(int i=0; i<patIllScript.getDiagnoses().size(); i++){
				graph.addImplicitEdge(rel.getListItemId(), patIllScript.getDiagnoses().get(i).getListItemId(), IllnessScriptInterface.TYPE_LEARNER_CREATED);
			}
		}*/
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}
	
	private void updateXAPIStatement(Relation rel){
		XAPIController.getInstance().addOrUpdateAddStatement(rel);
	}
	
	private Relation getRelationByType() {
		int type = box.getBoxType();
		switch(type) {
		  case Box.BOXTYPE_FDG: return new RelationProblem();
		  case Box.BOXTYPE_DDX: return new RelationDiagnosis();
		  case Box.BOXTYPE_MNG: return new RelationManagement();
		  case Box.BOXTYPE_TST: return new RelationTest();
		  case Box.BOXTYPE_AIM: return new RelationAim();
		  case Box.BOXTYPE_INF: return new RelationInformation();
		  case Box.BOXTYPE_PAT: return new RelationPatho();
		}
		return null;
	}
}
