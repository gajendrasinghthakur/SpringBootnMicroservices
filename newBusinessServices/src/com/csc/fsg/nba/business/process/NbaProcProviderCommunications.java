package com.csc.fsg.nba.business.process;

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
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import Snow.Snowbnd;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.copy.CommitContractCopyBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapterFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.vo.nbaschema.NbAccelerator;
import com.csc.fsg.nba.vo.nbaschema.ProviderReadyTransactions;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * NbaProcProviderCommunications is the abstract class that provides basic processing
 * for all provider communications.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td><tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1168</td><td>Version 3</td><td>Incorrect message displayed in AWD History</td></tr>
 * <tr><td>SPR1770</td><td>Version 4</td><td>When requirements are first ordered and moved to the NBORDERD queue they should not be suspended initially.</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>401/402 Migration</td></tr>
 * <tr><td>SPR1214</td><td>Version 4</td><td>The EMSI adapter does not throw an error when the required authorization is not available for the requirement APS</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN025</td><td>Version 5</td><td>Requirement Cancellation</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>SPR2738</td><td>Version 5</td><td>The submit date in the MIB Report update 402 transaction is being set incorrectly.</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interface - MIB</td></tr>
 * <tr><td>ALS2814</td><td>AXA Life Phase 1</td><td>QC# 1555 Ad hoc: requirement followup functionality</td></tr>
 * <tr><td>ALS5133</td><td>AXA Life Phase 1</td><td>QC #4145 - 3.7.31 Pharmaceutical Profile requirement not received by Exam One</td></tr>
 * <tr><td>ALS5366</td><td>AXA Life Phase 1</td><td>QC #4542 - MIB does not transmit on a case where MIB was previously transmitted without the occupation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public abstract class NbaProcProviderCommunications extends NbaAutomatedProcess implements NbaConstants {
	/** String for target, which can an URL or a file path from the configuration file */
	private java.lang.String target = null;

	/** The Provider object */
	public Provider provider; //ACN012

	//ACN014 code deleted
	/** String representing the filename */
	public final java.lang.String FILENAME = "FILENAME";

	/** String representing the provider data */
	public final java.lang.String DATA = "DATA";

	//SPR2380 removed logger
	private static boolean updateOccurred = false; //ACN014

	protected static final String AUTH_SENT = "Authorization sent"; //NBA212

	private boolean is402Request = false; //ALS5366

	private String attachment402 = null; //ALS5366

	/**
	 * NbaProcProviderCommunications default constructor.
	 */
	public NbaProcProviderCommunications() {
		super();
	}

	//NBA103 - removed method

	/**
	 * This abstract method allows each provider communication subclass the ability to perform any processing that may be required specifically for
	 * their provider.
	 * 
	 * @param data
	 *                an XML transaction for the requirement
	 * @return an Object determined by the subclass
	 * @throws NbaBaseException
	 */
	// ACN014 Changed method signature
	public Object doProviderSpecificProcessing(Object data) throws NbaBaseException {
		return data;
	}

	/**
	 * This abstract method allows each provider communication subclass the ability to evaluate the response from their provider to determine if the
	 * process was successful.
	 * 
	 * @param response
	 *                a <code>String</code> that contains the response from the provider
	 * @return <code>true</code> if the response is successful and <code>false</code> if unsuccessful
	 * @throws NbaBaseException
	 */
	public abstract boolean evaluateResponse(String response) throws NbaBaseException;

	/**
	 * This method retrieves the XML121 transaction from the database by locating the associated RequirementInfo object and looking for an attachment
	 * whose type is OLI_ATTACH_DOC
	 * 
	 * @return an Object determined by the subclass
	 * @throws NbaBaseException
	 */
	//ACN014 New Method
	public Object getXmlTransaction() throws NbaBaseException {//ACN009
		// SPR3290 code deleted
		Iterator reqIter = nbaTxLife.getPolicy().getRequirementInfo().iterator();
		NbaLob lob = getWork().getNbaLob(); //ACN025
		String reqUniqueId = lob.getReqUniqueID(); //ACN025
		if (reqUniqueId == null || reqUniqueId.length() <= 0) {
			throw new NbaBaseException("Requirement Unique Info ID invalid");
		}
		while (reqIter.hasNext()) {
			RequirementInfo reqInfo = (RequirementInfo) reqIter.next();
			if (reqInfo.getRequirementInfoUniqueID() != null && reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqUniqueId)) {
				// get the attachment object with the transaction
				//NBA212 code deleted
				Iterator attIter = reqInfo.getAttachment().iterator();
				//NBA212 code deleted
				long reqStatus = Long.parseLong(lob.getReqStatus()); //ACN025
				while (attIter.hasNext()) {
					Attachment attach = (Attachment) attIter.next();
					//begin ACN009
					long attachType = attach.getAttachmentType();
					//begin ACN025
					if (reqStatus == NbaOliConstants.OLI_REQSTAT_CANCELLED) {
						if (attachType == NbaOliConstants.OLI_ATTACH_REQCANCELTRANS) {
							return attach.getAttachmentData().getPCDATA();
						}
					} else {
						if (lob.getWorkType().equals(NbaConstants.A_WT_TEMP_REQUIREMENT)) { //ACN025
							if (attachType == NbaOliConstants.OLI_ATTACH_MIB402 && attach.hasDescription()
									&& attach.getDescription().equals(NbaConstants.MIB_UPDATE_PENDING_TRANSMISSION)) {
								is402Request = true; //ALS5366
								setAttachment402(attach.getId());
								updateMIBReportForTransmissionDetail(attach); //SPR2738
								return attach.getAttachmentData().getPCDATA();
							}
						} else if (attachType == NbaOliConstants.OLI_ATTACH_REQUIREREQUEST || attachType == NbaOliConstants.OLI_ATTACH_MIB401) {
							return attach.getAttachmentData().getPCDATA();
						}
					}
					//end ACN025
					//end ACN009
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * This method drives the Provider Communications process. It first initializes the statuses for the work item and then retrieves the work item
	 * from AWD. It then looks through the NbaSources to locate the provider-ready transaction and the authorization source. If the provider-ready
	 * transaction is not found, the process fails. An <code>NbaProviderAdapterFacade</code> is created to help process the work item. Provider
	 * specific processing is executed so that the provider communication will succeed and then the transaction is submitted to the provider. When the
	 * response is received, it is evaluated and, if unsuccessful, an error is repoted. If the transaction was sent successfully, the process will
	 * determine if the work item needs to be suspended by calling a VP/MS model, and, if so, suspends the work item. If no suspension is required,
	 * the work item is updated to move to the next queue.
	 * 
	 * @param user
	 *                the user/process for whom the process is being executed
	 * @param work
	 *                a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return statusProcessFailed();
		}
		//ACN014 Deleted Code
		NbaProviderAdapterFacade adapter = new NbaProviderAdapterFacade(work, user);//ACN014
		setProvider(adapter.getProvider());
		//ACN014 deleted code
		Object data = getXmlTransaction();//ACN014 ACN009
		//ACN014 begin
		if (data != null) {
			data = addAuthorizations(data);
		}
		// ACN014 end
		data = doProviderSpecificProcessing(data); //ACN009, ACN014
		if (data == null) {
			// APSL4165 code deleted
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_FUNC_PROVIDER_TRANS);// APSL4165
		} //NBA213
		getLogger().logInfo(data); //ACN009
		//NBA213 code deleted
		String response = (String) adapter.sendMessageToProvider(target, data, user); // AXAL3.7.31
		if (response == null) {
			// APSL4165 code deleted
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_FUNC_TECH_PROVIDER);// APSL4165
		} else if (evaluateResponse(response) == true) { // ACN014
			// begin NBA213
			if (getWork().getNbaLob().getWorkType().equals(NbaConstants.A_WT_TEMP_REQUIREMENT) || updateOccurred) {
				doContractUpdate();
			}
			// end NBA213
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUCCESSFUL", getPassStatus()));
			changeStatus(getResult().getStatus());
			int suspendDays = getSuspendDays();
			if (suspendDays <= 0) { // SPR1770
				doUpdateWorkItem();
			} else {
				suspendTransaction(suspendDays);
			}
			// NBA213 code deleted
		} else {
			//begin ALS5366
			if (is402Request) {
				updateFailed402();
				doContractUpdate();
			}
			//end ALS5366
			//APSL4165 code deleted
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH); //APSL4165
		}
		return getResult();
	}

	/**
	 * This method searches the RequirementInfo object for an Attachment this is considered an Authorization for the requirement. If one is found, it
	 * is added to the NbaTXLife 121 transaction found in the data parameter. Additionally, the RequirementDetails element of the RequirementInfo
	 * object in the database is updated to indicate that the Authorization was sent with the 121 transaction.
	 * 
	 * @param data
	 *                the XML transaction to which an Attachment object is added
	 * @return the update 121 transaction or the original data object
	 * @throws NbaBaseException
	 */
	//ACN014 New Method
	//NBA212 changed method visibility
	protected Object addAuthorizations(Object data) throws NbaBaseException {
		//begin NBA212
		NbaTXLife reqTxLife = null;
		try {
			reqTxLife = new NbaTXLife((String) data);
			setNbaOLifEId(new NbaOLifEId(reqTxLife));
		} catch (Exception whoops) {
			throw new NbaBaseException("Unable to un-marshall XML", whoops, NbaExceptionType.FATAL);
		}
		//end NBA212
		//ALS5133 code deleted...don't add authorizations
		return reqTxLife.toXmlString();
		//NBA212 code deleted
	}

	/**
	 * Locate the attachment Source in the parent case and create Attachment objects for each image/text file associated with it.
	 * 
	 * @param attachment
	 *                the original Type 57 Attachment from the RequirementInfo of the Contract for the current Requirement.
	 * @return List containing the copies of the original Attachment updated with the image/text file contents
	 * @throws NbaBaseException
	 */
	//NBA130 New Method
	//NBA212 changed method visibility and signature
	protected List getAttachmentSource(Attachment origAttachment) throws NbaBaseException {
		//begin NBA212
		List copies = new ArrayList();
		AttachmentData origAttachmentData = origAttachment.getAttachmentData();
		if (null == origAttachmentData) {
			origAttachmentData = new AttachmentData();
		}
		try {
			NbaSource nbaSource = getMatchingSourceFromCase(origAttachment.getAttachmentSource()); //Get Source from parent Case
			if (nbaSource != null) {
				Attachment newAttachment;
				AttachmentData newAttachmentData;
				if (nbaSource.isImageFormat()) { //Get the image(s) for the Source from the workflow system
					List images = WorkflowServiceHelper.getBase64SourceImage(getUser(), nbaSource);
					for (int i = 0; i < images.size(); i++) {
						newAttachment = origAttachment.clone(false);
						newAttachment.deleteId();
						getNbaOLifEId().setId(newAttachment);
						newAttachmentData = origAttachmentData.clone(false);
						newAttachment.setAttachmentData(newAttachmentData);
						newAttachment.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
						newAttachmentData.setPCDATA((String) images.get(i));
						newAttachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE);
						newAttachment.setImageType(NbaOliConstants.OLI_IMAGE_TIFF);
						newAttachment.setAttachmentSource(null);
						copies.add(newAttachment);
					}
				} else { //Get the text from the Source Item
					newAttachment = origAttachment.clone(false);
					newAttachmentData = origAttachmentData.clone(false);
					newAttachment.setAttachmentData(newAttachmentData);
					newAttachment.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
					newAttachmentData.setPCDATA(nbaSource.getText());
					newAttachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
					newAttachment.setAttachmentSource(null);
					copies.add(newAttachment);
				}
			}
			return copies;
		} catch (RemoteException whoops) {
			throw new NbaBaseException("Remote Exception occurred while creating Attachments", whoops, NbaExceptionType.FATAL);
		} catch (NbaBaseException whoops) {
			whoops.forceFatalExceptionType();
			throw whoops;
		}
		//end NBA212
	}

	/**
	 * Searches the RequirementInfo object for an Attachment object that is an authorization - OLI_ATTACH_ACORD751
	 * 
	 * @param temp
	 *                a RequirementInfo object
	 * @return an Attachment object containing the authorization or null if not found
	 */
	//ACN014 new method
	private Attachment getAuthorizationAttachment(RequirementInfo temp) {
		//NBA212 code deleted
		int attachCount = temp.getAttachmentCount();
		Attachment attach; //NBA212
		for (int i = 0; i < attachCount; i++) {
			attach = temp.getAttachmentAt(i); //NBA212
			if (attach.getAttachmentType() == NbaOliConstants.OLI_ATTACH_ACORD751) {
				return attach;
			}
		}
		return null;
	}

	/**
	 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to call VP/MS to execute the entryPoint.
	 * 
	 * @param entryPoint
	 *                the entry point in the VP/MS model to be executed
	 * @return the results from the VP/MS call in the form of an <code>NbaVpmsResultsData</code> object
	 * @throws NbaVpmsException
	 */
	public NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			//BEGIN NBA130
			oinkData.setContractSource(nbaTxLife);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			oinkRequest.setRequirementIdFilter(requirementInfo.getId());
			//END NBA130
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			//SPR3362 code deleted
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Provider Communication Process Problem" + NbaVpmsException.VPMS_EXCEPTION, re);
			//begin SPR3362
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		//end SPR3362
	}

	/**
	 * Answers the provider created from the NbaConfiguration file.
	 * 
	 * @return an NbaConfigProvider
	 */
	//ACN012 CHANGED SIGNATURE
	public Provider getProvider() {
		return provider;
	}

	/**
	 * This method retrieves the followUpFreq days from RequirementInfoExtension.
	 * 
	 * @return the number of suspend days from the model or 0 if no value returned
	 * @throws NbaBaseException
	 */
	public int getSuspendDays() throws NbaBaseException {
		getLogger().logDebug("Getting suspend days"); //NBA044
		//ALS5781 code deleted.
		return 0;
		//End ALS2814
	}

	/**
	 * Answers the target for the provider. The target may be a URL or a file path from the NbaConfiguration file.
	 * 
	 * @return the target for this provider
	 */
	public java.lang.String getTarget() {
		return target;
	}

	/**
	 * Answers the provider-ready transaction for this work item and process (based on requirement status).
	 * 
	 * @param an
	 *                Xml string
	 * @return a String of data that is the provider-ready transaction or null if none found
	 * @throws NbaBaseException
	 */
	public String getTransaction(String aReqCtlSrc) throws NbaBaseException {
		NbaXMLDecorator reqSource = new NbaXMLDecorator(aReqCtlSrc);
		NbAccelerator nba = reqSource.getNbAccelerator();
		ProviderReadyTransactions trans = nba.getProviderReadyTransactions();
		for (int i = 0; i < trans.getProviderRequestCount(); i++) {
			if (trans.getProviderRequestAt(i).getRequestType() == Integer.parseInt(getWork().getNbaLob().getReqStatus())) {
				return trans.getProviderRequestAt(i).getPCDATA();
			}
		}
		return null;
	}

	/**
	 * This abstract method allows each provider communication subclass the ability to initialize the target needed for their provider and for the
	 * specific requirement, if necessary.
	 * 
	 * @throws NbaBaseException
	 */
	public abstract void initializeTarget() throws NbaBaseException;

	/**
	 * Sets the provider with information from the NbaConfiguration file.
	 * 
	 * @param newProvider
	 *                the provider retrieved from the NbaConfiguration file
	 */
	//ACN012 CHANGED SIGNATURE
	public void setProvider(Provider newProvider) {
		provider = newProvider;
	}

	/**
	 * Sets the target for the provider. The target may be a URL or a file path from the NbaConfiguration file.
	 * 
	 * @param newTarget
	 *                the target from the NbaConfiguration file
	 */
	public void setTarget(java.lang.String newTarget) {
		target = newTarget;
	}

	/**
	 * This method suspends a work item by using the work item information and the supplied suspend date to populate the suspendVO.
	 * 
	 * @param suspendDays
	 *                the number of days to suspend
	 * @throws NbaBaseException
	 */
	public void suspendTransaction(int suspendDays) throws NbaBaseException {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
		Date reqSusDate = (calendar.getTime());
		addComment("Suspended awaiting matching work item"); //SPR1168
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(reqSusDate);
		updateForSuspend(suspendVO);
	}

	/**
	 * Since the work item must be suspended before it can be unlocked, this method is used instead of the superclass method to update AWD.
	 * <P>
	 * This method updates the work item in the AWD system, suspends the work item using the supsendVO, and then unlocks the work item.
	 * 
	 * @param suspendVO
	 *                the suspend value object created by the process to be used in suspending the work item.
	 * @throws NbaBaseException
	 */
	public void updateForSuspend(NbaSuspendVO suspendVO) throws NbaBaseException {
		getLogger().logDebug("Starting updateForSuspend"); //NBA044
		getWork().getNbaTransaction().getTransaction().setUnlock(NbaConstants.NO_VALUE); //NBA213
		updateWork(getUser(), getWork()); //NBA213
		suspendWork(getUser(), suspendVO); //NBA213
		//NBA213 code deleted
	}

	/**
	 * Update MIB report with the transmission details.
	 * 
	 * @param attach
	 *                the Attachment object that to be updated.
	 * @throws NbaBaseException
	 *                 if could not unmarshal NbaTXlife object from attachment data.
	 */
	//SPR2738 New Method
	protected void updateMIBReportForTransmissionDetail(Attachment attach) throws NbaBaseException {
		attach.setDescription(NbaConstants.MIB_UPDATE_TRANSMITTED);
		attach.setActionUpdate();
		//set submit date as current date in MIB report
		if (attach.hasAttachmentData()) {
			AttachmentData attachmentData = attach.getAttachmentData();
			NbaTXLife life;
			try {
				life = new NbaTXLife(attachmentData.getPCDATA());
			} catch (Exception e) {
				getLogger().logException("Could not create NbaTXLife object from attachemnt data for MIB report", e);
				throw new NbaBaseException("Could not create NbaTXLife object from attachemnt data for MIB report", e);
			}
			if (null != life) {
				OLifE olife = life.getOLifE();
				if (null != olife && olife.getFormInstanceCount() > 0) {
					olife.getFormInstanceAt(0).setSubmitDate(new Date());
					attachmentData.setPCDATA(life.toXmlString());
					attachmentData.setActionUpdate();
				}
			}
		}
	}

	/**
	 * Create and return copies of the Type 57 Attachment from the RequirementInfo of the Contract for the current Requirement. For Attachments which
	 * are Images, retrieve the Images from the workflow system andreturn an Attachemnt for each image. Otherwise return a copy of the Attachment.
	 * 
	 * @param attach -
	 *                the original Type 57 Attachment from the RequirementInfo of the Contract for the current Requirement.
	 * @return List containing the copies
	 * @throws NbaBaseException
	 */
	//NBA212 new Method
	protected List copyAttachments(Attachment attach) throws NbaBaseException {
		if (attach.getAttachmentLocation() == NbaOliConstants.OLI_URLREFERENCE) {
			return getAttachmentSource(attach);
		}
		List copies = new ArrayList();
		copies.add(attach.clone(false));
		return copies;
	}

	/**
	 * Locate and return an NbaSource for the Source item on the Case whose id matches the requested id
	 * 
	 * @param id -
	 *                the requested id
	 * @return NbaSource
	 * @throws RemoteException
	 * @throws NbaBaseException
	 */
	//NBA212 New Method
	protected NbaSource getMatchingSourceFromCase(String id) throws RemoteException, NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.requestSources();
		//get case from awd
		NbaDst parentCase = WorkflowServiceHelper.retrieveWorkItem(getUser(), retOpt);
		Iterator sourcesIter = parentCase.getNbaSources().iterator();
		NbaSource aSource;
		while (sourcesIter.hasNext()) {
			aSource = (NbaSource) sourcesIter.next();
			if (aSource.getID().equalsIgnoreCase(id)) {
				return aSource;
			}
		}
		return null;
	}

	protected String removeNameSpace(String inputXml) {
		String outPutXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <TXLife>";
		if (inputXml != null) { //CR60669-APS Order Authorization
			outPutXml = outPutXml + inputXml.substring(inputXml.indexOf("<User"));
		}
		return outPutXml;
	}

	protected String addNameSpace(String inputXml, String nameSpace) {
		StringBuffer outPutbuffer = new StringBuffer();
		outPutbuffer.append(inputXml.substring(0, inputXml.indexOf("e>") + 1));
		outPutbuffer.append(nameSpace);
		outPutbuffer.append(inputXml.substring(inputXml.indexOf("<User")));
		return outPutbuffer.toString();
	}

	/**
	 * Determine whether we received a failure response from the web service.
	 * 
	 * @param nbaTXLifeResponse
	 * @return boolean
	 */
	// AXAL3.7.31 New Method
	protected boolean isTransactionError(NbaTXLife nbaTXLifeResponse) {
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		if (transResult != null && transResult.hasResultCode()) {
			return transResult.getResultCode() > NbaOliConstants.TC_RESCODE_SUCCESSINFO;
		}
		return true;
	}

	/**
	 * Handle web service failure.
	 * 
	 * @param nbaTXLifeResponse
	 *                webservice failure response
	 */
	// AXAL3.7.31 New Method
	protected void handleProviderWebServiceFailure(NbaTXLife nbaTXLifeResponse) {
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		List errors = new ArrayList();
		if (transResult != null) {
			for (int i = 0; i < transResult.getResultInfoCount(); i++) {
				if (transResult.getResultInfoAt(i).hasResultInfoDesc()) {
					errors.add(transResult.getResultInfoAt(i).getResultInfoDesc());
				} else if (transResult.getResultInfoAt(i).hasResultInfoCode()) {
					errors.add("ResultInfoCode " + String.valueOf(transResult.getResultInfoAt(i).getResultInfoCode()));
				}
			}
		}
		if (errors.size() < 1) {
			errors.add("Error information not present on response or response was malformed");
		}
		addComments(errors); //add error messages on the workitem
	}

	/*
	 * Update 402 request so we have an indication that it failed and we can add a new request.
	 */
	//ALS5366 new method
	private void updateFailed402() throws NbaBaseException {
		Iterator reqIter = nbaTxLife.getPolicy().getRequirementInfo().iterator();
		NbaLob lob = getWork().getNbaLob(); //ACN025
		String reqUniqueId = lob.getReqUniqueID(); //ACN025
		while (reqIter.hasNext()) {
			RequirementInfo reqInfo = (RequirementInfo) reqIter.next();
			if (reqInfo.getRequirementInfoUniqueID() != null && reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqUniqueId)) {
				// get the attachment object with the transaction
				Iterator attIter = reqInfo.getAttachment().iterator();
				long reqStatus = Long.parseLong(lob.getReqStatus()); //ACN025
				while (attIter.hasNext()) {
					Attachment attach = (Attachment) attIter.next();
					long attachType = attach.getAttachmentType();
					if (lob.getWorkType().equals(NbaConstants.A_WT_TEMP_REQUIREMENT)) {
						if (attachType == NbaOliConstants.OLI_ATTACH_MIB402 && attach.getId().equalsIgnoreCase(getAttachment402())) {
							updateMIBReportForFailedTransmission(attach); //SPR2738
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Update MIB report with the transmission details.
	 * 
	 * @param attach
	 *                the Attachment object that to be updated.
	 * @throws NbaBaseException
	 *                 if could not unmarshal NbaTXlife object from attachment data.
	 */
	//ALS5366 New Method
	protected void updateMIBReportForFailedTransmission(Attachment attach) throws NbaBaseException {
		attach.setDescription(NbaConstants.MIB_FAILED);
		attach.setActionUpdate();
		//set submit date as current date in MIB report
		if (attach.hasAttachmentData()) {
			AttachmentData attachmentData = attach.getAttachmentData();
			NbaTXLife life;
			try {
				life = new NbaTXLife(attachmentData.getPCDATA());
			} catch (Exception e) {
				getLogger().logException("Could not create NbaTXLife object from attachemnt data for MIB report", e);
				throw new NbaBaseException("Could not create NbaTXLife object from attachemnt data for MIB report", e);
			}
			if (null != life) {
				OLifE olife = life.getOLifE();
				if (null != olife && olife.getFormInstanceCount() > 0) {
					olife.getFormInstanceAt(0).deleteSubmitDate();
					attachmentData.setPCDATA(life.toXmlString());
					attachmentData.setActionUpdate();
				}
			}
		}
	}

	/**
	 * @return Returns the attachment402.
	 */
	//ALS5366
	public String getAttachment402() {
		return attachment402;
	}

	/**
	 * @param attachment402
	 *                The attachment402 to set.
	 */
	//ALS5366
	public void setAttachment402(String attachment402) {
		this.attachment402 = attachment402;
	}

	//CR60669-APS Order Authorization New Method
	public NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(this.getClass().getName() + " Starting retrieveWorkItem for " + nbaDst.getID());
		}
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(nbaDst.getID(), false);
		retrieveOptionsValueObject.requestSources();
		return retrieveWorkItem(getUser(), retrieveOptionsValueObject);
	}
	
	/**
	 * Create Attachments for the Images associated with the Source. For Content Services, there may be more than one image.
	 * @param aSource - the Source containing the Image identification information
	 * @param attachmentType - the type of attachments to create
	 * @return List containing the newly created Attachments
	 * @throws NbaBaseException
	 * @throws NbaNetServerException
	 */
	//CR60669-APS Order Authorization New Method
	protected List createAttachmentsForImages(NbaSource aSource, long attachmentType) throws NbaBaseException, NbaNetServerException {
		List newAttachments = new ArrayList();
		List images = new ArrayList();
		try {
			images = WorkflowServiceHelper.getBase64SourceImage(getUser(), aSource);
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();
			throw e;
		} catch (Throwable t) {
			NbaNetServerException e = new NbaNetServerException(NbaNetServerException.GET_SOURCE_IMAGE, t, NbaExceptionType.FATAL);
			throw e;
		}
		//Begin APSL3641
		if(getWork().getNbaLob().getReqVendor().equalsIgnoreCase("PARAMED") || getWork().getNbaLob().getReqVendor().equalsIgnoreCase("EIS")){ //NBLXA-2073
			//NBLXA-1302
			if(images.size() > 1) {
				String guid = NbaUtils.getGUID();//new changes by pratik
				Attachment attach = initializeNewAttachment(attachmentType);
				attach.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
				attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE);
				attach.setImageType(NbaOliConstants.OLI_IMAGE_TIFF);
				AttachmentData attachData = new AttachmentData();
				
				//NBLXA-1302
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				System.out.println(" before method execution "+timestamp);
				//NBLXA-1302
				attachData.setPCDATA(generateMultiPageTiffEncodedString(images, guid));
				//NBLXA-1302
				Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
				System.out.println(" after method execution "+timestamp2);
				
				attach.setAttachmentData(attachData);
				newAttachments.add(attach);
			}else{
				for (int i = images.size()-1; i >=0; i--) {
					Attachment attach = initializeNewAttachment(attachmentType);
					attach.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
					attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE);
					attach.setImageType(NbaOliConstants.OLI_IMAGE_TIFF);
					AttachmentData attachData = new AttachmentData();
					attachData.setPCDATA((String) images.get(i));
					attach.setAttachmentData(attachData);
					newAttachments.add(attach);
				}
			}//NBLXA-1302
		}else{
			for (int i = 0; i < images.size(); i++) {
				Attachment attach = initializeNewAttachment(attachmentType);
				attach.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
				attach.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE);
				attach.setImageType(NbaOliConstants.OLI_IMAGE_TIFF);
				AttachmentData attachData = new AttachmentData();
				attachData.setPCDATA((String) images.get(i));
				attach.setAttachmentData(attachData);
				newAttachments.add(attach);
			}
		}
		//End APSL3641
		return newAttachments;
	}
	
	/**
	 * NBLXA-1302
     * Generate Multi Page Tiff image from List of images and return combine image encoded string. 
     * @param List imageStreamList
     * @param String newFileName
     * @return String
     */
    public String generateMultiPageTiffEncodedString(List imageStreamList, String newFileName) throws NbaBaseException {
		String encodeFinalImage = null;
		ImageWriter writer = null;
		ImageOutputStream ios = null;
		
		try {
			//Create the temporary output file on the disk
			File tempFile = File.createTempFile(newFileName, ".tif");
			ImageIO.scanForPlugins();
			ios = ImageIO.createImageOutputStream(tempFile);
			//Get the appropriate Tiff Image Writer
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
			if (writers != null && writers.hasNext()) {
				writer = writers.next();
				writer.setOutput(ios);
				ImageWriteParam param = writer.getDefaultWriteParam();
				writer.prepareWriteSequence(null);
				
				//Loop through all image files and write them to output tiff image
				for (int i = 0; i < imageStreamList.size(); i++) {
						
				    	byte[] decodeImage = NbaBase64.decode((String) imageStreamList.get(i));
						InputStream fis = new BufferedInputStream(new ByteArrayInputStream(decodeImage));
						BufferedImage image = ImageIO.read(fis);
						IIOImage img = new IIOImage(image, null, null);
						writer.writeToSequence(img, param);	
						image.flush();
						if (fis != null) {
							fis.close();
						}
				}
				writer.endWriteSequence();
			}
		   ios.flush();
		   byte[] imageInByte = saveAs_Tiff_OutputByteArray(tempFile.getAbsolutePath());
		   encodeFinalImage = NbaBase64.encodeBytes(imageInByte);
     		
		} catch (Exception ex) {
			throw new NbaBaseException("Error in Processing image files ", ex);
		} finally {
			if (null != writer)
				writer.dispose();
			if (null != ios)
				try {
					ios.close();
				} catch (IOException ioe) {
					throw new NbaBaseException("Error in closing ios", ioe);
				}
		}
		return encodeFinalImage;
	}
    
    
    /**
     * NBLXA-1302
     * return byte array of combine tiff image.
     * @param filename
     * @return
     */
    public static byte[] saveAs_Tiff_OutputByteArray(String filename)
   	{
   		Snowbnd snbd = new Snowbnd();
   		File input = new File(filename);
   		int pages = snbd.IMGLOW_get_pages(input.toString());
   		byte imagebytearray[] = new byte[(int) (input.length() * 3)];// Three times the size of the input image
   		int outputsize = 0;
   		for (int pg = 0; pg < pages; pg++)
   		{
   			snbd.IMG_decompress_bitmap(input.toString(), pg); // Decompress each page
   			outputsize = snbd.IMG_save_bitmap(imagebytearray, Snow.Defines.TIFF_G4_FAX);// Save to tmp byte array
   		}
  		// Correct byte array size
   		byte imagedata[] = new byte[outputsize];
   		System.arraycopy(imagebytearray, 0, imagedata, 0, outputsize);
   		return imagedata; // corrected data
   	}
}
