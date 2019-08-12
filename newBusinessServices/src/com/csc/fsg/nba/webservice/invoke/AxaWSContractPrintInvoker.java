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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.UserLoginNameAndUserPswd;

/**
 * This class is responsible for creating request for Contract Print.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.14</td><td>AXA Life Phase 1</td><td>Contract Print</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSContractPrintInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "ContractPrintExtract";

	private static final String FUNCTIONID = "storeExtracts";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSContractPrintInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	public NbaTXLife createRequest() throws NbaBaseException {
		UserLoginNameAndUserPswd userPswd = getNbaTXLife().getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest()
				.getUserLoginNameAndUserPswdOrUserSessionKey().getUserLoginNameAndUserPswd();
		userPswd.setUserLoginName(getUserVO().getUserID());
		userPswd.getUserPswd().getPswdOrCryptPswd().setPswd(getUserVO().getPassword());

		getNbaTXLife().getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest().getUserLoginNameAndUserPswdOrUserSessionKey()
				.setUserLoginNameAndUserPswd(userPswd);
		return getNbaTXLife();
	}

	public void cleanRequest() throws NbaBaseException { //APSL371 APSL372
		super.cleanRequest();
		getNbaTxLifeRequest().getPrimaryHolding().setSystemMessage(new ArrayList());//ALS1246 removal of all the System Messages from TX 500
		getNbaTxLifeRequest().getPrimaryHolding().setAttachment(new ArrayList());//AXAL3.7.14
		getNbaTxLifeRequest().getOLifE().setPolicyProduct(new ArrayList());
		NbaUtils.translateBESValueForSuffix(getNbaTxLifeRequest()); //APSL371 APSL372
	}
}
