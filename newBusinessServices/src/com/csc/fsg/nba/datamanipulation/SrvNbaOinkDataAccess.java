package com.csc.fsg.nba.datamanipulation;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 *  are proprietary to CSC Financial Services Group®.  The use,<BR>
 *  reproduction, distribution or disclosure of this program, in whole or in<BR>
 *  part, without the express written permission of CSC Financial Services<BR>
 *  Group is prohibited.  This program is also an unpublished work protected<BR>
 *  under the copyright laws of the United States of America and other<BR>
 *  countries.  If this program becomes published, the following notice shall<BR>
 *  apply:
 *      Property of Computer Sciences Corporation.<BR>
 *      Confidential. Not for publication.<BR>
 *      Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * The Object Interactive Name Keeper (OINK) Data Access allows information to be 
 * retrieved from or stored into NbaTXLife or NbaLob objects using pre-defined 
 * variable names.
 *
 * The source/destination objects are supplied to NbaOinkDataAccess either with 
 * constructer methods (Sources only) or as arguments to setXxxSource or setXxxDest 
 * methods. When an object is identified, the class responsible for accessing the 
 * values for the object is instantiated by NbaOinkDataAccess and the variable names 
 * and Method objects that may be used are added to a Map of available variable names
 * maintained by NbaOinkDataAccess.
 *
 * When NbaOinkDataAccess is invoked to access information, the variable name is parsed.
 * The value for the root variable is used to locate the corresponding entry in the Map.
 * Reflection is used to message the instance of the class responsible for accessing the 
 * values using the Method object in the Map.
 *
 * For information retrieval, an explicit formatter may be specified using the
 * setFormatter() method. A default formatter is used otherwise.
 *
 * NbaOinkDataAccess may be used to copy values from an NbaLob into a new NbaLob 
 * instance using the cloneNbaLobValues() method. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 * 
 */
public class SrvNbaOinkDataAccess extends NbaOinkDataAccess implements NbaContractDataAccessConstants {

	/**
	 * 
	 */
	public SrvNbaOinkDataAccess() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param newDstSource
	 * @throws NbaBaseException
	 */
	public SrvNbaOinkDataAccess(NbaDst newDstSource) throws NbaBaseException {
		super(newDstSource);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param newLobSource
	 */
	public SrvNbaOinkDataAccess(NbaLob newLobSource) {
		super(newLobSource);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param newContractSource
	 * @throws NbaBaseException
	 */
	public SrvNbaOinkDataAccess(NbaTXLife newContractSource)
			throws NbaBaseException {
		super(newContractSource);
		// TODO Auto-generated constructor stub
	}
	protected NbaRetrievePlanData nbaRetrievePlanData;
	/**
	 * @return
	 */
	public NbaRetrievePlanData getNbaRetrievePlanData() {
		return nbaRetrievePlanData;
	}

	/**
	 * @param data
	 */
	public void setNbaRetrievePlanData(NbaRetrievePlanData data) {
		nbaRetrievePlanData = data;
	}
	
	/**
	 * Store a NbaTxLife containing contract information.
	 * @param newTxLifeSource 
	 */
	public void setPlanSource(NbaTXLife newTxLifeSource) throws NbaBaseException {
		addRetrieveVariables(NbaRetrievePlanData.getVariables());
		setNbaRetrievePlanData(new NbaRetrievePlanData());
		addResolvers(getNbaRetrievePlanData().getClass().getName(), getNbaRetrievePlanData());
		getNbaRetrievePlanData().setUpdateMode(true);
		getNbaRetrievePlanData().initializeObjects(newTxLifeSource);
		getNbaRetrievePlanData().setUpdateMode(false);
	}
}
