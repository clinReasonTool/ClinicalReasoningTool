package beans.relation;

import java.io.Serializable;
import java.util.*;

import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;
import beans.list.*;

public class RelationInformation extends Relation implements Serializable{
	
	public static final int DEFAULT_X = 5; //245; //325; //default x position of problems in canvas

	private static final long serialVersionUID = 1L;
	private ListItem info;

	
	public RelationInformation(){}
	public RelationInformation(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
		
	public ListItem getInfo() {return info;}
	public void setInfo(ListItem info) {this.info = info;}
	public String getIdWithPrefix(){ return GraphController.PREFIX_INFO+this.getId();}

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_INFO;}	

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return info.getName();}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getListItem()
	 */
	public ListItem getListItem() {return info;}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return info.getSynonyma();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){		
		try {
			if(getSynId()<=0) return info.getName();
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
