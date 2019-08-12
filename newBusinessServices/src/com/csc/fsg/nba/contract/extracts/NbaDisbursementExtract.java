package com.csc.fsg.nba.contract.extracts;
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
import java.sql.Connection;

import com.csc.fsg.nba.database.NbaExtractDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaCWARefundDisbursementsExtractVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
* NbaDisbursementExtract class is used to create Disbursement Extracts for the refund of CWA.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>SPR1656</td><td>Version 4</td><td>Allow for Refund/Reversal Extracts.</td></tr>
* <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes.</td></tr>
* <tr><td>SPR1906</td><td>Version 4</td><td>General source code clean up</td></tr>
* <tr><td>NBA115</td><td>Version 5</td><td>Credit Card Payment Reversal and Refund</td></tr>
* <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
* <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
* <tr><td>AXAL3.7.28</td><td>AXA Life Phase 1</td><td>Check Writing Interface</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 4
*/
public class NbaDisbursementExtract {

	protected static NbaLogger logger = null;
	
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaDisbursementExtract.class.getName());  //SPR1906
			} catch (Exception e) {
				NbaBootLogger.log("NbaDisbursementExtract could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * This method creates Disbursement extracts for Refund or Partial Refund Financial Activity by writing 
	 * rows to the NBA_CWA_REFUND_DISBURSEMENT table for each eligible Activity. If an extract has 
	 * been previously generated (FinancialActivityExtension.DisbExtractInd = true) it is not regenerated.
	 * If the Financial Activity represents a Credit Card Payment, disbursement extract will not be generated.
	 * 
	 * If extracts are sucessfully created, the Connection to the NBA_CWA_REFUND_DISBURSEMENT table is returned. 
	 * Otherwise, null is returned.
	 * 
	 * @param nbaTXLife NbaTXLife object
	 * @return conn Connection object which contains the connection of extract database, or null 
	 * @throws NbaBaseException
	 */
	//AXAL3.7.28 - Added NbaUserVO to method signature
	//APSL2440 Internal Perfomance Issue changed method signature
	protected void createDisbursementExtractForCWARefund(NbaTXLife nbaTXLife, NbaUserVO user,boolean isIssue) throws NbaBaseException { //NBLXA-1457
		// APSL2440 Internal Perfomance Issue code deleted
		// code deleted for APSL3874
		// AXAL3.7.28
		// APSL2440 Internal Perfomance Issue code deleted
		int finActivityCount = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityCount();
		for (int i = 0; i < finActivityCount; i++) {
			FinancialActivity finAct = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityAt(i);
			// if (finAct.getAccountingActivityCount() > 0) {
			FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(finAct);
			// check if it is a credit card refund
			boolean isCCPayment = finActExt.getCreditCardTransID() != null; // NBA115
			// Create disbursement extracts for refund of CWA, skip disbursement extracts for credit card refunds
			if ((finAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REFUND || finAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND || (finAct.getFinActivityType() == NbaOliConstants.OLI_FINACT_PVT315 && nbaTXLife.getBackendSystem().equals(NbaConstants.SYST_CAPS)) )   //NBLXA-1457 NBLXA-1643  
					&& !isCCPayment) { // NBA115
				if (finActExt != null) {
					if (!(finActExt.getDisbExtractInd())) {
						// APSL2735 begin call different interface if initial primium type is electronic
						if (NbaUtils.isElectronicPaymentForm(finAct) && !(finAct.getFinActivityType() == NbaOliConstants.OLI_FINACT_PVT315 && isIssue)) { //NBLXA-1457
							AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_REFUND_ACHPAYMENT,
									user, nbaTXLife, null, finAct);
							webServiceInvoker.execute();
						}
						// APSL2735 end

						// AXAL3.7.28 code deleted
						// AXAL3.7.28 begin
						else {
							AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SEND_PREMIUM_REFUND,
									user, nbaTXLife, null, finAct);
							webServiceInvoker.execute();
							// AXAL3.7.28 end
						}
						finActExt.setDisbExtractInd(true);
						finActExt.setActionUpdate();
					}
				} else {
					// APSL2735 begin call different interface if initial primium type is electronic
					if (NbaUtils.isElectronicPaymentForm(finAct)) {
						AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_REFUND_ACHPAYMENT, user,
								nbaTXLife, null, finAct);
						webServiceInvoker.execute();
					}
					// APSL2735 end
					// AXAL3.7.28 code deleted
					// AXAL3.7.28 begin
					else {
						AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SEND_PREMIUM_REFUND,
								user, nbaTXLife, null, finAct);
						webServiceInvoker.execute();
					}
					// AXAL3.7.28 end
					// set disbursement Extract Generated indicator
					OLifEExtension olifExt = new OLifEExtension();
					olifExt.setActionAdd();
					finActExt = new FinancialActivityExtension();
					finActExt.setActionAdd();
					finActExt.setDisbExtractInd(true);
					olifExt.setFinancialActivityExtension(finActExt);
					finAct.addOLifEExtension(olifExt);
				}
			}
			// }
		}
		// AXAL3.7.28 begin
		// code deleted for APSL3874
		// AXAL3.7.28 end
		// APSL2440 Internal Perfomance Issue code deleted
	}

	/**
	 * Update Disbursement Extract for CWA RefundActivity in the nbA extract database.
	 * @param nbaTXLife NbaTXLife object
	 * @param finAct FinancialActivity object
	 * @throws NbaBaseException
	 */
	//AXAL3.7.28 - Added the parameter txLife1225 to the method signature
	protected Connection createDisbursementExtractForCWARefund(NbaTXLife nbaTXLife, FinancialActivity finAct, Connection conn, NbaTXLife txLife1225 )
		throws NbaDataAccessException {
		NbaCWARefundDisbursementsExtractVO element = new NbaCWARefundDisbursementsExtractVO();
		element.setSystemIDKey(nbaTXLife.getOLifE().getSourceInfo().getFileControlID());
		element.setCompanyCode(nbaTXLife.getPolicy().getCarrierCode());
		element.setContractNumber(nbaTXLife.getPolicy().getPolNumber());
		element.setExtractCreate(new java.util.Date());
		element.setExtractSent(null);
		element.setXml_Data(txLife1225.toXmlString());
		NbaExtractDataBaseAccessor.getInstance().insert(element, conn);
		return conn;
	}

	/**
	 * This method generates 1225 XML for Disbursement extract.
	 * @param nbaTXLife NbaTXLife object
	 * @param finAct FinancialActivity object
	 * @return disbTxLife1225 NbaTXLife
	 */
