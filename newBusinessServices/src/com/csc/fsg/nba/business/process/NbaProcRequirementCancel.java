package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapterFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vo.nbaschema.ReqItem;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;

/**
 * NbaProcRequirementCancel is the class that processes nbAccelerator work items
 * found on the AWD requirement cancellation queue (). The process cancels the
 * requirements, which have been ordered, from third party providers, owners or 
 * from a producer.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1234</td><td>Version 3</td><td>General code cleanup</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture changes</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>SPR1374</td><td>Version 3</td><td>Requirements that do not go thru Redundancy Check are not cancelled properly</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN025</td><td>Version 5</td><td>Requirement Cancellation</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR3160</td><td>Version 6</td><td>Requirement Evaluation is expecting Requirement Results attachment to be OLI_LU_BASICATTACHMENTTYP(271)  instead of OLI_LU_BASICATTMNTTY_TEXT (1)</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>SPR3416</td><td>Version 7</td><td>Manual Requirements are going to the NBERROR queue<td><tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcRequirementCancel extends NbaAutomatedProcess {
	//NBA008 begin added
	//ACN025 code deleted
	protected long transCode = -1L; //ACN025
	//NBA008 end added
	//NBA050 CODE DELETED
	//SPR2380 removed logger
/**
 * NbaProcRequirementCancel constructor comment.
 */
public NbaProcRequirementCancel() {
	super();
}

//NBA103 - removed method

/**
 * This method changes the status of first cross referenced work item
 * from "Added" to "Ordered" in its RCS.
 * @param xmlDecorator is the RCS for first cross referenced work item.
 */
//NBA008 New Method
public void changeRCSforFirstWI(NbaXMLDecorator xmlDeco) throws NbaBaseException {
	AutomatedProcess aProcess = xmlDeco.getAutomatedProcess(NbaConstants.PROC_REDUNDANCY_CHECK); //SPR2639
	if (aProcess != null) {
		aProcess.setOriginalStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_ORDER));
	}
}
/**
 * This method prefixes the original subject line with "Cancel:"
 * @param subject is the original subject value
 * @return String is the subject containing the new subject value like (Cancel:original subject)
 */
//NBA008 New Method
public String changeSubject(String subject) {
	subject = "Cancel:" + subject;
	return subject;
}
/**
 * This abstract method of super class is implemented here in order to
 * execute the automated process.
 * @param user the user for whom the process is being executed
 * @param work a DST value object for which the process is to occur
 * @return NbaAutomatedProcessResult containing information about
 *         the success or failure of the process
 */
