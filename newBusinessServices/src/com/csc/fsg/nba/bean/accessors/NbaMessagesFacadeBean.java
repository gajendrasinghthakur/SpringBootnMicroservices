package com.csc.fsg.nba.bean.accessors;

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
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionBean;

import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaMessagesTableVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;



/**
 * This is a stateless session bean class implementation for Enterprise Bean: NbaMessagesFacade
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA065</td><td>Version 3</td><td>Contract Error Override</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA091</td><td>Version 3</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR1850</td><td>Version 4</td><td>Problems occur in the Messages view if any SystemMessage contains a newline character in the MessageDescription</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>SPR1906</td><td>Version 4</td><td>General clean-up</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>Code clean up</td></tr>
 * <tr><td>SPR2113</td><td>Version 4</td><td>System Messages are not displayed and unknown error message is displayed for non-new applications</td></tr>
 * <tr><td>SPR2195</td><td>Version 4</td><td>System messages are not displayed in Contract Message view for wrappered mode</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */




/**
 * Bean implementation class for Enterprise Bean: NbaMessagesFacade
 */
public class NbaMessagesFacadeBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
	
	protected NbaTableAccessor nbaTableAccessor;	//NBA093
	protected Map tblKeys; //NBA093

	/**
	 * This method is used to retrieve the SystemMesages objects from xml203 transaction and create the List of 
	 * <code>NbaMessagesTableVO</code> object to populate the messages on the messages view.
	 * @param holdingInquiry An instance of <code>NbaTXLife</code>
	 * @return Lits of NbaMessagesTableVO Messages value objects.
	 * @throws NbaBaseException
	 */	
	//NBA103
	public List loadMessagesTableInfo(NbaTXLife holdingInquiry) throws NbaBaseException{
		try {//NBA103
			SystemMessage messageDetail = null;  //NBA093
			List allMessages = new ArrayList();
	//spr1906 - removed spurious Object[] declaration
	
			Holding holding = holdingInquiry.getPrimaryHolding();  //NBA093
			int messageSize = holding.getSystemMessageCount();  //NBA093
		
			NbaMessagesTableVO messagesTableVO = null;
			boolean dataStoreDB = NbaServerUtility.isDataStoreDB(holdingInquiry);  //NBA091 //SPR1715 SPR1234
			for (int j = 0; j < messageSize; j++) {
				messageDetail = holding.getSystemMessageAt(j);  //NBA093
				SystemMessageExtension messageExt = NbaUtils.getFirstSystemMessageExtension(messageDetail);  //NBA093
				messagesTableVO = new NbaMessagesTableVO();
				//begin NBA093
				if (messageExt != null) {
					messagesTableVO.setOverride((messageExt.getMsgOverrideInd()==true)?"true":"false");
					messagesTableVO.setType(Long.toString(messageExt.getMsgValidationType()));
				}
				messagesTableVO.setSeverity(Long.toString(messageDetail.getMessageSeverityCode()));  //NBA093
				//NBA093 deleted line
				messagesTableVO.setIdentification((messageDetail.getRelatedObjectID() != null) ? messageDetail.getRelatedObjectID() : "");  //NBA093
				//begin NBA093
				StringBuffer sb = new StringBuffer();
				if (messageDetail.hasMessageCode()) {
					sb.append(getMessageTranslation(holdingInquiry, messageDetail.getMessageCode(), dataStoreDB));	//NBA093 SPR1234				
				}
				if (messageDetail.getMessageDescription() != null) {
					sb.append(" ");	//NBA064
					sb.append(messageDetail.getMessageDescription());
				//NBA064 code deleted
				}
				
				//SPR2195 code deleted.
				String tempDesc = sb.toString().replace('\n', ' '); //SPR2113
				tempDesc = tempDesc.replace('\"', ' ');	//SPR2113	 		
				messagesTableVO.setDescription(tempDesc);	//SPR1850 //SPR2113
				
				//end NBA093
				messagesTableVO.setAdditionaldetail("");
				//begin NBA093
				if (messageExt != null) {
					messagesTableVO.setOverriddenby((messageExt.getMsgOverriddenByID() != null) ? messageExt.getMsgOverriddenByID() : "");
					messagesTableVO.setOverridedate((messageExt.getMsgOverrideDate() != null) ? messageExt.getMsgOverrideDate().toString() : "");
				}
				//end NBA093
				messagesTableVO.setMessageDetailId((messageDetail.getId() != null) ? messageDetail.getId() : "");
				allMessages.add(j, messagesTableVO);
			}
			return allMessages;
		} catch(Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
	}
	/**
	 * Return the translated value for a MessageCode value.
	 * @return com.csc.fsg.nba.sessionfacade.NbaMessagesFacadeHome
	 */
	//NBA093 new method
	protected String getMessageTranslation(NbaTXLife nbaTXLife, int messageCode, boolean dataStoreDB) {	//SPR1234 changed method signature
		StringBuffer sb = new StringBuffer();
		String strMessageCode = Integer.toString(messageCode);
		try {
			if (dataStoreDB) { //NBA091 //SPR1715 SPR1234
				sb.append(messageCode);
				sb.append(": ");
				//begin NBA064
				sb.append(
					getNbaTableAccessor()
						.getDataForOlifeValue(
							getTblKeys(nbaTXLife, strMessageCode),
							NbaTableConstants.NBA_VALIDATION_MESSAGE,
							strMessageCode)
						.text());
				 //end NBA064
			} else {
				sb.append(
					getNbaTableAccessor()
						.getDataForOlifeValue(getTblKeys(nbaTXLife, strMessageCode), NbaTableConstants.NBA_VALIDATION_TRANSLATIONS, strMessageCode)
						.text());
				sb.append(": ");
			}
		} catch (Throwable e) {
			NbaLogFactory.getLogger(this.getClass()).logInfo(e);	//NBA103
			sb.append("Unable to translate message number");	//NBA103
		}
		return sb.toString();
	}
	/**
	 * Returns a new NbaTableAccessor.
	 * @return NbaTableAccessor
	 * @see com.csc.fsg.nba.tableaccess.NbaTableAccessor
	 */
	//NBA093 new method
	protected NbaTableAccessor getNbaTableAccessor() {
		if (nbaTableAccessor == null) {
			nbaTableAccessor = new NbaTableAccessor();
		}
		return nbaTableAccessor;
	}	
	/**
	 * Get a reference to the table manager.
	 * @return com.csc.fsg.nba.foundation.NbaTableManager
	 */
	//NBA093 new method
	protected Map getTblKeys(NbaTXLife nbaTXLife, String strMessageCode) {
		if (tblKeys == null) {
			tblKeys = new HashMap();
			nbaTXLife.getBackendSystem();
			tblKeys.put("company", nbaTXLife.getCarrierCode());
			tblKeys.put("tableName", NbaTableConstants.NBA_VALIDATION_TRANSLATIONS);
			String plan = "*";
			if (nbaTXLife.getPolicy() != null && nbaTXLife.getPolicy().hasProductCode()) {
				plan = nbaTXLife.getPolicy().getProductCode();
			} else {
				Coverage coverage = nbaTXLife.getPrimaryCoverage();
				if (coverage != null && coverage.hasProductCode()) {
					plan = coverage.getProductCode();
				}
			}
			tblKeys.put("plan", plan);
			tblKeys.put("backendSystem", nbaTXLife.getBackendSystem());
		}
		tblKeys.put("msgCode", strMessageCode);
		return tblKeys;
	}
}
