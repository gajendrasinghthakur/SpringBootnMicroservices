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
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapter;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapterFactory;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNoValueException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.reinsurance.rgaschema.AttachedFile;
import com.csc.fsg.nba.reinsurance.rgaschema.Case;
import com.csc.fsg.nba.reinsurance.rgaschema.Cases;
import com.csc.fsg.nba.reinsurance.rgaschema.Document;
import com.csc.fsg.nba.reinsurance.rgaschema.Documents;
import com.csc.fsg.nba.reinsurance.rgaschema.NbaRgaRequest;
import com.csc.fsg.nba.reinsurance.rgaschema.ReinsuranceCases;
import com.csc.fsg.nba.reinsuranceadapter.NbaReinsuranceAdapterFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Reinsurer;
import com.csc.fsg.nba.vo.nbaschema.Correspondence;
import com.csc.fsg.nba.vo.nbaschema.Extract;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * NbaProcCorrespondence is the class to process workitems found in NBCORREQ queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th> </thead>
 * <tr>
 * <td>NBA012</td>
 * <td>Version 2</td>
 * <td>Correspondence Print</td>
 * <tr>
 * <td>NBA050</td>
 * <td>Version 3</td>
 * <td>Pending Database</td>
 * </tr>
 * <tr>
 * <td>SPR1359</td>
 * <td>Version 3</td>
 * <td>Automated processes stop poller when unable to lock supplementary work items</td>
 * </tr>
 * <tr>
 * <td>NBA095</td>
 * <td>Version 4</td>
 * <td>Queues Accept Any Work Type</td>
 * </tr>
 * <tr>
 * <td>NBA129</td>
 * <td>Version 5</td>
 * <td>xPression Integration/td>
 * </tr>
 * <tr>
 * <td>NBA146</td>
 * <td>Version 6</td>
 * <td>Workflow integration</td>
 * </tr>
 * <tr>
 * <td>NBA213</td>
 * <td>Version 7</td>
 * <td>Unified User Interface</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.13I</td>
 * <td>AXA Life Phase 1</td>
 * <td>Informal correspondence</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.13</td>
 * <td>AXA Life Phase 1</td>
 * <td>Formal correspondence</td>
 * </tr>
 * <tr>
 * <td>ALPC96</td>
 * <td>AXA Life Phase 1</td>
 * <td>xPression OutBound Email</td>
 * </tr>
 * <tr>
 * <td>ALII1334</td>
 * <td>AXA Life Phase 2</td>
 * <td>Event Driven Correspondence causing AWD Unlock issue</td>
 * </tr>
 * <tr>
 * <td>SR641590(APSL2012)</td>
 * <td>Discretionary</td>
 * <td>SUB-BGA SR</td>
 * </tr>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 2
 */
public class NbaProcCorrespondence extends NbaAutomatedProcess {

	protected Reinsurer configRien; // ACN012

	protected java.lang.String target = null;

	/**
	 * NbaProcCorrespondence constructor.
	 */

	public NbaProcCorrespondence() {
		super();
	}

