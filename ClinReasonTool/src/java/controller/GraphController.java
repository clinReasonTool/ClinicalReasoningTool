package controller;

import java.io.Serializable;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import application.AppBean;
import beans.scripts.*;
import beans.graph.*;
import beans.relation.Connection;
import beans.relation.Relation;
import beans.relation.RelationDiagnosis;
import beans.scripts.IllnessScriptInterface;

/**
 * Controls the graph creation and manipulation based on PatientIllnessScript and IllnessScript objects 
 * @author ingahege
 *
 */
public class GraphController implements Serializable{

	private static final long serialVersionUID = 1L;

	//public static final String PREFIX_PROB = "cmprb_";
	//public static final String PREFIX_EPI = "cmepi_";
	//public static final String PREFIX_DDX = "cmddx_";
	//public static final String PREFIX_MNG = "cmmng_";
	public static final String PREFIX_CNX = "cnx_";
	//public static final String PREFIX_TEST = "cmtes_";
	//public static final String PREFIX_PATHO = "cmpat_";
	//public static final String PREFIX_PROB2 = "fdg_";
	//public static final String PREFIX_EPI2 = "epi_";
	//public static final String PREFIX_DDX2 = "ddx_";
	//public static final String PREFIX_MNG2 = "mng_";
	//public static final String PREFIX_TEST2 = "tst_";
	//public static final String PREFIX_PATHO2 = "pat_";
	//public static final String PREFIX_NDDX = "nddx_"; //nursing diagnoses
	//public static final String PREFIX_NURSINGAIM = "naim_"; //nursing aims
	//public static final String PREFIX_NMNG = "nmng_"; //nursing management
	//public static final String PREFIX_INFO = "info_";
	//public static final String PREFIX_MHYP = "mhyp_"; //midwife hypotheses
	//public static final String PREFIX_MREC = "mrec_"; //midwife recommendations
	//public static final String PREFIX_MMNG = "mmng_"; //midwife management
	//ublic static final String PREFIX_MFDG = "mfdg_"; //midwife findings
	
	//public static final long PREFIX_MMNG = 0;
	
	private Graph graph;
	
	public GraphController(Graph g){
		this.graph = g;
	}
	
	/**
	 * Add all vertices (e.g. Problem, DDX) to the graph and all explicit (Connection objects) and implicit edges
	 * of the experts' PatientIllnessScript for this parentId
	 * @param parentId
	 */
	public void addExpPatIllScript(String vpId){
	    PatientIllnessScript expIllScript = AppBean.getExpertPatIllScript(vpId);
	    if(expIllScript!=null){
	    	graph.setExpertPatIllScriptId(expIllScript.getId());
	    	addVerticesOfPatientIllnessScript(expIllScript, IllnessScriptInterface.TYPE_EXPERT_CREATED);
	    	addExplicitEdgesOfPatientIllnessScript(expIllScript, IllnessScriptInterface.TYPE_EXPERT_CREATED); //and connect all implicitly
		   // addImplicitEdgesOfPatientIllnessScript(expIllScript,  IllnessScriptInterface.TYPE_EXPERT_CREATED);		    
	    }
	}
	/**
	 * Add all vertices (e.g. Problem, DDX) to the graph and all explicit (Connection objects) and implicit edges
	 * of the learners' PatientIllnessScript for this parentId
	 * @param parentId
	 */
	public void addLearnerPatIllScript(){
		PatientIllnessScript patIllScript = NavigationController.getInstance().getMyFacesContext().getPatillscript();
	    addVerticesOfPatientIllnessScript(patIllScript, IllnessScriptInterface.TYPE_LEARNER_CREATED);
	    addExplicitEdgesOfPatientIllnessScript(patIllScript, IllnessScriptInterface.TYPE_LEARNER_CREATED);
	    //addImplicitEdgesOfPatientIllnessScript(patIllScript,  IllnessScriptInterface.TYPE_LEARNER_CREATED);
	}
	
	/**
	 * Add all vertices (e.g. Problem, DDX) to the graph and all explicit edges of the IllnessScript for this parentId
	 * (IllnessScripts only have explicit edges, since they only include ONE diagnosis, every other item explicitly
	 * relates to this diagnosis.
	 * @param parentId	 
	 * */
	public void addIllnessScripts(String vpId){
	    ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	    //AppBean app = (AppBean) context.getAttribute(AppBean.APP_KEY);
	    List<IllnessScript> illScripts = AppBean.getIlnessScripts(vpId);
	    
	    if(illScripts!=null){
	    	Iterator<IllnessScript> it = illScripts.iterator();
	    	while(it.hasNext()){
	    		IllnessScript illScript = it.next();
	    		graph.addIllScriptId(illScript.getId());
	    		//addVertices(illScript.getProblems());
	    		//...
	    	}
	    }
	}	
	
