package com.csc.fsg.nba.business.transaction;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaAxaUnadmittedReplacementTransaction creates TXLife XML 213 request transaction and retrieves financial information 
 * from AXA’s EDW and determines if a policy is an unadmitted replacement.Financial information returned from AXA is 
 * evaluated by calling VPMS models where AXA’s business rules are defined.
 * <p>  
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> 
 * <thead><th align=left>Project</th><th align=left>Release</th><th align=left>Description</th></thead>
 * <tr><td>AXAL3.7.24</td><td>AXA Life Phase 1</td><td>Unadmitted Replacement Interface</td></tr>
 * <tr><td>ALS4811</td><td>AXA Life Phase 1</td><td>QC #3956 - Informals Regression: VP/MS Error encountered...</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUnadmittedReplTransaction extends NbaBusinessTransactions {
	double netDIVFinActivityAmount = 0;

	double netMAXFinActivityAmount = 0;

	double netSPECFinActivityAmount = 0;

	private boolean isDebuggingOn = getLogger().isDebugEnabled();

	/**
	 * Constructor for AxaUnadmittedReplTransaction.
	 */
	public AxaUnadmittedReplTransaction() {
		super();
	}

	/**
	 * This method takes three arguments and it will be the entry point for UNADMITTED REPLACEMENT PROCESSING
	 * 
	 * @param currentCaseTxLife		NbaTXLife object
	 * @param nbaDst		An instance of NbaDst
	 * @param userVO		The value object representation of the logged on user
	 * @return boolean indicates whether case is replacement or not
	 */
	public boolean checkCaseForUnadmittedReplacement(NbaTXLife currentCaseTxLife, NbaDst nbaDst, NbaUserVO userVO) throws NbaBaseException {
		Policy nbApolicy = currentCaseTxLife.getPolicy();
		ApplicationInfo applicationInfo = nbApolicy.getApplicationInfo();
		if (!applicationInfo.getReplacementInd()) {
			boolean loopedStatusCheckBlock = false;
			NbaTXLife priorInsTxLife = null;
			List priorInsPoliciesList = new ArrayList();
			List attachmentList = AxaUtils.getAttachmentsByType(currentCaseTxLife.getPrimaryParty().getParty(), NbaOliConstants.OLI_ATTACH_PRIORINS);
			for (int i = 0; i < attachmentList.size(); i++) {
				if (!applicationInfo.getReplacementInd()) { //If Not A Replacement Case
					Attachment attachment = (Attachment) attachmentList.get(i);
					if (attachment.getAttachmentData().getPCDATA() != null) {
						try {
							priorInsTxLife = new NbaTXLife(attachment.getAttachmentData().getPCDATA());
						} catch (Exception e) {
							if (isDebuggingOn) {
								getLogger().logDebug("Not able to extract 204 response data from TxLife " + e.getMessage());
							}
						}
						//ALS3340 Do Unadmitted Repl Processing ONLY for HIT response
						if (priorInsTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_HIT) != null) {
							Policy priorInsPolicy = priorInsTxLife != null ? priorInsTxLife.getPolicy() : null;
							if (priorInsPolicy != null) {
								loopedStatusCheckBlock = true;
								String priorPolNumber = priorInsPolicy.getPolNumber();
								long priorPolStatus = priorInsPolicy.getPolicyStatus();
								Date priorPolStatusChangeDate = priorInsPolicy.getStatusChangeDate();
								//Call the Unadmitted Replacement VPMS model for Status Activity
								if (checkPriorPolicyForStatus(Long.toString(priorPolStatus), priorPolStatusChangeDate, applicationInfo.getSignedDate())) {
									applicationInfo.setReplacementInd(true);
									nbApolicy.setReplacementType(NbaOliConstants.OLI_REPTY_UNADREPSTA);
								} else {
									priorInsPoliciesList.add(new AxaPriorInsPolicy(priorPolNumber, priorPolStatus));
								}
							}
						}
					}
				}
			}
			//Still If Not A Replacement Case and atleast one HIT response is returned by prior insurance call(checked by var loopedStatusCheckBlock)
			if (!applicationInfo.getReplacementInd() && loopedStatusCheckBlock) {
				Iterator priorInsPoliciesItr = priorInsPoliciesList.iterator();
				while (priorInsPoliciesItr.hasNext()) {
					AxaPriorInsPolicy axaPriorInsPolicy = (AxaPriorInsPolicy) priorInsPoliciesItr.next();
					Map deOinkMap = new HashMap();
					deOinkMap.put("A_PriorPolicyStatus", Long.toString(axaPriorInsPolicy.getPolicyStatus()));
					//Check if policy status is not in (4,6,,8,16,18,20)
					if (vpmsResult(NbaVpmsAdaptor.EP_CHECK_STATUS, deOinkMap)) {
						//create TXLife 213 request and call axa webservice ONLY for inforce policies
						AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_UNADMIT_REPL, userVO,
																										currentCaseTxLife, nbaDst, axaPriorInsPolicy.getPolNumber());
						NbaTXLife xmlResponse = (NbaTXLife) webServiceInvoker.execute();
						// processing the returned response
						if(xmlResponse != null) {//ALS5536
							extractFinActDataFromTx213Response(xmlResponse, axaPriorInsPolicy.getPolNumber());	
						}
					}
				}
				//Initialize the map containing total amount's
				Map finalAmountsMap = new HashMap();
				finalAmountsMap.put("divAmt", Double.toString(netDIVFinActivityAmount));
				finalAmountsMap.put("maxAmt", Double.toString(netMAXFinActivityAmount));
				finalAmountsMap.put("spclAmt", Double.toString(netSPECFinActivityAmount));
				//Call the Unadmitted Replacement VPMS model for Disbursement Activity
				if (checkPriorPolicyForDisbursment(finalAmountsMap)) {
					applicationInfo.setReplacementInd(true);
					nbApolicy.setReplacementType(NbaOliConstants.OLI_REPTY_UNADREPDISB);
				}
			}
			applicationInfo.setActionUpdate();
			nbApolicy.setActionUpdate();
			return applicationInfo.getReplacementInd();
		}
		return false;
	}

	/**
	 * This function calls the UNADMITTED REPLACEMENT model to check if prior policy replacement type is due to status
	 * @param priorPolicyStatus							String object
	 * @param priorPolicyStatusChangeDate			Date object in YYYY-MM-DD format
	 * @return Returns true if the call is successful Else returns false
	 */
	public boolean checkPriorPolicyForStatus(String priorPolicyStatus, Date priorPolicyStatusChangeDate, Date signedDate) {
		String applicationDate = NbaUtils.getStringInUSFormatFromDate(signedDate);
		String priorPolicyStatusChangeDateforVPMS = NbaUtils.getStringInUSFormatFromDate(priorPolicyStatusChangeDate);
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_StatusCheck", Boolean.toString(true));
		if (applicationDate != null) {
			deOinkMap.put("A_ApplicationDate", applicationDate);
		}
		if (priorPolicyStatus != null) {
			deOinkMap.put("A_PriorPolicyStatus", priorPolicyStatus);
		}
		if (priorPolicyStatusChangeDateforVPMS != null) {
			deOinkMap.put("A_PriorPolicyStatusChangeDate", priorPolicyStatusChangeDateforVPMS);
		}
		return vpmsResult(NbaVpmsAdaptor.EP_GET_REPLACEMENT_RESPONSE, deOinkMap);
	}

	/**
	 * This function calls the UnadmittedReplacement model to check replacement type due to disbursment
	 * @param Map		object The Map contains the finactivity response required for calling VPMS model.
	 * @return Returns true if the call is successful Else returns false
	 */
	public boolean checkPriorPolicyForDisbursment(Map finActivityResponse) {
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_DisbursementCheck", Boolean.toString(true));
		if (finActivityResponse.get("maxAmt") != null) {
			deOinkMap.put("A_PriorInsuranceLoanAmt", finActivityResponse.get("maxAmt"));
		}
		if (finActivityResponse.get("divAmt") != null) {
			deOinkMap.put("A_PriorInsuranceDividendAmt", finActivityResponse.get("divAmt"));
		}
		if (finActivityResponse.get("spclAmt") != null) {
			deOinkMap.put("A_PriorInsurancePartialWithdrawalAmt", finActivityResponse.get("spclAmt"));
		}
		return vpmsResult(NbaVpmsAdaptor.EP_GET_REPLACEMENT_RESPONSE, deOinkMap);
	}

	/**
	 * This method takes two arguments and processes the returned response from the AXA WebService.
	 * @param response				NbaTXLife object
	 * @param priorPolicyNumber	the String object contains policy no. used in TXLife213Req. This will be used to cross verify the response
	 */
	public void extractFinActDataFromTx213Response(NbaTXLife response, String priorPolicyNumber) {
		TransResult resultTL = response.getTransResult();
		if (priorPolicyNumber != null && resultTL != null && resultTL.getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) {
			Policy policy = response.getPolicy();
			if (policy != null && priorPolicyNumber.equals(policy.getPolNumber())) {
				for (int i = 0; i < policy.getFinancialActivityCount(); i++) {
					FinancialActivity financialActivity = policy.getFinancialActivityAt(i);
					if (financialActivity.getFinActivityStatus() == NbaOliConstants.OLI_FINACTSTAT_COMPLETED) {
						if (financialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_DIVPOL) {
							netDIVFinActivityAmount += financialActivity.getFinActivityGrossAmt();
						}
						if (financialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_SPECAMTGROSSWITH) {
							netSPECFinActivityAmount += financialActivity.getFinActivityGrossAmt();
						}
						if (financialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_MAXLOAN) {
							netMAXFinActivityAmount += financialActivity.getFinActivityGrossAmt();
						}
					}
				}
			}
		}
	}

	/**
	 * @param deOinkMap
	 * @return
	 */
	private boolean vpmsResult(String entryPoint, Map deOinkMap) {
		boolean status = false;
		NbaVpmsAdaptor adapter = null; //ALS4811
		try {
			adapter = new NbaVpmsAdaptor(new NbaOinkDataAccess(), NbaVpmsAdaptor.UNADMITTED_REPLACEMENT); //ALS4811
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				if (NbaConstants.TRUE_STR.equalsIgnoreCase((String) vpmsResultsData.getResultsData().get(0))) {
					status = true;
				}
			}
		} catch (Exception e) {
			if (isDebuggingOn) {
				getLogger().logDebug(this.getClass().getName() + "VPMS ERROR" + e.getMessage());
			}
		//begin ALS4811
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();					
				}			 
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		//end ALS4811
		return status;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private NbaLogger getLogger() {
		NbaLogger logger = null;
		try {
			logger = NbaLogFactory.getLogger(this.getClass().getName());
		} catch (Exception e) {
			NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
		}
		return logger;
	}

	/**
	 * This inner class represents the result returned by Prior Insurance Web Service 
	 */
	//AXAL3.7.24, ALS3338
	private class AxaPriorInsPolicy {
		private String polNumber = null;

		private long policyStatus = -1L;

		/**
		 * @return Returns the policyStatus.
		 */
		private long getPolicyStatus() {
			return policyStatus;
		}

		/**
		 * @return Returns the polNumber.
		 */
		private String getPolNumber() {
			return polNumber;
		}

		/**
		 * This contructor initializes the object's state from prior insurance data 
		 */
		private AxaPriorInsPolicy(String polNumber, long policyStatus) {
			this.polNumber = polNumber;
			this.policyStatus = policyStatus;
		}

		public String toString() {
			return "PolNumber: " + polNumber + ", PolicyStatus: " + policyStatus;
		}
	}
}