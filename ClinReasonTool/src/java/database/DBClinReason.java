package database;

import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.*;

import beans.scripts.PatientIllnessScript;
import beans.scripts.VPScriptRef;
//import beans.LogEntry;
import beans.relation.*;
import beans.relation.summary.*;
import util.*;

public class DBClinReason /*extends HibernateUtil*/{
	static HibernateSession instance = new HibernateSession();
	
    /**
     * saves an object into the database. Object has to have a hibernate mapping!
     * @param o
     */
    public void saveAndCommit(Object o){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
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
    	
    	if(patIllScript!=null){
    		patIllScript.setSummSt(loadSummSt(patIllScript.getSummStId(), s));
    		selectNodesAndConns(patIllScript, s);
    	}
    	s.close();
    	return patIllScript;  	
    }
    
    /**
     * loads all learner scripts for a given vpId, called from the reports section....
     * Do NOT load the summary statements!!!
     * @param vpId
     * @return
     */
    public List<PatientIllnessScript> selectLearnerPatIllScriptsByVPId(String vpId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("vpId", vpId));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	List<PatientIllnessScript> patIllScripts =  criteria.list();
    	if(patIllScripts!=null){   		
    		for(int i=0; i<patIllScripts.size(); i++){
    			selectNodesAndConns(patIllScripts.get(i), s);
    		}
    	}
    	s.close();
    	
