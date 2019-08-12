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
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.DisbursementStatement;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialStatement;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;

/**
 * This class is responsible for creating request for Contract Print.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.28</td>
 * <td>AXA Life Phase 1</td>
 * <td>Check Writing Interface</td>
 * </tr>
 * <tr>
 * <td>P2AXAL019</td>
 * <td>AXA Life Phase 2</td>
 * <td>Cash Management</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSSendPremiumRefundInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "AccountingService";

	private static final String FUNCTIONID = "CheckwritingService";

	/**
	 * constructor from superclass
	 * 
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSSendPremiumRefundInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	public NbaTXLife createRequest() throws NbaBaseException {
		Relation insuredRelation = null; //AXAL3.7.28
		FinancialActivity finAct = (FinancialActivity) getObject();
		NbaTXRequestVO nbaTXRequestVO = new NbaTXRequestVO();
		nbaTXRequestVO.setTransType(NbaOliConstants.TC_TYPE_ACCOUNTINGSTMTTRANS);
		nbaTXRequestVO.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequestVO.setBusinessProcess("");
		nbaTXRequestVO.setNbaUser(getUserVO()); //AXAL3.7.28
		//create txlife with default request fields
		NbaTXLife disbTxLife1225 = new NbaTXLife(nbaTXRequestVO);
		Party originalParty = null;
		OLifE olife = getNbaTXLife().getOLifE();
		OLifE newOlife = disbTxLife1225.getOLifE();
		Relation originalRelation = getRelationObject(olife);
		FinancialStatement finStatement = new FinancialStatement();
		finStatement.setId("FinancialStatement_1");
		disbTxLife1225.getOLifE().getSourceInfo().setFileControlID(getBackEnd());//P2AXAL019
		disbTxLife1225.getOLifE().addFinancialStatement(finStatement);
		Holding originalHolding = getNbaTXLife().getPrimaryHolding();
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
		newPolicy.setProductCode(originalPolicy.getProductCode()); //ALII1409
		newPolicy.setProductType(originalPolicy.getProductType()); //ALII1409
		//Begin P2AXAL019
		if (NbaConstants.SYST_LIFE70.equalsIgnoreCase(getBackEnd())) {
			newPolicy.setJurisdiction(originalPolicy.getJurisdiction());
			OLifEExtension oLifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(getNbaTXLife().getPolicy());
			if (polExt != null) {
				oLifeExtn.getPolicyExtension().setDistributionChannel(polExt.getDistributionChannel());
			}
			newPolicy.addOLifEExtension(oLifeExtn);
		}
		//End P2AXAL019
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
		while (paymentItr.hasNext()) {
			Payment payment = (Payment) paymentItr.next();
			payment.setPaymentAmt(finAct.getFinActivityGrossAmt());
//			payment.setCheckDescription(getDescription(finAct));//ALS3792 APSL2735 QC11962
			payment.setCheckDescription(NbaUtils.getDescription(finAct, getNbaTXLife()));
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
				insuredRelation = NbaUtils.getRelation(olife, NbaOliConstants.OLI_REL_INSURED,originalHolding.getId());//APSL2240
				/*
				 * ownerRelation = NbaSessionUtils.getRelation(olife, NbaOliConstants.OLI_REL_OWNER); Party ownerParty =
				 * NbaTXLife.getPartyFromId(ownerRelation.getRelatedObjectID(), olife.getParty());
				 */
				if (insuredRelation != null && originalRelation != null
						&& !(insuredRelation.getRelatedObjectID().equals(originalRelation.getRelatedObjectID()))) {
					generatePartyForNewTxLife(insuredRelation, olife, newOlife);
					generateRelationObject(insuredRelation, newOlife);
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
					/*
					 * generatePartyForNewTxLife(ownerRelation, olife, newOlife); generateRelationObject(ownerRelation, newOlife);
					 */
				} else {
					/*
					 * generatePartyForNewTxLife(ownerRelation, olife, newOlife); generateRelationObject(insuredRelation, newOlife);
					 * generateRelationObject(ownerRelation, newOlife);
					 */
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
					generateRelationObject(insuredRelation, newOlife);
				}
				if (originalParty.getAddressCount() > 0) {
					finStatement.setMailingAddressID(originalParty.getAddressAt(0).getId());
				}
			} else if (NbaOliConstants.OLI_REL_OWNER == originalRelation.getRelationRoleCode()) {
				insuredRelation = NbaUtils.getRelation(olife, NbaOliConstants.OLI_REL_INSURED,originalHolding.getId());//APSL2240
				Party ownerParty = NbaTXLife.getPartyFromId(originalRelation.getRelatedObjectID(), olife.getParty());
				if (insuredRelation != null && !(insuredRelation.getRelatedObjectID().equals(originalRelation.getRelatedObjectID()))) {
					generatePartyForNewTxLife(insuredRelation, olife, newOlife);
					generateRelationObject(insuredRelation, newOlife);
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
				} else {
					generateOriginalPartyforNewTxLife(originalParty, newOlife);
					generateRelationObject(insuredRelation, newOlife);
				}
				finStatement.setMailingAddressID(ownerParty.getAddressAt(0).getId());
			} else {
				generateOriginalPartyforNewTxLife(originalParty, newOlife);
			}
			// End AXAL3.7.28
			finStatement.setRemittancePartyID(originalRelation.getRelatedObjectID());
		}
		paymentItr = newFinancialActivity.getPayment().iterator();
		while (paymentItr.hasNext()) {
			Payment payment = (Payment) paymentItr.next();
			payment.setPayeeName(getPayeeNameFromPartyID(getNbaTXLife(), originalRelation.getRelatedObjectID()));
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
	}

	/**
	 * Returns relation object for RelationRole tc'"31" (Payor). If no Payor on contract, then user RelationRole tc"8" (Owner). If no Owner on
	 * contract then use RelationRole tc"32" (Insured). If no Insured on contract, then user RelationRole tc="35" (Annuitant)
	 * 
	 * @param olife
	 *            OLifE object
	 * @return Relation object
	 */
	//	P2AXAL019 Method reformatted
	protected Relation getRelationObject(OLifE olife) {
		// SPR3290 code deleted
		List relationList = olife.getRelation(); // SPR3290
		long[] relationCodeArr = { NbaOliConstants.OLI_REL_PAYER, NbaOliConstants.OLI_REL_OWNER, NbaOliConstants.OLI_REL_INSURED,
				NbaOliConstants.OLI_REL_ANNUITANT };
		for (int i = 0; i < relationCodeArr.length; i++) {
			for (int j = 0; j < relationList.size(); j++) {
				if (((Relation) relationList.get(j)).getRelationRoleCode() == relationCodeArr[i]) {
					if (relationCodeArr[i] == NbaOliConstants.OLI_REL_PAYER) { //Begin ALII1911
						Holding primaryHolding = getNbaTXLife().getPrimaryHolding();
						if (primaryHolding != null && ((Relation) relationList.get(j)).getOriginatingObjectID().equalsIgnoreCase(primaryHolding.getId())) {
							return (Relation) relationList.get(j);
						}
					} else { //End ALII1911
						return (Relation) relationList.get(j);
					}
				}
			}
		}
		return null;
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
		Relation newRelation = new Relation();
		newRelation.setId(originalRelation.getId());
		newRelation.setOriginatingObjectID(originalRelation.getOriginatingObjectID());
		newRelation.setRelatedObjectID(originalRelation.getRelatedObjectID());
		newRelation.setOriginatingObjectType(originalRelation.getOriginatingObjectType());
		newRelation.setRelatedObjectType(originalRelation.getRelatedObjectType());
		newRelation.setRelationRoleCode(originalRelation.getRelationRoleCode());
		newOlife.addRelation(newRelation);
	}

	/**
	 * Returns relation object for RelationRole tc'"37" (Primary Writing Agent). If no Primary Writing Agent on contract, then user RelationRole
	 * tc"121" (Agency).
	 * 
	 * @param olife
	 *            OLifE object
	 * @return Relation object
	 */
	//P2AXAL019 Method reformatted
	protected Relation getRelationProducer(OLifE olife) {
		List relationList = olife.getRelation();
		long[] relationCodeArr = { NbaOliConstants.OLI_REL_PRIMAGENT, NbaOliConstants.OLI_REL_AGENCYOF };
		for (int i = 0; i < relationCodeArr.length; i++) {
			for (int j = 0; j < relationList.size(); j++) {
				if (((Relation) relationList.get(j)).getRelationRoleCode() == relationCodeArr[i]) {
					return (Relation) relationList.get(j);
				}
			}
		}
		return null;
	}

	//	AXAL3.7.28 New Method
	/**
	 * @param txLife
	 * @param partyId
	 * @return Full Payee Name from the Party
	 */
	private String getPayeeNameFromPartyID(NbaTXLife txLife, String partyId) {
		String payeeName = "";
		NbaParty nbaParty = txLife.getParty(partyId);
		if (nbaParty.isPerson()) {
			if (nbaParty.getFirstName() != null) {
				payeeName = nbaParty.getFirstName();
			}
			if (nbaParty.getMiddleInitial() != null) {
				payeeName = payeeName.concat(" ");
				payeeName = payeeName.concat(nbaParty.getMiddleInitial());
			}
			if (nbaParty.getLastName() != null) {
				payeeName = payeeName.concat(" ");
				payeeName = payeeName.concat(nbaParty.getLastName());
			}
		} else {
			payeeName = nbaParty.getDBA();
		}
		return payeeName;
	}

	/**
	 * @param originalParty
	 * @param newOlife
	 */
	// AXAL3.7.28 Refactored the Base code for creation of original Party.
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
			generateAddressObjects(originalParty, newParty);
		}
	}

	/**
	 * The method creates Party object in the TXLife 1225 using the relation and the olife passed.
	 * 
	 * @param insuredRelation
	 * @param olife
	 */
	//AXAL3.7.28 New Method.
	private void generatePartyForNewTxLife(Relation relation, OLifE olife, OLifE newOlife) {
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
		if (NbaOliConstants.OLI_REL_OWNER == relation.getRelationRoleCode() || NbaOliConstants.OLI_REL_INSURED == relation.getRelationRoleCode()) { //ALS3389
			generateAddressObjects(party, newParty);
		}
		if (party.hasProducer()) {
			generateCarrierAppointmentObject(party, newParty);
		}
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
	 * Generates Address objects for XML1225 based on Address objects available in contract xml
	 * 
	 * @param originalParty
	 *            Party object from contract xml203
	 * @param newParty
	 *            Party object
	 */
	protected void generateAddressObjects(Party originalParty, Party newParty) {
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
	}

	/**
	 * Generates Party object for XML1225 based on Party object available in contract xml
	 * 
	 * @param originalParty
	 *            Party object from contract xml203
	 * @param newParty
	 *            Party object
	 * @param newOlife
	 *            OLifE object
	 */
	protected void generatePartyObject(Party originalParty, Party newParty, OLifE newOlife) {
		newParty.setId(originalParty.getId());
		newParty.setPartyTypeCode(originalParty.getPartyTypeCode());
		newParty.setGovtID(originalParty.getGovtID());
		newParty.setGovtIDTC(originalParty.getGovtIDTC());
		newOlife.addParty(newParty);
	}

	/**
	 * Generates CarrierAppointmentObject object for XML1225 based on CarrierAppointmentObject object available in contract xml
	 * 
	 * @param prodParty
	 *            Party object from contract xml203
	 * @param newParty
	 *            Party object
	 */
	protected void generateCarrierAppointmentObject(Party prodParty, Party newParty) {
		CarrierAppointment newCarrierAppointment = new CarrierAppointment();
		newCarrierAppointment.setCompanyProducerID(prodParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID());
		if (prodParty.getPartyTypeCode() == NbaOliConstants.OLI_PT_PERSON) {
			CarrierAppointmentExtension newCarrierAppointmentExtension = new CarrierAppointmentExtension();
			CarrierAppointmentExtension originalCarrierAppointmentExtension = NbaUtils.getFirstCarrierAppointmentExtension(prodParty.getProducer()
					.getCarrierAppointmentAt(0));
			if (originalCarrierAppointmentExtension != null) {
				newCarrierAppointmentExtension.setASUCode(originalCarrierAppointmentExtension.getASUCode());
			}
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_CARRIERAPPOINTMENT);
			olifeExt.setCarrierAppointmentExtension(newCarrierAppointmentExtension);
			newCarrierAppointment.addOLifEExtension(olifeExt);
		}

		Producer newProducer = new Producer();
		newProducer.addCarrierAppointment(newCarrierAppointment);
		newParty.setProducer(newProducer);
	}

	//AXAL3.7.28 New Method
	/**
	 * @param typeCode
	 * @return Description from the Type code
	 */
	//ALS3792 - Updated method body and signature
	//APSL2735 QC11962 Updated Method Signature
	/**
	 * Start :: QC14236/APSL4046
     * Method moved to NbaUtils from AxaWSSendPremiumRefundInvoker, as this method
     * will also be used by AxaWSElectronicPaymentInvoker for Refund of payment.
     */
	/* Method moved to NbaUtils.
	private String getDescription(FinancialActivity finAct) {
		long polStat = getNbaTXLife().getPrimaryHolding().getPolicy().getPolicyStatus();
		long appType = getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo().getApplicationType();//P2AXAL040
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo()); //APSL2735 QC11962
		 //Begin APSL2735 QC11962
		if (appInfoExt != null && appInfoExt.getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYMETH_ETRANS && finAct.getFinActivitySubType() == NbaOliConstants.OLI_FINACTSUB_REV) {
			return NbaConstants.MNY_REVERSAL_ELECTRONIC;
		}//End APSL2735 QC11962
		else if (polStat == NbaOliConstants.OLI_POLSTAT_DECISSUE) {
			return NbaConstants.MNY_REFUND_REASON_DECLINED;
		} else if (polStat == NbaOliConstants.OLI_POLSTAT_INCOMPLETE) {
			return NbaConstants.MNY_REFUND_REASON_INCOMPLETE;
		} else if (polStat == NbaOliConstants.OLI_POLSTAT_DEFERRED) {//ALS3792
			return NbaConstants.MNY_REFUND_REASON_POSTPONED;
		} else if (polStat == NbaOliConstants.OLI_POLSTAT_WITHDRAW) {
			return NbaConstants.MNY_REFUND_REASON_WITHDRAW;
		} else if (polStat == NbaOliConstants.OLI_POLSTAT_NOTAKE) {
			return NbaConstants.MNY_REFUND_REASON_NTO;
		} else if ((polStat == NbaOliConstants.OLI_POLSTAT_APPROVED || polStat == NbaOliConstants.OLI_POLSTAT_ISSUED || polStat == NbaOliConstants.OLI_POLSTAT_PENDING)
				&& isFreeLookExpired()) {
			return NbaConstants.MNY_REFUND_REASON_EXPIRED;
		} else if ((polStat == NbaOliConstants.OLI_POLSTAT_APPROVED || polStat == NbaOliConstants.OLI_POLSTAT_ISSUED || polStat == NbaOliConstants.OLI_POLSTAT_PENDING)
				&& NbaUtils.isNewApplication(appType) && hasMorePayment()) {//P2AXAL040
			return NbaConstants.MNY_REFUND_REASON_OVERPAYMENT;
		} else if ((polStat == NbaOliConstants.OLI_POLSTAT_APPROVED || polStat == NbaOliConstants.OLI_POLSTAT_ISSUED || polStat == NbaOliConstants.OLI_POLSTAT_PENDING)
				&& isContractChange() && hasMorePayment()) {
			return NbaConstants.MNY_REFUND_REASON_CONTRACT_CHANGE;
		}//APSL476 begins
		 else if (polStat == NbaOliConstants.OLI_POLSTAT_PENDING){
			return NbaConstants.MNY_REFUND_REASON_SPLIT_CHECK;
		}//APSL476 ends
		return "";
	}

	//ALS3792 new method
	public boolean isFreeLookExpired() {
		Date signedDate = null;
		RequirementInfo reqInfo = getNbaTXLife().getRequirementInfo(getNbaTXLife().getPrimaryParty(), NbaOliConstants.OLI_REQCODE_POLDELRECEIPT);
		if (reqInfo != null) {
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			if (NbaOliConstants.OLI_REQSTAT_RECEIVED == reqInfo.getReqStatus() && reqInfoExt != null) {
				signedDate = reqInfoExt.getDeliveryReceiptSignDate();
			}
		}
		if (signedDate != null) {
			//if Delivery Receipt Signed Date is within 10 days of the refund
			if (NbaUtils.calcDaysDiff(signedDate, new Date()) < 10) {
				return true;
			}
		}
		return false;
	}
	
	//ALS3792 new method
    public boolean isContractChange() {
        return getNbaTXLife().isReissue();//ALII1206
    }

    //ALS3792 new method
    public boolean hasMorePayment() {
        if (getNbaTXLife().getPrimaryHolding().getPolicy().hasPaymentAmt()
                && getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo().hasCWAAmt()) {
            if (getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo().getCWAAmt() >= getNbaTXLife().getPrimaryHolding().getPolicy()
                    .getPaymentAmt()) {
                return true;
            }//APSL263(ALS5975) begin
            List systemMessageList = getNbaTXLife().getPrimaryHolding().getSystemMessage();
            Iterator i = systemMessageList.iterator();
            while (i.hasNext()) {
                SystemMessage msg = (SystemMessage) i.next();
                if ((msg.getMessageCode() == NbaConstants.MESSAGECODE_CWA_OVERAGE) || (msg.getMessageCode() == NbaConstants.MESSAGECODE_CWA_SHORTAGE)) {
                    return false;
            }
        }
            return true;
        }   //APSL263(ALS5975) end
        return false;
    }
*/
//	End :: QC14236/APSL4046
	//ALS3792 new method
	//Holding.Policy.ApplicationInfo.ApplicationType
	public boolean isNewApplication() {
		if (NbaOliConstants.OLI_APPTYPE_NEW == getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo().getApplicationType()) {
			return true;
		}
		return false;
	}

	
}
