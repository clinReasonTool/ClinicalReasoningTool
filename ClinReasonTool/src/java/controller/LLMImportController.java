package controller;

import java.io.*;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.*;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;

import actions.beanActions.*;
import beans.list.*;
import beans.relation.*;
import beans.scripts.PatientIllnessScript;
import database.*;
import util.*;
import net.casus.util.io.IOUtilities;
import net.casus.util.request.UnivRequest;
import net.casus.util.table.*;
import net.casus.util.table.converter.*;
import net.casus.util.table.converter.excelimpl.ExcelXLSX2TableConverter;

//import org.apache.myfaces.custom.fileupload.UploadedFile;
@ManagedBean(name = "importbean")
@ViewScoped
public class LLMImportController {
	
	
	private Part file; 
	private Table table; //imported via CASUS code
	private Table cnxTable;
	private List<Relation> uploadedRelations;
	private List<Connection> uploadedConnections;
	
	/**
	 * uploads a xlsx file with two tabs - tab1 contains the concepts and tab 2 named "cnxs" contains the connections. 
	 * @throws Exception
	 */
	public void upload() throws Exception {
		//Scanner s = new Scanner(file.getInputStream());
		//String fileContent = s.useDelimiter("\\A").next();
		//s.close();
		File f = new File("f.xlsx");
		System.out.println("" + f.getAbsolutePath());
	
		IOUtilities.copyStream(file.getInputStream(), new FileOutputStream(f));
		table = Excel2TableConverter.processFileProduction(new UnivRequest(),f.getAbsolutePath());
		
		
		convertTable2Relations();
		//load connections tab:
		cnxTable = ExcelXLSX2TableConverter.processNewStreamProduction(new UnivRequest(),file.getInputStream(), 1, "cnxs");
		convertTable2Cnxs();
		
	}
	
	public void validate(FacesContext context, UIComponent component, Object value) {
		/*Part file = (Part) value;
		if (file.getSize() > 111) {
			throw new ValidatorException(new FacesMessage("File is too large"));
		}
		if (!file.getContentType().equals("text/plain")) 
			throw new ValidatorException(new FacesMessage("File is not a text file"));*/
	}
	
	/*public Table getTable() {
		return table;
	}*/
	
	
	/**
	 * writes the list of relations into the template...
	 * @return
	 */
	public List<Relation> getUploadedrelations() {return uploadedRelations;}
	public List<Connection> getUploadedconnections() {return uploadedConnections;}
	

	/**
	 * import version: 
	 * Stage	Concept	Type	MeSH UID	Must-not-miss (diff. only)	Working diagnosis (diff. only)	Ruled out (stage)	Comment
	 */
	private void convertTable2Relations() {
		
		if(table==null) return;
		for(int i=1;i<table.getRowCount();i++) { 
			List<TableCell> rowElems = table.getRow(i);
			if(rowElems!=null) {
				Object stage = rowElems.get(0).getContent();
				String name = rowElems.get(1).getContent().toString();
				Relation rel = getRelationbyType(rowElems.get(2).getContent().toString());
				String meshUid = ""; 
				if(rowElems.get(3)!=null && rowElems.get(3).getContent()!=null) meshUid = rowElems.get(3).getContent().toString();
				if(rel!=null) {
					if(stage!=null) rel.setStage(Integer.parseInt((String) stage));
					rel.setComment(name);
					ListInterface li = getListItemByNameOrMesUId(meshUid, name);
					if(li!=null) {
						rel.setListItemId(li.getListItemId());
						rel.setComment(name + " -> " + li.getName() + "(" + meshUid + ")");
						rel.setOrder(i); //we use the order as an internal ordering system in order to being able to identify the relations when selected. 
					}
					if(rel instanceof RelationDiagnosis) {
						String mnm = rowElems.get(4).getContent().toString();
						String workingdiagnosis = rowElems.get(5).getContent().toString();
						String ruledOut = rowElems.get(6).getContent().toString();
						if(mnm!=null && mnm.equalsIgnoreCase("yes")) 
							((RelationDiagnosis) rel).setMnm(1);
					}
					addRelation(rel);
				}
				else CRTLogger.out("Relation: " + rowElems.get(2).getContent().toString(), CRTLogger.LEVEL_TEST);
			}
		}	
	}
	
