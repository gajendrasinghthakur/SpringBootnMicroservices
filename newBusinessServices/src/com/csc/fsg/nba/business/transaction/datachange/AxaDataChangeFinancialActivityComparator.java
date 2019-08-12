
package com.csc.fsg.nba.business.transaction.datachange;
/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import java.util.List;

import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProducerVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;

/**
 * 
 * Helper classes to determine Data change 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangeFinancialActivityComparator extends AxaDataChangeComparator{
	protected FinancialActivity oldFinancialActivity;
	protected FinancialActivity newFinancialActivity;
	protected FinancialActivityExtension newFinancialActivityExtension;
	protected FinancialActivityExtension oldFinancialActivityExtension;
	
	/**
	 * @param oldFinancialActivity
	 * @param newFinancialActivity
	 */
	public AxaDataChangeFinancialActivityComparator(FinancialActivity newFinancialActivity,FinancialActivity oldFinancialActivity) {
		super();
		this.oldFinancialActivity = oldFinancialActivity;
		this.newFinancialActivity = newFinancialActivity;
		oldFinancialActivityExtension= NbaUtils.getFirstFinancialActivityExtension(oldFinancialActivity);
		newFinancialActivityExtension= NbaUtils.getFirstFinancialActivityExtension(newFinancialActivity);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewFinancialActivityExtension() {
		return (newFinancialActivityExtension != null && newFinancialActivityExtension.isActionAdd() && oldFinancialActivityExtension == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewFinancialActivity() {
		return (newFinancialActivity != null && newFinancialActivity.isActionAdd() && oldFinancialActivity == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isFinancialActivityReversed(){
		return isNewFinancialActivity() && NbaOliConstants.OLI_FINACTSUB_REV == newFinancialActivity.getFinActivitySubType();
	}
}
	
	
	
	

