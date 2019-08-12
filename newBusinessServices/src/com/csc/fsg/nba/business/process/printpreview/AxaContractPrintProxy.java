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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
package com.csc.fsg.nba.business.process.printpreview;

import com.csc.fsg.nba.accel.process.NbaAutoProcessAccelBP;
import com.csc.fsg.nba.business.process.NbaProcContractPrint;
import com.csc.fsg.nba.contract.extracts.AxaLifeContractPrintExtractFormater;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * This is a proxy class that generates Print Preview
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>APSL4419</td><td>Discretionary</td><td>Print NIGO</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 */
public class AxaContractPrintProxy extends NbaProcContractPrint {
	/**
	 * Constructor
	 * 
	 * @param work
	 * @param user
	 * @param txLife
	 */
	public AxaContractPrintProxy(NbaDst work, NbaUserVO user, NbaTXLife txLife) {
		setUser(user);
		setNbaTxLife(txLife);
		setWork(work);		
	}
	
	public byte[] generatePrintPreview() throws NbaBaseException {		
		return invokePrintPreviewService(createRequest());		
	}	
	
	protected NbaTXLife createRequest() throws NbaBaseException {
		String extComp2 = getWork().getNbaLob().getPrintExtract();	//APSL5055
		reprintInd = (extComp2 != null && extComp2.trim().length() > 0);
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		if (policyExtension != null && reprintInd) {
			policyExtension.setContractChangeReprintInd(reprintInd);			
		}
		setUnboundIndicator();
		performPrePrintValidation();
		NbaSource nbaSource = new AxaLifeContractPrintExtractFormater().generateContractPrintSource(getUser(), getWork(), getNbaTxLife());
		NbaTXLife nbaTXLife = null;
		try {
			nbaTXLife = new NbaTXLife(nbaSource.getText());			
			NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setContractPrintExtractDate(
					NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getContractPrintExtractDate());
			nbaTXLife.getPolicy().setEffDate(getNbaTxLife().getPolicy().getEffDate()); 
			NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setIllustrationInd(
					NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getIllustrationInd());  
			NbaUtils.getFirstApplicationInfoExtension(nbaTXLife.getPolicy().getApplicationInfo()).setRevisedIllustrationInd(
					NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo()).getRevisedIllustrationInd());  
				
			boolean unboundPrintInd = false;
			Double cwaTotal = getNbaTxLife().getNbaHolding().getCwaTotal();
			if (cwaTotal.doubleValue() <= 0 && !reprintInd) {
				unboundPrintInd = true;
			}
			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
			if (policyExtn != null) {
				policyExtn.setUnboundInd(unboundPrintInd);
			}
		} catch (Exception exp) {
			NbaBaseException nbe = new NbaBaseException(exp);
			NbaLogFactory.getLogger(this.getClass()).logException(nbe);
			throw nbe;
		}
		return nbaTXLife;
	}
	
	protected byte[] invokePrintPreviewService(NbaTXLife nbaTXLife) throws NbaBaseException {
		nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransSubType(NbaOliConstants.TC_SUBTYPE_PRINTPREVIEW);
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CONTRACT_PRINT, getUser(), nbaTXLife,
				null, null);
		NbaTXLife txLife = (NbaTXLife) webServiceInvoker.execute();
		// parse the policy print preview response 
		String previewData = null;
		if (txLife.getPrimaryHolding() != null && txLife.getPrimaryHolding().getAttachmentAt(0) != null
				&& txLife.getPrimaryHolding().getAttachmentAt(0).getAttachmentData() != null) {
			previewData = txLife.getPrimaryHolding().getAttachmentAt(0).getAttachmentData().getPCDATA();
		} else {
			throw new NbaBaseException(AxaWSConstants.WS_OP_CONTRACT_PRINT + " WebService for print preview returned invalid response.");
		}
		return NbaBase64.decode(previewData);
	}
	
}