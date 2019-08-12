package com.csc.fsg.nba.reinsuranceadapter;

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
 */
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.reinsurance.rgaschema.Applicant;
import com.csc.fsg.nba.reinsurance.rgaschema.Applicants;
import com.csc.fsg.nba.reinsurance.rgaschema.Benefit;
import com.csc.fsg.nba.reinsurance.rgaschema.Benefits;
import com.csc.fsg.nba.reinsurance.rgaschema.Case;
import com.csc.fsg.nba.reinsurance.rgaschema.Cases;
import com.csc.fsg.nba.reinsurance.rgaschema.CedingCompany;
import com.csc.fsg.nba.reinsurance.rgaschema.CedingUWContact;
import com.csc.fsg.nba.reinsurance.rgaschema.Document;
import com.csc.fsg.nba.reinsurance.rgaschema.Documents;
import com.csc.fsg.nba.reinsurance.rgaschema.NbaRgaRequest;
import com.csc.fsg.nba.reinsurance.rgaschema.ReinsuranceCases;
import com.csc.fsg.nba.reinsurance.rgaschema.Reinsurer;
import com.csc.fsg.nba.reinsurance.rgaschema.Request;
import com.csc.fsg.nba.reinsurance.rgaschema.Requests;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Contact;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * NbaReinsuranceAdapter is the class for connecting to a reinsurer for the purpose
 * of ordering bids and receiving offer from reinsurer vendors.
 * <p>This class implements the methods in "NbaReinsuranceAdapter" to send, receive 
 * and process messages to/from reinsurer.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.32</td><td>Axa Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaReinsuranceAdapter extends NbaReinsuranceAdapter{
	public static String TRANSACTION = "TRANSACTION";
	protected NbaTableAccessor ntsAccess = null;//AXAL3.7.32
	protected String reinVendorID = null;//AXAL3.7.32
/**
 * This method converts the XML 552 transactions into a format
 * that is understandable by the reinsurer.
 * @param txLife the 552 XML transaction
 * @param user the user value object
 * @return a reinsurer ready message in a HashMap which includes any errors that might have occurred.
 * @exception NbaBaseException thrown if an error occurs.
 */
	public java.util.Map convertXmlToReinsurerFormat(NbaTXLife txLife, NbaUserVO user, NbaDst work)throws NbaBaseException{
		Map map = new HashMap();
		setReinVendorID(work.getNbaLob().getReinVendorID());
		if(validateRequest(txLife)){
			String request = formatRequest(txLife, work);
			map.put(TRANSACTION,request);
		}		
		return map;
	}
	
	/**
	 * Validates 552 request for required fields.
	 * @param txLife the 552 XML transaction
	 * @return true if txlife has all required fields
	 */
	public boolean validateRequest(NbaTXLife txLife){
		return true;
	}

	/**
	 * Format the 552 request in RGA specific format
	 * @param txLife the 552 XML transaction
	 * @return RGA ready message
	 */
	public String formatRequest(NbaTXLife txLife, NbaDst work) throws NbaBaseException {
		NbaRgaRequest rgaRequest = new NbaRgaRequest();
		ReinsuranceCases reinsuranceCases = rgaRequest.getReinsuranceCases();
		populateReinsuranceCases(reinsuranceCases, txLife, work);
		return rgaRequest.toXmlString();
	}
	
	//AXAL3.7.32 New Method
	public void populateReinsuranceCases(ReinsuranceCases reinsuranceCases, NbaTXLife txLife, NbaDst work) throws NbaBaseException {
		com.csc.fsg.nba.vo.configuration.Reinsurer configReinsurer = NbaConfiguration.getInstance().getReinsurer(reinVendorID);
		Cases cases = new Cases();
		reinsuranceCases.setCases(cases);
		Case rgaCase = new Case();
		cases.addCase(rgaCase);
		Policy policy = txLife.getPolicy();
		rgaCase.setCaseID(policy.getPolNumber());
		rgaCase.setApplicationID(policy.getPolNumber());
		//NBLXA-2017 Begin
		List lifeParticipantList = txLife.getPrimaryCoverage().getLifeParticipant();
		if (reinVendorID.equals(NbaConstants.REINSURER_GENRE) && isJointLife(lifeParticipantList)) {
			rgaCase.setJointCase("Y");
		}
		//NBLXA-2017 End
		TXLifeRequest txRequest = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
		rgaCase.setDateEntered(NbaUtils.getStringInUSFormatFromDate(txRequest.getTransExeDate()));
		
		CedingCompany cedingInfo = getCedingCompanyInfo(configReinsurer);
		CedingUWContact uwContact = new CedingUWContact();
		uwContact.setCedingContactID(work.getNbaLob().getUndwrtQueue());
		cedingInfo.setCedingUWContact(uwContact);
		rgaCase.setCedingCompany(cedingInfo);
		//AXAL3.7.32 Code Deleted
		List applicantList = getApplicantList(txLife);
		if (!applicantList.isEmpty()) {
			rgaCase.setApplicants(new Applicants());
			rgaCase.getApplicants().setApplicant((ArrayList) applicantList);
		}
		if ((txLife.getPrimaryCoverage() != null) && txLife.getPrimaryCoverage().hasCurrentAmt()) { //ALII1024
			rgaCase.setFaceAmount(new BigDecimal(txLife.getPrimaryCoverage().getCurrentAmt()));	
		}
		Holding holding = txLife.getPrimaryHolding();
		Documents documents = null;
		int count = 0;
		for (int i = 0; i < holding.getAttachmentCount(); i++) {
			Attachment attachment = holding.getAttachmentAt(i);
			if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_DOC
					&& attachment.getDescription() != null) {//AXAL3.7.32
				count++;
				if (documents == null) {
					documents = new Documents();
					rgaCase.setDocuments(documents);
				}
				Document document = new Document();
				documents.addDocument(document);
				document.setID(String.valueOf(count));
				document.setFilename(attachment.getDescription());	
			} else if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_COMMENT) {
				rgaCase.setComment(attachment.getDescription());
			}
		}
		if(count > 0) {
			documents.setCount(String.valueOf(count));
		}
		Requests requests = new Requests();
		rgaCase.setRequests(requests);
		Request request = new Request();
		requests.addRequest(request);
		request.setID("1");
		if((txLife.getPrimaryCoverage() != null) && txLife.getPrimaryCoverage().getReinsuranceInfoAt(0) != null) { //ALII1024
			request.setReinsurerID(txLife.getPrimaryCoverage().getReinsuranceInfoAt(0).getReinsurersTreatyIdent());	//AXAL3.7.32
		}
		NbaTime time = txRequest.getTransExeTime();
		StringBuffer buffer = new StringBuffer();
		buffer.append(NbaUtils.getStringInUSFormatFromDate(txRequest.getTransExeDate()));
		buffer.append(" ");
		buffer.append(time.formatXMLString(time.getTimeZone()));
		request.setRequestDate(buffer.toString());
		Contact configContactInfo = NbaConfiguration.getInstance().getReinsurerContact(reinVendorID,
				NbaConfigurationConstants.CONTACT_TYPE_REINSURER); //ACN012
		Reinsurer reinsurer = new Reinsurer();
		request.setReinsurer(reinsurer);
		reinsurer.setReinsurerName(configContactInfo.getName());
		//Code Deleted AXAL3.7.32
	}
	
	//AXAL3.7.32 New Method
	public List getApplicantList(NbaTXLife txLife) throws NbaBaseException {
		String backend = txLife.getBackendSystem();
		Policy policy = txLife.getPolicy();
		List applicantList = new ArrayList();
		List parties = txLife.getOLifE().getParty();
		if (parties.size() > 0 && txLife.getLife() != null) {//SPR2821
			List coverages = txLife.getLife().getCoverage();
			for (int i = 0; i < parties.size(); i++) {
				// SPR3290 code deleted
				Applicant applicant = null;
				Benefits benefits = null;
				Party party = (Party) parties.get(i);
				for (int j = 0; j < coverages.size(); j++) {
					Coverage coverage = (Coverage) coverages.get(j);
					LifeParticipant lifeParticipant = getLifeParticipant(coverage, party.getId());
					if (lifeParticipant != null) {
						if (applicant == null) {
							applicant = new Applicant();
							benefits = new Benefits();
							applicant.setBenefits(benefits);
							applicant.setID(String.valueOf(i + 1));
							populateApplicant(applicant, party, backend);
							applicantList.add(applicant);
						}
						applicant.setPrimary(isPrimaryLifeParticipant(lifeParticipant.getLifeParticipantRoleCode()) ? "Y" : "N");
						benefits.setBenefit((ArrayList) createBenefits(coverage, policy));
						break;
					}
				}
			}
		}
		return applicantList;
	}
	
	//AXAL3.7.32 New Method
	public List createBenefits(Coverage coverage, Policy policy) {
		List benefitList = new ArrayList();
		String productType = String.valueOf(policy.getProductType());
		double reinsuranceAmt = coverage.getReinsuranceInfoAt(0).getReinsuredAmt();
		if (coverage != null) {
			//Create Benefit tag for Coverage
			Benefit benefit = new Benefit();
			benefit.setID(coverage.getId());
			benefit.setBenefitCode(coverage.getProductCode());
			if (coverage.getReinsuranceInfoCount() > 0) {
				benefit.setReinsuranceAmt(new BigDecimal(reinsuranceAmt));
			}
			benefit.setCurrencyCode("");
			benefit.setBenefitRefID(productType);
			benefitList.add(benefit);
			//Create Benefit tag for all CovOptions under Coverage
			for (int i = 0; i < coverage.getCovOptionCount(); i++) {
				CovOption covOption = coverage.getCovOptionAt(i);
				Benefit covOptionBenefit = new Benefit();
				covOptionBenefit.setID(covOption.getId());
				covOptionBenefit.setBenefitCode(covOption.getProductCode());
				if (coverage.getReinsuranceInfoCount() > 0) {
					covOptionBenefit.setReinsuranceAmt(new BigDecimal(reinsuranceAmt));
				}
				covOptionBenefit.setCurrencyCode("");
				covOptionBenefit.setBenefitRefID(productType);
				benefitList.add(covOptionBenefit);
			}
		}
		return benefitList;
	}
	
	//AXAL3.7.32 New Method
	public LifeParticipant getLifeParticipant(Coverage coverage, String partyID){
		if(coverage != null && partyID != null) {
			List lifeParticipants = coverage.getLifeParticipant();
			for (int k = 0; k < lifeParticipants.size(); k++) {
				LifeParticipant lifeParticipant = (LifeParticipant) lifeParticipants.get(k);
				if (partyID.equalsIgnoreCase(lifeParticipant.getPartyID())) {
					return lifeParticipant;
				}
			}
		}
		return null;
	}
	
	//AXAL3.7.32 New Method
	public boolean isPrimaryLifeParticipant(long roleCode){
		if (roleCode == NbaOliConstants.OLI_PARTICROLE_PRIMARY) {
			return true;
		}
		return false;
	}
	
	//AXAL3.7.32 New Method
	public void populateApplicant(Applicant applicant, Party party, String backend) throws NbaBaseException{
		if (party.getPersonOrOrganization() != null && party.getPersonOrOrganization().isPerson()) {
			Person person = party.getPersonOrOrganization().getPerson();
			applicant.setFirstName(person.getFirstName());
			applicant.setLastName(person.getLastName());
			applicant.setMiddleName(person.getMiddleName());
			applicant.setDOB(NbaUtils.getStringInUSFormatFromDate(person.getBirthDate()));
			applicant.setGender(String.valueOf(person.getGender()));
			applicant.setNationalIdentifier(party.getGovtID());
			applicant.setBirthStateCode(translateRGAState(backend, String.valueOf(person.getBirthJurisdictionTC())));
			applicant.setBirthCountryCode(String.valueOf(person.getBirthCountry()));
			applicant.setResidenceStateCode(translateRGAState(backend, String.valueOf(party.getResidenceState())));
			applicant.setResidenceCountryCode(String.valueOf(party.getResidenceCountry()));
			applicant.setOccupation(person.getOccupation());
			applicant.setSmoker(String.valueOf(person.getSmokerStat()));
		}
	}
	
	/**
	 * Find RGA State for an olife value
	 * @param backend the backend id
	 * @param olifeValue Olife value to translate
	 * @return Matched State Data
	 */
	protected String translateRGAState(String backend,String olifeValue) throws NbaBaseException {
		NbaStatesData[] table = (NbaStatesData[])getTableAccessor().getDisplayData(getDefaultTableMap(backend),NbaTableConstants.NBA_STATES);
		if (table != null) {
			if (olifeValue != null && olifeValue.length() > 0) {
				for (int i = 0; i < table.length; i++) {
					if (String.valueOf(table[i].getStateCode()).compareToIgnoreCase(olifeValue) == 0) {//SPR1346
						return table[i].getCybAlphaStateCodeTrans();
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Answers the default table map for a bakcend
	 * @param backend the backend id
	 * @return the default table map
	 */
	protected Map getDefaultTableMap(String backend) {
		Map aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, backend);
		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");
		aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");
		return aCase;
	}
	
	/**
	 * Answer the table accessor object. It creates and stores new object when call first time 
	 * @return an instance of NbaTableAccessor
	 */
	protected NbaTableAccessor getTableAccessor(){
		if (ntsAccess == null) {
			ntsAccess = new NbaTableAccessor();
		}
		return ntsAccess;
	}
	
	/**
	 * Find the Table Data for an olife value
	 * @param backend the backend id
	 * @param olifeValue Olife value to translate
	 * @param tableName the UCT table name
	 * @return Matched Table Data
	 */
	protected NbaUctData getTableRow(String backend,String olifeValue, String tableName) throws NbaBaseException {
		NbaUctData[] table = (NbaUctData[])getTableAccessor().getDisplayData(getDefaultTableMap(backend),tableName);
		if (table != null) {
			if (olifeValue != null && olifeValue.length() > 0) {
				for (int i = 0; i < table.length; i++) {
					if (table[i].getIndexValue().compareToIgnoreCase(olifeValue) == 0) {
						return table[i];
					}
				}
			}
		}
		return null;
	}
	
	//AXAL3.7.32 New Method
	public CedingCompany getCedingCompanyInfo(com.csc.fsg.nba.vo.configuration.Reinsurer configReinsurer){
		com.csc.fsg.nba.vo.configuration.CedingCompany configCedingInfo = configReinsurer.getCedingCompany(); //ACN012
		CedingCompany cedingInfo = new CedingCompany();
		cedingInfo.setID(configCedingInfo.getId());
		cedingInfo.setName(configCedingInfo.getName());
		cedingInfo.setIdentity(configCedingInfo.getIdentity());
		return cedingInfo;
	}

	//ACN012 CHANGED SIGNATURE
	public com.csc.fsg.nba.vo.configuration.Reinsurer getReinsurerConfigInfo(String reinsurerID) throws NbaBaseException {
		return NbaConfiguration.getInstance().getReinsurer(reinsurerID);
	}

	/**
	 * This method converts the reinsurer response/offer into XML transaction.It also updates required LOBs and result source with converted XMLife.
	 * @param work the reinsurance work item.
	 * @param user the user value object
	 * @return the requirement work item with formated source.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	public NbaDst processResponseFromReinsurer(NbaDst work, NbaUserVO user) throws NbaBaseException {
		return work;
	}

	/**
	 * This method provides the means by which a message representing a request for a requirement is submitted to the provider.
	 * <p>
	 * The means of communication varies and may include many different methods: HTTP Post, writing the message to a folder on a server, or others.
	 * The <code>NbaConfigReinsurer</code> that contains information on how to communicate with the reinsurer is passed to the
	 * <code>NbaProviderCommunicator</code> object. The <code>NbaProviderCommunicator</code> then sends the message to the provider.
	 * @param aTarget the destinatin of the message
	 * @param message the message to be sent to the provider
	 * @return an Object that must be evaluated by the calling process
	 */
	//AXAL3.7.32 New Method
	public Object sendMessageToProvider(String aTarget, Object aMessage, NbaUserVO userVO) throws NbaBaseException {
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SUBMIT_REINSURER_REQUEST, userVO,
				null, null, aMessage);
		return webServiceInvoker.execute();
	}

	/**
	 * @return Returns the reinVendorID.
	 */
	//AXAL3.7.32 New Method
	public String getReinVendorID() {
		return reinVendorID;
	}
	/**
	 * @param reinVendorID The reinVendorID to set.
	 */
	//AXAL3.7.32 New Method
	public void setReinVendorID(String reinVendorID) {
		this.reinVendorID = reinVendorID;
	}
	
	//NBLXA-2017 New Method
	public boolean isJointLife(List lifeParticipantList){
		LifeParticipant lifeParticipant = new LifeParticipant();
		boolean jointInd = false;		
		for(int i=0; i < lifeParticipantList.size(); i++){
			lifeParticipant = (LifeParticipant) lifeParticipantList.get(i);
			if(NbaUtils.isJointInsuredParticipant(lifeParticipant)){
				jointInd = true;
				break;
			}
		}
		return jointInd;
	}
}
