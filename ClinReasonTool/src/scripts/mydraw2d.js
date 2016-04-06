
var lookUpLabels = [];
var lookUpShortLabels = [];
var counter = 0;
/******************** concept map *******************************/
/**
 * read the json strings and place the rectangles and connection on the canvas.
 */
function initConceptMap(){
	//initConeptMap2("jsonGraph", "jsonConns");
	var conns = jQuery.parseJSON($("#jsonConns").html());
	var jsonRects = jQuery.parseJSON($("#jsonGraph").html());
	for(i=0; i<jsonRects.length;i++){
		createAndAddRectangle(jsonRects[i].label, jsonRects[i].x, jsonRects[i].y, jsonRects[i].id, jsonRects[i].shortlabel, jsonRects[i].type, jsonRects[i].l, jsonRects[i].e, jsonRects[i].p );
	}
	
	if(conns!=''){
		for(j=0; j<conns.length;j++){
			createConnection(conns[j].id, conns[j].sourceid, conns[j].targetid);
		}
	}
}


/*
 * When the learner clicks on the concept map tab we init an ajax call to make sure that the graph json representation 
 * is up-to-date.
 */
function updateGraph(){
	//alert("updateGraph");
	$("[id='graphform:hiddenGraphButton']").click();
}


/** when coming back from adding items, we empty the map and init it again from the updated json string*/
function updateCMCallback(data){
	switch (data.status) {
    case "begin": // This is called right before ajax request is been sent.
        //button.disabled = true;
        break;

    case "complete": // This is called right after ajax response is received.
        // We don't want to enable it yet here, right?
        break;

    case "success": // This is called right after update of HTML DOM.
    	my_canvas.clear();
    	//alert("updateCMCallback");
    	initConceptMap();
        break;
	}
}
/**
 * called when initializing the concept map on load.
 * @param id
 * @param sourceId
 * @param targetId
 */
function createConnection(id, sourceId, targetId){
	var lc = new LabelConnection();
	lc.setId(id);
	var sourceRect  = my_canvas.getFigure(sourceId); //source: Problem
	var targetRect  = my_canvas.getFigure(targetId); //target: DDX
	if(sourceRect!=null && targetRect!=null){
		lc.setSource(sourceRect.getOutputPort(0));
		lc.setTarget(targetRect.getInputPort(0));
		my_canvas.add(lc);	
	}
}
/**
 * a connection is added to the canvas, we save the start- and endpoint and label.
 */
function addConnection(sourcePort, targetPort){
	sendAjax(sourcePort.getParent().id, updateGraph, "addConnection", targetPort.getParent().id);
}

function delConnection(id){
	sendAjax(id, updateGraph, "delConnection", "");
}


function initAddRectangle(prefix, name, containerName){
	$( "#"+name ).draggable({ //add a new hypothesis
		  start: function( event, ui ) {
				 $("#"+name).clone().prependTo($("#"+containerName)); 
			  } , 
			  stop: function( event, ui ) {	
				  this.remove();	
				  var canvasX = ui.offset.left- my_canvas.html.offset().left;
		 		  var canvasY = ui.offset.top - my_canvas.html.offset().top;
		 		  createTempRect(" ", canvasX, canvasY, "cm"+prefix+"_-1");	 
		 		  openListForCM(canvasX,canvasY, "cm"+prefix+"_-1");
			  }
		  });	
}
/**
 * init the drag&drop rectangle for adding a new hypothesis
 */
function initAddHyp(){
	initAddRectangle("ddx", "addhyp", "hypcontainer");
  /*$( "#addhyp" ).draggable({ //add a new hypothesis
	  start: function( event, ui ) {
		 $("#addhyp").clone().prependTo($("#hypcontainer")); 
	  } , 
	  stop: function( event, ui ) {	
		  this.remove();	  
		  var canvasX = ui.offset.left- my_canvas.html.offset().left;
 		  var canvasY = ui.offset.top - my_canvas.html.offset().top;
 		  createTempRect("ddx", canvasX, canvasY, "cmddx_-1");
 		  openListForCM(canvasX,canvasY, "cmddx_-1");
	  }
  });	*/
}


