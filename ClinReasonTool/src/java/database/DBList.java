package database;

import java.util.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.ejb.criteria.expression.function.TrimFunction;

import beans.list.*;
import beans.relation.summary.SemanticQual;
import beans.relation.summary.TransformRule;
import util.CRTLogger;

/**
 * Contains all access methods for the list loading (mesh, etc)
 * @author ingahege
 *
 */
public class DBList extends DBClinReason {
 
	/**
     * Select the ListItem with the given id from the database.
     * @param id
     * @return ListItem or null
     */
    public ListItem selectListItemById(long id){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.add(Restrictions.eq("item_id", new Long(id)));
    	ListItem li = (ListItem) criteria.uniqueResult();
    	s.close();
    	return li;
    }
    
    public ListItem saveNewEntry(String entry, Locale loc){
    	return saveNewEntry(entry, loc.getLanguage());
    }
    /**
     * Learner has chosen to add his/her own entry, so we add this entry marked as "PRIVATE" to the list.
     * @param entry
     * @param loc
     * @return
     */
    public ListItem saveNewEntry(String entry, String loc){
    	if(entry==null || entry.trim().equals("")) return null;
    	ListItem li = new ListItem(loc, ListItem.TYPE_OWN, entry.trim());
    	saveAndCommit(li);
    	return li;
    }
    

    
    /**
     * Select the ListItem with the given meshId from the database.
     * Just needed for importing German Mesh List.
     * @param id
     * @return ListItem or null
     */
    public ListItem selectListItemByMeshIdAndLang(String id, String lang){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.add(Restrictions.eq("mesh_id", id));
    	criteria.add(Restrictions.eq("language", new Locale(lang)));
    	ListItem li = (ListItem) criteria.uniqueResult();
    	s.close();
    	return li;
    }
    
    /**
     * CAVE: Do not call, just for testing!!!
     * @param id
     * @return ListItem or null
     */
    public List<ListItem> selectListItemByLang(String lang){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.add(Restrictions.eq("language", new Locale(lang)));
    	return criteria.list();
    	//s.close();
    	//return li;
    }
    
    public ListInterface selectListItemByLangAndLabel(String lang, String label){
    	String[] sources = new String[]{ListItem.TYPE_ADDED, ListItem.TYPE_MESH};
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.add(Restrictions.eq("language", new Locale(lang)));
    	criteria.add(Restrictions.like("name", label));
    	criteria.add(Restrictions.in("source", sources));
    	ListItem li =  (ListItem) criteria.uniqueResult();
    	if(li!=null) return li;
    	
    	Criteria criteria2 = s.createCriteria(Synonym.class,"ListItem");
    	criteria2.add(Restrictions.eq("ignored", false));
    	if(lang!=null && !lang.trim().equals("")) criteria2.add(Restrictions.eq("language", new Locale(lang)));
    	criteria2.add(Restrictions.like("name", label));
    	return (ListInterface) criteria2.uniqueResult();
    	
    }
    
    /**
     * Searching for ListItems (admin interface)
     * @param lang
     * @param term
     * @param sources
     * @return List of ListItems or null
     */
    private List<ListInterface> selectListItemByLangAndTerm(String lang, String term, String[] sources){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.add(Restrictions.eq("ignored", false)); //we also could include that and reset the "ignore flag"
    	criteria.add(Restrictions.in("source", sources));
    	if(lang!=null && !lang.trim().equals("")) criteria.add(Restrictions.eq("language", new Locale(lang)));
    	criteria.add(Restrictions.ilike("name", term, MatchMode.ANYWHERE));

    	List l =  criteria.list();
    	return l;
    	//s.close();
    }
    
    /**
     * Select all matching items, that have been entered manually by users
     * @param lang
     * @param term
     * @return
     */
    public List<ListInterface> selectPrivateListItemsByLangAndTerm(String lang, String term){
    	return selectListItemByLangAndTerm(lang, term, new String[]{ListItem.TYPE_OWN});
    }
    
    /**
     * select all matching entries that are part of the list (either a MeSH term or a added term)
     * @param lang
     * @param term
     * @return
     */
    public List<ListInterface> selectPublicListItemsByLangAndTerm(String lang, String term){
    	return selectListItemByLangAndTerm(lang, term, new String[]{ListItem.TYPE_ADDED, ListItem.TYPE_MESH});
    }
    
    /**
     * Searching for ListItems (admin interface), synonyms are all public, since users can only add their own entries 
     * as main list items.
     * @param lang
     * @param term
     * @return List of ListItems or null
     */
    public List<ListInterface> selectSynonymsByLangAndTerm(String lang, String term){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(Synonym.class,"ListItem");
    	criteria.add(Restrictions.eq("ignored", false));
    	if(lang!=null && !lang.trim().equals("")) criteria.add(Restrictions.eq("language", new Locale(lang)));
    	criteria.add(Restrictions.like("name", term, MatchMode.ANYWHERE));

    	return criteria.list();
    	//s.close();
    }
    
    /**
     * Loads ListItems for the given types. CAVE: This returns lots of items, only call during init of application 
     * or for testing!
     * professionType: 0 = Medicine, 1 = Nursing
     * professionVariant added for future use with special lists, not yet used but already prepared
     * @param types
     * @return
     */
    public List<ListItem> selectListItemsByTypesAndLang(Locale loc, String[] types, int professionType, int professionVariant){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	if (types != null && types.length>0 ) {
    		criteria.add(Restrictions.in("itemType", types));
    	}
    	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    	criteria.add(Restrictions.eq("language", loc));
    	//do NOT include items that have been added by learners:
    	criteria.add(Restrictions.ne("source", ListItem.TYPE_OWN));
    	criteria.addOrder(Order.asc("name"));
    	if (professionType>=0) {
    		criteria.add(Restrictions.eq("nursing", professionType));
    	}
    	
    	criteria.add(Restrictions.eq("ignored", false));
    	List l = criteria.list();
    	//Collections.sort(l);
    	s.close();
    	return l;
    }
    
