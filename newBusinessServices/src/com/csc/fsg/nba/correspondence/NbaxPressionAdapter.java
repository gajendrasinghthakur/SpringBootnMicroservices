package com.csc.fsg.nba.correspondence;

/*
 * ************************************************************** <BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.csc.fsg.nba.correspondence.docprintschema.DocPrintForms;
import com.csc.fsg.nba.correspondence.docprintschema.DocPrintVariables;
import com.csc.fsg.nba.correspondence.docprintschema.Fields;
import com.csc.fsg.nba.correspondence.docprintschema.Forms;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaCorrespondenceRequestVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/** 
 * This class provides the correspondence adapter implementation for Document Sciences' xPression.
 * Basically it encapsulates all low level details involved while interacting with xPression.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Integration</td></tr>
 * <tr><td>SPR3004</td><td>Version 6</td><td>Invalid xPression user id for letter name retrieval</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>SPR3337</td><td>Version 7</td><td>PDF data for letters is not stored in the workflow system</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Correspondence</td></tr> 
 * <tr><td>ALS4442</td><td>AXA Life Phase 1</td><td>#QC2878 - Change Request for nbA-Correspondence XPressions Change</td></tr>
 * <tr><td>ALS4434		Correspondence payload needs to be scrubbed for special characters  </td></tr>  
 * <tr><td>AXAL3.7.10A</td><td>AXA Life Phase 2</td><td>Automatic Reinsurance UI</td></tr>
 * <tr><td>CR58636</td><td>Discretionary</td><td>ADC Retrofit</td></tr> 
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaxPressionAdapter extends NbaCorrespondenceAdapterBase {
	public final static String BATCH_PRINT_REQUEST = "getBatchPrint";
	public final static String CATEGORY_REQUEST = "getListOfCategories";    //AXAL3.7.13I
	public final static String LETTER_REQUEST = "getListOfDocuments";		//AXAL3.7.13I
	public final static String PDF_REQUEST = "previewPDF";					//AXAL3.7.13I
	public final static String VARIABLE_REQUEST = "getDocumentVariables";	//AXAL3.7.13I
	public final static String PDF_OPERATION = "requestDocumentsWithData";	//AXAL3.7.13I
	public final static String PDF_KEY_CONTRACTNUMBER = "CONTRACTNUMBER";	//AXAL3.7.13I
	
	protected final static String DATA_TABLE = "DATA";
	protected final static String STATE = "HOMSTATE";
	protected final static String CATEGORY_ID = "Correspondence";
	
	protected java.lang.String documentName;
	protected java.lang.String extract;
	//AXAL3.7.32 moved to super class.
	
	private String policyNumber;				//AXAL3.7.13I
	private boolean fromEventFlage = false;		//AXAL3.7.13I
	private NbaCorrespondenceRequestVO corrRequestVO;
	
	
/**
 * This constructor should never be used outside the current package.
 */
protected NbaxPressionAdapter() {
    super();
}
/**
 * This method returns the extract used for Letter generation.
 * @return java.lang.String
 */
public String createExtract(String strLetterName) throws NbaBaseException {
	
	if (getExtract() == null || getExtract().length() == 0) {
		createExtrFileStream(strLetterName);
	}	
    return getExtract();
}
/**
 * This method creates the variable data for a letter  
 * @param strLetterName Name of the Letter which needs to be created
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever an error occurs. 
 */