public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	//NBA008 code deleted
	//NBA008 begin added
	// NBA027 - logging code deleted
	if (!initialize(user, work)) {
		return getResult();//NBA050
	}
	//	begin SPR3416
	NbaLob nbaLob = getWork().getNbaLob(); 
	if(NbaConstants.PROVIDER_MANUAL.equalsIgnoreCase(nbaLob.getReqVendor())){
		processManualRequirement();
		return getResult();
	}
	//	end SPR3416
	//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retVO = new NbaAwdRetrieveOptionsVO();
		retVO.setWorkItem(getWork().getID(), false);
		retVO.requestSources();
		retVO.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retVO));  //NBA213
		//SPR3416 deleted code
		 
		if (!verifySources()) { //source not present
			//retrieving the RCS
			NbaXMLDecorator xmlDecorator = new NbaXMLDecorator(getWork().getRequirementControlSource().getText());
			AutomatedProcess autoProcess = xmlDecorator.getAutomatedProcess(NbaConstants.PROC_REDUNDANCY_CHECK); //SPR2639
			if (autoProcess != null) {
				String originalStatus = autoProcess.getOriginalStatus();
				//checking if requirement was added
				if (originalStatus != null && originalStatus.equals(Long.toString(NbaOliConstants.OLI_REQSTAT_ADD))) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Requirement was Added", getOtherStatus()));
					nbaLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED)); //ACN025, NBA130
				} else { //checking if requirement was ordered
					if (originalStatus.equals(Long.toString(NbaOliConstants.OLI_REQSTAT_ORDER))) {
						if (autoProcess.hasCrossReference()) {
							ArrayList cRefIds = autoProcess.getCrossReference().getReqItem(); //getting all the cross referenced Ids
							ArrayList crossRefWorkItems = retrieveCrossReferencedWorkItems(cRefIds); //ACN025 //first cross referencd work item retrieved 
							NbaDst aWork = (NbaDst) (crossRefWorkItems.get(0)); //ACN025
							xmlDecorator = new NbaXMLDecorator(aWork.getRequirementControlSource().getText());
							//updating 1st cross referenced work item with ORDERED status.
							changeRCSforFirstWI(xmlDecorator);

							aWork.getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_ORDER));
							updateWork(getUser(), aWork);  //NBA213
							unlockWork(getUser(), aWork);  //NBA213
							//changing the status of original work item to send it to end queue
							nbaLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED)); //NBA130
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Work item is sent to END queue", getOtherStatus()));

						} else { //means no cross referenced work item is available
							if (isCancellationSupported(getWork().getNbaLob().getReqVendor())) { //means cancellation is supported
								//ACN025 code deleted
								processCancellation(); //ACN025
							} else { //means cancellation is not supported
								setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Cancellation is not supported by Provider", getOtherStatus()));
								nbaLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED)); //ACN025
							}
						}
					}
				}
			} else { // means no APRDNCHK section
				if (xmlDecorator.getRequirement().getAgentOrdered() == true) { // send email to agent saying requirement has been waived
					try { // SPR1374
						NbaTXLife txL = create9001Request(getWork());
						getWork().addNbaSource(new NbaSource(getWork().getBusinessArea(), NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION, txL.toXmlString()));
						addComment("Agent Ordered Requirement Cancel Done");
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "REQPRVEML"));
					// SPR1374 BEGIN
					} catch (NbaDataException nbAExcptn) {
						addComment(nbAExcptn.getMessage());
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
					}
					// SPR1374 END
				} else {
					if (isCancellationSupported(getWork().getNbaLob().getReqVendor())) {
						// SPR1374 BEGIN
						//ACN025 code deleted
						processCancellation(); //ACN025
						// SPR1374 END
					} else { // not supported 
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Cancellation is not supported by Provider", getOtherStatus()));
						nbaLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED)); //ACN025, NBA130
					}
				}
			}
		} else { //means source present
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Source Present", getOtherStatus()));
			nbaLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED)); //ACN025, NBA130
		}
		//begin NBA130
		getRequirementInfo().setReqStatus(nbaLob.getReqStatus()); 
		getRequirementInfo().setActionUpdate(); 
		handleHostResponse(doContractUpdate(getNbaTxLife())); 
		//end NBA130
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		//NBA020 code deleted
		return getResult();
	//NBA213 deleted code
}
/**
 * This method checks the NbaConfiguration.xml file in the provider section
 * to determine if this provider accepts cancel requests or not.
 * @param vendor is the name of the provider
 * @return boolean supported which contains true if cancellation is supported otherwise false.
 */
//NBA008 New Method
public boolean isCancellationSupported(String vendor) throws NbaBaseException {
	return NbaConfiguration.getInstance().getProvider(vendor).getCancel(); //ACN012
}
/**
 * This method will retrieve the first cross referenced work item including
 * the locking of the work item. The auto suspend will be set so that the 
 * work item will be automatically suspended by AWD if unable to retrieve and lock it.
 * @param cRefIds is the ArrayList which contains the list of all cross referenced work items AWD IDs.
 * @return the first cross refrence workitem
 */
