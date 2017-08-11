package database;

import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.*;

import beans.scripts.*;
import beans.relation.RelationProblem;
import beans.relation.SummaryStatement;
import beans.relation.SummaryStatementSQ;
import controller.IllnessScriptController;
import util.StringUtilities;

public class DBClinReason /*extends HibernateUtil*/{
	static HibernateSession instance = new HibernateSession();
	//static SessionFactory sessionFactory = new SessionFactory();//new Configuration().configure().buildSessionFactory();
	
    /**
     * saves an object into the database. Object has to have a hibernate mapping!
     * @param o
     */
    public void saveAndCommit(Object o){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	//PatientIllnessScript ps = (PatientIllnessScript) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(CRTFacesContext.PATILLSCRIPT_KEY);
        try {
        	instance.beginTransaction();
            s.setFlushMode(FlushMode.COMMIT);     
            s.saveOrUpdate(o);
            instance.commitTransaction(s);
        }
        
        catch(Exception e){
        	System.out.println("DBClinReason.saveAndCommit(), Exception: " + StringUtilities.stackTraceToString(e));
        	instance.rollBackTx();
        }
        finally{
        	    s.flush();
        	    s.evict(o);
        	    s.close();       	   
        }
    }
        
    /**
     * Saves/updates a collection of objects into the database
     * @param o
     */
    public void saveAndCommit(Collection o) {
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
        try {
        	instance.beginTransaction();
            s.setFlushMode(FlushMode.COMMIT);   //commit will be done after insert!
            //tx = s.beginTransaction();
            Iterator it = o.iterator();
            while(it.hasNext())
            {
            	s.saveOrUpdate(it.next());
            }
            instance.commitTransaction(s);
        }
        catch (Exception e) {
        	System.out.println("DBClinReason.saveAndCommit(), Exception: " + StringUtilities.stackTraceToString(e));
        	instance.rollBackTx();
        }
        finally{
    	    s.flush();
    	    s.evict(o);
    	    s.close();
        }
    }
    /**
     * Delete a collection from the database
     * @param o
     */
    public void deleteAndCommit(Collection c){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);   	
        try {
        	instance.beginTransaction(s);
            s.setFlushMode(FlushMode.COMMIT);     
            Iterator it = c.iterator();
            while(it.hasNext())
            {
            	s.delete(it.next());
            }
            instance.commitTransaction(s);
        }        
        catch(Exception e){
        	System.out.println("DBClinReason.deleteAndCommit(), Exception: " + StringUtilities.stackTraceToString(e));
        	instance.rollBackTx();
        }   
        finally{
    	    s.flush();
    	    s.close();
        }
    }  
    
    /**
     * Delete a Bean from the database
     * @param o
     */
    public void deleteAndCommit(Object o){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);   		
        try {
        	instance.beginTransaction(s);
            s.setFlushMode(FlushMode.COMMIT);     
            s.delete(o);
            instance.commitTransaction(s);
        }        
        catch(Exception e){
        	System.out.println("DBClinReason.deleteAndCommit(), Exception: " + StringUtilities.stackTraceToString(e));
        	instance.rollBackTx();
        }  
        finally{
    	    s.flush();
    	    s.close();
        }
    }
    
    /**
     * Select the PatientIllnessScript for the parentId from the database. 
     * @param parentId
     * @return IllnessScript or null
     */
   /* public List<IllnessScript> selectIllScriptByParentId(long parentId){
    	return selectIllScripts(parentId, "parentId");
    }*/
    
    /**
     * Select the PatientIllnessScript for the parentId (e.g. vpId) from the database. 
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    public List<IllnessScript> selectIllScriptByDiagnosisId(long diagnosisId){
    	return selectIllScripts(diagnosisId, "diagnosisId");
    }
 
    /**
     * Select the IllnessScript for a given list of problems   
     * @param sessionId
     * @return PatientIllnessScript or null
     * TODO
     */
    public List<IllnessScript> selectIllScriptByProblems(List<RelationProblem> probs){
    	return null; //we need a matching algorithm here....
    }
    
    public List<IllnessScript> selectIllScriptByDiagnoses(List ddx){
    	if(ddx==null) return null; 
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(IllnessScript.class,"IllnessScript");
    	Long[] ids = new IllnessScriptController().getListItemsFromRelationList(ddx);
    	if(ids!=null){
    		criteria.add(Restrictions.in("diagnosisId", ids));
    		return criteria.list();
    	}
    	else return null;
    }
    /**
     * Select the IllnessScript
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    private List<IllnessScript> selectIllScripts(long id, String identifier){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(IllnessScript.class,"IllnessScript");
    	criteria.add(Restrictions.eq(identifier, new Long(identifier)));
    	List<IllnessScript> l =  (List<IllnessScript>) criteria.list();
    	s.close();
    	return l;
    }
    
    /**
     * Select the PatientIllnessScript for the sessionId from the database. 
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    /*public PatientIllnessScript selectPatIllScriptBySessionId(long sessionId){
    	return selectLearnerPatIllScript(sessionId, "sessionId");
    }*/
    
    /**
     * Select the PatientIllnessScript of the expert, which is identified by the parentId and type from the database. 
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    public PatientIllnessScript selectExpertPatIllScriptByVPId(String vpId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("vpId", vpId));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_EXPERT_CREATED)));
    	PatientIllnessScript patIllScript =  (PatientIllnessScript) criteria.uniqueResult();
    	s.close();
    	if(patIllScript!=null){
    		patIllScript.setSummSt(loadSummSt(patIllScript.getSummStId()));
    	}
    	return patIllScript;  	
    }
    
    /**
     * loads all learner scripts for a given vpId, called from the reports section....
     * Doe NOT load the summary statements!!!
     * @param vpId
     * @return
     */
    public List<PatientIllnessScript> selectLearnerPatIllScriptsByVPId(String vpId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("vpId", vpId));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	List patIllScripts =  criteria.list();
    	s.close();
    	//if(patIllScripts==null) return null;
    	return patIllScripts;
    	/*if(patIllScript!=null)
    		patIllScript.setSummSt(loadSummSt(patIllScript.getSummStId()));
    	return patIllScript;  */	
    }
   
    
    /**
     * Select the PatientIllnessScript for the sessionId from the database. 
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    public PatientIllnessScript selectPatIllScriptById(long id){
    	return selectLearnerPatIllScript(id, "id");
    }
    
    private PatientIllnessScript selectLearnerPatIllScript(long id, String identifier){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq(identifier, new Long(id)));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	PatientIllnessScript patIllScript =  (PatientIllnessScript) criteria.uniqueResult();
    	s.close();
    	if(patIllScript!=null)
    		patIllScript.setSummSt(loadSummSt(patIllScript.getSummStId()));
    	return patIllScript;
    }

    /**
     * Select the PatientIllnessScripts for the userId from the database. 
     * Beware: summStatement not loaded!
     * We only load the latest script of a user (not any previous, which have a deleteFlag=1) 
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    public List<PatientIllnessScript> selectActivePatIllScriptsByUserId(long userId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("userId", new Long(userId)));
    	criteria.add(Restrictions.eq("deleteFlag", new Boolean(false)));
    	criteria.addOrder(Order.desc("lastAccessDate"));
    	return  criteria.list();
    }
    
    /**
     * Select the PatientIllnessScripts for the userId and parentId from the database. 
     * @param sessionId
     * @param vpId
     * @param uId (an optional unique id that can be transferred from the VP system, e.g. a sessionId)
     * @return PatientIllnessScript or null
     */
    public PatientIllnessScript selectPatIllScriptsByUserIdAndVpId(long userId, String vpId, String extUId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("userId", new Long(userId)));
    	criteria.add(Restrictions.eq("vpId", vpId));
    	if(extUId!=null && !extUId.trim().equals("")) criteria.add(Restrictions.eq("extUId", extUId));
    	criteria.add(Restrictions.eq("type", PatientIllnessScript.TYPE_LEARNER_CREATED));
    	PatientIllnessScript patIllScript =  (PatientIllnessScript) criteria.uniqueResult();
    	if(patIllScript!=null)
    		patIllScript.setSummSt(loadSummSt(patIllScript.getSummStId()));
    	return patIllScript;
    }
    
    /**
     * Select the PatientIllnessScripts for the userId and parentId from the database. 
     * @param sessionId
     * @param vpId
     * @param uId (an optional unique id that can be transferred from the VP system, e.g. a sessionId)
     * @return PatientIllnessScript or null
     */
    public List<PatientIllnessScript> selectPatIllScriptsByUserIdAndVpId(long userId, String vpId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("userId", new Long(userId)));
    	criteria.add(Restrictions.eq("vpId", vpId));
    	criteria.add(Restrictions.eq("type", PatientIllnessScript.TYPE_LEARNER_CREATED));
    	return criteria.list();
    }
    
    
    /**
     * Selects all PatientIllnessScripts that can be included into the peer responses. 
     * Only scripts that have not yet been added (peerSync=0), scripts that have submitted a diagnosis (submittedStage>0),
     * and learner scripts (not expert ones) are considered and selected.
     * @return PatientIllnessScript or null
     */
    public List<PatientIllnessScript> selectLearnerPatIllScriptsByPeerSync(){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("peerSync", new Boolean(false)));
    	criteria.add(Restrictions.gt("submittedStage", 0));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	return criteria.list();
    }
    
    /**
     * Select the PatientIllnessScripts for the userId and parentId from the database. 
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    /*public List<PatientIllnessScript> selectLearnerPatIllScriptsByDate(){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	//criteria.add(Restrictions.eq("peerSync", new Boolean(false)));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	//TODO for peer calculation, we have to add a date range otherwise this is too much....
    	return criteria.list();
    }*/
    
	public SummaryStatement loadSummSt(long id){
		if(id<=0) return null;
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(SummaryStatement.class,"SummaryStatement");
		criteria.add(Restrictions.eq("id", new Long(id)));
		SummaryStatement st = (SummaryStatement) criteria.uniqueResult();
		st.setSqHits(selectSummaryStatementSQsBySumId(st.getId()));
		return st;
	}
	
	private List selectSummaryStatementSQsBySumId(long summStId){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(SummaryStatementSQ.class,"SummaryStatementSQ");
		criteria.add(Restrictions.eq("summStId", new Long(summStId)));
		return criteria.list();
	}
	
	/**
	 * Get all summary statements (experts & learners) depending of analyze status. Called on start 
	 * to analyze all non-analyzed statements.
	 * @param type
	 * @param analyzed
	 * @return
	 */
	public List<SummaryStatement> getSummaryStatementsByAnalyzed(boolean analyzed){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(SummaryStatement.class,"SummaryStatement");
		//criteria.add(Restrictions.eq("type", new Integer(type)));
		criteria.add(Restrictions.eq("analyzed", new Boolean(analyzed)));
		return criteria.list();
	}
	
	public static List<VPScriptRef> getVPScriptRefs(){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(VPScriptRef.class,"VPScriptRef");
		return criteria.list();		
	}
}
