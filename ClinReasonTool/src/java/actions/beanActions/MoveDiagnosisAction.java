package actions.beanActions;

import java.beans.Beans;
import java.util.*;

import beans.*;
import beans.scripts.*;
import beans.relation.*;
import database.DBClinReason;

/**
 * @author ingahege
 * @deprecated
 */
/*public class MoveDiagnosisAction implements MoveAction{

	private PatientIllnessScript patIllScript;
	
	public MoveDiagnosisAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	

	public void save(List l){
		new DBClinReason().saveAndCommit(l);
	}
	

	public void notifyLog(Relation rel) {
		LogEntry le = new LogEntry(LogEntry.MOVEDIAGNOSIS_ACTION, patIllScript.getId(), rel.getListItemId(), rel.getOrder());
		le.save();			
	}


	public void reorder(String idStrMovedItem,  String newOrderStr) {		
		String[] newOrderArr = newOrderStr.split("&");
		List<RelationDiagnosis> newList  = new ArrayList<RelationDiagnosis>();
		if(newOrderArr==null || newOrderArr.length==0) return;
		idStrMovedItem = idStrMovedItem.substring(7);
		for(int i=0; i<newOrderArr.length; i++){
			String idStr = newOrderArr[i];
			idStr = idStr.substring(7);	
			
			RelationDiagnosis rel = this.patIllScript.getDiagnosisById(Long.parseLong(idStr));
			if(rel!=null && idStrMovedItem.equals(idStr)) notifyLog(rel);
			rel.setOrder(i);
			newList.add(rel);
		}
		patIllScript.setDiagnoses(newList);
		save(patIllScript.getDiagnoses());
	
}}*/
