var color2 = "#c2c2c2";

var myDropOptions = {
        tolerance: "touch",
        hoverClass: "dropHover",
        activeClass: "dragActive"
    };


/*endpoint when connection has been made */
var endpointCR= {
		endpoint: ["Rectangle", {cssClass:"endpointArrowR", maxConnections: 10}],
	    isSource: true,
	    connector: ["Bezier", { curviness: 15 } ],
	    maxConnections: 10,
	    isTarget: true,
	    deleteEndpointsOnDetach:false,
	    connectorOverlays: [ [ "Label", { label:"<i class=\"fa overlay\"></i>" } ]]	
}
var endpointCL= {
	    endpoint: ["Rectangle", {cssClass:"endpointArrowL", maxConnections: 10}],
	    isSource: true,
	    connector: ["Bezier", { curviness: 15 } ],
	    maxConnections: 10,
	    isTarget: true,
	    deleteEndpointsOnDetach:false,
	    connectorOverlays: [ [ "Label", { label:"<i class=\"fa overlay\"></i>" } ]]	
}

var expendpoint = {
	    endpoint: ["Dot", { radius: 5 }],
	    paintStyle: { fillStyle: color2, fill:color2 },//fill:color2 needed for 2.2.0
	    isSource: true,
	    cssClass: 'exp-endpoint',
	    connector: ["Bezier", { curviness: 15 } ],
	    maxConnections: 10,
	    isTarget: true,
	    deleteEndpointsOnDetach:false
	    //detachable:false
}

   
function createConnection(cnxId, sourceId, targetId, learner, exp, expWeight, learnerWeight, startEpIdx, targetEpIdx, targetX, targetY, learnerStage){

	if(isView && displayMode==1){ //view in reports
		createViewConnection(cnxId, sourceId, targetId, learner, exp, expWeight, learnerWeight, startEpIdx, targetEpIdx, targetX, targetY, learnerStage);
		return;
	}

	if(/*!expFeedback &&*/ learner=="0" && exp=="1"){
		createExpConnection(cnxId, sourceId, targetId, expWeight, learnerWeight, startEpIdx, targetEpIdx,  targetX, targetY);
		return;
	}
	if(instance.getEndpoints==null || instance.getEndpoints(sourceId)==undefined || instance.getEndpoints(targetId)==undefined)
		return;
	var epSource = getEndpointForCnx(sourceId, startEpIdx);
	var epTarget =  getEndpointForCnx(targetId, targetEpIdx); 
	var target  = $("#"+targetId)[0];
	var color = getWeightToColor(learnerWeight);
	var lw = getWeightToLineWidth(learnerWeight);
	var cnx;
	
	if(targetX>=0 && targetY>=0){ //with have a dynamic position
		cnx = instance.connect({
			source:epSource, 
			target:target,
			//endpoint:"Dot",
		    maxConnections: 10,
			//anchor:[ "Perimeter", { shape: "Rectangle" }, { position:[10, 0]}],
			anchor: ["Perimeter", { shape:"Rectangle" }],
			    	//position:[targetX, targetY]}],
					
			id:"exp_"+cnxId,
			//detachable:false,
			paintStyle: { strokeStyle: color, strokeWidth: lw, stroke:color, outlineStroke: "transparent", outlineWidth: 5  } //stroke:color needed for 2.2.0
		});		
	}
	else{
		cnx = instance.connect({
			source:epSource, 
			target:epTarget,
		    maxConnections: 10,
			/*anchor:[ "Assign", { 
			    	position:[targetX, targetY]}],*/
			id:"exp_"+cnxId,
			//detachable:false,
			paintStyle: { strokeStyle: color, strokeWidth: lw, stroke:color, outlineStroke: "transparent", outlineWidth: 5 } //stroke:color needed for 2.2.0
		});
	}

	$(cnx).attr('id', cnxId);	
}

/**
 * creates the connections with labels for the view in the reports if a map is displayed completely.
 * @returns
 */