	private void convertTable2Cnxs() {
		if(cnxTable==null) return;
		for(int i=1;i<cnxTable.getRowCount();i++) { 
			List<TableCell> rowElems = cnxTable.getRow(i);
			if(rowElems!=null) {
				Connection conn = new Connection();
				Object stage = rowElems.get(0).getContent();
				//String concept1 = rowElems.get(1).getContent().toString();
				//String concept2 = rowElems.get(2).getContent().toString();
				conn.setConcept1(rowElems.get(1).getContent().toString());
				conn.setConcept2(rowElems.get(2).getContent().toString());
				if(stage!=null)
					conn.setStage(Integer.parseInt((String) stage));
				conn.setOrder(i-1);				
				addConn(conn);		
			}
		}
	}
		
	
	private void addRelation(Relation rel) {
		if(uploadedRelations==null) uploadedRelations = new ArrayList<Relation>();
		uploadedRelations.add(rel);
	}
	
	private void addConn(Connection c) {
		if(uploadedConnections==null) uploadedConnections = new ArrayList<Connection>();
		uploadedConnections.add(c);
	}
	
	/**
	 * Depending on the type specified in each row we create the appropriate Relation object
	 * @param type
	 * @return
	 */
	private Relation getRelationbyType(String type) {
		if(type==null) return null;
		if(type.equalsIgnoreCase("Finding") || type.equalsIgnoreCase("Risk factor")) return new RelationProblem();
		if(type.toLowerCase().contains("diagnosis")) return new RelationDiagnosis();
		if(type.equalsIgnoreCase("Test")) return new RelationTest();
		if(type.equalsIgnoreCase("Treatment")) return new RelationManagement();
		
		else return null; //TODO we need a defaul object here and let user decide which type it should be! 
	}
	
	private ListInterface getListItemByNameOrMesUId(String meshUiD, String name) {
		ListInterface li = new DBList().selectListItemByLangAndLabel("en", name);
		if(li!=null) return li;
		if(meshUiD!=null && !meshUiD.equalsIgnoreCase("")) 
			return new DBList().selectListItemByMeshIdAndLang(meshUiD, "en");		
		
		return null;
	}

	public Part getFile() {return file;}
	public void setFile(Part file) {this.file = file;} 
	
	/**
	 * We add the selected relations and connections to the PatientIllnessScript
	 */
	public void addSelectedRelations() {
		AjaxController ac = new AjaxController();
		PatientIllnessScript pis = NavigationController.getInstance().getAdminFacesContext().getPatillscript();
		//TODO if pis is null create it? 
		
		int[] orderNos = new int[]{0,0,0,0,0};
		if(uploadedRelations!=null) {
			for (int i=0; i<uploadedRelations.size();i++) {
				Relation rel = uploadedRelations.get(i);
				//of the checkbox for the given relation is set on "on" we add it to the patIllnessscript of the VP
				if(ac.getRequestParamByKey("rel_" + rel.getOrder())!=null && ac.getRequestParamByKey("rel_" + rel.getOrder()).equalsIgnoreCase("on")){
					boolean isAdded = saveRelation(rel, orderNos[rel.getRelationType()], pis.getId());
					if(isAdded) orderNos[rel.getRelationType()]++; 
				}
			}
		}	
		
		if(uploadedConnections!=null) {
			for (int i=0; i<uploadedConnections.size();i++) {
				Connection cnx = uploadedConnections.get(i);
				if(ac.getRequestParamByKey("conn_" + i)!=null && ac.getRequestParamByKey("conn_" + i).equalsIgnoreCase("on")){
					Relation source =  getRelationByName(cnx.getConcept1());
					Relation target = getRelationByName(cnx.getConcept2());
					if(target!=null & source!=null) {
						cnx.setStartId(source.getId());
						cnx.setTargetId(target.getId());
						cnx.setIllScriptId(pis.getId());
						cnx.setStartType(source.getRelationType());
						cnx.setTargetType(target.getRelationType());
						cnx.setWeight(Connection.WEIGHT_SOMEWHAT_RELATED);
						cnx.setStartEpIdx(1);
						new DBClinReason().saveAndCommit(cnx);
					}
				}
			}
		}
		
		//reload patientIllnessScript
		NavigationController.getInstance().getAdminFacesContext().setPatillscript(new DBClinReason().selectPatIllScriptById(pis.getId()));
	}
	
	/**
	 * We look for each connection whether both concepts are part of the map
	 * @param name
	 * @return
	 */
	private Relation getRelationByName(String concept) {
		if(uploadedRelations==null) return null; 
		for(int i=0; i<uploadedRelations.size(); i++) {
			Relation rel = uploadedRelations.get(i);
			if(rel.getComment()!=null && rel.getComment().contains(concept) && rel.getId()>0)
				return rel;
		}
		return null;
	}
	
	private boolean saveRelation(Relation rel, int orderNr, long pisId) {
		rel.setDestId(pisId);
		rel.setOrder(orderNr);
		rel.calculatePoints(orderNr, true);
		new DBClinReason().saveAndCommit(rel);
		
		return true;
	}
}
