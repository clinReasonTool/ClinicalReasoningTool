package beans.scripts;

import java.util.*;

import javax.faces.bean.SessionScoped;

import beans.graph.*;
import beans.relation.Relation;
import controller.NavigationController;

/**
 * A view object (no database storage) for displaying the expert's script. 
 * underlying structure for getter is the graph
 * @author ingahege
 *
 */
@SessionScoped
public class ExpViewPatientIllnessScript {

	private Graph g;
	private int currStage;
	
	public ExpViewPatientIllnessScript(Graph g, int stage){
		this.g = g;
		this.currStage = stage;		
	}
	
	/**
	 * get all findings/problems of the expert that have NOT been selected by the learner
	 * @return list of Relations or null
	 */
	public List<Relation> getProblems(){ 
		return getList(Relation.TYPE_PROBLEM);	
	}

	/**
	 * get all findings/problems of the expert that have NOT been selected by the learner
	 * @return list of Relations or null
	 */
	public List<Relation> getPatho(){ 
		return getList(Relation.TYPE_PATHO);	
	}
	/**
	 * get all diagnoses of the expert that have NOT been selected by the learner
	 * We might need this separately because of additional parameters in the RelationDiagnosis object
	 * @return list of Relations or null
	 */
	public List<Relation> getDiagnoses(){
		List<MultiVertex> l = g.getVerticesByTypeAndStageExpOnly(Relation.TYPE_DDX, currStage);
		if(l==null || l.isEmpty()) return null;
		
		List<Relation> expRels = new ArrayList<Relation>();
		Iterator<MultiVertex> it = l.iterator();
		while(it.hasNext()){
			expRels.add(it.next().getExpertVertex());
		}
		return expRels;			
	}

	/**
	 * get all test items of the expert that have NOT been selected by the learner
	 * @return list of Relations or null
	 */
	public List<Relation> getTests(){ return getList(Relation.TYPE_TEST);		}
	
	/**
	 * get all management items of the expert that have NOT been selected by the learner
	 * @return list of Relations or null
	 */
	public List<Relation> getMngs() {return getList(Relation.TYPE_MNG);}
	
	public List<Relation> getNursingDiagnoses() {return getList(Relation.SUBTYPE_NDDX);}
	public List<Relation> getNursingAims() {return getList(Relation.TYPE_AIM);}
	public List<Relation> getNursingManagement() {return getList(Relation.SUBTYPE_NMNG);}
	public List<Relation> getInfos() {return getList(Relation.TYPE_INFO);}
	
	public List<Relation> getMidwifeManagement() {return getList(Relation.SUBTYPE_MMNG);}
	public List<Relation> getMidwifeRecommendations() {return getList(Relation.SUBTYPE_MREC);}
	public List<Relation> getMidwifeHypotheses() {return getList(Relation.SUBTYPE_MHYP);}
	public List<Relation> getMidwifeFindings() {return getList(Relation.SUBTYPE_MFDG);}
	
	/**
	 * get all items of given type that have not been selected by the learner, but the expert
	 * @param type
	 * @return
	 */
	private List<Relation> getList(int type){
		List<Relation> expRels = new ArrayList<Relation>();
		if (g != null) {
			List<MultiVertex> l = g.getVerticesByTypeAndStageExpOnly(type, currStage);
			if(l==null || l.isEmpty()) return null;
			
			Iterator<MultiVertex> it = l.iterator();
			while(it.hasNext()){
				MultiVertex mv = it.next(); 
				//do NOT display nodes that are part of a syndrome, unless it has been added by a learner and been
				//scored -> then it is helpful as feedback
				if(mv.getExpertVertex().getIsSyndrome()==Relation.IS_SYNDROME_PART && mv.getLearnerVertex()==null){
					//do nothing here...
				}
				else 
					expRels.add(mv.getExpertVertex());
			}
		}
		
		return expRels;	
	}
}
