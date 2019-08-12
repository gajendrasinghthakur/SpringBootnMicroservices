package com.csc.fsg.nba.business.process;

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
 * 
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.evaluation.NbaVpmsModelProcessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaPollingException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.EvaluationControlModelResults;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * NbaProcContractEvaluation is the class that processes nbAccelerator cases found
 * on the AWD contract evaluation queue (NBCTEVAL).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN008</td><td>Version 4</td><td>Underwriting Workflow Changes</td></tr>
 * <tr><td>ACN010</td><td>Version 4</td><td>Evaluation Control</td></tr>
 * <tr><td>ACP002</td><td>Version 4</td><td>Contract Evaluation</td></tr>
 * <tr><td>ACP022</td><td>Version 4</td><td>Foreign Travel</td></tr>
 * <tr><td>ACP013</td><td>Version 4</td><td>Family History</td></tr>
 * <tr><td>ACP008</td><td>Version 4</td><td>IU Preferred Processing </td></tr>
 * <tr><td>ACP009</td><td>Version 4</td><td>Non Medical Screening</td></tr> 
 * <tr><td>ACP007</td><td>Version 4</td><td>Medical Screening</td></tr>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2450</td><td>Version 5 </td><td>AC Installation should bypass annuity products</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR2741</td><td>Version 6</td><td>Re-evaluation is generating insert errors</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>CR731686</td><td>AXA Life Phase 2</td><td>Preferred Processing</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 * @see NbaAutomatedProcess
 */

public class NbaProcContractEvaluation extends NbaAutomatedProcess {

	private String partyID = "";
	
	public NbaProcContractEvaluation() {
		super();
		setContractAccess(UPDATE); 
	}
	
	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess#executeProcess(com.csc.fsg.nba.vo.NbaUserVO, com.csc.fsg.nba.vo.NbaDst)
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		boolean debugLogging = getLogger().isDebugEnabled();

		if (!initialize(user, work)) {
			return getResult(); 	
		}
		if (debugLogging) { 
			getLogger().logDebug("Contract Evaluation: contract " + getWork().getNbaLob().getPolicyNumber());
		} 
       //Begin AXAL3.7.40G
		AxaPreventProcessVpmsData preventProcessData = new AxaPreventProcessVpmsData(user, getNbaTxLife(), NbaVpmsAdaptor.CONTRACTEVALUATION);
		if (preventProcessData.isPreventsProcess() && getWork().isCase()) {//CR731686
			addComment(preventProcessData.getComments());
		} else {//End AXAL3.7.40G
			//ACN010 begins
			ArrayList alEvalControl = callEvaluationControl(work);
			// SPR3290 code deleted
			//ACN016 code deleted
			//ACN010 ends

			EvaluationControlModelResults evalModel = null;

			for (int i = 0; i < alEvalControl.size(); i++) {
				evalModel = (EvaluationControlModelResults) alEvalControl.get(i);
				NbaVpmsModelProcessor processor;
				try {
					processor = (NbaVpmsModelProcessor) NbaUtils.classForName(evalModel.getJavaImplClass()).newInstance();
					processor.initialize(nbaTxLife, user, work);
					processor.execute();
				} catch (InstantiationException e) {
					throw new NbaBaseException(NbaPollingException.CLASS_INVALID, e);
				} catch (IllegalAccessException e) {
					throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS, e);
				} catch (ClassNotFoundException e) {
					throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND, e);
					//begin SPR2652
				} catch (NbaVpmsException e) {
					if (e.isFatal()) {
						throw e;
					} else {
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.toString(), getVpmsErrorStatus()));
						addComment(getResult().getText());
						changeStatus(getResult().getStatus());
						break;
					}
					//end SPR2652
					//begin SPR2741
				} catch (NbaBaseException nbe) {
					if (nbe.isFatal()) {
						throw nbe;
					} else {
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, nbe.toString(), getHostErrorStatus()));
						addComment(getResult().getText());
						changeStatus(getResult().getStatus());
						break;
					}

				}
				//end SPR2741
			}
		}
		if (getResult() == null) {	//SPR2652
			doContractUpdate(nbaTxLife);
			changeStatus(getPassStatus());
			result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
		}	//SPR2652
		//ACN010 ends
		doUpdateWorkItem();
		return result;
	}
	
	//ACN010 new method
	public ArrayList callEvaluationControl(NbaDst work) throws NbaBaseException {
		ArrayList sortedList = null; //ACP007
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(nbaTxLife); // SPR2450, AXAL3.7.07
			oinkDataAccess.setLobSource(work.getNbaLob());// SPR2450
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.EVALUATIONCONTROL); //ACP008,SPR2450, SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_CALCXMLOBJECTS); //ACP008
			Map deOink = new HashMap();
			
			int  reqCode = work.getNbaLob().getReqType();
			deOink.put("A_REQCODE_INS",String.valueOf(reqCode));
			deOink.put("A_XMLRESPONSE","true");
			deOink.put("A_INSTALLATION", getInstallationType()); 
			deOink.put("A_WORKTYPE_LOB",work.getNbaLob().getWorkType());
			
			vpmsProxy.setSkipAttributesMap(deOink);			
			VpmsComputeResult vcr = vpmsProxy.getResults();			
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
			ArrayList results = vpmsResultsData.getResultsData();
			
			results = vpmsResultsData.getResultsData();
			//Resulting string will be the zeroth element.
			NbaVpmsModelResult vpmsOutput = null;
			VpmsModelResult vpmsModelResult = null;
			if (results == null) {	
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.EVALUATIONCONTROL,NbaExceptionType.ERROR); //SPR2652
			}
			// SPR2652 code deleted
			String result = (String) results.get(0);			
			vpmsOutput = new NbaVpmsModelResult(result);
			vpmsModelResult = vpmsOutput.getVpmsModelResult();
			// SPR2652 Code Deleted
			//SPR3362 code deleted
	
			ArrayList modelResults = vpmsModelResult.getEvaluationControlModelResults();
			// Begin ACP007
			// Modified logic to use SortedMap
			SortedMap map = new TreeMap();
			for(int i=0;i<modelResults.size();i++)	{
				EvaluationControlModelResults modelResult = (EvaluationControlModelResults)modelResults.get(i);
				Integer key = new Integer(modelResult.getProcessSequence().toString());
				map.put(key, modelResult);
			}
			// Iterate on treemap and convert to sorted ArrayList
			Set set = map.entrySet();
			Iterator itr = set.iterator();
			sortedList = new ArrayList();
			while(itr.hasNext()) {
				Map.Entry me = (Map.Entry)itr.next();
				sortedList.add(me.getValue());
			}
			// End ACP007
		// SPR2652 Code Deleted
		} catch (RemoteException re) {
			throw new NbaBaseException("Remote Exception occured in callEvaluationControl", re);
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
		return sortedList;
	}
}