/*	protected NbaTXLife generateDisbursementTrx1225(NbaTXLife nbaTXLife, FinancialActivity finAct, NbaUserVO user) { //AXAL3.7.28
		Relation insuredRelation = null;	//AXAL3.7.28
		NbaTXRequestVO nbaTXRequestVO = new NbaTXRequestVO();
		nbaTXRequestVO.setTransType(NbaOliConstants.TC_TYPE_ACCOUNTINGSTMTTRANS);
		nbaTXRequestVO.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequestVO.setBusinessProcess("");
		nbaTXRequestVO.setNbaUser(user);	//AXAL3.7.28
		//create txlife with default request fields
		NbaTXLife disbTxLife1225 = new NbaTXLife(nbaTXRequestVO);
		Party originalParty = null;
		OLifE olife = nbaTXLife.getOLifE();
		OLifE newOlife = disbTxLife1225.getOLifE();
		Relation originalRelation = getRelationObject(olife);
		FinancialStatement finStatement = new FinancialStatement();
		finStatement.setId("FinancialStatement_1");
		disbTxLife1225.getOLifE().addFinancialStatement(finStatement);		
		Holding originalHolding = nbaTXLife.getPrimaryHolding();
		Policy originalPolicy = originalHolding.getPolicy();
		finStatement.setStatementType(NbaOliConstants.OLI_STMTTYPE_DISBEXT);
		finStatement.setCarrierCode(originalPolicy.getCarrierCode()); // Same as of the associated payment extract.
		finStatement.setDescription(originalPolicy.getPolNumber()); // Same as of the associated payment extract.
		
		DisbursementStatement disbStatement = new DisbursementStatement();
		disbStatement.setRemittanceAmt(finAct.getFinActivityGrossAmt());
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALSTATEMENT);
		olifeExt.getFinancialStatementExtension().setDisbursementStatement(disbStatement);
		finStatement.addOLifEExtension(olifeExt);
		
		Policy newPolicy = new Policy();
		newPolicy.setId(originalPolicy.getId());
		newPolicy.setPolNumber(originalPolicy.getPolNumber());
		newPolicy.setLineOfBusiness(originalPolicy.getLineOfBusiness());
		Holding newHolding = new Holding();
		newHolding.setId(originalHolding.getId());
		disbTxLife1225.getOLifE().addHolding(newHolding);
		FinancialActivity newFinancialActivity = new FinancialActivity();
		newFinancialActivity.setId(finAct.getId());
		newFinancialActivity.setFinActivityType(finAct.getFinActivityType());
		newFinancialActivity.setFinActivitySubType(finAct.getFinActivitySubType());
		newFinancialActivity.setFinActivityGrossAmt(finAct.getFinActivityGrossAmt());
		
		//Begin AXAL3.7.28
		ArrayList orgPaymentList = finAct.getPayment();
		newFinancialActivity.setPayment(orgPaymentList);
		Iterator paymentItr = newFinancialActivity.getPayment().iterator();
		while(paymentItr.hasNext()){
			Payment payment = (Payment)paymentItr.next();
			payment.setPaymentAmt(finAct.getFinActivityGrossAmt());
			payment.setCheckDescription(getDescription(finAct.getFinActivitySubType()));
			OLifEExtension olifeExtPayment = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PAYMENT);
			olifeExtPayment.getPaymentExtension().setCheckDestination(NbaConstants.CHECK_DESTINATION);
			payment.addOLifEExtension(olifeExtPayment);
		}
		//End AXAL3.7.28	
		newPolicy.addFinancialActivity(newFinancialActivity);
		disbTxLife1225.getPrimaryHolding().setPolicy(newPolicy);
		if (originalRelation != null) {
			originalParty = NbaTXLife.getPartyFromId(originalRelation.getRelatedObjectID(), olife.getParty());
			generateRelationObject(originalRelation, newOlife);
			
			//Begin AXAL3.7.28
			if (NbaOliConstants.OLI_REL_PAYER == originalRelation.getRelationRoleCode()) {
				insuredRelation = NbaUtils.getRelation(olife, NbaOliConstants.OLI_REL_INSURED);
				ownerRelation = NbaSessionUtils.getRelation(olife, NbaOliConstants.OLI_REL_OWNER);
				Party ownerParty = NbaTXLife.getPartyFromId(ownerRelation.getRelatedObjectID(), olife.getParty());
				if (insuredRelation != null && originalRelation != null && !(insuredRelation.getRelatedObjectID().equals(originalRelation.getRelatedObjectID()))) {
					generatePartyForNewTxLife(insuredRelation, olife, newOlife);
					generateRelationObject(insuredRelation, newOlife);
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
					generatePartyForNewTxLife(ownerRelation, olife, newOlife);
					generateRelationObject(ownerRelation, newOlife);
				}else{
					generatePartyForNewTxLife(ownerRelation, olife, newOlife);
					generateRelationObject(insuredRelation, newOlife);
					generateRelationObject(ownerRelation, newOlife);
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
					generateRelationObject(insuredRelation, newOlife);
				}
				if ( originalParty.getAddressCount()>0){
					finStatement.setMailingAddressID(originalParty.getAddressAt(0).getId());
				}
			}
			else if (NbaOliConstants.OLI_REL_OWNER == originalRelation.getRelationRoleCode()) {
				insuredRelation = NbaUtils.getRelation(olife, NbaOliConstants.OLI_REL_INSURED);
				Party ownerParty = NbaTXLife.getPartyFromId(originalRelation.getRelatedObjectID(), olife.getParty());
				if (insuredRelation != null && !(insuredRelation.getRelatedObjectID().equals(originalRelation.getRelatedObjectID()))) {
					generatePartyForNewTxLife(insuredRelation, olife, newOlife);
					generateRelationObject(insuredRelation, newOlife);
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
				}else{
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
					generateRelationObject(insuredRelation, newOlife);
				}
				finStatement.setMailingAddressID(ownerParty.getAddressAt(0).getId());
			}else{
				generateOriginalPartyforNewTxLife(originalParty, newOlife);
			}
			// End AXAL3.7.28
			finStatement.setRemittancePartyID(originalRelation.getRelatedObjectID());
		}
		paymentItr = newFinancialActivity.getPayment().iterator();
		while(paymentItr.hasNext()){
			Payment payment = (Payment)paymentItr.next();
			payment.setPayeeName(getPayeeNameFromPartyID(nbaTXLife,originalRelation.getRelatedObjectID() ));
			}
		//Begin AXAL3.7.28
		Relation prodRelation = getRelationProducer(olife);
		if (prodRelation != null) {
			generatePartyForNewTxLife(prodRelation, olife, newOlife);
			generateRelationObject(prodRelation, newOlife);
			
		}
		// End AXAL 3.7.28
		
		getLogger().logDebug("Disbursement XML1225: " + disbTxLife1225.toXmlString());
		return disbTxLife1225;
	}*/
	//AXAL3.7.28 New Method
	/**
	 * @param txLife
	 * @param partyId
	 * @return Full Payee Name from the Party
	 */
