package com.csc.fsg.nba.process.cashiering;

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
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaCashBundleVO;
import com.csc.fsg.nba.vo.NbaLockBundleVO;

/**
 * Locking the Bundle
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class LockBundlesBP extends NewBusinessAccelBP {

       /**
     * Called to Lock the selected bundles.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */

    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input != null) {
                List selectedBundle = (List) input;
                result.addResult(lockBundles(selectedBundle));
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * The method locks the selected Bundles.
     * @param bundleList list of bundles selected from cashiering workbench
     * @throws NbaBaseException
     */
    protected List lockBundles(List selectedBundle) throws NbaBaseException {
		NbaLockBundleVO bundleVO = null;
		NbaCashieringTable cashTable = new NbaCashieringTable();
		List lockedBundles = new ArrayList();
		boolean isLocked = false;

		try {
			for (int i = 0; i < selectedBundle.size(); i++) {
				bundleVO = (NbaLockBundleVO) selectedBundle.get(i);
				isLocked = cashTable.isLocked(bundleVO);
				if (!isLocked) {
					//Lock the Bundle by current user if not already locked
					if (!bundleVO.isLockedForUser()) {
						cashTable.lockBundle(bundleVO);
					}
				} else {
					bundleVO.setEligibleForLock(false);
				}
				lockedBundles.add(bundleVO);
			}
		} catch (NbaBaseException nbe) {
			throw nbe;
		}
		return lockedBundles;
	}
 }