private void createExtrFileStream(String strLetterName) throws NbaBaseException {
	if (strLetterName == null || strLetterName.length() == 0) {
		setLetterType(NbaCorrespondenceUtils.LETTER_EVENTDRIVEN);
		strLetterName = getLetterNameFromSource();
		setDocumentName(strLetterName);	
	}else{
		setDocumentName(strLetterName);
		if(getLetterType() == null || getLetterType().length() == 0) {//AXAL3.7.32
			setLetterType(NbaCorrespondenceUtils.LETTER_ONDEMAND);	
		}
	}

	Fields fields = null;
	String response = null;
	String userName = null;
	String password = null;
	try {
		// get the userid and password configured in nbaconfiguration.xml
		if ( isFromEventFlage()){
			userName = NbaConfiguration.getInstance().getCorrespondence().getEventDrivenAt(0).getEventDrivenUserName();
			password = NbaConfiguration.getInstance().getCorrespondence().getOnDemandAt(0).getOnDemandPassword();
		}else {
			userName = NbaConfiguration.getInstance().getCorrespondence().getOnDemandAt(0).getOnDemandUserName();
			password = NbaConfiguration.getInstance().getCorrespondence().getOnDemandAt(0).getOnDemandPassword();
		}
		NbaUserVO nbaUserVO = getNbaUserVO(userName,password);
		//put category name into Map 
		Map parameterMap = new HashMap();
		parameterMap.put("docName", strLetterName);
		
		//invoke the webservices
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CORRESPONDENCE_GETVARIABLES, nbaUserVO,
				null, null, parameterMap);
		response = (String) webServiceInvoker.execute();
		fields = DocPrintVariables.unmarshal(new ByteArrayInputStream(response.getBytes())).getImage().getFields();	
	} catch (Exception e) {
		getLogger(this.getClass().getName()).logError(e);////AXAL3.7.54
		throw new NbaBaseException("[Exception = " + e.getMessage()+"][Response = " + response + "]");////AXAL3.7.54
	}
	
	NbaCorrespondenceExtract extract = new NbaCorrespondenceExtract();
	initializeOinkAccess(getUser()); //Set the contract information necessary for oink to work on    NBA146
	// SPR3290 code deleted
	String prefix = null;
	setImagetoExtract(extract); // CR58636 ADC Retrofit
	boolean debugLogging = getLogger(this.getClass().getName()).isDebugEnabled(); 
	int j = 0;
	ArrayList variableList = new ArrayList();
	int fieldNameSize = fields.getName().size(); // SPR3290
	String aVariable = null; // SPR3290
	StringTokenizer tokens = null; // SPR3290
		for (int i = 0; i < fieldNameSize; i++) {
			aVariable = fields.getName().get(i).toString(); // SPR3290
			// Strip off the table name sent by the correspondence system
			tokens = new StringTokenizer(aVariable, "."); // SPR3290
			if (tokens.countTokens() >= 2) {
				prefix = tokens.nextToken();
				aVariable = tokens.nextToken();
			} else {
				aVariable = tokens.nextToken();
			}
			variableList.add(aVariable);
			try { // handle invalid XML tag exceptions
					// AXAL3.7.10A removed the if condition
					// ALS4442 begin
					// System.out.println("aVariable for setting value  >> "+aVariable);
					// APSL5200 : Start
				if (NbaConstants.DISPUNDID.equalsIgnoreCase(aVariable)) {// ALS4779
					extract.setVariableValue(aVariable, getUser().getUserID());
				} else if ("EMAILADDRESS_TO".equalsIgnoreCase(aVariable) && !NbaUtils.isBlankOrNull(getCorrRequestVO().getMailTO())) {
					extract.setVariableValue(aVariable, getCorrRequestVO().getMailTO());
				} else if ("EMAILADDRESS_CC".equalsIgnoreCase(aVariable) && !NbaUtils.isBlankOrNull(getCorrRequestVO().getMailCC())) {
					extract.setVariableValue(aVariable, getCorrRequestVO().getMailCC());
				} else {
					extract.setVariableValue(aVariable, resolveVariableValues(aVariable));// AXAL3.7.10A
				} // ALS4442 end
					// APSL5200 : End
				if (debugLogging) { // NBA027
					getLogger(this.getClass().getName()).logDebug(aVariable + " : " + extract.getVariableValue(j));
					j++;
				}
			} catch (NbaBaseException e) { // do not throw these exceptions
				getLogger(this.getClass().getName()).logError(e.getMessage());
			}
		}
	
	Map resolvedVariables = getOtherOINKVariables(variableList);   
	
	updateExtract(extract,resolvedVariables);
	
	//At this time the nbaDst always points to a CASE
	extract.setCompany(nbaDst.getNbaLob().getCompany());
	extract.setLetter(strLetterName);
	extract.setLetterType(getLetterType());
	extract.setLob(nbaDst.getNbaLob().getPlan());
	extract.setPolicyNumber(nbaDst.getNbaLob().getPolicyNumber());
	setPolicyNumber(nbaDst.getNbaLob().getPolicyNumber());			//AXAL3.7.13I - set policy number to be used as keys for previewPDF service
	extract.setEffectiveDate(NbaUtils.getStringInISOFormatFromDate(nbaDst.getCreateDate()));
	extract.setLanguage(NbaConfiguration.getInstance().getXPression().getLanguage());
	setExtract(extract.marshal());
		//Begin ALS4434
	if(!isFromEventFlage())//IF ON DEMAND LETTER 
	{
		String extractStr = null;
		if(getExtract().indexOf("'") > NbaOliConstants.OLI_TC_NULL){
			extractStr = getExtract().replaceAll("'", "~");
		}
		if(extractStr != null){
			setExtract(extractStr);	
		}
	}
	//End ALS4434
	if (debugLogging) { 
		getLogger(this.getClass().getName()).logDebug(getExtract());
	}
}
	
	/**
	 * @param life
	 * @throws NbaBaseException
	 */
	private HashMap getOtherOINKVariables(ArrayList variableList) throws NbaBaseException {
		HashMap resolvedVarialbes = new HashMap();
		//ALS4476 begin
		HashMap obj = new HashMap();
		obj.put(AXACorrespondenceConstants.PARENT_DST,getParentDst());
		AXACorrespondenceProcessorBase processor = AxaCorrespondenceProcessorFactory.createCorrespondenceProcessorRequestor(getDocumentName(),getUser(), doHoldingInquiry(), getNbaDst() , obj);
		//ALS4476 - end
		if (processor !=null ){
			resolvedVarialbes = (HashMap) processor.resolveVariables(variableList);
		}
		return resolvedVarialbes ;
	}
	
	private void updateExtract(NbaCorrespondenceExtract corrExtract, Map valueMap ){
		// get the key set and iterator on that 
		Iterator iterator  = valueMap.keySet().iterator();
		while( iterator.hasNext() ){
			String variableKey = (String) iterator.next();
			String variableValue = (String) valueMap.get(variableKey);
			if (! NbaUtils.isBlankOrNull(variableValue)){
				//Begin AXAL3.7.32 If the value is already present in the extract, replace it.
				if(corrExtract.hasVariable(variableKey)) {
					corrExtract.removeVariable(variableKey);
				}
				//End AXAL3.7.32
				corrExtract.setVariableValue(variableKey, variableValue);		
			}
		}
	}
