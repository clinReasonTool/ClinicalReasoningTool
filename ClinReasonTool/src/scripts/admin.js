/**
 * make changes to an expert script available to users by removing the current script version from cache.
 */
function removeExpScriptFromCache(vpId){
	sendAjaxAdmin(vpId, doNothing, "removeExpScriptFromCache", vpId);
}

function initSearch(){
	sendAjaxAdmin(vpId, doNothing, "removeExpScriptFromCache", vpId);
}
function doNothing(){}


/**
 * admin selects a vp id so we trigger an ajax call, to get back all learner scripts for the 
 * selected vpId. 
 */
function selectScriptForVPId(){
	var vpId = $("#report_vpid").val();
	window.location.replace(window.location.pathname + "?r_vp_id="+vpId);
	//sendAjaxReports(vpId, selectScriptForVPIdCallback, "getLearnerScripts", vpId);
}

function selectScriptForVPIdCallback(response){
	//alert(response);
}

function displayScript(){
	var vpId = $("#report_vpid").val();
	var scriptId =  $("#report_scripId").val();
	//$("#report_iframe").attr("src", "../view/exp_boxes_view.xhtml?script_id="+scriptId);
	sendAjaxReports(null, displayScriptCallback, "getLearnerScript", scriptId);

}

function displayScriptCallback(){
	var d = new Date().getMilliseconds(); 
	$("#report_iframe").attr("src", "../view/exp_boxes_view.xhtml?r_scriptid="+$("#report_scripId").val());

}