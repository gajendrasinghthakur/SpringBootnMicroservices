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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaCashieringTable;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractsWireTransferData;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
* NbaProcApplyMoney is the class that processes cases found in the AWD Apply Money queue (NBAPLCWA).
* This class
*		- creates XML508 transaction 
*		- sends the transaction to the back end system
*		- if the transaction fails, send the work item to the error queue
*		- if there is no error, update the Cashering and Wire Transfer tables
* <p>The NbaProcApplyMoney class extends the NbaAutomatedProcess class.  
* Although this class may be instantiated by any module, the NBA polling class 
* will be the primary creator of objects of this type.
* <p>When the polling process finds a case on the Apply Money queue, 
* it will create an object of this instance and call the object's 
* executeProcess(NbaUserVO, NbaDst) method.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead> 
* <tr><td>NBA009</td><td>Version 2</td><td>Cashering Component</td></tr>
* <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
* <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
* <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
* <tr><td>NBA086</td><td>Version 2</td><td>Performance Testing and Tuning</td></tr>
* <tr><td>SPR1554</td><td>Version 4</td><td>Payment Type is displayed incorrectly in CWA view</td></tr>
* <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
* <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
* <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
* <tr><td>SPR1876</td><td>Version 4</td><td>CWA Validation P001 does not calculate total CWA amount correctly</td></tr>
* <tr><td>SPR1841</td><td>Version 4</td><td>Wrong Entry Date and no As of Date in CWA and in reversal of CWA.</td></tr>
* <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
* <tr><td>NBA115</td><td>Version 5</td><td>Credit Card payment and authorization</td></tr>
* <tr><td>SPR2248</td><td>Version 6</td><td>Reinstatements not sent correctly for Traditional Term products and Advanced Products</td></tr>
* <tr><td>SPR3083</td><td>Version 6</td><td>Change AWDERROR to HOSTERROR in Apply Money and Redundancy Check</td></tr>
* <tr><td>SPR3123</td><td>Version 6</td><td>APAPLCWA does not release the case after having routed the CWA work item to error Queue.</td></tr>
* <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
* <tr><td>NBA173</td><td>Version 7</td><td>Indexing UI Rewrite Project</td></tr>
* <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
* <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>
* <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>  
* <tr><td>ALPC234</td><td>AXA Life Phase 1</td><td>Unbound Processing</td></tr>
* <tr><td>SR494086.7</td><td>Discretionary</td><td>ADC Retrofit</td>
* <tr><td>NBA298</td><td>AXA Life Phase 2</td><td>MEC Processing</td></tr>
* <tr><td>P2AXAL018</td><td>AXA Life Phase 2</td><td>Omission Requirements</td></tr>
* <tr><td>SR615900</td><td>Discretionary</td><td>Prevent Checks From Being Deposited When CWA Not Applied</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 2
* @see NbaAutomatedProcess
*/
public class NbaProcApplyMoney extends NbaAutomatedProcess {
	private FinancialActivity finActivity = null;  // NBA050
	 private List unsuspendList = null; 
	
