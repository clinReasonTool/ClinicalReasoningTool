package actions.beanActions;

import java.awt.Point;
import java.beans.Beans;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import actions.scoringActions.Scoreable;
import actions.scoringActions.ScoringAddCnxAction;
import actions.scoringActions.ScoringCnxsAction;
import application.ErrorMessageContainer;
import beans.scripts.*;
import beans.graph.Box;
import beans.graph.Graph;
import beans.graph.MultiEdge;
import beans.graph.MultiVertex;
import beans.relation.Connection;
import beans.relation.Relation;
import beans.scripts.IllnessScriptInterface;
import controller.GraphController;
import controller.NavigationController;
import beans.LogEntry;
import database.DBClinReason;
import properties.IntlConfiguration;
import util.CRTLogger;
import util.StringUtilities;

public class AddConnectionAction implements Scoreable{

	private PatientIllnessScript patIllScript;
	private Box sourceBox;
	private Box targetBox;
	
	public AddConnectionAction(PatientIllnessScript patIllScript/*, Box sourceBox, Box targetBox*/){
		this.patIllScript = patIllScript;
		//this.sourceBox = sourceBox;
		//this.targetBox = targetBox;
	}
	
	/*public AddConnectionAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
		this.sourceBox = sourceBox;
		this.targetBox = targetBox;

	}*/
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#save(java.beans.Beans)
	 */
	public void save(Beans b) {
		new DBClinReason().saveAndCommit(b);
	}

	/*private void notifyLog(Relation rel) {}	*/
	private void notifyLog(Connection cnx) {
		LogEntry le = new LogEntry(LogEntry.ADDCONNECTION_ACTION, patIllScript.getId(), cnx.getStartId(), cnx.getTargetId());
		le.save();	
	}