	private void addVerticesOfPatientIllnessScript(PatientIllnessScript patIllScript, int illnessScriptType){
		 addVertices( patIllScript.getBox1Relations(),illnessScriptType, 1);
		 addVertices( patIllScript.getBox2Relations(), illnessScriptType,2 );
		 addVertices( patIllScript.getBox3Relations(), illnessScriptType,3 );
		 addVertices( patIllScript.getBox4Relations(), illnessScriptType,4 );
	}
	
	/**
	 * if a relation has a synId we add the SimpleVertex for the syn:
	 * Learnerscript: we ONLY add the SimpleVertex
	 * ExpertSkript: we add the main relation and ALL synonyma as SimpleVertex
	 * @param vertices (List of Relation items)
	 * @param illnessScriptType
	 */
	private void addVertices(List vertices, int illnessScriptType, int boxNo){
		if(vertices==null) return;
		for(int i=0; i<vertices.size(); i++){
			Relation rel = (Relation) vertices.get(i);
			graph.addVertex(rel, illnessScriptType, boxNo);
		}
	}
	
	private void addExplicitEdgesOfPatientIllnessScript(PatientIllnessScript patIllScript, int type){
		if(patIllScript==null || patIllScript.getConns()==null) return;
		Iterator<Connection> it =  patIllScript.getConns().values().iterator();
		while (it.hasNext()){
			Connection cnx = (Connection) it.next();
			graph.addExplicitEdge(cnx, patIllScript, type);
		}
	}
	
	/*private void addImplicitEdgesOfPatientIllnessScript(PatientIllnessScript patIllScript, int illScriptType){
		if(patIllScript==null || patIllScript.getDiagnoses()==null) return;
		for(int i=0; i < patIllScript.getDiagnoses().size(); i++){
			//add problems -> ddx
			if(patIllScript.getProblems()!=null){
				for(int j=0; j < patIllScript.getProblems().size(); j++){
					//graph.addImplicitEdge(sourceId, targetId, type);
					if(patIllScript.getProblems().get(j)!=null && patIllScript.getDiagnoses().get(i)!=null)
						graph.addImplicitEdge(patIllScript.getProblems().get(j).getListItemId(), patIllScript.getDiagnoses().get(i).getListItemId(), illScriptType);
				}
			}

			//add ddx -> tests
			if(patIllScript.getTests()!=null){
				for(int j=0; j < patIllScript.getTests().size(); j++){
					graph.addImplicitEdge(patIllScript.getDiagnoses().get(i).getListItemId(), patIllScript.getTests().get(j).getListItemId(), illScriptType);
				}
			}
			//add ddx -> mng
			if(patIllScript.getMngs()!=null){
				for(int j=0; j < patIllScript.getMngs().size(); j++){
					graph.addImplicitEdge(patIllScript.getDiagnoses().get(i).getListItemId(), patIllScript.getMngs().get(j).getListItemId(), illScriptType);
				}
			}
		}
	}*/
	
	
	/**
	 * We look whether this vertex has any lower or higher hierarchy vertices within the 
	 * same type vertices. 
	 * @param mv
	 * @return
	 */
	public List<MultiVertex> findNextHierarchyVertices(MultiVertex mv, int type){
		List<MultiVertex> vertices = graph.getVerticesByType(type);
		if(mv.getLearnerVertex()==null || mv.getLearnerVertex().getListItem()==null) return null;
		//String thisCode = mv.getLearnerVertex().getListItem().getFirstCode();
		if(vertices==null || vertices.isEmpty()) return null;
		List<MultiVertex> nextHierarchy = new ArrayList<MultiVertex>();
		int codeDiff = -1;
		
		for(int i=0; i<vertices.size(); i++){
			MultiVertex mv2 = vertices.get(i);
			if(mv2.getLearnerVertex()!=null && mv2.getLearnerVertex().getListItem()!=null){
				int codeDiffTmp = mv.getLearnerVertex().getListItem().getAbsHierarchyDiff(mv2.getLearnerVertex().getListItem());
				if(codeDiffTmp>-1 /*&& (codeDiff==-1 || codeDiffTmp<codeDiff)*/){
					nextHierarchy.add(mv2);
					//codeDiff = codeDiffTmp;
				}
			}
		}
		return nextHierarchy;
	}
	
	
	public static int getTypeByPrefix(String prefix){ //"box1_", "box2_"
		if(prefix==null) return 0;
		return Integer.parseInt(prefix.substring(4));
		/*if(prefix.equals("box) || prefix.equals(PREFIX_PROB2)) return Relation.TYPE_PROBLEM;
		if(prefix.equals(PREFIX_DDX) || prefix.equals(PREFIX_DDX2)) return Relation.TYPE_DDX;
		if(prefix.equals(PREFIX_TEST) || prefix.equals(PREFIX_TEST2)) return Relation.TYPE_TEST;
		if(prefix.equals(PREFIX_MNG) || prefix.equals(PREFIX_MNG2)) return Relation.TYPE_MNG;
		//if(prefix.equals(PREFIX_NMNG) ) return Relation.TYPE_NMNG;
		if(prefix.equals(PREFIX_INFO) ) return Relation.TYPE_INFO;
		//if(prefix.equals(PREFIX_NDDX) ) return Relation.TYPE_NDDX;
		if(prefix.equals(PREFIX_NURSINGAIM) ) return Relation.TYPE_AIM;
		if(prefix.equals(PREFIX_PATHO) || prefix.equals(PREFIX_PATHO2)) return Relation.TYPE_PATHO;
		//if(prefix.equals(PREFIX_MFDG)) return Relation.TYPE_MFDG;
		//if(prefix.equals(PREFIX_MHYP)) return Relation.TYPE_MHYP;
		//if(prefix.equals(PREFIX_MREC)) return Relation.TYPE_MREC;
		//if(prefix.equals(PREFIX_MMNG)) return Relation.TYPE_MMNG;

		return 0;*/
	}
	
