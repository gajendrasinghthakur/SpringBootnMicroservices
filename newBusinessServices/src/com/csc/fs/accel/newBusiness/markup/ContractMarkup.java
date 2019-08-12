package com.csc.fs.accel.newBusiness.markup;

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

import com.csc.fsg.nba.vo.NbaTXLife;


/**
 * Interface class for performing data markup on the contract data <code>NbaTXLife</code>
 * prior to sending the information outside of nbA.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>Holding Inquiry (XML203) Service AttachmentData Object Needs Character Data Markup</td></tr>
 *  <tr><td>APSL4508</td>Websphere 8.5.5 Upgrade</tr>
 * </table>
 * <p>
 */

public interface ContractMarkup {

    public final static String NBA_HOLDING_INQUIRY_MARKUP  = "newBusiness/comp/markup/holdingInquiry";
   
    /**
	 * Markup nbA's internal contract data. 
	 * @param nbatxlife
	 * @return
	 */
	public NbaTXLife markup(NbaTXLife nbatxlife);
}
