package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

import application.AppBean;
import beans.list.ListInterface;
import beans.list.ListItem;
import beans.list.Synonym;
import database.DBList;
import properties.IntlConfiguration;
import util.AppBeanPropertyHelper;
import util.CRTLogger;
import util.StringUtilities;

/**
 * Creates Json files based on lists
 * Format: {"label": "Calcimycin", "value": "3"},
 * Nursing items are either marked with "1" in the nursing column or added as additional item with "U" as type.
 * (U1.xxx are nursing diagnoses)
 * 
 * Sep 2022: refactoring by gulpi (=Martin Adler):
 * <li>made it more generic and configurable by properties to make it more flexible for extension of lists</li>
 * 
 * @author ingahege
 */
public class JsonCreator {
	
	// following constants are not longer used in db querying, however we keep them here
	public static final String TYPE_PROBLEM = "C";
	public static final String TYPE_TEST = "E";
	public static final String TYPE_EPI = "F";
	public static final String TYPE_DRUGS = "D";
	public static final String TYPE_PERSONS = "M";
	public static final String TYPE_MANUALLY_ADDED = "MA";
	public static final String TYPE_HEALTHCARE = "N";
	public static final String TYPE_CONTEXT = "I";
	public static final String TYPE_B = "B";
	public static final String TYPE_G = "G";
	public static final String TYPE_ANATOMY = "A";
	public static final String TYPE_NURSING = "U";
	
	//private boolean createOneList = true; //if false, we create multiple lists for problems, ddx, etc.
	private static ServletContext context;	
	
	public synchronized void initJsonExport(ServletContext contextIn){
		context = contextIn;
		
		String lang = null; //language can be set with a query parameter, e.g. when calling through list admin page
		try{
			lang = AjaxController.getInstance().getRequestParamByKey("lang");
		}
		catch(Exception e){}
		
		if(lang!=null){
			exportGenericList("standard",new Locale(lang));
			exportGenericList("nursing",new Locale(lang));
			exportGenericList("context",new Locale(lang));
			
			
		}
		else{
			// loop thru all types
			String list_types = AppBean.getProperty("lists.types","standard,context,nursing");
			List<String> list_types_list = net.casus.util.StringUtilities.getStringListFromString(list_types, ",");
			Iterator<String> list_types_list_it = list_types_list.iterator();
			while(list_types_list_it.hasNext()) {
				String loop = list_types_list_it.next();
				//Log.info("JsonCreator","list_type:" + loop);
				
				// loop thru all languages
				String languages = AppBean.getProperty("lists.languages." + loop,"en,de,pl,sv,es,pt,fr,uk");
				List<String> languages_list = net.casus.util.StringUtilities.getStringListFromString(languages, ",");
				Iterator<String> languages_list_it = languages_list.iterator();
				while(languages_list_it.hasNext()) {
					exportGenericList(loop,new Locale(languages_list_it.next()));
				}
			}
		}
		//This is an ugly hack to satisfy ChapterSea nursing with their own list
		exportNursingChapterSeaList(this.context);
	}
		
	public void setContext(ServletContext context){
		JsonCreator.context = context;
	}
	
	/**
	 * We export the list of the given language from the database into a JSON file for use in the user interface 
	 * We also store the list items in the SummaryController for using it for the statement analysis and assessment.
	 * @param loc
	 */
	public File getGenericJsonFileByLoc(String type, String lang ){
		String file = AppBean.getProperty("lists." + type,"jsonp_#{locale}.json");
		List<String> getStringList = AppBeanPropertyHelper.getStringList("lists.languages.", type, null);
		if (getStringList != null && getStringList.contains(lang)) {
			file = net.casus.util.StringUtilities.replace(file, "#{locale}", lang != null ? lang : "en");
		}
		else {
			return null;
		}
		
		if (file != null) {
			if (context != null) {
				return new File(context.getRealPath(AppBean.getProperty("lists.base","src/html/") + file));
			}
			else {
				String name = file;
				int idx = name.lastIndexOf('/');
				name = name.substring(idx+1);
				return new File(name);
			}
		}
		
		return null;
	}
	