    	return patIllScripts;

    }
    
    /**
     * loads all learner scripts by given pisID
     *
     * @param vpId
     * @return
     */
    /*public List<PatientIllnessScript> selectLearnerPatIllScriptsById(long pisId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("id", pisId));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	List<PatientIllnessScript> patIllScripts =  criteria.list();
    	if(patIllScripts!=null){   		
    		for(int i=0; i<patIllScripts.size(); i++){
    			selectNodesAndConns(patIllScripts.get(i), s);
    		}
    	}
    	s.close();
    	
    	return patIllScripts;
    }*/

    
    /**
     * Select the PatientIllnessScript for the sessionId from the database. 
     * @param sessionId
     * @return PatientIllnessScript or null
     */
    public PatientIllnessScript selectPatIllScriptById(long id){
    	return selectAllIllScriptById(id, "id", false);
    }
    
    public PatientIllnessScript selectLearnerPatIllScript(long id, String identifier){
    	return selectAllIllScriptById(id, "id", true);
    }
    
    public PatientIllnessScript selectAllIllScriptById(long id, String identifier, boolean considerType){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq(identifier, new Long(id)));
    	if(considerType) criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	PatientIllnessScript patIllScript =  (PatientIllnessScript) criteria.uniqueResult();
    	
    	if(patIllScript!=null){
    		patIllScript.setSummSt(loadSummSt(patIllScript.getSummStId(), s));
    		selectNodesAndConns(patIllScript, s);
    	}
    	else {
    		CRTLogger.out("DBClinReason.selectAllIllScriptById: long: "  + id + ", identifier: " + identifier + ", considerType: " + considerType+ ", criteria: " + criteria, CRTLogger.LEVEL_PROD);

    	}
    	s.close();
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
    	List<PatientIllnessScript> scripts =   criteria.list();
    	if(scripts==null) return null;
    	for(int i=0;i<scripts.size(); i++){
    		selectNodesAndConns(scripts.get(i), s);
    	}
    	s.close();
    	return scripts;
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
    	if(patIllScript!=null){
    		patIllScript.setSummSt(loadSummSt(patIllScript.getSummStId(), s));
    		selectNodesAndConns(patIllScript, s);
    	}
    	s.close();
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
    	
    	//We do not need to load nodes here...
    	return
    			criteria.list();
    }
    
    public List<PatientIllnessScript> selectPatIllScriptsByExtUserIdsAndVpId(String[] extUserIds, String vpId){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.in("extUId", extUserIds ));
    	criteria.add(Restrictions.eq("vpId", vpId));
    	criteria.add(Restrictions.eq("type", PatientIllnessScript.TYPE_LEARNER_CREATED));
    	
    	List<PatientIllnessScript> scripts = criteria.list();
    	if(scripts!=null){
    		for(int i=0;i<scripts.size();i++){
    			selectNodesAndConns(scripts.get(i), s);
    			scripts.get(i).setSummSt(loadSummSt(scripts.get(i).getId(), s));
    		}
    	}
    	s.close();
    	return scripts;
    }
    
    
    /**
     * for recalculation and scoring of summary statements in new format
     * @return PatientIllnessScript or null
     */
    public List selectLearnerPatIllScriptsByNotAnalyzedSummSt(int max, Date startDate, Date endDate, int type, boolean loadNodes, int analyzed, boolean submittedStage, int recalc){
    	/*
    	 * select * from CRT.PATIENT_ILLNESSSCRIPT pis,CRT.SUMMSTATEMENT smst where pis.SUMMST_ID = smst.ID and smst.ANALYZED = 0 and lang in ('de', 'en') and pis.TYPE = 1
    	 */
		CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByNotAnalyzedSummSt: max: "  + max + ", startDate: " + startDate + ", endDate: " + endDate+ ", type: " + type+ ", loadNodes: " + loadNodes+ ", analyzed: " + analyzed+ ", submittedStage: " + submittedStage, CRTLogger.LEVEL_PROD);
   	
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("type", new Integer(type)));
    	
    	List<String> lang = new ArrayList<String>();
    	lang.add("de");
    	lang.add("en");
    	
    	List<Locale> locale = new ArrayList<Locale>();
    	locale.add(Locale.GERMAN);
    	locale.add(Locale.ENGLISH);
    	
    	// sub query:
    	DetachedCriteria stmts = DetachedCriteria.forClass(SummaryStatement.class, "stmt")
    			.setProjection( Property.forName("stmt.id") )
    			.add(Property.forName("stmt.lang").in(lang));
    	
    	if (analyzed>=0) {
    		stmts.add( Property.forName("stmt.analyzed").eq(analyzed == 1 ? Boolean.TRUE : Boolean.FALSE) );
    	}
    	
    	if (recalc>=0) {
    		stmts.add( Property.forName("stmt.recalcMode").eq(recalc) );
    	}
    	
    	if (startDate != null) {
    		stmts.add(Restrictions.ge("stmt.creationDate", startDate));
    	}
    	if (endDate != null) {
    		stmts.add(Restrictions.le("stmt.creationDate", endDate));
    	}
    	
    	// now query
    	criteria.add(Property.forName("summStId").in(stmts));
    	if (max>0) {
    		criteria.setMaxResults(max);
    	}

    	if (submittedStage) {
    		criteria.add(Property.forName("submittedStage").gt(Integer.valueOf(0)));
    	}
    	
    	Disjunction disjunction = Restrictions.disjunction();
    	disjunction.add(Property.forName("pis2.summStId").le(Long.valueOf(0)));
    	disjunction.add(Restrictions.not(Property.forName("pis2.locale").in(locale)));
    	
    	//select distinct(vp_id) from CRT.PATIENT_ILLNESSSCRIPT where type=2 and SUMMST_ID = -1
    	DetachedCriteria pis2 = DetachedCriteria.forClass(PatientIllnessScript.class, "pis2")
    			.setProjection( Projections.distinct(Property.forName("pis2.vpId")) )
    			.add(Property.forName("pis2.type").eq(Integer.valueOf(2)))
    			.add(disjunction);
    	
    	criteria.add(Property.forName("vpId").notIn(pis2));
    	    	
    	criteria.addOrder(Order.asc("creationDate"));
		CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByNotAnalyzedSummSt: criteria: "  + criteria + ", " + stmts + "," + pis2, CRTLogger.LEVEL_PROD);
  	
    	List<PatientIllnessScript> scripts = criteria.list();
    	long smstMs = 0;
    	long nodesMs = 0;
    	long startms = 0;
    	long endms = 0;
    	if(scripts!=null){
    		CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByNotAnalyzedSummSt: scripts: #"  + scripts.size(), CRTLogger.LEVEL_PROD);
    		for(int i=0;i<scripts.size();i++){
    			if ((i%250) == 0) {
    				CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByNotAnalyzedSummSt: scripts: i:"  + i, CRTLogger.LEVEL_PROD);
    			}
    			
    			PatientIllnessScript loop = scripts.get(i); 
    			startms = System.currentTimeMillis() ;
    			loop.setSummSt(loadSummSt(loop.getSummStId(), s));
    			endms = System.currentTimeMillis() ;
    			smstMs += (endms-startms);
    			
    			if (loadNodes) {
        			startms = System.currentTimeMillis() ;
    				selectNodesAndConns(loop, s);
        			endms = System.currentTimeMillis() ;
        			nodesMs += (endms-startms);
    			}
    		}
    		
    		CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByNotAnalyzedSummSt: finish post process: smstMs: "  + smstMs + "ms; nodesMs: "  + nodesMs + "ms; ", CRTLogger.LEVEL_PROD);
    	}
    	else {
    		CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByNotAnalyzedSummSt: finish post process: scripts == null !!", CRTLogger.LEVEL_PROD);
    	}
    	
    	s.close();
    	
    	return scripts;    	
    }
    
    
    /**
     * Selects all PatientIllnessScripts that can be included into the peer responses. 
     * Only scripts that have not yet been added (peerSync=0), scripts that have submitted a diagnosis (submittedStage>0),
     * and learner scripts (not expert ones) are considered and selected.
     * @return PatientIllnessScript or null
     */
    public List<PatientIllnessScript> selectLearnerPatIllScriptsByPeerSync(int max, Date startDate, Date endDate){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("peerSync", new Boolean(false)));
    	criteria.add(Restrictions.gt("submittedStage", 0));
    	criteria.add(Restrictions.eq("type", new Integer(PatientIllnessScript.TYPE_LEARNER_CREATED)));
    	
    	if (max>0) {
    		criteria.setMaxResults(max);
    	}
    	
    	if (startDate != null) {
    		criteria.add(Restrictions.ge("lastAccessDate", startDate));
    	}
    	if (endDate != null) {
    		criteria.add(Restrictions.le("lastAccessDate", endDate));
    		
    	}
    	//long minusOneYearMS = System.currentTimeMillis() - 1000 * 3600 * 24 * 365;
    	//Date minusOneYear = new Date(minusOneYearMS);
    	//criteria.add(Restrictions.gt("lastAccessDate", minusOneYear));
    	
    	List<PatientIllnessScript> scripts = criteria.list();
    	if(scripts!=null){
    		CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByPeerSync: scripts: #"  + scripts.size(), CRTLogger.LEVEL_PROD);
    		for(int i=0;i<scripts.size();i++){
    			if ((i%2500) == 0) {
    				CRTLogger.out("DBClinReason.selectLearnerPatIllScriptsByPeerSync: scripts: i:"  + i, CRTLogger.LEVEL_PROD);
    			}
    			selectNodesAndConns(scripts.get(i), s);
    		}
    	}
    	s.close();
    	return scripts;    	
    }
    
	public SummaryStatement loadSummSt(long id, Session s){
		if(id<=0) return null;
		if(s==null) s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(SummaryStatement.class,"SummaryStatement");
		criteria.add(Restrictions.eq("id", new Long(id)));
		SummaryStatement st = (SummaryStatement) criteria.uniqueResult();
		st.setSqHitsAsList(selectSummaryStatementSQsBySumId(st.getId(), s));
		return st;
	}

	public SummaryStatement selectExpSummStByVPId(long id, Session s){
		if(id<=0) return null;
		if(s==null) s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(SummaryStatement.class,"SummaryStatement");
		criteria.add(Restrictions.eq("id", new Long(id)));
		SummaryStatement st = (SummaryStatement) criteria.uniqueResult();
		st.setSqHitsAsList(selectSummaryStatementSQsBySumId(st.getId(), s));
		return st;
	}
	
	private List selectSummaryStatementSQsBySumId(long summStId, Session s){
		//Session s = instance.getInternalSession(Thread.currentThread(), false);
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
		criteria.add(Restrictions.eq("analyzed", new Boolean(analyzed)));
		return criteria.list();
	}
	
	/**
	 * Get all summary statements (experts & learners) depending of analyze status. Called on start 
	 * to analyze all non-analyzed statements.
	 * @param type
	 * @param analyzed
	 * @return
	 */
	public List<SummaryStatement> getSummaryStatementsById(Long[] ids){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(SummaryStatement.class,"SummaryStatement");
		criteria.add(Restrictions.in("id", ids));
		return criteria.list();
	}
	
	public static List<VPScriptRef> getVPScriptRefs(){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(VPScriptRef.class,"VPScriptRef");
		return criteria.list();		
	}
	
	public VPScriptRef getVPScriptRef(String parentId){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(VPScriptRef.class,"VPScriptRef");
		criteria.add(Restrictions.eq("parentId", new String(parentId)));
		return (VPScriptRef) criteria.uniqueResult();		
	}
	
	protected void selectNodesAndConns(PatientIllnessScript patIllScript, Session s){
		patIllScript.setProblems(selectProblemsForScript(s, patIllScript.getId()));
		patIllScript.setPatho(selectPathosForScript(s, patIllScript.getId()));
		patIllScript.setDiagnoses(selectDiagnosesForScript(s, patIllScript.getId()));
		patIllScript.setTests(selectTestsForScript(s, patIllScript.getId()));
		patIllScript.setMngs(selectMngsForScript(s, patIllScript.getId()));
		patIllScript.setConns(selectConnsForScript(s, patIllScript.getId()));
		patIllScript.setAims(selecAimsForScript(s, patIllScript.getId()));
		patIllScript.setInformation(selectInfosForScript(s, patIllScript.getId()));
		//patIllScript.setNursingDiagnoses(selectNursingDDXForScript(s, patIllScript.getId()));	
		//patIllScript.setMidwifeManagement(selectMMngForScript(s, patIllScript.getId()));
		//patIllScript.setMidwifeFindings(selectMFdgForScript(s, patIllScript.getId()));
		//patIllScript.setMidwifeHypotheses(selectMHypForScript(s, patIllScript.getId()));
		//patIllScript.setMidwifeRecommendations(selectMRecForScript(s, patIllScript.getId()));	
		//patIllScript.setNursingManagement(selectNursingMngsForScript(s, patIllScript.getId()));
	}
	
	private List<RelationProblem> selectProblemsForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationProblem.class,"RelationProblem");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}

	private List<RelationPatho> selectPathosForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationPatho.class,"RelationPatho");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}
	private List<RelationDiagnosis> selectDiagnosesForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationDiagnosis.class,"RelationDiagnosis");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		//criteria.add(Restrictions.eq("discriminator", new Integer(Relation.TYPE_DDX)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}
	
	private List<RelationTest> selectTestsForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationTest.class,"RelationTest");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}
	
	private List<RelationManagement> selectMngsForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationManagement.class,"RelationManagement");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		//criteria.add(Restrictions.eq("discriminator", new Integer(Relation.TYPE_MNG)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}
		
	private List<RelationAim> selecAimsForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationAim.class,"RelationAim");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}
		
	private List<RelationInformation> selectInfosForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationInformation.class,"RelationInformation");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}
	
	/*private List<RelationNursingManagement> selectNursingMngsForScript(Session s, long patIllscriptId){
	Criteria criteria = s.createCriteria(RelationNursingManagement.class,"RelationNursingManagement");
	criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
	criteria.add(Restrictions.eq("discriminator", new Integer(Relation.TYPE_NMNG)));
	criteria.addOrder(Order.asc("order"));
	return criteria.list();	
	}*/
	
	/*private List<RelationNursingDiagnosis> selectNursingDDXForScript(Session s, long patIllscriptId){
	Criteria criteria = s.createCriteria(RelationNursingDiagnosis.class,"RelationNursingDiagnosis");
	criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
	criteria.add(Restrictions.eq("discriminator", new Integer(Relation.TYPE_NDDX)));
	criteria.addOrder(Order.asc("order"));
	return criteria.list();	
	}*/
	
	/*private List<RelationMidwifeManagement> selectMMngForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationMidwifeManagement.class,"RelationMidwifeManagement");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}*/
	
	/*private List<RelationMidwifeFinding> selectMFdgForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationMidwifeFinding.class,"RelationMidwifeFinding");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}*/
	
	/*private List<RelationMidwifeHypothesis> selectMHypForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationMidwifeHypothesis.class,"RelationMidwifeHypothesis");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}*/
	
	/*private List<RelationMidwifeRecommendation> selectMRecForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(RelationMidwifeRecommendation.class,"RelationMidwifeRecommendation");
		criteria.add(Restrictions.eq("destId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();	
	}*/
	
	private Map<Long, Connection> selectConnsForScript(Session s, long patIllscriptId){
		Criteria criteria = s.createCriteria(Connection.class, "Connection");
		criteria.add(Restrictions.eq("illScriptId", new Long(patIllscriptId)));
		criteria.addOrder(Order.asc("id"));
		List<Connection> conns = criteria.list();
		if(conns==null) return null;
		Map<Long, Connection> connMap = new TreeMap<Long, Connection>();
		for(int i=0;i<conns.size(); i++){
			connMap.put(new Long(conns.get(i).getId()), conns.get(i));
		}
		return connMap;
	}
	

	
	public JsonTest selectJsonTestBySummStId(long id){
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(JsonTest.class, "JsonTest");
		criteria.add(Restrictions.eq("id", new Long(id)));
		return (JsonTest) criteria.uniqueResult();
	}
	
	/**
	 * temporary method to select scripts without a sessionId and add the encoded sessionId
	 * @return
	 */
	public List selectTmpPatientIllScripts() {
		Session s = instance.getInternalSession(Thread.currentThread(), false);
		Criteria criteria = s.createCriteria(PatientIllnessScript.class,"PatientIllnessScript");
    	criteria.add(Restrictions.eq("sessionId", new Long(-1)));
    	criteria.add(Restrictions.eq("type", PatientIllnessScript.TYPE_LEARNER_CREATED));
    	
    	return criteria.list();
	}
}