/*	private String getPayeeNameFromPartyID(NbaTXLife txLife, String partyId){
		String payeeName = "";
		NbaParty nbaParty = txLife.getParty(partyId);
		if(nbaParty.isPerson()){
			if(nbaParty.getFirstName()!= null){
				payeeName = nbaParty.getFirstName();	
			}
			if(nbaParty.getMiddleInitial()!= null){
				payeeName = payeeName.concat(" ");
				payeeName = payeeName.concat(nbaParty.getMiddleInitial());
			}if(nbaParty.getLastName() != null){
				payeeName = payeeName.concat(" ");
				payeeName = payeeName.concat(nbaParty.getLastName());
			}
		}else{
			payeeName = nbaParty.getDBA();
		}
		return payeeName;
	}*/

	//AXAL3.7.28 New Method
	/**
	 * @param typeCode
	 * @return Description from the Type code
	 */
	/*private String getDescription(long typeCode){
		String desc = "";
		if (typeCode == NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND){
			desc = "Partial Refund";
		}else if (typeCode == NbaOliConstants.OLI_FINACTSUB_REFUND){
			desc = "Refund";
		}else if (typeCode == NbaOliConstants.OLI_FINACTSUB_REV){
			desc = "Reversal";
		}
		return desc;
	}*/
	/**
	 * @param originalParty
	 * @param newOlife
	 */
	// AXAL3.7.28 Refactored the Base code for creation of original Party.
