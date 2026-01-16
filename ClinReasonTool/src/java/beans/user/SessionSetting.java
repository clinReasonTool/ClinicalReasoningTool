package beans.user;

import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import beans.scripts.PatientIllnessScript;
import controller.NavigationController;
import controller.SessionSettingController;
import util.*;

/**
 * Special settings for a VP session, e.g. different modes for feedback or scaffolding,...
 * Currently this needs to be provided by the parent VP system, otherwise default setting is used (e.g. expert feedback
 * always accessible)
 * @author ingahege
 *
 */
@ManagedBean(name = "sessSett", eager = true)
@SessionScoped
public class SessionSetting {
	public static final int EXPFEEDBACKMODE_END = 1; //show expert feedback only on last card
	public static final int EXPFEEDBACKMODE_NEVER = 2; //show no expert feedback at all
	public static final int LIST_MODE_NONE = 1; //no list is used
	public static final int LIST_MODE_USE = 0; //list is used

	
	private long id;
	/**
	 * When shall the expert script be displayed...
	 */
	private int expFeedbackMode = 0; //0 or -1 is default
	/**
	 * When shall the peer feedback be displayed...
	 */
	private int peerFeedbackMode = 0; //0 or -1 is default
	
	private String vpId;
	private long userId;
	/**
	 * if the hint for the expert feedback has been displayed once, we set this to true and do not display it again. 
	 * Not stored in the database....
	 */
	private boolean expHintDisplayed = false;
	
	/**
	 * is choosing "no diagnosis" enabled (0) or not (1)
	 */
	private int ddxMode = 0;
	
	/**
	 * Is a type-ahead list used or not? 0=default/List is used, 1=list is 
	 */
	private int listMode = 0;
	
	/**
	 * each position stands for a box (0=problems, 1=ddx, 2=tests, 3=therapies, 4=summary statement, 5 = patho), per default all 
	 * boxes are used. If boxes shall not be used/displayed, there needs to be a 0 at the position.
	 * If boxes shall be displayed but in a passive mode, there needs to be a 2 at the position.
	 * Is stores as a String in the database! 
	 */
	//private int[] boxesUsed = {1,1,1,1,1,0}; 
	//we currently only support active / passive for the four standard boxes (can be configured in coursemanager)
	private int boxMode1 = 1; //box 1 used as active=1, passive=2 or hidden
	private int boxMode2 = 1; //box 2 used as active=1, passive=2 or hidden
	private int boxMode3 = 1; //box 3 used as active=1, passive=2 or hidden
	private int boxMode4 = 1; //box 4 used as active=1, passive=2 or hidden
	//private int boxModePat = 0; //pathophys used (active=1, passive=2)
	private int boxModeSum = 1; //summary statement used (active=1, passive=2)
	//private int boxUsedNDDX = 0; 
	//private int boxUsedNMng = 0; 
	//private int boxUsedNAim = 0;
	//private int boxUsedNInfo = 0; 



	
	/**
	 * at which position is a box, default is problems (0) at pos[0] upper left corner etc 
	 * pos[1]=upper right corner, pos[2] = lower left corner, pos[3] = lower right corner
	 */
	//>private int[] boxesPositions = {0,1,2,3}; //default
	
	
	public SessionSetting(){}
	
	public SessionSetting(String vpId, long userId){
		this.userId = userId;
		this.vpId = vpId;
	}
	
