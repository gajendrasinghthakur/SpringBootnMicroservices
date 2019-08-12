
package com.csc.fsg.nba.business.rule;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group?.  The use,<BR>
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaAllowableBenefitsData;
import com.csc.fsg.nba.tableaccess.NbaAllowableRidersData;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaManualCommentType;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubAccountExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * NbaDeterminePlan gets the BasePlan, Coverages and Fund for a GenericPlan and validate the Benefits for calulated BasePlan.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaDeterminePlan {
    NbaOinkRequest oinkRequest;
	NbaOinkDataAccess aNbaOinkVO;
    NbaManualCommentType comment = new NbaManualCommentType();
	 protected static NbaLogger logger = null;

	protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaDeterminePlan.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("NbaDeterminePlan could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

	/**
     * This method is used to calculate BasePlan, Coverages, Funds and Benefits for the Generic Plan from the determineplan VP/MS model and case will mone to VPMS Error Queue if the base plan not determined correctly
     * @param nbalob the NbaLob object to be processed
     * @param nbaTXLife the NbaTXLife object to be processed
     * @throws NbaBaseException
     */
	public NbaTXLife determinePlanCode(NbaDst dst, NbaTXLife nbaTXLife) throws NbaVpmsException, NbaBaseException {
        if (nbaTXLife != null) {
            nbaTXLife = calculateBasePlan(dst, nbaTXLife, true);
            if (!("null").equals(NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy()))) {
                if (nbaTXLife.isLife()) {
                    calculateCoveragePlan(dst, nbaTXLife, true);
                }
                calculateFundPlan(dst, nbaTXLife, true);
            }
        }
        return nbaTXLife;
    }

	/**
	 * This overloaded method is called from the from the view to validate the Plans, Coverages and Benefits and do not want to add comments to DST.
	 * If base Plan not determined correctly then don't move the case to ERROR queue also do not calculate Coverage abd riders.
	 * @param dst NbaDst object used to get the NbaLob's instance
	 * @param nbaTXLife NbaTXLife object to be processed
	 * @param addComment boolean variable which decides to add the comment to dst
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	public NbaTXLife determinePlanCode(NbaDst dst, NbaTXLife nbaTXLife, boolean addComment) throws NbaVpmsException, NbaBaseException {
        if (nbaTXLife != null) {
            nbaTXLife = calculateBasePlan(dst, nbaTXLife, addComment);
            if (!("null").equals(NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy()))) {
                if (nbaTXLife.isLife()) {
                    calculateCoveragePlan(dst, nbaTXLife, addComment);
                }
                calculateFundPlan(dst, nbaTXLife, addComment);
            }
        }
        return nbaTXLife;
    }
	/**
	 * @param dst NbaDst object to be processed
	 * @param nbaTXLife NbaTXLife object to be processed
	 * @param addComment boolean variable which decides to add the comment to dst
	 * @return true if base plan is succesfully calculated else add the comments to DST and route the case to VPMSERR Queue
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	public NbaTXLife calculateBasePlan(NbaDst dst, NbaTXLife nbaTXLife, boolean addComment) throws NbaVpmsException, NbaBaseException {
        NbaLob nbalob = dst.getNbaLob();
        Map deOink = new HashMap(1);
        aNbaOinkVO = new NbaOinkDataAccess(nbalob);
        oinkRequest = new NbaOinkRequest();
        NbaVpmsAdaptor vpmsProxy = null;
        String basePlan = null;
        try {
            aNbaOinkVO.setContractSource(nbaTXLife, nbalob);
            vpmsProxy = new NbaVpmsAdaptor(aNbaOinkVO, NbaVpmsAdaptor.DETERMINE_PLAN);
            //A_Age_PINS attribute is not getting resolved by OINK in APAPPSUB process and it is a required attribute for BasePlan Processing from VPMS Model.
            //This attribute is getting calculated later on in APCTEVAL process after which it can be resolved by OINK Framework
            deOink.put("A_Age_PINS",  String.valueOf(NbaUtils.getYears(nbalob.getDOB(), new Date())));
            getLogger().logDebug("Starting retrieval of DeterminePlan from VP/MS model");
            vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_BASEPLAN);
            vpmsProxy.setANbaOinkRequest(oinkRequest);
            vpmsProxy.setSkipAttributesMap(deOink);
            NbaVpmsResultsData determinePlanData = new NbaVpmsResultsData(vpmsProxy.getResults());
            basePlan = (String) determinePlanData.getResultsData().get(0);
            if (("null").equals(basePlan)) {
				nbaTXLife.getPolicy().setProductCode(null);//to reset any previously calculated or set values
				if (nbaTXLife.getPrimaryCoverage() != null) {
					nbaTXLife.getPrimaryCoverage().setProductCode(null);//to reset any previously calculated or set values
				}
				if (addComment) {
					throw new NbaVpmsException(determinePlanData.getResult()
							.getMessage()
							+ NbaVpmsAdaptor.DETERMINE_PLAN);
				}
			} else {
				setBasePlan(dst, nbaTXLife, basePlan, addComment);
			}
            return nbaTXLife;
        } catch (java.rmi.RemoteException re) {
            throw new NbaBaseException(NbaBaseException.RMI, re, NbaExceptionType.FATAL);
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (Throwable th) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
            }
        }
    }


    /**
     * @param dst NbaDst object to be processed
	 * @param nbaTXLife NbaTXLife object to be processed and set the BasePlan in Policy and Coverage Extensions
     * @param basePlan calculated plan from the determineplan VPMS modelto set into TXLife
	 * @param addComment boolean variable which decides to add the comment to dst
     * @return
     * @throws NbaDataAccessException
     * @throws NbaBaseException
     */
    protected void setBasePlan(NbaDst dst, NbaTXLife nbaTXLife, String basePlan, boolean addComment) throws NbaBaseException {
        NbaLob nbalob = dst.getNbaLob();
        Policy policy = nbaTXLife.getPolicy();
        Coverage coverage = nbaTXLife.getPrimaryCoverage();
        NbaPlansData planData = new NbaPlansData();
        getLogger().logDebug("Completed retrieval of Base Plan from VP/MS model");
        nbalob.setPlan(basePlan);
        planData.setCompanyCode(nbalob.getCompany());
        planData.setCoverageKey(basePlan);
        planData = getPlanData(planData);
        nbalob.setProductTypSubtyp(planData.getProductType());
        if (policy != null && planData != null) {
            policy.setProductType(planData.getProductType());
            policy.setProductCode(basePlan);
            policy.setActionUpdate();
        }
        if (nbaTXLife.isLife() && planData != null && coverage != null) {
            coverage.setProductCode(basePlan);
            coverage.setLifeCovTypeCode(planData.getCoverageType());
            coverage.setActionUpdate();
            validateCovOption(dst, coverage, addComment);
        }
    }


    /**
     * @param dst NbaDst object to add comments to DST
     * @param sb StringBuffer instance contains message to be added to DST
     */
    protected void addComment(NbaDst dst, String sb) {
        NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
        npec.setActionAdd();
        npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
        npec.setText(sb.toString());
        dst.addManualComment(npec.convertToManualComment());
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Comment added: " + sb.toString());
        }
    }



    /**
     * @param planData NbaPlansData
     * @return a single row of containg Data from NBA_PLANS table
     * @throws NbaBaseException
     */
    protected NbaPlansData getPlanData(NbaPlansData planData) throws NbaBaseException {
        NbaTableAccessor nbaTable = new NbaTableAccessor();
        NbaPlansData resultPlansData = null;
        Map tblKeys = new HashMap(4);
        tblKeys.put(NbaTableAccessConstants.C_TABLE_NAME, NbaTableConstants.NBA_PLANS);
        if (planData.getCompanyCode() != null) {
            tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, planData.getCompanyCode());
        }
        tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, planData.getCoverageKey());
        resultPlansData = nbaTable.getPlanData(tblKeys);
        return resultPlansData;
    }

	/**
	 * Returns an array of allowable benefits for the calculated basePlan
	 * @param compCode CompanyCode
	 * @param backEndSysId BackEndSystenId
	 * @param coverageKey calculated basePlan from VPMS model
	 * @return
	 * @throws NbaDataAccessException
	 */
	protected NbaTableData[] getAllowableBenefitsData(String compCode, String backEndSysId, String coverageKey) throws NbaDataAccessException {
        NbaTableAccessor nta = new NbaTableAccessor();
        HashMap aCase = new HashMap(10);
        aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, compCode);
        aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, backEndSysId);
        aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, coverageKey);
        NbaTableData[] tArray = nta.getDisplayData(aCase, NbaTableConstants.NBA_ALLOWABLE_BENEFITS);
        return tArray;
	}


    /**
     * This method calculates the Coverage Plan and validate them against NBA_ALLOWABLE_BENEFITS table
     * @param dst NbaDst object to be processed
     * @param nbaTXLife NbaTXLife object to be processed
     * @param addComment boolean variable which decides to add the comment to dst
     * @throws NbaBaseException
     */
    public void calculateCoveragePlan(NbaDst dst, NbaTXLife nbaTXLife, boolean addComment) throws NbaBaseException {
        NbaLob nbalob = dst.getNbaLob();
        Map deOink = new HashMap(1);
        NbaVpmsAdaptor vpmsProxy = null;
        NbaAllowableRidersData plansRider;
        String rider = null;
        aNbaOinkVO = new NbaOinkDataAccess(nbalob);
        aNbaOinkVO.setContractSource(nbaTXLife, nbalob);
        oinkRequest = new NbaOinkRequest();
        Life life = nbaTXLife.getLife();
        try {
            int coverageCount = life.getCoverageCount();
            for (int j = 0; j < coverageCount; j++) {
                Coverage coverage = life.getCoverageAt(j);
                if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_RIDER) {
                    vpmsProxy = new NbaVpmsAdaptor(aNbaOinkVO, NbaVpmsConstants.DETERMINE_PLAN);
                    vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_COVERAGE);
                    deOink.put("A_Age_PINS", String.valueOf(NbaUtils.getYears(nbalob.getDOB(), new Date())));
                    deOink.put("A_CoverageGenericPlanCOV", NbaUtils.getFirstCoverageExtension(coverage).getGenericPlan());
                    vpmsProxy.setANbaOinkRequest(oinkRequest);
                    vpmsProxy.setSkipAttributesMap(deOink);
                    NbaVpmsResultsData determinePlanData = new NbaVpmsResultsData(vpmsProxy.getResults());
                    rider = (String) determinePlanData.getResultsData().get(0);
                    if (("null").equals(rider)) {
                        //to reset any previously calculated or set values
                        nbaTXLife.getLife().getCoverageAt(j).setProductCode(null);
                        if (addComment) {
                            addComment(dst, determinePlanData.getResult().getMessage());
                        }
                    } else {
                        coverage.setProductCode(rider);
                        coverage.setActionUpdate();
                        plansRider = (NbaAllowableRidersData) getNbaAllowableRiders(nbalob, rider);
                        if (plansRider != null) {
                            coverage.setLifeCovTypeCode(plansRider.getProductType());
                            coverage.setActionUpdate();
                        }
                        validateCovOption(dst, coverage, addComment);
                    }
                }
            }
        } catch (java.rmi.RemoteException re) {
            throw new NbaBaseException(NbaBaseException.RMI, re, NbaExceptionType.FATAL);
        } catch (NbaBaseException e) {
            e.forceFatalExceptionType();
            throw e;
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (Throwable th) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
            }
        }
    }

    /**
     * This method validates the CovOptionData against NBA_ALLOWABLE_BENEFITS table
     * @param nbalob the NbaLob object to be processed.
     * @param coverage the Coverage object to be processed
     * @throws NbaDataAccessException
     */
    protected void validateCovOption(NbaDst dst, Coverage coverage, boolean addComment) throws NbaDataAccessException {
        NbaLob nbalob = dst.getNbaLob();
        boolean allowedBenefit = true;
        StringBuffer sb = null;
        NbaAllowableBenefitsData[] benefits = (NbaAllowableBenefitsData[]) getAllowableBenefitsData(nbalob.getCompany(), NbaConstants.SYST_VANTAGE,
                nbalob.getPlan());
        int covCount = coverage.getCovOptionCount();
        CovOption covOption = null;
        for (int i = 0; i < covCount; i++) {
            covOption = coverage.getCovOptionAt(i);
            if (benefits != null) {
                for (int j = 0; j < benefits.length; j++) {
                    if (!benefits[j].getBesBenefitId().equals(covOption.getProductCode())) {
                        allowedBenefit = false;
                        break;
                    }
                }
                if (!allowedBenefit && addComment) {
                    sb = new StringBuffer();
                    sb.append("Benefit ");
                    sb.append(NbaUtils.getFirstCovOptionExtension(covOption).getGenericPlan());
                    sb.append(" not allowed for ");
                    sb.append(covOption.getProductCode());
                    sb.append(" and base plan ");
                    sb.append(nbalob.getPlan());
                    addComment(dst, sb.toString());
                }
            }
        }
    }


    /**
     * Get the NbaPlansRidersData for the current coverage.
     * @param nbalob
     * @param rider
     * @return
     * @throws NbaDataAccessException
     * @throws NbaBaseException
     */
    protected NbaTableData getNbaAllowableRiders(NbaLob nbalob, String rider) throws NbaDataAccessException, NbaBaseException {
        NbaTableAccessor tableAccessor = new NbaTableAccessor();
        Map tblKeys = tableAccessor.setupTableMap(nbalob);
        NbaTableData nbaTableData = tableAccessor.getDataForOlifeValue(tblKeys, NbaTableConstants.NBA_ALLOWABLE_RIDERS, rider);
        return nbaTableData;
    }

    /**
     * Calulates the fund for each SubAccount and set this FUND_ID to ProductId
     * @param nbalob the NbaLob object to be processed
     * @param nbaTXLife the NbaTXLife object to be processed
     * @throws NbaBaseException
     */
    public void calculateFundPlan(NbaDst dst, NbaTXLife nbaTXLife, boolean addComment) throws NbaBaseException {
        NbaLob nbalob = dst.getNbaLob();
        Map deOink = new HashMap(1);
        NbaVpmsAdaptor vpmsProxy = null;
        String fund = null;
        Investment investment = nbaTXLife.getPrimaryHolding().getInvestment();
        int subAccountCount = 0;

        aNbaOinkVO = new NbaOinkDataAccess(nbalob);
        oinkRequest = new NbaOinkRequest();
        try {
            vpmsProxy = new NbaVpmsAdaptor(aNbaOinkVO, NbaVpmsConstants.DETERMINE_PLAN);
            vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_FUND);
            aNbaOinkVO.setContractSource(nbaTXLife, nbalob);
            if (investment != null) {
                subAccountCount = investment.getSubAccountCount();
                for (int i = 0; i < subAccountCount; i++) {
                    SubAccount subAccount = investment.getSubAccountAt(i);
                    SubAccountExtension subAccountExt = NbaUtils.getFirstSubAccountExtension(subAccount);
                    deOink.put("A_Age_PINS", String.valueOf(NbaUtils.getYears(nbalob.getDOB(), new Date())));
                    deOink.put("A_SubAccountGenericPlan", subAccountExt.getGenericPlan());
                    vpmsProxy.setANbaOinkRequest(oinkRequest);
                    vpmsProxy.setSkipAttributesMap(deOink);
                    NbaVpmsResultsData determinePlanData = new NbaVpmsResultsData(vpmsProxy.getResults());
                    if (("null").equals(determinePlanData.getResultsData().get(0).toString())) {
                        subAccount.setProductCode(null); //to reset any previously calculated or set values
                        if (addComment) {
                            addComment(dst, determinePlanData.getResult().getMessage());
                        }
                    } else {
                        if (subAccount != null) {
                            fund = determinePlanData.getResultsData().get(0).toString();
                            subAccount.setProductCode(fund);
                            subAccount.setActionUpdate();
                        }
                    }
                }
            }
        } catch (NbaBaseException e) {
            e.forceFatalExceptionType();
            throw e;
        } catch (java.rmi.RemoteException re) {
            throw new NbaBaseException(NbaBaseException.RMI, re, NbaExceptionType.FATAL);
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (Throwable th) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
            }
        }
    }

    // Begin NBLXA-2181
	public void updateBenefitsAndRiders(NbaDst dst, NbaTXLife nbaTXLife) throws NbaBaseException {
		Life life = nbaTXLife.getLife();
		try {
			int coverageCount = life.getCoverageCount();
			for (int j = 0; j < coverageCount; j++) {
				Coverage coverage = life.getCoverageAt(j);
				updateCoverage(dst, coverage,nbaTXLife);
				updateCovOption(dst, coverage,nbaTXLife);
			}
		} catch (NbaBaseException e) {
			e.forceFatalExceptionType();
			throw e;
		}
	}

	protected void updateCoverage(NbaDst dst, Coverage coverage, NbaTXLife nbaTXLife) throws NbaDataAccessException {
		NbaLob nbalob = dst.getNbaLob();
		long lifecovTypeCode = coverage.getLifeCovTypeCode();
		String backend="*";
		Policy policy = nbaTXLife.getPolicy();
		if (!NbaUtils.isBlankOrNull(policy)) {
			backend=policy.getCarrierAdminSystem();
		}
		if (coverage.getIndicatorCode() == 2L) {
  		  NbaAllowableRidersData[] riders = (NbaAllowableRidersData[]) getAllowableRiderData(backend,nbalob.getCompany(), nbalob.getPlan(),
					getAppType(nbaTXLife), getSignedDate(nbaTXLife), lifecovTypeCode);
			if (riders != null) {
				for (int j = 0; j < riders.length; j++) {
					coverage.setProductCode(riders[j].getRiderCovKey());
				}
			}
		}
	}

	protected void updateCovOption(NbaDst dst, Coverage coverage, NbaTXLife nbaTXLife) throws NbaDataAccessException {
		NbaLob nbalob = dst.getNbaLob();
		int covCount = coverage.getCovOptionCount();
		long selectionrule =-1L;
		CovOption covOption = null;
		CovOptionExtension covOptionExt = null;
		for (int i = 0; i < covCount; i++) {
			covOption = coverage.getCovOptionAt(i);
			covOptionExt = NbaUtils.getFirstCovOptionExtension(covOption);
			if(covOptionExt!=null){
			selectionrule = covOptionExt.getSelectionRule();
			}
			if (selectionrule != 12 && selectionrule != 13) {
				Long lifecovTypeCode = covOption.getLifeCovOptTypeCode();
				NbaAllowableBenefitsData[] benefits = (NbaAllowableBenefitsData[]) getAllowableBenefitData(nbalob.getCompany(), nbalob.getPlan(),
						getAppType(nbaTXLife), getSignedDate(nbaTXLife), lifecovTypeCode);

				if (benefits != null) {
					for (int j = 0; j < benefits.length; j++) {
						if (benefits[j].getBesBenefitId() != covOption.getProductCode()) {
							covOption.setProductCode(benefits[j].getBesBenefitId());
						}
					}
				}
			}
			else{
				covOption.setActionDelete();
			}
		}
	}
	// End NBLXA-2181

	private String getAppType(NbaTXLife holdingInq) {
		long appType = 0;
		if (holdingInq != null) {
			appType = holdingInq.getPolicy().getApplicationInfo().getApplicationType();
		}
		return String.valueOf(appType);
	}

	private String getQualInd(NbaTXLife holdingInq) {
		long qualPlanType = 0;
		if (holdingInq != null) {
			qualPlanType = holdingInq.getLife().getQualPlanType() > 1 ? 1 : 0;
		}
		return String.valueOf(qualPlanType);
	}

	private Date getSignedDate(NbaTXLife holdingInq) {
		ApplicationInfo appin = holdingInq.getPolicy().getApplicationInfo();
		Date appSignDate = null;
		if (appin != null) {
			appSignDate = appin.getSignedDate();
		}
		return appSignDate;
	}

	protected NbaTableData[] getAllowableBenefitData(String compCode,String coverageKey, String AppType,
		Date SignedDate, long benefit_id) throws NbaDataAccessException {
		NbaTableAccessor nta = new NbaTableAccessor();
		HashMap aCase = new HashMap(7);
		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, compCode);
		aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, coverageKey);
		aCase.put(NbaTableAccessConstants.C_APP_TYPE, AppType);
		aCase.put(NbaTableAccessConstants.C_SIGNED_DATE,NbaUtils.getStringInUSFormatFromDate(SignedDate));
		aCase.put("benefitId",Long.toString(benefit_id));
		aCase.put(NbaTableAccessConstants.QUERY, "FINDBENEFITS");
		NbaTableData[] tArray = nta.getDisplayData(aCase, NbaTableConstants.NBA_ALLOWABLE_BENEFITS);
		return tArray;
	}

	protected NbaTableData[] getAllowableRiderData(String backend,String compCode,String coverageKey, String AppType,
			Date SignedDate, long lifecovtypecode) throws NbaDataAccessException {
			NbaTableAccessor nta = new NbaTableAccessor();
			HashMap aCase = new HashMap(7);
			aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, backend);
			aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, compCode);
			aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, coverageKey);
			aCase.put(NbaTableAccessConstants.C_APP_TYPE, AppType);
			aCase.put(NbaTableAccessConstants.C_SIGNED_DATE,NbaUtils.getStringInUSFormatFromDate(SignedDate));
			aCase.put("productType",Long.toString(lifecovtypecode));
			aCase.put(NbaTableAccessConstants.QUERY, "FINDRIDER");
			NbaTableData[] tArray = nta.getDisplayData(aCase, NbaTableConstants.NBA_ALLOWABLE_RIDERS);
			return tArray;
		}




}
