package com.csc.fsg.nba.contract.validation;
/* 
 * *******************************************************************************<BR>
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
 * 
 * *******************************************************************************<BR>
 */
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.business.transaction.NbaAgentNameAddressRetrieve;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.ResultInfoExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * NbaValAgent performs Agent validation using CyberLife Agent Validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>NBA073</td><td>Version 3</td><td>Agent Validation</td></tr>
 * <tr><td>SPR1789</td><td>Version 4</td><td>Auto population of the servicing agent from Phase 3 NBA091 agt name and address</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR1945</td><td>Version 4</td><td>Correct inconsistent contract validation edits for String values</td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR2721</td><td>Version 5</td><td>Servicing agent party is not getting created during agent validation processing</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>SPR3188</td><td>Version 8</td><td>The invalid agent indicator is not being reset to false after all agent validation errors have been corrected.<td><tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr>
 * <tr><td>ALPC183</td><td>AXA Life Phase 1</td><td>BGA and Retail Agent Contract Validation</td></tr> 
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaValAgent extends NbaContractValidationCommon implements NbaContractValidationBaseImpl, NbaContractValidationImpl {
	//NBA103 - removed getLogger()

	// SPR1994 code deleted
	/**
	 * Perform one time initialization.
	 */
	 //NBA237 changed method signature
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVO) { //AXAL3.7.18
		super.initialze(nbaDst, nbaTXLife, subset, nbaOLifEId, nbaProduct, userVO); //AXAL3.7.18
		initProcesses();
		Method[] allMethods = this.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("process_")) {
				// SPR3290 code deleted
				processes.put(aMethodName.substring(8).toUpperCase(), aMethod);
			}
		}
	}

	/**
	 * Verify the presence of primary writing agent (RelationRodeCode "37")..
	 */
	protected void process_P001() {
		logDebug("Performing NbaValAgent.process_P001()");//NBA103

		for (int i = 0; i < getNbaTXLife().getOLifE().getRelationCount(); i++) {
			Relation relation = getNbaTXLife().getOLifE().getRelationAt(i);
			if (!NbaUtils.isDeleted(relation) && relation.getRelationRoleCode() == OLI_REL_PRIMAGENT) {
				return;
			}
		}
		addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getNbaTXLife().getOLifE()));
	}

	/**
	 * Verify the presence of CompanyProducerID (producer identification) 
	 */
	protected void process_P002() {
		if (verifyCtl(CARRIERAPPOINTMENT)) {
			logDebug("Performing NbaValAgent.process_P002() for ", getCarrierAppointment());//NBA103
			if (!(getCarrierAppointment().hasCompanyProducerID() && getCarrierAppointment().getCompanyProducerID().trim().length() > 0)) { //SPR1945
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCarrierAppointment()));
			}
		}
	}

	/**
	 * If there is a single agent, set the InterestPercent (commission share percent) to 100. 
	 * Otherwise verify total InterestPercent equals 100.
	 */
	protected void process_P003() {
		logDebug("Performing NbaValAgent.process_P003()");//NBA103
		double total = 0;
		int agentCount = 0;
		for (int i = 0; i < getOLifE().getRelationCount(); i++) {
			if (!NbaUtils.isDeleted(getOLifE().getRelationAt(i))
					&& NbaUtils.isPrimaryWritingAgentRelationCode(getOLifE().getRelationAt(i).getRelationRoleCode())) {
				setRelation(getOLifE().getRelationAt(i));
				agentCount++;
				if (getRelation().hasInterestPercent()) {
					total += getRelation().getInterestPercent();
				}
			}
		}
		if (agentCount == 1) {
            //APSL5224 Deleted code to set InterestPercent to 100%
			getRelation().setRelationRoleCode(OLI_REL_PRIMAGENT);//ALS5846
			getRelation().setActionUpdate();
			//if there is single agent and there is no relationproducerextension for the relation , create a new one
			RelationProducerExtension relPrdExtn = getRelationProducerExtension();//APSL3397 QC12161
		} 
		if (agentCount > 0 && total != 100) { //APSL5224
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Interest Percent: ", total), getIdOf(getNbaTXLife().getOLifE()));
		}
	}

	/**
	 * If there is a single agent, set the RenewalInterestPercent (renewal  percent) to 100. 
	 * Otherwise verify total RenewalInterestPercent equals 100.
	 */
	protected void process_P004() {
		logDebug("Performing NbaValAgent.process_P004()");//NBA103
		double total = 0;
		int agentCount = 0;
		for (int i = 0; i < getOLifE().getRelationCount(); i++) {
			if (!NbaUtils.isDeleted(getOLifE().getRelationAt(i))
					&& NbaUtils.isPrimaryWritingAgentRelationCode(getOLifE().getRelationAt(i).getRelationRoleCode())) {
				setRelation(getOLifE().getRelationAt(i));
				agentCount++;
				if (getRelationProducerExtension().hasRenewalInterestPercent()) {
					total += getRelationProducerExtension().getRenewalInterestPercent();
				}
			}
		}
		if (agentCount == 1) {
			getRelationProducerExtension().setRenewalInterestPercent(100);
			getRelationProducerExtension().setActionUpdate();
		} else if (agentCount > 1 && total != 100) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Renewal Interest Percent: ", total), getIdOf(getNbaTXLife().getOLifE()));
		}
	}

	/**
	 * If there is a single agent, set the VolumeSharePercent (volume share) to 100. 
	 * Otherwise verify total VolumeSharePercent equals 100.
	 */
	protected void process_P005() {
		logDebug("Performing NbaValAgent.process_P005()");//NBA103
		double total = 0;
		int agentCount = 0;
		for (int i = 0; i < getOLifE().getRelationCount(); i++) {
			if (!NbaUtils.isDeleted(getOLifE().getRelationAt(i))
					&& NbaUtils.isPrimaryWritingAgentRelationCode(getOLifE().getRelationAt(i).getRelationRoleCode())) {
				setRelation(getOLifE().getRelationAt(i));
				agentCount++;
				//APSL3113
				if(!getRelation().hasVolumeSharePct() && getRelation().hasInterestPercent() ||
					(getRelation().hasVolumeSharePct() && getRelation().hasInterestPercent() && getRelation().getInterestPercent()!=getRelation().getVolumeSharePct())	){
					getRelation().setVolumeSharePct(getRelation().getInterestPercent());
					getRelation().setActionUpdate();
				}//APSL3113
				if (getRelation().hasVolumeSharePct()) {
					total += getRelation().getVolumeSharePct();
				}
			}
		}
		if (agentCount == 1) {
			getRelation().setVolumeSharePct(100);
			getRelation().setActionUpdate();
		} else if (agentCount > 1 && total != 100) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Volume Share Percent: ", total), getIdOf(getNbaTXLife().getOLifE()));
		}
	}

	/**
	 * This method creates xml514 transaction for each primary writing agent and additional writing agent and passes it to
	 * WebService for Agent Validation. Then updates contract data with SystemMessages received back from WebService.
	 */
	protected void process_P006() {
		//begin NBA073
		if (verifyCtl(RELATION)) {
			logDebug("Performing NbaValAgent.process_P006() for ", getRelation());//NBA103
			//Begin AXAL3.7.18
			
			try {
			    //Webservice refactoring
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_AGENTVALIDATION, getUserVO(), getNbaTXLife(),null,getRelation());
				NbaTXLife validationResponse = (NbaTXLife) webServiceInvoker.execute();
				//ALS3675 begin
                if (validationResponse != null) {
                    TransResult result = validationResponse.getTransResult();
                    if (result != null && result.getResultCode() != TC_RESCODE_SUCCESS) { //NBA112
                        long severity = -1;
                        long restrictCode = -1;
                        for (int i = 0; i < result.getResultInfoCount(); i++) {
                            ResultInfo resultInfo = result.getResultInfoAt(i);
                            if (resultInfo == null) {
                                addNewSystemMessage(getNbaConfigValProc().getMsgcode(), NbaBaseException.NO_RESULTMSG_FOUND, relation.getId()); //NBA112
                            }
                            StringBuffer msgDesc = null;
                            if (resultInfo.hasResultInfoDesc()) { //begin NBA112
                                String desc = resultInfo.getResultInfoDesc();
                                msgDesc = new StringBuffer(desc.trim());
                                int extCount = resultInfo.getOLifEExtensionCount();
                                ResultInfoExtension resultExtn = null;
                                for (int index = 0; index < extCount; index++) {
                                    OLifEExtension extension = resultInfo.getOLifEExtensionAt(index);
                                    if (extension != null) {
                                        if ((NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()))
                                                && (extension.isResultInfoExtension())) {
                                            resultExtn = extension.getResultInfoExtension();
                                            if (resultExtn != null) {
                                                severity = (resultExtn.getResultInfoSeverity() != -1L) ? resultExtn.getResultInfoSeverity() : -1L;
                                                restrictCode = (resultExtn.getResultInfoRestrict() != -1L) ? resultExtn.getResultInfoRestrict() : -1L;
                                            }
                                            if (severity != -1) {
                                                msgDesc.append(" Severity - ");
                                                msgDesc.append(severity);
                                            }
                                            //ALII179 a.k.a APSL245
                                            if (restrictCode != -1) {
                                                msgDesc.append(" Restrict Code - ");
                                                msgDesc.append(restrictCode);
                                                String restrictCd = getNbaTableAccessor().translateBackEndValue(getTblKeys(),NbaTableConstants.NBA_MSGRESTRICTCODE,String.valueOf(restrictCode),0);
                                                getNbaConfigValProc().setRestrict(restrictCd);
                                                
                                            }else{
                                            	getNbaConfigValProc().setRestrict(-1);
                                            }
                                        }

                                    }
                                } //Begin NBA112,
                            } //end NBA132, End NBA112
                           addNewSystemMessage(getNbaConfigValProc().getMsgcode(), msgDesc.toString(), relation.getId(), severity);
                        }
                    }
                } else {
                    addNewSystemMessage(getNbaConfigValProc().getMsgcode(), NbaBaseException.WEBSERVICE_NOT_AVAILABLE, relation.getId()); //NBA112
                } //End AXAL3.7.18
                //ALS3675 end
			} catch (NbaBaseException e) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Error invoking WebService: ", e.getMessage()), relation.getId()); //NBA112
				return;
			}
		} //end NBA073
	}

	/**
	 * Create ACORD 514 transaction (subtype 51409) for the Primary writing agent (tc=37) name and address. Invoke a WebService to retrieve name and
	 * adress information from the backend agency system. Create SystemMessage if name and address information is not returned in the response.
	 */
	protected void process_P007() throws NbaConfigurationException {
		if (verifyCtl(RELATION)) {
			logDebug("Performing NbaValAgent.process_P007() for ", getRelation());// NBA103, begin SPR1789
			NbaTXLife newTXLife = null;
			TransResult transResult = null;
			// NBA112 Code deleted
			try {
				if ((NbaUtils.isPrimaryWritingAgentRelation(relation) || NbaUtils.isBGAAgentRelation(relation)
						|| NbaUtils.isSBGAAgentRelation(relation) || NbaUtils.isFirmRelation(relation))
						&& (relation.getOriginatingObjectID().equalsIgnoreCase((getNbaTXLife().getPrimaryHolding()).getId()))) { // ALPC183, APSL2788,
																																	// APSL3447-HVT
					AxaProducerVO producerVO = new AxaProducerVO().createProducerVOfromTXLife(getNbaTXLife(), getRelation());
					AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_AGENTDEMOGRAPICS,
							getUserVO(), null, null, producerVO);
					newTXLife = (NbaTXLife) webServiceInvoker.execute();

					transResult = newTXLife.getTransResult();
					if (transResult != null) {
						if (transResult.getResultCode() == TC_RESCODE_SUCCESS) {
							logDebug("Performing NbaValAgent.process_P007() result Received ", getRelation());
							new NbaAgentNameAddressRetrieve().mergeParty(getNbaTXLife(), newTXLife, getRelation());
							logDebug("Performing NbaValAgent.process_P007() Agent Merge done ", getRelation());
						} else {
							ResultInfo resultInfo;
							String desc = null; // NBA112
							for (int i = 0; i < transResult.getResultInfoCount(); i++) {
								resultInfo = transResult.getResultInfoAt(i); // NBA112, begin NBA112
								if (resultInfo.hasResultInfoDesc()) {
									desc = resultInfo.getResultInfoDesc().trim();
									if (desc.length() > 2) {
										addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Failure code = " + desc.substring(0, 1)
												+ ", ", desc.substring(2, desc.length())), relation.getId());
									} else {
										addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Failure code = ", desc), relation.getId());
									}
								} // end NBA112
							}
							if (desc == null) { // NBA112
								addNewSystemMessage(getNbaConfigValProc().getMsgcode(), NbaBaseException.WEBSERVICE_NOT_AVAILABLE, relation.getId()); // NBA112
							}
						}
					} else {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), NbaBaseException.WEBSERVICE_NOT_AVAILABLE, relation.getId());
					}
				}
			} catch (NbaConfigurationException e) { // NBA103
				throw e; // NBA103
			} catch (Exception e) { // Begin NBA112
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Error invoking WebService: ", e.getMessage()), relation.getId()); // NBA112
			} // end SPR1789
		}
	}

	//NBA112
	private boolean shouldMergeAgency() {
		return getNbaTXLife().getBackendSystem().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)
				&& getRelation().getRelationRoleCode() == NbaOliConstants.OLI_REL_PRIMAGENT;
	}

	//NBA112
	private void mergeAgency(NbaTXLife newTXLife, NbaAgentNameAddressRetrieve retrieve) throws NbaBaseException {
		Relation agencyRelation = getAgencyRelation();
		if (agencyRelation == null) {
			agencyRelation = createAgencyRelation(createAgencyParty()); //NBA132
		}
		retrieve.mergeParty(getNbaTXLife(), newTXLife, agencyRelation);
	}

	//NBA112
	private Relation getAgencyRelation() {
		ArrayList relationList = getNbaTXLife().getOLifE().getRelation();
		int relCnt = relationList.size();
		for (int i = 0; i < relCnt; i++) {
			Relation relation = (Relation) relationList.get(i);
			if (relation.getRelationRoleCode() == OLI_REL_AGENCYOF) {
				return relation;
			}
		}
		return null;
	}

	//NBA112
	private Party createAgencyParty() {
		Party party = new Party();
		getNbaOLifEId().setId(party);
		party.setActionAdd();

		Organization organization = new Organization();
		PersonOrOrganization personOrOrganization = new PersonOrOrganization();
		personOrOrganization.setOrganization(organization);
		personOrOrganization.setActionAdd();
		party.setPersonOrOrganization(personOrOrganization);

		Producer producer = new Producer();
		producer.setActionAdd();

		CarrierAppointment carrierAppointment = new CarrierAppointment();
		carrierAppointment.setActionAdd();
		getNbaOLifEId().setId(carrierAppointment);
		producer.addCarrierAppointment(carrierAppointment);

		party.setProducer(producer);
		getOLifE().addParty(party);
		return party;
	}

	/**
	 * Creates a party to party relation for an agency to the primary writing agent.
	 * @param party agency
	 * @return
	 */
	//NBA112
	// NBA132 changed method signature, removed txlife parameter
	protected Relation createAgencyRelation(Party party) {
		Relation relation = new Relation();
		relation.setActionAdd();
		relation.setRelationRoleCode(OLI_REL_AGENCYOF);
		ArrayList relationList = getNbaTXLife().getOLifE().getRelation(); //NBA132
		// NBA132 deleted code
		int cnt = relationList.size(); //NBA132
		for (int i = 0; i < cnt; i++) {
			Relation tmpRelation = (Relation) relationList.get(i); //NBA132
			if (tmpRelation.getRelationRoleCode() == OLI_REL_PRIMAGENT) {
				relation.setOriginatingObjectID(tmpRelation.getRelatedObjectID());
				break;
			}
		}
		relation.setRelatedObjectID(party.getId());
		relation.setOriginatingObjectType(OLI_PARTY);
		relation.setRelatedObjectType(OLI_PARTY);
		getNbaOLifEId().setId(relation);
		NbaUtils.setRelatedRefId(relation, relationList); //NBA132
		getNbaTXLife().getOLifE().addRelation(relation);
		return relation;
	}

	/**
	 * Set AgentErrorsInd to true if there are severe or overridable agent validation errors present on the contract.
	 * Otherwise, set to false.  
	 */
	/**
	 * One more Condition is added for APSL 1457 :: Informational CV should generate in case of Licensing Agent.
	 */
	protected void process_P008() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValAgent.process_P008()");//NBA103
			boolean errors = false;
			//begin SPR3188
			SystemMessage systemMessage;
			SystemMessageExtension systemMessageExtension;
			long currentSubset = getCurrentSubSet().longValue();
			int count = getHolding().getSystemMessageCount();
			long severity;
			for (int i = 0; i < count; i++) {
				systemMessage = getHolding().getSystemMessageAt(i);
				if (!systemMessage.isActionDelete()) {
					severity = systemMessage.getMessageSeverityCode();
					//end SPR3188					
					if (severity == OLI_MSGSEVERITY_SEVERE || severity == OLI_MSGSEVERITY_OVERIDABLE || severity == OLI_MSGSEVERITY_INFO) { // APSL 1457
						systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(systemMessage); //SPR3188
						if (systemMessageExtension != null && systemMessageExtension.getMsgValidationType() == currentSubset) { //SPR3188
							if (severity == OLI_MSGSEVERITY_SEVERE   // APSL 1457
									|| (severity == OLI_MSGSEVERITY_OVERIDABLE && !systemMessageExtension.getMsgOverrideInd())) { //SPR3188
								errors = true;
								break;
							} else if (severity == OLI_MSGSEVERITY_INFO && NbaUtils.isWholeSale(getPolicy())) {//APSL1457
								errors = true;
								break;
							}
						}
					}
				} //SPR3188
			}
			getPolicyExtension().setAgentErrorsInd(errors);
		}
	}

	// SPR1994 code deleted
	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationImpl#validate()
	 */
	// ACN012 changed signature
	public void validate(ValProc nbaConfigValProc, ArrayList objects) {
		if (nbaConfigValProc.getUsebase()) { //ACN012
			super.validate(nbaConfigValProc, objects);
		} else {//ALS2600
		    if (getUserImplementation() != null) {
		        getUserImplementation().validate(nbaConfigValProc, objects);
		    }
		} //ALS2600    
	}

}