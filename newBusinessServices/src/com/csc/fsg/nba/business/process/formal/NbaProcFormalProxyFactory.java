package com.csc.fsg.nba.business.process.formal;
/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import com.csc.fsg.nba.accel.process.NbaAutoProcessAccelBP;
import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This class returns a proxy object based upon the given inputs to execute APFORMAL.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * <tr><td>SR494086.5</td><td>Discretionary</td><td>WorkFlow</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcFormalProxyFactory {
	/**
	 * Factory method to return NbaProcFormal proxy
	 * @param vo
	 * @return
	 */
	public static NbaAutomatedProcess getProxy(NbaDst work, NbaUserVO user, NbaProcessStatusProvider statusProvider, NbaAutoProcessAccelBP currentBP) {
		NbaAutomatedProcess proxy = null;
		try {
			if (!NbaUtils.isBlankOrNull(work.getNbaLob().getUnderwriterActionLob()) && work.getNbaLob().getUnderwriterActionLob().equalsIgnoreCase(NbaOliConstants.OLI_INFORMAL_MERGE_ACTION)) {
				proxy = new NbaFormaFromInformalMergeProxy();
				System.out.println("NbaFormaFromInformalMergeProxy");
			}
			else if (NbaProcFormalUtils.isReg60Case(work, user)) {
				proxy = new NbaReg60Proxy();
			}else if (NbaConstants.APPPROD_TYPE_ADC.equalsIgnoreCase(work.getNbaLob().getAppProdType())) { //SR494086.5,SR494086 ADC Retrofit
				proxy = new NbaAdcFormalProxy(); //SR494086.5 ADC Retrofit
			} else if (work.getNbaLob().getAppOriginType() == NbaConstants.FORMAL_APPLICATION) {
				proxy = new NbaFormalProxy();
			} else if (work.getNbaLob().getAppOriginType() == NbaConstants.TRIAL_APPLICATION) {
				proxy = new NbaInformalProxy();
			} else if (work.getNbaLob().getAppOriginType() == NbaConstants.FORMAL_APP_ORIGIN_TRIAL) {
				proxy = new NbaFormalFromInformalProxy();
			}
			//Initialize proxy
			proxy.setWork(work);
			proxy.setUser(user);
			proxy.setStatusProvider(statusProvider);
			proxy.setCurrentBP(currentBP);
		} catch (NbaBaseException ex) {
			return null;
		}

		return proxy;
	}

}
