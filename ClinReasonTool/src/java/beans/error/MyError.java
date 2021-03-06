package beans.error;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.faces.bean.SessionScoped;

import properties.IntlConfiguration;

/**
 * Abstract class for all types of errors. 
 * @author ingahege
 *
 */
@SessionScoped
public  abstract class MyError implements Serializable{
	private static final long serialVersionUID = 1L;

	public static final int TYPE_PREMATURE_CLOUSRE = 1;
	public static final int TYPE_AVAILABILITY = 2;
	public static final int TYPE_CONFIRMATION = 3;
	public static final int TYPE_ANCHORING = 4;
	public static final int TYPE_BASERATE = 5;
	public static final int TYPE_REPRESENTATIVENESS = 6;
	//....
	
	private long id; 
	private String description; //get from a general source based on the error type... 
	/**
	 * type of subclass
	 */
	private int type; 
	/**
	 * we have a list of errors, idx reflects the order in that the errors have occured.
	 */
	private int idx;
	/**
	 * type of subclass as string (discriminator needs to be a string).
	 */
	private String discr;
	private Timestamp creationDate;
	private long patIllScriptId;
	/**
	 * How confident is the learner with his ddxs. (1-100) -> moved from patientIllnessScript
	 */
	private int confidence; 
	/**
	 * id (not item_id) of diagnosis this error is related to. 
	 */
	private long sourceId; 
	/**
	 * At which stage did the error occur? We need this for displaying errors when ddx submission occurs multiple times.
	 */
	private int stage;
	
		
	public int getConfidence() {return confidence;}
	public void setConfidence(int confidence) {this.confidence = confidence;}
	public long getSourceId() {return sourceId;}
	public void setSourceId(long sourceId) {this.sourceId = sourceId;}
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}
	public void setDescription(String description) {this.description = description;}
	public abstract int getType();
	public long getPatIllScriptId() {return patIllScriptId;}
	public void setPatIllScriptId(long patIllScriptId) {this.patIllScriptId = patIllScriptId;}
	public void setType(int type) {this.type = type;}	
	public int getStage() {return stage;}
	public void setStage(int stage) {this.stage = stage;}
	public String getDiscr() {return discr;}
	public void setDiscr(String discr) {this.discr = discr;}	
	public int getIdx() {return idx;}
	public void setIdx(int idx) {this.idx = idx;}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		if(o instanceof MyError){
			MyError e = (MyError) o;
			if (e.getId()== this.getId() || e.getType()==getType() && e.getStage() == getStage()) return true;
		}
		return false;
	}
	
	public String getDescription() {return IntlConfiguration.getValue("ddx.errors."+getType()+".expl");}
	public String getName() { return IntlConfiguration.getValue("ddx.errors."+getType()); }


}
