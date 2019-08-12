package com.csc.fsg.nba.provideradapter;

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
 * *******************************************************************************<BR>
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXParseException;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.StatusEvent;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.tbf.xml.XmlValidationError;

/**
 * NbaEibProviderAdapter parses the results received from a provider, updates AWD LOB fields based
 * on those results and add additional sources, as required.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.31</td><td>Axa Life Phase 1</td><td>Provider Interfaces</td></tr>
 * <tr><td>ALS4966</td><td>AXA Life Phase 1</td><td>QC#4129-AXAL03.07.31 Provider Results for "Joint Life #2" error when Requirements are not posted</td></tr>
 * <tr><td>SPRNBA-597</td><td>Version NB-1301</td><td> Image data should not be stored in Attachments</td></tr>
 * <tr><td>APSL5085 </td><td>Discretionary</td><td>Requirement As Data</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapterFacade
 * @since New Business Accelerator - Version 4
 */
public class AxaEibProviderAdapter extends NbaGenericProviderAdapter {

	protected static NbaLogger logger = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.csc.fsg.nba.provideradapter.NbaProviderAdapter#convertXmlToProviderFormat(java.util.List)
	 */
	@Override
	public Map convertXmlToProviderFormat(List aList) throws NbaBaseException {//CR60669 enhanced logging
		String polNo = "";
		HashMap result = new HashMap();
		if (aList.size() == 3) {
			String txLifeString = (String) aList.get(0);
			StringWriter outputStream = new StringWriter();
			try {
				FileInputStream xslFile = new FileInputStream(NbaUtils.loadTransformationXSL((String) aList.get(1), (String) aList.get(2), null));
				if (xslFile == null) {
					throw new NbaBaseException("XSL Stylesheet not found! Please verify the stylesheet path");
				}
				if (getLogger().isDebugEnabled()) {
					String polStartTag = "<PolNumber>";
					String polEndTag = "</PolNumber>";
					if (txLifeString.indexOf(polStartTag) > -1 && txLifeString.indexOf(polEndTag) > -1) {
						polNo = txLifeString.substring(txLifeString.indexOf(polStartTag) + polStartTag.length(), txLifeString.indexOf(polEndTag));
					}
					getLogger().logDebug("TX121 Request for transform having Policy Number : " + polNo + " : " + txLifeString);
				}
				Transformer x = TransformerFactory.newInstance().newTemplates(new StreamSource(xslFile)).newTransformer();
				x.setParameter("CurrentDate", NbaUtils.getCurrentDateForXSL()); // Code Formatted
				BufferedReader reader = new BufferedReader(new StringReader(txLifeString.trim()));
				Source source = new StreamSource(reader);
				Result target = new javax.xml.transform.stream.StreamResult(outputStream);
				x.transform(source, target);
				reader.close(); //ALII959
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			result.put(NbaConstants.TRANSACTION, outputStream.toString());
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("TX121 Request after transform having Policy Number : " + polNo + " : " + outputStream.toString());
			}
		} else {
			if (aList.get(0) instanceof String) {
				result.put(NbaConstants.TRANSACTION, aList.get(0));
			} else if (aList.get(0) instanceof NbaTXLife) {
				NbaTXLife txLife = (NbaTXLife) aList.get(0);
				result.put(NbaConstants.TRANSACTION, txLife.toXmlString());
			}
		}
		return result;
	}

