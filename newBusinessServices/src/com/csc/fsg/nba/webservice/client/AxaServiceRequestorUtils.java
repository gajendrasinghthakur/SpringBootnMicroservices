package com.csc.fsg.nba.webservice.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Company;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vo.txlife.VendorApp;
import com.csc.fsg.nba.vo.txlife.VendorName;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

/** 
 * This class builds TXLife objects for interfaces to AXA
 * <p>  
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> 
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * 
 * <tr><td>AXAL3.7.17</td><td>AXA Life Phase 1</td><td>CAPS Interface</td>
 * <tr><td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance Interface</td></tr>
 * <tr><td>AXAL3.7.16</td><td>AXA Life Phase 1</td><td>TAI Interface</td></tr>
 * <tr><td>AXAL3.7.25</td><td>AXA Life Phase 2</td><td>Client Interface</td></tr>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interfaces</td></tr>
 * <tr><td>AXAL3.7.27</td><td>AXA Life Phase 1</td><td>RTS Interface</td></tr>
 * <tr><td>ALPC066</td><td>AXA Life Phase 1</td><td>Term Series Qualified</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaServiceRequestorUtils {
	protected NbaLogger logger = null;
	private String HOLDING_ID = "Holding_1"; 
	private String FINANCIAL_ACTIVITY_ID = "FinancialActivity_"; 
	private String providerHPH = "Hooper Holmes";
	private String providerLN = "Lexis Nexis";
	private String SERVICE_PROVIDER_LN = "LEXISNEXIS";
	
	private int MAX_RECORDS = 50;
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory."); 
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}	
	
	/**
	 * Return the primary insured Relation
	 * @return Relation or null if not found
	 */
	// SPR2817 New Method
	public Relation getWritingAgentRelation(NbaTXLife txLife) {
		ArrayList relations = txLife.getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (NbaUtils.isPrimaryWritingAgentRelation(relation)) {	//NBA213
					return relation;
				}
			}
		}
		return null;
	}
	
	//ALII53 Code deleted

	/**
	 * This method creates the XMLife message for 121 transaction code.
	 * 
	 * @return the generated XMLife message
	 * @param NbaDst - the work item
	 */
	// AXAL3.7.31 New Method
	public NbaTXLife createTXLife121Request(NbaDst reqItem, NbaTXLife nbaTxLife, NbaUserVO userVO, String compoundContractNumber) throws NbaBaseException {
	    NbaLob lob = reqItem.getNbaLob();
	    NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
	    nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_GENREQUIREORDREQ);
	    nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
	    nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(userVO));
	    nbaTXRequest.setNbaLob(lob);
	    RequirementInfo thisRequirementInfo = nbaTxLife.getRequirementInfo(lob.getReqUniqueID());
		
	    //create txlife with default request fields
	    NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
	    NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife);
	    //assign vendor code for EIB routing
	    VendorApp vendorApp = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest().getVendorApp();
		VendorName vendorName = new VendorName();
		vendorName.setVendorCode(getProviderID(reqItem, thisRequirementInfo, userVO));
		String providerName = null;
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		NbaTableData[] data = tableAccessor.getDisplayData(reqItem, NbaTableConstants.NBA_PROVIDERNAME);
		for (int i = 0; providerName == null && i < data.length; i++) {
			if ((((NbaUctData)data[i]).getIndexValue()).equalsIgnoreCase(lob.getReqVendor())){
				providerName = ((NbaUctData)data[i]).getIndexTranslation();
			}
		}
		vendorName.setPCDATA(providerName);
		vendorApp.setVendorName(vendorName);

		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTestIndicator(getTestIndicator(lob.getReqVendor()));
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest().getUserLoginNameAndUserPswdOrUserSessionKey().getUserLoginNameAndUserPswd().setUserLoginName(userVO.getUserID());

	    //get olife 
	    OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
	    Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
	    Policy policy = holding.getPolicy();
		policy.setId("Policy_1");
		
	    olife.getSourceInfo().setCreationDate(new Date());
	    olife.getSourceInfo().setCreationTime(new NbaTime());
	    
	    holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		holding.setHoldingForm(NbaOliConstants.OLI_HOLDFORM_IND); 

	    //Life
	    LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnut = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
	    policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnut);
	    long productType = nbaTxLife.getPrimaryHolding().getPolicy().getProductType();
	    policy.setProductType(productType);
		policy.setPolNumber(compoundContractNumber);
	    NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
	    NbaOinkRequest oinkRequest = new NbaOinkRequest();
	    if(providerHPH.equals(providerName)){
	    	policy.setProductCode(nbaTxLife.getPrimaryHolding().getPolicy().getProductCode());
	    	policy.setCarrierCode(nbaTxLife.getPrimaryHolding().getPolicy().getCarrierCode());
	    	policy.setPlanName(nbaTxLife.getPrimaryHolding().getPolicy().getPlanName());
	    	policy.setEffDate(nbaTxLife.getPrimaryHolding().getPolicy().getEffDate());
	    	policy.setPaymentMode(nbaTxLife.getPrimaryHolding().getPolicy().getPaymentMode());
	    	policy.setPaymentMethod(nbaTxLife.getPrimaryHolding().getPolicy().getPaymentMethod());
	    	
	    }
	    if ((productType == NbaOliConstants.OLI_PRODTYPE_ANN || productType == NbaOliConstants.OLI_PRODTYPE_VAR)
	        && (lob.getReqPersonCode() == NbaOliConstants.OLI_REL_ANNUITANT
	            || lob.getReqPersonCode() == NbaOliConstants.OLI_REL_JOINTANNUITANT)) {
	        policy.setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_ANNUITY);
	        Annuity annu = new Annuity();
	        lifeAnut.setAnnuity(annu);
	        annu.setInitPaymentAmt(lob.getFaceAmount());
	        Participant participant = new Participant();
	        participant.setPartyID("Party_1");
	        participant.setParticipantRoleCode(NbaOliConstants.OLI_PARTICROLE_ANNUITANT);
	        Payout payout = new Payout();
	        payout.addParticipant(participant);
	        annu.addPayout(payout);
	    } else {
			policy.setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_LIFE);
			Life life = new Life();
			nbaOLifEId.setId(life);
			lifeAnut.setLife(life);
			oinkRequest.setPartyFilter(
				lob.getReqPersonCode(),
				lob.getReqPersonSeq() < 10 ? "0" + lob.getReqPersonSeq() : String.valueOf(lob.getReqPersonSeq()));
			oinkRequest.setVariable("CurrentAmt");
			life.setFaceAmt(oinkDataAccess.getStringValueFor(oinkRequest)); // Face Amount
			 if (providerHPH.equals(providerName)) {
				Life origLife = nbaTxLife.getPrimaryHolding().getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
				if (origLife != null) {
					life.setQualPlanType(origLife.getQualPlanType());
					life.setInitialPremAmt(origLife.getInitialPremAmt());
					life.setDivType(origLife.getDivType());
					Coverage cov = new Coverage();

					Coverage baseCov = nbaTxLife.getPrimaryCoverage() ;
					if (baseCov != null) {
						cov.setPlanName(baseCov.getPlanName());
						cov.setShortName(baseCov.getShortName());
						cov.setIndicatorCode(baseCov.getIndicatorCode());
						cov.setDeathBenefitOptType(baseCov.getDeathBenefitOptType());
						cov.setCurrentAmt(baseCov.getCurrentAmt());
						LifeParticipant lifePartOrig = new LifeParticipant();
						LifeParticipant lifePart = baseCov.getLifeParticipantAt(0);
						if (lifePart != null) {
							lifePart.setLifeParticipantRoleCode(lifePartOrig.getLifeParticipantRoleCode());
							lifePart.setSmokerStat(lifePartOrig.getSmokerStat());
							lifePart.setUnderwritingClass(lifePartOrig.getUnderwritingClass());
							cov.addLifeParticipant(lifePart);
						}
						life.addCoverage(cov);
					}
				}
			}
	    }
	    
	    

	    //ApplicationInfo
	    ApplicationInfo applInfo = new ApplicationInfo();
	    applInfo.setSignedDate(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getSignedDate());
	    applInfo.setHOUnderwriterName(lob.getUndwrtQueue());
	    applInfo.setTrackingID(lob.getReqUniqueID());
	    applInfo.setApplicationJurisdiction(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getApplicationJurisdiction());
	    applInfo.setNBContactName(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getNBContactName());
	    policy.setApplicationInfo(applInfo);

	    //RequirementInfo
	    RequirementInfo reqInfo = new RequirementInfo();
	    nbaOLifEId.setId(reqInfo);
	    reqInfo.setReqCode(lob.getReqType());
		reqInfo.setRequestedDate(new Date());
		reqInfo.setRequirementInfoUniqueID(lob.getReqUniqueID());
		reqInfo.setRequirementAcctNum(getRequirementAccountNum(reqItem, nbaTxLife, reqInfo, userVO));
	    reqInfo.setRequirementDetails(thisRequirementInfo.getRequirementDetails());
	    if(providerHPH.equals(providerName)){
	    	reqInfo.setRequestedScheduleDate(thisRequirementInfo.getRequestedScheduleDate());
			reqInfo.setRequestedScheduleTimeStart(thisRequirementInfo.getRequestedScheduleTimeStart());
			reqInfo.setReleasePartyOrgCode(thisRequirementInfo.getReleasePartyOrgCode());
	    }
	    //NBLXA-2072 Begin
	    if(providerLN.equals(providerName)){
	    	OLifEExtension olifeExt = new OLifEExtension();
	    	olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
	    	reqInfo.addOLifEExtension(olifeExt);
	    	RequirementInfoExtension requirementInfoExtension = new RequirementInfoExtension();
			olifeExt.setRequirementInfoExtension(requirementInfoExtension);
			TrackingInfo  trackingInfo= new TrackingInfo();
			requirementInfoExtension.setTrackingInfo(trackingInfo);
			trackingInfo.setId(null);
		    nbaOLifEId.setId(trackingInfo); 
		    trackingInfo.setTrackingServiceProvider(SERVICE_PROVIDER_LN);
		    
			if(thisRequirementInfo.hasUserCode()){
				reqInfo.setUserCode(thisRequirementInfo.getUserCode());	
			}
	    }
	    //NBLXA-2072 end	
	    policy.addRequirementInfo(reqInfo);

	    //Attachment
	    Attachment attach = new Attachment();
	    nbaOLifEId.setId(attach);    
	    attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_COMMENT);
	    AttachmentData attachData = new AttachmentData();
	    attachData.setPCDATA("");
	    attach.setAttachmentData(attachData);
	    holding.addAttachment(attach);

	    //get insured party information (from holding inquiry)
	    Relation partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq());
	    NbaParty holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID());
	    if (holdingParty == null) {
	        throw new NbaBaseException("Could not get party information from holding inquiry");
	    }

	    //Insured person
	    if (holdingParty.getParty().getPartyTypeCode() != 1) { //should be an indivisual
	        throw new NbaBaseException("Invalid Party");
	    }
	    Party party = new Party();
	    nbaOLifEId.setId(party);    
	    party.setPartyTypeCode(holdingParty.getParty().getPartyTypeCode());
	    party.setGovtID(holdingParty.getSSN());
	    party.setFullName(holdingParty.getDisplayName());
	    party.setResidenceState( holdingParty.getParty().getResidenceState());
	    party.setResidenceCountry(holdingParty.getParty().getResidenceCountry());
        // NBLXA-2443 Begin
		if (lob != null && lob.getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT) {
			EMailAddress insuredEMail = null;
			for (int i = 0; i < holdingParty.getParty().getEMailAddressCount(); i++) {
				insuredEMail = holdingParty.getParty().getEMailAddressAt(i);
				if (insuredEMail != null && insuredEMail.getEMailType() == NbaOliConstants.OLI_EMAIL_BUSINESS && insuredEMail.getAddrLine() != null) {
					insuredEMail.setId(null);
					nbaOLifEId.setId(insuredEMail);
					party.addEMailAddress(insuredEMail);
				}
			}
		}
		// NBLXA-2443 End
	    olife.addParty(party);
		reqInfo.setAppliesToPartyID(party.getId());

		//Person
	    PersonOrOrganization perOrg = new PersonOrOrganization();
	    party.setPersonOrOrganization(perOrg);
	    Person person = new Person();
	    perOrg.setPerson(person);
	    Person holdingPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
	    person.setPrefix(holdingPerson.getPrefix());
	    person.setLastName(holdingPerson.getLastName());
	    person.setFirstName(holdingPerson.getFirstName());
	    person.setMiddleName(holdingPerson.getMiddleName());
	    person.setAge(holdingPerson.getAge());
	    person.setSuffix(holdingPerson.getSuffix());
	    person.setGender(holdingPerson.getGender());
	    person.setBirthDate(holdingPerson.getBirthDate());
	    person.setMarStat(holdingPerson.getMarStat());
	    person.setDriversLicenseNum(holdingPerson.getDriversLicenseNum());
	    person.setDriversLicenseState(holdingPerson.getDriversLicenseState());
	    person.setOccupation(holdingPerson.getOccupation());
		person.setSmokerStat(holdingPerson.getSmokerStat());
	    OLifEExtension olifeExt = new OLifEExtension();
		PersonExtension holdingPersonExtension = NbaUtils.getFirstPersonExtension(holdingPerson);
		if (holdingPersonExtension != null) {
			olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
			person.addOLifEExtension(olifeExt);
			PersonExtension personExtension = new PersonExtension();
			olifeExt.setPersonExtension(personExtension);
			personExtension.setRateClass(holdingPersonExtension.getRateClass());
			
		}
		
	    //Address
	    Address holdingAddr = null;
	    List list = holdingParty.getParty().getAddress();
	    for (int i = 0; i < list.size(); i++) {
	        Address addr = new Address();
	        holdingAddr = (Address) list.get(i);
	    	nbaOLifEId.setId(addr);         
	        addr.setAddressTypeCode(holdingAddr.getAddressTypeCode());
	        addr.setLine1(holdingAddr.getLine1());
	        addr.setLine2(holdingAddr.getLine2());
	        addr.setLine3(holdingAddr.getLine3());
	        addr.setCity(holdingAddr.getCity());
	        addr.setAddressStateTC(holdingAddr.getAddressStateTC());
	        if (holdingAddr.hasAddressState()) {
	        	addr.setAddressState(holdingAddr.getAddressState());
	        } else if (holdingAddr.hasAddressStateTC()) {
	        	addr.setAddressState(getStateCodeAbbreviation(holdingAddr.getAddressStateTC()));
	        }
	        addr.setZip(holdingAddr.getZip());
	        addr.setPrefAddr(holdingAddr.getPrefAddr());
	        party.addAddress(addr);
	    }

	    //Phone
	    list = holdingParty.getParty().getPhone();
	    for (int i = 0; i < list.size(); i++) {
	        Phone holdingPhone = (Phone) list.get(i);
	        Phone phone = new Phone();
	        nbaOLifEId.setId(phone);
	        phone.setPhoneTypeCode(holdingPhone.getPhoneTypeCode());
	        phone.setAreaCode(holdingPhone.getAreaCode());
	        phone.setDialNumber(holdingPhone.getDialNumber());
	        phone.setExt(holdingPhone.getExt());
			phone.setPrefPhone(holdingPhone.getPrefPhone()); 
			phone.setCountryCode(holdingPhone.getCountryCode()); 
			
	        party.addPhone(phone);
	    }

	    //Client
	    if (holdingParty.getParty().hasClient()) {
	        Client client = new Client();
	        if (holdingParty.getParty().hasClient() && holdingParty.getParty().getClient().hasPrefLanguage()) {
	        	client.setPrefLanguage(holdingParty.getParty().getClient().getPrefLanguage());
	        } else {
	        	client.setPrefLanguage(NbaOliConstants.OLI_LANG_ENGLISH);
	        }
	        olifeExt = new OLifEExtension();
	        client.addOLifEExtension(olifeExt);
	        ClientExtension clientExt = new ClientExtension();
	        olifeExt.setClientExtension(clientExt);
			if (holdingParty.getParty().getClient().getOLifEExtensionCount() > 0
				&& holdingParty.getParty().getClient().getOLifEExtensionAt(0).getClientExtension() != null) {
				clientExt.setEmployerName(holdingParty.getParty().getClient().getOLifEExtensionAt(0).getClientExtension().getEmployerName());
			}
	        party.setClient(client);
	    }

	    //Organization
	    party = new Party();
	    nbaOLifEId.setId(party);    
	    party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
	    perOrg = new PersonOrOrganization();
	    party.setPersonOrOrganization(perOrg);
	    Organization org = new Organization();
	    perOrg.setOrganization(org);
	    try {
	    	Company co = NbaConfiguration.getInstance().getProviderOrganizationKeyCompany(lob.getReqVendor(), lob.getCompany());
		    org.setOrganizationKey(co.getOrganizationKey());
		    org.setOrgForm(NbaOliConstants.OLI_ORG_CORPORATION);
	    	org.setOrgCode(co.getOrgCode());
	    	org.setDTCCMemberCode(NbaOliConstants.AXA_VENDOR_CODE);
		} catch (NbaBaseException nbe) {
			org.setOrganizationKey("Not found"); 
			org.setOrgCode("Not found"); 
		}
	    olife.addParty(party);
		reqInfo.setRequesterPartyID(party.getId());

	    //Doctor
		String doctorPartyId = null;
	    RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(thisRequirementInfo);
	    if( null != requirementInfoExt && requirementInfoExt.hasPhysicianPartyID()) {
		    Party doctor = (nbaTxLife.getParty(requirementInfoExt.getPhysicianPartyID())).getParty();
		    if ( null != doctor ) {
		        party = doctor.clone(false);
		        party.setId(null);
		    	nbaOLifEId.setId(party);           
				doctorPartyId = party.getId();
				//ALII1456 start
				//Doctor Address
			    Address docAddr = null;
			    for (int i = 0; i < party.getAddressCount(); i++) {
			    	docAddr = party.getAddressAt(i);
			    	docAddr.setId(null);
			    	nbaOLifEId.setId(docAddr);         
			        if (!docAddr.hasAddressState() && docAddr.hasAddressStateTC()) {
			        	docAddr.setAddressState(getStateCodeAbbreviation(docAddr.getAddressStateTC()));
			        }
			    }
				//Doctor Phone
			    Phone docPhone = null;
			    for (int i = 0; i < party.getPhoneCount(); i++) {
			    	docPhone = party.getPhoneAt(i);
			    	docPhone.setId(null);
			    	nbaOLifEId.setId(docPhone);         
			    }
			    //ALII1456 ends.				
		        olife.addParty(party);
		    }
		}
	    String agentPartyID = null;
	    String agencyPartyID = null;
	    String servicingAgentPartyID = null;
	    String fulfillerPartyId = null;
	    CarrierAppointment agentCarrier = null;
	    if (nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_PRIMAGENT) != null && nbaTxLife.getWritingAgent() != null ) {
	        party = nbaTxLife.getWritingAgent().getParty().clone(false);
	        party.setId(null);
	        nbaOLifEId.setId(party);
			if (party.hasProducer() && party.getProducer().getCarrierAppointmentCount() > 0) {
				agentCarrier = party.getProducer().getCarrierAppointmentAt(0);
				agentCarrier.setId(null);
				nbaOLifEId.setId(agentCarrier);
			}

			//Agent Address
		    Address agentAddr = null;
		    for (int i = 0; i < party.getAddressCount(); i++) {
		        agentAddr = party.getAddressAt(i);
		        agentAddr.setId(null);
		    	nbaOLifEId.setId(agentAddr);         
		        if (!agentAddr.hasAddressState() && agentAddr.hasAddressStateTC()) {
		        	agentAddr.setAddressState(getStateCodeAbbreviation(agentAddr.getAddressStateTC()));
		        }
		    }

			//Agent Phone
		    Phone agentPhone = null;
		    for (int i = 0; i < party.getPhoneCount(); i++) {
		        agentPhone = party.getPhoneAt(i);
		        agentPhone.setId(null);
		    	nbaOLifEId.setId(agentPhone);         
		    }

		    //ALII1456 start
		    //Agent EMailAddress
		    EMailAddress agentEMail = null;
		    for (int i = 0; i < party.getEMailAddressCount(); i++) {
		    	agentEMail = party.getEMailAddressAt(i);
		    	agentEMail.setId(null);
		    	nbaOLifEId.setId(agentEMail);         
		    }
		    //ALII1456 end
		    
		    agentPartyID = party.getId();
	        olife.addParty(party);
	    }

	    if (agentPartyID != null) {
	    	// Create the agency party using the ASU code of the agent
			if (party != null && party.hasProducer() &&	agentCarrier != null) {
				CarrierAppointmentExtension carrierApptExt = NbaUtils.getFirstCarrierAppointmentExtension(agentCarrier);
				if (carrierApptExt != null && carrierApptExt.hasASUCode()) {
					CarrierAppointment agencyCarrier = new CarrierAppointment();
					nbaOLifEId.setId(agencyCarrier);
					agencyCarrier.setCompanyProducerID(carrierApptExt.getASUCode());
					party = new Party();
					nbaOLifEId.setId(party);    
					party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
					perOrg = new PersonOrOrganization();
					party.setPersonOrOrganization(perOrg);
					org = new Organization();
					perOrg.setOrganization(org);
					Producer producer = new Producer();
					producer.addCarrierAppointment(agencyCarrier);
					party.setProducer(producer);
			        agencyPartyID = party.getId();
			        olife.addParty(party);
				}
			}
	    }
	    
		// add fulfiller party Id
		party = new Party();
		nbaOLifEId.setId(party);    
		party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		org = new Organization();
		perOrg.setOrganization(org);
		org.setOrganizationKey(providerName); 
	    org.setOrgForm(NbaOliConstants.OLI_ORG_CORPORATION);
		org.setOrgCode(getProviderOrgCode(reqItem, reqInfo, userVO)); 
    	org.setDTCCMemberCode(vendorName.getVendorCode());
		olife.addParty(party);
		fulfillerPartyId = party.getId();
		reqInfo.setFulfillerPartyID(fulfillerPartyId);

		// add requestor contact party Id
	    Party requestorParty = new Party();
	    nbaOLifEId.setId(requestorParty);
		requestorParty.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_INDIVIDUAL);
	    requestorParty.setIDReferenceType(NbaOliConstants.OLI_IDREFTYPE_CONTACTUSERID);
	    requestorParty.setIDReferenceNo(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getNBContactName());
		perOrg = new PersonOrOrganization();
		requestorParty.setPersonOrOrganization(perOrg);
		perOrg.setPerson(new Person());
	    olife.addParty(requestorParty);
		reqInfo.setRequestorContactPartyID(requestorParty.getId());

	    if (nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_SERVAGENT) != null) { 
	    	NbaParty holdingParty_1 = null;
	    	Relation partyRel_1 = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_SERVAGENT, Integer.parseInt("01"));
		    if(partyRel_1 != null){
			    holdingParty_1 = nbaTxLife.getParty(partyRel_1.getRelatedObjectID());
		    }
		    if(holdingParty_1 != null){
			    party = holdingParty_1.getParty().clone(false);
			    party.setId(null);
			    nbaOLifEId.setId(party); 
	    	    servicingAgentPartyID = party.getId();
		        olife.addParty(party);
		    }
	    }

	    olife.addRelation(
	        createRelation(txLife,
	            NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	            olife.getPartyAt(0).getId(),
	            NbaOliConstants.OLI_HOLDING,
	            NbaOliConstants.OLI_PARTY,
	            reqItem.getNbaLob().getReqPersonCode()));

	    olife.addRelation(
	        createRelation(txLife,
	            NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	            olife.getPartyAt(1).getId(),
	            NbaOliConstants.OLI_HOLDING,
	            NbaOliConstants.OLI_PARTY,
	            NbaOliConstants.OLI_REL_REQUESTOR));

	    olife.addRelation(
	        createRelation(txLife,
	            NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getRequirementInfoAt(0).getId(),
	            olife.getPartyAt(0).getId(),
	            NbaOliConstants.OLI_REQUIREMENTINFO,
	            NbaOliConstants.OLI_PARTY,
	            NbaOliConstants.OLI_REL_FORMFOR));

	    if (null != doctorPartyId ) {
	        olife.addRelation(
	                createRelation(txLife,
	                    NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	    				doctorPartyId,
	                    NbaOliConstants.OLI_HOLDING,
	                    NbaOliConstants.OLI_PARTY,
	                    NbaOliConstants.OLI_REL_PHYSICIAN));
	        }

	        if (agentPartyID != null ) {
	            olife.addRelation(
	                createRelation(txLife,
	                    NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	                    agentPartyID,
	                    NbaOliConstants.OLI_HOLDING,
	                    NbaOliConstants.OLI_PARTY,
	                    NbaOliConstants.OLI_REL_PRIMAGENT));
	        }

	        if (agencyPartyID != null ) {
	            olife.addRelation(
	                createRelation(txLife, 
	                    NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	                    agencyPartyID,
	                    NbaOliConstants.OLI_HOLDING,
	                    NbaOliConstants.OLI_PARTY,
	                    NbaOliConstants.OLI_REL_SERVAGENCY));
	        }
	        if (servicingAgentPartyID != null ) {
	            olife.addRelation(
	                createRelation(txLife, 
	                    NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	                    servicingAgentPartyID,
	                    NbaOliConstants.OLI_HOLDING,
	                    NbaOliConstants.OLI_PARTY,
	                    NbaOliConstants.OLI_REL_SERVAGENT)); 
	        }
	    	if (fulfillerPartyId != null ) {
	    		olife.addRelation(
	    			createRelation(txLife,
	    				NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	    				fulfillerPartyId,
	    				NbaOliConstants.OLI_HOLDING,
	    				NbaOliConstants.OLI_PARTY,
	    				NbaOliConstants.OLI_REL_FULFILLS)); 
	    	}

	        olife.addRelation(
	                createRelation(txLife,
	                    NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	                    requestorParty.getId(),
	                    NbaOliConstants.OLI_HOLDING,
	                    NbaOliConstants.OLI_PARTY,
	                    NbaOliConstants.OLI_REL_1009800010));
	        
	        //start ALII1623
	        if(NbaUtils.existRelationWithRoleCode(nbaTxLife, NbaOliConstants.OLI_REL_UNDERWRITER)){
	        	Relation relObj = NbaUtils.getRelation(nbaTxLife.getOLifE(), NbaOliConstants.OLI_REL_UNDERWRITER, nbaTxLife.getPrimaryHolding().getId());
	    		if(relObj != null){
	    			NbaParty partyObj = nbaTxLife.getParty(relObj.getRelatedObjectID());
	    			party = new Party();
		    		nbaOLifEId.setId(party);    
		    		party.setPersonOrOrganization(new PersonOrOrganization());
		    		party.setPartyTypeCode(NbaOliConstants.OLI_PT_PERSON);
		    		party.setPartyKey(partyObj.getParty().getPartyKey());
		    		person = new Person();
		    		person.setFirstName(partyObj.getFirstName());
		    		person.setLastName(partyObj.getLastName());
		    		person.setActionAdd();
		    		party.getPersonOrOrganization().setPerson(person);
		    		party.setFullName(partyObj.getFullName());
		    		List phoneObjList = partyObj.getParty().getPhone();
		    		if(phoneObjList != null && phoneObjList.size() > 0){
		    			Phone phoneObj = (Phone) phoneObjList.get(0);
		    			if(phoneObj != null) {
				    		Phone phone = new Phone();
				    		phone.setPhoneTypeCode(phoneObj.getPhoneTypeCode());
				    		phone.setAreaCode(phoneObj.getAreaCode());
				    		phone.setDialNumber(phoneObj.getDialNumber());
				    		phone.setActionAdd();
				    		nbaOLifEId.setId(phone);
				    		ArrayList newPhoneList = new ArrayList();
				    		newPhoneList.add(phone);
				    		party.setPhone(newPhoneList);
		    			}
		    		}
		    		List emailObjList = partyObj.getParty().getEMailAddress();
		    		if(emailObjList != null && emailObjList.size() > 0){
		    			EMailAddress emailObj = (EMailAddress) emailObjList.get(0);
		    			if(emailObj != null) {
				    		EMailAddress email = new EMailAddress();
				    		email.setAddrLine(emailObj.getAddrLine());
				    		email.setActionAdd();
				    		nbaOLifEId.setId(email);
				    		ArrayList newEmailList = new ArrayList();
				    		newEmailList.add(email);
				    		party.setEMailAddress(newEmailList);
		    			}
		    		}
		    		olife.addParty(party);
		    		olife.addRelation(
	    	                createRelation(txLife,
	    	                    NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	    	                    party.getId(),
	    	                    NbaOliConstants.OLI_HOLDING,
	    	                    NbaOliConstants.OLI_PARTY,
	    	                    NbaOliConstants.OLI_REL_UNDERWRITER));
	    		}
	    	}
	        if(NbaUtils.existRelationWithRoleCode(nbaTxLife, NbaOliConstants.OLI_REL_CASEMNGBY)){
	        	Relation relObj = NbaUtils.getRelation(nbaTxLife.getOLifE(), NbaOliConstants.OLI_REL_CASEMNGBY, nbaTxLife.getPrimaryHolding().getId());
	    		if(relObj != null){
	    			NbaParty partyObj = nbaTxLife.getParty(relObj.getRelatedObjectID());
	    			party = new Party();
		    		nbaOLifEId.setId(party);    
		    		party.setPersonOrOrganization(new PersonOrOrganization());
		    		party.setPartyTypeCode(NbaOliConstants.OLI_PT_PERSON);
		    		party.setPartyKey(partyObj.getParty().getPartyKey());
		    		person = new Person();
		    		person.setFirstName(partyObj.getFirstName());
		    		person.setLastName(partyObj.getLastName());
		    		person.setActionAdd();
		    		party.getPersonOrOrganization().setPerson(person);
		    		party.setFullName(partyObj.getFullName());
		    		List phoneObjList = partyObj.getParty().getPhone();
		    		if(phoneObjList != null && phoneObjList.size() > 0){
		    			Phone phoneObj = (Phone) phoneObjList.get(0);
		    			if(phoneObj != null) {
				    		Phone phone = new Phone();
				    		phone.setPhoneTypeCode(phoneObj.getPhoneTypeCode());
				    		phone.setAreaCode(phoneObj.getAreaCode());
				    		phone.setDialNumber(phoneObj.getDialNumber());
				    		phone.setActionAdd();
				    		nbaOLifEId.setId(phone);
				    		ArrayList newPhoneList = new ArrayList();
				    		newPhoneList.add(phone);
				    		party.setPhone(newPhoneList);
		    			}
		    		}
		    		List emailObjList = partyObj.getParty().getEMailAddress();
		    		if(emailObjList != null && emailObjList.size() > 0){
		    			EMailAddress emailObj = (EMailAddress) emailObjList.get(0);
		    			if(emailObj != null) {
				    		EMailAddress email = new EMailAddress();
				    		email.setAddrLine(emailObj.getAddrLine());
				    		email.setActionAdd();
				    		nbaOLifEId.setId(email);
				    		ArrayList newEmailList = new ArrayList();
				    		newEmailList.add(email);
				    		party.setEMailAddress(newEmailList);
		    			}
		    		}
		    		olife.addParty(party);
		    		olife.addRelation(
	    	                createRelation(txLife,
	    	                    NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
	    	                    party.getId(),
	    	                    NbaOliConstants.OLI_HOLDING,
	    	                    NbaOliConstants.OLI_PARTY,
	    	                    NbaOliConstants.OLI_REL_CASEMNGBY));
	    		}
	        }
	        //end ALII1623
	        int reqPerSeq = reqItem.getNbaLob().getReqPersonSeq();//APSL3205
	    	for(int z = 0; z < olife.getRelationCount(); z++ ){
	    		Relation arel = olife.getRelationAt(z);
	    		arel.setRelatedRefIDType(reqPerSeq);//APSL3205
	    	}
	    	thisRequirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_SUBMITTED);
	    	thisRequirementInfo.setRequestedDate(new Date());
	    	thisRequirementInfo.setActionUpdate();
	        return txLife;
	}
	protected String getProviderID(NbaDst reqItem, RequirementInfo requirementInfo, NbaUserVO userVO) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null;
	    NbaOinkDataAccess oinkData = new NbaOinkDataAccess(reqItem.getNbaLob());
		try {
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS);
			HashMap deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(userVO));
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_PROVIDER_FOR_REQUEST);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(requirementInfo.getId());
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
		    String providerID = (String) data.getResultsData().get(0);
			return providerID;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Problem in getting data from VPMS", re);
	    } finally {
	        try {
	            if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
	        } catch (RemoteException re) {
	            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	        }
	    }

	}
	protected String getProviderOrgCode(NbaDst reqItem, RequirementInfo requirementInfo, NbaUserVO userVO) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null;
	    NbaOinkDataAccess oinkData = new NbaOinkDataAccess(reqItem.getNbaLob());
		try {
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS);
			HashMap deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(userVO));
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_PROVIDER_ORGANIZATION);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(requirementInfo.getId());
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
		    String providerOrg = (String) data.getResultsData().get(0);
			return providerOrg;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Problem in getting data from VPMS", re);
	    } finally {
	        try {
	            if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
	        } catch (RemoteException re) {
	            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	        }
	    }

	}
	protected String getRequirementAccountNum(NbaDst reqItem, NbaTXLife txLife, RequirementInfo requirementInfo, NbaUserVO userVO) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null;
	    NbaOinkDataAccess oinkData = new NbaOinkDataAccess(txLife);
	    oinkData.setLobSource(reqItem.getNbaLob());
		try {
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS);
			HashMap deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(userVO));
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_REQUIREMENT_ACCOUNT);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(requirementInfo.getId());
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
		    String reqAcctNum = (String) data.getResultsData().get(0);
			return reqAcctNum;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Problem in getting data from VPMS", re);
	    } finally {
	        try {
	            if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
	        } catch (RemoteException re) {
	            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	        }
	    }

	}
	protected boolean getTestIndicator(String vendorCode) {
		try {
			return NbaConfiguration.getInstance().getProvider(vendorCode).getTestIndicator();
		} catch (NbaBaseException e) {
			e.printStackTrace();
		}
		return true;
	}
	/**
	* Answer relation object that matched relation type,person code 
	* and person sequence number for a XMLife.
	* @param life - the XMLife object
	* @param relType - the relation object type
	* @param personCode - the person relation role code
	* @param personSeq - the person sequence number
	* @return the relation object
	*/
	// AXAL3.7.31 New Method
	protected Relation getRelation(NbaTXLife life, long relType, long personCode, long personSeq) {
		List list = life.getOLifE().getRelation();
		Relation rel = null;
		for (int i = 0; i < list.size(); i++) {
			rel = (Relation) list.get(i);
			if (rel.getRelatedObjectType() == relType
				&& rel.getRelationRoleCode() == personCode) {
					if(rel.getRelatedRefID() == null || Long.parseLong(rel.getRelatedRefID()) == personSeq) {
						return rel;
					}
			}
		}
		return null;
	}
	/**
	* Creates the relation object..
	* 
	* @return  - the created relation object
	* @param relCount - the count of relation on the olife.
	* @param orgObjId - the originating object id
	* @param relObjId - the relation object id
	* @param orgType - the originating object type
	* @param relType - the relation object type
	* @param roleCode - the relation role code
	*/
	// AXAL3.7.31 New Method
	protected Relation createRelation(NbaTXLife txlife, String orgObjId, String relObjId, long orgType, long relType, long roleCode)	throws NbaBaseException {
		if(txlife == null) {
			throw new NbaBaseException("Unable to create Relation object");
		}
		NbaOLifEId nbaOLifEId = new NbaOLifEId(txlife);
		Relation rel = new Relation();
		nbaOLifEId.setId(rel);
		rel.setOriginatingObjectID(orgObjId);
		rel.setRelatedObjectID(relObjId);
		rel.setOriginatingObjectType(orgType);
		rel.setRelatedObjectType(relType);
		rel.setRelationRoleCode(roleCode);
		return rel;
	}

	/**
	 * Create Party object from old txLife to new Txlife 
	 * @param partyId
	 * @param txlife
	 * @param txlife502
	 * @return
	 */
	public Party createParty(String partyId,NbaTXLife txlife, NbaTXLife txlife502){
		Party newParty = new Party();
		
		NbaParty nbaParty = txlife.getParty(partyId);
		if(nbaParty!=null){
			Party origParty = nbaParty.getParty();
			newParty.setPartyTypeCode(origParty.getPartyTypeCode());
			newParty.setId(origParty.getId());
			newParty.setCompanyKey(origParty.getCompanyKey());
			newParty.setFullName(origParty.getFullName());
			newParty.setGovtID(origParty.getGovtID());
			PersonOrOrganization perOrg = new PersonOrOrganization();
			if(origParty.getPersonOrOrganization().isPerson()) {
			   perOrg.setPerson(origParty.getPersonOrOrganization().getPerson().clone(false));
			}else {
			   perOrg.setOrganization(origParty.getPersonOrOrganization().getOrganization().clone(false));
			}
			newParty.setPersonOrOrganization(perOrg);			
			
			for(int i=0; i<origParty.getAddressCount();i++){
			   Address origAddr = origParty.getAddressAt(i);
			   newParty.addAddress(origAddr.clone(false));
			}
			
			for(int i=0; i<origParty.getPhoneCount();i++){
				Phone origPhone = origParty.getPhoneAt(i);
				newParty.addPhone(origPhone.clone(false));
			}
			newParty.setClient(origParty.getClient()!=null ? origParty.getClient().clone(false) : null);
			newParty.setProducer(origParty.getProducer()!=null ? origParty.getProducer().clone(false) : null);
			for(int i=0; i<origParty.getEmploymentCount();i++){
				Employment employment = origParty.getEmploymentAt(i);
				newParty.addEmployment(employment.clone(false));
			}
		}
		return newParty;
	}
	// AXAL3.7.31 New Method
	protected String getStateCodeAbbreviation(long stateCode) throws NbaBaseException {
		Map aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		NbaStatesData[] table = (NbaStatesData[]) tableAccessor.getDisplayData(aCase, NbaTableConstants.NBA_STATES);
		if (table != null) {
			for (int i = 0; i < table.length; i++) {
				if (table[i].getStateCode() == stateCode) {
					return table[i].getStateCodeTrans();
				}
			}
		}
		return null;
	}
}
