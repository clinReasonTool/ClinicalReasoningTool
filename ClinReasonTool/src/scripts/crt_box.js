
/**
 * javascript for displaying the illness script tabs representation
 */

//**************** new box functions  ************/

function addBoxItem(itemId, name, typedinName, box){
	clearErrorMsgs();
	var prefix = $("#box"+box+"_prefix").val();
	
	if(name!=""){
		checkBoxColorOnAdd("box"+box+"_title", "box"+box+"s");
		sendAjax(itemId, boxCallBack, "addBox"+box+"Item", name, prefix, typedinName, box);
	}
}


/**
** user has entered an entry in a box working without a list and clicked on enter, so we submit the 
new entry. 
 */
function addBoxItemWithoutList(event){
	var box = event.target.id.substring(3,4); //e.g. box1list
	var name = event.target.value;
	sendAjax(-99, boxCallBack, "addBox"+box+"Item", name, "", "", box);	
}


//id, callback, type, methodName, typedinName, box
function delBoxItem(id, box){
	clearErrorMsgs();
	//sendAjax(id, callback, methodName, name, prefix, typedinName, box)
	sendAjax(id, delBoxItemCallBack, "delBox"+box+"Item", "", "", "",box);
}


function delBoxItemCallBack(id, name, box){ //name is not needed
	deleteEndpoints("box"+box+"_"+id);
	boxCallBack(id, name, box);
}

function boxCallBack(id, name, box){
	hideAllDropDowns();
	$("#box"+box+"list").val("");	
	$("#box"+box+"_prefix").val("");
	removeElems("box"+box+"s");
	removeElems("expbox"+box+"s");
	$("[id='box"+box+"form:hiddenBox"+box+"Button']").click();		
	$("[id='cnxsform:hiddenCnxButton']").click();
}

function updateBox1Callback(data){updateBoxCallback(data, 1);}
function updateBox2Callback(data){updateBoxCallback(data, 2);}
function updateBox3Callback(data){updateBoxCallback(data, 3);}
function updateBox4Callback(data){updateBoxCallback(data, 4);}

function updateBoxCallback(data, box){
	if(isCallbackStatusSuccess(data)){
		initElems("box"+box);
		initBoxHeights();
	}  
	if(box==diagnosesBox) 
		checkSubmitBtn();
}

/** TODO */
function addJokerBox(box){
	clearErrorMsgs();
	sendAjax("", boxItemCallBack, "addJoker", box);
	//sendAjaxUrlHtml("", problemAddCallBack, "addJoker", 1, "probbox2.xhtml");
}


/**
 * we check whether we have to change the box color from red to light red/gray when an item has been added.
 * @param titleId
 * @param boxnames
 */
function checkBoxColorOnAdd(titleId, boxnames){
	if($("#"+titleId).hasClass("contcol_0")) return; 
	var learnerNumItems = $("."+boxnames);
	var expNumItems = $(".exp"+boxnames);
	var learnerNum = learnerNumItems.length;
	var expNum = expNumItems.length;
	$("#"+titleId).removeClass("contcol_1");
	$("#"+titleId).removeClass("contcol_2");
	if(expNum - learnerNum > 5) $("#"+titleId).addClass("contcol_2") ;
	else if(expNum - learnerNum > 3) $("#"+titleId).addClass("contcol_1") ;
	else  $("#"+titleId).addClass("contcol_0");	
}


function toggleBoxFeedback(box){
	clearErrorMsgs();
	toggleExpBoxFeedback("expFeedbackBox"+box, "boxs", box);
}

function togglePeersBox(box){
	clearErrorMsgs();
	togglePeerBoxFeedback("peerFeedback"+box, "peer_score_box"+box, box);
}


/** a diagnosis is changed to must not missed -red icon or toggled back*/
function toggleMnM(id, box){
	clearErrorMsgs();
	//hideDropDown("ddbox"+box+"_"+id);
	hideAllDropDowns();
	var id2 = "mnmicon_"+id;
	if ($("#"+id2).hasClass("fa-exclamation-circle1")){
		$("#"+id2).removeClass("fa-exclamation-circle1");
		$("#"+id2).addClass("fa-exclamation-circle0");
	}
	else{
		$("#"+id2).removeClass("fa-exclamation-circle0");
		$("#"+id2).addClass("fa-exclamation-circle1");
	}
	sendAjax(id, doNothing, "changeMnM",  "", "", box);
}


/*function addJokerDDX(){
	clearErrorMsgs();
	//sendAjax("", diagnosisCallBack, "addJoker", 2);
	sendAjaxUrlHtml("", diagnosisCallBack, "addJoker", 2, "ddxbox2.xhtml");

}

function toggleBox2Feedback(){
	clearErrorMsgs();
	toggleExpBoxFeedback("expFeedbackBox2", "ddxs", 2);
}

function togglePeersBox2(){
	clearErrorMsgs();
	togglePeerBox2Feedback("peerFeedbackBox2", "peer_score_box2", 2);
}*/


