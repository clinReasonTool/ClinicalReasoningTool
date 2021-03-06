package controller;

import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import beans.search.SearchBean;
import beans.search.SearchResult;
import database.DBSearch;

/**
 * We can search for scripts of a user (learner or editor) based on a search term
 * The results can be used to directly filter the display or returned as an xml file for VP- or portfolio systems.
 * @author ingahege
 * 
 */
@ManagedBean(name = "searchctr", eager = true)
@RequestScoped
public class SearchController {
	
	private static final int MINLENGTH_SEARCHTERM = 3;
	
	//TODO: we need a module id later on, to decide which scripts the user has access to...
	
	public SearchController(){
		//doSearch(createSearchBeanForSearch());
	}
	
	public String getTest(){
		return "hallo";}
	
	public List<SearchResult> getSearchResults(){
		SearchBean sb = createSearchBeanForSearch();
		if(sb==null || sb.getSearchTerm()==null || sb.getSearchTerm().length() < MINLENGTH_SEARCHTERM) return null; 
		List<SearchResult> results = new DBSearch().selectScriptsForSearchTerm(sb.getSearchTerm());
		return results;
	}

	/**
	 * We have to load the json file for the given locale and scan it for the term.
	 * @param searchTerm
	 * @param userId
	 * @return
	 */
	//private Map<Long, List<SearchBean>> doSearch(SearchBean ){
	//	if(StringUtils.isEmpty(searchTerm) || searchTerm.length()<MINLENGTH_SEARCHTERM) return null; 
		//List<ListItem> items = new DBList().selectListItemBySearchTerm(searchTerm, loc);
	//	return getSearchBeansForItems(items, searchTerm, userId);


		/*String jsonFile = JsonCreator.getMeshJsonFileNameByLoc(loc);
		List<Long> matchingMeshIds = new ArrayList<Long>();
		String line;
		try{
			BufferedReader br = new BufferedReader(new FileReader(jsonFile));
			 while ((line = br.readLine()) != null) {
				 if(StringUtils.containsIgnoreCase(line, searchTerm)){
					 long mechId = getMeshIdFromLine(line)
					 matchingMeshIds.add(getMeshIdFromLine(line));
				 }
			 }
			 br.close();
		}
		catch(Exception e){}*/
		//if(items==null || items.isEmpty()) return null; //nothing found that matches the searchterm..
		//we might return if we have too many items found here....
		//if(matchingMeshIds==null || matchingMeshIds.isEmpty()) return null; //nothing found that matches the searchterm..
		
		//Map<Long, List<SearchBean>> searchHits = new HashMap<Long, List<SearchBean>>();

		//return searchHits;
	//}
	
	/*private Map<Long, List<SearchBean>> getSearchBeansForItems(List<ListItem> items, String searchTerm, long userId){
		//TODO get from a view??? 
		if(items==null || items.isEmpty()) return null;
		List<Long> itemIds = new ArrayList<Long>();
		for(int i=0; i<items.size(); i++){
			itemIds.add(items.get(i).getItem_id());
		}
		DBSearch dbs = new DBSearch();
		return dbs.selectSearchBeansForItemIds(itemIds, searchTerm, userId);		
	}*/
	
	private SearchBean createSearchBeanForSearch(){
		//assuming that search is triggered  from a VP system: 
		String sharedSecret = AjaxController.getInstance().getRequestParamByKey(AjaxController.REQPARAM_SECRET);
		
		if(!AjaxController.getInstance().isValidSharedSecret(sharedSecret)) return null; //invalid shared secret -> todo error message
		SearchBean sb = new SearchBean();
		sb.setUserId(AjaxController.getInstance().getLongRequestParamByKey(AjaxController.REQPARAM_USER));
		sb.setSearchTerm(AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_SEARCHTERM));
		sb.setLoc(new Locale(AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_LOC)));
		return sb;
	}
	
}
