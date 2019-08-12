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
import com.csc.fsg.nba.business.process.NbaProcAutoUnderwriting;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;

/**
 * NbaVpmsRequirementDebitCreditScoreData processes data passed to it by the NbaProcAutoUnderwriting process.
 * This data comes from the AutoUnderwriting VP/MS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA010</td><td>Version 3</td><td>Hooks for Iterative Underwriting</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProcAutoUnderwriting
 * @since New Business Accelerator - Version 3
 */
public class NbaVpmsRequirementDebitCreditScoreData extends NbaVpmsData {
	protected String debitScore;
	protected String message;
	protected String creditScore;	
	private static NbaLogger logger = null;
	/**
	 * Returns the message.
	 * @return String
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the score.
	 * @return String
	 */
	public String getDebitScore() {
		return debitScore;
	}
	/**
	 * Returns the score.
	 * @return String
	 */
	public String getCreditScore() {
		return creditScore;
	}
	/**
	 * Sets the message.
	 * @param message The message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Sets the score.
	 * @param score The score to set
	 */
	public void setDebitScore(String debitScore) {
		this.debitScore = debitScore;
	}
	/**
	 * Sets the score.
	 * @param score The score to set
	 */
	public void setCreditScore(String creditScore) {
		this.creditScore  = creditScore;
	}
	
	/**
	 * Default constructor.
	 */
	public NbaVpmsRequirementDebitCreditScoreData() {
		super();
	}
	
	/**
	 * This constructor invokes the base class constructor to parse the 
	 * VpmsComputeResult object and populates its data members with the 
	 * result data by invoking the parseResults() method.
	 * @param aResult the result of the call to the Automated Underwriting 
	 *                VPMS model
	 */
	// NBA021 Changed signature to remove String parameter
	public NbaVpmsRequirementDebitCreditScoreData(VpmsComputeResult aResult) {
	    super(aResult);
	    parseResults(); // NBA021
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaVpmsRequirementDebitCreditScoreData.class.getName()); //NBA103
			} catch (Exception e) {
				NbaBootLogger.log("NbaVpmsRequirementScoreData could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	/**
	 * When a case fails automated underwriting, the errors returned by the
	 * VPMS model are parsed from the result message and stored in the
	 * auErrors data member.
	 */
	public void parseResults() {
	    if (result.getReturnCode() == 0) { 
	        NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
	        if(tokens.hasMoreTokens()){
	            debitScore = tokens.nextToken();
	            if(tokens.hasMoreTokens()){
	            	creditScore = tokens.nextToken();
	            }
	            if(tokens.hasMoreTokens()){
	            	message = tokens.nextToken();
	            }
	   
	        }
	    }
	}
}
