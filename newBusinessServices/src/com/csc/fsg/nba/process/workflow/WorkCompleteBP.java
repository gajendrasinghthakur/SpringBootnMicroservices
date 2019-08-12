package com.csc.fsg.nba.process.workflow;

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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.process.NbaProcessWorkCompleteStatusProvider;
import com.csc.fsg.nba.database.AxaWorkflowDetailsDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaWorkCompleteRequest;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Allows the user to identify that they have completed their work on a work item and
 * automatically set the outgoing status to move the work item to the next queue as
 * defined in the AutoProcessStatus VP/MS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>APSL3836</td><td>Discretionary</td><td>Electronic Initial Premium - Phase 2</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class WorkCompleteBP extends NbaUpdateWorkBP {

    /*
     * (non-Javadoc)
     * 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    protected static final String A_RESOURCES = "A_Resources";
    protected NbaLogger logger = null; //SPR2386
	
    //NBA186 code deleted
    
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            //NBA186 code deleted
        	workComplete((NbaWorkCompleteRequest)input);//NBA186
        } catch (NbaBaseException nbe) {
        	if (nbe.isError()) {
        		addErrorMessage(result, nbe.getFormattedMessage());
        	} else {
        		addExceptionMessage(result, nbe);
        	}
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	/**
	 * Calls AutoProcess VP/MS Model's Work Completed Node. This model returns an outgoing
	 * status and the priority based on the current worktype and status. The work item's
	 * status and priority is then updated with the status and priority returned by the
	 * VP/MS model.
	 * @param work DST object 
	 * @param user NbaUserVO object representing the logged on user.
	 * @throws NbaBaseException if RemoteException occured
	 **/
    //NBA186 - changed method signature - added NbaWorkCompleteRequest removed - NbaDst,Map, NbaTXLife
    protected void workComplete(NbaWorkCompleteRequest workCompleteRequest) throws NbaBaseException {
		NbaDst work = workCompleteRequest.getNbaDst(); //NBA186
		NbaUserVO user = work.getNbaUserVO();
		String UwComplMsg=NbaTableConstants.EMPTY_STRING; //APSL4047
		//begin APSL4196
		Map deOink = new HashMap();
		if(workCompleteRequest.getNbaTXLife() != null){
			deOinkTermConvData(deOink, workCompleteRequest);	
		}//end APSL4196
		
		if(workCompleteRequest.isCommentRequired()){ //APSL4047
    	String queueTranslation=NbaUtils.getQueuTranslation(work.getQueue(),work); //APSL4047
		String statusTranslation=NbaUtils.getStatusTranslation(work.getWorkType(),work.getStatus()); //APSL4047
		UwComplMsg=NbaConstants.workCmplSts+user.getFullName()+" when Work Item was in "+queueTranslation+" Queue for Status:"+statusTranslation+"("+work.getStatus()+")"; //APSL4047
		}
		deOink.put(A_RESOURCES, getEnablementsAsString(workCompleteRequest.getEnablement())); //NBA186
		deOink.put("A_PHICVExists", String.valueOf(isPhiCvExistForSiCase(workCompleteRequest))); //APSL2808
		deOink.put("A_UWRole", user.getUwRole()); //APSL4047
		deOink.put("A_UWQueue", user.getUwQueue()); //NBLXA-2489
		//NBLXA-1326 BEGIN
		String reviewedInd = NbaConstants.FALSE_STR;
		if (workCompleteRequest.getNbaTXLife() != null) {
			RequirementInfo reqInfo = workCompleteRequest.getNbaTXLife().getRequirementInfo(work.getNbaLob().getReqUniqueID());
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			if (reqInfoExt != null) {
				reviewedInd = reqInfoExt.getReviewedInd() == true ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR;
			}
		}
		deOink.put("A_ReviewedInd", reviewedInd);
		//NBLXA-1326 END
		//Begin APSL2735
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		if(NbaUtils.isInitialPremiumPaymentForm(workCompleteRequest.getNbaTXLife()) && work.getNbaLob().getReqType() == (int)132L){
			deOinkCIPEValidationValues(deOink,workCompleteRequest,work,oinkRequest);
		}
		//End APSL2735		
		NbaProcessWorkCompleteStatusProvider provider = new NbaProcessWorkCompleteStatusProvider(oinkRequest,work.getNbaLob(), deOink, workCompleteRequest
				.getNbaTXLife());//NBA186,APSL2735-Change Signature of Method
		if (!NbaVpmsConstants.IGNORE.equals(provider.getStatus())) {// ALS5626
			work.setStatus(provider.getStatus());
			NbaUtils.setRouteReason(work, provider.getStatus(), provider.getReason()); // ALS5260
			if (!NbaUtils.isBlankOrNull(provider.getReason())) { // ALS5337
				NbaUtils.addGeneralComment(work, user, provider.getReason());// ALS5337
			}// ALS5337

			// increase the priority only when a valid status is returned
			if (!NbaUtils.isBlankOrNull(provider.getPriorityAction()) && !NbaUtils.isBlankOrNull(provider.getPriorityValue())) {
				work.increasePriority(provider.getPriorityAction(), provider.getPriorityValue());
			}

			// update the Work Item
			if (workCompleteRequest.isCommentRequired()) { // APSL4047
				NbaProcessingErrorComment comment = createUwWorkComplComment(work, UwComplMsg); // APSL4047
				work.addManualComment(comment.convertToManualComment()); // APSL4047
			}// APSL4047
			//APSL4342 :: START
			// Begin APSL4412
			if (work.getWorkType().equals(NbaConstants.A_WT_MISC_WORK)) {
				NbaTXLife nbaTXLife = workCompleteRequest.getNbaTXLife();
				if (provider.getStatus().equals(NbaConstants.A_STATUS_RCMHOLD)) {
					int suspendDays = 2;
					try {
						suspendDays = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
								NbaConfigurationConstants.RCM_NOTIF_WORK_SUSPEND_DAYS));
						if (nbaTXLife != null) {
							PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
							if (policyExtn != null && policyExtn.getRcmfollowupFreq() > 0) {
								suspendDays = policyExtn.getRcmfollowupFreq();
							}
							 
						}
					} catch (Exception e) {
					}
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(new Date());
					calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
					Date maxSuspendDate = (calendar.getTime());
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(work.getID());
					suspendVO.setActivationDate(maxSuspendDate); // NBA119
					AccelResult result = (AccelResult) callBusinessService("NbaSuspendWorkBP", suspendVO);
					processResult(result);
					if (nbaTXLife != null) { 
						AxaWorkflowDetailsDatabaseAccessor.getInstance().updateWorkFlowDetails(nbaTXLife.getPolicy().getPolNumber(), 0); // set value to 0 � hold /End
					}
				}
				//APSL4342 : END
			}
			// End APSL4412
			deleteUnderwritingReqTypesReceived(work); // APSL5055
			work = update(user, work);
		}
	}

	/**
	 * Format and return a String containing delimited values for the enabled fields found in the enablements Map.
     * @param enablements - the enablements Map
     * @return - the String containing the enabled fields
     */
    private String getEnablementsAsString(Map enablements) {
        StringBuffer resources = new StringBuffer(NbaVpmsAdaptor.VPMS_DELIMITER[0]);
        Iterator it = enablements.keySet().iterator();
        while (it.hasNext()) {
            String field = (String) it.next();
            if (!((Boolean) enablements.get(field)).booleanValue()) { //Add field to SrtingBuffer if not disabled
                resources.append(field).append(NbaVpmsAdaptor.VPMS_DELIMITER[0]);
            }
        }
        return resources.toString();
    }
    
