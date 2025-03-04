package database;

import java.util.*;
import org.hibernate.*;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import beans.helper.VPMapOverviewItem;
import beans.search.SearchResult;
import util.CRTLogger;
import util.StringUtilities;

public class DBSearch extends DBClinReason{
	

	public List<SearchResult> selectScriptsForSearchTerm(String searchTerm){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
      	Criteria criteria = s.createCriteria(SearchResult.class,"SearchResult");
    	criteria.add(Restrictions.ilike("name", searchTerm.toLowerCase(), MatchMode.ANYWHERE));
    	return criteria.list();
	}
	
	/**
	 * returns the main and child mapoverview objects for the given caseId where a final diagnosis has been submitted
	 * @param vpId
	 * @return
	 */
	public List<VPMapOverviewItem> selectVPMapOverviewItems(long vpId){
		List l = new ArrayList();
		Session s = instance.getInternalSession(Thread.currentThread(), false);
      
		//this gets the main (or parent case:
		Criteria criteria = s.createCriteria(VPMapOverviewItem.class,"VPMapOverviewItem");    	
    	criteria.add(Restrictions.eq("caseId", new Long(vpId)));
    	criteria.add(Restrictions.gt("ddxSubmittedStage",0));
    	l.add(criteria.list());
    	
    	//this gets the child cases:
    	Criteria criteria2 = s.createCriteria(VPMapOverviewItem.class,"VPMapOverviewItem");
    	criteria2.add(Restrictions.eq("orgCaseId", new Long(vpId)));
    	criteria2.add(Restrictions.gt("ddxSubmittedStage",0));
    	
    	l.addAll(criteria2.list());
    	return l;
	} 
	
	public Map<Long, List<VPMapOverviewItem>> selectVPMapOverviewItems(){
		Map m = new TreeMap<Long, List<VPMapOverviewItem>>();
		List<VPMapOverviewItem> l = new ArrayList<VPMapOverviewItem>(); //list with all entries in the view
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(VPMapOverviewItem.class,"VPMapOverviewItem");   
		criteria.add(Restrictions.gt("ddxSubmittedStage",0));
		criteria.add(Restrictions.gt("caseId",new Long(0)));
		l = criteria.list();
		
		if(l==null || l.isEmpty()) return null;
		for(int i=0;i<l.size();i++) {
			VPMapOverviewItem v = l.get(i);
			//add child case objects
			if(v.getOrgCaseId()>0 && v.getChildType() == VPMapOverviewItem.CHILD_TRANSLATION) {
				if(m.get(new Long(v.getOrgCaseId()))==null) {
					ArrayList l2 = new ArrayList();
					l2.add(l.get(i));
					m.put(new Long(v.getOrgCaseId()), l2);
				}
					else ((List<VPMapOverviewItem>) m.get(new Long(v.getOrgCaseId()))).add(v);
						
			}
			//add parent case objects
			else {
				if(m.get(new Long(v.getCaseId()))==null) {
					ArrayList l2 = new ArrayList();
					l2.add(l.get(i));
					m.put(new Long(v.getCaseId()), l2);
				}
				else ((List<VPMapOverviewItem>) m.get(new Long(v.getCaseId()))).add(v);
			}
			CRTLogger.out("DBSearch(): " + v.getOrgCaseId(), 0);
		}
		return m;	
	}	
}
