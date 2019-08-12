package com.csc.fsg.nba.business.process;

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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapterFacade;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.configuration.Company;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vo.nbaschema.InsurableParty;
import com.csc.fsg.nba.vo.nbaschema.Requirement;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Carrier;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.MIBRequest;
import com.csc.fsg.nba.vo.txlife.MIBServiceDescriptor;
import com.csc.fsg.nba.vo.txlife.MIBServiceDescriptorOrMIBServiceConfigurationID;
import com.csc.fsg.nba.vo.txlife.MIBServiceOptions;
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
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.client.AxaServiceRequestorUtils;

/**
 * NbaProcOrderRequirements is the class that processes nbAccelerator work items found on the AWD order requirement queue (NBORDREQ).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBA001</td>
 * <td>Version 1</td>
 * <td>Initial Development</td>
 * </tr>
 * <tr>
 * <td>NBA008</td>
 * <td>Version 2</td>
 * <td>Requirements Ordering and Receipting</td>
 * </tr>
 * <tr>
 * <td>NBA027</td>
 * <td>Version 3</td>
 * <td>Performance Tuning</td>
 * </tr>
 * <tr>
 * <td>SPR1183</td>
 * <td>Version 3</td>
 * <td>LabOne Adaptor is not creating the Bundle Transaction</td>
 * </tr>
 * <tr>
 * <td>NBA035</td>
 * <td>Version 3</td>
 * <td>App submit to Pending DB</td>
 * </tr>
 * <tr>
 * <td>NBA050</td>
 * <td>Version 3</td>
 * <td>Pending Database</td>
 * </tr>
 * <tr>
 * <td>SPR1135</td>
 * <td>Version 3</td>
 * <td>A default agent id should be provided in the configuration file to send to a provider when an agent is not included on the case</td>
 * <tr>
 * <tr>
 * <td>NBA036</td>
 * <td>Version 3</td>
 * <td>Underwriter Workbench Trx to nbA DB
 * <tr>
 * <td>NBA044</td>
 * <td>Version 3</td>
 * <td>Architecture changes</td>
 * </tr>
 * <tr>
 * <td>NBA1314</td>
 * <td>Version 3</td>
 * <td>Requirement workitem should go to error queue; rather it stops the APORDREQ polling process</td>
 * </tr>
 * <tr>
 * <td>SPR1184</td>
 * <td>Version 3</td>
 * <td>Required Information not present in EMSI MVR format (NAILBA). Requirement 147</td>
 * <tr>
 * <tr>
 * <td>SPR1207</td>
 * <td>Version 3</td>
 * <td>The EMSI format does not contain the required information</td>
 * <tr>
 * <tr>
 * <td>NBA081</td>
 * <td>Version 3</td>
 * <td>Hooper Holmes Ordering and Recipting</td>
 * <tr>
 * <tr>
 * <td>NBA093</td>
 * <td>Version 3</td>
 * <td>Upgrade to ACORD 2.8</td>
 * </tr>
 * <tr>
 * <td>NBA091</td>
 * <td>Version 3</td>
 * <td>Agent Name and Adress</td>
 * </tr>
 * <tr>
 * <td>NBA086</td>
 * <td>Version 2</td>
 * <td>Performance Testing and Tuning</td>
 * </tr>
 * <tr>
 * <td>SPR1770</td>
 * <td>Version 4</td>
 * <td>When requirements are first ordered and moved to the NBORDERD queue they should not be suspended initially.</td>
 * </tr>
 * <tr>
 * <td>SPR1778</td>
 * <td>Version 4</td>
 * <td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td>
 * </tr>
 * <tr>
 * <td>SPR1851</td>
 * <td>Version 4</td>
 * <td>Locking Issues</td>
 * </tr>
 * <tr>
 * <td>SPR1715</td>
 * <td>Version 4</td>
 * <td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td>
 * </tr>
 * <tr>
 * <td>NBA112</td>
 * <td>Version 4</td>
 * <td>Agent Name and Adress</td>
 * </tr>
 * <tr>
 * <td>NBA097</td>
 * <td>Version 4</td>
 * <td>Work Routing Reason Displayed</td>
 * </tr>
 * <tr>
 * <td>ACN012</td>
 * <td>Version 4</td>
 * <td>Architecture Changes</td>
 * </tr>
 * <tr>
 * <td>ACN014</td>
 * <td>Version 4</td>
 * <td>Migration of 121/1122</td>
 * </tr>
 * <tr>
 * <td>ACN009</td>
 * <td>Version 4</td>
 * <td>ACORD 401 / 402 MIB Inquiry and Update Migration</td>
 * </tr>
 * <tr>
 * <td>SPR2380</td>
 * <td>Version 5</td>
 * <td>Cleanup</td>
 * </tr>
 * <tr>
 * <td>SPR1346</td>
 * <td>Version 5</td>
 * <td>Displaying State Drop-down list in Alphabetical order by country/ACORD State Code Change</td>
 * </tr>
 * <tr>
 * <td>SPR2514</td>
 * <td>Version 5</td>
 * <td>CarrierApptTypeCode value is wrongly generated in MIB 401 request</td>
 * </tr>
 * <tr>
 * <td>SPR2639</td>
 * <td>Version 5</td>
 * <td>Automated process status should be based business function</td>
 * </tr>
 * <tr>
 * <td>SPR1311</td>
 * <td>Version 5</td>
 * <td>Manually ordered Requirements</td>
 * </tr>
 * <tr>
 * <td>SPR2975</td>
 * <td>Version 6</td>
 * <td>Ordering Owner Requirement (RQTP 167) causes APORDREQ process to error stop if the Owner is Organization</td>
 * </tr>
 * <tr>
 * <td>SPR2831</td>
 * <td>Version 6</td>
 * <td>Requirement Order '121' XML transaction validation issue.</td>
 * </tr>
 * <tr>
 * <td>SPR3041</td>
 * <td>Version 6</td>
 * <td>Email ID for the producer should be retrieved from the configuration file</td>
 * </tr>
 * <tr>
 * <td>SPR3050</td>
 * <td>Version 6</td>
 * <td>APORDREQ Process Stops with Error "A runtime error occurred; unable to complete processing Index: 2, Size: 2"</td>
 * </tr>
 * <tr>
 * <td>NBA130</td>
 * <td>Version 6</td>
 * <td>Requirements Reinsurance Project</td>
 * </tr>
 * <tr>
 * <td>SPR2992</td>
 * <td>Version 6</td>
 * <td>General Code Clean Up Issues for Version 6</td>
 * </tr>
 * <tr>
 * <td>SPR3160</td>
 * <td>Version 6</td>
 * <td>Requirement Evaluation is expecting Requirement Results attachment to be OLI_LU_BASICATTACHMENTTYP(271) instead of OLI_LU_BASICATTMNTTY_TEXT
 * (1)</td>
 * </tr>
 * <tr>
 * <td>SPR3236</td>
 * <td>Version 6</td>
 * <td>Formatting email message from creating the agent ordered requirement</td>
 * </tr>
 * <tr>
 * <td>SPR3362</td>
 * <td>Version 7</td>
 * <td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td>
 * </tr>
 * <tr>
 * <td>SPR3290</td>
 * <td>Version 7</td>
 * <td>General source code clean up during version 7</td>
 * </tr>
 * <tr>
 * <td>ALPC7</td>
 * <td>Version 7</td>
 * <td>Schema migration from 2.8.90 to 2.9.03</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.31</td>
 * <td>AXA Life Phase 1</td>
 * <td>Provider Interface - MIB</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.40</td>
 * <td>AXA Life Phase 1</td>
 * <td>Contract Validation for Agent Subset</td>
 * </tr>
 * <tr>
 * <td>NBA250</td>
 * <td>AXA Life Phase 1</td>
 * <td>nbA Requirement Form and Producer Email Management Project</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.31</td>
 * <td>AXA Life Phase 1</td>
 * <td>Provider Interfaces</td>
 * </tr>
 * <tr>
 * <td>ALS4914</td>
 * <td>AXA Life Phase 1</td>
 * <td>QC # 4068 - 3.7.31.M.8 MIB not ordered initially with blank birth state, but response received when manually ordered</td>
 * </tr>
 * <tr>
 * <td>NBA420</td>
 * <td>Version NB-1701</td>
 * <td>MIB Data Enrichment Enhancement</td>
 * </tr>
 * <tr>
 * <td>APSL5361</td>
 * <td>Discreationary</td>
 * <td>nbA Migrate MIB XML401_XML402 and XML404 to ACORD 2.34.00</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see insert reference here (optional - delete if not used)
 * @since New Business Accelerator - Version 1
 */
public class NbaProcOrderRequirements extends NbaAutomatedProcess {
	// NBA008 added start
	protected NbaDst parentCase = null;

	protected String transCode = null;

	protected String provTransId = null;

	protected List bundleReqArray = new ArrayList();

	protected List suspendList = new ArrayList();

	protected List unlockList = new ArrayList();

	protected List xmlifeList = new ArrayList();

	protected NbaOinkDataAccess oinkData = null;

	// SPR1770 code deleted
	protected boolean workSuspended = false;

	protected List unsuspendList = new ArrayList();

	protected NbaTableAccessor ntsAccess = null;

	ArrayList resultData = null;// NBA250

	protected final static String UNKNOWN = "UNKNOWN"; // NBA420

	// SPR2380 removed logger
	// NBA050 CODE DELETED
	// NBA008 added end
	/**
	 * NbaProcOrderRequirements constructor comment.
	 */
	public NbaProcOrderRequirements() {
		super();
		setContractAccess(UPDATE); // ACN014
	}

