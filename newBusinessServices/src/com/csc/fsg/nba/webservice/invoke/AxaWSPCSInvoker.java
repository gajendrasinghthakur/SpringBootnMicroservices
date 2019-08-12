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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;

/**
 * This class is responsible for creating request for Compensation PCS webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>ALPC255</td><td>AXA Life Phase 1</td><td>Compensation Interface Substandard Premium</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSPCSInvoker extends AxaWS1203Invoker {
	private static final String CATEGORY = "COMPENSATIONRETRIEVE";

	private static final String FUNCTIONID = "SubmitProductCompensation";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSPCSInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
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
		return nbaTXLife;
	}
	//AXAL3.7.22 overridden the method to stop compensation interface call for informal Application.
	public boolean isCallNeeded() {
		if (!getNbaTXLife().isInformalApplication()) {
			return true;
		}
		return false;
	}
	/**
     * This method is responsible for cleaning up the generic 1203 request created.
     * @return void
     */
	//ALPC255
    public void cleanRequest() throws NbaBaseException { //APSL371 APSL372
    	ArrayList attachList = getNbaTxLifeRequest().getPrimaryHolding().getAttachment();
		super.cleanRequest();
		Attachment subAttachment = getSubStandardAttachment(attachList);
		if (null != subAttachment) {
			getNbaTxLifeRequest().getPrimaryHolding().addAttachment(subAttachment);
		}
		NbaUtils.translateBESValueForSuffix(getNbaTxLifeRequest());//APSL371 APSL372
	}
    /*
     * return the substandard premium attachment
     */
    //ALPC255
    private Attachment getSubStandardAttachment(ArrayList attachmentList) {
    	Attachment attachment;
    	int attachmentListSize = attachmentList.size();
		if (attachmentList != null && attachmentListSize > 0) {
			for (int k = 0; k < attachmentListSize; k++) {
				attachment = (Attachment) attachmentList.get(k);
				if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_SUBSTANDARD_PREM) {
					return attachment;
					}
				}
		}
		return null;
    }
}
