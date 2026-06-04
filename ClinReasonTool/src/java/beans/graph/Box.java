package beans.graph;

import java.util.*;

import javax.faces.context.FacesContext;

import application.AppBean;
import beans.relation.Relation;
import controller.JsonCreator;
import controller.LocaleController;
import properties.IntlConfiguration;
import util.AppBeanPropertyHelper;

 /** Models a box displayed in the user interface (authoring and player). 
 * Each box is defined by its position (1-4), its category ("boxType") that defines in which table it is saved, and the heading (see intlprops)
 * list problems (type 1): subtypes 1 relevant fdgs, 14: midwife fdgs
 * list diagnoses (type 2): subtypes: 2 (ddx default), 7 nursing ddx, 11 midwife hypos, 15 drug-rel probs
 * list tests (type 3)
 *   
 *
 */
public class Box {
	
	public static final int BOX_WITH_LIST = 1; //default;
	public static final int BOX_WITHOUT_LIST = 2;
	public static final int BOXTYPE_FDG = Relation.TYPE_PROBLEM; //1; // plain box for all kinds of finding headers
	public static final int BOXTYPE_DDX = Relation.TYPE_DDX; //2 //comes with MnM, working, final diagnosis, and rule out/in
	public static final int BOXTYPE_TST = Relation.TYPE_TEST; //3
	public static final int BOXTYPE_MNG = Relation.TYPE_MNG; //4
	public static final int BOXTYPE_PAT = Relation.TYPE_PATHO; //6
	public static final int BOXTYPE_INF = Relation.TYPE_INFO; //10
	public static final int BOXTYPE_AIM = Relation.TYPE_AIM; //8
	public static final int BOXTYPE_REC = Relation.TYPE_REC; //18
	
	public static final int BOX1_3_X = 15;
	public static final int BOX2_4_X = 5;
	
	private static final int BOXMODE_HIDDEN = 0;
	private static final int BOXMODE_ACTIVE = 1;
	private static final int BOXMODE_PASSIVE = 2;
	
	
	private int titleNum; //points to the type of box in the international config file.  
	private int listType = BOX_WITH_LIST; //currently only with/without list, potentially extendible
	private int boxType; //default (findings, tests etc. or diagnoses
	
	private int boxMode = BOXMODE_ACTIVE;  //0=not display, 1=active, 2=passive
	private long patIllScriptId; // we store the id of the expertScrip here (not sure why)
	private long id;
	private int idx; //1-based, determines the position of the box
	
	public Box() {}
	
	public Box(long patIllScriptId, int boxType, int titleNum, int idx, int mode) {
		this.patIllScriptId = patIllScriptId;
		checkBoxTypeANdSubType (boxType);
		this.titleNum = titleNum;
		this.idx = idx;
		this.boxMode = mode;
	}
		
