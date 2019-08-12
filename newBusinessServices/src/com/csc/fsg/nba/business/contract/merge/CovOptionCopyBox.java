package com.csc.fsg.nba.business.contract.merge;
/**
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import com.csc.fsg.nba.vo.NbaContractVO;

/** 
 * 
 * This class extends CopyBox class. This class provides implementation of doSpecificProcessing() method for specific processing
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class CovOptionCopyBox extends CopyBox {
	
	public CovOptionCopyBox() {
		super();
		getSkipAttributes().add("lifeParticipantRefID");
		getSkipAttributes().add("maxBenefitAmt");
		getSkipAttributes().add("covOptionStatus");//P2AXAL029
	}
	
	public void doSpecificProcessing(NbaContractVO copyTo, NbaContractVO copyFrom) {
		
	}

}
