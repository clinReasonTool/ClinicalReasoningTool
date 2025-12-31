package beans.relation;

import java.io.Serializable;
import java.util.Set;

import beans.list.ListItem;
import beans.list.Synonym;
import controller.GraphController;
import net.casus.util.Utility;
import util.CRTLogger;

public class RelationAim extends Relation implements Serializable {

	private ListItem aim;
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_X = 15; //165; //245; //default x position of problems in canvas

	
	public RelationAim(){}
	public RelationAim(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}
	public ListItem getAim() {return aim;}
	public ListItem getListItem() {return getAim();}
	public void setAim(ListItem aim) {this.aim = aim;}	
	public void setListItem(ListItem li) {aim = li;}
	//public int getDiscriminator() {return REL_TYPE_NDDX;}
	//public void setDiscriminator(int i){}
		
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_AIM;}	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return aim.getName();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){	
		try {
			String postStr = "";
			
			if(getSynId()<=0) return aim.getName() + postStr;
			else return getSynonym().getName() + postStr;
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return "";
		}
	}

	public Set<Synonym> getSynonyma() {
		return aim.getSynonyma();
	}
	@Override
	/*public String getIdWithPrefix() {
		return GraphController.PREFIX_NURSINGAIM+this.getId();
	}*/
	
	public void calculatePoints(int pos, boolean isExp) {
		//TODO
	}
	public int getDiscriminator() {
		// TODO Auto-generated method stub
		return 0;
	}
}
