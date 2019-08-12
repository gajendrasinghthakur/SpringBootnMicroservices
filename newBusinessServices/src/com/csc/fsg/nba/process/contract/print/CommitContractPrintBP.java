package com.csc.fsg.nba.process.contract.print;

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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.bean.accessors.NbaContractPrintFacadeBean;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Generates specified contract print documents to support the Contract Print business
 * function.  The required input for this process is a <code>List</code> of the
 * following data:
 * <p>
 * <ul>
 * <li>index 0 - <code>NbaUserVO</code></li>
 * <li>index 1 - <code>NbaDst</code></li>
 * <li>index 2 - <code>String[]</code> of document codes for the docs to be printed in order</li>
 * </ul>
 * <p>
 * The process delegates to the pre-existing <code>NbaContractPrintFacadeBean</code>.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> 
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA183</td><td>Version 7</td><td>Contract Print Rewrite</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 * @see com.csc.fsg.nba.bean.accessors.NbaContractPrintFacadeBean
 */

public class CommitContractPrintBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			List paramList = (List) input;
			//NBLXA-2216 BEGIN
			String selectAction = (String) paramList.get(5);
			if (null != selectAction) {
				if (selectAction.equalsIgnoreCase(NbaConstants.SELECT_ACTION_PRINTCOMMIT)) {
					//NBLXA-2216 ENDS
					NbaContractPrintFacadeBean facade = new NbaContractPrintFacadeBean();
					// APSL4416 Begin
					String[] doc = (String[]) paramList.get(2);
					if (doc != null && doc.length > 0) {
						NbaDst updatedNbaDst = facade.generateContractExtract((NbaUserVO) paramList.get(0), (NbaDst) paramList.get(1),
								getCommaSeparatedString((String[]) paramList.get(2)), true, null); // SR787006-APSL3702,APSL5111
						result.addResult(updatedNbaDst);
					}
					NbaTXLife txlife = (NbaTXLife) paramList.get(3);
					if (!(txlife.isTermLife() || NbaUtils.isISWLProduct(txlife.getPolicy()) || NbaUtils.isAdcApplication((NbaDst) paramList.get(1)))) {
						NbaContractAccess.doContractUpdate(txlife, (NbaDst) paramList.get(1), (NbaUserVO) paramList.get(0));
					}
					// APSL4416 End
				//NBLXA-2216 BEGIN
				} else if (selectAction.equalsIgnoreCase(NbaConstants.SELECT_ACTION_DELIVERMETHOD)) {
					NbaTXLife txlife = (NbaTXLife) paramList.get(6);
					NbaContractAccess.doContractUpdate(txlife, (NbaDst) paramList.get(1), (NbaUserVO) paramList.get(0));
				}
				//NBLXA-2216 ENDS
			}			

		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

    /**
     * This method takes the input String array and creates a string with the values of the array elements separated by ','
     * @param selectedDocuments
     */
    private String getCommaSeparatedString(String[] selectedDocuments) {
        StringBuffer buffer = new StringBuffer();
        if (selectedDocuments != null) {
            int codes = selectedDocuments.length;
            for (int i = 0; i < codes; i++) {
                buffer.append(selectedDocuments[i]);
                if (i != codes - 1) {
                    buffer.append(NbaConstants.DELIMITER_COMMA_STRING);
                }
            }
        }
        return buffer.toString();
    }
}