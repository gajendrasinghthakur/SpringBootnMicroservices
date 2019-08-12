package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaReqIndicatorUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vo.nbaschema.Requirement;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.csc.fs.dataobject.nba.identification.RequirementsReceivedUpdateRequest;
import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;

/**
 * NbaProcAggregate is the class that processes nbAccelerator work items
 * found on the AWD aggregate queue (NBAGGRGT).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA087</td><td>Version 3</td><td>Post Approval & Issue Requirements</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>NBA097</td><td>Version 4</td><td>Work Routing Reason Displayed</td></tr>
 * <tr><td>NBA098</td><td>Version 4</td><td>Work Completed</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN010</td><td>Version 4</td><td>Evaluation Control Model</td></tr>
 * <tr><td>ACN022</td><td>Version 5</td><td>Re-Underwrite Project</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2812</td><td>Version 5</td><td>NBAGGRGT Automated Process is error stopped when a work completed status cannot be resolved in AutoProcessStatus VP/MS</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Cleanup</td></tr>
 * <tr><td>NBA136</td><td>Version 6</td><td>In Tray and Search Rewrite</td></tr>
 * <tr><td>SPR3046</td><td>Version 6</td><td>Receipted Requirement Remains in Aggregate Queue and Case Remains in Und Hold Queue when other Dependent Requirements are Cancelled</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3611</td><td>Version 8</td><td>The Aggregate process is not setting LOBs on a Case correctly</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr> 
 * <tr><td>NBA229</td><td>Version 8</td><td>nbA Work List and Search Results Enhancement</td></tr>
 * <tr><td>ALS4879</td><td>AXA Life Phase 1</td><td>QC # 4035 - AXAL03.07.31 Provider Feeds : Correct the Aggregate process</td></tr>
 * <tr><td>ALS4818</td><td>AXA Life Phase 1</td><td>QC # 3971 - E2E Informals: Blood rqmt suspended in AP aggregate queue</td></tr>
 * <tr><td>CR59174</td><td>XA Life Phase 2</td><td>1035 Exchange Case Manager</td></tr>
 * <tr><td>ALNA212</td><td>AXA Life Phase 2</td><td>Performance Improvement</td></tr>
 * <tr><td>CR1345857(APSL2575)</td><td>Discretionary</td>Predictive CR - Aggregate</tr>
 * <tr><td>NBLXA -1983</td><td>Discretionary</td>Requirement Bundling</tr>	
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcAggregate extends NbaAutomatedProcess {

	// begin NBA136
	// redefining these constants as strings for use in this process so that the string LOB fields do not 
	// have to be converted to a long value, and potentially causing a NumberFormatException
	protected static final String NO_RIGHTS = "User does not have rights".toUpperCase();	//SPR3611
	protected static final String REVIEW_SYSTEMATIC = String.valueOf(NbaConstants.REVIEW_SYSTEMATIC);
	protected static final String REVIEW_USER_REQUIRED = String.valueOf(NbaConstants.REVIEW_USER_REQUIRED);
	protected static final String OLI_REQSTAT_CANCELLED = String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED);
	protected static final String OLI_REQSTAT_COMPLETED = String.valueOf(NbaOliConstants.OLI_REQSTAT_COMPLETED);
	protected static final String OLI_REQSTAT_WAIVED = String.valueOf(NbaOliConstants.OLI_REQSTAT_WAIVED);
	protected static final String OLI_REQSTAT_RECEIVED = String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED);
	// end NBA136
	protected final static String A_ERR_NO_SUSPENSION = "SYS0116";	//ALS2575
	// NBA008 added -- start
	protected NbaDst parentCase = null;
	protected int suspendDays;
	protected List depenedReqList = new ArrayList();
	protected List suspendList = new ArrayList();
	protected List unsuspendList = new ArrayList();
	protected List unlockList = new ArrayList();
	protected NbaOinkDataAccess oinkData = null;
	// NBA008 added -- end
	protected boolean allRequirementsReviewed = false;	//SPR3611
	protected boolean reviewRequired = false;	//SPR3611
	protected boolean significantRequirement = false;	//SPR3611
	protected NbaProcessWorkCompleteStatusProvider workCompleteStatusProvider = null;	//SPR3611
	private NbaLob parentCaseLobs = null;  //SPR2992
	boolean suspendWork = false;
	protected boolean AnyOpenReevalWork = false;//ALS4596
	protected NbaTransaction reEvalTrans = null; //ALS5260
	

	/**
	 * NbaProcAggregate constructor comment.
	 */
	public NbaProcAggregate() {
		super();
	}
	/**
	 * This process will first call VP/MS to determine if a receipted requirement must have any additional requirement(s)
	 * on the case receipted before passing the requirement on to the next queue. If a requirement is dependent on additional requirement(s)
	 * being receipted, the status of the dependent requirement will not be changed until 
	 * the corresponding requirement(s) have been receipted. The work item is suspended for x number of 
	 * days based on the VP/MS model. If the dependent requirement(s) have not been received after number of days, 
	 * the work item is sent to the next queue based on a VP/MS model. 
	 * @param user the user for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return getResult(); //NBA050
		}
		// NBA008 code deleted
		// NBA008 code added start

		//retieve work items sources
		retrieveWork();

		//initialized VPMS value object
		oinkData = new NbaOinkDataAccess(getWorkLobs());  //SPR2992

		try {
			//get parent case from awd
			//Begin NBA300 
			retrieveParentCase();
			
			if(getNbaTxLife() != null && NbaUtils.isTermConvOPAICase(getNbaTxLife())){//ALII875//CR57950 an CR57951
				if(getParentCase() != null && (getParentCaseLobs().getCaseManagerQueue()==null || getParentCaseLobs().getCaseManagerQueue().trim().length()<=0)){
					suspendWI();
					this.parentCase.getTransactions().clear();//Transactions are not needed beyond this point
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Workitem suspended - Case Manager LOB is Null.", ""));
				}
			}	
			if (getResult() == null) {
				
				updateCaseTransactions();
				//End NBA300
				//begin ALS3972
				if (isAnyOpenReevalWork()) {
					//Unsuspend ReEval WI if suspended to allow the AutoUnderwriting
					unsuspendReEvalWI();
					suspendWI();
					addComment("Requirement has suspended due to outstanding ReEvalution work item");//ALS3972
				}
			}
			
			if (getResult() == null) {
				initializeVpmsStatus(); //NBA192 reinitialize after getting parent case
				if (isCaseDisposed()) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
				}
				//find dependencies and get dependend requirement from awd
				setDepenedReqList(getDependencies());
			}
			//end ALS3972
			//begin SPR3611	
			//APSL4992
			if(getWork()!=null && (A_WT_REQUIREMENT).equals(getWork().getWorkType())) {
				setTEXLobOnNegativeDisposedReq();
				
			}	
		} catch (NbaVpmsException e) {
			handleVpmsException(e);	 
		
		} catch (NbaBaseException e) {
			//Begin ALS2575
			
			if (e.getMessage() != null && e.getMessage().startsWith(A_ERR_NO_SUSPENSION)) {
				suspendWI();//ALS3972
				//addComment("Portions of this case are currently unavailable and cannot be processed at this time");//ALS3972//ALNA212 commented
				//End ALS2575
			} else if (e.isFatal() || e instanceof NbaAWDLockedException) {  //ALS4932
				unlockAWD(); //ALS4932 unlock any work that may have been locked prior to exception
				throw e; //ALS4932
			} else {
				e.printStackTrace(); //ALS4531
				handleNbaBaseException(e);
			}
		}
		 
		if (getResult() == null) {	 
			//if dependency exists process dependent requirements					
			if (getDepenedReqList().size() > 0) {
				suspendWork = !processDependencies();
			}
			if (suspendWork) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspend", "Suspend"));
			} else {
				 //NBA229 code deleted
				if (getResult() == null) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
					//update all dependcies to the AWD
					updateAWD(getDepenedReqList());
					//unsuspend all work item in the unsuspend list
					unsuspendAWD();
				} else {
					suspendWork = false;
				}
			}
		}
		if (!suspendWork) {    
			changeStatus(getResult().getStatus());		//If the current Transaction is not going to be suspended, change the status
			updateParentCase(); // NBA229
		}
		//end SPR3611
		// APSL5024
		if (!NbaUtils.isBlankOrNull(getStatusProvider().getReason())) {
			addComment(getStatusProvider().getReason());
		}
		// NBLXA-2133 start
		String pdrNIGOMSG = null;
		if (requirementInfo!=null && requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT) {
			Date deliveryReceiptSignDate = getWork().getNbaLob().getDeliveryReceiptSignDate();
			if (NbaUtils.isBlankOrNull(deliveryReceiptSignDate)) {
				RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
				if (reqInfoExt != null) {
					deliveryReceiptSignDate = reqInfoExt.getDeliveryReceiptSignDate();
				}
			}
			Date recievedDate = requirementInfo.getReceivedDate();
			if (NbaUtils.isPDR_NIGO(deliveryReceiptSignDate, recievedDate)) {
				if (deliveryReceiptSignDate == null) {
					pdrNIGOMSG="Delivery Receipt Signed Date is blank";
				}
				if (deliveryReceiptSignDate != null && (NbaUtils.compare(deliveryReceiptSignDate, recievedDate) > 0)) {
					pdrNIGOMSG="Delivery Receipt Signed Date can not be greater than Received date";
				}
				if (recievedDate != null && deliveryReceiptSignDate != null
						&& (NbaUtils.compare(deliveryReceiptSignDate, NbaUtils.addDaysToDate(recievedDate, -60)) < 0)) {
					pdrNIGOMSG="Delivery Receipt Signed Date can not be older than 60 days of received date";
				}
	     	}
			if (!NbaUtils.isBlankOrNull(pdrNIGOMSG)) {
				addComment(pdrNIGOMSG);
				changeStatus(getAlternateStatus());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));
				getWorkLobs().setArchivedStatus(getWorkLobs().getStatus());
			}
		}
		//NBLXA-2133 End
		//update the work item 
		updateWork();
		doContractUpdate(nbaTxLife);//NBLXA186-NBLXA1271
		//suspend work item but it must be called after update
		if (suspendWork) {		//SPR3611
			suspendAWD();
		}		//SPR3611
		
		//unlock work items
		unlockAWD();
		//ALS4932 code deleted
	
	
		// NBA008 code added end
		return getResult();
	}
	/**
	 * Create and initialize an <code>NbaVpmsResultsData</code> object to
	 * find any matching criteria.
	 * @param entryPoint the VP/MS model's entry point
	 * @return com.csc.fsg.nba.vo.NbaVpmsResultsData
	 */
	protected NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
	    try {
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS); //SPR3362
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			if (null != getParentCaseLobs()) {  //ALS4818
				deOinkMap.put("A_ParentApplicationType",getParentCaseLobs().getApplicationType());  //ALS4818
			}   //ALS4818
			deOinkImpairmentValues(deOinkMap); //AXAL3.7.07
			deOinkRequirementReviewedInd(getWorkLobs(),deOinkMap);//QC#8918
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			//SPR3362 code deleted
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("Aggeregate problem", re);
		//begin SPR3362
		} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
			}
		}
		//end SPR3362
	}
	/**
	 * Get all dependencies,Suspend days from vpms for a requirement.
	 * Populate dependent requirement type code in a List, set Suspend day 
	 * retrieve all dependant work items from awd.
	 * 
	 * @return List of all dependent requirement type
	 */
	// NBA008 New Method
	protected List getDependencies() throws NbaBaseException {

		List dependWorkItemList = new ArrayList();
		List list = null;
		List suspendModelResults = null; //SPR3611
		try {
			//call vpms to get all dependencies
			list = getDataFromVpms(NbaVpmsAdaptor.EP_DEPEND_REQ_CODES).getResultsData();
			if (list == null || list.size() == 0) {
				return dependWorkItemList;
			}
			//Begin ALS5189
			if(list.size()== 1){
				if(((String)list.get(0)).length()==0){
					return dependWorkItemList;
				}
			}
			//End ALS5189
			//call vpms to get suspend days
			suspendModelResults = getDataFromVpms(NbaVpmsAdaptor.EP_AGGREGATE_SUSPEND_DAYS).getResultsData(); //SPR3611
			if (suspendModelResults != null && suspendModelResults.size() > 0) { //SPR3611
				setSuspendDays(Integer.parseInt(suspendModelResults.get(0).toString())); //SPR3611
			}
		} catch (NbaBaseException be) {
			throw new NbaVpmsException("Error during VPMS call", be);
		}

		//get requirement control source from case
		NbaSource source = getParentCase().getRequirementControlSource();
		if (source == null) {
			throw new NbaBaseException(NO_REQ_CTL_SRC); //NBA050
		}
		//get all requirements on the requirement control source
		NbaXMLDecorator reqSrcCntr = new NbaXMLDecorator(source.getText());
		List allReq = reqSrcCntr.getInsurableParty(Integer.toString(getWorkLobs().getReqPersonSeq()), getWorkLobs().getReqPersonCode())
				.getRequirement(); //SPR2992

		//get all dependant requirement work items awdId
		List awdIdList = new ArrayList();
		Requirement req = null;
		for (int i = 0; i < allReq.size(); i++) {
			req = (Requirement) allReq.get(i);
			for (int j = 0; j < list.size(); j++) {
				if (req.getCode() == Integer.parseInt((String) list.get(j))) {
					awdIdList.add(req.getAwdId());
					break;
				}
			}
		}

		if (awdIdList.size() > 0) {
			//get all dependant work items from awd
			//NBA213 deleted code
			//create ans set retrive option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.setLockWorkItem();
			retOpt.requestSources();
			retOpt.setAutoSuspend();
			//get dst list
			dependWorkItemList = retrieveWorkItemList(getUser(), retOpt, awdIdList);  //NBA213
			//add list to unlock list
			//ALS4932 code deleted
			//NBA213 deleted code
		}
		return dependWorkItemList;
	}
	//ALS4932 override parent method
	protected List retrieveWorkItemList(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt, List workItemIdList) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		List dstList = new ArrayList();
		Iterator it = workItemIdList.iterator();
		String id;
		boolean caseInd;
		while (it.hasNext()) {
			id = (String) it.next();
			caseInd = "C".equals(id.substring(26, 27));
			retOpt.setWorkItem(id, caseInd);
			AccelResult accelResult = (AccelResult) currentBP.callBusinessService("NbaRetrieveWorkBP", retOpt);
			NewBusinessAccelBP.processResult(accelResult);
			NbaDst dst = (NbaDst) accelResult.getFirst();
			dstList.add(dst);  //add to the unlock list as soon as we retrieve so if a subsequent lock fails, we can unlock work already locked
			getUnlockList().add(dst); 
		}
		return dstList;
	}
	/**
	 * Answer dependent requirement array list
	 * @return java.util.List
	 */
	//NBA008 New Method
	protected java.util.List getDepenedReqList() {
		return depenedReqList;
	}
	//SPR3611 code deleted
	/**
	 * Answer suspend days
	 *
	 * @return int
	 */
	//NBA008 New Method
	protected int getSuspendDays() {
		return suspendDays;
	}
	/**
	 * answer whether case is disposed or not.
	 * 
	 * @return true if case is disposed
	 */
	//NBA008 New Method
	protected boolean isCaseDisposed() throws NbaBaseException {
		if (getParentCaseLobs().getCaseFinalDispstn() == 0) {  //SPR2992
			return false;
		}
		return true;
	}
	/**
	 * Check the requirement status LOB and return true if requirement is receipted or waived.
	 *
	 * @param workItem - A requirement 
	 * @return true is requirement is receipted or waived.
	 */
	//NBA008 New Method //method name changed for ACN010
	protected boolean isReqReceiptedOrWaivedOrCompleted(NbaDst workItem) {
		String reqStatus = workItem.getNbaLob().getReqStatus();  //SPR2992
		if (OLI_REQSTAT_RECEIVED.equals(reqStatus) || OLI_REQSTAT_WAIVED.equals(reqStatus) || OLI_REQSTAT_COMPLETED.equals(reqStatus)
                || OLI_REQSTAT_CANCELLED.equals(reqStatus)) { //ACN010, SPR2992 SPR3046
            return true;
        }
		return false;
	}
	/**
	* This method process the dependent requirements in following manner:
	* - Check for receipt and waived status
	* - if not receipt check for original work item suspend status
	* 		- if previously suspended return true
	*		- else add work item to suspend map and return false
	* - else check suspend days and current queue for dependent requirement
	*		- if suspended and in current queue add work item to unsuspend map
	*		- get pass status from vpms
	*		- change work item status to pass status
	* - return true
	* @return boolean
	*/
	//NBA008 New Method
	protected boolean processDependencies() throws NbaBaseException {

		List reqList = getDepenedReqList();

		for (int i = 0; i < reqList.size(); i++) {
			NbaDst item = (NbaDst) reqList.get(i);
			if (isReqReceiptedOrWaivedOrCompleted(item)) {//ACN010
				//unsuspend if currently suspend
				if (item.isSuspended() && item.getQueue().equals(getWork().getQueue())) {
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(item.getID());
					unsuspendList.add(suspendVO);
				}
				//change the work item status to pass status
				//assuming that underwriting review required indicator will be same for all dependant requirements
				//begin SPR3046
				String reqStatus = item.getNbaLob().getReqStatus();
				if ((OLI_REQSTAT_RECEIVED.equals(reqStatus) || OLI_REQSTAT_COMPLETED.equals(reqStatus)) && //ALS4879
						( NbaConstants.A_QUEUE_AGGREGATE_CONTRACT.equalsIgnoreCase(item.getQueue()) ||  //CR59174
								NbaConstants.A_QUEUE_AGGREGATE_1035.equalsIgnoreCase(item.getQueue()))) { //if receipted //ALS4879  CR59174
                    changeStatus(item, getPassStatus()); // NBA097
                } 
                //end SPR3046
			} else {
				//get requirement source control and check for previous supension
				NbaSource source = getWork().getRequirementControlSource();
				NbaXMLDecorator reqSource = new NbaXMLDecorator(source.getText());
				AutomatedProcess process = reqSource.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639

				// if original work item previously suspended
				if (process != null && process.hasSuspendDate()) {
					//clear unsuspend list
					unsuspendList.clear();
					//clear dependent requriement list
					depenedReqList.clear();
					return true;
				} else {
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(getWork().getID());
					GregorianCalendar calendar = new GregorianCalendar();
					Date currDate = new Date();
					calendar.setTime(currDate);
					calendar.add(Calendar.DAY_OF_WEEK, getSuspendDays());
					suspendVO.setActivationDate(calendar.getTime());
					//set requirement source control for suspend days
					if (process == null) {
						process = new AutomatedProcess();
						process.setProcessId(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
						reqSource.getRequirement().addAutomatedProcess(process);
					}
					process.setSuspendDate(currDate);
					source.setText(reqSource.toXmlString());
					source.setUpdate();
					suspendList.add(suspendVO);
					return false;
				}
			}

		}
		return true;
	}
	/**
	 * Retrieve the original work item with sources
	 * 
	 */
	// NBA008 New Method
	protected void retrieveWork() throws NbaBaseException {
		//NBA213 deleted code
		//create and set retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.setLockWorkItem();
		retOpt.requestSources();
		setWork(retrieveWorkItem(getUser(), retOpt));  //NBA213
		//ALS4932 code deleted
		//NBA213 deleted code
	}
	/**
	 * Set dependent requirement list
	 * @param newDepenedReqList - requirement array list 
	 */
	//NBA008 New Method
	protected void setDepenedReqList(java.util.List newDepenedReqList) {
		depenedReqList = newDepenedReqList;
	}
	/**
	 * set the parent case dst.
	 * 
	 * @param newParentCase NbaDst which represent a awd case
	 */
	//NBA008 New Method
	protected void setParentCase(NbaDst newParentCase) {
		parentCase = newParentCase;
	}
	/**
	 * set suspend dyas.
	 * 
	 * @param newSuspendDays suspend days.
	 */
	//NBA008 New Method
	protected void setSuspendDays(int newSuspendDays) {
		suspendDays = newSuspendDays;
	}
	/**
	 * suspend the work item in suspend list to AWD
	 */
	//NBA008 New Method
	protected void suspendAWD() throws NbaBaseException {
		//NBA213 deleted code
		for (int i = 0; i < suspendList.size(); i++) {
			suspendWork(getUser(), (NbaSuspendVO) suspendList.get(i));  //NBA213
		}
		//NBA213 deleted code
	}
	/**
	 * unlock the AWD.
	 */
	//NBA008 New Method
	protected void unlockAWD() throws NbaBaseException {
		//NBA213 deleted code
		//unlock all work items
		for (int i = 0; i < unlockList.size(); i++) {
			unlockWork(getUser(), (NbaDst) unlockList.get(i));  //NBA213
		}
		//NBA213 deleted code
	}
	/**
	 * unsuspend the work item in unsuspend list to AWD
	 */
	//NBA008 New Method
	protected void unsuspendAWD() throws NbaBaseException {
		//NBA213 deleted code
		for (int i = 0; i < unsuspendList.size(); i++) {
			unsuspendWork(getUser(), (NbaSuspendVO) unsuspendList.get(i));  //NBA213
		}
		//NBA213 deleted code
	}
	/**
	 * update the work item to the AWD.
	 * 
	 * @param work - A case or transaction.
	 */
	//NBA008 New Method
	protected void updateAWD(NbaDst workItem) throws NbaBaseException {
			updateWork(getUser(), workItem);  //NBA213
	}
	/**
	 * update the work items to the AWD.
	 * 
	 * @param dstList - A list of NbaDst objects.
	 */
	//NBA008 New Method
	protected void updateAWD(List dstList) throws NbaBaseException {
		for (int i = 0; i < dstList.size(); i++) {
			updateAWD((NbaDst) dstList.get(i));
		}
	}

	//SPR3611 code deleted
	/**
	 * Returns true if all other requirements attached to the case are not pending a 
	 * review by either the system or a user.
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA136 New Method
	//SPR3611 added transactions parameter
	protected boolean isLastNonReviewedRequirementReceived(List transactions) throws NbaBaseException {
		NbaTransaction nbaTrans = null;
		//SPR3611 code deleted
		int count = transactions.size();
		for (int i = 0; i < count; i++) {
			nbaTrans = (NbaTransaction) transactions.get(i);
			if (A_WT_REQUIREMENT.equals(nbaTrans.getWorkType()) && !getWork().getID().equals(nbaTrans.getID())) {
				if (isRequirementReviewNeeded(nbaTrans)) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Determines if the requirement's review is still pending.  Returns true if the requirement
	 * requires review and is not cancelled, waived, or received and not previously reviewed.
	 * @param reqTrx requirement work item
	 * @return
	 * @throws NbaBaseException 
	 */
	//NBA136 New Method
	protected boolean isRequirementReviewNeeded(NbaTransaction reqTrx) throws NbaBaseException {
		NbaLob lobs = reqTrx.getNbaLob();
		if (!lobs.getReqMedicalType()) { // APSL3512 CHAUG004
			return false;
		}
		RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(lobs.getReqUniqueID());
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if (reqInfoExt != null) {
			if (REVIEW_SYSTEMATIC.equals(reqInfoExt.getReviewCode()) || REVIEW_USER_REQUIRED.equals(reqInfoExt.getReviewCode())) {
				String reqStatus = lobs.getReqStatus();
				if (OLI_REQSTAT_CANCELLED.equals(reqStatus) || OLI_REQSTAT_WAIVED.equals(reqStatus)) {
					return false;
				}
				if (OLI_REQSTAT_RECEIVED.equals(reqStatus) && !NbaUtils.isBlankOrNull(lobs.getUndwrtQueue())) { //ALII991
					return !reqInfoExt.getReviewedInd();
				}
				try {
					if(OLI_REQSTAT_RECEIVED.equals(reqStatus) && NbaUtils.isBlankOrNull(lobs.getUndwrtQueue())){ //ALII991 APSL3512 CHAUG004 Deleted Condition
						return false;
					}
				} catch(Exception e){
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Determines if the requirement having Medical indicator set.
	 * @param currentReq last requirement's nbaLob
	 * @return true if medical indicator is true otherwise false
	 */
	//ALS5588 ALS5625 New Method
	protected boolean isMedicalInd(NbaLob currentReq)
	{
		RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(currentReq.getReqUniqueID());
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if (reqInfoExt !=null) {
			if (reqInfoExt.getMedicalIndicator()) {
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}
	
	

	/**
	 * Returns the <code>NbaLob</code> for the parent case work item.
	 * @return
	 * @throws NbaBaseException
	 */
	// SPR2992 New Method
	protected NbaLob getParentCaseLobs() throws NbaBaseException {
		if (parentCaseLobs == null) {
			if (getParentCase() != null) {
				parentCaseLobs = getParentCase().getNbaLob();
			}
		}
		return parentCaseLobs;
	}
	
	/**
	 * Reinitializes the vpms status fields after retrieving the parent case.
	 * @throws NbaBaseException
	 */
	//NBA192 New Method
	private boolean initializeVpmsStatus() throws NbaBaseException {
		Map deOink = new HashMap();
		String undrQueue = getUnderwriter(getParentCaseLobs()); // QC9307/APSL2344
		boolean outstandingPrint = isOutstandingContractPrint(getNbaTxLife().getPrimaryHolding().getPolicy().getPolNumber());// APSL4869
		deOink.put("A_OutstandingPrint", String.valueOf(outstandingPrint));
		deOink.put("A_UndwrtQueueLOB", undrQueue); // SPR3611 QC9307/APSL2344
		deOinkRequirementReviewedInd(getWorkLobs(), deOink);// APSL3733
		deOink.put("A_CseFnlDispstnLOB", String.valueOf(getParentCaseLobs().getCaseFinalDispstn()));// APSL5112
		deOink.put("A_ReinVendorIDLOB", String.valueOf(getWorkLobs().getReinVendorID()));// APSL5321
		deOink.put("A_PaidChgCMQueueLOB", String.valueOf(getParentCaseLobs().getPaidChgCMQueue()));// NBLXA-1283
		deOink.put("A_CaseManagerQueueLOB", String.valueOf(getParentCaseLobs().getCaseManagerQueue()));// NBLXA-1696
		setStatusProvider(new NbaProcessStatusProvider(getUser(), getWork(), getNbaTxLife(), deOink));
		return true;
	}
	/**
	 * Return the allRequirementsReviewed value.
	 */
	//SPR3611 New Method
	protected boolean allRequirementsReviewed() {
		return this.allRequirementsReviewed;
	}
	/**
	 * Set the value of allRequirementsReviewed.
	 * @param allRequirementsReviewed The allRequirementsReviewed to set.
	 */
	//SPR3611 New Method
	protected void setAllRequirementsReviewed(boolean allRequirementsReviewed) {
		this.allRequirementsReviewed = allRequirementsReviewed;
	}
	/**
	 * Return the reviewRequired value.
	 */
	//SPR3611 New Method
	protected boolean isReviewRequired() {
		return this.reviewRequired;
	}
	/**
	 * Set the value of reviewRequired.
	 */
	//SPR3611 New Method
	protected void setReviewRequired(boolean reviewRequired) {
		this.reviewRequired = reviewRequired;
	}
	/**
	 * Return the value of unlockList.
	 */
	//SPR3611 New Method
	protected List getUnlockList() {
		return this.unlockList;
	}
	/**
	 * Set the value of unlockList.
	 */
	//SPR3611 New Method
	protected void setUnlockList(List unlockList) {
		this.unlockList = unlockList;
	}
	/**
	 * Return the value of suspendList.
	 */
	//SPR3611 New Method
	protected List getSuspendList() {
		return this.suspendList;
	}
	/**
	 * Set the value of suspendList.
	 */
	//SPR3611 New Method
	protected void setSuspendList(List suspendList) {
		this.suspendList = suspendList;
	}
	/**
	 * Return the value of unsuspendList.
	 */
	//SPR3611 New Methodv
	protected List getUnsuspendList() {
		return this.unsuspendList;
	}
	/**
	 * Set the value of unsuspendList.
	 */
	//SPR3611 New Method
	protected void setUnsuspendList(List unsuspendList) {
		this.unsuspendList = unsuspendList;
	}
	/**
	 * Calls NbaWorkCompleteStatusProvider to return the outgoing status and the priority based on the UNDQ
	 * LOB value of current workitem.
	 * @return the workCompleteStatusProvider.
	 * @throws NbaBaseException
	 */
	//SPR3611 New Method
	protected NbaProcessWorkCompleteStatusProvider getWorkCompleteStatusProvider() throws NbaBaseException {
		if (this.workCompleteStatusProvider == null) {
			Map deOink = new HashMap(3, 1);
			// Begin QC9307/APSL2344
			String undrQueue = getUnderwriter(getParentCaseLobs());			
			deOink.put("A_UndwrtQueueLOB", undrQueue);
			//begin APSL3976 
			deOink.put("A_LastReceivedRequirement", String.valueOf(getWorkLobs().getReqType()));
			ApplicationInfo appinfo = null;
			if (getNbaTxLife().getPolicy() != null) {
				appinfo = getNbaTxLife().getPolicy().getApplicationInfo();
			}
			if (appinfo != null && appinfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_TRIAL
					&& getParentCaseLobs().getDistChannel() == NbaOliConstants.OLI_DISTCHNNL_BD) { 
				int aPSPageCount = getAPSPageCount(getWorkLobs());
				if(aPSPageCount > 0){
					deOink.put("A_APSPAGECOUNT", String.valueOf(aPSPageCount));
				}
			}
			//End APSL3976
			//CR1345857(APSL2575)
			long issuType = getNbaTxLife().getPolicy().getIssueType();
			deOink.put("A_IssueType", String.valueOf(issuType));
			getParentCaseLobs().setUndwrtQueue(undrQueue);
			getParentCaseLobs().setActionUpdate();
			// End QC9307/APSL2344
			this.workCompleteStatusProvider = new NbaProcessWorkCompleteStatusProvider(getUser(), getParentCaseLobs(), deOink);
		}
		return this.workCompleteStatusProvider;
	}
	/**
	 * Set the value of parentCaseLobs
	 * @param nbaLob
	 */
	//SPR3611 New Method
	private void setParentCaseLobs(NbaLob nbaLob) {
		this.parentCaseLobs = nbaLob;
	}
	/**
	 * Return the NbaDst for the Case and Sources.
	 * 
	 * If the NbaDst has not been retrieved yet:
	 * - retrieve the Case, Sources and sibling Transactions of the current Transaction from the workflow system.
	 * - if the Case will be updated, lock the Case
	 * 
	 * The Case will be updated if this Requirement requires review and any of the following are true:
	 *  (a) All other requirements attached to the Case are not pending a review by either the system or a user.
	 *      This causes the last non-reviewed requirement received (RQLS) LOB to be set to true. 
	 *  (b) The parent Case is in the Underwriter Hold Queue (NBUNDHLD)
	 *       This causes the Case status, routing reason, and priority to be updated
	 *  (c) This Requirement has not been reviewed yet and the requirement type is considered a significant requirement by the autoprocess status VPMS model. 
	 * 		 This causes the significant requirement (SRQR) LOB to set to true
	 * 
	 * @return NbaDst - the NbaDst for the Case and Sources
	 */
	// SPR3611 New Method
	//ALS3972 refactor method
	protected NbaDst getParentCase() {
		return this.parentCase;
	}

	protected void updateCaseTransactions() throws NbaBaseException {
		if (this.parentCase != null) {
			//ALS4932 code deleted
			//set whether a the case is having any REEVAL work item which is not in END queue
			if(setAnyReevalWorkOpen(this.parentCase.getNbaTransactions())){
				setAnyOpenReevalWork(true);	
			} else {
				boolean outstandingReq = isOutstandingRequirements();  //NBLXA-1371	
				if (outstandingReq || getWorkLobs().getReview() != REVIEW_NOT_REQUIRED) {
					//Set the requirements reviewed value based on whether all sibling Requirements have been reviewed
					//setAllRequirementsReviewed(isLastNonReviewedRequirementReceived(getWorkLobs())); //ALS5588 // ALS5625 //ALS3972//commented for NBLXA186 -NBLXA1271
					setAllRequirementsReviewed(checkAllRelatedPartyReqReceived(getWorkLobs()));//Added for NBLXA186-NBLXA1271
				}
				if (outstandingReq) {//ALS5588
					//Set the review required value based on whether the work item indicates that that review is required and the parent case is in in
					// the underwriter hold queue
					setReviewRequired(NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equals(getParentCaseLobs().getQueue()));
				}
			}
			//code deleted ALS5588,ALS5625
			this.parentCase.getTransactions().clear();//Transactions are not needed beyond this point
			//ALS4932 code deleted
			//ALS4932..for one, retrieveParentCase locks the case already.
			//two, let the framework catch the locked exception and handle properly
		}
		//ALS4531 Code deleted
	}

	/**
	 * Calls NbaConfiguration to get activate date for a workitem, and returns suspendVO object for this work item.
	 * @throws NbaBaseException
	 */
	//ALS2575 new method
	protected NbaSuspendVO getSuspendWorkVO() throws NbaBaseException {
        GregorianCalendar cal = new GregorianCalendar();
        NbaSuspendVO suspendVO = new NbaSuspendVO();
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.LOCKED_CHILD_SUSPEND)));
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(cal.getTime());
		suspendVO.setSuspendCode(NbaConstants.AWD_SUSPEND_REASON_NOWORK);
		//ALS3972 code removed outside
		return suspendVO;
	}
	/**
	 * Update the Case if necessary:
	 * 
	 * The Case will be updated if this Requirement requires review and any of the following are true:
	 *  (a) All other requirements attached to the Case are not pending a review by either the system or a user.
	 *      This causes the last non-reviewed requirement received (RQLS) LOB to be set to true. 
	 *  (b) The parent Case is in the Underwriter Hold Queue (NBUNDHLD)
	 *       This causes the Case status, routing reason, and priority to be updated
	 *  (c) This Requirement has not been reviewed yet and the requirement type is considered a significant requirement by the autoprocess status VPMS model. 
	 * 		 This causes the significant requirement (SRQR) LOB to set to true
	 * @throws NbaBaseException
	 * 
	 * @throws NbaBaseException
	 **/
	//SPR3611 New Method
	//ALS4947 modified the method
	protected void updateParentCase() throws NbaBaseException {
		//NBA229 code deleted
		//ALS4947 Begin
		try {
			NbaLob lob = getParentCaseLobs();
			addRequirementsReceivedToApplication(); // APSL5055
			//Begin Code commented APSL5055 
			/*	int seq = 1;
			//ALS3972 code deleted
			while (lob.getReqTypeAt(seq) != 0) {
				seq++;
			}
			if (getWork().getNbaLob().getReqMedicalType()) { //ALS4947 Only for underwriting Requirements
				lob.setReqTypeAt(getWork().getNbaLob().getReqType(), seq++);
				//ALS3972 code deleted
			}//ALS4947
			if (getDepenedReqList() != null && getDepenedReqList().size() > 0) {
				int size = getDepenedReqList().size();
				for (int i = 0; i < size; i++) {
					if (((NbaDst) getDepenedReqList().get(i)).getNbaLob().getReqMedicalType()) { //ALS4947 Only for underwriting Requirements
						lob.setReqTypeAt(((NbaDst) getDepenedReqList().get(i)).getNbaLob().getReqType(), seq++);
						//ALS3972 code deleted
					}//ALS4947
				}
<<<<<<< HEAD
			}*/
			// END Code commented APSL5055
			if (!isCaseDisposed()) { //NBA229
				if (isReviewRequired() || allRequirementsReviewed()) {//ALS4947 //ALS3972 //ALS5588
					if (NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equals(getParentCaseLobs().getQueue()) && !isOutstandingBundleReq()) {//ALS3972 NBLXA -1983
						String previousStatus = getParentCase().getStatus();

					}
				}
			}
			// End NBA229
			long confStatus = -1L ;
			if(getNbaTxLife() != null && getNbaTxLife().getPolicy() != null){						
				PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
				if(policyExt != null){
					confStatus = policyExt.getIllustrationStatus();
				}
			}
			String previousStatus = getParentCase().getStatus();
			if (!isCaseDisposed() && !(confStatus == NbaOliConstants.OLIEXT_LU_ILLUSSTAT_NCONF &&  previousStatus.equals(NbaConstants.UWHOLD_STATUS_CONFORMING))) { //NBA229 NBLXA-2155[NBLXA-2265]				
				if ((isReviewRequired() || allRequirementsReviewed())) {//ALS4947 //ALS3972 //ALS5588
					if (NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equals(getParentCaseLobs().getQueue()) && !isOutstandingBundleReq()) {//ALS3972 NBLXA -1983	
						getParentCase().setStatus(getWorkCompleteStatusProvider().getStatus());
						//Begin APSL5152
						if (getNbaTxLife() != null && getNbaTxLife().isInformalApplication() && !isRiskRighterCase()
								&& previousStatus.equals(NbaConstants.UWHOLD_STATUS) && getWork().getNbaLob().getReqMedicalType()) {
							setRouteReason(getParentCase(), getWorkCompleteStatusProvider().getStatus(), "Case Ready for Initial Underwriting"); 
							NbaUtils.addGeneralComment(getParentCase(), getUser(), "Case Ready for Initial Underwriting");
						} else { //End APSL5152
							setRouteReason(getParentCase(), getWorkCompleteStatusProvider().getStatus(), getWorkCompleteStatusProvider().getReason());// ALS5260
							NbaUtils.addGeneralComment(getParentCase(), getUser(), getWorkCompleteStatusProvider().getReason());// ALS5260,ALS5337
						}

						getParentCaseLobs().setUnderwriterActionLob(NbaOliConstants.OLI_UW_SUBSEQUENT_ACTION);//APSL4981
						String action = getWorkCompleteStatusProvider().getPriorityAction();
						String priority = getWorkCompleteStatusProvider().getPriorityValue();
						if (action != null && action.trim().length() > 0 && priority != null && priority.trim().length() > 0) {
							getParentCase().increasePriority(action, priority);
						}
					}
				}
				//Begin : Commented for NBLXA-1485
				/*if (allRequirementsReviewed()) {
					getParentCaseLobs().setLstNonRevReqRec(true);
					getParentCaseLobs().setSigReqRecd(true); //ALS5588,ALS5625
				}*/
				//End:Commented for NBLXA-1485
				//Code deleted ALS5588,ALS5625
			}
			//ALS4947 End
	       // Start APSL3383
			//Start APSL3701
			ApplicationInfo appinfo = null;
			if (getNbaTxLife().getPolicy() != null) {
				appinfo = getNbaTxLife().getPolicy().getApplicationInfo();
			}//End APSL3701
			if (appinfo != null && appinfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_TRIAL
					&& getWorkLobs().getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT
					&& lob.getDistChannel() == NbaOliConstants.OLI_DISTCHNNL_CAPTIVE) { // APSL3701,APSL3813
				int aPSPageCount = getAPSPageCount(getWorkLobs());// APSL3426
				List resultData = getRiskRighterStatus(lob, aPSPageCount);// APSL3426
				if (!resultData.isEmpty()) {
					String retailRiskRighterStatus = (String) resultData.get(0); // APSL3426
					if (!NbaUtils.isBlankOrNull(retailRiskRighterStatus)) {
						getParentCase().setStatus(retailRiskRighterStatus);
					}
				}
			}
			//End APSL3383
			getParentCase().setUpdate();
			updateAWD(getParentCase());
			//ALS3972 removed the code from here
		} catch (NbaVpmsException e) {
			handleVpmsException(e);
		} catch (NbaBaseException e) {
			if (e.getMessage().toUpperCase().startsWith(NO_RIGHTS)) {
				getParentCase().setStatus(getVpmsErrorStatus());
				setRouteReason(getParentCase(), getVpmsErrorStatus());
				addComment("Notify system administrator. Could not determine outgoing underwriter status.");
			} else {
				e.forceFatalExceptionType();
				throw e;
			}
		} catch (Exception e) {
			NbaBaseException ex = new NbaBaseException(e);
			ex.forceFatalExceptionType();
			throw ex;
		}
	}
	/**
	 * Unsuspend the ReEval WI
	 * 
	 * @throws NbaBaseException
	 */
	//ALS5260 New Method
	protected void unsuspendReEvalWI()throws NbaBaseException {
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		if(reEvalTrans != null && reEvalTrans.isSuspended()){
			suspendVO.setTransactionID(reEvalTrans.getID());
			unsuspendWork(suspendVO);
		}
	}

	 
	/**
	 * Returns the significantRequirement.
	 */
	//SPR3611 New Method
	protected boolean isSignificantRequirement() {
		return this.significantRequirement;
	}
	/**
	 * Set the value of significantRequirement 
	 */
	//SPR3611 New Method
	protected void setSignificantRequirement(boolean significantRequirement) {
		this.significantRequirement = significantRequirement;
	}
	/**
	 * Retrieve the Case
	 * @throws NbaBaseException
	 */
	//SPR3611 New Method
	protected void retrieveParentCase() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.setLockParentCase(); //NBA229
		retOpt.requestSources();
		retOpt.requestTransactionAsSibling();
		this.parentCase = retrieveWorkItem(getUser(), retOpt); //Retrieve the Case, Sources and sibling Transactions 
		setParentCaseLobs(this.parentCase.getNbaLob());
		getUnlockList().add(this.parentCase); //ALS4932..since we are locking already
	}
	/**
	 * Lock the Case
	 * @throws NbaNetServerDataNotFoundException
	 * @throws NbaBaseException
	 */
	//SPR3611 New Method
	protected void lockParentCase() throws NbaNetServerDataNotFoundException, NbaBaseException {
		RetrieveWorkItemsRequest retrieveWorkItemsRequest = new RetrieveWorkItemsRequest();
		retrieveWorkItemsRequest.setUserID(getUser().getUserID());
		String id = this.parentCase.getCase().getItemID();
		retrieveWorkItemsRequest.setWorkItemID(id);
		retrieveWorkItemsRequest.setRecordType(id.substring(26, 27));
		retrieveWorkItemsRequest.setLockWorkItem();
		lockWorkItem(retrieveWorkItemsRequest);		 //Lock the Case
		this.parentCase.getCase().setLockStatus(getUser().getUserID());
		getUnlockList().add(this.parentCase);
	}
	/**
     * Determine if the Case should be updated.
     * The Case should be updated if:
     *  -
	 * @return boolean
	 */
	 //SPR3611 New Method 
	protected boolean isCaseUpdateNeeded() {
		return isReviewRequired() || allRequirementsReviewed(); //code modified ALS5588,ALS5625
	}
 
	/**
	 * Handle a NbaVpmsException. Create a Result with a Host error status and add a comment containing the exception message.
	 * @param e - the NbaVpmsException
	 */
	//SPR3611 New Method
	protected void handleVpmsException(NbaVpmsException e) {
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getVpmsErrorStatus(), getVpmsErrorStatus()));
		addComment(e.getMessage());
	}
	
	/**
	 * Handle a NbaBaseException. If the NbaBaseException is fatal, re-throw it. Otherwise, create a Result with a Host error status
	 * and add a comment containing the exception message.
	 * @param e - the NbaBaseException
	 */
	//SPR3611 New Method
	protected void handleNbaBaseException(NbaBaseException e) throws NbaBaseException {
		if (e.isFatal()) {
			throw e;
		}
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getHostErrorStatus(), getHostErrorStatus()));
		addComment(e.getMessage());
	}
	/**
	 * Call a VPMS model to determine if the current Requirement is a significant requirement
	 * @return a boolean based on the model result
	 * @throws NbaBaseException
	 */
	//SPR3611 New Method
	protected boolean getSignificFromModel() throws NbaBaseException {
		Map deOink = new HashMap(1, 1);
		deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
		NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.AUTO_PROCESS_STATUS);
		vpmsProxy.setSkipAttributesMap(deOink);
		vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_SIGNIFICANT_REQUIREMENT);
		try {
			VpmsComputeResult vpmsResult = vpmsProxy.getResults();			
			if (vpmsResult.getReturnCode() != 0 && vpmsResult.getReturnCode() != 1) {	//If a bad code is returned throw an exception 
				throw new NbaVpmsException(vpmsResult.getMessage());
			}
			return Boolean.valueOf(vpmsResult.getResult()).booleanValue();
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_GENERIC, re);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //Log and continue
			}
		}
	}
	
	/**
	 * Reinitializes the vpms status fields after retrieving the parent case.
	 * Map deOinkMap
	 */
	// AXAL3.7.07 new method
	protected void deOinkImpairmentValues(Map deOinkMap) throws NbaBaseException {

		int covCount = getNbaTxLife().getLife().getCoverageCount();
		int impCount = 0;
		
		//Begin APSL4109 
		LifeParticipant lifeParticipant = null;
		int personCode = getWorkLobs().getReqPersonCode();
		for (int i = 0; i < covCount; i++) {
			if (NbaOliConstants.OLI_REL_INSURED == personCode) {
				lifeParticipant = getLifeParticipant(getNbaTxLife().getLife().getCoverageAt(i), NbaOliConstants.OLI_PARTICROLE_PRIMARY);
			} else if (NbaOliConstants.OLI_REL_JOINTINSURED == personCode) {
				lifeParticipant = getLifeParticipant(getNbaTxLife().getLife().getCoverageAt(i), NbaOliConstants.OLI_PARTICROLE_JOINT);
			}
			if (lifeParticipant != null) {
				String partyID = lifeParticipant.getPartyID();
				ArrayList impairments = getNbaTxLife().getImpairments(partyID);
				for (int j = 0; j < impairments.size(); j++) {
					ImpairmentInfo impairmentInfo = (ImpairmentInfo) impairments.get(j);
					if (impCount == 0) {
						deOinkMap.put("A_ImpairmentStatus", String.valueOf(impairmentInfo.getImpairmentStatus()));
						deOinkMap.put("A_ImpairmentUserID", String.valueOf(impairmentInfo.getImpairmentUserID()));// ALS3972
					} else {
						deOinkMap.put("A_ImpairmentStatus[" + impCount + "]", String.valueOf(impairmentInfo.getImpairmentStatus()));
						deOinkMap.put("A_ImpairmentUserID[" + impCount + "]", String.valueOf(impairmentInfo.getImpairmentUserID()));// ALS3972
					}
					impCount++;
				} // end for
			}//End APSL4109
		} // end for
		deOinkMap.put("A_no_of_ImpairmentS", String.valueOf(impCount));
		
	}
	/**
	 * Call VPMS to determine if there are outstanding impairments needing UW review.
	 * throws NbaBaseException
	 */
	// AXAL3.7.07 new method
	protected boolean isOutstandingRequirements() throws NbaBaseException { //NBLXA-1371
		boolean outstandingRequirements = false; //NBLXA-1371

		List needUWReviewModelResults = getDataFromVpms(NbaVpmsAdaptor.EP_OUTSTANDING_REQUIREMENTS).getResultsData();
		if (needUWReviewModelResults != null && needUWReviewModelResults.size() > 0) {
			if (needUWReviewModelResults.get(0).toString().equals("1")) {
				outstandingRequirements = true;
			}
		}
		return outstandingRequirements;
	}
	

	/**
	 * Verifies if CharlesBaileyAgent is present on the case
	 * @param producerid
	 * @return true if CharlesBaileyAgent is present on the case
	 *  NBLXA -1983
	 */
	protected boolean isCharlesBaileyAgent(String producerid)  {
		boolean isCharlesBaileyAgentID = false;
		NbaVpmsResultsData agentData = getDataFromVPMSForIdentifyingAgent(NbaVpmsConstants.CONTRACTVALIDATIONCALCULATIONS, NbaVpmsConstants.EP_AGENTVALIDATION,
				producerid);
		if (agentData != null && agentData.getResultsData() != null) {
			isCharlesBaileyAgentID = Boolean.parseBoolean(((String) agentData.getResultsData().get(0)));
		}
		return isCharlesBaileyAgentID;
	}
	
	/**
	 * Calls VPMS model to fetch agentData
	 * @param modelName
	 * @param entryPoint
	 * @param producerID
	 * @return agentData
	 * New Method NBLXA -1983
	 */
	public NbaVpmsResultsData getDataFromVPMSForIdentifyingAgent(String modelName, String entryPoint, String producerID) {
		NbaVpmsAdaptor adapter = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(new NbaLob());
			adapter = new NbaVpmsAdaptor(oinkData, modelName);
			Map deOinkMap = new HashMap();
			deOinkMap.put("A_PRODUCERID", producerID);
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			return vpmsResultsData;
		} catch (Exception e) {
			getLogger().logDebug("Problem in getting valid agent from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
				th.printStackTrace();
			}
		}
		return null;
	}
	

	/**
	 * It Bundle Certain Requirements Before Sending to UW. If all the bundle requirements are not 
	 * received on a case for a particular party  it does not sent the case to UW from UWhold.
	 * @return  true If all the bundle requirements are not received on a case for a particular party
	 *  NBLXA -1983 New method  	
	 */
	protected boolean isOutstandingBundleReq(){ 
		boolean outstandingBundleReq = false; 
		boolean isBundlingRequirement = false; 
		Long appType=getNbaTxLife().getPolicy().getApplicationInfo().getApplicationType();
		ApplicationInfo appInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if(NbaOliConstants.OLI_ACTTYPE_1000500003 !=appType && NbaOliConstants.OLI_APPTYPE_SIMPLIFIEDISSUE !=appType  && (!NbaUtils.isBlankOrNull(appInfoExt) && !NbaUtils.isContractChange(getNbaTxLife()) && NbaUtils.isBlankOrNull(appInfoExt.getReopenDate()))){
			String producerid =NbaUtils.getProducerID(getNbaTxLife());
			if (!isCharlesBaileyAgent(producerid)) {
				RequirementInfo currReqInfo = getNbaTxLife().getRequirementInfo(getWorkLobs().getReqUniqueID());
				String partyID = currReqInfo.getAppliesToPartyID();
				List valueList = NbaUtils.getValRule(NbaConstants.VALRULE_BUNDLING_REQ).getValue();
				List requirementInfoList = getNbaTxLife().getRequirementInfoList(partyID ,valueList);
				for(int j=0; j< valueList.size(); j++){
					if(currReqInfo.getReqCode()==Long.parseLong(String.valueOf(valueList.get(j)))){
						isBundlingRequirement=true;
						break;
					}
				}
				if(isBundlingRequirement){
					for(int i=0;i<requirementInfoList.size();i++){
						RequirementInfo reqinfo = (RequirementInfo) requirementInfoList.get(i);
						if (currReqInfo.getId()!=reqinfo.getId() && (NbaUtils.isRequirementOutstanding(reqinfo.getReqStatus()))){
							outstandingBundleReq =true;
							break ;	
						}
					}
				}
			}
		}
		return outstandingBundleReq;
	}

	/**
	 * Verify if an open Reeval wokritem exists on the case. Calls VP/MS model to
	 * determine if the work item is in the end queue or not.
	 * @return true if an open Reeval wokritem exists on the case else return false.
	 * @throws NbaBaseException
	 */
	//ALS4596 new method
	protected boolean setAnyReevalWorkOpen(List transactions) throws NbaBaseException {
		int count = transactions.size();
		NbaTransaction nbaTransaction = null;
		boolean hasOpenReeval = false;
		for (int i = 0; i < count; i++) {
			nbaTransaction = (NbaTransaction) transactions.get(i);
			if (NbaConstants.A_WT_REEVALUATE.equalsIgnoreCase(nbaTransaction.getWorkType())
					&& !isReevalInEndQueue(nbaTransaction.getNbaLob())) {
				hasOpenReeval = true;
				reEvalTrans = nbaTransaction;//ALS5260
				break;
			}
		}
		return hasOpenReeval;
	}
	
	/**
	 * Calls VP/MS model to determine if an Reeval work item is in the end queue or not.
	 * @param nbaLob the work LOBS
	 * @return true if an Reeval is in the end queue else return false.
	 * @throws NbaBaseException
	 */
	//ALS4596 new method
	protected boolean isReevalInEndQueue(NbaLob nbaLob) throws NbaBaseException {
		NbaVpmsModelResult data = new NbaVpmsModelResult(getDataFromVpms(NbaVpmsConstants.QUEUE_STATUS_CHECK, NbaVpmsConstants.EP_GET_END_QUEUE_VERIFICATION, new NbaOinkDataAccess(nbaLob), null, null).getResult());
		boolean inEndQueue = true;
		if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
			ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
			if (resultData.getResult().size() > 0) {
				inEndQueue = Boolean.valueOf(resultData.getResultAt(0)).booleanValue();
			}
		}
		return inEndQueue;
	}	
	/**
	 * @return Returns the anyOpenReevalWork.
	 */
	//ALS4596 new method
	public boolean isAnyOpenReevalWork() {
		return AnyOpenReevalWork;
	}
	/**
	 * @param anyOpenReevalWork The anyOpenReevalWork to set.
	 */
	//ALS4596 new method
	public void setAnyOpenReevalWork(boolean anyOpenReevalWork) {
		AnyOpenReevalWork = anyOpenReevalWork;
	}
	
	/**
	 * suspend the current WI
	 * @throws NbaBaseException
	 */
	//ALS3972 New Method
	protected void suspendWI()throws NbaBaseException{ 
		this.suspendWork = true;
		NbaSuspendVO suspendVO = getSuspendWorkVO();
		suspendList.add(suspendVO);
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspend", "Suspend"));
	}
	

	/**
	 * Deoink Requirement ReviewedInd
	 * @param lobs
	 * @param deOinkMap
	 */
	//QC#8918
	protected void deOinkRequirementReviewedInd(NbaLob lobs, Map deOinkMap) {
		String reviewedInd = FALSE_STR;
		if (lobs != null) {
			RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(lobs.getReqUniqueID());
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			if (reqInfoExt != null) {
				reviewedInd = reqInfoExt.getReviewedInd() == true ? TRUE_STR : FALSE_STR;
			}
		}
		deOinkMap.put("A_ReviewedInd", reviewedInd);
	}
	
	//APSL4109 new method
	protected LifeParticipant getLifeParticipant(Coverage coverage, long lifeParticipantType) {
		LifeParticipant participant = null;
		if (coverage != null) {
			int sizeParticipant = coverage.getLifeParticipantCount();
			for (int i = 0; i < sizeParticipant; i++) {
				participant = coverage.getLifeParticipantAt(i);
				if (participant.getLifeParticipantRoleCode() == lifeParticipantType) {
					return participant;
				}
			}
		}
		return participant;
	}
	//APSL4869 New Method
	protected boolean isOutstandingContractPrint(String polNumber) throws NbaBaseException {
		NbaSearchVO searchPrintVO = searchWI(NbaConstants.A_WT_CONT_PRINT_EXTRACT, polNumber);
		if (searchPrintVO != null && searchPrintVO.getSearchResults() != null && !searchPrintVO.getSearchResults().isEmpty()) {
			List searchResultList = searchPrintVO.getSearchResults();
			for (int i = 0; i < searchResultList.size(); i++) {
				NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
				if (!(searchResultVo.getQueue().equalsIgnoreCase(END_QUEUE))) {
					return false;
				}
			}
		}
		return true;
	}
	
	protected NbaSearchVO searchWI(String workType, String policyNumber) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(policyNumber);
		searchVO = lookupWork(getUser(), searchVO);
		return searchVO;
	}
	//APSL4869 End
	
	//APSL4992 NEW METHOD
	protected void setTEXLobOnNegativeDisposedReq(){
		if (NbaUtils.isNegativeDisposition(this.parentCase)) {
			NbaLob reqlob = work.getNbaLob();
			NbaLob parentlob = this.parentCase.getNbaLob();
			reqlob.setDisplayIconLob(parentlob.getDisplayIconLob());

		}

	}
	//NBA331.1, APSL5055 New Method
	protected void addRequirementsReceivedToApplication() throws Exception {
		RequirementsReceivedUpdateRequest request = new RequirementsReceivedUpdateRequest();
		request.setApplicationWorkItemID(getParentCase().getID());
		if (getWork().getNbaLob().getReqMedicalType()) { 
			request.addRequirementWorkItem(String.valueOf(getWork().getNbaLob().getReqType()));
		}
		if (getDepenedReqList() != null && getDepenedReqList().size() > 0) {
			int size = getDepenedReqList().size();
			for (int i = 0; i < size; i++) {
				if (((NbaDst) getDepenedReqList().get(i)).getNbaLob().getReqMedicalType()) { //ALS4947 Only for underwriting Requirements
					request.addRequirementWorkItem(String.valueOf(((NbaDst) getDepenedReqList().get(i)).getNbaLob().getReqType()));
					//ALS3972 code deleted
				}//ALS4947
			}
		}
		Result result = new AccelResult();
		result.addResult(request);
		result = getCurrentBP().callService(ServiceCatalog.ADD_UW_REQTYPES_RECEIVED, result);
		//NewBusinessAccelBP.processResult(result);
	}	
	
	// APSL5152 : new method Returns true if case is RiskRighterCase
	protected boolean isRiskRighterCase() throws NbaBaseException {
		String riskRighterCase = getParentCaseLobs().getRiskRighterCase();
			if (riskRighterCase!= null && String.valueOf(NbaOliConstants.OLI_RISKRIGHTER_CASE).equalsIgnoreCase(riskRighterCase)) {
				return true;
			}
		return false;
	}	
	
	//NBLXA186-NBLXA1271
	protected boolean checkAllRelatedPartyReqReceived(NbaLob nbalob) throws NbaBaseException {
		boolean currentPartyAllReqReceived = false;
		boolean lastReqReceived = false;
		long roleCode = -1L;
		if (nbaTxLife != null && nbalob != null) {
			Policy policy = nbaTxLife.getPolicy();
			ApplicationInfo appInfo = policy.getApplicationInfo();
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (appInfoExt != null && NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER != appInfoExt.getUnderwritingApproval()) {
				RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(nbalob.getReqUniqueID());
				if (reqInfo != null) {
					String partyID = reqInfo.getAppliesToPartyID();
					currentPartyAllReqReceived = AxaReqIndicatorUtils.isAllValidReqReceived(nbaTxLife, partyID);
					getLogger().logInfo("Last requirement indicator status  for current party via aggregate  " + currentPartyAllReqReceived);
					if (currentPartyAllReqReceived) {
						lastReqReceived = true;
						if (nbaTxLife.getParty(partyID) != null) {
							Party party = nbaTxLife.getParty(partyID).getParty();
							if (party != null) {
								PartyExtension partyExtension = NbaUtils.getFirstPartyExtension(party);
								if (partyExtension != null) { // NBLXA-2313 
									long oldlastReqIndValue = partyExtension.getLastRequirementIndForParty(); // NBLXA-1432
									if (oldlastReqIndValue != NbaOliConstants.OLI_LU_LASTREQSTAT_COMPLETE) {
										partyExtension.setLastRequirementIndForParty(NbaOliConstants.OLI_LU_LASTREQSTAT_RECEIVED);
										partyExtension.setActionUpdate();
										// Begin: Modified for NBLXA-1485
										Relation relation = NbaUtils.getRelationForParty(party.getId(), nbaTxLife.getOLifE().getRelation().toArray());
										if (relation != null) {
											roleCode = relation.getRelationRoleCode();
										}
										if (!NbaUtils.isBlankOrNull(roleCode) && roleCode != NbaOliConstants.OLI_REL_OWNER
												&& roleCode != NbaOliConstants.OLI_REL_DEPENDENT) {
											appInfoExt.setLastRequirementInd(NbaOliConstants.OLI_LU_LASTREQSTAT_RECEIVED);
											appInfoExt.setActionUpdate();
											// Begin : Added for NBLXA-1682
											if (null != getParentCaseLobs()) {
												if (!(getParentCaseLobs().getLstNonRevReqRec())) {// NBLXA-2385
													getParentCaseLobs().setQueueEntryDate(new Date()); // NBLXA-2385
												}
												getParentCaseLobs().setLstNonRevReqRec(true);
												getParentCaseLobs().setSigReqRecd(true);

											}
											// End : Added for NBLXA-1682
											/*
											 * getWork().getNbaLob().setSigReqRecd(true); getWork().getNbaLob().setLstNonRevReqRec(true);
											 */// added for QC18821/APSL5385 //commented for NBLXA-1682
										}
										// End: Modified for NBLXA-1485
										// Adding automated comment for last requirement indicator
										String reqStatusTrans = "";
										reqStatusTrans = NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REQSTAT,
												Long.valueOf(reqInfo.getStatus()));
										getLogger().logInfo("Requiremenmt Status for current aggregated requirement " + reqStatusTrans);
										if (relation != null && oldlastReqIndValue != partyExtension.getLastRequirementIndForParty()) { // NBLXA-1432
											String roleTrans = NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REL, roleCode);
											String reqTrans = NbaUtils.getRequirementTranslation(String.valueOf(nbalob.getReqType()),
													nbaTxLife.getPolicy());
											NbaUtils.addAutomatedComment(getWork(), user, "Last Requirement Indicator for " + roleTrans
													+ "  has been changed to ON due to '" + reqTrans + "' requirement received on case. "); //NBLXA-1718 changed from general to automated
										}
									}
								}
						   }
						}
					}
				}
			}

		}
		return lastReqReceived;
	}
}