package controller;

import java.util.*;

import application.AppBean;
import beans.LogEntry;
import beans.scripts.*;
import beans.error.*;
import beans.graph.*;
import beans.relation.*;
import database.DBClinReason;
import util.*;

/**
 * If the leaner has submitted a wrong final diagnosis, we check whether we can see a pattern that 
 * speaks in favor of one of the common errors.
 * @author ingahege
 *
 */
public class ErrorController {

	public List<MyError> checkError(List<RelationDiagnosis> learnerFinals,List<RelationDiagnosis> expFinals){
		List<MyError> errors = new ArrayList<MyError>();
		PatientIllnessScript patIllScript = new NavigationController().getCRTFacesContext().getPatillscript();
		PatientIllnessScript expIllScript = AppBean.getExpertPatIllScript(patIllScript.getVpId());
		
		if(learnerFinals==null || learnerFinals.isEmpty() || expFinals==null || expFinals.isEmpty()) return null;
		checkPrematureClosure(expIllScript, patIllScript);
		checkAvailabilityError(patIllScript);
		checkConfirmationBias(expIllScript, patIllScript);
		checkBaseRateNeglect(expIllScript, patIllScript);
		checkRepresentativeness(expIllScript, patIllScript);
		//new DBClinReason().saveAndCommit(errors);
		
		return errors;
	}
	
	
	/**
	 * ddx has been submitted too early
	 * TODO: we could check in addition whether user has missed important findings (or these come at a later stage) that 
	 * would lead to the correct diagnoses.
	 * @param expIllScript
	 * @param patIllScript
	 */
	private void checkPrematureClosure(PatientIllnessScript expIllScript, PatientIllnessScript patIllScript){	
		try{
			Graph g = NavigationController.getInstance().getCRTFacesContext().getGraph();
			if(patIllScript.getCurrentStage() < expIllScript.getSubmittedStage()){
				//we get any additional problems the expert might have collected AFTER the current stage:
				List<MultiVertex> expAddRels = g.getVerticesByTypeAndStageRangeExpOnly(Relation.TYPE_PROBLEM, patIllScript.getCurrentStage() + 1, expIllScript.getSubmittedStage());
				if(expAddRels!=null && !expAddRels.isEmpty()){
					PrematureClosure pcl =  new PrematureClosure(patIllScript.getId(), patIllScript.getCurrentStage(), patIllScript.getConfidence(), patIllScript.getFinalDiagnoses());
					if(patIllScript.addError(pcl)) //we save only if this is a new error that has not previously occured at this stage
						new DBClinReason().saveAndCommit(pcl);	
					notifyLog(pcl, patIllScript.getId());
				}
			}
		}
		catch (Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
	
	
	/**
	 * we have to check here the last x scripts/VPs of the user and whether it involved the diagnosis he has come up with here...
	 * @return
	 */
	private void checkAvailabilityError(PatientIllnessScript patIllScript){
		try{
			PatIllScriptContainer cont = NavigationController.getInstance().getCRTFacesContext().getScriptContainer();
			if(cont==null || cont.getScriptsOfUser()==null) return; //no previous scripts
			List<PatientIllnessScript> lastScripts = cont.getLastCompletedScripts(AvailabilityBias.NUM_SCRIPTS);
			List <RelationDiagnosis> currFinalDiagn = patIllScript.getFinalDiagnoses();
			
			if(lastScripts!=null && currFinalDiagn!=null){
				for(int i=0; i<lastScripts.size(); i++){
					PatientIllnessScript script = lastScripts.get(i);
					if (script.getFinalDiagnoses()!=null){
						List<RelationDiagnosis> finals = script.getFinalDiagnoses();
						for(int k=0; k<finals.size(); k++){
							long id = finals.get(k).getDiagnosis().getItem_id();
							for(int l=0; l< currFinalDiagn.size(); l++){
								if(currFinalDiagn.get(l).getListItem().getItem_id()==id){
									AvailabilityBias ab = new AvailabilityBias(patIllScript.getId(), patIllScript.getCurrentStage(), patIllScript.getConfidence(), patIllScript.getListByType(Box.BOXTYPE_DDX, -1));
									
									if(patIllScript.addError(ab)) //we save only if this is a new error that has not previously occured at this stage
										new DBClinReason().saveAndCommit(ab);	
									notifyLog(ab, patIllScript.getId());
									return;
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
	
	/**
	 * Learner has NOT added disconforming finding(s) or speaks against connections between his final 
	 * dignosis and the disconforming finding(s)
	 * Only works if the expert has also included the learner's final diagnosis in his map as a differential diagnosis
	 * @param expIllScript
	 * @param patIllScript
	 */
	private void checkConfirmationBias(PatientIllnessScript expIllScript, PatientIllnessScript patIllScript){
		try{
			Graph g = NavigationController.getInstance().getCRTFacesContext().getGraph();
			List<RelationDiagnosis> finalDiagnoses = patIllScript.getFinalDiagnoses();
			for(int i=0; i<finalDiagnoses.size(); i++){
				RelationDiagnosis fd = finalDiagnoses.get(i);
				MultiVertex vertex = g.getVertexByIdAndType(fd.getListItemId(), Relation.TYPE_DDX);
				if(vertex.isExpertVertex()){ //only then we can check it
					RelationDiagnosis expDiagn = (RelationDiagnosis) vertex.getExpertVertex();
					if(!expDiagn.isFinalDDX()){ //if it is a final diagnosis, it is correct anyway -> can happen if we have more than one final diagnoses
						Set<MultiEdge> edges = g.edgesOf(vertex);
						if(edges!=null && !edges.isEmpty()){ //then we have connections we can check:
							Iterator<MultiEdge> it = edges.iterator();
							while (it.hasNext()){
								MultiEdge me = it.next();
								if(me.isExplicitExpertEdge() && me.getExpertWeight()==Connection.WEIGHT_SPEAKS_AGAINST && me.getLearnerWeight()!=Connection.WEIGHT_SPEAKS_AGAINST){
									ConfirmationBias cb = new ConfirmationBias(patIllScript.getId(), patIllScript.getCurrentStage(), patIllScript.getConfidence(), patIllScript.getListByType(Box.BOXTYPE_DDX,-1));
									if(patIllScript.addError(cb)) //we save only if this is a new error that has not previously occured at this stage
										new DBClinReason().saveAndCommit(cb);	
									notifyLog(cb, patIllScript.getId());
									return;
								}
							}
						}
					}
 				}
			}
		}
		catch(Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
	
	/**
	 * we check whether the final diagnoses of the learner have been added by the expert as well and whether they have 
	 * been rated as rare (prevalence). 
	 * TODO: As a next development step we could also check for general prevalences in the ListItems if the expert has 
	 * not added the prevalence information. CAVE: Prevalence can be very different depending on age, gender, country, etc... 
	 * @param expIllScript
	 * @param patIllScript
	 */
	private void checkBaseRateNeglect(PatientIllnessScript expIllScript, PatientIllnessScript patIllScript){
		try{
			List<RelationDiagnosis> finalDiagnoses = patIllScript.getFinalDiagnoses();
			for(int i=0; i<finalDiagnoses.size(); i++){
				RelationDiagnosis rd = finalDiagnoses.get(i);
				RelationDiagnosis expRel = (RelationDiagnosis) expIllScript.getRelationByListItemIdAndType(rd.getListItemId(), Relation.TYPE_DDX);
				if(expRel!=null && !expRel.isFinalDDX() && expRel.getPrevalence()==RelationDiagnosis.PREVALENCE_RARE){
					BaseRateNeglect brn = new BaseRateNeglect(patIllScript.getId(), patIllScript.getCurrentStage(), patIllScript.getConfidence(), patIllScript.getListByType(Box.BOXTYPE_DDX,-1));
					if(patIllScript.addError(brn)) //we save only if this is a new error that has not previously occured at this stage
						new DBClinReason().saveAndCommit(brn);	
					notifyLog(brn, patIllScript.getId());
					return;
				}
			}
		}
		catch(Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}
	
	/**
	 * Learner has connected non-prototypical findings as speaks against to a diagnosis, that is the experts final diagnosis. 
	 * @param expIllScript
	 * @param patIllScript
	 */
	private void checkRepresentativeness(PatientIllnessScript expIllScript, PatientIllnessScript patIllScript){
		try{
			//List<RelationDiagnosis> finalDiagnoses = patIllScript.getFinalDiagnoses();
			Graph g = NavigationController.getInstance().getCRTFacesContext().getGraph();
			List<RelationDiagnosis> expFinalDiagnoses = expIllScript.getFinalDiagnoses();
			for(int i=0; i<expFinalDiagnoses.size(); i++){
				RelationDiagnosis finalDDXExp = expFinalDiagnoses.get(i);
				MultiVertex vertex = g.getVertexByIdAndType(finalDDXExp.getListItemId(), Relation.TYPE_DDX);
				if(vertex.isLearnerVertex() && !((RelationDiagnosis) vertex.getLearnerVertex()).isFinalDDX()){
					Set<MultiEdge> edges = g.edgesOf(vertex);
					if(edges!=null && !edges.isEmpty()){
						Iterator<MultiEdge> it = edges.iterator();
						while (it.hasNext()){
							MultiEdge me = it.next();
							if(me.getLearnerWeight()==Connection.WEIGHT_SPEAKS_AGAINST){
								MultiVertex finding = null;
								if(me.getSource()!=null && me.getSource().getVertexType()==Relation.TYPE_PROBLEM)
									finding = me.getSource();
								else if(me.getTarget()!=null && me.getTarget().getVertexType()==Relation.TYPE_PROBLEM)
									finding = me.getTarget();
								if(finding!=null && finding.getExpertVertex()!=null){
									RelationProblem expFind= (RelationProblem) finding.getExpertVertex();
									if(expFind!=null && expFind.getPrototypical()==RelationProblem.FIND_NONPROTOTYPICAL){
										Representativeness rep = new Representativeness(patIllScript.getId(), patIllScript.getCurrentStage(), patIllScript.getConfidence(), patIllScript.getListByType(Box.BOXTYPE_DDX,-1));
										if(patIllScript.addError(rep)) //we save only if this is a new error that has not previously occured at this stage
											new DBClinReason().saveAndCommit(rep);	
										notifyLog(rep, patIllScript.getId());
										return;
									}
									
								}
							}
						}
					}
				}
				
			}
			
			
		}
		catch(Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}

	}
	
	private void notifyLog(MyError err, long patIllScriptId){
		LogEntry le = new LogEntry(LogEntry.ERROR_ACTION, patIllScriptId, err.getType());
		le.save();		
	}
}