/**
 * init the drag&drop rectangle for adding a new problem
 */
function initAddFind(){initAddRectangle("prob", "addfind", "findcontainer");}

/**
 * init the drag&drop rectangle for adding a new management item
 */
function initAddMng(){ initAddRectangle("mng", "addmng", "mngcontainer");}

/**
 * init the drag&drop rectangle for adding a new diagnostic step
 */
function initAddTest(){ initAddRectangle("ds", "adddiagnstep", "dscontainer");}
function initAddEpi(){ initAddRectangle("epi", "addepi", "epicontainer");}

/**
 * open the list for a new item, that has been dragged to the canvas
 * @param x
 * @param y
 * @param type
 */
function createTempRect(type, x,y,id){
	var rect = createRect(name,"#cccccc"/*"#99CC99"*/,id,"0","1");
	rect.setBackgroundColor("#ffffff");	
	my_canvas.add(rect, x, y);	
	my_canvas.addSelection(rect); //necessary to address it after selection from list!
}

function delTempRect(){
	var newRect = my_canvas.getFigure("cmddx_-1"); 
	if(newRect!=null) my_canvas.remove(newRect);
}

/**
 * we show the div tag with the list and lay it over the rectangle the user has clicked on. 
 * this is a workaround, because I was not able to control the events of the canvas and its childs.
 */
function openListForCM(x,y, clickedId){
	//positioning stuff:
	canvasX = $("#canvas").offset().left;
	canvasY = $("#canvas").offset().top;
	xOfDialog = canvasX + x; 
	yOfDialog = canvasY + y;
	var dialogName = "dialogCMProb";
	var inputFieldName = "cm_prob_sel";
	//getting type of item the user has clicked on: 
	if(clickedId.startsWith("cmddx")){
		dialogName = "dialogCMDDX";	
		inputFieldName = "cm_ddx_sel";
	}
	if(clickedId.startsWith("cmds")){
		dialogName = "dialogCMTest";	
		inputFieldName = "cm_ds_sel";
	}	
	if(clickedId.startsWith("cmmng")){
		dialogName = "dialogCMMng";	
		inputFieldName = "cm_mng_sel";
	}	
	if(clickedId.startsWith("cmepi")){
		dialogName = "dialogCMEpi";	
		inputFieldName = "cm_epi_sel";
	}	
	//display:
	$("#"+dialogName ).show();
	$("#"+inputFieldName).focus();
	$("#"+dialogName ).offset({ top: yOfDialog, left: xOfDialog });
}

/**
 * We would have to update here the 
 * - figure: label, itemid(?), we do not have to change the id of the rectangle 
 * - list: change problem 
 * - connection
 * -> ajax: get problem with of the rectangles id and update the associated Problem object,
 * All ids can stay the same, so we do not have to update connections.
 * @param value
 * @param label
 */
function editOrAddProblemCM(newValue, label){
	var selFigure = my_canvas.getSelection().getPrimary();
	var id = selFigure.id.substring(7);
	if(id=="-1") //a new problem
		sendAjaxCM(newValue, problemCallBackCM, "addProblem", label, selFigure.x, selFigure.y);
	else //change of existing problem
		sendAjax(newValue, problemCallBackCM, "changeProblem", id);
}

function editOrAddEpiCM(newValue, label){
	var selFigure = my_canvas.getSelection().getPrimary();
	var id = selFigure.id.substring(6);
	if(id=="-1") //a new problem
		sendAjaxCM(newValue, epiCallBackCM, "addEpi", label, selFigure.x, selFigure.y);
	else //change of existing problem
		sendAjax(newValue, epiCallBackCM, "changeEpi", id);
}

function editOrAddDDXCM(newValue, label){
	var selFigure = my_canvas.getSelection().getPrimary();
	var id = selFigure.id.substring(6);
	if(id=="-1") //a new problem
		sendAjaxCM(newValue, diagnosisCallBackCM, "addDiagnosis", label, selFigure.x, selFigure.y);
	else //change of existing problem
		sendAjax(newValue, diagnosisCallBackCM, "changeDiagnosis", id);
}