	/**
	 * NbaProcApplyMoney constructor comment.
	 */
	public NbaProcApplyMoney() {
		super();
		//SPR1851 code deleted
	}
	/**
	 * This method constructs Xml 508 transaction to be sent to the host
	 * @return FinancialActivity An instance of FinancialActivity object
	 */
	// NBA050 NEW METHOD
	protected FinancialActivity createFinancialActivityObject() {
		try {
			NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);	//NBA050	
			//Get Lob
			NbaLob lob = getWork().getNbaLob();
			//Create Financial Activity Object
			FinancialActivity financialActivity = new FinancialActivity();
			nbaOLifEId.setId(financialActivity); //NBA050			
			financialActivity.setFinancialActivityKey("01");
			// Begin APSl5164
			OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
			FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
			//End APSL5164
			//Begin APSL3683
			if (NbaOliConstants.OLI_PAYFORM_EFT == Long.valueOf(lob.getPaymentMoneySource())) { //QC#18723
				financialActivity.setFinActivityDate(lob.getCwaDate());
				// Begin APSL5164 NBLXA-1256 code deleted for variable product
				if (!NbaUtils.isBlankOrNull(lob.getCwaDate()) && !NbaUtils.isBlankOrNull(lob.getCwaTime())) {
						java.text.SimpleDateFormat time_sdf = new java.text.SimpleDateFormat("hh:mmaa"); // APSL5164
						String strEntryTime = lob.getCwaTime();
						Date dateTime = NbaUtils.getTimefromString(strEntryTime);
						strEntryTime = time_sdf.format(dateTime);
						String cipedate = NbaUtils.getStringFromDate(lob.getCwaDate());
						String cipeDateTime = cipedate.trim() + " " + strEntryTime;
						boolean flag = NbaUtils.isDateAfterTodays4PM(lob.getCwaTime());
						if (flag && NbaUtils.isVariableProduct(nbaTxLife)) { // NBLXA-1256 code modified for variable product
							Date currentFinEffDate = lob.getCwaDate();
							Calendar c = Calendar.getInstance();
							c.setTime(currentFinEffDate);
							c.add(Calendar.DATE, 1);
							Date newFinEffDate = NbaUtils.getDateFromString(NbaUtils.getStringFromDate(c.getTime()));
							financialActivity.setFinEffDate(newFinEffDate);
							finactext.setCipeDateTime(cipeDateTime);
						} else {
							financialActivity.setFinEffDate(lob.getCwaDate());
							finactext.setCipeDateTime(cipeDateTime);
						}
					} else if (!NbaUtils.isBlankOrNull(lob.getCwaDate()))
						financialActivity.setFinEffDate(lob.getCwaDate());
					else
						financialActivity.setFinEffDate(lob.getReceiptDate());

					if (!NbaUtils.isBlankOrNull(lob.getCIPEPaymentIGO()) && NbaConstants.CIPEREQ_IGO.equalsIgnoreCase(lob.getCIPEPaymentIGO())) {// Constants moved from NbaOliConstants to NbaConstants
						finactext.setCipeIgo(NbaConstants.YES_VALUE);
					} else if (!NbaUtils.isBlankOrNull(lob.getCIPEPaymentIGO())
							&& (NbaConstants.CIPEREQ_NIGO).equalsIgnoreCase(lob.getCIPEPaymentIGO())) {// Constants moved from NbaOliConstants to NbaConstants
						finactext.setCipeIgo(NbaConstants.NO_VALUE);
						finactext.setCipeReqCode(lob.getCIPEPaymentReqType());
						finactext.setOtherReason(lob.getCipeOtherReason());
					}

				// APSL5164
			}
				else{
				financialActivity.setFinActivityDate(lob.getReceiptDate());
				financialActivity.setFinEffDate(lob.getReceiptDate()); //SPR1841
			}
			//END APSL3683
			//NBA093 deleted 8 lines
			if (lob.getPendPaymentType() > 0) { //NBA173
			    financialActivity.setFinActivityType(lob.getPendPaymentType()); //NBA173
			} else if (lob.getExchangeReplace() > 0) { //NBA173
				if (lob.getExchangeReplace() == 2) {
					financialActivity.setFinActivityType(NbaOliConstants.OLI_FINACT_1035INIT);  //NBA093 //SPR1554
				} else {
					financialActivity.setFinActivityType(lob.getExchangeReplace());  //NBA093
				}
			} else {
				financialActivity.setFinActivityType(NbaOliConstants.OLI_FINACT_PREMIUMINIT);  //NBA093 //SPR1554
			}
			// Begin APSL5164
			if (NbaOliConstants.OLI_PAYFORM_EFT == Long.valueOf(lob.getPaymentMoneySource())) {
				if (nbaTxLife.isUnderwriterApproved()
						|| (lob.getContractChgType() != null && Long.parseLong(lob.getContractChgType()) == NbaOliConstants.NBA_CHNGTYPE_REISSUE)) {
					financialActivity.setFinActivityType(NbaOliConstants.OLI_FINACT_PREMIUMINIT);
					finactext.setCipePaymentType(NbaOliConstants.OLI_CIPEPAYMENTTYPE_INITIAL);
				} else {
					financialActivity.setFinActivityType(NbaOliConstants.OLI_FINACT_CWA);
					finactext.setCipePaymentType(NbaOliConstants.OLI_CIPEPAYMENTTYPE_CWA);
				}

			} // End APSL5164
			//NBA093 deleted line
			String sourceType = getSourceType();
			if (sourceType != null && sourceType.equals(NbaConstants.A_ST_XML508)) {
				if (lob.getCostBasis() >= 0) {
					financialActivity.setCostBasisAdjAmt(lob.getCostBasis());  //NBA093
				}
			} else {
				if (lob.getCostBasis() > 0) {
					financialActivity.setCostBasisAdjAmt(lob.getCostBasis());  //NBA093
				}
			}
			if (lob.getCwaAmount() > 0) {
				financialActivity.setFinActivityGrossAmt(lob.getCwaAmount());  //NBA093
			} else {
				financialActivity.setFinActivityGrossAmt(lob.getCheckAmount());  //NBA093
			}
			financialActivity.setUserCode(getUser().getUserID().substring(3, 8));  //NBA093
			//NBA093 deleted 4 lines
			//begin NBA115
			//create FinancialActivityExtension.CreditCardTransID for credit card payments
			// NBA173 code deleted 
			/*OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
			FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();*///commented for APSL5164
			finactext.setCreateDateTime(new NbaTime(NbaUtils.getDateFromStringInAWDFormat(lob.getCreateDate())));// APSL3709
			finactext.setCreditCardTransID(lob.getCCTransactionId());
			finactext.setPrevTaxYrInd(lob.getPreviousTaxYear()); //NBA173
			//Begin APSL2735
			if(!NbaUtils.isBlankOrNull(lob.getPaymentRefNumber())){
				financialActivity.setReferenceNo(lob.getPaymentRefNumber());
			}
			if(!NbaUtils.isBlankOrNull(lob.getPaymentConfNumber())){
				finactext.setConfirmationID(lob.getPaymentConfNumber());
			}
			//End APSL2735
			//Begin NBA228
			NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
			//AXAL3.7.26 code deleted.
			Payment payment = null;
			if (financialActivity.getPaymentCount() > 0) {
				payment = financialActivity.getPaymentAt(0);
				payment.setActionUpdate();
			} else {
				payment = new Payment();
				payment.setActionAdd();
				financialActivity.addPayment(payment);
			}
			payment.setPaymentForm(lob.getPaymentMoneySource());
			//AXAL3.7.26 begin
			if(sourceType != null && sourceType.equals(NbaConstants.A_ST_XML508)){
				NbaTXLife nbaTXlife = new NbaTXLife(nbaSource.getText());
				payment.setSourceOfFundsDetails(nbaTXlife.getOLifE().getSourceInfo().getSourceInfoDescription());//AXAL3.7.26
				finactext.setLocationID(NbaConstants.LOCATION_ID_084);
			}
			else{
				finactext.setLocationID(NbaUtils.getLocationFromScanStation(nbaSource.getNbaLob().getCreateStation()));
			}//AXAL3.7.26 end
			//End NBA228
			finactext.setCheckScanDate(nbaSource.getCreateDate());//QC#8467 APSL1995
			finactext.setReceivedPdrind(nbaSource.getNbaLob().getReceivedPDRInd());//APSL4967
			finactext.setActionAdd();
			financialActivity.addOLifEExtension(oExt);
			// NBA173 code deleted
			//end NBA115
			financialActivity.getActionIndicator().setAdd();
			return financialActivity;
		} catch (Exception e) {
			addComment("Error while generating Financial Activity object");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));//NBA298
			getLogger().logError("Apply Money: " + e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * This abstract method must be implemented by each subclass in order to
	 * execute the automated process.
	 * @param user the user/process for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		//NBA095 code deleted
		// NBA027 - logging code deleted
		// APSL2560 QC#10007 Retrieve perent case Company if CWA indexed with different company
		setWork(work);
		setUser(user);
		String cwaCompany = work.getNbaLob().getCompany(); //taking back up of CWA WI company code
		NbaDst parentWI = retrieveParentCaseOnly();
		if (parentWI != null && parentWI.getNbaLob().getCompany() != null)
			work.getNbaLob().setCompany(parentWI.getNbaLob().getCompany());
		if (!initialize(user, work)) {
			return getResult(); //NBA050
		}
		work.getNbaLob().setCompany(cwaCompany); // reset CWA WI company code with back up company code
		//NBA213 deleted code
		//retrieve the complete work item
		//Start NBLXA-1250 -Moved code at the top 
		//Begin APSL3836
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);//SPR1876
		retOpt.requestSources();
		retOpt.setLockWorkItem();
		retOpt.setLockSiblingTransaction();//ALII1394
		//begin SPR1876
		retOpt.requestCaseAsParent();
		retOpt.requestTransactionAsSibling(); //ALPC234
		retOpt.setLockParentCase();
		NbaDst parentCase = retrieveWorkItem(user, retOpt); //NBA213
		List transactions = parentCase.getTransactions();
		for (int i = 0; i < transactions.size(); i++) {
			//NBA208-32
			WorkItem transaction = (WorkItem) transactions.get(i);
			if (getWork().getID().equals(transaction.getItemID())) {
				getWork().getTransaction().getSourceChildren().addAll(transaction.getSourceChildren());
			}
		}
		// End NBLXA-1250
			
		if(NbaUtils.isInitialPremiumPaymentForm(nbaTxLife)){
			//Start NBLXA-1250
			boolean paperCheck = false;
			List allSources = getWork().getNbaSources();
			if (allSources != null && allSources.size() > 0) {
				NbaSource aSource = (NbaSource) getWork().getNbaSources().get(0);
				if (aSource.isCheckSource() && aSource.getNbaLob().getPaymentMoneySource() != String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT)) {
					paperCheck = true;
				}
			}
			// End NBLXA-1250
			if (!paperCheck) {
				if (!nbaTxLife.isUnderwriterApproved()) {
					if (nbaTxLife.getPolicy() != null && nbaTxLife.getPolicy().getApplicationInfo() != null
							&& nbaTxLife.getPolicy().getApplicationInfo().getCWAAmt() > 0) {
						addComment("Initial payment already present on the case. Possible duplicate payment");
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
						changeStatus(getResult().getStatus());
						doUpdateWorkItem();
						unlockWork(getUser(), parentWI);
						return getResult();
					}
				} else {
					if (!NbaUtils.is1035Case(nbaTxLife) && nbaTxLife.getPolicy() != null
							&& (NbaUtils.getPolicydeliveryInstrutionAmt(nbaTxLife.getPolicy()) == 0)
							&& nbaTxLife.getPolicy().getApplicationInfo().getCWAAmt() > 0) {// APSL4240
						addComment(" Case is CIPE. No money due on the policy. Possible duplicate payment received.");
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
						changeStatus(getResult().getStatus());
						doUpdateWorkItem();
						unlockWork(getUser(), parentWI);
						return getResult();
					}
				}
			}
		}
		//End APSL3836
		
		//end SPR1876
		//begin NBA115
		NbaLob workLob = work.getNbaLob();
		if (null != workLob.getCCTransactionId()) {
			boolean found = false;
			String payType = String.valueOf(workLob.getPendPaymentType());
			NbaTableAccessor nta = new NbaTableAccessor();
			NbaTableData[] finActivity = nta.getDisplayData(parentCase, NbaTableConstants.OLI_LU_FINACTTYPE);
			for (int i = 0; i < finActivity.length; i++) {
				if (finActivity[i].code().equals(payType)) {
					found = true;
					break;
				}
			}
			if (!found) {
				changeStatus(getHostErrorStatus()); //SPR3083
				addComment("Invalid payment type" + payType + "for the back end system" + workLob.getBackendSystem());
				doUpdateWorkItem();
				unlockWork(getUser(), parentCase); //SPR3123, NBA213
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getHostErrorStatus(), getHostErrorStatus())); //SPR3083
				return getResult();
			}
		}
		//end NBA115

		// NBA050 BEGIN
		//Create financial activity object
		boolean doesApplyMoney = false;// APSL3709
		// begin NBA077
		if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == getWork().getNbaLob().getInforcePaymentType()) {
			parentCase.getNbaLob().setInforcePaymentType(getWork().getNbaLob().getInforcePaymentType());
			setFinActivity(createFinancialActivityForReinstatement());
		} else {
			// end NBA077
			// APSL3709
			List financlActList = nbaTxLife.getPrimaryHolding().getPolicy().getFinancialActivity();
			doesApplyMoney = !(NbaUtils.isDuplicateFinancialActivity(getWork().getNbaLob().getCreateDate(), financlActList));
			if (doesApplyMoney) {
				setFinActivity(createFinancialActivityObject());
			}
			// APSL3709
		} //NBA077
		if (getFinActivity() != null) {
			nbaTxLife.getPrimaryHolding().getPolicy().addFinancialActivity(getFinActivity());
			Date origPolicyEffDate = nbaTxLife.getPrimaryHolding().getPolicy().getEffDate();
			new NbaContractValidation().validate(nbaTxLife, getWork(), user); //APSL395
			//Effective date after validation run
			Date policyEffDate = nbaTxLife.getPrimaryHolding().getPolicy().getEffDate();//NBLXA-222 Print Preview Phase ll
			determineRePrint(origPolicyEffDate,policyEffDate);//NBLXA-222 Print Preview Phase ll
			createPrintWorkItem(parentCase); //ALPC234
		}

		// NBA050 END
		//APSL395 Moved Code below AXAWS Call
		// Update AppliedIndicator
		if (!(NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == getWork().getNbaLob().getInforcePaymentType())) { // NBA077
			setAppliedIndicator();
			// Begin AXAL3.7.22
			if (doesApplyMoney && NbaUtils.isCompensationCallEnabled()) { // APSL3077 // APSL3709 //APSL5168 code deleted.
				if (getResult() == null) {
					boolean eligibleForExpressCommission = AxaUtils.getAgentsEligibilityForExpressCommission(nbaTxLife, parentCase);// ALS4633
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPolicy().getApplicationInfo());// APSL3822
					//Start APSL5232
					long activityType;
					if (nbaTxLife.getPolicy().getApplicationInfo().getCWAAmt() > 0) {
						activityType = NbaOliConstants.OLI_ACTTYPE_ECS_CALL;
					} else {
						activityType = NbaOliConstants.OLI_ACTTYPE_TEMP_ECS_CALL;
					}			
					// end APSL5232
					if (eligibleForExpressCommission
							&& (appInfoExt != null && appInfoExt.getUnderwritingApproval() != NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED)
							&& !NbaUtils.hasECSTriggered(nbaTxLife.getOLifE().getActivity(), activityType)) {// APSL3822, APSL5232
						AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ECSSUBMIT, this.user,
								nbaTxLife, work, null); // SR494086.7 ADC Retrofit
						webServiceInvoker.execute();
						NbaUtils.addECSActivity(nbaTxLife, user.getUserID(), activityType); //APSL5232
					}
				}
			}
			// End AXAL3.7.22
		} // NBA077
		receiveMoneyRequirements(parentCase);//ALII1394
		//Begin APSL395
		//Begin APSL3836
		updatePolicyDeliveryInstructionAmt(getWork().getNbaLob());
		//End APSL3836
		setNbaTxLife(doContractUpdate()); //SPR1876
		handleHostResponse(getNbaTxLife()); //SPR1876
		//if there is a host error, change the status to HOSTERRD
		if (getResult() != null) {
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			unlockWorkItems(parentCase);//SPR3123, NBA213,P2AXAL018
			return getResult();
		}
		//End APSL395
		//Begin SPR1876
		parentCase.updateLobFromNbaTxLife(getNbaTxLife());
		parentCase.setUpdate();
		updateWork(getUser(), parentCase); //NBA213
		//End SPR1876
		//		The status needs to be changed to CWAAPPLID
		if (getResult() == null) {
			//Begin NBA298
			boolean mecError = false;
			if (NbaOliConstants.OLI_LINEBUS_LIFE == nbaTxLife.getPrimaryHolding().getPolicy().getLineOfBusiness()) {
				LifeUSA lifeUSA = ((Life) nbaTxLife.getPrimaryHolding().getPolicy()
						.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).getLifeUSA();
				if (lifeUSA != null && lifeUSA.getMECInd()) {
					LifeUSAExtension lifeUSAExt = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
					if (lifeUSAExt != null && lifeUSAExt.getMECReason() == NbaOliConstants.OLI_TC_NULL) {
						mecError = true;
					}
				}
			}

			if (getOtherStatus() != CONTRACT_DELIMITER && mecError) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getOtherStatus()));
			} else {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
		}
		//End NBA298

		//Change the status
		changeStatus(getResult().getStatus());
		//SPR1876 code deleted
		doUpdateWorkItem();
		//NBA213 deleted code
		commit(parentCase); // NBLXA-236New Method :: Its used for Unsuspend the PDC req.
		unlockWorkItems(parentCase);//SPR1876, NBA213,P2AXAL018
		return getResult();
	}
	
	/**
	 * Retrieve info about check
	 * 
	 * @return com.csc.fsg.nba.tableaccess.NbaCheckData
	 */
	protected NbaCheckData getCheckData() {
		try {
			NbaLob lob = getWork().getNbaLob();
			NbaCashieringTable table = new NbaCashieringTable();
			NbaContractCheckData contractCheck =
				table.getContractCheckData(
					lob.getBundleNumber(),
					lob.getCompany(),
					lob.getPolicyNumber(),
					lob.getCheckAmount(),
					lob.getCheckNumber(),
					lob.getCwaAmount());
			//NbaContractCheckData contractCheck = table.getContractCheckData("0000000123" , lob.getCompany(), lob.getPolicyNumber());		
			return (table.getCheckData(contractCheck.getTechnicalKey()));
		} catch (NbaBaseException nbe) {
			getLogger().logError("Apply Money get check data failed: " + nbe.getMessage());
			return null;
		} catch (Exception e) {
			getLogger().logError("Apply Money get check data failed: " + e.getMessage());
			return null;
		}
	}
	/**
	 * Retrieve the credit card source if it exists.
	 * @return NbaSource
	 */
	//New Method NBA115
	protected NbaSource getCreditCardSource() {
		NbaSource source = null;
		if (getWork().getNbaSources().size() > 0) {
			source = (NbaSource) getWork().getNbaSources().get(0);
			if(source.isCreditCard()) {
				return source;
			}
		}
		return source;
	}
	/**
	 * This method sets the applied indicator for credit card payments.
	 */
	//New Method NBA115
	protected void setCreditCardAppliedIndicator() {
		try {
			NbaCreditCardData.updateCCAppliedPayment(getWork().getNbaLob().getCCTransactionId());					
		} catch (NbaBaseException nbe) {
			addComment("Error updating the credit card applied indicator(" + nbe.getMessage() + ")");
			changeStatus(getSqlErrorStatus());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Credit Card SQL Error", getSqlErrorStatus()));
		}

	}	
	/**
	 * This method returns the type of source that is attached to the work item
	 * @return java.lang.String Source type
	 */
	protected String getSourceType() {
		if (getWork().getNbaSources().size() > 0) {
			NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
			return nbaSource.getSource().getSourceType();
		} else {
			return null;
		}
	}
	/**
	 * Depending on whether attached source is a check or a wire tranfer, this method 
	 * will call the appropriate method
	 */
	protected void setAppliedIndicator() {
		String sourceType = getSourceType();
		if(sourceType == null || sourceType.equals(NbaConstants.A_ST_CREDIT_CARD)){//NBA115
			setCreditCardAppliedIndicator();//NBA115
		} else if (sourceType.equals(NbaConstants.A_ST_CWA_CHECK) || sourceType.equals(NbaConstants.A_ST_PAYMENT)) { //NBA115
			updateContractToCheck();
		} else if (sourceType.equals(NbaConstants.A_ST_XML508)) {
			updateWireTransfer();
		}
	}
	/**
	 * This will see if the check is marked as deposited in the Check tablle. 
	 * If yes, write an error comment in AWD and send the work item to error queue
	 * If no, set the Applied Indicator to true in the contratcs_checks table. 
	 */
	protected void updateContractToCheck() {
		NbaCheckData check = getCheckData();
		if (check == null) {
			addComment("Apply Money failed: Check information not found in database");
			setResult(
				new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering SQL Error", getSqlErrorStatus()));
			return;
		}
		String dbYear=null;
		if (check.getDepositTimeStamp() != null) {
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		    dbYear = df.format(check.getDepositTimeStamp());
		}
		if (check.getDepositTimeStamp() == null || (dbYear!=null && dbYear.endsWith("1900"))) { //APSL5289
			try {
				NbaLob lob = getWork().getNbaLob();
				NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);		//APSL341
				//Set the values from LOB
				//NbaContractCheckData contractCheckData = new NbaContractCheckData();
				NbaContractCheckData.updateAppliedInd(
					true,
					true,
					lob.getBundleNumber(),
					lob.getCompany(),
					lob.getPolicyNumber(),
					lob.getCheckAmount(),
					lob.getCheckNumber(),
					lob.getCwaAmount()); // SR615900
				//Start APSL5289
				boolean setDepositTimeStamp = false;
				if (NbaContractCheckData.getRowCountForTechKey(check.getTechnicalKey()) > 0) {
					setDepositTimeStamp = true;
				}

				if (((setDepositTimeStamp && check.getDepositTimeStamp() == null) || (!setDepositTimeStamp && check.getDepositTimeStamp() != null))) {
					NbaCheckData.updateDepositeTime(lob.getBundleNumber(), lob.getCheckAmount(), lob.getCwaDate(), lob.getCheckNumber(), nbaSource
							.getSource().getCreateStation(), nbaSource.getCreateDate(), setDepositTimeStamp);
				}
				//End APSL5289
				NbaCheckData.updateIncludeInd(
					true, lob.getBundleNumber(), lob.getCheckAmount(), lob.getCwaDate(),
					lob.getCheckNumber(), nbaSource.getSource().getCreateStation(), nbaSource.getCreateDate()); 		//APSL341
			} catch (NbaBaseException nbe) {
				addComment(
					"Apply Money failed: Database error while updating the check's applied indicator("
						+ nbe.getMessage()
						+ ")");
				changeStatus(getSqlErrorStatus());
				setResult(
					new NbaAutomatedProcessResult(
						NbaAutomatedProcessResult.FAILED,
						"Cashiering SQL Error",
						getSqlErrorStatus()));
			}
		} else {
			addComment("Apply Money failed: Check has already been deposited, the Applied indicator cannot be changed");
			try {
				//Read the value from the configuration to send the workitemt o error queue
				String depositIndicator = NbaConfiguration.getInstance().getCashiering().getDepositEligibility(); //ACN012
				if (depositIndicator != null && depositIndicator.trim().equals(DEPOSIT_INDICATOR_APPLIED)) {
					setResult(
						new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering Error", getFailStatus()));
				}
			} catch (NbaBaseException e) {
				addComment("Apply Money failed: Error while reading deposit indicator(" + e.getMessage() + ")");
				changeStatus(getFailStatus());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering Error", getFailStatus()));
			}
		}
	}
	/**
	 * This method updates Applied Indicator in the Wire transfer table
	 */
	protected void updateWireTransfer() {
		try {
			NbaLob lob = getWork().getNbaLob();
			NbaSource nbaSource = (NbaSource) getWork().getNbaSources().get(0);
			NbaTXLife nbaTXlife = new NbaTXLife(nbaSource.getText());
			java.util.Date effDate =
				nbaTXlife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransExeDate();
			//Set the values from LOB	
			NbaContractsWireTransferData.updateAppliedInd(true, effDate, lob.getCompany(), lob.getPolicyNumber());
		} catch (NbaBaseException nbe) {
			addComment("Apply Money failed: Database error while updating the Applied indicator(" + nbe.getMessage() + ")");
			changeStatus(getSqlErrorStatus());
			setResult(
				new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Cashiering SQL Error", getSqlErrorStatus()));
		} catch (Exception e) {
			addComment("Invalid XML");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));//NBA298
		}
	}
	/**
	 * Returns the finActivity.
	 * @return FinancialActivity
	 */
	// NBA050 NEW METHOD
	public FinancialActivity getFinActivity() {
		return finActivity;
	}

	/**
	 * Sets the finActivity.
	 * @param finActivity The finActivity to set
	 */
	// NBA050 NEW METHOD
	public void setFinActivity(FinancialActivity finActivity) {
		this.finActivity = finActivity;
	}

	/**
	 * This method returns the NBPAYMENT Source attached to a NBPAYMENT workitem
	 * @return nbaSource NbaSource object
	 */
	// NBA077 new method
	protected NbaSource getSource() {
		if (NbaConstants.A_WT_PAYMENT.equals(getWork().getNbaLob().getWorkType())) {
			List sources = getWork().getNbaSources();
			for (int i = 0; i < sources.size(); i++) {
				NbaSource source = (NbaSource) sources.get(i);
				if (NbaConstants.A_ST_PAYMENT.equals(source.getSource().getSourceType())) {
					return source;
				}
			}
		}
		return null;
	}

	/**
	 * This method constructs FinancialActivity object to be added to the xml203 for Reinstatement Payments
	 * @return FinancialActivity An instance of FinancialActivity object
	 */
	// NBA077 new method
	protected FinancialActivity createFinancialActivityForReinstatement() throws NbaBaseException {
		// SPR2248 code deleted
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
		NbaLob lob = getWork().getNbaLob();
		//Create Financial Activity Object
		FinancialActivity financialActivity = new FinancialActivity();
		nbaOLifEId.setId(financialActivity);
		financialActivity.setDataRep(NbaOliConstants.DATAREP_TYPES_FULL);
		// SPR2248 code deleted
		financialActivity.setFinActivityDate(lob.getInforcePaymentDate());	//SPR2248
		financialActivity.setFinEffDate(lob.getInforcePaymentDate());	//SPR2248
		// SPR2248 code deleted
		financialActivity.setFinActivityType(NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN);
		financialActivity.setFinActivityGrossAmt(lob.getCheckAmount());
		financialActivity.setUserCode(getUser().getUserID().substring(3, 8));
		financialActivity.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY));
		FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
		//Begin APSL2735
		if(!NbaUtils.isBlankOrNull(lob.getPaymentRefNumber())){
			financialActivity.setReferenceNo(lob.getPaymentRefNumber());
		}
		if(!NbaUtils.isBlankOrNull(lob.getPaymentConfNumber())){
			finActExt.setConfirmationID(lob.getPaymentConfNumber());
		}
		//End APSL2735
		finActExt.setDisbursedInd(false);
		finActExt.setAcctgExtractInd(false);
		financialActivity.getActionIndicator().setAdd();
		return financialActivity;
	}
	/*
	 * Invoke VPMS to create new Print Work Item if necessary.
	 */
	//ALPC234 New Method
	private void createPrintWorkItem(NbaDst parentCase) {
		
		Map deOink = new HashMap();
		try {
			NbaTransaction origPrint = getOriginalPrint(parentCase);
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
			if (provider != null && provider.getWorkType() != null && provider.getInitialStatus() != null) {
				NbaTransaction newPrintTrans = parentCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
				copyPrintLobs(newPrintTrans,origPrint);
				//ALS5556 code deleted, moved to Print
			}
		} catch (NbaBaseException nbe) {
			addComment("Error creating (Re)Print Work Item for Unbound processing");
			getLogger().logError("Apply Money: " + nbe.getMessage());
		}
	}
	/*
	 * copy LOBS from original print to reprint work item
	 */
	//ALPC234 New Method
	private void copyPrintLobs(NbaTransaction newPrintTrans,NbaTransaction origPrint) throws NbaBaseException{
		
		if (!(null == origPrint)) {
			NbaLob workNbaLob = origPrint.getNbaLob(); 
			NbaLob tempTransNbaLob = newPrintTrans.getNbaLob();
			tempTransNbaLob.setPolicyNumber(workNbaLob.getPolicyNumber());
			tempTransNbaLob.setSsnTin(workNbaLob.getSsnTin());
			tempTransNbaLob.setTaxIdType(workNbaLob.getTaxIdType());
			tempTransNbaLob.setLastName(workNbaLob.getLastName());
			tempTransNbaLob.setFirstName(workNbaLob.getFirstName());
			tempTransNbaLob.setMiddleInitial(workNbaLob.getMiddleInitial());
			tempTransNbaLob.setCompany(workNbaLob.getCompany());
			tempTransNbaLob.setReview(workNbaLob.getReview());
			tempTransNbaLob.setAppDate(workNbaLob.getAppDate());
			tempTransNbaLob.setAppState(workNbaLob.getAppState());
			tempTransNbaLob.setIssueOthrApplied(workNbaLob.getIssueOthrApplied());
			tempTransNbaLob.setExtractComp(workNbaLob.getExtractComp());
			tempTransNbaLob.setPrintExtract(NbaConstants.UNBOUND_EXTRACT);	//APSL5055 ALS5566
			tempTransNbaLob.setDistChannel(String.valueOf(workNbaLob.getDistChannel())); 
			tempTransNbaLob.setSpecialCase(workNbaLob.getSpecialCase());
			tempTransNbaLob.setPaidChgCMQueue(workNbaLob.getPaidChgCMQueue());// Start NBLXA-1308
			tempTransNbaLob.setCaseManagerQueue(workNbaLob.getCaseManagerQueue());
			tempTransNbaLob.setUndwrtQueue(workNbaLob.getUndwrtQueue());
			tempTransNbaLob.setSpecialCase(workNbaLob.getSpecialCase()); //End NBLXA-1308
			tempTransNbaLob.setReplacementIndicator(workNbaLob.getReplacementIndicator()); 
			tempTransNbaLob.setExchangeReplace(workNbaLob.getExchangeReplace()); 
			tempTransNbaLob.setReplacementIndicator(workNbaLob.getReplacementIndicator());
			tempTransNbaLob.setExchangeReplace(workNbaLob.getExchangeReplace());//ALS5718
			newPrintTrans.setUpdate();
		}
	}
	/*
	 * Find the original print work item
	 */
	//ALPC234 New Method	
	private NbaTransaction getOriginalPrint(NbaDst parentCase) throws NbaBaseException {
		Iterator transactionItr = parentCase.getNbaTransactions().listIterator();
			NbaTransaction printTrans;
			//Loop over transactions.  If a new print work item exists, then set boolean to true;
			while (transactionItr.hasNext()) {
				printTrans = (NbaTransaction) transactionItr.next();
				if (A_WT_CONT_PRINT_EXTRACT.equals(printTrans.getWorkType())) {
					return printTrans;
				}
			}
		return null;
	}