function createViewConnection(cnxId, sourceId, targetId, learner, exp, expWeight, learnerWeight, startEpIdx, targetEpIdx, targetX, targetY, learnerStage){
	
	if(/*!expFeedback &&*/ learner=="0" && exp=="1"){
		createExpConnection(cnxId, sourceId, targetId, expWeight, learnerWeight, startEpIdx, targetEpIdx,  targetX, targetY);
		return;
	}

	if(instance.getEndpoints==null || instance.getEndpoints(sourceId)==undefined || instance.getEndpoints(targetId)==undefined)
		return;
	var epSource = getEndpointForCnx(sourceId, startEpIdx);
	var epTarget =  getEndpointForCnx(targetId, targetEpIdx); 
	var target  = $("#"+targetId)[0];
	var color = getWeightToColor(learnerWeight);
	var lw = getWeightToLineWidth(learnerWeight);
	var cnx;
	//if(learnerWeight==2 || learnerWeight=="2") return; //implicit connection
	
	if(targetX>=0 && targetY>=0){ //with have a dynamic position
		cnx = instance.connect({
			source:epSource, 
			target:target,
			//endpoint:"Dot",
		    maxConnections: 10,
			//anchor:[ "Perimeter", { shape: "Rectangle" }, { position:[10, 0]}],
			anchor: ["Perimeter", { shape:"Rectangle" }],
			    	//position:[targetX, targetY]}],
					
			id:"exp_"+cnxId,
			//detachable:false,
			paintStyle: { strokeStyle: color, strokeWidth: lw, stroke:color, outlineStroke: "transparent", outlineWidth: 5  }, //stroke:color needed for 2.2.0
			overlays:[["Label", {label:"<span class=\"labeltst\">("+learnerStage+")</span>", location:0.5, id:"label_"+cnxId}]]

		});		
	}
	else{
		cnx = instance.connect({
			source:epSource, 
			target:epTarget,
		    maxConnections: 10,
			id:"exp_"+cnxId,
			paintStyle: { strokeStyle: color, strokeWidth: lw, stroke:color, outlineStroke: "transparent", outlineWidth: 5 }, //stroke:color needed for 2.2.0
			overlays:[["Label", {label:"<span class=\"labeltst\">("+learnerStage+")</span>", location:0.5, id:"label_"+cnxId}]]
		});
	}

	$(cnx).attr('id', cnxId);	
}

function getEndpointForCnx(itemId, epIdx){
	var eps = instance.getEndpoints(itemId);
	if(eps==null) return;
	if(epIdx=="0"){ //default
		return eps[0];
	}
	for(var i=0; i<eps.length;i++){
		if(eps[i].id==epIdx+"_"+itemId) return eps[i];
	}
}

function createExpConnection(cnxId, sourceId, targetId, expWeight, learnerWeight, startEpIdx, targetEpIdx, targetEpX, targetEpY){
	if($("#"+sourceId)[0]==undefined ||$("#"+targetId)[0]==undefined) 
		return;

	createExpEndpointsForItems(sourceId);
	createExpEndpointsForItems(targetId);

	var epSource = getEndpointForCnx(sourceId, startEpIdx);
	var epTarget =  getEndpointForCnx(targetId, targetEpIdx); 
	//var target  = $("#"+targetId)[0];
	var color = getWeightToColor(expWeight);
	var lw = getWeightToLineWidth(expWeight);
	//alert(color);
	var cnx = instance.connect({
		source:epSource, 
		target:epTarget,
		id:"exp_"+cnxId,
		deleteEndpointsOnDetach:false,
		paintStyle: { strokeStyle: color, strokeWidth: lw, stroke:color } //stroke:color2 needed for 2.2.0
	//paintStyle: {strokeWidth: lw}
	});
	var cnxs2 = instance.getConnections();
	if(cnx!=undefined){
		$(cnx).attr('id', "exp_"+cnxId);
		cnx.setPaintStyle({strokeStyle:color, strokeWidth: lw, stroke: color}); //color depending on the weight!!!
		if(!isOverallExpertOn() || !isOverallCnxOn()){
			cnx.addClass("jtk-exp-connector-hide");
		}
		cnx.addClass("jtk-exp-connector");
		cnx.removeClass("jtk-connector");
	}
}

