package actions.beanActions;

import java.beans.Beans;

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
public class ChangePathoAction /*extends ChgAction*/{

	private PatientIllnessScript patIllScript;
	
	public ChangePathoAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	public void changePatho(String oldPathoIdStr, String changeModeStr){
		long oldPathoId = Long.valueOf(oldPathoIdStr.trim());
		int changeMode = Integer.valueOf(changeModeStr.trim());
		//if(changeMode==2 || changeMode==3) changeItem(oldPathoId, patIllScript, Relation.TYPE_PATHO); //synonyma or hierarchy item
	}
	

	public void changeRelation(MultiVertex expVertex, Relation rel){
		RelationPatho relToChg = (RelationPatho) rel;
		notifyLog(relToChg, expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setPatho(expVertex.getExpertVertex().getListItem());
		relToChg.setListItemId(expVertex.getExpertVertex().getListItem().getItem_id());
		relToChg.setSynId(-1);	
		save(relToChg);		
	}

	
	public void notifyLog(Beans pathoToChg, long newPathoId){
		LogEntry le = new LogEntry(LogEntry.CHGTEST_ACTION, patIllScript.getId(), ((Relation)pathoToChg).getListItemId(), newPathoId);
		le.save();
	}
	
	public void save(Beans rel){
		new DBClinReason().saveAndCommit(rel);
	}

	/*@Override
	public void triggerScoringAction(Beans beanToScore, boolean isJoker) {
		// TODO Auto-generated method stub
		
	}*/
}

