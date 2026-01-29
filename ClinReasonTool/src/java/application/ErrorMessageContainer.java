package application;

import java.util.*;


import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "errmsg", eager = true)
@RequestScoped
public class ErrorMessageContainer {

	public void addErrorMessage(String summary, String details, Severity sev){
		addErrorMessage("", summary, details, sev);
	}
	
	public void addErrorMessage(String formId, String summary, String details, Severity sev){
		FacesContext facesContext = FacesContext.getCurrentInstance(); 
		facesContext.addMessage("",new FacesMessage(sev, summary,details));
	}
	
	public void toXml(StringBuffer xmlResponse){
		//FacesContext cntxt = FacesContext.getCurrentInstance();
		Iterator it = FacesContext.getCurrentInstance().getClientIdsWithMessages(); //getMessageList();
		//Iterator it = msgs.keySet().iterator();
		if(it.hasNext()){ //per default we only display last message
			String formId = (String) it.next();
			Iterator it2 = FacesContext.getCurrentInstance().getMessages(formId);
			if(it2.hasNext()){
				FacesMessage fmsg = (FacesMessage) it2.next();
				if(fmsg.getSummary()!=null && !fmsg.getSummary().trim().equals(""))
					xmlResponse.append("<msg>"+fmsg.getSummary()+"</msg>");
					xmlResponse.append("<formId>"+formId+"</formId>");
					xmlResponse.append("<ok>0</ok>"); //indicates that an error has occured
			}
		}
		else xmlResponse.append("<ok>1</ok>"); //no error has occured
	}
	
	public String getErrorMessage(){
		if(FacesContext.getCurrentInstance().getMessages()!=null && FacesContext.getCurrentInstance().getMessages().hasNext()) 
			return FacesContext.getCurrentInstance().getMessages().next().getSummary();
		return "";
	}
}
