package beans.relation;

import java.io.Serializable;
import java.util.*;

import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;
import beans.list.*;

/**
 * Relation between an (Patient-)IllnessScript and a problem. We need this to specify a problem, e.g. whether it is 
 * almost proving a diagnosis or rarely occurs with a diagnosis.
 * We might need more qualifiers,...
 * @author ingahege
 * 
 * @deprecated
 *
 */
public class RelationMidwifeFinding extends Relation implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final int QUALIFIER_RARE = 0; 
	public static final int QUALIFIER_MEDIUM = 1;
	public static final int QUALIFIER_OFTEN = 2;
	public static final int DEFAULT_X = 15; //80; //default x position of problems in canvas
	
	
	//private Timestamp creationDate;
	private ListItem problem;

	/**
	 * If a user has selected a synonym instead of the main item, we store this here in addition to the main item.
	 */
	
	public RelationMidwifeFinding(){}
	public RelationMidwifeFinding(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
	//public int getDiscriminator() {return Relation.SUBTYPE_MFDG;}
	//public void setDiscriminator(int i){}
	
	public ListItem getProblem() {return problem;}
	public ListItem getListItem() {return getProblem();}
	public void setProblem(ListItem problem) {this.problem = problem;}	
	public void setListItem(ListItem li) {problem = li;}
	
	public String getIdWithPrefix(){return ""; /*GraphController.PREFIX_MFDG+this.getId();*/}
	
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	//public int getRelationType() {return TYPE_MFDG;}	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return problem.getName();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){	
		try{
			String postStr = "";
			//if(this.getIsSyndrome()==1) postStr = " (Syndrome)";
			if(getSynId()<=0) return problem.getName() + postStr;
			else  return getSynonym().getName() + postStr;
			//else return "";
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return "";
		}
		
	}
	
///	public int getPrototypical() {return prototypical;}
//	public void setPrototypical(int prototypical) {this.prototypical = prototypical;}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return problem.getSynonyma();}
	
	public void calculatePoints(int pos, boolean isExp) {
		//TODO
	}
	@Override
	public int getRelationType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
