package beans.graph;

import java.util.*;

import javax.faces.bean.SessionScoped;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import application.AppBean;
import beans.scripts.*;
import beans.relation.Connection;
import beans.relation.Relation;
import controller.*;
import database.DBList;
import beans.list.ListItem;
import util.CRTLogger;
import util.StringUtilities;

/**
 * A Graph that models the components of (Patient-)IllnessScripts in a MultiGraph. 
 * Vertices (Multivertex) are Problems, Diagnoses, Tests, and Management options. Edges (MultiEdge) are explicit 
 * connections made in the concept map/or stemming from an IllnessScript or implicit connections made by putting 
 * e.g. referencing a Problem and a Diagnosis in a PatientIllnessScript.
 * CAVE: Currently it is not a MultiGraph, because we subsumed the multiple edges into a MultiEdge
 * @author ingahege
 *
 */
@SessionScoped
public class Graph extends DirectedWeightedMultigraph<MultiVertex, MultiEdge> {

	private static final long serialVersionUID = 1L;
	private String vpId; //e.g. VPId,...
	/**
	 * We need this here in addition to the vpId, because we allow redoing a session so there might 
	 * be multiple scripts for one VPId and it is no longer unique 
	 */
	private long patIllScriptId;
	private long expertPatIllScriptId;
	private boolean expEdit = false; 
	/**
	 * How many peers have completed this patientIllnessScript? If we have enough we can include the peers into 
	 * the scoring algorithm.
	 */
	private int peerNums; //we might need this for the scoring process?
	private List<Long> illScriptIds;//TODO: more than one! 
	private GraphController gctrl;
	
	/**
	 * Called from Admin Interface to set the expedit param to true
	 * @param vpId
	 * @param isExpEdit
	 */
	public Graph(String vpId, boolean isExpEdit, long  patIllScriptId){
		super(MultiEdge.class);
		this.expEdit = isExpEdit;
		initGraph(vpId, patIllScriptId);
	}
	public Graph(String vpId, long  patIllScriptId){
		super(MultiEdge.class);
		initGraph(vpId, patIllScriptId);		
	}
	
	private void initGraph(String vpId, long patIllScriptId){
		this.vpId = vpId;
		this.patIllScriptId = patIllScriptId;
		gctrl = new GraphController(this);
		gctrl.addExpPatIllScript(vpId);
		gctrl.addLearnerPatIllScript();
		gctrl.addIllnessScripts(vpId);
		
	}
		
	public long getExpertPatIllScriptId() {return expertPatIllScriptId;}
	public void setExpertPatIllScriptId(long expertPatIllScriptId) {this.expertPatIllScriptId = expertPatIllScriptId;}
	public List<Long> getIllScriptIds() {return illScriptIds;}
	public void setIllScriptId(List<Long> illScriptIds) {this.illScriptIds = illScriptIds;}	
	public int getPeerNums() {return peerNums;}
	public void setPeerNums(int peerNums) {this.peerNums = peerNums;}
	public String getVpId() {return vpId;}
	public void setExpEdit(boolean expEdit){ this.expEdit = expEdit;}
	public long getPatIllScriptId() {return patIllScriptId;}
	public void setPatIllScriptId(long patIllScriptId) {this.patIllScriptId = patIllScriptId;}
	public void addIllScriptId(long id){
		if(illScriptIds==null) illScriptIds = new ArrayList<Long>();
		if(!illScriptIds.contains(new Long(id))) illScriptIds.add(new Long(id));
	}
	
	/**
	 * We look for the Vertex for the given relation and remove the learnerVertex from it (only learnerVertices can be
	 * removed!!!). If there do not 
	 * @param rel
	 */
	public void removeMultiVertex(Relation rel){
		MultiVertex vertex = this.getVertexByIdAndType(rel.getListItemId(), rel.getRelationType());
		if(vertex==null) return; //Should not happen
		vertex.setLearnerVertex(null);
		//Shall we remove the vertex from the graph if there is no relation attached? (we still might have some peer nums)
		//then also the edges would be removed automatically.
	
	}
	
