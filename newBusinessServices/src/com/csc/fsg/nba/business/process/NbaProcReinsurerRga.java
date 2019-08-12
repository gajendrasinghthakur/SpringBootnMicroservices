package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaEntityResolver;
import com.csc.fsg.nba.reinsurance.rgaschema.Case;
import com.csc.fsg.nba.reinsurance.rgaschema.Cases;
import com.csc.fsg.nba.reinsurance.rgaschema.Document;
import com.csc.fsg.nba.reinsurance.rgaschema.Documents;
import com.csc.fsg.nba.vo.NbaSource;
import com.tbf.xml.XmlElement;
import com.tbf.xml.XmlParser;

/**
 * <code>NbaProcReinsurerRga</code> handles communications between nbAccelerator
 * and RGA.  It extends the NbaProcReinsurerCommunications class, which drives the process,
 * and supplies RGA specific functionality.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>SPR3303</td><td>Version 7</td><td>Images Excluded from RGA XML Request for Facultative Reinsurance</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * 
 * 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProcReinsurerRga extends NbaProcReinsurerCommunications {
	/**
	 * NbaProcReinsurerRga default constructor.
	 */
	public NbaProcReinsurerRga() {
		super();
	}
	/**
	 * For RGA processing,  this will sets the URL (path) from the NbaConfiguration file.
	 */
	public void initializeTarget() throws NbaBaseException {
		setTarget(getConfigRien().getUrl());
	}
	//AXAL3.7.32 Code Deleted and moved to its super class 'NbaProcReinsurerCommunications'
}
