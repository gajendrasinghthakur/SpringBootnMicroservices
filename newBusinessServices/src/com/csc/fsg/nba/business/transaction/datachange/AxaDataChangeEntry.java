
package com.csc.fsg.nba.business.transaction.datachange;
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
/**
 * 
 * Helper classes to determine Data change 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangeEntry extends AxaDataChangeComparator{
	private long changedObjectType;
	private String changedObjectId;
	private long changeType;
	
	/**
	 * 
	 */
	public AxaDataChangeEntry() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param changedObjectType
	 * @param changedObjectId
	 * @param changeType
	 */
	public AxaDataChangeEntry(long changeType,String changedObjectId, long changedObjectType) {
		super();
		this.changedObjectType = changedObjectType;
		this.changedObjectId = changedObjectId;
		this.changeType = changeType;
	}
	/**
	 * @return Returns the changedObjectId.
	 */
	public String getChangedObjectId() {
		return changedObjectId;
	}
	/**
	 * @param changedObjectId The changedObjectId to set.
	 */
	public void setChangedObjectId(String changedObjectId) {
		this.changedObjectId = changedObjectId;
	}
	/**
	 * @return Returns the changedObjectType.
	 */
	public long getChangedObjectType() {
		return changedObjectType;
	}
	/**
	 * @param changedObjectType The changedObjectType to set.
	 */
	public void setChangedObjectType(long changedObjectType) {
		this.changedObjectType = changedObjectType;
	}
	/**
	 * @return Returns the changeType.
	 */
	public long getChangeType() {
		return changeType;
	}
	/**
	 * @param changeType The changeType to set.
	 */
	public void setChangeType(long changeType) {
		this.changeType = changeType;
	}
	
	public String toString() {
		return "ChangeType = " + changeType + ",   ObjectType = " + changedObjectType + ",   ObjectId = " + changedObjectId;
	}
	
}
	
	
	
	

