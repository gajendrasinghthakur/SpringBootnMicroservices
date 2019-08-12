package com.csc.fsg.nba.process.contract.info;

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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaContractInfoFacadeBean;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NewBusinessSaveContractInfoRequest;

/**
 * Commits any contract information data required to support the Contract Info
 * business function.  It requires a <code>NewBusinessSaveContractInfoRequest</code>
 * request object as input.
 * <p>
 * The process delegates to the pre-existing <code>NbaContractInfoFacadeBean</code>.
 * <p>
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 * @see com.csc.fsg.nba.bean.accessors.NbaContractInfoFacadeBean
 */
public class SaveContractInfoBP extends NewBusinessAccelBP {
    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
		    NewBusinessSaveContractInfoRequest req = (NewBusinessSaveContractInfoRequest)input;
			NbaContractInfoFacadeBean contractInfoBean = new NbaContractInfoFacadeBean();
			contractInfoBean.saveContractInfo(req.getNbaUserVO(), req.getNbaDst(), req.getNbaTXLife(), req.getContractInfoVO());
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}
}
