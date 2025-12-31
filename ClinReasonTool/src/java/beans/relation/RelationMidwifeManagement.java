package beans.relation;

import java.io.Serializable;
import java.util.*;

import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;
import beans.list.*;

/**
 * @author ingahege
 * @deprecated
 */
public class RelationMidwifeManagement extends Relation implements Serializable{
	
	public static final int DEFAULT_X = 5; //245; //325; //default x position of problems in canvas

	private static final long serialVersionUID = 1L;
	private ListItem mwmanagement;

	
	public RelationMidwifeManagement(){}
	public RelationMidwifeManagement(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
	public int getDiscriminator() {return SUBTYPE_MMNG;}
	public void setDiscriminator(int i){}
	
	//do not change name (needed for mapping)	
	public ListItem getManagement() {return mwmanagement;}
	public void setManagement(ListItem mwmanagement) {this.mwmanagement = mwmanagement;}
	public void setListItem(ListItem li) {mwmanagement = li;}
	public String getIdWithPrefix(){ return ""; /*GraphController.PREFIX_MMNG+this.getId();*/}

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return SUBTYPE_MMNG;}	

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return mwmanagement.getName();}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getListItem()
	 */
	public ListItem getListItem() {return mwmanagement;}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return mwmanagement.getSynonyma();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){		
		try {
			if(getSynId()<=0) return mwmanagement.getName();
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
