package com.csc.fsg.nba.business.process;

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
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.axa.fsg.nba.foundation.AxaConstants;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.util.SortingHelper;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.database.AxaRulesDataBaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaFormsValidationData;
import com.csc.fsg.nba.tableaccess.NbaRequirementsData;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableHelper;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaTransactionSearchResultVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.ContractChangeInfo;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.EndorsementExtension;
import com.csc.fsg.nba.vo.txlife.ExpenseNeedTypeCodeCC;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormInstanceExtension;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.Intent;
import com.csc.fsg.nba.vo.txlife.IntentExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.MedicalCertification;
import com.csc.fsg.nba.vo.txlife.MedicalCondition;
import com.csc.fsg.nba.vo.txlife.MedicalConditionExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SignatureInfoExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsRequirement;
import com.csc.fsg.nba.vpms.NbaVpmsRequirementsData;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.Messages;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * NbaProcRequirementsDetermination is the class that processes nbAccelerator
 * cases found on the AWD Requirements Determination queue (NBREQDET).
 * It invokes the VP/MS Requirements Determination model to get the requirements for the case.
 * <p>The NbaProcRequirementsDetermination class extends the NbaAutomatedProcess class.  
 * Although this class may be instantiated by any module, the NBA polling class 
 * will be the primary creator of objects of this type.
 * <p>When the polling process finds a case in the Requirements Determination queue, 
 * it will create an object of this instance and call the object's 
 * executeProcess(NbaUserVO, NbaDst) method.
 * This method will manage the steps necessary to submit a case to a VP/MS model to 
 * determine if any requirements are to be generated for the case.
 * SPR2399 has removed audit history of NBA004, NBA007, NBA020, NBA035, NBA010, NBA093,
 * NBA097, ACN012, SPR1146, SPR1227, SPR1359, SPR1375, SPR1720, SPR1855 and SPR1364.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>General code clean-up</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>ACN008</td><td>Version 4</td><td>Underwriting Workflow Changes</td></tr>
 * <tr><td>ACP019</td><td>Version 4</td><td>(DTS)Requirement Determination Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>Requirement Control Source migration Changes</td></tr>
 * <tr><td>SPR1753</td><td>Version 5</td><td>Automated Underwriting and Requirements Determination Should Detect Severe Errors for Both AC and Non - AC</td></tr>
 * <tr><td>SPR2399</td><td>Version 5</td><td>Requirements are not getting generated properly on Primary Insured.</td></tr>
 * <tr><td>NBA119</td><td>Version 5</td><td>Automated Process Suspend</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2544</td><td>Version 6</td><td>Duplicate Requirements get generated for REEVAL workitem.</td></tr>
 * <tr><td>SPR2199</td><td>Version 6</td><td>P&R Requirements Merging Logic Needs to Change to Not Discard some Requirements</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement Reinsurance Project</td></tr>
 * <tr><td>SPR2697</td><td>Version 6</td><td>Requirement Matching Criteria Needs to Be Expanded</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR2742</td><td>Version 7</td><td>Remove OINK qualifiers INS, PINS and ANN from requirements determination processing</td></tr>
 * <tr><td>SPR3329</td><td>Version 7</td><td>Prevent erroneous "Retrieve variable name is invalid" messages from being generated by OINK</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3413</td><td>Version 7</td><td>Requirements Determination generates NbaDataAccessException</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation</td></tr>
 * <tr><td>AXAL3.7.06</td><td>AXA Life Phase 1</td><td>Requirement Determination</td></tr>
 * <tr><td>NBA250</td><td>AXA Life Phase 1</td><td>nbA Requirement Form and Producer Email Management Project</td></tr>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td<</tr>
 * <tr><td>AXAL3.7.62</td><td>AXA Life Phase 1</td><td>Amendments Endorsements</td></tr>
 * <tr><td>ALS2375</td><td>AXA Life Phase 1</td><td>QC #1217 - XML missing RequirementInfo.ReceivedDate</td></tr>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * <tr><td>ALS2907</td><td>AXALife Phase 1</td><td>EndosementCode to EndorsementExtension.EndorsementCodeContent</td></tr>
 * <tr><td>P2AXAL018</td><td>AXA Life Phase 2</td><td>Ommission Requirements</td></tr>
 * <tr><td>P2AXAL040</td><td>AXA Life Phase 2</td><td>Term Conversion AXA</td></tr>
 * <tr><td>A2_AXAL002</td><td>AXA Life New App A2</td><td>Requirement Determination for Owner </td></tr>
 * <tr><td>SR534326</td><td>Discretionary</td><td>Unpaid Reissue - Premium Due Requirements</td></tr>
 * <tr><td>SR566149 and SR519592</td><td>Discretionary</td><td>Reissue and Delivery Requirement Follow Up</td></tr>
 * <tr><td>SR514766</td><td>Discretionary</td><td>Premium Quote</td></tr>
 * <tr><td>P2AXAL054</td><td>AXA Life Phase 2</td><td>Omissions and Contract Validations</td></tr>
 * <tr><td>CR1453745</td><td>Discretionary</td><td>Revised Illustration Indicator</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 * @see NbaAutomatedProcess
 */

public class NbaProcRequirementsDetermination extends NbaAutomatedProcess {
	// NBA006 code deleted
	/** NbaOinkDataAccess object, which allows information to be retrieved from NbaTxlife or NbaLob object using pre-defined variables */
	protected NbaOinkDataAccess oinkDataAccess = null; // NBA008

	/** The NbaTableAccessor object, which is the interface for all table access */
	protected NbaTableAccessor tableAccessor; // SPR1018

	/** The NbaOinkRequest object provides a container for the variable name and its values. */
	protected NbaOinkRequest oinkRequest; //NBA008

	protected NbaDst parentCase; //ACN008

	private String strDBA = ""; //ACP019

	private String strAgent = ""; //ACP019

	private String strRelInsOwn = ""; //ACP019

	//SPR2742 code deleted
	protected int iterationCount = 0; //SPR1753

	protected List messages = new ArrayList(); //SPR2399

	protected boolean requirementsGenerated = false; //SPR2399

	protected boolean debugLogging = false; //SPR2399

	protected boolean preventProcess = false; //AXAL3.7.40G

	//protected ArrayList ownerReqs = null; //ALS1929,ALS2305
	
	protected ArrayList miscMailSourceList = new ArrayList();	

	protected boolean OwnOtherThenIns = false;
	
	protected List filteredRequirementsForJointAndReplacement = new ArrayList();//APSL3099
	
	protected List filteredFormRequirementsForJointAndReplacement = new ArrayList();//APSL3099
	
	protected Party termConvPertainsTo = null;//APSL3099
	
	protected Party replacementPertainsTo = null;//APSL3099
	
	protected boolean indForEOLI = false; //APSL5177
	
	protected ArrayList retrivedWorkItemsList = new ArrayList(); //NBLXA-2119
	protected ArrayList matchingWorkItemsList = new ArrayList(); //NBLXA-2119
	private static final String A_FORMNUMBERLIST = "A_FormNumberList";//NBLXA-2184
	private static final String A_FORMNUMBERLISTSIZE = "A_FormNumberListSize";//NBLXA-2184
	
	/**
	 * This constructor calls the superclass constructor which will set
	 * the appropriate statues for the process.
	 */
	public NbaProcRequirementsDetermination() {
		super();
	}

	/**
	 * This method drives the requirements determination process.
	 * After obtaining a reference to the <code>NbaNetServerAccessor</code> EJB, 
	 * it retrieves the Holding Inquiry and uses it to update the case's LOB fields.
	 * Those LOB fields are then used to instantiate the <code>NbaVpmsVO<code>.
	 * Then the XML103 object is retrieved and it and the holding inquiry are 
	 * linked to the NbaVpmsVO object.  Requirements are created from a 
	 * VP/MS model and an <code>NbaVpmsRequirementsData</code> 
	 * object contains the results of the VP/MS invocation.
	 * <p>An <code>NbaAutomatedProcessResult</code> object is created for
	 * return to the polling process and the status of the case is changed to move 
	 * the case to the next queue. Finally, the changes are committed to AWD.
	 * @param user the user for whom the work was retrieved
	 * @param work the AWD case to be reviewed
	 * @return NbaAutomatedProcessResult the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// NBA027 - logging code deleted
		try{
		if (!initialize(user, work)) {
			return getResult(); // NBA050
		}
		//begin SPR2399
		debugLogging = getLogger().isDebugEnabled(); // NBA027
		if (debugLogging) { // NBA027
			getLogger().logDebug("Requirements started Determination for contract " + getWork().getNbaLob().getPolicyNumber());
		} // NBA027
		String riskRighterCaseStatus=getWork().getNbaLob().getStatus();//APSL3426
		boolean isTransaction = work.isTransaction(); //ACN008
		retreiveWorkFromAWD(); //retrieve all workitem and sources from AWD
		prepareForProcessing();
		initializeMiscMailSourceList(miscMailSourceList);	//ALS2584
		//Begin ALS4054
		AxaPreventProcessVpmsData preventProcessData = new AxaPreventProcessVpmsData(user, getNbaTxLife(), getVpmsModelToExecute());
		if (preventProcessData.isPreventsProcess()) {
			addComment(preventProcessData.getComments());
			// Begin NBLXA-1288 
			if (!isTransaction) {
				List<Activity> activity = NbaUtils.getActivityByTypeCodeAndStatus(getNbaTxLife(), NbaOliConstants.OLI_ACTTYPE_PREVENT_PROCESS,
						NbaOliConstants.OLI_ACTSTAT_COMPLETE);
				if (activity.size() == 0) {
					NbaUtils.addPreventProcessActivity(nbaTxLife, user.getUserID(), NbaOliConstants.OLI_ACTTYPE_PREVENT_PROCESS);
				}
			}
		// End NBLXA-1288
			
		preventProcess = true;
		}//End ALS4054
		//Start APSL4980 Requirement Bundling
		else{
			retrieveAndSetInitialReviewEndDateOfRequirements();
		}
		//End APSL4980
		// Start NBLXA-2119
		if (!nbaTxLife.isUnderwriterApproved()) { //NBLXA-2579
			updateActivationDateForMatchingWI();//NBLXA2450[NBLXA2328]
		}
		// End NBLXA-2119			
		determineRequirements(); //SPR2742
		addComentsFromModel(messages); //SPR1753
		///SPR3413 code deleted
		
		//QC8185 - Code Deleted - Removed the removeMiscMailSourceForCreateStation() method call. The method logic is merged into initializeMiscMailSourceList() method.
		if(preventProcess != true) {//QC8185
			createMiscMailTransactions();	//ALS2584	
		}
		
		setWork(update(getWork())); //SPR1851

		if (requirementsGenerated) {
			updateRequirementControlSources();
			//SPR3413 code deleted
			result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
			if (debugLogging) {
				getLogger().logDebug("Requirements Determination completed. Adding new requirements.");
			}
		} else {
			result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "No Requirements Generated", getPassStatus());
			if (debugLogging) {
				getLogger().logDebug("Requirements Determination completed. No requirements Generated.");
			}
		}
		updateAmendment(); // SR566149 and SR519592
		updateLastReqIndicatorForOtherParties();//NBLXA186-NBLXA1272
		doContractUpdate(nbaTxLife); //SPR3413
		//ACN008 Begin		
		if (isTransaction) {
			updateReEvaluateWorkItem();
		} else {
			// Start APSL3426
			boolean riskRighterExecuted = false;
			ApplicationInfo appinfo=null;
			if(nbaTxLife.getPolicy()!=null){
				appinfo=nbaTxLife.getPolicy().getApplicationInfo();	
			}
			if (appinfo!=null && appinfo.getApplicationType()==NbaOliConstants.OLI_APPTYPE_TRIAL) {
				String routingReason =null;
				int aPSPageCount = getAPSRecivedWithAppPageCount(getWork(), A_WT_MISC_MAIL);//APSL3701
				if (aPSPageCount > 0) {
					List resultData = getRiskRighterStatus(getWork().getNbaLob(), aPSPageCount,riskRighterCaseStatus);
					
					if (!resultData.isEmpty()) {
						String retailRiskRighterStatus = (String) resultData.get(0);
						if (getWork().getNbaLob().getDistChannel() == NbaOliConstants.OLI_DISTCHNNL_BD && resultData.size()>1) {
							routingReason = (String) resultData.get(1);
						}else
						{
							routingReason = getRouteReason();
						}
						if (!NbaUtils.isBlankOrNull(retailRiskRighterStatus)) {
							changeStatus(retailRiskRighterStatus, routingReason);
							riskRighterExecuted = true;
						}
					}
				}
			if (!riskRighterExecuted) {
			    aPSPageCount = getAPSRecivedWithAppPageCount(getWork(), A_WT_REQUIREMENT); //APSL3701
				if (aPSPageCount > 0) {
					List resultData = getRiskRighterStatus(getWork().getNbaLob(), aPSPageCount,riskRighterCaseStatus);
					if (!resultData.isEmpty()) {
						
						String wholesaleRiskRighterStatus = (String) resultData.get(0);
						
						if (getWork().getNbaLob().getDistChannel() == NbaOliConstants.OLI_DISTCHNNL_BD && resultData.size()>1) {
							routingReason = (String) resultData.get(1);
						}else
						{
							routingReason = getRouteReason();
						}
							if (!NbaUtils.isBlankOrNull(wholesaleRiskRighterStatus)) {
								changeStatus(wholesaleRiskRighterStatus, routingReason);
								riskRighterExecuted = true;
							}
						
					}
				}
			 }
			}
		  if (!riskRighterExecuted) {
				changeStatus(getPassStatus(), getRouteReason()); // ALS5260, ALS4842
			}// End APSL3426

		}
		//ACN008 End         
		isReqOutofSynch(); //NBLXA-2241
		doUpdateWorkItem();
		// end NBA008
		return result;
		//end SPR2399
		// begin APSL5055-NBA331.1
		} catch (NbaBaseException e) {
			unlockParentWork();
			throw e;
		} catch (Throwable e) {
			unlockParentWork();
			throw new NbaBaseException(e);
		}
		// end APSL5055-NBA331.1
		finally {
			setWork(getOrigWorkItem()); //APSL4376
		}
	}
	
	
	//New Method APSL4980 Requirement Bundling
	/**
	 * @Purpose This method is used to get the next 3 days/date from the current day/date and check if any weekend is coming then bypass the same   
	 * @return Calendar : expected date(next 3 days/date)
	 */
	public static final Integer NUM_BUSINESS_DAYS = 3; 
	private Calendar add72HrsToCurrentTime()
	{
		Calendar date = Calendar.getInstance();
		final Integer numBusinessDays = NUM_BUSINESS_DAYS;//3 days
	    if (date == null || numBusinessDays == null || numBusinessDays.intValue() == 0){
	        return date;
	    }
	    final int numDays = Math.abs(numBusinessDays.intValue());
	    final int dateAddition = numBusinessDays.intValue() < 0 ? -1 : 1;//if numBusinessDays is negative
	    int businessDayCount = 0;
	    while (businessDayCount < numDays)
	    {
	    	date.add(Calendar.DATE, dateAddition);
	        //check for weekend
	        if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
	        	continue;//adds another day}
	        }
	        businessDayCount++;
	    }
	    date.setLenient(false);
	    return date;
	}
	
	
