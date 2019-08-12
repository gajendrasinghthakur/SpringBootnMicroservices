package com.csc.fsg.nba.business.transaction;

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
 * 
 */

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.xml.sax.SAXException;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.contract.auditor.NbaAuditorFactory;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaParseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaAuditingContext;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaSessionUtils;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAuditorVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaReinsuranceRequestVO;
import com.csc.fsg.nba.vo.NbaReinsuranceResponseVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.PredictiveResult;
import com.csc.fsg.nba.vo.txlife.ReinsuranceCalcInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfoExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceOffer;
import com.csc.fsg.nba.vo.txlife.ReinsuranceRequest;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/** 
 * 
 * This class updates facultative reinsurance workitem  
 * and creates ACORD XML 552-Life Insurance Facultative transaction.
 * Updates the reinsurance response in database.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>SPR1699</td><td>Version 3</td><td>Reinsurance Issues</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>SPR2613</td><td>Version 5</td><td>Updates to Reinsurance Responses are not saved</td></tr>
 * <tr><td>SPR2806</td><td>Version 5</td><td>Reinsurance work item is getting matched even though the result is not receipted</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
 * <tr><td>AXAL3.7.10C</td><td>AXA Life Phase 2</td><td>Reinsurance Calculator</td></tr>
 * <tr><td>P2AXAL056</td><td>AXA Life Phase 2 Release2</td><td>Reinsurance</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaReinsuranceUtils {
	
	/**
	 * Updates the LOB values
	 * @param nbaTXLife Holding inquiry
	 * @param nbaTransaction the Transaction to which the Source object will be added.
	 * @param nbaDst the <code>NbaDst</code> instance that represents a case from AWD
	 * @param nbaReinsuranceRequestVO Request VO object to update transaction
	 * @throws NbaBaseException.
	 */
	public  void updateReinsuraceTransaction(NbaTXLife nbaTxLife,NbaTransaction nbaTransaction, NbaDst nbaDst,NbaReinsuranceRequestVO nbaReinsuranceRequestVO) throws NbaBaseException {
		NbaSource nbA552Source = create552Source(nbaTxLife, nbaDst, nbaReinsuranceRequestVO); //SPR2806
		nbaTransaction.addNbaSource(nbA552Source); //SPR2806
		NbaLob lob = nbaTransaction.getNbaLob();
		lob.setReinVendorID(nbaReinsuranceRequestVO.getCompany());
		lob.setProductTypSubtyp(nbaReinsuranceRequestVO.getPlan());
		
		//begin SPR2806
		NbaTXLife currentSourceTxLife;
		try {
			currentSourceTxLife = new NbaTXLife(nbA552Source.getText());
		} catch (SAXException sEx) {
			throw new NbaParseException(NbaBaseException.XML_SOURCE, sEx); 
		} catch (Throwable t) {
			throw new NbaNetServerDataNotFoundException(NbaBaseException.XML_SOURCE, t);
		}
		String participantPartyId = "";
		OLifE currentOLifE = currentSourceTxLife.getOLifE();
		Life currentLife = currentSourceTxLife.getLife();
		if (currentSourceTxLife.getLife().getCoverageCount() > 0) {
			Coverage currentCoverage = currentLife.getCoverageAt(0);
			for (int j = 0; j < currentCoverage.getLifeParticipantCount(); j++) {
				LifeParticipant currentLifeParticipant = currentCoverage.getLifeParticipantAt(j);
				participantPartyId = currentLifeParticipant.getPartyID();
				for (int k = 0; k < currentOLifE.getPartyCount(); k++) {
					Party currentParty = currentOLifE.getPartyAt(k);					
					if (currentParty.getId().equals(participantPartyId)) {
						lob.setSsnTin(currentParty.getGovtID());
						if (currentParty.hasPersonOrOrganization()) {
							PersonOrOrganization currentPersonOrOrganization = currentParty.getPersonOrOrganization();
							if (currentPersonOrOrganization.isPerson()) {
								Person currentPerson = currentPersonOrOrganization.getPerson();
								lob.setFirstName(currentPerson.getFirstName());
								lob.setMiddleInitial(currentPerson.getMiddleName());
								lob.setLastName(currentPerson.getLastName());
								lob.setGender(String.valueOf(currentPerson.getGender()));
								lob.setDOB(currentPerson.getBirthDate());
								//NBA208-32
								nbaTransaction.getTransaction().setLock("Y");
								return;
							}
						}
					}
				}
			}

		}
		//end SPR2806
		//NBA208-32
		nbaTransaction.getTransaction().setLock("Y");	
	}
	
	/**
	 * Creates ACORD XML 552-Life Insurance Facultative transaction.
	 * @return com.csc.fsg.nba.vo.NbaSource
	 * @param nbaTXLife Holding inquiry
	 * @param nbaDst the <code>NbaDst</code> instance that represents a case from AWD
	 * @param nbaReinsuranceRequestVO Request VO object to create source
	 * @throws NbaBaseException.
	 */
	public NbaSource create552Source(NbaTXLife nbaTxLife, NbaDst nbaDst, NbaReinsuranceRequestVO nbaReinsuranceRequestVO) throws NbaBaseException{
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_LIFEINSFACULTATIVE);
		if(nbaReinsuranceRequestVO.getRequestType().equals(NbaReinsuranceRequestVO.REIN_ORIGINAL_REQUEST)){
			nbaTXRequest.setTransSubType(NbaOliConstants.TC_TYPE_FACREQFORCAPSUBTYPE);
		}else if (nbaReinsuranceRequestVO.getRequestType().equals(NbaReinsuranceRequestVO.REIN_ADDITIONAL_INFO)){
			nbaTXRequest.setTransSubType(55214);
		}
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess("");
		nbaTXRequest.setNbaLob(nbaDst.getNbaLob());
	
		NbaTXLife txLife552 = new NbaTXLife(nbaTXRequest);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife552);
		
		//TXLife.TXLifeRequest.ReinsuranceRequest.ReinsuranceRequestID
		//TXLife.TXLifeRequest.ReinsuranceRequest.ReinsuranceEffDat
		ReinsuranceRequest reinsuranceRequest = new ReinsuranceRequest();
		reinsuranceRequest.setId("ReinsuranceRequest_1"); 
		
		Holding existingHolding = nbaTxLife.getPrimaryHolding();
		//existing holding
		Policy existingPolicy = existingHolding.getPolicy();
		//existing policy
	
		Holding holding = txLife552.getPrimaryHolding();
		Policy policy = holding.getPolicy();
	
		policy.setPolNumber(existingPolicy.getPolNumber());
		policy.setProductCode(existingPolicy.getProductCode());
		policy.setCarrierCode(existingPolicy.getCarrierCode());
		policy.setPolicyStatus(existingPolicy.getPolicyStatus());
		policy.setReinsuranceInd(existingPolicy.getReinsuranceInd());
	
			//Life Object
		if (nbaTxLife.isLife()) {
			// SPR3290 code deleted
			Life existingLife = existingPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth =
				new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			Life life = new Life(); //new Life object
			life.setId(existingLife.getId()); //ALPC7
	
			lifeOrAnnuityOrDisabilityHealth.setLife(life);
			policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeOrAnnuityOrDisabilityHealth);
	
			//Coverage Object
			Map reinsuredAmt = nbaReinsuranceRequestVO.getReinsuredAmt();//SPR1699
			for (int i = 0; i < existingLife.getCoverageCount(); i++) {
				Coverage existingCoverage = existingLife.getCoverageAt(i);
				if (nbaReinsuranceRequestVO.getCoverageIds().contains(existingCoverage.getId())) {//SPR1699
					Coverage coverage = new Coverage();
					coverage.setId(existingCoverage.getId());
					coverage.setCoverageKey(existingCoverage.getCoverageKey());
					coverage.setProductCode(existingCoverage.getProductCode());
					coverage.setCurrentAmt(existingCoverage.getCurrentAmt());
					coverage.setIndicatorCode(existingCoverage.getIndicatorCode());
					//LifeParticipant Object
					for (int j = 0; j < existingCoverage.getLifeParticipantCount(); j++) {
						LifeParticipant existingLifeParticipant = existingCoverage.getLifeParticipantAt(j);
						LifeParticipant lifeParticipant = new LifeParticipant();
						// SPR3290 code deleted
						//if (participantName==null || participantName.equals(nbaReinsuranceRequestVO.getName())) {
							// SPR3290 code deleted
							lifeParticipant.setId(existingLifeParticipant.getId());
							lifeParticipant.setParticipantName(existingLifeParticipant.getParticipantName());
							lifeParticipant.setLifeParticipantRoleCode(existingLifeParticipant.getLifeParticipantRoleCode());
							lifeParticipant.setIssueAge(existingLifeParticipant.getIssueAge());
							lifeParticipant.setIssueGender(existingLifeParticipant.getIssueGender());
							lifeParticipant.setPartyID(existingLifeParticipant.getPartyID());
							coverage.addLifeParticipant(lifeParticipant);
						//}
					}
					//SPR1699 code deleted
					//begin	SPR1699 
					double reinsuredAmount = ((Double)reinsuredAmt.get(existingCoverage.getId())).doubleValue();
					double currentAmount = existingCoverage.getCurrentAmt();
					ReinsuranceInfo reinsuranceInfo = new ReinsuranceInfo();
					reinsuranceInfo.setRetentionAmt(currentAmount-reinsuredAmount);
					reinsuranceInfo.setReinsuredAmt(reinsuredAmount);
					coverage.addReinsuranceInfo(reinsuranceInfo);
					//end SPR1699 
					life.addCoverage(coverage);
					//SPR1699 code deleted
				}
			}
	
			//Policy.ApplicationInfo object
			if (existingPolicy.hasApplicationInfo()) {
				ApplicationInfo existingApplicationInfo = existingPolicy.getApplicationInfo();
				ApplicationInfo applicationInfo = new ApplicationInfo();
				applicationInfo.setHOAssignedAppNumber(existingApplicationInfo.getHOAssignedAppNumber());
				policy.setApplicationInfo(applicationInfo);
			}
	
			Attachment attachment = new Attachment();
			nbaOLifEId.setId(attachment);	
			attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_COMMENT);
			attachment.setDescription(nbaReinsuranceRequestVO.getComments());
			holding.addAttachment(attachment);
	
			StringTokenizer st = new StringTokenizer(nbaReinsuranceRequestVO.getSource(),"##");
			while(st.hasMoreTokens()){
				String sourceId = st.nextToken();
				if(sourceId!=null && !sourceId.equals("")){
					attachment = new Attachment();
					nbaOLifEId.setId(attachment);
					attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_DOC);
					attachment.setDescription(sourceId);
					holding.addAttachment(attachment);
				}
			}
			OLifE existingOLifE = nbaTxLife.getOLifE();
			OLifE olife = txLife552.getOLifE();
			ArrayList partyList = new ArrayList();
			NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(nbaTxLife); // set up the NbaOinkDataAccess object 
			oinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			NbaOinkRequest oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
			oinkRequest.setVariable("Count_INS"); // get a count of the insureds
			int insurableRolesCount = Integer.parseInt(oinkDataAccess.getStringValueFor(oinkRequest));
			for (int x = 0; x < insurableRolesCount; x++) {
				oinkRequest = new NbaOinkRequest(); // set up the NbaOinkRequest object
				oinkRequest.setPartyFilter(x);
				oinkRequest.setVariable("RelationRoleCode_INS");
				long relationRoleCode = Long.parseLong(oinkDataAccess.getStringValueFor(oinkRequest)); // Person Code
				oinkRequest.setVariable("RelatedRefID_INS");
				long relatedRefID = Long.parseLong(oinkDataAccess.getStringValueFor(oinkRequest)); // Person Sequence	
				for (int i = 0; i < existingOLifE.getRelationCount(); i++) {
					Relation existingRelation = existingOLifE.getRelationAt(i);
					if (relationRoleCode==existingRelation.getRelationRoleCode()
							&& (existingRelation.getRelatedRefID()==null || (Long.parseLong(existingRelation.getRelatedRefID())==relatedRefID))) {
						Relation relation = new Relation();
						relation.setId(existingRelation.getId());
						relation.setOriginatingObjectID(existingRelation.getOriginatingObjectID());
						relation.setRelatedObjectID(existingRelation.getRelatedObjectID());
						partyList.add(existingRelation.getRelatedObjectID());
						relation.setRelationKey(existingRelation.getRelationKey());
						relation.setOriginatingObjectType(existingRelation.getOriginatingObjectType());
						relation.setRelatedObjectType(existingRelation.getRelatedObjectType());
						relation.setRelationRoleCode(existingRelation.getRelationRoleCode());
						relation.setRelatedRefID(existingRelation.getRelatedRefID());
						olife.addRelation(relation);
						break;
					}
				}
			}
				
			for (int i = 0; i < existingOLifE.getPartyCount(); i++) {
				Party existingParty = existingOLifE.getPartyAt(i);
				if (partyList.contains(existingParty.getId())) {
					Party party = new Party();
					party.setId(existingParty.getId());
					party.setPartyTypeCode(existingParty.getPartyTypeCode());
					party.setGovtID(existingParty.getGovtID());
					party.setResidenceState(existingParty.getResidenceState());
					party.setResidenceCountry(existingParty.getResidenceCountry()); //SPR1699
					ArrayList addressList = existingParty.getAddress();
					for (int j = 0; j < addressList.size(); j++) {
						Address existingAddress = (Address) addressList.get(j);
						Address address = new Address();
						address.setId(existingAddress.getId());
						address.setAddressTypeCode(existingAddress.getAddressTypeCode());
						address.setAttentionLine(existingAddress.getAttentionLine());
						address.setLine1(existingAddress.getLine1());
						address.setLine2(existingAddress.getLine2());
						address.setLine3(existingAddress.getLine3());
						address.setCity(existingAddress.getCity());
						address.setAddressStateTC(existingAddress.getAddressStateTC());
						address.setZip(existingAddress.getZip());
						address.setAddressCountry(existingAddress.getAddressCountry());
						address.setStartDate(existingAddress.getStartDate());
						party.addAddress(address);
	
					}
	
					ArrayList phoneList = existingParty.getPhone();
					for (int j = 0; j < phoneList.size(); j++) {
						Phone existingPhone = (Phone) phoneList.get(j);
						Phone phone = new Phone();
						phone.setId(existingPhone.getId());
						phone.setPhoneTypeCode(existingPhone.getPhoneTypeCode());
						phone.setDialNumber(existingPhone.getDialNumber());
						party.addPhone(phone);
					}
	
					if (existingParty.hasPersonOrOrganization()) {
						PersonOrOrganization existingPersonOrOrganization = existingParty.getPersonOrOrganization();
						PersonOrOrganization personOrOrganization = new PersonOrOrganization();
						if (existingPersonOrOrganization.isPerson()) {
							Person existingPerson = existingPersonOrOrganization.getPerson();
							Person person = new Person();
							person.setFirstName(existingPerson.getFirstName());
							person.setMiddleName(existingPerson.getMiddleName());
							person.setLastName(existingPerson.getLastName());
							person.setPrefix(existingPerson.getPrefix());
							person.setSuffix(existingPerson.getSuffix());
							person.setOccupation(existingPerson.getOccupation());
							person.setMarStat(existingPerson.getMarStat());
							person.setGender(existingPerson.getGender());
							person.setBirthDate(existingPerson.getBirthDate());
							person.setAge(existingPerson.getAge());
							person.setSmokerStat(existingPerson.getSmokerStat());
							//begin SPR1778
							PersonExtension existingPersonExtension = NbaUtils.getFirstPersonExtension(existingPerson);
							if (existingPersonExtension != null) {
								OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
								person.addOLifEExtension(olifeExt);
								PersonExtension personExtension = new PersonExtension();
								olifeExt.setPersonExtension(personExtension);
								personExtension.setRateClass(existingPersonExtension.getRateClass()); 
							}
							//end SPR1778
							person.setBirthJurisdictionTC(existingPerson.getBirthJurisdictionTC()); //SPR1699
							person.setBirthCountry(existingPerson.getBirthCountry()); //SPR1699
							personOrOrganization.setPerson(person);
						} else if (existingPersonOrOrganization.isOrganization()) {
							Organization existingOrganization = existingPersonOrOrganization.getOrganization();
							Organization organization = new Organization();
							organization.setDBA(existingOrganization.getDBA());
							organization.setOrgForm(existingOrganization.getOrgForm());
							personOrOrganization.setOrganization(organization);
						}
						party.setPersonOrOrganization(personOrOrganization);
					}
					olife.addParty(party);
				}
			}
			
			Party party = new Party();
			Organization organization = new Organization();
			organization.setDBA(nbaReinsuranceRequestVO.getCompany());
			PersonOrOrganization personOrOrganization = new PersonOrOrganization();
			personOrOrganization.setOrganization(organization);
			party.setPersonOrOrganization(personOrOrganization);
			nbaOLifEId.setId(party);
			olife.addParty(party);
	
			Relation relation = new Relation();
			relation.setOriginatingObjectID(holding.getId());
			relation.setRelatedObjectID(party.getId());
			relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
			relation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
			relation.setRelationRoleCode(NbaOliConstants.OLI_REL_COVERTOREINSURER); //NBA130
			nbaOLifEId.setId(relation);
			olife.addRelation(relation);
		}else{
			throw new NbaBaseException("Non Life reinsurance is not supported");
		}
		return new NbaSource(nbaDst.getBusinessArea(),NbaConstants.A_ST_REINSURANCE_XML_TRANSACTION,txLife552.toXmlString()); //SPR1699		
	}
	
	/**
	 * Updates the response and the Accept indicator in response.
	 * @param nbaTXLife Holding inquiry
	 * @param nbaDst the <code>NbaDst</code> instance that represents a case from a workflow system
	 * @param nbaReinsuranceRequestVO Request VO object to update response
	 * @throws NbaBaseException.
	 */
	public  void updateResponse(NbaTXLife nbaTxLife,NbaDst nbaDst,NbaReinsuranceRequestVO nbaReinsuranceRequestVO) throws NbaBaseException {

		if(nbaTxLife.isLife()){
			//NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
			NbaReinsuranceResponseVO responseVO = nbaReinsuranceRequestVO.getResponseVO();
			ReinsuranceOffer reinsuranceOffer = responseVO.getReinsuranceOffer();
			//nbaOLifEId.setId(reinsuranceOffer);
			ArrayList coverageList = nbaTxLife.getLife().getCoverage();
			for(int l = 0; l < coverageList.size();l++){
				NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
				Coverage coverage = (Coverage)coverageList.get(l);
				if(nbaReinsuranceRequestVO.getCoverageIds().contains(coverage.getId())){
					ArrayList oLifEExtList = coverage.getOLifEExtension();
					OLifEExtension oLifEExtension = null;
					CoverageExtension coverageExtension = null;
					for(int m = 0; m < oLifEExtList.size(); m++){
						oLifEExtension = (OLifEExtension)oLifEExtList.get(m);
						if(oLifEExtension!=null){
							break;
						}
					}
					if(oLifEExtension==null){
						oLifEExtension = new OLifEExtension();
						coverage.addOLifEExtension(oLifEExtension);
					}
					coverageExtension = oLifEExtension.getCoverageExtension();
					if(coverageExtension==null){
						coverageExtension = new CoverageExtension();
						oLifEExtension.setCoverageExtension(coverageExtension);
					}
					// SPR2613 code deleted
					boolean offerExist = false;
					if(coverageExtension.getReinsuranceOfferCount() > 0){	//SPR2613
						ArrayList attachmentList = reinsuranceOffer.getAttachment();
						String transactionId = null;
						for(int p = 0; p < attachmentList.size(); p++){
							Attachment attachment = (Attachment)attachmentList.get(p);
							if(attachment!=null && attachment.getAttachmentType()==NbaOliConstants.OLI_ATTACH_DOC){
								transactionId = attachment.getAttachmentData().getPCDATA();
							}
						}
						ReinsuranceOffer existingReinsuranceOffer = null;
						int reinIdx;	//SPR2613
						for (reinIdx = 0; reinIdx < coverageExtension.getReinsuranceOfferCount(); reinIdx++) {	//SPR2613
							existingReinsuranceOffer = coverageExtension.getReinsuranceOfferAt(reinIdx);	//SPR2613
							ArrayList existingAttachmentList = existingReinsuranceOffer.getAttachment();
							for(int p = 0; p < existingAttachmentList.size(); p++){
								Attachment attachment = (Attachment)existingAttachmentList.get(p);
								if(attachment!=null && attachment.getAttachmentData().getPCDATA().equals(transactionId)){
									offerExist = true;
									break;
								}
							}
							if(offerExist){
								break;
							}
						}
						//begin SPR2613
						if (offerExist) { //Replace existing with input values
							//copy key fields
							reinsuranceOffer.setId(existingReinsuranceOffer.getId());
							reinsuranceOffer.setBackendKey(existingReinsuranceOffer.getBackendKey());
							reinsuranceOffer.setCompanyKey(existingReinsuranceOffer.getCompanyKey());
							reinsuranceOffer.setContractKey(existingReinsuranceOffer.getContractKey());
							reinsuranceOffer.setParentIdKey(existingReinsuranceOffer.getParentIdKey());
							reinsuranceOffer.setAttachment(existingReinsuranceOffer.getAttachment());
							if (existingReinsuranceOffer.getAcceptInd()) {
								reinsuranceOffer.setAcceptInd(true);
							}
							reinsuranceOffer.setActionUpdate();
							coverageExtension.setReinsuranceOfferAt(reinsuranceOffer, reinIdx);
						}
						//end SPR2613
					}
					if(!offerExist){	//SPR2613
						reinsuranceOffer.setActionAdd();
						ReinsuranceOffer newReinsuranceOffer = reinsuranceOffer.clone(false);
						newReinsuranceOffer.setActionAdd();
						nbaOLifEId.setId(newReinsuranceOffer);
						ArrayList attachmentList = newReinsuranceOffer.getAttachment();
						for(int i = 0; i < attachmentList.size();i++){
							Attachment attachment = (Attachment)attachmentList.get(i);
							attachment.setActionAdd();
							nbaOLifEId.setId(attachment);	
						}
						coverageExtension.addReinsuranceOffer(newReinsuranceOffer);
					}
				}
				//break;
			}						
		}
	}
	
	//AXAL3.7.10C New Method
	//P2AXAL056 Refactored for Joint Insured
	public static Map deoinkReinsuranceCalcAttributes(NbaTXLife nbaTxLife) throws NbaBaseException {
		List partyList = nbaTxLife.getOLifE().getParty();
		Map deoinkMap = new HashMap();
		for (int i = 0; i < partyList.size(); i++) {
			Party party = (Party) partyList.get(i);
			String str = "";
			if (nbaTxLife.isPrimaryInsured(party.getId())) {//sets the suffix relative to insured
				str = "_PINS";
			} else if (nbaTxLife.isJointInsured(party.getId())) {
				str = "_JNT";
			}
			if (nbaTxLife.isPrimaryInsured(party.getId()) || nbaTxLife.isJointInsured(party.getId())) {
				Map additionalConsiderations = getAdditionalConsiderationsForReinCalc(nbaTxLife, party.getId());
				deoinkMap.put(NbaVpmsConstants.A_RESIDENCE + str, additionalConsiderations.get(NbaConstants.RESIDENCE));
				deoinkMap.put(NbaVpmsConstants.A_HAZARD + str, additionalConsiderations.get(NbaConstants.HAZARD));
				deoinkMap.put(NbaVpmsConstants.A_FOREIGNTRAVEL + str, additionalConsiderations.get(NbaConstants.FOREIGN_TRAVEL));
				deoinkMap.put(NbaVpmsConstants.A_MILITARYACTIVEDUTYINDCODE + str, additionalConsiderations.get(NbaConstants.MILITARY_IND));
				deoinkMap.put(NbaVpmsConstants.A_CALCUWCODE + str, additionalConsiderations.get(NbaConstants.UNDERWRITING_TYPE));
				deoinkMap.put(NbaVpmsConstants.A_CALCPRODUCTTYPE, additionalConsiderations.get(NbaConstants.PRODUCT_TYPE));
				deoinkMap.put(NbaVpmsConstants.A_CALCSHOPPINGIND + str, additionalConsiderations.get(NbaConstants.FACULTATIVE_SHOPIND));
				long equivalentRating = getEquivalentRating(nbaTxLife, party);//ALII231
				deoinkMap.put(NbaVpmsConstants.A_CALCTABLERATING + str, String.valueOf(equivalentRating));
				deoinkMap.put("A_EquivalentRating" + str, String.valueOf(equivalentRating));//ALII231
				deoinkMap.put(NbaVpmsConstants.A_TYPEREPLACEMENT, String.valueOf(getCalcReplacementType(nbaTxLife)));
				deoinkMap.put("A_LivesCovered", String.valueOf(nbaTxLife.getPrimaryCoverage().getLivesType()));
			}
		}
		return deoinkMap;
	}
	
	//New method APSL2806
	public static Map deoinkReinsuranceCalcAttributesFromDB(NbaTXLife nbaTxLife) throws NbaBaseException {
		List partyList = nbaTxLife.getOLifE().getParty();
		Map deoinkMap = new HashMap();
		ReinsuranceInfo reinsuranceInfo = nbaTxLife.getDefaultReinsuranceInfo();
		ReinsuranceCalcInfo reinsuranceCalcInfo = null;
		String str = "";
		String partyId = null;
		ReinsuranceInfoExtension reinsuranceInfoExtension = null;
		if (!NbaUtils.isBlankOrNull(reinsuranceInfo)) {
			reinsuranceInfoExtension = NbaUtils.getFirstReinsuranceInfoExtension(reinsuranceInfo);
			if (!NbaUtils.isBlankOrNull(reinsuranceInfoExtension)) {
				for (int i = 0; i < reinsuranceInfoExtension.getReinsuranceCalcInfoCount(); i++) {
					reinsuranceCalcInfo = reinsuranceInfoExtension.getReinsuranceCalcInfoAt(i);
					if (!NbaUtils.isBlankOrNull(reinsuranceCalcInfo)) {
						partyId = reinsuranceCalcInfo.getAppliesToPartyID();
						if (NbaUtils.isBlankOrNull(partyId) || nbaTxLife.isPrimaryInsured(partyId)) {
							str = "_PINS";
						} else if (nbaTxLife.isJointInsured(partyId)) {
							str = "_JNT";
						}
						if (nbaTxLife.isPrimaryInsured(partyId) || nbaTxLife.isJointInsured(partyId)) {
							if (!NbaUtils.isBlankOrNull(reinsuranceCalcInfo)) {
								deoinkMap.put(NbaVpmsConstants.A_RESIDENCE + str, String.valueOf(reinsuranceCalcInfo.getResidence()));
								deoinkMap.put(NbaVpmsConstants.A_HAZARD + str, String.valueOf(reinsuranceCalcInfo.getHazard()));
								deoinkMap.put(NbaVpmsConstants.A_FOREIGNTRAVEL + str, String.valueOf(reinsuranceCalcInfo.getForeignTravelInd() ? 1
										: 0));
								deoinkMap.put(NbaVpmsConstants.A_MILITARYACTIVEDUTYINDCODE + str, String
										.valueOf(reinsuranceCalcInfo.getMilitaryInd() ? 1 : 0));
								deoinkMap.put(NbaVpmsConstants.A_CALCUWCODE + str, String.valueOf(reinsuranceCalcInfo.getCalcUWCode()));
								deoinkMap.put(NbaVpmsConstants.A_CALCPRODUCTTYPE, String.valueOf(reinsuranceCalcInfo.getReinProductType()));
								deoinkMap.put(NbaVpmsConstants.A_CALCSHOPPINGIND + str, String.valueOf(reinsuranceCalcInfo.getCalcShoppingInd() ? 1
										: 0));
							}
							long equivalentRating = getEquivalentRating(nbaTxLife, nbaTxLife.getPartyFromId(partyId, partyList));
							deoinkMap.put(NbaVpmsConstants.A_CALCTABLERATING + str, String.valueOf(equivalentRating));
							deoinkMap.put("A_EquivalentRating" + str, String.valueOf(equivalentRating));//ALII231
							deoinkMap.put(NbaVpmsConstants.A_TYPEREPLACEMENT, String.valueOf(getCalcReplacementType(nbaTxLife)));
							deoinkMap.put("A_LivesCovered", String.valueOf(nbaTxLife.getPrimaryCoverage().getLivesType()));
						}
					}
				}
			}
		}
		return deoinkMap;
	}
	
 
	
	//AXAL3.7.10C New Method
	//P2AXAL056 Signature changed
	public static Map getAdditionalConsiderationsForReinCalc(NbaTXLife aNbaTXLife, String partyid) throws NbaBaseException {
		Map map = new HashMap();
		VpmsModelResult vpmsModelResult = new VpmsModelResult();
		String str = (aNbaTXLife.isJointInsured(partyid) ? "_JNT" : "");//P2AXAL056
		VpmsComputeResult vpmsComputeResult = NbaReinsuranceUtils.getDataFromVpms(aNbaTXLife, NbaVpmsConstants.REINSURANCE,
				NbaVpmsAdaptor.EP_ADDITIONAL_CONSIDERATIONS + str, null, null);//P2AXAL056
		NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsComputeResult);
		if (vpmsResultsData.wasSuccessful()) {
			String xmlString = (String) vpmsResultsData.getResultsData().get(0);
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
			vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
			ArrayList strAttrs = vpmsModelResult.getStandardAttr();
			StandardAttr standardAttr = null;
			for (int i = 0; i < strAttrs.size(); i++) {
				standardAttr = (StandardAttr) strAttrs.get(i);
				map.put(standardAttr.getAttrName(), standardAttr.getAttrValue());
			}
		}
		return map;
	}
	
	//AXAL3.7.10C New Method
	public static VpmsComputeResult getDataFromVpms(NbaTXLife nbaTXLife, String model, String entryPoint, Map deOink, String execptText)
			throws NbaBaseException, NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTXLife);
			vpmsProxy = new NbaVpmsAdaptor(oinkData, model);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			if(deOink != null) {
				vpmsProxy.setSkipAttributesMap(deOink);	
			}
			return vpmsProxy.getResults();
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException(execptText + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				throw new NbaVpmsException(NbaBaseException.VPMS_REMOVAL_FAILED + re);
			}
		}
	}
	
	//AXAL3.7.10C New Method
	//P2AXAL056 Refactored for joint insured
	public static long getEquivalentRating(NbaTXLife nbaTXLife, Party party) throws NbaVpmsException, NbaBaseException {
		if (nbaTXLife != null) {
			Map deoinkMap = new HashMap();
			int permFlatExtraCount = 0;
			int tempFlatExtraCount = 0;
			List coverages = nbaTXLife.getLife().getCoverage();
			String str = "";
			String partyId = party.getId();
			//Sets the qualifier for the party
			if (nbaTXLife.isPrimaryInsured(partyId)) {
				str = "_PINS";
			} else if (nbaTXLife.isJointInsured(partyId)) {
				str = "_JNT";
			}
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				LifeParticipant lifeParticipant = NbaUtils.getLifeParticipantFor(coverage, partyId);
				if (coverage.hasLivesType()) {
					deoinkMap.put("A_LivesCovered", String.valueOf(coverage.getLivesType()));
				}
				if (lifeParticipant != null) {
					for (int k = 0; k < lifeParticipant.getSubstandardRatingCount(); k++) {
						SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(k);
						SubstandardRatingExtension substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
						if (substandardRatingExt != null && !substandardRatingExt.getProposedInd() && !substandardRating.isActionDelete()) {
							if (substandardRating.hasPermTableRating()) {
								deoinkMap.put("A_TableRating" + str, String.valueOf(substandardRating.getPermTableRating()));
							}
							if (substandardRatingExt.hasPermFlatExtraAmt()) {
								if (permFlatExtraCount == 0) {
									deoinkMap.put("A_PermFlatExtraAmt" + str, String.valueOf(substandardRatingExt.getPermFlatExtraAmt()));
								} else {
									deoinkMap.put("A_PermFlatExtraAmt" + str + "[" + permFlatExtraCount + "]", String.valueOf(substandardRatingExt.getPermFlatExtraAmt()));
								}
								permFlatExtraCount++;
							}
							if (substandardRating.hasTempFlatExtraAmt()) {
								if (tempFlatExtraCount == 0) {
									deoinkMap.put("A_TempFlatExtraAmt" + str, String.valueOf(substandardRating.getTempFlatExtraAmt()));
									deoinkMap.put("A_TempFlatExtraAmtDuration" + str, String.valueOf(substandardRatingExt.getDuration()));
								} else {
									deoinkMap.put("A_TempFlatExtraAmt" + str + "[" + tempFlatExtraCount + "]", String.valueOf(substandardRating.getTempFlatExtraAmt()));
									deoinkMap.put("A_TempFlatExtraAmtDuration" + str + "[" + tempFlatExtraCount + "]", String.valueOf(substandardRatingExt.getDuration()));
								}
								tempFlatExtraCount++;
							}
						}
					}
				}
			}
			deoinkMap.put("A_NoofPermFlatExtra" + str, String.valueOf(permFlatExtraCount));
			deoinkMap.put("A_NoofTempFlatExtra" + str, String.valueOf(tempFlatExtraCount));
			VpmsComputeResult compResult = getDataFromVpms(nbaTXLife, NbaVpmsConstants.REINSURANCE, NbaVpmsAdaptor.EP_GET_EQUIVALENT_RATING + str,
					deoinkMap, null);
			return Long.parseLong(compResult.getResult());
		}
		return -1;
	}
	
	//AXAL3.7.10C New Method
	public static boolean isRunAutoCalculator(NbaTXLife nbaTXLife) throws NbaBaseException {
		VpmsComputeResult compResult = NbaReinsuranceUtils.getDataFromVpms(nbaTXLife, NbaVpmsConstants.REINSURANCE,
				NbaVpmsAdaptor.EP_GET_AUTORUN_CALCULATOR, null, null);
		return Integer.parseInt(compResult.getResult()) == 1 ? true : false;
	}
	
	//AXAL3.7.10C New Method
	public static boolean isAdditionalConsiderationPresent(NbaTXLife nbaTXLife) {
		try {
			Map deoinkMap = deoinkReinsuranceCalcAttributesFromDB(nbaTXLife);//APSL2806 method call changed
			VpmsComputeResult compResult = NbaReinsuranceUtils.getDataFromVpms(nbaTXLife, NbaVpmsConstants.REINSURANCE,
					NbaVpmsAdaptor.EP_Is_ADDITIONAL_CONSIDERATION_PRESENT, deoinkMap, null);
			return compResult.getResult().equals("1")?true:false;//P2AXAL056, ALII1352
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	//AXAL3.7.10C New Method
	public static void commitReinsuranceInfo(NbaTXLife nbaTXLife) throws SQLException {
		Connection pendConn = null;
		Connection auditConn = null;
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext extContext = context.getExternalContext();
		try {
			NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTXLife);
			Coverage coverage = nbaTXLife.getPrimaryCoverage();
			ReinsuranceInfo reinsuranceInfo = nbaTXLife.getDefaultReinsuranceInfo();
			if (coverage != null && reinsuranceInfo != null) {
				nbaOLifEId.assureId(nbaTXLife);
				pendConn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
				pendConn.setAutoCommit(false);
//				reinsuranceInfo.processChanges(coverage.getKeysForChildren(coverage.getId()), coverage.getActionIndicator(), pendConn);
				NbaAuditingContext.addAuditingContext(nbaTXLife, NbaSessionUtils.getUser((HttpSession)extContext.getSession(false))); //NBA102
				reinsuranceInfo.processChanges(coverage.getKeysForChildren(coverage.getId()), coverage.getActionIndicator(), pendConn);
				if (!NbaConfigurationConstants.NO_LOGGING.equals((NbaConfiguration.getInstance().getAuditConfiguration().getAuditLevel()))) {
	                NbaAuditorVO auditorVO = NbaAuditingContext.getTxnAuditObjectsFromStack(nbaTXLife);//NBA102 NBA208-18
	                auditConn = NbaConnectionManager.borrowConnection(NbaConfiguration.AUDITING_CONTRACT);
	                auditConn.setAutoCommit(false);
	                NbaAuditorFactory.getInstance().getAuditor().audit(auditorVO, auditConn);
	                auditConn.commit();
				} 
				pendConn.commit();
				reinsuranceInfo.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, null);
			}
		} catch (Exception e) {
			if(pendConn != null) {
				pendConn.rollback();	
			}
		} finally {
			if(pendConn != null) {
				pendConn.close();	
			}
		}
	}
	
	//AXAL3.7.10C New Method
	public static long getCalcReplacementType(NbaTXLife nbaTXLife) {
		Policy policy = nbaTXLife.getPolicy();
		if(NbaOliConstants.OLI_REPTY_EXTERNAL == policy.getReplacementType() && nbaTXLife.is1035Exchange()){
			return NbaOliConstants.OLI_REPTY_EXTERNAL;
		}else if(NbaOliConstants.OLI_REPTY_INTERNAL == policy.getReplacementType()) {
			return NbaOliConstants.OLI_REPTY_INTERNAL;
		}else {
			return NbaOliConstants.OLI_REPTY_NONE;
		}
	}
	
	/**
	 * Phase 2 
	 * nbA will invoke the nbA TAI interface when a policy is approved by the Underwriter to send a TAI automatic reinsurance record in the
	 * following scenarios: 1. Special Case - See AXAL3.7.10.13. � nbA will have the Special Case indicator for the EIB to set the Reinsurance Type to
	 * �A� for Automatic and the ceded amount to �zero�. 2. Facultatively Shopped and No Reinsurer Offer Accepted and Case is Fully Retained (Face
	 * Amount = Retained Amount). a. Set the Ceded Override Amount to zero. b. Set the Reason for Reinsurance to �Fully Retained�. 3. Not
	 * Facultatively Shopped and Case is Not an Internal Term Replacement. 4. Not Facultatively Shopped and Case is an Internal Term Replacement. a.
	 * Set the Ceded Override Amount to Zero b. Set the Reason for Reinsurance to Blank c. Populate the Original Policy Number fields in the TAI
	 * Records with all the source replacement policies. 
	 * 
	 * P2AXAL043
	 * nbA shall send an automatic reinsurance TAI record to TAI with the ceded override amount set
	 * to zero with a reason for the override in the following situations (reasons for no automatic reinsurance) :
	 * b) Juvenile Insurance (ages 0-14).
	 * c) If the policy falls under the IUP International Underwriting Program
	 * (Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension. InternationalUWProgInd)
	 * 
	 * @param nbaTXLife
	 */
	//New Method AXAL3.7.10B
	public static void updateReinsuranceInfoForTAI(NbaTXLife nbaTXLife,boolean jumboLimitInd) {
		boolean isFacReinsAccpt = AxaUtils.isFacultativelyShopped(nbaTXLife) && AxaUtils.isReinsuranceOfferAccepted(nbaTXLife);
		boolean isInternalReplacement = nbaTXLife.getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_INTERNAL;
		long appType = nbaTXLife.getPolicy().getApplicationInfo().getApplicationType();
		boolean isAdditionalConsiderations = isAdditionalConsiderationPresent(nbaTXLife);
		ReinsuranceInfo reinInfo = nbaTXLife.getDefaultReinsuranceInfo();
		if (reinInfo == null) {
			reinInfo = new ReinsuranceInfo();
			reinInfo.setActionAdd();
			nbaTXLife.getPrimaryCoverage().addReinsuranceInfo(reinInfo);
			nbaTXLife.getPrimaryCoverage().setActionUpdate();
		}
		ReinsuranceInfoExtension reinInfoExt = NbaUtils.getFirstReinsuranceInfoExtension(reinInfo);
		if (reinInfoExt == null) {
			OLifEExtension newExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REINSURANCEINFO);
			reinInfoExt = newExtension.getReinsuranceInfoExtension();
			reinInfo.addOLifEExtension(newExtension);
			reinInfoExt.setActionAdd();
		}
		//Begin Refactored in P2AXAL056
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
		if (polExt!=null && polExt.getPredictiveInd()) { //ALII1679,ALII1902
			List predictiveResultList = polExt.getPredictiveResult();
			PredictiveResult lastResult = (PredictiveResult) predictiveResultList.get(predictiveResultList.size() - 1);
			lastResult.setUWDecision(NbaOliConstants.OLIX_PAUWDECISON_AGREE);
			lastResult.setActionUpdate();
			reinInfo.setReinsuranceRiskBasis(NbaOliConstants.OLI_REINRISKBASE_AU);
			reinInfoExt.setRetentionReason("Predictive/Jet Approved");
		}
		
		int reinCalcInfosize = (nbaTXLife.getPrimaryCoverage().getLivesType() == NbaOliConstants.OLI_COVLIVES_JOINTLTD ? 2 : 1);
		for (int i = 0; i < reinCalcInfosize; i++) {
			ReinsuranceCalcInfo reinCalcInfo = null;
			if (reinInfoExt.getReinsuranceCalcInfoCount() > i) {
				reinCalcInfo = reinInfoExt.getReinsuranceCalcInfoAt(i);
			}
			if (reinCalcInfo == null) {
				reinCalcInfo = new ReinsuranceCalcInfo();
				reinCalcInfo.setActionAdd();
				reinInfoExt.addReinsuranceCalcInfo(reinCalcInfo);
			} else {
				reinCalcInfo.setActionUpdate();
			}
			String partyId = reinCalcInfo.getAppliesToPartyID();
			if (partyId == null) {
				partyId = (i == 0 ? nbaTXLife.getPartyId(NbaOliConstants.OLI_REL_INSURED) : nbaTXLife
						.getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED));
			}

			
			LifeParticipant lifeParticipant = NbaUtils.getLifeParticipantFor(nbaTXLife.getPrimaryCoverage(), partyId);
			if (lifeParticipant != null) {
				double totalAppliedAmount = getTotalAppliedAmount(nbaTXLife);//APSL3285/QC#12191
				int issueAge = lifeParticipant.getIssueAge();
				long permTableRating = getInsuredPermTableRating(lifeParticipant);
				boolean isGoodHealth = reinCalcInfo.getCalcUWCode() == NbaOliConstants.AXA_ReinCalcUWCode_GoodHealth;//P2AXAL056, ALNA549
				boolean isASULIV = NbaConstants.Product_Code_ASUL4.equalsIgnoreCase(nbaTXLife.getPolicy().getProductCode()); //APSL2520 ASUL4
				//For condition1, all the values are already set as per the mapping ss. So no need to update anything here.
				//NBLXA-1651 start-- Reverted APSL5206
				boolean isManulReinsuranceType = false;
				boolean isFacOfferAllReject = AxaUtils.isFacultativelyShopped(nbaTXLife) && NbaUtils.isAllReinsuranceOfferRejected(nbaTXLife);//NBLXA-2520
				if (!(polExt != null && polExt.getPredictiveInd())) {
					List activityList = NbaUtils.getActivityByTypeCode(nbaTXLife.getOLifE().getActivity(), NbaOliConstants.OLI_ACTTYPE_1009900004);
					if (reinInfo.hasReinsuranceRiskBasis() && (activityList.size() == 0)) {
						if (isFacOfferAllReject) {//NBLXA-2520
							addReinsuraceActivity(nbaTXLife, "SYS");
						} else {
							addReinsuraceActivity(nbaTXLife, "MANL");
						}
					} else if (activityList.size() > 0) {
						isManulReinsuranceType = true;
					}
				}
				if(isFacOfferAllReject) {//NBLXA-2520
					reinInfo.setReinsuranceRiskBasis(NbaOliConstants.OLI_REINRISKBASE_AU);
					deleteFacultativeReinsuraceInfo(nbaTXLife);
				}
				//End NBLXA-1651
				if ((!isFacReinsAccpt && !reinInfo.hasReinsuranceRiskBasis()) || isFacOfferAllReject) {//ALII1269,NBLXA-2520-add check for facultative all rejected
					if (reinInfoExt.getOverrideRetentionLimitsInd() || AxaUtils.isSpecialCase(nbaTXLife) || isGoodHealth
							|| ((AxaUtils.isTermConversion(appType) || AxaUtils.isOPAI(appType)) && !reinCalcInfo.hasAutomaticCapacityAvailable())
							|| (issueAge < 15) || NbaUtils.isIUPCase(nbaTXLife) || AxaUtils.isGuaranteedIssue(appType)
							||  isASULIV || (jumboLimitInd && totalAppliedAmount<=reinCalcInfo.getTotalAmtAvailableForIssuance())) { //P2AXAL069, APSL2520 ASUL4,APSL3285/QC#12191,APSL3491, APSL4095 removed internal replacement condition
						if (!reinInfo.hasReinsuranceRiskBasis() && ! isManulReinsuranceType) {
							reinInfo.setReinsuranceRiskBasis(NbaOliConstants.OLI_REINRISKBASE_AU);
						}
						reinInfoExt.setOverrideAutoCededAmt(0);
					} else if (isAdditionalConsiderations && ! isManulReinsuranceType) {
						reinInfoExt.setOverrideAutoCededAmt(reinCalcInfo.getAutomaticCapacityAvailable());
						if (!reinInfo.hasReinsuranceRiskBasis()) {
							reinInfo.setReinsuranceRiskBasis(NbaOliConstants.OLI_REINRISKBASE_AU);
						}
					} else {
						reinInfoExt.deleteOverrideAutoCededAmt();
						reinCalcInfo.deleteAutoRedRetReason();
						if (!reinInfo.hasReinsuranceRiskBasis() && ! isManulReinsuranceType) {
							reinInfo.setReinsuranceRiskBasis(NbaOliConstants.OLI_REINRISKBASE_NONE);
						}
					}
					if ((isInternalReplacement || (AxaUtils.isTermConversion(appType) || AxaUtils.isOPAI(appType)))
							&& !(NbaOliConstants.OLI_REINRISKBASE_AU == reinInfo.getReinsuranceRiskBasis())){		//APSL4745
						reinCalcInfo.deleteAutoRedRetReason();
					} else if (isGoodHealth) {
						reinCalcInfo.setAutoRedRetReason("Credit Program");		//APSL4754 
					} else if (reinInfoExt.getOverrideRetentionLimitsInd()) {
						reinCalcInfo.setAutoRedRetReason("Ceded Override Approved by" + reinInfoExt.getOverrideRetentionUWName());
					} else if (AxaUtils.isSpecialCase(nbaTXLife)) {
						ApplicationInfo appInfo = nbaTXLife.getPolicy().getApplicationInfo();
						ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
						String reinsuranceReason = NbaTransOliCode.lookupText(NbaOliConstants.NBA_SPECIALCASE, appInfoExt.getSpecialCase());
						reinCalcInfo.setAutoRedRetReason(reinsuranceReason);
					} else if (AxaUtils.isGuaranteedIssue(appType)) { //P2AXAL069
						reinCalcInfo.setAutoRedRetReason("Guaranteed Issue");//P2AXAL069
					}else if (isASULIV){//APSL2520 ASUL4
						reinCalcInfo.setAutoRedRetReason("ASUL IV");
					}

				} else if (reinInfo.hasReinsuranceRiskBasis() && reinInfo.getReinsuranceRiskBasis() == NbaOliConstants.OLI_REINRISKBASE_NONE) {//Begin ALII1269
					reinCalcInfo.deleteAutoRedRetReason();
				}//End ALII1269
				if ("0".equals(reinCalcInfo.getAutoRedRetReason())) {//ALII756
					reinCalcInfo.deleteAutoRedRetReason();//ALII756
				}
				if (reinInfoExt.getRetentionReason() != null) {//ALII1269
					reinCalcInfo.setAutoRedRetReason(reinInfoExt.getRetentionReason());
				}
				reinCalcInfo.setAppliesToPartyID(partyId);
				reinCalcInfo.setActionUpdate();
			}//End Refactored in P2AXAL056
		}
		reinInfo.setActionUpdate();
		reinInfoExt.setActionUpdate();
	}
	
	/**
	 * @param lifeParticipant
	 * @return
	 */
	//New Method AXAL3.7.10B
	//P2AXAL056 Method signature changed
	private static long getInsuredPermTableRating(LifeParticipant lifeParticipant) {
		if (lifeParticipant != null) {//P2AXAL056
			List substandardRatings = lifeParticipant.getSubstandardRating();
			for (int i = 0; i < substandardRatings.size(); i++) {
				SubstandardRating substandardRating = (SubstandardRating) substandardRatings.get(i);
				if (NbaUtils.isValidRating(substandardRating) && substandardRating.hasPermTableRating()) {
					return substandardRating.getPermTableRating();
				}
			}
		}
		return -1;
	}
	
	/**
	 * @param nbaTXLife
	 * @return double
	 * This method is used to calculate total face amount = faceamt+CLR+ROPR+EPR
	 */
	//New Method APSL3285/QC#12191
	public static double getTotalAppliedAmount(NbaTXLife nbaTXLife) {
		List coverages = nbaTXLife.getLife().getCoverage();
		double totalFaceAmt = 0.0;
		totalFaceAmt += nbaTXLife.getFaceAmount();
		for (Iterator iter = coverages.iterator(); iter.hasNext();) {
			Coverage coverage = (Coverage) iter.next();
			List covOptions = coverage.getCovOption();
			if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
				for (Iterator covIter = covOptions.iterator(); covIter.hasNext();) {
					CovOption covOption = (CovOption) covIter.next();
					if (covOption != null && covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_ROPR) {
						totalFaceAmt += covOption.getMaxBenefitAmt();
					}
				}
			} else if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_RIDER
					&& (NbaOliConstants.OLI_COVTYPE_CLR == coverage.getLifeCovTypeCode() || NbaOliConstants.OLI_COVTYPE_ESTATEPROT == coverage
							.getLifeCovTypeCode()) && coverage.hasCurrentAmt()) {
				totalFaceAmt += coverage.getCurrentAmt();
			}
		}
		return totalFaceAmt;
	}
	
	//Start NBLXA-1651 new method
	public static void addReinsuraceActivity(NbaTXLife nbaTXLife, String userRole) {
		Activity newActivity = new Activity();
		NbaOLifEId olifeId = new NbaOLifEId(nbaTXLife);
		olifeId.setId(newActivity);
		newActivity.setDoneDate(new Date());
		newActivity.setStartTime(new NbaTime(new java.sql.Timestamp(System.currentTimeMillis())));
		newActivity.setUserCode(userRole);
		newActivity.setActivityStatus(NbaOliConstants.OLI_ACTSTAT_COMPLETE);
		newActivity.setActivityTypeCode(NbaOliConstants.OLI_ACTTYPE_1009900004);
		newActivity.setActionAdd();		
		nbaTXLife.getOLifE().setActionUpdate();
		nbaTXLife.getOLifE().addActivity(newActivity);

	}
	
	//NBLXA-2520 delete all reinsuranceInfo if reinsurance type is not facultative
	protected static void  deleteFacultativeReinsuraceInfo(NbaTXLife nbaTXLife) {
		Life life = nbaTXLife.getLife();
		if (life != null) {
			List coverages = life.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				List reinInfoList = coverage.getReinsuranceInfo();
				ReinsuranceInfo reinInfo = null;
				for (int k = 0; k < reinInfoList.size(); k++) {
					reinInfo = (ReinsuranceInfo) reinInfoList.get(k);
					if (reinInfo.hasCarrierPartyID()) {
						reinInfo.setActionDelete();
					}
				}
			}
		}
}

}
