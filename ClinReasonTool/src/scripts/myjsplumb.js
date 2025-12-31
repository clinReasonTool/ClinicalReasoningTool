var dynamicAnchors = [ "Left",  "Right" ];

var ep_right_prefix = "1_"; //e.g. 1_fdg_12345
var ep_left_prefix = "2_";	
var ep_top_prefix = "3_";
var ep_bottom_prefix = "4_";

//var groups = new Array("fdg_group", "ddx_group","tst_group", "mng_group", "sum_group", "pat_group", "nddx_group", "naim_group", "nmng_group", "info_group", "mhyp_group", "mrec_group", "mmng_group", "mfdg_group" );
var groups = new Array("box1_group", "box2_group","box3_group", "box4_group", "sum_group");

var boxes = new Array("box1", "box2", "box3", "box4");
/*
 * TODO not very elegant, but the "" vs non "" is important and seems to be difficult to do when getting the items/ids from 
 * an array....
 * all groups are created and all items attached to a group
 */
function initGroups(){
	//boxes = createBoxesArr();
	for(var i=0; i<boxes.length;i++){
		instance.addGroup({
	        el: document.getElementById(boxes[i]),
	        id: groups[i], //getGroupIdByElem(boxes[i]),
	        constrain:true,
	        dropOverride:true,
	        droppable:false,
	        draggable:false //remove if boxes shall be draggable again!!!
	    });
	}
	initElems("");
}

/**
elemName e.g. fdg_box, pat_box, nddx_box
not needed for new boxes
 */
/*function getGroupIdByElem(elemName){
	if(elemName==null || elemName =="") return;
	for(var i=0; i<groups.length;i++ ){
		var groupPrefix = groups[i].substring(0,groups[i].indexOf("_"));
		if(elemName.startsWith(groupPrefix)) 
		return groups[i];
	}
	
}*/

function registerItemEvents(itemId, item) {
	console.log( "regTabEvents itemId: " +  itemId );
	instance.on(item, "dbltap", function( index ) {
		// show toolbar?
	    console.log( "initElems .itembox each tap " +  itemId );
	});
	instance.on(item, "dbltap", function( index ) {
		console.log( "initElems .itembox each dbltap " +  itemId );
		showDropDown("dd" + itemId,itemId);
	});
	instance.on(item, "contextmenu", function( index ) {
		// did not receive this kind of event ever?
		console.log( "initElems .itembox each contextmenu " +  itemId );
	});
	
}

function initElems(box){
	var selector = box;
	console.log( "initElems selector: " +  selector );
	for(var i=0; i<item_arr.length;i++){
		var itemId = item_arr[i];
		console.log( "initElems loop itemId: " +  itemId );
		if(selector==""|| itemId.startsWith(selector)){
			var item = $("#"+itemId)[0];
			if (!isView) addToGroup(itemId, item);
			//this makes each node a target for connections, independent from the endpoint:
			instance.makeTarget(item,{
				isTarget:true,
				maxConnections:10,			
				anchor: ["Perimeter", { shape:"Rectangle" }]
			});
			createEndpointsForItems(itemId);
			registerItemEvents(itemId, item);
	    	 
		}
		 
	}
	for(var i=0; i<exp_arr.length;i++){
		var itemId = exp_arr[i];
		if(selector==""|| itemId.startsWith(selector)){
			var item = $("#"+itemId)[0];
			addToGroup(itemId, item);
		}
	}
	for(var i=0; i<pass_arr.length;i++){
		var itemId = pass_arr[i];
		if(selector==""|| itemId.startsWith(selector)){
			var item = $("#"+itemId)[0];
			if (!isView) addToGroup(itemId, item);
			//this makes each node a target for connections, independent from the endpoint:
			instance.makeTarget(item,{
				isTarget:true,
				maxConnections:10,			
				anchor: ["Perimeter", { shape:"Rectangle" }]
			});
			createEndpointsForItems(itemId);
		}
	}
	initDraggables();
} 