    /**
     * Loads ListItems for the given types. CAVE: This returns lots of items, only call during init of application 
     * or for testing!
     * professionType: 0 = Medicine, 1 = Nursing
     * @param types
     * @return
     * @deprecated please us selectListItemsByTypesAndLang above with types == null!
     */
    public List<ListItem> selectListItemsByProfessionAndLang(Locale loc, int professionType){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    	criteria.add(Restrictions.eq("language", loc));
    	//do NOT include items that have been added by learners:
    	criteria.add(Restrictions.ne("source", ListItem.TYPE_OWN));
    	criteria.addOrder(Order.asc("name"));
    	criteria.add(Restrictions.eq("nursing", professionType));
    	criteria.add(Restrictions.eq("ignored", false));
    	List l = criteria.list();
    	//Collections.sort(l);
    	s.close();
    	return l;
    }
    
    public Synonym selectSynonymById(long id){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(Synonym.class,"Synonym");
    	criteria.add(Restrictions.eq("id", new Long(id)));
    	Synonym li = (Synonym) criteria.uniqueResult();
    	s.close();
    	return li;
    }
    
    /**
     * We select (based on the code of the MESH item all parent and child items. (e.g. for cough the first parent item would be 
     * "respiratory disorders). 
     *  
     * @param li
     * @return list of ListItem objects or null
     */
    public List<ListItem> selectParentAndChildListItems(ListItem li){
    	long startms = System.currentTimeMillis();
    	if(li==null) return null;

    	String code = li.getFirstCode();
    	if(code==null || code.indexOf(".")<0) return null;
    	CRTLogger.out("Start selectParentAndChildListItems " + startms + "ms", CRTLogger.LEVEL_PROD);

    	List<ListItem> items = new ArrayList<ListItem>();
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	
    	//select & add childs:
		List<ListItem> childs = selectChildListItemsByCode(code, s, li.getLanguage());
		if(childs!=null && !childs.isEmpty()) items.addAll(childs);
    	
		//select and add all parents:
		while(code.indexOf(".")>0){
			code = code.substring(0, code.lastIndexOf("."));
			if(code==null || code.trim().equals("")) break;
			ListItem l = selectListItemByCode(code, s, li.getLanguage());
			if(l!=null) items.add(l);
		}

		s.close();
		CRTLogger.out("End selectParentAndChildListItems " + (System.currentTimeMillis() - startms) + "ms", CRTLogger.LEVEL_PROD);
		return items;		
    }
    
	/**
     * Select the ListItem with the given id from the database.
     * @param id
     * @return ListItem or null
     */
    private ListItem selectListItemByCode(String code, Session s, Locale lang){   
    	ListItem li = null;
    	try {
	    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
	    	criteria.add(Restrictions.eq("firstCode", code));
	    	criteria.add(Restrictions.eq("language", lang));
	    	li = (ListItem) criteria.uniqueResult();
	    	
    	}
    	catch(Exception e) {
    		return null;
    	}
    	return li;
    }
    
    public ListItem selectListItemByCode(String code, Locale lang){ 
    	return selectListItemByCode(code, instance.getInternalSession(Thread.currentThread(), false), lang);
    }
    
    private List<ListItem> selectChildListItemsByCode(String code, Session s, Locale lang){
       	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.add(Restrictions.like("firstCode", code+".", MatchMode.START));
    	criteria.add(Restrictions.eq("language", lang));
    	return criteria.list();
    }
    
    public List<ListItem> selectListItemsByCode(String code){ 
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	criteria.add(Restrictions.eq("firstCode", code));
    	List l = (List<ListItem>) criteria.list();   	
    	return l;    	
    }
    
    /*public List<ListItem> selectListItemBySearchTerm(String searchTerm, Locale lang){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ListItem.class,"ListItem");
    	//criteria.add(Restrictions.eq("language", lang));
    	criteria.add(Restrictions.eq("ignored", false));
    	criteria.add(Restrictions.ilike("name", searchTerm.toLowerCase(), MatchMode.ANYWHERE));
    	return criteria.list();
    }*/
    
	/**
     * Select the ListItem with the given id from the database.
     * @param id
     * @return ListItem or null
     */
    public List<SemanticQual> selectSemanticQuals(String lang){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(SemanticQual.class,"SemanticQual");
    	criteria.add(Restrictions.eq("lang", lang));
    	criteria.add(Restrictions.eq("deleteFlag", new Integer(0)));
    	return criteria.list();
    }
    
    public List<SIUnit> selectSIUnits(){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(SIUnit.class,"SIUnit");
    	return criteria.list();
    }
    
    public List<TransformRule> selectTransformRules(){
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(TransformRule.class,"TransformRule");
    	return criteria.list();
    }        
    
    
    /**
     * hack for ChapterSea nursing implementation
     */
    public List<ChapterSeaNursingItem>loadChapterSeaNursingList() {
    	Session s = instance.getInternalSession(Thread.currentThread(), false);
    	Criteria criteria = s.createCriteria(ChapterSeaNursingItem.class,"ChapterSeaNursingItem");
    	return criteria.list();
    	
    }
}
