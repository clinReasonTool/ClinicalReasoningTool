package beans;

import java.beans.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import beans.list.ListItem;
import controller.NavigationController;
import database.DBClinReason;
import database.DBList;

/**
 * We save all the actions in a log table. 
 * TODO: we need a second variable for some actions, such as addCnx, move
 * @author ingahege
 *
 */
public class LogEntry extends Beans implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int ADDPROBLEM_ACTION = 1;
	public static final int ADDTEST_ACTION = 17;
	public static final int ADDMNG_ACTION = 18;
	public static final int ADDDIAGNOSIS_ACTION = 5;
	public static final int ADDEPI_ACTION = 29;
	public static final int ADDCONNECTION_ACTION = 8; 
	public static final int ADDPATHO_ACTION = 46;
	public static final int ADDAIM_ACTION = 70;

	public static final int DELPROBLEM_ACTION = 2;
	public static final int DELDIAGNOSIS_ACTION = 6;
	public static final int DELMNG_ACTION = 15;
	public static final int DELTEST_ACTION = 16;
	public static final int DELCONNECTION_ACTION = 9; 
	public static final int DELEPI_ACTION = 30;
	public static final int DELPATHO_ACTION = 47;
	public static final int DELCNXAFTERSTARTNODE_ACTION = 11; //a connection is deleted because the related start point has been deleted
	public static final int DELCNXAFTERTARGETNODE_ACTION = 12; //a connection is deleted because the related target point has been deleted
	
	public static final int CHGPROBLEM_ACTION = 10; 
	public static final int CHGDDX_ACTION = 13; 
	public static final int CHGDDXMNM_ACTION = 14; //change MnM flag
	public static final int CHGDDXTIER_ACTION = 35; //change tier flag

	public static final int CHGCOURSETIME_ACTION = 3; 
	public static final int CHGTEST_ACTION = 21; 
	public static final int CHGMNG_ACTION = 22; 
	public static final int CHGPATHO_ACTION = 31;
	public static final int CHGCNXWEIGHT_ACTION = 33;
	
	public static final int MOVEPROBLEM_ACTION = 4; 	
	public static final int MOVETEST_ACTION = 19; 
	public static final int MOVEMNG_ACTION = 20; 
	public static final int MOVEDIAGNOSIS_ACTION = 7; 
	public static final int DRAGDROP_ACTION = 34;
	public static final int MOVEPATHO_ACTION = 48; 

	
	public static final int CRTPATILLSCRIPT_ACTION = 23;
	public static final int CLOSEPATILLSCRIPT_ACTION = 24;
	
	public static final int CREATESUMMST_ACTION = 25;
	public static final int UPDATESUMMST_ACTION = 26;
	public static final int CREATENOTE_ACTION = 27;
	public static final int UPDATENOTE_ACTION = 28;
	public static final int SUBMITDDX_ACTION = 32;
	
	public static final int FEEDBACK_ON_ACTION = 36;
	public static final int FEEDBACK_OFF_ACTION = 37;
	public static final int CHG_CONFIDENCE_ACTION = 38;
	public static final int CHG_COURSEOFTIME_ACTION = 39;
	public static final int TOGGLE_PREFIX_ACTION = 40;
	public static final int PEERFEEDBACK_ON_ACTION = 41;
	public static final int PEERFEEDBACK_OFF_ACTION = 42;
	public static final int ERROR_ACTION = 43; //an error (e.g. Premature Closure) has happened.
	public static final int SHOWSOL_ACTION = 44;
	public static final int CLICK_MOVIE_ACTION = 45;
	public static final int ADD_ACTOR_ACTION = 49;
	public static final int DEL_ACTOR_ACTION = 50;
	public static final int ADD_CONTEXT_ACTION = 51;
	public static final int DEL_CONTEXT_ACTION = 52;
	public static final int ADDNMNG_ACTION = 53;
	public static final int ADDINFO_ACTION = 54;
	public static final int ADDNDDX_ACTION = 55;
	public static final int ADDNAIM_ACTION = 56;
	public static final int DELNMNG_ACTION = 57;
	public static final int DELINFO_ACTION = 58;
	public static final int DELNDDX_ACTION = 59;
	public static final int DELNAIM_ACTION = 60;	
	public static final int ADDMFDG_ACTION = 61;
	public static final int ADDMHYP_ACTION = 62;
	public static final int ADDMREC_ACTION = 63;
	public static final int ADDMMNG_ACTION = 64;
	public static final int DELMFDG_ACTION = 65;
	public static final int DELMHYP_ACTION = 66;
	public static final int DELMREC_ACTION = 67;
	public static final int DELMMNG_ACTION = 68;	

	/**
	 * action triggered by user, e.g. add a problem,... (see static definitions above)
	 */
	private int action;
	/**
	 * Id of the log entry (internal purposes)
	 */
	private long id;
	private Timestamp creationDate; //automatically set in the database
	private long patIllscriptId; 
	private long sessionId;
	//private long patIllScriptId; //necessary? We have the sessionId, but might be quicker to access.
	/**
	 * E.g. Id of the problem or diagnosis added/removed,... new value for courseOfTime
	 */
	private long sourceId;
	/**
	 * e.g. orderNr for problem/diagnosis or targetId for Cnx 
	 */
	private long sourceId2;
	
	private int stage;
	/**
	 * we can store text elements here, for example the summary statements at different stages...
	 */
	private String sourceText;
	
	public LogEntry(){}
	public LogEntry(int action, long patIllscriptId, long sourceId){
		this.action = action; 
		this.patIllscriptId = patIllscriptId;
		this.sourceId = sourceId;
	}
	
	public LogEntry(int action, long patIllscriptId, long sourceId, long sourceId2){
		this.action = action; 
		this.patIllscriptId = patIllscriptId;
		this.sourceId = sourceId;
		this.sourceId2 = sourceId2;
	}
	
	public int getAction() {return action;}
	public void setAction(int action) {this.action = action;}
	public Timestamp getCreationDate() {return creationDate;}
	public void setCreationDate(Timestamp creationDate) {this.creationDate = creationDate;}
	public long getSourceId() {return sourceId;}
	public void setSourceId(long sourceId) {this.sourceId = sourceId;}
	//public long getOldValue() {return oldValue;}
	//public void setOldValue(long oldValue) {this.oldValue = oldValue;}	
	
	public long getId() {return id;}
	public String getSourceText() {return sourceText;}
	public void setSourceText(String sourceText) {this.sourceText = sourceText;}
	public long getSessionId() {return sessionId;}
	public void setSessionId(long sessionId) {this.sessionId = sessionId;}
	public void setId(long id) {this.id = id;}	
	public long getPatIllscriptId() {return patIllscriptId;}
	public void setPatIllscriptId(long patIllscriptId) {this.patIllscriptId = patIllscriptId;}	
	public long getSourceId2() {return sourceId2;}
	public void setSourceId2(long sourceId2) {this.sourceId2 = sourceId2;}	
	public int getStage() {return stage;}
	public void setStage(int stage) {this.stage = stage;}
	private void setStageBasedOnCurrentStage(){
		stage = NavigationController.getInstance().getMyFacesContext().getPatillscript().getCurrentStage();
	}
	public void save(){
		setStageBasedOnCurrentStage();
		new DBClinReason().saveAndCommit(this);
	}
	
	public String getActionStr(){
		String s =  NavigationController.getInstance().getAdminFacesContext().getAppBean().intlConf.getValue("log.action."+action, new Locale("en"));
		return s;
	}
	
	public String getSourceIdStr(){
		if(this.sourceId<=0) return "";
		ListItem li = new DBList().selectListItemById(sourceId);
		if(li!=null) return li.getName();
		return "";
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer(100);
		sb.append(getActionStr()+": ");
		sb.append("Stage: " + this.stage);
		sb.append("Id: " + this.sourceId);
		return sb.toString();
		
	}
		
}
