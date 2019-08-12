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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaValidationMessageData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * 
 * This class invokes the Zip Code interface for validation of the Zip Code of the US address which are appentered by the user.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>P2AXAL038</td><td>AXA Life Phase 2</td><td>Zip Code Interface</td>
 * <td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaValidateZipCodeTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
	
	protected static long[] changeTypes = { DC_ADDRESS_ZIP };


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

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded() && !nbaTxLife.isSIApplication()) { // APSL2808
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_VALIDATE_ZIP, user, nbaTxLife,
					nbaDst, null);
			NbaTXLife nbaTXLifeResponse = (NbaTXLife) webServiceInvoker.execute();
			processTxLifeResponse(nbaTXLifeResponse, nbaTxLife, nbaDst);
		}
		return nbaDst;
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes; 
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ZIP_INTERFACE_CALL_SWITCH);
	}
	/**
	 * 
	 * @param resultCode
	 * @return
	 */
	protected boolean errorStop(long resultCode) {
		if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == resultCode || NbaOliConstants.TC_RESINFO_SECVIOLATION == resultCode) {
			return true;
		}
		return false;
	}
	
	protected void processTxLifeResponse(NbaTXLife nbaTXLifeResponse, NbaTXLife nbaTXLife, NbaDst nbaDst) throws NbaBaseException {
		if (nbaTXLifeResponse != null) {
			OLifE oLife = nbaTXLifeResponse.getOLifE();
			if (oLife != null && oLife.getSystemMessageCount() > 0) {
				removeZipcodeMessages(nbaTXLife);
				List messageList = oLife.getSystemMessage();
				ListIterator messageIterator = messageList.listIterator();
				SystemMessage currentMessage = null;
				Holding holding = nbaTXLife.getPrimaryHolding();
				while (messageIterator.hasNext()) {
					currentMessage = (SystemMessage) messageIterator.next();
					addValidationMessage(nbaTXLife, nbaDst, (" : " + currentMessage.getMessageDescription()));
				}
				holding.setActionUpdate();
			}
		}
	}
	
	
	
	/**
	 * Add a validation error message to the contract. 
	 * @param req An instance of <code>NbaEvaluateRequest</code>
	 * @throws NbaBaseException
	 */
	protected void addValidationMessage(NbaTXLife nbaTXLife, NbaDst nbaDst, String description) throws NbaBaseException {
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		HashMap aMap = nbaTableAccessor.setupTableMap(nbaDst);
		aMap.put("msgCode", String.valueOf(NbaConstants.ZIP_CODE_ERROR));
		NbaValidationMessageData nbaTableData = (NbaValidationMessageData) nbaTableAccessor.getDataForOlifeValue(aMap,
				NbaTableConstants.NBA_VALIDATION_MESSAGE, String.valueOf(NbaConstants.ZIP_CODE_ERROR));

		Holding holding = nbaTXLife.getPrimaryHolding();
		SystemMessage msg = new SystemMessage();
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTXLife);
		nbaOLifEId.setId(msg);
		msg.setMessageCode(NbaConstants.ZIP_CODE_ERROR);
		msg.setMessageDescription(description);
		msg.setRelatedObjectID(holding.getId());
		msg.setSequence("0");
		msg.setMessageSeverityCode(nbaTableData.getMsgSeverityTypeCode());
		msg.setMessageStartDate(new Date(System.currentTimeMillis()));
		msg.setActionAdd();

		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
		msg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = olifeExt.getSystemMessageExtension();
		systemMessageExtension.setMsgOverrideInd(false);
		systemMessageExtension.setMsgValidationType(NbaConstants.SUBSET_ZIPCODE);
		systemMessageExtension.setMsgRestrictCode(NbaOliConstants.NBA_MSGRESTRICTCODE_RESTCONTRACTPRINT);

		holding.addSystemMessage(msg);
	}

	protected void removeZipcodeMessages(NbaTXLife nbaTxLife) {
		Holding holding = nbaTxLife.getPrimaryHolding();
		SystemMessage systemMessage;
		List systemMessageList = holding.getSystemMessage();
		int count = systemMessageList.size();
		for (int i = 0; i < count; i++) {
			systemMessage = (SystemMessage) systemMessageList.get(i);
			if (systemMessage.getMessageCode() == (NbaConstants.ZIP_CODE_ERROR)) {
				systemMessage.setActionDelete();
			}
		}
		holding.setActionUpdate();
	}
}
