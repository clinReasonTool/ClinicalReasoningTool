package beans.relation.summary;

import beans.list.SIUnit;
import controller.SummaryStatementController;

public class SummaryStNumeric {

	private long id; 
	private long summstId;
	/**
	 * name of the matched item (e.g. 14mg,  128/100 mmHg,...)
	 */
	private String name; 
	
	private SIUnit unit;
	
	private long unitId;
	
	/**
	 * for the moment we store here if we have identified a number that is combined with the unit (this is 
	 * not stored in the database.
	 */
	//private String numeric;
	
	/**
	 * position of the word in the text (0-based)
	 */
	private int pos;
	private int endPos;
	private int idx;
	private String spacyType = null;
	/**
	 * Does the expert also have this unit in the statement?
	 */
	private boolean expMatch = false;
	
	public SummaryStNumeric(){}
	public SummaryStNumeric(SIUnit unit, int pos, int idx, String spacy, long summStId){
		this.unit = unit;	
		this.pos = pos;
		this.endPos = pos;
		if(idx>=0) this.idx = idx;
		if(spacy!=null && spacyType==null) spacyType = spacy;
		if(unit!=null) this.unitId = unit.getId();
		this.summstId = summStId;
		//if(StringUtilities.isNumeric(name)) this.numeric = name;
	}
	public SummaryStNumeric(SIUnit unit, String name, int pos, int idx, String spacy, long summStId){
		this.unit = unit;	
		this.name = name;
		this.pos = pos;
		this.endPos = pos;
		if(idx>=0) this.idx = idx;
		if(spacy!=null && spacyType==null) spacyType = spacy;
		if(unit!=null) this.unitId = unit.getId();
		this.summstId = summStId;
	}
	
	
	public int getIdx() {return idx;}
	public void setIdx(int idx) {this.idx = idx;}
	public String getSpacyType() {return spacyType;}
	public void setSpacyType(String spacyType) {this.spacyType = spacyType;}
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}	
	public SIUnit getUnit() {
		if(unit!=null)return unit;
		setUnit(SummaryStatementController.getUnitById(this.unitId));
		return unit;
		
	}
	public void setUnit(SIUnit unit) {this.unit = unit;}
	public int getPos() {return pos;}
	public void setPos(int pos) {this.pos = pos;}
	public int getEndPos() {return endPos;}
	public void setEndPos(int endPos) {this.endPos = endPos;}
	
	//public String getNumeric() {return numeric;}
	//public void setNumeric(String numeric) {this.numeric = numeric;}
	
	public long getUnitId() {return unitId;}
	public void setUnitId(long unitId) {this.unitId = unitId;}
	public long getSummstId() {return summstId;}
	public void setSummstId(long summstId) {this.summstId = summstId;}
	public boolean isExpMatch() {return expMatch;}
	public void setExpMatch(boolean expMatch) {this.expMatch = expMatch;}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		if(o instanceof SummaryStNumeric){
			SummaryStNumeric sn = (SummaryStNumeric) o;
			if (id>0 && (sn.getId()==id)) return true;
			if(sn.getSummstId()==this.summstId && sn.getUnitId()==this.unitId) return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		if(name!=null && unit!=null) return name + " " + unit.toString() + " (" + pos + "," + expMatch + ")";
		if(name!=null) return name + " (" + pos +  "," + expMatch + ")";
		if(unit!=null) return unit.toString() + " (" + pos  + "," + expMatch+")";
		return "";
	}
			
}