//Begin P2AXAL018
	/**
	 * @return Returns the unsuspendList.
	 */
	public List getUnsuspendList() {
	    if (unsuspendList == null) {
	        unsuspendList = new ArrayList();
	    }
	    return unsuspendList;
	}
	/**
	 * Unlocks the workitems in the work flow system.
	 * @param parentCase
	 * @throws NbaBaseException
	 */
//	NBA192 New Method
	protected void unlockWorkItems(NbaDst parentCase) throws NbaBaseException {
	    NbaContractLock.removeLock(getUser());
	    if (parentCase != null) {
	        unlockWork(parentCase);
	    }  
	}
	//End P2AXAL018
	
	//ALII1394 New Method 
	protected void receiveMoneyRequirements(NbaDst parentCase) throws NbaBaseException {
	    if (NbaConstants.A_WT_CWA.equalsIgnoreCase(getWork().getWorkType())) {
	        List moneyRequirements = getMoneyRequirements();
	        int size = moneyRequirements.size();
	        if (size > 0) {
	            List requirements = parentCase.getNbaTransactions();
	            int reqCount = requirements.size();
	            NbaTransaction nbaTransaction = null;
	            String moneyReqType = null;
	            for (int i = 0; i < reqCount; i++) {
	                nbaTransaction = (NbaTransaction) requirements.get(i);
	                if (NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(nbaTransaction.getWorkType())) {
	                    NbaLob lob = nbaTransaction.getNbaLob();
	                    long reqStatus = NbaUtils.convertStringToLong(lob.getReqStatus());
	                    for (int j = 0; j < size; j++) {
	                        moneyReqType = (String) moneyRequirements.get(j);
	                        if (lob.getReqType() == NbaUtils.convertStringToInt(moneyReqType)
	                                && (reqStatus != NbaOliConstants.OLI_REQSTAT_RECEIVED || reqStatus != NbaOliConstants.OLI_REQSTAT_WAIVED)) {
	                            processMoneyRequirement(nbaTransaction);
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	    }
	}
	
	
	//ALII1394 New Method
	protected List getMoneyRequirements() throws NbaBaseException {
	    List moneyRequirements = new ArrayList();
	    NbaVpmsModelResult data = new NbaVpmsModelResult(getDataFromVpms(NbaVpmsConstants.REQUIREMENTS, NbaVpmsConstants.EP_GET_MONEY_REQ_TYPES,
	            new NbaOinkDataAccess(getWorkLobs()), null, null).getResult());
	    if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
	        ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
	        int resultSize = resultData.getResult().size();
	        for (int i = 0; i < resultSize; i++) {
	            moneyRequirements.add(resultData.getResultAt(i));
	        }
	    }
	    return moneyRequirements;
	}
	
	//ALII1394 New Method
	protected void processMoneyRequirement(NbaTransaction nbaTransaction) throws NbaBaseException {
		List moneyRequirementQueues = getMoneyRequirementQueues();
		int size = moneyRequirementQueues.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				if (nbaTransaction.getQueue().equalsIgnoreCase((String) moneyRequirementQueues.get(i))) {
					NbaLob lob = nbaTransaction.getNbaLob();
					if (NbaOliConstants.OLI_REQCODE_PREMDUE == lob.getReqType() && !(isTotalCWAAmtValid())) {//AXAL3.7.15
						break;//AXAL3.7.15
					}//AXAL3.7.15
					/*Begin APSL351
					 * If the case was negatively disposed. Requirements were sent to END. 
					 * case was reopened and reapproved. Premium Due did not generate a 2nd. time. 1st Premium Due is still sitting in the END. 
					 * NBCWA comes in and goes through Money Und- this is process that will receipt the Premium Due (if it one of 3 active queues). This did not occur as there was no active Prem Due. 
					 * added END Queue in the list of Money requirements queue to process Premium due carrier.
					 */
					boolean reqFlag = true;
					if (NbaOliConstants.OLI_REQCODE_PREMDUE == lob.getReqType()
							&& NbaConstants.A_STATUS_NEG_DISPOSED.equalsIgnoreCase(lob.getStatus())
							&& NbaConstants.END_QUEUE.equalsIgnoreCase(lob.getQueue())) {
						reqFlag = true;
					} else {
						if (NbaConstants.END_QUEUE.equalsIgnoreCase(lob.getQueue()) && NbaOliConstants.OLI_REQCODE_PREMDUE != lob.getReqType()) {
							reqFlag = false;
						}
					}//End APSL351
					if (reqFlag) { //APSL351 added if
						lob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
						lob.setReqReceiptDate(Calendar.getInstance().getTime());
						lob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(Calendar.getInstance().getTime()));//QC20240
						RequirementInfo aReqInfo = nbaTxLife.getRequirementInfo(lob.getReqUniqueID());
						aReqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED);
						aReqInfo.setReceivedDate(lob.getReqReceiptDate());
						aReqInfo.setStatusDate(lob.getReqReceiptDate());
						aReqInfo.setFulfilledDate(lob.getReqReceiptDate());
						aReqInfo.setActionUpdate();
						// Begin AXAL3.7.01	
						if (aReqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PREMDUE) {
							RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
							if (requirementInfoExt == null) {
								OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
								requirementInfoExt = olifeExt.getRequirementInfoExtension();
							}
							requirementInfoExt.setPremiumDueCarrierReceiptDate(lob.getReqReceiptDate());
							requirementInfoExt.setReceivedDateTime(lob.getReqReceiptDateTime());//QC20240
							requirementInfoExt.setActionUpdate();
						}
						//End AXAL3.7.01
						NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), lob);
						nbaTransaction.setStatus(provider.getPassStatus());
						nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
						if (nbaTransaction.isSuspended()) {
							NbaSuspendVO suspendVO = new NbaSuspendVO();
							suspendVO.setTransactionID(nbaTransaction.getID());
							getUnsuspendList().add(suspendVO);
						}
						break;
					}// APSL351 End if
				}
			}
		}
	}
	
	//New Method APSL3836
	protected void updatePolicyDeliveryInstructionAmt(NbaLob cwaLob) throws NbaBaseException{
		double cwaAmount = cwaLob.getCwaAmount();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		if(policyExtension!=null){
			double newCwaAmount = policyExtension.getPolicyDeliveryInstructionAmt()-cwaAmount;
			if(newCwaAmount < 0)
			{
				policyExtension.setPolicyDeliveryInstructionAmt(0);
			}else{
				policyExtension.setPolicyDeliveryInstructionAmt(newCwaAmount);
			}
			policyExtension.setActionUpdate();
		}
	}
	
	// NBLXA-222 Print Preview Phase ll
	/*
	 * if unbound..and reprint is false set reprint ind to true when new pol eff date not equal current eff date
	 */
	protected void determineRePrint(Date origPolicyEffDate, Date effDate) {
		PolicyExtension polExt = NbaUtils.getPolicyExtension(nbaTxLife.getPrimaryHolding().getPolicy());
		ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo());
		if ((polExt != null && polExt.getContractChangeReprintInd()) || null == origPolicyEffDate) {
			return;
		}
		if (polExt != null && !polExt.getUnboundInd()) {
			return;
		}

		if (appInfoExt != null && !appInfoExt.hasContractPrintExtractDate()) {
			return;
		}

		if (polExt != null && !(origPolicyEffDate.compareTo(effDate) == 0)) {
			polExt.setContractChangeReprintInd(true);
			polExt.setActionUpdate();
		}
	}
	/**
	 * Commit the changes to the work flow system.
	 * @throws NbaBaseException
	 */
	// NBLXA-236New Method
	protected void commit(NbaDst parentCase) throws NbaBaseException {
	    if (parentCase != null) {
	        int size = getUnsuspendList().size();
	        for (int i = 0; i < size; i++) {
	            unsuspendWork((NbaSuspendVO) getUnsuspendList().get(i));
	        }
	    }
	    updateWork();
	}
	
}
