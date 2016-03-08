package beans.relation;

import java.beans.Beans;
import java.io.Serializable;

import javax.faces.bean.*;

/**
 * Relation between an (Patient-)IllnessScript and a problem. We need this to specify a problem, e.g. whether it is 
 * almost proving a diagnosis or rarely occurs with a diagnosis.
 * We might need more qualifiers,...
 * @author ingahege
 *
 */
/*@ManagedBean(name = "problem", eager = true)
@SessionScoped*/
public class RelationProblem extends Beans implements Relation, Serializable{

	private static final long serialVersionUID = 1L;
	public static final int QUALIFIER_RARE = 0; 
	public static final int QUALIFIER_MEDIUM = 1;
	public static final int QUALIFIER_OFTEN = 2;
	
	private long id;
	/**
	 * can be problem, test, management, diagnosis
	 */
	private long sourceId; 
	/**
	 * To be on the safe side we store the name here as well....
	 */
	private String name;
	/**
	 * (Patient)Illnesscript
	 */
	private long destId; 
	
	private int order;
	/**
	 * problems: key-finding, other,... (?)
	 */
	private int value; //key finding,...
	
	/**
	 * how often is a problem prevalent in a diagnosis (rare, medium, often)
	 */
	private int qualifier;
	
	public RelationProblem(){}
	public RelationProblem(long sourceId, long destId){
		this.setSourceId(sourceId);
		this.setDestId(destId);
	}
	public long getSourceId() {return sourceId;}
	public void setSourceId(long sourceId) {this.sourceId = sourceId;}
	public long getDestId() {return destId;}
	public void setDestId(long destId) {this.destId = destId;}	
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}	
	public int getOrder() {return order;}
	public void setOrder(int order) {this.order = order;}	
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	
	public boolean equals(Object o){
		if(o!=null){
			if(o instanceof RelationProblem && ((RelationProblem)o).getSourceId()==this.sourceId && ((RelationProblem)o).getDestId()==this.destId)
				return true;
		}
		return false;
	}
}
