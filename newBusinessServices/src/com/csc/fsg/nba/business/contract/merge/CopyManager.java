package com.csc.fsg.nba.business.contract.merge;
/**
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
 * 
 */

import com.csc.fsg.nba.business.transaction.NbaContractChangeUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;

/** 
 * 
 * This class has the method which returns the appropriate CopyBox object on the basis of tagName
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * <tr><td>P2AXAL016CV</td><td>Axa Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class CopyManager {
	
	private static NbaLogger logger;

	/**Determines the appropriate CopyBox object based on the tagName
	 * @param tagName
	 * @return CopyBox object
	 */
	public static CopyBox getCopyBox(long tagName) {
    	if(NbaOliConstants.OLI_COVOPTION == tagName) {
    		return new CovOptionCopyBox();
    	}else if(NbaOliConstants.OLI_LIFEPARTICIPANT == tagName) {
    		return new LifeParticipantCopyBox();
    	}else if(NbaOliConstants.OLI_REQUIREMENTINFO == tagName) {
    		return new RequirementInfoCopyBox();
    	}else if(NbaOliConstants.OLI_RELATION == tagName) {
    		return new RelationCopyBox();
    	}else if(NbaOliConstants.OLI_PARTY == tagName) {
    		return new PartyCopyBox();
    	}else if(NbaOliConstants.OLI_LIFEUSA == tagName){//P2AXAL016CV
    		return new LifeUSACopyBox();
    	}
    	return new CopyBox();
    }
	
	/**returns the default CopyBox object
	 * @param tagName
	 * @return returns the default CopyBox object
	 */
	public static CopyBox getCopyBox(String tagName) {
    	return new CopyBox();
    }
	
    /**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
    public static  NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaContractChangeUtils.class);
            } catch (Exception e) {
                NbaBootLogger.log("CopyManager could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

}