/** 1. we show the submit ddx dialog and ask for confidence **/
function doSubmitDDXDialog(){
	clearErrorMsgs();
	//we check whether there are diagnoses, if so we open a dialog, else we display a hint
	var ddxNum = $( ".box"+diagnosesBox+"s").length;
	$('.ui-tooltip').remove();
	if(ddxNum>0){ //then open jdialog:
		
		$("#jdialog").dialog( "option", "width", ['300'] );
		$("#jdialog").dialog( "option", "height", ['350'] );
		
		$("#jdialog").dialog( "option", "title", submitDialogTitle );
		$("#jdialog").dialog( "option", "buttons", [ ] );
		//$("#jdialog").dialog( "option", "position", [20,20] );
		
		//loadFile();
		if(submitted=="true" || presubmitted=="true") $("#jdialog").load("submitteddialog.xhtml"); // $("#jdialog").load("submitteddialog.xhtml");
		else $("#jdialog").load("submitdialog.xhtml"); //$("#jdialog").load("submitdialog.xhtml");	
		
		$("#jdialog" ).dialog( "open" );
		
		$("#jdialog").show();
		
	}
	else{
		alert(submitDDXOne);
		$('.ui-tooltip').remove();
	}
}

/**
 * successful submission -> continue case = just close dialog...
 */
function continueCase(){
	$("#jdialog" ).dialog( "close" );
	removeElems("box"+diagnosesBox+"s");
	removeElems("expbox"+diagnosesBox+"s");
	$("[id='box"+diagnosesBox+"form:hiddenBox"+diagnosesBox+"Button']").click();
	$("[id='cnxsform:hiddenCnxButton']").click();
}

/** 2. user has selected ddxs and submits final ddxs */
function submitDDXConfirmed(){
	var checks = $(".chb_ddx:checked");
	if(checks.length<=0){
		alert(subcheck);
		return;
	}
	var ids="";
	for(i=0; i<checks.length;i++){
		ids += checks[i].value +"#";
	}
	ids = ids.substring(0, ids.length-1);
	var confVal = $( "#confidence_slider" ).slider( "value" );
	//(id, callback, methodName, name, prefix, typedinName, box)
	//sendAjax(id, doNothing, "changeTier", 5, "", box);
	sendAjax(ids, submitDDXConfirmedCallBack, "submitDDXAndConf",  confVal, "","",diagnosesBox);
}

/** 3. we come back after the submission and have to reload ddxs once again show feedback for submitted diagnoses */
function submitDDXConfirmedCallBack(data){
	$("#jdialog").load("submitteddialog.xhtml");		
}

/* 5. show feedback for submitted diagnoses */
function doScoreDDXDialogCallback(){
	$(".tier_4").prop( "checked", true );
	$(".chb_ddx").attr("disabled","disabled");
}

/*
 * close the dialog and user continues with case....
 */
function backToCase(){	
	$("#jdialog" ).dialog( "close" );	
	sendAjax("", revertSubmissionCallback, "resetFinalDDX","");
}

/** called when the submitting dialog is closed or backToCase is clicked**/
function revertSubmissionCallback(data){
	//we update the ddx boxes (re-printing all boxes)
		presubmitted = "false";
		checkSubmitBtn();
		postEnforceFinalDDXSubmission("false");
}

/* 
 * dialog remains open and user can choose again from list? -> what if correct diagnosis is not included 
 */
function tryAgain(){
	sendAjax("", tryAgainCallback, "resetFinalDDX","");
}

function tryAgainCallback(data){
	presubmitted = "false";
	postEnforceFinalDDXSubmission("false");
	$("#jdialog").load("submitdialog.xhtml");
}

function showSolution(){
	sendAjax("", showSolutionCallBack, "showSolution",  "");
}

function showSolutionCallBack(data){
	//if(isCallbackStatusSuccess(data)){
		showSolutionStage = currentStageScriptNoUPdate;
		$("#jdialog" ).dialog( "close" );
		
		presubmitted = "false";
		submitted = "true";
		postEnforceFinalDDXSubmission("true");
		//make changes visible in the ddx box:
		//removeElems("box"+diagnosesBox+"s");
		//removeElems("expbox"+diagnosesBox+"s");
		//$("[id='box"+diagnosesBox+"form:hiddenBox"+diagnosesBox+"Button']").click();		
		//$("[id='cnxsform:hiddenCnxButton']").click();
		turnOverallExpFeedbackOn("", "");
		clickExpFeedbackOn();
		checkSubmitBtn();
	//}
}

/**
 * called when user clicks on close button of the submit dialog
 */
function closeSubmitDialog(){
	//just close jdialog if diagnosis already submitted OR only submitdialog is opened.
	if(submitted=="true" || presubmitted=="false"){
		//removeElems("ddxs");
		//removeElems("expddxs");
		//$("[id='box"+diagnosesBox+"form:hiddenBox"+diagnosesBox+"Button']").click();
		//$("[id='cnxsform:hiddenCnxButton']").click();
		checkSubmitBtn();
		return true; 
	}
	if(parseInt(currentStage) < parseInt(maxSubmittedStage)){ //user can continue the case:
		sendAjax("", revertSubmissionCallback, "resetFinalDDX","");
		return true;
	}
	else{ //user has already reached max number of cards and has to decide about final diagnosis, so we do NOT close the dialog
		//but display a message:
		$("#enforceSubmitMsg").show();
		return false;
	}
}

function ruleOut(id, box){
	clearErrorMsgs();
	//hideDropDown("ddbox"+boxNumber+"_"+id);	
	var item = $("#box"+box+"_"+id);	//box#{boxNumber}_#{itembox.id}
	item.css("border-color", "#cccccc"); 
	item.css("color", "#cccccc");
	$("#ri_a_"+id).removeClass();
	$("#ro_a_"+id).removeClass();
	$("#ro_a_"+id).hide();
	$("#ri_a_"+id).show();
	hideAllDropDowns();
	sendAjax(id, doNothing, "changeTier", 5, "", box);
}

