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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.util.ArrayList;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * This class is responsible for creating request for Life 70 Calculation webservice.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL029</td><td>AXA Life Phase 2</td><td>Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSLife70PrintCalculationInvoker extends AxaWSLife70CalculationInvoker {
	private static final String CATEGORY = "L70PrintCalculation";
	private static final String FUNCTIONID = "performCalculation";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSLife70PrintCalculationInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}
	
	/**
	 * Override method for handling validation resopnse as its handeled by calling class
	 * 
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 */
//	ALII1215 rewrote
	protected void handleResponse() throws NbaBaseException {
        TransResult transResult = ((NbaTXLife) getWebserviceResponse()).getTransResult();
        if (transResult != null && transResult.getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE) {
			StringBuffer errorString = new StringBuffer();
			List resultInfoList = transResult.getResultInfo();
			if (resultInfoList != null && resultInfoList.size() > 0) {
				for (int i = 0; i < resultInfoList.size(); i++) {
					ResultInfo resultInfo = (ResultInfo) resultInfoList.get(i);
					if (i > 0)
						errorString.append(" Error count : " + i);
					long resultInfoCode = resultInfo.getResultInfoCode();
					if (!NbaUtils.isBlankOrNull(resultInfoCode))
						errorString.append(" Error Code : (" + resultInfoCode + ") "
								+ NbaTransOliCode.lookupText(NbaOliConstants.RESULT_INFO_CODES, resultInfoCode) + "\n");
					if (!NbaUtils.isBlankOrNull(resultInfo.getResultInfoDesc()))
						errorString.append(" Error Desc : " + resultInfo.getResultInfoDesc());

					errorString.append("\n");
				}
				getLogger().logError("Error Messages in Response for " + getOperation() + ":" + errorString.toString());
				throw new NbaBaseException("Failure response : " + errorString.toString());
			}
		} else if (transResult != null && transResult.getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESSINFO) {
			Holding holding = ((NbaTXLife) getWebserviceResponse()).getPrimaryHolding();
			List qSysMsg = new ArrayList();
			for (int i = 0; i < holding.getSystemMessageCount(); i++) {
				SystemMessage sysM = holding.getSystemMessageAt(i);
				SystemMessageExtension sysMX = NbaUtils.getFirstSystemMessageExtension(sysM);
				if (NbaOliConstants.OLI_MSGSEVERITY_SEVERE == sysM.getMessageSeverityCode()
						|| (sysMX != null && sysMX.getMsgRestrictCode() == NbaOliConstants.NBA_MSGRESTRICTCODE_RESTCONTRACTPRINT)) {
					qSysMsg.add(sysM.getMessageDescription());
				}
			}
			if ( qSysMsg.size() > 0 ){
				throw new NbaDataException(qSysMsg);				
			}
		}
	}
}
