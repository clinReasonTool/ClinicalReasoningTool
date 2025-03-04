package beans.relation;

import java.awt.Point;
import java.beans.Beans;
import java.io.Serializable;
import java.sql.Timestamp;

import beans.graph.MultiEdge;

/**
 * Connection in the concept map between Relation objects, e.g. ProblemRelation -> DiagnosisRelation. 
 * CAVE: start- and targetIds are the ids of the Relations NOT the listItems! 
 * @author ingahege
 *
 */
/**
 * @author ingahege
 *
 */
public class Connection extends Beans implements Serializable, Comparable<Connection>{
	
	public static final int WEIGHT_NONE = MultiEdge.WEIGHT_NONE;	
	public static final int WEIGHT_SLIGHTLY_RELATED = MultiEdge.WEIGHT_SLIGHTLY_RELATED;
	public static final int WEIGHT_SOMEWHAT_RELATED = MultiEdge.WEIGHT_SOMEWHAT_RELATED;
	public static final int WEIGHT_HIGHLY_RELATED = MultiEdge.WEIGHT_HIGHLY_RELATED;
	public static final int WEIGHT_SPEAKS_AGAINST = MultiEdge.WEIGHT_SPEAKS_AGAINST;
	public static final int WEIGHT_HIERARCHY = MultiEdge.WEIGHT_EXPLICIT_HIERARCHY;
	public static final int WEIGHT_SYNDROME = MultiEdge.WEIGHT_SYNDROME;
	

	private static final long serialVersionUID = 1L;
	/**
	 * just an internal id
	 */
	private long id;
	/**
	 * id of a Relation (e.g. a ProblemRelation) object
	 */
	private long startId; 
	/**
	 * id of a Relation (e.g. a DiagnosisRelation) object
	 */
	private long targetId;
	private long illScriptId;
	//private String label; 
	//private String color; //define default color
	private Timestamp creationDate;
	private int startType; //see definitions in ConceptMapController
	private int targetType; //see definitions in ConceptMapController
	private int weight; 
	/**
	 * stage at which connection will be displayed.
	 */
	private int stage = -1;
	/**
	 * if we have multiple endpoints for item boxes we have to store the idx of the start point used for this connection
	 */
	private int startEpIdx = 0;
	/**
	 * if we have multiple endpoints for item boxes we have to store the idx of the target point used for this connection
	 */
	private int targetEpIdx = 0;
	/** exact positioning of the target ep (now the whole node can be a target) **/
	private int targetEpX = -1;
	private int targetEpY = -1;
	
	/**
	 * only use temporarily for import - not stored in database!
	 */
	private String concept1;
	
	/**
	 * only use temporarily for import - not stored in database!
	 */
	private String concept2; 
	/**
	 * only use temporarily for import - not stored in database!
	 */
	private int order;
	
	public Connection(){}
	public Connection(long startId, long targetId, long illScriptId, int startType, int targetType, int stage){
		this.startId = startId;
		this.targetId = targetId;
		this.illScriptId = illScriptId;
		this.startType = startType;
		this.targetType = targetType;
		this.stage = stage;
	}
	
	public long getStartId() {return startId;}
	public void setStartId(long startId) {this.startId = startId;}
	public long getTargetId() {return targetId;}
	public void setTargetId(long targetId) {this.targetId = targetId;}
	public long getIllScriptId() {return illScriptId;}
	public void setIllScriptId(long illScriptId) {this.illScriptId = illScriptId;}
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}
	public Timestamp getCreationDate() {return creationDate;}
	public void setCreationDate(Timestamp creationDate) {this.creationDate = creationDate;}			
	public int getStartType() {return startType;}
	public void setStartType(int startType) {this.startType = startType;}
	public int getTargetType() {return targetType;}
	public void setTargetType(int targetType) {this.targetType = targetType;}	
	public int getWeight() {return weight;}
	public void setWeight(int weight) {this.weight = weight;}	
	public int getStage() {return stage;}
	public void setStage(int stage) {this.stage = stage;}	
	public int getStartEpIdx() {return startEpIdx;}
	public void setStartEpIdx(int startEpIdx) {this.startEpIdx = startEpIdx;}
	public int getTargetEpIdx() {return targetEpIdx;}
	public void setTargetEpIdx(int targetEpIdx) {this.targetEpIdx = targetEpIdx;}	
	public int getTargetEpX() {return targetEpX;}
	public void setTargetEpX(int targetEpX) {this.targetEpX = targetEpX;}
	public int getTargetEpY() {return targetEpY;}
	public void setTargetEpY(int targetEpY) {this.targetEpY = targetEpY;}	
	public String getConcept1() {return concept1;}
	public void setConcept1(String concept1) {this.concept1 = concept1;}
	public String getConcept2() {return concept2;}
	public void setConcept2(String concept2) {this.concept2 = concept2;}
	public int getOrder() {return order;}
	public void setOrder(int order) {this.order = order;}
	
	public void setTargetEndpoint(Point p){
		if(p==null) return;
		setTargetEpX(p.x);
		setTargetEpY(p.y);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		if(o!=null){
			if(o instanceof Connection && ((Connection)o).getId()==this.id) return true;
			if(o instanceof Connection && ((Connection)o).getStartId()==this.startId && ((Connection)o).getTargetId()==this.targetId) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Connection o) {
		if(o instanceof Connection){
			Connection cnx = (Connection) o;
			if(this.getId() < cnx.getId()) return -1;
			if(this.getId() > cnx.getId()) return 1;
			if(this.getId() == cnx.getId()) return 0;			
		}		
		return 0;
	}
	
}