/*all boxes have 2 endpoints (left and right) if we are on the view mode we use the expEndpoints to avoid any connection making*/
function createEndpointsForItems(itemId){	
	var eps = instance.getEndpoints(itemId);
	if(eps!=undefined && eps.length>0) return; //do not create endpoints again if they are already there, e.g. for boxes added by learner & expert	
		var ep;
		if(isView) ep  = instance.addEndpoint(itemId, { anchor:"LeftMiddle" }, expendpoint);
		else  ep = instance.addEndpoint(itemId, { anchor:"LeftMiddle" }, endpointCL);
		ep.id = ep_left_prefix + itemId;
		ep.maxConnections = 10;

		var ep2;
		if(isView) ep2 = instance.addEndpoint(itemId, { anchor:"RightMiddle" }, expendpoint);
		else ep2 = instance.addEndpoint(itemId, { anchor:"RightMiddle" }, endpointCR);
		ep2.id = ep_right_prefix + itemId;
	    ep2.maxConnections = 10;
}

//var lookup = new Array("", "fdg_box", "ddx_box", "tst_box", "mng_box", "sum_box", "pat_box", "nddx_box", "naim_box", "nmng_box", "info_box", "mfdg_box", "mhyp_box", "mmng_box", "mrec_box");

/**
 * we have to create the boxes array dynamically now
 */
/*function createBoxesArr(){
	var boxesArr = new Array("box1", "ddx_box", "tst_box", "mng_box", "sum_box");	
	boxesArr[0] = lookup[box1Type];
	boxesArr[1] = lookup[box2Type];
	boxesArr[2] = lookup[box3Type];
	boxesArr[3] = lookup[box4Type];
	return boxesArr;
}*/

function createExpEndpointsForItems(itemId){	
	var eps = instance.getEndpoints(itemId);
	if(eps!=undefined && eps.length>0) return; ///do not create endpoints again if they are already there, e.g. for boxes added by learner & expert	
	var ep = instance.addEndpoint(itemId, { anchor:"LeftMiddle" }, expendpoint);
	ep.id = ep_left_prefix + itemId;
	var ep2 = instance.addEndpoint(itemId, { anchor:"RightMiddle" }, expendpoint);
	ep2.id = ep_right_prefix + itemId;
}
/*
 * called after an item has been added or deleted (and after clicking the hidden button)
 */
/*function updateItemCallback(data, items, box){ 
	 var status = data.status; // Can be "begin", "complete" or "success".
	if(isCallbackStatusSuccess(data)){
		initElems(items);
		initBoxHeights();
	}   
}
*/

/*
 * attach an item to a group and make it a target
 */
function addToGroup(itemId, item){ 
	try{
		//!!!CAVE: this is with indexOf, so we need to make sure that the more specific ones are checked first - this is ugls and needs to be changed in the future!!!!
		if(itemId.indexOf("box1")>=0){
			instance.addToGroup("box1_group", item);
			return;
		}
		if(itemId.indexOf("box2")>=0){
			instance.addToGroup("box2_group", item);
			return;
			}
		if(itemId.indexOf("box3")>=0){
			instance.addToGroup("box3_group", item);
			return;
		}
		if(itemId.indexOf("box4")>=0){
			instance.addToGroup("box4_group", item);
			return;
		}
		/*if(itemId.indexOf("fdg")>=0){
			instance.addToGroup("fdg_group", item);
			return;
		}
		if(itemId.indexOf("ddx")>=0){
			instance.addToGroup("ddx_group", item);
			return;
		}
		if(itemId.indexOf("tst")>=0){
			instance.addToGroup("tst_group", item);
			return;
		}
		if(itemId.indexOf("mng")>=0){
			instance.addToGroup("mng_group", item);
			return;
		}
		
		if(itemId.indexOf("pat")>=0){
			instance.addToGroup("pat_group", item);
			return;
		}
		if(itemId.indexOf("info")>=0){
			instance.addToGroup("info_group", item);
			return;
		}
		if(itemId.indexOf("naim")>=0){
			instance.addToGroup("naim_group", item);
			return;
		}
						
		if(itemId.indexOf("mhyp")>=0){
			instance.addToGroup("mhyp_group", item);
			return;
		}

		if(itemId.indexOf("mrec")>=0){
			instance.addToGroup("mrec_group", item);
			return;
		}*/
	}
	catch(err){
		var x = err;
	}
	
}


