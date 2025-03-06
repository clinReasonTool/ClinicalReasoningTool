package controller;

import java.util.*;

import beans.helper.CaseDifficulty;
import beans.helper.VPMapOverviewItem;
import database.DBSearch;
import util.CRTLogger;

/**
 * Calculates the difficulty of a VP based on the number of tries to come up with the final diagnosis.
 * @author ingahege
 *
 */
public class CaseDifficultyCalculator {
	
	/**
	 * key = orgCaseId, value=List of VPMapOverviewItems
	 * not sure if we need this here...
	 */
	private Map<Long, List<VPMapOverviewItem>> vpMapOverviewItems;
	
	/**
	 * CaseDifficulty is calculated by dividing the number of correct first attempts by the overall number of sessions/maps. 
	 * -> the higher the value, the easier the case.
	 * 
	 * @param caseId
	 * @return
	 */
	public float calculateDifficulty(long caseId) {
		List<VPMapOverviewItem> l = loadVPMapOverviewItem(caseId);		
		return calculateDifficulty(l, caseId);
		
	}
	
	private float calculateDifficulty(List<VPMapOverviewItem> l, long caseId) {
		//List<VPMapOverviewItem> l = loadVPMapOverviewItem(caseId);
		if(l==null || l.isEmpty()) return -1;
		int numFirstAttemptsCorr = 0;
		//int numFirstAttemptsWrong = 0;
		for(int i=0;i<l.size(); i++) {
			VPMapOverviewItem item = l.get(i);
			if(item.getNumFirstAttempts()==1) numFirstAttemptsCorr++;
			//else numFirstAttemptsWrong++;
		}
		float caseDiff = (float) numFirstAttemptsCorr / (float) l.size();
		CRTLogger.out("Case: " + caseId + ", difficulty: " + caseDiff, CRTLogger.LEVEL_PROD);
		saveCaseDiffObj(caseDiff, l.size(), caseId);
		return caseDiff;	
	}
	
	/**
	 * loads the overviewMapItems for a given caseId including any childCases
	 * @param caseId
	 * @return
	 */
	private List<VPMapOverviewItem> loadVPMapOverviewItem(long caseId) {
		
		List l = new DBSearch().selectVPMapOverviewItems(caseId);
		if(l!=null && !l.isEmpty()) {
			if(vpMapOverviewItems==null) {
				vpMapOverviewItems = new TreeMap<Long,List<VPMapOverviewItem>>();
			}
			if(!vpMapOverviewItems.containsKey(new Long(caseId)))
				vpMapOverviewItems.put(new Long(caseId), l);
		}
		return l;
	}
	
	/**
	 * calculates difficulty for all cases that have concept maps
	 * CAVE: Should not be called on a daily bases
	 */
	public void calculateDifficulty() {
		vpMapOverviewItems = new DBSearch().selectVPMapOverviewItems();
		Iterator<Long> it = vpMapOverviewItems.keySet().iterator();
		while(it.hasNext()) { 
			Long caseId = it.next().longValue();
			calculateDifficulty(vpMapOverviewItems.get(caseId), caseId.longValue());
		}	
	}
	
	private void saveCaseDiffObj(float caseDiff, int numSess, long caseId) {
		CaseDifficulty d = new CaseDifficulty(caseDiff, numSess, caseId);
		new DBSearch().saveAndCommit(d);	
	}
}
