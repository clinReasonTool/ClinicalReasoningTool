package controller;

import java.util.*;

import javax.faces.context.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import beans.error.MyError;
import beans.graph.Box;
import beans.relation.Connection;
import beans.relation.Relation;
import beans.relation.RelationDiagnosis;
import beans.scoring.LearningAnalyticsBean;
import beans.scoring.ScoreBean;
import beans.scripts.PatientIllnessScript;
import beans.user.User;
import database.DBClinReason;
import database.DBLog;
//import net.casus.model.course.Course;
//import net.casus.util.SessionUtility;
import properties.IntlConfiguration;

/**
 * Handles the export of maps into an Excel (xlsx) files and sheets. Currently three sheets are created - (1) basic data of the map,
 * (2) all elements, and (3) all connections
 *
 */
public class ExportController {

	private XSSFWorkbook workbook; 
	private String fileName = null;
	private XSSFCellStyle cellDateStyle;
	
	private void exportTableToExcel(XSSFWorkbook workbook) {
		try {
			FacesContext crtContext = new NavigationController().getCRTFacesContext();
			ExternalContext externalContext = new NavigationController().getCRTFacesContext().getExternalContext();
		    externalContext.setResponseContentType("application/vnd.ms-exc	el");
		    externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\""+fileName+".xlsx\"");
		    
		    workbook.write(externalContext.getResponseOutputStream());
		    crtContext.responseComplete();
		}
		catch(Exception e) {}
	}
	
	 /**
	  * For individual reports all data of one PatientIllnessScript are written into Excel
	 * @param p
	 */
	public void createAndWriteBasicTable(PatientIllnessScript p) {
		 if(p==null) return;
		 List<PatientIllnessScript> maps = new ArrayList<PatientIllnessScript>();
		 maps.add(p);
		 fileName = "mapExport_"+p.getId();
		 createAndWriteBasicTable(maps);
	 }
	 
	 /**
	  * create column headers for the basic data of an PatientIllnessScripts
	 * @param sheet
	 */
	private void createBasicTableColumnHeader(XSSFSheet sheet) {
		 String[] columnHeaders = new String[]{"CASUS user id", "user name", IntlConfiguration.getValue("reports.header.patillsscriptId"), IntlConfiguration.getValue("reports.header.vpname"),  IntlConfiguration.getValue("reports.header.date"),  IntlConfiguration.getValue("reports.header.confidence"), IntlConfiguration.getValue("reports.header.probscore"),IntlConfiguration.getValue("reports.header.ddxscore"), IntlConfiguration.getValue("reports.header.tstscore"), IntlConfiguration.getValue("reports.header.mngscore"), 
					IntlConfiguration.getValue("reports.header.probnum"), IntlConfiguration.getValue("reports.header.ddxnum"),  IntlConfiguration.getValue("reports.header.tstnum"),  IntlConfiguration.getValue("reports.header.mngnum"),  IntlConfiguration.getValue("reports.header.cnxnum"), IntlConfiguration.getValue("reports.header.summstscore"), IntlConfiguration.getValue("reports.header.sumst"),
					/*IntlConfiguration.getValue("reports.header.finalcrd"),*/ IntlConfiguration.getValue("reports.header.error1"),IntlConfiguration.getValue("reports.header.error2"), IntlConfiguration.getValue("reports.header.error3"), IntlConfiguration.getValue("reports.numownfinal"),  IntlConfiguration.getValue("reports.showsol")}; 
		
		 Row headerRow = sheet.createRow(0);
		 for (int i = 0; i < columnHeaders.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(columnHeaders[i]);
		    }
	 }
	
	 /**
	  * create column headers for the basic data of an PatientIllnessScripts
	 * @param sheet
	 */
	private void createCnxTableColumnHeader(XSSFSheet sheet) {
		 String[] columnHeaders = new String[]{"Id", IntlConfiguration.getValue("reports.header.patillsscriptId"), IntlConfiguration.getValue("reports.header.stage"), "start id", "start type", "start name", "target id", "target type", "target name", IntlConfiguration.getValue("authoring.node.weight")}; 
		
		 Row headerRow = sheet.createRow(0);
		 for (int i = 0; i < columnHeaders.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(columnHeaders[i]);
		    }
	 }
	
	 /**
	  * create column headers for the basic data of an PatientIllnessScripts
	 * @param sheet
	 */
	private void createItemsTableColumnHeader(XSSFSheet sheet) {
		 String[] columnHeaders = new String[]{"name", "id", IntlConfiguration.getValue("reports.header.patillsscriptId"), "type", IntlConfiguration.getValue("reports.header.stage"), IntlConfiguration.getValue("reports.header.prefix"), IntlConfiguration.getValue("reports.header.mnm"), IntlConfiguration.getValue("reports.header.ruledout"), IntlConfiguration.getValue("reports.header.workddx"),
				 IntlConfiguration.getValue("reports.header.final") , IntlConfiguration.getValue("reports.header.finalcrd"), "org score", "score"}; 
		
		 Row headerRow = sheet.createRow(0);
		 for (int i = 0; i < columnHeaders.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(columnHeaders[i]);
		    }
	 }
	 
	
	/** retrieves all maps/scripts for all users and VPs in the gi ven course from the database
	 * and writes it into Excel sheets.
	 * @param c
	 */
	/*public void getMapsForCourseAsExcel(Course c) {
		if(c==null) return;
		List maps = ReportController.getInstance().getLearnerScriptsForCourse(c);
		if (maps==null) return;
		createAndWriteBasicTable(maps);
		
	}*/
	
	 /**
	  * Create table with the basic data of each map and each map in a separate row.
	 * @param maps
	 */
	public void createAndWriteBasicTable(List<PatientIllnessScript> maps) {	
		 	if(maps==null || maps.isEmpty()) return;
		 	
		 	initWorkbook();
		    //create columnHeaders:
		    createBasicTableColumnHeader(workbook.getSheetAt(0));
		    createItemsTableColumnHeader(workbook.getSheetAt(1));
		    createCnxTableColumnHeader(workbook.getSheetAt(2));
		   
		    for(int i=0; i<maps.size();i++) { //each map is in one row
		    	int rowIdx = i+1;
		    	PatientIllnessScript p = maps.get(i);
		    	XSSFRow row = workbook.getSheetAt(0).createRow(rowIdx);
		    	createBasicTableRowAndCells(p, row);
		    	createItemsTableRowsAndCells(p, workbook.getSheetAt(1));
		    	createCnxTableRowAndCells(p, workbook.getSheetAt(2));
		    }		    
		    exportTableToExcel(workbook);
		    
	 }
	
	private void initWorkbook() {
	  	workbook = new XSSFWorkbook();
	    workbook.createSheet("Basic data");
	    workbook.createSheet("Elements"); //all elements (findings, ddx,...
	    workbook.createSheet("Connections"); //all elements (findings, ddx,...
		 cellDateStyle  = workbook.createCellStyle();
		cellDateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("MMMM dd, yyyy")); 
	}
	 
	 /**
	  * Add elements of the 4 boxes to the sheet (each in a separate row)
	 * @param p
	 * @param sheetItems
	 */
	private void createItemsTableRowsAndCells(PatientIllnessScript p, XSSFSheet sheetItems) {
		 int rowIdx = 1;

		 	if(p.getBox1Relations()!=null) {
		 	//if(p.getProblems()!=null) {
				for(int i=0;i<p.getBox1Relations().size();i++) {
					 XSSFRow row = sheetItems.createRow(rowIdx);
					 Relation rel = (Relation) p.getBox1Relations().get(i);
					 createCommonCells(rel, p.getBox1().getTitle(), p.getId(), row);
					 row.createCell(5).setCellValue(rel.getPrefix());
					 if(p.getBox1().getBoxType()==Box.BOXTYPE_DDX)
						 addDDXCells(row, rel);
					 rowIdx++;
				}
		 	}
			if(p.getBox2Relations()!=null) {
		 	//if(p.getDiagnoses()!=null) {
				for(int i=0;i<p.getBox2Relations().size();i++) {
					 XSSFRow row = sheetItems.createRow(rowIdx);
					 Relation rel = (Relation) p.getBox2Relations().get(i);
					 createCommonCells(rel, p.getBox2().getTitle(), p.getId(), row);
					 if(p.getBox2().getBoxType()==Box.BOXTYPE_DDX)
						 addDDXCells(row, rel);
					 rowIdx++;
				}
		 	}
		 	
			if(p.getBox3Relations()!=null){
				for(int i=0;i<p.getBox2Relations().size();i++) {
					 XSSFRow row = sheetItems.createRow(rowIdx);
					 Relation rel = (Relation) p.getBox3Relations().get(i);
					 createCommonCells(rel, p.getBox3().getTitle(), p.getId(), row);
					 if(p.getBox3().getBoxType()==Box.BOXTYPE_DDX)
						 addDDXCells(row, rel);
					 rowIdx++;
				}
		 	}
		 	
			if(p.getBox4Relations()!=null){
				for(int i=0;i<p.getBox2Relations().size();i++) {
					 XSSFRow row = sheetItems.createRow(rowIdx);
					 Relation rel = (Relation) p.getBox3Relations().get(i);
					 createCommonCells(rel, p.getBox4().getTitle(), p.getId(), row);
					 if(p.getBox4().getBoxType()==Box.BOXTYPE_DDX)
						 addDDXCells(row, rel);
					 rowIdx++;
				}
		 	}
	 }
	
	/**
	 * if the relation ios a diagnosis of any kind, we add the specific parameters as well.
	 * @param row
	 * @param rel
	 */
	private void addDDXCells(Row row, Relation rel) {
		RelationDiagnosis ddx = (RelationDiagnosis) rel;
		 row.createCell(6).setCellValue(ddx.getMnm());
		 row.createCell(7).setCellValue(ddx.getRuledOut());
		 row.createCell(8).setCellValue(ddx.getWorkingDDX());
		if(ddx.getFinalDiagnosis()>0) {
			row.createCell(9).setCellValue(1);//marker for final
			row.createCell(10).setCellValue(ddx.getFinalDiagnosis()); // stage for setting it final
		}
		else row.createCell(9).setCellValue(0);
	}
	 
	 /**
	  * create the cells that all elements have in common
	 * @param rel
	 * @param type
	 * @param patIllscriptId
	 * @param row
	 */
	private void  createCommonCells(Relation rel, String type, long patIllscriptId,  XSSFRow row) { 		
		LearningAnalyticsBean  analyticsBean = new NavigationController().getCRTFacesContext().getLearningAnalytics();
		//if(analyticsBean==null) analyticsBean = new LearningAnalyticsBean(); //avoid a NullPointerException!
	
		row.createCell(0).setCellValue(rel.getLabelOrSynLabel());
		 row.createCell(1).setCellValue(rel.getId()); //itemId
		 row.createCell(2).setCellValue(patIllscriptId); //patientIllnessScript id 
		 row.createCell(3).setCellValue(IntlConfiguration.getValue(type));
		 row.createCell(4).setCellValue(rel.getStage());
		 if(analyticsBean!=null && analyticsBean.getScoreContainer()!=null && analyticsBean.getScoreContainer().getScoreBeanByTypeAndItemId(rel.getRelationType(), rel.getListItemId())!=null) {
			 ScoreBean sb = analyticsBean.getScoreContainer().getScoreBeanByTypeAndItemId(rel.getRelationType(), rel.getListItemId());
			 row.createCell(11).setCellValue(sb.getOrgScoreBasedOnExp());
			 row.createCell(12).setCellValue(sb.getScoreBasedOnExp());
		 }
	 }
	 
	 /**
	  * Adds the cells to the given row for the basic table of a PatientIllnessScript
	 * @param p
	 * @param row
	 */
	private void createBasicTableRowAndCells(PatientIllnessScript p, XSSFRow row) {
		 	//CAVE: this only works in individual reports!!!
			LearningAnalyticsBean  analyticsBean = new NavigationController().getCRTFacesContext().getLearningAnalytics();
			if(analyticsBean==null) analyticsBean = new LearningAnalyticsBean(); //avoid a NullPointerException!
	    	 //HSSFCell cell = row.createCell(0);
	    	User u = NavigationController.getInstance().getCRTFacesContext().getLearner();
	    	if(u!=null) {
	    		row.createCell(0).setCellValue(u.getExtUserId2());
	    		row.createCell(1).setCellValue("todo");
	    	}
			row.createCell(2).setCellValue(p.getId());
	    	 row.createCell(3).setCellValue(p.getVPName());
	    	if(p.getCreationDate()!=null) {
	    		Cell cell = row.createCell(4);
	    	    cell.setCellValue(p.getCreationDate());
	    	    cell.setCellStyle(cellDateStyle);
	    		//row.createCell(4).setCellValue(p.getCreationDate()); //not sure why this is null???
	    	}
	    	else  row.createCell(4).setCellValue("");
	    	 row.createCell(5).setCellValue(p.getConfidence() + "%");
	    	 row.createCell(6).setCellValue(analyticsBean.getProblemScore());
	    	 row.createCell(7).setCellValue(analyticsBean.getDDXScore());
	    	 row.createCell(8).setCellValue(analyticsBean.getTestScore());
	    	 row.createCell(9).setCellValue(analyticsBean.getMngScore());
			if(p.getBox1Relations()!=null) row.createCell(10).setCellValue(p.getBox1Relations().size());
			else  row.createCell(10).setCellValue(0);
			if(p.getBox2Relations()!=null)  row.createCell(11).setCellValue(p.getBox2Relations().size());
			else  row.createCell(11).setCellValue(0);
			if(p.getBox3Relations()!=null)  row.createCell(12).setCellValue(p.getBox3Relations().size());
			else  row.createCell(12).setCellValue(0);
			if(p.getBox4Relations()!=null)  row.createCell(13).setCellValue(p.getBox4Relations().size());
			else  row.createCell(13).setCellValue(0);			
			if(p.getConns()!=null)  row.createCell(14).setCellValue(p.getConns().size());
			else  row.createCell(14).setCellValue(0);			
			
			if(analyticsBean.getLastSummStScore()!=null)
				row.createCell(15).setCellValue(analyticsBean.getLastSummStScore().getOrgScoreBasedOnExp());
			else  row.createCell(15).setCellValue("");
		
			if(p.getSummSt()!=null)  row.createCell(16).setCellValue(p.getSummSt().getText());
			else  row.createCell(16).setCellValue("");
			//super.addTableCell( new TableCell(p.getStage(), 15, row));
			 if(p.hasError(MyError.TYPE_PREMATURE_CLOUSRE)) row.createCell(17).setCellValue(1);
			 else row.createCell(17).setCellValue(0);
			 if(p.hasError(MyError.TYPE_AVAILABILITY)) row.createCell(18).setCellValue(1);
			 else row.createCell(18).setCellValue(0);
			 if(p.hasError(MyError.TYPE_CONFIRMATION)) row.createCell(19).setCellValue(1);
			 else row.createCell(19).setCellValue(0);		 
			 row.createCell(20).setCellValue(new DBLog().getNumOfFinalDiagnosisAttempts(p.getId()));	
			if(p.getIsShowSolution()) row.createCell(21).setCellValue(1);	
			else row.createCell(21).setCellValue(0);	
	}
	
	 /**
	  * Adds the cells to the given row for a connection,  adds all start and target information (including name/label), stage, 
	  * the weight (including label) etc...
	 * @param p
	 * @param row
	 */
	private void createCnxTableRowAndCells(PatientIllnessScript p, XSSFSheet cnxItems) {
		int rowIdx = 1;
		if(p.getConns()==null || p.getConns().isEmpty()) return;
		Iterator<Connection> it = p.getConns().values().iterator();
		while(it.hasNext()){
			Connection cnx = (Connection) it.next();
				 XSSFRow row = cnxItems.createRow(rowIdx);
		    	 row.createCell(0).setCellValue(cnx.getId());
		    	 row.createCell(1).setCellValue(p.getId());
		    	 row.createCell(2).setCellValue(cnx.getStage());
		    	 row.createCell(3).setCellValue(cnx.getStartId());
		    	 row.createCell(4).setCellValue(cnx.getStartType());
		    	 Relation start = p.getRelationByIdAndType(cnx.getStartId(), cnx.getStartType());
		    	 if(start!=null)  row.createCell(5).setCellValue(start.getLabelOrSynLabel());
		    	 else row.createCell(5).setCellValue("");
		    	 row.createCell(6).setCellValue(cnx.getTargetId());
		    	 row.createCell(7).setCellValue(cnx.getTargetType());
		    	 Relation target = p.getRelationByIdAndType(cnx.getTargetId(), cnx.getTargetType());
		    	 if(start!=null)  row.createCell(8).setCellValue(target.getLabelOrSynLabel());
		    	 else row.createCell(8).setCellValue("");
		    	 row.createCell(9).setCellValue(cnx.getWeight());
		    	 String weightStr = IntlConfiguration.getValue("cnx.relation."+cnx.getWeight());
		    	 if(weightStr!=null && !weightStr.equals("")) 
		    		 row.createCell(9).setCellValue(weightStr + "("+cnx.getWeight()+")");

		    	 rowIdx++;
		}
	}
}
