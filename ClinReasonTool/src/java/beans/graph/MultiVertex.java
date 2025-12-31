package beans.graph;

import java.io.Serializable;

import beans.relation.Rectangle;
import beans.relation.Relation;
import beans.relation.RelationDiagnosis;
import beans.scripts.IllnessScriptInterface;
import controller.NavigationController;
import beans.list.ListItem;
/**
 * This is a vertex container, that can contains from which soure this vertex has been added.
 * @author ingahege
 *
 */
public class MultiVertex /*extends SynonymVertex*/ implements VertexInterface, Serializable{

	private static final long serialVersionUID = 1L;
	public static final int VERTEX_TYPE_DIRECT = 1; //expert/learner has chosen this vertex
	public static final int VERTEX_TYPE_HIERARCHY = 2;
	//public static final int VERTEX_TYPE_CHILD = 2; //the vertex is added because it is a parent/child item of the expert's choice.
	//public static final int VERTEX_TYPE_PARENT = 3; //the vertex is added because it is a parent/child item of the expert's choice.

	/**
	 * how many peers have added this item (e.g. Problem) to their PatientIllnessScript, this includes all synonyma
	 */
	private int peerNums; 
	private Relation learnerVertex; //e.g. RelationProblem of learner (can include a synonym)s
	private Relation expertVertex;
	//private Relation illScriptVertex;
	private String label;
	private long vertexId; //list_id (e.g. of Mesh item)
	/**
	 * Problem, DDX,... see definitions in Relation
	 */
	private int type; 
	
	private int box;
	
	/**
	 * see definition above, we need this here so that we do not display hierarchy vertices unless chosen by the learner. 
	 */
	private int vertexType = VERTEX_TYPE_DIRECT;
	
	public MultiVertex(){}
	
	public MultiVertex(Relation rel, int illnessScriptType, int box){
		setType(rel.getRelationType());
		setLabel(rel.getLabel());
		this.vertexId = rel.getListItemId();
		this.addRelation(rel, illnessScriptType);
		this.box = box;
	}
	
	public MultiVertex(ListItem li, int illnessScriptType, int type, int box){
		this.type = type;
		this.label = li.getName();
		this.vertexId = li.getItem_id();
		vertexType = VERTEX_TYPE_HIERARCHY;
		this.box = box;
	}
		
	public int getBox() {return box;}
	public void setBox(int box) {this.box = box;}

	/**
	 * We add the Relation object depending on the illScriptType. 
	 * If an expert or illscript Relation has already by added, we do NOT update. the learner Relation is always updated. 
	 * @param rel
	 * @param illnessScriptType
	 */
	public void addRelation(Relation rel, int illnessScriptType){
		if(/*learnerVertex==null && */ illnessScriptType==IllnessScriptInterface.TYPE_LEARNER_CREATED)
			learnerVertex = rel;
		if(expertVertex==null && illnessScriptType==IllnessScriptInterface.TYPE_EXPERT_CREATED)
			expertVertex = rel;
		//if(illScriptVertex==null && illnessScriptType==IllnessScriptInterface.TYPE_ILLNESSSCRIPT)
		//	illScriptVertex = rel;
		/*if(illnessScriptType==IllnessScriptInterface.TYPE_LEARNER_CREATED)
			learnerAdded = true;
		if(illnessScriptType==IllnessScriptInterface.TYPE_EXPERT_CREATED)
			expAdded = true;
		if(illnessScriptType==IllnessScriptInterface.TYPE_ILLNESSSCRIPT)
			illScriptAdded = true;*/
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		if(o !=null && o instanceof MultiVertex && ((MultiVertex)o).getVertexId()==getVertexId()) 
				return true;		
		return false;
	}
	
	/**
	 * We check whether the multiVertex already contains the VertexInterface (as learner-, expert-, or IllnessScript
	 * vertex.
	 * @param vertexIF
	 * @return
	 */
	public Relation getRelationByType(int illScriptType){
		if(illScriptType==IllnessScriptInterface.TYPE_LEARNER_CREATED) return learnerVertex;
		if(illScriptType==IllnessScriptInterface.TYPE_EXPERT_CREATED) return expertVertex;
		//if(illScriptType==IllnessScriptInterface.TYPE_ILLNESSSCRIPT) return illScriptVertex;
		/*if(expertVertex!=null && expertVertex.equals(rel)) return true;
		if(illScriptVertex!=null && illScriptVertex.equals(rel)) return true;
		return false;*/ 
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getLabel()+" ("+getVertexId()+"), learner: "+ isLearnerVertex() + ", exp: " + isExpertVertex();

		//return getLabel()+" ("+getVertexId()+"), learner: "+ isLearnerAdded() + ", exp: " + isExpAdded() + ", illscript: " + isIllScriptAdded();
	}
	
	public boolean isLearnerVertex(){
		if(learnerVertex==null) return false;
		return true;
	}
	public boolean isExpertVertex(){
		if(expertVertex==null) return false;
		return true;
	}
	
	/**
	 * has the expert added this vertex at the current stage?
	 * @param stage
	 * @return
	 */
	public boolean isExpertVertexAtStage(int stage){
		if(expertVertex==null) return false;
		if(expertVertex.getStage()<= stage) return true;
		return false;
	}
	
	/*public boolean isIllScriptVertex(){
		if(illScriptVertex==null) return false;
		return true;
	}*/
	public int getPeerNums() {return peerNums;}
	/*public Relation getLearnerVertex() {return learnerVertex;}
	public Relation getExpertVertex() {return expertVertex;}
	public Relation getIllScriptVertex() {return illScriptVertex;}	*/

