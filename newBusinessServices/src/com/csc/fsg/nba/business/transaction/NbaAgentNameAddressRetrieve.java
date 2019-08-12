package com.csc.fsg.nba.business.transaction;
/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
//APSL2788


/**
 * Create a 228 request for calling the webservice. 
 * This class also provides methods to call the web service 
 * and merge the response data to original NbaTXLife object  
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA091</td><td>Version 3</td><td>Agent Name and Address</td></tr>* 
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR2366</td><td>Version 5</td><td>Added unique file names for webservice stubs</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>General source code clean up during version 5</td></tr>
 * <tr><td>SPR2662</td><td>Version 6</td><td>Poller stops when invalid contract data is present</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation for Agent Subset</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaAgentNameAddressRetrieve {
	protected NbaLob lobData = null; //NBA112
	protected static NbaLogger logger = null; //SPR2380 
	//NBA112 code deleted
	/**
	 * This method recieves a party object, a NbaTXLife and a relation role code. It creates an XML
	 * request for Agent name and address from these object.  
	 * @param com.csc.fsg.nba.vo.NbaTXLife
	 * @param com.csc.fsg.nba.vo..txLife.Party
	 * @param long
	 * @return NbaTXLife object containing 228 request
	 */
	//NBA112 changed method name and signature
	public NbaTXLife createRequest(NbaTXLife origNbaTXLife, NbaTXLife txLifeRequest, Relation origRelation) {//throws NbaBaseException{

		NbaOLifEId nbaOLifEId = new NbaOLifEId(txLifeRequest); //NBA112
		Party origParty = origNbaTXLife.getParty(origRelation.getRelatedObjectID()).getParty(); //NBA112
		origParty = resetNameField(origParty); //NBA112
		Party party = new Party();
		party.setId(origParty.getId());	//SPR2662
				
		Producer producer = new Producer();
		party.setProducer(producer);
		//begin NBA112
		OLifE olife = txLifeRequest.getOLifE();
		Holding holding = new Holding();
		Policy policy = new Policy();

		holding.setId(origRelation.getOriginatingObjectID());	//SPR2662
		olife.addHolding(holding);
		policy.setCarrierCode(origNbaTXLife.getCarrierCode()); //NBA112
		holding.setPolicy(policy);
		//end NBA112		
		CarrierAppointment carrierAppointment = new CarrierAppointment();
		nbaOLifEId.setId(carrierAppointment);
		carrierAppointment.setPartyID(party.getId());
		//NBA112 code deleted
		carrierAppointment.setCompanyProducerID(origParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID());
		producer.addCarrierAppointment(carrierAppointment);
		//NBA112 code deleted
		//Begin NBA112
		olife.addParty(party);//NBA112
		Relation relation = new Relation();
		relation.setRelationRoleCode(origRelation.getRelationRoleCode());
		relation.setOriginatingObjectID(origRelation.getOriginatingObjectID());
		relation.setRelatedObjectID(origRelation.getRelatedObjectID());
		relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
		relation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		olife.addRelation(relation);
		//End NBA112 
		return txLifeRequest; //NBA112
		
	}
	/**
	 * This method recieves an NbaTXLife object. Then it checks if the Agent data needs to be called from 
	 * External System. If the answer is yes, then it calls for agent data, merges it with the NbaTXLife
	 * object and returns the object. 
	 * 
	 * @param com.csc.fsg.nba.vo.NbaTXLife.
	 * @return com.csc.fsg.nba.vo.NbaTXLife.
	 */
	//NBA112 new method
	public NbaTXLife getExternalAgentSystemData(NbaTXLife nbaTXLife, NbaUserVO userVO) throws NbaBaseException {//AXAL3.7.40
		//Begin NBA112
		boolean dataStoreDB = false;
		if(getLobData() != null){
			dataStoreDB = NbaServerUtility.isDataStoreDB(getLobData(), null);
		} else {
			dataStoreDB = NbaServerUtility.isDataStoreDB(nbaTXLife);
		}
		if (dataStoreDB) {
		//End NBA112
			NbaTXLife result = null;
			ArrayList relationList = nbaTXLife.getOLifE().getRelation();
			Relation relation = null;
			TransResult transResult = null;
			ArrayList results = null;
			for (int i = 0; i < relationList.size(); i++) {
				relation = (Relation) relationList.get(i);
				if (NbaOliConstants.OLI_REL_PRIMAGENT == relation.getRelationRoleCode()) { 	//SPR2662
					result = getExternalAgentSystemData(nbaTXLife, relation, userVO);//AXAL3.7.40
					transResult = result.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					results = transResult.getResultInfo();
					break;
				}
			}
			if (transResult != null && transResult.getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) {
				mergeParty(nbaTXLife, result, relation);
			} else {
				ResultInfo resultInfo;
				String errorMessage = NbaBaseException.AGENT_WEBSERVICE_PROCESSING_ERROR; //SPR2662
				for (int i = 0; i < results.size(); i++) {
					resultInfo = (ResultInfo) results.get(i);
					//begin SPR2662
					if (resultInfo.getResultInfoDesc() == null) {
						errorMessage = errorMessage.concat(", ResultInfoCode: ").concat(Long.toString(resultInfo.getResultInfoCode()));
					} else {
						errorMessage =
							errorMessage.concat(resultInfo.getResultInfoDesc().substring(2, resultInfo.getResultInfoDesc().length()).trim());
					}
					//end SPR2662
				}
				throw new NbaBaseException(errorMessage);
			}
		}
		return nbaTXLife;
	}

	/**
	 * This method create and sends a NbaTXLife object to webservice and returns the 
	 * webservice response in the form of NbaTXLife object.
	 * @param NbaTXLife holding inquiry
	 * @param relation the agent relation for which webservice needs to be called
	 * @return NbaTXLife webservice response containing agent's information
	 */
	//NBA112 New Method
	//Begin AXAL3.7.40
	public NbaTXLife getExternalAgentSystemData(NbaTXLife nbaTXLife, Relation relation,NbaUserVO userVO) throws NbaBaseException { 

	    AxaProducerVO producerVO = new AxaProducerVO().createProducerVOfromTXLife(nbaTXLife, relation);
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_AGENTDEMOGRAPICS,userVO, null,null,producerVO);
		NbaTXLife newTXLife= (NbaTXLife) webServiceInvoker.execute();
		return 	newTXLife;
	}
   //End AXAL3.7.40
	/**
	 * This method recieves the Party object for agent in the original NbaTXLife object and the party 
	 * object recieved from the response. It merges the Person information in the original party object
	 * @param origParty Party object on contract
	 * @param newParty party returned from webservice
	 */
	//NBA112 New Method
	protected void mergePerson(Party origParty, Party newParty) {

		PersonOrOrganization personOrOrg = origParty.getPersonOrOrganization();
		Person origPerson = null;
		PersonOrOrganization newPersonOrOrg = newParty.getPersonOrOrganization(); //NBA112
		if (newPersonOrOrg != null && newPersonOrOrg.getPerson() != null) { //NBA112
			Person newPerson = newPersonOrOrg.getPerson();//NBA112
			if (personOrOrg != null) {
				origPerson = personOrOrg.getPerson();
				if (origPerson == null) {
					origPerson = new Person();
					origPerson.setActionAdd();
					personOrOrg.setPerson(origPerson);
				} else {
					origPerson.setActionUpdate();
				}
			} else {
				personOrOrg = new PersonOrOrganization();
				personOrOrg.setActionAdd();
				origParty.setPersonOrOrganization(personOrOrg);
				origPerson = new Person();
				origPerson.setActionAdd();
				personOrOrg.setPerson(origPerson);
			}
			if (newPerson != null) {
				origPerson.setPersonKey(newPerson.getPersonKey());
				//Begin NBA112
				if (newPerson.getFirstName() != null) {
					origPerson.setFirstName(newPerson.getFirstName().trim());
				}
				if (newPerson.getMiddleName() != null) {
					origPerson.setMiddleName(newPerson.getMiddleName().trim());
				}
				if (newPerson.getLastName() != null) {
					origPerson.setLastName(newPerson.getLastName().trim());
				}
				if (newPerson.getPrefix() != null) {
					origPerson.setPrefix(newPerson.getPrefix().trim());
				}
				if (newPerson.getSuffix() != null) {
					origPerson.setSuffix(newPerson.getSuffix().trim());
				}
				//End NBA112
			}
		}
	}
	/**
	 * This method recieves the Party object for agent in the orignial NbaTXLife object and the party 
	 * object recieved from the response. It merges the Address information in the original party object
	 * @param origParty Party object on contract
	 * @param newParty party returned from webservice
	 * @param nbaOlifeId an instance of <code>NbaOLifEId</code>
	 */
	//NBA112 New Method
	protected void mergeAddress(Party origParty, Party newParty, NbaOLifEId nbaOlifeId) {
		Address origAddress = null;

		int newAddCount = newParty.getAddressCount();
		int origAddrCount = origParty.getAddressCount();
		Address newAddress;
		boolean flag;
		if (newAddCount > 0) {
			for (int i = 0; i < newAddCount; i++) {
				flag = false;
				newAddress = newParty.getAddressAt(i);
				if (origAddrCount > 0) {
					for (int j = 0; j < origAddrCount; j++) {
						origAddress = origParty.getAddressAt(j);
						if (origAddress.getAddressTypeCode() == newAddress.getAddressTypeCode()) {
							flag = true;
							//Begin NBA112
							if(newAddress.getAttentionLine() != null){
								origAddress.setAttentionLine(newAddress.getAttentionLine().trim());	
							}
							if(newAddress.getLine1() != null){
								origAddress.setLine1(newAddress.getLine1().trim());	
							}
							if(newAddress.getLine2() != null){
								origAddress.setLine2(newAddress.getLine2().trim());	
							}
							if(newAddress.getLine3() != null){
								origAddress.setLine3(newAddress.getLine3().trim());	
							}
							if(newAddress.getCity() != null){
								origAddress.setCity(newAddress.getCity().trim());	
							}
							if(newAddress.getZip() != null){
								origAddress.setZip(newAddress.getZip().trim());	
							}
							//End NBA112
							origAddress.setAddressStateTC(newAddress.getAddressStateTC());
							origAddress.setAddressCountryTC(newAddress.getAddressCountryTC());
							origAddress.setActionUpdate();
							break;
						}
					}
				}
				if (!(flag)) {
					origAddress = new Address();
					nbaOlifeId.setId(origAddress);
					origAddress.setAddressTypeCode(newAddress.getAddressTypeCode()); // APSL4655
					//Begin NBA112
					if(newAddress.getAttentionLine() != null){
						origAddress.setAttentionLine(newAddress.getAttentionLine().trim());	
					}
					if(newAddress.getLine1() != null){
						origAddress.setLine1(newAddress.getLine1().trim());	
					}
					if(newAddress.getLine2() != null){
						origAddress.setLine2(newAddress.getLine2().trim());	
					}
					if(newAddress.getLine3() != null){
						origAddress.setLine3(newAddress.getLine3().trim());	
					}
					if(newAddress.getCity() != null){
						origAddress.setCity(newAddress.getCity().trim());	
					}
					if(newAddress.getZip() != null){
						origAddress.setZip(newAddress.getZip().trim());	
					}
					//End NBA112
					origAddress.setAddressStateTC(newAddress.getAddressStateTC());
					origAddress.setAddressCountryTC(newAddress.getAddressCountryTC());
					origAddress.setActionAdd();
					origParty.addAddress(origAddress);
				}
			}
		}
	}
	/**
	 * This method recieves the Party object for agent in the orignial NbaTXLife object and the party 
	 * object recieved from the response. It merges the Email information in the original party object
	 * and returns the original party object.  
	 * @param origParty Party object on contract
	 * @param newParty party returned from webservice
	 * @param origNbaOLifEId an instance of <code>NbaOLifEId</code>
	 */

	//NBA112 New Method
	protected void mergeEmail(Party origParty, Party newParty, NbaOLifEId origNbaOLifEId) {
		EMailAddress origEmailAddress = null;
		if (newParty.getEMailAddress() != null && newParty.getEMailAddress().size() != 0 && newParty.getEMailAddressAt(0).getAddrLine() != null
			&& newParty.getEMailAddressAt(0).getAddrLine().trim() != "") { //NBA112
			if (origParty.getEMailAddressCount() == 0) {
				origEmailAddress = new EMailAddress();
				origNbaOLifEId.setId(origEmailAddress);
				origParty.addEMailAddress(origEmailAddress);
				origEmailAddress.setActionAdd();
				origEmailAddress.setAddrLine(newParty.getEMailAddressAt(0).getAddrLine()); //APSL2539
				origEmailAddress.setEMailType(newParty.getEMailAddressAt(0).getEMailType()); //APSL2539
			} else {
				origEmailAddress = origParty.getEMailAddressAt(0);
				//Start APSL2539
				if(origEmailAddress.getEMailType() == newParty.getEMailAddressAt(0).getEMailType())
				{
					origEmailAddress.setActionUpdate();
					origEmailAddress.setAddrLine(newParty.getEMailAddressAt(0).getAddrLine());
				}
				//End APSL2539
			}			
			
		}
	}

	/** This method recieves the Party object for agency in the orignial NbaTXLife object and 
	 * the party object recieved from the response. It merges carrier appointment data from response
	 * @param origAgentParty Party object on contract
	 * @param responseAgentParty party returned from webservice
	 */
	//NBA112 New Method
	protected void mergeCarrierAppointment(Party origAgentParty, Party responseAgentParty) {
		//Begin NBA112
		Producer newProducer = responseAgentParty.getProducer();
		if(newProducer != null){
			CarrierAppointment origCarrierAppt = origAgentParty.getProducer().getCarrierAppointmentAt(0);
			CarrierAppointment newCarrierAppt = newProducer.getCarrierAppointmentAt(0);
			if (newCarrierAppt != null) {
				origCarrierAppt.setCompanyProducerID(newCarrierAppt.getCompanyProducerID());
				origCarrierAppt.setCarrierCode(newCarrierAppt.getCarrierCode());
				origCarrierAppt.setAppointmentCategory(newCarrierAppt.getAppointmentCategory());
				origCarrierAppt.setCarrierApptStatus(newCarrierAppt.getCarrierApptStatus());
				origCarrierAppt.setActionUpdate();
			}
		}
		//End NBA112
	}

	/**
	 * This method recieves the Party object for agency in the orignial NbaTXLife object and the 
	 * party object recieved from the response. It merges organzation assuming response contains a organization 
	 * @param origAgentParty Party object on contract
	 * @param responseAgentParty party returned from webservice
	 * @param nbaOlifeId an instance of <code>NbaOLifEId</code>
	 */
	//NBA112 New Method
	protected void mergeOrganization(Party origAgentParty, Party responseAgentParty) {
		PersonOrOrganization newPersonOrOrg = responseAgentParty.getPersonOrOrganization();
		PersonOrOrganization oldPersonOrOrg = origAgentParty.getPersonOrOrganization();
		Organization oldOrg = null;
		if (newPersonOrOrg != null) {
			Organization newOrg = newPersonOrOrg.getOrganization();
			if (newOrg != null) {				
				OrganizationExtension newOrgExtn = NbaUtils.getFirstOrganizationExtension(newOrg);//APSL2788
				OrganizationExtension oldOrgExtn = null;//APSL2788
				if (oldPersonOrOrg != null) {
					oldOrg = oldPersonOrOrg.getOrganization();
					if (oldOrg != null) {						
						oldOrg.setActionUpdate();
						//Begin APSL2788
						oldOrgExtn = NbaUtils.getFirstOrganizationExtension(oldOrg);
						if(oldOrgExtn != null) {
							oldOrgExtn.setActionUpdate();
						} else if(newOrgExtn != null && newOrgExtn.hasPermissionCode()){
							OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ORGANIZATION);
							oldOrg.addOLifEExtension(oLifeExt);
							oldOrgExtn = oLifeExt.getOrganizationExtension();
							if(oldOrgExtn != null){
								oldOrgExtn.setActionAdd();
							}
						}
						//end APSL2788
					} else {
						oldOrg = new Organization();
						oldOrg.setActionAdd();
						//Begin APSL2788
						if(newOrgExtn != null && newOrgExtn.hasPermissionCode()){
							OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ORGANIZATION);
							oldOrg.addOLifEExtension(oLifeExt);
							oldOrgExtn = oLifeExt.getOrganizationExtension();
							if(oldOrgExtn != null){
								oldOrgExtn.setActionAdd();
							}
						}
						//End APSL2788
					}
					oldOrg.setDBA(newOrg.getDBA());
					oldOrg.setOrgForm(newOrg.getOrgForm());
					//Begin APSL2788
					if(oldOrgExtn != null && newOrgExtn != null && newOrgExtn.hasPermissionCode()){
						oldOrgExtn.setPermissionCode(newOrgExtn.getPermissionCode());
					}
					//End APSL2788
				} else {
					oldPersonOrOrg = new PersonOrOrganization();
					oldOrg = new Organization();
					oldOrg.setActionAdd();
					oldOrg.setDBA(newOrg.getDBA());
					oldOrg.setOrgForm(newOrg.getOrgForm());
					//Begin APSL2788
					if(newOrgExtn != null && newOrgExtn.hasPermissionCode()){
						OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ORGANIZATION);
						oldOrg.addOLifEExtension(oLifeExt);
						oldOrgExtn = oLifeExt.getOrganizationExtension();
						if(oldOrgExtn != null){							
							oldOrgExtn.setActionAdd();
							oldOrgExtn.setPermissionCode(newOrgExtn.getPermissionCode());
						}
					}
					//End APSL2788
					oldPersonOrOrg.setOrganization(oldOrg);
				}
			}
		}
	}

	/**
	 * This method recieves the Party object for agency in the orignial NbaTXLife object and the party 
	 * object recieved from the response. It merges phone data for the party from agent subsystem to the contract   
	 * @param origAgentParty Party object representing original agent party
	 * @param responseAgentParty Party object representing agent party retreived from agent subsystem
	 */
	//NBA112 new method
	protected void mergePhone(Party origAgentParty, Party responseAgentParty, NbaOLifEId nbaOlifeId) {
		int origPhoneCount = origAgentParty.getPhoneCount();
		int newPhoneCount = responseAgentParty.getPhoneCount();
		Phone newPhone = null;
		Phone origPhone = null;
		boolean foundFlag;
		if (newPhoneCount > 0) {
			for (int i = 0; i < newPhoneCount; i++) {
				foundFlag = false;
				newPhone = responseAgentParty.getPhoneAt(i);
				if (origPhoneCount > 0) {
					for (int j = 0; j < origPhoneCount; j++) {
						origPhone = origAgentParty.getPhoneAt(j);
						if (origPhone.getPhoneTypeCode() == newPhone.getPhoneTypeCode()) {
							foundFlag = true;
							origPhone.setAreaCode(newPhone.getAreaCode());
							origPhone.setDialNumber(newPhone.getDialNumber());
							origPhone.setActionUpdate();
							break;
						}
					}
				}
				if (!(foundFlag)) {
					origPhone = new Phone();
					nbaOlifeId.setId(origPhone);
					origPhone.setPhoneTypeCode(newPhone.getPhoneTypeCode());
					origPhone.setAreaCode(newPhone.getAreaCode());
					origPhone.setDialNumber(newPhone.getDialNumber());
					origPhone.setActionAdd();
					origAgentParty.addPhone(origPhone);
				}
			}
		}
	}

	/** This method recieves the original holding inquiry object and the webservice response
	 *  It then merges the party's information to the contract for given relation. 
	 * @param nbaTxlife existing contract 
	 * @param txlifeAgentInfo result containing agent data
	 * @param relation represents the party whose information to be updated  
	 * @throws NbaBaseException
	 */
	//NBA112 new method
	public void mergeParty(NbaTXLife nbaTxlife, NbaTXLife txlifeAgentInfo, Relation relation) throws NbaBaseException {
		NbaOLifEId nbaOlifeId = null;
		Party origAgentParty = nbaTxlife.getParty(relation.getRelatedObjectID()).getParty();
		TXLifeResponse txLifeResponse = txlifeAgentInfo.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
		Party responseAgentParty  = null;
		if(NbaOliConstants.OLI_REL_BGACASEMANAGER == relation.getRelationRoleCode()){
		    responseAgentParty = txlifeAgentInfo.getParty(txlifeAgentInfo.getPartyId(relation.getRelationRoleCode())).getParty();
		}
        else {
            responseAgentParty = txLifeResponse.getOLifE().getPartyAt(0);
        }
		//ALPC183 end
		origAgentParty.setPrefComm(responseAgentParty.getPrefComm());
		origAgentParty.setPartyTypeCode(responseAgentParty.getPartyTypeCode());
		//NBA103 - removed try catch
		nbaOlifeId = new NbaOLifEId(nbaTxlife);
		mergePerson(origAgentParty, responseAgentParty);
		mergeAddress(origAgentParty, responseAgentParty, nbaOlifeId);
		mergePhone(origAgentParty, responseAgentParty, nbaOlifeId);
		mergeEmail(origAgentParty, responseAgentParty, nbaOlifeId);
		mergeOrganization(origAgentParty, responseAgentParty);
		mergeCarrierAppointment(origAgentParty, responseAgentParty);
	}
	/**
	 * Reset the agent's name before calling name and address retrieval web service  
	 * @param origParty the agent's party to be updated
	 * @return Party updated party object
	 */
	//NBA112 New Method
	protected Party resetNameField(Party origParty){
		if(origParty.getPersonOrOrganization() != null){
			Person person = origParty.getPersonOrOrganization().getPerson();
			if(person != null){
				if (person.getFirstName() != null) {
					person.setFirstName("");
				}
				if (person.getMiddleName() != null) {
					person.setMiddleName("");
				}
				if (person.getLastName() != null) {
					person.setLastName("");
				}
				if (person.getPrefix() != null) {
					person.setPrefix("");
				}
				if (person.getSuffix() != null) {
					person.setSuffix("");
				}
				person.setActionUpdate();
			}
		}
		return origParty;
	}
	/**
	 * Get LOB data for the work item 
	 * @return NbaLob LOB data on work item
	 */
	//NBA112 New Method
	protected NbaLob getLobData() {
		return lobData;
	}
	
	/**
	 * Set LOB data for the work item
	 * @param lob NbaLob object representing LOB data for work item
	 */
	//NBA112 New Method
	public void setLobData(NbaLob lob){
		lobData = lob;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	//SPR2380 New Method
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log("NbaAgentNameAddressRetrieve could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	
	/** This method recieves the original holding inquiry object and the webservice response
	 *  It then merges the party's information to the contract for given relation. 
	 * @param nbaTxlife existing contract 
	 * @param txlifeAgentInfo result containing agent data
	 * @param relation represents the party whose information to be updated  
	 * @throws NbaBaseException
	 */
	//APSL3447 new method	
	public void updateParty(NbaTXLife nbaTxlife, NbaTXLife txlifeAgentInfo, Relation relation) throws NbaBaseException {
		TXLifeResponse txLifeResponse = txlifeAgentInfo.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
		List partyList = txLifeResponse.getOLifE().getParty();
		List relList = txLifeResponse.getOLifE().getRelation();
		Party responseAgentParty = null;
		boolean flag = true;
		responseAgentParty = txLifeResponse.getOLifE().getPartyAt(0);
		ArrayList hierarchyRelList = new ArrayList();
		ArrayList resetRelIdList = new ArrayList();

		for (int i = 1; i <= (partyList.size() - 1); i++) {
			boolean partyExist = false;
			Party iParty = (Party) partyList.get(i);
			List partyRelations = NbaUtils.getAgentHierarchyRelationListbyRelatedObj(txlifeAgentInfo.getOLifE(), iParty.getId());
			for (int j = 0; j < partyRelations.size(); j++) {
				Relation partyRel = (Relation) partyRelations.get(j);
				if (partyRel.getRelationRoleCode() == NbaOliConstants.OLI_REL_DVP 
						|| partyRel.getRelationRoleCode() == NbaOliConstants.OLI_REL_NATIONAL_AGENCY) { //APSL4839
					partyExist = true;
					break;
				}
			}
			if (!partyExist) {
				iParty.setActionAdd();
				nbaTxlife.getOLifE().addParty(iParty);
			}
		}
		deleteParties(nbaTxlife, relation.getRelatedObjectID());		
		for (int i = 1; i <= (relList.size() - 1); i++) {
			Relation rel = (Relation) relList.get(i);
			flag = true;
			if (!(rel.getRelationRoleCode() == NbaOliConstants.OLI_REL_DVP 
					|| rel.getRelationRoleCode() == NbaOliConstants.OLI_REL_NATIONAL_AGENCY)) { //APSL4839
				if (rel.getOriginatingObjectID().equalsIgnoreCase(responseAgentParty.getId())) {
					rel.setOriginatingObjectID(relation.getRelatedObjectID());
				}
				if (rel.getRelatedObjectID().equalsIgnoreCase(responseAgentParty.getId())) {
					rel.setRelatedObjectID(relation.getRelatedObjectID());
					resetRelIdList.add(rel);
					flag = false;
				}
				if (flag) {
					hierarchyRelList.add(rel);
				}
				rel.setActionAdd();
				nbaTxlife.getOLifE().addRelation(rel);
			}
		}
		resetCMIds(nbaTxlife, relation.getRelatedObjectID(),txLifeResponse, NbaOliConstants.OLI_REL_PROCESSINGFIRM);
		resetCMIds(nbaTxlife, relation.getRelatedObjectID(),txLifeResponse, NbaOliConstants.OLI_REL_SUBORDAGENT);
		resetCMIds(nbaTxlife, relation.getRelatedObjectID(),txLifeResponse, NbaOliConstants.OLI_REL_GENAGENT);
		NbaOLifEId nbaOlifeId = new NbaOLifEId(nbaTxlife);
		nbaOlifeId.resolveAgentHierarchyIds(nbaTxlife, hierarchyRelList, resetRelIdList);
		nbaOlifeId.resolveDuplicateIds(nbaTxlife);
		nbaOlifeId.resetOwnerOtherInsRelationId(nbaTxlife);
		Party agentParty = NbaTXLife.getPartyFromId(relation.getRelatedObjectID(), nbaTxlife.getOLifE().getParty());
		NbaUtils.getFirstPartyExtension(agentParty).setHirarchyRetrievedInd(true);
		agentParty.setActionUpdate();
	}
	
	protected void deleteParties(NbaTXLife txlife,String relId){
		List relList = new ArrayList();
		relList = NbaUtils.getRelationByOriginatingObjectIdAndRoleCode(txlife.getOLifE(), relId, NbaOliConstants.OLI_REL_PROCESSINGFIRM);
		if(relList.size() > 0){
			Relation rel = (Relation)relList.get(0);
			rel.setActionDelete();
			Party party = NbaTXLife.getPartyFromId(rel.getRelatedObjectID(),txlife.getOLifE().getParty());
			party.setActionDelete();
		}
		relList = NbaUtils.getRelationByOriginatingObjectIdAndRoleCode(txlife.getOLifE(), relId, NbaOliConstants.OLI_REL_SUBORDAGENT);
		if(relList.size() > 0){
			Relation rel = (Relation)relList.get(0);
			rel.setActionDelete();
			Party party = NbaTXLife.getPartyFromId(rel.getRelatedObjectID(),txlife.getOLifE().getParty());
			party.setActionDelete();
		}		
		relList = NbaUtils.getRelationByOriginatingObjectIdAndRoleCode(txlife.getOLifE(), relId, NbaOliConstants.OLI_REL_GENAGENT);
		if(relList.size() > 0){
			Relation rel = (Relation)relList.get(0);
			rel.setActionDelete();
			Party party = NbaTXLife.getPartyFromId(rel.getRelatedObjectID(),txlife.getOLifE().getParty());
			party.setActionDelete();
		}
	}
	
	protected void resetCMIds(NbaTXLife txlife, String relId, TXLifeResponse response, long roleCode) {
		List relList = new ArrayList();
		Relation rel = null;
		String id = null;
		rel = NbaUtils.getRelation(response.getOLifE(), roleCode);
		id = rel != null ? rel.getRelatedObjectID() : null;
		List cmRelList = new ArrayList();
		relList = NbaUtils.getRelationByOriginatingObjectIdAndRoleCode(txlife.getOLifE(), relId, roleCode);
		Iterator itr = relList.iterator();
		while (itr.hasNext()) {
			rel = (Relation) itr.next();
			if (rel.isActionDelete()) {
				cmRelList = NbaUtils.getRelationByOriginatingObjectIdAndRoleCode(txlife.getOLifE(), rel.getRelatedObjectID(),
						NbaOliConstants.OLI_REL_BGACASEMANAGER);
			}
		}
		if (cmRelList.size() > 0 && id != null) {
			rel = (Relation) cmRelList.get(0);
			rel.setOriginatingObjectID(id);
		}
	}

}