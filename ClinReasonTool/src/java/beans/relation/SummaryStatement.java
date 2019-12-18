package beans.relation;
import java.beans.Beans;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import javax.faces.bean.SessionScoped;

import beans.list.*; 
/**
 * Summary Statement of the author or learner for a VP. There might be multiple Summary Statements for a case (changed
 * at distinct steps), all changes, variants are saved in the PIS_Log object.
 * @author ingahege
 *
 */
@SessionScoped
public class SummaryStatement extends Beans implements Serializable{

	private static final long serialVersionUID = 1L;
	private String text; 
	private long id = -1; 
	private Timestamp creationDate;
	private int stage = -1;
	private String lang;
	/**
	 * not yet needed, but we store it already for changing later on to multiple statements/script...
	 */
	private long patillscriptId = -1;
	/**
	 * expert or learner statement(see definitions in PatientIllnessScript)
	 */
	private int type = -1;
	/**
	 * has the text been evaluated/analyzed
	 */
	private boolean analyzed;
	/**
	 * all semantic qualifier ids that have been
	 */
	private List<SummaryStatementSQ> sqHits;
	private List<SummaryStElem> itemHits; //identified findings and diseases
	
	/**
	 * score can be 0, 1, or 2 (see rubric by Smith et al)
	 */
	private int sqScore;
	/**
	 * score can be 0, 1, or 2 (see rubric by Smith et al)
	 */
	private int transformationScore;
	
	/**
	 * score can be 0, 1, or 2 (see rubric by Smith et al)
	 * we try to do the same here as manually done
	 */
	private int narrowingScore;
	
	/**
	 * score can be 0, 1, or 2 (see rubric by Smith et al)
	 * we try to be more accurate here, considering more than for the original score.
	 */
	private int narrowingScoreNew;
	
	/**
	 * SIunits we have found in the summary statement (e.g. mg, dl, mmHg,...) as a negative indicator 
	 * for transformation
	 */
	private List<SummaryStNumeric> units;
	
