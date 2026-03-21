package database;

import java.util.*;

import org.apache.commons.collections.FastArrayList;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * non static reusable version of hibernateUtil static class, this class does the real work....
 * 
 * @author admin
 *
 */
public class HibernateSession {
	/**
	 * SessionFactory provides the sessions for the application. Each session has a database
	 * connection.
	 */
	private SessionFactory factory = null;
	private Configuration cfg = null;
	private Properties alternativeProperties = null;
	/**
	 * turn logging of stacktraces per session get / put on or off -> performance critical!!!!
	 */
	//private boolean logstacktrace = false;
	/**
	 * replaced by threadLocal implementation, now used for documentation only when logstacktrace is true
	 */
	private Map sessions; 
	
	/**
	 * new implementation with thread local.... will replace sessions
	 */
	private ThreadLocal<List> threadLocal = new ThreadLocal<List>();
	
	/**
	 * new implementation with thread local.... will replace sessions
	 */
	private ThreadLocal<List> threadExceptionList = new ThreadLocal<List>();
	
	/**
	 * for faster test scenarios i can limit class addition...
	 */
	public boolean add_all_classes = true;
	
	public HibernateSession() {}
	
	public HibernateSession(Properties p) {alternativeProperties = p;}
	
	/**
	 * @return Returns the session factory. If null, an init-methode will be called first.
	 */
	public SessionFactory getFactory() 
	{ 
		if(factory==null) initHibernate();
		return factory;
	}
	
	public void resetHibernate() {
		try { 
			cfg = null;
			factory = null;
		}
		catch(Throwable th) {}
	}
	
	/**
	 * Initializing Hibernate. Configurationfile is loaded and the SessionFactory is 
	 * built.
	 * Here all classes matching database tables have to be added!!!!
	 */
	public void initHibernate()
	{
		try { 

			if (cfg == null) cfg = createNewConfiguration();
			

			//load classes here....
			cfg.addClass(beans.list.ListItem.class);
			cfg.addClass(beans.list.Synonym.class);
			cfg.addClass(beans.graph.Box.class);	//new box 
			cfg.addClass(beans.scripts.PatientIllnessScript.class);				
			cfg.addClass(beans.relation.RelationProblem.class);
			cfg.addClass(beans.relation.RelationPatho.class);
			cfg.addClass(beans.relation.RelationSyndrome.class);
			cfg.addClass(beans.relation.RelationDiagnosis.class);	
			//cfg.addClass(beans.relation.RelationNursingDiagnosis.class);
			cfg.addClass(beans.relation.RelationInformation.class);
			//fg.addClass(beans.relation.RelationNursingManagement.class);
			cfg.addClass(beans.relation.RelationAim.class);
			//cfg.addClass(beans.relation.RelationMidwifeHypothesis.class);
			//cfg.addClass(beans.relation.RelationMidwifeFinding.class);
			//cfg.addClass(beans.relation.RelationMidwifeManagement.class);
			cfg.addClass(beans.relation.RelationRecommendation.class);
			cfg.addClass(beans.LogEntry.class);
			cfg.addClass(beans.relation.Connection.class);
			cfg.addClass(beans.relation.RelationManagement.class);			
			cfg.addClass(beans.relation.RelationTest.class);	
			cfg.addClass(beans.relation.summary.SummaryStatement.class);	
			//cfg.addClass(beans.helper.Range.class);
			cfg.addClass(beans.scoring.ScoreBean.class);
			cfg.addClass(beans.error.MyError.class);
			cfg.addClass(beans.scoring.FeedbackBean.class);
			cfg.addClass(beans.scoring.PeerBean.class);
			cfg.addClass(beans.user.User.class);
			cfg.addClass(beans.scripts.VPScriptRef.class);
			cfg.addClass(beans.helper.TypeAheadBean.class);
			cfg.addClass(beans.scoring.LearningBean.class);
			cfg.addClass(beans.relation.summary.SemanticQual.class);
			cfg.addClass(beans.relation.summary.SummaryStatementSQ.class);
			cfg.addClass(beans.relation.summary.SummaryStElem.class);
			cfg.addClass(beans.relation.summary.SummaryStNumeric.class);
			cfg.addClass(beans.search.SearchResult.class);
			cfg.addClass(beans.user.SessionSetting.class);
			cfg.addClass(beans.list.SIUnit.class);
			cfg.addClass(beans.relation.summary.TransformRule.class);
			cfg.addClass(beans.context.Actor.class);
			cfg.addClass(beans.context.Context.class);
			cfg.addClass(beans.relation.summary.JsonTest.class); //testing only
			cfg.addClass(beans.helper.VPMapOverviewItem.class);
			cfg.addClass(beans.helper.CaseDifficulty.class);
			//cfg.addClass(test.LMMeshMapping.class);
			
			if (factory==null) {
				//init_setHibernateDialect();
				addAlternativeProperties();
			}
				
			try {factory = cfg.buildSessionFactory();} 
			finally{}
		}
		finally{}

	}
	

