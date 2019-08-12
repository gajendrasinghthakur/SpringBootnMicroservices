package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.model.NbaCreditCardPaymentRelease;
import com.csc.fsg.nba.business.transaction.NbaApproveContract;
import com.csc.fsg.nba.business.transaction.NbaMIBReportUtils;
import com.csc.fsg.nba.business.transaction.NbaReinsuranceUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.PolicyBenefitCodeCC;
import com.csc.fsg.nba.vo.txlife.PolicyRiderCodeCC;
import com.csc.fsg.nba.vo.txlife.ReinsuranceCalcInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaProcApproval is the class that processes nbAccelerator cases found
 * on the AWD approval queue (NBAPPRVL). It creates and sends an approval
 * transaction to the back-end system to approve the application.
 * <p>NbaProcApproval implements the Singleton pattern. The singleton is
 * accessed through the getInstance() method and the automated process
 * is initiated through the executeProcess method.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA012</td><td>Version 2</td><td>Contract Extract Print</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA030</td><td>Version 3</td><td>Rewrite Route view to HTML</td></tr>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1274</td><td>Version 3</td><td>The print process needs to be configurable in the configuration file</td></tr>
 * <tr><td>NBA051</td><td>Version 3</td><td>Allow Search on Work Items</td></tr>
 * <tr><td>NBA087</td><td>Version 3</td><td>Post Approval & Issue Requirements</td></tr>
 * <tr><td>NBA036</td><td>Version 3</td><td>nbA Underwriter Workbench Transaction to DB</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN023</td><td>Version 5</td><td>Automatic MIB Update</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit Card payment and authorization</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>A3_AXAL005</td><td>AXA Life New App A3</td><td>Amendment & Endorsement</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcApproval extends NbaAutomatedProcess implements NbaOliConstants {
	/**
	 * NbaProcApproval constructor comment.
	 */
	public NbaProcApproval() {
		super();
		//SPR1851 code deleted
	}
	/**
	 * Perform the Approval business process:
	 * - Retrieve the child work items and sources.
	 * - Transmit MIB reports
	 * - Create an approval transaction
	 * - Send the request to the adaptor for processing.
	 * - Check for transmission errors
	 * - Change status for the case
	 * - Update AWD
	 * - Updates nbProducer database
	 * @param user the user for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		// NBA095 code deleted
		if (!initialize(user, work)) {
			return getResult(); // NBA050
		}

		//NBA213 deleted code
			// SPR3290 code deleted
			//NBA087 code deleted
			//begin NBA012 
			//create and set retrieve option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), true);
			retOpt.setLockWorkItem();
			retOpt.requestSources();
			retOpt.requestTransactionAsChild(); //ACN023
			retOpt.setLockTransaction(); //ACN023
			NbaDst nbaDst = retrieveWorkItem(getUser(), retOpt); //ALII221
			setWork(nbaDst); //retrieve the complete Work Item	 NBA213
			
			//end NBA012
			//begin ACN023
			try {
				NbaMIBReportUtils mibUtils = new NbaMIBReportUtils(nbaTxLife, getUser());
				mibUtils.setNbaDstWithAllTransactions(nbaDst); //ALII221
				setWork(mibUtils.processMIBReportsForAContract(getWork(), true));
				setNbaTxLife(mibUtils.getNbaTxLife());
			} catch (Throwable t) {
				//If an error occurs when creating or updating the MIB report transaction, 
				//send case to error queue with comment “Error creating MIB update report”
				if (t.getMessage() != null) {
					addComment("Error creating MIB update report");
					addComment(t.getMessage());
				} else {
					addComment("Error creating MIB update report");
				}
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getHostErrorStatus()));
			}
			//end ACN023
			//begin NBA087
			if(getResult() == null){
				nbaTxLife.getPrimaryHolding().getPolicy().setApplicationInfo(createApplicationInfoObject()); // NBA050
				//end NBA087
				//ALS5273 reverted from here and moved to AutoUnderwriting
				//Begin NBA254
				ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPrimaryHolding().getPolicy()
						.getApplicationInfo());
				appInfoExtn.setClosureOverrideInd(FALSE);
				appInfoExtn.setActionUpdate();
				//End NBA254
				//Begin AXAL3.7.10B
				long appType = nbaTxLife.getPolicy().getApplicationInfo().getApplicationType();
				if(AxaUtils.isTermConversion(appType)   || AxaUtils.isOPAI(appType) ){
					NbaReinsuranceUtils.updateReinsuranceInfoForTAI(nbaTxLife,getJumboLimitInd()); //APSL3491
				}//End AXAL3.7.10B
				
				NbaApproveContract nac = new NbaApproveContract(getUser(), nbaTxLife, work, ""); //ALNA172, A3_AXAL005
	            nac.checkEndorsement(nbaTxLife);  //ALNA172, A3_AXAL005
	            
				handleHostResponse(doContractUpdate());  // NBA050
				
				// Begins ALII1041
				if (getResult() != null) { 
					addComment("Work Item suspended: Auto-approval failed because of transaction validation error present of case.");
					suspendVO = new NbaSuspendVO();
					suspendVO.setCaseID(getWork().getID());
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(new Date());
					calendar.add(Calendar.DAY_OF_WEEK , 1);
					Date activationDate = calendar.getTime(); 
					suspendVO.setActivationDate(activationDate);
					setSuspendVO(suspendVO);
					updateForSuspend();	
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
					return getResult(); //ALII1076
				}
				// Ends ALII1041
				
				if (getResult() == null) {
					NbaCreditCardPaymentRelease.releasePayments(getWork(), user);//NBA115
					addPrintExtractTransaction(user, getWork(), getBusfunc(), getNbaTxLife()); //Add Print Extract Work item //NBA100  
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
					// NBA100 code deleted 
				}
			}			
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			updateTAIForReinsuranceInfo();//AXAL3.7.10B
			//NBA020 code deleted
		//NBA213 deleted code
		return getResult();
	}

	//ALS5273 reverted from here and moved to AutoUnderwriting

	/**
	 * Calls the translation tables for UCT Tables
	 * @return tarray NbaTableData.
	 */
	//NBA012 new method
	protected NbaUctData[] getUctTable() {
		HashMap aCase = new HashMap();
		aCase.put("company", "*");
		aCase.put("tableName", NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		aCase.put("plan", "*");
		aCase.put("backendSystem", "*");
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		NbaTableAccessor ntsAccess = new NbaTableAccessor();
		NbaUctData[] tableData = null;
		try {
			tableData = (NbaUctData[]) ntsAccess.getDisplayData(aCase, NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		return (tableData);
	}
	
	
	/**
	 * Creates an application info object with information needed to approve the case.
	 * Updates the action indicator to ensure the back end system is updated properly.
	 * @return a newly created ApplicationInfo object containing necessary information
	 * @throws NbaBaseException
	 */
	// NBA050 NEW METHOD
	protected ApplicationInfo createApplicationInfoObject() throws NbaBaseException {
		// Add ApplicationInfo
		ApplicationInfo appInfo = null;
		if (nbaTxLife.getPrimaryHolding().getPolicy().hasApplicationInfo()) {
			appInfo = nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo();
		} else {
			appInfo = new ApplicationInfo();
		}
		// Add ApplicationInfoExtension
		OLifEExtension olifeExt = null;
		if ( appInfo.getOLifEExtensionCount() > 0 ) {
			olifeExt = appInfo.getOLifEExtensionAt(0);
		} else {
			olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_APPLICATIONINFO); //SPR2992
			appInfo.addOLifEExtension(olifeExt);
			olifeExt.getApplicationInfoExtension().setActionAdd(); //NBA036
		}
		ApplicationInfoExtension appInfoExt = olifeExt.getApplicationInfoExtension();
		appInfoExt.setUnderwritingApproval(OLIX_UNDAPPROVAL_UNDERWRITER);
		appInfoExt.setActionUpdate(); //NBA036
		if(!appInfo.hasHOCompletionDate()){ //ALII1036
			appInfo.setHOCompletionDate(new Date());	//ALII1036
		} //ALII1036
		
		appInfo.getActionIndicator().setUpdate();
		return appInfo;
	}
	
	//New Method AXAL3.7.10B
	private void updateTAIForReinsuranceInfo() throws NbaBaseException {
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_TAI_SERVICE_TRANSMIT, getUser(),
				getNbaTxLife(), null, new Long(NbaOliConstants.TC_SUBTYPE_GET_TAI_HOLDING_TRANSMIT));
		webServiceInvoker.execute();
	}
	//APSL3491 - New Method
	private boolean getJumboLimitInd() throws NbaBaseException {
		Map deoinkMap = null;
		VpmsComputeResult compResult = null;
		deoinkMap = deoinkCalcAttributes(nbaTxLife);
		compResult = NbaReinsuranceUtils.getDataFromVpms(nbaTxLife, NbaVpmsConstants.REINSURANCE, NbaVpmsAdaptor.EP_GET_JUMBOLIMITIND,
				deoinkMap, null);
		return NbaConstants.TRUE == Integer.parseInt(compResult.getResult());
	}
	
	//APSL3491 - New Method
	private Map deoinkCalcAttributes(NbaTXLife nbaTXLife) {
		Map deoinkMap = new HashMap();
		if (nbaTXLife != null) {
			ReinsuranceInfo reinsuranceInfo = nbaTXLife.getDefaultReinsuranceInfo();
			ReinsuranceInfoExtension reinsuranceInfoExt = NbaUtils.getFirstReinsuranceInfoExtension(reinsuranceInfo);
			if (reinsuranceInfoExt != null) {
				List reinCalcInfoList = reinsuranceInfoExt.getReinsuranceCalcInfo();
				for (int k = 0; k < reinCalcInfoList.size(); k++) {
					ReinsuranceCalcInfo reinCalcInfo = reinsuranceInfoExt.getReinsuranceCalcInfoAt(k);
					String str = "";
					String partyId = reinCalcInfo.getAppliesToPartyID();
					//str set according to the Insured, to be used while deoinking attributes
					if (nbaTXLife.isPrimaryInsured(partyId)) {
						str = "_PINS";
					} else if (nbaTXLife.isJointInsured(partyId)) {
						str = "_JNT";
					}
					if (!str.equalsIgnoreCase("")) {
						updateDeoinkMap(deoinkMap, reinCalcInfo, str, true);
					}
				}
			}
		}
		return deoinkMap;
	}
	
	//APSL3491 - New Method
	private void updateDeoinkMap(Map deoinkMap, ReinsuranceCalcInfo reinCalcInfo, String str, boolean isLevelOne) {
		if (reinCalcInfo != null) {
			if (isLevelOne) {
				deoinkMap.put("A_Residence" + str, String.valueOf(reinCalcInfo.getResidence()));
				deoinkMap.put("A_Hazard" + str, String.valueOf(reinCalcInfo.getHazard()));
				deoinkMap.put("A_ForeignTravel" + str, String.valueOf(reinCalcInfo.getForeignTravelInd() ? 1 : 0));
				deoinkMap.put("A_MilitaryActiveDutyIndCode" + str, String.valueOf(reinCalcInfo.getMilitaryInd() ? 1 : 0));
				deoinkMap.put("A_CalcUWCode" + str, String.valueOf(reinCalcInfo.getCalcUWCode()));
				deoinkMap.put("A_CalcProductType", String.valueOf(reinCalcInfo.getReinProductType()));
				deoinkMap.put("A_CalcShoppingInd" + str, String.valueOf(reinCalcInfo.getCalcShoppingInd() ? 1 : 0));
				deoinkMap.put("A_LivesCovered", String.valueOf(reinCalcInfo.getLivesCovered()));
				deoinkMap.put("A_IssueAge" + str, String.valueOf(reinCalcInfo.getIssueAge()));
				deoinkMap.put("A_CalcRateClass" + str, reinCalcInfo.getCalcUWClass());
				deoinkMap.put("A_CalcTableRating" + str, String.valueOf(reinCalcInfo.getCalcTableRating()));
				deoinkMap.put("A_PolicyProductCode", reinCalcInfo.getProductCode());
				deoinkMap.put("A_ReplAmount" + str, String.valueOf(reinCalcInfo.getReplAmount()));
				deoinkMap.put("A_FACEAMT", String.valueOf(reinCalcInfo.getFaceAmt()));
				deoinkMap.put("A_TotalMaxBenefitROPRAmt", String.valueOf(reinCalcInfo.getTotalMaxBenefitROPRAmt()));
			} else {
				deoinkMap.put("A_SRetAmount" + str, String.valueOf(reinCalcInfo.getSRetAmount()));
				deoinkMap.put("A_SCededAmt" + str, String.valueOf(reinCalcInfo.getSCededAmt()));
				deoinkMap.put("A_JRetAmount" + str, String.valueOf(reinCalcInfo.getJRetAmount()));
				deoinkMap.put("A_JCededAmt" + str, String.valueOf(reinCalcInfo.getJCededAmt()));
				deoinkMap.put("A_PriorReinsFaceAmt" + str, String.valueOf(reinCalcInfo.getAssumedFaceAmount()));
				deoinkMap.put("A_CalcTotalRiskAmt" + str, String.valueOf(reinCalcInfo.getTotInforce()));
				deoinkMap.put("A_TypeReplacement", String.valueOf(reinCalcInfo.getTypeReplacement()));
				deoinkMap.put("A_SingleLifeConcAmt" + str, String.valueOf(reinCalcInfo.getSingleLifeConcAmt()));
				deoinkMap.put("A_JointLifeConcAmt" + str, String.valueOf(reinCalcInfo.getJointLifeConcAmt()));
				deoinkMap.put("A_TotalInternalReplacementAmt" + str, String.valueOf(reinCalcInfo.getTotalInternalReplacementAmt()));
				deoinkMap.put("A_TotalExternalReplacementAmt" + str, String.valueOf(reinCalcInfo.getTotalExternalReplacementAmt()));
				PolicyRiderCodeCC policyRiderCodeCC = reinCalcInfo.getPolicyRiderCodeCC();
				if (policyRiderCodeCC != null && policyRiderCodeCC.getPolicyRiderCodeCount() > 0) {
					for (int i = 0; i < policyRiderCodeCC.getPolicyRiderCodeCount(); i++) {
						deoinkMap.put("A_CalcRiderProductCodeX[" + i + "]", policyRiderCodeCC.getPolicyRiderCodeAt(i));
					}
					deoinkMap.put("A_CalcRiderProductCodeXCount", String.valueOf(policyRiderCodeCC.getPolicyRiderCodeCount()));
				}
				PolicyBenefitCodeCC policyBenefitCodeCC = reinCalcInfo.getPolicyBenefitCodeCC();
				if (policyBenefitCodeCC != null && policyBenefitCodeCC.getPolicyBenefitCodeCount() > 0) {
					for (int i = 0; i < policyBenefitCodeCC.getPolicyBenefitCodeCount(); i++) {
						deoinkMap.put("A_CalcBenefitProductCodeX[" + i + "]", policyBenefitCodeCC.getPolicyBenefitCodeAt(i));
					}
					deoinkMap.put("A_CalcBenefitProductCodeXCount", String.valueOf(policyBenefitCodeCC.getPolicyBenefitCodeCount()));
				}
			}
		}
	}
}
