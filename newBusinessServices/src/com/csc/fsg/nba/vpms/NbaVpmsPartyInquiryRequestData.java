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

import java.util.HashMap;
import java.util.Map;

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
 * <tr><td>NBA105</td><td>Version 4</td><td>Party Inquiry</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaAutoContractNumber
 * @since New Business Accelerator - Version 4
 */

public class NbaVpmsPartyInquiryRequestData extends NbaVpmsData {
	//Cyberlife Fields
	protected boolean lastName = false;
	protected boolean firstName = false;
	protected boolean middleName = false;
	protected boolean birthDate = false;
	protected boolean birthState = false;
	protected boolean govtID = false;
	//Vantage fields
	protected boolean partyTypeCode = false;
	protected boolean partyKey = false;
	protected boolean residenceState = false;//AXAL3.7.21
	protected Map fieldMap = new HashMap(8, 1);

	/**
	 * NbaVpmsPartyInquiryRequestData constructor passes the <code>VpmsComputeResult</code> object
	 * to the super class.
	 * @param aResult the results of a call to the ClientSearch VPMS model with an entry point for Party inquiry seach keys.
	 * @throws NbaVpmsException
	 */
	public NbaVpmsPartyInquiryRequestData(VpmsComputeResult aResult) throws NbaVpmsException {
		super(aResult);
		parseResults();
	}

	/**
	* Parses the data from the results of the VPM model 
	* @throws NbaVpmsException
	*/
	public void parseResults() throws com.csc.fsg.nba.exception.NbaVpmsException {
		if (result.getReturnCode() == 0) {
			parseResults(result.getResult());
			result.setResult(String.valueOf(result.getReturnCode()));
		}
	}

	/**
	* Parses the data from the results of the VPM model 
	* @param result Result of VPMS model
	* @throws NbaVpmsException
	*/
	protected void parseResults(String result)  {
		NbaStringTokenizer fieldToken = new NbaStringTokenizer(result.trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
		// SPR3290 code deleted
		String fieldName;
		// SPR3290 code deleted
		while (fieldToken.hasMoreTokens()) {
			fieldName = fieldToken.nextToken();
			fieldMap.put(fieldName.toUpperCase(), fieldName.toUpperCase());
		}
		if (fieldRequired((String) fieldMap.get("FIRSTNAME"))) {
			setFirstName(true);
		}

		if (fieldRequired((String) fieldMap.get("LASTNAME"))) {
			setLastName(true);
		}

		if (fieldRequired((String) fieldMap.get("MIDDLENAME"))) {
			setMiddleName(true);
		}

		if (fieldRequired((String) fieldMap.get("GOVTID"))) {
			setGovtID(true);
		}

		if (fieldRequired((String) fieldMap.get("BIRTHDATE"))) {
			setBirthDate(true);
		}

		if (fieldRequired((String) fieldMap.get("BIRTHSTATE"))) {
			setBirthState(true);
		}
		if (fieldRequired((String) fieldMap.get("PARTYKEY"))) {
			setPartyKey(true);
		}
		//begin AXAL3.7.21
		if (fieldRequired((String) fieldMap.get("RESIDENCESTATE"))) {
			setResidenceState(true);
		}
		// end AXAL3.7.21
	}
	/**
	 * @return Returns the residenceState.
	 */
	//New Method AXAL3.7.21
	public boolean isResidenceState() {
		return residenceState;
	}
	/**
	 * @param residenceState The residenceState to set.
	 */
	//New Method AXAL3.7.21
	public void setResidenceState(boolean residenceState) {
		this.residenceState = residenceState;
	}
	/**
	 * Returns true if the passed in fieldname is required otherwise false is returned.
	 * @param fieldValue Required fieldname
	 * @return boolean
	 */
	protected boolean fieldRequired(String fieldValue) {
		if (fieldValue != null && fieldValue.trim().length() > 0 && !"-".equals(fieldValue.trim())) {
			return true;
		}
		return false;
	}

	/**
	 * Answers whether firstName is to be included in crieteria or not
	 * @return boolean
	 */
	public boolean getFirstName() {
		return firstName;
	}

	/**Answers whether  govtID is to be included in crieteria or not
	 * @return  boolean 
	 */
	public boolean getGovtID() {
		return govtID;
	}

	/**Answers whether lastName is to be included in crieteria or not 
	 * @return boolean 
	 */
	public boolean getLastName() {
		return lastName;
	}

	/**Answers whether middleName is to be included in crieteria or not 
	 * @return boolean  
	 */
	public boolean getMiddleName() {
		return middleName;
	}

	/**Sets firstName
	 * @param firstName First name of the Party
	 */
	public void setFirstName(boolean firstName) {
		this.firstName = firstName;
	}

	/**Sets GovtID
	 * @param govtID Govtid of the Party
	 */
	public void setGovtID(boolean govtID) {
		this.govtID = govtID;
	}

	/** Sets LastName
	 * @param lastName Last name of the Party
	 */
	public void setLastName(boolean lastName) {
		this.lastName = lastName;
	}

	/** Sets MiddleName
	 * @param middleName Middlename of the Party
	 */
	public void setMiddleName(boolean middleName) {
		this.middleName = middleName;
	}

	/**Answers whether birthDate is to be included in crieteria or not  
	 * @return boolean 
	 */
	public boolean getBirthDate() {
		return birthDate;
	}

	/**Answers whether birthState is to be included in crieteria or not  
	 * @return boolean 
	 */
	public boolean getBirthState() {
		return birthState;
	}

	/** Sets setBirthDate
	 * @param birthDate Birth date
	 */
	public void setBirthDate(boolean birthDate) {
		this.birthDate = birthDate;
	}

	/** Sets setBirthState
	 * @param birthState birth state
	 */
	public void setBirthState(boolean birthState) {
		this.birthState = birthState;
	}

	/**Answers whether partyKey is to be included in crieteria or not 
	 *  @return boolean 
	 */
	public boolean getPartyKey() {
		return partyKey;
	}

	/**Answers whether  partyTypeCode is to be included in crieteria or not
	 * @return  boolean 
	 */
	public boolean getPartyTypeCode() {
		return partyTypeCode;
	}

	/**Sets partyKey
	 * @param partyKey Party key of the Party
	 */
	public void setPartyKey(boolean partyKey) {
		this.partyKey = partyKey;
	}

	/**Sets partyTypeCode
	 * @param partyTypeCode Party typecode of the Party
	 */
	public void setPartyTypeCode(boolean partyTypeCode) {
		this.partyTypeCode = partyTypeCode;
	}

}