	public long getVertexId() {return vertexId;}
	public void setVertexId(long vertexId) {this.vertexId = vertexId;}
	public Relation getLearnerVertex() {return learnerVertex;}
	public void setLearnerVertex(Relation learnerVertex) {this.learnerVertex = learnerVertex;}
	public Relation getExpertVertex() {return expertVertex;}
	public void setExpertVertex(Relation expertVertex) {this.expertVertex = expertVertex;}
	public String getLabel() {return label;}
	public void setLabel(String label) {this.label = label;}
	/**
	 * Problem, DDX,... see definitions in Relation
	 */
	public int getType() {return type;}
	public void setType(int type) {this.type = type;}	
	
	/**
	 * direct or hierarchy type, etc...
	 * @return
	 */
	public int getVertexType() {return vertexType;}
	public void setVertexType(int vertexType) {this.vertexType = vertexType;}

	public String toJson(){
		StringBuffer sb = new StringBuffer();
		int currentStage = new NavigationController().getMyFacesContext().getPatillscript().getCurrentStage();

		if(learnerVertex!=null && expertVertex==null )
			sb.append(createLearnerJson());
		else if(learnerVertex!=null && expertVertex!=null)
			sb.append(createLearnerAndExpertJson(currentStage));
			else if(learnerVertex==null && expertVertex!=null)	
				sb.append(createExpertJson(currentStage));

		return sb.toString();
	}
	
	/**
	 * Learner and expert have chosen the item.
	 * @param currentStage
	 * @return
	 */
	private String createLearnerAndExpertJson(int currentStage){
		StringBuffer sb = new StringBuffer();
		sb.append("{\"label\":\""+learnerVertex.getLabelOrSynLabel()+"\",");
		sb.append("\"shortlabel\":\""+learnerVertex.getShortLabelOrSynShortLabel()+"\",");
		sb.append("\"id\":\"box"+box+"_"+learnerVertex.getId()+"\",");
		sb.append("\"x\":\""+((Rectangle)learnerVertex).getX()+"\",");
		sb.append("\"y\":\""+((Rectangle)learnerVertex).getY()+"\",");	
		sb.append("\"type\":\""+learnerVertex.getRelationType()+"\",");
		sb.append("\"l\":\"1\",");
		//if the learner has already picked the item we do not check the experts' stage, but just display it.
		if(isExpertVertexAtStage(currentStage) || (isExpertVertex() && isLearnerVertex())) sb.append("\"e\":\"1\",");
		else sb.append("\"e\":\"0\",");
		sb.append("\"p\":\""+this.peerNums+"\"");
		if(learnerVertex.getRelationType()==Relation.TYPE_DDX ){
			sb.append(", \"mnm\":\""+((RelationDiagnosis) learnerVertex).getMnm() +"\"");				
		}
	
		sb.append("},");
		return sb.toString();
	}
	
	/**
	 * Only expert has chosen the item at this stage
	 * @param currentStage
	 * @return
	 */
	private String createExpertJson(int currentStage){
		if(!isExpertVertexAtStage(currentStage)) return ""; //learner has not yet reached the stage where the expert has entered this item, so we do not display it.
		StringBuffer sb = new StringBuffer();
		sb.append("{\"label\":\""+expertVertex.getLabelOrSynLabel()+"\",");
		sb.append("\"shortlabel\":\""+expertVertex.getShortLabelOrSynShortLabel()+"\",");
		sb.append("\"id\":\"box"+this.box+"_"+expertVertex.getId()+"\",");
		sb.append("\"x\":\""+((Rectangle)expertVertex).getX()+"\",");
		sb.append("\"y\":\""+((Rectangle)expertVertex).getY()+"\",");	
		sb.append("\"type\":\""+expertVertex.getRelationType()+"\",");
		sb.append("\"l\":\"0\",");
		sb.append("\"e\":\"1\",");
		
		sb.append("\"p\":\""+this.peerNums+"\"");
		if(expertVertex.getRelationType()==Relation.TYPE_DDX ){
			sb.append(", \"mnm\":\""+((RelationDiagnosis) expertVertex).getMnm() +"\"");				
		}
		sb.append("},");
		return sb.toString();
	}
	/**
	 * Creates the learnerVertex without any experts solution (expert did not choose this item)
	 * @return
	 */
	private String createLearnerJson(){
		StringBuffer sb = new StringBuffer();
		if(learnerVertex!=null){
			//if learner has chosen the item, we alsways display the learners labels (could be a synonm)
			sb.append("{\"label\":\""+learnerVertex.getLabelOrSynLabel()+"\",");
			sb.append("\"shortlabel\":\""+learnerVertex.getShortLabelOrSynShortLabel()+"\",");
			sb.append("\"id\":\"box"+box+"_"+learnerVertex.getId()+"\",");
			sb.append("\"x\":\""+((Rectangle)learnerVertex).getX()+"\",");
			sb.append("\"y\":\""+((Rectangle)learnerVertex).getY()+"\",");	
			sb.append("\"type\":\""+learnerVertex.getRelationType()+"\",");
			 sb.append("\"l\":\"1\",");
			sb.append("\"e\":\"0\",");
			sb.append("\"p\":\""+this.peerNums+"\"");
			if(learnerVertex.getRelationType()==Relation.TYPE_DDX ){
				sb.append(", \"mnm\":\""+((RelationDiagnosis) learnerVertex).getMnm() +"\"");				
			}
		}
		sb.append("},");
		return sb.toString();
	}
	
	public int getStage(){
		if(learnerVertex!=null) return learnerVertex.getStage();
		return -1;
	}
	
	
}
