package beans.scripts;

import java.beans.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import javax.faces.bean.*;

import org.apache.commons.lang.StringUtils;

import actions.beanActions.*;
import actions.scoringActions.ScoringAction;
import actions.scoringActions.ScoringListAction;
import application.AppBean;
import beans.*;
import beans.error.MyError;
import beans.helper.TypeAheadBean;
import beans.relation.*;
import beans.relation.summary.SummaryStatement;
import beans.scoring.*;
import beans.user.User;
import beans.xAPI.StatementContainer;
import controller.*;
import database.DBClinReason;
import database.DBLog;
import properties.IntlConfiguration;
import util.CRTLogger;
import util.Encoder;
import util.StringUtilities;

/**
 * This is the Illness script the learner creates during the VP session.
 * @author ingahege
 *
 */
/*@ManagedBean(name = "patillscript", eager = true)*/
/**
 * @author ingahege
 *
 */
@SessionScoped
public class PatientIllnessScript extends Beans implements Comparable, IllnessScriptInterface, Serializable /*, PropertyChangeListener*/ {

	
	private static final long serialVersionUID = 1L;
	/**
	 * The VP has one or more final diagnoses
	 */
	public static final int FINAL_DDX_YES = 0;
	
	/**
	 * The VP has no final diagnosis
	 */
	public static final int FINAL_DDX_NO = 1; //2 could be to continue with a working diagnosis ...

	private Timestamp creationDate;
	/**
	 * We need this for example for determining availability biases...
	 */
	private Timestamp lastAccessDate;
	/**
	 * the VP the patIllScript is related to. We need this in addition to the sessionId to be able to connect 
	 * the learners' script to the authors'/experts' script.
	 * @deprecated
	 */
	//private long parentId;
	/**
	 * unique across systems and vps. format: "vpId_systemId"
	 */
	private String vpId;
	private long userId; //needed so that we can display all the users' scripts to him/her
	/**
	 * Id of the PatientIllnessScript
	 */
	private long id = -1;
	/**
	 * language of the VP / script 
	 */
	private Locale locale; 
	private SummaryStatement summSt;
	private long summStId = -1;
	//private Note note;
	//private long noteId;
	/**
	 * If we get this information from the VP (or other) system, we store it here.
	 */
	private int currentStage;
	
	/**
	 * This is the real current stage and in contrast to currentStage this is not written into database and is updated on 
	 * each stage change. We need this for editing expert scripts...
	 */
	private int stage = 1;
	/**
	 * How confident is the learner with his ddxs. (1-100)
	 * @deprecated -> this needs to be stored with the final diagnoses / errors
	 */
	private int confidence;
	/**
	 * 1=acute, 2=subacute, 3=chronic
	 */	
	private int courseOfTime = -1;
	
	/**
	 * created by learner or expert
	 */
	private int type = IllnessScriptInterface.TYPE_LEARNER_CREATED;
	/**
	 * List of related problems to the PatientIllnessScript
	 */
	private List<RelationProblem> problems;
	private List<RelationDiagnosis> diagnoses; //contains all diagnoses, including the final(s)?
	private List<RelationManagement> mngs;
	private List<RelationTest> tests;
	private List<RelationPatho> patho;	
	private List<RelationNursingDiagnosis> nursingDiagnoses;	
	private List<RelationNursingAim> nursingAims;	
	private List<RelationInformation> infos;
	private List<RelationNursingManagement> nursingManagement;	
	private List<RelationMidwifeManagement> midwifeManagement;	
	private List<RelationMidwifeHypothesis> midwifeHypotheses;
	private List<RelationMidwifeRecommendation> midwifeRecommendations;
	private List<RelationMidwifeFinding> midwifeFindings;
	

	/**
	 * key = cnxId (Long), value = Connection object
	 */
	private Map<Long,Connection> conns;
	
	/**
	 * at which stage have the ddx of this script been submitted by the learner? this is important to detect certain errors
	 */
	private int submittedStage;
	
	/**
	 * Stage at which the learner really has to submit a diagnosis, ONLY expert scripts!
	 */
	private int maxSubmittedStage = -1;
	
	/**
	 * key=error id, value=error
	 * It is complicated to have lists in Maps in hibernate, therefore, we currently have a map with all errors and the unique 
	 * id as key...
	 */
	private List<MyError> errors;
	
	/**
	 * if the learner has chosen that the correct final diagnoses shall be revealed automatically we store this here 
	 * (including the stage when this happened)
	 */
	private int showSolution = -1;
	private String extUId="";
	
	/**
	 * true for old scripts if learner creates a new script for the same VP.
	 */
	private boolean deleteFlag = false;
	
	/**
	 * Has this script added to the peer table? ONLY learner scripts!
	 */
	private boolean peerSync = false;
	
	/**
	 * We store here all xAPI statements for this script
	 */
	private StatementContainer stmtContainer;
	
	/**
	 * does the VP have a final diagnosis (0) or not (1), -1 = not yet at this stage
	 */
	private int finalDDXType = -1;
	
	/**
	 * number of script worked on (1-based), excluding scripts that are created a second time (e.g. after a session reset), 
	 * so only first attempt counts and only if opened more than one card. This makes data analysis easier!
	 */
	private int orderNr = -1;
	/**
	 * number of script completed (1-based), excluding scripts that are created a second time (e.g. after a session reset), 
	 * so only first attempt counts and only if diagnosis has been submitted.This makes data analysis easier!
	 */	
	private int orderNrSubmitted = -1;
	private List<LogEntry> logentries;
	/**
	 * the CASUS session id
	 */
	private long sessionId;
	
	private int[] tsttypes = new int[]{1,2,3,4};
	//private int box1Type = 1; //default is fdgs, upper left corner box
	//private int box2Type = 2; //default is ddxs, upper right corner box
	//private int box3Type = 3; //default is tests, lower left corner box
	//private int box4Type = 4; //default is mngs, lower right corner box
	private int box1Mode = 1; //0=not display, 1=active, 2=passive
	private int box2Mode = 1; //0=not display, 1=active, 2=passive
	private int box3Mode = 1; //0=not display, 1=active, 2=passive
	private int box4Mode = 1; //0=not display, 1=active, 2=passive
	
	private int showAll = 0; //0 = only show until open card, 1 = show all nodes and cnxs (authoring only!)

