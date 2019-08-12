package com.csc.fsg.nba.reinsuranceadapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.reinsurance.rgaschema.Applicant;
import com.csc.fsg.nba.reinsurance.rgaschema.Applicants;
import com.csc.fsg.nba.reinsurance.rgaschema.Benefit;
import com.csc.fsg.nba.reinsurance.rgaschema.Benefits;
import com.csc.fsg.nba.reinsurance.rgaschema.Case;
import com.csc.fsg.nba.reinsurance.rgaschema.Cases;
import com.csc.fsg.nba.reinsurance.rgaschema.CedingAdminContact;
import com.csc.fsg.nba.reinsurance.rgaschema.CedingCompany;
import com.csc.fsg.nba.reinsurance.rgaschema.CedingUWContact;
import com.csc.fsg.nba.reinsurance.rgaschema.Document;
import com.csc.fsg.nba.reinsurance.rgaschema.Documents;
import com.csc.fsg.nba.reinsurance.rgaschema.NbaRgaRequest;
import com.csc.fsg.nba.reinsurance.rgaschema.Reinsurer;
import com.csc.fsg.nba.reinsurance.rgaschema.Request;
import com.csc.fsg.nba.reinsurance.rgaschema.Requests;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Contact;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;

/**
 * NbaRGAadapter provides support for converting 552 XML request into the transactions required by RGA.
 * In addition, it parses the offer received  from RGA, updates AWD work items for those results and 
 * adds any additional sources that might have been received for this work item.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1346</td><td>Version 5</td><td>Displaying State Drop-down list in Alphabetical order by country/ACORD State Code Change</td></tr>
 * <tr><td>SPR2821</td><td>Version 5</td><td>When the additional information is created for a request which is not yet responded, the NBTEMPREN work item stops the APORDREN process</td></tr>
 * <tr><td>SPR3303</td><td>Version 7</td><td>Images Excluded from RGA XML Request for Facultative Reinsurance</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.32</td><td>Axa Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaRgaAdapter extends AxaReinsuranceAdapter {

	/**
	 * NbaRgaAdapter default constructor.
	 */
	public NbaRgaAdapter() {
		super();		
	}
	//AXAL3.7.32 Code is moved to its super class (AxaReinsuranceAdapter)
}
