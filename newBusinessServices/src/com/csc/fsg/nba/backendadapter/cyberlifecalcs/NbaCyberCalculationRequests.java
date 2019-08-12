package com.csc.fsg.nba.backendadapter.cyberlifecalcs;
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
 * 
 * *******************************************************************************<BR>
 */
import com.csc.fs.sa.accel.interaction.services.AccelCyberLifeDXEDataTransformationIntf;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberConstants;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberRequests;
import com.csc.fsg.nba.backendadapter.cyberlifeInforce.NbaCyberInforceAppAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
/**
 * This class create the data streams(DXE) for cyberlife backend calculations.
 * <p>
 * <b>Modifications:</b><br>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * </table>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
// NBA195 signature modified
public class NbaCyberCalculationRequests extends NbaCyberRequests implements AccelCyberLifeDXEDataTransformationIntf {
    
    private long calcType = -1;
    private NbaTXLife nbaTXLife = null;
	/**
	 * NbaCyberInforceRequests constructor.
	 */
	public NbaCyberCalculationRequests() {
		super();
	}

	/**
	 * Create calculation request to be sent to the cyberlife backend
	 * @param calcType the calculation type
	 * @param nbaTXLife the request
	 * @return the calculation request to be sent to the cyberlife backend
	 * @throws NbaBaseException
	 */
	public String createCalculationRequest(long calcType, NbaTXLife nbaTXLife) throws NbaBaseException {
        setCalcType(calcType);
        setNbaTXLife(nbaTXLife);
        StringBuffer requestDXE = new StringBuffer();
        requestDXE.append(getIS00Dxe()); //IS00 transaction
        requestDXE.append(getResolveVariableRequest());
        return requestDXE.toString();
    }

    /**
     * @return
     * @throws NbaBaseException
     */
    protected String getIS00Dxe() throws NbaBaseException {
        return new NbaCyberInforceAppAdapter().createIS00Request(getNbaTXLife().getTXLife());
    }
    
    /**
     * Returns the DXE string for a calculation type
     * @return the DXE string for a calculation type
     * @throws NbaBaseException
     */
    protected String getResolveVariableRequest() throws NbaBaseException {
        String calcDxe = null;
        if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_STDMODEPREMIUM) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_STD_MODES_PREMIUM);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_NONSTDMODEPREMIUM) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_NON_STANDARD_MODE_PREMIUM);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_COMMISSIONTARGET) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_COMMISSION_TARGET);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_GUIDELINEPREMIUM) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_GUIDELINE_PREMIUM);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_JOINTEQUALAGE) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_JOINT_EQUAL_AGE);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_MAPTARGET) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_MIN_NO_LAPSE_PREMIUM);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_LIFECOVERAGE) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_COVERAGE_PREMIUM);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_LIFECOVOPTION || calcType == NbaOliConstants.OLIX_CHANGETYPE_RIDERCOVOPTION) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_COVERAGE_OPTION_PREMIUM);
        } else if (getCalcType() == NbaOliConstants.OLIX_CHANGETYPE_COVERAGESUBRATING) {
            calcDxe = createCalcResolveDxe(NbaCyberCalculationConstants.RESOLVE_SUBRATING_PREMIUM);
        } else {
            throw new NbaBaseException("Calculation is not supported for " + getCalcType());
        }
        return calcDxe;
    }
    
    /**
     * Creates request DXE string
     * @param resolveVariableString the resolve DXE string
     * @return
     */
    protected String createCalcResolveDxe(String resolveVariableString){
        StringBuffer buffer = new StringBuffer();
	    buffer.append(getCalcDxeHeader());
	    buffer.append(resolveVariableString);
	    return buffer.toString();
    }
    
	/**
	 * Returns common DXE header for calculation requests
	 * @return the common DXE header for calculation requests
	 */
	protected String getCalcDxeHeader() {
        Policy policy = getNbaTXLife().getPolicy();
        StringBuffer buffer = new StringBuffer();
        buffer.append(NbaCyberCalculationConstants.TRANSACTION_TYPE_HOLDING);
        buffer.append(NbaCyberConstants.COMP_CODE);
        buffer.append("=");
        buffer.append(policy.getCarrierCode());
        buffer.append(";");
        buffer.append(NbaCyberConstants.POL_NUM);
        buffer.append("=");
        buffer.append(policy.getPolNumber());
        buffer.append(";");
        buffer.append(NbaCyberCalculationConstants.POL_LOC_CALCULATION);
        return buffer.toString();
    }
	
    /**
     * Returns the calculation type
     * @return the calculation type.
     */
    public long getCalcType() {
        return calcType;
    }
    
    /**
     * Sets the calculation type
     * @param calcType the calculation type to set.
     */
    public void setCalcType(long calcType) {
        this.calcType = calcType;
    }
    
    /**
     * Returns the calculation request
     * @return the calculation request.
     */
    public NbaTXLife getNbaTXLife() {
        return nbaTXLife;
    }
    
    /**
     * Sets calculation request
     * @param nbaTXLife calculation request to set.
     */
    public void setNbaTXLife(NbaTXLife nbaTXLife) {
        this.nbaTXLife = nbaTXLife;
    }
    
    /**
	 * Prepares DXE request for CyberLife from ACORD XML transaction passed as request
	 * @param request the NbaTXLife request for calculation
	 * @return DXE Request for CyberLife
	 * @throws NbaBaseException if no response from host
	 * @see com.csc.fsg.nba.backendadapter.NbaBackEndAdapter#prepareRequestToHost(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	//NBA195 new method
	public String prepareRequestToHost(Object request) {
		UserAuthRequestAndTXLifeRequest userRequest = ((NbaTXLife) request).getTXLife().getUserAuthRequestAndTXLifeRequest();
		String clRequest = null;
		if (userRequest != null && userRequest.getTXLifeRequestCount() > 0) {
			TXLifeRequest txRequest = userRequest.getTXLifeRequestAt(0);
			long transType = txRequest.getTransType();
			long transSubType = txRequest.getTransSubType();
			long changeSubType = -1;
			if (txRequest.getChangeSubTypeCount() > 0) {
				changeSubType = txRequest.getChangeSubTypeAt(0).getChangeTC();
			}
			if (transType == NbaOliConstants.TC_TYPE_HOLDINGINQ && transSubType == NbaOliConstants.TC_SUBTYPE_BACKEND_CALCULATIONS
					&& changeSubType > -1) {
				// [TODO] Determine why a new instance of this same class is needed
				NbaCyberCalculationRequests cyberRequest = new NbaCyberCalculationRequests();
				try {
					clRequest = cyberRequest.createCalculationRequest(changeSubType, (NbaTXLife) request);
				} catch (NbaBaseException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException(new NbaBaseException("Invalid Calculation Type Requested"));
			}

		} else {
			throw new RuntimeException(new NbaBaseException("Invalid Transaction Type Requested"));
		}
		return clRequest;
	}

	/**
	 * Parse the calculation results from host response. It creates XMLIfe resposne from incoming request and update reponse with calculation results.
	 * @param request the NnaTXLife request for calculation
	 * @param hostResponse the DXE response from host
	 * @return the NbaTXLife response updated with calculation results.
	 * @throws NbaBaseException if no response from host
	 * @see com.csc.fsg.nba.backendadapter.NbaBackEndAdapter#parseHostResponse(com.csc.fsg.nba.vo.NbaTXLife, java.lang.String)
	 */
	//NBA195 new method
	public Object parseHostResponse(Object request, String hostResponse) {
		NbaCyberCalculationParser cyberParser = new NbaCyberCalculationParser();
		UserAuthRequestAndTXLifeRequest userRequest = ((NbaTXLife) request).getTXLife().getUserAuthRequestAndTXLifeRequest();
		long changeSubType = -1;
		if (userRequest != null && userRequest.getTXLifeRequestCount() > 0) {
			TXLifeRequest txRequest = userRequest.getTXLifeRequestAt(0);
			if (txRequest.getChangeSubTypeCount() > 0) {
				changeSubType = txRequest.getChangeSubTypeAt(0).getChangeTC();
			}
		}
		cyberParser.setHostResponse(hostResponse);
		cyberParser.setCalcType(changeSubType);
		if (hostResponse == null || hostResponse.length() < 2
				|| hostResponse.substring(0, 2).compareTo(String.valueOf(NbaOliConstants.OLI_TC_NULL)) == 0) {
			throw new RuntimeException(new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_HOST_UNAVAILABLE + "  " + hostResponse,
					NbaExceptionType.FATAL));
		}
		try {
			//create the XML response
			return cyberParser.createXmlResponse((NbaTXLife) request);
		} catch (NbaBaseException e) {
			throw new RuntimeException(e);
		}
	}
}