function ruleIn(id, box){
	clearErrorMsgs();
	//ideDropDown("ddbox"+boxNumber+"_"+id);
	var item = $("#box"+box+"_"+id);	//box#{boxNumber}_#{itembox.id}
	item.css("border-color", "#000000"); 
	item.css("color", "#000000");
	$("#ri_a_"+id).removeClass();
	$("#ro_a_"+id).removeClass();
	$("#ri_a_"+id).hide();
	$("#ro_a_"+id).show();
	hideAllDropDowns();
	sendAjax(id, doNothing, "changeTier", 5, "", box);
}

function workingDDXOn(id, box){
	clearErrorMsgs();
	//hideDropDown("ddbox"+boxNumber+"_"+id);
	var item = $("#box"+box+"_"+id);	//box#{boxNumber}_#{itembox.id}
	item.css("background-color", getRectColor(6))
	//item.css("color", "#c00815");
	$("#wdon_a_"+id).removeClass();
	$("#wdoff_a_"+id).removeClass();
	$("#wdon_a_"+id).hide();
	$("#wdoff_a_"+id).show();
	hideAllDropDowns();
	sendAjax(id, doNothing, "changeTier",  6, "", box);
}

function workingDDXOff(id, box){
	clearErrorMsgs();
	//hideDropDown("ddbox"+boxNumber+"_"+id);
	var item = $("#box"+box+"_"+id);	//box#{boxNumber}_#{itembox.id}
	item.css("background-color", getRectColor(-1))
	//item.css("color", "#000000");
	$("#wdoff_a_"+id).removeClass();
	$("#wdon_a_"+id).removeClass();
	$("#wdoff_a_"+id).hide();
	$("#wdon_a_"+id).show();
	hideAllDropDowns();
	sendAjax(id, doNothing, "changeTier",  6, "", box);
}

 function getRectColor(tier){
	if(tier==5) return "#cccccc"; //ruled-out
	if(tier==6) return "#cce6ff"; //working diagnosis
	if(tier==4) return "#80bfff"; //final diagnosis
	//if(tier==7) return "#f3546a"; //MnM
	return "#ffffff";
 }

function checkSubmitBtn(){
	var ddxNum = $( ".box"+diagnosesBox+"s").length;
	//enable submit button:
	if(ddxNum>0 && submitted!="true"){
		$("#submitBtnSpan").removeClass("submitBtnOff");
	}
	//disable submit button:
	else if((ddxNum<=0 || submitted=="true") && !$("#submitBtnSpan").hasClass("submitBtnOff")){
		$("#submitBtnSpan").addClass("submitBtnOff");		
	}
	if( submitted=="true"){
		$("#submitBtnA").html(submittedButonName);
	}
	else $("#submitBtnA").html(submitButonName);
}

/*
 * user has changed the confidence slider, so, we send the new value via ajax.
 * -> now we submit it together with the ddx submission.
 */
function submitSliderChange(){
	var confVal = $( "#confidence_slider" ).slider( "value" );
	//sendAjax(-1, doNothing, "changeConfidence",  confVal);
}
/**
 * we init the display of the dialog shown after a diagnosis has been submitted.
 * depending on the score we display different dialogs. If showSolution or score >=0.5 we als trigger 
 * the update of the ddx box to show the final diagnosis in the appropriate color.
 */
function initSubmittedDialog(){
	if(minScoreCorrect=="") minScoreCorrect = "0.5";
	presubmitted = "true";
	$(".tier_4").prop( "checked", true );
	var ddxsFBIcons = $(".expfb");
	if(ddxsFBIcons!=null){
		for(i=0; i<ddxsFBIcons.length;i++){
			var iItem =  ddxsFBIcons[i];
			if($(iItem).attr("mytooltip")=="") $(iItem).hide();
		}
	}
	//learner has the correct or pretty correct solution:
	if($("#score").val()>=parseFloat(minScoreCorrect) || $("#score").val()==-2){ // 50 - 100% correct solution or no scoring possible:
		submitted = "true";
		presubmitted = "false";
		$(".aftersubmit_succ").show();
		$(".aftersubmit_fail").hide();
		$(".errors").hide();
		postEnforceFinalDDXSubmission(submitted/*, myStage, maxSubmittedStage*/);
		return;
	}
	 //show solution has been selected
	if(showSolutionStage!="" && showSolutionStage!="-1" && showSolutionStage!="0"){
		submitted = "true";
		$(".aftersubmit_succ").show();
		$(".aftersubmit_fail").hide();
		postEnforceFinalDDXSubmission(submitted/*, myStage, maxSubmittedStage*/);

		return;
	}
	else{
		$(".aftersubmit_fail").show();	
		$(".aftersubmit_succ").hide();
		if($("#errors").html().trim()=="") 
			$(".errors").hide();
	}

}


/******************** summary statement *******************************/


function saveSummSt(id){
	clearErrorMsgs();
	var text =  $("#summStText").val();		
	sendAjax(id, saveSummStatementCallBack, "saveSummStatement",text);	
}

function saveSummStatementCallBack(data){
	//if(isCallbackStatusSuccess(data)){
	$("#msg_sumstform").html(saveSumConfirm);	
	//}
}



 
/******************** Feedback *******************************/

  /*
   * display exp feedback (green/gray borders for boxes) for given box/type.
   * Does not include missing items!
   */
