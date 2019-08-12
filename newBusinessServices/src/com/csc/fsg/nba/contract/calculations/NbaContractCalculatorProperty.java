package com.csc.fsg.nba.contract.calculations;
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

/**
 * NbaContractCalculatorProperty
 * <p>
 * This class is used as a placeholder for a calculation property, so that
 * multiple properties can be solved with a single call to the VP/MS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaContractCalculatorProperty {
	private String objectID = null;
	private String targetName = null;
	private String transTable = null;
	private int resultIndex = -1;
	private int transIndex = -1;
	/**
	 * Returns the object id for a calculation property.
	 */
	public String getObjectID() {
		return objectID;
	}

	/**
	 * @param aObjectID
	 */
	public void setObjectID(String aObjectID) {
		objectID = aObjectID;
	}

	/**
	 * Returns the result index for a VP/MS compute.
	 */
	public int getResultIndex() {
		return resultIndex;
	}

	/**
	 * @param aResultIndex
	 */
	public void setResultIndex(int aResultIndex) {
		resultIndex = aResultIndex;
	}

	/**
	 * Returns the target result name for the calculation property.
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * @param aTargetName
	 */
	public void setTargetName(String aTargetName) {
		targetName = aTargetName;
	}

	/**
	 * Returns the translation table name for the calculation property.
	 */
	public String getTranslationTable() {
		return transTable;
	}

	/**
	 * @param aTransTable
	 */
	public void setTranslationTable(String aTransTable) {
		transTable = aTransTable;
	}
	
	/**
	 * Returns the translation result index for a VP/MS translation compute.
	 */
	public int getTranslationIndex() {
		return transIndex;
	}

	/**
	 * @param aTransIndex
	 */
	public void setTranslationIndex(int aTransIndex) {
		transIndex = aTransIndex;
	}

}
