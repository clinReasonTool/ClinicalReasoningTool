package beans.list;

import java.io.Serializable;
import java.util.*;
import util.*;

/**
 * hack for ChapterSea nursing implementation -> this corresponds to the ListItem class.
 * @author ingahege
 *
 */
public class ChapterSeaNursingItem implements Serializable, ListInterface, Comparable{
	private String name; 
	private long item_id = -1;
	private String source; //NADA
	private Locale language; //currently only en.
	
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public long getItem_id() {return item_id;}
	public void setItem_id(long item_id) {this.item_id = item_id;}
	public String getSource() {return source;}
	public void setSource(String source) {this.source = source;}
	public Locale getLanguage() {return language;}
	public void setLanguage(Locale language) {this.language = language;}
	
	public ChapterSeaNursingItem(){}
	
	public ChapterSeaNursingItem(String lang, String source, String name){
		this.language = new Locale(lang);
		this.source = source;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return this.name + ", Id: " + this.item_id;
	}
	
	public boolean equals(Object o){
		if(o instanceof ChapterSeaNursingItem && ((ChapterSeaNursingItem)o).getItem_id()==item_id) return true;
		return false;
	}
	
	public String getIdForJsonList(){
		return String.valueOf(item_id);
	}

	public int compareTo(Object o) {
		if(o instanceof ListInterface){
			ListInterface li = (ListInterface) o;
			return name.compareToIgnoreCase(li.getName());
		}
		return 0;
	}
	@Override
	public String getNameLower() {
		return this.name.toLowerCase();
	}
	@Override
	public boolean isSynonym() {
		return false;
	}
	@Override
	public String getItemType() {
		return "";
	}
	@Override
	public long getListItemId() {
		return item_id;
	}
}
