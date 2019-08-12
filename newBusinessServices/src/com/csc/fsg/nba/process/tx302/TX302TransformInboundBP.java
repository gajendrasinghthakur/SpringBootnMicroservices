package com.csc.fsg.nba.process.tx302;

/*
 * *******************************************************************************<BR>
 * Copyright 2014, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.valueobject.WebServiceCall;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead> <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th> </thead>
 * <tr>
 * <tr><td>APSL4508 Websphere 8.5.5 Upgrade</td></tr>
 * </table>
 * <p>
 */

public class TX302TransformInboundBP extends AccelBP {

    /**
     * This method supports validation and Transform.
     * 
     * @param input requires a TXLife request object
     * @return the TXLife response is returned in the Result.
     */
	public Result process(Object request) {
		Result output = Result.Factory.create();
		WebServiceCall webcall = (WebServiceCall) request;
		String txlife = webcall.getPayload();
		StringBuffer tXLifeStr = null;
		tXLifeStr = new StringBuffer(txlife);
		tXLifeStr.delete(0, tXLifeStr.indexOf("<TXLife"));
		tXLifeStr.delete(tXLifeStr.toString().indexOf("</TXLife>") + 9, tXLifeStr.length());
		String strTXLife = tXLifeStr.toString();
		strTXLife = strTXLife.replaceAll("\t", "").trim();
		strTXLife = setTxLifeNamespace(strTXLife);
		NbaTXLife nbaTxlife = null;
		try {
			nbaTxlife = new NbaTXLife(strTXLife);
		} catch (Exception e) {
			NbaLogFactory.getLogger(this.getClass()).logException(e);
		}
		output.addResult(nbaTxlife);
		return output;
	}

	protected String setTxLifeNamespace(String txLifeStr) {
		StringBuffer txLifeStrBuf = new StringBuffer(txLifeStr);
		if (txLifeStr.indexOf("<TXLife") > -1) {
			int start = txLifeStr.indexOf("<TXLife");
			int end = txLifeStr.indexOf(">", start) + 1;
			txLifeStrBuf.replace(start, end, "<TXLife xmlns=\"http://ACORD.org/Standards/Life/2\">");
		}
		return txLifeStrBuf.toString();
	}
}