/*	private void generateOriginalPartyforNewTxLife(Party originalParty, OLifE newOlife) {
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
			generateAddressObjects(originalParty, newParty);
		}
	}*/

	/**
	 * The method creates Party object in the TXLife 1225 using the relation and the olife passed.
	 * @param insuredRelation
	 * @param olife
	 */
	//AXAL3.7.28 New Method.
	/*private void generatePartyForNewTxLife(Relation relation, OLifE olife, OLifE newOlife) {
		Party party;
		Party newParty = new Party();
		party = NbaTXLife.getPartyFromId(relation.getRelatedObjectID(), olife.getParty());
		generatePartyObject(party, newParty, newOlife);
		if (party != null) {
			PersonOrOrganization insuredPersonOrOrg = party.getPersonOrOrganization();
			PersonOrOrganization newPersonOrOrg = new PersonOrOrganization();
			if (insuredPersonOrOrg != null && insuredPersonOrOrg.isPerson()) {
				generatePersonObject(insuredPersonOrOrg, newPersonOrOrg);
				newParty.setPersonOrOrganization(newPersonOrOrg);
			}
			if (insuredPersonOrOrg != null && insuredPersonOrOrg.isOrganization()) {
				generateOrganizationObject(insuredPersonOrOrg, newPersonOrOrg);
				newParty.setPersonOrOrganization(newPersonOrOrg);
			}
		}
		if(NbaOliConstants.OLI_REL_OWNER == relation.getRelationRoleCode() || NbaOliConstants.OLI_REL_INSURED == relation.getRelationRoleCode()){		//ALS3389
			generateAddressObjects(party, newParty);
		}if(party.hasProducer()){
			 generateCarrierAppointmentObject(party, newParty);
		}
	}*/

	/**
	 * Returns relation object for RelationRole tc'"31" (Payor).  
	 * If no Payor on contract, then user RelationRole tc"8" (Owner). 
	 * If no Owner on contract then use RelationRole tc"32" (Insured).  
	 * If no Insured on contract, then user RelationRole tc="35" (Annuitant)
	 * @param olife OLifE object
	 * @return Relation object
	 */
	/*protected Relation getRelationObject(OLifE olife) {
		Relation relation = null;
		// SPR3290 code deleted
		List relationList = olife.getRelation(); // SPR3290
		long[] relationCodeArr =
			{ NbaOliConstants.OLI_REL_PAYER, NbaOliConstants.OLI_REL_OWNER, NbaOliConstants.OLI_REL_INSURED, NbaOliConstants.OLI_REL_ANNUITANT };
		for (int i = 0; i < relationCodeArr.length; i++) {
			for (int m = 0; m < relationList.size(); m++) {
				if (((Relation) relationList.get(m)).getRelationRoleCode() == relationCodeArr[i]) {
					relation = (Relation) relationList.get(m);
					return relation;
				}
			}
		}
		return relation;
	}*/

	/**
	 * Generates Person object for XML1225 based on Person object available in contract xml
	 * @param originalPersonOrOrg PersonOrOrganization object from contract xml203
	 * @param newPersonOrOrg PersonOrOrganization object
	 */
