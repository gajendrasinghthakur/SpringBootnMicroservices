package com.csc.fsg.nba.business.transaction;

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
 * 
 */
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaObjectPrinter;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaRequirementsData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaMIBCodedReport;
import com.csc.fsg.nba.vo.NbaMIBResponse;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaRequirement;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaValueObject;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.InsurableParty;
import com.csc.fsg.nba.vo.nbaschema.Satisfy;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.AddressExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.EndorsementExtension;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.Message;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PredictiveResult;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsRequirement;

/** 
 * 
 * This class provides an easy way to create requirement work item and
 * requirement control source.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td><tr>
 * <tr><td>NBA022</td><td>Version 2</td><td>Case Manager HTML Views</td><tr> 
 * <tr><td>NBA051</td><td>Version 3</td><td>Allow Search on Work Items</td></tr>
 * <tr><td>NBA036</td><td>Version 3</td><td>nbA Underwriter Workbench Transactions to DB</td></tr>
 * <tr><td>NBA087</td><td>Version 3</td><td>Post Approval & Issue Requirements</td></tr>
 * <tr><td>SPR1784</td><td>Version 4</td><td>The Temporary Requirement Work Item moves to NBEND queue even when there is no Requirement Work Item.</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>Requirement Control Source migration Changes</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR3017</td><td>Version 6</td><td>MIB Checking requirement Received date changes when MIB codes are added on the MIB Report detail view</td></tr>
 * <tr><td>SPR3050</td><td>Version 6</td><td>APORDREQ Process Stops with Error "A runtime error occurred; unable to complete processing Index: 2, Size: 2" </td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA154</td><td>Version 6</td><td>Requirement Business Function rewrite</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>NBA138</td><td>Version 6</td><td>Requirements Override Settings Project</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA188</td><td>Version 7</td><td>nbA XML Sources to Auxiliary</td></tr>
 * <tr><td>SPR2926</td><td>Version 7</td><td>Red bar not displayed on tabs when there are items that require attention</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up</td></tr>
 * <tr><td>SPR2742</td><td>Version 7</td><td>Remove OINK qualifiers INS, PINS and ANN from requirements determination processing</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item retrieval</td></tr>
 * <tr><td>AXAL3.7.01</td><td>AxaLife Phase 1</td><td>Scan and Indexing UI</td></tr>
 * <tr><td>AXAL3.7.06</td><td>AxaLife Phase 1</td><td>Requirement Determination</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>NBA250</td><td>AXA Life Phase 1</td><td>nbA Requirement Form and Producer Email Management Project</td></tr>
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement</td></tr>
 * <tr><td>ALS2218</td><td>AxaLife Phase 1</td><td>QC# 1022  - Ad Hoc: Reply to Tentative Offer sets immediate Follow up</td></tr>
 * <tr><td>ALS2914</td><td>AxaLife Phase 1</td><td>Distribution Channel (DIST) LOB is now required for downstream processing for requirements.  Ensure it is copied from the NBAPPLCTN when the requirement is created.</td></tr>
 * <tr><td>SR566149 and SR519592</td><td>Discretionary</td><td>Reissue and Delivery Requirement Follow Up</td></tr>
 * <tr><td>APSL692</td><td>QC#4630 Satisfy message not saving in nbA requirements</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
 * <tr><td>APSL3836</td><td>Discretionary</td><td>Electronic Initial Premium - Phase 2</td></tr>
 * <tr><td>APSL4872</td><td>Discretionary</td><td>Requirement As Data</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaRequirementUtils {
	protected int reqType;
	protected int personCode;
	protected int personSeq;
	//SPR2742 code deleted
	protected boolean autoGenerated = false;
	protected String empId = null;
	protected NbaTXLife holding = null;

	protected NbaOinkDataAccess oinkDataAccess = null;
	protected NbaTableAccessor tableAccessor = null;

	protected static NbaLogger logger = null;
	
	// begin SPR3290
	public static final String actionAdd = "ADD"; //ACN014  NBA122
	public static final String actionUpdate = "UPDATE"; //ACN014  NBA122
	private static String OLI_REQSTAT_ORDER = Long.toString(NbaOliConstants.OLI_REQSTAT_ORDER);  //NBA122
	private static String OLI_REQSTAT_RECEIVED = Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED);  //NBA122
	private static String reqVendorModel = null; //NBA138
	private static String reqVendorEntryPoint = null; //NBA138
	// end SPR3290
/**
 * NbaRequirementUtils constructor comment.
 */
public NbaRequirementUtils() {
	super();
}
//SPR2742 code deleted
/**
 * Add a new Requirement Control Source to the case for the requirement transaction.
 * @param work the case 
 * @param nbaTransaction the requirement transaction
 */
// NBA008 New Method
public void addMasterRequirementControlSource(NbaDst work, NbaTransaction nbaTransaction) throws NbaBaseException {
	NbaSource requirementControlSource = nbaTransaction.getRequirementControlSource();
	if (requirementControlSource == null) {
		return;
	}

	NbaSource nbaSource = work.getRequirementControlSource();
	NbaXMLDecorator xmlDecorator;
	if (nbaSource == null) { // add a new source to the case
		getLogger().logDebug("About to add master requirement control source");
		xmlDecorator = new NbaXMLDecorator();
		NbaXMLDecorator sourceDecorator = new NbaXMLDecorator(requirementControlSource.getText());
		getLogger().logDebug("Get transaction RCS");
		xmlDecorator.addRequirement(
			nbaTransaction,
			sourceDecorator.getRequirement().getTransactionId(),
			sourceDecorator.getRequirement().getProvTransId(),
			sourceDecorator.getRequirement().getAgentOrdered());
		getLogger().logDebug("Add transaction RCS");
		updateRequirementControlSource(work,null,xmlDecorator.toXmlString(),actionAdd); //ACN014
		getLogger().logDebug("Update master requirement control source");
	} else { // update the source on the case

		getLogger().logDebug("About to update master requirement control source");
		xmlDecorator = new NbaXMLDecorator(nbaSource.getText());
		InsurableParty party = xmlDecorator.getInsurableParty(nbaTransaction.getNbaLob().getReqPersonSeq(), nbaTransaction.getNbaLob().getReqPersonCode());
		if (party != null) {
			for (int j = 0; j < party.getRequirementCount(); j++) {
				if (party.getRequirementAt(j).getAwdId() != null && //ACN014
				    party.getRequirementAt(j).getAwdId().equals(nbaTransaction.getID())) {
					return; //if already exists return
				}
			}
		}
		getLogger().logDebug("Get transaction RCS");
		NbaXMLDecorator sourceDecorator = new NbaXMLDecorator(requirementControlSource.getText());
		xmlDecorator.addRequirement(
			nbaTransaction,
			sourceDecorator.getRequirement().getTransactionId(),
			sourceDecorator.getRequirement().getProvTransId(),
			sourceDecorator.getRequirement().getAgentOrdered());
		updateRequirementControlSource(work,null,xmlDecorator.toXmlString(),actionUpdate); //ACN014	
		getLogger().logDebug("Update master requirement control source");
	}
}
/**
 * Add a new Requirement Control Source to the requirement work item for the requirement transaction.
 * @param nbaTransaction the requirement transaction
 */
public void addRequirementControlSource(NbaTransaction nbaTransaction) throws NbaBaseException {	//ACN014
	//begin ACN014
	NbaLob lob = nbaTransaction.getNbaLob(); //SPR3050
    getOinkDataAccess().setLobSource(lob); //SPR3050
	NbaVpmsModelResult nbaVpmsModelResult = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_TRANSACTION);
	if (nbaVpmsModelResult != null) {
		String transactionId = nbaVpmsModelResult.getVpmsModelResult().getResultAt(0);
		String provTransId = transactionId;
		//SPR3050 code deleted
		if (lob.getReqStatus() == null || !OLI_REQSTAT_RECEIVED.equals(lob.getReqStatus())) {  //NBA122
			nbaVpmsModelResult = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_REQUIREMENT_INITIAL_STATUS); //ANC014
				if (nbaVpmsModelResult != null) { //ACN014
					lob.setReqStatus(nbaVpmsModelResult.getVpmsModelResult().getResultAt(0)); // Requirement Status ACN014
				}
		}
		//end ACN014
								
		boolean agentOrdered = false;
		if (autoGenerated && !OLI_REQSTAT_ORDER.equals(lob.getReqStatus())) {	//ACN014  //NBA122
			agentOrdered = true;
		}
		if (nbaTransaction.getRequirementControlSource() == null) { // add a new source to the transaction
			NbaXMLDecorator xmlDecorator = new NbaXMLDecorator();
			xmlDecorator.addRequirement(nbaTransaction, transactionId, provTransId, agentOrdered);
			updateRequirementControlSource(null,nbaTransaction,xmlDecorator.toXmlString(),actionAdd); //ACN014
		}
	} else {
		throw new NbaBaseException(
			"Could not create requirement control source for person code "
				+ nbaTransaction.getNbaLob().getReqPersonCode()
				+ " person sequence "
				+ nbaTransaction.getNbaLob().getReqPersonSeq());
	}
}
//ALS4666 new method
protected NbaVpmsModelResult getDataFromVpmsModelRequirements(String entryPoint, Map deOink) throws NbaBaseException {
	return getDataFromVpmsModelRequirements(entryPoint, deOink, null);
}

/**
 * Create and initialize an <code>NbaVpmsResultsData</code> object to find matching work items.
 * @param entryPoint the VP/MS model's entry point
 * @param deOink map to skip attributes
 * @param userVO Nba UserVO for user id
 * @return NbaVpmsResultsData the VP/MS results
 */
//ACN014 Changed Signature //ALS4322 signature changed //ALS4666 signature changed //SR343236 signature changed
public NbaVpmsModelResult getDataFromVpmsModelRequirements(String entryPoint, Map deOink, NbaUserVO userVO) throws NbaBaseException { //APSL404
    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
	try {
		vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
		vpmsProxy.setVpmsEntryPoint(entryPoint);
		String processId = (userVO == null) ? "DUMMY" : userVO.getUserID(); //ALS4666
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, processId); //SPR2639 //ALS4666
		deOink.put("A_XmlResponse", "true"); //ACN014
		vpmsProxy.setSkipAttributesMap(deOink);
		//ACN014 Begin
		VpmsComputeResult compResult = vpmsProxy.getResults();
		if( compResult.getReturnCode() != 0) {
			return null;			
		}
		//ACN014 End
		//SPR3362 code deleted
		NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(compResult.getResult());  //ACN014
		return nbaVpmsModelResult; //ACN014
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException("RequirementsDetermination problem", re);
	//begin SPR3362
	} finally {
	    try {
	        if(vpmsProxy != null){
	            vpmsProxy.remove();
	        }
        } catch (RemoteException e) {
            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
        }
	}
    //end SPR3362
}

/**
 * Create and initialize an <code>NbaVpmsResultsData</code> object to find matching work items.
 * @param entryPoint the VP/MS model's entry point
 * @return NbaVpmsResultsData the VP/MS results
 */
