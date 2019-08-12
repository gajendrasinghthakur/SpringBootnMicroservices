
package com.csc.fsg.nba.business.process;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.UserSessionKey;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.SecureComment;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.MedicalCondition;
import com.csc.fsg.nba.vo.txlife.MedicalConditionExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;



/**
 * This class returns a proxy object based upon the given inputs to execute APFORMAL.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * 
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>SR564247</td><td>Discretionary</td><td>Predictive Analytics Switch</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Analytics Full Implementation</td></tr>
 * <tr><td>CR1345857(APSL2575)</td><td>Discretionary</td>Predictive CR - Aggregate</tr>
 * <tr><td>APSL3520</td><td>Discretionary</td>nbA Comments Improvements</tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */

public class NbaProcPredictiveAnalysis extends NbaAutomatedProcess {

	private static final String BP_RETRIEVE_COMPANION_CASES = "RetrieveCompanionCasesBP";
	
	//List of missing fields to be added with Secure Comment
	private List missingFieldsList = new ArrayList();
	
	private boolean isTransaction = false;
	
	private NbaDst originalTransaction = null;
	
	//Constant to execute Predictive first and second criteria
	private static final String FIRST_CRITERIA_VALUE = "1";
	
	private static final String SECOND_CRITERIA_VALUE = "2";	
	
	//Constant for VPMS Attributes to be deoink
	private static final String A_DELIMITER = "A_Delimiter";	
	
	private static final String A_COMPFACEAMOUNTLIST =  "A_CompFaceAmountList";
	
	private static final String A_COMPFACEAMOUNTLISTSIZE = "A_CompFaceAmountListSize";
	
	private static final String A_FORMNUMBERLIST = "A_FormNumberList";
	
	private static final String A_FORMNUMBERLISTSIZE = "A_FormNumberListSize";
	
	private static final String A_CRITERIAVALUE = "A_CriteriaValue";
	
	//Removed code - ALII1905,ALII1904
	
	//Starts ALII1905,ALII1904
	private static final String A_MED_CONDITION13 = "A_MedCondition13";
	
	private static final String A_MED_CONDITION12 = "A_MedCondition12";
	//End ALII1905,ALII1904
	
	private static final String A_REQTYPELIST = "A_ReqTypeList";
	
	private static final String A_REQTYPELISTSIZE = "A_ReqTypeListSize";
	
	private static final String A_NO_OF_REQUIREMENTS = "A_No_Of_Requirements";
	
	private static final String A_REQCODELIST = "A_ReqCodeList";
	
	private static final String A_REQSTATUSLIST = "A_ReqStatusList";
	
	private static final String A_PREDOVERRIDEIND = "A_PredOverrideInd";
	
	private static final String A_SUBMISSIONTYPE = "A_SubmissionType";
	
	

