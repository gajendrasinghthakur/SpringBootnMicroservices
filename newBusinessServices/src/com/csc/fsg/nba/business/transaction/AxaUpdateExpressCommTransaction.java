/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;


/**
 * 
 * This class encapsulates checks whenever the Primary Writing Agent is added, deleted on the case, Financial Activity is reversed on the case, ReplacementType is changed, ModalPremAmt is changed.
 * If any of the above change occurs then the class calls ECS update web service.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td>
 * <td>SR494086.7</td><td>Discretionary</td><td>Interfaces</td>
 * <tr><td>CR57907</td><td></td><td>Xpress Commission Transaction</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateExpressCommTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;

	protected long[] changeTypes = { 
			//DC_MODAL_AMT_CHG, //CR57907 Retrofit
			DC_FIN_ACT_REVERSE,
			//DC_REPL_TYPE_CHG, //CR57907 Retrofit
			DC_PRIMARY_AGENT_DELETE,
			DC_AGENT_ELIGIBLE_TO_ELIGIBLE,
			DC_ADDI_AGENT_DELETE,
			DC_ADDI_AGENT_ADD
	};

	/*
	 * (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded() && !nbaTxLife.isSIApplication()) { // APSL2808
			AxaWSInvoker webServiceInvoker = null;
			//begin ALPC136
			//begin ALS4633
			List changeSubTypeList = createChangeSubTypeList();
			//to avoid redundant calls, check if change registered is eligible to eligible and some other critical data also changed
			if (hasChangeSubType(DC_AGENT_ELIGIBLE_TO_ELIGIBLE) && ((changeSubTypeList.size() > 1 || isAddiAgentModified()) || (changeSubTypeList.size() == 1 && isPrimaryAgentModified()))) { //ALS4908 //QC6228(APSL1245)
                webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ECSSUBMIT, user, nbaTxLife, nbaDst,
                        "one of a pair"); //SR494086.7 ADC Retrofit
                NbaTXLife nbaTXLifeResponse = (NbaTXLife) webServiceInvoker.execute();
                if (nbaTXLifeResponse != null) { //ALS5144/ALS5128
                    Map parametersMap = new HashMap();
                    String transRefGUID = nbaTXLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0)
                            .getTransRefGUID();
                    parametersMap.put("transRefGUID", transRefGUID);
                    parametersMap.put("changeSubTypeList", changeSubTypeList);
                    webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_ECSUPDATE, user, nbaTxLife, nbaDst,
                            parametersMap); //SR494086.7 Retrofit
                    webServiceInvoker.execute();
                }//ALS5144/ALS5128	
                //end ALS4633
            }
		}//end ALPC136
		return nbaDst;
	}

	/**
	 * @return
	 */
	 //ALS4633 signature changed
	protected List createChangeSubTypeList() {
		List changeSubTypeList = new ArrayList();
		boolean agentDeleted = hasChangeSubType(DC_PRIMARY_AGENT_DELETE);//ALS4633
		//ALS4633 code deleted
		Iterator registerChangesItr = registeredChanges.iterator();
		while (registerChangesItr.hasNext()) {
			AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
			ChangeSubType changeSubType = null;
			if (DC_AGENT_ELIGIBLE_TO_ELIGIBLE == change.getChangeType()) {
				long transContentCode = NbaOliConstants.TC_CONTENT_UPDATE;
				if (agentDeleted) {
					transContentCode = NbaOliConstants.TC_CONTENT_DELETE;
				}
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), transContentCode);
			}
			if (DC_AGENT_ELIGIBLE_TO_INELIGIBLE == change.getChangeType()) {
				//ALS3337,ALS3591 begin
				//ALS4633 code deleted
				long transContentCode = NbaOliConstants.TC_CONTENT_UPDATE;
				if (agentDeleted) {
					transContentCode = NbaOliConstants.TC_CONTENT_DELETE;
				}
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PARTYINFO, change.getChangedObjectId(), transContentCode);
				//ALS3337,ALS3591 end
			}
			//ALS4633 code deleted
			if (DC_MODAL_AMT_CHG == change.getChangeType()) {//ALS4633 
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PREMAMT, NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_FIN_ACT_REVERSE == change.getChangeType()) {
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_CHGBILLING, NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (DC_REPL_TYPE_CHG == change.getChangeType()) {//ALS4633
				changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_REPLTYPE, NbaOliConstants.TC_CONTENT_UPDATE);
			}
			if (changeSubType != null) {
				changeSubTypeList.add(changeSubType);
			}
		}
		return changeSubTypeList;
	}

	/*
	 * (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.COMPENSATION_INTERFACE_CALL_SWITCH);
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	//ALS4908 new method
	protected boolean isAddiAgentModified(){
		return hasChangeSubType(DC_ADDI_AGENT_ADD) || hasChangeSubType(DC_ADDI_AGENT_DELETE);
	}
	//QC6228(APSL1245) new method
	protected boolean isPrimaryAgentModified(){
		return hasChangeSubType(DC_PRIMARY_AGENT_ADD) || hasChangeSubType(DC_PRIMARY_AGENT_DELETE);
	}
	
}