/*
 * an item is moved, so we store the new position
 */
function handleRectDrop(ui){
	//if((xDragStart>-1 && xDragStart>-1) && rect.x >= xDragStart+10 || rect.x <= xDragStart-10 || rect.y >= yDragStart+10 || rect.y <= yDragStart-10){
		//var position = ui.position;
		var x = ui.position.left;
		var y = ui.position.top;
		var id = $(ui.helper).attr("id");
		var box = id.substring(3,4); //we get the number of the box
		  //after drag&drop we have to re-hide the connections if they are turned off:
		initCnxDisplay();	  
		sendAjaxCM(id, doNothing, "moveItemBox"+box, name, x, y, box);
}




function toggleContainerCollapse(elem){
    var g = elem.getAttribute("group"), collapsed = instance.hasClass(elem, "collapsed");
    instance[collapsed ? "removeClass" : "addClass"](elem, "collapsed");
    instance[collapsed ? "expandGroup" : "collapseGroup"](g);
    toggleStoredContainerCollapsed(elem.id);
}

var conns = '';

/** we parse the JSON string of connections and store the connections in the conn variable 
 * We do this quite at the beginning to be able to access the conn variable when initializing the endpoints
**/
function parseConns(){
	if($("#jsonConns").html()!=null && $("#jsonConns").html()!='' && $("#jsonConns").html()!=undefined)
		conns = jQuery.parseJSON($("#jsonConns").html());
}
/**
 * On load we draw the connections between the items.
 **/
function initConnections(){	
	
	/*if($("#jsonConns").html()!=null && $("#jsonConns").html()!='' && $("#jsonConns").html()!=undefined)
		conns = jQuery.parseJSON($("#jsonConns").html());*/
	
	if(conns!=''){
		for(j=0; j<conns.length;j++){
			createConnection(conns[j].id, conns[j].sourceid, conns[j].targetid, conns[j].l, conns[j].e,  conns[j].weight_e,  conns[j].weight_l, conns[j].start_ep, conns[j].target_ep, conns[j].target_x, conns[j].target_y, conns[j].stage);
		}
	}
}


/*
 * init positions of all containers....
 */
/*function initContainerPos(){
	initOneContainerPos("fdg", "fdg_box");
	//initOneContainerPos("pat", "pat_box");
	initOneContainerPos("ddx", "ddx_box");
	initOneContainerPos("tst", "tst_box");
	initOneContainerPos("mng", "mng_box");
	initOneContainerPos("sum", "sum_box");
}*/

/*function initOneContainerPos(type, box){
	var x = getContainerX(type); 
	var y = getContainerY(type); 
	$("#"+box).offset({ top: getContainerY(type), left: getContainerX(type) });
}*/

/*
 * init collapse status of all containers....
 */
/*function initContainerCollapsed(){
	initOneContainerCollapsed("fdg", "fdg_box");
	//initOneContainerCollapsed("pat", "pat_box");
	initOneContainerCollapsed("ddx", "ddx_box");
	initOneContainerCollapsed("tst", "tst_box");
	initOneContainerCollapsed("mng", "mng_box");
	initOneContainerCollapsed("sum", "sum_box");
	initOneContainerCollapsed("pat", "pat_box");
}*/
/**
 * 
 * @param type e.g. "fdg"
 * @param box e.g. "fdg_box"
 */
/*function initOneContainerCollapsed(type, box){ 
	var isCollapsed = getContainerCollapsed(type); 
	if(!isCollapsed || isCollapsed=="false") return; //nothing to do...
	$("#"+box).addClass("collapsed");
	$("#fdg_box").addClass("jsplumb-group-collapsed");
}*/

