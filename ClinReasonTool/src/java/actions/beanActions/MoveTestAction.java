package actions.beanActions;

import java.beans.Beans;
import java.util.*;

import beans.*;
import beans.scripts.*;
import beans.relation.RelationTest;
import beans.relation.Relation;
import database.DBClinReason;
/**
 * @author ingahege
 * @deprecated
 */
/*public class MoveTestAction implements MoveAction{

	private PatientIllnessScript patIllScript;
	
	public MoveTestAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}

	public void save(List l){
		new DBClinReason().saveAndCommit(l);
	}
	

	public void notifyLog(Relation relBeforeReSort) {
		LogEntry le = new LogEntry(LogEntry.MOVETEST_ACTION, patIllScript.getId(), relBeforeReSort.getListItemId(), relBeforeReSort.getOrder());
		le.save();			
	}

	public void reorder(String idStrMovedItem, String newOrderStr) {		
		String[] newOrderArr = newOrderStr.split("&");
		List<RelationTest> newList  = new ArrayList<RelationTest>();
		if(newOrderArr==null || newOrderArr.length==0) return;
		idStrMovedItem = idStrMovedItem.substring(6);
		for(int i=0; i<newOrderArr.length; i++){
			String idStr = newOrderArr[i];
			idStr = idStr.substring(6);	
			
			RelationTest rel = this.patIllScript.getTestById(Long.parseLong(idStr));
			if(rel!=null && idStrMovedItem.equals(idStr)) notifyLog(rel);
			rel.setOrder(i);
			newList.add(rel);
		}
		patIllScript.setTests(newList);
		save(patIllScript.getTests());
		//now we change the order attribute of each Relationproblem object:
		//new ActionHelper().reOrderItems(this.patIllScript.getProblems());
	}
}*/
