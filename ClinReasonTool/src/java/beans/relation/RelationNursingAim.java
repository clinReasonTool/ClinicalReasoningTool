package beans.relation;

import java.io.Serializable;
import java.util.Set;

import beans.list.ListItem;
import beans.list.Synonym;
import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;

public class RelationNursingAim extends Relation implements Serializable {

	private ListItem nursingAim;
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_X = 15; //165; //245; //default x position of problems in canvas

	
	public RelationNursingAim(){}
	public RelationNursingAim(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
	public ListItem getNursingAim() {return nursingAim;}
	public ListItem getListItem() {return getNursingAim();}
	public void setNursingAim(ListItem nursingAim) {this.nursingAim = nursingAim;}		
		
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_NURSAIM;}	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return nursingAim.getName();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){	
		try {
			String postStr = "";
			
			if(getSynId()<=0) return nursingAim.getName() + postStr;
			else return getSynonym().getName() + postStr;
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return "";
		}
	}

	public Set<Synonym> getSynonyma() {
		return nursingAim.getSynonyma();
	}
	@Override
	public String getIdWithPrefix() {
		return GraphController.PREFIX_NURSINGAIM+this.getId();
	}
	
	public void calculatePoints(int pos, boolean isExp) {
		//TODO
	}
}