//NBA008 New Method
//ACN025 added return type ArrayList
public ArrayList retrieveCrossReferencedWorkItems(ArrayList cRefIds) throws NbaBaseException {
	ArrayList crossRefWorkItems =  new ArrayList(cRefIds.size()); //ACN025 //SPR3290 
	//NBA213 deleted code
		ListIterator results = cRefIds.listIterator();
		//ACN025 code deleted
		// just retrieve the first cross reference.
		if(results.hasNext()) { //ACN014  
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			ReqItem reqItem = (ReqItem)results.next(); //ACN014
			retOpt.setWorkItem(reqItem.getAwdId(), false); //ACN014
			retOpt.requestSources();
			retOpt.setLockWorkItem();
			retOpt.setAutoSuspend();
			NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);  //NBA213
			crossRefWorkItems.add(aWorkItem);
		} //ACN014
	//NBA213 deleted code
	return crossRefWorkItems; //ACN025
}

/**
 * This method retrieves Requirement XMLife transaction attached for 
 * the work item.
 * @return NbaTXLife the requirement xml transaction
 */
//ACN025 New method
public NbaTXLife retrieveXMLifeTransaction() throws NbaBaseException {
	if (getTransactionCode() == NbaOliConstants.TC_TYPE_GENREQUIREORDREQ) {
		String reqUniqueId = getWork().getNbaLob().getReqUniqueID();
		if (reqUniqueId == null || reqUniqueId.length() == 0) {
			throw new NbaBaseException("Invalid Requirement Unique Info ID");
		}
		Policy policy = nbaTxLife.getPolicy();
		for (int i = 0; i < policy.getRequirementInfoCount(); i++) {
			RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
			if (reqInfo.getRequirementInfoUniqueID() != null && reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqUniqueId)) {
				// get the attachment object with the transaction
				for (int a = 0; a < reqInfo.getAttachmentCount(); a++) {
					Attachment attach = reqInfo.getAttachmentAt(a);
					//if found supplement or result type of attachment
					if (attach.getAttachmentType() == NbaOliConstants.OLI_ATTACH_REQUIREREQUEST) {
						AttachmentData attachData = attach.getAttachmentData();
						if (attachData != null) {
							try {
								NbaTXLife xmlTrans = new NbaTXLife(attachData.getPCDATA());
								return xmlTrans;
							} catch (Exception e) {
								getLogger().logException("Invalid Requirement Original Request XML Transaction ", e);
								throw new NbaBaseException("Invalid Requirement Original Request XML Transaction ", e);
							}
						}
					}
				}
			}
		}
		addComment("Requirement Original Request XML Transaction is Missing");
	} else if (getTransactionCode() == NbaOliConstants.TC_TYPE_EMAIL) {
		Iterator sources = getWork().getNbaSources().iterator();
		NbaSource source = null;
		while (sources.hasNext()) {
			source = (NbaSource) sources.next();
			if (source.getSource().getSourceType().equals(NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION)) {
				try {
					NbaTXLife xmlTrans = new NbaTXLife(source.getText());
					return xmlTrans;
				} catch (Exception e) {
					getLogger().logException("Invalid Requirement XML Transaction Source", e);
					throw new NbaBaseException("Invalid Requirement XML Transaction Source", e);
				}
			}
		}
		addComment("Requirement XML Transaction Source is Missing");
	} else {
		addComment("Invalid transaction code for cancellation");
	}
	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
	return null;
}
/**
 * If the way of communication is EMAIL, this method changes the original source with cancellation related 
 * information. It also changes the subject of email and transaction mode as "Cancel". In case of other 
 * than email communication, it gets the original request trasaction and create a new thansaction from it 
 * with updated values. It then creates an Attachment object for new cancel transaction and updates the
 * database.
 * @param xmlTransaction the original request transaction
 */