function delConnection(){
	if(isView) return;
	var cnxId = $("#conn_id").html();	
	var cnx = getConnectionById(cnxId);
	instance.deleteConnection(cnx); //new from version 2.4.0, detach no longer supported

	$("#conn_id").html('');
	$("#connContext" ).dialog( "close" );
	sendAjax(cnxId, doNothing, "delConnection", "");
}
/**
 * a connection is added to the canvas, we save the start- and endpoint and label.
 */
function addConnection(conn){
	if(isView) return;
	var epSource = conn.sourceEndpoint.id;//instance.getEndpoints(conn.id);
	var epTarget = conn.targetEndpoint.id;
	var targetEPId =  conn.targetEndpoint.id;
	var targetPosX = conn.targetEndpoint.endpoint.x;
	var targetPosY = conn.targetEndpoint.endpoint.y;
	if(epSource==epTarget) return;
	var epSourceIdx = epSource.charAt(0);
	
	var sourceId = conn.sourceId; 
	var targetId = conn.targetId;
	var target = $("#"+targetId)[0];
	var targetLeft = 0;
	var targetTop = 0;
	if(target.offsetParent!=null){		
		targetLeft =  target.offsetParent.offsetLeft;
		targetTop = target.offsetParent.offsetTop;
	}
	targetPosX = targetPosX - targetLeft +12.5;
	targetPosY = targetPosY - targetTop +7.5;
	//sendAjaxCnx(sourceId, addConnectionCallback, "addConnection", targetId, epSourceIdx, epTargetIdx);
	sendAjaxCnx(sourceId, addConnectionCallback, "addConnection", targetId, epSourceIdx, targetPosX, targetPosY);

}

/**
 * 
 * @param id = sourceId
 * @param name = targetId
 */
function addConnectionCallback(sourceId, cnxId, targetId){
	if(sourceId==null || sourceId<0 || targetId==null || targetId<0) return; //cnx with same source & target
	var cnx = getConnectionBySourceAndTargetId(sourceId, targetId);
	$(cnx).attr('id', cnxId);
	var color = getWeightToColor(2);
	var lw = getWeightToLineWidth(2);

	cnx.setPaintStyle({strokeStyle:color, strokeWidth:lw, stroke: color}); //fill:color for 2.2.0
}

function openConnContext(cnxId){
	if(isView) return;
	if(!cnxId.startsWith("cnx_")) return; //click on endpoint/anchor
	clearErrorMsgs();
	setConnContextVals();
	//var cnx = getConnectionById(cnxId);
	$("#conn_id").html(cnxId);
	$("#connContext").show();
}

function setConnContextVals(){
	$("#connContext").dialog( "option", "width", ['200'] );
	$("#connContext").dialog( "option", "height", ['200'] );
	$("#connContext").dialog( "option", "title", chgCnxDialogTitle);
	$("#connContext").dialog( "option", "buttons", [ ] );
	$("#connContext" ).dialog( "open" );
}

function openConnContextExp(cnxId){
	if(isView) return;
	if(!cnxId.startsWith("cnx_")) return; //click on endpoint/anchor
	clearErrorMsgs();
	//var cnx = getConnectionById(cnxId);
	setConnContextVals();
	$("#conn_id").html(cnxId);
	var stage = $("#chgstage_"+cnxId).val();
	//alert(cnxId);
	$("#conn_stage").val(stage);
	$("#connContext").show();
}

/**
 * in view mode (reports) we display the stage next to the connection when the complete map 
 * shall be displayed.
 * @param cnxId
 * @returns
 */
function displayCnxStage(){
	
	var cns = instance.getConnections('*');
	for(var i =0; i<cns.length; i++){
		var cnx = cns[i];
		alert(cnx.learnerStage);
	}
		
}

function chgConnectionWeight(weight){
	if(isView) return;
	var cnxId = $("#conn_id").html();	
	var cnx = getConnectionById(cnxId);
	var color = getWeightToColor(weight);
	var lw = getWeightToLineWidth(weight);
	cnx.setPaintStyle({strokeWidth:lw, strokeStyle:color, stroke:color}); //stroke: color for 2.2.0
	$("#conn_id").html('');
	$("#connContext" ).dialog( "close" );
	sendAjax(cnxId, doNothing, "chgConnection", weight);	
}

