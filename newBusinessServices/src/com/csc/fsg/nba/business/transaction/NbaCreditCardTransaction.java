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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import java.util.Date;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.AccountHolderNameCC;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Relation;

/**
 * NbaCreditCardTransaction will be used to create xml transactions for submission to credit card
 * clearing houses.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card Information is not saved</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaCreditCardTransaction extends NbaBusinessTransactions {
	/**
	 * Constructor for NbaCreditCardTransaction
	 */
	public NbaCreditCardTransaction() {
		super();
	}

	//SPR3573 code deleted
	
	/**
	 * Creates a credit card debit transaction for submission to a credit card clearing house
	 * adapter.  
	 * @param nbaDst An instance of <code>NbaDst</code> containing CWA work for credit card payment
	 * @param user user value obhect
	 * @return NbaTXLife request XML transaction for credit card debit
	 * @throws NbaBaseException
	 */
	public NbaTXLife generateDebitRequest(NbaDst nbaDst, NbaUserVO user) throws NbaBaseException {

		NbaLob ccLob = nbaDst.getNbaLob();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_CREDITCARD_DEBIT);
		nbaTXRequest.setBusinessProcess(NbaConstants.PROC_CREADIT_CARD); //SPR2639
		nbaTXRequest.setTranContentCode(NbaOliConstants.TC_CONTENT_INSERT);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ADD);
		nbaTXRequest.setNbaLob(ccLob);

		//create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife);
		OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		Holding holding = txLife.getPrimaryHolding();
		// SPR3290 code deleted
		
		Banking banking = new Banking();
		nbaOLifEId.setId(banking);
		banking.setAccountNumber(ccLob.getCCNumber());
		banking.setCreditCardExpDate(ccLob.getCCExpDate());
		banking.setCreditCardType(ccLob.getCCType());
		
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);	
		BankingExtension bankingExt = olifeExt.getBankingExtension();
		banking.addOLifEExtension(olifeExt);

		AccountHolderNameCC acctHolderCC = new AccountHolderNameCC();
		String name = ccLob.getCCBillingName();
		if (name != null && name.length() > 0) {
			acctHolderCC.addAccountHolderName(name);
		}

		//Populate banking fields from txlife that are not available from the credit card work item lobs.		
		//BankingExtension txLifeBankingExt = getTxLifeBankingExtension(nbaDst, user);
		//TODO get holding to populate bankingExt fields
		BankingExtension txLifeBankingExt = null;				
				
		if(txLifeBankingExt != null){
			bankingExt.setPaymentType(txLifeBankingExt.getPaymentType());
			bankingExt.setCreditCardChargeUse(txLifeBankingExt.getCreditCardChargeUse());	
		}				
		bankingExt.setAccountHolderNameCC(acctHolderCC);
		bankingExt.setPaymentChargeAmt(ccLob.getCwaAmount());
		bankingExt.setChargeEffDate(new java.util.Date());		
		holding.addBanking(banking);
		
		Party party = new Party();
		nbaOLifEId.setId(party);	
		party.setFullName(ccLob.getCCBillingName());

		Address address = new Address();
		nbaOLifEId.setId(address);
		address.setLine1(ccLob.getCCBillingAddr());
		address.setCity(ccLob.getCCBillingCity());
		address.setAddressStateTC(ccLob.getCCBillingState());
		address.setZip(ccLob.getCCBillingZip());	
		party.addAddress(address);
		olife.addParty(party);
		
		bankingExt.setAppliesToPartyID(party.getId());
		bankingExt.setMailingAddressID(address.getId());
		
		Relation relation = new Relation();
		nbaOLifEId.setId(relation);
		relation.setOriginatingObjectID(holding.getId());
		relation.setRelatedObjectID(party.getId());
		relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
		relation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		relation.setRelationRoleCode(NbaOliConstants.OLI_REL_PYMT_FACILITATOR);
		olife.addRelation(relation);
		
		return txLife;
	}

	/**
	 * Creates a credit card credit transaction for submission to a credit card clearing house
	 * adapter.  The adapter converts this data into a transaction that is sent
	 * directly to the clearing house.  The adapter is created and maintained by
	 * nbA customers and is external to nbA processing.
	 * @param nbaDst An instance of <code>NbaDst</code>
	 * @return an instance of <NbaTXLife> containing acord transaction for credit card 'credit' 
	 * @throws NbaBaseException
	 */
	public NbaTXLife generateCreditRequest(NbaDst nbaDst, NbaUserVO user) throws NbaBaseException {

		NbaLob ccLob = nbaDst.getNbaLob();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_CREDITCARD_CREDIT);
		nbaTXRequest.setBusinessProcess(NbaConstants.PROC_CREDIT_CARD_REFUND);
		nbaTXRequest.setTranContentCode(NbaOliConstants.TC_CONTENT_INSERT);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ADD);
		nbaTXRequest.setNbaLob(ccLob);

		//create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife);
		OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		Holding holding = txLife.getPrimaryHolding();
		// SPR3290 code deleted
		
		Banking banking = new Banking();
		nbaOLifEId.setId(banking);
		banking.setCreditDebitType(NbaOliConstants.OLI_ACCTDBCRTYPE_CREDIT);
		banking.setAccountNumber(NbaBase64.decodeToString(ccLob.getCCNumber()));
		banking.setCreditCardExpDate(ccLob.getCCExpDate());
		banking.setCreditCardType(ccLob.getCCType());
		
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);	
		BankingExtension bankingExt = olifeExt.getBankingExtension();
		banking.addOLifEExtension(olifeExt);

		AccountHolderNameCC acctHolderCC = new AccountHolderNameCC();
		String name = ccLob.getCCBillingName();
		if (name != null && name.length() > 0) {
			acctHolderCC.addAccountHolderName(name);
		}
		bankingExt.setAccountHolderNameCC(acctHolderCC);
		bankingExt.setPaymentChargeAmt(ccLob.getCwaAmount());
		bankingExt.setCreditCardChargeUse(NbaOliConstants.OLIEXT_LU_CHARGEUSE_REFUND);
		bankingExt.setChargeEffDate(new Date());
		holding.addBanking(banking);
		
		Party party = new Party();
		nbaOLifEId.setId(party);	
		party.setFullName(name);

		Address address = new Address();
		nbaOLifEId.setId(address);
		address.setLine1(ccLob.getCCBillingAddr());
		address.setCity(ccLob.getCCBillingCity());
		address.setAddressStateTC(ccLob.getCCBillingState());
		address.setZip(ccLob.getCCBillingZip());	
		party.addAddress(address);
		olife.addParty(party);
		
		bankingExt.setAppliesToPartyID(party.getId());
		bankingExt.setMailingAddressID(address.getId());
		
		Relation relation = new Relation();
		nbaOLifEId.setId(relation);
		relation.setOriginatingObjectID(holding.getId());
		relation.setRelatedObjectID(party.getId());
		relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
		relation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		relation.setRelationRoleCode(NbaOliConstants.OLI_REL_PYMT_FACILITATOR);
		olife.addRelation(relation);
		
		return txLife;
	}
}