	public List exportGenericList(String type, Locale loc){
		List<ListItem> items = new DBList().selectListItemsByTypesAndLang(loc, AppBeanPropertyHelper.getArray("lists.dbtypes.", type, null), AppBeanPropertyHelper.getInt("lists.professionType.", type, -1), AppBeanPropertyHelper.getInt("lists.professionVariant.", type, -1));
		return exportGenericList(type, loc, items);
	}
	/**
	 * We export the list of the given language from the database into a JSON file for use in the user interface 
	 * We also store the list items in the SummaryController for using it for the statement analysis and assessment.
	 * @param loc
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List exportGenericList(String type, Locale loc, List items){
		// dababase call configurable by lists.dbtypes.<type>=<STring delimioted by , (comma) with categories; lists.professionType.<type>=0 | 1; listvaliant for future extension
		//List<ListItem> items = new DBList().selectListItemsByTypesAndLang(loc, AppBeanPropertyHelper.getArray("lists.dbtypes.", type, null), AppBeanPropertyHelper.getInt("lists.professionType.", type, -1), AppBeanPropertyHelper.getInt("lists.professionVariant.", type, -1));
		if(items==null || items.isEmpty()) {
			CRTLogger.out("JsonCreator.exportGenericList(\"" + type + "\"," + loc + ") => items null | empty: " + items, CRTLogger.LEVEL_ERROR);
			return null; //then something went really wrong!
		}
		
		//we collect all items here, to sort it alphabetically
		List itemsAndSyns = new ArrayList();
		try{
			int lines = 0; int json_lines = 0;

			// preprocess for standard list only (until now -> configure by lists.preprocess.<type>=true | false
			if (AppBeanPropertyHelper.getBoolean("lists.preprocess.", type, false)) {
				lines = exportGenericList_preprocess(loc, items, itemsAndSyns, lines);
			}
			else {
				itemsAndSyns = items;
			}
			//SummaryStatementController.addListItems(itemsAndSyns, loc.getLanguage());
			
			// should NOT happen!!!!
			if (itemsAndSyns != null) {
				Collections.sort(itemsAndSyns);
				json_lines = itemsAndSyns.size();
			}
			
			// generate json entries
			StringBuffer sb = new StringBuffer("[");
			for(int i=0; i<itemsAndSyns.size();i++){
				ListInterface li = (ListInterface) itemsAndSyns.get(i);		
				// returns destroy the JSONP file, so clean that up, to be more fault tolerant, even though this should also be checked AND cleaned on insert into database
				String tmpName = li.getName();
				// huh should not happen!!
				if (tmpName == null) tmpName = "";
				tmpName = cleanJsonString(tmpName);
				sb.append("{\"label\": \"" + tmpName + "\", \"value\": \"" + li.getIdForJsonList() + "\"},\n");
			}
			
			// own entries global and configurable per type lists.allowOwnEntries.<type>=true | false
			exportGenericList_ownEntries(type, loc, sb);
			
			// clean up -> remove last comma!!
			sb.replace(sb.length()-2, sb.length(), "]");
			
			// write to file finally
			exportGenericList_write2File(type, loc, json_lines, sb);
		    return itemsAndSyns;
		}
		catch( Exception e){
			CRTLogger.out(StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_PROD);
			return null;
		}
	}

	private String cleanJsonString(String tmpName) {
		tmpName = net.casus.util.StringUtilities.replace(tmpName, "\r\n", " ");
		tmpName = net.casus.util.StringUtilities.replace(tmpName, "\n\r", " ");
		tmpName = net.casus.util.StringUtilities.replace(tmpName, "\r", " ");
		tmpName = net.casus.util.StringUtilities.replace(tmpName, "\n", " ");
		tmpName = net.casus.util.StringUtilities.replace(tmpName, "\"", " ");
		return tmpName;
	}

	private void exportGenericList_write2File(String type, Locale loc, int json_lines, StringBuffer sb)
			throws IOException {
		File f = getGenericJsonFileByLoc(type,loc != null ? loc.getLanguage() : "en");
		if (f != null) {
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.print(sb.toString());
			pw.flush();
			pw.close();
			CRTLogger.out("lines exported: " + json_lines + " to <" + (f!=null?f.getAbsolutePath():"-") + ">", CRTLogger.LEVEL_PROD);
		}
		else {
			CRTLogger.out("lines not exported: type:" + type + "and loc:" + loc + " ar not enabled!", CRTLogger.LEVEL_PROD);
		}
	}

	private void exportGenericList_ownEntries(String type, Locale loc, StringBuffer sb) {
		boolean allowOwnEntries = AppBean.getProperty("AllowOwnEntries", false);
		if (allowOwnEntries) {
			allowOwnEntries = AppBeanPropertyHelper.getBoolean("lists.allowOwnEntries.", type, false);
		}
		if(allowOwnEntries) {
			sb.append(getOwnEntry(loc));
		}
	}

	private int exportGenericList_preprocess(Locale loc, List<ListItem> items, @SuppressWarnings("rawtypes") List itemsAndSyns, int lines) {
		for(int i=0; i<items.size(); i++){
			ListItem item = items.get(i);
			//add items for SummaryStatement Rating
			if(doAddItem(item)){
				lines += addItemAndSynonymaNew(item, itemsAndSyns);
				SummaryStatementController.addListItem(item, loc.getLanguage());
			}
			
			else if(item.getFirstCode().startsWith("A")){
				SummaryStatementController.addListItemsA(item, loc.getLanguage());
			}
			
			else if(item.getFirstCode().startsWith("Z")){ //we add countries...
				SummaryStatementController.addListItem(item, loc.getLanguage());
			}				
		}
		
		return lines;
	}
	
	private String getOwnEntry(Locale loc){
		String ownEntry = IntlConfiguration.getValue("list.ownEntry", loc);
		return "{\"label\": \""+ownEntry+"\", \"value\": \"-99\"},\n";
	}
	
	/**
	 * Make some improvements of the list, we do not add a certain item_level depending on the category etc.
	 * maybe do more here....
	 * @param item
	 * @return
	 */
	private boolean doAddItem(ListItem item){
		if(item.isIgnored()) return false;
		//D:
		if(item.getItemType().equals("D") && item.getLevel()>=10) return false;
		if(item.getItemType().equals("A")) return false;
		if(item.getFirstCode()==null) return true;
		if(item.getFirstCode().startsWith("D20.0") || item.getFirstCode().startsWith("D20.1")) return false;
		if(item.getFirstCode().startsWith("D20.3") || item.getFirstCode().startsWith("D20.4")) return false;
		if(item.getFirstCode().startsWith("D20.7") || item.getFirstCode().startsWith("D20.8")) return false;
		if(item.getFirstCode().startsWith("D20.9")) return false;
		if(item.getFirstCode().startsWith("D26.2")) return false;
		if(item.getFirstCode().startsWith("C22")) return false; //Animal diseases		
		if(item.getName().startsWith("1") || item.getName().startsWith("2") || item.getName().startsWith("3")) return false;
		if(item.getName().startsWith("4-") || item.getName().startsWith("4,")) return false;
		if(item.getName().startsWith("5") || item.getName().startsWith("6") || item.getName().startsWith("7")) return false;
		if(item.getName().startsWith("8") || item.getName().startsWith("9")) return false;
		if(item.getName().contains("[")) return false;
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	private int addItemAndSynonymaNew(ListItem item, @SuppressWarnings("rawtypes") List itemsAndSyns/*PrintWriter pw, boolean lastEntry*/){
		List<ListInterface> toAddItems = new ArrayList<ListInterface>();
		if(item.getSynonyma()==null || item.getSynonyma().isEmpty()){ //no synonyma, only one main item:
			toAddItems.add(item);
		}
		
		//now we compare the synonyma
		else{
			Iterator<Synonym> it = item.getSynonyma().iterator();
			toAddItems.add(item);
			while(it.hasNext()){	
				Synonym syn = it.next();
				if(!syn.isIgnored()){
					boolean isSimilar = false;
					for(int i=0;i<toAddItems.size(); i++){						
						isSimilar = StringUtilities.similarStrings(toAddItems.get(i).getName(), syn.getName(), item.getLanguage(), false);
						if(isSimilar){
							ListInterface bestItem = bestTerm(toAddItems.get(i),syn);
							if(!bestItem.equals(toAddItems.get(i))){
								toAddItems.remove(i);
								toAddItems.add(i, bestItem);
							}
							break;
						}
					}
					if(!isSimilar && !toAddItems.contains(syn))
						toAddItems.add(syn);
				}
			}
								
		}
		itemsAndSyns.addAll(toAddItems);
		return toAddItems.size();
	}										
	
	private ListInterface bestTerm(ListInterface currBestTerm, ListInterface newTerm){
		if(!currBestTerm.getName().contains(" ")) return currBestTerm; //current term is one word
		if(!newTerm.getName().contains(" ")) return newTerm; //new term is one word -> better term
		if(currBestTerm.getName().contains(",") && !newTerm.getName().contains(",")) return newTerm;
		return currBestTerm;
	}
	
	/**
	 * called from context bean:
	 * configurable in properties:
	 * this shouldbe used for defining lists in templates:
	 * 
	 * <li>#{crtContext.getMyListUrl("<type>",crtContext.patillscript.locale)}</li>
	 * <li>#{adminContext.getMyListUrl("standard",adminContext.patillscript.locale)}</li>
	 * 
	 * <type> := (at this moment) standard | context | nursing (as defined for creation in lists.types property)
	 * 
	 * lists.<mode>.<type> and overridable by:
	 * lists.<mode>.<type>.<lang>
	 * 
	 * 
	 * @param mode
	 * @param type
	 * @param lang
	 * @return
	 */
	static public String getDisplayListName(String mode, String type, String lang) {
		String result = AppBean.getProperty("lists." + mode /*+ "." + type*/,"");
		result = AppBean.getProperty("lists." + mode /*+ "." + type */+ (lang!=null&&lang.length()>0 ? "." + lang : ""),result);
		
		List<String> getStringList = AppBeanPropertyHelper.getStringList("lists.languages.", type, null);
		if (getStringList != null && getStringList.contains(lang)) {
			result = net.casus.util.StringUtilities.replace(result, "#{locale}", lang != null ? lang : "en");
		}
		else {
			result = "";
		}
		
		return result;
	}
	
	/**
	 * hack for ChapterSea nursing implementation
	 */
	public List exportNursingChapterSeaList(ServletContext contextIn) {
		if(contextIn!=null) context = contextIn;
		try {
			List l = new DBList().loadChapterSeaNursingList();
			return exportGenericList("nursing_cs", new Locale("en"), l);
		}
		catch(Exception e) {
			CRTLogger.out("Error during ChapterSea Nursing list export", CRTLogger.LEVEL_PROD);
			return null;
		}	
	}
}