	/**
	 * for non-hierarchy relations/connections we use a dynamic position of the target endpoint., depending on where
	 * the user has entered the target.
	 * @param sourceIdStr
	 * @param targetIdStr
	 * @param startEpIdxStr
	 * @param targetEpIdxStr
	 */
	public void add(String sourceIdStr, String targetIdStr, String startEpIdxStr, String targetEpXStr, String targetEpYStr){
		add(sourceIdStr, targetIdStr, startEpIdxStr, targetEpXStr, targetEpYStr, null);

	}
	/* (non-Javadoc)
	 * @see beanActions.AddAction#add(java.lang.String, java.lang.String)
	 */
	private void add(String sourceIdStr, String targetIdStr, String startEpIdxStr, String targetEpXStr, String targetEpYStr, String targetEpIdxStr) {
		setSourceAndTargetBoxes(sourceIdStr, targetIdStr);
		//String startType = sourceIdStr.substring(0, sourceIdStr.indexOf("_")+1);
		//String targetType = targetIdStr.substring(0, targetIdStr.indexOf("_")+sourceIdStr = sourceIdStr.substring(sourceIdStr.indexOf("_")+1);
		targetIdStr = targetIdStr.substring(targetIdStr.indexOf("_")+1);
		sourceIdStr = sourceIdStr.substring(sourceIdStr.indexOf("_")+1);
		long sourceId = Long.valueOf(sourceIdStr);
		long targetId = Long.valueOf(targetIdStr);
		int startEpIdx = 0; 
		int targetEpIdx = -1;  //for hierarchy relations still needed
		Point targetEP = null;
		try{
			startEpIdx = Integer.parseInt(startEpIdxStr);
			if(targetEpXStr!=null && targetEpXStr!=null){
				targetEP = new Point((int )Float.parseFloat(targetEpXStr),(int)Float.parseFloat(targetEpYStr));
				calculateEnpointPos(targetEP,targetId, targetBox.getBoxType()/*GraphController.getTypeByPrefix(targetType)*/);
			}
			else if(targetEpIdxStr!=null) targetEpIdx = Integer.parseInt(targetEpIdxStr);
		}
		catch(Exception e){
			CRTLogger.out("AddConnectionAction.add(), Exception: " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
		
		addConnection(sourceId, targetId, sourceBox.getBoxType()/*GraphController.getTypeByPrefix(startType)*/, targetBox.getBoxType()/*GraphController.getTypeByPrefix(targetType)*/, MultiEdge.WEIGHT_EXPLICIT, startEpIdx, targetEP, targetEpIdx);
	}
	
	/**
	 * for adding hierarchy relations, we use a fixed endpoint for the target.
	 * @param sourceId
	 * @param targetId
	 * @param startType
	 * @param targetType
	 * @param weight
	 * @param startEpIdx
	 * @param targetEpIdx
	 */
	protected void addConnection(long sourceId, long targetId, int startType, int targetType, int weight, int startEpIdx, int targetEpIdx){
		addConnection(sourceId, targetId, startType, targetType, weight, startEpIdx, null, targetEpIdx);	
	}
	
	private void addConnection(long sourceId, long targetId, int startType, int targetType, int weight, int startEpIdx, Point targetEP, int targetEpIdx){
		if(patIllScript.getConns()==null) patIllScript.setConns(new TreeMap<Long, Connection>());
		
		Connection cnx = new Connection(sourceId, targetId, this.patIllScript.getId(), startType, targetType, patIllScript.getStageForAction());
		cnx.setWeight(weight);
		//this should not happen, since we already handle it on the client side:
		if(patIllScript.getConns().containsValue(cnx)){
			createErrorMessage(IntlConfiguration.getValue("cnx.duplicate"), "", FacesMessage.SEVERITY_ERROR, cnx.getTargetType());
			return; //cnx already made... 
		}
		cnx.setStartEpIdx(startEpIdx);
		if(targetEP!=null) cnx.setTargetEndpoint(targetEP);
		else cnx.setTargetEpIdx(targetEpIdx);
		save(cnx); //first save to get an id...
		patIllScript.getConns().put(new Long(cnx.getId()), cnx);
		updateGraph(cnx);
		notifyLog(cnx);
		//Do not remove the following: it is necessary to correctly initialiaze the new connections!
		((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).setAttribute("id2", GraphController.PREFIX_CNX + cnx.getId());
	}

	/* (non-Javadoc)
	 * @see actions.scoringActions.Scoreable#triggerScoringAction(java.beans.Beans)
	 */
	public void triggerScoringAction(Beans cnx, boolean isJoker) {
		new ScoringAddCnxAction().scoreAction(((Connection)cnx).getId(), this.patIllScript, isJoker);
		new ScoringCnxsAction(patIllScript).scoreConnections(patIllScript.getCurrentStage());

		
	}

	private void createErrorMessage(String summary, String details, Severity sev, int targetType) {
		/*String formId = "probform";
		if(targetType==Relation.TYPE_DDX) formId = "cnxform";
		else if(targetType==Relation.TYPE_TEST) formId = "testform";
		else if(targetType == Relation.TYPE_MNG) formId = "mngform";*/
		new ErrorMessageContainer().addErrorMessage("", summary, details, sev);
		
	}
	
	/* (non-Javadoc)
	 * @see actions.beanActions.AddAction#updateGraph(beans.relation.Relation)
	 */
	private void updateGraph(Connection cnx) {
		Graph g = NavigationController.getInstance().getMyFacesContext().getGraph();
		g.addExplicitEdge(cnx, patIllScript, IllnessScriptInterface.TYPE_LEARNER_CREATED, cnx.getWeight());
		CRTLogger.out(g.toString(), CRTLogger.LEVEL_TEST);
	}
	
	/**
	 * The position we get from the endpoint of the connection is in relation to the overall canvas, but 
	 * we want to store it in relation to the target item/group container. So, we have to do some calculation here, 
	 * to get to the correct position. 
	 * @param ep
	 * @param targetId
	 * @param targetType
	 * @return
	 */
	private Point calculateEnpointPos(Point ep, long targetId, int targetType){
		Graph g = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex mv = g.getVertexByIdAndType(targetId, targetType);
		if(mv==null || mv.getLearnerVertex()==null) return null; //should not happen!
		Relation rel = mv.getLearnerVertex();
		return ep;
	}
	private void setSourceAndTargetBoxes(String src, String target) { 
		String s = src.substring(3,4);
		int sourceBoxIdx = Integer.parseInt(src.substring(3,4));
		int targetBoxIdx = Integer.parseInt(target.substring(3,4));
		sourceBox = this.patIllScript.getBoxByNo(sourceBoxIdx);
		targetBox = this.patIllScript.getBoxByNo(targetBoxIdx);
	}
}