/**
 * This method frees up all resources used by the correspondence system.
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever an error occurs.
 */
public void freeResources() throws NbaBaseException {
}

/**
 * This method returns the category list used for Letter generation.
 * @return java.util.List List of Categories
 */
public List getCategoryNames() throws NbaBaseException {
	Forms forms= null;
	try {
		String userName = NbaConfiguration.getInstance().getCorrespondence().getOnDemandAt(0).getOnDemandUserName();
		String password = NbaConfiguration.getInstance().getCorrespondence().getOnDemandAt(0).getOnDemandPassword();
		NbaUserVO nbaUserVO = getNbaUserVO(userName,password);
		//invoke the webservices
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CORRESPONDENCE_GETCATEGORIES, nbaUserVO,
				null, null, null);
		String response = (String) webServiceInvoker.execute();
		forms = DocPrintForms.unmarshal(new ByteArrayInputStream(response.getBytes())).getForms();	
	} catch (Exception e) {
		if (e instanceof NbaBaseException)
			throw (NbaBaseException)e;
	    throw new NbaBaseException("Error in :" + this.getClass().getName(), e);
	}
	// forms will not be null because an exception will be thrown above if it cannot be instantiated
	int nameCount = forms.getNameAndDescAndCategoryAndDefaultDataSourceNameCount(); // SPR3290
	List info = new ArrayList(nameCount); // SPR3290

	for (int i = 0; i < nameCount; i++) { // SPR3290
			info.add(forms.getNameAndDescAndCategoryAndDefaultDataSourceNameAt(i).getCategory());
	}

	return info;
}

