package com.csc.fsg.nba.bean.accessors;
/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.ejb.SessionBean;

import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.vo.NbaCWAReverseRefundVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Loan;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
/**
 * Business model for the CWA Reverse Refund Business function. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA037</td><td>Version 3</td><td>CWA Add, Reversal & Refund</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA069</td><td>Version 3</td><td>Cashiering Enhancement</td></tr>
 * <tr><td>NBA066</td><td>Version 3</td><td>nbA Accounting and Disbursements extracts</td></tr>
 * <tr><td>SPR1629</td><td>Version 4</td><td>Transaction Validation</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1656</td><td>Version 4</td><td>Allow for Refund/Reversal Extracts</td></tr>
 * <tr><td>SPR1841</td><td>Version 4</td><td>Wrong Entry Date and no As of Date in CWA and in reversal of CWA</td></tr>
 * <tr><td>SPR1556</td><td>Version 4</td><td>Able to refund the payment more than once</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR1876</td><td>Version 4</td><td>CWA Validation P001 does not calculate total CWA amount correctly</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit Card Payments Reversal and Refunds</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA169</td><td>Version 6</td><td>CWA Rewrite</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.12</td><td>Axa Life Phase 1</td><td>Cash Management</td></tr>
 * <tr><td>AXAL3.7.23</td><td>Axa Life Phase 1</td><td>Accounting Interface</td></tr>
 * <tr><td>SPR1959</td><td>Version 8</td><td>Partial Refund Needs to Create Reversal Entry for Original</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaCWAReverseRefundFacadeBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx;
	protected static NbaLogger logger = null;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {}
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCWAReverseRefundFacadeBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaCWAReverseRefundFacadeBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}	
	/**
	 * Creates value objects representing CWA information.
	 * @param An instance of <code>NbaUserVO</code>
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @return ArrayList A list of <code>NbaCWAReverseRefundVO</code> objects to be returned.
	 */
	//NBA103
	public ArrayList loadCWAInfo(NbaTXLife aNbaTXLife) throws NbaBaseException {
		ArrayList cwaObjects = new ArrayList();

		try {//NBA103
			Policy policy = aNbaTXLife.getPrimaryHolding().getPolicy();
			int finactCount = policy.getFinancialActivityCount();
			for (int i=0; i < finactCount; i++) {
				FinancialActivity finact = policy.getFinancialActivityAt(i);
				//begin NBA093
				NbaCWAReverseRefundVO vo = new NbaCWAReverseRefundVO();
				//set the FinancialActivity fields
				vo.setFinActivityID(finact.getId());
				vo.setFinActivitySubType(finact.getFinActivitySubType());
				//Begin AXAL3.7.12
				if (finact.getPaymentCount() > 0) {
					vo.setPaymentSource(finact.getPaymentAt(0).getPaymentForm());
				}
				//End AXAL3.7.12
				vo.setFinEffDate(finact.getFinEffDate());
				vo.setOrderSource(finact.getOrderSource()); // NBLXA-1356
				vo.setReferenceNo(finact.getReferenceNo());
				vo.setFinActivityType(finact.getFinActivityType());
				vo.setFinActivityGrossAmt(finact.getFinActivityGrossAmt());
				vo.setFinActivityDate(finact.getFinActivityDate());
				vo.setUserCode(finact.getUserCode());
				vo.setCostBasis(finact.getCostBasis());//NBA169
				vo.setRollOverIntAmount(finact.getRollloverIntAmt());//NBA169 NBA223
				vo.setOldRecord(true);//NBA069
				if (finact.hasBestIntRateType()) {
					vo.setBestIntRateType(finact.getBestIntRateType());
				} else {
					vo.setBestIntRateType(0);
				}
				vo.setIntPostingRate(finact.getIntPostingRate());
				vo.setCostBasisAdjAmt(finact.getCostBasisAdjAmt());
				vo.setIntTreatmentInd(finact.getIntTreatmentInd());
				if (finact.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND) {
					vo.setPartialRefundAsOfDate(finact.getFinActivityDate());
				}
				
				// set the FinancialActivityExtension fields
				FinancialActivityExtension finactext = NbaUtils.getFirstFinancialActivityExtension(finact);
				if (finactext != null) {
					vo.setPartialRefundAmt(finactext.getPartialRefundAmt());
					vo.setDisbursedInd(finactext.getDisbursedInd());
					vo.setErrCorrInd(finactext.getErrCorrInd());
					vo.setPrevTaxYrInd(finactext.getPrevTaxYrInd());
					vo.setPreTEFRATAMRAInd(finactext.getPreTEFRATAMRAInd());
					vo.setCreditCardTransID(finactext.getCreditCardTransID()); //NBA115 
					vo.setActivityReasonCode(finactext.getActivityReasonCode()); //APSL4910
					vo.setRefPolicyNo(finactext.getReferencePolicyNo());
					//APSL5164 Starts 
					if(!NbaUtils.isBlankOrNull(finactext.getCipeDateTime()))
					{
					vo.setFinActivityTime(finactext.getCipeDateTime().substring(finactext.getCipeDateTime().indexOf(' ')+1));
					}
					//APSL5164 Ends
				}
				
				cwaObjects.add(vo);
				//end NBA093
			}
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
		
		return cwaObjects;
	}	
	/**
	 * Saves CWA details for a contract provided by the calling program. 
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param aNbaDst An instance of <code>NbaDst</code>
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @param cwaObjects An object containing CWAs.
	 * @return ArrayList A list of <code>NbaCWAReverseRefundVO</code> objects containing updated information
	 * @throws NbaBaseException
	 */
	//NBA103
	public ArrayList saveCWAInfo(NbaUserVO user, NbaDst aNbaDst, NbaTXLife aNbaTXLife, ArrayList cwaObjects)
		throws NbaBaseException {
		NbaCWAReverseRefundVO vo = null;

		//SPR1851 code deleted

		try {//NBA103
			Policy policy = aNbaTXLife.getPrimaryHolding().getPolicy();
			int finactCount = policy.getFinancialActivityCount();
			for (int i = 0; i < cwaObjects.size(); i++) {
				vo = (NbaCWAReverseRefundVO) cwaObjects.get(i);
				if (vo.isOldRecord() && vo.isActionUpdate()) {  //NBA069
					for (int j = 0; j < finactCount; j++) {
						FinancialActivity finact = policy.getFinancialActivityAt(j);
						if (finact.getId().compareTo(vo.getFinActivityID()) == 0) {
							switch ((int) vo.getFinActivitySubType()) { //NBA093
								case (int) NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND : //NBA093
									updatePartialRefund(user, vo, finact, aNbaTXLife, aNbaDst); //NBA115
									vo.setAction(NbaActionIndicator.ACTION_PARTIALREFUND_SUCCESSFUL);
									break;
								case (int) NbaOliConstants.OLI_FINACTSUB_REFUND : //NBA093
									updateRefundReversal(user, vo, finact, aNbaTXLife, aNbaDst); //NBA115
									vo.setAction(NbaActionIndicator.ACTION_REFUND_SUCCESSFUL);
									break;
								case (int) NbaOliConstants.OLI_FINACTSUB_REV : //NBA093
									updateRefundReversal(user, vo, finact, aNbaTXLife, aNbaDst); //NBA115
									vo.setAction(NbaActionIndicator.ACTION_REVERSE_SUCCESSFUL);
									break;
								default :
									updateFinancialActivityFromVO(vo, finact, NbaActionIndicator.ACTION_UPDATE);
									vo.setActionSuccessful();
									break;
							}
						}
					}
					//NBA069 Begin 
				} else if (!vo.isOldRecord()) {
					addFinancialActivity(vo, aNbaTXLife);
					if (vo.isActionAdd()) {
						vo.setAction(NbaActionIndicator.ACTION_ADD_SUCCESSFUL);
					} else {
						vo.setAction(NbaActionIndicator.ACTION_UPDATE_SUCCESSFUL);
					}
					vo.setOldRecord(true);
				}
				//NBA069 End
			}
			//NBA103 removed catch			
			aNbaTXLife.setBusinessProcess(NbaConstants.PROC_VIEW_CWA_REVERSE_REFUND); //NBA169
			aNbaTXLife = NbaContractAccess.doContractUpdate(aNbaTXLife, aNbaDst, user); //SPR1851
			//NBA103 removed debug code
			//Begin SPR1876
			//removed try-catch block
			aNbaDst.getNbaLob().updateLobFromNbaTxLife(aNbaTXLife);
			aNbaDst.setUpdate();
			WorkflowServiceHelper.update(user, aNbaDst); // SPR3290
			//End SPR1876
		return cwaObjects;
		} catch(NbaTransactionValidationException ntve){ //SPR1629//NBA103
		    throw ntve; //SPR1629//NBA103
		} catch (NbaBaseException e) {//NBA103
			setActionIndicatorsToFailed(cwaObjects);//NBA103
			getLogger().logError(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			setActionIndicatorsToFailed(cwaObjects);//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logError(e);//NBA103
			throw e;//NBA103
		}
		
	}
	
	//NBA103
	private void setActionIndicatorsToFailed(ArrayList cwaObjects) {
		NbaCWAReverseRefundVO vo;
		for (int i = 0; i < cwaObjects.size(); i++) {
			vo = (NbaCWAReverseRefundVO) cwaObjects.get(i);
			if (vo.isActionSuccessful()) {
				vo.setActionFailed();
			} else if (vo.isAction(NbaActionIndicator.ACTION_PARTIALREFUND_SUCCESSFUL)) {
				vo.setAction(NbaActionIndicator.ACTION_PARTIALREFUND_FAILED);
			} else if (vo.isAction(NbaActionIndicator.ACTION_REFUND_SUCCESSFUL)) {
				vo.setAction(NbaActionIndicator.ACTION_REFUND_FAILED);
			} else if (vo.isAction(NbaActionIndicator.ACTION_REVERSE_SUCCESSFUL)) {
				vo.setAction(NbaActionIndicator.ACTION_REVERSE_FAILED);
			}
		}
	}	
	/**
	 * Updates the <code>NbaCWAReverseRefundVO</code> instance data to FinancialActivity Object.
	 * @param vo
	 * @param finact
	 * @param action
	 */
	protected void updateFinancialActivityFromVO(NbaCWAReverseRefundVO vo, FinancialActivity finact, String action) {
		if (finact.getFinActivitySubType() != vo.getFinActivitySubType()) {  //NBA093
			finact.setFinActivitySubType(vo.getFinActivitySubType());  //NBA093
			finact.setAction(action);
		}
		//Begin SPR1841
		if (!finact.getFinActivityDate().equals(vo.getFinActivityDate())) { 
			finact.setFinActivityDate(vo.getFinActivityDate());
			finact.setAction(action);
		}
		//End SPR1841
		if (finact.getFinEffDate() == null || (finact.getFinEffDate() != null && !finact.getFinEffDate().equals(vo.getFinEffDate()))) {  //NBA093,NBA169
			finact.setFinEffDate(vo.getFinEffDate());  //NBA093
			finact.setAction(action);
		}
		if (finact.getFinActivityGrossAmt() != vo.getFinActivityGrossAmt()) {  //NBA093
			finact.setFinActivityGrossAmt(vo.getFinActivityGrossAmt());  //NBA093
			finact.setAction(action);
		}
		// begin NBA093
		if (finact.getBestIntRateType() != vo.getBestIntRateType()) {
			finact.setBestIntRateType(vo.getBestIntRateType());
			finact.setAction(action);
		}
		if (finact.getUserCode() != null && finact.getUserCode().compareTo(vo.getUserCode()) != 0) {
			finact.setUserCode(vo.getUserCode());
			finact.setAction(action);
		}
		if (finact.getIntPostingRate() != vo.getIntPostingRate()) {
			finact.setIntPostingRate(vo.getIntPostingRate());
			finact.setAction(action);
		}
		if (finact.getCostBasisAdjAmt() != vo.getCostBasisAdjAmt()) {
			finact.setCostBasisAdjAmt(vo.getCostBasisAdjAmt());
			finact.setAction(action);
		}
		if (finact.getIntTreatmentInd() != vo.isIntTreatmentInd()) {
			finact.setIntTreatmentInd(vo.isIntTreatmentInd());
			finact.setAction(action);
		}
		if (finact.getCostBasis() != vo.getCostBasis()) { //NBA169
			finact.setCostBasis(vo.getCostBasis()); //NBA169
			finact.setAction(action); //NBA169
		}
		// APSL4087-SR826148 Begin 
		if (finact.getFinActivityType() != vo.getFinActivityType()) {
			finact.setFinActivityType(vo.getFinActivityType());
			finact.setAction(action);
		}
		// APSL4087-SR826148 End
		//Begin AXAL3.7.12
		if (finact.getPaymentCount() > 0) {//check for existing payment, In the case of update of current CWA, or in case of clonned financial activity.
			if (NbaActionIndicator.ACTION_ADD.equals(action)) finact.getPaymentAt(0).deleteId();//In the case of reverse or refund
			if (finact.getPaymentAt(0).getPaymentForm() != vo.getPaymentSource()) {
				finact.getPaymentAt(0).setPaymentForm(vo.getPaymentSource());
				finact.getPaymentAt(0).setAction(action);
				finact.setAction(action);
			}
		} else {// handle for historical cases, those doesn't have a payment in a financial activity.
			Payment payment = new Payment();
			payment.setPaymentForm(vo.getPaymentSource());
			payment.setActionAdd();
			finact.addPayment(payment);
			finact.setAction(action);
		}		
		//End AXAL3.7.12
		
		
		boolean extExists = false;
		// end NBA093
		int oextCount = finact.getOLifEExtensionCount();
		for (int j=0; j < oextCount; j++) {
			OLifEExtension oExt = finact.getOLifEExtensionAt(j);
			if (oExt.isFinancialActivityExtension()) {
				extExists = true;  //NBA093
				FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
				// NBA093 deleted 3 lines
				if (finactext.getPartialRefundAmt() != vo.getPartialRefundAmt()) { //NBA093
					finactext.setPartialRefundAmt(vo.getPartialRefundAmt()); //NBA093
					finactext.setAction(action); //NBA093
				}
				// NBA093 code deleted
				if (finactext.getDisbursedInd() != vo.isDisbursedInd()) {  //NBA093
					finactext.setDisbursedInd(vo.isDisbursedInd());  //NBA093
					finactext.setAction(action);  //NBA093
				}
				if (finactext.getErrCorrInd() != vo.isErrCorrInd()) {  //NBA093
					finactext.setErrCorrInd(vo.isErrCorrInd());  //NBA093
					finactext.setAction(action);  //NBA093
				}
				if (finactext.getPrevTaxYrInd() != vo.isPrevTaxYrInd()) {  //NBA093
					finactext.setPrevTaxYrInd(vo.isPrevTaxYrInd());  //NBA093
					finactext.setAction(action);  //NBA093
				}
				if (finactext.getPreTEFRATAMRAInd() != vo.isPreTEFRATAMRAInd()) {  //NBA093
					finactext.setPreTEFRATAMRAInd(vo.isPreTEFRATAMRAInd());  //NBA093
					finactext.setAction(action);  //NBA093
				}
				// NBA093 code deleted
				// APSL4910 Started
				if (finactext.getActivityReasonCode() != vo.getActivityReasonCode()) { 
					finactext.setActivityReasonCode(vo.getActivityReasonCode());  
					finactext.setAction(action);  
				}
				if (finactext.getReferencePolicyNo() != vo.getRefPolicyNo()) {  
					finactext.setReferencePolicyNo(vo.getRefPolicyNo());  
					finactext.setAction(action);  
				}
				// APSL4910 END
			}
		}
		//begin NBA093
		if (extExists == false) {
			if (vo.getPartialRefundAmt() != 0 || vo.isDisbursedInd() || vo.isErrCorrInd() || vo.isPrevTaxYrInd() || vo.isPreTEFRATAMRAInd()) {
				OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
				if (oExt != null) {
					FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
					finactext.setPartialRefundAmt(vo.getPartialRefundAmt());
					finactext.setDisbursedInd(vo.isDisbursedInd());
					finactext.setErrCorrInd(vo.isErrCorrInd());
					finactext.setPrevTaxYrInd(vo.isPrevTaxYrInd());
					finactext.setPreTEFRATAMRAInd(vo.isPreTEFRATAMRAInd());
					finactext.setActivityReasonCode(vo.getActivityReasonCode()); // APSL4910
					finactext.setReferencePolicyNo(vo.getRefPolicyNo());  // APSL4910
					finactext.setActionAdd();
					finact.addOLifEExtension(oExt);
				}
			}
		}
		//end NBA093
	}
	/**
	 * Updates the <code>NbaCWAReverseRefundVO</code> instance data to FinancialActivity Object
	 * for a CWA partial refund.
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param vo
	 * @param finact
	 * @param nbaTXLife
	 * @param caseDst DST object contains case
	 */
	//NBA115 changed method signature, added  NbaDst caseDst
	protected void updatePartialRefund(NbaUserVO user, NbaCWAReverseRefundVO vo, FinancialActivity finact, NbaTXLife nbaTXLife, NbaDst caseDst)
		throws NbaBaseException {

		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();

		double origCwaAmt = vo.getFinActivityGrossAmt();  //NBA093
		double newCwaAmt = origCwaAmt - vo.getPartialRefundAmt();  //NBA093
		String origDeptDesk = vo.getUserCode();  // save original  //NBA093
		long newFinActSubType = vo.getFinActivitySubType();  //NBA093
		double newGrossAmt = vo.getPartialRefundAmt();  //NBA093

		// create a new Financial Activity for the Partial Refund
		FinancialActivity newFinact = finact.clone(false);
		//begin SPR1656
		int aaCount = newFinact.getAccountingActivity().size();
		while (aaCount > 0) {
			newFinact.removeAccountingActivityAt(--aaCount);
		}
		//end SPR1656		
		newFinact.deleteId();
		setIndForExtract(newFinact);// SPR1656
		NbaOLifEId olifeid = new NbaOLifEId(nbaTXLife);
		olifeid.setId(newFinact);
		vo.setFinActivityGrossAmt(vo.getPartialRefundAmt());  //NBA093
		vo.setUserCode(user.getUserID());  //NBA093
		updateFinancialActivityFromVO(vo, newFinact, NbaActionIndicator.ACTION_ADD);
		newFinact.setFinActivityDate(vo.getPartialRefundAsOfDate());
		newFinact.setOrderSource(finact.getId()); // NBLXA-1356
		policy.addFinancialActivity(newFinact);
		//process credit card partial refunds 
		if (vo.isCreditCardPayment()) { //NBA115
			processCreditCardRefund(caseDst, finact, vo, newFinact); //NBA115
		} //NBA115

		// create a new Financial Activity for the CWA remaining		
		newFinact = finact.clone(false);
		//begin SPR1656
		aaCount = newFinact.getAccountingActivity().size();
		while (aaCount > 0) {
			newFinact.removeAccountingActivityAt(--aaCount);
		}
		//end SPR1656		
		newFinact.deleteId();
		//SPR1959 code deleted
		olifeid.setId(newFinact);
		vo.setFinActivitySubType(-1L);  //NBA093
		vo.setPartialRefundAmt(0.0);  //NBA093
		vo.setFinActivityGrossAmt(newCwaAmt);  //NBA093
		updateFinancialActivityFromVO(vo, newFinact, NbaActionIndicator.ACTION_ADD);
		newFinact.setOrderSource(finact.getId()); // NBLXA-1356
		newFinact.setFinActivityDate(new Date()); // NBLXA-1356
		policy.addFinancialActivity(newFinact);

		policy.addFinancialActivity(addReversalForPartialRefund(finact, origCwaAmt, olifeid)); //SPR1959
		// update existing record as Disbursed
		vo.setDisbursedInd(true);  //NBA093
		vo.setFinActivityGrossAmt(origCwaAmt);  //NBA093
		vo.setUserCode(origDeptDesk);  //NBA093
		updateFinancialActivityFromVO(vo, finact, NbaActionIndicator.ACTION_UPDATE);

		// reset incoming values
		vo.setFinActivitySubType(newFinActSubType);  //NBA093
		vo.setPartialRefundAmt(newGrossAmt);  //NBA093
	}
	/**
	 * Updates the <code>NbaCWAReverseRefundVO</code> instance data to FinancialActivity Object
	 * for a CWA refund or reversal.
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param vo
	 * @param finact
	 * @param nbaTXLife
	 */
	//NBA115 changed method signature, added  NbaDst caseDst
	protected void updateRefundReversal(NbaUserVO user, NbaCWAReverseRefundVO vo, FinancialActivity finact, NbaTXLife nbaTXLife, NbaDst caseDst)
		throws NbaBaseException {
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();

		// create a new Financial Activity for the Refund or Reversal
		FinancialActivity newFinact = finact.clone(false);
		//begin SPR1656
		int aaCount = newFinact.getAccountingActivity().size();
		while (aaCount > 0) {
			newFinact.removeAccountingActivityAt(--aaCount);
		}
		//end SPR1656
		newFinact.deleteId();
		setIndForExtract(newFinact);// NBA066
		NbaOLifEId olifeid = new NbaOLifEId(nbaTXLife);
		olifeid.setId(newFinact);
		vo.setUserCode(user.getUserID()); //APSL629
		updateFinancialActivityFromVO(vo, newFinact, NbaActionIndicator.ACTION_ADD);
		newFinact.setOrderSource(finact.getId()); // NBLXA-1356
		newFinact.setFinActivityDate(new Date()); // NBLXA-1356
		policy.addFinancialActivity(newFinact);

		// update existing record
		setAccountingActivityType(finact, NbaActionIndicator.ACTION_UPDATE);
		setDisbursementInd(finact, true); //SPR1556
		//create a CWA refund work item for credit card payment refunds.
		if ((vo.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REFUND) && (vo.isCreditCardPayment())) { //NBA115
			processCreditCardRefund(caseDst, finact, vo, newFinact); //NBA115
		} //NBA115
		// APSL4984 Start
		if ((vo.getFinActivityType() == NbaOliConstants.OLI_FINANCIALACTIVITYTYPE_LOANCARRYOVER)
				&& (vo.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REV )) {
				 updateCarryoverLoanDetails(nbaTXLife,finact);
		}
		// APSL4984 END
	}
	
	// 	APSL4984 Start :: New method for Loan Carry Over Updation 
	protected void updateCarryoverLoanDetails(NbaTXLife nbaTXLife, FinancialActivity finact) {

		Holding loanholding = null;
		double loanBalance = 0;
		double finActivityAmt  = 0;
		ArrayList holdingList = new ArrayList();
		finActivityAmt = finact.getFinActivityGrossAmt();
		Loan loan = null;
		int relationCnt = nbaTXLife.getOLifE().getRelationCount();
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = nbaTXLife.getOLifE().getRelationAt(j);
			if (aRelation.hasRelationRoleCode() && aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY) {
				loanholding = nbaTXLife.getHolding(aRelation.getRelatedObjectID());
				int loanCountTemp = loanholding.getLoanCount();
				for(int i=0;i<loanCountTemp;i++) {
					Loan loanTemp = loanholding.getLoanAt(i); 
					if (loanTemp.hasLoanBalance() && loanTemp.getLoanBalance() > 0) {
						holdingList.add(loanholding);
					}
			} 
	} }
		if(holdingList !=null && !holdingList.isEmpty())
		{
			if(holdingList.size()==1)
			{
				loanholding = (Holding)holdingList.get(0);
				loan = loanholding.getLoanAt(0); 
				loanBalance = getLoanBalanceForLoanCarryOver(loanholding);
				if(loanBalance == finActivityAmt)
				{
					loan.setActionDelete();	
				}				
			}else if(holdingList.size()>1)
			{
				for(int i=0;i<holdingList.size();i++)
				{
					loanholding = (Holding)holdingList.get(i);
					double loanBalancetemp = getLoanBalanceForLoanCarryOver(loanholding);
					loanBalance = loanBalance+loanBalancetemp;
				}
				if(loanBalance == finActivityAmt)
				{
					for(int i=0;i<holdingList.size();i++)
					{
						loanholding = (Holding)holdingList.get(i);
						int loanCount = loanholding.getLoanCount();
						for(int k=0;k<loanCount;k++) {
							loan = loanholding.getLoanAt(k); 
							loan.setActionDelete();
						}
					}
					
				}
			}
		}
		
	}
	protected double getLoanBalanceForLoanCarryOver(Holding loanholding) {

		double loanBalanceSum = 0;
		double loanBalance = 0;
		int loanCount = loanholding.getLoanCount();
		Loan loan = null;
		for(int i=0;i<loanCount;i++) {
			loan = loanholding.getLoanAt(i); 
			if (loan.hasLoanBalance() && loan.getLoanBalance() > 0) {
				loanBalance = loan.getLoanBalance();
				loanBalanceSum = loanBalanceSum + loanBalance;
				}
		}
		return loanBalanceSum;
	}
// 	APSL4984 END ::
	/**
	 * Sets the AccountingActivityType indicator for the FinancialActivity Object.
	 * @param finact
	 * @param action
	 */
	// NBA093 changed method name
	protected void setAccountingActivityType(FinancialActivity finact, String action) {
		finact.setAccountingActivityType(NbaConstants.TRUE); // NBA093
		finact.setAction(action);
		//NBA093 code deleted
	}
	
	/**
	 * Adds the <code>NbaCWAReverseRefundVO</code> instance data to new FinancialActivity Object
	 * for a adding new CWA.
	 * @param vo
	 * @param nbaTXLife
	 */
	protected void addFinancialActivity(NbaCWAReverseRefundVO vo, NbaTXLife nbaTXLife) throws NbaBaseException {

		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		// SPR3290 code deleted

		// create a new Financial Activity
		FinancialActivity finActivity = new FinancialActivity();
		finActivity.setActionAdd();

		//set the ID for new FinancialActivity object
		NbaOLifEId olifeid = new NbaOLifEId(nbaTXLife);
		olifeid.setId(finActivity);

		//set the fields
		finActivity.setFinActivityType(vo.getFinActivityType());			//Type:
		finActivity.setFinActivitySubType(vo.getFinActivitySubType());		//SubType: //SPR1656
		finActivity.setFinActivityGrossAmt(vo.getFinActivityGrossAmt());	//Amount:
		finActivity.setRollloverIntAmt(vo.getRollOverIntAmount());	//NBA169 NBA223
		finActivity.setCostBasis(vo.getCostBasis());	//NBA169
		finActivity.setFinActivityDate(vo.getFinActivityDate());			//Entry Date:
		finActivity.setFinEffDate(vo.getFinEffDate());						//As of Date:
		finActivity.setIntTreatmentInd(vo.isIntTreatmentInd());				//Set to Issue:
		finActivity.setUserCode(vo.getUserCode());							//Originator:
		finActivity.setReferenceNo(vo.getReferenceNo());					//Ticket Number:
		
		finActivity.setBestIntRateType(vo.getBestIntRateType());			//Most Beneficial Interest
		finActivity.setCostBasisAdjAmt(vo.getCostBasisAdjAmt());			//Rollover Interest Amount
		finActivity.setIntPostingRate(vo.getIntPostingRate());				//Override Interest Rate
		//Begin AXAL3.7.12
		Payment payment = new Payment();
		payment.setPaymentForm(vo.getPaymentSource());
		finActivity.addPayment(payment);
		//End AXAL3.7.12
		//AXAL3.7.23 - code deleted
		OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
		if (oExt != null) {
			FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
			//finactext.setDisbursedInd(vo.isDisbursedInd()); 			//Disbursed if you need to uncomment this add an additional or statement in 
			finactext.setPrevTaxYrInd(vo.isPrevTaxYrInd());				//Previous Tax Year
			finactext.setPreTEFRATAMRAInd(vo.isPreTEFRATAMRAInd());		//Pre Tefra
			finactext.setLocationID(getLocationID(policy));//ALS4266
			//Begin QC1599 - ALS2905 // if the financial activity type is wire then set location id to '084' - Charlotte
			//QC4956 - ALS5772 code deleted of QC1599 - ALS2905
			if (NbaUtils.isRetail(policy)) { //QC4956 - ALS5772
				finactext.setLocationID(NbaConstants.LOCATION_ID_084);
			} else if (NbaUtils.isWholeSale(policy)) { //QC4956 - ALS5772
				finactext.setLocationID(NbaConstants.LOCATION_ID_042);
			}
			// End QC1599
			// finactext.setActivityReasonCode(vo.getA)
			
			// APSL4910 START
			finactext.setActivityReasonCode(vo.getActivityReasonCode());
			finactext.setReferencePolicyNo(vo.getRefPolicyNo());
			// APSL4910 END
			finactext.setActionAdd();
			finActivity.addOLifEExtension(oExt);
		}
		
		policy.addFinancialActivity(finActivity);
	}
	
	/**
	* Returns the Location ID from the Policy.FinancialActivity
	* @param policy Policy object
	*/
	//	ALS4266 New Method
	protected String getLocationID(Policy policy) {
		List finActivities = policy.getFinancialActivity();
		for(int i = 0; i< finActivities.size(); i++){
			FinancialActivityExtension finExt = NbaUtils.getFirstFinancialActivityExtension((FinancialActivity)finActivities.get(i));
			if(finExt != null && finExt.getLocationID() != null){
				return finExt.getLocationID();
			}	
		}
		return null;
	}
	
	
	/**
	* Set Accounting Extract Indicator to false for cloned FinancialActivity object.
	* @param finAct FinancialActivity object
	*/
	//	NBA066 New Method
	protected FinancialActivity setIndForExtract(FinancialActivity finAct) {
		FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(finAct);
		if(finActExt != null){
			finActExt.setAcctgExtractInd(false);
		}
		return finAct;
	}

	/**
	 * Sets disbursement indicator in financial activity object as passed in the method
	 * @param finAct an instance of FinancialActivity object
	 * @param disburseInd boolean indicating disbursement indicator to be set. 
	 */
	//SPR1556 New Method
	protected void setDisbursementInd(FinancialActivity finAct, boolean disburseInd) {
		List oliExtList = finAct.getOLifEExtension();
		if (oliExtList.size() != 0) {
			OLifEExtension oliExt = finAct.getOLifEExtensionAt(0);
			FinancialActivityExtension finactExt = oliExt.getFinancialActivityExtension();
			if (finactExt != null) {
				finactExt.setDisbursedInd(disburseInd);
				finactExt.setActionUpdate();
			}
		}
	}
	
	/**
	 * Creates a CWA work item for credit card refund. Initializes required LOB data from the original payment work item, which is getting refunded.
	 * This method assumes the FinancialActivity and FinancialActivityExtension objects are not null for new reversal activity. 
	 * @param caseDst case DST object
	 * @param finAct the payment which is getting refunded.
	 * @throws NbaBaseException
	 */
	//NBA115 New Method
	protected void processCreditCardRefund(NbaDst caseDst, FinancialActivity finAct, NbaCWAReverseRefundVO vo, FinancialActivity newFinact)
		throws NbaBaseException {
		NbaUserVO userVO = new NbaUserVO(NbaConstants.PROC_CWA_REVERSE_REFUND, ""); //SPR2639
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
				if (vo.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REFUND) {
					refundAmount = vo.getFinActivityGrossAmt();
				} else if (vo.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND) {
					refundAmount = vo.getPartialRefundAmt();
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
	/**
	 * Create a new Financial Activity for reversal of the original payment on which a partial refund is applied
	 * @param finact original financial activity
	 * @param origCwaAmt original amount
	 * @param olifeid 
	 * @return a new financial activity object representing reversal of original amount.
	 */
	//SPR1959 New Method
	protected FinancialActivity addReversalForPartialRefund(FinancialActivity finact, double origCwaAmt, NbaOLifEId olifeid) {
		FinancialActivity reversalFinActivity = new FinancialActivity();
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
		FinancialActivityExtension finActExt = olifeExt.getFinancialActivityExtension();
		reversalFinActivity.addOLifEExtension(olifeExt);
		finActExt.setLocationID(finact.getOLifEExtensionAt(0).getFinancialActivityExtension().getLocationID());
		finActExt.setAcctgExtractInd(true); //APSL5250
		finActExt.setActionAdd();
		olifeid.setId(reversalFinActivity);
		reversalFinActivity.setFinActivityType(finact.getFinActivityType());
		reversalFinActivity.setFinActivitySubType(NbaOliConstants.OLI_FINACTSUB_REV);
		reversalFinActivity.setFinActivityGrossAmt(origCwaAmt);
		reversalFinActivity.setOrderSource("None"); // NBLXA-1356
		reversalFinActivity.setActionAdd();
		return reversalFinActivity;
	}	

}