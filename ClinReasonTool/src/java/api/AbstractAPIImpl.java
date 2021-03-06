package api;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import actions.scoringActions.ScoringSummStAction;
import api.ApiInterface;
import application.AppBean;
import beans.list.ListItem;
import beans.relation.summary.SummaryStElem;
import beans.relation.summary.SummaryStatement;
import beans.relation.summary.SummaryStatementSQ;
import beans.scoring.ScoreBean;
import beans.scripts.PatientIllnessScript;
import controller.JsonCreator;
import controller.SummaryStatementController;
import database.DBClinReason;
import database.DBList;
import net.casus.util.CasusConfiguration;
import net.casus.util.StringUtilities;
import net.casus.util.Utility;
import net.casus.util.io.IOUtilities;
import net.casus.util.nlp.spacy.SpacyStructureStats;
import util.CRTLogger;

/**
 * simple JSON Webservice for simple API JSON framework
 * 
 * <base>/crt/src/html/api/api.xhtml?impl=spacy
 * should either start a new thread, or return basic running thread data!
 *
 * @author Gulpi (=Martin Adler)
 */
public abstract class AbstractAPIImpl implements ApiInterface {
	AppBean appBean = null;
	
	/**
	 * needs to be initialized, to be available alos from Threads, which do NOT have a Faces context!!!
	 * @return
	 */
	public AppBean getAppBean(){
		if (appBean == null) {
			ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			appBean = (AppBean) context.getAttribute(AppBean.APP_KEY);
		}
		
		return appBean;
	}
	
	public void init() {
		try {
			// make sure appBean is there / in internal thread we don't have contexts!!
			this.getAppBean();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