	/**
	 * We change the edge weight for this illnessScriptType to implicit if it was explicit. This needs to be done
	 *  when a user deletes a connection from the concept map.
	 *  Can only be called for learner scripts!
	 * @param cnx
	 */
	public void removeExplicitEdgeWeight(long cnxId){
		MultiEdge edge = getEdgeByCnxId(IllnessScriptInterface.TYPE_LEARNER_CREATED, cnxId);//this.getEdge(this.getVertexById(sourceId), this.getVertexById(targetId));
		if(edge==null) return; //should not happen
		edge.removeExplicitWeight();
	}

	public MultiEdge getEdgeByCnxId(int illScriptType, long cnxId){
		if(this.edgeSet()==null) return null;
		Iterator<MultiEdge> it = this.edgeSet().iterator();
		while(it.hasNext()){
			MultiEdge edge = it.next();
			if(expEdit && edge.getLearnerCnxId()==cnxId) return edge;

			if(illScriptType == IllnessScriptInterface.TYPE_LEARNER_CREATED && edge.getLearnerCnxId()==cnxId) return edge;
			if(illScriptType == IllnessScriptInterface.TYPE_EXPERT_CREATED && edge.getExpCnxId()==cnxId) return edge;			
		}
		return null;
	}
	
	/**
	 * We remove the edge weight (explicit or implicit) from the edge from the source to the target MultiVertex. 
	 * This needs to be done when we remove a MultiVertex vertex for the given illnessScriptType component 
	 * @param sourceId
	 * @param targetId
	 * @param illScriptType
	 */
	public void removeEdgeWeight(long sourceId, long targetId){
		MultiEdge edge = this.getEdge(this.getVertexById(sourceId), this.getVertexById(targetId));
		if(edge==null) return; //should not happen
		edge.removeWeight(IllnessScriptInterface.TYPE_LEARNER_CREATED);
	}
	
	/**
	 * We can call this for any addAction, no matter of main ListItem or Synonym, 
	 * Check whether for this Relation a MultiVertex exists, if not create one. If yes, 
	 * we check whether this Relation has been added or needs to be updated (e.g. because the learner has now 
	 * changed from the synonym to the main ListItem entry. 
	 * @param rel ALWAYS the Relation containing the ListItem (optional with the synonymId)
	 * @param illnessScriptType
	 */
	public MultiVertex addVertex(Relation rel, int illScriptType, int boxNo){
		if(rel==null) return null;
		MultiVertex multiVertex = getVertexByIdAndType(rel.getListItemId(), rel.getRelationType());
		if(multiVertex==null){ //create a new one:
			multiVertex = new MultiVertex(rel, illScriptType, boxNo); 
			super.addVertex(multiVertex);
		}
		else{ //we only have to update the relation in the MultiVertex
			//Relation relInVertex = multiVertex.getRelationByType(illScriptType); 
			multiVertex.addRelation(rel, illScriptType); //relation not yet added			
		}
		if(illScriptType==IllnessScriptInterface.TYPE_EXPERT_CREATED)
			addParentAndChildVertices(multiVertex, boxNo);
		
		return multiVertex;
	}
	
	/**
	 * For the expert's script we add all parent and child nodes of the current one
	 * parent: higher in hierarchy (more general)
	 * child: lower in hierarchy (more specific)
	 */
	private void addParentAndChildVertices(MultiVertex vertex, int boxNo){
		if(vertex==null || vertex.getExpertVertex()==null) return;
		List<ListItem> items = new DBList().selectParentAndChildListItems(vertex.getExpertVertex().getListItem());
		if(items==null || items.isEmpty()) return;
		for(int i=0; i< items.size(); i++){
			MultiVertex relatedVertex = addVertex(items.get(i), vertex.getExpertVertex().getRelationType(), boxNo);
			if(vertex.getExpertVertex().getListItem().getFirstCode().length()<items.get(i).getFirstCode().length())
				addHierarchyEdge(vertex, relatedVertex); //vertex is a parent
			else //vertex is the child
				addHierarchyEdge(relatedVertex, vertex);
		}
	}
	
