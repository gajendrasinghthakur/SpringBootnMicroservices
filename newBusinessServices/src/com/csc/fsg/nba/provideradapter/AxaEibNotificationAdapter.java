package com.csc.fsg.nba.provideradapter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.xml.sax.SAXParseException;

import com.csc.fsg.nba.bean.accessors.NbaContractPrintFacadeBean;
import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.tbf.xml.XmlValidationError;

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

/**
 * Adapter for Notification System PDR response processing.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>APSL2808</td>
 * <td>AXA Simplified Issue Project</td>
 * <td></td>
 * </tr>* </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class AxaEibNotificationAdapter extends AxaEibProviderAdapter {

	protected static final String CASE = "C";

	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO"; //SPR2697

	NbaDst work = null;

	NbaUserVO user = null;

	/**
	 * This method converts the Provider's response into XML transaction.It also updates required LOBs and result source with converted XMLife.
	 * 
	 * @param work
	 *            the requirement work item.
	 * @return the requirement work item with formated source.
	 * @exception NbaBaseException
	 *                thrown if an error occurs.
	 */
	public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException, NbaDataException {

		String response = getDataFromSource(work);
		setUser(user);
		setWork(work);
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
				throw new NbaDataException("Provider Validation failed; response invalid.\nValidation Error(s):\n" + errorString.toString()); // SPR2580
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Process provider response: " + nbaTxLife.toXmlString());
			}

			Policy policy = nbaTxLife.getPolicy();
			// SR787006-APSL3702 Begin
			if (policy == null) { 
				throw new NbaDataException("Provider response requirement info is missing or invalid");
			}
			long transSubType = nbaTxLife.getTransSubType();
			if (transSubType == NbaOliConstants.TC_SUBTYPE_EMAILCHANGE_REQUEST) {
				createMiscWork(work, user, policy.getPolNumber(), transSubType);
				aList.add(work);
			} else if (transSubType == NbaOliConstants.TC_SUBTYPE_PAPERDELIVERY_REQUEST) {
				createPrintWork(work, user, policy.getPolNumber());
				aList.add(work);
			} else if (transSubType == NbaOliConstants.TC_SUBTYPE_FPLOCALPRINT_REQUEST) {  //NBLXA-2019 START
				addPrintComment(work, user, policy.getPolNumber(),transSubType);
				aList.add(work);
			} else if (transSubType == NbaOliConstants.TC_SUBTYPE_AXAPRINTMAIL_REQUEST) {
				addPrintComment(work, user, policy.getPolNumber(),transSubType);
				aList.add(work); //NBLXA-2019 END
			} else { // SR787006-APSL3702 End
				if (policy.getRequirementInfoCount() == 0) { // SR787006-APSL3702
					throw new NbaDataException("Provider response requirement info is missing or invalid");
				}

				// begin SPRNBA-597
				List requirementInfoImagesList = stripImagesFromAttachments(policy); // Get a List of images from the <Attachments> and removed the image
				// bytes from the 1122
				response = nbaTxLife.toXmlString(); // Save the 1122 with images removed.
				getProviderSupplementSource().setText(response); // Update the original NBPROVSUPP Source
				getProviderSupplementSource().setUpdate();
				// end SPRNBA-597
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
				RequirementInfo reqInfo = nbaTxLife.getPolicy().getRequirementInfoAt(0); // ALII2006
				if (reqInfo != null
						&& (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED
								|| reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD || reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_803)) { // ALII2006
					workLob.setParamedSignDate(getParamedSignDate(nbaTxLife, workLob, nbaTxLife.getPolicy().getRequirementInfoAt(0), user)); // ALII2006
				}
				// APSL3361 Begin
				if (reqInfo != null && reqInfo.hasFormNo()) {
					workLob.setFormNumber(reqInfo.getFormNo());
				}
				RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				if (reqInfoExt != null && reqInfoExt.hasDeliveryReceiptSignDate()) {
					workLob.setDeliveryReceiptSignDate(reqInfoExt.getDeliveryReceiptSignDate());
				}
				// APSL3361 End
				work = updateWorkItem(work, nbaTxLife.getPolicy().getRequirementInfoAt(0), (List) requirementInfoImagesList.get(0), nbaTxLife); // SPRNBA-597

				if ((policy.getRequirementInfoCount() > 0 && policy.getRequirementInfoAt(0).getReqStatus() == NbaOliConstants.OLI_REQSTAT_INERROR)) { // SR787006-APSL3702
					work.getNbaLob().setReqStatus(Long.toString(policy.getRequirementInfoAt(0).getReqStatus()));
					createMiscWork(work, user, policy.getPolNumber(), transSubType); // SR787006-APSL3702
				} 
				aList.add(work);
				// APSL3361 Begin
				int count = policy.getRequirementInfoCount();
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
					reqInfo = policy.getRequirementInfoAt(i); // ALII2006
					if (reqInfo != null
							&& (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED
									|| reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD || reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_803)) { // ALII2006
						tempLob.setParamedSignDate(getParamedSignDate(nbaTxLife, tempLob, policy.getRequirementInfoAt(i), user)); // ALII2006
					}
					if (reqInfo != null && reqInfo.hasFormNo()) {
						tempLob.setFormNumber(reqInfo.getFormNo());
					}
					reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if (reqInfoExt != null && reqInfoExt.hasDeliveryReceiptSignDate()) {
						tempLob.setDeliveryReceiptSignDate(reqInfoExt.getDeliveryReceiptSignDate());
					}
					tempTrans.addNbaSource(new NbaSource(NbaConstants.A_BA_NBA, NbaConstants.A_ST_PROVIDER_SUPPLEMENT, response));
					tempTrans = updateWorkItem(tempTrans, nbaTxLife.getPolicy().getRequirementInfoAt(i), (List) requirementInfoImagesList.get(i),
							nbaTxLife); // SPRNBA-597
					aList.add(tempTrans);
				}
				// APSL3361 End
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

	protected void createMiscWork(NbaDst work, NbaUserVO user, String polNumber, long transSubType) throws NbaBaseException {// SR787006-APSL3702
		NbaDst parentWork = retrieveParentWork(user, polNumber);
		if (parentWork != null) {
			work.getTransaction().setWorkType(A_WT_MISC_WORK);
			parentWork.getNbaCase().addNbaTransaction(work.getTransaction());
			copyLOBs(parentWork, work);
			work.getNbaLob().setCaseManagerQueue(parentWork.getNbaLob().getCaseManagerQueue());
			WorkflowServiceHelper.updateWork(user, parentWork);
			addCommentOnWork(transSubType); // SR787006-APSL3702
			WorkflowServiceHelper.unlockWork(user, parentWork);
			//After Unlock retrieving work again to get the lock.
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(work.getWorkItem().getItemID(), true);
			retOpt.setLockWorkItem();
			work = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
		}

	}

	/**
	 * Retrieve the parent case WARNING: Call this method for transaction work only
	 * 
	 * @return the parent case
	 */
	protected NbaDst retrieveParentWork(NbaUserVO user, String polNumber) throws NbaBaseException {
		NbaDst parent = lookup(user, polNumber, A_WT_APPLICATION); // SR787006-APSL3702
		return parent;
	}

	/**
	 * Create and initialize an <code>NbaSearchVO</code> object to find any matching work items. Call the Requirements VP/MS model to get the
	 * criteria (sets of LOB fields) to be used in the search. Different criteria is applicable depending on whether a Transaction or Case is being
	 * searched for. The sets are iterated over until a successful search is performed. For each search, the LOB values identifed in the set are
	 * copied from the work item to the SearchVo. Then the LOB values are examined to verify that values for all LOBs were present on the work item.
	 * If not, the set is bypassed. For each search, the worktypes to search against are determined by a VPMS model. The worktypes vary based on
	 * whether a Transaction or Case is being searched for. If a successful search is performed, the work item referenced in the NbaSearchResultVO
	 * object are retrieved.
	 * 
	 * @return the search value object containing the results of the search
	 * @throws NbaBaseException
	 */
	// SR787006-APSL3702 Method refactored
	protected NbaDst lookup(NbaUserVO user, String polNumber, String workType) throws NbaBaseException {
		NbaSearchVO searchVO = performSearch(user, polNumber, workType); //Perform searches
		//Retrieve the work items referenced in the NbaSearchResultVO
		NbaDst parentWork = retrieveMatchingCases(searchVO.getSearchResults(), user);
		return parentWork;
	}

	/**
	 * Search for matching work items. Iterate over the criteria (sets of LOB fields) to be used in the search until a successfull search is
	 * performed. For each set, try each work type in the workItemTypes list. For each search, the LOB values identifed in the criteria set are copied
	 * from the work item to the SearchVo. Then the LOB values are examined to verify that values for all LOBs were present on the work item. If not,
	 * the set is bypassed.
	 * 
	 * @return the search value object containing the results of the search
	 * @throws RemoteException
	 * @throws NbaBaseException
	 */
	// SR787006-APSL3702 Method refactored
	protected NbaSearchVO performSearch(NbaUserVO user, String polNumber, String workType) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(CASE_SEARCH_VO);
		searchVO.setWorkType(workType); 
		searchVO.setContractNumber(polNumber);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO); 
		return searchVO;
	}

	/**
	 * This method retrieves the matching cases found in the NbaSearchResultVO object. Each case is retrieved and locked. If unable to retrieve with
	 * lock, this work item will be suspended for a brief period of time due to the auto suspend flag being set.
	 * 
	 * @param searchResults
	 *            the results of the previous AWD lookup
	 * @throws NbaBaseException
	 *             NbaLockException
	 */
	public NbaDst retrieveMatchingCases(List searchResults, NbaUserVO user) throws NbaBaseException {
		NbaDst aWorkItem = null;
		ListIterator results = searchResults.listIterator();
		while (results.hasNext()) {
			NbaSearchResultVO resultVO = (NbaSearchResultVO) results.next();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(resultVO.getWorkItemID(), true);
			retOpt.requestSources();
			retOpt.setLockWorkItem();
			aWorkItem = WorkflowServiceHelper.retrieveWork(user, retOpt);
		}
		return aWorkItem;
	}

	protected void copyLOBs(NbaDst parentWork, NbaDst work) {
		try {
			NbaLob parentWokLob = parentWork.getNbaLob();
			NbaLob wrkLob = work.getNbaLob();
			wrkLob.setCompany(parentWokLob.getCompany());
			wrkLob.setPolicyNumber(parentWokLob.getPolicyNumber());
			wrkLob.setBackendSystem(parentWokLob.getBackendSystem());
			wrkLob.setPlan(parentWokLob.getPlan());
			wrkLob.setOperatingMode(parentWokLob.getOperatingMode());
			wrkLob.setProductTypSubtyp(parentWokLob.getProductTypSubtyp());
			wrkLob.setContractChgType(parentWokLob.getContractChgType());
			wrkLob.setDistChannel(String.valueOf(parentWokLob.getDistChannel()));
			wrkLob.setLastName(parentWokLob.getLastName());
			wrkLob.setFirstName(parentWokLob.getFirstName());
			wrkLob.setMiddleInitial(parentWokLob.getMiddleInitial());
			wrkLob.setDOB(parentWokLob.getDOB());
			wrkLob.setGender(parentWokLob.getGender());
			wrkLob.setSsnTin(parentWokLob.getSsnTin());

		} catch (NbaBaseException nbe) {
			getLogger().logException(nbe);
		}

	}

	// SR787006-APSL3702 Method refactored
	protected void addCommentOnWork(long transSubType) {
		if (transSubType == NbaOliConstants.TC_SUBTYPE_EMAILCHANGE_REQUEST) {
			addComment("email bounce-Premium Accept/Reject is not completed. Please correct the email address."); 
		} else {
			addComment("eDelivery not complete, policy print required"); // SR784122, APSL3361 New auto comments text for both SI and regular cases
			// SR784122, APSL3361 Code deleted			
		}
	}

	/**
	 * Adds a new comment to the AWD system.
	 * 
	 * @param aComment
	 *            the comment to be added to the AWD system.
	 * @param aProcess
	 *            the process that added the comment.
	 */
	public void addComment(String aComment) {
		NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
		npec.setActionAdd();
		npec.setOriginator(getUser().getUserID());
		npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		npec.setProcess(getUser().getUserID());
		npec.setText(aComment);
		getWork().addManualComment(npec.convertToManualComment());
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Comment added: " + aComment);
		}
	}

	/**
	 * @return Returns the user.
	 */
	public NbaUserVO getUser() {
		return user;
	}

	/**
	 * @param user
	 *            The user to set.
	 */
	public void setUser(NbaUserVO user) {
		this.user = user;
	}

	/**
	 * @return Returns the work.
	 */
	public NbaDst getWork() {
		return work;
	}

	/**
	 * @param work
	 *            The work to set.
	 */
	public void setWork(NbaDst work) {
		this.work = work;
	}

	// SR787006-APSL3702 New method
	protected void createPrintWork(NbaDst work, NbaUserVO user, String polNumber) throws NbaBaseException {
		NbaDst parentWork = retrieveParentWork(user, polNumber);
		boolean createPrint = true;
		if (parentWork != null) {
			// Check if print already created for automatic paper delivery
			NbaSearchVO searchPrintVO = performSearch(user, polNumber, A_WT_TEMP_REQUIREMENT);
			if (searchPrintVO != null && searchPrintVO.getSearchResults() != null && !searchPrintVO.getSearchResults().isEmpty()) {
				List searchResultList = searchPrintVO.getSearchResults();
				for (int i = 0; i < searchResultList.size(); i++) {
					NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
					if (END_QUEUE.equalsIgnoreCase(searchResultVo.getQueue()) && searchResultVo.getNbaLob().getWorkSubType() == NbaOliConstants.TC_SUBTYPE_PAPERDELIVERY_REQUEST) {
						addComment("Paper Delivery request is already Processed.");
						createPrint = false;
						break;
					}
				}
			}
			if(createPrint){
				NbaContractPrintFacadeBean facade = new NbaContractPrintFacadeBean();
				facade.generateContractExtract(getUser(), parentWork, String.valueOf(NbaOliConstants.OLI_ATTACH_WHOLEPOLICY), false , NbaConstants.PROVIDER_NOTIFICATION);//APSL5364
			}
			copyLOBs(parentWork, work);
			work.getNbaLob().setWorkSubType((int)NbaOliConstants.TC_SUBTYPE_PAPERDELIVERY_REQUEST);
			WorkflowServiceHelper.unlockWork(user, parentWork);
			//After Unlock retrieving work again to get the lock.
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(work.getWorkItem().getItemID(), true);
			retOpt.setLockWorkItem();
			work = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
		}
	}
	
	// NBLXA-2019 New method
	protected void addPrintComment(NbaDst work, NbaUserVO user, String polNumber,long transSubType) throws NbaBaseException {
		NbaDst parentWork = retrieveParentWork(user, polNumber);
		String comment = "";
		if(transSubType == NbaOliConstants.TC_SUBTYPE_FPLOCALPRINT_REQUEST){
			comment = "FP chose Local Print for delivery of the policy";
		}else if(transSubType == NbaOliConstants.TC_SUBTYPE_AXAPRINTMAIL_REQUEST){
			comment = "FP chose AXA Print and Mail for delivery of the policy";
		}
		if (parentWork != null) {
			NbaUtils.addAutomatedComment(parentWork, user, comment);
			WorkflowServiceHelper.commitComments(user, parentWork);
			copyLOBs(parentWork, work);
			work.getNbaLob().setWorkSubType((int)transSubType);
			WorkflowServiceHelper.unlockWork(user, parentWork);
			//After Unlock retrieving work again to get the lock.
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(work.getWorkItem().getItemID(), true);
			retOpt.setLockWorkItem();
			work = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
		}
	}
}
