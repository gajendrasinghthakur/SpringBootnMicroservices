package com.csc.fsg.nba.datamanipulation;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
  
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.AgeAmtProduct;
import com.csc.fs.dataobject.accel.product.AgeAmtProductExtension;
import com.csc.fs.dataobject.accel.product.CovOptionProduct;
import com.csc.fs.dataobject.accel.product.CovOptionProductExtension;
import com.csc.fs.dataobject.accel.product.CoverageProduct;
import com.csc.fs.dataobject.accel.product.CoverageProductExtension;
import com.csc.fs.dataobject.accel.product.Fee;
import com.csc.fs.dataobject.accel.product.FeeExtension;
import com.csc.fs.dataobject.accel.product.FeeTableRef;
import com.csc.fs.dataobject.accel.product.LifeProductExtension;
import com.csc.fs.dataobject.accel.product.NonForProvision;
import com.csc.fs.dataobject.accel.product.PaymentModeMethProduct;
import com.csc.fs.dataobject.accel.product.PermTableRatingCC;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fs.dataobject.accel.product.SubstandardRisk;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProduct;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProductExtension;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.contract.validation.NbaContractValidationCommon;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;

/**
 *  NbaRetrieveContractData retrieves information from an NbaTXLife object. A static 
 *  initializer method generates a Map containing the variable names that may be used 
 *  and the Method objects used to access them. Map entries are present for all methods
 *  of the class whose method name starts with the string "retrieve" and which accept
 *  an NbaOinkRequest as an argument. This Map of variables is returned to the 
 *  NbaOinkDataAccess when the NbaTXLife source is initialized.
 *
 *  When retrieving information, all values that satisfy the variable qualifier and 
 *  filter values are retrieved from the NbaTXLife, up to the limit in the count field. 
 *  Formatting information (phone, social security, etc) is also stored for use by 
 *  the formatter. If a table is associated with the field, the table name is also 
 *  stored.	
 * <p>  
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA072</td><td>Version 3</td><td>Calculations</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA142</td><td>Version 6</td><td>Minimum Initial Premium</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>P2AXAL062</td><td>AXA Life Phase 2 R2</td><td>UWWB R2</td></tr>
 * <tr><td>CR731686</td><td>AXA Life Phase 2</td><td>Preferred Processing</td></tr>
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 *  @see com.csc.fsg.nba.datamanipulation.NbaContractDataAccess
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 * @since New Business Accelerator - Version 3 
 */
public class NbaRetrievePlanData extends NbaContractDataAccess {
	static HashMap variables = new HashMap();
	protected Map tblKeys = null; //NBA100
	//NBA104 deleted code
	private static NbaLogger logger = null;
	
