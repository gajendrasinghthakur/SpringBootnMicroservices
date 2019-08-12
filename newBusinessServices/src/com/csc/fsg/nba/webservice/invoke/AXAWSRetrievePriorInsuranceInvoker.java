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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fsg.nba.contract.validation.NbaContractValidationConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaValidationMessageData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsPartyInquiryRequestData;

/**
 * This class is responsible for creating request for Retreive Prior Insurance webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.22</td>
 * <td>AXA Life Phase 1</td>
 * <td>TAIRetreive Interface</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AXAWSRetrievePriorInsuranceInvoker extends AxaWSInvokerBase {
	
	private static final String CATEGORY = "UnderwritingRisk";

	private static final String FUNCTIONID = "UnderwritingRisk"; 

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AXAWSRetrievePriorInsuranceInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}
		
	/**
	 * This method first calls the superclass createRequest() and then set the request specefic attribute.
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		NbaUserVO user = getUserVO();
		Map partyMap = (HashMap) getObject();
		Party party = (Party) partyMap.get(NbaConstants.ORG_PARTY);
		NbaVpmsPartyInquiryRequestData data = (NbaVpmsPartyInquiryRequestData) partyMap.get(NbaConstants.PARTY_INQDATA);
		Policy policy = getNbaTXLife().getPolicy();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PARTYINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_TYPE_UWRSKSUBTYPE);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setNbaUser(user);
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		// SPR3290 code deleted
		Party requestedParty = new Party();
		PersonOrOrganization personOrOrganization = party.getPersonOrOrganization();
		PersonOrOrganization newPersonOrOrganization = new PersonOrOrganization();
		Person person = personOrOrganization.getPerson();
		Person newPerson = new Person();
		//If vpms data includes first name then include first name in party inquiry transaction
		if (data.getFirstName() && person.hasFirstName() && person.getFirstName().trim().length() > 0) {
			newPerson.setFirstName(person.getFirstName());
		}
		//If vpms data includes last name then include last name in party inquiry transaction
		if (data.getLastName() && person.hasLastName() && person.getLastName().trim().length() > 0) {
			newPerson.setLastName(person.getLastName());
		}
		if (data.getMiddleName() && person.hasMiddleName() && person.getMiddleName().trim().length() > 0) {
			newPerson.setMiddleName(person.getMiddleName());
		}
		if (data.getBirthDate() && person.hasBirthDate()) {
			newPerson.setBirthDate(person.getBirthDate());
		}
		if (data.getBirthState() && person.hasBirthJurisdictionTC()) {
			newPerson.setBirthJurisdictionTC(person.getBirthJurisdictionTC());
		}
		if (data.getGovtID() && party.hasGovtID() && party.getGovtID().trim().length() > 0) {
			requestedParty.setGovtID(party.getGovtID());
		}
		//begin AXAL3.7.21
		if (data.isResidenceState() && party.hasResidenceState() && party.getResidenceState() != 0) {
			requestedParty.setResidenceState(party.getResidenceState());
		}
		requestedParty.setGovtIDTC(party.getGovtIDTC());
		requestedParty.setId(party.getId());
		requestedParty.setPartyTypeCode(party.getPartyTypeCode());
		OLifE olife = nbaReqTXLife.getOLifE();
		SourceInfo sourceInfo = new SourceInfo();
		sourceInfo.setFileControlID(getBackEnd());	//P2AXAL008
		sourceInfo.setSourceInfoName("nbA");
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
		//end AXAL3.7.21
		newPersonOrOrganization.setPerson(newPerson);
		requestedParty.setPersonOrOrganization(newPersonOrOrganization);
		olife.addParty(requestedParty);
		return nbaReqTXLife;
	}	
	
	/**
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 */
	protected void handleResponse() throws NbaBaseException {
		try {
			super.handleResponse();
		} catch (NbaBaseException e) {
			if (!e.isFatal()) {
				Map partyMap = (HashMap) getObject();
				Party orgParty = (Party) partyMap.get(NbaConstants.ORG_PARTY);
				addSystemMessage((NbaTXLife) getWebserviceResponse(), orgParty.getId());
			} else {
				throw e;
			}
		}
	}	
	
	/**
	 * Add a system message to original contract based on the type of failure
	 * @param txLifeResponse an instance of <code>TXLifeResponse</code> object containing service response
	 * @param partyId ID of party being processed
	 */
	//NAB124 New Method
	protected void addSystemMessage(NbaTXLife txLifeResponse, String partyId) {
		NbaTXLife holdingInq = getNbaTXLife();
		SystemMessage msg = new SystemMessage();
		long messageCode = -1L;
		StringBuffer messageDesc = new StringBuffer(); //SPR2737
		Holding holding = holdingInq.getPrimaryHolding();
		List messageList = holding.getSystemMessage();
		ListIterator messageIterator = messageList.listIterator();
		SystemMessage currentMessage = null;
		while (messageIterator.hasNext()) {
			currentMessage = (SystemMessage) messageIterator.next();
			if (currentMessage.getMessageCode() == NbaConstants.UW_RISK_REQUEST_ERROR) {	//ALS4006
				currentMessage.setActionDelete();
			}
		}

		TransResult transResult = txLifeResponse.getTransResult();
		long resultCode = transResult.getResultCode();
		//begin SPR2737
		if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
			if (transResult.getResultInfoCount() > 0) {
				ResultInfo resultInfo = transResult.getResultInfoAt(0);
				if (resultInfo.hasResultInfoCode() && resultInfo.hasResultInfoDesc()) {
					messageDesc.append(resultInfo.getResultInfoCode());
					messageDesc.append(", ");
					messageDesc.append(resultInfo.getResultInfoDesc().trim());
				}
			}
			messageCode = NbaConstants.UW_RISK_REQUEST_ERROR;
		}
		//end SPR2737
		NbaOLifEId nbaOLifEId = new NbaOLifEId(holdingInq);
		nbaOLifEId.setId(msg);
		msg.setMessageCode((int) messageCode);
		msg.setRelatedObjectID(partyId);
		msg.setActionAdd();
		msg.setMessageStartDate(new Date(System.currentTimeMillis()));
		msg.setSequence("0");
		//Add the SystemMessageExtension
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
		msg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(msg);
		systemMessageExtension.setMsgOverrideInd(false);
		systemMessageExtension.setMsgValidationType(NbaContractValidationConstants.SUBSET_UW_RISK);
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		NbaValidationMessageData[] nbaTableData;
		try {
			nbaTableData = (NbaValidationMessageData[]) nbaTableAccessor.getValidationMessages(String.valueOf(messageCode));
			if (nbaTableData.length > 0) {
				//trim if message length is greater then 100
				messageDesc.insert(0, nbaTableData[0].getMsgDescription() + " "); //SPR2737
				msg.setMessageDescription(messageDesc.length() > 100 ? messageDesc.substring(0, 100) : messageDesc.toString()); //SPR2737
				msg.setMessageSeverityCode(nbaTableData[0].getMsgSeverityTypeCode());
			}
		} catch (NbaDataAccessException e) {
			getLogger().logException(e);
		}

		holding.addSystemMessage(msg);
		holding.setActionUpdate();
	}
}
