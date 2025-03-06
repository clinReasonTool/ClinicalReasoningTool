package beans.helper;

import java.beans.Beans;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * @author ingahege
 * This object is a helper object for the calculation of case difficulty based on the diagnostic accuracy. 
 * It relates to the crt.map_overvoew_view
 *
 */
@ManagedBean(name = "vpmapoverviewitem", eager = true)
@SessionScoped
public class VPMapOverviewItem extends Beans implements Serializable{

	public static final int CHILD_TRANSLATION = 2;
	private long mapId =-1;
	private long caseId = -1; 
	private long orgCaseId = -1; // the parent case if there is any
	private int numFirstAttempts = -1; //number of tries a learner has needed to come up with the final diagnosis. 
	private int showSolStage = -1; // card idx at which the final daignosis was requested from the system 
	private int childType = -1;//net.casus.model.casestructure.CsCase.CHILD_TRANSLATION; 
	private int ddxSubmittedStage = -1; //card on which the final daignosis was med or 0/-1
	
	public long getCaseId() {return caseId;}
	public void setCaseId(long caseId) {this.caseId = caseId;}
	public long getOrgCaseId() {return orgCaseId;}
	public void setOrgCaseId(long orgCaseId) {this.orgCaseId = orgCaseId;}
	public int getNumFirstAttempts() {return numFirstAttempts;}
	public void setNumFirstAttempts(int numFirstAttempts) {this.numFirstAttempts = numFirstAttempts;}
	public long getMapId() {return mapId;}
	public void setMapId(long mapId) {this.mapId = mapId;}
	public int getShowSolStage() {return showSolStage;}
	public void setShowSolStage(int showSolStage) {this.showSolStage = showSolStage;}
	public int getChildType() {return childType;}
	public void setChildType(int childType) {this.childType = childType;}
	public int getDdxSubmittedStage() {return ddxSubmittedStage;}
	public void setDdxSubmittedStage(int ddxSubmittedStage) {this.ddxSubmittedStage = ddxSubmittedStage;}
	
	public boolean solutionRequested() {
		if(showSolStage>0) return true; 
		return false;
	}
	
	/**
	 * user has made a final diagnosis (only those sessions should be included)
	 * @return
	 */
	public boolean finalDiagnosisSubmitted() {
		if(ddxSubmittedStage>0) return true; 
		return false;
	}
	

	public boolean equals(Object o) {
		if(o instanceof VPMapOverviewItem && ((VPMapOverviewItem)o).getMapId()==this.mapId) 
			return true;
			
		return false;
	}
	
	
}