//	SR514766 New Method
	/**
	 * Set Illustration Indicator 
	 * Xpression will be modified to generate Premium quote/ Signed Illustration if the indicator is TRUE 
	 */
	public void setIllustrationIndicator(NbaTXLife nbaTxLife, NbaDst nbaDst,List requirementList) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaDst.getNbaLob());
			Map deOink = new HashMap(2,1);
			String productCode = nbaTxLife.getPolicy().getProductCode();
			deOink.put("A_ProductCode",productCode);
			deOink.put("A_ReqFromDataBase", requirementList.toArray(new String[requirementList.size()]));
			deOink.put("A_ReqFromDataBaseLength", String.valueOf(requirementList.size()));
			nbaOinkDataAccess.setContractSource(nbaTxLife);
			nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.ACREQUIREMENTSDETERMINATION);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_ILLUSTRATIONIND); 
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {					
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getNbaHolding().getApplicationInfo());
					appInfoExt.setIllustrationInd((NbaConstants.TRUE == Integer.parseInt((String) nbaVpmsResultsData.getResultsData().get(0))));
					appInfoExt.setActionUpdate();
				}
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException t) {
					getLogger().logError(t);
				}
			}
		}
	}

	/**CR1453745 - New Method
	 * Set Revised Illustration Indicator 
	 * Xpression will be modified to generate Premium quote/ Signed Illustration if the indicator is TRUE 
	 */
	public void setRevisedIllustrationIndicator(NbaTXLife nbaTxLife, NbaDst nbaDst,List requirementList) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaDst.getNbaLob());
			Map deOink = new HashMap(2,1);
			String productCode = nbaTxLife.getPolicy().getProductCode();
			deOink.put("A_ProductCode",productCode);
			deOink.put("A_ReqFromDataBase", requirementList.toArray(new String[requirementList.size()]));
			deOink.put("A_ReqFromDataBaseLength", String.valueOf(requirementList.size()));
			nbaOinkDataAccess.setContractSource(nbaTxLife);
			nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.ACREQUIREMENTSDETERMINATION);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_REVISEDILLUSTRATIONIND); 
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {					
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getNbaHolding().getApplicationInfo());
					appInfoExt.setRevisedIllustrationInd((NbaConstants.TRUE == Integer.parseInt((String) nbaVpmsResultsData.getResultsData().get(0))));
					appInfoExt.setActionUpdate();
				}
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException t) {
					getLogger().logError(t);
				}
			}
		}
	}

	/**
	 * Iterate through all insurured or annutant on the contract to determine requirements.
	 */
	//SPR2742 New Method //ALS1929,ALS2305
	protected void determineRequirements() throws NbaBaseException {
		//APSL3099 Begin
		if(NbaUtils.isTermConvOPAICase(getNbaTxLife())){
			termConvPertainsTo = getTermConversionPertainsTo();			
		}
		if(getNbaTxLife().isReplacement()){
			replacementPertainsTo = getReplacementPertainsTo();
		}
		//APSL3099 End
		OLifE olife = getNbaTxLife().getOLifE();
		int partyCount = olife.getPartyCount();
		for (int index = 0; index < partyCount; index++) {
			Party party = olife.getPartyAt(index);
			if (getNbaTxLife().isInsured(party.getId()) || getNbaTxLife().isAnnuitant(party.getId())
					|| (getNbaTxLife().isOwner(party.getId()) && !getNbaTxLife().isOwnerSameAsInsured())) {//P2AXAL054
				processPartyForRequirements(party);				
			}
		}
	}
	// New method APSL3099
	// Returns Party Object who initiated term conversion
	protected Party getTermConversionPertainsTo(){
		Party pertainsToParty = null;
		List pertainsTo = NbaUtils.getTermConvPertainsTo(getNbaTxLife());
		if(!NbaUtils.isBlankOrNull(pertainsTo)){
			pertainsToParty = (Party)pertainsTo.get(0);
		}
		return pertainsToParty;
	}
	
	// New method APSL3099
	// Returns Party Object who initiated Replacement
	protected Party getReplacementPertainsTo(){
		Party pertainsToParty = null;
		List pertainsTo = NbaUtils.getReplacementPertainsTo(getNbaTxLife());
		if(!NbaUtils.isBlankOrNull(pertainsTo)){
			pertainsToParty = (Party)pertainsTo.get(0);
		}
		return pertainsToParty;
	}
	//ALS1929,ALS2305 code deleted

	/**
	 * Creates keys for acdb lookup
	 * @param party the Party object
	 * @return
	 */
	//ACN010 New Method
	//SPR2742 added Parameter party
	private Object[] getKeys(Party party) {
		Object[] keys = new Object[4];
		//begin SPR2742
		keys[0] = party.getId();
		NbaLob lob = getWorkLobs();
		keys[1] = lob.getPolicyNumber();
		keys[2] = lob.getCompany();
		keys[3] = lob.getBackendSystem();
		//end SPR2742
		return keys;
	}

	/**This method merges the Requirements,   
	 * @param newReq is the list which contains the new requirements returned by the model.
	 * @param party the Party object
	 * @return List Containing the merged requirements.
	 */
	// ACN016 New Method
	// SPR2544 changed signature. Changed return type from ArrayList to List , Changed parameter from ArrayList to List
	//SPR2199 changed method name to determineNewRequirements and method visibility to protected
	//SPR2742 added Parameter party
	//AXAL3.7.40 Change Party to partyId, ALS1929,ALS2305 reverted
	protected List determineNewRequirements(List newReq, Party party) throws NbaBaseException {
		List reqList = new ArrayList();
		List arrOldReqForIns = getRequirementInfoForInsured(party); //SPR2544 SPR2742 AXAL3.7.40		
		//SPR2544 deleted Code	 
		reqList = new NbaRequirementMerger(getUser(), getNbaTxLife()).determineNewRequirements(arrOldReqForIns, newReq); //SPR2199 //ALS3963 //ALS4082
		if((termConvPertainsTo != null && replacementPertainsTo != null) && !termConvPertainsTo.getId().equalsIgnoreCase(replacementPertainsTo.getId())){//APSL3099
			// If term conversion and replacement is initiated by Primary and Joint Insured or vice versa, 
			//then no need to filter requirements.
			return reqList;
		}
		else{
			if(termConvPertainsTo != null){
				if((getNbaTxLife().isJointInsured(termConvPertainsTo.getId()) && getNbaTxLife().isPrimaryInsured(party.getId())) ||
					(getNbaTxLife().isPrimaryInsured(termConvPertainsTo.getId()) && getNbaTxLife().isJointInsured(party.getId()))	
				){
					reqList = filterRequirementsForJointAndReplacement(reqList);
			    }
			}else if(replacementPertainsTo != null){
				if((getNbaTxLife().isJointInsured(replacementPertainsTo.getId()) && getNbaTxLife().isPrimaryInsured(party.getId())) ||
						(getNbaTxLife().isPrimaryInsured(replacementPertainsTo.getId()) && getNbaTxLife().isJointInsured(party.getId()))	
					){
						reqList = filterRequirementsForJointAndReplacement(reqList);
				    }
				}
		}
		
		
		return reqList;
	}

