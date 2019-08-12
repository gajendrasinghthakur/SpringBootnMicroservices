package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.contract.merge.CopyBox;
import com.csc.fsg.nba.business.contract.merge.CopyManager;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaCompanionCaseControlData;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaAgentVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaGeneralComment;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.CompanionCase;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ArrDestination;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.AuthorizedSignatory;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.ClientAcknowledgeInfo;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.DistributionAgreementInfo;
import com.csc.fsg.nba.vo.txlife.DistributionAgreementInfoExtension;
import com.csc.fsg.nba.vo.txlife.DistributionChannelInfo;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.MedicalCertification;
import com.csc.fsg.nba.vo.txlife.MedicalCondition;
import com.csc.fsg.nba.vo.txlife.MedicalConditionExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SignatureInfoExtension;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementInfo;
import com.csc.fsg.nba.vo.txlife.UWReqFormsCC;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
import com.csc.fsg.nbaac.database.NbaAcImpSearchDataBaseAccessor;

/**
 * NbaProcAutoAppSubmit is the class to process Cases found in N2AUTAPP queue.It: - retrieves the child work items and sources - assigns a default
 * User ID if necesary <td>SR515492</td><td>Discretionary</td><td>E-App Integration</td> <td>CR1345594</td><td>Discretionary</td><td>Translation Table
 * and General Comment</td>
 * <tr>
 * <td>SR641590</td>
 * <td>Sub-BGA</td>
 * <td>Sub BGA Enhancements</td>
 * </tr>
 * <tr>
 * <td>APSL2735</td>
 * <td>Discretionary</td>
 * <td>Electronic Initial Premium</td>
 * </tr>
 * <tr>
 * <td>APSL2808</td>
 * <td>Discretionary</td>
 * <td>Simplified Issue</td>
 * </tr>
 * <tr>
 * <td>APSL3447</td>
 * <td>Discretionary</td>
 * <td>HVT</td>
 * </tr>
 * </tr> </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaProcAutoAppSubmit extends com.csc.fsg.nba.business.process.NbaAutomatedProcess {

	/** Holds list of validation errors. */
	protected List validationErrors = new Vector();

	protected List subAccoutErrors = new ArrayList(); // APSL2510

	protected String missingSubAccout = EMPTY_STRING; // APSL3352

	private NbaTXLife txLife = null;

	private NbaTXLife agentTxLife = null;

	private NbaOLifEId nbaOLifEId = null;

	private static final String RETRIEVE_AGENT_DEMOGRAPHICS_BP = "RetrieveAgentDemographicsTxLifeBP";

	NbaXML103SubmitPolicyHelper submitPolicyHelper = new NbaXML103SubmitPolicyHelper();// QC7936

	/**
	 * SR515492 New Method NbaProcAppSubmit constructor comment.
	 */
	public NbaProcAutoAppSubmit() {
		super();
	}

	/**
	 * SR515492 New Method - Perform XML Processing to update Agent Demographics details.
	 * 
	 * @throws NbaBaseException
	 */
	protected void doProcess() throws NbaBaseException {
		setNbaOLifEId(new NbaOLifEId(getTxLife()));
		List relationList = getTxLife().getOLifE().getRelation();
		int size = relationList.size();
		setFormNameOnSupplementForm();
		updateTXLife(getTxLife());
		getWork().updateXML103Source(getTxLife()); //  Updating 103 as If Agent WS fail Then it is not upadting Other Data.
		for (int i = 0; i < size; i++) {
			Relation aRelation = (Relation) relationList.get(i);
			long relationRoleCode = aRelation.getRelationRoleCode();
			if (relationRoleCode == NbaOliConstants.OLI_REL_PRIMAGENT || relationRoleCode == NbaOliConstants.OLI_REL_ADDWRITINGAGENT
					|| relationRoleCode == NbaOliConstants.OLI_REL_GENAGENT || relationRoleCode == NbaOliConstants.OLI_REL_SUBORDAGENT) { // SR641590
				// SUB-BGA
				String partyId = aRelation.getRelatedObjectID();
				NbaParty party = getTxLife().getParty(partyId);
				Party agParty = party.getParty();
				setAgentTxLife(retrieveAgentDemographics(agParty, relationRoleCode));
				updateAgentParty(agParty, relationRoleCode);
			}
		}
		getNbaOLifEId().resetIds(getTxLife());
		getNbaOLifEId().resolveDuplicateIds(getTxLife());
		getNbaOLifEId().resetOwnerOtherInsRelationId(getTxLife()); // ALII1878
		getWork().updateXML103Source(getTxLife());
		validateRetailAgent();
	}
	/**
	 * APSL5313- This method is used for validating Company and agent.
	 */
	private void validateRetailAgent() {
		NbaParty primaryAgentParty = getTxLife().getWritingAgent();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getTxLife().getPolicy());
		boolean setErrorReason = false;
		if (null != policyExtension && NbaOliConstants.OLI_DISTCHAN_10 == policyExtension.getDistributionChannel()
				&& NbaUtils.isEquitablePaperApplicable(getTxLife().getPolicy().getApplicationInfo().getSignedDate())) {
			Address agentAddress = primaryAgentParty.getAddress(NbaOliConstants.OLI_ADTYPE_BUS);
			if (null != agentAddress && NbaOliConstants.OLI_USA_NY == agentAddress.getAddressStateTC()
					&& COMPANY_MLOA.equalsIgnoreCase(getTxLife().getPolicy().getCarrierCode())) {
				setErrorReason = true;
			} else if (null != agentAddress && NbaOliConstants.OLI_USA_NY != agentAddress.getAddressStateTC()
					&& COMPANY_AA.equalsIgnoreCase(getTxLife().getPolicy().getCarrierCode())
					&& NbaOliConstants.OLI_USA_PR != Long.valueOf(getTxLife().getPolicy().getJurisdiction())
					&& NbaOliConstants.OLI_USA_NY != Long.valueOf(getTxLife().getPolicy().getJurisdiction())
					&& NbaUtils.isValidMLOAPlan(String.valueOf(getTxLife().getPolicy().getProductCode()))) {
				setErrorReason = true;
			}
		}
		if (setErrorReason) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			addComment(EQUITABLE_PAPER);
		}
	}
	/**
	 * SR515492 New Method Retrieve Agent Demographics details by calling procuder Web Service.
	 * 
	 * 
	 * @param aParty
	 * @param relationRoleCode
	 * @return
	 * @throws NbaBaseException
	 */
	protected NbaTXLife retrieveAgentDemographics(Party aParty, long relationRoleCode) throws NbaBaseException {
		NbaLob nbaLob = getWork().getNbaLob();
		AxaProducerVO producer = createAXAProducerSearchVO(aParty, relationRoleCode, nbaLob);
		AxaAgentVO agentVO = new AxaAgentVO(producer);
		agentVO.setNbaUserVO(getUser());
		// Begin APSL3286,QC#12229
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SEARCHPRODUCER, agentVO.getNbaUserVO(),
				null, null, producer);
		NbaTXLife txLife = (NbaTXLife) webServiceInvoker.execute();
		if (txLife != null) {
			Party party = txLife.getOLifE().getPartyAt(0);
			if (!NbaUtils.isBlankOrNull(party)) {
				Producer producerObj = party.getProducer();
				if (!NbaUtils.isBlankOrNull(producerObj)) {
					String companyProducerID = producerObj.getCarrierAppointmentAt(0).getCompanyProducerID();
					producer.setCompanyProducerID(companyProducerID);
					agentVO.setProducerVO(producer);
				}
			}
		}// End APSL3286,QC#12229
		AccelResult accelResult = (AccelResult) ServiceHandler.invoke(RETRIEVE_AGENT_DEMOGRAPHICS_BP, ServiceContext.currentContext(), agentVO);
		if (accelResult.hasErrors()) {
			try {// APSL3874
				WorkflowServiceHelper.checkOutcome(accelResult);
			} catch (NbaBaseException ex) {// APSL3874
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, ex.getMessage());// APSL3874
			}// APSL3874

		} else {
			return (NbaTXLife) accelResult.getFirst();
		}
		return null;
	}

	/**
	 * SR515492 New Method Add the DistributionChannelInfo tag in XML.
	 * 
	 * @param producer
	 * @throws NbaBaseException
	 */
	protected void updateAgentDistributionChannelInfo(Producer producer) throws NbaBaseException {
		if (producer.getCarrierAppointmentCount() > 0) {
			CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
			if (null != carrierAppointment) {
				DistributionChannelInfo distributionChannelInfo = null; // APSL4507
				if (carrierAppointment.getDistributionChannelInfoCount() == 0) {
					distributionChannelInfo = new DistributionChannelInfo();
					carrierAppointment.addDistributionChannelInfo(distributionChannelInfo);
				}
				distributionChannelInfo = carrierAppointment.getDistributionChannelInfoAt(0);
				distributionChannelInfo.setDistributionChannel(getWork().getNbaLob().getDistChannel());
				// APSL4507 starts
				if (getWork().getNbaLob().getMarketingInd() != null) {
					distributionChannelInfo.setDistributionChannelName(NbaUtils.getDistributionChannelName(Long.parseLong(getWork().getNbaLob().getMarketingInd())));
				}
				// APSL4507 ends
			}
		}
	}	

	/**
	 * APSL3140 New Method Add AdvancingAllowedIndCode to the DistributionAgreementInfoExtension tag in XML.
	 * 
	 * @param producer
	 * @throws NbaBaseException
	 */
	protected void updateAgentDistributionAgreementInfo(Producer producer) throws NbaBaseException {
		if (producer.getCarrierAppointmentCount() > 0) {
			CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
			if (null != carrierAppointment) {
				if (carrierAppointment.getDistributionAgreementInfoCount() > 0) {
					DistributionAgreementInfo distributionAgreementInfo = carrierAppointment.getDistributionAgreementInfoAt(0);
					if (distributionAgreementInfo != null && distributionAgreementInfo.hasAdvancingAllowedInd()) {
						DistributionAgreementInfoExtension distributionAgreementInfoExtension = NbaUtils
								.getFirstDistributionAgreementInfoExtension(distributionAgreementInfo);
						if (distributionAgreementInfoExtension != null) {
							distributionAgreementInfoExtension.setActionUpdate();
						} else {
							OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_DISTRIBUTIONAGREEMENTINFO);
							distributionAgreementInfo.addOLifEExtension(olifeExtension);

							distributionAgreementInfoExtension = olifeExtension.getDistributionAgreementInfoExtension();
							if (distributionAgreementInfoExtension != null) {
								distributionAgreementInfoExtension.setActionAdd();
							}
						}

						if (distributionAgreementInfo.getAdvancingAllowedInd()) {
							distributionAgreementInfoExtension.setAdvancingAllowedIndCode(NbaOliConstants.NBA_ANSWERS_YES);
						} else {
							distributionAgreementInfoExtension.setAdvancingAllowedIndCode(NbaOliConstants.NBA_ANSWERS_NO);
						}
					}
				}
			}
		}
	}

	/**
	 * SR515492 New Method Update the Agent Party details in XML using Agent search web service response.
	 * 
	 * @param agParty
	 * @throws NbaBaseException
	 */
	public void updateAgentParty(Party agParty, long relationRoleCode) throws NbaBaseException {
		// primary writing Agent
		if (String.valueOf(NbaOliConstants.OLI_PT_PERSON).equals(String.valueOf(agParty.getPartyTypeCode()))
				&& getWork().getNbaLob().getDistChannel() == NbaOliConstants.OLI_DISTCHAN_6) {
			String partyId = getAgentTxLife().getPartyId(relationRoleCode);
			if (partyId == null) {
				Relation relation = getAgentTxLife().getRelationForRelationRoleCode(relationRoleCode);
				partyId = relation.getOriginatingObjectID();
			}
			if (partyId != null) {// QC8154
				NbaParty agentParty = getAgentTxLife().getParty(partyId);
				Party pwaParty = agentParty.getParty();
				agParty.setFullName(pwaParty.getFullName());
				agParty.setPersonOrOrganization(pwaParty.getPersonOrOrganization());
				agParty.setAddress(pwaParty.getAddress());
				agParty.setPhone(pwaParty.getPhone());
				agParty.setProducer(pwaParty.getProducer());
				agParty.setEMailAddress(pwaParty.getEMailAddress());
			}
			updateAgentDistributionChannelInfo(agParty.getProducer());
			setNbaOLifEId(new NbaOLifEId(getTxLife()));
			// Methode to create General Agent Party
			constructValueObjectForGeneralAgentParty(getNbaOLifEId(), getTxLife().getOLifE(), agParty.getId());

		} else if (String.valueOf(NbaOliConstants.OLI_PT_PERSON).equals(String.valueOf(agParty.getPartyTypeCode()))
				&& getWork().getNbaLob().getDistChannel() == NbaOliConstants.OLI_DISTCHAN_10) {

			String partyId = getAgentTxLife().getPartyId(relationRoleCode);
			if (partyId == null) {
				Relation relation = getAgentTxLife().getRelationForRelationRoleCode(relationRoleCode);
				partyId = relation.getOriginatingObjectID();
				if (partyId != null) {
					NbaParty agentParty = getAgentTxLife().getParty(partyId);
					Party pwaParty = agentParty.getParty();
					if(NbaUtils.isBlankOrNull(agParty.getFullName())){//APSL4925
						agParty.setFullName(pwaParty.getFullName());//APSL4925
					}
					agParty.setPersonOrOrganization(pwaParty.getPersonOrOrganization());
					agParty.setAddress(pwaParty.getAddress());
					agParty.setProducer(pwaParty.getProducer());
					agParty.setAddress(pwaParty.getAddress());
					agParty.setPhone(pwaParty.getPhone());
					agParty.setEMailAddress(pwaParty.getEMailAddress());
				}
			}
			updateAgentDistributionAgreementInfo(agParty.getProducer()); // APSL3140
			updateAgentDistributionChannelInfo(agParty.getProducer());
		} else if (String.valueOf(NbaOliConstants.OLI_PT_ORG).equals(String.valueOf(agParty.getPartyTypeCode()))
				&& getWork().getNbaLob().getDistChannel() == NbaOliConstants.OLI_DISTCHAN_6) {
			String partyId = getAgentTxLife().getPartyId(relationRoleCode);// SR641590 SUB-BGA
			if (partyId == null) {
				Relation relation = getAgentTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
				partyId = relation.getOriginatingObjectID();
			}
			NbaParty agentParty = getAgentTxLife().getParty(partyId);
			Party pwaParty = agentParty.getParty();
			if(NbaUtils.isBlankOrNull(agParty.getFullName())){//APSL4925
				agParty.setFullName(pwaParty.getFullName());//APSL4925
			}
			agParty.setPersonOrOrganization(pwaParty.getPersonOrOrganization());
			agParty.setAddress(pwaParty.getAddress());
			agParty.setProducer(pwaParty.getProducer());
			updateAgentDistributionChannelInfo(agParty.getProducer());
			agParty.setAddress(pwaParty.getAddress());
			agParty.setPhone(pwaParty.getPhone());
			agParty.setEMailAddress(pwaParty.getEMailAddress());
			if (relationRoleCode == NbaOliConstants.OLI_REL_SUBORDAGENT) { // SR641590 SUB-BGA
				constructValueObjectForEntityGeneralAgentParty(getNbaOLifEId(), getTxLife().getOLifE(), agParty.getId());
			}
		}
	}

	/**
	 * SR515492 New Method Construct value object for general agent Party.
	 * 
	 * @param olifeId
	 * @param olife
	 * @param id
	 */
	public void constructValueObjectForGeneralAgentParty(NbaOLifEId olifeId, NbaContractVO olife, String id) {
		long relationRoleCode = NbaOliConstants.OLI_REL_PROCESSINGFIRM; // APSL3447
		String partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_PROCESSINGFIRM); // APSL3447
		if (partyId == null) { // APSL3447
			partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_GENAGENT);
			relationRoleCode = NbaOliConstants.OLI_REL_GENAGENT; // APSL3447
		}
		if (partyId != null) {// QC8154
			Party aParty = new Party();
			aParty.setActionAdd();
			olifeId.setId(aParty);
			((OLifE) olife).addParty(aParty);
			OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
			aParty.addOLifEExtension(oLifeExt);
			PartyExtension partyExt = oLifeExt.getPartyExtension();
			partyExt.setActionAdd();
			NbaParty party = getAgentTxLife().getParty(partyId);
			Party agParty = party.getParty();
			mapPartyAttribute(aParty, agParty);
			Relation aRelation = (Relation) constructValueObjectForRelation(olifeId, olife);
			aRelation.setOriginatingObjectID(id);
			aRelation.setRelatedObjectID(aParty.getId());
			aRelation.setRelationRoleCode(relationRoleCode); // APSL3447
			constructValueObjectForSubFirmAgentParty(olifeId, getTxLife().getOLifE(), id); // APSL3447
			// APSL3447 removed construct methode for superior Agent
		}
	}

	/**
	 * SR515492 New Method Construct value object for entity general agent Party.
	 * 
	 * @param olifeId
	 * @param olife
	 * @param id
	 */
	public void constructValueObjectForEntityGeneralAgentParty(NbaOLifEId olifeId, NbaContractVO olife, String id) {
		String partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_GENAGENT);
		if (partyId != null) {// QC8154
			Party aParty = new Party();
			aParty.setActionAdd();
			olifeId.setId(aParty);
			((OLifE) olife).addParty(aParty);
			OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
			aParty.addOLifEExtension(oLifeExt);
			PartyExtension partyExt = oLifeExt.getPartyExtension();
			partyExt.setActionAdd();
			NbaParty party = getAgentTxLife().getParty(partyId);
			Party agParty = party.getParty();
			mapPartyAttribute(aParty, agParty);
			Relation aRelation = (Relation) constructValueObjectForRelation(olifeId, olife);
			aRelation.setOriginatingObjectID(id);
			aRelation.setRelatedObjectID(aParty.getId());
			aRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);

		}
	}

	/**
	 * SR641590 SUB-BGA New Method. construct value object for Sub-Firm agent party.
	 * 
	 * @param olifeId
	 * @param olife
	 * @param id
	 */
	public void constructValueObjectForSubFirmAgentParty(NbaOLifEId olifeId, NbaContractVO olife, String id) {
		String partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_SUBORDAGENT);
		if (partyId != null) {
			Party aParty = new Party();
			aParty.setActionAdd();
			olifeId.setId(aParty);
			((OLifE) olife).addParty(aParty);
			OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
			aParty.addOLifEExtension(oLifeExt);
			PartyExtension partyExt = oLifeExt.getPartyExtension();
			partyExt.setActionAdd();

			NbaParty party = getAgentTxLife().getParty(partyId);
			Party agParty = party.getParty();
			mapPartyAttribute(aParty, agParty);
			Relation aRelation = (Relation) constructValueObjectForRelation(olifeId, olife);
			aRelation.setOriginatingObjectID(id);
			aRelation.setRelatedObjectID(aParty.getId());
			aRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_SUBORDAGENT);
		}
	}

	/**
	 * SR515492 New Method. construct value object for superior agent party.
	 * 
	 * @param olifeId
	 * @param olife
	 * @param id
	 */
	public void constructValueObjectForSuperiorAgentParty(NbaOLifEId olifeId, NbaContractVO olife, String id) {
		String partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_SUPERIORAGENT);
		if (partyId != null) {// QC8154
			Party aParty = new Party();
			aParty.setActionAdd();
			olifeId.setId(aParty);
			((OLifE) olife).addParty(aParty);
			OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PARTY);
			aParty.addOLifEExtension(oLifeExt);
			PartyExtension partyExt = oLifeExt.getPartyExtension();
			partyExt.setActionAdd();

			NbaParty party = getAgentTxLife().getParty(partyId);
			Party agParty = party.getParty();
			mapPartyAttribute(aParty, agParty);
			Relation aRelation = (Relation) constructValueObjectForRelation(olifeId, olife);
			aRelation.setOriginatingObjectID(id);
			aRelation.setRelatedObjectID(aParty.getId());
			aRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_SUPERIORAGENT);
		}
	}

	/**
	 * SR515492 New Method construct value object for relation.
	 * 
	 * @param olifeId
	 * @param olife
	 * @return
	 */
	public NbaContractVO constructValueObjectForRelation(NbaOLifEId olifeId, NbaContractVO olife) {
		Relation aRelation = new Relation();
		aRelation.setActionAdd();
		olifeId.setId(aRelation);
		((OLifE) olife).addRelation(aRelation);
		aRelation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		aRelation.setOriginatingObjectType(NbaOliConstants.OLI_PARTY);
		OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RELATION);
		aRelation.addOLifEExtension(oLifeExt);
		RelationExtension relationExt = oLifeExt.getRelationExtension();

		relationExt.setActionAdd();
		return aRelation;
	}

	/**
	 * SR515492 New Method Map the agent party
	 * 
	 * @param partyBean
	 * @param party
	 */
	public void mapPartyAttribute(Party partyBean, Party party) {
		if (partyBean != null && party != null) {// 8154
			partyBean.setPartyTypeCode(party.getPartyTypeCode());
			partyBean.setFullName(party.getFullName());
			partyBean.setPrefComm(party.getPrefComm());
			partyBean.setPersonOrOrganization(party.getPersonOrOrganization());
			partyBean.setAddress(party.getAddress());
			partyBean.setProducer(party.getProducer());
			partyBean.setPriorName(party.getPriorName());
		}
	}

	/**
	 * Perform the Application Auto Aplication business process: - Retrieve the child work items and sources.
	 * 
	 * @param user
	 *            the NbaUser for whom the process is being executed
	 * @param work
	 *            a NbaDst value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		try {
			if (!initializeFields(user, work)) {
				return getResult();
			}
			setTxLife(getWork().getXML103Source());// APSL3262
			updateAchCwaLOBInXML103(); // ALII2735
			// Call business validation routine
			doXML103Validation();
			if (getValidationErrors() != null && getValidationErrors().size() > 0) {
				writeErrorsInAWD();
				if (getResult() == null) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, formatErrorText(), getFailStatus()));
					// "EAPPINCMP"));
				}
			}
			if (getResult() == null) {

				doProcess();
			}
			
			if (getResult() == null) {

				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
		} catch (NbaBaseException ex) {
			// code deleted APSL3874
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, ex.getMessage());// APSL3874
		}

		// APSL2510 Begin Iterate through SubAccoutErrors

		if (getSubAccoutErrors().size() > 0) {
			List missingtranslations = getSubAccoutErrors();
			String error = formatSubAccoutErrors(missingtranslations);
			// Code deleted for APSL4149
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_ACCOUNT, error); // APSL4149
		}

		// APSL2510 End

		changeStatus(getResult().getStatus());
		// APSL5204 Begin
		if (!NbaUtils.isBlankOrNull(getStatusProvider().getReason())) {
			addComment(getStatusProvider().getReason());
		}
		// APSL5204 End
		// Update the Work Item with it's new status and update the work item in AWD
		doUpdateWorkItem();
		return getResult();
	}

	/**
	 * Retrieve and lock the child sources. Perform default initialization and initialize miscellaneous fields.
	 * 
	 * @param newUser
	 * @param newWork
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaNetServerDataNotFoundException
	 * @throws NbaNetServerException
	 */
	protected boolean initializeFields(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException, NbaNetServerDataNotFoundException,
			NbaNetServerException {
		NbaDst expandedWork;
		// Retrieve and lock the child sources.
		NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
		newWork.setUpdate();
		aNbaAwdRetrieveOptionsVO.setWorkItem(newWork.getID(), true);
		aNbaAwdRetrieveOptionsVO.requestSources();
		aNbaAwdRetrieveOptionsVO.setLockWorkItem();
		expandedWork = retrieveWorkItem(newUser, aNbaAwdRetrieveOptionsVO);
		// Now continue with the expanded NbaDst
		return super.initialize(newUser, expandedWork);
	}

	/**
	 * This methode set the supplement form name based on provider from number.
	 * 
	 * @throws NbaBaseException
	 */
	public void setFormNameOnSupplementForm() throws NbaBaseException {
		List formInstanceList = getTxLife().getOLifE().getFormInstance();
		Iterator itr = formInstanceList.iterator();
		while (itr.hasNext()) {
			FormInstance formInstance = (FormInstance) itr.next();
			if (SYSTEMATIC_FORM.equalsIgnoreCase(formInstance.getFormName())) { // APSL2735
				formInstance.setFormName(FORM_NAME_SYSPAY);
			} else if (FORM_NAME_NOTICEANDCONCENT.equalsIgnoreCase(formInstance.getFormName())) { // NBLXA-1850
				formInstance.setFormName(FORM_NAME_NOTICEANDCONCENT);
			} 
			else {
				String formNumber = formInstance.getProviderFormNumber();
				String formName = getFormNameForFormNumber(formNumber);
				formInstance.setFormName(formName);
			}
		}
	}

	/**
	 * Get the form name using form nuber from VPMS.
	 * 
	 * @param fromNumber
	 * @return
	 * @throws NbaBaseException
	 */
	public String getFormNameForFormNumber(String fromNumber) throws NbaBaseException {
		Map deOinkMap = new HashMap(2, 1);
		NbaVpmsAdaptor rulesProxy = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(getWork().getNbaLob());
			rulesProxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			deOinkMap.put("A_HOAppFormNumber", fromNumber);
			rulesProxy.setSkipAttributesMap(deOinkMap);
			rulesProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_FORMNAME_TYPE);

			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			VpmsComputeResult rulesProxyResult = rulesProxy.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List rulesList = vpmsResultsData.getResultsData();
				if (!rulesList.isEmpty()) {
					String returnStr = (String) rulesList.get(0);
					if (!NbaUtils.isBlankOrNull(returnStr)) {
						return returnStr;
					}
				}
			}
			return null;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {

			if (rulesProxy != null) {
				try {
					rulesProxy.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED); // SPR3362
				}
			}
		}
	}

	/**
	 * Create the producer object with demographics request information.
	 * 
	 * @param producerInfoTable
	 * @return
	 */
	public AxaProducerVO createAXAProducerSearchVO(Party aParty, long relationRoleCode, NbaLob nbaLob) throws NbaBaseException {
		AxaProducerVO producerVO = new AxaProducerVO();
		String distChannel = Long.toString(nbaLob.getDistChannel());
		producerVO.setTransType(NbaOliConstants.TC_TYPE_PARTYSRCH);
		producerVO.setOperator(NbaOliConstants.LOGICAL_OPERATOR_AND);
		producerVO.setTransSubType(NbaOliConstants.TC_SUBTYPE_PRODUCER_SEARCH);
		if (!NbaUtils.isBlankOrNull(aParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID())) {
			producerVO.addCriteria(NbaOliConstants.OLI_CARRIERAPPOINTMENT, CarrierAppointment.$COMPANY_PRODUCER_ID, aParty.getProducer()
					.getCarrierAppointmentAt(0).getCompanyProducerID(), NbaOliConstants.OLI_OP_EQUAL);
		}
		if (!NbaUtils.isBlankOrNull(distChannel)) {
			producerVO.addCriteria(NbaOliConstants.OLI_DISTRIBUTIONCHANNELINFO, DistributionChannelInfo.$DISTRIBUTION_CHANNEL, distChannel,
					NbaOliConstants.OLI_OP_EQUAL);
		}
		producerVO.addCriteria(NbaOliConstants.OLI_RELATION, Relation.$RELATION_ROLE_CODE, String.valueOf(relationRoleCode),
				NbaOliConstants.OLI_OP_EQUAL);

		producerVO.setPartyTypeCode(String.valueOf(aParty.getPartyTypeCode()));
		if (aParty.getProducer().getCarrierAppointmentCount() > 0) {
			producerVO.setCompanyProducerID(aParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID());
			producerVO.setCarrierApptTypeCode(String.valueOf(aParty.getProducer().getCarrierAppointmentAt(0).getCarrierApptTypeCode()));
		}

		producerVO.setOperator(NbaOliConstants.OLI_OPER_AND);
		producerVO.setCarriearAptSysKey(new ArrayList());
		producerVO.setRelationRoleCode(String.valueOf(relationRoleCode));
		producerVO.setDistributionChannel(String.valueOf(nbaLob.getDistChannel()));
		return producerVO;
	}

	/**
	 * @return Returns the txLife.
	 */
	public NbaTXLife getTxLife() {
		return txLife;
	}

	/**
	 * @param txLife
	 *            The txLife to set.
	 */
	public void setTxLife(NbaTXLife txLife) {
		this.txLife = txLife;
	}

	/**
	 * @return Returns the agentTxLife.
	 */
	public NbaTXLife getAgentTxLife() {
		return agentTxLife;
	}

	/**
	 * @param agentTxLife
	 *            The agentTxLife to set.
	 */
	public void setAgentTxLife(NbaTXLife agentTxLife) {
		this.agentTxLife = agentTxLife;
	}

	/**
	 * This method writes the validations errors as comments in AWD.
	 * 
	 */
	protected void writeErrorsInAWD() {
		addComments(getValidationErrors());
	}

	/**
	 * @return Returns the validationErrors.
	 */
	public List getValidationErrors() {
		return validationErrors;
	}

	/**
	 * @param validationErrors
	 *            The validationErrors to set.
	 */
	public void setValidationErrors(List validationErrors) {
		this.validationErrors = validationErrors;
	}

	/**
	 * format the error text
	 * 
	 * @return
	 */
	private String formatErrorText() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = validationErrors.iterator(); iter.hasNext();) {
			if (sb.length() > 0) {
				sb.append("~");
			}
			sb.append(iter.next());
		}
		return sb.toString();

	}

	/**
	 * Do business validation. If validation fails, add the errors as AWD comment. Change the status to INCAPPERRD and exit
	 */

	protected void doXML103Validation() throws NbaBaseException {
		NbaXML103Validation xml103Validation = new NbaXML103Validation();
		setValidationErrors(xml103Validation.doXML103Validation(getWork().getNbaCase()));
	}

	/**
	 * @return Returns the nbaOLifEId.
	 */
	public NbaOLifEId getNbaOLifEId() {
		return nbaOLifEId;
	}

	/**
	 * @param nbaOLifEId
	 *            The nbaOLifEId to set.
	 */
	public void setNbaOLifEId(NbaOLifEId nbaOLifEId) {
		this.nbaOLifEId = nbaOLifEId;
	}

	/**
	 * Updates the Txlife values
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 * @throws NbaBaseException
	 */

	public void updateTXLife(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			mergeForm(nbATXLife, NbaConstants.FORM_NAME_REPLNY);// APSL1720
			mergeForm(nbATXLife, NbaConstants.FORM_NAME_FINSUPII);// QC7936
			translateNbaValues(nbATXLife);// QC7936
			updateCoverageDetails(nbATXLife);
			updateCovoptionDetails(nbATXLife); //APSL5263
			updateSignatureDetails(nbATXLife);
			updatePartyDetails(nbATXLife);
			updateClientAcknowledgeDetails(nbATXLife);
			updateRelationDetails(nbATXLife);
			updateOtherInsuranceInfo(nbATXLife);// QC9291
			updateSubFirmIndicator(nbATXLife);// SR641590 SUB-BGA
			updateSubBgaRelationRoleCode(nbATXLife);// SR641590 SUB-BGA
			translateProductfullnameToProductcode(nbATXLife);// APSL2510
			removeDuplicateFunds(nbATXLife);// QC9590
			removeOldFunds(nbATXLife);// APSL3352
			updateSystematicSignature(nbATXLife); // APSL2735
			updateBankingInformation(nbATXLife); // APSL2735
			updateLifeExtension(nbATXLife); //APSL4761
			createCompanionCaseInformation(nbATXLife); // APSL4636
		}
	}

	/**
	 * Updates the Coverage Details
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */

	public void updateCoverageDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			try {
				Policy policy = nbATXLife.getPrimaryHolding().getPolicy();
				if (policy != null) {
					// APSL2562 QC#10093
					NbaTableAccessor nta = new NbaTableAccessor();
					NbaPlansData nbaPlansData = nta.getPlanData(nta.setupTableMap(getWork()));
					if (!NbaUtils.isBlankOrNull(nbaPlansData) && !NbaUtils.isBlankOrNull(nbaPlansData.getCovKeyTranslation())) {
						policy.setPlanName(nbaPlansData.getCovKeyTranslation());
						policy.setActionUpdate();
					}
					String productCode = policy.getProductCode();
					String planName = policy.getPlanName();
					Coverage coverage = nbATXLife.getPrimaryCoverage();
					coverage.setCurrentAmt(nbATXLife.getLife().getFaceAmt());// QC7768
					coverage.setProductCode(productCode);
					coverage.setPlanName(planName);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Process the eSignature and copy it to the various sections of the application
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */

	public void updateSignatureDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			updateInsuredSignature(nbATXLife);
			updateOwnerSignature(nbATXLife);
			updateProducerSignature(nbATXLife);
		}
	}

	/**
	 * Process the eSignature and copy it to the various sections of the application
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */
	// APSL2808 method refactored
	public void updateInsuredSignature(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			NbaParty nbaParty = nbATXLife.getPrimaryParty();
			//if (appInfo != null && nbaParty != null && appInfo.getApplicationType() != NbaOliConstants.OLI_APPTYPE_CONVERSIONNEW) { // APSL2650
			if (appInfo != null && nbaParty != null){ //APSL4568 //APSL4690	
			SignatureInfo insuredESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(),
						NbaOliConstants.OLI_PARTICROLE_PRIMARY);// QC8291
				if (insuredESign != null) {
					appInfo.setSignedDate(insuredESign.getSignatureDate());

					// Process Application Signature
					SignatureInfo appSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY,
							NbaOliConstants.OLI_SIGTYPE_APPSIG);
					getSubmitPolicyHelper().updateSignatureInfo(insuredESign, appSign);
					// Begin QC8291 - Copy the Signed City, State and Date from Owner App Sign to the Insured App Sign
					NbaParty ownerParty = nbATXLife.getPrimaryOwner();
					if (ownerParty != null) {
						SignatureInfo ownerESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(),
								ownerParty.getParty().getId(), NbaOliConstants.OLI_PARTICROLE_OWNER);// QC8291
						if (ownerESign != null) {
							appSign.setSignatureDate(ownerESign.getSignatureDate());
							appSign.setSignatureCity(ownerESign.getSignatureCity());
							appSign.setSignatureState(ownerESign.getSignatureState());
						}
						// End QC8291
						appInfo.addSignatureInfo(appSign);

						// Process HIPPA Signature
						SignatureInfo hipaaSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY,
								NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH);
						getSubmitPolicyHelper().updateSignatureInfo(insuredESign, hipaaSign);
						appInfo.addSignatureInfo(hipaaSign);
						ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
						if (appInfoExt != null) {
							TempInsAgreementInfo tempInsAgInfo = appInfoExt.getTempInsAgreementInfo();
							if (tempInsAgInfo != null && tempInsAgInfo.hasTIACashAmt() && tempInsAgInfo.getTIACashAmt() > 0) {
								SignatureInfo tiaSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM,
										NbaOliConstants.OLI_PARTICROLE_PRIMARY, NbaOliConstants.OLI_SIGTYPE_TIA);
								getSubmitPolicyHelper().updateSignatureInfo(insuredESign, tiaSign);
								appInfo.addSignatureInfo(tiaSign);
							}
						}

						// Begin APSL1720
						// Process Replacement NY Sign
						SignatureInfo replacementNYSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_REPLNY,
								NbaOliConstants.OLI_PARTICROLE_PRIMARY, NbaOliConstants.OLI_SIGTYPE_RPLNY);
						getSubmitPolicyHelper().updateSignatureInfo(insuredESign, replacementNYSign);
						List formInstanceList = getFormInstances(nbATXLife, FORM_NAME_REPLNY);
						if (!formInstanceList.isEmpty()) {
							FormInstance formInstance = (FormInstance) formInstanceList.get(0);
							formInstance.addSignatureInfo(replacementNYSign);
						}
						// End APSL1720
						appInfo.removeSignatureInfo(insuredESign);
					}
				}
			}
		}
	}

	/**
	 * Process the eSignature and copy it to the various sections of the application
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */
	// APSL2808 Method refactored
	public void updateProducerSignature(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			NbaParty nbaParty = nbATXLife.getWritingAgent();
			if (appInfo != null && nbaParty != null) {
				SignatureInfo producerESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(),
						NbaOliConstants.OLI_PARTICROLE_PRIMAGENT);// QC8291
				if (producerESign != null) {
					// Process Producer Client Profile signature
					SignatureInfo prodClientProfileSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM,
							NbaOliConstants.OLI_PARTICROLE_PRIMAGENT, NbaOliConstants.OLI_SIGTYPE_PRODCLNTSIG);
					getSubmitPolicyHelper().updateSignatureInfo(producerESign, prodClientProfileSign);
					appInfo.addSignatureInfo(prodClientProfileSign);

					// Process Producer Client Profile signature
					// Begin QC8022
					SignatureInfo prodAppSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMAGENT,
							NbaOliConstants.OLI_SIGTYPE_PRODAPPSIG);
					getSubmitPolicyHelper().updateSignatureInfo(producerESign, prodAppSign);
					appInfo.addSignatureInfo(prodAppSign);
					// End QC8022

					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
					if (appInfoExt != null) {
						TempInsAgreementInfo tempInsAgInfo = appInfoExt.getTempInsAgreementInfo();
						if (tempInsAgInfo != null && tempInsAgInfo.hasTIACashAmt() && tempInsAgInfo.getTIACashAmt() > 0) {
							SignatureInfo tiaSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM,
									NbaOliConstants.OLI_PARTICROLE_PRIMAGENT, NbaOliConstants.OLI_SIGTYPE_TIA);
							getSubmitPolicyHelper().updateSignatureInfo(producerESign, tiaSign);
							appInfo.addSignatureInfo(tiaSign);
						}
					}

					// Process Replacement NY Sign
					// Begin APSL1720
					SignatureInfo replacementNYSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_REPLNY,
							NbaOliConstants.OLI_PARTICROLE_PRIMAGENT, NbaOliConstants.OLI_SIGTYPE_RPLNY);
					getSubmitPolicyHelper().updateSignatureInfo(producerESign, replacementNYSign);
					List formInstanceList = getFormInstances(nbATXLife, FORM_NAME_REPLNY);
					if (!formInstanceList.isEmpty()) {
						FormInstance formInstance = (FormInstance) formInstanceList.get(0);
						formInstance.addSignatureInfo(replacementNYSign);
					}
					// End APSL1720
					appInfo.removeSignatureInfo(producerESign);
				}
			}
		}
	}

	/**
	 * Process the eSignature and copy it to the various sections of the application
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */
	// APSL2808 Method refactored
	public void updateOwnerSignature(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			NbaParty nbaParty = nbATXLife.getPrimaryOwner();
			if (appInfo != null && nbaParty != null) {
				SignatureInfo ownerESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(),
						NbaOliConstants.OLI_PARTICROLE_OWNER);// QC8291
				if (ownerESign != null) {
					SignatureInfo appSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_OWNER,
							NbaOliConstants.OLI_SIGTYPE_APPSIG);
					getSubmitPolicyHelper().updateSignatureInfo(ownerESign, appSign);
					appInfo.addSignatureInfo(appSign);
					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
					if (appInfoExt != null) {
						TempInsAgreementInfo tempInsAgInfo = appInfoExt.getTempInsAgreementInfo();
						if (tempInsAgInfo != null && tempInsAgInfo.hasTIACashAmt() && tempInsAgInfo.getTIACashAmt() > 0) {
							SignatureInfo tiaSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_OWNER,
									NbaOliConstants.OLI_SIGTYPE_TIA);
							getSubmitPolicyHelper().updateSignatureInfo(ownerESign, tiaSign);
							appInfo.addSignatureInfo(tiaSign);
						}
					}
					// For Term Conversion application, copy Owner signature to Insured signature
					// APSL2650 Begin
					//if (appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_CONVERSIONNEW) {
						if (appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_CONVERSIONNEW ||appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_CONVOPAIAD ) { //APSL4568 
						NbaParty insuredParty = nbATXLife.getPrimaryParty();
						SignatureInfo insuredAppSign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(),
								insuredParty.getParty().getId(), NbaOliConstants.OLI_PARTICROLE_PRIMARY);
						if (insuredAppSign == null) {
							insuredAppSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY,
									NbaOliConstants.OLI_SIGTYPE_APPSIG);
							appInfo.addSignatureInfo(insuredAppSign);
						}
						getSubmitPolicyHelper().updateSignatureInfo(ownerESign, insuredAppSign);
						insuredAppSign.setSignaturePartyID(insuredParty.getParty().getId());
						appInfo.setSignedDate(ownerESign.getSignatureDate());
					}
					// APSL2650 End
				}

				// Process Replacement NY Sign
				// Begin APSL1720
				SignatureInfo replacementNYSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_REPLNY, NbaOliConstants.OLI_PARTICROLE_OWNER,
						NbaOliConstants.OLI_SIGTYPE_RPLNY);
				getSubmitPolicyHelper().updateSignatureInfo(ownerESign, replacementNYSign);
				List formInstanceList = getFormInstances(nbATXLife, FORM_NAME_REPLNY);
				if (!formInstanceList.isEmpty()) {
					FormInstance formInstance = (FormInstance) formInstanceList.get(0);
					formInstance.addSignatureInfo(replacementNYSign);
				}
				// End APSL1720
				//Start APSL4507
				SignatureInfo ePolicyAckSigInfo =NbaUtils.getSignatureInfo(appInfo, NbaOliConstants.OLI_PARTICROLE_OWNER, NbaOliConstants.OLI_SIGTYPE_PRIVAGREE);
				if (ePolicyAckSigInfo != null) {
					SignatureInfo ePolicyAckSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_OWNER,
							NbaOliConstants.OLI_SIGTYPE_PRIVAGREE);
					getSubmitPolicyHelper().updateSignatureInfo(ePolicyAckSigInfo, ePolicyAckSign);
					appInfo.addSignatureInfo(ePolicyAckSign);
				}
				//End APSL4507  
				//Begin APSL4569 
				 ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				 long epolicycheck=appInfoExt.getReqPolicyDeliverMethod();
				 NbaParty OwnerEpolicyparty = nbATXLife.getPrimaryParty();
				 if(NbaOliConstants.OLI_POLDELMETHOD_EMAIL==epolicycheck)
				 {
					 SignatureInfo OwnerEpolicySign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(),
							 OwnerEpolicyparty.getParty().getId(), NbaOliConstants.OLI_PARTICROLE_OWNER,NbaOliConstants.OLI_SIGTYPE_PRIVAGREE);
					 if(OwnerEpolicySign==null){
						 OwnerEpolicySign = getSubmitPolicyHelper().createSignatureInfo(NbaConstants.LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_OWNER,
								NbaOliConstants.OLI_SIGTYPE_PRIVAGREE);
					 appInfo.addSignatureInfo(OwnerEpolicySign);
					 }
					 getSubmitPolicyHelper().updateSignatureInfo(ownerESign, OwnerEpolicySign);
					 OwnerEpolicySign.setSignaturePartyID(OwnerEpolicyparty.getParty().getId());
					}
				//End APSL4569  
				appInfo.removeSignatureInfo(ownerESign);
			}
		}
	}

	/**
	 * Updates the Party Details
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 * @throws NbaBaseException
	 */
	// QC7979
	public void updatePartyDetails(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			updateInsuredDetails(nbATXLife);
			updateOwnerDetails(nbATXLife);
		}
	}

	// APSL2808 Methods deleted from here and moved to NbaXML103SubmitPolicyHelper.java

	/**
	 * Updates the ClientAcknowledgeInfo details
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */

	public void updateClientAcknowledgeDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			List uwReqFormList = new ArrayList();  // NBLXA-2199
			NbaParty nbaParty = nbATXLife.getPrimaryParty();
			Client client = nbaParty.getParty().getClient();
			ClientExtension clientExtension = NbaUtils.getClientExtension(client);
			if (clientExtension == null) {
				OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_CLIENT);
				clientExtension = oliExt.getClientExtension();
				client.addOLifEExtension(oliExt);
			}
			ClientAcknowledgeInfo clientAckInfo = clientExtension.getClientAcknowledgeInfo();
			if (clientAckInfo == null) {
				clientAckInfo = new ClientAcknowledgeInfo();
				clientExtension.setClientAcknowledgeInfo(clientAckInfo);
				getNbaOLifEId().setId(clientAckInfo);
			}
			clientAckInfo.setProductInfoType(getSubmitPolicyHelper().getProductInfoType(nbATXLife.getPolicy().getProductType(),
					nbATXLife.getPolicy().getProductCode())); // APSL2432, APSL2808
			UWReqFormsCC uwReqFormsCC = clientAckInfo.getUWReqFormsCC();
			if (uwReqFormsCC == null) {
				uwReqFormsCC = new UWReqFormsCC();
				clientAckInfo.setUWReqFormsCC(uwReqFormsCC);
			}else { // Begin NBLXA-2199
				if(uwReqFormsCC.getUWReqFormsTCCount() >0){
					//UWReqFormsCC uwReqForms = null;
					for (int t=0 ; t<uwReqFormsCC.getUWReqFormsTCCount();t++  ){
						uwReqFormList.add(""+uwReqFormsCC.getUWReqFormsTCAt(t));
					}
				}
			}// End NBLXA-2199
			// checking the forms value in UWReqFormsTC list and adding form only if it is not there in the existing list  NBLXA-2199
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_OWNER)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_OWNER_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_OWNER_QUEST);
			}
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_FRTS)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_FRTI_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_FRTI_QUEST);
			}
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_MEDSUP)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_MEDICAL_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_MEDICAL_QUEST);
			}
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_FINSUPII)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_FINANCIAL_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_FINANCIAL_QUEST);
			}
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_CTR)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_CTIR_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_CTIR_QUEST);
			}
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_SUBUSAGE)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_SUBSTANCE_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_SUBSTANCE_QUEST);
			}
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_AVISUP)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_AVIATION_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_AVIATION_QUEST);
			}
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_AVOSUP)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_AVOCATION_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_AVOCATION_QUEST);
			}
			// Begin APSL3646/QC13059
			if (isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_LTCSUPP)&& !(uwReqFormList.contains(""+NbaOliConstants.AXA_UWREQFORMS_LTCSR_QUEST))) {
				uwReqFormsCC.addUWReqFormsTC(NbaOliConstants.AXA_UWREQFORMS_LTCSR_QUEST);
			}
			// End APSL3646/QC13059
			ApplicationInfo appInfo = nbATXLife.getNbaHolding().getApplicationInfo();
			SignatureInfo insuredSignature = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(),
					NbaOliConstants.OLI_PARTICROLE_PRIMARY);// QC8291, APSL2808
			if (insuredSignature != null && appInfo != null) {
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if (appInfoExt == null) {
					OLifEExtension lifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
					appInfo.addOLifEExtension(lifeExtension);
					appInfoExt = lifeExtension.getApplicationInfoExtension();
				}
				appInfoExt.setProposedInsuredInd(true);
			}
		}
	}

	// APSL2808 Method deleted from here and moved to NbaXML103SubmitPolicyHelper.java

	/**
	 * Checks if a form is available in nbA or not.
	 * 
	 * @param nbATXLife
	 *            nbATXLife, in which the form presence needs to be saerched.
	 * @param formName
	 *            Form Name which needs to be searched.
	 */
	private boolean isFormAvailable(NbaTXLife nbATXLife, String formName) {
		if (nbATXLife != null && formName != null) {
			List formInstanceList = nbATXLife.getOLifE().getFormInstance();
			for (int i = 0; i < formInstanceList.size(); i++) {
				if (formName.equals(((FormInstance) formInstanceList.get(i)).getFormName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * merge the form Instance Details
	 * 
	 * @param nbATXLife
	 * @param formName
	 * @throws NbaBaseException
	 */
	// New Method APSL1720
	public void mergeForm(NbaTXLife nbATXLife, String formName) throws NbaBaseException {
		if (nbATXLife != null && formName != null) {
			List formInstanceList = getFormInstances(nbATXLife, formName);
			if (formInstanceList.size() > 1) {
				// Take the first FormInstance
				FormInstance mergedForm = (FormInstance) formInstanceList.get(0);
				FormInstance formInstance = null;
				// Loop through the remaining formInstances and merge them in the first one. Remove this formInstances after merging them with the
				// First FormInstance
				for (int i = 1; i < formInstanceList.size(); i++) {
					formInstance = (FormInstance) formInstanceList.get(i);
					mergeFormInstance(mergedForm, formInstance);
					nbATXLife.getOLifE().removeFormInstance(formInstance);
				}
			}
		}
	}

	/**
	 * Retrieves the formInstance with the matching formName.
	 * 
	 * @param nbATXLife
	 *            nbATXLife, in which the form presence needs to be saerched.
	 * @param formName
	 *            Form Name which needs to be searched.
	 */
	// New Method APSL1720
	private List getFormInstances(NbaTXLife nbATXLife, String formName) {
		List matchingForms = new ArrayList();
		if (nbATXLife != null && formName != null) {
			List formInstanceList = nbATXLife.getOLifE().getFormInstance();
			for (int i = 0; i < formInstanceList.size(); i++) {
				if (formName.equals(((FormInstance) formInstanceList.get(i)).getFormName())) {
					matchingForms.add(formInstanceList.get(i));
				}
			}
		}
		return matchingForms;
	}

	/**
	 * merge the form Instance Details
	 * 
	 * @param nbATXLife
	 * @throws NbaBaseException
	 */
	// New Method APSL1720
	public void mergeFormInstance(FormInstance formInstanceTo, FormInstance formInstanceFrom) throws NbaBaseException {
		if (formInstanceTo != null && formInstanceFrom != null) {
			CopyBox copyBox = CopyManager.getCopyBox(NbaOliConstants.OLI_FORMINSTANCE);
			copyBox.copy(formInstanceTo, formInstanceFrom);
			if (formInstanceFrom.getAttachmentCount() > 0) {
				formInstanceTo.setAttachment(formInstanceFrom.getAttachment());
			}
			if (formInstanceFrom.getFormResponseCount() > 0) {
				formInstanceTo.setFormResponse(formInstanceFrom.getFormResponse());
			}
		}
	}

	/**
	 * Updates the Relation Details. Sets the RelatedRefID value for the Relation objects.
	 * 
	 * @param nbATXLife
	 *            The nbATXLife
	 */
	// New Method QC7891
	public void updateRelationDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			Relation relation = null;
			List allRelations = nbATXLife.getOLifE().getRelation();
			for (int i = 0; i < allRelations.size(); i++) {
				relation = (Relation) allRelations.get(i);
				NbaUtils.setRelatedRefId(relation, allRelations);
			}
		}
	}

	/**
	 * Updates the Insured Details
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 * @throws NbaBaseException
	 */
	// New Method QC7979
	public void updateInsuredDetails(NbaTXLife nbATXLife) throws NbaBaseException {
		//Hippa
		Party party=null;
		//Begin APSL4464(QC15850)
		if (nbATXLife.getJointParty() != null) {
			party = nbATXLife.getJointParty().getParty();
			if (!nbATXLife.isSIApplication()) {
				createHipaaParty(nbATXLife, party);
			}
		}
		//End APSL4464(QC15850)
		if (nbATXLife.getPrimaryParty() != null) {
			party = nbATXLife.getPrimaryParty().getParty();
			//Begin APSL4464(QC15850)
			if (!nbATXLife.isSIApplication()) {
				createHipaaParty(nbATXLife, party);
			}
			//End APSL4464(QC15850)
			// Update Email Address Type
			EMailAddress emailAddress = null;
			for (int k = 0; k < party.getEMailAddressCount(); k++) {
				emailAddress = party.getEMailAddressAt(k);
				if (!emailAddress.hasEMailType()) {
					emailAddress.setEMailType(NbaOliConstants.OLI_EMAIL_BUSINESS);
				}
			}
			Risk risk = party.getRisk();
			RiskExtension riskExt = NbaUtils.getFirstRiskExtension(risk);
			if (riskExt != null && !riskExt.getMedicalCertification().isEmpty()) {
				MedicalCertification medicalCert = riskExt.getMedicalCertificationAt(0);
				if (medicalCert.hasInsCompanyName()) {
					String encodedCompanyName = getSubmitPolicyHelper().encodeReplacementCompany(medicalCert.getInsCompanyName()); // APSL2808
					if (encodedCompanyName != null) {
						medicalCert.setInsCompanyName(encodedCompanyName);
					}
				}
			}
			// Begin APSL4782
			if (!NbaUtils.isBlankOrNull(risk) && isFormAvailable(nbATXLife, NbaConstants.FORM_NAME_MEDSUP )){
				boolean medCond9BPresent = false;
				if (risk.getMedicalConditionCount()>0){
					for (int index = 0; index < risk.getMedicalConditionCount(); index++) {
						MedicalCondition medCon = risk.getMedicalConditionAt(index);
						MedicalConditionExtension medConExt =NbaUtils.getFirstMedicalConditionExtension(medCon);
						if(!NbaUtils.isBlankOrNull(medConExt) &&  medConExt.hasQuestionNumber() && medConExt.getQuestionNumber().equalsIgnoreCase("9b")){
							medCond9BPresent = true;
							break;
						}
					}
				}
				if(!medCond9BPresent){
					NbaAcImpSearchDataBaseAccessor object = new NbaAcImpSearchDataBaseAccessor();
					String condDesc = null;
					try {
						condDesc = object.getConditionDescription("LAST 5 YEARS" , false);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MedicalCondition medCon = new MedicalCondition();
					medCon.setConditionType(NbaOliConstants.OLI_MEDCOND_Q9B);
					medCon.setConditionDescription(condDesc);
					risk.addMedicalCondition(medCon);
					medCon.setActionAdd();
					OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_MEDICALCONDITION);
					medCon.addOLifEExtension(olifeExtension);
					olifeExtension.setVendorCode(NbaOliConstants.CSC_VENDOR_CODE);
					olifeExtension.setActionAdd();
					MedicalConditionExtension medExtn = olifeExtension.getMedicalConditionExtension();
					if (medExtn != null) {//APSL2253
						medExtn.setQuestionNumber(NbaConstants.MEDSUPP_QUESTION_NUMBER_9);
						medExtn.setRecommendTreatmentIndCode(NbaOliConstants.NBA_RecommendedTreatInd);
						medExtn.setQuestionText(condDesc);
						medExtn.setRiskType(NbaOliConstants.NBA_RiskType);
						medExtn.setActionAdd();
					}
				}
			}
			//End APSL4782
			updateInsuranceCompanyName(nbATXLife);// QC7923
			updateLTCInsuranceCompanyName(nbATXLife); // APSL2935
			// Begin QC7923
			// Update Occupation
			Person person = party.getPersonOrOrganization().getPerson();
			if (person != null && person.hasOccupation()) {// QC8033
				String translatedOccupation = getSubmitPolicyHelper().getTranslatedNbaValue(OCCUPATION, person.getOccupation());
				if (!NbaUtils.isBlankOrNull(translatedOccupation)) {
					person.setOccupation(translatedOccupation);
				}
				// Begin QC7927
				String occupation = getSubmitPolicyHelper().encodeOlifeValue(person.getOccupation().toUpperCase(), NBA_OCCUPATION); // APSL2808
				if (NbaUtils.isBlankOrNull(occupation)) {
					if (party.getEmploymentCount() > 0) {
						// If occupation does not match with nba value, set the occupation as 'Other' and 'Other occupation' as the value coming from
						// iPipeline
						Employment employment = party.getEmploymentAt(0);
						employment.setOccupation(person.getOccupation());
					}
					occupation = NbaOliConstants.NBA_OCCUPATION_OTHER;
				}
				person.setOccupation(occupation);
				// End QC7927
			}
			// End QC7923
			// Begin CR1345594
			PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
			if (personExt != null) {
				if (personExt.hasRateClassAppliedFor()) {
					String translatedRateClass = getSubmitPolicyHelper().getTranslatedNbaValue(RATE_CLASS_APPLIED_FOR,
							personExt.getRateClassAppliedFor());
					if (!NbaUtils.isBlankOrNull(translatedRateClass)) {
						personExt.setRateClassAppliedFor(translatedRateClass);
						getWork().addManualComment(getGeneralComment(APPLIED_FOR_RATING_DEFAULTING_MSG));// QC8273
					}
				}else if (NbaUtils.isTermConvOPAICase(nbATXLife)) { // APSL5047
					String rateClass = getRateClass(person);
					if (!NbaUtils.isBlankOrNull(rateClass)) {
						personExt.setRateClassAppliedFor(rateClass);
						personExt.setActionUpdate();
					}
				} 
			}
			// End CR1345594
		}

	}
	
	/**
	 * Updates the Owner Details
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */
	// New Method QC7979
	public void updateOwnerDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null && nbATXLife.getPrimaryOwner() != null) {
			Party party = nbATXLife.getPrimaryOwner().getParty();
			// Update Email Address Type
			EMailAddress emailAddress = null;
			for (int k = 0; k < party.getEMailAddressCount(); k++) {
				emailAddress = party.getEMailAddressAt(k);
				if (!emailAddress.hasEMailType()) {
					emailAddress.setEMailType(NbaOliConstants.OLI_EMAIL_BUSINESS);
				}
			}
			// BEGIN QC9565
			Organization organization = nbATXLife.getPrimaryOwner().getOrganization();
			OrganizationExtension orgExtn = NbaUtils.getFirstOrganizationExtension(organization);
			if (party.hasPersonOrOrganization() && party.getPersonOrOrganization().getOrganization() != null) {
				NbaParty poaParty = nbATXLife.getParty(nbATXLife.getPartyId(NbaOliConstants.OLI_REL_POWEROFATTRNY));
				if (poaParty != null) {
					if (NbaOliConstants.OLI_ORG_TRUST == party.getPersonOrOrganization().getOrganization().getOrgForm()
							&& !NbaUtils.isBlankOrNull(poaParty.getPerson().getTitle())) {
						if (orgExtn != null) {
							orgExtn.setApplicantTitlePresentInd(true);
						}
					}
				}
			}
			// END QC9565
		}
	}

	// APSL2808 Methods deleted from here and moved to NbaXML103SubmitPolicyHelper.java

	/**
	 * Updates the Insurance Company Name to Index Value
	 * 
	 * @param nbATXLife
	 *            The nbATXLife object
	 * @throws NbaBaseException
	 * @throws NbaBaseException
	 */
	// New Method QC7923
	public void updateInsuranceCompanyName(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			Map relations = nbATXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_HOLDINGCO);
			Iterator entries = relations.entrySet().iterator();
			while (entries.hasNext()) {
				Entry thisEntry = (Entry) entries.next();
				Relation relation = (Relation) thisEntry.getValue();
				if (relation.getRelatedObjectType() == NbaOliConstants.OLI_PARTY) {
					NbaParty nbaParty = nbATXLife.getParty(relation.getRelatedObjectID());
					if (nbaParty.getParty().hasFullName()) {
						nbaParty.getParty().setPartyKey(getSubmitPolicyHelper().encodeReplacementCompany(nbaParty.getParty().getFullName())); // APSL2808
						nbaParty.getOrganization().setDBA(nbaParty.getParty().getFullName());
					}
				}
			}
		}
	}

	// Begin APSL2935
	public void updateLTCInsuranceCompanyName(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			Map relations = nbATXLife.getAllRelationsForRole(NbaOliConstants.OLI_REL_HEALTHCO);
			Iterator entries = relations.entrySet().iterator();
			while (entries.hasNext()) {
				Entry thisEntry = (Entry) entries.next();
				Relation relation = (Relation) thisEntry.getValue();
				if (!NbaUtils.isBlankOrNull(relation.getRelatedObjectID())) {
					String relatedObjectId = relation.getRelatedObjectID();
					Holding holding = nbATXLife.getHolding(relatedObjectId);
					if (holding != null) {
						Policy policy = holding.getPolicy();
						if (policy != null)
							policy.setCarrierCode(getSubmitPolicyHelper().encodeReplacementCompany(policy.getShortName()));
					}
				}
			}
		}
	}

	// End APSL2935

	// APSL2808 Method deleted from here and moved to NbaXML103SubmitPolicyHelper.java

	/**
	 * Translates the Financial Supplement Question numbers from iPipeLine quesion numbers to nbA question numbers.
	 * 
	 * @param nbATXLife
	 *            the nbATXLife object
	 * @throws NbaBaseException
	 */
	// QC7936 New Method
	public void translateNbaValues(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			// Translate the Question Numbers in Financial Supplement Form from iPipeLine question number to the nbA question number.
			List formInstanceList = getFormInstances(nbATXLife, FORM_NAME_FINSUPII);
			if (!formInstanceList.isEmpty()) {
				FormInstance formInstance = (FormInstance) formInstanceList.get(0);
				if (!NbaUtils.isBlankOrNull(formInstance.getProviderFormNumber())) {
					List formResponses = formInstance.getFormResponse();
					for (int i = 0; i < formResponses.size(); i++) {
						FormResponse formResponse = formInstance.getFormResponseAt(i);
						formResponse.setQuestionNumber(getSubmitPolicyHelper().getTranslatedNbaValue(formInstance.getProviderFormNumber(),
								formResponse.getQuestionNumber()));
					}
				}
			}
		}
	}

	/**
	 * @return Returns the submitPolicyHelper.
	 */
	// QC7936 New Method
	public NbaXML103SubmitPolicyHelper getSubmitPolicyHelper() {
		return submitPolicyHelper;
	}

	/**
	 * @param submitPolicyHelper
	 *            The submitPolicyHelper to set.
	 */
	// QC7936 New Method
	public void setSubmitPolicyHelper(NbaXML103SubmitPolicyHelper submitPolicyHelper) {
		this.submitPolicyHelper = submitPolicyHelper;
	}

	/**
	 * Creates a GeneralComment object
	 * 
	 * @param commentText
	 * @return Comment
	 */
	// New Method QC8273
	protected Comment getGeneralComment(String commentText) throws NbaBaseException {
		NbaGeneralComment generalComment = new NbaGeneralComment();
		generalComment.setEnterDate(getTimeStamp(getUser()));
		generalComment.setText(commentText);
		generalComment.setOriginator(getUser().getUserID());
		generalComment.setActionAdd();
		return generalComment.convertToManualComment();
	}

	// QC9291(APSL2272) New Method
	public void updateOtherInsuranceInfo(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			List holdingList = nbATXLife.getOLifE().getHolding();
			int holdingCount = holdingList.size();
			NbaParty insuranceCompanyParty = null;
			long replacementType = NbaOliConstants.OLI_REPTY_NONE;
			for (int i = 1; holdingCount > 1 && i < holdingCount; i++) {
				Holding holding = (Holding) holdingList.get(i);
				HoldingExtension holdEx = NbaUtils.getFirstHoldingExtension(holding);
				if (!OLI_HOLD_DATAREP.equalsIgnoreCase(holding.getDataRep())
						&& (holdEx == null || (NbaOliConstants.OLI_HOLDSUBTYPE_TERMCONV != holdEx.getHoldingSubType() && NbaOliConstants.OLI_HOLDSUBTYPE_OPAI != holdEx
								.getHoldingSubType()))) {
					PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(holding.getPolicy());
					if (policyExt != null && policyExt.getReplacementIndCode() == NbaOliConstants.NBA_ANSWERS_YES) {
						if (replacementType != NbaOliConstants.OLI_REPTY_INTERNAL) {
							Relation holdingCompanyRelation = nbATXLife.getRelationForRoleAndOriginatingID(holding.getId(),
									NbaOliConstants.OLI_REL_HOLDINGCO);
							if (holdingCompanyRelation != null) {
								insuranceCompanyParty = nbATXLife.getParty(holdingCompanyRelation.getRelatedObjectID());
								if (insuranceCompanyParty != null) {
									replacementType = getSubmitPolicyHelper().evalReplacementType(insuranceCompanyParty.getParty().getPartyKey(),
											holding.getPolicy().getReplacementType());
									//Start APSL5343 
									holding.getPolicy().setReplacementType(replacementType); 
									holding.getPolicy().setActionUpdate();
									//End APSL5343 
								}
							}
						}
						constructHoldingToHoldingRelation(holding, NbaOliConstants.OLI_REL_REPLACEDBY);
					}
				}
			}
			nbATXLife.getPolicy().setReplacementType(replacementType);
			// Begin APSL2602 APSL2573
			if (nbATXLife.getLife() != null) {
				LifeUSA lifeUSA = getLifeUSA(nbATXLife.getLife(), true);
				LifeUSAExtension lifeUSAExtension = getLifeUSAExtension(lifeUSA, true);
				lifeUSAExtension.setExchange1035Ind(nbATXLife.isExch1035IndCodePresent());
			}
			// End APSL2602 APSL2573
		}
	}

	// APSL2808 Method deleted from here and moved to NbaXML103SubmitPolicyHelper.java

	/**
	 * Constructs the Holding to Holding Relation object. The Primary Holding objects acts as the Originating Object and the Holding Object acts as
	 * the Related Object
	 * 
	 * @param relatedObj
	 *            the Related Holding Object
	 * @param roleCode
	 *            the Relation Role Code for Relation object
	 * @return Relation object
	 */
	// QC9291(APSL2272) New Method
	public Relation constructHoldingToHoldingRelation(Holding relatedObj, long roleCode) {
		Holding originatingObj = getTxLife().getPrimaryHolding();
		Relation relation = getTxLife().getRelation(originatingObj.getId(), relatedObj.getId(), roleCode);
		if (relation == null) {
			relation = getTxLife().createRelation(originatingObj, relatedObj, roleCode);
			getNbaOLifEId().setId(relation);
		}
		return relation;
	}

	/**
	 * SR641590 SUB-BGA Set subFirmIndicator if SBGA Lob is present on case
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */

	public void updateSubFirmIndicator(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (appInfoExt != null && !NbaUtils.isBlankOrNull(getWork().getNbaLob().getSubBrokerGeneralAgency())) {
				appInfoExt.setSubFirmIndicator(true);
			}
		}
	}

	/**
	 * SR641590 SUB-BGA Set Sub-Firm relation role code in Sub-Firm Relation.
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */

	public void updateSubBgaRelationRoleCode(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ArrayList subFirmRelations = nbATXLife.getOLifE().getRelation();
			for (int i = 0; subFirmRelations != null && i < subFirmRelations.size(); i++) {
				Relation subFirmRelation = (Relation) subFirmRelations.get(i);
				if (subFirmRelation != null && !subFirmRelation.isActionDelete()
						&& subFirmRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_GENAGENT) {
					NbaParty subfirmParty = nbATXLife.getParty(subFirmRelation.getRelatedObjectID());
					if (subfirmParty != null && subfirmParty.getParty().getProducer() != null
							&& subfirmParty.getParty().getProducer().getCarrierAppointmentCount() > 0) {
						CarrierAppointment carAppt = subfirmParty.getParty().getProducer().getCarrierAppointmentAt(0);
						if (carAppt != null && !NbaUtils.isBlankOrNull(carAppt.getCompanyProducerID())
								&& !NbaUtils.isBlankOrNull(getWork().getNbaLob().getSubBrokerGeneralAgency())
								&& getWork().getNbaLob().getSubBrokerGeneralAgency().equalsIgnoreCase(carAppt.getCompanyProducerID())) {
							subFirmRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_SUBORDAGENT);
						}
					}
				}
			}
		}
	}

	// Begin APSL3352
	public void removeOldFunds(NbaTXLife nbaTXLife) {
		String toBeDeletedSubAccounts = getMissingSubAccout();
		removeArrangmentDestination(nbaTXLife, toBeDeletedSubAccounts);
		removeSubAccounts(nbaTXLife, toBeDeletedSubAccounts);

	}

	// End APSL3352

	// New Method QC9590
	public void removeDuplicateFunds(NbaTXLife nbaTXLife) {
		Holding primaryHolding = nbaTXLife.getPrimaryHolding();
		Investment investment = primaryHolding.getInvestment();
		Map prodCodeToSubAcctID = new HashMap();
		List toBeDeletedSubAccounts = new ArrayList();
		if (investment != null) {
			List subAccounts = investment.getSubAccount();
			// Iterate through the subaccounts and create a map from product code to subaccount IDs
			for (int i = 0; i < subAccounts.size(); i++) {
				SubAccount subAccount = (SubAccount) subAccounts.get(i);
				if (subAccount.hasProductCode()) {
					// If subaccount product code already exists in the map, then add the SubAccount ID in the existing list.
					// else create a new arraylist and add the SubAccount ID in the new list.
					if (prodCodeToSubAcctID.get(subAccount.getProductCode()) == null) {
						List subAccountIds = new ArrayList();
						subAccountIds.add(subAccount.getId());
						prodCodeToSubAcctID.put(subAccount.getProductCode(), subAccountIds);
					} else {
						((List) prodCodeToSubAcctID.get(subAccount.getProductCode())).add(subAccount.getId());
						toBeDeletedSubAccounts.add(subAccount);
					}
				}
			}
			List arrangements = primaryHolding.getArrangement();
			for (int k = 0; k < arrangements.size(); k++) {
				Arrangement arrangement = (Arrangement) arrangements.get(k);
				List arrDestinations = arrangement.getArrDestination();
				for (int j = 0; j < arrDestinations.size(); j++) {
					ArrDestination arrDestination = (ArrDestination) arrDestinations.get(j);
					// Loop through the subAccountIDs map and find out the similar subaccount IDs for this ArrDestination
					Iterator it = prodCodeToSubAcctID.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pairs = (Map.Entry) it.next();
						List tempSubAccountIDs = (ArrayList) pairs.getValue();
						if (tempSubAccountIDs.contains(arrDestination.getSubAcctID())
								&& tempSubAccountIDs.indexOf(arrDestination.getSubAcctID()) != 0) {
							// If the SubAccounID of ArrDestination is present in the list of SubAccountIDs, then reset
							// the ArrDestination.SubAccountID to the first entry in the list
							arrDestination.setSubAcctID((String) tempSubAccountIDs.get(0));
						}

					}
				}
			}
			// Remove the subAccounts
			for (int i = 0; i < toBeDeletedSubAccounts.size(); i++) {
				SubAccount subAccount = (SubAccount) toBeDeletedSubAccounts.get(i);
				investment.removeSubAccount(subAccount);
			}
		}
	}

	// APSL2510 Begin

	public void translateProductfullnameToProductcode(NbaTXLife nbaTXLife) {
		Holding primaryHolding = nbaTXLife.getPrimaryHolding();
		Investment investment = primaryHolding.getInvestment();
		
		Map prodTranslationToProdCode = getFundsMap(nbaTXLife);
		String productCode="";
		boolean isFundReplaced=false;
		List missingTranslation = new ArrayList();
		if (investment != null) {
			List subAccounts = investment.getSubAccount();
			// Iterate through the subaccounts and create a map of productfulltranslation to ProductCode
			for (int i = 0; i < subAccounts.size(); i++) {
				SubAccount subAccount = (SubAccount) subAccounts.get(i);
				String productName = subAccount.getProductFullName();
				// NBLXA-187 Begin
				 productCode = subAccount.getProductCode();
					// Begin NBLXA-2043
					Map fundsMap = new HashMap();
					if(nbaTXLife.getOLifE().getSourceInfo().getFileControlID() != null){
						fundsMap.put("SYSTEM_ID", nbaTXLife.getOLifE().getSourceInfo().getFileControlID());
					}
					fundsMap.put("COVERAGE_KEY", nbaTXLife.getProductCode());
					fundsMap.put("COMPANY_CODE", nbaTXLife.getCarrierCode());
					fundsMap.put("CURRENT_DATE", NbaUtils.getStringInUSFormatFromDate(new Date()));
					
				
				if (NbaUtils.isBlankOrNull(productCode)) {
					productCode = (String) prodTranslationToProdCode.get(productName);
					fundsMap.put("OLD_FUND_ID", productCode);
					
					if(!(NbaUtils.isBlankOrNull(productCode))&& NbaUtils.isFundsCVRequired() &&  NbaUtils.replaceFunds(subAccount, fundsMap)){
						isFundReplaced = true;
						productCode = subAccount.getProductCode();
						LogHandler.Factory.LogDebug(this,"Fund is replaced for Ipipeline");
					}
					
				} else {
					fundsMap.put("OLD_FUND_ID", productCode);
					if (!(NbaUtils.isFundsCVRequired() && NbaUtils.replaceFunds(subAccount, fundsMap))) {
						productCode = prodTranslationToProdCode.containsValue(productCode) ? productCode : null;
					} else {
						isFundReplaced = true;
						productCode = subAccount.getProductCode();
						LogHandler.Factory.LogDebug(this, "Fund is replaced for Ipipeline");
					}
					if (productCode != null && (productName == null || productName.equals(EMPTY_STRING))) { /* NBLXA-1960 added 'If' */
						productName = (String) prodTranslationToProdCode.get(productCode);
					}
					// End NBLXA-2043
				}
				// NBLXA-187 End      
				if (productCode == null || productName == null) {     /*NBLXA-1960 added productName check*/
					// Begin APSL3352
					if (missingSubAccout.equals(EMPTY_STRING))
						missingSubAccout = subAccount.getId() + NbaConstants.seperator;
					else
						missingSubAccout = missingSubAccout + subAccount.getId() + NbaConstants.seperator;
					// End APSL3352
					missingTranslation.add(productName);
				} else {
					subAccount.setProductCode(productCode);
				}			 							
			}
			if(isFundReplaced){
				NbaUtils.addGeneralComment(getWork(), getUser(),NbaOliConstants.FUNDS_UPDATED_SYSTEMATICALLY); // NBLXA-2043
			}
			
		}
				// Mantain a list of missingtranslations
		if (missingTranslation.size() > 0) {
			setSubAccoutErrors(missingTranslation);
		}
	}

	// fetch Funds based on Coverage key from Nba_Funds
	public Map getFundsMap(NbaTXLife nbaTXLife) {
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		Map map = null;
		String productCode;
		if (policy != null) {
			productCode = policy.getProductCode();
			try {
				map = NbaUtils.getFunds(productCode);
			} catch (NbaBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}

		}
		return map;
	}

	public List getSubAccoutErrors() {
		return subAccoutErrors;
	}

	public void setSubAccoutErrors(List missingTranslation) {
		subAccoutErrors = missingTranslation;
	}

	private String formatSubAccoutErrors(List translations) {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = translations.iterator(); iter.hasNext();) {
			if (sb.length() > 0) {
				sb.append("~");
			}
			sb.append(iter.next());
		}
		return sb.toString();

	}

	// APSL2510 End

	// New Method APSL2573
	public LifeUSA getLifeUSA(Life life, boolean createNewObj) {
		LifeUSA lifeUSA = life.getLifeUSA();
		if (lifeUSA == null && createNewObj) {
			lifeUSA = new LifeUSA();
			life.setLifeUSA(lifeUSA);
		}
		return lifeUSA;
	}

	// New Method APSL2573
	public LifeUSAExtension getLifeUSAExtension(LifeUSA lifeUSA, boolean createNewObj) {
		LifeUSAExtension lifeUSAExt = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
		if (lifeUSAExt == null && createNewObj) {
			OLifEExtension lifeUSAExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LIFEUSA);
			lifeUSA.addOLifEExtension(lifeUSAExtension);
			lifeUSAExt = lifeUSAExtension.getLifeUSAExtension();
		}
		return lifeUSAExt;
	}

	// APSL2735 New Method, APSL2808
	public void updateSystematicSignature(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			NbaParty nbaParty = nbATXLife.getPrimaryParty();
			FormInstance formInstance = nbATXLife.getFormInstanceByName(FORM_NAME_SYSPAY);
			SignatureInfo insuredESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(),
					NbaOliConstants.OLI_PARTICROLE_PRIMARY);
			if (appInfo != null && nbaParty != null && formInstance != null && insuredESign != null) {
				Banking initialBanking = NbaUtils.getBankingByHoldingSubType(nbATXLife, NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
				if (initialBanking != null) {
					if (nbATXLife.isOwnerSameAsPrimaryIns()) {
						SignatureInfo pInsuredSign = findSignatureInfoForSystematic(formInstance.getSignatureInfo(), FORM_NAME_ELECTRONIC,
								NbaOliConstants.OLI_PARTICROLE_PRIMARY);
						if (pInsuredSign == null) {
							pInsuredSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_ELECTRONIC, NbaOliConstants.OLI_PARTICROLE_PRIMARY,
									NbaOliConstants.OLI_SIGTYPE_INITIAL);
							updateSystemeticSignatureInfo(insuredESign, pInsuredSign);
							formInstance.addSignatureInfo(pInsuredSign);
						}
					} else {
						SignatureInfo ownerSign = findSignatureInfoForSystematic(formInstance.getSignatureInfo(), FORM_NAME_ELECTRONIC,
								NbaOliConstants.OLI_PARTICROLE_OWNER);
						if (ownerSign == null) {
							ownerSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_ELECTRONIC, NbaOliConstants.OLI_PARTICROLE_OWNER,
									NbaOliConstants.OLI_SIGTYPE_INITIAL);
							updateSystemeticSignatureInfo(insuredESign, ownerSign);
							formInstance.addSignatureInfo(ownerSign);
						}
					}
					SignatureInfo depositerSign = findSignatureInfoForSystematic(formInstance.getSignatureInfo(), FORM_NAME_ELECTRONIC,
							NbaOliConstants.OLI_PARTICROLE_DEPOSITOR);
					if (depositerSign == null) {
						depositerSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_ELECTRONIC, NbaOliConstants.OLI_PARTICROLE_DEPOSITOR,
								NbaOliConstants.OLI_SIGTYPE_INITIAL);
						updateSystemeticSignatureInfo(insuredESign, depositerSign);
						formInstance.addSignatureInfo(depositerSign);
					}
				}
				Banking systematicBanking = NbaUtils.getBankingByHoldingSubType(nbATXLife, NbaOliConstants.OLI_HOLDSUBTYPE_SYSTEMATIC);
				if (systematicBanking != null) {
					if (nbATXLife.isOwnerSameAsPrimaryIns()) {
						SignatureInfo pInsuredSign = findSignatureInfoForSystematic(formInstance.getSignatureInfo(), FORM_NAME_SYSPAY,
								NbaOliConstants.OLI_PARTICROLE_PRIMARY);
						if (pInsuredSign == null) {
							pInsuredSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_SYSPAY, NbaOliConstants.OLI_PARTICROLE_PRIMARY,
									NbaOliConstants.OLI_SIGTYPE_SYST);
							updateSystemeticSignatureInfo(insuredESign, pInsuredSign);
							formInstance.addSignatureInfo(pInsuredSign);
						}
					} else {
						SignatureInfo ownerSign = findSignatureInfoForSystematic(formInstance.getSignatureInfo(), FORM_NAME_SYSPAY,
								NbaOliConstants.OLI_PARTICROLE_OWNER);
						if (ownerSign == null) {
							ownerSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_SYSPAY, NbaOliConstants.OLI_PARTICROLE_OWNER,
									NbaOliConstants.OLI_SIGTYPE_SYST);
							updateSystemeticSignatureInfo(insuredESign, ownerSign);
							formInstance.addSignatureInfo(ownerSign);
						}
					}
					SignatureInfo depositerSign = findSignatureInfoForSystematic(formInstance.getSignatureInfo(), FORM_NAME_SYSPAY,
							NbaOliConstants.OLI_PARTICROLE_DEPOSITOR);
					if (depositerSign == null) {
						depositerSign = getSubmitPolicyHelper().createSignatureInfo(FORM_NAME_SYSPAY, NbaOliConstants.OLI_PARTICROLE_DEPOSITOR,
								NbaOliConstants.OLI_SIGTYPE_SYST);
						updateSystemeticSignatureInfo(insuredESign, depositerSign);
						formInstance.addSignatureInfo(depositerSign);
					}
				}
			}
		}
	}

	/**
	 * Find the signature for systematic tab with specific role code and type
	 */
	// APSL2735 New Method
	public SignatureInfo findSignatureInfoForSystematic(List signatureInfoList, String signCode, long relationRoleCode) {
		if (signatureInfoList != null) {
			for (int i = 0; i < signatureInfoList.size(); i++) {
				SignatureInfo signatureInfo = (SignatureInfo) signatureInfoList.get(i);
				if (signCode.equalsIgnoreCase(signatureInfo.getSignatureCode()) && relationRoleCode == signatureInfo.getSignatureRoleCode()) {
					return signatureInfo;
				}
			}
		}
		return null;
	}

	// APSL2735 New Method
	public void updateSystemeticSignatureInfo(SignatureInfo fromSignature, SignatureInfo toSignatureInfo) {
		if (fromSignature != null && toSignatureInfo != null) {
			toSignatureInfo.setSignatureDate(fromSignature.getSignatureDate());
			SignatureInfoExtension fromSignInfoExt = NbaUtils.getFirstSignatureInfoExtension(fromSignature);
			if (fromSignInfoExt != null) {
				getSignatureInfoExtension(toSignatureInfo).setSignatureType(NbaOliConstants.OLI_SIGFORMAT_ESIGN);
			}
		}
	}

	public SignatureInfoExtension getSignatureInfoExtension(SignatureInfo signnfo) {
		SignatureInfoExtension signInfoExt = NbaUtils.getFirstSignatureInfoExtension(signnfo);
		if (signInfoExt == null) {
			OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SIGNATUREINFO);
			signnfo.addOLifEExtension(oliExt);
			signInfoExt = oliExt.getSignatureInfoExtension();
		}
		return signInfoExt;
	}

	// APSL2735 New Method
	public void updateBankingInformation(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			Banking initialBanking = NbaUtils.getBankingByHoldingSubType(nbATXLife, NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
			Holding initialHolding = NbaUtils.getHoldingByTypeAndSubTypeCode(nbATXLife.getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING,
					NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
			if (initialBanking != null && initialHolding != null) {
				NbaParty payerParty = nbATXLife.getParty(nbATXLife.getPayerPartyId(NbaOliConstants.OLI_REL_PAYER, initialHolding.getId()));
				initialBanking.setAcctHolderName(NbaUtils.getFullName(payerParty));
				addUpdateAuthorizedSignatory(initialBanking);
			}
			Banking systematicBanking = NbaUtils.getBankingByHoldingSubType(nbATXLife, NbaOliConstants.OLI_HOLDSUBTYPE_SYSTEMATIC);
			addUpdateAuthorizedSignatory(systematicBanking);
		}
	}

	// APSL2735 New Method
	public void addUpdateAuthorizedSignatory(Banking banking) {
		BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);
		if (bankingExt != null && bankingExt.getAuthorizedSignatory().isEmpty()) {
			AuthorizedSignatory authSign = new AuthorizedSignatory();
			authSign.setSignatoryName(banking.getAcctHolderName());
			bankingExt.addAuthorizedSignatory(authSign);
		} else if (bankingExt != null && !bankingExt.getAuthorizedSignatory().isEmpty()) {
			AuthorizedSignatory authSign = bankingExt.getAuthorizedSignatoryAt(0);
			banking.setAcctHolderName(authSign.getSignatoryName());
		}
	}

	// APSL2735
	protected void updateAchCwaLOBInXML103() throws NbaBaseException {
		List sourceList = getWork().getNbaCase().getNbaSources();
		if (sourceList != null && !sourceList.isEmpty()) {
			NbaSource nbaSource = null;
			for (int i = 0; i < sourceList.size(); i++) {
				nbaSource = (NbaSource) sourceList.get(i);
				if (A_ST_CWA_CHECK.equalsIgnoreCase(nbaSource.getSourceType())) {
					NbaLob sourceLob = nbaSource.getNbaLob();
					if (String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT).equalsIgnoreCase(sourceLob.getPaymentMoneySource())) {
						setTxLife(getWork().getXML103Source());
						Banking banking = NbaUtils.getBankingByHoldingSubType(getTxLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
						setNbaOLifEId(new NbaOLifEId(getTxLife()));
						if (banking == null) { // create new objects
							Holding initialHolding = new Holding();
							getNbaOLifEId().setId(initialHolding);
							initialHolding.setActionAdd();
							initialHolding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_BANKING);
							OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_HOLDING);
							oliExt.setActionAdd();
							oliExt.getHoldingExtension().setHoldingSubType(NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
							initialHolding.addOLifEExtension(oliExt);
							banking = new Banking();
							getNbaOLifEId().setId(banking);
							banking.setActionAdd();
							OLifEExtension bankExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);
							bankExt.setActionAdd();
							banking.addOLifEExtension(bankExt);
							initialHolding.addBanking(banking);
							getTxLife().getOLifE().addHolding(initialHolding);
						}
						banking.setRoutingNum(sourceLob.getBankRoutingNumber());
						banking.setAccountNumber(sourceLob.getBankAccountNumber());
						banking.setBankAcctType(sourceLob.getPaymentCategory());
						BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);
						if (bankingExt == null) {
							OLifEExtension bankExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);
							bankExt.setActionAdd();
							banking.addOLifEExtension(bankExt);
							bankingExt = bankExt.getBankingExtension();
						}
						bankingExt.setBankName(sourceLob.getFinancialInstitutionName());
						if (!banking.isActionAdd()) {
							banking.setActionUpdate();
						}
						if (!bankingExt.isActionAdd()) {
							bankingExt.setActionUpdate();
						}
						Holding initialHolding = NbaUtils.getHoldingByTypeAndSubTypeCode(getTxLife().getOLifE(),
								NbaOliConstants.OLI_HOLDTYPE_BANKING, NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
						NbaParty payerParty = getTxLife()
								.getParty(getTxLife().getPayerPartyId(NbaOliConstants.OLI_REL_PAYER, initialHolding.getId()));
						if (payerParty == null || payerParty.getParty() == null) {
							Party party = new Party();
							party.setActionAdd();
							getNbaOLifEId().setId(party);
							getTxLife().getOLifE().addParty(party);
							constructPayerRelation(initialHolding, party);
							payerParty = new NbaParty(party);
							updatePayerPartyObj(payerParty, sourceLob);
						} else {
							updatePayerPartyObj(payerParty, sourceLob);
						}
						banking.setAcctHolderName(NbaUtils.getFullName(payerParty));
						addUpdateAuthorizedSignatory(banking);

						if (bankingExt.getSubsequentSameAsAchInd()) { // update systematic also
							UpdateSubsequentBanking(banking, bankingExt);
						}
						getWork().updateXML103Source(getTxLife());
					}
				}
			}
		}
	}

	protected void UpdateSubsequentBanking(Banking initBanking, BankingExtension initBankingExt) {
		Banking systBanking = NbaUtils.getBankingByHoldingSubType(getTxLife(), NbaOliConstants.OLI_HOLDSUBTYPE_SYSTEMATIC);
		if (systBanking != null) {
			systBanking.setRoutingNum(initBanking.getRoutingNum());
			systBanking.setAccountNumber(initBanking.getAccountNumber());
			systBanking.setBankAcctType(initBanking.getBankAcctType());
			BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(systBanking);
			if (bankingExt == null) {
				OLifEExtension bankExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);
				bankExt.setActionAdd();
				systBanking.addOLifEExtension(bankExt);
				bankingExt = bankExt.getBankingExtension();
			}
			bankingExt.setBankName(initBankingExt.getBankName());
			if (!systBanking.isActionAdd()) {
				systBanking.setActionUpdate();
			}
			if (!bankingExt.isActionAdd()) {
				bankingExt.setActionUpdate();
			}
			systBanking.setAcctHolderName(initBanking.getAcctHolderName());
			addUpdateAuthorizedSignatory(systBanking);
		}
	}

	// APSL2735
	protected void updatePayerPartyObj(NbaParty payerParty, NbaLob sourceLob) {
		payerParty.getParty().setPartyTypeCode(sourceLob.getCheckIdentity());
		// Begin APSL3262
		if (payerParty.getParty().getEMailAddressCount() > 0) {
			EMailAddress address = payerParty.getParty().getEMailAddressAt(0);
			if (address != null && !address.hasEMailType()) {  //APSL4507
				address.setEMailType(NbaOliConstants.OLI_EMAIL_BUSINESS);
			}
		}
		// End APSL3262
		if (String.valueOf(NbaOliConstants.OLI_PT_PERSON).equalsIgnoreCase(sourceLob.getCheckIdentity())) {
			payerParty.constructPerson();
			payerParty.getPerson().setActionUpdate();
			payerParty.getPerson().setFirstName(sourceLob.getFirstName());
			payerParty.getPerson().setLastName(sourceLob.getLastName());
		} else if (String.valueOf(NbaOliConstants.OLI_PT_ORG).equalsIgnoreCase(sourceLob.getCheckIdentity())) {
			payerParty.constructOrganization();
			payerParty.getOrganization().setActionUpdate();
			payerParty.getOrganization().setDBA(sourceLob.getCheckEntityName());
		}
	}

	// APSL2735
	protected void constructPayerRelation(Holding initialHolding, Party payerParty) {
		Relation aRelation = new Relation();
		aRelation.setActionAdd();
		getNbaOLifEId().setId(aRelation);
		getTxLife().getOLifE().addRelation(aRelation);
		aRelation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		aRelation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
		aRelation.setOriginatingObjectID(initialHolding.getId());
		aRelation.setRelatedObjectID(payerParty.getId());
		aRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_PAYER);
	}

	// Begin APSL3352
	protected void removeArrangmentDestination(NbaTXLife nbaTXLife, String listofSubaccountsId) {
		Holding primaryHolding = nbaTXLife.getPrimaryHolding();
		List arrangements = primaryHolding.getArrangement();
		for (int k = 0; k < arrangements.size(); k++) {
			Arrangement arrangement = (Arrangement) arrangements.get(k);
			List arrDestinations = arrangement.getArrDestination();
			for (int j = 0; j < arrDestinations.size(); j++) {
				ArrDestination arrDestination = (ArrDestination) arrDestinations.get(j);
				if (!listofSubaccountsId.equals(EMPTY_STRING)
						&& (listofSubaccountsId.indexOf(arrDestination.getSubAcctID() + NbaConstants.seperator) != -1)) {
					arrangement.removeArrDestination(arrDestination);
					j--;
				}
			}

		}
	}

	protected void removeSubAccounts(NbaTXLife nbaTXLife, String listofSubaccountsId) {
		Holding primaryHolding = nbaTXLife.getPrimaryHolding();
		Investment investment = primaryHolding.getInvestment();
		if (investment != null) {
			List subAccounts = investment.getSubAccount();
			for (int j = 0; j < subAccounts.size(); j++) {
				SubAccount subAcct = (SubAccount) subAccounts.get(j);
				if (!listofSubaccountsId.equals(EMPTY_STRING) && (listofSubaccountsId.indexOf(subAcct.getId() + NbaConstants.seperator) != -1)) {
					investment.removeSubAccount(subAcct);
					j--;
				}
			}

		}
	}

	/**
	 * @return the missingSubAccout
	 */
	public String getMissingSubAccout() {
		return missingSubAccout;
	}

	/**
	 * @param missingSubAccout
	 *            the missingSubAccout to set
	 */
	public void setMissingSubAccout(String missingSubAccout) {
		this.missingSubAccout = missingSubAccout;
	}

	// End APSL3352
	
	// New method APSL4464(QC15850)
	public void createHipaaParty(NbaTXLife nbATXLife, Party aParty) {
		FormInstance formInstance = null;
		formInstance = NbaUtils.getFormInstanceByFormNameAndRelatedObjectId(nbATXLife.getOLifE(), NbaConstants.FORM_NAME_HIPAA, aParty.getId());
		if (formInstance == null) {
			List formInstanceList = getFormInstances(nbATXLife, NbaConstants.FORM_NAME_HIPAA);
			if (!formInstanceList.isEmpty()) {
				formInstance = (FormInstance) formInstanceList.get(0);
			}
		}
		if (formInstance != null) {
			Person insured = aParty.getPersonOrOrganization().getPerson();
			Party hippaParty = new Party();
			hippaParty.setActionAdd();
			nbATXLife.getOLifE().addParty(hippaParty);
			PersonOrOrganization personororag = new PersonOrOrganization();
			personororag.setActionAdd();
			hippaParty.setPersonOrOrganization(personororag);
			nbaOLifEId.setId(hippaParty);//APSL4563
			Person person = new Person();
			person.setActionAdd();
			personororag.setPerson(person);
			person.setPrefix(insured.getPrefix());
			person.setFirstName(insured.getFirstName());
			person.setMiddleName(insured.getMiddleName());//APSL4631
			person.setLastName(insured.getLastName());
			person.setSuffix(insured.getSuffix());
			person.setBirthDate(insured.getBirthDate());
			Relation aRelation = new Relation();
			aRelation.setActionAdd();
			nbATXLife.getOLifE().addRelation(aRelation);
			nbaOLifEId.setId(aRelation);//APSL4563
			OLifEExtension oLifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RELATION);
			aRelation.addOLifEExtension(oLifeExt);
			RelationExtension relationExt = oLifeExt.getRelationExtension();
			if (relationExt != null) {
				relationExt.setActionAdd();
			}
			aRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_168);
			aRelation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
			aRelation.setOriginatingObjectType(NbaOliConstants.OLI_PARTY);
			aRelation.setOriginatingObjectID(aParty.getId());
			aRelation.setRelatedObjectID(hippaParty.getId());
			formInstance.setRelatedObjectID(hippaParty.getId());
			formInstance.setActionUpdate();
		}
	}
	
	
	//APSL4761 New Method (QC17108)
	/**
	 * Returns the LifeExtension. Create a new LifeExtension if necessary. 
	 * @return LifeExtension
	 */
	protected void updateLifeExtension(NbaTXLife nbATXLife) {
		if (nbATXLife != null && NbaUtils.isAdcApplication(getWork())) {
			Life life = nbATXLife.getLife();
			LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
			if (lifeExtension == null) {
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LIFE);
				oLifEExtension.setActionAdd();
				life.addOLifEExtension(oLifEExtension);
				lifeExtension = NbaUtils.getFirstLifeExtension(life);
			}
			lifeExtension.setTotalModalPremAmt(nbATXLife.getPolicy().getPaymentAmt());
		}		
	}
	
	// APSL4636 New Method
	public void createCompanionCaseInformation(NbaTXLife nbATXLife) throws NbaBaseException {
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(nbATXLife.getPolicy().getApplicationInfo());
		if (appInfoExtn != null && !NbaUtils.isBlankOrNull(appInfoExtn.getCompanionCaseReferenceID())) {
			CompanionCase companionCase = new CompanionCase();
			companionCase.setAwdId(getWork().getID());
			companionCase.setContractNumber(nbATXLife.getNbaHolding().getPolicy().getPolNumber());
			companionCase.setPrimaryName(nbATXLife.getPrimaryPartyName());			
			NbaCompanionCaseVO nbaCompanionCaseVO = new NbaCompanionCaseVO();
			nbaCompanionCaseVO.setWorkItemID(getWork().getID());
			nbaCompanionCaseVO.setCompanionReferenceID(appInfoExtn.getCompanionCaseReferenceID());
			nbaCompanionCaseVO.setCompanionCase(companionCase);
			NbaCompanionCaseControlData nbaCompanionCaseControlData = new NbaCompanionCaseControlData();
			List companionCases = nbaCompanionCaseControlData.getNbaCompanionCaseVOSByReferenceId(appInfoExtn.getCompanionCaseReferenceID());
			if (!companionCases.isEmpty()) {
				Date primaryCaseDate = null;				
				NbaCompanionCaseVO vo = null;
				String primaryCaseContractNumber = null;
				for (int i = 0; i < companionCases.size(); i++) {
					vo = (NbaCompanionCaseVO) companionCases.get(i);
					Date companionCaseDate = NbaUtils.getDateFromStringInAWDFormat(vo.getWorkItemID());
					if (primaryCaseDate == null || companionCaseDate.before(primaryCaseDate)) {
						primaryCaseDate = companionCaseDate;						
						primaryCaseContractNumber = vo.getContractNumber();
					}
				}
				if (!NbaUtils.isBlankOrNull(primaryCaseContractNumber) && isContractPrintDone(primaryCaseContractNumber)) { //APSL5053 
					return;
				}
			}
			new NbaCompanionCaseControlData().insert(nbaCompanionCaseVO); 
		}
	}	
	
	// APSL4636 New Method
	protected boolean isContractPrintDone(String primaryCaseContractNumber) throws NbaBaseException {		
		NbaSearchVO searchPrintVO = searchWI(NbaConstants.A_WT_CONT_PRINT_EXTRACT, primaryCaseContractNumber);
		if (searchPrintVO != null && searchPrintVO.getSearchResults() != null && !searchPrintVO.getSearchResults().isEmpty()) {
			List searchResultList = searchPrintVO.getSearchResults();
			for (int i = 0; i < searchResultList.size(); i++) {
				NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				if (searchResultVo.getQueue().equalsIgnoreCase(END_QUEUE) || searchResultVo.getQueue().equalsIgnoreCase(A_QUEUE_POST_ISSUE)) {
					return true;
				}
			}
		} 
		return false;
	}

	// APSL4636 New Method
	protected NbaSearchVO searchWI(String workType, String contractNumber) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(contractNumber);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}
	
	/* APSL5047 new method 
	 * Calculating RateClass based on insured's Age if insured's rateClassAppliedFor is blank for TCON cases
	 * */
	
	public String getRateClass(Person person) throws NbaBaseException {
		Map deOinkMap = new HashMap(2, 1);
		NbaVpmsAdaptor rulesProxy = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(getWork().getNbaLob());
			if (person.hasAge()) {
				deOinkMap.put("A_AGE", Integer.toString(person.getAge()));
			}
			rulesProxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			rulesProxy.setSkipAttributesMap(deOinkMap);
			rulesProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GETRATECLASS);

			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			VpmsComputeResult rulesProxyResult = rulesProxy.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List rulesList = vpmsResultsData.getResultsData();
				if (!rulesList.isEmpty()) {
					String returnStr = (String) rulesList.get(0);
					if (!NbaUtils.isBlankOrNull(returnStr)) {
						return returnStr;
					}
				}
			}
			return null;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {

			if (rulesProxy != null) {
				try {
					rulesProxy.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED); // SPR3362
				}
			}
		}
	}
	
	/** 
	 * APSL5263 new method
	 * Updates the Covoption
	 * 
	 * @param nbATXLife
	 *           The nbATXLife to set.
	 */

	public void updateCovoptionDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			try {
				Policy policy = nbATXLife.getPrimaryHolding().getPolicy();
				if (policy != null) {
					Coverage coverage = nbATXLife.getPrimaryCoverage();
					List covOptionList = coverage.getCovOption();
					LifeParticipant primaryLifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(coverage);
					if (!covOptionList.isEmpty() && primaryLifeParticipant != null) {
						Iterator<CovOption> aItr = covOptionList.iterator();
						while (aItr.hasNext()) {
							CovOption aCovOption = aItr.next();
							if (NbaUtils.isBlankOrNull(aCovOption.getLifeParticipantRefID())) {
								aCovOption.setLifeParticipantRefID(primaryLifeParticipant.getId());
								aCovOption.setActionUpdate();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
