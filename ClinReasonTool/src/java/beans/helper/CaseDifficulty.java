package beans.helper;

import java.sql.Timestamp;

/**
 * Contains the calculated case difficulty (for now only for cases with concept maps) for a parent case and all child cases that are
 * translated versions (CHILD_TYPE = 2)
 * @author ingahege
 * @deprecated -> should by handled on CASUS side by class net.casus.model.casestructure.addons.Casedifficulty 
 */
public class CaseDifficulty {

	private long caseId;
	/**
	 * for cases with CR concept maps this is based on the number of attempts needed for the final diagnosis -> see CaseDifficultyCalculator
	 */
	private float difficulty; 
	private Timestamp calcDate; 
	/**
	 * number of sessions included into the calculation
	 */
	private int numSess;
	
	public CaseDifficulty() {};
	public CaseDifficulty(float diff, int numSess, long caseId) {
		this.difficulty = diff;
		this.numSess = numSess;
		this.caseId = caseId;
	}
	
	public long getCaseId() {return caseId;}
	public void setCaseId(long caseId) {this.caseId = caseId;}
	public float getDifficulty() {return difficulty;}
	public void setDifficulty(float difficulty) {this.difficulty = difficulty;}
	public Timestamp getCalcDate() {return calcDate;}
	public void setCalcDate(Timestamp calcDate) {this.calcDate = calcDate;}
	public int getNumSess() {return numSess;}
	public void setNumSess(int numSess) {this.numSess = numSess;}
	

	public boolean equals(Object o) {
		if(o instanceof CaseDifficulty) {
			if(((CaseDifficulty) o).getCaseId()==this.caseId) return true;
		}
		return false;
	}
	
}