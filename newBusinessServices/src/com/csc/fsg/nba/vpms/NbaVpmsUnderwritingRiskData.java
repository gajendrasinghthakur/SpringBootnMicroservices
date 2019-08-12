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

import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.process.NbaAutoContractNumber;

/**
 * This class parses data returned by vpms model for Underwriting Risk
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA088</td><td>Version 3</td><td>Underwriting Risk</td></tr>
 * <tr><td>SPR1753</td><td>Version 5</td><td>Automated Underwriting and Requirements Determination Should Detect Severe Errors for Both AC and Non - AC</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaAutoContractNumber
 * @since New Business Accelerator - Version 1
 */

public class NbaVpmsUnderwritingRiskData extends NbaVpmsData {
	public String totalInforceAndApplied;
	public String totalPending;
	public String totalAdditional;
	public String highestAlternate;
	public String totalInforce;
	private String totalAmtAtRisk; //ALS4744

	/**
	 * This constructor calls the super class constructor passing it aResult
	 * and then parses the data from aResult into its data member.
	 * @param aResult the result of the call to the Underwriting Risk 
	 *                VPM model
	 */
	public NbaVpmsUnderwritingRiskData(VpmsComputeResult aResult) {
		super(aResult);
		parseResults();
	}



	/**
	 * If the VPM model result is 0, then tokenize the result seprated by ## 
	 * and populate the class-level variables.
	 */
	public void parseResults() {
		if (result.getReturnCode() == 0) {
			int i = 0;
			// SPR3290 code deleted
			String value = null;
			StringTokenizer subTokens = null;
			StringTokenizer tokens = new StringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[1]);

	        while (tokens.hasMoreTokens()) {
	        	subTokens = new StringTokenizer(tokens.nextToken(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
	        	while(subTokens.hasMoreTokens()){
	        		subTokens.nextToken(); // SPR3290
	        		if(subTokens.hasMoreTokens()){
	        			value = subTokens.nextToken();
	        		}else{
	        			value = null;	
	        		}
	        		if(i==0){
	        			setTotalInforceAndApplied(value);
	        		}else if(i==1){
	        			setTotalPending(value);
	        		}else if(i==2){
	        			setTotalAdditional(value);
	        		}else if(i==3){
	        			setHighestAlternate(value);
	        		}else if(i==4){
	        			setTotalInforce(value);
	        		} else if (i == 5) { //ALS4744
	        			setTotalAmtAtRisk(value); //ALS4744
	        		}
	        		i++;
	        	}
	        	
	        }
		} else {
		}
	}
	
	public static void main(String args[]){
		VpmsComputeResult result = new VpmsComputeResult();
		result.setResult("a##20@@b##50@@c##@@d##60@@e##");
		// SPR3290 code deleted
	}

	/**
	 * Returns the highestAlternate.
	 * @return String
	 */
	public String getHighestAlternate() {
		return highestAlternate;
	}

	/**
	 * Returns the totalAdditional.
	 * @return String
	 */
	public String getTotalAdditional() {
		return totalAdditional;
	}

	/**
	 * Returns the totalInforce.
	 * @return String
	 */
	public String getTotalInforce() {
		return totalInforce;
	}

	/**
	 * Returns the totalInforceAndApplied.
	 * @return String
	 */
	public String getTotalInforceAndApplied() {
		return totalInforceAndApplied;
	}

	/**
	 * Returns the totalPending.
	 * @return String
	 */
	public String getTotalPending() {
		return totalPending;
	}

	/**
	 * Sets the highestAlternate.
	 * @param highestAlternate The highestAlternate to set
	 */
	public void setHighestAlternate(String highestAlternate) {
		this.highestAlternate = highestAlternate;
	}

	/**
	 * Sets the totalAdditional.
	 * @param totalAdditional The totalAdditional to set
	 */
	public void setTotalAdditional(String totalAdditional) {
		this.totalAdditional = totalAdditional;
	}

	/**
	 * Sets the totalInforce.
	 * @param totalInforce The totalInforce to set
	 */
	public void setTotalInforce(String totalInforce) {
		this.totalInforce = totalInforce;
	}

	/**
	 * Sets the totalInforceAndApplied.
	 * @param totalInforceAndApplied The totalInforceAndApplied to set
	 */
	public void setTotalInforceAndApplied(String totalInforceAndApplied) {
		this.totalInforceAndApplied = totalInforceAndApplied;
	}

	/**
	 * Sets the totalPending.
	 * @param totalPending The totalPending to set
	 */
	public void setTotalPending(String totalPending) {
		this.totalPending = totalPending;
	}
	/**
	 * Checks whether highest alternate total is available
	 * @return true is desired total is available, false otherwise
	 */
	//SPR1753 New Method
	public boolean hasHighestAlternate() {
		return (highestAlternate != null && highestAlternate.length() > 0);
	}

	/**
	 * Checks whether additional total is available
	 * @return true is desired total is available, false otherwise
	 */
	//SPR1753 New Method
	public boolean hasTotalAdditional() {
		return (totalAdditional != null && totalAdditional.length() > 0);
	}

	/**
	 * Checks whether inforce total is available
	 * @return true is desired total is available, false otherwise
	 */
	//SPR1753 New Method
	public boolean hasTotalInforce() {
		return (totalInforce != null && totalInforce.length() > 0);
	}

	/**
	 * Checks whether inforce and pending total is available
	 * @return true is desired total is available, false otherwise
	 */
	//SPR1753 New Method
	public boolean hasTotalInforceAndApplied() {
		return (totalInforceAndApplied != null && totalInforceAndApplied.length() > 0);
	}
	/**
	 * Checks whether pending total is available
	 * @return true is desired total is available, false otherwise
	 */
	//SPR1753 New Method	
	public boolean hasTotalPending() {
		return (totalPending != null && totalPending.length() > 0);
	}
	//ALS4744 New Method
	public String getTotalAmtAtRisk() {
		return totalAmtAtRisk;
	}
	//ALS4744 New Method
	public void setTotalAmtAtRisk(String totalAmtAtRisk) {
		this.totalAmtAtRisk = totalAmtAtRisk;
	}
}