/*	protected void generatePersonObject(PersonOrOrganization originalPersonOrOrg, PersonOrOrganization newPersonOrOrg) {
		Person originalPerson = originalPersonOrOrg.getPerson();
		Person newPerson = new Person();
		newPerson.setFirstName(originalPerson.getFirstName());
		newPerson.setMiddleName(originalPerson.getMiddleName());
		newPerson.setLastName(originalPerson.getLastName());
		newPerson.setPrefix(originalPerson.getPrefix());
		newPerson.setSuffix(originalPerson.getSuffix());
		newPersonOrOrg.setPerson(newPerson);
	}*/
	/**
	 * Generates Organization object for XML1225 based on Organization object available in contract xml
	 * @param originalPersonOrOrg PersonOrOrganization object from contract xml203
	 * @param newPersonOrOrg PersonOrOrganization object
	 */
/*	protected void generateOrganizationObject(PersonOrOrganization originalPersonOrOrg, PersonOrOrganization newPersonOrOrg) {
		Organization originalOrganization = originalPersonOrOrg.getOrganization();
		Organization newOrganization = new Organization();
		newOrganization.setDBA(originalOrganization.getDBA());
		newPersonOrOrg.setOrganization(newOrganization);
	}*/
	/**
	 * Generates Address objects for XML1225 based on Address objects available in contract xml
	 * @param originalParty Party object from contract xml203
	 * @param newParty Party object
	 */
/*	protected void generateAddressObjects(Party originalParty, Party newParty) {
		for (int i = 0; i < originalParty.getAddressCount(); i++) {
			Address originalAddress = originalParty.getAddressAt(i);
			Address newAddress = new Address();
			newAddress.setId(originalAddress.getId());
			newAddress.setAddressTypeCode(originalAddress.getAddressTypeCode());
			newAddress.setAttentionLine(originalAddress.getAttentionLine());
			newAddress.setLine1(originalAddress.getLine1());
			newAddress.setLine2(originalAddress.getLine2());
			newAddress.setLine3(originalAddress.getLine3());
			newAddress.setCity(originalAddress.getCity());
			newAddress.setAddressStateTC(originalAddress.getAddressStateTC());
			newAddress.setZip(originalAddress.getZip());
			newAddress.setAddressCountryTC(originalAddress.getAddressCountryTC());
			newParty.addAddress(newAddress);
		}
	}*/
	/**
	 * Generates Relation object for XML1225 based on Relation object available in contract xml
	 * @param originalRelation Relation object from contract xml203
	 * @param newOlife OLifE object
	 */
