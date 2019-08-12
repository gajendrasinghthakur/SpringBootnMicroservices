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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SourceInfo;

/**
 * This class is responsible for creating request for Compensation ECS webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>SR494086.6</td><td>Discretionary</td><td>Interfaces</td></tr>
 * <tr><td>CR57907</td><td>Discretionary</td><td>Xpress Commission Transaction</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSECSSubmitInvoker extends AxaWS1203Invoker{
	
	private static final String CATEGORY = "COMPENSATIONRETRIEVE";

	private static final String FUNCTIONID = "SubmitExpressCompensation";
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSECSSubmitInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
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
		nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransSubType(
				NbaOliConstants.TC_SUBTYPE_HOLDING_TRANSMITTAL);
		if (getObject() != null) {
			SourceInfo sourceInfo = nbaTXLife.getOLifE().getSourceInfo();
            if (sourceInfo != null) {
                sourceInfo.setSourceInfoComment((String) getObject());
            }
        }
		return nbaTXLife;
	}
	//AXAL3.7.22 overridden the method to stop compensation interface call for informal Application.
	public boolean isCallNeeded() {
		//Begin CR57907 Retrofit
		boolean ecsSupress = false;
		NbaUserVO user = getUserVO();
		String buisnessProcess=getNbaTXLife().getBusinessProcess();
		if (null != buisnessProcess
				&& (buisnessProcess.equalsIgnoreCase(NbaConstants.PROC_UW_APPROVE_CONTRACT)
						|| buisnessProcess.equalsIgnoreCase(NbaConstants.PROC_UW_DISPOSITION) || NbaConstants.PROC_AUTO_UNDERWRITING
						.equalsIgnoreCase(buisnessProcess))) {//QC#8323 APSL1900
			ecsSupress = true;
		}
		//End CR57907
		if (getNbaTXLife().isInformalApplication()
				|| getNbaTXLife().isPaidReIssue()
				|| NbaUtils.isAdcApplication(getNbaDst())
				|| ecsSupress
				|| (NbaConstants.SYST_LIFE70.equalsIgnoreCase(getNbaTXLife().getBackendSystem()) 
						&& !NbaUtils.isMsgCodeExists(getNbaTXLife(),NbaConstants.MESSAGECODE_CM36_CBL))) {// ALS4493, APSL459, APSL5218
			return false;
		}
		return true;
	}

}
