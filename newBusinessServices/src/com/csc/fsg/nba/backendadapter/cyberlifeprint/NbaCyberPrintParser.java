package com.csc.fsg.nba.backendadapter.cyberlifeprint;

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
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * Parse the CyberLife host response and create a XML document to send back out.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public class NbaCyberPrintParser {
    private String hostResponse = null;
    /**
     * @return Returns the hostResponse.
     */
    public String getHostResponse() {
        return hostResponse;
    }
    /**
     * @param hostResponse The hostResponse to set.
     */
    public void setHostResponse(String hostResponse) {
        this.hostResponse = hostResponse;
    }

	
	/**
	 * This method acts as the entry point for the creation of the Xml response from host response 
	 * @param txLifeRequest the incoming response from the host.
	 * @return the XMLIfe response for the host response
	 */
	public NbaTXLife createXmlResponse(NbaTXLife txLifeRequest) throws NbaBaseException {
	    NbaTXLife response = new NbaTXLife();
	    //convert TXLifeRequest to TXLifeResponse
	    response.setTXLife(NbaTXLife.createTXLifeResponse(txLifeRequest.getTXLife())); 
		//copy OlifE from request to resonse
	    updateFromHostResponse(response);
		return response;
	}
	
	protected void updateFromHostResponse(NbaTXLife response) throws NbaBaseException {
	    UserAuthResponseAndTXLifeResponseAndTXLifeNotify userAuth = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
	    TXLifeResponse txLifeResponse = userAuth.getTXLifeResponseAt(0);
	    TransResult transResult = new TransResult();
		txLifeResponse.setTransResult(getTransResult(transResult));		
    }
	
	/**
     * Gets the transaction results
     * 
     * @param transResult
     * @return TransResult
     */
	protected TransResult getTransResult(TransResult transResult) {
        int beginIndex = 0;
        int endIndex = 0;
        int newIndex = 0;
        int currentTransResult = 0;
        int maxTransResult = 0;
        String errorDesc = new String();

        //first loop through and find the various return codes
        while (hostResponse.length() > beginIndex) {
            beginIndex = hostResponse.indexOf("WHATTODO", beginIndex);
            //if no more occurences return the transResult
            if (beginIndex == -1) {
                break;
            }
            //get the return code
            beginIndex = hostResponse.indexOf(",", beginIndex);
            endIndex = hostResponse.indexOf(";", beginIndex);
            currentTransResult = NbaUtils.convertStringToInt(hostResponse.substring(beginIndex + 1, endIndex));
            //set Result Code and check to see if any error messages
            if (currentTransResult > maxTransResult) {
                maxTransResult = currentTransResult;
            }
            while (hostResponse.length() > beginIndex) {
                newIndex = hostResponse.indexOf("ERR=", beginIndex);
                if (newIndex == -1) {
                    break;
                } else {
                    ResultInfo resultInfo = new ResultInfo();
                    if ((currentTransResult == NbaCyberConstants.HOST_UNAVAILABLE) || (currentTransResult == NbaCyberConstants.HOST_ABEND)) {
                        resultInfo.setResultInfoCode(NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL);
                    } else {
                        resultInfo.setResultInfoCode(NbaOliConstants.TC_RESINFO_UNKNOWNREASON);
                    }
                    endIndex = hostResponse.indexOf(";", newIndex);
                    String currentError = hostResponse.substring(newIndex + 4, endIndex);
                    errorDesc = "";
                    errorDesc = errorDesc.concat("  " + currentError);
                    if (hostResponse.length() == endIndex + 1) {
                        resultInfo.setResultInfoDesc(errorDesc);
                        beginIndex = endIndex;
                        transResult.addResultInfo(resultInfo);
                        break;
                    }
                    if (hostResponse.substring(endIndex + 1, endIndex + 5).compareTo("ERR=") == 0) {
                        resultInfo.setResultInfoDesc(errorDesc);
                        beginIndex = endIndex;
                        transResult.addResultInfo(resultInfo);
                        continue;
                    } else {
                        resultInfo.setResultInfoDesc(errorDesc);
                        beginIndex = endIndex;
                        transResult.addResultInfo(resultInfo);
                        break;
                    }
                }
            }
        }
        switch (maxTransResult) {
	        case NbaCyberConstants.SUCCESS:
	        case NbaCyberConstants.SUCCESS_FORCIBLE:
	            transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
	            break;
	        case NbaCyberConstants.DATA_FAILURE:
	        case NbaCyberConstants.TRANSACTION_FAILURE:
	        case NbaCyberConstants.HOST_UNAVAILABLE:
	        case NbaCyberConstants.HOST_ABEND:
	        case NbaCyberConstants.BAD_INFO_RETURNED:
	        default:
	            transResult.setResultCode(NbaOliConstants.TC_RESCODE_FAILURE);
	            break;
        }
        return transResult;

    }
}
	

