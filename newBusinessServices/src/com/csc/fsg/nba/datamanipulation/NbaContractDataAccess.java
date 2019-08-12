package com.csc.fsg.nba.datamanipulation; //NBA201

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
 *     Confidential. Not for protectedation.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProduct;
import com.csc.fsg.nba.contract.validation.NbaContractValidationCommon;
import com.csc.fsg.nba.contract.validation.NbaContractValidationConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.AccountHolderNameCC;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.AirSportsExp;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AviationExp;
import com.csc.fsg.nba.vo.txlife.AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CriminalConviction;
import com.csc.fsg.nba.vo.txlife.DisabilityHealth;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.FamilyIllness;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.ForeignTravel;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.Intent;
import com.csc.fsg.nba.vo.txlife.IntentExtension;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.LabTesting;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeStyleActivity;
import com.csc.fsg.nba.vo.txlife.MedicalCondition;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.MedicalPrevention;
import com.csc.fsg.nba.vo.txlife.MedicalTreatment;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationFinancialData;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PrescriptionDrug;
import com.csc.fsg.nba.vo.txlife.PriorName;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubstanceUsage;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementDetails;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementInfo;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vo.txlife.UWReqFormsCC;
import com.csc.fsg.nba.vo.txlife.Violation;

/**
 * NbaContractDataAccess is the super class of NbaRetrieveContractData and
 * NbaUpdateContract.  It provides utility functions to access information 
 * in an NbaTXLife object.  	
 * <p>  
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 * <tr><td>NBA059</td><td>Version 3</td><td>Jet Suitability</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architectural Changes</td></tr>
 * <tr><td>NBA036</td><td>Version 3</td><td>nbA Underwriter Workbench Transactions to DB</td></tr>
 * <tr><td>NBA053</td><td>Version 3</td><td>Application Update Enhancement</td></tr>
 * <tr><td>SPR1335</td><td>Version 3</td><td>Vantage Beneficiary Changes</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA072</td><td>Version 3</td><td>Calculations</td></tr>
 * <tr><td>SPR1721</td><td>Version 4</td><td>nbP/nbA Integration (Annuitant Issue)</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Calculations</td></tr>
 * <tr><td>ACP002</td><td>Version 4</td><td>IU-Driver (Drv/Cmn)</td></tr>
 * <tr><td>ACP001</td><td>Version 4</td><td>IU-Lab Result Processing</td></tr>
 * <tr><td>ACP005</td><td>Version 4</td><td>IU-UFS</td></tr>
 *  <tr><td>ACP007</td><td>Version 4</td><td>Medical Screening</td></tr>
 * <tr><td>ACP022</td><td>Version 4</td><td>Foreign Travel</td></tr>
 * <tr><td>ACP019</td><td>Version 4</td><td>nbA IU-DTS into acRequirementsDetermination</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>ACP017</td><td>Version 4</td><td>Key Person</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Coverage</td></tr>
 * <tr><td>ACP016</td><td>Version 4</td><td>Aviation Evaluation</td></tr>
 * <tr><td>ACP008</td><td>Version 4</td><td>Preferred Questionnaire</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit Card Payment and Authorization</td></tr>
 * <tr><td>SPR2399</td><td>Version 5</td><td>Requirements are not getting generated properly on Primary Insured.</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL Contract Evaluation process stopped with Run time error.</td></tr>
 * <tr><td>SPR2722</td><td>Version 6</td><td>Application Entry Creates Multiple Primary Producer Relations Instead of One Primary and Others Additional Writing.</td></tr>
 * <tr><td>NBA126</td><td>Version 6</td><td>Vantage Contract Change</td></tr>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>SPR2590</td><td>Version 6</td><td>Proposed Table Substandard Ratings are not being excluded from premium calculations.</td></tr>
 * <tr><td>NBA164</td><td>Version 6</td><td>nbA Contract Status View Rewrite Project</td></tr> 
 * <tr><td>SPR3179</td><td>Version 6</td><td>Exception encountered at requirement evaluation when a temporary requirement work item is matched to case</td></tr>
 * <tr><td>SPR3231</td><td>Version 6</td><td>Multiple Holdings on Replacement contract cause it to be sent to error queue</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan COde Determination</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3353</td><td>Version 8</td><td>OINK problem handling multiple requirements in the same XML</td></tr>
 * <tr><td>SPR3610</td><td>Version 8</td><td>Correct problems with Insurance Validation edit P145</td></tr>
 * <tr><td>AXAL3.7.07</td><td>Version 8</td><td>Auto Underwriting</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Corresponsence</td></tr>
 * <tr><td>ALCP161</td><td>AXA Life Phase 1</td><td>Ultimate Amounts</td></tr>
 * <tr><td>ALS4029</td><td>AXA Life Phase 1</td><td>QC # 2851 - AXAL3.7.31 Provider Feeds : ExamOne : nbA only recognizing ReqCode tc="496" when it is listed first among the Provider results</td></tr>
 * <tr><td>ALS4028</td><td>AXA Life Phase 1</td><td>QC # 2854  - AXAL3.7.31 Provider Interfaces : nbA not accepting a CRL lab results feed, even with ReqCode = "496" is the first requirement</td></tr>
 * <tr><td>P2AXAL035</td><td>AXA Life Phase 2</td><td>Amendment / Endorsement / Delivery Instructions</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>A2_AXAL003</td><td>AXA Life NewApp</td><td>New Application � Application Entry A2</td></tr>
 * <tr><td>SR641590(APSL2012)</td><td>Discretionary</td><td>SUB-BGA SR</td></tr>
 * <tr><td>A4_AXAL001</td><td>AXA Life NewApp</td><td>New Application � Application Entry A4</td></tr>
 * <tr><td>CR1343973</td><td>Discretionary</td><td>Reinsurance Corr Display</td></tr>
 * <tr><td>APSL3447</td><td>Discretionary</td><td>HVT</td></tr>
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.datamanipulation.NbaRetrieveContractData
 * @see com.csc.fsg.nba.datamanipulation.NbaUpdateContract
 * @since New Business Accelerator - Version 2 
 */

abstract class NbaContractDataAccess implements NbaContractDataAccessConstants, NbaDataFormatConstants {
	protected int applicant_Index = 0;
	protected boolean updateMode;
	protected String productType = null;
	protected Map map_Indices = null;
	protected Map participantRelations = null;
	protected OLifE oLifE = null;
	protected boolean applicationUpdate = false; //NBA053
	protected NbaTXLife nbaTXLife = null; //NBA053
	protected NbaAcdb nbaAcdb = null; //ACN015
	protected AccelProduct nbaProduct = null; // NBA072, NBA237
	Holding holding = null; //NBA139
	protected Map formNameMap = new HashMap(); //P2AXAL035
	protected NbaContractValidationCommon commonVal = null; //P2AXAL016

