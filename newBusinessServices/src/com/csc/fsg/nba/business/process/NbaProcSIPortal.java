package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.List;

import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaParseException;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaAgentVO;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaGeneralComment;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.DistributionChannelInfo;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.UserAuthRequest;
import com.csc.fsg.nba.vo.txlife.VendorApp;
import com.csc.fsg.nba.vo.txlife.VendorName;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * <tr><td>APSL3447</td><td>Discretionary</td><td>HVT</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcSIPortal extends NbaProcPortal {

	private NbaTXLife agentTxLife = null;
	NbaXML103SubmitPolicyHelper submitPolicyHelper = new NbaXML103SubmitPolicyHelper();	
	private static final String RETRIEVE_AGENT_DEMOGRAPHICS_BP = "RetrieveAgentDemographicsTxLifeBP";
	
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialize
		if (!initializeFields(user, work)) {
			return statusProcessFailed();
		}	
		
		if (getResult() == null) {
			doProcess();			
		}
		
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		changeStatus(getResult().getStatus());
		// Update the Work Item with it's new status and update the work item in AWD
		doUpdateWorkItem();		
		// Return the result
		return getResult();
	}
	
	protected void doProcess() throws NbaBaseException {
		// Check if XML103 source is available
		List errLst = new ArrayList();
		try {
			if (getXML103() == null) {
				errLst.add(NbaBaseException.XML_SOURCE); // No source. move this case to error queue with proper message
			} else {
				setNbaTxLife(getXML103());
			}
		} catch (NbaParseException nbPEx) {
			errLst.add(nbPEx.getMessage()); // set the encapsulated message to the error list
		}
		
		//APSL3308(QC12368)
		if(getNbaTxLife() != null && !getNbaTxLife().isSIApplication()){
			handleNonSICaseTOSIQueue();
			return;
		}// APSL3308(QC12368) end
		
		if (errLst.size() > 0) {
			setValidationErrors(errLst);
			writeErrorsInAWD();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, formatErrorText(), getHostErrorStatus()));			
			return;
		}
				
		// Set the portal indicator to true
		getWork().getNbaLob().setPortalCreated(true);
		translateNbaValues();
		if (!updateXmlAndLob()) {
			writeErrorsInAWD();
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, formatErrorText(), getFailStatus()));
				return;
			}
		}
		
		commitIndexingData();		
		//Call the routine to update Lob fields
		updateLobFromNbaTxLife(getNbaTxLife());
		if (getResult() == null) {
			updateTXLife(getNbaTxLife());
			List relationList = getNbaTxLife().getOLifE().getRelation();
			for (int i = 0; i < relationList.size(); i++) {
				Relation aRelation = (Relation) relationList.get(i);
				long relationRoleCode = aRelation.getRelationRoleCode();
				if (relationRoleCode == NbaOliConstants.OLI_REL_PRIMAGENT
						|| relationRoleCode == NbaOliConstants.OLI_REL_ADDWRITINGAGENT
						|| relationRoleCode == NbaOliConstants.OLI_REL_GENAGENT || relationRoleCode == NbaOliConstants.OLI_REL_SUBORDAGENT) { //SR641590 SUB-BGA
					String partyId = aRelation.getRelatedObjectID();
					NbaParty party = getNbaTxLife().getParty(partyId);
					Party agParty = party.getParty();
					setAgentTxLife(retrieveAgentDemographics(agParty, relationRoleCode));
					updateAgentParty(agParty, relationRoleCode);
				}
			}
			getNbaOLifEId().resetIds(getNbaTxLife());
			getNbaOLifEId().resolveDuplicateIds(getNbaTxLife());
			getWork().updateXML103Source(getNbaTxLife());
		}
	}
	
	/**
	 * Updates the Txlife values 
	 *  @param nbATXLife The nbATXLife to set.
	 * @throws NbaBaseException
	 */
	protected void updateTXLife(NbaTXLife nbATXLife) throws NbaBaseException {		
		if (nbATXLife != null) {
			updateCoverageDetails(nbATXLife);
			updateSignatureDetails(nbATXLife);
			updatePartyDetails(nbATXLife);			
			updateRelationDetails(nbATXLife);
			updateAppInfoDetails(nbATXLife);
			updateSubFirmIndicator(nbATXLife);//SR641590 SUB-BGA
			updateSubBgaRelationRoleCode(nbATXLife);//SR641590 SUB-BGA			
		}
	}
	
	/**
	 * Updates the Coverage Details 
	 *  @param nbATXLife The nbATXLife to set.
	 */
	
	public void updateCoverageDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			try {
				Policy policy = nbATXLife.getPrimaryHolding().getPolicy();
				if (policy != null) {
					//APSL2562 QC#10093
					NbaTableAccessor nta = new NbaTableAccessor();
					NbaPlansData nbaPlansData = nta.getPlanData(nta.setupTableMap(getWork()));
					if (!NbaUtils.isBlankOrNull(nbaPlansData) && !NbaUtils.isBlankOrNull(nbaPlansData.getCovKeyTranslation()) ) {
						policy.setPlanName(nbaPlansData.getCovKeyTranslation());
						policy.setActionUpdate();
					}
					String productCode = policy.getProductCode();
					String planName = policy.getPlanName();
					Coverage coverage = nbATXLife.getPrimaryCoverage();
					coverage.setCurrentAmt(nbATXLife.getLife().getFaceAmt());//QC7768
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
	 *  @param nbATXLife The nbATXLife to set.
	 */
	
	public void updateSignatureDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			updateInsuredSignature(nbATXLife);
			updateOwnerSignature(nbATXLife);
			updateProducerSignature(nbATXLife);
		}
	}
	
	/**
	 * Updates the Party Details 
	 *  @param nbATXLife The nbATXLife to set.
	 * @throws NbaBaseException
	 */
	//QC7979
	public void updatePartyDetails(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			updateInsuredDetails(nbATXLife);
			updateOwnerDetails(nbATXLife);
		}
	}
	
	/**
	 * Process the eSignature and copy it to the various sections of the application 
	 *  @param nbATXLife The nbATXLife to set.
	 */
	protected void updateInsuredSignature(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			NbaParty nbaParty = nbATXLife.getPrimaryParty();
			if (appInfo != null && nbaParty != null) { //APSL2650
				SignatureInfo insuredESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(), NbaOliConstants.OLI_PARTICROLE_PRIMARY);//QC8291
				if (insuredESign != null) {
					appInfo.setSignedDate(insuredESign.getSignatureDate());

					//Process Application Signature
					SignatureInfo appSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY, NbaOliConstants.OLI_SIGTYPE_APPSIG);					
					getSubmitPolicyHelper().updateSignatureInfo(insuredESign, appSign);
					//Begin QC8291 - Copy the Signed City, State and Date from Owner App Sign to the Insured App Sign
					NbaParty ownerParty = nbATXLife.getPrimaryOwner();
					if (ownerParty != null) {
						SignatureInfo ownerESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), ownerParty.getParty().getId(), NbaOliConstants.OLI_PARTICROLE_OWNER);//QC8291
						if (ownerESign != null) {
							appSign.setSignatureDate(ownerESign.getSignatureDate());
							appSign.setSignatureCity(ownerESign.getSignatureCity());
							appSign.setSignatureState(ownerESign.getSignatureState());
						}
						appInfo.addSignatureInfo(appSign);
						
						// Process HIPPA Signature APSL3125(QC11909)
						SignatureInfo hipaaSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY, NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH);
						getSubmitPolicyHelper().updateSignatureInfo(insuredESign, hipaaSign);
						appInfo.addSignatureInfo(hipaaSign);
						// End APSL3125(QC11909)
						
						//End QC8291						
						appInfo.removeSignatureInfo(insuredESign);
					}
				}
			}
		}
	}
	
	/**
	 * Process the eSignature and copy it to the various sections of the application 
	 *  @param nbATXLife The nbATXLife to set.
	 */
	protected void updateOwnerSignature(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			NbaParty nbaParty = nbATXLife.getPrimaryOwner();
			if (appInfo != null && nbaParty != null) {
				SignatureInfo ownerESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(), NbaOliConstants.OLI_PARTICROLE_OWNER);//QC8291
				if (ownerESign != null) {
					SignatureInfo appSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_OWNER, NbaOliConstants.OLI_SIGTYPE_APPSIG);
					getSubmitPolicyHelper().updateSignatureInfo(ownerESign, appSign);
					appInfo.addSignatureInfo(appSign);
				}
				appInfo.removeSignatureInfo(ownerESign);
			}
		}
	}
	
	/**
	 * Process the eSignature and copy it to the various sections of the application 
	 *  @param nbATXLife The nbATXLife to set.
	 */
	
	protected void updateProducerSignature(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			ApplicationInfo appInfo = nbATXLife.getPolicy().getApplicationInfo();
			NbaParty nbaParty = nbATXLife.getWritingAgent();
			if (appInfo != null && nbaParty != null) {
				SignatureInfo producerESign = getSubmitPolicyHelper().findSignatureInfo(appInfo.getSignatureInfo(), nbaParty.getParty().getId(), NbaOliConstants.OLI_PARTICROLE_PRIMAGENT);//QC8291
				if (producerESign != null) {
					//Process Producer Client Profile signature
					SignatureInfo prodClientProfileSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMAGENT, NbaOliConstants.OLI_SIGTYPE_PRODCLNTSIG);
					getSubmitPolicyHelper().updateSignatureInfo(producerESign, prodClientProfileSign);
					appInfo.addSignatureInfo(prodClientProfileSign);
					
					//Process Producer Client Profile signature
					//Begin QC8022					
					SignatureInfo prodAppSign = getSubmitPolicyHelper().createSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMAGENT, NbaOliConstants.OLI_SIGTYPE_PRODAPPSIG);
					getSubmitPolicyHelper().updateSignatureInfo(producerESign, prodAppSign);
					appInfo.addSignatureInfo(prodAppSign);
					//End QC8022					
					
					appInfo.removeSignatureInfo(producerESign);
				}
			}
		}
	}	
	
	/**
	 * Updates the Insured Details 
	 *  @param nbATXLife The nbATXLife to set.
	 * @throws NbaBaseException
	 */
	//New Method QC7979
	protected void updateInsuredDetails(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null && nbATXLife.getPrimaryParty() != null) {
			Party party = nbATXLife.getPrimaryParty().getParty();
			//Update Email Address Type
			EMailAddress emailAddress = null;
			for (int k = 0; k < party.getEMailAddressCount(); k++) {
				emailAddress = party.getEMailAddressAt(k);
				if (!emailAddress.hasEMailType()) {
					emailAddress.setEMailType(NbaOliConstants.OLI_EMAIL_BUSINESS);
				}
			}			
			
			//Begin QC7923 
			//Update Occupation
			Person person = party.getPersonOrOrganization().getPerson();
			if(person != null && person.hasOccupation()) {//QC8033
				String translatedOccupation = getSubmitPolicyHelper().getTranslatedNbaValue(OCCUPATION,person.getOccupation());
				if(!NbaUtils.isBlankOrNull(translatedOccupation)) {
					person.setOccupation(translatedOccupation);
				}
				//Begin QC7927
				String occupation = getSubmitPolicyHelper().encodeOlifeValue(person.getOccupation().toUpperCase(), NBA_OCCUPATION);
				if (NbaUtils.isBlankOrNull(occupation)) {
					if (party.getEmploymentCount() > 0) {
						//If occupation does not match with nba value, set the occupation as 'Other' and 'Other occupation' as the value coming from
						// iPipeline
						Employment employment = party.getEmploymentAt(0);
						employment.setOccupation(person.getOccupation());
					}
					occupation = NbaOliConstants.NBA_OCCUPATION_OTHER;
				}
				person.setOccupation(occupation);
				//End QC7927
			}
			//End QC7923
			//Begin CR1345594
			PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
			if(personExt != null) {
				if(personExt.hasRateClassAppliedFor()) {
					String translatedRateClass = getSubmitPolicyHelper().getTranslatedNbaValue(RATE_CLASS_APPLIED_FOR,	personExt.getRateClassAppliedFor()); 
					if(!NbaUtils.isBlankOrNull(translatedRateClass)) {
						personExt.setRateClassAppliedFor(translatedRateClass);
						getWork().addManualComment(getGeneralComment(APPLIED_FOR_RATING_DEFAULTING_MSG));//QC8273
					}
				}
			}
			//End CR1345594
		}
	}
	
	/**
	 * Updates the Relation Details. Sets the RelatedRefID value for the Relation objects. 
	 *  @param nbATXLife The nbATXLife
	 */
	//New Method QC7891
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
	
	public void updateAppInfoDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null) {
			UserAuthRequest userAuthRequest = nbATXLife.getUserAuthRequest();
			VendorApp vendorApp = userAuthRequest.getVendorApp();
			Policy policy = nbATXLife.getPrimaryHolding().getPolicy();
			if (vendorApp != null && vendorApp.getVendorName() != null && policy.getApplicationInfo() != null) {
				VendorName vendorName = vendorApp.getVendorName();				
				ApplicationInfo appInfo = policy.getApplicationInfo();
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if(appInfoExt == null) {
					OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
					appInfo.addOLifEExtension(oliExt);
					appInfoExt = oliExt.getApplicationInfoExtension();
				}
				appInfoExt.setSourceVendorCode(vendorName.getVendorCode());
			}
		}
	}
	
	/**
	 * SR641590 SUB-BGA 
	 * Set subFirmIndicator if SBGA Lob is present on case
	 *  @param nbATXLife The nbATXLife to set.
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
	
	/**
	 * Updates the Owner Details
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */
	//New Method QC7979
	public void updateOwnerDetails(NbaTXLife nbATXLife) {
		if (nbATXLife != null && nbATXLife.getPrimaryOwner() != null) {
			Party party = nbATXLife.getPrimaryOwner().getParty();
			//Update Email Address Type
			EMailAddress emailAddress = null;
			for (int k = 0; k < party.getEMailAddressCount(); k++) {
				emailAddress = party.getEMailAddressAt(k);
				if (!emailAddress.hasEMailType()) {
					emailAddress.setEMailType(NbaOliConstants.OLI_EMAIL_BUSINESS);
				}
			}
			//BEGIN QC9565
			Organization organization = nbATXLife.getPrimaryOwner().getOrganization();
			OrganizationExtension orgExtn = NbaUtils.getFirstOrganizationExtension(organization);
			if (party.hasPersonOrOrganization()&& party.getPersonOrOrganization().getOrganization()!= null) {
				NbaParty poaParty = nbATXLife.getParty(nbATXLife.getPartyId(NbaOliConstants.OLI_REL_POWEROFATTRNY));
				if(poaParty!=null){
					if(NbaOliConstants.OLI_ORG_TRUST==party.getPersonOrOrganization().getOrganization().getOrgForm()&&!NbaUtils.isBlankOrNull(poaParty.getPerson().getTitle())){
						if(orgExtn!=null){
							orgExtn.setApplicantTitlePresentInd(true);
						}
					}
				}
			}
			//END QC9565
		}
	}
	
	/**
	 * Creates a GeneralComment object
	 * @param commentText
	 * @return Comment
	 */
	 //New Method QC8273
	protected Comment getGeneralComment(String commentText) throws NbaBaseException  {
		NbaGeneralComment generalComment = new NbaGeneralComment();
		generalComment.setEnterDate(getTimeStamp(getUser())); 
		generalComment.setText(commentText);
		generalComment.setOriginator(getUser().getUserID());
		generalComment.setActionAdd();
		return generalComment.convertToManualComment();
	}
	
	/**
     * SR515492 New Method
     * Retrieve Agent Demographics details by calling procuder Web Service.
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
		AccelResult accelResult = (AccelResult) ServiceHandler.invoke(RETRIEVE_AGENT_DEMOGRAPHICS_BP, ServiceContext.currentContext(), agentVO);
		if (accelResult.hasErrors()) {
			WorkflowServiceHelper.checkOutcome(accelResult);
		} else {
			return (NbaTXLife) accelResult.getFirst();
		}
		return null;
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
		if(aParty.getProducer().getCarrierAppointmentCount() > 0 ){
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
	 * SR515492 New Method
	 * Update the Agent Party details in XML using Agent search web service response.
	 * @param agParty
	 * @throws NbaBaseException
	 */
	public void updateAgentParty(Party agParty, long relationRoleCode) throws NbaBaseException {
		// primary writing Agent
		if (String.valueOf(NbaOliConstants.OLI_PT_PERSON).equals(String.valueOf(agParty.getPartyTypeCode()))) {
			String partyId = getAgentTxLife().getPartyId(relationRoleCode);
			if (partyId == null) {
				Relation relation = getAgentTxLife().getRelationForRelationRoleCode(relationRoleCode);
				partyId = relation.getOriginatingObjectID();
			}
			if (partyId != null) {//QC8154
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
			setNbaOLifEId(new NbaOLifEId(getNbaTxLife()));
			//Methode to create General Agent Party
			constructValueObjectForGeneralAgentParty(getNbaOLifEId(), getNbaTxLife().getOLifE(), agParty.getId());

		} else if (String.valueOf(NbaOliConstants.OLI_PT_ORG).equals(String.valueOf(agParty.getPartyTypeCode()))) {
			String partyId = getAgentTxLife().getPartyId(relationRoleCode);//SR641590 SUB-BGA
			if (partyId == null) {
				Relation relation = getAgentTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
				partyId = relation.getOriginatingObjectID();
			}
			NbaParty agentParty = getAgentTxLife().getParty(partyId);
			Party pwaParty = agentParty.getParty();
			agParty.setPersonOrOrganization(pwaParty.getPersonOrOrganization());
			agParty.setAddress(pwaParty.getAddress());
			agParty.setProducer(pwaParty.getProducer());
			updateAgentDistributionChannelInfo(agParty.getProducer());
			agParty.setAddress(pwaParty.getAddress());
			agParty.setPhone(pwaParty.getPhone());
			agParty.setEMailAddress(pwaParty.getEMailAddress());
			if (relationRoleCode == NbaOliConstants.OLI_REL_SUBORDAGENT) { //SR641590 SUB-BGA
				constructValueObjectForEntityGeneralAgentParty(getNbaOLifEId(), getNbaTxLife().getOLifE(), agParty.getId());
			}
		}
	}
	
	/**
	 * SR515492 New Method
	 * Add the DistributionChannelInfo tag in XML.
	 * 
	 * @param producer
	 * @throws NbaBaseException
	 */
	protected void updateAgentDistributionChannelInfo(Producer producer) throws NbaBaseException{
		if (producer.getCarrierAppointmentCount() >0){
			CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
			if (null != carrierAppointment) {
				if (carrierAppointment.getDistributionChannelInfoCount() > 0) {
					DistributionChannelInfo distributionChannelInfo = carrierAppointment.getDistributionChannelInfoAt(0);
					if (null != distributionChannelInfo) {
						distributionChannelInfo.setDistributionChannel(getWork().getNbaLob().getDistChannel());
					}
				} else {
					DistributionChannelInfo distributionChannelInfo = new DistributionChannelInfo();
					distributionChannelInfo.setDistributionChannel(getWork().getNbaLob().getDistChannel());
					carrierAppointment.addDistributionChannelInfo(distributionChannelInfo);
				}
			}
		}
	}
	
	/**
	 * @return Returns the agentTxLife.
	 */
	public NbaTXLife getAgentTxLife() {
		return agentTxLife;
	}
	/**
	 * @param agentTxLife The agentTxLife to set.
	 */
	public void setAgentTxLife(NbaTXLife agentTxLife) {
		this.agentTxLife = agentTxLife;
	}
	
	/**
	 * SR515492 New Method
	 * Construct value object for general agent Party.
	 * @param olifeId
	 * @param olife
	 * @param id
	 */
	public void constructValueObjectForGeneralAgentParty(NbaOLifEId olifeId, NbaContractVO olife, String id) {
		long relationRoleCode = NbaOliConstants.OLI_REL_PROCESSINGFIRM; //APSL3447
		String partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_PROCESSINGFIRM); //APSL3447
		if(partyId == null) {	//APSL3447	
			partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_GENAGENT);
			relationRoleCode = NbaOliConstants.OLI_REL_GENAGENT; //APSL3447
		}
		if (partyId != null) {//QC8154
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
			aRelation.setRelationRoleCode(relationRoleCode); //APSL3447
			constructValueObjectForSubFirmAgentParty(olifeId, getNbaTxLife().getOLifE(), id); //APSL3447
			//APSL3447 removed construct method for superior Agent
		}
	}
	
	/**
	 * SR515492 New Method
	 * construct value object for relation.
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
	 * SR515492 New Method
	 * Map the agent party 
	 * @param partyBean
	 * @param party
	 */
	public void mapPartyAttribute(Party partyBean, Party party) {
		if (partyBean != null && party != null) {//8154
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
	 * SR515492 New Method
	 * Construct value object for entity general agent Party.
	 * @param olifeId
	 * @param olife
	 * @param id
	 */
	public void constructValueObjectForEntityGeneralAgentParty(NbaOLifEId olifeId, NbaContractVO olife, String id) {
		String partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_GENAGENT);
		if (partyId != null) {//QC8154
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
	 * SR641590 SUB-BGA New Method.
	 * construct value object for Sub-Firm agent party.
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
	 * SR515492 New Method.
	 * construct value object for superior agent party.
	 * @param olifeId
	 * @param olife
	 * @param id
	 */
	public void constructValueObjectForSuperiorAgentParty(NbaOLifEId olifeId, NbaContractVO olife, String id) {
		String partyId = getAgentTxLife().getPartyId(NbaOliConstants.OLI_REL_SUPERIORAGENT);
		if (partyId != null) {//QC8154
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
	
}