	/**
	 * This method converts the Provider's response into XML transaction.It also updates required LOBs and result source with converted XMLife.
	 *
	 * @param work
	 *                the requirement work item.
	 * @return the requirement work item with formated source.
	 * @exception NbaBaseException
	 *                     thrown if an error occurs.
	 */
	@Override
	public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException, NbaDataException {
		String response = getDataFromSource(work);
		ArrayList aList = new ArrayList();
		try {
			NbaTXLife nbaTxLife = new NbaTXLife(response);
			Vector vctrErrors = nbaTxLife.getTXLife().getValidationErrors(false);
			if (vctrErrors != null && vctrErrors.size() > 0) {
				int count = vctrErrors.size();
				StringBuffer errorString = new StringBuffer();
				for (int ndx = 0; ndx < count; ndx++) {
					XmlValidationError error = (XmlValidationError) vctrErrors.get(ndx);
					if (error != null) {
						errorString.append("Error(" + ndx + "): " + error.getErrorMessage() + "\n");
					} else {
						errorString.append("A problem occurred retrieving the validation error.");
					}
				}
				throw new NbaDataException("Provider Validation failed; response invalid.\nValidation Error(s):\n" + errorString.toString()); //SPR2580
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Process provider response: " + nbaTxLife.toXmlString());
			}
			Policy policy = nbaTxLife.getPolicy();
			if (policy == null || policy.getRequirementInfoCount() == 0) {
				throw new NbaDataException("Provider response requirement info is missing or invalid");
			}
			//begin SPRNBA-597
			List requirementInfoImagesList = stripImagesFromAttachments(policy); // Get a List of images from the <Attachments> and removed the image bytes from the 1122
			response = nbaTxLife.toXmlString(); // Save the 1122 with images removed.
			getProviderSupplementSource().setText(response);  //Update the original NBPROVSUPP Source
			getProviderSupplementSource().setUpdate();
			//end SPRNBA-597
			NbaLob workLob = work.getNbaLob();
			if (!policy.hasPolNumber() || policy.getPolNumber().indexOf(NbaAutomatedProcess.CONTRACT_DELIMITER) == -1) {
				workLob.setCompany(policy.getCarrierCode());
				if (nbaTxLife.getOLifE().getSourceInfo() != null) {
					workLob.setBackendSystem(nbaTxLife.getOLifE().getSourceInfo().getFileControlID());
				}
				workLob.setPolicyNumber(policy.getPolNumber());
			} else {
				NbaStringTokenizer tokens = new NbaStringTokenizer(policy.getPolNumber(), NbaAutomatedProcess.CONTRACT_DELIMITER);
				if (tokens.hasMoreTokens()) {
					workLob.setCompany(tokens.nextToken());
					workLob.setBackendSystem(tokens.nextToken());
					workLob.setPolicyNumber(tokens.nextToken());
				}
			}
			workLob.setReqReceiptDate(new Date());
			workLob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
			//APLS3011 CRL CR Code moved to updateWorkItem method
			work = updateWorkItem(work, nbaTxLife.getPolicy().getRequirementInfoAt(0), (List) requirementInfoImagesList.get(0), nbaTxLife); //SPRNBA-597
			aList.add(work);
			int count = policy.getRequirementInfoCount();
			for (int i = 1; i < count; i++) {
				/*if (AxaUtils.isBlankTransaction(policy.getRequirementInfoAt(i), nbaTxLife)) {
					if (getLogger().isDebugEnabled())
						getLogger().logDebug(policy.getRequirementInfoAt(i).getId().concat(" is a blank transaction and will be ignored."));
					continue;
				}*/
				// create transaction
				NbaDst tempTrans = createTransaction(user, work);
				NbaLob tempLob = tempTrans.getNbaLob();
				tempLob.setPolicyNumber(workLob.getPolicyNumber());
				tempLob.setBackendSystem(workLob.getBackendSystem());

				tempLob.setCompany(workLob.getCompany());
				tempLob.setReqVendor(workLob.getReqVendor());
				tempLob.setReqReceiptDate(workLob.getReqReceiptDate());
				tempLob.setReqReceiptDateTime(workLob.getReqReceiptDateTime());//QC20240
				//APLS3011 CRL CR Code moved to updateWorkItem method
				tempTrans.addNbaSource(new NbaSource(NbaConstants.A_BA_NBA, NbaConstants.A_ST_PROVIDER_SUPPLEMENT, response));
				tempTrans = updateWorkItem(tempTrans, nbaTxLife.getPolicy().getRequirementInfoAt(i), (List) requirementInfoImagesList.get(i), nbaTxLife); //SPRNBA-597
				aList.add(tempTrans);
			}
			return aList;
		} catch (SAXParseException spe) {
			throw new NbaDataException("Provider Validation failed; response invalid.");
		} catch (NbaDataException nde) {
			throw nde;
		} catch (Exception e) {
			throw new NbaBaseException("Provider Validation failed\n" + e.toString(), e);
		}
	}

