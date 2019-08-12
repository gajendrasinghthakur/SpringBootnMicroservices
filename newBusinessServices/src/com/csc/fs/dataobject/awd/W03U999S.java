package com.csc.fs.dataobject.awd;

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
 *APSL5055 New Class
 *
 */

import java.io.Serializable;


/**
 * W03U999S is a data object representing a single row from the W03U999S table in the AWD
 * database schema.  This class is used to query and data in the W03U999S table.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331.1</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class W03U999S implements Serializable {
	
	private static final long serialVersionUID = -8034191877993472631L;

	private String crDatTim = null;
	private String recordCd = null;
	private String crNode = null;
	private String ownrNode = null;
	private String untCd = null;
	private String wrkType = null;
	private String sttCd = null;
	private String queCd = null;
	private String prty = null;
	private String incrs = null;
	private String viFlg = null;
	private String suspFlg = null;
	private String lockStt = null;
	private String amntType = null;
	private String amnt = null;
	private String inxfld01 = null;
	private String inxfld02 = null;
	private String inxfld03 = null;
	private String inxfld04 = null;
	private String endQueFlg = null;
	private String lastNonBatchUser = null;
	private String stChgDattim = null;
	
	/**
	 * @return the crDatTim
	 */
	public String getCrDatTim() {
		return crDatTim;
	}
	/**
	 * @param crDatTim the crDatTim to set
	 */
	public void setCrDatTim(String crDatTim) {
		this.crDatTim = crDatTim;
	}
	/**
	 * @return the recordCd
	 */
	public String getRecordCd() {
		return recordCd;
	}
	/**
	 * @param recordCd the recordCd to set
	 */
	public void setRecordCd(String recordCd) {
		this.recordCd = recordCd;
	}
	/**
	 * @return the crNode
	 */
	public String getCrNode() {
		return crNode;
	}
	/**
	 * @param crNode the crNode to set
	 */
	public void setCrNode(String crNode) {
		this.crNode = crNode;
	}
	/**
	 * @return the ownrNode
	 */
	public String getOwnrNode() {
		return ownrNode;
	}
	/**
	 * @param ownrNode the ownrNode to set
	 */
	public void setOwnrNode(String ownrNode) {
		this.ownrNode = ownrNode;
	}
	/**
	 * @return the untCd
	 */
	public String getUntCd() {
		return untCd;
	}
	/**
	 * @param untCd the dataName to set
	 */
	public void setUntCd(String untCd) {
		this.untCd = untCd;
	}
	/**
	 * @return the wrkType
	 */
	public String getWrkType() {
		return wrkType;
	}
	/**
	 * @param wrkType the dataValue to set
	 */
	public void setWrkType(String wrkType) {
		this.wrkType = wrkType;
	}
	
	/**
	 *  @return the sttCd
	 */
	public String getSttCd() {
		return sttCd;
	}
	/**
	 * @param sttCd the dataValue to set
	 */
	public void setSttCd(String sttCd) {
		this.sttCd = sttCd;
	}
	/**
	 * @return the queCd
	 */
	public String getQueCd() {
		return queCd;
	}
	/**
	 * @param queCd the dataValue to set
	 */
	public void setQueCd(String queCd) {
		this.queCd = queCd;
	}
	/**
	 * @return the prty
	 */
	public String getPrty() {
		return prty;
	}
	/**
	 * @param prty the dataValue to set
	 */
	public void setPrty(String prty) {
		this.prty = prty;
	}
	/**
	 * @return the incrs
	 */
	public String getIncrs() {
		return incrs;
	}
	/**
	 * @param incrs the dataValue to set
	 */
	public void setIncrs(String incrs) {
		this.incrs = incrs;
	}
	/**
	 * @return the viFlg
	 */
	public String getViFlg() {
		return viFlg;
	}
	/**
	 * @param viFlg the dataValue to set
	 */
	public void setViFlg(String viFlg) {
		this.viFlg = viFlg;
	}
	/**
	 * @return the suspFlg
	 */
	public String getSuspFlg() {
		return suspFlg;
	}
	/**
	 * @param suspFlg the dataValue to set
	 */
	public void setSuspFlg(String suspFlg) {
		this.suspFlg = suspFlg;
	}
	/**
	 * @return the lockStt
	 */
	public String getLockStt() {
		return lockStt;
	}
	/**
	 * @param lockStt the dataValue to set
	 */
	public void setLockStt(String lockStt) {
		this.lockStt = lockStt;
	}
	/**
	 * @return the amntType
	 */
	public String getAmntType() {
		return amntType;
	}
	/**
	 * @param amntType the dataValue to set
	 */
	public void setAmntType(String amntType) {
		this.amntType = amntType;
	}
	/**
	 * @return the amnt
	 */
	public String getAmnt() {
		return amnt;
	}
	/**
	 * @param amnt the dataValue to set
	 */
	public void setAmnt(String amnt) {
		this.amnt = amnt;
	}
	/**
	 * @return the inxfld01
	 */
	public String getInxfld01() {
		return inxfld01;
	}
	/**
	 * @param inxfld01 the dataValue to set
	 */
	public void setInxfld01(String inxfld01) {
		this.inxfld01 = inxfld01;
	}
	/**
	 * @return the inxfld02
	 */
	public String getInxfld02() {
		return inxfld02;
	}
	/**
	 * @param inxfld02 the dataValue to set
	 */
	public void setInxfld02(String inxfld02) {
		this.inxfld02 = inxfld02;
	}
	/**
	 * @return the inxfld03
	 */
	public String getInxfld03() {
		return inxfld03;
	}
	/**
	 * @param inxfld03 the dataValue to set
	 */
	public void setInxfld03(String inxfld03) {
		this.inxfld03 = inxfld03;
	}
	/**
	 * @return the inxfld04
	 */
	public String getInxfld04() {
		return inxfld04;
	}
	/**
	 * @param inxfld04 the dataValue to set
	 */
	public void setInxfld04(String inxfld04) {
		this.inxfld04 = inxfld04;
	}
	/**
	 * @return the endQueFlg
	 */
	public String getEndQueFlg() {
		return endQueFlg;
	}
	/**
	 * @param endQueFlg the dataValue to set
	 */
	public void setEndQueFlg(String endQueFlg) {
		this.endQueFlg = endQueFlg;
	}
	/**
	 * @return the lastNonBatchUser
	 */
	public String getLastNonBatchUser() {
		return lastNonBatchUser;
	}
	/**
	 * @param lastNonBatchUser the dataValue to set
	 */
	public void setLastNonBatchUser(String lastNonBatchUser) {
		this.lastNonBatchUser = lastNonBatchUser;
	}
	/**
	 * @return the stChgDattim
	 */
	public String getStChgDattim() {
		return stChgDattim;
	}
	/**
	 * @param stChgDattim the dataValue to set
	 */
	public void setStChgDattim(String stChgDattim) {
		this.stChgDattim = stChgDattim;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o != null && o instanceof W03U999S) {
			String key = getKey();
			if (key != null) {
				return key.equals(((W03U999S) o).getKey());
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		String key = getKey();
		if (key != null) {
			return key.hashCode();
		}
		return 0;
	}

	protected String getKey() {
		StringBuilder sb = new StringBuilder(100);
		sb.append(getCrDatTim());
		sb.append(getRecordCd());
		sb.append(getCrNode());
		return sb.toString();
	}
}