function toggleExpBoxFeedback(iconId, itemClass, type){
	if($("#"+iconId).hasClass("fa-user-md_on")){ //turn feedback off
		turnExpBoxFeedbackOff(iconId, itemClass);
		sendAjaxContext(0, doNothing, "toogleExpBoxFeedback", type);
	}
	else{ //turn feedback on
		turnExpBoxFeedbackOn(iconId, itemClass);
		sendAjaxContext(1, doNothing, "toogleExpBoxFeedback", type);
	}
}

function turnExpBoxFeedbackOn(iconId, itemClass){
	//if($("#"+iconId).hasClass("fa-user-md_on")) return; //already on

	$("#"+iconId).removeClass("fa-user-md_off");
	$("#"+iconId).addClass("fa-user-md_on");
	$("#"+iconId).attr("title", hideExpTitle);
	$("."+itemClass+"_score").show();
	if(itemClass!="ddxs") return;
	var items = $("."+itemClass);
	if(items!=null){
		for (i=0; i<items.length; i++){
			var suffix = $("#"+items[i].id).attr("suffix");
			var expSuffix = $("#expstyle_"+items[i].id).attr("suffix");
			if(suffix!=expSuffix && expSuffix>=0){
				$("#"+items[i].id).removeClass("ddxclass_"+suffix);
				$("#"+items[i].id).addClass("ddxclass_"+expSuffix);
			}
		}
	}
}

function turnExpBoxFeedbackOff(iconId, itemClass){
	if($("#"+iconId).hasClass("fa-user-md_off")) return; //already off
	
	$("#"+iconId).removeClass("fa-user-md_on");
	$("#"+iconId).addClass("fa-user-md_off");
	$("#"+iconId).attr("title", showExpTitle);
	$("."+itemClass+"_score").hide();
	if(itemClass!="ddxs") return;
	var items = $("."+itemClass);
	if(items!=null){
		for (i=0; i<items.length; i++){
			var suffix = $("#"+items[i].id).attr("suffix");
			var expSuffix = $("#expstyle_"+items[i].id).attr("suffix");
			if(suffix!=expSuffix && expSuffix>=0){
				$("#"+items[i].id).removeClass("ddxclass_"+expSuffix);
				$("#"+items[i].id).addClass("ddxclass_"+suffix);				
			}
			//var cssclass = $("#"+items[i].id).attr("class");
			//alert(cssclass);
			//addClass("box_"+score);
		}
	}
}

var addHeightPixSum = 0;
/*
 * display or hide the expert's summary statement
 */
function toggleSumFeedback(iconId, type){
	clearErrorMsgs();
	if($("#"+iconId).hasClass("fa-user-md_on")){ //turn feedback off
		$("#"+iconId).removeClass("fa-user-md_on");
		$("#"+iconId).addClass("fa-user-md_off");		
		$("#"+iconId).attr("title", showExpTitle);
		$("#list_score_sum").hide();
		sendAjaxContext(0, doNothing, "toogleExpBoxFeedback", type);

	}
	else{ //turn feedback on
		//sumstanchor
		$("#"+iconId).removeClass("fa-user-md_off");	
		$("#"+iconId).addClass("fa-user-md_on");
		$("#"+iconId).attr("title", hideExpTitle);
		$("#list_score_sum").show();
		//var element = document.getElementById("list_score_sum");
		$("#list_score_sum")[0].scrollIntoView({
		    behavior: "smooth", // or "auto" or "instant"
		    block: "start" // or "end"
		});
		sendAjaxContext(1, doNothing, "toogleExpBoxFeedback", type);
	}
}

function isOverallExpertOn(){
	if($("#expFeedback").prop("checked"))
		return true;
	return false;
}

function clickExpFeedbackOn(){
	$("#expFeedback").prop("checked", "true");
}

/*
 * we display the peer feedback for the given box/type
 */
function togglePeerBoxFeedback(iconId, itemClass, type){
	hideTooltips();
	if($("#"+iconId).hasClass("fa-users_on")){ //turn off
		$("#"+iconId).removeClass("fa-users_on");
		$("#"+iconId).addClass("fa-users_off");		
		$("#"+iconId).removeClass("icon-users_on");
		$("#"+iconId).attr("title", "click to hide expert feedback");
		$("."+itemClass).hide();
		sendAjaxContext(0, doNothing, "tooglePeerBoxFeedback",  type);
	
	}	
	else{ //turn it on
		$("#"+iconId).removeClass("fa-users_off");	
		$("#"+iconId).addClass("fa-users_on");
		$("#"+iconId).attr("title", "click to show expert feedback");
		$("."+itemClass).show();
		sendAjaxContext(1, doNothing, "tooglePeerBoxFeedback",  type);
	}
}


/*
 * display/hide overall expert feedback (including missing items)
 * if isAllowed=false, then user is not allwoed to access feedback at this time
 */
function toggleExpFeedback(iconId, itemClass, isAllowed){
	hideTooltips();
	if(isAllowed=="false" || !isAllowed) return; //should not happen, because button is hidden anyway. 
	if($("#"+iconId).hasClass("fa-user-md_on")){ //turn feedback off
		turnOverallExpFeedbackOff(iconId, itemClass);
		sendAjaxContext(0, doNothing, "toogleExpFeedback", "");
	}
	else{ 
		turnOverallExpFeedbackOn(iconId, itemClass);
		sendAjaxContext(1, doNothing, "toogleExpFeedback", "");
	}
}


