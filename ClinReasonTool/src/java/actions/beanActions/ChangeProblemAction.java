package actions.beanActions;

import java.beans.Beans;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import beans.LogEntry;
import beans.scripts.*;

import beans.graph.MultiVertex;
import beans.relation.*;

import database.DBClinReason;


/**
 * A ProblemRelation is changed and a different Problem object attached, id remains the same, so, no
 * other changes necessary.
 * @author ingahege
 * @deprecated
 */
public class ChangeProblemAction /*extends ChgAction*/{

	private PatientIllnessScript patIllScript;
	
	public ChangeProblemAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	/**
	 * Called from map, where we could allow to change the item to any other item, currently not enabled.
	 * @param oldProbIdStr
	 * @param newProbIdStr
	 */
	public void changeProblem(String oldProbIdStr, String changeModeStr){		
		long oldProbId = Long.valueOf(oldProbIdStr.trim());
		int changeMode = Integer.valueOf(changeModeStr.trim());
		//if(changeMode==1) toggleProblem(oldProbId); //change prefix
		//if(changeMode==2 || changeMode==3) 
			//changeItem(oldProbId, patIllScript, Relation.TYPE_PROBLEM); //changeProblem(oldProbId); //synonyma or hierarchy item
		//changeProblem(oldProbId, newProbId);
	}
	
	/*public void toggleProblem(String relIdStr){
		if(!StringUtils.isAlphanumeric(relIdStr)) return;
		toggleProblem(Long.valueOf(relIdStr).longValue());
	}*/
		
	/**
	 * We toggle the prefix (No ... vs ...)
	 * @param relIdStr
	 */
	/*private void toggleProblem(long relId){		
		RelationProblem probToChg = patIllScript.getProblemById(relId);
		probToChg.togglePrefix();
		save(probToChg);
		notifyLogTogglePrefix(probToChg);
		//TODO re-score:
		//ScoreBean score = new ScoringController().getScoreBeanForItem(Relation.TYPE_PROBLEM, probToChg.getListItemId());
		new ScoringAddAction(true).scoreAction(probToChg.getListItemId(), patIllScript, false, Relation.TYPE_PROBLEM);
	}*/
	
	
	public void changeRelation(MultiVertex expVertex, Relation probToChg){
		RelationProblem prob = (RelationProblem) probToChg;
		notifyLog(prob, expVertex.getExpertVertex().getListItem().getItem_id());
		prob.setProblem(expVertex.getExpertVertex().getListItem());
		prob.setListItemId(expVertex.getExpertVertex().getListItem().getItem_id());
		prob.setSynId(-1);	
		save(prob);		
	}
	
	
	/*private void changeProblem(long newProbId, long probRel){
		RelationProblem probToChg = patIllScript.getProblemById(probRel);
		ListItem oldProblem = new DBList().selectListItemById(probToChg.getListItemId());
		ListItem newProblem = new DBList().selectListItemById(newProbId);
		if(probToChg!=null && newProblem!=null && oldProblem!=null){
			notifyLog(probToChg, newProbId);
			probToChg.setProblem(newProblem);
			probToChg.setListItemId(newProblem.getItem_id());
			save(probToChg);		
		}
		//else -> error...
	}*/
	
	public void notifyLog(Beans probToChg, long newProbId){
		LogEntry le = new LogEntry(LogEntry.CHGPROBLEM_ACTION, patIllScript.getId(), ((Relation)probToChg).getListItemId(), newProbId);
		le.save();
	}
	
	/*private void notifyLogTogglePrefix(Relation probToChg){
		LogEntry le = new LogEntry(LogEntry.TOGGLE_PREFIX_ACTION, patIllScript.getId(), probToChg.getListItemId(), probToChg.getPrefix());
		le.save();
	}*/
	
	public void save(Beans rel){
		new DBClinReason().saveAndCommit(rel);
	}

	/*public void triggerScoringAction(Beans beanToScore, boolean isJoker) {
		// TODO Auto-generated method stub
		
	}*/
}
