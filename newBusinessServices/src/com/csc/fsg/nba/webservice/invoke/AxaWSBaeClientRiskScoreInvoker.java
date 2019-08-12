/*
 * ******************************************************************************* <BR> This program contains trade secrets and confidential
 * information which <BR> are proprietary to CSC Financial Services Group�. The use, <BR> reproduction, distribution or disclosure of this program, in
 * whole or in <BR> part, without the express written permission of CSC Financial Services <BR> Group is prohibited. This program is also an
 * unpublished work protected <BR> under the copyright laws of the United States of America and other <BR> countries. If this program becomes
 * published, the following notice shall <BR> apply: Property of Computer Sciences Corporation. <BR> Confidential. Not for publication. <BR> Copyright
 * (c) 2002-2013 Computer Sciences Corporation. All Rights Reserved. <BR>
 * 
 * ******************************************************************************* <BR>
 */
package com.csc.fsg.nba.webservice.invoke;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;

import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * This class is responsible for creating request for searchCustomer webservice .
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSBaeClientRiskScoreInvoker extends AxaWSInvokerBase {

	private static final String CATEGORY = "CIP";

	private static final String FUNCTIONID = "retrieveClientRiskScore";

	/**
	 * @param operation
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 * @param object
	 */
	public AxaWSBaeClientRiskScoreInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	/**
	 * Create webservice request for searchCustomer
	 * 
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {

		NbaUserVO user = getUserVO();
		Party party = (Party) getObject();
		Policy policy = getNbaTXLife().getPolicy();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PARTYINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_TYPE_UWRSKSUBTYPE);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setNbaUser(user);
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		Party requestedParty = new Party();
		PersonOrOrganization personOrOrganization = party.getPersonOrOrganization();
		PersonOrOrganization newPersonOrOrganization = new PersonOrOrganization();
		Person person = personOrOrganization.getPerson();

		List address = party.getAddress();
		Address homeAddress = new Address();
		Address mailAddress = new Address();
		Address busAddress = new Address();
		Address foreignAddress = new Address();

		if (!NbaUtils.isBlankOrNull(address)) {
			homeAddress = getResidenceAddress(address);
			mailAddress = getMailingAddress(address);
			busAddress = getBusinessAddress(address);
			foreignAddress = getForeignAddress(address);
		}

		Person newPerson = new Person();

		Organization organization = personOrOrganization.getOrganization();
		if (person != null) {
			if (person.hasFirstName() && person.getFirstName().trim().length() > 0) {
				newPerson.setFirstName(person.getFirstName());
			}
			if (person.hasLastName() && person.getLastName().trim().length() > 0) {
				newPerson.setLastName(person.getLastName());
			}
			if (person.hasMiddleName() && person.getMiddleName().trim().length() > 0) {
				newPerson.setMiddleName(person.getMiddleName());
			}
			if (person.hasBirthDate()) {
				newPerson.setBirthDate(person.getBirthDate());
			}
			if (person.hasBirthJurisdictionTC()) {
				newPerson.setBirthJurisdictionTC(person.getBirthJurisdictionTC());
			}
			if (person.hasOccupation()) {
				newPerson.setOccupation(person.getOccupation());
			}
			newPersonOrOrganization.setPerson(newPerson);
		} else {
			Organization newOrganization = new Organization();
			if (organization.hasDBA() && organization.getDBA().trim().length() > 0) {
				newOrganization.setDBA(organization.getDBA());
			}

			OrganizationExtension organizationExtension = NbaUtils.getFirstOrganizationExtension(organization);
			OrganizationExtension nbaOrganizationExtension = new OrganizationExtension();
			if (organizationExtension != null) {
				nbaOrganizationExtension.setNAICSCode(organizationExtension.getNAICSCode());

			}
			OLifEExtension oLifeorganizationExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
			oLifeorganizationExt.setOrganizationExtension(nbaOrganizationExtension);
			requestedParty.addOLifEExtension(oLifeorganizationExt);

			newPersonOrOrganization.setOrganization(newOrganization);
		}
		if (party.hasGovtID() && party.getGovtID().trim().length() > 0) {
			requestedParty.setGovtID(party.getGovtID());
		}
		requestedParty.setGovtIDTC(party.getGovtIDTC());
		requestedParty.setId(party.getId());
		requestedParty.setPartyTypeCode(party.getPartyTypeCode());
		requestedParty.setFullName(party.getFullName());
		OLifE olife = nbaReqTXLife.getOLifE();
		SourceInfo sourceInfo = new SourceInfo();
		sourceInfo.setFileControlID(getBackEnd());
		sourceInfo.setSourceInfoName("nbA_Life");//NBLXA-2152
		sourceInfo.setCreationDate(new Date());
		sourceInfo.setCreationTime(new NbaTime());
		olife.setSourceInfo(sourceInfo);
		Holding holding = new Holding();
		holding.setId(HOLDING_ID);
		Policy nbaPolicy = new Policy();
		nbaPolicy.setPolNumber(policy.getPolNumber());
		nbaPolicy.setProductType(policy.getProductType());
		nbaPolicy.setProductCode(policy.getProductCode());
		nbaPolicy.setCarrierCode(policy.getCarrierCode());
		nbaPolicy.setPolicyStatus(policy.getPolicyStatus());
		nbaPolicy.setEffDate(policy.getEffDate());

		//NBLXA-2464
		NbaVpmsAdaptor proxy = null;		
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(getNbaTXLife());
		boolean technicalIUPInd = false;
		Relation primaryRelation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_INSURED);
		oinkRequest.setPartyFilter(primaryRelation.getRelationRoleCode(), primaryRelation.getRelatedRefID());
		
		try{
		proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.AC_NON_MEDICAL_HISTORY);
		proxy.setANbaOinkRequest(oinkRequest);
		//proxy.setSkipAttributesMap(deOink);
		proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_TECHNICAL_IUP);
		NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
		if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {	
			ArrayList vpmsResultArray = vpmsResultsData.getResultsData();
			if(NbaConstants.TRUE == Integer.parseInt((String)vpmsResultArray.get(0))){
				technicalIUPInd = true;
			}
		}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			e.printStackTrace();
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("AxaWSBaeClientRiskScoreInvoker").logError(re);
				}
			}
		}			
		//NBLXA-2464 -End
		
		
		ApplicationInfo applInfo = new ApplicationInfo();
		applInfo.setSubmissionDate(policy.getApplicationInfo().getSubmissionDate());

		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
		ApplicationInfoExtension nbaApplicationInfoExtension = new ApplicationInfoExtension();
		if (!NbaUtils.isBlankOrNull(applicationInfoExtension)) {
			
			if(applicationInfoExtension.getInternationalUWProgInd() || technicalIUPInd){ //NBLXA-2464
				nbaApplicationInfoExtension.setInternationalUWProgInd(true);
			}else{
				nbaApplicationInfoExtension.setInternationalUWProgInd(applicationInfoExtension.getInternationalUWProgInd());			
			}
			
		}
		OLifEExtension oLifeApplicationInfoExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
		oLifeApplicationInfoExt.setApplicationInfoExtension(nbaApplicationInfoExtension);
		applInfo.addOLifEExtension(oLifeApplicationInfoExt);
		nbaPolicy.setApplicationInfo(applInfo);

		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		PolicyExtension nbaPolicyExtension = new PolicyExtension();
		if (!NbaUtils.isBlankOrNull(policyExtension)) {
			nbaPolicyExtension.setDistributionChannel(policyExtension.getDistributionChannel());
		}

		OLifEExtension oLifePolicyExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
		oLifePolicyExt.setPolicyExtension(nbaPolicyExtension);
		nbaPolicy.addOLifEExtension(oLifePolicyExt);

		holding.setPolicy(nbaPolicy);
		olife.addHolding(holding);
		requestedParty.setPersonOrOrganization(newPersonOrOrganization);

		if (requestedParty.getPartyTypeCode() == NbaOliConstants.OLIX_PARTYTYPE_INDIVIDUAL) {
			requestedParty.addAddress(homeAddress);
		} else if (requestedParty.getPartyTypeCode() == NbaOliConstants.OLIX_PARTYTYPE_CORPORATION) {
			requestedParty.addAddress(NbaUtils.isBlankOrNull(busAddress) ? mailAddress : busAddress);
		}
		if (!NbaUtils.isBlankOrNull(foreignAddress)) {
			requestedParty.addAddress(foreignAddress);
		}
		requestedParty.setPhone(party.getPhone());

		PartyExtension partyExtension = NbaUtils.getFirstPartyExtension(party);
		PartyExtension nbaPartyExtension = new PartyExtension();
		if (!NbaUtils.isBlankOrNull(partyExtension)) {
			nbaPartyExtension.setMDMPartyId(partyExtension.getMDMPartyId());
			nbaPartyExtension.setComplexEntityInd(partyExtension.getComplexEntityInd());
		}
		OLifEExtension oLifePartyExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
		oLifePartyExt.setPartyExtension(nbaPartyExtension);
		requestedParty.addOLifEExtension(oLifePartyExt);

		for (int i = 0; i < party.getEmploymentCount(); i++) {
			Employment employment = party.getEmploymentAt(i);
			Employment nbaEmployment = new Employment();
			nbaEmployment.setId(employment.getId());
			nbaEmployment.setEmploymentStatusTC(employment.getEmploymentStatusTC());
			nbaEmployment.setOccupation(employment.getOccupation());
			nbaEmployment.setTitle(employment.getTitle());
			nbaEmployment.setEmployerName(employment.getEmployerName());
			requestedParty.addEmployment(nbaEmployment);
		}
		if (party.hasRisk()) {
			Risk nbaRisk = new Risk();
			Risk risk = party.getRisk();
			RiskExtension riskExtension = NbaUtils.getFirstRiskExtension(risk);
			RiskExtension nbaRiskExtension = new RiskExtension();
			nbaRiskExtension.setFundingDisclosureDetails(riskExtension.getFundingDisclosureDetails());
			OLifEExtension oLifeRiskExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RISK);
			oLifeRiskExt.setRiskExtension(nbaRiskExtension);
			nbaRisk.addOLifEExtension(oLifeRiskExt);
			requestedParty.setRisk(nbaRisk);

		}
		if (party.hasClient()) {
			Client nbaClient = new Client();
			Client client = party.getClient();
			ClientExtension clientExtension = NbaUtils.getFirstClientExtension(client);
			ClientExtension nbaClientExtension = new ClientExtension();
			nbaClientExtension.setAgentVerifiesMilitaryStatusIndCode(clientExtension.getAgentVerifiesMilitaryStatusIndCode());
			OLifEExtension oLifeClientExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_CLIENT);
			oLifeClientExt.setClientExtension(nbaClientExtension);
			nbaClient.addOLifEExtension(oLifeClientExt);
			requestedParty.setClient(nbaClient);
		}
		olife.addParty(requestedParty);
		Relation ownerRelation = getNbaTXLife().getRelationForRoleAndRelatedId(NbaOliConstants.OLI_REL_OWNER,requestedParty.getId());
		if (!NbaUtils.isBlankOrNull(ownerRelation)) {
				Relation relation = new Relation();
				relation.setId(ownerRelation.getId());
				relation.setOriginatingObjectID(ownerRelation.getOriginatingObjectID());
				relation.setRelatedObjectID(ownerRelation.getRelatedObjectID());
				relation.setOriginatingObjectType(ownerRelation.getOriginatingObjectType());
				relation.setRelatedObjectType(ownerRelation.getRelatedObjectType());
				relation.setRelationRoleCode(NbaOliConstants.OLI_REL_OWNER);
				relation.setRelatedRefID(ownerRelation.getRelatedRefID());
				olife.addRelation(relation);
		}
		return nbaReqTXLife;
	}

	public Address getAddressByType(List address, long addressType) {
		Address partyAddress = new Address();
		Iterator iterate = address.iterator();
		while (iterate.hasNext()) {
			partyAddress = (Address) iterate.next();
			if (partyAddress.getAddressTypeCode() == addressType) {
				return partyAddress;
			}
		}
		return null;
	}

	public Address getResidenceAddress(List address) {
		return getAddressByType(address, NbaOliConstants.OLI_ADTYPE_HOME);
	}

	public Address getBusinessAddress(List address) {
		return getAddressByType(address, NbaOliConstants.OLI_ADTYPE_BUS);
	}

	public Address getMailingAddress(List address) {
		return getAddressByType(address, NbaOliConstants.OLI_ADTYPE_MAILING);
	}

	public Address getForeignAddress(List address) {
		return getAddressByType(address, NbaOliConstants.OLI_ADTYPE_27);
	}

}