function editOrAddTestCM(newValue, label){
	var selFigure = my_canvas.getSelection().getPrimary();
	var id = selFigure.id.substring(6);
	//alert(id);
	if(id=="-1") //a new problem
		sendAjaxCM(newValue, testCallBackCM, "addTest", label, selFigure.x, selFigure.y);
	else //change of existing problem
		sendAjax(newValue, testCallBackCM, "changeTest", id);
}

function editOrAddMngCM(newValue, label){
	var selFigure = my_canvas.getSelection().getPrimary();
	var id = selFigure.id.substring(6);
	if(id=="-1") //a new problem
		sendAjaxCM(newValue, managementCallBackCM, "addMng", label, selFigure.x, selFigure.y);
	else //change of existing problem
		sendAjax(newValue, managementCallBackCM, "changeMng", id);
}



/** we create a rectangle & label with the basic settings like size, color,... **/
function createRect(name, color, id, exp, learner){
	var rect = new LabelRectangle();
	rect.label.text=name;
	//rect.label.setBackgroundColor("#cccccc");
	//rect.label.setWidth(10);
	rect.color= new draw2d.util.Color(color);
	rect.setId(id);
	if(exp=="1" && learner=="0"){
		rect.setResizeable(false);
		rect.setDeleteable(false);
		//rect.setSelectable(false);
	}
	
	return rect;
}

/* for some reason the setCssClass method is not working properly...*/
function designPort(port){
	port.setBackgroundColor("#cccccc");
	port.setRadius(3);
}

/*
 * learner = 0|1 (0=not added by learner, 1= added by learner)
 * exp = 0|1
 * peer = number of peers that have chosen this
 */
function createAndAddRectangle(name, x, y, id, shortname, type, learner, exp, peer){
	if(shortname!=""){
		lookUpLabels[counter] = name;
		lookUpShortLabels[counter] = shortname;
		counter++;
	}
	else shortname = name;
	var rect=null;
	//we need individual functions here, to be able to adapt the number and types of ports
	if(type=="1") rect = createAndAddFind(name, x, y, id, shortname, learner, exp, peer);
	if(type=="2") rect = createAndAddHyp(name, x, y, id, shortname, learner, exp, peer);
	if(type=="3") rect = createAndAddDiagnStep(name, x, y, id, shortname, learner, exp, peer);
	if(type=="4") rect = createAndAddMng(name, x, y, id, shortname), learner, exp, peer;
	if(type=="6") rect = createAndAddEpi(name, x, y, id, shortname), learner, exp, peer;

	if(rect!=null) my_canvas.add(rect, Number(x), Number(y));		
}

function getColorForRect(exp, learner){
	var color = "#cccccc"; 
	if(expFeedback && exp=="1" && learner=="1") //learner and exp have added this item
		color="#088A29";
	
	return color;
}
/**
 * create a new problem rectangle and add it to the canvas (including label and ports)
 **/
function createAndAddFind(name, x, y, id, shortname,learner, exp, peer){
	if(!expFeedback && learner=="0") return; //we do not display items that have added by expert if expert feedback is off
	var color = getColorForRect(exp, learner);

	var rect = createRect(shortname,color/*"#99CC99"*/,id, exp, learner);//new draw2d.shape.basic.Rectangle();
	 rect.createPort("output", new draw2d.layout.locator.RightLocator());
	 designPort(rect.getOutputPort(0));
	 rect.setBackgroundColor("#ffffff");	 
	 return rect;
}

/**
 * create a new problem rectangle and add it to the canvas (including label and ports)
 **/
function createAndAddEpi(name, x, y, id, shortname,learner, exp, peer){
	if(!expFeedback && learner=="0") return; //we do not display items that have added by expert if expert feedback is off
	var color = getColorForRect(exp, learner);

	var rect = createRect(shortname,color/*"#99CC99"*/,id, exp, learner);//new draw2d.shape.basic.Rectangle();
	 rect.createPort("output", new draw2d.layout.locator.RightLocator());
	 designPort(rect.getOutputPort(0));
	 rect.setBackgroundColor("#ffffff");	 
	 return rect;
}

