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
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;

/**
 * NbaProcContractValidationCheck is the class to process cases found in NBVALCK queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA004</td><td>Version 2</td><td>Automated Process Model Support for Work Items</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA073</td><td>Version 3</td><td>Agent Validation/Retrieve Contract Info</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr> 
 * <tr><td>SPR1855</td><td>Version 4</td><td>Improve performance times for nbA pollers making an AWD work select.</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>ALS2597</td><td>AXA Life Phase 1</td><td>QC # 1409  - Contract Validation Messages are appear 2 on the contract message window</td></tr>
 * <tr><td>ALS2847</td><td>AXA Life Phase 1</td><td>QC # 1570  - End to End: Status in nbA does not match AXADistributors.com</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaProcContractValidationCheck extends NbaAutomatedProcess { //NBA073 changed the class name
	/**
	 * NbaProcContractValidationCheck constructor.
	 */
	public NbaProcContractValidationCheck() {
		super();
	}
	/**
	 * - Get Holding Inquiry and Update LOBs.
	 * - Process an agent to determine if a writing agent is present
	 *	 and valid on the contract.
	 * - Update the nbProducer database
	 * @throws NbaBaseException
	 */
	protected void doProcess() throws NbaBaseException {
		// NBA050 CODE DELETED
		processAgent();
	}
	/**
	 * This method drive the Automated Contract Validation Check process.It is
	 * - Get Holding Inquiry and Update LOBs.
	 * - Process an agent to determine if a writing agent is present
	 *	 and valid on the contract.
	 * - If Case is entered from portal and has no agent information,
	 *	 move the Case to the next queue.
	 * - In other scenario Check for Invalid Agent. If true, Create an 
	 *	 agent problem work item with the status "AGTNOTLICD".
	 * @param user the NbaUser for whom the process is being executed
	 * @param work a NbaDst value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult(); // NBA050
		}
		// NBA050 CODE DELETED
		doProcess();
		// NBA050 CODE DELETED

		if (isSeverityExists(nbaTxLife)) { // NBA073
			createValErrorWorkItem(); // NBA073
		} //NBA073
		//ALS4707 Code Deleted
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}

		changeStatus(getResult().getStatus());
		doUpdateWorkItem(); // also unlocks the case
		//NBA020 code deleted

		return getResult();
	}
	/**
	 * - If Case is entered from portal and has no agent information,
	 *	 move the Case to the next queue.
	 * - In other scenario Check for Invalid Agent. If true, Create an 
	 *	 agent problem work item with the status "AGTNOTLICD".
	 * @return boolean true when the case is entered from portal and has no agent 
	 * 			information or when the agent is invalid. 
	 * 			false indicating nbProducer database need not be updated.
	 * @throws NbaBaseException
	 */
	protected void processAgent() throws NbaBaseException {

		NbaLob lob = getWork().getNbaLob();
		if (lob.getPortalCreated() && lob.getAgentID() == null) {
			return;
		}

		if (lob.getInvalidAgent()&& is3701NotOnlySevereCVExists()) {//APSL4234/QC15120
			//begin NBA073
			Map deOink = new HashMap(); 
			NbaTransaction nbaTrans =null; //NBLXA-1337
			deOink.put("A_ErrorSeverity", Long.toString(getMessageSeverity(getNbaTxLife())));//ALS5718
			// NBLXA-1337 -- Check For Licensing WI
			String appendRoutReason = NbaUtils.getAppendReason(getNbaTxLife());
			
			retrieveLicWorkItem(getNbaTxLife());
			if(licensingworkExists == false){
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink); //NBA004 ALS5718
				//end NBA073
				 nbaTrans = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus()); //NBA004
				//NBA208-32
				nbaTrans.getTransaction().setAction("L"); //SPR1855
				nbaTrans.increasePriority(provider.getWIAction(), provider.getWIPriority()); //NBA020
				//Begin NBA112
				nbaTrans.getNbaLob().setRouteReason(nbaTrans.getNbaLob().getRouteReason()+" "+appendRoutReason);
			}else if(licensingworkExists == true){
				if(searchResultForLicWIVO !=null){
					retrieveExisitngLicensingWIFromEndQueue(getWork(),getNbaTxLife(),deOink);
					nbaTrans = endedTransaction;
				}
			}
			// NBLXA-1337 -- END
			NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
			npec.setActionAdd();
			npec.setOriginator(getUser().getUserID());
			npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
			npec.setProcess(getWork().getQueue());
			npec.setText("Agent not licensed");
			nbaTrans.addManualComment(npec.convertToManualComment());
			//End NBA112	
           //Beging AXAL3.7.20
			String licenseCaseManagerLOB = lob.getLicCaseMgrQueue();
			if (licenseCaseManagerLOB != null && !CONTRACT_DELIMITER.equalsIgnoreCase(licenseCaseManagerLOB)) {
				nbaTrans.getNbaLob().setLicCaseMgrQueue(licenseCaseManagerLOB);
			}
			nbaTrans.getNbaLob().setLastName(lob.getLastName());//ALS3926
			nbaTrans.getNbaLob().setFirstName(lob.getFirstName());//ALS3926
			nbaTrans.getNbaLob().setSpecialCase(lob.getSpecialCase());//ALS3937
			//End AXAL3.7.20
		}
	}

	/**
	 * Checks all the SystemMessage objects in xml203 and returns true if any of the SystemMessage object has message severity 2 or 4. Current Base
	 * processing triggers a Validation Work Item (N2VALERR) to notify the Case Manager after application submit when an overridable or sever error
	 * exists on the case. The presence of validation error 3700 as defined in FUNC16804 will trigger this work item, and it will be routed to the
	 * Underwriter Case Manager as defined in phase 1.
	 */
	//NBA073 new method 
	protected boolean isSeverityExists(NbaTXLife nbaTxLife) throws NbaBaseException {
		boolean severity = false;
		SystemMessage sysMessage;
		SystemMessageExtension systemMessageExtension;//ALS4242
		ArrayList messages = nbaTxLife.getPrimaryHolding().getSystemMessage();
		for (int i = 0; i < messages.size(); i++) {
			sysMessage = (SystemMessage) messages.get(i);
			if (sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE
					|| sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE) { //P2AXAL007 NBLXA-2280
					systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage); //ALS4242
					if ((systemMessageExtension != null && systemMessageExtension.getMsgValidationType() == NbaConstants.SUBSET_AGENT
							&& getWork().getNbaLob().getInvalidAgent())
							|| (nbaTxLife.isADCApplication() && NbaUtils.isInitialPremiumPaymentForm(nbaTxLife) 
									&& (sysMessage.getMessageCode() == NbaConstants.MESSAGECODE_5902
									           || sysMessage.getMessageCode() == NbaConstants.MESSAGECODE_5903))){//ALS4242, QC4887
						continue; //ALS4242 Ignore agent CV message if Agent error exists
					}
					severity = true;
					break;
			}
		}
		return severity;
	}

	/**
	 * This method creates Validation Error WorkItem if any of the SystemMessage object has message severity 2 or 4.
	 * @throws RemoteException
	 */
	//NBA073 new method
	protected void createValErrorWorkItem() throws NbaBaseException {
		//begin SPR2639
		Map deOink = new HashMap();
		deOink.put("A_ErrorSeverity", Long.toString(NbaOliConstants.OLI_MSGSEVERITY_SEVERE));
		deOink.put("A_CreateValidationWI", "true");//ALS5718
		NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), nbaTxLife, deOink);
		getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
		//end SPR2639
	}

	//ALS4707 Code deleted
	
	//New Method ALS5718
	protected long getMessageSeverity(NbaTXLife nbaTxLife) {
		ArrayList messages = nbaTxLife.getPrimaryHolding().getSystemMessage();
		SystemMessage sysMessage = null;
		SystemMessageExtension systemMessageExtension = null;
		long severity = NbaOliConstants.OLI_OTHER;;
		for (int i = 0; i < messages.size(); i++) {
			sysMessage = (SystemMessage) messages.get(i);
			if (!sysMessage.isActionDelete() && !sysMessage.isActionDeleteSuccessful()) {
				systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
				if (systemMessageExtension != null && (systemMessageExtension.getMsgValidationType() == NbaConstants.SUBSET_AGENT)) {
					if ((sysMessage.hasMessageSeverityCode()) || (sysMessage.getMessageSeverityCode() < severity)) {
						severity = sysMessage.getMessageSeverityCode();
					}	
				}
			}
		}
		if(severity == NbaOliConstants.OLI_OTHER) {
			return NbaConstants.LONG_NULL_VALUE;
		}
		return severity;
	}
	
	//APSL4234/QC15120 new method
	protected boolean is3701NotOnlySevereCVExists() {
		boolean createAgentLICWI = false;
		SystemMessage sysMessage;
		SystemMessageExtension systemMessageExtension;
		ArrayList messages = getNbaTxLife().getPrimaryHolding().getSystemMessage();
		for (int i = 0; i < messages.size(); i++) {
			sysMessage = (SystemMessage) messages.get(i);
			if (sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_SEVERE
					|| sysMessage.getMessageSeverityCode() == NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE) {
				systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
				if(systemMessageExtension != null && systemMessageExtension.getMsgValidationType() == SUBSET_AGENT){
					if(sysMessage.getMessageCode() != MESSAGECODE_AGENTLIC_WI && !systemMessageExtension.getMsgOverrideInd()){
						createAgentLICWI = true;
						break;
					}
				}
			}
		}
		return createAgentLICWI;
	}
	
}