	public static String getPrefixByType(int type){ //1,2,3,4,6,8,10
		return "box"+type+"_";
	/*	if(type==Relation.TYPE_PROBLEM) return PREFIX_PROB2;
		if(type==Relation.TYPE_DDX) return PREFIX_DDX2;
		if(type==Relation.TYPE_TEST) return PREFIX_TEST2;
		if(type==Relation.TYPE_MNG) return PREFIX_MNG2;
		//if(type==Relation.TYPE_NMNG) return PREFIX_NMNG;
		//if(type==Relation.TYPE_NDDX) return PREFIX_NDDX;
		if(type==Relation.TYPE_INFO) return PREFIX_INFO;
		if(type==Relation.TYPE_PATHO) return PREFIX_PATHO;
		if(type==Relation.TYPE_AIM) return PREFIX_NURSINGAIM;
		//if(type==Relation.TYPE_MFDG) return PREFIX_MFDG;
		//if(type==Relation.TYPE_MHYP) return PREFIX_MHYP;
		//if(type==Relation.TYPE_MREC) return PREFIX_MREC;
		//if(type==Relation.TYPE_MMNG) return PREFIX_MMNG;
		return "";*/
	}
	
	public void transferEdges(MultiVertex oldVertex, MultiVertex newVertex){
		//first we have to transfer edges the learner might have had created:
		Set<MultiEdge> explOldEdges = graph.getExplicitLearnerEdges(oldVertex);
		if(explOldEdges!=null){
			Iterator<MultiEdge> it = explOldEdges.iterator();
			while(it.hasNext()){
				MultiEdge e = it.next();
				
				if(e.getSource()!=null && e.getSource().equals(oldVertex)){ //oldVertex is source of edge
					
					MultiEdge newEdge = graph.getEdge(newVertex, e.getTarget());
					if(newEdge==null){
						newEdge = new MultiEdge(e.getParams());
						graph.addEdge(oldVertex, newVertex);
					}
					else{ 
						newEdge.mergeParams(e.getParams());
					}
					
				}
				if(e.getTarget()!=null && e.getTarget().equals(oldVertex)){ //oldVertex is target of edge
					
					MultiEdge newEdge = graph.getEdge(e.getSource(), newVertex);
					if(newEdge==null){
						newEdge = new MultiEdge(e.getParams());
						graph.addEdge(oldVertex, newVertex);
					}
					else{ 
						newEdge.mergeParams(e.getParams());
					}
					
				}
			}
		}
	}
	
	/**
	 * Gets all ddx vertices added by the expert and marked as a final diagnosis. 
	 * @return
	 */
	public List<MultiVertex> getExpertFinalVertices(){
		Set<MultiVertex> verts = graph.vertexSet();
		if(verts==null) return null;
		List<MultiVertex> list = new ArrayList<MultiVertex>();
		Iterator<MultiVertex> it = verts.iterator();
		while(it.hasNext()){
			MultiVertex mv = it.next();
			if(mv.getType()==Relation.TYPE_DDX && mv.getExpertVertex()!=null){
				RelationDiagnosis expRel = (RelationDiagnosis) mv.getExpertVertex();
				if(expRel.isFinalDDX()) list.add(mv);				
			}
		}
		return list;
	}
	
	/**
	 * We check whether the graph has two different vertices, so that the learner could draw a connection. 
	 * Only call if user is new, to determine whether we shall display a hint on how to draw connections
	 * @return
	 */
	public boolean hasTwoDiffVertices(){
		if(graph==null) return false;
		List<MultiVertex> l = graph.getAllVertices();
		if(l==null || l.isEmpty() || l.size()==1) return false;
		int tmp = -1;
		for(int i=0; i<l.size(); i++){
			MultiVertex mv = l.get(i);
			if(tmp<0) tmp = mv.getType();
			if(tmp>0 && tmp!=mv.getType()) return true;
		}
		return false;
	}
}
