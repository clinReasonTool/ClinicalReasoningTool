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
import beans.*;
import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiEdge;
import beans.graph.MultiVertex;
import beans.helper.TypeAheadBean;
import beans.relation.*;
import beans.scoring.ScoreBean;
import beans.scripts.IllnessScriptInterface;
import controller.GraphController;
import controller.NavigationController;
import controller.RelationController;
import controller.XAPIController;
import database.DBClinReason;
import beans.list.ListItem;
import properties.IntlConfiguration;
import util.CRTLogger;

public class AddDiagnosisAction implements AddAction, Scoreable{

	private PatientIllnessScript patIllScript;
	
	public AddDiagnosisAction() {}
	public AddDiagnosisAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#add(java.lang.String)
	 */
	public void add(String idStr, String name){ 
		//addProblem(idStr, name);
		//long id = Long.valueOf(idStr.trim());
		add(idStr, name, "-1", "-1");
	}
	
	/**
	 * @param idStr either an id or syn_id (for a synonym)
	 * @param name
	 * @param xStr (e.g. "199.989894") -> we have to convert it into int
	 * @param yStr
	 */
	public void add(String idStr, String name, String xStr, String yStr){ 
		new RelationController().initAdd(idStr, name, xStr, yStr, this, patIllScript.getLocale());
	}
	
	public void addRelation(/*long id, String name,*/ListItem li, int x, int y, long synId){
		 addRelation(li, x, y, synId, false);

	}
	
	public void addRelation(ListItem li, /*long id, String name2,*/ int x, int y, long synId, boolean isJoker){
		if(patIllScript.getDiagnoses()==null) patIllScript.setDiagnoses(new ArrayList<RelationDiagnosis>());
		RelationDiagnosis rel = new RelationDiagnosis(li.getItem_id(), patIllScript.getId(), synId);		
		if(patIllScript.getDiagnoses().contains(rel)){
			createErrorMessage(IntlConfiguration.getValue("ddx.duplicate"),"optional details", FacesMessage.SEVERITY_WARN);
			return;
		}
		rel.setOrder(patIllScript.getDiagnoses().size());
		rel.setStage(patIllScript.getCurrentStage());
		if(patIllScript.isExpScript()){
			rel.setStage(patIllScript.getStage());
		}
		if(x<0 && y<0) rel.setXAndY(calculateNewItemPosInCanvas());		
		else rel.setXAndY(new Point(x,y)); //problem has been created from the concept map, therefore we have a position

		patIllScript.getDiagnoses().add(rel);
		rel.setDiagnosis(li);
		//rel.setDiagnosis(new DBList().selectListItemById(id));
		save(rel);
		notifyLog(rel);
		updateGraph(rel);
		triggerScoringAction(rel, isJoker);	
		if(!patIllScript.isExpScript()) updateXAPIStatement(rel);
		//((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute("ddx", rel);

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
	private Point calculateNewItemPosInCanvas(){
		int size=0;
		if(patIllScript.getDiagnoses()!=null || !patIllScript.getDiagnoses().isEmpty()){
			size = patIllScript.getDiagnoses().size();
		}
		return calculateNewItemPosInCanvas(size, patIllScript.isExpScript());		
	}
	
	public Point calculateNewItemPosInCanvas(int size, boolean isExpert){
		int y = AddAction.MIN_Y;
		y = size * 26; //CAVE max y! 
		
		//if an expert script we have to position the item on the x axis to the left:
		if(isExpert){
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
	public void updateGraph(Relation rel) {
		Graph graph = NavigationController.getInstance().getMyFacesContext().getGraph();
		graph.addVertex(rel, IllnessScriptInterface.TYPE_LEARNER_CREATED);

		// add implicit edges:
		if(patIllScript.getTests()!=null){
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
		}
		//addHierarchyRelation(rel, graph); //creates a lot of unwanted connections
		CRTLogger.out(graph.toString(), CRTLogger.LEVEL_TEST);
	}
	
	/**
	 * If a new differential is a more specific one than a previously added differential, we connect those two 
	 * with a specific hierarchy connection. 
	 * Currently we only do that for expert scripts!
	 * @param rel
	 * @param g
	 */
	/*private void addHierarchyRelation(Relation rel, Graph g){
		if(patIllScript.isExpScript()){
			MultiVertex mv = g.getVertexByIdAndType(rel.getListItemId(), Relation.TYPE_DDX);
			List<MultiVertex> mv2 = new GraphController(g).findNextHierarchyVertices(mv, Relation.TYPE_DDX);
			if(mv2==null)return;
			for(int i=0;i<mv2.size(); i++){
				new AddConnectionAction(patIllScript).addConnection(rel.getId(), mv2.get(i).getLearnerVertex().getId(), Relation.TYPE_DDX, Relation.TYPE_DDX, MultiEdge.WEIGHT_EXPLICIT_HIERARCHY, MultiEdge.ENDPOINT_RIGHT, MultiEdge.ENDPOINT_RIGHT);
			}
		}
	}*/
	
	public void updateXAPIStatement(Relation rel){
		XAPIController.getInstance().addOrUpdateAddStatement(rel);
	}
}
