package controller;

import java.util.Locale;

import beans.list.ListItem;
import beans.relation.*;
import beans.scripts.PatientIllnessScript;
import database.DBClinReason;
import util.*;

public class ScriptTranslationController {
	
	/**
	 * new language has already been set!
	 */
	private PatientIllnessScript patIllScript; 

	public ScriptTranslationController(){}
	public ScriptTranslationController(PatientIllnessScript pis){
		this.patIllScript = pis;
	}
	
	/**
	 * duplicate the script/map and translate all items (if possible) into the new language. 
	 * @param newLang
	 * @param vpId
	 */
	public void translateScript(Locale newLang){
		try{
			if(patIllScript.getBox1Relations()!=null){
				for(int i=0;i<patIllScript.getBox1Relations().size();i++){
					translateRelation((Relation)patIllScript.getBox1Relations().get(i), newLang);
				}
			}
			if(patIllScript.getBox2Relations()!=null){
				for(int i=0;i<patIllScript.getBox2Relations().size();i++){
					translateRelation((Relation)patIllScript.getBox2Relations().get(i), newLang);
				}
			}
			if(patIllScript.getBox3Relations()!=null){
				for(int i=0;i<patIllScript.getBox3Relations().size();i++){
					translateRelation((Relation) patIllScript.getBox3Relations().get(i), newLang);
				}
			}
			if(patIllScript.getBox4Relations()!=null){
				for(int i=0;i<patIllScript.getBox4Relations().size();i++){
					translateRelation((Relation)patIllScript.getBox4Relations().get(i), newLang);
				}
			}	
			/*if(patIllScript.getPatho()!=null){
				for(int i=0;i<patIllScript.getPatho().size();i++){
					translatePatho(patIllScript.getPatho().get(i), newLang);
				}
			}*/
			//patIllScript.setLocale(newLang);
			new DBClinReason().saveAndCommit(patIllScript);
		}
		catch(Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
		}
	}


	/**
	 * We translate a problem/finding by looking for the MeSH code in the new language
	 * @param rel
	 */
	/*private void translateProblem(RelationProblem rel, Locale newLang){		
		ListItem li = ScriptCopyController.getListItem(rel.getProblem().getFirstCode(), newLang);		
		if (li!=null){ //we found a match in the list
			rel.setProblem(li);
			CRTLogger.out(rel.getProblem().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			rel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(rel);

		}
	}*/
	
	private void translateRelation(Relation rel, Locale newLang){		
		ListItem li = ScriptCopyController.getListItem(rel.getListItem().getFirstCode(), newLang);		
		if (li!=null){ //we found a match in the list
			rel.setListItem(li);
			CRTLogger.out(rel.getListItem().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			rel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(rel);

		}
	}
	
	/**
	 * We translate a ddx by looking for the MeSH code in the new language
	 * @param rel
	 */
	/*private void translateDiagnosis(RelationDiagnosis rel, Locale newLang){		
		ListItem li = ScriptCopyController.getListItem(rel.getDiagnosis().getFirstCode(), newLang);		
		if (li!=null){ //we found a match in the list
			rel.setDiagnosis(li);
			CRTLogger.out(rel.getDiagnosis().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			rel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(rel);

		}
	}*/
	
	/**
	 * We translate a test by looking for the MeSH code in the new language
	 * @param rel
	 */
	/*private void translateTest(RelationTest rel, Locale newLang){		
		ListItem li = ScriptCopyController.getListItem(rel.getTest().getFirstCode(), newLang);		
		if (li!=null){ //we found a match in the list
			rel.setTest(li);
			CRTLogger.out(rel.getTest().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			rel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(rel);

		}
	}*/
	
	/**
	 * We translate a test by looking for the MeSH code in the new language
	 * @param rel
	 */
	/*private void translateMng(RelationManagement rel, Locale newLang){		
		ListItem li = ScriptCopyController.getListItem(rel.getManagement().getFirstCode(), newLang);		
		if (li!=null){ //we found a match in the list
			rel.setManagement(li);
			CRTLogger.out(rel.getManagement().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			rel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(rel);

		}
	}*/
	
	/**
	 * We translate a test by looking for the MeSH code in the new language
	 * @param rel
	 */
	/*private void translatePatho(RelationPatho rel, Locale newLang){		
		ListItem li = ScriptCopyController.getListItem(rel.getPatho().getFirstCode(), newLang);		
		if (li!=null){ //we found a match in the list
			rel.setPatho(li);
			CRTLogger.out(rel.getPatho().getName() + "->" + li.getName(), CRTLogger.LEVEL_PROD);
			rel.setListItemId(li.getItem_id());
			new DBClinReason().saveAndCommit(rel);

		}
	}*/
}