//    APSL2808 New Method
    private boolean isPhiCvExistForSiCase(NbaWorkCompleteRequest workCompleteRequest){
    	boolean flag = false;
    	NbaTXLife txLife = workCompleteRequest.getNbaTXLife();
    	if(txLife != null && NbaUtils.isSimplifiedIssueCase(txLife)){
    		ArrayList messages = txLife.getPrimaryHolding().getSystemMessage();
			if (NbaUtils.hasSignificantValErrors(messages, NbaConstants.SUBSET_SI_PHI)) {
				flag = true;
			}
    	}
    	
    	return flag;
    }
    
    
    //New Method APSL2735
    protected NbaDst retrieveParentWithTransactions(NbaDst work,NbaUserVO userVO) throws NbaBaseException {
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (work.isCase()) {
			retOpt.setWorkItem(work.getID(), true);
			retOpt.requestTransactionAsChild();
			retOpt.setNbaUserVO(userVO);
		} else { // if a transaction
			retOpt.setWorkItem(work.getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestTransactionAsSibling();
			retOpt.setLockParentCase();
		}
		if (work.isLocked(userVO.getUserID())) {
			retOpt.setLockWorkItem();
		} 
		NbaDst newDst = WorkflowServiceHelper.retrieveWorkItem(userVO, retOpt);
		return newDst;

	}
    
    //New Method APSL2735
	private boolean isSystematicReqOutStanding(WorkItem nbaTrans) {
		boolean flag = false;
		if (nbaTrans.getStatus().equalsIgnoreCase("SYSREQRCD2")
				|| nbaTrans.getStatus().equalsIgnoreCase("SYSREQRCD1")) {
			flag = true;
		}
		return flag;
	}

	//New Method APSL2735
	private boolean isServiceResultOutstanding(WorkItem nbaTrans) {
		boolean flag = false;
		if (NbaUtils.suspenseTeam.contains(nbaTrans.getStatus())) { // NBLXA-2535[NBLXA-2328]
			flag = true;
		}
		return flag;
	}

	//New Method APSL2735
	protected void deOinkCIPEValidationValues(Map deOink,NbaWorkCompleteRequest workCompleteRequest,NbaDst work,NbaOinkRequest oinkRequest) throws NbaBaseException{
    	NbaTXLife txlife = workCompleteRequest.getNbaTXLife();
		String reqInfoUniqueId = work.getNbaLob().getReqUniqueID();
		if(! NbaUtils.isBlankOrNull(reqInfoUniqueId)){
			RequirementInfo reqInfo = txlife.getRequirementInfo(reqInfoUniqueId);
			if(reqInfo != null){
				if (oinkRequest.getRequirementIdFilter() == null || oinkRequest.getRequirementIdFilter().trim().length() == 0) {
					oinkRequest.setRequirementIdFilter(reqInfo.getId());
					//APSL3836 Begins
					RequirementInfoExtension reqInfoExt =  NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if(reqInfoExt != null){
						deOink.put("A_IsDueAmtByCheck", String.valueOf(reqInfoExt.getIsDueAmtByCheck()));
					}
					//APSL3836 Ends
				}
			}
		}
		NbaDst parentCase  = retrieveParentWithTransactions(work,work.getNbaUserVO());//Updating parent case as it doesn't have transactions with it
		List childTrans = parentCase.getTransactions();
		Iterator childTransIter = childTrans.iterator();
		//Begin ALII2015
		Iterator childTransIter1 = childTrans.iterator();
		double validatingAmt = 0.0;
		double txLifePDISValue = (Double.isNaN(NbaUtils.getPolicydeliveryInstrutionAmt(txlife.getPolicy()))? 0.0 : NbaUtils.getPolicydeliveryInstrutionAmt(txlife.getPolicy()));//ALII2035,ALII2042
		double checkAmountLOB = 0.0;
		Date failureDate = null;
		Date pdisGeneratedDate = NbaUtils.getPdisCreateDate(txlife.getPolicy());;
		WorkItem servresult = null;
		while (childTransIter.hasNext()) {
			WorkItem nbaTrans = (WorkItem) childTransIter.next();
			if ((nbaTrans.getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)
					&& (nbaTrans.getLobValue(NbaConstants.A_LOB_REQUIREMENT_TYPE) == Long
							.toString(NbaOliConstants.OLI_REQCODE_AUTHEFT)) && isSystematicReqOutStanding(nbaTrans))) {
				checkAmountLOB = convertLOBtoCurrency(nbaTrans.getLobValue(NbaConstants.A_LOB_CHECK_AMOUNT));
				break;
			}
		}

		while (childTransIter1.hasNext()) {
			WorkItem nbaTrans1 = (WorkItem) childTransIter1.next();
			if (nbaTrans1.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_SERVICE_RESULT)) {
				double SVRSLTcheckAmountLOB = convertLOBtoCurrency(nbaTrans1.getLobValue(NbaConstants.A_LOB_CHECK_AMOUNT));
				if (isServiceResultOutstanding(nbaTrans1)) {
					checkAmountLOB = SVRSLTcheckAmountLOB;
					servresult = nbaTrans1;
					break;
				} else {
					if (SVRSLTcheckAmountLOB == checkAmountLOB) {
						servresult = nbaTrans1;
						break;
					}
				}
			}
		}

		if (servresult != null) {
			failureDate = NbaUtils.getDateFromStringInAWDFormat(servresult.getItemID());
		}

		if (failureDate != null && pdisGeneratedDate != null && failureDate.before(pdisGeneratedDate)) {
			validatingAmt = (double)Math.round((txLifePDISValue - checkAmountLOB)*100)/100;
		} else {
			validatingAmt = txLifePDISValue;
		}
		//End ALII2015

		deOink.put("A_ValidatingAmt", String.valueOf(validatingAmt));
		deOink.put("A_TotalPremiumDue",String.valueOf(txlife.getPolicy().getPaymentAmt()));//ALII2015
		deOink.put("A_FailureDraftInd",String.valueOf(servresult!=null?true:false));
		deOink.put("A_FailureDraftAmt",String.valueOf(checkAmountLOB));//This LOB is deoinked as we have PDR reqt in session,but we need LOB of SVRSLT or Systematic reqt
		
    }
	
	public double convertLOBtoCurrency(String lobValue) throws NbaBaseException {
		if ((lobValue == null) || (lobValue.trim().length() == 0)) {
			return 0;
		} else {
			lobValue = "00" + lobValue.trim();	//prefix with "00" in case incoming string doesn't have at least 2 digits
			int decLoc = lobValue.length() - 2;
			lobValue = new StringBuffer(lobValue).insert(decLoc, ".").toString();
			return convertLOBtoDouble(lobValue);
		}
	}
	//New Method APSL2735
	/**
	 * Converts the LOB string value to a double value
	 * @param lobValue the LOB string to be converted
	 * @return the LOB value as a double
	 */
	protected double convertLOBtoDouble(String lobValue) throws NbaBaseException {
		if (lobValue == null) {
			return 0;
		} else if (lobValue.length() > 0) {
			try {
				return Double.parseDouble(lobValue);
			} catch (NumberFormatException nfe) {
				throw new NbaBaseException(nfe);
			}
		} else {
			return 0;
		}
	}
	
	// Begin APSL4047
	protected NbaProcessingErrorComment createUwWorkComplComment(NbaDst nbaDst, String msg) throws NbaBaseException {
		NbaUserVO userVO = nbaDst.getNbaUserVO();
		NbaProcessingErrorComment aComment = new NbaProcessingErrorComment();
		aComment.setEnterDate(getTimeStamp(userVO));
		aComment.setText(msg);
		aComment.setOriginator(userVO.getUserID());
		aComment.setUserNameEntered(NbaConstants.SystemAudit);
		aComment.setActionAdd();
		return aComment;
	}
		// End APSL4047	
	
	/**
	 * This method gets all Term Conv deOink variables by calling CONVERSIONUNDERWRITING model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//APSL4196 new method
	protected void deOinkTermConvData(Map deOink, NbaWorkCompleteRequest workCompleteRequest) throws NbaBaseException {
		//Calling a new Term Conversion model to determine if underwriting is required based on certain data 
		//entered on the Replacement view for the term conversion. This model will return two pieces of data:
		//(a)a Boolean to indicate if underwriting is required, (b) a conversion increase amount to be used for underwriting, if applicable
		//The vpmsadaptor object, which provides an interface into the VPMS system
	    NbaVpmsAdaptor vpmsProxy = null;
	    String entryPoint = "P_ResultXml";
	    try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(workCompleteRequest.getNbaTXLife());
			oinkData.setContractSource(workCompleteRequest.getNbaTXLife(), workCompleteRequest.getNbaDst().getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CONVERSIONUNDERWRITING); 
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			VpmsComputeResult result = vpmsProxy.getResults();
			String undReqdValue = "true";
			String convIncrAmtValue = "0";
			if (!result.isError()) {
	            NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(result);
	            List rulesList = vpmsResultsData.getResultsData();
	            if (!rulesList.isEmpty()) {
	                String xmlString = (String) rulesList.get(0);
		            NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
		            VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
		            List strAttrs = vpmsModelResult.getStandardAttr();
		            //Generate delimited string if there are more than one parameters returned
		            Iterator itr = strAttrs.iterator();
	        		while (itr.hasNext()) {
		            	StandardAttr stdAttr = (StandardAttr) itr.next();
		            	if("UndRequired".equalsIgnoreCase(stdAttr.getAttrName())){
		            		undReqdValue = stdAttr.getAttrValue();
		            	} else if("ConvIncreaseAmt".equalsIgnoreCase(stdAttr.getAttrName())){
		            		convIncrAmtValue = stdAttr.getAttrValue();
		            	}
	                }
	            }
	        }
			deOink.put("A_UndRequired", undReqdValue);
			deOink.put("A_ConvIncreaseAmt", convIncrAmtValue);
			
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Auto Process, Term Conv deoink: " + NbaVpmsException.VPMS_EXCEPTION, re);
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
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	//APSL4196
	protected NbaLogger getLogger() {//NBA103
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass()); //NBA103
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory."); //NBA103
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	/**
	 * Remove all underwriting requirement types set on the current application work
	 * item so that they are no longer reported as received when the application work
	 * status is displayed using work item identification. 
	 * @param work
	 * @return
	 */
	//NBA331.1, APSL5055 New Method
	protected Result deleteUnderwritingReqTypesReceived(NbaDst work) {
		if (NbaConstants.A_WT_APPLICATION.equals(work.getWorkType())) {
			Result result = callService(ServiceCatalog.UW_REQTYPES_RECEIVED_DISASSEMBLER, work.getID());
			if (!result.hasErrors()) {
				result = invoke(ServiceCatalog.DELETE_UW_REQTYPES_RECEIVED, result.getData());
			}
			return result;
		}
		return new AccelResult();
	}
 }
