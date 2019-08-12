package com.csc.fsg.nba.provideradapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.xml.sax.SAXParseException;

import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LabTestRemark;
import com.csc.fsg.nba.vo.txlife.LabTestResult;
import com.csc.fsg.nba.vo.txlife.LabTesting;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.tbf.xml.XmlValidationError;

/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

/**
 * Adapter for CRL response processing.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ALS5223</td><td>AXA Life Phase 1</td><td>QC # 4390  - 3.7.31 Exam One - Additional Lab Test requirement not processed correctly</td></tr>
 * * <tr><td>SPRNBA-597</td><td>Version NB-1301</td><td> Image data should not be stored in Attachments</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class AxaEibExamOneAdapter extends AxaEibProviderAdapter {

	private boolean isAdditionalTest = false;

	
	/**
	 * This method converts the Provider's response into XML transaction.It 
	 * also updates required LOBs and result source with converted XMLife.
	 * @param work the requirement work item.
	 * @return the requirement work item with formated source.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException, NbaDataException {
		String response = getDataFromSource(work);
		
		ArrayList aList = new ArrayList();
		try {
			NbaTXLife nbaTxLife = new NbaTXLife(response);
			Vector vctrErrors = nbaTxLife.getTXLife().getValidationErrors(false);
			if (vctrErrors != null && vctrErrors.size() > 0) {
				int count = vctrErrors.size();
				StringBuffer errorString = new StringBuffer();
				for( int ndx = 0; ndx < count; ndx++) {
					XmlValidationError error = (XmlValidationError)vctrErrors.get(ndx);
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
			
			//Start US-283120
			Holding holding = nbaTxLife.getPrimaryHolding();
			List systemMessageList = new ArrayList();
			String msgCodeText = "";
			List<String> lnrcStatusCodesList = Arrays.asList(NbaConstants.LNRC_STATUS_CODES);
			
			StringBuilder errorString = new StringBuilder("Lexis Nexis error returned - ");
			boolean throwException = false;
			if(holding!=null) {
				systemMessageList = holding.getSystemMessage();
				for(int i=0;i<systemMessageList.size();i++) {
					SystemMessage sysMsg = (SystemMessage) systemMessageList.get(i);
					if(sysMsg.getCarrierAdminSystem().equalsIgnoreCase(NbaConstants.PROVIDER_LEXISNEXIS)) {
						msgCodeText = NbaUtils.getFirstSystemMessageExtension(sysMsg).getMsgCodeText();
						if (!((msgCodeText != null && lnrcStatusCodesList.contains(msgCodeText))
								|| (NbaUtils.isEmpty(msgCodeText) && isMVRMsg(sysMsg.getMessageDescription())))) {
							errorString = errorString.append(sysMsg.getMessageDescription());
							throwException = true;
						}
					}
				}
			}
			if(throwException) {
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, errorString.toString());
			}
			//End US-283120
			
			
			setAdditionalTest(nbaTxLife);
			//begin SPRNBA-597
			List requirementInfoImagesList = stripImagesFromAttachments(policy); // Get a List of images from the <Attachments> and removed the image bytes from the 1122
			response = nbaTxLife.toXmlString(); // Save the 1122 with images removed.
			getProviderSupplementSource().setText(response);  //Update the original NBPROVSUPP Source 
			getProviderSupplementSource().setUpdate();
			//end SPRNBA-597
			NbaLob workLob = work.getNbaLob();
			
			if (!policy.hasPolNumber() || policy.getPolNumber().indexOf(NbaAutomatedProcess.CONTRACT_DELIMITER) == -1) {
				workLob.setCompany(policy.getCarrierCode());
				if( nbaTxLife.getOLifE().getSourceInfo() != null) {
					workLob.setBackendSystem(nbaTxLife.getOLifE().getSourceInfo().getFileControlID());				
				}
				workLob.setPolicyNumber(policy.getPolNumber());			
			} else {
				NbaStringTokenizer tokens = new NbaStringTokenizer(policy.getPolNumber(), NbaAutomatedProcess.CONTRACT_DELIMITER);
				if( tokens.hasMoreTokens()) {
					workLob.setCompany(tokens.nextToken());
					workLob.setBackendSystem(tokens.nextToken());
					workLob.setPolicyNumber(tokens.nextToken());
				}
			}
			workLob.setReqReceiptDate(new Date());
			workLob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
			RequirementInfo reqInfo = nbaTxLife.getPolicy().getRequirementInfoAt(0); //ALII2006
			if(reqInfo != null && (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED ||
					reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD ||
					reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_803)){ //ALII2006
				workLob.setParamedSignDate(getParamedSignDate(nbaTxLife, workLob, nbaTxLife.getPolicy().getRequirementInfoAt(0), user)); //ALII2006
			}
			if (isAdditionalTest) {
				updateTxLife(work,nbaTxLife);
			}	
			work = updateWorkItem(work, nbaTxLife.getPolicy().getRequirementInfoAt(0), (List) requirementInfoImagesList.get(0), nbaTxLife); //SPRNBA-597
		
			aList.add(work);
			int count = policy.getRequirementInfoCount();
			if (!isAdditionalTest) {
				for (int i = 1; i < count; i++) {
					// create transaction
					NbaDst tempTrans = createTransaction(user, work);
					NbaLob tempLob = tempTrans.getNbaLob();
					tempLob.setPolicyNumber(workLob.getPolicyNumber());
					tempLob.setBackendSystem(workLob.getBackendSystem());
					tempLob.setCompany(workLob.getCompany());
					tempLob.setReqVendor(workLob.getReqVendor());
					tempLob.setReqReceiptDate(workLob.getReqReceiptDate());
					tempLob.setReqReceiptDateTime(workLob.getReqReceiptDateTime());//QC20240
					reqInfo = policy.getRequirementInfoAt(i); //ALII2006
					if(reqInfo != null && (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED ||
							reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD ||
							reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_803)){ //ALII2006
						tempLob.setParamedSignDate(getParamedSignDate(nbaTxLife, tempLob, policy.getRequirementInfoAt(i), user)); //ALII2006
					}
					tempTrans.addNbaSource(new NbaSource(NbaConstants.A_BA_NBA, NbaConstants.A_ST_PROVIDER_SUPPLEMENT, response));
					tempTrans = updateWorkItem(tempTrans, nbaTxLife.getPolicy().getRequirementInfoAt(i), (List) requirementInfoImagesList.get(i), nbaTxLife); //SPRNBA-597
					aList.add(tempTrans);
				}
			}
			return aList;
		} catch (SAXParseException spe) {
			throw new NbaDataException("Provider Validation failed; response invalid.");
		} catch (NbaDataException nde) {
			throw nde;
		}catch (AxaErrorStatusException ex) {
			throw new NbaBaseException(ex);
		} catch (Exception e) {
			throw new NbaBaseException("Provider Validation failed\n" + e.toString(), e);
		}
	}
	/**
	 * @param work
	 * @param nbaTxLife
	 */
	private void updateTxLife(NbaDst work, NbaTXLife nbaTxLife) {
		String reqId = nbaTxLife.getPolicy().getRequirementInfoAt(0).getId();
		NbaParty nbaParty = nbaTxLife.getPrimaryParty();
		nbaTxLife.getPolicy().getRequirementInfoAt(0).setReqCode(NbaOliConstants.OLI_REQCODE_1009800019);
		RiskExtension riskExt = NbaUtils.getFirstRiskExtension(nbaParty.getParty().getRisk());
		if (null != riskExt && riskExt.hasLabTesting()) {
			LabTesting labTesting = riskExt.getLabTesting();
			long remarkCount = labTesting.getLabTestResultCount();
			for (int i = 0;i<remarkCount;i++) {
				LabTestResult ltr = labTesting.getLabTestResultAt(i);
				ltr.setRequirementInfoID(reqId);
				
			}
		}
		NbaSource source = getProviderSupplement(work);
		if (null != source) {
			source.updateText(nbaTxLife.toXmlString());
		}
		
	}
	private void setAdditionalTest(NbaTXLife nbaTxLife) {

		if (nbaTxLife.getPolicy().getRequirementInfoAt(0).getReqCode() == NbaOliConstants.OLI_REQCODE_1009800019) {
			isAdditionalTest = true;
		} else {			
			isAdditionalTest = false;
		}
	}
	
	protected boolean isMVRMsg(String msgDescription) {
		String searchStr = "MVR";
		String subStrs[] = msgDescription.split("(\\s+)");
		for (int i = 0; i < subStrs.length; i++) {
			//check only first three words
			if (i >= 2) {
				return false;
			}
			if (searchStr.equalsIgnoreCase(subStrs[i])) {
				return true;
			}
		}
		return false;
	}
}