/**
 * @return
 * @throws NbaBaseException
 */
private NbaUserVO getNbaUserVO(String uName, String uPwd) throws NbaBaseException {
	NbaUserVO nbaUserVO = new NbaUserVO();
	nbaUserVO.setUserID(uName);
	nbaUserVO.setPassword(uPwd);
	Map tokens = new HashMap();
	tokens.put(NbaUserVO.EIB_TOKEN, getUser().getToken());
	nbaUserVO.setTokens(tokens);
	return nbaUserVO;
}
/**
 * This method returns the document name
 * @return java.lang.String Document Name
 */
private java.lang.String getDocumentName() {
	return documentName;
}

/**
 * This method returns the extract data used for Letter generation.
 * @return java.lang.String
 */
public String getExtract() {
	
	return extract;
}
/**
 * This method returns the Letter in HTML format, which basically represents a preview of the letter.
 * Invalid action for xPression
 * @return byte[]
 * @param strLetterName java.lang.String
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever an error occurs.
 */
public byte[] getLetterAsHTML(String strLetterName) throws NbaBaseException {		
	throw new NbaBaseException("HTML request is invalid for " + this.getClass().getName());
}

/**
 * This method returns the Letter in PDF format. The PDF letter generation request requires
 * the name of the letter to be generated for on demand letters, and the variable XML of the changed letter contents 
 * @return byte[]
 * @param strLetterName Name of the letter to be gererated.
 * @param strChangedXml Change letter contents obtained from the HTML preview of the letter
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever an error occurs. 
 */
public byte[] getLetterAsPDF(String strLetterName, String strChangedXml) throws NbaBaseException {
	setFromEventFlage(true);
	String response = null;//AXAL3.7.54
	if (getExtract() == null || getExtract().length() == 0) {
		createExtrFileStream(strLetterName);
	}else{ 
		setDocumentName(strLetterName);
	}
	//SPR3337 code deleted
	try{
		HashMap keysMap = new HashMap();
		keysMap.put(PDF_KEY_CONTRACTNUMBER,policyNumber);

		//put document name into Map 
		Map parameterMap = new HashMap();
		parameterMap.put("docName", documentName);
		parameterMap.put("keyMap", keysMap);
		parameterMap.put("customerData", getExtract());
		
		//invoke the webservices
		String eventUser = NbaConfiguration.getInstance().getCorrespondence().getEventDrivenAt(0).getEventDrivenUserName();
		String eventPassword = NbaConfiguration.getInstance().getCorrespondence().getEventDrivenAt(0).getEventDrivenUserPassword();
		
		NbaUserVO nbaUserVO = getNbaUserVO(eventUser,eventPassword);
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CORRESPONDENCE_GETPDF, nbaUserVO,
				null, null, parameterMap);
		response = (String) webServiceInvoker.execute();
		byte [] decodeArray = NbaBase64.decode(response);
    	setFromEventFlage(false);        
		return decodeArray;
	} catch (Exception e) {
		getLogger(this.getClass().getName()).logError(e);//AXAL3.7.54
		throw new NbaBaseException("[Exception = " + e.getMessage()+"][Response = " + response + "]");//AXAL3.7.54
	}
	//SPR3337 code deleted 
}
/**
 * This method retrieves a <code>List</code> of Correspondence applicable for
 * On demand Correspondence
 * @return java.util.List The list of letter Names
 * @exception com.csc.fsg.nba.exception.NbaBaseException This exception is thrown whenever there is an error in Document Solutions.
 */
