package com.csc.fsg.nba.process.requirements;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.database.AxaRulesDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaRequirement;
import com.csc.fsg.nba.vo.NbaRequirementRequestVO;
import com.csc.fsg.nba.vo.NbaRequirementResponseVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.ibm.ejs.ras.SystemOutStream;

/**
 * Retrieves a list of companion cases for the given case by calling  
 * the <code>NbaCompanionCaseFacadeBean</code>. Requires an <code>NbaDst</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Performance Tuning and Testing - - Incremental change 35</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public class RetrieveRequirementsBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve a List of companion cases for the given case
     * @param an instance of <code>NbaDst</code> object
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(retrieveRequirements((NbaRequirementRequestVO) input));
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	public NbaRequirementResponseVO retrieveRequirements(NbaRequirementRequestVO reqRequestVO) throws Exception {
	    NbaTXLife nbaTXLife = reqRequestVO.getNbaTXLife();
	    NbaRequirementResponseVO reqResponseVO = new NbaRequirementResponseVO();
	    List relations = nbaTXLife.getUIRelationList();
	    if (relations != null) {
            int count = relations.size();
            Map reqMap = new HashMap(count);  //NBA208-26
            List insuredRelations = new ArrayList(count);  
            if (reqRequestVO.isImpairmentLoaded()){
                reqResponseVO.setImpMap(new HashMap(count));
            }
            Relation relation = null;
            String partyID = null;
            List impList = null;
            for (int i = 0; i < count; i++) {
                relation = (Relation) relations.get(i);
                insuredRelations.add(relation);
                partyID = relation.getRelatedObjectID();
                reqMap.put(partyID, getRequirements(nbaTXLife, relation));  //NBA208-36 
                if (reqRequestVO.isImpairmentLoaded()){
                	impList = nbaTXLife.getImpairments(partyID);
                	reqResponseVO.getImpMap().put(partyID, impList);
                }
            }
    	    reqResponseVO.setReqMap(reqMap);  //NBA208-36
            reqResponseVO.setRelationlist(insuredRelations);
	    }
	    return reqResponseVO;
	}

	/**
	 * Return a list of NbaRequirement objects for the insured identified by the Relation
	 * @param contract
	 * @param relation
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA208-36 New Method
	protected List getRequirements(NbaTXLife contract, Relation relation) throws NbaBaseException {
        List requirements = new ArrayList();
        Policy policy = contract.getPolicy();
        String partyID = relation.getRelatedObjectID();
		AxaRulesDataBaseAccessor dataBaseAccessor = AxaRulesDataBaseAccessor.getInstance();
		Date startLNRCStartDate = dataBaseAccessor.getConfigDateValue(NbaConstants.LNRC_START_DATE);
		Date submissionDate = policy.getApplicationInfo().getSubmissionDate();
        if (policy != null) {
            int count = policy.getRequirementInfoCount();
            RequirementInfo reqInfo = null;
            for (int i = 0; i < count; i++) {
                reqInfo = policy.getRequirementInfoAt(i);
                String reqType = Long.toString(reqInfo.getReqCode());
                String riskClassifier = Long.toString(NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER);
                if (partyID.equals(reqInfo.getAppliesToPartyID()) && !((riskClassifier.equalsIgnoreCase(reqType))&& 
                		!(startLNRCStartDate != null && submissionDate != null && startLNRCStartDate.compareTo(submissionDate)<=0))) {//NBLXA-2072
                	requirements.add(new NbaRequirement(reqInfo, contract, relation));
                }
            }
           
        }
        return requirements;
	}
}