	protected Date getParamedSignDate(NbaTXLife txLife, NbaLob lob, RequirementInfo reqInfo, NbaUserVO user) throws NbaBaseException {
		if (txLife != null && txLife.getOLifE() != null) {
			HashMap skipMap = new HashMap();
			//APSL3011 Code deleted
			skipMap.put("A_ReqTypeLOB", String.valueOf(reqInfo.getReqCode()));
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(lob);
			oinkData.setContractSource(txLife);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(reqInfo.getId());
			NbaVpmsAdaptor vpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
			vpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_COMPLETION_DATE_LOCATION);
			vpmsAdaptor.setANbaOinkRequest(oinkRequest);
			vpmsAdaptor.setSkipAttributesMap(skipMap);
			try {
				VpmsComputeResult computeResult = vpmsAdaptor.getResults();
				NbaVpmsModelResult resultsData = new NbaVpmsModelResult(computeResult.getResult());
				String location = null;
				ResultData resultData;
				for (int i = 0; i < resultsData.getVpmsModelResult().getResultDataCount(); i++) {
					resultData = resultsData.getVpmsModelResult().getResultDataAt(i);
					for (int j = 0; j < resultData.getResultCount(); j++) {
						location = resultData.getResultAt(j);
						if (location.equals("RequirementInfo.RequestedScheduleDate")) {
							if (reqInfo.hasRequestedScheduleDate()) {
								return reqInfo.getRequestedScheduleDate();
							}
						} else if (location.equals("RequirementInfo.FulfilledDate")) {//APLS3011 CRL CR
							if (reqInfo.hasFulfilledDate()) {
								return reqInfo.getFulfilledDate();
							}
						} else if (location.equals("RequirementInfo.StatusEvent.StatusEventDate")) {
							StatusEvent event = null;
							for (int k = 0; k < reqInfo.getStatusEventCount(); k++) {
								event = reqInfo.getStatusEventAt(k);
								if (event.getStatusEventCode() == NbaOliConstants.OLI_STATEVENTCD_COMPLETEASSCHED && event.hasStatusEventDate()) {
									return event.getStatusEventDate();
								}
							}
						} else if (location.equals("Party.Risk.MedicalExam.ExamDate")) {
							Party party = txLife.getPrimaryParty().getParty();
							MedicalExam medExam = null;
							for (int k = 0; party.hasRisk() && k < party.getRisk().getMedicalExamCount(); k++) {
								medExam = party.getRisk().getMedicalExamAt(k);
								if (medExam.hasExamDate()) {
									return medExam.getExamDate();
								}
							}
						} else if (location.equals("Party.Risk.LabTesting.TestUpdateDate")) {
							Party party = txLife.getPrimaryParty().getParty();
							if (party.hasRisk()) {
								RiskExtension riskExt = NbaUtils.getFirstRiskExtension(party.getRisk());
								if (riskExt != null && riskExt.hasLabTesting() && riskExt.getLabTesting().hasTestUpdateDate()) {
									return riskExt.getLabTesting().getTestUpdateDate();
								}
							}
						}
					}
				}
			} catch (java.rmi.RemoteException re) {
				throw new NbaBaseException("VPMS problem", re);
			} finally {
				if (vpmsAdaptor != null) {
					try {
						vpmsAdaptor.remove();
					} catch (RemoteException e) {
						getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
					}
				}
			}
		}
		return new Date();//APSL3011 nbA needs to have logic to default to current date if date is missing in these tags
	}

	/**
	 * This method updates NbaDst work item LOB fields and adds sources found withing the Attachment object(s).
	 *
	 * @param work
	 *                an NbaDst temporary requirement work item to be updated/indexed
	 * @param reqInfo
	 *                a RequirementInfo object from the provider results that may contain Attachment objects
	 * @param nbaTxLife
	 *                the XMLife provider result message
	 */
	//SPRNBA-597 added images parameter
	@Override
	protected NbaDst updateWorkItem(NbaDst work, RequirementInfo reqInfo, List images, NbaTXLife nbaTxLife) throws NbaBaseException {
		Party party = getParty(reqInfo, nbaTxLife);
		NbaLob lob = work.getNbaLob();
		boolean sourceCreated = false; //APSL5085
		long transType = 0;
		try {
			if (nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest() != null) {
				transType = nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType();
				if (transType == NbaOliConstants.TC_TYPE_GENREQUIRERESTRN
						|| transType == NbaOliConstants.TC_TYPE_MIBINQUIRY
						|| transType == NbaOliConstants.TC_TYPE_MIBUPDATE
						|| transType == NbaOliConstants.TC_TYPE_ADD_UPDATE_MESSAGE) /*Added the change as a part of NBLXA-1822*/ {
					lob.setReqUniqueID(getValue(reqInfo.getRequirementInfoUniqueID()));
				} else {
					lob.setReqUniqueID(nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo().getTrackingID());
				}
			} else if (nbaTxLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify() != null) {
				transType = nbaTxLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransType();
				if (transType == NbaOliConstants.TC_TYPE_GENREQUIRERESTRN
						|| transType == NbaOliConstants.TC_TYPE_MIBINQUIRY
						|| transType == NbaOliConstants.TC_TYPE_MIBUPDATE) {
					lob.setReqUniqueID(getValue(reqInfo.getRequirementInfoUniqueID()));
				} else {
					lob.setReqUniqueID(nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo().getTrackingID());
				}
			}
		} catch (Exception e) {
			lob.setReqUniqueID(NbaAutomatedProcess.LOB_NOT_AVAILABLE);
		}
		if (!(transType == NbaOliConstants.TC_TYPE_MIBINQUIRY || transType == NbaOliConstants.TC_TYPE_MIBUPDATE)) {
			Person person = party.getPersonOrOrganization() != null ? party.getPersonOrOrganization().getPerson() : null;
			if (person != null) {
				lob.setLastName(getValue(person.getLastName()));
				lob.setJointLastName(getValue(person.getLastName())); //ALS4966
				lob.setFirstName(getValue(person.getFirstName()));
				lob.setJointFirstName(getValue(person.getFirstName())); //ALS4966
				lob.setMiddleInitial(getValue(person.getMiddleName()));
				if (person.hasBirthDate()) {
					lob.setDOB(person.getBirthDate());
					lob.setJointDOB(person.getBirthDate()); //ALS4966
				}
				if (person.hasGender()) {
					lob.setGender(getValue(person.getGender()));
				}
			}
			lob.setSsnTin(getValue(party.getGovtID()));
			lob.setJointSsnTin(getValue(party.getGovtID())); //ALS4966
			lob.setReqVendor(getValue(lob.getReqVendor()));

			// Begin NBLXA-2493
			if (nbaTxLife.getWritingAgent() != null) {
				NbaParty writingParty = nbaTxLife.getWritingAgent();
				if (writingParty.isPerson()) {
					if (writingParty.getPerson().hasFirstName()) {
						lob.setReqAgentFirstName(writingParty.getPerson().getFirstName());
					}
					if (writingParty.getPerson().hasLastName()) {
						lob.setReqAgentLastName(writingParty.getPerson().getLastName());
					}
				}
				if (!NbaUtils.isBlankOrNull(writingParty.getWritingAgentId())) {
					lob.setAgentID(writingParty.getWritingAgentId());
				}
			}
			// End NBLXA-2493
			lob.setReqType((new Long(reqInfo.getReqCode())).intValue());
			lob.setReqDrName(getDoctorName(reqInfo, nbaTxLife));
			lob.setProviderOrder(reqInfo.getProviderOrderNum()); //ALII1818
			lob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED)); //ALS4961
			//NBLXA-1656 Starts
			//if (!NbaUtils.isBlankOrNull(reqInfo.getReqCode()) && reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PHYSSTMT && //NBLXA-1777
			if (!NbaUtils.isBlankOrNull(nbaTxLife.getUserAuthRequest()) && !NbaUtils.isBlankOrNull(nbaTxLife.getTransSubType()) //NBLXA-1777
					&& nbaTxLife.getTransSubType() == NbaOliConstants.OLI_TRANSSUB_INQREQUIREMENT) // QC-19653

			{
				lob.setReqSubStatus(NbaConstants.SUB_STATUS);
			}
			//NBLXA-1656 Ends

			// NBLXA-1822 Starts
			if ((!NbaUtils.isBlankOrNull(nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest()))
					&& (nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType() == NbaOliConstants.TC_TYPE_ADD_UPDATE_MESSAGE))
			{
				lob.setReqSubStatus(NbaConstants.REQ_AGENT_CODE);
			}
			// NBLXA-1822 Ends

			if (NbaConstants.PROVIDER_CRL.equalsIgnoreCase(lob.getReqVendor())
					|| NbaConstants.PROVIDER_APPS.equalsIgnoreCase(lob.getReqVendor())) {//APSL2468
				String vendorCode = nbaTxLife.getUserAuthRequest().getVendorApp().getVendorName().getVendorCode();
				String reqCode = new Long(reqInfo.getReqCode()).toString();
				NbaVpmsResultsData vpmsResultsData = getDataFromVpms(vendorCode, reqCode);
				if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
					String providerID = (String) vpmsResultsData.getResultsData().get(0);
					lob.setReqVendor(providerID);
				}
			}
		}
		if(lob.getReqType() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED ||
				lob.getReqType() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD ||
				lob.getReqType() == NbaOliConstants.OLI_REQCODE_803){ //ALII2006
			lob.setParamedSignDate(getParamedSignDate(nbaTxLife, lob, reqInfo, null)); //APSL3011 sends updated provider to VPMS
			//APSL5085:Start
			if(NbaConstants.TRUE_STR.equalsIgnoreCase(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.TIFF_IMAGE_SWITCH))){
				addSourcesForImages(work, images, true); //SPRNBA-597
				sourceCreated = true;
			}
			else{
				addSourcesForImages(work, images, false);
			}
			//APSL5085:End
		}