function deleteEndpoints(id){
	var ep = instance.getEndpoints(id);
	if(ep!=undefined && ep.length>0){
		for(var i=0; i<ep.length;i++){
			instance.deleteEndpoint(ep[i]);
		}
	}
}

function initBoxHeights(){
	var storedHeightRow1 = getBoxRow1Height();//getBoxFdgDDXHeight();
	var storedHeightRow2 = getBoxRow2Height();//getBoxTstMngHeight();
	var maxYRow1 = 210; //height of fdg & ddx containers
	var maxYRow2 = 210; //height of tst & mng containers
	
	var itemArr2 = $(".row1");
	if(itemArr2.length==0) return;
	var isLastElemExpItem = false;
	for(i=0; i<itemArr2.length; i++){
		var myY = $(itemArr2[i]).position().top + 30;
		if(myY > maxYRow1){
			maxYRow1 = myY;
			if($(itemArr2[i]).hasClass("expbox")){
				isLastElemExpItem = true;
			}
			else isLastElemExpItem = false;
		}
	}
	//if the last element (lowest) is an expert item, we have to add some more pixels to the box, because
	//otherwise the lowest expBox lays over the submit button!
	if(isLastElemExpItem==true){
		maxYRow1 += 30; 
	}
	var itemArr3 = $(".row2");
	if(itemArr3.length!=0){ //return;
		for(i=0; i<itemArr3.length; i++){
			var myY = $(itemArr3[i]).position().top + 30;
			if(myY > maxYRow2) maxYRow2 = myY;
		}	
	}
	if(maxYRow1>storedHeightRow1){
		storedHeightRow1 = maxYRow1;
		setBoxHeight(1, maxYRow1);
	}
	
	if(maxYRow2>storedHeightRow2){
		storedHeightRow2 = maxYRow2;
		setBoxHeight(3, maxYRow2);
	}
	/*$("#fdg_box").height(storedHeightRow1);
	$("#ddx_box").height(storedHeightRow1);

	$("#tst_box").height(maxYRow2);
	$("#mng_box").height(maxYRow2);*/
	$(".pos_1").height(storedHeightRow1);
	$(".pos_2").height(storedHeightRow1);

	$(".pos_3").height(storedHeightRow2);
	$(".pos_4").height(storedHeightRow2);
	
	sendNewHeightToHostSystem();

}

function sendNewHeightToHostSystem(){
	var rowsHeight = $(".pos_1").height() + $(".pos_3").height(); //$("#fdg_box").height() + $("#mng_box").height();
	//var minHeight = 520;
	var newFrameHeight = 730;
	if((rowsHeight-520)>0) newFrameHeight += rowsHeight-520;
	postFrameHeight(newFrameHeight);			
}

/**
 * we display the connection hint close to the first element we see. The arrow should point onto the endpoint of 
 * this element. Whether the box shall be displayed or not is handled server-side (only then the display box has the  
 * css class cnxhint_true).
 * @deprecated
 */
function checkDisplayCnxHint(){
	var item = $(".itembox")[0];	
	if(item===undefined) return;//important, otherwise we cause trouble for the page initialization!
	var posLeft = item.offsetLeft + item.offsetParent.offsetLeft + item.offsetWidth + 20;
	var posTop = item.offsetTop  + item.offsetParent.offsetTop + item.offsetHeight;
	
	$(".cnxhint_true").css({top: posTop+'px', left: posLeft+'px'});
	$(".cnxhint_true").hide();
	$(".cnxhint_true").toggle("drop", {direction: "right"},1000);
}

function initDraggables(){
	if(isView) return;
    instance.draggable(jsPlumb.getSelector(".drag-drop .itembox"));   
    
	//item was moved -> trigger ajax call to update position
    $( ".drag-drop .itembox" ).draggable({
    	  stop: function( event, ui ) {
    		  handleRectDrop(ui);
    	  },	
        start: function(event, ui) {             
            // $(ui.helper).find('.tooltip').hide(); 
             $('.ui-tooltip').remove();
         }, 
    
    });
}