	static {
		NbaRetrievePlanData aNbaRetrievePlanData = new NbaRetrievePlanData();
		String thisClassName = aNbaRetrievePlanData.getClass().getName();
		Method[] allMethods = aNbaRetrievePlanData.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("retrieve")) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 1 && parmClasses[0].getName().equals("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")) {
					Object[] args = { thisClassName, aMethod };
					variables.put(aMethodName.substring(8).toUpperCase(), args);
				}
			}
		}
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaRetrievePlanData.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaRetrievePlanData could not get a logger from the factory.");  //NBA104
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Answer a Map of the available variables. The keys to the map are the
	 * variable names. The values are an array containing the class name string
	 * and the Method to be invoked to retrieve the variable.
	 * @return methods
	 */
	public static Map getVariables() {
		return variables;
	}
	/**
	 * Retrieves the PolicyProduct information that supports the current contract.
	 */
	protected void initializePolicyProduct() throws NbaBaseException {
		//get NbaProductAccessFacade Home
		if(nbaProduct == null){//P2AXAL016
			try {		
				NbaProductAccessFacadeBean npa = new NbaProductAccessFacadeBean();  //NBA213
				nbaProduct = npa.doProductInquiry(getNbaTXLife());
				// NBA104 deleted code
				// begin NBA104
				if (getLogger().isInfoEnabled()) {
					if (nbaProduct.getErrorCode() == NbaOliConstants.TC_RESCODE_SUCCESS) {
						getLogger().logInfo("NbaRetrievePlanData: Loaded product information successfully");
					} else {
						getLogger().logInfo("NbaRetrievePlanData: Problems occurred loading product information");
						for (int i = 0; i < nbaProduct.getErrorMessageCount(); i++) {
							getLogger().logInfo(nbaProduct.getErrorMessageAt(i));
						}
					}
				}
				// end NBA104
			} catch (Exception e) {
				if (getLogger().isErrorEnabled()) {  //NBA104
					getLogger().logError("Exception thrown while initializing PolicyProduct: " + e.getMessage());  //NBA104
				}
				throw new NbaBaseException(e);
			}
		}
		
		getCommonVal().setNbaProduct(nbaProduct);//P2AXAL016
	}
	/**
	 * Obtain the value for Policy Fee Amount.
	 * OLifE.PolicyProduct.Fee.FeeAmt 
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePolicyFeeAmt(NbaOinkRequest aNbaOinkRequest) {
		getFeeAmt(aNbaOinkRequest, NbaOliConstants.OLI_FEE_POLICYFEE);  //NBA104
	}

	/**
	 * Obtain the value for MultipleOrder.
	 * OLifE.PolicyProduct.PaymentAssembly.MultipleOrder  
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMultipleOrder(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		// begin NBA104
		PolicyProduct policyProduct = getNbaProduct().getPrimaryPolicyProduct();
		if (policyProduct != null) {
			PolicyProductExtension polProdExt = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
			if (polProdExt != null && polProdExt.getPaymentAssemblyAt(0) != null) {
				aNbaOinkRequest.addValue(polProdExt.getPaymentAssemblyAt(0).getMultipleOrder());
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for Monthly Mode Factor.
	 * OLifE.PolicyProduct.PaymentModeMethProduct.ModeFactor
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMonthlyModeFactor(NbaOinkRequest aNbaOinkRequest) {
		getModeFactor(aNbaOinkRequest, NbaOliConstants.OLI_PAYMODE_MNTHLY);  //NBA104
	}

	/**
	 * Obtain the value for LoadRule1.
	 * OLifE.PolicyProduct.Fee.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveLoadRule1(NbaOinkRequest aNbaOinkRequest) {
		getFeeTableRef(aNbaOinkRequest, 21, 59);
	}

	/**
	 * Obtain the value for Quarterly Mode Factor.
	 * OLifE.PolicyProduct.PaymentModeMethProduct.ModeFactor
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveQuarterlyModeFactor(NbaOinkRequest aNbaOinkRequest) {
		getModeFactor(aNbaOinkRequest, NbaOliConstants.OLI_PAYMODE_QUARTLY);  //NBA104
	}

	/**
	 * Obtain the value for FirstRoundingRule.
	 * OLifE.PolicyProduct.PaymentAssembly.FirstRoundingRule
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveFirstRoundingRule(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		// begin NBA104
		if (getNbaProduct() != null) {
			PolicyProductExtension ppExt = AccelProduct.getFirstPolicyProductExtension(getNbaProduct().getPrimaryPolicyProduct());//NBA237
			if (ppExt != null && ppExt.getPaymentAssemblyAt(0) != null) {
				aNbaOinkRequest.addValue(ppExt.getPaymentAssemblyAt(0).getFirstRoundingRule());
			}
		}
		// end NBA104
	}

	/**
	* Obtain the value for CovBandRuleCode.
	* OLifE.PolicyProduct.LifeProduct.CoverageProduct.CovBandRuleCode
	* @param aNbaOinkRequest - data request container
	*/
	public void retrieveCovBandRuleCode(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		// begin NBA104
		if (getNbaProduct() == null  || aNbaOinkRequest == null) {
			return;
		}
		String qualifier = aNbaOinkRequest.getQualifier();
		if (qualifier.equals(BASE_COV)) {
			CoverageProduct coverageProduct = getNbaProduct().getBaseCoverageProduct();
			if (coverageProduct != null) {
				CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237 
				if (coverageProductExt != null) {
					aNbaOinkRequest.addValue(coverageProductExt.getCovBandRuleCode());
				}
			}
		} else if (qualifier.equals(NON_RIDER_COV) || qualifier.equals(RIDER)) {
			List coverages = null;
			if (qualifier.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider(); 
			}

			if (coverages != null) {
				int count = coverages.size();
				for (int i=0; i < count; i++) {
					Coverage coverage = (Coverage)coverages.get(i);
					if (coverage != null) {
						CoverageProduct coverageProduct = getNbaProduct().getCoverageProduct(coverage.getProductCode());
						if (coverageProduct != null) {
							CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237 
							if (coverageProductExt != null) {
								aNbaOinkRequest.addValue(coverageProductExt.getCovBandRuleCode());
								continue;
							}
						} else {
							if (getLogger().isErrorEnabled()) {
								StringBuffer sb = new StringBuffer("No CoverageProduct information found for contract(");
								sb.append(getPolicy().getPolNumber());
								sb.append(") - coverage productCode=");
								sb.append(coverage.getProductCode());
								getLogger().logError(sb.toString());
							}
						}
						aNbaOinkRequest.addValue(-1L);
					}
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for Collection Fee Amount.
	 * OLifE.PolicyProduct.Fee.FeeAmt 
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCollectFeeAmt(NbaOinkRequest aNbaOinkRequest) {
		getFeeAmt(aNbaOinkRequest, NbaOliConstants.OLI_FEE_COLLECTION);  //NBA104
	}

	/**
	 * Obtain the value for the Collection Commissionable Premium Calculation Indicator.
	 * OLifE.PolicyProduct.Fee.CommissionablePremCalcInd
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCollectCommissionablePremCalcInd(NbaOinkRequest aNbaOinkRequest) {
		getCommissionablePremCalcInd(aNbaOinkRequest, NbaOliConstants.OLI_FEE_COLLECTION);  //NBA104
	}

	/**
	 * Obtain the value for the Policy Commissionable Premium Calculation Indicator.
	 * OLifE.PolicyProduct.Fee.CommissionablePremCalcInd
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePolicyCommissionablePremCalcInd(NbaOinkRequest aNbaOinkRequest) {
		getCommissionablePremCalcInd(aNbaOinkRequest, NbaOliConstants.OLI_FEE_POLICYFEE);  //NBA104
	}

	/**
	 * Obtain the value for Semi-Annual Mode Factor.
	 * OLifE.PolicyProduct.PaymentModeMethProduct.ModeFactor
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSemiAnnualModeFactor(NbaOinkRequest aNbaOinkRequest) {
		getModeFactor(aNbaOinkRequest, NbaOliConstants.OLI_PAYMODE_BIANNUAL);  //NBA104
	}

	/**
	 * Obtain the value for Mode Premium Table Identity.
	 * OLifE.PolicyProduct.PaymentAssembly.ModePremTableIdentity
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA104 New Method
	public void retrieveModePremTableIdentity(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaProduct() != null) {
			PolicyProduct policyProduct = getNbaProduct().getPrimaryPolicyProduct();
			if (policyProduct != null) {
				PolicyProductExtension policyProductExt = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
				if (policyProductExt != null) {
					if (policyProductExt.getPaymentAssemblyCount() > 0) {
						aNbaOinkRequest.addValue(policyProductExt.getPaymentAssemblyAt(0).getModePremTableIdentity());
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.datamanipulation.NbaContractDataAccess#initializeObjects(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected void initializeObjects(NbaTXLife objNbaTXLife) throws NbaBaseException {
		if (objNbaTXLife == null) {
			throw new NbaBaseException("Invalid NbaTXLife");
		}
		setOLifE(objNbaTXLife);
		setNbaTXLife(objNbaTXLife);
		initializePolicyProduct();
		setUpdateMode(false);
	}

	/**
	 * Return the requested CommissionablePremCalcInd.
	 * @param aNbaOinkRequest - data request container
	 * @param feeType - fee type
	 */
	// NBA104 changed 2nd parameter
	private void getCommissionablePremCalcInd(NbaOinkRequest aNbaOinkRequest, long feeType) {
		if (getNbaProduct() != null) {  //NBA104
			Fee fee = getNbaProduct().getFee(feeType);  //NBA104
			if (fee != null) {
				aNbaOinkRequest.addValue(fee.getCommissionablePremCalcInd());
			}
		}  // NBA104
	}

	/**
	 * Return the requested Fee Amount.
	 * @param aNbaOinkRequest - data request container
	 * @param feeType - fee type
	 */
	// NBA104 changed 2nd parameter
	private void getFeeAmt(NbaOinkRequest aNbaOinkRequest, long feeType) {
		if (getNbaProduct() != null) {  //NBA104
			Fee fee = getNbaProduct().getFee(feeType);  //NBA104
			if (fee != null) {
				aNbaOinkRequest.addValue(fee.getFeeAmt());
			}
		}  // NBA104
	}

	/**
	 * Return the requested FeeTableRef. 
	 * @param aNbaOinkRequest - data request container
	 * @param feeType - fee type
	 * @param tableType - table type
	 */
	// NBA104 changed 2nd parameter
	private void getFeeTableRef(NbaOinkRequest aNbaOinkRequest, long feeType, long tableType) {
		// NBA104 deleted coded
		// begin NBA104
		if (getNbaProduct() != null) {
			FeeExtension feeExt = getNbaProduct().getFeeExtension(feeType);
			if (feeExt != null) {
				int count = feeExt.getFeeTableRefCount();
				for (int i=0; i < count; i++) {
					FeeTableRef feeTableRef = feeExt.getFeeTableRefAt(i);
					if (feeTableRef != null && feeTableRef.getTableType() == tableType) {
						aNbaOinkRequest.addValue(feeTableRef.getRuleKeyDef());
					}
				}
			}
		}
		// end NBA104
	}
	
	/**
	 * Return the requested mode factor.
	 * @param aNbaOinkRequest - data request container
	 * @param mode - payment mode
	 */
	private void getModeFactor(NbaOinkRequest aNbaOinkRequest, long mode) {
		if (getNbaProduct() != null) {  //NBA104
			PaymentModeMethProduct pmmp = getPaymentModeMethProduct(getNbaProduct().getPrimaryPolicyProduct(), mode);  //NBA104
			if (pmmp != null) {
				if (pmmp.hasModeFactor()) {
					aNbaOinkRequest.addValue(pmmp.getModeFactor());
				}
			}
		}  // NBA104
	}

	/**
	 * Returns the requested PaymentModeMethProduct based on payment mode.
	 * @param policyProduct - plan description
	 * @param mode - payment mode
	 */
	// NBA104 changed 1st parameter
	private PaymentModeMethProduct getPaymentModeMethProduct(PolicyProduct policyProduct, long mode) {
		// begin NBA104
		if (policyProduct == null) {
			return null;
		}
		// end NBA104
		int count = policyProduct.getPaymentModeMethProductCount();
		for (int i = 0; i < count; i++) {
			PaymentModeMethProduct pmmp = policyProduct.getPaymentModeMethProductAt(i);
			if (pmmp != null && pmmp.getPaymentMode() == mode) {  //NBA104
				return pmmp;
			}
		}
		return null;
	}
	/**
	 * Obtain the value for a PolicyProduct Age Calculation Type.
	 * PolicyProduct.AgeCalculationType 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	public void retrieveAgeCalculationType(NbaOinkRequest aNbaOinkRequest) {
		PolicyProduct policyprod = null;
		String policyType = aNbaOinkRequest.getQualifier();
		if (policyType.equals(BASE_COV)) {
			policyprod = getNbaProduct().getPrimaryPolicyProduct();
			if (policyprod != null && policyprod.hasAgeCalculationType()) {
				aNbaOinkRequest.addValue(policyprod.getAgeCalculationType());
			}
		}
		
	}
	/**
	 * Obtain the value for a ParticipatingType.
	 * PolicyProduct.PolicyProductExtension.ParticipatingType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method	
	public void retrieveParticipatingType(NbaOinkRequest aNbaOinkRequest) {
		long type = -1;
		if (getNbaProduct() != null) {
			PolicyProduct policyProduct;
			PolicyProductExtension policyProductExtension;
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				policyProduct = getNbaProduct().getPolicyProduct(coverage.getProductCode());
				if (policyProduct != null) {
					policyProductExtension = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
					if (policyProductExtension != null) {
						type = policyProductExtension.getParticipatingType();
					}
				}
				aNbaOinkRequest.addValue(type);
				type = -1;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(type);
		}
	}
	/**
	 * Obtain the value for a ReserveFunction.
	 * PolicyProduct.PolicyProductExtension.ReserveFunction
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method	
	public void retrieveReserveFunction(NbaOinkRequest aNbaOinkRequest) {
		long function = -1;
		if (getNbaProduct() != null) {
			PolicyProduct policyProduct;
			PolicyProductExtension policyProductExtension;
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				policyProduct = getNbaProduct().getPolicyProduct(coverage.getProductCode());
				if (policyProduct != null) {
					policyProductExtension = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
					if (policyProductExtension != null) {
						function = policyProductExtension.getReserveFunction();
					}
				}
				aNbaOinkRequest.addValue(function);
				function = -1;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(function);
		}
	}
	/**
	 * Obtain the value for a NetSinglePremIntRate.
	 * PolicyProduct.PolicyProductExtension.NonForProvision.NetSinglePremIntRate for NonForProvision.NonFortProv="6"
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method	
	public void retrieveNetSinglePremIntRate(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaProduct() != null) {
			// SPR3290 code deleted
			String qualifier = aNbaOinkRequest.getQualifier();
			if (qualifier.length() == 0 || BASE_COV.equals(qualifier)) {
				aNbaOinkRequest.addValue(getNetSinglePremIntRate(getNbaProduct().getPrimaryPolicyProduct()));
			} else if (NON_RIDER_COV.equals(qualifier) || RIDER.equals(qualifier)) {
				List coverages = getCoveragesOrRiders(aNbaOinkRequest);
				// SPR3290 code deleted
				if (coverages != null) {
					int count = coverages.size();
					for (int i = 0; i < count; i++) {
						Coverage coverage = (Coverage) coverages.get(i);
						if (coverage.hasProductCode()) {
							aNbaOinkRequest.addValue(getNetSinglePremIntRate(getNbaProduct().getPolicyProduct(coverage.getProductCode())));
						}
						if (aNbaOinkRequest.getValue().size() == i) {
							aNbaOinkRequest.addValue(0);
						}
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0.0);
		}
	}	
	/**
	 * Return the requested NetSinglePremIntRate. 
	 * @param aNbaOinkRequest - data request container
	 * @param feeType - fee type
	 * @param tableType - table type
	 */
	// NBA100 New Method
	private double getNetSinglePremIntRate(PolicyProduct policyprod) {
		double rate = 0;
		if (policyprod != null) {
			NonForProvision nonForProvision;
			PolicyProductExtension policyProductExt;
			policyProductExt = AccelProduct.getFirstPolicyProductExtension(policyprod); //NBA237
			if (policyProductExt != null) {
				for (int i = 0; i < policyProductExt.getNonForProvisionCount(); i++) {
					nonForProvision = policyProductExt.getNonForProvisionAt(i);
					if (nonForProvision.getNonFortProv() == NbaOliConstants.OLI_NONFORF_APLRPU) {
						rate = nonForProvision.getNetSinglePremIntRate();
						break;
					}
				}
			}
		}
		return rate;
	}
	/**
	 * Obtain the value for Plan Code (class and series).
	 * Holding.Policy.Life.Coverage.OLifEExtension.CoverageExtension.ValuationClassType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePlanCode(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException, NbaBaseException {
		retrievePlanCode(aNbaOinkRequest, false);
	}
	/**
	 * Obtain the value for Plan Code (class and series), translating the class code to the 
	 * value needed by VPMS.
	 * Holding.Policy.Life.Coverage.OLifEExtension.CoverageExtension.ValuationClassType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePlanCodeVPMS(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException, NbaBaseException {
		retrievePlanCode(aNbaOinkRequest, true);
	}
	/**
	 * Obtain the value for Plan Code (class and series), translating the class code to the 
	 * value needed by VPMS if needed.
	 * @param coverage the coverage 
	 * @param tranlate true if tranlation neeeded
	 */
	//NBA100 New Method	
	protected String getPlanCode(Coverage coverage, boolean translate) throws NbaDataAccessException, NbaBaseException {
		String planCode = " ";
		PolicyProduct policyProduct = getNbaProduct().getPolicyProduct(coverage.getProductCode());
		if (policyProduct != null) {
			PolicyProductExtension ppe = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
			if (ppe != null) {
				if (ppe.hasValuationClassType()) {
					planCode = Long.toString(ppe.getValuationClassType());
					if (translate) {
						if (ppe.getValuationClassType() != NbaOliConstants.OLI_VALCLASS_DECREASINGTERM) { //Not decreasing term
							NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
							NbaTableData aNbaTableData =
								nbaTableAccessor.getDataForOlifeValue(getTblKeys(), NbaTableConstants.OLI_LU_VALCLASS, planCode);
							if (nbaTableAccessor != null) {
								planCode = ((NbaUctData) aNbaTableData).getBesValue();
							}
						} else if (ppe.hasValuationClassFreq() && ppe.getValuationClassFreq() == NbaOliConstants.OLI_FREQ_MONTHLY) {
							//For decreasing term, set to 6 for monthly, leave as 7 for annual
							planCode = "6";
						}
					}
				}
				if (ppe.hasValuationBaseSeries()) {
					planCode = planCode + ppe.getValuationBaseSeries();
				}
				if (translate){
					while (planCode.length() < 4){	//pad to 4 characters
						planCode = planCode + " ";
					}
				}
				if (ppe.hasValuationSubSeries()) {
					planCode = planCode + ppe.getValuationSubSeries();
				}
			}
		}
		return planCode;
	}
	/**
	 * Answer a Map containing the key values to be used to retrieve table data.
	 * @return java.util.Map
	 */
	//NBA100 New Method	
	protected java.util.Map getTblKeys() throws NbaBaseException {
		if (tblKeys == null) {
			tblKeys = new HashMap();
			try {		
				// ACN012 code deleted		
				// SPR3290 code deleted
				NbaLob aNbalob = new NbaLob();
				aNbalob.updateLobFromNbaTxLife(getNbaTXLife());
				Map parms = NbaConfiguration.getInstance().getDatabaseSearchKeys(); //ACN012 SPR3290
				Method[] meths = aNbalob.getClass().getDeclaredMethods();
				for (int x = 0; x < meths.length; x++) {
					String memberValue = null;
					if (meths[x].getName().substring(0, 3).equalsIgnoreCase("GET")
						&& parms.containsKey(meths[x].getName().substring(3).toUpperCase())) {
						try {
							memberValue = (String) meths[x].invoke(aNbalob, null);
							if (memberValue != null)
								memberValue = memberValue.trim();
							else
								memberValue = NbaTableAccessConstants.WILDCARD;
							tblKeys.put((parms.get(meths[x].getName().substring(3).toUpperCase())), memberValue); // SPR3290
						} catch (InvocationTargetException ie) {
							throw new NbaBaseException("InvocationTargetException", ie);
						} catch (IllegalArgumentException iae) {
							throw new NbaBaseException("IllegalArgumentException", iae);
						} catch (IllegalAccessException iae) {
							throw new NbaBaseException("IllegalAccessException", iae);
						}
					}
				}
				return tblKeys;
			} catch (Exception e) {
				throw new NbaBaseException("Unable to load table keys for NbaTableAccessor ", e);
			}
		}
		return tblKeys;
	}
	/**
	 * Obtain the value for Plan Code (class and series)
	 * Holding.Policy.Life.Coverage.OLifEExtension.CoverageExtension.ValuationClassType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	protected void retrievePlanCode(NbaOinkRequest aNbaOinkRequest, boolean translate) throws NbaDataAccessException, NbaBaseException {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		Coverage coverage;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			aNbaOinkRequest.addValue(getPlanCode(coverage, translate));
		}
	}
	/**
	 * Obtain the value for ExpenseFreq
	 * PolicyProduct.Fee.FeeMode where PolicyProduct.Fee.FeeType = 8
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveExpenseFreq(NbaOinkRequest aNbaOinkRequest) {
		long mode = -1L;
		if (getNbaProduct() != null) {
			PolicyProduct policyProduct = getNbaProduct().getPrimaryPolicyProduct();
			int feeCnt = policyProduct.getFeeCount();
			Fee fee;
			for (int i = 0; i < feeCnt; i++) {
				fee = policyProduct.getFeeAt(i);
				if (fee.getFeeType() == NbaOliConstants.OLI_FEE_EXPENSE) {
					mode = fee.getFeeMode();
				}
			}
		}
		aNbaOinkRequest.addValue(mode);
	}
	/**
	 * Obtain the value for GracePeriodEntryType
	 * PolicyProduct.LapseProvision.GracePeriodEntryType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveGracePeriodEntryType(NbaOinkRequest aNbaOinkRequest) {
		long type = -1;
		if (getNbaProduct() != null) {
			PolicyProduct policyProduct = getNbaProduct().getPrimaryPolicyProduct();
			if (policyProduct != null) {
				PolicyProductExtension policyProductExtension = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
				if (policyProductExtension != null && policyProductExtension.getLapseProvisionCount() > 0) {
					type = policyProductExtension.getLapseProvisionAt(0).getGracePeriodEntryType();
				}
			}
		}
		aNbaOinkRequest.addValue(type);
	}
	/**
	 * Obtain the value for FeeTableIdentityExp - Expense Table
	 * PolicyProduct.Fee.FeeExtension.FeeTableIdentity 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveFeeTableIdentityExp(NbaOinkRequest aNbaOinkRequest) {
		retrieveFeeTableIdentity(aNbaOinkRequest, NbaOliConstants.OLI_FEE_EXPENSE);
	}
	/**
	 * Obtain the value for FeeTableIdentityPrm - Premium Load Table
	 * PolicyProduct.Fee.FeeExtension.FeeTableIdentity 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveFeeTableIdentityPrm(NbaOinkRequest aNbaOinkRequest) {
		retrieveFeeTableIdentity(aNbaOinkRequest, NbaOliConstants.OLI_FEE_PREMLOAD);
	}
	/**
	 * Obtain the value for FeeTableIdentitySur - Surrender Charge Table
	 * PolicyProduct.Fee.FeeExtension.FeeTableIdentity 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveFeeTableIdentitySur(NbaOinkRequest aNbaOinkRequest) {
		retrieveFeeTableIdentity(aNbaOinkRequest, NbaOliConstants.OLI_FEE_SURRCHG);
	}
	/**
	 * Obtain the FeeTableIdentity value for the feeType
	 * PolicyProduct.Fee.FeeExtension.FeeTableIdentity 
	 * @param aNbaOinkRequest - data request container
	 * @param feeType - 8 = Expense, 22 = Premium Load, 16 = Full Surrender
	 */
	//NBA100 New Method
	protected void retrieveFeeTableIdentity(NbaOinkRequest aNbaOinkRequest, long feeType) {
		String table = "";
		if (getNbaProduct() != null) {
			FeeExtension feeExtension = getNbaProduct().getFeeExtension(feeType);
			if (feeExtension != null) {
				table = feeExtension.getFeeTableIdentity();
			}
		}
		aNbaOinkRequest.addValue(table);
	}
	/**
	 * Obtain the value for ChargeMethodTCExp1 - Expense charge rule 1
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCExp1(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_EXPENSE, NbaOliConstants.CONTENTTYPE_PRIMARY);
	}
	/**
	 * Obtain the value for ChargeMethodTCExp2 - Expense charge rule 2
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCExp2(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_EXPENSE, NbaOliConstants.CONTENTTYPE_SECONDARY);
	}
	/**
	 * Obtain the value for ChargeMethodTCExp3 - Expense charge rule 3
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCExp3(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_EXPENSE, NbaOliConstants.CONTENTTYPE_TERTIARY);
	}
	/**
	 * Obtain the value for ChargeMethodTCPrm1 - Premium Load rule 1
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCPrm1(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_PREMLOAD, NbaOliConstants.CONTENTTYPE_PRIMARY);
	}
	/**
	 * Obtain the value for ChargeMethodTCPrm2 - Premium Load rule 2
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCPrm2(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_PREMLOAD, NbaOliConstants.CONTENTTYPE_SECONDARY);
	}
	/**
	 * Obtain the value for ChargeMethodTCPrm3 - Premium Load rule 3
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCPrm3(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_PREMLOAD, NbaOliConstants.CONTENTTYPE_TERTIARY);
	}
	/**
	 * Obtain the value for ChargeMethodTCSur1 - Surrender Charge rule 1
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCSur1(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_SURRCHG, NbaOliConstants.CONTENTTYPE_PRIMARY);
	}
	/**
	 * Obtain the value for ChargeMethodTCSur2 - Surrender Charge rule 2
	 * PolicyProduct.Fee.FeeExtension.FeeTableRef.ChargeMethodTC
	 * or PolicyProduct.Fee.FeeExtension.FeeTableRef.RuleKeyDef
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveChargeMethodTCSur2(NbaOinkRequest aNbaOinkRequest) {
		retrieveRule(aNbaOinkRequest, NbaOliConstants.OLI_FEE_SURRCHG, NbaOliConstants.CONTENTTYPE_SECONDARY);
	}
	/**
	 * Obtain the rule value for the feeType and tableType combination.  
	 * If TableRef.ChargeMethodTC = 1000500009, the rule is is an amount per 
	 * thousand and is stored in TableRef.RuleKeyDef. Otherwise the rule is
	 * in TableRef.ChargeMethodTC.
	 * @param aNbaOinkRequest - data request container
	 * @param feeType - 8 = Expense, 22 = Premium Load, 16 = Full Surrender
	 * @param tableType - 59 = first rule, 60 = second rule, 61 = third rule
	 */
	// NBA100 New Method
	protected void retrieveRule(NbaOinkRequest aNbaOinkRequest, long feeType, long tableType) {
		long rule = -1;
		if (getNbaProduct() != null) {
			FeeTableRef feeTableRef = getNbaProduct().getFeeTableRef(feeType, tableType);
			if (feeTableRef != null && feeTableRef.getTableType() == tableType) {
				if (feeType == NbaOliConstants.OLI_FEE_EXPENSE) { //Expense rules are weird
					if (feeTableRef.getChargeMethodTC() == NbaOliConstants.OLIX_CHARGERULECALCMETHOD_DEDUCTAMTK) {
						if (feeTableRef.hasRuleKeyDef()) {
							rule = Long.valueOf(feeTableRef.getRuleKeyDef()).longValue(); //rule is stored in RuleKeyDef 
						}
					} else if (feeTableRef.getChargeMethodTC() == NbaOliConstants.OLIX_CHARGERULECALCMETHOD_NOCHARGE) {
						rule = 0;
					} else if (feeTableRef.getChargeMethodTC() == NbaOliConstants.OLIX_CHARGERULECALCMETHOD_FLATAMT) {
						rule = 1;
					}
				} else {
					rule = feeTableRef.getChargeMethodTC();
				}
			}
		}
		aNbaOinkRequest.addValue(rule);
	}
	/**
	* Obtain the value for MortalityOrMorbidityTable.
	* OLifE.PolicyProduct.MortalityOrMorbidityTable
	* @param aNbaOinkRequest - data request container
	*/
	// NBA100 New Method
	public void retrieveMortalityOrMorbidityTable(NbaOinkRequest aNbaOinkRequest) {
		long table = -1;
		if (getNbaProduct() != null) {
			PolicyProduct policyProduct;
			PolicyProductExtension policyProductExtension;
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				policyProduct = getNbaProduct().getPolicyProduct(coverage.getProductCode());
				if (policyProduct != null) {
					policyProductExtension = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
					if (policyProductExtension != null) {
						table = policyProductExtension.getMortalityOrMorbidityTable();
					}
				}
				aNbaOinkRequest.addValue(table);
				table = -1;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(table);
		}
	}
	/**
	* Obtain the value for ReserveIntRate.
	* OLifE.PolicyProduct.ReserveIntRate
	* @param aNbaOinkRequest - data request container
	*/
	// NBA100 New Method
	public void retrieveReserveIntRate(NbaOinkRequest aNbaOinkRequest) {
		double rate = 0;
		if (getNbaProduct() != null) {
			PolicyProduct policyProduct;
			PolicyProductExtension policyProductExtension;
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				policyProduct = getNbaProduct().getPolicyProduct(coverage.getProductCode());
				if (policyProduct != null) {
					policyProductExtension = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
					if (policyProductExtension != null) {
						rate = policyProductExtension.getReserveIntRate();
					}
				}
				aNbaOinkRequest.addValue(rate);
				rate = 0;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(rate);
		}
	}
	/**
	 * Obtain the value for RatingCommissionRule.
	 * OLifE.PolicyProduct.LifeProduct.CoverageProduct.CoverageProductExtension.SubstandardRisk.RatingCommissionRule
	 * @param aNbaOinkRequest
	 */
	// NBA104 New Method
	public void retrieveRatingCommissionRule(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaProduct() != null) {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				SubstandardRisk substandardRisk = getNbaProduct().getSubstandardRisk(((Coverage)coverages.get(i)).getProductCode()); //NBA237
				if (substandardRisk != null) {
					aNbaOinkRequest.addValue(substandardRisk.getRatingCommissionRule());
				} else {
					aNbaOinkRequest.addValue(-1L);
				}
			}
		}
	}

	/**
	 * Obtain the value for CommissionablePremCalcInd.
	 * OLifE.PolicyProduct.LifeProduct.CoverageProduct.CovOptionProduct.CovOptionProductExtension.CommissionablePremCalcInd
	 * @param aNbaOinkRequest
	 */	
	// NBA104 New Method
	public void retrieveCommissionablePremCalcInd(NbaOinkRequest aNbaOinkRequest) {
		List covOptions = getCovOptions(aNbaOinkRequest);
		int count = covOptions.size();
		for (int i = 0; i < count; i++) {
			CovOption covOption = (CovOption) covOptions.get(i);
			if (getNbaProduct() != null) {
				CovOptionProduct covOptionProduct = getNbaProduct().getCovOptionProduct(covOption.getProductCode());
				CovOptionProductExtension covOptionProductExt = AccelProduct.getFirstCovOptionProductExtension(covOptionProduct); //NBA237
				if (covOptionProductExt != null) {
					aNbaOinkRequest.addValue(covOptionProductExt.getCommissionablePremCalcInd());
				} else {
					aNbaOinkRequest.addValue(-1L);
				}
			} else {
				aNbaOinkRequest.addValue(-1L);
			}	
		}
	}

	/**
	 * Obtain the value for GuidelinePremCalcRule.
	 * OLifE.PolicyProduct.LifeProduct.LifeProductExtension.GuidelinePremCalcRule
	 * @param aNbaOinkRequest
	 */	
	// NBA104 New Method
	public void retrieveGuidelinePremCalcRule(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaProduct() != null) {
			LifeProductExtension lifeProductExt = AccelProduct.getFirstLifeProductExtension(getNbaProduct().getLifeProduct()); //NBA237
			if (lifeProductExt != null) {
				aNbaOinkRequest.addValue(lifeProductExt.getGuidelinePremCalcRule());
			} else {
				aNbaOinkRequest.addValue(-1L);
			}
		}
	}

	/**
	 * Obtain the value for DefLifeInsMethod.
	 * OLifE.PolicyProduct.LifeProduct.LifeProductExtension.DefLifeInsMethod
	 * @param aNbaOinkRequest
	 */	
	// NBA104 New Method
	public void retrieveDefLifeInsMethod(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaProduct() != null) {
			LifeProductExtension lifeProductExt = AccelProduct.getFirstLifeProductExtension(getNbaProduct().getLifeProduct()); //NBA237
			if (lifeProductExt != null) {
				aNbaOinkRequest.addValue(lifeProductExt.getDefLifeInsMethod());
			} else {
				aNbaOinkRequest.addValue(-1L);
			}
		}
	}
	/**
	 * Obtain the value for MinPremiumInitialAmt.
	 * PolicyProduct.LifeProduct.LifeProductExtension.MinPremiumInitialAmt
	 * @param aNbaOinkRequest
	 */
	//NBA142 New Method
	public void retrieveMinPremiumInitialAmt(NbaOinkRequest aNbaOinkRequest) {
	    if (getNbaProduct() != null) {
			LifeProductExtension lifeProductExt = AccelProduct.getFirstLifeProductExtension(getNbaProduct().getLifeProduct()); //NBA237
			if (lifeProductExt != null) {
				aNbaOinkRequest.addValue(lifeProductExt.getMinPremiumInitialAmt());
			} else {
				aNbaOinkRequest.addValue(0.0);
			}
		}	    
	}
	/**
	 * Obtain the value for MinPremInitialRule.
	 * PolicyProduct.LifeProduct.LifeProductExtension.MinPremInitialRule
	 * @param aNbaOinkRequest
	 */
	//NBA142 New Method
	public void retrieveMinPremInitialRule(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaProduct() != null) {
			LifeProductExtension lifeProductExt = AccelProduct.getFirstLifeProductExtension(getNbaProduct().getLifeProduct()); //NBA237
			if (lifeProductExt != null) {
				aNbaOinkRequest.addValue(lifeProductExt.getMinPremInitialRule());
			} else {
				aNbaOinkRequest.addValue(-1L);
			}
		}
	}
	
	//P2AXAL016 new method added
	public void retrievePPFLUnderwritingClass(NbaOinkRequest aNbaOinkRequest) {
		UnderwritingClassProduct underwritingClassProduct = getUWClassProductByQualifier(aNbaOinkRequest);//P2AXAL024
		if (underwritingClassProduct != null && underwritingClassProduct.hasUnderwritingClass()) {
			aNbaOinkRequest.addValue(underwritingClassProduct.getUnderwritingClass());
		} else {
			aNbaOinkRequest.addValue(-1L);
		}
	}

	//CR731686 new method added
	public void retrievePPFLUnderwritingClassX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		Coverage coverage = getBaseCoverage();
		Party party = null;
		int partyIndex = aNbaOinkRequest.getPartyFilter();
		if (partyIndex != NbaOinkRequest.noFilterInt) {
			party = oLifE.getPartyAt(partyIndex);
		} else {
			String partyId = nbaTXLife.getPartyId(aNbaOinkRequest.getRelationRoleCode());
			NbaParty nbaParty = nbaTXLife.getParty(partyId);
			party = nbaParty.getParty();
		}

		if (party != null) {
			LifeParticipant lifeParticipant = nbaTXLife.getLifeParticipantFor(party.getId());
			Set allowedRateClasses = getCommonVal().getAllowedRateClasses(coverage, lifeParticipant);
			Iterator it = allowedRateClasses.iterator();
			while (it.hasNext()) {
				aNbaOinkRequest.addValue((String) it.next());
			}
		}
	}
	
	//P2AXAL016 new method added
	public void retrievePPFLSmokerStat(NbaOinkRequest aNbaOinkRequest) {
		UnderwritingClassProduct underwritingClassProduct = getUWClassProductByQualifier(aNbaOinkRequest);//P2AXAL024
		if (underwritingClassProduct != null && underwritingClassProduct.hasSmokerStat()) {
			aNbaOinkRequest.addValue(underwritingClassProduct.getSmokerStat());
		} else {
			aNbaOinkRequest.addValue(-1L);
		}
	}	
	
	//P2AXAL016 New method added , P2AXAL062 refactored
	public void retrievePPFLPermTableRatingCCX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List permTableRatings = new ArrayList();
		boolean flag = false;
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		LifeParticipant lifeParticipant = getLifeParticipant(aNbaOinkRequest);
		if (coverage != null && lifeParticipant != null) {
			AgeAmtProduct ageAmtProduct = getCommonVal().getAgeAmtProductFor(coverage, lifeParticipant);
			if (ageAmtProduct != null) {
				AgeAmtProductExtension ageAmtProductExt = getCommonVal().getAgeAmtProductExtension(ageAmtProduct);
				if (ageAmtProductExt != null) {
					PermTableRatingCC permTableRatingCC = ageAmtProductExt.getPermTableRatingCC();
					if (permTableRatingCC.getPermTableRatingCount() > 0) {
						permTableRatings = permTableRatingCC.getPermTableRating();
						flag = true;
					}
				}
			}
			if (!flag) {//If not found under AgeAmtProductExt in PPFL
				permTableRatings = getCommonVal().getPermTableRatingsFromJointProduct(coverage, lifeParticipant);
		}

			for (int i = 0; i < permTableRatings.size(); i++) {
				aNbaOinkRequest.addValue(String.valueOf(permTableRatings.get(i)));
	}
		}
	}
	
	//P2AXAL024 new method added
	public void retrievePPFLMaxFlatExtra(NbaOinkRequest aNbaOinkRequest) {
		UnderwritingClassProduct underwritingClassProduct = getUWClassProductByQualifier(aNbaOinkRequest);
		if (underwritingClassProduct != null) {
			UnderwritingClassProductExtension ext = getCommonVal().getUnderwritingClassProductExtensionFor(underwritingClassProduct);
			if (ext != null && ext.hasMaxFlatExtra()) {
				aNbaOinkRequest.addValue(ext.getMaxFlatExtra());
			} else {
				aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
			}
		} else {
			aNbaOinkRequest.addValue(0.0);
		}
	}
	
	//P2AXAL024 new method added
	public void retrievePPFLMaxTempFlatExtra(NbaOinkRequest aNbaOinkRequest) {
		UnderwritingClassProduct underwritingClassProduct = getUWClassProductByQualifier(aNbaOinkRequest);
		if (underwritingClassProduct != null) {
			UnderwritingClassProductExtension ext = getCommonVal().getUnderwritingClassProductExtensionFor(underwritingClassProduct);
			if (ext != null && ext.hasMaxTempFlatExtra()) {
				aNbaOinkRequest.addValue(ext.getMaxTempFlatExtra());
			} else {
				aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
			}
		} else {
			aNbaOinkRequest.addValue(0.0);
		}
	}	

	//P2AXAL024 new method added
	public void retrievePPFLFlatExtraMaxIssueAge(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
		if (coverageProduct != null) {
			CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237
			if (coverageProductExt != null && coverageProductExt.hasFlatExtraMaxIssueAge()) {
				aNbaOinkRequest.addValue(coverageProductExt.getFlatExtraMaxIssueAge());
			} else {
				aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
			}
		} else {
			aNbaOinkRequest.addValue(0);
		}
	}	
	
	//P2AXAL024 New method added 
	public void retrievePPFLMaxFlatExtraOnParent(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
		if (coverageProduct != null) {
			CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237
			if (coverageProductExt != null && coverageProductExt.hasMaxFlatExtraOnParent()) {
				aNbaOinkRequest.addValue(coverageProductExt.getMaxFlatExtraOnParent());
			} else {
				aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
			}
		} else {
			aNbaOinkRequest.addValue(0.0);
		}
	}
	
	//P2AXAL024 New method added 
	public void retrievePPFLMaxTableOnBase(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
		if (coverageProduct != null && coverageProduct.hasMaxTableOnBase()) {
			aNbaOinkRequest.addValue(coverageProduct.getMaxTableOnBase());
		} else {
			aNbaOinkRequest.addValue(NbaConstants.LONG_NULL_VALUE);//P2AXAL062
		}
	}
	
	//P2AXAL062 New method added to retrieve ALL the MaxTableOnBase for ALL the coverages on the case
	public void retrievePPFLMaxTableOnBaseX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true); 
		List coverageList = getLife().getCoverage();
		Coverage coverage = null;
		CoverageProduct coverageProduct = null;
		for (int i = 0; i < coverageList.size(); i++) {
			coverage = (Coverage) coverageList.get(i);
			coverageProduct = getCommonVal().getCoverageProductFor(coverage);
			if (coverageProduct != null && coverageProduct.hasMaxTableOnBase()) {
				aNbaOinkRequest.addValue(coverageProduct.getMaxTableOnBase());
			} else {
				aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_LONG));//ALNA640
			}
		}
	}
	
	//P2AXAL024 New method added 
	public void retrievePPFLMaxTempFlatExtraOnParent(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
		if (coverageProduct != null) {
			CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237
			if (coverageProductExt != null && coverageProductExt.hasMaxTempFlatExtraOnParent()) {
				aNbaOinkRequest.addValue(coverageProductExt.getMaxTempFlatExtraOnParent());
			} else {
				aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
			}
		} else {
			aNbaOinkRequest.addValue(0.0);
		}
	}
	
	//P2AXAL024 New method added 
	public void retrievePPfLCovOptMaxFlatExtraOnParentX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		List covOptionList = coverage.getCovOption();
		for (int i = 0; i < covOptionList.size(); i++) {
			CovOption covOption = (CovOption) covOptionList.get(i);
			if (!NbaUtils.isDeleted(covOption) && covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {
				CovOptionProduct covOptionProduct = getCommonVal().getCovOptionProductFor(coverage, covOption);
				if (covOptionProduct != null && covOptionProduct.hasMaxFlatExtraOnParent()) {
					aNbaOinkRequest.addValue(covOptionProduct.getMaxFlatExtraOnParent());
				} else {
					aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
				}
			}
		}
	}
	
	//P2AXAL024 New method added 
	public void retrievePPfLCovOptMaxTableOnParentX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		List covOptionList = coverage.getCovOption();
		CovOptionProduct covOptionProduct = null;
		for (int i = 0; i < covOptionList.size(); i++) {
			CovOption covOption = (CovOption) covOptionList.get(i);
			if (!NbaUtils.isDeleted(covOption) && covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {
				covOptionProduct = getCommonVal().getCovOptionProductFor(coverage, covOption);
				if (covOptionProduct != null && covOptionProduct.hasMaxTableOnParent()) {
					aNbaOinkRequest.addValue((double) covOptionProduct.getMaxTableOnParent());
				} else {
					aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
				}
			}
		}
	}
	
	//P2AXAL024 New method added 
	public void retrievePPfLCovOptMaxTempFlatExtraOnParentX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		List covOptionList = coverage.getCovOption();
		for (int i = 0; i < covOptionList.size(); i++) {
			CovOption covOption = (CovOption) covOptionList.get(i);
			if (!NbaUtils.isDeleted(covOption) && covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {
				CovOptionProduct covOptionProduct = getCommonVal().getCovOptionProductFor(coverage, covOption);
				CovOptionProductExtension covOptionProductExt = getCommonVal().getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExt != null && covOptionProductExt.hasMaxTempFlatExtraOnParent()) {
					aNbaOinkRequest.addValue(covOptionProductExt.getMaxTempFlatExtraOnParent());
				} else {
					aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
				}
			}
		}
	}
	
	//P2AXAL062 commented
	