	private void addAlternativeProperties() {
		if (this.alternativeProperties != null) {
			Iterator it = alternativeProperties.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				Object value = alternativeProperties.get(key);
				if (key != null && value != null && value instanceof String && ((String) value).equalsIgnoreCase("<null>")) {
					cfg.getProperties().remove(key);
					//Logger.info("HibernateSession.addAlternativeProperties ", "cfg.getProperties().remove=" + key);
				}
				else if (key != null && value != null){
					cfg.setProperty(key.toString(), value.toString());
					//Logger.info("HibernateSession.addAlternativeProperties ", "cfg.setProperty(" + key + ")=" + value);
				}
			}
		}
	}

	public Configuration getConfiguration() {return cfg;}
	
	public Configuration getOrCreateConfiguration() {
		if (cfg == null) {
			cfg = createNewConfiguration();
		}
		return cfg;
	}
	
	public Configuration createNewConfiguration() { return new Configuration();}
	
	/*public Map getAllSessions() {
		return new HashMap(sessions);
	}*/
	
	/**
	 * opens and returns a session from the session factory.
	 * @return Session
	 */
	public Session getInternalSession(Thread t, boolean debug)
	{
		SessionFactory f = this.getFactory();
		Session s = null;
		if(t!=null) s = getInternalSessionByThread(t);

		if(s==null)
		{
			s = f.openSession();
			//Logger.debug ("HibernateSession.getSession","t " + t.hashCode() + ", s:" + s.hashCode() + "," + s.isOpen());
			addSession(s,Thread.currentThread());
		}
		else {
			//Logger.debug ("HibernateSession.getSession","found s: t " + t.hashCode() + ", s:" + s.hashCode() + "," + s.isOpen());
		}
		return  s;             
	}

	public Session getInternalSessionByThread(Thread t)
	{
		List sessionList = null;
		
		sessionList = threadLocal.get();
		if(sessionList!=null)
		{
			for(int i=0; i<sessionList.size();i++)
			{
				Session s = (Session) sessionList.get(i);
				if(s!=null && s.isOpen()) {
					//Logger.debug ("HibernateSession.getSessionByThread","return t:" + t.hashCode() + ", s:" + s.hashCode() + ", " + s.isOpen());
					return s;
				}
				else {
					if (s == null) {
						//Logger.important("HibernateSession.getSessionByThread","t:" + t.hashCode() + ", s==null");
					}
					else {
						//Logger.important ("HibernateSession.getSessionByThread","t:" + t.hashCode() + ", s:" + s.hashCode() + ", " + s.isOpen());
						if (s.isOpen() == false) {
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * if a session has been opened it will be written into the session HashMap with 
	 * the current Thread as key.
	 * @param s
	 * @param t
	 */
	private void addSession(Session s, Thread t)
	{
		List sessionList;
		//logstacktrace = CasusConfiguration.getGlobalBooleanValue("HibernateUtil.logstacktrace", false);
		if (sessions==null) {sessions = new HashMap();
		}

		if(threadLocal.get()==null)
		{
			sessionList = new ArrayList();
			sessionList.add(s);
			{
				List logstacktraceList = new ArrayList();
				//StringObjectHashKey v = new StringObjectHashKey(logstacktrace ? Utility.stackTraceToString(new Exception("HibernateSession.addSession")) : "HibernateSession.addSession",s);
				//logstacktraceList.add(v);
				sessions.put(t,logstacktraceList);
			}
			threadLocal.set(sessionList);
		}
		else
		{
			sessionList = threadLocal.get();
			if(!sessionList.contains(s)) {
				sessionList.add(0,s);
				{
					List logstacktraceList = (List) sessions.get(t);
					//StringObjectHashKey v = new StringObjectHashKey(logstacktrace ? Utility.stackTraceToString(new Exception("HibernateSession.addSession")) : "HibernateSession.addSession",s);
					//logstacktraceList.add(v);
				}
			}
			
			if (sessionList.size()>1) {
				//Utility.dummyX("HibernateSession.addSession -> WARNING multiple hibsession per thread!! t:" + t.hashCode());
				try {
					for(int i=0; i<sessionList.size();i++)
					{
						Session loop = (Session) sessionList.get(i);
						//Logger.serious ("HibernateSession.addSession","t:" + t.hashCode() + ", loop:" + loop.hashCode() + ", " + loop.isOpen());
						//if(loop!=null && loop.isOpen())
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void reallyRemoveSessionsInternal(Thread t)
	{
		try{    
			if(sessions!=null)
			{
				List sessionList = null;
				sessionList = threadLocal.get();
				int sessionListSize = 0;
				if(sessionList!=null && (sessionListSize = sessionList.size())>0)
				{
					for(int i=0; i<sessionListSize;i++)
					{
						Session s = (Session) sessionList.get(i);
						//Logger.debug ("HibernateSession.reallyRemoveSessions","t " + t.hashCode() + ", s:" + s.hashCode());
						try {
							if(s!=null && s.isOpen()) {
								s.clear();
							}
						}
						catch(Throwable x) {
							// ok end of lifecycle
							//Logger.info ("HibernateSession.reallyRemoveSessions","x1:" + x.getMessage());
						}
						
						try {
							if(s!=null && s.isOpen()) {
								s.close();
							}
						}
						catch(Throwable x) {
							// ok end of lifecycle
							//Logger.info ("HibernateSession.reallyRemoveSessions","x2:" + x.getMessage());
						}
						
						try {
							if(s!=null && !s.isOpen() && s.isConnected()) {
								s.disconnect();
							}
						}
						catch(Throwable x) {
							// ok end of lifecycle
							//Logger.info ("HibernateSession.reallyRemoveSessions","x3:" + x.getMessage());
						}
						
						Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
						
					}
					sessionList.clear();
					//Logger.debug ("HibernateSession.reallyRemoveSessions","sessions.size():" + sessions.size());
					Map mysessions = sessions;
					threadLocal.remove();
					sessions.remove(t);
					threadExceptionList.remove();
					//Logger.debug ("HibernateSession.reallyRemoveSessions","sessions.size():" + sessions.size());
					int mysize = sessions.size();
					if (mysize < 0) {
						//Logger.info ("HibernateSession.reallyRemoveSessions","huh? sessions.size():" + sessions.size());
					}
				}  
				else {
					threadLocal.remove();
					sessions.remove(t);
					threadExceptionList.remove();
				}
			}
		}
		catch (Exception e)
		{
			//Logger.serious("HibernateSession.reallyRemoveSessions() ", "Exception: " + Utility.stackTraceToString(e));
		}
	}
	
	public void internalRemoveSessions(Thread t){
		
	}
	
	/**
	 * We look into the session, whether there is a transaction active. 
	 * If not, we begin a transaction 
	 */
	public Transaction beginTransaction() {
		return beginTransaction(this.getInternalSession(Thread.currentThread(),false));
	}
	
	/**
	 * We look into the session, whether there is a transaction active. 
	 * If not, we begin a transaction 
	 */
	public Transaction beginTransaction(Session s) {
		Transaction tx = null;
		try {
			Transaction mytx = s.getTransaction();
			if (mytx != null && mytx.isActive()) {
				tx = mytx;
				//Logger.debug("HibernateSession.beginTransaction()","Session= " + s.hashCode() + ", tx= " + tx.hashCode() + "(is active)");
			}
			else {
				tx = s.beginTransaction();
				//tx.setTimeout(CasusConfiguration.getGlobalIntegerValue("HibnerateSession.beginTransaction.timeout", 120));
				//Logger.debug("HibernateSession.beginTransaction()","Session= " + s.hashCode() + ", tx= " + tx.hashCode());
			}
		}
		catch(Exception x) 
		{
			//Logger.serious("HibernateSession.beginTransaction()","Exception= " + Utility.stackTraceToString(x));
		}
		return tx;
	}

	/**
	 * We look into the session, whether there is a transaction active.
	 * If yes, we commit it.
	 */
	public void commitTransaction()
	{
		try {
			commitTransaction(getInternalSession(Thread.currentThread(),false));
		}
		catch(Exception x) 
		{
			//Logger.serious("HibernateSession.commitTransaction()","Exception= " + Utility.stackTraceToString(x));
		}
	}

	/**
	 * We look into the session, whether there is a transaction active.
	 * If yes, we commit it.
	 * 
	 * with possible thrown Exception!!!!
	 */
	public void commitTransactionWithException()
	{
		commitTransactionWithException(getInternalSession(Thread.currentThread(),false));
	}

	/**
	 * We look into the session, whether there is a transaction active.
	 * If yes, we commit it.
	 */
	public void commitTransaction(Session s)
	{
		Transaction tx = null;
		try {			
			//Logger.debug("HibernateSession.commitTransaction()","Session= " + s.hashCode());

			tx = s.getTransaction();
			if (tx != null && tx.isActive()) {
				//Logger.debug("HibernateSession.commitTransaction()","tx= " + tx.hashCode());
				tx.commit();
			}
			else {
				s.flush();
			}
		}
		catch(Exception x) 
		{
			//Logger.serious("HibernateSession.commitTransaction()","Exception= " + Utility.stackTraceToString(x));
		}
	}

	/**
	 * We look into the session, whether there is a transaction active.
	 * If yes, we commit it.
	 * 
	 * with possible thrown Exception!!!!
	 */
	public void commitTransactionWithException(Session s)
	{
		Transaction tx = s.getTransaction();
		if (tx != null) 
			tx.commit();

	}

	/**
	 * try to rollback a transaction.
	 */
	public void rollBackTx()
	{
		try{
			Session s = getInternalSession(Thread.currentThread(),false);
			Transaction tx = s.getTransaction();
			if (tx!=null && tx.isActive()) tx.rollback();
		}
		catch(Exception e){
			//Logger.serious("HibernateSession.rollBackTx()", "rollback failed:" + e.toString());
		}
	}
	
	/*public List getMyThreadExceptionList() {
		return threadExceptionList.get();
	}
	
	public void removeMyThreadExceptionList() {
		threadExceptionList.remove();
	}*/
	
	/*public void add2ThreadExceptionList(Throwable th) {
		if (th != null) {
			if (th instanceof NullPointerException) {
				//Logger.info("HibernateSesison.add2ThreadExceptionList","npX:" + Utility.stackTraceToString(th));
				return;
			}
			
			
			List myThreadExceptionList = threadExceptionList.get();
			if (myThreadExceptionList == null) {
				myThreadExceptionList = new FastArrayList();
				threadExceptionList.set(myThreadExceptionList);
			}
			
			if (myThreadExceptionList != null && th instanceof RuntimeException) {
				myThreadExceptionList.add(th);
			}
		}
	}*/

	/*public ThreadLocal<List> getThreadExceptionList() {
		return threadExceptionList;
	}*/

	/*public void setThreadExceptionList(ThreadLocal<List> threadExceptionList) {
		this.threadExceptionList = threadExceptionList;
	}*/
	
}
