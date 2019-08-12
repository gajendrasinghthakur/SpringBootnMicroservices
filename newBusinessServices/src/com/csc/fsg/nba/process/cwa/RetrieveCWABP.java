package com.csc.fsg.nba.process.cwa;

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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaCWAReverseRefundFacadeBean;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * Class Description. This class is serves as an entry point to the service Layer, It uses the
 * EJB Facade and it's exposed methods to Retrieve the CWA Data.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA169</td><td>Version 6</td><td>CWA Rewrite</td></tr> 
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveCWABP extends NewBusinessAccelBP {

    /**
     * This method uses  <code>NbaCWAReverseRefundFacade</code> EJB to retrieve CWA data. 
     *@param Instance of <code>NbaTXLife</code>
     *@return The <code>AccelResult</code> object containing the list of retrieved <code>NbaCWAReverseRefundVO</code>  
     *@see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */

    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input instanceof NbaTXLife) {
                NbaTXLife aNbaTXLife = (NbaTXLife) input;
                NbaCWAReverseRefundFacadeBean facade = new NbaCWAReverseRefundFacadeBean();  //NBA213
                List cwas = facade.loadCWAInfo(aNbaTXLife);
                result.addResults(cwas);
            }

        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }
	//NBA213 deleted code
}