//	//P2AXAL024 new method added
//	public void retrievePPFLMaxFlatExtraX(NbaOinkRequest aNbaOinkRequest) {
//		aNbaOinkRequest.setParseMultiple(true);
//		List coverageList = getLife().getCoverage();
//		Coverage coverage = null;
//		UnderwritingClassProduct underwritingClassProduct = null;
//		for (int i = 0; i < coverageList.size(); i++) {
//			coverage = (Coverage) coverageList.get(i);
//			underwritingClassProduct = getUWClassProductForCoverage(coverage);
//			if (underwritingClassProduct != null) {
//				UnderwritingClassProductExtension ext = getCommonVal().getUnderwritingClassProductExtensionFor(underwritingClassProduct);
//				if (ext != null && ext.hasMaxFlatExtra()) {
//					aNbaOinkRequest.addValue(ext.getMaxFlatExtra());
//				} else {
//					CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
//					if (coverageProduct != null && coverageProduct.hasMaxFlatExtra()) {
//						aNbaOinkRequest.addValue(coverageProduct.getMaxFlatExtra());
//					} else {
//						aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
//					}
//				}
//			} else {
//				aNbaOinkRequest.addValue(0.0);
//			}
//		}
//	}
//	
//	//P2AXAL024 new method added
//	public void retrievePPFLMaxTempFlatExtraX(NbaOinkRequest aNbaOinkRequest) {
//		aNbaOinkRequest.setParseMultiple(true);
//		List coverageList = getLife().getCoverage();
//		Coverage coverage = null;
//		UnderwritingClassProduct underwritingClassProduct = null;
//		for (int i = 0; i < coverageList.size(); i++) {
//			coverage = (Coverage) coverageList.get(i);
//			underwritingClassProduct = getUWClassProductForCoverage(coverage);
//			if (underwritingClassProduct != null) {
//				UnderwritingClassProductExtension ext = getCommonVal().getUnderwritingClassProductExtensionFor(underwritingClassProduct);
//				if (ext != null && ext.hasMaxTempFlatExtra()) {
//					aNbaOinkRequest.addValue(ext.getMaxTempFlatExtra());
//				} else {
//					CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
//					if (coverageProduct != null) {
//						CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct);
//						if (coverageProductExt != null && coverageProductExt.hasMaxTempFlatExtra()) {
//							aNbaOinkRequest.addValue(coverageProductExt.getMaxTempFlatExtra());
//						} else {
//							aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
//						}
//					}
//				}
//			} else {
//				aNbaOinkRequest.addValue(0.0);
//			}
//		}
//	}
	
	//P2AXAL024 new method added
	public void retrievePPFLMaxTableRating(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
		if (coverageProduct != null && coverageProduct.hasMaxTableRating()) {
			aNbaOinkRequest.addValue(coverageProduct.getMaxTableRating());
		} else {
			aNbaOinkRequest.addValue(Math.abs(NbaConstants.DEFAULT_DOUBLE));
		}
	}
	
	//P2AXAL024 new method added
	public void retrievePPFLMaxIssueAge(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		if (coverage != null) {
			try {
				aNbaOinkRequest.addValue(getCommonVal().getMaxIssueAge(coverage));
				return ;
			} catch (Exception ex) {}
		}
		aNbaOinkRequest.addValue(0);
	}
	
	//P2AXAL024 new method added
	public void retrievePPFLMinIssueAge(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		if (coverage != null) {
			try {
				aNbaOinkRequest.addValue(getCommonVal().getMinIssueAge(coverage));
			} catch (Exception ex) {}
		}
		aNbaOinkRequest.addValue(0);
	}
	
	//P2AXAL024 new method added
	public void retrievePPFLAgeAmtProductMinAgeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		UnderwritingClassProduct underwritingClassProduct = getUWClassProductByQualifier(aNbaOinkRequest);
		UnderwritingClassProductExtension ext = underwritingClassProduct == null ? null : getCommonVal().getUnderwritingClassProductExtensionFor(underwritingClassProduct);
		if (ext != null) {
			List ageAmtProductList = ext.getAgeAmtProduct();
			for (int i = 0; i < ageAmtProductList.size(); i++) {
				AgeAmtProduct ageAmtProduct = (AgeAmtProduct) ageAmtProductList.get(i);
				if (ageAmtProduct.hasMinAge())
					aNbaOinkRequest.addValue(ageAmtProduct.getMinAge());
				else
					aNbaOinkRequest.addValue(Integer.MIN_VALUE);
			}
		}
	}
	
	//P2AXAL024 new method added
	public void retrievePPFLAgeAmtProductMaxAgeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		UnderwritingClassProduct underwritingClassProduct = getUWClassProductByQualifier(aNbaOinkRequest);
		UnderwritingClassProductExtension ext = underwritingClassProduct == null ? null : getCommonVal().getUnderwritingClassProductExtensionFor(underwritingClassProduct);
		if (ext != null) {
			List ageAmtProductList = ext.getAgeAmtProduct();
			for (int i = 0; i < ageAmtProductList.size(); i++) {
				AgeAmtProduct ageAmtProduct = (AgeAmtProduct) ageAmtProductList.get(i);
				if (ageAmtProduct.hasMaxAge())
					aNbaOinkRequest.addValue(ageAmtProduct.getMaxAge());
				else
					aNbaOinkRequest.addValue(Integer.MAX_VALUE);
			}
		}
	}
	
	//P2AXAL024 new method added
	public void retrievePPFLAgeAmtProductAgeBasisTypeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		UnderwritingClassProduct underwritingClassProduct = getUWClassProductByQualifier(aNbaOinkRequest);
		UnderwritingClassProductExtension ext = underwritingClassProduct == null ? null : getCommonVal().getUnderwritingClassProductExtensionFor(underwritingClassProduct);
		if (ext != null) {
			List ageAmtProductList = ext.getAgeAmtProduct();
			for (int i = 0; i < ageAmtProductList.size(); i++) {
				AgeAmtProduct ageAmtProduct = (AgeAmtProduct) ageAmtProductList.get(i);
				if (ageAmtProduct.hasAgeBasisType())
					aNbaOinkRequest.addValue(ageAmtProduct.getAgeBasisType());
				else
					aNbaOinkRequest.addValue(NbaConstants.LONG_NULL_VALUE);

			}
		}
	}
	//APSL2808 new method added for Product Term
	public void retrieveProductTerm(NbaOinkRequest aNbaOinkRequest) {
		int productTerm = 0;
		Coverage coverage = getCoverage(NbaOliConstants.OLI_COVIND_BASE);
		if(coverage != null){
			CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
			if (coverageProduct != null) {
				CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237
				if (coverageProductExt != null && coverageProductExt.hasInitialPremRenewYrs()) {
					productTerm = coverageProductExt.getInitialPremRenewYrs();
					
				} 
			} 
		}
		
		aNbaOinkRequest.addValue(productTerm);
	}
	
	//APSL2808 new method added for RenPremUltAge
	public void retrieveRenPremUltAge(NbaOinkRequest aNbaOinkRequest) {	
		int issueAge = 0;
		int productTerm = 0;
		Coverage coverage = getCoverage(NbaOliConstants.OLI_COVIND_BASE);			
		if (coverage!= null) {
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			if (lifeParticipant != null && lifeParticipant.hasIssueAge()) {
				issueAge = lifeParticipant.getIssueAge();
				
			}
			CoverageProduct coverageProduct = getCommonVal().getCoverageProductFor(coverage);
			if (coverageProduct != null) {
				CoverageProductExtension coverageProductExt = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237
				if (coverageProductExt != null && coverageProductExt.hasInitialPremRenewYrs()) {
					productTerm = coverageProductExt.getInitialPremRenewYrs();
					
				} 
			} 
			int renPremUltAge = issueAge + productTerm - 1;
			aNbaOinkRequest.addValue(renPremUltAge);
		}
		
	}

  //APSL3698 new method for retriving DPW Maturity Age from PPFL.
	public void retrievePPFLDpwMaturityAge(NbaOinkRequest aNbaOinkRequest) {
		int maturityAge = 0;
		CovOptionProduct covOptionProduct = getNbaProduct().getCovOptionProduct(NbaConstants.COVOPTION_DPW);
		if (covOptionProduct != null) {
			maturityAge = covOptionProduct.getMaturityAge();
		}
		aNbaOinkRequest.addValue(maturityAge);
	}
	//APSL4759 New method added 
	public void retrievePPfLLtcsrCovOptMaxMedFlatExtraOnParent(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		List covOptionList = coverage.getCovOption();
		double maxMedFlatExtraOnParent = Math.abs(NbaConstants.DEFAULT_DOUBLE);
		for (int i = 0; i < covOptionList.size(); i++) {
			CovOption covOption = (CovOption) covOptionList.get(i);
			if (!NbaUtils.isBlankOrNull(covOption) && covOption.getLifeCovOptTypeCode()==NbaOliConstants.OLI_COVTYPE_LTCRIDER && !NbaUtils.isDeleted(covOption) && covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {//QC17385
				CovOptionProduct covOptionProduct = getCommonVal().getCovOptionProductFor(coverage, covOption);
				CovOptionProductExtension covOptionProductExt = getCommonVal().getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExt != null && covOptionProductExt.hasMaxMedFlatExtraOnParent()) {
					maxMedFlatExtraOnParent = covOptionProductExt.getMaxMedFlatExtraOnParent();
				}
				break;
			}
		}
				aNbaOinkRequest.addValue(maxMedFlatExtraOnParent);
		
	}
	// APSL4759 New method added 
	public void retrievePPfLLtcsrCovOptMaxMedTempFlatExtraOnParent(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getCoverageByQualifier(aNbaOinkRequest);
		List covOptionList = coverage.getCovOption();
		double maxMedFlatExtraOnParent = Math.abs(NbaConstants.DEFAULT_DOUBLE);
		for (int i = 0; i < covOptionList.size(); i++) {
			CovOption covOption = (CovOption) covOptionList.get(i);
			if (!NbaUtils.isBlankOrNull(covOption) && covOption.getLifeCovOptTypeCode()==NbaOliConstants.OLI_COVTYPE_LTCRIDER && !NbaUtils.isDeleted(covOption) && covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {//QC17385
				CovOptionProduct covOptionProduct = getCommonVal().getCovOptionProductFor(coverage, covOption);
				CovOptionProductExtension covOptionProductExt = getCommonVal().getCovOptionProductExtensionFor(covOptionProduct);
				if (covOptionProductExt != null && covOptionProductExt.hasMaxMedTempFlatExtraOnParent()) {
					maxMedFlatExtraOnParent = covOptionProductExt.getMaxMedFlatExtraOnParent();
				} 
				break;
			}
		}
		aNbaOinkRequest.addValue(maxMedFlatExtraOnParent);
	}
	
}
