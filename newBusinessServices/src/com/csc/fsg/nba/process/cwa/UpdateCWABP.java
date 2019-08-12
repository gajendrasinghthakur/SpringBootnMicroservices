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

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.LockRetrieveWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.bean.accessors.NbaCWAReverseRefundFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Class Description. This class is serves as an entry point to the service Layer, It uses the
 * EJB Facade and it's exposed methods to Save the CWA Data.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA169</td><td>Version 6</td><td>CWA Rewrite</td></tr> 
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item retrieval</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class UpdateCWABP extends NewBusinessAccelBP {

    /**
     * This method uses  <code>NbaCWAReverseRefundFacade</code> EJB to save the input CWA data. 
     *@param An ArrayList containing <code>NbaUserVO</code>,<code>NbaDst</code>,<code>NbaTXLife</code>,
     *<code>ArrayList</code> in order. 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input instanceof List) {
                List inputList = (List) input;
                NbaDst work = (NbaDst) inputList.get(1); //NBA208-36
                work = getTransactions(work);  //NBA208-36
                NbaCWAReverseRefundFacadeBean facade = new NbaCWAReverseRefundFacadeBean();  //NBA213
                facade.saveCWAInfo((NbaUserVO) inputList.get(0), work, (NbaTXLife) inputList.get(2), (ArrayList) inputList.get(3)); //NBA208-36
                result.addResult(work); //NBA213
            }

        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	//NBA213 deleted code
    /**
	 * Retrieves a list of cwa transactions and
	 * appends them to the current case.
	 * @param work
	 * @param workLob
	 * @param queue
	 * @return
	 */
	//NBA208-36 New Method
	protected NbaDst getTransactions(NbaDst work) throws NbaBaseException {
		LockRetrieveWorkRequest request = new LockRetrieveWorkRequest();
		NbaLob workLob = work.getNbaLob();
		//setup the search lobs
		NbaLob tempLob = new NbaLob();
		tempLob.setCompany(workLob.getCompany());
		tempLob.setPolicyNumber(workLob.getPolicyNumber());

		request.setBusinessArea(workLob.getBusinessArea());
		request.setWorkType(NbaConstants.A_WT_CWA);
		request.setPageNumber("1"); // APSL5055-NBA331
		request.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
		request.setRetrieveWorkLocked(false);
		request.setRetrieveImages(true);
		request.setRetrieveSupportingSources(true);
		request.setWorkItem(work.getCase());

		AccelResult result = (AccelResult) callService("LockRetrieveWorkBP", request);
		if (!result.hasErrors()) {
			work.addCase((WorkItem) result.getFirst());
		}
		return work;
	}
}