	/**
	 * direction  child -> parent with a special parent weight for the edge.
	 * @param sourcevertex
	 * @param parentvertex
	 */
	private void addHierarchyEdge(MultiVertex childvertex, MultiVertex parentvertex){
		if(childvertex==null || parentvertex==null) return;		
		MultiEdge e = getEdge(childvertex, parentvertex); 
		if(e==null){
			e = new MultiEdge(IllnessScriptInterface.TYPE_EXPERT_CREATED, MultiEdge.WEIGHT_PARENT); 
			addEdge(childvertex, parentvertex, e);
		}
		else e.addParam(IllnessScriptInterface.TYPE_EXPERT_CREATED, MultiEdge.WEIGHT_PARENT);
	}
	
	private MultiVertex addVertex(ListItem li, int type, int boxNo){
		MultiVertex multiVertex = getVertexByIdAndType(li.getItem_id(), type);
		if(multiVertex==null){ //create a new one:
			multiVertex = new MultiVertex(li, IllnessScriptInterface.TYPE_EXPERT_CREATED, type, boxNo); 
			super.addVertex(multiVertex);
		}
		return multiVertex;
		//else{} -> nothing to do!?! 
	}
	

	/**
	 * @param cnx
	 * @param patIllScript
	 * @param type (see definition in IllnessScriptInterface)
	 */
	public void addExplicitEdge(Connection cnx, PatientIllnessScript patIllScript, int type, int weight){
		Relation source = patIllScript.getRelationByIdAndType(cnx.getStartId(), cnx.getStartType());
		Relation target = patIllScript.getRelationByIdAndType(cnx.getTargetId(), cnx.getTargetType());
		//the weight has to be minimum of the explicit weight or a specified higher weight:s
		//int weight = MultiEdge.WEIGHT_EXPLICIT;
		if(cnx.getWeight()>MultiEdge.WEIGHT_EXPLICIT) weight = cnx.getWeight();
		if(source!=null && target!=null){
			MultiVertex s = getVertexByIdAndType(source.getListItemId(), source.getRelationType());
			MultiVertex t = getVertexByIdAndType(target.getListItemId(), target.getRelationType());
			MultiEdge edge = addOrUpdateEdge(s, t, type, weight, cnx, patIllScript.getType());
			
		}
		else if(source==null || target==null) {
			checkAndAddPassiveEdge(cnx, source, target, type, weight);
		}	
	}
	
	/**
	 * If one or both of the end nodes is a "passive" on, i.e. has been added by the expert and just displayed
	 * for the learner, we have to check in the expert map for this node. 
	 * @param cnx
	 * @param source
	 * @param target
	 */
	private void checkAndAddPassiveEdge(Connection cnx, Relation source, Relation target, int type, int weight) {
		if(source==null) {
			 PatientIllnessScript expIllScript = AppBean.getExpertPatIllScript(vpId);
			 if(expIllScript!=null)
				 source = expIllScript.getRelationByIdAndType(cnx.getStartId(), cnx.getStartType());
			 
		}
		if(target==null) {
			 PatientIllnessScript expIllScript = AppBean.getExpertPatIllScript(vpId);
			 if(expIllScript!=null)
				 target = expIllScript.getRelationByIdAndType(cnx.getTargetId(), cnx.getTargetType());
			 
		}
		if(source!=null && target!=null) {
			MultiEdge edge = addOrUpdateEdge(getVertexByIdAndType(source.getListItemId(), source.getRelationType()), getVertexByIdAndType(target.getListItemId(), target.getRelationType()), type, weight, cnx, PatientIllnessScript.TYPE_LEARNER_CREATED);
		}
	}
	
	public void addExplicitEdge(Connection cnx, PatientIllnessScript patIllScript, int type){
		addExplicitEdge(cnx, patIllScript, type, MultiEdge.WEIGHT_EXPLICIT);
	}
	
	
	