	/**
	 * Backward compatibility! It might happen that the type is a subtype ( =titlenum) of the new
	 * approach. SO we have to check that and return the real type.
	 * @param boxType
	 */
	private void checkBoxTypeANdSubType(int boxType) {
		switch(boxType) {
		//main types:
			case Relation.TYPE_PROBLEM: 
				this.boxType = Relation.TYPE_PROBLEM; //1
				break;
			case Relation.TYPE_DDX: 
				this.boxType = Relation.TYPE_DDX; //2
				break;
			case Relation.TYPE_TEST: 
				this.boxType = Relation.TYPE_TEST; //3
				break;
			case Relation.TYPE_MNG: 
				this.boxType = Relation.TYPE_MNG; //4
				break;
			case Relation.TYPE_AIM: 
				this.boxType = Relation.TYPE_AIM; //8
				break;
			case Relation.TYPE_INFO: 
				this.boxType = Relation.TYPE_INFO; //10
				break;
			case Relation.TYPE_PATHO: 
				this.boxType = Relation.TYPE_PATHO; //6
				break;
			case Relation.TYPE_REC:
				this.boxType = Relation.TYPE_REC; //18
				break;
			
			//subtypes: 
			case Relation.SUBTYPE_MFDG: 
				this.boxType = Relation.TYPE_PROBLEM; //14
				break;
			case Relation.SUBTYPE_MHYP: 
				this.boxType = Relation.TYPE_DDX; //11
				break;
			case Relation.SUBTYPE_MMNG: 
				this.boxType = Relation.TYPE_MNG; //13
				break;
			case Relation.SUBTYPE_MED: 
				this.boxType = Relation.TYPE_TEST; //20
				break;
			case Relation.SUBTYPE_NDDX: 
				this.boxType = Relation.TYPE_DDX; //7
				break;
			case Relation.SUBTYPE_NMNG:	
				this.boxType = Relation.TYPE_MNG; //9
				break;
			case Relation.SUBTYPE_DRP : 
				this.boxType = Relation.TYPE_DDX; //15
				break;
			case Relation.SUBTYPE_ASS: 
				this.boxType = Relation.TYPE_PROBLEM; //19
				break;
			case Relation.SUBTYPE_INT:  
				this.boxType = Relation.TYPE_TEST; //17
				break;
			case Relation.SUBTYPE_MREC: 
				this.boxType = Relation.TYPE_REC; //12
				break;
			case Relation.SUBTYPE_OUTC: 
				this.boxType = Relation.TYPE_AIM; //16			
		}
		
	}
	public int getTitleNum() {return titleNum;}
	public int getSubType() {return titleNum;} 
	public void setTitleNum(int titleNum) {this.titleNum = titleNum;}	
	public int getListType() {return listType;}
	public void setListType(int listType) {this.listType = listType;}
	public int getBoxMode() {return boxMode;}
	public void setBoxMode(int boxMode) {this.boxMode = boxMode;}	
	public int getBoxType() {return boxType;}
	public void setBoxType(int boxType) {this.boxType = boxType;}	
	public long getPatIllScriptId() {return patIllScriptId;}
	public void setPatIllScriptId(long patIllScriptId) {this.patIllScriptId = patIllScriptId;}	
	public long getId() {return id;}
	public void setId(long id) {this.id = id;}
	public int getIdx() {return idx;}
	public void setIdx(int idx) {this.idx = idx;}

	/**
	 * @param loc
	 * @return title of the box to be displayed
	 */
	public String getTitle() {
		Locale loc = LocaleController.getLocale();
		return IntlConfiguration.getValue("boxtitle."+boxType+"."+titleNum, loc);
	}
	
	public String getTooltip() {
		Locale loc = LocaleController.getLocale();
		return IntlConfiguration.getValue("boxsearch."+boxType+"."+titleNum, loc);
	}
	
	/**
	 * needed for determining which template to use for item display 
	 * @return
	 */
	public int getBoxCategory() {
		if (boxType==BOXTYPE_DDX) return BOXTYPE_DDX;
		return BOXTYPE_FDG;
	}
	
	public String getListUrl(Locale loc, long groupId) {
		String result = "";
		String groupIdHackStr = AppBean.getProperty("groupid.extralist", "-1").trim();
		long groupIdHack = Long.parseLong(groupIdHackStr); 
		try{
			groupIdHack = Long.parseLong(groupIdHackStr);
		}
		catch(Exception e) {}
		if(this.listType==BOX_WITH_LIST) {
			result = JsonCreator.getDisplayListName("standard", "standard", loc.getLanguage());
			if(groupId==groupIdHack) //if(groupId==1547) //ugly ChapterSea hack
				result = JsonCreator.getDisplayListName("nursing_cs", "nursing_cs", "en");
		}
		
			//result = AppBean.getProperty("lists.standard_","");
			//result = AppBean.getProperty("lists.standard" + (loc.getLanguage()!=null&&loc.getLanguage().length()>0 ? "." + loc.getLanguage() : ""),result);
		
		//List<String> getStringList = AppBeanPropertyHelper.getStringList("lists.languages.", type, null);
		return result;
	}
	
}
