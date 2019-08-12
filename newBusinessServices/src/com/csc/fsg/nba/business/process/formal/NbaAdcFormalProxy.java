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

import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSearchResultVO;

/**
 * This class executes APFORMAL for a case whose Application Origin (APTP) is Formal Originating From Informal.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>SR494086.5</td><td>Discretionary</td><td>WorkFlow</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaAdcFormalProxy extends NbaFormalFromInformalProxy {
	
	
	protected void processDuplicateWork(List duplicateWorks) throws NbaBaseException {
		if (duplicateWorks.size() >= 1) {
			NbaProcessingErrorComment npec = createComment();
			npec.setText("Duplicate matches found for the case.");
			getWork().addManualComment(npec.convertToManualComment());
		}
		//Start : LIM-ADC APSL3460
		if (duplicateWorks.size() == 1) {
		    NbaLob dupCaseLob = ((NbaSearchResultVO) duplicateWorks.get(0)).getNbaLob();
		    NbaLob currCaseLob = getWork().getNbaLob();
		    if (dupCaseLob != null && dupCaseLob.getApplicationNumber()!= null && dupCaseLob.getApplicationNumber().length()>0){
		        currCaseLob.setApplicationNumber(dupCaseLob.getApplicationNumber());
		    }
		    if (dupCaseLob != null && dupCaseLob.getWorkFlowCaseId()!= null && dupCaseLob.getWorkFlowCaseId().length()>0){
                currCaseLob.setWorkFlowCaseId(dupCaseLob.getWorkFlowCaseId());
            }
		    currCaseLob.setActionUpdate();
		    //End : LIM-ADC APSL3460
		}
		setResult(getDuplicateWorkResult()); 
	}

	public void doProcess() throws NbaBaseException {
		
	}

}