/**
 * create a new hypothesis rectangle and add it to the canvas (including label and ports)
 **/
function createAndAddHyp(name, x, y, id, shortname,learner, exp, peer){
	if(!expFeedback && learner=="0") return; //we do not display items that have added by expert if expert feedback is off
	var color = getColorForRect(exp, learner);
	var rect = new DDXRectangle();
	rect.label.text=name;
	//rect.label.setBackgroundColor("#cccccc");
	//rect.label.setWidth(10);
	rect.color= new draw2d.util.Color(color);
	rect.setId(id);
	if(exp=="1" && learner=="0"){
		rect.setResizeable(false);
		rect.setDeleteable(false);
		//rect.setSelectable(false);
	}
	//var rect = createRect(shortname, color, id/*,"990000"*/, exp, learner); //new draw2d.shape.basic.Rectangle();
	
	rect.createPort("input");
	rect.createPort("output");
	//rect.getInputPort(0).setCssClass("ports");
	designPort(rect.getInputPort(0)); 
	designPort(rect.getOutputPort(0));
	/*if(name==""){ //then it is a new hyp from drag&drop and we have to attach an editor to select a label:
		var editor = new draw2d.ui.LabelLMEditor();
		rect.label.installEditor(editor);
		editor.start(rect.label);
	}*/
	rect.setBackgroundColor("#ffffff");	
	return rect;
	//initAddHyp(); //we have to re-init this here, because we have created a clone!!!
}

/**
 * create a new diagnostic step rectangle and add it to the canvas (including label and ports)
 **/
function createAndAddDiagnStep(name, x, y, id, shortname,learner, exp, peer){
	if(!expFeedback && learner=="0") return; //we do not display items that have added by expert if expert feedback is off
	var color = getColorForRect(exp, learner);
	var rect = createRect(shortname,color /*"#F6E3CE"*/, id, exp, learner);
	rect.createPort("input");
	designPort(rect.getInputPort(0)); 
	rect.setBackgroundColor("#ffffff");
	return rect;
	//initAddTest();
}

/**
 * create a new management item rectangle and add it to the canvas (including label and ports)
 **/
function createAndAddMng(name, x, y, id, shortname,learner, exp, peer){
	if(!expFeedback && learner=="0") return; //we do not display items that have added by expert if expert feedback is off
	var color = getColorForRect(exp, learner);
	var rect = createRect(shortname,color /*"#FFFF99"*/, id, exp, learner);
	rect.createPort("input");
	designPort(rect.getInputPort(0)); 
	rect.setBackgroundColor("#ffffff");			
	return rect;
}



/** delete item from concept map AND list (except if connection)
 * 
 * */
function deleteItem(idWithPrefix){	
	var idPrefix = idWithPrefix.substring(0,idWithPrefix.indexOf("_"));
	var id = idWithPrefix.substring(idWithPrefix.indexOf("_")+1);
	
	switch (idPrefix) {
    case "cmprob": 
        delProblem(id);
        break;
    case "cmddx": 
    	delDiagnosis(id);
        break;       
    case "cmds": 
    	delTest(id);
        break;   
    case "cmmng": 
    	delManagement(id);
        break;
    case "cmepi": 
    	delEpi(id);
        break;
    case "success": // This is called right after update of HTML DOM.
    	
        break;
	}
}


function getToolTipForRect(element){
	var s = element.html();
	if(element.html().endsWith(".")){
		for(i=0; i<lookUpShortLabels.length;i++){
			if(lookUpShortLabels[i]==element.html())
				return lookUpLabels[i];
		}
	}
	return ""; //element.html(); if tooltip is the same as the label we do not display a tooltip
}

function handleContextMenuRect(rect, key){
	if(!rect.getResizeable()) return; //make sure user cannot change anything in the experts' map.
    switch(key){
    case "mnm":
    	rect.setBackgroundColor('#f3546a');
    	rect.setColor('#f3546a');
        break;
   /* case "green":
        this.setColor('#b9dd69');
        break;*/
    case "final":
    	rect.setColor('#00A8F0');
        break;
    case "delete":
 	   deleteItem(rect.getId());
    default:
        break;
    }
}