	/**
	 * This method drive the Automated Predictive Analysis process.
	 * @param user the NbaUser for whom the process is being executed
	 * @param work a NbaDst value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 * the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {

		//Process initialization
		if (!initialize(user, work)) {
			return getResult();
		}

		isTransaction = work.isTransaction();
		if (isTransaction) {
			originalTransaction = work;
			setWork(retrieveParentWork(work, true, false));
		}
		//Retrieves policyExtension to update EFPA LOB and PredictiveInd		
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		//Calls VPMS model to check whether prevent 1cv present on the case,if not check for Predictive conditions
		AxaPreventProcessVpmsData preventProcessData = new AxaPreventProcessVpmsData(user, getNbaTxLife(), getVpmsModelToExecute());
		if (preventProcessData.isPreventsProcess() || !checkPredictiveConditions()) {
			setResult(false, policyExtension);			
			nbaTxLife = doContractUpdate();
			handleHostResponse(nbaTxLife);
			doUpdateWorkItem();
			return result;
		}

		String companionType = getWork().getNbaLob().getCompanionType();
		Map outputParams = new HashMap();
		List companionCaseList = new ArrayList();

		//Retrieves Companion cases list
		if (!NbaUtils.isBlankOrNull(companionType)) {
			companionCaseList = retrieveCompanionCases(getWork(), getUser(), outputParams);
		}
		//Checks for Predictive criteria
		if (checkPredictiveCriteria(getWork(), companionCaseList, FIRST_CRITERIA_VALUE)) {
			if (checkPredictiveCriteria(getWork(), companionCaseList, SECOND_CRITERIA_VALUE)) {
				setResult(true,policyExtension);
			} else {
				setResult(false,policyExtension);
			}
			//Adds Secure Comment for missing fields
			if (missingFieldsList.size() != 0) {
				addSecureComment(missingFieldsList);
			}
		} else {
			setResult(false,policyExtension);
		}
		
		//APSL5122
		updateTermExpressInd();
		
		//Updates Contract and Work item
		nbaTxLife = doContractUpdate();
		handleHostResponse(nbaTxLife);
		doUpdateWorkItem();
		return getResult();
	}

	/**
	 * This method retrieves a work item from AWD.
	 * 
	 * @param nbaDst
	 *            a work item to be retrieved
	 * @return the retrieved work item
	 * @throws NbaBaseException
	 */
	protected NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("APPRDANL Starting retrieveWorkItem for " + nbaDst.getID());
		}
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(nbaDst.getID(), false);
		retrieveOptionsValueObject.requestSources();
		return retrieveWorkItem(getUser(), retrieveOptionsValueObject);
	}

	/**
	 * This method calls the VP/MS model to check the work item is eligible for Predictive criteria or not 
	 * @param work A work item to be proccessed
	 * @param companionCaseList List of the companion cases
	 * @param criteriaValue Criteria Value to be execute
	 * @return Result true/false
	 * @throws NbaBaseException
	 */
	protected boolean checkPredictiveCriteria(NbaDst work, List companionCaseList, String criteriaValue) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			Map deOink = new HashMap();
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
			nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			nbaOinkDataAccess.setLobSource(getWork().getNbaLob());
            //CR1345759 (APSL 2577 ) PA Override Starts
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPolicy().getApplicationInfo());
		    deOink.put(A_PREDOVERRIDEIND, String.valueOf(appInfoExt.getPredOverrideInd()));
            //CR1345759 (APSL 2577 ) PA Override Ends
		    
			//UserStoryNo 269593 - Start
		    ApplicationInfo appInfo = nbaTxLife.getPolicy().getApplicationInfo();
			deOink.put(A_SUBMISSIONTYPE, String.valueOf(appInfo.getSubmissionType()));
			//UserStoryNo 269593 - End
			
		    if (criteriaValue.equals(FIRST_CRITERIA_VALUE)) {
				int count = 0;
				List faceAmoumtList = new ArrayList();
				count = companionCaseList.size();
				for (int j = 0; j < count; j++) {
					NbaCompanionCaseVO aCase = (NbaCompanionCaseVO) companionCaseList.get(j);
					NbaDst nbaDst = aCase.getNbaDst();
					Double fAmount = new Double(nbaDst.getNbaLob().getFaceAmount());
					faceAmoumtList.add(fAmount.toString());
				}
				
				List forms = new ArrayList();
				forms = getApplicationSourceInfo(work);

				deOink.put(A_COMPFACEAMOUNTLIST, faceAmoumtList.toArray(new String[faceAmoumtList.size()]));
				deOink.put(A_COMPFACEAMOUNTLISTSIZE, String.valueOf(faceAmoumtList.size()));
				deOink.put(A_FORMNUMBERLIST, forms.toArray(new String[forms.size()]));
				deOink.put(A_FORMNUMBERLISTSIZE, String.valueOf(forms.size()));
				deOink.put(A_CRITERIAVALUE, criteriaValue);
				//NBLXA-2453
				String appState = String.valueOf(appInfo.getApplicationJurisdiction());
				if(!NbaUtils.isBlankOrNull(appState)){
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("ApplicationState == " + appState);
					}
					deOink.put("A_AppStateLOB", appState);	
				}
				//NBLXA-2453
				
			}
			if (criteriaValue.equals(SECOND_CRITERIA_VALUE)) {
				 // // ALII1974 -Old Code Deleted / New Code Below
				// ALII1974 Starts
				int medicalCount = 0;
				ArrayList medicalConditionList = new ArrayList();
				NbaParty party = nbaTxLife.getParty(nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_INSURED));
				if (party.getRisk() == null) {
					return false;
				}
				medicalCount = party.getRisk().getMedicalConditionCount();
				medicalConditionList = party.getRisk().getMedicalCondition();
				int insuredIndex = nbaTxLife.getOLifE().getParty().indexOf(party);
				// ALII1974 Ends
				MedicalCondition medCondition = null;
				NbaOinkRequest oinkRequest = new NbaOinkRequest();
				oinkRequest.setVariable("QuestionNumber");
				oinkRequest.setPartyFilter(insuredIndex);
				//Removed code - ALII1905,ALII1904
				boolean isMedCond13 = false;
				boolean isMedCond12 = false;
				for (int i = 0; i < medicalCount; i++) {
					medCondition = ((MedicalCondition) medicalConditionList.get(i));
					MedicalConditionExtension medCondExtn = NbaUtils.getFirstMedicalConditionExtension(medCondition);//ALII1841
					if (medCondExtn.getQuestionNumber().startsWith("13")) {//ALII1841
						isMedCond13 = true;
						//Removed code - ALII1905,ALII1904
					} else {//ALII1702 Starts
						//Removed code - ALII1841
						if (medCondExtn.getQuestionNumber().startsWith("12")) {
							isMedCond12 = true;
							//Removed code - ALII1905,ALII1904
							//ALII1702 Ends
						}
					}
				} 
				deOink.put(A_CRITERIAVALUE, criteriaValue);
				deOink.put(A_MED_CONDITION13, String.valueOf(isMedCond13)); //ALII1905,ALII1904
				deOink.put(A_MED_CONDITION12, String.valueOf(isMedCond12)); //ALII1905,ALII1904
				//Removed code - ALII1905,ALII1904
				deOink.put(A_DELIMITER, NbaVpmsConstants.VPMS_DELIMITER[0]);
			}
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.PREDICTIVE_ANALYSIS);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_CHECK_PRED_CRITERIA);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {				
				ArrayList vpmsResultArray = vpmsResultsData.getResultsData();
				for (int i = 1; i < vpmsResultArray.size(); i++) {
					if(!NbaUtils.isBlankOrNull(vpmsResultArray.get(i))){
						missingFieldsList.add(vpmsResultArray.get(i));
					}
				}
				return NbaConstants.TRUE == Integer.parseInt((String)vpmsResultArray.get(0));
			}

		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaProcPredictiveAnalysis").logError(re);
				}
			}
		}

		return false;
	}	
	
	/**
	 * This method calls the VP/MS model to check whether the work item qualifies predictive conditions
	 * @return Result true/false
	 * @throws NbaBaseException
	 */
	protected boolean checkPredictiveConditions() throws NbaBaseException {
		setWork(retrieveWorkItem(getWork()));
		NbaVpmsAdaptor proxy = null;
		try {
			List reqTypeList = new ArrayList();
			reqTypeList = getReqTypeList(work);
			Map deOink = new HashMap();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
			oinkRequest.setVariable("ReqCodeList");
			String[] codeList = nbaOinkDataAccess.getStringValuesFor(oinkRequest);
			int reqCount = codeList.length;
			oinkRequest.setVariable("ReqStatusList");
			String[] statusList = nbaOinkDataAccess.getStringValuesFor(oinkRequest);
			deOink.put(A_REQTYPELIST, reqTypeList.toArray(new String[reqTypeList.size()]));
			deOink.put(A_REQTYPELISTSIZE, String.valueOf(reqTypeList.size()));
			deOink.put(A_NO_OF_REQUIREMENTS, String.valueOf(reqCount));
			deOink.put(A_REQCODELIST, codeList);
			deOink.put(A_REQSTATUSLIST, statusList);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.PREDICTIVE_ANALYSIS);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_CHECK_PRED_CONDITIONS);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				if (NbaConstants.FALSE == Integer.parseInt((String) vpmsResultsData.getResultsData().get(0))) {
					return false;
				}
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaProcPredictiveAnalysis").logError(re);
				}
			}
		}

		return true;
	} 
	
	/**
	 * Adds a new secure comment for missing fields.
	 * 
	 * @param missingFieldsList the missingFieldsList to be added with comment.
	 */
	protected void addSecureComment(List missingFieldsList) {
		String missingFields = (String) missingFieldsList.get(0);
		for (int i = 1; i < missingFieldsList.size(); i++) {
			missingFields = missingFields + ",";
			missingFields = missingFields + missingFieldsList.get(i);

		}
		String comment = "Questions Omitted for Predictive Evaluation -";
		comment = comment + missingFields + ".";
		nbaOLifEId = new NbaOLifEId(nbaTxLife);
		Attachment attachment = new Attachment();
		nbaOLifEId.setId(attachment);
		attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE);
		attachment.setActionAdd();
		attachment.setDateCreated(new Date());
		attachment.setUserCode(getUser().getUserID());
		AttachmentData attachmentData = new AttachmentData();
		attachmentData.setTc("8");

		SecureComment secure = new SecureComment();
		secure.setComment(comment);
		secure.setUserNameEntered(getUser().getUserID());
		secure.setAutoInd(true);// APSL3520
		attachmentData.setPCDATA(toXmlString(secure));

		attachmentData.setActionAdd();
		attachment.setAttachmentData(attachmentData);
		getNbaTxLife().getPrimaryHolding().addAttachment(attachment);
	}

	/**
	 * This method retrieves the list of application sources attached with work item
	 * 
	 * @param work
	 *            A work item to be proccessed
	 * @return List of sources
	 * @throws NbaBaseException
	 */
	protected List getApplicationSourceInfo(NbaDst work) throws NbaBaseException {
		List forms = new ArrayList();
		List transactions = work.getNbaTransactions();
		String formNumber;
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction transaction = (NbaTransaction) transactions.get(i);
			List sources = transaction.getNbaSources();
			for (int j = 0; j < sources.size(); j++) {
				NbaSource aSource = (NbaSource) sources.get(j);
				String sType = aSource.getSourceType();

				if (sType.equals(NbaConstants.A_ST_MISC_MAIL) || sType.equals(NbaConstants.A_ST_FORMS)) {
					formNumber = aSource.getNbaLob().getFormNumber();
					if (formNumber != null && formNumber.length() > 0) {
						forms.add(formNumber);
					}
				}
			}
		}
		List caseSources = work.getNbaSources();
		for (int i = 0; i < caseSources.size(); i++) {
			NbaSource source = (NbaSource) caseSources.get(i);
			formNumber = source.getNbaLob().getFormNumber();
			if (formNumber != null && formNumber.length() > 0) {
				forms.add(formNumber);
			}
		}
		return forms;
	}
	
	
	/**
	 * This method retrieves the list of ReqType of attached MiscMail with work item
	 * @param work A work item to be proccessed
	 * @return List of reqType 
	 * @throws NbaBaseException
	 */
	protected List getReqTypeList(NbaDst work) throws NbaBaseException {
		List reqTypeList = new ArrayList();
		String reqType = null;
		List caseSources = work.getNbaSources();
		for (int i = 0; i < caseSources.size(); i++) {
			NbaSource source = (NbaSource) caseSources.get(i);
			String sourceType = source.getSourceType();
			if (sourceType.equals(NbaConstants.A_ST_MISC_MAIL)) {
				if(!NbaUtils.isBlankOrNull(source.getNbaLob().getReqType())){
					reqType = String.valueOf(source.getNbaLob().getReqType());
					if (reqType != null) {
						reqTypeList.add(reqType);
					}
				}
			}
		}
		return reqTypeList;
	}
	

	/**
	 * Populate companion case information by calling companion case business process.	  
	 * @param nbaDst
	 * @param user the NbaUser for whom the process is being executed
	 * @param outputParams
	 * @return List of companion cases
	 * @throws NbaBaseException
	 */
	protected List retrieveCompanionCases(NbaDst nbaDst, NbaUserVO user, Map outputParams) throws NbaBaseException {
		nbaDst.setNbaUserVO(user);
		AccelResult accelResult = (AccelResult) ServiceHandler.invoke(BP_RETRIEVE_COMPANION_CASES, ServiceContext.currentContext(), nbaDst);
		if (accelResult.hasErrors()) {
			WorkflowServiceHelper.checkOutcome(accelResult);
		} else {
			return (List) accelResult.getFirst();
		}
		return new ArrayList();
	}
		
	/**
	 * Convert the SecureComment to xml string
	 * @return java.lang.String
	 */
	protected String toXmlString(SecureComment secureComment) {
		String xml = "";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		if (stream != null) {
			secureComment.marshal(stream);
			xml = stream.toString();
			try {
				stream.close();
			} catch (java.io.IOException e) {
			}
		}
		return (xml);
	}
	
	/**
	 * Set NbaAutomatedProcess result and true/false value for PredictiveInd 
	 */
	protected void setResult(boolean predictiveInd,PolicyExtension policyExtension) throws NbaBaseException {		
		boolean initialPredictiveIndValue = getWork().getNbaLob().getPredictiveInd();//CR57873
		//Begin CR1345857(APSL2575)
		setWork(retrieveParentWithTransactions(work,user));
		//code moved down for APSL3442
		//End CR1345857(APSL2575)
		getWork().getNbaLob().setPredictiveInd(predictiveInd);
		policyExtension.setPredictiveInd(predictiveInd);
		policyExtension.setActionUpdate();
		if (predictiveInd) { 
			//Assigning Predictive underwriter to the case,if the case is qualify for Predictive. 
			//Do not need to reassign underwriter,if case was already Predictive qualified - CR57873.
//			if(initialPredictiveIndValue == false){
//				AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();			
//				uwAssignment.setNbaDst(getWork());
//				uwAssignment.setReassignment(true);
//				uwAssignment.setTxLife(nbaTxLife);
//				AxaUnderwriterAssignmentEngine underwriterAssignmentEngine = new AxaUnderwriterAssignmentEngine();
//				underwriterAssignmentEngine.execute(uwAssignment);
//			}
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
		} else {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));//APSL4226
		}
		// Start APSL3442
		List transactions = getWork().getNbaTransactions();
		int count = transactions.size();
		NbaTransaction nbaTransaction = null;
		for (int i = 0; i < count; i++) {
			nbaTransaction = (NbaTransaction) transactions.get(i);
			if (NbaConstants.A_WT_AGGREGATE_CONTRACT.equalsIgnoreCase(nbaTransaction.getWorkType())
					&& NbaConstants.A_QUEUE_AGGREGATE_CONTRACT.equalsIgnoreCase(nbaTransaction.getNbaLob().getAggrReference())) {
				nbaTransaction.getNbaLob().setPredictiveInd(predictiveInd);
				if (predictiveInd) {
					String newCaseManagerQueue = getWork().getNbaLob().getCaseManagerQueue();
					if (!nbaTransaction.getNbaLob().getCaseManagerQueue().equals(newCaseManagerQueue)) {
						if (nbaTransaction.getQueue().equals(nbaTransaction.getNbaLob().getCaseManagerQueue())) {
							nbaTransaction.getNbaLob().setCaseManagerQueue(newCaseManagerQueue);
							NbaUserVO nbaUser = new NbaUserVO(NbaConstants.PROC_VIEW_CASEMANAGER_QUEUE_REASSIGNMENT, "");
							NbaProcessStatusProvider provider = new NbaProcessStatusProvider(nbaUser, nbaTransaction.getNbaLob());
							if (provider != null && provider.getPassStatus() != null && !provider.getPassStatus().equals(nbaTransaction.getStatus())) {
								nbaTransaction.setStatus(provider.getPassStatus());
								NbaUtils.setRouteReason(nbaTransaction, provider.getPassStatus());
							}
						} else {
							nbaTransaction.getNbaLob().setCaseManagerQueue(newCaseManagerQueue);
						}
					}
				}
				nbaTransaction.setUpdate();
			}
		}
		//End APSL3442
		if (isTransaction) {
			originalTransaction.setStatus(getResult().getStatus());	
			//SR564247(ALII1572)
			if(!predictiveInd){
				originalTransaction.getNbaLob().setReevalSubType(false);
			}			
			update(originalTransaction);
		} else {
			changeStatus(getResult().getStatus());
		}		
	}
	//New Method CR1345857(APSL2575)
	protected NbaDst retrieveParentWithTransactions(NbaDst dst, NbaUserVO user) throws NbaBaseException{ //ALS5177
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(dst.getID(), true);
		retOpt.requestTransactionAsChild();
		retOpt.setLockWorkItem();
		retOpt.setLockTransaction();
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		NewBusinessAccelBP.processResult(workResult);//ALS5177
		return (NbaDst) workResult.getFirst();
}

	// APSL5122
	private void updateTermExpressInd() {
		if (getWork().getNbaLob().getDisplayIconLob() != null && getWork().getNbaLob().getDisplayIconLob().equals("1")) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getPolicy().getApplicationInfo());
			if (appInfoExt != null) {
				appInfoExt.setTermExpressInd(true);
				appInfoExt.setActionUpdate();
			}
		}
	}
}