public List getLetterNames(String categoryName) throws NbaBaseException {
    Forms forms = null;
    String response = null;//AXAL3.7.54
    try {
		String userName = NbaConfiguration.getInstance().getCorrespondence().getOnDemandAt(0).getOnDemandUserName();
		String password = NbaConfiguration.getInstance().getCorrespondence().getOnDemandAt(0).getOnDemandPassword();
    	
		NbaUserVO nbaUserVO = getNbaUserVO(userName,password);
		//put category name into Map 
		Map parameterMap = new HashMap();
		parameterMap.put("categoryName", categoryName);
		
		//invoke the webservices
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CORRESPONDENCE_GETLETTERS, nbaUserVO,
				null, null, parameterMap);
		response = (String) webServiceInvoker.execute();
		
		forms = DocPrintForms.unmarshal(new ByteArrayInputStream(response.getBytes())).getForms();	
    } catch (Exception e) {
    	getLogger(this.getClass().getName()).logError(e);//AXAL3.7.54
		throw new NbaBaseException("[Exception = " + e.getMessage()+"][Response = " + response + "]");//AXAL3.7.54
    }
	// forms will not be null because an exception will be thrown above if it cannot be instantiated
    int nameCount = forms.getNameAndDescAndCategoryAndDefaultDataSourceNameCount(); // SPR3290
    List info = new ArrayList(nameCount); // SPR3290
    String[] data; // SPR3290

	for (int i = 0; i < nameCount; i++) { // SPR3290
	   if (forms.getNameAndDescAndCategoryAndDefaultDataSourceNameAt(i).getDesc().length() > 0) { //meaning that this is an ondemand letter
		   data = new String[4]; // SPR3290
		   data[0] = forms.getNameAndDescAndCategoryAndDefaultDataSourceNameAt(i).getName();
		   data[1] = forms.getNameAndDescAndCategoryAndDefaultDataSourceNameAt(i).getDesc();
		   data[2] = forms.getNameAndDescAndCategoryAndDefaultDataSourceNameAt(i).getCategory(); 
		   data[3] = forms.getNameAndDescAndCategoryAndDefaultDataSourceNameAt(i).getDefaultDataSourceName(); 
		   info.add(data);
	   }
    }
    return info;
}

//Moved the method to Super Class

/**
 * This method initializes all instance variables.
 * @param dst The NbaDst Xml representing the contract for whic a letter needs to be generated
 * @param sessionID A unique string to Indentify each Correspondence session
 * @param UserID The user to log on to the correspondence system.
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever an error occurs. 
 */
//NBA129, NBA213 new method signature
public void initializeObjects(NbaDst dst, NbaUserVO user) throws NbaBaseException {
	initializeNbaDst(dst);
	setUser(user);
	setLetterType(new String());
	setExtract(new String());
	setCorrRequestVO(new NbaCorrespondenceRequestVO());
}

//APSL5200
public void initializeObjects(NbaCorrespondenceRequestVO correspondenceRequestVO) throws NbaBaseException {
	initializeNbaDst(correspondenceRequestVO.getDst());
	setUser(correspondenceRequestVO.getNbaUserVO());
	setLetterType(new String());
	setExtract(new String());
	setCorrRequestVO(correspondenceRequestVO);
}

/**
 * This method marks the letter for batch printing.
 * @exception com.csc.fsg.nba.exception.NbaBaseException The exception description.
 */
public void markForBatchPrint() throws NbaBaseException {
	try{
		String batchName = NbaConfiguration.getInstance().getBatchPrint(CATEGORY_ID).getBatch();
		NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(nbaDst.getNbaLob().getBackendSystem(), CATEGORY_ID, BATCH_PRINT_REQUEST);
		//invoke the webservices
		service.invokeCorrespondenceWebService(getUser().getUserID(),getUser().getPassword(),null,batchName,getExtract(), BATCH_PRINT_REQUEST,getUser().getToken(), null );	//SPR3337
		
	} catch (Exception e) {
		   throw new NbaBaseException("[Exception =" + e.getMessage() + "]", e);
	   }
}

/**
 * This method resolves a variable value using OINK 
 * If the variable is the home state only the first
 * 2 chararacters are returned.
 * @param aVariable A variable whose value needs to be resolved
 * return resolved variable
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever there is an exception.
 */
protected String resolveVariableValue(String aVariable)
	throws NbaBaseException {
	String value = super.resolveVariableValue(aVariable);
	if (aVariable.length() > 8 && value != null && value.length() > 2) {//AXAL3.7.10A
		if (STATE.equalsIgnoreCase(aVariable.substring(0, 8)) && !NbaUtils.isBlankOrNull(value)) {//ALII587
			value = value.substring(0, 2);
		}
	}
	return value;
}

