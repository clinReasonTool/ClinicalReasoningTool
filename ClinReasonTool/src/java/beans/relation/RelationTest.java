package beans.relation;

import java.awt.Point;
import java.io.Serializable;
import java.util.*;

import actions.beanActions.AddDiagnosisAction;
import actions.beanActions.AddTestAction;
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
 */
public class RelationTest extends Relation implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final int QUALIFIER_RARE = 0; 
	public static final int QUALIFIER_MEDIUM = 1;
	public static final int QUALIFIER_OFTEN = 2;
	public static final int DEFAULT_X = 15; //165; //245; //default x position of problems in canvas
	
	
	/**
	 * problems: key-finding, other,... (?)
	 */
	private int value; //key finding,...
	
	/**
	 * how often is a problem prevalent in a diagnosis (rare, medium, often)
	 */
	private int qualifier;
	
	private ListItem test;
	
	public RelationTest(){}
	public RelationTest(long listItemId, long destId, long synId){
		this.setListItemId(listItemId);
		this.setDestId(destId);
		if(synId>0) setSynId(synId);
	}	
	public ListItem getTest() {return test;}
	public void setTest(ListItem test) {this.test = test;}		
	public String getIdWithPrefix(){ return GraphController.PREFIX_TEST+this.getId();}
	

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getRelationType()
	 */
	public int getRelationType() {return TYPE_TEST;}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabel()
	 */
	public String getLabel(){return test.getName();}
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getListItem()
	 */
	public ListItem getListItem(){return test;}

	/* (non-Javadoc)
	 * @see beans.relation.Relation#getSynonyma()
	 */
	public Set<Synonym> getSynonyma(){ return test.getSynonyma();}
	
	/* (non-Javadoc)
	 * @see beans.relation.Relation#getLabelOrSynLabel()
	 */
	public String getLabelOrSynLabel(){		
		try {
			if(getSynId()<=0) return test.getName();
			else return getSynonym().getName();
		}
		catch(Exception e) {
			CRTLogger.out(Utility.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return "";
		}
	}
	
	public void calculatePoints(int pos, boolean isExp) {
		Point p = new AddTestAction().calculateNewItemPosInCanvas(pos, isExp);
		this.setXAndY(p);
	}
}
