package com.csc.fs.dataobject.nba.nbascorfeed;

import com.csc.fs.dataobject.accel.AccelDataObject;
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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>NBA-SCOR</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaScorFeedDO extends AccelDataObject {
	private String token;
	private String userName;
	private String password;
	private String contractNumber;
	private String userSessionKey;
	private String nbaScorFeedResponse;
	private String workitemID;

	/**
	 * @return Returns the userSessionKey.
	 */
	public String getUserSessionKey() {
		return userSessionKey;
	}

	/**
	 * @param userSessionKey The userSessionKey to set.
	 */
	public void setUserSessionKey(String userSessionKey) {
		this.userSessionKey = userSessionKey;
	}	

	/**
	 * @return Returns the feedResponse.
	 */
	public String getNbaScorFeedResponse() {
		return nbaScorFeedResponse;
	}

	/**
	 * @param feedResponse The feedResponse to set.
	 */
	public void setNbaScorFeedResponse(String nbaScorFeedResponse) {
		this.nbaScorFeedResponse = nbaScorFeedResponse;
	}

	/**
	 * @return Returns the contractNumber.
	 */
	public String getContractNumber() {
		return contractNumber;
	}

	/**
	 * @param contractNumber The contractNumber to set.
	 */
	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	/**
	 * @return Returns the userVO.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param userVO The userVO to set.
	 */
	public void setToken(String userVO) {
		token = userVO;
	}
	/**
	 * @return Returns the workitemID.
	 */
	public String getWorkitemID() {
		return workitemID;
	}
	/**
	 * @param workitemID The workitemID to set.
	 */
	public void setWorkitemID(String workitemID) {
		this.workitemID = workitemID;
	}
	/**
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName The userName to set.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}