function turnOverallExpFeedbackOn(iconId, itemClass){
	turnViewModeOff();
	if(iconId!=""){
		$("#"+iconId).removeClass("fa-user-md_off");	
		$("#"+iconId).addClass("fa-user-md_on");
		$("#"+iconId).attr("title", hideExpTitle);
	}
	$(".expbox").addClass("expboxstatus_show");
	$(".expbox").removeClass("expboxstatus");
	$(".expbox").removeClass("expboxinvis");
	if(box1Mode==1)turnExpBoxFeedbackOn("expFeedbackBox1", "box1");
	if(box2Mode==1) turnExpBoxFeedbackOn("expFeedbackBox2", "box2");
	if(box3Mode==1)turnExpBoxFeedbackOn("expFeedbackBox3", "box3");
	if(box4Mode==1)turnExpBoxFeedbackOn("expFeedbackBox4", "box4");
	if(isOverallCnxOn()){
		$(".jtk-exp-connector").addClass("jtk-exp-connector-show");
		$(".jtk-exp-connector").removeClass("jtk-exp-connector-hide");
	}
}

function turnViewModeBoxOn(box, prefix, prefix2){
	if(box=="2"){ //view mode
		$(".pass"+prefix+"s").removeClass("passboxinvis");
		$(".pass"+prefix+"s").removeClass("passboxstatus");
		$(".pass"+prefix+"s").addClass("passboxstatus_show");
		//turnExpBoxFeedbackOn("expFeedbackFdg", "fdgs");
		$("."+prefix2+"search").hide(); //hide search box
		//THIS IS AN UGLY HACK - without it, the box is 30px downwards.
		$("#"+prefix+"_box.search").removeClass("boxchild");
		$("#"+prefix+"_box.search").height(30);
		$("."+prefix+"passive").show();
	}
}

function turnViewModeBoxOff(box, prefix, prefix2){
	if(box=="2"){ //view mode
		$(".pass"+prefix+"s").addClass("passboxinvis");
		$(".pass"+prefix+"s").addClass("passboxstatus");
		$(".pass"+prefix+"s").removeClass("passboxstatus_show");
		//turnExpBoxFeedbackOn("expFeedbackFdg", "fdgs");
		//$("."+prefix2+"search").hide(); //hide search box
		//THIS IS AN UGLY HACK - without it, the box is 30px downwards.
		//$("#"+prefix+"_box.search").removeClass("boxchild");
		//$("#"+prefix+"_box.search").height(30);
		$("."+prefix+"passive").show();
	}
}

/*
 * if boxes are displayed in view mode we show the expert items and hide the search box.
 */
function turnViewModeOn(){
	turnViewModeBoxOn(box1Mode, "box1", "box1");
	turnViewModeBoxOn(box2Mode, "box2", "box2");
	turnViewModeBoxOn(box3Mode, "box3", "box3");
	turnViewModeBoxOn(box4Mode, "box4", "box4");
}

function turnViewModeOff(){
	turnViewModeBoxOff(box1Mode, "box1", "box1");
	turnViewModeBoxOff(box2Mode, "box2", "box2");
	turnViewModeBoxOff(box3Mode, "box3", "box3");
	turnViewModeBoxOff(box4Mode, "box4", "box4");
}

function turnOverallExpFeedbackOff(iconId, itemClass){
	if(iconId!=""){
		$("#"+iconId).removeClass("fa-user-md_on");
		$("#"+iconId).addClass("fa-user-md_off");	
		$("#"+iconId).attr("title", showExpTitle);
	}
	$(".expbox").removeClass("expboxstatus_show");
	$(".expbox").addClass("expboxstatus");
	turnExpBoxFeedbackOff("expFeedbackBox1", "box1");
	turnExpBoxFeedbackOff("expFeedbackBox2", "box2");
	turnExpBoxFeedbackOff("expFeedbackBox3", "box3");
	turnExpBoxFeedbackOff("expFeedbackBox4", "box4");
	$(".jtk-exp-connector").addClass("jtk-exp-connector-hide");
	$(".jtk-exp-connector").removeClass("jtk-exp-connector-show");
	turnViewModeOn();
}

function openErrorDialog(){
	$("#jdialogError").dialog( "option", "width", ['300'] );
	$("#jdialogError").dialog( "option", "height", ['300'] );
	$("#jdialogError").dialog( "option", "title", errorDialogTitle);
	$("#jdialogError").dialog( "option", "buttons", [ ] );
	//$("#jdialogError").load("errors.xhtml");
	$('.ui-tooltip').remove();
	$("#jdialogError" ).dialog( "open" );
	$("#jdialogError").show();
}

/*
 * show the expert items as a step-wise process thru the stages
 */
function showExpStages(){
	
}

/**
 * global switch using jq dlg api for drop downs
 */
var dropDownJQueryDlg = true;

/*
 * update experimental using jq dlg!
 * We get the position of the pos element (the itembox) and position the dropdown menu close to it
 */
