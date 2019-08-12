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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;


/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcSIAppSubmit extends NbaProcAppSubmit {

	protected void doProcess() throws NbaBaseException { 
		NbaTXLife nbaTXLife = getXML103();
		nbaTXLife.doXMLMarkUp();
		NbaLob aNbaLob = getWorkLobs();
		// APSL3308(QC12368)
		if(nbaTXLife != null && !nbaTXLife.isSIApplication()){
			handleNonSICaseTOSIQueue();
			return;
		} // APSL3308(QC12368) end
		
		assignUserID();
		setOperatingModeLOB();
		setRiskLOB();
		setCaseCreateDate(aNbaLob, nbaTXLife);		
		
		if (aNbaLob.getPolicyNumber() == null || aNbaLob.getPolicyNumber().length() < 1) {
			setMaxtries(Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.DUPLICATE_CONTRACT_RETRY)));
			needContractNo = true;
		}
		 
		if (getMaxtries() < 1) {
			setMaxtries(1);
		}
		
		assignPolicyNumber(); // Assign a policy number if necessary
		
		doCIPInquiry(nbaTXLife);//APSL4412 readded		
		
		if (getResult() == null) { // Submit the Application to the Back End System and handle the response				
			setNbaTxLife(doContractInsert(nbaTXLife));
			handleHostResponse();			
		}

		assignQueues(0.0);	//Modified for NBLXA186 - Term Processing Automate	
		if (getResult() == null) {
			updateWorkflowLobs();
		}
			
	}
	
	/**
	 * Assign a policy number to the Case if one is not already present.
	 * @param work a NbaDst value object for which the process is to occur
	 * @throws NbaBaseException
	 */	
	protected void assignPolicyNumber() throws NbaBaseException {
		if (needContractNo) {						
			String polNumber;
			try {
				polNumber = selectPolNumberFromAuxilliaryDB(getWork().getID());
				if (NbaUtils.isBlankOrNull(polNumber)) {
					polNumber = NbaAutoContractNumber.getInstance().generateEIBContractNumber(getXML103(), getWork(), getUser());
					updatePolNumberToAuxilliaryDB(polNumber, getWork().getID());
				}
				if (!NbaUtils.isBlankOrNull(polNumber)) {
					getXML103().getPolicy().setPolNumber(polNumber); 
					getWorkLobs().setPolicyNumber(polNumber);
					updatePolNumberOnTransactions();
				} else { 
					throw new NbaBaseException("Could not generate contract number ");
				}
			} catch (NbaBaseException nbe) {
				if (!nbe.isFatal()) {
					addComment(nbe.getMessage());
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, nbe.getMessage(), getAWDFailStatus()));
				}
				throw nbe; 
			}			
		}
	}
	
	/*
	 * This method first iterates over all the owner parties and invokes CIP Request for each. It adds the response from the cip service to the
	 * attachment of the party.
	 */
	//APSL4412 - New Method 
	protected void doCIPInquiry(NbaTXLife nbaTXLife) throws NbaBaseException { //SR564247(APSL2525) - New Method
		if (nbaTXLife != null) {
			//nbaTXLife.setAccessIntent(NbaConstants.UPDATE); //APSL2228
			List ownerParties = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_OWNER);
			// NBLXA-1352
			List primaryInsured = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_INSURED);
			List jointInsured = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_JOINTINSURED);
			ownerParties = NbaUtils.mergeInsAndOwnPartiesList(ownerParties, primaryInsured, jointInsured);
		//NBLXA-1352
			if (ownerParties != null) {
				Iterator ownerPartyIterator = ownerParties.iterator();

				while (ownerPartyIterator.hasNext()) {
					Party ownerParty = ((NbaParty) ownerPartyIterator.next()).getParty();
					//APSL4412 start
					try {
						NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachment(ownerParty);
						long msgSeverity = -1l;
						if (txLife != null && txLife.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) {				
							msgSeverity = AxaUtils.getCIPMessageSeverity(txLife);
						}
						if( (txLife == null) || (txLife.getTransResult().getResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS)
								|| (msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_INFO && msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_WARNING && msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_SEVERE) ) {
							//ALII1564,Removed code-ALII1718						
							//APSL4224	
							AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CIP, getUser(),
									nbaTXLife, null, ownerParty);
							NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
							Attachment attachment = new Attachment();
							AttachmentData attachmentData = new AttachmentData();
							List attachmentList = AxaUtils.getAttachmentsByType(ownerParty, NbaOliConstants.OLI_ATTACH_1009800001);			
							if (attachmentList.size() > 0) {
								attachment = (Attachment) attachmentList.get(0);
								attachmentData = attachment.getAttachmentData();
								attachmentData.setActionUpdate();
								attachment.setActionUpdate();
							} else {
								attachment = new Attachment();
								attachmentData = new AttachmentData();
								attachmentData.setActionAdd();
								attachment.setActionAdd();
							}
							//APSL4224 code deleted
							attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_1009800001);
							attachmentData.setPCDATA(nbaTxLifeRes.toXmlString());
							attachment.setDateCreated(new Date());
							attachment.setAttachmentData(attachmentData);
							attachment.setUserCode(getUser().getUserID());
							ownerParty.addAttachment(attachment);
							ownerParty.setActionUpdate();
							createTransactionForCIP(nbaTxLifeRes, nbaTXLife);
						}
					} catch (Exception e) {
						setWork(getOrigWorkItem()); //APSL4224
						throw new NbaBaseException("Error invoking CIP WebService", e);
					}
					//APSL4412 end
				}
			}
			//NbaContractAccess.doContractUpdate(getNbaTxLife(), getWork(), getUser());//APSL2228
			;
		}
	}

	/**
	 * This method creates a work item for CIP CM.
	 * @throws NbaBaseException
	 */
	//APSL4412 New Method
	protected void createTransactionForCIP(NbaTXLife nbaTxLifeRes, NbaTXLife nbaTXLife) throws NbaBaseException {
		if (nbaTXLife != null && NbaUtils.isRetail(nbaTXLife.getPolicy()) && nbaTxLifeRes != null && 
				((nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE) ||
				(nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS && AxaUtils.getCIPMessageSeverity(nbaTxLifeRes) == NbaOliConstants.OLI_OTHER) ||
				(nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS && AxaUtils.getCIPMessageSeverity(nbaTxLifeRes) == NbaOliConstants.OLI_MSGSEVERITY_SEVERE))) {
			HashMap deoinkMap = new HashMap();
			deoinkMap.put("A_CIPWork", "true");
			//NBLXA-1511 start
			if (NbaUtils.isDtcCase(nbaTXLife)) {
				deoinkMap.put(NbaVpmsConstants.A_RCMTEAM, "RET1");
			}
			// NBLXA-1511 end
			else {
				deoinkMap
						.put(NbaVpmsConstants.A_RCMTEAM, NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(nbaTXLife), NbaUtils.getEPGInd(nbaTXLife))); // APSL4412
			}

			//Invoke VP/MS to determine work type and status
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTXLife, deoinkMap);
			if(provider.getWorkType() != null && provider.getInitialStatus() != null) {
				NbaTransaction aTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
				aTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
				//Initialize LOBs of the WI
				aTransaction.getNbaLob().setAppOriginType(getWork().getNbaLob().getAppOriginType());
				aTransaction.getNbaLob().setAppState(getWork().getNbaLob().getAppState());
				aTransaction.getNbaLob().setCaseManagerQueue(getWork().getNbaLob().getCaseManagerQueue());
			}
		}
	}

}
