/* 
 * *******************************************************************************<BR>
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
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.correspondence;

/**
 * This is the interface for all the correspondence processors constants. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.13</td><td>Version 7</td><td>Formal Correspondence</td></tr>
 * <tr><td>ALPC195</td><td>AXA Life Phase 1</td><td>AUD Negative Correspondence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public interface AXACorrespondenceConstants {
	
	public static String REPLACEMENT_LETTERS [] = { "Presale_External_Company_Coverletter",
													"NY_REG60_Initial_Notice_Letter",
													"NY_PreSale_Request_Values_20Day",
													"App_Review_Initial_Notice_letter",
													"BGA_NY_AIG_Auth_Good_Faith",
													"BGA_NY_AIG_Auth",
													"BGA-NY_Pre_Sale_Letter_No_Reply",
													"BGA_NY_PreSale_Request_Values_20Day",
													"BGA_NY_PreSale_Auth_To_Take_Application",
													"NY_PreSale_Auth_To_Take_Application"
												};
	//ALPC195
	public static String AUD_LETTERS[] = { "AUD-AA","AUD-AB","AUD-AC","AUD-AD","AUD-AE1",
										   "AUD-AE2","AUD-AE3","AUD-AE4","AUD-AE5","AUD-AE6",
										   "AUD-AE6O","AUD-AE7","AUD-AE8","AUD-AE9","AUD-AE10",
										   "AUD-AE11","AUD-AG","AUD-AH","AUD-AK","AUD-AF",
										   "AUD-AOC","AUD-AE6I"
		};
	
	public static final String PARENT_DST = "PARENTDST";  //ALS4476
	public static final String BP_RETRIEVE_COMMENTS = "RetrieveCommentsBP";  //ALS4476  
	public static final String SPECIAL_INSTRUCTION_DELIMITER = "##";  //ALS4476
	
	
}
