package com.csc.fsg.nba.business.transaction;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

/**
 * This class represents the results of an event-to-request.
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
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.transaction.NbaEventToRequest
 * @since New Business Accelerator - Version 1
 */
public class NbaEventToRequestResult implements java.io.Serializable {
	protected int returnCode;
	protected String text;
	protected String status;
	public static final int SUCCESSFUL = 0;
	public static final int FAILED = 1;
	public static final int NO_ATTEMPT = -1;
	private static final long serialVersionUID = 6735656927753560845L;
/**
 * The constructor initializes the class members.
 * @param aReturnCode the result/return code from the request
 * @param aText       a description of the result
 * @param aStatus     the new status to which the work object was moved
 */
public NbaEventToRequestResult(int aReturnCode, String aText, String aStatus) {
	setReturnCode(aReturnCode);
	setText(aText);
	setStatus(aStatus);
}
/**
 * Insert the method's description here.
 * @return int
 */
public int getReturnCode() {
	return returnCode;
}
/**
 * Answers the status to which the work was moved.
 * @return java.lang.String
 */
public java.lang.String getStatus() {
	return status;
}
/**
 * Insert the method's description here.
 * @return java.lang.String
 */
public java.lang.String getText() {
	return text;
}
/**
 * Insert the method's description here.
 * @param newReturnCode int
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
 * Insert the method's description here.
 * @param newText java.lang.String
 */
public void setText(java.lang.String newText) {
	text = newText;
}
}