	/**
	 * This method drives the Automated Correspondence process.It does the following: - Generates a Correspondence Letter based on a letter name. -
	 * Attaches the letter image (pdf) as a source to the correspondence work item.
	 * 
	 * @param user
	 *            the NbaUser for whom the process is being executed
	 * @param work
	 *            a NbaDst object for which processing occurs
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		// NBA095 - block move begin
		if (!initialize(user, work)) {
			return getResult(); // NBA050
		}
		// NBA095 - block move end

		// NBA213 deleted code
		// create and set retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		// ALII1334 code deleted
		retOpt.requestSources();
		setWork(retrieveWorkItem(getUser(), retOpt)); // retrieve the complete Work Item NBA213
		// NBLXA-2114 begin
		// gets the workItem of type NBCORRINFO having letter type reisurance
		if (getWork() != null && getWork().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CORRESPONDENCE)
				&& getWork().getNbaLob().getLetterType().equalsIgnoreCase(
						NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_ACCEPT_REJECT_LETTER))) {
			NbaDst parentDst = retrieveCase(); // retrieves the parent for all NBREINSURE
			List list = parentDst.getNbaTransactions();
			NbaLob workLob = getWork().getNbaLob();
			for (int i = 0; i < list.size(); i++) {
				NbaTransaction transaction = (NbaTransaction) list.get(i);
				if (transaction.getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REINSURANCE)) {
					NbaLob lob = transaction.getNbaLob();
					if (workLob.getReinVendorID().equalsIgnoreCase(lob.getReinVendorID())) {
						NbaSource xmlTransactionSource = null;
						Iterator sources = transaction.getNbaSources().iterator();
						while (sources.hasNext()) {
							NbaSource source = (NbaSource) sources.next();
							if (source.getSource().getSourceType().equals(NbaConstants.A_ST_REINSURANCE_TRANSACTION)) {
								xmlTransactionSource = source;
								break;
							}

						}
						result = (NbaAutomatedProcessResult) sendReinsurerRequest(xmlTransactionSource);
						break;
					}
				}
			}
			return getResult();
		}

		// NBA213 deleted code

		// being APSL3887
		if (NbaUtils.isWholeSale(getNbaTxLife().getPolicy())) {
			boolean errorFlag = false;
			boolean hvtCase = NbaUtils.isHVTCase(getNbaTxLife());
			// executed only for informal cases
			if (getNbaTxLife().getPolicy().getApplicationInfo().getApplicationType() == NbaOliConstants.OLI_APPTYPE_TRIAL) { // AXAL3.7.13 //ALS4843
				Relation firmRelation = null;
				if (hvtCase) {
					firmRelation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PROCESSINGFIRM);
				} else {
					firmRelation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
				}
				if (firmRelation == null) {
					// APSL4149 Indicator Moved to Inner If
					if (hvtCase) {
						errorFlag = true;
						addComment("Case does not have Processing Firm");
					} else {
						addComment("Case does not have BGA");
						// APSL4149 Begins
						suspendWorkItem(Integer.parseInt(
								NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.CORREQ_SUSPEND_DAYS)));
						createLicensingWI();
						return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.RETRY, "", getPassStatus());// APSL4149
						// APSL4149 Ends
					}
				}
			} else if (hasSubfirm() && !hasSubFirmCaseManager()) { // SR641590(APSL2012)SUB-BGA
				addComment("Case does not have Sub-Firm Case Manager");
				errorFlag = true;
			}

			// Begin APSL2079
			if (!(getNbaTxLife().isSIApplication())) {
				Relation bgacmRelation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_BGACASEMANAGER);
				if (bgacmRelation == null) {
					// APSL4149 Indicator Moved to Inner If
					if (hvtCase) {
						errorFlag = true;
						addComment("Case does not have Processing Case Manager");
					} else {
						addComment("Case does not have BGA Case Manager");
						// APSL4149 Starts
						suspendWorkItem(Integer.parseInt(
								NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.CORREQ_SUSPEND_DAYS)));
						createLicensingWI();
						return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.RETRY, "", getPassStatus());// APSL4149
						// APSL4149 Ends
					}
				}
			}
			// End APSL2079

			if (errorFlag) {
				changeStatus(getFailStatus());
				doUpdateWorkItem();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
				return getResult();
			}
		}
		// End APSL3887
		NbaCorrespondenceAdapter adapter = new NbaCorrespondenceAdapterFactory().getAdapterInstance();
		adapter.initializeObjects(getWork(), getUser()); // NBA129 NBA213
		adapter.setParentDst(retrieveParentWork(getWork(), false, false)); // ALS4476
		try {
			// APSL4270 start
			if (adapter.getLetterNameFromSource() == null) { // APSL4270
				addComment("A2CORREQ - Correspondence auto process skipped since either NBCORRXML Source or xPression Letter Name not present.");
				changeStatus(getPassStatus());
				doUpdateWorkItem();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			} else {
				NbaCorrespondenceUtils utils = new NbaCorrespondenceUtils(getUser()); // NBA146
				utils.setImage(adapter.getLetterAsPDF(null, null)); // The PDF
				updateXMLSource(adapter.getExtract()); // Store the extract for reference
				// NBA146 code deleted
				utils.setStatus(getPassStatus()); // The Pass status
				utils.setWorkItem(getWork()); // The Work item
				utils.updateWorkItem(false); // ALII1334 - don't unlock..let the framework do that
				if (getWork().isTransaction() && getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT)) { // ALPC96
					suspendWorkItem(getReqSuspendDays());// ALPC96, APSL4149
				}
				// adapter.markForBatchPrint(); //AXAL 3.7.13I
				if (getResult() == null) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
				}
			}
			// APSL4270 end
			// NBA050 Begin
		} catch (NbaDataAccessException nde) { // If this is a data related error move the work item to an error queue
			addComment(nde.getMessage());
			changeStatus(getFailStatus());
			doUpdateWorkItem();
			adapter.freeResources(); // clear the Correspondence system temp location
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			// NBA050 End
		} catch (NbaNoValueException e) { // If this is a data related error move the work item to an error queue
			addComment(e.getMessage());
			changeStatus(getFailStatus());
			doUpdateWorkItem();
			adapter.freeResources(); // clear the Correspondence system temp location
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
		}
		// NBA213 deleted code
		return getResult();
	}

	/**
	 * This method updates the Correspondence XML Source with extract data
	 * 
	 * @param Correspondence
	 *            XML to be updated
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 *                The exception is thrown when the XML source is invalid.
	 */
	protected void updateXMLSource(String extractXML) throws NbaBaseException {
		NbaSource source = null; // Need a CORRXML source
		for (int i = 0; i < getWork().getNbaSources().size(); i++) {
			source = (NbaSource) getWork().getNbaSources().get(i);
			if (source.getSource().getSourceType().equalsIgnoreCase(NbaConstants.A_ST_CORRESPONDENCE_XML)) { // Want a CorrXML source
				break; // found a match, so break
			}
		}
		try {
			Correspondence sourceXML = Correspondence.unmarshal(new ByteArrayInputStream(source.getText().getBytes()));
			Extract extract = new Extract();
			extract.setPCDATA(extractXML);
			sourceXML.setExtract(extract);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			sourceXML.marshal(stream);
			source.setText(stream.toString());
			source.setUpdate(); // Mark the source for update
		} catch (Exception e) {
			getLogger().logError(e);
			throw new NbaBaseException(e.getMessage());
		}
	}

