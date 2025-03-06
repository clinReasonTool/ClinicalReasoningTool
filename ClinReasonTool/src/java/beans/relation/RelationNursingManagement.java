package beans.relation;

import java.io.Serializable;
import java.util.*;

import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;
import beans.list.*;

public class RelationNursingManagement extends Relation implements Serializable{
	
	public static final int DEFAULT_X = 5; //245; //325; //default x position of problems in canvas

	private static final long serialVersionUID = 1L;
	private ListItem nursmanagement;

	
	public RelationNursingManagement(){}
	public RelationNursingManagement(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
	public int getDiscriminator() {return TYPE_NMNG;}
	public void setDiscriminator(int i){}
	
	//do not change name (needed for mapping)	
	public ListItem getManagement() {return nursmanagement;}
	public void setManagement(ListItem nursmanagement) {this.nursmanagement = nursmanagement;}
	public String getIdWithPrefix(){ return GraphController.PREFIX_NMNG+this.getId();}

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_NMNG;}	

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return nursmanagement.getName();}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getListItem()
	 */
	public ListItem getListItem() {return nursmanagement;}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return nursmanagement.getSynonyma();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){		
		try {
			if(getSynId()<=0) return nursmanagement.getName();
			else return getSynonym().getName();
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return "";
		}
	}
	
	public void calculatePoints(int pos, boolean isExp) {
		//TODO
	}
}