	/**
	 * This method creates the awd source with generated xmlife message and attached to the wotk item
	 * 
	 * @param NbaDst
	 *            - the work item
	 */
	// NBA008 New Method, ACN014 Changed signature
	protected void addXMLifeTrans(NbaDst reqItem, String xmlTrans) throws NbaBaseException {
		// ACN014 Begin
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
		// SPR3290 code deleted
		Policy policy = nbaTxLife.getPolicy();
		Iterator reqInfoIter = policy.getRequirementInfo().iterator();
		while (reqInfoIter.hasNext()) {
			RequirementInfo reqInfo = (RequirementInfo) reqInfoIter.next();
			if (reqInfo.getRequirementInfoUniqueID() != null
					&& reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqItem.getNbaLob().getReqUniqueID())) {
				// Attachment
				Attachment attach = new Attachment();
				nbaOLifEId.setId(attach);
				attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);// ACN009 SPR3160
				// ACN009 begin
				if (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MIBCHECK) {
					attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_MIB401);
				} else {
					attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_REQUIREREQUEST);
				}
				// ACN009 end
				AttachmentData attachData = new AttachmentData();
				attachData.setPCDATA(xmlTrans); // ACN014
				attach.setAttachmentData(attachData);
				attach.setActionAdd();
				reqInfo.addAttachment(attach);
				reqInfo.getActionIndicator().setUpdate();
				policy.getActionIndicator().setUpdate();
				// ACN009 code deleted
				return;
			}
		}
		// ACN014 End
	}

	/**
	 * This method creates the XMLife message for 121 transaction code.
	 * 
	 * @return the generated XMLife message
	 * @param NbaDst
	 *            - the work item
	 */
	// AXAL3.7.31 - Method is now obsolete. Requirement request is now built in AxaServiceRequestorUtils
	// NBA008 New Method
	protected NbaTXLife create121Request(NbaDst reqItem) throws NbaBaseException {
		NbaLob lob = reqItem.getNbaLob();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_GENREQUIREORDREQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); // NBA050 //SPR2639
		nbaTXRequest.setNbaLob(lob);
		RequirementInfo thisRequirementInfo = nbaTxLife.getRequirementInfo(lob.getReqUniqueID()); // NBA130

		// ACN014 Code Deleted

		// create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife); // NBA050, ACN014
		// NBA050 CODE DELETED
		// NBA035 deleted code - 103 source

		// get olife
		OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); // NBA044
		Policy policy = holding.getPolicy();
		policy.setId("Policy_1"); // SPR1207

		olife.getSourceInfo().setCreationDate(new Date());
		olife.getSourceInfo().setCreationTime(new NbaTime());

		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);

		// Life
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnut = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); // NBA093
		policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnut); // NBA093
		long productType = nbaTxLife.getPrimaryHolding().getPolicy().getProductType();// NBA050 //NBA044
		policy.setProductType(productType);
		policy.setPolNumber(generateCompoundContractNumber()); // ACN009
		NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(nbaTxLife); // SPR1184
		NbaOinkRequest oinkRequest = new NbaOinkRequest(); // SPR1184 set up the NbaOinkRequest object
		if ((productType == NbaOliConstants.OLI_PRODTYPE_ANN || productType == NbaOliConstants.OLI_PRODTYPE_VAR)
				&& (lob.getReqPersonCode() == NbaOliConstants.OLI_REL_ANNUITANT || lob.getReqPersonCode() == NbaOliConstants.OLI_REL_JOINTANNUITANT)) {
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
			// begin SPR1184
			policy.setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_LIFE);
			Life life = new Life();
			getNbaOLifEId().setId(life);// ALPC7
			lifeAnut.setLife(life);
			oinkRequest.setPartyFilter(lob.getReqPersonCode(),
					lob.getReqPersonSeq() < 10 ? "0" + lob.getReqPersonSeq() : String.valueOf(lob.getReqPersonSeq()));
			oinkRequest.setVariable("CurrentAmt");
			life.setFaceAmt(oinkDataAccess.getStringValueFor(oinkRequest)); // Face Amount
			// end SPR1184
		}

		// ApplicationInfo
		ApplicationInfo applInfo = new ApplicationInfo();
		applInfo.setSignedDate(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getSignedDate());// NBA050
		applInfo.setHOUnderwriterName(lob.getUndwrtQueue());
		applInfo.setTrackingID(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getTrackingID()); // ACN014
		policy.setApplicationInfo(applInfo);

		// RequirementInfo
		RequirementInfo reqInfo = new RequirementInfo();
		nbaOLifEId.setId(reqInfo); // NBA050
		// ACN014 Code Deleted
		reqInfo.setReqCode(lob.getReqType());
		reqInfo.setRequestedDate(new Date()); // ACN014
		reqInfo.setRequirementInfoUniqueID(lob.getReqUniqueID());// ACN014

		NbaTableAccessor tableAccessor = getTableAccessor();
		Map hashMap = tableAccessor.createDefaultHashMap(NbaConstants.PROVIDER_HOOPERHOLMES);
		NbaTableData[] data = tableAccessor.getDisplayData(hashMap, NbaTableConstants.NBA_REQUIREMENT_ACCOUNT);
		for (int i = 0; i < data.length; i++) {
			if ((((NbaUctData) data[i]).getSystemId()).equalsIgnoreCase(lob.getReqVendor())) {
				if (Integer.parseInt(((NbaUctData) data[i]).getIndexValue()) == reqInfo.getReqCode()) {
					if (((NbaUctData) data[i]).getBesValue() != null) {
						reqInfo.setRequirementAcctNum(((NbaUctData) data[i]).getBesValue());
					}
				}
			}
		}

		// NBA130 code deleted
		reqInfo.setRequirementDetails(requirementInfo.getRequirementDetails()); // NBA130
		policy.addRequirementInfo(reqInfo);

		// Attachment
		Attachment attach = new Attachment();
		nbaOLifEId.setId(attach); // NBA050
		attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_COMMENT);
		AttachmentData attachData = new AttachmentData();
		attachData.setPCDATA("");
		attach.setAttachmentData(attachData);
		holding.addAttachment(attach);

		// get insured party information (from holding inquiry)
		Relation partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq()); // NBA050
		NbaParty holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID()); // NBA050
		if (holdingParty == null) {
			throw new NbaBaseException("Could not get party information from holding inquiry");
		}

		// NBA035 delete code - to get insured party information (from XML103)

		// Insured person
		if (holdingParty.getParty().getPartyTypeCode() != 1) { // should be an indivisual
			throw new NbaBaseException("Invalid Party");
		}
		Party party = new Party();
		nbaOLifEId.setId(party); // NBA050
		party.setPartyTypeCode(holdingParty.getParty().getPartyTypeCode());
		party.setGovtID(holdingParty.getSSN());
		party.setFullName(holdingParty.getDisplayName());
		// SPR2831 code deleted
		party.setResidenceState(holdingParty.getParty().getResidenceState()); // NBA081
		party.setResidenceCountry(holdingParty.getParty().getResidenceCountry()); // NBA081
		olife.addParty(party);
		reqInfo.setAppliesToPartyID(party.getId()); // ACN014

		// Person
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
		person.setDriversLicenseNum(holdingPerson.getDriversLicenseNum()); // NBA035 get DL from 203 not 103
		person.setDriversLicenseState(holdingPerson.getDriversLicenseState()); // NBA035 get DL ST from 203 not 103
		person.setOccupation(holdingPerson.getOccupation());

		// NBA093 Code Deleted
		person.setSmokerStat(holdingPerson.getSmokerStat());// NBA093
		// NBA093 Code Deleted
		OLifEExtension olifeExt = new OLifEExtension();// NBA093
		// begin SPR1778
		PersonExtension holdingPersonExtension = NbaUtils.getFirstPersonExtension(holdingPerson);
		if (holdingPersonExtension != null) {
			olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
			person.addOLifEExtension(olifeExt);
			PersonExtension personExtension = new PersonExtension();
			olifeExt.setPersonExtension(personExtension);
			personExtension.setRateClass(holdingPersonExtension.getRateClass());
		}
		// end SPR1778

		// Address
		Address holdingAddr = null;
		List list = holdingParty.getParty().getAddress();
		for (int i = 0; i < list.size(); i++) {
			Address addr = new Address();
			holdingAddr = (Address) list.get(i);
			nbaOLifEId.setId(addr); // NBA050
			addr.setAddressTypeCode(holdingAddr.getAddressTypeCode());
			addr.setLine1(holdingAddr.getLine1());
			addr.setLine2(holdingAddr.getLine2());
			addr.setLine3(holdingAddr.getLine3());
			addr.setCity(holdingAddr.getCity());
			addr.setAddressStateTC(holdingAddr.getAddressStateTC()); // NBA093
			addr.setZip(holdingAddr.getZip());
			addr.setPrefAddr(holdingAddr.getPrefAddr()); // NBA081
			party.addAddress(addr);
		}

		// Phone
		// SPR1314 code deleted
		list = holdingParty.getParty().getPhone();
		for (int i = 0; i < list.size(); i++) {
			Phone holdingPhone = (Phone) list.get(i);
			Phone phone = new Phone();
			nbaOLifEId.setId(phone); // NBA050
			phone.setPhoneTypeCode(holdingPhone.getPhoneTypeCode());
			phone.setAreaCode(holdingPhone.getAreaCode());
			phone.setDialNumber(holdingPhone.getDialNumber());
			phone.setExt(holdingPhone.getExt());
			// SPR1314 code deleted
			phone.setPrefPhone(holdingPhone.getPrefPhone());
			phone.setCountryCode(holdingPhone.getCountryCode());

			party.addPhone(phone);
		}

		// Client
		if (holdingParty.getParty().hasClient()) { // NBA035 get client from 203, not 103 source
			Client client = new Client();
			// clinet.setPrefLanguage(); //nbA does not support
			olifeExt = new OLifEExtension();
			client.addOLifEExtension(olifeExt);
			ClientExtension clientExt = new ClientExtension();
			olifeExt.setClientExtension(clientExt);
			// begin SPR1314
			if (holdingParty.getParty().getClient().getOLifEExtensionCount() > 0
					&& holdingParty.getParty().getClient().getOLifEExtensionAt(0).getClientExtension() != null) { // NBA035 get client from 203, not
																													// 103 source
				clientExt.setEmployerName(holdingParty.getParty().getClient().getOLifEExtensionAt(0).getClientExtension().getEmployerName());
			}
			// end SPR1314
			party.setClient(client);
		}

		// Organization
		party = new Party();
		nbaOLifEId.setId(party); // NBA050
		party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		Organization org = new Organization();
		// NBA093 code deleted
		perOrg.setOrganization(org);
		// ACN014 begin
		try {
			Company co = NbaConfiguration.getInstance().getProviderOrganizationKeyCompany(lob.getReqVendor(), lob.getCompany());
			org.setOrganizationKey(co.getOrganizationKey()); // "CSC Consolidated");
			org.setOrgCode(co.getOrgCode());// "CSC");
		} catch (NbaBaseException nbe) {
			org.setOrganizationKey("Not found");
			org.setOrgCode("Not found");
		}
		// ACN014 end
		olife.addParty(party);
		reqInfo.setRequesterPartyID(party.getId()); // ACN014

		// Doctor
		// Begin NBA130
		String doctorPartyId = null;
		RequirementInfoExtension requirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(thisRequirementInfo);
		if (null != requirementInfoExt && requirementInfoExt.hasPhysicianPartyID()) {
			Party doctor = (nbaTxLife.getParty(requirementInfoExt.getPhysicianPartyID())).getParty();
			if (null != doctor) {
				party = doctor.clone(false);
				party.setId(null);
				nbaOLifEId.setId(party);
				doctorPartyId = party.getId();
				olife.addParty(party);
			}
		}
		// End NBA130
		// AXAL3.7.40 Code Deleted

		String agentPartyID = null;
		String agencyPartyID = null;
		String servicingAgentPartyID = null; // NBA081
		// SPR3290 code deleted
		String fulfillerPartyId = null; // ACN014
		if (nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_PRIMAGENT) != null && nbaTxLife.getWritingAgent() != null) { // SPR1135, ACN014
			party = nbaTxLife.getWritingAgent().getParty().clone(false); // NBA050
			party.setId(null); // ACN014
			nbaOLifEId.setId(party); // NBA050
			agentPartyID = party.getId();
			olife.addParty(party);
		}

		if (nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_SERVAGENCY) != null && nbaTxLife.getServicingAgent() != null) { // SPR1135, ACN014
			party = nbaTxLife.getServicingAgent().getParty().clone(false);// NBA050
			party.setId(null); // ACN014
			nbaOLifEId.setId(party); // NBA050
			agencyPartyID = party.getId();
			olife.addParty(party);
		}

		// ACN014 begin
		// [TODO uncomment this section and delete the other after PostRequirement is updated to add Fulfiller Party
		/*
		 * if (nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_FULFILLS) != null) { party = nbaTxLife.getFulfillerParty().getParty().clone(false);
		 * nbaOLifEId.setId(party); fulfillerPartyId = party.getId(); olife.addParty(party); }
		 */
		// add fulfiller party Id
		party = new Party();
		nbaOLifEId.setId(party);
		party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		org = new Organization();
		perOrg.setOrganization(org);
		org.setOrganizationKey(lob.getReqVendor());
		org.setOrgCode(lob.getReqVendor());
		olife.addParty(party);
		fulfillerPartyId = party.getId();
		reqInfo.setFulfillerPartyID(fulfillerPartyId);
		// ACN014 code deleted
		// ACN014 end

		// NBA010 - begin
		if (nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_SERVAGENT) != null) {
			NbaParty holdingParty_1 = null;
			Relation partyRel_1 = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_SERVAGENT, Integer.parseInt("01"));
			if (partyRel_1 != null) {
				holdingParty_1 = nbaTxLife.getParty(partyRel_1.getRelatedObjectID());
			}
			if (holdingParty_1 != null) {
				party = holdingParty_1.getParty().clone(false); // ACN014
				party.setId(null); // ACN014
				nbaOLifEId.setId(party);
				servicingAgentPartyID = party.getId();
				olife.addParty(party);
			}
		}
		// NBA010 - end

		olife.addRelation(createRelation(
				txLife, // ACN014
				olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), olife.getPartyAt(0).getId(),
				NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, reqItem.getNbaLob().getReqPersonCode())); // NBA044

		olife.addRelation(createRelation(
				txLife, // ACN014
				olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), olife.getPartyAt(1).getId(),
				NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_REQUESTOR)); // NBA044, ACN014

		olife.addRelation(createRelation(txLife, // ACN014
				olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getRequirementInfoAt(0).getId(), olife
						.getPartyAt(0).getId(), NbaOliConstants.OLI_REQUIREMENTINFO, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_FORMFOR)); // NBA044

		if (null != doctorPartyId) { // NBA130
			olife.addRelation(createRelation(txLife, // ACN014
					olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), doctorPartyId, // NBA130
					NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_PHYSICIAN)); // NBA044
		}

		if (agentPartyID != null) { // SPR1135
			olife.addRelation(createRelation(
					txLife, // ACN014
					olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), agentPartyID, NbaOliConstants.OLI_HOLDING,
					NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_PRIMAGENT)); // NBA044
		}

		if (agencyPartyID != null) { // SPR1135
			olife.addRelation(createRelation(
					txLife, // ACN014
					olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), agencyPartyID, NbaOliConstants.OLI_HOLDING,
					NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_SERVAGENCY)); // NBa044
		}
		if (servicingAgentPartyID != null) { // NBA081
			olife.addRelation(createRelation(
					txLife, // ACN014
					olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), servicingAgentPartyID,
					NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_SERVAGENT));
		}
		// ACN014 code deleted
		if (fulfillerPartyId != null) { // ACN014
			olife.addRelation(createRelation(txLife, olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), fulfillerPartyId,
					NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_FULFILLS));
		}

		for (int z = 0; z < olife.getRelationCount(); z++) {
			Relation arel = olife.getRelationAt(z);
			arel.setRelatedRefID("01");
		}
		// Begin NBA130
		thisRequirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_SUBMITTED);
		thisRequirementInfo.setRequestedDate(new Date());
		thisRequirementInfo.setActionUpdate();
		// End NBA130
		return txLife;
	}

	/**
	 * This method creates the XMLife message for 401 transaction code.
	 * 
	 * @return the generated XMLife message
	 * @param NbaDst
	 *            - the work item
	 */
	// NBA008 New Method
	protected NbaTXLife create401Request(NbaDst reqItem) throws NbaBaseException {
		NbaLob lob = reqItem.getNbaLob(); // AXAL3.7.31
		// ACN009 begin
		String pendingResponseOK = "0";
		boolean testIndicator = NbaConfiguration.getInstance().getProvider(lob.getReqVendor()).getTestIndicator(); // AXAL3.7.31
		String mibCheckingFollowUpInd = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_CHECKING_FOLLOWUP_INDICATOR); //NBLXA-1524
		String mibIaiFollowUpInd = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_IAI_FOLLOWUP_INDICATOR); //NBLXA-1524
		
		// ACN009 end
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_MIBINQUIRY);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); // NBA050 SPR2639
		nbaTXRequest.setNbaLob(lob);

		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife); // NBA050

		// create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);

		// ACN009 begin
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setPendingResponseOK(pendingResponseOK);
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTestIndicator(testIndicator);
		txLife.getTXLife().setVersion(NbaOliConstants.OLIFE_VERSION_34_02); // AXAL3.7.31,APSL5361

		MIBRequest mibRequest = new MIBRequest();
		mibRequest.setMIBPriority(NbaOliConstants.TC_MIBPRIORITY_STANDARD);
		mibRequest.setMIBSearchDepth(NbaOliConstants.TC_MIBSEARCH_STANDARD);

		MIBServiceDescriptor cMibServiceDescriptor = new MIBServiceDescriptor();
		cMibServiceDescriptor.setMIBService(NbaOliConstants.TC_MIBSERVICE_CHECKING); 
		MIBServiceOptions cMibServiceOptions = new MIBServiceOptions();
		cMibServiceOptions.setMIBFollowUpInd(mibCheckingFollowUpInd); //NBLXA-1524
		cMibServiceDescriptor.setMIBServiceOptions(cMibServiceOptions);
		MIBServiceDescriptorOrMIBServiceConfigurationID mibServiceDescriptorOrMIBServiceConfigurationID = new MIBServiceDescriptorOrMIBServiceConfigurationID();
		mibServiceDescriptorOrMIBServiceConfigurationID.addMIBServiceDescriptor(cMibServiceDescriptor);

		MIBServiceDescriptor iMibServiceDescriptor = new MIBServiceDescriptor();
		iMibServiceDescriptor.setMIBService(NbaOliConstants.TC_MIBSERVICE_IAI);
		MIBServiceOptions iMibServiceOptions = new MIBServiceOptions();
		iMibServiceOptions.setMIBFollowUpInd(mibIaiFollowUpInd); //NBLXA-1524
		iMibServiceDescriptor.setMIBServiceOptions(iMibServiceOptions);
		mibServiceDescriptorOrMIBServiceConfigurationID.addMIBServiceDescriptor(iMibServiceDescriptor);
		mibRequest.setMIBServiceDescriptorOrMIBServiceConfigurationID(mibServiceDescriptorOrMIBServiceConfigurationID);

		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setMIBRequest(mibRequest);
		// ACN009 end

		// get olife
		OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		olife.setVersion(NbaOliConstants.OLIFE_VERSION_34_02); // AXAL3.7.31,APSL5361

		// ACN009 begin
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setPrimaryObjectID(holding.getId()); // AXAL3.7.31
		Policy policy = holding.getPolicy();
		long productType = nbaTxLife.getPrimaryHolding().getPolicy().getProductType();
		policy.setProductType(productType);
		policy.setPolNumber(generateCompoundContractNumber()); // ACN009
		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		holding.setHoldingStatus(NbaOliConstants.OLI_HOLDSTATE_PROPOSED);
		holding.setCurrencyTypeCode(NbaOliConstants.OLI_CURRENCY_USD);
		holding.setHoldingForm(NbaOliConstants.OLI_HOLDFORM_IND);

		holding.getPolicy().setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_LIFE);

		olife.getSourceInfo().setCreationDate(new Date());
		olife.getSourceInfo().setCreationTime(new NbaTime());
		olife.setCurrentLanguage(NbaOliConstants.OLI_LANG_ENGLISH);

		// Life
		Life life = new Life();
		getNbaOLifEId().setId(life);// ALPC7
		life.setFaceAmt(lob.getFaceAmount());
		Coverage coverage = new Coverage();
		LifeParticipant lifeParticipant = new LifeParticipant();
		lifeParticipant.setLifeParticipantRoleCode(NbaOliConstants.OLI_PARTICROLE_PRIMARY);
		coverage.addLifeParticipant(lifeParticipant);
		life.addCoverage(coverage);
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnnut = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		lifeAnnut.setLife(life);
		policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnnut);
		// ACN009 end

		// RequirementInfo
		RequirementInfo reqInfo = new RequirementInfo();
		nbaOLifEId.setId(reqInfo);
		reqInfo.setReqCode(lob.getReqType());
		reqInfo.setRequirementInfoUniqueID(lob.getReqUniqueID());// ACN014
		reqInfo.setRequestedDate(new Date());
		reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_SUBMITTED);// ACN009
		reqInfo.setMIBInquiryReason(NbaOliConstants.OLI_MIBREASON_NB);// ACN009
		// ACN009 code deleted
		// ACN009 begin
		// ApplicationInfo
		ApplicationInfo applInfo = new ApplicationInfo();
		applInfo.setHOAssignedAppNumber(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getHOAssignedAppNumber());
		applInfo.setTrackingID(nbaTxLife.getNbaHolding().getPolicy().getApplicationInfo().getTrackingID()); // AXAL3.7.31
		if (!applInfo.hasTrackingID()) { // NBA420
			applInfo.setTrackingID(lob.getReqUniqueID());
		}
		policy.setApplicationInfo(applInfo);
		// ACN009 end

		// NBA130 code deleted
		reqInfo.setRequirementDetails(requirementInfo.getRequirementDetails()); // NBA130
		// begin ACN014
		policy.addRequirementInfo(reqInfo);

		// do holding inquiry
		// NBA050 DELETED CODE
		Relation partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq());// NBA050
		NbaParty holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID());// NBA050
		if (holdingParty == null) {
			throw new NbaBaseException("Could not get party information from holding inquiry");
			// end ACN014
		}
		// ACN014 code deleted
		// create person party
		Party party = new Party();
		nbaOLifEId.setId(party); // NBA050
		// ACN009 begin
		party.setGovtID(holdingParty.getSSN());
		party.setGovtIDTC(holdingParty.getParty().getGovtIDTC()); // NBA420
		party.setPartyKey(holdingParty.getParty().getPartyKey()); // APSL4361
		// Begin APSL357
		if (holdingParty.getParty().hasResidenceState() && !(holdingParty.getParty().getResidenceState() == NbaOliConstants.OLI_STATE_1009800001)) {
			// birth place
			// NBA093 deleted 4 lines
			party.setResidenceState(holdingParty.getParty().getResidenceState()); // AXAL3.7.31
		} else {
			party.setResidenceState(NbaOliConstants.OLI_UNKNOWN); // AXAL3.7.31
		}

		if (holdingParty.getParty().hasResidenceCountry()) { // ALS4914
			long residenceCountry = holdingParty.getParty().getResidenceCountry();
			String besValue = getTableAccessor().translateOlifeValue(getKeyMap(), NbaTableConstants.OLI_LU_NATION, String.valueOf(residenceCountry),
					-1);
			if (!NbaUtils.isBlankOrNull(besValue)) {
				party.setResidenceCountry(besValue); // AXAL3.7.31
			} else {
				party.setResidenceCountry(residenceCountry); // AXAL3.7.31
			}

		} else {
			party.setResidenceCountry(NbaOliConstants.OLI_UNKNOWN);
		}
		// End APSL357

		party.setPartyKey(holdingParty.getParty().getPartyKey());
		// SPR3290 code deleted
		if (holdingParty.getParty().hasResidenceZip()) {
			party.setResidenceZip(holdingParty.getParty().getResidenceZip());
		} else if (holdingParty.getParty().getAddress() != null && holdingParty.getParty().getAddressCount() > 0) {
			party.setResidenceZip(holdingParty.getParty().getAddressAt(0).getZip());
		} else {
			party.setResidenceZip("");
		}
		// ACN009 end
		PersonOrOrganization perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		Person person = new Person();
		perOrg.setPerson(person);
		person.setLastName(holdingParty.getLastName());
		person.setFirstName(holdingParty.getFirstName());
		if(holdingParty.getMiddleInitial() != null){ //NBLXA-145
		person.setMiddleName(holdingParty.getMiddleInitial());
		//NBLXA-1455 Starts
		}
		else {
			person.setMiddleName("");
		}
		//NBLXA-1455 Ends
		person.setBirthDate(holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthDate());
		person.setGender(holdingParty.getParty().getPersonOrOrganization().getPerson().getGender()); // ACN009
		person.setOccupation(holdingParty.getOccupation());// NBA420
		if (!person.hasOccupation()) { // NBA420
			person.setOccupation(UNKNOWN);
		}
		person.setOccupClass(holdingParty.getParty().getPersonOrOrganization().getPerson().getOccupClass()); // APSL5361
		// begin ALS4914
		if (holdingParty.getParty().getPersonOrOrganization().getPerson().hasBirthCountry()) { // ALS4914
			// begin APSL357
			long birthCountry = holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthCountry();
			String besValue = getTableAccessor().translateOlifeValue(getKeyMap(), NbaTableConstants.OLI_LU_NATION, String.valueOf(birthCountry), -1);
			if (!NbaUtils.isBlankOrNull(besValue)) {
				person.setBirthCountry(besValue);
			} else {
				person.setBirthCountry(birthCountry);
			}
			// End APSL357
		} else {
			person.setBirthCountry(NbaOliConstants.OLI_UNKNOWN);
		}
		// end ALS4914
		if (holdingParty.getParty().getPersonOrOrganization().getPerson().hasBirthJurisdictionTC() && // ALS4914
				!(holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthJurisdictionTC() == NbaOliConstants.OLI_STATE_1009800001)) { // ALS4914
			// birth place
			// NBA093 deleted 4 lines
			person.setBirthJurisdictionTC(holdingParty.getParty().getPersonOrOrganization().getPerson().getBirthJurisdictionTC()); // AXAL3.7.31
		} else {
			person.setBirthJurisdictionTC(NbaOliConstants.OLI_UNKNOWN); // AXAL3.7.31
		}
		// ACN009 code deleted
		//Start NBLXA-1714
		if (lob.getReqPersonCode() == NbaOliConstants.OLI_REL_DEPENDENT) {
			NbaParty primaryParty = nbaTxLife.getPrimaryParty();
			Party insuredParty = null;
			if (primaryParty != null) {
				insuredParty = primaryParty.getParty();
			}
			if (insuredParty != null) {
				party.setGovtID(UNKNOWN);
				if (insuredParty.hasResidenceState() && !(insuredParty.getResidenceState() == NbaOliConstants.OLI_STATE_1009800001)) {
					party.setResidenceState(insuredParty.getResidenceState());
				} else {
					party.setResidenceState(NbaOliConstants.OLI_UNKNOWN); 
	 			}
				
				if (insuredParty.hasResidenceCountry()) { 
					long residenceCountry = insuredParty.getResidenceCountry();
					String besValue = getTableAccessor().translateOlifeValue(getKeyMap(), NbaTableConstants.OLI_LU_NATION, String.valueOf(residenceCountry),
							-1);
					if (!NbaUtils.isBlankOrNull(besValue)) {
						party.setResidenceCountry(besValue); 
					} else {
						party.setResidenceCountry(residenceCountry); 
					}

				} else {
					party.setResidenceCountry(NbaOliConstants.OLI_UNKNOWN);
				}
				if (insuredParty.hasResidenceZip()) {
					party.setResidenceZip(insuredParty.getResidenceZip());
				} else if (insuredParty.getAddress() != null && insuredParty.getAddressCount() > 0) {
					party.setResidenceZip(insuredParty.getAddressAt(0).getZip());
				} else {
					party.setResidenceZip("");
				}
				person.setBirthJurisdictionTC(NbaOliConstants.OLI_UNKNOWN);
				person.setBirthCountry(NbaOliConstants.OLI_UNKNOWN);
				person.setOccupation(UNKNOWN);
			}

		}
		//End NBLXA-1714
		olife.addParty(party);
		reqInfo.setAppliesToPartyID(party.getId());// ACN009
		lifeParticipant.setPartyID(party.getId());// ACN009

		// ACN009 begin create organization party
		party = new Party();
		nbaOLifEId.setId(party); // NBA050
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		Organization org = new Organization();
		// NBA093 code deleted
		perOrg.setOrganization(org);
		Producer producer = new Producer();
		CarrierAppointment carrierAppointment = new CarrierAppointment();
		//carrierAppointment.setCarrierApptTypeCode(NbaOliConstants.OLI_UNKNOWN); // SPR2514 //NBLXA-1455
		carrierAppointment.setCarrierApptTypeCode(NbaOliConstants.OLI_PROTYPE_AGENT); //NBLXA-1455
		producer.addCarrierAppointment(carrierAppointment);
		party.setProducer(producer);
		olife.addParty(party);
		// ACN014 code deleted

		party = new Party();
		nbaOLifEId.setId(party);
		perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		org = new Organization();
		perOrg.setOrganization(org);
		Carrier carrier = new Carrier();
		Company comp = NbaConfiguration.getInstance().getProviderOrganizationKeyCompany("MIB", holding.getPolicy().getCarrierCode()); // ACN014
		carrier.setCarrierCode(comp.getOrgCode()); // ACN014
		carrier.setCarrierForm(NbaOliConstants.OLI_CARRIERFORM_DIRECT);
		party.setCarrier(carrier);
		olife.addParty(party);
		reqInfo.setRequesterPartyID(party.getId()); // ACN014
		holding.getPolicy().setCarrierPartyID(party.getId());
		// ACN009 end

		// add relations to the olife
		olife.addRelation(createRelation(txLife, // ACN014
				olife.getRelationCount(), holding.getId(),// ACN014
				olife.getPartyAt(0).getId(), NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, reqItem.getNbaLob().getReqPersonCode())); // NBA044

		// ACN009 begin
		olife.addRelation(createRelation(txLife, olife.getRelationCount(), holding.getId(), olife.getPartyAt(1).getId(), NbaOliConstants.OLI_HOLDING,
				NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_PRIMAGENT));

		olife.addRelation(createRelation(
				txLife, // ACN014
				olife.getRelationCount(), holding.getId(), olife.getPartyAt(2).getId(), NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY,
				NbaOliConstants.OLI_REL_REQUESTOR)); // ACN014
		// ACN009 end
		// Begin NBA130
		requirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_SUBMITTED);
		requirementInfo.setRequestedDate(new Date());
		requirementInfo.setActionUpdate();
		// End NBA130

		return txLife;
	}

	/**
	 * This method creates the XMLife message for 9001 transaction code.
	 * 
	 * @return the generated XMLife message
	 * @param NbaDst
	 *            - the work item
	 */
	// NBA008 New Method
	protected NbaTXLife create9001Request(NbaDst reqItem) throws NbaBaseException {
		List errorList = new ArrayList();

		NbaLob lob = reqItem.getNbaLob();
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_EMAIL);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser())); // NBA050 SPR2639
		nbaTXRequest.setNbaLob(lob);

		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife); // NBA050

		// create txlife with default request fields
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
		// NBA050 CODE DELETED
		// get olife
		OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); // NBA044
		Policy policy = holding.getPolicy();

		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);

		// RequirementInfo
		RequirementInfo reqInfo = new RequirementInfo();
		nbaOLifEId.setId(reqInfo); // NBA050
		reqInfo.setAppliesToPartyID("Party_1");
		reqInfo.setReqCode(lob.getReqType());
		reqInfo.setRequirementDetails(NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REQCODE, lob.getReqType()));
		policy.addRequirementInfo(reqInfo);

		// get insured party information
		Relation partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq()); // NBA050
		NbaParty holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID());// NBA050
		if (holdingParty == null) {
			throw new NbaDataException("Could not get party information from holding inquiry");
		}

		Party party = new Party();
		nbaOLifEId.setId(party); // NBA050
		party.setPartyTypeCode(holdingParty.getParty().getPartyTypeCode());
		olife.addParty(party);
		// person
		PersonOrOrganization perOrg = new PersonOrOrganization();
		party.setPersonOrOrganization(perOrg);
		Person person = new Person();
		perOrg.setPerson(person);
		Person holdingPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
		person.setLastName(holdingPerson.getLastName());
		person.setFirstName(holdingPerson.getFirstName());
		person.setMiddleName(holdingPerson.getMiddleName());

		// begin SPR3050
		olife.addRelation(createRelation(txLife, olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy()
				.getRequirementInfoAt(0).getId(), party.getId(), NbaOliConstants.OLI_REQUIREMENTINFO, NbaOliConstants.OLI_PARTY,
				NbaOliConstants.OLI_REL_FORMFOR));
		olife.addRelation(createRelation(txLife, olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), party.getId(),
				NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, reqItem.getNbaLob().getReqPersonCode()));
		// Add Organization Party
		party = new Party();
		nbaOLifEId.setId(party); // NBA050
		party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
		olife.addParty(party);
		party.setFullName("");

		EMailAddress mail = new EMailAddress();
		nbaOLifEId.setId(mail); // NBA050
		mail.setEMailType(NbaOliConstants.OLI_EMAIL_BUSINESS);
		mail.setAddrLine(NbaConfiguration.getInstance().getEmailUI().getReplyTo()); // SPR3041
		party.addEMailAddress(mail);

		olife.addRelation(createRelation(txLife, olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), party.getId(),
				NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_REQUESTEDBY));
		// end SPR3050
		// Attachment
		Attachment attach = new Attachment();
		nbaOLifEId.setId(attach); // NBA050
		StringBuffer subject = new StringBuffer();
		subject.append(getTranslationText(lob.getCompany(), NbaTableConstants.NBA_COMPANY));
		subject.append(", ");
		subject.append(EMAIL_CONTRACT);
		subject.append(lob.getPolicyNumber());
		subject.append(", ");
		subject.append(EMAIL_INSURED);
		subject.append(holdingPerson.getLastName());
		if (holdingPerson.hasFirstName()) {
			subject.append(" ");
			subject.append(holdingPerson.getFirstName());
		}
		if (holdingPerson.hasMiddleName()) {
			subject.append(" ");
			subject.append(holdingPerson.getMiddleName());
		}
		subject.append(", ");
		subject.append(EMAIL_REQUIREMENT);
		subject.append(reqInfo.getRequirementDetails());
		attach.setAttachmentKey(subject.toString());

		attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_EMAIL);
		AttachmentData attachData = new AttachmentData();

		StringBuffer body = new StringBuffer();
		body.append("Following information is required to process your application. \n\n"); // SPR3236
		body.append(reqInfo.getRequirementDetails());
		body.append("\n\nRegards,\n"); // SPR3236

		body.append("Manager New Business");
		attachData.setPCDATA(body.toString());
		attach.setAttachmentData(attachData);
		holding.addAttachment(attach);

		// get owner or agent information
		if (lob.getReqVendor().equals(NbaConstants.PROVIDER_OWNER)) {
			partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_OWNER, 1);// NBA050
			holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID());// NBA050
			if (holdingParty == null) {
				throw new NbaDataException("Could not get owner information from holding inquiry");
			}

			party = new Party();
			nbaOLifEId.setId(party); // NBA050
			party.setPartyTypeCode(holdingParty.getParty().getPartyTypeCode());
			olife.addParty(party);
			// person or Organization
			perOrg = new PersonOrOrganization();
			party.setPersonOrOrganization(perOrg);
			if (NbaOliConstants.OLI_PT_PERSON == holdingParty.getParty().getPartyTypeCode()) { // SPR2975
				person = new Person();
				perOrg.setPerson(person);
				holdingPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
				person.setLastName(holdingPerson.getLastName());
				person.setFirstName(holdingPerson.getFirstName());
				person.setMiddleName(holdingPerson.getMiddleName());
				// begin SPR2975
			} else if (NbaOliConstants.OLI_PT_ORG == holdingParty.getParty().getPartyTypeCode()) {
				Organization org = new Organization();
				perOrg.setOrganization(org);
				Organization holdingOrg = holdingParty.getParty().getPersonOrOrganization().getOrganization();
				org.setOrgCode(holdingOrg.getOrgCode());
			}
			// end SPR2975
			if (holdingParty.getParty().getEMailAddress() != null && holdingParty.getParty().getEMailAddress().size() > 0
					&& holdingParty.getParty().getEMailAddressAt(0).getAddrLine() != null) {
				party.addEMailAddress(holdingParty.getParty().getEMailAddressAt(0));
			} else {
				errorList.add("Owner Email Address");
			}
			olife.addRelation(createRelation(txLife, olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), party.getId(),
					NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_OWNER)); // SPR3050
		} else if (lob.getReqVendor().equals(NbaConstants.PROVIDER_PRODUCER)) {
			// AXAL3.7.40 Code Deleted
			// get writing agent (from holding inquiry)
			holdingParty = nbaTxLife.getWritingAgent();// NBA050
			if (holdingParty == null) {
				throw new NbaDataException("Could not get producer information from holding inquiry");
			}

			party = new Party();
			nbaOLifEId.setId(party); // NBA050
			party.setPartyTypeCode(holdingParty.getParty().getPartyTypeCode());
			olife.addParty(party);
			// person
			perOrg = new PersonOrOrganization();
			party.setPersonOrOrganization(perOrg);
			person = new Person();
			perOrg.setPerson(person);
			holdingPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
			person.setLastName(holdingPerson.getLastName());
			person.setFirstName(holdingPerson.getFirstName());
			person.setMiddleName(holdingPerson.getMiddleName());
			if (holdingParty.getParty().getEMailAddress() != null && holdingParty.getParty().getEMailAddress().size() > 0
					&& holdingParty.getParty().getEMailAddressAt(0).getAddrLine() != null) {
				party.addEMailAddress(holdingParty.getParty().getEMailAddressAt(0));
			} else {
				errorList.add("Agent Email Address");
			}

			Producer prdc = holdingParty.getParty().getProducer();
			if (prdc != null) {
				for (int i = 0; i < prdc.getCarrierAppointmentCount(); i++) {
					prdc.getCarrierAppointmentAt(i).setPartyID(party.getId());
				}
				party.setProducer(holdingParty.getParty().getProducer());
			}
			olife.addRelation(createRelation(txLife, olife.getRelationCount(), NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(), party.getId(),
					NbaOliConstants.OLI_HOLDING, NbaOliConstants.OLI_PARTY, NbaOliConstants.OLI_REL_PRIMAGENT)); // SPR3050
		} else {
			errorList.add("Provider Id");
		}

		// SPR3050 code deleted

		if (errorList.size() > 0) {
			throw new NbaDataException(errorList);
		}
		// Begin NBA130
		requirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_SUBMITTED);
		requirementInfo.setRequestedDate(new Date());
		requirementInfo.setActionUpdate();
		// End NBA130
		return txLife;
	}

	/**
	 * This method creates the XMLife message based on the transaction code.
	 * 
	 * @return the generated XMLife message
	 * @param NbaDst
	 *            - the work item
	 */
	// NBA008 New Method
	protected NbaTXLife createXMLifeTransaction(NbaDst reqItem) throws NbaBaseException {

		if (Long.parseLong(getTransactionCode()) == NbaOliConstants.TC_TYPE_GENREQUIREORDREQ) {
			return new AxaServiceRequestorUtils().createTXLife121Request(reqItem, nbaTxLife, getUser(), reqItem.getNbaLob().getPolicyNumber()); // AXAL3.7.31,
																																				// ALII2067
		} else if (Long.parseLong(getTransactionCode()) == NbaOliConstants.TC_TYPE_MIBINQUIRY) {
			return create401Request(reqItem);
		} else if (Long.parseLong(getTransactionCode()) == NbaOliConstants.TC_TYPE_EMAIL) {
			return create9001Request(reqItem);
		}

		throw new NbaBaseException("Invalid transaction code");
	}

	/**
	 * This method first determine the bundle requirements based on provider id and transaction code.Call AWD to get array of bundle requirement dst
	 * objects. Then process the bundle requirements.
	 * 
	 * @return true if bundle requirements processed succesfully.
	 */
	// NBA008 New Method
	protected boolean doBundleRequirements() throws NbaBaseException {
		List wfIdList = new ArrayList(); // SPR3290
		// SPR3290 code deleted
		NbaLob lob = getWork().getNbaLob();

		NbaSource source = getParentCase().getRequirementControlSource();
		if (source == null) {
			throw new NbaBaseException(NO_REQ_CTL_SRC); // NBA050
		}
		NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
		// get insurable party object from requirement control source
		InsurableParty party = reqSource.getInsurableParty(Integer.toString(lob.getReqPersonSeq()), lob.getReqPersonCode());
		Requirement req = null;
		for (int i = 0; i < party.getRequirementCount(); i++) {
			req = party.getRequirementAt(i);
			if (req.getProvider().equals(lob.getReqVendor()) && req.getProvTransId() == null) { // ALII1459, ALII1463
				throw new NbaBaseException("ProvTransId missing in Parent case Requirement Control Source");
			}
			if (req.getProvider().equals(lob.getReqVendor()) && req.getProvTransId().equals(getProvTransId())) {
				if (!req.getAwdId().equals(getWork().getID())) {
					// add bundle work items id to the list
					wfIdList.add(req.getAwdId()); // SPR3290
				}
			}
		}
		// if bundle requirement ids are present on the requirement control source
		if (wfIdList.size() > 0) { // SPR3290
			// NBA213 deleted code
			// create ans set retrive option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.setLockWorkItem();
			retOpt.requestSources();
			retOpt.setAutoSuspend();
			// get dst list
			bundleReqArray = retrieveWorkItemList(getUser(), retOpt, wfIdList); // NBA213 SPR3290
			// add list to unlock list
			unlockList.addAll(bundleReqArray);
			// NBA213 deleted code

			// process the all bundle requirements
			return processBundleRequirements();
		}
		return true;

	}

	/**
	 * This process is to order requirements from a third party provider, producer or owner depending on criteria set up in a user defined VP/MS
	 * model. Third party providers supported are MIB, EMSI, Lab One, and CRL. A VP/MS model will be used to determine, by requirement code and other
	 * criteria, whether or not the requirement should be requested from one of these providers or a request, via email, sent to the owner or
	 * producer.
	 * 
	 * @param user
	 *            the user for whom the process is being executed
	 * @param work
	 *            a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// NBA027 - logging code deleted
		// Initialization
		if (!initialize(user, work)) {
			return getResult();// NBA050
		}
		
		// NBA008 code deleted
		// begin SPR1311
		NbaLob lob = getWork().getNbaLob();
		if (NbaConstants.PROVIDER_MANUAL.equalsIgnoreCase(lob.getReqVendor())) {
			processManualRequirement();
			return getResult();
		}
		// Start NBLXA-1313,NBLXA-1897
		if (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_PPR) {
			Relation partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq());
			NbaParty holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID());
			String validateMsg = validateDOBGenderZIP(holdingParty);
			if (!NbaUtils.isBlankOrNull(validateMsg)) {
				String comment = NbaUtils.getRequirementTranslation(String.valueOf(getWork().getNbaLob().getReqType()), getNbaTxLife().getPolicy())
						+ " requirement cannot be ordered due to missing " + validateMsg;
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_MISSING_DETAILS, comment);
			}
		}
		// End NBLXA-1313,NBLXA-1897
		// Start NBLXA-1501
		if (lob.getReqType() == NbaOliConstants.OLI_REQCODE_MIBCHECK) {
			boolean isValidFlag = true;
			Relation partyRel = getRelation(nbaTxLife, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode(), lob.getReqPersonSeq()); // NBA050
			NbaParty holdingParty = nbaTxLife.getParty(partyRel.getRelatedObjectID());
			if (holdingParty != null) {
				Person holdingPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
				if (holdingPerson != null) {
					if (!NbaUtils.validateSpecialCharWithoutSpace(holdingPerson.getFirstName()) //NBLXA-1868
							|| !NbaUtils.validateSpecialCharWithoutSpace(holdingPerson.getLastName())
							|| !NbaUtils.validateSpecialCharWithoutSpace(holdingPerson.getMiddleName())
							|| !NbaUtils.validateSpecialCharWithoutSpace(holdingPerson.getOccupation())) {
						isValidFlag = false;
					}
					if (!isValidFlag) {
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_SPECIAL_CHAR_PRESENT);
					}
				}
			}
		}
		// End NBLXA-1501
		
		// end SPR1311
		// NBA008 code added Start
		// NBA086 code deleted
		// retrieve sources
		retrieveWork();

		// initialized VPMS value object
		oinkData = new NbaOinkDataAccess(lob); // SPR1311
		// get parent case from awd
		getParentCase();
		// get bundle support indicator from configuration file
		if (getBundleSupportInd()) {
			try {
				if (!doBundleRequirements()) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspend", "Suspend"));
				}
			} catch (NbaNetServerException e) {
				// SPR1851 code deleted
				throw e;
			}
		}
		if (getResult() == null) {
			// order requirements
			try {

				orderRequirements();
			} catch (NbaVpmsException ve) {
				// if VPMS error
				// APSL3874 Code deleted
				unlockAWD();
				throw ve; // APSL3874
			}
			if (getResult() == null) {
				getWork().getNbaLob().setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED));
				getWork().getNbaLob().setReqOrderDate(requirementInfo.getRequestedDate()); // NBA130
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
			}
		}
		if (!isWorkSuspended()) {
			changeStatus(getResult().getStatus());
			updateAWD(bundleReqArray);
			unsuspendAWD();
		}
		// update to the AWD
		updateWork();
		// suspend work item but it must be called after update
		suspendAWD();
		// unlock work items
		unlockAWD();

		
		
		// NBA008 code added End
		return getResult();
	}

	//NBLXA-1313 method will check DOB, Gender and ZIP present if not then return relevant message  
	protected String  validateDOBGenderZIP(NbaParty holdingParty) {
			String msg = "";
			Person insuredPerson = null;
			if ((holdingParty.getParty() != null) && (holdingParty.getParty().getPersonOrOrganization() != null)) {
				insuredPerson = holdingParty.getParty().getPersonOrOrganization().getPerson();
				if (insuredPerson != null) {
					if (!insuredPerson.hasBirthDate()) {
						msg = msg.concat("DOB ");
					}
					if (!insuredPerson.hasGender()) {
						msg = msg.concat("Gender ");
					}
					List addressList = holdingParty.getParty().getAddress();
					for (int j = 0; (addressList != null) && (j < addressList.size()); j++) {
						Address address = (Address) addressList.get(j);
						if (!address.hasZip() || address.getZip().equals(NbaTableConstants.EMPTY_STRING)) {
							msg = msg.concat("ZIP ");
							break;
						}
					}
				}
			}
			msg = msg.trim().replace(" ", ",");
			return msg;
	}
	
	/**
	 * Answer whether provider supports bundle transactions.
	 * 
	 * @return boolean
	 */
	// NBA008 New Method
	protected boolean getBundleSupportInd() throws NbaBaseException {
		return NbaConfiguration.getInstance().getProvider(getWork().getNbaLob().getReqVendor()).getBundle(); // ACN012
	}

	/**
	 * Create and initialize an <code>NbaVpmsResultsData</code> object to find any matching criteria.
	 * 
	 * @param entryPoint
	 *            the VP/MS model's entry point
	 * @return com.csc.fsg.nba.vo.NbaVpmsResultsData
	 */
	// NBA008 New Method //ALS4843 signature modified
	protected NbaVpmsResultsData getDataFromVpms(String entryPoint, NbaOinkDataAccess oinkData) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; // SPR3362
		try {
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS); // SPR3362
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); // SPR2639
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			// Begin NBA130
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(requirementInfo.getId());
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			// End NBA130
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			// SPR3362 code deleted
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Problem in getting data from VPMS", re);
			// SPR3362
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		// end SPR3362
	}

	// ALS4843 New Method
	protected NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException {
		return getDataFromVpms(entryPoint, oinkData);
	}

	/**
	 * Answer the MIB territory code
	 * 
	 * @param state
	 *            the state code
	 * @return MIB territory code for a state
	 */
	// NBA008 New Method
	protected long getMIBTerritoryCode(String state) throws NbaBaseException {
		if (state.equals("-1")) {
			return 0;
		}
		Map aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");
		NbaStatesData[] table = (NbaStatesData[]) getTableAccessor().getDisplayData(aCase, NbaTableConstants.NBA_STATES);
		if (table != null) {
			if (state != null && state.length() > 0) {
				for (int i = 0; i < table.length; i++) {
					if (String.valueOf(table[i].getStateCode()).compareToIgnoreCase(state) == 0) {// SPR1346
						return Long.parseLong(table[i].getMibTerritoryCode());
					}
				}
			}
		}
		return 0;

	}

	/**
	 * Answer the awd case and sources
	 * 
	 * @return NbaDst which represent a awd case
	 */
	// NBA008 New Method
	protected NbaDst getParentCase() throws NbaBaseException {
		if (parentCase == null) {
			// NBA213 deleted code
			// create and set parent case retrieve option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestSources();
			// get case from awd
			parentCase = retrieveWorkItem(getUser(), retOpt); // NBA213
			// remove original transaction
			parentCase.getTransactions().clear();
			// NBA213 deleted code

		}
		return parentCase;
	}

	/**
	 * Answer the provTransId. it retrieve provider transaction id from original work item requirement control source.
	 * 
	 * @return NbaDst which represent a awd case
	 */
	// NBA008 New Method
	protected String getProvTransId() throws NbaBaseException {
		if (provTransId == null) {
			NbaSource source = getWork().getRequirementControlSource();
			NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
			provTransId = reqSource.getRequirement().getProvTransId();
		}
		return provTransId; // SPR1183
	}

	/**
	 * Answer order suspend days so requirement will wait to recieve response from provider before wake up in ordered process.
	 * 
	 * @return number of suspend days.
	 * @param item
	 *            - the wotk item.
	 */
	// NBA008 New Method
	protected int getReqSuspendDays(NbaDst item) throws NbaBaseException {
		// call vpms to get suspend days
		// begin ALS4843
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(item.getNbaLob());
		oinkData.setContractSource(nbaTxLife); // NBA130
		int suspendDays = 0;
		try {
			List suspendDayList = getDataFromVpms(NbaVpmsAdaptor.EP_GET_SUSPEND_DAYS).getResultsData(); // NBA050 SPR1770
			// NBA050 BEGIN
			if (suspendDayList != null && suspendDayList.size() > 0) {
				suspendDays = Integer.parseInt(suspendDayList.get(0).toString());
			}
			if (isResetFollowUpDaysNeeded(item)) {
				return suspendDays;
			}
			return getFollowUpFrequency(item.getNbaLob().getReqUniqueID());
			// end ALS4843
			// NBA050 END
		} catch (NbaBaseException e) {
			throw new NbaVpmsException("Problem in getting suspend days from VPMS", e);
		}
		// NBA050 CODE DELETED
	}

	/**
	 * Answer the order requirement queue suspend minutes
	 * 
	 * @return the number of minuteswhich a work item in the order requirement queue may be suspeneded
	 */
	// NBA008 New Method
	protected int getSuspendMinute() throws NbaBaseException {
		String suspendMinutes = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
				NbaConfigurationConstants.ORDER_REQUIREMENT_SUSPEND_MINUTES); // ACN012
		if (suspendMinutes != null) { // ACN012
			return Integer.parseInt(suspendMinutes); // ACN012
		}
		return 0;

	}

	/**
	 * Answer the table accessor *
	 * 
	 * @return an instance of NbaTableAccessor
	 */
	// NBA008 New Method
	protected NbaTableAccessor getTableAccessor() throws NbaBaseException {
		if (ntsAccess == null) {
			ntsAccess = new NbaTableAccessor();
		}
		return ntsAccess;
	}

	/**
	 * Answer the transaction code. it retrieve transaction code from original work item requirement control source.
	 * 
	 * @return NbaDst which represent a awd case
	 */
	// NBA008 New Method
	protected String getTransactionCode() throws NbaBaseException {
		if (transCode == null) {
			NbaSource source = getWork().getRequirementControlSource();
			NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
			transCode = reqSource.getRequirement().getTransactionId();
		}
		return transCode;
	}

	/**
	 * Answer the translation text for an olife value
	 * 
	 * @param olifeValue
	 *            the olife value
	 * @param table
	 *            the table name
	 * @return translated text
	 */
	// NBA008 New Method
	protected String getTranslationText(String olifeValue, String table) throws NbaBaseException {
		NbaTableData[] data = getTableAccessor().getDisplayData(getWork(), table);
		for (int i = 0; i < data.length; i++) {
			if (data[i].code().equalsIgnoreCase(olifeValue)) {
				return data[i].text();
			}
		}
		throw new NbaBaseException("No Translation found");
	}

	/**
	 * Answer the work item queue
	 * 
	 * @return returns true if work item in the order requirement queue
	 * @param item
	 *            - the wotk item.
	 */
	// NBA008 New Method
	protected boolean isReqInOrderQueue(NbaDst item) {
		return item.getQueue().equals(getWork().getQueue());

	}

	/**
	 * Answer whether work item is suspended during current process
	 * 
	 * @return boolean
	 */
	// NBA008 New Method
	protected boolean isWorkSuspended() {
		return workSuspended;
	}

	/**
	 * This method first creates and adds XMLife message to the each work item. Then It call provider to transform list of XMLife message to the
	 * provider ready message. When provider ready message is receipt, it associates each work item with provider ready message as a source.Calls vpms
	 * to get pass status and suspend days. Also changes the bundle work items status to pass status
	 */
	// NBA008 New Method
	protected void orderRequirements() throws NbaBaseException {

		NbaVpmsResultsData data = null;// NBA250
		// 9001(Email) does not required provider ready transaction
		if (Long.parseLong(getTransactionCode()) == NbaOliConstants.TC_TYPE_EMAIL) {
			// Begin NBA250
			for (int i = 0; i < bundleReqArray.size(); i++) {
				NbaDst item = (NbaDst) bundleReqArray.get(i);
				RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(item.getNbaLob().getReqUniqueID());
				RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				oinkData = new NbaOinkDataAccess(item.getNbaLob());
				data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_FOLLOWUP_DAYS);
				if (String.valueOf(NbaOliConstants.OLI_REQSTAT_ORDER).equalsIgnoreCase(item.getNbaLob().getReqStatus())) {
					if (reqInfoExt != null && reqInfoExt.hasFollowUpDate()) {
						if (data.getResultsData() != null && data.getResultsData().size() > 0) {
							updateRequirementInfoExtension(data, reqInfoExt, item);// ALS4843
						}
					}
				} else if (String.valueOf(NbaOliConstants.OLI_REQSTAT_ADD).equalsIgnoreCase(item.getNbaLob().getReqStatus())
						|| String.valueOf(NbaOliConstants.OLI_REQSTAT_SUBMITTED).equalsIgnoreCase(item.getNbaLob().getReqStatus())) {
					NbaVpmsResultsData followupData = getDataFromVpms(NbaVpmsAdaptor.EP_NUMBER_FOLLOW_UPS);
					processFollowupRequest(followupData, reqInfoExt, data, item);// ALS4843
				}
			}
		}// End NBA250

		xmlifeList.add(createXMLifeTransaction(getWork()));
		for (int i = 0; i < bundleReqArray.size(); i++) {
			// create and add XMLife transaction to the xmlife array list
			xmlifeList.add(createXMLifeTransaction((NbaDst) bundleReqArray.get(i)));
		}

		HashMap reqMsg = null;
		// call provider to get provider ready message
		NbaProviderAdapterFacade facade = new NbaProviderAdapterFacade(getWork(), getUser());// ACN014
		reqMsg = (HashMap) facade.convertXmlToProviderFormat(new ArrayList(xmlifeList));
		// if original work item has error return here and set original work item status to fail status
		if (reqMsg.get(((NbaTXLife) xmlifeList.get(0)).getTransRefGuid()) != null) {
			addComment((String) reqMsg.get(((NbaTXLife) xmlifeList.get(0)).getTransRefGuid()));
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
			return;
		}

		// ALS5786
		NbaTXLife nbaTXlife = (NbaTXLife) xmlifeList.get(0);
		String partyId = nbaTXlife.getPartyId(getWork().getNbaLob().getReqPersonCode(), String.valueOf(getWork().getNbaLob().getReqPersonSeq()));
		if (getWork().getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_MVRPT
				&& (NbaUtils.isBlankOrNull(nbaTXlife.getParty(partyId).getDriverLicenseNum()) || nbaTXlife.getParty(partyId).getDriverLicenseState() == NbaOliConstants.OLI_TC_NULL)) {
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_MISSING_DRIVER);// APSL4165
		}
		// End ALS5786

		// ACN014 begin
		String xmlTrans = (String) reqMsg.get(NbaConstants.TRANSACTION);
		addXMLifeTrans(getWork(), xmlTrans);
		// Begin NBA250
		if (Long.parseLong(getTransactionCode()) == NbaOliConstants.TC_TYPE_EMAIL) {
			try {
				getWork().addNbaSource(new NbaSource(getWork().getBusinessArea(), NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION, xmlTrans));
			} catch (NbaBaseException e) {
				if (e instanceof NbaDataException) {
					addComment(e.getMessage());
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
					return;
				}
				throw e;
			}
		}// End NBA250
			// ACN014 end
			// check for all other bundled work items
		for (int i = 0; i < bundleReqArray.size(); i++) {
			if (reqMsg.get(((NbaTXLife) xmlifeList.get(i + 1)).getTransRefGuid()) != null) {
				// add manual comments
				NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
				npec.setActionAdd();
				npec.setOriginator(getUser().getUserID());
				npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
				npec.setProcess(getUser().getUserID());
				npec.setText((String) reqMsg.get(((NbaTXLife) xmlifeList.get(i + 1)).getTransRefGuid()));
				((NbaDst) bundleReqArray.get(i)).addManualComment(npec.convertToManualComment());
				changeStatus(((NbaDst) bundleReqArray.get(i)), getFailStatus()); // NBA097
			} else if (!(NbaOliConstants.TC_TYPE_EMAIL == Long.parseLong(getTransactionCode()))) { // NBA250
				addXMLifeTrans((NbaDst) bundleReqArray.get(i), xmlTrans);// ACN014
			}
		}

		handleHostResponse(doContractUpdate(nbaTxLife)); // ACN009

		NbaXMLDecorator prvTran = new NbaXMLDecorator();
		prvTran.addProviderRequest(Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED), 1, (String) reqMsg.get(NbaConstants.TRANSACTION));// ACN014
		if (getLogger().isDebugEnabled()) { // PERF-APSL479
			getLogger().logDebug("Provider ready transaction" + prvTran.toXmlString()); // PERF-APSL479
		}// PERF-APSL479

		NbaProcessStatusProvider newStatuses = null;
		for (int i = 0; i < bundleReqArray.size(); i++) {
			NbaDst item = (NbaDst) bundleReqArray.get(i);
			if (!item.getStatus().equals(getFailStatus())) { // if ststus is set to fail ststus leave the work item

				// change requirement status to submitted
				item.getNbaLob().setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED));
				item.getNbaLob().setReqOrderDate(requirementInfo.getRequestedDate()); // NBA130
				if (newStatuses == null) {
					// get pass status from vpms for bundled requirements
					try {
						newStatuses = new NbaProcessStatusProvider(getUser(), item, nbaTxLife); // SPR1715
					} catch (NbaBaseException e) {
						throw new NbaVpmsException("Problem in getting next status from VPMS", e);
					}
				}
				// change awd status to pass status from vpms
				changeStatus(item, newStatuses.getPassStatus()); // NBA097
				item.increasePriority(newStatuses.getWIAction(), newStatuses.getWIPriority()); // NBA020

				int suspendDays = getReqSuspendDays(item); // SPR1770
				if (suspendDays > 0) { // SPR1770
					// suspend work item to number of days from vpms
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(item.getID());
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(new Date());
					calendar.add(Calendar.DAY_OF_WEEK, suspendDays); // SPR1770
					Date reqSusDate = (calendar.getTime());
					suspendVO.setActivationDate(reqSusDate);
					if (item.isSuspended()) {
						// unsuspend first if suspended
						unsuspendList.add(suspendVO);
					}
					suspendList.add(suspendVO);
				} // SPR1770
			}
		}

	}

	/**
	 * This method process the bundle requirements as below: - First check whether work item in the order queue - if it is in order queue continue
	 * process other work items. - if not in order queue, check the requirement status - if requirement status is order and original work item is not
	 * previous suspended in order queue - suspend original work item for number of minutes from configuration file. - else leave the delinquent work
	 * item and continue process other work items.
	 * 
	 * @return true if original work item is not set for suspension and all bundle requirements are processed successfully.
	 */
	// New Method NBA008
	protected boolean processBundleRequirements() throws NbaBaseException {
		boolean suspended = false;
		NbaSource source = null;
		NbaXMLDecorator reqSource = null;
		// SPR3290 code deleted
		NbaDst item = null;
		Iterator iterateReq = bundleReqArray.iterator();
		while (iterateReq.hasNext()) {
			item = (NbaDst) iterateReq.next();
			if (isReqInOrderQueue(item)) {
				continue;
			}
			// if work item is not in order queue.
			if (item.getNbaLob().getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_ORDER))) {
				if (suspended == false) {
					// if requirement status is order
					// check for previous suspension only one time
					source = getWork().getRequirementControlSource();
					reqSource = new NbaXMLDecorator(source.getText());
					AutomatedProcess process = reqSource.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); // SPR2639
					// if requirement is previusly suspended in order process
					if (process != null && process.hasSuspendDate()) {
						suspended = true;
					} else {
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						suspendVO.setTransactionID(getWork().getID());
						GregorianCalendar calendar = new GregorianCalendar();
						Date currDate = new Date();
						calendar.setTime(currDate);
						calendar.add(Calendar.MINUTE, getSuspendMinute());
						suspendVO.setActivationDate(calendar.getTime());
						// set requirement source control for suspend days
						if (process == null) {
							process = new AutomatedProcess();
							process.setProcessId(NbaUtils.getBusinessProcessId(getUser())); // SPR2639
							reqSource.getRequirement().addAutomatedProcess(process);
						}
						addComment("Sibling requirements for Bundling are not in Order Queue"); // ACN014
						process.setSuspendDate(currDate);
						source.setText(reqSource.toXmlString());
						source.setUpdate();
						NbaRequirementUtils reqUtils = new NbaRequirementUtils(); // ACN014
						reqUtils.updateRequirementControlSource(getWork(), null, source.getText(), NbaRequirementUtils.actionUpdate); // ACN014
																																		// SPR2992
						suspendList.add(suspendVO);
						setWorkSuspended(true);
						return false;
					}
				}
			}
			// Begin NBA250
			if (item.getNbaLob().getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_ADD))
					|| item.getNbaLob().getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_SUBMITTED))) {
				String uniqueId = item.getNbaLob().getReqUniqueID();
				RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(uniqueId);
				RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				// The followup provider is PRDC
				if (reqInfoExtn != null
						&& NbaConstants.PROVIDER_PRODUCER.equalsIgnoreCase(reqInfoExtn.getTrackingInfo().getFollowUpServiceProvider())) {
					if (reqInfoExtn.getFollowUpDate() != null && reqInfoExtn.getFollowUpDate().getTime() <= System.currentTimeMillis()) {
						continue;
					}
				}
			}// End NBA250
				// remove work item from array if it is not in order queue and has requirement status as order
			iterateReq.remove();
		}
		return true;
	}

	/**
	 * Retrieve the original work item with sources
	 * 
	 */
	// NBA008 New Method
	protected void retrieveWork() throws NbaBaseException {
		// NBA213 deleted code
		// create and set retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.setLockWorkItem();
		retOpt.requestSources();
		setWork(retrieveWorkItem(getUser(), retOpt)); // NBA213
		unlockList.add(getWork());
		// NBA213 deleted code

	}

	/**
	 * set to true is work is suspended during current process
	 * 
	 * @param newWorkSuspended
	 *            boolean
	 */
	protected void setWorkSuspended(boolean newWorkSuspended) {
		workSuspended = newWorkSuspended;
	}

	/**
	 * suspend the work item in suspend list to AWD
	 */
	// NBA008 New Method
	protected void suspendAWD() throws NbaBaseException {
		// NBA213 deleted code
		for (int i = 0; i < suspendList.size(); i++) {
			suspendWork(getUser(), (NbaSuspendVO) suspendList.get(i)); // NBA213
		}
		// NBA213 deleted code
	}

	/**
	 * unlock the AWD.
	 */
	// NBA008 New Method
	protected void unlockAWD() throws NbaBaseException {
		// NBA213 deleted code
		// unlock all work items
		String originalID = getOrigWorkItem().getID(); // NBA213
		for (int i = 0; i < unlockList.size(); i++) {
			if (!originalID.equals(((NbaDst) unlockList.get(i)).getID())) { // NBA213
				unlockWork(getUser(), (NbaDst) unlockList.get(i)); // NBA213
			}
		}
		// NBA213 deleted code
	}

	/**
	 * unsuspend the work item in unsuspend list to AWD
	 */
	// NBA008 New Method
	public void unsuspendAWD() throws NbaBaseException {
		// NBA213 deleted code
		for (int i = 0; i < unsuspendList.size(); i++) {
			unsuspendWork(getUser(), (NbaSuspendVO) unsuspendList.get(i)); // NBA213
		}
		// NBA213 deleted code
	}

	/**
	 * update the work items to the AWD.
	 * 
	 * @param dstList
	 *            - A list of NbaDst objects.
	 */
	// NBA008 New Method
	protected void updateAWD(List dstList) throws NbaBaseException {
		// NBA213 deleted code
		for (int i = 0; i < dstList.size(); i++) {
			updateWork(getUser(), (NbaDst) dstList.get(i)); // NBA213
		}
		// NBA213 deleted code
	}

	/**
	 * Change the work item status to pass status. Update and unlock workitem.
	 * 
	 * @throws NbaBaseException
	 */
	// SPR1311 New Method
	protected void processManualRequirement() throws NbaBaseException {
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
	}

	/**
	 * Updates the RequirementInfoExtension for the RequirementInfo object
	 * 
	 * @param data
	 *            Contains the follow up frequency for the requirement
	 * @param reqInfoExt
	 *            RequirementInfoExtension which has to be updated
	 */
	// New Method NBA250 //ALS4843 signature changed
	private void updateRequirementInfoExtension(NbaVpmsResultsData data, RequirementInfoExtension reqInfoExt, NbaDst nbaDst) throws NbaBaseException {
		// Begin ALS4843
		int followupDays = Integer.parseInt((String) data.getResultsData().get(0));
		if (isResetFollowUpDaysNeeded(nbaDst)) {
			reqInfoExt.setFollowUpFreq(followupDays);
		}
		if (reqInfoExt.hasFollowUpFreq()) {
			followupDays = reqInfoExt.getFollowUpFreq(); // get the stored value or the latest updated value
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_WEEK, followupDays);
		reqInfoExt.setFollowUpDate(calendar.getTime());
		reqInfoExt.setActionUpdate();
		// End ALS4843
	}

	/**
	 * Process a Requirement follow-up. If the maximum number of follow-ups has not been exceeded, update the Requirement control source. Otherwise,
	 * cause the status of the work item to set to the failed status from the VPMS status model.
	 * 
	 * @throws NbaBaseException
	 */
	// New Method NBA250 //ALS4843 signature changed
	protected void processFollowupRequest(NbaVpmsResultsData followupData, RequirementInfoExtension reqInfoExt, NbaVpmsResultsData data, NbaDst nbaDst)
			throws NbaBaseException {
		int count = 0;
		try {
			resultData = followupData.getResultsData();
			if (resultData == null || resultData.size() == 0) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
			}
			count = Integer.parseInt((String) resultData.get(0));
		} catch (NumberFormatException e1) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS);
		}
		int followUpReqNumber = 0;
		if (null != reqInfoExt) {
			followUpReqNumber = reqInfoExt.getFollowUpRequestNumber();
		}
		if (followUpReqNumber < count) {
			// if maxmimum number of follow-ups has not been reached then
			if (reqInfoExt == null) {
				OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
				reqInfoExt = oliExt.getRequirementInfoExtension();
				reqInfoExt.setActionAdd();
				getRequirementInfo().addOLifEExtension(oliExt);
				getRequirementInfo().setActionUpdate();
			} else {
				reqInfoExt.setActionUpdate();
			}
			updateReqInfoExtn(reqInfoExt, data, nbaDst);// ALS4843
			handleHostResponse(doContractUpdate());
		}
	}

	/**
	 * Updates the RequirementInfoExtension for the RequirementInfo object
	 * 
	 * @param data
	 *            Contains the follow up frequency for the requirement
	 * @param reqInfoExt
	 *            RequirementInfoExtension which has to be updated
	 */
	// New Method NBA250 //ALS4843 signature changed
	protected void updateReqInfoExtn(RequirementInfoExtension reqInfoExt, NbaVpmsResultsData data, NbaDst nbaDst) throws NbaBaseException {

		// THIS if MEANS SECOND or MORE TIME FOLLOW-UP
		if (reqInfoExt.getFollowUpRequestNumber() > 0) {
			int followUpNumber = reqInfoExt.getFollowUpRequestNumber();
			followUpNumber++;
			reqInfoExt.setFollowUpRequestNumber(String.valueOf(followUpNumber));
		} else { // THIS else MEANS FIRST TIME FOLLOW-UP
			reqInfoExt.setFollowUpRequestNumber(1);
		}
		reqInfoExt.getTrackingInfo().setFollowUpCompleted(true);
		if (data.getResultsData() != null && data.getResultsData().size() > 0) {
			updateRequirementInfoExtension(data, reqInfoExt, nbaDst);// ALS4843
		}
		reqInfoExt.setActionUpdate();
	}

	/**
	 * Calls VP/MS to check if resetting of followup days needed or not.
	 * 
	 * @return True or False
	 * @throws NbaBaseException
	 */
	// ALS4843 new Method
	protected boolean isResetFollowUpDaysNeeded(NbaDst req) throws NbaBaseException {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(req.getNbaLob());
		NbaVpmsResultsData data = getDataFromVpms(NbaVpmsConstants.EP_IS_RESET_FOLLOWUP_DAYS_NEEDED, oinkData);
		if (data.getResultsData() != null && data.getResultsData().size() > 0) {
			String strResult = (String) data.getResultsData().get(0);
			if (strResult != null && !strResult.trim().equals("")) {
				return Boolean.valueOf(strResult).booleanValue();
			}
		}
		return false;
	}

	// APSL357 new method
	private Map getKeyMap() {
		Map params = new HashMap();
		params.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");
		params.put(NbaTableAccessConstants.C_SYSTEM_ID, "*");
		params.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");

		return params;

	}

}
