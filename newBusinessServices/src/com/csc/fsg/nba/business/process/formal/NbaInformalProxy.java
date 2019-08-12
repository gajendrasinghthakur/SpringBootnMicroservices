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

import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaAutomatedProcessResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * This class executes APFORMAL for a case whose Application Origin (APTP) is Informal.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * <tr><td>QC1300</td><td>AXA Life Phase 1</td><td>Work itme created for NBCM, but do detail on what needs to be done</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaInformalProxy extends NbaFormalFromInformalProxy {

	public NbaAutomatedProcessResult getDuplicateWorkResult() {
		return (new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));
	}
	
	//ALS4005 New Method
	protected void processDuplicateWork(List duplicateWorks) throws NbaBaseException {
		if (duplicateWorks.size() == 1) {
			if (!getWorkLobs().getFaxedOrEmailedInd() && ((NbaSearchResultVO) duplicateWorks.get(0)).getNbaLob().getFaxedOrEmailedInd()) { //Begin ALS4854 ALS4891	
				//Check if UW decision has not taken upon for the matching app yet, merge current application with it.
				String businessProcess = NbaUtils.getBusinessProcessId(getUser());
				NbaSearchResultVO searchResult = (NbaSearchResultVO) duplicateWorks.get(0);
				NbaTXLife matchingContract = NbaContractAccess.doContractInquiry(createRequestObject(searchResult, businessProcess));
				NbaHolding holding = matchingContract.getNbaHolding();
				if (holding != null
						&& (!(holding.getInformalAppApproval() == NbaOliConstants.OLIX_INFORMALAPPROVAL_OFFERACCEPTED || holding
								.getInformalAppApproval() == NbaOliConstants.OLIX_INFORMALAPPROVAL_OFFERPENDING))) {
					//Begin ALS4664
					convertFaxedToPaper((NbaSearchResultVO) duplicateWorks.get(0));
					NbaProcessingErrorComment npec = createComment();
					npec.setText("Paper application received - needs review by NBCM.");
					getWork().addManualComment(npec.convertToManualComment());
					//End ALS4664
				} else {
					NbaProcessingErrorComment npec = createComment();
					npec.setText("Did not match as underwriting decision has already occurred.");
					getWork().addManualComment(npec.convertToManualComment());
					//ALS4664 Code deleted
				}
			//Begin ALS4854 ALS4891	
			} else {
				NbaProcessingErrorComment npec = createComment();
				npec.setText("Single match found- case not merged. Needs review.");
				getWork().addManualComment(npec.convertToManualComment());
			}
			//End ALS4854 ALS4891
		} else if (duplicateWorks.size() > 1) {
			//Begin QC1300
			NbaProcessingErrorComment npec = createComment();
			npec.setText("Multiple matches found for the case.");
			getWork().addManualComment(npec.convertToManualComment());
			//End QC1300
			//ALS4664 Code deleted
		}
		setResult(getDuplicateWorkResult()); //ALS4664 Code deleted
	}

	public void doProcess() throws NbaBaseException {
		//Begin QC1300
    	NbaProcessingErrorComment npec = createComment();
		npec.setText("No matches found for the case.");
    	getWork().addManualComment(npec.convertToManualComment());
    	//End QC1300
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getFailStatus()));//APSL4226
	}
	
	//ALS4854 ALS4891 Code deleted
}