function showDropDown(id, category){	
	hideAllDropDowns(); //we first hide all in case any are still open...
	clearErrorMsgs();
	
	if (dropDownJQueryDlg) {
		var title = $("#"+id + " .title").html();
		$("#jdialogToolbox").dialog( "option", "title", title);
		$("#jdialogToolbox").dialog( "option", "buttons", [ ] );
		$("#jdialogToolbox").dialog( "option", "minWidth", 150 );
		
		$("#jdialogToolbox").html($("#"+id).html());
		//$("#jdialogError").load("errors.xhtml");
		
		$('#jdialogToolbox .dropdownX').hide();
		if(category<2)
			$(".linkscategory").hide();
		else $(".linkscategory").show();
		
		$("#jdialogToolbox").dialog( "open" );
		$("#jdialogToolbox").show();
	}
	/*else {
		$("#"+id).show();
		var x =  $("#"+pos).position().left-10; //changed to -10 from +10 because otherwise cut-off on right side
		var y = $("#"+pos).position().top+5;
		$("#"+id).css( { left: x + "px", top: y + "px" } ) 
	}*/
	
}

/**
 * Onmouseleave (! not onmouseout) we hide the dropdown again
 * when jq dlg by dlg api!
 */
/*function hideDropDown(id){
	$("#"+id).hide();
	$("#jdialogToolbox").hide();
}*/

/**
 * not needed when using jq dlg, as this is modal!
 */
function hideAllDropDowns(){
	$(".dropdown-content").hide();
	$("#jdialogToolbox").hide();
	$("#jdialogToolbox").dialog('close');
}

// ********************************************************

function clearErrorMsgs(){
	$(".errormsg").html("");
}

function hideTooltips(){
	$('.ui-tooltip').remove();
	$('.hintdiv').hide();
}
/**
 * opens the help dialog
 */
function openHelp(expFBMode){
	clearErrorMsgs();
	
	$("#jdialogHelp").dialog( "option", "width", ['350'] );
	$("#jdialogHelp").dialog( "option", "position",  {my: "center top", at: "center top", of: window}  );
	$("#jdialogHelp").dialog( "option", "height", ['400'] );
	$("#jdialogHelp").dialog( "option", "title", helpDialogTitle);
	$("#jdialogHelp").dialog( "option", "buttons", [ ] );
	$("#jdialogHelp").load("help/index_"+lang+".template");
	//$("#help" ).dialog.html(template);
	$('.ui-tooltip').remove();
	
	$("#jdialogHelp" ).dialog( "open" ).dialog("widget").css("visibility", "hidden");
	
	$("#helpicon").effect("transfer", { //opening effect to show where help icon is:
        to: $("#jdialogHelp").dialog("widget"),
        className: "ui-effects-transfer"
    	}, 500, function () {
        $("#jdialogHelp").dialog("widget").css("visibility", "visible");
        toggleExpFBHelp(expFBMode);
    });
	//toggleExpFBHelp(expFBMode);

	//var helpoptions = { to: "#jdialogHelp" , className: "ui-effects-transfer" }
	//$( "#helpicon" ).effect( "transfer", helpoptions, 500, openHelpCallback );
}

/**
 * depending on the exp feedback mode we hide/display different sections in the help pages
 * @param expFBMode
 */
function toggleExpFBHelp(expFBMode){
	if(expFBMode<0) expFBMode = 0;
	$("#help_expfb_0").hide();
	$("#help_expfb_1").hide();
	$("#help_expfb_"+ expFBMode).show();
}

function closeHelpDialog(){}

function openHelpCallback() {
	$("#jdialogHelp").show();
	//toggleExpFBHelp(1);
}

function removeElems(className){
	turnOverallExpFeedbackOn("", "");
	var arr = $("."+className);
	if(arr!=null){
		for(i=0; i<arr.length;i++){
			var rmId = $(arr[i]).attr("id");
			deleteEndpoints(rmId);
			instance.remove(arr[i],true);
		}
	}
	item_arr = new Array();
	exp_arr = new Array();
	turnOverallExpFeedbackOff("", "");
}

function correctFdgScore(in_score) {
	return correctSpecialScore (in_score, 40, 2, 80, 40, 3);
}

function correctDDxScore(in_score) {
	return correctSpecialScore (in_score, 40, 2, 80, 40, 3);
}

function correctTstScore(in_score) {
	return correctSpecialScore (in_score, 40, 2, 80, 40, 3);
}

function correctMngScore(in_score) {
	return correctSpecialScore (in_score, 40, 2, 80, 40, 3);
}
/**
** in the view mode we can hide/show the scoring for each item (checkmarks)
 */
function toggleExpItemFeedback(){
	if($("#expItemFeedback").prop("checked"))
		$(".icons_score").show();
	else	
		$(".icons_score").hide();
	
}


/******************** general add/del*******************************/

/**
 * user changes the course of time, we trigger an ajax call to save change.
 */
/*function changeCourseOfTime(){
	var courseTime = $("#courseTime").val(); 
	sendAjax(courseTime, doNothing, "chgCourseOfTime", "");
}*/


/******************** problems tab*******************************/

/**
 * a problem is added to the list of the already added problems:
 **/
/*function addProblem(problemId, name, typedinName){
	clearErrorMsgs();
	var prefix = $("#fdg_prefix").val();
	
	if(name!=""){
		checkBoxColorOnAdd("fdg_title", "fdgs");
		sendAjax(problemId, problemCallBack, "addProblem", prefix, typedinName);
	}
}
*/