	public PatientIllnessScript(){}
	public PatientIllnessScript(long userId, String vpId, Locale loc, int systemId){
		if(vpId==null) vpId = "";
		if(userId>0) this.userId = userId;
		this.locale = loc;
		if(!vpId.contains("_")) this.vpId = vpId.trim() +"_"+systemId;
		else this.vpId = vpId.trim();
		this.stmtContainer = new StatementContainer(userId, this.vpId, systemId);
	}
	
	/**
	 * Expert creates a script, so we init the basic parameters and make sure that the type is set correctly
	 */
	public void iniExpertScript(int maxStage, int maxddxstage){
		this.type = TYPE_EXPERT_CREATED;	
		this.currentStage = maxStage;
		this.maxSubmittedStage = maxddxstage;
	}
	
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}

	public int getStage() {
		updateStage(AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_STAGE));
		return stage;
	}
	
	public int getShowAll() { return showAll;}
	public void setShowAll(int showAll) {this.showAll = showAll;}
	
	public void toggleShowAll() {
		if(showAll == 0) showAll = 1;
		else showAll = 0;
	}
	public void setStage(int stage) {this.stage = stage;}
	public int getCourseOfTime() {return courseOfTime;}
	public void setCourseOfTime(int courseOfTime) {this.courseOfTime = courseOfTime;}
	public List<RelationProblem> getProblems() {
	//	if(!this.isExpScript())
			return problems;
	/*	else {
			if(showAll ==0)return problems;
			else return getProblemsStage();
		}*/
	}
	public List<RelationProblem> getProblemsStage() { return getRelationsByStage(problems);}
	public void setProblems(List<RelationProblem> problems) {this.problems = problems;}
	public Timestamp getCreationDate(){ return this.creationDate;} //setting is done in DB	
	public void setCreationDate(Timestamp creationDate) {this.creationDate = creationDate;}	
	public Timestamp getLastAccessDate() {return lastAccessDate;}
	public void setLastAccessDate(Timestamp lastAccessDate) {this.lastAccessDate = lastAccessDate;}

	public int getType() {return type;}
	public void setType(int type) {this.type = type;}	
	public boolean isExpScript(){
		if(type==TYPE_EXPERT_CREATED) return true;
		return false;
	}
	
	public boolean isDeleteFlag() {return deleteFlag;}
	public void setDeleteFlag(boolean deleteFlag) {this.deleteFlag = deleteFlag;}
	
	public List<RelationDiagnosis> getDiagnoses() {return diagnoses;}
	public List<RelationDiagnosis> getDiagnosesStage() { return getRelationsByStage(diagnoses);}
	public void setDiagnoses(List<RelationDiagnosis> diagnoses) {this.diagnoses = diagnoses;}
	
	public List<RelationNursingDiagnosis> getNursingDiagnoses() {return nursingDiagnoses;}
	public List<RelationNursingDiagnosis> getNursingDiagnosesStage() { return getRelationsByStage(nursingDiagnoses);}
	public void setNursingDiagnoses(List<RelationNursingDiagnosis> nursingDiagnoses) {this.nursingDiagnoses = nursingDiagnoses;}

	public List<RelationNursingAim> getNursingAims() {return nursingAims;}
	public List<RelationNursingAim> getNursingAimsStage() { return getRelationsByStage(nursingAims);}
	public void setNursingAims(List<RelationNursingAim> nursingAims) {this.nursingAims = nursingAims;}

	public List<RelationNursingManagement> getNursingManagement() {return nursingManagement;}
	public List<RelationNursingManagement> getNursingManagementStage() { return getRelationsByStage(nursingManagement);}
	public void setNursingManagement(List<RelationNursingManagement> nursingManagement) {this.nursingManagement = nursingManagement;}

	public List<RelationInformation> getInfos() {return infos;}
	public List<RelationInformation> getInformationStage() { return getRelationsByStage(infos);}
	public void setInformation(List<RelationInformation> infos) {this.infos = infos;}
	
	public List<RelationManagement> getMngs() {return mngs;}
	public List<RelationManagement> getMngsStage() { return getRelationsByStage(mngs);}
	public void setMngs(List<RelationManagement> mngs) {this.mngs = mngs;}	
	
	public List<RelationTest> getTests() {return tests;}
	public List<RelationTest> getTestsStage() { return getRelationsByStage(tests);}
	public void setTests(List<RelationTest> tests) {this.tests = tests;}	

	public List<RelationPatho> getPatho() {return patho;}
	public List<RelationPatho> getPathoStage() { return getRelationsByStage(patho);}
	public void setPatho(List<RelationPatho> patho) {this.patho = patho;}	
	
	public List<RelationMidwifeManagement> getMidwifeManagement() {return midwifeManagement;}
	public List<RelationMidwifeManagement> getMidwifeManagementStage() { return getRelationsByStage(midwifeManagement);}
	public void setMidwifeManagement(List<RelationMidwifeManagement> mngs) {this.midwifeManagement = mngs;}	

	public List<RelationMidwifeRecommendation> getMidwifeRecommendations() {return midwifeRecommendations;}
	public List<RelationManagement> getMidwifeRecommendationsStage() { return getRelationsByStage(midwifeRecommendations);}
	public void setMidwifeRecommendations(List<RelationMidwifeRecommendation> midwifeRecommendations) {this.midwifeRecommendations = midwifeRecommendations;}	

	public List<RelationMidwifeHypothesis> getMidwifeHypotheses() {return midwifeHypotheses;}
	public List<RelationMidwifeHypothesis> getMidwifeHypothesesStage() { return getRelationsByStage(midwifeHypotheses);}
	public void setMidwifeHypotheses(List<RelationMidwifeHypothesis> mh) {this.midwifeHypotheses = mh;}	
	
	public List<RelationMidwifeFinding> getMidwifeFindings() {return midwifeFindings;}
	public List<RelationMidwifeFinding> getMidwifeFindingsStage() { return getRelationsByStage(midwifeFindings);}
	public void setMidwifeFindings(List<RelationMidwifeFinding> mf) {this.midwifeFindings = mf;}	
	
	
	public Map<Long,Connection> getConns() {return conns;}
	public void setConns(Map<Long,Connection> conns) {this.conns = conns;}
	public long getUserId() {return userId;}
	public void setUserId(long userId) {this.userId = userId;}	
	public Locale getLocale() {return locale;}
	public void setLocale(Locale locale) {this.locale = locale;}	
	public SummaryStatement getSummSt() {return summSt;}
	public SummaryStatement getSummStStage() {
		if(summSt==null && summStId<=0) return null;
		if(summSt==null && summStId>0){ //for some reason summSt not yet loaded, so load it now:
			try{
				summSt = new DBClinReason().loadSummSt(summStId, null);
			}
			catch(Exception e){
				CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			}
		}
		if(summSt.getStage()<=stage) return summSt;
		return null;
	}	
	public String getExtUId() {return extUId;}
	public void setExtUId(String extUId) {
		this.extUId = extUId;
	}
	public void setSummSt(SummaryStatement summSt) {this.summSt = summSt;	}	
	public int getShowSolution() {return showSolution;}
	public boolean getIsShowSolution() {
		if(showSolution>0) return true; 
		else return false;}
	public void setShowSolution(int showSolution) {this.showSolution = showSolution;}
	public boolean isPeerSync() {return peerSync;}
	public boolean getPeerSync() {return peerSync;}
	public void setPeerSync(boolean peerSync) {this.peerSync = peerSync;}	
	public long getSessionId() {return sessionId;}
	public void setSessionId(long sessionId) {this.sessionId = sessionId;}
	public void setSessionId(String sessionId) {
		try {			
			String s = Encoder.getInstance().decodeQueryParam(extUId);
			this.sessionId = Long.valueOf(s).longValue();
		}
		catch(Exception e) {}
	}
	/**
	 * @deprecated
	 */
	public int getConfidence() {return confidence;}
	/**
	 * @deprecated
	 */
	public String getConfidenceRange() {
		if(showSolution>0) return ""; //do not display any previous confidence if learner has chosen to get the solution!
		if(confidence<25) return IntlConfiguration.getValue("submit.slider.after") + " " + IntlConfiguration.getValue("confidence.verylow") +".";
		if(confidence<50) return IntlConfiguration.getValue("submit.slider.after") + " " + IntlConfiguration.getValue("confidence.low") +".";
		if(confidence<75) return IntlConfiguration.getValue("submit.slider.after") + " " + IntlConfiguration.getValue("confidence.high") +".";
		if(confidence==100) return IntlConfiguration.getValue("submit.slider.after") + " " + IntlConfiguration.getValue("confidence.high") +"."; 
		else return IntlConfiguration.getValue("submit.slider.after") + " " + IntlConfiguration.getValue("confidence.highest") +".";
	}
	/**
	 * @deprecated
	 */
	public void setConfidence(int confidence) {this.confidence = confidence;}
	public long getSummStId() {return summStId;}
	public void setSummStId(long summStId) {this.summStId = summStId;}		
	public int getFinalDDXType() {return finalDDXType;}
	public void setFinalDDXType(int finalDDXType) {this.finalDDXType = finalDDXType;}
	public String getVpId() {return vpId;}		
	public void setVpId(String vpId) {this.vpId = vpId;}	
	public int getCurrentStage() {return currentStage;}
	
	public int getMaxSubmittedStage() {
		if(maxSubmittedStage>0) return maxSubmittedStage;
		return submittedStage; //default
	}
	public void setMaxSubmittedStage(int maxSubmittedStage) {this.maxSubmittedStage = maxSubmittedStage;}
	
	public int getCurrentStageWithUpdate() {
		updateStage(AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_STAGE));
		return currentStage;
	}	
	public void setCurrentStage(int currentStage) { 
		this.currentStage = currentStage;
		}		
	public List<MyError> getErrors() {return errors;}
	
	public int getOrderNr() {return orderNr;}
	public void setOrderNr(int orderNr) {this.orderNr = orderNr;}
	public int getOrderNrSubmitted() {return orderNrSubmitted;}
	public void setOrderNrSubmitted(int orderNrSubmitted) {this.orderNrSubmitted = orderNrSubmitted;}
	
	/**
	 * @return all Errors that have been made at the current stage
	 */
	public List<MyError> getErrorsCurrStage() {
		if(errors==null) return null;
		List<MyError> stageErr = new ArrayList<MyError>();
		for(int i=0; i<errors.size(); i++){
			if(errors.get(i).getStage()==this.currentStage) stageErr.add(errors.get(i));
		}
		if(stageErr==null || stageErr.isEmpty()) return null;
		return stageErr;
	}
	
	public void setErrors(List<MyError> errors) {this.errors = errors;}
	public void addErrors(List<MyError> list){
		if(list==null || list.isEmpty()) return;
		for(int i=0; i<list.size(); i++){
			addError(list.get(i));
		}
	}
	
	/**
	 * Creates the feedback string for the submitted diagnosis dialog. Feedback depends on the score and whether the learner
	 * has chosen to get the solution of the expert.
	 * @return
	 */
	public String getScoreFinalDDXFeedback(){		
		
		if(showSolution>0){ //solution was requested by learner 
			return IllnessScriptController.getInstance().getExpFinalsAsString(this.getVpId());
		}
		//solution was created by learner:
		ScoreBean scoreBean = ScoringController.getInstance().getOverallFinalDDXScore();
		List<ScoreBean> finalBeans = ScoringController.getInstance().getScoreBeansFortype(ScoreBean.TYPE_FINAL_DDX);
		if(this.getOverallFinalDDXScore() == ScoringController.FULL_SCORE) // 100% correct score by learner
			return IntlConfiguration.getValue("submit.corr");
		if(this.getOverallFinalDDXScore() >= ScoringController.scoreForAllowReSubmit){ //99-50% score
			StringBuffer sb = new StringBuffer();
			if(finalBeans!=null && finalBeans.size()==1) sb.append(IntlConfiguration.getValue("submit.partcorr")+ " ");
			else sb.append(IntlConfiguration.getValue("submit.partcorr") + " ");
			String expFinals = IllnessScriptController.getInstance().getExpFinalsAsString(this.getVpId());
			//append the solutions of the expert:
			if(expFinals!=null && !expFinals.trim().equals(""))
				sb.append(expFinals);
			return sb.toString();		
		}		
		return "";
	}
	
	/**
	 * Add an error, if for the stage and type no error has been occured.
	 * @param err
	 */
	public boolean addError(MyError err){
		if(err==null) return false;
		if(errors==null) errors = new ArrayList<MyError>();
		if(!errors.contains(err)){
			errors.add(err);
			return true;
		}
		return false;
	}	
	
	/**
	 * Looks whether in the error list stored for this map, there can be found an error of the given type
	 * @param error
	 * @return
	 */
	public boolean hasError(int error) {
		if(errors==null || errors.isEmpty()) return false;
		for(int i=0; i<errors.size();i++) {
			if(errors.get(i).getType()==error) return true;
		}
		return false;
	}
	public boolean getSubmitted() {
		if(submittedStage>0) return true;
		return false;
	}	
	public int getSubmittedStage() {return submittedStage;}
	

	public void setSubmittedStage(int submittedStage) {this.submittedStage = submittedStage;}
	//public void setSubmitted(boolean submitted) {this.submitted = submitted;}
	public void addProblem(String idStr, String prefix){new AddProblemAction(this).add(idStr, prefix);}
	public void addProblem(String idStr){new AddProblemAction(this).add(idStr,"");}
	public void addProblem(String idStr, String name, String x, String y){ new AddProblemAction(this).add(idStr, name, x,y);}
	public void addDiagnosis(String idStr, String name){ new AddDiagnosisAction(this).add(idStr, name);}
	public void addDiagnosis(String idStr, String name, String x, String y){ new AddDiagnosisAction(this).add(idStr, name, x,y);}
	public void addTest(String idStr, String name){ new AddTestAction(this).add(idStr, name);}
	public void addTest(String idStr, String name, String x, String y){ new AddTestAction(this).add(idStr, name, x,y);}
	public void addMng(String idStr, String name){ new AddMngAction(this).add(idStr, name);}
	public void addMng(String idStr, String name, String x, String y){ new AddMngAction(this).add(idStr, name, x,y);}
	public void addPatho(String idStr, String name){ new AddPathoAction(this).add(idStr, name);}	
	public void addPatho(String idStr, String name, String x, String y){ new AddPathoAction(this).add(idStr, name, x,y);}
	
	//nursing stuff:
	public void addNddx(String idStr, String name){ new AddNursingDiagnosisAction(this).add(idStr, name);}
	public void addNddx(String idStr, String name, String x, String y){ new AddNursingDiagnosisAction(this).add(idStr, name, x,y);}
	public void addInfo(String idStr, String name){ new AddInfoAction(this).add(idStr, name);}
	public void addInfo(String idStr, String name, String x, String y){ new AddInfoAction(this).add(idStr, name, x,y);}
	public void addNaim(String idStr, String name){ new AddNursingAimAction(this).add(idStr, name);}
	public void addNaim(String idStr, String name, String x, String y){ new AddNursingAimAction(this).add(idStr, name, x,y);}
	public void addNmng(String idStr, String name){new AddNursingMngAction(this).add(idStr, name);}
	public void addNmng(String idStr, String name, String x, String y){ new AddNursingMngAction(this).add(idStr, name, x,y);}

	//midwife stuff
	public void addMmng(String idStr, String name){new AddMidwifeMngAction(this).add(idStr, name);}
	public void addMmng(String idStr, String name, String x, String y){ new AddMidwifeMngAction(this).add(idStr, name, x,y);}
	public void addMfdg(String idStr, String name){new AddMidwifeFdgAction(this).add(idStr, name);}
	public void addMfdg(String idStr, String name, String x, String y){ new AddMidwifeFdgAction(this).add(idStr, name, x,y);}
	public void addMrec(String idStr, String name){new AddMidwifeRecAction(this).add(idStr, name);}
	public void addMrec(String idStr, String name, String x, String y){ new AddMidwifeRecAction(this).add(idStr, name, x,y);}
	public void addMhyp(String idStr, String name){new AddMidwifeHypAction(this).add(idStr, name);}
	public void addMhyp(String idStr, String name, String x, String y){ new AddMidwifeHypAction(this).add(idStr, name, x,y);}

	
	public void delProblem(String idStr){ new DelProblemAction(this).delete(idStr);}
	public void delDiagnosis(String idStr){ new DelDiagnosisAction(this).delete(idStr);}
	public void delTest(String idStr){ new DelTestAction(this).delete(idStr);}
	public void delMng(String idStr){ new DelMngAction(this).delete(idStr);}
	public void delPatho(String idStr){ new DelPathoAction(this).delete(idStr);}
	public void delInfo(String idStr){ new DelInfoAction(this).delete(idStr);}
	public void delNaim(String idStr){ new DelNursingAimAction(this).delete(idStr);}
	public void delNmng(String idStr){ new DelNursingMngAction(this).delete(idStr);}
	public void delNddx(String idStr){ new DelNursingDiagnosisAction(this).delete(idStr);}
	public void delMfdg(String idStr){ new DelMidwifeFdgAction(this).delete(idStr);}
	public void delMmng(String idStr){ new DelMidwifeMngAction(this).delete(idStr);}
	public void delMrec(String idStr){ new DelMidwifeRecAction(this).delete(idStr);}
	public void delMhyp(String idStr){ new DelMidwifeHypAction(this).delete(idStr);}

	//public void reorderProblems(String idStr, String newOrderStr){ new MoveProblemAction(this).reorder(idStr, newOrderStr);}
	//public void reorderDiagnoses(String idStr, String newOrderStr){ new MoveDiagnosisAction(this).reorder(idStr, newOrderStr);}
	//public void reorderTests(String idStr, String newOrderStr){ new MoveTestAction(this).reorder(idStr, newOrderStr);}
	//public void reorderMngs(String idStr, String newOrderStr){ new MoveMngAction(this).reorder(idStr, newOrderStr);}
	public void moveItem(String idStr, String newOrderStr, String x, String y){ new DragDropAction(this).move(idStr, x, y);}
	public void moveItem(String idStr, String x, String y){ new DragDropAction(this).move(idStr, x, y);}

	public void changeProblem(String idStr,String changeMode){new ChangeProblemAction(this).changeProblem(idStr, changeMode);}
	public void changeDiagnosis(String idStr,String changeMode){new ChangeDiagnosisAction(this).changeDiagnosis(idStr, changeMode);}
	public void changeTest(String idStr,String changeMode){new ChangeTestAction(this).changeTest(idStr, changeMode);}
	public void changeMng(String idStr,String changeMode){new ChangeMngAction(this).changeMng(idStr, changeMode);}
	public void changeMnM(String idStr/*, String newValue*/){new ChangeDiagnosisAction(this).toggleMnM(idStr/*, newValue*/);}
	public void changePatho(String idStr,String changeMode){new ChangePathoAction(this).changePatho(idStr, changeMode);}
	
	//public void addConnection(String sourceId, String targetId){new AddConnectionAction(this).add(sourceId,targetId);}
	public void addConnection(String sourceId, String targetId, String startEpId, String targetEpX, String targetEpY){new AddConnectionAction(this).add(sourceId,targetId,startEpId,targetEpX, targetEpY);}
	public void delConnection(String idStr){new DelConnectionAction(this).delete(idStr);}
	public void chgConnection(String idStr, String weightStr){new ChgConnectionAction(this).chgConnection(idStr, weightStr);}

	public void saveSummStatement(String idStr, String text){new SummaryStatementChgAction(this).updateOrCreateSummaryStatement( idStr, text);}
	//public void saveNote(String idStr, String text){new NoteChgAction(this).updateOrCreateNote( idStr, text);}
	public void submitDDX(String idStr){new DiagnosisSubmitAction(this).submitDDX(idStr);}
	public void submitDDXAndConf(String idStr, String confStr){new DiagnosisSubmitAction(this).submitDDXAndConf(idStr, confStr);}
	public void expSetFinalDiagnosis(String idStr){new DiagnosisSubmitAction(this).submitExpFinalDiagnosis(idStr);}
	public void expSetNoFinalDiagnosis(String idStr){new DiagnosisSubmitAction(this).submitExpNoFinalDiagnosis(idStr);}

	public void resetFinalDDX(String idStr){new DiagnosisSubmissionRevertAction(this).revertSubmission();}
	public void chgStateOfItem(String itemId, String newStage){ new ExpChgAction().chgStage(itemId, newStage);}
	public void chgStateOfEdge(String itemId, String newStage){ new ExpChgAction().chgEdgeStage(itemId, newStage);}
	public void chgFinalState(String itemId, String newStage){new ChgPatIllScriptAction(this).chgFinalStage(itemId, newStage);}
	//public void submitDDX(){new DiagnosisSubmitAction(this).submitDDX();}
	public void changeTier(String idStr, String tierStr){new DiagnosisSubmitAction(this).changeTier(idStr, tierStr);}
	public void changeConfidence(String idStr, String confVal){new ChgPatIllScriptAction(this).changeConfidence(idStr, confVal);}
	public void showSolution(String s){new DiagnosisSubmitAction(this).showSolution();}
	//public void chgCourseOfTime(String courseOfTimeStr) { new ChgPatIllScriptAction(this).chgCourseOfTime(courseOfTimeStr);}
	public void chgSummStCard(String s, String newStage) {
		new SummaryStatementChgAction(this).updateSummaryStatementStage(newStage);}
	/**
	 * author/expert wants to change the language of the (expert) map, we trigger this. If the map/script is not empty we try to
	 * translate already created items. 
	 * @param newLang
	 */
	public void changeLangOfScript(String newLang){
		if(newLang==null) return;
		if(this.getType() == IllnessScriptInterface.TYPE_LEARNER_CREATED) return; //no language change for learner maps!
				
		if(!this.getIsEmptyScript()){ //we try to translate the complete script and entered items:
			new ScriptTranslationController(this).translateScript(new Locale(newLang));
		}	
		this.setLocale(new Locale(newLang));
		this.save();
	}
	
	/** change of box types in authoring system
	 * we get for the four boxes a string like '6,1,2,3' (patho, fdg, ddx, tests)
	 *
	 **/
	public void changeBoxType(String newTypes) {
		try {
			//int type = Integer.valueOf(newType).intValue();
			if(newTypes==null || newTypes.trim().contentEquals("")) return; //an error happened... 
			int[] types = StringUtilities.getIntArrFromString(newTypes, ",");

			tsttypes = new int[]{0,0,0,0};
			int counter = 0;
			for(int i=0; i<types.length;i++) {
				if(types[i]>=1) {
					tsttypes[counter] = types[i]; //]this.box1Type = 1; 
					counter++;
				}
			}

			new DBClinReason().saveAndCommit(this);
		}
		catch(Exception e) {
			CRTLogger.out(StringUtilities.stackTraceToString(e),CRTLogger.LEVEL_ERROR);
		}
	}
	
	/*** end change of box types in authoring system *****/
	
	
	public void addJoker(String idStr, String type){ new JokerAction(this).addJoker(type);}
	public StatementContainer getStmtContainer() {
		if(stmtContainer==null) stmtContainer = new StatementContainer(userId, vpId, 2);
		return stmtContainer;}
	public void addTypeAheadBean(String type){
		new TypeAheadBean(type).save();
	}
	
	public void save(){
		User u = NavigationController.getInstance().getMyFacesContext().getUser();
		if(u==null) return; //do not save if we do not have a user! 
		
		//in expert maps the editing user can be different from the original, so only if we are in player we have to have matching users: 
		if(this.getType()!=PatientIllnessScript.TYPE_EXPERT_CREATED && u.getUserId()!=this.userId) return; 
		//if(!(this.getType()==PatientIllnessScript.TYPE_EXPERT_CREATED || u.getUserId()!=this.userId)) return; //so not save if the users do not match!
		boolean isNew = false;
		if(getId()<=0) isNew = true;
		setLastAccessDate(new Timestamp(System.currentTimeMillis()));
		new DBClinReason().saveAndCommit(this);
		if(isNew) notifyLog();
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		if(o!=null){
			if(o instanceof PatientIllnessScript && ((PatientIllnessScript)o).id ==this.id)
				return true;
		}
		return false;
	}

	
	public RelationProblem getProblemById(long id){return (RelationProblem) getRelationById(problems, id);}	
	public RelationDiagnosis getDiagnosisById(long id){return (RelationDiagnosis) getRelationById(diagnoses, id);}	
	public RelationTest getTestById(long id){return (RelationTest) getRelationById(tests, id);}		
	public RelationPatho getPathoById(long id){return (RelationPatho) getRelationById(patho, id);}		
	public RelationManagement getMngById(long id){return (RelationManagement) getRelationById(mngs, id);}	
	public RelationInformation getInfoById(long id){return (RelationInformation) getRelationById(infos, id);}	
	public RelationNursingDiagnosis getNursingDiagnosisById(long id){return (RelationNursingDiagnosis) getRelationById(nursingDiagnoses, id);}	
	public RelationNursingAim getNursingAimById(long id){return (RelationNursingAim) getRelationById(nursingAims, id);}	
	public RelationNursingManagement getNursingMngById(long id){return (RelationNursingManagement) getRelationById(nursingManagement, id);}	
	public RelationMidwifeManagement getMidwifeMngById(long id){return (RelationMidwifeManagement) getRelationById(midwifeManagement, id);}	
	public RelationMidwifeFinding getMidwifeFdgById(long id){return (RelationMidwifeFinding) getRelationById(midwifeFindings, id);}	
	public RelationMidwifeRecommendation getMidwifeRecById(long id){return (RelationMidwifeRecommendation) getRelationById(midwifeRecommendations, id);}	
	public RelationMidwifeHypothesis getMidwifeHypById(long id){return (RelationMidwifeHypothesis) getRelationById(midwifeHypotheses, id);}	


	public Relation getRelationByListItemIdAndType(long id, int type){
		if(type==Relation.TYPE_PROBLEM) return getRelationByListItemId(this.problems, id);
		if(type==Relation.TYPE_DDX) return getRelationByListItemId(this.diagnoses, id);
		if(type==Relation.TYPE_MNG) return getRelationByListItemId(this.mngs, id);
		if(type==Relation.TYPE_TEST) return getRelationByListItemId(this.tests, id);
		if(type==Relation.TYPE_PATHO) return getRelationByListItemId(this.patho, id);	
		if(type==Relation.TYPE_INFO) return getRelationByListItemId(this.infos, id);	
		if(type==Relation.TYPE_NDDX) return getRelationByListItemId(this.nursingDiagnoses, id);	
		if(type==Relation.TYPE_NMNG) return getRelationByListItemId(this.nursingManagement, id);	
		if(type==Relation.TYPE_NURSAIM) return getRelationByListItemId(this.nursingAims, id);	
		if(type==Relation.TYPE_MMNG) return getRelationByListItemId(this.midwifeManagement, id);	
		if(type==Relation.TYPE_MFDG) return getRelationByListItemId(this.midwifeFindings, id);	
		if(type==Relation.TYPE_MREC) return getRelationByListItemId(this.midwifeRecommendations, id);	
		if(type==Relation.TYPE_MHYP) return getRelationByListItemId(this.midwifeHypotheses, id);	

		return null;
	}
	
	private Relation getRelationByListItemId(List items, long listItemId){
		if(items==null || items.isEmpty()) return null;
		for(int i=0; i< items.size(); i++){
			Relation rel = (Relation) items.get(i);
			if(rel.getListItemId()==listItemId) return rel;
		}
		return null; //nothing found
	}
	
	private Relation getRelationById(List items, long itemId){
		if(items==null || items.isEmpty()) return null;
		for(int i=0; i< items.size(); i++){
			Relation rel = (Relation) items.get(i);
			if(rel.getId()==itemId) return rel;
		}
		return null; //nothing found
	}
	
	private List getRelationsByStage(List items){
		if(items==null || items.isEmpty()) return null;
		//when viewing indiv. maps in the reports and the complete map shall be displayed, we return all items here:
		if(NavigationController.getInstance().getMyFacesContext().isView() && AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_REPORTS_DISPLAYMODE, 0)==1)
			return items;

		if(this.isExpScript() && AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_REPORTS_DISPLAYMODE, 0)==1)
			return items;

		
		List<Relation> stageList = new ArrayList<Relation>();
		for(int i=0; i< items.size(); i++){
			Relation rel = (Relation) items.get(i);
			if(rel.getStage()<=stage) stageList.add(rel);
		}
		return stageList; //nothing found
	}
	
	/**
	 * return all final diagnoses entered by the learner. We do this in order to 
	 * display the final diagnoses in a different way (non-editable) than the other diagnoses.
	 * @return
	 */	
	public List<RelationDiagnosis> getFinalDiagnoses(){
		if(diagnoses==null || diagnoses.isEmpty()) return null; 
		List<RelationDiagnosis> finals = new ArrayList<RelationDiagnosis>();
		for(int i=0; i<diagnoses.size(); i++){
			if(diagnoses.get(i).isFinalDDX()) finals.add(diagnoses.get(i));
		}
		return finals;
	}
	
	public String getFinalDiagnosesToString(){
		List<RelationDiagnosis> l = getFinalDiagnoses();
		if(l==null) return "";
		StringBuffer sb = new StringBuffer(100);
		for (int i=0;i<l.size();i++){
			sb.append(l.get(i).getLabelOrSynLabel() + " ");
		}
		return sb.toString();
	}
	
	
	/**
	 * return all diagnoses entered by the learner that are NOT final diagnoses. We do this in order to 
	 * display the final diagnoses in a different way (non-editable).
	 * @return
	 */
	public List<RelationDiagnosis> getDiagnosesWithoutFinals(){
		if(diagnoses==null || diagnoses.isEmpty()) return null; 
		List<RelationDiagnosis> ddxs = new ArrayList<RelationDiagnosis>();
		for(int i=0; i<diagnoses.size(); i++){
			if(!diagnoses.get(i).isFinalDDX()) ddxs.add(diagnoses.get(i));
		}
		return ddxs;	
	}
	
		
	public Relation getRelationByIdAndType(long id, int type){
		if(type==Relation.TYPE_PROBLEM) return getRelationById(this.problems, id);
		if(type==Relation.TYPE_DDX) return getRelationById(this.diagnoses, id);
		if(type==Relation.TYPE_MNG) return getRelationById(this.mngs, id);
		if(type==Relation.TYPE_TEST) return getRelationById(this.tests, id);
		if(type==Relation.TYPE_PATHO) return getRelationById(this.patho, id);
		if(type==Relation.TYPE_NDDX) return getRelationById(this.nursingDiagnoses, id);
		if(type==Relation.TYPE_NMNG) return getRelationById(this.nursingManagement, id);
		if(type==Relation.TYPE_INFO) return getRelationById(this.infos, id);
		if(type==Relation.TYPE_NURSAIM) return getRelationById(this.nursingAims, id);
		if(type==Relation.TYPE_MMNG) return getRelationById(this.midwifeManagement, id);
		if(type==Relation.TYPE_MFDG) return getRelationById(this.midwifeFindings, id);
		if(type==Relation.TYPE_MHYP) return getRelationById(this.midwifeHypotheses, id);
		if(type==Relation.TYPE_MREC) return getRelationById(this.midwifeRecommendations, id);
	
		return null;
	}

	private void notifyLog(){
		LogEntry le = new LogEntry( LogEntry.CRTPATILLSCRIPT_ACTION, this.getId(), -1, this.getId());
		new DBClinReason().saveAndCommit(le);
	}
	
	/**
	 * We save the current stage the user is in in the Script. We do not change the current stage if the stage 
	 * is before the current stage (can happen e.g. if the user goes back in a VP)
	 * @param stage
	 */
	public void updateStage(String stage){
		try{
			if(StringUtils.isNumeric(stage)){
				int stageNum = Integer.valueOf(stage);
				//at the next stage we disable the display of the help dialog:
				if(stageNum==2 && !this.isExpScript()){
					if(NavigationController.getInstance().getMyFacesContext().getUser()!=null)
						NavigationController.getInstance().getMyFacesContext().getUser().getUserSetting().setOpenHelpOnLoad(false);
				}
				this.stage = stageNum; //we always update the stage
				//we cannot save if the user is an admin and views a learners script
				if(stageNum > this.currentStage && !NavigationController.getInstance().getMyFacesContext().isView()){
					//if user is on first card, do NOT calculate list scores:
					if(stageNum>=2 && !this.isExpScript()) new ScoringListAction(this).checkListScoresAtStage();
					this.setCurrentStage(stageNum);	
					save();
				}
			}
		}
		catch(Exception e) {
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
	
	public int getErrorNum(){
		if(errors==null) return 0;
		return errors.size();
	}
	
	public String getCourseOfTimeScore(){ 
		return new ScoringController().getIconForScore(ScoreBean.TYPE_COURSETIME, -1);
	}
	/**
	 * return the expert's summary statement at the current stage...
	 * @return
	 */
	public String getSummStExp(){
		if(this.type==IllnessScriptInterface.TYPE_LEARNER_CREATED){
			PatientIllnessScript expScript = AppBean.getExpertPatIllScript(this.vpId);
			if(expScript==null || expScript.getSummSt()==null) //no expert summary statement at all
					return IntlConfiguration.getValue("summst.exp.none");
			else if(expScript.getSummSt().getStage()>currentStage) //summary statement available at later stage
				return IntlConfiguration.getValue("summst.exp.no");
			return expScript.getSummSt().getText();
		}
		return "";
	}
	
	/**
	 * We offer the user to retry the diagnoses submission if he has less than 100%
	 * TODO: we might make this more specific
	 * @return
	 */
	public boolean getOfferTryAgain(){
		LearningAnalyticsBean laBean = NavigationController.getInstance().getCRTFacesContext().getLearningAnalytics();
		if(laBean==null || laBean.getScoreContainer()==null) return false;
		ScoreBean finalDDXScore = laBean.getScoreContainer().getScoreByType(ScoreBean.TYPE_FINAL_DDX_LIST);
		if(finalDDXScore!=null && finalDDXScore.getScoreBasedOnExp()==ScoringAction.NO_SCORING_POSSIBLE) return false;
		if(finalDDXScore==null || finalDDXScore.getScoreBasedOnExp()<ScoringController.scoreForAllowReSubmit) return true;

		return false;
	}
	
	/**
	 * Learner can continue the case 
	 * @return
	 */
	public boolean getOfferContinueCase(){
		if(AppBean.getExpertPatIllScript(this.getVpId())==null) return true; //we have no expert's script....
		if(getOfferTryAgain() && this.currentStage < AppBean.getExpertPatIllScript(this.getVpId()).getMaxSubmittedStage()) return true;
		return false; //end of case and/or 100% score
	}
	
	public boolean getOfferSolution(){
		if(!getOfferTryAgain()) return false; //solution was correct anyway
		if(AppBean.getExpertPatIllScript(this.getVpId())==null) return true; //we have no expert's script....
		if(this.currentStage >= AppBean.getExpertPatIllScript(this.getVpId()).getMaxSubmittedStage() /*&& this.currentStage >= AppBean.getExpertPatIllScript(this.getParentId()).getSubmittedStage()*/) return true;
		return false;
	}
	
	/**
	 * needed to decide which feedback to display after final diagnoses have been submitted -> either error dialog or
	 * a confirmation dialog for correct diagnosis...
	 * @return
	 */
	public float getOverallFinalDDXScore(){ 
		ScoreBean scoreBean = ScoringController.getInstance().getOverallFinalDDXScore();
		if(scoreBean==null) return -1;
		return scoreBean.getScoreBasedOnExp();
	}
	
	/**
	 * if we have a expert script we always use the actual stage on which the expert is to store for the action. For 
	 * learner scripts we use the currentStage (=maxStage), since he might just have jumped back...
	 * @return
	 */
	public int getStageForAction(){
		if(isExpScript()) return stage;
		return currentStage;
	}
	
	/**
	 * For displaying expert scripts in admin area we display name of VP to make selection easier
	 * @return
	 */
	public String getVPName(){return AppBean.getVPNameByVPId(this.vpId);}
	
	/**
	 * For displaying expert scripts in admin area we display the VP system to make selection easier
	 * @deprecated
	 * @return
	 */
	public String getVPSystem(){return AppBean.getVPSystemByVPId(this.vpId);
}
	
	public String getVpIdCrop() {		
		if(this.vpId==null || this.vpId.trim().equals("")) return "";
		if(!this.vpId.contains("_")) return vpId;
		else return vpId.substring(0, vpId.indexOf("_"));
	}
	
	public int compareTo(Object o) {
		if(o instanceof PatientIllnessScript){
			PatientIllnessScript pat = (PatientIllnessScript) o;
			if(this.getId() < pat.getId()) return -1;
			if(this.getId() > pat.getId()) return 1;
			if(this.getId() == pat.getId()) return 0;			
		}		
		return 0;
	}
	
	/**
	 * difference between problems entered by expert and student at current stage. If view mode is on we return 0.
	 * @return
	 */
	public int getProblemsDiff(){
		if(this.isExpScript()) return 0;
		CRTFacesContext crtContext = new NavigationController().getCRTFacesContext();
		//if we have view mode only, we do not change color of box:
		if(crtContext.getSessSetting()!=null && crtContext.getSessSetting().getBoxModeFdg()==2) return 0;
		return FeedbackController.getInstance().getItemsDiffExpForStage(currentStage, getProblems(), Relation.TYPE_PROBLEM);
	}

	/**
	 * difference between pathophysiology items entered by expert and student at current stage. If view mode is on we return 0.
	 * @return
	 */
	public int getPathoDiff(){
		if(this.isExpScript()) return 0;
		CRTFacesContext crtContext = new NavigationController().getCRTFacesContext();
		//if we have view mode only, we do not change color of box:
		if(crtContext.getSessSetting()!=null /*&& crtContext.getSessSetting().getBoxModePat()==2*/) return 0;
		return FeedbackController.getInstance().getItemsDiffExpForStage(currentStage, getPatho(), Relation.TYPE_PATHO);
	}
	/**
	 * difference between differentials entered by expert and student at current stage. If view mode is on we return 0.
	 * @return
	 */
	public int getDDXDiff(){
		if(this.isExpScript()) return 0;
		if(this.getSubmitted()) return 0; //if diagnosis has been made, we do not have to make the box red any longer...
		CRTFacesContext crtContext = new NavigationController().getCRTFacesContext();
		//if we have view mode only, we do not change color of box:
		if(crtContext.getSessSetting()!=null && crtContext.getSessSetting().getBoxModeDDX()==2) return 0;
		return FeedbackController.getInstance().getItemsDiffExpForStage(currentStage, getDiagnoses(), Relation.TYPE_DDX);
	}
	/**
	 * difference between tests entered by expert and student at current stage. If view mode is on we return 0.
	 * @return
	 */
	public int getTestsDiff(){
		if(this.isExpScript()) return 0;
		CRTFacesContext crtContext = new NavigationController().getCRTFacesContext();
		//if we have view mode only, we do not change color of box:
		if(crtContext.getSessSetting()!=null && crtContext.getSessSetting().getBoxModeTst()==2) return 0;
		return FeedbackController.getInstance().getItemsDiffExpForStage(currentStage, getTests(), Relation.TYPE_TEST);
	}
	/**
	 * difference between management options entered by expert and student at current stage. If view mode is on we return 0.
	 * @return
	 */
	public int getMngsDiff(){
		if(this.isExpScript()) return 0;
		CRTFacesContext crtContext = new NavigationController().getCRTFacesContext();
		//if we have view mode only, we do not change color of box:
		if(crtContext.getSessSetting()!=null && crtContext.getSessSetting().getBoxModeMng()==2) return 0;

		return FeedbackController.getInstance().getItemsDiffExpForStage(currentStage, getMngs(), Relation.TYPE_MNG);
	}
	
	public int getSumDiff(){
		if(this.getSummStId()>0) return 0;
		return FeedbackController.getInstance().getSumStDiffForStage(currentStage, vpId);
	}
	
	public int getNumFinalDiagnosisAttempts() {return new DBLog().getNumOfFinalDiagnosisAttempts(this.getId());}
	
	/**
	 * Exports all map related data into Excel sheets (called from reports area)
	 */
	public void getMapAsExcel() {new ExportController().createAndWriteBasicTable(this);	}
	
	/**
	 * 
	 * @return true is no items have been added (no fdgs, ddxs, tests, or mng items), otherwise false.
	 */
	public boolean getIsEmptyScript(){
		if(this.getProblems()!=null && !this.getProblems().isEmpty()) return false; 
		if(this.getDiagnoses()!=null && !this.getDiagnoses().isEmpty()) return false; 
		if(this.getTests()!=null && !this.getTests().isEmpty()) return false; 
		if(this.getMngs()!=null && !this.getMngs().isEmpty()) return false; 
		if(this.getNursingAims()!=null && !this.getNursingAims().isEmpty()) return false; 
		if(this.getNursingDiagnoses()!=null && !this.getNursingDiagnoses().isEmpty()) return false; 
		if(this.getInfos()!=null && !this.getInfos().isEmpty()) return false; 
		if(this.getNursingManagement()!=null && !this.getNursingManagement().isEmpty()) return false; 
		if(this.getPatho()!=null && !this.getPatho().isEmpty()) return false;
		if(this.getMidwifeFindings()!=null && !this.getMidwifeFindings().isEmpty()) return false;
		if(this.getMidwifeHypotheses()!=null && !this.getMidwifeHypotheses().isEmpty()) return false;
		if(this.getMidwifeRecommendations()!=null && !this.getMidwifeRecommendations().isEmpty()) return false;
		if(this.getMidwifeManagement()!=null && !this.getMidwifeManagement().isEmpty()) return false;

		return true;
	}
	//used for admin purposes only (display in admin area for individual maps)
	public List<LogEntry> getLogEntries(){return logentries;}
	public void setLogEntries(List<LogEntry> le ){this.logentries = le;}

	public int getBox1Type() {
		return this.tsttypes[0];}
	public void setBox1Type(int box1Type) {
		this.tsttypes[0] = box1Type;}
	public int getBox2Type() {return tsttypes[1];}
	public void setBox2Type(int box2Type) {this.tsttypes[1] = box2Type;}
	public int getBox3Type() {return tsttypes[2];}
	public void setBox3Type(int box3Type) {this.tsttypes[2] = box3Type;}
	public int getBox4Type() {return tsttypes[3];}
	public void setBox4Type(int box4Type) {this.tsttypes[3] = box4Type;}	
	
	public int getBox1Mode() {return box1Mode;}
	public void setBox1Mode(int box1Mode) {this.box1Mode = box1Mode;}
	public int getBox2Mode() {return box2Mode;}
	public void setBox2Mode(int box2Mode) {this.box2Mode = box2Mode;}
	public int getBox3Mode() {return box3Mode;}
	public void setBox3Mode(int box3Mode) {this.box3Mode = box3Mode;}
	public int getBox4Mode() {return box4Mode;}
	public void setBox4Mode(int box4Mode) {this.box4Mode = box4Mode;}
	
	public int getPatBoxNo() { return getBoxByType(Relation.TYPE_PATHO);}
	public int getFdgBoxNo() { return getBoxByType(Relation.TYPE_PROBLEM);}
	public int getDdxBoxNo() { return getBoxByType(Relation.TYPE_DDX);}
	public int getTstBoxNo() { return getBoxByType(Relation.TYPE_TEST);}
	public int getMngBoxNo() { return getBoxByType(Relation.TYPE_MNG);}
	public int getNMngBoxNo() { return getBoxByType(Relation.TYPE_NMNG);}
	public int getNDdxBoxNo() { return getBoxByType(Relation.TYPE_NDDX);}
	public int getNAimBoxNo() { return getBoxByType(Relation.TYPE_NURSAIM);}
	public int getInfoBoxNo() { return getBoxByType(Relation.TYPE_INFO);}
	public int getMMngBoxNo() { return getBoxByType(Relation.TYPE_MMNG);}
	public int getMFdgBoxNo() { return getBoxByType(Relation.TYPE_MFDG);}
	public int getMRecBoxNo() { return getBoxByType(Relation.TYPE_MREC);}
	public int getMHypBoxNo() { return getBoxByType(Relation.TYPE_MHYP);}
	/**
	 * we return the box that contains the given type (fgd, ddx,...) or 
	 * 0 if should not be displayed. 
	 * @param type
	 * @return
	 */
	private int getBoxByType(int type) {
		for(int i=0;i< tsttypes.length;i++) {
			if(tsttypes[i] == type ) return i+1;
		}
		return 0;
	}
}
