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
/** 
 * NbaRulesDataProcessor processes the NbaRulesData for cache, insert, update, delete etc.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>CR 57873</td><td>Version 1</td><td>Initial Version</td></tr>
 * <tr><td>SR831136 APSL4088</td><td>Descretionary</td><td>PCCM Workflow</td></tr>
 * </table>
 * <p>
 */
import java.util.ArrayList;
import java.util.List;

import com.csc.fsg.nba.database.AxaRulesDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaAssignmentRulesVO;
import com.csc.fsg.nba.vo.AxaUnderWriterPODVO;
import com.csc.fsg.nba.vo.AxaUnderwriterWeightRules;



public class AxaRulesDataProcessor {

	public static List getRules(AxaAssignmentRulesVO rulesVO) throws NbaDataAccessException{
		//APSL3883 Code deleted.
		return AxaRulesDataBaseAccessor.getInstance().getUnderwriterRules(rulesVO);

	}
	public static List getUWQueues(List uwIds) throws NbaDataAccessException{
		return AxaRulesDataBaseAccessor.getInstance().getUWQueueForRules(uwIds);
	}
	public static AxaUnderwriterWeightRules getFactor(AxaUnderwriterWeightRules weightRulesVO) throws NbaDataAccessException{
		AxaRulesDataBaseAccessor.getInstance().calculateWeightedFactorForCase(weightRulesVO);
		return weightRulesVO;
	}
	public static void updateScoreAndCount(AxaUnderWriterPODVO uwPod) throws NbaDataAccessException{
		AxaRulesDataBaseAccessor.getInstance().updateCurrentScoreAndCount(uwPod);
	}
	public static List getUWQueuesForPOD(AxaAssignmentRulesVO ruleVO) throws NbaDataAccessException {
		List underWriterQueues = null;
		List rules = getRules(ruleVO);
		if (!NbaUtils.isBlankOrNull(rules)) {
			List uwQueues = getUWQueuesForRules(rules);
			if (!NbaUtils.isBlankOrNull(uwQueues)) {
				underWriterQueues = getUWQueues(uwQueues);
			}else {
				underWriterQueues = new ArrayList();
			}
		} else {
			underWriterQueues = new ArrayList();
		}
		return underWriterQueues;
	}
	public static List getUWQueuesForRules(List rules) throws NbaDataAccessException{
		return AxaRulesDataBaseAccessor.getInstance().getUWQsForRules(rules);
	}
	
	public static String getUWCMQueue(String underwriterQueue) throws NbaDataAccessException{
		return AxaRulesDataBaseAccessor.getInstance().getUWCasemanagerQueue(underwriterQueue);
	}
	/**
	 * @param assignmentRulesVO
	 * @param underWriterLOB
	 * @return
	 * @throws NbaDataAccessException
	 */
	public static List getUWCMQueue(AxaAssignmentRulesVO assignmentRulesVO, String underWriterQ) throws NbaDataAccessException {
		List uwcmQueue = new ArrayList();
		List rules = null; //APSL4839
		if(!assignmentRulesVO.isRetail() && !assignmentRulesVO.isSiCase() && !assignmentRulesVO.isTconvOPAI() && !assignmentRulesVO.isGiApp() && !assignmentRulesVO.isCoilProduct()){ //APSL4839,NBLXA-1632,NBLXA-1801
			rules = AxaRulesDataBaseAccessor.getInstance().getWholesaleRCMRules(assignmentRulesVO);
		} else {		
			rules = AxaRulesDataBaseAccessor.getInstance().getUWCasemanagerRules(assignmentRulesVO, underWriterQ);
		}
		if (!NbaUtils.isBlankOrNull(rules)) {
			uwcmQueue = getUWQueuesForRules(rules);
		}
		return uwcmQueue;
	}		
		
	//APSL4088 new method
	public static List getPCCMRules(AxaAssignmentRulesVO rulesVO) throws NbaDataAccessException{		
		return AxaRulesDataBaseAccessor.getInstance().getPCCMRules(rulesVO);

	}
	
	//APSL4685 new method
	public static List getPICMRules(AxaAssignmentRulesVO rulesVO) throws NbaDataAccessException{		
		return AxaRulesDataBaseAccessor.getInstance().getPICMRules(rulesVO);

	}
	
	//APSL4088 new method
	public static List getPCCMQueuesForRules(List rules) throws NbaDataAccessException{
		return AxaRulesDataBaseAccessor.getInstance().getUWQsForRules(rules);
	}
	
	//APSL4685 New Method
	public static List getPICMQueuesForRules(List rules) throws NbaDataAccessException{
		return AxaRulesDataBaseAccessor.getInstance().getUWQsForRules(rules);
	}
	
	/**
	 * Gets the list of Paid Change Case Managers from Table PCCM_RULES  
	 * @param  AxaAssignmentRulesVO           
	 * @return List 
	 */
	//APSL4088 new method
	public static List getPCCMQueues(AxaAssignmentRulesVO ruleVO) throws NbaDataAccessException {
		List pccmQueues = null;
		List rules = getPCCMRules(ruleVO);
		if (!NbaUtils.isBlankOrNull(rules)) {
			pccmQueues = getPCCMQueuesForRules(rules);			
		} 
		
		if (NbaUtils.isBlankOrNull(pccmQueues)) {
			pccmQueues = new ArrayList();
		}		
		return pccmQueues;
	}
	
	/**
	 * Gets the list of Post Issue Case Managers  
	 * @param  AxaAssignmentRulesVO           
	 * @return List 
	 */
	//APSL4685 New Method
	public static List getPICMQueues(AxaAssignmentRulesVO ruleVO) throws NbaDataAccessException {
		List picmQueues = null;
		List rules = getPICMRules(ruleVO);
		if (!NbaUtils.isBlankOrNull(rules)) {
			picmQueues = getPICMQueuesForRules(rules);
		}
		if (NbaUtils.isBlankOrNull(picmQueues)) {
			picmQueues = new ArrayList();
		}		
		return picmQueues;
	}
	//NBLXA-2339
	public static void insertRulesUct(String hiddenParam) throws NbaDataAccessException {
		AxaRulesDataBaseAccessor.getInstance().insertIntoRulesUct(hiddenParam);
	}
	//NBLXA-2339
	
	//New Method: NBLXA-2328[NBLXA-2595]
	public static List<AxaUnderWriterPODVO> getQueueListByTeam(AxaUnderWriterPODVO axaUnderWriterPODVO) throws NbaDataAccessException {
		return AxaRulesDataBaseAccessor.getInstance().getQueueListByTeam(axaUnderWriterPODVO.getTeam(), axaUnderWriterPODVO.getAssignType());
	}
}
