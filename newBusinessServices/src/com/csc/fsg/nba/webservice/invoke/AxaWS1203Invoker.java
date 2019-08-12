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
import java.util.Date;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Policy;

/**
 * This class is responsible for creating generic 1203 request.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.27</td><td>AXA Life Phase 1</td><td>RTS Interface</td></tr>
<tr><td>ALS4507</td><td>AxaLife Phase 1</td><td>QC ## 3505 - 1203 does not contain TransExeTime, TransExeDate & LineOfBusiness </td></tr>  
 * <tr><td>ALS4568</td><td>AXA Life Phase 1</td><td>QC # 3622 - 3.7.25 - transmitClientHolding fails with java.lang.OutOfMemoryError</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWS1203Invoker extends AxaWSInvokerBase{

    /**
     * constructor from superclass
     * @param userVO
     * @param nbaTXLife
     */
    public AxaWS1203Invoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation, userVO, nbaTXLife, nbaDst, object);
    }

    /**
     * This method is used for creating generic 1203 request
     * @return nbaReqTXLife
     */
    public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(getUserVO());
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		nbaReqTXLife.setOLifE(getNbaTXLife().getOLifE().clone(false));
		// ALS4507 begin
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeDate(new Date());
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeTime(new NbaTime());
		Policy policy = nbaReqTXLife.getPolicy();
		if (policy != null) {
			policy.setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_LIFE); // ALS4507
		}
		// ALS4507 end
		return nbaReqTXLife;
    }
    
    /**
     * This method is responsible for cleaning up the generic 1203 request created.
     * @return void
     */
	public void cleanRequest() throws NbaBaseException {// APSL371 APSL372
		super.cleanRequest();
		getNbaTxLifeRequest().getPrimaryHolding().setAttachment(new ArrayList());// AXAL3.7.14
		// begin ALS4568
		int reqCount = getNbaTxLifeRequest().getPolicy().getRequirementInfoCount();
		List statusTypeAttachment = new ArrayList();
		com.csc.fsg.nba.vo.txlife.Attachment attachment = null; //NBLXA-1656
		for (int x = 0; x < reqCount; x++) {
			//NBLXA-1656 Starts
			if (getNbaTxLifeRequest().getPolicy().getRequirementInfoAt(x).getReqCode() == NbaOliConstants.OLI_REQCODE_PHYSSTMT) {
				int attachmentCount = getNbaTxLifeRequest().getPolicy().getRequirementInfoAt(x).getAttachmentCount();
				for (int attachCount = 0; attachCount < attachmentCount; attachCount++) {
					attachment = getNbaTxLifeRequest().getPolicy().getRequirementInfoAt(x).getAttachmentAt(attachCount);
					if ((NbaOliConstants.OLI_ATTACH_STATUSCHG) != attachment.getAttachmentType()) {
						getLogger().logDebug("The Attachments removed from the RequirementInfo Object has type " + attachment.getAttachmentType());
						statusTypeAttachment.add(attachment);
						
					}
				}
				getNbaTxLifeRequest().getPolicy().getRequirementInfoAt(x).getAttachment().removeAll(statusTypeAttachment);
				getNbaTxLifeRequest().getPolicy().getRequirementInfoAt(x).setActionUpdate();
			} else {
				//NBLXA-1656 Ends
				getNbaTxLifeRequest().getPolicy().getRequirementInfoAt(x).setAttachment(new ArrayList());
			}
		} //NBLXA-1656
		// end ALS4568
		// ALS4287,ALS4367 code deleted
	}
}
