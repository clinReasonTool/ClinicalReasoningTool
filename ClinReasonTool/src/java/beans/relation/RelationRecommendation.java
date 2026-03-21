package beans.relation;

import java.awt.Point;
import java.io.Serializable;
import java.util.*;

import actions.beanActions.AddRelationAction;
import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;
import beans.list.*;

/**
 * @author ingahege
 */
public class RelationRecommendation extends Relation implements Serializable{
	
	public static final int DEFAULT_X = 5; //245; //325; //default x position of problems in canvas

	private static final long serialVersionUID = 1L;
	private ListItem recommendation;

	
	public RelationRecommendation(){}
	public RelationRecommendation(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
		
	public ListItem getRecommendation() {return recommendation;}
	public void setRecommendation(ListItem recommendation) {this.recommendation = recommendation;}
	public void setListItem(ListItem li) {recommendation = li;}
	public String getIdWithPrefix(){ return ""; /*GraphController.PREFIX_MREC+this.getId();*/}

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_REC;}	

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return recommendation.getName();}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getListItem()
	 */
	public ListItem getListItem() {return recommendation;}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return recommendation.getSynonyma();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){		
		try {
			if(getSynId()<=0) return recommendation.getName();
			else return getSynonym().getName();
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return "";
		}
	}
	
	public void calculatePoints(int pos, boolean isExp) {
		Point p = new AddRelationAction().calculateNewItemPosInCanvas(pos, isExp);
		this.setXAndY(p);
	}
}