	public int getExpFeedbackMode() {return expFeedbackMode;}
	public void setExpFeedbackMode(int expFeedbackMode) {this.expFeedbackMode = expFeedbackMode;}
	public int getPeerFeedbackMode() {return peerFeedbackMode;}
	public void setPeerFeedbackMode(int peerFeedbackMode) {this.peerFeedbackMode = peerFeedbackMode;}
	public String getVpId() {return vpId;}
	public void setVpId(String vpId) {this.vpId = vpId;}
	public long getUserId() {return userId;}
	public void setUserId(long userId) {this.userId = userId;}	
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}	
	private boolean isExpHintDisplayed() {return expHintDisplayed;}
	public void setExpHintDisplayed(boolean hintDisplayed) {this.expHintDisplayed = hintDisplayed;}
	public int getDdxMode() {return ddxMode;}
	public int getListMode() {return listMode;}
	public void setListMode(int listMode) {this.listMode = listMode;}
	public void setListMode(Locale loc) {
		//not very nice, should be configurable which languages use lists and which not. 
		if(loc!=null && (loc.getLanguage().equalsIgnoreCase("pl") || loc.getLanguage().equalsIgnoreCase("sv")))
			this.listMode = LIST_MODE_NONE;
	}

	/**
	 * only allow "no diagnosis" option when the learner is at the point where he/she has to submit a daignosis. Otherwise
	 * this could be misleading....
	 * @return
	 */
	public int getDdxModeForStage() {
		PatientIllnessScript patillscript = NavigationController.getInstance().getMyFacesContext().getPatillscript();
		if(patillscript!=null && patillscript.getCurrentStage()==patillscript.getMaxSubmittedStage())
			return ddxMode;
		
		else return PatientIllnessScript.FINAL_DDX_YES;
	}
			
	public void setDdxMode(int ddxMode) {this.ddxMode = ddxMode;}

	/**
	 * return true if exp feedback shall be accessible, otherwise false. Depends on current stage in session and 
	 * feedback mode.
	 * @param actStage
	 * @param maxStage
	 * @return
	 */
	public boolean displayExpFeedback(int actStage, int maxStage){
		if(expFeedbackMode == EXPFEEDBACKMODE_NEVER) return false; //never show exp feedback
		if(expFeedbackMode<=0) return true; //default setting, feedback always accessible
		if(expFeedbackMode==EXPFEEDBACKMODE_END){ //only show at end:
			if(actStage>=maxStage){
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Decide whether a hint to the expert feedback button shall be displayed or not. Display if feedback is only available 
	 * at the end of a session if learner is on last stage/card.
	 * @param actStage
	 * @param maxStage
	 * @return
	 */
	public boolean displayExpFeedbackHint(int actStage, int maxStage){
		if(isExpHintDisplayed()) return false; //hint has already been displayed before....
		if(expFeedbackMode == EXPFEEDBACKMODE_NEVER) return false;
		if(expFeedbackMode<=0) return false;
		if(expFeedbackMode==EXPFEEDBACKMODE_END && actStage==maxStage){
			setExpHintDisplayed(true);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		if(o instanceof SessionSetting){
			SessionSetting sessSett = (SessionSetting) o;
			if(sessSett.getUserId()==this.userId && sessSett.getVpId().equals(this.vpId)) return true;
			if(sessSett.getId()==this.id) return true;
			return false;
		}
		return false;
	}
	
	/**
	 * returns whether a box shall be displayed or not. Default is that it is displayed. 
	 * @param pos
	 * @return
	 */
	/*public boolean displayBoxatPos(int pos){
		try{
			if (boxesUsed==null) return true; 
			if (pos>boxesUsed.length) return true; 
			if (boxesUsed[pos]==0) return false; 
			return true;
		}
		catch (Exception e){
		CRTLogger.out("Exception" + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return true;
		}
	}*/
	
	//public int[] getBoxesUsed(){ return boxesUsed;}
	//public String getBoxesUsedStr(){ return StringUtilities.toString(boxesUsed,",");}
	
	/*public void setBoxesUsedStr(String boxesUsedStr){
		if(boxesUsedStr==null || boxesUsedStr.equals("") || !boxesUsedStr.contains(",")) return;
		boxesUsed = StringUtilities.getIntArrFromString(boxesUsedStr, ",");
	}*/
	
	/**
	 * value was originally 0 or 1 (on/off), now we use a value to indicate the type of box 
	 * shall be displayed at which position. 
	 * value definitions see Relation class
	 * @param i
	 * @param value
	 */
	/*private void setBoxesUsed(int i, int value){	
		try{
			if(value>=0)
				boxesUsed[i]=value;
			else boxesUsed[i]=1;
		}
		catch (Exception e){};
	}*/
	
	public int getBoxMode1() {return boxMode1;}
	public void setBoxMode1(int boxMode1) {	this.boxMode1 = boxMode1;}
	
	public int getBoxMode2() {return boxMode2;}
	public void setBoxMode2(int boxMode2) {this.boxMode2= boxMode2;}
	
	public int getBoxMode3() {return boxMode3;}
	public void setBoxMode3(int boxMode3) {this.boxMode3= boxMode3;}
	
	public int getBoxMode4() {return boxMode4;}
	public void setBoxMode4(int boxMode4) {this.boxMode4= boxMode4;}
	/*public int getBoxModeTst() {return boxModeTst;}
	public void setBoxModeTst(int boxUsedTst) {this.boxModeTst = boxUsedTst;}
	public int getBoxModeMng() {return boxModeMng;}
	public void setBoxModeMng(int boxModeMng) {this.boxModeMng = boxModeMng;}
	public int getBoxModePat() {return boxModePat;}
	public void setBoxModePat(int boxModePat) {this.boxModePat = boxModePat;}*/
	public int getBoxModeSum() {return boxModeSum;}
	public void setBoxModeSum(int boxModeSum) {this.boxModeSum = boxModeSum;}
	/*public int getBoxUsedNDDX() {return boxUsedNDDX;}
	public void setBoxUsedNDDX(int boxUsedNDDX) {this.boxUsedNDDX = boxUsedNDDX;}	
	public int getBoxUsedNMng() {return boxUsedNMng;}
	public void setBoxUsedNMng(int boxUsedNMng) {this.boxUsedNMng = boxUsedNMng;}
	public int getBoxUsedNInfo() {return boxUsedNInfo;}
	public void setBoxUsedNInfo(int boxUsedNInfo) {this.boxUsedNInfo = boxUsedNInfo;}
	public int getBoxUsedNAim() {return boxUsedNAim;}
	public void setBoxUsedNAim(int boxUsedNAim) {this.boxUsedNAim = boxUsedNAim;}	*/
	/*public int getProbBoxUsed(){ return boxesUsed[0];}
	public int getDdxBoxUsed(){ return boxesUsed[1];}	
	public int getTestBoxUsed(){ return boxesUsed[2];}
	public int getMngBoxUsed(){ return boxesUsed[3];}
	public int getPathoBoxUsed(){ return boxesUsed[4];}*/
	
}
