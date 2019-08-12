/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;


import java.util.Date;
import java.util.List;

import com.csc.fsg.nba.contract.validation.NbaContractValidationConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>P2AXAL041</td><td>AXA Life Phase 2</td><td>Message received from OLSA Unit Number Validation Interface</td>
 * <td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaInvokeOLSATransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
	
	
	protected static long[] changeTypes = { DC_BILLING_UNIT_NUMBER, DC_NEW_CONTRACT, DC_PAYMENT_METHOD };
	

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
	
	/**
	 * Calls the interface of the webservice
	 * @param nbaTXLife
	 * @param userVO
     * @param nbaDst
     * @throws NbaBaseException
     */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded() && !nbaTxLife.isSIApplication() && !NbaUtils.isProductCodeCOIL(nbaTxLife)) { // NBLXA-1632 // APSL2808 
			removeBillingSystemMessage(nbaTxLife);
			if (nbaTxLife.getPolicy().getPaymentMethod() == NbaOliConstants.OLI_PAYMETH_LISTBILL) {
				try {
					AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CHECK_BILLING_UNIT_NUMBER,
							user, nbaTxLife, nbaDst, null);
					webServiceInvoker.execute();
				} catch (NbaBaseException e) {
					addBillingSystemMessage(nbaTxLife, e.toString());
				}
			}
		}
		return nbaDst;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes; 
		
	}


	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		// TODO Auto-generated method stub
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_DATA_CHANGE_OLSA_CALL);
	}

	/**
	 * Removes the billingSystemMessage from holding, if already exists, before adding new message 
	 * @param nbaTXLife
     */
	protected void removeBillingSystemMessage(NbaTXLife nbaTxLife) {
		Holding holding = nbaTxLife.getPrimaryHolding();
		SystemMessage systemMessage;
		List systemMessageList = holding.getSystemMessage();
		int count = systemMessageList.size();
		for (int i = 0; i < count; i++) {
			systemMessage = (SystemMessage) systemMessageList.get(i);
			if(systemMessage.getMessageCode() == (NbaConstants.BILLING_NUMBER_INVALID)){
				systemMessage.setActionDelete();
			}
		}
		holding.setActionUpdate();
	}
	
	/**
	 * Adds the billingSystemMessage to holding 
	 * @param nbaTXLife
	 * @param msgDescription
     */
	protected void addBillingSystemMessage(NbaTXLife nbaTxLife, String msgDescription) {
		Holding holding = nbaTxLife.getPrimaryHolding();
		SystemMessage msg = new SystemMessage();
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
		nbaOLifEId.setId(msg);//ALS4814
		msg.setMessageCode(NbaConstants.BILLING_NUMBER_INVALID);
		msg.setRelatedObjectID(holding.getId());
		msg.setSequence("0");
		msg.setMessageSeverityCode(NbaOliConstants.OLI_MSGSEVERITY_SEVERE);
		msg.setMessageStartDate(new Date(System.currentTimeMillis()));
		if(msgDescription.length() > 100){
			msgDescription = msgDescription.substring(0,99);	
		}
		msg.setMessageDescription(msgDescription);
		msg.setActionAdd();

		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
		msg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = olifeExt.getSystemMessageExtension();
		systemMessageExtension.setMsgOverrideInd(false);
		systemMessageExtension.setMsgRestrictCode(NbaOliConstants.NBA_MSGRESTRICTCODE_RESTCONTRACTPRINT);
		systemMessageExtension.setMsgValidationType(NbaContractValidationConstants.SUBSET_OLSA); //ALII714
		holding.addSystemMessage(msg);
		holding.setActionUpdate();
	}

}
