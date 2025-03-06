package beans.relation;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.list.ListItem;
import beans.list.Synonym;
import beans.scripts.PatientIllnessScript;
import controller.*;
import net.casus.util.Utility;
import properties.IntlConfiguration;
import util.CRTLogger;

public class RelationMidwifeHypothesis extends Relation implements Serializable {

	private static final long serialVersionUID = 1L;
	//public static final int REL_TYPE_FINAL = 1; //final diagnosis (for PIS only)
	public static final int REL_TYPE_NDDX = 7; //differential diagnosis 
	public static final int DEFAULT_X = 5; //165; //default x position of problems in canvas
	public static final String COLOR_DEFAULT = "#ffffff";
	public static final int TIER_NONE = 0; //I do not now

	
	public int getDiscriminator() {return TYPE_MHYP;}
	public void setDiscriminator(int i){}

	/**
	 * how likely is this diagnosis, from unlikely to final diagnosis
	 * see tier definitions above
	 */
	private int tier = -1; 

	private ListItem diagnosis;
	/**
	 * Must-not-miss
	 */
	//private int mnm = 0;
	
	/**
	 * stage at which a diagnosis has been ruled out, -1/0: not ruled out
	 */
	//private int ruledOut;
	
	/**
	 * Current working diagnosis (most likely one at current stage)
	 */
	//private int workingDDX;
	
	/**
	 * How common is this diagnosis in patients like the one this diagnosis is assigned to. 
	 * Currently can only be set in the expert maps to be able to detec BaseRateNeglect errors...
	 * This could also be something to be inherited from the ListItem if a disease is very rare in general... 
	 */
	//private int prevalence = -1;

	
	public RelationMidwifeHypothesis(){}
	public RelationMidwifeHypothesis(long lisItemId, long destId, long synId){
		this.setListItemId(lisItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}

	public int getTier() {return tier;}
	
	/**
	 * We only display the expert's final diagnosis after the user has submitted a diagnosis.... 
	 * @return
	 */
	public int getExptier(){
		//if(tier!=TIER_FINAL) return tier; //everything except finals are displayed... 
		//PatientIllnessScript learnerscript = NavigationController.getInstance().getMyFacesContext().getPatillscript();
		//if(learnerscript.getSubmitted()) return TIER_FINAL;
		return 0;
	}
	
	//public void setTier(int tier) {this.tier = tier;}
	public ListItem getDiagnosis() {return diagnosis;}
	public void setDiagnosis(ListItem diagnosis) {this.diagnosis = diagnosis;}	
	public String getShortLabelOrSynShortLabel(){return StringUtils.abbreviate(getLabelOrSynLabel(), ListItem.MAXLENGTH_NAME-2);}
	
	
	public String getIdWithPrefix(){ return GraphController.PREFIX_NDDX+this.getId();}

	
	public String getColor(){		
		//boolean expEdit = NavigationController.getInstance().isExpEdit();
		PatientIllnessScript learnerscript = new NavigationController().getMyFacesContext().getPatillscript();
		//if(learnerscript!=null && learnerscript.isExpScript()){ 			
			//if(isRuledOutBool() && ruledOut<=learnerscript.getStage()) return COLOR_RULEDOUT;
			return COLOR_DEFAULT;//"#000000";

	}
	
	
	public int getExpCssClass(){		
		//if the current script is created as an expert script we display the colors only from the stage on in which they have been assigned.
		PatientIllnessScript learnerscript = NavigationController.getInstance().getMyFacesContext().getPatillscript();
		if(learnerscript.isExpScript()) return -1;
		Graph g = NavigationController.getInstance().getCRTFacesContext().getGraph();
		MultiVertex mv = g.getVertexByIdAndType(this.getListItemId(), Relation.TYPE_MHYP);
		if(mv==null || mv.getExpertVertex()==null) return -1;
		RelationMidwifeHypothesis expRel = (RelationMidwifeHypothesis) mv.getExpertVertex();
		//if(isFinalDiagnosis() && learnerscript.getSubmittedStage()<=learnerscript.getStage()) return COLOR_FINAL;
		
		//if(expRel.isFinalDDX() && (learnerscript.getCurrentStage()>learnerscript.getMaxSubmittedStage() || learnerscript.getSubmitted())) return TIER_FINAL;
		//if(isRuledOutBool() && ruledOut<=learnerscript.getCurrentStage()) return TIER_RULEDOUT;
		//if(isWorkingDDXBool() && workingDDX<=learnerscript.getCurrentStage()) return TIER_WORKINGDDX;
		return -1;
		
	}
	
	public int getCssClass(){
		//boolean expEdit = NavigationController.getInstance().isExpEdit();
		PatientIllnessScript learnerscript = NavigationController.getInstance().getMyFacesContext().getPatillscript();

		if(learnerscript!=null && learnerscript.isExpScript()){

			return TIER_NONE;

		}
		
		return TIER_NONE;
	}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_MHYP;}	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return diagnosis.getName();}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getListItem()
	 */
	public ListItem getListItem() {return diagnosis;}

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return diagnosis.getSynonyma();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){		
		try {
			if(this.diagnosis.getItem_id()==0){
				return IntlConfiguration.getValue("ddx.nodiagnosis");
			}
			if(getSynId()<=0) return diagnosis.getName();
			else return getSynonym().getName();
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return "";
		}
	}
	
	/**
	 * return the score if this ddx has been selected as a final diagnosis.  
	 * @return
	 */
	public String getFinalDDXScore(){ 
		return new ScoringController().getIconForFinalDDXScore(this.getListItemId());
	}
	
	/**
	 * return the score if this ddx has been selected as a final diagnosis.  
	 * @return
	 */
	public String getExp(){ 
		Graph g = NavigationController.getInstance().getMyFacesContext().getGraph();
		MultiVertex mvertex = g.getVertexByIdAndType(this.getListItemId(), Relation.TYPE_MHYP);
		if(mvertex==null || mvertex.getLearnerVertex()==null) return ""; //should not happen
		//only return something if learner has chosen it as a final ddx:
		/*boolean isLearnerFinal = ((RelationNursingDiagnosis)mvertex.getLearnerVertex()).getTier() == RelationNursingDiagnosis.TIER_FINAL;
		if(isLearnerFinal && mvertex.getExpertVertex()==null){ 
			//no scoring was done, then we have to return the expert final diagnoses/-is
			if(NavigationController.getInstance().getCRTFacesContext().getSessSetting().getListMode()==SessionSetting.LIST_MODE_NONE){ 
				String s = IllnessScriptController.getInstance().getExpFinalsAsString(g.getVpId());
				return s;
			}
			else
				return IntlConfiguration.getValue("ddx.nodiff");
		}*/
		try{
			RelationMidwifeHypothesis expRel = (RelationMidwifeHypothesis) mvertex.getExpertVertex();
			//if(expRel==null) return IntlConfiguration.getValue("ddx.tierdescr.noexp"); //expert does not have item
			//if(isLearnerFinal) return IntlConfiguration.getValue("ddx.tierdescr."+expRel.getTier()); //String.valueOf(expRel.getTier());
		}
		catch(Exception e){return "";} //can happen if expert has this item not as a diagnosis but as a something else....	
		return "";
	}
	
	/**
	 * is currently an expert script edited? If so we display a link to mark a diagnosis as final.
	 * @return
	 */
	public boolean getIsExpEdit(){
		return NavigationController.getInstance().getMyFacesContext().getPatillscript().isExpScript();}
	
	public void calculatePoints(int pos, boolean isExp) {
		//TODO
	}

}