	/**
	 * This constructor initializes all member variables.
	 */
	protected NbaContractDataAccess() {
		map_Indices = new HashMap();
	}
	/**
	 * Set the party filter based on the RelatedRefID
	 * and RelationRoleCode (if present);
	 * @param aNbaOinkRequest - data request container
	 */
	protected void checkFilterByRelation(NbaOinkRequest aNbaOinkRequest) {
		long relationRoleCode = aNbaOinkRequest.getRelationRoleCode();
		if (relationRoleCode != NbaOinkRequest.noFilterLong) {
			//SPR2399 code deleted
			Relation relation = null;
			String partyId = ""; //NBA036
			String relatedRefID = aNbaOinkRequest.getRelatedRefID();
			int sizeRelation = getOLifE().getRelationCount();
			for (int i = 0; i < sizeRelation; i++) {
				relation = getOLifE().getRelationAt(i);
				String relationRefid = relation.getRelatedRefID();
				if (relationRefid == null) {
					relationRefid = "";
				}
				if (relation.getRelationRoleCode() == relationRoleCode && relationRefid.equals(relatedRefID)) {
					partyId = relation.getRelatedObjectID();
					break;
				}
			}
			if (partyId.length() > 0) {
				int sizeParty = getOLifE().getPartyCount();
				for (int i = 0; i < sizeParty; i++) {
					Party party = getOLifE().getPartyAt(i);
					if (party.getId().equals(partyId)) {
						aNbaOinkRequest.setPartyFilter(i);
						break;
					}
				}
			}
			//SPR2399 code deleted
		}
	}
	/**
	 * Creates an Address object for a particular Party Object, based on Address type.
	 * @param party
	 * @param addressType long
	 */
	protected Address createAddress(Party party, long addressType) {
		int indexOfNewId = 1;
		for (int i = 0; i < party.getAddressCount(); i++) {
			Address address = party.getAddressAt(i);
			if (address.hasId()) {
				int indexOfId = Integer.parseInt(address.getId().substring(ADDRESSID_PREFIX.length()));
				if (indexOfId >= indexOfNewId) {
					indexOfNewId = indexOfId + 1;
				}
			}
		}
		Address address = new Address();
		address.setId(ADDRESSID_PREFIX + indexOfNewId);
		address.setAddressTypeCode(addressType);
		party.addAddress(address);
		return address;
	}
	/**
	 * This function creates a CarrierAppointment object of a specified type.
	 * @param producer com.csc.fsg.nba.vo.txlife.Producer
	 * @param carrierApptTypeCode long
	 * @return int
	 */
	protected int createCarrierAppointment(Producer producer) { //NBA053

		CarrierAppointment carrierAppointment = new CarrierAppointment();
		//deleted code NBA053
		producer.addCarrierAppointment(carrierAppointment);

		return producer.getCarrierAppointmentCount() - 1;
	}
	/**
	 * This method creates a Client object for a specified Party.
	 * @param party com.csc.fsg.nba.vo.txlife.Party
	 */
	protected void createClient(Party party) {

		Client client = new Client();
		client.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_CLIENT));
		party.setClient(client);

	}
	/**
	 * This function creates a Coverage object.
	 * @param life com.csc.fsg.nba.vo.txlife.Life
	 * @param coverageType long
	 * @return int
	 */
	protected int createCoverage(Life life, long coverageType) {
		int index = 0;
		int indexOfNewId = 1;

		Coverage coverage = null;
		//create a new index
		for (int i = 0; i < life.getCoverageCount(); i++) {
			coverage = life.getCoverageAt(i);
			if (coverage.hasId()) {
				index = Integer.parseInt(coverage.getId().substring(COVERAGEID_PREFIX.length()));
				if (index >= indexOfNewId)
					indexOfNewId = index + 1;
			}
		}

		coverage = new Coverage();
		coverage.setId(COVERAGEID_PREFIX + indexOfNewId);
		coverage.setIndicatorCode(coverageType);
		life.addCoverage(coverage);

		return life.getCoverageCount() - 1;
	}
	/**
	 * Creates an EMailAddress object for a particular Party Object, based on Email type.
	 * @param party
	 * @param emailType long
	 */
	protected EMailAddress createEMail(Party party, long addressType) {
		int indexOfNewId = 1;
		for (int i = 0; i < party.getEMailAddressCount(); i++) {
			EMailAddress anEMailAddress = party.getEMailAddressAt(i);
			if (anEMailAddress.hasId()) {
				int indexOfId = Integer.parseInt(anEMailAddress.getId().substring(EMAILID_PREFIX.length()));
				if (indexOfId >= indexOfNewId) {
					indexOfNewId = indexOfId + 1;
				}
			}
		}
		EMailAddress anEMailAddress = new EMailAddress();
		anEMailAddress.setId(EMAILID_PREFIX + indexOfNewId);
		anEMailAddress.setEMailType(addressType);
		party.addEMailAddress(anEMailAddress);
		return anEMailAddress;
	}
	/**
	 * This function creates a FinancialActivity object.
	 * @return int
	 */
	protected int createFinancialActivity() {
		int index = 0;
		int indexOfNewId = 1;
		Policy policy = getPolicy();
		FinancialActivity activity = null;
		//create a new index
		for (int i = 0; i < policy.getFinancialActivityCount(); i++) {
			activity = policy.getFinancialActivityAt(i);
			if (activity.hasId()) {
				index = Integer.parseInt(activity.getId().substring(FINACTIVITYID_PREFIX.length()));
				if (index >= indexOfNewId)
					indexOfNewId = index + 1;
			}
		}
		activity = new FinancialActivity();
		activity.setId(FINACTIVITYID_PREFIX + indexOfNewId);
		policy.addFinancialActivity(activity);
		return policy.getFinancialActivityCount() - 1;
	}

	/**
	 * This function creates a Intent object.
	 * @param partyId java.lang.String
	 * @return int
	 */
	//NBA059 new method
	protected int createIntent(String partyID) {
		int index = 0;
		int indexOfNewId = 1;		
		Intent intent = null;

		//create a new index
		for (int i = 0; i < getHolding().getIntentCount(); i++) {
			intent = getHolding().getIntentAt(i);
			if (intent.hasId()) {
				index = Integer.parseInt(intent.getId().substring(INTENTID_PREFIX.length()));
				if (index >= indexOfNewId)
					indexOfNewId = index + 1;
			}
		}
		intent = new Intent();
		intent.setId(INTENTID_PREFIX + indexOfNewId);
		intent.setPartyID(partyID);
		getHolding().addIntent(intent);
		return getHolding().getIntentCount() - 1;
	}
	/**
	 * This function retrieves a Intent object.
	 * @return com.csc.fsg.nba.vo.txlife.Intemt 
	 * @param elementIndex int
	 * @param partyId String
	 */
	//AXAL3.7.07 new method
	protected IntentExtension getPersonalIntentExtension(int elementIndex, String partyID) {
		Intent intent = null;
		IntentExtension intentExtension = null;
		if (getHolding().getIntentCount() > elementIndex) {
			for (int i = 0; i < getHolding().getIntentCount(); i++) {
				if (getHolding().getIntentAt(i).getPartyID().equalsIgnoreCase(partyID)) {
					intent = getHolding().getIntentAt(i);
					if (intent != null) {
						int index_extension = getExtensionIndex(intent.getOLifEExtension(), INTENT_EXTN);
						if (index_extension != -1) {
							intentExtension = intent.getOLifEExtensionAt(index_extension).getIntentExtension();
							if (intentExtension != null && intentExtension.getIntentCategory() == NbaOliConstants.OLI_INTENT_PERSONAL) {
								break;
							}
						}
					}
				}
			}
		}

		return intentExtension;
	}
	/**
	 * This function retrieves a Intent object.
	 * @return com.csc.fsg.nba.vo.txlife.Intemt 
	 * @param elementIndex int
	 * @param partyId String
	 */
	//AXAL3.7.07 new method
	protected IntentExtension getBusinessIntentExtension(int elementIndex, String partyID) {
		Intent intent = null;
		IntentExtension intentExtension = null;
		if (getHolding().getIntentCount() > elementIndex) {
			for (int i = 0; i < getHolding().getIntentCount(); i++) {
				if (getHolding().getIntentAt(i).getPartyID().equalsIgnoreCase(partyID)) {
					intent = getHolding().getIntentAt(i);
					if (intent != null) {
						int index_extension = getExtensionIndex(intent.getOLifEExtension(), INTENT_EXTN);
						if (index_extension != -1) {
							intentExtension = intent.getOLifEExtensionAt(index_extension).getIntentExtension();
							if (intentExtension != null && intentExtension.getIntentCategory() == NbaOliConstants.OLI_INTENT_BUSINESS) {
								break;
							}
						}
					}
				}
			}
		}

		return intentExtension;
	}
	/**
	 * This method creates a Life or an Annuity object.
	 * @param insuranceType java.lang.String
	 */

	protected void createLifeOrAnnuityOrDisabilityHealth() {

		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty product = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093

		if (productType.equals(PRODUCT_ANNUITY)) {
			product.setAnnuity(new Annuity());
		} else if (productType.equals(PRODUCT_INSURANCE)) {
			product.setLife(new Life());
		}
		getPolicy().setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(product); //NBA093

	}
	/**
	 * This function creates a LifeParticipant object for a specified Coverage.
	 * @param coverage com.csc.fsg.nba.vo.txlife.Coverage
	 * @param lifeParticipantType long
	 * @return int
	 */
	protected int createLifeParticipant(Coverage coverage, long lifeParticipantType) {
		int indexOfId = 0;
		int indexOfNewId = 1;
		LifeParticipant participant = null;
		Coverage temp = null;
		Life life = getLife();

		//create a new index
		for (int i = 0; i < life.getCoverageCount(); i++) {
			temp = life.getCoverageAt(i);

			for (int j = 0; j < temp.getLifeParticipantCount(); j++) {
				participant = temp.getLifeParticipantAt(j);
				if (participant.hasId()) {
					indexOfId = Integer.parseInt(participant.getId().substring(LIFEPARTICIPANTID_PREFIX.length()));
					if (indexOfId >= indexOfNewId)
						indexOfNewId = ++indexOfId;
				}
			}
		}

		participant = new LifeParticipant();
		participant.setId(LIFEPARTICIPANTID_PREFIX + indexOfNewId);
		participant.setLifeParticipantRoleCode(lifeParticipantType);
		coverage.addLifeParticipant(participant);

		return coverage.getLifeParticipantCount() - 1;
	}
	/**
	 * This function creates a MedicalCondition object on top of a Risk object.
	 * @param risk com.csc.fsg.nba.vo.txlife.Risk
	 * @param conditionType long
	 * @return int
	 */
	protected int createMedicalCondition(Risk risk, long conditionType) {
		// SPR3290 code deleted
		MedicalCondition condition = null;
		// SPR3290 code deleted
		NbaTXLife nbaTxlife = getNbaTXLife();
		//NBA103 - removed try catch
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxlife);

		condition = new MedicalCondition();
		nbaOLifEId.setId(condition);
		condition.setConditionType(conditionType);
		condition.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_MEDICALCONDITION));
		risk.addMedicalCondition(condition);

		return risk.getMedicalConditionCount() - 1;
	}
	/**
	 * This function creates a Organization object within a given Organization object
	 * @param party com.csc.fsg.nba.vo.txlife.Party
	 */
	protected void createOrganization(Party party) {
		//create a new PO object
		PersonOrOrganization po = new PersonOrOrganization();
		po.setOrganization(new Organization());
		party.setPersonOrOrganization(po);
	}
	/**
	 * This function creates a Participant object for a specified Payout.
	 * @param payout com.csc.fsg.nba.vo.txlife.Payout
	 * @param paarticipantType long
	 * @return int
	 */
	protected int createParticipant(Payout payout, long participantType) {
		int indexOfId = 0;
		int indexOfNewId = 1;
		Participant participant = null;
		Payout temp = null;
		Annuity annuity = getAnnuity();

		//create a new index
		for (int i = 0; i < annuity.getPayoutCount(); i++) {
			temp = annuity.getPayoutAt(i);

			for (int j = 0; j < temp.getParticipantCount(); j++) {
				participant = temp.getParticipantAt(j);
				if (participant.hasId()) {
					indexOfId = Integer.parseInt(participant.getId().substring(PARTICIPANTID_PREFIX.length()));
					if (indexOfId >= indexOfNewId)
						indexOfNewId = indexOfId + 1;
				}
			}
		}

		participant = new Participant();
		participant.setId(PARTICIPANTID_PREFIX + indexOfNewId);
		participant.setParticipantRoleCode(participantType);
		payout.addParticipant(participant);

		return payout.getParticipantCount() - 1;
	}
	/**
	 * This function creates a Party object.
	 * @param hashKey java.lang.String
	 */
	protected void createParty(String hashKey) {
		// SPR3290 code deleted
		int sizeParty = getOLifE().getPartyCount();
		boolean createRelObject = false;
		Party party = null;
		//NBA053 - begin
		NbaTXLife nbaTxlife = getNbaTXLife();
		//NBA103 - removed try catch
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxlife);
		if (hashKey.startsWith(PARTY_BENEFICIARY) || hashKey.startsWith(PARTY_COBENEFICIARY))
			createRelObject = true;
		//deleted code //NBA053
		//NBA053-end
		getIndices().put(hashKey, new Integer(sizeParty));
		party = new Party();
		nbaOLifEId.setId(party);
		//NBA053 - begin
		if (applicationUpdate) {
			party.setActionAdd();
			if (!party.hasPersonOrOrganization()) {
				createPerson(party);
			}
		}
		//NBA053-end
		getOLifE().addParty(party);
		if (createRelObject) {
			//begin SPR1335
			String origPartyId = null;
			if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				if (productType.equals(PRODUCT_ANNUITY)) {
					origPartyId = getHolding().getId();
				} else {
					origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
				}
			} else {
				origPartyId = getPartyForPrimaryIns().getId();
			}
			//end SPR1335
			int index_relation = createRelation(hashKey, origPartyId, party.getId()); //NBA053
			setActionAdd(getOLifE().getRelationAt(index_relation)); //NBA053
		} else {
			int index_relation = createRelation(hashKey, PRIMARY_HOLDINGID, party.getId()); //NBA053
			setActionAdd(getOLifE().getRelationAt(index_relation)); //NBA053
		}

	}
	/**
	 * This function creates a Payout object.
	 * @param annuity com.csc.fsg.nba.vo.txlife.Annuity
	 * @return int
	 */
	protected int createPayout(Annuity annuity) {
		int index = 0;
		int indexOfNewId = 1;

		Payout payout = null;
		//create a new index
		for (int i = 0; i < annuity.getPayoutCount(); i++) {
			payout = annuity.getPayoutAt(i);
			if (payout.hasId()) {
				index = Integer.parseInt(payout.getId().substring(PAYOUTID_PREFIX.length()));
				if (index >= indexOfNewId)
					indexOfNewId = index + 1;
			}
		}

		payout = new Payout();
		payout.setId(PAYOUTID_PREFIX + indexOfNewId);
		annuity.addPayout(payout);

		return annuity.getPayoutCount() - 1;
	}
	/**
	 * This function creates a Payout object of a specified Payout Type.
	 * @param annuity com.csc.fsg.nba.vo.txlife.Annuity
	 * @param payoutType long
	 * @return int
	 */
	protected int createPayout(Annuity annuity, long payoutType) {
		int index = 0;
		int indexOfNewId = 1;

		Payout payout = null;
		//create a new index
		for (int i = 0; i < annuity.getPayoutCount(); i++) {
			payout = annuity.getPayoutAt(i);
			if (payout.hasId()) {
				index = Integer.parseInt(payout.getId().substring(PAYOUTID_PREFIX.length()));
				if (index >= indexOfNewId)
					indexOfNewId = index + 1;
			}
		}

		payout = new Payout();
		payout.setId(PAYOUTID_PREFIX + indexOfNewId);
		payout.setPayoutType(payoutType);
		annuity.addPayout(payout);

		return annuity.getPayoutCount() - 1;
	}
	/**
	 * This function creates a Person object within a given Person object
	 * @param party com.csc.fsg.nba.vo.txlife.Party
	 */
	protected void createPerson(Party party) {
		//create a new PO object
		PersonOrOrganization po = new PersonOrOrganization();
		po.setPerson(new Person());
		party.setPersonOrOrganization(po);
	}
	/**
	 * Creates an Phone object for a particular Party Object, based on Phone type.
	 * @param party
	 * @param phoneType long
	 */
	protected Phone createPhone(Party party, long phoneType) {
		int indexOfNewId = 1;
		for (int i = 0; i < party.getPhoneCount(); i++) {
			Phone phone = party.getPhoneAt(i);
			if (phone.hasId()) {
				int indexOfId = Integer.parseInt(phone.getId().substring(PHONEID_PREFIX.length()));
				if (indexOfId >= indexOfNewId) {
					indexOfNewId = indexOfId + 1;
				}
			}
		}
		Phone phone = new Phone();
		phone.setId(PHONEID_PREFIX + indexOfNewId);
		phone.setPhoneTypeCode(phoneType);
		party.addPhone(phone);
		return phone;
	}
	/**
	 * This function Creates a Party-to-Party or a Holding-to-Party Relation object.
	 * @param hashKey java.lang.String
	 * @param originatingPartyId java.lang.String
	 * @param relatedPartyId java.lang.String 
	 */
	protected int createRelation(String hashKey, String originatingObjectId, String relatedPartyId) {
		// SPR3290 code deleted
		Relation relation = null;
		OLifEExtension oli = null;
		// SPR3290 code deleted
		//deleted code //NBA053
		//NBA053 - begin
		NbaTXLife nbaTxlife = getNbaTXLife();
		//NBA103 - removed try catch
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxlife);
		//deleted code //NBA053
		//NBA053-end
		//create a relation object for particular participant/annuitant 
		relation = new Relation();
		nbaOLifEId.setId(relation);
		relation.setRelatedObjectID(relatedPartyId);
		relation.setOriginatingObjectID(originatingObjectId);
		relation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		if (originatingObjectId.startsWith(HOLDINGID_PREFIX))
			relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
		else if (originatingObjectId.startsWith(COVERAGEID_PREFIX)) { //Begin-SPR1335
			relation.setOriginatingObjectType(NbaOliConstants.OLI_LIFECOVERAGE);
		} else {
			relation.setOriginatingObjectType(NbaOliConstants.OLI_PARTY);
		}
		//end- SPR1335

		if (hashKey.startsWith(PARTY_ANNUITANT)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_ANNUITANT);
		} else if (hashKey.startsWith(PARTY_INSURED) || hashKey.startsWith(PARTY_PRIM_INSURED)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_INSURED);
		} else if (hashKey.startsWith(PARTY_OTHER_INSURED)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_COVINSURED); //NBA093
		} else if (hashKey.startsWith(PARTY_JOINT_INSURED)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_JOINTINSURED);
		} else if (hashKey.startsWith(PARTY_OWNER) || hashKey.startsWith(PARTY_PRIM_OWNER)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_OWNER);
		} else if (hashKey.startsWith(PARTY_SPOUSE)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_SPOUSE);
		} else if (hashKey.startsWith(PARTY_PAYOR)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_PAYER);
		} else if (hashKey.startsWith(PARTY_PRIWRITINGAGENT)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		} else if (hashKey.startsWith(PARTY_ADDWRITINGAGENT)) { //SPR2722
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_ADDWRITINGAGENT); //SPR2722
		} else if (hashKey.startsWith(PARTY_REQUESTEDBY)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_REQUESTEDBY);
		} else if (hashKey.startsWith(PARTY_CARRIER)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_CARRIER);
		} else if (hashKey.startsWith(PARTY_COBENEFICIARY) || hashKey.startsWith(PARTY_BENEFICIARY)) {
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_BENEFICIARY);
			oli = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RELATION);
			if (hashKey.startsWith(PARTY_BENEFICIARY))
				relation.setRelationRoleCode(NbaOliConstants.OLI_REL_BENEFICIARY);			//NBA093
			else if (hashKey.startsWith(PARTY_COBENEFICIARY))
				relation.setRelationRoleCode(NbaOliConstants.OLI_REL_CONTGNTBENE);			//NBA093
			relation.addOLifEExtension(oli);
		} else if (hashKey.startsWith(CREDIT_CARD_PAYMENT)) { //NBA115
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_PYMT_FACILITATOR); //NBA115 
		}else if (hashKey.startsWith(PARTY_BCM)) { //AXAL3.7.13I 
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_BGACASEMANAGER); //AXAL3.7.13I 
		}else if (hashKey.startsWith(PARTY_BGA)) { //AXAL3.7.13I 
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT); //AXAL3.7.13I 
		}else if (hashKey.startsWith(PARTY_MULTIPLEASSIGNEE)) {//P2AXAL005
            relation.setRelationRoleCode(NbaOliConstants.OLI_REL_ASSIGNEE);//P2AXAL005
		}else if (hashKey.startsWith(PARTY_REPLCOMP)) { //P2AXAL028
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_HOLDINGCO); //P2AXAL028
		}else if (hashKey.startsWith(PARTY_APPLCNT)) { //ALII104
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_APPLICANT); //ALII104
		}
		getOLifE().addRelation(relation);
		return getOLifE().getRelationCount() - 1;
	}
	/**
	 * This function creates a RequirementInfo object.
	 * @return int
	 */
	protected int createRequirementInfo() {
		int index = 0;
		int indexOfNewId = 1;
		Policy policy = getPolicy();
		RequirementInfo aRequirementInfo = null;
		//create a new index
		for (int i = 0; i < policy.getRequirementInfoCount(); i++) {
			aRequirementInfo = policy.getRequirementInfoAt(i);
			if (aRequirementInfo.hasId()) {
				index = Integer.parseInt(aRequirementInfo.getId().substring(REQUIREMENT_PREFIX.length()));
				if (index >= indexOfNewId)
					indexOfNewId = index + 1;
			}
		}
		aRequirementInfo = new RequirementInfo();
		aRequirementInfo.setId(REQUIREMENT_PREFIX + indexOfNewId);
		policy.addRequirementInfo(aRequirementInfo);
		return policy.getRequirementInfoCount() - 1;
	}
	/**
	 * This function creates a Risk object within a specified Party object.
	 * @param party com.csc.fsg.nba.vo.txlife.Party
	 */
	protected void createRisk(Party party) {

		Risk risk = new Risk();
		risk.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RISK));
		party.setRisk(risk);

	}
	/**
	 * Creates an subAccount object for a particular Party Object, based on Activity type.
	 * @param activityType long
	 * @return int
	 */
	protected int createSubAccount(long activityType) {
		int indexOfId = 0;
		int indexOfNewId = 1;
		SubAccount subAccount = null;
		if (!getHolding().hasInvestment()) {
			getHolding().setInvestment(new Investment());
		}
		Investment investment = getHolding().getInvestment();

		//create a new indexOfId
		for (int i = 0; i < investment.getSubAccountCount(); i++) {
			subAccount = investment.getSubAccountAt(i);
			if (subAccount.hasId()) {
				indexOfId = Integer.parseInt(subAccount.getId().substring(SUBACCOUNTID_PREFIX.length()));
				if (indexOfId >= indexOfNewId)
					indexOfNewId = indexOfId + 1;
			}
		}
		subAccount = new SubAccount();
		subAccount.setId(SUBACCOUNTID_PREFIX + indexOfNewId);
		subAccount.setSystematicActivityType(activityType);
		investment.addSubAccount(subAccount);

		return investment.getSubAccountCount() - 1;
	}
	/**
	 * Creates an subAccount object for a particular Party Object.
	 * @return int
	 */
	//NBA059 new method
	protected int createSubAccount() {
		int indexOfId = 0;
		int indexOfNewId = 1;
		SubAccount subAccount = null;
		if (!getHolding().hasInvestment()) {
			getHolding().setInvestment(new Investment());
		}
		Investment investment = getHolding().getInvestment();

		//create a new indexOfId
		for (int i = 0; i < investment.getSubAccountCount(); i++) {
			subAccount = investment.getSubAccountAt(i);
			if (subAccount.hasId()) {
				indexOfId = Integer.parseInt(subAccount.getId().substring(SUBACCOUNTID_PREFIX.length()));
				if (indexOfId >= indexOfNewId)
					indexOfNewId = indexOfId + 1;
			}
		}
		subAccount = new SubAccount();
		subAccount.setId(SUBACCOUNTID_PREFIX + indexOfNewId);
		investment.addSubAccount(subAccount);

		return investment.getSubAccountCount() - 1;
	}

	/**
	 * Find the index of the Party object for the primary insured or annuitant.
	 */
	protected int findPrimaryInsured() {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093
		if (lifeOrAnnuityOrDisabilityHealth != null) {
			String partyId = "";
			if (lifeOrAnnuityOrDisabilityHealth.isAnnuity()) {
				//begin NBA126
				if(getAnnuity().getPayoutCount() > 0){
					ArrayList allParticipants = getAnnuity().getPayoutAt(0).getParticipant();
					for (int i = 0; i < allParticipants.size(); i++) {
						Participant aParticipant = (Participant) allParticipants.get(i);
						if (aParticipant.getParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_ANNUITANT) {
							partyId = aParticipant.getPartyID();
							break;
						}
					}
				} else {
					ArrayList allRelations = getOLifE().getRelation();
					int size = allRelations.size();
					Relation relation = null;
					for (int i = 0; i < size; i++) {
						relation = (Relation) allRelations.get(i);
						if (NbaOliConstants.OLI_REL_ANNUITANT == relation.getRelationRoleCode()) {
							partyId = relation.getRelatedObjectID();
							break;
						}
					}
				}
				//end NBA126
			} else {
				ArrayList allCoverages = getLife().getCoverage();
				covLoop : for (int cov = 0; cov < allCoverages.size(); cov++) {
					Coverage aCoverage = (Coverage) allCoverages.get(cov);
					ArrayList allParticipants = aCoverage.getLifeParticipant();
					for (int i = 0; i < allParticipants.size(); i++) {
						LifeParticipant aParticipant = (LifeParticipant) allParticipants.get(i);
						if (aParticipant.getLifeParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY) {
							partyId = aParticipant.getPartyID();
							cov = allCoverages.size();
							continue covLoop;
						}
					}
				}
			}
			if (partyId.length() > 0) {
				ArrayList allParties = getOLifE().getParty();
				for (int i = 0; i < allParties.size(); i++) {
					Party aParty = (Party) allParties.get(i);
					if (aParty.getId().equals(partyId)) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	/**
	 * Find the index of the Party object for the primary Owner.
	 */
	protected int findPrimaryOwner() {
	    //begin NBA164
		String aPartyId = "";
	    NbaParty nbaParty = getNbaTXLife().getPrimaryOwner();
	    if(nbaParty != null){
	        aPartyId = nbaParty.getID();
	    }
		if (aPartyId != null && aPartyId.length() > 0) {
		    //end NBA164 
			ArrayList allParties = getOLifE().getParty();
			for (int i = 0; i < allParties.size(); i++) {
				Party aParty = (Party) allParties.get(i);
				if (aParty.getId().equals(aPartyId)) {
					return i;
				}
			}
		}
		return -1;
	}
	/**
	 * Locate an Address for a Party based on Address type.
	 * @param party  
	 * @param addressType long
	 * @return com.csc.fsg.nba.vo.txlife.Address
	 */
	protected Address getAddressForType(Party party, long addressType) {
		ArrayList addresses = party.getAddress();
		for (int i = 0; i < addresses.size(); i++) {
			Address address = (Address) addresses.get(i);
			if (address.getAddressTypeCode() == addressType) {
				setActionUpdate(address); //NBA053
				return address;
			}
		}
		if (isUpdateMode()) {
			Address address = createAddress(party, addressType); //NBA053
			setActionAdd(address); //NBA053
			return address; //NBA053
		} else {
			return null;
		}
	}
	/**
	 * This function retrieves an Annuity.  
	 * @return com.csc.fsg.nba.vo.txlife.Annuity
	 */
	protected Annuity getAnnuity() {
		Policy policy = getPolicy();
		Object obj = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents(); //NBA093
		if ((obj == null) || (obj instanceof DisabilityHealth)) {
			if (isUpdateMode()) {
				createLifeOrAnnuityOrDisabilityHealth();
				setActionAdd(policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity()); //NBA053 //NBA093
			} else {
				return null;
			}
		}
		Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity(); //NBA093
		setActionUpdate(annuity); //NBA053
		return annuity;
	}
	/**
	 * This function retrieves an Creates an ApplicationInfo object.
	 * @return com.csc.fsg.nba.vo.txlife.ApplicationInfo
	 */
	protected ApplicationInfo getApplicationInfo() {
		Policy policy = getPolicy();
		ApplicationInfo applInfo = null;
		if (!policy.hasApplicationInfo()) {
			if (isUpdateMode()) {
				policy.setApplicationInfo(new ApplicationInfo());
				setActionAdd(policy.getApplicationInfo()); //NBA053
			} else {
				return null;
			}
		}
		applInfo = policy.getApplicationInfo();
		setActionUpdate(applInfo); //NBA053
		return applInfo;
	}
	/**
	 * This function retrieves or creates an SourceInfo object.
	 * @return com.csc.fsg.nba.vo.txlife.SourceInfo
	 */
	//NBA064 New Method
	protected SourceInfo getSourceInfo() {
		SourceInfo sourceInfo = null;
		if (!oLifE.hasSourceInfo()) {
			if (isUpdateMode()) {
				oLifE.setSourceInfo(new SourceInfo());
				setActionAdd(oLifE.getSourceInfo());  
			} else {
				return null;
			}
		}
		sourceInfo = oLifE.getSourceInfo();
		setActionUpdate(sourceInfo);  
		return sourceInfo;
	}
	/**
	 * This function retrieves an Attachment object.
	 * @return com.csc.fsg.nba.vo.txlife.Policy
	 */
	protected Attachment getAttachment() {
		Attachment anAttachment = null;
		if (getHolding().getAttachment().size() > 0) {
			anAttachment = getHolding().getAttachmentAt(0);
		}
		if (anAttachment == null) {
			if (isUpdateMode()) {
				anAttachment = new Attachment();
				getHolding().addAttachment(anAttachment);
				setActionAdd(anAttachment); //NBA053
			}
		}
		setActionUpdate(anAttachment); //NBA053
		return anAttachment;
	}
	/**
	 * This function retrieves a CarrierAppointment object for a particular Party Object, based on CarrierApptType Code.
	 * OLifE().Party().Producer().CarrierAppointment()
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex - the specific occurrence of Primary Writing Agent to be accessed.
	 * @param carrierApptTypeCode - producer type
	 * @return com.csc.fsg.nba.vo.txlife.CarrierAppointment
	 */
	protected CarrierAppointment getCarrierAppointment(NbaOinkRequest aNbaOinkRequest, int elementIndex) { //NBA053
		CarrierAppointment carrierAppointment = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			if (!party.hasProducer()) {
				party.setProducer(new Producer());
				setActionAdd(party.getProducer()); //NBA053
			}
			Producer producer = party.getProducer();
			int sizeCarrierAppointment = producer.getCarrierAppointmentCount();
			for (int i = 0; i < sizeCarrierAppointment; i++) {
				carrierAppointment = producer.getCarrierAppointmentAt(i);
				//removed CarrierApptTypeCode as a condition for CarrierAppointment.  TypeCode no longer in Accord 2.8 model
				//NBA053 code deleted
				setActionUpdate(carrierAppointment); //NBA053
				setActionUpdate(producer); //NBA053
				return carrierAppointment;
				//NBA053 code deleted
				//NBA053 code deleted 
			}
			if (isUpdateMode()) {
				int index = createCarrierAppointment(producer); //NBA053
				setActionAdd(producer.getCarrierAppointmentAt(index)); //NBA053
				carrierAppointment = producer.getCarrierAppointmentAt(index);
			}
		}

		return carrierAppointment;
	}
	/**
	 * This function retrieves a Client object for a particular Party Object.
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Client
	 */
	protected Client getClient(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		Client client = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			if (!party.hasClient()) {
				if (isUpdateMode()) {
					createClient(party);
					setActionAdd(client); //NBA053
				} else {
					return null;
				}
			}
			client = party.getClient();
		}
		setActionUpdate(client); //NBA053
		return client;
	}
	
	/**
	 * This function retrieves a Coverage object.
	 * @param typeCode long
	 * @return com.csc.fsg.nba.vo.txlife.Coverage
	 */

	protected Coverage getCoverage(long coverageType) {
		int index;
		int sizeCoverage = 0;
		Life life = getLife();
		Coverage coverage = null;

		if (life != null) {
			sizeCoverage = life.getCoverageCount();
			for (int i = 0; i < sizeCoverage; i++) {
				coverage = life.getCoverageAt(i);
				if (coverage.getIndicatorCode() == coverageType) {
					return coverage;
				}
				coverage = null;
			}

			if (isUpdateMode()) {
				index = createCoverage(life, coverageType);
				coverage = life.getCoverageAt(index);
				setActionAdd(coverage); //NBA053
			}
		}
		setActionUpdate(coverage); //NBA053
		return coverage;
	}
	/**
	 * This function retrieves a Coverage object.
	 * @param lifeCovTypeCode long
	 * @return com.csc.fsg.nba.vo.txlife.Coverage
	 */
	// ACP002 new method
	protected Coverage getCoverageByTypeCode(long lifeCovTypeCode) {
		// SPR3290 code deleted
		int sizeCoverage = 0;
		Life life = getLife();
		Coverage coverage = null;

		if (life != null) {
			sizeCoverage = life.getCoverageCount();
			for (int i = 0; i < sizeCoverage; i++) {
				coverage = life.getCoverageAt(i);
				if (coverage.getLifeCovTypeCode() == lifeCovTypeCode) {
					return coverage;
				}
				coverage = null;
			}
		}
		return coverage;
	}	
	
	/**
	 * This function retrieves a Coverage object.
	 * @param id String
	 * @return com.csc.fsg.nba.vo.txlife.Coverage
	 */
	// ACP002 new method
	protected Coverage getCoverage(String id) {
		// SPR3290 code deleted
		int sizeCoverage = 0;
		Life life = getLife();
		Coverage coverage = null;

		if (life != null) {
			sizeCoverage = life.getCoverageCount();
			for (int i = 0; i < sizeCoverage; i++) {
				coverage = life.getCoverageAt(i);
				if (coverage.getId().equals(id)) {
					return coverage;
				}
				coverage = null;
			}
		}
		return coverage;
	}	
	
	/**
	 * This function retrieves a Base Coverage object.
	 * @return com.csc.fsg.nba.vo.txlife.Coverage
	 */
	// NBA072 New Method	
	protected Coverage getBaseCoverage() {
		//NBA104 deleted code
		Life life = getLife();
		Coverage coverage = null;
		if (life != null) {
			int sizeCoverage = life.getCoverageCount();  //NBA104
			for (int i = 0; i < sizeCoverage; i++) {
				coverage = life.getCoverageAt(i);
				if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
					return coverage;
				}
				//NBA104 deleted code
			}
		}	
		return null;  //NBA104
	}

	/**
	 * This function retrieves a Rider  object.
	 * @param riderFilter long
	 * @return com.csc.fsg.nba.vo.txlife.Coverage
	 */
//		NBA072 New Method	

			protected Coverage getRider(long riderFilter) {
				// SPR3290 code deleted
				int sizeCoverage = 0;
				// SPR3290 code deleted
				Life life = getLife();
				Coverage rider = null;

				if (life != null) {
					sizeCoverage = life.getCoverageCount();
					for (int i = 0; i < sizeCoverage; i++) {
						rider = life.getCoverageAt(i);
						if ((rider.getIndicatorCode() == NbaOliConstants.OLI_COVIND_RIDER || rider.getIndicatorCode() == NbaOliConstants.OLI_COVIND_INTEGRATED)
								&& rider.getProductCode() != getBaseCoverage().getProductCode()) { //NBA237
							return rider;
							
						}
						rider = null;
					}
				}	
				return rider;
			}	
			
	/**
	 * This function retrieves a list of coverages other than Base and increase coverages.
	 * @return com.csc.fsg.nba.vo.txlife.Coverage
	 */
	// NBA072 New Method
	// NBA104 changed return type
		protected List getRider() {
			// NBA104 deleted code
			ArrayList covRiders = new ArrayList(); //NBA104
			Coverage rider = null;
			Life life = getLife(); //NBA104
			if (life != null && !life.isActionDelete()) { //NBA104
				int sizeCoverage = life.getCoverageCount(); //NBA104
				Coverage baseCoverage = getBaseCoverage(); //NBA100
				String baseProductCode = baseCoverage.getProductCode(); //NBA100
				LifeParticipant primaryInsured = NbaUtils.getInsurableLifeParticipant(baseCoverage); //NBA100
				String primaryPartyID = primaryInsured.getPartyID(); //NBA100
				for (int i = 0; i < sizeCoverage; i++) {
					rider = life.getCoverageAt(i);
					if (rider.getIndicatorCode() == NbaOliConstants.OLI_COVIND_RIDER && !rider.isActionDelete()) { //NBA104	
						LifeParticipant insured = NbaUtils.getInsurableLifeParticipant(rider); //NBA100
						if (!rider.getProductCode().equals(baseProductCode) || !insured.getPartyID().equals(primaryPartyID)) { //NBA100
							covRiders.add(rider);
						}
					}
				}
			} //NBA100
			return covRiders;
		}
			
	/**
	 * This function retrieves a list of Base and increase coverages.
	 * @return list of com.csc.fsg.nba.vo.txlife.Coverage
	 */
	// NBA072 New Method
	// NBA104 changed return type
	protected List getNonRider() {
		// NBA104 deleted code
		ArrayList nonCovRiders = new ArrayList(); //NBA104
		Coverage nonRider = null;
		Life life = getLife(); //NBA104
		if (life != null && !life.isActionDelete()) { //NBA104
			int sizeCoverage = life.getCoverageCount(); //NBA104
			Coverage baseCoverage = getBaseCoverage(); //NBA100
			String baseProductCode = baseCoverage.getProductCode(); //NBA100
			LifeParticipant primaryInsured = NbaUtils.getInsurableLifeParticipant(baseCoverage); //NBA100
			String primaryPartyID = primaryInsured.getPartyID(); //NBA100
			for (int i = 0; i < sizeCoverage; i++) {
				nonRider = life.getCoverageAt(i);
				if (!nonRider.isActionDelete()) { //NBA100
					if (nonRider.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) { //NBA100
						nonCovRiders.add(nonRider);
					} else { //NBA100
						LifeParticipant insured = NbaUtils.getInsurableLifeParticipant(nonRider); //NBA100
						if (nonRider.getProductCode().equals(baseProductCode) && insured.getPartyID().equals(primaryPartyID)) { //NBA100
							nonCovRiders.add(nonRider); //NBA100
						}
					}
				}
			} //NBA100
		} //NBA100
		return nonCovRiders;
	}
	/**
	 * This function retrieves a CovOption object.
	 * @param typeCode long
	 * @param coverage object
	 * @return com.csc.fsg.nba.vo.txlife.covOption
	 */
	// NBA072 New Method	
	protected CovOption getCovOption(Coverage coverage, long covOptionType) {
		// NBA104 deleted code
		CovOption covOption = null;

		// NBA104 deleted code
		int sizeCovOption = coverage.getCovOptionCount();  // NBA104
		for (int i = 0; i < sizeCovOption; i++) {
			covOption = coverage.getCovOptionAt(i);
			if (covOption.getLifeCovOptTypeCode() == covOptionType) {
				return covOption;
			}
			// NBA104 deleted code
		}
		// NBA104 deleted code
		return null;  // NBA104
	}
	/**
	 * Return a list containing the Coverages or Riders based on the Qualifier value in 
	 * the NbaOinkRequest.
	 * @param aNbaOinkRequest - data request container
	 * @return list of benefits
	 */
	// NBA104 New Method	
	protected List getCovOptions(NbaOinkRequest aNbaOinkRequest) {
		String qualifier = aNbaOinkRequest.getQualifier();
		if (ACCIDENTAL_DEATH_BENEFIT.equals(qualifier)){
			return getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
		}
		return getCovOptions();
	}
	/**
	 * Returns a list of all CovOptions on a contract.
	 * @return list of benefits
	 */
	// NBA104 New Method	
	protected List getCovOptions() {
		
		ArrayList dpwFirstBenefitList = new ArrayList(); //ALII1454
		ArrayList covOptions = new ArrayList();

		Life life = getLife();
		if (life != null && !life.isActionDelete()) {
			int sizeCoverage = life.getCoverageCount();
			for (int i = 0; i < sizeCoverage; i++) {
				Coverage coverage = life.getCoverageAt(i);
				if (coverage.isActionDelete()) {
					continue;
				}
				int sizeCovOption = coverage.getCovOptionCount();
				for (int j = 0; j < sizeCovOption; j++) {
					CovOption covOption = coverage.getCovOptionAt(j);
					if (!covOption.isActionDelete()) {
						covOptions.add(covOption);
						if(covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_WP){ //ALII1454
							dpwFirstBenefitList.add(covOption);
						}
					}
				}
			}
		} else {
			Annuity annuity = getAnnuity();
			if (annuity != null && !annuity.isActionDelete()) {
				int sizeRiders = annuity.getRiderCount();
				for (int i = 0; i < sizeRiders; i++) {
					Rider rider = annuity.getRiderAt(i);
					if (rider.isActionDelete()) {
						continue;
					}
					int sizeCovOption = rider.getCovOptionCount();
					for (int j = 0; j < sizeCovOption; j++) {
						CovOption covOption = rider.getCovOptionAt(j);
						if (!covOption.isActionDelete()) {
							covOptions.add(covOption);
						}
					}
				}
			}
		}
		
		//ALII1454 start
		if(dpwFirstBenefitList.size() > 0) {
			for (int j = 0; j < covOptions.size(); j++) {
				CovOption covOption = (CovOption) covOptions.get(j);
				if(covOption.getLifeCovOptTypeCode() != NbaOliConstants.OLI_OPTTYPE_WP){ 
					dpwFirstBenefitList.add(covOption);
				}
			}
			return dpwFirstBenefitList;
		}
		//ALII1454 end
		
		return covOptions;
	}
	/**
	 * Returns a list of specific CovOptions on a contract.
	 * @return list of benefits
	 */
	// NBA104 New Method	
	protected List getCovOptions(long covOptionType) {
		ArrayList covOptions = new ArrayList();
		List allCovOptions = getCovOptions();
		
		int sizeAll = allCovOptions.size();
		for (int i = 0; i < sizeAll; i++) {
			CovOption covOption = (CovOption) allCovOptions.get(i);
			if (covOption.getLifeCovOptTypeCode() == covOptionType) {
				covOptions.add(covOption);
			}
		}
		return covOptions;
	}
	/**
	 * This function retrieves a Coverage object.
	 * @param typeCode long
	 * @param start int
	 * @return com.csc.fsg.nba.vo.txlife.Coverage
	 */

	protected Coverage getCoverage(long coverageType, int start) {
		int index;
		int sizeCoverage = 0;
		int lookingFor = 0;
		Life life = getLife();
		Coverage coverage = null;

		if (life != null) {
			sizeCoverage = life.getCoverageCount();
			for (int i = 0; i < sizeCoverage; i++) {
				coverage = life.getCoverageAt(i);
				if (coverage.getIndicatorCode() == coverageType) {
					if (start == lookingFor) {
						return coverage;
					} else {
						lookingFor++;
					}
				}
				coverage = null;
			}

			if (isUpdateMode()) {
				index = createCoverage(life, coverageType);
				coverage = life.getCoverageAt(index);
				setActionAdd(coverage); //NBA053
			}
		}
		setActionUpdate(coverage); //NBA053
		return coverage;
	}
	/**
	 * Locate the the next coverage with a life participant
	 * matching the supplied party id.
	 * @param personPartyID - the party id
	 * @param start - the starting index
	 * @return the index of the coverage 
	 */
	protected int getCoverageForParty(NbaOinkRequest aNbaOinkRequest, int start) {
		Life aLife = getLife();
		if (aLife != null) {
			boolean baseCov = false;
			if (aNbaOinkRequest.getQualifier().equals(BASE_COV)) {
				baseCov = true;
				aNbaOinkRequest.setQualifier(PARTY_PRIM_INSURED);
				aNbaOinkRequest.setCount(1);
			}
			int covSearchInx = 0;
			int covFoundIndx = 0; //ACP002
			int covIndx;
			if (!baseCov) {
				if (aNbaOinkRequest.getCoverageFilter() == NbaOinkRequest.noFilterLong) {
					covSearchInx = start;
					covFoundIndx = start; //ACP002
				} else {
					covSearchInx = aNbaOinkRequest.getCoverageFilter();
				}
			}
			//begin SPR1778
			int partyIdx = 0;
			if (aNbaOinkRequest.getPartyFilter() != NbaOinkRequest.noFilterLong) {
				partyIdx = aNbaOinkRequest.getPartyFilter();
			}
			//begin NBA100 
			Party party = getParty(aNbaOinkRequest, partyIdx);
			String personPartyID = "NONE";	
			if (party != null) {
				personPartyID = (party.getId());
			}
			//begin SPR2652
			boolean useJoint =
				PARTY_JOINT_INSURED.equals(aNbaOinkRequest.getQualifier())
					|| NbaOliConstants.OLI_REL_JOINTINSURED == aNbaOinkRequest.getRelationRoleCode();
			//end SPR2652
			//end NBA100
			//end SPR1778
			ArrayList coverages = aLife.getCoverage();
			covLoop : for (covIndx = start; covIndx < coverages.size(); covIndx++) {
				Coverage aCoverage = (Coverage) coverages.get(covIndx);
				//begin NBA100 				
				if (baseCov) {
					if (aCoverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
						return covIndx;
					}
				} else {
					//LifeParticipant aLifeParticipant = NbaUtils.findInsuredLifeParticipant(aCoverage, useJoint);
					LifeParticipant aLifeParticipant = NbaUtils.findInsuredLifeParticipant(aCoverage, personPartyID);  //AXAL3.7.07
					if (aLifeParticipant != null) {
						if (personPartyID.equals(aLifeParticipant.getPartyID())) {
							if (covFoundIndx >= covSearchInx) { //ACP002
								return covIndx;
							}
							++covFoundIndx; //ACP002
						}
					}
				}
				//end NBA100						
			}
			if (isUpdateMode()) {
				LifeParticipant aLifeParticipant;
				if (baseCov) {
					covIndx = createCoverage(aLife, NbaOliConstants.OLI_COVIND_BASE);
					setActionAdd(aLife.getCoverageAt(covIndx)); //NBA053
					aLifeParticipant = getLifeParticipant(aLife.getCoverageAt(covIndx), NbaOliConstants.OLI_PARTICROLE_PRIMARY);
				} else {
					covIndx = createCoverage(aLife, NbaOliConstants.OLI_COVIND_RIDER);
					setActionAdd(aLife.getCoverageAt(covIndx)); //NBA053
					aLifeParticipant = getLifeParticipant(aLife.getCoverageAt(covIndx), NbaOliConstants.OLI_PARTICROLE_OTHINSURED);
				}
				aLifeParticipant.setPartyID(personPartyID);
				setActionUpdate(aLife.getCoverageAt(covIndx)); //NBA053
				return covIndx;
			}
		}
		return -1;
	}
	/**
	 * Locate an EMailAddress for a Party based on Email type.
	 * @param party  
	 * @param emailType long
	 * @return com.csc.fsg.nba.vo.txlife.EMailAddress
	 */
	protected EMailAddress getEMailForType(Party party, long emailType) {
		ArrayList eMailAddresses = party.getEMailAddress();
		for (int i = 0; i < eMailAddresses.size(); i++) {
			EMailAddress address = (EMailAddress) eMailAddresses.get(i);
			if (address.getEMailType() == emailType) {
				setActionUpdate(address); //NBA053
				return address;
			}
		}
		if (isUpdateMode()) {
			EMailAddress address = createEMail(party, emailType); //NBA053
			setActionAdd(address); //NBA053
			return address; //NBA053
		} else {
			return null;
		}
	}
	/**
	 * This function retrieves an Extension index depending upon index type.
	 * @return int
	 * @param list java.util.List
	 * @param extensionType java.lang.String
	 */

	protected int getExtensionIndex(List list, String extensionType) {

		OLifEExtension extension = null;

		for (int i = 0; i < list.size(); i++) {
			extension = (OLifEExtension) list.get(i);

			if (extensionType.equals(CLIENT_EXTN)) {
				if (extension.getClientExtension() != null)
					return i;
			} else if (extensionType.equals(PERSON_EXTN)) {
				if (extension.getPersonExtension() != null)
					return i;
			} else if (extensionType.equals(APPLICATIONINFO_EXTN)) {
				if (extension.getApplicationInfoExtension() != null)
					return i;
			} else if (extensionType.equals(RISK_EXTN)) {
				if (extension.getRiskExtension() != null)
					return i;
			} else if (extensionType.equals(MEDICALCONDITION_EXTN)) {
				if (extension.getMedicalConditionExtension() != null)
					return i;
			} else if (extensionType.equals(POLICY_EXTN)) {
				if (extension.getPolicyExtension() != null)
					return i;

			} else if (extensionType.equals(FINACTIVITY_EXTN)) {
				if (extension.getFinancialActivityExtension() != null)
					return i;
			} else if (extensionType.equals(PRODUCER_EXTN)) {
				if (extension.getRelationProducerExtension() != null)
					return i;
			//begin	ACP001
			} else if (extensionType.equals(MEDICALEXAM_EXTN)) { 
				if (extension.getMedicalExamExtension() != null)
					return i;
			}
			//end ACP001
			//begin ACP002
			else if (extensionType.equals(SUBSTANCEUSAGE_EXTN)) {
				if (extension.getSubstanceUsageExtension() != null)
					return i;
			}
			//end ACP002
			//begin ACP005
			else if (extensionType.equals(FORMINSTANCE_EXTN)) {
				if (extension.getFormInstanceExtension() != null)
					return i;
			}
			//end ACP005
			//NBA093 deleted 6 lines
			//begin ACP009 
			else if (extensionType.equals(REQUIREMENTINFO_EXTN)) {
				if (extension.getRequirementInfoExtension() != null)
					return i;
			}//end ACP009	
			//ACN017 Begins
			else if (extensionType.equals(PARTY_EXTN)) {
							if (extension.getPartyExtension() != null)
								return i;
						}
			//PARTY_EXTN
			// Sub
			else if (extensionType.equals(LIFE_PARTICIPANT_EXTN)) {
				if (extension.getPartyExtension() != null)
				return i;
			}
		 	else if (extensionType.equals(COV_OPTION_EXTN)){
		 		if (extension.getCovOptionExtension()!=null){
		 			return i;			
		 		}
		 	}
			else if (extensionType.equals(MEDICAL_EXAM_EXTENSION)){
				if (extension.getMedicalExamExtension()!= null){
					return i;			
				}
			}
			// begin AXAL3.7.07
			else if (extensionType.equals(INTENT_EXTN)){
				if (extension.getIntentExtension()!= null){
					return i;			
				}
			}
			// end AXAL3.7.07
			// begin ALCP161
			else if (extensionType.equals(HHFAMILYINSURANCE_EXTN)){
				if (extension.getHHFamilyInsuranceExtension()!= null){
					return i;			
				}
			}
			// end ALCP161	
			//begin APSL1759 
			else if (extensionType.equals(ACTIVITY_EXTN)){
				if (extension.getActivityExtension()!= null){
					return i;			
				}
			}
			//end APSL1759
			
		}

		return -1;
	}
	/**
	 * This function retrieves a FinancialActivity object.
	 * @return com.csc.fsg.nba.vo.txlife.FinancialActivity 
	 * @param elementIndex int
	 */

	protected FinancialActivity getFinancialActivity(int elementIndex) {
		FinancialActivity activity = null;
		Policy policy = getPolicy();

		if (policy != null) {
			if (policy.getFinancialActivityCount() > elementIndex) {
				activity = policy.getFinancialActivityAt(elementIndex);
				setActionUpdate(activity); //NBA053
			}

			if (activity == null) {
				if (isUpdateMode()) {
					int index = createFinancialActivity();
					activity = policy.getFinancialActivityAt(index);
					setActionAdd(activity); //NBA053
				}
			}

		}

		return activity;
	}
	/**
	 * This function retrieves a Intent object.
	 * @return com.csc.fsg.nba.vo.txlife.Intemt 
	 * @param elementIndex int
	 * @param partyId String
	 */
	//NBA059 new method
	protected Intent getIntent(int elementIndex, String partyID) {
		Intent intent = null;if (getHolding().getIntentCount() > elementIndex) {
			for (int i = 0; i < getHolding().getIntentCount(); i++) {
				if (getHolding().getIntentAt(i).getPartyID().equalsIgnoreCase(partyID)) {
					intent = getHolding().getIntentAt(i);
					setActionUpdate(intent); //NBA053
					break;
				}

			}
		}

		if (intent == null) {
			if (isUpdateMode()) {
				int index = createIntent(partyID);
				intent = getHolding().getIntentAt(index);
				setActionAdd(intent); //NBA053
			}
		}

		return intent;
	}
	/**
	 * This object retrieve XML 103 object indices.
	 * @return java.util.Map
	 */
	protected Map getIndices() {
		return map_Indices;
	}
	/**
	 * This function retrieves a Life object.
	 * @return com.csc.fsg.nba.vo.txlife.Life
	 */
	protected Life getLife() {
		Policy policy = getPolicy();
		Object obj = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents(); //NBA093
		if ((obj == null) || (obj instanceof DisabilityHealth)) {
			if (isUpdateMode()) {
				createLifeOrAnnuityOrDisabilityHealth();
				setActionAdd(policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife()); //NBA053 //NBA093
			} else {
				return null;
			}
		}
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife(); //NBA093
		setActionUpdate(life);
		return life;
	}
	/**
	 * This function creates a LifeParticipant object for a specified Coverage.
	 * @return com.csc.fsg.nba.vo.txlife.LifeParticipant
	 * @param coverage com.csc.fsg.nba.vo.txlife.Coverage
	 * @param lifeParticipantType long 
	 */

	protected LifeParticipant getLifeParticipant(Coverage coverage, long lifeParticipantType) {
		int index;
		int sizeParticipant = 0;
		LifeParticipant participant = null;

		if (coverage != null) {
			sizeParticipant = coverage.getLifeParticipantCount();
			for (int i = 0; i < sizeParticipant; i++) {
				participant = coverage.getLifeParticipantAt(i);
				if (participant.getLifeParticipantRoleCode() == lifeParticipantType) {
					return participant;
				}
				participant = null;
			}

			if (isUpdateMode()) {
				index = createLifeParticipant(coverage, lifeParticipantType);
				participant = coverage.getLifeParticipantAt(index);
				setActionAdd(participant);
			}

		}
		setActionUpdate(participant);
		return participant;
	}
	/**
	 * This function retrieves a MedicalCondition object within a Risk
	 * for the specified condition type.
	 * @param risk
	 * @param conditionType 
	 * @return com.csc.fsg.nba.vo.txlife.MedicalCondition
	 */
	protected MedicalCondition getMedicalCondition(Risk risk, long conditionType) {
		MedicalCondition condition = null;
		int index = 0;
		if (risk != null) {
			for (int i = 0; i < risk.getMedicalConditionCount(); i++) {
				condition = risk.getMedicalConditionAt(i);
				if (risk.getMedicalConditionAt(i).getConditionType() == conditionType) {
					setActionUpdate(condition); //NBA053
					return condition;
				}
			}
			if (isUpdateMode()) {
				index = createMedicalCondition(risk, conditionType);
				condition = risk.getMedicalConditionAt(index);
				setActionAdd(condition); //NBA053
				return condition;
			}
		}
		return null;
	}
	/**
	 * This function generates a new unique key for Maps.
	 * @return java.lang.String
	 * @param map java.util.Map
	 * @param keyPreffix java.lang.String
	 */
	protected String getNewKey(Map map, String keyPreffix) {
		int i = 0;
		StringBuffer key = new StringBuffer();
		key.append(keyPreffix);
		key.append(i);
		while (map.containsKey(key.toString())) {
			key.setLength(0);
			key.append(keyPreffix);
			key.append(++i);
		}
		return key.toString();
	}
	/**
	 * Answer a reference of the OLife object.
	 */
	protected OLifE getOLifE() {
		return oLifE;
	}
	//New Method NBA053
	/**
	 * Answer a reference of the OLife object.
	 */
	protected NbaTXLife getNbaTXLife() {
		return nbaTXLife;
	}

	/**
	 * This function retrieves a Organization object for a specified Party
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Person
	 */
	protected Organization getOrganization(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		Organization organization = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			Object obj = party.getPersonOrOrganization$Contents();
			if ((obj == null) || (obj instanceof Person)) {
				if (isUpdateMode()) {
					createOrganization(party);
					setActionAdd(organization); //NBA053
				} else {
					return null;
				}
			}

			organization = party.getPersonOrOrganization().getOrganization();
		}
		setActionUpdate(organization); //NBA053

		return organization;
	}
	/**
	 * This function creates a Participant object for a specified Payout.
	 * @return com.csc.fsg.nba.vo.txlife.Participant
	 * @param payout com.csc.fsg.nba.vo.txlife.Payout
	 * @param participantType long 
	 */

	protected Participant getParticipant(Payout payout, long participantType) {
		int index;
		int sizeParticipant = 0;
		Participant participant = null;

		if (payout != null) {
			sizeParticipant = payout.getParticipantCount();
			for (int i = 0; i < sizeParticipant; i++) {
				participant = payout.getParticipantAt(i);
				if (participant.getParticipantRoleCode() == participantType) {
					setActionUpdate(participant); //NBA053
					return participant;
				}
				participant = null;
			}

			if (isUpdateMode()) {
				index = createParticipant(payout, participantType);
				participant = payout.getParticipantAt(index);
				setActionAdd(participant); //NBA053
			}

		}

		return participant;
	}
	/**
	 * This function retrieves a Party object.
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Party
	 */
	//ALII104 Code Refactored
	protected Party getParty(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		String qualifier = aNbaOinkRequest.getQualifier();
		int occurrence = elementIndex;
		checkFilterByRelation(aNbaOinkRequest);
		StringBuffer hashKey = new StringBuffer();
		hashKey.append(aNbaOinkRequest.getQualifier());//ALII124
		hashKey.append(elementIndex);//ALII124
		if (qualifier.length() > 0) {
			//Begin P2AXAL024, ALII484
			if (aNbaOinkRequest.getPartyFilter() != NbaOinkRequest.noFilterInt) {
				HashSet relRoleCodes = getRoleCodesByQualifier(qualifier);
				if (!relRoleCodes.contains(new Long(aNbaOinkRequest.getRelationRoleCode()))) {
					Integer index = (Integer) getIndices().get(hashKey.toString());
					if (index != null) {
						return getOLifE().getPartyAt(index.intValue());
					}
				}
			} else {
				Integer index = (Integer) getIndices().get(hashKey.toString());
				if (index != null) {
					return getOLifE().getPartyAt(index.intValue());
				}
			}
		}//ALII124 //End P2AXAL024, ALII484 
		if (aNbaOinkRequest.getPartyFilter() != NbaOinkRequest.noFilterInt) {
			occurrence = aNbaOinkRequest.getPartyFilter();
			try {
				return getOLifE().getPartyAt(occurrence);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}
		//Begin P2AXAL024..add back original code for 'default' at this point
		if (qualifier.length() == 0 || NbaOinkRequest.noFilterInt != aNbaOinkRequest.getRelationRoleCode()) { 
			try {
				return getOLifE().getPartyAt(occurrence);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}
		//End P2AXAL024
		//ALII124 code deleted
		if (!getIndices().containsKey(hashKey.toString())) {
			if (!isUpdateMode())
				if (aNbaOinkRequest.getQualifier().equals(PARTY_REQUIREMENT)) {
					aNbaOinkRequest.setQualifier(PARTY_PRIM_INSURED);
					aNbaOinkRequest.setPartyFilter(NbaOinkRequest.noFilterLong, NbaOinkRequest.noFilterString);
					return getParty(aNbaOinkRequest, elementIndex);
				} else {
					return null;
				}
			else {
				//begin NBA115
				if (CREDIT_CARD_PAYMENT.equals(qualifier) && getBankingList(aNbaOinkRequest).isEmpty()) {
					initCreditCardPayment(createBanking());
				} else {
					createParty(hashKey.toString());
				}
				//end NBA115
				return getOLifE().getPartyAt(getOLifE().getPartyCount() - 1); //NBA053
			}
		}
		Integer index = (Integer) getIndices().get(hashKey.toString());
		setActionUpdate(getOLifE().getPartyAt(index.intValue())); //NBA053
		return getOLifE().getPartyAt(index.intValue());
	}
	/**
	 * This function retrieves a Party object.
	 * @return com.csc.fsg.nba.vo.txlife.Party
	 * @param partyKey java.lang.String
	 */
	protected Party getParty(String partyKey) {
		Integer index_party;
		if (!getIndices().containsKey(partyKey.toString())) {
			if (isUpdateMode()) {
				createParty(partyKey.toString());
				index_party = (Integer) getIndices().get(partyKey.toString()); //NBA053
				setActionAdd(getOLifE().getPartyAt(index_party.intValue())); //NBA053
			} else {
				return null;
			}
		}
		index_party = (Integer) getIndices().get(partyKey.toString());
		Party party = getOLifE().getPartyAt(index_party.intValue());
		setActionUpdate(party); //NBA053
		return party;
	}
	/**
	 * This function retrieves a Party object.
	 * @return com.csc.fsg.nba.vo.txlife.Party
	 * @param partyType java.lang.String
	 * @param elementIndex int
	 */
	protected Party getParty(String partyType, int elementIndex) {
		StringBuffer hashKey = new StringBuffer();
		Integer index;
		hashKey.append(partyType);
		hashKey.append(elementIndex);
		if (!getIndices().containsKey(hashKey.toString())) {
			if (isUpdateMode()) {
				createParty(hashKey.toString());
				index = (Integer) getIndices().get(hashKey.toString()); //NBA053
				setActionAdd(getOLifE().getPartyAt(index.intValue())); //NBA053
			} else {
				return null;
			}
		}
		index = (Integer) getIndices().get(hashKey.toString());
		Party party = getOLifE().getPartyAt(index.intValue());
		setActionUpdate(party); //NBA053
		return party;
	}
	/**
	 * This function retrieves a Party object for the primary insured.
	 * @return com.csc.fsg.nba.vo.txlife.Party
	 */
	protected Party getPartyForPrimaryIns() {
		Integer index;
		StringBuffer hashKey = new StringBuffer();
		hashKey.append(PARTY_PRIM_INSURED);
		hashKey.append("0");
		String accessKey = hashKey.toString();
		if (!getIndices().containsKey(accessKey)) {
			if (isUpdateMode()) {
				// Create the party and role and add an entry to the Map for "PINS0"
				createParty(accessKey);
				index = (Integer) getIndices().get(accessKey);
				getIndices().put(getNewKey(getIndices(), productType.equalsIgnoreCase(PRODUCT_ANNUITY) ? PRODUCT_ANNUITY : PARTY_INSURED), index);
				setActionAdd(getOLifE().getPartyAt(index.intValue())); //NBA053
			} else {
				return null;
			}
		} else {
			index = (Integer) getIndices().get(accessKey);
		}
		setActionUpdate(getOLifE().getPartyAt(index.intValue())); //NBA053
		return getOLifE().getPartyAt(index.intValue());
	}
	/**
	 * This function retrieves a Payout object based on index.
	 * @return com.csc.fsg.nba.vo.txlife.Payout
	 * @param payoutType long
	 */

	protected Payout getPayout(int elementIndex) {
		Annuity annuity = getAnnuity();
		Payout payout = null;

		if (annuity != null) {
			if (annuity.getPayoutCount() > elementIndex) {
				payout = annuity.getPayoutAt(elementIndex);
				setActionUpdate(payout); //NBA053
			}

			if (payout == null) {
				if (isUpdateMode()) {
					int index = createPayout(annuity);
					payout = annuity.getPayoutAt(index);
					setActionAdd(payout); //NBA053
				}
			}
		}

		return payout;
	}
	/**
	 * This function retrieves a Payout object based on payout type.
	 * @return com.csc.fsg.nba.vo.txlife.Payout
	 * @param payoutType long
	 */
	protected Payout getPayout(long payoutType) {
		int index;
		int sizePayout = 0;
		Annuity annuity = getAnnuity();
		Payout payout = null;

		if (annuity != null) {
			sizePayout = annuity.getPayoutCount();
			for (int i = 0; i < sizePayout; i++) {
				payout = annuity.getPayoutAt(i);
				if (payout.getPayoutType() == payoutType) {
					setActionUpdate(payout); //NBA053
					return payout;
				}
				payout = null;
			}

			if (isUpdateMode()) {
				index = createPayout(annuity, payoutType);
				payout = annuity.getPayoutAt(index);
				setActionAdd(payout); //NBA053
			}
		}

		return payout;
	}
	/**
	 * This function retrieves a Payout object.
	 * @return com.csc.fsg.nba.vo.txlife.Payout
	 * @param start int
	 * @param payoutType long
	 */
	protected Payout getPayout(long payoutType, int start) {
		int index;
		int sizePayout = 0;
		int lookingFor = 0;
		Annuity annuity = getAnnuity();
		Payout payout = null;

		if (annuity != null) {
			sizePayout = annuity.getPayoutCount();
			for (int i = 0; i < sizePayout; i++) {
				payout = annuity.getPayoutAt(i);
				if (payout.getPayoutType() == payoutType) {
					if (start == lookingFor) {
						setActionUpdate(payout); //NBA053
						return payout;
					} else {
						lookingFor++;
					}
				}
				payout = null;
			}

			if (isUpdateMode()) {
				index = createPayout(annuity, payoutType);
				payout = annuity.getPayoutAt(index);
				setActionAdd(payout); //NBA053
			}
		}

		return payout;
	}
	/**
	 * This function retrieves a Person object for a specified Party
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Person
	 */
	protected Person getPerson(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		Person person = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			Object obj = party.getPersonOrOrganization$Contents();
			if ((obj == null) || (obj instanceof Organization)) {
				if (isUpdateMode()) {
					createPerson(party);
					setActionAdd(person); //NBA053
				} else {
					return null;
				}
			}
			if (!party.hasPartyTypeCode()) {
				party.setPartyTypeCode(NbaOliConstants.OLI_PT_PERSON);
			}
			person = party.getPersonOrOrganization().getPerson();
		}
		setActionUpdate(person); //NBA053
		return person;
	}
	/**
	 * Locate a Phone for a Party based on Address type.
	 * @param party  
	 * @param addressType long
	 * @return com.csc.fsg.nba.vo.txlife.Address
	 */
	protected Phone getPhoneForType(Party party, long phoneType) {
		ArrayList phones = party.getPhone();
		for (int i = 0; i < phones.size(); i++) {
			Phone aPhone = (Phone) phones.get(i);
			if (aPhone.getPhoneTypeCode() == phoneType) {
				setActionUpdate(aPhone); //NBA053
				return aPhone;
			}
		}
		if (isUpdateMode()) {
			Phone aPhone = createPhone(party, phoneType); //NBA053
			setActionAdd(aPhone); //NBA053
			return aPhone; //NBA053
		}
		return null;
	}
	/**
	 * This function retrieves a Policy object.
	 * @return com.csc.fsg.nba.vo.txlife.Policy
	 */

	protected Policy getPolicy() {

		if (!getHolding().hasPolicy()) { //NBA044
			//create policy, it is a basic object
			getHolding().setPolicy(new Policy()); //NBA044
			setActionAdd(getHolding().getPolicy()); //NBA053
		}

		Policy policy = getHolding().getPolicy(); //NBA044
		setActionUpdate(policy); //NBA053
		return policy;
	}
	/**
	 * Locate the preferred Address. If none are identified
	 * as the preferred Address, default to the first Address.
	 * @param party  
	 * @return com.csc.fsg.nba.vo.txlife.Address
	 */
	protected Address getPreferredAddress(Party party) {
		ArrayList addresses = party.getAddress();
		for (int i = 0; i < addresses.size(); i++) {
			Address address = (Address) addresses.get(i);
			if (address.getPrefAddr()) {
				setActionUpdate(address); //NBA053
				return address;
			}
		}
		if (addresses.size() > 0) {
			setActionUpdate((Address) addresses.get(0)); //NBA053
			return (Address) addresses.get(0);
		} else {
			return null;
		}
	}
	/**
	 * Retrieves a Relation object based on a Related Object Id and a Originating Object Id.
	 * @return com.csc.fsg.nba.vo.txlife.Relation
	 * @param partyType java.lang.String
	 * @param elementIndex int
	 * @param originatingObjectId java.lang.String
	 * @param relatedPartyId java.lang.String
	 */
	protected Relation getRelation(String partyType, int elementIndex, String originatingObjectId, String relatedPartyId) {
		if (originatingObjectId == null) {
			originatingObjectId = PRIMARY_HOLDINGID;
		}
		int index_relation = getRelationIndex(partyType, originatingObjectId, relatedPartyId);
		StringBuffer partyKey = new StringBuffer();
		partyKey.append(partyType);
		partyKey.append(elementIndex);
		if (index_relation == -1) {
			if (isUpdateMode()) {
				index_relation = createRelation(partyKey.toString(), originatingObjectId, relatedPartyId);
				setActionAdd(getOLifE().getRelationAt(index_relation)); //NBA053
			} else {
				return null;
			}
		}
		setActionUpdate(getOLifE().getRelationAt(index_relation)); //NBA053
		return getOLifE().getRelationAt(index_relation);
	}
	/**
	 * Retrieves an index of a Relation object based on a Related Object Id and a Originating Object Id.
	 * @return int
	 * @param relType java.lang.String 
	 * @param originatingObjectId java.lang.String
	 * @param relatedPartyId java.lang.String
	 */
	protected int getRelationIndex(String relType, String originatingObjectId, String relatedPartyId) {
		int sizeRelation = getOLifE().getRelationCount();
		int sizeExtension = 0;
		Relation relation = null;
		// SPR3290 code deleted
		HashSet relRoleCode = getRoleCodesByQualifier(relType);//P2AXAL024, ALII484
		for (int i = 0; i < sizeRelation; i++) {
			relation = getOLifE().getRelationAt(i);
			if (relation.getOriginatingObjectID().equals(originatingObjectId)
				&& relation.getRelatedObjectID().equals(relatedPartyId)
				&& relation.hasRelationRoleCode()
				&& relRoleCode.contains(new Long(relation.getRelationRoleCode()))) {
				if (relType.equals(PARTY_BENEFICIARY) || relType.equals(PARTY_COBENEFICIARY)) {
					sizeExtension = relation.getOLifEExtensionCount();
					for (int j = 0; j < sizeExtension; j++) {
						//NBA093 deleted 1 line
						if (relType.equals(PARTY_BENEFICIARY)
							&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_BENEFICIARY) //NBA093
							return i;
						else if (relType.equals(PARTY_COBENEFICIARY)
								&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_CONTGNTBENE) //NBA093
							return i;
					}
					if (sizeExtension == 0 && (relType.equals(PARTY_BENEFICIARY) || relType.equals(PARTY_COBENEFICIARY))) //NBA093
						return i;
				} else
					return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * Retrieves roleCodes in a HashSet depending upon the Relation Type.
	 * @return HashSet
	 * @param relType java.lang.String 
	 */
	//P2AXAL024,ALII484 new method
	protected HashSet getRoleCodesByQualifier(String relType) {
		HashSet relRoleCode = new HashSet();
		if (relType.equals(PARTY_INSURED)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_INSURED));
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_DEPENDENT));
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_COVINSURED)); //NBA093
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_JOINTINSURED));
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_SPOUSE));
		} else if (relType.equals(PARTY_PRIM_INSURED)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_INSURED));
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_ANNUITANT));
		} else if (relType.equals(PARTY_OTHER_INSURED)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_COVINSURED)); //NBA093
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_SPOUSE));	//NBA100
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_DEPENDENT));	//NBA100
		} else if (relType.equals(PARTY_JOINT_INSURED)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_JOINTINSURED));
		} else if (relType.equals(PARTY_PRIMARY_AND_JOINT_INSURED)) {	//NBA100
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_JOINTINSURED));		//NBA100
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_INSURED));			//NBA100			
		} else if (relType.equals(PARTY_SPOUSE)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_SPOUSE));
		} else if (relType.equals(PARTY_OWNER)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_OWNER));
		} else if (relType.equals(PARTY_PRIM_OWNER)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_OWNER));
		} else if (relType.equals(PARTY_PRIWRITINGAGENT)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_PRIMAGENT));
		} else if (relType.equals(PARTY_ADDWRITINGAGENT)) {  //SPR2722
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_ADDWRITINGAGENT));//SPR2722
		} else if (relType.equals(PARTY_PAYOR)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_PAYER));
		} else if (relType.equals(PARTY_REQUESTEDBY)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_REQUESTEDBY));
		} else if (relType.equals(PARTY_CARRIER)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_CARRIER));
		//begin SPR1721
		} else if (relType.equals(PARTY_ANNUITANT)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_ANNUITANT));
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_JOINTANNUITANT));
		//end SPR1721
		} else if (relType.equals(PARTY_BENEFICIARY)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_BENEFICIARY));
		}else if (relType.equals(PARTY_COBENEFICIARY)) { //NBA093
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_CONTGNTBENE));//NBA093
		}else if (relType.equals(PARTY_BGA)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_GENAGENT)); //AXAL3.7.13I
		}else if (relType.equals(PARTY_SBGA)) {
				relRoleCode.add(new Long(NbaOliConstants.OLI_REL_SUBORDAGENT)); // SR641590  SUB-BGA	
		}else if (relType.equals(PARTY_BCM)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_BGACASEMANAGER)); //AXAL3.7.13
		}else if (relType.equals(PARTY_SBCM)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_BGACASEMANAGER)); //SR641590  SUB-BGA	 
		} else if (relType.equals(PARTY_REPLCOMP)) {//P2AXAL028
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_HOLDINGCO)); //P2AXAL028
		} else if (relType.equals(PARTY_MULTIPLEASSIGNEE)) {
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_ASSIGNEE)); //P2AXAL005
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_COLLASSIGNEE)); //P2AXAL005
		} else if (relType.equals(PARTY_APPLCNT)) {	//ALII104
			relRoleCode.add(new Long(NbaOliConstants.OLI_REL_APPLICANT));
		}else if (relType.equals(PARTY_PIR)) {// APSL3447
				relRoleCode.add(new Long(NbaOliConstants.OLI_REL_PEND_INFO_RECIPIENT)); 	
		}else if (relType.equals(PARTY_PROCESSFIRM)) {// APSL3447
				relRoleCode.add(new Long(NbaOliConstants.OLI_REL_PROCESSINGFIRM));	
		}else if (relType.equalsIgnoreCase(PARTY_CONTRACTFIRM)) {// APSL3447
				relRoleCode.add(new Long(NbaOliConstants.OLI_REL_CONTRACTINGFIRM)); 	
		}else if (relType.equals(PARTY_PROCESSFIRMCM)) {// APSL3447
				relRoleCode.add(new Long(NbaOliConstants.OLI_REL_BGACASEMANAGER));	
		}
		return relRoleCode;
	}
	
	/**
	 * This function retrieves a list of RequirementInfo objects
	 * If a requirementIdFilter has been set, return the requirement matching that id
	 * If a requirementIdFilter has not been set, and no qualifier has been set or the qualifier of INS is set
	 * return the requirement specified by elementIndex of all requirements
	 * If a qualifier has been set, return the requirement specified by elementIndex for requirements belonging to
	 * the qualifier.
	 * @param nbaOinkRequest
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.RequirementInfo 
	 */
	//SPR3353 New Method
	protected List getRequirementInfos(NbaOinkRequest nbaOinkRequest) {
		List reqList = new ArrayList();
		RequirementInfo aRequirementInfo = null;
		Policy policy = getPolicy();
		//begin SPR3353
		if (policy != null) {
			//return the requirement for the specified id filter
			if (!NbaUtils.isBlankOrNull(nbaOinkRequest.getRequirementIdFilter())) {
				int size = policy.getRequirementInfoCount();
				for (int i = 0; i < size; i++) {
					aRequirementInfo = policy.getRequirementInfoAt(i);
					if (nbaOinkRequest.getRequirementIdFilter().equals(aRequirementInfo.getId())) {
						reqList.add(aRequirementInfo);
						return reqList;
					}
				}
			}
			//if a qualifier is not set or all requirements are asked for, return the list of all requirements
			if (NbaUtils.isBlankOrNull(nbaOinkRequest.getQualifier()) || PARTY_INSURED.equals(nbaOinkRequest.getQualifier())) {
					return policy.getRequirementInfo();
			} 
			//Build a list of requirements for the qualifier and index specified
			Party party = null;
			int partyIndex = 0;
			do {
				party = getParty(nbaOinkRequest, partyIndex);
				if (party != null) {
					Map reqMap = getNbaTXLife().getRequirementInfos(party.getId());
					reqList.addAll(reqMap.values());
				}
				partyIndex++;
			} while (party != null);

			if (!reqList.isEmpty()) {
				return reqList;
			}
		}	
		return reqList;
		//end SPR3353
	}
	/**
	 * This function retrieves a RequirementInfo object.
	 * @param nbaOinkRequest
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.RequirementInfo 
	 */
	//SPR3353 Changed Method signature
	protected RequirementInfo getRequirementInfo(List reqList, int index) {
		if (reqList.size() > index) {
			return (RequirementInfo) reqList.get(index);
		}
		RequirementInfo reqInfo = null;
		if (isUpdateMode()) {
			int i = createRequirementInfo();
			reqInfo = getPolicy().getRequirementInfoAt(i);
			setActionAdd(reqInfo);
		}
		return reqInfo;
	}
	/**
	 * This function retrieves a Risk object for a specified Party
	 * @param party
	 * @return com.csc.fsg.nba.vo.txlife.Risk
	 */
	protected Risk getRisk(Party party) {
		if (!party.hasRisk()) {
			if (isUpdateMode()) {
				createRisk(party);
				setActionAdd(party.getRisk()); //NBA053
			} else {
				return null;
			}
		}
		setActionUpdate(party.getRisk()); //NBA053
		return party.getRisk();
	}
	/**
	 * This function retrieves a SubAccount object
	 * @return com.csc.fsg.nba.vo.txlife.SubAccount
	 * @param activityType long
	 */

	protected SubAccount getSubAccount(long activityType) {
		int index;
		int sizeSubAccount = 0;
		SubAccount subAccount = null;
		if (!getHolding().hasInvestment()) {
			getHolding().setInvestment(new Investment());
			setActionAdd(getHolding().getInvestment()); //NBA053
		}

		Investment investment = getHolding().getInvestment();

		sizeSubAccount = investment.getSubAccountCount();
		for (int i = 0; i < sizeSubAccount; i++) {
			subAccount = investment.getSubAccountAt(i);
			if (subAccount.getSystematicActivityType() == activityType) {
				setActionUpdate(subAccount); //NBA053
				return subAccount;
			}
			subAccount = null;
		}

		if (isUpdateMode()) {
			index = createSubAccount(activityType);
			subAccount = investment.getSubAccountAt(index);
			setActionAdd(subAccount); //NBA053
		}

		return subAccount;
	}

	/**
	 * This function retrieves a SubAccount object
	 * @return com.csc.fsg.nba.vo.txlife.SubAccount
	 * @param elementIndex int
	 */
	//NBA059 new method 
	protected SubAccount getSubAccount(int elementIndex) {
		int index;
		// SPR3290 code deleted
		SubAccount subAccount = null;
		if (!getHolding().hasInvestment()) {
			if (isUpdateMode()) {		//SPR3610
				getHolding().setInvestment(new Investment());
				setActionAdd(getHolding().getInvestment()); //NBA053
			} else {		//SPR3610
				return subAccount;		//SPR3610
			}	//SPR3610
		}

		Investment investment = getHolding().getInvestment();

		if (investment.getSubAccountCount() > elementIndex) {
			subAccount = investment.getSubAccountAt(elementIndex);
			setActionUpdate(subAccount); //NBA053
		}

		if (subAccount == null) {
			if (isUpdateMode()) {
				index = createSubAccount();
				subAccount = investment.getSubAccountAt(index);
				setActionAdd(subAccount); //NBA053
			}
		}

		return subAccount;
	}

	/**
	 * Determine if a Address object is available within a Party.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @param addressType long
	 */
	protected boolean hasAddress(NbaOinkRequest aNbaOinkRequest, int elementIndex, long addressType) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			ArrayList addresses = party.getAddress();
			for (int i = 0; i < addresses.size(); i++) {
				Address address = (Address) addresses.get(i);
				if (address.getAddressTypeCode() == addressType) {
					isPresent = true;
					break;
				}
			};
		}
		return isPresent;
	}
	/**
	 * Test if a CarrierAppointment object is available within a Party.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @param carrierApptTypeCode long
	 */
	protected boolean hasCarrierAppointment(NbaOinkRequest aNbaOinkRequest, int elementIndex, long carrierApptTypeCode) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			if (!party.hasProducer()) {
				party.setProducer(new Producer());
			}
			Producer producer = party.getProducer();
			int sizeCarrierAppointment = producer.getCarrierAppointmentCount();
			for (int i = 0; i < sizeCarrierAppointment; i++) {
				if (producer.getCarrierAppointmentAt(i).getCarrierApptTypeCode() == carrierApptTypeCode) {
					isPresent = true;
				}
			}
		}
		return isPresent;
	}
	/**
	 * This function tests if a Client object is available within a Party.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex long
	 */

	protected boolean hasClient(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			isPresent = party.hasClient();

		}
		return isPresent;
	}
	/**
	 * Determine if a EMail object is available within a Party. 
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @param emailType long
	 */
	protected boolean hasEMail(NbaOinkRequest aNbaOinkRequest, int elementIndex, long emailType) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			ArrayList eMailAddresses = party.getEMailAddress();
			for (int i = 0; i < eMailAddresses.size(); i++) {
				EMailAddress address = (EMailAddress) eMailAddresses.get(i);
				if (address.getEMailType() == emailType) {
					isPresent = true;
					break;
				}
			}
		}
		return isPresent;
	}

	/**
	 * Determine if a ProductCode is present or not. 
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex - index
	 */
	//NBA059 new method
	protected boolean hasProductCode(NbaOinkRequest aNbaOinkRequest, int elementIndex) {//NBA093
		if (getSubAccount(elementIndex) != null) {
			return getSubAccount(elementIndex).hasProductCode();//NBA093
		}
		return false;
	}

	/**
	 * Determine if a AllocPercent is present or not. 
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex - index
	 */
	//NBA059 new method
	protected boolean hasAllocPercent(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		if (getSubAccount(elementIndex) != null) {
			return getSubAccount(elementIndex).hasAllocPercent();
		}
		return false;
	}

	/**
	 * Determine if a ProductObjective is present or not. 
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex - index
	 */
	//NBA059 new method
	protected boolean hasProductObjective(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		if (getSubAccount(elementIndex) != null) {
			return getSubAccount(elementIndex).hasProductObjective();
		}
		return false;
	}

	/**
	 * Return true if the the party has insurance.
	 */
	protected boolean hasInsurance(String partyId, long particRole) {
		if (!productType.equals(PRODUCT_ANNUITY)) {
			ArrayList allCoverages = getLife().getCoverage();
			for (int cov = 0; cov < allCoverages.size(); cov++) {
				Coverage aCoverage = (Coverage) allCoverages.get(cov);
				ArrayList allParticipants = aCoverage.getLifeParticipant();
				for (int i = 0; i < allParticipants.size(); i++) {
					LifeParticipant aParticipant = (LifeParticipant) allParticipants.get(i);
					long role = aParticipant.getLifeParticipantRoleCode(); //NBA100
					if (aParticipant.getPartyID().equals(partyId) && role == particRole) { //NBA100
						partyId = aParticipant.getPartyID();
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * This function tests if a Party exists.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int 
	 */
	protected boolean hasParty(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		String qualifier = aNbaOinkRequest.getQualifier();
		int occurrence;
		if (aNbaOinkRequest.getPartyFilter() > 0) {
			occurrence = aNbaOinkRequest.getPartyFilter();
		} else {
			occurrence = elementIndex;
		}
		if (qualifier.length() == 0) {
			try {
				getOLifE().getPartyAt(occurrence);
				return true;
			} catch (IndexOutOfBoundsException e) {
				return false;
			}
		}
		StringBuffer hashKey = new StringBuffer();
		hashKey.append(qualifier);
		hashKey.append(occurrence);
		return getIndices().containsKey(hashKey.toString());
	}
	/**
	 * This function tests if a Party exists.
	 * @return boolean
	 * @param partyType java.lang.String
	 * @param elementIndex int 
	 */

	protected boolean hasParty(String partyType, int elementIndex) {
		StringBuffer hashKey = new StringBuffer();
		hashKey.append(partyType);
		hashKey.append(elementIndex);
		return getIndices().containsKey(hashKey.toString());
	}
	/**
	 * This function tests if a Person object is available within a Party.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 */
	protected boolean hasPerson(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			Object obj = party.getPersonOrOrganization$Contents();
			isPresent = ((obj instanceof Person) ? true : false);
		}
		return isPresent;
	}
	/**
	 * Determine if a Phone object is available within a Party. 
	 * @return boolean
	 * @param partyType java.lang.String
	 * @param elementIndex int
	 * @param phoneType long
	 */
	protected boolean hasPhone(NbaOinkRequest aNbaOinkRequest, int elementIndex, long phoneType) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			ArrayList phones = party.getPhone();
			;
			for (int i = 0; i < phones.size(); i++) {
				Phone phone = (Phone) phones.get(i);
				if (phone.getPhoneTypeCode() == phoneType) {
					isPresent = true;
					break;
				}
			};
		}
		return isPresent;
	}
	/**
	 * This is an abstract method and should ideally contain all initialization code.
	 * @param nbaTXLife com.csc.fsg.nba.vo.NbaTXLife
	 * @param subSystem int
	 */
	protected abstract void initializeObjects(NbaTXLife nbaTXLife) throws NbaBaseException;
	/**
	 * Initializes an internal Hash Map, with indices of Party objects,
	 */
	protected void initPartyIndices() {
		long relRoleCode = 0;
		int sizeRelation = getOLifE().getRelationCount();
		int sizeParty = getOLifE().getPartyCount();
		// SPR3290 code deleted
		String relObjectId = null;
		String orgObjectId = null;
		Relation relation = null;
		Party party = null;
		// SPR3290 code deleted
		//store indices of Party objects 
		for (int i = 0; i < sizeParty; i++) {
			party = getOLifE().getPartyAt(i);
			for (int j = 0; j < sizeRelation; j++) {
				relation = getOLifE().getRelationAt(j);
				relObjectId = relation.getRelatedObjectID();
				orgObjectId = relation.getOriginatingObjectID(); //SR641590  SUB-BGA
				relRoleCode = relation.getRelationRoleCode();
				if (party.getId().equals(relObjectId)) {
					if (relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING
                            //&& NbaConstants.PRIMARY_HOLDING_ID.equals(relation.getOriginatingObjectID())) { //SPR3231 ignore if not primary holding
							&& getPrimaryHoldingID().equals(relation.getOriginatingObjectID())) { //ALS4028
						if (relRoleCode == NbaOliConstants.OLI_REL_INSURED) { //Insured
							getIndices().put(getNewKey(getIndices(), PARTY_INSURED), new Integer(i));
							if (i != findPrimaryInsured()) {
								getIndices().put(getNewKey(getIndices(), PARTY_OTHER_INSURED), new Integer(i));
							}
						} else if (relRoleCode == NbaOliConstants.OLI_REL_ANNUITANT) { //Annuitant
							getIndices().put(getNewKey(getIndices(), PARTY_ANNUITANT), new Integer(i));
						} else if (relRoleCode == NbaOliConstants.OLI_REL_DEPENDENT) { //Dependent
							if (hasInsurance(party.getId(), NbaOliConstants.OLI_PARTICROLE_DEP)) {
								getIndices().put(getNewKey(getIndices(), PARTY_INSURED), new Integer(i));
								getIndices().put(getNewKey(getIndices(), PARTY_OTHER_INSURED), new Integer(i));	//NBA100
							}
						} else if (relRoleCode == NbaOliConstants.OLI_REL_COVINSURED) { //Other Insured //NBA093
							getIndices().put(getNewKey(getIndices(), PARTY_INSURED), new Integer(i));
							getIndices().put(getNewKey(getIndices(), PARTY_OTHER_INSURED), new Integer(i));
						} else if (relRoleCode == NbaOliConstants.OLI_REL_SPOUSE) { //Spouse
							getIndices().put(getNewKey(getIndices(), PARTY_SPOUSE), new Integer(i));
							if (hasInsurance(party.getId(), NbaOliConstants.OLI_PARTICROLE_SPOUSE)) {
								getIndices().put(getNewKey(getIndices(), PARTY_INSURED), new Integer(i));
								getIndices().put(getNewKey(getIndices(), PARTY_OTHER_INSURED), new Integer(i));	//NBA100
							}
						} else if (relRoleCode == NbaOliConstants.OLI_REL_OWNER) { //Owner
							getIndices().put(getNewKey(getIndices(), PARTY_OWNER), new Integer(i));
							if (i == findPrimaryOwner()) {
								getIndices().put(getNewKey(getIndices(), PARTY_PRIM_OWNER), new Integer(i));
							}
						} else if (relRoleCode == NbaOliConstants.OLI_REL_PAYER) { //Payor
							getIndices().put(getNewKey(getIndices(), PARTY_PAYOR), new Integer(i));
						} else if (relRoleCode == NbaOliConstants.OLI_REL_REQUESTEDBY) { //Requested By
							getIndices().put(getNewKey(getIndices(), PARTY_REQUESTEDBY), new Integer(i));
						} else if (relRoleCode == NbaOliConstants.OLI_REL_JOINTINSURED) { //Joint Insured
							getIndices().put(getNewKey(getIndices(), PARTY_JOINT_INSURED), new Integer(i));
							getIndices().put(getNewKey(getIndices(), PARTY_PRIMARY_AND_JOINT_INSURED), new Integer(i));	//NBA100
							// NBA100 code deleted
							getIndices().put(getNewKey(getIndices(), PARTY_INSURED), new Integer(i));//NBA111
						} else if (relRoleCode == NbaOliConstants.OLI_REL_PRIMAGENT) { //Primary Agent
							getIndices().put(getNewKey(getIndices(), PARTY_PRIWRITINGAGENT), new Integer(i));
						} else if (relRoleCode == NbaOliConstants.OLI_REL_ADDWRITINGAGENT) {//SPR2722
							getIndices().put(getNewKey(getIndices(), PARTY_ADDWRITINGAGENT), new Integer(i));//SPR2722
						} else if (relRoleCode == NbaOliConstants.OLI_REL_SERVAGENCY) { //Servicing Agency //ACP019
							getIndices().put(getNewKey(getIndices(), PARTY_SAG), new Integer(i));	
						} else if (relRoleCode == NbaOliConstants.OLI_REL_CARRIER) { //Carrier
							getIndices().put(getNewKey(getIndices(), PARTY_CARRIER), new Integer(i));
						} else if (relRoleCode == NbaOliConstants.OLI_REL_PYMT_FACILITATOR) { //NBA115
							getIndices().put(getNewKey(getIndices(), CREDIT_CARD_PAYMENT), new Integer(i)); //NBA115
						} else if (relRoleCode == NbaOliConstants.OLI_REL_ASSIGNEE || relRoleCode == NbaOliConstants.OLI_REL_COLLASSIGNEE) { //P2AXAL005
							getIndices().put(getNewKey(getIndices(), PARTY_MULTIPLEASSIGNEE), new Integer(i));//P2AXAL005 
						}else if (relRoleCode == NbaOliConstants.OLI_REL_APPLICANT) { //ALII104 Applicant
							getIndices().put(getNewKey(getIndices(), PARTY_APPLCNT), new Integer(i));
						}
					}
					// AXAL3.7.13I Begin
					// AXAL3.7.13I Begin , SR641590  SUB-BGA, APSL3447-HVT
					if (relRoleCode == NbaOliConstants.OLI_REL_BGACASEMANAGER) {
						for (int k = 0; k < sizeRelation; k++) {
							Relation tempRelation = getOLifE().getRelationAt(k);
							long relationRoleCode = tempRelation.getRelationRoleCode();
							if (relationRoleCode == NbaOliConstants.OLI_REL_GENAGENT
									&& orgObjectId.equalsIgnoreCase(tempRelation.getRelatedObjectID())) {
								getIndices().put(getNewKey(getIndices(), PARTY_BCM), new Integer(i));
							} else if (relationRoleCode == NbaOliConstants.OLI_REL_SUBORDAGENT
									&& orgObjectId.equalsIgnoreCase(tempRelation.getRelatedObjectID())) {
								getIndices().put(getNewKey(getIndices(), PARTY_SBCM), new Integer(i));
							} else if (relationRoleCode == NbaOliConstants.OLI_REL_PROCESSINGFIRM
									&& orgObjectId.equalsIgnoreCase(tempRelation.getRelatedObjectID())) { //APSL3447
								getIndices().put(getNewKey(getIndices(), PARTY_PROCESSFIRMCM), new Integer(i));
							}
						}
					}	
				  //END	SR641590 SUB-BGA
					if (relRoleCode == NbaOliConstants.OLI_REL_GENAGENT) { 
						getIndices().put(getNewKey(getIndices(), PARTY_BGA), new Integer(i)); 
					}	
					// Begin SR641590  SUB-BGA
					if (relRoleCode == NbaOliConstants.OLI_REL_SUBORDAGENT) { 
						getIndices().put(getNewKey(getIndices(), PARTY_SBGA), new Integer(i)); 
					}
                   //End SR641590  SUB-BGA
					//AXAL3.7.13I End
					if (relRoleCode == NbaOliConstants.OLI_REL_HOLDINGCO) { //P2AXAL028
						getIndices().put(getNewKey(getIndices(), PARTY_REPLCOMP), new Integer(i));//P2AXAL028 
					}	
					//begin SPR1335
					if (relRoleCode == NbaOliConstants.OLI_REL_BENEFICIARY || relRoleCode == NbaOliConstants.OLI_REL_CONTGNTBENE) { //Beneficiary //NBA093
						if (isContingentBeneficiary(relation)) {
							getIndices().put(getNewKey(getIndices(), PARTY_COBENEFICIARY), new Integer(i));
						} else {
							getIndices().put(getNewKey(getIndices(), PARTY_BENEFICIARY), new Integer(i));
						}
					}
					//end SPR1335
					//Begin P2AXAL028
					if(relRoleCode == NbaOliConstants.OLI_REL_PRIMAGENT || relRoleCode == NbaOliConstants.OLI_REL_ADDWRITINGAGENT){
						getIndices().put(getNewKey(getIndices(),PARTY_AGENT), new Integer(i));
					}// End P2AXAL028
					// Begin APSL3447
					if (relRoleCode == NbaOliConstants.OLI_REL_PEND_INFO_RECIPIENT) { 
						getIndices().put(getNewKey(getIndices(), PARTY_PIR), new Integer(i)); 
					}
                    //End APSL3447 
					// Begin APSL3447
					if (relRoleCode == NbaOliConstants.OLI_REL_PROCESSINGFIRM) { //TODO : PROCESSFIRM RELATION ROLE CODE
						getIndices().put(getNewKey(getIndices(), PARTY_PROCESSFIRM), new Integer(i)); 
					}
                    //End APSL3447
					// Begin APSL3447
					if (relRoleCode == NbaOliConstants.OLI_REL_CONTRACTINGFIRM) { //TODO : CONTRACTFIRM RELATION ROLE CODE
						getIndices().put(getNewKey(getIndices(), PARTY_CONTRACTFIRM), new Integer(i)); 
					}
                    //End APSL3447
				}
			}
		}
		initPrimaryInsured();
		initFormNumbers(); //P2AXAL035
	}
	/**
	 * Initialize the index entry for the Primary Insured or Annuitant,
	 */
	protected void initPrimaryInsured() {
		if (!getPolicy().hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) { //NBA093
			createLifeOrAnnuityOrDisabilityHealth();
			if (productType.equals(PRODUCT_ANNUITY)) {
				Participant participant = getParticipant(getPayout(0), NbaOliConstants.OLI_PARTICROLE_ANNUITANT);
				participant.setPartyID(getParty(PARTY_ANNUITANT + "0").getId());
				if (!getOLifE().getSourceInfo().getFileControlID().equals(NbaConstants.SYST_CYBERLIFE)) {
					SubAccount subAccount = getSubAccount(NbaOliConstants.OLI_SYSACTTYPE_DEPT);
					subAccount.setAllocPercent(100);
					subAccount.setPortfolioCode("001");
				}
			} else {
				Coverage coverage = getCoverage(NbaOliConstants.OLI_COVIND_BASE);
				LifeParticipant participant = getLifeParticipant(coverage, NbaOliConstants.OLI_PARTICROLE_PRIMARY);
				participant.setPartyID(getParty(PARTY_INSURED + "0").getId());
				coverage.setProductCode(getPolicy().getProductCode());
			}
		}
		int insIndx = findPrimaryInsured();
		if (insIndx != -1) {
			getIndices().put(getNewKey(getIndices(), PARTY_PRIM_INSURED), new Integer(insIndx));
			getIndices().put(getNewKey(getIndices(), PARTY_PRIMARY_AND_JOINT_INSURED), new Integer(insIndx)); //NBA100
		}
	}
	
	
	/**
	 * This method resolves "Form Information"
	 */
	//P2AXAL035 New Method
	protected void initFormNumbers() {
		getFormNameMap().put(FORM_VUL,NbaConstants.FORM_NAME_VULSUP);
	}
	
	/**
	 * This method resolves "Product Information"
	 */
	protected void initProductType() {
		long productType = getPolicy().getProductType();
		if (productType == NbaOliConstants.OLI_PRODTYPE_ANN || productType == NbaOliConstants.OLI_PRODTYPE_VAR) {
			this.productType = PRODUCT_ANNUITY;
		} else {
			this.productType = PRODUCT_INSURANCE;
		}
	}
	/**
	 * Return true if the Beneficiary is a contingent Beneficiary
	 */
	protected boolean isContingentBeneficiary(Relation aRelation) {
		// SPR3290 code deleted
		//NBA093 deleted 10 lines
		//begin NBA093
		if(aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_CONTGNTBENE) {
			return true;
		}
		//end NBA093
		return false;
	}
	/**
	 * Return true if new objects may be created.
	 * @return boolean
	 */
	protected boolean isUpdateMode() {
		return updateMode;
	}
	/**
	 * This function assigns a reference of the Map object to an instance variable.
	 * @param map java.util.Map
	 */
	protected void setIndices(Map map) {
		map_Indices = map;
	}
	/**
	 * This function assigns a reference of the OLife object to an instance variable.
	 * @param objNbaTXLife com.csc.fsg.nba.vo.NbaTXLife
	 */
	protected void setOLifE(NbaTXLife objNbaTXLife) {
		oLifE = objNbaTXLife.getOLifE();
		if (getOLifE().getHoldingCount() == 0) {
			Holding holding = new Holding();
			holding.setId(PRIMARY_HOLDINGID);
			getOLifE().addHolding(holding);
			setActionAdd(holding); //NBA053
		}
		initProductType();
		setCommonVal(new NbaContractValidationCommon(objNbaTXLife));//P2AXAL016
	}
	//New method for NBA053
	/**
	 * This function assigns a reference of the NbaTXLife object to an instance variable.
	 * @param objNbaTXLife com.csc.fsg.nba.vo.NbaTXLife
	 */
	protected void setNbaTXLife(NbaTXLife objNbaTXLife) {
		nbaTXLife = objNbaTXLife;

	}
	/**
	* Set the value of updateMode.  When updateMode is true 
	* new objects will be created if needed.
	* @param newUpdateMode boolean
	*/
	protected void setUpdateMode(boolean newUpdateMode) {
		updateMode = newUpdateMode;
	}
	//New method for NBA053
	/**
	 * Set the value of updateMode.  When updateMode is true 
	 * new objects will be created if needed.
	 * @param newUpdateMode boolean
	 */
	protected void setApplicationUpdateMode(boolean newApplicationUpdate) {
		applicationUpdate = newApplicationUpdate;
	}
	//New method for NBA053
	/**
	 * Set the action indicator to Add.  When Application updateMode is true 
	 * new objects will be created if needed.
	 * @param NbaContractVO aNbaContractVO
	 * @param NbaOinkRequest aNbaOinkRequest
	 */
	public void setActionAdd(NbaContractVO aNbaContractVO) {

		if (applicationUpdate) {
			aNbaContractVO.setActionAdd();
		}
	}
	//New method for NBA053
	/**
	 * Set the action indicator to Update.  When Application updateMode is true 
	 * new objects will be created if needed.
	 * @param NbaContractVO aNbaContractVO
	 * @param NbaOinkRequest aNbaOinkRequest
	 */
	public void setActionUpdate(NbaContractVO aNbaContractVO) {

		if (applicationUpdate) {
			aNbaContractVO.setActionUpdate();
		}
	}

	/**
	 * @return
	 */
	 //NBA237 changed method signature
	public AccelProduct getNbaProduct() {
		return nbaProduct;
	}

	/**
	 * @param product
	 */
	 //NBA237 changed method signature
	public void setNbaProduct(AccelProduct product) {
		nbaProduct = product;
	}

	/**
	 * This function retrieves a LifeStyleActivity object for a specified Party
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Party.Risk.LifeStyleActivity
	 */
	//ACP002 New Method	 
	protected LifeStyleActivity getLifeStyleActivity(
		NbaOinkRequest aNbaOinkRequest,
		int elementIndex) {
		Party party = getParty(aNbaOinkRequest, elementIndex);
		LifeStyleActivity activity = null;
		if (party != null) {
			Risk risk = getRisk(party);
			if (risk != null) {
				if (risk.getLifeStyleActivityCount() > 0) {
					activity = risk.getLifeStyleActivityAt(0);
				}
			}
		}
		return activity;
	}
	/**
	 * This function retrieves a OrganizationFinancialData object
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.OrganizationFinancialData
	 */	 
	//ACP018 New Method	 	
	protected OrganizationFinancialData getOrganizationFinancialData(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		Organization organization = getOrganization(aNbaOinkRequest, elementIndex);
		if (organization != null) {
			if (organization.getOrganizationFinancialDataCount() == 0) {
				if (isUpdateMode()) {
					OrganizationFinancialData orgFinData = new OrganizationFinancialData();
					organization.addOrganizationFinancialData(orgFinData);
					setActionAdd(orgFinData);
					return orgFinData;
				} 
			} else {
				return organization.getOrganizationFinancialDataAt(0);
			}
		}
		return null;
	}
 
	/**
	 * This function retrieves a Employment object
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Employment
	 */
	//	ACP002 New Method	
	protected Employment getEmployment(NbaOinkRequest aNbaOinkRequest, int elementIndex){		
		Employment employment = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if(party != null){
			employment = party.getEmploymentAt(0);
			if(employment ==null){
				if(isUpdateMode()){
					createEmployment(party);
					setActionAdd(party.getEmploymentAt(0));
				}				
				else {
					return null;
				}
			}								
		}		
		setActionUpdate(party.getEmploymentAt(0));	
		return employment;
	}
	
	/**
	 * This function retrieves a Employment object by comparing employerPartyId attribute of Employment object with the owner party Id.	
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Employment
	 */
	//	ACP017 New Method	
	protected Employment getEmploymentForKeyPerson(NbaOinkRequest aNbaOinkRequest, int elementIndex){		
		Employment employment = null;
		Party insuredParty = getParty(aNbaOinkRequest, elementIndex);
		String employerPartyId = null;
		ArrayList partyList = getOLifE().getParty();
		ArrayList empList = insuredParty.getEmployment();
		int partyCount = partyList.size(); // SPR3290
		Party party = null;
		for(int i=0;i<partyCount;i++){ // SPR3290
			party = (Party)partyList.get(i);
			if(nbaTXLife.isOwner(party.getId())){			
				if(insuredParty != null){
					for(int j=0;j<empList.size();j++){
						employment = (Employment)empList.get(j);
						if(employment!=null){
							employerPartyId = employment.getEmployerPartyID();
							if(employerPartyId !=null  && employerPartyId.equalsIgnoreCase(party.getId())){
								return employment;
							}	
						}
					}
				}
			}
		}	
		return null;
	}
	/**
	 * This function creates a Employment object within a given Party object
	 * @param party com.csc.fsg.nba.vo.txlife.Party
	 */
	//	ACP002 New Method		
	protected void createEmployment(Party party) {
		Employment employment = new Employment();
		if (party != null) {
			party.setEmploymentAt(employment, 0);
		}
	}	
	/**
	 * This function retrieves a FamilyIllness object
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.FamilyIllness
	 */	
	//	ACP002 New Method
	protected FamilyIllness getFamilyIllness(NbaOinkRequest aNbaOinkRequest, int elementIndex){
		FamilyIllness familyIllness = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		Risk risk = null;
		if (party != null) {
			risk = getRisk(party);
			if (risk != null) {
				familyIllness = risk.getFamilyIllnessAt(0);
				if (familyIllness == null) {
					if (isUpdateMode()) {
						createFamilyIllness(risk);
						setActionAdd(risk.getFamilyIllnessAt(0));
					} else {
						return null;
					}
				}
			}
		}
		if (risk != null) {
			setActionUpdate(risk.getFamilyIllnessAt(0));
		}
		return familyIllness;
	}
	/**
	 * This function creates a FamilyIllness object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	//	ACP002 New Method	
	protected void createFamilyIllness(Risk risk) {
		FamilyIllness familyIllness = new FamilyIllness();
		if (risk != null) {
			risk.setFamilyIllnessAt(familyIllness, 0);
		}
	}

	/**
	 * This function creates a LifeStyleActivity object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	//	ACP002 New Method		
	protected void createLifeStyleActivity(Risk risk) {
		LifeStyleActivity lifeStyleActivity = new LifeStyleActivity();
		if (risk != null) {
			risk.setLifeStyleActivityAt(lifeStyleActivity, 0);
		}
	}	
	/**
	 * This function retrieves a AirSportsExp object
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.AirSportsExp
	 */
	//	ACP002 New Method		
	protected AirSportsExp getAirSportsExp(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		AirSportsExp airSportsExp = null;		
		LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest, elementIndex);
		if (lifeStyleActivity != null) {
			Object obj = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr$Contents();
			if ((obj == null) || !(obj instanceof AirSportsExp)) {
				if (isUpdateMode()) {
					createAirSportsExp(lifeStyleActivity);
					airSportsExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAirSportsExp();
					setActionAdd(airSportsExp); 
				} else {
					return null;
				}
			}
			airSportsExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAirSportsExp();
		}
		setActionUpdate(airSportsExp);
		return airSportsExp;
	}
	/**
	 * This function creates a AirSportsExp object within a given LifeStyleActivity object
	 * @param party com.csc.fsg.nba.vo.txlife.LifeStyleActivity
	 */
	//	ACP002 New Method		
	protected void createAirSportsExp(LifeStyleActivity lifeStyleActivity) {
		AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr avi = new AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr();
		avi.setAirSportsExp(new AirSportsExp());
		if( lifeStyleActivity != null ) {
			lifeStyleActivity.setAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr(avi);
		}
	}	
	/**
	 * This function retrieves a AviationExp object
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.AviationExp
	 */
	//	ACP002 New Method		
	protected AviationExp getAviationExp(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		AviationExp aviationExp = null;		
		LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest, elementIndex);
		if (lifeStyleActivity != null) {
			Object obj = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr$Contents();
			if ((obj == null) || !(obj instanceof AviationExp)) {
				if (isUpdateMode()) {
					createAviationExp(lifeStyleActivity);
					aviationExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAviationExp();
					setActionAdd(aviationExp); 
				} else {
					return null;
				}
			}			
			aviationExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAviationExp();
		}
		setActionUpdate(aviationExp);
		return aviationExp;
	}
	/**
	 * This function creates a AviationExp object within a given LifeStyleActivity object
	 * @param party com.csc.fsg.nba.vo.txlife.LifeStyleActivity
	 */
	//	ACP002 New Method		
	protected void createAviationExp(LifeStyleActivity lifeStyleActivity) {
		AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr avi = new AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr();
		avi.setAviationExp(new AviationExp());
		if(lifeStyleActivity != null) {
			lifeStyleActivity.setAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr(avi);
		}
	}
	
	/**
	 * This function retrieves a ForeignTravel object
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.ForeignTravel
	 */
	//	ACP022 New Method		
	protected ForeignTravel getForeignTravel(LifeStyleActivity lifeStyleActivity) {
		ForeignTravel foreignTravel = null;		
		if (lifeStyleActivity != null) {
			Object obj = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr$Contents();
			if ((obj == null) || !(obj instanceof ForeignTravel)) {
				if (isUpdateMode()) {
					createForeignTravel(lifeStyleActivity);
					foreignTravel = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getForeignTravel();
					setActionAdd(foreignTravel); 
				} else {
					return null;
				}
			}
			foreignTravel = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getForeignTravel();
		}
		setActionUpdate(foreignTravel);
		return foreignTravel;
	}
	/**
	 * This function creates a ForeignTravel object within a given LifeStyleActivity object
	 * @param party com.csc.fsg.nba.vo.txlife.LifeStyleActivity
	 */
	//	ACP022 New Method		
	protected void createForeignTravel(LifeStyleActivity lifeStyleActivity) {
		AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr avi = new AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr();
		avi.setForeignTravel(new ForeignTravel());
		if(lifeStyleActivity != null) {
			lifeStyleActivity.setAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr(avi);
		}
	}
	/**
	 * This function tests if a Employment object is available within a Party.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex long
	 */
	 //ACP002 New Method
	protected boolean hasEmployment(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			if (party.getEmploymentCount() > 0) {
				isPresent = true;
			}
		}
		return isPresent;
	}	
	/**
	 * This function creates an MedicalExam object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	 //ACP002 New Method
	protected void createMedicalExam(Risk risk) {
		risk.addMedicalExam(new MedicalExam());
	}
	/**
	 * This function retrieves an MedicalExam object for a specific requirement
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.MedicalExam
	 */
	 //ACP002 New Method
	protected MedicalExam getMedicalExam(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		ArrayList list = new ArrayList();
		MedicalExam medicalExam = null;
		//ALS4029 code deleted
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}
		if (risk != null) {
			if (!hasMedicalExam(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createMedicalExam(risk);
					setActionAdd(medicalExam);
				} else {
					return null;
				}
			}
			list = risk.getMedicalExam();
			if (list.size() > 0) { //ALS4029
				medicalExam = (MedicalExam)list.get(0);//ALS4029
			} //ALS4029
		}
		setActionUpdate(medicalExam);
		return medicalExam;
	}
	/**
	 * This function tests if a MedicalExam object is available within a Risk.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex long
	 */
	 //ACP002 New Method
	protected boolean hasMedicalExam(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}		
		if (risk != null) {
			if (risk.getMedicalExamCount() > 0) {
				isPresent = true;
			}
		}
		return isPresent;
	}

	/**
	 * This function creates an MedicalCondition object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	 //ACP007 New Method
	protected void createMedicalCondition(Risk risk) {
		risk.addMedicalCondition(new MedicalCondition());
	}

	/**
	 * This function tests if a MedicalCondition object is available within a Risk.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 */
	 //ACP007 New Method
	protected boolean hasMedicalCondition(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}		
		if (risk != null) {
			if (risk.getMedicalConditionCount() > 0) {
				isPresent = true;
			}
		}
		return isPresent;
	}
	
	/**
	 * This function retrieves an MedicalCondition object for a specified Risk
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.MedicalCondition
	 */
	 //ACP007 New Method
	protected MedicalCondition getMedicalCondition(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		MedicalCondition medicalCondition = null;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}
		if (risk != null) {
			if (!hasMedicalCondition(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createMedicalCondition(risk);
					setActionAdd(medicalCondition);
				} else {
					return null;
				}
			}
			if(aNbaOinkRequest.getMedConditionFilter() != -1){
				medicalCondition = risk.getMedicalConditionAt(aNbaOinkRequest.getMedConditionFilter()); //ACP007 - modified in MB2 to use medConditionFilter
			}
		}
		if(medicalCondition != null){
			setActionUpdate(medicalCondition);
		}
		return medicalCondition;
	}
	
	/**
	 * This function creates an MedicalPrevention object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	 //ACP002 New Method
	protected void createMedicalPrevention(Risk risk) {
		risk.addMedicalPrevention(new MedicalPrevention());
	}	
	
	/**
	 * This function retrieves an MedicalPrevention object for a specified Risk
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.MedicalPrevention
	 */
	 //ACP002 New Method
	 //ACP007 modified to get List of MedicalPreventions that match Risk.MedicalCondition.MedConditionID
	protected ArrayList getMedicalPreventions(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		MedicalPrevention medicalPrevention = null;
		ArrayList mpOutList = null; //ACP007
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}
		if (risk != null) {
			if (!hasMedicalPrevention(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createMedicalPrevention(risk); 
					setActionAdd(medicalPrevention); //NBA053
				} else {
					return null;
				}
			}
		// Begin ACP007
			String medConditionID = null;
			MedicalCondition medCond = getMedicalCondition(aNbaOinkRequest, elementIndex);
			if (medCond != null) { 
				medConditionID = medCond.getId();
				ArrayList mpList = risk.getMedicalPrevention();
				int mpSize = mpList.size();
				mpOutList = new ArrayList();
				for(int i=0; i<mpSize; i++) {
					MedicalPrevention mp = (MedicalPrevention)mpList.get(i);
					if(mp.getMedConditionID().equals(medConditionID)) mpOutList.add(mp);
				}
			}
		}
		if (mpOutList != null) {
			int mpOutSize = mpOutList.size();
			for(int i=0; i<mpOutSize; i++) {
				setActionUpdate((MedicalPrevention)mpOutList.get(i)); 
	}
		}
		return mpOutList;
		// End ACP007
	}
	
	/**
	 * This function tests if a MedicalPrevention object is available within a Risk.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 */
	 //ACP002 New Method
	//ACP007 modified criteria to filter by medConditionID
	protected boolean hasMedicalPrevention(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}		
		if (risk != null) {
			String medConditionID = null;
			MedicalCondition medCond = getMedicalCondition(aNbaOinkRequest, elementIndex);
			if (medCond != null) {
				medConditionID = medCond.getId();
				for (int i=0; i<risk.getMedicalPreventionCount(); i++) { 
					if (risk.getMedicalPreventionAt(i).getMedConditionID().equals(medConditionID)) {
				isPresent = true;
						break;
					}
			}
		}
				
		}
		return isPresent;
	}	
	
	/**
	 * This function creates an MedicalTreatment object within a given MedicalCondition object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk.MedicalCondition
	 */
	 //ACP002 New Method
	// ACP007 modified from Risk.MedicalTreatment to Risk.MedicalCondition.MedicalTreatment
	protected void createMedicalTreatment(MedicalCondition medicalCondition) {
		medicalCondition.addMedicalTreatment(new MedicalTreatment());
	}
	
	/**
	 * This function retrieves an MedicalTreatment object for a specified Risk
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.MedicalTreatment
	 */
	 //ACP002 New Method
	 // ACP007 modified from Risk.MedicalTreatment to Risk.MedicalCondition.MedicalTreatment
	 // ACP007 modified to return List of MedicalTreatments
	protected ArrayList getMedicalTreatments(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		MedicalTreatment medicalTreatment = null;
		ArrayList medicalTreatments = null;
		MedicalCondition medicalCondition = null;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}
		if (risk != null) {
			medicalCondition = getMedicalCondition(aNbaOinkRequest, elementIndex); //ACP007
		}		
		if (medicalCondition != null) { // ACP007
			if (!hasMedicalTreatment(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createMedicalTreatment(medicalCondition);
					setActionAdd(medicalTreatment); //NBA053 
				} else {
					return null;
				}
			}
			medicalTreatments = medicalCondition.getMedicalTreatment();
		}
		if (medicalTreatments != null) {
			int mtSize = medicalTreatments.size();
			for(int i=0; i<mtSize; i++) {
				setActionUpdate((MedicalTreatment)medicalTreatments.get(i)); 
			}
		}
		return medicalTreatments;
	}
	
	/**
	 * This function tests if a MedicalTreatment object is available within a Risk.MedicalCondition
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 */
	 //ACP002 New Method
	// ACP007 modified from Risk.MedicalTreatment to Risk.MedicalCondition.MedicalTreatment
	protected boolean hasMedicalTreatment(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Risk risk = null;
		MedicalCondition medicalCondition = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}		
		if (risk != null) {
			medicalCondition = getMedicalCondition(aNbaOinkRequest, elementIndex); //ACP007
		}
		if (medicalCondition != null) { //ACP007
			if (medicalCondition.getMedicalTreatmentCount() > 0) {
				isPresent = true;
			}
		}
		return isPresent;
	}	

	/**
	 * This function creates an PrescriptionDrug object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	 //ACP002 New Method
	protected void createPrescriptionDrug(Risk risk) {
		risk.addPrescriptionDrug(new PrescriptionDrug());
	}
	
	/**
	 * This function retrieves an PrescriptionDrug object for a specified Risk
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.PrescriptionDrug
	 */
	 //ACP002 New Method
	protected PrescriptionDrug getPrescriptionDrug(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		PrescriptionDrug prescriptionDrug = null;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}
		if (risk != null) {
			if (!hasPrescriptionDrug(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createPrescriptionDrug(risk);
					setActionAdd(prescriptionDrug); //NBA053
				} else {
					return null;
				}
			}
			prescriptionDrug = risk.getPrescriptionDrugAt(0);
		}
		setActionUpdate(prescriptionDrug);
		return prescriptionDrug;
	}
	
	/**
	 * This function tests if a PrescriptionDrug object is available within a Risk.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex long
	 */
	 //ACP002 New Method

	protected boolean hasPrescriptionDrug(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}		
		if (risk != null) {
			if (risk.getPrescriptionDrugCount() > 0) {
				isPresent = true;
			}
		}
		return isPresent;
	}	

	/**
	 * This function creates an SubstanceUsage object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	 //ACP002 New Method
	protected void createSubstanceUsage(Risk risk) {
		risk.addSubstanceUsage(new SubstanceUsage());
	}
	
	/**
	 * This function retrieves an SubstanceUsage object for a specified Risk
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.SubstanceUsage
	 */
	 //ACP002 New Method
	protected SubstanceUsage getSubstanceUsage(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		SubstanceUsage substanceUsage = null;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}
		if (risk != null) {
			if (!hasSubstanceUsage(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createSubstanceUsage(risk);
					setActionAdd(substanceUsage); //NBA053
				} else {
					return null;
				}
			}
			substanceUsage = risk.getSubstanceUsageAt(0);
		}
		setActionUpdate(substanceUsage);
		return substanceUsage;
	}
	
	/**
	 * This function tests if a SubstanceUsage object is available within a Risk.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex long
	 */
	 //ACP002 New Method

	protected boolean hasSubstanceUsage(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}		
		if (risk != null) {
			if (risk.getSubstanceUsageCount() > 0) {
				isPresent = true;
			}
		}
		return isPresent;
	}	

	/**
	 * This function creates an Violation object within a given Risk object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	 //ACP002 New Method
	protected void createViolation(Risk risk) {
		risk.addViolation(new Violation());
	}
	
	/**
	 * This function retrieves an Violation object for a specified Risk
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.Violation
	 */
	 //ACP002 New Method
	protected Violation getViolation(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		Violation violation = null;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}
		if (risk != null) {
			if (!hasViolation(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createViolation(risk);
					setActionAdd(violation); //NBA053
				} else {
					return null;
				}
			}
			violation = risk.getViolationAt(0);
		}
		setActionUpdate(violation);
		return violation;
	}
	
	/**
	 * This function tests if a Violation object is available within a Risk.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex long
	 */
	 //ACP002 New Method

	protected boolean hasViolation(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Risk risk = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			risk = getRisk(party);
		}		
		if (risk != null) {
			if (risk.getViolationCount() > 0) {
				isPresent = true;
			}
		}
		return isPresent;
	}		
	/**
	   * This function creates an PriorName object within a given Party object
	   * @param party com.csc.fsg.nba.vo.txlife.Party
	   */
   //ACP002 New Method
	protected void createPriorName(Party party) {
		party.addPriorName(new PriorName());
	}

	/**
	 * This function retrieves an PriorName object for a specified Party
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.PriorName
	 */
   //ACP002 New Method
	protected PriorName getPriorName(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		PriorName priorName = null;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			if (!hasPriorName(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createPriorName(party);
					setActionAdd(party.getPriorNameAt(elementIndex));
				} else
					return null;
			}
			priorName = party.getPriorNameAt(elementIndex);
		}
		setActionUpdate(priorName);
		return priorName;
	}

	/**
	 * This function tests if a PriorName object is available within a Party.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return boolean
	 */
   //ACP002 New Method	
	protected boolean hasPriorName(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		Party party = getParty(aNbaOinkRequest, elementIndex);
		if (party != null) {
			if (party.getPriorNameCount() > elementIndex) {
				isPresent = true;
			}
		}
		return isPresent;
	}

	/**
	 * This function creates an FormInstance object within a given OLifE object
	 * @param party com.csc.fsg.nba.vo.txlife.OLifE
	 */
   //ACP002 New Method
	protected void createFormInstance(OLifE oLifE) {
		oLifE.addFormInstance(new FormInstance());
	}

	/**
	 * This function retrieves an FormInstance object for a specified OLifE
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.OLifE.FormInstance
	 */
   //ACP002 New Method
	protected FormInstance getFormInstance(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		FormInstance formInstance = null;
		OLifE oLifE = getOLifE();
		if (oLifE != null) {
			if (!hasFormInstance(aNbaOinkRequest, elementIndex)) {
				if (isUpdateMode()) {
					createFormInstance(oLifE);
					setActionAdd(oLifE.getFormInstanceAt(elementIndex));
				} else
					return null;
			}
			formInstance = oLifE.getFormInstanceAt(elementIndex);
		}
		setActionUpdate(formInstance);
		return formInstance;
	}

	/**
	 * This function tests if a PriorName object is available within a Party.
	 * @return boolean
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex long
	 */
   //ACP002 New Method
	protected boolean hasFormInstance(NbaOinkRequest aNbaOinkRequest, int elementIndex) {
		boolean isPresent = false;
		OLifE oLifE = getOLifE();
		if (oLifE != null) {
			if (oLifE.getFormInstanceCount() > elementIndex) {
				isPresent = true;
			}
		}
		return isPresent;
	}
	/**
	 * This function retrieves a Coverage object.
	 * @param int index
	 * @param Life life
	 * @return com.csc.fsg.nba.vo.txlifetxlife.OLifE.Holding.Policy.Life.Coverage
	 */
   //ACP002 New Method
	protected Coverage getCoverage(Life life, int index) {
		Coverage coverage = null;
		if (life != null) {
			if (!hasCoverage(life, index)) {
				if (isUpdateMode()) {
					createCoverage(life);
					setActionAdd(life.getCoverageAt(index));
				} else
					return null;
			}
			coverage = life.getCoverageAt(index);
		}
		setActionUpdate(coverage);
		return coverage;
	
	}

	/**
	 * This function creates an Coverage object within a given LifE object
	 * @param life
	 */
   //ACP002 New Method
	private void createCoverage(Life life) {
		life.addCoverage(new Coverage());
	}
	/**
	 * This function checks the Coverage object within a given LifE object
	 * @param life
	 * @param index
	 * @return boolean
	 */
   //ACP002 New Method
	private boolean hasCoverage(Life life, int index) {
		if (life.getCoverageCount() > index)
			return true;
		return false;
	}
	/**
	 * This function retrieves a CovOption object.
	 * @param int index
	 * @param coverage object
	 * @return com.csc.fsg.nba.vo.txlife.OLifE.Holding.Policy.Life.Coverage.CovOption
	 */
   //ACP002 New Method
	protected CovOption getCovOption(Coverage coverage, int index) {
		CovOption covOption = null;
		if (coverage != null) {
			if (!hasCovOption(coverage, index)) {
				if (isUpdateMode()) {
					createCovOption(coverage);
					setActionAdd(coverage.getCovOptionAt(index));
				} else
					return null;
			}
			covOption = coverage.getCovOptionAt(index);
		}
		setActionUpdate(covOption);
		return covOption;

	}

	/**
	* This function checks the CovOption object within a given Coverage object
	* @param coverage
	*/
   //ACP002 New Method
	private void createCovOption(Coverage coverage) {
		coverage.addCovOption(new CovOption());
	}
	/**
	 * This function checks the CovOption object within a given Coverage object
	 * @param coverage
	 * @return
	 */
   //ACP002 New Method
	private boolean hasCovOption(Coverage coverage, int index) {
		if (coverage.getCovOptionCount() > index)
			return true;
		return false;
	}
	/**
	  * This function retrieves a LifeParticipant object.
	  * @param typeCode long
	  * @param coverage object
	  * @return com.csc.fsg.nba.vo.txlife.LifeParticipant
	  */
   //ACP002 New Method
	protected LifeParticipant getLifeParticipant(Coverage coverage, int index) {
		LifeParticipant lifeParticipant = null;
		if (coverage != null) {
			if (!hasLifeParticipant(coverage, index)) {
				if (isUpdateMode()) {
					createLifeParticipant(coverage);
					setActionAdd(coverage.getLifeParticipantAt(index));
				} else
					return null;
			}
			lifeParticipant = coverage.getLifeParticipantAt(index);
		}
		setActionUpdate(lifeParticipant);
		return lifeParticipant;

	}

	/**
	* This function creates the LifeParticipant object within a given Coverage object
	* @param coverage
	*/
   //ACP002 New Method
	private void createLifeParticipant(Coverage coverage) {
		coverage.addLifeParticipant(new LifeParticipant());
	}
	/**
	 * This function checks the LifeParticipant object within a given Coverage object
	 * @param coverage
	 * @return
	 */
   //ACP002 New Method
	private boolean hasLifeParticipant(Coverage coverage, int index) {
		if (coverage.getLifeParticipantCount() > index)
			return true;
		return false;
	}
	/**
		 * @return nbaAcdb
		 */
	//ACN015 new method
	public NbaAcdb getNbaAcdb() {//ACP
			return nbaAcdb;
	}
		/**
		 * @param acdb
		 */
	//ACN015 new method
	public void setNbaAcdb(NbaAcdb acdb) {//ACP
		nbaAcdb = acdb;
	}	
	/**
	 * This function retrieves a LabTesting object
	 * @param aNbaOinkRequest - data request container
	 * @param elementIndex int
	 * @return com.csc.fsg.nba.vo.txlife.LabTesting
	 */	
	//	ACP001 New Method
	protected LabTesting getlabTesting(NbaOinkRequest aNbaOinkRequest, int elementIndex){
		LabTesting labTesting = null;		
		Party party = getParty(aNbaOinkRequest, elementIndex);
		Risk risk = null;
		// SPR3290 code deleted
		RiskExtension riskExtension = null;
		if (party != null) {
            risk = getRisk(party);
            if (risk != null) {
                riskExtension = NbaUtils.getFirstRiskExtension(risk);	//SPR3179
                if (riskExtension != null) {	//SPR3179
                    labTesting = riskExtension.getLabTesting();
                    if (labTesting == null) {
                        if (isUpdateMode()) {
                            createLabTesting(riskExtension);
                            setActionAdd(riskExtension.getLabTesting());
                        } else {	//SPR3179
                            return null;
                        }
                    }
                }	//SPR3179
            }
        }	
		if(riskExtension !=null){
			setActionUpdate(riskExtension.getLabTesting());
		}
		return labTesting;
	}
	/**
	 * This function creates a LabTesting object within a given RiskExtension object
	 * @param party com.csc.fsg.nba.vo.txlife.Risk
	 */
	//	ACP001 New Method	
	protected void createLabTesting(RiskExtension riskExtension) {
		LabTesting labTesting = new LabTesting();
		if(riskExtension != null){		
			riskExtension.setLabTesting(labTesting);
		}
	}	
	//ACP019 New Method	
	public ImpairmentInfo getImpairmentInfo(NbaOinkRequest aNbaOinkRequest, int i){
		Person person = getPerson(aNbaOinkRequest, i);
		if (person != null) {
			 PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
			 if(personExtension!=null) {
				if(!hasImpairmentInfo(personExtension, i)) {
					if (isUpdateMode()) {
						createImpairmentInfo(personExtension);
						setActionAdd(personExtension.getImpairmentInfoAt(i));
					} else
						return null;
				}	 	
				return personExtension.getImpairmentInfoAt(i);
			 }
		}
		return null;
	}
	
	/**
	* This function creates the ImpairmentInfo object within a given PersonExtension object
	* @param personExtension
	*/
	 //ACP019 New Method
	private void createImpairmentInfo(PersonExtension personExtension) {
		personExtension.addImpairmentInfo(new ImpairmentInfo());
		}
	/**
	 * This function checks the ImpairmentInfo object within a given PersonExtension object
	 * @param personExtension
	 * @param index
	 * @return
	 */
	 //ACP019 New Method
	private boolean hasImpairmentInfo(PersonExtension personExtension, int index) {
		if (personExtension.getImpairmentInfo().size() > 0)
				return true;
			return false;
	}
	//ACP019 New Method
	public TrackingInfo getTrackingInfo(List reqList, int i){
		
		  RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); //SPR3353
		  if (aRequirementInfo != null) {
			//TrackinngInfo trackInfo
			if(!hasOLifEExtension(aRequirementInfo, i)) {
				if (isUpdateMode()) {
					createOLifEExtension(aRequirementInfo);
					setActionAdd(aRequirementInfo.getOLifEExtensionAt(i));
				} else
					return null;
			}	 	
			
			ArrayList arrOlifeExt= aRequirementInfo.getOLifEExtension();
			for (int j = 0; j < arrOlifeExt.size(); j++) {
				if(arrOlifeExt.get(i)!=null)
				{
				  OLifEExtension olifeExt=(OLifEExtension)arrOlifeExt.get(i);
				  if(!hasRequirementInfoExtension(olifeExt, i)) {
					if (isUpdateMode()) {
						createRequirementInfoExtension(olifeExt);
						setActionAdd(olifeExt.getRequirementInfoExtension());
					} else
						return null;						 	
					
					RequirementInfoExtension reqInfoEx=olifeExt.getRequirementInfoExtension(); // SPR3290
					if(!hasTrackingInfo(reqInfoEx, i)) {
						if (isUpdateMode()) {
							createTrackingInfo(reqInfoEx);
							setActionAdd(reqInfoEx.getTrackingInfo());
						} else
							return null;
					}	 	
					return reqInfoEx.getTrackingInfo();
				 }
			}
			return null;
		  }
		 }
		return null;
	}	  
	
	/**
	* This function creates the OLifEExtension object within a given RequirementInfo object
	* @param aRequirementInfo
	*/
	 //ACP019 New Method
	private void createOLifEExtension(RequirementInfo aRequirementInfo) {
		aRequirementInfo.addOLifEExtension(new OLifEExtension());
	}
	/**
	 * This function checks the OLifEExtension object within a given RequirementInfo object
	 * @param reqInfo
	 * @param index
	 * @return
	 */
	//ACP019 New Method
	private boolean hasOLifEExtension(RequirementInfo reqInfo, int index) {
		if (reqInfo.getOLifEExtension() !=null)
			return true;
		return false;
	}
	/**
	* This function creates the RequirementInfoExtension object within a given OLifEExtension object
	* @param olifeExt
	*/
	//ACP019 New Method
	private void createRequirementInfoExtension(OLifEExtension olifeExt) {
		olifeExt.setRequirementInfoExtension(new RequirementInfoExtension());
	}
	/**
	 * This function checks the RequirementInfoExtension object within a given OLifEExtension object
	 * @param olifeExt
	 * @param index
	 * @return
	 */
	//ACP019 New Method
	private boolean hasRequirementInfoExtension(OLifEExtension olifeExt, int index) {
		if (olifeExt.getRequirementInfoExtension() !=null) {
				return true;
		}
		return false;
	}
	/**
	* This function creates the TrackingInfo object within a given RequirementInfoExtension object
	* @param reqInfoExt
	*/
	//ACP019 New Method
	private void createTrackingInfo(RequirementInfoExtension reqInfoExt) {
		reqInfoExt.setTrackingInfo(new TrackingInfo());
	}
	/**
	 * This function checks the TrackingInfo object within a given RequirementInfoExtension object
	 * @param reqInfoExt
	 * @param index
	 * @return
	 */
	//ACP019 New Method
	private boolean hasTrackingInfo(RequirementInfoExtension reqInfoExt, int index) {
		if (reqInfoExt.getTrackingInfo() !=null)
			return true;
		return false;
	}
	/**
	 * Return a list containing the Temporary Flat SubstandardRatings for each Coverage or Rider.
	 * If the Coverage or Rider does not have a Permanent Flat SubstandardRating, the corresponding
	 * entry in the list is null;
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	protected List getTempFlatSubstandardRatings(NbaOinkRequest aNbaOinkRequest) {
		return getFlatSubstandardRatings(aNbaOinkRequest, true);
	}
	/**
	 * Return a list containing the Permanent Flat SubstandardRatings for each Coverage or Rider.
	 * If the Coverage or Rider does not have a Permanent Flat SubstandardRating, the corresponding
	 * entry in the list is null;
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	protected List getPermFlatSubstandardRatings(NbaOinkRequest aNbaOinkRequest) {
		return getFlatSubstandardRatings(aNbaOinkRequest, false);
	}
	/**
	 * Return a list containing the Permanent or Temporary Flat SubstandardRatings for each Coverage or Rider.
	 * If the Coverage or Rider does not have a Permanent or Temporary Flat SubstandardRating, the corresponding
	 * entry in the list is null;
	 * @param aNbaOinkRequest - data request container
	 * @param tempRating - true if temporary ratings are to be returned, false if permanent ratings are to be returned
	 */
	// NBA100 New Method
	protected List getFlatSubstandardRatings(NbaOinkRequest aNbaOinkRequest, boolean tempRating) {
		ArrayList extraRatings = new ArrayList();
		List lifeParticipants = getInsurableLifeParticipants(aNbaOinkRequest);
		int next = 0;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardRatingExt;
		SubstandardRating ratingForCov;
		while ((lifeParticipant = getNextLifeParticipant(lifeParticipants, next++)) != null) {//P2AXAL035 call to getNextLifeParticipant Modified
			ratingForCov = null;
			int countSR = lifeParticipant.getSubstandardRatingCount();
			for (int j = 0; j < countSR; j++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(j);
				if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
					if (tempRating) {
						if (substandardRating.hasTempFlatExtraAmt()) {
							ratingForCov = substandardRating;
							extraRatings.add(ratingForCov);	//AXAL3.7.13I
						}
					} else {
						substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
						if (substandardRatingExt != null && substandardRatingExt.hasPermFlatExtraAmt()) {
							ratingForCov = substandardRating;
							extraRatings.add(ratingForCov);	//AXAL3.7.13I
						}
					}
				}
			}
			
		}
		return extraRatings;
	}
	/**
	 * Return a list containing the Coverages or Riders based on the Qualifier value in 
	 * the NbaOinkRequest.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	protected List getCoveragesOrRiders(NbaOinkRequest aNbaOinkRequest) {
		List coverages = new ArrayList();
		Life life = getLife();
		if (life != null) {
			String qualifier = aNbaOinkRequest.getQualifier();
			if (qualifier.equals(BASE_COV)) {
				coverages.add(getBaseCoverage());
			} else if (qualifier.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else if (qualifier.equals(RIDER)) {
				coverages = getRider();
			} else {
				int origfilter = aNbaOinkRequest.getPartyFilter();
				int partyIdx = 0;
				//begin SPR2399
				boolean retreiveForAllParties = true;
				//if there is party filter, retreive coverages only related to that party.
				if (NbaOinkRequest.noFilterInt != origfilter || NbaOinkRequest.noFilterInt != aNbaOinkRequest.getRelationRoleCode()) {
					retreiveForAllParties = false;
					partyIdx = origfilter;
				}
				//end SPR2399				
				Party party;
				if (PARTY_PRIMARY_AND_JOINT_INSURED.equals(qualifier)) {
					aNbaOinkRequest.setQualifier(PARTY_PRIM_INSURED);	//Prevent coverages from being added twice
				}
				do { // examine all parties for role //SPR2399
					aNbaOinkRequest.setPartyFilter(partyIdx);
					party = getParty(aNbaOinkRequest, partyIdx);
					if (party == null) {
						partyIdx = -1;
					} else {
						int covIndx = 0;
						while (covIndx > -1) {
							covIndx = getCoverageForParty(aNbaOinkRequest, covIndx);
							if (covIndx != -1) {
								Coverage coverage = life.getCoverageAt(covIndx);
								if (!coverage.isActionDelete()) {
									coverages.add(life.getCoverageAt(covIndx)); // add Coverage for current role
								}
								covIndx++;
							} else {
								partyIdx++; //try next Party for role
								break;
							}
						}
					}
				} while (retreiveForAllParties && partyIdx > -1); //SPR2399
				aNbaOinkRequest.setPartyFilter(origfilter);
				aNbaOinkRequest.setQualifier(qualifier);
			}
		}
		return coverages;
	}
	/**
	 * Return a list containing the Insurable LifeParticipants for the Coverages or Riders 
	 * based on the Qualifier value in the NbaOinkRequest.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	protected List getInsurableLifeParticipants(NbaOinkRequest aNbaOinkRequest) {
		List coverages = new ArrayList();
		List participants = new ArrayList();
		Life life = getLife();
		if (life != null) {
			String coverageType = aNbaOinkRequest.getQualifier();
			if (BASE_COV.equals(coverageType) || NON_RIDER_COV.equals(coverageType) || RIDER.equals(coverageType)) {
				if (BASE_COV.equals(coverageType)) {
					coverages.add(getBaseCoverage());
				} else if (NON_RIDER_COV.equals(coverageType)) {
					coverages = getNonRider();
				} else if (RIDER.equals(coverageType)) {
					coverages = getRider();
				}
				int next = 0;
				Coverage coverage;
				while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
					participants.add(NbaUtils.getInsurableLifeParticipant(coverage));
				}
			} else {
				int partyIdx = 0;
				LifeParticipant aLifeParticipant;
				Party party;
				String personPartyID;
				Coverage coverage;
				int origIndx = aNbaOinkRequest.getPartyFilter();
				//begin SPR2399
				boolean retreiveForAllParties = true;
				//if there is party filter, retreive coverages only related to that party.   
				if (NbaOinkRequest.noFilterInt != origIndx || NbaOinkRequest.noFilterInt != aNbaOinkRequest.getRelationRoleCode()) {
					retreiveForAllParties = false;
					partyIdx = origIndx;
				}								
				do { 
				//end SPR2399
					aNbaOinkRequest.setPartyFilter(partyIdx);
					party = getParty(aNbaOinkRequest, partyIdx);
					if (party == null) {
						partyIdx = -1;
					} else {
						personPartyID = party.getId();
						int covIndx = 0;
						while (covIndx > -1) {
							covIndx = getCoverageForParty(aNbaOinkRequest, covIndx);
							if (covIndx != -1) {
								coverage = life.getCoverageAt(covIndx);
								int cnt = coverage.getLifeParticipantCount();
								for (int partIndx = 0; partIndx < cnt; partIndx++) {
									aLifeParticipant = coverage.getLifeParticipantAt(partIndx);
									if (personPartyID.equals(aLifeParticipant.getPartyID())) {
										participants.add(aLifeParticipant);
										break;
									}
								}
								covIndx++;
							} else {
								partyIdx++;
								break;
							}
						}
					}
				} while (retreiveForAllParties && partyIdx > -1); //SPR2399				
				aNbaOinkRequest.setPartyFilter(origIndx);
			}
		}
		return participants;
	}
	/**
	 * Return true if the qualifier is COV or RDR. These qualifiers are typically used
	 * by VPMS models.
	 * @param aNbaOinkRequest
	 * @return
	 */
	// NBA100 ACP002 New Method
	protected boolean isCOVOrRDRQualifier(NbaOinkRequest aNbaOinkRequest) {
		String qual = aNbaOinkRequest.getQualifier();
		return NON_RIDER_COV.equals(qual) || RIDER.equals(qual);
	}
	/**
	 * This function retrieves a Form Instance Object based on the Related Object Id
	 * @param aNbaOinkRequest - data request container	 
	 * @return OLifE.FormInstance
	 */	
	//ACP005 New Method
	protected FormInstance getFormInstanceByRelatedObjectType(NbaOinkRequest aNbaOinkRequest){
		ArrayList formInstanceList = oLifE.getFormInstance();
		Party party = getParty(aNbaOinkRequest,0);
		Risk risk = null;
		LifeStyleActivity lifeStyleActivity = null;
		SubstanceUsage	substanceUsage = null;
		Violation violation = null;
		CriminalConviction criminalConviction = null;
		MedicalCondition medicalCondition = null;
		//Get the related object type from oink request
		String relatedObjectType = aNbaOinkRequest.getRelatedObjectTypeFilter();
		int elementIndex = aNbaOinkRequest.getElementIndexFilter();
		String objectId = null;
		if(formInstanceList !=null && formInstanceList.size() != 0){		
			if(party != null && relatedObjectType !=null && !relatedObjectType.equals("")){
				risk = party.getRisk();
				if(risk != null){
					//Retrieve the Related Object ID through the Related Object Type
					if(relatedObjectType.equals(String.valueOf(NbaOliConstants.OLI_LIFESTYLEACTIVITY))){
						lifeStyleActivity = risk.getLifeStyleActivityAt(elementIndex);
						if(lifeStyleActivity !=null){
							objectId = lifeStyleActivity.getId();
						}
					}else if(relatedObjectType.equals(String.valueOf(NbaOliConstants.OLI_CRIMCONVICTION))){
						criminalConviction = risk.getCriminalConvictionAt(elementIndex);
						if(criminalConviction != null){
							objectId = criminalConviction.getId();
						}					
					}else if(relatedObjectType.equals(String.valueOf(NbaOliConstants.OLI_VIOLATION))){
						violation = risk.getViolationAt(elementIndex);
						if(violation !=null){
							objectId = violation.getId();
						}					
					}else if(relatedObjectType.equals(String.valueOf(NbaOliConstants.OLI_SUBSTANCEUSAGE))){
						substanceUsage = risk.getSubstanceUsageAt(elementIndex);
						if(substanceUsage !=null){
							objectId = substanceUsage.getId();
						}
					}else if(relatedObjectType.equals(String.valueOf(NbaOliConstants.OLI_MEDCONDITION))){
						medicalCondition = risk.getMedicalConditionAt(elementIndex);
						if(medicalCondition != null){
							objectId = medicalCondition.getId();
						}// ACP008 Begin
					}else if(relatedObjectType.equals(String.valueOf(NbaOliConstants.OLI_PARTY))){
						objectId = party.getId();
				    }// ACP008 End
				}
			}
			if(objectId != null){
				int count = formInstanceList.size();
				FormInstance formInstance = null;
				for(int i=0; i<count; i++){
					formInstance = (FormInstance) formInstanceList.get(i);
					if(formInstance!=null && objectId.equals(formInstance.getRelatedObjectID())){	//AXAL3.7.02
						return formInstance;						
					}
				}
			}
		}
		return null;
	}
	/*
	 * Return the next Coverage or null.
	 */
	//NBA100 New Method	 
	protected Coverage getNextCoverage(NbaOinkRequest aNbaOinkRequest, List coverages, int next) {
		if (next < coverages.size() && next < aNbaOinkRequest.getCount()) {
			return (Coverage) coverages.get(next);
		}
		return null;
	}
	
	/*
	 * Return the next LifeParticipant or null.
	 */
	//NBA100 New Method	 
	protected LifeParticipant getNextLifeParticipant(NbaOinkRequest aNbaOinkRequest, List lifeParticipants, int next) {
		if (next < lifeParticipants.size() && next < aNbaOinkRequest.getCount()) {
			return (LifeParticipant) lifeParticipants.get(next);
		}
		return null;
	}
	
	/*
	 * Return the next LifeParticipant or null.
	 */
	//P2AXAL035 New Method
	protected LifeParticipant getNextLifeParticipant(List lifeParticipants, int next) {
		if (next < lifeParticipants.size()) {
			return (LifeParticipant) lifeParticipants.get(next);
		}
		return null;
	}
	
	/**
	 * Get the LifeStyleActivity object.
	 * @param aNbaOinkRequest
	 * @return
	 */
	//ACP016 new method
	protected LifeStyleActivity getLifeStyleActivity(NbaOinkRequest aNbaOinkRequest){
		
		int index = aNbaOinkRequest.getElementIndexFilter();
		Party party = getParty(aNbaOinkRequest,0);
		if(party!=null){
			Risk risk = party.getRisk();
			if(risk!= null){
				ArrayList lifeStyleActivityList = risk.getLifeStyleActivity();
				if(index <= lifeStyleActivityList.size()){
					return (LifeStyleActivity)lifeStyleActivityList.get(index);
				}
			}
		}		
		return null; 
	}
	
	/**
	 * Get the AirSportsExp object.
	 * @param aNbaOinkRequest
	 * @return
	 */
	//ACP016 new method	
	protected AirSportsExp getAirSportsExp(NbaOinkRequest aNbaOinkRequest){
		AirSportsExp airSportsExp = null;
		LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest);
		if(lifeStyleActivity!=null){		
			Object obj = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr$Contents();
			if ((obj == null) || !(obj instanceof AirSportsExp)) {
				if (isUpdateMode()) {
					createAirSportsExp(lifeStyleActivity);
					airSportsExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAirSportsExp();
					setActionAdd(airSportsExp); 
				} else {
					return null;
				}
			}
		}
		airSportsExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAirSportsExp();
		setActionUpdate(airSportsExp);
		return airSportsExp;
	}
	
	/**
	 * Get the AviationExp object.
	 * @param aNbaOinkRequest
	 * @return
	 */
	//ACP016 new method
	protected AviationExp getAviationExp(NbaOinkRequest aNbaOinkRequest){
		AviationExp aviationExp = null;		
		LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest);
		if (lifeStyleActivity != null) {
			Object obj = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr$Contents();
			if ((obj == null) || !(obj instanceof AviationExp)) {
				if (isUpdateMode()) {
					createAviationExp(lifeStyleActivity);
					aviationExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAviationExp();
					setActionAdd(aviationExp); 
				} else {
					return null;
				}
			}			
			aviationExp = lifeStyleActivity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr().getAviationExp();
		}
		setActionUpdate(aviationExp);
		return aviationExp;
	}
	
	/**
	 * Process all the banking objects in Holding and returns an ArrayList with single banking object 
	 * if called for a credit card payment and list of non-credit card payment banking objects if called otherwise   
	 * @param aNbaOinkRequest data request container
	 * @return List A list containing Banking objects 
	 */
	//NBA115 New Method
	protected List getBankingList(NbaOinkRequest aNbaOinkRequest) {
		List bankingList = new ArrayList();
		Banking banking = null;
		int bankingCount = getHolding().getBankingCount();
		
		if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) { //return list with single banking object for CC payments
			for (int i = 0; i < bankingCount; i++) {
				banking = getHolding().getBankingAt(i);
				if (NbaUtils.isCreditCardPayment(banking)) {
						bankingList.add(banking);
						break;
				}
			}
		} else { //return list of banking objects other then cc payment
			for (int i = 0; i < bankingCount; i++) {
				banking = getHolding().getBankingAt(i);
				if (!NbaUtils.isCreditCardPayment(banking)) {
					bankingList.add(banking);
				}
			}
		}
		return bankingList;
	}

	/**
	 * Creates banking and banking extension objects and adds the banking object to holding 
	 * @return Banking newly created Banking object
	 */
	//NBA115 New Method
	protected Banking createBanking() {
		Banking banking = new Banking();
		OLifEExtension extension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);
		// SPR3290 code deleted
		banking.addOLifEExtension(extension);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(getNbaTXLife());
		nbaOLifEId.setId(banking);
		setActionAdd(banking);
		getHolding().addBanking(banking);
		return banking;
	}
	
	/**
	 * Initialize the credit card payment. Sets banking and extension object default values
	 * for a credit card payment banking object. If a credit card payment facilitator party is not already present, 
	 * this method also creates one.
	 * @param banking Banking object worked upon
	 */
	//NBA115 New Method 
	protected void initCreditCardPayment(Banking banking){
		Party party = getParty(CREDIT_CARD_PAYMENT + "0");
		Address address = getAddressForType(party, NbaOliConstants.OLI_ADTYPE_HOME);
		banking.setBankAcctType(NbaOliConstants.OLI_BANKACCT_CREDCARD);
		BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);
		//assumes that BankingExtension was created by createBanking()
		bankingExt.setCreditCardChargeUse(NbaOliConstants.OLIEXT_LU_CHARGEUSE_PAYMENT);
		bankingExt.setAppliesToPartyID(party.getId());
		bankingExt.setMailingAddressID(address.getId());
	}
	
	/**
	 * Checks if the account holder name is present at given index on the passed Banking object
	 * @param banking banking object to be worked upon
	 * @param index account holder name index 
	 * @return boolean true if account holder name is present at given index on banking, false otherwise
	 */
	protected boolean hasAcctHolderName(Banking banking, int index) {
		boolean present = false;
		if (banking != null) {
			BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
			if (bankingExtn != null) {
				AccountHolderNameCC accHolderNameCC = bankingExtn.getAccountHolderNameCC();
				if ((accHolderNameCC != null) && (index < accHolderNameCC.getAccountHolderNameCount())) {
					present = accHolderNameCC.getAccountHolderNameCount() > index;
				}
			}
		}
		return present;
	}
	
	/**
	 * Set account holder name for given banking at passed index value. If count of account holders is less then 
	 * index value, add it at the end.
	 * @param banking banking object worked upon
	 * @param value value to be set
	 * @param index position of name to be set in the names list
	 */
	//NBA115 New Method
	protected void setAcctHolderName(Banking banking, String value, int index) {
		BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
		int accHolderNameCount = 0;
		if (bankingExtn != null) {
			AccountHolderNameCC acntHolderCC = null;
			if (!bankingExtn.hasAccountHolderNameCC()) {
				acntHolderCC = new AccountHolderNameCC();
				bankingExtn.setAccountHolderNameCC(acntHolderCC);
				setActionAdd(acntHolderCC);
			} else {
				acntHolderCC = bankingExtn.getAccountHolderNameCC();
				accHolderNameCount = acntHolderCC.getAccountHolderNameCount();
				setActionUpdate(acntHolderCC);
			}
			if (index < accHolderNameCount) {
				acntHolderCC.setAccountHolderNameAt(value, index);
			} else {
				acntHolderCC.addAccountHolderName(value);
			}
		}
	}

	/**
	 * Returns the Party for an agency related to an agent. 
	 * @param aNbaOinkRequest
	 * @param index
	 * @return
	 */
	//NBA132 New Method
	protected Party getAgencyParty(NbaOinkRequest aNbaOinkRequest, int index) {
		Party agentParty = null;
		Party agencyParty = null;
		//must find the agent first before we can find the agency
		if (PARTY_PRIWRITINGAGENTAGENCY.equals(aNbaOinkRequest.getQualifier())) {
			//adjust the OINK request to find the agent
			aNbaOinkRequest.setQualifier(PARTY_PRIWRITINGAGENT);
			agentParty = getParty(aNbaOinkRequest, index);
			//reset the qualifier back to the way it was
			aNbaOinkRequest.setQualifier(PARTY_PRIWRITINGAGENTAGENCY);
		}
		if (agentParty != null) {
			Relation relation;
			for (int i = 0; i < getOLifE().getRelationCount(); i++) {
				relation = getOLifE().getRelationAt(i);
				if (relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_AGENCYOF
					&& relation.getOriginatingObjectID().equals(agentParty.getId())) {
					NbaParty nbaParty = getNbaTXLife().getParty(relation.getRelatedObjectID());
					if (nbaParty != null) {
						agencyParty = nbaParty.getParty();
						break;
					}
				}
			}
		}
		return agencyParty;
	}
	/**
	 * This function retrieves a Investment object.
	 * @return com.csc.fsg.nba.vo.txlife.Policy
	 */
	//New method NBA139
	protected Investment getInvestment() {
		if (!getHolding().hasPolicy()) { 
			getHolding().setInvestment(new Investment());
			setActionAdd(getHolding().getInvestment());
		}
		Investment investment = getHolding().getInvestment();
		setActionUpdate(investment);
		return investment;
	}
	
    /**
     * @return Returns the holding.
     */
    //New method NBA139
    public Holding getHolding() {
        return NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());
    }
	/**
	 * @return Returns the primaryHoldingID.
	 */
    //ALS4028 new method
	public String getPrimaryHoldingID() {
		if (null != getHolding()) {
			return getHolding().getId();
		}
		return null;
	}
	
	//New Method P2AXAL028
	public ArrayList getPartiesByQualifier(NbaOinkRequest aNbaOinkRequest){
		ArrayList partyList = new ArrayList();
		StringBuffer hashKey = new StringBuffer();
		String qual= aNbaOinkRequest.getQualifier();
		if(!NbaUtils.isBlankOrNull(qual)){
			hashKey.append(qual);
			int i=0;
			hashKey.append(i);
			while (getIndices().containsKey(hashKey.toString())) {
				Integer index = (Integer) getIndices().get(hashKey.toString());
				if (index != null) {
					Party party = getOLifE().getPartyAt(index.intValue());
					if (!party.isActionDelete()) {
						partyList.add(party);
					}
					hashKey.deleteCharAt(qual.length());
					hashKey.append(++i);
				}
			}
		}else{
			partyList = getOLifE().getParty();
		}
		return partyList;
	}
	

	/**
	 * @param aNbaOinkRequest
	 * @return the Form instance based on the Qualifier
	 */
	//P2AXAL035 New Method	
	public FormInstance getFormInstanceByQualifier(NbaOinkRequest aNbaOinkRequest){
		ArrayList formInstanceList = getOLifE().getFormInstance();
		int listSize = formInstanceList.size();
		FormInstance formInstance = null;
		for (int i = 0; i < listSize; i++) {
			formInstance = (FormInstance) formInstanceList.get(i);
			if (formInstance.getFormName() != null) {
				if (formInstance.getFormName().equals(getFormNameMap().get(aNbaOinkRequest.getQualifier()))) {
					return formInstance;
				}
			}
		}
		return null;	
	}
	
	//P2AXAL016 new method
	public Coverage getCoverageByQualifier(NbaOinkRequest aNbaOinkRequest){
		String coverageType = aNbaOinkRequest.getQualifier();
		Coverage coverage = null;
		if (BASE_COV.equals(coverageType)) {
			coverage = getBaseCoverage();
		} else if (CTIR_COV.equals(coverageType)) {
			coverage = getCoverageByTypeCode(NbaOliConstants.OLI_COVTYPE_CHILDTERM);
		} else if (CLR_COV.equals(coverageType)) {
			coverage = getCoverageByTypeCode(NbaOliConstants.OLI_COVTYPE_CLR);
		}
		return coverage;
	}

	//P2AXAL062 new method
	public LifeParticipant getLifeParticipant(NbaOinkRequest aNbaOinkRequest){
		LifeParticipant aLifeParticipant = null;
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		Party party = getParty(aNbaOinkRequest, aNbaOinkRequest.getPartyFilter());
		if(coverage!= null && party != null){
			aLifeParticipant = NbaUtils.findInsuredLifeParticipant(coverage, party.getId()); 
		}
		return aLifeParticipant;
	}
	
	/**
	 * @return Returns the formNameMap.
	 */
	 //P2AXAL035
	public Map getFormNameMap() {
		return formNameMap;
	}
	
	/**
	 * @param formNameMap The formNameMap to set.
	 */
	 //P2AXAL035
	public void setFormNameMap(Map formNameMap) {
		this.formNameMap = formNameMap;
	}
	/**
	 * @return Returns the commonVal.
	 */
	 //P2AXAL016 new method
	public NbaContractValidationCommon getCommonVal() {
		return commonVal;
	}
	/**
	 * @param commonVal The commonVal to set.
	 */
	 //P2AXAL016 new method
	public void setCommonVal(NbaContractValidationCommon commonVal) {
		this.commonVal = commonVal;
	}
	
	//P2AXAL024 new method
	public UnderwritingClassProduct getUWClassProductByQualifier(NbaOinkRequest aNbaOinkRequest) {
		UnderwritingClassProduct underwritingClassProduct = null;
		CovOption covOption = null;
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		LifeParticipant lifeParticipant = getLifeParticipant(aNbaOinkRequest);//P2AXAL062
		if (coverage != null && lifeParticipant!= null) {//P2AXAL062
			if (aNbaOinkRequest.getCovOptionFilter() != -1) {
				covOption = getCovOption(coverage, (long) aNbaOinkRequest.getCovOptionFilter());
			}
			if (covOption != null) {
				underwritingClassProduct = getCommonVal().getUnderwritingClassProduct(coverage, lifeParticipant, covOption);//P2AXAL062
			} else {
				underwritingClassProduct = getCommonVal().getUnderwritingClassProductFor(coverage,lifeParticipant, NbaContractValidationConstants.POS1);//P2AXAL062
				if(underwritingClassProduct == null)
					underwritingClassProduct = getCommonVal().getUnderwritingClassProductFor(coverage, lifeParticipant, 0);//P2AXAL062
			}
		}
		return underwritingClassProduct;
	}	
	
	// A2_AXAL003 New Method
	protected boolean getUWReqFormInd(NbaOinkRequest aNbaOinkRequest, long typeCode) {
		//A3_AXAL005 start
		for (int indx = 0; indx < aNbaOinkRequest.getCount(); indx++) {
			Party aParty = getParty(aNbaOinkRequest, indx);
			if (aParty != null) {
				//A3_AXAL005 end
				Client client = aParty.getClient();
				if (client != null) {
					ClientExtension clientExt = NbaUtils.getFirstClientExtension(client);
					if (clientExt != null) {
						if (clientExt.getClientAcknowledgeInfo() != null) {
							UWReqFormsCC uwReqFormsCC = clientExt.getClientAcknowledgeInfo().getUWReqFormsCC();
							if (uwReqFormsCC != null && uwReqFormsCC.getUWReqFormsTCCount() > 0) {
								for (int i = 0; i < uwReqFormsCC.getUWReqFormsTCCount(); i++) {
									if (uwReqFormsCC.getUWReqFormsTCAt(i) == typeCode) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	//P2AXAL024 new method //P2AXAL062 modified signature
	public UnderwritingClassProduct getUWClassProductForCoverage(Coverage coverage, LifeParticipant lifeParticipant) {
		UnderwritingClassProduct underwritingClassProduct = null;
		underwritingClassProduct = getCommonVal().getUnderwritingClassProductFor(coverage, lifeParticipant, NbaContractValidationConstants.POS1);//P2AXAL062
		if (underwritingClassProduct == null)
			underwritingClassProduct = getCommonVal().getUnderwritingClassProductFor(coverage, lifeParticipant, 0);//P2AXAL062
		return underwritingClassProduct;
	}
	
	//A4_AXAL001
	public TempInsAgreementDetails getTempInsAgreementDetails(TempInsAgreementInfo tempInsAgreementInfo, String partyId) {
		TempInsAgreementDetails tempInsAgreementDetails = null;
		for (int i = 0; i < tempInsAgreementInfo.getTempInsAgreementDetailsCount(); i++) {
			if (tempInsAgreementInfo.getTempInsAgreementDetailsAt(i).getPartyID().equalsIgnoreCase(partyId)) {
				tempInsAgreementDetails = tempInsAgreementInfo.getTempInsAgreementDetailsAt(i);
				break;
			}
		}
		return tempInsAgreementDetails;
	}

	/**
	 * Return a list containing the Insurable LifeParticipants for the Coverages or Riders or Party
	 * based on the Qualifier value in the NbaOinkRequest.
	 * @param aNbaOinkRequest - data request container
	 */
	// CR1343973 New Method
	protected List getLifeParticipants(NbaOinkRequest aNbaOinkRequest) {
		List coverages = new ArrayList();
		List participants = new ArrayList();
		Life life = getLife();
		if (life != null) {
			String coverageType = aNbaOinkRequest.getQualifier();
			if (BASE_COV.equals(coverageType) || NON_RIDER_COV.equals(coverageType) || RIDER.equals(coverageType)) {
				if (BASE_COV.equals(coverageType)) {
					coverages.add(getBaseCoverage());
				} else if (NON_RIDER_COV.equals(coverageType)) {
					coverages = getNonRider();
				} else if (RIDER.equals(coverageType)) {
					coverages = getRider();
				}
				int next = 0;
				Coverage coverage;
				while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
					participants.add(NbaUtils.getInsurableLifeParticipant(coverage));
				}
			} else {
				int partyIdx = 0;
				LifeParticipant aLifeParticipant;
				Party party;
				String personPartyID;
				Coverage coverage;				
				party = getParty(aNbaOinkRequest, partyIdx);
				if (party != null) {					
					personPartyID = party.getId();
					int covIndx = 0;
					while (covIndx > -1) {
						covIndx = getCoverageForParty(aNbaOinkRequest, covIndx);
						if (covIndx != -1) {
							coverage = life.getCoverageAt(covIndx);
							int cnt = coverage.getLifeParticipantCount();
							for (int partIndx = 0; partIndx < cnt; partIndx++) {
								aLifeParticipant = coverage.getLifeParticipantAt(partIndx);
								if (personPartyID.equals(aLifeParticipant.getPartyID())) {
									participants.add(aLifeParticipant);
									break;
								}
							}
							covIndx++;
						} 
					}
				}
			}
		}
		return participants;
	}
	
	/**
	 * Return a list containing the Permanent or Temporary Flat SubstandardRatings for a party.
	 * @param aNbaOinkRequest - data request container
	 * @param tempRating - true if temporary ratings are to be returned, false if permanent ratings are to be returned
	 */
	// CR1343973 New Method
	protected List getFlatSubstandardRatingsForParty(NbaOinkRequest aNbaOinkRequest, boolean tempRating) {
		ArrayList extraRatings = new ArrayList();
		List lifeParticipants = getLifeParticipants(aNbaOinkRequest);
		int next = 0;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardRatingExt;
		SubstandardRating ratingForCov;
		while ((lifeParticipant = getNextLifeParticipant(lifeParticipants, next++)) != null) {
			ratingForCov = null;
			int countSR = lifeParticipant.getSubstandardRatingCount();
			for (int j = 0; j < countSR; j++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(j);
				if (NbaUtils.isValidRating(substandardRating)) {
					if (tempRating) {
						if (substandardRating.hasTempFlatExtraAmt()) {
							ratingForCov = substandardRating;
							extraRatings.add(ratingForCov);
						}
					} else {
						substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
						if (substandardRatingExt != null && substandardRatingExt.hasPermFlatExtraAmt()) {
							ratingForCov = substandardRating;
							extraRatings.add(ratingForCov);
						}
					}
				}
			}
		}
		return extraRatings;
	}
	
	/**
	 * Return a list containing the Temporary Flat SubstandardRatings for a party.
	 * @param aNbaOinkRequest - data request container
	 */
	// CR1343973 New Method
	protected List getTempFlatSubstandardRatingsForParty(NbaOinkRequest aNbaOinkRequest) {
		return getFlatSubstandardRatingsForParty(aNbaOinkRequest, true);
	}
	
	/**
	 * Return a list containing the Temporary Flat SubstandardRatings for each Coverage or Rider.
	 * If the Coverage or Rider does not have a Permanent Flat SubstandardRating, the corresponding
	 * entry in the list is null;
	 * @param aNbaOinkRequest - data request container
	 */
	// CR1343973 New Method
	protected List getPermFlatSubstandardRatingsForParty(NbaOinkRequest aNbaOinkRequest) {
		return getFlatSubstandardRatingsForParty(aNbaOinkRequest, false);
	}
	
	//APSL3619 New Method
	protected List getRequirementInfosByReqCode(NbaOinkRequest nbaOinkRequest) {
		Party party = getPartyForPrimaryIns();
		if (party != null) {
			// return the requirement for the specified id filter
			if (!NbaUtils.isBlankOrNull(Long.parseLong(nbaOinkRequest.getQualifier()))) {
				return getNbaTXLife().getRequirementInfoList(party.getId(), Long.parseLong(nbaOinkRequest.getQualifier()));
			}
		}
		return new ArrayList();
	}			
}