//	APSL3099 New Method
	protected List filterRequirementsForJointAndReplacement(List reqList) {
		List filteredList = new ArrayList();//APSL3329
		if (!reqList.isEmpty()) {
			if (reqList.get(0) instanceof RequirementInfo) {
				for (int x = 0; x < reqList.size(); x++) {
					RequirementInfo rInfo = (RequirementInfo) reqList.get(x);
					long reqCode = rInfo.getReqCode();
					if (reqCode == NbaOliConstants.OLI_REQCODE_REPLETTER || reqCode == NbaOliConstants.OLI_REQCODE_644
							|| reqCode == NbaOliConstants.OLI_REQCODE_700) {
						filteredRequirementsForJointAndReplacement.add(rInfo);
					}else{
						filteredList.add(rInfo);//APSL3329
					}
				}
			} else if (reqList.get(0) instanceof NbaVpmsRequirement) {

				for (int x = 0; x < reqList.size(); x++) {
					NbaVpmsRequirement rInfo = (NbaVpmsRequirement) reqList.get(x);
					long reqCode = rInfo.getType();
					if (reqCode == NbaOliConstants.OLI_REQCODE_REPLETTER || reqCode == NbaOliConstants.OLI_REQCODE_644
							|| reqCode == NbaOliConstants.OLI_REQCODE_700) {
						filteredRequirementsForJointAndReplacement.add(rInfo);
					}else{
						filteredList.add(rInfo);//APSL3329
					}
				}

			}
		}

		return filteredList;//APSL3329
	}
	
	/**
	 * Create and initialize an <code>NbaVpmsResultsData</code> object to find matching work items.
	 * @param entryPoint the VP/MS model's entry point
	 * @return NbaVpmsResultsData the VP/MS results
	 * @throws NbaBaseException
	 */
	// NBA008 New Method
	protected NbaVpmsResultsData getDataFromVpmsModelRequirements(String entryPoint) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			Map deOink = new HashMap();
			deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			deOink.put("A_XMLResponse", "false"); //ACN014
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			//SPR3362 code deleted
			return vpmsResultsData;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("RequirementsDetermination problem", re);
			//begin SPR3362
		} finally {
			try {
				if (vpmsProxy != null) { //APSL588
					vpmsProxy.remove();
				} //APSL588
			} catch (RemoteException e) {
				getLogger().logError(e);
			}
			//end SPR3362      
		}

	}

	/**
	 * Create and initialize an <code>NbaVpmsRequirementsData</code> object to find matching work items.
	 * @param entryPoint the VP/MS model's entry point
	 * @param deOink The Skip Attribute map for Vpms Model
	 * @param Party the Party object
	 * @return NbaVpmsRequirementsData the VP/MS results
	 * @throws NbaBaseException
	 */
	// NBA008 New Method
	// SPR2506 added Parameter to the method 
	//SPR2742 added Parameter party
	//A2_AXAL002,ALS1929,ALS2305
	protected NbaVpmsRequirementsData getDataFromVpmsModelRequirementsDetermination(String entryPoint, Map deOink, Party party) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; //SPR2199
		try {
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, getVpmsModelToExecute());//ACN008 SPR2199
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			//begin ACP019
			//SPR2506 code deleted
			deOink.put("A_AgentLicNum_SAG", strAgent);
			deOink.put("A_DBA_SAG", strDBA);
			deOink.put("A_ReltoAnnOrIns_OWN", strRelInsOwn);
			deOink.put("A_IterationCount", Integer.toString(getNextIterationCount())); //SPR1753
			//end ACP019
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink); //ACP019
			//begin ACP019
			VpmsComputeResult aResult = vpmsProxy.getResults();
			NbaVpmsRequirementsData vpmsRequirementsData = new NbaVpmsRequirementsData(aResult); //SPR1753
			if (aResult.getReturnCode() == 1) { // no requirements found
				// SPR1753 code deleted
				aResult.setResult("0");
				vpmsRequirementsData.setResult(aResult); //SPR1753
				//SPR2199 code deleted 
				return vpmsRequirementsData; //SPR1753
			}
			//SPR1753 code deleted
			//end ACP019
			//begin ACN016 
			//A2_AXAL002,ALS1929,ALS2305
			NbaVpmsModelResult nbVpmsResult=new NbaVpmsModelResult(vpmsProxy.getResults().getResult());		
			ArrayList arrReq= nbVpmsResult.getVpmsModelResult().getRequirementInfo();
			ArrayList workupRequirements = getImpairmentWorkupRequirements(party); // ACP019 SPR2742
			// add workupRequirements to initialrequirements
			if (arrReq == null) {
				arrReq = new ArrayList(); // ACP019
			}
			if (workupRequirements != null) {
				arrReq.addAll(workupRequirements);		
			}
			vpmsRequirementsData.setArrReq((ArrayList)determineNewRequirements(arrReq, party)); //SPR2544 SPR2199 SPR2742
			//end ACN016

			//SPR2199 code deleted 
			return vpmsRequirementsData;
			//begin SPR2199
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();
			throw e;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.RMI, re, NbaExceptionType.FATAL);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
			//end SPR2199
		}
	}

	/**
	 * Create and initialize an <code>NbaVpmsModelResult</code> object to return list of form instances.
	 * @param entryPoint the VP/MS model's entry point
	 * @return ArrayList of the form instances
	 * @throws NbaBaseException
	 */
	// NBA250 New Method
	protected FormInstance getDataFromVpmsModelFormRequirements(String entryPoint, Map deOink) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			oinkDataAccess = new NbaOinkDataAccess(getWork().getNbaLob());
			if (nbaTxLife != null) {
				oinkDataAccess.setAcdbSource(new NbaAcdb(), nbaTxLife);
				oinkDataAccess.setContractSource(nbaTxLife);
			}
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, getVpmsModelToExecute());
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult aResult = vpmsProxy.getResults();
			NbaVpmsModelResult nbVpmsResult = new NbaVpmsModelResult(aResult.getResult());
			ArrayList arrReq = nbVpmsResult.getVpmsModelResult().getFormInstance();
			FormInstance formInstance = (FormInstance) arrReq.get(0);
			return formInstance;
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();
			throw e;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.RMI, re, NbaExceptionType.FATAL);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
			}
		}
	}

	/**
	 * Get a List of Impairment Workup Requirements, by looping over all Impairments for the insured party.
	 * @param party the party object
	 * @return ArrayList - List of RequirementInfo objects
	 * @throws NbaBaseException
	 */
	// ACP019 New Method
	//SPR2742 added Parameter party
	//A2_AXAL002,ALS1929,ALS2305
	protected ArrayList getImpairmentWorkupRequirements(Party party) throws NbaBaseException {
		ArrayList workupRequirements = new ArrayList();
		//SPR2742 code deleted
		// get List of Impairments for above party
		ArrayList impairments = nbaTxLife.getImpairments(party.getId()); //SPR2742, A2_AXAL002,ALS1929,ALS2305
		int listSize = impairments.size();
		ImpairmentInfo aImpairment = null;
		ArrayList tempRequirments = null;
		for (int i = 0; i < listSize; i++) {
			aImpairment = (ImpairmentInfo) impairments.get(i);
			if (!("true".equalsIgnoreCase(aImpairment.getImpWorkupInd()))) {
				tempRequirments = getDataFromVpmsModelRequirementsDetermination(NbaVpmsAdaptor.EP_GET_IMP_WORKUP_REQUIREMENTS, aImpairment);
				if (tempRequirments != null) {
					workupRequirements.addAll(tempRequirments);
				}
				// update Indicator to true
				aImpairment.setImpWorkupInd("true");
				aImpairment.setActionUpdate();
			}
		}
		return workupRequirements;
	}

	/**
	 * Get a List of Impairment Workup Requirements from VPMS model
	 * @param entryPoint the VP/MS model's entry point
	 * @param impairment the ImpairmentInfo object on which workup has to be done
	 * @return ArrayList - List of RequirementInfo objects
	 * @throws NbaBaseException
	 */
	// ACP019 New Method
	protected ArrayList getDataFromVpmsModelRequirementsDetermination(String entryPoint, ImpairmentInfo impairment) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, getVpmsModelToExecute());//ACN008 SPR3362
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			//begin ACP019
			Map deOink = new HashMap();
			deOink.put("A_AgentLicNum_SAG", strAgent);
			deOink.put("A_DBA_SAG", strDBA);
			deOink.put("A_ReltoAnnOrIns_OWN", strRelInsOwn);
			// deOink Impairment fields
			//begin SPR2742
			deOink.put("A_ImpairmentDate", NbaUtils.getStringInISOFormatFromDate(impairment.getImpairmentDate()));
			deOink.put("A_ImpairmentStatus", String.valueOf(impairment.getImpairmentStatus()));
			deOink.put("A_ImpairmentType", impairment.getImpairmentType());
			deOink.put("A_Debit", String.valueOf(impairment.getDebit()));
			deOink.put("A_Description", impairment.getDescription());
			deOink.put("A_ImpWorkupInd", impairment.getImpWorkupInd());
			//end SPR2742
			//end ACP019
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink); //ACP019
			//begin ACP019
			VpmsComputeResult aResult = vpmsProxy.getResults();
			if (aResult.getReturnCode() == 1) { // no requirements found
				return null;
			}
			NbaVpmsModelResult nbVpmsResult = new NbaVpmsModelResult(aResult.getResult()); //ACN016		
			ArrayList workupRequirements = nbVpmsResult.getVpmsModelResult().getRequirementInfo(); //ACN016
			// SPR3290 code deleted
			//end ACP019
			//SPR3362 code deleted
			return workupRequirements;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("ImpairmentWorkup RequirementsDetermination problem", re);
			//begin SPR3362
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException e) {
				getLogger().logError(e);
			}
			//end SPR3362    
		}
	}

	//ALS5252 code refactored to parent

	/**
	 * Answer the awd case and sources 
	 * @return NbaDst which represent a awd case
	 */
	// ACN008 New Method
	protected NbaDst getParentCase() throws NbaBaseException {
		if (parentCase == null) {
			//NBA213 deleted code
			//create and set parent case retrieve option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestSources();
			retOpt.requestTransactionAsSibling();//SPR2544
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
			retOpt.setAutoSuspend();
			//get case from awd
			parentCase = retrieveWorkItem(getUser(), retOpt); //NBA213
			//NBA213 deleted code
		}
		return parentCase;
	}

	/**
	 * Add Comments for any Messages generated by the model
	 * @param aList - Messages list
	 */
	// SPR1753 New Method
	protected void addComentsFromModel(List aList) {
		for (int i = 0; i < aList.size(); i++) {
			Messages aMessage = (Messages) aList.get(i);
			if (aMessage.getPrivacyInd()) { //Attachments are created for private messages
				Attachment attachment = new Attachment();
				NbaOLifEId nbaOLifEId = new NbaOLifEId(getNbaTxLife());
				nbaOLifEId.setId(attachment);
				attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_COMMENT);
				attachment.setActionAdd();
				attachment.setDateCreated(new Date());
				attachment.setUserCode(getUser().getUserID());
				AttachmentData attachmentData = new AttachmentData();
				attachmentData.setTc(Long.toString(NbaOliConstants.OLI_VARIANT_STRING));
				attachmentData.setPCDATA(aMessage.getMessageText());
				attachmentData.setActionAdd();
				attachment.setAttachmentData(attachmentData);
				getNbaTxLife().getPrimaryHolding().addAttachment(attachment);
			} else { //AWD comments are created for non-provate messages
				addComment(aMessage.getMessageText());
			}
		}
	}

	/**
	 * Answer the next value for the iteration count;
	 * @return int
	 */
	// SPR1753 New Method
	protected int getNextIterationCount() {
		return ++iterationCount;
	}

	/**
	 * This Methods gets All the Requirement present on the case for the given Relation Role code and Related Reference Id. 
	 * @param roleCode Rolecode for which requirements needs to be searched.
	 * @param refId relatedReference Id for which requirements needs to be searched.
	 * @return List Containing all the Requirements.
	 */
	//SPR2506 New Method
	//SPR2742 changed first parameter type from String to long
	protected List getAllRequirementForInsured(long roleCode, String refId) throws NbaBaseException {
		List aTransactioList = getWork().getNbaTransactions();
		List requirementlist = new ArrayList();
		NbaLob lob = null;
		NbaTransaction transaction = null;
		int transactionListSize = aTransactioList.size();
		for (int i = 0; i < transactionListSize; i++) {
			transaction = (NbaTransaction) aTransactioList.get(i);
			lob = transaction.getNbaLob();
			if (NbaConstants.A_WT_REQUIREMENT.equals(lob.getWorkType()) && roleCode == lob.getReqPersonCode()
					&& (refId == null || Integer.parseInt(refId) == lob.getReqPersonSeq())) { //SPR2742
				requirementlist.add(String.valueOf(lob.getReqType())); // Matching requirement for Insured Found.
			}
		}
		return requirementlist;
	}

	/**Gets the Requirements for insured from AWD and Creates RequirementInfo Object of these Requirements for Further processing. 
	 * @param party the Party object 
	 * @return List of all the RequirementInfo Objects.
	 * @throws NbaBaseException
	 */
	//SPR2544 New Method
	//SPR2742 added Parameter party
	//A2_AXAL002,ALS1929,ALS2305
	protected List getRequirementInfoForInsured(Party party) throws NbaBaseException {
		List aTransactioList = getWork().getNbaTransactions();
		SortingHelper.sortData(aTransactioList,true,SORT_BY_CREATEDATE); //APSL488
		List reqInfoList = new ArrayList();
		NbaLob lob = null;
		NbaTransaction transaction = null;
		Relation relation = NbaUtils.getRelationForParty(party.getId(), getNbaTxLife().getOLifE().getRelation().toArray()); //SPR2742
		long roleCode = relation.getRelationRoleCode(); //SPR2742
		int relRefId = NbaUtils.convertStringToInt(relation.getRelatedRefID()); //SPR2742
		int transactionListSize = aTransactioList.size();
		for (int i = 0; i < transactionListSize; i++) {
			transaction = (NbaTransaction) aTransactioList.get(i);
			lob = transaction.getNbaLob();
			RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(lob.getReqUniqueID()); //ALS4006
			//Begin APSL3229,QC12107
			RequirementInfo newReqInfo = new RequirementInfo();
			newReqInfo.setReqCode(lob.getReqType());
			newReqInfo.setRequestedDate(new Date());
			NbaRequirementMerger reqMerger = new NbaRequirementMerger(getUser(), getNbaTxLife()); 
			//End APSL3229,QC12107
			if (NbaConstants.A_WT_REQUIREMENT.equals(lob.getWorkType()) && roleCode == lob.getReqPersonCode() && (relRefId == lob.getReqPersonSeq())) { //SPR2742
				RequirementInfo requirementInfo = new RequirementInfo();
				requirementInfo.setRequirementInfoUniqueID(lob.getReqUniqueID());//APSL404
				requirementInfo.setReqCode(lob.getReqType());
				requirementInfo.setReqStatus(lob.getReqStatus());//SPR2199
				requirementInfo.setReceivedDate(lob.getReqReceiptDate()); //SPR2199
				requirementInfo.setFormNo(lob.getFormNumber()); //ALS3255
				if(reqInfo != null){ 
					requirementInfo.setRequirementDetails(reqInfo.getRequirementDetails()); //ALS4006
					requirementInfo.setReqSubStatus(reqInfo.getReqSubStatus()); //ALS3963
					requirementInfo.setRequestedDate(reqInfo.getRequestedDate()); //APSL931
				}
				OLifEExtension extension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
				RequirementInfoExtension requirementInfoExtension = extension.getRequirementInfoExtension();
				TrackingInfo trackingInfo = new TrackingInfo();
				trackingInfo.setTrackingServiceProvider(lob.getReqVendor());
				requirementInfoExtension.setTrackingInfo(trackingInfo);
				requirementInfoExtension.setReceivedDateTime(lob.getReqReceiptDateTime());//QC20240
				requirementInfo.addOLifEExtension(extension);
				if(reqInfo != null && (reqInfo.getReceivedDate()==null || (reqInfo.getReceivedDate()!=null && (!reqMerger.isBeyondReceiptDate(newReqInfo, reqInfo))))){//APSL3229,QC12107, APSL4228
					reqInfoList.add(requirementInfo);
				}
			}
		}
		return reqInfoList;
	}

	/**
	 * Retreive workitems and sources from AWD and set retreived case to the work object. 
	 * @throws NbaBaseException if RemoteException is thrown by netserver.
	 */
	//SPR2399 New Method
	protected void retreiveWorkFromAWD() throws NbaBaseException {
		//NBA213 deleted code
		if (work.isCase()) {
			NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
			retrieveOptionsValueObject.setWorkItem(getWork().getID(), true);
			retrieveOptionsValueObject.requestSources();
			retrieveOptionsValueObject.requestTransactionAsChild();
			// APSL5055-NBA331.1 code deleted
			setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject)); //NBA213
		} else {
			setWork(getParentCase());
		}
		//NBA213 deleted code
	}

	/**
	 * Resolve Agent Lic No, Servicing Agency Name and Relation To Owner.
	 * @throws NbaBaseException
	 */
	//SPR2399 New Method
	protected void prepareForProcessing() throws NbaBaseException {
		oinkDataAccess = new NbaOinkDataAccess(nbaTxLife); // set up the NbaOinkDataAccess object
		oinkDataAccess.setAcdbSource(new NbaAcdb(), nbaTxLife);
		oinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);

		oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
		oinkRequest.setVariable("AgentLicNum_SAG"); // get the Agent Lic No
		strAgent = oinkDataAccess.getStringValueFor(oinkRequest);

		oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
		oinkRequest.setVariable("DBA_SAG"); // get the Servicing Agency Name
		strDBA = oinkDataAccess.getStringValueFor(oinkRequest);

		oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
		oinkRequest.setVariable("ReltoAnnOrIns_OWN"); // get the Relation To Owner 
		strRelInsOwn = oinkDataAccess.getStringValueFor(oinkRequest);
	}

	/**
	 * Call vp/ms model to check if any requirement can be ordered for a party. If requirements are found to be
	 * ordered, this method add and process these requirements.
	 * @param party the party object
	 * @throws NbaBaseException
	 * 
	 **/
	//SPR2399 New Method
	//SPR2742 changed parameter type from int to Party
	//A2_AXAL002,ALS1929,ALS2305
	protected void processPartyForRequirements(Party party) throws NbaBaseException {
		NbaLob workLOB = getWork().getNbaLob();
		oinkDataAccess.setLobSource(workLOB);
		oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
		//SPR2742 code deleted
		oinkDataAccess.setAcdbSource(new NbaAcdb(), nbaTxLife);
		oinkDataAccess.setContractSource(nbaTxLife);//SPR3329
		//begin SPR2742
		Relation relation = NbaUtils.getRelationForParty(party.getId(), getNbaTxLife().getOLifE().getRelation().toArray());
		long roleCode = relation.getRelationRoleCode();
		String relRefId = relation.getRelatedRefID();
		oinkRequest.setPartyFilter(roleCode, relRefId); //Note the OINK filter could be the Owner party
		oinkRequest.setArgs(getKeys(party));
		//end SPR2742
		//A2_AXAL002,ALS1929,ALS2305
		if (!getNbaTxLife().isOwnerSameAsInsured()) { //A2_AXAL002,ALS1929,ALS2305, P2AXAL054
			OwnOtherThenIns = true;//NA_AXAL004
		}
		Map deOink = new HashMap();
		deOink.put("A_PreventProcess", String.valueOf(preventProcess)); //ALS4054
		deOink.put("A_OwnOtherThenIns", String.valueOf(OwnOtherThenIns));  //NA_AXAL004
		// APSL5177 :: START-- EOLI requirement should generate
		indForEOLI = getEOLIreqInd();
		if(indForEOLI){
			deOink.put("A_EOLIReqInd", "true"); //ALS4054
		}
		// APSL5177 :: END		
		
		List requirementList = getAllRequirementForInsured(roleCode, relRefId); //SPR2742 //A2_AXAL002,ALS1929,ALS2305
		//Begin AXAL3.7.06		
		List forms = new ArrayList();
		List transactions = getWork().getNbaTransactions();
		String formNumber;
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction transaction = (NbaTransaction) transactions.get(i);
			List sources = transaction.getNbaSources();
			for (int j = 0; j < sources.size(); j++) {
				NbaSource aSource = (NbaSource) sources.get(j);
				String sType = aSource.getSourceType();
				
				if (sType.equals(NbaConstants.A_ST_MISC_MAIL) || sType.equals(NbaConstants.A_ST_FORMS)) {
					 formNumber = aSource.getNbaLob().getFormNumber();
					if (formNumber != null && formNumber.length() > 0) {
						forms.add(formNumber);
					}
				}			
			}
		}
		//if(workLOB.getApplicationType().equals(String.valueOf(NbaOliConstants.OLI_APPTYPE_TRIAL))){
		//ALS3319 code deleted
		List caseSources = getWork().getNbaSources();
		for (int i = 0; i < caseSources.size(); i++) {
			NbaSource source = (NbaSource) caseSources.get(i);
			//ALS4514 code deleted
			formNumber = source.getNbaLob().getFormNumber();
			if (formNumber != null && formNumber.length() > 0) {
				forms.add(formNumber);
			} else if (source.isMiscMail() && source.getNbaLob().getReqType() > 0) {
				//miscmail with rqtp are prospective requirements in DB, add to the list
				requirementList.add(String.valueOf(source.getNbaLob().getReqType()));//ALS4671-ALS4685 refac
			}
			//ALS4514 code deleted
		}
		//ALS4671-ALS4685 refac
		deOink.put("A_ReqFromDataBase", requirementList.toArray(new String[requirementList.size()]));
		deOink.put("A_ReqFromDataBaseLength", String.valueOf(requirementList.size()));
		deOink.put("A_FormNumberLOBList", forms.toArray(new String[forms.size()]));
		deOink.put("A_no_of_FormNumberLOBList", String.valueOf(forms.size()));
		int reqCount = nbaTxLife.getPolicy().getRequirementInfoCount();
		deOink.put("A_no_of_ReqList", Integer.toString(reqCount));
		deOinkFormInstance(deOink, party);//P2AXAL054
		deOinkBeneOwnerValues(deOink);
		deOinkSignatureValues(deOink);
		deOinkReplacementValues(deOink);
		deOinkCovOptionValues(deOink);
		NbaRequirementUtils.deOinkEndorsementValues(deOink, nbaTxLife);//ALS4322
		deOinkGovtIDValues(deOink, party);
		//End AXAL3.7.06
		deOink.put("A_generateChangeFormReq",(hasContractChangeForm()?"1":"0"));//ALS5351
		//Begin AXAL3.7.40
		List sysMessagesIds = getSystemMessagesIdsList(party); //A2_AXAL002,ALS1929,ALS2305
		deOink.put("A_CVErrorMsgCode", sysMessagesIds.toArray(new String[sysMessagesIds.size()]));
		deOink.put("A_no_of_CVErrorMsgCodes", String.valueOf(sysMessagesIds.size()));
		//Begin NBLXA-1640
		if(party.getPartyTypeCode() == NbaOliConstants.OLI_PT_ORG && !NbaUtils.isBlankOrNull(party.getPersonOrOrganization()) 
				&& !NbaUtils.isBlankOrNull(party.getPersonOrOrganization().getOrganization())){
			deOink.put("A_OrgFormOWN", String.valueOf(party.getPersonOrOrganization().getOrganization().getOrgForm()));
		}
		//End NBLXA-1640
		if (party.hasRisk()) {
			RiskExtension riskExtension = NbaUtils.getFirstRiskExtension(party.getRisk());
			if (riskExtension != null) {
				ArrayList medicalCertificationList = riskExtension.getMedicalCertification();
				if (medicalCertificationList != null) {
					int count = medicalCertificationList.size();
					if (count > 0) {
						MedicalCertification medicalCertification = (MedicalCertification) medicalCertificationList.get(0);
						if (medicalCertification != null) {
							deOink.put("A_OtherCompanyExamUsed", String.valueOf(medicalCertification.getOtherCompanyExamUsedInd()));
							deOink.put("A_ExamStmtsRemainTrue", String.valueOf(medicalCertification.getExamStmtsRemainTrueIndCode()));
							deOink.put("A_ExamDate", String.valueOf(medicalCertification.getExamDate()));
							deOink.put("A_MedicalInfoSuppliedIndCode", String.valueOf(medicalCertification.getMedicalInfoSuppliedIndCode()));
							deOink.put("A_MedicalDrConsultIndCode", String.valueOf(medicalCertification.getMedicalDrConsultIndCode()));
						}
					}
				}

			}
			deOinkRecommendTreatmentIndCodeForQNO9(deOink, party.getRisk());
		}
		//QC12046/APSL3126 reverted changes of APSL1712
		ArrayList addressList = party.getAddress();
		boolean hasPOBox = false;
		boolean hasResidence = false;
		if (addressList != null) {
			for (int i = 0; i < addressList.size(); i++) {
				Address address = (Address) addressList.get(i);
				if (address.getAddressFormatTC() == NbaOliConstants.OLI_ADDRTC_USPOBX) {
					hasPOBox = true;
				} 
				if (address.getAddressTypeCode() == NbaOliConstants.OLI_ADTYPE_HOME) { //ALII1723
					hasResidence = true;
				}
			}
			if (hasPOBox && !hasResidence) {
				deOink.put("A_ResidentAddressStatus", String.valueOf(hasPOBox && !hasResidence));
			}
		}
		deOink.put("A_CWA_Present", hasCWA());//ALS4923
		//End AXAL3.7.40
		deOink.put("A_MissingDataReqSwitch",String.valueOf(NbaUtils.getPostDeployAppInd(workLOB.getCreateDate())));//APSL3612
		// APSL4855 Begin
		Policy policy = getNbaTxLife().getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && getOrigWorkItem() != null && NbaConstants.A_WT_REEVALUATE.equals(getOrigWorkItem().getWorkType())
					&& parentCase != null && parentCase.getNbaLob().hasPredictiveInd() == null) {
				deOink.put("A_HasPredictiveRun", "false");
			} else {
				deOink.put("A_HasPredictiveRun", "true");
			}
			// NBLXA-2072 Begin
			Date appSubmissionDate = null;
			if (null != policy.getApplicationInfo() && null != policy.getApplicationInfo().getSubmissionDate()) {
				appSubmissionDate = policy.getApplicationInfo().getSubmissionDate();
				deOink.put("A_LNRCScoreInd", String.valueOf(isLNRCScoreApplicable(appSubmissionDate)));
			}
			// NBLXA-2072 End
			//NBLXA-2184 - Begin
			boolean isCIPHigh = NbaRequirementUtils.isCIPHighRiskCVPresent(getNbaTxLife(),party);
			deOink.put("A_CIPHighRiskInd", String.valueOf(isCIPHigh));
			List formsSupp = new ArrayList();
			formsSupp = getApplicationSourceInfo(getWork());
			if(formsSupp != null && !formsSupp.isEmpty()){
				deOink.put(A_FORMNUMBERLIST, formsSupp.toArray(new String[formsSupp.size()]));
				deOink.put(A_FORMNUMBERLISTSIZE, String.valueOf(formsSupp.size()));
			}
			//NBLXA-2184 - End
			
		}
		// APSL4855 End
		//Begin NBLXA-2315
		deOink.put("A_isClientDataRedLevel", "false");
		deOink.put("A_isBAERedLevel", "false");
		int sysMsgCount = getNbaTxLife().getPrimaryHolding().getSystemMessageCount();
		int breakFlag = 0;
		for (Object sysMsg : getNbaTxLife().getPrimaryHolding().getSystemMessage()) {
			if (((SystemMessage)sysMsg).getMessageCode() == 6767) {
				deOink.put("A_isClientDataRedLevel", "true");
				++breakFlag;
			}
			if (((SystemMessage)sysMsg).getMessageCode() == 6770) {
				deOink.put("A_isBAERedLevel", "true");
				++breakFlag;
			}
			if (breakFlag == 2) {
				break;
			}
		}
		//End NBLXA-2315
		
		
		deOinkTermConvData(deOink); //P2AXAL040
		NbaVpmsRequirementsData requirementsData = getDataFromVpmsModelRequirementsDetermination(
				NbaVpmsConstants.EP_GET_REQUIREMENTS_AND_PROVIDERS_AND_COMMENTS, deOink, party); //SPR2742, A2_AXAL002,ALS1929,ALS2305

		messages.addAll(requirementsData.getMessages());

		//A2_AXAL002,ALS1929,ALS2305
		//ALS4054 Code Deleted
		if (!requirementsData.wasSuccessful() && requirementsData.getRequirements().size() > 0) {
			List requirements = requirementsData.getRequirements();
			if (getOrigWorkItem().isCase()) {//ALS1758
				requirements = filterVpmsRequirements(requirements);//AXAL3.7.40G
			}
			ListIterator li = requirements.listIterator();//AXAL3.7.40G
			String tempReqStatus = workLOB.getReqStatus();
			workLOB.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_ORDER));
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), workLOB);
			workLOB.setReqStatus(tempReqStatus);

			NbaRequirementUtils reqUtils = new NbaRequirementUtils();
			reqUtils.setHoldingInquiry(nbaTxLife);

			reqUtils.setReqPersonCodeAndSeq((int)roleCode, NbaUtils.convertStringToInt(relRefId)); //SPR2742, A2_AXAL002,ALS1929,ALS2305
			reqUtils.setAutoGeneratedInd(true); //always true for req determination
			reqUtils.setEmployeeId(getUser().getUserID());

			NbaTransaction nbaTransaction = null;
			NbaVpmsRequirement vpmsRequirement = null;
			//begin APSL404
			while (li.hasNext()) {
				vpmsRequirement = (NbaVpmsRequirement) li.next();
				if (NbaOliConstants.OLI_REQCODE_SIGNILLUS == vpmsRequirement.getType()
						|| NbaOliConstants.OLI_REQCODE_PREMIUMQUOTE == vpmsRequirement.getType()
						|| NbaOliConstants.OLI_REQCODE_PREMDUE == vpmsRequirement.getType()) { //SR534326
					processMultiRequirement(nbaTransaction, vpmsRequirement, reqUtils, workLOB, party.getId(), provider, vpmsRequirement.getType());
				} else {
					nbaTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
					nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
					setRouteReason(nbaTransaction, provider.getInitialStatus());
					nbaTransaction.getTransaction().setLock("Y");
					//set action flag so new transaction will be created with a lock
					nbaTransaction.getTransaction().setAction("L");
					processRequirement(nbaTransaction, vpmsRequirement, reqUtils, workLOB, party.getId());// NBA130 //SPR2742
																										  // A2_AXAL002,ALS1929,ALS2305
				}
			}
			//end APSL404			
			requirementsGenerated = true;
		}
		if ((!preventProcess || !getOrigWorkItem().isCase())) { //AXAL3.7.40G //ALS1758
			//Begin NBA250
			List formReqList = (ArrayList) processFormRequirements(party, deOink);
			List formReqQuestionList = (ArrayList) processFormRequirementsFromQuestions(party, deOink);
			NbaRequirementMerger reqMerger = new NbaRequirementMerger(getUser(), getNbaTxLife()); //ALS3963 //ALS4082
			List filteredFormReq = reqMerger.determineFormRequirements(formReqList, formReqQuestionList);
			List allexistingReqs = getRequirementInfoForInsured(party); //A2_AXAL002,ALS1929,ALS2305		
			List reqGenList = reqMerger.removeExistingRequirements(filteredFormReq, allexistingReqs);
			//APSL3099 Begin
			if((termConvPertainsTo != null && replacementPertainsTo != null) && !termConvPertainsTo.getId().equalsIgnoreCase(replacementPertainsTo.getId())){//APSL3099
				// If term conversion and replacement is initiated by Primary and Joint Insured or vice versa, 
				//then no need to filter requirements.
			}
			else{
				if(termConvPertainsTo != null){
					if((getNbaTxLife().isJointInsured(termConvPertainsTo.getId()) && getNbaTxLife().isPrimaryInsured(party.getId())) ||
							(getNbaTxLife().isPrimaryInsured(termConvPertainsTo.getId()) && getNbaTxLife().isJointInsured(party.getId()))	
						){
						reqGenList = filterRequirementsForJointAndReplacement(reqGenList);
					}				
				}else if(replacementPertainsTo != null){
					if((getNbaTxLife().isJointInsured(replacementPertainsTo.getId()) && getNbaTxLife().isPrimaryInsured(party.getId())) ||
							(getNbaTxLife().isPrimaryInsured(replacementPertainsTo.getId()) && getNbaTxLife().isJointInsured(party.getId()))	
						){
						reqGenList = filterRequirementsForJointAndReplacement(reqGenList);
					}				
				}
			}
			
			//End APSL3099
			if (reqGenList.size() > 0) {
				String tempReqStatus = workLOB.getReqStatus();
				workLOB.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_ORDER));
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), workLOB);
				workLOB.setReqStatus(tempReqStatus);

				NbaRequirementUtils reqUtils = new NbaRequirementUtils();
				reqUtils.setHoldingInquiry(nbaTxLife);
				reqUtils.setReqPersonCodeAndSeq((int) roleCode, NbaUtils.convertStringToInt(relRefId)); //SPR2742
				reqUtils.setAutoGeneratedInd(true); //always true for req determination
				reqUtils.setEmployeeId(getUser().getUserID());

				NbaTransaction nbaTransaction = null;
				NbaVpmsRequirement vpmsRequirement = null;
				ListIterator li = reqGenList.listIterator();
				while (li.hasNext()) {
					nbaTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
					nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
					setRouteReason(nbaTransaction, provider.getInitialStatus());
					nbaTransaction.getTransaction().setLock("Y");
					//set action flag so new transaction will be created with a lock
					nbaTransaction.getTransaction().setAction("L");
					vpmsRequirement = (NbaVpmsRequirement) li.next();
					processRequirement(nbaTransaction, vpmsRequirement, reqUtils, workLOB, party.getId());// NBA130 //SPR2742
				}
				requirementsGenerated = true;
			}//End NBA250
		}
		//A2_AXAL002,ALS1929,ALS2305
		// Start SR514766
		if (hasUnderwriterApproval(getNbaTxLife()) && (roleCode == NbaOliConstants.OLI_REL_INSURED)) {
			setIllustrationIndicator(getNbaTxLife(), getWork(), requirementList); 
			setRevisedIllustrationIndicator(getNbaTxLife(), getWork(), requirementList); //CR1453745 
		}
	    //End  SR51476
	}


	//A2_AXAL002,ALS1929,ALS2305
    // SR566149 and SR519592 new method 
	/**
	 * Determine if there is any Amendment on case than set RequirementGeneratedInd to true
	 * So that in 2nd Pass of Requirement Determination after Requirement Hold no Amendment requirement should be generate for same amendment
	 */
	
	public void updateAmendment() {
		ArrayList endorsements = getNbaTxLife().getPolicy().getEndorsement();
		Endorsement endorsement;
		for (int z = 0; z < endorsements.size(); z++) {
			endorsement = (Endorsement) endorsements.get(z);
			if(NbaUtils.isAmendment(endorsement) && hasUnderwriterApproval(getNbaTxLife())) { //APSL1535
				EndorsementExtension endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
				endorsementExtension.setRequirementGeneratedInd(true);
				endorsementExtension.setActionUpdate();
			}
		}	
	}
	/**
	 * Call NBA_FORMS_VALIDATION table to check if any form specific requirement can be ordered for 
	 * the party. If requirements are found to be ordered, this method add and process these 
	 * requirements.
	 * @param party the party object
	 * @param deOink the HashMap with deoink variables
	 * @throws NbaBaseException
	 */
	//NBA250 New Method
	protected List processFormRequirements(Party party, Map deOink) throws NbaBaseException {
		List formRequirementList = new ArrayList();
		NbaTableAccessor tableaccessor = new NbaTableAccessor();
		Map caseData = tableaccessor.setupTableMap(getWork());
		long relationRoleCode = NbaUtils.getRelationForParty(party.getId(), nbaTxLife.getOLifE().getRelation().toArray()).getRelationRoleCode();
		//call to VPMS model for general questions 
		// QUERY : Select COMPANY_CODE, APPLICATION_TYPE, PRODUCT_TYPE, COVERAGE_KEY, REPLACEMENT_IND, INS_ROLE, START_DATE, END_DATE, APPLICATION_STATE, 
		//QUESTION_NUMBER, QUESTION_ANSWER, REQUIREMENT_TYPE, FORM_NUMBER, PROVIDER, FOLLOWUP_PROVIDER from NBA_FORMS_VALIDATION 
		//where (COMPANY_CODE = {COMPANY_CODE} or COMPANY_CODE = '*') AND (APPLICATION_TYPE = {APPLICATION_TYPE}) AND 
		//(PRODUCT_TYPE = {PRODUCT_TYPE}) AND (COVERAGE_KEY = {COVERAGE_KEY} or COVERAGE_KEY = '*') AND (REPLACEMENT_IND = {REPLACEMENT_IND}) AND 
		//({APPLICATION_SIGNED_DATE} BETWEEN START_DATE AND END_DATE) AND (APPLICATION_STATE = {APPLICATION_STATE} or APPLICATION_STATE = '*') AND 
		//(QUESTION_NUMBER = {QUESTION_NUMBER} OR QUESTION_NUMBER = '*') AND (QUESTION_ANSWER = {QUESTION_ANSWER}  OR QUESTION_ANSWER = '*')
		Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
		/*caseData.put(NbaTable.COMPANY_CODE,policy.getCarrierCode());
		 caseData.put(NbaTable.APP_TYPE,Long.toString(policy.getApplicationInfo().getApplicationType()));
		 caseData.put(NbaTable.PRODUCT_TYPE, Long.toString(policy.getProductType()));
		 caseData.put(NbaTable.COVERAGE_KEY, policy.getProductCode());
		 caseData.put(NbaTable.REPLACEMENT_IND, Integer.toString(repInd));
		 caseData.put(NbaTable.INS_ROLE, Long.toString(relationRoleCode));
		 caseData.put(NbaTable.APPLICATION_SIGNED_DATE, appSignedDate.toString());
		 caseData.put(NbaTable.APPLICATION_STATE, Long.toString(policy.getApplicationInfo().getApplicationJurisdiction()));
		 caseData.put(NbaTable.QUESTION_NUMBER, "*");
		 caseData.put(NbaTable.QUESTION_ANSWER, "*");*/

		// add aditional column values in caseData Map (QuestionNumber, Question Answer , ReplacementInd)
		FormInstance formInstanceData = getDataFromVpmsModelFormRequirements(NbaVpmsConstants.EP_FORM_REQ_DATA, deOink);
		if (formInstanceData != null && formInstanceData.getFormResponseCount() <= 0) {
			return formRequirementList;
		}
		String query = getGenericReqQueryForFormsTable(policy, relationRoleCode, formInstanceData);
		NbaFormsValidationData nbaFormsValidations[] = (NbaFormsValidationData[]) tableaccessor.getFormRequirementsForQuestions(caseData,
				"NBA_FORMS_VALIDATION", query);
		if (debugLogging) { 
			getLogger().logDebug("Generic Req Query: " + query);
		}
		if (nbaFormsValidations != null && nbaFormsValidations.length > 0) {
			for (int i = 0; i < nbaFormsValidations.length; i++) {
				NbaFormsValidationData formsValidationData = nbaFormsValidations[i];

				NbaVpmsRequirement nbaVpmsRequirement = new NbaVpmsRequirement(formsValidationData.getRequirementType(), formsValidationData
						.getProvider(), formsValidationData.getFormNumber(), formsValidationData.getFollowUpProvider());
				if (debugLogging) { 
					getLogger().logDebug(nbaVpmsRequirement);
				}
				formRequirementList.add(nbaVpmsRequirement);
			}
		}
		return formRequirementList;
	}

	/**
	 * Call VPMS to get the formInstance object of all the questions and responses. Using the data query
	 * NBA_FORMS_VALIDATION table to check if any form specific requirement can be ordered for 
	 * the party. If requirements are found to be ordered, this method add and process these 
	 * requirements.
	 * @param party the party object
	 * @param deOink the HashMap with deoink variables
	 * @throws NbaBaseException
	 */
	//	NBA250 New Method
	protected List processFormRequirementsFromQuestions(Party party, Map deOink) throws NbaBaseException {
		List formRequirementList = new ArrayList();
		NbaTableAccessor tableaccessor = new NbaTableAccessor();
		Map caseData = tableaccessor.setupTableMap(getWork());
		//call to VPMS model for specific questions and answers
		//QUERY : Select COMPANY_CODE, APPLICATION_TYPE, PRODUCT_TYPE, COVERAGE_KEY, REPLACEMENT_IND, INS_ROLE, START_DATE, END_DATE,
		// APPLICATION_STATE,
		//QUESTION_NUMBER, QUESTION_ANSWER, REQUIREMENT_TYPE, FORM_NUMBER, PROVIDER, FOLLOWUP_PROVIDER from NBA_FORMS_VALIDATION
		//where (COMPANY_CODE = {COMPANY_CODE} or COMPANY_CODE = '*') AND (APPLICATION_TYPE = {APPLICATION_TYPE}) AND
		//(PRODUCT_TYPE = {PRODUCT_TYPE}) AND (COVERAGE_KEY = {COVERAGE_KEY} or COVERAGE_KEY = '*') AND (REPLACEMENT_IND = {REPLACEMENT_IND}) AND
		//({APPLICATION_SIGNED_DATE} BETWEEN START_DATE AND END_DATE) AND (APPLICATION_STATE = {APPLICATION_STATE} or APPLICATION_STATE = '*') AND
		//(QUESTION_NUMBER = {QUESTION_NUMBER} AND QUESTION_ANSWER = {QUESTION_ANSWER}) OR
		//(QUESTION_NUMBER = {QUESTION_NUMBER}AND QUESTION_ANSWER = {QUESTION_ANSWER}) OR...
		Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
		long relationRoleCode = NbaUtils.getRelationForParty(party.getId(), nbaTxLife.getOLifE().getRelation().toArray()).getRelationRoleCode();
		FormInstance formInstanceData = getDataFromVpmsModelFormRequirements(NbaVpmsConstants.EP_FORM_REQ_DATA, deOink);
		if (formInstanceData != null && formInstanceData.getFormResponseCount() <= 0) {
			return formRequirementList;
		}
		//begin ALS4615 
		for (int j = 0; j < formInstanceData.getFormResponseCount(); j++) {
			FormResponse formResponse = formInstanceData.getFormResponseAt(j);
			if (formResponse != null) {
				String query = getSpecificReqQueryForFormsTable(policy, relationRoleCode, formResponse);
				if (debugLogging) { 
					getLogger().logDebug("Specific Req Query: " + query);
				}
				NbaFormsValidationData nbaFormsValidations[] = (NbaFormsValidationData[]) tableaccessor.getFormRequirementsForQuestions(caseData,
						"NBA_FORMS_VALIDATION", query);
				if (nbaFormsValidations != null && nbaFormsValidations.length > 0) { //ALS5159 refactored
					List tempFormReqList = new ArrayList(); //APSL1419
					for (int k = 0; k < nbaFormsValidations.length; k++) {
						NbaFormsValidationData formsValidationData = nbaFormsValidations[k];
						if (formsValidationData.getQuestionNumber().equalsIgnoreCase(formResponse.getQuestionNumber())) {
							String remark = formResponse.getQuestionText();
							NbaVpmsRequirement nbaVpmsRequirement = new NbaVpmsRequirement(formsValidationData.getRequirementType(), remark,
									formsValidationData.getProvider(), formsValidationData.getFormNumber(), formsValidationData.getFollowUpProvider());
							//Begin APSL1390/APSL1467
							if(!formsValidationData.getQuestionNumber().equalsIgnoreCase(NbaConstants.FORM_VALID_QUES_NUM_REPLMT) && isFormPresent(formsValidationData.getFormNumber())) { //ALII1944
								tempFormReqList = new ArrayList();//APSL1419
								tempFormReqList.add(nbaVpmsRequirement);//APSL1419
								break;//APSL1419
							}
							//End APSL1390/APSL1467
							tempFormReqList.add(nbaVpmsRequirement);//APSL1419	
						}
					}	
					formRequirementList.addAll(tempFormReqList);//APSL1419
				} else if (debugLogging) {
					getLogger().logDebug("BAD RESULTS FOR: " + formResponse.getQuestionNumber() + ", WHERE QUERY: " + query);
				}
			}
		}
		//end ALS4615
		return formRequirementList;
	}

	/**
	 * Updates a requirement wokitem with all required LOBs. Sets the correct status from vp/ms model. It also updates
	 * requirement control source on the requirement workitem.
	 * @param nbaTransaction the new requirement workitem.
	 * @param vpmsRequirement the NbaVpmsRequirement object return by vp/ms for a requirement.
	 * @param reqUtils the instance of requirement utility object.
	 * @param workLOB the case LOBs.
	 * @param partyId the partyID of the insured
	 * @throws NbaBaseException
	 */
	//SPR2399 New Method, NBA130 Modified signature
	protected void processRequirement(NbaTransaction nbaTransaction, NbaVpmsRequirement vpmsRequirement, NbaRequirementUtils reqUtils,
			NbaLob workLOB, String partyId) throws NbaBaseException {

		NbaLob lob = nbaTransaction.getNbaLob();
		lob.setReqVendor(vpmsRequirement.getProvider()); // Vendor code
		lob.setReqType(vpmsRequirement.getType()); // Req Type
		lob.setFormNumber(vpmsRequirement.hasFormNumber() ? vpmsRequirement.getFormNumber() : ""); //NBA250 Form Number 
		//NBA130 CODE DELETED
		lob.setAgency(workLOB.getAgency()); //agency
		lob.setAgentID(workLOB.getAgentID()); //agent
		lob.setCompany(workLOB.getCompany());
		lob.setAppState(workLOB.getAppState());
		lob.setPlan(workLOB.getPlan());
		lob.setProductTypSubtyp(workLOB.getProductTypSubtyp());
		lob.setFaceAmount(workLOB.getFaceAmount());
		lob.setAppOriginType(workLOB.getAppOriginType());
		lob.setReplacementIndicator(workLOB.getReplacementIndicator()); //ALS5458
		lob.setPaidChgCMQueue(workLOB.getPaidChgCMQueue()); //ALII1250
		oinkDataAccess.setLobSource(lob);
		oinkDataAccess.setContractSource(nbaTxLife);//SPR3329
		//begin SPR2697
		//Code deleted NBA250
		//end SPR2697
		updateRequirementForSources(nbaTransaction, lob, partyId);//P2AXAL054
		// BEGIN NBA130
		RequirementInfo aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, partyId, vpmsRequirement, getUser(), lob); // NBA130
		Policy policy = nbaTxLife.getPolicy();
		policy.addRequirementInfo(aReqInfo);
		policy.setActionUpdate();
		lob.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
		lob.setReqRestriction(aReqInfo.getRestrictIssueCode());//ALS5718
		aReqInfo.setRequirementDetails(vpmsRequirement.getComment()); //Variable data	 
		// END NBA130
		//Code deleted ALII755
		updateTransactionSources(nbaTransaction, lob);//APSL3211 SR657984
		if (null == lob.getReqStatus() || NbaOliConstants.OLI_REQSTAT_RECEIVED != NbaUtils.convertStringToLong(lob.getReqStatus())) {
			NbaVpmsResultsData vpmsResultsData = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_REQUIREMENT_INITIAL_STATUS);
			if (vpmsResultsData.wasSuccessful()) {
				lob.setReqStatus((String) vpmsResultsData.getResultsData().get(0)); // Requirement Status
				aReqInfo.setReqStatus((String) vpmsResultsData.getResultsData().get(0)); //NBA130
			}
		}
		if (NbaOliConstants.OLI_REQSTAT_ORDER != NbaUtils.convertStringToLong(lob.getReqStatus())) {
			NbaProcessWorkItemProvider tempProvider = new NbaProcessWorkItemProvider(getUser(), lob);
			changeStatus(nbaTransaction, tempProvider.getInitialStatus());
		}

		reqUtils.setReqType(vpmsRequirement.getType());
		reqUtils.processRequirementWorkItem(getWork(), nbaTransaction); //update LOBs 
		//NBLXA-1254 start
		if (lob.getReqType() == NbaOliConstants.OLI_REQCODE_SEPSUPP) {
			NbaParty payerParty = NbaUtils.getPayerParty(nbaTxLife);
			if (payerParty != null) {
				lob.setEntityEinTin(payerParty.getSSN());
				lob.setEntityName(payerParty.getDisplayName());
			}
		}
		//NBLXA-1254 End
		if (debugLogging) {
			getLogger().logDebug("Added requirement: " + vpmsRequirement.getType() + "-" + vpmsRequirement.getName());
		}
		//APSL4787 
		if(vpmsRequirement.getType() == NbaOliConstants.OLI_REQCODE_1009800175){
			createReplaceNotnWorkItem(vpmsRequirement.getType()); //NBLXA-1554[NBLXA-2064]
		}
		//Start APSL5109
		// RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
		if(aReqInfo.getType() == NbaOliConstants.OLI_REQCODE_1009800033){
            boolean hipaaIgoInd = NbaRequirementUtils.retrieveHIPAAIgoInd(getWork(), getUser());
            RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
            if (reqInfoExt != null && !hipaaIgoInd) {
                  reqInfoExt.setReviewedInd(false);
                  reqInfoExt.setReviewID(null);
                  reqInfoExt.setReviewDate((Date)null);
                  reqInfoExt.setActionUpdate();
            }
		}//End APSL5109
		//Start APSL5348
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);//NBLXA186-NBLXA1272
		if (NbaUtils.isIUPCaseNoOverride(getNbaTxLife())
				&& (aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_INSPRPTQUES || aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PPR)) {
			aReqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_ADD);
			aReqInfo.setRequirementDetails(NbaConstants.REQ_INFO_DETAILS_AXA_UW);
			//RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);//commented for NBLXA186-NBLXA1272
			TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
			if (trackingInfo != null) {
				trackingInfo.setFollowUpServiceProvider(NbaConstants.FOLLOW_UP_SERVICE_PROVIDER_MANNUAL);
				trackingInfo.setTrackingServiceProvider(NbaConstants.FOLLOW_UP_SERVICE_PROVIDER_MANNUAL);
			}
			lob.setReqVendor(NbaConstants.FOLLOW_UP_SERVICE_PROVIDER_MANNUAL); // Vendor code
			lob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_ADD)); // Status
		}// End APSL5348
		//SC: NBLXA186 -NBLXA1272
		if (reqInfoExt.getUwrequirementsInd()) {
			ApplicationInfo appInfo = policy.getApplicationInfo();
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			Party party = nbaTxLife.getParty(partyId).getParty();
			if (appInfoExt != null && NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER != appInfoExt.getUnderwritingApproval() && party != null) {
				PartyExtension partyExt = NbaUtils.getFirstPartyExtension(party);
				if (partyExt != null && NbaOliConstants.OLI_LU_LASTREQSTAT_INCOMPLETE != partyExt.getLastRequirementIndForParty()) {
					partyExt.setLastRequirementIndForParty(NbaOliConstants.OLI_LU_LASTREQSTAT_INCOMPLETE);
					partyExt.setActionUpdate();
					if(getOrigWorkItem()!=null && A_WT_REEVALUATE.equals(getOrigWorkItem().getWorkType())){
						long roleCode = -1L;
						String roleTrans = "";
						String reqTrans = "";
						Relation relation = NbaUtils.getRelationForParty(partyId, nbaTxLife.getOLifE().getRelation().toArray());
						if (relation != null) {
							roleCode = relation.getRelationRoleCode();
							roleTrans = NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REL, roleCode);
						}
						reqTrans = NbaUtils.getRequirementTranslation(String.valueOf(aReqInfo.getReqCode()), nbaTxLife.getPolicy());
						NbaUtils.addAutomatedComment(getWork(),getUser(), "Last Requirement Indicator for " + roleTrans
								+ "  has been changed to OFF due to '" + reqTrans + "' requirement added on case."); //NBLXA-1718 changed from general to automated
					}
				}
			}
		}
		//EC: NBLXA186-NBLXA1272
	}
	
	//New method APSL3211 SR657984
	//This method update the sources on the requirement from the requirement transaction
	public void updateTransactionSources(NbaTransaction nbaTransaction, NbaLob transactionLOB) throws NbaBaseException {
		List sources = nbaTransaction.getNbaSources();
		int sourceCount = sources.size();
		NbaSource nbaSource = null;
		for (int i = 0; i < sourceCount; i++) {
			nbaSource = (NbaSource) sources.get(i);
			NbaLob sourceLob = nbaSource.getNbaLob();
			if (transactionLOB.getFormNumber() != null){
				if(transactionLOB.getFormNumber().equals(sourceLob.getFormNumber())) {
					sourceLob.setReqUniqueID(transactionLOB.getReqUniqueID());
					nbaSource.setUpdate();
				}
			} else if (sourceLob.getReqType() == transactionLOB.getReqType()) {
				sourceLob.setReqUniqueID(transactionLOB.getReqUniqueID());
				nbaSource.setUpdate();
			}
		}
	}

	/**
	 * Checks if a requirement can be satisfied with any source on the case. It calls vp/ms model to get form number and 
	 * source that can be used as result for this requirement. If it matches with the workitem than set the req status LOB
	 * to received and attach source with requirement workitem.
	 * @param nbaTransaction the requirement workitem.
	 * @param transactionLOB the requirement workitem's LOBs
	 * @throws NbaBaseException
	 */
	//SPR2399 New Method
	//ALS2584 ALS4751 Code Refactored //P2AXAL054 signature changed
	protected void updateRequirementForSources(NbaTransaction nbaTransaction, NbaLob transactionLOB, String partyId) throws NbaBaseException {
		//begin NBA119
		String formNumber = transactionLOB.getFormNumber(); //SPR2697
		NbaVpmsResultsData vpmsResultsDataAWdSources = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_AWD_SOURCES);
		if (vpmsResultsDataAWdSources.wasSuccessful()) {
			List awdSources = vpmsResultsDataAWdSources.getResultsData();
			List sources = getWork().getNbaCase().getNbaSources();
			int sourceCount = sources.size();
			NbaSource nbaSource = null;
			for (int i = 0; i < sourceCount; i++) {
				//end NBA119
				nbaSource = (NbaSource) sources.get(i);
				NbaLob sourceLob = nbaSource.getNbaLob();
				Party matchingParty = getPartyForMatchingResult(sourceLob);//P2AXAL054
				if ( (transactionLOB.getReqType() == NbaOliConstants.OLI_REQCODE_1009800051) || (matchingParty !=null && matchingParty.getId().equals(partyId)) ) {//P2AXAL054, ALII1951
					// For Informal Request (1009800051) Requirement, no need to check Source is indexed with which Party. Receive this Requiremet even if Source is indexed for this Requirement with any Insured (Primary or Joint).
					if (!NbaUtils.isBlankOrNull(formNumber)) {
						if (awdSources.contains(nbaSource.getSource().getSourceType()) && null != sourceLob.getFormNumber()
								&& sourceLob.getFormNumber().equalsIgnoreCase(formNumber)) {  //APSL4007-QC14195
							receiveRequirement(nbaTransaction, transactionLOB, nbaSource); //ALS5119
							removeMiscMailSourceForForm(formNumber);
							// APSL5321 Begin  For risk righter cases results received with the case for requirements already on the case should not go to UW or UWCM for review
							if (Long.toString(NbaOliConstants.OLI_RISKRIGHTER_CASE).equals(getWork().getNbaCase().getNbaLob().getRiskRighterCase())
									&& NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(nbaTransaction.getWorkType())) {
								transactionLOB.setRiskRighterCase(getWork().getNbaCase().getNbaLob().getRiskRighterCase());
								transactionLOB.setReinVendorID(NbaConstants.TRUE_STR);
							}
							// APSL5321 End
						}
					} else if (NbaUtils.isBlankOrNull(sourceLob.getFormNumber()) || "NULL".equalsIgnoreCase(sourceLob.getFormNumber())) {
						if (awdSources.contains(nbaSource.getSource().getSourceType()) && sourceLob.getReqType() == transactionLOB.getReqType()) {
							receiveRequirement(nbaTransaction, transactionLOB, nbaSource); //ALS5119
							removeMiscMailSourceForReqType(transactionLOB.getReqType());
							// APSL5321 Begin  For risk righter cases results received with the case for requirements already on the case should not go to UW or UWCM for review
							if (Long.toString(NbaOliConstants.OLI_RISKRIGHTER_CASE).equals(getWork().getNbaCase().getNbaLob().getRiskRighterCase())
									&& NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(nbaTransaction.getWorkType())) {
								transactionLOB.setRiskRighterCase(getWork().getNbaCase().getNbaLob().getRiskRighterCase());
								transactionLOB.setReinVendorID(NbaConstants.TRUE_STR);
							}
							// APSL5321 End
						}
					}
				}
			}
		}
		if (NbaUtils.convertStringToLong(transactionLOB.getReqStatus()) != NbaOliConstants.OLI_REQSTAT_RECEIVED) { //NBA192
			updateTransactionForRequirementSources(transactionLOB); //NBA192
		} //NBA192
		//NBA119 Code deleted.
	}

	/**
	 * Checks if a requirement can be satisfied with sources on the case. Calls VP/MS model to get list of sources that can be used to satisfy this
	 * requirement. If any source on case matches with the source type return from VP/MS model than sets the req status LOB to received.
	 * @param transactionLOB the requirement workitem's LOBs
	 * @throws NbaBaseException
	 */
	//NBA192 New Method
	protected void updateTransactionForRequirementSources(NbaLob transactionLOB) throws NbaBaseException {
		List requirementSources = new ArrayList();
		NbaVpmsModelResult data = new NbaVpmsModelResult(getDataFromVpms(NbaVpmsConstants.REQUIREMENTS, NbaVpmsConstants.EP_GET_REQUIREMENT_SOURCES,
				new NbaOinkDataAccess(transactionLOB), null, null).getResult());
		if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
			ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
			int resultSize = resultData.getResult().size();
			for (int i = 0; i < resultSize; i++) {
				requirementSources.add(resultData.getResultAt(i));
			}
		}
		if (requirementSources != null && !requirementSources.isEmpty()) {
			List sources = getWork().getNbaCase().getNbaSources();
			int sourceCount = sources.size();
			int requirementSourceCount = requirementSources.size();
			String sourceType = null;
			for (int i = 0; i < sourceCount; i++) {
				sourceType = ((NbaSource) sources.get(i)).getSourceType();
				for (int j = 0; j < requirementSourceCount; j++) {
					if (sourceType.equalsIgnoreCase((String) requirementSources.get(j))) {
						transactionLOB.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
						break;
					}
				}
			}
		}
	}

	/**
	 * Update requirement control source for requirement workitem and parent case.
	 * @throws NbaBaseException
	 */
	//SPR2399 New Method
	protected void updateRequirementControlSources() throws NbaBaseException {
		List nbaTransactions = getWork().getNbaTransactions();
		NbaRequirementUtils nbaReqUtils = new NbaRequirementUtils();
		NbaTransaction nbaTransaction = null;
		int transCount = nbaTransactions.size();
		for (int j = 0; j < transCount; j++) {
			nbaTransaction = (NbaTransaction) nbaTransactions.get(j);
			if (NbaConstants.A_WT_REQUIREMENT.equals(nbaTransaction.getTransaction().getWorkType())) {
				//always true for req determination
				nbaReqUtils.setAutoGeneratedInd(true);
				nbaReqUtils.setEmployeeId(getUser().getUserID());
				oinkDataAccess.setLobSource(nbaTransaction.getNbaLob());
				nbaReqUtils.setOinkDataAccess(oinkDataAccess);
				nbaReqUtils.addRequirementControlSource(nbaTransaction);
				nbaReqUtils.addMasterRequirementControlSource(getWork(), nbaTransaction);
				updateRequirementInfo(nbaTransaction, nbaTxLife.getRequirementInfo(nbaTransaction.getNbaLob().getReqUniqueID())); //NBA130
			}
		}
	}

	/**
	 * Find and update pass status on Re-Evaluate Work Item
	 * @throws NbaBaseException
	 */
	//SPR2399 New Method
	protected void updateReEvaluateWorkItem() throws NbaBaseException {
		//AXAL3.7.07 code deleted 
		if (debugLogging) {
			getLogger().logDebug("Requirements Determination for Re-Evaluate Work Item completed");
		}
		ListIterator transList = getWork().getNbaTransactions().listIterator();
		NbaTransaction nbaTrans = null;
		String origWorkItemId = getOrigWorkItem().getID(); //NBA208-32
		while (transList.hasNext()) {
			nbaTrans = (NbaTransaction) transList.next();
			if (A_WT_REEVALUATE.equals(nbaTrans.getTransaction().getWorkType()) && origWorkItemId.equals(nbaTrans.getID())) { //NBA208-32
				nbaTrans.setStatus(getPassStatus());
				break;
			}
		}
	}

	//SPR2742 code deleted
	/** Reterive a list of required system messages Id.
	 * @return
	 */
	//AXAL3.7.40 new method added. 
	// Refactored A2_AXAL002,ALS1929,ALS2305 
	public List getSystemMessagesIdsList(Party party) {
		List systemIdsList = new ArrayList();
		Holding holding = getNbaTxLife().getPrimaryHolding();
		List msgList = holding.getSystemMessage();
		if (msgList != null) {
			for (Iterator iter = msgList.iterator(); iter.hasNext();) {
				SystemMessage systemMessage = (SystemMessage) iter.next();
				SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(systemMessage);
				//ALII2083 start - code refactor to avoid NullPointerException
				if (systemMessageExtension != null) {
					String requirementKey = systemMessageExtension.getMsgRequirementKey();
					if (requirementKey != null) {
						if ((nbaTxLife.isOwner(party.getId()) && !nbaTxLife.isInsured(party.getId()))
								&& systemMessage.getRelatedObjectID().startsWith("Party")) {//ALII681, QC6003
							if (systemMessage.getRelatedObjectID().equals(party.getId())) {
								systemIdsList.add(requirementKey);
							}
						} else {
							systemIdsList.add(requirementKey);
						}
					}
				}
				//ALII2083 end
			}
		}
		return systemIdsList;
	}

	/**
	 * deoink all form instance and response
	 */

	//3.7.06 New Method P2AXAL054 Signature Changed
	protected void deOinkFormInstance(Map deOink, Party party) {
		ArrayList formInstanceList = nbaTxLife.getOLifE().getFormInstance();
		FormInstance formInstance = null;
		
		FormInstanceExtension formInstanceExtension = null;//P2AXAL054
		FormResponse formResponse = null;
		List formInstanceForParty = filterFormInstanceForParty(formInstanceList, party.getId());//P2AXAL054
		int formCnt = formInstanceForParty.size();
		int listSize = 0;
		String formName = "";
		String queNo = null;
		String queText = null;
		int responseCode = 0;
		deOink.put("A_no_of_Forms", (new Integer(formCnt)).toString());

		for (int i = 0; i < formCnt; i++) {
			formInstance = (FormInstance) formInstanceForParty.get(i);
			// begin P2AXAL054
			formInstanceExtension = NbaUtils.getFirstFormInstanceExtension(formInstance);
			boolean formRecieved = false;
			if(formInstanceExtension != null){
				formRecieved = formInstanceExtension.getFormRecdConfirmInd();
			}
			//end P2AXAL054
			if (formInstance != null) {
				formName = formInstance.getFormName();
				listSize = formInstance.getFormResponseCount();
			} else {
				formName = "";
				listSize = 0;
			}

			if (i == 0) {
				deOink.put("A_FormName", formName);
				deOink.put("A_no_of_Responses", (new Integer(listSize)).toString());
				deOink.put("A_FormReceived", formRecieved ? "true" : "false");//P2AXAL054
			} else {
				deOink.put("A_FormName[" + i + "]", formName);
				deOink.put("A_no_of_Responses[" + i + "]", (new Integer(listSize)).toString());
				deOink.put("A_FormReceived[" + i + "]", formRecieved ? "true" : "false");//P2AXAL054
			}
			for (int k = 0; k < listSize; k++) {
				formResponse = formInstance.getFormResponseAt(k);
				if (formResponse != null) {
					queNo = formResponse.getQuestionNumber();
					responseCode = formResponse.getResponseCode();
					queText = formResponse.getQuestionText();
				} else {
					queNo = "";
					responseCode = 0;
					queText = "";
				}
				if (i == 0 && k == 0) {
					deOink.put("A_QuestionNumber", queNo);
					deOink.put("A_ResponseCode", String.valueOf(responseCode));
					deOink.put("A_QuestionText", queText);
				} else {
					deOink.put("A_QuestionNumber[" + i + "," + k + "]", queNo);
					deOink.put("A_ResponseCode[" + i + "," + k + "]", String.valueOf(responseCode));
					deOink.put("A_QuestionText[" + i + "," + k + "]", queText);
				}
			} // end inner for
		} // end outter for
	}

	/**
	 * This method gets all the deOink variables for ACRequirementDetermination model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//AXA3.7.06
	protected void deOinkBeneOwnerValues(Map deOink) throws NbaBaseException {
		List relToAnnOrInsBenList = new ArrayList();
		List relToAnnOrInsCbnList = new ArrayList();
		List relToAnnOrInsOwnList = new ArrayList();
		List orgFormBenList = new ArrayList(); //ALS5554
		List orgFormCbnList = new ArrayList(); //ALS5554
		List orgFormOwnList = new ArrayList(); //ALS5554
		for (int i = 0; i < getNbaTxLife().getOLifE().getRelationCount(); i++) {
			Relation relation = getNbaTxLife().getOLifE().getRelationAt(i);
			if (relation != null && NbaOliConstants.OLI_REL_BENEFICIARY == relation.getRelationRoleCode()) {
				NbaOinkRequest oinkRequest = getOinkRequest(relation, "RELTOANNORINS_BEN");
				if (oinkRequest != null) {
					relToAnnOrInsBenList.addAll(Arrays.asList(oinkDataAccess.getStringValuesFor(oinkRequest)));
				}
				NbaOinkRequest oinkRequest1 = getOinkRequest(relation, "ORGFORM_BEN"); //ALS5554
				if (oinkRequest1 != null) {
					orgFormBenList.addAll(Arrays.asList(oinkDataAccess.getStringValuesFor(oinkRequest1))); //ALS5554
				}
			}
			if (relation != null && NbaOliConstants.OLI_REL_CONTGNTBENE == relation.getRelationRoleCode()) {
				NbaOinkRequest oinkRequest = getOinkRequest(relation, "RELTOANNORINS_CBN");
				if (oinkRequest != null) {
					relToAnnOrInsCbnList.addAll(Arrays.asList(oinkDataAccess.getStringValuesFor(oinkRequest)));
				}
				NbaOinkRequest oinkRequest1 = getOinkRequest(relation, "ORGFORM_CBN"); //ALS5554
				if (oinkRequest1 != null) {
					orgFormCbnList.addAll(Arrays.asList(oinkDataAccess.getStringValuesFor(oinkRequest1))); //ALS5554
				}
			}
			if (relation != null && NbaOliConstants.OLI_REL_OWNER == relation.getRelationRoleCode()) {
				NbaOinkRequest oinkRequest = getOinkRequest(relation, "RELTOANNORINS_OWN");
				if (oinkRequest != null) {
					relToAnnOrInsOwnList.addAll(Arrays.asList(oinkDataAccess.getStringValuesFor(oinkRequest)));
				}
				NbaOinkRequest oinkRequest1 = getOinkRequest(relation, "ORGFORM_OWN"); //ALS5554
				if (oinkRequest1 != null) {
					orgFormOwnList.addAll(Arrays.asList(oinkDataAccess.getStringValuesFor(oinkRequest1))); //ALS5554
				}
			}
		}
		int countBen = relToAnnOrInsBenList.size();
		int countCbn = relToAnnOrInsCbnList.size();
		int countOwn = relToAnnOrInsOwnList.size();
		deOink.put("A_NO_OF_RELTOANNORINS_BEN", String.valueOf(countBen));
		deOink.put("A_NO_OF_RELTOANNORINS_CBN", String.valueOf(countCbn));
		deOink.put("A_NO_OF_RELTOANNORINS_OWN", String.valueOf(countOwn));
		deOink.put("A_RELTOANNORINS_BEN", relToAnnOrInsBenList.toArray(new String[countBen]));
		deOink.put("A_RELTOANNORINS_CBN", relToAnnOrInsCbnList.toArray(new String[countCbn]));
		deOink.put("A_RELTOANNORINS_OWN", relToAnnOrInsOwnList.toArray(new String[countOwn]));
		deOink.put("A_NO_OF_ORGFORM_BEN", String.valueOf(orgFormBenList.size())); //ALS5554
		deOink.put("A_NO_OF_ORGFORM_CBN", String.valueOf(orgFormCbnList.size())); //ALS5554
		deOink.put("A_NO_OF_ORGFORM_OWN", String.valueOf(orgFormOwnList.size())); //ALS5554
		deOink.put("A_ORGFORM_BEN", orgFormBenList.toArray(new String[orgFormBenList.size()])); //ALS5554
		deOink.put("A_ORGFORM_CBN", orgFormCbnList.toArray(new String[orgFormCbnList.size()])); //ALS5554
		deOink.put("A_ORGFORM_OWN", orgFormOwnList.toArray(new String[orgFormOwnList.size()])); //ALS5554
	}

	/**
	 * This method deOinks GovtID variables for ACRequirementDetermination model. Since owners are sent to the model, the deOINK variables can be used
	 * to determine the OWNER GovtId of the party being processed whether it is an owner or an insured.
	 * 
	 * @param Map
	 *                deOink
	 * @param Party
	 *                party
	 * @return java.util.Map : The Hash Map containing all the deOink variables
	 * @throws NbaBaseException
	 */
	//	AXA3.7.06
	protected void deOinkGovtIDValues(Map deOink, Party party) {
		Party ownerParty = null;
		if (getNbaTxLife().isOwner(party.getId())) {//ALS3654
			ownerParty = party;
		} else {
			ownerParty = getOwnerParty();
		}
		if (ownerParty !=null && ownerParty.hasGovtID()) {
			deOink.put("A_GOVTID_OWN", ownerParty.getGovtID());
		} else {
			deOink.put("A_GOVTID_OWN", "");
		}
	}

	/**
	 * Find owner party when insured is not the owner
	 */
	/**
	 * @return Party
	 */
	//	AXA3.7.06
	protected Party getOwnerParty() {
		OLifE olife = getNbaTxLife().getOLifE();
		int partyCount = olife.getPartyCount();
		Party ownerParty = null;
		Party party = null;
		for (int index = 0; index < partyCount; index++) {
			party = olife.getPartyAt(index);
			if (getNbaTxLife().isOwner(party.getId())) {//ALS3654
				ownerParty = party;
				break;//ALS3654
			}
		}
		return ownerParty;
	}

	/**
	 * @param relation
	 * @param name
	 * @return
	 */
	protected NbaOinkRequest getOinkRequest(Relation relation, String name) {
		NbaParty nbaParty = getNbaTxLife().getParty(relation.getRelatedObjectID());
		NbaOinkRequest oinkRequest = null;
		if (nbaParty != null && nbaParty.getParty() != null) {
			oinkRequest = new NbaOinkRequest();
			oinkRequest.setPartyFilter(relation.getRelationRoleCode(), relation.getRelatedRefID());
			oinkRequest.setArgs(getKeys(nbaParty.getParty()));
			oinkRequest.setVariable(name);
		}
		return oinkRequest;
	}

	/**
	 * This method gets all the deOink variables for ACRequirementDetermination model
	 * 
	 * @param
	 * @return java.util.Map : The Hash Map containing all the deOink variables
	 * @throws NbaBaseException
	 */
	//AXA3.7.06
	protected void deOinkSignatureValues(Map deOink) {
		List signatureList = nbaTxLife.getPolicy().getApplicationInfo().getSignatureInfo();
		List signatureCodeList = new ArrayList(); //AXAL3.7.40
		List signaturePurposeList = new ArrayList();
		List signatureRoleCodeList = new ArrayList();
		List signatureTypeList = new ArrayList();
		List signatureOkindCodeList = new ArrayList(); //AXAL3.7.40
		int sigCount = signatureList.size();
		if (signatureList != null && sigCount > 0) {
			for (int i = 0; i < sigCount; i++) {
				SignatureInfo signatureInfo = (SignatureInfo) signatureList.get(i);
				signatureCodeList.add(signatureInfo.getSignatureCode()); //AXAL3.7.40
				signaturePurposeList.add(new Long(signatureInfo.getSignaturePurpose()).toString());
				signatureRoleCodeList.add(new Long(signatureInfo.getSignatureRoleCode()).toString());
				SignatureInfoExtension signInfoExtn = NbaUtils.getFirstSignatureInfoExtension(signatureInfo);
				if (signInfoExtn != null) {
					signatureTypeList.add(new Long(signInfoExtn.getSignatureType()).toString());
					signatureOkindCodeList.add(new Long(signInfoExtn.getSignatureOKIndCode()).toString()); //AXAL3.7.40 
				}
			}
		}
		deOink.put("A_SignatureCodeList", signatureCodeList.toArray(new String[signatureCodeList.size()])); //AXAL3.7.40
		deOink.put("A_SignaturePurposeList", signaturePurposeList.toArray(new String[signaturePurposeList.size()]));
		deOink.put("A_SignatureRoleCodeList", signatureRoleCodeList.toArray(new String[signatureRoleCodeList.size()]));
		deOink.put("A_SignatureTypeList", signatureTypeList.toArray(new String[signatureTypeList.size()]));
		deOink.put("A_SignatureOkIndCodeList", signatureOkindCodeList.toArray(new String[signatureOkindCodeList.size()])); //AXAL3.7.40
		deOink.put("A_no_of_SignatureList", String.valueOf(signatureList.size()));
	}

	/**
	 * This method gets all the deOink variables for ACRequirementDetermination model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//	AXA3.7.06
	protected void deOinkReplacementValues(Map deOink) {
		ArrayList partyIdList = new ArrayList();
		ArrayList holdingIdList = new ArrayList();
		List fullNameList = new ArrayList();
		List polNumList = new ArrayList();
		List repYearIssuedList = new ArrayList();
		List replaceContractTypeList = new ArrayList();
		List replacementIndCodeList = new ArrayList();
		List replacementProductTypeList = new ArrayList();
		List replacementTypeList = new ArrayList();
		int relationCnt = nbaTxLife.getOLifE().getRelationCount();
		List partyList = nbaTxLife.getOLifE().getParty();
		List HoldingList = nbaTxLife.getOLifE().getHolding();
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = nbaTxLife.getOLifE().getRelationAt(j);
			if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO && aRelation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING) {
				partyIdList.add(aRelation.getRelatedObjectID());
				holdingIdList.add(aRelation.getOriginatingObjectID());
			}
		}
		int partyIdCnt = partyIdList.size();
		int holdingIdCnt = holdingIdList.size();
		for (int i = 0; i < partyIdCnt; i++) {
			String partyId = (String) partyIdList.get(i);
			Party aParty = NbaTXLife.getPartyFromId(partyId, partyList);
			if (aParty.hasFullName()) {
				fullNameList.add(aParty.getFullName());
			}
		}
		for (int i = 0; i < holdingIdCnt; i++) {
			String holdingId = (String) holdingIdList.get(i);
			Holding holding = NbaTXLife.getHoldingFromId(holdingId, HoldingList);
			Policy repPolicy = holding.getPolicy();
			polNumList.add(repPolicy.getPolNumber());
			replacementTypeList.add(String.valueOf(repPolicy.getType()));
			PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(repPolicy);
			repYearIssuedList.add(polExtn.getYearIssued());
			replaceContractTypeList.add(new Long(polExtn.getReplaceContractType()).toString());
			replacementIndCodeList.add(new Long(polExtn.getReplacementIndCode()).toString());
			replacementProductTypeList.add(new Long(polExtn.getReplaceProductType()).toString());

		}
		deOink.put("A_ReplFullNameList", fullNameList.toArray(new String[fullNameList.size()]));
		deOink.put("A_ReplPolNumberList", polNumList.toArray(new String[polNumList.size()]));
		deOink.put("A_ReplYearIssuedList", repYearIssuedList.toArray(new String[repYearIssuedList.size()]));
		deOink.put("A_ReplContractTypeList", replaceContractTypeList.toArray(new String[replaceContractTypeList.size()]));
		deOink.put("A_ReplIndCodeList", replacementIndCodeList.toArray(new String[replacementIndCodeList.size()]));
		deOink.put("A_ReplProductTypeList", replacementProductTypeList.toArray(new String[replacementProductTypeList.size()]));
		deOink.put("A_ReplacementTypeList", replacementTypeList.toArray(new String[replacementTypeList.size()]));
		deOink.put("A_no_of_ReplContracts", String.valueOf(holdingIdList.size()));
	}

	/**
	 * This method gets all the deOink variables for ACRequirementDetermination model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//	AXA3.7.06
	protected void deOinkCovOptionValues(Map deOink) {
		Life life = nbaTxLife.getLife();
		int covCount = life.getCoverageCount();
		int covOptionCount = 0;
		List coverageProductCodeList = new ArrayList();
		List lifeCovTypeCodeList = new ArrayList();
		List indicatorCodeList = new ArrayList();
		List unbornChildIndList = new ArrayList();
		List covOptionProductCodeList = new ArrayList();
		List lifeCovOptTypeCodeList = new ArrayList();
		List selectionRuleList = new ArrayList();
		List parentLifeCovTypeCodeList = new ArrayList();
		for (int i = 0; i < covCount; i++) {
			Coverage coverage = life.getCoverageAt(i);
			int covOptCount = coverage.getCovOptionCount();
			coverageProductCodeList.add(coverage.getProductCode());
			lifeCovTypeCodeList.add(String.valueOf(coverage.getLifeCovTypeCode()));
			indicatorCodeList.add(String.valueOf(coverage.getIndicatorCode()));
			CoverageExtension covExtn = NbaUtils.getFirstCoverageExtension(coverage);
			unbornChildIndList.add(new Boolean(covExtn != null ? covExtn.getUnbornChildInd() : false).toString());
			covOptionCount = covOptionCount + covOptCount;
			//deoink covoption lists			
			for (int j = 0; j < covOptCount; j++) {
				CovOption covOption = coverage.getCovOptionAt(j);
				parentLifeCovTypeCodeList.add(String.valueOf(coverage.getLifeCovTypeCode()));
				covOptionProductCodeList.add(covOption.getProductCode());
				lifeCovOptTypeCodeList.add(String.valueOf(covOption.getLifeCovOptTypeCode()));
				CovOptionExtension covOptionExtn = NbaUtils.getFirstCovOptionExtension(covOption);
				selectionRuleList.add(String.valueOf(covOptionExtn != null ? covOptionExtn.getSelectionRule() : -1));
			}
		}
		deOink.put("A_CoverageProductCodeList", coverageProductCodeList.toArray(new String[coverageProductCodeList.size()]));		
		deOink.put("A_LifeCovTypeCodeList", lifeCovTypeCodeList.toArray(new String[lifeCovTypeCodeList.size()]));
		deOink.put("A_IndicatorCodeList", indicatorCodeList.toArray(new String[indicatorCodeList.size()]));
		deOink.put("A_no_of_CoverageProductCode", Integer.toString(covCount));
		deOink.put("A_UnbornChildIndList", unbornChildIndList.toArray(new String[unbornChildIndList.size()]));
		deOink.put("A_CovOptionProductCodeList", covOptionProductCodeList.toArray(new String[covOptionProductCodeList.size()]));
		deOink.put("A_LifeCovOptTypeCodeList", lifeCovOptTypeCodeList.toArray(new String[lifeCovOptTypeCodeList.size()]));
		deOink.put("A_no_of_CovOptionProductCode", Integer.toString(covOptionCount));
		deOink.put("A_SelectionRuleList", selectionRuleList.toArray(new String[selectionRuleList.size()]));
		deOink.put("A_ParentLifeCovTypeCodeList", parentLifeCovTypeCodeList.toArray(new String[parentLifeCovTypeCodeList.size()]));
	}
	
	/**
	 * Returns if Contract Change Form is available on recent NBCNTCHG WI. 	 
	 * @param 
	 * @return boolean	
	 * @throws NbaBaseException
	 */
	//ALS5351 New Method
	protected boolean hasContractChangeForm()throws NbaBaseException {
		//BEGIN APSL5349
		ContractChangeInfo latestContractChange = NbaUtils.getLatestValidContractChangeInfo(nbaTxLife);
		if (!NbaUtils.isBlankOrNull(latestContractChange)) {
			List<Activity> activityList = nbaTxLife.getOLifE().getActivity();
			List<Activity> amicaActivityList = NbaUtils.getActivityByTypeCodeAndRelatedObjId(activityList, NbaOliConstants.OLI_ACTTYPE_AMICACONTRACTCHANGE, latestContractChange.getId());
			if (!NbaUtils.isBlankOrNull(amicaActivityList) &&
					amicaActivityList.size() > 0)  {
				return true;
			}
		}
		return false;
		//END APSL5349
		/*	NbaTransaction changeFormTransaction = getRecentTransaction(A_WT_CONTRACT_CHANGE);
			if(changeFormTransaction != null){
				List sources = changeFormTransaction.getNbaSources();
				for (int j = 0; j < sources.size(); j++) {
					NbaSource aSource = (NbaSource) sources.get(j);
					if (NbaConstants.A_ST_CHANGE_FORM.equals(aSource.getSource().getSourceType())) {				
						return true;
					}
				}
			}
			return false;
		*/	
	}
	
	/**
	 * Returns the most recent transaction from the case. 	 
	 * @param transactionType to be returned
	 * @return NbaTransaction : The most recent transaction 	
	 * @throws NbaBaseException
	 */
	//ALS5351 New Method
	protected NbaTransaction getRecentTransaction(String transactionType) throws NbaBaseException {
		List transactions = getWork().getNbaTransactions();
		NbaTransaction tempTrans = null;
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction transaction = (NbaTransaction) transactions.get(i);
			if(transactionType.equals(transaction.getNbaLob().getWorkType())){
				if(tempTrans == null || 
						(tempTrans != null && 
						NbaUtils.getDateFromStringInAWDFormat(tempTrans.getNbaLob().getCreateDate()).before(NbaUtils.getDateFromStringInAWDFormat(transaction.getNbaLob().getCreateDate())))){
					tempTrans = transaction;
				}
			}
		}
		return tempTrans;
	}

	//ALS4322 method moved to RequirementUtils
	
	/**
	 * This method gets the deOink MedicalConditionExtension.RecommendTreatmentIndCode for ACRequirementDetermination model	 
	 * @param deOink
	 * @param risk
	 */
	// AXAL3.7.40 new method
	protected void deOinkRecommendTreatmentIndCodeForQNO9(Map deOink, Risk risk) {
		String recommendTreatmentIndCode = "";
		for (int i = 0; i < risk.getMedicalConditionCount(); i++) {
			MedicalCondition medCond = risk.getMedicalConditionAt(i);
			MedicalConditionExtension medicalConditionExtn = NbaUtils.getFirstMedicalConditionExtension(medCond);
			if (medicalConditionExtn != null && MEDSUPP_QUESTION_NUMBER_9.equals(medicalConditionExtn.getQuestionNumber())) {
				recommendTreatmentIndCode = String.valueOf(medicalConditionExtn.getRecommendTreatmentIndCode());
				break;
			}
		}
		deOink.put("A_RecommendTreatmentIndCode", recommendTreatmentIndCode);
	}

	/**
	 * This method creates the query using the formInstance data from the VPMS.
	 * @param Policy
	 * @param relationRoleCode
	 * @param formInstanceData
	 */
	// NBA250 new method
	public String getGenericReqQueryForFormsTable(Policy policy, long relationRoleCode, FormInstance formInstanceData) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		String appSignedDate = null;
		if(policy.getApplicationInfo().getSignedDate() != null) {//ALS5664
			appSignedDate = df.format(policy.getApplicationInfo().getSignedDate());
		}else {
			appSignedDate = df.format(new Date());//ALS5664
		}
		StringBuffer query = new StringBuffer("Select COMPANY_CODE, APPLICATION_TYPE, PRODUCT_TYPE, COVERAGE_KEY, REPLACEMENT_IND, "
				+ "INS_ROLE, START_DATE, END_DATE, APPLICATION_STATE, QUESTION_NUMBER, QUESTION_ANSWER, REQUIREMENT_TYPE, FORM_NUMBER, "
				+ "PROVIDER, FOLLOWUP_PROVIDER, FORM_TYPE from NBA_FORMS_VALIDATION"); //NBA231

		query.append(" WHERE (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COMPANY_CODE, policy.getCarrierCode()));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COMPANY_CODE, "*"));
		query.append(")");
		query.append(" AND ("
				+ NbaTableHelper.formatSQLWhereCriterion(NbaTable.APP_TYPE, Long.toString(policy.getApplicationInfo().getApplicationType())));
		query.append(") AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.PRODUCT_TYPE, Long.toString(policy.getProductType())));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.PRODUCT_TYPE, "*"));
		query.append(") AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COVERAGE_KEY, policy.getProductCode()));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COVERAGE_KEY, "*"));
		query.append(")");
		query.append(" AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.INS_ROLE, relationRoleCode));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.INS_ROLE, "*"));
		query.append(")");
		query.append(" AND " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.APPLICATION_STATE, "*"));
		query.append(" AND (to_date('" + appSignedDate.toString() + "', 'mm-dd-yy') BETWEEN START_DATE AND END_DATE)");
		if (null != formInstanceData) {
			int formResCount = formInstanceData.getFormResponseCount();
			if (formResCount > 0) {
				query.append(" AND (");
				for (int j = 0; j < formResCount; j++) {
					FormResponse formResponse = formInstanceData.getFormResponseAt(j);
					query.append("(" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.QUESTION_NUMBER, formResponse.getQuestionNumber()));
					query.append(" AND "
							+ NbaTableHelper.formatSQLWhereCriterion(NbaTable.QUESTION_ANSWER, String.valueOf(formResponse.getResponseCode())));
					query.append(")");
					if (j < formResCount - 1) {
						query.append(" OR ");
					}
				}
				query.append(")");
			}
		}

		return query.toString();
	}

	//	AXAL3.7.40G New Method Added.
	private boolean isOmissionRequirement(long reqCode) {
		int length = NbaConstants.CONTRACT_VALIDATION_MESSAGES_REQUIREMENTS.length;
		for (int i = 0; i < length; i++) {
			if (reqCode == NbaConstants.CONTRACT_VALIDATION_MESSAGES_REQUIREMENTS[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param requirements
	 * @throws NbaBaseException
	 */
	//AXAL3.7.40G New Method Added.
	private List filterVpmsRequirements(List requirements) throws NbaBaseException {
		ArrayList newList = new ArrayList();
		if (requirements != null) {
			ListIterator li = requirements.listIterator();
			if (preventProcess) {
				while (li.hasNext()) {
					NbaVpmsRequirement vpmsRequirement = (NbaVpmsRequirement) li.next();
					if (isOmissionRequirement(vpmsRequirement.getType())) {
						newList.add(vpmsRequirement);
					}
				}
				return newList;
			}
		}
		return requirements;
	}

	/**
	 * This method creates the query using the formInstance data from the VPMS.
	 * @param Policy
	 * @param relationRoleCode
	 * @param formInstanceData
	 */
	// NBA250 new method //ALS4615 signature changed
	public String getSpecificReqQueryForFormsTable(Policy policy, long relationRoleCode, FormResponse formResponse) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		String appSignedDate = null;
		
		
		if(policy.getApplicationInfo().getSignedDate() != null) {//ALS5664
			appSignedDate = df.format(policy.getApplicationInfo().getSignedDate());
		}else {
			appSignedDate = df.format(new Date());//ALS5664
		}
		//Start NBLXA-1790
		SignatureInfo signInfo = null;
		long contractSignedState = -1L;
		if (nbaTxLife != null) {
			Relation rel = nbaTxLife.getRelationForRelationRoleCode(relationRoleCode);
			if (FORM_NAME_HIPAA.equalsIgnoreCase(formResponse.getQuestionNumber()) && rel != null) {
				if (rel.getRelationRoleCode() == NbaOliConstants.OLI_PARTICROLE_32) {
					signInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY, NbaOliConstants.OLI_SIGTYPE_APPSIG,
							nbaTxLife);
					if (signInfo != null) {
						contractSignedState = signInfo.getSignatureState();
					}
				} else if (rel.getRelationRoleCode() == NbaOliConstants.OLI_REL_JOINTINSURED) {
					signInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_JOINT, NbaOliConstants.OLI_SIGTYPE_APPSIG,
							nbaTxLife);
					if (signInfo != null) {
						contractSignedState = signInfo.getSignatureState();
					}
				}
			}
		}
		if (NbaUtils.isNull(contractSignedState)) {
			contractSignedState = policy.getApplicationInfo().getApplicationJurisdiction();
		}
		//End NBLXA-1790
		StringBuffer query = new StringBuffer("Select COMPANY_CODE, APPLICATION_TYPE, PRODUCT_TYPE, COVERAGE_KEY, REPLACEMENT_IND, "
				+ "INS_ROLE, START_DATE, END_DATE, APPLICATION_STATE, QUESTION_NUMBER, QUESTION_ANSWER, REQUIREMENT_TYPE, FORM_NUMBER, "
				+ "PROVIDER, FOLLOWUP_PROVIDER, FORM_TYPE from NBA_FORMS_VALIDATION"); //NBA231

		query.append(" WHERE (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COMPANY_CODE, policy.getCarrierCode()));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COMPANY_CODE, "*"));
		query.append(")");
		query.append(" AND ("
				+ NbaTableHelper.formatSQLWhereCriterion(NbaTable.APP_TYPE, Long.toString(policy.getApplicationInfo().getApplicationType())));
		query.append(") AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.PRODUCT_TYPE, Long.toString(policy.getProductType())));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.PRODUCT_TYPE, "*"));
		query.append(") AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COVERAGE_KEY, policy.getProductCode()));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.COVERAGE_KEY, "*"));
		query.append(")");
		query.append(" AND (" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.INS_ROLE, relationRoleCode));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.INS_ROLE, "*"));
		query.append(")");
		query.append(" AND ("
				+ NbaTableHelper.formatSQLWhereCriterion(NbaTable.APPLICATION_STATE, Long.toString(contractSignedState)));
		query.append(" OR " + NbaTableHelper.formatSQLWhereCriterion(NbaTable.APPLICATION_STATE, "*"));
		query.append(")");
		query.append(" AND (to_date('" + appSignedDate.toString() + "', 'mm-dd-yy') BETWEEN START_DATE AND END_DATE)");
		query.append(" AND ");
		query.append("(" + NbaTableHelper.formatSQLWhereCriterion(NbaTable.QUESTION_NUMBER, formResponse.getQuestionNumber()));
		query.append(" AND "+ NbaTableHelper.formatSQLWhereCriterion(NbaTable.QUESTION_ANSWER, String.valueOf(formResponse.getResponseCode())));
		query.append(")");
		//ALS4615 code deleted
		return query.toString();
	}
		
	//ALS2584 New Method
	protected void initializeMiscMailSourceList(List miscMailSourceList) throws NbaBaseException {
		List sources = getWork().getNbaCase().getNbaSources();
		int sourceCount = sources.size();
		for (int i = 0; i < sourceCount; i++) {
			NbaSource nbaSource = (NbaSource) sources.get(i);
			if (nbaSource.isMiscMail() && !((NbaConstants.CREATE_STATION_NETSERVWS.equalsIgnoreCase(nbaSource.getNbaLob().getCreateStation()))
					&& !nbaSource.getNbaLob().getPortalCreated())) {//QC8185
				miscMailSourceList.add(nbaSource.getID());
			}
		}
	}
	
	//ALS2584 New Method
	//ALS4751 Name changed
	protected void removeMiscMailSourceForForm(String formNumber) throws NbaBaseException {
		Iterator itr = miscMailSourceList.iterator();
		while (itr.hasNext()) {
			String nbaSourceId = (String) itr.next();
			NbaSource nbaSource = getMatchingSource(nbaSourceId);
			if (nbaSource != null && formNumber != null && formNumber.equalsIgnoreCase(nbaSource.getNbaLob().getFormNumber())) {
				itr.remove();
				break;
			}
		}
	}
	
	//ALS4751 New Method
	protected void removeMiscMailSourceForReqType(int ReqType) throws NbaBaseException {
		Iterator itr = miscMailSourceList.iterator();
		while (itr.hasNext()) {
			String nbaSourceId = (String) itr.next();
			NbaSource nbaSource = getMatchingSource(nbaSourceId);
			if (nbaSource != null && ReqType == nbaSource.getNbaLob().getReqType()) {
				itr.remove();
				break;
			}
		}
	}
	
	//ALS2584 New Method
	protected NbaSource getMatchingSource(String sourceId) throws NbaBaseException {
		List sources = getWork().getNbaCase().getNbaSources();
		int sourceCount = sources.size();
		for (int i = 0; i < sourceCount; i++) {
			NbaSource nbaSource = (NbaSource) sources.get(i);
			if (nbaSource.isMiscMail() && sourceId.equalsIgnoreCase(nbaSource.getID())) {
				return nbaSource;
			}
		}
		return null;
	}
	
	//New Method - ALS2584
	private boolean sourceNotAlreadyProcessed(NbaSource aSource) throws NbaBaseException {
		List aTransactionList = getWork().getNbaTransactions();
		int transactionListSize = aTransactionList.size();
		for (int i = 0; i < transactionListSize; i++) {
			NbaTransaction aTransaction = (NbaTransaction) aTransactionList.get(i);
			NbaLob lob = aTransaction.getNbaLob();
			if (A_WT_REQUIREMENT.equals(lob.getWorkType()) || A_WT_MISC_MAIL.equals(lob.getWorkType())) {
				List sources = aTransaction.getNbaSources();
				int size = sources.size();
				for (int j = 0; j < size; j++) {
					NbaSource currentSource = (NbaSource) sources.get(j);
					if (currentSource != null && aSource != null && aSource.getID().equals(currentSource.getID())) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/*
	 * This method loops through all MiscMail sources to determine if a MiscMail Work Item should be created.
	 */ 
	// New Method - ALS2584
	protected void createMiscMailTransactions() {
		try {
			Map deOinkMap = new HashMap();
			deOinkFormType(deOinkMap);
			List allSources = getWork().getNbaSources();
			int size = allSources.size();
			NbaTransaction aTransaction = null;
			for (int i = 0; i < size; i++) {
				NbaSource aSource = (NbaSource) allSources.get(i);
				String formNumber = aSource.getNbaLob().getFormNumber();
				if (aSource.isMiscMail() && miscMailSourceList.contains(aSource.getID()) && sourceNotAlreadyProcessed(aSource)) { //ALS2584
					deOinkMap.put("A_ReplacementTransaction", "false");
					deOinkMap.put("A_SourceTypeLOB", aSource.getSourceType());
					if (formNumber != null) {
						deOinkMap.put("A_FormNumberLOB", formNumber);
					} else {
						deOinkMap.put("A_FormNumberLOB", "");
					}
					String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
					if (workTypeAndStatus[0] != null) {
						// Create a new transaction, set LOBs, and move the source to the transaction
						aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
						aTransaction.increasePriority(workTypeAndStatus[2], workTypeAndStatus[3]);
						copyMiscMailLOBs(aTransaction, aSource);
						// APSL5321 Begin  For risk righter cases requirements for miscmail received with application should not go to UW or UWCM for review
						if (Long.toString(NbaOliConstants.OLI_RISKRIGHTER_CASE).equals(getWork().getNbaCase().getNbaLob().getRiskRighterCase())
								&& NbaConstants.A_ST_MISC_MAIL.equalsIgnoreCase(aTransaction.getWorkType())) {
							aTransaction.getNbaLob().setReinVendorID(NbaConstants.TRUE_STR);
						}
						// APSL5321 End
						//Begin ALS5119
						NbaSource newSource = null;
						if (!NbaUtils.isBlankOrNull(formNumber)) {
							newSource = aTransaction.addNbaSource(aSource);
						} else { //move everything else to MISCMAIL
							newSource = getWork().getNbaCase().moveNbaSource(aTransaction, aSource);
						} 
						// End ALS5119
						newSource.setUpdate();
					}
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
	}
	
	/**
	 * Return the Work Type and Status for the new transaction.
	 * @param Map of deOink variables
	 * @return String of work type and status
	 */
	//NBA231 new method
	protected String[] getWorkTypeAndStatus(Map deOinkMap) {
		String[] result = new String[4];
		try {
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(),deOinkMap); 
			result[0] = provider.getWorkType();
			result[1] = provider.getInitialStatus();
			result[2] = provider.getWIAction(); 
			result[3] = provider.getWIPriority();
		} catch (NbaBaseException nbe) {
			getLogger().logError(nbe);
		}
		return result;
	}
	
	//New Method - ALS2584
	protected void copyMiscMailLOBs(NbaTransaction aTransaction, NbaSource aSource) {
		try {
			NbaLob transLob = aTransaction.getNbaLob();
			NbaLob sourceLob = aSource.getNbaLob();
			
			VpmsModelResult vpmsModelResult = getLOBsToCopy(aTransaction, aSource);
			ArrayList strAttrs = vpmsModelResult.getStandardAttr();
            List lobList = new ArrayList();
            StandardAttr standardAttr = null;
			for (int i = 0; i < strAttrs.size(); i++) {
				standardAttr = (StandardAttr)strAttrs.get(i);
				lobList.add(standardAttr.getAttrValue());
			}
            sourceLob.copyLOBsTo(transLob, lobList);
		} catch (NbaBaseException nbe) {
			getLogger().logException(nbe);
		}
	}
	
	//New Method - ALS2584
	protected VpmsModelResult getLOBsToCopy(NbaTransaction aTransaction, NbaSource aSource) throws NbaBaseException {
		VpmsModelResult vpmsModelResult = new VpmsModelResult();
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(aSource.getNbaLob());
		NbaVpmsAdaptor nbaVpmsAdaptor = null;
        try {
            nbaVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.INDEX);
            nbaVpmsAdaptor.setVpmsEntryPoint(NbaVpmsConstants.EP_INDEX_PARENT_WORK_ITEM);
            nbaVpmsAdaptor.getSkipAttributesMap().put("A_SourceTypeLOB", aSource.getSourceType());
            nbaVpmsAdaptor.getSkipAttributesMap().put("A_WorkTypeLOB", aTransaction.getWorkType());
            try {
                VpmsComputeResult vpmsComputeResult = nbaVpmsAdaptor.getResults();
    			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsComputeResult);
    			// check if the vpms call was successfull
    			if (vpmsResultsData.wasSuccessful()) {
    				// got the xml result back
    				String xmlString = (String) vpmsResultsData.getResultsData().get(0);
    				// parsing the xml result
    				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
    				vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
    			}
            } catch (RemoteException e) {
                throw new NbaVpmsException(e);
            }
        } finally {
            if (nbaVpmsAdaptor != null) {
                try {
                    nbaVpmsAdaptor.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        }
		return vpmsModelResult;
	}	
	/**
	 * Provides additional initialization support by setting the
	 * pass/fail/error status values from VP/MS.
	 * Add De-Oink variable to determine if Case is Reg60 and issued other than applied for
	 * @see com.csc.fsg.nba.business.process.NbaProcessStatusProvider
	 */
	//NBA231 new method
	protected void initializeStatusFields() throws NbaBaseException {
		//ACN024 BEGIN
//		String businessProcess = NbaUtils.getBusinessProcessId(getUser()); //SPR2697 //ALS3928 commented Cache logic
		Map deOinkMap = new HashMap();
		String reg60 = "false";
		if (isReg60() && isIssueOtherThanAppliedFor()) {
			reg60 = "true";
		}
		deOinkMap.put("A_IssueOthrApplied", reg60);
		//ALS3928 commented Cache logic
//		statusProvider = NbaProcessStatusCache.getInstance().getStatusProvider(businessProcess); //SPR2697
//		if (statusProvider != null) {
//			return;
//		}
		
		statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), getRequirementInfo(), deOinkMap);
	}
	/**
	 * If ReplacementInd is set and Policy Jurisdiction is New York (tc 37) return true
	 * 
	 * @param NbaTXLife
	 * @result boolean
	 */
	//NBA231 new method
	protected boolean isReg60() {
		ApplicationInfo appInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		if (appInfo.getReplacementInd()
				&& appInfo.getApplicationJurisdiction() == NbaOliConstants.OLI_USA_NY) {
			return true;
		}
		return false;
	}
	/*
	 * determine if case other than applied for
	 */
	//NBA231 new method
	protected boolean isIssueOtherThanAppliedFor() {
		boolean otherThanApplied = false;
		try {
		  otherThanApplied = getWork().getNbaLob().getIssueOthrApplied();
		} catch (NbaBaseException nbe) {
			return false;
		}
		return otherThanApplied;
	}

	//ALS5119 refactoring
	protected void receiveRequirement(NbaTransaction nbaTransaction, NbaLob transactionLOB, NbaSource nbaSource) throws NbaBaseException {
		try {
			
			WorkItemSource source = nbaSource.getSource(); // Begin ALS5416
			if (NbaConstants.A_ST_MISC_MAIL.equalsIgnoreCase(source.getSourceType())) {
				source.setSourceType(NbaConstants.A_ST_PROVIDER_RESULT);
				source.setUpdate("Y");
			}											// End ALS5416
			NbaSource newSource = null;
			if (!NbaUtils.isBlankOrNull(nbaSource.getNbaLob().getFormNumber())  || nbaSource.getNbaLob().getReqType()==NbaOliConstants.OLI_REQCODE_700) { // //NBLXA-1554 (NBLXA-1888)) {
				newSource = nbaTransaction.addNbaSource(nbaSource);
			} else {
				newSource = getWork().getNbaCase().moveNbaSource(nbaTransaction, nbaSource);
			}
			newSource.getNbaLob().setReqType(nbaTransaction.getNbaLob().getReqType());//ALS5444
			newSource.setUpdate();
			transactionLOB.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
			transactionLOB.setReqReceiptDate(new Date()); //ALS2375
			transactionLOB.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date())); //QC20240
			transactionLOB.setParamedSignDate(nbaSource.getNbaLob().getParamedSignDate());//ALS5913
			transactionLOB.setFormRecivedWithAppInd(nbaSource.getNbaLob().getFormRecivedWithAppInd()); //ALS5276
			transactionLOB.setLabCollectionDate(nbaSource.getNbaLob().getLabCollectionDate());//NBLXA-1794
		} catch (Exception e) {
			e.printStackTrace();
			throw new NbaBaseException(e.getMessage());
		}
	}
	
	/**
	 * @return
	 * @throws NbaBaseException
	 */
	//ALS4923 New Method
	private String hasCWA() throws NbaBaseException {
		//NbaDst parentCase = retrieveCaseAndTransactions(getWork(), getUser(), false);
		List tempList = getWork().getNbaTransactions();
		String cwaPresent = "No";
		Iterator itr = tempList.iterator();
		while (itr.hasNext()) {
			NbaTransaction tempTrans = (NbaTransaction) itr.next();
			if (NbaConstants.A_WT_CWA.equals(tempTrans.getTransaction().getWorkType())) {
				cwaPresent = "Yes";
				break;
			}
		}
		return cwaPresent;
	}
	
	//QC8185 - Removed the method removeMiscMailSourceForCreateStation(). The logic is moved to initializeMiscMailSourceList() method.
	/**
	 * Filters the FormInstance and removes the forminstances which are specific for any other parties 
	 *  
	 */
	//P2AXAL054 New method
	protected List filterFormInstanceForParty(List formInstanceList, String partyId) {
		Iterator itr = formInstanceList.iterator();
		List formInstancesForParty = new ArrayList();
		while (itr.hasNext()) {
			FormInstance formInstance = (FormInstance) itr.next();
			if (!formInstance.hasRelatedObjectID() || formInstance.getRelatedObjectID().equalsIgnoreCase(partyId)) {//P2AXAL054
				formInstancesForParty.add(formInstance);
			}
		}
		return formInstancesForParty;
	}
	
	/**
	 * Returns the requirement transaction by reqUniqueID.
	 * @param nbaDst
	 * @param id
	 * @return
	 * @throws NbaNetServerDataNotFoundException
	 */
	//APSL404 New Method
	private NbaDst getRequirement(NbaDst nbaDst, String reqUniqueID) throws NbaNetServerDataNotFoundException {
		NbaTransaction nbaTransaction = null;
		if (nbaDst != null && reqUniqueID != null) {
			List allTrans = nbaDst.getNbaTransactions();
			int count = allTrans.size();
			for (int i = 0; i < count; i++) {
				nbaTransaction = (NbaTransaction) allTrans.get(i);
				if (nbaTransaction.getNbaLob() != null && reqUniqueID.equals(nbaTransaction.getNbaLob().getReqUniqueID())) {
					NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
					retrieveOptionsValueObject.setWorkItem(nbaTransaction.getID(), false);
					retrieveOptionsValueObject.requestSources();
					retrieveOptionsValueObject.setLockWorkItem();
					try {
						return retrieveWorkItem(getUser(), retrieveOptionsValueObject); //NBA213
					} catch (NbaBaseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}	
	
	/**
	 * Updates a Signed Illustration/Premium Quote requirement wokitem with all required LOBs. Sets the correct status from vp/ms model. It also updates
	 * requirement control source on the requirement workitem.
	 * @param nbaTransaction the new requirement workitem.
	 * @param vpmsRequirement the NbaVpmsRequirement object return by vp/ms for a requirement.
	 * @param reqUtils the instance of requirement utility object.
	 * @param workLOB the case LOBs.
	 * @param partyId the partyID of the insured
	 * @param provider
	 * @throws NbaBaseException
	 */
	//APSL404 New Method
	private void processMultiRequirement(NbaTransaction nbaTransaction, NbaVpmsRequirement vpmsRequirement, NbaRequirementUtils reqUtils,
			NbaLob workLOB, String partyId, NbaProcessWorkItemProvider provider, long reqCode) throws NbaBaseException {
		boolean addRequirement = true;
		List reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getParty(partyId), reqCode); //P2R2 Retrofit
		if (reqInfoList != null) {
			int count = reqInfoList.size();
			RequirementInfo reqInfo = null;
			for (int i = 0; i < count; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null && !NbaUtils.isRequirementFulfilled(String.valueOf(reqInfo.getReqStatus()))) {
					//BEGIN: APSL4271
					if(NbaUtils.isEmpty(reqInfo.getRequirementDetails())) {
						reqInfo.setRequirementDetails(vpmsRequirement.getComment());
					}
					//END: APSL4271
					NbaDst nbaDst = getRequirement(getWork(), reqInfo.getRequirementInfoUniqueID());
					NbaLob nbaLob = nbaDst.getNbaLob();
					NbaTableAccessor tableAccessor = new NbaTableAccessor();
					NbaRequirementsData requirementsData = (NbaRequirementsData) tableAccessor.getDataForOlifeValue(tableAccessor
							.setupTableMap(nbaLob), NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(reqInfo.getReqCode()));
					if (requirementsData == null) {
						throw new NbaDataAccessException("No data found for requirement " + reqInfo.getReqCode());
					}
					reqUtils.getOinkDataAccess().setLobSource(nbaLob);
					reqUtils.getOinkDataAccess().setContractSource(nbaTxLife);
					Map deOinkMap = new HashMap();
					NbaRequirementUtils.deOinkEndorsementValues(deOinkMap, nbaTxLife);
					NbaRequirementUtils.deOinkImpairmentValues(deOinkMap, nbaTxLife, partyId);
					NbaVpmsModelResult nbaVpmsModelReqOverrideResult = reqUtils.getDataFromVpmsModelRequirements(
							NbaVpmsAdaptor.EP_REQ_OVERRIDE_SETTINGS, deOinkMap, getUser());
					if (nbaVpmsModelReqOverrideResult.getVpmsModelResult() != null) {
						RequirementInfo overrideReqInfo = nbaVpmsModelReqOverrideResult.getVpmsModelResult().getRequirementInfoAt(0);
						if (overrideReqInfo.hasRestrictIssueCode()) {
							reqInfo.setRestrictIssueCode(overrideReqInfo.getRestrictIssueCode());
							nbaLob.setReqRestriction(overrideReqInfo.getRestrictIssueCode());
						} else if (! reqInfo.hasRestrictIssueCode())  { // APSL5127
							reqInfo.setRestrictIssueCode(requirementsData.getRestrictionCode());
							nbaLob.setReqRestriction(NbaUtils.convertStringToLong(requirementsData.getRestrictionCode()));
						}
					}
					reqInfo.setActionUpdate();
					nbaDst.setUpdate();
					nbaDst = WorkflowServiceHelper.update(getUser(), nbaDst);
					WorkflowServiceHelper.unlockWork(getUser(), nbaDst);
					addRequirement = false;
					break;
				}
			}
		}
		if (addRequirement) {
			nbaTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
			nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			setRouteReason(nbaTransaction, provider.getInitialStatus());
			nbaTransaction.getTransaction().setLock("Y"); 
			//set action flag so new transaction will be created with a lock
			nbaTransaction.getTransaction().setAction("L"); 
			processRequirement(nbaTransaction, vpmsRequirement, reqUtils, workLOB, partyId);					
		}
	}	
	//New Method added APSL1390/APSL1467
	public boolean isFormPresent(String formNumber) throws NbaBaseException{
		List sources = getWork().getNbaCase().getNbaSources();
		NbaSource nbaSource = null;
		for (int i = 0; i < sources.size(); i++) {
			nbaSource = (NbaSource) sources.get(i);
			NbaLob sourceLob = nbaSource.getNbaLob();
			if (!NbaUtils.isBlankOrNull(formNumber) && formNumber.equals(sourceLob.getFormNumber())) {
					return true;
			}
		}
		return false;
	}
	
	/** APSL1535 New Method
	 * Returns true if the contract has underwriter approval and false if not.
	 * 
	 * @return boolean
	 * @param nbaTXLife Holding Inquiry 
	 */
	public boolean hasUnderwriterApproval(NbaTXLife nbaTXLife) {
		try {
			ApplicationInfo appInfo = nbaTXLife.getPrimaryHolding().getPolicy().getApplicationInfo();
			int extCount = appInfo.getOLifEExtensionCount();
			for (int i = 0; i < extCount; i++){
				ApplicationInfoExtension appInfoExt = appInfo.getOLifEExtensionAt(0).getApplicationInfoExtension();
				if (appInfoExt != null) {
					if (appInfoExt.getUnderwritingApproval() == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER) {
						return true;
					} else {
						return false;
					}
				}
			}
			return false;
		} catch (Exception e) {
			getLogger().logError("Requirement Determination hasUnderwriterApproval: " + e.getMessage());
			return false;
		}
	}
	
	//APSL4787 New Method
	protected void createReplaceNotnWorkItem(int rqtp) throws NbaBaseException { //NBLXA-1554[NBLXA-2064]
		if (!isAnyOpenErrWI(A_WT_REPL_NOTIFICATION)) {
			Map deOink = new HashMap();	
			deOink.put("A_CreateReplNotnWI", "true");			
			NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTxLife, deOink);
			//BEGIN NBLXA-1554[NBLXA-2064]
			NbaTransaction notification = getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
			if (!NbaUtils.isBlankOrNull(notification)) {
				notification.getNbaLob().setReqType(rqtp);
			}
			//END NBLXA-1554[NBLXA-2064]
		}
	}
	
	//APSL4787 New Method
	protected boolean isAnyOpenErrWI(String workType) throws NbaBaseException {
		List transactions = getWork().getTransactions(); // get all the transactions
		Iterator transactionItr = transactions.iterator();
		WorkItem workItem;
		while (transactionItr.hasNext()) {
			workItem = (WorkItem) transactionItr.next();
			if (workType.equals(workItem.getWorkType()) && !END_QUEUE.equals(workItem.getQueueID())) {
				return true;
			}
		}
		return false;
	}
	
	// APSL4980 New Menthod Requirement Bundling
	protected void retrieveAndSetInitialReviewEndDateOfRequirements() {
		Date expectedDate_TimeAfter72Hrs;
		String expectedTimeAfter72Hrs;
		try {
			expectedDate_TimeAfter72Hrs = add72HrsToCurrentTime().getTime();
			expectedTimeAfter72Hrs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(expectedDate_TimeAfter72Hrs);
			ApplicationInfo applicationInfo = getNbaTxLife().getNbaHolding().getApplicationInfo();
			if (expectedDate_TimeAfter72Hrs != null && applicationInfo != null) {
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
				if (appInfoExt != null && NbaUtils.isBlankOrNull(appInfoExt.getInitialReviewEndDate())) {
					appInfoExt.setInitialReviewEndDate(expectedTimeAfter72Hrs);
					appInfoExt.setActionUpdate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// APSL5177 new Method Started -- AS per BRD EOLI req should generate while Org is not other with Buy/Sell,Deffer etc is
	// available on owner Tab on case.
	private boolean getEOLIreqInd() {
		boolean indForEOLI = false;
		// boolean intExpNeedVal = false;
		NbaParty owner = nbaTxLife.getPrimaryOwner();
		if (owner != null) {
			if (owner.isPerson()
					|| (owner.isOrganization() && owner.getOrganization().getOrgForm() == NbaOliConstants.OLI_ORG_PARTNER
							|| owner.getOrganization().getOrgForm() == NbaOliConstants.OLI_ORG_CORPORATION
							|| owner.getOrganization().getOrgForm() == NbaOliConstants.OLI_ORG_LTDLIAB || owner.getOrganization().getOrgForm() == NbaOliConstants.OLI_ORG_SOLEP)) { //NBLXA-2034
				List intents = nbaTxLife.getIntentForParty(owner.getID());
				if (intents != null) {
					for (int i = 0; i < intents.size(); i++) {
						Intent intent = (Intent) intents.get(i);
						if (intent != null && intent.getPartyID() != null && owner.getID() != null
								&& intent.getPartyID().equalsIgnoreCase(owner.getID())) {
							IntentExtension intentExt = NbaUtils.getIntentExtension(intent);
							if (intentExt != null && intentExt.getIntentCategory() == NbaOliConstants.OLI_INTENT_BUSINESS) {
								ExpenseNeedTypeCodeCC expenseNeedtypeCC = intentExt.getExpenseNeedTypeCodeCC();
								if (expenseNeedtypeCC != null) {
									ArrayList expesneNeedList = expenseNeedtypeCC.getExpenseNeedTypeCode();
									if (expesneNeedList != null && !expesneNeedList.isEmpty()) {
										for (int j = 0; j < expesneNeedList.size(); j++) {
											String expenseNeedType = expesneNeedList.get(j).toString();
											if (!NbaUtils.isBlankOrNull(expenseNeedType)) {
												long expenseNeedTypeCode = NbaUtils.convertStringToLong(expenseNeedType);
												if (expenseNeedTypeCode == NbaOliConstants.OLI_NEED_BUYSELL
														|| expenseNeedTypeCode == NbaOliConstants.OLI_NEED_KEYPERSON
														|| expenseNeedTypeCode == NbaOliConstants.OLI_NEED_DEFERREDCOMP) {
													indForEOLI = true;
													break;
												}
											}
										}
									}
								}
							}
						}
						if (indForEOLI) {
							break;
						}
					}

				}
			}
		}
		return indForEOLI;
	}
	//New Method NBLXA186-NBLXA1272
	protected void updateLastReqIndicatorForOtherParties() {
		if (nbaTxLife != null) {
			List relations = nbaTxLife.getOLifE().getRelation();
			Relation relation = null;
			String partyID = null;
			for (int i = 0; i < relations.size(); i++) {
				relation = (Relation) relations.get(i);
				partyID = relation.getRelatedObjectID();
				if (nbaTxLife.getParty(partyID) != null) {
					Party party = nbaTxLife.getParty(partyID).getParty();
					PartyExtension partyExt = NbaUtils.getFirstPartyExtension(party);
					if (partyExt != null && NbaOliConstants.OLI_LU_LASTREQSTAT_INCOMPLETE != partyExt.getLastRequirementIndForParty()
							&& NbaOliConstants.OLI_LU_LASTREQSTAT_RECEIVED != partyExt.getLastRequirementIndForParty()) { // //NBLXA1549-QC19308
						partyExt.setLastRequirementIndForParty(NbaOliConstants.OLI_LU_LASTREQSTAT_COMPLETE);
						partyExt.setActionUpdate();
					}
				}
			}
			NbaUtils.updateCompositeLRI(nbaTxLife, getWork());// NBLXA1549-QC19308
		}
	}
	
	//NBLXa-2072 New Method
	public static boolean isLNRCScoreApplicable(Date submissionDate) {
		if (null != submissionDate) {
			try {
				AxaRulesDataBaseAccessor dataBaseAccessor = AxaRulesDataBaseAccessor.getInstance();
				Date startDateLNRC = dataBaseAccessor.getConfigDateValue(NbaConstants.LNRC_START_DATE);
				Date endDateLNRC = dataBaseAccessor.getConfigDateValue(NbaConstants.LNRC_END_DATE);
				if (null != startDateLNRC && null != endDateLNRC) {
					if(submissionDate.compareTo(startDateLNRC) >= 0 &&  submissionDate.compareTo(endDateLNRC) <= 0 ){
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// NBLXA-2119-This method will suspend matching temp req for 15 mins
	protected void updateActivationDateForMatchingWI() throws NbaBaseException {
		lookupWorkForMatching(A_WT_TEMP_REQUIREMENT, AxaConstants.QUEUE_N2ORDER2);// NBLXA-2450[NBLXA-2328]
		lookupWorkForMatching(A_WT_MISC_MAIL, QUEUE_N2ORDER4);// NBLXA-2450[NBLXA-2328]
		retrieveMatchingWorkItems();
		NbaDst nbaDst = null;
		String mints = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SUSPEND_MATCHED_TEMPREQ);
		if (!NbaUtils.isBlankOrNull(mints)) {
			Date reqSusDate = AxaUtils.addMinutesToDate(new Date(), Integer.valueOf(mints));
			NbaSuspendVO suspendVO = null;
			if (retrivedWorkItemsList.size() > 0) {
				for (int i = 0; i < retrivedWorkItemsList.size(); i++) {
					nbaDst = (NbaDst) retrivedWorkItemsList.get(i);
					NbaUtils.addGeneralComment(nbaDst, getUser(), "Result unsuspended by policy " + nbaTxLife.getPolicy().getPolNumber());
					suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(nbaDst.getID());
					suspendVO.setActivationDate(reqSusDate);
					updateWork(getUser(), nbaDst);
					suspendWorkItem(suspendVO, nbaDst);
				}
			}
		}
	}

	// NBLXA-2119 new method- retrieving case matching criteria
	protected List getVpmsLookupCriteria() throws NbaBaseException {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWorkLobs());
		oinkData.setContractSource(getNbaTxLife());
		VpmsComputeResult computeResult = getDataFromVpms(NbaVpmsAdaptor.REQUIREMENTS, NbaVpmsAdaptor.EP_GET_CASE_MATCHING_SEARCH_CRITERIA, oinkData,
				null, null);
		NbaVpmsModelResult modelResult = new NbaVpmsModelResult(computeResult.getResult());
		List criteriaSets = new ArrayList();
		List lobList = null;
		if (modelResult.getVpmsModelResult() != null && modelResult.getVpmsModelResult().getResultDataCount() > 0) {
			int criteriaCount = modelResult.getVpmsModelResult().getResultDataCount();
			ResultData resultData = null;
			boolean polNumCriteria = false;
			for (int i = 0; i < criteriaCount; i++) {
				resultData = modelResult.getVpmsModelResult().getResultDataAt(i); // Next criteria
				polNumCriteria = false;
				int resultCount = resultData.getResultCount();
				if (resultCount > 0) {
					lobList = new ArrayList();
					for (int j = 0; j < resultCount; j++) {
						if (resultData.getResultAt(j).contains(AxaConstants.POLICYNUMBERLOB)) {
							polNumCriteria = true;
						}
						lobList.add(resultData.getResultAt(j));
					}
					if (!polNumCriteria) {
						criteriaSets.add(lobList);
					}
				}
			}
		}
		return criteriaSets;
	}

	// NBLXA-2119 new method
	protected NbaSearchVO lookupWorkForMatching(String workType, String queue) throws NbaBaseException { // NBLXA-2450[NBLXA-2328]
		List criteriaSets = getVpmsLookupCriteria();
		NbaSearchVO searchVO = null;
		int setCount = criteriaSets.size();
		if (setCount > 0) {
			searchVO = new NbaSearchVO();
			searchVO.setResultClassName(NbaSearchVO.TRANSACTION_SEARCH_RESULT_CLASS);
			searchVO.setWorkType(workType);
			searchVO.setQueue(queue);
			List searchLobs = null;
			for (int i = 0; i < setCount; i++) {
				searchLobs = (List) criteriaSets.get(i);
				if (searchLobs.size() > 0 && checkLobPresence(getWorkLobs(), searchLobs)) {
					searchVO.setNbaLob(getNbaLobForLookup((ArrayList) searchLobs));
					searchVO = lookupWork(getUser(), searchVO);
					if (searchVO != null && searchVO.getSearchResults() != null && !searchVO.getSearchResults().isEmpty()) {
						addMatchingWorkItems(searchVO.getSearchResults().listIterator());
					}
				}
			}
		}
		return searchVO;
	}

	// NBLXA-2119 new method
	protected void retrieveWorkItem(String transactionID) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Req det Starting retrieveTempReqWorkItem :" + transactionID);
		}
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(transactionID, false);
		retOpt.requestSources();
		retOpt.setLockTransaction();
		retOpt.setAutoSuspend();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		if (aWorkItem != null) {
			getRetrivedWorkItemsList().add(aWorkItem);
		}
	}

	// NBLXA-2119 new method
	protected void retrieveMatchingWorkItems() throws NbaBaseException {
		for (int i = 0; i < matchingWorkItemsList.size(); i++) {
			if (!NbaUtils.isBlankOrNull(matchingWorkItemsList.get(i))) {
				retrieveWorkItem((String) matchingWorkItemsList.get(i));
			}
		}
	}

	// NBLXA-2119 new method
	protected void addMatchingWorkItems(ListIterator results) {
		while (results.hasNext()) {
			NbaTransactionSearchResultVO resultVO = (NbaTransactionSearchResultVO) results.next();
			if (!matchingWorkItemsList.contains(resultVO.getTransactionID())) {
				matchingWorkItemsList.add(resultVO.getTransactionID());
			}
		}
	}

	// NBLXA-2119 new method
	protected void suspendWorkItem(NbaSuspendVO suspendVO, NbaDst nbaDst) throws NbaBaseException {
		suspendWork(getUser(), suspendVO);
		unlockWork(getUser(), nbaDst);
	}

	// NBLXA-2119 new method
	protected java.util.ArrayList getRetrivedWorkItemsList() {
		return retrivedWorkItemsList;
	}
	// NBLXA-2241 New Method - Checking Requirement out of sync on the case and also logger the time details on servers
	public void isReqOutofSynch() throws NbaBaseException {
		//getLogger().logError(new Timestamp(new Date().getTime())+" Start ReqOutofSynch method "+getNbaTxLife().getPolicy().getContractKey());
		int reqCount = 0; 
		ListIterator li = getWork().getNbaTransactions().listIterator();
		RequirementInfo reqInfo = null;
		while (li.hasNext()) {
			NbaTransaction trans = (NbaTransaction) li.next();
			if( A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
				reqInfo = getNbaTxLife().getRequirementInfo(trans.getNbaLob().getReqUniqueID());
				if (reqInfo == null) {
					getLogger().logError(new Timestamp(new Date().getTime())+" ERROR : ReqOutofSynch method - AWD Extra found "+trans.getNbaLob().getReqUniqueID());
				}
				reqCount++; //NBLXA-2241
			}
		}
		if(reqCount != getNbaTxLife().getPolicy().getRequirementInfoCount()) {
			getLogger().logError(new Timestamp(new Date().getTime())+" ERROR : ReqOutofSynch method - AWD and nbA requirements are not balanced");
		}
		//getLogger().logError(new Timestamp(new Date().getTime())+" END ReqOutofSynch method");
	}
	
	/**
	 * Unlock the parent work item
	 * 
	 * @throws NbaBaseException
	 */
	// APSL5055-NBA331.1 New Method
	protected void unlockParentWork() throws NbaBaseException {
		if (getParentCase() != null && getParentCase().getWorkItem().isLockedByMe()) {
			getParentCase().getWorkItem().setWorkItemChildren(new ArrayList()); // Unlock only the Parent Work Item
			unlockWork(getParentCase());
		}
	}	
}