//		APSL5085 : Start
		if(lob.getReqType() == NbaOliConstants.OLI_REQCODE_1009800030){
			lob.setFormNumber(reqInfo.getFormNo());
		}
//		APSL5085 : End
		if(!sourceCreated){//APSL5085
			addSourcesForImages(work, images, false); //SPRNBA-597 // APSL5085
		}
		// NBLXA-1794 Begin
		if (lob.getReqType() == NbaOliConstants.OLI_REQCODE_BLOOD || lob.getReqType() == NbaOliConstants.OLI_REQCODE_URINE
				|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_VITALS
				|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_496
				|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_498
				|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_499
				) {
			Date examDate = getExamDate(nbaTxLife);
			if (examDate != null) {
				lob.setLabCollectionDate(examDate);
			}
		}
		// NBLXA-1794 End

		setSourceLobs(work.getNbaSources(), work.getNbaLob());//ALS5057
		return work;
	}

	/**
	 * This method extracts AttachmentData and adds it as a Source to the NbaDst work item. The type of source added, Image or Data, is determined by
	 * the AttachmentBasicType within the Attachment object.
	 *
	 * @param work
	 *                an NbaDst work item to which the new source will be added
	 * @param anAttachment
	 *                the Attachment that contains the data which will populate the source
	 */
	@Override
	protected void addNewSource(NbaDst work, Attachment anAttachment) throws NbaBaseException {
		if (!anAttachment.hasAttachmentData() || !anAttachment.getAttachmentData().hasPCDATA()) {
			return;
		}
		String info = "AttachmentData removed and placed in AWD source";
		if (anAttachment.getAttachmentBasicType() == NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE) {
			byte[] data;
			if (anAttachment.getAttachmentLocation() == NbaOliConstants.OLI_INLINE) { // data is included in attachment
				data = NbaBase64.decode(anAttachment.getAttachmentData().getPCDATA());
			} else {
				data = getImageFromExternalFile(anAttachment.getAttachmentData().getPCDATA());
			}
			if (data != null) {
				anAttachment.getAttachmentData().setPCDATA(info);
				work.addImageSource(work.getNbaTransaction(), A_ST_PROVIDER_RESULT, data);
			}
		} else {
			String data;
			if (anAttachment.getAttachmentLocation() == NbaOliConstants.OLI_INLINE) {
				data = anAttachment.getAttachmentData().getPCDATA();
			} else {
				data = String.valueOf(getDataFromExternalFile(anAttachment.getAttachmentData().getPCDATA()));
			}
			if (data != null) {
				anAttachment.getAttachmentData().setPCDATA(info);
				work.addNbaSource(new NbaSource(A_BA_NBA, A_ST_PROVIDER_RESULT, data));
			}
		}
	}

	/**
	 * @param sources
	 * @throws NbaBaseException
	 */
	//ALS5057 New Method
	private void setSourceLobs(List sources, NbaLob workItemLob) throws NbaBaseException {
		if (sources != null && sources.size() > 0) {
			for (Iterator iter = sources.iterator(); iter.hasNext();) {
				NbaSource src = (NbaSource) iter.next();
				NbaLob srcLob = src.getNbaLob();
				srcLob.setFirstName(workItemLob.getFirstName());
				srcLob.setJointFirstName(workItemLob.getJointFirstName());
				srcLob.setMiddleInitial(workItemLob.getMiddleInitial());
				srcLob.setLastName(workItemLob.getLastName());
				srcLob.setJointLastName(workItemLob.getJointLastName());
				srcLob.setSsnTin(workItemLob.getSsnTin());
				srcLob.setJointSsnTin(workItemLob.getJointSsnTin());
				srcLob.setCompany(workItemLob.getCompany());
				srcLob.setGender(workItemLob.getGender());
				srcLob.setReqVendor(workItemLob.getReqVendor());
				srcLob.setReqType(workItemLob.getReqType());
				srcLob.setDOB(workItemLob.getDOB());
				srcLob.setJointDOB(workItemLob.getJointDOB());
				srcLob.setReqDrName(workItemLob.getReqDrName());
				srcLob.setReqStatus(workItemLob.getReqStatus());
				srcLob.setPolicyNumber(workItemLob.getPolicyNumber());
				srcLob.setPlan(workItemLob.getPlan());
				srcLob.setPlanType(workItemLob.getPlanType());
				srcLob.setProductTypSubtyp(workItemLob.getProductTypSubtyp());
				srcLob.setFormNumber(workItemLob.getFormNumber());
				srcLob.setBackendSystem(workItemLob.getBackendSystem());
				srcLob.setTaxIdType(workItemLob.getTaxIdType());
				if(workItemLob.getPageRange() != null){// APSL5085
					srcLob.setPageRange(workItemLob.getPageRange());//APSL5085
				}
				if(workItemLob.getLabCollectionDate() != null){// NBLXA-1794
					srcLob.setLabCollectionDate(workItemLob.getLabCollectionDate());//NBLXA-1794
				}
			}
		}
	}
	//APSL2468 CRLCR New Method
	protected NbaVpmsResultsData getDataFromVpms(String vendorCode, String reqCode) throws NbaBaseException {
		NbaVpmsAdaptor vpmsAdaptor = null;
		HashMap skipMap = new HashMap();
		skipMap.put("A_XmlVendorCode", vendorCode);
		skipMap.put("A_RequirementCode", reqCode);
		try {
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(new NbaLob());
			vpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
			vpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_PROVIDER_FOR_RESULT);
			vpmsAdaptor.setANbaOinkRequest(oinkRequest);
			vpmsAdaptor.setSkipAttributesMap(skipMap);
			return new NbaVpmsResultsData(vpmsAdaptor.getResults());
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Requirement provider problem", re);
		} finally {
			try {
				if (vpmsAdaptor != null) {
					vpmsAdaptor.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
	}
	// NBLXA-1794 Begin
	protected Date getExamDate(NbaTXLife txLife) {
		if (txLife != null && txLife.getOLifE() != null) {
			NbaParty nbaParty = txLife.getPrimaryParty();
			if (nbaParty != null) {
				Party party = nbaParty.getParty();
				MedicalExam medExam = null;
				for (int k = 0; party != null && party.hasRisk() && k < party.getRisk().getMedicalExamCount(); k++) {
					medExam = party.getRisk().getMedicalExamAt(k);
					if (medExam != null && medExam.hasExamDate()) {
						return medExam.getExamDate();
					}
				}
			}
		}
		return null;
	}
	// NBLXA-1794 - End
	@Override
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(AxaEibProviderAdapter.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("AxaEibProviderAdapter could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

}