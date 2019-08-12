package com.csc.fsg.nba.business.transaction;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaAutoProcessProxy;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaAmendEndorseData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ArrDestination;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.EndorsementExtension;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ResultData;
/**
 * Produce and execute the back-end transaction to approve/deny the contract.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBA001</td>
 * <td>Version 1</td>
 * <td>Initial Development</td>
 * </tr>
 * <tr>
 * <td>NBA012</td>
 * <td>Version 2</td>
 * <td>Contract Extract Print</td>
 * </tr>
 * <tr>
 * <td>NBA013</td>
 * <td>Version 2</td>
 * <td>Use superclass service to submit request to host.</td>
 * </tr>
 * <tr>
 * <td>NBA022</td>
 * <td>Version 2</td>
 * <td>Case Manager view support</td>
 * </tr>
 * <tr>
 * <td>NBA020</td>
 * <td>Version 2</td>
 * <td>AWD Priority</td>
 * </tr>
 * <tr>
 * <td>NBA027</td>
 * <td>Version 3</td>
 * <td>Performance Tuning</td>
 * </tr>
 * <tr>
 * <td>NBA030</td>
 * <td>Version 3</td>
 * <td>Rewrite Route view to HTML</td>
 * </tr>
 * <tr>
 * <td>SPR1274</td>
 * <td>Version 3</td>
 * <td>The print process needs to be configurable in the configuration file</td>
 * </tr>
 * <tr>
 * <td>NBA050</td>
 * <td>Version 3</td>
 * <td>Peding Database Changes</td>
 * </tr>
 * <tr>
 * <td>NBA051</td>
 * <td>Version 3</td>
 * <td>Allow Search on Work Items</td>
 * </tr>
 * <tr>
 * <td>NBA093</td>
 * <td>Version 3</td>
 * <td>Upgrade to ACORD 2.8</td>
 * </tr>
 * <tr>
 * <td>NBA038</td>
 * <td>Version 3</td>
 * <td>Reinsurance Business Function</td>
 * </tr>
 * <tr>
 * <td>NBA094</td>
 * <td>Version 3</td>
 * <td>Transaction Validation</td>
 * </tr>
 * <tr>
 * <td>SPR1851</td>
 * <td>Version 4</td>
 * <td>Locking Issues</td>
 * </tr>
 * <tr>
 * <td>SPR1715</td>
 * <td>Version 4</td>
 * <td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td>
 * </tr>
 * <tr>
 * <td>NBA100</td>
 * <td>Version 4</td>
 * <td>Create Contract Print Extracts for new Business Documents</td>
 * </tr>
 * <tr>
 * <td>ACN012</td>
 * <td>Version 4</td>
 * <td>Architecture Changes</td>
 * </tr>
 * <tr>
 * <td>SPR2818</td>
 * <td>Version 5</td>
 * <td>The edit message that the case must be reinsured facultative prior to approval is displayed for negative disposition as well</td>
 * </tr>
 * <tr>
 * <td>SPR2673</td>
 * <td>Version 6</td>
 * <td>Routing Reason is not updated when underwriter or case manager approves or declines a case</td>
 * </tr>
 * <tr>
 * <td>NBA130</td>
 * <td>Version 6</td>
 * <td>Requirements Reinsurance Project</td>
 * </tr>
 * <tr>
 * <td>NBA213</td>
 * <td>Version 7</td>
 * <td>Unified User Interface</td>
 * </tr>
 * <tr>
 * <td>SPR3362</td>
 * <td>Version 7</td>
 * <td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td>
 * </tr>
 * <tr>
 * <td>NBA208-11</td>
 * <td>Version 7</td>
 * <td>Performance Tuning and Testing - Incremental change 11</td>
 * </tr>
 * <tr>
 * <td>NBA208-15</td>
 * <td>Version 7</td>
 * <td>Performance Tuning and Testing - Incremental change 15 - Avoid holding inquiry during Contract approve decline</td>
 * </tr>
 * <tr>
 * <td>SPR3290</td>
 * <td>Version 7</td>
 * <td>General source code clean up during version 7</td>
 * </tr>
 * <tr>
 * <td>NBA186</td>
 * <td>Version 8</td>
 * <td>nbA Underwriter Additional Approval and Referral Project</td>
 * </tr>
 * <tr><td>AXAL3.7.62</td><td>AXALife Phase 1</td><td>Amendments Endorsements</td></tr>
 * <tr><td>ALS2907</td><td>AXALife Phase 1</td><td>EndosementCode to EndorsementExtension.EndorsementCodeContent</td></tr>
 * <tr><td>P2AXAL035</td><td>AXA Life Phase 2</td><td>Amendment / Endorsement / Delivery Instructions</td></tr>
 * <tr><td>AXAL3.7.10B</td><td>AXA Life Phase 2</td><td>Reinsurance</td></tr>
 * <tr><td>CR60519</td><td>AXA Life Phase 2</td><td>IUL Refresh</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaApproveContract extends NbaEventToRequest {
	//  NBA208-15 code deleted
	protected String secondLvlDecisionQueue = null; //NBA186
	protected NbaOLifEId nbaOLifEId;  //AXAL3.7.62
	//Begin ALII1107
	protected int ROPR_ACCUMRATE_MSGCODE = 6723;
	protected int DEATHBENOPT_MSGCODE = 6739;
	protected int DEFLIFEINSTST_MSGCODE = 6740;
	protected int LTCBENPERC_MSGCODE = 6741;
	//End ALII1107
	protected Set invalidSourceRequirements = new HashSet(); //APSL3211/SR657984
	
	/**
	 * Create an NbaEventToRequest for a user.
	 * 
	 * @param newUser
	 *            an nbA user
	 * @param originalHolding
	 *            the holding inquiry as retrieved from a back-end system
	 * @param changedHolding
	 *            a holding inquiry with changes to base the request on
	 * @param newDst
	 *            the AWD case that the holding inquiry is for
	 * @param newStatusKey
	 *            the logical "user" key to the auto process status VP/MS model
	 */
	// NBA022 - new constructor
	//  NBA208-15 modified constructor - removed parameter.
	public NbaApproveContract(NbaUserVO newUser, NbaTXLife changedHolding, NbaDst newDst, String newStatusKey) {
		super(newUser);
		//NBA208-15 code deleted
		holdingInq = changedHolding;
		work = newDst; //NBA012 Changed from newCase to newDst
		statusKey = newStatusKey; // NBA022 - added
	}

	/**
	 * Create an NbaApproveContract for a user.
	 * 
	 * @param newUser
	 *            an nbA user
	 * @param changedHolding
	 *            a holding inquiry with changes to base the request on
	 * @param newDst
	 *            the AWD case that the holding inquiry is for
	 * @param newStatusKey
	 *            the logical "user" key to the auto process status VP/MS model
	 * @param newSecondLvlDecisionQueue
	 *            the second level underwriter queue
	 */
	//NBA186 added new constructor
	public NbaApproveContract(NbaUserVO newUser, NbaTXLife changedHolding, NbaDst newDst, String newStatusKey, String newSecondLvlDecisionQueue) {
		super(newUser);
		holdingInq = changedHolding;
		work = newDst;
		statusKey = newStatusKey;
		secondLvlDecisionQueue = newSecondLvlDecisionQueue;
	}

	/**
	 * Has a contract approval been requested? The answer is based upon the policy-level action indicator and the value of the Underwriting Approval
	 * field.
	 * 
	 * @return whether a contract approval has been requested.
	 */
	protected boolean approvalRequested() {
		Policy newPolicy = getHoldingInq().getPrimaryHolding().getPolicy();
		if (!newPolicy.isActionUpdate()) {
			// If the action indicator is wrong then no request has been made.
			return false;
		}
		Long oldApproval = getUnderwritingApproval(getHoldingInq(), true); //NBA208-15
		Long newApproval = getUnderwritingApproval(getHoldingInq(), false); //NBA208-15
		// If either is null then it couldn't be determined
		if (oldApproval == null || newApproval == null) {
			return false;
		}
		return newApproval.longValue() != oldApproval.longValue();
	}

	/**
	 * The method checks if a contract approval been tentatively requested. The answer is based upon the policy-level action indicator and the value
	 * of the tentative disposition field.
	 * 
	 * @return boolean whether a contract approval has been tentatively requested.
	 */
	//NBA186 New Method
	protected boolean approvalTentativelyRequested() {
		Policy newPolicy = getHoldingInq().getPrimaryHolding().getPolicy();
		if (!newPolicy.isActionUpdate()) {
			// If the action indicator is wrong then no request has been made.
			return false;
		}

		NbaTXLife aHolding = getHoldingInq();
		return getTentativeApproval(aHolding) == NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED;
	}

	/**
	 * Execute the event-to-request.
	 * 
	 * @return information about the success or failure of the event-to-request
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	public NbaEventToRequestResult executeRequest() throws NbaBaseException {
		String passStatus = null; //NBA012
		try {
			if (approvalRequested() || approvalTentativelyRequested()) {//NBA186
				// Begin NBA050
				NbaTXLife contract = getHoldingInq();
				//SPR1851 code deleted
				contract.setBusinessProcess(PROC_UW_APPROVE_CONTRACT);
				NbaDst nbaDstArg = getWork();
				try {
					NbaContractAccess.doContractUpdate(contract, nbaDstArg, user); //SPR1851, NBA213
					//SPR1851 code deleted
				} catch (NbaBaseException nbe) {
					//Begin NBA094
					if (nbe instanceof NbaTransactionValidationException) {
						throw nbe;
					}
					//End NBA094
					return new NbaEventToRequestResult(NbaEventToRequestResult.FAILED, "Couldn't create request.", null);
					//NBA213 deleted code
				}
				// End NBA050
				NbaUserVO tempUserVO = new NbaUserVO(statusKey, "");
				// NBA022 - use statusKey variable instead of literal
				//begin NBA012
				//Change the status and set the issue date
				NbaDst awdDst = getWork();
				//Begin NBA186
				HashMap deOink = new HashMap();
				//DeOink the second level Decision Queue
				deOink.put("A_SecondLvlDecisionQueue", secondLvlDecisionQueue);

				VpmsComputeResult data = getDataFromVpms(NbaVpmsAdaptor.EP_WORKITEM_STATUSES, NbaVpmsAdaptor.AUTO_PROCESS_STATUS, deOink);
				NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(data);
				//End NBA186
				passStatus = statusProvider.getPassStatus();
				//NBA186 code deleted
				awdDst.setStatus(passStatus);
				awdDst.increasePriority(statusProvider.getCaseAction(), statusProvider.getCasePriority());
				NbaUtils.setRouteReason(awdDst, passStatus); //SPR2673
				// NBA020
				if (getUnderwritingApproval(getHoldingInq(), false).longValue() == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER) { //NBA186
					NbaAutoProcessProxy vbaAutoProcessProxy = new NbaAutoProcessProxy(tempUserVO, awdDst, false); //NBA100 //NBA208-11
					vbaAutoProcessProxy.addPrintExtractTransaction(tempUserVO, awdDst, NbaConstants.PROC_APPROVAL, contract); //Add Print Extract
																															  // Work item //NBA100
				}//NBA186
				//end NBA012
			} else {
				return new NbaEventToRequestResult(NbaEventToRequestResult.NO_ATTEMPT, null, null);
			}
		} catch (NbaBaseException e) { //NBA213
			throw e; //NBA213
		} catch (Exception e) {
			throw new NbaBaseException(e);
		}
		return new NbaEventToRequestResult(NbaEventToRequestResult.SUCCESSFUL, null, passStatus);
		//NBA012
	}

	/**
	 * Extract the requested issue date from the Application Info Extension.
	 * 
	 * @param aHolding
	 *            the Acord model for a holding object
	 * @return the requested issue date
	 */
	//NBA012 new method
	protected Date getRequestedIssueDate(NbaTXLife aHolding) {
		// NBA093 deleted 10 lines
		// begin NBA093
		Holding holding = aHolding.getPrimaryHolding();
		if ((holding != null) && (holding.hasPolicy())) {
			ApplicationInfo appInfo = aHolding.getPrimaryHolding().getPolicy().getApplicationInfo();
			if (appInfo != null) {
				return appInfo.getRequestedPolDate();
			}
		}
		// end NBA093
		return null; // none could be found
	}

	/**
	 * Calls the translation tables for UCT Tables
	 * 
	 * @return array of UCT support table data.
	 */
	//NBA012 new method
	protected NbaUctData[] getUctTable() {
		HashMap aCase = new HashMap();

		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");

		aCase.put(NbaTableAccessConstants.C_TABLE_NAME, NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");
		aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, "*");

		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("Loading UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		} // NBA027

		NbaTableAccessor ntsAccess = new NbaTableAccessor();
		NbaUctData[] tableData = null;
		try {
			tableData = (NbaUctData[]) ntsAccess.getDisplayData(aCase, NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		}
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("Loaded UCT " + NbaTableConstants.NBA_CONTRACT_EXTRACT_COMPONENTS);
		} // NBA027
		return (tableData);
	}

	/**
	 * Extract the Underwriting Approval code from the Application Info Extension.
	 * 
	 * @param aHolding
	 *            the Acord model for a holding object
	 * @return the Underwriting Approval code
	 */
	//NBA208-15 changed method signature, added parameter originalUndApproval
	protected Long getUnderwritingApproval(NbaTXLife aHolding, boolean originalUndApproval) {
		ApplicationInfo appInfo = aHolding.getPrimaryHolding().getPolicy().getApplicationInfo();
		//begin NBA208-15
		ApplicationInfoExtension appInfoExtension = null;
		long approvalCode = -1L;
		if (originalUndApproval) {
			OLifEExtension olifeExt = appInfo.getOLifEExtensionAt(0);
			appInfoExtension = olifeExt.getApplicationInfoExtensionGhost();
		} else {
			appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		}
		if (appInfoExtension != null) {
			approvalCode = appInfoExtension.getUnderwritingApproval();
		}
		return new Long(approvalCode);
		//end NBA208-15
	}

	/**
	 * The method extracts the Underwriting tentative Approval disposition from the Application Info Extension.
	 * 
	 * @param aHolding
	 *            the Acord model for a holding object
	 * @return the Underwriting tentative Approval disposition
	 */
	//NBA186 New Method
	protected long getTentativeApproval(NbaTXLife aHolding) {
		ApplicationInfo appInfo = aHolding.getPrimaryHolding().getPolicy().getApplicationInfo();
		if (appInfo.getOLifEExtensionCount() > 0) {
			OLifEExtension extension = appInfo.getOLifEExtensionAt(0); // only 1 is expected
			if (extension != null) {
				if ((NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode())) && (extension.isApplicationInfoExtension())) {
					ApplicationInfoExtension appInfoExtension = null;
					appInfoExtension = extension.getApplicationInfoExtension();
					if (appInfoExtension != null && appInfoExtension.getTentativeDispCount() > 0) {
						if (NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED == appInfoExtension.getTentativeDispAt(0).getDisposition()) {
							return appInfoExtension.getTentativeDispAt(0).getDisposition();
						}
					}
				}
			}
		}
		return LONG_NULL_VALUE; // none could be found
	}

	/**
	 * If reinsurance LOB is not set invokes VPMS model. If VPMS model returns facultative reinsurace prompt user to enter reinsurance type. If VPMS
	 * model returns automatic or no reinsurace set the LOB and proceed.
	 * 
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @param nbaDst
	 *            com.csc.fsg.nba.vo.NbaDst
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//NBA038 New Method
	public void checkReinsurance(NbaTXLife nbaTxLife, NbaDst nbaDst) throws NbaBaseException {
		if (approvalRequested() || approvalTentativelyRequested()) { //SPR2818 NBA186
			NbaVpmsAdaptor vpmsProxy = null; //SPR3362
			try {
				long reinsuranceType = getReinsuranceType(nbaTxLife); // NBA130
				if (reinsuranceType == NbaOliConstants.OLI_UNKNOWN) { // NBA130
					if (nbaTxLife.isLife()) {
						LifeParticipant lifeParticipant = nbaTxLife.getPrimaryInuredLifeParticipant();
						HashMap deOink = new HashMap();
						if (lifeParticipant.getSubstandardRatingCount() > 0) {
							SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(0); // SPR3290
							if (substandardRating != null) {
								String permTableRating = String.valueOf(substandardRating.getPermTableRating());
								deOink.put("A_Rating", permTableRating);
							} else {
								deOink.put("A_Rating", "");
							}
						} else {
							deOink.put("A_Rating", "");
						}
						NbaOinkRequest oinkRequest = new NbaOinkRequest();
						NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
						oinkData.setContractSource(nbaTxLife);
						oinkData.setLobSource(getWork().getNbaLob());
						vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REINSURANCE); //SPR3362
						vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_REINSURANCE_TYPE);
						vpmsProxy.setSkipAttributesMap(deOink);
						vpmsProxy.setANbaOinkRequest(oinkRequest);
						NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
						if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
							if (nbaVpmsResultsData.getResultsData().size() >= 1) {
								reinsuranceType = Long.parseLong((String) nbaVpmsResultsData.getResultsData().get(0)); //NBA130
							}
						}
						if (reinsuranceType != NbaOliConstants.OLI_UNKNOWN) { // NBA130
							if (reinsuranceType == NbaOliConstants.OLI_REINRISKBASE_FA) { // NBA130
								throw new NbaBaseException("Case must be reinsured Facultative prior to approval");
							}
							//NBA130 CODE DELETED
						}
					}
				}
			} catch (NbaBaseException e) {
				throw e;
			} catch (Exception e) {
				throw new NbaBaseException(e);
				//begin SPR3362
			} finally {
				if (vpmsProxy != null) {
					try {
						vpmsProxy.remove();
					} catch (RemoteException e) {
						getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
					}
				}
				//end SPR3362
			}
		}
	}

	/**
	 * This method parses the Coverage objects looking for a ReinsurnaceInfo object where the ReinsuranceRiskBasis indicates Facultative. If not
	 * found, a value of UNKNOWN is returned.
	 * 
	 * @param nbaTxLife
	 *            object containing the Coverage objects
	 * @return a reinsuranceType for the Coverage, if found; else UNKNOWN
	 */
	// NBA130 NEW METHOD
	private long getReinsuranceType(NbaTXLife nbaTxLife) {
		long reinsuranceType = NbaOliConstants.OLI_UNKNOWN;
		Life life = nbaTxLife.getLife();
		if (life == null || life.getCoverageCount() == 0) {
			return reinsuranceType;
		}
		int covCount = life.getCoverageCount();
		// SPR3290 code deleted
		for (int i = 0; i < covCount; i++) {
			Coverage aCoverage = nbaTxLife.getLife().getCoverageAt(i);
			if (aCoverage.getReinsuranceInfoCount() > 0) {
				ReinsuranceInfo reinsInfo = aCoverage.getReinsuranceInfoAt(i);
				reinsuranceType = reinsInfo.getReinsuranceRiskBasis();
				if (reinsuranceType == NbaOliConstants.OLI_REINRISKBASE_FA) {
					return reinsuranceType;
				}
			}
		}
		return reinsuranceType;
	}

	/**
	 * Process Lifeparticipants, Coverages, and CovOptions and call VPMS model to add Endorsements/Amendments to contract. If VPMS model returns
	 * Endorsement objects.
	 * 
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//AXAL3.7.62 New Method
	public void checkEndorsement(NbaTXLife nbaTxLife) throws NbaBaseException {
		OLifE oLifE = nbaTxLife.getOLifE();
		for (int i = 0; i < oLifE.getPartyCount(); i++) {
			String partyId = oLifE.getPartyAt(i).getId();
			if (nbaTxLife.isInsured(partyId)) {
				computeInsuredEndorsementAmendmentObjects(nbaTxLife, partyId);
				computeCoverageEndorsementAmendmentObjects(nbaTxLife, partyId);
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("partyId: " + partyId + "  index " + i);
				}
			}
		}  // end for
	}

	/**
	 * Call the VPMS AXAEndorsements model for the Insured matching the partyID.
	 *  
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @param String partyId
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//AXAL3.7.62 New Method
	protected void computeInsuredEndorsementAmendmentObjects(NbaTXLife nbaTxLife, String partyId) throws NbaBaseException {
		HashMap deOinkMap = new HashMap();
		int LifeCoverageCount = nbaTxLife.getLife().getCoverageCount();
		Coverage coverage = null;

		for (int CovCnt = 0; CovCnt < LifeCoverageCount; CovCnt++) {
			coverage = nbaTxLife.getLife().getCoverageAt(CovCnt);
			int lifeParticipantCount = coverage.getLifeParticipantCount();
			for (int lifeCnt = 0; lifeCnt < lifeParticipantCount; lifeCnt++) {
				LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(lifeCnt);
				if (partyId.equals(lifeParticipant.getPartyID())) { //insured party id match
					computePersonEndorsements(nbaTxLife,deOinkMap, partyId, lifeParticipant);
					//CovCnt = lifeParticipantCount; Removed unwanted line
					break;
				} // insured party ids match
			} //end for LifeCnt
		}  //end for
	}	
	/**
	 * Call the VPMS AXAEndorsements model for the Coverages matching the partyID.
	 *  
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @param String partyId
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//AXAL3.7.62 New Method
	protected void computeCoverageEndorsementAmendmentObjects(NbaTXLife nbaTxLife, String partyId) throws NbaBaseException {
		HashMap deOinkMap = new HashMap();
		int LifeCoverageCount = nbaTxLife.getLife().getCoverageCount();
		int covOptionCount = 0;
		Coverage coverage = null;
		CovOption covOption = null;

		for (int CovCnt = 0; CovCnt < LifeCoverageCount; CovCnt++) {
			coverage = nbaTxLife.getLife().getCoverageAt(CovCnt);
			int lifeParticipantCount = coverage.getLifeParticipantCount();
			//Coverages or Riders need to know all the insureds that are on the coverage
			for (int lifeCnt = 0; lifeCnt < lifeParticipantCount; lifeCnt++) {
				LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(lifeCnt);
				deOinkPersonObjects(nbaTxLife,  deOinkMap, lifeParticipant, lifeCnt);

			} //end for LifeCnt
			deOinkMap.put("A_CoveragePartyCount", String.valueOf(lifeParticipantCount));
			computeCoverageEndorsements(nbaTxLife, deOinkMap, partyId, coverage);
			covOptionCount = coverage.getCovOptionCount();
			for (int covOptCnt = 0; covOptCnt < covOptionCount; covOptCnt++) {
				covOption = coverage.getCovOptionAt(covOptCnt);
				computeCovOptionEndorsements(nbaTxLife, deOinkMap, partyId, covOption);

			} // end for CovOptCnt

		} // end for CovCnt
	}
	/**
	 * This method deOinks VPMS party attributes.  VPMS needs to format 
	 * Amendment description value including multiple insureds attributes.
	 * EndorsementExtension,
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @param HashMap deOinkMap
	 * @param String LifeParticipant
	 * @param String attribute
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected void deOinkPersonObjects(NbaTXLife nbaTxLife, HashMap deOinkMap, LifeParticipant lifeParticipant, int counter) throws NbaBaseException {
		long participantStatus = -1;
		String partyId = lifeParticipant.getPartyID();
		LifeParticipantExtension lifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
		if (lifeParticipantExtension != null) {
			participantStatus = lifeParticipantExtension.getParticipantStatus();
		}
		deOinkArrayValue(deOinkMap, "A_ParticipantStatus", String.valueOf(participantStatus), counter);
		deOinkPartyAttribute(nbaTxLife, deOinkMap, partyId, "FullName", counter);
		deOinkPartyAttribute(nbaTxLife, deOinkMap, partyId, "BirthDate", counter);
		deOinkPartyAttribute(nbaTxLife, deOinkMap, partyId, "Age", counter);
	}
	/**
	 * Compute Person Endorsements
	 * @param NbaTXLife
	 * @param HashMap deOinkMap
	 * @param String partyID
	 * @param LifeParticipant
	 */
	//AXAL3.7.62 New Method
	protected void computePersonEndorsements(NbaTXLife nbaTxLife, HashMap deOinkMap, String partyId, LifeParticipant lifeParticipant) throws NbaBaseException {
		NbaVpmsModelResult nbaVpmsModelResult = null;
		deOinkCoverage(deOinkMap, null);
		deOinkCovOption(deOinkMap, null, nbaTxLife);//ALII982
		/*
		 * NBLXA-1515 -- Code Committed as hasMONYReplacement is used only for A094 Amendment which will create on 
		   Coverage with Primary Insured Only , Code replaced from computePersonEndorsements to computeCoverageEndorsements
		 */

	/*	//begin QC15465-APSL4316
		String hasMONYReplacement = null;
		ArrayList partyIdList = new ArrayList();
		int relationCnt = nbaTxLife.getOLifE().getRelationCount();
		List partyList = nbaTxLife.getOLifE().getParty();
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = nbaTxLife.getOLifE().getRelationAt(j);
			if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO && aRelation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING) {
				partyIdList.add(aRelation.getRelatedObjectID());
			}
		}
		for (int i = 0; i < partyIdList.size(); i++) {
			String pId = (String) partyIdList.get(i);
			Party aParty = NbaTXLife.getPartyFromId(pId, partyList);
			if (aParty.hasPartyKey()) {
				if(aParty.getPartyKey().equalsIgnoreCase(NbaConstants.AXA_COMPANY_MONY001))
				{
					hasMONYReplacement = "true";
				}
				break;
			}
		}
		deOinkMap.put("A_hasMONYReplacement", hasMONYReplacement);
		//End QC15465-APSL4316
*/
		deOinkMap.put("A_LifeParticipantID", lifeParticipant.getId());
		deOinkMap.put("A_LifeParticipantType", String.valueOf(NbaOliConstants.OLI_LIFEPARTICIPANT));//A2_AXAL004
		deOinkSubstandardRating(deOinkMap, lifeParticipant.getSubstandardRating());
		nbaVpmsModelResult = invokeVPMS(nbaTxLife, deOinkMap, partyId, NbaOliConstants.OLI_LIFEPARTICIPANT);
		addEndorsementAmendmentObjects(nbaTxLife, nbaVpmsModelResult, lifeParticipant.getId(),
				NbaOliConstants.OLI_LIFEPARTICIPANT, partyId);
	}
	
	/**
	 * Compute Coverage Endorsements
	 * @param NbaTXLife
	 * @param HashMap deOinkMap
	 * @param String partyID
	 * @param Coverage
	 */
	//AXAL3.7.62 New Method
	protected void computeCoverageEndorsements(NbaTXLife nbaTxLife, HashMap deOinkMap, String partyId, Coverage coverage) throws NbaBaseException {
		NbaVpmsModelResult nbaVpmsModelResult = null;
		deOinkCoverage(deOinkMap, coverage);
		deOinkCovOption(deOinkMap, null, nbaTxLife);//ALII982
		//Begin	P2AXAL035
		Policy policy = nbaTxLife.getPolicy();
		if(policy != null && (NbaOliConstants.OLI_PRODTYPE_INDXUL == policy.getProductType() ||
				NbaOliConstants.OLI_PRODTYPE_VUL == policy.getProductType()) ){
			deOinkFundAllocation(deOinkMap, nbaTxLife);
		}
		//Begin ALII1107
		Holding holding = nbaTxLife.getPrimaryHolding();
		SystemMessage sysMsg = null;
		String defLife = "true";
		String detbenOptType = "true";

		for (int i = 0; i < holding.getSystemMessageCount(); i++) {
			sysMsg = holding.getSystemMessageAt(i);
			SystemMessageExtension messageExt = null;
			if (sysMsg != null && NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE == sysMsg.getMessageSeverityCode()) {
				messageExt = NbaUtils.getFirstSystemMessageExtension(sysMsg);
				if (messageExt != null && !messageExt.getMsgOverrideInd()) {
					if (DEFLIFEINSTST_MSGCODE == sysMsg.getMessageCode()) {
						defLife = "false";
					} else if (DEATHBENOPT_MSGCODE == sysMsg.getMessageCode()) {
						detbenOptType = "false";
					}
				}
			}
		}
		deOinkMap.put("A_DefLifeInsMethod", defLife);
		deOinkMap.put("A_DeathBenefitOptType", detbenOptType);
		//End ALII1107
		getNbaOLifEId(nbaTxLife).assureId(nbaTxLife);
		//End P2AXAL035
		
		// NBLXA1515 -- Code Started
				String hasMONYReplacement = null;
				ArrayList partyIdList = new ArrayList();
				if(nbaTxLife.getOLifE() !=null){
					int relationCnt = nbaTxLife.getOLifE().getRelationCount();
					List partyList = nbaTxLife.getOLifE().getParty();
					if(partyList !=null && !partyList.isEmpty() && partyList.size()>0){
						for (int j = 0; j < relationCnt; j++) {
							Relation aRelation = nbaTxLife.getOLifE().getRelationAt(j);
							if (aRelation !=null && aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO && aRelation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING) {
								partyIdList.add(aRelation.getRelatedObjectID());
							}
						}
						for (int i = 0; i < partyIdList.size(); i++) {
							String pId = (String) partyIdList.get(i);
							Party aParty = NbaTXLife.getPartyFromId(pId, partyList);
							if (aParty !=null && aParty.hasPartyKey()) {
								if(aParty.getPartyKey().equalsIgnoreCase(NbaConstants.AXA_COMPANY_MONY001))
								{
									hasMONYReplacement = "true";
								}
								break;
							}
						}
						deOinkMap.put("A_hasMONYReplacement", hasMONYReplacement);
						
					}
					
				}
				// NBLXA1515 -- Code END		
		
		deOinkMap.put("A_CoverageID", coverage.getId());
		deOinkMap.put("A_CoverageType", String.valueOf(NbaOliConstants.OLI_LIFECOVERAGE));//A2_AXAL004
		nbaVpmsModelResult = invokeVPMS(nbaTxLife, deOinkMap, partyId, NbaOliConstants.OLI_LIFECOVERAGE);
		addEndorsementAmendmentObjects(nbaTxLife, nbaVpmsModelResult, coverage.getId(), NbaOliConstants.OLI_LIFECOVERAGE, partyId);
	}
	/**
	 * Compute CovOption Endorsements
	 * @param NbaTXLife
	 * @param HashMap deOinkMap
	 * @param String partyID
	 * @param CovOption
	 */
	//AXAL3.7.62 New Method
	protected void computeCovOptionEndorsements(NbaTXLife nbaTxLife, HashMap deOinkMap, String partyId, CovOption covOption) throws NbaBaseException {
		NbaVpmsModelResult nbaVpmsModelResult = null;
		deOinkCovOption(deOinkMap, covOption, nbaTxLife);//ALII982
		deOinkCovOptionSubstandardRating(deOinkMap, covOption.getSubstandardRating());
		deOinkMap.put("A_CovOptionID", covOption.getId());
		deOinkMap.put("A_CovOptionType", String.valueOf(NbaOliConstants.OLI_COVOPTION));//A2_AXAL004
		//Begin P2AXAL035
		//ALII982 code removed
		//Begin ALII1107
		Holding holding = nbaTxLife.getPrimaryHolding();
		SystemMessage sysMsg = null;
		String accumRate = "true";
		String optionPct = "true";

		for(int i=0; i < holding.getSystemMessageCount(); i++){
			sysMsg = holding.getSystemMessageAt(i);
			SystemMessageExtension messageExt = null;
			if(sysMsg != null && NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE == sysMsg.getMessageSeverityCode()){
				 messageExt = NbaUtils.getFirstSystemMessageExtension(sysMsg);
			     if (messageExt != null && !messageExt.getMsgOverrideInd()) {
					if(ROPR_ACCUMRATE_MSGCODE == sysMsg.getMessageCode() ){
						accumRate = "false";
					} else if(LTCBENPERC_MSGCODE == sysMsg.getMessageCode() ){
						optionPct = "false";
					}
			     }
			 }
		}
		deOinkMap.put("A_AccumulationRate", accumRate);
		deOinkMap.put("A_OptionPctLTC", optionPct);
		//End ALII1107
		//End P2AXAL035
		nbaVpmsModelResult = invokeVPMS(nbaTxLife, deOinkMap, partyId, NbaOliConstants.OLI_COVOPTION);
		addEndorsementAmendmentObjects(nbaTxLife, nbaVpmsModelResult, covOption.getId(), NbaOliConstants.OLI_COVOPTION, partyId);
	}	
	/**
	 * This method deOinks an VPMS party attribute
	 * EndorsementExtension,
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @param HashMap deOinkMap
	 * @param String partyId
	 * @param String attribute
	 * @param int counter
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected void deOinkPartyAttribute(NbaTXLife nbaTxLife, HashMap deOinkMap, String partyId, String attribute, int counter) throws NbaBaseException {
		String indexValue = null;
		if (counter == 0) {
			indexValue = "";
		}
		else {
			indexValue = "[" + counter + "]";
		}
		NbaOinkDataAccess dataAccess = new NbaOinkDataAccess(nbaTxLife);
		NbaOinkRequest oinkRequest = new NbaOinkRequest();	
		oinkRequest.setVariable(attribute);
		updatePartyFilterInRequest(oinkRequest, nbaTxLife, partyId);
		String[] responseCodeList = dataAccess.getStringValuesFor(oinkRequest);	
		if(responseCodeList.length > 0){//ALII262
			deOinkMap.put("A_" + attribute + indexValue, responseCodeList[0]);
		}//ALII262
	}
	
	/**
	 * This method adds the EndorsementAmendment objects to the contract, returned from VPMS. Returned objects may be Endorsement,
	 * EndorsementExtension,
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @param NbaVpmsModelResult
	 * @param String objectId
	 * @param long objectType
	 * @param String partyId
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//AXAL3.7.62 New Method
	protected  void addEndorsementAmendmentObjects(NbaTXLife nbaTxLife, NbaVpmsModelResult nbaVpmsModelResult, String objectId, long objectType,
		String partyId) throws NbaBaseException {
		ArrayList existingList = nbaTxLife.getPolicy().getEndorsement();
		ArrayList arrList = null;
		ArrayList arrResultData = null;
		Endorsement endorsement = null;
		ResultData resultData = null;
		EndorsementExtension endorsementExtension = null;
		OLifEExtension extension = null;
		boolean criticalEndorsementInd = false;
		NbaAmendEndorseData nbaAmendEndorseData = new NbaAmendEndorseData();

		if (nbaVpmsModelResult != null && nbaVpmsModelResult.getVpmsModelResult() != null) {
			 arrList = nbaVpmsModelResult.getVpmsModelResult().getEndorsement();
			 arrResultData = nbaVpmsModelResult.getVpmsModelResult().getResultData();
		}
		
		if (arrList != null) {
			for (int i = 0; i < arrList.size(); i++) {
				endorsement = (Endorsement) arrList.get(i);
				extension = endorsement.getOLifEExtensionAt(0);
				
				if (extension != null) {
					extension.setVendorCode(NbaOliConstants.CSC_VENDOR_CODE);  //for NbaUtil.getEndorsementExtension 
					endorsementExtension = extension.getEndorsementExtension();
					if (endorsementExtension != null) {
						
						if (endorsementMatch(nbaTxLife, endorsementExtension.getEndorsementCodeContent(), endorsement.getRelatedObjectID(), objectType, partyId) == false) {//P2AXAL044, APSL3982
							getNbaOLifEId(nbaTxLife).setId(endorsement);
							// valus may or may not be set from VPMS
							if (endorsement.hasRelatedObjectID() == false) {
								endorsement.setRelatedObjectID(objectId);
							}
							if (endorsement.hasRelatedObjectType() == false) {
								endorsement.setRelatedObjectType(objectType);
							}
							if (endorsement.hasAppliesToPartyID() == false) {
								endorsement.setAppliesToPartyID(partyId);
							}
							// set endorsementExtension values
							criticalEndorsementInd = nbaAmendEndorseData.getTableCriticalEndorsementInd(work, endorsementExtension.getEndorsementCodeContent());
							endorsementExtension.setCriticalEndorsementInd(criticalEndorsementInd);
							//APSL5173: Start
							endorsementExtension.setLastModifiedBy(AUTO_ORIGINATOR);
							endorsementExtension.setLastModifiedDate(new Date());
							//APSL5173: End
							endorsementExtension.setActionAdd();
							

							// add endorsement to contract
							endorsement.setActionAdd();
							resultData = (ResultData) arrResultData.get(i);
							addFormInstance(nbaTxLife, endorsement, resultData.getResultAt(0));
							existingList.add(endorsement);
						}  // end match not found
					}  // end EndorsementsExtension found
				}  // end extension found
			}  // end new endorsement loop
		}  // end if
	}  // end method

	/**
	 * Call the VPMS Reinsurance model for each Coverage and CovOption. For each invocation add the ReinsuranceInfo object if returned from VPMS.
	 * 
	 * @param nbaTXLife
	 *            com.csc.fsg.nba.vo.NbaTXLife
	 * @param nbaDst
	 *            com.csc.fsg.nba.vo.NbaDst
	 * @param deOink
	 *            HashMap
	 * @param String
	 *            partyID
	 * @param String
	 *            relatedObjectType
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//AXAL3.7.62 New Method
	protected NbaVpmsModelResult invokeVPMS(NbaTXLife nbaTxLife, HashMap deOink, String partyID, long relatedObjectType)
			throws NbaBaseException {
		NbaVpmsModelResult nbaVpmsModelResult = null;
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			List reqInfoList = nbaTxLife.getPolicy().getRequirementInfo();
			int count = reqInfoList.size();
			int j = 0;//SR657984
			if (count == 0) {
				deOink.put("A_ReqCodeList", "");
			} else {

				for (int i = 0; i < count; i++) {
					// Begin SR657984
					RequirementInfo reqInfo = (RequirementInfo) reqInfoList.get(i);
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if (!(reqInfoExt != null && reqInfoExt.getInvalidSourceInd())) {
						if (j == 0) {
							deOink.put("A_ReqCodeList", Long.toString(reqInfo.getReqCode()));
							deOink.put("A_ReqStatus", Long.toString(reqInfo.getReqStatus()));

						} else {
							deOink.put("A_ReqCodeList[" + j + "]", Long.toString(reqInfo.getReqCode()));
							deOink.put("A_ReqStatus[" + j + "]", Long.toString(reqInfo.getReqStatus()));
						}
						j++;
					}

				}
			}
			deOink.put("A_RequirementInfoCount", Long.toString(j));
			//end SR657984
			deOink.put("A_RELATEDOBJECTTYPE", String.valueOf(relatedObjectType));
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			updatePartyFilterInRequest(oinkRequest, nbaTxLife, partyID);
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			oinkData.setContractSource(nbaTxLife);
			oinkData.setLobSource(getWork().getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.AXAENDORSEMENTS);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_ENDORSEMENTS);
			vpmsProxy.setSkipAttributesMap(deOink);
			vpmsProxy.setANbaOinkRequest(oinkRequest);

			VpmsComputeResult aResult = vpmsProxy.getResults();
			if (aResult.getReturnCode() != 1) { // no results == 1
				nbaVpmsModelResult = new NbaVpmsModelResult(vpmsProxy.getResults().getResult());
			}
		} catch (NbaBaseException e) {
			throw e;
		} catch (Exception e) {
			throw new NbaBaseException(e);

		} finally {
			if (vpmsProxy != null) {
				try {
					vpmsProxy.remove();
				} catch (Exception e) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}

			}

		}

		return (nbaVpmsModelResult);
	}
	/**
	 * This method determines if the endorsement is already in the existing endorsement lists.
	 * 
	 * @param NbaTxLife
	 * @param String
	 * @return boolean
	 */
	// AXAL3.7.62 New Method
    protected boolean endorsementMatch(NbaTXLife nbaTxLife, String indexValue, String relatedObjectID, long objectType) {
        boolean found = false; // no match
        Endorsement endorsement = null;
        EndorsementExtension endorsementExtension = null;
        ArrayList existingList = nbaTxLife.getPolicy().getEndorsement();

        for (int i = 0; i < existingList.size(); i++) {
            endorsement = (Endorsement) existingList.get(i);
            endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
            if (endorsementExtension != null
                    && endorsementExtension.getEndorsementCodeContent() != null
                    && endorsementExtension.getEndorsementCodeContent().equals(indexValue)
                    && ((objectType == NbaOliConstants.OLI_COVOPTION || objectType == NbaOliConstants.OLI_LIFECOVERAGE) ? true : endorsement
                            .getRelatedObjectID().equals(relatedObjectID))) {// P2AXAL044, ALNA580 // APSL3904
                found = true;
                break;
            }
        } // end for
        return found;
    }
    
	// APSL3982::Start
	protected boolean endorsementMatch(NbaTXLife nbaTxLife, String indexValue, String relatedObjectID, long objectType, String appliesToPartyId) {
		boolean found = false; // no match
		Endorsement endorsement = null;
		EndorsementExtension endorsementExtension = null;
		ArrayList existingList = nbaTxLife.getPolicy().getEndorsement();

		for (int i = 0; i < existingList.size(); i++) {
            endorsement = (Endorsement) existingList.get(i);
            endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
            if (endorsementExtension != null
                    && endorsementExtension.getEndorsementCodeContent() != null
                    && endorsementExtension.getEndorsementCodeContent().equals(indexValue)
                    && ((objectType == NbaOliConstants.OLI_COVOPTION || objectType == NbaOliConstants.OLI_LIFECOVERAGE) ? true
                            : ((objectType == NbaOliConstants.OLI_LIFEPARTICIPANT ? endorsement.getAppliesToPartyID().equalsIgnoreCase(
                                    appliesToPartyId) : endorsement.getRelatedObjectID().equals(relatedObjectID))))) {// APSL3904
                found = true;
                break;
            }
        } // end for
		return found;
	}
	//APSL3982::END	

	/**
	 * Update the NbaOinkRequest with the relation role code and related reference ID from the primary relationship for the Party. These values are
	 * used in OINK processing to locate the values related to the Party.
	 * 
	 * @param nbaOinkRequest
	 *            the NbaOinkRequest
	 * @param aNbaTXLife
	 *            the NbaTXLife object to use for processing
	 * @param partyId -
	 *            the ID string which uniqualy identifies the Party
	 * @return a boolean which indicates where the values could be set correctly
	 */
	// AXAL3.7.62 New Method
	protected boolean updatePartyFilterInRequest(NbaOinkRequest nbaOinkRequest, NbaTXLife aNbaTXLife, String partyId) {
		Relation relation = NbaUtils.getRelationForParty(partyId, aNbaTXLife.getOLifE().getRelation().toArray());
		if (null != relation) {
			nbaOinkRequest.setPartyFilter(relation.getRelationRoleCode(), relation.getRelatedRefID());
			return true;
		}
		return false;
	}

	/**
	 * Deoink Coverage Fields
	 * @param HashMap
	 * @param Coverage
	 */
	// AXAL3.7.62 New Method
	protected void deOinkCoverage(HashMap deOinkMap, Coverage coverage) {
		if (coverage == null) {
			deOinkMap.put("A_ProductCode", "");
			deOinkMap.put("A_CurrentAmt", "");
			deOinkMap.put("A_LifeCovStatus", "");		}
		else {
			deOinkMap.put("A_ProductCode", coverage.getProductCode());
			deOinkMap.put("A_CurrentAmt", String.valueOf(coverage.getCurrentAmt()));
			deOinkMap.put("A_LifeCovStatus", String.valueOf(coverage.getLifeCovStatus()));
			}
	}
	
	/**
	 * Deoink CovOption Fields
	 * @param HashMap 
	 * @param CovOption
	 */
	// AXAL3.7.62 New Method , ALII982 signature modified
	protected void deOinkCovOption(HashMap deOinkMap, CovOption covOption, NbaTXLife nbaTxLife) {
		if (covOption == null) {
			deOinkMap.put("A_OptionAmt", "");
			deOinkMap.put("A_CovOptStatus", "");
			deOinkMap.put("A_LifeCovOptTypeCode", "");
		}
		else {
			deOinkMap.put("A_ProductCode", covOption.getProductCode());
			deOinkMap.put("A_OptionAmt", String.valueOf(covOption.getOptionAmt()));
			deOinkMap.put("A_CovOptStatus", String.valueOf(covOption.getCovOptionStatus()));
			deOinkMap.put("A_LifeCovOptTypeCode", String.valueOf(covOption.getLifeCovOptTypeCode()));
			//Begin P2AXAL035
			//Default ROPR Accumulation Percentage
			String accumRate= "true";//ALII982 //ALII1016
			if (!NbaUtils.isTermLife(nbaTxLife.getPolicy()) && covOption.hasLifeCovOptTypeCode() && covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_ROPR
					&& covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {//ALII982
				CovOptionExtension cOExt = NbaUtils.getFirstCovOptionExtension(covOption);
				if (cOExt != null) {
					if (!cOExt.hasAccumulationRate()) {
						cOExt.setAccumulationRate(0);
						cOExt.setActionUpdate();//ALII982
						accumRate = "false";//ALII982 //ALII1016
					}
				}
			}
			deOinkMap.put("A_AccumulationRate", accumRate);
			//End P2AXAL035
		}
	}
	/**
	 * Deoink SubstandardRating objects
	 * 
	 * @param HashMap
	 * @param ArrayList
	 */
	// AXAL3.7.62 New Method
	protected void deOinkSubstandardRating(HashMap deOinkMap, ArrayList arrList) {
		SubstandardRating substandardRating = null;
		SubstandardRatingExtension substandardRatingXtn = null;
		int substandardRatingCount = arrList.size();
		int deOinkCount=0;

		for (int i = 0; i < substandardRatingCount; i++) {
			substandardRating = (SubstandardRating) arrList.get(i);
			substandardRatingXtn = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if (substandardRatingXtn == null || substandardRatingXtn.getProposedInd() == false) {
				if (substandardRating.hasPermTableRating()) {
					deOinkArrayValue(deOinkMap, "A_PermFlatExtraRating", String.valueOf(substandardRating.getPermTableRating()), deOinkCount);
				} else {
					deOinkArrayValue(deOinkMap, "A_PermFlatExtraRating", "", deOinkCount);
				}
				if (substandardRatingXtn.hasPermFlatExtraAmt() && substandardRatingXtn != null) {
					deOinkArrayValue(deOinkMap, "A_PermFlatExtraAmt", String.valueOf(substandardRatingXtn.getPermFlatExtraAmt()), deOinkCount);
				} else {
					deOinkArrayValue(deOinkMap, "A_PermFlatExtraAmt", "", deOinkCount);
				}
				if (substandardRating.hasTempFlatExtraAmt()) {
					deOinkArrayValue(deOinkMap, "A_TempFlatExtraAmt", String.valueOf(substandardRating.getTempFlatExtraAmt()), deOinkCount);
				} else {
					deOinkArrayValue(deOinkMap, "A_TempFlatExtraAmt", "", deOinkCount);
				}				
				if (substandardRating.hasTempFlatExtraAmt() && substandardRatingXtn != null) {
					deOinkArrayValue(deOinkMap, "A_TempFlatExtraAmtDuration", String.valueOf(substandardRatingXtn.getDuration()), deOinkCount);
				} else {
					deOinkArrayValue(deOinkMap, "A_TempFlatExtraAmtDuration", "", deOinkCount);
				}
				deOinkCount++;
			}
		} // end for
		deOinkMap.put("A_SubstandardRatingCount", String.valueOf(deOinkCount));	
	}
	/**
	 * Deoink SubstandardRating objects
	 * 
	 * @param HashMap
	 * @param ArrayList
	 */
	// AXAL3.7.62 New Method
	protected void deOinkCovOptionSubstandardRating(HashMap deOinkMap, ArrayList arrList) {
		SubstandardRating substandardRating = null;
		SubstandardRatingExtension substandardRatingXtn = null;
		int substandardRatingCount = arrList.size();
		int deOinkCount=0;

		for (int i = 0; i < substandardRatingCount; i++) {
			substandardRating = (SubstandardRating) arrList.get(i);
			substandardRatingXtn = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if (substandardRatingXtn == null || substandardRatingXtn.getProposedInd() == false) {
				if (substandardRating.hasPermTableRating()) {
					deOinkArrayValue(deOinkMap, "A_CovOptionPermFlatExtraRating", String.valueOf(substandardRating.getPermTableRating()), deOinkCount);
				} else {
					deOinkArrayValue(deOinkMap, "A_CovOptionPermFlatExtraRating", "", deOinkCount);
				}
				deOinkCount++;
			}
		} // end for
		deOinkMap.put("A_CovOptionSubstandardRatingCount", String.valueOf(deOinkCount));	
	}
	
	/**
	 * Deoink VPMS Array attribute
	 * 
	 * @param HashMap deOinkMap
	 * @param String tag
	 * @param String value
	 * @param int counter
	 */
	// AXAL3.7.62 New Method
	protected void deOinkArrayValue(HashMap deOinkMap, String tag, String value, int count) {
		if (count == 0) {
			deOinkMap.put(tag, value);
		}
		else {
			deOinkMap.put(tag + "[" + count + "]", value);
		}
	}
	/**
	 * add Form Instance/Attachment Description message to Endorsement
	 * @param NbaTXLife
	 * @param Endorsement endorsement
	 * @param String message
	 */
	// AXAL3.7.62 New Method
	protected void addFormInstance(NbaTXLife nbaTxLife, Endorsement endorsement, String message) {
		FormInstance formInstance = null;
		Attachment attachment = null;

		if (message.length() > 0) {
			formInstance = new FormInstance();
			formInstance.setActionAdd();
			formInstance.setRelatedObjectID(endorsement.getId());
			attachment = new Attachment();
			attachment.setActionAdd();
			formInstance.addAttachment(attachment);
			
			if (message.length() > 1000){
				attachment.setDescription(message.substring(0,1000));
			} else {
				attachment.setDescription(message);
			}
			attachment.setActionUpdate();
			nbaTxLife.getOLifE().addFormInstance(formInstance);
		}
	}
	/**
	 * Retrieve an NbaOLifEId for the contract
	 * @param NbaTXLife
	 * @return
	 */
	// AXAL3.7.62 New Method
	protected  NbaOLifEId getNbaOLifEId(NbaTXLife nbaTXLife) {
		if (nbaOLifEId == null) {
			nbaOLifEId = new NbaOLifEId(nbaTXLife);
		}
		return nbaOLifEId;
	}

	/**
	 * @param deOinkMap
	 * @param holding
	 * Defaults and deOink the fund allocations
	 */
	//	P2AXAL035 New Method, ALII1202 fix
	protected void deOinkFundAllocation(HashMap deOinkMap, NbaTXLife txLife) {
		deOinkMap.put("A_DeductionAllocation", "true");
		deOinkMap.put("A_PremiumAllocation", "true");
		Holding holding = txLife.getPrimaryHolding();
		Arrangement cdArr = AxaUtils.getArrangementByType(holding, NbaOliConstants.OLI_ARRTYPE_CHARGEDEDUCTION);
		if (!NbaUtils.isENLGPresent(txLife) && AxaUtils.hasArrDestination(cdArr) && !AxaUtils.hasArrDestinationAlloc(cdArr)) {
			defaultAllocation(cdArr);
			deOinkMap.put("A_DeductionAllocation", "false");
		}
		//Default Premium Allocation if not present
		Arrangement saArr = AxaUtils.getArrangementByType(holding, NbaOliConstants.OLI_ARRTYPE_STANDINGALLOC);
		if (AxaUtils.hasArrDestination(saArr) && !AxaUtils.hasArrDestinationAlloc(saArr)) {
			defaultAllocation(saArr);
			deOinkMap.put("A_PremiumAllocation", "false");
		}
		//CR60519 Removed code
	}

	/**
	 * @param holding
	 * @param srcType
	 * @param destType Creates a new arrangement and defalts it with the given arrangment
	 */
	//P2AXAL035 New Method, ALII1202 refactored
	private void defaultAllocation(Arrangement arr) {
		if (arr != null) {
			int totalAlloc = 0;
			List arrList = NbaUtils.getNonDeletedList(arr.getArrDestination());
			for (int i = 0; i < arrList.size(); i++) {
				ArrDestination arrDest = (ArrDestination) arrList.get(i);
				arrDest.setTransferAmtType(NbaOliConstants.OLI_TRANSAMTTYPE_PCT);
				arrDest.setActionUpdate();
				if (i == arrList.size() - 1) {
					arrDest.setTransferPct(100 - totalAlloc);
				} else {
					arrDest.setTransferPct(Math.floor(100 / arrList.size()));
					totalAlloc += arrDest.getTransferPct();
				}
			}
		}
	}
	
	//Code Deleted AXAL3.7.10B
}
