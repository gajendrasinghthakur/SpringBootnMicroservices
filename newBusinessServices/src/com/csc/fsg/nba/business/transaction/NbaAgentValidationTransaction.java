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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.AnnuityRiderExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.Rider;

/**
 * NbaAgentValidationTransaction will be used to create Agent validation transaction(514) for WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA073</td><td>Version 3</td><td>Agent Validation</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaAgentValidationTransaction extends NbaBusinessTransactions{
	/**
	 * Constructor for NbaAgentValidationTransaction
	 */
	public NbaAgentValidationTransaction() {
		super();
	}

	/**
	 * This method takes three parameters and creates Agent validation transaction, which will be passed to the WebService
	 * for validation.
	 * @param existingTXLife An instance of <code>NbaTXLife</code> holding inquiry
	 * @param existingRelation An instance of <code>Relation</code> which is either Primary writing agent or Additional writing agent
	 * @param nbaDst An instance of <code>NbaDst</code>
	 * @return NbaTXLife agent validation transaction
	 * @throws NbaBaseException
	 */
	// NBA132 changed method name
	public NbaTXLife createRequest(NbaTXLife existingTXLife, Relation existingRelation, NbaDst nbaDst) throws NbaBaseException {
		Holding existingHolding = existingTXLife.getPrimaryHolding();
		//existing Holding object
		Policy existingPolicy = existingHolding.getPolicy(); //existing policy

		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PRODUCER);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_TYPE_PRODUCERSUBTYPE_AGENTVALIDATION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess("");
		nbaTXRequest.setNbaLob(nbaDst.getNbaLob());

		//create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);  //NBA132
		// NBA132 deleted code
		Policy policy = txLife.getPolicy();  //NBA132
		policy.setProductType(existingPolicy.getProductType());
		//NBA112 code deleted
		policy.setCarrierCode(existingPolicy.getCarrierCode());
		//NBA112 code deleted
		policy.setIssueDate(existingPolicy.getIssueDate()); //NBA112
		policy.setEffDate(existingPolicy.getEffDate());	//NBA112
		policy.setTermDate(existingPolicy.getTermDate()); //SPR1986
		//Life Object
		if (existingPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) {
			Life existingLife = existingPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth =
				new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			Life life = new Life(); //new Life object
			life.setId(existingLife.getId());//ALPC7

			life.setFaceAmt(existingLife.getFaceAmt());
			lifeOrAnnuityOrDisabilityHealth.setLife(life);
			policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeOrAnnuityOrDisabilityHealth);

			//Coverage Object
			for (int i = 0; i < existingLife.getCoverageCount(); i++) {
				Coverage existingCoverage = existingLife.getCoverageAt(i);
				Coverage coverage = new Coverage(); //new Coverage object
				coverage.setIndicatorCode(existingCoverage.getIndicatorCode());	//NBA112
				coverage.setId(existingCoverage.getId());
				coverage.setCoverageKey(existingCoverage.getCoverageKey());
				coverage.setProductCode(existingCoverage.getProductCode());
				//NBA112 code deleted
				coverage.setEffDate(existingCoverage.getEffDate());//NBA112
				coverage.setTermDate(existingCoverage.getTermDate());
				coverage.setLifeCovTypeCode(existingCoverage.getLifeCovTypeCode());
				coverage.setCurrentAmt(existingCoverage.getCurrentAmt());
				life.addCoverage(coverage);

				//LifeParticipant Object
				for (int j = 0; j < existingCoverage.getLifeParticipantCount(); j++) {
					LifeParticipant existingLifeParticipant = existingCoverage.getLifeParticipantAt(j);
					LifeParticipant lifeParticipant = new LifeParticipant();

					lifeParticipant.setId(existingLifeParticipant.getId());
					lifeParticipant.setLifeParticipantRoleCode(existingLifeParticipant.getLifeParticipantRoleCode());
					lifeParticipant.setIssueAge(existingLifeParticipant.getIssueAge());
					coverage.addLifeParticipant(lifeParticipant);
				}

				//CoverageExtension Object
				CoverageExtension existingCoverageExtension = NbaUtils.getFirstCoverageExtension(existingCoverage);
				// NBA112 code deleted
				if (existingCoverageExtension != null) {					  
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVERAGE); //NBA112
					CoverageExtension coverageExtension = olifeExt.getCoverageExtension();	//NBA112					 
					coverage.addOLifEExtension(olifeExt);	//NBA112
					coverageExtension.setCommissionPlanCode(existingCoverageExtension.getCommissionPlanCode());
					coverageExtension.setValuationClassType(existingCoverageExtension.getValuationClassType());
					coverageExtension.setValuationBaseSeries(existingCoverageExtension.getValuationBaseSeries());
					coverageExtension.setValuationSubSeries(existingCoverageExtension.getValuationSubSeries());
					coverageExtension.setPayUpDate(existingCoverageExtension.getPayUpDate());
					// NBA112 code deleted
				}
			}
		}

		//Annuity Object
		if (existingPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) {
			Annuity existingAnnuity = existingPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth =
				new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			Annuity annuity = new Annuity();

			lifeOrAnnuityOrDisabilityHealth.setAnnuity(annuity);
			policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeOrAnnuityOrDisabilityHealth);
			//NBA112 code deleted
			annuity.setAnnuityKey(existingAnnuity.getAnnuityKey());

			//Annuity.Payout Object
			for (int i = 0; i < existingAnnuity.getPayoutCount(); i++) {
				Payout existingPayout = existingAnnuity.getPayoutAt(i);
				Payout payout = new Payout();
				annuity.addPayout(payout);

				//Annuity.Payout.Participant Object
				for (int j = 0; j < existingPayout.getParticipantCount(); j++) {
					Participant existingParticipant = existingPayout.getParticipantAt(j);
					Participant participant = new Participant();
					participant.setParticipantRoleCode(existingParticipant.getParticipantRoleCode());
					participant.setIssueAge(existingParticipant.getIssueAge());
					payout.addParticipant(participant);
				}
			}

			//AnnuityExtension Object
			AnnuityExtension existingAnnuityExtension = NbaUtils.getFirstAnnuityExtension(existingAnnuity);
			// NBA112 code deleted
			if (existingAnnuityExtension != null) {
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ANNUITY); //NBA112
				AnnuityExtension annuityExtension = olifeExt.getAnnuityExtension();	//NBA112				 
				annuity.addOLifEExtension(olifeExt);	//NBA112
				annuityExtension.setCommissionPlanCode(existingAnnuityExtension.getCommissionPlanCode());
				annuityExtension.setValuationClassType(existingAnnuityExtension.getValuationClassType());
				annuityExtension.setValuationBaseSeries(existingAnnuityExtension.getValuationBaseSeries());
				annuityExtension.setValuationSubSeries(existingAnnuityExtension.getValuationSubSeries());
				// SPR1986 code deleted
				// NBA112 code deleted
			}

			//Annuity.Rider Object
			for (int i = 0; i < existingAnnuity.getRiderCount(); i++) {
				Rider existingRider = existingAnnuity.getRiderAt(i);
				Rider rider = new Rider();
				rider.setId(existingRider.getId());
				rider.setRiderKey(existingRider.getRiderKey());
				//NBA112 code deleted
				rider.setEffDate(existingRider.getEffDate());

				//Annuity.Rider.Participant
				rider.setParticipant(existingRider.getParticipant());	//NBA112

				//AnnuityRiderExtension
				AnnuityRiderExtension existingAnnRiderExt = NbaUtils.getFirstAnnuityRiderExtension(existingRider);
				// NBA112 code deleted
				if (existingAnnRiderExt != null) {
					//NBA112 code deleted
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ANNUITYRIDER);	//NBA112
					AnnuityRiderExtension annRiderExt = olifeExt.getAnnuityRiderExtension();	//NBA112
					rider.addOLifEExtension(olifeExt);	//NBA112
					annRiderExt.setCommissionPlanCode(existingAnnRiderExt.getCommissionPlanCode());
					annRiderExt.setPayUpDate(existingAnnRiderExt.getPayUpDate());
					annRiderExt.setValuationClassType(existingAnnRiderExt.getValuationClassType());
					annRiderExt.setValuationBaseSeries(existingAnnRiderExt.getValuationBaseSeries());
					annRiderExt.setValuationSubSeries(existingAnnRiderExt.getValuationSubSeries());
					// NBA112 code deleted					
				}
				annuity.addRider(rider);
			}
		}

		//Policy.ApplicationInfo object
		if (existingPolicy.hasApplicationInfo()) {
			ApplicationInfo existingApplicationInfo = existingPolicy.getApplicationInfo();
			ApplicationInfo applicationInfo = new ApplicationInfo();
			applicationInfo.setApplicationType(existingApplicationInfo.getApplicationType());
			applicationInfo.setApplicationJurisdiction(existingApplicationInfo.getApplicationJurisdiction());
			applicationInfo.setSignedDate(existingApplicationInfo.getSignedDate());

			policy.setApplicationInfo(applicationInfo);
		}

		OLifE existingOLifE = existingTXLife.getOLifE();
		OLifE olife = txLife.getOLifE();  //NBA132

		String party_id = existingRelation.getRelatedObjectID();
		for (int i = 0; i < existingOLifE.getPartyCount(); i++) {
			Party existingParty = existingOLifE.getPartyAt(i);
			if (existingParty.getId().equals(party_id)) {
				Party party = new Party();
				party.setId(party_id);
				if (existingParty.hasProducer()) {
					Producer existingProducer = existingParty.getProducer();
					Producer producer = new Producer();
					for (int j = 0; j < existingProducer.getCarrierAppointmentCount(); j++) {
						CarrierAppointment existingCarrierAppointment = existingProducer.getCarrierAppointmentAt(j);
						CarrierAppointment carrierAppointment = new CarrierAppointment();
						carrierAppointment.setCompanyProducerID(existingCarrierAppointment.getCompanyProducerID());
						carrierAppointment.setPartyID(existingCarrierAppointment.getPartyID()); //NBA112
						//NBA112 code deleted
						producer.addCarrierAppointment(carrierAppointment);
					}
					party.setProducer(producer);
				}
				olife.addParty(party);
			}
		}

		Relation relation = new Relation();
		relation.setId(existingRelation.getId());
		relation.setRelatedObjectID(existingRelation.getRelatedObjectID());
		relation.setOriginatingObjectID(existingRelation.getOriginatingObjectID());
		relation.setInterestPercent(existingRelation.getInterestPercent());
		relation.setRelationRoleCode(existingRelation.getRelationRoleCode());
		relation.setVolumeSharePct(existingRelation.getVolumeSharePct());

		RelationProducerExtension existingRelationProducerExtension = NbaUtils.getFirstRelationProducerExtension(existingRelation);
		// NBA112 code deleted		
		if (existingRelationProducerExtension != null) {
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RELATIONPRODUCER);	//NBA112
			RelationProducerExtension relationProducerExtension = olifeExt.getRelationProducerExtension();	//NBA112
			relation.addOLifEExtension(olifeExt);	//NBA112
			relationProducerExtension.setSituationCode(existingRelationProducerExtension.getSituationCode());
			// NBA112 code deleted			
		}
		olife.addRelation(relation);
		return txLife;  //NBA132
	}

	/**
	 * Updates the agency information on a party from a response agency party.
	 * @param agency existing on the contract
	 * @param responseAgency agency from the validation transaction response
	 * @param nbaOLifEId unique refid generator
	 * @return
	 */
	// NBA132 New Method
	public Party updateAgency(Party agency, Party responseAgency, NbaOLifEId nbaOLifEId) {
		CarrierAppointment agencyAppt = getCarrierAppointment(agency);
		CarrierAppointment responseAppt = getCarrierAppointment(responseAgency);
		if (responseAppt != null) {
			if (agencyAppt == null) {
				agency = createCarrierAppointment(agency, nbaOLifEId);
				agencyAppt = getCarrierAppointment(agency);
			}
			if (responseAppt.hasCompanyProducerID() && !responseAppt.getCompanyProducerID().equals(agencyAppt.getCompanyProducerID())) {
				agencyAppt.setCompanyProducerID(responseAppt.getCompanyProducerID());
				agencyAppt.setActionUpdate();
			}
		}
		return agency;
	}

	/**
	 * Returns the carrier appointment from the party.
	 * @param party an agency party
	 * @return
	 */
	// NBA132 New Method
	protected CarrierAppointment getCarrierAppointment(Party party) {
		CarrierAppointment appt = null;
		if (party != null && party.hasProducer()) {
			Producer producer = party.getProducer();
			if (producer.getCarrierAppointmentCount() > 0) {
				appt = producer.getCarrierAppointmentAt(0);
			}
		}
		return appt;
	}

	/**
	 * Creates a new CarrierAppointment on a party.  It will also create the <code>Producer</code>
	 * instance if it does not already exist.
	 * @param party an agency party
	 * @param nbaOLifEId unique refid generator
	 * @return
	 */
	// NBA132 New Method
	protected Party createCarrierAppointment(Party party, NbaOLifEId nbaOLifEId) {
		Producer producer = null;
		if (party != null && party.hasProducer()) {
			producer = party.getProducer();
		} else {
			producer = new Producer();
			producer.setActionAdd();
			party.setProducer(producer);
		}
		CarrierAppointment appt = new CarrierAppointment();
		appt.setActionAdd();
		nbaOLifEId.setId(appt);
		producer.addCarrierAppointment(appt);
		return party;
	}
}