	public void addImplicitEdge(long sourceId, long targetId, int type){
		addOrUpdateEdge(this.getVertexById(sourceId), this.getVertexById(targetId), type, MultiEdge.WEIGHT_IMPLICIT, null, -1);
	}
	
	/**
	 * creates and adds a MultiEgde to a grah or of the edge has already been created it does just add the 
	 * new type/weight parameter to the MultiEdge
	 * @param source (e.g. a RelationProblem vertex)
	 * @param target (e.g a RelationDiagnosis vertex)
	 * @param type (see definitions in IllnessScriptInterface)
	 * @param weight (implicit or explicit - see defintions in MultiEdge)
	 * @param expEdit (if true, then currently an expert script is edited, so, the expert connections are the learner conns!!!
	 * @return
	 */
	private MultiEdge addOrUpdateEdge(MultiVertex source, MultiVertex target, int type, int weight, Connection conn, int patIllScriptType){
		if(source==null || target==null)
			return null;
		
		MultiEdge e = getEdge(source, target); 
		if(e==null){
			e = new MultiEdge(type, weight); 
			addEdge(source, target, e);
		}
		else e.addParam(type, weight);
		if(conn!=null && patIllScriptType==IllnessScriptInterface.TYPE_LEARNER_CREATED) e.setLearnerCnx(conn);//e.setLearnerCnxId(cnxId);
		
		if(conn!=null && patIllScriptType==IllnessScriptInterface.TYPE_EXPERT_CREATED && !expEdit) e.setExpertCnx(conn);
		if(conn!=null && patIllScriptType==IllnessScriptInterface.TYPE_EXPERT_CREATED && expEdit) e.setLearnerCnx(conn);
		return e; //edge created or we have changed the params of it.
		
	}

	/**
	 * @param vertexId
	 * @return
	 * @deprecated -> we need id AND type, since a vertex can be added for more than one group (e.g. as fdg AND ddx)
	 * use getVertexByIdAndType instead!!!
	 */
	public MultiVertex getVertexById(long vertexId){
		Iterator<MultiVertex> it = this.vertexSet().iterator();
		while(it.hasNext()){
			MultiVertex vi = it.next();
			if(vi.getVertexId()==vertexId) return vi;
		}
		return null;
	}
	
	public MultiVertex getVertexByIdAndType(long vertexId, int type){
		Iterator<MultiVertex> it = this.vertexSet().iterator();
		while(it.hasNext()){
			MultiVertex vi = it.next();
			if(vi.getVertexId()==vertexId && vi.getType()==type) return vi;
		}
		return null;
	}
	
	/**
	 * Get all MultiVertex objects of the given type (e.g. Diagnosis, Problem,...)
	 * @param type (see definitions in Relation)
	 * @return List<MultiVertex> or null
	 */
	public List<MultiVertex> getVerticesByType(int type){
		Set<MultiVertex> verts = this.vertexSet();
		if(verts==null) return null;
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		Iterator<MultiVertex> it = verts.iterator();
		while(it.hasNext()){
			MultiVertex mv = it.next();
			if(mv.getType()==type) list.add(mv);				
		}
		return list;
	}
	
	/**
	 * Get all MultiVertex objects of the given type (e.g. Diagnosis, Problem,...), which have 
	 * only been selected by the expert (for joker handling)
	 * @param type (see definitions in Relation)
	 * @return List<MultiVertex> or null
	 */
	public List<MultiVertex> getVerticesByTypeAndStageExpOnly(int type, int stage){
		Set<MultiVertex> verts = this.vertexSet();
		if(verts==null) return null;
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		Iterator<MultiVertex> it = verts.iterator();
		while(it.hasNext()){
			MultiVertex mv = it.next();
			if(mv.getType()==type && mv.getExpertVertex()!=null && mv.getExpertVertex().getStage()<= stage && !mv.isLearnerVertex()) 
				list.add(mv);				
		}
		return list;
	}
	