function getConnectionById(cnxId){
	var cns = instance.getConnections('*');
	for(var i =0; i<cns.length; i++){
		cnx = cns[i];
		if(cnx.id==cnxId) return cnx;
	}	
}
/**
 * get the connection with the given source- and targetId. 
 * @param sourceId
 * @param targetId
 * @returns {___anonymous_cnx}
 */
function getConnectionBySourceAndTargetId(sourceId, targetId){
	var cns = instance.getConnections('*');
	for(var i =0; i<cns.length; i++){
		cnx = cns[i];
		if(cnx.sourceId==sourceId && cnx.targetId==targetId) return cnx;
	}	
	//TODO:
	//return jsPlumb.select({source:sourceId,target:targetId});
}

/**
 * looks whether a learner connections is already made between the source and target. If so return 
 * it. We need this as an array, because the cnx the learner currently tries to make is also already
 * registered. So, array length is always >=1!  -> version >2.2.0 - no longer true, so if arr.length=1 we have a duplicate cnx.
 * @param sourceId
 * @param targetId
 * @returns {Array}
 */
function getAllLearnerConnectionBySourceAndTargetId(sourceId, targetId){
	var cns = instance.getConnections('*');
	var cnxArr = new Array();
	var counter = 0;
	for(var i =0; i<cns.length; i++){
		cnx = cns[i];
		if((cnx.sourceId==sourceId && cnx.targetId==targetId  || cnx.targetId==sourceId && cnx.sourceId==targetId) && !cnx.id.startsWith("exp") && !cnx.id.startsWith("con_")){
			cnxArr[counter] = cnx;
			counter ++;
		}
	}
	return cnxArr;
	//TODO:
	//return jsPlumb.select({source:sourceId,target:targetId});
}

/** get color of connection based on the weight **/
function getWeightToColor(weight){
	//return "#006699";
	switch(weight){
	case 6: return "#990000"; //speaks against (red)
	case 5: return "#004466"; //"#999999"; //"#009933"; //highly related (black)
	//case 4: return "#999999"; //"#00e64d"; //somewhat related (dark grey)
	case 3: return "#b3e6ff"; //"#e6e6e6"; //"#e6ffee"; //slightly related (light grey)
	case 8: return "#006699"; //hierarchy
	case 9: return "#cccccc"; //syndrome
	case "6": return "#990000";
	case "5": return "#004466";
	//case "4": return "#999999";
	case "3": return "#b3e6ff";
	case "8": return "#006699";
	case "9": return "#cccccc"; //syndrome
	default: 
		return "#006699"; //"#999999";
	}
}

var default_linewidth = 3;
var strong_linewidth = 4;
var light_linewidth = 2;
/**
 * NOT WORKING, WHY?????
 * @param weight
 * @returns {Number}
 */
function getWeightToLineWidth(weight){
	switch(weight){
	case 6: return default_linewidth; 		//speaks against (red)
	case 5: return strong_linewidth; 		//"#009933"; //highly related (black)
	//case 4: return 4; 	//"#00e64d"; //somewhat related (dark grey)
	case 3: return light_linewidth; 		//"#e6ffee"; //slightly related (light grey)
	case 8: return default_linewidth;
	case 9: return default_linewidth; 
	case 2: return default_linewidth; 		//default
	case "6": return default_linewidth; 	//speaks against (red)
	case "5": return strong_linewidth; 	//"#009933"; //highly related (black)
	//case "4": return 4; 	//"#00e64d"; //somewhat related (dark grey)
	case "3": return light_linewidth; 	//"#e6ffee"; //slightly related (light grey)
	case "8": return default_linewidth; 
	case "9": return default_linewidth; 
	case "2": return default_linewidth; 	//default
	default: 
		return default_linewidth;
	}
}

