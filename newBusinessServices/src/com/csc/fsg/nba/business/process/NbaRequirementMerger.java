package com.csc.fsg.nba.business.process;
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
 * 
 * *******************************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsRequirement;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
/**
 * NbaRequirementMerger determines which (if any) of the new requirements for a person should be added to the contract. A new requirement is added if: 
 * - the requirement does not exist. This is determined by verifying that there is not a pre-existing requirement of the same type, 
 *   and for APS (type 11) requirements the same requiremnt vendor. 
 * - the requirement exists, but has been receipted for more than x days, where x is the requirement received allowable days obtained from a VPMS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN016</td><td>Version 4</td><td>IU - Problems and Requirements Merging</td></tr>
 * <tr><td>SPR2199</td><td>Version 6</td><td>P&R Requirements Merging Logic Needs to Change to Not Discard some Requirements</td></tr> *
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaRequirementMerger {
	private NbaUserVO userVO;
	private NbaTXLife nbaTxLife; //ALS4082
	
	public NbaRequirementMerger(){
	
	}

	//ALS3963 new method //ALS4082 Signature changed
	public NbaRequirementMerger(NbaUserVO uservo, NbaTXLife nbaTxLife){
		this.userVO = uservo;
		this.nbaTxLife = nbaTxLife;
	}
	protected static NbaLogger logger = null; //SPR2199
	//SPR2199 code deleted
	/**
     * Determine which (if any) of the new requirements for a person should be added to the contract. A new requirement is added if: 
     * - the requirement does not exist. This is determined by verifying that there is not a pre-existing requirement of the same type, 
     *   and for APS (type 11) requirements the same requiremnt vendor. 
     * - the requirement exists, but has been receipted for more than x days, where x is obtained from a VPMS model.
     * @param existingReqs list of existing requirements for the person
     * @param newReqs list of new requirements for the person.
     * @return list of new requirements to be added to the contract
     * @throws NbaBaseException
     */
	//SPR2199 New Method
	public List determineNewRequirements(List existingReqs, List newReqs) throws NbaBaseException {
        //begin SPR2199
	    List newList = removeDuplicates(newReqs);
        int newReqSize = newList.size();
        RequirementInfo newReqInfo = null;
        List requirementsToAdd = new ArrayList();
        for (int i = 0; i < newReqSize; i++) {
            newReqInfo = (RequirementInfo) newList.get(i);
            if (addRequirement(newReqInfo, existingReqs)) {
                requirementsToAdd.add(newReqInfo);
            }
        }
        return requirementsToAdd;
    }
	
	/**
	 * @param newReqs
	 * @param existingReqs
	 * @return
	 * @throws NbaBaseException
	 */
	 public List removeExistingRequirements(List newReqs, List existingReqs) throws NbaBaseException {
		List reqGenList = new ArrayList();
		for (int i = 0; i < newReqs.size(); i++) {
			NbaVpmsRequirement vpmsReq = (NbaVpmsRequirement) newReqs.get(i);
			RequirementInfo newReqInfo = new RequirementInfo();
			newReqInfo.setReqCode(vpmsReq.getType());
			newReqInfo.setRequestedDate(new Date());
			newReqInfo.setFormNo(vpmsReq.getFormNumber());//ALS3255
			if (addRequirement(newReqInfo, existingReqs)) {
				reqGenList.add(vpmsReq);
			}
		}
		return reqGenList;
	}
	
	/**
     * Determine the duplicate requirements and remove the duplicate one from the list.The one removed of the common reqs found in the lists 
     * would be the one from formReqList
     * @param formReqList list of existing requirements for the person
     * @param formReqQuestionList list of new requirements for the person.
     * @return list of new requirements to be added to the contract
     * @throws NbaBaseException
     */
	//NBA250 New Method
	public List determineFormRequirements(List formReqList, List formReqQuestionList) throws NbaBaseException {
        int formReqSize = formReqList.size();
        int size = formReqQuestionList.size();
        NbaVpmsRequirement formReq = null;
        NbaVpmsRequirement formQuesReq = null;
        for (int i = 0; i < formReqSize; i++) {
        	boolean reqIsPresent = false;
        	formReq = (NbaVpmsRequirement) formReqList.get(i);
            for (int j = 0; j < size; j++) {
            	formQuesReq = (NbaVpmsRequirement) formReqQuestionList.get(j);
                if (isSameRequirement(formReq, formQuesReq)) {
                	reqIsPresent = true;
                }
            }
            if(!reqIsPresent){
            	formReqQuestionList.add(formReq);
            }
        }
        //APSL4780 Starts
        List uniqueReqList = new ArrayList();
        NbaVpmsRequirement requirementOldList = null;
        NbaVpmsRequirement requirementNewList = null;
        boolean duplicate;
        for(int i =0;i<formReqQuestionList.size();i++){
        	requirementOldList = (NbaVpmsRequirement)formReqQuestionList.get(i);
        	duplicate = false;
        	for(int j=0;j<uniqueReqList.size();j++){
        		requirementNewList = (NbaVpmsRequirement)uniqueReqList.get(j);
        		if(duplicateFormRequirement(requirementOldList,requirementNewList)){
        			duplicate = true;
        			break;
        		}
        	}
        	if(!duplicate){
        		uniqueReqList.add(requirementOldList);
        	}
        }
        return uniqueReqList;
        //APSL4780 Ends
    }
	
	//APSL4780 New Method
	/**
	 * Returns true if both requirements have same QuestionNumber, QuestionText and ResponseCode
	 */
	private boolean duplicateFormRequirement(NbaVpmsRequirement requirementOldList, NbaVpmsRequirement requirementNewList) {
		return requirementOldList.getFormNumber().equals(requirementNewList.getFormNumber())
		&& requirementOldList.getProvider().equals(requirementNewList.getProvider())
		&& (requirementOldList.getType()) == requirementNewList.getType();
	}

	/**
	 * Eliminate duplicate new Requirements;
     * @param newReqs
     * @return a List containing the new Requirement with duplicates removed.
     */
	//SPR2199 New Method 
	protected List removeDuplicates(List newReqs) {
        List newList = new ArrayList();
        RequirementInfo reqInfo;
        RequirementInfo inListReqInfo;
        boolean found;       
        for (int i = 0; i < newReqs.size(); i++) {
            reqInfo = (RequirementInfo) newReqs.get(i);
            found = false;
            for (int j = 0; j < newList.size(); j++) {
                inListReqInfo = (RequirementInfo) newList.get(j);	//AXAL3.7.40
                if ((reqInfo.getReqCode() == inListReqInfo.getReqCode()) && (reqInfo.getRequirementDetails().equalsIgnoreCase(inListReqInfo.getRequirementDetails()))) { //QC966
                	//ALS4006 code deleted
                	found = true;
					break;
				}
            }
            if (!found) {
                newList.add(reqInfo);
            }
        }
        return newList;
    }  
	/**
     * Determine if the both the NbaVpmsRequirement object have same requirement Type
     * @param formReq - NbaVpmsRequirement
     * @param formQuesReq - NbaVpmsRequirement
     * @return true if the NbaVpmsRequirement objects represent the same requirement
     */
 	//NBA250 New Method     
	protected boolean isSameRequirement(NbaVpmsRequirement formReq, NbaVpmsRequirement formQuesReq) {
        if (formReq.getType() == formQuesReq.getType()) {
        	return true;
        }
        return false;
	}
	/**
     * Determine if the existing and new RequirementInfo objects represent the same requirement by comparing the requirement codes. If they match and
     * the requirement is an APS, also check the vendor codes for a match.
     * @param newReqInfo - the new RequirementInfo
     * @param existingReqInfo - the existing RequirementInfo
     * @return true if the RequirementInfo objects represent the same requirement
     */
 	//SPR2199 New Method     
	protected boolean isSameRequirement(RequirementInfo newReqInfo, RequirementInfo existingReqInfo) {
		if (newReqInfo.getReqCode() == existingReqInfo.getReqCode()) {
			//Begin ALII537
			if (NbaOliConstants.OLI_REQCODE_AMENDMENT == newReqInfo.getReqCode() && existingReqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_ADD) {//NBLXA-1281
				return false;
			}
			if(!NbaUtils.isBlankOrNull(newReqInfo.getFormNo())){
				return newReqInfo.getFormNo().equalsIgnoreCase(existingReqInfo.getFormNo());
			}
			//End ALII537
			if (NbaOliConstants.OLI_REQCODE_PHYSSTMT == newReqInfo.getReqCode()) {
				return isSameVendor(newReqInfo, existingReqInfo);
				//Begin ALS4006
			} else if (NbaUtils.isBlankOrNull(newReqInfo.getRequirementDetails()) || NbaUtils.isBlankOrNull(existingReqInfo.getRequirementDetails())) {
				return true;
			} else {
				return newReqInfo.getRequirementDetails().equalsIgnoreCase(existingReqInfo.getRequirementDetails());
			}//end ALS4006
		}
		return false;
	}
	/**
	 * Determine if the new requirement should be added to the contract. - locate the most recent requirement which matches on requirement type and
	 * (for APS's) vendor code, or a non-receipted matching requirement - if a match is not found, the new requirement should be added to the
	 * contract. - if a match is found, and the requirement has been receipted more the x days previously, the new requirement should be added to the
	 * contract. - otherwise, the new requirement is bypassed.
	 * @param reqCode requirement code of the new requirement.
	 * @param existingReqs list of existing requirements
	 * @return true if the new requirement should be added to the contract.
	 * @throws NbaBaseException
	 */
	//SPR2199 New Method
    protected boolean addRequirement(RequirementInfo newReqInfo, List existingReqs) throws NbaBaseException {
		RequirementInfo existingRequirement = null;
		int size = existingReqs.size();
		int locateIdx = -1;
		for (int j = 0; j < size; j++) {
			existingRequirement = (RequirementInfo) existingReqs.get(j);
			if (isSameRequirement(newReqInfo, existingRequirement)) {
				if (locateIdx == -1) {
					locateIdx = j; // first match
				} else {
					if (existingRequirement.getReqStatus() != NbaOliConstants.OLI_REQSTAT_RECEIVED) {
						locateIdx = j; // not receipted, use this one
					} else if (NbaUtils.compare(existingRequirement.getRequestedDate(), ((RequirementInfo) existingReqs.get(locateIdx))
							.getRequestedDate()) >= 0) { //NBLXA-2175
						locateIdx = j; // request date more recent on current one
					} else if (!NbaUtils.isBlankOrNull(newReqInfo.getFormNo())
							&& newReqInfo.getFormNo().equalsIgnoreCase(existingRequirement.getFormNo())) {
						locateIdx = j; // ALS4181 consider the one which was created with right form #
					}
				}
			}
		}

		if (locateIdx == -1) { //If none found, new requirement will be added
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug(
						"Pre-existing requirement not found for requirement type: " + newReqInfo.getReqCode() + ". Requirement will be added.");
			}
			return true;
		}
		existingRequirement = (RequirementInfo) existingReqs.get(locateIdx);
		if (NbaUtils.isRequirementFulfilled(String.valueOf(existingRequirement.getReqStatus())) && isExpired(existingRequirement)) { //ALS3963 //ALS4892
			return true;
		}		
		if (existingRequirement.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED) {// receipted
			if (existingRequirement.getFormNo() == null || isSameFormNumber(newReqInfo, existingRequirement) ) {//ALS3255, ALII1547
				return isBeyondReceiptDate(newReqInfo, existingRequirement);
			}
			return (!isSameFormNumber(newReqInfo, existingRequirement) && existingRequirement.getReqCode() != NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED);//ALS3255, APSL1533
		} else if ((getNbaTxLife().isReissue() && isCreate(newReqInfo, existingReqs))) {//ALS4082 //NBLXA-1774
			return true;
		} else {
			return false;
		}
	}

    
    
    //NBLXA-1774 new method created
    
    protected boolean isCreate(RequirementInfo newReqInfo, List existingReqs) throws NbaBaseException {

		boolean doCreate = true;
		RequirementInfo existingRequirement = null;
		int size = existingReqs.size();
		for (int j = 0; j < size; j++) {
			existingRequirement = (RequirementInfo) existingReqs.get(j);
			if (isSameRequirement(newReqInfo, existingRequirement)) {
				if (existingRequirement.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED
						&& (!isBeyondReceiptDate(newReqInfo, existingRequirement))){
					doCreate = false;
					break;
				}
				if (NbaUtils.isRequirementOutstanding(existingRequirement.getReqStatus())) {
					doCreate = false;
					break;
				}
			}
		}
		return doCreate;
	}
    
    
    
    
    
    
    
 
    /**
	 * Populate NbaLob object with lobs required for vpms call to retrieve maximum allowable days from RequirementInfo object which will later be used
	 * to retrieve allowable days from REQUIREMENTS model.
	 * @param existReqInfo
	 * @return NbaLob object
	 */
    //SPR2199 New Method
    protected NbaLob getLobFromReqInfo(RequirementInfo requirementInfo) {
        NbaLob nbaLob = new NbaLob();
        nbaLob.setReqType((int) requirementInfo.getReqCode());
        nbaLob.setReqVendor(getVendor(requirementInfo));
        return nbaLob;
    }

	/**
     * Compare the the vendor codes (TrackingServiceProvider) of an existing and new RequirementInfo.
     * @param newReq - the new RequirementInfo
     * @param existReq - the existing RequirementInfo
     * @return true if the codes are present on both RequirementInfo objects, and contain the same value (ignoring case).
     */
 	//SPR2199 New Method
	protected boolean isSameVendor(RequirementInfo newReq, RequirementInfo existReq) {
        return (getVendor(existReq).equalsIgnoreCase(getVendor(newReq)));
    }
	
	/**
     * Compare the the form number of an existing and new RequirementInfo.
     * @param newReq - the new RequirementInfo
     * @param existReq - the existing RequirementInfo
     * @return true if the codes are present on both RequirementInfo objects, and contain the same value (ignoring case).
     */
 	//ALS3255 New Method
	protected boolean isSameFormNumber(RequirementInfo newReq, RequirementInfo existReq) {
      	return (existReq.getFormNo().equalsIgnoreCase(newReq.getFormNo()));
    }	
	/**
     * Get the vendor code for a RequirementInfo object.
     * @param requirementInfo
     * @return RequirementInfo.RequirementInfoExtension.TrackingInfo.TrackingServiceProvider
     */
 	//SPR2199 New Method
	protected String getVendor(RequirementInfo requirementInfo) {
        String vendor = "";
        RequirementInfoExtension reqExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
        if (reqExt != null && reqExt.hasTrackingInfo()) {
            TrackingInfo trackingInfo = reqExt.getTrackingInfo();
            if (trackingInfo.hasTrackingServiceProvider()) {
                vendor = trackingInfo.getTrackingServiceProvider();
            }
        }
        return vendor;
    }
 	
	/**
     * Determine if the new requirement request date is beyond the Requirement received allowable days of 
     * a pre-existing receipted requiremnt. 
     * @param newReqInfo - the new requirement
     * @param existReqInfo - the pre-existing receipted requirement
     * @return true if the request date is greater than or equal to the receipt date plus allowable days
     * @throws NbaBaseException
     */
	//SPR2199 New Method
	protected boolean isBeyondReceiptDate(RequirementInfo newReqInfo, RequirementInfo existReqInfo) throws NbaBaseException {
        boolean isBeyond = false;
        if (existReqInfo.getReceivedDate() != null) {
            NbaLob existinglob = getLobFromReqInfo(existReqInfo);
            NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(existinglob);
            oinkDataAccess.setContractSource(getNbaTxLife());//ALS4366
            int days = getAllowableDaysToReceive(oinkDataAccess); //Requirement received allowable days
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(existReqInfo.getReceivedDate());
            calendar.add(Calendar.DATE, days);
            if (getLogger().isDebugEnabled()) {
                getLogger().logDebug(
                        "New requirement request date: " + newReqInfo.getRequestedDate() + ", existing requirement receipt date: "
                                + existReqInfo.getReceivedDate() + ", period: " + days + ", calculated limit date:" + calendar.getTime());
            }
            isBeyond = NbaUtils.compare(newReqInfo.getRequestedDate(), calendar.getTime()) > -1; //Return true if greater than or equal to
            if (getLogger().isDebugEnabled()) {
                if (isBeyond) {
                    getLogger()
                            .logDebug(
                                    "New Requirement requested date occurs after receipt date limit for existing requirement. New requirement will be added.");
                } else {
                    getLogger().logDebug(
                            "New Requirement requested date is within receipt date limit for exiting requirement. New requirement will be bypassed.");
                }
            }
        }
        return isBeyond;
    }
    /**
     * Retrieve the value of "Received within allowable days" from the VP/MS model.
     * @return number of valid days.
     * @throws NbaBaseException 
     */
	//SPR2199 New Method
    protected int getAllowableDaysToReceive(NbaOinkDataAccess oinkDataAccess) throws NbaBaseException {
        int days = 0;
        List daysList = new ArrayList();
        daysList = getDataFromVpms(NbaVpmsAdaptor.EP_RECEIPT_ALLOWABLE_DAYS, oinkDataAccess).getResultsData();
        if (daysList != null && daysList.size() > 0) {
            days = Integer.parseInt(daysList.get(0).toString());
        }
        return days;
    }
    
    /**
     * Retreive data from the Requirements VP/MS model based on the entryPoint passed in.
     * @param entryPoint the entry point to be executed in the VP/MS model.
     * @return the results of the call to the VP/MS model
     * @throws NbaBaseException
     * @throws NbaVpmsException
     * @throws NbaBaseException
     */
     //SPR2199 New Method 
    protected NbaVpmsResultsData getDataFromVpms(String entryPoint, NbaOinkDataAccess oinkData) throws NbaBaseException {
        NbaVpmsAdaptor vpmsProxy = null;
        try {
            // SPR3290 code deleted
            Map deOink = new HashMap();	//ALS3963
            deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUserVO())); //ALS3963
            vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
            vpmsProxy.setVpmsEntryPoint(entryPoint);
            vpmsProxy.setSkipAttributesMap(deOink);	//ALS3963
            // SPR3290 code deleted
            try {
                return new NbaVpmsResultsData(vpmsProxy.getResults());
            } catch (NbaVpmsException e) {
                e.forceFatalExceptionType();
                throw e;
            } catch (RemoteException e) {
                throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION + NbaVpmsAdaptor.REQUIREMENTS, e, NbaExceptionType.FATAL);
            }
        } finally {
            if (vpmsProxy != null) {
                try {
                    //begin SPR3362
    				vpmsProxy.remove();					
    				//end SPR3362
                } catch (Exception e) {
                    LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
                }
            }
        }
    }
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
    //SPR2199 New Method 
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaRequirementMerger.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaRequirementMerger could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	} 
	  /**
	   * @param reqCode
	   * @return
	   */
	//AXAL3.7.40 New Method Added.
	private boolean isCVMessageRequirement(long reqCode) {
		int length = NbaConstants.CONTRACT_VALIDATION_MESSAGES_REQUIREMENTS.length;
		for (int i = 0; i < length; i++) {
			if (reqCode == NbaConstants.CONTRACT_VALIDATION_MESSAGES_REQUIREMENTS[i]) {
				return true;
			}
		}
		return false;
	}

	/**
     * Checks whether the requirement is expired.
     * @param existReq - the existing RequirementInfo
     * @return true if the requirement is expired. 
     */
 	//ALS3963 New Method
	protected boolean isExpired(RequirementInfo existReq) {
		return (NbaOliConstants.OLI_REQSUBSTAT_CNCLINSCO == existReq.getReqSubStatus());
	}
	
	/**
	 * @return Returns the userVO.
	 */
	//ALS3963
	public NbaUserVO getUserVO() {
		return userVO;
	}
	/**
	 * @param userVO The userVO to set.
	 */
	//ALS3963
	public void setUserVO(NbaUserVO userVO) {
		this.userVO = userVO;
	}
	/**
	 * @return Returns the nbaTxLife.
	 */
	public NbaTXLife getNbaTxLife() {
		return nbaTxLife;
	}
	/**
	 * @param nbaTxLife The nbaTxLife to set.
	 */
	public void setNbaTxLife(NbaTXLife nbaTxLife) {
		this.nbaTxLife = nbaTxLife;
	}
}