// ALS4322 added method
protected NbaVpmsModelResult getDataFromVpmsModelRequirements(String entryPoint) throws NbaBaseException {
	Map deOink = new HashMap();
	return getDataFromVpmsModelRequirements(entryPoint, deOink);
}

/**
 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected static NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaRequirementUtils.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaAutomatedProcess could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * Answer the table accessor * 
 * @return an instance of NbaTableAccessor
 */
protected NbaTableAccessor getTableAccessor() throws NbaBaseException {
	if (tableAccessor == null) {
		tableAccessor = new NbaTableAccessor();
	}
	return tableAccessor;
}
/**
 * Set true for Automatically generated requirements, else set to false
 * @param value the auto generated requirement indicator
 */
public void setAutoGeneratedInd(boolean value) {
	autoGenerated = value;
}
/**
 * Sets the Employee ID who is ordering the requirement
 * @param value the employee id
 */
public void setEmployeeId(String value) {
	empId = value;	
}
/**
 * Sets the Holding Inquiry xmlife
 * @param value the holding inquiry
 */
public void setHoldingInquiry(NbaTXLife value) throws NbaBaseException {
	holding = value;
	oinkDataAccess = new NbaOinkDataAccess(holding); // set up the NbaOinkDataAccess object
	oinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
}
//SPR2742 code deleted
/**
 * Sets the Requirement Persone Sequence
 * @param value the person sequence
 */
public void setReqPersonCodeAndSeq(int code,int seq) {
	personCode = code;
	personSeq = seq;
	//SPR2742 code deleted
}
/**
 * Sets the Requirement Type lob
 * @param value the requirement type
 */
public void setReqType(int value) {
	reqType = value;	
}
/**
 * Update a requirement work item on the case. Call Oink, VP/MS and Table Accessor to 
 * populate the required LOBs. Also create a requirement control source and attach it to the
 * work item.
 * WARNING : Throws Exception if required information is not available to update requirement 
 * work item and requirement control source. Make sure you have set all the required information
 * prior to calling this method.
 * @return com.csc.fsg.nba.vo.NbaTransaction
 */
public NbaTransaction updateRequirementWorkItem(NbaDst work, NbaTransaction nbaTransaction) throws NbaBaseException {
	processRequirementWorkItem(work, nbaTransaction);	//ACN014
	addRequirementControlSource(nbaTransaction);		//ACN014
	return nbaTransaction;								//ACN014
}

/**
 * Update a requirement work item on the case. Call Oink, VP/MS and Table Accessor to 
 * populate the required LOBs. 
 * WARNING : Throws Exception if required information is not available to update requirement 
 * work item. Make sure you have set all the required information
 * prior to calling this method.
 * @return com.csc.fsg.nba.vo.NbaTransaction
 */
//APSL3960 - Update method for avoid duplicate LOB issue or avoiding multiple reference of NbaLob in NbaTransaction. 
public NbaTransaction processRequirementWorkItem(NbaDst work, NbaTransaction nbaTransaction) throws NbaBaseException {
	processRequirementWorkItem(work, nbaTransaction, nbaTransaction.getNbaLob()); //APSL3960
	return nbaTransaction; 
}
/**
 * Update a requirement work item on the case. Call Oink, VP/MS and Table Accessor to 
 * populate the required LOBs. 
 * WARNING : Throws Exception if required information is not available to update requirement 
 * work item. Make sure you have set all the required information
 * prior to calling this method.
 * @return com.csc.fsg.nba.vo.NbaTransaction
 */
