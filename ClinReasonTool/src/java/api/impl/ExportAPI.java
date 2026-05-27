package api.impl;

import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import actions.scoringActions.ScoringSummStAction;
import api.AbstractAPIImpl;
import beans.relation.summary.SummaryStatement;
import beans.scoring.ScoreBean;
import beans.scripts.PatientIllnessScript;
import beans.user.User;
import controller.AjaxController;
import controller.IllnessScriptController;
import controller.SummaryStatementController;
import controller.UserController;
import database.DBClinReason;
import net.casus.util.CasusConfiguration;
import net.casus.util.StringUtilities;
import util.CRTLogger;

/**
 * simple JSON Webservice for simple API JSON framework
 * 
 * <base>/crt/src/html/api/api.xhtml?impl=peerSync
 * should either start a new thread, or return basic running thread data!
 *
 * @author Gulpi (=Martin Adler)
 */
public class ExportAPI extends AbstractAPIImpl {
	
	public ExportAPI() {
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public synchronized String handle() {
		String result = "";
		Map resultObj = new TreeMap();
		IllnessScriptController isc = new IllnessScriptController();
		try {
			//String extUserId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt(AjaxController.REQPARAM_EXTUID);
			//int systemId = 2; //AjaxController.getInstance().getIntRequestParamByKey(AjaxController.REQPARAM_SYSTEM, 2);
			//User user = user =  new UserController().getUser(/*systemId,*/ extUserId);
			long vpId = StringUtilities.getLongFromString((String) ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("vp_id"), -1);
			if (vpId > 0) {
				PatientIllnessScript expScript = new DBClinReason().selectExpertPatIllScriptByVPId( vpId+"_2"); 
				if (expScript != null && expScript.getSummSt() != null && expScript.getSummSt().getText() != null) {
					resultObj.put("status", "ok");
					resultObj.put("expert.text", expScript.getSummSt().getText());
					resultObj.put("expert.lang", expScript.getSummSt().getLang());
				}
				else {
					resultObj.put("status", "error");
					resultObj.put("errorMsg", "expScript script | expert summary staement invalid");
				}
			}
			

			ObjectMapper mapper = new ObjectMapper();
			try {
				if (CasusConfiguration.getGlobalBooleanValue("SummaryStatementAPI.prettyJSON", true)) {
					result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultObj);
				}
				else {
					result = mapper.writeValueAsString(resultObj);
				}
			} catch (JsonProcessingException e) {
				result = e.getMessage();
			}
		} catch (Exception e1) {
			result = e1.getMessage();
		}
		
		return result;
	}
	
	public SummaryStatement handleByPatientIllnessScript(PatientIllnessScript userPatientIllnesScript) {
		SummaryStatement st = null;
		
		PatientIllnessScript expScript = getAppBean().addExpertPatIllnessScriptForVpId(userPatientIllnesScript.getVpId());
		expScript.getSummStStage();
		
		ScoreBean scoreBean = new ScoreBean(userPatientIllnesScript, userPatientIllnesScript.getSummStId(), ScoreBean.TYPE_SUMMST, userPatientIllnesScript.getCurrentStage());
		if(expScript!=null && expScript.getSummSt()!=null){
			ScoringSummStAction action = new ScoringSummStAction();
			st = new SummaryStatementController().initSummStRating(expScript, userPatientIllnesScript, action);	
			action.doScoring(st, expScript.getSummSt());
		}
		else {
    		CRTLogger.out("SummaryStatementAPI.handleByPatientIllnessScript: !!!! No expert SummSt for userPatientIllnesScript.id: "  + userPatientIllnesScript.getId() + "; userPatientIllnesScript.vp_id: " + userPatientIllnesScript.getVpId() , CRTLogger.LEVEL_PROD);
		}
		
		return st;
	}
}
