package actions.beanActions;

import java.beans.Beans;
import java.util.*;

import beans.*;
import beans.scripts.*;
import beans.relation.RelationManagement;
import beans.relation.Relation;
import database.DBClinReason;
/**
 * @author ingahege
 * @deprecated
 */
/*public class MoveMngAction implements MoveAction{

	private PatientIllnessScript patIllScript;
	
	public MoveMngAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	

	public void save(List l){
		new DBClinReason().saveAndCommit(l);
	}
	

	public void notifyLog(Relation relBeforeReSort) {
		LogEntry le = new LogEntry(LogEntry.MOVEMNG_ACTION, patIllScript.getId(), relBeforeReSort.getListItemId(), relBeforeReSort.getOrder());
		le.save();			
	}


	public void reorder(String idStrMovedItem, String newOrderStr) {		
		String[] newOrderArr = newOrderStr.split("&");
		List<RelationManagement> newList  = new ArrayList<RelationManagement>();
		if(newOrderArr==null || newOrderArr.length==0) return;
		idStrMovedItem = idStrMovedItem.substring(7);
		for(int i=0; i<newOrderArr.length; i++){
			String idStr = newOrderArr[i];
			idStr = idStr.substring(7);	
			
			RelationManagement rel = this.patIllScript.getMngById(Long.parseLong(idStr));
			if(rel!=null && idStrMovedItem.equals(idStr)) notifyLog(rel);
			rel.setOrder(i);
			newList.add(rel);
		}
		patIllScript.setMngs(newList);
		save(patIllScript.getMngs());
		//now we change the order attribute of each Relationproblem object:
		//new ActionHelper().reOrderItems(this.patIllScript.getProblems());
	}
}*/
