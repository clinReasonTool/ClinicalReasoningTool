package actions.beanActions;


import java.awt.Point;

import beans.LogEntry;
import beans.graph.Box;
import beans.scripts.*;
import beans.relation.Rectangle;
import beans.relation.Relation;
import controller.GraphController;
import database.DBClinReason;
import net.casus.util.Utility;
import util.CRTLogger;

/**
 * An item has been moved in the map view (drag&drop). so, we store the new position in the map. 
 * @author ingahege
 *
 */
public class DragDropAction {
	private PatientIllnessScript patIllScript;
	private Box box;
	
	public DragDropAction(PatientIllnessScript patIllScript, Box box){
		this.patIllScript = patIllScript;
		this.box = box;
	}
	
	public void move(String idStr, String xStr, String yStr){
		try {
		if(idStr==null) return;
		//String cutStr = idStr.substring(0,idStr.indexOf("_")+1);
		int type= box.getBoxType();
		/*int type = GraphController.getTypeByPrefix(cutStr);
		if(type==0)	//old mechanism, not flexible enough if prefix is longer (just kept here for safety reasons)
			type = GraphController.getTypeByPrefix(idStr.substring(0,4)); //6 for tabs & map*/
		//String idStr2 = idStr.substring(idStr.indexOf("_")+1);
		long id = Long.parseLong(idStr.substring(idStr.indexOf("_")+1));
		Relation rel = patIllScript.getRelationByIdAndType(id, type);
		if(rel!=null){
			float x = Float.valueOf(xStr.trim());
			float y = Float.valueOf(yStr.trim()) - Rectangle.addToY;
			rel.setXAndY(new Point((int) x, (int) y));
		}
		save(rel);
		notifyLog(rel);
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
	
	private void save(Relation rel){
		new DBClinReason().saveAndCommit(rel);
	}
	
	/* (non-Javadoc)
	 * @see beanActions.AddAction#notifyLog(beans.relation.Relation)
	 */
	public void notifyLog(Relation relProb){
		LogEntry le = new LogEntry(LogEntry.DRAGDROP_ACTION, patIllScript.getId(), relProb.getListItemId(), relProb.getOrder());
		le.save();
	}

}
