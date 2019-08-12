package com.csc.fsg.nba.process.application;

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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.rule.NbaDeterminePlan;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaOverrideContractUpdateVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Submits the current application for nbA processing. The submitting process
 * requires determining the work item's next status and then commits the work item.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA151</td><td>Version 6</td><td>UL and VUL Application Entry Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class SubmitApplicationBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result;
		try {
			result = submit((NbaOverrideContractUpdateVO) input);//NBA139
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * Submits the current application for nbA processing. The submitting process
	 * requires determining the work item's next status and then updating the work item.
	 * @param contractUpdate application information
	 * @return
	 * @throws Exception
	 */
	protected AccelResult submit(NbaOverrideContractUpdateVO contractUpdate) throws Exception {
		NbaDst work = contractUpdate.getNbaDst();
		NbaUserVO user = contractUpdate.getNbaUserVO();
		boolean isUpdateMode = contractUpdate.isUpdateMode();//NBA139
		work.setNbaUserVO(user);
		
		setStatus(work);
		//NBA213 code deleted
		if (isUpdateMode) {//NBA139
            determinePlanCode(work.getXML103Source(), work, user);//NBA139
        }//NBA139
        Result result = callService("NbaUpdateWorkBP", work);  //NBA213
        return AccelResult.buildResult(result);
	}

	/**
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next
	 * status and priority.
	 * @param work
	 * @throws NbaBaseException
	 */
	protected void setStatus(NbaDst work) throws NbaBaseException {
        NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(NbaConstants.PROC_APPLICATION_ENTRY, ""), work, work.getXML103Source());//ALPC168
        work.setStatus(provider.getPassStatus());
        work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
	}
	
	//NBA213 code deleted
	
	/**
     * This method is called after clicking the Commit button from AppEntry view in Application Update mode.
     * @param nbaTXLife NbaTXLifeobject to be processed
     * @param dst NbaDst Object to be processed
     * @param user NbaUserVO object to be processed
     * @throws NbaBaseException
     */
	//New method NBA139
	protected NbaTXLife determinePlanCode(NbaTXLife nbaTXLife, NbaDst dst, NbaUserVO user) throws NbaBaseException {
        NbaLob aNbaLob = dst.getNbaLob();
        try {
            if (NbaConfiguration.getInstance().isGenericPlanImplementation()) {
                if ((aNbaLob.getContractChgType() == null) && (aNbaLob.getAppOriginType() != 0)) {
                    NbaDeterminePlan determinePlan = new NbaDeterminePlan();
                    nbaTXLife = determinePlan.determinePlanCode(dst, nbaTXLife);
                }
            }
            return nbaTXLife;
        } catch (NbaVpmsException e) {
            e.forceFatalExceptionType();
            throw e;
        } catch (NbaBaseException e) {
            e.forceFatalExceptionType();
            throw e;
        }
    }
}
