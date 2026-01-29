/*****
 * functions for the expert script creation process.
 * 
 ******/

function chgStage(chg, loc){
	if(chg==-1 && currentStage==1) return;
	if(chg>=1 && currentStage==maxStage){
		alert("last stage");
		return;
	}
	if(chg==1 || chg==-1){
		var oldStage = currentStage;
		currentStage += chg;
	}
	else if(chg>1) currentStage = chg;
	var winloc = window.location.href;
	
	if(winloc.indexOf("stage")<0) winloc = loc+"?stage="+currentStage;
	else {
		winloc = location.href.replace("stage="+oldStage, "stage="+currentStage);		
	}	
	window.location.href = winloc;
}
/**
** We either display the map at the current stage of the case or display the complete map (as if on last card)
 */
/*function toggleMapDisplay(){
	sendAjax(0, callBackReload, "toggleShowAll", "");
}*/

/**
 * we change the language of the map and reload the page to display the changed list items
 */
function chgMapLang(){
	var newLang = $("#scriptloc").val();
	listUrl="../jsonp_"+newLang+".json";
	//listNursingUrl="../jsonp_n_"+newLang+".json";
	scriptlang = newLang;
	//alert(listUrl);
	if(isEmptyScript=="true"){ //trigger submit, script is empty
		sendAjax(newLang, callBackReload, "changeLangOfScript", "");
	}
	else{ //we have to ask user what he/she wants to do with the already created items
		var cont = confirm("Wollen Sie die Sprache ändern? Es wird versucht alle bereits eingegebenen Knoten in die neue Sprache zu übersetzen.")
		if(cont){
			sendAjax(newLang, callBackReload, "changeLangOfScript", "");			
		}
	}
	//location.reload();
}


function toggleDisplayMode(){
	if(sessionStorage.displayMode==1) sessionStorage.displayMode = 0;
	else sessionStorage.displayMode = 1; 
	/*if (displayMode==1) displayMode = 0; 
	else displayMode = 1;*/
	toggleShowAll();	
}
/**
 * the display of the individual map is either as a step-thru (0) or complete map display (1)
 * @param url
 * @returns
 */
function toggleShowAll(){
	
	var winloc = window.location.href;
	if(winloc.indexOf("repdm")<0) winloc = winloc+"&repdm=0";

	if (sessionStorage.displayMode==1){
		winloc = winloc.replace("repdm=1", "repdm=0");
	}
	else
		winloc = winloc.replace("repdm=0", "repdm=1");
	
	//alert ( winloc);
	window.location.href = winloc;
}

/*
 * expert has chosen a diagnosis to be a final one...
 */
function expFinalDiagnosis(id, box){
	sendAjax(id, boxCallBack, "expSetFinalDiagnosis", "", "", "", box);
}

/*
 * expert has chosen a diagnosis to be a final one...
 */
function expNoFinalDiagnosis(id, box){
	sendAjax(id, boxCallBack, "expSetNoFinalDiagnosis", "", "", "", box);
}


function chgStageItem(id, obj){
	var newStage = $(obj).val();
	if(newStage<=0 || newStage>maxStage){ //check that the new card idx is within the case length!
		alert("Invalid card number");
		return;
	} 
	else
		sendAjax(id, chgStageCallback, "chgStateOfItem", newStage);
}
/* changes the stage when the final diagnosis is made */
function chgStageFinal(id, obj){
	//var id = obj.id;
	//var realId = id.substring(14);
	//var newStage = $("#"+id).val();
	var newStage = $(obj).val();
	sendAjax(id, chgStageCallback, "chgFinalState", newStage);
}

/** 
* the stage at which the connection appears is changed
 */
function chgStageEdge(obj){
	var cnxId = $("#conn_id").html();	// form of "cnx_12345"
	//var cnx = getConnectionById(cnxId);	
	var newStage = $("#conn_stage").val();
	sendAjax(cnxId, chgStageCallback, "chgStateOfEdge", newStage);
}

function chgSummStCrd(obj){
	var id = obj.id;
	var newStage = $("#"+id).val();
	sendAjax(newStage, chgStageCallback, "chgSummStCard", newStage);
}

/**
* we reload the changed box and the connections.
 */
function chgStageCallback(){
	location.reload();
}

