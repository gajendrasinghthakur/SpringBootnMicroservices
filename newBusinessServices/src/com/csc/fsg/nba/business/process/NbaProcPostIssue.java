package com.csc.fsg.nba.business.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axa.fsg.nba.fileGen.AxaFileGenerateProcessor;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.transaction.NbaRemovingXMLNodesUtils;
import com.csc.fsg.nba.business.transaction.NbaSFTPClientUtils;
import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.database.NbaContractDataBaseAccessor;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaPostIssueServiceAccessor;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaContractAccessException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.AxaValueObjectUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaPostIssueServiceVO;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

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
 * Post issue process handles webservice calls required once the Issue process is successful
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ALS4467</td><td>AXA Life Phase 1</td><td>QC #3235 - 3.7.17 CAPS - duplicate policies sent to CAPS submitAdministrationPolicy in QA</td></tr>
 * <tr><td>P2AXAL031</td><td>AXA Life Phase 2</td><td>RTS Interface</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>CR60956</td><td>AXA Life Phase 2</td><td>Life 70 Reissue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcPostIssue extends NbaAutomatedProcess {
	private boolean sftpFlag = false;// NBLXA-1632
	private NbaPostIssueServiceVO postIssueVO = null;
	private boolean serviceFailures = false;
	//Begin NBLXA-1632
	private NbaTXLife jointTXLife = null;
	private String filepath = null;
	private String fileName = null;
	private static final String CDATA = "<![CDATA[";
	private static final String CDATA_END = "]]>";
	private static final String POLICY_TO_ENDESSA = "<PoliciesToAndessa>";
	private static final String POLICY_TO_ENDESSA_END = "</PoliciesToAndessa>";
	private static final String NEXT_LINE = "\n";
	private static final String POLICY_DETAILS = "<PolicyDetails>";
	private static final String POLICY_DETAILS_END = "</PolicyDetails>";
	private static final String XML_VERSION = "<?xml version=" + "\"1.0\" " + "encoding= " + "\"UTF-8\"" + "?>";
	private static final String HOLDING_ID = "Holding_1";
	private static final String ATTACHMENT_ID = "Attachment_1";
	private static final String COIL_NBA = "COIL_nbA_ADS";
	private static final String XML = "xml";
	private static final String TIMESTAMP = "HH.mm.ss";
	private String andessaFileFormat = null;
	//End NBLXA-1632
		
	
	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess#executeProcess(com.csc.fsg.nba.vo.NbaUserVO, com.csc.fsg.nba.vo.NbaDst)
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		if (!initialize(user, work)) {
			return getResult();
		}
		try {
			postIssueVO = NbaPostIssueServiceAccessor.selectPostIssueRecord(getWork().getNbaLob().getPolicyNumber(), getWork().getWorkType());
			if (null == postIssueVO) {
				postIssueVO = new NbaPostIssueServiceVO(getWork().getWorkType());
				postIssueVO.setContractNumber(getWork().getNbaLob().getPolicyNumber());
				postIssueVO.setActionAdd();
			} else {
				postIssueVO.setActionUpdate();
			}
			doProcess();

			if (serviceFailures) {
				NbaPostIssueServiceAccessor.save(postIssueVO);
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", this.getHostErrorStatus()));
			} else {
				if (!postIssueVO.isActionAdd()) {
					NbaPostIssueServiceAccessor.delete(postIssueVO);
				}
			}
		} catch (NbaDataAccessException ndae) {
			addComment(ndae.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
		}
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		//QC#11357 APSL2943 Internal term to term replacement workflow  set PCCM Queue
		List<Policy> termToTermPolicies = NbaUtils.getTermToTermInternalReplPolicies(getNbaTxLife());
		if (!termToTermPolicies.isEmpty()) {
			//getWork().getNbaLob().setPaidChgCMQueue(getAlternateStatus()); //APSL4778 removed generic PCCM queues assignment and Apply Individual PCCM queues assignment.
			//Begin APSL4778
			AxaUWAssignmentEngineVO pccmAssignment = new AxaUWAssignmentEngineVO();
			pccmAssignment.setTxLife(getNbaTxLife());
			pccmAssignment.setNbaDst(getWork());
			pccmAssignment.setPaidChangeCaseManagerRequired(true);
			new AxaUnderwriterAssignmentEngine().execute(pccmAssignment);
			//End APSL4778
			
			// Begin NBLXA-2399
			NbaVpmsAdaptor vpmsProxy = null;
			try {
				NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
				vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL);
				vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_TERM_TO_TERM_REPLACEMENT);
				NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
				List resultsData = data.getResultsData();
				// The entry point in VP/MS is configured to return business area, work type and status.
				if (resultsData != null && resultsData.size() >= 3) {
					NbaDst lifeSRVTransaction = AxaUtils.createLifeSrvTransaction((String) resultsData.get(0), (String) resultsData.get(1),
							(String) resultsData.get(2), termToTermPolicies, getWork().getNbaUserVO());
					update(lifeSRVTransaction);
					unlockWork(lifeSRVTransaction);
					addComment("Work item created in LIFESRV Business Area");
				}
			} catch (java.rmi.RemoteException re) {
				throw new NbaVpmsException("APSTISS" + NbaVpmsException.VPMS_EXCEPTION, re);
			} finally {
				try {
					if (vpmsProxy != null) {
						vpmsProxy.remove();
					}
				} catch (Throwable th) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
			// End NBLXA-2399
		}
        //NBLXA-1632 Start
		if (NbaUtils.isProductCodeCOIL(nbaTxLife)) {
			if (NbaUtils.isGIApplication(nbaTxLife)) {
				String policyNumber = nbaTxLife.getPolicy().getPolNumber(); // nbaTxLife.getTXLife().getContractKey();
				String BACKEND_KEY = nbaTxLife.getBackendSystem();
				String COMPANY_KEY = nbaTxLife.getCarrierCode();
				if (AxaValueObjectUtils.getValueObjectForInsuredEmployerParty(nbaTxLife.getOLifE()) != null) {
					Organization org = AxaValueObjectUtils.getValueObjectForOrganization(AxaValueObjectUtils
							.getValueObjectForInsuredEmployerParty(nbaTxLife.getOLifE()));

					String employer_name = org.getDBA();
					if (!NbaSystemDataDatabaseAccessor.doesRecordExistForAndessa(policyNumber)) {
						NbaSystemDataDatabaseAccessor.insertGICOILAndessaData(policyNumber, COMPANY_KEY, BACKEND_KEY, employer_name);
					}
				}
			} else {
				andessaFileFormat = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.ANDESSA_FILE_FORMAT);
				getLogger().logError("andessaFileFormat  == " + andessaFileFormat);
				if (andessaFileFormat.equalsIgnoreCase("REQ")) { // REQ stands to generate the XML request.
					getLogger().logError("Genrate XML  == ");
					StringBuilder eibXML = new StringBuilder();
					filepath = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConstants.UPLOADED_FILEPATH_FOR_GI);// For Local use
																																	// this
					eibXML.append(POLICY_TO_ENDESSA);
					eibXML.append(NEXT_LINE);
					NbaRemovingXMLNodesUtils nodesUtils = new NbaRemovingXMLNodesUtils();
					// NbaTXLife aTXLife = null;
					String content = null;
					String policyNumber = nbaTxLife.getPolicy().getPolNumber(); // nbaTxLife.getTXLife().getContractKey();
					// String backendKey = nbaTxLife.getBackendSystem();
					// String carrierCode = nbaTxLife.getCarrierCode();
					try {
						// aTXLife = retrieveTxlifeForWI(policyNumber, carrierCode, backendKey, getUser());
						// //Call utility to prepare xml for retrieved txLife List.
						content = nodesUtils.removeNodesFromTXLife(nbaTxLife.toXmlString());
						eibXML.append(NEXT_LINE);
						eibXML.append(POLICY_DETAILS);
						eibXML.append(NEXT_LINE);
						eibXML.append(content);
						eibXML.append(NEXT_LINE);
						eibXML.append(POLICY_DETAILS_END);
						eibXML.append(NEXT_LINE);
						eibXML.append(POLICY_TO_ENDESSA_END);
						jointTXLife = prepareXMLToSendEIB(eibXML.toString());
						fileName = uploadXML(jointTXLife.toXmlString());

						// routeWorkItemToEndAndUpdateAndesaData(policyNumber);
						// if (getResult() == null) {
						// return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
						// }
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					getLogger().logError("Genrate XLS  == ");
					AxaFileGenerateProcessor processor = new AxaFileGenerateProcessor();
					fileName = processor.generateSSForTxLIfe(nbaTxLife);
				}
				if(null != fileName){
					getLogger().logError("SFTP fileName  == " + fileName);
					sftpFlag=uploadFileOnAXAServer(fileName);
					System.out.println("sftpFlag::::"+sftpFlag);
				}
				

			}
		}
        //NBLXA-1632 End
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		processUnSuspendEvent(getUser(),getWork()); //NBLXA-1379
		return getResult();
	}
	
	private void doProcess() {
		try {
			if (getWork().isCase()) {
				postIssueServices();
			} else {
				postPrintServices();
			}
		} catch (NbaBaseException nbe) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			addComment(nbe.getMessage());
			getLogger().logError(nbe.getMessage());
		}
	}
	/**
	 * @return Returns the postIssueVO.
	 */
	public NbaPostIssueServiceVO getPostIssueVO() {
		return postIssueVO;
	}
	/**
	 * @param postIssueVO The postIssueVO to set.
	 */
	public void setPostIssueVO(NbaPostIssueServiceVO postIssueVO) {
		this.postIssueVO = postIssueVO;
	}

	/*
	 * method to invoke services using the 203 TxLife
	 */
	private boolean invokeWebService(String service)  {

		return invokeWebService(getNbaTxLife(),service);
	}
	
	/*
	 * This method invokes the AXA WebServices.  Any errors are caught and logged as comments.  Additionally, the serviceFailure Flag is set to true
	 * to be used in final processing which determines how we route the work item.
	 * Returns TRUE if the service was successful.
	 */
	private boolean invokeWebService(NbaTXLife txLife, String service) {
		try {
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(service, getUser(),txLife, getWork(), null);
			webServiceInvoker.execute();
			} catch (NbaBaseException nbe) {
				StringBuffer messageBuffer = new StringBuffer();
				messageBuffer.append("Error invoking ");
				messageBuffer.append(service);
				messageBuffer.append(" Reason: ");
				messageBuffer.append(nbe.getMessage());
				serviceFailures = true;
				addComment(messageBuffer.toString());
				return false;
			}
			return true;
	}
	private void postIssueServices() throws NbaBaseException {
		if (!isIncrease() && !isReinstatment()) {  //following existing logic, prior insurance not invoked on Increase or reinstatement
			invokePriorInsurance();
		}
		if (getPostIssueVO().isPcs() && NbaUtils.isCompensationCallEnabled() && ( !isPermProduct(getNbaTxLife()) || NbaUtils.isISWLProduct(getNbaTxLife().getPolicy()) )  && invokeWebService(AxaWSConstants.WS_OP_PCS) ) {//P2AXAL007, ALII1796
			getPostIssueVO().setPcs(false);
		}
		//end AXAL3.7.22
		//begin AXAL3.7.26
		if (getPostIssueVO().isOlsa() && NbaUtils.isOLSACallEnabled() && isOLSACallNeeded() && invokeWebService(AxaWSConstants.WS_OP_OLSA)) {
			getPostIssueVO().setOlsa(false);
		}
		//Begin NBLXA-1254
		if (getPostIssueVO().isMdm() && invokeMDMWebService()) {
			getPostIssueVO().setMdm(false);
		}
		//End NBLXA-1254
	}
	private void invokePriorInsurance() {
		
		boolean passFail = getPostIssueVO().isPriorIns();
		if (passFail && !getNbaTxLife().paidReissue()) {//CR60956
			//P2AXALP010 code deleted
			passFail = invokeWebService(AxaWSConstants.WS_OP_ADD_PRIOR_INSURANCE);
		}
		getPostIssueVO().setPriorIns(passFail);
	}
	
	//APSL459 method deleted protected boolean isReissue()
	
	protected boolean isReinstatment() {
		return getWork().getNbaLob().getContractChgType() != null
			&& NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT == Long.parseLong(getWork().getNbaLob().getContractChgType());
	}
	protected boolean isIncrease() {
		return getWork().getNbaLob().getContractChgType() != null && NbaOliConstants.NBA_CHNGTYPE_INCREASE == Long.parseLong(getWork().getNbaLob().getContractChgType());
	}

	private void postPrintServices() throws NbaBaseException  { // ALII1668
		//NBLXA-1632 Start
		if(NbaUtils.isProductCodeCOIL(nbaTxLife)){
			postIssueServices();
		}
		//NBLXA-1632 End		
		// ALII1668 - code deleted.
		// for PCS, RCS, PIRS, use Contract Print TxLife object
		if (NbaUtils.isCompensationCallEnabled()) {
			// updatePCSWebService(getNbaTxLife()); // ALII1668 APSL5192 WS call is not required
			updateRCSWebService(getNbaTxLife()); // ALII1668
			updatePIRSWebService(getNbaTxLife()); // ALII1668
		}
		// end AXAL3.7.22
		// begin AXAL3.7.27
		
		if (NbaUtils.isRTSCallEnabled()) {
			updateRTSWebService();
		}
		
		// Begin APSl5100
		getWork().getNbaLob().setContractPrinted(true); 
		// For sending Print Status to Epolicy
		if (isPreviewNeeded(getNbaTxLife())) {
			performPrintStatusWebServiceCall();
		}
		// End APSL5100
		}
	protected NbaSource retrieveContractPrintSource() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), true);
		retOpt.requestSources();
		try {
			setWork(retrieveWorkItem(getUser(), retOpt)); 
			
		} catch (NbaNetServerException e) {
			throw new NbaBaseException("Unable to retrieve", e);
		//NBA213 deleted code
		}		
		List sources = getWork().getNbaSources();
		for (int i = 0; i < sources.size(); i++) {
			if (((NbaSource) sources.get(i)).isContractPrintExtract()) {
				return (NbaSource) sources.get(i);
			}
		}
		return null;
	}	
	/**
	 * 
	 */
	//AXAL3.7.26 new method added.
	protected boolean isOLSACallNeeded() throws NbaBaseException {
		NbaVpmsAdaptor olsaVpmsAdaptor;
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
		oinkData.setContractSource(getNbaTxLife());
		olsaVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.ISSUE);
		olsaVpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_OLSACALLNEEDED);
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_PaymentFound", getNbaTxLife().getPolicy().getFinancialActivityCount() > 0 ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
		deOinkMap.put("A_OLSAPaymentFound", isOLSAPaymentFound() ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
		olsaVpmsAdaptor.setSkipAttributesMap(deOinkMap);
		VpmsComputeResult computeResult = null;
		try {
			computeResult = olsaVpmsAdaptor.getResults();
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS, re);
		} finally {
			try {
			if (null != olsaVpmsAdaptor) {
				olsaVpmsAdaptor.remove();
			}
			} catch (Exception e) {
				 getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		return NbaConstants.TRUE_STR.equalsIgnoreCase(computeResult.getResult());
	}

	/**
	 * @return
	 */
	//AXAL3.7.26 new method added.
	protected boolean isOLSAPaymentFound() {
		boolean olsaPaymentFound = false;
		Iterator finActItr = getNbaTxLife().getPolicy().getFinancialActivity().iterator();
		outer: while (finActItr.hasNext()) {
			FinancialActivity finAct = (FinancialActivity) finActItr.next();
			if (NbaOliConstants.OLI_FINACT_PREMIUMINIT == finAct.getFinActivityType()) {
				Iterator paymentItr = finAct.getPayment().iterator();
				while (paymentItr.hasNext()) {
					Payment payment = (Payment) paymentItr.next();
					if (NbaOliConstants.SOURCE_FUNDS_DETAILS_1213.equalsIgnoreCase(payment.getSourceOfFundsDetails())) {
						olsaPaymentFound = true;
						break outer;
					}
				}
			}
		}
		return olsaPaymentFound;
	}
	protected void updatePCSWebService(NbaTXLife txLife)  {
		if ( (getPostIssueVO().isPcs() && ( !isPermProduct(txLife)) || NbaUtils.isISWLProduct(txLife.getPolicy()) ) && invokeWebService(txLife, AxaWSConstants.WS_OP_PCS)) {//P2AXAL007, ALII1796
			getPostIssueVO().setPcs(false);
		}

	}

	/**
	  * The method would check if RCS for Compensation need to be called. If yes, then calls the appropriate web service
	  * @param nbaTXLife
	  * @param axaServiceRequestorUtils
	  * @return
	  * @throws NbaBaseException
	  */
	//AXAL3.7.22 new method added
	protected void updateRCSWebService(NbaTXLife txlife) {
		long replType = -1L;
		if (getPostIssueVO().isRcs() && getNbaTxLife().isReplacement())
			replType = getNbaTxLife().getPolicy().getReplacementType();
		boolean ph1Cond = replType == NbaOliConstants.OLI_REPTY_INTERNAL;
		boolean ph2CondA = getNbaTxLife().isTermConversion(); //P2AXAL007
		boolean ph2CondB = ph1Cond && getNbaTxLife().is1035Exchange(); //P2AXAL007
		if (ph1Cond || ph2CondA || ph2CondB) { //ALS4395
			if (invokeWebService(txlife, AxaWSConstants.WS_OP_RCS)) {
				getPostIssueVO().setRcs(false);
			}
		}
	}	

	/**
	 * The method would check if PIRS for Compensation need to be called. If yes, then calls the appropriate web service
	 * @param nbaTXLife
	 * @param axaServiceRequestorUtils
	 * @param policyExtension
	 * @throws NbaBaseException
	 */
	//AXAL3.7.22 new method added
	protected void updatePIRSWebService(NbaTXLife txLife) {
		boolean callPIRSWebService = false;
		Policy primaryPolicy = getNbaTxLife().getPolicy();
		if (getNbaTxLife().isWholeSale()||NbaUtils.isProductCodeCOIL(txLife)) { //NBLXA-2090
			callPIRSWebService = true;
		} else if (getNbaTxLife().isRetail()) {
			Iterator requirementIterator = primaryPolicy.getRequirementInfo().iterator();
			while (requirementIterator.hasNext()) {
				RequirementInfo requirementInfo = (RequirementInfo) requirementIterator.next();
				if (requirementInfo != null
						&& (NbaOliConstants.OLI_REQCODE_POLDELRECEIPT == requirementInfo.getReqCode() 
								|| NbaOliConstants.OLI_REQCODE_SIGNILLUS == requirementInfo.getReqCode() || NbaOliConstants.OLI_REQCODE_1009800041 == requirementInfo.getReqCode()) //APSL550 - Added Premium Quote(1009800041)
						&& (NbaOliConstants.OLI_REQSTAT_ADD == requirementInfo.getReqStatus()
								|| NbaOliConstants.OLI_REQSTAT_ORDER == requirementInfo.getReqStatus() 
								|| NbaOliConstants.OLI_REQSTAT_SUBMITTED == requirementInfo	.getReqStatus())) {
					callPIRSWebService = true;
					break;
				}
			}
		}
		if (getPostIssueVO().isPirs() && callPIRSWebService) {
			if (invokeWebService(txLife, AxaWSConstants.WS_OP_PIRS)) {
				getPostIssueVO().setPirs(false);
			}
		}
	}
	/**
	 * The method creates TX 1203 and send it to RTS interface by making a call to the interface.
	 * @param nbaUserVO
	 * @param nbaTxLife
	 * @param axaInvokeWebservice
	 * @param axaServiceRequestorUtils
	 * @throws NbaBaseException
	 */
	//AXAL3.7.27 new method added.
	private void updateRTSWebService() {
		Policy primPol = getNbaTxLife().getPolicy();
		boolean ph1Cond = false;		
		if (getNbaTxLife().isReplacement()) {//AXAL3.7.27/ALPC257 added external
			if (NbaOliConstants.OLI_REPTY_INTERNAL == primPol.getReplacementType()
					|| NbaOliConstants.OLI_REPTY_EXTERNAL == primPol.getReplacementType()
					|| NbaOliConstants.OLI_REPTY_UNADREPSTA == primPol.getReplacementType()
					|| NbaOliConstants.OLI_REPTY_UNADREPDISB == primPol.getReplacementType()) {
				ph1Cond = true;
			}
		}
		boolean ph2Cond = getNbaTxLife().isTermConversion(); //P2AXAL031
		boolean updateFlag = ph1Cond || ph2Cond;
		if (updateFlag && getPostIssueVO().isRts()) {
			if (invokeWebService(AxaWSConstants.WS_OP_RTS)) {
				getPostIssueVO().setRts(false);
			}
		}
	}
	
	//	P2AXAL007 New Method
	private boolean isPermProduct(NbaTXLife txLife){
		boolean perm = false;
		Policy pol = txLife.getPolicy(); 
		if(pol != null){
			perm = AxaUtils.isPermProduct(pol.getProductType());
		}
		return perm;
	}
	
	//	APSL5100 New Method
	private boolean isPreviewNeeded(NbaTXLife txLife){
		boolean previewNeeded = false;
		Policy pol = txLife.getPolicy(); 
		if(pol != null && pol.getOLifEExtensionCount()>0 
				&& pol.getOLifEExtensionAt(0).getPolicyExtension() !=null ){
			previewNeeded = pol.getOLifEExtensionAt(0).getPolicyExtension().getPrintPreviewNeededInd();
		}
		return previewNeeded;
	}
	
	/**
	 * Set the Business Process to Pre Issue Validation and perfrom Contract Validation. If Severe or non-Overridden errors are present
	 * @throws NbaBaseException
	 */
	// APSL5100 New Method
	protected void performPrintStatusWebServiceCall() throws NbaBaseException {
		EPolicyData ePolicyData = null;
		try {
			if (getNbaTxLife().getPolicy().getOLifEExtensionCount() > 0) {
				PolicyExtension policyExtn = getNbaTxLife().getPolicy().getOLifEExtensionAt(0).getPolicyExtension();
				if (policyExtn != null && policyExtn.getEPolicyDataCount() > 0) {
					for (int i = 0, j = policyExtn.getEPolicyDataCount(); i < j; i++) {
						ePolicyData = policyExtn.getEPolicyDataAt(i);
						if (ePolicyData != null && ePolicyData.getActive()) {
							ePolicyData.setPrintStatus(NbaOliConstants.OLI_PRINT_STATUS_IGO);
							ePolicyData.setContractPrintIgoDate(new Date()); //ASPL5128;
							// NBLXA-188(APSL5318) Legacy Decommissioning
							if (NbaUtils.isGIApplication(getNbaTxLife()) && policyExtn.getPrintTogetherIND()) {
								String ePolicyPrintID = ePolicyData.getEPolicyPrintID();
								NbaSystemDataDatabaseAccessor
										.updatePrintPassInd(policyExtn.getGIBatchID(), getNbaTxLife().getPolicy().getPolNumber(),ePolicyPrintID);
								if (NbaSystemDataDatabaseAccessor.isPrintPassIndUpdatedForAll(policyExtn.getGIBatchID())) {
									ePolicyData.setReleasePrintInd(true);
									NbaSystemDataDatabaseAccessor.updateReleaseBatch(policyExtn.getGIBatchID());
								}
							}
							ePolicyData.setActionUpdate();
							break;
						}
					}
				}
			}
			ArrayList<Activity> activityList = getNbaTxLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && (activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_IGO || activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_IGO_CORRECTED_IN_EPOLICY)){
						activity.setActivityStatus(NbaOliConstants.OLI_ACTSTAT_COMPLETE);
						activity.setActionUpdate();
					}
				}
			}
			doContractUpdate();
			// NBLXA-188,NBLXA-1528 Legacy Decommissioning: ESLI product: web service call exclusion
			if (ePolicyData != null && !NbaUtils.isESLIProduct(getNbaTxLife()) && !NbaUtils.isEmpty(ePolicyData.getEPolicyPrintID())) {
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SEND_PRINT_PREVIEW_STATUS,
						this.user, getNbaTxLife(), work, null);
				webServiceInvoker.execute();
			}
		}catch (NbaBaseException ex) {
	        //Begin APSL5394		
			if (ex.getMessage()!=null && ex.getMessage().contains(ePolicyData.getEPolicyPrintID())) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug(ex.getMessage());
				}} else {
					if (!ex.isFatal()) {
						throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, ex.getMessage());
					}
					throw ex;
				}
			}
		//End APSL5394
	}
	
	// NBLXA-1254 New method
	private boolean invokeMDMWebService()  {
		try {
			AxaWSInvoker cifwebServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CIF_TRANSMIT, getUser(),
					getNbaTxLife(), getWork(), null);
			NbaTXLife nbaTXLifeResponse = (NbaTXLife) cifwebServiceInvoker.execute();
			return verifyTxLifeResponse(nbaTXLifeResponse);
		} catch (NbaBaseException nbe) {
			StringBuffer messageBuffer = new StringBuffer();
			messageBuffer.append("Error invoking ");
			messageBuffer.append(AxaWSConstants.WS_OP_CIF_TRANSMIT);
			messageBuffer.append(" Reason: ");
			messageBuffer.append(nbe.getMessage());
			serviceFailures = true;
			addComment(messageBuffer.toString());
			return false;
		}		
	}
	
	// NBLXA-1254 New method
	public boolean verifyTxLifeResponse(NbaTXLife response) throws NbaBaseException {
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify txlifeResponseParent = response.getTXLife()
				.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (txlifeResponseParent.getTXLifeResponseCount() > 0 && txlifeResponseParent.getTXLifeResponseAt(0).hasTransResult()) {
			TXLifeResponse txLifeResponse = txlifeResponseParent.getTXLifeResponseAt(0);
			TransResult transResult = txLifeResponse.getTransResult();
			long resultCode = transResult.getResultCode();
			if ((NbaOliConstants.TC_RESCODE_FAILURE == resultCode)) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logError(" Data error occured during web service invoke of CIF Submit");
				}
				throw new NbaBaseException(AxaWSConstants.WS_OP_CIF_TRANSMIT + " WebService returned invalid response.");
			}
		} else {
			throw new NbaBaseException(AxaWSConstants.WS_OP_CIF_TRANSMIT + " WebService returned invalid response.");
		}
		return true;
	}
	
	//Begin NBLXA-1632
	protected String uploadXML(String eibXML) {
		fileName = filepath + getFileName();
		File file = new File(fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			// Write in file
			bw.write(eibXML);
			bw.flush();
			bw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	private NbaTXLife retrieveTxlifeForWI(String policyNumber, String companyKey, String backendSys, NbaUserVO user) throws Exception {
		NbaTXLife txLife203 = null;
		try {
			String[] args = new String[4];
			args[0] = "NO_ID";
			args[1] = policyNumber;
			args[2] = companyKey;
			args[3] = backendSys;

			StringBuffer txLifeStringBuffer = new StringBuffer("<TXLife xmlns=\"http://ACORD.org/Standards/Life/2\" >");
			txLifeStringBuffer.append("<UserAuthRequest><UserLoginName>" + user.getUserID()
					+ "</UserLoginName><UserPswd><CryptType>NONE</CryptType><Pswd>" + user.getPassword() + "</Pswd></UserPswd></UserAuthRequest>");
			txLifeStringBuffer.append("<TXLifeRequest><TransRefGUID>303721ZQ-9DFB-11D4-AF00-00D0B781A9F9</TransRefGUID></TXLifeRequest></TXLife>");

			NbaTXLife nbatxLife = new NbaTXLife(txLifeStringBuffer.toString());
			nbatxLife.setBusinessProcess(PROC_NBP);
			nbatxLife.setAccessIntent(READ);
			ArrayList pendResults = retrievePendEnquire(args);
			txLife203 = new NbaTXLife(nbatxLife);
			TXLife newTXLife = txLife203.getTXLife();
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify userAuthResponse = newTXLife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			TXLifeResponse txLifeResponse = userAuthResponse.getTXLifeResponseAt(0);
			txLifeResponse.setOLifE((OLifE) pendResults.get(0));

		} catch (SQLException sqle) {
			NbaLogFactory.getLogger(this.getClass()).logException(sqle);
			throw new NbaBaseException("An error occured while performing DB operation.", sqle);
		} catch (NbaBaseException nbe) {
			nbe.forceFatalExceptionType();
			NbaLogFactory.getLogger(this.getClass()).logException(nbe);
			throw new NbaBaseException("An error occured while setting Bean details.", nbe);
		}
		return txLife203;
	}

	private ArrayList retrievePendEnquire(String[] args) throws NbaContractAccessException {
		ArrayList results = NbaContractDataBaseAccessor.getInstance().selectOLifE(args);
		if (results == null || results.size() < 1) {
			throw new NbaContractAccessException("Unable to retrieve information from Contract Database for " + args[1]);
		}
		return results;
	}

	protected void routeWorkItemToEndAndUpdateAndesaData(String policyNum) {
		NbaDst parentWorkItem;
		try {
			parentWorkItem = getParentWorkItem(policyNum, user);
			unsuspendParentCase(getUser(), parentWorkItem);
			setWork(parentWorkItem);
			initializeStatusFields();
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
				changeStatus(parentWorkItem, getResult().getStatus());
			}
			doUpdateWorkItem();
			NbaContractLock.removeLock(getUser());
			unlockWork(parentWorkItem);
			// unsuspendParentCase(getUser(), parentWorkItem);
			NbaSystemDataDatabaseAccessor.updateRouteIndForAndessa(policyNum);
		} catch (NbaBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public NbaDst getParentWorkItem(String polNumber, NbaUserVO userVo) throws NbaBaseException {
		NbaSearchVO searchParentWI = new NbaSearchVO();
		searchParentWI.setContractNumber(polNumber);
		searchParentWI.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchParentWI.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchParentWI = WorkflowServiceHelper.lookupWork(userVo, searchParentWI);
		NbaDst aWorkItem = null;
		if (searchParentWI != null && searchParentWI.getSearchResults().size() > 0) {
			aWorkItem = retrieveWorkItem((NbaSearchResultVO) searchParentWI.getSearchResults().get(0));
		}
		return aWorkItem;
	}

	public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(getUser());
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}

	protected void unsuspendParentCase(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (work.isSuspended()) {
			work.setNbaUserVO(user);
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setCaseID(work.getID());
			suspendVO.setNbaUserVO(user);
			unsuspendWork(user, suspendVO);
		}
	}
	
	protected void writeToLogFile(String entry) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(entry);
		}
	}

	protected NbaTXLife prepareXMLToSendEIB(String xml) {
		xml.replace(XML_VERSION, "");
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS); // We will update these fields after discussion
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(getUser());
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		OLifE olifE = new OLifE();
		olifE.setVersion(NbaOliConstants.OLIFE_VERSION);
		Holding holding = new Holding();
		holding.setId(HOLDING_ID);
		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		Attachment attachment = new Attachment();
		attachment.setId(ATTACHMENT_ID);
		attachment.setDateCreated(new Date());
		attachment.setUserCode(getUser().getUserID());
		AttachmentData attachmentData = new AttachmentData();
		StringBuilder sb = new StringBuilder();
		sb.append(CDATA).append(XML_VERSION).append(NEXT_LINE).append(xml).append(CDATA_END);
		attachmentData.setPCDATA(sb.toString());
		attachment.setAttachmentData(attachmentData);
		attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_ANDESSA);
		holding.addAttachment(attachment);
		olifE.addHolding(holding);

		nbaReqTXLife.setOLifE(olifE);
		return nbaReqTXLife;
	}

	/**
	 * @purpose This method will return the fileName
	 * @param fieldName
	 * @param fileName
	 * @return
	 */
	private String getFileName() {
		return COIL_NBA + "_" + NbaUtils.getDateWithoutSeparator(new Date()) + "_" + getTimeStampFromStringInAWDFormat(new Date())
				+ "." + XML;
	}

	private String getTimeStampFromStringInAWDFormat(Date date) {
		String aDate = null;
		aDate = new java.text.SimpleDateFormat(TIMESTAMP).format(date);
		return aDate;
	}
	
	/**
	 * @purpose This method will Upload the file(XLS/XML) onto the AXA server
	 * @throws NbaBaseException
	 */
	private boolean uploadFileOnAXAServer(String fileNameWithPath) throws NbaBaseException{
		try {
			String destinationFile = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SFTP_AXA_PATH);// For
			NbaSFTPClientUtils sftpClientUtils = new NbaSFTPClientUtils();
			sftpClientUtils.uploadFile(fileNameWithPath, destinationFile);
			if (!NbaUtils.isBlankOrNull(fileNameWithPath)) {
				File file = new File(fileNameWithPath);
				if (file.exists()) {
					file.delete();
				}
			}
			return true;
		} catch (NbaBaseException e) {	
			NbaLogFactory.getLogger(this.getClass()).logException(e);
			return false;
		}
	}
	
	//End NBLXA-1632
}
