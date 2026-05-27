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
	private PatientIllnessScript expIllScript;
	
	public ExpViewPatientIllnessScript(PatientIllnessScript illscript, int stage, Graph graph){
		this.expIllScript = illscript;
		this.currStage = stage;	
		g = graph;
	}
	
	/**
	 * get all findings/problems of the expert that have NOT been selected by the learner
	 * @return list of Relations or null
	 */
	/*public List<Relation> getProblems(){ 
		return getList(Relation.TYPE_PROBLEM);	
	}*/
	public List getBox1Relations(){return getList(expIllScript.getBox1().getBoxType());} 
	public List getBox2Relations(){return getList(expIllScript.getBox2().getBoxType());}
	public List getBox3Relations(){return getList(expIllScript.getBox3().getBoxType());}
	public List getBox4Relations(){return getList(expIllScript.getBox4().getBoxType());}
			
	/**
	 * get all diagnoses of the expert that have NOT been selected by the learner
	 * We might need this separately because of additional parameters in the RelationDiagnosis object
	 * @return list of Relations or null
	 */
	//public List<Relation> getDiagnoses(){
		/*List<MultiVertex> l = g.getVerticesByTypeAndStageExpOnly(Relation.TYPE_DDX, currStage);
		if(l==null || l.isEmpty()) return null;*/
		
		//List<Relation> expRels = new ArrayList<Relation>();
		/*Iterator<MultiVertex> it = l.iterator();
		while(it.hasNext()){
			expRels.add(it.next().getExpertVertex());
		}*/
		//return expRels;			
	//}

	/**
	 * get all test items of the expert that have NOT been selected by the learner
	 * @return list of Relations or null
	 */
	//public List<Relation> getTests(){ return getList(Relation.TYPE_TEST);		}
	
	
	/**
	 * get all items of given type that have not been selected by the learner, but the expert - needed for display of expert 
	 *  answer when student clicks the expert on button. Avoids that expert item isdisplayed, when student has also added it correctly.
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
