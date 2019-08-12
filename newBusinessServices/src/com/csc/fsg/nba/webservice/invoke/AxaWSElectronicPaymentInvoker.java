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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * This class is responsible for creating request for ACH payment.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL2735</td>
 * <td>Discretionary</td>
 * <td>Electronic Initial Premium</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSElectronicPaymentInvoker extends AxaWSInvokerBase {
	
	private static final String CATEGORY = "AccountingService";

	/**
	 * constructor from superclass
	 * 
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSElectronicPaymentInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(operation);
	}
	
	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequestVO = new NbaTXRequestVO();
		nbaTXRequestVO.setTransType(NbaOliConstants.TC_TYPE_ACCOUNTINGSTMTTRANS);
		nbaTXRequestVO.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequestVO.setNbaUser(getUserVO());
		//Deleted Code of ALII2048
		NbaTXLife txLifeRequest = new NbaTXLife(nbaTXRequestVO);
		//Begin ALII2048
		if(getNbaTXLife()!=null && getNbaTXLife().getPolicy().getCarrierAdminSystem()!=null){
			txLifeRequest.getOLifE().getSourceInfo().setFileControlID(getNbaTXLife().getPolicy().getCarrierAdminSystem());
		}
		//End ALII2048
		if(WS_OP_SUBMIT_ACHPAYMENT.equalsIgnoreCase(getOperation())){
			 createSubmitAchRequest(txLifeRequest);
		} else if(WS_OP_INQUIRE_ACHPAYMENT.equalsIgnoreCase(getOperation())){
			createInquireAchRequest(txLifeRequest);
		} else if(WS_OP_REFUND_ACHPAYMENT.equalsIgnoreCase(getOperation())){
			createRefundAchRequest(txLifeRequest);
		}
		return txLifeRequest;
	}
	
	public NbaTXLife createInquireAchRequest(NbaTXLife txLifeRequest) {
		Policy aPolicy = new Policy();
		aPolicy.setPolNumber(getNbaTXLife().getPolicy().getPolNumber());
		aPolicy.setCarrierCode(getNbaTXLife().getPolicy().getCarrierCode());
		aPolicy.setProductCode(getNbaTXLife().getPolicy().getProductCode());
		aPolicy.setId("Policy_1");
		FinancialActivity finAcct = new FinancialActivity();
		finAcct.setId("FinancialActivity_1");
		FinancialActivity finObject = (FinancialActivity) getObject();
		FinancialActivityExtension finObjectExt = NbaUtils.getFirstFinancialActivityExtension(finObject);
		finAcct.setReferenceNo(finObject.getReferenceNo());
		OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
		oliExt.getFinancialActivityExtension().setConfirmationID(finObjectExt.getConfirmationID());
		finAcct.addOLifEExtension(oliExt);
		aPolicy.addFinancialActivity(finAcct);
		Holding aHolding = new Holding();
		aHolding.setId("Holding_1");
		aHolding.setPolicy(aPolicy);
		txLifeRequest.getOLifE().addHolding(aHolding);
		return txLifeRequest;
	}
	public NbaTXLife createSubmitAchRequest(NbaTXLife txLifeRequest) throws NbaBaseException{
		Policy aPolicy = new Policy();
		aPolicy.setPolNumber(getNbaTXLife().getPolicy().getPolNumber());
		aPolicy.setCarrierCode(getNbaTXLife().getPolicy().getCarrierCode());
		aPolicy.setProductCode(getNbaTXLife().getPolicy().getProductCode());
		aPolicy.setId("Policy_1");
		FinancialActivity finAcct = new FinancialActivity();
		finAcct.setId("FinancialActivity_1");
		FinancialActivity finObject = (FinancialActivity) getObject();
		finAcct.setReferenceNo(finObject.getReferenceNo());
		Payment payment = new Payment();
		payment.setId("Payment_1");
		payment.setPaymentForm(NbaOliConstants.OLI_PAYFORM_EFT);
		payment.setPaymentAmt(getNbaDst().getNbaLob().getCheckAmount());
		finAcct.addPayment(payment);
		aPolicy.addFinancialActivity(finAcct);
		Banking newBanking = new Banking();
		Banking orgBanking = NbaUtils.getBankingByHoldingSubType(getNbaTXLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if(orgBanking != null){
			newBanking.setBankAcctType(orgBanking.getBankAcctType());
			newBanking.setAccountNumber(orgBanking.getAccountNumber());
			newBanking.setAcctHolderName(orgBanking.getAcctHolderName());
			newBanking.setRoutingNum(orgBanking.getRoutingNum());
			//Begin APSL3262
			BankingExtension bankExt = NbaUtils.getFirstBankingExtension(orgBanking); 
			if(bankExt != null){
				OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);
				oliExt.getBankingExtension().setBankName(bankExt.getBankName());
				newBanking.addOLifEExtension(oliExt);
			}
			// End APSL3262
			newBanking.setId(orgBanking.getId());
		}
		Holding aHolding = new Holding();
		aHolding.addBanking(newBanking);
		aHolding.setPolicy(aPolicy);
		Holding initialHolding = NbaUtils.getHoldingByTypeAndSubTypeCode(getNbaTXLife().getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING, NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if (initialHolding != null) {
			NbaParty payerParty = getNbaTXLife().getParty(getNbaTXLife().getPayerPartyId(NbaOliConstants.OLI_REL_PAYER, initialHolding.getId()));
			if (payerParty != null) {
				generateOriginalPartyforNewTxLife(payerParty.getParty(), txLifeRequest.getOLifE());
				Relation payerRelation = getNbaTXLife().getRelation(initialHolding.getId(), payerParty.getParty().getId(), NbaOliConstants.OLI_REL_PAYER);
				generateRelationObject(payerRelation, txLifeRequest.getOLifE());
			}
			aHolding.setId(initialHolding.getId());
		}
		txLifeRequest.getOLifE().addHolding(aHolding);
		return txLifeRequest;
	}
	
	/**
	 * @param originalParty
	 * @param newOlife
	 */
	private void generateOriginalPartyforNewTxLife(Party originalParty, OLifE newOlife) {
		if (originalParty != null) {
			Party newParty = new Party();
			generatePartyObject(originalParty, newParty, newOlife);
			PersonOrOrganization originalPersonOrOrg = originalParty.getPersonOrOrganization();
			PersonOrOrganization newPersonOrOrg = new PersonOrOrganization();
			if (originalPersonOrOrg.isPerson()) {
				generatePersonObject(originalPersonOrOrg, newPersonOrOrg);
				newParty.setPersonOrOrganization(newPersonOrOrg);
			}
			if (originalPersonOrOrg.isOrganization()) {
				generateOrganizationObject(originalPersonOrOrg, newPersonOrOrg);
				newParty.setPersonOrOrganization(newPersonOrOrg);
			}
			//Begin APSL3262
			if(originalParty.getEMailAddressCount()>0){
				EMailAddress newEmailAdd = new EMailAddress();
				newEmailAdd = originalParty.getEMailAddressAt(0);
				newParty.addEMailAddress(newEmailAdd);
			}
			// End APSL3262
		}
	}
	
	protected void generatePartyObject(Party originalParty, Party newParty, OLifE newOlife) {
		newParty.setId(originalParty.getId());
		newParty.setPartyTypeCode(originalParty.getPartyTypeCode());
		newParty.setGovtID(originalParty.getGovtID());
		newParty.setGovtIDTC(originalParty.getGovtIDTC());
		newOlife.addParty(newParty);
	}
	
	/**
	 * Generates Person object for XML1225 based on Person object available in contract xml
	 * 
	 * @param originalPersonOrOrg
	 *            PersonOrOrganization object from contract xml203
	 * @param newPersonOrOrg
	 *            PersonOrOrganization object
	 */
	protected void generatePersonObject(PersonOrOrganization originalPersonOrOrg, PersonOrOrganization newPersonOrOrg) {
		Person originalPerson = originalPersonOrOrg.getPerson();
		Person newPerson = new Person();
		newPerson.setFirstName(originalPerson.getFirstName());
		newPerson.setMiddleName(originalPerson.getMiddleName());
		newPerson.setLastName(originalPerson.getLastName());
		newPerson.setPrefix(originalPerson.getPrefix());
		newPerson.setSuffix(originalPerson.getSuffix());
		newPersonOrOrg.setPerson(newPerson);
	}
	
	/**
	 * Generates Organization object for XML1225 based on Organization object available in contract xml
	 * 
	 * @param originalPersonOrOrg
	 *            PersonOrOrganization object from contract xml203
	 * @param newPersonOrOrg
	 *            PersonOrOrganization object
	 */
	protected void generateOrganizationObject(PersonOrOrganization originalPersonOrOrg, PersonOrOrganization newPersonOrOrg) {
		Organization originalOrganization = originalPersonOrOrg.getOrganization();
		Organization newOrganization = new Organization();
		newOrganization.setDBA(originalOrganization.getDBA());
		newPersonOrOrg.setOrganization(newOrganization);
	}
	
	/**
	 * Generates Relation object for XML1225 based on Relation object available in contract xml
	 * 
	 * @param originalRelation
	 *            Relation object from contract xml203
	 * @param newOlife
	 *            OLifE object
	 */
	protected void generateRelationObject(Relation originalRelation, OLifE newOlife) {
		if (originalRelation != null) {
			Relation newRelation = new Relation();
			newRelation.setId(originalRelation.getId());
			newRelation.setOriginatingObjectID(originalRelation.getOriginatingObjectID());
			newRelation.setRelatedObjectID(originalRelation.getRelatedObjectID());
			newRelation.setOriginatingObjectType(originalRelation.getOriginatingObjectType());
			newRelation.setRelatedObjectType(originalRelation.getRelatedObjectType());
			newRelation.setRelationRoleCode(originalRelation.getRelationRoleCode());
			newOlife.addRelation(newRelation);
		}
	}
	
	/**
	 * Blank Overridden for submit and inquire interfaces and in case of refund interface, If failure response is received 
	 * from interface and if ResultInfoCode != RESULTINFO_DUPL, call super class handleResponse. 
     */
    protected void handleResponse() throws NbaBaseException {
		if (WS_OP_REFUND_ACHPAYMENT.equalsIgnoreCase(getOperation())) {
			TransResult transResult = ((NbaTXLife) getWebserviceResponse()).getTransResult();
			if (transResult != null && transResult.getResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
				List resultInfoList = transResult.getResultInfo();
				if (resultInfoList != null && resultInfoList.size() > 0) {
					for (int i = 0; i < resultInfoList.size(); i++) {
						ResultInfo resultInfo = (ResultInfo) resultInfoList.get(i);
						if (resultInfo.getResultInfoCode() == NbaOliConstants.TC_RESINFO_BELOWMINDAYS) { //Begin ALII1918
							if (!NbaUtils.isBlankOrNull(resultInfo.getResultInfoDesc())) {
								throw new NbaBaseException(resultInfo.getResultInfoDesc());
							}
						} else { //End ALII1918
							super.handleResponse();
						}
					}
				} else {
					throw new NbaBaseException("Failure response from refundACHPayment ");
				}
			}
		}
	}
    
    public NbaTXLife createRefundAchRequest(NbaTXLife txLifeRequest) {
		Policy aPolicy = new Policy();
		aPolicy.setPolNumber(getNbaTXLife().getPolicy().getPolNumber());
		aPolicy.setCarrierCode(getNbaTXLife().getPolicy().getCarrierCode());
		aPolicy.setProductCode(getNbaTXLife().getPolicy().getProductCode());
		aPolicy.setId("Policy_1");
		FinancialActivity finAcct = new FinancialActivity();
		finAcct.setId("FinancialActivity_1");
		FinancialActivity finObject = (FinancialActivity) getObject();
		finAcct.setFinActivitySubType(finObject.getFinActivitySubType());//APSL3262
		finAcct.setReferenceNo(getPaymentReferenceID(AxaWSConstants.WS_OP_REFUND_ACHPAYMENT));
		//Start :: QC14236/APSL4046
		/*Payment payment = new Payment();
		payment.setId("Payment_1");
		payment.setPaymentForm(NbaOliConstants.OLI_PAYFORM_EFT);*/
		ArrayList orgPaymentList = finObject.getPayment();
		finAcct.setPayment(orgPaymentList);
        Iterator paymentItr = finAcct.getPayment().iterator();
        while (paymentItr.hasNext()) {
            Payment payment = (Payment) paymentItr.next();
            payment.setPaymentForm(NbaOliConstants.OLI_PAYFORM_EFT);
            payment.setPaymentAmt(finObject.getFinActivityGrossAmt());
            payment.setCheckDescription(NbaUtils.getDescription(finObject, getNbaTXLife()));
            OLifEExtension olifeExtPayment = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PAYMENT);
            olifeExtPayment.getPaymentExtension().setCheckDestination(NbaConstants.CHECK_DESTINATION);
            payment.addOLifEExtension(olifeExtPayment);
        }
      //End :: QC14236/APSL4046
		
		FinancialActivityExtension finObjectExt = NbaUtils.getFirstFinancialActivityExtension(finObject);
		if(finObjectExt != null){
			OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
			oliExt.getFinancialActivityExtension().setConfirmationID(finObjectExt.getConfirmationID());
			finAcct.addOLifEExtension(oliExt);	
		}
		aPolicy.addFinancialActivity(finAcct);
		Banking newBanking = new Banking();
		Banking orgBanking = NbaUtils.getBankingByHoldingSubType(getNbaTXLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if(orgBanking != null){
			newBanking.setBankAcctType(orgBanking.getBankAcctType());
			newBanking.setAccountNumber(orgBanking.getAccountNumber());
			newBanking.setAcctHolderName(orgBanking.getAcctHolderName());
			newBanking.setRoutingNum(orgBanking.getRoutingNum());
			//Begin APSL3262
			BankingExtension bankExt = NbaUtils.getFirstBankingExtension(orgBanking);
			if(bankExt != null){
				OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);
				oliExt.getBankingExtension().setBankName(bankExt.getBankName());
				newBanking.addOLifEExtension(oliExt);
			}
			//End APSL3262
			newBanking.setId(orgBanking.getId());
		}
		Holding aHolding = new Holding();
		aHolding.addBanking(newBanking);
		aHolding.setPolicy(aPolicy);
		Holding initialHolding = NbaUtils.getHoldingByTypeAndSubTypeCode(getNbaTXLife().getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING, NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if (initialHolding != null) {
			NbaParty payerParty = getNbaTXLife().getParty(getNbaTXLife().getPayerPartyId(NbaOliConstants.OLI_REL_PAYER, initialHolding.getId()));
			if (payerParty != null) {
				generateOriginalPartyforNewTxLife(payerParty.getParty(), txLifeRequest.getOLifE());
				Relation payerRelation = getNbaTXLife().getRelation(initialHolding.getId(), payerParty.getParty().getId(), NbaOliConstants.OLI_REL_PAYER);
				generateRelationObject(payerRelation, txLifeRequest.getOLifE());
			}
			aHolding.setId(initialHolding.getId());
		}
		txLifeRequest.getOLifE().addHolding(aHolding);
		return txLifeRequest;
	}
    
    /**
	 * Generates PaymentReferenceID
	 * 
	 * @return PaymentReferenceID in format PolNumberyyyyMMddHHmmssSSS
	 */
	protected String getPaymentReferenceID(String operationName) {
		if (AxaWSConstants.WS_OP_REFUND_ACHPAYMENT.equalsIgnoreCase(operationName)) {
			StringBuffer paymentRef = new StringBuffer();
			paymentRef.append(getNbaTXLife().getPolicy().getPolNumber());
			paymentRef.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
			return paymentRef.toString();
		}
		return null;
	}
}