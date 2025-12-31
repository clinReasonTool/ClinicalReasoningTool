package beans.relation;

import java.awt.Point;
import java.io.Serializable;
import java.util.*;

//import actions.beanActions.AddDiagnosisAction;
//import actions.beanActions.AddMngAction;
import actions.beanActions.AddRelationAction;
import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;
import beans.list.*;

public class RelationManagement extends Relation implements Serializable{
	/** @deprecated **/
	public static final int DEFAULT_X = 5; //245; //325; //default x position of problems in canvas

	private static final long serialVersionUID = 1L;
	private ListItem management;

	
	public RelationManagement(){}
	public RelationManagement(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
	
	//public int getDiscriminator() {return TYPE_MNG;}
	//public void setDiscriminator(int i){}
	
	public ListItem getManagement() {return management;}
	public void setManagement(ListItem management) {this.management = management;}
	public void setListItem(ListItem li) {management = li;}
	//public String getIdWithPrefix(){ return GraphController.PREFIX_MNG+this.getId();}

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_MNG;}	

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return management.getName();}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getListItem()
	 */
	public ListItem getListItem() {return management;}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return management.getSynonyma();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){		
		try {
			if(getSynId()<=0) return management.getName();
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
