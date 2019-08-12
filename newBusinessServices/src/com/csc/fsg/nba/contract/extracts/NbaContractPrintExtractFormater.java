package com.csc.fsg.nba.contract.extracts;
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
import java.io.DataInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculationsConstants;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculatorFactory;
import com.csc.fsg.nba.contract.calculations.results.CalcProduct;
import com.csc.fsg.nba.contract.calculations.results.CalculationResult;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaPerformanceLogger;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaGIAppSystemDataVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSpecialInstructionComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.SpecialInstruction;
import com.csc.fsg.nba.vo.txlife.AltPremMode;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.AttachmentExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.ExtractIllusSummaryInfo;
import com.csc.fsg.nba.vo.txlife.ExtractScheduleInfo;
import com.csc.fsg.nba.vo.txlife.ExtractSummaryProjections;
import com.csc.fsg.nba.vo.txlife.ExtractValuesInfo;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.GIAPPBatchPolicy;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UnderwritingAnalysis;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ContractPrintAttachment;
import com.csc.fsg.nba.vpms.results.ContractPrintAttachments;
/**
 * NbaContractPrintExtractFormater constructs the NbaSource object which contains the 
 * data to be used for Contract Print Extracts. The NbaSource has a Source Type of NBPRTEXT.
 * The NbaSource is a Text file which contains a TXLife formatted as XML.  The TXLife contains 
 * a TXLifeRequest object for each Contract Print Extract type identified in the "EXTC" LOB 
 * field of the Transaction Work Item.
 * 
 * Each TXLifeRequest contains:
 * - the static information for the contract (obtained from the contract database)
 * - additional data specific to the extract type
 * - attachments containing contract documents. The attachments to be included
 * are determined by a VPMS model. The contents of the attachments are formatted as 
 * Base64 encoded strings to prevent Web Service marshalling errors.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>SPR2692</td><td>Version 5</td><td>Print Extract work item set to PRINTERRD status</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.14</td><td>AXA Life Phase 1</td><td>Contract Print Interface</td></tr>
 * <tr><td>ALS5296</td><td>AXA Life Phase 1</td><td>QC #4483-AXAL03.07.14 Policy Print:Paramed or Medical does not print with the Contract Print package if the "form number" is not indexed</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL006</td><td>AXA Life Phase 2</td><td>Product Implementation</td></tr>
 * <tr><td>P2AXAL029</td><td>AXA Life Phase 2</td><td>Contract Print</td></tr>
 * <tr><td>SR545390 Retrofit</td><td>Discretionary</td><td>Back Due Premium at Issue</td></tr>
 * <tr><td>APSL5085 </td><td>Discretionary</td><td>Requirement As Data</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaContractPrintExtractFormater implements NbaConstants, NbaOliConstants, NbaTableConstants {
	protected ArrayList altPremModes;
	protected com.csc.fsg.nba.vpms.results.ContractPrintAttachments attachmentTypesForReport;
	protected NbaDst caseDst;
	protected String commonTXLifeRequest;
	protected NbaCalculation docsCalculation;
	protected NbaTableAccessor nbaTableAccessor;
	protected NbaTXLife nbaTxLife = null;
	protected NbaUserVO nbaUserVO;
	protected NbaTXLife newNbaTXLife;
	//NBA213 deleted code
	protected Holding primaryHolding;
	protected NbaVpmsAdaptor printAttachmentsVpmsAdaptor;
	protected Map tblKeys;
	protected NbaDst transactionDst;
	private static NbaLogger logger = null;	
	private ContractPrintAttachment contractPrintAttachment = null; //NBLXA-2445

	/**
	 * NbaProcContractPrint constructor.
	 */
	public NbaContractPrintExtractFormater() {
		super();
	}

	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractPrintExtractFormater could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * Use a VPMS model to determine the attachments to be included in the tXLifeRequest for the report.
	 * For each attachment, locate the corresponding Source item on Case. Convert the Source to text 
	 * and add it as an attchment to the tXLifeRequest.   
	 * param aReport
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	protected void addAttachmentsForReport(String aReport, TXLifeRequest tXLifeRequest) throws NbaBaseException, NbaVpmsException {
		//getPrintAttachmentsVpmsAdaptor().getSkipAttributesMap().put("A_Extract_Type", aReport); //APSL4639
		try {
			VpmsComputeResult vpmsComputeResult = getPrintAttachmentsVpmsAdaptor().getResults();
			if (vpmsComputeResult.getReturnCode() == 0) {
				NbaVpmsModelResult nbaResult = new NbaVpmsModelResult(vpmsComputeResult.getResult());
				setAttachmentTypesForReport(nbaResult.getVpmsModelResult().getContractPrintAttachments());
				addAttachmentsFromSourceToRequest(tXLifeRequest); 
			} else {
				throw createNbaVpmsException(vpmsComputeResult);
			}
		} catch (RemoteException e) {
			throw new NbaBaseException(e);
		}
	}

	/**
	 * Add the source for the attachments identified by the VPMS model to the TXLifeRequest for the report.
	 * The VPMS model identifies the Sources to be retrieved from the Case Work item, an optional 
	 * requirement code,Form Number and the value to be used as the Attachment.AttachmentType
	 * @param tXLifeRequest the TXLifeRequest for the report
	 */
	//APSL4639 Method refactored
	protected void addAttachmentsFromSourceToRequest(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		List sources = getAllSources();		
		List processed = new ArrayList();	
		NbaSource nbaSource = null;		 
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		boolean imsgSaveInd  = policyExtension.getPrintSaveInd();		
		int count = getAttachmentTypesForReport().getContractPrintAttachmentCount();
//		ContractPrintAttachment contractPrintAttachment = null;
		for (int i = 0; i < sources.size(); i++) {
			nbaSource = (NbaSource) sources.get(i);

			// APSL5001 Start :: New method fetching correct source when sourceType is NBPROVRSLT and had ParendID
			nbaSource = getSourceWithParentID (sources,nbaSource,i); 
			// APSL5001 ::END 
			boolean match = false;
			String currFormNo = nbaSource.getNbaLob().getFormNumber(); 
			String reqType = String.valueOf(nbaSource.getNbaLob().getReqType());
			// Begin NBLXA-2445
			if (!isInvalidateSource(nbaSource.getSourceType())) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("nbaSource.getNbaLob().getPrintInd() == " + nbaSource.getNbaLob().getPrintInd());
				}
				if ((NbaUtils.isBlankOrNull(nbaSource.getNbaLob().getPrintInd())) || (!NbaUtils.isBlankOrNull(nbaSource.getNbaLob().getPrintInd()) && nbaSource.getNbaLob().getPrintInd() == TRUE)) {
					match = isMatchWithTheSource(nbaSource, currFormNo, reqType);
				} /*else if (NbaUtils.isBlankOrNull(nbaSource.getNbaLob().getPrintInd())) {
					match = isMatchWithTheSource(nbaSource, currFormNo, reqType);
					if (match) {
						nbaSource.getNbaLob().setPrintInd(1);
					}
				}*/
			} // End NBLXA-2445
			if (match) {
				if (!processed.contains(nbaSource.getID())) { 
					processed.add(nbaSource.getID());	
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Source attachment type code == " + contractPrintAttachment.getAttachmentTypeCode());
					}
					updatedTxLifeWithSourceData(tXLifeRequest, nbaSource, contractPrintAttachment, imsgSaveInd); //ALS5198
				}
			}
		}
	}

	// APSL5001 new method
	/*
	 * When Source (Provider Result) is attached with App and Requirement, In that caseSource attached with parent does not 
	 * have ParendID and source should be with Parent IDIterating list again and use Source of req. instead of parents
	 */
	protected NbaSource getSourceWithParentID(List sources, NbaSource nbaSource, int i) {
		WorkItemSource wrkItemsource = nbaSource.getWorkItemSource();
		if (wrkItemsource.getParentWorkItemID() != null || !NbaConstants.A_ST_PROVIDER_RESULT.equalsIgnoreCase(nbaSource.getSourceType())) {
			return nbaSource;
		}
		for (int j = i + 1; j < sources.size(); j++) {
			NbaSource nbaSourceFromList = (NbaSource) sources.get(j);
			if (nbaSource.getID().equalsIgnoreCase(nbaSourceFromList.getID())) {
				WorkItemSource wrkItemsourceFromList = nbaSourceFromList.getWorkItemSource();
				if (wrkItemsourceFromList.getParentWorkItemID() != null) {
					nbaSource = nbaSourceFromList;
					break;
				}
			}
		}
		return nbaSource;
	}



	//	APSL4639 New Method
	private boolean isInvalidateSource(String sourceType) {
		if (sourceType.equalsIgnoreCase(NbaConstants.A_ST_INVALID_APPLICATION) || sourceType.equalsIgnoreCase(NbaConstants.A_ST_INVALID_FORM)) {
			return true;
		}
		return false;
	}


	//APSL4639 New Method
	/**
	 * @purpose This method will check the source type coming from the VPMS table
	 * @param nbaSource
	 * @param sourceType
	 * @param reqType
	 * @param formNumberOrReqCode
	 * @param currFormNo
	 * @return
	 */
	private boolean isSpecificSourceType(NbaSource nbaSource, String sourceType, String reqType , String formNumberOrReqCode, String currFormNo){
		if (!NbaUtils.isBlankOrNull(sourceType) && (sourceType != "*") && sourceType.equalsIgnoreCase(nbaSource.getSourceType())) {
			if (nbaSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_APPLICATION) && nbaSource.getNbaLob() != null 
					&& !NbaUtils.isBlankOrNull(nbaSource.getNbaLob().getApplicationType())
					&& NbaUtils.isNewApplication(Long.parseLong(nbaSource.getNbaLob().getApplicationType()))) {
				return true;
			} else if (!nbaSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_APPLICATION)) {
				return true;
			}
		} else if (((reqType != null && reqType.equalsIgnoreCase(formNumberOrReqCode))
				|| (!NbaUtils.isBlankOrNull(currFormNo) && currFormNo.equalsIgnoreCase(formNumberOrReqCode)))
				&& !(nbaSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_APPLICATION))){
			return true;
		}
		return false;
	}

	/**
	 * Add the source for the attachments identified by the VPMS model to the TXLifeRequest for the report.
	 * The VPMS model identifies the Source type to be retrieved from the Case Work item, an optional 
	 * requirement code, and the value to be used as the Attachment.AttachmentType
	 * @param tXLifeRequest the TXLifeRequest for the report
	 */
	//ALS5198 - code refactor
	/*protected void addAttachmentsFromSourceToRequest(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		boolean match = false; //ALS5198
		List sources = getAllSources();		//ALS5296
		List processed = new ArrayList();	//ALS5296
		NbaSource nbaSource = null;
		int count = getAttachmentTypesForReport().getContractPrintAttachmentCount();
		ContractPrintAttachment contractPrintAttachment = null;
		for (int i = 0; i < sources.size(); i++) {
			nbaSource = (NbaSource) sources.get(i);
			for (int j = 0; j < count; j++) {
				match = false;
				contractPrintAttachment = getAttachmentTypesForReport().getContractPrintAttachmentAt(j);
				if (nbaSource.getSourceType().equalsIgnoreCase(contractPrintAttachment.getSourceType())) { //Match on Source type,AXAL3.7.14
					String formNumberOrReqCode = contractPrintAttachment.getSourceFormTypeOrReqCode(); //ALS5296
					if (formNumberOrReqCode == null || formNumberOrReqCode.equals("*")) { //ALS5296
						//ALS5198 -  pick formal source - if ApplicationType = 1
						if (nbaSource.getNbaLob() != null && !NbaUtils.isBlankOrNull(nbaSource.getNbaLob().getApplicationType())
								&& NbaUtils.isNewApplication(Long.parseLong(nbaSource.getNbaLob().getApplicationType()))){ //ALS5198 ALS5329 //P2AXAL040 added check for apptype 4 and 20
							match = true;
							break;
						}
						//Begin ALS5296
						if (nbaSource.getSourceType().equalsIgnoreCase(A_ST_BENE_DOC)
								|| nbaSource.getSourceType().equalsIgnoreCase(A_ST_CHANGE_FORM)) {//ALS5438
							match = true;
							break;
						}
					} else {
						String currFormNo = nbaSource.getNbaLob().getFormNumber();
						String reqType = String.valueOf(nbaSource.getNbaLob().getReqType());
						if (reqType != null && reqType.equalsIgnoreCase(formNumberOrReqCode)) {
							match = true;
							break; //Done if requirement type matches
						} else if (currFormNo != null && currFormNo.equalsIgnoreCase(formNumberOrReqCode)) {
							match = true;
							break; //Done if form number matches
						}
					} // End ALS5296
				}
			}
			if (match) {
				if (!processed.contains(nbaSource.getID())) { //ALS5296
					processed.add(nbaSource.getID());	//ALS5296
					updatedTxLifeWithSourceData(tXLifeRequest, nbaSource, contractPrintAttachment); //ALS5198
				}
			}

		}
	} */

	//ALS5198 - New Method, APSL4639 signature modified
	protected void updatedTxLifeWithSourceData (TXLifeRequest tXLifeRequest, NbaSource nbaSrc, ContractPrintAttachment attachment, boolean imgSaveInd) throws NbaBaseException{
		long startTimeInMs = System.currentTimeMillis(); //APSL5085
		NbaPerformanceLogger.initMethod("updatedTxLifeWithSourceData");//APSL5085
		long basicType;
		String pageRangeLob;
		if (nbaSrc != null) {
			List images;	//NBA212
			//APSL4639 Begin
			List pageNumberList = null; 

			String reqType = String.valueOf(nbaSrc.getNbaLob().getReqType());
			if (nbaSrc.isTextFormat()) {
				basicType = OLI_LU_BASICATTMNTTY_TEXT;
				images = new ArrayList();	//NBA212
				images.add(NbaBase64.encodeString(nbaSrc.getText())); //Convert the source to a Base64 String //NBA212
			} else {
				basicType = OLI_LU_BASICATTMNTTY_IMAGE;
				try {
					images = WorkflowServiceHelper.getBase64SourceImage(getNbaUserVO(), nbaSrc); //Retrieve the source as a Base64 String //NBA212, NBA213
				} catch (NbaNetServerException e) {
					throw new NbaBaseException("Error retrieving contract source " + e.getMessage(), e);
					//NBA213 deleted code
				}
			}
			for (int j = 0; j < images.size(); j++) { // NBA212
				// Begin QC17368 APSL4639
				long type = -1;
				if (attachment != null) {
					type = Long.parseLong(attachment.getAttachmentTypeCode());
				}
				// End QC17368 APSL4639
				// APSL5085: Execute tiff image break logic only if switcher value is true.
				DataInputStream di = null;
				byte[] byteArrNew = null;
				List seprateImageString = new ArrayList();
				try {
					byteArrNew = NbaBase64.decode((String) images.get(j));
					di = new DataInputStream(new java.io.ByteArrayInputStream(byteArrNew));
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!NbaUtils.isBlankOrNull(reqType) && !NbaUtils.isBlankOrNull(NbaConfiguration.getInstance())
						&& !NbaUtils.isBlankOrNull(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
								NbaConfigurationConstants.TIFF_IMAGE_SWITCH))
								&& NbaConstants.TRUE_STR.equalsIgnoreCase(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
										NbaConfigurationConstants.TIFF_IMAGE_SWITCH))
										&& (Long.parseLong(reqType) == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED)
										&& (NbaUtils.identifyTiffAndRetrieveData(seprateImageString, di))) {

					// Identifying whether image is tiff,if so break it to retrieve pages
					int tiffImageCount = seprateImageString.size();
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Tiff image count:" + tiffImageCount);
					}

					// If page range is not given by the user in 'view image summary'sets page range LOB.
					if ((nbaSrc.getNbaLob().getPageRange() == null)
							|| (nbaSrc.getNbaLob().getPageRange() != null && nbaSrc.getNbaLob().getPageRange().trim().length() <= 0)) {

						if (tiffImageCount < 4) {
							if (getLogger().isDebugEnabled()) {
								getLogger().logDebug("Recieved Paramed Old Form");
							}

							nbaSrc.getNbaLob().setPageRange("1");
						} else if (tiffImageCount >= 6) {
							// If count is >=6,means received new Paramed Form,removing last 2 pages from the count to prevent Paramed Report pages
							// to print.
							nbaSrc.getNbaLob().setPageRange("1-" + (tiffImageCount - 2));
						} else {
							nbaSrc.getNbaLob().setPageRange("1-" + tiffImageCount);// Printing all pages,if new form received without Paramed
							// Report pages.
						}
					}
					pageRangeLob = nbaSrc.getNbaLob().getPageRange();
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Page range LOB:" + pageRangeLob);
					}
					if (!NbaUtils.isBlankOrNull(pageRangeLob)) {
						pageNumberList = getPageNumbers(pageRangeLob);
						getLogger().logDebug("Page number List:" + pageNumberList);
					}
					if (!imgSaveInd) {
						imgSaveInd = true;
					}
					// Preparing Attachment for each page
					for (int k = 0; k < tiffImageCount; k++) {
						Attachment newAttachment = addAttachmentToTXLifeRequest(type, tXLifeRequest, basicType, OLI_APPSUBMITTYPE_ATTACHED, nbaSrc);
						AttachmentData attachmentData = new AttachmentData();
						attachmentData.setTc(Long.toString(NbaOliConstants.OLI_VARIANT_STRING));
						attachmentData.setPCDATA((String) seprateImageString.get(k));
						newAttachment.setAttachmentData(attachmentData);
						AttachmentExtension attchmentExt = NbaUtils.getFirstAttachmentExtension(newAttachment);
						attchmentExt.setMultiPageImageInd(true);
						if (attchmentExt != null) {
							if (NbaUtils.isBlankOrNull(pageRangeLob) || !imgSaveInd) {
								attchmentExt.setPrintInd(OLIX_PRINTIND_PRINTALL);
							} else if (!NbaUtils.isBlankOrNull(pageNumberList) && pageNumberList.contains(String.valueOf(j + 1))) {
								attchmentExt.setPrintInd(OLIX_PRINTIND_PRINT);
							} else {
								attchmentExt.setPrintInd(OLIX_PRINTIND_DONOTPRINT);
							}
						}

						if (k + 1 < tiffImageCount) {
							j++;
						}
					}
					seprateImageString.clear();
				} else {
					// APSL5085 ::End
					// APSL5082 ::Start
					String currFormNo = nbaSrc.getNbaLob().getFormNumber();
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Requirement Type:" + reqType);
						getLogger().logDebug("Form Number:" + currFormNo);
					}
					if ((nbaSrc.getNbaLob().getPageRange() == null || (nbaSrc.getNbaLob().getPageRange() != null && nbaSrc.getNbaLob().getPageRange()
							.trim().length() > 0))
							&& (Long.parseLong(reqType) == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED)) {
						if (NbaUtils.isBlankOrNull(currFormNo)) {
							RequirementInfo requirementInfo = getNbaTxLife().getRequirementInfo(nbaSrc.getNbaLob().getReqUniqueID());
							if (requirementInfo != null) {
								currFormNo = requirementInfo.getFormNo();
							}
						}

						if ((!NbaUtils.isBlankOrNull(currFormNo)) && (currFormNo.contains("180-225D"))) { // If old Paramed Form is present,setting
							// page rang to 1.

							nbaSrc.getNbaLob().setPageRange("1");
							if (!imgSaveInd) {
								imgSaveInd = true;
							}
						}
					}
					// APSL5082::End
					// End APSL4639
					pageRangeLob = nbaSrc.getNbaLob().getPageRange();
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Page Range Lob:" + pageRangeLob);
					}
					if (imgSaveInd && !NbaUtils.isBlankOrNull(pageRangeLob)) {
						pageNumberList = getPageNumbers(pageRangeLob);
					} // APSL4639 End
					Attachment newAttachment = addAttachmentToTXLifeRequest(type, tXLifeRequest, basicType, OLI_APPSUBMITTYPE_ATTACHED, nbaSrc);
					AttachmentData attachmentData = new AttachmentData();
					attachmentData.setTc(Long.toString(NbaOliConstants.OLI_VARIANT_STRING));
					attachmentData.setPCDATA((String) images.get(j)); // NBA212
					newAttachment.setAttachmentData(attachmentData);
					AttachmentExtension attchmentExt = NbaUtils.getFirstAttachmentExtension(newAttachment);
					if (attchmentExt != null) {
						if (NbaUtils.isBlankOrNull(pageRangeLob) || !imgSaveInd) {
							attchmentExt.setPrintInd(OLIX_PRINTIND_PRINTALL);
						} else if (!NbaUtils.isBlankOrNull(pageNumberList) && pageNumberList.contains(String.valueOf(j + 1))) {
							attchmentExt.setPrintInd(OLIX_PRINTIND_PRINT);
						} else {
							attchmentExt.setPrintInd(OLIX_PRINTIND_DONOTPRINT);
						}
					}

					// End APSL4639
				} // NBA212
			}	//NBA212
			
			if(NbaUtils.isBlankOrNull(nbaSrc.getNbaLob().getPrintInd())|| nbaSrc.getNbaLob().getPrintInd() == -1){
				System.out.println("nbaSrc.getNbaLob().getPrintInd() while saving == " + nbaSrc.getNbaLob().getPrintInd());
				nbaSrc.getNbaLob().setPrintInd(1);
			}
		}
	}


	/**
	 * @Purpose This method will returm the list contains page numbers from the PageRangeLob  
	 * @param pageRange this conatins the value of the pageRangeLob 
	 * @return list of pages conatins page tobe print
	 */
	//APSL4639 New Method
	private List getPageNumbers(String pageRange){
		List pageNumberList = new ArrayList();
		String[] pageNumbers;
		pageNumbers = pageRange.replaceAll("\\s","").split(",");
		for (int i = 0; i < pageNumbers.length; i++) {
			if(pageNumbers[i].indexOf('-') != -1){
				String range[] = pageNumbers[i].split("-");
				getNumberRange(pageNumberList,Integer.parseInt(range[0]), Integer.parseInt(range[1]));
			}else{
				pageNumberList.add(pageNumbers[i]);
			}
		}
		return pageNumberList;
	}

	/**
	 * @purpose The purpose of this method is to get the page number from min to max
	 * @param min this will contain the minimum number
	 * @param max this will contain the maximum number
	 */
	//APSL4639 New Method
	private void getNumberRange(List pageNumberList,int min,int max){
		for(int i=min; i <=max; i++){
			pageNumberList.add(String.valueOf(i));
		}
	}


	/**
	 * Add a FormInstance containing an Attachement which identifies the extract type
	 * @param type
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected Attachment addAttachmentToTXLifeRequest(long type, TXLifeRequest tXLifeRequest, long basicType) throws NbaBaseException {
		return addAttachmentToTXLifeRequest(type, tXLifeRequest, basicType, -1,null); //ALS5198
	}
	/**
	 * Add a FormInstance containing an Attachement which identifies the extract type
	 * @param type - the value obtained from the VPMS model to be used as the Attachment.AttachmentType
	 * @param tXLifeRequest - the TXLifeRequest to which the FormInstance is to be added
	 * @param basicType - a value indicating whether the attacment respresents a Text or Image 
	 * @param imageSubmissionType - for Attachments for Sources, this contains a value of "8" (OLI_APPSUBMITTYPE_ATTACHED). 
	 * @throws NbaBaseException
	 */
	//ALS5198 - updated method signature
	protected Attachment addAttachmentToTXLifeRequest(long type, TXLifeRequest tXLifeRequest, long basicType, long imageSubmissionType, NbaSource source) throws NbaBaseException { //ALS5198
		Attachment attachment = new Attachment();
		attachment.setUserCode(getNbaUserVO().getUserID());
		attachment.setAttachmentBasicType(basicType);
		if(type != -1){ //QC17368 APSL4639
			attachment.setAttachmentType(type);
		}
		FormInstance formInstance = new FormInstance();
		if (imageSubmissionType != -1) {
			formInstance.setImageSubmissionType(imageSubmissionType);
		}
		//ALS5198 - Begin
		if ( source !=null){
			formInstance.setFormName(source.getNbaLob().getFormNumber());
			//APSL1954 Retrofit Begin
			com.csc.fsg.nba.vo.txlife.OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_ATTACHMENT);
			attachment.addOLifEExtension(oLifEExtension);
			AttachmentExtension attchmentExt = oLifEExtension.getAttachmentExtension();
			if ((source.getNbaLob().getPortalCreated() || NbaConstants.PROVIDER_PRODUCER.equalsIgnoreCase(source.getNbaLob().getReqVendor()))
					&& (!source.isApplication())) { // QC8410 //NBLXA-2059
				attchmentExt.setMultiPageImageInd(true);
			}else{
				attchmentExt.setMultiPageImageInd(false);
			}
			//APSL1954 Retrofit End
			//Begin QC15359/APSL4570
			if (NbaConstants.A_ST_PROVIDER_RESULT.equalsIgnoreCase(source.getSourceType())) {
				WorkItemSource wrkItemsource = source.getWorkItemSource();
				String parentId = wrkItemsource.getParentWorkItemID();
				if (!NbaUtils.isBlankOrNull(parentId)) {
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					retOpt.setWorkItem(parentId, false);
					NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(getNbaUserVO(), retOpt);
					if (aWorkItem != null && NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(aWorkItem.getWorkType())) {
						RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(aWorkItem.getNbaLob().getReqUniqueID());
						if (reqInfo != null) {
							formInstance.setRelatedObjectID(reqInfo.getId());
						}
					}
				}

			}
			//End QC15359/APSL4570
		}
		//ALS5198 - End
		formInstance.addAttachment(attachment);
		tXLifeRequest.getOLifE().addFormInstance(formInstance);
		return attachment;
	}

	/**
	 * Update Life death benefit and lapse fields:
	 * Life.ProjectedCurrLapseDate, Life.ProjectedGuarLapseDate, Life.DeathBenefitAmt, Life.GDBValue
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void updateProjectedAmts(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		if (getNewNbaTXLife().isLife()) {
			Policy policy = getPolicy(tXLifeRequest);
			Life life = getLife(tXLifeRequest);
			CalculationResult calculationResult = getDocsResultForID(policy.getId());
			int prodCount;
			CalcProduct calcProduct;
			String field;
			ArrayList use = new ArrayList();
			use.add(NbaConstants.YES_VALUE);
			use.add(Integer.toString(NbaConstants.TRUE));
			String policyCurrLapseInd = "N";
			String policyCurrLapseYear = "";
			String policyGuarLapseInd = "N";
			String policyGuarLapseYear = "";
			if (calculationResult != null) {
				prodCount = calculationResult.getCalcProductCount();
				for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
					calcProduct = calculationResult.getCalcProductAt(prdIdx);
					field = calcProduct.getType();
					if (field.equalsIgnoreCase("PolicyCurrLapseInd")) {
						policyCurrLapseInd = calcProduct.getValue();
					} else if (field.equalsIgnoreCase("PolicyCurrLapseYear")) {
						policyCurrLapseYear = calcProduct.getValue();
					} else if (field.equalsIgnoreCase("PolicyGuarLapseInd")) {
						policyGuarLapseInd = calcProduct.getValue();
					} else if (field.equalsIgnoreCase("PolicyGuarLapseYear")) {
						policyGuarLapseYear = calcProduct.getValue();
					}
				}
				if (use.contains(policyCurrLapseInd) && policyCurrLapseYear.length() > 0) {
					life.setProjectedCurrLapseDate(NbaUtils.calcDayFotFutureDate(policy.getEffDate(),Integer.parseInt(policyCurrLapseYear) * 12 ));
				}
				if (use.contains(policyGuarLapseInd) && policyGuarLapseYear.length() > 0) {
					life.setProjectedGuarLapseDate(NbaUtils.calcDayFotFutureDate(policy.getEffDate(),Integer.parseInt(policyGuarLapseYear) * 12 ));
				}
			}
			calculationResult = getDocsResultForID(getObjectDurId(policy.getId(), 1)); //Get the current values
			if (calculationResult != null) {
				prodCount = calculationResult.getCalcProductCount();
				for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
					calcProduct = calculationResult.getCalcProductAt(prdIdx);
					field = calcProduct.getType();
					if (field.equalsIgnoreCase("PolicyCurrDeathBenefit")) {
						life.setDeathBenefitAmt(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("PolicyGuarDeathBenefit")) {
						life.setGDBValue(calcProduct.getValue());
					}
				}
			}
		}
	}
	/**
	 * Construct ExtractIllusSummaryInfo objects for each Coverage for which there are values in the 
	 * VPMS model calculation results.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractIllusSummaryInfo(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		if (getNewNbaTXLife().isLife()) {
			addExtractIllusSummaryInfoForLife(tXLifeRequest);
		} else if (getNewNbaTXLife().isAnnuity()) {
			addExtractIllusSummaryInfoForAnnuity(tXLifeRequest);
		}
	}
	/**
	 * Construct ExtractIllusSummaryInfo objects for each Coverage for which there are values in the 
	 * VPMS model calculation results.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractIllusSummaryInfoForLife(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		Life life = getLife(tXLifeRequest);
		int covCount = life.getCoverageCount();
		Coverage coverage;
		CalculationResult calculationResult;
		CalcProduct calcProduct;
		ExtractIllusSummaryInfo e10 = new ExtractIllusSummaryInfo();
		ExtractIllusSummaryInfo e20 = new ExtractIllusSummaryInfo();
		String field;
		int prodCount;
		int prdIdx;
		for (int i = 0; i < covCount; i++) {
			coverage = life.getCoverageAt(i);
			CoverageExtension coverageExtension = getCoverageExtension(coverage);
			calculationResult = getDocsResultForID(coverage.getId());
			if (calculationResult != null) {
				prodCount = calculationResult.getCalcProductCount();
				for (prdIdx = 0; prdIdx < prodCount; prdIdx++) {
					calcProduct = calculationResult.getCalcProductAt(prdIdx);
					field = calcProduct.getType();
					if (field.equalsIgnoreCase("CovGuarNetPmtCostIndex10")) {
						e10.setDuration(10);
						e10.setGuarNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovGuarNetPmtCostIndex20")) {
						e20.setDuration(20);
						e20.setGuarNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovGuarSurrenderCostIndex10")) {
						e10.setDuration(10);
						e10.setGuarSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovGuarSurrenderCostIndex20")) {
						e20.setDuration(20);
						e20.setGuarSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrNetPmtCostIndex10")) {
						e10.setDuration(10);
						e10.setCurrentNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrNetPmtCostIndex20")) {
						e20.setDuration(20);
						e20.setCurrentNetPayment(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrSurrenderCostIndex10")) {
						e10.setDuration(10);
						e10.setCurrentSurrValue(calcProduct.getValue());
					} else if (field.equalsIgnoreCase("CovCurrSurrenderCostIndex20")) {
						e20.setDuration(20);
						e20.setCurrentSurrValue(calcProduct.getValue());
					}
				}
			}
			if (e10.hasDuration()) {
				coverageExtension.addExtractIllusSummaryInfo(e10);
			}
			if (e20.hasDuration()) {
				coverageExtension.addExtractIllusSummaryInfo(e20);
			}
		}
	}
	/**
	 * Construct ExtractIllusSummaryInfo objects for the Annuity.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractIllusSummaryInfoForAnnuity(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		Policy policy = getPolicy(tXLifeRequest);
		Annuity annuity = getAnnuity(tXLifeRequest);
		AnnuityExtension annuityExtension = getAnnuityExtension(annuity);
		CalcProduct calcProduct;
		ExtractIllusSummaryInfo e10 = new ExtractIllusSummaryInfo();
		ExtractIllusSummaryInfo e20 = new ExtractIllusSummaryInfo();
		String field;
		String currEffYieldMat = "0";
		String guarEffYieldMat = "0";
		int prdIdx;
		CalculationResult calculationResult = getDocsResultForID(policy.getId());
		if (calculationResult != null) {
			int prodCount = calculationResult.getCalcProductCount();
			for (prdIdx = 0; prdIdx < prodCount; prdIdx++) {
				calcProduct = calculationResult.getCalcProductAt(prdIdx);
				field = calcProduct.getType();
				if (field.equalsIgnoreCase("CurrEffYield10")) {
					e10.setDuration(10);
					e10.setCurrentEffYield(calcProduct.getValue());
				} else if (field.equalsIgnoreCase("GuarEffYield10")) {
					e10.setDuration(10);
					e10.setGuarEffYield(calcProduct.getValue());
				} else if (field.equalsIgnoreCase("CurrEffYieldMat")) {
					currEffYieldMat = calcProduct.getValue();
				} else if (field.equalsIgnoreCase("GuarEffYieldMat")) {
					guarEffYieldMat = calcProduct.getValue();
				}
				if (e10.hasDuration()) {
					e10.setCurrentEffYieldMat(currEffYieldMat);
					e10.setGuarEffYieldMat(guarEffYieldMat);
					annuityExtension.addExtractIllusSummaryInfo(e10);
				}
				if (e20.hasDuration()) {
					e20.setCurrentEffYieldMat(currEffYieldMat);
					e20.setGuarEffYieldMat(guarEffYieldMat);
					annuityExtension.addExtractIllusSummaryInfo(e20);
				}
			}
		}
	}
	/**
	 * Construct ExtractValuesInfo objects for each durtation for the primary coverage for which 
	 * there are values in the VPMS model calculation results. Depending on the values, an 
	 * ExtractValuesInfo may be created for each duration from zero to maturity.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractValuesInfo(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		if (getNewNbaTXLife().isLife()) {
			addExtractValuesInfoForLife(tXLifeRequest);
		}
	}
	/**
	 * Construct ExtractValuesInfo objects for each durtation for the primary coverage for which 
	 * there are values in the VPMS model calculation results. Depending on the values, an 
	 * ExtractValuesInfo may be created for each duration from zero to maturity.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractValuesInfoForLife(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		CalculationResult calculationResult;
		int dur;
		Policy policy = getPolicy(tXLifeRequest);
		Coverage coverage = getPrimarycoverage(policy);
		if (coverage != null) {
			CoverageExtension coverageExtension = getCoverageExtension(coverage);
			int maxDur = getMaxDuration(coverage);
			int issueAge = getIssueAge(coverage);
			for (dur = 1; dur < maxDur; dur++) {
				calculationResult = getDocsResultForID(getObjectDurId(policy.getId(), dur));
				if (calculationResult != null) {
					coverageExtension.addExtractValuesInfo(createExtractValuesInfoFromCalc(calculationResult, issueAge, dur));
				}
			}
		}
	}
	/**
	 * Construct ExtractValuesInfo the values returned from the model
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected ExtractValuesInfo createExtractValuesInfoFromCalc(CalculationResult calculationResult, int issueAge, int dur) {
		CalcProduct calcProduct;
		String field;
		int prodCount;
		int prdIdx;
		ExtractValuesInfo extractValuesInfo = new ExtractValuesInfo();
		extractValuesInfo.setPolicyYears(dur);
		extractValuesInfo.setAge(issueAge + dur -1);
		prodCount = calculationResult.getCalcProductCount();
		for (prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
			calcProduct = calculationResult.getCalcProductAt(prdIdx);
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("CorridorPercent")) {
				extractValuesInfo.setCorridorPercent(calcProduct.getValue());
			}
		}
		return extractValuesInfo;
	}

	/**
	 * Construct ExtractScheduleInfo objects for each Coverage for which there are values in the 
	 * VPMS model calculation results. Depending on the values, an ExtractScheduleInfo may be 
	 * created for each duration from zero to maturity.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractScheduleInfo(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		if (getNewNbaTXLife().isLife()) {
			addExtractScheduleInfoForLife(tXLifeRequest);
		} else if (getNewNbaTXLife().isAnnuity()) {
			addExtractScheduleInfoForAnnuity(tXLifeRequest);
		}
	}
	/**
	 * Construct ExtractScheduleInfo objects for each Coverage for which there are values in the 
	 * VPMS model calculation results. Depending on the values, an ExtractScheduleInfo may be 
	 * created for each duration from zero to maturity.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractScheduleInfoForLife(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		CalculationResult calculationResult;
		int dur;
		Policy policy = getPolicy(tXLifeRequest);
		PolicyExtension policyExtension = getPolicyExtension(policy);
		int maxDur = getMaxDuration(policy);
		int issueAge = getIssueAge(tXLifeRequest);
		for (dur = 1; dur < maxDur; dur++) {
			calculationResult = getDocsResultForID(getObjectDurId(policy.getId(), dur));
			if (calculationResult != null) {
				policyExtension.addExtractScheduleInfo(createExtractScheduleInfoFromCalc(calculationResult, issueAge, dur));
			}
		}
		Life life = getLife(tXLifeRequest);
		int covCount = life.getCoverageCount();
		Coverage coverage;
		CoverageExtension coverageExtension;
		for (int cov = 0; cov < covCount; cov++) {
			coverage = life.getCoverageAt(cov);
			coverageExtension = getCoverageExtension(coverage);
			maxDur = getMaxDuration(coverage);
			issueAge = getIssueAge(coverage);
			for (dur = 1; dur < maxDur; dur++) {
				calculationResult = getDocsResultForID(getObjectDurId(coverage.getId(), dur));
				//Find Results than match coverage id + duration
				if (calculationResult != null) {
					coverageExtension.addExtractScheduleInfo(createExtractScheduleInfoFromCalc(calculationResult, issueAge, dur));
				}
			}
			if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
				ExtractScheduleInfo cesi;
				ExtractScheduleInfo pesi;
				int covExtcnt = coverageExtension.getExtractScheduleInfoCount();
				int polExtcnt = policyExtension.getExtractScheduleInfoCount();
				for (int j = 0; j < covExtcnt; j++) {
					cesi = coverageExtension.getExtractScheduleInfoAt(j);
					for (int k = 0; k < polExtcnt; k++) {
						pesi = policyExtension.getExtractScheduleInfoAt(k);
						if (cesi.getDuration() == pesi.getDuration()) {
							cesi.setCOIAmt(pesi.getCOIAmt());
							cesi.setDeathBenefitAmt(pesi.getDeathBenefitAmt());
							break;
						}
					}
				}
			}
		}
		//Create ExtractScheduleInfo for CovOptions
		CovOption covOption;
		CovOptionExtension covOptionExtension;
		for (int cov = 0; cov < covCount; cov++) {
			coverage = life.getCoverageAt(cov);
			issueAge = getIssueAge(coverage);
			int covOptionCount = coverage.getCovOptionCount();
			for (int i = 0; i < covOptionCount; i++) {
				covOption = coverage.getCovOptionAt(i);
				covOptionExtension = getCovOptionExtension(covOption);
				maxDur = getMaxDuration(covOption);
				for (dur = 1; dur < maxDur; dur++) {
					calculationResult = getDocsResultForID(getObjectDurId(covOption.getId(), dur)); //Find Results than match CovOption id + duration
					if (calculationResult != null) {
						covOptionExtension.addExtractScheduleInfo(createExtractScheduleInfoFromCalc(calculationResult, issueAge, dur));
					}
				}
			}
		}
	}
	/**
	 * Construct ExtractScheduleInfo objects for the Annuity for which there are values in the 
	 * VPMS model calculation results. Depending on the values, an ExtractScheduleInfo may be 
	 * created for each duration from zero to maturity.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractScheduleInfoForAnnuity(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		CalculationResult calculationResult;
		Annuity annuity = getAnnuity(tXLifeRequest);
		AnnuityExtension annuityExtension = getAnnuityExtension(annuity);
		Policy policy = getPolicy(tXLifeRequest);
		// SPR3290 code deleted
		int maxDur = getMaxDuration(policy);
		int issueAge = getIssueAge(tXLifeRequest);
		for (int dur = 1; dur < maxDur; dur++) {
			calculationResult = getDocsResultForID(getObjectDurId(policy.getId(), dur));
			if (calculationResult != null) {
				annuityExtension.addExtractScheduleInfo(createExtractScheduleInfoFromCalc(calculationResult, issueAge, dur));
			}
		}
	}
	/**
	 * Construct ExtractScheduleInfofrom the values returned from the model
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected ExtractScheduleInfo createExtractScheduleInfoFromCalc(CalculationResult calculationResult, int issueAge, int dur) {
		CalcProduct calcProduct;
		String field;
		int prodCount;
		int prdIdx;
		ExtractScheduleInfo extractScheduleInfo = new ExtractScheduleInfo();
		extractScheduleInfo.setDuration(dur);
		extractScheduleInfo.setAge(issueAge + dur - 1);
		prodCount = calculationResult.getCalcProductCount();
		for (prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current ExtractScheduleInfo
			calcProduct = calculationResult.getCalcProductAt(prdIdx);
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("PolicyEtiYears")) {
				extractScheduleInfo.setETIYears(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyEtiMonths")) {
				extractScheduleInfo.setETIMonths(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyPureEndowment")) {
				extractScheduleInfo.setEndowmentAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyAnnualPremium")) {
				extractScheduleInfo.setAnnualPremAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrAccumPrem")) {
				extractScheduleInfo.setTotAnnualPremAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarAnnualPremium")) {
				extractScheduleInfo.setGuarPrem(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrSummIllusDb")) {
				extractScheduleInfo.setDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrAccumValue")) {
				extractScheduleInfo.setCashValue(calcProduct.getValue());
				extractScheduleInfo.setPolicyValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrCsv")) {
				extractScheduleInfo.setCashSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarCsv")) {
				extractScheduleInfo.setGuarCashValueAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovRpuAmount")) {
				extractScheduleInfo.setRPUAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovEtiYears")) {
				extractScheduleInfo.setETIYears(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovEtiDays")) {
				extractScheduleInfo.setETIDays(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovPureEndowmentAmount")) {
				extractScheduleInfo.setEndowmentAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovTotalAnnualPremium")) {
				extractScheduleInfo.setTotAnnualPremAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarDeathBenefit")) {
				extractScheduleInfo.setAmtOfIns(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrAnnualPremium")) {
				extractScheduleInfo.setAnnualPremAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrDeathBenefit")) {
				extractScheduleInfo.setDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrCsv")) {
				extractScheduleInfo.setCashSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovNfValueFactor")) {
				extractScheduleInfo.setNFFactor(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarAnnualPremium")) {
				extractScheduleInfo.setGuarPrem(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCOIDeduction")) {
				extractScheduleInfo.setCOIAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("BenefitCOIDeduction")) {
				extractScheduleInfo.setCOIAmt(calcProduct.getValue());
			}

		}
		return extractScheduleInfo;
	}
	/**
	 * Create SummaryProjections.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractSummaryProjections(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		if (getNewNbaTXLife().isLife()) {
			addExtractSummaryProjectionsForLife(tXLifeRequest);
		} else if (getNewNbaTXLife().isAnnuity()) {
			addExtractSummaryProjectionsForAnnuity(tXLifeRequest);
		}
	}
	/**
	 * Create SummaryProjections for each duration of the Annuity
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractSummaryProjectionsForAnnuity(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		int issueAge = getIssueAge(tXLifeRequest);
		CalculationResult calculationResult;
		Policy policy = getPolicy(tXLifeRequest);
		Annuity annuity = getAnnuity(tXLifeRequest);
		AnnuityExtension annuityExtension = getAnnuityExtension(annuity);
		int maxDur = getMaxDuration(policy);
		//Create ExtractSummaryProjections for Annuity
		for (int dur = 1; dur < maxDur; dur++) {
			calculationResult = getDocsResultForID(getObjectDurId(policy.getId(), dur)); //Find Results than match policy id + duration
			if (calculationResult != null) {
				annuityExtension.addExtractSummaryProjections(createSummaryProjectionsFromCalc(dur, issueAge, calculationResult));
			}
		}
	}
	/**
	 * Create SummaryProjections for each duration of the Policy, each Coverage, and each CovOption
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void addExtractSummaryProjectionsForLife(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		int dur;
		// SPR3290 code deleted
		int issueAge = getIssueAge(tXLifeRequest);
		// SPR3290 code deleted
		CalculationResult calculationResult;
		Policy policy = getPolicy(tXLifeRequest);
		PolicyExtension policyExtension = getPolicyExtension(policy);
		int maxDur = getMaxDuration(policy);
		//Create ExtractSummaryProjections for Policy
		for (dur = 1; dur < maxDur; dur++) {
			calculationResult = getDocsResultForID(getObjectDurId(policy.getId(), dur)); //Find Results than match policy id + duration
			if (calculationResult != null) {
				policyExtension.addExtractSummaryProjections(createSummaryProjectionsFromCalc(dur, issueAge, calculationResult));
			}
		}
		//Create ExtractSummaryProjections for Coverages
		Life life = getLife(tXLifeRequest);
		int covCount = life.getCoverageCount();
		Coverage coverage;
		for (int i = 0; i < covCount; i++) {
			coverage = life.getCoverageAt(i);
			CoverageExtension coverageExtension = getCoverageExtension(coverage);
			maxDur = getMaxDuration(coverage);
			issueAge = getIssueAge(coverage);
			for (dur = 1; dur < maxDur; dur++) {
				calculationResult = getDocsResultForID(getObjectDurId(coverage.getId(), dur)); //Find Results than match Coverage id + duration
				if (calculationResult != null) {
					coverageExtension.addExtractSummaryProjections(createSummaryProjectionsFromCalc(dur, issueAge, calculationResult));
				}
			}
			if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
				ExtractSummaryProjections ceii;
				ExtractSummaryProjections peii;
				int covExtcnt = coverageExtension.getExtractSummaryProjectionsCount();
				int polExtcnt = policyExtension.getExtractSummaryProjectionsCount();
				for (int j = 0; j < covExtcnt; j++) {
					ceii = coverageExtension.getExtractSummaryProjectionsAt(j);
					for (int k = 0; k < polExtcnt; k++) {
						peii = policyExtension.getExtractSummaryProjectionsAt(k);
						if (ceii.getDuration() == peii.getDuration()) {
							ceii.setCOIDeduction(peii.getCOIDeduction());
							ceii.setDeathBenefitAmt(peii.getDeathBenefitAmt());
							ceii.setGuarDeathBenefitAmt(peii.getGuarDeathBenefitAmt());
							break;
						}
					}
				}
			}
		}
		//Create ExtractSummaryProjections for CovOptions
		CovOption covOption;
		CovOptionExtension covOptionExtension;
		for (int cov = 0; cov < covCount; cov++) {
			coverage = life.getCoverageAt(cov);
			issueAge = getIssueAge(coverage);
			int covOptionCount = coverage.getCovOptionCount();
			for (int i = 0; i < covOptionCount; i++) {
				covOption = coverage.getCovOptionAt(i);
				covOptionExtension = getCovOptionExtension(covOption);
				maxDur = getMaxDuration(covOption);
				for (dur = 1; dur < maxDur; dur++) {
					calculationResult = getDocsResultForID(getObjectDurId(covOption.getId(), dur)); //Find Results than match CovOption id + duration
					if (calculationResult != null) {
						covOptionExtension.addExtractSummaryProjections(createSummaryProjectionsFromCalc(dur, issueAge, calculationResult));
					}
				}
			}
		}
	}
	/*
	 * Create a SummaryProjections from the values returned from the model
	 */
	protected ExtractSummaryProjections createSummaryProjectionsFromCalc(int dur, int issueAge, CalculationResult calculationResult) {
		int prodCount;
		int prdIdx;
		String field;
		ExtractSummaryProjections extractSummaryProjections;
		CalcProduct calcProduct;
		extractSummaryProjections = new ExtractSummaryProjections();
		extractSummaryProjections.setDuration(dur);
		extractSummaryProjections.setAge(issueAge + dur -1);
		prodCount = calculationResult.getCalcProductCount();
		for (prdIdx = 0; prdIdx < prodCount; prdIdx++) { //Get the values for the current SummaryProjections
			calcProduct = calculationResult.getCalcProductAt(prdIdx);
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("PolicyAnnualPremium")) {
				extractSummaryProjections.setAnnualPremAmt(calcProduct.getValue());
				extractSummaryProjections.setPaymentAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarAnnualPremium")) {
				extractSummaryProjections.setGuarPrem(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrCsv")) {
				extractSummaryProjections.setCashSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrAccumValue")) {
				extractSummaryProjections.setCashValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarAccumValue")) {
				extractSummaryProjections.setGuarCashValue(calcProduct.getValue());
				extractSummaryProjections.setGuarAccumValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarCsv")) {
				extractSummaryProjections.setGuarSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyMidpointDeathBenefit")) {
				extractSummaryProjections.setMIDDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyMidpointSummIllusDb")) {
				extractSummaryProjections.setMIDDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarAccumPrem")) {
				extractSummaryProjections.setGuarAccumPrem(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyMidpointAccumPrem")) {
				extractSummaryProjections.setMidpointAccumPrem(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyMidpointAccumValue")) {
				extractSummaryProjections.setMIDCashValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyMidpointAnnualPremium")) {
				extractSummaryProjections.setMidpointAnnualPremium(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyMidpointCsv")) {
				extractSummaryProjections.setMIDSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrDeathBenefit")) {
				extractSummaryProjections.setDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrSummIllusDb")) {
				extractSummaryProjections.setDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarDeathBenefit")) {
				extractSummaryProjections.setGuarDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarSummIllusDb")) {
				extractSummaryProjections.setGuarDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrSurrCharge")) {
				extractSummaryProjections.setSurrCharge(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyGuarSurrCharge")) {
				extractSummaryProjections.setGuarSurrCharge(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("ModePremium")) {
				extractSummaryProjections.setModalPremAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyNetOutlay")) {
				extractSummaryProjections.setNetOutLay(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("PolicyCurrAnnualDividend")) {
				extractSummaryProjections.setDivPaidInCash(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrAnnualPremium")) {
				extractSummaryProjections.setAnnualPremAmt(calcProduct.getValue());
				extractSummaryProjections.setPaymentAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrCsv")) {
				extractSummaryProjections.setCashSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrDeathBenefit")) {
				extractSummaryProjections.setDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarCsv")) {
				extractSummaryProjections.setGuarSurrValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarDeathBenefit")) {
				extractSummaryProjections.setGuarDeathBenefitAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarAnnualPremium")) {
				extractSummaryProjections.setGuarAnnualPremium(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCurrAccumValue")) {
				extractSummaryProjections.setCashValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovGuarAccumValue")) {
				extractSummaryProjections.setGuarCashValue(calcProduct.getValue());
				extractSummaryProjections.setGuarAccumValue(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("CovCOIDeduction")) {
				extractSummaryProjections.setCOIDeduction(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("BenefitCurrPremium")) {
				extractSummaryProjections.setPaymentAmt(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("BenefitGuarPremium")) {
				extractSummaryProjections.setGuarPrem(calcProduct.getValue());
			} else if (field.equalsIgnoreCase("BenefitCOIDeduction")) {
				extractSummaryProjections.setCOIDeduction(calcProduct.getValue());
			}
		}
		return extractSummaryProjections;
	}
	/**
	 * Add a TXLifeRequest object for a Print Extract
	 * @param tXLifeRequest containing the values for the Extract
	 */
	protected void addTXLifeRequestForExtract(TXLifeRequest tXLifeRequest) {
		TXLife txLife = getNewNbaTXLife().getTXLife();
		UserAuthRequestAndTXLifeRequest request = txLife.getUserAuthRequestAndTXLifeRequest();
		request.addTXLifeRequest(tXLifeRequest);
	}
	/**
	 * Add AltPremMode objects to the TXLifeRequest for each of the Standard Modes: 
	 * Monthly, Quarterly, SemiAnnual and Annual.
	 * @param tXLifeRequest
	 * @throws NbaBaseException
	 */
	protected void calculateAltPremModePaymentAmts(TXLifeRequest tXLifeRequest) throws NbaBaseException {
		Policy policy = getPolicy(tXLifeRequest);
		if (policy != null) {
			policy.setAltPremMode(getAltPremModes(policy));
		}
	}
	/**
	 * Create a TXLifeRequest for the Agent Card extract
	 *
	 */
	protected TXLifeRequest createAgentCardObjects() throws NbaBaseException {
		TXLifeRequest tXLifeRequest = getCopyOfCommonTXLifeRequest();
		// Create a FormInstace for the Agent Card extract
		addAttachmentToTXLifeRequest(OLI_ATTACH_AGTCARD, tXLifeRequest, OLI_LU_BASICATTMNTTY_TEXT);
		//Add AltPremMode objects to the TXLifeRequest for each of the Standard Modes
		calculateAltPremModePaymentAmts(tXLifeRequest);
		// Add the completed TXLifeRequest 
		addTXLifeRequestForExtract(tXLifeRequest);
		return tXLifeRequest;
	}
	/**
	 * Create a NbaTXRequestVO for the extracts.
	 * @return NbaTXLife
	 */
	protected NbaTXRequestVO createNbaTXRequestVO() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(TC_TYPE_CONTRACTPRINTEXT);
		nbaTXRequest.setTransMode(TC_MODE_ORIGINAL);
		nbaTXRequest.setTranContentCode(TC_CONTENT_INSERT);
		nbaTXRequest.setObjectType(OLI_POLICY);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getNbaUserVO()));
		nbaTXRequest.setNbaLob(getTransactionDst().getNbaLob());
		return nbaTXRequest;
	}
	/**
	 * Create a NbaVpmsException to describe a VPMS problem
	 * @param vpmsComputeResult
	 * @return NbaVpmsException
	 */
	protected NbaVpmsException createNbaVpmsException(VpmsComputeResult vpmsComputeResult) {
		StringBuffer buff = new StringBuffer();
		buff.append("VPMS error in ");
		buff.append(NbaVpmsAdaptor.CONTRACT_PRINT_ATTACHMENTS);
		buff.append(".");
		buff.append(NbaVpmsAdaptor.EP_GET_PRINT_IMAGE_ATTACHMENTS);//APSL4639
		buff.append(": ");
		if (vpmsComputeResult.getRefField() != null && vpmsComputeResult.getRefField().length() > 0) {
			buff.append("missing field: ");
			buff.append(vpmsComputeResult.getRefField());
		} else {
			if (vpmsComputeResult.getMessage() != null && vpmsComputeResult.getMessage().length() > 0) {
				buff.append(vpmsComputeResult.getMessage());
			}
		}
		return new NbaVpmsException(buff.toString());
	}
	/**
	 * Create a TXLifeRequest for the Policy Summary extract
	 *
	 */
	protected TXLifeRequest createPolicySummaryObjects() throws NbaBaseException {
		TXLifeRequest tXLifeRequest = getCopyOfCommonTXLifeRequest();
		// Create a FormInstance for the Policy Summary extract
		addAttachmentToTXLifeRequest(OLI_ATTACH_POLSUM, tXLifeRequest, OLI_LU_BASICATTMNTTY_TEXT);
		//Create ExtractIllusSummaryInfo for each duration of the policy, each coverage, and each benefit		
		addExtractIllusSummaryInfo(tXLifeRequest);
		//Update Life death benefit and lapse fields
		updateProjectedAmts(tXLifeRequest);
		//Create SummaryProjections for each duration of the policy, each coverage, and each benefit		
		addExtractSummaryProjections(tXLifeRequest);
		// Add the completed TXLifeRequest
		addTXLifeRequestForExtract(tXLifeRequest);
		return tXLifeRequest;
	}
	/**
	 *  Create Objects as needed based on the report types requested. 
	 */
	protected void createReportObjects() throws NbaBaseException, NbaVpmsException {
		try { //SPR3362
			String extComp = getTransactionDst().getNbaLob().getExtractComp();
			NbaStringTokenizer extTokens = new NbaStringTokenizer(extComp, ",");
			TXLifeRequest tXLifeRequest;
			String reportCode;
			long reportValue;
			while (extTokens.hasMoreTokens()) {
				reportCode = extTokens.nextToken();
				tXLifeRequest = null;
				reportValue = Long.parseLong(reportCode);
				if (reportValue == OLI_ATTACH_AGTCARD) {
					tXLifeRequest = createAgentCardObjects();
				} else if (reportValue == OLI_ATTACH_POLSUM) {
					tXLifeRequest = createPolicySummaryObjects();
				} else if (reportValue == OLI_ATTACH_POLVAL) {
					tXLifeRequest = createValuesPageObjects();
				} else if (reportValue == OLI_ATTACH_POLSCHED) {
					tXLifeRequest = createSchedulePageObjects();
				} else {
					throw new NbaBaseException("Invalid report type in EXTC LOB field: " + reportCode);
				}
				if (tXLifeRequest != null) {
					addAttachmentsForReport(reportCode, tXLifeRequest);
				}
			}
		} finally { //SPR3362
			removePrintAttachmentsVpmsAdaptor();
		} //SPR3362

	}
	/**
	 * Create a TXLifeRequest for the Schedule Page extract
	 *
	 */
	protected TXLifeRequest createSchedulePageObjects() throws NbaBaseException {
		TXLifeRequest tXLifeRequest = getCopyOfCommonTXLifeRequest();
		addAttachmentToTXLifeRequest(OLI_ATTACH_POLSCHED, tXLifeRequest, OLI_LU_BASICATTMNTTY_TEXT);
		// Create a FormInstace for the Agent Card extract
		addTXLifeRequestForExtract(tXLifeRequest); // Add the completed TXLifeRequest 
		calculateAltPremModePaymentAmts(tXLifeRequest);
		updateProjectedAmts(tXLifeRequest);
		//P2AXAL006 Commented code as it is overriden in AxaLifeContractPrintExtractFormater
		//initializePolicyProductInfo(tXLifeRequest);
		return tXLifeRequest;
	}
	/**
	 * Create a TXLifeRequest for the Values Page extract
	 *
	 */
	protected TXLifeRequest createValuesPageObjects() throws NbaBaseException {

		TXLifeRequest tXLifeRequest = getCopyOfCommonTXLifeRequest();
		//Create a FormInstace for the Values Page extract
		addAttachmentToTXLifeRequest(OLI_ATTACH_POLVAL, tXLifeRequest, OLI_LU_BASICATTMNTTY_TEXT);
		// Create ExtractScheduleInfo for each duration of each coverage
		addExtractScheduleInfo(tXLifeRequest);
		// Update Life death benefit and lapse fields
		updateProjectedAmts(tXLifeRequest);
		// Create ExtractValuesInfo for each durtation for the primary coverage
		addExtractValuesInfo(tXLifeRequest);
		// Add the completed TXLifeRequest
		addTXLifeRequestForExtract(tXLifeRequest);
		return tXLifeRequest;
	}
	/**
	 * Create the Contract Print Extract Source and attach it to the work item.
	 * @return Contract Print Extract Source or null
	 * @throws Exception 
	 */
	public NbaSource generateContractPrintSource(NbaUserVO nbaUserVO, NbaDst transactionDst, NbaTXLife nbaTxLife) throws NbaBaseException {
		setNbaUserVO(nbaUserVO);
		setTransactionDst(transactionDst);
		//Begin SR545390 Retrofit
//		String extComp2 = transactionDst.getNbaLob().getExtractCompAt(2);
		String extComp2 = transactionDst.getNbaLob().getPrintExtract(); //APSL5055
		boolean reprintInd = (extComp2 != null && extComp2.trim().length() > 0);
		boolean unboundInd = (extComp2 != null && NbaConstants.UNBOUND_EXTRACT.equalsIgnoreCase(extComp2.trim())); ///NBLXA-1308
		if (!reprintInd) {	
			//Begin APSL3992
			try{
				voidPreviousDeliveryInstructions(nbaTxLife); 
			}catch (Exception e){
				throw new NbaBaseException(e); 
			}
			//End APSL3992
			addSpecialInstructions(nbaTxLife, transactionDst);
			setInitialPaymentDueDate(nbaTxLife, transactionDst);
			setPremiumDueRequirementDetails(nbaTxLife, transactionDst);
		}//End SR545390 Retrofit

		// NBLXA-188(APSL5318) Legacy Decommissioning
		if (NbaUtils.isGIApplication(nbaTxLife)) {
			if (!reprintInd && !nbaTxLife.isPaidReIssue() && !NbaSystemDataDatabaseAccessor.isPrintPass(nbaTxLife.getPolicy().getPolNumber())) {
				setPolicyDeliveryRequirementDetails(nbaTxLife);
			} else {
				PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
				if (policyExtn != null) {
					policyExtn.setPrintTogetherIND(false);
					policyExtn.setMDRConsentIND(false);
					policyExtn.setActionUpdate();
				}
			}
		}
		// NBLXA-188(APSL5318) Legacy Decommissioning



		if(!unboundInd){  // /NBLXA-1308
			updateOldEpolicyData(nbaTxLife); //APSL5100
			PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
			if (policyExtension != null && policyExtension.getPrintPreviewNeededInd()) {
				constructEpolicyData(nbaTxLife, transactionDst); // APSL5100
				policyExtension.deleteContractChangeReprintDate(); //NBLXA-1786
			}}
		setNbaTxLife(nbaTxLife);
		initializeNewNbaTXLife();
		updateCommonData();
		createReportObjects();
		TXLife txLife = getNewNbaTXLife().getTXLife();
		UserAuthRequestAndTXLifeRequest request = txLife.getUserAuthRequestAndTXLifeRequest();
		request.removeTXLifeRequestAt(0); //Remove the original
		new NbaOLifEId(newNbaTXLife).assureId(newNbaTXLife); //AXAL3.7.14
		return new NbaSource(NbaConstants.A_BA_NBA, NbaConstants.A_ST_CONTRACT_PRINT_EXTRACT, newNbaTXLife.toXmlString());
	}
	/**
	 * Calculate the payment amounts for and create AltPremMode objects for each of the 
	 * Standard Modes: Monthly, Quarterly, SemiAnnual and Annual.
	 * @param policy
	 * @return
	 * @throws NbaBaseException
	 */
	protected ArrayList getAltPremModes(Policy policy) throws NbaBaseException {
		if (altPremModes == null) {
			altPremModes = new ArrayList();
			try {
				NbaCalculation nbaCalculation;
				CalcProduct calcProduct;
				String paymentMode;
				String paymentAmt;
				AltPremMode altPremMode;
				Date startTime = Calendar.getInstance().getTime();
				nbaCalculation =
						NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_ALL_STD_MODES_PREMIUM, getNbaTxLife());
				logElapsedTime(startTime, "Alternate Premium Modes VPMS calculation");
				if (nbaCalculation.getCalcResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
					throw new NbaVpmsException(nbaCalculation.toString());
				}
				int count = nbaCalculation.getCalculationResultCount();
				CalculationResult calculationResult;
				for (int i = 0; i < count; i++) {
					calculationResult = nbaCalculation.getCalculationResultAt(i);
					//begin SPR2692
					if (calculationResult.hasCalcError()) {
						throw new NbaBaseException(
								"Alternate mode premium calculation error " + calculationResult.getCalcError().getMessage(),
								NbaExceptionType.ERROR);	//SPR2692
					} else {
						//end SPR2692
						paymentMode = calculationResult.getObjectId();
						calcProduct = calculationResult.getCalcProductAt(0);
						paymentAmt = calcProduct.getValue();
						altPremMode = new AltPremMode();
						altPremMode.setPaymentMode(paymentMode);
						altPremMode.setPaymentAmt(paymentAmt);
						altPremMode.setPaymentMethod(policy.getPaymentMethod());
						altPremModes.add(altPremMode);
					}		//SPR2692
				}
			} catch (NbaBaseException e) {
				throw e;
			} catch (Throwable e) {
				throw new NbaBaseException(e.toString());	//SPR2692
			}
		}
		return altPremModes;
	}
	/**
	 * Retrieve  the Annuity object in the TXLifeRequest
	 * @param tXLifeRequest
	 * @return Annuity
	 */
	protected Annuity getAnnuity(TXLifeRequest tXLifeRequest) {
		Annuity annuity = null;
		Policy policy = getPolicy(tXLifeRequest);
		if (policy != null) {
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			if (ladh.isAnnuity()) {
				annuity = ladh.getAnnuity();
			}
		}
		return annuity;
	}
	/**
	 * Returns the AnnuityExtension. Create a new AnnuityExtension if necessary. 
	 * @return AnnuityExtension
	 */
	protected AnnuityExtension getAnnuityExtension(Annuity annuity) {
		AnnuityExtension annuityExtension = NbaUtils.getFirstAnnuityExtension(annuity);
		if (annuityExtension == null) {
			com.csc.fsg.nba.vo.txlife.OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_POLICY); //NBA237
			annuity.addOLifEExtension(oLifEExtension);
			annuityExtension = NbaUtils.getFirstAnnuityExtension(annuity);
		}
		return annuityExtension;
	}
	/**
	 * Retrieve the Attachments Types identified by the VPMS model to be included in the report
	 * @return attachmentTypesForReport
	 */
	protected ContractPrintAttachments getAttachmentTypesForReport() {
		return attachmentTypesForReport;
	}
	/**
	 * Return the DST for the Case
	 * @return NbaDst
	 */
	protected NbaDst getCaseDst() throws NbaBaseException {
		if (caseDst == null) {
			retrieveCase();
		}
		return caseDst;
	}
	/**
	 * Retrieve the TXLifeRequest object containing the common data
	 * @return commonTXLifeRequest
	 */
	protected String getCommonTXLifeRequest() {
		return commonTXLifeRequest;
	}
	/**
	 * Retrieve a copy of the TXLifeRequest object containing the common contract data.
	 * @return
	 */
	protected TXLifeRequest getCopyOfCommonTXLifeRequest() throws NbaBaseException {
		TXLifeRequest tXLifeRequest = null;
		try {
			tXLifeRequest = TXLifeRequest.unmarshal(new ByteArrayInputStream(getCommonTXLifeRequest().getBytes()));
		} catch (Exception e) {
			throw new NbaBaseException("Unable to unmarshall TXLifeRequest", e);
		}
		return tXLifeRequest;
	}
	/**
	 * Returns the CoverageExtension. Create a new CoverageExtension if necessary. 
	 * @return CoverageExtension
	 */
	protected CoverageExtension getCoverageExtension(Coverage coverage) {
		CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
		if (coverageExtension == null) {
			com.csc.fsg.nba.vo.txlife.OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_COVERAGE); //NBA237
			coverage.addOLifEExtension(oLifEExtension);
			coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
		}
		return coverageExtension;
	}
	/**
	 * Returns the CovOptionExtension. Create a new CovOptionExtension if necessary. 
	 * @return CovOptionExtension
	 */
	protected CovOptionExtension getCovOptionExtension(CovOption covOption) {
		CovOptionExtension covOptionExtension = NbaUtils.getFirstCovOptionExtension(covOption);
		if (covOptionExtension == null) {
			com.csc.fsg.nba.vo.txlife.OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_COVOPTION); //NBA237
			covOption.addOLifEExtension(oLifEExtension);
			covOptionExtension = NbaUtils.getFirstCovOptionExtension(covOption);
		}
		return covOptionExtension;
	}
	/**
	 * Get the issue age of the insured for a Coverage
	 * @param coverage
	 * @return age
	 */
	protected int getIssueAge(Coverage coverage) {
		int age = 0;
		LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
		if (lifeParticipant != null && lifeParticipant.hasIssueAge()) {
			age = lifeParticipant.getIssueAge();
		}
		return age;
	}
	/**
	 * Get the issue age of the primary insured or annuitant.
	 * @param coverage
	 * @return age
	 */
	protected int getIssueAge(TXLifeRequest tXLifeRequest) {
		int age = 0;
		Life life = getLife(tXLifeRequest);
		if (life != null) {
			Coverage coverage;
			for (int i = 0; i < life.getCoverageCount(); i++) {
				coverage = life.getCoverageAt(i);
				if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
					age = getIssueAge(coverage);
					break;
				}
			}
		} else {
			Annuity annuity = getAnnuity(tXLifeRequest);
			if (annuity != null) {
				Payout payout = NbaUtils.getFirstPayout(annuity);
				for (int i = 0; i < payout.getParticipantCount(); i++) {
					Participant participant = payout.getParticipantAt(i);
					if (NbaUtils.isAnnuitantParticipant(participant)) {
						age = participant.getIssueAge();
						break;
					}
				}
			}
		}
		return age;
	}
	/**
	 * Retrieve the NbaCalculation from the applicable Document calculation model.
	 * @return NbaCalculation
	 */
	protected NbaCalculation getDocsCalculation() throws NbaBaseException {
		if (docsCalculation == null) {
			Date startTime = Calendar.getInstance().getTime();
			docsCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_CONTRACT_DOCS, getNbaTxLife());
			logElapsedTime(startTime, "Contract Documents VPMS calculation");
			if (docsCalculation.getCalcResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
				throw new NbaVpmsException("Contract Documents VPMS calculation failure");
			}
		}
		return docsCalculation;
	}
	/**
	 * Retrieve the CalculationResult for the object identified by the id. Return
	 * null if there is no matching CalculationResult.
	 * @param id - the id of the object, including a duration value if applicable
	 * @return CalculationResult
	 * @throws NbaBaseException
	 */
	protected CalculationResult getDocsResultForID(String id) throws NbaBaseException {
		CalculationResult calculationResult = null;
		int resultCount = getDocsCalculation().getCalculationResultCount();
		int resIdx;
		for (resIdx = 0; resIdx < resultCount; resIdx++) {
			calculationResult = (CalculationResult) getDocsCalculation().getCalculationResult().get(resIdx);
			if (id.equals(calculationResult.getObjectId())) {
				return calculationResult;
			}
		}
		return null;
	}
	/**
	 * Retrieve  the Life object in the TXLifeRequest
	 * @param tXLifeRequest
	 * @return Life
	 */
	protected Life getLife(TXLifeRequest tXLifeRequest) {
		Life life = null;
		Policy policy = getPolicy(tXLifeRequest);
		if (policy != null) {
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			if (ladh.isLife()) {
				life = ladh.getLife();
			}
		}
		return life;
	}
	/**
	 * Calculate the maximum duration for a Coverage as the differrence in years between
	 * the TermDate and the EffDate
	 * @param coverage
	 * @return the duration in years
	 */
	protected int getMaxDuration(Coverage coverage) {
		return NbaUtils.calcYearsDiff(coverage.getTermDate(), coverage.getEffDate()) + 1;
	}
	/**
	 * Calculate the maximum duration for a CovOption as the differrence in years between
	 * the TermDate and the EffDate
	 * @param covOption
	 * @return the duration in years
	 */
	protected int getMaxDuration(CovOption covOption) {
		return NbaUtils.calcYearsDiff(covOption.getTermDate(), covOption.getEffDate()) + 1;
	}
	/**
	 * Calculate the maximum duration for a Policy as the differrence in years between
	 * the TermDate and the EffDate
	 * @param policy
	 * @return the duration in years
	 */
	protected int getMaxDuration(Policy policy) {
		return NbaUtils.calcYearsDiff(policy.getTermDate(), policy.getEffDate()) + 1;
	}
	/**
	 * Returns a NbaTableAccessor using lazy initialization.
	 * @return A new NbaTableAccessor
	 * @see com.csc.fsg.nba.tableaccess.NbaTableAccessor
	 */
	protected NbaTableAccessor getNbaTableAccessor() {
		if (nbaTableAccessor == null) {
			nbaTableAccessor = new NbaTableAccessor();
		}
		return nbaTableAccessor;
	}
	/**
	 * Retrieve the NbaTXLife wrapper object for a TXLife
	 * @return the NbaTXLife wrapper object for a TXLife
	 */
	protected NbaTXLife getNbaTxLife() {
		return nbaTxLife;
	}
	/**
	 * Retrieve the NbaUser value object
	 * @return the NbaUser value object
	 */
	protected NbaUserVO getNbaUserVO() {
		return nbaUserVO;
	}
	/**
	 * Retrieve the NbaTXLife which contain the contract data for the extracts
	 * @return NbaTXLife
	 */
	protected NbaTXLife getNewNbaTXLife() {
		return newNbaTXLife;
	}
	//NBA213 deleted code

	/**
	 * Format the string to be used to identify an object by duration
	 * @param dur
	 */
	protected String getObjectDurId(String id, int dur) {
		StringBuffer buff = new StringBuffer();
		buff.append(id);
		buff.append("[");
		buff.append(String.valueOf(dur));
		buff.append("]");
		return (buff.toString());
	}
	/**
	 * Retrieve  the Policy from the TXLifeRequest
	 * @param tXLifeRequest
	 * @return Policy
	 */
	protected Policy getPolicy(TXLifeRequest tXLifeRequest) {
		Policy policy = null;
		if (tXLifeRequest.hasOLifE()) {
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(tXLifeRequest.getOLifE());
			policy = holding.getPolicy();
		}
		return policy;
	}
	/**
	 * Retrieve  the Base Coverage 
	 * @param policy
	 * @return Coverage
	 */
	protected Coverage getPrimarycoverage(Policy policy) {
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
		Coverage coverage = null;
		int cnt = life.getCoverageCount();
		for (int i = 0; i < cnt; i++) {
			if (life.getCoverageAt(i).getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
				coverage = life.getCoverageAt(i);
				break;
			}
		}
		return coverage;
	}
	/**
	 * Returns the policyExtension. Create a new PolicyExtension if necessary. 
	 * @return PolicyExtension
	 */
	protected PolicyExtension getPolicyExtension(Policy policy) {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension == null) {
			com.csc.fsg.nba.vo.txlife.OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_POLICY); //NBA237
			policy.addOLifEExtension(oLifEExtension);
			policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		}
		return policyExtension;
	}
	/**
	 * Retrieve the Primary Holding using lazy initialization
	 * @return the Primary Holding 
	 */
	protected Holding getPrimaryHolding() {
		if (primaryHolding == null) {
			primaryHolding = getNbaTxLife().getPrimaryHolding();
		}
		return primaryHolding;
	}
	/**
	 * Retrieve the NbaVpmsAdaptor instance with lazy initialization.
	 * @return NbaVpmsAdaptor
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	protected NbaVpmsAdaptor getPrintAttachmentsVpmsAdaptor() throws NbaVpmsException, NbaBaseException {
		if (printAttachmentsVpmsAdaptor == null) {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getTransactionDst().getNbaLob());
			printAttachmentsVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CONTRACT_PRINT_ATTACHMENTS);
			printAttachmentsVpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_PRINT_IMAGE_ATTACHMENTS);//APSL4639
			printAttachmentsVpmsAdaptor.setSkipAttributesMap(new HashMap());
		}
		return printAttachmentsVpmsAdaptor;
	}	

	/**
	 * Retrieve the Source on the Case which mathces the Source type and form number returned from the VPMS model.
	 * If the form number from the model is missing or "*", bypass form number matching. 
	 * @return
	 */
	protected NbaSource getSourceForAttachment(ContractPrintAttachment attachment) throws NbaBaseException {
		NbaSource nbaSource = null;
		List sources = getCaseDst().getNbaSources();
		for (int i = 0; i < sources.size(); i++) {
			if (((NbaSource) sources.get(i)).getSource().getSourceType().equalsIgnoreCase(attachment.getSourceType())) { //Match on Source type,AXAL3.7.14
				nbaSource = (NbaSource) sources.get(i);//AXAL3.7.14
				//Begin ALS5296
				String formNumberOrReqCode = attachment.getSourceFormTypeOrReqCode(); 
				if (formNumberOrReqCode == null || formNumberOrReqCode.equals("*")) { 
					break; //Done if form number not specified or not applicable
				} 
				String reqType = String.valueOf(nbaSource.getNbaLob().getReqType());
				String currFormNo = nbaSource.getNbaLob().getFormNumber();
				if (currFormNo != null && currFormNo.equalsIgnoreCase(formNumberOrReqCode)) {
					break; //Done if form number matches
				}
				if (reqType != null && reqType.equalsIgnoreCase(formNumberOrReqCode)) {
					break; //Done if requirement type matches
				} else if (currFormNo != null && currFormNo.equalsIgnoreCase(formNumberOrReqCode)) {
					break;
				}
				// End ALS5296
				nbaSource = null;//AXAL3.7.14
			}
		}
		return nbaSource;
	}
	/**
	 * Returns the tblKeys.
	 * @return Map
	 */
	protected Map getTblKeys() {
		if (tblKeys == null) {
			try {
				tblKeys = getNbaTableAccessor().setupTableMap(getTransactionDst());
			} catch (Exception e) {
				tblKeys = new HashMap();
			}
		}
		return tblKeys;
	}
	/**
	 * Retrieve the DST for the Print Extract (NBPRTEXT) Transaction
	 * @return the DST for the Print Extract Transaction
	 */
	protected NbaDst getTransactionDst() {
		return transactionDst;
	}
	/**
	 * Create the new NbaTXLife request object which will contain the contract data for the extracts.
	 * The new NbaTXLife has a TXLifeRequest object which is initialized with the OLife from the TXLifeResponse
	 * in retrieved during initialization. Lazy initialization will populate the remaining OLife objects in 
	 * the request object. 
	 * @param nbaTXLife
	 */
	protected void initializeNewNbaTXLife() throws NbaBaseException {
		setNewNbaTXLife(new NbaTXLife(createNbaTXRequestVO()));
		UserAuthRequestAndTXLifeRequest userAuthRequestAndTXLifeRequest = getNewNbaTXLife().getTXLife().getUserAuthRequestAndTXLifeRequest();
		TXLifeRequest newRequest = (TXLifeRequest) userAuthRequestAndTXLifeRequest.getTXLifeRequest().get(0);
		newRequest.setOLifE(getNbaTxLife().getOLifE()); //Copy Olife from the current contract data to the new
		removeExistingObjects(); //Prevent attachments, etc from being added from database
	}
	/**
	 * Update the new NbaTXLife request object with Policy Product information. 
	 */
	/*P2AXAL006 Commented as it is not used
	 * protected void initializePolicyProductInfo(com.csc.fs.dataobject.accel.product.TXLifeRequest tXLifeRequest) throws NbaBaseException {
		AccelProduct product; //NBA237
		//NBA213 deleted code
		NbaProductAccessFacadeBean nbaProductAccessFacade = new NbaProductAccessFacadeBean();  //NBA213
		product = nbaProductAccessFacade.doProductInquiry(getNewNbaTXLife());
		//NBA213 deleted code
		com.csc.fs.dataobject.accel.product.OLifE oLifE = tXLifeRequest.getOLifE();
		if (getNewNbaTXLife().isLife()) {
			Life life = getLife(tXLifeRequest);
			// SPR3290 code deleted
			String plan;
			int covCount = life.getCoverageCount();
			for (int i = 0; i < covCount; i++) {
				plan = life.getCoverageAt(i).getProductCode();
				oLifE.setPolicyProduct(product.getOLifE().getPolicyProduct());
			}
		} else if (getNewNbaTXLife().isAnnuity()) {
			oLifE.setPolicyProduct(product.getOLifE().getPolicyProduct());
		}
		PolicyProduct policyProduct;
		for (int i = 0; i < oLifE.getPolicyProductCount(); i++) {
			policyProduct = oLifE.getPolicyProductAt(i);
			policyProduct.setInvestProductInfoGhost(new ArrayList());
			policyProduct.setLifeProductOrAnnuityProductGhost(new LifeProductOrAnnuityProduct());
			policyProduct.setOwnershipGhost(new ArrayList());
			policyProduct.setBusinessProcessAllowedGhost(new ArrayList());
			policyProduct.setJurisdictionApprovalGhost(new ArrayList());
		}
	}*/
	/**
	 * Prevent objects from being added from database by initializing the corresponding ghost objects in their 
	 * parents to prevent them from being hydrated. The objects excluded are:
	 * OLifE.FormInstance
	 * Party.Attachment
	 * Party.Employment
	 * Party.Risk
	 * Party.PartyExtension.UnderwritingAnalysis
	 * Party.PersonOrOrganization.Organization.OrganizationFinancialData
	 * Party.PersonOrOrganization.Person.PersonExtension.ImpairmentInfo
	 * Policy.RequirementInfo.Attachment
	 * In addition, Life.Coverage.LifeParticipant.SubstandardRatings are removed if 
	 * SubstandardRating.SubstandardRatingExtension.ProposedInd is True.
	 */
	protected void removeExistingObjects() {
		getNewNbaTXLife().getPrimaryHolding().setAttachmentGhost(new java.util.ArrayList());
		Policy policy = getNewNbaTXLife().getPrimaryHolding().getPolicy();
		int cnt;
		ArrayList doNotHydrate = new java.util.ArrayList();
		com.csc.fsg.nba.vo.txlife.OLifE oLifE = getNewNbaTXLife().getOLifE(); //NBA237
		oLifE.setFormInstanceGhost(doNotHydrate);
		cnt = oLifE.getPartyCount();
		Party party;
		PartyExtension partyExtension;
		PersonExtension personExtension;
		for (int i = 0; i < cnt; i++) {
			party = oLifE.getPartyAt(i);
			party.setAttachmentGhost(doNotHydrate);
			party.setEmploymentGhost(doNotHydrate);
			party.setRiskGhost(new Risk());
			partyExtension = NbaUtils.getFirstPartyExtension(party);
			if (partyExtension != null) {
				partyExtension.setUnderwritingAnalysisGhost(new UnderwritingAnalysis());
			}
			if (party.hasPersonOrOrganization()) {
				if (party.getPersonOrOrganization().isOrganization()) {
					party.getPersonOrOrganization().getOrganization().setOrganizationFinancialDataGhost(doNotHydrate);
				} else if (party.getPersonOrOrganization().isPerson()) {
					personExtension = NbaUtils.getFirstPersonExtension(party.getPersonOrOrganization().getPerson());
					if (personExtension != null) {
						personExtension.setImpairmentInfoGhost(doNotHydrate);
					}
				}
			}
		}
		cnt = policy.getRequirementInfoCount();
		for (int i = 0; i < cnt; i++) {
			policy.getRequirementInfoAt(i).setAttachmentGhost(doNotHydrate);
		}
		if (getNewNbaTXLife().isLife()) {
			Life life = getNewNbaTXLife().getLife();
			Coverage coverage;
			LifeParticipant lifeParticipant;
			SubstandardRating substandardRating;
			SubstandardRatingExtension substandardRatingExtension;
			cnt = life.getCoverageCount();
			for (int i = 0; i < cnt; i++) {
				coverage = life.getCoverageAt(i);
				for (int j = 0; j < coverage.getLifeParticipantCount(); j++) {
					lifeParticipant = coverage.getLifeParticipantAt(j);
					for (int k = lifeParticipant.getSubstandardRatingCount() -1; k > -1; k--) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(k);
						substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
						if (substandardRatingExtension != null && substandardRatingExtension.getProposedInd()) {
							lifeParticipant.removeSubstandardRatingAt(k);
						}
					}
				}
			}
		}
	}
	/**
	 * Remove the IVpmsProduct EJB instance and any loaded models
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 */
	protected void removePrintAttachmentsVpmsAdaptor() throws NbaVpmsException {
		if (printAttachmentsVpmsAdaptor != null) {
			try {
				printAttachmentsVpmsAdaptor.remove();
			} catch (RemoteException e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
			}
		}
	}
	/**
	 * Retrieve the Case work item and its Sources from AWD.
	 */
	protected void retrieveCase() throws NbaBaseException {
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getTransactionDst().getID(), false); //Retrieve the Case
		retOpt.requestCaseAsParent();
		retOpt.requestSources(); //Retrieve the Sources
		retOpt.requestTransactionAsSibling(); //ALS5296
		setCaseDst(WorkflowServiceHelper.retrieveWorkItem(getNbaUserVO(), retOpt));  //NBA213
		//NBA213 deleted code
	}
	/**
	 * Set the Attachments Types to be included in the report
	 * @param attachments
	 */
	protected void setAttachmentTypesForReport(ContractPrintAttachments attachments) {
		attachmentTypesForReport = attachments;
	}
	/**
	 * Set the DST for the Case
	 * @param dst
	 */
	protected void setCaseDst(NbaDst dst) {
		caseDst = dst;
	}
	/**
	 * Set the TXLifeRequest object containing the common data
	 * @param string
	 */
	protected void setCommonTXLifeRequest(TXLifeRequest txLifeRequest) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		txLifeRequest.marshal(stream);
		commonTXLifeRequest = stream.toString();
		try {
			stream.close();
		} catch (java.io.IOException e) {
		}
	}
	/**
	 * Store the NbaTXLife wrapper object for a TXLife
	 * @param life
	 */
	protected void setNbaTxLife(NbaTXLife life) {
		nbaTxLife = life;
	}
	/**
	 * Store the NbaUser value object
	 * @param userVO
	 */
	protected void setNbaUserVO(NbaUserVO userVO) {
		nbaUserVO = userVO;
	}
	/**
	 * Set the NbaTXLife which contain the contract data for the extracts
	 * @param nbaTXLife
	 */
	protected void setNewNbaTXLife(NbaTXLife nbaTXLife) {
		newNbaTXLife = nbaTXLife;
	}
	/**
	 * Store the DST for the Print Extract (NBPRTEXT) Transaction
	 * @param dst
	 */
	protected void setTransactionDst(NbaDst dst) {
		transactionDst = dst;
	}
	/**
	 * Update common fields in the new NbaTXLife 
	 */
	protected void updateCommonData() throws NbaBaseException {
		getPrimaryHolding().getPolicy().getApplicationInfo().setUserCode(getNbaUserVO().getUserID());
		//Create the common portion of the TXLifeRequest to be re-used for each extract
		TXLife txLife = getNewNbaTXLife().getTXLife();
		UserAuthRequestAndTXLifeRequest request = txLife.getUserAuthRequestAndTXLifeRequest();
		TXLifeRequest tXLifeRequest = request.getTXLifeRequestAt(0);
		//		<here>

		if (!NbaUtils.isProductCodeCOIL(getNbaTxLife()) && !NbaConstants.SYST_LIFE70.equalsIgnoreCase(getNbaTxLife().getBackendSystem())) {// P2AXAL029
			CalculationResult calculationResult;
			CalcProduct calcProduct;
			String field;
			Life life = getLife(tXLifeRequest);
			if (life != null) {
				ArrayList covOptions = new ArrayList();
				int covCount = life.getCoverageCount();
				Coverage coverage;
				for (int i = 0; i < covCount; i++) {
					coverage = life.getCoverageAt(i);
					covOptions.addAll(coverage.getCovOption());
					calculationResult = getDocsResultForID(getObjectDurId(coverage.getId(), 1));
					int prodCount = calculationResult != null ? calculationResult.getCalcProductCount() : 0;// AXAL3.7.14
					for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) {
						calcProduct = calculationResult.getCalcProductAt(prdIdx);
						field = calcProduct.getType();
						if (field.equalsIgnoreCase("CovCurrAnnualPremium") && !coverage.hasAnnualPremAmt()) {
							coverage.setAnnualPremAmt(calcProduct.getValue());
						}
					}
				}
				CovOption covOption;
				for (int i = 0; i < covOptions.size(); i++) {
					covOption = (CovOption) covOptions.get(i);
					calculationResult = getDocsResultForID(getObjectDurId(covOption.getId(), 1));
					int prodCount = calculationResult != null ? calculationResult.getCalcProductCount() : 0;// AXAL3.7.14
					for (int prdIdx = 0; prdIdx < prodCount; prdIdx++) {
						calcProduct = calculationResult.getCalcProductAt(prdIdx);
						field = calcProduct.getType();
						if (field.equalsIgnoreCase("BenefitCurrPremium") && !covOption.hasAnnualPremAmt()) {
							covOption.setAnnualPremAmt(calcProduct.getValue());
						}
					}
				}
			}
		}

		setCommonTXLifeRequest(tXLifeRequest);
	}

	/**
	 * Log the elapsed time.
	 */
	protected void logElapsedTime(Date startTime, String message) {
		if (getLogger().isDebugEnabled()) {
			if (startTime == null) {
				return;
			}
			Date endTime = java.util.Calendar.getInstance().getTime();
			float elapsed = ((float) (endTime.getTime() - startTime.getTime())) / 1000;
			StringBuffer elStr = new StringBuffer();
			elStr.append("Elapsed time: ");
			elStr.append(elapsed);
			elStr.append(" seconds ");
			elStr.append(message);
			getLogger().logDebug(elStr.toString());
		}
	}

	//ALS5296
	protected List getAllSources() throws NbaBaseException {
		List sources = getCaseDst().getNbaSources();
		Iterator it = getCaseDst().getNbaTransactions().iterator();
		NbaTransaction tran;
		while (it.hasNext()) {
			tran = (NbaTransaction) it.next();
			sources.addAll(tran.getNbaPrintSources());//QC15359/APSL4570
		}
		return sources;
	}

	//SR545390 New Method Retrofit
	/**
	 * Add Special Instruction to the Policy Delivery Receipt for back due premium and due date 
	 * @throws NbaBaseException
	 */
	protected void addSpecialInstructions(NbaTXLife nbaTxLife, NbaDst nbaDst)throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;//APSL1685Changes Done: Close proxy due to OutOfMemory error
		try {
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaDst.getNbaLob());
			nbaOinkDataAccess.setContractSource(nbaTxLife);
			nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			//Begin  NBLXA-1850 
			Map deOinkMap = new HashMap();
			long appJurisdiction = nbaTxLife.getPolicy().getApplicationInfo().getApplicationJurisdiction();
			if(!NbaUtils.isBlankOrNull(appJurisdiction) && NBA_STATES_NY != appJurisdiction){			
			RequirementInfo reqInfo = nbaTxLife.getRequirementInfoLatest(nbaTxLife.getPrimaryParty().getID(),OLI_REQCODE_NOTICEANDCONCENT);
			if(!NbaUtils.isBlankOrNull(reqInfo)){
				RequirementInfoExtension requirementInfoExtension = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				try {
						if(!NbaUtils.isBlankOrNull(requirementInfoExtension) && !requirementInfoExtension.getIGOInd()  &&  NbaUtils.checkNIGODueToSignatureDate(reqInfo.getAppliesToPartyID(),nbaTxLife)){
						deOinkMap.put("A_NIGOINDPINS", NbaConstants.TRUE_STR);
					}
				} catch (Exception e) {
					getLogger().logError(e);
				}
			}
			if(!NbaUtils.isBlankOrNull(nbaTxLife.getJointParty())){
				RequirementInfo reqInfoJnt = nbaTxLife.getRequirementInfoLatest(nbaTxLife.getPrimaryParty().getID(),OLI_REQCODE_NOTICEANDCONCENT);
				if(!NbaUtils.isBlankOrNull(reqInfoJnt)){
					RequirementInfoExtension requirementInfoExtensionJnt = NbaUtils.getFirstRequirementInfoExtension(reqInfoJnt);
					try {
							if(!NbaUtils.isBlankOrNull(requirementInfoExtensionJnt) && !requirementInfoExtensionJnt.getIGOInd()  && NbaUtils.checkNIGODueToSignatureDate(reqInfoJnt.getAppliesToPartyID(),nbaTxLife)){
							deOinkMap.put("A_NIGOINDJNT", NbaConstants.TRUE_STR);
						}
					} catch (Exception e) {
						getLogger().logError(e);
						}
					}
				}
			}
			//End  NBLXA-1850 
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setSkipAttributesMap(deOinkMap);// NBLXA-1850 
			proxy.setVpmsEntryPoint("P_GetPolicyDeliveryInstruction");			
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {
					String instruction = (String) nbaVpmsResultsData.getResultsData().get(0);
					if (!NbaUtils.isBlankOrNull(instruction)) {
						NbaSpecialInstructionComment nsic = new NbaSpecialInstructionComment();
						nsic.setActionAdd();
						nsic.setOriginator(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER);
						nsic.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
						nsic.setText(instruction);
						getPolicyExtension(nbaTxLife.getPolicy()).setPolicyDeliveryInstructionAmt(NbaVPMSHelper.getPremiumDue(nbaTxLife));//APSL2735
						getPolicyExtension(nbaTxLife.getPolicy()).setPdiscreateDate(new Date());//ALII2015
						nsic.setUserNameEntered(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER);
						nsic.setVoidInd(NbaConstants.FALSE);
						nsic.setInstructionType(NbaConstants.INSTRUCTION_TYPE);
						NbaUtils.addAttachmentForSpecialInstruction(nbaTxLife.getPrimaryHolding(), nsic);
					}
				}
			}

			//NBLXA-1552 Money related change
			if(nbaTxLife!=null && NbaUtils.isGIApplication(nbaTxLife) && !isCWAApplied(nbaTxLife)){
				generateSpecialInstructionForGIApplication(nbaTxLife);
			}
		} catch (RemoteException t) {//APSL1685 Begin
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException t) {
					getLogger().logError(t);
				}
			}
		}//APSL1685 End

		//APSL3922 Begin
		try {
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaDst.getNbaLob());
			nbaOinkDataAccess.setContractSource(nbaTxLife);
			nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setVpmsEntryPoint("P_DeliveryInstructionResult");
			Map deOink = new HashMap();
			List reqInfoList = nbaTxLife.getPolicy().getRequirementInfo();
			int count = reqInfoList.size();
			deOink.put("A_RequirementInfoCount", Long.toString(count));
			if (count == 0) {
				deOink.put("A_ReqCodeList", "");
			} else {
				for (int i = 0; i < count; i++) {
					if (i == 0) {
						deOink.put("A_ReqCodeList", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqCode()));
						deOink.put("A_ReqStatus", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqStatus()));
					} else {
						deOink.put("A_ReqCodeList[" + i + "]", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqCode()));
						deOink.put("A_ReqStatus[" + i + "]", Long.toString(((RequirementInfo) reqInfoList.get(i)).getReqStatus()));
					}
				}
			}
			proxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {
					String instruction = (String) nbaVpmsResultsData.getResultsData().get(0);
					if (!NbaUtils.isBlankOrNull(instruction)) {
						NbaSpecialInstructionComment nsic = new NbaSpecialInstructionComment();
						nsic.setActionAdd();
						nsic.setOriginator(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER); //NBLXA-1312
						nsic.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
						nsic.setText(instruction);                        
						nsic.setUserNameEntered(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER); //NBLXA-1312
						nsic.setVoidInd(NbaConstants.FALSE);
						nsic.setInstructionType(NbaConstants.INSTRUCTION_TYPE);
						NbaUtils.addAttachmentForSpecialInstruction(nbaTxLife.getPrimaryHolding(), nsic);
					}
				}
			}
		} catch (RemoteException t) {//APSL1685 Begin
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException t) {
					getLogger().logError(t);
				}
			}
		}
		// End APSL3922
	} 

	//SR545390 New Method Retrofit
	/**
	 * Set Initial Payment Due Date
	 * Next date after print on which Premium needs to be paid 
	 */
	protected void setInitialPaymentDueDate(NbaTXLife nbaTxLife, NbaDst nbaDst)throws NbaBaseException{
		NbaVpmsAdaptor proxy = null;//APSL1685 Changes Done: Close proxy due to OutOfMemory error
		try {
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaDst.getNbaLob());
			nbaOinkDataAccess.setContractSource(nbaTxLife);
			nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setVpmsEntryPoint("P_GetInitialPaymentDueDate");
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {					
					String initialPaymentDueDate = (String) nbaVpmsResultsData.getResultsData().get(0);
					PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
					polExt.setInitialPaymentDueDate(NbaUtils.getDateFromStringInUSFormat(initialPaymentDueDate));
					polExt.setActionUpdate();
				}
			}
		} catch (RemoteException t) {//APSL1685 Begin
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException t) {
					getLogger().logError(t);
				}
			}
		}//APSL1685 End
	}

	//SR545390 New Method Retrofit
	/**
	 * Get the Requirement Details to be set on outstanding Premium Due Carrier Requirement  
	 */
	protected void setPremiumDueRequirementDetails(NbaTXLife nbaTxLife, NbaDst nbaDst) throws NbaBaseException{
		NbaVpmsAdaptor proxy = null;//APSL1685Changes Done: Close proxy due to OutOfMemory error
		try {
			String reqDetails = null;
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaDst.getNbaLob());
			nbaOinkDataAccess.setContractSource(nbaTxLife);
			nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setVpmsEntryPoint("P_GetPremiumDueRequirementDetails");
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {					
					reqDetails = (String) nbaVpmsResultsData.getResultsData().get(0);					
				}
			}
			List reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(),OLI_REQCODE_PREMDUE);
			if (reqInfoList != null) {
				int count = reqInfoList.size();
				RequirementInfo reqInfo = null;
				for (int i = 0; i < count; i++) {
					reqInfo = (RequirementInfo) reqInfoList.get(i);
					if (reqInfo != null && !NbaUtils.isRequirementFulfilled(String.valueOf(reqInfo.getReqStatus()))) {
						reqInfo.setRequirementDetails(reqDetails);
						reqInfo.setActionUpdate();
					}
				}
			}			
		} catch (RemoteException t) {//APSL1685 Begin
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException t) {
					getLogger().logError(t);
				}
			}
		}//APSL1685 End
	} 

	//New Method APSL3992
	protected void voidPreviousDeliveryInstructions(NbaTXLife txLife) throws Exception {
		ArrayList attList = txLife.getPrimaryHolding().getAttachment();
		Attachment attach = null;
		if ( attList !=null ){
			for ( int i = 0; i< attList.size(); i ++){
				attach = (Attachment) attList.get(i);
				if ( NbaOliConstants.OLI_ATTACH_INSTRUCTION == attach.getAttachmentType() && 
						NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER.equalsIgnoreCase(attach.getUserCode())) {
					AttachmentData attchData = attach.getAttachmentData();
					SpecialInstruction spclInst = SpecialInstruction.unmarshal(new ByteArrayInputStream(attchData.getPCDATA().getBytes()));
					spclInst.setVoidInd(String.valueOf(NbaConstants.TRUE));
					spclInst.setUserNameVoided(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER);
					spclInst.setDateVoided(NbaUtils.getStringFromDate(new java.sql.Date(System.currentTimeMillis())));
					spclInst.setUserVoided(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER);
					attchData.setPCDATA(NbaUtils.commentDataToXmlString(spclInst));
					attchData.setActionUpdate();
					attach.setActionUpdate();
				}
			}
		}
	}

	// NBLXA-188(APSL5318) Legacy Decommissioning
	/**
	 * Set Insured Details in PDR for Employer Owned GI Case 
	 * @throws Exception
	 */
	protected void setPolicyDeliveryRequirementDetails(NbaTXLife nbaTxLife) throws NbaBaseException {
		NbaOLifEId olifeId = new NbaOLifEId(nbaTxLife);
		List reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(), OLI_REQCODE_POLDELRECEIPT);
		ApplicationInfoExtension appInfoExtension =  NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPolicy().getApplicationInfo());
		if (reqInfoList != null) {
			int count = reqInfoList.size();
			RequirementInfo reqInfo = null;
			for (int i = 0; i < count; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null/* && !NbaUtils.isRequirementFulfilled(String.valueOf(reqInfo.getReqStatus()))*/) {
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					ArrayList<AxaGIAppSystemDataVO> policyList = null;// acc getALL policy for that batch
					PolicyExtension polExtension = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
					if (null != polExtension && polExtension.getPrintTogetherIND()) {
						String giBatchID = polExtension.getGIBatchID();
						policyList = NbaSystemDataDatabaseAccessor.getListOfPoliciesForRelevantBatchID(giBatchID);
						int policyListCount = policyList.size();
						for (int j = 0; j < policyListCount; j++) {
							if(null != appInfoExtension){
								boolean isEmployerOwned = (appInfoExtension.getOwnerTypeCode() == 1);
								boolean isEmployeeOwned = (appInfoExtension.getOwnerTypeCode() == 2);
								if( (isEmployerOwned && polExtension.getPdrInd()) || (isEmployeeOwned && polExtension.getPdrInd() && polExtension.getMDRConsentIND())){
									String insuredName = policyList.get(j).getLastName() + ", " + policyList.get(j).getFirstName();
									String policyNo = policyList.get(j).getPolicynumber();
									String ownerName = policyList.get(j).getOwnFullName();
									GIAPPBatchPolicy giAPPBatchPolicy = new GIAPPBatchPolicy();
									olifeId.setId(giAPPBatchPolicy);
									if (!NbaUtils.isBlankOrNull(ownerName)) {
										giAPPBatchPolicy.setOwnerName(ownerName);
									} else {
										giAPPBatchPolicy.setOwnerName(insuredName);
									}
									giAPPBatchPolicy.setInsuredName(insuredName);
									giAPPBatchPolicy.setPolicynumber(policyNo);
									reqInfoExt.addGIAPPBatchPolicy(giAPPBatchPolicy);
								}

							}

						}
						if(null != reqInfoExt){
							reqInfoExt.setActionUpdate();
						}
						reqInfo.setActionUpdate();
					}
				}

			}
		}
	}

	//New Method APSL5100
	protected void constructEpolicyData(NbaTXLife txLife, NbaDst nbaDst) {
		EPolicyData ePolicyData = new EPolicyData();
		NbaOLifEId olifeId = new NbaOLifEId(txLife);
		ePolicyData.setPrintCRDA(nbaDst.getID());
		ePolicyData.setActive(true);
		ePolicyData.setPreviewRequestedTime(new NbaTime(new Date()));//NBLXA-2215
		ePolicyData.setActionAdd();
		olifeId.setId(ePolicyData);
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(txLife.getPolicy());
		policyExtension.addEPolicyData(ePolicyData);
		policyExtension.setActionUpdate();
	}

	// New Method APSL5100
	protected void updateOldEpolicyData(NbaTXLife txLife) {
		EPolicyData ePolicyData = null;
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(txLife.getPolicy());
		if (policyExtension != null && policyExtension.getEPolicyDataCount() > 0) {
			for (int i = 0, j = policyExtension.getEPolicyDataCount(); i < j; i++) {
				ePolicyData = policyExtension.getEPolicyDataAt(i);
				if (ePolicyData != null && ePolicyData.getActive()) {
					ePolicyData.setActive(false);
					ePolicyData.setActionUpdate();
				}

			}
		}
	}

	// NBLXA-1552: BEGIN
	private void generateSpecialInstructionForGIApplication(NbaTXLife nbaTxLife) throws NbaBaseException {
		NbaSpecialInstructionComment nsic = new NbaSpecialInstructionComment();
		nsic.setActionAdd();
		nsic.setOriginator(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER);
		nsic.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		nsic.setText(NbaConstants.GI_AUTOMATED_DELIVERY_INSTRUCTION);
		getPolicyExtension(nbaTxLife.getPolicy()).setPolicyDeliveryInstructionAmt(NbaVPMSHelper.getPremiumDue(nbaTxLife));
		getPolicyExtension(nbaTxLife.getPolicy()).setPdiscreateDate(new Date());
		nsic.setUserNameEntered(NbaConstants.AUTOMATED_DELIVERY_INST_PRINT_USER);
		nsic.setVoidInd(NbaConstants.FALSE);
		nsic.setInstructionType(NbaConstants.INSTRUCTION_TYPE);
		NbaUtils.addAttachmentForSpecialInstruction(nbaTxLife.getPrimaryHolding(), nsic);
	}

	private boolean isCWAApplied(NbaTXLife nbaTxLife){
		Policy policy = nbaTxLife.getPolicy();
		if(policy!=null && policy.getApplicationInfo()!=null ) {
			return policy.getApplicationInfo().getCWAAmt() > 0.0;
		}
		return false;
	}
	// NBLXA-1552: END
	
	//NBLXA-2445 New Method
		private boolean isMatchWithTheSource(NbaSource nbaSource, String currFormNo, String reqType){
			boolean isMatch = false;
			int count = getAttachmentTypesForReport().getContractPrintAttachmentCount();
			for (int j = 0; j < count; j++) {
				contractPrintAttachment = getAttachmentTypesForReport().getContractPrintAttachmentAt(j);
				String formNumberOrReqCode = contractPrintAttachment.getSourceFormTypeOrReqCode();
				String sourceType = contractPrintAttachment.getSourceType();
				if (isSpecificSourceType(nbaSource, sourceType, reqType, formNumberOrReqCode, currFormNo)) {
					isMatch = true;
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Save PIND LOB for Requirement source == " + formNumberOrReqCode);
					}
					break;
				}
			}
			return isMatch;
		}
}
