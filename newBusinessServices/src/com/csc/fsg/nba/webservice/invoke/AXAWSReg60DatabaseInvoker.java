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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This class is responsible for creating request for Reg60 Database webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL039</td><td>AXA Life Phase 2</td><td>Reg60 Database Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AXAWSReg60DatabaseInvoker extends AxaWS1203Invoker {
	
	private static final String CATEGORY = "AxaReg60Database";

	private static final String FUNCTIONID = "submitPresaleData";
	
	
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AXAWSReg60DatabaseInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	/**
	 * This method first calls the superclass createRequest() and then set the request specefic attribute.
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXLife nbaTXLife = super.createRequest();
		nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransSubType(NbaOliConstants.TC_SUBTYPE_REG60DB_HOLDING_TRANSMIT);
		return nbaTXLife;
	}

}
