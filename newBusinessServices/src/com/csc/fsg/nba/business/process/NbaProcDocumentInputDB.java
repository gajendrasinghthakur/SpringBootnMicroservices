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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.csc.fs.SystemAccess;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.AutomatedProcess;
import com.csc.fsg.nba.vo.configuration.DocumentDescription;
import com.csc.fsg.nba.vo.configuration.DocumentInputDefinition;
import com.csc.fsg.nba.vo.configuration.DocumentInputDefinitions;
import com.csc.fsg.nba.vo.configuration.DocumentSource;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

/**
 * NbaProcDocumentInput performs
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead> <th align=left>Project</th> <th align=left>Release</th> <th align=left>Description</th> </thead>
 * <tr>
 * <td>SR515492</td>
 * <td>Discretionary</td>
 * <td>E-App Integration</td>
 * <tr>
 * <td>APSL2735</td>
 * <td>Discretionary</td>
 * <td>Electronic Initial Premium</td>
 * 
 * </tr>
 * </tr>
 * <tr>
 * <td>APSL2808</td>
 * <td>Discretionary</td>
 * <td>Simplified Issue</td>
 * <td>APSL3888</td>
 * <td>Discretionary</td>
 * <td>Electronic Initial Premium</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaProcDocumentInputDB extends com.csc.fsg.nba.business.process.NbaAutomatedProcess {
	private static final String NOT_LOGGED_ON_ERROR = "SYS0111";

	protected static final String ACTION_LOCK = "L";

	protected static final String DELETE = "DELETE";

	protected static final String DOCUMENTINPUTDEFINITION_ACCESS_ERROR = "Unable to access DocumentInputDefinition configuration information for user: ";

	protected static final String NO_DOCUMENTINPUTDEFINITIONS = "No DocumentInputDefinitions defined for user";

	protected static final String NO_LOGGER = "NbaProcDocumentInput could not get a logger from the factory.";

	protected static final String READ_REQUEST = "READ";

	protected static final String RESULT = "result";

	protected static final String WT_CASE = "C";

	protected static final String WT_TRANSACTION = "T";

	protected Map xmlSources;

	private SOAPMessage soapMessage;

	protected DocumentDescription documentDescription;

	NbaXML103SubmitPolicyHelper submitPolicyHelper = new NbaXML103SubmitPolicyHelper();

	NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();

	/**
	 * NbaProcAppSubmit default constructor.
	 */

	public NbaProcDocumentInputDB() {
		super();
	}

	/**
	 * SR515492 New Method Create a Case work item from the information in the DocumentDescription.
	 */

	protected void constructCase() {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
					"Creating Case: Business Area=" + getDocumentDescription().getBusinessArea() + ", Work Type="
							+ getDocumentDescription().getWorkType() + ", Status=" + getDocumentDescription().getStatus() + ", Source Type="
							+ getDocumentDescription().getSourceType());
		}
		WorkItem awdCase = new WorkItem();
		// set Business Area, Work type and Status
		awdCase.setBusinessArea(getDocumentDescription().getBusinessArea());
		awdCase.setWorkType(getDocumentDescription().getWorkType());
		awdCase.setStatus(getDocumentDescription().getStatus());
		awdCase.setLock("Y");
		awdCase.setAction(ACTION_LOCK);
		awdCase.setRecordType(WT_CASE);
		awdCase.setCreate("Y");
		getWork().addCase(awdCase);
		getWork().setWork(new NbaCase(awdCase));
		if (getNbaTxLife().getPolicy() != null) {
			PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
			String distChannel = null;
			if (policyExtension != null) {
				distChannel = String.valueOf(policyExtension.getDistributionChannel());
			}
			getWork().getNbaLob().setDistChannel(distChannel);
		}
	}

	/**
	 * SR515492 New Method Create the Work Item and attach the Source to it. Create a dummy StatusProvider. The status is obtained from the
	 * DocumentDescription
	 * 
	 * @throws NbaBaseException
	 */

	protected void constructWorkItemsAndSources(NbaDst nbaDst, NbaUserVO nbaUserVO) throws Exception {
		getWork().setUserID(getUser().getUserID());
		getWork().setPassword(getUser().getPassword());
		nbaDst.setNbaUserVO(nbaUserVO);
		// Construct The case WI
		constructCase();
		// QC8410 Begin
		setStatusProvider(new NbaProcessStatusProvider(getUser(), getWork()));
		updateWork();
		retreiveWorkFromAWD();
		// QC8410 End
		createSources();
	}

	/**
	 * Create the Sources.
	 * 
	 * @throws NbaBaseException
	 * @throws IndexOutOfBoundsException
	 */
	protected void createSources() throws IndexOutOfBoundsException, NbaBaseException {
		getWork().getCase().setSourceChildren(new ArrayList());
		String distChannel = null;
		if (getNbaTxLife().getPolicy() != null) {
			PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
			if (policyExtension != null) {
				distChannel = String.valueOf(policyExtension.getDistributionChannel());
			}
			getWork().getNbaLob().setDistChannel(distChannel);
		}
		List formInstanceList = getNbaTxLife().getOLifE().getFormInstance();
		Iterator itr = formInstanceList.iterator();
		boolean cwaWIcreated = false;// ALII2024
		while (itr.hasNext()) {
			FormInstance formInstance = (FormInstance) itr.next();
			if ((formInstance.hasProviderFormNumber() || NbaConstants.SYSTEMATIC_FORM.equalsIgnoreCase(formInstance.getFormName()))
					&& formInstance.getAttachmentCount() > 0) {// APSL1720, APSL2735
				String sourceType = getSourceTypeToBeCreated(formInstance.getProviderFormNumber());
				if (isApplicationSource(sourceType)) {
					getWork().getNbaLob().setFormNumber(formInstance.getProviderFormNumber());
					createApplicationSource(getWork(), formInstance.getAttachmentAt(0), sourceType, formInstance.getProviderFormNumber(), distChannel);// QC8410
				} else if (NbaConstants.SYSTEMATIC_FORM.equalsIgnoreCase(formInstance.getFormName())
						&& NbaUtils.isInitialPremiumPaymentForm(getNbaTxLife())) { // APSL2735
					// Begin ALII2024
					if (!cwaWIcreated) {
						addNewCwaCheckSource(getWork(), formInstance.getAttachmentAt(0), distChannel);
					} else {
						formInstance.setActionDelete();
					}
					// End ALII2024
					cwaWIcreated = true; // ALII2024
				} else if (!NbaConstants.SYSTEMATIC_FORM.equalsIgnoreCase(formInstance.getFormName())
						|| (NbaConstants.SYSTEMATIC_FORM.equalsIgnoreCase(formInstance.getFormName()) && !NbaUtils
								.isInitialPremiumPaymentForm(getNbaTxLife()))) { // APSL2735,APSL3888
					addNewSource(getWork(), formInstance.getAttachmentAt(0), sourceType, formInstance.getProviderFormNumber(), distChannel);
				}
				formInstance.removeAttachmentAt(0);
			}
		}
		getWork().addXML103Source(getNbaTxLife());
	}

	protected String getSourceTypeToBeCreated(String formNumber) {
		try {
			NbaTableAccessor nta = new NbaTableAccessor();
			Map caseData = new HashMap();
			//Start APSL4507
			if (getNbaTxLife().getProductCode() != null && getNbaTxLife().getProductCode().trim().equalsIgnoreCase(PRODCODE_ADC)) {
				caseData.put(NbaTableAccessConstants.C_SYSTEM_ID, SYST_CAPS); 
				caseData.put(NbaTableAccessConstants.C_COMPANY_CODE, COMPANY_EQC); 
				caseData.put(NbaTableAccessConstants.C_COVERAGE_KEY, PRODCODE_ADC); 
			} else {
			caseData.put(NbaTableAccessConstants.C_SYSTEM_ID, NbaTableAccessConstants.WILDCARD); // NBA182
			caseData.put(NbaTableAccessConstants.C_COMPANY_CODE, NbaTableAccessConstants.WILDCARD); // NBA182
			caseData.put(NbaTableAccessConstants.C_COVERAGE_KEY, NbaTableAccessConstants.WILDCARD); // NBA182
			}
			//End APSL4507
			NbaUctData data = (NbaUctData) nta.getDataForOlifeValue(caseData, NbaTableConstants.NBA_APPLICATION_FORM, formNumber);
			if (data != null && data.getIndexValue().equalsIgnoreCase(formNumber)) {
				return NbaConstants.A_ST_APPLICATION;
			}
		} catch (NbaBaseException ex) {
			getLogger().logDebug("Exception in finding the " + formNumber + " in the New Application Forms List" + ex.getMessage());
			addComment("Error in finding the " + formNumber + " in the New Application Forms List");
		}
		return NbaConstants.A_ST_MISC_MAIL;
	}

	protected boolean isApplicationSource(String sourceType) {
		if (NbaConstants.A_ST_APPLICATION.equals(sourceType)) {
			return true;
		}
		return false;
	}

	/**
	 * SR515492 New Method
	 * 
	 * This method extracts AttachmentData and adds it as a Source to the NbaDst work item. The type of source added, Image or Data, is determined by
	 * the AttachmentBasicType within the Attachment object.
	 * 
	 * 
	 * @param work
	 *            an NbaDst work item to which the new source will be added
	 * @param anAttachment
	 *            the Attachment that contains the data which will populate the source
	 */
	protected void addNewSource(NbaDst work, Attachment anAttachment, String sourceType, String formNumer, String distChannel)
			throws NbaBaseException {
		byte[] finalImage = getImageFromSOAPMessage(anAttachment.getDescription()); //APSL5055-NBA331.1
		if (finalImage != null) { //APSL5055-NBA331.1
			work.getNbaCase().getNbaSources();
			WorkItemSource newWorkItemSource = new WorkItemSource();
			newWorkItemSource.setCreate("Y");
			newWorkItemSource.setRelate("Y");
			newWorkItemSource.setLobData(new ArrayList());
			newWorkItemSource.setBusinessArea(A_BA_NBA);
			newWorkItemSource.setSourceType(sourceType);
			LobData newLob = new LobData();
			newLob.setDataName(NbaLob.A_LOB_FORM_NBR);
			newLob.setDataValue(formNumer);
			newWorkItemSource.getLobData().add(newLob);
			LobData newLob1 = new LobData();
			newLob1.setDataName(NbaLob.A_LOB_DISTRIBUTION_CHANNEL);
			newLob1.setDataValue(distChannel);
			newWorkItemSource.getLobData().add(newLob1);

			newWorkItemSource.setFormat(NbaConstants.A_SOURCE_IMAGE);
			newWorkItemSource.setSourceStream(finalImage); //APSL5055-NBA331.1
			work.getCase().getSourceChildren().add(newWorkItemSource);
		} else {
			addComment("No Image found for Form Number - " + formNumer);
		}
	}

	/**
	 * QC8410 New Method
	 * 
	 * This method extracts AttachmentData and adds it as a Source to the NbaDst work item. The type of source added, Image or Data, is determined by
	 * the AttachmentBasicType within the Attachment object.
	 * 
	 * 
	 * @param work
	 *            an NbaDst work item to which the new source will be added
	 * @param anAttachment
	 *            the Attachment that contains the data which will populate the source
	 */
	protected void createApplicationSource(NbaDst work, Attachment anAttachment, String sourceType, String formNumer, String distChannel)
			throws NbaBaseException {
		byte[] encodeFinalImage = null;// QC8410
		AccelResult result = null;// QC8410
		encodeFinalImage = getImageFromSOAPMessage(anAttachment.getDescription());
		if (encodeFinalImage != null) {
			work.getNbaCase().getNbaSources();
			WorkItemSource newWorkItemSource = new WorkItemSource();
			newWorkItemSource.setCreate("Y");
			newWorkItemSource.setRelate("Y");
			newWorkItemSource.setLobData(new ArrayList());
			newWorkItemSource.setBusinessArea(A_BA_NBA);
			newWorkItemSource.setSourceType(sourceType);
			LobData newLob = new LobData();
			newLob.setDataName(NbaLob.A_LOB_FORM_NBR);
			newLob.setDataValue(formNumer);
			newWorkItemSource.getLobData().add(newLob);
			LobData newLob1 = new LobData();
			newLob1.setDataName(NbaLob.A_LOB_DISTRIBUTION_CHANNEL);
			newLob1.setDataValue(distChannel);
			newWorkItemSource.getLobData().add(newLob1);
			newWorkItemSource.setFormat(NbaConstants.A_SOURCE_IMAGE);
			newWorkItemSource.setFileName(anAttachment.getDescription());
			newWorkItemSource.setSourceStream(encodeFinalImage);
			// Begin QC8410
			List parentWorkItems = new ArrayList();
			parentWorkItems.add(getWork().getWorkItem());
			newWorkItemSource.setParentWorkItems(parentWorkItems);
			newWorkItemSource.setSystemName(WorkflowServiceHelper.getSystemName()); // APSL5055-NBA331
			result = (AccelResult) currentBP.callBusinessService("CreateIpipeLineSourceBP", newWorkItemSource);
			NewBusinessAccelBP.processResult(result);
			// End QC8410
		} else {
			addComment("No Image found for Form Number - " + formNumer);
		}
	}

	/**
	 * SR515492 New Method Perform document input processing: - Retreieve Soap Message from dataBase and process it for constructing the work item and
	 * sources. After Processing update the Processed Indicator.
	 * 
	 * @param user
	 *            the NbaUser for whom the process is being executed
	 * @param work
	 *            - null
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */

	public NbaAutomatedProcessResult executeProcess(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
		long startTime = System.currentTimeMillis();
		initialize(nbaUserVO, nbaDst);
		findXMLToProcess();
		if (getResult() == null) { // Null indicates that a work item should be created
			Set keySet = xmlSources.keySet();
			Iterator it = keySet.iterator();
			while (it.hasNext()) {
				nbaDst = new NbaDst();
				setUser(nbaUserVO);
				setWork(nbaDst);
				String id = (String) it.next();
				List soapData = (List) xmlSources.get(id);
				MessageFactory messageFactory;
				ByteArrayInputStream inputStream = null;
				try {
					messageFactory = MessageFactory.newInstance();
					inputStream = new ByteArrayInputStream((byte[]) soapData.get(0));
					MimeHeaders mimeHeaders = setMimeHeaders((String) soapData.get(1));
					SOAPMessage msg = messageFactory.createMessage(mimeHeaders, inputStream);
					inputStream.close();
					setSoapMessage(msg);
				} catch (Exception ex) {
					ex.printStackTrace();
					getLogger().logError("Exception in creating the SOAP Message from the retrieved request for Request ID: " + id);
					getLogger().logError(ex.getMessage());
					getNbaSystemDataDBAccessor().updateIPipeLineData(id, null, 2, 0); // APSL2378

					continue;
				}
				String txLifeStr = null;
				try {
					SOAPBody soapBody = getSoapMessage().getSOAPBody(); // APSL2498
					if (soapBody != null) { // APSL2498
						txLifeStr = soapBody.toString(); // APSL2498
						txLifeStr = txLifeStr.substring(txLifeStr.indexOf("<TXLife"), txLifeStr.indexOf("</TXLife>") + 9);
						txLifeStr = setTxLifeNamespace(txLifeStr);
						getLogger().logError("TxLife is :" + txLifeStr);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					getLogger().logError(
							"Exception in extracting the TxLife String from SOAP Message for Request ID: " + id + " TxLife is :" + txLifeStr);
					getLogger().logError(ex.getMessage());
					getNbaSystemDataDBAccessor().updateIPipeLineData(id, null, 2, 0); // APSL2378

					continue;
				}
				try {
					setNbaTxLife(new NbaTXLife(txLifeStr));
				} catch (Exception ex) {
					ex.printStackTrace();
					getLogger().logError("Exception in parsing TxLife from SOAP Message for Request ID: " + id + " TxLife is :" + txLifeStr);
					getLogger().logError(ex.getMessage());
					getNbaSystemDataDBAccessor().updateIPipeLineData(id, null, 2, 0); // APSL2378

					continue;
				}
				initializeDocumentDescription(); // APSL2808
				translateNbaValues(getNbaTxLife());
				try {
					constructWorkItemsAndSources(nbaDst, nbaUserVO);
				} catch (Exception ex) {
					ex.printStackTrace();
					getLogger().logError("Exception in creating WorkItem and Sources for Request ID: " + id);
					getLogger().logError(ex.getMessage());
					getNbaSystemDataDBAccessor().updateIPipeLineData(id, null, 2, 0); // APSL2378

					continue;
				}
				if (getResult() == null) {
					try {
						long startCommit = System.currentTimeMillis();
						doUpdateWorkItem();
						String crdaLob = nbaDst.getNbaLob().getKey().toString();
						getNbaSystemDataDBAccessor().updateIPipeLineData(id, crdaLob, 1, 0); // APSL2378

						logCommitTime(startCommit);
					} catch (NbaBaseException ex) {
						ex.printStackTrace();
						getLogger().logError("Exception in updating the workitem for Request ID: " + id);
						getLogger().logError(ex.getMessage());
						continue;
					}
					if (getResult() == null) {
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
					}
				}
			}
		}
		logElapsed(startTime);
		return getResult();
	}

	/**
	 * Retreive workitems and sources from AWD and set retreived case to the work object.
	 * 
	 * @throws NbaBaseException
	 *             if RemoteException is thrown by netserver.
	 */
	// QC8410 New Method
	protected void retreiveWorkFromAWD() throws NbaBaseException {
		if (work.isCase()) {
			NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
			retrieveOptionsValueObject.setWorkItem(getWork().getID(), true);
			retrieveOptionsValueObject.requestSources();
			retrieveOptionsValueObject.requestTransactionAsChild();
			retrieveOptionsValueObject.setLockWorkItem();
			setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));
		}
	}

	public void translateNbaValues(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			translateFormNumbers(nbATXLife);
		}
	}

	public void translateFormNumbers(NbaTXLife nbATXLife) throws NbaBaseException {
		if (nbATXLife != null) {
			String nbAFormNumber = null;
			List formInstanceList = nbATXLife.getOLifE().getFormInstance();
			Iterator itr = formInstanceList.iterator();
			while (itr.hasNext()) {
				FormInstance formInstance = (FormInstance) itr.next();
				if (formInstance.hasProviderFormNumber()) {
					nbAFormNumber = submitPolicyHelper.getTranslatedNbaValue(NbaConstants.FORM_NUMBER, formInstance.getProviderFormNumber());
					if (nbAFormNumber != null) { // APSL2735
						formInstance.setProviderFormNumber(nbAFormNumber);
						formInstance.setActionUpdate();
					}
				}
			}
		}
	}

	/**
	 * Return my <code>NbaLogger</code> implementation.
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	// New Method SR515492
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProcDocumentInputDB.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log(NO_LOGGER);
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * Construct a Map containing the document documentPaths and the configuration information for the <DocumentSource>s applicable to the path.
	 * 
	 * @throws NbaBaseException
	 * @throws NbaBaseException
	 */
	// New Method SR515492
	public boolean initialize(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {

		// Get the <AutomatedProcess> entry
		AutomatedProcess automatedProcess = null;
		String userID;
		if (nbaUserVO != null) {
			userID = nbaUserVO.getUserID();
			automatedProcess = NbaConfiguration.getInstance().getAutomatedProcessConfigEntry(userID);
			if (automatedProcess == null) {
				// Necessary configuration could not be found so raise exception
				throw new NbaConfigurationException(DOCUMENTINPUTDEFINITION_ACCESS_ERROR + userID);
			}
			// Code deleted APSL2808
		}
		return true;
	}

	/**
	 * SR515492 New Method Log the elapsed time for committing a work item.
	 * 
	 * @param startTimeWorkFlow
	 */
	protected void logCommitTime(long startTimeWorkFlow) {
		if (getLogger().isInfoEnabled()) {
			StringBuffer buf = new StringBuffer();
			buf.append("Elapsed time to commit new  was ").append(System.currentTimeMillis() - startTimeWorkFlow).append(" milliseconds");
			getLogger().logInfo(buf.toString());
		}
	}

	/**
	 * Log the elapsed time between the current time and the start time for the process id.
	 * 
	 * @param id
	 *            - the process id
	 * @param startTime
	 */
	protected void logElapsed(long startTime) {
		if (getLogger().isInfoEnabled()) {
			StringBuffer buf = new StringBuffer();
			buf.append("Elapsed time for a");
			switch (getResult().getReturnCode()) {
			case NbaAutomatedProcessResult.SUCCESSFUL:
				buf.append(" successful ");
				break;
			case NbaAutomatedProcessResult.FAILED:
				buf.append(" failed ");
				break;
			case NbaAutomatedProcessResult.NOWORK:
				buf.append(" no documents found ");
				break;
			default:
				buf.append("n unknown ");
				break;
			}
			buf.append("NbaProcDocumentInputDB was ").append(System.currentTimeMillis() - startTime).append(" milliseconds");
			getLogger().logInfo(buf.toString());
		}
	}

	/**
	 * SR515492 New Method Logon the user. First logoff the user to clear any session data, then logon.
	 */

	protected void logon() {
		SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
		String system = null;
		try {
			system = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
		} catch (NbaBaseException exp) {
			getLogger().logException("Exception occured while reading AWD external system from configuration : ", exp);
			system = NbaConstants.AWD_EXTERNAL_SYSTEM;
		}
		sysAccess.logout(system);
		sysAccess.login(system);
	}

	/**
	 * SR515492 New Method Get the Soap Message to process from database and create a Map.
	 * 
	 * @throws NbaBaseException
	 */
	protected void findXMLToProcess() throws NbaBaseException {
		try {
			boolean xmlFound = false;
			NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor = new NbaSystemDataDatabaseAccessor();
			setXmlSources(nbaSystemDataDBAccessor.selectIPipeLineData());
			updateLockedInd(getXmlSources(), nbaSystemDataDBAccessor); // APSL2378
			if (getXmlSources().size() > 0) {
				xmlFound = true;
			}
			if (!xmlFound) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			getLogger().logError("Exception in retrieving the requests from the database : " + ex.getMessage());
			throw new NbaBaseException(ex, NbaExceptionType.FATAL);
		}
	}

	/**
	 * @return Returns the xmlSources.
	 */
	public Map getXmlSources() {
		return xmlSources;
	}

	/**
	 * @param xmlSources
	 *            The xmlSources to set.
	 */
	public void setXmlSources(Map xmlSources) {
		this.xmlSources = xmlSources;
	}

	/**
	 * @return Returns the documentDescription.
	 */
	public DocumentDescription getDocumentDescription() {
		return documentDescription;
	}

	/**
	 * @param documentDescription
	 *            The documentDescription to set.
	 */
	public void setDocumentDescription(DocumentDescription documentDescription) {
		this.documentDescription = documentDescription;
	}

	/**
	 * SR515492 New Method This methode used to get Image content from SoapMessage retreived from database.
	 * 
	 * @param contentId
	 * @return
	 */
	// QC8410 Return Type changed
	protected byte[] getImageFromSOAPMessage(String contentId) {
		Iterator soapAttachmentItr = getSoapMessage().getAttachments();
		byte imageData[] = null;// QC8410
		try {
			while (soapAttachmentItr.hasNext()) {
				AttachmentPart soapAttachment = (AttachmentPart) soapAttachmentItr.next();
				if (soapAttachment.getContentId().equals(contentId)) {
					DataHandler dataHandler = soapAttachment.getDataHandler();
					imageData = new byte[dataHandler.getInputStream().available()];
					dataHandler.getInputStream().read(imageData);
				}
			}
			return imageData;// QC8410
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * SR515492 New Method This method is use to set the Mime Header to create SoapMessage.
	 * 
	 * @param mimeHeaderString
	 * @return
	 */

	public MimeHeaders setMimeHeaders(String mimeHeaderString) {
		String mimeHeadersString[] = mimeHeaderString.split("#");
		MimeHeaders mimeHeaders = new MimeHeaders();
		for (int i = 0; i < mimeHeadersString.length; i++) {
			String header[] = mimeHeadersString[i].split("=");
			String headerName = header[0];
			String value = mimeHeadersString[i];
			String headerValue = value.substring(value.indexOf('=') + 1);
			mimeHeaders.addHeader(headerName, headerValue);
		}
		return mimeHeaders;
	}

	/**
	 * @return Returns the soapMessage.
	 */
	public SOAPMessage getSoapMessage() {
		return soapMessage;
	}

	/**
	 * @param soapMessage
	 *            The soapMessage to set.
	 */
	public void setSoapMessage(SOAPMessage soapMessage) {
		this.soapMessage = soapMessage;
	}

	/**
	 * SR515492 New Method This methode add Namespace in TxLife for processing.
	 * 
	 * @param txLifeStr
	 * @return
	 */
	protected String setTxLifeNamespace(String txLifeStr) {
		StringBuffer txLifeStrBuf = new StringBuffer(txLifeStr);
		if (txLifeStr.indexOf("<TXLife") > -1) {
			int start = txLifeStr.indexOf("<TXLife");
			int end = txLifeStr.indexOf(">", start) + 1;
			txLifeStrBuf.replace(start, end, "<TXLife xmlns=\"http://ACORD.org/Standards/Life/2\">");
		}
		return txLifeStrBuf.toString();
	}

	/**
	 * @return Returns the nbaSystemDataDBAccessor.
	 */
	public NbaSystemDataDatabaseAccessor getNbaSystemDataDBAccessor() {
		return nbaSystemDataDBAccessor;
	}

	/**
	 * @param nbaSystemDataDBAccessor
	 *            The nbaSystemDataDBAccessor to set.
	 */
	public void setNbaSystemDataDBAccessor(NbaSystemDataDatabaseAccessor nbaSystemDataDBAccessor) {
		this.nbaSystemDataDBAccessor = nbaSystemDataDBAccessor;
	}

	// APSL2378 Begin
	protected void updateLockedInd(Map m, NbaSystemDataDatabaseAccessor dbAccessor) throws NbaBaseException {
		Set keySet = m.keySet();
		Iterator it = keySet.iterator();
		while (it.hasNext()) {
			String id = (String) it.next();
			dbAccessor.updateLockedInd(1, id);
		}

	}

	// APSL2378 End

	/**
	 * Create new NBCWACHECK source
	 * 
	 * @param work
	 *            an NbaDst work item to which the new source will be added
	 * @param anAttachment
	 *            the Attachment that contains the data which will populate the source
	 */
	// APSL2735 New Method
	protected void addNewCwaCheckSource(NbaDst work, Attachment anAttachment, String distChannel) throws NbaBaseException {
		byte[] finalImage = getImageFromSOAPMessage(anAttachment.getDescription()); //APSL5055-NBA331.1
		if (finalImage != null) { //APSL5055-NBA331.1
			work.getNbaCase().getNbaSources();
			WorkItemSource newWorkItemSource = new WorkItemSource();
			newWorkItemSource.setCreate("Y");
			newWorkItemSource.setRelate("Y");
			newWorkItemSource.setLobData(new ArrayList());
			newWorkItemSource.setBusinessArea(A_BA_NBA);
			newWorkItemSource.setSourceType(NbaConstants.A_ST_CWA_CHECK);
			newWorkItemSource.setCreateStation(NbaConstants.SCAN_STATION_EAPPACH);

			LobData newLob = new LobData();
			newLob.setDataName(NbaLob.A_LOB_DISTRIBUTION_CHANNEL);
			newLob.setDataValue(distChannel);
			newWorkItemSource.getLobData().add(newLob);

			LobData newLob2 = new LobData();
			newLob2.setDataName(NbaLob.A_LOB_PORTAL_CREATED_INDICATOR);
			newLob2.setDataValue(String.valueOf(NbaConstants.TRUE));
			newWorkItemSource.getLobData().add(newLob2);

			newWorkItemSource.setFormat(NbaConstants.A_SOURCE_IMAGE);
			newWorkItemSource.setSourceStream(finalImage); //APSL5055-NBA331.1
			work.getCase().getSourceChildren().add(newWorkItemSource);
		} else {
			addComment("No Image found for - " + NbaConstants.A_ST_CWA_CHECK);
		}
	}

	/**
	 * Construct a Map containing the document documentPaths and the configuration information for the <DocumentSource>s applicable to the path.
	 * 
	 * @throws NbaBaseException
	 * @throws NbaBaseException
	 */
	// New Method APSL2808
	public void initializeDocumentDescription() {
		String busFunc = NbaConfiguration.getInstance().getAutomatedProcessConfigEntry(getUser().getUserID()).getBusfunc();
		long appType = NbaOliConstants.OLI_TC_NULL;
		if (getNbaTxLife().getNbaHolding().getApplicationInfo() != null) {
			appType = getNbaTxLife().getNbaHolding().getApplicationInfo().getApplicationType();
		}
		DocumentInputDefinitions documentInputDefinitions = NbaConfiguration.getInstance().getDocumentInputDefinitions();
		DocumentInputDefinition documentInputDefinition;
		int cnt = documentInputDefinitions.getDocumentInputDefinitionCount();
		for (int i = 0; i < cnt; i++) {
			documentInputDefinition = documentInputDefinitions.getDocumentInputDefinitionAt(i);
			// For each documentInputDefinition whose business process matches that of the user, add the path to the map.
			if (documentInputDefinitions.getDocumentInputDefinitionAt(i).getBusfunc().equals(busFunc)) {
				DocumentSource documentSource = documentInputDefinition.getDocumentSourceAt(0);
				for (int j = 0; j < documentSource.getDocumentDescriptionCount(); j++) {
					if (documentSource.getDocumentDescriptionAt(j).getAppType() == NbaOliConstants.OLI_TC_NULL) { // Generic match
						setDocumentDescription(documentSource.getDocumentDescriptionAt(j));
					} else if (documentSource.getDocumentDescriptionAt(j).getAppType() == appType) { // Exact Match
						setDocumentDescription(documentSource.getDocumentDescriptionAt(j));
						break;
					}
				}
			}
		}
	}
}