function chgBoxType(id, box){
	var newType = $("#"+id).val(); //e.g. 3.20
	var newCat = newType.substring(0,newType.indexOf(".")); // e.g. 3
	var subtype = newType.substring(newType.indexOf(".")+1); // e.g. 20
	if((box!=1 && newCat==box1Type && subtype == box1TitleNum) || (box!=2 && newCat==box2Type && subtype == box2TitleNum) || (box!=3 && newCat==box3Type && subtype == box3TitleNum) || (box!=4 &&newCat==box4Type  && subtype == box4TitleNum)){
		alert("Box type already in use. You cannot have two boxes of the same type.");
		//reset the select box to the original selection:
		$("#"+id).val(getOrgSelVal(box));
		return;	
	}
	//if box is not exmpty, we ask about what to do with the items:
	var itemsCount = $(".box"+box+"s").length;
	if(itemsCount>0){	//TODO: does not work

		//if only subtype change we can keep the items if category/tyoe change we have to inform that it cannot be changed and items will not be displayed
		if(newCat!=getOrgSelVal(box)){ // then we cannot keep items and disable "keep button"
			//TODO WAUDL M
			$("#keepButton").unbind('click');
			$("#keepButton").css('color', '#ff0000');
		}
		 
		$("#chgBoxId").val(id);
		$("#chgBoxIdx").val(box);
		$("#jdialogSwitchCat" ).dialog( "open" );
	}
	else //sendAjaxCM(id, callback, type, name, x, y, box)
		sendAjaxBox(id, boxCallBack, "chgBoxType", newType, false, box);
}


function getOrgSelVal(boxStr){
	var box = +boxStr;
	switch (box){
		case 1: return box1Type+"."+box1TitleNum;
		case 2: return box2Type+"."+box2TitleNum;
		case 3: return box3Type+"."+box3TitleNum;
		case 4: return box4Type+"."+box4TitleNum;		
	}
}
/**
** we come back from the jdialog for deciding whether to keep or discard the items
 */
function confirmSwitch(keepItems){
	//var keepItems = true;
	var id = $("#chgBoxId").val();
	var box = $("#chgBoxIdx").val();
	var newType = $("#"+id).val();
	$("#jdialogSwitchCat" ).dialog( "close" );
	
	sendAjaxBox(id, boxCallBack, "chgBoxType", newType, keepItems, box);
}

function cancelChBoxType(){
	var box = $("#chgBoxIdx").val();
	var id = $("#chgBoxId").val();
	$("#"+id).val(getOrgSelVal(box));
	$("#jdialogSwitchCat" ).dialog( "close" );
}

/*function chgBoxTypeCallback(id, name, box){
	$("[id='box1form:hiddenBox1Button']").click();	
	$("[id='box2form:hiddenBox2Button']").click();	
	$("[id='box3form:hiddenBox3Button']").click();	
	$("[id='box4form:hiddenBox4Button']").click();		
	$("[id='cnxsform:hiddenCnxButton']").click();
}*/


function toggleStageDisplay(){
    $(".stagedisplay").hide();
    $(".stagestepon").show();
    $(".stagestepoff").hide();
    if(displayMode==1){
    	$(".stagedisplay").show(); //display of stages
    	$(".stagestepon").hide(); //the navigation
    	$(".stagestepoff").show();
    }
}

/*function openSelBoxes(title){
	$("#boxesSel").dialog( "option", "width", ['300'] );
	$("#boxesSel").dialog( "option", "height", 'auto' );
	$("#boxesSel").dialog( "option", "maxHeight", '400' );
	$("#boxesSel").dialog( "option", "title", title?title:"");
	$("#boxesSel").dialog( "option", "buttons", [ ] );
	$("#boxesSel" ).dialog( "open" );
	$("#boxesSel").show();
}*/

/**
* we check whether the author has checked 4 or less boxes, if more we alert a warning. 
 */
/*function checkBoxNum(){
	var boxesNum = ($('.boxeschk :checked').size());
	if(boxesNum>4) alert("Please only select 4 Boxes.");
}

function saveBoxesSel(){
	if(($('.boxeschk :checked').size())>4){
		alert("Please only select max. 4 Boxes.");
	}
	else{
		var arr = [];
		$(".boxeschk").each(function(){
	   		if($(this).is(":checked")){
		 		arr.push($(this).val());
	   		}
			else{
		 		arr.push(0);
	   		}
		})
	var vals = arr.join(",");
	//alert(vals);	
		sendAjax(vals, callBackReload, "changeBoxType", "");
	}
}*/

/*function initBoxesSel(){
	   for(var i=1; i<=20;i++){
		//var val = box1type;
		if(box1Type==i || box2Type==i || box3Type==i || box4Type==i)
			$("#boxtypes"+i).prop("checked", true);
	}

}*/

/**  uploaded CM is submitted with selected items and then window can be closed*/
function submitUploadedCM(){
	sendAjax("", callBackUpload, "addSelectedRelations", "");
}

function callBackUpload(){
	alert("back");
}