//ACN014 New Method. Stopped addition of requirement control source and renamed the method (Original name was updateRequirementWorkItem)
//New Method APSL3960 - Only add one more parameter in method.   
public NbaTransaction processRequirementWorkItem(NbaDst work, NbaTransaction nbaTransaction, NbaLob lob) throws NbaBaseException {
	if (!work.isCase()) {
		throw new NbaBaseException("Invalid work item");
	}
	NbaOinkRequest oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
	oinkDataAccess.setLobSource(lob);
	if (lob.getReqVendor() == null) { //if the provider vendor is already set don't call VP/MS
		NbaVpmsModelResult nbaVpmsModelResult = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_PROVIDER); //ACN014
		if (nbaVpmsModelResult != null) { //ACN014
			lob.setReqVendor(nbaVpmsModelResult.getVpmsModelResult().getResultAt(0));  //ACN014
		}
	}

	oinkDataAccess.setLobSource(work.getNbaLob());

	//SPR2742 code deleted
	String temp = null;
	//NBA036 code deleted
	if (personSeq > 0 && personSeq < 10) { //NBA036
		temp = "0" + String.valueOf(personSeq);
	} else {
		temp = String.valueOf(personSeq);
	}
	oinkRequest.setPartyFilter(personCode, temp);
	//SPR2742 code deleted

	try {
		if (lob.getAgentID() == null) {
			lob.setAgentID(work.getNbaLob().getAgentID());
		}
		if (lob.getAgency() == null) {
			lob.setAgency(work.getNbaLob().getAgency());
		}
		if (lob.getAppState() == null) {//AXAL3.7.06
			lob.setAppState(work.getNbaLob().getAppState());
		}
		if (NbaUtils.isBlankOrNull(lob.getDistChannel()) || lob.getDistChannel() <= 0) {//ALS2914 //ALS1667 //ALS3392
			lob.setDistChannel(String.valueOf(work.getNbaLob().getDistChannel()));//ALS1667
		}
		NbaRequirementsData requirementsData =
			(NbaRequirementsData) getTableAccessor().getDataForOlifeValue(
				tableAccessor.setupTableMap(work),
				NbaTableConstants.NBA_REQUIREMENTS,
				String.valueOf(reqType));
		if (requirementsData == null) {
			throw new NbaDataAccessException("No data found for requirement " + reqType);
		}

		lob.setReqType(reqType); // Requirement Type
		lob.setReqMedicalType(requirementsData.getMedicalTypeIndicator() == 1); // Medical Indicator
		//NBA130 CODE DELETED
		if(new Integer(lob.getReview())==null){//SR564247(APSL2254)
			lob.setReview((int)requirementsData.getReviewIndicator()); //NBA087 // Underwriter Review Indicator  //NBA122
		}
		//Begin ALNA159, ALNA161
		oinkRequest.setVariable("PartyTypeCode");
		String typeCode = oinkDataAccess.getStringValueFor(oinkRequest); 
		if ( typeCode != null && typeCode.equals(String.valueOf(NbaOliConstants.OLI_PT_ORG))){
			oinkRequest.setVariable("FullName");
			lob.setLastName(oinkDataAccess.getStringValueFor(oinkRequest)); 
			//NBLXA-1254 start
			oinkRequest.setVariable("GovtID");
			lob.setEntityEinTin(oinkDataAccess.getStringValueFor(oinkRequest));
			oinkRequest.setVariable("DBA");
			lob.setEntityName(oinkDataAccess.getStringValueFor(oinkRequest));
			//NBLXA-1254 End
		} else {
			oinkRequest.setVariable("Gender");
			lob.setGender(oinkDataAccess.getStringValueFor(oinkRequest)); // Gender
			oinkRequest.setVariable("BirthDate");
			lob.setDOB(NbaUtils.getDateFromString(oinkDataAccess.getStringValueFor(oinkRequest))); // Date of Birth
			oinkRequest.setVariable("LastName");
			lob.setLastName(oinkDataAccess.getStringValueFor(oinkRequest)); // Last name
			oinkRequest.setVariable("FirstName");	//SPR1784
			lob.setFirstName(oinkDataAccess.getStringValueFor(oinkRequest)); // First name //SPR1784
			oinkRequest.setVariable("MiddleInitial");	
			lob.setMiddleInitial(oinkDataAccess.getStringValueFor(oinkRequest)); //ALS3118
		}
		oinkRequest.setVariable("RelationRoleCode");
		lob.setReqPersonCode(Integer.parseInt(oinkDataAccess.getStringValueFor(oinkRequest))); // Person Code
		oinkRequest.setVariable("RelatedRefID");
		lob.setReqPersonSeq(Integer.parseInt(oinkDataAccess.getStringValueFor(oinkRequest))); // Person Sequence
		oinkRequest.setVariable("GovtID");
		lob.setSsnTin(oinkDataAccess.getStringValueFor(oinkRequest)); // SSN
		oinkRequest.setVariable("GovtIDTc"); // Tax ID Type 
		String taxIDType = oinkDataAccess.getStringValueFor(oinkRequest);
		if (taxIDType != null && taxIDType.trim().length() !=0 ) {
			lob.setTaxIdType(Integer.parseInt(taxIDType)); 
		}
		lob.setProductTypSubtyp(work.getNbaLob().getProductTypSubtyp()); //NBA022
		lob.setAppOriginType(work.getNbaLob().getAppOriginType()); //ALS5466
		lob.setReplacementIndicator(work.getNbaLob().getReplacementIndicator()); //ALS5466
		//End ALNA159, ALNA161
		oinkDataAccess.setLobSource(lob);

	} catch (java.text.ParseException pe) {
		throw new NbaBaseException("Problem in adding requirement", pe);
	}
	return nbaTransaction;
}
	/**
	 * Sets the Oink database access
	 * @param value the database access
	 */
	//ACN014 New Method
	public void setOinkDataAccess(NbaOinkDataAccess access) {
		oinkDataAccess = access;
	}

	/**
	 * This method recieves the case or transaction, the requirement control source to be added 
	 * or updated and an action indicator. If the indicator is add, it adds the requirementcontrolsource
	 * to the database and a NbaSource object of type RequirementControlSource to the workitem. 
	 * If it is update,it updates the source in database and updates the NbaSource object for the 
	 * RequirementControlSource associated with the workitem.   
	 * @param work The NbaDst object for case
	 * @param nbaTransaction The NbaTransaction object for workitem
	 * @param requirementControlSource The requirement control source string to be stored in the Database
	 * @param actionIndicator the indicator add/update
	 */
	//ACN014 new method
	public void updateRequirementControlSource(NbaDst work, NbaTransaction nbaTransaction, String requirementControlSource, String actionIndicator) throws NbaBaseException{					
	    //NBA188 code deleted
		if (actionIndicator.equals(actionAdd)){
			if (work != null){ //if case, get the id of case
				//NBA188 code deleted
				work.addNbaSource(new NbaSource(work.getBusinessArea(),NbaConstants.A_ST_REQUIREMENT_CONTROL, requirementControlSource));
			}else { // if transaction, get the id of transaction
				//NBA188 code deleted	
				nbaTransaction.addNbaSource(new NbaSource(nbaTransaction.getBusinessArea(),NbaConstants.A_ST_REQUIREMENT_CONTROL, requirementControlSource));
			}
		}else if (actionIndicator.equals(actionUpdate)){
		    //Begin NBA188
		    NbaSource nbaSource;
		    if (work != null){//if case, get the id of case
				//NBA188 code deleted	
		        nbaSource = work.getRequirementControlSource();
		        nbaSource.setText(requirementControlSource);
				nbaSource.setUpdate();
			}else {// if transaction, get the id of transaction
				//NBA188 code deleted	
			    nbaSource = nbaTransaction.getRequirementControlSource();
			    nbaSource.setText(requirementControlSource);
			    nbaSource.setUpdate();
			    //End NBA188
			}
		}
	}  

	/**
	 * Returns a list of <code>NbaRequirements</code> for a party.  Only call this
	 * method with a <code>NbaDst</code> for a case.  
	 * @param nbaDst a case work item
	 * @param nbaTXLife contract information
	 * @param partyID ID of an insurable party
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA122 New Method
	//NBA154 Changed method signature
	public static List getRequirements(NbaDst nbaDst, NbaTXLife nbaTXLife, Relation relation) throws NbaBaseException {
		List reqList = new ArrayList();
		if (nbaTXLife != null) {
			List workflowList = getRequirementTransactions(nbaDst,relation); //NBA154
			String partyID = relation.getRelatedObjectID(); //NBA154
			Map contractList = nbaTXLife.getRequirementInfos(partyID); 
			int workflowCount = workflowList.size();
			for (int i=0; i < workflowCount; i++) {
				NbaTransaction trans = (NbaTransaction)workflowList.get(i);
				NbaLob lob = trans.getNbaLob();
				RequirementInfo reqInfo = (RequirementInfo) contractList.get(lob.getReqUniqueID());
				reqList.add(new NbaRequirement(trans, reqInfo, nbaTXLife, relation)); //NBA130 NBA208-36
			}
		}
		return reqList;
	}

	/**
	 * Returns a list of requirement transactions <code>NbaTransactions</code>
	 * for the related party in a relation.  Only call this method with
	 * a <code>NbaDst</code> for a case.
	 * @param nbaDst - a case work item 
	 * @param relation - insurable relation for a party 
	 * @return List of requirement NbaTransactions
	 * @throws NbaBaseException
	 */
	//NBA122 New Method
	public static List getRequirementTransactions(NbaDst nbaDst, Relation relation) throws NbaBaseException {
		List reqList = null;
		if (relation != null) {
			reqList = getRequirementTransactions(nbaDst, relation.getRelationRoleCode(), relation.getRelatedRefID());
		} else {
			reqList = new ArrayList();
		}
		return reqList;
	}

	/**
	 * Returns a list of requirement transactions <code>NbaTransactions</code>
	 * for a person by person code and sequence.  Only call this method with
	 * a <code>NbaDst</code> for a case.
	 * @param nbaDst - a case work item
	 * @param personCode - the person code of a party relation
	 * @param personSeq - the person sequence of a party relation
	 * @return List of requirement NbaTransactions
	 * @throws NbaBaseException
	 */
	//NBA122 New Method
	public static List getRequirementTransactions(NbaDst nbaDst, long personCode, String personSeq) throws NbaBaseException {
		try {
			return getRequirementTransactions(nbaDst, personCode, Long.parseLong(personSeq));
		} catch (NumberFormatException nfe) {
			throw new NbaBaseException("Invalid person sequence value: " + personSeq, nfe);
		}
	}

	/**
	 * Returns a list of requirement transactions <code>NbaTransactions</code>
	 * for a person by person code and sequence.  Only call this method with
	 * a <code>NbaDst</code> for a case.
	 * @param nbaDst - a case work item
	 * @param personCode - the person code of a party relation
	 * @param personSeq - the person sequence of a party relation
	 * @return List of requirement NbaTransactions
	 * @throws NbaBaseException
	 */
	//NBA122 New Method
	public static List getRequirementTransactions(NbaDst nbaDst, long personCode, long personSeq) throws NbaBaseException {
		List reqList = new ArrayList();
		if (nbaDst != null) {
			List allTrans = nbaDst.getNbaTransactions();
			int count = allTrans.size();
			for (int i=0; i<count; i++) {
				NbaTransaction trans = (NbaTransaction)allTrans.get(i);
				NbaLob lob = trans.getNbaLob();
				if (lob != null && personCode == lob.getReqPersonCode() && personSeq == lob.getReqPersonSeq()) {
					reqList.add(trans);
				}
			}
		}
		return reqList;
	}

	/**
	 * Returns the requirement transaction by ID.
	 * @param nbaDst
	 * @param id
	 * @return
	 * @throws NbaNetServerDataNotFoundException
	 */
	//NBA122 New Method
	public static NbaTransaction getRequirementTransaction(NbaDst nbaDst, String id) throws NbaNetServerDataNotFoundException {
		NbaTransaction nbaTransaction = null;
		if (nbaDst != null && id != null) {
			List allTrans = nbaDst.getNbaTransactions();
			int count = allTrans.size();
			for (int i=0; i<count; i++) {
				nbaTransaction = (NbaTransaction)allTrans.get(i);
				if (id.equals(nbaTransaction.getID())) {
					return nbaTransaction;
				}
			}
		}
		return null;
	}

	/**
	 * Creates a requirement transaction and attaches it to the case work item.
	 * @param nbaDst case work item
	 * @param nbaRequirement requirement info
	 * @throws NbaNetServerDataNotFoundException
	 */
	//NBA122 New Method
	public static void createRequirementTransaction(NbaDst nbaDst, NbaRequirement nbaRequirement) throws NbaNetServerDataNotFoundException, NbaBaseException {
		NbaTransaction nbaTransaction = null;
		if (nbaRequirement.getStatus() == NbaOliConstants.OLI_REQSTAT_ORDER) {
			nbaTransaction = nbaDst.addTransaction(NbaConstants.PROC_VIEW_REQUIREMENT_ORDER, "");
			nbaTransaction.setAction(NbaActionIndicator.ACTION_ORDER);
		} else {
			nbaTransaction = nbaDst.addTransaction(NbaConstants.PROC_VIEW_REQUIREMENT_ADD, "");
			nbaTransaction.setAction(NbaActionIndicator.ACTION_ADD);
		}
		nbaTransaction.getNbaLob().setAppOriginType(nbaDst.getNbaLob().getAppOriginType());
		updateRequirementTransaction(nbaTransaction, nbaRequirement);
	}

	/**
	 * Updates the work item requirement information.  The requirement transaction
	 * work item being updated should be a child of the case <code>NbaDst</code>.
	 * @param nbaDst case work item being updated
	 * @param nbaRequirement requirement info
	 * @throws NbaNetServerDataNotFoundException
	 */
	//NBA122 New Method
	public static void updateRequirement(NbaDst nbaDst, NbaRequirement nbaRequirement) throws NbaNetServerDataNotFoundException {
		if (nbaRequirement != null) {
			NbaTransaction nbaTransaction = getRequirementTransaction(nbaDst, nbaRequirement.getTransactionID());
			updateRequirementTransaction(nbaTransaction, nbaRequirement);
		}
	}

	/**
	 * Updates the contract requirement information.
	 * @param nbaTXLife contract being updated
	 * @param nbaRequirement requirement info
	 * @throws NbaBaseException
	 */
	//NBA122 New Method
	public static void updateRequirement(NbaTXLife nbaTXLife, NbaRequirement nbaRequirement) throws NbaBaseException {
		updateMIBResponse(nbaTXLife, nbaRequirement);
		updateMIBReports(nbaTXLife, nbaRequirement);
		updateRequirementInfo(nbaTXLife, nbaRequirement); //NBA138
	}

	/**
	 * Updates the MIB Response on the contract.  The response is stored as an <code>Attachment</code>
	 * object off the <code>RequirementInfo</code> object.
	 * @param nbaTXLife contract being updated
	 * @param nbaRequirement nbA representation of a requirement
	 */
	//NBA122 New Method
	public static void updateMIBResponse(NbaTXLife nbaTXLife, NbaRequirement nbaRequirement) {
		if (nbaTXLife != null && nbaRequirement != null) {
			NbaMIBResponse mibResponse;
			try {
				mibResponse = nbaRequirement.getNbaMibResponse();
			} catch (Exception e) {
				// if exception thrown, there is no response to update
				return;
			}
			if (mibResponse != null && mibResponse.isActionUpdate()) {
				RequirementInfo reqInfo = nbaTXLife.getRequirementInfo(nbaRequirement.getRequirementInfoUniqueID());
				if (reqInfo != null) {
					int count = reqInfo.getAttachmentCount();
					for (int i = 0; i < count; i++) {
						Attachment attachment = reqInfo.getAttachmentAt(i);
						if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_MIB_SERVRESP) {
							AttachmentData attachmentData = attachment.getAttachmentData();
							if (attachmentData != null) { 
								attachmentData.setPCDATA(mibResponse.toXmlString());
								attachmentData.setActionUpdate(); 
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Updates the MIB Reports on the contract.  The reports are stored as <code>Attachment</code>
	 * objects off the <code>RequirementInfo</code> object.
	 * @param nbaTXLife contract being updated
	 * @param nbaRequirement nbA representation of a requirement
	 * @throws NbaBaseException
	 */
	//NBA122 New Method
	public static void updateMIBReports(NbaTXLife nbaTXLife, NbaRequirement nbaRequirement) throws NbaBaseException {
		if (nbaTXLife != null && nbaRequirement != null) {
			List reports;
			try {
				reports = nbaRequirement.getNbaMibReports();
			} catch (Exception e) {
				// if exception thrown, there is no response to update
				return;
			}
			if (reports != null) {
				NbaMIBCodedReport mibReport;
				RequirementInfo reqInfo = nbaTXLife.getRequirementInfo(nbaRequirement.getRequirementInfoUniqueID());
				if (reqInfo != null) {
					Attachment attachment;
					AttachmentData attachmentData;
					int count = reports.size();
					for (int i = 0; i < count; i++) {
						mibReport = (NbaMIBCodedReport) reports.get(i);
						if (mibReport.isActionAdd()) {
						    reqInfo.getAttachmentCount();	//NBA208-36 Retrieve from database prior to adding
							reqInfo.addAttachment(mibReport.createParentAttachment());
						} else if (mibReport.isActionUpdate() || mibReport.isActionDelete()) {  //SPR3017
							attachment = getMibReportAttachment(reqInfo, mibReport.getAttachmentId());
							if (attachment != null) {
								if (mibReport.isActionUpdate())  {
									attachmentData = attachment.getAttachmentData();
									if (attachmentData != null) {
										attachmentData.setPCDATA(mibReport.toNbaTXLife().toXmlString());
										attachmentData.setActionUpdate();
									}
								} else if (mibReport.isActionDelete()) {
									attachment.setActionDelete();
								}
							}
						}
					}
				}					
			}
		}
	}

	/**
	 * Finds the corresponding MIB Report <code>Attachment</code> within a <code>RequirementInfo</code>
	 * based on the attachment type and ID.
	 * @param reqInfo requirement information
	 * @param id MIB report attachment ID
	 * @return
	 */
	// NBA122 New Method
	public static Attachment getMibReportAttachment(RequirementInfo reqInfo, String id) {
		Attachment attachment;
		if (reqInfo != null && id != null) {
			int count = reqInfo.getAttachmentCount();
			for (int i = 0; i < count; i++) {
				attachment = reqInfo.getAttachmentAt(i);
				if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_MIB402 && id.equals(attachment.getId())) {
					return attachment;
				}
			}
		}
		return null;								
	}

	/**
	 * Updates the requirement work item with changes from the <code>NbaRequirement</code>.
	 * @param nbaTransaction requirement work item
	 * @param nbaRequirement requirement value object 
	 */
	//NBA122 New Method
	public static void updateRequirementTransaction(NbaTransaction nbaTransaction, NbaRequirement nbaRequirement) {
		if (nbaTransaction != null && nbaRequirement != null
                && (((NbaValueObject) nbaRequirement).isActionAdd() || ((NbaValueObject) nbaRequirement).isActionUpdate())) { //SPR3017
			NbaLob nbaLob = nbaTransaction.getNbaLob();

			nbaLob.setReqType((int)nbaRequirement.getType());
			nbaLob.setReqStatus(Long.toString(nbaRequirement.getStatus()));
			//Begin NBA130
			nbaLob.setReqOrderDate(nbaRequirement.getOrderDate());
			nbaLob.setReqPersonCode((int)nbaRequirement.getPersonCode());
			nbaLob.setReqPersonSeq((int)nbaRequirement.getPersonSeq());
			nbaLob.setReview((int)nbaRequirement.getReview());
			nbaLob.setReqDrName(NbaObjectPrinter.formatFullName(nbaRequirement.getDrFirstName(), nbaRequirement.getDrMiddleName(), nbaRequirement.getDrLastName()));  //NBA224
		 	// Begin NBLXA-1895
			if(!NbaUtils.isBlankOrNull(nbaRequirement.getFullName())){
			nbaLob.setEntityName(nbaRequirement.getFullName()); 
			nbaLob.setReqDrName(nbaRequirement.getFullName());
		    }
			// End NBLXA-1895
			nbaLob.setReqUniqueID(nbaRequirement.getUniqueID());
			nbaLob.setFormNumber(nbaRequirement.getFormNumber()); //NBA154
			nbaLob.setCheckAmount(nbaRequirement.getDraftAmount());//APSL2735
			//End NBA130 
			nbaLob.setOkToAdjust(nbaRequirement.isOverrideDraftAmtInd()); //APSL5254
			nbaLob.setReqVendor(nbaRequirement.getVendor()); //NBA138
			nbaLob.setParamedSignDate(nbaRequirement.getParamedSignDate()); //ALS4364
			nbaLob.setReqSignDate(nbaRequirement.getSignDate()); //APSL4872
			nbaLob.setDeliveryReceiptSignDate(nbaRequirement.getDeliveryReceiptSignDate());//NBLXA-2133
			nbaTransaction.setUpdate();
			nbaTransaction.setAction(NbaActionIndicator.ACTION_UPDATE);
			//APSL483 Begin
			if (nbaRequirement.getSatisfyComment()) {
				NbaSource source;
				try {
					source = nbaTransaction.getRequirementControlSource();
					if (source != null) {
						NbaXMLDecorator reqCntlSrc = new NbaXMLDecorator(source.getText());
						Satisfy satisfy = new Satisfy();
						satisfy.setCommentIndicator(true);
						reqCntlSrc.getRequirement().setSatisfy(satisfy);
						source.setText(reqCntlSrc.toXmlString());
						source.setUpdate();
					}
				} catch (NbaBaseException e) {
					getLogger().logDebug(NbaBaseException.SOURCE_XML + NbaUtils.getStackTrace(e));
				}
			}
			//APSL483 End
		}
	}

	/**
	 * The process of satisfying a requirement adds a comment to the requirement and
	 * sets the receipt date.  The requirement status is first checked to make sure
	 * the requirement has not already been satisfied.  There are two different ways
	 * to add a comment to the requirement based on user input.  The requirement
	 * control source can be updated with a satisfy comment, or a new Provider Result
	 * source can be added to the requirement.
	 * @param nbaDst current case work item
	 * @param nbaRequirement nbA representation of the requirement
	 * @throws NbaBaseException
	 */
	// NBA122 New Method //ALS5281, ALS5099 Refactored
	public static void satisfyRequirement(NbaDst nbaDst, NbaRequirement nbaRequirement, RequirementInfo reqInfo) throws NbaBaseException {
		NbaTransaction nbaTransaction = getRequirementTransaction(nbaDst, nbaRequirement.getTransactionID());
		NbaLob nbaLob = nbaTransaction.getNbaLob();
		boolean actionStatusReceived = OLI_REQSTAT_RECEIVED.equals(nbaLob.getReqStatus());
		if (!actionStatusReceived) {
			//APSL483 code moved to updateRequirementTransaction
			if (!nbaRequirement.getSatisfyComment()){
				//Begin APSL692
				try {
					NbaSource source = nbaTransaction.getRequirementControlSource();
					if (source != null) {
						NbaXMLDecorator reqCntlSrc = new NbaXMLDecorator(source.getText());
						Satisfy satisfy = new Satisfy();
						satisfy.setComment(nbaRequirement.getSatisfyMessage());
						reqCntlSrc.getRequirement().setSatisfy(satisfy);
						source.setText(reqCntlSrc.toXmlString());
						source.setUpdate();
					}
				} catch (NbaBaseException e) {
					getLogger().logDebug(NbaBaseException.SOURCE_XML + NbaUtils.getStackTrace(e));
				}
				//End APSL692
				//APSL692 Code Deleted
			}
			//begin SPR3017
			if (nbaLob.getReqReceiptDate() == null) {
				nbaLob.setReqReceiptDate(new Date());
				nbaLob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
			}
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			reqInfoExt.setDisplayImagesInd(false);
			//Begin ALS5714
			for(int i=0;i<nbaTransaction.getNbaSources().size();i++) {
				if (isSourceDisplayable(((NbaSource)nbaTransaction.getNbaSources().get(i)).getSourceType(),nbaDst)) {
					reqInfoExt.setDisplayImagesInd(true);
				}
			}
			//End ALS5714
			//end SPR3017
		}
		updateRequirementTransaction(nbaTransaction, nbaRequirement);//ALS5533
		//SPR3017 deleted code
	}
	
    /**
     * Returns OINK data access member variable 
     * @return Returns the oinkDataAccess.
     */
	//SPR3050 New Method
    public NbaOinkDataAccess getOinkDataAccess() {
        return oinkDataAccess;
    }
	/**
	 * Locates and returns the Physician Party object related to the RequirementInfo object
	 * @param holdingInquiry contains all the Party objects to be searched for the Physician
	 * @param requirementInfo the specific RequirementInfo for which the Physican is required
	 * @return the Physican Party object from the holdingInquiry
	 */
	 //NBA130 New Method
	public static Party getDoctorForRequirement(NbaTXLife holdingInquiry, RequirementInfo requirementInfo) {
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
		if( null != reqInfoExt && reqInfoExt.hasPhysicianPartyID()) {
			return (holdingInquiry.getParty(reqInfoExt.getPhysicianPartyID())).getParty();
		}
		return null;
	}
	/**
	* Create a new RequirementInfoUniqueID
	* @param aNbaTxLife the NbaTXLife object in which the RequirmentInfo object resides
	* @param reqInfo the requirement info object for which the RequirementInfoUniqueID will be generated
	* @return the newly generated RequirementInfoUniqueID
	*/
	//NBA130 New Method
	public static String generateRequirementInfoUniqueID(NbaTXLife aNbaTxLife, RequirementInfo reqInfo) throws NbaBaseException {
		String UNDERSCORE = "_";
		if( aNbaTxLife == null || aNbaTxLife.getPolicy() == null) {
			throw new NbaBaseException("NbaTXLife invalid");
		}
		Policy policy = aNbaTxLife.getPolicy();
		String polNum = policy.getPolNumber();
		String carrier = policy.getCarrierCode();
		String reqCode = String.valueOf(reqInfo.getReqCode());
		String reqId = reqInfo.getId().substring(reqInfo.getId().lastIndexOf('_') + 1, reqInfo.getId().length());
		String reqVendor = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REQUIREMENT_VENDOR_CODE);
		return reqVendor + "-" + carrier + UNDERSCORE + polNum + UNDERSCORE + reqCode + UNDERSCORE + reqId;
	}

	/**
	 * Creates a TrackingInfo object and initializes it with the Provider information.
	 * Updates the RequirementInfo object with the new TrackingInfo object.
	 * @param provider the name of the Provider for the Requirement
	 * @param aReqInfo the requirement object that will be updated with the new TrackingInfo object
	 */
	//NBA130 New Method
	public static void addProviderInfo(String provider, RequirementInfoExtension reqInfoExt) {
		TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
		if( trackingInfo == null) {
			trackingInfo = new TrackingInfo();
			reqInfoExt.setTrackingInfo(trackingInfo);
			trackingInfo.setActionAdd();
		} else {
			trackingInfo.setActionUpdate();
		}
		trackingInfo.setTrackingServiceProvider(provider);
	}
	/**
	 * Creates and initializes a RequirementInfo object with the ActionIndicator set to Add.
	 * @param nbaRequirement the requirement object to use to create the RequirementInfo object 
	 * @param nbaOLifeId used to generate the ID for the RequirementInfo object
	 * @return the newly created and initialized object
	 * @throws NbaBaseException
	 */
	//NBA130 New Method
	public RequirementInfo createRequirementInfo(NbaTXLife anbaTXLife, NbaLob lob, NbaRequirement nbaRequirement, NbaOLifEId nbaOLifEId, String partyId)//ALS4243
			throws NbaBaseException {//ALS2886
		RequirementInfo reqInfo = null;
		NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess();//ALS2886
		int reqType = lob.getReqType();
		lob.setReqType((int) nbaRequirement.getType());
		nbaOinkDataAccess.setLobSource(lob);//ALS2886
		nbaOinkDataAccess.setContractSource(anbaTXLife);//ALS2886
		setOinkDataAccess(nbaOinkDataAccess);//ALS2886
		if (null != nbaRequirement) {
			reqInfo = new RequirementInfo();
			nbaOLifEId.setId(reqInfo);
			reqInfo.setReqCode((int) nbaRequirement.getType());
			reqInfo.setReqStatus(Long.toString(nbaRequirement.getStatus()));
			reqInfo.setStatusDate(nbaRequirement.getCreatedDate());
			reqInfo.setRestrictIssueCode(nbaRequirement.getRestriction());
			reqInfo.setRequirementDetails(nbaRequirement.getMessage());
			reqInfo.setFormNo(nbaRequirement.getFormNumber()) ; //ALS4905
			reqInfo.setSequence(createRequirementSequence(anbaTXLife,partyId,nbaRequirement.getType()));//ALS4243
			reqInfo.setActionAdd();
			reqInfo.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO));
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			//Begin ALS4642
			if(nbaRequirement.getFollowUpFreq() == NbaConstants.LONG_NULL_VALUE) {
				Map deoinkMap = new HashMap(5,(float)0.9);
		    	deoinkMap.put(NbaVpmsConstants.A_REQ_TYPE_LOB, Long.toString(nbaRequirement.getType()));
		        deoinkMap.put(NbaVpmsConstants.A_REQ_VENDOR_LOB, nbaRequirement.getVendor());
		        deoinkMap.put(NbaVpmsConstants.A_ReqStatusLOB, Long.toString(nbaRequirement.getStatus()));
				reqInfoExt.setFollowUpFreq(getFollowUpDays(NbaVpmsAdaptor.EP_GET_FOLLOWUP_DAYS, deoinkMap));
			} else {//End ALS4642
				reqInfoExt.setFollowUpFreq(nbaRequirement.getFollowUpFreq());	
			}
			reqInfoExt.setFollowUpRequestNumber(0);
			reqInfoExt.setMedicalIndicator(nbaRequirement.isMedical());
			reqInfoExt.setReviewCode(Long.toString(nbaRequirement.getReview()));
			reqInfoExt.setCreatedDate(nbaRequirement.getCreatedDate());
			reqInfoExt.setBypassRedundancyInd(nbaRequirement.isBypassRedundancyInd());//QC16338/APSL4619
			reqInfoExt.setIGOInd(nbaRequirement.isIgoInd());//APSL4703
			reqInfoExt.setUwrequirementsInd(nbaRequirement.getUWRequirementInd()==1 ? true:false);//NBLXA186-NBLXA1271
			// NBLXA-1983 Begin
			List valueList = NbaUtils.getValRule(NbaConstants.VALRULE_BUNDLING_REQ).getValue();
			for (int j = 0; j < valueList.size(); j++) {
				if (reqInfo.getReqCode() == Long.parseLong(String.valueOf(valueList.get(j)))) {
					reqInfoExt.setReqBundleInd(true);
				}
			}
			// NBLXA-1983 End
			//begin NBA192
			TrackingInfo trackingInfo = new TrackingInfo();
			nbaOLifEId.setId(trackingInfo);
			trackingInfo.setReorderInd(nbaRequirement.isReorder());
			//Begin ALS2886
			NbaVpmsModelResult nbaVpmsModelResult = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_FOLLOWUP_PROVIDER);
			if (nbaVpmsModelResult.getVpmsModelResult() != null && nbaVpmsModelResult.getVpmsModelResult().getResultCount() > 0) {
				trackingInfo.setFollowUpServiceProvider(nbaVpmsModelResult.getVpmsModelResult().getResultAt(0));
			}
			//End ALS2886
			//APSL3447
			if(NbaUtils.isHVTReqForAXAProcFirm(anbaTXLife,nbaRequirement.getType()) 
					&& nbaRequirement.getVendor() != null && nbaRequirement.getVendor().equalsIgnoreCase(NbaConstants.PROVIDER_MANUAL)){ 
				trackingInfo.setFollowUpServiceProvider(nbaRequirement.getVendor());
			}
			//End APSL3447
			reqInfoExt.setTrackingInfo(trackingInfo);
			trackingInfo.setActionAdd();
			//end NBA192
		}
		lob.setReqType(reqType);
		lob.setReqRestriction(reqInfo.getRestrictIssueCode());//ALS5718
		return reqInfo;
	}
	/**
	 * Creates and returns the Physican Party Object for the Requirement.
	 * @param nbaReq the Requirement that the Physican is created for
	 * @param nbaOLifEId used to generate the ID for the Party object
	 * @return the newly created and initialized object
	 */
	//NBA130 New Method
	public static Party createDoctorForRequirement(NbaRequirement nbaReq, NbaOLifEId nbaOLifEId) {

		Party doctor = new Party();
		nbaOLifEId.setId(doctor);
		doctor.setPersonOrOrganization(new PersonOrOrganization());
		if (nbaReq.getDrPartyType() == NbaOliConstants.OLI_PT_PERSON) {//APSL5045
			doctor.setPartyTypeCode(NbaOliConstants.OLI_PT_PERSON);
			Person person = new Person();
			populateDoctorName(doctor, nbaReq, person);
			person.setActionAdd();
		} else {
			doctor.setPartyTypeCode(NbaOliConstants.OLI_PT_ORG);//APSL5045
			Organization organization = new Organization();
			populateDoctorName(doctor, nbaReq, organization);
			organization.setActionAdd();
		}

		/*
		 * Person person = new Person(); populateDoctorName(doctor, nbaReq, person); person.setActionAdd();
		 */
		doctor.setActionAdd();
		if (nbaReq.hasDrAddress()) {	//ALII1325
			Address address = new Address();
			nbaOLifEId.setId(address);
			populateDoctorAddress(nbaReq, address);
			address.setActionAdd();
			doctor.addAddress(address);
		}

		Phone phone = new Phone();
		nbaOLifEId.setId(phone);
		populateDoctorPhone(nbaReq, phone);
		phone.setActionAdd();
		doctor.addPhone(phone);
		
		return doctor;
	}
	
	/**
	 * Creates and returns the Physican Party Object for the Requirement.
	 * @param nbaReq the Requirement that the Physican is created for
	 * @param nbaOLifEId used to generate the ID for the Party object
	 * @return the newly created and initialized object
	 */
	//ALS2859 new method
	public static Party createDoctorForRequirement(NbaLob nbaLob, NbaOLifEId nbaOLifEId) throws NbaBaseException {

		Party doctor = new Party();
		nbaOLifEId.setId(doctor);
		doctor.setPersonOrOrganization(new PersonOrOrganization());
		doctor.setPartyTypeCode(NbaOliConstants.OLI_PT_PERSON);

		Person person = new Person();
		RequirementDoctorName doctorName = new RequirementDoctorName(nbaLob.getReqDrName());
		person.setFirstName(doctorName.getFirstName());
		person.setLastName(doctorName.getLastName());
		person.setMiddleName(doctorName.getMiddleName());
		doctor.getPersonOrOrganization().setPerson(person);
		doctor.setFullName(nbaLob.getReqDrName());
		person.setActionAdd();
		doctor.setActionAdd();
		return doctor;
	}
	/**
	* Create common RequirementInfo object to add to NbaTXLife.
	* @param aNbaTXLife the NbaTXLife object to use to generate
	*                   the RequirementInfo ID
	* @param relatedObjectID the Party object for the RequirementInfo
	* @param vpmsRequirement contains information needed to create the object
	* @return a RequirementInfo object
	*/
	//NBA130 New Method
	public RequirementInfo createNewRequirementInfoObject(NbaTXLife aNbaTXLife, String relatedObjectID, NbaVpmsRequirement vpmsRequirement, NbaUserVO user, NbaLob nbaLob) throws NbaBaseException {
	    RequirementInfo reqInfo = createNewRequirementInfoObject(aNbaTXLife, relatedObjectID, user, nbaLob); //NBA192
	    reqInfo.setRequirementDetails(vpmsRequirement.getComment()); //NBA192
	    //Begin NBA250
	    reqInfo.setFormNo(vpmsRequirement.hasFormNumber()? vpmsRequirement.getFormNumber(): ""); 
	    RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
	    TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();		
	    if(!NbaUtils.isHVTReqForAXAProcFirm(aNbaTXLife,nbaLob.getReqType())){ //APSL3447
		    if(vpmsRequirement.hasFollowupProvider()){
				trackingInfo.setFollowUpServiceProvider(vpmsRequirement.getFollowupProvider());
			}
			if(vpmsRequirement.hasProvider()){
				trackingInfo.setTrackingServiceProvider(vpmsRequirement.getProvider());
			}
	    }
		return reqInfo;
		//End NBA250
	}
	
	
	/**
	* Create common RequirementInfo object to add to NbaTXLife.
	* @param aNbaTXLife the NbaTXLife object to use to generate
	*                   the RequirementInfo ID
	* @param relatedObjectID the Party object for the RequirementInfo
	* @param vpmsRequirement contains information needed to create the object
	* @return a RequirementInfo object
	*/
	//NBA130 New Method
	public RequirementInfo createNewRequirementInfoObject(NbaTXLife aNbaTXLife, String relatedObjectID, NbaUserVO user, NbaLob nbaLob)
			throws NbaBaseException {
		NbaOLifEId nbaOLifEId = new NbaOLifEId(aNbaTXLife);
		RequirementInfo reqInfo = new RequirementInfo();
		reqInfo.setAppliesToPartyID(relatedObjectID);
		reqInfo.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO)); //SPR2992
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		//begin AXAL3.7.07
		if (nbaLob.getReqStatus() != null && nbaLob.getReqStatus().length() > 0) {
			reqInfo.setReqStatus(new Long(nbaLob.getReqStatus()).longValue()); // Requirement Status
			if (NbaOliConstants.OLI_REQSTAT_RECEIVED == NbaUtils.convertStringToLong(nbaLob.getReqStatus())) {
				reqInfo.setReceivedDate(new Date());//ALS3110
				reqInfoExt.setDisplayImagesInd(true);//ALS2204
				reqInfoExt.setReceivedDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
				reqInfoExt.setActionUpdate();
			}
		} else {
			reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_ORDER);
		}
		//end AXAL3.7.07
		reqInfo.setReqCode(nbaLob.getReqType());
		reqInfo.setFormNo(nbaLob.getFormNumber());//ALS4432
		reqInfo.setFormRecievedWithAppInd(nbaLob.getFormRecivedWithAppInd());//ALS5276
		if (!NbaUtils.isBlankOrNull(nbaLob.getProviderOrder())) { //ALII1818
			reqInfo.setProviderOrderNum(nbaLob.getProviderOrder());
		}
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		NbaRequirementsData requirementsData = (NbaRequirementsData) tableAccessor.getDataForOlifeValue(tableAccessor.setupTableMap(nbaLob),
				NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(reqInfo.getReqCode()));
		if (requirementsData == null) {
			throw new NbaDataAccessException("No data found for requirement " + reqInfo.getReqCode());
		}
		//Begin AXAL3.7.06
		getOinkDataAccess().setLobSource(nbaLob);
		getOinkDataAccess().setContractSource(aNbaTXLife);
		Map deOink = new HashMap();//ALS4322
		
		NbaRequirementUtils.deOinkEndorsementValues(deOink, aNbaTXLife);//ALS4322
		NbaRequirementUtils.deOinkImpairmentValues(deOink, aNbaTXLife, relatedObjectID);//ALS5385
		//Begin APSL4397
		Object[] relations = aNbaTXLife.getOLifE().getRelation().toArray();
		Relation relation = NbaUtils.getRelationForParty(relatedObjectID, relations);
		if(relation != null){
			deOink.put("A_RelationRoleCode", Long.toString(relation.getRelationRoleCode()));
		}
		//End APSL4397		
		NbaVpmsModelResult nbaVpmsModelReqOverrideResult = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_REQ_OVERRIDE_SETTINGS, deOink, user);//ALS4322 //ALS4666
		if (nbaVpmsModelReqOverrideResult.getVpmsModelResult() != null) {
			RequirementInfo overrideReqInfo = nbaVpmsModelReqOverrideResult.getVpmsModelResult().getRequirementInfoAt(0);
			if (overrideReqInfo.hasRestrictIssueCode()) {
				reqInfo.setRestrictIssueCode(overrideReqInfo.getRestrictIssueCode());
			} else {
				reqInfo.setRestrictIssueCode(requirementsData.getRestrictionCode());
			}
			RequirementInfoExtension overrideReqInfoExtn = overrideReqInfo.getOLifEExtensionAt(0).getRequirementInfoExtension();
			if (overrideReqInfoExtn.hasMedicalIndicator()) {
				reqInfoExt.setMedicalIndicator(overrideReqInfoExtn.getMedicalIndicator());
			} else {
				reqInfoExt.setMedicalIndicator(requirementsData.getMedicalTypeIndicator() == 1 ? true : false);
			}
			if (overrideReqInfoExtn.hasReviewCode()) {
				reqInfoExt.setReviewCode(overrideReqInfoExtn.getReviewCode());
				nbaLob.setReview(Integer.parseInt(overrideReqInfoExtn.getReviewCode()));//SR564247(APSL2525)-Full
			} else {
				reqInfoExt.setReviewCode(Long.toString(requirementsData.getReviewIndicator()));
				nbaLob.setReview((int)requirementsData.getReviewIndicator());//SR564247(ALII1579)
			}
			//Begin ALS4666
			if (overrideReqInfoExtn.getReviewedInd()) {
				reqInfoExt.setReviewedInd(overrideReqInfoExtn.getReviewedInd());
				reqInfoExt.setReviewID(user.getUserID());
				reqInfoExt.setReviewDate(new Date());
			}
			//End ALS4666
			//SC:NBLXA186-NBLXA1272
			reqInfoExt.setUwrequirementsInd(requirementsData.getUWRequirementInd()==1 ? true:false);
			//EC: NBLXA186-NBLXA1272
		}
		//End AXAL3.7.06
		//requirementsData.getReviewIndicator();
		// NBLXA-1983 Begin
		List valueList = NbaUtils.getValRule(NbaConstants.VALRULE_BUNDLING_REQ).getValue();
		for (int j = 0; j < valueList.size(); j++) {
			if (reqInfo.getReqCode() == Long.parseLong(String.valueOf(valueList.get(j)))) {
				reqInfoExt.setReqBundleInd(true);
			}
		}
		// NBLXA-1983 End
		NbaRequirementUtils.addProviderInfo(nbaLob.getReqVendor(), reqInfoExt);
		reqInfo.setUserCode(user.getUserID());
		reqInfo.setSequence(createRequirementSequence(aNbaTXLife, relatedObjectID, reqInfo.getReqCode()));
		//begin AXAL3.7.01
		if (nbaLob.getDeliveryReceiptSignDate() != null) {
			reqInfoExt.setDeliveryReceiptSignDate(nbaLob.getDeliveryReceiptSignDate());
		}
		reqInfoExt.setParamedSignedDate(nbaLob.getParamedSignDate());
		reqInfoExt.setSignDate(nbaLob.getReqSignDate());//APSL4872
		reqInfoExt.setPremiumDueCarrierReceiptDate(nbaLob.getReqReceiptDate());
		//end AXAL3.7.01
		reqInfoExt.setCreatedDate(new Date());
		reqInfo.setStatusDate(reqInfoExt.getCreatedDate());
		reqInfoExt.setAutoOrderIndicator(true);
		reqInfoExt.setFollowUpRequestNumber("0");
		reqInfoExt.setLabCollectedDate(nbaLob.getLabCollectionDate()); //NBLXA-1794
		reqInfo.setActionAdd();
		nbaOLifEId.setId(reqInfo);
		reqInfo.setRequirementInfoUniqueID(generateRequirementInfoUniqueID(aNbaTXLife, reqInfo));
		//begin NBA192
		getOinkDataAccess().setLobSource(nbaLob);
		NbaVpmsModelResult nbaVpmsModelResult = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_FOLLOWUP_PROVIDER);
		if (nbaVpmsModelResult.getVpmsModelResult() != null && nbaVpmsModelResult.getVpmsModelResult().getResultCount() > 0) {
			TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
			trackingInfo.setFollowUpServiceProvider(nbaVpmsModelResult.getVpmsModelResult().getResultAt(0));
			trackingInfo.setActionUpdate();
		}
		//end NBA192
		// Begin ALS2218
		String days = getFollowUpDays(NbaVpmsAdaptor.EP_GET_FOLLOWUP_DAYS, null);//ALS4642
		if (days != null) {
			reqInfoExt.setFollowUpFreq(days);
		}
		// end ALS2218
		//APSL3447
		if (nbaVpmsModelReqOverrideResult.getVpmsModelResult() != null) {
			RequirementInfo overrideReqInfo = nbaVpmsModelReqOverrideResult.getVpmsModelResult().getRequirementInfoAt(0);
			if(overrideReqInfo != null && overrideReqInfo.getOLifEExtensionAt(0) != null 
					&& overrideReqInfo.getOLifEExtensionAt(0).getRequirementInfoExtension() != null){
				RequirementInfoExtension overrideReqInfoExtn = overrideReqInfo.getOLifEExtensionAt(0).getRequirementInfoExtension();
				if(overrideReqInfoExtn.hasTrackingInfo()){
					TrackingInfo overridetrackingInfo = overrideReqInfoExtn.getTrackingInfo();
					if(overridetrackingInfo.hasTrackingServiceProvider() && overridetrackingInfo.hasFollowUpServiceProvider()){
						nbaLob.setReqVendor(overridetrackingInfo.getTrackingServiceProvider());
						TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
						trackingInfo.setTrackingServiceProvider(overridetrackingInfo.getTrackingServiceProvider());
						trackingInfo.setFollowUpServiceProvider(overridetrackingInfo.getFollowUpServiceProvider());
						trackingInfo.setActionUpdate();
					}					
				}	
			}
		}
		//end APSL3447
		return reqInfo;
	}
	/**
	 * Create the sequence number of the requirement type being added
	 * @param aNbaTXLife the NbaTXLife object to use to generate the RequirementInfo ID
	 * @param relatedObjectID the Party object for the RequirementInfo
	 * @param reqType contains the requirement type ID
	 * @return the sequence number to be stored
	 */
	//NBA130 New Method
	public static int createRequirementSequence(NbaTXLife aNbaTXLife, String partyID, long reqType){
	    int sequence = 0;
	    if (null == aNbaTXLife || null == partyID) {
	        return sequence;
	    }
	    Policy policy = aNbaTXLife.getPolicy();
	    int size = 	policy.getRequirementInfoCount();
		RequirementInfo reqInfo = null;
		for (int i=0; i<size; i++) {
			reqInfo = policy.getRequirementInfoAt(i);
			if (partyID.equals(reqInfo.getAppliesToPartyID()) && reqType == reqInfo.getReqCode()) {
				if (reqInfo.getSequence()>= sequence){
				    sequence = reqInfo.getSequence() + 1;
				}
			}
		}
	    
		return sequence;

	}
    /**
     * Get the value of reqVendorModel.
     * @return Returns the reqVendorModel.
     */
	//NBA138 New Method
    public static synchronized String getReqVendorModel() {
        return reqVendorModel;
    }
    /**
     * Set the value of reqVendorModel.
     * @param reqVendorModel The reqVendorModel to set.
     */
    //NBA138 New Method
    public static void setReqVendorModel(String reqVendorModel) {
        NbaRequirementUtils.reqVendorModel = reqVendorModel;
    }
    /**
     * Get the value of ReqVendorEntryPoint.
     * @return Returns the ReqVendorEntryPoint.
     */
    //NBA138 New Method
    public static synchronized String getReqVendorEntryPoint() {
        return reqVendorEntryPoint;
    }
    /**
     * Set the value of ReqVendorEntryPoint.
     * @param ReqVendorEntryPoint The ReqVendorEntryPoint to set.
     */
    //NBA138 New Method
    public static void setReqVendorEntryPoint(String reqVendorEntryPoint) {
        NbaRequirementUtils.reqVendorEntryPoint = reqVendorEntryPoint;
    }
    
    /**
     * Update Contract holding from requirement info object
     * @param nbaTXLife contract holding inquiry
     * @param nbaRequirement wrapper for current requirement
     */
    //NBA138 New Method
    protected static void updateRequirementInfo(NbaTXLife nbaTXLife, NbaRequirement nbaRequirement) {
		RequirementInfo reqInfo = nbaTXLife.getRequirementInfo(nbaRequirement.getRequirementInfoUniqueID());
        if (reqInfo != null) {
            RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
            if (reqInfoExt == null) {
                reqInfo.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO));
                reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
            }
            reqInfoExt.setFollowUpFreq(nbaRequirement.getFollowUpFreq());
            reqInfoExt.setParamedSignedDate(nbaRequirement.getParamedSignDate()); //ALS4364
            reqInfoExt.setSignDate(nbaRequirement.getSignDate()); //APSL4872
            reqInfoExt.setIsDueAmtByCheck(nbaRequirement.isDueAmtByCheck());//APSL3836
            reqInfoExt.setBypassRedundancyInd(nbaRequirement.isBypassRedundancyInd());//QC16338/APSL4619
            reqInfoExt.setIGOInd(nbaRequirement.isIgoInd());///APSL4703
            reqInfoExt.setManuallyUpdatedInd(nbaRequirement.isManuallyUpdateInd());//NBLXA-1324-NBLXA-1327
            //BEGIN: NBLXA-2328[NBLXA-2396]
            if (nbaRequirement.isReqNigoInd() && !NbaUtils.isBlankOrNull(nbaRequirement.getReqNigoReason())) {
            	reqInfoExt.setReqNigoInd(nbaRequirement.isReqNigoInd());
            	reqInfoExt.setReqNigoReason(nbaRequirement.getReqNigoReason());
            	reqInfoExt.setInvalidateFormInd(nbaRequirement.isInvalidateReq()); // NBLXA-2328[NBLXA-2434]
            }
            //END: NBLXA-2328[NBLXA-2396]
            //Start NBLXA-2133
            if (NbaOliConstants.OLI_REQCODE_POLDELRECEIPT == nbaRequirement.getType()) { 
				reqInfoExt.setDeliveryReceiptSignDate(nbaRequirement.getDeliveryReceiptSignDate());
			}
           //End NBLXA-2133
            if (nbaRequirement.getSatisfyComment()){
            	reqInfoExt.setUndnotesInd(nbaRequirement.getSatisfyComment());//NBLXA-1324-NBLXA-1327
            }else{
            	reqInfoExt.setUndnotesInd(nbaRequirement.isUndNotesInd());//NBLXA-1324-NBLXA-1327
            }
            reqInfoExt.setActionUpdate();
        }
    }
    /**
	 * Updates the Physican Party Object for the Requirement.
	 * @param nbaReq the Requirement that the Physican is created for
	 * @param nbaOLifEId used to generate the ID for the Party object
	 * 	 */
	//NBA130 New Method
	public static void updateDoctorForRequirement(Party doctor, NbaRequirement nbaReq, NbaOLifEId nbaOLifeId) {		
		NbaParty nbaParty = new NbaParty(doctor);
		if(nbaParty.isPerson()){
			Person person = nbaParty.getPerson();
			populateDoctorName(doctor, nbaReq, person);
			person.setActionUpdate();
			//ALS3872 code deleted
		} else {//ALS3872 begin
			Organization organization = nbaParty.getOrganization();
			populateDoctorName(doctor, nbaReq, organization);
			organization.setActionUpdate();
		}//ALS3872 end
		doctor.setActionUpdate();//ALS3872
		
		Address address = nbaParty.getAddress(NbaOliConstants.OLI_ADTYPE_BUS);
        if (null == address && nbaReq.hasDrAddress()) {	//ALII1325
            address = new Address();//ALS3131
        	nbaOLifeId.setId(address);
            address.setActionAdd();
            doctor.addAddress(address);
        } 
        // ALII1325 - deleted else block, putting address.setActionUpdate(); statement in below if for null check.  
        
        if ( address != null ){	//ALII1325
        	address.setActionUpdate();
    		populateDoctorAddress(nbaReq, address);        	
        }
		
		Phone phone = nbaParty.getPhone(NbaOliConstants.OLI_ADTYPE_BUS);
		if (null == phone){
		    phone = new Phone();
		    nbaOLifeId.setId(phone);
			phone.setActionAdd();
			doctor.addPhone(phone);
		}else{
		    phone.setActionUpdate();
		}		    
		populateDoctorPhone(nbaReq, phone);
	}
    /**
     * Poulate the Doctor Phone information
     * @param nbaReq
     * @param phone
     */
    //NBA130  New Method
    protected static void populateDoctorPhone(NbaRequirement nbaReq, Phone phone) {
        phone.setAreaCode(nbaReq.getDrPhoneAreaCode());
		phone.setDialNumber(nbaReq.getDrPhoneDialNumber());
		phone.setPhoneTypeCode(NbaOliConstants.OLI_ADTYPE_BUS);
    }
    /**
     * Populate the Doctor Address information 
     * @param nbaReq
     * @param address
     */
    //NBA130 New Method
    protected static void populateDoctorAddress(NbaRequirement nbaReq, Address address) {
        address.setAddressTypeCode(NbaOliConstants.OLI_ADTYPE_BUS);
		address.setLine1(nbaReq.getDrAddrLine1());
		address.setLine2(nbaReq.getDrAddrLine2());
		address.setCity(nbaReq.getDrAddrCity());
		address.setAddressStateTC(nbaReq.getDrAddrState());
		address.setZip(nbaReq.getDrAddrZip());
		//address.setAddressCountryTC(nbaReq.getDrAddrZipTC());  //ALII1325
		if( null != nbaReq.getDrAddrZipTC()) {
		    AddressExtension addrExt = NbaUtils.getAddressExtension(address);
		    if (null == addrExt) {
                address.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ADDRESS));
                addrExt = NbaUtils.getAddressExtension(address);
                //NBA130
                addrExt.setActionAdd();
            } else {
            	//NBA130
            	addrExt.setActionUpdate();
            }
			addrExt.setZipCodeTC(nbaReq.getDrAddrZipTC());
		}
    }
    /**
     * Populate the Doctor Name Information
     * @param doctor
     * @param nbaReq
     * @param person
     */
    //NBA130 New Method
    protected static void populateDoctorName(Party doctor, NbaRequirement nbaReq, Person person) {
        person.setFirstName(nbaReq.getDrFirstName());
		person.setMiddleName(nbaReq.getDrMiddleName());
		person.setLastName(nbaReq.getDrLastName());
		doctor.getPersonOrOrganization().setPerson(person);
		doctor.setFullName(NbaObjectPrinter.formatFullName(person.getFirstName(), person.getMiddleName(), person.getLastName()));
    }
    
    /**
     * Populate the Doctor Name Information when doctor is an entity
     * @param doctor
     * @param nbaReq
     * @param person
     */
    //ALS3872 new method
    protected static void populateDoctorName(Party doctor, NbaRequirement nbaReq, Organization organization) {
        organization.setDBA(nbaReq.getFullName());//APSL5045
		doctor.getPersonOrOrganization().setOrganization(organization);
		doctor.setFullName(organization.getDBA());
    }
    
    /**
     *Retrieves the followup days from vp/ms
     * @param entryPoint the VP/MS model's entry point
     * @return VP/MS results
     */
    // ALS2218	New Method 
    // ALS4642 Signature Changed
    protected String getFollowUpDays(String entryPoint, Map deOink) throws NbaBaseException {
        NbaVpmsAdaptor vpmsProxy = null; //SPR3362
    	try {
    		vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.REQUIREMENTS); 
    		vpmsProxy.setVpmsEntryPoint(entryPoint);
    		if(deOink == null) {//ALS4642
    			deOink = new HashMap();
        		deOink.put(NbaVpmsConstants.A_PROCESS_ID, "DUMMY");
        		deOink.put("A_XmlResponse", "true");
    		}
    		vpmsProxy.setSkipAttributesMap(deOink);
    		VpmsComputeResult compResult = vpmsProxy.getResults();
    		if( compResult.getReturnCode() != 0) {
    			return null;			
    		}
    		return compResult.getResult();
    		
    	} catch (java.rmi.RemoteException re) {
    		throw new NbaBaseException("RequirementsDetermination problem", re);
    	
    	} finally {
    	    try {
    	        if(vpmsProxy != null){
    	            vpmsProxy.remove();
    	        }
            } catch (RemoteException e) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
            }
    	}
    }
    
	/**
	 * This method gets all the deOink variables for VPMS model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//AXA3.7.06 //ALS4322 moved from ReqDet
	public static void deOinkEndorsementValues(Map deOink, NbaTXLife nbaTxLife) {
		Policy policy = nbaTxLife.getPolicy();
		Endorsement endorsement = null;
		EndorsementExtension endorsementExtension = null;
		List endorsementCodeList = new ArrayList();
		List criticalEndorsementIndList = new ArrayList(); //AXAL3.7.62
		int countAmendment = 0; //APSL554
		int count = policy.getEndorsementCount();
		for (int i = 0; i < count; i++) {
			endorsement = policy.getEndorsementAt(i);
			//begin APSL554
				// moved code to NBLXA-1430
			//end APSL554
			// begin AXAL3.7.62
			endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
			if (endorsementExtension != null) {
				if(NbaUtils.isAmendment(endorsement) && !endorsementExtension.getRequirementGeneratedInd()){ //SR566149 and SR519592
					countAmendment++;//SR566149 and SR519592
				}//SR566149 and SR519592
				endorsementCodeList.add(endorsementExtension.getEndorsementCodeContent()); //ALS2907
				criticalEndorsementIndList.add(Boolean.toString(endorsementExtension.getCriticalEndorsementInd()));
			} else {
				if (NbaUtils.isAmendment(endorsement)) {//NBLXA-1430
					countAmendment++;
				}
				endorsementCodeList.add(""); //ALS2907
				criticalEndorsementIndList.add(Boolean.toString(false));
			}
			// end AXAL3.7.62
		}
		deOink.put("A_EndorsementCodeList", endorsementCodeList.toArray(new String[endorsementCodeList.size()]));
		deOink.put("A_CriticalEndorsementIndList", criticalEndorsementIndList.toArray(new String[criticalEndorsementIndList.size()])); //AXAL3.7.62
		deOink.put("A_no_of_EndorsementsList", Integer.toString(count));
		deOink.put("A_no_of_Amendments", Integer.toString(countAmendment));//APSL554 - Generate "Amendment" requirement only if Amendment is present
		   																	// and no Endorsement
	}
	
	/**
	 * This method gets all the deOink variables for VPMS model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//ALS5385 New Method
	public static void deOinkImpairmentValues(Map deOink, NbaTXLife nbaTxLife, String partyId) {
		ArrayList impairments = nbaTxLife.getImpairments(partyId);
		List impairmentStatusList = new ArrayList(); 
	    deOink.put("A_no_of_Impairments" , String.valueOf(impairments.size()));
		for (int i = 0; i < impairments.size(); i++) {
			ImpairmentInfo impInfo = (ImpairmentInfo)impairments.get(i);
			impairmentStatusList.add(String.valueOf(impInfo.getImpairmentStatus()));
		}
			deOink.put("A_IMPAIRMENTSTATUS", impairmentStatusList.toArray(new String[impairmentStatusList.size()]));

	}
   
    //ALS2859 new class
	public static class RequirementDoctorName {
		String firstName;
		String lastName;
		String middleName;

    public RequirementDoctorName(String doctorName) {
			super();
			NbaStringTokenizer tokenizer = new NbaStringTokenizer(doctorName, ", ");
			if (tokenizer.hasMoreTokens()) {
				setLastName(tokenizer.nextToken());
			}
			if (tokenizer.hasMoreTokens()) {
				NbaStringTokenizer tokenizer1 = new NbaStringTokenizer(tokenizer.nextToken(), " ");
				if (tokenizer1.hasMoreTokens()) {
					setFirstName(tokenizer1.nextToken());
				}
				if (tokenizer1.hasMoreTokens()) {
					setMiddleName(tokenizer1.nextToken());
				}
			}
		}

		/**
		 * @return Returns the firstName.
		 */
		public String getFirstName() {
			return firstName;
		}

		/**
		 * @param firstName
		 *            The firstName to set.
		 */
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		/**
		 * @return Returns the lastName.
		 */
		public String getLastName() {
			return lastName;
		}

		/**
		 * @param lastName
		 *            The lastName to set.
		 */
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		/**
		 * @return Returns the middleName.
		 */
		public String getMiddleName() {
			return middleName;
		}

		/**
		 * @param middleName
		 *            The middleName to set.
		 */
		public void setMiddleName(String middleName) {
			this.middleName = middleName;
		}
	}
	
	   /**
	 * Determines if an incoming source is displayable and updates the requirement info extension
	 * @param permWorkItem
	 * @param requirementInfo
	 * @param holdingInq
	 * @param aSource
	 * @throws NbaBaseException
	 */
	//ALS4420 - Copied from NbaProcRequirementDetermination by ALS2544
	public static void markSourceDisplayable(RequirementInfo requirementInfo,NbaSource aSource, NbaDst nbaDst)
			throws NbaBaseException {
		if (isSourceDisplayable(aSource.getSourceType(),nbaDst)) {
			if (requirementInfo != null) {
				RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
				if (requirementInfoExt == null) {
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
					requirementInfoExt = olifeExt.getRequirementInfoExtension();
				}
				requirementInfoExt.setDisplayImagesInd(true);
				requirementInfoExt.setActionUpdate();
			}
		}
	}
	
    /**
     * Retrieve a map of displayable source types
     */
    //NBA208-36 - copied from NbaProcRequirementDetermination
	public static  Map getDisplayableSources(NbaDst nbaDst) throws NbaBaseException {
	    return  NbaUtils.getDisplayableImagesMap(nbaDst);
	}
	/**
	 * Determine if a source is displayable based on its type
	 * @param sourceType
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA208-36 - copied from NbaProcRequirementDetermination
	public static boolean isSourceDisplayable(String sourceType, NbaDst nbaDst) throws NbaBaseException {
		return getDisplayableSources(nbaDst).containsKey(sourceType);
	}
	
//	APSL1526 New Method
	public static boolean isMedicalInd(NbaTXLife txLife, NbaLob currentReq)
	{
		RequirementInfo reqInfo = txLife.getRequirementInfo(currentReq.getReqUniqueID());
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if (reqInfoExt !=null) {
			if (reqInfoExt.getMedicalIndicator()) {
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}
	/**
	 * NBLXA-2151
	 * @param requirementInfo
	 * @return
	 */
	public static boolean isMedicalInd(RequirementInfo reqInfo) {
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if (null != reqInfoExt) {
			return reqInfoExt.getMedicalIndicator();
		}
		return false;
	}
	//QC16338/APSL4619 New Method
	public static boolean isBypassRedundancyCheck(NbaTXLife txLife, NbaDst work) {
		String reqInfoUniqueId = work.getNbaLob().getReqUniqueID();
		RequirementInfo reqInfo = txLife.getRequirementInfo(reqInfoUniqueId);
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if (reqInfoExt != null) {
			if (reqInfoExt.getBypassRedundancyInd()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	//APSL4740 New method
	public static boolean retrieveHIPAAIgoInd(NbaDst nbaDst, NbaUserVO user) throws NbaBaseException {
		boolean hipaaIgoInd = true;
		NbaDst parentCase = nbaDst;
		if (!nbaDst.isCase()) {
			parentCase = retrieveParentCase(nbaDst, user, false);
		}
		if (NbaUtils.isEAPP(parentCase)) {
			return true;
		}
		if ((NbaConstants.A_WT_REQUIREMENT.equals(nbaDst.getWorkType()))
				&& (Long.toString(NbaOliConstants.OLI_REQCODE_1009800033).equals(Integer.toString(nbaDst.getNbaLob().getReqType())))) {
			int pageCount = NbaRequirementUtils.getHIPAAPageCount(nbaDst, user);
			if (pageCount < 2) {
				hipaaIgoInd = false;
			}
		}
		return hipaaIgoInd;
	}
	
	public static int getHIPAAPageCount(NbaDst nbaDst, NbaUserVO user) throws NbaBaseException {
		List sources = new ArrayList();
		NbaSource nbaSource = null;
		NbaDst workWithSources = null;
		int pageCount = 0;
		if (nbaDst != null) {
			workWithSources = retrieveWorkWithSources(nbaDst, user);
			sources = workWithSources.getNbaSources();
		}
		if(sources.size() > 0 ) {
			nbaSource = (NbaSource) sources.get(0);
		}
		if (nbaSource != null && !(NbaConstants.A_ST_INVALID_FORM.equals(nbaSource.getSourceType()))) {
			if ((NbaConstants.A_ST_MISC_MAIL.equals(nbaSource.getSourceType()) || NbaConstants.A_ST_PROVIDER_RESULT.equals(nbaSource.getSourceType()))
					&& Long.toString(NbaOliConstants.OLI_REQCODE_1009800033).equals(Integer.toString(nbaSource.getNbaLob().getReqType()))) {
				String fileName = nbaSource.getWorkItemSource().getFileName();
				if (!NbaUtils.isBlankOrNull(fileName)) {
					pageCount = retrievePageCount(fileName);
				}
			}
		}
		return pageCount;
	}
	
	//APSL4740 New Method
	protected static NbaDst retrieveParentCase(NbaDst dst, NbaUserVO user, boolean locked) {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(dst.getID(), false);
		retOpt.requestCaseAsParent();
		if (locked) {
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
		}
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult) ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		return (NbaDst) workResult.getFirst();
	}
	
	//APSL4740 New Method
	protected static NbaDst retrieveWorkWithSources(NbaDst dst, NbaUserVO user){
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (dst.isCase()) {
			retOpt.setWorkItem(dst.getID(), true);
			retOpt.requestSources();
		} else { // if a transaction
			retOpt.setWorkItem(dst.getID(), false);
			retOpt.requestSources();
		}
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		return (NbaDst) workResult.getFirst();
	}
	
	public static int retrievePageCount(String fileName) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int pageCount = 0;
		try {
			conn = NbaConnectionManager.borrowConnection("AWDTables");
			HashMap sqlKey = new HashMap();
			sqlKey.putAll(NbaConfiguration.getInstance().getDatabaseSqlKeys(NbaTableConstants.AWD_TABLES));
			String query = (String) sqlKey.get("FINDSOURCEPAGECOUNT");
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, fileName);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				pageCount = rs.getInt(1);
			}
		} catch (NbaBaseException nbae) {
			nbae.printStackTrace();
		} catch (Exception sqle) {
			String sqlMessage = sqle.getMessage();

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
		return pageCount;
	}
	
	//NBLXA-2072 new method
	public static void copyRiskClassifierData(NbaTXLife txLifeReqRslt, NbaTXLife txLife, String partyId) {
		ArrayList predictiveResultList = new ArrayList();
		int score = -1;
		String modelName = "";
		boolean rcPresent = false;
		if (txLife != null && txLifeReqRslt != null && txLifeReqRslt.getTransType() == NbaOliConstants.TC_TYPE_GENREQUIRERESTRN) {
			NbaParty insuredParty = txLife.getParty(partyId);
			NbaParty respInsuredParty = txLifeReqRslt.getParty(txLifeReqRslt.getPartyId(NbaOliConstants.OLI_REL_INSURED));
			if (respInsuredParty != null) {
				PartyExtension respInsuredPartyExt = NbaUtils.getFirstPartyExtension(respInsuredParty.getParty());
				if (respInsuredPartyExt != null) {
					List respPredictiveResult = respInsuredPartyExt.getPredictiveResult();
					if (respPredictiveResult != null && respPredictiveResult.size() > 0) {
						score = ((PredictiveResult) respPredictiveResult.get(0)).getScore();
						modelName = ((PredictiveResult) respPredictiveResult.get(0)).getModelName();
						rcPresent = true;
					}
					if (insuredParty != null) {
						PartyExtension insuredPartyExtension = NbaUtils.getFirstPartyExtension(insuredParty.getParty());
						NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife);
						PredictiveResult predResult = new PredictiveResult();
						predResult.setCreateTime(new NbaTime(new Date()));
						predResult.setResponseTime(new NbaTime(new Date()));
						predResult.setScore(score);
						predResult.setModelName(modelName);
						nbaOLifEId.setId(predResult);
						int msgCount = txLifeReqRslt.getPrimaryHolding().getSystemMessageCount();
						for (int k = 0; k < msgCount; k++) {
							SystemMessage sysMsg = txLifeReqRslt.getPrimaryHolding().getSystemMessageAt(k);
							String carrierAdminSys = sysMsg.getCarrierAdminSystem();
							if (carrierAdminSys.equalsIgnoreCase(NbaConstants.RSKCLASS)
									|| carrierAdminSys.equalsIgnoreCase(NbaConstants.PROCESSING_CODE_LN)) {
								Message aMessage = new Message();
								aMessage.setOriginator(sysMsg.getCarrierAdminSystem());
								aMessage.setReason(sysMsg.getMessageDescription());
								aMessage.setActionAdd();
								predResult.getMessage().add(aMessage);
								rcPresent = true;
							}
						}
						if (rcPresent) {
							predResult.setActionAdd();
							if (insuredPartyExtension.getPredictiveResultCount() > 0) {
								predictiveResultList = insuredPartyExtension.getPredictiveResult();
							}
							predictiveResultList.add(predResult);
							insuredPartyExtension.setPredictiveResult(predictiveResultList);
							insuredPartyExtension.setActionUpdate();
						}
					}
				}
			}
		}
	}
	// Begin NBLXA-1895
	public static Party createOrgForRequirement(NbaLob nbaLob, NbaOLifEId nbaOLifEId) {
		Party doctor = new Party();
		nbaOLifEId.setId(doctor);
		doctor.setPersonOrOrganization(new PersonOrOrganization());
		doctor.setPartyTypeCode(NbaOliConstants.OLI_PT_ORG);
		Organization organization = new Organization();
		organization.setDBA(nbaLob.getEntityName());
		doctor.setFullName(organization.getDBA());
		doctor.getPersonOrOrganization().setOrganization(organization);
		organization.setActionAdd();
		doctor.setActionAdd();
		return doctor;
	}
	// End NBLXA-1895
	
	//NBLXA-2184 - Begin
	public static boolean isCIPHighRiskCVPresent(NbaTXLife txLife,Party party){
		if(txLife != null){
			int CIPHighRiskMessageCode = 6987;
			List<SystemMessage> systemMessageList=txLife.getPrimaryHolding().getSystemMessage();
			if(systemMessageList != null && !systemMessageList.isEmpty()){
				for(SystemMessage sysMsg : systemMessageList){
					if(party.getId().equalsIgnoreCase(sysMsg.getRelatedObjectID())){
						int msgCode=sysMsg.getMessageCode();
						if(msgCode == CIPHighRiskMessageCode){
							return true;
						}
					}
				}
			}	
		}
		return false;
	}
	//NBLXA-2184 - End
	
	//NBLXA-2184 New Method	
	public static void copyProductReferenceNumber(NbaTXLife txLifeReqRslt, NbaTXLife txLife, String partyId) {
		if (txLife != null && txLifeReqRslt != null) {
			NbaParty insuredParty = txLife.getParty(partyId);
			RequirementInfo reqInfo =  txLife.getRequirementInfo(insuredParty, NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER);
			
			List resReqList = txLifeReqRslt.getRequirementInfoList(NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER);
			RequirementInfo resReqInfo = (RequirementInfo) resReqList.get(0);
			RequirementInfoExtension resReqInfoExt = NbaUtils.getFirstRequirementInfoExtension(resReqInfo);
			String productReference = null;
			if (!NbaUtils.isBlankOrNull(resReqInfoExt)) {
				productReference = resReqInfoExt.getProductReference();
			}
			
			if(!NbaUtils.isBlankOrNull(productReference)){
				RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				if (!NbaUtils.isBlankOrNull(reqInfoExt)) {
					reqInfoExt.setProductReference(productReference);
				}
			}
		}
	}
}