/**
 * This method resolves a variable value using OINK 
 * If the variable is the home state only the first
 * 2 chararacters are returned.
 * @param aVariable A variable whose value needs to be resolved
 * return resolved variable
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever there is an exception.
 */
//AXAL3.7.10A New Method
protected String[] resolveVariableValues(String aVariable)
	throws NbaBaseException {
	String[] value = super.resolveVariableValues(aVariable);
	if (aVariable.length() > 8 && value.length > 0 && value[0] != null) {
		if (STATE.equalsIgnoreCase(aVariable.substring(0, 8)) && !NbaUtils.isBlankOrNull(value[0])) {//ALII587
			value[0] = value[0].substring(0, 2);
		}
	}
	return value;
}

/**
 * This method sets the document name
 * @param string
 */
private void setDocumentName(java.lang.String string) {
	documentName = string;
}
/**
 * This method can be used to provide the extract for the letter to be generated. 
 * @param extract An xml string representing the letter extract
 */
public void setExtract(String extract) {			
	this.extract = extract;
}

	//Moved the method to Super Class


	/**
	 * @return Returns the policyNumber.
	 */
	//AXAL3.7.13I - New Method
	public String getPolicyNumber() {
		return policyNumber;
}
	/**
	 * @param policyNumber The policyNumber to set.
	 */
	//AXAL3.7.13I - New Method
	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = policyNumber;
	}
	
	/**
	 * @return Returns the fromEventFlage.
	 */
	//AXAL3.7.13I - New Method
	public boolean isFromEventFlage() {
		return fromEventFlage;
	}
	/**
	 * @param fromEventFlage The fromEventFlage to set.
	 */
	//AXAL3.7.13I - New Method	
	public void setFromEventFlage(boolean fromEventFlage) {
		this.fromEventFlage = fromEventFlage;
	}
	//CR58636 New method ADC Retrofit
    public void setImagetoExtract(NbaCorrespondenceExtract extract) throws NbaBaseException
	{
		if(isLetterImage())
		{
			retrieveParent(nbaDst.getWorkItem().getItemID());
			int numberOfSources = nbaDst.getNbaSources().size(); 
			for (int i = 0; i < numberOfSources ; i++) { 
				NbaSource source = (NbaSource) nbaDst.getNbaSources().get(i);
				if (source.getSource().getSourceType().equalsIgnoreCase(NbaConstants.A_WT_APPLICATION))
				{ 
					List images = WorkflowServiceHelper.getBase64SourceImage(getUser(), source);
					extract.setAttachedImages(images);
					break;
				}
		                                               }
		}
	}
	//CR58636 ADC Retrofit
	public NbaDst retrieveParent(String transactionID) throws NbaBaseException {
		        NbaAwdRetrieveOptionsVO vo = new NbaAwdRetrieveOptionsVO();
		        vo.requestCaseAsParent();
		        vo.requestTransactionAsChild();
		        vo.requestSources();
		        vo.setWorkItem(transactionID, true);
		        nbaDst = WorkflowServiceHelper.retrieveWorkItem(getUser(), vo);  
		    return nbaDst;
		}
	/**
	 * @return the corrRequestVO
	 */
	public NbaCorrespondenceRequestVO getCorrRequestVO() {
		return corrRequestVO;
	}
	/**
	 * @param corrRequestVO the corrRequestVO to set
	 */
	public void setCorrRequestVO(NbaCorrespondenceRequestVO corrRequestVO) {
		this.corrRequestVO = corrRequestVO;
	}
	
	//NBLXA-2114 New Method
	//Added NbaTxLife additional field in param
	public void initializeObjects(NbaDst dst, NbaUserVO user, NbaTXLife nbaTXLife) throws NbaBaseException {
		initializeNbaDst(dst);
		setUser(user);
		setLetterType(new String());
		setExtract(new String());
		setCorrRequestVO(new NbaCorrespondenceRequestVO());
		setNbaTXLife(nbaTXLife);
	}

}