	/**
	 * This method suspend the requirement WI for number of suspend days
	 * 
	 * @throws NbaBaseException
	 */
	// ALPC96 New Method, APSL4149 Method Signature Changed
	private void suspendWorkItem(int suspendDays) throws NbaBaseException {
		if (suspendDays > 0) {
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setTransactionID(getWork().getID());
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
			Date reqSusDate = (calendar.getTime());
			suspendVO.setActivationDate(reqSusDate);
			suspendWork(getUser(), suspendVO);
		}
	}

	/**
	 * This method calculates the suspend days for requirement from VPMS
	 * 
	 * @return number of days to suspend the requirement
	 * @throws NbaBaseException
	 */
	// ALPC96 New Method
	private int getReqSuspendDays() throws NbaBaseException {
		// call vpms to get suspend days
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
		oinkData.setContractSource(nbaTxLife);
		NbaVpmsAdaptor vpmsProxy = null;
		int suspendDays = 0;// ALS4843
		try {
			getLogger().logDebug("Starting Retrieval of data from VPMS model");
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS);
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_SUSPEND_DAYS);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			List suspendDayList = data.getResultsData();
			// begin ALS4843
			if (suspendDayList != null && suspendDayList.size() > 0) {
				suspendDays = Integer.parseInt(suspendDayList.get(0).toString());
			}
			if (isResetFollowUpDaysNeeded(getWork())) {
				return suspendDays;
			}
			return getFollowUpFrequency(getWork().getNbaLob().getReqUniqueID());
			// end ALS4843
		} catch (java.rmi.RemoteException re) {
			String desc = new StringBuffer().append("Model: ").append(NbaConfiguration.REQUIREMENTS).append(", entrypoint:  ")
					.append(NbaVpmsAdaptor.EP_GET_SUSPEND_DAYS).toString();
			throw new NbaVpmsException(desc, re, NbaExceptionType.FATAL);
		} catch (NbaBaseException e) {
			throw new NbaVpmsException("Problem in getting suspend days from VPMS", e);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException re) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
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
		NbaVpmsResultsData data = getRequirementDataFromVpms(NbaVpmsConstants.EP_IS_RESET_FOLLOWUP_DAYS_NEEDED, oinkData);
		if (data.getResultsData() != null && data.getResultsData().size() > 0) {
			String strResult = (String) data.getResultsData().get(0);
			if (strResult != null && !strResult.trim().equals("")) {
				return Boolean.valueOf(strResult).booleanValue();
			}
		}
		return false;
	}

	/**
	 * SR641590 SUB-BGA Identifying that the Sub-Firm Case Manager is present in the Primary Writing Agent's Hierarchy or not.
	 */
	protected boolean hasSubFirmCaseManager() {
		if (getNbaTxLife() != null) {
			Relation primAgentRel = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
			if (primAgentRel != null) {
				ArrayList subFirmRelations = getNbaTxLife().getOLifE().getRelation();
				for (int i = 0; subFirmRelations != null && i < subFirmRelations.size(); i++) {
					Relation subFirmRelation = (Relation) subFirmRelations.get(i);
					if (subFirmRelation != null && !subFirmRelation.isActionDelete()
							&& subFirmRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_SUBORDAGENT
							&& primAgentRel.getRelatedObjectID().equalsIgnoreCase(subFirmRelation.getOriginatingObjectID())) {
						ArrayList cmRelations = getNbaTxLife().getOLifE().getRelation();
						for (int j = 0; cmRelations != null && j < cmRelations.size(); j++) {
							Relation cmRelation = (Relation) cmRelations.get(j);
							if (cmRelation != null && !cmRelation.isActionDelete()
									&& cmRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_BGACASEMANAGER
									&& cmRelation.getOriginatingObjectID().equals(subFirmRelation.getRelatedObjectID())) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * SR641590 SUB-BGA Identifying that the case is related to Sub-Firm or not with subFirmIndicator in ApplicationInfoExtension.
	 */
	protected boolean hasSubfirm() {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils
				.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		if (applicationInfoExtension != null) {
			return applicationInfoExtension.getSubFirmIndicator();
		}
		return false;
	}

	// APSL4149 New Method
	protected void createLicensingWI() throws NbaBaseException {
		Map deOink = new HashMap();
		deOink.put("A_CreateAgentLicWI", "true");
		// NBLXA-1337 -- Check For Licensing WI
		NbaTransaction nbaTrans = null; // NBLXA-1337
		String appendRoutReason = NbaUtils.getAppendReason(getNbaTxLife());
		retrieveLicWorkItem(getNbaTxLife());
		if (licensingworkExists == false) {
			NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOink);
			setWork(retrieveParentWork(work, true, false));
			nbaTrans = getWork().addTransaction(workProvider.getWorkType(), workProvider.getInitialStatus());
			if (nbaTrans != null) {
				nbaTrans.getNbaLob().setRouteReason(nbaTrans.getNbaLob().getRouteReason() + " " + appendRoutReason);
			}

			update(getWork());
			unlockWork();
		} else if (licensingworkExists == true) {
			if (licensingworkInEndQueue == true && searchResultForLicWIVO != null) {
				retrieveExisitngLicensingWIFromEndQueue(getWork(), getNbaTxLife(), deOink);
			}
		}

	}

	/**
	 * This method allows each Correspondence subclass the ability to perform any processing that may be required specifically for their reinsurer.
	 * 
	 * @param aSource
	 *            an <code>NbaSource</code> object that contains a reinsurer-ready transaction
	 * @return an Object determined by the subclass
	 */
	// NBLXA-2114 New Method
	public Object sendReinsurerRequest(NbaSource aSource) throws NbaBaseException {
		NbaRgaRequest nbaRgaRequest = new NbaRgaRequest(aSource.getText());
		ReinsuranceCases reinsuranceCases = nbaRgaRequest.getReinsuranceCases();
		AttachedFile attachedCorrespondenceLetter = new AttachedFile();
		String letterName = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_ACCEPT_REJECT_LETTER);
		List sources = getWork().getNbaSources();
		Iterator itr = sources.iterator();
		while (itr.hasNext()) {
			NbaSource nbASource = (NbaSource) itr.next();
			List images = WorkflowServiceHelper.getBase64SourceImage(getUser(), nbASource);
			Iterator image = images.iterator();
			while (image.hasNext()) {
				Object img = image.next();
				attachedCorrespondenceLetter.setFile((String) img);
			}
		}

		if (getWork().getNbaLob().getUnderwriterActionLob() != null && NbaConstants.REINSURER_GENRE.equalsIgnoreCase(getWork().getNbaLob().getReinVendorID())) {
			if (getWork().getNbaLob().getUnderwriterActionLob().equalsIgnoreCase(NbaOliConstants.OLI_ACCEPT_ACTION)) {
				attachedCorrespondenceLetter.setFileName("Reinsurance_Accept_Offer");
			} else if (getWork().getNbaLob().getUnderwriterActionLob().equalsIgnoreCase(NbaOliConstants.OLI_REJECT_ACTION)) {
				attachedCorrespondenceLetter.setFileName("Reinsurance_Reject_offer"); // NBLXA-2114 setting file name for accept and reject offer for
																						// GenRe
			}
		} else {
			attachedCorrespondenceLetter.setFileName(letterName);
		}
		reinsuranceCases.addAttachedFile(attachedCorrespondenceLetter);
		Cases cases = reinsuranceCases.getCases();
		Case aCase = cases.getCaseAt(0);
		if (aCase != null) {
			Documents documents = aCase.getDocuments();
			if (documents == null) {// ALII377
				documents = new Documents();
				documents.setCount("0");
				aCase.setDocuments(documents);
			}
			Document document = new Document();
			for (int i = 0; i < documents.getDocumentCount();) { // Removes all attached documents
				documents.removeDocumentAt(i);
			}
			documents.setCount("0");
			if (getWork().getNbaLob().getUnderwriterActionLob() != null && NbaConstants.REINSURER_GENRE.equalsIgnoreCase(getWork().getNbaLob().getReinVendorID())) {
				if (getWork().getNbaLob().getUnderwriterActionLob().equalsIgnoreCase(NbaOliConstants.OLI_ACCEPT_ACTION)) {
					document.setFilename("Reinsurance_Accept_Offer");
				} else if (getWork().getNbaLob().getUnderwriterActionLob().equalsIgnoreCase(NbaOliConstants.OLI_REJECT_ACTION)) {
					document.setFilename("Reinsurance_Reject_offer"); // NBLXA-2114 setting file name for accept and reject offer for
																		// GenRe
				}
			} else {
				document.setFilename(letterName);
			}
			documents.addDocument(document);
			long count = documents.getDocumentCount();
			documents.setCount(BigInteger.valueOf(documents.getDocumentCount())); // Converts int count to BigInteger and set to document
			document.setID(String.valueOf(count));

		}
		NbaReinsuranceAdapterFacade adapter = new NbaReinsuranceAdapterFacade(getWork(), getUser());
		adapter.setConfigRien(getConfigRien());
		target = getConfigRien().getUrl();
		NbaTXLife txLife = (NbaTXLife) adapter.sendMessageToProvider(target, nbaRgaRequest.toXmlString());
		if (txLife != null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUCCESSFUL", getPassStatus()));
			changeStatus(getPassStatus());
			doUpdateWorkItem();
		} else {
			addComment("Unable to evaluate Reinsurer response"); // NBA103
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "FAILURE", getFailStatus()));
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
		}
		return getResult();
	}

	/**
	 * Copies the document files from the workflow system listed in the reinsurance request.
	 * 
	 * @param cases
	 * @return list
	 */
	// NBLXA-2114 New Method
	protected List getDocumentsToCopy(Cases cases) throws NbaBaseException {
		List aList = new ArrayList();
		Documents documents = getDocuments(cases);
		Document doc = null;
		List images; // NBA212
		List sources = getAllSources();
		if (documents != null) {
			int count = documents.getDocumentCount();
			int imageCount; // NBA212
			for (int i = 0; i < count; i++) {
				doc = documents.getDocumentAt(i);
				NbaSource nbaSource = getSource(sources, doc.getFilename());
				if (nbaSource != null && !nbaSource.isTextFormat()) {// AXAL3.7.32
					images = retrieveWorkflowImage(nbaSource); // NBA212 AXAL3.7.32
					imageCount = images.size(); // NBA212
					for (int j = 0; j < imageCount; j++) { // NBA212
						Map aMap = new HashMap();// AXAL3.7.32
						aMap.put(FILENAME, doc.getFilename());
						aMap.put(DATA, images.get(j)); // NBA212
						aList.add(aMap);
					} // NBA212
				}
			}
		}
		return aList;
	}

	/**
	 * Returns a list of documents from the reinsurance request.
	 * 
	 * @param cases
	 * @return
	 */
	// NBLXA-2114 New Method
	protected Documents getDocuments(Cases cases) {
		Documents documents = null;
		int count = cases.getCaseCount();
		for (int i = 0; i < count; i++) {
			Case rgaCase = cases.getCaseAt(i);
			if (documents == null) {
				documents = rgaCase.getDocuments();
			} else {
				documents.getDocument().addAll(rgaCase.getDocuments().getDocument());
			}
		}
		return documents;
	}

	/**
	 * Returns a source from the list of source, whose source id is equal to the id passd in the parameter.
	 * 
	 * @param sources
	 * @param source
	 *            id
	 * @return
	 */
	// NBLXA-2114 New Method
	protected NbaSource getSource(List sources, String sourceID) {
		if (sources != null && sourceID != null) {
			for (int i = 0; i < sources.size(); i++) {
				NbaSource source = (NbaSource) sources.get(i);
				if (sourceID.equalsIgnoreCase(source.getID())) {
					return source;
				}
			}
		}
		return null;
	}

	/**
	 * Retrives all the sources
	 * 
	 * @param sources
	 */
	// NBLXA-2114 New Method
	protected List getAllSources() throws NbaBaseException {
		List sources = getParent().getNbaSources();
		Iterator it = getParent().getNbaTransactions().iterator();
		NbaTransaction tran;
		while (it.hasNext()) {
			tran = (NbaTransaction) it.next();
			sources.addAll(tran.getNbaSources());
		}
		return sources;
	}

	// NBLXA-2114 New Method
	protected List retrieveWorkflowImage(NbaSource nbaSource) throws NbaBaseException {
		// NBA208-32
		try {
			return WorkflowServiceHelper.getBase64SourceImage(getUser(), nbaSource); // NBA213 AXAL3.7.32
		} catch (NbaBaseException e) {
			throw e;
		} catch (Exception e) {
			throw new NbaBaseException("Error retrieving workflow images", e);
		}
	}

	/**
	 * Answers the reinsurer configuration from the NbaConfiguration file.
	 * 
	 * @return an NbaConfigProvider
	 */
	// NBLXA-2114 New Method
	public Reinsurer getConfigRien() throws NbaBaseException {
		if (configRien == null) {
			configRien = NbaConfiguration.getInstance().getReinsurer(getWork().getNbaLob().getReinVendorID());
		}
		return configRien;
	}

}