	/**
	 * gets all expert vertices that are syndromes or part of syndromes (depending on syndrType)
	 * @return
	 */
	public List<MultiVertex> getVerticesSyndromeExpOnly(int syndrType){
		Set<MultiVertex> verts = this.vertexSet();
		if(verts==null) return null;
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		Iterator<MultiVertex> it = verts.iterator();
		while(it.hasNext()){
			MultiVertex mv = it.next();
			if(mv.getExpertVertex()!=null && mv.getExpertVertex().getIsSyndrome()==syndrType)
				list.add(mv);
		}
		return list;
	}
	
	/**
	 * Returns a list of vertices that have been added by the expert (and not the learner) in the given stage interval 
	 * (including start- and endstage). We use that for determination of premature closure errors.
	 * @param type
	 * @param startStage
	 * @param endStage
	 * @return
	 */
	public List<MultiVertex> getVerticesByTypeAndStageRangeExpOnly(int type, int startStage, int endStage){
		Set<MultiVertex> verts = this.vertexSet();
		if(verts==null) return null;
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		Iterator<MultiVertex> it = verts.iterator();
		while(it.hasNext()){
			MultiVertex mv = it.next();
			if(mv.getType()==type && mv.getExpertVertex()!=null && !mv.isLearnerVertex()){
				if(mv.getExpertVertex().getStage() >= startStage && mv.getExpertVertex().getStage()<=endStage)
					list.add(mv);
			}								
		}
		return list;
	}
	public List<MultiVertex> getVerticesByTypeAndStageRangeExp(int type, int startStage, int endStage){
		Set<MultiVertex> verts = this.vertexSet();
		if(verts==null) return null;
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		Iterator<MultiVertex> it = verts.iterator();
		while(it.hasNext()){
			MultiVertex mv = it.next();
			if(mv.getType()==type && mv.getExpertVertex()!=null){
				if(mv.getExpertVertex().getStage() >= startStage && mv.getExpertVertex().getStage()<=endStage)
					list.add(mv);
			}								
		}
		return list;
	}

	/**
	 * Returns all vertices added by the expert. 
	 * Needed/used for Expert script creation
	 * @return
	 */
	public List<MultiVertex> getAllVertices(){
		Set<MultiVertex> verts = this.vertexSet();
		if(verts==null) return null;
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		Iterator<MultiVertex> it = verts.iterator();
		while(it.hasNext()){
			MultiVertex mv = it.next();
			if( mv.getLearnerVertex()!=null)  list.add(mv);				
		}
		return list;
	}
	/**
	 * Returns all edges added by the expert. 
	 * Needed/used for Expert script creation
	 * @return
	 */	
	public List<MultiEdge> getAllEdges(){
		Set<MultiEdge> edges = this.edgeSet();
		if(edges==null || edges.isEmpty()) return null;
		List<MultiEdge> expEdges = new ArrayList<MultiEdge>();
		Iterator<MultiEdge> it = edges.iterator();
		while(it.hasNext()){
			MultiEdge e = it.next();
			if(e.isExplicitLearnerEdge()) expEdges.add(e);
		}
		
		return expEdges;
	}
	
