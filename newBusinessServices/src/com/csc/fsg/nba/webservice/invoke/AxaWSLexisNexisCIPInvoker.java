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

import java.util.List;
import java.util.Iterator;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.SourceInfo;

/**
 * This class is responsible for creating request for CIP webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>SR 564247</td>
 * <td>Discretionary</td>
 * <td>CIP Interface</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */



public class AxaWSLexisNexisCIPInvoker extends AxaWSInvokerBase {

	private static final String CATEGORY = "CIP";

	private static final String FUNCTIONID = "bridgerRetrieveCIData";
	

	/**
	 * @param operation
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 * @param object
	 */
	public AxaWSLexisNexisCIPInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	/**
	 * Create webservice request for CIP
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
		//Begin ALII1596
		List address = party.getAddress();
		Address homeAddress= new Address();
		Address mailAddress = new Address();
		Address busAddress = new Address();
		if (!NbaUtils.isBlankOrNull(address)) {
			homeAddress = getResidenceAddress(address);
			mailAddress = getMailingAddress(address);//APSL3100
			busAddress = getBusinessAddress(address);//APSL3100
		}
		//End ALII1596
		Person newPerson = new Person();
		//ALII1718
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
			newPersonOrOrganization.setPerson(newPerson);
		} else { //ALII1718
			Organization newOrganization = new Organization();
			if (organization.hasDBA() && organization.getDBA().trim().length() > 0) {
				newOrganization.setDBA(organization.getDBA());
			}
			newPersonOrOrganization.setOrganization(newOrganization);
		}
		if (party.hasGovtID() && party.getGovtID().trim().length() > 0) {
			requestedParty.setGovtID(party.getGovtID());
		}
		requestedParty.setGovtIDTC(party.getGovtIDTC());
		requestedParty.setId(party.getId());
		requestedParty.setPartyTypeCode(party.getPartyTypeCode());
		OLifE olife = nbaReqTXLife.getOLifE();
		SourceInfo sourceInfo = new SourceInfo();
		sourceInfo.setFileControlID(getBackEnd());
		sourceInfo.setSourceInfoName("nbA_Life");//NBLXA-2152
		olife.setSourceInfo(sourceInfo);
		Holding holding = new Holding();
		holding.setId(HOLDING_ID);
		Policy nbaPolicy = new Policy();
		nbaPolicy.setPolNumber(policy.getPolNumber());
		nbaPolicy.setProductCode(policy.getProductCode());
		nbaPolicy.setCarrierCode(policy.getCarrierCode());
		nbaPolicy.setPolicyStatus(policy.getPolicyStatus());
		nbaPolicy.setLineOfBusiness(policy.getLineOfBusiness());
		holding.setPolicy(nbaPolicy);
		olife.addHolding(holding);
		requestedParty.setPersonOrOrganization(newPersonOrOrganization);
		//APSL3100 Begin
		if(requestedParty.getPartyTypeCode() == NbaOliConstants.OLIX_PARTYTYPE_INDIVIDUAL){
			requestedParty.addAddress(homeAddress);
		}
		else if(requestedParty.getPartyTypeCode() == NbaOliConstants.OLIX_PARTYTYPE_CORPORATION){
			requestedParty.addAddress(NbaUtils.isBlankOrNull(busAddress)? mailAddress : busAddress);
		}
		//APSL3100 End
		requestedParty.setPhone(party.getPhone());//ALII1842
		olife.addParty(requestedParty);
		return nbaReqTXLife;

	}
	//ALII1596 New Method, APSL3100 Method Refactored
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
	
	//APSL3100 End
}
