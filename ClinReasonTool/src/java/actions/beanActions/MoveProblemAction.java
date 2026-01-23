package actions.beanActions;

import java.beans.Beans;
import java.util.*;

import beans.*;
import beans.relation.*;
import database.DBClinReason;
import beans.scripts.*;
/**
 * @author ingahege
 * @deprecated
 */
/*public class MoveProblemAction implements MoveAction{

	private PatientIllnessScript patIllScript;
	
	public MoveProblemAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	

	public void save(List l){
		new DBClinReason().saveAndCommit(l);
	}
	

	public void notifyLog(Relation relBeforeReSort) {
		LogEntry le = new LogEntry(LogEntry.MOVEPROBLEM_ACTION, patIllScript.getId(), relBeforeReSort.getListItemId(), relBeforeReSort.getOrder());
		le.save();			
	}


	public void reorder(String idStrMovedItem, String newOrderStr) {		
		String[] newOrderArr = newOrderStr.split("&");
		List<RelationProblem> newList  = new ArrayList<RelationProblem>();
		if(newOrderArr==null || newOrderArr.length==0) return;
		idStrMovedItem = idStrMovedItem.substring(8);
		for(int i=0; i<newOrderArr.length; i++){
			String idStr = newOrderArr[i];
			idStr = idStr.substring(8);				
			RelationProblem relProb = this.patIllScript.getProblemById(Long.parseLong(idStr));
			if(relProb!=null && idStrMovedItem.equals(idStr)) notifyLog(relProb);
			relProb.setOrder(i);
			newList.add(relProb);
		}
		patIllScript.setProblems(newList);
		save(patIllScript.getProblems());
		//now we change the order attribute of each Relationproblem object:
		//new ActionHelper().reOrderItems(this.patIllScript.getProblems());
	}
}*/