	/* (non-Javadoc)
	 * @see org.jgrapht.graph.AbstractGraph#toString()
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Graph: parent_id = " + this.vpId + ", vertices[ ");
		if(this.vertexSet()!=null){
			Iterator<MultiVertex> it = this.vertexSet().iterator();
			while(it.hasNext()){
				sb.append(it.next().toString() +";\n ");
			}
			sb.append("]\n");
		}
		if(this.edgeSet()!=null){
			sb.append("Edges[ ");
			Iterator<MultiEdge> it = this.edgeSet().iterator();
			while(it.hasNext()){
				MultiEdge e = it.next();
				if(e.isExplicitExpertEdge() || e.isExplicitLearnerEdge()) sb.append(e.toString() +"; ");
			}
			sb.append("]");
		}
		return sb.toString();
	}	
		
	/**
	 * 
	 * Format: {"label":"Cough","shortlabel":"Cough","id":"12345","x": "10","y":"200", "type":"1", "l":"1", "e":"0", "p":"23"}");	
	 * l = learner (1 = added, 0 = not added)
	 * e = expert ( ")
	 * p = peer nums
	 * 
	 * 
	 * @return learners' patIllScript
	 */
	public String getToJson(){
		Set<MultiVertex> vertices = this.vertexSet();
		if(vertices==null || vertices.isEmpty()) return "[]"; 
		Iterator<MultiVertex> it = vertices.iterator();
		StringBuffer sb = new StringBuffer("[");
		
		while(it.hasNext()){
			MultiVertex mv = it.next();
			//Rectangle learnerRel = (Rectangle) mv.getLearnerVertex();
			//if(learnerRel!=null)
				sb.append(mv.toJson());
		}
		if(sb.length()>1) sb.replace(sb.length()-1, sb.length(), ""); //remove the last ","
		sb.append("]");
		//CRTLogger.out(sb.toString(), CRTLogger.LEVEL_TEST);
		return sb.toString();
	}
	
