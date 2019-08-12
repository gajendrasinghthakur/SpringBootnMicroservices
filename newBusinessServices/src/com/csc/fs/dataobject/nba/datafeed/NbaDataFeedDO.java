package com.csc.fs.dataobject.nba.datafeed;

import java.util.Date;

import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
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
 * NbaDataFeedDO Add a description of your new type here.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * <tr><td>AXAL3.7.54</td><td>AXA Life Phase 1</td><td>AXAOnline / AXA Distributors Service</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 *
 */
public class NbaDataFeedDO extends AccelDataObject {
    private String contractNumber;
    private String companyCode;
    private String backendSystem;
    private String feedResponse;
    private String userID;
    private Date feedDate;
    private NbaTime feedTime;
    private String processName;
    private String processPwd;
    private String userSessionKey;
    private String operatingMode;
    private String token; //AXAL3.7.54
	/**
	 * @return Returns the operatingMode.
	 */
	public String getOperatingMode() {
		return operatingMode;
	}
	/**
	 * @param operatingMode The operatingMode to set.
	 */
	public void setOperatingMode(String operatingMode) {
		this.operatingMode = operatingMode;
	}
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
	 * @return Returns the processName.
	 */
	public String getProcessName() {
		return processName;
	}
	/**
	 * @param processName The processName to set.
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	/**
	 * @return Returns the processPwd.
	 */
	public String getProcessPwd() {
		return processPwd;
	}
	/**
	 * @param processPwd The processPwd to set.
	 */
	public void setProcessPwd(String processPwd) {
		this.processPwd = processPwd;
	}
	/**
	 * @return Returns the feedResponse.
	 */
	public String getFeedResponse() {
		return feedResponse;
	}
	/**
	 * @param feedResponse The feedResponse to set.
	 */
	public void setFeedResponse(String feedResponse) {
		this.feedResponse = feedResponse;
	}
	/**
	 * @return Returns the backendSystem.
	 */
	public String getBackendSystem() {
		return backendSystem;
	}
	/**
	 * @param backendSystem The backendSystem to set.
	 */
	public void setBackendSystem(String backendSystem) {
		this.backendSystem = backendSystem;
	}
	/**
	 * @return Returns the companyCode.
	 */
	public String getCompanyCode() {
		return companyCode;
	}
	/**
	 * @param companyCode The companyCode to set.
	 */
	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
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
	 * @return Returns the feedDate.
	 */
	public Date getFeedDate() {
		return feedDate;
	}
	/**
	 * @param feedDate The feedDate to set.
	 */
	public void setFeedDate(Date feedDate) {
		this.feedDate = feedDate;
	}
	/**
	 * @return Returns the feedTime.
	 */
	public NbaTime getFeedTime() {
		return feedTime;
	}
	/**
	 * @param feedTime The feedTime to set.
	 */
	public void setFeedTime(NbaTime feedTime) {
		this.feedTime = feedTime;
	}
	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		return userID;
	}
	/**
	 * @param userID The userID to set.
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	/**
	 * @return Returns the userVO.
	 */
	//AXAL3.7.54 new method
	public String getToken() {
		return token;
	}
	/**
	 * @param userVO The userVO to set.
	 */
	//AXAL3.7.54 new method
	public void setToken(String userVO) {
		token = userVO;
	}
}
