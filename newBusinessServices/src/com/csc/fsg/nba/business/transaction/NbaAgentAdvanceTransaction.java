package com.csc.fsg.nba.business.transaction;

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


import java.util.ArrayList;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;

/**
 * NbaAgentAdvanceTransaction creates Agent Advances/Chargeback transaction requests for WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA137</td><td>Version 6</td><td>nbA Agent Advances</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public class NbaAgentAdvanceTransaction extends NbaBusinessTransactions {

	/**
	 * Constructor for NbaAgentAdvanceTransaction
	 */
	public NbaAgentAdvanceTransaction() {
		super();
	}
	
	/**
	 * Creates Agent Advance transaction request for Tx103 subtype 1000500022 WebService
	 * @param existingTXLife An instance of <code>NbaTXLife</code> holding inquiry
	 * @param nbaLob An instance of <code>NbaLob</code>
	 * @return NbaTXLife agent Advances request transaction
	 * @throws NbaBaseException
	 */    
    public static NbaTXLife createAgentAdvanceRequest(NbaTXLife holding, NbaLob nbaLob) throws NbaBaseException {
        NbaTXLife clonedHolding = (NbaTXLife)holding.clone(false); //get deep copy
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
        nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_AGENT_ADVANCE_REQUEST);
        nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        nbaTXRequest.setBusinessProcess("");
        nbaTXRequest.setNbaLob(nbaLob);

        //create txlife with default request fields
        NbaTXLife request = new NbaTXLife(nbaTXRequest);
        request.setOLifE(clonedHolding.getOLifE());
        return request;
    }
    
	/**
	 * Creates Agent Advance Chargeback transaction request, that goes to the webservice
	 * @param holding An instance of <code>NbaTXLife</code> holding inquiry
	 * @param nbaLob An instance of <code>NbaLob</code>
	 * @return NbaTXLife agent Advances chargeback request transaction
	 * @throws NbaBaseException
	 */    
    public static NbaTXLife createAgentAdvanceChargebackRequest(NbaTXLife holding, NbaLob nbaLob) throws NbaBaseException {
 
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_APPROVE);
        nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        nbaTXRequest.setNbaLob(nbaLob);
        nbaTXRequest.setBusinessProcess("");

        //create txlife with default request fields
        NbaTXLife newNbaTxLife = new NbaTXLife(nbaTXRequest);
        // SPR3290 code deleted
		Holding newHolding = NbaTXLife.getPrimaryHoldingFromOLifE(newNbaTxLife.getOLifE());
		newHolding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		
		Policy policy = newHolding.getPolicy();
        
		policy.setProductType(holding.getPrimaryHolding().getPolicy().getProductType());
        ApplicationInfoExtension existingAppInfoExtension = 
            NbaUtils.getFirstApplicationInfoExtension(holding.getPrimaryHolding().getPolicy().getApplicationInfo());
        if (existingAppInfoExtension != null) {
            long underwritingStatus = existingAppInfoExtension.getUnderwritingStatus();
            
            if (nbaLob.getBackendSystem().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) { //map the underwriting status value to correct polstat value
                if (NbaOliConstants.NBA_FINALDISPOSITION_REJMEDICAL == underwritingStatus
                        || NbaOliConstants.NBA_FINALDISPOSITION_REJNONMEDICAL == underwritingStatus) {
                    policy.setPolicyStatus(NbaOliConstants.OLI_POLSTAT_DECISSUE);
                } else if (NbaOliConstants.NBA_FINALDISPOSITION_REJINCOMPLETE == underwritingStatus) {
                    policy.setPolicyStatus(NbaOliConstants.OLI_POLSTAT_INCOMPLETE);
                } else {
                    policy.setPolicyStatus(underwritingStatus);
                }
    		} else {
    		    policy.setPolicyStatus(underwritingStatus); //for CLIF policy status same as underwriting status.
    		}
        }        
        
        //policy.setEffDate("2000-12-15");
        
         return newNbaTxLife;
    }
    
	/**
	 * Create a 103 transaction from holding inquiry and workitem's LOBs 
	 * to issue/reissue a policy on admin system. 
	 * @return the NbaTXLife for the Issue transaction
	 * @throws NbaBaseException when unable to retrieve the PolicyProduct for the Contract
	 */
	public static NbaTXLife createAgentAdvanceRequestTransaction(NbaTXLife nbaTXLife, NbaLob nbaLob) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_AGENT_ADVANCE_REQUEST);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(nbaTXLife.getBusinessProcess());
		//create txlife with default request fields
		NbaTXLife txLifeIssue = new NbaTXLife(nbaTXRequest);
		OLifE olifeIssue = nbaTXLife.getOLifE().clone(false);
		Holding holdingIssue = NbaTXLife.getPrimaryHoldingFromOLifE(olifeIssue);
		//remove any replcement policies
		int count = olifeIssue.getHoldingCount() -1;
		for (int i = count; i > -1; i--) {
            if (olifeIssue.getHoldingAt(i).getId() != holdingIssue.getId()) {
                olifeIssue.removeHoldingAt(i);
            }
        }
		//remove holding to holding relations
        count = olifeIssue.getRelationCount() - 1;
        for (int i = count; i > -1; i--) {
            if (olifeIssue.getRelationAt(i).getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY) {
                olifeIssue.removeRelationAt(i);
            }
        }
        //remove all SystemMessage objects
        holdingIssue.setSystemMessage(new ArrayList());
		
		//Remove Ids on CarrierAppointment objects
		count = olifeIssue.getPartyCount();
		for (int i = 0; i < count; i++) {
			Producer producer = (olifeIssue.getPartyAt(i)).getProducer();
			if(producer != null) {
				int cnt = producer.getCarrierAppointmentCount();
				for (int j = 0; j < cnt; j++) {
				    producer.getCarrierAppointmentAt(j).deleteId();
				}
			}
		}		
		//Delete Risk objects 
		count = olifeIssue.getPartyCount();
		for (int i = 0; i < count; i++) {
			olifeIssue.getPartyAt(i).deleteRisk();
		}
		//Delete ProductObjective objects
		if (holdingIssue.hasInvestment()) {
			count = holdingIssue.getInvestment().getSubAccountCount();
			for (int i = 0; i < count; i++) {
				holdingIssue.getInvestment().getSubAccountAt(i).deleteProductObjective();
			}
		}
		//Set ghost values to non null to prevent them from being hydrated from the database
		holdingIssue.setIntentGhost(new ArrayList());
		holdingIssue.getPolicy().setAltPremModeGhost(new ArrayList());
		holdingIssue.getPolicy().setRequirementInfoGhost(new ArrayList());
		//Remove reveral/refund FinancialActivity objects
		count = holdingIssue.getPolicy().getFinancialActivityCount() -1;
		long type;
		for (int i = count; i > -1; i--) {
            type = holdingIssue.getPolicy().getFinancialActivityAt(i).getFinActivityType();
            if (type == NbaOliConstants.OLI_FINACT_CWAREVERSAL || type == NbaOliConstants.OLI_FINACT_CWAREFUND) {
                holdingIssue.getPolicy().removeFinancialActivityAt(i);
            }
        }

		UserAuthRequestAndTXLifeRequest requestIssue = txLifeIssue.getTXLife().getUserAuthRequestAndTXLifeRequest();
		if (requestIssue != null) {
			if (requestIssue.getTXLifeRequestCount() > 0) {
				requestIssue.getTXLifeRequestAt(0).setOLifE(olifeIssue);
			}
		}

		return txLifeIssue;
	}    
}