/*	protected void generateRelationObject(Relation originalRelation, OLifE newOlife) {
		Relation newRelation = new Relation();
		newRelation.setId(originalRelation.getId());
		newRelation.setOriginatingObjectID(originalRelation.getOriginatingObjectID());
		newRelation.setRelatedObjectID(originalRelation.getRelatedObjectID());
		newRelation.setOriginatingObjectType(originalRelation.getOriginatingObjectType());
		newRelation.setRelatedObjectType(originalRelation.getRelatedObjectType());
		newRelation.setRelationRoleCode(originalRelation.getRelationRoleCode());
		newOlife.addRelation(newRelation);
	}
*/	/**
	 * Generates Party object for XML1225 based on Party object available in contract xml
	 * @param originalParty Party object from contract xml203
	 * @param newParty Party object
	 * @param newOlife OLifE object
	 */
/*	protected void generatePartyObject(Party originalParty, Party newParty, OLifE newOlife) {
		newParty.setId(originalParty.getId());
		newParty.setPartyTypeCode(originalParty.getPartyTypeCode());
		newParty.setGovtID(originalParty.getGovtID());
		newParty.setGovtIDTC(originalParty.getGovtIDTC());
		newOlife.addParty(newParty);
	}
*/
	//BEGIN AXAL3.7.28
	/**
	 * Returns relation object for RelationRole tc'"37" (Primary Writing Agent).  
	 * If no Primary Writing Agent on contract, then user RelationRole tc"121" (Agency). 
	 * @param olife OLifE object
	 * @return Relation object
	 */
/*	protected Relation getRelationProducer(OLifE olife) {
		Relation relation = null;
		List relationList = olife.getRelation();
		long[] relationCodeArr =
			{ NbaOliConstants.OLI_REL_PRIMAGENT, NbaOliConstants.OLI_REL_AGENCYOF};
		for (int i = 0; i < relationCodeArr.length; i++) {
			for (int m = 0; m < relationList.size(); m++) {
				if (((Relation) relationList.get(m)).getRelationRoleCode() == relationCodeArr[i]) {
					relation = (Relation) relationList.get(m);
					return relation;
				}
			}
		}
		return relation;
	}*/
	
	/**
	 * Generates CarrierAppointmentObject object for XML1225 based on CarrierAppointmentObject object available in contract xml
	 * @param prodParty Party object from contract xml203
	 * @param newParty Party object
	*/
/*	protected void generateCarrierAppointmentObject(Party prodParty, Party newParty) {
	    CarrierAppointment newCarrierAppointment = new CarrierAppointment();
		newCarrierAppointment.setCompanyProducerID(prodParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID());
		if(prodParty.getPartyTypeCode() == NbaOliConstants.OLI_PT_PERSON){
		    CarrierAppointmentExtension newCarrierAppointmentExtension = new CarrierAppointmentExtension();
			CarrierAppointmentExtension originalCarrierAppointmentExtension = NbaUtils.getFirstCarrierAppointmentExtension(prodParty.getProducer().getCarrierAppointmentAt(0));
			if(originalCarrierAppointmentExtension != null){
				newCarrierAppointmentExtension.setASUCode(originalCarrierAppointmentExtension.getASUCode());
			}
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_CARRIERAPPOINTMENT);
		    olifeExt.setCarrierAppointmentExtension(newCarrierAppointmentExtension);
			newCarrierAppointment.addOLifEExtension(olifeExt);
		}
		
		Producer newProducer = new Producer();
		newProducer.addCarrierAppointment(newCarrierAppointment);
		newParty.setProducer(newProducer);
	}*/
	// END AXAL3.7.28
	
	/**
	 * This method send the disbursement extract for CWA to web service.
	 * @param extract508List an ArrayList containg 508 xml extract. 
	 * @throws NbaBaseException
	 * @throws NbaDataAccessException
	 */
	/*// AXAL3.7.28 New Method 
	protected void sendCWAExtract( NbaTXLife txLife1225, NbaUserVO user) throws NbaBaseException {
		AxaInvokeWebservice axaInvokeWebservice = new AxaInvokeWebservice();
		try {
			NbaTXLife responseTxlife = axaInvokeWebservice.invokeAXACheckWritingWS(txLife1225, user);
			AxaInvokeWebservice.handleWebServiceFailure(responseTxlife);
		} catch (NbaBaseException nbe) {
			nbe.printStackTrace();
			throw new NbaBaseException("Unable to send the policy level disbursement extract (1225) to WebService", NbaExceptionType.FATAL);
		}
	}*/

	
}