/*unction chgProblem(id, type){
	clearErrorMsgs();
	sendAjax(id, problemCallBack, "changeProblem", type);
	
}

function delProblem(id){
	clearErrorMsgs();
	sendAjax(id, delProbCallBack, "delProblem", "");
}


function delProbCallBack(probId, selProb){
	deleteEndpoints("fdg_"+probId);
	problemCallBack(probId, selProb);
}

function problemCallBack(){
	$("#problems").val("");	
	$("#fdg_prefix").val("");
	//var arr = $(".fdgs");
	removeElems("fdgs");
	removeElems("expfdgs");
	//we update the problems list and the json string
	$("[id='probform:hiddenProbButton']").click();		
	$("[id='cnxsform:hiddenCnxButton']").click();
}

function updateProbCallback(data){
	if(isCallbackStatusSuccess(data)){
		updateItemCallback(data, "fdg", "fdg_box");
		if(isOverallExpertOn()) //re-display expert items:
			turnOverallExpFeedbackOn('expFeedback', 'icon-user-md');
	}
	
}


function addJokerFdg(){
	clearErrorMsgs();
	sendAjax("", problemCallBack, "addJoker", 1);
	//sendAjaxUrlHtml("", problemAddCallBack, "addJoker", 1, "probbox2.xhtml");
} */

/******************** diagnoses *******************************/

/**
 * a diagnosis is added to the list of the already added diagnoses:
 **/
/*function addDiagnosis(diagnId, name, typedinName){
	clearErrorMsgs();
	if(name!=""){
		checkBoxColorOnAdd("ddx_title", "ddxs");
		sendAjax(diagnId, diagnosisCallBack, "addDiagnosis", name, typedinName);
	}
		//sendAjaxUrlHtml(diagnId, diagnosisAddCallBack, "addDiagnosis", name, "ddxbox2.xhtml");
}

function delDiagnosis(id){
	clearErrorMsgs();
	sendAjax(id, delDiagnosisCallBack, "delDiagnosis", "");
}*/

/** we come back from deleting a diagnosis **/
/*function delDiagnosisCallBack(ddxId, selDDX){
	//we have to delete the endpoint separately, otherwise they persist....
	//deleteEndpoints("ddx_"+ddxId);
	diagnosisCallBack();
}*/

/**
 * we come back from adding a diagnosis (either manually or with a joker)
 */
/*function diagnosisCallBack(){
	$("#ddx").val("");		
	//$(".ddxs").remove();	
	removeElems("ddxs");
	removeElems("expddxs");

	//we update the ddx boxes (re-printing all boxes)
	$("[id='ddxform:hiddenDDXButton']").click();	
	$("[id='cnxsform:hiddenCnxButton']").click();	
}*/

/**
 * click on the hidden ddx button calls this function
 */
/*function updateDDXCallback(data){
	if(isCallbackStatusSuccess(data)){
		checkSubmitBtn();
		updateItemCallback(data, "ddx", "ddx_box");
		if(isOverallExpertOn()) //re-display expert items:
			turnOverallExpFeedbackOn('expFeedback', 'icon-user-md');
	}
}*/

/******************** management *******************************/

/*
 * a management item is added to the list of the already added items:
 */
/*function addManagement(mngId, name, typedinName){
	clearErrorMsgs();
	if(name!=""){
		checkBoxColorOnAdd("mng_title", "mngs");
		sendAjax(mngId, managementCallBack, "addMng", name, typedinName);
	}
}

function delManagement(id){
	clearErrorMsgs();
	sendAjax(id, delManagementCallBack, "delMng", "");
}

function chgManagement(id, type){
	clearErrorMsgs();
	sendAjax(id, managementCallBack, "changeMng", type);
	
}

function delManagementCallBack(mngId, selMng){
	//if(isCallbackStatusSuccess(data)){
		//deleteEndpoints("mng_"+mngId);
		managementCallBack(mngId, selMng);
	//}
}

function managementCallBack(mngId, selMng){
	//if(isCallbackStatusSuccess(data)){
		$("#mng").val("");	
		//$(".mngs").remove();
		removeElems("mngs");
		removeElems("expmngs");
		//we update the problems list and the json string
		$("[id='mngform:hiddenMngButton']").click();	
		$("[id='cnxsform:hiddenCnxButton']").click();
	//}
}

function updatMngCallback(data){
	if(isCallbackStatusSuccess(data)){
		updateItemCallback(data, "mng", "mng_box");
		if(isOverallExpertOn()) //re-display expert items:
			turnOverallExpFeedbackOn('expFeedback', 'icon-user-md');
	}
}

function addJokerMng(){
	clearErrorMsgs();
	sendAjax("", managementCallBack, "addJoker", 4);
}

function toggleMngFeedback(){
	clearErrorMsgs();
	toggleExpBoxFeedback("expFeedbackBox4g", "mngs", 4);
}

function togglePeersMng(){
	clearErrorMsgs();
	togglePeerBoxFeedback("peerFeedbackMng", "peer_score_box4", 4);
}
*/
/******************** diagnostic steps *******************************/

/*
 * a test is added to the list of the already added tests:
 */