	/**
	 * format: [{"id":"cmcnx_1", "sourceid":"cmprb_1234","targetid":"cmddx_47673", "l":"0", "e":"1", weight_l":"3","weight_e":"4"},....]
	 * @return
	 */
	public String getJsonConns(){
		Set<MultiEdge> edges = this.edgeSet();
		if(edges==null || edges.isEmpty()) return "[]";
		Iterator<MultiEdge> it = edges.iterator();
		StringBuffer sb = new StringBuffer("[");
		while(it.hasNext()){
			MultiEdge edge = it.next();
			boolean showExpEdge = true;
			//if((edge.getLearnerWeight()>=MultiEdge.WEIGHT_EXPLICIT && edge.getLearnerWeight()<MultiEdge.WEIGHT_PARENT) || (edge.getExpertWeight()>=MultiEdge.WEIGHT_EXPLICIT && edge.getExpertWeight()<MultiEdge.WEIGHT_PARENT)){ //then we add the edge to the concept map
			if(edge.isExplicitExpertEdge() || edge.isExplicitLearnerEdge()){
				long cnxId = 0;
				String l = "0";
				String e = "0";

				if(edge.getLearnerCnxId()>0){
					cnxId = edge.getLearnerCnxId();
					l = "1";
				}
				else cnxId = edge.getExpCnxId();
				if(edge.getExpCnxId()>0){
					e = "1";
				}
				
				//if(this.expEdit || (edge.getLearnerCnxId()<=0 && edge.getExpertCnx()!=null)){ //then it is only an expert edge and we 
				int currentStage = NavigationController.getInstance().getMyFacesContext().getPatillscript().getCurrentStage();
				if(expEdit) {
					currentStage = NavigationController.getInstance().getMyFacesContext().getPatillscript().getStage();
					if(AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_REPORTS_DISPLAYMODE, 0)==1) 
						currentStage = NavigationController.getInstance().getMyFacesContext().getPatillscript().getCurrentStage();
				}
			
				if(expEdit && edge.getLearnerCnx()!=null && edge.getLearnerCnx().getStage()>currentStage) 
					showExpEdge = false;
				
				//else if(expEdit && AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_REPORTS_DISPLAYMODE, 0)==1)
				//	showExpEdge = true;
				
				else if(edge.getExpertCnx()!=null && edge.getLearnerCnx()==null && edge.getExpertCnx().getStage()>0 && edge.getExpertCnx().getStage()>currentStage) 
					showExpEdge = false;
				//}
				MultiVertex sourceVertex = edge.getSource();
				MultiVertex targetVertex = edge.getTarget();
				if((e.equals("1") && l.equals("0"))){ //only expert cnx, do not include connections for which the vertices have not yet been added
					//TODO! currently we do that client side...
				}
				String startIdWithPrefix=null;
				String targetIdWithPrefix=null;
				//if we have a vertex selected by learner (and expert) we always use the source/target-id of the learner vertex. 
				//only if a vertex has been selected only by the expert we use the expert id...
				if(sourceVertex.getLearnerVertex()!=null) {
					startIdWithPrefix = GraphController.getPrefixByType(sourceVertex.getBox())+sourceVertex.getLearnerVertex().getId(); //GraphController.getPrefixByType(sourceVertex.getType())+sourceVertex.getLearnerVertex().getId(); 	
				}
				else if(sourceVertex.getExpertVertex()!=null){
					startIdWithPrefix = GraphController.getPrefixByType(sourceVertex.getType())+sourceVertex.getExpertVertex().getId(); 	
				}
				if(targetVertex.getLearnerVertex()!=null){
					targetIdWithPrefix = GraphController.getPrefixByType(targetVertex.getType())+targetVertex.getLearnerVertex().getId();
				}
				else if(targetVertex.getExpertVertex()!=null){
					//startIdWithPrefix = GraphController.getPrefixByType(sourceVertex.getType())+sourceVertex.getExpertVertex().getId(); 	
					targetIdWithPrefix = GraphController.getPrefixByType(targetVertex.getType())+targetVertex.getExpertVertex().getId();
				}
				if(cnxId>0 && startIdWithPrefix!=null && targetIdWithPrefix!=null && showExpEdge){
					sb.append("{\"id\":\""+GraphController.PREFIX_CNX + cnxId+"\",");
					sb.append("\"l\":\""+l+"\", \"e\":\""+e+"\",");
					sb.append("\"sourceid\": \""+startIdWithPrefix+"\",\"targetid\": \""+targetIdWithPrefix+"\",");
					sb.append("\"weight_l\": \""+edge.getLearnerWeight()+"\", \"weight_e\": \""+edge.getExpertWeight()+"\",");
					sb.append("\"start_ep\": \""+edge.getStartEpIdx()+"\",");
					sb.append("\"target_ep\": \""+edge.getTargetEpIdx()+"\",");	
					sb.append("\"target_x\": \""+edge.getTargetX()+"\",");	
					sb.append("\"target_y\": \""+edge.getTargetY()+"\",");	
					//we append the stage for the display in view mode (reports) to show stage as a number
					if(edge.getLearnerCnx()!=null)
						sb.append("\"stage\": \""+edge.getLearnerCnx().getStage()+"\"},");
					else
						sb.append("\"stage\": \""+"-1"+"\"},");
				}
			}	
		}
		if(sb.length()>1) sb.replace(sb.length()-1, sb.length(), ""); //remove the last ","
		sb.append("]");
		//CRTLogger.out(sb.toString(), CRTLogger.LEVEL_TEST);
		return sb.toString();
	}
	
	public List<MultiVertex> getParentVertices(MultiVertex vertex){
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		getParentVertices(list, vertex);
		if(list.isEmpty()) return null;
		return list;
	}
	
	/**
	 * look whether the expert has chosen a more general term than the learner, if so return the vertex.
	 * @param lvertex
	 * @return
	 */
	public MultiVertex getExpParentVertex(MultiVertex lvertex){
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		getParentVertices(list, lvertex);
		if(list.isEmpty()) return null;
		for(int i=0; i<list.size(); i++){
			MultiVertex v = list.get(i);
			if(v.isExpertVertex()) return v;
		}
		return null;
	}
	
	
	/**
	 * returns the distance between two vertices
	 * @param source
	 * @param target
	 * @return
	 */
	public int getDistance(MultiVertex source, MultiVertex target){
		DijkstraShortestPath shortestPath = new DijkstraShortestPath(this, source, target);
		return (int) shortestPath.getPathLength();
	}
	
	/**
	 * TODO: we should be able to get the hierarchy and distance from the graph! For this the implicit edges 
	 * inclusion needs to be more tested and reliable!
	 * (-) learner is less specific than expert
	 * (+) learner is more specific than expert
	 * @param learnerVertex
	 * @param expVertex
	 * @return
	 */
	public int getHierarchyDistance(MultiVertex learnerVertex, MultiVertex expVertex){
		try{
			if(learnerVertex==null || expVertex==null) return (int )Double.POSITIVE_INFINITY;	
			if(learnerVertex.getLearnerVertex()==null || expVertex.getExpertVertex()==null) return (int )Double.POSITIVE_INFINITY;
			if(learnerVertex.getLearnerVertex().getListItem()==null || expVertex.getExpertVertex().getListItem()==null) return (int )Double.POSITIVE_INFINITY;
			
			int dist = learnerVertex.getLearnerVertex().getListItem().getHierarchyDiff(expVertex.getExpertVertex().getListItem());
			//int dist2 = expVertex.getExpertVertex().getListItem().getHierarchyDiff(learnerVertex.getLearnerVertex().getListItem());
			return dist;
		}
		catch(Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
			return (int )Double.POSITIVE_INFINITY; 
		}
	}


	/**
	 * look whether the expert has chosen a more specific term than the learner, if so return the vertex.
	 * Can be more than one!!!! Therefore we return a list! 
	 * @param lvertex
	 * @return
	 */
	public List<MultiVertex> getExpChildVertices(MultiVertex lvertex){
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		getChildVertices(list, lvertex);
		List<MultiVertex> childs = new ArrayList<MultiVertex>();
		if(list.isEmpty()) return null;
		for(int i=0; i<list.size(); i++){
			MultiVertex v = list.get(i);
			if(v.isExpertVertex()) childs.add(v);
		}
		return childs;
	}
	
	
	public List<MultiVertex> getChildVertices(MultiVertex vertex){
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		getChildVertices(list, vertex);
		if(list.isEmpty()) return null;
		return list;
	}
	
	/**
	 * recursive method to find all parent vertices and add them to the list.
	 * @param list
	 * @param vertex
	 */
	private void getParentVertices(List<MultiVertex> list, MultiVertex vertex){
		Set<MultiEdge> edges = this.incomingEdgesOf(vertex);
		if(edges==null) return; 
		Iterator<MultiEdge> it = edges.iterator(); 
		while(it.hasNext()){
			MultiEdge edge = it.next();
			if(edge.getExpertWeight()==MultiEdge.WEIGHT_PARENT){
				list.add(edge.getSource());
				getParentVertices(list, edge.getSource());
			}
		}	
	}
	
	/**
	 * recursive method to find all parent vertices and add them to the list.
	 * @param list
	 * @param vertex
	 */
	private void getChildVertices(List<MultiVertex> list, MultiVertex vertex){
		Set<MultiEdge> edges = this.outgoingEdgesOf(vertex);
		if(edges==null) return; 
		Iterator<MultiEdge> it = edges.iterator(); 
		while(it.hasNext()){
			MultiEdge edge = it.next();
			if(edge.getExpertWeight()==MultiEdge.WEIGHT_PARENT){
				list.add(edge.getTarget());
				getChildVertices(list, edge.getTarget());
			}
		}	
	}
	
	public Set<MultiEdge> getExplicitLearnerEdges(MultiVertex v){
		if(this.edgesOf(v)==null) return null;
		Iterator<MultiEdge> it = this.edgesOf(v).iterator();
		Set<MultiEdge> edges = new TreeSet<MultiEdge>();
		while(it.hasNext()){
			MultiEdge e = it.next();
			if(e.isExplicitLearnerEdge()) edges.add(e);
		}
		return edges;
	}
	
	public Set<MultiEdge> getExplicitExpertEdges(MultiVertex v){
		if(this.edgesOf(v)==null) return null;
		Iterator<MultiEdge> it = this.edgesOf(v).iterator();
		Set<MultiEdge> edges = new TreeSet<MultiEdge>();
		while(it.hasNext()){
			MultiEdge e = it.next();
			if(e.isExplicitExpertEdge()) edges.add(e);
		}
		return edges;
	}
	
	public boolean isSameGraph(String vpId, long patillscriptId){
		if(this.vpId==null || vpId==null || patillscriptId<0) return false;
		if(this.vpId.equals(vpId) && this.patIllScriptId == patillscriptId) return true;
		return false;
	}
}
