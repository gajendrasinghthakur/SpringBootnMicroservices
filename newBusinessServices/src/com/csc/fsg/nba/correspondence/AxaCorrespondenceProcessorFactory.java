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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
package com.csc.fsg.nba.correspondence;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * 
 * This is the factory class to get the instance of WebService invoker class. It decides which Web Service invoker
 * instance should be returned based-on entries passed.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead> 
 * <tr><td>AXAL3.7.13</td><td>Version 7</td><td>Formal Correspondence</td></tr>
 * <tr><td>ALPC195</td><td>AXA Life Phase 1</td><td>AUD Negative Correspondence</td></tr>
 * </table>
 * <p>
 */
public class AxaCorrespondenceProcessorFactory implements AXACorrespondenceConstants{

    /**
     * This method takes 5 arguments and returns the instance of letters processor class based on the letter name.
     * 
     * @param letterName
     * @param userVO
     * @param nbaTxLife
     * @param nbaDst
     * @param object
     * @return AXACorrespondenceProcessorBase which contains instance of specific letter processor 
     * @throws NbaBaseException
     */
    public static AXACorrespondenceProcessorBase createCorrespondenceProcessorRequestor(String letterName, NbaUserVO userVO, NbaTXLife nbaTxLife, 
    		NbaDst nbaDst, Object object)
			throws NbaBaseException {
		try {
			if(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_LETTER_NAME).equalsIgnoreCase(letterName)) {
				return new AXAReinsuranceCorrespondenceVariableResolver(userVO, nbaTxLife, nbaDst, object);//AXLS3.7.32
			}else if(nbaTxLife.isSIApplication()){
				return new AXASICorrespondenceVariableResolver(userVO, nbaTxLife, nbaDst, object);//APSL2808
			}
			return new AXACorrespondenceVariableResolverProcessor(userVO, nbaTxLife, nbaDst, object); //ALS4231
		} catch (Exception exp) {
			NbaBaseException nce = new NbaBaseException(exp);
			NbaLogFactory.getLogger(AXACorrespondenceProcessorBase.class).logException("Unable to create correspondence processor for " + letterName, nce); //NBA103
			throw nce;
		}
	}

    protected static boolean isReplacementLetter (String ltrName){
    	boolean flag = false;
    	for (int i =0 ; i < REPLACEMENT_LETTERS.length; i ++){
    		if ( REPLACEMENT_LETTERS[i].equalsIgnoreCase(ltrName)){
    			flag = true;
    			break;
    		}
    	}
    	return flag;
    }

    //ALPC195 New method
    protected static boolean isAUDLetter(String ltrName){
    	boolean flag = false;
    	for (int i =0 ; i < AUD_LETTERS.length; i ++){
    		if ( AUD_LETTERS[i].equalsIgnoreCase(ltrName)){
    			flag = true;
    			break;
    		}
    	}
    	return flag;
    }

    
}
