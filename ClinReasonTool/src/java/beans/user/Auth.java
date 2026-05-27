package beans.user;

import java.io.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import beans.AdminFacesContext;
import beans.CRTFacesContext;
import controller.AjaxController;
import controller.UserController;
import database.DBUser;
import util.CRTLogger;
import util.Encoder;
import util.StringUtilities;

/**
 * Authentication of admins or editors; the need to login in order to access the script editor or reports
 * (corresponds with login.xhtml)
 * @author ingahege
 * @deprecated
 */
@ManagedBean
@ViewScoped
public class Auth implements Serializable{
    private String username;
    private String password;
    private String originalURL;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        //String tmp = ((HttpServletRequest) externalContext.getRequest()).getRequestDispatcher();
        originalURL = (String) externalContext.getRequestMap().get(RequestDispatcher.FORWARD_REQUEST_URI);

        if (originalURL == null) {
            originalURL = externalContext.getRequestContextPath() + "/src/html/edit/exp_scripts.xhtml";
        } else {
            String originalQuery = (String) externalContext.getRequestMap().get(RequestDispatcher.FORWARD_QUERY_STRING);

            if (originalQuery != null) {
                originalURL += "?" + originalQuery;
            }
        }
    }

    //private UserService userService;

    /**
     * login of admin users for script editing or reports
     * @throws IOException
     * 
     */
    public void login() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        
        try {
        	User user = new DBUser().selectUserByLogin(username);
        	if(user!=null && user.isEditor() && compareEncrPwd(password, user.getPassword())){
        		AdminFacesContext cnxt =  (AdminFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(AdminFacesContext.CRT_FC_KEY);
        		if(cnxt!=null) cnxt.setUser(user);
        		externalContext.getSessionMap().put("user", user);
        		externalContext.redirect(originalURL);
        	}
        	else handleError(user);
        } 
        catch (Exception e) {
        	CRTLogger.out("Auth.login(): " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
            handleError(null);
        }
    }
    
    /**
     * Automatic login of expert map editors if they connect thru the parent VP system, 
     * CAVE: currently we do not check for password here...
     * @throws IOException
     */
    public void loginAdminsViaAPI() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
       // HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        String extUserId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt("userid_ext");
        String groupId = AjaxController.getInstance().getRequestParamByKeyNoDecrypt("groupid");
        //int systemId = 2; //AjaxController.getInstance().getIntRequestParamByKey("system_id", 2);
        if(extUserId==null || extUserId.trim().isEmpty()) return;
        try {
        	User user = new DBUser().selectUserByExternalId(extUserId/*, systemId*/);
        	if(user==null){ //create user:
        		user = new UserController().createAndSaveExpertUser(/*systemId,*/ extUserId, groupId);
        	}
        	user.updateGroupId(groupId);
        	AdminFacesContext cnxt =  (AdminFacesContext) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(AdminFacesContext.CRT_FC_KEY);
        	if(cnxt!=null) cnxt.setUser(user);
        	externalContext.getSessionMap().put("user", user);

        } 
        catch (Exception e) {
        	CRTLogger.out("Auth.loginAdminsViaAPI(): " + StringUtilities.stackTraceToString(e), CRTLogger.LEVEL_ERROR);
            handleError(null);
        }
    }
    
    /** 
 	 * Handle unknown username/password in request.login().
     */
    private void handleError(User user){
    	FacesContext context = FacesContext.getCurrentInstance();
    	if(user!=null && !compareEncrPwd(password, user.getPassword()))
    		context.addMessage(null, new FacesMessage("Unknown login/Pwd"));
    	else if (user!=null && !user.isEditor())
    		context.addMessage(null, new FacesMessage("No permission to access admin area"));
    	else 
    		context.addMessage(null, new FacesMessage("Unknow error...Shit happens..."));

    }
    
    /**
     * We compare the entered password (non-encrypted) with the encrypted password stored in the database.
     * @param enteredPwd
     * @param encPwd
     * @return
     */
    private boolean compareEncrPwd(String enteredPwd, String encPwd){
    	if(enteredPwd==null || enteredPwd.trim().equals("")) return false;
    	//if(encPwd==null || encPwd.trim().equals("")) return false;
    	try{
	    	String encEnteredPwd = Encoder.getInstance().encodeQueryParam(enteredPwd);
	    	if(encEnteredPwd.equals(encPwd)) return true;
	    	return false;
    	}
    	catch(Exception e){
    		return false;
    	}  	
    }

    public void logout() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.invalidateSession();
        externalContext.redirect(externalContext.getRequestContextPath() + "/login.xhtml");
    }

	public String getUsername() {return username;}
	public void setUsername(String username) {this.username = username;}
	public String getPassword() {return password;}
	public void setPassword(String password) {this.password = password;}
}
