package actions.beanActions;

import java.util.*;

import actions.scoringActions.ScoringCourseOfTimeAction;
import beans.LogEntry;
import beans.relation.RelationDiagnosis;
import beans.scripts.*;
import database.DBClinReason;

/**
 * Changes to properties of the patientIllnessScript, currently courseOfTime and confidence.
 * @author ingahege
 *
 */
public class ChgPatIllScriptAction {

	private PatientIllnessScript patIllScript;
	
	public ChgPatIllScriptAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/**
	 * Learner changes the level of confidence with his/her ddxs.
	 * @param idStr
	 * @param confVal
	 */
	public void changeConfidence(String idStr, String confVal){changeConfidence(confVal);}
	
	/**
	 * Learner changes the level of confidence with his/her ddxs.
	 * @param idStr
	 * @param confVal
	 */
	public void changeConfidence(String confVal){
		notifyLog(LogEntry.CHG_CONFIDENCE_ACTION, patIllScript.getConfidence());
		int newConfidence = Integer.parseInt(confVal);
		if(patIllScript.getConfidence()==newConfidence) return; //no change, we do not have to do anything...
		
		patIllScript.setConfidence(newConfidence);
		patIllScript.save();
	}
	
	/**
	 * We change the stage at which the final diagnosis shall be made.
	 * @param itemId
	 * @param stageStr
	 */
	public void chgFinalStage(String itemId, String stageStr){
		int newstage = Integer.valueOf(stageStr);
		long diagnosisId = Long.valueOf(itemId);
		patIllScript.setSubmittedStage(newstage);
		patIllScript.setMaxSubmittedStage(newstage);
		List<RelationDiagnosis> finals = patIllScript.getFinalDiagnoses();
		if(finals!=null && !finals.isEmpty()){
			for(int i=0; i<finals.size(); i++){
				RelationDiagnosis rd = finals.get(i);
				if(rd!=null && rd.getListItemId()==diagnosisId){
					rd.setFinalDiagnosis(newstage);
					new DBClinReason().saveAndCommit(rd);
					new DBClinReason().saveAndCommit(patIllScript);
					break;
				}
			}
		}
	}
	/**
	 * Learner changes the courseOfTime (acute, subactue, chronic)
	 * @param courseOfTimeStr
	 */
	public void chgCourseOfTime(String courseOfTimeStr) { 
		patIllScript.setCourseOfTime(Integer.parseInt(courseOfTimeStr));
		new ScoringCourseOfTimeAction().scoreAction(patIllScript);
		notifyLog(LogEntry.CHGCOURSETIME_ACTION, patIllScript.getCourseOfTime());
		patIllScript.save();
	}
	
	private void notifyLog(int action, int newVal){
		LogEntry le = new LogEntry(action, patIllScript.getId(), newVal);
		le.save();
	}
}
