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
 * *******************************************************************************<BR>
 */
import java.util.HashMap;
import java.util.List;

import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaRolesRelationData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
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
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class NbaCyberCalculationParser {
    private NbaTableAccessor ntsAccess = null;
    private String hostResponse = null;
    private long calcType = -1;
    private NbaRolesRelationData[] relRolesTable = null;
    private String compCode = null;
    private String prodType = null;
	/**
	 * Acts as the entry point for the creation of the Xml response from host response 
	 * @param txLifeRequest the incoming response from the host.
	 * @return the XMLIfe response for the host response
	 */
	public NbaTXLife createXmlResponse(NbaTXLife txLifeRequest) throws NbaBaseException {
        NbaTXLife response = new NbaTXLife();
        //convert TXLifeRequest to TXLifeResponse
        response.setTXLife(NbaTXLife.createTXLifeResponse(txLifeRequest.getTXLife()));
        //copy OlifE from request to resonse
        response.setOLifE(txLifeRequest.getOLifE());
        setCompCode(response.getCarrierCode());
        setProdType(response.getProductCode());
        updateFromHostResponse(response);
        return response;
    }

    /**
     * Update xml response from host response
     * @param response the xml response
     * @throws NbaBaseException
     */
    protected void updateFromHostResponse(NbaTXLife response) throws NbaBaseException {
        UserAuthResponseAndTXLifeResponseAndTXLifeNotify userAuth = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
        TXLifeResponse txLifeResponse = userAuth.getTXLifeResponseAt(0);
        TransResult transResult = new TransResult();
        txLifeResponse.setTransResult(getTransResult(transResult));

        if (transResult.getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) {
            updateResponseForCalculations(response);
        }
    }

    /**
     * Update xml response for backend calculations
     * @param response the xml response
     * @throws NbaBaseException
     */
    protected void updateResponseForCalculations(NbaTXLife response) throws NbaBaseException {
        if (calcType == NbaOliConstants.OLIX_CHANGETYPE_STDMODEPREMIUM) {
            updateForStandardModePremiumsResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_NONSTDMODEPREMIUM) {
            updateForNonStandardModePremiumsResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_COMMISSIONTARGET) {
            updateForCommissionTargetPremResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_GUIDELINEPREMIUM) {
            updateForGuidelinePremiumsResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_JOINTEQUALAGE) {
            updateForJointEqualAgeResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_MAPTARGET) {
            updateForMinNoLapsePremResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_LIFECOVERAGE) {
            updateForCoverageResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_LIFECOVOPTION || calcType == NbaOliConstants.OLIX_CHANGETYPE_RIDERCOVOPTION) {
            updateForCovOptionResponse(response);
        } else if (calcType == NbaOliConstants.OLIX_CHANGETYPE_COVERAGESUBRATING) {
            updateForSubRatingResponse(response);
        }
    }

    /**
     * Update XML response for standard mode premiums
     * @param response the XML response
     */
    protected void updateForStandardModePremiumsResponse(NbaTXLife response) {
        String[][] stdModePremData = getData(1, NbaCyberCalculationConstants.MODE_PREMIUM_FIELDS);
        Policy policy = response.getPolicy();
        policy.setPaymentAmt(stdModePremData[0][0]);

        PolicyExtension polictExt = NbaUtils.getFirstPolicyExtension(policy);
        if(polictExt == null){
            OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
            policy.addOLifEExtension(oliExt);
            polictExt = oliExt.getPolicyExtension();                
        }
        polictExt.setNonStandardBillAmt(stdModePremData[0][1]);
        policy.setAnnualPaymentAmt(stdModePremData[0][2]);
    }

    /**
     * Update the XML response for non standard mode premium
     * @param response the XML response
     */
    protected void updateForNonStandardModePremiumsResponse(NbaTXLife response) {
        String[][] stdNonStdModePremData = getData(1, NbaCyberCalculationConstants.NON_STANDARD_MODE_PREMIUM_FIELDS);
        Policy policy = response.getPolicy();
        policy.setPaymentAmt(stdNonStdModePremData[0][0]);
        policy.setAnnualPaymentAmt(stdNonStdModePremData[0][1]);
    }

    /**
     * Update the XML response for commission target premium
     * @param response the XML response
     */
    protected void updateForCommissionTargetPremResponse(NbaTXLife response) {
        String[][] commTragetData = getData(1, NbaCyberCalculationConstants.COMMISSION_TARGET_FIELDS);
        Coverage covergae = response.getPrimaryCoverage();
        CoverageExtension covExt = NbaUtils.getFirstCoverageExtension(covergae);
        if (covExt == null) {
            OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVERAGE);
            covergae.addOLifEExtension(olifeExt);
            covExt = olifeExt.getCoverageExtension();
        }
        covExt.setCommTargetPrem(commTragetData[0][0]);
    }

    /**
     * Update the XML response for guideline premiums
     * @param response the XML response
     */
    protected void updateForGuidelinePremiumsResponse(NbaTXLife response) {
        String[][] guidelinePremData = getData(1, NbaCyberCalculationConstants.GUIDELINE_PREMIUM_FIELDS);
        Life life = response.getLife();
        LifeUSA lifeUSA = life.getLifeUSA();
        if (lifeUSA == null) {
            lifeUSA = new LifeUSA();
            life.setLifeUSA(lifeUSA);
        }
        lifeUSA.setGuidelineAnnPrem(guidelinePremData[0][0]);
        lifeUSA.setGuidelineSinglePrem(guidelinePremData[0][1]);
    }

    /**
     * Update the XML response for joint equal age for joint coverages
     * @param response the XML response
     */
    // SPR3290 signature changed - NbaBaseException no longer thrown
    protected void updateForJointEqualAgeResponse(NbaTXLife response) {
        int count = getNumInstances(NbaCyberCalculationConstants.ISSUE_AGE_COUNT);
        String[][] jointEqualAgeData = getData(count, NbaCyberCalculationConstants.JOINT_EQUAL_AGE_FIELDS);
        Life life = response.getLife();
        int covCount = life.getCoverageCount();
        Coverage coverage = null;
        for (int i = 0; i < covCount; i++) {
            for (int j = 0; j < count; j++) {
                coverage = life.getCoverageAt(i);
                if (NbaUtils.convertStringToInt(coverage.getCoverageKey()) == NbaUtils.convertStringToInt(jointEqualAgeData[j][0])
                        && coverage.getLivesType() == NbaOliConstants.OLI_COVLIVES_JOINTFTD) {
                    updateLifePartForJointEqualAge(coverage, jointEqualAgeData[j][2]);
                }
            }
        }
    }

    /**
     * Update the life participants for joint issue age
     * @param coverage the caoverage for which issue age will be updated
     * @param issueAge the issue age from backend
     */
    protected void updateLifePartForJointEqualAge(Coverage coverage, String issueAge){
        int count = coverage.getLifeParticipantCount();
        LifeParticipant lifepart = null;
        for(int i=0; i<count; i++){
            lifepart = coverage.getLifeParticipantAt(i);
            lifepart.setIssueAge(issueAge);
        }
    }
    
    /**
     * Updates the XML response for min no lapse (MAP) premium
     * @param response the XML response
     */
    protected void updateForMinNoLapsePremResponse(NbaTXLife response) {
        String[][] mapData = getData(1, NbaCyberCalculationConstants.MIN_NO_LAPSE_PREMIUM_FIELDS);
        Life life = response.getLife();
        life.setMinPremAmt(mapData[0][0]);
        LifeExtension lifeExt = NbaUtils.getFirstLifeExtension(life);
        if (lifeExt == null) {
            OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LIFE);
            life.addOLifEExtension(olifeExt);
            lifeExt = olifeExt.getLifeExtension();
        }
        lifeExt.setMapTargetEndDate(formatOLifEDate(mapData[0][1]));
    }

    /**
     * Update the XML response for coverages premium
     * @param response the XML response
     */
    protected void updateForCoverageResponse(NbaTXLife response) {
        int count = getNumInstances(NbaCyberCalculationConstants.COVERAGE_COUNT);
        String[][] coverageData = getData(count, NbaCyberCalculationConstants.COVERAGE_PREMIUM_FIELDS);
        Life life = response.getLife();
        int covCount = life.getCoverageCount();
        Coverage coverage = null;
        for (int i = 0; i < covCount; i++) {
            coverage = life.getCoverageAt(i);
            for (int j = 0; j < count; j++) {
                if (NbaUtils.convertStringToInt(coverage.getCoverageKey()) == NbaUtils.convertStringToInt(coverageData[j][0])) {
                    coverage.setAnnualPremAmt(coverageData[j][1]);
                    break;
                }
            }
        }
    }

    /**
     * Update the XML response for benefits premium
     * @param response the XML response
     */
    protected void updateForCovOptionResponse(NbaTXLife response) {
        if (response.isLife()) {
            updateCovOptionPremiumForLife(response.getLife());
        } else {
            updateCovOptionPremiumForAnnuity(response.getAnnuity());
        }
    }

    /**
     * Update the XML response for for benefits premium on life contract
     * @param life the life object
     */
    protected void updateCovOptionPremiumForLife(Life life) {
        int count = getNumInstances(NbaCyberCalculationConstants.COVOPTION_COUNT);
        String[][] covOptionData = getData(count, NbaCyberCalculationConstants.COVOPTION_PREMIUM_FIELDS);
        int covCount = life.getCoverageCount();
        Coverage coverage = null;
        for (int i = 0; i < covCount; i++) {
            coverage = life.getCoverageAt(i);
            for (int j = 0; j < count; j++) {
                if (NbaUtils.convertStringToInt(coverage.getCoverageKey()) == NbaUtils.convertStringToInt(covOptionData[j][0])) {
                    updateCovOptionPremium(coverage.getCovOption(), covOptionData[j]);
                }
            }
        }
    }

    /**
     * Update the XML response for for benefits premium on annuity contract
     * @param annuity the annuity object
     */
    protected void updateCovOptionPremiumForAnnuity(Annuity annuity) {
        int count = getNumInstances(NbaCyberCalculationConstants.COVOPTION_COUNT);
        String[][] covOptionData = getData(count, NbaCyberCalculationConstants.COVOPTION_PREMIUM_FIELDS);
        int riderCount = annuity.getRiderCount();
        Rider rider = null;
        for (int i = 0; i < riderCount; i++) {
            rider = annuity.getRiderAt(i);
            for (int j = 0; j < count; j++) {
                if (NbaUtils.convertStringToInt(rider.getRiderKey()) == NbaUtils.convertStringToInt(covOptionData[j][0])) {
                    updateCovOptionPremium(rider.getCovOption(), covOptionData[j]);
                }
            }
        }
    }

    /**
     * Update benefit premium
     * @param covOptions the covoption list
     * @param covOptionData the benefit data array from backend
     */
    protected void updateCovOptionPremium(List covOptions, String[] covOptionData) {
        int covOptCount = covOptions.size();
        CovOption covOption = null;
        for (int i = 0; i < covOptCount; i++) {
            covOption = (CovOption) covOptions.get(i);
            if (covOption.getProductCode().equalsIgnoreCase(covOptionData[1])) {
                covOption.setAnnualPremAmt(covOptionData[2]);
                break;
            }
        }
    }

    /**
     * Update the XML response for sub standard ratings premium
     * @param response the XML response
     * @throws NbaBaseException
     */
    protected void updateForSubRatingResponse(NbaTXLife response) throws NbaBaseException {
        int count = getNumInstances(NbaCyberCalculationConstants.SUB_STAND_COUNT);
        String[][] subRatingData = getData(count, NbaCyberCalculationConstants.SUB_STAND_PREMIUM_FIELDS);
        OLifE olife = response.getOLifE();
        Life life = response.getLife();
        int covCount = life.getCoverageCount();
        Coverage coverage = null;
        for (int i = 0; i < covCount; i++) {
            coverage = life.getCoverageAt(i);
            for (int j = 0; j < count; j++) {
                if (NbaUtils.convertStringToInt(coverage.getCoverageKey()) == NbaUtils.convertStringToInt(subRatingData[j][0])) {
                    upadteSubRatingPremium(olife, coverage, subRatingData[j]);
                }
            }
        }
    }

    /**
     * Update the XML response for sub standard ratings premium
     * @param olife the olife object
     * @param coverage the coverage object
     * @param subRatingData the sub standard rating data from backend
     * @throws NbaBaseException
     */
    protected void upadteSubRatingPremium(OLifE olife, Coverage coverage, String[] subRatingData) throws NbaBaseException {
        LifeParticipant lifeParticipant = getMatchingLifeParticipant(olife, coverage, subRatingData[1]);
        int countSubRating = lifeParticipant.getSubstandardRatingCount();
        SubstandardRating substandardRating = null;
        for (int i = 0; i < countSubRating; i++) {
            substandardRating = lifeParticipant.getSubstandardRatingAt(i);
            if (isRatingTypeMatches(substandardRating, subRatingData[2])) {
                SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
                if (substandardRatingExtension == null) {
                    OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SUBSTANDARDRATING);
                    substandardRatingExtension = olifeExt.getSubstandardRatingExtension();
                    substandardRating.addOLifEExtension(olifeExt);
                }
                substandardRatingExtension.setAnnualPremAmt(subRatingData[3]);
            }
        }
    }

    /**
     * Answers the marching life participant
     * @param olife the olife object
     * @param coverage the coverage object
     * @param backendPersonIdentity the person rolecode and refid from backend 
     * @return the matching life participant
     * @throws NbaBaseException
     */
    protected LifeParticipant getMatchingLifeParticipant(OLifE olife, Coverage coverage, String backendPersonIdentity) throws NbaBaseException {
        String backendRelOlifeValue = findOLifeCode(backendPersonIdentity.substring(0, 2), getRolesTable());
        int partCount = coverage.getLifeParticipantCount();
        LifeParticipant lifepart = null;
        Relation primRelation = null;
        for (int i = 0; i < partCount; i++) {
            lifepart = coverage.getLifeParticipantAt(i);
            primRelation = NbaUtils.getRelationForParty(lifepart.getPartyID(), olife.getRelation().toArray());
            if (primRelation.getRelationRoleCode() == NbaUtils.convertStringToLong(backendRelOlifeValue)
                    && primRelation.getRelatedRefID().equalsIgnoreCase(backendPersonIdentity.substring(2, 4))) {
                return lifepart;
            }
        }
        return null;
    }

    /**
     * Answers whether olife rating type matches with backend rating type
     * @param substandardRating the life rating
     * @param backendRatingType the backend rating
     * @return true if rating types are matches with each other.
     */
    protected boolean isRatingTypeMatches(SubstandardRating substandardRating, String backendRatingType) {
        boolean matches = false;
        int ratingType = NbaUtils.getRatingType(substandardRating);
        switch (ratingType) {
        case NbaConstants.RATING_TYPE_PERM_TABLE:
            matches = backendRatingType.charAt(0) == NbaCyberConstants.SUB_STAND_TYPE_PERM_TABLE;
            break;
        case NbaConstants.RATING_TYPE_TEMP_TABLE:
            matches = backendRatingType.charAt(0) == NbaCyberConstants.SUB_STAND_TYPE_TEMP_TABLE;
            break;
        case NbaConstants.RATING_TYPE_PERM_FLAT:
            matches = backendRatingType.charAt(0) == NbaCyberConstants.SUB_STAND_TYPE_PERM_FLAT;
            break;
        case NbaConstants.RATING_TYPE_TEMP_FLAT:
            matches = backendRatingType.charAt(0) == NbaCyberConstants.SUB_STAND_TYPE_TEMP_FLAT;
            break;
        }
        return matches;
    }
    
	/**
     * Gets the transaction results 
     * @param transResult the transaction result object
     * @return TransResult the transaction result object
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
	
	/**
	 * Parses through the host response and pulls out the requested data.
	 * @param numInstances The number of instances of segment data.
	 * @param fieldNames The host values to populate data for.
	 * @return itemArray Parsed data	
	 */
	protected String[][] getData(int numInstances, String[] fieldNames) {
        int beginIndex = 0;
        int endIndex = 0;
        int flag = 0;
        // SPR3290 code deleted
        int field = 0;
        int numFields = fieldNames.length;
        String[][] itemArray = null;
        if (numInstances > 0) {
            itemArray = new String[numInstances][numFields];
            while (field < numFields) {
                while (flag < numInstances) {
                    beginIndex = hostResponse.indexOf(fieldNames[field] + "=", beginIndex);
                    if (beginIndex == -1) {
                        flag++;
                    } else {
                        beginIndex = hostResponse.indexOf("=", beginIndex);
                        endIndex = hostResponse.indexOf(";", beginIndex);
                        itemArray[flag][field] = hostResponse.substring(beginIndex + 1, endIndex);
                        flag++;
                    }

                }
                field = field + 1;
                beginIndex = 0;
                endIndex = 0;
                flag = 0;
            }
        }
        return itemArray;
    }
	
	/**
	 * Gets the number of instances of a particular segment type
	 * @param countField The name of the segment to get a count for.
	 * @return the number of instances of a particular segment type 
	 */
	protected int getNumInstances(String countField) {
        int numInstances = 0;
        int beginIndex = hostResponse.indexOf(countField);
        if (beginIndex != -1) {
            beginIndex = hostResponse.indexOf("=", beginIndex);
            int endIndex = hostResponse.indexOf(";", beginIndex);
            numInstances = NbaUtils.convertStringToInt(hostResponse.substring(beginIndex + 1, endIndex));
        }
        return numInstances;
    }
	
	/**
	 * Format incoming date string as YYYY-MM-DD
	 * @param date Date value needed to be reformatted
	 * @return date New date value
	 */
	protected String formatOLifEDate(String date) {
		if (date.length() < 8) {
			return null;
		} else {
		    StringBuffer buffer = new StringBuffer();
		    buffer.append(date.substring(0, 4));
		    buffer.append("-");
		    buffer.append(date.substring(4, 6));
		    buffer.append("-");
		    buffer.append(date.substring(6, 8));
			return buffer.toString();
		}
	}
	
	/**
	 * Calls the translation tables for relation roles table
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param prodType the product key
	 * @return the array of NbaTableData.
	 */
	private NbaRolesRelationData[] getRolesTable() throws NbaBaseException {
	    if(relRolesTable == null){
	        HashMap aCase = new HashMap();
        aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, getCompCode());
        aCase.put(NbaTableAccessConstants.C_TABLE_NAME, NbaTableConstants.NBA_ROLES_RELATION);
        aCase.put(NbaTableAccessConstants.C_PRODUCT_TYPE, getProdType());
        aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, NbaConstants.SYST_CYBERLIFE);
        relRolesTable = (NbaRolesRelationData[]) getNtsAccess().getDisplayData(aCase, NbaTableConstants.NBA_ROLES_RELATION);
	    }
        return relRolesTable;
    }
	
	/**
	 * Find OLife code for Cyberlife value for relation roles
	 * @param cyberValue the cyberlife value to translate
	 * @param table the NbaRolesRelationData data to search through
	 * @return the translated olife code
	 */
	private String findOLifeCode(String cyberValue, NbaRolesRelationData[] table) {
        String olifeValue = null;
        if (table != null) {
            if (cyberValue != null && cyberValue.length() > 0) {
                for (int i = 0; i < table.length; i++) {
                    if (table[i].besValue != null && table[i].besValue.equalsIgnoreCase(cyberValue)) {
                        olifeValue = table[i].code();
                        break;
                    }
                }
            }
        }
        return olifeValue;
    }
	
    /**
     * Returns the company code.
     * @return the company code.
     */
    public String getCompCode() {
        return compCode;
    }
    
    /**
     * Sets company code
     * @param compCode The company code to set.
     */
    public void setCompCode(String compCode) {
        this.compCode = compCode;
    }
    
    /**
     * Returns the product type
     * @return the product type.
     */
    public String getProdType() {
        return prodType;
    }
    
    /**
     * Sets product type
     * @param prodType the product type to set.
     */
    public void setProdType(String prodType) {
        this.prodType = prodType;
    }
    
    /**
     * Returns the table accessor to access AV framework
     * @return the intsance of NbaTableAccessor.
     */
    public NbaTableAccessor getNtsAccess() {
        if(ntsAccess == null){
            ntsAccess = new NbaTableAccessor();
        }
        return ntsAccess;
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
     * @param calcType The calculation type to set.
     */
    public void setCalcType(long calcType) {
        this.calcType = calcType;
    }
    
    /**
     * Returns the host resposne
     * @return the host response.
     */
    public String getHostResponse() {
        return hostResponse;
    }
    /**
     * Sets host response
     * @param hostResponse The host response to set.
     */
    public void setHostResponse(String hostResponse) {
        this.hostResponse = hostResponse;
    }

	

}
	

