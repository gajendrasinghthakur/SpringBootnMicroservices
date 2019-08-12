package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.csc.fsg.nba.bean.accessors.NbaCompanionCaseFacadeBean;
import com.csc.fsg.nba.business.process.formal.NbaProcFormalProxyFactory;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * NbaProcFormal is the class that processes work items found on the NBFORMAL queue.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA187</td><td>Version 7</td><td>Initial Development</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>AXAL3.7.03</td><td>AXA Life Phase 1</td><td>Informals</td></tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
 * <tr><td>SPR3439</td><td>Version 8</td><td>Informal contract remains locked after merged with formal contract </td></tr>
 * <tr><td>SPR3712</td><td>Version 8</td><td>Receive Exception on Submit of Formal Originating from Trial Application if Tab 5 or Tab 6 Visited on Trial Application Entry View</td></tr>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>AXAL3.7.20</td><td>AXA Life Phase 1</td><td>Workflow</td></tr>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * <tr><td>NA_AXAL008</td><td>AXA Life Phase 2</td><td>Workflow</td></tr>
 * <tr><td>CR1344614</td><td>AXA Life Phase 2</td><td>CHBM Form</td></tr>
 * <tr><td>CR1344078</td><td>AXA Life Phase 2</td><td>LTC Supp</td></tr>
 * <tr><td>APSL3836</td><td>Discretionary</td><td>Electronic Initial Premium - Phase 2</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcFormal extends NbaAutomatedProcess {
	

	//ALS3091 Code deleted

	/**
	 * This method drives the Formal process by retrieving the work item from AWD and then searching for matching informal work item. If one is
	 * found, the informal work item is copied to the formal work item. The formal work item is then moved to the next queue.  If a matching informal
	 * work item is not  found, the formal work items is suspended.
	 * @param user the user for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return getResult();
		}
		//Begin ALS3091
		setWork(retrieveWorkItem(getWork()));
		reinitializeFields(); //NA_AXAL008
		doProcess();
		//ALS4833 Code Deleted
		//End ALS3091
		return getResult();
	}

	/**
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	protected void doProcess() throws NbaBaseException {
		//Begin ALS3091
		NbaAutomatedProcess proxy = NbaProcFormalProxyFactory.getProxy(getWork(), getUser(), getStatusProvider(), getCurrentBP());
		if (proxy != null) {
			NbaAutomatedProcessResult result = proxy.executeProcess(getUser(), getWork());
			setResult(result);
		}
		//End ALS3091
	}

	//ALS3091 Code deleted

	/**
	 * This method retrieves a work item from AWD.
	 * 
	 * @param nbaDst a work item to be retrieved
	 * @return the retrieved work item
	 * @throws NbaBaseException
	 */
	protected NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("APFORMAL Starting retrieveWorkItem for " + nbaDst.getID());
		}
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(nbaDst.getID(), false);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockTransaction();
		return retrieveWorkItem(getUser(), retrieveOptionsValueObject);
	}
	/*
	 * This method reinitializes the statusProvider based on existence of ElSI or COIL form
	 */
	//NA_AXAL008 New Method
	protected void reinitializeFields() throws NbaBaseException {
		setStatusProvider(new NbaProcessStatusProvider(user, getWork(), getDeOinkMap()));//CR1344614
	}
	/*
	 * Method adds a deOink variable if satisfied the mention condtion.
	 * Reads the FRNB LOB on MiscMail Sources
	 * returns a Map which includes the deOink variable
	 */
	//NA_AXAL008 New Method
	//	CR1344614  - Rename method and changes the conditions for performance 
	protected Map getDeOinkMap() throws NbaBaseException {
		NbaVpmsResultsData data = getElsiCoilFormsFromVpms();
		ArrayList formList = data.getResultsData();
		Map deOinkMap = new HashMap();
		List sourceList = getWork().getSources();
		int count = sourceList.size();
		int cipeCwaCount=0; //APSL3836
		NbaSource aSource = null;
		boolean elsiCoil = false;
		boolean cHMBFormPresent = false; //CR1344614
		boolean LTCFormPresent = false; //CR1344078
		boolean medSuppPresent = false; //APSL2859
		//Start APSL1879
		String companionType = getWork().getNbaLob().getCompanionType();
		String underwriterAction = getWork().getNbaLob().getUnderwriterActionLob();
		List faceAmoumtList = new ArrayList();
		
		String agentId = getWork().getNbaLob().getAgentID();//APSL4100		
		
		//Retrieves Companion cases list
		if (!NbaUtils.isBlankOrNull(companionType)) {
			List companionCaseList = new ArrayList();			
			companionCaseList = retrieveCompanionCases(getWork(), getUser());
			int caseCount = companionCaseList.size();
			for (int j = 0; j < caseCount; j++) {
				NbaCompanionCaseVO aCase = (NbaCompanionCaseVO) companionCaseList.get(j);
				NbaDst nbaDst = aCase.getNbaDst();
				Double fAmount = new Double(nbaDst.getNbaLob().getFaceAmount());
				faceAmoumtList.add(fAmount.toString());				
			}
		}
		//End APSL1879
		for (int i = 0; i < count; i++) {
			aSource = (NbaSource) work.getNbaSources().get(i);
			if (aSource.getSource().getSourceType().equals(NbaConstants.A_WT_MISC_MAIL)) { //CR1344614
				if (formList.contains(aSource.getNbaLob().getFormNumber())) {
					elsiCoil = true;
				}
				if (aSource.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_CHBM) { //CR1344614
					cHMBFormPresent = true; //CR1344614
				}
				if (aSource.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_LTC){ //CR1344078
					LTCFormPresent = true;//CR1344078
				}
				if (aSource.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800031){ //APSL2859
					medSuppPresent = true;//APSL2859
				}
				if(aSource.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800082){ //APSL3321
					if (aSource.getNbaLob().getFormNumber() != null ){ //APSL3321
						deOinkMap.put("A_TconvSuppForm", aSource.getNbaLob().getFormNumber());//APSL3321 
					}
				}//APSL3321
			}else if (aSource.getSource().getSourceType().equals(NbaConstants.A_ST_CWA_CHECK)){ //APSL3742
				deOinkMap.put("A_PaymentMoneySource", aSource.getNbaLob().getPaymentMoneySource());
				//APSL3836 Begins
				if (!NbaUtils.isBlankOrNull(aSource.getNbaLob().getPaymentMoneySource()) && aSource.getNbaLob().getPaymentMoneySource().equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_PAYFORM_EFT))) { //APSL4164
					cipeCwaCount++;
				}
				//APSL3836 Ends
			}
		}
		if (!NbaUtils.isBlankOrNull(underwriterAction)) {
			deOinkMap.put("A_UnderwriterActionLob", String.valueOf(underwriterAction));//APSL3836
		}
		deOinkMap.put("A_CipeCwaCount", String.valueOf(cipeCwaCount));//APSL3836
		deOinkMap.put("A_ElsiCoilForm", String.valueOf(elsiCoil));
		deOinkMap.put("A_CHBMForm", String.valueOf(cHMBFormPresent)); //CR1344614
		deOinkMap.put("A_LTCSupp", String.valueOf(LTCFormPresent));//CR1344078	
		deOinkMap.put("A_MedSupp", String.valueOf(medSuppPresent));//APSL2859
		deOinkMap.put("A_COMPFACEAMOUNTLIST", faceAmoumtList.toArray(new String[faceAmoumtList.size()]));//APSL1879
		deOinkMap.put("A_COMPFACEAMOUNTLISTSIZE", String.valueOf(faceAmoumtList.size()));//APSL1879
		
		deOinkMap.put("A_ISEARCAGENT", String.valueOf(NbaUtils.isEarcAgent(agentId)));//APSL4100		
		
		//end NBA231
		return deOinkMap;
	}
	
	/*
	 * Invoke VPMS to return an ArrayList of Forms.
	 */
	//NA_AXAL008 New Method
	protected NbaVpmsResultsData getElsiCoilFormsFromVpms() throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL);
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_ELSI_COIL_FORMS);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("InformalToFormal" + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
			}
		}
	}
	
	/**
	 * Populate companion case information by calling companion case business process.	  
	 * @param nbaDst
	 * @param user the NbaUser for whom the process is being executed
	 * @param outputParams
	 * @return List of companion cases
	 * @throws NbaBaseException
	 */
	//APSL1879 - New Method
	protected List retrieveCompanionCases(NbaDst nbaDst, NbaUserVO user) throws NbaBaseException {
		nbaDst.setNbaUserVO(user);
		NbaCompanionCaseFacadeBean bean = new NbaCompanionCaseFacadeBean();
		List companionCases = bean.retrieveCompanionCases(nbaDst.getNbaUserVO(), nbaDst);
		return companionCases;		
	}
}