//ACN025 New Method
public void updateResourcesForCancellation(NbaTXLife xmlTransaction) throws NbaBaseException {
	if (getTransactionCode() == NbaOliConstants.TC_TYPE_EMAIL) { //means XML 9001
		String providerReadyTransaction = getProviderReadyTransaction(xmlTransaction);
		if (providerReadyTransaction != null) {
			Iterator sources = getWork().getNbaSources().iterator();
			NbaSource source = null;
			while (sources.hasNext()) {
				source = (NbaSource) sources.next();
				if (source.getSource().getSourceType().equals(NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION)) {
					source.setText(xmlTransaction.toXmlString());
					source.setUpdate();
				}
			}
			getWork().getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED));
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
		}

	} else if (getTransactionCode() == NbaOliConstants.TC_TYPE_GENREQUIREORDREQ) {
		String providerReadyTransaction = getProviderReadyTransaction(xmlTransaction);
		if (providerReadyTransaction != null) {
			//retreive requirement info object from database
			String reqUniqueId = getWork().getNbaLob().getReqUniqueID();
			Policy policy = nbaTxLife.getPolicy();
			for (int i = 0; i < policy.getRequirementInfoCount(); i++) {
				RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
				if (reqInfo.getRequirementInfoUniqueID() != null && reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqUniqueId)) {
					//add new transaction to txlife					
					NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
					Attachment attach = new Attachment();
					nbaOLifEId.setId(attach);
					attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT); //SPR3160
					attach.setAttachmentType(NbaOliConstants.OLI_ATTACH_REQCANCELTRANS);

					AttachmentData attachData = new AttachmentData();
					attachData.setActionAdd();
					attachData.setPCDATA(providerReadyTransaction);
					attach.setAttachmentData(attachData);
					attach.setActionAdd();
					reqInfo.addAttachment(attach);
					reqInfo.setActionUpdate();
					break;
				}
			}
			//NBA130 code deleted
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
			getWork().getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED));
			//NBA130 code deleted
		}
	}
}
/**
 * This method checks whether the required source for this work item 
 * has been received or not. if received return true else return false.
 * @return boolean true if supplement or result source are present.
 */
//NBA008 New Method
public boolean verifySources() throws NbaBaseException {
	getLogger().logDebug("Starting verifySources"); //NBA044
	
	//begin ACN025
	String reqUniqueId = getWork().getNbaLob().getReqUniqueID();
	if (reqUniqueId == null || reqUniqueId.length() == 0) {
		throw new NbaBaseException("Invalid Requirement Unique Info ID");
	}
	Policy policy = nbaTxLife.getPolicy();
	for (int i = 0; i < policy.getRequirementInfoCount(); i++) {
		RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
		if (reqInfo.getRequirementInfoUniqueID() != null && reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqUniqueId)) {
			// get the attachment object with the transaction
			for (int a = 0; a < reqInfo.getAttachmentCount(); a++) {
				Attachment attach = reqInfo.getAttachmentAt(a);
				//if found supplement or result type of attachment
				if (attach.getAttachmentType() == NbaOliConstants.OLI_ATTACH_REQUIRERESULTS
					|| attach.getAttachmentType() == NbaOliConstants.OLI_ATTACH_MIB_SERVRESP) {
					return true;
				}
			}
		}
	}
	//if attachement not found in database check AWD for sources
	//end ACN025
	List sources = getWork().getNbaSources();
	for (int i = 0; i < sources.size(); i++) {
		NbaSource aSource = (NbaSource) sources.get(i); //ACN025
		if ((aSource.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_RESULT))
			|| (aSource.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT))) {
			return true;
		}
	}
	return false;
}

/**
 * Answer the transaction code. it retrieve transaction code from original 
 * work item requirement control source.
 * @return the transaction code
 */
//ACN025 New Method
protected long getTransactionCode() throws NbaBaseException {
	if (transCode == -1) {
		NbaSource source = getWork().getRequirementControlSource();
		NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
		transCode = Long.parseLong(reqSource.getRequirement().getTransactionId());
	}
	return transCode;
}


/**
 * This method process the cancellation of requirement. 
 * @throws NbaBaseException
 */
