package com.csc.fsg.nba.business.process;

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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.bean.accessors.NbaContractPrintFacadeBean;
import com.csc.fsg.nba.bean.accessors.NbaUnderwriterWorkbenchFacadeBean;
import com.csc.fsg.nba.business.transaction.NbaAgentAdvancesRequestor;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaMagnumUtils;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.vo.AutoClosureCriteria;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaProcAppSubmit processes Cases found in NBAPPSUB queue.
 * 
 * A holding inquiry is peformed to determine the current values of the contract. A message is sent to the back end system to set the underwriting
 * status of the case unless the final disposition is "issued" or there is CWA present on the case.
 *
 * The work item status is set based on the answer from VP/MS. The status is based on the value of the final disposition and the CWA total. Cases to
 * be issued are routed to the application hold queue. Cases with CWA are routed to a different queue to allow the CWA to be refunded. Cases which are
 * not being issued and which do not have CWA are routed to different queues based on the final disposition type.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th> </thead>
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
 * <td>NBA020</td>
 * <td>Version 2</td>
 * <td>AWD Priority</td>
 * </tr>
 * <tr>
 * <td>NBA050</td>
 * <td>Version 3</td>
 * <td>Pending Database</td>
 * </tr>
 * <tr>
 * <td>NBA035</td>
 * <td>Version 3</td>
 * <td>App submit to nbA Pending DB</td>
 * </tr>
 * <tr>
 * <td>NBA044</td>
 * <td>Version 3</td>
 * <td>Architecture changes</td>
 * </tr>
 * <tr>
 * <td>NBA093</td>
 * <td>Version 3</td>
 * <td>Upgrade to ACORD 2.8</td>
 * </tr>
 * <tr>
 * <td>SPR1851</td>
 * <td>Version 4</td>
 * <td>Locking Issues</td>
 * </tr>
 * <tr>
 * <td>NBA077</td>
 * <td>Version 4</td>
 * <td>Reissues and Complex Change etc.</td>
 * </tr>
 * <tr>
 * <td>NBA122</td>
 * <td>Version 5</td>
 * <td>Underwriter Workbench Rewrite</td>
 * </tr>
 * <tr>
 * <td>SPR2992</td>
 * <td>Version 6</td>
 * <td>General Code Clean Up Issues for Version 6</td>
 * </tr>
 * <tr>
 * <td>NBA137</td>
 * <td>Version 6</td>
 * <td>nbA Agent Advances</td>
 * </tr>
 * <tr>
 * <td>NBA254</td>
 * <td>Version 8</td>
 * <td>Automatic Closure and Refund of CWA</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.21</td>
 * <td>AXA Life Phase 1</td>
 * <td>Prior Insurance</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.22</td>
 * <td>AXA Life Phase 1</td>
 * <td>Compenasation Interface</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.13</td>
 * <td>AXA Life Phase 1</td>
 * <td>Formal Correspondence</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.26</td>
 * <td>AXA Life Phase 1</td>
 * <td>OLSA Interface</td>
 * </tr>
 * <tr>
 * <td>ALS4834</td>
 * <td>AXA Life Phase 1</td>
 * <td>QC # 3990 - Override Auto closure did not change status to Offer Expired</td>
 * </tr>
 * <tr>
 * <td>ALS5272</td>
 * <td>AXA Life Phase 1</td>
 * <td>QC # 4442 - Received AWD error on case that was should have been an NTO</td>
 * </tr>
 * <tr>
 * <td>ALS5344</td>
 * <td>AXA Life Phase 1</td>
 * <td>QC #4526 - MIB reported codes not being sent on final dispositionfor informals</td>
 * </tr>
 * <tr>
 * <td>APSL222</td>
 * <td>AXA Life Phase 1</td>
 * <td>QC #5088 - Multiple refund checks were generated at Negative Disposition, but only one payment was applied to case</td>
 * </tr>
 * <tr>
 * <td>CR59174</td>
 * <td>XA Life Phase 2</td>
 * <td>1035 Exchange Case Manager</td>
 * </tr>
 * <tr>
 * <td>SR534322</td>
 * <td>Discretionary</td>
 * <td>Reopen from NTO</td>
 * </tr>
 * <tr>
 * <td>CR1664600(APSL3116)</td>
 * <td>Discretionary</td>
 * <td>Carryover Loans Compared with 1035 Exchange Amount</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcFinalDisposition extends NbaAutomatedProcess implements NbaOliConstants {
	// ALS5041 moved to NbaConstants
	protected ArrayList nonEligibleWorkTypeList = new ArrayList();// SR534322 Retrofit- Performance Related Changes

	protected static int DAY = 10;// ALII2043, APSL3836

	protected static int refundDays = 14;// ALII2043
	
	protected static int UWDAY = 5;//NBLXA-2340

	/**
	 * NbaProcFinalDisposition constructor comment.
	 */
	public NbaProcFinalDisposition() {
		super();
		// SPR1851 code deleted
	}

	/**
	 * Creates an application info object with information needed to approve the case. Updates the action indicator to ensure the back end system is
	 * updated properly.
	 * 
	 * @return a newly created ApplicationInfo object containing necessary information
	 * @throws NbaBaseException
	 */
	// NBA050 NEW METHOD
	protected ApplicationInfo createApplicationInfoObject() throws NbaBaseException {
		ApplicationInfo appInfo = null;
		if (nbaTxLife.getPrimaryHolding().getPolicy().hasApplicationInfo()) {
			appInfo = nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo();
		} else {
			appInfo = new ApplicationInfo();
		}
		// Add ApplicationInfoExtension
		OLifEExtension olifeExt = null;
		if (appInfo.getOLifEExtensionCount() > 0) {
			olifeExt = appInfo.getOLifEExtensionAt(0);
		} else {
			olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_APPLICATIONINFO); // SPR2992
			appInfo.addOLifEExtension(olifeExt); // NBA122
			olifeExt.getApplicationInfoExtension().setActionAdd(); // NBA122
		}
		ApplicationInfoExtension appInfoExt = olifeExt.getApplicationInfoExtension();
		// Begin APSL3184
		if (getWork().getNbaLob().getIssueDate() != null) {
			appInfo.setRequestedPolDate(getWork().getNbaLob().getIssueDate()); // NBA093
		}
		// End APSL3184
		if (getWork().getNbaLob().getCaseFinalDispstn() > 0) { // ALNA659
			appInfoExt.setUnderwritingStatus(getWork().getNbaLob().getCaseFinalDispstn());
		}
		// NBA007 begins
		if (getWork().getNbaLob().getFinalDispReason() > 0) { // ALNA659
			appInfoExt.setUnderwritingStatusReason(getWork().getNbaLob().getFinalDispReason());
		}
		// NBA007 ends
		appInfo.getActionIndicator().setUpdate();
		appInfoExt.setActionUpdate(); // NBA122
		// NBA122 code deleted
		return appInfo;
	}

	/**
	 * Perform an holding inquiry and update the LOB fields with the result. If the status of the case is not "issued" and there is no CWA to refund,
	 * create a transaction to change the status on the back end system.Performs the Agent Advance Chargeback transaction
	 * 
	 * @throws NbaBaseException
	 */
	protected void doProcess() throws NbaBaseException {
		NbaDst parentCase = retrieveCaseAndTransactions(getWork(), getUser(), true, true); // ALS4808
		setWork(parentCase); // ALS4808

		// ALS5272 code deleted Config file says we lock the contract so no need to do a 2nd holding inquiry
		updateLobFromNbaTxLife(nbaTxLife);// ALS5272
		getStatusProvider().initializeStatusFields(getUser(), getWork(), nbaTxLife); // NBA035 call different initializeStatusFields method & NBA050
		NbaLob lob = getWork().getNbaLob(); // NBA077
		boolean txLifeUpdated = false; // NBA077
		if (lob.getCwaTotal() <= 0) { // NBA077
			nbaTxLife.getPrimaryHolding().getPolicy().setApplicationInfo(createApplicationInfoObject()); // NBA050 //NBA044
			txLifeUpdated = true; // NBA077
			// NBA077 line deleted
		}

		// begin NBA077
		boolean isStandAlone = NbaServerUtility.isDataStoreDB(lob, user);
		if (isStandAlone) {
			// Begin NBA254
			if (AUTOCLOSE.equals(getWork().getStatus())) {// Automatically Negative disposed case
				updateUnderwtitingStatus(getNbaTxLife()); // ALS4834
				applyUWApproveAndDisposition(); // APSL1150 Code Refactored
			}
			updateClosureIndicator(nbaTxLife);
			deleteReg60Records(nbaTxLife);// ALS5090
			if (lob.getCwaTotal() > 0) {
				refundCWAAmountforCase();// Method name change APSL222
				txLifeUpdated = true; // AXAL3.7.13
			}
			// End NBA254

			// begin AXAL3.7.06
			ApplicationInfo applicationInfo = getNbaTxLife().getPolicy().getApplicationInfo();
			ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (applicationInfoExtension != null) {
				if ((NbaUtils.isNegativeDisposition(applicationInfoExtension.getUnderwritingStatus())
						|| applicationInfoExtension.getUnderwritingStatus() == NbaOliConstants.NBA_FINALDISPOSITION_REG60NIGO
						|| applicationInfoExtension.getUnderwritingStatus() == NbaOliConstants.NBA_FINALDISPOSITION_REG60_PRESALE_EXPIRED)
						&& (NbaUtils.isStatusPending(getNbaTxLife().getPolicy().getPolicyStatus()) || nbaTxLife.isInformalApp())) {// ALS5041
																																	// //QC15660/APSL4350//APSL5125
					getNbaTxLife().getPolicy().setPolicyStatus(applicationInfoExtension.getUnderwritingStatus());
					getNbaTxLife().getPolicy().setActionUpdate(); // APSL3124
					if (!applicationInfo.hasHOCompletionDate()) {// ALS5512
						applicationInfo.setHOCompletionDate(new Date()); // ALS3251
					} // ALS5512
						// SC: NBLXA186-NBLXA-1271
					/* if (applicationInfoExtension.hasLastRequirementInd()) { */
					applicationInfoExtension.setLastRequirementInd(NbaOliConstants.OLI_LU_LASTREQSTAT_COMPLETE);
					/* } */
					// EC: NBLXA186-NBLXA-1271
					applicationInfo.setActionUpdate(); // ALS3251
					txLifeUpdated = true;
					applyUWApproveAndDisposition();// APSL1150
				}
			}
			// end AXAL3.7.06
			if ((lob.getContractChgType() != null) && (Long.parseLong(lob.getContractChgType()) == NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT)) {
				FinancialActivity suspenseFinActivity = getSuspenseMoney();
				if (suspenseFinActivity != null) {
					NbaOLifEId olifeId = new NbaOLifEId(getNbaTxLife());
					processReinstatementPayment(suspenseFinActivity, olifeId);
					txLifeUpdated = true;
				}
			}
			// SC: NBLXA186/NBLXA-1271
			int partyCount = getNbaTxLife().getOLifE().getPartyCount();
			for (int k = 0; k < partyCount; k++) {
				Party party = getNbaTxLife().getOLifE().getPartyAt(k);
				if (party != null) {
					PartyExtension partyExt = NbaUtils.getFirstPartyExtension(party);
					if (partyExt != null) {
						partyExt.setLastRequirementIndForParty(NbaOliConstants.OLI_LU_LASTREQSTAT_COMPLETE);
						partyExt.setActionUpdate();
					}
					txLifeUpdated = true;
				}
			}
			if (getWork().getNbaLob().getSigReqRecd()) {
				getWork().getNbaLob().setSigReqRecd(false);
				getWork().getNbaLob().setLstNonRevReqRec(false);
				NbaUtils.addAutomatedComment(getWork(), user, "Last requirement indicator has been turned off due to Final action."); // NBLXA-1718
																																		// changed
																																		// from
																																		// general to
																																		// automated
			} else {
				getWork().getNbaLob().setSigReqRecd(false);
				getWork().getNbaLob().setLstNonRevReqRec(false);
			}
			// EC: NBLXA186/NBLXA-1271

		}

		// begin NBA137
		if (isStandAlone && getWork().isCase() && NbaConfiguration.getInstance().isAgentAdvanceSupported()) {
			NbaAgentAdvancesRequestor agentAdvancesRequestor = new NbaAgentAdvancesRequestor();
			agentAdvancesRequestor.processAgentAdvances(getNbaTxLife(), getWork().getNbaLob(),
					NbaConfigurationConstants.WEBSERVICE_FUNCTION_AGT_ADV_CHGBK);
			txLifeUpdated = true;

			// Check for any attachment with user code 3. If exists it implies that there has been webservice failure error for agent
			// advances chargeback, so move the workitem to the error queue.
			if (isAgtAdvChgbkRequestFailed(nbaTxLife)) {
				addComment(agentAdvancesRequestor.getErrorMessage());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getHostErrorStatus()));
			}
		}
		// end NBA137

		txLifeUpdated = generateCorrospondance();

		if (txLifeUpdated) {
			handleHostResponse(doContractUpdate());
		}
		// end NBA077

	}

	// APSL2735-EIP New Method
	private boolean isACHPaymentAndNotNegativeDisp() throws NbaBaseException {
		boolean isACHPaymentForm = false;
		ApplicationInfo applicationInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if (applicationInfoExtension.hasInitialPremiumPaymentForm()) {
			if ((applicationInfoExtension.getInitialPremiumPaymentForm() == NbaConstants.PAYMENT_TYPE_ACH)
					&& !NbaUtils.isNegativeDisposition(applicationInfoExtension.getUnderwritingStatus())) {
				if (NbaConstants.PAYMENT_NOTIF_REVIEW_REQUIRED_NOTDONE == getOrigWorkItem().getNbaLob().getPaymentReview()) {
					isACHPaymentForm = true;
				}
			}
		}

		return isACHPaymentForm;
	}

	// APSL2735-EIP New Method
	/**
	 * Verify if an open PrintNotification wokritem exists on the case. Calls VP/MS model to determine if the work item is in the end queue or not.
	 * 
	 * @return true if an open PrintNotification wokritem exists on the case else return false.
	 * @throws NbaBaseException
	 */
	// ALNA204 refactored method
	protected boolean isAnyOpenPaymentNotification(NbaDst parentCase) throws NbaBaseException {
		List transactions = parentCase.getNbaTransactions();
		int count = transactions.size();
		NbaTransaction anyTransaction = null;
		NbaTransaction endedTransaction = null;
		boolean hasOpenSimilarPayNotif = false;
		for (int i = 0; i < count; i++) {
			anyTransaction = (NbaTransaction) transactions.get(i);
			if (NbaConstants.A_WT_PAYMENT_NOTIFICATION.equalsIgnoreCase(anyTransaction.getWorkType())) {
				hasOpenSimilarPayNotif = true;
				endedTransaction = anyTransaction;
				break;
			}
		}
		if (hasOpenSimilarPayNotif) {
			updateExistingPaymentNotification(endedTransaction);
		}
		return hasOpenSimilarPayNotif;
	}

	/**
	 * Verify if an open PrintNotification wokritem exists on the case. Update the WI to send back to OtherStatus returned from VPMS
	 * 
	 * @return true if an open aggregate contract wokritem exists on the case else return false.
	 * @throws NbaBaseException
	 */
	// APSL2735-EIP New Method
	protected void updateExistingPaymentNotification(NbaTransaction endedTransaction) throws NbaBaseException {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(endedTransaction.getNbaLob());
		oinkData.setContractSource(getNbaTxLife());
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork());
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(endedTransaction.getID(), false);
		retOpt.setLockWorkItem();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		aWorkItem.setStatus(provider.getInitialStatus());
		aWorkItem.getNbaLob().setRouteReason(NbaConstants.NBA_ROUTE_REASON_PYRVNEED);
		aWorkItem.setUpdate();
		update(aWorkItem);
		unlockWork(aWorkItem);
	}

	/**
	 * Creates new PrintNotification work item. Call VP/MS model to get work type, initial status, work priority and priority action.
	 * 
	 * @throws NbaBaseException
	 */
	protected void createNewPaymentNotification() throws NbaBaseException {
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork());
		NbaTransaction nbaTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
		nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		NbaLob transactionLOBs = nbaTransaction.getNbaLob();
		NbaLob parentLob = getWork().getNbaLob();

		transactionLOBs.setPolicyNumber(parentLob.getPolicyNumber());
		transactionLOBs.setLastName(parentLob.getLastName());
		transactionLOBs.setFirstName(parentLob.getFirstName());
		transactionLOBs.setQueueEntryDate(new Date());
		transactionLOBs.setWritingAgency(NbaUtils.getWritingAgencyId(getNbaTxLife()));
		transactionLOBs.setAppProdType(parentLob.getAppProdType());
		transactionLOBs.setReplCMQueue(parentLob.getReplCMQueue());
		transactionLOBs.setDistChannel(String.valueOf(parentLob.getDistChannel()));
		transactionLOBs.setRouteReason(NbaConstants.NBA_ROUTE_REASON_PYRVNEED);
		getWork().setUpdate();

	}

	// APSL2735-EIP New Method
	protected NbaSuspendVO getSuspendWorkVO(int type, int value) {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(new Date());
		cal.add(type, value);
		suspendVO.setCaseID(getOrigWorkItem().getID());
		suspendVO.setActivationDate(cal.getTime());
		return suspendVO;
	}

	// NBA254 New Method
	private void refundCWAAmountforCase() throws NbaBaseException {// Method name change APSL222
		int finActivityCount = getNbaTxLife().getPolicy().getFinancialActivityCount();
		for (int i = 0; i < finActivityCount; i++) {
			FinancialActivity finActivity = getNbaTxLife().getPolicy().getFinancialActivityAt(i);
			FinancialActivityExtension finActExtension = NbaUtils.getFirstFinancialActivityExtension(finActivity); // APSL222
			if (finActivity != null && finActExtension.getDisbursedInd() == false
					&& !(finActivity.getFinActivityType() == NbaOliConstants.OLI_FINANCIALACTIVITYTYPE_LOANCARRYOVER)) {// APSL
																														// 222,CR1664600(APSL3116)
				if (!(finActivity.hasFinActivitySubType() && finActivity.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND
						|| finActivity.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REFUND
						|| finActivity.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REV)) {

					FinancialActivity finActivityRefund = finActivity.clone(false);
					finActivityRefund.setUserCode(getUser().getUserID()); // AXAL3.7.13
					int aaCount = finActivityRefund.getAccountingActivity().size();
					while (aaCount > 0) {
						finActivityRefund.removeAccountingActivityAt(--aaCount);
					}
					// CODE DELETED APSL222

					if (finActExtension != null) {
						finActExtension.setDisbursedInd(true);
						finActExtension.setActionUpdate();
					}

					finActivityRefund.setFinActivitySubType(NbaOliConstants.OLI_FINACTSUB_REFUND);
					FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(finActivityRefund);
					if (finActExt != null) {
						finActExt.setAcctgExtractInd(false);
					}
					finActExt.setDisbursedInd(false);
					finActExt.setActionAdd();
					finActivityRefund.setId(null);
					// Begin ALS4888
					NbaOLifEId olifeid = new NbaOLifEId(getNbaTxLife());
					if (finActivityRefund.getPaymentCount() > 0) {
						finActivityRefund.getPaymentAt(0).setId(null);
						olifeid.setId(finActivityRefund.getPaymentAt(0));
						finActivityRefund.getPaymentAt(0).setActionUpdate();
					}
					// End ALS4888
					finActivityRefund.setFinActivityDate(new Date()); // ALS5284
					finActivityRefund.setFinEffDate(new Date()); // ALS5284
					olifeid.setId(finActivityRefund);
					finActivityRefund.setActionAdd();
					// APSL2735 begin
					if (finActivity.hasReferenceNo()) {
						finActivityRefund.setReferenceNo(finActivity.getReferenceNo());
					}
					if (finActExtension.hasConfirmationID()) {
						finActExt.setConfirmationID(finActExtension.getConfirmationID());
					}
					// APSL2735 end
					finActivityRefund.setOrderSource(finActivity.getId()); // NBLXA-1356
					getNbaTxLife().getPolicy().addFinancialActivity(finActivityRefund);
					// update existing record
					finActivity.setAccountingActivityType(NbaConstants.TRUE);
					finActivity.setAction(NbaActionIndicator.ACTION_UPDATE);
					if (finActExtension.getCreditCardTransID() != null) { // Payment was made by credit card
						processCreditCardRefund(getWork(), finActivity, finActivityRefund);
					}
				}
			}
		}
	}

	/**
	 * Iterates through the attachment objects Returns true if the Advance Chargeback Request has a user code of 3
	 * 
	 * @param nbaTxLife
	 *            An instance of <code>NbaTXLife</code> holding inquiry
	 * @return boolean whether attachment type 1000500004 exists with a userCode 3.
	 */
	// NBA137 New Method
	private boolean isAgtAdvChgbkRequestFailed(NbaTXLife nbaTXLife) {
		boolean agtAdvChgbkRequestFailed = false;

		int attachmentCount = nbaTXLife.getPrimaryHolding().getAttachmentCount();
		Holding primaryHolding = nbaTXLife.getPrimaryHolding();
		for (int i = 0; i < attachmentCount; i++) {
			Attachment attachment = primaryHolding.getAttachmentAt(i);
			String userCode = attachment.getUserCode();
			if (NbaOliConstants.OLI_ATTACH_AGTADVSTATUS == attachment.getAttachmentType()
					&& NbaAgentAdvancesRequestor.USER_CODE_AGTAGVCHGBKREQNEEDED.equals(userCode)) {
				agtAdvChgbkRequestFailed = true;
				break;
			}
		}

		return agtAdvChgbkRequestFailed;
	}

	/**
	 * Perform the Final Disposition Automated Process. Send a message to the back end system to set the underwriting status of the case unless the
	 * final disposition is "issued" or there is CWA present on the case. Set the successful status based on the answer from VP/MS. The status is
	 * based on the value of the final disposition and the CWA total. Update the work item in AWD with the results.
	 *
	 * @param user,
	 *            the user for whom the process is being executed
	 * @param work,
	 *            a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws com.csc.fsg.nba.exception.NbaBaseException {
		if (!initialize(user, work)) {
			return getResult(); // NBA050
		}
		// Begin APSL2735 EIP
		Date latestFinDate = getLatestFinActivityDate();// ALII2043
		if (isACHPaymentAndNotNegativeDisp()) {
			NbaDst parentCase = retrieveCaseAndTransactions(getWork(), getUser(), true, false); // ALS4808
			setWork(parentCase); // ALS4808
			if (!isAnyOpenPaymentNotification(parentCase)) {
				createNewPaymentNotification();
				doUpdateWorkItem();
			}
			if (!getOrigWorkItem().isSuspended()) {
				// APSL2043 begin, APSL3836 begin - Code refactored
				addComment("SUSPENDED : Payment Notification work item needs review");
				setSuspendVO(getSuspendWorkVO(Calendar.DATE, DAY));
				updateForSuspend();
				// ALII2043 ends
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "")); // APSL3836
				// APSL3836 end
				return getResult();
			}
		}
		if (latestFinDate != null && NbaUtils.calcDaysDiff(new Date(), latestFinDate) < refundDays) { // APSL3836
			addComment("SUSPENDED : Electronic payment can not be refund within 10 business days");
			setSuspendTime(latestFinDate);
			updateForSuspend(); // APSL3836
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "")); // APSL3836
			return getResult();
		}

		// End APSL2735 EIP
		//NBLXA-2340 Begins
		if (! NbaUtils.isBlankOrNull(getOrigWorkItem().getNbaLob().getUndwrtQueue())) { //NBLXA-2392--if condition
			NbaSearchVO searchVO = searchWorkItemInUWQ();
			if (!NbaUtils.isBlankOrNull(searchVO)) {
				List wiList = searchVO.getSearchResults();
				if (!NbaUtils.isBlankOrNull(wiList)) {
					addComment("SUSPENDED : Workitem in UW's queue needs review");
					setSuspendVO(getSuspendWorkVO(Calendar.DATE, UWDAY));
					updateForSuspend();
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "")); // APSL3836
					return getResult();
				}
			}
			// NBLXA-2340 Ends
		}
		doProcess();
		// AXAL3.7.21 start
		if (getResult() == null) {
			sendCaseInformation();
			// begin AXAL3.7.22
			// Begin ALS5155
			if (isECSCallNeeded()) {
				ApplicationInfo applicationInfo = getNbaTxLife().getPolicy().getApplicationInfo();
				ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
				if (applicationInfoExtension != null) {
					if (NbaUtils.triggerUpdateECS(applicationInfoExtension.getUnderwritingStatus())) { // APSL5311
						updateECS(user, getNbaTxLife(), getWork()); // APSL3385
						NbaUtils.updateECSActivity(nbaTxLife); // NBLXA-253
						NbaUtils.addECSActivity(nbaTxLife, user.getUserID(), NbaOliConstants.OLI_ACTTYPE_REVERSE_ECS_CALL); // NBLXA-253
						handleHostResponse(doContractUpdate()); // NBLXA-253

					}
				}
			}
		}
		// End ALS5155
		// AXAL3.7.21 end
		// AXAL3.7.26 begin
		if (NbaUtils.isOLSACallEnabled()) {
			if (isOLSACallNeeded()) {
				invokeOlsa(work);// APSL2555
			}
		}
		// AXAL3.7.26 end.
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		sendWorkItemsToEnd();// NBA254 ALS3767
		changeStatus(getResult().getStatus());
		// Update the Work Item with it's new status and update the work item in AWD
		// BEGIN NBLXA-1632
		if (isCOILCaseNegativeDisp()) {
			generatePrintWorkItem(user, String.valueOf(NbaOliConstants.OLI_ATTACH_WHOLEPOLICY), work);
		}
		// END NBLXA-1632
		//Start NBLXA-2402(NBLXA-2569)
		boolean isMagnumCase = AxaMagnumUtils.isMagnumCase(nbaTxLife);
		if(isMagnumCase) {
			callMagnumWebServices(nbaTxLife);//NBLXA-2402(NBLXA-2569)
		}
		doUpdateWorkItem();
		// NBA020 code deleted
		// Return the result
		return getResult();
	}
	
	//NBLXA-2402 New Method
    public void callMagnumWebServices(NbaTXLife nbaTxLife)throws NbaBaseException {
			Holding paramedHolding = AxaMagnumUtils.getParamedMagnumHolding(nbaTxLife);
			//NBLXA-2569
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_GET_CASE_SUMMARY,
					new NbaUserVO(), getNbaTxLife(), null, paramedHolding); 
			webServiceInvoker.execute();
			//NBLXA-2591
			webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_GET_CASE_DATA,
					new NbaUserVO(), getNbaTxLife(), null, paramedHolding);
			webServiceInvoker.execute();
    }
	/**
	 * The method make a call to OLSA interface if following conditions are met: 1) If case is a salary or a military allotment and i)Has status as
	 * cancel or incomplete and has a NBPRTEXT WI with status PRINTPASSD ii) Has status as AUTOCLOSE.
	 * 
	 * @throws NbaBaseException
	 * 
	 */
	// AXAL3.7.26 new method added
	protected boolean isOLSACallNeeded() throws NbaBaseException {
		NbaDst nbaDst = retrieveWorkItem(getWork().getID());
		String printWIStatus = getPrintWIStatus(nbaDst);
		NbaVpmsAdaptor vpmsAdaptor = getVpmsAdaptor();
		vpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_OLSACALLNEEDED);
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_PrintWIStatus", printWIStatus);
		vpmsAdaptor.setSkipAttributesMap(deOinkMap);
		VpmsComputeResult computeResult = null;
		try {
			computeResult = vpmsAdaptor.getResults();
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS, re);
			// begin ALS5009
		} finally {
			try {
				if (vpmsAdaptor != null) {
					vpmsAdaptor.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		// end ALS5009
		return NbaConstants.TRUE_STR.equalsIgnoreCase(computeResult.getResult());
	}

	/**
	 * @param nbaDst
	 * @return
	 */
	protected String getPrintWIStatus(NbaDst nbaDst) {
		String status = null;
		Iterator transactionList = null;
		try {
			transactionList = nbaDst.getNbaTransactions().iterator();
			while (transactionList.hasNext()) {
				NbaTransaction transaction = (NbaTransaction) transactionList.next();
				if (NbaConstants.A_WT_CONT_PRINT_EXTRACT.equalsIgnoreCase(transaction.getWorkType())) {
					status = transaction.getStatus();
					break;
				}
			}
		} catch (NbaNetServerDataNotFoundException e) {
			getLogger().logError("Error fetching transactions from AWD");
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * @param nbaDst
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	protected NbaVpmsAdaptor getVpmsAdaptor() throws NbaBaseException, NbaVpmsException {
		NbaVpmsAdaptor olsaVpmsAdaptor;
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
		oinkData.setContractSource(getNbaTxLife(), getWork().getNbaLob());
		olsaVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.FINAL_DISPOSITION);
		return olsaVpmsAdaptor;
	}

	/**
	 * This method retrieves the work item referenced in the NbaSearchResultVO object.
	 * 
	 * @param resultVO
	 *            the work item to be retrieved; The informal application work item.
	 * @return the retrieved work item
	 * @throws NbaBaseException
	 */
	// AXAL3.7.26 new method added
	protected NbaDst retrieveWorkItem(String workItemID) throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(workItemID, true);
		retOpt.setLockWorkItem();// ALS3379 deleted line
		retOpt.requestTransactionAsChild();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}

	/**
	 * The method sets the change sub type to NbaOliConstants.OLI_CHG_CHGRISK, creates 502 request using the change sub type and then calls the ECS
	 * web service using the request created.
	 * 
	 * @param userVO
	 * @param holdingInq
	 * @throws NbaBaseException
	 */
	// APSL3385 Modified Method Signature
	protected void updateECS(NbaUserVO userVO, NbaTXLife holdingInq, NbaDst nbaDst) throws NbaBaseException {
		List changeSubTypeList = new ArrayList();
		ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_CHGRISK, NbaOliConstants.TC_CONTENT_UPDATE);
		changeSubTypeList.add(changeSubType);
		Map parametersMap = new HashMap();
		parametersMap.put("changeSubTypeList", changeSubTypeList);
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ECSUPDATE, userVO, holdingInq, nbaDst,
				parametersMap);// APSL3385 pass nbadst instead on null//Map is sent as object over here since the call to ECS made from
								// AxaUpdateExpressCommTransaction needed two things
								// to be passed, so Map is used over there and so we need to use Map over here as well.
		webServiceInvoker.execute();
	}

	/**
	 * This method send 1203 transaction to the back-end.
	 * 
	 * @throws NbaBaseException
	 */
	// AXAL3.7.21 New Method
	protected void sendCaseInformation() throws NbaBaseException {
		if (!nbaTxLife.paidReissue()) { // APSL2662
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ADD_PRIOR_INSURANCE, user, nbaTxLife,
					null, null);
			webServiceInvoker.execute();
		}
	}

	/**
	 * This method returns the FinancialActivity object which has FinActivityType of UNAPPLDCASHIN - 278
	 * 
	 * @return finActivity FinancialActivity object
	 */
	// NBA077 new method
	protected FinancialActivity getSuspenseMoney() {
		ArrayList finActivities = getNbaTxLife().getPrimaryHolding().getPolicy().getFinancialActivity();
		int finActCount = finActivities.size();
		FinancialActivity finActivity = null;
		for (int i = 0; i < finActCount; i++) {
			finActivity = (FinancialActivity) finActivities.get(i);
			if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == finActivity.getFinActivityType()) {
				return finActivity;
			}
		}
		return null;
	}

	/**
	 * This method processes Reinstatement payment to make the policy back to active status from lapsed.
	 * 
	 * @param suspenseFinActivity
	 *            FinancialActivity object
	 * @param olifeId
	 *            NbaOLifEId
	 * @throws NbaBaseException
	 */
	// NBA077 new method
	protected void processReinstatementPayment(FinancialActivity suspenseFinActivity, NbaOLifEId olifeId) throws NbaBaseException {
		createFinActToRemoveSuspenseMoney(suspenseFinActivity, olifeId);
		createFinActForReinstatement(suspenseFinActivity, olifeId);
		updateSuspenseFinancialActivity(suspenseFinActivity);
		getNbaTxLife().getPrimaryHolding().getPolicy().setActionUpdate();
	}

	/**
	 * This method creates a new FinancialActivity object with FinActivityType of UNAPPLDCASHOUT - 279 to remove UNAPPLDCASHIN - 278 money
	 * 
	 * @param suspenseFinActivity
	 *            FinancialActivity object
	 * @param olifeId
	 *            NbaOLifEId
	 */
	// NBA077 new method
	protected void createFinActToRemoveSuspenseMoney(FinancialActivity suspenseFinActivity, NbaOLifEId olifeId) {
		FinancialActivity removeSuspenseFinActivity = suspenseFinActivity.clone(false);
		removeSuspenseFinActivity.deleteId();
		olifeId.setId(removeSuspenseFinActivity);
		removeSuspenseFinActivity.setAccountingActivity(new ArrayList());
		removeSuspenseFinActivity.setFinActivityType(NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHOUT);
		FinancialActivityExtension finActExtension = NbaUtils.getFirstFinancialActivityExtension(removeSuspenseFinActivity);
		if (finActExtension != null) {
			finActExtension.setDisbursedInd(false);
			finActExtension.setAcctgExtractInd(false);
		}
		removeSuspenseFinActivity.setActionAdd();
		getNbaTxLife().getPrimaryHolding().getPolicy().addFinancialActivity(removeSuspenseFinActivity);
	}

	/**
	 * This method changes the value of DataRep attribute from "Full" to "Removed" in FinancialActivity object with FinActivityType of UNAPPLDCASHIN -
	 * 278
	 * 
	 * @param suspenseFinActivity
	 *            FinancialActivity object
	 */
	// NBA077 new method
	protected void updateSuspenseFinancialActivity(FinancialActivity suspenseFinActivity) {
		suspenseFinActivity.setDataRep(NbaOliConstants.DATAREP_TYPES_REMOVED);
		suspenseFinActivity.setActionUpdate();
	}

	/**
	 * This method creates a new FinancialActivity object with FinActivityType of REINPYMT - 248. In this new FinancialActivity object the
	 * AcctgExtractInd will be set to true so that extracts will not be generated for this Reinstatement payment.
	 * 
	 * @param suspenseFinActivity
	 *            FinancialActivity object
	 * @param olifeId
	 *            NbaOLifEId
	 * @return finActForReinstatement FinancialActivity object
	 */
	// NBA077 new method
	protected void createFinActForReinstatement(FinancialActivity suspenseFinActivity, NbaOLifEId olifeId) {
		FinancialActivity finActForReinstatement = suspenseFinActivity.clone(false);
		finActForReinstatement.deleteId();
		olifeId.setId(finActForReinstatement);
		finActForReinstatement.setAccountingActivity(new ArrayList());
		finActForReinstatement.setFinActivityType(NbaOliConstants.OLI_FINACT_REINPYMT);
		FinancialActivityExtension finActExtension = NbaUtils.getFirstFinancialActivityExtension(finActForReinstatement);
		if (finActExtension != null) {
			finActExtension.setDisbursedInd(false);
			finActExtension.setAcctgExtractInd(true);
		}
		finActForReinstatement.setActionAdd();
		getNbaTxLife().getPrimaryHolding().getPolicy().addFinancialActivity(finActForReinstatement);
	}

	// NBA254 New Mothod to Set the Underwriting Approval on the Automatic Closure Cases
	private void updateUnderwtitingStatus(NbaTXLife txLife) { // ALS4834 change method signature
		ApplicationInfo appInfo = txLife.getNbaHolding().getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExt != null) {
			// ALS5552 Removed
			appInfoExt.setUnderwritingStatus(getClsoureTypeFromString(appInfoExt.getClosureType()));
			appInfoExt.setActionUpdate();
			getWork().getNbaLob().setCaseFinalDispstn((int) getClsoureTypeFromString(appInfoExt.getClosureType())); // ALS4834
		}
		txLife.getNbaHolding().getPolicy().setActionUpdate();
	}

	private long getClsoureTypeFromString(String closureType) {
		long closureTypeTC = -1;
		if (closureType != null) {
			if (INCOMPLETE_STRING.equals(closureType)) {// ALS5041
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_INCOMPLETE;
			} else if (NOT_TAKEN_STR.equals(closureType)) {
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_NTO;
			} else if (OFFER_EXPIRED_STR.equals(closureType)) {
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_OFFEREXPIRED;
			} else if (REG60_DECLINE_STR.equals(closureType) || PRESAL_EDECLINE_STR.equals(closureType)) { // ALS2773
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_WITHDRAW; // ALS2773
			} else if (CANCELLED_STR.equals(closureType)) {// ALS5041
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_CANCELLED;// ALS5041
			} else if (REG60_PRESALE_EXPIRED.equals(closureType)) {// APSL5125
				closureTypeTC = NbaOliConstants.NBA_FINALDISPOSITION_REG60_PRESALE_EXPIRED;// APSL5125
			}
		}
		return closureTypeTC;
	}

	// NBA254 New Mothod to Set the Closure Indicator on the Automatic Closure Cases
	private void updateClosureIndicator(NbaTXLife txLife) throws NbaBaseException {
		ApplicationInfo appInfo = txLife.getNbaHolding().getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExt != null) {
			appInfoExt.setClosureInd(String.valueOf(NbaConstants.NEGATIVE_DISP_DONE));
		}
		NbaAutoClosureAccessor.updateClosureIndicator(txLife.getNbaHolding().getPolicy().getPolNumber(), 1);
	}

	// ALS5090 New Mothod to delete the Reg60 records from tha NBAAUX DataBase.
	private void deleteReg60Records(NbaTXLife txLife) throws NbaBaseException {
		NbaAutoClosureAccessor naca = new NbaAutoClosureAccessor();
		AutoClosureCriteria acc = new AutoClosureCriteria();
		acc.setWorkItemID(getWork().getID());
		acc.setContractNumber(getWork().getNbaLob().getPolicyNumber());
		naca.deleteNIGORecord(acc);
	}

	// NBA254 New Method
	protected void processCreditCardRefund(NbaDst caseDst, FinancialActivity finAct, FinancialActivity newFinact) throws NbaBaseException {
		NbaUserVO userVO = new NbaUserVO(NbaConstants.PROC_CWA_REVERSE_REFUND, "");
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(userVO, caseDst);
		NbaTransaction refundWork = caseDst.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		NbaLob refundLob = refundWork.getNbaLob();
		FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(finAct);
		String ccID = finActExt.getCreditCardTransID();
		String transCCID = null;
		NbaLob workLob = null;
		String refundCCID = null;
		ListIterator transList = caseDst.getNbaTransactions().listIterator();
		while (transList.hasNext()) {
			workLob = ((NbaTransaction) transList.next()).getNbaLob();
			transCCID = workLob.getCCTransactionId();
			if (transCCID != null && transCCID.equalsIgnoreCase(ccID)) {
				refundLob.setCCNumber(workLob.getCCNumber());
				refundLob.setCCExpDate(workLob.getCCExpDate());
				refundLob.setCCType(workLob.getCCType());
				refundLob.setCCBillingName(workLob.getCCBillingName());
				double refundAmount = 0;
				if (finAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REFUND) {
					refundAmount = finAct.getFinActivityGrossAmt();
				} else if (finAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND) {
					refundAmount = finActExt.getPartialRefundAmt();
				}
				refundLob.setCwaAmount(refundAmount);
				refundLob.setCCBillingAddr(workLob.getCCBillingAddr());
				refundLob.setCCBillingCity(workLob.getCCBillingCity());
				refundLob.setCCBillingState(workLob.getCCBillingState());
				refundLob.setCCBillingZip(workLob.getCCBillingZip());
				refundCCID = NbaCreditCardData.createCCTransactionId();
				refundLob.setCCTransactionId(refundCCID);
				FinancialActivityExtension newFinActExt = NbaUtils.getFirstFinancialActivityExtension(newFinact);
				newFinActExt.setCreditCardTransID(refundCCID);
				newFinActExt.setActionUpdate();
				break;
			}
		}
	}

	private void invokeOlsa(NbaDst dst) throws NbaBaseException {// APSL2555
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_OLSA, getUser(), getNbaTxLife(), dst,
				null);// APSL2555
		webServiceInvoker.execute();
	}

	/**
	 * Sends all the workitems associated with the case, with the exception of miscellaneous work, to the End queue when a case is negatively disposed
	 * Sets ACST lob on all workitems which will be sent to END queue by this(final disposition)poller,with the exception of Print,CWA and
	 * AggregateContract workitems. * @throws NbaBaseException
	 */
	// NBA254-ALS3767 New Method
	private void sendWorkItemsToEnd() throws NbaBaseException {
		List unsuspendVOs = new ArrayList();
		// ALS4808 - deleted code
		List transactions = getWork().getNbaTransactions();
		Iterator transactionItr = transactions.iterator();
		// ALS4933/ALS3767/ALS4839/ALS4728 Begin
		boolean miscWorkOnCase = false;
		boolean reInsCorrInfo = false; //NBLXA-2114
		NbaTransaction tempAggrTransaction = null;
		List tempAggrTransList = new ArrayList(); // APSL1295
		NbaTransaction transaction = null; // ALS5286 code change
		while (transactionItr.hasNext()) {
			transaction = (NbaTransaction) transactionItr.next();
			// Start NBLXA-2114
			reInsCorrInfo = false;
			if (transaction != null && transaction.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CORRESPONDENCE)
					&& transaction.getNbaLob().getLetterType().equalsIgnoreCase(NbaConfiguration.getInstance()
							.getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_ACCEPT_REJECT_LETTER))) {
				reInsCorrInfo = true;

			}
			//End NBLXA-2114
			if (!transaction.isInEndQueue() && !reInsCorrInfo) { //NBLXA-2114
				// Begin SR534322 - Performance Related Changes
				if (isResetEligibleWorkType(transaction)) {
					transaction.getNbaLob().setAutoClosureStat(transaction.getStatus());
					transaction.setUpdate();
				}
				// End SR534322 - Performance Related Changes
				// At final disp processing, if the case has a NBMISCWORK item attached, then leave NBMMSICWORK and NBAGGNT alone.
				// If the case doesnt have have a NBMISCWORK WI attached, then move the NBAGGCNT to the END.
				if (A_WT_MISC_WORK.equals(transaction.getWorkType())) {// ALS4839
					if (NbaConstants.A_QUEUE_AGGREGATE_CONTRACT.equalsIgnoreCase(transaction.getQueue()) || // CR59174
							NbaConstants.A_QUEUE_AGGREGATE_1035.equalsIgnoreCase(transaction.getQueue())) {// ALS4839 CR59174
						miscWorkOnCase = true;
					}
				} else if (A_WT_AGGREGATE_CONTRACT.equals(transaction.getWorkType())) {
					// APSL1295 - Code deleted
					tempAggrTransList.add(transaction); // APSL1295
				} else if (!reInsCorrInfo) {
					transaction.setStatus(A_STATUS_NEG_DISPOSED);
					NbaUtils.setRouteReason(transaction, A_STATUS_NEG_DISPOSED); // ALS5286
					if (transaction.isSuspended()) {
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						suspendVO.setTransactionID(transaction.getID());
						unsuspendVOs.add(suspendVO);
					}
				}
				// Start NBLXA-2114
				
				if (reInsCorrInfo && transaction.isSuspended()) {
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(transaction.getID());
					unsuspendVOs.add(suspendVO);
				}
				//NBLXA-2114

			}
		}

		if (!miscWorkOnCase && tempAggrTransList.size() > 0) { // start APSL1295
			Iterator aggCntItr = tempAggrTransList.iterator();
			while (aggCntItr.hasNext()) {
				tempAggrTransaction = (NbaTransaction) aggCntItr.next(); // end APSL1295
				tempAggrTransaction.setStatus(A_STATUS_NEG_DISPOSED);
				NbaUtils.setRouteReason(tempAggrTransaction, A_STATUS_NEG_DISPOSED); // ALS5286
				if (tempAggrTransaction.isSuspended()) {
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(tempAggrTransaction.getID());
					unsuspendVOs.add(suspendVO);
				}
			}
		}

		// ALS4933/ALS3767/ALS4839/ALS4728 end
		// updateWork(getUser(), getWork()); //ALS4808
		// ALS4808 - deleted code
		unsuspendWorkitems(unsuspendVOs);

	}

	/**
	 * Retrieve the Case and Transactions associated
	 * 
	 * @param dst
	 *            workItem / case, for which the sibiling transactions / child transactions need to be retrieved along with the case
	 * @param user
	 *            AWD user id
	 * @param locked
	 *            lock indicator
	 * @return NbaDst containing the case and all the transactions
	 * @throws NbaBaseException
	 */
	// NBA254-ALS3767 New Method
	protected NbaDst retrieveCaseAndTransactions(NbaDst dst, NbaUserVO user, boolean lockWork, boolean lockTran) throws NbaBaseException { // ALS5177
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(dst.getID(), true);
		retOpt.requestTransactionAsChild();
		if (lockWork) {
			retOpt.setLockWorkItem();
		}
		if (lockTran) {
			retOpt.setLockTransaction();
		}
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult) ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		NewBusinessAccelBP.processResult(workResult);// ALS5177
		return (NbaDst) workResult.getFirst();
	}

	/**
	 * Iterate thru unsuspendVO list and call unsuspendWork method on netserveraccessor bean for each value object.
	 * 
	 * @throws NbaBaseException
	 */
	// NBA254-ALS3767 New Method
	protected void unsuspendWorkitems(List unsuspendVOs) throws NbaBaseException {
		int size = unsuspendVOs.size();
		for (int i = 0; i < size; i++) {
			unsuspendWork((NbaSuspendVO) unsuspendVOs.get(i));
		}
	}

	// ALS5155 New Method
	private boolean isECSCallNeeded() {
		return (NbaUtils.isCompensationCallEnabled() && NbaUtils.isExpressCommissionsent(nbaTxLife)); // NBLXA-253
	}

	// APSL1150 New Method
	private void applyUWApproveAndDisposition() throws NbaBaseException {
		nbaTxLife.setAccessIntent(1);
		NbaUnderwriterWorkbenchFacadeBean bean = new NbaUnderwriterWorkbenchFacadeBean();
		// begin ALS5547
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), true);
		retOpt.setLockWorkItem();
		retOpt.requestSources();
		retOpt.requestTransactionAsChild();
		bean.setNbaDstWithAllTransactions(retrieveWorkItem(getUser(), retOpt));
		// end ALS5547
		bean.applyApproveAndDisposition(getUser(), nbaTxLife, getWork(), getUser().getUserID(), "-1", nbaTxLife.isInformalApp());
	}

	// New Methods Begin - SR534322 Retrofit Performance Related Changes
	/**
	 * Checks if a work Item is eligible for reset functionality or not. Returns true for all workItems except Print,CWA and Aggregate Contract
	 * WorkItems. * @param transaction
	 * 
	 * @throws NbaVpmsException
	 */
	public boolean isResetEligibleWorkType(NbaTransaction transaction) throws NbaVpmsException {
		ArrayList getNonResetEligibleWorkTypesList = getNonResetEligibleWorkTypesList();
		Iterator listIterator = getNonResetEligibleWorkTypesList.iterator();
		while (listIterator.hasNext()) {
			String workType = (String) listIterator.next();
			if (workType.equals(transaction.getWorkType())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method returns list of workItems which are not eligible for Reset Functionality List is retrieved from VPMS Cuurently,Returns CWA,Print
	 * and Aggregate Contract workitem in the list * @throws NbaVpmsException
	 */
	private ArrayList getNonResetEligibleWorkTypesList() throws NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			if (nonEligibleWorkTypeList.isEmpty()) {
				NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
				Map deOink = new HashMap();
				deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
				vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.QUEUE_STATUS_CHECK);
				vpmsProxy.setVpmsEntryPoint(EP_GET_NONRESET_ELIGIBLE_WORKTYPE);
				vpmsProxy.setSkipAttributesMap(deOink);
				VpmsComputeResult compResult = vpmsProxy.getResults();
				NbaStringTokenizer tokens = new NbaStringTokenizer(compResult.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
				String aToken;
				while (tokens.hasMoreTokens()) {
					aToken = tokens.nextToken();
					nonEligibleWorkTypeList.add(aToken);
				}
			}
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Closure Check Problem" + NbaVpmsException.VPMS_EXCEPTION, re);
		} catch (NbaBaseException e) {
			getLogger().logDebug(
					"Exeception occurred while getting valid hold statuses from QUEUE STATUS CHECK VPMS Model : " + NbaUtils.getStackTrace(e));
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return nonEligibleWorkTypeList;
	}

	// End - SR534322 Retrofit Performance Related Changes
	// ALII2043 New Method
	private Date getLatestFinActivityDate() {
		Date latestFinDate = null;
		boolean isDisbursed = false; // APSL5140
		int finActivityCount = getNbaTxLife().getPolicy().getFinancialActivityCount();
		// ALII2083 code deleted
		for (int i = 0; i < finActivityCount; i++) {
			FinancialActivity finActivity = getNbaTxLife().getPolicy().getFinancialActivityAt(i);
			FinancialActivityExtension finActivityExtension = NbaUtils.getFirstFinancialActivityExtension(finActivity);
			isDisbursed = finActivityExtension.getDisbursedInd();
			if (NbaUtils.isElectronicPaymentForm(finActivity) && !isDisbursed) { // APSL3836, APSL5140 Extra Check for non disburse amount
				if (!(finActivity.getFinActivitySubType() == 1L || finActivity.getFinActivitySubType() == 2L
						|| finActivity.getFinActivitySubType() == 3L)) {// ALII2049,Exclude refund FinancialActivities
					// Begin APSL3836
					Date paymentDate = NbaUtils.getPaymentDraftDate(finActivity.getReferenceNo(), getNbaTxLife().getPolicy().getPolNumber());
					if (latestFinDate == null || paymentDate.after(latestFinDate)) { // ALII2083
						latestFinDate = paymentDate;
					}
					// End APSL3836
				}
			}
		}

		return latestFinDate;
	}

	// ALII2043 New Method
	private void setSuspendTime(Date latestFinDate) {
		// this will set activation date to 10 next days from latest finactivity date
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(latestFinDate);
		cal.add(Calendar.DATE, refundDays);
		suspendVO.setCaseID(getOrigWorkItem().getID());
		suspendVO.setActivationDate(cal.getTime());
		// APSL3836 code deleted
		setSuspendVO(suspendVO); // APSL3836
	}

	// ALII2043 New Method, APSL3836 method removed
	// APSL5140 -- New method to Check If reversal is available.
	private boolean checkReversalActivity() {
		boolean isMoneyReverse = false;
		int finActivityCount = getNbaTxLife().getPolicy().getFinancialActivityCount();

		for (int i = 0; i < finActivityCount; i++) {
			FinancialActivity finActivity = getNbaTxLife().getPolicy().getFinancialActivityAt(i);
			if (NbaUtils.isElectronicPaymentForm(finActivity)) {
				if (finActivity != null
						&& (finActivity.hasFinActivitySubType() && finActivity.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REFUND)) {

					isMoneyReverse = true;
				}
			}
		}

		return isMoneyReverse;
	}

	// NBLXA-1632 New Method
	private boolean isCOILCaseNegativeDisp() throws NbaBaseException {
		boolean isCOILCaseNegativeDisp = false;
		ApplicationInfo applicationInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if ((NbaUtils.isProductCodeCOIL(getNbaTxLife())) && NbaUtils.isNegativeDisposition(applicationInfoExtension.getUnderwritingStatus())) {
			isCOILCaseNegativeDisp = true;
		}

		return isCOILCaseNegativeDisp;
	}

	// NBLXA-1632 New Method
	protected void generatePrintWorkItem(NbaUserVO user, String extComp, NbaDst work) {
		NbaContractPrintFacadeBean facade = new NbaContractPrintFacadeBean();
		try {
			facade.generateContractExtract(user, work, extComp, false, null);
		} catch (NbaBaseException e) {
			e.printStackTrace();
		}
	}
	
	//NBLXA-2340 new method
	protected NbaSearchVO searchWorkItemInUWQ() throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setContractNumber(getNbaTxLife().getPolicy().getContractKey());
		searchVO.setQueue(getOrigWorkItem().getNbaLob().getUndwrtQueue());
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}
}