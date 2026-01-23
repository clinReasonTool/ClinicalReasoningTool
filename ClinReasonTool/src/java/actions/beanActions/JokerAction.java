package actions.beanActions;

import java.util.*;

import javax.faces.application.FacesMessage;

import application.ErrorMessageContainer;
import beans.scripts.*;
import beans.graph.Graph;
import beans.graph.MultiVertex;
import beans.relation.Relation;
import controller.NavigationController;
import properties.IntlConfiguration;

/**
 * All kinds of jokers we offer the learner, such as adding a finding, test,....
 * @author ingahege
 * TODO implement new mechanism
 */
public class JokerAction {

	private PatientIllnessScript patIllScript;

	public JokerAction(PatientIllnessScript patIllScript){
		this.patIllScript = patIllScript;
	}
	
	
	public void addJoker(String type){
		if(type==null || type.equals("")) return;
		/*if(type.equals("1")) addFdgJoker();
		else if(type.equals("2")) addDDXJoker();
		else if(type.equals("3")) addTestJoker();
		else if(type.equals("4")) addMngJoker();*/
	}
	/**
	 * Add a finding the expert has added 
	 */
	private void addFdgJoker(){
	/*	Graph g = NavigationController.getInstance().getMyFacesContext().getGraph();
		List<MultiVertex> expsVertices = g.getVerticesByTypeAndStageExpOnly(Relation.TYPE_PROBLEM, patIllScript.getCurrentStage());
		MultiVertex jokerVertex = chooseItem(expsVertices);
		if(jokerVertex==null){ //no more fdgs to be added at this stage
			new ErrorMessageContainer().addErrorMessage("probform", IntlConfiguration.getValue("findings.nojoker"), "", FacesMessage.SEVERITY_ERROR);
			return;
		}
		new AddProblemAction(patIllScript, jokerVertex.getExpertVertex().getPrefix()).addRelation(jokerVertex.getExpertVertex().getListItem(), -1, -1, -1, true);
	*/}
	
	/**
	 * Add a differential the expert has added 
	 */
	private void addDDXJoker(){
	/*	Graph g = NavigationController.getInstance().getMyFacesContext().getGraph();
		List<MultiVertex> expsVertices = g.getVerticesByTypeAndStageExpOnly(Relation.TYPE_DDX, patIllScript.getCurrentStage());
		MultiVertex jokerVertex = chooseItem(expsVertices);
		if(jokerVertex==null){ //no more fdgs to be added at this stage
			new ErrorMessageContainer().addErrorMessage("ddxform", IntlConfiguration.getValue("ddx.nojoker"), "", FacesMessage.SEVERITY_ERROR);
			return;
		}
		new AddDiagnosisAction(patIllScript).addRelation(jokerVertex.getExpertVertex().getListItem(), -1, -1, -1, true);
	*/}
	
	/**
	 * Add a test the expert has added 
	 */
	private void addTestJoker(){
	/*	Graph g = new NavigationController().getCRTFacesContext().getGraph();
		List<MultiVertex> expsVertices = g.getVerticesByTypeAndStageExpOnly(Relation.TYPE_TEST, patIllScript.getCurrentStage());
		MultiVertex jokerVertex = chooseItem(expsVertices);
		if(jokerVertex==null){ //no more fdgs to be added at this stage
			new ErrorMessageContainer().addErrorMessage("testform", IntlConfiguration.getValue("tests.nojoker"), "", FacesMessage.SEVERITY_ERROR);
			return;
		}
		new AddTestAction(patIllScript).addRelation(jokerVertex.getExpertVertex().getListItem(),  -1, -1, -1, true);
	*/}
	
	/**
	 * Add a management option the expert has added 
	 */
	private void addMngJoker(){
	/*	Graph g = NavigationController.getInstance().getCRTFacesContext().getGraph();
		List<MultiVertex> expsVertices = g.getVerticesByTypeAndStageExpOnly(Relation.TYPE_MNG, patIllScript.getCurrentStage());
		MultiVertex jokerVertex = chooseItem(expsVertices);
		if(jokerVertex==null){ //no more fdgs to be added at this stage
			new ErrorMessageContainer().addErrorMessage("mngform", IntlConfiguration.getValue("mng.nojoker"), "", FacesMessage.SEVERITY_ERROR);
			return;
		}
		new AddMngAction(patIllScript).addRelation(jokerVertex.getExpertVertex().getListItem(), -1, -1, -1, true);
	*/}
	
	
	private MultiVertex chooseItem(List<MultiVertex> expsVertices){
		if(expsVertices==null || expsVertices.isEmpty()) return null;
		//for now we pick randomly, but we could add a algorithm here based on the connections to the 
		// final ddx and picks of the peers....
		if(expsVertices.size()==1) return expsVertices.get(0);
		int myRandomPick = new Random().nextInt(expsVertices.size());
		return expsVertices.get(myRandomPick);
	}
}