//ACN025 New Method
protected void processCancellation() throws NbaBaseException {
	NbaTXLife xmlTrans = retrieveXMLifeTransaction(); //ACN025
	if (xmlTrans != null) {
		updateResourcesForCancellation(xmlTrans);
	}
}

/**
 * This method call the adapter defined in the configuration file and
 * retreive provider ready transaction. If any error occured during
 * transaction, this method will add an AWD comments and set the result to fail status.
 * @param xmlTransaction the original request transaction
 * @throws NbaBaseException
 */
//ACN025 New Method
protected String getProviderReadyTransaction(NbaTXLife xmlTransaction) throws NbaBaseException {
	if (getTransactionCode() == NbaOliConstants.TC_TYPE_EMAIL) { //means XML 9001
		TXLifeRequest txLifeRequest = xmlTransaction.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
		txLifeRequest.setTransMode(NbaOliConstants.TC_MODE_CANCEL); //Transaction Mode update
		txLifeRequest.setTransExeDate(new Date()); //Transaction Date update
		txLifeRequest.setTransExeTime(new NbaTime()); //Transaction Time update

		//Subject Change
		Attachment attachment = xmlTransaction.getPrimaryHolding().getAttachmentAt(0);
		attachment.setAttachmentKey(changeSubject(attachment.getAttachmentKey()));
		return xmlTransaction.toXmlString();

	} else if (getTransactionCode() == NbaOliConstants.TC_TYPE_GENREQUIREORDREQ) {
		NbaTXLife clonedXmlTransaction = (NbaTXLife) xmlTransaction.clone(false);
		TXLifeRequest txLifeRequest = clonedXmlTransaction.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
		txLifeRequest.setTransMode(NbaOliConstants.TC_MODE_CANCEL); //Transaction Mode update
		txLifeRequest.setTransExeDate(new Date()); //Transaction Date update
		txLifeRequest.setTransExeTime(new NbaTime()); //Transaction Time update
		NbaLob lob = getWork().getNbaLob();
		Policy clonedpolicy = clonedXmlTransaction.getPolicy();

		if (clonedpolicy.getRequirementInfoCount() > 1) { //means original request was bundled
			RequirementInfo originalReqInfo = null;
			for (int i = 0; i < clonedpolicy.getRequirementInfoCount(); i++) {
				RequirementInfo reqInfo = clonedpolicy.getRequirementInfoAt(i);
				if (lob.getReqType() == reqInfo.getReqCode()) {
					originalReqInfo = reqInfo;
				}
			}
			if (originalReqInfo != null) {
				clonedpolicy.getRequirementInfo().clear();
				clonedpolicy.addRequirementInfo(originalReqInfo);
			} else {
				throw new NbaBaseException("Missing RequirementInfo for original requirement");
			}
		}

		RequirementInfo originalReqInfo = clonedpolicy.getRequirementInfoAt(0);
		originalReqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_CANCELLED);
		originalReqInfo.setRequestedDate(new Date());

		//get provider ready transaction
		List aList = new ArrayList(1); //SPR3290
		aList.add(clonedXmlTransaction);
		HashMap aMap = null;

		//calling provider adapter to get provider ready message
		NbaProviderAdapterFacade facade = new NbaProviderAdapterFacade(getWork(), getUser());
		aMap = (HashMap) facade.convertXmlToProviderFormat(aList);
		if (aMap.get(((NbaTXLife) aList.get(0)).getTransRefGuid()) != null) { //means provider error
			addComment((String) aMap.get(((NbaTXLife) aList.get(0)).getTransRefGuid()));
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
		} else { //means not provider error
			return (String) aMap.get(NbaConstants.TRANSACTION);
		}		
	}
	return null;
}

/**
 * Change the work item status to pass status. Update and unlock workitem. 
 * @throws NbaBaseException
 */
//SPR3416 New Method
protected void processManualRequirement() throws NbaBaseException{
	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
	changeStatus(getResult().getStatus());
	doUpdateWorkItem();
}
}