function getWeightToColorExp(weight){
	return "#006699";
	/*
	switch(weight){
	case 6: return "#990000"; //speaks against (red)
	case 5: return "#009933"; //"#009933"; //highly related (black)
	case 4: return "#00e64d"; //"#00e64d"; //somewhat related (dark grey)
	case 3: return "#e6ffee"; //"#e6ffee"; //slightly related (light grey)
	case 8: return "#006699"; 
	default: 
		return "#00e64d";
	}
	*/
}

function toggleCnxDisplay(){
	hideTooltips();
	clearErrorMsgs();
	toggleCnxStatus();
	initCnxDisplay();
}

function initCnxDisplay(){
	var cnxStatus = getCnxStatus();
	if(cnxStatus==1 || cnxStatus=="1") showCnx();
	else if(cnxStatus==0 || cnxStatus=="0") hideCnx();
}

function hideCnx(){
	$(".jtk-connector").addClass("jtk-connector-hide");
	$(".jtk-exp-connector").removeClass("jtk-exp-connector-show"); //hide expert cnx as well
	$(".jtk-exp-connector").addClass("jtk-exp-connector-hide");
	$("#cnxtoggle").attr("title", showCnxTitle);
	$("#cnxtoggle").removeAttr("checked");
}

function showCnx(){
	$("#cnxtoggle").attr("title", hideCnxTitle);
	$("#cnxtoggle").attr("checked", "checked");
	$(".jtk-connector").removeClass("jtk-connector-hide");
	//if expert on -> display expert cnx: TODO
	if(isOverallExpertOn()){
		$(".jtk-exp-connector").addClass("jtk-exp-connector-show");
		$(".jtk-exp-connector").removeClass("jtk-exp-connector-hide");
	}
	$(".jtk-connector").show();
}

function isInvalidCnx(info, errormsg){
	//first we check whether there has already been made a connection between the source and target. 
	//If so display an error msg
	var con=info.connection;
	var maxCnx = con.source.maxConnections;
	var isInvalid = false;
	var arr = getAllLearnerConnectionBySourceAndTargetId(con.sourceId, con.targetId);
	if(arr.length>=1){
	    var msgSpanId = getErrorMsgSpanByTargetId(con.targetId);
	    $("#"+msgSpanId).html(errormsg);
	    isInvalid =  true;
	 }
	if(con.sourceId == con.targetId){ //connection from/to same element
		isInvalid =  true;
	}
	//for some reason the (invisible) expert elements can be connected,so we prevent this here:
	var cssList =info.target.classList;
	if(cssList[0].startsWith("exp")) isInvalid =  true;
	
	if(isInvalid) instance.deleteConnection(con); 	
	return isInvalid;
}
/**
 * we display the error message for connections in the box of the target item
 * @param targetId
 */
function getErrorMsgSpanByTargetId(targetId){
	var targetStart = targetId.substring(0,4);
	switch(targetStart){
	case "box1":
		return "msg_box1form";	
	case "box2":
		return "msg_box2form";
	case "box3":
		return "msg_box3form";
	case "box4":
		return "msg_box4form";
	/*case "pat":
		return "msg_pathoform";*/
		default: return "";
	}
}
/**
 * when an action has been performed we have to update the cnxs as well. It can be that we have to repaint the exp 
 * cnxs if the learner has added an element that is correct and has an expert cnx (then the id of the element changes).
 */
function reInitCnxsCallback(data){
	 var status = data.status; 
	 if(isCallbackStatusSuccess(data)){
    	fireAddConnection = false; //!!! needs to be here, otherwise we fire another addConnection event!!!
    	var cnxs = instance.getConnections('*');//instance.getConnections();
    	if(cnxs!=null){
        	for(i=0; i<cnxs.length;i++){
        		var tmpCnx = cnxs[i];
        		//if(tmpCnx.id.startsWith("exp_"))
        			instance.deleteConnection(tmpCnx);
        	}
    	}
    	var arr2 = $(".jtk-connector");
    	var cnxs2 = instance.getConnections();
    	parseConns();
    	initConnections();   	
    	
    	//reInitExpConnections();
    	fireAddConnection = true;  
    	//checkDisplayCnxHint();
    }
}

function isOverallCnxOn(){
	if($("#cnxtoggle").prop("checked"))
		return true;
	return false;
}