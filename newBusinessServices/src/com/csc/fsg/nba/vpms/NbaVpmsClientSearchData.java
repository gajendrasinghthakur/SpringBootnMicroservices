package com.csc.fsg.nba.vpms; //NBA201
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
 
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.NbaAutoContractNumber;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
/**
 * This class represents the result of a vpms computation for the ClientSearch model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA067</td><td>Version 3</td><td>Client Search</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaAutoContractNumber
 * @since New Business Accelerator - Version 3
 */

public class NbaVpmsClientSearchData extends NbaVpmsData {
	protected String lastNameOrOrganization, firstNameOrTaxID,middleName, birthDate, birthState,gender, govtId;
	
	/**
	 * NbaVpmsClientSearchData constructor passes the <code>VpmsComputeResult</code> object
	 * to the super class.
	 * @param aResult the results of a call to the ClientSearch VPMS model.
	 * @throws NbaVpmsException
	 */
	public NbaVpmsClientSearchData(VpmsComputeResult aResult) throws NbaVpmsException {
		super(aResult);
		parseResults(); 
	}
	/**
	 * Returns the LastName.
	 * @return String
	 */
	public String getLastNameOrOrganization() {
		return lastNameOrOrganization;
	}
	/**
		 * Returns the FirstName.
		 * @return String
		 */
		public String getFirstNameOrTaxID() {
			return firstNameOrTaxID;
		}
	/**
		 * Returns the MiddleName.
		 * @return String
		 */
		public String getMiddleName() {
			return middleName;
		}
	/**
		 * Returns the BirthDate.
		 * @return String
		 */
		public String getBirthDate() {
			return birthDate;
		}
	/**
		 * Returns the Gender.
		 * @return String
		 */
		public String getGender() {
			return gender;
		}
	/**
		 * Returns the GovtId.
		 * @return String
		 */
		public String getGovtId() {
			return govtId;
		}
	/**
	* Returns the BirthDate.
	 * @return String
	 */
	  public String getBirthState() {
			return birthState; 
	  }


	/**
	* Parses the data from the results of the ClienSearch VPM model . It uses a default delimiter 
	*@throws NbaVpmsException
	*/
	public void parseResults() throws com.csc.fsg.nba.exception.NbaVpmsException {
		if (result.getReturnCode() == 0) {
			NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			if(tokens.hasMoreTokens()){
			  lastNameOrOrganization = tokens.nextToken();
			 }
			 if(tokens.hasMoreTokens()){
			   firstNameOrTaxID = tokens.nextToken();
			 }
			 if(tokens.hasMoreTokens()){
			   middleName = tokens.nextToken();
			 }
			 if(tokens.hasMoreTokens()){
			    birthDate = tokens.nextToken();
			 }
			 if(tokens.hasMoreTokens()){
			    gender = tokens.nextToken();
		     }
			 if(tokens.hasMoreTokens()){
			    govtId = tokens.nextToken();
		     }
			if(tokens.hasMoreTokens()){
				birthState = tokens.nextToken();
			 }
		result.setResult(String.valueOf(result.getReturnCode()));
	}
  }	
}
