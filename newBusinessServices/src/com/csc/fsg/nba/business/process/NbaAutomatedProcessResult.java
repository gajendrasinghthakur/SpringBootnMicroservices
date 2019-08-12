package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */

/**
 * This class represents the results of an automated process.
 * When the process completes sucessfully, the return code is set to 
 * SUCCESSFUL. When the process encounters an error during processing, 
 * the return code is set to FAILED.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>JavaDoc, comments and minor source code changes.</td></tr>
 * <tr><td>NBA188</td><td>Version 7</td><td>nbA XML Sources to Auxiliary</td></tr>
 * <tr><td>ALS3400</td><td>AXA Life Phase 1</td><td>Cashiering Reports Firewall Issue</td></tr>
 * 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaAutomatedProcessResult implements java.io.Serializable {
	public int returnCode;
	public static final int SUCCESSFUL = 0;
	public static final int FAILED = 1;
	public static final int NOWORK = 2;	//NBA188
	public static final int RETRY = 4; //NBA188
	public static final int SUCCESS_NOWORK = 5; //ALS3400
	public java.lang.String text;
	public java.lang.String status;
	private final static long serialVersionUID = 6735656927753560845L;
	protected int countSuccessful = 1; //Number of items successfully processed    NBA308
/**
 * The constructor initializes the class members.
 * @param aReturnCode the result/return code from the Automated Process
 * @param aText       a description of the result
 * @param aStatus     the new status to which the work object was moved
 */
public NbaAutomatedProcessResult(int aReturnCode, String aText, String aStatus) {
	setReturnCode(aReturnCode);
	setText(aText);
	setStatus(aStatus);
}
/**
 * Answers the result's return code.
 * @return int
 */
public int getReturnCode() {
	return returnCode;
}
/**
 * Answers the status to which the work was moved.
 * @return java.lang.String status
 */
public java.lang.String getStatus() {
	return status;
}
/**
 * Answers the text associated with the result.
 * @return java.lang.String result text
 */
public java.lang.String getText() {
	return text;
}
/**
 * Sets the return code.
 * @param newReturnCode
 */
public void setReturnCode(int newReturnCode) {
	returnCode = newReturnCode;
}
/**
 * Sets the status to which a work object was moved.
 * @param newStatus java.lang.String
 */
public void setStatus(java.lang.String newStatus) {
	status = newStatus;
}
/**
 * Sets the message to be associated with this result.
 * @param newText java.lang.String
 */
public void setText(java.lang.String newText) {
	text = newText;
}
/**
 * Return the number of items successfully processed
 */
//NBA308 New Method
public int getCountSuccessful() {
    return countSuccessful;
}
/**
 * Set the number of items successfully processed
 * @param countSuccessful
 */
//NBA308 New Method
public void setCountSuccessful(int countSuccessful) {
    this.countSuccessful = countSuccessful;
}
}
