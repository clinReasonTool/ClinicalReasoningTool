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
	listNursingUrl="../jsonp_n_"+newLang+".json";
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



/*function chgBox2(){
	var newType = $("#chgbox2").val();
	sendAjax(newType, callBackReload, "changeBoxType2", "");
	//location.reload();
}

function chgBox3(){
	var newType = $("#chgbox3").val();
	sendAjax(newType, callBackReload, "changeBoxType3", "");
	//location.reload();
}

function chgBox4(){
	var newType = $("#chgbox4").val();
	sendAjax(newType, callBackReload, "changeBoxType4", "");
	//location.reload();
}

function chgBox6(){
	var newType = $("#chgbox6").val();
	sendAjax(newType, callBackReload, "changeBoxType6", "");
	//location.reload();
}*/

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
function expFinalDiagnosis(id){
	sendAjax(id, expFinalDiagnosisCallback, "expSetFinalDiagnosis", "");
}

/*
 * expert has chosen a diagnosis to be a final one...
 */
function expNoFinalDiagnosis(id){
	sendAjax(id, expFinalDiagnosisCallback, "expSetNoFinalDiagnosis", "");
}

function expFinalDiagnosisCallback(){
	diagnosisCallBack();
}

/* expert changes the stage for a problem or ddx or ...*/
function chgStageItem(obj){
	var id = obj.id;
	var realId = id.substring(9);
	var newStage = $(obj).val();
	if(newStage<=0 || newStage>maxStage){ //check that the new card idx is within the case length!
		alert("Invalid card number");
		return;
	} 
	else {
		sendAjax(realId, chgStageCallback, "chgStateOfItem", newStage);
	}
}

/* changes the stage when the final diagnosis is made */
function chgStageFinal(obj){
	var id = obj.id;
	var realId = id.substring(9);
	var newStage = $(obj).val();
	sendAjax(realId, chgStageCallback, "chgFinalState", newStage);
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


/**
 * open the jdialog to display the editor to create a new script 
 **/
function createNewScript(){
	$("#jdialog").dialog( "option", "width", ['350'] );
	$("#jdialog").dialog( "option", "height", ['250'] );	
	$("#jdialog").dialog( "option", "title", "Create new script" );
	$("#jdialog").dialog( "option", "buttons", [ ] );
	$("#jdialog").dialog( "open" );	
	$("#jdialog").dialog( "option", "position", [0,0] );
	$("#jdialog").html();
	$("#jdialog").show();
}

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

function openSelBoxes(title){
	$("#boxesSel").dialog( "option", "width", ['300'] );
	$("#boxesSel").dialog( "option", "height", 'auto' );
	$("#boxesSel").dialog( "option", "maxHeight", '400' );
	$("#boxesSel").dialog( "option", "title", title?title:"");
	$("#boxesSel").dialog( "option", "buttons", [ ] );
	$("#boxesSel" ).dialog( "open" );
	$("#boxesSel").show();
}

/**
* we check whether the author has checked 4 or less boxes, if more we alert a warning. 
 */
function checkBoxNum(){
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
}

function initBoxesSel(){
	   for(var i=1; i<=20;i++){
		//var val = box1type;
		if(box1Type==i || box2Type==i || box3Type==i || box4Type==i)
			$("#boxtypes"+i).prop("checked", true);
	}

}

/**  uploaded CM is submitted with selected items and then window can be closed*/
function submitUploadedCM(){
	sendAjax("", callBackUpload, "addSelectedRelations", "");
}

function callBackUpload(){
	alert("back");
}