	public SummaryStatement(){}
	public SummaryStatement(String text){
		this.text = text;
	}
	public SummaryStatement(String text, long patIllscriptId){
		this.text = text;
		this.patillscriptId = patIllscriptId;
	}
	public String getText() {return text;}
	public void setText(String text) {this.text = text;}
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}		
	public long getPatillscriptId() {return patillscriptId;}
	public void setPatillscriptId(long patillscriptId) {this.patillscriptId = patillscriptId;}
	public int getStage() {return stage;}
	public void setStage(int stage) {this.stage = stage;}		
	public int getType() {return type;}
	public void setType(int type) {this.type = type;}
	public boolean isAnalyzed() {return analyzed;}
	public void setAnalyzed(boolean analyzed) {this.analyzed = analyzed;}	
	public String getLang() {return lang;}
	public void setLang(String lang) {this.lang = lang;}
	public int getTransformationScore() {return transformationScore;}
	public void setTransformationScore(int transformationScore) {this.transformationScore = transformationScore;}	
	public int getNarrowingScore() {return narrowingScore;}
	public void setNarrowingScore(int narrowingScore) {this.narrowingScore = narrowingScore;}	
	public int getNarrowingScoreNew() {return narrowingScoreNew;}
	public void setNarrowingScoreNew(int narrowingScoreNew) {this.narrowingScoreNew = narrowingScoreNew;}
	public List<SummaryStNumeric> getUnits() {return units;}
	public void setUnits(List<SummaryStNumeric> units) {this.units = units;}
	
	public void addUnit(SummaryStNumeric u){
		if(u==null) return;
		if(units==null) units = new ArrayList<SummaryStNumeric>();
		units.add(u); 
	}
	
	public int getUnitNum(){
		if(units==null) return 0;
		return units.size();
	}
	/**
	 * column "SQ1"
	 * @return
	 */
	public List getSqHits(){return sqHits;}
	/**
	 * add a matching listItem 
	 * @param li
	 */
	public void addItemHit(ListItem li){
		if(itemHits==null) itemHits = new ArrayList<SummaryStElem>();
		SummaryStElem el = new SummaryStElem(li);
		if(!itemHits.contains(el)) //do not add duplicates!
			itemHits.add(el);		
	}

	/**
	 * add a matching synonym.
	 * @param li
	 * @param s
	 */
	public void addItemHit(ListItem li, Synonym s){
		if(itemHits==null) itemHits = new ArrayList<SummaryStElem>();
		SummaryStElem el = new SummaryStElem(li);
		if(s!=null) el.setSynonymStr(s.getName());
		if(!itemHits.contains(el)) //do not add duplicates!
			itemHits.add(el);		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		if(o instanceof SummaryStatement){
			if(((SummaryStatement) o).getId() == this.id) return true;
		}
		return false;
	}	
	
	public List<SummaryStElem> getItemHits() {return itemHits;}
	public void setItemHits(List<SummaryStElem> itemHits) {this.itemHits = itemHits;}
	public void setSqHits(List<SummaryStatementSQ> hits){this.sqHits = hits;}
	
	/**
	 * display the summary statement with the highlighted sematic qualifiers 
	 * We split the text, match it with the sematic qualifier hits and print the hits in a different color/bold.
	 * @return
	 */
	public String getTextSQ(){
		if(sqHits==null || sqHits.isEmpty()) return text;
		if(text ==null || text.trim().isEmpty()) return "";
		String[] splitText = text.split(" ");
		for(int i=0;i<splitText.length; i++){
			String s = splitText[i].toLowerCase().trim();
			for(int j=0; j<sqHits.size(); j++){
				if(s.contains(sqHits.get(j).getText().trim().toLowerCase())){
					splitText[i] = "<span class='sqhit'>"+s+"</span>";
				}
			}
		}
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < splitText.length; i++) {
		   strBuilder.append(splitText[i]+ " ");
		}
		return strBuilder.toString();
	}
	
	/**
	 * Findings (column "Findings"
	 * @return
	 */
	public String getFindingHits(){
		if(itemHits==null) return "";
		StringBuffer fdgs = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.isFinding()) fdgs.append(e.toString()+", ");
		}
		return fdgs.toString();
	}
	
	public int getFindingHitsNum(){
		if(itemHits==null || itemHits.isEmpty()) return 0;
		int counter=0;
		for(int i=0; i<itemHits.size();i++){
			if(itemHits.get(i).isFinding()) counter++;
		}
		return counter;
	}
	
	public int getDiagnosesHitsNum(){
		if(itemHits==null || itemHits.isEmpty()) return 0;
		int counter=0;
		for(int i=0; i<itemHits.size();i++){
			if(itemHits.get(i).isDiagnosis()) counter++;
		}
		return counter;
	}
	
	/**
	 * column "Anatomy"
	 * @return
	 */
	public String getAnatomyHits(){
		if(itemHits==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.isAnatomy()) sb.append(e.toString()+", ");
		}
		return sb.toString();
	}
	
	public int getAnatomyHitsNum(){
		if(itemHits==null || itemHits.isEmpty()) return 0;
		int counter=0;
		for(int i=0; i<itemHits.size();i++){
			if(itemHits.get(i).isAnatomy()) counter++;
		}
		return counter;
	}
	
	
	/**
	 * column "Diagnoses"
	 * @return
	 */
	public String getDiagnosesHits(){
		if(itemHits==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.isDiagnosis()) sb.append(e.toString()+", ");
		}
		return sb.toString();
	}

	/**
	 * column "Therapies"
	 * @return
	 */
	public String getTherHits(){
		if(itemHits==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.isTher()) sb.append(e.toString()+", ");
		}
		return sb.toString();
	}
	
	/**
	 * column "Tests"
	 * @return
	 */
	public String getTestHits(){
		if(itemHits==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.isTest()) sb.append(e.toString()+", ");
		}
		return sb.toString();
	}
	
	/**
	 * column ExpMatchSumSt
	 * all matching entries of the summary statement with the expert statement
	 * @return
	 */
	public String getExpMatches(){
		if(itemHits==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.isExpertMatch()) sb.append(e.toString()+", ");
		}
		return sb.toString();
	}
	
	/**
	 * column ExpMatchSumSt
	 * all matching entries of the summary statement with the expert statement and matches with the 
	 * expert map are counted as 50%
	 * @return
	 */
	public float getExpMatchesNum(){
		if(itemHits==null) return 0;
		float counter = (float) 0.0;
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.isExpertMatch()) counter = (float) (counter + 1.0);
			else if (e.getExpertScriptMatch()==1 || e.getExpertScriptMatch()==2)
				counter = (float) (counter + 0.5);
		}
		return counter;
	}
	
	/**
	 * all matching entries of the summary statement with the expert map
	 * column ExpMatchScript
	 * @return
	 */
	public String getExpScriptMatches(){
		if(itemHits==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.getExpertScriptMatch()>0) sb.append(e.toString()+" ("+e.getExpertScriptMatch()+"), ");
		}
		return sb.toString();
	}
	
	/**
	 * we return the number of matches with the expert statement and the map in findings, diagnoses, and anatomy as a 
	 * narrowing indicator
	 * @return
	 */
	public int getExpMatchNarrowing(){
		if(itemHits==null) return 0;
		int counter = 0;
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(e.getExpertScriptMatch()==Relation.TYPE_PROBLEM || e.getExpertScriptMatch()==Relation.TYPE_DDX)
				counter++;
			else if(e.isExpertMatch() && (e.isAnatomy() || e.isFinding() || e.isDiagnosis())) counter++;
		}
		return counter;
	}
	
	/**
	 * column "Other"
	 * @return
	 */
	public String getOtherHits(){
		if(itemHits==null) return "";
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<itemHits.size();i++){
			SummaryStElem e = itemHits.get(i);
			if(!e.isAnatomy() && !e.isDiagnosis() &&!e.isFinding() && !e.isTest() && !e.isTher()) 
				sb.append(e.toString());
		}
		return sb.toString();
	}
	
	public int getSqScore() {return sqScore;}	
	public String getSqScoreToString() {return Integer.toString(sqScore);}
	public void setSqScore(int sqScore) {this.sqScore = sqScore;}
	public String getUnitHitsToString(){
		if(units==null) return "";
		return units.toString();
	}
	public int getUnitsNum(){
		if(units==null) return 0;
		return units.size();
	}
	
}