/*function addTest(testId, name, typedinName){
	clearErrorMsgs();
	if(name!=""){
		checkBoxColorOnAdd("tst_title", "tests");
		sendAjax(testId, testCallBack, "addTest", name, typedinName);
	}
}

function delTest(id){
	clearErrorMsgs();
	sendAjax(id, delTestCallBack, "delTest", "");
}

function delTestCallBack(testId, selTest){
	//deleteEndpoints("tst_"+testId);
	testCallBack(testId, selTest);
}

function testCallBack(testId, selTest){
	$("#test").val("");	
	//$(".tests").remove();
	removeElems("tests");
	removeElems("exptests");
	//we update the problems list and the json string
	$("[id='testform:hiddenTestButton']").click();		
	$("[id='cnxsform:hiddenCnxButton']").click();
}

function updateTestCallback(data){
	if(isCallbackStatusSuccess(data)){
		updateItemCallback(data, "tst","tst_box");
		if(isOverallExpertOn()) //re-display expert items:
			turnOverallExpFeedbackOn('expFeedback', 'icon-user-md');
	}
}

function chgTest(id, type){
	clearErrorMsgs();
	sendAjax(id, testCallBack, "changeTest", type);
	
}

function addJokerTest(){
	clearErrorMsgs();
	sendAjax("", testCallBack, "addJoker", 3);
}

function toggleTestFeedback(){
	clearErrorMsgs();
	toggleExpBoxFeedback("expFeedbackTest", "tests", 3);
}

function togglePeersTest(){
	clearErrorMsgs();
	togglePeerBoxFeedback("peerFeedbackTest", "peer_score_test", 3);
}
*/
/******************** pathophysiology *******************************/

/*
 * a test is added to the list of the already added tests:
 */
/*function addPatho(pathoId, name, typedinName){
	clearErrorMsgs();
	if(name!=""){
		checkBoxColorOnAdd("patho_title", "patho");
		sendAjax(pathoId, pathoCallBack, "addPatho", name, typedinName);
	}
}

function delPatho(id){
	clearErrorMsgs();
	sendAjax(id, delPathoCallBack, "delPatho", "");
}

function delPathoCallBack(pathoId, selPatho){
	pathoCallBack(pathoId, selPatho);
}

function pathoCallBack(pathoId, selPatho){
	$("#patho").val("");	
	//$(".tests").remove();
	removeElems("patho");
	removeElems("exppatho");
	//we update the problems list and the json string
	$("[id='pathoform:hiddenPathoButton']").click();		
	$("[id='cnxsform:hiddenCnxButton']").click();
}

function updatePathoCallback(data){
	if(isCallbackStatusSuccess(data)){
		updateItemCallback(data, "pat","patho_box");
		if(isOverallExpertOn()) //re-display expert items:
			turnOverallExpFeedbackOn('expFeedback', 'icon-user-md');
	}
}

function chgPatho(id, type){
	clearErrorMsgs();
	sendAjax(id, pathoCallBack, "changePatho", type);
	
}

function addJokerPatho(){
	clearErrorMsgs();
	sendAjax("", pathoCallBack, "addJoker", 3);
}

function togglePathoFeedback(){
	clearErrorMsgs();
	toggleExpBoxFeedback("expFeedbackPatho", "patho", 6);
}

function togglePeersPatho(){
	clearErrorMsgs();
	togglePeerBoxFeedback("peerFeedbackPatho", "peer_score_patho", 6);
}*/



/****************items in general *************** */

/*function addItem(itemId, name, typedinName, itemType, box){
	clearErrorMsgs();
	if(name!=""){
		checkBoxColorOnAdd(itemType.toLowerCase()+"_title", itemType.toLowerCase());
		//sendAjaxItemUrl(id, callback, type, name, typedInName, action)
		sendAjaxItemUrl(itemId, itemCallBack, itemType, name, typedinName, "add", box);
	}
}*/

/*function delItem(itemId, itemType, box){
	clearErrorMsgs();
	//sendAjax(id, delItemCallBack, "del"+itemNameUpper, "");
	sendAjaxItemUrl(itemId, itemCallBack, itemType, "", "", "del", box);
}*/

/*function itemCallBack(itemId, action, itemTypeUpper, box){
	$("#"+itemTypeUpper.toLowerCase()).val("");	
	//$(".tests").remove();
	hideDropDown("dd"+itemTypeUpper.toLowerCase()+"_"+itemId);
	removeElems(itemTypeUpper.toLowerCase());
	removeElems("exp"+itemTypeUpper.toLowerCase());
	//we update the problems list and the json string
	$("[id='"+itemTypeUpper.toLowerCase()+"form:hidden"+ itemTypeUpper+"Button'").click();		
	$("[id='cnxsform:hiddenCnxButton']").click();
}

function updateItemCallback(data, itemNameLower, box){
	if(isCallbackStatusSuccess(data)){
		updateItemCallback(data, "pat",itemNameLower+"_box");
		if(isOverallExpertOn()) //re-display expert items:
			turnOverallExpFeedbackOn('expFeedback', 'icon-user-md');
	}
}*/

/*function chgItem(id, type, itemNameUpper, box){
	clearErrorMsgs();
	//sendAjax(id, itemCallBack, "change"+itemNameUpper, type);
	sendAjaxItemUrl(id, itemCallBack, itemNameUpper, "", "", "change", box);
	
}

function addJokerItem(itemIdentifier, box){
	clearErrorMsgs();
	sendAjax("", itemCallBack, "addJoker", itemIdentifier, box);
}

function toggleItemFeedback(itemNameUpper, itemIdentifier, box){
	clearErrorMsgs();
	toggleExpBoxFeedback("expFeedback"+itemNameUpper, itemNameUpper.toLowerCase(), itemIdentifier, box);
}

function togglePeersItem(itemIdentifier, box){
	clearErrorMsgs();
	togglePeerBoxFeedback("peerFeedback"+itemNameUpper, "peer_score_"+itemNameUpper.toLowerCase(), itemIdentifier, box);
}
*/
/******************end items in general*************+ */


