package com.csc.fsg.nba.contract.validation;
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
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.dataobject.accel.product.AccelerationPctOption;
import com.csc.fs.dataobject.accel.product.AgeAmtProduct;
import com.csc.fs.dataobject.accel.product.AgeAmtProductExtension;
import com.csc.fs.dataobject.accel.product.AnnuityProduct;
import com.csc.fs.dataobject.accel.product.AnnuityProductExtension;
import com.csc.fs.dataobject.accel.product.ArrangementOptProduct;
import com.csc.fs.dataobject.accel.product.ArrangementProduct;
import com.csc.fs.dataobject.accel.product.BenefitLimitOptionExtension;
import com.csc.fs.dataobject.accel.product.ConflictObjectInfo;
import com.csc.fs.dataobject.accel.product.ConflictObjectInfoExtension;
import com.csc.fs.dataobject.accel.product.CovOptionProduct;
import com.csc.fs.dataobject.accel.product.CovOptionProductExtension;
import com.csc.fs.dataobject.accel.product.CoverageProduct;
import com.csc.fs.dataobject.accel.product.CoverageProductExtension;
import com.csc.fs.dataobject.accel.product.DisabilityHealthProvisions;
import com.csc.fs.dataobject.accel.product.DisabilityHealthProvisionsExtension;
import com.csc.fs.dataobject.accel.product.DistributionInfo;
import com.csc.fs.dataobject.accel.product.Dividend;
import com.csc.fs.dataobject.accel.product.FeatureOptProduct;
import com.csc.fs.dataobject.accel.product.FeatureProduct;
import com.csc.fs.dataobject.accel.product.FeeExtension;
import com.csc.fs.dataobject.accel.product.FeeTableRef;
import com.csc.fs.dataobject.accel.product.GenderProductInfo;
import com.csc.fs.dataobject.accel.product.InvestProductInfo;
import com.csc.fs.dataobject.accel.product.InvestProductInfoExtension;
import com.csc.fs.dataobject.accel.product.InvestProductInfoSysKey;
import com.csc.fs.dataobject.accel.product.JointProductUWGuideline;
import com.csc.fs.dataobject.accel.product.JurisdictionApproval;
import com.csc.fs.dataobject.accel.product.JurisdictionCC;
import com.csc.fs.dataobject.accel.product.LapseProvision;
import com.csc.fs.dataobject.accel.product.LifeProduct;
import com.csc.fs.dataobject.accel.product.LifeProductExtension;
import com.csc.fs.dataobject.accel.product.LifeUSAProduct;
import com.csc.fs.dataobject.accel.product.NBAAllowedBensRdrs;
import com.csc.fs.dataobject.accel.product.NonForProvision;
import com.csc.fs.dataobject.accel.product.Ownership;
import com.csc.fs.dataobject.accel.product.OwnershipExtension;
import com.csc.fs.dataobject.accel.product.ParticipantRoleCodeCC;
import com.csc.fs.dataobject.accel.product.ParticipantUWGuideline;
import com.csc.fs.dataobject.accel.product.PaymentModeMethProduct;
import com.csc.fs.dataobject.accel.product.PaymentModeMethProductExtension;
import com.csc.fs.dataobject.accel.product.PermTableRatingCC;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fs.dataobject.accel.product.PolicyProductInfoExtension;
import com.csc.fs.dataobject.accel.product.PremiumRate;
import com.csc.fs.dataobject.accel.product.ProductAgeInfo;
import com.csc.fs.dataobject.accel.product.QualTypeLimits;
import com.csc.fs.dataobject.accel.product.RelatedParticipantUWGuideline;
import com.csc.fs.dataobject.accel.product.SubstandardRisk;
import com.csc.fs.dataobject.accel.product.UWAgeLimits;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProduct;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProductExtension;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculationsConstants;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculatorFactory;
import com.csc.fsg.nba.contract.calculations.backend.NbaBackendContractCalculator;
import com.csc.fsg.nba.contract.calculations.backend.NbaBackendContractCalculatorFactory;
import com.csc.fsg.nba.contract.calculations.backend.NbaLife70CalculationUtil;
import com.csc.fsg.nba.contract.calculations.results.CalcProduct;
import com.csc.fsg.nba.contract.calculations.results.CalculationResult;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.contract.validator.ValidatorBase;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaObjectPrinter;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.tableaccess.NbaValidationMessageData;
import com.csc.fsg.nba.utility.NbaReflectionUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaAutoClosureContract;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.AnnuityRiderExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ArrDestination;
import com.csc.fsg.nba.vo.txlife.ArrDestinationExtension;
import com.csc.fsg.nba.vo.txlife.ArrSource;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.ArrangementExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.ContractChangeInfo;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.DisabilityHealth;
import com.csc.fsg.nba.vo.txlife.DistributionAgreementInfo;
import com.csc.fsg.nba.vo.txlife.DistributionAgreementInfoExtension;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.EndorsementExtension;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.FinancialExperience;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.FormResponseExtension;
import com.csc.fsg.nba.vo.txlife.FundingDisclosureDetails;
import com.csc.fsg.nba.vo.txlife.HHFamilyInsurance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Intent;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationExtension;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.PaymentFees;
import com.csc.fsg.nba.vo.txlife.PaymentModeAssembly;
import com.csc.fsg.nba.vo.txlife.PaymentModeMethods;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubAccountExtension;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.SuitabilityDetailsCC;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TaxWithholding;
import com.csc.fsg.nba.vo.txlife.TaxWithholdingExtension;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementDetails;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
/**
 * NbaContractValidationCommon 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1705</td><td>Version 4</td><td>Vantage Annuity validation</td></tr>
 * <tr><td>SPR1706</td><td>Version 4</td><td>Sever Errors pertaining to Plan and Rates are generated</td></tr>
 * <tr><td>SPR1747</td><td>Version 4</td><td>Correct INS filter to exclude Party and Relation objects if they are not associated with a Coverage or Rider</td></tr>  
 * <tr><td>SPR1765</td><td>Version 4</td><td>Bypass min/max edits if both the minimum and maximum values are zero.</td></tr>
 * <tr><td>SPR1669</td><td>Version 4</td><td>Getting Null Pointer Exception on Producer BF</td></tr>
 * <tr><td>SPR1707</td><td>Version 4</td><td>Severe errors are generated for Substandard Extras</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>SPR1684</td><td>Version 4</td><td>Correct Age validation edits</td></tr>
 * <tr><td>SPR1818</td><td>Version 4</td><td>OtherInsuredInd getting set when there is no Other Insured defined.</td></tr>
 * <tr><td>SPR1799</td><td>Version 4</td><td>Debit and Credit account numbers are not assigned to AccountingActivity during validation.</td></tr>
 * <tr><td>SPR1917</td><td>Version 4</td><td>Validation routines which have a CTL value of HOLD.INV are bypassed</td></tr>
 * <tr><td>SPR1956</td><td>Version 4</td><td>Set TermDate and PayUpDate for CovOptions</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR2065</td><td>Version 4</td><td>NbaContractValidationCommon.duplicateMessage assumed a message exists</td></tr>
 * <tr><td>SPR1973</td><td>Version 4</td><td>Rider & CovOption problems using CoverageParty BF</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>SPR1945</td><td>Version 4</td><td>Correct inconsistent contract validation edits for String values</td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>SPR1996</td><td>Version 4</td><td>Insurance Validation Min-Max Amts Need to Vary by Issue Age/Add Min-Max for Units/Prem Amt</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1839</td><td>Version 4</td><td>When a coverage option is rated substandard above the maximum rating factor as per the plan business rules, error message is not displayed</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Coverage</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2011</td><td>Version 4</td><td>Billing Validation P011 does not detect missing Initial net annual premium amount</td></tr>
 * <tr><td>NBA107</td><td>Version 4</td><td>Vntg Contract issue to admin</td></tr>
 * <tr><td>SPR2269</td><td>Version 4</td><td>Surrender charge calculation only required for CyberLife rule 6</td></tr>
 * <tr><td>SPR1744</td><td>Version 5</td><td>Insurance validation Process ID P088 is not set</td></tr>
 * <tr><td>SPR1806</td><td>Version 5</td><td>Error message 2054 is not generated when the number of fund allocations for the same systematic activity type are not within plan limits.</td></tr>
 * <tr><td>SPR2226</td><td>Version 5</td><td>True age is not being Calculated Correctly</td></tr>
 * <tr><td>SPR2237</td><td>Version 5</td><td>Plan Information missing error is generated for Qualified Annuities</td></tr>
 * <tr><td>SPR2103</td><td>version 5</td><td>Process ID P102 is not validating SpecialClass value against table NBA_Special Class.</td></tr>
 * <tr><td>SPR2686</td><td>Version 5</td><td>Contract Status Bar incorrectly displaying the contract status for negatively disposed cases</td></tr>
 * <tr><td>SPR2811</td><td>Version 5</td><td>Doctor information on Requirements is being inserted into the NBA Pending Database</td></tr>
 * <tr><td>SPR2986</td><td>version 6</td><td>Validation Process P154 is not validating the allocation percentage of the funds if it is less than defined in FeatureOptProduct.MinPercent.</td></tr>
 * <tr><td>SPR3043</td><td>version 6</td><td>Increase Coverage PWRateUsage and PWRateFactor Should Default from Base Coverage</td></tr>  
 * <tr><td>SPR2265</td><td>Version 6</td><td>Severe Errors In Insurance Contract Validation for Rider Product Information For Child Term Rider (CHILDTR) on Dependent On WL Plan</td></tr>
 * <tr><td>SPR2817</td><td>Version 6</td><td>Pending Accounting Needs to Be Added to nbA</td>
 * <tr><td>NBA143</td><td>version 6</td><td>Inherent benefits processing</td></tr>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>    
 * <tr><td>SPR3098</td><td>Version 6</td><td>Contract Validation Processes and Edits Should Bypass Proposed Substandard Ratings</td></tr>
 * <tr><td>NBA142</td><td>Version 6</td><td>Minimum Initial Premium</td></tr>
 * <tr><td>SPR3174</td><td>Version 6</td><td>Clean up the calculation results for NbaCalculations</td></tr>
 * <tr><td>SPR3207</td><td>Version 6</td><td>Invalid Financial Underwriting Impairment generated for Total ADB limits</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations </td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3371</td><td>Version 7</td><td>Remove CyberLife Filter from Insurance.ContractValidation.P088 to reset the Dividend Options for NonParticipating Vantage Plans</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3562</td><td>Version 8</td><td>Premium Amounts Larger than Seven Whole Numbers and Two Decimals Are Not Handled Correctly</td></tr>
 * <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card Information is not saved</td></tr>
 * <tr><td>SPR3375</td><td>Version 8</td><td>Credit Card Payment and Billing Information Not Displayed in Application Update</td></tr>
 * <tr><td>SPR2731</td><td>Version 8</td><td>Deny Person Does Not Correctly Remove All Coverages Causing an Exception on the View</td></tr>
 * <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation</td></tr>
 * <tr><td>AXAL3.7.56</td><td>AXA Life Phase 1</td><td>Calculations</td></tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr>
 * <tr><td>SPR3168</td><td>Version 8</td><td>System Messages related to application data are generated with an invalid RelatedObjectID</td></tr>
 * <tr><td>NBA234</td><td>Version 8</td><td>ACORD Transformation project</td></tr>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>ALPC119</td><td>AXA Life Phase 1</td><td>YRT Discount Accounting </td></tr>
 * <tr><td>ALPC131</td><td>AXA Life Phase 1</td><td>Express Commission Override Indicator</td></tr>
 * <tr><td>ALPC171</td><td>AXA Life Phase 1</td><td>Express Commission 95% Rule </td></tr>
 * <tr><td>ALPC066</td><td>AXA Life Phase 1</td><td>Term Series Qualified</td></tr>
 * <tr><td>ALPC075</td><td>AXA Life Phase 1</td><td>State Variations on Exclusion Riders</td></tr>
 * <tr><td>AXAL3.7.04</td><td>AXA Life Phase 1</td><td>Paid Changes</td></tr>
 * <tr><td>ALS2847</td><td>AXA Life Phase 1</td><td>QC # 1570  - End to End: Status in nbA does not match AXADistributors.com</td></tr>
 * <tr><td>ALS4938</td><td>AXA Life Phase 1</td><td>QC # 4096 - Contract Val - 3.7.40 - Delivery Receipt processed prior to Initial Payment. CV error did not generate</td></tr>
 * <tr><td>ALPC234</td><td>AXA Life Phase 1</td><td>Unbound Processing</td></tr>
 * <tr><td>ALS5872</td><td>AXA Life Phase 1</td><td>QC # 5045 - Expiration date on offer letter is incorrect</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL018</td><td>AXA Life Phase 2</td><td>Ommission Requirements</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>P2AXAL019</td><td>AXA Life Phase 2</td><td>Cash Management</td></tr>
 * <tr><td>P2AXAL027</td><td>AXA Life Phase 2</td><td>Omission and Misc Validation</td></tr>
 * <tr><td>P2AXAL016CV</td><td>Axa Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
 * <tr><td>P2AXAL021</td><td>AXA Life Phase 2</td><td>Suitability</td></tr> 
 * <tr><td>A3_AXAL002</td><td>AXA Life New App A3</td><td>Omissions and Contract Validations</td></tr>
 * <tr><td>CR56683</td><td>AXA Life Phase 2 R1 CR</td><td> Redesign CV Plus Rider</td></tr>
 * <tr><td>P2AXAL054</td><td>AXA Life Phase 2</td><td>Omissions and Contract Validations</td></tr>
 * <tr><td>A4_AXAL001</td><td>AXA Life NewApp</td><td>New Application � Application Entry A4</td></tr>
 * <tr><td>P2AXAL068</td><td>AXA Life Phase 2</td><td>Group Contract Validations</td></tr>
 * <tr><td>P2AXAL065</td><td>AXA Life Phase 2</td><td>Group Requirements</td></tr>
 * <tr><td>P2AXAL055</td><td>AXA Life Phase 2</td><td>Phase 2 Release 2 Product Validation</td></tr>
 * <tr><td>P2AXAL048</td><td>AXA Life Phase 2 R2</td><td>P2R2 ISWL CAPS Issue</td></tr>
 * <tr><td>P2AXAL062</td><td>AXA Life Phase 2 R2</td><td>UWWB R2</td></tr>
 * <tr><td>CR1346709</td><td>Discretionary</td><td>Loan Carryover Indicator</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
 * <tr><td>SR734121(APSL3172)</td><td>Discretionary</td><td>DPW State Approvals for all states except CA</td></tr>
 * <tr><td>QC18186(APSL5130)</td><td>Discretionary</td><td>Informational CV for CV-code 2763 should not display twice on case </td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 3
 */
/**
 * @author agehlot
 *
 */
/**
 * @author agehlot
 *
 */
/**
 * @author agehlot
 *
 */
public class NbaContractValidationCommon implements NbaContractValidationConstants, NbaConstants, NbaOliConstants {
	private static NbaLogger logger = null; // SPR3290
	protected static final String MMDDYYYY_DATEFORMAT = "MM/dd/yyyy";  //NBA104
	protected static final String YYYY_MM_DD_DATEFORMAT = "yyyy-MM-dd";
	protected static SimpleDateFormat mmddyyyydf;
	protected static SimpleDateFormat yyyy_mm_dd;
	protected static Map objectMethods = new HashMap();
	protected static NumberFormat pf;
	// NBA104 deleted code
	protected NbaContractValidationImpl userImpl;	//SPR1994
	protected NbaCalculation nbaCalculation;
	protected NbaCalculation docsCalculation;//ALS4450,ALS4451,ALS4452,ALS4453
	protected NbaCalculation nbaRateUtilCalc;  //NBA104
	private NbaTableAccessor ntsAccess = null;  //NBA104
	//begin NBA133
	private NbaTXLife backendCalcCoverage = null; 
	private NbaTXLife backendCalcRider = null; 
	private NbaTXLife backendCalcLifeCovOption = null;
	private NbaTXLife backendCalcRiderCovOption = null;
	private NbaTXLife backendCalcSubStandardRating = null; 
	//end NBA133
	private NbaUserVO userVO = null; //AXAL3.7.18
	NbaReflectionUtils nbaReflectionUtils = null; //NBA297
	private Map serviceMap = null; //P2AXAL068
	private final static java.lang.String PROCESS_PROBLEM = "Problem in VPMS Calculations:";//APSL2461,QC#9579
	private String ltcSuppFormNumber = null;//APSL2947, LTCSR

	static {
		NbaContractValidationCommon nbaContractValidationCommon = new NbaContractValidationCommon();
		// SPR3290 code deleted
		Method[] allMethods = nbaContractValidationCommon.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("set")) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 1 && parmClasses[0].getName().startsWith("com.csc.fsg.nba.vo.")) {
					//					Object[] args = { thisClassName, aMethod };
					objectMethods.put(aMethodName.substring(3), aMethod);
				}
			}
		}
	}
	//P2AXAL016 new method
	public NbaContractValidationCommon(){
	}

	//P2AXAL016 new method
	public NbaContractValidationCommon(NbaTXLife nbaTXLife){
		setNbaTXLife(nbaTXLife);
	}
	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationBaseImpl#getUserImplementation()
	 */
	// SPR1994 New Method
	public NbaContractValidationImpl getUserImplementation() {
		return userImpl;
	}
	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationBaseImpl#setUserImplementation(NbaContractValidationImpl)
	 */
	// SPR1994 New Method
	public void setUserImplementation(NbaContractValidationImpl userImpl) {
		this.userImpl = userImpl;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() { // NBA208-26
		// Begin SPR3290
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractValidationCommon.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractValidationCommon could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		// End SPR3290
		return logger;
	}

	//NBA103
	protected void logDebug(String msg) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(msg);
		}
	}

	//NBA103 New Method
	protected void logDebug(String msgPart1, NbaContractVO nbaContractVO) {
		if (getLogger().isDebugEnabled()) {
			String msgPart2 = getIdOf(nbaContractVO);
			getLogger().logDebug(new StringBuffer(msgPart1).append(" ").append(msgPart2));
		}
	}


	/**
	 * Returns the common object set methods.
	 * @return HashMap
	 */
	protected static Map getObjectMethods() {
		return objectMethods;
	}
	/**
	 * Return the NumberFormat for percentage.
	 * @return java.text.NumberFormat
	 */
	public static java.text.NumberFormat getPf() {
		if (pf == null) {
			pf = java.text.NumberFormat.getNumberInstance();
		};
		return pf;
	}
	protected Address address;
	protected Annuity annuity;
	protected AnnuityRiderExtension annuityRiderExtension;
	protected ApplicationInfo applicationInfo;
	protected ApplicationInfoExtension applicationInfoExtension;
	protected Arrangement arrangement;
	protected ArrDestination arrDestination;
	protected ArrSource arrSource;
	protected Banking banking;
	protected CarrierAppointment carrierAppointment;
	protected Coverage coverage;
	protected CovOption covOption;
	protected Date currentDate;
	protected Integer currentSubSet;
	protected Map filters = new HashMap();
	protected FeeExtension feeExtensionForPlan;
	protected FinancialActivity financialActivity;
	protected Holding holding;
	protected HoldingExtension holdingExtension;
	protected Investment investment;
	protected Life life;
	protected LifeExtension lifeExtension;
	protected LifeParticipant lifeParticipant;
	protected LifeUSA lifeUSA;
	protected LifeUSAExtension lifeUSAExtension;  //NBA104
	protected ValProc nbaConfigValProc;	//ACN012
	protected NbaDst nbaDst;
	protected NbaOLifEId nbaOLifEId;
	protected NbaParty nbaParty;
	protected AccelProduct nbaProduct; //NBA237
	protected NbaProductAccessFacadeBean nbaProductAccessFacade;  //NBA213
	protected NbaTableAccessor nbaTableAccessor;
	protected NbaTXLife nbaTXLife;
	protected OLifE oLifE;
	protected Organization organization;
	protected Participant participant;
	protected Party party;
	protected Payout payout;
	protected Person person;
	protected Policy policy;
	protected PolicyExtension policyExtension;
	protected PolicyProductExtension policyProductExtensionForPlan;
	protected PolicyProduct policyProductForPlan;	
	protected Map processes = new HashMap();
	protected Producer producer;
	protected Relation relation;
	protected RequirementInfo requirementInfo;
	protected Rider rider;
	protected Risk risk;
	protected Date startTime;
	protected SubAccount subAccount;
	protected SubstandardRating substandardRating;
	protected String systemId;
	protected TaxWithholding taxWithholding;
	protected Map tblKeys;
	private String nbaCalc; //NBA133
	protected SignatureInfo signatureInfo; //AXAL3.7.40
	protected FormInstance formInstance; //AXAL3.7.40
	protected boolean generateRequirement=false;//AXaL3.7.40
	protected HHFamilyInsurance hhFamilyInsurance; //AXAL3.7.40
	protected Holding bankHolding; //ALS3600
	protected FormResponse formResponse; //P2AXAL004

	protected Employment employment; //NBA297
	protected FinancialExperience financialExperience; //NBA297	
	protected Intent intent; //NBA297
	protected SystemMessage systemMessage; //NBA297	
	protected Client client; //P2AXAL021
	protected SuitabilityDetailsCC suitabilityDetailsCC; //P2AXAL021
	private Boolean isNewAppForm; //A3_AXAL002
	private Boolean isNewAppRevForm; //A3_AXAL002 (for New App Rev Form  ) 
	private Boolean isTConvForm; //A3_AXAL002
	private Boolean isAMIGVForm; //A3_AXAL002
	private Boolean isGIForm; //P2AXAL068
	private Boolean isA4A5Form; //NBLXA-2132
	protected TempInsAgreementDetails tempInsAgreementDetails;//A4_AXAL001

	/**
	 * @return Returns the tempInsAgreementDetails.
	 */
	//A4_AXAL001 new method
	public TempInsAgreementDetails getTempInsAgreementDetails() {
		return tempInsAgreementDetails;
	}
	/**
	 * @param tempInsAgreementDetails The tempInsAgreementDetails to set.
	 */
	//A4_AXAL001 new method
	public void setTempInsAgreementDetails(TempInsAgreementDetails tempInsAgreementDetails) {
		this.tempInsAgreementDetails = tempInsAgreementDetails;
	}
	/**
	 * @return Returns the generateRequirement.
	 */
	public boolean isGenerateRequirement() {
		return generateRequirement;
	}
	/**
	 * @param generateRequirement The generateRequirement to set.
	 */
	public void setGenerateRequirement(boolean generateRequirement) {
		this.generateRequirement = generateRequirement;
	}
	/**
	 * Add a message to the contract. Use the default severity for msgCode.
	 * @param msgCode the error message number
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 * @param msgSeverity the severity of the message. This overrides the default severity of msgCode if zero or greater.
	 */
	protected void addNewSystemMessage(int msgCode, String msgDescription, String msgRefId) {
		addNewSystemMessage(msgCode, msgDescription, msgRefId, -1);
	}
	/**
	 * Add a message to the contract, overriding the message severity if msgSeverity is not less than zero. 
	 * Overriding the message severity is applicable when constructing SystemMessages from SystemMessages 
	 * generated by an external process, e.g. a Web Service.
	 * @param msgCode the error message number
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 * @param msgSeverity the severity of the message. This overrides the default severity of msgCode if zero or greater.
	 */
	protected void addNewSystemMessage(int msgCode, String msgDescription, String msgRefId, long msgSeverity) {
		SystemMessage msg = getNewSystemMessage(msgCode, msgDescription, msgRefId);
		if (msgSeverity < 0) {
			msg.setMessageSeverityCode(getMsgSeverity(msg.getMessageCode()));
		} else {
			msg.setMessageSeverityCode(msgSeverity);
		}
		if (!duplicateMessage(msg)) {
			//ALS5229 If no msg-code, no need to add cryptic vpms msgs/or empty msgs
			if (msg.getMessageCode() != NbaOliConstants.OLI_TC_NULL) {
				getHolding().addSystemMessage(msg);
			}
			logNewMessage(msg);
		}
	}
	/**
	 * Add a message to the contract.
	 * @param msgCode the error message number
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 */
	protected void addNewSystemMessage(String msgCode, String msgDescription, String msgRefId) {
		addNewSystemMessage(msgCode, msgDescription, msgRefId, -1);
	}
	/**
	 * Add a message to the contract. Use the default severity for msgCode.
	 * @param msgCode the error message number
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 * @param msgSeverity the severity of the message. This overrides the default severity of msgCode.
	 */
	protected void addNewSystemMessage(String msgCode, String msgDescription, String msgRefId, long msgSeverity) {
		if (msgCode.length() > 0) {
			addNewSystemMessage(Integer.parseInt(NbaUtils.selectDigits(msgCode)), msgDescription, msgRefId, msgSeverity);
		} else {
			addNewSystemMessage(-1, msgDescription, msgRefId, 2);
		}
	}
	/**
	 * Add a plan information missing message to the contract.
	 * @param msgCode the error message number
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 */
	protected void addPlanInfoMissingMessage(String msgDescription, String msgRefId) {
		//begin ALS4095
		if (msgDescription != null) {
			msgDescription = concat("Process: ", getNbaConfigValProc().getId(), ", ", msgDescription);
		} else {
			msgDescription = concat("Process: ", getNbaConfigValProc().getId());
		}
		addNewSystemMessage(INVALID_PLAN_INFO, msgDescription, msgRefId, -1);
		//end ALS4095
	}
	/**
	 * Add a rate information missing message to the contract.
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 */
	protected void addRateInfoMissingMessage(String msgDescription, String msgRefId) {
		//begin ALS4095
		if (msgDescription != null) {
			msgDescription = concat("Process: ", getNbaConfigValProc().getId(), ", ", msgDescription);
		} else {
			msgDescription = concat("Process: ", getNbaConfigValProc().getId());
		}
		addNewSystemMessage(INVALID_RATE_INFO, msgDescription, msgRefId, -1);
		//end ALS4095
	}
	/**
	 * Add a calculation error message to the contract.
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 */
	//NBA104 New Method
	protected void addCalcErrorMessage(String msgDescription, String msgRefId) {
		//begin ALS4095
		if (msgDescription != null) {
			msgDescription = concat("Process: ", getNbaConfigValProc().getId(), ", ", msgDescription);
		} else {
			msgDescription = concat("Process: ", getNbaConfigValProc().getId());
		}
		addNewSystemMessage(getNbaConfigValProc().getMsgcode(), msgDescription, msgRefId, -1);
		//end ALS4095
	}
	/**
	 * Checks the result code from the calculation and generates the appropriate error messages.
	 * @param calculation
	 */
	//NBA104 New Method                                                
	protected void handleCalculationErrors(NbaCalculation calculation) {
		if (calculation.getCalcResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
			StringBuffer buff = new StringBuffer();
			int count = calculation.getCalculationResultCount();
			for (int i = 0; i < count; i++) {
				CalculationResult calculationResult = calculation.getCalculationResultAt(i);
				if (calculationResult.hasCalcError()) {
					buff.append(calculationResult.getCalcError().getMessage());
					buff.append(" ");
				}
				addCalcErrorMessage(buff.toString(), "");
				calculation.setCalculationResult(null);
			}
		}
	}
	/**
	 * Concatenate a String with a Date
	 * @param a - String
	 * @param b - Date
	 * @return the combined Strings
	 */
	protected String concat(String a, Date b) {
		return concat(a, NbaUtils.getStringInUSFormatFromDate(b));
	}
	/**
	 * Concatenate a two pairs of String/Date fields
	 * @param a - String
	 * @param b - Date
	 * @param c - String
	 * @param d - Date
	 * @return the combined Strings
	 */
	protected String concat(String a, Date b, String c, Date d) {
		return concat(a, NbaUtils.getStringInUSFormatFromDate(b), c, NbaUtils.getStringInUSFormatFromDate(d));
	}
	/**
	 * Concatenate a String with a double value
	 * @param a -   String
	 * @param b - double
	 * @return the combined String
	 */
	protected String concat(String a, double b) {
		StringBuffer buf = new StringBuffer();
		buf.append(a);
		buf.append(b);
		return buf.toString();
	}
	/**
	 * Concatenate 2 pairs of String/double values
	 * @param a - first String
	 * @param b - first double  
	 * @param c - second String
	 * @param d - second double 
	 * @return the combined String
	 */
	protected String concat(String a, double b, String c, double d) {
		StringBuffer buf = new StringBuffer();
		buf.append(a);
		buf.append(b);
		buf.append(c);
		buf.append(d);
		return buf.toString();
	}
	/**
	 * Concatenate 2 pairs of String/int values
	 * @param a - first String
	 * @param b - first int
	 * @param c - second String
	 * @param d - second int
	 * @return the combined String
	 */
	protected String concat(String a, int b, String c, int d) {
		StringBuffer buf = new StringBuffer();
		buf.append(a);
		buf.append(b);
		buf.append(c);
		buf.append(d);
		return buf.toString();
	}
	/**
	 * Concatenate a String with a long
	 * @param a - String
	 * @param b - long
	 * @return the combined Strings
	 */
	protected String concat(String a, long b) {
		return concat(a, Long.toString(b));
	}
	/**
	 * Concatenate two pairs of Date and long values
	 * @param a - String
	 * @param b - Date
	 * @param c - String
	 * @param d - long
	 * @return the combined Strings
	 */
	//NBA077 New Method
	protected String concat(String a, Date b, String c, long d) {
		return concat(a, NbaUtils.getStringInUSFormatFromDate(b), c, Long.toString(d));
	}
	/**
	 * Concatenate two pairs of String/long values
	 * @param a - String
	 * @param b - long
	 * @param c - String
	 * @param d - long
	 * @return the combined Strings
	 */
	protected String concat(String a, long b, String c, long d) {
		return concat(a, Long.toString(b), c, Long.toString(d));
	}
	/**
	 * Concatenate two Strings
	 * @param a - first String
	 * @param b - second String
	 * @return the combined Strings
	 */
	protected String concat(String a, String b) {
		StringBuffer buf = new StringBuffer();
		buf.append(a);
		buf.append(b);
		return buf.toString();
	}
	/**
	 * Concatenate four Strings
	 * @param a - first String
	 * @param b - second String
	 * @param c - third String
	 * @param d - fourth String
	 * @return the combined Strings
	 */
	protected String concat(String a, String b, String c, String d) {
		StringBuffer buf = new StringBuffer();
		buf.append(a);
		buf.append(b);
		buf.append(c);
		buf.append(d);
		return buf.toString();
	}
	/**
	 * Concatenate a String with a double which is to be formatted as a dollar amount
	 * @param a - String
	 * @param b - long
	 * @return the combined Strings
	 */
	protected String concatAmt(String a, double b) {
		return concat(a, NbaObjectPrinter.localeMoneyToDisplay(b));
	}
	/**
	 * Concatenate a String with a double which is to be formatted as a dollar amount
	 * @param a - String
	 * @param b - long
	 * @return the combined Strings
	 */
	protected String concatAmt(String a, double b, String c, double d) {
		return concat(a, NbaObjectPrinter.localeMoneyToDisplay(b), c, NbaObjectPrinter.localeMoneyToDisplay(d));
	}
	/**
	 * Concatenate a String with a double which is to be formatted as a percentage
	 * @param a - String
	 * @param b - long
	 * @return the combined Strings
	 */

	//NBLXA-1254  New Method
	/**
	 * Concatenate three Strings
	 * @param a - first String
	 * @param b - second String
	 * @param c - third String
	 * @return the combined Strings
	 */
	protected String concat(String a, String b,String c) {
		StringBuffer buf = new StringBuffer();
		buf.append(a);
		buf.append(b);
		buf.append(c);
		return buf.toString();
	}

	protected String concatPct(String a, double b) {
		String pct = "";
		if (Double.isNaN(b)) {
			pct = getPf().format(0);
		} else {
			pct = getPf().format(b);
		}
		pct = concat(pct, "%");
		return concat(a, pct);
	}
	/**
	 * Create a relation
	 * @return true if the LifeParticipant is a beneficiary
	 */
	protected void createRelation(Relation existingRelation, long rolecode) {
		Relation newRelation = new Relation();
		newRelation.setActionAdd();
		newRelation.setOriginatingObjectID(existingRelation.getOriginatingObjectID());
		newRelation.setRelatedObjectID(existingRelation.getRelatedObjectID());
		newRelation.setOriginatingObjectType(existingRelation.getOriginatingObjectType());
		newRelation.setRelatedObjectType(existingRelation.getRelatedObjectType());
		newRelation.setRelationRoleCode(rolecode);
		NbaUtils.setRelatedRefId(newRelation, getNbaTXLife().getOLifE().getRelation());
		getNbaOLifEId().setId(newRelation);
		getNbaTXLife().getOLifE().addRelation(newRelation);
	}
	/**
	 * Determine if a message to the contract. Ignore any errors which will be delete.
	 * Compare only the MsgCode, MsgDescription, and MsgRefId fields.
	 */
	protected boolean duplicateMessage(SystemMessage newMsg) {
		//begin SPR2065
		if (newMsg == null) {
			return true; //tell caller not to add a new message
		}
		//end SPR2065
		int systemMessageCount = getHolding().getSystemMessageCount();
		//Check content
		for (int msgIdx = 0; msgIdx < systemMessageCount; msgIdx++) {
			SystemMessage oldMsg = getHolding().getSystemMessageAt(msgIdx);
			if (! NbaUtils.isDeleted(oldMsg)) { //QC2724
				if (oldMsg.getMessageCode() == newMsg.getMessageCode()) {
					String oldMsgDescription =  oldMsg.getMessageDescription() == null ? "" : oldMsg.getMessageDescription();//SPR2065
					if (oldMsgDescription.equals(newMsg.getMessageDescription())) {  //SPR2065
						String oldMsgRelatedObjectID = oldMsg.getRelatedObjectID();  //SPR2065
						if (oldMsgRelatedObjectID != null && oldMsgRelatedObjectID.equals(newMsg.getRelatedObjectID())) {  //SPR2065
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/*
	 * Set and Edit CovOption Values 
	 */
	protected void editCovOptionValues(
			UnderwritingClassProduct underwritingClassProduct,
			CovOption covOption,
			double covAmt,
			double covUnits,
			double covVpu) {
		UnderwritingClassProductExtension underwritingClassProductExtension =
				getUnderwritingClassProductExtensionFor(underwritingClassProduct);
		int type = 0;	//SPR1706
		if (underwritingClassProductExtension != null) {	//SPR1706
			if (underwritingClassProductExtension.getMinIssueRatio() > 0 || underwritingClassProductExtension.getMaxIssueRatio() > 0) {	//SPR3207
				type = 2;
			} else if (underwritingClassProductExtension.getMinIssueUnits() > 0 || underwritingClassProductExtension.getMaxIssueUnits() > 0) {	//SPR3207
				type = 1;
			}
			//begin SPR1706
		}
		double amt = 0;
		double units = 0;
		double vpu;
		if (covOption.hasValuePerUnit()) {
			vpu = covOption.getValuePerUnit();
		} else if (covUnits > 0) {
			vpu = covVpu;
		} else {
			vpu = 1000;
		}
		//begin SPR1973
		//begin SPR3207
		if (covOption.getCovOptionPctInd() && covOption.hasOptionPct()) {
			if (covUnits > 0) {
				units = covUnits * covOption.getOptionPct();
			} else if (covAmt > 0) {
				amt = covAmt * covOption.getOptionPct();
			}
		} else if (covOption.hasOptionNumberOfUnits()) {
			//end SPR3207
			units = covOption.getOptionNumberOfUnits();
		} else if (covOption.hasOptionAmt()) {
			amt = covOption.getOptionAmt();
		} else if (covUnits > 0) {
			units = covUnits;
		} else if (covAmt > 0) {
			amt = covAmt;
		}	
		if (units == 0) {
			units = amt / vpu;
		} else {
			amt = units * vpu;
		}
		//SPR3207 code deleted
		// end SPR1973
		covOption.setOptionNumberOfUnits(units);
		covOption.setValuePerUnit(vpu);
		covOption.setOptionAmt(amt);
		covOption.setActionUpdate();
		// SPR1973 deleted code
		if (type == 2) {             
			performCovOptionRatioEdit(covOption, covAmt, covUnits, underwritingClassProductExtension, amt, units);	//SPR3207             
		} else if (type == 1) { 
			performCovOptionUnitsEdit(covOption, underwritingClassProductExtension, units);	//SPR3207  
		} else {                       
			performCovOptionAmountEdit(underwritingClassProduct, covOption, amt); 	//SPR3207                         
		}
	}
	/**
	 * Determine a Beneficiary relation exists for the coverage.
	 * @param coverageId - the coverage id
	 * @return Relation
	 */
	protected Relation findBeneficiaryToCovRelation(String coverageId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				long roleCode = relation.getRelationRoleCode();
				if (roleCode == OLI_REL_BENEFICIARY || roleCode == OLI_REL_CONTGNTBENE) {
					if (relation.getOriginatingObjectID().equals(coverageId)) {
						return relation;
					}
				}
			}
		}
		return null;
	}
	/**
	 * Return a coverage for which the insuredId is identified as an 
	 * insured LifeParticipant.
	 * @param insuredId - the insured id
	 * @param primaryInsured - boolean
	 * @return Coverage
	 */
	//SPR2731 added new parameter, priamryCoverage
	protected Coverage findCoverageForId(String insuredId, int start, boolean primaryCoverage) {
		if (getLife() != null) {
			for (int i = start; i < getLife().getCoverageCount(); i++) {
				Coverage coverage = getLife().getCoverageAt(i);
				for (int j = 0; j < coverage.getLifeParticipantCount(); j++) {
					LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(j);					
					if (insuredId != null && insuredId.equals(lifeParticipant.getPartyID())) { //SPR1945
						//begin SPR2731
						if (primaryCoverage) {
							long partRole = lifeParticipant.getLifeParticipantRoleCode();
							if (partRole == OLI_PARTICROLE_PRIMARY || partRole == OLI_PARTICROLE_OTHINSURED) {
								return coverage;
							}
						} else {
							return coverage;
						}
						//end SPR2731
					}					
				}
			}
		}
		return null;
	}
	/**
	 * Determine if the Address exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextAddress(int occurrence) {
		if (getParty() != null) {
			if (getParty().getAddressCount() > occurrence) {
				setAddress(getParty().getAddressAt(occurrence));
				logDebug(getAddress().getId() + " found");//NBA103
				return true;
			}
		}
		setAddress(null);
		return false;
	}
	/**
	 * Determine if the Annuity exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextAnnuity(int occurrence) {
		if (occurrence < 1 && getAnnuity() != null) { // Only one occurrence of Annuity
			logDebug("Annuity found");//NBA103
			return getAnnuity() != null;
		}
		setAnnuity(null);
		return false;
	}
	/**
	 * Determine if the ApplicationInfo exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextApplicationInfo(int occurrence) {
		if (occurrence < 1) { // Only one occurrence of ApplicationInfo
			logDebug("ApplicationInfo found");//NBA103
			return getApplicationInfo() != null;
		}
		setApplicationInfo(null);
		return false;
	}
	/**
	 * Determine if the Arrangement exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextArrangement(int occurrence) {
		if (getHolding() != null) {
			if (getHolding().getArrangementCount() > occurrence) {
				setArrangement(getHolding().getArrangementAt(occurrence));
				logDebug(getArrangement().getId() + " found");//NBA103
				return true;
			}
		}
		setArrangement(null);
		return false;
	}
	/**
	 * Determine if the ArrDestination exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextArrDestination(int occurrence) {
		if (getArrangement() != null) {
			if (getArrangement().getArrDestinationCount() > occurrence) {
				setArrDestination(getArrangement().getArrDestinationAt(occurrence));
				logDebug(getArrDestination().getId() +  " found");//NBA103
				return true;
			}
		}
		setArrDestination(null);
		return false;
	}
	/**
	 * Determine if the ArrSource exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextArrSource(int occurrence) {
		if (getArrangement() != null) {
			if (getArrangement().getArrSourceCount() > occurrence) {
				setArrSource(getArrangement().getArrSourceAt(occurrence));
				logDebug(getArrSource().getId() +  " found");//NBA103
				return true;
			}
		}
		setArrSource(null);
		return false;
	}
	/**
	 * Determine if the Banking exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextBanking(int occurrence) {
		if (getBankHolding() != null) { //ALS3600
			if (getBankHolding().getBankingCount() > occurrence) { //ALS3600
				setBanking(getBankHolding().getBankingAt(occurrence)); //ALS3600
				logDebug(getBanking().getId() +  " found");//NBA103
				return true;
			}
		}
		setBanking(null);
		return false;
	}
	/**
	 * Determine if the CarrierAppointment exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextCarrierAppointment(int occurrence) {
		if (getProducer() != null) {
			if (getProducer().getCarrierAppointmentCount() > occurrence) {
				setCarrierAppointment(getProducer().getCarrierAppointmentAt(occurrence));
				logDebug(getCarrierAppointment().getId() +  " found");//NBA103
				return true;
			}
		}
		setCarrierAppointment(null);
		return false;
	}
	/**
	 * Determine if the Coverage exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextCoverage(int occurrence) {
		if (getLife() != null) {
			if (getLife().getCoverageCount() > occurrence) {
				setCoverage(getLife().getCoverageAt(occurrence));
				logDebug(getCoverage().getId() +  " found");//NBA103
				return true;
			}
		}
		setCoverage(null);
		return false;
	}
	/**
	 * Determine if the CovOption exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextCovOption(int occurrence) {
		if (getCoverage() != null) {
			if (getCoverage().getCovOptionCount() > occurrence) {
				setCovOption(getCoverage().getCovOptionAt(occurrence));
				logDebug(getCovOption().getId() +  " found");//NBA103
				return true;
			}
		}
		//begin SPR1996
		if (getRider() != null) {
			if (getRider().getCovOptionCount() > occurrence) {
				setCovOption(getRider().getCovOptionAt(occurrence));
				logDebug(getCovOption().getId() +  " found");//NBA103
				return true;
			}
		}
		//end SPR1996
		setCovOption(null);
		return false;
	}
	/**
	 * Determine if the FinancialActivity exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextFinancialActivity(int occurrence) {
		if (getPolicy() != null) {
			if (getPolicy().getFinancialActivityCount() > occurrence) {
				setFinancialActivity(getPolicy().getFinancialActivityAt(occurrence));
				logDebug(getFinancialActivity().getId() +  " found");//NBA103
				return true;
			}
		}
		setFinancialActivity(null);
		return false;
	}
	/**
	 * Determine if the Holding exists.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextHolding(int occurrence) {
		if (occurrence < 1 && getHolding() != null) {
			logDebug("Holding found");//NBA103
		}
		return occurrence < 1 && getHolding() != null; // Only one occurrence of primary Holding
	}


	/**
	 * Determine if the bankHolding exists.
	 * @param occurrence the occurrence to search for
	 */
	//ALS3600 New Method
	protected boolean findNextBankHolding(int occurrence) {
		if (occurrence < 1 && getBankHolding() != null) {
			logDebug("Holding found");//NBA103
		}
		return occurrence < 1 && getBankHolding() != null; // Only one occurrence of Bank Holding
	}
	/**
	 * Determine if the Investment exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextInvestment(int occurrence) {
		if (occurrence < 1 && getInvestment() != null) {
			logDebug("Investment found");//NBA103
		}
		return occurrence < 1 && getInvestment() != null; // Only one occurrence of Investment
	}
	/**
	 * Determine if the Life exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextLife(int occurrence) {
		if (occurrence < 1 && getLife() != null) {
			logDebug("Life found");//NBA103
		}
		return occurrence < 1 && getLife() != null; // Only one occurrence of Life
	}
	/**
	 * Determine if the LifeParticipant exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextLifeParticipant(int occurrence) {
		if (getCoverage() != null) {
			if (getCoverage().getLifeParticipantCount() > occurrence) {
				setLifeParticipant(getCoverage().getLifeParticipantAt(occurrence));
				logDebug(getLifeParticipant().getId() +  " found");//NBA103
				return true;
			}
		}
		setLifeParticipant(null);
		return false;
	}
	/**
	 * Determine if the Organization exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextOrganization(int occurrence) {
		if (occurrence < 1) {
			if (getParty() != null) {
				if (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isOrganization()) {
					setOrganization(getParty().getPersonOrOrganization().getOrganization());
					logDebug(getOrganization().getId() +  " found");//NBA103
					return true;
				}
			}
		}
		setOrganization(null);
		return false;
	}
	/**
	 * Determine if the participant exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextParticipant(int occurrence) {
		if (getPayout() != null) {
			if (getPayout().getParticipantCount() > occurrence) {
				setParticipant(getPayout().getParticipantAt(occurrence));
				logDebug(getParticipant().getId() +  " found");//NBA103
				return true;
			}
		}
		if (getRider() != null) {
			if (getRider().getParticipantCount() > occurrence) {
				setParticipant(getRider().getParticipantAt(occurrence));
				logDebug(getParticipant().getId() +  " found");//NBA103
				return true;
			}
		}
		setParticipant(null);
		return false;
	}
	/**
	 * Determine if the Party exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextParty(int occurrence) {
		if (getOLifE().getPartyCount() > occurrence) {
			setParty(getOLifE().getPartyAt(occurrence));
			//SPR1669 code deleted
			logDebug(getParty().getId() +  " found");//NBA103
			return true;
		}
		setParty(null);
		return false;
	}
	/**
	 * Determine if the Payout exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextPayout(int occurrence) {
		if (getAnnuity() != null) {
			if (getAnnuity().getPayoutCount() > occurrence) {
				setPayout(getAnnuity().getPayoutAt(occurrence));
				logDebug(getPayout().getId() +  " found");//NBA103
				return true;
			}
		}
		setPayout(null);
		return false;
	}
	/**
	 * Determine if the Person exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextPerson(int occurrence) {
		if (occurrence < 1) {
			if (getParty() != null) {
				if (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson()) {
					setPerson(getParty().getPersonOrOrganization().getPerson());
					logDebug(getPerson().getId() +  " found");//NBA103
					return true;
				}
			}
		}
		setPerson(null);
		return false;
	}
	/**
	 * Determine if the Policy exists.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextPolicy(int occurrence) {
		if (occurrence < 1 && getPolicy() != null) {
			logDebug("Policy found");//NBA103
		}
		return occurrence < 1 && getPolicy() != null; // Only one occurrence of Policy
	}
	/**
	 * Determine if the Producer exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextProducer(int occurrence) {
		if (occurrence < 1 && getParty() != null) {
			if (getParty().hasProducer()) {
				setProducer(getParty().getProducer());
				logDebug(getProducer().getId() +  " found");//NBA103
				return true;
			}
		}
		setProducer(null);
		return false;
	}
	/**
	 * Determine if the Relation exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextRelation(int occurrence) {
		if (getOLifE().getRelationCount() > occurrence) {
			setRelation(getOLifE().getRelationAt(occurrence));
			logDebug(getRelation().getId() +  " found");//NBA103
			return true;
		}
		setRelation(null);
		return false;
	}
	/**
	 * Determine if the RequirementInfo exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextRequirementInfo(int occurrence) {
		if (getPolicy() != null) {
			if (getPolicy().getRequirementInfoCount() > occurrence) {
				setRequirementInfo(getPolicy().getRequirementInfoAt(occurrence));
				logDebug(getRequirementInfo().getId() +  " found");//NBA103
				return true;
			}
		}
		setRequirementInfo(null);
		return false;
	}
	/**
	 * Determine if the Rider exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextRider(int occurrence) {
		if (getAnnuity() != null) {
			if (getAnnuity().getRiderCount() > occurrence) {
				setRider(getAnnuity().getRiderAt(occurrence));
				logDebug(getRider().getId() +  " found");//NBA103
				return true;
			}
		}
		setRider(null);
		return false;
	}
	/**
	 * Determine if the Risk exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextRisk(int occurrence) {
		if (occurrence < 1 && getParty() != null) {
			if (getParty().hasRisk()) {
				setRisk(getParty().getRisk());
				logDebug(getRisk().getId() +  " found");//NBA103
				return true;
			}
		}
		setRisk(null);
		return false;
	}
	/**
	 * Determine if the SubAccount exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextSubAccount(int occurrence) {
		if (getInvestment() != null) {
			if (getInvestment().getSubAccountCount() > occurrence) {
				setSubAccount(getInvestment().getSubAccountAt(occurrence));
				logDebug(getSubAccount().getId() +  " found");//NBA103
				return true;
			}
		}
		setSubAccount(null);
		return false;
	}
	/**
	 * Determine if the SubstandardRating exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextSubstandardRating(int occurrence) {
		if (getLifeParticipant() != null) {
			if (getLifeParticipant().getSubstandardRatingCount() > occurrence) {
				setSubstandardRating(getLifeParticipant().getSubstandardRatingAt(occurrence));
				logDebug(getSubstandardRating().getId() +  " found");//NBA103
				return true;
			}
		}
		if (getCovOption() != null) {
			if (getCovOption().getSubstandardRatingCount() > occurrence) {
				setSubstandardRating(getCovOption().getSubstandardRatingAt(occurrence));
				logDebug(getSubstandardRating().getId() +  " found");//NBA103
				return true;
			}
		}
		setSubstandardRating(null);
		return false;
	}
	/**
	 * Determine if the TaxWithholding exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findNextTaxWithholding(int occurrence) {
		if (getArrangement() != null) {
			if (getArrangement().getTaxWithholdingCount() > occurrence) {
				setTaxWithholding(getArrangement().getTaxWithholdingAt(occurrence));
				logDebug(getTaxWithholding().getId() +  " found");//NBA103
				return true;
			}
		}
		setTaxWithholding(null);
		return false;
	}
	/**
	 * Determine if the participant exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	protected boolean findParty(String partyID) {
		setNbaParty(getNbaTXLife().getParty(partyID));
		setParty(null);
		if (getNbaParty() != null && !getNbaParty().getParty().isActionDelete()) {
			setParty(getNbaParty().getParty());
		}
		return getParty() != null;	//SPR1747 
	}
	/**
	 * Returns the address.
	 * @return Address
	 */
	protected Address getAddress() {
		return address;
	}
	/**
	 * Return the Payor relation if it exists.
	 * @return Relation
	 */
	protected Relation getAnnuitantRelation() {
		return getRelation(OLI_REL_ANNUITANT);
	}
	//NBLXA-1254 New Method
	/**
	 * Return the Ctrl Person relation if it exists.
	 * @return Relation
	 */
	protected Relation getControllingPersonRelation(String relationId) {
		return getRelation(OLI_REL_CONTROLLINGPERSON,relationId);
	}

	//NBLXA-1254 New Method
	/**
	 * Return the Beneficiary Owner relation if it exists.
	 * @return Relation
	 */
	protected Relation getBenOwnerRelation(String relationId) {
		return getRelation(OLI_REL_BENEFICIALOWNER,relationId);
	}

	//NBLXA-1254 New Method
	/**
	 * Return the Auth Person relation if it exists.
	 * @return Relation
	 */
	protected Relation getAuthorizedPersonRelation(String relationId) {
		return getRelation(OLI_REL_AUTHORIZEDPERSON,relationId);
	}

	//NBLXA-1254 New Method
	/**
	 * Return the trustee relation if it exists.
	 * @return Relation
	 */
	protected Relation getTrusteeRelation(String relationId) {
		return getRelation(OLI_REL_TRUSTEE,relationId);
	}
	/**
	 * Returns the Annuity.
	 * @return Annuity
	 */
	protected Annuity getAnnuity() {
		if (annuity == null) {
			if (getNbaTXLife().isAnnuity()) {
				setAnnuity(getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity());
			}
		}
		return annuity;
	}
	/**
	 * Returns the AnnuityExtension. Create a new AnnuityExtension if necessary. 
	 * @return AnnuityExtension
	 */
	protected AnnuityExtension getAnnuityExtension() {
		AnnuityExtension annuityExtension = NbaUtils.getFirstAnnuityExtension(getAnnuity());
		if (annuityExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_ANNUITY);
			oLifEExtension.setActionAdd();
			getAnnuity().addOLifEExtension(oLifEExtension);
			annuityExtension = NbaUtils.getFirstAnnuityExtension(getAnnuity());
		}
		return annuityExtension;
	}
	/**
	 * Return the AnnuityProductExtension for an AnnuityProduct.
	 * @return the AnnuityProductExtension
	 */
	protected AnnuityProductExtension getAnnuityProductExtension() {
		AnnuityProductExtension annuityProductExtension = null;
		if (getPolicyProductForPlan() != null) {
			AnnuityProduct annuityProduct = getAnnuityProductFor(getPolicyProductForPlan());
			if (annuityProduct != null) {
				annuityProductExtension = getAnnuityProductExtensionFor(annuityProduct);
			}
		}
		return annuityProductExtension;
	}
	/**
	 * Return the AnnuityProductExtension for a AnnuityProduct.
	 * @param AnnuityProduct the AnnuityProduct
	 * @return the AnnuityProductExtension
	 */
	protected AnnuityProductExtension getAnnuityProductExtensionFor(AnnuityProduct annuityProduct) {
		AnnuityProductExtension annuityProductExtension = null;
		int extCount = annuityProduct.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			com.csc.fs.dataobject.accel.product.OLifEExtension extension = annuityProduct.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isAnnuityProductExtension()) {
					annuityProductExtension = extension.getAnnuityProductExtension();
				}
			}
		}
		return annuityProductExtension;
	}
	/**
	 * Return the AnnuityProduct for a PolicyProduct.
	 * @param policyProduct the PolicyProduct
	 * @return the AnnuityProduct
	 */
	protected AnnuityProduct getAnnuityProductFor(PolicyProduct policyProduct) {
		AnnuityProduct annuityProduct = null;
		if (policyProduct.hasLifeProductOrAnnuityProduct() && policyProduct.getLifeProductOrAnnuityProduct().isAnnuityProduct()) {
			annuityProduct = policyProduct.getLifeProductOrAnnuityProduct().getAnnuityProduct();
		}
		return annuityProduct;
	}
	/**
	 * Return the AnnuityProduct for the plan PolicyProduct.
	 * @return the AnnuityProduct
	 */
	protected AnnuityProduct getAnnuityProductForPlan() {
		AnnuityProduct annuityProduct = null;
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			annuityProduct = getAnnuityProductFor(policyProduct);
		}
		return annuityProduct;
	}
	/**
	 * Returns the annuityRiderExtension. Create a new AnnuityRiderExtension if necessary. 
	 * @return AnnuityRiderExtension
	 */
	protected AnnuityRiderExtension getAnnuityRiderExtension() {
		if (annuityRiderExtension == null) {
			annuityRiderExtension = NbaUtils.getFirstAnnuityRiderExtension(getRider());
			if (annuityRiderExtension == null) {
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_ANNUITYRIDER);
				oLifEExtension.setActionAdd();
				getRider().addOLifEExtension(oLifEExtension);
				annuityRiderExtension = NbaUtils.getFirstAnnuityRiderExtension(getRider());
			}
		}
		return annuityRiderExtension;
	}
	/**
	 * Returns the applicationInfo.
	 * @return ApplicationInfo
	 */
	protected ApplicationInfo getApplicationInfo() {
		if (applicationInfo == null) {
			setApplicationInfo(getPolicy().getApplicationInfo());
		}
		return applicationInfo;
	}
	/**
	 * Returns the applicationInfoExtension.
	 * @return ApplicationInfoExtension
	 */
	protected ApplicationInfoExtension getApplicationInfoExtension() {
		//Begin ALS5363
		if (applicationInfoExtension == null) {
			applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
			if (applicationInfoExtension == null) {
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_APPLICATIONINFO);
				oLifEExtension.setActionAdd();
				getApplicationInfo().addOLifEExtension(oLifEExtension);
				applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
			}
		}
		//End ALS5363
		return applicationInfoExtension;
	}
	/**
	 * Returns the arrangement.
	 * @return Arrangement
	 */
	protected Arrangement getArrangement() {
		return arrangement;
	}
	/**
	 * Returns the Arrangement based on arrType, product code. 
	 * @return Arrangement
	 */
	//P2AXAL027
	public Arrangement getArrangement(long arrType, long arrSubType) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Inside getArrangement with arrType >>> "+ arrType+" and arrSubType >>> "+arrSubType);
		}
		Holding holding = getHolding();
		for (int i = 0; i < holding.getArrangementCount(); i++) {
			Arrangement arrangement = holding.getArrangementAt(i);
			if (arrangement != null && !NbaUtils.isDeleted(arrangement) && arrangement.getArrType() == arrType) {
				ArrangementExtension arrExt = NbaUtils.getFirstArrangementExtension(arrangement);
				if(arrSubType>0){
					if (arrExt != null && !NbaUtils.isDeleted(arrExt)) {
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("Inside getArrangement with arrExt.getArrSubType() >>> "+ arrExt.getArrSubType());
						}
						if(arrExt.getArrSubType()==arrSubType)
							return arrangement;
					}
				} else{
					if (getLogger().isDebugEnabled() && arrExt != null ) {
						getLogger().logDebug("Inside getArrangement with arrExt >>> "+ arrExt);
						getLogger().logDebug("Inside getArrangement with arrExt.hasArrSubType() >>> "+ arrExt.hasArrSubType());
					}
					if (arrExt == null || !arrExt.hasArrSubType()){
						return arrangement;
					}
				}
			}
		}
		return null;
	}

	//P2AXAL012, P2AXAL027 new method
	public Arrangement getArrangementByArrSourceCode(long arrType, String arrSourceCode) {
		Holding holding = getHolding();
		for (int i = 0; i < holding.getArrangementCount(); i++) {
			Arrangement arrangement = holding.getArrangementAt(i);
			if (!NbaUtils.isDeleted(arrangement) && arrangement.getArrType() == arrType) {
				for (int j = 0; j < arrangement.getArrSourceCount(); j++) {
					ArrSource arrSource = arrangement.getArrSourceAt(j);
					SubAccount subAccount = findSubAccountById(arrSource.getSubAcctID());
					if (subAccount != null && arrSourceCode.equals(subAccount.getProductCode())) {
						return arrangement;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param arrangement
	 * @param subAcctProdCode
	 * @return
	 */
	//Defect ALII783
	public ArrSource getArrSourceBySubAcctProdCode(Arrangement arrangement, String subAcctProdCode) {
		if (!NbaUtils.isDeleted(arrangement) && subAcctProdCode != null) {
			for (int j = 0; j < arrangement.getArrSourceCount(); j++) {
				ArrSource arrSource = arrangement.getArrSourceAt(j);
				SubAccount subAccount = findSubAccountById(arrSource.getSubAcctID());
				if (subAccount != null && subAcctProdCode.equals(subAccount.getProductCode())) {
					return arrSource;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the ArrangementExtension. Create a new ArrangementExtension if necessary. 
	 * @return ArrangementExtension
	 */
	protected ArrangementExtension getArrangementExtension() {
		ArrangementExtension arrangementExtension;
		arrangementExtension = NbaUtils.getFirstArrangementExtension(getArrangement());
		if (arrangementExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_ARRANGEMENT);
			oLifEExtension.setActionAdd();
			getArrangement().addOLifEExtension(oLifEExtension);
			arrangementExtension = NbaUtils.getFirstArrangementExtension(getArrangement());
		}
		return arrangementExtension;
	}
	/**
	 * Returns the ArrangementExtension based on arrType. 
	 * @return ArrangementExtension
	 */
	//P2AXAL027
	protected ArrangementExtension getArrangementExtension(long arrType, long arrSubType) {
		return NbaUtils.getFirstArrangementExtension(getArrangement(arrType, arrSubType));
	}
	/**
	 * @return
	 */
	protected ArrDestination getArrDestination() {
		return arrDestination;
	}
	/**
	 * Returns the ArrDestinationExtension. Create a new ArrDestinationExtension if necessary. 
	 * @return ArrDestinationExtension
	 */
	//SPR1466
	protected ArrDestinationExtension getArrDestinationExtension() {
		ArrDestinationExtension arrDestinationExtension = NbaUtils.getFirstArrDestinationExtension(getArrDestination());
		if (arrDestinationExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_ARRDESTINATION);
			oLifEExtension.setActionAdd();
			getArrDestination().addOLifEExtension(oLifEExtension);
			arrDestinationExtension = NbaUtils.getFirstArrDestinationExtension(getArrDestination());
		}
		return arrDestinationExtension;
	}
	/**
	 * @return
	 */
	protected ArrSource getArrSource() {
		return arrSource;
	}
	/**
	 * Returns the banking.
	 * @return Banking
	 */
	protected Banking getBanking() {
		return banking;
	}
	/**
	 * Returns the carrierAppointment.
	 * @return CarrierAppointment
	 */
	protected CarrierAppointment getCarrierAppointment() {
		return carrierAppointment;
	}
	/**
	 * Returns the coverage.
	 * @return Coverage
	 */
	protected Coverage getCoverage() {
		return coverage;
	}
	/**
	 * Returns the CoverageExtension. Create a new CoverageExtension if necessary. 
	 * @return CoverageExtension
	 */
	protected CoverageExtension getCoverageExtension() {
		CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(getCoverage());
		if (coverageExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_COVERAGE);
			oLifEExtension.setActionAdd();
			getCoverage().addOLifEExtension(oLifEExtension);
			coverageExtension = NbaUtils.getFirstCoverageExtension(getCoverage());
		}
		return coverageExtension;
	}
	/**
	 * Return the CoverageProductExtension for a Coverage.
	 * @param coverage the Coverage
	 * @return the CoverageProductExtension
	 */
	//SPR1706 New Method
	protected CoverageProductExtension getCoverageProductExtensionFor(Coverage coverage) {
		return getCoverageProductExtensionFor(getCoverageProductFor(coverage, BEST_MATCH)); //SPR2265
	}
	/**
	 * Return the CoverageProductExtension for a Rider.
	 * @param rider the Rider
	 * @return the CoverageProductExtension
	 */
	//SPR1986 New Method
	protected CoverageProductExtension getCoverageProductExtensionFor(Rider rider) {
		return getCoverageProductExtensionFor(getCoverageProductFor(rider));
	}
	/**
	 * Return the CoverageProductExtension for a CoverageProduct.
	 * @param coverageProduct the CoverageProduct
	 * @return the CoverageProductExtension
	 */
	//SPR1706 New Method
	protected CoverageProductExtension getCoverageProductExtensionFor(CoverageProduct coverageProduct) {
		CoverageProductExtension coverageProductExtension = null;
		if (coverageProduct != null) {
			com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
			int extCount = coverageProduct.getOLifEExtensionCount();
			for (int index = 0; index < extCount; index++) {
				extension = coverageProduct.getOLifEExtensionAt(index); //nba237
				if (extension != null) {
					if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isCoverageProductExtension()) {
						coverageProductExtension = extension.getCoverageProductExtension();
					}
				}
			}
		}
		return coverageProductExtension;
	}
	/**
	 * Return the CoverageProduct from the PolicyProduct for a Coverage.
	 * If no indicator code was specified by caller (i.e. -1 is pased in) lookup for record with base indicator code,
	 * if none is found, attempt for record with riders indicator code 
	 * @param coverage the Coverage
	 * @return the CoverageProduct
	 */
	protected CoverageProduct getCoverageProductFor(Coverage coverage, long indicatorCode) { 	//SPR1706
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Inside getCoverageProductFor with IndicatorCode >>> "+ indicatorCode);
		}
		CoverageProduct coverageProduct = null;
		PolicyProduct policyProduct = getPolicyProductFor(coverage);
		if (policyProduct != null) {
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("policyProduct  found");
			}
			LifeProduct lifeProduct = getLifeProductFor(policyProduct);
			if (lifeProduct != null) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("lifeProduct found");
				}
				//begin SPR2265
				if (indicatorCode == BEST_MATCH) {
					coverageProduct = getCoverageProductFor(lifeProduct, coverage, OLI_COVIND_BASE);
					if (coverageProduct == null) {
						coverageProduct = getCoverageProductFor(lifeProduct, coverage, OLI_COVIND_RIDER);
					}
					if (coverageProduct == null) { //NBA237
						coverageProduct = getCoverageProductFor(lifeProduct, coverage, OLI_COVIND_INTEGRATED); //NBA237
					} //NBA237
				} else {
					//end SPR2265
					coverageProduct = getCoverageProductFor(lifeProduct, coverage, indicatorCode);  //SPR1706
				} //SPR2265
			}
		}
		return coverageProduct;
	}
	/**
	 * Return the CoverageProduct from the PolicyProduct for a Coverage.
	 * @param coverage the Coverage
	 * @return the CoverageProduct
	 */
	//SPR1706 New Method //P2AXAL024 changed method visibility to public
	public CoverageProduct getCoverageProductFor(Coverage coverage) {
		return getCoverageProductFor(coverage, BEST_MATCH);	//SPR1706 SPR2265
	}
	/**
	 * Return the CoverageProduct from a LifeProduct for a Coverage.
	 * @param lifeProduct the LifeProduct
	 * @param coverage the Coverage
	 * @return the CoverageProduct
	 */
	protected CoverageProduct getCoverageProductFor(LifeProduct lifeProduct, Coverage coverage, long indicatorCode) {	//SPR1706
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Inside getCoverageProductFor based on indicatorCode >>> "+ indicatorCode);
		}
		CoverageProduct coverageProduct = null;
		//SPR1706 code deleted
		String productCode = coverage.getProductCode();
		for (int i = 0; i < lifeProduct.getCoverageProductCount(); i++) {
			CoverageProduct tempCoverageProduct = lifeProduct.getCoverageProductAt(i);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("tempCoverageProduct.getProductCode() ==== "+tempCoverageProduct.getProductCode());
				getLogger().logDebug("tempCoverageProduct.getIndicatorCode() ==== "+tempCoverageProduct.getIndicatorCode());
				getLogger().logDebug("productCode ==== "+productCode);
			}
			if (tempCoverageProduct.getProductCode().equals(productCode) && tempCoverageProduct.getIndicatorCode() == indicatorCode) {
				coverageProduct = tempCoverageProduct;
				break;
			}
		}
		return coverageProduct;
	}
	/**
	 * Return the CoverageProduct from a LifeProduct for a Rider.
	 * @param lifeProduct the LifeProduct
	 * @param coverage the Coverage
	 * @return the CoverageProduct
	 */
	protected CoverageProduct getCoverageProductFor(LifeProduct lifeProduct, Rider rider) {
		CoverageProduct coverageProduct = null;
		for (int i = 0; i < lifeProduct.getCoverageProductCount(); i++) {
			if (lifeProduct.getCoverageProductAt(i).getProductCode().equals(rider.getRiderCode())) {  //NBA104
				coverageProduct = lifeProduct.getCoverageProductAt(i);
				break;
			}
		}
		return coverageProduct;
	}
	/**
	 * Return the CoverageProduct from the PolicyProduct for a Rider.
	 * @param coverage the Rider
	 * @return the CoverageProduct
	 */
	protected CoverageProduct getCoverageProductFor(Rider rider) {
		CoverageProduct coverageProduct = null;
		PolicyProduct policyProduct = getPolicyProductFor(rider);
		if (policyProduct != null) {
			LifeProduct lifeProduct = getLifeProductFor(policyProduct);
			if (lifeProduct != null) {
				coverageProduct = getCoverageProductFor(lifeProduct, rider);	//SPR1706
			}
		}
		return coverageProduct;
	}
	/**
	 * Return the CoverageProduct for a Coverage.
	 * @param lifeProduct the LifeProduct
	 * @param coverage the Coverage
	 * @return the CoverageProduct
	 */
	protected CoverageProduct getCoverageProductForPlan(Coverage coverage) {
		CoverageProduct coverageProduct = null;
		LifeProduct lifeProduct = getLifeProductForPlan();
		if (lifeProduct != null) {
			long indicatorCode = coverage.getIndicatorCode();
			String productCode = coverage.getProductCode();
			for (int i = 0; i < lifeProduct.getCoverageProductCount(); i++) {
				CoverageProduct tempCoverageProduct = lifeProduct.getCoverageProductAt(i);
				if (tempCoverageProduct.getProductCode().equals(productCode) && tempCoverageProduct.getIndicatorCode() == indicatorCode) {
					coverageProduct = tempCoverageProduct;
					break;
				}
			}
		}
		return coverageProduct;
	}
	/**
	 * Return the CoverageProduct for a Rider. 
	 * @param Rider the Rider
	 * @return the RiderProduct
	 */
	protected CoverageProduct getCoverageProductForPlan(Rider Rider) {
		CoverageProduct coverageProduct = null;
		LifeProduct lifeProduct = getLifeProductForPlan();
		if (lifeProduct != null) {
			for (int i = 0; i < lifeProduct.getCoverageProductCount(); i++) {
				if (lifeProduct.getCoverageProductAt(i).getProductCode().equals(Rider.getProductCode())) {
					coverageProduct = lifeProduct.getCoverageProductAt(i);
					break;
				}
			}
		}
		return coverageProduct;
	}
	/**
	 * Returns the covOption.
	 * @return CovOption
	 */
	protected CovOption getCovOption() {
		return covOption;
	}
	/**
	 * Returns the CovOptionExtension. Create a new CovOptionExtension if necessary. 
	 * @return CovOptionExtension
	 */
	protected CovOptionExtension getCovOptionExtension() {
		return getCovOptionExtension(getCovOption());
	}	

	/**
	 * Returns the CovOptionExtension. Create a new CovOptionExtension if necessary. 
	 * @return CovOptionExtension
	 */
	protected CovOptionExtension getCovOptionExtension(CovOption covOption) {
		CovOptionExtension covOptionExtension = NbaUtils.getFirstCovOptionExtension(covOption);
		if (covOptionExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_COVOPTION);
			oLifEExtension.setActionAdd();
			covOption.addOLifEExtension(oLifEExtension);
			covOptionExtension = NbaUtils.getFirstCovOptionExtension(covOption);
		}
		return covOptionExtension;
	}
	/**
	 * Return the CovOptionProductExtension for a CovOptionProduct.
	 * @param CovOptionProduct the CovOptionProduct
	 * @return the CovOptionProductExtension
	 */
	//P2AXAL024 method visibility changed to public
	public CovOptionProductExtension getCovOptionProductExtensionFor(CovOptionProduct covOptionProduct) {
		CovOptionProductExtension CovOptionProductExtension = null;
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		int extCount = covOptionProduct.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			extension = covOptionProduct.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isCovOptionProductExtension()) {
					CovOptionProductExtension = extension.getCovOptionProductExtension();
				}
			}
		}
		return CovOptionProductExtension;
	}
	/**
	 * Return the CovOptionProduct from the CoverageProduct for a CovOption associated with a Coverage.
	 * @param coverage the Coverage
	 * @param covOption the CovOption 
	 * @return the CovOptionProduct
	 */
	//P2AXAL024 Changed the signature to public
	public CovOptionProduct getCovOptionProductFor(Coverage coverage, CovOption covOption) {
		CovOptionProduct covOptionProduct = null;
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);	//SPR1706,AXAL3.7.40
		if (coverageProduct != null) {
			String productCode = covOption.getProductCode();

			for (int i = 0; i < coverageProduct.getCovOptionProductCount(); i++) {
				// begin AXAL3.7.40
				if (getLogger().isDebugEnabled() && coverageProduct.getCovOptionProductAt(i) != null) {
					getLogger().logDebug("covOptProduct Product Code" + coverageProduct.getCovOptionProductAt(i).getProductCode());
					getLogger().logDebug("covOptProduct id" + coverageProduct.getCovOptionProductAt(i).getId());
					getLogger().logDebug("Is covOptProduct active >>> " + isActiveCoverageProduct(coverageProduct.getCovOptionProductAt(i)));
				}
				if (coverageProduct.getCovOptionProductAt(i).getProductCode().equals(productCode)
						&& isActiveCoverageProduct(coverageProduct.getCovOptionProductAt(i))) { // NBLXA-1988
					covOptionProduct = coverageProduct.getCovOptionProductAt(i);
					break;
				}
				// end AXAL3.7.40
				// Code deleted for CR56683
			}
		}
		return covOptionProduct;
	}

	/**
	 * Return the CovOptionProduct from the CoverageProduct for a CovOption associated with a Coverage.
	 * @param coverage the Coverage
	 * @param covOption the CovOption 
	 * @return the CovOptionProduct
	 */
	//ALII1117 New Method
	public CovOptionProduct getCovOptionProductFor(Coverage coverage, long lifeCovOptTypeCode) {
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);
		if (coverageProduct == null) {
			return null;
		}
		for (int i = 0; i < coverageProduct.getCovOptionProductCount(); i++) {
			CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(coverageProduct.getCovOptionProductAt(i));
			if (covOptionProductExtension == null) {
				return null;
			}
			if (getLogger().isDebugEnabled() && coverageProduct.getCovOptionProductAt(i) != null) {
				getLogger().logDebug("covOptProduct Product Code" + coverageProduct.getCovOptionProductAt(i).getProductCode());
				getLogger().logDebug("covOptProduct id" + coverageProduct.getCovOptionProductAt(i).getId());
				getLogger().logDebug("Is covOptProduct active >>> " + isActiveCoverageProduct(coverageProduct.getCovOptionProductAt(i)));
			}
			if (covOptionProductExtension.getLifeCovOptTypeCode() == lifeCovOptTypeCode
					&& isActiveCoverageProduct(coverageProduct.getCovOptionProductAt(i))) { // NBLXA-1988
				return coverageProduct.getCovOptionProductAt(i);
			}
		}
		return null;
	}	


	/**
	 * Return the CovOptionProduct from the CoverageProduct for a CovOption associated with a Rider.
	 * @param rider the Rider
	 * @param covOption the CovOption 
	 * @return the CovOptionProduct
	 */
	protected CovOptionProduct getCovOptionProductFor(Rider rider, CovOption covOption) {
		CovOptionProduct covOptionProduct = null;
		CoverageProduct coverageProduct = getCoverageProductFor(rider);
		if (coverageProduct != null) {
			String productCode = covOption.getProductCode();
			for (int i = 0; i < coverageProduct.getCovOptionProductCount(); i++) {
				if (getLogger().isDebugEnabled() && coverageProduct.getCovOptionProductAt(i) != null) {
					getLogger().logDebug("covOptProduct Product Code" + coverageProduct.getCovOptionProductAt(i).getProductCode());
					getLogger().logDebug("covOptProduct id" + coverageProduct.getCovOptionProductAt(i).getId());
					getLogger().logDebug("Is covOptProduct active >>> " + isActiveCoverageProduct(coverageProduct.getCovOptionProductAt(i)));
				}
				if (isActiveCoverageProduct(coverageProduct.getCovOptionProductAt(i))) { // NBLXA-1988
					// begin AXAL3.7.40
					if (coverageProduct.getCovOptionProductAt(i).getProductCode().equals(productCode)) {
						covOptionProduct = coverageProduct.getCovOptionProductAt(i);
						break;
					} else if (coverageProduct.getCovOptionProductAt(i) != null) {
						CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(coverageProduct
								.getCovOptionProductAt(i));
						if (covOptionProductExtension != null) {
							if (String.valueOf(covOptionProductExtension.getLifeCovOptTypeCode()).equals(productCode)) {
								covOptionProduct = coverageProduct.getCovOptionProductAt(i);
								break;
							}
						}
					} // end AXAL3.7.40
				}
			}
		}
		return covOptionProduct;
	}
	/**
	 * Returns the currentDate.
	 * @return Date
	 */
	protected Date getCurrentDate() {
		if (currentDate == null) {
			setCurrentDate(new Date(System.currentTimeMillis()));
		}
		return currentDate;
	}
	/**
	 * Returns the currentSubSet.
	 * @return Integer
	 */
	protected Integer getCurrentSubSet() {
		return currentSubSet;
	}
	/**
	 * Returns the financialActivity.
	 * @return FinancialActivity
	 */
	protected FinancialActivity getFinancialActivity() {
		return financialActivity;
	}
	/**
	 * Returns the FinancialActivityExtension. Create a new FinancialActivityExtension if necessary. 
	 * @return FinancialActivityExtension
	 */
	protected FinancialActivityExtension getFinancialActivityExtension() {
		FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(getFinancialActivity());
		if (financialActivityExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_FINANCIALACTIVITY);
			oLifEExtension.setActionAdd();
			getFinancialActivity().addOLifEExtension(oLifEExtension);
			financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(getFinancialActivity());
		}
		return financialActivityExtension;
	}
	/**
	 * Returns the holding.
	 * @return Holding
	 */
	protected Holding getHolding() {
		if (holding == null) {
			setHolding(getNbaTXLife().getPrimaryHolding());
		}
		return holding;
	}

	/**
	 * Returns the bankHolding.
	 * @return Holding
	 */
	//ALS3600 New Method
	protected Holding getBankHolding() {
		Holding hold = null;
		if (bankHolding == null) {
			for(int i=0; i< getNbaTXLife().getOLifE().getHoldingCount(); i++){
				hold = getNbaTXLife().getOLifE().getHoldingAt(i);
				if(OLI_HOLDTYPE_BANKING == hold.getHoldingTypeCode()){
					setBankholding(hold);
				}
			}
		}
		return bankHolding;
	}

	/**
	 * Returns the holdingExtension. Create a new HoldingExtension if necessary. 
	 * @return HoldingExtension
	 */
	protected HoldingExtension getHoldingExtension() {
		if (holdingExtension == null) {
			holdingExtension = NbaUtils.getFirstHoldingExtension(getHolding());
			if (holdingExtension == null) {
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_HOLDING);
				oLifEExtension.setActionAdd();
				getHolding().addOLifEExtension(oLifEExtension);
				holdingExtension = NbaUtils.getFirstHoldingExtension(getHolding());
			}
		}
		return holdingExtension;
	}
	/**
	 * Sets the nbaConfigValProc.
	 * @param nbaConfigValProc The nbaConfigValProc to set
	 */
	protected String getIdOf(NbaContractVO nbaContractVO) {
		String id = nbaContractVO.getId();
		if (id.equals(NbaConstants.NO_ID)) {
			if (nbaContractVO instanceof Life) {
				return getPolicy().getId(); //SPR3168
			}
			if (nbaContractVO instanceof ApplicationInfo) {
				return getPolicy().getId(); //SPR3168
			}
			if (nbaContractVO instanceof Annuity) {
				return getPolicy().getId(); //SPR3168
			}
			if (nbaContractVO instanceof Organization) {
				return getPolicy().getId(); //SPR3168
			}
			//NBLXA2303[NBLXA-2312]
			if (nbaContractVO instanceof Person) {
				return getParty().getId(); //NBLXA2303[NBLXA-2312]
			}
			return "";
		}
		return id;
	}
	/**
	 * Returns the investment.
	 * @return Investment
	 */
	protected Investment getInvestment() {
		//begin SPR1917
		if (investment == null) {
			setInvestment(getHolding().getInvestment());
		}
		//end spr1917 
		return investment;
	}
	/**
	 * Get the InvestProductInfo for the productCode.
	 * @param productCode the product code
	 * @return InvestProductInfo
	 */
	//ALII1990 Method Refactored
	protected InvestProductInfo getInvestProductInfo(String productCode) {		
		InvestProductInfo investProductInfo = null;
		Date appSignDate = getApplicationInfo().getSignedDate();
		PolicyProduct policyProduct = getPolicyProductForPlan();		
		if (policyProduct != null) {			
			for (int i = 0; i < policyProduct.getInvestProductInfoCount(); i++) {
				if (policyProduct.getInvestProductInfoAt(i).getProductCode().equals(productCode)) {
					investProductInfo = policyProduct.getInvestProductInfoAt(i);					
					if ((!NbaUtils.isBlankOrNull(investProductInfo.getSaleExpirationDate()) || !NbaUtils.isBlankOrNull(investProductInfo.getSaleEffectiveDate()) && !NbaUtils.isBlankOrNull(appSignDate))) { //APSL4248
						if (!(NbaUtils.compare(appSignDate, investProductInfo.getSaleExpirationDate()) <= 0) || !(NbaUtils.compare(appSignDate, investProductInfo.getSaleEffectiveDate()) >= 0)) { //APSL4248
							return null;
						}
					}
					break;
				}
			}
		}
		return investProductInfo;
	}
	/**
	 * Return the InvestProductInfoExtension for a InvestProductInfo.
	 * @param InvestProductInfo the InvestProductInfo
	 * @return the InvestProductInfoExtension
	 */
	protected InvestProductInfoExtension getInvestProductInfoExtensionFor(InvestProductInfo investProductInfo) {
		InvestProductInfoExtension investProductInfoExtension = null;
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		int extCount = investProductInfo.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			extension = investProductInfo.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isInvestProductInfoExtension()) {
					investProductInfoExtension = extension.getInvestProductInfoExtension();
				}
			}
		}
		return investProductInfoExtension;
	}
	/**
	 * Returns the life.
	 * @return Life
	 */
	protected Life getLife() {
		if (life == null) {
			// Only one occurrence of Life
			if (getPolicy().hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) {
				LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladhpc =
						getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
				if (ladhpc.isLife()) {
					setLife(ladhpc.getLife());
				}
			}
		}
		return life;
	}
	/**
	 * Returns the LifeExtension. Create a new LifeExtension if necessary. 
	 * @return LifeExtension
	 */
	protected LifeExtension getLifeExtension() {
		if (lifeExtension == null) {
			lifeExtension = NbaUtils.getFirstLifeExtension(getLife());
			if (lifeExtension == null) {
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_LIFE);
				oLifEExtension.setActionAdd();
				getLife().addOLifEExtension(oLifEExtension);
				lifeExtension = NbaUtils.getFirstLifeExtension(getLife());
			}
		}
		return lifeExtension;
	}
	/**
	 * Returns the lifeParticipant.
	 * @return LifeParticipant
	 */
	protected LifeParticipant getLifeParticipant() {
		return lifeParticipant;
	}
	/**
	 * Return the LifeProductExtension for a LifeProduct.
	 * @param LifeProduct the LifeProduct
	 * @return the LifeProductExtension
	 */
	protected LifeProductExtension getLifeProductExtensionFor(LifeProduct lifeProduct) {
		LifeProductExtension lifeProductExtension = null;
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		int extCount = lifeProduct.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			extension = lifeProduct.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isLifeProductExtension()) {
					lifeProductExtension = extension.getLifeProductExtension();
				}
			}
		}
		return lifeProductExtension;
	}
	/**
	 * Return the LifeProduct for the plan.
	 * @param policyProduct the PolicyProduct
	 * @return the LifeProduct
	 */
	protected LifeProductExtension getLifeProductExtensionForPlan() {
		LifeProductExtension lifeProductExtension = null;
		LifeProduct lifeProduct = getLifeProductForPlan();
		if (lifeProduct != null) {
			lifeProductExtension = getLifeProductExtensionFor(lifeProduct);
		}
		return lifeProductExtension;
	}
	/**
	 * Return the LifeProduct for a PolicyProduct.
	 * @param policyProduct the PolicyProduct
	 * @return the LifeProduct
	 */
	protected LifeProduct getLifeProductFor(PolicyProduct policyProduct) {
		LifeProduct LifeProduct = null;
		if (policyProduct.hasLifeProductOrAnnuityProduct() && policyProduct.getLifeProductOrAnnuityProduct().isLifeProduct()) {
			LifeProduct = policyProduct.getLifeProductOrAnnuityProduct().getLifeProduct();
		}
		return LifeProduct;
	}
	/**
	 * Return the LifeProduct for the plan.
	 * @param policyProduct the PolicyProduct
	 * @return the LifeProduct
	 */
	protected LifeProduct getLifeProductForPlan() {
		LifeProduct LifeProduct = null;
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			if (policyProduct.hasLifeProductOrAnnuityProduct() && policyProduct.getLifeProductOrAnnuityProduct().isLifeProduct()) {
				LifeProduct = policyProduct.getLifeProductOrAnnuityProduct().getLifeProduct();
			}
		}
		return LifeProduct;
	}
	/**
	 * Returns the LifeUSA. Create a new LifeUSA if necessary. 
	 * @return LifeUSA
	 */
	protected LifeUSA getLifeUSA() {
		if (lifeUSA == null) {
			lifeUSA = getLife().getLifeUSA();
			if (lifeUSA == null) {
				lifeUSA = new LifeUSA();
				getLife().setLifeUSA(lifeUSA);
				lifeUSA.setActionAdd();
			}
		}
		return lifeUSA;
	}
	/**
	 * Returns the LifeUSAExtension. Create a new LifeUSAExtension if necessary. 
	 * @return LifeUSAExtension
	 */
	// NBA104 New Method
	protected LifeUSAExtension getLifeUSAExtension() {
		if (lifeUSAExtension == null) {
			lifeUSAExtension = NbaUtils.getFirstLifeUSAExtension(getLifeUSA());
			if (lifeUSAExtension == null) {
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_LIFEUSA);
				oLifEExtension.setActionAdd();
				getLifeUSA().addOLifEExtension(oLifEExtension);
				lifeUSAExtension = NbaUtils.getFirstLifeUSAExtension(getLifeUSA());
			}
		}
		return lifeUSAExtension;
	}
	/**
	 * Get the Minimum Issue Age
	 * @return age
	 */
	//SPR1684 New Method P2AXAL055 Method refactored
	protected int getMinIssueAge(Coverage coverage, CovOption covOption) {	//SPR1996 changed method signature
		int age = 0;
		//Begin AXAL3.7.40
		if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_DEP && coverage.getLifeCovTypeCode() == OLI_COVTYPE_CHILDTERM) {
			return age;
		}
		//End AXAL3.7.40
		UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProduct(coverage, getLifeParticipant(), covOption);//ALII1160 look for best matching UnderwritingClassProduct
		if (underwritingClassProduct != null) {
			UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
			if (ext != null && !ext.getAgeAmtProduct().isEmpty()) {
				PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
				int ages[] = getMinMaxAgesForCovOption(ext, policyExtn.getDistributionChannel(), getLifeParticipant()); //NBLXA-1988
				age = ages[0];
			} else {
				age = underwritingClassProduct.hasMinIssueAge() ? underwritingClassProduct.getMinIssueAge() : age;
			}
		}
		return age;
	}
	/**
	 * Get the Maximum Issue Age
	 * @return age
	 */
	//P2AXAL055 New Method
	protected int getMaxIssueAge(Coverage coverage, CovOption covOption) {
		int maxAge = 999;
		UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProduct(coverage, getLifeParticipant(), covOption);
		if (underwritingClassProduct != null) {
			UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
			if (ext != null && !ext.getAgeAmtProduct().isEmpty()) {
				PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
				int ages[] = getMinMaxAgesForCovOption(ext, policyExtn.getDistributionChannel(), getLifeParticipant()); //NBLXA-1988
				maxAge = ages[1];
			} else {
				maxAge = underwritingClassProduct.hasMaxIssueAge() ? underwritingClassProduct.getMaxIssueAge() : maxAge;
			}
		}
		return maxAge;
	}

	/**
	 * Get the Max Issue Age
	 * 
	 * @return age
	 */
	//SPR1684 New Method
	protected int getMaxIssueAge(Rider rider, CovOption covOption) { //SPR1996 changed method signature
		int age = 999;
		UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProduct(rider, covOption); //SPR1996
		if (underwritingClassProduct != null && underwritingClassProduct.hasMaxIssueAge()) {
			age = underwritingClassProduct.getMaxIssueAge();
			// SPR1996 code deleted
			if (age <= 0) {
				age = 999;
			}
		}	//SPR1996
		return age;
	}
	/**
	 * Get the Minimum Issue Age
	 * @return age
	 */
	//SPR1684 New Method
	protected int getMinIssueAge(Rider rider, CovOption covOption) {		//SPR1996 changed method signature
		int age = 0;
		UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProduct(rider, covOption);		//SPR1996
		if (underwritingClassProduct != null && underwritingClassProduct.hasMinIssueAge()) {
			age = underwritingClassProduct.getMinIssueAge();
		}
		return age;
	}
	/**
	 * Get the Max Issue Age from a CoverageProduct
	 * @return age
	 */
	// SPR1996 New Method
	protected int getMaxIssueAge(ArrayList underwritingClassProducts, long smokerStat, long gender) {
		int age = -1;
		UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductMaxAgeFor(underwritingClassProducts, smokerStat, gender);
		if (underwritingClassProduct != null && underwritingClassProduct.hasMaxIssueAge()) {
			age = underwritingClassProduct.getMaxIssueAge();
		}
		return age;
	}
	/**
	 * Get the Min Issue Age from a CoverageProduct
	 * @return age
	 */
	// SPR1996 New Method
	protected int getMinIssueAge(ArrayList underwritingClassProducts, long smokerStat, long gender) {
		int age = -1;
		UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductMinAgeFor(underwritingClassProducts, smokerStat, gender);
		if (underwritingClassProduct != null && underwritingClassProduct.hasMinIssueAge()) {
			age = underwritingClassProduct.getMinIssueAge();
		}
		return age;
	}
	/**
	 * Get the Max Issue Age for an Annuity From Ownership
	 * @return age
	 */
	protected int getMaxIssueAge(long gender) {
		int age = 999; //SPR1705
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			age = getMaxIssueAge(policyProduct, gender);
			// SPR1996 code deleted
			if (age <= 0) { //SPR1705
				age = 999; //SPR1705
			} //SPR1705
		}	//SPR1996
		return age;
	}
	/**
	 * Get the Max Issue Age for an Annuity From Ownership
	 * @return age
	 */
	protected int getMaxIssueAge(PolicyProduct policyProduct, long gender) {
		int age = 0;
		for (int i = 0; i < policyProduct.getOwnershipCount(); i++) {
			Ownership ownership = policyProduct.getOwnershipAt(i);
			OwnershipExtension ownershipExtension = getOwnershipExtensionFor(ownership);
			if (ownershipExtension == null || (ownershipExtension != null && ownershipExtension.getIssueGender() == gender)) { //SPR1705
				age = ownership.getMaxIssueAge();
				break;
			}
		}
		return age;
	}
	/**
	 * Get the Max Issue Age
	 * @return age
	 */
	protected int getMaxIssueAge(Rider rider, long gender) {
		int age = -1;
		CoverageProduct coverageProduct = getCoverageProductFor(rider);
		if (coverageProduct != null) {
			//begin SPR1996
			Participant participant = NbaUtils.getInsurableParticipant(rider);
			if (participant != null) {
				long smokerStat = getSmokerStat(participant);
				age = getMaxIssueAge(coverageProduct.getUnderwritingClassProduct(), smokerStat, gender);
			}
			//end SPR1996
		}
		if (age <= 0) { //SPR1705
			age = 999; //SPR1705
		} //SPR1705
		return age;
	}
	// SPR1996 code deleted
	/**
	 * Get the Min Issue Age for an Annuity
	 * @return age
	 */
	protected int getMinIssueAge(long gender) {
		int age = 0;
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			age = getMinIssueAge(policyProduct, gender);
		}
		return age;
	}
	/**
	 * Get the Min Issue Age for an Annuity
	 * @return age
	 */
	protected int getMinIssueAge(PolicyProduct policyProduct, long gender) {
		int age = 0;
		for (int i = 0; i < policyProduct.getOwnershipCount(); i++) {
			Ownership ownership = policyProduct.getOwnershipAt(i);
			OwnershipExtension ownershipExtension = getOwnershipExtensionFor(ownership);
			if (ownershipExtension != null && ownershipExtension.getIssueGender() == gender) {
				age = ownership.getMinIssueAge();
				break;
			}
		}
		return age;
	}
	/**
	 * Get the Minimum Issue Age
	 * @return age
	 */
	protected int getMinIssueAge(Rider rider, long gender) {
		int age = 0; //SPR1705
		CoverageProduct coverageProduct = getCoverageProductFor(rider);
		if (coverageProduct != null) {		
			//begin SPR1996	 
			Participant participant = NbaUtils.getInsurableParticipant(rider);
			if (participant != null) {
				long smokerStat = getSmokerStat(participant);
				age = getMinIssueAge(coverageProduct.getUnderwritingClassProduct(), smokerStat, gender);
			}
			//end SPR1996
		}
		return age;
	}
	/**
	 * Lazy initialize a <code>DateFormat</code>
	 * @return java.text.DateFormat
	 */
	protected DateFormat getMMDDYYYYsdf() {
		if (mmddyyyydf == null) {
			mmddyyyydf = new SimpleDateFormat(MMDDYYYY_DATEFORMAT);
		}
		return mmddyyyydf;
	}
	/**
	 * Lazy initialize a <code>DateFormat</code>
	 * @return java.text.DateFormat
	 */
	protected DateFormat get_YYYY_MM_DD_sdf() {
		if (yyyy_mm_dd == null) {
			yyyy_mm_dd = new SimpleDateFormat(YYYY_MM_DD_DATEFORMAT);
		}
		return yyyy_mm_dd;
	}
	/**
	 * Calculate the mode factor.
	 * @return modefactor
	 */
	protected double getModeFactor() {
		long mode = getPolicy().getPaymentMode();
		double factor = 0;
		if (mode < 1) {
			factor = 1;
		} else if (mode < 5) {
			factor = mode;
		} else if (mode == 5) {
			factor = 24;
		} else if (mode == 6) {
			factor = 52;
		} else if (mode == 7) {
			factor = 26;
		} else if (mode == 8) {
			factor = 365;
		} else if (mode == 9) {
			factor = 1;
		} else if (mode == 10) {
			factor = 9;
		} else if (mode == 12) {
			factor = 13;
		} else if (mode == 13) {
			factor = 10;
		} else if (mode == 14) {
			factor = 8;
		} else if (mode == 15) {
			factor = 6;
		} else if (mode == 16 || mode == 23) {
			factor = 12 / 4;
		} else if (mode == 17 || mode == 25) {
			factor = 12 / 5;
		} else if (mode == 18) {
			factor = 12 / 7;
		} else if (mode == 19) {
			factor = 12 / 8;
		} else if (mode == 20) {
			factor = 12 / 9;
		} else if (mode == 21) {
			factor = 12 / 10;
		} else if (mode == 22) {
			factor = 12 / 11;
		} else if (mode == 24) {
			factor = 52 / 3;
		} else if (mode == 26) {
			factor = 11;
		} else if (mode == 27) {
			factor = 14;
		} else if (mode == 28) {
			factor = 28;
		}
		return factor;
	}
	/**
	 * Determine the message severity for a msgCode. If the table entry cannot 
	 * be located, return OLIX_MSGSEVERITY_SEVERE and post a Z999 error. 
	 * @param msgCode the message code
	 * @return long the message severity
	 */
	protected long getMsgSeverity(int msgCode) {
		Integer.toString(msgCode);
		NbaValidationMessageData[] msgs = null;
		try {
			msgs = (NbaValidationMessageData[]) getNbaTableAccessor().getValidationMessages(Integer.toString(msgCode));
		} catch (NbaDataAccessException e) {
		}
		if (msgs == null || msgs.length < 1) {
			SystemMessage msg = getNewSystemMessage(INVALID_ERROR_MESSAGE, "Message code " + msgCode, "");
			if (!duplicateMessage(msg)) {
				getHolding().addSystemMessage(msg);
				logNewMessage(msg);
			}
			return OLI_MSGSEVERITY_SEVERE;
		}
		return msgs[0].getMsgSeverityTypeCode();
	}
	/**
	 * Returns the nbaConfigValProc.
	 * @return NbaConfigValProc
	 */
	// ACN012 changed signature
	protected ValProc getNbaConfigValProc() {
		return nbaConfigValProc;
	}
	/**
	 * Returns the nbaDst.
	 * @return NbaDst
	 */
	protected NbaDst getNbaDst() {
		return nbaDst;
	}
	/**
	 * Returns the nbaOLifEId.
	 * @return NbaOLifEId
	 */
	protected NbaOLifEId getNbaOLifEId() {
		return nbaOLifEId;
	}
	/**
	 * Returns the nbaParty.
	 * @return NbaParty
	 */
	protected NbaParty getNbaParty() {
		return nbaParty;
	}
	/**
	 * Returns the NbaProduct.
	 * @return NbaProduct
	 */
	//P2AXAL016 Method changed
	public AccelProduct getNbaProduct() {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In Method getNbaProduct where nbaProduct ==== "+nbaProduct);
		}
		if (nbaProduct == null) {
			NbaProductAccessFacadeBean nbaProductAccessFacade = new NbaProductAccessFacadeBean(); 
			try {
				setNbaProduct(nbaProductAccessFacade.doProductInquiry(getNbaTXLife()));
			} catch (NbaBaseException e) {
				getLogger().logException(e);	
			}
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In Method getNbaProduct Returning nbaProduct ==== "+nbaProduct);
		}
		return nbaProduct;
	}
	/**
	 * Returns a new NbaTableAccessor.
	 * @return A new NbaTableAccessor
	 * @see com.csc.fsg.nba.tableaccess.NbaTableAccessor
	 */
	protected NbaTableAccessor getNbaTableAccessor() {
		if (nbaTableAccessor == null) {
			nbaTableAccessor = new NbaTableAccessor();
		}
		return nbaTableAccessor;
	}
	/**
	 * Returns the nbaTXLife.
	 * @return NbaTXLife
	 */
	protected NbaTXLife getNbaTXLife() {
		return nbaTXLife;
	}
	/**
	 * Create an initialized SystemMessage.
	 * @param msgCode the error message number
	 * @param msgDescription the additional text for the message
	 * @param msgRefId the related object
	 * @return SystemMessage the initilialized SystemMessage
	 */
	protected SystemMessage getNewSystemMessage(int msgCode, String msgDescription, String msgRefId) {
		SystemMessage msg = new SystemMessage();
		if (getNbaOLifEId() == null) { // Really bad problem if it's null
			msg.setId("SystemMessage_" + System.currentTimeMillis());
		} else {
			getNbaOLifEId().setId(msg);
		}
		msg.setMessageCode(msgCode);
		if (msgDescription.length() > 1000) {	//ALS3500 //NBLXA-1554[NBLXA-1916]
			msg.setMessageDescription(msgDescription.substring(0, 1000)); //NBLXA-1554[NBLXA-1916]
		} else {
			msg.setMessageDescription(msgDescription);
		}
		msg.setRelatedObjectID(( NbaUtils.isBlankOrNull(msgRefId) || msgRefId.equals(NbaConstants.NO_ID)) ? getPolicy().getId() : msgRefId);
		msg.setSequence("0");
		msg.setMessageSeverityCode(OLI_MSGSEVERITY_SEVERE); //default		 
		msg.setMessageStartDate(getCurrentDate());
		msg.setActionAdd();
		//Add the SystemMessageExtension 
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_SYSTEMMESSAGE);
		msg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(msg);
		systemMessageExtension.setMsgOverrideInd(false);
		systemMessageExtension.setMsgValidationType(getCurrentSubSet().intValue());
		//begin AXAL3.7.40
		if(generateRequirement){
			systemMessageExtension.setMsgRequirementKey(getRequirementKey());
		}else{
			systemMessageExtension.setMsgRequirementKey(null);
		}
		if (getNbaConfigValProc() != null) {
			systemMessageExtension.setMsgRestrictCode(getNbaConfigValProc().getRestrict());		
		}
		//end AXAL3.7.40

		return msg;
	}
	/**
	 * Returns the oLifE.
	 * @return OLifE
	 */
	protected OLifE getOLifE() {
		if (oLifE == null) {
			setOLifE(getNbaTXLife().getOLifE());
		}
		return oLifE;
	}
	/**
	 * Returns the organization.
	 * @return Organization
	 */
	protected Organization getOrganization() {
		return organization;
	}
	/**
	 * Return the Owner relation if it exists.
	 * @return Relation
	 */
	protected Relation getOwnerRelation() {
		return getRelation(OLI_REL_OWNER);
	}	
	/**
	 * Return the OwnershipExtension for a Ownership.
	 * @param Ownership the Ownership
	 * @return the OwnershipExtension
	 */
	protected OwnershipExtension getOwnershipExtensionFor(Ownership ownership) {
		OwnershipExtension ownershipExtension = null;
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		int extCount = ownership.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			extension = ownership.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isOwnershipExtension()) {
					ownershipExtension = extension.getOwnershipExtension();
				}
			}
		}
		return ownershipExtension;
	}
	/**
	 * Locate the Ownership for the plan matching the Gender of the primary insured or annuitant.
	 * @return Ownership
	 */
	protected Ownership getOwnershipForPlan() {
		Ownership ownership = null;
		long gender = -1;
		if (!isSystemIdVantage()) { //SPR1705
			if (getNbaTXLife().isAnnuity()) {
				Participant participant = getNbaTXLife().getPrimaryAnnuitantParticipant();
				if (participant != null) {
					gender = participant.getIssueGender();
				}
			} else {
				LifeParticipant lifeParticipant = getNbaTXLife().getPrimaryInuredLifeParticipant();
				if (lifeParticipant != null) {
					gender = lifeParticipant.getIssueGender();
				}
			}
		}	 //SPR1705
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			for (int i = 0; i < policyProduct.getOwnershipCount(); i++) {
				Ownership temp = policyProduct.getOwnershipAt(i);
				if (isSystemIdVantage()) { //SPR1705
					ownership = temp; //SPR1705
					break; //SPR1705
				} else { //SPR1705
					OwnershipExtension ownershipExtension = getOwnershipExtensionFor(temp);
					if (ownershipExtension != null && ownershipExtension.getIssueGender() == gender) {
						ownership = temp;
						break;
					}
				}
			}
		}
		return ownership;
	}
	/**
	 * Returns the participant.
	 * @return Participant
	 */
	protected Participant getParticipant() {
		return participant;
	}
	/**
	 * Returns the party.
	 * @return Party
	 */
	protected Party getParty() {
		return party;
	}
	/**
	 * Return the Payor relation if it exists.
	 * @return Relation
	 */
	protected Relation getPayerRelation() {
		String orgObjID = getNbaTXLife().getPrimaryHolding() != null ? getNbaTXLife().getPrimaryHolding().getId() : null; //APSL2735
		return getRelation(orgObjID, OLI_REL_PAYER); 
	}

	// Begin APSL3431
	protected Relation getAchPayerRelation() {
		String orgObjID = getNbaTXLife().getAchHolding() != null ? getNbaTXLife().getAchHolding().getId() : null;
		return getRelation(orgObjID, OLI_REL_PAYER); 
	}
	// End APSL3431

	/**
	 * Returns the payout.
	 * @return Payout
	 */
	protected Payout getPayout() {
		return payout;
	}
	/**
	 * Returns the person.
	 * @return Person
	 */
	protected Person getPerson() {
		return person;
	}
	/**
	 * Returns the PersonExtension. Create a new PersonExtension if necessary. 
	 * @return PersonExtension
	 */
	protected PersonExtension getPersonExtension() {
		PersonExtension personExtension = NbaUtils.getFirstPersonExtension(getPerson());
		if (personExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_PERSON);
			oLifEExtension.setActionAdd();
			getPerson().addOLifEExtension(oLifEExtension);
			personExtension = NbaUtils.getFirstPersonExtension(getPerson());
		}
		return personExtension;
	}
	/**
	 * Returns the policy.
	 * @return Policy
	 */
	protected Policy getPolicy() {
		if (policy == null) {
			setPolicy(getHolding().getPolicy());
		}
		return policy;
	}
	/**
	 * Returns the policyExtension. Create a new PolicyExtension if necessary. 
	 * @return PolicyExtension
	 */
	protected PolicyExtension getPolicyExtension() {
		if (policyExtension == null) {
			policyExtension = NbaUtils.getFirstPolicyExtension(getPolicy());
			if (policyExtension == null) {
				OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_POLICY);
				oLifEExtension.setActionAdd();
				getPolicy().addOLifEExtension(oLifEExtension);
				policyExtension = NbaUtils.getFirstPolicyExtension(getPolicy());
			}
		}
		return policyExtension;
	}
	/**
	 * Returns the PaymentModeAssembly. Creates a new PaymentModeAssembly if necessary. 
	 * @return PaymentModeAssembly
	 */
	// NBA104 New Method
	protected PaymentModeAssembly getPaymentModeAssembly() {
		if (!getPolicyExtension().hasPaymentModeAssembly()) {
			PaymentModeAssembly paymentModeAssembly = new PaymentModeAssembly();
			paymentModeAssembly.setActionAdd();
			getPolicyExtension().setPaymentModeAssembly(paymentModeAssembly);
		}
		return getPolicyExtension().getPaymentModeAssembly();
	}
	/**
	 * Returns the PaymentModeMethods. Creates a new PaymentModeMethods if necessary.
	 * @param payMode 
	 * @return PaymentModeMethods
	 */
	// NBA104 New Method
	protected PaymentModeMethods getPaymentModeMethods(long payMode) {
		PaymentModeMethods pmm = null;
		int count = getPolicyExtension().getPaymentModeMethodsCount();
		for (int i=0; i<count; i++) {
			pmm = getPolicyExtension().getPaymentModeMethodsAt(i);
			if (pmm.getPaymentMode() == payMode) {
				return pmm;
			}
		}
		pmm = new PaymentModeMethods();
		pmm.setActionAdd();
		pmm.setPaymentMode(payMode);
		if (getNbaOLifEId() != null) {
			getNbaOLifEId().setId(pmm);
		}
		getPolicyExtension().addPaymentModeMethods(pmm);
		return pmm;
	}
	/**
	 * Returns the PaymentFees. Creates a new PaymentFees if necessary.
	 * @param feeType 
	 * @return PaymentFees
	 */
	// NBA104 New Method
	// AXAL3.7.56 - Rearchitected to include Mode as a key
	protected PaymentFees getPaymentFees(long feeType, long feeMode) {
		PaymentFees paymentFees = null;
		int count = getPolicyExtension().getPaymentFeesCount();
		for (int i=0; i<count; i++) {
			paymentFees = getPolicyExtension().getPaymentFeesAt(i);
			if (paymentFees.getFeeType() == feeType && (!paymentFees.hasFeeMode() || paymentFees.getFeeMode() == feeMode)) {
				if (!paymentFees.hasFeeMode()) {
					paymentFees.setFeeMode(feeMode);
				}
				return paymentFees;
			}
		}
		paymentFees = new PaymentFees();
		paymentFees.setActionAdd();
		paymentFees.setFeeType(feeType);
		paymentFees.setFeeMode(feeMode);
		if (getNbaOLifEId() != null) {
			getNbaOLifEId().setId(paymentFees);
		}
		getPolicyExtension().addPaymentFees(paymentFees);
		return paymentFees;
	}
	/**
	 * Return the PolicyProductExtension for a PolicyProduct.
	 * @param policyProduct the PolicyProduct
	 * @return the PolicyProductExtension
	 */
	protected PolicyProductExtension getPolicyProductExtensionFor(PolicyProduct policyProduct) {
		PolicyProductExtension policyProductExtension = null;
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		int extCount = policyProduct.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			extension = policyProduct.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isPolicyProductExtension()) {
					policyProductExtension = extension.getPolicyProductExtension();
				}
			}
		}
		return policyProductExtension;
	}
	// SPR2011 code deleted
	/**
	 * Return the PolicyProductExtension for the plan.
	 * @return the PolicyProductExtension
	 */
	protected PolicyProductExtension getPolicyProductExtensionForPlan() {
		if (policyProductExtensionForPlan == null) {
			if (getPolicyProductForPlan() != null) {
				setPolicyProductExtensionForPlan(getPolicyProductExtensionFor(getPolicyProductForPlan()));
			}
		}
		return policyProductExtensionForPlan;
	}
	// SPR2011 code deleted
	/**
	 * Return the PolicyProduct for a  Coverage.
	 * @param coverage the Coverage
	 * @return PolicyProduct
	 */
	protected PolicyProduct getPolicyProductFor(Coverage coverage) {
		String plan = coverage.getProductCode();
		return getNbaProduct().getPolicyProduct(plan);
	}
	/**
	 * Return the PolicyProductExtension for a Coverage.
	 * @param coverage the Coverage
	 * @return PolicyProductExtension
	 */
	protected PolicyProductExtension getPolicyProductExtensionFor(Coverage coverage) {
		PolicyProductExtension policyProductExtension = null;
		PolicyProduct policyProduct = getPolicyProductFor(coverage);
		if (policyProduct != null) {
			policyProductExtension = getPolicyProductExtensionFor(policyProduct);
		}
		return policyProductExtension;
	}
	/**
	 * Return true if the participant role code is valid.
	 * @param coverage the Coverage
	 * @param role the participant role code
	 * @return true if the role is present in PolicyProductExtension.ParticipantRoleCodeCC
	 */
	protected boolean isValidParticipantRole(Coverage coverage, long role) {
		PolicyProduct policyProduct = getPolicyProductFor(coverage);
		if (policyProduct == null) {
			addPlanInfoMissingMessage("PolicyProduct", getIdOf(getCoverage()));
		} else {
			return isValidParticipantRole(getPolicyProductExtensionFor(policyProduct), role);
		}
		return false;
	}
	/**
	 * Return true if the participant role code is valid. 
	 * @ param role the participant role code
	 * @return true if the role is present in PolicyProductExtension.ParticipantRoleCodeCC
	 */
	protected boolean isValidParticipantRole(long role) {
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			return isValidParticipantRole(getPolicyProductExtensionFor(policyProduct), role);
		}
		return false;
	}
	/**
	 * Return true if the participant role code is valid.
	 * @param rider the Rider 
	 * @param role the participant role code
	 * @return true if the role is present in PolicyProductExtension.ParticipantRoleCodeCC
	 */
	protected boolean isValidParticipantRole(Rider rider, long role) {
		PolicyProduct policyProduct = getPolicyProductFor(rider);
		if (policyProduct != null) {
			return isValidParticipantRole(getPolicyProductExtensionFor(policyProduct), role);
		}
		return false;
	}
	/**
	 * Return true if the participant role code is valid.
	 * @param policyProductExtension the PolicyProductExtension 
	 * @param role the participant role code
	 * @return true if the role is present in PolicyProductExtension.ParticipantRoleCodeCC
	 */
	protected boolean isValidParticipantRole(PolicyProductExtension policyProductExtension, long role) {
		if (policyProductExtension == null || !policyProductExtension.hasParticipantRoleCodeCC()) {
			addPlanInfoMissingMessage("ProductExtension.ParticipantRoleCodeCC", "");
		} else {
			ParticipantRoleCodeCC participantRoleCodeCC = policyProductExtension.getParticipantRoleCodeCC();
			for (int i = 0; i < participantRoleCodeCC.getParticipantRoleCodeCount(); i++) {
				if (participantRoleCodeCC.getParticipantRoleCodeAt(i) == role) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Return the PolicyProduct for a Rider.
	 * @param product the NbaProduct to set
	 */
	protected PolicyProduct getPolicyProductFor(Rider rider) {
		String plan = rider.getRiderCode();
		return getNbaProduct().getPolicyProduct(plan);
	}
	/**
	 * Return the PolicyProduct for the plan.
	 * @param product the NbaProduct to set
	 */
	protected PolicyProduct getPolicyProductForPlan() {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In Method getPolicyProductForPlan where policyProductForPlan ==== "+policyProductForPlan);
		}
		if (policyProductForPlan == null) {
			String plan = getNbaTXLife().getPrimaryHolding().getPolicy().getProductCode();
			setPolicyProductForPlan(getNbaProduct().getPolicyProduct(plan));
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In Method getPolicyProductForPlan Returning policyProductForPlan ==== "+policyProductForPlan);
		}
		return policyProductForPlan;
	}
	/**
	 * Return the primary insured or annuitant relation if it exists.
	 * @return Relation
	 */
	protected Relation getPrimaryInsOrAnnuitantRelation() {
		if (getAnnuity() != null) {
			return getAnnuitantRelation();
		}
		if (getLife() != null) {
			return getPrimaryInsRelation();
		}
		return null;
	}
	/**
	 * Return the primary insured relation
	 * @return Relation
	 */
	protected Relation getPrimaryInsRelation() {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (OLI_REL_INSURED == relation.getRelationRoleCode() && isPrimaryInsured(relation.getRelatedObjectID())) {
					return relation;
				}
			}
		}
		return null;
	}
	/**
	 * Returns the processes.
	 * @return HashMap
	 */
	protected Map getProcesses() {
		return processes;
	}
	/**
	 * Returns the producer.
	 * @return Producer
	 */
	protected Producer getProducer() {
		return producer;
	}
	/**
	 * Return the QualTypeLimits for a qualification type.
	 * @return QualTypeLimits
	 */
	// NBA104 New Method
	protected QualTypeLimits getQualTypeLimits(long qualType) {
		QualTypeLimits qualTypeLimits = null;
		AnnuityProductExtension annuityProductExt = getAnnuityProductExtension();
		if (annuityProductExt != null) {
			int count = annuityProductExt.getQualTypeLimitsCount();
			for (int i=0; i<count; i++) {
				qualTypeLimits = annuityProductExt.getQualTypeLimitsAt(i);
				if (qualTypeLimits != null && qualTypeLimits.getQualifiedPlan() == qualType) {
					return qualTypeLimits;
				}
			}
			//begin SPR2237
			//If not found, check again to see if there are limits for a generic Qualified plan
			if (OLI_QUALPLN_NONE != qualType) {
				for (int i = 0; i < count; i++) {
					qualTypeLimits = annuityProductExt.getQualTypeLimitsAt(i);
					if (OLI_QUALPLN_QUAL == qualTypeLimits.getQualifiedPlan()) {
						return qualTypeLimits;
					}
				}
			}
			//end SPR2237			
		}
		return null;
	}
	/**
	 * Returns the relation.
	 * @return Relation
	 */
	protected Relation getRelation() {
		return relation;
	}
	/**
	 * Return the Relation for a role code.
	 * @return Relation
	 */
	protected Relation getRelation(long role) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelationRoleCode() == role) {
					return relation;
				}
			}
		}
		return null;
	}

	/**
	 * Return the Relation for a role code OriginatingObjectID.
	 * @return Relation
	 */
	//APSL2735
	protected Relation getRelation(String orgObjID, long role) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelationRoleCode() == role) {
					if (orgObjID != null) {
						if (relation.getOriginatingObjectID().equals(orgObjID)) {
							return relation;
						}
					} else {
						return relation;
					}
				} //Code removed for QC11021
			}
		}
		return null;
	}

	/**
	 * Returns the relationExtension. Create a new RelationExtension if necessary. 
	 * @return PolicyExtension
	 */
	protected RelationExtension getRelationExtension() {
		return getRelationExtension(getRelation());
	}
	/**
	 * Returns the relationExtension. Create a new RelationExtension if necessary. 
	 * @return PolicyExtension
	 */
	protected RelationExtension getRelationExtension(Relation relation) {
		RelationExtension relationExtension = NbaUtils.getFirstRelationExtension(relation);
		if (relationExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_RELATION);
			oLifEExtension.setActionAdd();
			relation.addOLifEExtension(oLifEExtension);
			relationExtension = NbaUtils.getFirstRelationExtension(relation);
		}
		return relationExtension;
	}
	/**
	 * Returns the RelationProducerExtension. Create a new RelationProducerExtension if necessary. 
	 * @return PolicyExtension
	 */
	protected RelationProducerExtension getRelationProducerExtension() {
		RelationProducerExtension relationProducerExtension;
		relationProducerExtension = NbaUtils.getFirstRelationProducerExtension(getRelation());
		if (relationProducerExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_RELATIONPRODUCER); //NBA107
			oLifEExtension.setActionAdd();
			getRelation().addOLifEExtension(oLifEExtension);
			relationProducerExtension = NbaUtils.getFirstRelationProducerExtension(getRelation());
		}
		return relationProducerExtension;
	}
	//SPR1986 code deleted
	/**
	 * Returns the requirementInfo.
	 * @return RequirementInfo
	 */
	protected RequirementInfo getRequirementInfo() {
		return requirementInfo;
	}
	/**
	 * Returns the rider.
	 * @return Rider
	 */
	protected Rider getRider() {
		return rider;
	}
	/**
	 * Returns the risk.
	 * @return Risk
	 */
	protected Risk getRisk() {
		return risk;
	}
	/**
	 * Returns the RiskExtension. Create a new RiskExtension if necessary. 
	 * @return PolicyExtension
	 */
	protected RiskExtension getRiskExtension(Risk risk) {
		RiskExtension riskExtension = NbaUtils.getFirstRiskExtension(risk);
		if (riskExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_RISK);
			oLifEExtension.setActionAdd();
			risk.addOLifEExtension(oLifEExtension);
			riskExtension = NbaUtils.getFirstRiskExtension(risk);
		}
		return riskExtension;
	}
	/**
	 * Returns the startTime.
	 * @return Date
	 */
	protected Date getStartTime() {
		return startTime;
	}
	/**
	 * Returns the subAccount.
	 * @return SubAccount
	 */
	protected SubAccount getSubAccount() {
		return subAccount;
	}
	/**
	 * Get the count of SubAccounts for a systematicActivityType for an Investment
	 * @param investment - the investment object
	 * @param systematicActivityType
	 * @return the count of matching SubAccounts
	 */
	protected int getSubAccountCount(Investment investment, long systematicActivityType) {
		int total = 0;
		for (int i = 0; i < investment.getSubAccountCount(); i++) {
			if (investment.getSubAccountAt(i).getSystematicActivityType() == systematicActivityType) {
				total++;
			}
		}
		return total;
	}
	/**
	 * Returns the SubAccountExtension. Create a new SubstandardExtension if necessary. 
	 * @return SubAccountExtension
	 */
	protected SubAccountExtension getSubAccountExtension() {
		SubAccountExtension substandardExtension = NbaUtils.getFirstSubAccountExtension(getSubAccount());
		if (substandardExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_SUBACCOUNT);
			oLifEExtension.setActionAdd();
			getSubAccount().addOLifEExtension(oLifEExtension);
			substandardExtension = NbaUtils.getFirstSubAccountExtension(getSubAccount());
		}
		return substandardExtension;
	}
	/**
	 * Returns the substandardRating.
	 * @return SubstandardRating
	 */
	protected SubstandardRating getSubstandardRating() {
		return substandardRating;
	}
	/**
	 * Returns the SubstandardRatingExtension. Create a new SubstandardExtension if necessary. 
	 * @return SubstandardRatingExtension
	 */
	protected SubstandardRatingExtension getSubstandardRatingExtension() {
		SubstandardRatingExtension substandardExtension = NbaUtils.getFirstSubstandardExtension(getSubstandardRating());
		if (substandardExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_SUBSTANDARDRATING);
			oLifEExtension.setActionAdd();
			getSubstandardRating().addOLifEExtension(oLifEExtension);
			substandardExtension = NbaUtils.getFirstSubstandardExtension(getSubstandardRating());
		}
		return substandardExtension;
	}
	/**
	 * Locate the SubstandardRisk for the coverage matching the Smoker Status and Gender. 
	 * @param coverage the Coverage
	 * @return SubstandardRisk
	 */
	protected SubstandardRisk getSubstandardRiskFor(Coverage coverage, CovOption covOption, long smokerStat, long gender) {
		SubstandardRisk substandardRisk = null;
		CovOptionProduct covOptionProduct = getCovOptionProductFor(coverage, covOption);
		if (covOptionProduct != null) {
			CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
			if (covOptionProductExtension != null) {
				for (int j = 0; j < covOptionProductExtension.getSubstandardRiskCount(); j++) {
					SubstandardRisk temp = covOptionProductExtension.getSubstandardRiskAt(j);
					if (temp.getSmokerStat() == smokerStat && temp.getIssueGender() == gender) {
						substandardRisk = temp;
						break;
					}
				}
			}
		}
		return substandardRisk;
	}

	/**
	 * Locate the SubstandardRisk for the covOption . 
	 * @param coverage the Coverage
	 * @param covOption the CovOption
	 * @return SubstandardRisk
	 */
	//SPR2103 New Method
	protected SubstandardRisk getSubstandardRiskFor(Coverage coverage, CovOption covOption) {
		SubstandardRisk substandardRisk = null;
		CovOptionProduct covOptionProduct = getCovOptionProductFor(coverage, covOption);
		if (covOptionProduct != null) {
			CovOptionProductExtension covOptionProductExtension = getCovOptionProductExtensionFor(covOptionProduct);
			if (covOptionProductExtension != null && covOptionProductExtension.getSubstandardRiskCount() > 0) {
				substandardRisk = covOptionProductExtension.getSubstandardRiskAt(0);
			}
		}
		return substandardRisk;
	}
	/**
	 * Locate the SubstandardRisk for the coverage. 
	 * @param coverage the Coverage
	 * @return SubstandardRisk
	 */
	protected SubstandardRisk getSubstandardRiskFor(Coverage coverage) {	//SPR1778
		SubstandardRisk substandardRisk = null;
		CoverageProductExtension coverageProductExtension = getCoverageProductExtensionFor(getCoverage()); 	//SPR1706
		if (coverageProductExtension != null && coverageProductExtension.getSubstandardRiskCount() > 0) { //SPR1778	//SPR1706
			substandardRisk = coverageProductExtension.getSubstandardRiskAt(0); //SPR1778	//SPR1706
			//SPR1707 code deleted
		}
		return substandardRisk;
	}
	/**
	 * Returns the systemId.
	 * @return String
	 */
	protected String getSystemId() {
		if (systemId == null) {
			setSystemId(getNbaTXLife().getBackendSystem());
		}
		return systemId;
	}
	/**
	 * @return
	 */
	protected TaxWithholding getTaxWithholding() {
		return taxWithholding;
	}
	/**
	 * Returns the TaxWithholdingExtension. Create a new TaxWithholdingExtension if necessary. 
	 * @return TaxWithholdingExtension
	 */
	protected TaxWithholdingExtension getTaxWithholdingExtension() {
		TaxWithholdingExtension taxWithholdingExtension = NbaUtils.getFirstTaxWithholdingExtension(getTaxWithholding());
		if (taxWithholdingExtension == null) {
			OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_TAXWITHHOLDING);
			oLifEExtension.setActionAdd();
			getTaxWithholding().addOLifEExtension(oLifEExtension);
			taxWithholdingExtension = NbaUtils.getFirstTaxWithholdingExtension(getTaxWithholding());
		}
		return taxWithholdingExtension;
	}
	/**
	 * Returns the tblKeys.
	 * @return Map
	 */
	protected Map getTblKeys() {
		if (tblKeys == null) {
			try {
				tblKeys = getNbaTableAccessor().setupTableMap(getNbaDst());
			} catch (Exception e) {
				tblKeys = new HashMap();
			}
		}
		return tblKeys;
	}
	/**
	 * Get the total percent for a systematicActivityType for the subaccounts of an Investment
	 * @param investment - the investment object
	 * @param systematicActivityType
	 * @return the total percent 
	 */
	protected double getTotalPercent(Investment investment, long systematicActivityType) {
		double total = 0;
		for (int i = 0; i < investment.getSubAccountCount(); i++) {
			setSubAccount(investment.getSubAccountAt(i));
			if (getSubAccount().getSystematicActivityType() == systematicActivityType
					&& getSubAccountExtension().getAllocType() == OLI_TRANSAMTTYPE_PCT) {
				if (systematicActivityType == OLI_SYSACTTYPE_CHARGES) {
					if (getSubAccount().hasPolicyChargePct()) {
						total += getSubAccount().getPolicyChargePct();
					}
				} else if (getSubAccount().hasAllocPercent()) {
					total += getSubAccount().getAllocPercent();
				}
			}
		}
		return total;
	}
	/**
	 * Return the UnderwritingClassProduct with the minimum age.
	 * @param coverage the Coverage
	 * @param covOption the CovOption
	 * @return the UnderwritingClassProduct
	 */
	// SPR1996 New Method //P2AXAL055 changed method signature
	protected UnderwritingClassProduct getUnderwritingClassProductMinAge(Coverage coverage,CovOption covOption,LifeParticipant lifeParticipant) {
		UnderwritingClassProduct underwritingClassProduct = null;
		if(lifeParticipant==null){//P2AXAL055
			lifeParticipant = NbaUtils.getInsurableLifeParticipant(getCoverage());
		}
		if (lifeParticipant != null) {
			long gender = lifeParticipant.getIssueGender();
			long smokerStat = getSmokerStat(lifeParticipant);
			CovOptionProduct covOptionProduct = getCovOptionProductFor(coverage, covOption);
			if (covOptionProduct != null) {
				underwritingClassProduct = getUnderwritingClassProductMinAgeFor(covOptionProduct.getUnderwritingClassProduct(), smokerStat, gender);
			}
		}
		return underwritingClassProduct;
	}
	// AXAL3.7.40 - Removed code
	/**
	 * Get the smokerstat for a LifeParticipant
	 * @param lifeParticipant
	 * @return smokerStat
	 */
	// SPR1996 New Mehod
	protected long getSmokerStat(LifeParticipant lifeParticipant) {
		long smokerStat = OLI_TOBACCO_NEVER;
		if (lifeParticipant.hasSmokerStat()) {
			return lifeParticipant.getSmokerStat();
		}
		if (findParty(lifeParticipant.getPartyID()) && (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson())) {
			smokerStat = getParty().getPersonOrOrganization().getPerson().getSmokerStat();
		}
		return smokerStat;
	}
	/**
	 * Get the smokerstat for a Participant
	 * @param participant
	 * @return smokerStat
	 */
	// SPR1996 New Mehod
	protected long getSmokerStat(Participant participant) {
		long smokerStat = OLI_TOBACCO_NEVER;
		if (findParty(participant.getPartyID()) && (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson())) {
			smokerStat = getParty().getPersonOrOrganization().getPerson().getSmokerStat();
		}
		return smokerStat;
	}
	// AXAL3.7.40 - Removed code
	// SPR1996 code deleted
	/**
	 * Return the UnderwritingClassProduct from the CovOptionProduct for the gender.
	 * @param rider the Rider
	 * @param covOption the CovOption
	 * @param gender the gender 
	 * @return the UnderwritingClassProduct
	 */
	protected UnderwritingClassProduct getUnderwritingClassProduct(Rider rider, CovOption covOption) { //SPR1996 changed method signature
		UnderwritingClassProduct underwritingClassProduct = null;
		Participant participant = NbaUtils.getInsurableParticipant(rider);	//SPR1996		
		if (participant != null) {	//SPR1996		
			CovOptionProduct covOptionProduct = getCovOptionProductFor(rider, covOption);
			if (covOptionProduct != null) {
				long gender = participant.getIssueGender();	//SPR1996		
				long smokerStat = getSmokerStat(participant);	//SPR1996		
				long age = participant.getIssueAge();			//SPR1996				 
				underwritingClassProduct = getUnderwritingClassProductFor(covOptionProduct.getUnderwritingClassProduct(), smokerStat, gender, age);	//SPR1996		
			}
		}	//SPR1996		
		return underwritingClassProduct;
	}
	/**
	 * Return the UnderwritingClassProductExtension for a UnderwritingClassProduct.
	 * @param UnderwritingClassProduct the UnderwritingClassProduct
	 * @return the UnderwritingClassProductExtension
	 */
	//P2AXAL024 method visibility changed to public
	public UnderwritingClassProductExtension getUnderwritingClassProductExtensionFor(UnderwritingClassProduct underwritingClassProduct) {
		UnderwritingClassProductExtension UnderwritingClassProductExtension = null;
		if (underwritingClassProduct == null) {//QC8011
			return UnderwritingClassProductExtension;
		}
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		int extCount = underwritingClassProduct.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			extension = underwritingClassProduct.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isUnderwritingClassProductExtension()) {
					UnderwritingClassProductExtension = extension.getUnderwritingClassProductExtension();
				}
			}
		}
		return UnderwritingClassProductExtension;
	}
	/**
	 * Locate the UnderwritingClassProduct matching the Smoker Status, Gender and Age (if applicable). 
	 * If the plan varies by issue age, UnderwritingClassProductExtension.MinIssueTableIdentity and/or 
	 * MaxIssueTableIdentity will be <> null.
	 * @param underwritingClassProducts - ArrayList of UnderwritingClassProduct
	 * @param smokerStat 
	 * @param gender
	 * @param age
	 * @return UnderwritingClassProduct
	 */
	// SPR1996 New Method
	protected UnderwritingClassProduct getUnderwritingClassProductFor(ArrayList underwritingClassProducts, long smokerStat, long gender, long age) {
		UnderwritingClassProduct underwritingClassProduct = null;
		// SPR3290 code deleted
		UnderwritingClassProduct temp;
		UnderwritingClassProductExtension ext;
		long issueGender;
		int prevMin = -1;
		int prevMax = 1000;
		int min;
		int max;
		for (int j = 0; j < underwritingClassProducts.size(); j++) {
			temp = (UnderwritingClassProduct) underwritingClassProducts.get(j);
			if (!temp.hasSmokerStat() || temp.getSmokerStat() == smokerStat) {
				issueGender = temp.getIssueGender();
				if (issueGender == -1L || issueGender == OLI_GENDER_UNISEX || issueGender == OLI_GENDER_COMBINED || issueGender == gender) { 
					ext = getUnderwritingClassProductExtensionFor(temp);
					if (ext != null && (ext.hasMinIssueTableIdentity() || ext.hasMaxIssueTableIdentity())) { //Varies by age
						min = 0;
						max = 999;
						if (ext.hasMinIssueTableIdentity()) {
							min = temp.getMinIssueAge();
						}
						if (ext.hasMaxIssueTableIdentity()) {
							max = temp.getMaxIssueAge();
						}
						if (!(age < min || age > max)) { // not outside of range
							if (min > prevMin || max < prevMax) {
								prevMin = min;
								prevMax = max;
								underwritingClassProduct = temp;
							}
						}
					} else {	//Does not vary by age
						underwritingClassProduct = temp;
						break;
					}
				}
			}
		}
		return underwritingClassProduct;
	}
	/**
	 * Locate the UnderwritingClassProduct with the minimum Age matching the Smoker Status and Gender. 
	 * If the plan varies by issue age, UnderwritingClassProductExtension.MinIssueTableIdentity and/or 
	 * MaxIssueTableIdentity will be <> null.
	 * @param underwritingClassProducts - ArrayList of UnderwritingClassProduct
	 * @param smokerStat 
	 * @param gender
	 * @return UnderwritingClassProduct
	 */
	// SPR1996 New Method
	protected UnderwritingClassProduct getUnderwritingClassProductMinAgeFor(ArrayList underwritingClassProducts, long smokerStat, long gender) {
		UnderwritingClassProduct underwritingClassProduct = null;
		// SPR3290 code deleted
		UnderwritingClassProduct temp;
		UnderwritingClassProductExtension ext;
		long issueGender;
		int prevMin = 9999;
		int min;
		for (int j = 0; j < underwritingClassProducts.size(); j++) {
			temp = (UnderwritingClassProduct) underwritingClassProducts.get(j);
			if (!temp.hasSmokerStat() || temp.getSmokerStat() == smokerStat) {
				issueGender = temp.getIssueGender();
				if (issueGender == -1L || issueGender == OLI_GENDER_UNISEX || issueGender == OLI_GENDER_COMBINED || issueGender == gender) {
					ext = getUnderwritingClassProductExtensionFor(temp);
					if (ext != null && (ext.hasMinIssueTableIdentity())) { //Varies by age
						min = temp.getMinIssueAge();
						if (min < prevMin) {
							prevMin = min;
							underwritingClassProduct = temp;
						}
					} else { //Does not vary by age
						underwritingClassProduct = temp;
						break;
					}
				}
			}
		}
		return underwritingClassProduct;
	}
	/**
	 * Locate the UnderwritingClassProduct with the maximum Age matching the Smoker Status and Gender. 
	 * If the plan varies by issue age, UnderwritingClassProductExtension.MinIssueTableIdentity and/or 
	 * MaxIssueTableIdentity will be <> null.
	 * @param underwritingClassProducts - ArrayList of UnderwritingClassProduct
	 * @param smokerStat 
	 * @param gender
	 * @return UnderwritingClassProduct
	 */
	// SPR1996 New Method
	protected UnderwritingClassProduct getUnderwritingClassProductMaxAgeFor(ArrayList underwritingClassProducts, long smokerStat, long gender) {
		UnderwritingClassProduct underwritingClassProduct = null;
		// SPR3290 code deleted
		UnderwritingClassProduct temp;
		UnderwritingClassProductExtension ext;
		long issueGender;
		int prevMax = -1;
		int max;
		for (int j = 0; j < underwritingClassProducts.size(); j++) {
			temp = (UnderwritingClassProduct) underwritingClassProducts.get(j);
			if (!temp.hasSmokerStat() || temp.getSmokerStat() == smokerStat) {
				issueGender = temp.getIssueGender();
				if (issueGender == -1L || issueGender == OLI_GENDER_UNISEX || issueGender == OLI_GENDER_COMBINED || issueGender == gender) {
					ext = getUnderwritingClassProductExtensionFor(temp);
					if (ext != null && (ext.hasMaxIssueTableIdentity())) { //Varies by age
						max = temp.getMaxIssueAge();
						if (max > prevMax) {
							prevMax = max;
							underwritingClassProduct = temp;
						}
					} else { //Does not vary by age
						underwritingClassProduct = temp;
						break;
					}
				}
			}
		}
		return underwritingClassProduct;
	}
	/**
	 * Locate the UnderwritingClassProduct for the Rider matching the Smoker Status, Gender and Age (if applicable). 
	 * @param Rider the Rider
	 * @return UnderwritingClassProduct
	 */
	protected UnderwritingClassProduct getUnderwritingClassProductFor(Rider rider) {
		UnderwritingClassProduct underwritingClassProduct = null;
		Participant participant = NbaUtils.getInsurableParticipant(rider);
		if (participant != null) {
			long gender = participant.getIssueGender();
			long age = participant.getIssueAge();	//SPR1996
			long smokerStat = participant.getSmokerStat();
			underwritingClassProduct = getUnderwritingClassProductFor(rider, smokerStat, gender, age);	//SPR1996
		}
		return underwritingClassProduct;
	}
	/**
	 * Locate the UnderwritingClassProduct for the coverage matching the Smoker Status, Gender and Age (if applicable). 
	 * @param Rider the Rider
	 * @param smokerStat 
	 * @param gender
	 * @param age
	 * @return UnderwritingClassProduct
	 */
	protected UnderwritingClassProduct getUnderwritingClassProductFor(Rider rider, long smokerStat, long gender, long age) {	//SPR1996 added age to method signature
		UnderwritingClassProduct underwritingClassProduct = null;
		CoverageProduct coverageProduct = getCoverageProductFor(rider);
		if (coverageProduct != null) {
			underwritingClassProduct = getUnderwritingClassProductFor(coverageProduct.getUnderwritingClassProduct(), smokerStat, gender, age); //SPR1996
		}
		return underwritingClassProduct;
	}
	/**
	 * Return the Writing Agent relation if it exists.
	 * @return Relation
	 */
	protected Relation getWritingAgentRelation() {
		return getRelation(OLI_REL_PRIMAGENT);
	}
	//Code Deleted NBA139
	/**
	 * Returns true if there is a rider for a person with the specified role.
	 * @return Life
	 */
	protected boolean hasAnnuityRiderRole(String[] roles) {
		boolean roleFound = false;
		if (getAnnuity() != null) {
			if (!getAnnuity().isActionDelete()) {
				int riderCount = getAnnuity().getRiderCount();
				for (int rdrIdx = 0; rdrIdx < riderCount; rdrIdx++) {
					Rider rider = annuity.getRiderAt(rdrIdx);
					if (!rider.isActionDelete()) {
						int participantCount = rider.getParticipantCount();
						for (int partIdx = 0; partIdx < participantCount; partIdx++) {
							Participant participant = rider.getParticipantAt(partIdx);
							if (roleMatch(participant.getParticipantRoleCode(), roles)) {
								roleFound = true;
								break;
							}
						}
					}
				}
			}
		}
		return roleFound;
	}
	/**
	 * Returns true if there is a coverage or rider for an other insured person.
	 * @return Life
	 */
	protected boolean hasCoverageOrRiderForOtherInsured() {
		//begin SPR1818
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty =
				getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty != null) {
			if (LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty.isLife()) {
				Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
				if (!life.isActionDelete()) {
					int coverageCount = getLife().getCoverageCount();
					for (int covIdx = 0; covIdx < coverageCount; covIdx++) {
						Coverage coverage = getLife().getCoverageAt(covIdx);
						if (!coverage.isActionDelete()) {
							int lifeParticipantCount = coverage.getLifeParticipantCount();
							for (int partIdx = 0; partIdx < lifeParticipantCount; partIdx++) {
								LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(partIdx);
								if (!lifeParticipant.isActionDelete()) {
									if (NbaUtils.isInsuredParticipantRoleCode(lifeParticipant.getLifeParticipantRoleCode())
											&& !isPrimaryInsured(lifeParticipant.getPartyID())) {
										return true;
									}
								}
							}
						}
					}
				}
			} else if (LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty.isAnnuity()) {
				if (getAnnuity() != null) {
					if (!getAnnuity().isActionDelete()) {
						int riderCount = getAnnuity().getRiderCount();
						for (int rdrIdx = 0; rdrIdx < riderCount; rdrIdx++) {
							Rider rider = annuity.getRiderAt(rdrIdx);
							if (!rider.isActionDelete()) {
								int participantCount = rider.getParticipantCount();
								for (int partIdx = 0; partIdx < participantCount; partIdx++) {
									Participant participant = rider.getParticipantAt(partIdx);
									if (!participant.isActionDelete()) {
										if (NbaUtils.isInsuredParticipantRoleCode(participant.getParticipantRoleCode())
												&& !isPrimaryInsured(participant.getPartyID())) {
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
		//end SPR1818
	}
	/**
	 * Returns true if there is a coverage or rider with a beneficiary.
	 * @return Life
	 */
	protected boolean hasCoverageOrRiderRole(String[] roles) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty =
				getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty != null) {
			if (LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty.isLife()) {
				return hasLifeCoverageRole(roles);
			} else if (LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty.isAnnuity()) {
				return hasAnnuityRiderRole(roles);
			}
		}
		return false;
	}
	/**
	 * Determine if duplicate SubAccounts are present for a systematicActivityType for an Investment
	 * @param investment - the investment object
	 * @param systematicActivityType
	 * @return boolean;
	 */
	protected boolean hasDuplicateSubAccount(Investment investment, long systematicActivityType) {
		Map map = new HashMap();
		for (int i = 0; i < investment.getSubAccountCount(); i++) {
			SubAccount subAccount = investment.getSubAccountAt(i);
			if (subAccount.getSystematicActivityType() == systematicActivityType) {
				String productCode = subAccount.getProductCode();
				if (map.containsKey(productCode)) {
					return true;
				} else {
					map.put(productCode, productCode);
				}
			}
		}
		return false;
	}
	/**
	 * Returns true if there is a coverage for a person with the specified role.
	 * @return Life
	 */
	protected boolean hasLifeCoverageRole(String[] roles) {
		boolean roleFound = false;
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
		if (!life.isActionDelete()) {
			int coverageCount = getLife().getCoverageCount();
			mainLoop : for (int covIdx = 0; covIdx < coverageCount; covIdx++) {
				Coverage coverage = getLife().getCoverageAt(covIdx);
				if (!coverage.isActionDelete()) {
					int lifeParticipantCount = coverage.getLifeParticipantCount();
					for (int partIdx = 0; partIdx < lifeParticipantCount; partIdx++) {
						LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(partIdx);
						if (!lifeParticipant.isActionDelete()) {
							if (roleMatch(lifeParticipant.getLifeParticipantRoleCode(), roles)) {
								roleFound = true;
								break mainLoop;
							}
						}
					}
				}
			}
		}
		return roleFound;
	}
	/**
	 * Perform one time initialization for the instance.
	 */
	//NBA237 changed method signature
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVO) { //AXAL3.7.18
		setNbaDst(nbaDst);
		setNbaTXLife(nbaTXLife);
		setCurrentSubSet(subset);
		setNbaOLifEId(nbaOLifEId);
		setNbaProduct(nbaProduct);
		setUserVO(userVO); //AXAL3.7.18
		serviceMap = new HashMap();  //P2AXAL068
		serviceMap.put("DefaultValidator", ServiceLocator.lookup("newBusiness/comp/DefaultValidator")); //P2AXAL068
	}
	/**
	 * Initialize the process table.
	 */
	protected void initProcesses() {
		processes = new HashMap();
	}
	/**
	 * Initialize the timer.
	 * @return java.text.DateFormat
	 */
	protected void initTimer() {
		setStartTime(Calendar.getInstance().getTime());
	}
	/**
	 * Determine if the Party is an Annuitant by determining if 
	 * the person has a Annuitant Relation
	 * @param insuredId - the insured id
	 * @return true if the insured is the primary insured
	 */
	protected boolean isAnnuitant(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && NbaUtils.isAnnuitantRelation(relation)) { //SPR1705
					return true;
				}
			}
		}
		return false;
	}
	//SPR1705 deleted code
	/**
	 * Return true if Automatic Withdrawal is an Interest Withdrawal Arrangement object.
	 * 42=Interest Between WD's
	 * 44=Project Interest
	 */
	protected boolean isAutoWdwlIntArrangement() {
		if (getArrangement() != null) {
			long type = getArrangement().getArrType();
			if (type == 42 || type == 44) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Return true if Automatic Withdrawal is a Percentage Arrangement object.
	 * 14= Percent of Value Withdrawal
	 * 43 = Interest Earned From Issue
	 */
	protected boolean isAutoWdwlPctArrangement() {
		if (getArrangement() != null) {
			long type = getArrangement().getArrType();
			if (type == OLI_ARRTYPE_PCTVALWITH || type == 43) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Return true if Automatic Withdrawal is a Specified Amount Arrangement object.
	 */
	protected boolean isAutoWdwlSpecAmtArrangement() {
		if (getArrangement() != null) {
			long type = getArrangement().getArrType();
			if (type == OLI_ARRTYPE_SPECAMTNETWITH || type == OLI_ARRTYPE_SPECAMTGROSSWITH) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Determine if the Party can be insured by determining if 
	 * the person has an insurable Relation
	 * @param insuredId - the party id
	 * @return true if the insured can be insurable
	 */
	protected boolean isInsurable(String partyId) {
		return getNbaTXLife().isInsured(partyId);		//SPR1747
	}
	/**
	 * Determine if the Annuity is a Joint Annuity
	 * AnnuityProduct.FeatureProduct.FeatureOptProduct.LivesType tc=(2-5).
	 * @return true if the Annuity is a Joint Annuity 
	 */
	protected boolean isJointAnnuity() {
		AnnuityProduct annuityProduct = getAnnuityProductForPlan();
		if (annuityProduct != null) {
			return isJointAnnuity(annuityProduct);
		}
		return false;
	}
	/**
	 * Determine if the Annuity is a Joint Annuity
	 * AnnuityProduct.FeatureProduct.FeatureOptProduct.LivesType tc=(2-5).
	 * @param annuityProduct the AnnuityProduct
	 * @return true if the Annuity is a Joint Annuity 
	 */
	protected boolean isJointAnnuity(AnnuityProduct annuityProduct) {
		for (int i = 0; i < annuityProduct.getFeatureProductCount(); i++) {
			FeatureProduct featureProduct = annuityProduct.getFeatureProductAt(i);
			for (int j = 0; j < featureProduct.getFeatureOptProductCount(); j++) {
				FeatureOptProduct featureOptProduct = featureProduct.getFeatureOptProductAt(j);
				long livesType = featureOptProduct.getLivesType();
				if (livesType == OLI_COVLIVES_JOINTFTD
						|| livesType == OLI_COVLIVES_JOINTLTD
						|| livesType == OLI_COVLIVES_MULTFTD
						|| livesType == OLI_COVLIVES_MULTLTD) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Return true if the payemnt method indicates that the payment will be 
	 * automatically drafted. This includes: 
	 * List Bill (tc="5")
	 * Payroll Deduction (tc="6")
	 * EFT (tc="7")
	 * Government Allotment (tc="8")
	 * Credit Card (tc="9")
	 * Pre-authorized Check (tc="26")
	 */
	protected boolean isMethodAutoDraft(long paymentMethod) {
		return paymentMethod == OLI_PAYMETH_LISTBILL
				|| paymentMethod == OLI_PAYMETH_PAYROLL
				|| paymentMethod == OLI_PAYMETH_ETRANS
				|| paymentMethod == OLI_PAYMETH_GOVALLOT
				|| paymentMethod == OLI_PAYMETH_CREDCARD
				|| paymentMethod == OLI_PAYMETH_PAC;
	}
	/**
	 * Returns true if the date is not greater than the current date.
	 * @return boolean
	 */
	protected boolean isNotFutureDated(Date date) {
		return !date.after(getCurrentDate());
	}
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an owner
	 */
	protected boolean isOwner(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_OWNER) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the party is a payor or not
	 * the person has a Payor Relation
	 * @param partyId - the party id
	 * @return true if the party is a Payor
	 */
	//APSL3351 Added check for OriginatingObjectID
	protected boolean isPayor(String partyId,String orgObjID) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_PAYER) {
					if (orgObjID != null) {
						if (relation.getOriginatingObjectID().equals(orgObjID)) {
							return true;
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	// End APSL3351
	/**
	 * Determine if the Party is the Primary Annuitant by determining if 
	 * the person has a Primary Annuitant Relation
	 * @param insuredId - the insured id
	 * @return true if the insured is the primary Annuitant
	 */
	protected boolean isPrimaryAnnuitant(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_ANNUITANT) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the insured is the primary insured by determining if 
	 * the person is the insured on the primary coverage.
	 * @param insuredId - the insured id
	 * @return true if the insured is the primary insured
	 */
	protected boolean isPrimaryInsured(String insuredId) {
		boolean continueProcess = true;
		int idx = 0;
		while (continueProcess) {
			Coverage coverage = findCoverageForId(insuredId, idx++,true);//SPR2731
			if (coverage == null) {
				continueProcess = false;
			} else if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
				return true;
			}
		}
		return false;
	}

	//SPR3098 method deleted

	/**
	 * Determine if a CovOption is rated
	 * @param CovOption
	 * @return true if the CovOption has a SubStandard Rating
	 */
	protected boolean isRated(CovOption covOption) {
		return covOption.hasPermFlatExtraAmt()
				|| covOption.hasPermTableRating()
				|| covOption.hasTempFlatExtraAmt()
				|| covOption.hasTempTableRating()
				|| covOption.hasPermPercentageLoading();  //SPR1839
	}
	/**
	 * Return true if the system id is CyberLife.
	 */
	protected boolean isSystemIdCyberLife() {
		return getSystemId().equals(SYST_CYBERLIFE);
	}
	/**
	 * Return true if the system id is Vantage.
	 */
	protected boolean isSystemIdVantage() {
		return getSystemId().equals(SYST_VANTAGE);
	}
	/**
	 * Return true if the state code is valid.
	 */
	protected boolean isValidCompany(String companyCode) {
		return companyCode != null && isValidTableValue(NbaTableConstants.NBA_COMPANY, companyCode);
	}
	/**
	 * Returns true if the date is not before 1/1/1900 and not greater than the current date.
	 * @return boolean
	 */
	protected boolean isValidDate(Date date) {
		return !date.before(NbaUtils.getDateFromStringInUSFormat("01/01/1900"));
	}
	/**
	 * Return true if the state code is valid.
	 */
	protected boolean isValidState(long stateCode) {
		return stateCode > -1L && isValidState(Long.toString(stateCode));
	}
	/**
	 * Return true if the state code is valid.
	 */
	protected boolean isValidState(String stateCode) {
		try {
			return stateCode != null
					&& getNbaTableAccessor().getDataForOlifeValue(getTblKeys(), NbaTableConstants.NBA_STATES, stateCode) != null;
		} catch (NbaDataAccessException e) {
			return false;
		}
	}
	/**
	 * Determine if a table entry is present.
	 * @param table the table name
	 * @param value the value to search for
	 * @return true if an entry is present in the table for the value
	 */
	protected boolean isValidTableValue(String table, long value) {
		return isValidTableValue(table, String.valueOf(value));
	}
	/**
	 * Determine if a table entry is present.
	 * @param table the table name
	 * @param value the value to search for
	 * @return true if an entry is present in the table for the value
	 */
	protected boolean isValidTableValue(String table, String value) {
		try {
			return getNbaTableAccessor().getDataForOlifeValue(getTblKeys(), table, value) != null;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Determine if the party is a writing agent by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is a writing agent
	 */
	protected boolean isWritingAgent(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && NbaUtils.isPrimaryWritingAgentRelation(relation)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the party is a servicing agent or agency by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is a servicing agent or agency
	 */
	//NBA112 New Method
	protected boolean isServicingAgent(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if ((relation.getRelatedObjectID().equals(partyId) && NbaUtils.isServicingAgentRelation(relation))
						|| (relation.getRelatedObjectID().equals(partyId) && NbaUtils.isServicingAgencyRelation(relation))) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the party is an agent by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an agent
	 */
	//SPR1705 new method
	protected boolean isAgent(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && NbaUtils.isAgentRelation(relation)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Determine if a Relation has a relation role code which is is an Agent role.
	 * 
	 * @param relation
	 *            the Relation
	 * @return true if the relation has a relationt role code which is is an Agent role.
	 */
	// NBLXA1850
	protected boolean isNCFParty(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && NbaUtils.isNCFRelation(relation)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Log the elapsed time.
	 */
	protected void logElapsedTime(String message) {
		if (getLogger().isInfoEnabled()) {
			if (getStartTime() != null) {
				Date endTime = java.util.Calendar.getInstance().getTime();
				float elapsed = ((float) (endTime.getTime() - startTime.getTime())) / 1000;
				StringBuffer elStr = new StringBuffer();
				elStr.append(message);
				elStr.append(" Elapsed time: ");
				elStr.append(elapsed);
				elStr.append(" seconds ");
				getLogger().logInfo(elStr.toString());
			}
		}
	}
	/**
	 * Write a message to the log file.
	 */
	protected void logNewMessage(SystemMessage msg) {
		if (getLogger().isInfoEnabled()) {
			StringBuffer buf = new StringBuffer();
			buf.append("Creating validation message number: ");
			buf.append(msg.getMessageCode());
			buf.append(", severity: ");
			buf.append(msg.getMessageSeverityCode());
			buf.append(", subset: ");
			SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(msg);
			buf.append(systemMessageExtension.getMsgValidationType());
			if (getNbaConfigValProc() != null) {
				buf.append(", process: ");
				buf.append(getNbaConfigValProc().getId());
			}
			buf.append(", description: ");
			buf.append(msg.getMessageDescription());
			buf.append(", reference object id: ");
			buf.append(msg.getRelatedObjectID());
			getLogger().logInfo(buf.toString());
		}
	}
	/**
	 * Perform a VPMS calculation and return the results.
	 */
	protected NbaVpmsResultsData performVpmsCalculation() {
		return performVpmsCalculation(true);	//NBA100
	}
	/**
	 * Perform a VPMS calculation and return the results.
	 */
	// NBA100 New Method
	protected NbaVpmsResultsData performVpmsCalculation(boolean formattedDate) {
		Map skipAttributes = new HashMap();
		return performVpmsCalculation(skipAttributes, null, formattedDate);
	}
	/**
	 * Perform a VPMS calculation and return the results.
	 */
	//SPR1778 New Method
	protected NbaVpmsResultsData performVpmsCalculation(Map skipAttributes) {		
		return performVpmsCalculation(skipAttributes, null, true);	//NBA100
	}
	/**
	 * Perform a VPMS calculation and return the results.
	 */
	// NBA100 added new parameter, formattedDate
	protected NbaVpmsResultsData performVpmsCalculation(Map skipAttributes, NbaOinkRequest nbaOinkRequest, boolean formattedDate) {
		NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(getNbaDst().getNbaLob());
		NbaVpmsAdaptor nbaVpmsAdaptor = null; //SPR3362
		try {
			nbaOinkDataAccess.setContractSource(getNbaTXLife());
			nbaOinkDataAccess.setPlanSource(getNbaTXLife(), getNbaProduct());//P2AXAL016
			if (formattedDate){	//NBA100
				nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
			}	//NBA100
			nbaVpmsAdaptor = new NbaVpmsAdaptor(nbaOinkDataAccess, getNbaConfigValProc().getModel()); //SPR3362
			nbaVpmsAdaptor.setSkipAttributesMap(skipAttributes);
			//begin SPR1778
			if(nbaOinkRequest != null){
				nbaVpmsAdaptor.setANbaOinkRequest(nbaOinkRequest);
				nbaOinkDataAccess.setAcdbSource(new NbaAcdb(), getNbaTXLife());  //AXAL3.7.40
			}
			//end SPR1778
			nbaVpmsAdaptor.setVpmsEntryPoint(getNbaConfigValProc().getEntrypoint());
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(nbaVpmsAdaptor.getResults());
			//SPR3362 code deleted
			if (nbaVpmsResultsData != null) {
				if (!nbaVpmsResultsData.wasSuccessful()) {
					VpmsComputeResult vpmsComputeResult = nbaVpmsResultsData.getResult();
					addNewSystemMessage(
							INVALID_VPMS_CALC,
							concat(getNbaConfigValProc().getId(), ".", getNbaConfigValProc().getModel(), ".")
							+ concat(getNbaConfigValProc().getEntrypoint(), ": ", vpmsComputeResult.toString(), ""),
							"");
				}
			}
			return nbaVpmsResultsData;
		} catch (NbaVpmsException e) {
			addNewSystemMessage(
					INVALID_VPMS_CALC,
					concat("Process: ", getNbaConfigValProc().getId(), " NbaVpmsException ", e.toString()),
					"");
		} catch (RemoteException e) {
			addNewSystemMessage(
					INVALID_VPMS_CALC,
					concat("Process: ", getNbaConfigValProc().getId(), " RemoteException ", e.toString()),
					"");
		} catch (NbaBaseException e) {
			addNewSystemMessage(
					INVALID_VPMS_CALC,
					concat("Process: ", getNbaConfigValProc().getId(), " NbaBaseException ", e.toString()),
					"");
		} catch (Throwable e) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Exception ", e.toString()), "");
			//begin SPR3362
		} finally {
			try {
				if (nbaVpmsAdaptor != null) {
					nbaVpmsAdaptor.remove();					
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
			}
		}
		//end SPR3362
		return null;
	}
	/**
	 * Returns true the role matches one in the array.
	 * @return Life
	 */
	protected boolean roleMatch(long role, String[] roles) {
		String strRole = String.valueOf(role);
		for (int i = 0; i < roles.length; i++) {
			if (strRole.equals(roles[i])) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Sets the address.
	 * @param address The address to set
	 */
	protected void setAddress(Address address) {
		this.address = address;
	}
	/**
	 * Sets the annuity.
	 * @param annuity The annuity to set
	 */
	protected void setAnnuity(Annuity annuity) {
		this.annuity = annuity;
	}
	/**
	 * Sets the applicationInfo.
	 * @param applicationInfo The applicationInfo to set
	 */
	protected void setApplicationInfo(ApplicationInfo applicationInfo) {
		this.applicationInfo = applicationInfo;
	}
	/**
	 * Sets the applicationInfoExtension.
	 * @param applicationInfoExtension The applicationInfoExtension to set
	 */
	protected void setApplicationInfoExtension(ApplicationInfoExtension applicationInfoExtension) {
		this.applicationInfoExtension = applicationInfoExtension;
	}
	/**
	 * Sets the arrangement.
	 * @param arrangement The arrangement to set
	 */
	protected void setArrangement(Arrangement arrangement) {
		this.arrangement = arrangement;
	}
	/**
	 * @param destination
	 */
	protected void setArrDestination(ArrDestination destination) {
		arrDestination = destination;
	}
	/**
	 * @param source
	 */
	protected void setArrSource(ArrSource source) {
		arrSource = source;
	}
	/**
	 * Sets the banking.
	 * @param banking The banking to set
	 */
	protected void setBanking(Banking banking) {
		this.banking = banking;
	}
	/**
	 * Sets the carrierAppointment.
	 * @param carrierAppointment The carrierAppointment to set
	 */
	protected void setCarrierAppointment(CarrierAppointment carrierAppointment) {
		this.carrierAppointment = carrierAppointment;
	}
	/**
	 * Sets the coverage.
	 * @param coverage The coverage to set
	 */
	protected void setCoverage(Coverage coverage) {
		this.coverage = coverage;
	}
	/**
	 * Sets the covOption.
	 * @param covOption The covOption to set
	 */
	protected void setCovOption(CovOption covOption) {
		this.covOption = covOption;
	}
	/**
	 * Sets the currentDate.
	 * @param currentDate The currentDate to set
	 */
	protected void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	/**
	 * Sets the currentSubSet.
	 * @param currentSubSet The currentSubSet to set
	 */
	protected void setCurrentSubSet(Integer currentSubSet) {
		this.currentSubSet = currentSubSet;
	}
	/**
	 * Sets the financialActivity.
	 * @param financialActivity The financialActivity to set
	 */
	protected void setFinancialActivity(FinancialActivity financialActivity) {
		this.financialActivity = financialActivity;
	}
	/**
	 * Sets the holding. 
	 * @param holding the holding to set
	 */
	protected void setHolding(Holding newHolding) {
		holding = newHolding;
	}
	/**
	 * Sets the investment.
	 * @param investment The investment to set
	 */
	protected void setInvestment(Investment investment) {
		this.investment = investment;
	}
	/**
	 * Sets the life.
	 * @param life The life to set
	 */
	protected void setLife(Life life) {
		this.life = life;
	}
	/**
	 * Sets the lifeParticipant.
	 * @param lifeParticipant The lifeParticipant to set
	 */
	protected void setLifeParticipant(LifeParticipant lifeParticipant) {
		this.lifeParticipant = lifeParticipant;
	}
	/**
	 * Sets the nbaConfigValProc.
	 * @param nbaConfigValProc The nbaConfigValProc to set
	 */
	// ACN012 changed signature
	protected void setNbaConfigValProc(ValProc nbaConfigValProc) {
		this.nbaConfigValProc = nbaConfigValProc;
	}
	/**
	 * Sets the nbaDst.
	 * @param nbaDst The nbaDst to set
	 */
	protected void setNbaDst(NbaDst nbaDst) {
		this.nbaDst = nbaDst;
	}
	/**
	 * Sets the nbaOLifEId.
	 * @param nbaOLifEId The nbaOLifEId to set
	 */
	protected void setNbaOLifEId(NbaOLifEId nbaOLifEId) {
		this.nbaOLifEId = nbaOLifEId;
	}
	/**
	 * Sets the nbaParty.
	 * @param nbaParty The nbaParty to set
	 */
	protected void setNbaParty(NbaParty nbaParty) {
		this.nbaParty = nbaParty;
	}
	/**
	 * Sets the NbaProduct.
	 * @param product the NbaProduct to set
	 */
	//NBA237 changed method signature //P2AXAL016 changed method signature
	public void setNbaProduct(AccelProduct product) {
		nbaProduct = product;
	}
	/**
	 * Sets the nbaTXLife.
	 * @param nbaTXLife The nbaTXLife to set
	 */
	protected void setNbaTXLife(NbaTXLife nbaTXLife) {
		this.nbaTXLife = nbaTXLife;
	}
	/**
	 * Set the objects from the Object array.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an array containing the objects identified in the ctl of nbaConfigValProc 
	 */
	protected void setObjects(ArrayList objects) throws IllegalAccessException, InvocationTargetException {
		for (int i = 0; i < objects.size(); i++) {
			Object x = objects.get(i);
			String qualifiedName = x.getClass().getName();
			int idx = qualifiedName.lastIndexOf(".");
			String className = qualifiedName.substring(idx + 1);
			if (getObjectMethods().containsKey(className)) {
				Method method = (Method) getObjectMethods().get(className);
				Object[] args = new Object[1];
				args[0] = x;
				method.invoke(this, args);
			}
		}
		generateRequirement = false;//AXAL3.7.40
	}
	/**
	 * Sets the oLifE.  
	 * @param oLifE The oLifE to set 
	 */
	protected void setOLifE(OLifE newOLifE) {
		oLifE = newOLifE;
	}
	/**
	 * Sets the organization.
	 * @param organization The organization to set
	 */
	protected void setOrganization(Organization organization) {
		this.organization = organization;
	}
	/**
	 * Sets the participant.
	 * @param participant The participant to set
	 */
	protected void setParticipant(Participant participant) {
		this.participant = participant;
	}
	/**
	 * Sets the party.
	 * @param party The party to set
	 */
	protected void setParty(Party party) {
		this.party = party;
	}
	/**
	 * Sets the payout.
	 * @param payout The payout to set
	 */
	protected void setPayout(Payout payout) {
		this.payout = payout;
	}
	/**
	 * Sets the person.
	 * @param person The person to set
	 */
	protected void setPerson(Person person) {
		this.person = person;
	}
	/**
	 * Sets the policy.  
	 * @param policy The policy to set 
	 */
	protected void setPolicy(Policy newPolicy) {
		policy = newPolicy;
	}
	/**
	 * Sets the policyExtension.
	 * @param policyExtension The policyExtension to set
	 */
	protected void setPolicyExtension(PolicyExtension policyExtension) {
		this.policyExtension = policyExtension;
	}
	/**
	 * Set the PolicyProductExtension for the Plan
	 * @param product
	 */
	protected void setPolicyProductExtensionForPlan(PolicyProductExtension policyProductExtension) {
		policyProductExtensionForPlan = policyProductExtension;
	}
	/**
	 * Set the PolicyProduct for the Plan
	 * @param product
	 */
	protected void setPolicyProductForPlan(PolicyProduct product) {
		policyProductForPlan = product;
	}
	/**
	 * Sets the processes.
	 * @param processes The processes to set
	 */
	protected void setProcesses(HashMap processes) {
		this.processes = processes;
	}
	/**
	 * Sets the producer.
	 * @param producer The producer to set
	 */
	protected void setProducer(Producer producer) {
		this.producer = producer;
	}
	/**
	 * Sets the relation.
	 * @param relation The relation to set
	 */
	protected void setRelation(Relation relation) {
		this.relation = relation;
	}
	/**
	 * Sets the requirementInfo.
	 * @param requirementInfo The requirementInfo to set
	 */
	protected void setRequirementInfo(RequirementInfo requirementInfo) {
		this.requirementInfo = requirementInfo;
	}
	/**
	 * Sets the rider.
	 * @param rider The rider to set
	 */
	protected void setRider(Rider rider) {
		this.rider = rider;
	}
	/**
	 * Sets the risk.
	 * @param risk The risk to set
	 */
	protected void setRisk(Risk risk) {
		this.risk = risk;
	}
	/**
	 * Sets the startTime.
	 * @param startTime The startTime to set
	 */
	protected void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * Sets the subAccount.
	 * @param subAccount The subAccount to set
	 */
	protected void setSubAccount(SubAccount subAccount) {
		this.subAccount = subAccount;
	}
	/**
	 * Sets the substandardRating.
	 * @param substandardRating The substandardRating to set
	 */
	protected void setSubstandardRating(SubstandardRating substandardRating) {
		this.substandardRating = substandardRating;
	}
	/**
	 * Sets the systemId.
	 * @param systemId The systemId to set
	 */
	protected void setSystemId(String systemId) {
		if (systemId == null) {
			this.systemId = "";
		} else {
			this.systemId = systemId;
		}
	}
	/**
	 * @param withholding
	 */
	protected void setTaxWithholding(TaxWithholding withholding) {
		taxWithholding = withholding;
	}
	/**
	 * Common validation processing. Invoke the processing routine identified
	 * in the nbaConfigValProc.
	 */
	// ACN012 changed signature
	public void validate(ValProc nbaConfigValProc, ArrayList objects) {
		if (getProcesses().containsKey(nbaConfigValProc.getId())) {
			try {
				setObjects(objects);
				setNbaConfigValProc(nbaConfigValProc);
				Method process = (Method) getProcesses().get(nbaConfigValProc.getId());
				process.setAccessible(true); //NBA297
				process.invoke(this, null);
			} catch (IllegalAccessException e) {
				getLogger().logException(e);//NBA103
				addNewSystemMessage(
						VALIDATION_PROCESSING,
						concat("Process id: ", nbaConfigValProc.getId(), ", IllegalAccessException: ", e.toString()),
						"");
			} catch (InvocationTargetException e) {
				getLogger().logException(e);//NBA103
				addNewSystemMessage(
						VALIDATION_PROCESSING,
						concat("Process id: ", nbaConfigValProc.getId(), " : ", e.getTargetException().getMessage()), //SPR1973
						""); //ALS4095
			}
		} else {
			addNewSystemMessage(INVALID_PROCESS_ID, concat("Processing routine process_", nbaConfigValProc.getId()), "");
		}
	}
	/**
	 * Determine if the last node of the Control Value matches the object expected by the process. 
	 * Post a validation error if it does not.
	 * @param expectedCtl - long
	 * @return boolean
	 */
	protected boolean verifyCtl(String expectedCtl) {
		if (getNbaConfigValProc().getCtl().endsWith(expectedCtl)) {
			return true;
		}
		addNewSystemMessage(
				INVALID_CTL_ID,
				concat("Process ", getNbaConfigValProc().getId(), " expected a control value of: ", expectedCtl),
				"");
		return false;
	}
	/**
	 * Retrieve the NonFortProv for the plan
	 * @return NonForProvision
	 */
	protected NonForProvision getNonForProvisionForPlan(long nonFortProv) {
		NonForProvision nonForProvision = null;
		PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
		if (policyProductExtension != null) {
			nonForProvision = getNonForProvision(policyProductExtension, nonFortProv);
		}
		return nonForProvision;
	}
	/**
	 * Retrieve the NonFortProv from a PolicyProductExtension mathcing the nonFortProv
	 * @return NonForProvision
	 */
	protected NonForProvision getNonForProvision(PolicyProductExtension policyProductExtension, long nonFortProv) {
		NonForProvision nonForProvision = null;
		for (int i = 0; i < policyProductExtension.getNonForProvisionCount(); i++) {
			if (policyProductExtension.getNonForProvisionAt(i).getNonFortProv() == nonFortProv) {
				nonForProvision = policyProductExtension.getNonForProvisionAt(i);
				break;
			}
		}
		return nonForProvision;
	}
	/**
	 * Retrieve the default NonFortProv for the plan
	 * @return NonForProvision
	 */
	protected NonForProvision getDefaultNonFortProvForPlan() {
		NonForProvision nonForProvision = null;
		PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
		if (policyProductExtension != null) {
			nonForProvision = getDefaultNonFortProv(policyProductExtension);
		}
		return nonForProvision;
	}
	/**
	 * Retrieve the default NonFortProv for a Coverage
	 * @return NonForProvision
	 */
	protected NonForProvision getDefaultNonFortProvFor(Coverage coverage) {
		NonForProvision nonForProvision = null;
		PolicyProduct policyProduct = getPolicyProductFor(coverage);
		if (policyProduct != null) {
			PolicyProductExtension policyProductExtension = getPolicyProductExtensionFor(policyProduct);
			if (policyProductExtension != null) {
				nonForProvision = getDefaultNonFortProv(policyProductExtension);
			}
		}
		return nonForProvision;
	}
	/**
	 * Retrieve the default NonFortProv from a PolicyProductExtension
	 * @return NonForProvision
	 */
	protected NonForProvision getDefaultNonFortProv(PolicyProductExtension policyProductExtension) {
		NonForProvision nonForProvision = null;
		for (int i = 0; i < policyProductExtension.getNonForProvisionCount(); i++) {
			if (policyProductExtension.getNonForProvisionAt(i).getDefaultInd()) {
				nonForProvision = policyProductExtension.getNonForProvisionAt(i);
				break;
			}
		}
		return nonForProvision;
	}
	/**
	 * Edit the number of funds against the maximum.
	 * @param max
	 */
	protected void editMaxFunds(int max) {
		int count = getSubAccountCount(getInvestment(), OLI_SYSACTTYPE_CHARGES);
		if (count > max) {
			addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Charge SubAccounts: ", count, "Maximum: ", max),
					getIdOf(getInvestment()));
		}
		count = getSubAccountCount(getInvestment(), OLI_SYSACTTYPE_WTHDRW);
		if (count > max) {
			addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Withdrawal SubAccounts: ", count, "Maximum: ", max),
					getIdOf(getInvestment()));
		}
		count = getSubAccountCount(getInvestment(), OLI_SYSACTTYPE_DOLLARCOSTAVG);
		if (count > max) {
			addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Dollar Cost Average SubAccounts: ", count, "Maximum: ", max),
					getIdOf(getInvestment()));
		}
		count = getSubAccountCount(getInvestment(), OLI_SYSACTTYPE_ASSETREALLOC);
		if (count > max) {
			addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Asset Reallocation SubAccounts: ", count, "Maximum: ", max),
					getIdOf(getInvestment()));
		}
		//begin SPR1806
		count = getSubAccountCount(getInvestment(), OLI_SYSACTTYPE_DEPT);
		if (count > max) {
			addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Deposits SubAccounts: ", count, "Maximum: ", max),
					getIdOf(getInvestment()));
		}
		//end SPR1806
	}
	/**
	 * Create an error message when the results data from a VP/MS calculation does not match the expected results.
	 * @param expected - the number of results expected
	 * @param actual - the number of actual results
	 */
	protected void addUnexpectedVpmsResultMessage(int expected, int actual) {
		addNewSystemMessage(
				INVALID_VPMS_CALC,
				concat("Process: ", getNbaConfigValProc().getId(), ", Unexpected result data length: ", Integer.toString(actual))
				+ concat(", Expected: ", Integer.toString(actual)),
				"");
	}
	/**
	 * Increment the date by the period in the NbaVpmsResultsData
	 * @return the adjusted date, or null if unable to calcualte
	 */
	protected Date addVpmsPeriod(Date date, NbaVpmsResultsData nbaVpmsResultsData) {
		if (nbaVpmsResultsData.getResultsData().size() == 2) {
			GregorianCalendar newg = new GregorianCalendar();
			newg.setTime(date);
			int type = 0;
			String strType = (String) nbaVpmsResultsData.getResultsData().get(0);
			if (strType.equals("M")) {
				type = Calendar.MONTH;
			} else if (strType.equals("D")) {
				type = Calendar.DAY_OF_YEAR;
			} else if (strType.equals("Y")) {
				type = Calendar.YEAR;
			} else {
				addNewSystemMessage(
						INVALID_VPMS_CALC,
						concat("Process: ", getNbaConfigValProc().getId(), " Invalid unit of measure: ", strType),
						"");
				return null;
			}
			String strDur = (String) nbaVpmsResultsData.getResultsData().get(1);
			int length = 0;
			try {
				length = Integer.valueOf(strDur).intValue();
			} catch (Throwable e) {
				addNewSystemMessage(
						INVALID_VPMS_CALC,
						concat("Process: ", getNbaConfigValProc().getId(), " Invalid duration: ", strDur),
						"");
				return null;
			}
			newg.add(type, length);
			return newg.getTime();
		} else {
			addUnexpectedVpmsResultMessage(2, nbaVpmsResultsData.getResultsData().size());
			return null;
		}
	}
	/**
	 * Increment the date by the number of months
	 * @return the adjusted date
	 */
	protected Date addPeriod(Date date, long period) {
		int months = 0;
		if (period == OLI_PAYMODE_ANNUAL) {
			months = 12;
		} else if (period == OLI_PAYMODE_QUARTLY) {
			months = 3;
		} else if (period == OLI_PAYMODE_MNTHLY) {
			months = 1;
		} else if (period == OLI_PAYMODE_BIANNUAL) {
			months = 6;
		}
		GregorianCalendar newg = new GregorianCalendar();
		newg.setTime(date);
		if (months > 0) {
			newg.add(Calendar.MONTH, months);
		}
		return newg.getTime();
	}

	/**
	 * Increment the date by the number of modes
	 * @return the adjusted date
	 */
	//SPR3562 New Method
	protected Date addPeriod(Date date, long period, int numModes) {
		int months = 0;
		if (period == OLI_PAYMODE_ANNUAL) {
			months = 12;
		} else if (period == OLI_PAYMODE_QUARTLY) {
			months = 3;
		} else if (period == OLI_PAYMODE_MNTHLY) {
			months = 1;
		} else if (period == OLI_PAYMODE_BIANNUAL) {
			months = 6;
		}
		GregorianCalendar newg = new GregorianCalendar();
		newg.setTime(date);
		newg.add(Calendar.MONTH, months * numModes);
		return newg.getTime();
	}

	/**
	 * Increment the date by the number of Years
	 * @return the adjusted date
	 */
	protected Date addYears(Date date, long years) {
		GregorianCalendar newg = new GregorianCalendar();
		newg.setTime(date);
		if (years > 0) {
			newg.add(Calendar.YEAR, new Long(years).intValue());
		}
		return newg.getTime();
	}
	// SPR2817 code deleted
	/**
	 * Set the FeeExtensionForPlan value
	 * @param FeeExtensionForPlan
	 */
	protected void setFeeExtensionForPlan(FeeExtension extension) {
		feeExtensionForPlan = extension;
	}
	/**
	 * Calculate IssueAge based on effective date of the contract, birth date 
	 * and PolicyProduct.AgeCalculationType (age calculation type - near or last birthday, etc).
	 */
	protected int calcIssueAge(String effDate, String birthDate, String ageRule) {
		Map skipAttributes = new HashMap();
		int age = 0;
		if (effDate == null) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid Effective date ", ""), "");
		} else if (birthDate == null) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid Birth date ", ""), "");
		} else if (ageRule == null) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid Age Rule ", ""), "");
		} else {
			skipAttributes.put("A_EffDate", effDate);
			skipAttributes.put("A_BirthDate_PINS", birthDate);//ALS5233
			skipAttributes.put("A_AgeRule", ageRule);
			NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(skipAttributes); //Get the  age
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {
					String strAage = (String) nbaVpmsResultsData.getResultsData().get(0);
					age = Integer.parseInt(strAage);
				} else {
					addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
				}
			}
		}
		return age;
	}
	/**
	 * Edit the initial premium amount against the limits.
	 */
	// NBA104 New Method
	protected void editInitialPremium(double initialPremAmt, double minInitialPremAmt, double maxInitialPremAmt) {
		if (initialPremAmt < minInitialPremAmt || initialPremAmt > maxInitialPremAmt) {
			addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat(
							concatAmt("Min initial amount: ", minInitialPremAmt),
							concatAmt(", Max initial amount: ", maxInitialPremAmt),
							concatAmt(", Requested amount: ", initialPremAmt),
							""),
							getIdOf(getPolicy()));
		}
	}
	//NBA142 code deleted
	/**
	 * Edit the initial premium amount against values from AnnuityProduct.QualTypeLimits.
	 */
	// NBA104 New Method
	protected void editInitialPremiumByQualType(long qualType, double initialPremAmt) {
		QualTypeLimits qualTypeLimits = getQualTypeLimits(qualType);
		if (qualTypeLimits == null) {
			addPlanInfoMissingMessage("QualTypeLimits", getIdOf(getPolicy()));
		} else {
			editInitialPremium(initialPremAmt, qualTypeLimits.getMinPremiumInitialAmt(), qualTypeLimits.getMaxPremiumInitialAmt());
		}
	}
	/**
	 * Edit the initial premium amount against values from Ownership.
	 */
	//SPR1705 new method
	protected void editInitialPremiumAgainstPlan(double initialPremAmt) {
		Ownership ownership = getOwnershipForPlan();
		if (ownership == null) {
			addPlanInfoMissingMessage("Ownership", getIdOf(getPolicy()));
		} else {
			// NBA104 deleted code
			editInitialPremium(initialPremAmt, ownership.getMinPremiumInitialAmt(), ownership.getMaxPremiumInitialAmt());  //NBA104
		}
	}
	/**
	 * Edit the periodic premium amount the limits.
	 */
	// NBA104 New Method
	protected void editPeriodicPremium(double periodicPremAmt, double minPeriodicPremAmt, double maxPeriodicPremAmt) {
		if (minPeriodicPremAmt > 0 || maxPeriodicPremAmt > 0) { //Bypass edit if both are zero
			if (periodicPremAmt < minPeriodicPremAmt || periodicPremAmt > maxPeriodicPremAmt) {
				addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concat(
								concatAmt("Min periodic amount: ", minPeriodicPremAmt),
								concatAmt(", Max periodic amount: ", maxPeriodicPremAmt),
								concatAmt(", Requested amount: ", periodicPremAmt),
								""),
								getIdOf(getPolicy()));
			}
		}
	}
	/**
	 * Edit the periodic premium amount against values from LifeProduct.
	 */
	// NBA104 New Method
	protected void editPeriodicPremiumForLife(double periodicPremAmt) {
		LifeProductExtension lifeProductExt = getLifeProductExtensionForPlan();
		if (lifeProductExt == null) {
			addPlanInfoMissingMessage("LifeProduct", getIdOf(getPolicy()));
		} else {
			editPeriodicPremium(periodicPremAmt, lifeProductExt.getMinPremiumAddOnAmt(), lifeProductExt.getMaxPremiumAddOnAmt());
		}
	}

	/**
	 * Edit the periodic premium amount against values from Ownership.
	 */
	//SPR1705 new method
	protected void editPeriodicPremiumAgainstPlan(double periodicPremAmt) {
		Ownership ownership = getOwnershipForPlan();
		if (ownership == null) {
			addPlanInfoMissingMessage("Ownership", getIdOf(getPolicy()));
		} else {
			// NBA104 deleted code
			editPeriodicPremium(periodicPremAmt, ownership.getMinPremiumAddOnAmt(), ownership.getMaxPremiumAddOnAmt());  //NBA104
		}
	}
	/**
	 * Edit the periodic premium amount against values from AnnuityProduct.QualTypeLimits.
	 */
	// NBA104 New Method
	protected void editPeriodicPremiumByQualType(long qualType, double periodicPremAmt) {
		QualTypeLimits qualTypeLimits = getQualTypeLimits(qualType);
		if (qualTypeLimits == null) {
			addPlanInfoMissingMessage("QualTypeLimits", getIdOf(getPolicy()));
		} else {
			editPeriodicPremium(periodicPremAmt, qualTypeLimits.getMinPremiumAddOnAmt(), qualTypeLimits.getMaxPremiumAddOnAmt());
		}
	}
	/**
	 * Locate the BirthDate from the Party matching the party id 
	 * @param partyID the party id
	 * @return the YYYY-MM-DD string value of the BirthDate or null
	 */
	protected String getBirthDateString(String partyID) {
		String birthDate = null;
		if (findParty(partyID)) {
			if (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson()) {
				if (getParty().getPersonOrOrganization().getPerson().hasBirthDate()) {
					birthDate = NbaUtils.getStringFromDate(getParty().getPersonOrOrganization().getPerson().getBirthDate());
				}
			}
		}
		return birthDate;
	}
	/**
	 * Retrieve the NbaCalculation for mode premium calculations.  
	 * @return NbaCalculation
	 */
	protected NbaCalculation getNbaModePremiumCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation =
						NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_MODE_PREMIUM, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);   //NBA104
				//NBA104 deleted code 
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");   //NBA104
			}
		}
		return nbaCalculation;
	}
	/**
	 * Retrieve and update the Mode Premium and Annual Premium amounts from the backend system. It also update non standard premium if 
	 * payment mode is a type of special frequency.
	 * @param Policy the policy object
	 */
	//NBA133 New Method
	protected void updateBESStandardModePremiums(Policy policy) {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_ALL_STD_MODES_PREMIUM, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				Policy calcPolicy = calcData.getPolicy();
				policy.setPaymentAmt(calcPolicy.getPaymentAmt());                
				policy.setActionUpdate();
				//begin NBA117
				if (isSystemIdCyberLife()) {
					policy.setAnnualPaymentAmt(calcPolicy.getAnnualPaymentAmt());
					PolicyExtension calcPolicyExt = NbaUtils.getFirstPolicyExtension(calcPolicy);
					if (calcPolicyExt != null) {
						PolicyExtension policyExt = getPolicyExtension();
						policyExt.setNonStandardBillAmt(calcPolicyExt.getNonStandardBillAmt());
						policyExt.setActionUpdate();
					}
				}
				//end NBA117
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(policy));
			}
		} catch (Throwable e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}

	/**
	 * Retrieve and update annualized premium amount for the coverage.
	 * @param coverage the Coverage object
	 */
	//NBA133 New Method
	protected void updateBackendPremiums(Coverage coverage) {
		NbaTXLife calcData = getBackendCoveragePremiumCalculation();
		if (calcData != null && !calcData.isTransactionError()) {
			Life calcLife = calcData.getLife();
			int count = calcLife.getCoverageCount();
			Coverage calcCov = null;
			for (int i = 0; i < count; i++) {
				calcCov = calcLife.getCoverageAt(i);
				if (coverage.getCoverageKey().equalsIgnoreCase(calcCov.getCoverageKey())) {
					coverage.setAnnualPremAmt(calcCov.getAnnualPremAmt());
					coverage.setActionUpdate();
					break;
				}
			}
		} else {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(coverage));
		}
	}

	/**
	 * Retrieve the coverages premium from backend system  
	 * @return the backend coverages premium
	 */
	//NBA133 New Method
	protected NbaTXLife getBackendCoveragePremiumCalculation() {
		if (backendCalcCoverage == null) {
			try {
				backendCalcCoverage = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
						NbaContractCalculationsConstants.CALC_TYPE_LIFE_COVERAGE_PREMIUM, getNbaTXLife());
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return backendCalcCoverage;
	}

	/**
	 * Retrieve and update annualized premium amount for the rider.
	 * @param rider the Rider object
	 */
	//NBA133 New Method
	protected void updateBackendPremiums(Rider rider) {
		NbaTXLife calcData = getBackendRiderPremiumCalculation();
		if (calcData != null && !calcData.isTransactionError()) {
			Annuity calcAnnuity = calcData.getAnnuity();
			int count = calcAnnuity.getRiderCount();
			Rider calcRider = null;
			for (int i = 0; i < count; i++) {
				calcRider = calcAnnuity.getRiderAt(i);
				if (rider.getRiderKey().equalsIgnoreCase(calcRider.getRiderKey())) {
					//rider.setAnnualPremAmt(calcRider.get()); //will support in future
					rider.setActionUpdate();
					break;
				}
			}
		} else {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(rider));
		}
	}

	/**
	 * Retrieve the rider premium from backend system  
	 * @return the backend rider premium
	 */
	//NBA133 New Method
	protected NbaTXLife getBackendRiderPremiumCalculation() {
		if (backendCalcRider == null) {
			try {
				backendCalcRider = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
						NbaContractCalculationsConstants.CALC_TYPE_ANNUITY_TERM_RIDER_PREMIUM, getNbaTXLife());
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return backendCalcRider;
	}

	/**
	 * Retrieve and update annualized premium amount for the cov option.
	 * @param covOpt the CovOption object
	 */
	//NBA133 New Method
	protected void updateBackendPremiums(CovOption covOpt) {
		if (getLife() != null) {
			updateBackendCovOptionPremiumsForCoverage(covOpt);
		} else {
			updateBackendCovOptionPremiumsForRider(covOpt);
		}
	}

	/**
	 * Updates cov option annualized premium amount for the life coverages.
	 * @param covOpt the CovOption object
	 */
	//NBA133 New Method
	protected void updateBackendCovOptionPremiumsForCoverage(CovOption covOpt) {
		NbaTXLife calcData = getBackendLifeCovOptionPremiumCalculation();
		if (calcData != null && !calcData.isTransactionError()) {
			Life calcLife = calcData.getLife();
			int count = calcLife.getCoverageCount();
			Coverage calcCov = null;
			CovOption calcCovOption = null;
			int covOptCount = -1;
			for (int i = 0; i < count; i++) {
				calcCov = calcLife.getCoverageAt(i);
				if (covOpt.getCovOptionKey().equalsIgnoreCase(calcCov.getCoverageKey())) { //matches with coverage key
					covOptCount = calcCov.getCovOptionCount();
					for (int j = 0; j < covOptCount; j++) {
						calcCovOption = calcCov.getCovOptionAt(j);
						if (covOpt.getProductCode().equalsIgnoreCase(calcCovOption.getProductCode())) {
							covOpt.setAnnualPremAmt(calcCovOption.getAnnualPremAmt());
							covOpt.setActionUpdate();
							break;
						}
					}
				}
			}
		} else {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(covOpt));
		}
	}

	/**
	 * Retrieve the coverage cov options premium from backend system  
	 * @return the backend coverage cov options premium
	 */
	//NBA133 New Method
	protected NbaTXLife getBackendLifeCovOptionPremiumCalculation() {
		if (backendCalcLifeCovOption == null) {
			try {
				backendCalcLifeCovOption = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
						NbaContractCalculationsConstants.CALC_TYPE_LIFE_COVERAGE_OPTION_PREMIUM, getNbaTXLife());
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return backendCalcLifeCovOption;
	}
	/**
	 * Updates substandard rating annualized premium amount for the life riders.
	 * @param covOpt the CovOption object
	 */
	//NBA133 New Method
	protected void updateBackendCovOptionPremiumsForRider(CovOption covOpt) {
		NbaTXLife calcData = getBackendRiderCovOptionPremiumCalculation();
		if (calcData != null && !calcData.isTransactionError()) {
			Annuity calcAnnuity = calcData.getAnnuity();
			int count = calcAnnuity.getRiderCount();
			Rider rider = null;
			CovOption calcCovOption = null;
			int covCount = -1;
			for (int i = 0; i < count; i++) {
				rider = calcAnnuity.getRiderAt(i);
				if (covOpt.getCovOptionKey().equalsIgnoreCase(rider.getRiderKey())) { //matches with rider key
					covCount = rider.getCovOptionCount();
					for (int j = 0; j < covCount; j++) {
						calcCovOption = rider.getCovOptionAt(j);
						if (covOpt.getProductCode().equalsIgnoreCase(calcCovOption.getProductCode())) {
							covOpt.setAnnualPremAmt(calcCovOption.getAnnualPremAmt());
							covOpt.setActionUpdate();
							break;
						}
					}
				}
			}

		} else {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(covOpt));
		}
	}

	/**
	 * Retrieve the rider covoptions premium from backend system  
	 * @return the backend rider covoptions premium
	 */
	//NBA133 New Method
	protected NbaTXLife getBackendRiderCovOptionPremiumCalculation() {
		if (backendCalcRiderCovOption == null) {
			try {
				backendCalcRiderCovOption = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
						NbaContractCalculationsConstants.CALC_TYPE_ANNUITY_RIDER_COVERAGE_OPTION_PREMIUM, getNbaTXLife());
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return backendCalcRiderCovOption;
	}

	/**
	 * Retrieve and update annualized premium amount for the substandard rating.
	 * @param substandardRating the SubstandardRating object
	 */
	//NBA133 New Method
	protected void updateBackendPremiums(SubstandardRating substandardRating) {
		NbaTXLife calcData = getBackendSubstandardRatingPremiumCalculation();
		if (calcData != null && !calcData.isTransactionError()) {
			Life calcLife = calcData.getLife();
			int count = calcLife.getCoverageCount();
			Coverage calcCov = null;
			for (int i = 0; i < count; i++) {
				calcCov = calcLife.getCoverageAt(i);
				if (NbaUtils.convertStringToInt(getCoverage().getCoverageKey()) == NbaUtils.convertStringToInt(calcCov.getCoverageKey())) { //matches with coverage key
					updateBackendSubstandardRatingPremiums(substandardRating, calcCov);
					break;
				}
			}
		} else {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(substandardRating));
		}
	}

	/**
	 * Retrieve the rating premium from backend system  
	 * @return the backend rating premium
	 */
	//NBA133 New Method
	protected NbaTXLife getBackendSubstandardRatingPremiumCalculation() {
		if (backendCalcSubStandardRating == null) {
			try {
				backendCalcSubStandardRating = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
						NbaContractCalculationsConstants.CALC_TYPE_LIFE_COVERAGE_SUBRATING_PREMIUM, getNbaTXLife());
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return backendCalcSubStandardRating;
	}

	/**
	 * Updates substandard rating annualized premium amount for the substandard rating.
	 * @param substandardRating the SubstandardRating object
	 * @param coverage the calculated Coverage object
	 */
	//NBA133 New Method
	protected void updateBackendSubstandardRatingPremiums(SubstandardRating substandardRating, Coverage calcCov) {
		long partRoleCode = getLifeParticipant().getLifeParticipantRoleCode();
		int ratingType = NbaUtils.getRatingType(substandardRating);
		int calcPartCount = calcCov.getLifeParticipantCount();
		int partSubCount = -1;
		for (int j = 0; j < calcPartCount; j++) {
			LifeParticipant calcLifepart = calcCov.getLifeParticipantAt(j);
			if (partRoleCode == calcLifepart.getLifeParticipantRoleCode()) {
				partSubCount = calcLifepart.getSubstandardRatingCount();
				for (int k = 0; k < partSubCount; k++) {
					SubstandardRating calcSubstandardRating = calcLifepart.getSubstandardRatingAt(k);
					if (ratingType == NbaUtils.getRatingType(calcSubstandardRating)) {
						SubstandardRatingExtension calcSubstandardRatingExtension = NbaUtils.getFirstSubstandardExtension(calcSubstandardRating);
						if (calcSubstandardRatingExtension != null) {
							getSubstandardRatingExtension().setAnnualPremAmt(calcSubstandardRatingExtension.getAnnualPremAmt());
							getSubstandardRatingExtension().setActionUpdate();
							break;
						}
					}
				}
			}
		}
	}
	/**
	 * Update the Mode Premium, Annual Premium, and Total Annual Premium amounts for a coverage.
	 * @param coverage
	 */
	protected void updatePremiums(Coverage coverage) {
		try {//SPR3174
			// begin NBA104
			if (getNbaModePremiumCalculation() == null) {
				return;
			}
			// end NBA104
			String id = coverage.getId();
			for (int i = 0; i < getNbaModePremiumCalculation().getCalculationResultCount(); i++) {
				CalculationResult aresult = getNbaModePremiumCalculation().getCalculationResultAt(i);
				// begin NBA104
				if (id.equals(aresult.getObjectId())) {
					if (aresult.hasCalcError()) {
						//AXAL3996 code deleted
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", id); //ALS3996
						return;
					}
					// end NBA104
					for (int j = 0; j < aresult.getCalcProductCount(); j++) {
						CalcProduct aprod = aresult.getCalcProductAt(j);
						if (aprod.getType().equalsIgnoreCase("ModalPremAmt")) {
							coverage.setModalPremAmt(Double.parseDouble(aprod.getValue()));
						} else if (aprod.getType().equalsIgnoreCase("ANNUALPREMAMT")) {
							coverage.setAnnualPremAmt(Double.parseDouble(aprod.getValue()));
						} else if (aprod.getType().equalsIgnoreCase("TOTANNUALPREMAMT")) {
							coverage.setTotAnnualPremAmt(Double.parseDouble(aprod.getValue()));
						} else if (aprod.getType().equalsIgnoreCase("SurrCharge")) { //P2AXAL048 - to set Coverage.SurrCharge
							coverage.setSurrCharge(Double.parseDouble(aprod.getValue()));
						}
					}
				}
			}
			coverage.setActionUpdate();
		} finally {//SPR3174
			cleanUpCalculation();//SPR3174
		}//SPR3174
	}
	/**
	 * Update the Mode Premium and Annual Premium amounts for a Policy.
	 * @param Policy
	 */
	protected void updatePremiums(Policy policy) {
		// begin NBA104
		if (getNbaModePremiumCalculation() == null) {
			return; 
		}
		// reset the following Non-Standard Mode values in case mode changed to Standard
		getPolicyExtension().deleteQuotedPremiumBasisAmt();
		getPolicyExtension().deleteNonStandardBillAmt();
		getPolicyExtension().deleteQuotedPremiumBasisFrequency();
		// end NBA104
		String id = policy.getId();
		for (int i = 0; i < getNbaModePremiumCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getNbaModePremiumCalculation().getCalculationResultAt(i);
			// begin NBA104
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					//AXAL3996 code deleted
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", id); //ALS3996
					return;
				}
				// end NBA104
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("PaymentAmt")) {
						policy.setPaymentAmt(Double.parseDouble(aprod.getValue()));
					} else if (aprod.getType().equalsIgnoreCase("AnnualPremAmt")) {
						policy.setAnnualPaymentAmt(Double.parseDouble(aprod.getValue()));
						// begin NBA104
					} else if (aprod.getType().equalsIgnoreCase("QuotedPremiumBasisAmt")) {
						getPolicyExtension().setQuotedPremiumBasisAmt(Double.parseDouble(aprod.getValue()));
					} else if (aprod.getType().equalsIgnoreCase("NonStandardBillAmt")) {
						getPolicyExtension().setNonStandardBillAmt(Double.parseDouble(aprod.getValue()));
					} else if (aprod.getType().equalsIgnoreCase("QuotedPremiumBasisFreq")) {
						getPolicyExtension().setQuotedPremiumBasisFrequency(Long.parseLong(aprod.getValue()));
					} else if (aprod.getType().equalsIgnoreCase("MonthlyModeFactor")) {
						PaymentModeMethods pmm = getPaymentModeMethods(OLI_PAYMODE_MNTHLY);
						pmm.setModeFactor(Double.parseDouble(aprod.getValue()));
						pmm.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("QuarterlyModeFactor")) {
						PaymentModeMethods pmm = getPaymentModeMethods(OLI_PAYMODE_QUARTLY);
						pmm.setModeFactor(Double.parseDouble(aprod.getValue()));
						pmm.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("SemiAnnualModeFactor")) {
						PaymentModeMethods pmm = getPaymentModeMethods(OLI_PAYMODE_BIANNUAL);
						pmm.setModeFactor(Double.parseDouble(aprod.getValue()));
						pmm.setActionUpdate();
						// Begin AXAL3.7.56
					} else if (aprod.getType().equalsIgnoreCase("AnnualModeFactor")) {
						PaymentModeMethods pmm = getPaymentModeMethods(OLI_PAYMODE_ANNUAL);
						pmm.setModeFactor(Double.parseDouble(aprod.getValue()));
						pmm.setActionUpdate();
						// End AXAL3.7.56
					} else if (aprod.getType().equalsIgnoreCase("ExceptionModeRule")) {
						PaymentModeMethods pmm = getPaymentModeMethods(OLI_PAYMODE_MNTHLY);
						pmm.setExceptionModeRule(Long.parseLong(aprod.getValue()));
						pmm.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("MultipleOrder")) {
						getPaymentModeAssembly().setMultipleOrder(Long.parseLong(aprod.getValue()));
						getPaymentModeAssembly().setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("RatingOrder")) {
						getPaymentModeAssembly().setRatingOrder(Long.parseLong(aprod.getValue()));
						getPaymentModeAssembly().setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("FirstRoundingRule")) {
						getPaymentModeAssembly().setFirstRoundingRule(Long.parseLong(aprod.getValue()));
						getPaymentModeAssembly().setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("PolicyFeeAmt")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setFeeAmt(Double.parseDouble(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("PolicyFeeAddRule")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setFeeAddRule(Long.parseLong(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("PolicyFeeCommInd")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setCommissionablePremCalcInd(Boolean.getBoolean(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("PolicyFeeCalcRule")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setFeeCalcRule(Long.parseLong(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("PolicyFeeTableIdentity")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setFeeTableIdentity(aprod.getValue());
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("CollectionFeeAmt")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_COLLECTION, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setFeeAmt(Double.parseDouble(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("CollectionFeeAddRule")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_COLLECTION, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setFeeAddRule(Long.parseLong(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("CollectionFeeCommInd")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_COLLECTION, policy.getPaymentMode());	// AXAL3.7.56
						paymentFees.setCommissionablePremCalcInd(Boolean.getBoolean(aprod.getValue()));
						paymentFees.setActionUpdate();
						// end NBA104
						// Begin AXAL3.7.56
					} else if (aprod.getType().equalsIgnoreCase("MonthlyPolicyFeeAmt")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, OLI_PAYMODE_MNTHLY);
						paymentFees.setFeeAmt(Double.parseDouble(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("QuarterlyPolicyFeeAmt")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, OLI_PAYMODE_QUARTLY);
						paymentFees.setFeeAmt(Double.parseDouble(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("SemiannualPolicyFeeAmt")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, OLI_PAYMODE_BIANNUAL);
						paymentFees.setFeeAmt(Double.parseDouble(aprod.getValue()));
						paymentFees.setActionUpdate();
					} else if (aprod.getType().equalsIgnoreCase("AnnualPolicyFeeAmt")) { 
						PaymentFees paymentFees = getPaymentFees(OLI_FEE_POLICYFEE, OLI_PAYMODE_ANNUAL);
						paymentFees.setFeeAmt(Double.parseDouble(aprod.getValue()));
						paymentFees.setActionUpdate();
						// End AXAL3.7.56
					}
				}
			}
		}
		policy.setActionUpdate();
	}
	/**
	 * Update the Mode Premium and Annual Premium amounts for a CovOption.
	 * @param CovOption
	 */
	protected void updatePremiums(CovOption covOption) {
		// begin NBA104
		if (getNbaModePremiumCalculation() == null) {
			return; 
		}
		// end NBA104
		String id = covOption.getId();
		for (int i = 0; i < getNbaModePremiumCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getNbaModePremiumCalculation().getCalculationResultAt(i);
			// begin NBA104
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
					return;
				}
				// end NBA104
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("ModalPremAmt")) {
						covOption.setModalPremAmt(Double.parseDouble(aprod.getValue()));
					} else if (aprod.getType().equalsIgnoreCase("ANNUALPREMAMT")) {
						covOption.setAnnualPremAmt(Double.parseDouble(aprod.getValue()));
					}
				}
			}
		}
		covOption.setActionUpdate();
	}
	/**
	 * Update the Annual Premium amount for a SubstandardRating.
	 * No calculation will be done for proposed SubstandartRating.
	 * @param Policy
	 */
	protected void updatePremiums(SubstandardRating substandardRating) {
		// begin NBA104
		if (getNbaModePremiumCalculation() == null) {
			return; 
		}
		// end NBA104
		String id = substandardRating.getId();
		for (int i = 0; i < getNbaModePremiumCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getNbaModePremiumCalculation().getCalculationResultAt(i);
			// begin NBA104
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
					return;
				}
				// end NBA104
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("AnnualPremAmt")) {
						getSubstandardRatingExtension().setAnnualPremAmt(Double.parseDouble(aprod.getValue()));
					}
				}
			}
		}
		substandardRating.setActionUpdate();
	}
	/**
	 * Return the OINK filter value for a Party.
	 * @param partyId the partyId
	 * @return int
	 */
	//SPR1778 New Method
	protected int getOinkPartyFilter(String partyId) {
		for (int i = 0; i < getOLifE().getPartyCount(); i++) {
			Party party = getOLifE().getPartyAt(i);
			if (party.getId().equals(partyId)) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Return the OINK filter value for a Coverage.
	 * The filter is the index of the coverage for the Party, so if the id
	 * matched the second coverage for Party_x, a value of 1 is returned.
	 * @param covId the Coverage Id
	 * @return int
	 */
	//SPR1778 New Method
	protected int getOinkCoverageFilter(String covId) {
		for (int i = 0; i < getLife().getCoverageCount(); i++) {
			Coverage coverage = getLife().getCoverageAt(i);
			if (coverage.getId().equals(covId)) {
				//Find the PartyId of the insured
				LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
				if (lifeParticipant == null) {
					return i;
				} else {
					//Find the index of the coverage for that PartyId
					String partyID = lifeParticipant.getPartyID();
					int tmpIdx = 0;
					for (int j = 0; j < getLife().getCoverageCount(); j++) {
						coverage = getLife().getCoverageAt(j);
						lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
						if (partyID.equals(lifeParticipant.getPartyID())) {
							if (coverage.getId().equals(covId)) {
								return tmpIdx;
							} else {
								tmpIdx++;
							}
						}
					}
				}
				return i;
			}
		}
		return -1;
	}
	/**
	 * Update the CovOption.TermDate using CovOptionProductExtension.MaturityCalcMethod.
	 * CovOptionProductExtension.MaturityCalcMethod values:
	 * OLIX_MATURITYCALCMETH_DURATION (1000500001) - The policy duration indicated.
	 * OLIX_MATURITYCALCMETH_ATTAINEDAGE (1000500002) - The attained age indicated
	 * OLIX_MATURITYCALCMETH_EARLYDURPAYUP (1000500003) - The earlier of the policy duration or the pay-up date of the associated coverage.
	 * OLIX_MATURITYCALCMETH_EARLYAGEPAYUP (1000500004) - The earlier of the attained age or the pay-up date of the associated coverage.
	 * OLIX_MATURITYCALCMETH_EARLYDURCEASE (1000500005) - The earlier of the policy duration or the cease date of the associated coverage.
	 * OLIX_MATURITYCALCMETH_EARLYAGECEASE (1000500006) - The earlier of the attained age or the cease date of associated coverage.
	 * @param covOption
	 * @param covOptionProduct
	 * @param issueAge
	 * @param payUpDate
	 * @param termDate
	 */
	// SPR1956 New Method
	protected void updateCovOptionTermDate(CovOption covOption, CovOptionProduct covOptionProduct, int issueAge, Date payUpDate, Date termDate) {
		Date covOptionTermDate = termDate;
		int years = 0;
		CovOptionProductExtension covOptionProductExtension = AccelProduct.getFirstCovOptionProductExtension(covOptionProduct); //NBA237
		if (covOptionProductExtension != null && covOptionProductExtension.hasMaturityCalcMethod()) {
			long method = covOptionProductExtension.getMaturityCalcMethod();
			if (method == OLIX_MATURITYCALCMETH_ATTAINEDAGE || method == OLIX_MATURITYCALCMETH_EARLYAGEPAYUP
					|| method == OLIX_MATURITYCALCMETH_EARLYAGECEASE) {
				years = covOptionProduct.getMaturityAge() - issueAge;
			} else if (method == OLIX_MATURITYCALCMETH_DURATION || method == OLIX_MATURITYCALCMETH_EARLYDURPAYUP
					|| method == OLIX_MATURITYCALCMETH_EARLYDURCEASE) {
				years = covOptionProductExtension.getMaturityDuration();
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Invalid Maturity Calc Method", getIdOf(covOption));
				method = -1;
			}
			if (method > -1) {
				covOptionTermDate = getTermDate(method, payUpDate, termDate, years);
			}
		} else if(covOptionProduct != null && covOptionProduct.hasMaturityAge()){//AXAL3.7.04
			years = covOptionProduct.getMaturityAge() - issueAge;
			covOptionTermDate = getTermDate(NbaOliConstants.OLI_TC_NULL, payUpDate, termDate, years);
		}
		covOption.setTermDate(covOptionTermDate);
		covOption.setActionUpdate();
	}

	protected Date getTermDate(long method, Date payUpDate, Date termDate, int years) {
		if (!covOption.hasEffDate()) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Invalid CovOption effective date", getIdOf(covOption));
		} else {
			if (years < 0) {
				years = 0;
			}
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(covOption.getEffDate());
			cal.add(Calendar.YEAR, years);
			if (method == OLIX_MATURITYCALCMETH_EARLYAGEPAYUP || method == OLIX_MATURITYCALCMETH_EARLYDURPAYUP) {
				if (NbaUtils.compare(payUpDate, cal.getTime()) < 0) {
					cal.setTime(payUpDate);
				}
			} else if (method == OLIX_MATURITYCALCMETH_EARLYAGECEASE || method == OLIX_MATURITYCALCMETH_EARLYDURCEASE) {
				if (NbaUtils.compare(termDate, cal.getTime()) < 0) {
					cal.setTime(termDate);
				}
			}
			if (NbaUtils.compare(cal.getTime(), termDate) > 0) { //Cannot be greater than Coverage Term Date
				cal.setTime(termDate);
			}
			return cal.getTime();
		}
		return termDate;
	}
	/**
	 * Update the CovOption.PayUpDate using CovOptionProductExtension.PayUpCalcMethod.
	 * CovOptionProductExtension.PayUpCalcMethod values:
	 * OLIX_MATURITYCALCMETH_DURATION (1000500001) - The policy duration indicated.
	 * OLIX_MATURITYCALCMETH_ATTAINEDAGE (1000500002) - The attained age indicated
	 * OLIX_MATURITYCALCMETH_EARLYDURPAYUP (1000500003) - The earlier of the policy duration or the pay-up date of the associated coverage.
	 * OLIX_MATURITYCALCMETH_EARLYAGEPAYUP (1000500004) - The earlier of the attained age or the pay-up date of the associated coverage.
	 * OLIX_MATURITYCALCMETH_EARLYDURCEASE (1000500005) - The earlier of the policy duration or the cease date of the associated coverage.
	 * OLIX_MATURITYCALCMETH_EARLYAGECEASE (1000500006) - The earlier of the attained age or the cease date of associated coverage.
	 * @param covOption
	 * @param covOptionProduct
	 * @param issueAge
	 * @param payUpDate
	 * @param termDate
	 */
	// SPR1956 New Method
	protected void updateCovOptionPayUpDate(CovOption covOption, CovOptionProduct covOptionProduct, int issueAge, Date payUpDate, Date termDate) {
		Date covOptionPayUpDate = payUpDate;
		CovOptionProductExtension covOptionProductExtension = AccelProduct.getFirstCovOptionProductExtension(covOptionProduct); //NBA237
		int years = 0;//APSL3522
		if (covOptionProductExtension != null && covOptionProductExtension.hasPayUpCalcMethod()) {
			long method = covOptionProductExtension.getPayUpCalcMethod();
			if (method == OLIX_MATURITYCALCMETH_ATTAINEDAGE
					|| method == OLIX_MATURITYCALCMETH_EARLYAGEPAYUP
					|| method == OLIX_MATURITYCALCMETH_EARLYAGECEASE) {
				years = covOptionProductExtension.getPayUpAge() - issueAge;
			} else if (
					method == OLIX_MATURITYCALCMETH_DURATION
					|| method == OLIX_MATURITYCALCMETH_EARLYDURPAYUP
					|| method == OLIX_MATURITYCALCMETH_EARLYDURCEASE) {
				years = covOptionProductExtension.getPayUpAge(); //PayUpAge contains duration
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Invalid Pay-Up Calc Method", getIdOf(covOption));
				method = -1;
			}
			if (method > -1) {
				if (!covOption.hasEffDate()) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Invalid CovOption effective date", getIdOf(covOption));
				} else {
					if (years < 0) {
						years = 0;
					}
					Calendar cal = GregorianCalendar.getInstance();
					cal.setTime(covOption.getEffDate());
					cal.add(Calendar.YEAR, years);
					if (method == OLIX_MATURITYCALCMETH_EARLYAGEPAYUP || method == OLIX_MATURITYCALCMETH_EARLYDURPAYUP) {
						if (NbaUtils.compare(payUpDate, cal.getTime()) < 0) {
							cal.setTime(payUpDate);
						}
					} else if (method == OLIX_MATURITYCALCMETH_EARLYAGECEASE || method == OLIX_MATURITYCALCMETH_EARLYDURCEASE) {
						if (NbaUtils.compare(termDate, cal.getTime()) < 0) {
							cal.setTime(termDate);
						}
					}
					if (NbaUtils.compare(cal.getTime(), termDate) > 0) { //Cannot be greater than Coverage Term Date
						cal.setTime(termDate);
					}
					covOptionPayUpDate = cal.getTime();
				}
			}
		}else if(covOptionProduct != null && covOptionProduct.hasMaturityAge()){//Begin APSL3522
			years = covOptionProduct.getMaturityAge() - issueAge;
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(covOption.getEffDate());
			cal.add(Calendar.YEAR, years);
			if (NbaUtils.compare(cal.getTime(), termDate) > 0) { //Cannot be greater than Coverage Term Date
				cal.setTime(termDate);
			}
			covOptionPayUpDate = cal.getTime();
		}//End APSL3522
		getCovOptionExtension().setPayUpDate(covOptionPayUpDate);
		getCovOptionExtension().setActionUpdate();
	}

	/**
	 * Translates a value to an OLifE value.
	 * @param inVal
	 * @param table
	 * @return
	 */
	// NBA104 New Method	
	protected String translateToOLifECode(String inVal, String tableName) {
		String olifeValue = " ";
		NbaUctData[] table = (NbaUctData[])getUctTable(tableName);
		if (table != null && inVal.length() != 0) {
			for (int i = 0; i < table.length; i++) {
				if (table[i].getBesValue() != null && table[i].getBesValue().compareToIgnoreCase(inVal) == 0) {
					olifeValue = table[i].code();
					break;
				}
			}
		}
		return (olifeValue);
	}

	/**
	 * Translates an olife value to an BES value.
	 * @param olifeVal the olife value
	 * @param table the UCT table name
	 * @return translated BES value for an olife value
	 */
	//SPR2686 New Method	
	protected String translateToBESValue(String olifeVal, String tableName) {
		String besValue = null;
		if (null != tableName && null != olifeVal && olifeVal.trim().length() > 0) {
			HashMap aCase = new HashMap();
			aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, getNbaTXLife().getCarrierCode());
			aCase.put(NbaTableAccessConstants.C_TABLE_NAME, tableName);
			aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");
			aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, getSystemId());

			int aBackEndSystem = -1;
			if (isSystemIdCyberLife()){
				aBackEndSystem = NbaTableAccessConstants.CYBERLIFE;
			}else if (isSystemIdVantage()){
				aBackEndSystem = NbaTableAccessConstants.VANTAGE;
			}
			try{
				besValue = getTableAccessor().translateOlifeValue(aCase,tableName,olifeVal,aBackEndSystem);
			}
			catch(NbaDataAccessException nde){
				getLogger().logException(nde);
			}
		}
		return besValue;
	}

	/**
	 * Retrieve the appropriate description from the NBA_UCT table.
	 * @param inVal
	 * @param table
	 * @return
	 */
	// NBA104 New Method //P2AXAL062 signature modified
	public String getDescription(long inVal, String tableName) {
		String description = "";
		NbaUctData[] table = (NbaUctData[])getUctTable(tableName);
		if (table != null) {
			for (int i = 0; i < table.length; i++) {
				if (table[i].getIndexValue() != null && table[i].getIndexValue().compareToIgnoreCase(Long.toString(inVal)) == 0) {
					description = table[i].getIndexTranslation();
					break;
				}
			}
		}
		return (description);
	}

	/**
	 * Calls the translation tables for UCT Tables
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	// NBA104 New Method
	protected NbaTableData[] getUctTable(String tableName) {

		HashMap aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, getNbaTXLife().getCarrierCode());
		aCase.put(NbaTableAccessConstants.C_TABLE_NAME, tableName);
		aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");
		aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, getNbaTXLife().getBackendSystem());


		NbaTableData[] tArray = null;
		try {
			tArray = getTableAccessor().getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {}

		return (tArray);
	}

	/**
	 * Return the NbaTableAccessor.
	 * @return
	 */
	// NBA104 New Method	
	protected NbaTableAccessor getTableAccessor() {
		if (ntsAccess == null) {
			ntsAccess = new NbaTableAccessor();
		}
		return ntsAccess;
	}
	/**
	 * Updates the special frequency premiums for a contract allowing flexible premiums.
	 * @param policy
	 */
	// NBA104 New Method
	protected void updateSpecialFrequencyPremiums(Policy policy) {		
		try {//SPR3174
			if (getSpecialFrequencyPremiumsCalculation() == null) {
				return;
			}
			String id = policy.getId();
			for (int i = 0; i < getSpecialFrequencyPremiumsCalculation().getCalculationResultCount(); i++) {
				CalculationResult aresult = getSpecialFrequencyPremiumsCalculation().getCalculationResultAt(i);
				if (id.equals(aresult.getObjectId())) {
					if (aresult.hasCalcError()) {
						addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
						return;
					}
					for (int j = 0; j < aresult.getCalcProductCount(); j++) {
						CalcProduct aprod = aresult.getCalcProductAt(j);
						if (aprod.getType().equalsIgnoreCase("PaymentAmt")) {
							policy.setPaymentAmt(Double.parseDouble(aprod.getValue()));
							policy.setActionUpdate();
						} else if (aprod.getType().equalsIgnoreCase("NonStandardBillAmt")) {
							getPolicyExtension().setNonStandardBillAmt(Double.parseDouble(aprod.getValue()));
							getPolicyExtension().setActionUpdate();
						}
					}
				}
			}
		} finally {//SPR3174
			cleanUpCalculation();//SPR3174
		}//SPR3174
	}
	/**
	 * Update the special frequency premiums for a contract allowing flexible premiums.
	 * @param policy the policy Object
	 */
	//NBA133 New Method
	protected void updateBESNonStandardModePremiums(Policy policy) {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_NON_STANDARD_MODE_PREMIUM, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				Policy calcPolicy = calcData.getPolicy();
				policy.setPaymentAmt(calcPolicy.getPaymentAmt());
				policy.setAnnualPaymentAmt(calcPolicy.getAnnualPaymentAmt());
				policy.setActionUpdate();
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(policy));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}
	/**
	 * Retrieve the NbaCalculation for Special Frequency Premiums calculations.
	 * @return NbaCalculation
	 */
	//  NBA104 New Method                                                
	protected NbaCalculation getSpecialFrequencyPremiumsCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation =
						NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_NON_STANDARD_MODE_PREMIUM, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation ;
	}


	/**
	 * Get the default InvestProductInfo 
	 * @return InvestProductInfo
	 */
	// NBA100 New Method	
	protected InvestProductInfo getDefaultInvestProductInfo() {
		InvestProductInfo investProductInfo = null;
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			InvestProductInfoExtension investProductInfoExtension;
			for (int i = 0; i < policyProduct.getInvestProductInfoCount(); i++) {
				investProductInfoExtension = getInvestProductInfoExtensionFor(policyProduct.getInvestProductInfoAt(i));
				if (investProductInfoExtension != null && investProductInfoExtension.getDefaultInd()) {
					investProductInfo = policyProduct.getInvestProductInfoAt(i);
					break;
				}
			}
		}
		return investProductInfo;
	}
	/**
	 * Get the new money interest rate from a VPMS model for Variable plans. 
	 * @return InvestProductInfo
	 */
	// NBA100 New Method	
	protected double getNewMoneyRateFromModel() {
		double rate = 0;
		NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(false); //Get the new money interest rate
		if (nbaVpmsResultsData != null) {
			if (nbaVpmsResultsData.wasSuccessful()) {
				rate = Double.valueOf((String) nbaVpmsResultsData.getResultsData().get(0)).doubleValue();
			}
		}
		return rate;
	}
	/**
	 * Update the Joint Equal Age Premium for a coverage.
	 * @param coverage
	 */
	//NBA104 New Method                                      
	protected void updateJointEqualAge(Coverage coverage) {
		try {//SPR3174
			if (getJointEqualAgeCalculation() == null) {
				return;
			}
			String id = coverage.getId();
			for (int i = 0; i < getJointEqualAgeCalculation().getCalculationResultCount(); i++) {
				CalculationResult aresult = getJointEqualAgeCalculation().getCalculationResultAt(i);
				if (id.equals(aresult.getObjectId())) {
					if (aresult.hasCalcError()) {
						addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
						return;
					}
					for (int j = 0; j < aresult.getCalcProductCount(); j++) {
						CalcProduct aprod = aresult.getCalcProductAt(j);
						if (aprod.getType().equalsIgnoreCase("IssueAge")) {
							for (int k = 0; k < coverage.getLifeParticipantCount(); k++) {
								LifeParticipant lifepart = coverage.getLifeParticipantAt(k);
								if (lifepart != null) {
									if (lifepart.getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY
											|| lifepart.getLifeParticipantRoleCode() == OLI_PARTICROLE_JOINT) {
										lifepart.setIssueAge(Integer.parseInt(aprod.getValue()));
										lifepart.setActionUpdate();
									}
								}
							}
						}
					}
				}
			}
		} finally {//SPR3174
			cleanUpCalculation();
		}//SPR3174
	}
	/**
	 * Invoke the backend calculation service to retreive calculated issue age and update 
	 * all the life participants on the coverage
	 * @param coverage the Coverage object
	 */
	//NBA133 New Method                                      
	protected void updateBESJointEqualAge(Coverage coverage) {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_JOINT_EQUAL_AGE, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				String covKey = coverage.getCoverageKey();
				Life calcLife = calcData.getLife();
				int covCount = calcLife.getCoverageCount();
				for (int i = 0; i < covCount; i++) {
					Coverage calcCov = calcLife.getCoverageAt(i);
					if (covKey.equalsIgnoreCase(calcCov.getCoverageKey())) {
						updateBESJointEqualAge(coverage, calcCov);
						break;
					}
				}
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(coverage));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}

	/**
	 * Update the Joint Equal Age for all life participant on a coverage.
	 * @param coverage the Coverage object that to be updated for issue age
	 * @param calcCov the Coverage object that is having calculated issue age
	 */
	//NBA133 New Method
	protected void updateBESJointEqualAge(Coverage coverage, Coverage calcCov) {
		int partCount = coverage.getLifeParticipantCount();
		int calcPartCount = calcCov.getLifeParticipantCount();
		for (int i = 0; i < partCount; i++) {
			LifeParticipant lifepart = coverage.getLifeParticipantAt(i);
			for (int j = 0; j < calcPartCount; j++) {
				LifeParticipant calcLifepart = calcCov.getLifeParticipantAt(j);
				if (lifepart.getLifeParticipantRoleCode() == calcLifepart.getLifeParticipantRoleCode()){
					lifepart.setIssueAge(calcLifepart.getIssueAge());
					lifepart.setActionUpdate();
					break;
				}
			}
		}
	}
	/**
	 * Retrieve the NbaCalculation for Joint Equal Age calculations.
	 * @return NbaCalculation
	 */
	//NBA104 New Method                                                
	protected NbaCalculation getJointEqualAgeCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_JOINT_EQUAL_AGE, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation;
	}
	/**
	 * Returns true if the plan has premium load targets defined.
	 * @return
	 */
	// NBA104 New Method
	protected boolean hasPremLoadRules() {
		FeeExtension feeExtension = getNbaProduct().getFeeExtension(NbaOliConstants.OLI_FEE_PREMLOAD);
		if (feeExtension == null) {
			addPlanInfoMissingMessage("Fee", getIdOf(getPolicy()));
			return false;
		}
		int count = feeExtension.getFeeTableRefCount();
		for (int i = 0; i < count; i++) {
			FeeTableRef feeTableRef = feeExtension.getFeeTableRefAt(i);
			if (feeTableRef.getChargeMethodTC() == OLIX_CHARGERULECALCMETHOD_PCTANNLOAD
					|| feeTableRef.getChargeMethodTC() == OLIX_CHARGERULECALCMETHOD_SECANNLOAD) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Update the Premium Load Target for a contract.
	 * @param coverage
	 */
	//NBA104 New Method                                      
	protected void updatePremLoadTarget(Life life) {
		try {//SPR3174
			if (getPremLoadTargetCalculation() == null) {
				return;
			}
			String id = life.getId();
			for (int i = 0; i < getPremLoadTargetCalculation().getCalculationResultCount(); i++) {
				CalculationResult aresult = getPremLoadTargetCalculation().getCalculationResultAt(i);
				if (id.equals(aresult.getObjectId())) {
					if (aresult.hasCalcError()) {
						addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
						return;
					}
					for (int j = 0; j < aresult.getCalcProductCount(); j++) {
						CalcProduct aprod = aresult.getCalcProductAt(j);
						if (aprod.getType().equalsIgnoreCase("PremLoadTargetAmt")) {
							getLifeExtension().setPremLoadTargetAmt(aprod.getValue());
							getLifeExtension().setActionUpdate();
						}
					}
				}
			}
		} finally {//SPR3174
			cleanUpCalculation();
		}//SPR3174
	}
	/**
	 * Sets Holding.Policy.Life.CommTargetPrem
	 */	
	//NBA117 New Method
	protected void updateVNTGPremLoadTarget() {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_PREMIUM_LOAD, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				LifeExtension lifeExtn = NbaUtils.getFirstLifeExtension(getLife());
				LifeExtension calcLifeExtn = NbaUtils.getFirstLifeExtension(calcData.getLife());
				if (lifeExtn != null && calcLifeExtn != null) {
					lifeExtn.setPremLoadTargetAmt(calcLifeExtn.getPremLoadTargetAmt());
					lifeExtn.setActionUpdate();
				}
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(getCoverage()));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}
	/**
	 * Retrieve the NbaCalculation for Premium Load Target calculations.
	 * @return NbaCalculation
	 */
	//NBA104 New Method                                                
	protected NbaCalculation getPremLoadTargetCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_PREMIUM_LOAD, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation;
	}
	/**
	 * Returns true if the plan has commission rules defined.
	 * @return boolean
	 */
	// NBA104 New Method
	protected boolean hasCommissionRules() {
		PolicyProductInfoExtension policyProdInfoExt = AccelProduct.getFirstPolicyProductInfoExtension(getNbaProduct().getPolicyProductInfo()); //NBA237

		if (policyProdInfoExt == null) {
			addPlanInfoMissingMessage("Policy Product Info Extension", getIdOf(getCoverage()));
			return false;
		}
		if (policyProdInfoExt.getCommExtractCode() == OLI_PAYRATECAT_TRGTPREM || policyProdInfoExt.getCommExtractCode() == OLI_PAYRATECAT_TARGET) {
			return true;
		}
		return false;
	}
	/**
	 * Update the Commission Premium Target for a coverage.
	 * @param coverage
	 */
	//NBA104 New Method                                      
	protected void updateCommissionTargetPrem(Coverage coverage) {
		if (getCommissionTargetPremCalculation() == null) {
			return;
		}
		String id = coverage.getId();
		for (int i = 0; i < getCommissionTargetPremCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getCommissionTargetPremCalculation().getCalculationResultAt(i);
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
					return;
				}
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("COMMTARGETPREM")) { 
						getCoverageExtension().setCommTargetPrem(aprod.getValue());
						getCoverageExtension().setActionUpdate();
					}
				}
			}
		}
		cleanUpCalculation();       
	}
	/**
	 * Update the Commission Premium Target for a coverage.
	 * @param coverage the Coverage object
	 */
	//NBA133 New Method                                      
	protected void updateBESCommissionTargetPrem(Coverage coverage) {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_COMMISSION_TARGET, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				String covKey = coverage.getCoverageKey();
				Life calcLife = calcData.getLife();
				int covCount = calcLife.getCoverageCount();
				for (int i = 0; i < covCount; i++) {
					Coverage calcCov = calcLife.getCoverageAt(i);
					if (covKey.equalsIgnoreCase(calcCov.getCoverageKey())) {
						CoverageExtension calcCovExt = NbaUtils.getFirstCoverageExtension(calcCov);
						if (calcCovExt != null) {
							CoverageExtension covExt = getCoverageExtension();
							covExt.setCommTargetPrem(calcCovExt.getCommTargetPrem());
							covExt.setActionUpdate();
							break;
						}
					}
				}
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(coverage));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}
	/**
	 * Sets Holding.Policy.Life.CommTargetPrem
	 */
	//NBA117 New Method
	protected void updateVNTGCommissionTargetPrem() {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_COMMISSION_TARGET, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				LifeExtension lifeExtn = NbaUtils.getFirstLifeExtension(getLife());
				LifeExtension calcLifeExtn = NbaUtils.getFirstLifeExtension(calcData.getLife());
				if (lifeExtn != null && calcLifeExtn != null) {
					lifeExtn.setCommTargetPrem(calcLifeExtn.getCommTargetPrem());
					lifeExtn.setActionUpdate();
				}
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(getCoverage()));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}
	/**
	 * Retrieve the NbaCalculation for Commission Premium Target calculations.
	 * @return NbaCalculation
	 */
	//NBA104 New Method                                                
	protected NbaCalculation getCommissionTargetPremCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation =
						NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_COMMISSION_TARGET, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation;
	}
	/**
	 * Returns true if the plan has surrender charge rules defined.
	 * @return
	 */
	// NBA104 New Method
	protected boolean hasSurrenderChargeRules() {
		FeeExtension feeExtension = getNbaProduct().getFeeExtension(NbaOliConstants.OLI_FEE_SURRCHG); //NBA237
		if (feeExtension == null) {
			addPlanInfoMissingMessage("Fee", getIdOf(getCoverage()));
			return false;
		}
		int count = feeExtension.getFeeTableRefCount();
		for (int i = 0; i < count; i++) {
			FeeTableRef feeTableRef = feeExtension.getFeeTableRefAt(i);
			if (feeTableRef.getTableType() == CONTENTTYPE_PRIMARY || feeTableRef.getTableType() == CONTENTTYPE_SECONDARY) {
				if (feeTableRef.getChargeMethodTC() == OLIX_CHARGERULECALCMETHOD_PCTSURRAGE) {  //SPR2269
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Update the Surrender Charge Target for a coverage.
	 * @param coverage
	 */
	//NBA104 New Method                                      
	protected void updateSurrenderChargeTarget(Coverage coverage) {
		if (getSurrenderChargeTargetCalculation() == null) {
			return;
		}
		String id = coverage.getId();
		for (int i = 0; i < getSurrenderChargeTargetCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getSurrenderChargeTargetCalculation().getCalculationResultAt(i);
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
					return;
				}
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("SurrTargetPrem")) { 
						coverage.setSurrTargetPrem(aprod.getValue());
						coverage.setActionUpdate();
					}
				}
			}
		}
		cleanUpCalculation();       
	}
	//NBA117 New Method
	protected void updateVNTGSurrenderChargeTarget() {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_SURRENDER_CHARGE, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				LifeExtension lifeExtn = NbaUtils.getFirstLifeExtension(getLife());
				LifeExtension calcLifeExtn = NbaUtils.getFirstLifeExtension(calcData.getLife());
				if (lifeExtn != null && calcLifeExtn != null) {
					lifeExtn.setSurrTargetPrem(calcLifeExtn.getSurrTargetPrem());
					lifeExtn.setActionUpdate();
				}
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(getLife()));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}
	/**
	 * Retrieve the NbaCalculation for Surrender Charge Target calculations.
	 * @return NbaCalculation
	 */
	//NBA104 New Method                                                
	protected NbaCalculation getSurrenderChargeTargetCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_SURRENDER_CHARGE, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation;
	}
	/**
	 * Reset the NbaCalculation.
	 */
	//NBA104 New Method                                                
	protected void cleanUpCalculation() {
		nbaCalculation = null;
	}	

	/**
	 * Returns a person's true age. 
	 * @return
	 */
	//NBA111 New Method
	protected int getTrueAge(String partyID) {
		if (getNbaTXLife() != null) {
			NbaParty party = getNbaTXLife().getParty(partyID);
			if (party != null) {
				return party.getAge(); 
			}
		}
		return -1;
	}
	/**
	 * Returns a person's birthdate. 
	 * @return
	 */
	//NBA111 New Method
	protected Date getBirthDate(String partyID) {
		if (getNbaTXLife() != null) {
			NbaParty party = getNbaTXLife().getParty(partyID);
			if (party != null) {
				return party.getBirthDate();
			}
		}
		return null;
	}
	/**
	 * The Rate Utility model assumes that only one table rating will exist for a coverage.
	 * This method generates a validation error if there is more than one table rating.
	 * @return 
	 */
	// NBA104 New Method
	protected boolean isValidCoverageTableRating() {
		boolean hasTableRating = false;
		long countTableRating = 0;
		Coverage coverage = getCoverage();
		int count = coverage.getLifeParticipantCount();
		for (int i=0; i < count; i++) {
			LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(i);
			if (lifeParticipant != null  && !lifeParticipant.isActionDelete()) {
				int srCount = lifeParticipant.getSubstandardRatingCount();
				for (int j = 0; j < srCount; j++) {
					SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(j);
					if (NbaUtils.isValidRating(substandardRating)
							&& (substandardRating.hasPermTableRating() || substandardRating.hasTempTableRating())) { //SPR3098
						hasTableRating = true;
						SubstandardRatingExtension substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
						// validating that only one table rating is allowed without an ExtraPremPerUnit
						if ((substandardRatingExt == null || !substandardRatingExt.hasExtraPremPerUnit())) {
							if (countTableRating == 1) {
								addCalcErrorMessage("Too many table ratings on coverage", coverage.getId());
								return false;
							}
							countTableRating++;
						}
					}
				}
			}
		}

		return hasTableRating;
	}
	/**
	 * The Rate Utility model assumes that only one table rating will exist for a coverage.
	 * This method generates a validation error if there is more than one table rating.
	 * @return 
	 */
	// NBA104 New Method
	protected boolean isValidPercentageTableRating() {
		if (isValidCoverageTableRating()) {
			SubstandardRisk substandardRisk = getSubstandardRiskFor(getCoverage());
			if (substandardRisk != null && substandardRisk.getRateBasedOn() == NbaOliConstants.OLI_RATEBASEDON_INITDTHBENPU) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Update the ExtraPremPerUnit for a substandard rating.
	 * @param substandardRating
	 */
	// NBA104 New Method                                      
	protected void updateExtraPremPerUnit(SubstandardRating substandardRating) {
		if (getRateUtilityCalculation() == null) {
			return;
		}
		String id = substandardRating.getId();
		for (int i = 0; i < getRateUtilityCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getRateUtilityCalculation().getCalculationResultAt(i);
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					String rating = "";
					if (substandardRating.hasPermTableRating()) {
						rating = concat(" Table: ", getDescription(substandardRating.getPermTableRating(), NbaTableConstants.OLI_LU_RATINGS));
					} else {
						rating = concat(" Table: ", getDescription(substandardRating.getTempTableRating(), NbaTableConstants.OLI_LU_RATINGS));
					}
					addCalcErrorMessage(concat(aresult.getCalcError().getMessage(), rating), id);
					return;
				}
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("ExtraPremPerUnit")) {
						SubstandardRatingExtension substandardRatingExt = getSubstandardRatingExtension(); 
						substandardRatingExt.setExtraPremPerUnit(aprod.getValue());
						substandardRatingExt.setActionUpdate();
						return;
					}
				}
			}
		}
	}
	/**
	 * Update the PermPercentageLoading or TempPercentageLoading for a substandard rating.
	 * @param substandardRating
	 */
	// NBA104 New Method                                      
	protected void updatePercentageLoading(SubstandardRating substandardRating) {
		if (getRateUtilityCalculation() == null) {
			return;
		}
		String id = substandardRating.getId();
		for (int i = 0; i < getRateUtilityCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getRateUtilityCalculation().getCalculationResultAt(i);
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					String rating = "";
					if (substandardRating.hasPermTableRating()) {
						rating = concat(" Table: ", getDescription(substandardRating.getPermTableRating(), NbaTableConstants.OLI_LU_RATINGS));
					} else {
						rating = concat(" Table: ", getDescription(substandardRating.getTempTableRating(), NbaTableConstants.OLI_LU_RATINGS));
					}
					addCalcErrorMessage(concat(aresult.getCalcError().getMessage(), rating), id);
					return;
				}
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("PercentageLoading")) {
						SubstandardRatingExtension substandardRatingExt = getSubstandardRatingExtension(); 
						if (substandardRating.hasPermTableRating()) {
							substandardRatingExt.setPermPercentageLoading(aprod.getValue());
						} else {
							substandardRatingExt.setTempPercentageLoading(aprod.getValue());
						}
						substandardRatingExt.setActionUpdate();
						return;
					}
				}
			}
		}
	}
	/**
	 * Update the OptionPct for a CovOption.
	 * @param covOption
	 */
	// NBA104 New Method                                      
	protected void updateOptionPct(CovOption covOption) {
		if (getRateUtilityCalculation() == null) {
			return;
		}
		String id = covOption.getId();
		for (int i = 0; i < getRateUtilityCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getRateUtilityCalculation().getCalculationResultAt(i);
			if (id.equals(aresult.getObjectId())) {
				if (aresult.hasCalcError()) {
					addCalcErrorMessage(aresult.getCalcError().getMessage(), concat("Coverage Option = ", id));
					return;
				}
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("OptionPct")) {
						covOption.setOptionPct(aprod.getValue());
						covOption.setActionUpdate();
						return;
					}
				}
			}
		}
	}
	/**
	 * Retrieve the NbaCalculation for calculations supported by the rate utility calculation.
	 * @return NbaCalculation
	 */
	// NBA104 New Method                                                
	protected NbaCalculation getRateUtilityCalculation() {
		if (nbaRateUtilCalc == null) {
			try {
				nbaRateUtilCalc = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_RATE_UTIL, getNbaTXLife());
				handleCalculationErrors(nbaRateUtilCalc);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaRateUtilCalc;
	}

	/**
	 * Return the FeatureProduct for a Rider. 
	 * @param Rider the Rider
	 * @return the RiderProduct
	 */
	// NBA104 New Method
	protected FeatureProduct getFeatureProductForPlan(Rider Rider) {
		FeatureProduct featureProduct = null;
		AnnuityProduct annuityProduct = getAnnuityProductForPlan();
		if (annuityProduct != null) {
			for (int i = 0; i < annuityProduct.getFeatureProductCount(); i++) {
				featureProduct = annuityProduct.getFeatureProductAt(i);
				if (featureProduct.hasFeatureCode() && featureProduct.getFeatureCode().equals(Rider.getRiderCode())) {
					return featureProduct;
				}
			}
		}
		return null;
	}

	/**
	 * Returns true if no Basic or Insurance validation process has generated a severe error message.
	 * @return
	 */
	// NBA104 New Method
	protected boolean isValidContractForGuidelines() {
		int count = getHolding().getSystemMessageCount();
		for (int i=0; i < count; i++) {
			SystemMessage sysMsg = getHolding().getSystemMessageAt(i);
			if (sysMsg != null && !sysMsg.isActionDelete() && sysMsg.getMessageSeverityCode() == OLI_MSGSEVERITY_SEVERE) {
				SystemMessageExtension sysMsgExt = NbaUtils.getFirstSystemMessageExtension(sysMsg);
				if (sysMsgExt != null
						&& (sysMsgExt.getMsgValidationType() == OLIX_VALIDATION_BASIC || sysMsgExt.getMsgValidationType() == OLIX_VALIDATION_INSURANCE)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns true if LifeProductExtension.DefLifeInsMethod is equal set to DEFRA or TEFRA guidelines.
	 * @return
	 */
	// NBA104 New Method
	protected boolean hasGuidelinePremiumRule() {
		LifeProductExtension lifeProductExt = getLifeProductExtensionForPlan();
		if (lifeProductExt == null) {
			addPlanInfoMissingMessage("LifeProduct", getIdOf(getLife()));
		} else if (lifeProductExt.getDefLifeInsMethod() == OLI_INSTEST_TEFRAGPEST || lifeProductExt.getDefLifeInsMethod() == OLI_INSTEST_DEFRAGPTEST) {
			return true;
		}
		return false;
	}

	/**
	 * Updates the calculated GuidelineAnnPrem and the GuidelineSinglePrem for a life contract.
	 * @param lifeUSA
	 */
	// NBA104 New Method
	protected void updateGuidelinePremiums(LifeUSA lifeUSA) {
		try {//SPR3174
			if (getGuidelinePremiumCalculation() == null) {
				return;
			}
			String id = lifeUSA.getId();
			for (int i = 0; i < getGuidelinePremiumCalculation().getCalculationResultCount(); i++) {
				CalculationResult aresult = getGuidelinePremiumCalculation().getCalculationResultAt(i);
				if (id.equals(aresult.getObjectId())) {
					if (aresult.hasCalcError()) {
						addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
						return;
					}
					for (int j = 0; j < aresult.getCalcProductCount(); j++) {
						CalcProduct aprod = aresult.getCalcProductAt(j);
						if (aprod.getType().equalsIgnoreCase("GuidelineAnnPrem")) {
							lifeUSA.setGuidelineAnnPrem(aprod.getValue());
							lifeUSA.setActionUpdate();
						} else if (aprod.getType().equalsIgnoreCase("GuidelineSinglePrem")) {
							lifeUSA.setGuidelineSinglePrem(aprod.getValue());
							lifeUSA.setActionUpdate();
						}
					}
				}
			}
		} finally {//SPR3174
			cleanUpCalculation();
		}//SPR3174
	}
	/**
	 * Updates the calculated GuidelineAnnPrem and the GuidelineSinglePrem for a life contract.
	 * @param lifeUSA the LifeUSA object
	 * APSL4295 
	 * Guideline Premium changed to CVA
	 * GuidelineAnnPrem & GuidelineSinglePrem should be removed from 203 to avoid cv 2060
	 */
	//NBA133 New Method
	protected void updateBESGuidelinePremiums(LifeUSA lifeUSA) {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_GUIDELINE_PREMIUM, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				LifeUSA calcLifeUSA = calcData.getLife().getLifeUSA();
				if (calcLifeUSA != null) {
					if(calcLifeUSA.hasGuidelineAnnPrem()){
						lifeUSA.setGuidelineAnnPrem(calcLifeUSA.getGuidelineAnnPrem());
					}else{
						lifeUSA.deleteGuidelineAnnPrem();
					}
					if(calcLifeUSA.hasGuidelineSinglePrem()){
						lifeUSA.setGuidelineSinglePrem(calcLifeUSA.getGuidelineSinglePrem());
					}else{
						lifeUSA.deleteGuidelineSinglePrem();
					}
					lifeUSA.setActionUpdate();
				}
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(getLife()));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}
	/**
	 * Retrieve the NbaCalculation for Guideline Premium calculations.
	 * @return NbaCalculation
	 */
	//NBA104 New Method                                                
	protected NbaCalculation getGuidelinePremiumCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_GUIDELINE_PREMIUM, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation;
	}

	/**
	 * Returns true if LifeProductExtension.MECIssueType is equal set to Seven Pay.
	 * @return
	 */
	// NBA104 New Method
	protected boolean has7PayPremiumRule() {
		LifeProductExtension lifeProductExt = getLifeProductExtensionForPlan();
		if (lifeProductExt == null) {
			addPlanInfoMissingMessage("LifeProduct", getIdOf(getLife()));
		} else if(getLifeUSAProduct(lifeProductExt) != null && getLifeUSAProduct(lifeProductExt).getMECIssueType() == OLIX_MECISSUETYPE_7PAYTEST) { //P2AXAL048
			return true;
		}
		return false;
	}

	/**
	 * Updates the calculated GuidelineAnnPrem and the GuidelineSinglePrem for a life contract.
	 * @param lifeUSA
	 */
	// NBA104 New Method
	protected void update7PayPremiums(LifeUSA lifeUSA) {
		try {//SPR3174
			if (get7PayPremiumCalculation() == null) {
				return;
			}
			String id = lifeUSA.getId();
			LifeUSAExtension lifeUSAExt = getLifeUSAExtension();
			for (int i = 0; i < get7PayPremiumCalculation().getCalculationResultCount(); i++) {
				CalculationResult aresult = get7PayPremiumCalculation().getCalculationResultAt(i);
				if (id.equals(aresult.getObjectId())) {
					if (aresult.hasCalcError()) {
						addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
						return;
					}
					//getLifeUSA().setSevenPayPrem(1000);
					//getLifeUSA().setInitSevenPayPrem(1000);
					//getLifeUSA().setCumSevenPayPrem(1000);
					for (int j = 0; j < aresult.getCalcProductCount(); j++) {
						CalcProduct aprod = aresult.getCalcProductAt(j);
						if (aprod.getType().equalsIgnoreCase("CurrIntRate7Pay")) {
							lifeUSAExt.setCurrIntRate7Pay(aprod.getValue());
							lifeUSAExt.setActionUpdate();
						} else if (aprod.getType().equalsIgnoreCase("DeemedFaceAmt")) {
							lifeUSA.setDeemedFaceAmt(aprod.getValue());
							lifeUSA.setActionUpdate();
						} else if (aprod.getType().equalsIgnoreCase("GuarPeriod7Pay")) {
							lifeUSAExt.setGuarPeriod7Pay(aprod.getValue());
							lifeUSAExt.setActionUpdate();
						} else if (aprod.getType().equalsIgnoreCase("MECDate")) {
							try {
								lifeUSA.setMECDate(getMMDDYYYYsdf().parse(aprod.getValue()));
								lifeUSA.setActionUpdate();
							} catch (ParseException pe) {
								addCalcErrorMessage("MECDate: " + pe.getMessage(), id);
							}
						} else if (aprod.getType().equalsIgnoreCase("SevenPayPrem")) {
							lifeUSA.setSevenPayPrem(aprod.getValue());
							lifeUSA.setActionUpdate();
						}
					}
				}
			}
		} finally {//SPR3174
			cleanUpCalculation();
		}//SPR3174
	}

	//NBA117 New Method
	protected void updateVNTG7PayPremiums(){	      
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_7PAY_PREMIUM, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				LifeUSA calcLifeUSA = calcData.getLife().getLifeUSA();
				getLifeUSA().setSevenPayPrem(calcLifeUSA.getSevenPayPrem());
				getLifeUSA().setActionUpdate();
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(getLife()));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}	    

	}

	/**
	 * Retrieve the NbaCalculation for Seven Pay Premium calculations.
	 * @return NbaCalculation
	 */
	//NBA104 New Method                                                
	protected NbaCalculation get7PayPremiumCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_7PAY_PREMIUM, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation;
	}
	/**
	 * Returns true if the plan has min no laps targets defined.
	 * @return
	 */
	// NBA104 New Method
	protected boolean hasMinNoLapseRules() {
		PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
		if (policyProductExtension == null) {
			addPlanInfoMissingMessage("PolicyProductExtension", getIdOf(getPolicy()));
			return false;
		}
		int count = policyProductExtension.getLapseProvisionCount();
		for (int i = 0; i < count; i++) {
			LapseProvision lapseProvision = policyProductExtension.getLapseProvisionAt(i);
			if (lapseProvision.getGracePeriodEntryType() == OLI_GPENTRY_CVORAPMAPCV
					|| lapseProvision.getGracePeriodEntryType() == OLI_GPENTRY_CVORAPMAPSV
					|| lapseProvision.getGracePeriodEntryType() == OLI_GPENTRY_APMAPGP
					|| lapseProvision.getGracePeriodEntryType() == OLI_GPENTRY_APMAPSV
					|| lapseProvision.getGracePeriodEntryType() == OLI_GPENTRY_CVANDAPMAPCV
					|| lapseProvision.getGracePeriodEntryType() == OLI_GPENTRY_SVANDAPMAPSV) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Update the Min No Lapse Target for a contract.
	 * @param coverage
	 */
	//NBA104 New Method                                      
	protected void updateMinNoLapseTarget(Life life) {
		try {//SPR3174
			if (getMinNoLapseTargetCalculation() == null) {
				return;
			}
			String id = life.getId();
			for (int i = 0; i < getMinNoLapseTargetCalculation().getCalculationResultCount(); i++) {
				CalculationResult aresult = getMinNoLapseTargetCalculation().getCalculationResultAt(i);
				if (id.equals(aresult.getObjectId())) {
					if (aresult.hasCalcError()) {
						addCalcErrorMessage(aresult.getCalcError().getMessage(), id);
						return;
					}
					for (int j = 0; j < aresult.getCalcProductCount(); j++) {
						CalcProduct aprod = aresult.getCalcProductAt(j);
						if (aprod.getType().equalsIgnoreCase("MapTargetEndDate")) {
							try {
								getLifeExtension().setMapTargetEndDate(getMMDDYYYYsdf().parse(aprod.getValue()));
								getLifeExtension().setActionUpdate();
							} catch (ParseException pe) {
								addCalcErrorMessage("MapTargetEndDate " + pe.getMessage(), id);
							}
						} else if (aprod.getType().equalsIgnoreCase("MinPremAmt")) {
							getLife().setMinPremAmt(aprod.getValue());
							getLife().setActionUpdate();

						}
					}
				}
			}
		} finally {//SPR3174
			cleanUpCalculation();
		}//SPR3174
	}

	/**
	 * Update the Min No Lapse Target for a contract.
	 * @param life the Life object
	 */
	//NBA133 New Method                                      
	protected void updateBESMinNoLapsePrem(Life life) {
		try {
			NbaTXLife calcData = NbaBackendContractCalculatorFactory.calculate(getSystemId(),
					NbaContractCalculationsConstants.CALC_TYPE_MIN_NO_LAPSE_PREMIUM, getNbaTXLife());
			if (calcData != null && !calcData.isTransactionError()) {
				Life calcLife = calcData.getLife();
				life.setMinPremAmt(calcLife.getMinPremAmt());
				life.setActionUpdate();

				if(isSystemIdCyberLife()){//NBA117
					LifeExtension calcLifeExt = NbaUtils.getFirstLifeExtension(calcLife);
					if (calcLifeExt != null) {
						LifeExtension lifeExt = getLifeExtension();
						lifeExt.setMapTargetEndDate(calcLifeExt.getMapTargetEndDate());
						lifeExt.setActionUpdate();
					}
				}//NBA117                
			} else {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), getIdOf(getLife()));
			}
		} catch (NbaBaseException e) {
			addCalcErrorMessage(e.getMessage(), "");
		}
	}
	/**
	 * Retrieve the NbaCalculation for Min No Lapse Prem Target calculations.
	 * @return NbaCalculation
	 */
	//NBA104 New Method                                                
	protected NbaCalculation getMinNoLapseTargetCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation =
						NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_MIN_NO_LAPSE_PREMIUM, getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (Throwable e) {
				addCalcErrorMessage(e.getMessage(), "");
			}
		}
		return nbaCalculation;
	}


	/**
	 * Validate DivType
	 */
	//NBA107
	protected void validateDivType() {
		PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
		if (policyProductExtension == null) {
			addPlanInfoMissingMessage("PolicyProductExtension.Dividend", getIdOf(getLife()));
		} else {
			int divCount = policyProductExtension.getDividendCount(); //SPR3371
			//Primary Dividend Option
			boolean needDiv = !getLife().hasDivType(); //SPR1744
			boolean invalid = true;			
			for (int i = 0; i < divCount; i++) { //SPR3371
				Dividend dividend = policyProductExtension.getDividendAt(i);
				if (needDiv) {
					if (dividend.getDefaultInd()) { //default value indicator
						getLife().setDivType(dividend.getDivType());
						getLife().setActionUpdate();
						invalid = false;
						break;
					}
				} else {
					if (getLife().getDivType() == dividend.getDivType()) {
						invalid = false;
						break;
					}
				}
			}
			//begin SPR3371
			if (invalid && needDiv && divCount > 0) {
				getLife().setDivType(policyProductExtension.getDividendAt(0).getDivType()); //Use first if no default was found
				getLife().setActionUpdate();
				invalid = false;
			}
			//end SPR3371
			if (invalid) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Primary Dividend Option", getIdOf(getLife()));
			}
			//begin SPR1744
			//Secondary Dividend Option
			boolean needSecDiv = !getLifeExtension().hasSecondaryDividendType();
			//SPR3371 code deleted
			Dividend dividend = null;
			invalid = true;
			for (int i = 0; i < divCount; i++) {
				dividend = policyProductExtension.getDividendAt(i);
				if (needSecDiv) {
					if (dividend.getDefaultInd()) {
						getLifeExtension().setSecondaryDividendType(dividend.getDivType());
						getLifeExtension().setActionUpdate();
						//end SPR1744
						invalid = false;
						break;
					}
					//begin SPR1744
				} else {
					if (getLifeExtension().getSecondaryDividendType() == dividend.getDivType()) {
						invalid = false;
						break;
					}
					//end SPR1744
				}
			}
			//begin SPR3371
			if (invalid && needSecDiv && divCount > 0) {
				getLifeExtension().setSecondaryDividendType(policyProductExtension.getDividendAt(0).getDivType()); //Use first if no default was found
				getLifeExtension().setActionUpdate();
				invalid = false;
			}
			//end SPR3371
			if (invalid) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Secondary Dividend Option", getIdOf(getLife()));
			}
		}
	}
	/**
	 * Determine if the party is a physician by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is a physician
	 */
	// SPR2811 NEW METHOD
	protected boolean isPhysician(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_PHYSICIAN) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the party is a payment facilitator by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is a payment facilitator
	 */
	// SPR3375 NEW METHOD
	protected boolean isPaymentFacilitator(String partyId) {
		List relations = getNbaTXLife().getOLifE().getRelation();
		int relationCount = relations.size();
		for (int i = 0; i < relationCount; i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_PYMT_FACILITATOR) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * This method adds a new System Message if the allocated percentage for a SubAccount is less than the allowed percentage value.
	 * @param minValue minimum allowed allocation percent
	 * @param systematicActivityCode systematic activity code value
	 */
	//SPR2986 New Method
	protected void validateMinPct(double minValue, long systematicActivityCode) {
		double percent = 0;		
		//The percentage to be compared against the plan level value is SubAccount.AllocPercent for payments or SubAccount.PolicyChargePct for charges.	
		if (-1L == systematicActivityCode) {
			percent = getSubAccount().getAllocPercent();
		} else if (OLI_SYSACTTYPE_CHARGES == systematicActivityCode) {
			percent = getSubAccount().getPolicyChargePct();
		}
		if (minValue > percent) {
			addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat(concatPct("Requested: ", percent), concatPct(", Minimum: ", minValue)),
					getIdOf(getSubAccount()));
		}
	}	

	/**
	 * Returns base coverage extenstion
	 * @param life the Life object
	 * @return the base coverage extenstion
	 */
	//SPR3043 New Method
	protected CoverageExtension getBaseCoverageExtension(Life life){
		int covCount = getLife().getCoverageCount();
		for (int i = 0; i < covCount; i++) {
			if (getLife().getCoverageAt(i).getIndicatorCode() == OLI_COVIND_BASE) {
				return NbaUtils.getFirstCoverageExtension(getLife().getCoverageAt(i));
			}
		}
		return null;
	}

	/**
	 * Determine if transaction errors are present on the transaction result. If yes then
	 * concat all transaction error messages in a String and return them to the caller. 
	 * @param calcContract the transaction result
	 * @return concated transaction error messages.
	 */
	//NBA133 New Method
	protected String getBackendErrors(NbaTXLife calcContract) {
		String errors = null;
		if (calcContract != null) {
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = calcContract.getTXLife()
					.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			if (allResponses.getTXLifeResponseCount() > 0) {
				TransResult transResult = allResponses.getTXLifeResponseAt(allResponses.getTXLifeResponseCount() - 1).getTransResult();
				StringBuffer buffer = new StringBuffer();
				int count = transResult.getResultInfoCount();
				for (int i = 0; i < count; i++) {
					buffer.append(transResult.getResultInfoAt(i).getResultInfoDesc());
					buffer.append(";");
				}
				errors = buffer.toString();
			}
		}
		return errors;
	}

	/**
	 * Checks for the sub standard rating on coverage, if rating exceeds the value defined in product plan return true otherwise false
	 * @param covOptProd the CovOptionProduct object
	 * @param covOptProdExtn the CovOptionProductExtension object
	 * @param cov the Coverage object
	 * @return boolean value
	 */
	//NBA143 New Method
	protected boolean checkSubStandardRatingExceeds(long maxRating, CovOptionProductExtension covOptProdExtn, Coverage cov) {
		if (NbaOliConstants.OLIX_MAXTBLRESTRICT_NOISSUEEXCEED == covOptProdExtn.getMaxTableRestrictType()) {
			return exceedsMaxTableRating(cov, maxRating);            
		} else if (NbaOliConstants.OLIX_MAXTBLRESTRICT_NOISSUESUBSTD == covOptProdExtn.getMaxTableRestrictType()) {
			return hasSubStandardRating(cov);
		}
		return false;
	}

	/**
	 * Adds a new inherent CovOption in the Coverage
	 * @param cov the Coverage object
	 * @param covOptProd the CovOptionProduct object
	 * @param covOptProdExtn the CovOptionProductExtension object
	 */
	//NBA143 New Method
	protected void addOrUpdateInherentCovOption(Coverage cov, CovOption covOpt, CovOptionProduct covOptProd, CovOptionProductExtension covOptProdExtn) {
		//P2AXAL016 AXA Specific Code moved to New Method validateInherentCovOptionProduct
		//P2AXAL055 Code Removed
		covOpt.setCovOptionKey(cov.getCoverageKey());
		if (isSystemIdVantage()) {
			covOpt.setCovOptionStatus(NbaOliConstants.OLI_POLSTAT_APPRVD);
		}
		covOpt.setProductCode(covOptProd.getProductCode());
		covOpt.setPlanName(covOptProd.getPlanName());//P2AXAL016
		covOpt.setShortName(covOptProd.getPlanName());//ALII2013
		covOpt.setLifeCovOptTypeCode(covOptProdExtn.getLifeCovOptTypeCode());
		covOpt.setOptionNumberOfUnits(cov.getCurrentNumberOfUnits());
		covOpt.setOptionAmt((cov.getCurrentNumberOfUnits() * cov.getValuePerUnit()));
		covOpt.setEffDate(cov.getEffDate());
		//Begin ALNA213
		if(cov.hasTermDate()){
			covOpt.setTermDate(cov.getTermDate());
		}
		//End ALNA213

		//Begin ALS4958, ALII134
		CovOptionExtension covOptExtn = null;
		covOptExtn = getCovOptionExtension(covOpt);//P2AXAL055
		covOptExtn.setActionUpdate();			//ALII145
		//End ALS4958, ALII134
		covOptExtn.setSelectionRule(covOptProdExtn.getSelectionRule());
	}

	/**
	 * Adds a new inherent CovOption in the Rider
	 * @param rider the Rider object
	 * @param covOptProd the CovOptionProduct object
	 * @param covOptProdExtn the CovOptionProductExtension object
	 */
	//NBA143 New Method
	protected void addInherentCovOption(Rider rider, CovOptionProduct covOptProd, CovOptionProductExtension covOptProdExtn) {
		CovOption covOpt = new CovOption();
		nbaOLifEId.setId(covOpt);
		Participant participant = NbaUtils.findPrimaryInsuredParticipant(rider.getParticipant());
		if (participant != null) {
			covOpt.setLifeParticipantRefID(participant.getId());
		}
		covOpt.setCovOptionKey(rider.getRiderKey());
		if (isSystemIdVantage()) {
			covOpt.setCovOptionStatus(NbaOliConstants.OLI_POLSTAT_APPRVD);
		}
		covOpt.setProductCode(covOptProd.getProductCode());
		covOpt.setLifeCovOptTypeCode(covOptProdExtn.getLifeCovOptTypeCode());
		covOpt.setOptionNumberOfUnits(rider.getNumberOfUnits());
		covOpt.setOptionAmt((rider.getNumberOfUnits() * covOptProd.getValuePerUnit()));
		covOpt.setEffDate(rider.getEffDate());
		covOpt.setTermDate(rider.getTermDate());

		OLifEExtension oLifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVOPTION);
		covOpt.addOLifEExtension(oLifeExtn);

		CovOptionExtension covOptExtn = oLifeExtn.getCovOptionExtension();
		covOptExtn.setSelectionRule(covOptProdExtn.getSelectionRule());
		covOptExtn.setActionAdd();

		covOpt.setActionAdd();
		rider.addCovOption(covOpt);
	}

	/**
	 * Removes the covOption Object from ArrayList for APAPPSUB process otherwise sets the delete indicator
	 * @param covOpt the CovOption object
	 * @param allCovOpts ArrayList of all covOptions
	 */
	//NBA143 New Method
	protected void removeCovOption(CovOption covOpt, List allCovOpts) {
		if (NbaConstants.PROC_APP_SUBMIT.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())
				|| NbaConstants.PROC_CONTRACT_CHANGE.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())//ALS4372
				|| NbaConstants.PROC_GI_APP_SUBMIT.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())) {//NBLXA188
			allCovOpts.remove(covOpt);
		} else {
			covOpt.setActionDelete();
		}
	}

	/**
	 * Adds/Removes covOption from the coverage as per requiremenst of P245
	 * @param covProd the CoverageProduct object
	 * @param cov the Coverage object 
	 */
	//NBA143 New Method
	protected void addOrRefreshInherentBenefits(CoverageProduct covProd, Coverage cov) {
		int covOptProdCount = covProd.getCovOptionProductCount();
		List allCovOpts = cov.getCovOption();
		int allCovOptCount = allCovOpts.size();
		CovOption covOpt = null;
		CovOptionProduct covOptProd = null;
		CovOptionProductExtension covOptProdExtn = null;
		String prodcode = "";
		boolean inherentBenefitExistInCoverage = false;
		long selectionRule = -1L;
		//compare all of the plan defined inherent coverage options to the existing coverage options on the associated coverage
		for (int k = 0; k < covOptProdCount; k++) {
			covOptProd = covProd.getCovOptionProductAt(k);
			covOptProdExtn = getCovOptionProductExtensionFor(covOptProd);
			if (covOptProdExtn != null) {
				selectionRule = covOptProdExtn.getSelectionRule();
				if (NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM == selectionRule || NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM == selectionRule) {
					//P2AXAL016 Begin
					boolean isValidInherentBenefit = validateInherentCovOptionProduct(cov, covProd, covOptProd, covOptProdExtn);	//APSL5130


					//Base Code Restructued to only delete the benefits, those are not applicable.
					covOpt = NbaUtils.getCovOption(getCoverage(), covOptProd.getProductCode());
					if (!isValidInherentBenefit){
						if (covOpt == null  || covOpt.getCovOptionStatus() == OLI_POLSTAT_DECISSUE) continue;
						removeCovOption(covOpt,getCoverage().getCovOption());
						continue; //go for next CovOptionProduct
					}

					//add or update inherent benefit
					if (covOpt != null){
						covOpt.setActionUpdate();
						addOrUpdateInherentCovOption(cov, covOpt, covOptProd, covOptProdExtn);//ALS4958, ALII134
						continue; //go for next CovOptionProduct
					}
					//P2AXAL016 End
					//ALS4958, ALII134 Code deleted
					CovOption covOptNew = new CovOption();
					nbaOLifEId.setId(covOptNew);
					covOptNew.setActionAdd();
					cov.addCovOption(covOptNew);//ALII134//P2AXAL055					
					addOrUpdateInherentCovOption(cov, covOptNew, covOptProd, covOptProdExtn);//ALS4958, ALII134

				}
			}
		}
	}


	/**
	 * Adds/Removes covOption from the coverage as per requiremenst of P245
	 * @param covProd the CoverageProduct object
	 * @param cov the Coverage object 
	 */
	//P2AXAL016 New Method
	protected boolean validateInherentCovOptionProduct(Coverage cov, CoverageProduct covProd, CovOptionProduct covOptProd, CovOptionProductExtension covOptProdExtn) { //QC18186(APSL5130)

		if (!isActiveCoverageProduct(covOptProd)) {// APSL5304
			return false;
		}

		// Begin AXAL3.7.40
		// Only add the inherent benefit if it's applicable for the state
		long jurisdiction = getApplicationInfo() == null ? -1L : getApplicationInfo().getApplicationJurisdiction();
		if (covOptProdExtn != null && covOptProdExtn.getJurisdictionApprovalCount() > 0) {
			if (!isValidApplicationJurisdiction(jurisdiction, covOptProdExtn.getJurisdictionApproval())) {
				if(!validSimilarCovOptPresent(covProd, covOptProd, jurisdiction)) { //QC18186(APSL5130)
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName() +" not approved in application state: "
							+ getStateCodeTranslation(jurisdiction, NbaTableConstants.NBA_STATES), getIdOf(getCoverage()));
				}
				return false;
			}
		} else {
			PolicyProduct policyProduct = getPolicyProductFor(cov);
			if (policyProduct != null) {
				if (!isValidApplicationJurisdiction(jurisdiction, policyProduct.getJurisdictionApproval())) {
					if(!validSimilarCovOptPresent(covProd, covOptProd, jurisdiction)) { //QC18186(APSL5130)
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName() +" not approved in application state: "
								+ getStateCodeTranslation(jurisdiction, NbaTableConstants.NBA_STATES), getIdOf(getCoverage()));
					}
					return false;
				}
			}
		}
		// End AXAL3.7.40

		//Begin P2AXAL016
		List allowedBensRdrsList = covOptProdExtn.getNBAAllowedBensRdrs(); 
		if(!allowedBensRdrsList.isEmpty() && getNBAAllowedBensRdrs(allowedBensRdrsList) == null){
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName() +" not allowed on Application type.", getIdOf(getCoverage()));
			return false;
		}


		for (int i = 0; i < covOptProdExtn.getConflictObjectInfoCount(); i++) {
			ConflictObjectInfo conflictObjectInfo = covOptProdExtn.getConflictObjectInfoAt(i);
			if (OLI_COVOPTION == conflictObjectInfo.getObjectType()) {
				CovOption conflictCovOption = NbaUtils.getCovOption(getCoverage(), conflictObjectInfo.getProductCode());
				if (conflictCovOption == null || conflictCovOption.getCovOptionStatus() == OLI_POLSTAT_DECISSUE) continue;
				ConflictObjectInfoExtension ext = getConflictObjectInfoExtension(conflictObjectInfo);
				if (ext != null && ext.hasJurisdictionCC()) {
					for (int j = 0; j < ext.getJurisdictionCC().getJurisdictionCount(); j++) {
						if (ext.getJurisdictionCC().getJurisdictionAt(j) == jurisdiction) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName() +" not allowed with "
									+  conflictCovOption.getPlanName() + " in application state: "
									+ getStateCodeTranslation(jurisdiction, NbaTableConstants.NBA_STATES), getIdOf(getCoverage()));
							return false;
						}
					}
				} else  {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName() + " not allowed with " + conflictCovOption.getPlanName(), getIdOf(getCoverage()));
					return false;
				}
			}
			if (OLI_DEATHBENEFITOPTCC == conflictObjectInfo.getObjectType()) {
				if (getCoverage().hasDeathBenefitOptType()
						&& String.valueOf(getCoverage().getDeathBenefitOptType()).equals(conflictObjectInfo.getProductCode())) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName() + " not allowed with Death Benefit "
							+ getDescription(getCoverage().getDeathBenefitOptType(), NbaTableConstants.OLI_LU_DTHBENETYPE), getIdOf(getCoverage()));
					return false;
				}
			}
			//Begin P2AXAL016-ALII910
			if (OLI_DEFLIFEINSMETHODCC == conflictObjectInfo.getObjectType()) {
				if (getLifeUSA().hasDefLifeInsMethod()
						&& String.valueOf(getLifeUSA().getDefLifeInsMethod()).equals(conflictObjectInfo.getProductCode())) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName() + " not allowed under Definition of Life Insured test.", getIdOf(getCoverage()));					
					return false;
				}
			}  
			//End P2AXAL016-ALII910
		}
		//End P2AXAL016
		//Begin P2AXAL055
		LifeParticipant primaryLifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(cov);
		LifeParticipant jointLifeParticipant = NbaUtils.findJointInsuredLifeParticipant(cov);
		if (!isValidGenderCombination(primaryLifeParticipant, jointLifeParticipant, covOptProdExtn.getGenderProductInfo())) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), covOptProd.getPlanName()+" not allowed with gender combination.", getIdOf(cov));
			return false;
		}
		//End P2AXAL055
		return true;
	}    

	//QC18186(APSL5130) New Method
	public boolean validSimilarCovOptPresent(CoverageProduct covProd, CovOptionProduct covOptProd, long jurisdiction) {
		// Check for another valid Inherent benefit exists for same Plan name. No Message required as the valid CovOpt will be added.
		long selectionRule = -1L;
		int covOptProdCount = covProd.getCovOptionProductCount();
		CovOptionProduct covOptProd1;
		for (int k = 0; k < covOptProdCount; k++) {
			covOptProd1 = covProd.getCovOptionProductAt(k);
			if((covOptProd.getProductCode() != null && !covOptProd.getProductCode().equalsIgnoreCase(covOptProd1.getProductCode()))
					&& (covOptProd.getPlanName() != null && covOptProd.getPlanName().equalsIgnoreCase(covOptProd1.getPlanName()))) {
				CovOptionProductExtension covOptProdExtn = getCovOptionProductExtensionFor(covOptProd1);
				if (covOptProdExtn != null) {
					selectionRule = covOptProdExtn.getSelectionRule();
					if (NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM == selectionRule || NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM == selectionRule) {
						if(isValidApplicationJurisdiction(jurisdiction, covOptProdExtn.getJurisdictionApproval())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/**
	 * Adds/Removes covOption from the Rider as per requirements of P246
	 * @param covProd the CoverageProduct object
	 * @param rider the Rider object 
	 */
	//NBA143 New Method
	protected void addOrRefreshInherentBenefits(CoverageProduct covProd, Rider rider) {
		int covOptProdCount = covProd.getCovOptionProductCount();
		List allCovOpts = rider.getCovOption();
		int allCovOptCount = allCovOpts.size();
		CovOption covOpt = null;        
		CovOptionProduct covOptProd = null;
		CovOptionProductExtension covOptProdExtn = null;
		String prodcode = "";        
		boolean inherentBenefitExistInCoverage = false;
		long selectionRule = -1L;
		//compare all of the plan defined inherent coverage options to the existing coverage options on the associated Rider
		for (int k = 0; k < covOptProdCount; k++) {
			covOptProd = covProd.getCovOptionProductAt(k);
			covOptProdExtn = getCovOptionProductExtensionFor(covOptProd);
			if (covOptProdExtn != null) {
				selectionRule = covOptProdExtn.getSelectionRule();
				if (NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM == selectionRule || NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM == selectionRule) {
					//get productCode to compare and match the CovOtion in the Rider
					prodcode = covOptProd.getProductCode();
					inherentBenefitExistInCoverage = false;
					for (int m = 0; m < allCovOptCount; m++) {
						covOpt = (CovOption) allCovOpts.get(m);
						if (prodcode.equalsIgnoreCase(covOpt.getProductCode()) && !covOpt.isActionDelete()) {
							//inherent benefit exist on Rider
							inherentBenefitExistInCoverage = true;
							break;
						}
					}
					//add inherent benefit
					if (!inherentBenefitExistInCoverage) {
						addInherentCovOption(rider, covOptProd, covOptProdExtn);
					}
				}
			}
		}
	}

	/**
	 * Checks if a Coverage Product has Inherent CovOption(s) 
	 * @param covProd the Coverage Product object 
	 * @return true if the Coverage Product is defined with any Inherent CovOption, else false
	 */
	//NBA143 New Method
	protected boolean hasInherentBenefit(CoverageProduct covProd) {
		int covOptProdCount = covProd.getCovOptionProductCount();
		long selectionRule = -1L;
		boolean hasInherentBenefit = false;
		CovOptionProductExtension covOptProdExtn = null;
		for (int j = 0; j < covOptProdCount; j++) {
			covOptProdExtn = getCovOptionProductExtensionFor(covProd.getCovOptionProductAt(j));
			if (covOptProdExtn != null) {
				selectionRule = covOptProdExtn.getSelectionRule();
				if (NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM == selectionRule || NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM == selectionRule) {
					hasInherentBenefit = true;
					break;
				}
			}
		}
		return hasInherentBenefit;
	}

	/**
	 * Removes all Inherent CovOptions from the List
	 * @param allCovOpts List having all CovOption of the Coverage
	 */
	//NBA143 New Method
	protected void removeAllUnmodifiedInherentBenefits(List allCovOpts) {
		int allCovOptCount = allCovOpts.size();
		CovOption covOpt = null;
		CovOptionExtension covOptExtn = null;
		long selectionRule = -1L;
		for (int i = 0; i < allCovOptCount; i++) {
			covOpt = (CovOption) allCovOpts.get(i);
			covOptExtn = NbaUtils.getFirstCovOptionExtension(covOpt);
			if (covOptExtn != null) {
				selectionRule = covOptExtn.getSelectionRule();
				if (NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM == selectionRule || NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM == selectionRule) {
					removeCovOption(covOpt, allCovOpts);
				}
			}
		}
	}

	/**
	 * Check if the table rating exceed the Max allowed table rating
	 * @param cov the Coverage object
	 * @param maxRating long value
	 * @return true if table rating exceeds the Max allowed rating, else false
	 */
	//NBA143 New Method
	protected boolean exceedsMaxTableRating(Coverage cov, long maxRating){
		boolean subStandardRatingExceeds = false;
		LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(cov, true);
		if (lifeParticipant != null) {            
			int subStandardRatingCount = lifeParticipant.getSubstandardRatingCount();
			SubstandardRating substandardRating = null;
			//SPR3098 code deleted
			for (int i = 0; i < subStandardRatingCount; i++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(i);
				//SPR3098 code deleted
				if (NbaUtils.isValidRating(substandardRating)
						&& (substandardRating.getPermTableRating() > maxRating || substandardRating.getTempTableRating() > maxRating)) { //SPR3098
					subStandardRatingExceeds = true;
					break;
				}
			}
		}
		return subStandardRatingExceeds;
	}
	/**
	 * Check if the coverage is rated No Issue SubStandard
	 * @param cov the Coverage object
	 * @return true if coverage is rated No Issue substandard, else false
	 */
	//NBA143 New Method
	protected boolean hasSubStandardRating(Coverage cov){
		boolean subStandardRatingExceeds = false;
		LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(cov, true);
		if (lifeParticipant != null) {
			int subStandardRatingCount = lifeParticipant.getSubstandardRatingCount();
			SubstandardRating substandardRating = null;
			//SPR3098 code deleted
			for (int i = 0; i < subStandardRatingCount; i++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(i);
				//SPR3098 code deleted
				if (NbaUtils.isValidRating(substandardRating)) { //SPR3098
					subStandardRatingExceeds = true;
					break;
				}
			}
		}
		return subStandardRatingExceeds;
	}
	/**
	 * Retrieve the NbaCalculation for Minimum Initial Premium Target calculations.
	 * @return NbaCalculation
	 */
	//NBA142 New Method                                                
	protected NbaCalculation getMinInitPremTargetCalculation() {
		if (nbaCalculation == null) {
			try {
				nbaCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_TYPE_MINIMUM_INITIAL_PREMIUM,
						getNbaTXLife());
				handleCalculationErrors(nbaCalculation);
			} catch (NbaBaseException e) {
				addCalcErrorMessage(e.getMessage(), "Error calculating minimum initial premium");
			}
		}
		return nbaCalculation;
	}

	/**
	 * Call MIP VP/MS model to calculate the model properties and process the results
	 * @param lifeProdExtn LifeProductExtension object
	 */
	//NBA142 New Method
	protected void validateMinInitPremium(LifeProductExtension lifeProdExtn) {
		NbaCalculation nbaCalc = getMinInitPremTargetCalculation();
		if (nbaCalc == null) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Minimum Initial Premimum not calculated", getIdOf(getLife()));
		} else {
			int calcResultCount = nbaCalc.getCalculationResultCount();
			for (int i = 0; i < calcResultCount; i++) {
				if (processForMIPResult(nbaCalc.getCalculationResultAt(i), lifeProdExtn)) {
					break;
				}
			}
		}
		cleanUpCalculation();
	}

	/**
	 * For MinInitPremium node, add a contract message if the calculated initial premium is greater than requested initail premium
	 * @param aresult CalculationResult object
	 * @param lifeProdExtn LifeProductExtension object
	 * @return true if MinInitPremium node is processed
	 */
	//NBA142 New Method
	protected boolean processForMIPResult(CalculationResult aresult, LifeProductExtension lifeProdExtn) {
		boolean isMIPprocessed = false;
		if (aresult.hasCalcError()) {
			addCalcErrorMessage(aresult.getCalcError().getMessage(), aresult.getObjectId());
		} else {
			CalcProduct aprod = null;
			int calcProductCount = aresult.getCalcProductCount();
			for (int i = 0; i < calcProductCount; i++) {
				aprod = aresult.getCalcProductAt(i);
				if (NbaContractCalculationsConstants.P_MIP_TARGET.equalsIgnoreCase(aprod.getType().trim())) {
					isMIPprocessed = true;
					double policyMinInitPremium = getLife().getInitialPremAmt();
					double calcMinInitPremium = NbaUtils.convertStringToDouble(aprod.getValue());                    
					editInitialPremium(policyMinInitPremium, calcMinInitPremium, lifeProdExtn.getMaxPremiumInitialAmt());                    
					break;
				}
			}
		}
		return isMIPprocessed;
	}

	/**
	 * Returns true if nba calculation is supported. Returns false if required backend calculations.
	 * @return true if nba calculation is supported esle returns false.
	 * @throws NbaBaseException
	 */
	//NBA133 New Method
	public boolean isNbaCalc() throws NbaBaseException {
		if (nbaCalc == null) {
			if (getNbaDst() != null) {
				nbaCalc = String.valueOf(NbaUtils.isNbaCalcSupported(getNbaDst().getNbaLob()));
			} else {
				nbaCalc = NbaConstants.TRUE_STR;
			}
		}
		return NbaConstants.TRUE_STR.equalsIgnoreCase(nbaCalc);
	}
	/**
	 * Compare the CovOption amount against the UnderwritingClassProductExtension MinIssueUnits and MaxIssueUnits values.
	 * @param underwritingClassProduct
	 * @param covOption
	 * @param amt
	 */
	//SPR3207 New Method
	protected void performCovOptionAmountEdit(UnderwritingClassProduct underwritingClassProduct, CovOption covOption, double amt) {
		if (underwritingClassProduct.getMinIssueAmt() > 0) {
			double min = underwritingClassProduct.getMinIssueAmt();
			if (amt < min) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Amount: ", amt, ", Low limit: ", min), getIdOf(covOption));
			}
		}
		if (underwritingClassProduct.getMaxIssueAmt() > 0) {
			double max = underwritingClassProduct.getMaxIssueAmt();
			if (amt > max) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Amount: ", amt, ", High limit: ", max), getIdOf(covOption));
			}
		}
	}
	/**
	 * Compare the CovOption number of units against the UnderwritingClassProductExtension MinIssueUnits and MaxIssueUnits values.
	 * @param covOption
	 * @param underwritingClassProductExtension
	 * @param units
	 */
	//SPR3207 New Method
	protected void performCovOptionUnitsEdit(CovOption covOption, UnderwritingClassProductExtension underwritingClassProductExtension, double units) {
		if (underwritingClassProductExtension.getMinIssueUnits() > 0) {
			double min = underwritingClassProductExtension.getMinIssueUnits();
			if (units < min) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Units: ", units, ", Low limit: ", min), getIdOf(covOption));
			}
		}
		if (underwritingClassProductExtension.getMaxIssueUnits() > 0) {
			double max = underwritingClassProductExtension.getMaxIssueUnits();
			if (units > max) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Units: ", units, ", High limit: ", max), getIdOf(covOption));
			}
		}
	}
	/**
	 * Calculate the ratio of CovOption Units/Amount to the corresponding Coverage value and compare the ratio against 
	 * the UnderwritingClassProductExtension MinIssueRatio and MaxIssueRatio values.
	 * @param covOption
	 * @param covAmt
	 * @param covUnits
	 * @param underwritingClassProductExtension
	 * @param amt
	 * @param units
	 */
	//SPR3207 New Method
	protected void performCovOptionRatioEdit(CovOption covOption, double covAmt, double covUnits, UnderwritingClassProductExtension underwritingClassProductExtension, double amt, double units) {
		double value = 100;
		if (units > 0 && covUnits > 0) {
			value = NbaUtils.divideTo2DecimalPlaces(units, covUnits);
			value = NbaUtils.multiplyTo2DecimalPlaces(value, 100.0);
		} else if (amt > 0 && covAmt > 0) {
			value = NbaUtils.divideTo2DecimalPlaces(amt, covAmt);
			value = NbaUtils.multiplyTo2DecimalPlaces(value, 100.0);
		}
		if (underwritingClassProductExtension.getMinIssueRatio() > 0) {
			double min = NbaUtils.multiplyTo2DecimalPlaces(underwritingClassProductExtension.getMinIssueRatio(), 100); 
			if (value < min) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Ratio: ", value, ", Low limit: ", min), getIdOf(covOption));
			}
		}
		if (underwritingClassProductExtension.getMaxIssueRatio() > 0) {
			double max = NbaUtils.multiplyTo2DecimalPlaces(underwritingClassProductExtension.getMaxIssueRatio(), 100);  
			if (value > max) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Ratio: ", value, ", High limit: ", max), getIdOf(covOption));
			}
		}
	}

	/**
	 * checks for CommissionLoadTarget
	 * @return boolean true if condition is met for CommissionLoadTarget 
	 */
	//NBA117 New Method
	protected boolean hasCommissionLoadTarget(){
		return hasRequiredPremiumRateType(OLI_LU_PREMIUMRATETYPE_COMMISSIONABLETGTPRM);
	}
	/**
	 * checks for PremLoadTarget
	 * @return boolean true if condition is met for PremLoadTarget 
	 */
	//NBA117 New Method    
	protected boolean hasPremLoadTarget(){
		return hasRequiredPremiumRateType(OLI_LU_PREMIUMRATETYPE_LOADTGTPRM);
	}
	/**
	 * checks for MinPremAmt
	 * @return boolean true if condition is met for MinPremAmt 
	 */
	//NBA117 New Method 
	protected boolean hasMinPremAmt(){
		return hasRequiredPremiumRateType(OLI_LU_PREMIUMRATETYPE_NOLAPSETGTPRM);
	}
	/**
	 * checks for SurrenderTarget
	 * @return boolean true if condition is met for SurrenderTarget
	 */
	//NBA117 New Method 
	protected boolean hasSurrenderTarget(){
		return hasRequiredPremiumRateType(OLI_LU_PREMIUMRATETYPE_SURRENDERTGTPRM);
	}
	/**
	 * Loop through all PremiumRate objects
	 * @param rateTypeValue value of RateType
	 * @return boolean true if condition is met
	 */    
	//NBA117 NewMethod
	protected boolean hasRequiredPremiumRateType(long rateTypeValue){
		boolean valueFound = false;
		CoverageProductExtension covProdExtn = getCoverageProductExtensionFor(getCoverage());        
		int count = covProdExtn.getPremiumRateCount();
		PremiumRate premiumRate = null;
		for (int i=0; i < count; i++){
			premiumRate = covProdExtn.getPremiumRateAt(i);
			if (premiumRate.getPremiumRateType() == rateTypeValue ){
				valueFound = true;
				break;
			}
		}
		return valueFound;
	}
	/**
	 * checks for 7PayPremium
	 * @return boolean true if condition met for 7PayPremium
	 */
	//NBA117 New Method
	protected boolean has7PayPremium(){
		boolean has7PayPremium = false;
		LifeProductExtension lifeProdExtn = getLifeProductExtensionForPlan();
		if (lifeProdExtn != null && lifeProdExtn.getMECIssueType() == OLIX_MECISSUETYPE_7PAY){
			has7PayPremium = true;
		}
		return has7PayPremium;
	}
	/**
	 * Verify that the Banking object is not for a Credit Card Payment
	 * @return true if the Banking.AcctType() is not 3 (Credit Card) or BankingExtension.CreditCardChargeUse 
	 * is not 1000500002 (Credit Card payment).
	 */
	//SPR3573 New Method
	protected boolean isNotBankingForCreditCardPayment(){
		return  !NbaUtils.isCreditCardPayment(getBanking());  
	}

	//P2AXAL054 method deleted
	/**
	 * Determine if the Payer is other than the Owner or Insured.
	 * @param
	 * @return
	 */
	//AXAL3.7.40 New Method
	protected boolean isPayerOtherThanOwnerAndInsured() {
		NbaParty owner = nbaTXLife.getPrimaryOwner();
		NbaParty insured = nbaTXLife.getPrimaryParty();
		NbaParty payer = nbaTXLife.getParty(nbaTXLife.getPartyId(OLI_REL_PAYER));
		if (payer != null) {
			if (!payer.getID().equalsIgnoreCase(owner.getID()) || !payer.getID().equalsIgnoreCase(insured.getID()))
				return true;
		}
		return false;
	}
	/**
	 * Return the Beneficiary relation if it exists.
	 * @return Relation
	 */
	// AXAL3.7.40 New Method
	protected Relation getBeneficiaryRelation() {
		return getRelation(OLI_REL_BENEFICIARY);
	}
	/**
	 * Return the Contingent Beneficiary relation if it exists.
	 * @return Relation
	 */
	// AXAL3.7.40 New Method
	protected Relation getContingentBeneficiaryRelation() {
		return getRelation(OLI_REL_CONTGNTBENE);
	}
	/**
	 * Determine if the Party is the Beneficiary by determining if 
	 * the person has a Beneficiary Relation
	 * @param insuredId - the insured id
	 * @return true if the party is a Beneficiary
	 */
	//AXAL3.7.40 New Method
	protected boolean isBeneficiary(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		NbaParty insured = nbaTXLife.getPrimaryParty();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getOriginatingObjectID().equals(insured.getID()) && relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_BENEFICIARY) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the Party is a Contingent Beneficiary by determining if 
	 * the person has a Contingent Beneficiary Relation
	 * @param insuredId - the insured id
	 * @return true if the party is a Beneficiary
	 */
	//AXAL3.7.40 New Method
	protected boolean isContingentBeneficiary(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_CONTGNTBENE) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Return the Primary Agent relation if it exists.
	 * @return Relation
	 */
	//New Method AXAL3.7.40
	protected Relation getPrimaryAgentRelation() {
		return getRelation(OLI_REL_PRIMAGENT);
	}

	/**
	 * Determine if the Party is a primar agent.
	 * @return Relation
	 */
	//New Method AXAL3.7.40
	protected boolean isPrimaryAgent(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_PRIMAGENT) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @param formName
	 * @param questionNo
	 * @return
	 */
	//New Method AXAL3.7.40
	protected long getFormResponseCode(String formName, String questionNo) {
		if (formName != null || questionNo != null) {
			int noOfformInstances = (getNbaTXLife().getOLifE().getFormInstance() == null) ? 0 : getNbaTXLife().getOLifE().getFormInstance().size();
			for (int index = 0; index < noOfformInstances; index++) {
				FormInstance formInstance = getNbaTXLife().getOLifE().getFormInstanceAt(index);
				if (formInstance != null && formName.equalsIgnoreCase(formInstance.getFormName())) {
					int noOfformResponses = (formInstance.getFormResponse() == null) ? 0 : formInstance.getFormResponse().size();
					for (int i = 0; i < noOfformResponses; i++) {
						FormResponse formResponse = formInstance.getFormResponseAt(i);
						if (formResponse != null && questionNo.equalsIgnoreCase(formResponse.getQuestionNumber())) {
							return formResponse.getResponseCode();
						}
					}
				}
			}
		}
		return LONG_NULL_VALUE;
	} 
	/**
	 * @param value
	 * @return
	 */
	//New Method AXAL3.7.40
	protected boolean isBlankOrUnanswered(long value) {
		return NbaUtils.isBlankOrUnanswered(value);//ALS4633 
	}

	//New Method AXAL3.7.40
	protected boolean isAnsweredYes(long value) {
		return NbaUtils.isAnsweredYes(value);//ALS4633
	}

	//ALPC131 New Method 
	protected boolean isAnsweredNo(long value) {
		return NbaUtils.isAnsweredNo(value);//ALS4633
	}
	//P2AXAL018 New Method 
	protected boolean isUnanswered(long value) {
		return NbaUtils.isUnanswered(value);
	}

	/**
	 * Determine if the Party is the Beneficiary by determining if 
	 * the person has a Beneficiary Relation
	 * @param insuredId - the insured id
	 * @return true if the party is a Beneficiary
	 */
	//AXAL3.7.40 New Method
	protected boolean isPrimaryBeneficiaryRelation(String relationId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getId().equals(relationId) && relation.getRelationRoleCode() == OLI_REL_BENEFICIARY) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the Party is the Beneficiary by determining if 
	 * the person has a Beneficiary Relation
	 * @param insuredId - the insured id
	 * @return true if the party is a Beneficiary
	 */
	//AXAL3.7.40 New Method
	protected boolean isContingentBeneficiaryRelation(String relationId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getId().equals(relationId) && relation.getRelationRoleCode() == OLI_REL_CONTGNTBENE) {
					return true;
				}
			}
		}
		return false;
	}	
	//APSL4250 New Method
	protected boolean isSubFirmRelation(String relationId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getId().equals(relationId) && relation.getRelationRoleCode() == OLI_REL_SUBORDAGENT) {
					System.out.println("sub firm rel id >>"+relation.getId());
					return true;
				}
			}
		}
		return false;
	}

	//APSL4250 New Method
	protected boolean isSubFirm(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_SUBORDAGENT) {
					System.out.println("sub firm rel id from issubfirm >>"+relation.getId());
					return true;
				}
			}
		}
		return false;
	}

	//AXAL3.7.40 New Method
	private String getRequirementKey() {
		String subset = String.valueOf(getCurrentSubSet());
		logDebug("NbaContractValidationCommon.getRequirementKey(), getNbaConfigValProc()=" + String.valueOf(getNbaConfigValProc()));
		if (getNbaConfigValProc() != null) {
			String processId = concat("_", getNbaConfigValProc().getId());
			String index = ""; //NA_AXAL005
			if (!NbaUtils.isBlankOrNull(getNbaConfigValProc().getMsgkey())) {
				index = "(" + getNbaConfigValProc().getMsgkey() + ")";
			}
			return concat(subset, concat(processId, index));
		}
		return "";

	}
	//AXAL3.7.40 New Method
	public SignatureInfo findSignatureInfo(String code, long partCode, long purpose) {
		if (LIFE_APP_FORM.equalsIgnoreCase(code) || GI_APP_FORM.equalsIgnoreCase(code)) { // P2AXAL068
			return getSignatureInfo(getApplicationInfo().getSignatureInfo(), partCode, purpose);
		}
		return findSignatureInfo(partCode, purpose);
	}	
	public SignatureInfo findSignatureInfo(long partCode, long purpose) {
		if (formInstance != null) { //well get it from formInstance
			return getSignatureInfo(formInstance.getSignatureInfo(), partCode, purpose);
		}
		return null;
	}	
	public SignatureInfo getSignatureInfo(String formInstanceName, long partCode, long purpose) {
		FormInstance formInstance = NbaUtils.getFormInstance(nbaTXLife, formInstanceName);
		if (formInstance != null) {
			return getSignatureInfo(formInstance.getSignatureInfo(), partCode, purpose);
		}
		return null;
	}
	public SignatureInfo getSignatureInfo(List signInfoList, long partCode, long purpose) {
		SignatureInfo signInfo = null;
		if (signInfoList != null) {
			for (int i = 0; i < signInfoList.size(); i++) {
				signInfo = (SignatureInfo) signInfoList.get(i);
				if (! signInfo.isActionDelete() && signInfo.getSignatureRoleCode() == partCode && signInfo.getSignaturePurpose() == purpose) //NBLXA-1290 added isActionDelete check
					return signInfo;
			}
		}
		return null;
	}
	/**
	 * This method checks if the back-end system is CAPS
	 * @return
	 */
	//AXAL3.7.40 New Method
	protected boolean isSystemIdCAPS() {
		return getSystemId().equals(SYST_CAPS);
	}
	/**
	 * This method checks if the back-end system is CAPS
	 * @return
	 */
	//P2AXAL007 New Method
	protected boolean isSystemIdLIFE70() {
		return getSystemId().equals(SYST_LIFE70);
	}  
	/** 
	 * This method checks if the value object is marked for deletion.
	 * @param obj
	 * @return
	 */
	//AXAL3.7.40 New Method, NBA234 Changed Method Signature
	public boolean isDeleted(NbaContractVO obj) {
		return NbaUtils.isDeleted(obj);
	}
	/**
	 * @param applicationJurisdiction
	 * @param policyProduct
	 * @return
	 */
	//AXAL3.7.40 New Method
	public boolean isValidApplicationJurisdiction(long applicationJurisdiction, List jurisdictionApprovals) {
		for (int i = 0; i < jurisdictionApprovals.size(); i++) {
			JurisdictionApproval jurisdictionApproval = (JurisdictionApproval) jurisdictionApprovals.get(i); // NBA237
			if (jurisdictionApproval != null) {
				long jurisdiction = jurisdictionApproval.getJurisdiction();
				if (jurisdiction == applicationJurisdiction && isActiveInJurisdiction(jurisdictionApproval)) { // NBLXA-2063
					return true; // APSL3172 NBLXA-2063
				}
			}
		}
		return false; // NBLXA-2063
	}
	/**
	 * Return the PolicyProduct for a CovOption.
	 * @param covOption the CovOption
	 * @return PolicyProduct
	 */
	//AXAL3.7.40 New Method
	protected PolicyProduct getPolicyProductFor(CovOption covOption) {
		return getPolicyProductFor(getCoverage()); // AXAL3.7.40
	}
	/**
	 * @param paymentModeMethProduct
	 * @return
	 */
	//AXAL3.7.40 New Method
	public PaymentModeMethProductExtension getPaymentModeMethProductExtension(PaymentModeMethProduct paymentModeMethProduct) {
		if (paymentModeMethProduct == null) {
			return null;
		}
		int extCount = paymentModeMethProduct.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			com.csc.fs.dataobject.accel.product.OLifEExtension extension = paymentModeMethProduct.getOLifEExtensionAt(index);
			if (extension != null) {
				if ((NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode())) && (extension.isPaymentModeMethProductExtension())) {//NBA093
					return extension.getPaymentModeMethProductExtension();
				}
			}
		}
		return null; // none could be found
	}

	/**
	 * check for CTIR rider
	 * @param coverages
	 * @return
	 */
	//AXAL3.7.40 New Method
	public boolean isRiderExist(long productType) {
		List coverages = getLife().getCoverage();
		if (coverages != null) {
			for (Iterator iter = coverages.iterator(); iter.hasNext();) {
				Coverage coverage = (Coverage) iter.next();
				if (coverage.getIndicatorCode() == OLI_COVIND_RIDER && productType == coverage.getLifeCovTypeCode()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @param ageAmtProduct
	 * @param distributionChannel
	 * @return
	 */
	//AXAL3.7.40 New Method
	public boolean checkDistributionChannel(AgeAmtProduct ageAmtProduct, long distributionChannel) {
		AgeAmtProductExtension ageAmtProductExtension = getAgeAmtProductExtension(ageAmtProduct);
		if(ageAmtProductExtension == null) return true;//P2AXAL016
		List distributionInfoList = ageAmtProductExtension.getDistributionInfo();
		if (distributionInfoList == null|| distributionInfoList.size()==0) return true;//P2AXAL016
		for (int i = 0; distributionInfoList != null && i < distributionInfoList.size(); i++) {
			DistributionInfo distributionInfo = ageAmtProductExtension.getDistributionInfoAt(i);
			if (distributionInfo.getDistributionChannel() == OLI_DISTCHAN_1009800001  || distributionInfo.getDistributionChannel() == distributionChannel) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param paymentModeMethProduct
	 * @return
	 */
	//AXAL3.7.40 New Method
	public AgeAmtProductExtension getAgeAmtProductExtension(AgeAmtProduct ageAmtProduct) {
		if (ageAmtProduct == null) {
			return null;
		}
		int extCount = ageAmtProduct.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			com.csc.fs.dataobject.accel.product.OLifEExtension extension = ageAmtProduct.getOLifEExtensionAt(index);
			if (extension != null) {
				if ((NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode())) && (extension.isAgeAmtProductExtension())) {//NBA093
					return extension.getAgeAmtProductExtension();
				}
			}
		}
		return null; // none could be found
	}
	/**
	 * check for
	 * @param coverages
	 * @return
	 */
	//AXAL3.7.40 New Method
	public CovOption getCovOption(List covOptions, long benefitTypeCode) {
		if (covOptions == null) {
			return null;
		}
		for (Iterator iter = covOptions.iterator(); iter.hasNext();) {
			CovOption covOption = (CovOption) iter.next();
			if (covOption.getLifeCovOptTypeCode() == benefitTypeCode) {
				return covOption;
			}
		}
		return null;
	}
	/**
	 * check for covoption
	 * @param coverages
	 * @return
	 */
	//AXAL3.7.40 New Method
	public Coverage getCoverage(List coverages, long coverageTypeCode) {
		if (coverages == null) {
			return null;
		}
		for (Iterator iter = coverages.iterator(); iter.hasNext();) {
			Coverage coverage = (Coverage) iter.next();
			if (coverage.getLifeCovTypeCode() == coverageTypeCode) {
				return coverage;
			}
		}
		return null;

	}
	/**
	 * Retrieves the appropriate jurisdiction-specific ProductAgeInfo object from a LifeProductExtension.
	 * @param lpExtension The LifeProductExtension object for the product
	 * @param appInfo The ApplicationInfo object for the contract.
	 * @return
	 */
	// AXAL3.7.40 New Method
	protected ProductAgeInfo getProductAgeInfo(LifeProductExtension lpExtension, ApplicationInfo appInfo) {
		ProductAgeInfo pai = null;
		if (appInfo != null && appInfo.hasApplicationJurisdiction()) {
			for (int i = 0; lpExtension != null && i < lpExtension.getProductAgeInfoCount(); i++) {
				pai = lpExtension.getProductAgeInfoAt(i);
				boolean found = false;
				JurisdictionCC juris = null;
				if (pai.getJurisdictionCCCount() > 0) {
					for (int j = 0; j < pai.getJurisdictionCCCount() && !found; j++) {
						juris = pai.getJurisdictionCCAt(j);
						if (juris.getLongValue() == appInfo.getApplicationJurisdiction()) {
							found = true;
						}
					}
					if (!found) {
						pai = null;
					}
				} else {
					break;
				}
			}
		}
		return pai;
	}
	/**
	 * Get the UnderwritingClassProduct for the coverage
	 * @param coverage
	 * @return UnderwritingClassProduct
	 */
	// AXAL3.7.40 - Rewrote Method //P2AXAL016 changed method signature
	public UnderwritingClassProduct getUnderwritingClassProductFor(Coverage coverage) {
		return getUnderwritingClassProductFor(coverage, POS1);//P2AXAL016
	}

	/**
	 * Get the UnderwritingClassProduct for the coverage
	 * @param coverage
	 * @return UnderwritingClassProduct
	 */
	//P2AXAL055 New Method
	public UnderwritingClassProduct getUnderwritingClassProductFor(Coverage coverage, int criteria) {
		return getUnderwritingClassProductFor(coverage, null, criteria);
	}	

	/**
	 * Get the UnderwritingClassProduct for the coverage
	 * @param coverage
	 * @param criteria
	 * @return UnderwritingClassProduct
	 */
	// AXAL3.7.40 - New Method //P2AXAL016 //P2AXAL055 changed method signature
	public UnderwritingClassProduct getUnderwritingClassProductFor(Coverage coverage,LifeParticipant lifeParticipant, int criteria) {
		if (lifeParticipant == null) {//P2AXAL055
			lifeParticipant = getLifeParticipantForLookup(coverage);
		}
		//Begin ALS3033
		long gender = lifeParticipant.getIssueGender(); 
		if ( coverage.getIndicatorCode() != OLI_COVIND_BASE) {
			gender = getNbaTXLife().getParty(lifeParticipant.getPartyID()).getGender();
		}
		// End ALS3033

		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
					"getUnderwritingClassProduct: gender=" + String.valueOf(gender) + ",smokerStat=" + String.valueOf(getSmokerStat(lifeParticipant))
					+ ",uwClass=" + String.valueOf(lifeParticipant.getUnderwritingClass()) + ",age="
					+ String.valueOf(lifeParticipant.getIssueAge()) + ",applicationType=" + getApplicationInfo().getApplicationType()
					+ ",qualPlanType=" + String.valueOf(getLife().getQualPlanType()));
		}
		boolean isAppTypeBestMatch = false;
		UnderwritingClassProduct temp = null;
		UnderwritingClassProduct bestMatchProduct = null; //ALS3333
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);
		for (int i = 0; coverageProduct != null && i < coverageProduct.getUnderwritingClassProductCount(); i++) {
			temp = (UnderwritingClassProduct) coverageProduct.getUnderwritingClassProduct().get(i);
			if (productMatches(temp, lifeParticipant,  gender, criteria | POS3)) {//P2AXAL016 ////CR731686
				//P2AXAL016 Begin
				if(hasApplicationType(temp)){
					isAppTypeBestMatch = true;
				} else if (isAppTypeBestMatch) {
					continue;
				}
				//P2AXAL016 End						
				bestMatchProduct = temp;//ALS3333


			}
		}
		if(bestMatchProduct != null) { //ALS3333
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("getUnderwritingClassProductFor found: " + bestMatchProduct.getId());//ALS3333
			}
			return bestMatchProduct;//ALS3333
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("getUnderwritingClassProductFor found: NULL");
		}
		return null;
	}

	/**
	 * Get the UnderwritingClassProducts for the coverage, lifeParticipant and criteria
	 */
	//CR731686 new method
	public List getAllUnderwritingClassProductsFor(Coverage coverage,LifeParticipant lifeParticipant, int criteria) {
		if (lifeParticipant == null) {
			lifeParticipant = getLifeParticipantForLookup(coverage);
		}
		long gender = lifeParticipant.getIssueGender(); 
		if ( coverage.getIndicatorCode() != OLI_COVIND_BASE) {
			gender = getNbaTXLife().getParty(lifeParticipant.getPartyID()).getGender();
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
					"getUnderwritingClassProduct: gender=" + String.valueOf(gender) + ",smokerStat=" + String.valueOf(getSmokerStat(lifeParticipant))
					+ ",uwClass=" + String.valueOf(lifeParticipant.getUnderwritingClass()) + ",age="
					+ String.valueOf(lifeParticipant.getIssueAge()) + ",applicationType=" + getApplicationInfo().getApplicationType()
					+ ",qualPlanType=" + String.valueOf(getLife().getQualPlanType()));
		}
		UnderwritingClassProduct temp = null;
		List bestMatchProducts = new ArrayList(); 
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);
		for (int i = 0; coverageProduct != null && i < coverageProduct.getUnderwritingClassProductCount(); i++) {
			temp = (UnderwritingClassProduct) coverageProduct.getUnderwritingClassProduct().get(i);
			if (productMatches(temp, lifeParticipant,  gender, criteria)) {
				bestMatchProducts.add(temp);
			}
		}
		return bestMatchProducts;
	}	

	/**
	 * Get the Rate classes allowed from PPFL with searching UnderwritingClassProducts for the coverage, lifeParticipant and criteria
	 */
	//CR731686 new method
	public Set getAllowedRateClasses(Coverage coverage, LifeParticipant lifeParticipant) {
		Set rateClasses = new HashSet();
		List underwritingClassProducts = getAllUnderwritingClassProductsFor(coverage, lifeParticipant, NbaContractValidationConstants.POS1
				| NbaContractValidationConstants.POS2);
		for (int i = 0; i < underwritingClassProducts.size(); i++) {
			UnderwritingClassProduct underwritingClassProduct = (UnderwritingClassProduct) underwritingClassProducts.get(i);
			if (underwritingClassProduct.hasUnderwritingClass()) {
				rateClasses.add(String.valueOf(underwritingClassProduct.getUnderwritingClass()));
			}
		}
		return rateClasses;
	}	

	/**
	 * Evaluate whether the given UnderwritingClassProduct object matches the case data.
	 * @param temp
	 * @param lifeParticipant
	 * @param gender
	 * @param compCriteria
	 * @return boolean (if the object is a match)
	 */
	// AXAL3.7.40 New Method //P2AXAl016 method Refactored
	private boolean productMatches(UnderwritingClassProduct temp, LifeParticipant lifeParticipant,  long gender, int compCriteria) {
		//Begin P2AXAl016 Code moved from method getUnderwritingClassProduct
		int age = lifeParticipant.getIssueAge() ;
		long uwClass = lifeParticipant.getUnderwritingClass();
		long smokerStat = getSmokerStat(lifeParticipant);
		long qualPlanType = getLife().getQualPlanType();//ALPC066
		long distributionChannel = 0;		
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtn != null) {
			distributionChannel = policyExtn.getDistributionChannel();
		}
		long jurisdiction = getApplicationInfo() == null ? -1L : getApplicationInfo().getApplicationJurisdiction();		
		//End P2AXAl016 Code moved from method getUnderwritingClassProduct


		qualPlanType = qualPlanType == -1 ? NbaOliConstants.OLI_QUALPLN_NONE : qualPlanType; //ALPC066
		if (temp.hasSmokerStat() && temp.getSmokerStat() != smokerStat)  return false; // SmokerStat Unmatched
		//Begin CR731686
		if((compCriteria & POS3 ) > 0){
			if (temp.hasUnderwritingClass() && temp.getUnderwritingClass() != uwClass)
				return false; // UW Class Unmatched
		}//End CR731686	
		if (temp.hasIssueGender() 
				&& temp.getIssueGender() != gender 
				&& temp.getIssueGender()  != OLI_GENDER_UNISEX
				&& temp.getIssueGender()  != OLI_GENDER_COMBINED )	return false; // Gender Unmatched  
		UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(temp);

		//Face Amount Moved up by ALII1436
		if ((compCriteria & POS2 ) > 0){
			double faceAmt = getLife().getFaceAmt();
			if (faceAmt>=0 && temp.hasMinIssueAmt() && temp.getMinIssueAmt() > faceAmt)  return false; // FaceAmount less than minimum
			if (faceAmt>=0 && temp.hasMaxIssueAmt() && temp.getMaxIssueAmt() < faceAmt)  return false; // FaceAmount more than Maximum
		}		

		if (ext != null) {
			//jurisdiction
			boolean jurisdictionPassed = false;			
			if (ext.getJurisdictionCCCount() > 0) {
				for (int i = 0; i < ext.getJurisdictionCCCount(); i++) {
					JurisdictionCC jurisdictionCC = (JurisdictionCC) ext.getJurisdictionCC().get(i);
					for (int j = 0; j < jurisdictionCC.getJurisdictionCount(); j++) {
						if (jurisdictionCC.getJurisdictionAt(j) == jurisdiction) {
							jurisdictionPassed = true;
						}
					}
				}//Begin ALS3333 
			}else if(ext.getJurisdictionApprovalCount()>0){
				for (int i = 0; i < ext.getJurisdictionApprovalCount(); i++) {
					JurisdictionApproval jurisdictionApproval = (JurisdictionApproval) ext.getJurisdictionApproval().get(i); //P2AXAL006
					if (jurisdictionApproval.getJurisdiction() == jurisdiction) {
						jurisdictionPassed = true;
					}
				}//End ALS3333	
			} else {
				jurisdictionPassed = true;
			}
			if (!jurisdictionPassed) return false;

			if (ext.hasQualifiedPlan() && ext.getQualifiedPlan() != qualPlanType) return false;//ALPC066

			//AgeAmtProduct Added by ALII1436
			boolean matchingAgeAmtProduct = (ext.getAgeAmtProductCount() <= 0);
			for (int i = 0; ext != null && i < ext.getAgeAmtProductCount(); i++) {
				if (productMatches(ext.getAgeAmtProductAt(i), lifeParticipant,   compCriteria )) {
					matchingAgeAmtProduct = true;
				}
			}
			if (!matchingAgeAmtProduct) return false;			

			//P2AXAL016 Begin
			//Application Type
			long applicationType = getApplicationInfo().getApplicationType(); 
			if (ext.hasApplicationType() && ext.getApplicationType() != applicationType) return false;
			//P2AXAL016 End
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("productMatches UnderwritingClassProduct Id: " + temp.getId());//ALS3333
		}		
		return true;
	}
	//P2AXAL055 New Method
	protected int[] getMinMaxAges(UnderwritingClassProductExtension ext, long distributionChannel) {
		return getMinMaxAges(ext,distributionChannel,null);
	}
	/**
	 * Determine the minimum and maximum ages for the distribution channel
	 * 
	 * @param ext
	 * @param distributionChannel
	 * @return long[] ([0] is the minimum age, [1] is the maximum age)
	 */
	// AXAL3.7.40 New Method //P2AXAL055 Changed method signature
	protected int[] getMinMaxAges(UnderwritingClassProductExtension ext, long distributionChannel, LifeParticipant lifeParticipant) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("IN getMinMaxAges() distributionChannel == "+distributionChannel );
		}
		int ages[] = { Integer.MAX_VALUE, Integer.MIN_VALUE }; // Initialize to MAX_VALUE, MIN_VALUE so that any data from PPfL will overlay it
		AgeAmtProduct ageAmount = null;
		for (int i = 0; ext != null && i < ext.getAgeAmtProductCount(); i++) {
			ageAmount = ext.getAgeAmtProductAt(i);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("IN getMinMaxAges() ageAmount == "+ageAmount );
			}
			if (checkDistributionChannel(ageAmount, distributionChannel) && checkAgebasisType(ageAmount, lifeParticipant)
					&& checkTableRating(ageAmount, lifeParticipant)) {// P2AXAL055 // QC16321/APSL4533) {//P2AXAL055
				if (ageAmount.hasMinAge() && ageAmount.getMinAge() < ages[0]) {// ALS3033
					ages[0] = ageAmount.getMinAge();
				}
				if (ageAmount.hasMaxAge() && ageAmount.getMaxAge() > ages[1]) {// ALS3033
					ages[1] = ageAmount.getMaxAge();
				}
			}
		}
		return ages;
	}
	// AXAL3.7.40 - Rewrote Method //P2AXAL024 changed method visibility to public
	public int getMaxIssueAge(Coverage coverage) throws NbaBaseException{ //ALS4095
		int maxAge = 0; //ALS4095
		try{
			//P2AXAL016 Code Deleted
			long distributionChannel = 0;
			UnderwritingClassProduct product = getUnderwritingClassProductFor(coverage,getLifeParticipant(), 0);//P2AXAL055
			UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(product);
			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
			if (policyExtn != null) {
				distributionChannel = policyExtn.getDistributionChannel();
			}
			if (coverage.getLifeCovTypeCode() == OLI_COVTYPE_CHILDTERM) {
				if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_DEP) {
					CoverageProductExtension cpExt = getCoverageProductExtensionFor(coverage);
					if (cpExt != null) {
						return cpExt.getChildMaxIssueAge();
					}
				} else if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
					return product.getMaxIssueAge(); //return Integer.MAX_VALUE;
				}
			}
			if (getNbaConfigValProc().hasModel()) {
				// Max Age is retrieved from a VPMS model for 148 series with substandard
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation();
				if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
					if (nbaVpmsResultsData.getResultsData().size() == 1) {
						String strAge = (String) nbaVpmsResultsData.getResultsData().get(0);
						int age = Integer.parseInt(strAge);
						if (age >= 0) {
							return age;
						}
					} else {
						addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
					}
				}
			}
			int ages[] = getMinMaxAges(ext, distributionChannel,getLifeParticipant());//P2AXAL055
			//begin	ALS4095
			maxAge = ages[1];
		} catch (Exception e){
			getLogger().logDebug("Product rules not found, missing underwriting data."+ e.getMessage());//P2AXAL006
			throw new NbaBaseException("Product rules not found, missing underwriting data.");
		}
		return maxAge;
		//end ALS4095
	}
	//P2AXAL055 GetMaxIssueAge(Coverage coverage, CovOption covOption) Method Retired

	// AXAL3.7.40 - Rewrote Method //P2AXAL024 changed method visibility to public
	public int getMinIssueAge(Coverage coverage) throws NbaBaseException{ //ALS4095
		int minAge = 0; //ALS4095
		try{
			long distributionChannel = 0;
			//P2AXAL016 Code Deleted			
			UnderwritingClassProduct product = getUnderwritingClassProductFor(coverage,getLifeParticipant(),0);//P2AXAL016 //P2AXAL055
			UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(product);		
			if ( coverage.getLifeCovTypeCode() == OLI_COVTYPE_CHILDTERM) {
				if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_DEP) {
					return 0;
				}else if ( getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
					return product.getMinIssueAge();//Integer.MIN_VALUE;
				}
			}

			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
			if (policyExtn != null) {
				distributionChannel = policyExtn.getDistributionChannel();
			}
			int ages[] = getMinMaxAges(ext, distributionChannel,getLifeParticipant());//P2AXAL055
			//begin	ALS4095
			minAge = ages[0];
		}catch (Exception e){
			getLogger().logDebug("Product rules not found, missing underwriting data."+ e.getMessage());//P2AXAL006
			throw new NbaBaseException("Product rules not found, missing underwriting data.");
		}
		return minAge;
		//end ALS4095
	}
	// AXAL3.7.40 New Method
	protected List getUnisexStatesFor(Coverage coverage) {
		ArrayList list = new ArrayList();

		UnderwritingClassProduct temp = null;
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);
		for (int i = 0; coverageProduct != null && i < coverageProduct.getUnderwritingClassProductCount() && list.isEmpty(); i++) {
			temp = (UnderwritingClassProduct) coverageProduct.getUnderwritingClassProduct().get(i);
			if (temp.getIssueGender() == OLI_GENDER_UNISEX) {
				UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(temp);
				for (int j = 0; ext != null && j < ext.getJurisdictionCCCount(); j++) {
					JurisdictionCC jurisdictionCC = (JurisdictionCC) ext.getJurisdictionCC().get(j);
					for (int k = 0; k < jurisdictionCC.getJurisdictionCount(); k++) {
						list.add(new Long(jurisdictionCC.getJurisdictionAt(k)));
					}
				}
			}
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("getUnisexStatesFor " + coverage.getProductCode() + ": " + list.toString());
		}
		return list;
	}
	/**
	 * Get the AgeAmtProduct for the coverage
	 * @param coverage
	 * @return AgeAmtProduct
	 */
	// AXAL3.7.40 New Method //P2AXAL016 changed method signature //P2AXAL055 New Method
	public AgeAmtProduct getAgeAmtProductFor(Coverage coverage) {
		return getAgeAmtProductFor (coverage, getLifeParticipantForLookup(coverage));
	}	

	//P2AXAL055 New Method
	protected AgeAmtProduct getAgeAmtProductFor(Coverage coverage,  long distributionChannel) {
		return getAgeAmtProductFor (coverage,getLifeParticipantForLookup(coverage), distributionChannel);
	}


	/**
	 * Get the AgeAmtProduct for the coverage
	 * @param coverage
	 * @return AgeAmtProduct
	 */
	// AXAL3.7.40 New Method //P2AXAL055 changed method signature
	public AgeAmtProduct getAgeAmtProductFor(Coverage coverage, LifeParticipant lifeParticipant) {
		//ALS4516 Code Deleted
		long distributionChannel = 0;
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtn != null) {
			distributionChannel = policyExtn.getDistributionChannel();
		}
		return getAgeAmtProductFor (coverage,lifeParticipant, distributionChannel); //ALS4516 //P2AXAL055
	}

	//ALS4516 New Method //P2AXAL055 Method Signature changed
	protected AgeAmtProduct getAgeAmtProductFor(Coverage coverage, LifeParticipant lifeParticipant, long distributionChannel) {
		UnderwritingClassProduct temp = getUnderwritingClassProductFor(coverage,lifeParticipant, POS1);//P2AXAL055
		if(temp == null) //P2AXAL024
			return null;
		//P2AXAL055 Code deleted
		AgeAmtProduct ageAmtProd = getUnderwritingClassAgeAmtProduct(temp, lifeParticipant, distributionChannel);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("getAgeAmtProductFor found: " + (ageAmtProd == null ? "NULL" : ageAmtProd.getId()));
		}
		return ageAmtProd;
	}

	// AXAL3.7.40 New Method //P2AXAL055 Method Signature changed
	private AgeAmtProduct getUnderwritingClassAgeAmtProduct(UnderwritingClassProduct uwClassProduct, LifeParticipant lifeParticipant, long distributionChannel) {
		int age = lifeParticipant.getIssueAge();//P2AXAL055
		AgeAmtProduct ageAmount = null;
		AgeAmtProduct  ageAmountLoc = null; //NBLXA-1765
		UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(uwClassProduct);
		for (int i = 0; ext != null && i < ext.getAgeAmtProductCount(); i++) {
			ageAmount = ext.getAgeAmtProductAt(i);
			if (checkDistributionChannel(ageAmount, distributionChannel) && checkAgebasisType(ageAmount, lifeParticipant)) {//P2AXAL055
				// Begin NBLXA-1765
				if(checkDTPEligibility(ageAmount)) {
					if (ageAmount.hasMinAge() && ageAmount.getMinAge() <= age) {
						if (ageAmount.hasMaxAge() && age <= ageAmount.getMaxAge()) {
							return ageAmount;
						}
					}
				}else if (!isDTP(ageAmount) && ageAmount.hasMinAge() && ageAmount.getMinAge() <= age) {// End NBLXA-1765
					if (ageAmount.hasMaxAge() && age <= ageAmount.getMaxAge()) {
						ageAmountLoc = ageAmount;
					}
				}
			}
		}
		return ageAmountLoc;	//  NBLXA-1765
	}


	/**
	 * @param ageAmtProduct
	 * @param distributionChannel
	 * @return
	 */
	//NBLXA-1765 New Method
	public boolean checkDTPEligibility(AgeAmtProduct ageAmtProduct) {
		NbaParty writingAgent = getNbaTXLife().getWritingAgent();
		if(writingAgent==null){
			writingAgent = getNbaTXLife().getParty(getNbaTXLife().getPartyId(OLI_REL_ADDWRITINGAGENT,true));
		}
		if (writingAgent != null && writingAgent.getParty().hasProducer()) {
			CarrierAppointment objCarrierAppointment = (CarrierAppointment) writingAgent.getParty().getProducer().getCarrierAppointmentAt(0);
			if(!NbaUtils.isBlankOrNull(objCarrierAppointment) ){
				CarrierAppointmentExtension ext = NbaUtils.getFirstCarrierAppointmentExtension(objCarrierAppointment);
				if(!NbaUtils.isBlankOrNull(ext) && ext.hasDtp()){
					AgeAmtProductExtension ageAmtProductExtension = getAgeAmtProductExtension(ageAmtProduct);
					if(!NbaUtils.isBlankOrNull(ageAmtProductExtension) ){
						List distributionInfoList = ageAmtProductExtension.getDistributionInfo();
						for (int i = 0; distributionInfoList != null && i < distributionInfoList.size(); i++) {
							DistributionInfo distributionInfo = ageAmtProductExtension.getDistributionInfoAt(i);			
							if (!NbaUtils.isBlankOrNull(distributionInfo) && distributionInfo.hasDistributionChannelInfoSysKey() && (!NbaUtils.isBlankOrNull(distributionInfo.getDistributionChannelInfoSysKey().getPCDATA())) && distributionInfo.getDistributionChannelInfoSysKey().getPCDATA().equalsIgnoreCase(ext.getDtp())) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * @param ageAmtProduct
	 * @param distributionChannel
	 * @return
	 */
	//NBLXA-1765 New Method
	public boolean isDTP(AgeAmtProduct ageAmtProduct) {
		AgeAmtProductExtension ageAmtProductExtension = getAgeAmtProductExtension(ageAmtProduct);
		if(!NbaUtils.isBlankOrNull(ageAmtProductExtension) ){
			List distributionInfoList = ageAmtProductExtension.getDistributionInfo();
			for (int i = 0; distributionInfoList != null && i < distributionInfoList.size(); i++) {
				DistributionInfo distributionInfo = ageAmtProductExtension.getDistributionInfoAt(i);										
				if (!NbaUtils.isBlankOrNull(distributionInfo) && distributionInfo.hasDistributionChannelInfoSysKey() && (!NbaUtils.isBlankOrNull(distributionInfo.getDistributionChannelInfoSysKey().getPCDATA()))){
				}
				if (!NbaUtils.isBlankOrNull(distributionInfo) && distributionInfo.hasDistributionChannelInfoSysKey() && 
						(!NbaUtils.isBlankOrNull(distributionInfo.getDistributionChannelInfoSysKey().getPCDATA())) && 
						distributionInfo.getDistributionChannelInfoSysKey().getPCDATA().equalsIgnoreCase(NbaConstants.DTP_AXADISTRIBUTOTSDTP100K)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param ageAmtProduct
	 * @param distributionChannel
	 * @return
	 */
	//P2AXAL055 New Method
	public boolean checkAgebasisType(AgeAmtProduct ageAmtProduct, LifeParticipant lifeParticipant) {
		if(lifeParticipant!=null){
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("IN checkAgebasisType() ");
			}
			LifeParticipantExtension lPExt = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
			if(lPExt!=null){
				if (!ageAmtProduct.hasAgeBasisType() || lPExt.getAgeBasisType() <= 0) {
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("IN checkAgebasisType() Returing True");
					}
					return true;
				} 
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("IN checkAgebasisType() >> lPExt.getAgeBasisType() == "+lPExt.getAgeBasisType() +" and ageAmtProduct.getAgeBasisType() ==  "+ageAmtProduct.getAgeBasisType());
					getLogger().logDebug("IN checkAgebasisType() >> lPExt.getAgeBasisType() == ageAmtProduct.getAgeBasisType() ==  "+(lPExt.getAgeBasisType() == ageAmtProduct.getAgeBasisType()));
				}
				return ((lPExt.getAgeBasisType() == ageAmtProduct.getAgeBasisType()) || (ageAmtProduct.getAgeBasisType()==OLI_AGEBASIS_ISSUE));
			}	
		}
		return true;
	}	

	// P2AXAL055 New Method 
	public UnderwritingClassProduct getUnderwritingClassProduct(Coverage coverage, CovOption covOption) {
		return getUnderwritingClassProduct(coverage,null, covOption);
	}

	// AXAL3.7.40 - Rewrote Method //P2AXAL016 changed method signature
	public UnderwritingClassProduct getUnderwritingClassProduct(Coverage coverage,LifeParticipant lifeParticipant, CovOption covOption) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("IN getUnderwritingClassProduct()");
		}
		
		if(lifeParticipant==null){//P2AXAL055
			lifeParticipant = getLifeParticipantForLookup(coverage);
		}
		//ALS3033,ALS1514
		long gender = lifeParticipant.getIssueGender();
		if (coverage.getIndicatorCode() != OLI_COVIND_BASE) {
			gender = getNbaTXLife().getParty(lifeParticipant.getPartyID()).getGender();
		}
		//ALS3033,ALS1514
		//P2AXAl016	Code Moved to method productMatches
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
					"getUnderwritingClassProduct: gender=" + String.valueOf(gender) + ",smokerStat=" + String.valueOf(getSmokerStat(lifeParticipant))
					+ ",uwClass=" + String.valueOf(lifeParticipant.getUnderwritingClass()) + ",age="
					+ String.valueOf(lifeParticipant.getIssueAge()) + ",covOpt=" + covOption.getProductCode() + ",applicationType="
					+ getApplicationInfo().getApplicationType() + ",qualPlanType=" + String.valueOf(getLife().getQualPlanType()));
		}		
		UnderwritingClassProduct temp = null;
		CovOptionProduct covOptProduct = null;
		CovOptionProductExtension covOptProdExtension = null;
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);
		if (getLogger().isDebugEnabled() && coverageProduct != null) {
			getLogger().logDebug("coverageProduct id"+coverageProduct.getId());
			getLogger().logDebug("coverageProduct >> CovOptionProduct Count >>> "+coverageProduct.getCovOptionProductCount());
		}
		for (int j = 0; coverageProduct != null && j < coverageProduct.getCovOptionProductCount(); j++) {
			covOptProduct = coverageProduct.getCovOptionProductAt(j);
			if (getLogger().isDebugEnabled()&& covOptProduct != null) {
				getLogger().logDebug("covOptProduct Product Code"+covOptProduct.getProductCode());
				getLogger().logDebug("covOptProduct id"+covOptProduct.getId());
				getLogger().logDebug("Is covOptProduct active >>> "+isActiveCoverageProduct(covOptProduct));
			}
			
			covOptProdExtension = getCovOptionProductExtensionFor(covOptProduct);
			//  NBLXA 1988 
			if ((covOptProduct.getProductCode().equals(covOption.getProductCode()) && isActiveCoverageProduct(covOptProduct))|| 
					(covOptProdExtension != null && covOptProdExtension.hasLifeCovOptTypeCode() &&
					covOptProdExtension.getLifeCovOptTypeCode() == covOption.getLifeCovOptTypeCode()) && isActiveCoverageProduct(covOptProduct)) {
				UnderwritingClassProduct bestMatchProduct = null; //ALS3333
				boolean isAppTypeBestMatch = false;//P2AXAL016
				boolean isAppJurisdictionBestMatch = false; //NBLXA-2115
				if (getLogger().isDebugEnabled() && covOptProduct != null) {
					getLogger().logDebug("covOptProduct.getUnderwritingClassProductCount() >>"+covOptProduct.getUnderwritingClassProductCount());
				}
				for (int i = 0; i < covOptProduct.getUnderwritingClassProductCount(); i++) {
					temp = (UnderwritingClassProduct) covOptProduct.getUnderwritingClassProduct().get(i);
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("UnderwritingClassProduct id >>"+temp.getId());
					}
					if (productMatches(temp, lifeParticipant, gender, POS3)) {//ALPC066//P2AXAL016 //CR731686
						//P2AXAL016 Begin //NBLXA-2115 Start
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("hasApplicationType temp >>"+hasApplicationType(temp));
							getLogger().logDebug("hasApplicationJurisduction temp >>"+hasApplicationJurisduction(temp));
						}
						if(hasApplicationType(temp) && hasApplicationJurisduction(temp)){
							isAppTypeBestMatch = true;
							isAppJurisdictionBestMatch = true;
							if (getLogger().isDebugEnabled()) {
								getLogger().logDebug("has Application Type and Application Jurisduction >> "+temp.getId());
							}
						} else if(!isAppTypeBestMatch && hasApplicationType(temp)){
							isAppTypeBestMatch = true;
							if (getLogger().isDebugEnabled()) {
								getLogger().logDebug("has only Application Type >> "+temp.getId());
							}
						} else if (!isAppJurisdictionBestMatch && hasApplicationJurisduction(temp)){ 
							isAppJurisdictionBestMatch = true;
							if (getLogger().isDebugEnabled()) {
								getLogger().logDebug("has ApplicationJurisduction >> "+temp.getId());
							}
						} else if (isAppTypeBestMatch || isAppJurisdictionBestMatch) {
							if (getLogger().isDebugEnabled()) {
								getLogger().logDebug("Application Type and Application Jurisdiction already Matched.");
							}
							continue;
						}
						//P2AXAL016 End	//NBLXA-2115 End					
						bestMatchProduct = temp; //ALS3333
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("bestMatchProduct set to Temp "+temp.getId());
						}
					}
				}
				if(bestMatchProduct != null){//ALS3333
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("getUnderwritingClassProduct found: " + bestMatchProduct.getId()); //ALS3333
					}
					return bestMatchProduct;//ALS3333
				}
			}
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("getUnderwritingClassProduct found: NULL");
		}
		return null;
	}
	//AXAL3.7.40 - New Method
	protected LifeParticipant getLifeParticipantForLookup(Coverage coverage) {
		LifeParticipant lifeParticipant = null;
		if (coverage != null && coverage.hasIndicatorCode()) {
			if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
				lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			} else if (coverage.getIndicatorCode() == OLI_COVIND_RIDER && coverage.getLifeCovTypeCode() == OLI_COVTYPE_CHILDTERM){
				lifeParticipant = getNbaTXLife().getPrimaryInuredLifeParticipant(); 
			} else {
				lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			}
		}
		return lifeParticipant;
	}

	//AXAL3.7.40 - New Method
	protected NbaVpmsResultsData vpmsResultForDateRules() {
		long ageRule = getAgeRule();
		Map skipAttributesMap = new HashMap();
		skipAttributesMap.put("A_AgeRule", new Long(ageRule).toString());
		skipAttributesMap.put(NbaVpmsConstants.A_PROCESS_ID, getNbaTXLife().getBusinessProcess()); //ALS4938
		return performVpmsCalculation(skipAttributesMap, null, true);
	}

	//AXAL3.7.40 - New Method
	protected void executeDateRules() throws NbaVpmsException {
		String msgCode = "";
		String msgDescription = "";
		//QC9716/APSL2460 removed variables
		int restrictCode= 0;//QC9716/APSL2460
		NbaVpmsResultsData nbaVpmsResultsData = vpmsResultForDateRules();
		if (nbaVpmsResultsData != null) {
			List modelResults = nbaVpmsResultsData.getResultsData();
			if (modelResults != null && modelResults.size() > 0) {
				String resultXml = (String) modelResults.get(0);
				VpmsModelResult data = new NbaVpmsModelResult(resultXml).getVpmsModelResult();
				if (data != null) {
					for (int i = 0; i < data.getResultDataCount(); i++) {
						msgCode = data.getResultDataAt(i).getResultAt(0);
						msgDescription = data.getResultDataAt(i).getResultAt(1);
						if (msgCode != null && msgCode.length() > 0) {
							//Begin APSL2460/QC9716
							restrictCode = getRestrictCodeFromVpms(NbaVpmsConstants.EP_GET_MSGRESTRICTCODE,msgCode);
							if(restrictCode>0){
								//End APSL2460/QC9716
								getNbaConfigValProc().setRestrict(restrictCode);
								addNewSystemMessage(msgCode, msgDescription, getIdOf(getHolding()));
							} else {
								getNbaConfigValProc().setRestrict("");
								addNewSystemMessage(msgCode, msgDescription, getIdOf(getHolding()));
							}
						}
					}
				}
			}
		}
	}	

	//AXAL3.7.40 - New Method
	protected void addDateRuleMessage() throws NbaVpmsException {
		String msgCode = "";
		String msgDescription = "";
		NbaVpmsResultsData nbaVpmsResultsData = vpmsResultForDateRules();
		if (nbaVpmsResultsData != null) {
			List modelResults = nbaVpmsResultsData.getResultsData();
			if (modelResults != null && modelResults.size() > 0) {
				String resultXml = (String) modelResults.get(0);
				VpmsModelResult data = new NbaVpmsModelResult(resultXml).getVpmsModelResult();
				if (data != null && data.getResultDataCount() > 0) {
					msgCode = data.getResultDataAt(0).getResultAt(0);
					msgDescription = data.getResultDataAt(0).getResultAt(1);
					if (getNbaConfigValProc().getMsgcode().equalsIgnoreCase(msgCode)) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), msgDescription, getIdOf(getHolding()));
					}
				}
			}
		}
	}	

	//AXAL3.7.40 - New Method
	protected void setEffectiveDate() {
		long ageRule = getAgeRule();
		Map skipAttributesMap = new HashMap();
		Date origPolicyEffDate = getPolicy().getEffDate();
		skipAttributesMap.put("A_AgeRule", new Long(ageRule).toString());
		//Begin APSL3665
		if (getPolicy().hasMinPremiumInitialAmt()) {
			skipAttributesMap.put("A_MinPremiumInitialAmt", new Double(getPolicy().getMinPremiumInitialAmt()).toString());
		}
		//End APSL3665
		skipAttributesMap.put(NbaVpmsConstants.A_PROCESS_ID, getNbaTXLife().getBusinessProcess()); //ALPC234
		//start QC14171/APSL3994
		NbaDst parentCase = getNbaDst();
		if(!getNbaDst().isCase()){
			parentCase = retrieveParentCase(getNbaDst(), getUserVO(), false); 
		}
		skipAttributesMap.put("A_ContractChgTypeLOB", NbaUtils.isBlankOrNull(parentCase.getNbaLob().getContractChgType())? "" : parentCase.getNbaLob().getContractChgType());
		skipAttributesMap.put("A_ContractApprovedDate", NbaUtils.getStringFromDate(getNbaTXLife().getPolicy().getApplicationInfo().getHOCompletionDate()));//APSL4287
		//end QC14171/APSL3994
		//Start NBLXA-1542
		List activityList = NbaUtils.getActivityByTypeCode(getNbaTXLife().getOLifE().getActivity(), NbaOliConstants.OLI_ACTTYPE_1009900003);
		if (activityList.size() > 0) {
			skipAttributesMap.put("A_FirstApprovedDate", NbaUtils.getStringFromDate(((Activity) activityList.get(0)).getDoneDate()));
		}
		//End NBLXA-1542		
		NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(skipAttributesMap, null, true);
		if (nbaVpmsResultsData != null) {
			if (nbaVpmsResultsData.wasSuccessful()) {
				String effStringDate = (String) nbaVpmsResultsData.getResultsData().get(0);
				try {
					Date effDate = get_YYYY_MM_DD_sdf().parse(effStringDate);
					getPolicy().setEffDate(effDate);
					getPolicy().setActionUpdate();
					// determineRePrint(origPolicyEffDate,effDate); //ALPC234 //NBLXA-222
				} catch (ParseException e) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Unknown date format returned from VP/MS model: ",
							getNbaConfigValProc().getModel(), ", Date: ", effStringDate), getIdOf(getPolicy()));
				}
			}
		} else {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), ", Model: ", getNbaConfigValProc().getModel()),
					getIdOf(getPolicy()));
		}
		if (getPolicy().hasEffDate()) {
			Date effDate = getPolicy().getEffDate();
			if (getLife() != null) { // Life products
				for (int i = 0; i < getLife().getCoverageCount(); i++) {
					Coverage coverage = getLife().getCoverageAt(i);
					coverage.setEffDate(effDate);
					coverage.setActionUpdate();
					for (int j = 0; j < coverage.getCovOptionCount(); j++) {
						CovOption covOption = coverage.getCovOptionAt(j);
						covOption.setEffDate(effDate);
						covOption.setActionUpdate();
						for (int k = 0; k < covOption.getSubstandardRatingCount(); k++) {
							SubstandardRating substandardRating = covOption.getSubstandardRatingAt(k);
							SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
							substandardRatingExtension.setEffDate(effDate);
							substandardRatingExtension.setActionUpdate();
							//NBLXA-2122 Begins
							if (substandardRatingExtension.hasDuration()) {
								NbaUtils.setTempEndDate(substandardRating, substandardRatingExtension.getEffDate(), substandardRatingExtension.getDuration());
							}
							//NBLXA-2122 Ends
						}
					}
					for (int j = 0; j < coverage.getLifeParticipantCount(); j++) {
						LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(j);
						for (int k = 0; k < lifeParticipant.getSubstandardRatingCount(); k++) {
							SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(k);
							SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
							substandardRatingExtension.setEffDate(effDate);
							substandardRatingExtension.setActionUpdate();
							//NBLXA-2122 Begins
							if (substandardRatingExtension.hasDuration()) {
								NbaUtils.setTempEndDate(substandardRating, substandardRatingExtension.getEffDate(),
										substandardRatingExtension.getDuration());
								if (substandardRating.hasTempTableRatingEndDate()) {
									lifeParticipant.setTempTableRatingEndDate(substandardRating.getTempTableRatingEndDate());
								} else{
									lifeParticipant.setTempFlatEndDate(substandardRating.getTempFlatEndDate());
								}
								lifeParticipant.setActionUpdate();
							}
							//NBLXA-2122 Ends
						}
					}
				}
			}
		}
	}
	// get AgeRule from PPFL
	protected long getAgeRule () {
		long ageRule = 11; //Age last Birthdate
		ArrayList coverageList = getLife().getCoverage();
		if (coverageList != null) {
			for (int i = 0; i < coverageList.size(); i++) {
				coverage = (Coverage) coverageList.get(i);
				if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
					PolicyProduct policyProduct = getPolicyProductFor(coverage);
					if (policyProduct != null && policyProduct.hasAgeCalculationType()) {
						ageRule = policyProduct.getAgeCalculationType();
					}
				}
			}  //end for
		}
		return ageRule;
	}
	/**
	 * Returns the signatureInfo.
	 * @return SignatureInfo
	 */
	//AXAL3.7.40 New Method
	protected SignatureInfo getSignatureInfo() {
		return signatureInfo;
	}
	/**
	 * Sets the signatureInfo.
	 * @param signatureInfo The SignatureInfo to set
	 */
	//AXAL3.7.40 New Method
	protected void setSignatureInfo(SignatureInfo signatureInfo) {
		this.signatureInfo = signatureInfo;
	}
	/**
	 * Returns the formInstance.
	 * @return FormInstance
	 */
	//AXAL3.7.40 New Method
	protected FormInstance getFormInstance() {
		return formInstance;
	}
	/**
	 * Sets the formInstance.
	 * @param formInstance The FormInstance to set
	 */
	//AXAL3.7.40 New Method
	protected void setFormInstance(FormInstance formInstance) {
		this.formInstance = formInstance;
	}
	/**
	 * Returns the hhFamilyInsurance.
	 * @return hhFamilyInsurance
	 */
	//AXAL3.7.40 New Method
	protected HHFamilyInsurance getHHFamilyInsurance() {
		return hhFamilyInsurance;
	}
	/**
	 * Sets the hhFamilyInsurance.
	 * @param hhFamilyInsurance The hhFamilyInsurance to set
	 */
	//AXAL3.7.40 New Method
	protected void setHHFamilyInsurance(HHFamilyInsurance hhFamilyInsurance) {
		this.hhFamilyInsurance = hhFamilyInsurance;
	}
	/**
	 * Determine if the SignatureInfo exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//AXAL3.7.40 New Method
	protected boolean findNextSignatureInfo(int occurrence) {
		if (getFormInstance() != null) {
			if (getFormInstance().getSignatureInfoCount() > occurrence) {
				setSignatureInfo(getFormInstance().getSignatureInfoAt(occurrence));
				logDebug(getSignatureInfo().getId() + " found");
				return true;
			}
		}
		setSignatureInfo(null);
		return false;
	}
	/**
	 * Determine if the SignatureInfo exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//AXAL3.7.40 New Method
	protected boolean findNextAppSignatureInfo(int occurrence) {
		if (getApplicationInfo() != null) {
			if (getApplicationInfo().getSignatureInfoCount() > occurrence) {
				setSignatureInfo(getApplicationInfo().getSignatureInfoAt(occurrence));
				logDebug(getSignatureInfo().getId() + " found");
				return true;
			}
		}
		setSignatureInfo(null);
		return false;
	}
	/**
	 * Determine if the FormInstance exists and create a pointer to it.
	 * 
	 * @param occurrence the occurrence to search for
	 */
	//AXAL3.7.40 New Method
	protected boolean findNextFormInstance(int occurrence) {
		if (getOLifE().getFormInstanceCount() > occurrence) {
			setFormInstance(getOLifE().getFormInstanceAt(occurrence));
			logDebug(getFormInstance().getId() + " found");
			return true;
		}
		setFormInstance(null);
		return false;
	}
	/**
	 * Returns true if it is owner's signature
	 */
	//AXAL3.7.40 New Method
	protected boolean isOwnerSignature(SignatureInfo signatureInfo) {
		if (signatureInfo != null) {
			if (!signatureInfo.isActionDelete() && signatureInfo.getSignatureRoleCode() == OLI_PARTICROLE_OWNER) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is Primary Insured's signature
	 */
	//AXAL3.7.40 New Method //P2AXAL054 method signature changed
	protected boolean isPrimaryInsured(SignatureInfo signatureInfo) {
		if (signatureInfo != null) {
			if (!signatureInfo.isActionDelete() && signatureInfo.getSignatureRoleCode() == OLI_PARTICROLE_PRIMARY) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is Survivorship Insured's signature
	 */
	//P2AXAL054 new method 
	protected boolean isSurvivorshipInsured(SignatureInfo signatureInfo) {
		if (signatureInfo != null) {
			if (!signatureInfo.isActionDelete()) {
				if (signatureInfo.getSignatureRoleCode() == OLI_PARTICROLE_PRIMARY || signatureInfo.getSignatureRoleCode() == OLI_PARTICROLE_JOINT) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if it is Primary Agent''s signature
	 */
	//AXAL3.7.40 New Method
	protected boolean isPrimaryAgentSignature(SignatureInfo signatureInfo) {
		if (signatureInfo != null) {
			if (!signatureInfo.isActionDelete() && signatureInfo.getSignatureRoleCode() == OLI_PARTICROLE_PRIMAGENT) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is MedSuppForm
	 */
	// AXAL3.7.40 New Method
	protected boolean isMedSuppForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_MEDSUP.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is CTRForm
	 */
	// AXAL3.7.40 New Method
	protected boolean isCTRForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_CTR.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is FTRForm
	 */
	// AXAL3.7.40 New Method
	protected boolean isFRTSForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_FRTS.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is ReplNyForm
	 */
	// AXAL3.7.40 New Method
	protected boolean isReplNyForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_REPLNY.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is OptBenForm
	 */
	// AXAL3.7.40 New Method
	protected boolean isOptBenForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_OPTBEN.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is FinSuppIIForm
	 */
	// AXAL3.7.40 New Method
	protected boolean isFinSuppIIForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_FINSUPII.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is SysPayForm
	 */
	// AXAL3.7.40 New Method
	protected boolean isSysPayForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_SYSPAY.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if it is VULSupp Form
	 */
	// P2AXAL027 New Method
	protected boolean isVULSuppForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_VULSUP.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if the form with given FormName present in the txLife.
	 */
	// AXAL3.7.40 New Method
	protected boolean verifyFormPresence(String formName) {
		return NbaUtils.getFormInstance(nbaTXLife, formName) != null ;
	}
	/**
	 * Determine if the party is an child by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an Child
	 */
	//AXAL3.7.40 New Method
	protected boolean isChild(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_DEPENDENT) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Determine if the Party is the Child by determining if 
	 * the person has a Child Relation
	 * @param insuredId - the insured id
	 * @return true if the party is a Beneficiary
	 */
	//AXAL3.7.40 New Method
	protected boolean isChildRelation(String relationId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getId().equals(relationId) && relation.getRelationRoleCode() == OLI_REL_DEPENDENT) {
					return true;
				}
			}
		}
		return false;
	}  
	/**
	 * @param formInstance
	 * @param questionNo
	 * @return
	 */
	//AXAL3.7.40 New Method
	protected long getFormResponseCode(FormInstance formInstance, String questionNo) {
		long responseCode =-1L ;
		if (formInstance != null && questionNo != null) {
			List formResponseList = formInstance.getFormResponse();
			for (int i = 0; formResponseList != null && i < formResponseList.size(); i++) {
				FormResponse formResponse = (FormResponse) formResponseList.get(i);
				if (formResponse != null && questionNo.equalsIgnoreCase(formResponse.getQuestionNumber())) {
					responseCode = formResponse.getResponseCode();
				}
			}
		}
		return responseCode;
	}

	//CR61047 new method
	protected long getFormResponseByAbbr(FormInstance formInstance, String abbrev) {
		List formResponseList = formInstance.getFormResponse();
		if (formInstance != null && abbrev != null) {
			for (int i = 0; formResponseList != null && i < formResponseList.size(); i++) {
				FormResponse formResponse = (FormResponse) formResponseList.get(i);
				FormResponseExtension formResponseExt = NbaUtils.getFirstFormResponseExtension(formResponse);
				if (formResponseExt != null && abbrev.equalsIgnoreCase(formResponseExt.getQuestionTypeAbbr())) {
					return formResponse.getResponseCode();
				}
			}
		}
		return -1;
	}

	//AXAL3.7.40 New Method Added.
	protected boolean isInsurableChildRelDescription(long relDesc) {
		int length = NbaConstants.INS_CHILD_REL_DESC.length;
		for (int i = 0; i < length; i++) {
			if (relDesc == NbaConstants.INS_CHILD_REL_DESC[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param productCode
	 * @return
	 */
	//AXAL3.7.40 New Method Added
	protected double getTotalPriorInsFaceAmtFor(String productCode) {
		double faceAmt = nbaTXLife.getFaceAmount();
		List attachmentList = AxaUtils.getAttachmentsByType(nbaTXLife.getPrimaryParty().getParty(), NbaOliConstants.OLI_ATTACH_PRIORINS);
		int attachmentListSize = attachmentList.size();
		for (int i = 0; i < attachmentListSize; i++) {
			Attachment attachment = (Attachment) attachmentList.get(i);
			String data = attachment.getAttachmentData().getPCDATA();
			if (data != null) {
				try {
					NbaTXLife attachmentTxLife = new NbaTXLife(data);
					if (attachmentTxLife.getPolicy() !=null && !nbaTXLife.getPolicy().getPolNumber().equalsIgnoreCase(attachmentTxLife.getPolicy().getPolNumber())) {
						//Begin QC12944/APSL3524
						Map deOink = new HashMap();
						deOink.put("A_POLICYSTATUS", String.valueOf(attachmentTxLife.getPolicy().getPolicyStatus()));
						if (checkPolicyStatus(NbaVpmsAdaptor.EP_CHECK_POLICY_STATUS, deOink)) {
							//End QC12944/APSL3524
							Life life = attachmentTxLife.getLife();
							if (life != null) {
								for (int j = 0; j < life.getCoverageCount(); j++) {
									Coverage coverage = life.getCoverageAt(j);
									double currentAmt = 0;
									if (NbaUtils.isBaseCoverage(coverage) && coverage.hasProductCode() && coverage.getProductCode().equalsIgnoreCase(productCode)) {
										if (coverage.hasCurrentAmt()) {
											currentAmt = coverage.getCurrentAmt();
										} else if (coverage.hasCurrentNumberOfUnits()) {
											currentAmt = coverage.getCurrentNumberOfUnits() * coverage.getValuePerUnit();
										}
										faceAmt = faceAmt + currentAmt;
									}
								}
							}
						}	
					}
				} catch (Exception e) {
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Not able to extract 204 response data from TxLife " + e.getMessage());
					}
				}
			}
		}
		return faceAmt;
	}
	//AXAL3.7.40 New Method Added.
	protected boolean isPrimaryOwner(Party party) {
		NbaParty nbParty = NbaUtils.getPrimaryOwner(nbaTXLife);
		if (nbParty != null && nbParty.getID().equalsIgnoreCase(party.getId())) {
			return true;
		}
		return false;
	}

	/**
	 * Determine if the HHFamilyInsurance exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//AXAL3.7.40 New Method
	protected boolean findNextHHFamilyInsurance(int occurrence) {
		if (getRisk() != null) {
			if (getRisk().getHHFamilyInsuranceCount() > occurrence) {
				setHHFamilyInsurance(getRisk().getHHFamilyInsuranceAt(occurrence));
				logDebug(getHHFamilyInsurance().getId() +  " found");
				return true;
			}
		}
		setHHFamilyInsurance(null);
		return false;
	}	


	/**
	 * @return Returns the userVO.
	 */
	//AXAL3.7.18 new method
	public NbaUserVO getUserVO() {
		return userVO;
	}
	/**
	 * @param userVO The userVO to set.
	 */
	//AXAL3.7.18 new method
	public void setUserVO(NbaUserVO userVO) {
		this.userVO = userVO;
	}

	//AXAL3.7.40 New Method Added.
	protected DistributionAgreementInfoExtension getDistAgreementInfoExtension(Producer producer) {
		if (producer != null && producer.getCarrierAppointmentCount() > 0) {
			for (int i = 0; i < producer.getCarrierAppointmentCount(); i++) {
				CarrierAppointment carrierAppt = producer.getCarrierAppointmentAt(i);
				if (carrierAppt != null && carrierAppt.getDistributionAgreementInfoCount() > 0) {
					for (int j = 0; j < carrierAppt.getDistributionAgreementInfoCount(); j++) {
						DistributionAgreementInfo aDistributionAgreementInfo = carrierAppt.getDistributionAgreementInfoAt(j);
						int extCount = aDistributionAgreementInfo.getOLifEExtensionCount();
						for (int index = 0; index < extCount; index++) {
							OLifEExtension extension = aDistributionAgreementInfo.getOLifEExtensionAt(index);
							if (extension != null) {
								if ((CSC_VENDOR_CODE.equals(extension.getVendorCode())) && (extension.isDistributionAgreementInfoExtension())) {
									return extension.getDistributionAgreementInfoExtension();
								}
							}
						}
					}
				}
			}
		}
		return null ;
	}
	/**
	 * Filter if there are  deleted objects in the object array.
	 * @param objects
	 * @return true if there are
	 */
	protected List getUndeletedObjects(ArrayList objects) {
		ArrayList undeletedObjectsList = new ArrayList();
		if (objects != null) {
			for (int i = 0; i < objects.size(); i++) {
				//Begin NBA234
				NbaContractVO nbaContractValueObject = (NbaContractVO) objects.get(i);
				if (!NbaUtils.isDeleted(nbaContractValueObject)) {
					undeletedObjectsList.add(nbaContractValueObject);
				}
				//End NBA234
			}
		}
		return undeletedObjectsList;
	}

	protected boolean hasFundDisclosureDetails(RiskExtension riskExtn) {
		boolean hasDetails = false;
		if (riskExtn.getFundingDisclosureDetails().size() > 0) {
			Iterator iter = riskExtn.getFundingDisclosureDetails().iterator();
			while (iter.hasNext()) {
				FundingDisclosureDetails details = (FundingDisclosureDetails) iter.next();
				if (!details.getActionIndicator().isDelete()) {
					hasDetails = true;
					break;
				}
			}

		}
		return hasDetails;
	}

	/**
	 * Contract Validation process calculates the pending closure data for a case 
	 * and will update the case add the data to the NBA_AUTO_CLOSURE table.
	 * @throws NbaBaseException 
	 * @throws ParseException 
	 */
	//New Method Added NBA254
	protected void setPlacementEndDateAndClosureType() throws NbaBaseException, ParseException {
		if (!PROC_FINAL_DISPOSITION.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())) { // QC14034/APSL3971
			ApplicationInfo appInfo = getApplicationInfo();
			ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (appInfoExtn == null) {
				OLifEExtension olife = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
				olife.setActionAdd();
				appInfo.addOLifEExtension(olife);
				appInfoExtn = olife.getApplicationInfoExtension();
				appInfoExtn.setActionAdd();
			}
			if (!appInfoExtn.hasClosureInd()) {
				appInfoExtn.setClosureInd(NbaConstants.CLOSURE_ACTIVE);// defalut value
			}
			if (!appInfoExtn.hasClosureOverrideInd()) { // ALS4592
				appInfoExtn.setClosureOverrideInd(FALSE);// defalut value //ALS4592
			}
			// ALS5708 Begin
			String printExtractGenerated = "No";
			if (appInfoExtn.hasContractPrintExtractDate()) {// ALS4506
				printExtractGenerated = "Yes";
			}
			// ALS5708 End
			if (ClosureCalculationRequired(printExtractGenerated, appInfoExtn.getClosureOverrideInd(), appInfo.getApplicationType(),
					appInfoExtn.getUnderwritingApproval())) {// ALS4592 //ALS5708
				// Begin AXAL3.7.57
				// check if print extract is generated or not
				// ALS4506 removed
				// ALS5708 moved the code before the if condition
				Map skipAttributes = new HashMap();
				skipAttributes.put("A_PrintExtractGenerated", printExtractGenerated);
				skipAttributes.put("A_ContractPrintDate", NbaUtils.getStringFromDate(appInfoExtn.getContractPrintExtractDate()));// ALS4506
				skipAttributes.put("A_ContractReprintDate", NbaUtils.getStringFromDate(appInfoExtn.getContractReprintDate()));// CR61047
				skipAttributes.put("A_ContractChangeReprintInd", getPolicyExtension().getContractChangeReprintInd() ? "true" : "false");// CR61047
				skipAttributes.put("A_DistributionChannel", String.valueOf(getPolicyExtension().getDistributionChannel()));
				skipAttributes.put("A_InformalOfferMade", NbaUtils.isInformalOfferMade(appInfoExtn) ? "1" : "0"); // ALS5041
				skipAttributes.put("A_InformalReceiptDate", getReceiptDate());// ALS5041
				skipAttributes.put("A_ContractApprovedDate", NbaUtils.getStringFromDate(appInfo.getHOCompletionDate()));// ALS5127
				// QC11621-APSL3588 START
				skipAttributes.put("A_UnpaidReissue", nbaTXLife.unpaidReissue() ? "yes" : "no");
				// If print date is before to contract change date set new print ind as false else true
				boolean ind = false;
				// BEGIN: APSL5349
				NbaDst CntChgWI = null;
				Date contractChangeDate = null;
				if (nbaTXLife.unpaidReissue()) {
					CntChgWI = searchWI(NbaConstants.A_WT_CONTRACT_CHANGE);
					if (!NbaUtils.isBlankOrNull(CntChgWI) && CntChgWI.getNbaSources().isEmpty()) {
						contractChangeDate = NbaUtils.getDateFromStringInAWDFormat(CntChgWI.getNbaLob().getCreateDate());
						skipAttributes.put("A_ContractChangeDate", NbaUtils.getStringFromDate(contractChangeDate));

					}
					ContractChangeInfo latestContractChange = NbaUtils.getLatestValidContractChangeInfo(getNbaTXLife());
					if (!NbaUtils.isBlankOrNull(latestContractChange)) {
						List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
						List<Activity> reissueActivityList = NbaUtils.getActivityByTypeCodeAndRelatedObjId(activityList,
								NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE, latestContractChange.getId());
						Activity activity = NbaUtils.getActivityByStatus(reissueActivityList, NbaOliConstants.OLIEXT_LU_ACTSTAT_INITIATED);

						if (!NbaUtils.isBlankOrNull(activity)) {
							contractChangeDate = activity.getStartTime().getTime();
							skipAttributes.put("A_ContractChangeDate", NbaUtils.getStringFromDate(contractChangeDate));
						}

					}
					if (!NbaUtils.isBlankOrNull(latestContractChange) || !NbaUtils.isBlankOrNull(CntChgWI)) {
						if (appInfoExtn.hasContractPrintExtractDate()) {
							int result = appInfoExtn.getContractPrintExtractDate().compareTo(contractChangeDate); //NBLXA-1514 changed from compare to compareTo
							if(result >= 0 
									|| (appInfoExtn.hasContractReprintDate() && (appInfoExtn.getContractReprintDate().compareTo(contractChangeDate) > 0))) { // QC#
								// 15141 (APSL4332) //NBLXA-1514 changed from compare to compareTo
								ind = true;
								if (appInfoExtn.hasContractReprintDate()) { // QC# 15141 (APSL4332)
									skipAttributes.put("A_ContractPrintDate", NbaUtils.getStringFromDate(appInfoExtn.getContractReprintDate())); // QC#
									// 15141
									// (APSL4332)
								}
							}
							skipAttributes.put("A_NewPrintInd", ind ? "yes" : "no");
							if (ind == false)
								skipAttributes.put("A_PrintExtractGenerated", "no");// if print is not a new one set printextracted as false
						}
					}
					// END: APSL5349
				}
				// QC11621-APSL3588 END
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(skipAttributes); //Get the PlacementEndDate(Closure Date) and ClosureType
				//End AXAL3.7.57
				if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
					String closureDate = (String) nbaVpmsResultsData.getResultsData().get(0);
					String closureType = (String) nbaVpmsResultsData.getResultsData().get(1);
					try {
						Date placementEndDate = get_YYYY_MM_DD_sdf().parse(closureDate);
						// Begin ALS4506/QC3504
						if(printExtractGenerated.equalsIgnoreCase("Yes") && appInfoExtn.getAddlPlacementDays() > 0){
							GregorianCalendar calendar = new GregorianCalendar();
							calendar.setTime(appInfoExtn.getContractPrintExtractDate());//ALS5853
							calendar.add(Calendar.DAY_OF_WEEK, appInfoExtn.getAddlPlacementDays());
							placementEndDate = calendar.getTime();
							appInfoExtn.setAddlPlacementDays(0);
							appInfoExtn.setClosureOverrideInd(1);//Set the override indicator
						}
						// End ALS4506/QC3504
						// BEGIN NBLXA-2155[NBLXA-2196]
						if (!nbaTXLife.isUnderwriterApproved()) { // NBLXA-2452 Begin
							RequirementInfo reqInfo = null;
							Date appSubmitDate = getApplicationInfo().getSubmissionDate();
							if (NbaUtils.isTermCase(getPolicy())) {
								reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_PREMIUMQUOTE);
							} else {
								reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_SIGNILLUS);
							}
							if (appInfo.getApplicationJurisdiction() == NbaOliConstants.OLI_USA_NY && !NbaUtils.isBlankOrNull(reqInfo)
									&& reqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_RECEIVED
									&& NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) { // NBLXA-2155[NBLXA-2300]
								placementEndDate = NbaUtils.addDaysToDate(appSubmitDate, 10);
							}
							// END NBLXA-2155[NBLXA-2196]
							// Start NBLXA-2155[NBLXA-2202]
							if (appInfo.getApplicationJurisdiction() == OLI_USA_TX) {
								long planType = nbaTXLife.getPolicy().getProductType();
								RequirementInfo signedIllustration = reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_SIGNILLUS);
								if (planType == OLI_PRODTYPE_VUL) {
									if (!NbaUtils.isBlankOrNull(signedIllustration) && signedIllustration.getReqStatus() != OLI_REQSTAT_RECEIVED
											&& NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) { // NBLXA-2155[NBLXA-2300]
										placementEndDate = NbaUtils.addDaysToDate(getApplicationInfo().getSignedDate(), 90);
									}
								} else if (AxaUtils.isPermProduct(planType)) { // NBLXA-2155[NBLXA-2258]
									if (!NbaUtils.isProductCodeCOIL(getNbaTXLife())) {
										RequirementInfo illustrationCertificate = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_1009800011);
										if (((!NbaUtils.isBlankOrNull(illustrationCertificate)
												&& illustrationCertificate.getReqStatus() != OLI_REQSTAT_RECEIVED)
												|| (!NbaUtils.isBlankOrNull(signedIllustration)
														&& signedIllustration.getReqStatus() != OLI_REQSTAT_RECEIVED))
												&& NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) { // NBLXA-2155[NBLXA-2300]
											placementEndDate = NbaUtils.addDaysToDate(getApplicationInfo().getSignedDate(), 90);
										}
									}
								}
							}
						} // NBLXA-2452 End
							// End NBLXA-2155[NBLXA-2202]

						appInfoExtn.setClosureType(closureType);
						appInfo.setPlacementEndDate(placementEndDate);
						appInfoExtn.setActionUpdate();
						appInfo.setActionUpdate();
						updateAutoClosureTable(placementEndDate, closureType, appInfoExtn.getClosureInd());
					} catch (ParseException e) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Unknown date format returned from VP/MS model: ",
								getNbaConfigValProc().getModel(), ", Date: ", closureDate), getIdOf(getApplicationInfo()));
					} catch (NbaBaseException e) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("PolicyNumber: ", getPolicy().getPolNumber()), getIdOf(getApplicationInfo()));	
						e.printStackTrace();
					}
				} else {
					addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), ", Model: ", getNbaConfigValProc()
							.getModel()), getIdOf(getPolicy()));
				}
			}
		}	
	}


	/**
	 * @return
	 */
	//ALS5041 New Method
	private String getReceiptDate(){
		return NbaUtils.getStringFromDate(NbaUtils.getDateFromStringInAWDFormat(getNbaDst().getNbaLob().getCreateDate()));
	}

	/**
	 * @return
	 * @throws NbaBaseException
	 */
	//AXAL3.7.57 New Method
	private NbaTransaction retrieveContractPrintExtract() throws NbaBaseException {
		NbaDst parentCase = retrieveCaseAndTransactions(getNbaDst(), getUserVO(), false);
		List tempList = parentCase.getNbaTransactions();
		NbaTransaction printTrans = null;
		Iterator itr = tempList.iterator();
		while (itr.hasNext()) {
			NbaTransaction tempTrans = (NbaTransaction) itr.next();
			if (NbaConstants.A_WT_CONT_PRINT_EXTRACT.equals(tempTrans.getTransaction().getWorkType())) {
				printTrans = tempTrans;
				break;
			}
		}
		return printTrans;
	}

	/**
	 * Retrieve the Case and Transactions associated
	 * @param  dst workItem / case, for which the sibiling transactions / child transactions need to be retrieved along with the case
	 * @param  user AWD user id  
	 * @param  locked lock indicator
	 * @return NbaDst containing the case and all the transactions
	 * @throws NbaBaseException
	 */
	//AXAL3.7.57 New Method
	protected NbaDst retrieveCaseAndTransactions(NbaDst dst, NbaUserVO user, boolean locked){
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (dst.isCase()) {
			retOpt.setWorkItem(dst.getID(), true);
			retOpt.requestTransactionAsChild();
			if (locked) {
				retOpt.setLockWorkItem();
				retOpt.setLockTransaction();
			}
		} else { // if a transaction
			retOpt.setWorkItem(dst.getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestTransactionAsSibling();
			if (locked) {
				retOpt.setLockWorkItem();
				retOpt.setLockParentCase();
				retOpt.setLockSiblingTransaction();
			}
		}
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		return (NbaDst) workResult.getFirst();
	}

	/**
	 * Retrieve just the Parent Case
	 * @param  dst workItem for which Parent case need to be retrieved.
	 * @param  user AWD user id  
	 * @param  locked lock indicator
	 * @return NbaDst containing the Parent case
	 * @throws NbaBaseException
	 */
	//ALII2041 New Method
	protected NbaDst retrieveParentCase(NbaDst dst, NbaUserVO user, boolean locked){
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(dst.getID(), false);
		retOpt.requestCaseAsParent();
		if (locked) {
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
		}
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		return (NbaDst) workResult.getFirst();
	}


	//NBA254 New Method
	private void updateAutoClosureTable(Date closureDate, String closureType, int closureInd) throws NbaBaseException{
		NbaAutoClosureContract closureContract = null;
		closureContract = NbaAutoClosureAccessor.getAutoClosureCase(getPolicy().getPolNumber());//look for contract data in the Auto Closure table
		if(closureContract != null){ //Contract data found in the table, so update the values
			closureContract.setClosureDate(closureDate);
			closureContract.setClosureTypeString(closureType);
			NbaAutoClosureAccessor.updateClosureDateAndType(closureContract);
		}else{//Contract data not found in table, insert a new Row for the contract
			closureContract = new NbaAutoClosureContract();
			closureContract.setClosureDate(closureDate);
			closureContract.setClosureTypeString(closureType);
			closureContract.setContractNumber(getPolicy().getPolNumber());
			closureContract.setSystemId(getSystemId());
			closureContract.setCompanyCode(getNbaDst().getNbaLob().getCompany());
			closureContract.setClosureInd(closureInd);
			closureContract.setWorkItemId(getNbaDst().getID());
			NbaAutoClosureAccessor.save(closureContract);
		}

	}
	/**
	 * Returns the life.
	 * @return Life
	 */
	//AXAL3.7.40 new method
	protected Life getLife(Policy policy) {
		Life life = null;
		if (policy != null) {		
			if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) {
				LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladhpc = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
				if (ladhpc.isLife()) {
					life = ladhpc.getLife();
				}
			}
		}
		return life;
	}
	/**
	 * @param stateId
	 * @return
	 */
	//AXAL3.7.40 new method
	protected boolean isJurisdiction(long stateId) {
		return getApplicationInfo() != null ? getApplicationInfo().getApplicationJurisdiction() == stateId : false;
	}

	/**
	 * Set CovOptionStatus as OLI_POLSTAT_DECISSUE for all covOptions 
	 * @param covOptions list of all covOptions
	 */
	//NBA223 New Method
	protected void denyCovOptions(List covOptions) {
		CovOption covOpt = null;		
		for (Iterator itr = covOptions.iterator(); itr.hasNext();) {
			covOpt = (CovOption) itr.next();
			covOpt.setCovOptionStatus(OLI_POLSTAT_DECISSUE);
			covOpt.setActionUpdate();
		}
	}	

	/**
	 * Update the YRT Discount for a Policy.
	 * @param Policy
	 */
	//ALPC119 new method
	protected void updateYRTDiscount(Policy policy)throws NbaBaseException {
		CalculationResult calculationResult=  getDocsResultForID(policy.getId());
		if (calculationResult != null) {
			updatePolicyExtensionInExtract(policyExtension, calculationResult);
		}
	}

	//NBA223 New Method ALS4208 Refactored
	protected boolean canDenyCoverage(Coverage coverage) {
		boolean canDenyCoverage = false;
		for (int i = 0; i < coverage.getLifeParticipantCount(); i++) {
			LifeParticipant lifeParticipant = getCoverage().getLifeParticipantAt(i);
			if (!NbaUtils.isPrimaryInsuredParticipant(lifeParticipant)) {
				LifeParticipantExtension lifeParticipantExtn = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
				//begin ALS4522
				if (lifeParticipantExtn != null && lifeParticipantExtn.getParticipantStatus() == OLI_CLISTAT_DECISSUE) {
					canDenyCoverage = true;
				} else {
					canDenyCoverage = false;
					break;
				}
			}//end ALS4522
		}
		return canDenyCoverage;
	}

	//NBA223 New Method
	protected void denyCoverage(Coverage coverage) {
		coverage.setLifeCovStatus(OLI_POLSTAT_DECISSUE);
		denyCovOptions(coverage.getCovOption());
		coverage.setAction(null);//SPR3098
		coverage.setActionUpdate();
	}

	//NBA223 New Method
	protected void denyRider(Rider rider) {
		rider.setRiderStatus(OLI_POLSTAT_DECISSUE);
		denyCovOptions(rider.getCovOption());
		rider.setActionUpdate();
	}	

	/**
	 * Retrieve the CalculationResult for the object identified by the id. Return
	 * null if there is no matching CalculationResult.
	 * @param id - the id of the object, including a duration value if applicable
	 * @return CalculationResult
	 * @throws NbaBaseException
	 */
	protected CalculationResult getDocsResultForID(String id) throws NbaBaseException {
		CalculationResult calculationResult = null;
		int resultCount = getDocsCalculation().getCalculationResultCount();
		int resIdx;
		for (resIdx = 0; resIdx < resultCount; resIdx++) {
			calculationResult = (CalculationResult) getDocsCalculation().getCalculationResult().get(resIdx);
			if (id.equals(calculationResult.getObjectId())) {
				return calculationResult;
			}
		}
		return null;
	}


	/**
	 * Retrieve the NbaCalculation from the applicable Document calculation model.
	 * @return NbaCalculation
	 */
	//ALS4450,ALS4451,ALS4452,ALS4453 code refactored
	public NbaCalculation getDocsCalculation() throws NbaBaseException {
		if(docsCalculation == null){
			docsCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_AXA_CONTRACT_PRINT, getNbaTXLife());
			if (docsCalculation.getCalcResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
				throw new NbaVpmsException("Contract Documents VPMS calculation failure");
			}
		}
		return docsCalculation;
	}


	/**
	 * This method will read the result from CalculationResult coming from VPMS Calulation Model and will populate PolicyExtension
	 * @param policyExtension
	 * @param calculationResult
	 */
	private void updatePolicyExtensionInExtract(PolicyExtension policyExtension, CalculationResult calculationResult) {
		String field = null;

		Iterator calculationResultIterator = calculationResult.getCalcProduct().iterator();
		while (calculationResultIterator.hasNext()) {
			CalcProduct calcProduct = (CalcProduct) calculationResultIterator.next();
			field = calcProduct.getType();
			if (field.equalsIgnoreCase("BaseDiscount")) {
				policyExtension.setFirstYrPremDiscountAmt(new Double(calcProduct.getValue()).doubleValue());
			} 
		}
		policyExtension.setActionUpdate();
	}

	/**
	 * The method call the Mode Premium calculator to get the value for <PolicyExtension.BestClassModalPremAmt>using the best rate class.
	 *  
	 */
	//ALPC171 new method added. ALS5046
	public void calculateBestClassModalPremiumAmount() {
		if (getNbaModePremiumCalculation() == null) {
			return;
		}
		Policy policy = getNbaTXLife().getPolicy();// ALS5046
		String policyID = policy.getId();//ALS5046
		outer: for (int i = 0; i < getNbaModePremiumCalculation().getCalculationResultCount(); i++) {
			CalculationResult aresult = getNbaModePremiumCalculation().getCalculationResultAt(i);
			if (policyID != null && policyID.equals(aresult.getObjectId())) {//ALS5046
				if (aresult.hasCalcError()) {
					addCalcErrorMessage(aresult.getCalcError().getMessage(), policyID);
					return;
				}
				for (int j = 0; j < aresult.getCalcProductCount(); j++) {
					CalcProduct aprod = aresult.getCalcProductAt(j);
					if (aprod.getType().equalsIgnoreCase("BestClassPremAmt")) {//ALS5046
						getPolicyExtension().setBestClassModalPremAmt(Double.parseDouble(aprod.getValue()));
						getPolicyExtension().setActionUpdate();
						break outer;
					}
				}
			}
		}
	}

	/**
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaDataAccessException
	 */
	protected NbaPlansData getPlanData() throws NbaBaseException, NbaDataAccessException {
		HashMap map = getNbaTableAccessor().createDefaultHashMap(NbaTableAccessConstants.WILDCARD);
		map.put(NbaTableAccessConstants.C_COMPANY_CODE, getNbaDst().getNbaLob().getCompany());
		map.put(NbaTableAccessConstants.C_COVERAGE_KEY, getPolicy().getProductCode());
		NbaPlansData planData = getNbaTableAccessor().getPlanData(map);
		return planData;
	}
	/**
	 * @param relation
	 * @return
	 */
	//ALPC066 New Method Added 
	protected String getPartyName(NbaParty nbaParty) {
		String partyFullName = "";
		if (nbaParty != null) {
			if (nbaParty.isPerson()) {
				Person person = nbaParty.getPerson();
				if (person.hasFirstName()) {
					partyFullName += person.getFirstName().trim();
				}
				if (person.hasMiddleName()) {
					partyFullName += person.getMiddleName().trim();
				}
				if (person.hasLastName()) {
					partyFullName += person.getLastName().trim();
				}
				if (person.hasSuffix()) {
					partyFullName += person.getSuffix().trim();
				}

			} else if (nbaParty.getFullName() != null) {
				partyFullName += nbaParty.getFullName().trim();
			}
		}
		return partyFullName;
	}
	/**
	 * @param covOptions
	 * @return
	 */
	//ALPC066 New Method Added
	protected boolean checkNotInheritedCovOption(Coverage coverage) {
		boolean found = false;
		List covOptionList = coverage.getCovOption();
		if (covOptionList != null) {
			for (int j = 0; j < covOptionList.size(); j++) {
				CovOption covOption = coverage.getCovOptionAt(j);
				if (covOption.getCovOptionStatus() == OLI_POLSTAT_DECISSUE || NbaUtils.isDeleted(covOption))
					continue;
				CovOptionProduct covOptionProduct = getCovOptionProductFor(coverage, covOption);
				if (covOptionProduct != null) {
					CovOptionProductExtension cvpExt = getCovOptionProductExtensionFor(covOptionProduct);
					if (cvpExt != null && cvpExt.getSelectionRule() != OLI_RIDERSEL_INHERENTNOPREM) {
						found = true;
						break;
					}
				}
			}
		}
		return found;
	}
	/**
	 * @return
	 */
	//ALPC139 New method added.
	protected boolean checkForPlan150Or151X() {
		NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation();
		boolean isPlan150_OR_151X = false;
		if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
			isPlan150_OR_151X = new Integer((String) nbaVpmsResultsData.getResultsData().get(0)).intValue() == 1 ? true : false;
		}
		return isPlan150_OR_151X;
	}

	/**
	 * 
	 * @return
	 */	
	//ALPC075 New Method
	protected Map deOinkEndorsementValues() {
		NbaTXLife txLife = getNbaTXLife();
		Map deOinkMap = new HashMap();
		List endorsementList = txLife.getPolicy().getEndorsement();
		int noOfEndoresements = endorsementList.size();
		List endorsementCodeList = new ArrayList();
		EndorsementExtension endorsementExtension = null;
		for (int i = 0; i < noOfEndoresements; i++) {
			Endorsement endorsement = (Endorsement) endorsementList.get(i);
			if (NbaUtils.isDeleted(endorsement)) {
				continue;
			}

			endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
			if (endorsementExtension != null) {
				endorsementCodeList.add(endorsementExtension.getEndorsementCodeContent());
			} else {
				endorsementCodeList.add("");
			}
		}
		deOinkMap.put("A_EndorsementCodeList", endorsementCodeList.toArray(new String[endorsementCodeList.size()]));
		deOinkMap.put("A_No_of_Endorsementcode", Integer.toString(endorsementCodeList.size()));
		return deOinkMap;
	}

	/**
	 * Return array of keys for query
	 * @param nbaContractVO
	 * @return query object array
	 */
	//AXAL3.7.04 New Method
	protected Object[] getArgs(NbaContractVO nbaContractVO) {
		Object args[] = new Object[4];
		args[0] = nbaContractVO.getParentIdKey();
		args[1] = nbaContractVO.getContractKey();
		args[2] = nbaContractVO.getCompanyKey();
		args[3] = nbaContractVO.getBackendKey();
		return args;
	}

	//Code refractor for ALS3166
	protected void setPendingContractStatus() {
		//ALS4707 Code Deleted
		String char1 = "0";
		String char2 = "0";
		String char3 = "0";
		String char4 = "0";
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null && !applicationInfo.isActionDelete()) {
			//Beging NBA254

			if (applicationInfo.getPlacementEndDate() != null && new Date().after(applicationInfo.getPlacementEndDate())
					&& applicationInfo.getApplicationType() == OLI_APPTYPE_TRIAL) {
				char1 = "9";
			}
			//End NBA254
			ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (applicationInfoExtension != null && !applicationInfoExtension.isActionDelete()) {
				if (applicationInfoExtension.hasUnderwritingApproval()) {
					String undwrtApproval = translateToBESValue(String.valueOf(applicationInfoExtension.getUnderwritingApproval()),
							NbaTableConstants.OLIEXT_LU_UNDAPPROVAL);
					if (null != undwrtApproval) {
						char3 = undwrtApproval;
					}
				}
				boolean isAutoClose = false; //APSL4802
				//Beging NBA254
				if (applicationInfo.getPlacementEndDate() != null && new Date().after(applicationInfo.getPlacementEndDate())&& applicationInfoExtension.getClosureInd()==NbaConstants.NEGATIVE_DISP_DONE) {
					if ( getNbaTXLife().isNewApplication() && applicationInfoExtension.getUnderwritingApproval() == OLIX_UNDAPPROVAL_UNDERWRITER) {// P2AXAL040//ALII522 //ALII1206 //ALII1406 (Overwrite in 2.2 Retrofit)//ALII1845 
						isAutoClose = true; //APSL4802
						char1 = "8";
					}
				}
				//End NBA254
				if (applicationInfoExtension.hasUnderwritingStatus()
						&& OLIX_UNDAPPROVAL_UNDERWRITER != applicationInfoExtension.getUnderwritingApproval()) { //AXAL3.7.06
					//AXAL3.7.06 code deleted
					char4 = translateUnderwritingStatus(applicationInfoExtension.getUnderwritingStatus()); //AXAL3.7.06
				}
				// begin AXAL3.7.06
				if (OLI_POLSTAT_ISSUED != getPolicy().getPolicyStatus()) {
					if (OLIX_UNDAPPROVAL_CONDITIONAL == applicationInfoExtension.getUnderwritingApproval()) {
						char2 = "1";
						char3 = "0";
						char4 = "0";
					} else if (OLIX_UNDAPPROVAL_UNDERWRITER == applicationInfoExtension.getUnderwritingApproval()) {
						char3 = "1";
						char4 = "0";
					}

				}
				// end AXAL3.7.06
				if ((OLI_POLSTAT_ISSUED == getPolicy().getPolicyStatus())
						&& (OLIX_UNDAPPROVAL_UNDERWRITER == applicationInfoExtension.getUnderwritingApproval())) {
					char3 = "0";
					char4 = "1";
				}
				// APSL4802
				if (!isAutoClose
						&& (OLIX_UNDAPPROVAL_CONDITIONAL == applicationInfoExtension.getUnderwritingApproval() || OLIX_UNDAPPROVAL_UNDERWRITER == applicationInfoExtension
						.getUnderwritingApproval()) && NbaUtils.isNegativeDisposition(applicationInfoExtension.getUnderwritingStatus())) {
					char1 = "0";
					char2 = "0";
					char3 = "0";
					char4 = translateUnderwritingStatus(applicationInfoExtension.getUnderwritingStatus());
				}
				// APSL4802

				//NBLXA-1433 code Deleted
				//NBLXA-1632 BEGIN
				if (NbaUtils.isProductCodeCOIL(getNbaTXLife()) && !NbaUtils.isBlankOrNull(NbaUtils.getFirstPolicyExtension(getPolicy()))
						&& NbaUtils.getFirstPolicyExtension(getPolicy()).getCoilCsgreviewInd() == NbaOliConstants.NBA_CSGREVIEW_NIGO && applicationInfoExtension.getReopenDate() == null) {
					char1 = "0";
					char2 = "0";
					char3 = "1";
					char4 = "6";
					getPolicy().setPolicyStatus(OLI_POLSTAT_WITHDRAW);
					getPolicy().setActionUpdate();
				}
				//NBLXA-1632 END
			}
		}
		getPolicyExtension().setPendingContractStatus(concat(char1, char2, char3, char4));
		getPolicyExtension().setActionUpdate();
	}

	/**
	 * This method will translate underwriting status into 1 character values. 27 -> 2 //Decline 23 -> 3 //Incomplete 29 -> 4 //Postponed 39 -> 5
	 * //Cancelled 1009800001 -> 6 //Withdraw 7 -> 7 //NTO
	 * 
	 * @param int
	 *                underwritingStatus
	 * @return true if the underwriting status is a negative disposition; false otherwise.
	 */
	// AXAL3.7.06 new method.
	protected String translateUnderwritingStatus(long underwritingStatus) {
		String statusChar = "0";
		if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_DECLINED) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_DECLINE;
		} else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_INCOMPLETE) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_INCOMPLETE;
		} else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_POSTPONED) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_POSTPONED;
		} else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_CANCELLED) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_CANCELLED;
		} else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_WITHDRAW || underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_REG60NIGO) { //APSL3123
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_WITHDRAW;
		} else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_NTO || underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_REG60_PRESALE_EXPIRED) {//APSL5125
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_NTO;
		}
		return statusChar;
	}
	/**
	 * @param bankHolding The bankholding to set.
	 */
	//ALS3600 New Method
	public void setBankholding(Holding bankHolding) {
		this.bankHolding = bankHolding;
	}
	/**
	 * Returns the description for the given role from NBA_ROLES
	 * @param role
	 * @return
	 */
	//QC2614 New Method
	protected String getTranslatedRole(String role) {
		try {
			return getNbaTableAccessor().getRolesTranslationData(getTblKeys(), "", role).text();
		} catch (Exception ex) {
			return "";
		}
	}

	//A2_AXAL006 new method
	//Returns the Role string for the current party.
	protected String getTranslatedRole(Party currParty) {
		Relation relation = getNbaTXLife().getRelationByRelatedId(currParty.getId());
		String orgObjID = getNbaTXLife().getPrimaryHolding() != null ? getNbaTXLife().getPrimaryHolding().getId() : null; //APSL3351

		//Begin ALNA104
		//begin P2AXAL068
		if (isOwner(currParty.getId()) || isPayor(currParty.getId(),orgObjID)){ //APSL3351
			if(isPrimaryInsured(currParty.getId())) {  
				return getTranslatedRole(Long.toString(OLI_REL_INSURED));
			} else if (isJointInsured(currParty.getId())) { 
				return getTranslatedRole(Long.toString(OLI_REL_JOINTINSURED));
			} else if (isOwner(currParty.getId())){ //APSL3086
				return getTranslatedRole(Long.toString(OLI_REL_OWNER));//APSL3086
			}
			//end P2AXAL068 
		}
		//END ALNA104
		return getTranslatedRole(Long.toString(relation.getRelationRoleCode()));
	}

	//P2AXAL054 new method
	protected String getTranslatedRole(LifeParticipant lifePart) {
		try {
			return getNbaTableAccessor().getRolesTranslationData(getTblKeys(), NbaTableAccessConstants.XML_PARTICIPANT_RELATION_TYPE,
					String.valueOf(lifePart.getLifeParticipantRoleCode())).text();
		} catch (Exception ex) {
			return "";
		}
	}

	//AXAL3.7.40
	protected boolean hasRidersOrBenefits(Life life) {
		boolean isError = false;
		List coverageList = life.getCoverage();
		if (coverageList != null) {
			for (int i = 0; i < coverageList.size(); i++) {
				Coverage coverage = (Coverage) coverageList.get(i);
				if (coverage.getIndicatorCode() != OLI_COVIND_BASE && !NbaUtils.isDeleted(coverage)
						&& coverage.getLifeCovStatus() != OLI_POLSTAT_DECISSUE) {
					isError = true;
					break;
				}
				if (coverage.getCovOption() != null && checkNotInheritedCovOption(coverage)) {
					isError = true;
					break;
				}
			}
		}
		return isError;
	}
	/*
	 * if unbound..and reprint is false
	 * set reprint ind to true when new pol eff date not equal current eff date
	 */
	//ALPC234 new method
	protected void determineRePrint(Date origPolicyEffDate,Date effDate) {
		if (getPolicyExtension().getContractChangeReprintInd() || null == origPolicyEffDate) {
			return;
		}

		if (!getPolicyExtension().getUnboundInd()) {
			return;
		}
		//Begin ALS4705
		if (isOriginalPrint()) {
			return;
		}
		if (!getApplicationInfoExtension().hasContractPrintExtractDate()) {
			return;
		}
		//End ALS4705
		if (!(origPolicyEffDate.compareTo(effDate) == 0)) {
			getPolicyExtension().setContractChangeReprintInd(true);
			getPolicyExtension().setActionUpdate();
		}
	}
	/*
	 * If running contract print and this is origianl print, return true
	 */
	//ALS4705 New Method
	protected boolean isOriginalPrint() {
		if (PROC_CONTRACT_PRINT.equalsIgnoreCase(getNbaTXLife().getBusinessProcess()) ||
				PROC_VALIDATE_PRINT.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())	) {
			String extComp2 = getNbaDst().getNbaLob().getPrintExtract(); //APSL5055	
			return (extComp2 == null || extComp2.trim().length() <= 0); 
		}
		return false;
	}

	//ALS5248 New Method
	//Returns true if answer to Q 43G is Yes.
	protected boolean isReplacementParty(String partyId) {
		Relation relation = getNbaTXLife().getRelationForRoleAndRelatedId(NbaOliConstants.OLI_REL_HOLDINGCO, partyId);
		if (relation != null && relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING) {
			Holding holding = getNbaTXLife().getHolding(relation.getOriginatingObjectID());
			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(holding.getPolicy());
			if (policyExtn != null && NbaUtils.isAnsweredYes(policyExtn.getReplacementIndCode())) {
				return true;
			}
		}
		return false;
	}
	//ALS5348 New Method
	//Returns true if for given Party ID, there exists a Holding Company relation
	protected boolean hasHoldingCompanyRelation(String partyId) {
		Relation relation = getNbaTXLife().getRelationForRoleAndRelatedId(NbaOliConstants.OLI_REL_HOLDINGCO, partyId);
		if (relation != null && relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING) {
			return true;
		}
		return false;
	}

	////ALS5708 new Method
	protected boolean ClosureCalculationRequired(String printGenerated, long closureOverrideInd, long applicationType, long underwritingApproval){
		boolean calcRequired = false;
		if(closureOverrideInd == FALSE){	
			if(NbaUtils.isNewApplication(applicationType) || applicationType == OLI_APPTYPE_TRIAL ){//ALS5872 refactored,  P2AXAL040/ALII522 ,APSL624, APSL3232
				calcRequired= true;
			}
			//QC11621-APSL3588 Need to recalculate closure date for reissue Cases
			if(!(getNbaTXLife().unpaidReissue())&&getNbaTXLife().isReissue() && !(underwritingApproval == OLIX_UNDAPPROVAL_UNDERWRITER && printGenerated.equals("Yes"))){//ALII1206 Clac Required only if the reissue case is approved and print is complete 
				calcRequired = false; //ALII1206
			}

		}
		return calcRequired;
	}	
	/**
	 * Returns the formResponse.
	 * @return FormResponse
	 */
	//P2AXAL004 New Method
	protected FormResponse getFormResponse() {
		return formResponse;
	}

	/**
	 * Sets the formResponse.
	 * @param formResponse The FormResponse to set
	 */
	//P2AXAL004 New Method
	protected void setFormResponse(FormResponse formResponse) {
		this.formResponse = formResponse;
	}

	/**
	 * Determine if the FormResponse exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//P2AXAL004 New Method
	protected boolean findNextFormResponse(int occurrence) {
		if (getFormInstance() != null) {
			if (getFormInstance().getFormResponseCount() > occurrence) {
				setFormResponse(getFormInstance().getFormResponseAt(occurrence));
				logDebug(getFormResponse().getId() + " found");
				return true;
			}
		}
		setFormResponse(null);
		return false;
	}
	/**
	 * Returns true if it is IULForm
	 */
	// P2AXAL027 New Method
	protected boolean isIULForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_INDEXEDUL.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Get SubAccount object for the passed id.
	 * @param id
	 * @return
	 */
	//P2AXAL027
	public SubAccount findSubAccountById(String id) {
		SubAccount subAccount = null;
		Investment investment = getInvestment();
		if (investment != null) {
			List subAccounts = investment.getSubAccount();
			for (int i = 0; i < subAccounts.size(); i++) {
				subAccount = investment.getSubAccountAt(i);
				if (!NbaUtils.isDeleted(subAccount) && id != null && id.equalsIgnoreCase(subAccount.getId())) {
					return subAccount;
				}
			}
		}
		return null;
	}

	//P2AXAL027 New Method
	protected boolean isPartyInRole(String partyId, long role) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == role) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Returns LTCSupp form
	 */
	//	P2AXAL027 New Method
	protected boolean isLTCForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_LTCSUPP.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns DisabilityHealth
	 */
	//	P2AXAL027 New Method
	public DisabilityHealth getDisabilityHealth(Policy policy) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladhpc = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		DisabilityHealth disabilityHealth = ladhpc.getDisabilityHealth();
		if (disabilityHealth != null && !NbaUtils.isDeleted(disabilityHealth)) {
			return disabilityHealth;
		}
		return null;
	}
	/**
	 * Returns true if it is Term Conv Form
	 */
	// P2AXAL027 New Method
	protected boolean isTermConvSuppForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_TERMCONVSUPP.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}

	//	 P2AXAL019 Method removed from Nbavalcwa.java and placed here	
	protected void assignPremBalDue(double diffAmt, double over, boolean shortage) {
		if (diffAmt == 0 ) {				
			getApplicationInfo().setPremBalDue(0);			
		} else if (diffAmt < 0 && shortage) {
			if (Math.abs(diffAmt) > over) {
				getApplicationInfo().setPremBalDue(Math.abs(diffAmt));
			} else {
				getApplicationInfo().setPremBalDue(0);
			}				
		} else if (diffAmt > 0 && !shortage) {
			if(diffAmt > over) {
				getApplicationInfo().setPremBalDue(-diffAmt);	
			} else {
				getApplicationInfo().setPremBalDue(0);	
			}			
		}
		getApplicationInfo().setActionUpdate();
	}

	/**
	 * Concatenate a three pairs of String/int fields
	 * @param a - String
	 * @param b - int
	 * @param c - String
	 * @param d - int
	 * @param e - String
	 * @param f - int
	 * @return the combined Strings
	 */
	//P2AXAL016 New Method
	protected String concat(String a, double b, String c, double d, String e, double f) {
		StringBuffer buf = new StringBuffer();
		if(!Double.isNaN(b)){		
			buf.append(a);
			buf.append(b);
		}
		if(!Double.isNaN(d)){		
			buf.append(c);
			buf.append(d);
		}
		if(!Double.isNaN(f)){		
			buf.append(e);
			buf.append(f);
		}
		return buf.toString();
	}
	/**
	 * Determine by examining the Relation objects.
	 * @return true if any Power Of Attorney is present on case
	 */
	//P2AXAL016 New Method
	protected boolean isPOACase() {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete() && !relation.isActionDeleteSuccessful()) {
				if ( NbaUtils.isPOARelation(relation)) return true;
			}
		}
		return false;
	}	


	/**
	 * Get the ConflictObjectInfoExtension for an ConflictObjectInfo.
	 * Return a null if the parameter is null or the extension can't be found.
	 * @param ConflictObjectInfo
	 * @return ConflictObjectInfoExtension
	 */
	//P2AXAL016 New Method
	public static ConflictObjectInfoExtension getConflictObjectInfoExtension(com.csc.fs.dataobject.accel.product.ConflictObjectInfo conflictObjectInfo) {
		if (conflictObjectInfo == null) {
			return null;
		}
		int extCount = conflictObjectInfo.getOLifEExtensionCount();
		com.csc.fs.dataobject.accel.product.OLifEExtension extension;
		for (int index = 0; index < extCount; index++) {
			extension = conflictObjectInfo.getOLifEExtensionAt(index);
			if (extension != null) {
				if ((NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode())) && (extension.isConflictObjectInfoExtension())) {
					return extension.getConflictObjectInfoExtension();
				}
			}
		}
		return null; // none could be found
	}

	/**
	 * Get the AgeAmtProduct for the coverage
	 * @param Coverage
	 * @param CovOption
	 * @return AgeAmtProduct
	 */	
	//P2AXAl016 New method
	protected AgeAmtProduct getAgeAmtProductFor(Coverage coverage, CovOption covOption) {
		UnderwritingClassProduct temp = getUnderwritingClassProduct(coverage, covOption);
		long distributionChannel = 0;		
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtn != null) {
			distributionChannel = policyExtn.getDistributionChannel();
		}
		//P2AXAL055 Code deleted
		AgeAmtProduct ageAmtProd = getUnderwritingClassAgeAmtProduct(temp, getLifeParticipantForLookup(coverage), distributionChannel);//P2AXAL055
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("getAgeAmtProductFor found: " + (ageAmtProd == null ? "NULL" : ageAmtProd.getId()));
		}
		return ageAmtProd;		
	}

	/**
	 * Return the BenefitLimitOption for a CovOptionProduct.
	 * @param Coverage 
	 * @param CovOption
	 * @return the BenefitLimitOption
	 */
	//P2AXAL016 New Method
	public com.csc.fs.dataobject.accel.product.BenefitLimitOption getBenefitLimitOptByPCT(Coverage coverage, CovOption covOption) {
		CovOptionProduct covOptProduct = getCovOptionProductFor(coverage, covOption);
		com.csc.fs.dataobject.accel.product.BenefitLimitOption benefitLimitOpt = null;
		com.csc.fs.dataobject.accel.product.BenefitLimitOptionExtension benefitLimitOptExt = null;
		if (covOptProduct != null) {
			CovOptionProductExtension cvpExt = getCovOptionProductExtensionFor(covOptProduct);
			if (cvpExt != null && cvpExt.hasDisabilityHealthProvisions()) {
				DisabilityHealthProvisions dhp = cvpExt.getDisabilityHealthProvisions();
				if (dhp != null) {
					int count = dhp.getBenefitLimitOptionCount();
					for (int i = 0; i < count; i++) {
						benefitLimitOpt = dhp.getBenefitLimitOptionAt(i);
						if (benefitLimitOpt != null && benefitLimitOpt.getBenefitLimitType() == OLI_BENEFITLIMIT_PERCENT) {
							benefitLimitOptExt = getBenefitLimitOptionExtensionFor(benefitLimitOpt);
							if (benefitLimitOptExt.getBenefitLimitPct() == getCovOption().getOptionPct()) {
								return benefitLimitOpt;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Return the BenefitLimitOptionExtension for a BenefitLimitOption.
	 * @param benefitLimitOption 
	 * @return the BenefitLimitOptionExtension
	 */
	//P2AXAL016 New Method
	protected BenefitLimitOptionExtension getBenefitLimitOptionExtensionFor(com.csc.fs.dataobject.accel.product.BenefitLimitOption benefitLimitOption) {
		BenefitLimitOptionExtension benefitLimitOptionExtension = null;
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		int extCount = benefitLimitOption.getOLifEExtensionCount();
		for (int index = 0; index < extCount; index++) {
			extension = benefitLimitOption.getOLifEExtensionAt(index); //NBA237
			if (extension != null) {
				if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isBenefitLimitOptionExtension()) {
					benefitLimitOptionExtension = extension.getBenefitLimitOptionExtension();
				}
			}
		}
		return benefitLimitOptionExtension;
	}
	/**
	 * Return the ArrangementProduct from the PolicyProduct for a Arrangement.
	 * @param Arrangement
	 * @return ArrangementProduct
	 */
	//P2AXAL016 New Method	
	protected ArrangementProduct getArrangementProductFor(Arrangement arrangement) { 
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Inside getArrangementProductFor >>> "+ arrangement.getArrType());
		}
		LifeProductExtension lifeProdExtn = getLifeProductExtensionForPlan();
		if (lifeProdExtn != null ){
			ArrangementProduct arrangementProduct = null;
			ArrangementOptProduct arrangementOptProduct = null;
			for (int i = 0; i < lifeProdExtn.getArrangementProductCount(); i++) {
				arrangementProduct = lifeProdExtn.getArrangementProductAt(i);
				if (arrangementProduct.hasArrType() && arrangementProduct.getArrType() == arrangement.getArrType()) {
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Inside getArrangementProductFor  matching arrangementProduct >>> "+ arrangementProduct.getArrType());
					}
					//Begin: P2AXAL027 - Need to check for ArrsubType also, as there can be 2 ArrangementProduct present in PPFL with same ArrType
					ArrangementExtension arrExt = NbaUtils.getFirstArrangementExtension(arrangement);
					if(arrExt!=null && arrExt.hasArrSubType() && arrExt.getArrSubType()>-1L){
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("Inside getArrangementProductFor ArrangementOptProductCount >>> "+ arrangementProduct.getArrangementOptProductCount());
						}
						for (int j = 0; j < arrangementProduct.getArrangementOptProductCount(); j++) {
							arrangementOptProduct = arrangementProduct.getArrangementOptProductAt(j);
							if (arrangementOptProduct.hasArrSubType() && arrangementOptProduct.getArrSubType() == arrExt.getArrSubType()){
								return arrangementProduct;
							}
						}
						continue; //ALII872
					}
					//End: P2AXAL027
					return arrangementProduct;
				}
			}
		}
		return null;
	}	

	/**
	 * Return the ArrangementOptProduct from the PolicyProduct for a Arrangement.
	 * @param Arrangement
	 * @return ArrangementOptProduct
	 */
	//P2AXAL016 New Method	
	protected ArrangementOptProduct getArrangementOptProductFor(Arrangement arrangement) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Inside getArrangementOptProductFor >>> "+ arrangement.getArrType());
		}
		ArrangementProduct arrangementProduct = getArrangementProductFor(arrangement);
		if (getLogger().isDebugEnabled() && arrangementProduct != null) {
			getLogger().logDebug("Inside getArrangementOptProductFor  get arrangementProduct >>> "+ arrangementProduct.getArrType());
		}
		ArrangementExtension arrangementExtension = NbaUtils.getFirstArrangementExtension(arrangement);
		if (arrangementProduct != null ){
			ArrangementOptProduct arrangementOptProduct = null;
			for (int i = 0; i < arrangementProduct.getArrangementOptProductCount(); i++) {
				arrangementOptProduct = arrangementProduct.getArrangementOptProductAt(i);
				if (arrangementOptProduct.hasArrSubType() && arrangementExtension != null && arrangementOptProduct.getArrSubType() != arrangementExtension.getArrSubType())	
					continue;
				if (getLogger().isDebugEnabled() && arrangementProduct != null) {
					getLogger().logDebug("Inside getArrangementOptProductFor  got arrangementOptProduct >>> "+ arrangementOptProduct.getId());
				}
				return arrangementOptProduct;
			}

		}
		if (getLogger().isDebugEnabled() && arrangementProduct != null) {
			getLogger().logDebug("Inside getArrangementOptProductFor  got arrangementOptProduct >>> null ");
		}
		return null;
	}	

	/**
	 * Get the UnderwritingClassProduct for the coverage
	 * @return NBAAllowedBensRdrs
	 */
	// P2AXAL016 - New Method 
	public NBAAllowedBensRdrs getNBAAllowedBensRdrs(List nBAAllowedBensRdrs) {
		NBAAllowedBensRdrs bestMatch = null; 
		NBAAllowedBensRdrs temp = null;		
		if (nBAAllowedBensRdrs != null) {
			boolean isAppTypeBestMatch = false;		
			for (int i = 0; i < nBAAllowedBensRdrs.size(); i++) {
				temp = (NBAAllowedBensRdrs) nBAAllowedBensRdrs.get(i);
				if (productMatches(temp)) {
					if(temp.hasApplicationType()){
						isAppTypeBestMatch = true;
					} else if (isAppTypeBestMatch) {
						continue;
					}
					bestMatch = temp;
				}
			}
		}
		if(bestMatch != null) { 
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("NBAAllowedBensRdrs found: " + bestMatch.getId());
			}
			return bestMatch;
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("NBAAllowedBensRdrs found: NULL");
		}
		return null;
	}	

	/**
	 * Evaluate whether the given NBAAllowedBensRdrs object matches the case data.
	 * @param nbaAllowedBensRdrs
	 * @return boolean (if the object is a match)
	 */	
	//P2AXAL016 - New Method 
	private boolean productMatches(NBAAllowedBensRdrs nbaAllowedBensRdrs) {
		//Use GuarIssInd
		if (nbaAllowedBensRdrs.hasGuarIssInd() && nbaAllowedBensRdrs.getGuarIssInd()) return false;

		//Use QualifiedInd
		if (nbaAllowedBensRdrs.hasQualifiedInd() && nbaAllowedBensRdrs.getQualifiedInd()!= NbaUtils.isQualifiedPlan(getLife().getQualPlanType())) return false;

		//Use ApplicationType
		long applicationType = getApplicationInfo().getApplicationType(); 
		if( nbaAllowedBensRdrs.hasApplicationType() && nbaAllowedBensRdrs.getApplicationType() != applicationType) return false;

		return true;
	}

	/**
	 * Returns the Arrangement based on product code.
	 * @param productCode
	 * @return Arrangement
	 */
	//P2AXAL016 - New Method 
	public Arrangement getArrangement(String productCode) {
		Holding holding = getHolding();
		for (int i = 0; i < holding.getArrangementCount(); i++) {
			Arrangement arrangement = holding.getArrangementAt(i);
			if (arrangement != null && !NbaUtils.isDeleted(arrangement) && productCode!= null
					&& productCode.equals(arrangement.getProductCode())) {
				return arrangement;
			}
		}
		return null;
	}	

	/**
	 * Checks for whether ApplicationType is set or not.
	 * @param UnderwritingClassProduct
	 * @return true if ApplicationType is set, false if not
	 */
	//P2AXAL016 - New Method 	
	public boolean hasApplicationType (UnderwritingClassProduct underwritingClassProduct) {
		UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
		return ext!=null?ext.hasApplicationType():false;

	}

	/**
	 * Retrieve the appropriate translation from the NBA_STATES table.
	 * @param inVal
	 * @param tableName
	 * @return translation
	 */
	// P2AXAL016 New Method	
	protected String getStateCodeTranslation(long inVal, String tableName) {
		String description = "";
		NbaStatesData[] table = (NbaStatesData[])getUctTable(tableName);
		if (table != null) {
			for (int i = 0; i < table.length; i++) {
				if (table[i].getStateCode() == inVal) {
					description = table[i].getStateCodeTrans();
					break;
				}
			}
		}
		return (description);
	}
	/**
	 * Retrieve the PaymentModeMethProduct from the policyproduct for plan.
	 * @return paymentModeMethProduct
	 */
	//P2AXAL027 New Method
	public PaymentModeMethProduct getPaymentModeMethProduct() {

		PolicyProduct productForPlan = getPolicyProductForPlan();
		if (productForPlan != null) {
			PaymentModeMethProduct paymentModeMethProduct = null;
			for (int i = 0; i < productForPlan.getPaymentModeMethProductCount(); i++) {
				paymentModeMethProduct = productForPlan.getPaymentModeMethProductAt(i);
				if (paymentModeMethProduct.hasPaymentMethod() && paymentModeMethProduct.getPaymentMethod() != getPolicy().getPaymentMethod()) continue;
				if (paymentModeMethProduct.hasPaymentMode() && paymentModeMethProduct.getPaymentMode() != getPolicy().getPaymentMode()) continue;
				return paymentModeMethProduct;
			}
		}
		return null;		
	}
	/**
	 * Retrieve the rider premium from backend system
	 * @return the backend rider premium
	 * @throws NbaBaseException
	 */
	//P2AXAL016CV New Method
	protected void updateLife70BackendCalculations() {
		try {
			NbaBackendContractCalculator backendL70Calcs = NbaBackendContractCalculatorFactory.getCalculator(getSystemId());
			backendL70Calcs.setNbaUserVO(getUserVO());
			NbaTXLife calcData = backendL70Calcs.calculate(NbaContractCalculationsConstants.CALC_TYPE_CV_CALC, getNbaTXLife());

			if (calcData != null && calcData.isTransactionError()) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getBackendErrors(calcData), "");
				return;
			}
			//Merge the calculations in response with the input holding
			(new NbaLife70CalculationUtil(getNbaTXLife(), calcData, getNbaOLifEId(), NbaContractCalculationsConstants.CALC_TYPE_CV_CALC)).performCalculationMerge();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Life70 Merged Calculation Response : " + (null != getNbaTXLife() ? getNbaTXLife().toXmlString() : ""));
			}
		} catch (Throwable e) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Error in invoking calculation service", "");
		}
	}
	/**
	 * @return Returns the employment.
	 */
	//NBA297 New Method
	public Employment getEmployment() {
		return employment;
	}
	/**
	 * @param employment The employment to set.
	 */
	//NBA297 New Method
	public void setEmployment(Employment employment) {
		this.employment = employment;
	}
	/**
	 * @return Returns the financialExperience.
	 */
	//NBA297 New Method
	public FinancialExperience getFinancialExperience() {
		return financialExperience;
	}
	/**
	 * @param financialExperience The financialExperience to set.
	 */
	//NBA297 New Method
	public void setFinancialExperience(FinancialExperience financialExperience) {
		this.financialExperience = financialExperience;
	}
	/**
	 * @return Returns the intent.
	 */
	//NBA297 New Method
	public Intent getIntent() {
		return intent;
	}
	/**
	 * @param intent The intent to set.
	 */
	//NBA297 New Method
	public void setIntent(Intent intent) {
		this.intent = intent;
	}
	/**
	 * @return Returns the systemMessage.
	 */
	//NBA297 New Method
	public SystemMessage getSystemMessage() {
		return systemMessage;
	}
	/**
	 * @param systemMessage The systemMessage to set.
	 */
	//NBA297 New Method
	public void setSystemMessage(SystemMessage systemMessage) {
		this.systemMessage = systemMessage;
	}

	/**
	 * Determine if the Employment exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//NBA297 New Method
	protected boolean findNextEmployment(int occurrence) {
		if (getParty() != null) {
			if (getParty().getEmploymentCount() > occurrence) {
				setEmployment(getParty().getEmploymentAt(occurrence));
				logDebug(getEmployment().getId() + " found");
				return true;
			}
		}
		setEmployment(null);
		return false;
	}	

	/**
	 * Determine if the FinancialExperience exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//NBA297 New Method
	protected boolean findNextFinancialExperience(int occurrence) {
		if (getRisk() != null) {
			if (getRisk().getFinancialExperienceCount() > occurrence) {
				setFinancialExperience(getRisk().getFinancialExperienceAt(occurrence));
				logDebug(getFinancialExperience().getId() + " found");
				return true;
			}
		}
		setFinancialExperience(null);
		return false;
	}	
	/**
	 * Determine if the Employment exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//NBA297 New Method
	protected boolean findNextIntent(int occurrence) {
		if (getHolding() != null) {
			if (getHolding().getIntentCount() > occurrence) {
				setIntent(getHolding().getIntentAt(occurrence));
				logDebug(getIntent().getId() + " found");
				return true;
			}
		}
		setIntent(null);
		return false;
	}	

	/**
	 * Determine if the Employment exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//NBA297 New Method
	protected boolean findNextSystemMessage(int occurrence) {
		if (getHolding() != null) {

			if (getHolding().getSystemMessageCount() > occurrence) {
				setSystemMessage(getHolding().getSystemMessageAt(occurrence));
				logDebug(getSystemMessage().getId() + " found");
				return true;
			}
		}
		setSystemMessage(null);
		return false;
	}	

	/**
	 * Determine if the TempInsAgreementDetails exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//A4_AXAL001 New Method
	protected boolean findNextTempInsAgreementDetails(int occurrence) {
		TempInsAgreementInfo tempInsAgreementInfo = null;
		if (getApplicationInfoExtension() != null) {
			tempInsAgreementInfo = getApplicationInfoExtension().getTempInsAgreementInfo();
			if (tempInsAgreementInfo != null) {
				if (tempInsAgreementInfo.getTempInsAgreementDetailsCount() > occurrence) {
					setTempInsAgreementDetails(tempInsAgreementInfo.getTempInsAgreementDetailsAt(occurrence));
					logDebug(getTempInsAgreementDetails().getId() + " found");
					return true;
				}
			}
		}
		setTempInsAgreementDetails(null);
		return false;
	}
	/**
	 * Determine if the LifeUSA exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//CR1346709 new method
	protected boolean findNextLifeUSA(int occurrence) {
		if (occurrence < 1 && getLifeUSA() != null) {
			logDebug("LifeUSA found");
		}
		return occurrence < 1 && getLifeUSA() != null; // Only one occurrence of LifeUSA
	}
	/**
	 * @return Returns the employment.
	 */
	//NBA297 New Method
	public Object getLastControlObject() {
		return getControlObject(getLastControl());
	}

	/**
	 * Returns the objectStack.
	 * @return Object
	 */
	//NBA297 New Method created Logic Moved from NbaContractValidation.java
	protected Object getControlObject(String objectID) {
		if (objectID.equals(ADDRESS)) { //ACN012
			return getAddress();
		} else if (objectID.equals(ANNUITY)) {
			return getAnnuity();
		} else if (objectID.equals(APPLICATIONINFO)) {
			return getApplicationInfo();
		} else if (objectID.equals(ARRANGEMENT)) {
			return getArrangement();
		} else if (objectID.equals(ARRDESTINATION)) {
			return getArrDestination();
		} else if (objectID.equals(ARRSOURCE)) {
			return getArrSource();
		} else if (objectID.equals(BANKING)) {
			return getBanking();
		} else if (objectID.equals(CARRIERAPPOINTMENT)) {
			return getCarrierAppointment();
		} else if (objectID.equals(COVERAGE)) {
			return getCoverage();
		} else if (objectID.equals(COVOPTION)) {
			return getCovOption();
		} else if (objectID.equals(FINANCIALACTIVITY)) {
			return getFinancialActivity();
		} else if (objectID.equals(HOLDING)) {
			return getHolding();
		} else if (objectID.equals(INVESTMENT)) {
			return getInvestment();
		} else if (objectID.equals(LIFE)) {
			return getLife();
		} else if (objectID.equals(LIFEPARTICIPANT)) {
			return getLifeParticipant();
		} else if (objectID.equals(ORGANIZATION)) {
			return getOrganization();
		} else if (objectID.equals(PARTICIPANT)) {
			return getParticipant();
		} else if (objectID.equals(PARTY)) {
			return getParty();
		} else if (objectID.equals(PAYOUT)) {
			return getPayout();
		} else if (objectID.equals(PERSON)) {
			return getPerson();
		} else if (objectID.equals(POLICY)) {
			return getPolicy();
		} else if (objectID.equals(PRODUCER)) {
			return getProducer();
		} else if (objectID.equals(RIDER)) {
			return getRider();
		} else if (objectID.equals(RELATION)) {
			return getRelation();
		} else if (objectID.equals(REQUIREMENTINFO)) {
			return getRequirementInfo();
		} else if (objectID.equals(RISK)) {
			return getRisk();
		} else if (objectID.equals(SUBACCOUNT)) {
			return getSubAccount();
		} else if (objectID.equals(SUBSTANDARDRATING)) {
			return getSubstandardRating();
		} else if (objectID.equals(TAXWITHHOLDING)) {
			return getTaxWithholding();
		} else if (objectID.equals(SIGNATUREINFO)) { //AXAL3.7.40
			return getSignatureInfo();
		} else if (objectID.equals(APPSIGNATUREINFO)) { //AXAL3.7.40
			return getSignatureInfo();
		} else if (objectID.equals(FORMINSTANCE)) { //AXAL3.7.40
			return getFormInstance();
		} else if (objectID.equals(HHFAMILYINSURANCE)) { //AXAL3.7.40
			return getHHFamilyInsurance();
		} else if (objectID.equals(BANKHOLDING)) { //ALS3600
			return getBankHolding(); //ALS3600
		} else if (objectID.equals(FORMRESPONSE)) { //P2AXAL004
			return getFormResponse(); //P2AXAL004
		}else if (objectID.equals(EMPLOYMENT)) { //NBA297
			return getEmployment(); //NBA297
		}else if (objectID.equals(FINANCIALEXPERIENCE)) { //NBA297
			return getFinancialExperience(); //NBA297
		}else if (objectID.equals(INTENT)) { //NBA297
			return getIntent(); //NBA297
		}else if (objectID.equals(SYSTEMMESSAGE)) { //NBA297
			return getSystemMessage(); //NBA297
		}else if (objectID.equals(CLIENT)) { // Begin P2AXAL021
			return getClient(); 
		}else if (objectID.equals(SUITABILITYDETAILSCC)) {
			return getSuitabilityDetailsCC(); // End P2AXAL021
		} else if (objectID.equals(TEMPINSAGREEMENTDETAILS)) {//A4_AXAL001
			return getTempInsAgreementDetails();
		} else if (objectID.equals(LIFEUSA)) {// Begin CR1346709
			return getLifeUSA();
		}// End CR1346709	
		return null;
	}

	/**
	 * Determine if the last node of the Control Value matches the object expected by the process. Post a validation error if it does not.
	 * @param expectedCtl - long
	 * @return boolean
	 */
	//NBA297 New Method
	protected String getLastControl() {
		String ctlString = nbaConfigValProc.getCtl().trim();	
		return ctlString.substring(ctlString.lastIndexOf(".")+1, ctlString.length()); //The string for the object, i.e. COV
	}	

	/**
	 * Get the nbA vo extension to the ACORD model.
	 * Return a null if the parameter is null or the extension can't be found.
	 * @param object whose extension we want
	 * @return the extension object
	 */
	//NBA297 New Method	 
	public Object getObjectExtension(Object object) throws NbaBaseException{
		if (object == null) {
			return null;
		}
		try{
			NbaReflectionUtils nbaReflectionUtils = getNbaReflectionUtils();
			int extCount =((Integer) nbaReflectionUtils.getValue(object, "OLifEExtensionCount")).intValue();
			for (int index = 0; index < extCount; index++) {
				String clsName = (object.getClass().getName());
				clsName = clsName.substring(clsName.lastIndexOf(".") + 1, clsName.length());
				Class[] parameterTypes = {int.class};
				Object[] args = {new Integer(index)};		
				OLifEExtension extension  =(OLifEExtension) nbaReflectionUtils.getValue(object, "OLifEExtensionAt",parameterTypes,  args);			
				if (extension != null) {
					if ((NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode())) && nbaReflectionUtils.isValue(extension, clsName+"Extension") ) {
						return nbaReflectionUtils.getValue(extension, clsName + "Extension");
					}
				}			
			}			
		}catch (Exception e) {
			getLogger().logDebug(e.getMessage());
			throw new NbaBaseException(e.getMessage());
		}

		return null; // none could be found
	}

	/**
	 * Determine if the Client exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//P2AXAL021 New Method
	protected boolean findNextClient(int occurrence) {
		if (occurrence < 1 && getParty() != null) {
			if (getParty().hasClient()) {
				setClient(getParty().getClient());
				logDebug(getClient().getId() + " found");
				return true;
			}
		}
		setClient(null);
		return false;
	}


	/**
	 * @return Returns the client.
	 */
	//P2AXAL021 New Method
	public Client getClient() {
		return client;
	}
	/**
	 * @param client The client to set.
	 */
	//P2AXAL021 New Method
	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * Determine if the SuitabilityDetailsCC exists and create a pointer to it.
	 * @param occurrence the occurrence to search for
	 */
	//P2AXAL021 New Method
	protected boolean findNextSuitabilityDetailsCC(int occurrence) {
		if (getRisk() != null) {
			RiskExtension risExt = getRiskExtension(getRisk());
			if (risExt != null && risExt.hasSuitabilityDetailsCC() && occurrence < 1 ) {
				setSuitabilityDetailsCC(risExt.getSuitabilityDetailsCC());
				logDebug(getSuitabilityDetailsCC().getId() + " found");
				return true;
			}
		}
		setSuitabilityDetailsCC(null);
		return false;
	}



	/**
	 * @return Returns the suitabilityDetailsCC.
	 */
	public SuitabilityDetailsCC getSuitabilityDetailsCC() {
		return suitabilityDetailsCC;
	}
	/**
	 * @param suitabilityDetailsCC The suitabilityDetailsCC to set.
	 */
	public void setSuitabilityDetailsCC(SuitabilityDetailsCC suitabilityDetailsCC) {
		this.suitabilityDetailsCC = suitabilityDetailsCC;
	}

	/**
	 * @return NbaReflectionUtils instance.
	 */
	//NBA297 New Method
	public NbaReflectionUtils getNbaReflectionUtils() {
		if (nbaReflectionUtils == null) {
			nbaReflectionUtils = (NbaReflectionUtils)ServiceLocator.lookup(NbaReflectionUtils.REFLECTION_UTILS);
		}
		return nbaReflectionUtils;
	}

	/**
	 * Return the Joint insured relation
	 * @return Relation
	 */
	//P2AXAL054 New Method
	protected Relation getJointInsRelation() {
		return getRelation(OLI_REL_JOINTINSURED);
	}

	/**
	 * Determine if the party is Joint Insured by examining the Relation objects for the Party.
	 * @param insuredId - the party id of joint insured
	 * @return true if the party is an Joint Insured
	 */
	//P2AXAL054 New Method
	protected boolean isJointInsured(String insuredId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(insuredId) && relation.getRelationRoleCode() == OLI_REL_JOINTINSURED) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if it is Joint Insured's signature
	 */
	//P2AXAL054 New Method
	protected boolean isJointInsured(SignatureInfo signatureInfo) {
		if (signatureInfo != null) {
			if (!signatureInfo.isActionDelete() && signatureInfo.getSignatureRoleCode() == OLI_PARTICROLE_JOINT) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if Application form is New App
	 * @return true if Application form is New App
	 * @throws NbaBaseException
	 */
	//A3_AXAL002 New Method
	public boolean isNewAppFormNumber() throws NbaBaseException {
		if (isNewAppForm == null) {
			isNewAppForm = Boolean.valueOf(NbaVPMSHelper.isNewAppFormNumber(getNbaTXLife()));            
		}
		return isNewAppForm.booleanValue();
	}

	/**
	 * Returns true if Application form is New App Rev
	 * @return true if Application form is New App Rev
	 * @throws NbaBaseException
	 */
	//A3_AXAL002 New Method (Same logic applied for isNewAppRevFormNumber)Retrofit
	//APSL 1581
	public boolean isNewAppRevFormNumber() throws NbaBaseException {
		if (isNewAppRevForm == null) {
			isNewAppRevForm = Boolean.valueOf(NbaVPMSHelper.isNewAppRevFormNumber(getNbaTXLife()));            
		}
		return isNewAppRevForm.booleanValue();
	}

	/**
	 * Returns true if Application form is TConv
	 * @return true if Application form is TConv
	 * @throws NbaBaseException
	 */
	//A3_AXAL002 New Method
	public boolean isTConvFormNumber() throws NbaBaseException {
		if (isTConvForm == null) {
			isTConvForm = Boolean.valueOf(NbaVPMSHelper.isTConvFormNumber(getNbaTXLife()));            
		}
		return isTConvForm.booleanValue();
	}

	/**
	 * Returns true if Application form is AMIGV
	 * @return true if Application form is AMIGV
	 * @throws NbaBaseException
	 */
	//A3_AXAL002 New Method
	public boolean isAMIGVFormNumber() throws NbaBaseException {
		if (isAMIGVForm == null) {
			isAMIGVForm = Boolean.valueOf(NbaVPMSHelper.isAMIGVFormNumber(getNbaTXLife()));            
		}
		return isAMIGVForm.booleanValue();
	}

	/**
	 * @return true if the 1035exchange is set by the party on the case
	 * @param Party
	 */
	/**
	 * @return true if the 1035exchange is set by the party on the case
	 * @param Party
	 */
	//	A3_AXAL002 new method
	protected boolean is1035forParty(Party party) {
		List replHoldings = NbaUtils.getReplacementHolding(nbaTXLife, party.getId());
		for (int i = 0; i < replHoldings.size(); i++) {
			Holding holding = (Holding) replHoldings.get(i);
			PolicyExtension polExtension = NbaUtils.getFirstPolicyExtension(holding.getPolicy());
			if (polExtension != null && NbaUtils.isAnsweredYes(polExtension.getReplacementIndCode())) {
				Life life = getLife(holding.getPolicy());
				LifeUSAExtension lifeUsext = NbaUtils.getFirstLifeUSAExtension(life.getLifeUSA());
				if (lifeUsext != null && NbaUtils.isAnsweredYes(lifeUsext.getExch1035IndCode()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Return HasPowerOfAttorneyInd
	 * @param party
	 * @return
	 */
	//P2AXAl016, ALII834,ALII835 New Method
	protected boolean getPOAInd(Party party){
		PartyExtension partyExt = NbaUtils.getFirstPartyExtension(party);
		if (partyExt != null) {
			return partyExt.getInsHasPowerOfAttorneyInd();
		}
		return false;
	}

	/**
	 * Return CV Validator
	 */
	//P2AXAl068, New Method
	protected ValidatorBase getCVValidator() {
		if (getNbaConfigValProc().hasValidator() && serviceMap.get(getNbaConfigValProc().getValidator()) == null) {
			serviceMap.put(getNbaConfigValProc().getValidator(), ServiceLocator.lookup("newBusiness/comp/" + getNbaConfigValProc().getValidator()));
		}
		return getNbaConfigValProc().hasValidator() ? (ValidatorBase) serviceMap.get(getNbaConfigValProc().getValidator()) : (ValidatorBase) serviceMap.get("DefaultValidator");
	}
	/**
	 * Return Control Object's field value
	 */
	//P2AXAl068, New Method
	protected Object getFieldValue(Object ctrlObject) throws NbaBaseException {
		Object value = null;
		try {
			if (getNbaReflectionUtils().hasMethod(ctrlObject, getNbaConfigValProc().getField())) {
				value = getNbaReflectionUtils().getValue(ctrlObject, getNbaConfigValProc().getField());
			} else {
				Object objExt = getObjectExtension(ctrlObject);
				if (objExt != null) {
					value = getNbaReflectionUtils().getValue(objExt, getNbaConfigValProc().getField());
				}
			}
			return value;
		} catch (Exception ex) {
			getLogger().logDebug(ex.getMessage());
			throw new NbaBaseException(ex.getMessage());
		}
	}

	/**     
	 * @return true if Application form is GI
	 * @throws NbaBaseException
	 */
	//P2AXAL068 New Method
	public boolean isGIFormNumber() throws NbaBaseException {
		if (isGIForm == null) {
			isGIForm = Boolean.valueOf(NbaVPMSHelper.isGIFormNumber(getNbaTXLife()));            
		}
		return isGIForm.booleanValue();
	}

	/**
	 * Returns true if it is GI Supp Form
	 */
	// P2AXAL068 New Method
	protected boolean isGISuppForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_GISUPP.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return Requirement Validator
	 */
	//P2AXAL065, New Method
	protected ValidatorBase getReqValidator() {
		if (getNbaConfigValProc().hasReqval() && serviceMap.get(getNbaConfigValProc().getReqval()) == null) {
			serviceMap.put(getNbaConfigValProc().getReqval(), ServiceLocator.lookup("newBusiness/comp/" + getNbaConfigValProc().getReqval()));
		}
		return getNbaConfigValProc().hasReqval() ? (ValidatorBase) serviceMap.get(getNbaConfigValProc().getReqval()) : (ValidatorBase) serviceMap.get("DefaultValidator");
	}

	/**
	 * Validate gender combination of primary and joint insured for benefit as per PPfL.
	 */
	// P2AXAL055 New Method
	protected boolean isValidGenderCombination(LifeParticipant insuredOne, LifeParticipant insuredTwo, ArrayList genderProductInfoList) {
		boolean validGender = true;
		if (insuredOne != null && insuredTwo != null && insuredOne.hasIssueGender() && insuredTwo.hasIssueGender()) {
			if (genderProductInfoList != null  && !genderProductInfoList.isEmpty()) {
				validGender = false;
				for (int i = 0; i < genderProductInfoList.size(); i++) {
					GenderProductInfo genderProductInfo = (GenderProductInfo) genderProductInfoList.get(i);
					if (genderProductInfo.hasValidGender() && genderProductInfo.getValidGender() == insuredOne.getIssueGender()
							&& genderProductInfo.hasRelatedValidGender()) {
						if (genderProductInfo.getRelatedValidGender() == insuredTwo.getIssueGender()) {
							validGender = true;
							break;
						}
					}
				}
			}
		}
		return validGender;
	}


	/**
	 * Validate if Coverage is allowed based on NBAAllowedBensRdrs
	 * @param coverageProduct the List of CoverageProduct object
	 * @param covType the long Object 
	 */
	//ALII1194 New Method
	protected boolean validateCoverageProduct(List coverageProduct, long CovType ) {
		for (int i = 0; i < coverageProduct.size(); i++) {
			CoverageProduct covProd = (CoverageProduct) coverageProduct.get(i);
			CoverageProductExtension covProdExt = getCoverageProductExtensionFor(covProd);
			if (covProdExt != null && covProdExt.hasLifeCovTypeCode() && covProdExt.getLifeCovTypeCode() == CovType) {
				if (!covProdExt.getNBAAllowedBensRdrs().isEmpty() && getNBAAllowedBensRdrs(covProdExt.getNBAAllowedBensRdrs()) == null) {
					return false;
				}
				return true;
			}
		}   
		return false;
	} 	


	//CR61047 new method
	public DisabilityHealthProvisionsExtension getDisabilityHealthProvisionsExtensionFor(DisabilityHealthProvisions dhp) {
		DisabilityHealthProvisionsExtension DisabilityHealthProvisionsExtension = null;
		com.csc.fs.dataobject.accel.product.OLifEExtension extension; //NBA237
		if(dhp != null) { //APSL4521
			int extCount = dhp.getOLifEExtensionCount();
			for (int index = 0; index < extCount; index++) {
				extension = dhp.getOLifEExtensionAt(index); //NBA237
				if (extension != null) {
					if (NbaOliConstants.CSC_VENDOR_CODE.equals(extension.getVendorCode()) && extension.isDisabilityHealthProvisionsExtension()) {
						DisabilityHealthProvisionsExtension = extension.getDisabilityHealthProvisionsExtension();
					}
				}
			}
		}
		return DisabilityHealthProvisionsExtension;
	}

	//CR61047 new method
	public AccelerationPctOption getAccelerationPctOption(DisabilityHealthProvisionsExtension dhpe, long deathBenefitType) {
		AccelerationPctOption aco = null;
		for (int i = 0; i < dhpe.getAccelerationPctOptionCount(); i++) {
			aco = dhpe.getAccelerationPctOptionAt(i);
			if (aco.getDeathBenefitOptType() == deathBenefitType) {
				return aco;
			}
		}
		return null;
	}

	//CR61047 new method
	public AccelerationPctOption getAccelerationPctOption(long deathBenefitType){
		CovOptionProduct cop = getCovOptionProductFor(getCoverage(), getCovOption());
		CovOptionProductExtension copx = cop != null ? getCovOptionProductExtensionFor(cop): null; //APSL3629
		if ( copx != null ){
			DisabilityHealthProvisionsExtension dhpe = getDisabilityHealthProvisionsExtensionFor(copx.getDisabilityHealthProvisions());
			if ( dhpe != null ){
				return getAccelerationPctOption(dhpe, deathBenefitType);				
			}
		}
		return null;
	}

	//CR61047 new method
	public NonForProvision getNonForProvisionDefault(DisabilityHealthProvisionsExtension dhpe) {
		NonForProvision nfp = null;
		for (int i = 0; i < dhpe.getNonForProvisionCount(); i++) {
			nfp = dhpe.getNonForProvisionAt(i);
			if (nfp.getDefaultInd()) {
				return nfp;
			}
		}
		return null;
	}

	//CR61047 new method
	public NonForProvision getNonForProvisionDefault(){
		CovOptionProduct cop = getCovOptionProductFor(getCoverage(), getCovOption());
		CovOptionProductExtension copx = cop != null ? getCovOptionProductExtensionFor(cop): null; //APSL3629
		if ( copx != null ){
			DisabilityHealthProvisionsExtension dhpe = getDisabilityHealthProvisionsExtensionFor(copx.getDisabilityHealthProvisions());
			if ( dhpe != null ){
				return getNonForProvisionDefault(dhpe);				
			}
		}
		return null;
	}		
	/**
	 * Get the SubAccount.ProductCode for the subAccountIdentifier.
	 * @param subAccountIdentifier
	 * @return String
	 */
	//ALII1044 New Method
	protected String getSubAccountProductCodeFor(String subAccountIdentifier) {
		InvestProductInfo investProductInfo = null;
		PolicyProduct policyProduct = getPolicyProductForPlan();
		if (policyProduct != null) {
			for (int i = 0; i < policyProduct.getInvestProductInfoCount(); i++) {
				investProductInfo = policyProduct.getInvestProductInfoAt(i);
				for (int j = 0; j < investProductInfo.getInvestProductInfoSysKeyCount(); j++) {
					InvestProductInfoSysKey invProdInfoSysKey = investProductInfo.getInvestProductInfoSysKeyAt(j);
					if ("nbA_InvestProductID".equalsIgnoreCase(invProdInfoSysKey.getSystemCode())
							&& subAccountIdentifier.equalsIgnoreCase(invProdInfoSysKey.getPCDATA())) {
						return investProductInfo.getProductCode();
					}
				}
			}
		}
		return null;
	}

	/**
	 * determine if MSO fund is chosen in Standing allocation
	 */
	//ALII1044 New Method
	protected boolean isMSOSelected() {
		Arrangement saArr = AxaUtils.getArrangementByType(getHolding(), NbaOliConstants.OLI_ARRTYPE_STANDINGALLOC);
		if (saArr == null) {
			return false;
		}
		String msoProductCode = getSubAccountProductCodeFor("MSO");
		if (msoProductCode == null) {
			return false;
		}

		for (int i = 0; i < saArr.getArrDestinationCount(); i++) {
			ArrDestination arrDest = saArr.getArrDestinationAt(i);
			SubAccount subAccount = AxaUtils.getSubAccountByID(getHolding(), arrDest.getSubAcctID());
			if (subAccount != null && subAccount.getProductCode().equals(msoProductCode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the LifeUSAProduct for a Plan.
	 * @param lifeProductExt the LifeProductExtension
	 * @return the LifeUSAProduct
	 */
	//P2AXAL048 New Method
	protected LifeUSAProduct getLifeUSAProduct(LifeProductExtension lifeProductExt) {
		if (lifeProductExt != null) {
			if(lifeProductExt.getLifeUSAProductCount() > 0){
				return (LifeUSAProduct) lifeProductExt.getLifeUSAProduct().get(0);
			}
		}
		return null;
	}
	/**
	 * Get the UnderwritingClassProduct for the coverage
	 * @param coverage
	 * @param useAge
	 * @return UnderwritingClassProduct
	 */
	// AXAL3.7.40 - New Method QC8011
	protected UnderwritingClassProduct getUnderwritingClassProductFor(Coverage coverage, boolean useAge) {
		LifeParticipant lifeParticipant = getLifeParticipantForLookup(coverage);
		//Begin ALS3033
		long gender = lifeParticipant.getIssueGender(); 
		if ( coverage.getIndicatorCode() != OLI_COVIND_BASE) {
			gender = getNbaTXLife().getParty(lifeParticipant.getPartyID()).getGender();
		}
		// End ALS3033		
		int age = useAge ? lifeParticipant.getIssueAge() : -1;
		long uwClass = lifeParticipant.getUnderwritingClass();
		long smokerStat = getSmokerStat(lifeParticipant);
		long qualPlanType = getLife().getQualPlanType();//ALPC066
		long distributionChannel = 0;
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtn != null) {
			distributionChannel = policyExtn.getDistributionChannel();
		}
		long jurisdiction = getApplicationInfo() == null ? -1L : getApplicationInfo().getApplicationJurisdiction();
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
					"getUnderwritingClassProductFor: gender=" + String.valueOf(gender) + ",smokerStat=" + String.valueOf(smokerStat) + ",uwClass="
							+ String.valueOf(uwClass) + ",distributionChannel=" + String.valueOf(distributionChannel) + ",age=" + String.valueOf(age)
							+ ",qualPlanType=" + String.valueOf(qualPlanType));
		}
		UnderwritingClassProduct temp = null;
		UnderwritingClassProduct bestMatchProduct = null; //ALS3333
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);
		for (int i = 0; coverageProduct != null && i < coverageProduct.getUnderwritingClassProductCount(); i++) {
			temp = (UnderwritingClassProduct) coverageProduct.getUnderwritingClassProduct().get(i);
			if (productMatches(temp, smokerStat, gender, uwClass, age, distributionChannel, jurisdiction, qualPlanType)) {
				bestMatchProduct = temp;//ALS3333
			}
		}
		if(bestMatchProduct != null) { //ALS3333
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("getUnderwritingClassProductFor found: " + bestMatchProduct.getId());//ALS3333
			}
			return bestMatchProduct;//ALS3333
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("getUnderwritingClassProductFor found: NULL");
		}
		return null;
	}
	/**
	 * Evaluate whether the given UnderwritingClassProduct object matches the case data.
	 * @param temp
	 * @param smokerStat
	 * @param gender
	 * @param uwClass
	 * @param age
	 * @param distributionChannel
	 * @param jurisdiction
	 * @return boolean (if the object is a match)
	 */
	// AXAL3.7.40 New Method QC8011
	private boolean productMatches(UnderwritingClassProduct temp, long smokerStat, long gender, long uwClass, long age, long distributionChannel,
			long jurisdiction, long qualPlanType) {
		qualPlanType = qualPlanType == -1 ? NbaOliConstants.OLI_QUALPLN_NONE : qualPlanType; //ALPC066
		if (!temp.hasSmokerStat() || temp.getSmokerStat() == smokerStat) { // SmokerStat matches
			if (!temp.hasUnderwritingClass() || temp.getUnderwritingClass() == uwClass) { // UW Class matches
				if (!temp.hasIssueGender() 
						|| temp.getIssueGender() == gender 
						|| temp.getIssueGender()  == OLI_GENDER_UNISEX
						|| temp.getIssueGender()  == OLI_GENDER_COMBINED
						) {	// Gender matches
					UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(temp);
					boolean agePassed = false;
					boolean jurisdictionPassed = false;
					boolean qualPlanTypePassed = false;//ALPC066
					if (ext == null) {
						agePassed = true;
						jurisdictionPassed = true;
						qualPlanTypePassed = true;//ALPC066
					} else {
						if (ext.getAgeAmtProductCount() > 0 && age > 0) {
							int ages[] = getMinMaxAges(ext, distributionChannel);
							if (ages[0] <= age && age <= ages[1]) { // Age is in range
								agePassed = true;
							}
						} else {
							agePassed = true;
						}

						if (ext.getJurisdictionCCCount() > 0) {
							for (int i = 0; i < ext.getJurisdictionCCCount(); i++) {
								JurisdictionCC jurisdictionCC = (JurisdictionCC) ext.getJurisdictionCC().get(i);
								for (int j = 0; j < jurisdictionCC.getJurisdictionCount(); j++) {
									if (jurisdictionCC.getJurisdictionAt(j) == jurisdiction) {
										jurisdictionPassed = true;
									}
								}
							}//Begin ALS3333 
						}else if(ext.getJurisdictionApprovalCount()>0){
							for (int i = 0; i < ext.getJurisdictionApprovalCount(); i++) {
								JurisdictionApproval jurisdictionApproval = (JurisdictionApproval) ext.getJurisdictionApproval().get(i);
								if (jurisdictionApproval.getJurisdiction() == jurisdiction) {
									jurisdictionPassed = true;
								}
							}//End ALS3333	
						} else {
							jurisdictionPassed = true;
						}

						//Begin ALPC066
						if (ext.hasQualifiedPlan()) {
							if (ext.getQualifiedPlan() == qualPlanType) {
								qualPlanTypePassed = true;
							}
						} else {
							qualPlanTypePassed = true;
						}
						//End Begin ALPC066
					}
					if (agePassed && jurisdictionPassed && qualPlanTypePassed) {//ALPC066
						return true;
					}
				}
			}
		}
		return false;
	}


	//P2AXAL062 new method
	public List getPermTableRatingsFromJointProduct(Coverage coverage, LifeParticipant lifeParticipant) {
		CoverageProductExtension cpe = getCoverageProductExtensionFor(coverage);
		Set permTableRatingsSet = new HashSet();
		ArrayList permTableRatingsList = new ArrayList();
		for (int i = 0; i < cpe.getJointProductUWGuidelineCount(); i++) {
			JointProductUWGuideline jointProduct = cpe.getJointProductUWGuidelineAt(i);
			for (int j = 0; j < jointProduct.getParticipantUWGuidelineCount(); j++) {
				ParticipantUWGuideline participantProduct = jointProduct.getParticipantUWGuidelineAt(j);
				if (participantProduct.getUnderwritingClass() == lifeParticipant.getUnderwritingClass()) {
					for (int k = 0; k < participantProduct.getUWAgeLimitsCount(); k++) {
						UWAgeLimits uwAgeLimits = participantProduct.getUWAgeLimitsAt(k);
						if (lifeParticipant.getIssueAge() <= uwAgeLimits.getMaxAge() && lifeParticipant.getIssueAge() >= uwAgeLimits.getMinAge()) {
							PermTableRatingCC permTableRatingCC = uwAgeLimits.getPermTableRatingCC();
							for (int l = 0; l < permTableRatingCC.getPermTableRatingCount(); l++) {
								permTableRatingsSet.add(String.valueOf(permTableRatingCC.getPermTableRatingAt(l)));
							}
						}
					}
				}
			}
		}
		permTableRatingsList.addAll(permTableRatingsSet);//add all distinct values into List
		return permTableRatingsList;
	}

	//P2AXAL062 new method
	public RelatedParticipantUWGuideline getRelatedParticipantUWGuidelineFor(ParticipantUWGuideline participantUWGuideline, LifeParticipant lifeParticipant, long tableRating) {
		RelatedParticipantUWGuideline relatedParticipantUWGuideline = null;
		RelatedParticipantUWGuideline relatedParticipantUWGuidelineWithoutSmokerStat = null;//APSL4057
		for (int i = 0; participantUWGuideline!=null && i<participantUWGuideline.getRelatedParticipantUWGuidelineCount(); i++) {
			relatedParticipantUWGuideline = participantUWGuideline.getRelatedParticipantUWGuidelineAt(i);
			if(relatedParticipantUWGuideline.hasSmokerStat() 
					&& relatedParticipantUWGuideline.getSmokerStat() == lifeParticipant.getSmokerStat()) {//APSL4057
				if (productMatches(relatedParticipantUWGuideline, lifeParticipant, tableRating)) {
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("RelatedParticipantUWGuideline found: " + relatedParticipantUWGuideline.getId());
					}
					return relatedParticipantUWGuideline;
				}	
			}else if (relatedParticipantUWGuidelineWithoutSmokerStat == null) {//If RelatedParticipantUWGuideline without smokerstat is still not found
				//APSL4057
				if (productMatches(relatedParticipantUWGuideline, lifeParticipant, tableRating)) {
					relatedParticipantUWGuidelineWithoutSmokerStat = relatedParticipantUWGuideline;
				}
			}
		}
		if (getLogger().isDebugEnabled()) {
			if(relatedParticipantUWGuidelineWithoutSmokerStat != null) {
				getLogger().logDebug("RelatedParticipantUWGuideline found: " + relatedParticipantUWGuidelineWithoutSmokerStat.getId());
			}else {
				getLogger().logDebug("RelatedParticipantUWGuideline not found: NULL");
			}
		}
		return relatedParticipantUWGuidelineWithoutSmokerStat;//APSL4057
	}    

	//P2AXAL062 new method
	public ParticipantUWGuideline getParticipantUWGuidelineFor(Coverage coverage, LifeParticipant lifeParticipant, long tableRating) {
		CoverageProductExtension cpe = getCoverageProductExtensionFor(coverage);
		ParticipantUWGuideline participantUWGuidelineWithoutSmokerStat = null;//APSL4057
		for (int i = 0; cpe!=null && i < cpe.getJointProductUWGuidelineCount(); i++) {
			JointProductUWGuideline jointProductUWGuideline = cpe.getJointProductUWGuidelineAt(i);
			ParticipantUWGuideline participantUWGuideline = null;
			for (int j = 0; j < jointProductUWGuideline.getParticipantUWGuidelineCount(); j++) {
				participantUWGuideline = jointProductUWGuideline.getParticipantUWGuidelineAt(j);
				if(participantUWGuideline.hasSmokerStat() 
						&& participantUWGuideline.getSmokerStat() == lifeParticipant.getSmokerStat()) {//APSL4057
					//smoker stat is matching
					if (productMatches(participantUWGuideline, lifeParticipant, tableRating)) {
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("ParticipantUWGuideline found: " + participantUWGuideline.getId());
						}
						return participantUWGuideline;
					}
				}else if(participantUWGuidelineWithoutSmokerStat == null) {//If ParticipantUWGuideline without smokerstat is still not found
					//APSL4057
					if (productMatches(participantUWGuideline, lifeParticipant, tableRating)) {
						participantUWGuidelineWithoutSmokerStat = participantUWGuideline;//APSL4057
					}

				}
			}
		}
		if (getLogger().isDebugEnabled()) {
			if (participantUWGuidelineWithoutSmokerStat != null) {
				getLogger().logDebug("ParticipantUWGuideline found: " + participantUWGuidelineWithoutSmokerStat.getId());
			}else {
				getLogger().logDebug("ParticipantUWGuideline not found: NULL");	
			}

		}
		return participantUWGuidelineWithoutSmokerStat;//APSL4057
	}

	/**
	 * Evaluate whether the given participantUWGuideline object matches the case data.
	 * @return boolean (if the object is a match)
	 */
	//P2AXAL062 new method
	private boolean productMatches(ParticipantUWGuideline participantUWGuideline, LifeParticipant lifeParticipant,long tableRating) {

		if (participantUWGuideline.hasUnderwritingClass() && participantUWGuideline.getUnderwritingClass() != lifeParticipant.getUnderwritingClass()){
			return false; // UW Class Unmatched
		}

		if(!checkUWAgeLimits(participantUWGuideline.getUWAgeLimits(),lifeParticipant,tableRating)){
			return false;
		}


		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("productMatches ParticipantUWGuideline Id: " + participantUWGuideline.getId());
		}		
		return true;
	}


	/**
	 * Evaluate whether the given RelatedParticipantUWGuideline object matches the case data.
	 * @return boolean (if the object is a match)
	 */
	//P2AXAL062 new method
	private boolean productMatches(RelatedParticipantUWGuideline relParticipantUWGuideline, LifeParticipant lifeParticipant,long tableRating) {

		if (relParticipantUWGuideline.hasUnderwritingClass() && relParticipantUWGuideline.getUnderwritingClass() != lifeParticipant.getUnderwritingClass()){
			return false; // UW Class Unmatched
		}
		if(!checkUWAgeLimits(relParticipantUWGuideline.getUWAgeLimits(),lifeParticipant,tableRating)){
			return false;
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("productMatches RelatedParticipantUWGuideline Id: " + relParticipantUWGuideline.getId());
		}		
		return true;
	}    

	/**
	 * @param uwAgeLimitsList
	 * @param tableRating
	 * @return
	 */
	//P2AXAL062 new method
	public boolean checkUWAgeLimits(ArrayList uwAgeLimitsList,LifeParticipant lifeParticipant, long tableRating) {
		if(uwAgeLimitsList.size() == 0) return true;
		for (int i = 0; i < uwAgeLimitsList.size(); i++) {
			UWAgeLimits uwAgeLimits = (UWAgeLimits)uwAgeLimitsList.get(i);
			if (lifeParticipant.getIssueAge() <= uwAgeLimits.getMaxAge() && lifeParticipant.getIssueAge() >= uwAgeLimits.getMinAge()) {
				PermTableRatingCC permTableRatingCC = uwAgeLimits.getPermTableRatingCC();
				if (permTableRatingCC == null|| permTableRatingCC.getPermTableRatingCount()==0) return true;
				for (int j = 0;  j < permTableRatingCC.getPermTableRatingCount(); j++) {
					if (permTableRatingCC.getPermTableRatingAt(j) == tableRating) {
						return true;
					}
				}			
			}
		}	
		return false;
	}

	//P2AXAL062 new method
	public boolean validateJointAgeRatingLimits(Coverage coverage, LifeParticipant lifeParticipantPrimary, long primaryTableRating,
			LifeParticipant lifeParticipantJoint, long JointTableRating) {
		CoverageProductExtension cpe = getCoverageProductExtensionFor(coverage);
		if (cpe == null || cpe.getJointProductUWGuidelineCount()<1) { 
			return true;
		}
		ParticipantUWGuideline pUWGuideline = getParticipantUWGuidelineFor(coverage, lifeParticipantPrimary, primaryTableRating);
		if (pUWGuideline == null) {
			return false;
		} else {
			RelatedParticipantUWGuideline relatedGuideline = getRelatedParticipantUWGuidelineFor(pUWGuideline, lifeParticipantJoint, JointTableRating);
			if (relatedGuideline == null) {
				return false;
			}
		}
		return true;
	}

	//  P2AXAL062 new method
	public String getEquivalentRatingText(long equivalentRating) {
		String description = "";
		if (equivalentRating == 1) {
			description = "None";
		} else {
			description = getDescription(equivalentRating, NbaTableConstants.AXA_EQUIVRATINGS);
		}
		return description;
	}

	/**
	 * Returns the ClientExtension. 
	 */	
	//QC8420
	public ClientExtension getClientExtension() {
		return NbaUtils.getFirstClientExtension(getClient());
	}	

	/**
	 * Returns true if EmploymentStatusTC = OLI_EMPSTAT_ACTIVE 
	 */	
	//QC8437
	public boolean isActiveEmp(Employment employement) {
		return employement != null && employement.hasEmploymentStatusTC() && employement.getEmploymentStatusTC() == OLI_EMPSTAT_ACTIVE;
	}	


	/**
	 * Evaluate whether the given AgeAmtProduct object matches the case data.
	 * @param AgeAmtProduct
	 * @param lifeParticipant
	 * @param gender
	 * @param compCriteria
	 * @return boolean (if the object is a match)
	 */
	// ALII1436 New Method //P2AXAl016 method Refactored
	private boolean productMatches(AgeAmtProduct ageAmount, LifeParticipant lifeParticipant,  int compCriteria) {
		//Begin P2AXAl016 Code moved from method getUnderwritingClassProduct

		//Match Criteria : Distribution channel Wholesale/Retail		
		long distributionChannel = 0;		
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtn != null) {
			distributionChannel = policyExtn.getDistributionChannel();
		}
		if (! checkDistributionChannel(ageAmount, distributionChannel)) 
		{
			return false;
		}

		//Match Criteria :Age 
		if ((compCriteria & POS1 ) > 0 ) {
			int age = lifeParticipant.getIssueAge() ;			
			if (age>=0 && ageAmount.hasMinAge() && ageAmount.getMinAge() > age)  return false; // FaceAmount less than minimum
			if (age>=0 && ageAmount.hasMaxAge() && ageAmount.getMaxAge() < age)  return false; // FaceAmount more than Maximum			
		} 

		//Match Criteria :Age
		if ((compCriteria & POS2 ) > 0){
			double faceAmt = getLife().getFaceAmt();
			if (faceAmt>=0 && ageAmount.hasMinAmt() && ageAmount.getMinAmt() > faceAmt)  return false; // FaceAmount less than minimum
			if (faceAmt>=0 && ageAmount.hasMaxAmt() && ageAmount.getMaxAmt() < faceAmt)  return false; // FaceAmount more than Maximum
		}

		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("productMatches AgeAmtProduct Id: " + ageAmount.getId());//ALS3333
		}		
		return true;
	}	

	/**
	 * SR641590 (APSL2012) SUB-BGA new method
	 * @param partyId
	 * Identifying that the Primary Writing Agent belongs to Sub-Firm or not.
	 */
	protected boolean isWritingAgentBelongToSubfirm(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; relations != null && i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getOriginatingObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_SUBORDAGENT) { // NbaUtils.isSubFirmRelation(relation)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * SR641590 (APSL2012) SUB-BGA new method 
	 * Identifying that the case is related to Sub-Firm or not 
	 * with subFirmIndicator in ApplicationInfoExtension.
	 */
	protected boolean hasSubfirm() {
		ApplicationInfoExtension appInfoExt = getApplicationInfoExtension();
		if (appInfoExt != null) {
			return appInfoExt.getSubFirmIndicator();
		}
		return false;
	}

	/**
	 * SR641590 (APSL2012) SUB-BGA new method	 
	 * Identifying the Permission Level of Sub-Firm 
	 * with permissionCode in OrganizationExtension.
	 */
	protected int subFirmPermissionLevel() {
		if (getNbaTXLife() != null) {
			Relation primAgentRel = getNbaTXLife().getRelationForRelationRoleCode(OLI_REL_PRIMAGENT);
			if (primAgentRel != null) {				
				ArrayList relations = getNbaTXLife().getOLifE().getRelation();
				for (int i = 0; relations != null && i < relations.size(); i++) {
					Relation relation = (Relation) relations.get(i);
					if (relation != null && !relation.isActionDelete()
							&& relation.getRelationRoleCode() == OLI_REL_SUBORDAGENT 
							&& primAgentRel.getRelatedObjectID().equalsIgnoreCase(relation.getOriginatingObjectID()) ) {
						NbaParty subfirmParty = getNbaTXLife().getParty(relation.getRelatedObjectID());
						if(subfirmParty != null){
							OrganizationExtension organizationExtension = NbaUtils.getFirstOrganizationExtension(subfirmParty.getOrganization());
							if (organizationExtension != null) {					
								return organizationExtension.getPermissionCode();
							}
						}
					}
				}
			}
		}
		return -1;
	}

	/**
	 * SR641590 (APSL2012) SUB-BGA new method	
	 * Identifying that the Sub-Firm Case Manager is present in the Primary Writing Agent's Hierarchy or not. 
	 */
	protected boolean hasSubFirmCaseManager() {
		if (getNbaTXLife() != null) {
			Relation primAgentRel = getNbaTXLife().getRelationForRelationRoleCode(OLI_REL_PRIMAGENT);
			if (primAgentRel != null) {
				ArrayList subFirmRelations = getNbaTXLife().getOLifE().getRelation();
				for (int i = 0; subFirmRelations != null && i < subFirmRelations.size(); i++) {
					Relation subFirmRelation = (Relation) subFirmRelations.get(i);
					if (subFirmRelation != null && !subFirmRelation.isActionDelete()
							&& subFirmRelation.getRelationRoleCode() == OLI_REL_SUBORDAGENT
							&& primAgentRel.getRelatedObjectID().equalsIgnoreCase(subFirmRelation.getOriginatingObjectID())) {
						ArrayList cmRelations = getNbaTXLife().getOLifE().getRelation();
						for (int j = 0; cmRelations != null && j < cmRelations.size(); j++) {
							Relation cmRelation = (Relation) cmRelations.get(j);
							if (cmRelation != null && !cmRelation.isActionDelete()
									&& cmRelation.getRelationRoleCode() == OLI_REL_BGACASEMANAGER
									&& cmRelation.getOriginatingObjectID().equals(subFirmRelation.getRelatedObjectID()) ){
								return true;
							}									
						}
					}					
				}
			}
		}
		return false;
	}

	/**
	 * SR641590 (APSL2012) SUB-BGA new method	
	 * Identifying that the Sub-Firm is present in the Primary Writing Agent's Hierarchy or not. 
	 */
	protected boolean hasSubFirmInPrimaryAgentHierarchy() {
		if (getNbaTXLife() != null) {
			Relation primAgentRel = getNbaTXLife().getRelationForRelationRoleCode(OLI_REL_PRIMAGENT);
			if (primAgentRel != null) {				
				ArrayList relations = getNbaTXLife().getOLifE().getRelation();
				for (int i = 0; relations != null && i < relations.size(); i++) {
					Relation relation = (Relation) relations.get(i);
					if (relation != null && !relation.isActionDelete()
							&& relation.getRelationRoleCode() == OLI_REL_SUBORDAGENT 
							&& primAgentRel.getRelatedObjectID().equalsIgnoreCase(relation.getOriginatingObjectID()) ) {
						return true;
					}
				}				
			} else { //APSL2506 
				return true;
			}
		}
		return false;
	}

	//APSL4412 METHOD DELETED - getTXLifeFromCIPAttachment(), moved to AxaUtils.java
	//APSL4412 METHOD DELETED - getCIPMessageSeverity(), moved to AxaUtils.java

	/**
	 * Contract Validation process calculates the pending closure data for a Reg60 Nigo case 
	 * and will update the case add the data to the NBA_AUTO_CLOSURE table.
	 */
	// QC#9579 APSL2461 New Method  
	protected void setPlacementEndDateForReg60Nigo() throws NbaBaseException, NbaVpmsException {
		if (!PROC_FINAL_DISPOSITION.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())) { //QC14034/APSL3971
			ApplicationInfo appInfo = getApplicationInfo();
			ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			NbaVpmsAdaptor vpmsProxy = null;
			if (appInfoExtn != null && (appInfoExtn.getReg60Review() == NbaOliConstants.NBA_REG60REVIEW_NIGO || (appInfoExtn.getReg60Review() == NbaOliConstants.NBA_REG60REVIEW_PENDING && appInfoExtn.getReopenDate()!=null))) {//APSL4140 SR#662330
				try {
					NbaConfiguration nbaConfiguration = NbaConfiguration.getInstance();
					Map skipAttributes = new HashMap();
					skipAttributes.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUserVO().getUserID()));
					skipAttributes.put(NbaVpmsAdaptor.A_INSTALLATION, String.valueOf(nbaConfiguration.isAcNba()));
					// QC11621-APSL3588 START
					skipAttributes.put("A_UnpaidReissue", nbaTXLife.unpaidReissue() ? "yes" : "no");
					//BEGIN: APSL5349 
					//NbaDst CntChgWI = null;
					if (nbaTXLife.unpaidReissue()) {
						ContractChangeInfo latestContractChange = NbaUtils.getLatestValidContractChangeInfo(getNbaTXLife());
						if (!NbaUtils.isBlankOrNull(latestContractChange)) {
							List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
							List<Activity> reissueActivityList = NbaUtils.getActivityByTypeCodeAndRelatedObjId(activityList, NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE, latestContractChange.getId());
							Activity activity = NbaUtils.getActivityByStatus(reissueActivityList,NbaOliConstants.OLIEXT_LU_ACTSTAT_INITIATED);
							if (!NbaUtils.isBlankOrNull(activity)) {
								String CntDate = NbaUtils.getStringFromDate(activity.getStartTime().getTime());					
								skipAttributes.put("A_ContractChangeDate", CntDate);
							}
						}
						/*CntChgWI = searchWI(NbaConstants.A_WT_CONTRACT_CHANGE);
						if (!NbaUtils.isBlankOrNull(CntChgWI)) {
							String CntDate = NbaUtils.getStringFromDate(NbaUtils.getDateFromStringInAWDFormat(CntChgWI.getNbaLob().getCreateDate()));
							skipAttributes.put("A_ContractChangeDate", CntDate);
						}*/
						//END: APSL5349
					}
					//QC11621-APSL3588 END

					NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getNbaDst().getNbaLob());
					oinkData.setContractSource(nbaTXLife, getNbaDst().getNbaLob());
					vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REPLACEMENTS_PROCESSING);
					vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GETCLOSURE);
					vpmsProxy.setSkipAttributesMap(skipAttributes);
					NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults()); //Get the PlacementEndDate(Closure Date) and
					if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
						String closureDate = (String) nbaVpmsResultsData.getResultsData().get(0);
						String closureType = (String) nbaVpmsResultsData.getResultsData().get(1);
						int closureInd = Integer.parseInt(nbaVpmsResultsData.getResultsData().get(2).toString());
						try {
							Date placementEndDate = get_YYYY_MM_DD_sdf().parse(closureDate);
							appInfoExtn.setClosureType(closureType);
							appInfoExtn.setClosureInd(closureInd);
							// BEGIN NBLXA-2155[NBLXA-2196]
							if(!nbaTXLife.isUnderwriterApproved()) {
							RequirementInfo reqInfo = null;
							Date appSubmitDate = getApplicationInfo().getSubmissionDate();
							if (NbaUtils.isTermCase(getPolicy())) {
								reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_PREMIUMQUOTE);
							} else {
								reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_SIGNILLUS);
							}
							if (appInfo.getApplicationJurisdiction() == NbaOliConstants.OLI_USA_NY && !NbaUtils.isBlankOrNull(reqInfo)
									&& reqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_RECEIVED
									&& NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) { // NBLXA-2155[NBLXA-2300]
								placementEndDate = NbaUtils.addDaysToDate(appSubmitDate, 10);
								}
							}
							// END NBLXA-2155[NBLXA-2196]
							appInfo.setPlacementEndDate(placementEndDate);
							appInfoExtn.setActionUpdate();
							appInfo.setActionUpdate();
							updateAutoClosureTable(placementEndDate, closureType, closureInd);
						} catch (ParseException e) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Unknown date format returned from VP/MS model: ",
									getNbaConfigValProc().getModel(), ", Date: ", closureDate), getIdOf(getApplicationInfo()));
						} catch (NbaBaseException e) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("PolicyNumber: ", getPolicy().getPolNumber()),
									getIdOf(getApplicationInfo()));
							e.printStackTrace();
						}
					} else {
						addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), ", Model: ",
								NbaVpmsAdaptor.REPLACEMENTS_PROCESSING), getIdOf(getPolicy()));
					}
				} catch (java.rmi.RemoteException re) {
					throw new NbaVpmsException(PROCESS_PROBLEM + NbaVpmsException.VPMS_EXCEPTION, re);
				} finally {
					try {
						if (vpmsProxy != null) {
							vpmsProxy.remove();
						}
					} catch (RemoteException re) {
						getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
					}
				}
			}
		}	
	}

	/**
	 * Function to retrieve restrictCode from vpms on the basis of msgCode 
	 */
	// QC9716/APSL2460 New Method 
	public int getRestrictCodeFromVpms(String entryPoint,String msgCode){
		NbaVpmsAdaptor vpmsProxy = null;
		int restrictCode=0; 
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getNbaDst().getNbaLob());
			Map deOink = new HashMap();
			deOink.put("A_MsgCode", msgCode); 
			vpmsProxy = new NbaVpmsAdaptor(oinkData, getNbaConfigValProc().getModel()); 
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				restrictCode = Integer.parseInt((String) nbaVpmsResultsData.getResultsData().get(0));
			}	
			return restrictCode; 
		} catch (NbaVpmsException e) {
			addNewSystemMessage(
					INVALID_VPMS_CALC,
					concat("Process: ", getNbaConfigValProc().getId(), " NbaVpmsException ", e.toString()),
					"");
		} catch (RemoteException e) {
			addNewSystemMessage(
					INVALID_VPMS_CALC,
					concat("Process: ", getNbaConfigValProc().getId(), " RemoteException ", e.toString()),
					"");
		} catch (NbaBaseException e) {
			addNewSystemMessage(
					INVALID_VPMS_CALC,
					concat("Process: ", getNbaConfigValProc().getId(), " NbaBaseException ", e.toString()),
					"");
		}finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		return restrictCode;
	}

	//	New Method APSL2808
	public List getAllUnderwritingClassProductsFor(Coverage coverage,LifeParticipant lifeParticipant, int criteria, boolean verifySmokerStatAndUWClass) {
		if (lifeParticipant == null) {
			lifeParticipant = getLifeParticipantForLookup(coverage);
		}
		long gender = lifeParticipant.getIssueGender(); 
		if ( coverage.getIndicatorCode() != OLI_COVIND_BASE) {
			gender = getNbaTXLife().getParty(lifeParticipant.getPartyID()).getGender();
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
					"getUnderwritingClassProduct for SI: gender=" + String.valueOf(gender) + ",smokerStat=" + String.valueOf(getSmokerStat(lifeParticipant))
					+ ",uwClass=" + String.valueOf(lifeParticipant.getUnderwritingClass()) + ",age="
					+ String.valueOf(lifeParticipant.getIssueAge()) + ",applicationType=" + getApplicationInfo().getApplicationType()
					+ ",qualPlanType=" + String.valueOf(getLife().getQualPlanType()));
		}
		UnderwritingClassProduct temp = null;
		List bestMatchProducts = new ArrayList(); 
		CoverageProduct coverageProduct = getCoverageProductFor(coverage, BEST_MATCH);
		for (int i = 0; coverageProduct != null && i < coverageProduct.getUnderwritingClassProductCount(); i++) {
			temp = (UnderwritingClassProduct) coverageProduct.getUnderwritingClassProduct().get(i);
			if (productMatches(temp, lifeParticipant,  gender, criteria, verifySmokerStatAndUWClass)) {
				bestMatchProducts.add(temp);
			}
		}
		return bestMatchProducts;
	}	

	//New Method APSL2808
	private boolean productMatches(UnderwritingClassProduct temp, LifeParticipant lifeParticipant,  long gender, int compCriteria, boolean verifySmokerStatAndUWClass) {
		//Begin P2AXAl016 Code moved from method getUnderwritingClassProduct
		int age = lifeParticipant.getIssueAge() ;
		long uwClass = lifeParticipant.getUnderwritingClass();
		long smokerStat = getSmokerStat(lifeParticipant);
		long qualPlanType = getLife().getQualPlanType();//ALPC066
		long distributionChannel = 0;		
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
					"getUnderwritingClassProduct for SI: gender=" + String.valueOf(gender) + ",smokerStat=" + smokerStat+ " ,uwClass=" + uwClass + ",age="
							+ age + ",applicationType=" + getApplicationInfo().getApplicationType()
							+ ",qualPlanType=" + String.valueOf(getLife().getQualPlanType()));
			getLogger().logDebug("By Pass smokerstat and UWClass check "+ verifySmokerStatAndUWClass);
		}
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtn != null) {
			distributionChannel = policyExtn.getDistributionChannel();
		}
		long jurisdiction = getApplicationInfo() == null ? -1L : getApplicationInfo().getApplicationJurisdiction();	
		//End P2AXAl016 Code moved from method getUnderwritingClassProduct


		qualPlanType = qualPlanType == -1 ? NbaOliConstants.OLI_QUALPLN_NONE : qualPlanType; //ALPC066
		if (verifySmokerStatAndUWClass && temp.getSmokerStat() != smokerStat)  return false; // SmokerStat Unmatched
		getLogger().logDebug("By Pass smokerstat check ");
		//Begin CR731686
		if((compCriteria & POS3 ) > 0){
			if (verifySmokerStatAndUWClass && temp.hasUnderwritingClass() && temp.getUnderwritingClass() != uwClass)
				return false; // UW Class Unmatched
			getLogger().logDebug("By Pass UWClass check ");
		}//End CR731686	
		if (temp.hasIssueGender() 
				&& temp.getIssueGender() != gender 
				&& temp.getIssueGender()  != OLI_GENDER_UNISEX
				&& temp.getIssueGender()  != OLI_GENDER_COMBINED )	return false; // Gender Unmatched  
		UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(temp);

		//Face Amount Moved up by ALII1436
		if ((compCriteria & POS2 ) > 0){
			double faceAmt = getLife().getFaceAmt();
			if (faceAmt>=0 && temp.hasMinIssueAmt() && temp.getMinIssueAmt() > faceAmt)  return false; // FaceAmount less than minimum
			if (faceAmt>=0 && temp.hasMaxIssueAmt() && temp.getMaxIssueAmt() < faceAmt)  return false; // FaceAmount more than Maximum
		}		

		if (ext != null) {
			//jurisdiction
			boolean jurisdictionPassed = false;			
			if (ext.getJurisdictionCCCount() > 0) {
				for (int i = 0; i < ext.getJurisdictionCCCount(); i++) {
					JurisdictionCC jurisdictionCC = (JurisdictionCC) ext.getJurisdictionCC().get(i);
					for (int j = 0; j < jurisdictionCC.getJurisdictionCount(); j++) {
						if (jurisdictionCC.getJurisdictionAt(j) == jurisdiction) {
							jurisdictionPassed = true;
						}
					}
				}//Begin ALS3333 
			}else if(ext.getJurisdictionApprovalCount()>0){
				for (int i = 0; i < ext.getJurisdictionApprovalCount(); i++) {
					JurisdictionApproval jurisdictionApproval = (JurisdictionApproval) ext.getJurisdictionApproval().get(i); //P2AXAL006
					if (jurisdictionApproval.getJurisdiction() == jurisdiction) {
						jurisdictionPassed = true;
					}
				}//End ALS3333	
			} else {
				jurisdictionPassed = true;
			}
			if (!jurisdictionPassed) return false;

			if (ext.hasQualifiedPlan() && ext.getQualifiedPlan() != qualPlanType) return false;//ALPC066

			//AgeAmtProduct Added by ALII1436
			boolean matchingAgeAmtProduct = (ext.getAgeAmtProductCount() <= 0);
			for (int i = 0; ext != null && i < ext.getAgeAmtProductCount(); i++) {
				if (productMatches(ext.getAgeAmtProductAt(i), lifeParticipant,   compCriteria )) {
					matchingAgeAmtProduct = true;
				}
			}
			if (!matchingAgeAmtProduct) return false;			

			//P2AXAL016 Begin
			//Application Type
			long applicationType = getApplicationInfo().getApplicationType(); 
			if (ext.hasApplicationType() && ext.getApplicationType() != applicationType) return false;
			//P2AXAL016 End
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("productMatches UnderwritingClassProduct Id: " + temp.getId());//ALS3333
		}		
		return true;
	}

	//New Method APSL2808
	public int getMaxIssueAgeWoSmokerStatAndUWClass(Coverage coverage) throws NbaBaseException{ //ALS4095
		int maxAge = 0; 
		try{
			getLogger().logDebug("Processing getMaxIssueAgeWoSmokerStatAndUWClass");
			long distributionChannel = 0;
			List productList = null;
			productList = getAllUnderwritingClassProductsFor(coverage, lifeParticipant, 0, false);
			if (null != productList) {
				for (int i = 0; i < productList.size(); i++) {
					UnderwritingClassProduct product = (UnderwritingClassProduct) productList.get(i);
					UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(product);
					PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
					if (policyExtn != null) {
						distributionChannel = policyExtn.getDistributionChannel();
					}
					if (coverage.getLifeCovTypeCode() == OLI_COVTYPE_CHILDTERM) {
						if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_DEP) {
							CoverageProductExtension cpExt = getCoverageProductExtensionFor(coverage);
							if (cpExt != null) {
								return cpExt.getChildMaxIssueAge();
							}
						} else if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
							return product.getMaxIssueAge(); //return Integer.MAX_VALUE;
						}
					}
					if (getNbaConfigValProc().hasModel()) {
						// Max Age is retrieved from a VPMS model for 148 series with substandard
						NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation();
						if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
							if (nbaVpmsResultsData.getResultsData().size() == 1) {
								String strAge = (String) nbaVpmsResultsData.getResultsData().get(0);
								int age = Integer.parseInt(strAge);
								if (age >= 0) {
									return age;
								}
							} else {
								addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
							}
						}
					}

					int ages[] = getMinMaxAges(ext, distributionChannel, getLifeParticipant());//P2AXAL055
					if (maxAge < ages[1]) {
						maxAge = ages[1];
					}
				}
			}
		} catch (Exception e){
			getLogger().logDebug("Product rules not found, missing underwriting data."+ e.getMessage());//P2AXAL006
			throw new NbaBaseException("Product rules not found, missing underwriting data.");
		}
		return maxAge;
		//end ALS4095
	}

	//New Method APSL2808
	public int getMinIssueAgeWoSmokerStatAndUWClass(Coverage coverage) throws NbaBaseException{ //ALS4095
		int minAge = 0; 
		try{
			getLogger().logDebug("Processing getMinIssueAgeWoSmokerStatAndUWClass");
			long distributionChannel = 0;
			List productList = null;
			productList = getAllUnderwritingClassProductsFor(coverage, lifeParticipant, 0, false);
			if (null != productList) {
				for (int i = 0; i < productList.size(); i++) {
					UnderwritingClassProduct product = (UnderwritingClassProduct) productList.get(i);
					UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(product);
					if (coverage.getLifeCovTypeCode() == OLI_COVTYPE_CHILDTERM) {
						if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_DEP) {
							return 0;
						} else if (getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
							return product.getMinIssueAge();//Integer.MIN_VALUE;
						}
					}

					PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());
					if (policyExtn != null) {
						distributionChannel = policyExtn.getDistributionChannel();
					}
					int ages[] = getMinMaxAges(ext, distributionChannel, getLifeParticipant());//P2AXAL055
					if(minAge > ages[0]){
						minAge = ages[0];
					}
				}
			}
		}catch (Exception e){
			getLogger().logDebug("Product rules not found, missing underwriting data."+ e.getMessage());//P2AXAL006
			throw new NbaBaseException("Product rules not found, missing underwriting data.");
		}
		return minAge;
	}
	//New Method - APSL2947, LTCSR
	protected String getLtcSuppFormNumber() {
		if (ltcSuppFormNumber == null) {
			RequirementInfo rInfo = getNbaTXLife().getRequirementInfo(getNbaTXLife().getPrimaryParty(),NbaOliConstants.OLI_REQCODE_1009800084);
			if(rInfo!= null){
				setLtcSuppFormNumber(rInfo.getFormNo());
			}

		}
		return ltcSuppFormNumber;
	}
	//New Method - APSL2947, LTCSR
	protected void setLtcSuppFormNumber(String form) {
		this.ltcSuppFormNumber = form;
	}

	/**
	 * Compare application sign date with SaleExpirationDate and SaleEffectiveDate while validating
	 * coverage/rider and benefits's validity in states. Impacted CVs are CV2910, CV2911, CV2912
	 * @param jurisdictionApproval
	 * @return true/false
	 */
	//APSL3172 - New Method
	public boolean isActiveInJurisdiction(JurisdictionApproval jurisdictionApproval) {
		Date appSignDate = getApplicationInfo().getSignedDate();
		if (appSignDate == null) {
			return true;
		}
		if (!jurisdictionApproval.hasSaleEffectiveDate() && !jurisdictionApproval.hasSaleExpirationDate()) {
			return true;
		} else if (!jurisdictionApproval.hasSaleEffectiveDate() && jurisdictionApproval.hasSaleExpirationDate()) {
			return appSignDate.compareTo(jurisdictionApproval.getSaleExpirationDate()) <= 0;
		} else if (jurisdictionApproval.hasSaleEffectiveDate() && !jurisdictionApproval.hasSaleExpirationDate()) {
			return appSignDate.compareTo(jurisdictionApproval.getSaleEffectiveDate()) >= 0;
		} else {
			return (appSignDate.compareTo(jurisdictionApproval.getSaleExpirationDate()) <= 0 && appSignDate.compareTo(jurisdictionApproval.getSaleEffectiveDate()) >= 0);
		}
	}	

	// SR787006-APSL3702 Begin
	private List priorInsurances = null;

	protected List getPriorInsurancesForLifeProductCoverage() { // APSL4285 Method name refactored
		if (priorInsurances == null) {
			priorInsurances = new ArrayList();
			if (getParty() != null) {
				try {
					List attachments = getParty().getAttachment();
					NbaTXLife nbaTXLife;
					for (int k = 0; k < attachments.size(); k++) {
						Attachment attachment = (Attachment) attachments.get(k);
						if (attachment.getAttachmentType() == OLI_ATTACH_PRIORINS) {
							AttachmentData attachmentData = attachment.getAttachmentData();
							nbaTXLife = new NbaTXLife(attachmentData.getPCDATA());
							Coverage coverage = nbaTXLife.getPrimaryCoverage(); // APSL4285
							if (!nbaTXLife.getPolicy().getPolNumber().equals(getNbaTXLife().getPolicy().getPolNumber()) && coverage != null
									&& !NbaUtils.isCoverageAnnuityProduct(coverage)) { // APSL4285
								priorInsurances.add(nbaTXLife);
							}
						}
					}
				} catch (Exception ex) {
					getLogger().logException("Error in retrieving the prior insurance contracts from TXLife for Party " + party.getId(), ex);
				}
			}
		}
		return priorInsurances;
	}

	private List mostRecentPriorInsurances = null;

	protected List getMostRecentPriorInsurancesForLifeProductCoverage() { // APSL4285 Method name refactored
		if (mostRecentPriorInsurances == null) {
			mostRecentPriorInsurances = new ArrayList();
			List priorInsurances = getPriorInsurancesForLifeProductCoverage(); // APSL4285			
			Date mostRecentPriorInsuranceEffDate = null;
			for (int i = 0; i < priorInsurances.size(); i++) {
				NbaTXLife priorInsurance = (NbaTXLife) priorInsurances.get(i);			
				if (priorInsurance.getPrimaryCoverage() != null && priorInsurance.getPrimaryCoverage().hasEffDate()) {
					Date effDate = priorInsurance.getPrimaryCoverage().getEffDate();
					int effDateCompare = NbaUtils.compare(effDate, mostRecentPriorInsuranceEffDate);
					if (effDateCompare > 0) {
						mostRecentPriorInsurances.clear();
						mostRecentPriorInsurances.add(priorInsurance);
						mostRecentPriorInsuranceEffDate = effDate;
					} else if (effDateCompare == 0) {
						mostRecentPriorInsurances.add(priorInsurance);
						mostRecentPriorInsuranceEffDate = effDate;
					}
				}
			}
		}

		return mostRecentPriorInsurances;
	}	
	// SR787006-APSL3702 End

	/**
	 * Function to check Prior insurance policy status 
	 */
	// QC12944/APSL3524 New Method
	public boolean checkPolicyStatus(String entryPoint, Map deOinkMap) {
		NbaVpmsAdaptor vpmsProxy = null;
		boolean status = false;
		try {
			vpmsProxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(), NbaVpmsAdaptor.AUTOUNDERWRITING);
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (NbaConstants.TRUE_STR.equalsIgnoreCase((String) nbaVpmsResultsData.getResultsData().get(0))) {
					status = true;
				}
			}
		} catch (NbaVpmsException e) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " NbaVpmsException ", e.toString()), "");
		} catch (RemoteException e) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " RemoteException ", e.toString()), "");
		} catch (NbaBaseException e) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " NbaBaseException ", e.toString()), "");
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Exception e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		return status;
	}
	//start APSL3451
	/**return boolean true for CV 2104 if any of below condition is true
	 * If the 1035 exchange question is answered �YES� 
	 * If the UWCM lowers the premium amount to be under the 7 pay limit,
	 * If MEC acknowledgement form requirement (RQTP - 1009800083) received and MEC reason selected in the information tab.
	 * If MEC amendment (MEC Acknowledgement) added on case,MEC indicator is yes and the MEC reason selected.
	 */
	protected boolean isPremiumExceeded7PayTest() {
		boolean isPremiumExceeded7Pay = false;
		boolean isMECReasonSelected = false;
		boolean exch1035Ind = false;
		boolean mec1035Ind = false;
		boolean mecInd = false;
		List compHolding = NbaUtils.getReplacementHolding(getNbaTXLife());
		for (int i = 0; i < compHolding.size(); i++) {
			Holding holding = (Holding) compHolding.get(i);
			if (holding.getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null) {
				Life lifeRep = holding.getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
				LifeUSAExtension lifeUSAExRep = NbaUtils.getFirstLifeUSAExtension(lifeRep.getLifeUSA());
				if (lifeUSAExRep != null && lifeUSAExRep.hasExch1035IndCode()) {
					if (lifeUSAExRep.getExch1035IndCode() == NbaOliConstants.NBA_ANSWERS_YES) {
						exch1035Ind = true;
					}
				}
			}
		}
		if (exch1035Ind) {
			isPremiumExceeded7Pay = true;
		} else {
			//Start APSL3695
			//double premium = getPolicy().getPaymentAmt();
			double premium = -1;
			if(getPolicy().getApplicationInfo().hasCWAAmt()){
				premium = getPolicy().getApplicationInfo().getCWAAmt();
			}//End APSL3695
			Life life = getNbaTXLife().getLife();
			if (life != null) {
				LifeUSA lifeUSA = life.getLifeUSA();
				if (lifeUSA != null) {
					mec1035Ind = lifeUSA.getMEC1035();
					mecInd = lifeUSA.getMECInd();
					if (mec1035Ind || mecInd) {
						if (lifeUSA.getSevenPayPrem() >= premium ) {
							isPremiumExceeded7Pay = true;
						}
					}else{
						isPremiumExceeded7Pay = true;
					}
					LifeUSAExtension lifeUSAExtn = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
					if (lifeUSAExtn != null && lifeUSAExtn.hasMECReason()) {
						long mecReason = lifeUSAExtn.getMECReason();
						if (!NbaUtils.isBlankOrNull(mecReason)) { //APSL3706
							isMECReasonSelected = true;
						}
					}
				}else{
					isPremiumExceeded7Pay = true;
				}
				List requirementInfos = getPolicy().getRequirementInfo();
				for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
					RequirementInfo reqInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
					if (reqInfo.getReqCode() == OLI_REQCODE_1009800083) {
						if (reqInfo.getReqStatus() == OLI_REQSTAT_RECEIVED && isMECReasonSelected) {
							isPremiumExceeded7Pay = true;
						}
					}
				}
				ArrayList endorsementList = new ArrayList();
				Endorsement endorsement = null;
				endorsementList = getPolicy().getEndorsement();
				for (int i = 0; i < endorsementList.size(); i++) {
					endorsement = (Endorsement) endorsementList.get(i);
					if (endorsement != null && NbaUtils.getFirstEndorseExtension(endorsement) != null) {
						EndorsementExtension endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
						String endorsementCodeContent = endorsementExtension.getEndorsementCodeContent();
						if (endorsementCodeContent.equalsIgnoreCase("A037") && mecInd && isMECReasonSelected) {
							isPremiumExceeded7Pay = true;
						}
					}
				}
			}
		}
		return isPremiumExceeded7Pay;
	} // END APSL3451
	//QC11621-APSL3588 new method to get Contract Change Work item
	public NbaDst searchWI(String workType) throws NbaBaseException {
		NbaSearchResultVO resultVO = null;
		NbaDst cntDst = null;
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(workType);
		NbaLob lob = new NbaLob();
		lob.setPolicyNumber(getNbaDst().getNbaLob().getPolicyNumber());
		searchVO.setNbaLob(lob);
		searchVO.setResultClassName("NbaSearchResultVO");
		searchVO = WorkflowServiceHelper.lookupWork(getUserVO(), searchVO);
		List searchResult = searchVO.getSearchResults();
		if (searchResult != null && searchResult.size() > 0) {
			resultVO = (NbaSearchResultVO) searchResult.get(searchResult.size() - 1);
			if (resultVO != null) {
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(resultVO.getWorkItemID(), true);
				cntDst = WorkflowServiceHelper.retrieveWorkItem(getUserVO(), retOpt);
			}
		}
		return cntDst;
	}
	/**
	 * Get the LTCSR Maximum Issue Age
	 * @return age
	 */
	//QC13028/APSL3630 New Method 
	protected int getLtcsrMaxIssueAge(Coverage coverage, CovOption covOption) {
		int maxAge = 999;
		UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProduct(coverage, getLifeParticipant(), covOption);
		if (underwritingClassProduct != null) {
			maxAge = underwritingClassProduct.hasMaxIssueAge() ? underwritingClassProduct.getMaxIssueAge() : maxAge;

		}
		return maxAge;
	}

	/**
	 * Get Total Initial Payment applied on the policy. 
	 * @return initPayment Total Initial Payment
	 */
	//APSL3451 New Method 
	protected double getTotalInitialPayment(){
		double initPayment = 0;
		for (int i = 0; i < getPolicy().getFinancialActivityCount(); i++) {
			FinancialActivity finActivity = (FinancialActivity) getPolicy().getFinancialActivity().get(i);
			FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(finActivity);
			if (!finActivity.hasFinActivitySubType() && financialActivityExtension != null && !financialActivityExtension.getDisbursedInd()) {
				if (finActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWA
						|| finActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PREMIUMINIT
						|| finActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_SPECPRODUCTCHK) {
					initPayment += finActivity.getFinActivityGrossAmt();
				}
			}
		}
		return initPayment;
	}

	/**
	 * Check if Notice and Ack of Potential Fed Income Tax Implications - MEC requirement is received on the policy or not. 
	 * @return boolean true if requirement is received else false
	 */
	//APSL3451 New Method 
	protected boolean isMecAckRequirementReceived(Policy policy) {
		RequirementInfo requirementInfo;
		for (int i = 0; i < policy.getRequirementInfo().size(); i++) {
			requirementInfo = policy.getRequirementInfoAt(i);
			if (requirementInfo.getReqCode() == OLI_REQCODE_1009800083 && requirementInfo.getReqStatus() == OLI_REQSTAT_RECEIVED) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if MEC Acknowledgement amendment is added on the policy or not. 
	 * @return boolean true if amendment is added else false
	 */
	//APSL3451 New Method 
	protected boolean isMecAmendmentAdded(Policy policy) {
		Endorsement endorsement = null;
		for (int i = 0; i < policy.getEndorsement().size(); i++) {
			endorsement = policy.getEndorsementAt(i);
			EndorsementExtension endorsementExt = NbaUtils.getFirstEndorseExtension(endorsement);
			if ("A037".equalsIgnoreCase(endorsementExt.getEndorsementCodeContent())) {
				return true;
			}
		}
		return false;
	}
	//QC16321/APSL4533 New Method
	public boolean checkTableRating(AgeAmtProduct ageAmount, LifeParticipant lifeParticipant) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("IN checkTableRating() ");
		}
		SubstandardRating subStandardRating = null;
		AgeAmtProductExtension ageAmountExt = getAgeAmtProductExtension(ageAmount);
		if (ageAmountExt != null) {
			PermTableRatingCC permTableRatingCC = ageAmountExt.getPermTableRatingCC();
			if (permTableRatingCC == null || permTableRatingCC.getPermTableRatingCount() == 0)
				return true;
			List substandardRatingList = lifeParticipant.getSubstandardRating();
			if (substandardRatingList == null || substandardRatingList.size() == 0)
				return true;
			for (int i = 0; i < substandardRatingList.size(); i++) {
				subStandardRating = (SubstandardRating) substandardRatingList.get(i);
				SubstandardRatingExtension subStandardRatingExt = NbaUtils.getFirstSubstandardExtension(subStandardRating);
				if (subStandardRatingExt != null) {
					if ((subStandardRatingExt.hasProposedInd() && subStandardRatingExt.getProposedInd() == false)
							|| subStandardRatingExt.getRatingStatus() == 1) {
						break;

					}
				}
			}
			List permTableRatingList = permTableRatingCC.getPermTableRating();
			if (permTableRatingList == null || permTableRatingList.size() == 0) {
				return true;
			}
			long subStandardrating = subStandardRating.getPermTableRating();
			if (subStandardrating <= 0) { //NBLXA-1705
				return true;
			}
			for (int j = 0; j < permTableRatingList.size(); j++) {
				Long permTableRating = (Long) permTableRatingList.get(j);
				long rating = permTableRating.longValue();
				if (rating == subStandardrating) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Calculate Insurance Age based on application signed date and birth date 
	 * and AgeRule=4 (Age nearest to month).
	 */
	// New Method QC16237/APSL4743
	protected int calcInsuranceAge(String signedDate, String birthDate, String ageRule) {
		Map skipAttributes = new HashMap();
		int age = 0;
		if (signedDate == null) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid Signed Date ", ""), "");
		} else if (birthDate == null) {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid Birth date ", ""), "");
		} else {
			skipAttributes.put("A_SignedDate", signedDate);
			skipAttributes.put("A_BirthDate_PINS", birthDate);
			skipAttributes.put("A_AgeRule", ageRule);
			NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(skipAttributes); // Get the age
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {
					String strAage = (String) nbaVpmsResultsData.getResultsData().get(0);
					age = Integer.parseInt(strAage);
				} else {
					addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
				}
			}
		}
		return age;
	}

	//APSL4740 New Method
	protected NbaDst retrieveCaseWithSources(NbaDst dst, NbaUserVO user){
		//create and set parent case retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		// if case
		if (dst.isCase()) {
			retOpt.setWorkItem(dst.getID(), true);
			retOpt.requestSources();
		} else { // if a transaction
			retOpt.setWorkItem(dst.getID(), true);
			retOpt.requestCaseAsParent();
			retOpt.requestSources();
			retOpt.requestTransactionAsSibling();
		}
		retOpt.setNbaUserVO(user);
		AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		return (NbaDst) workResult.getFirst();
	}
	//APSL4740

	/**
	 * Returns if LTC Personal Worksheet form present
	 */
	//	APSL4916 New Method
	protected boolean isLTCPWForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_LTCPW.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}

	//Begin changes done for PED CR04 - APSL4922
	protected Date calcBackDate(Date appSignedDate, Long appState) {
		Date maxBackDate = null;
		if (appSignedDate != null) {
			if (NbaOliConstants.OLI_USA_OH == appState.longValue())
				maxBackDate = NbaUtils.addMonthsToDate(appSignedDate, -3);
			else {
				maxBackDate = NbaUtils.addMonthsToDate(appSignedDate, -6);
			}
		}
		return maxBackDate;
	}
	//End changes done for PED CR04 - APSL4922

	//DOL APSL5015 New method to check if ERISA form present on case
	protected boolean isERISABICForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && (FORM_NAME_BICERISA.equalsIgnoreCase(formInstance.getFormName()))) {
				return true;
			}				
		}
		return false;
	}
	//APSL5015 DOL New method to check if IRA form present on the case
	protected boolean isIRABICForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && (FORM_NAME_BICIRA.equalsIgnoreCase(formInstance.getFormName()))) {
				return true;
			}
		}
		return false;
	}
	//APSL5304		
	public boolean isActiveCoverageProduct(CovOptionProduct covOptProd) {
		Date appSignDate = getApplicationInfo().getSignedDate();
		if (appSignDate == null) {
			return true;
		}
		if (!covOptProd.hasSaleEffectiveDate() && !covOptProd.hasSaleExpirationDate()) {
			return true;
		} else if (!covOptProd.hasSaleEffectiveDate() && covOptProd.hasSaleExpirationDate()) {
			return appSignDate.compareTo(covOptProd.getSaleExpirationDate()) <= 0;
		} else if (covOptProd.hasSaleEffectiveDate() && !covOptProd.hasSaleExpirationDate()) {
			return appSignDate.compareTo(covOptProd.getSaleEffectiveDate()) >= 0;
		} else {
			return (appSignDate.compareTo(covOptProd.getSaleExpirationDate()) <= 0 && appSignDate.compareTo(covOptProd.getSaleEffectiveDate()) >= 0);
		}
	}

	/**
	 * Calls the translation tables for UCT Tables
	 * 
	 * @param tableName
	 *            The name of the UCT table.	 
	 * @param covKey
	 *            Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	// NBLXA-187 New Method
	protected NbaTableData[] getUctTable(String tableName, String covKey) {

		HashMap aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, getNbaTXLife().getCarrierCode());
		aCase.put(NbaTableAccessConstants.C_TABLE_NAME, tableName);
		aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, covKey);
		aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, getNbaTXLife().getBackendSystem());

		NbaTableData[] tArray = null;
		try {
			tArray = getTableAccessor().getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {
		}

		return (tArray);
	}

	/**This Function checks is Application is Short term Con Case.
	 * @return
	 */
	protected boolean isTConvCase() {

		try {
			return Boolean.valueOf(NbaVPMSHelper.isTConvFormNumber(getNbaTXLife()));
		} catch (NbaBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return false;

	}
	//NBLXA-1254  New Method
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an Beneficiary Owner
	 */
	protected boolean isBeneficialOwner(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_BENEFICIALOWNER) {
					return true;
				}
			}
		}
		return false;
	}
	//NBLXA-1254  New Method
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an Controlling person
	 */
	protected boolean isControllingPerson(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_CONTROLLINGPERSON) {
					return true;
				}
			}
		}
		return false;
	}

	//NBLXA-1254  New Method
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an Auth Person
	 */
	protected boolean isAuthorizedPerson(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_AUTHORIZEDPERSON) {
					return true;
				}
			}
		}
		return false;
	}
	//NBLXA-1254  New Method
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an Trustee
	 */
	protected boolean isTrustee(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_TRUSTEE) {
					return true;
				}
			}
		}
		return false;
	}


	//NBLXA-1254  New Method
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an Entity Owner
	 */
	protected boolean isEntityOwner(String partyId) {
		List relations = NbaUtils.getRelationListbyRelatedObj(getNbaTXLife().getOLifE(), partyId);
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation)relations.get(i);
			NbaParty party =getNbaTXLife().getParty(partyId);
			if (relation.getRelationRoleCode()==OLI_REL_OWNER && (party.getPartyTypeCode()== NbaOliConstants.OLI_PT_ORG ) && (party.getOrganization().hasOrgForm() && (party.getOrgForm() == OLI_LU_ORGFORM_CORPORATION  || party.getOrgForm() == OLI_LU_ORGFORM_PARTNERSHIP ||party.getOrgForm() == OLI_LU_ORGFORM_LLC ) )) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns true if it is SeoSuppForm
	 */
	//NBLXA-1254  New Method
	protected boolean isSeoSuppForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_EntityOwnership.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}

	//NBLXA-1254  New Method
	/**
	 * Return the Relation for a role code RelatedObjectID.
	 * @return Relation
	 */
	protected Relation getRelation(long role,String relationId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelationRoleCode() == role) {
					if(relationId!=null){
						if(relation.getId().equals(relationId)){
							return relation;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Determine if the relation is a given role code and a given originating relation role code
	 * @param relation - the given relation
	 * @param roleCode - the given relation role code
	 * @param originatingRoleCode - the given originating relation role code
	 * @return true if the relation is for a given role code and a given originating relation role code, false otherwise
	 */
	// NBLXA-1254 New Method
	protected boolean isRelationFromRoleCodeAndOriginatingRelationRoleCode(Relation relation, long roleCode, long originatingRoleCode) {
		if (relation != null && relation.getRelationRoleCode() == roleCode) {
			Party originatingParty = NbaTXLife.getPartyFromId(relation.getOriginatingObjectID() , getNbaTXLife().getOLifE().getParty()) ;
			if(!NbaUtils.isBlankOrNull(originatingParty)){//NBLXA-1688
				Relation originatingRelation = getNbaTXLife().getRelationByRelatedId(originatingParty.getId());
				if (originatingRelation != null && originatingRelation.hasRelationRoleCode() && originatingRelation.getRelationRoleCode() == originatingRoleCode ) {//NBLXA-1688
					if (roleCode==OLI_REL_CONTROLLINGPERSON ){
						Party relatedParty = NbaTXLife.getPartyFromId(relation.getRelatedObjectID() , getNbaTXLife().getOLifE().getParty()) ;
						PartyExtension partyExt = NbaUtils.getFirstPartyExtension(relatedParty);
						if(partyExt != null && !partyExt.getBenOwnerSameAsCtrlPersonInd() ){
							return true;
						}
					}  else {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determine if the party is a given role code and a given originating relation role code
	 * @param partyId - the given party id
	 * @param roleCode - the given relation role code
	 * @param originatingRoleCode - the given originating relation role code
	 * @return true if the party id is for a given role code and a given originating relation role code, false otherwise
	 */
	// NBLXA-1254 New Method
	protected boolean isPartyFromRoleCodeAndOriginatingRelationRoleCode(String partyId, long roleCode, long originatingRoleCode) {
		// Relation relation = getNbaTXLife().getRelationByRelatedId(partyId);
		Relation roleRelation = null;
		List relList = getNbaTXLife().getRelationListByRoleCode(roleCode);
		for (int i = 0; i < relList.size(); i++) {
			Relation tmprelation = (Relation) relList.get(i);
			if (tmprelation != null && tmprelation.getRelatedObjectID() != null && tmprelation.getRelatedObjectID().equalsIgnoreCase(partyId)) {
				roleRelation = tmprelation;
				break;
			}
		}
		return isRelationFromRoleCodeAndOriginatingRelationRoleCode(roleRelation, roleCode, originatingRoleCode);
	}

	/**
	 * Returns true if it is SepSuppForm
	 */
	//NBLXA-1254  New Method
	protected boolean isSepSuppForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_ENTITYPAYOR.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}

	//NBLXA-1254  New Method
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * @param partyId - the party id
	 * @return true if the party is an Entity Owner
	 */
	protected boolean isEntityPayor(String partyId) {
		List relations = NbaUtils.getRelationListbyRelatedObj(getNbaTXLife().getOLifE(), partyId);
		OLifE oLifE = getNbaTXLife().getOLifE();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation)relations.get(i);
			Party party =  getNbaTXLife().getPartyFromId(relation.getRelatedObjectID(), oLifE.getParty());
			PartyExtension partyExt = NbaUtils.getFirstPartyExtension(party);
			if (partyExt != null) {
				if (relation.getRelationRoleCode()==OLI_REL_PAYER && (party.getPartyTypeCode()== NbaOliConstants.OLI_PT_ORG ) && (partyExt.getPayerTypePartCorpLLCInd())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if it is Trusted Contact Form
	 */
	// NBLXA-1611 New Method
	protected boolean isTrustedContactForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_TRUSTEDCONTACT.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}

	// NBLXA-1611 New Method
	/**
	 * Return the trustee relation if it exists.
	 * 
	 * @return Relation
	 */
	protected Relation getTrustedContactRelation(String relationId) {
		return getRelation(OLI_REL_TRUSTEDCONTACT, relationId);
	}

	// NBLXA-1254 New Method
	/**
	 * Determine if the party is an owner by examining the Relation objects for the Party.
	 * 
	 * @param partyId
	 *            - the party id
	 * @return true if the party is an Trustee
	 */
	protected boolean isTrusteContact(String partyId) {
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == OLI_REL_TRUSTEDCONTACT) {
					return true;
				}
			}
		}
		return false;
	}
	protected boolean isPayorSameAsInsuredOrOwner() {
		NbaParty primaryInsured = getNbaTXLife().getPrimaryParty();
		NbaParty owner = getNbaTXLife().getPrimaryOwner();
		NbaParty payorParty = nbaTXLife.getParty(getParty().getId());
		NbaParty jointInsured = getNbaTXLife().getJointParty();
		String payorName = NbaUtils.getFullName(payorParty);
		String pIName = NbaUtils.getFullName(primaryInsured);
		String jIName = NbaUtils.getFullName(jointInsured);
		String ownerName = NbaUtils.getFullName(owner);
		
		if (primaryInsured != null && !NbaUtils.isBlankOrNull(pIName) && !NbaUtils.isBlankOrNull(payorName) && pIName.equalsIgnoreCase(payorName)) {
			return true;
		}
		
		if (jointInsured != null && !NbaUtils.isBlankOrNull(jIName) && !NbaUtils.isBlankOrNull(payorName) && jIName.equalsIgnoreCase(payorName)) {
			return true;
		}
		
		if (owner != null && !NbaUtils.isBlankOrNull(ownerName) && !NbaUtils.isBlankOrNull(payorName) && ownerName.equalsIgnoreCase(payorName)) {
			return true;
		}
		return false;
	}

	// NBLXA-1782 Starts
	public void setPolicySubStatus(NbaDst parentCase) throws NbaNetServerDataNotFoundException, NbaBaseException {
		logDebug("Set Policy sub Status ");
		ApplicationInfo applInfo = getPolicy().getApplicationInfo();
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applInfo);
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getPolicy());

		long dist = 0;
		if (!NbaUtils.isBlankOrNull(policyExtn)) {
			dist = policyExtn.getDistributionChannel();

			if (dist == NbaOliConstants.OLI_DISTCHAN_10 && getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_ISSUED
					&& !NbaUtils.getOutstandingDeliveryRequirementList(getPolicy()).isEmpty()) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_INFORCEPENDINGPDR);// 9.Inforce, Pending Delivery Requirements
			} else if (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_ISSUED) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_INFORCE); // 10.Inforce (case is issued)
			//} else if (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_INCOMPLETE ) { //NBLXA-1782 New Changes 
			} else if (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_INCOMPLETE && NbaUtils.isClosureActivity(getNbaTXLife())) { //NBLXA-1782 New Changes 
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_AUTOINCOMPLETE); // 11.Auto Closed, Incomplete
				//NBLXA-1782 New Changes Starts
			} else if (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_INCOMPLETE) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_INCOMPLETE); // 16.Closed, Incomplete
			//} else if (NbaUtils.isReg60Case(getPolicy()) && getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_WITHDRAW) { 
			} else if ((NbaUtils.isReg60Nigo(getNbaTXLife()) && getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_WITHDRAW)
					|| getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_LAPSEPEND
					|| getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_WAIVERCOI) { // NBLXA-1782 (QC#20218) NBLXA-2224
				//NBLXA-1782 New Changes Ends 
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_REG60WITHDRAWN); // 3.Reg 60 Withdrawn

			} else if (!getNbaTXLife().isUnderwriterApproved()
					&& (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_WITHDRAW
							|| getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_CANCELLED
							|| getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_NOTTAKEN || getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_NOTAKE)) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_WITHDRAWN); // 12. Withdrawn
			} else if (getNbaTXLife().isUnderwriterApproved()
					&& (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_WITHDRAW
							|| getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_CANCELLED
							|| getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_NOTTAKEN || getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_NOTAKE)) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_NOTTAKEN); // 13.Not Taken
			} else if (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_DECISSUE) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_DECLINE); // 14.DECLINE
			} else if (getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_DEFERRED) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_POSTPONE); // 15.Postpone
			} else if (NbaUtils.isAgentMsgRestrictCodeOneExists(getNbaTXLife(), NbaOliConstants.NBA_MSGRESTRICTCODE_RESTREQDET)) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_LICENSINGREVIEW); // 1.Licensing Review

			} else if (NbaUtils.isReg60Case(getPolicy()) && null != appInfoExt && appInfoExt.hasReg60Review()
					&& appInfoExt.getReg60Review() == NbaOliConstants.NBA_REG60REVIEW_PENDING) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_REG60REVIEW); // 2.Reg 60 Review

			} else if (NbaUtils.isMsgRestrictCodeOneExists(getNbaTXLife(), NbaOliConstants.NBA_MSGRESTRICTCODE_RESTREQDET)) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_MISSINGAPPLICATION); // 4.Missing Application Info, Prevents
																												// Underwriter Review
			} else if (!getNbaTXLife().isUnderwriterApproved()) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_PENDING); // 5.Underwriting in Progress

			} else if (getNbaTXLife().isUnderwriterApproved()
					&& (NbaUtils.isPrintRestrictCVExists(getNbaTXLife()) || NbaUtils.checkReqHoldprint(getNbaTXLife())
							|| policyExtn.getPrintRistrictInd())) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_MEDICALLYAPPROVED);
				// 6.Medically Approved, Pending Admin Requirements
				// need to check for printhold and requirement printhold poller
			} else if (getNbaTXLife().isUnderwriterApproved()) {
				// NbaDst parentCase = getNbaDst();
				if (!parentCase.isCase()) {
					parentCase = retrieveParentCase(parentCase, getUserVO(), false);
				}
				String printStatus = getPrintStatus(getNbaTXLife(), parentCase, appInfoExt);
				if (NbaUtils.isBlankOrNull(printStatus) || NbaConstants.PRINT_IN_REVIEW.equalsIgnoreCase(printStatus)) {
					policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_APPROVEDPRINTPENDING); // 7.Approved, Pending Print
				} else if (NbaConstants.PRINT_COMPLETED.equalsIgnoreCase(printStatus)) {
				policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_MAILED); // 8.Mailed/eDelivered, Pending Delivery Requirements
				} else {
					policyExtn.setPolicySubStatus(NbaOliConstants.OLIEXT_LU_POLICYSUBSTATUS_APPROVEDPRINTPENDING); // 7.Approved, Pending Print
				}
			}
			policyExtn.setActionUpdate();
		}
	}

	public String getPrintStatus(NbaTXLife holdingInq, NbaDst parentCase, ApplicationInfoExtension appInfoExt) throws NbaBaseException {
		String awdprtStatus = NbaConstants.PRINT_IN_REVIEW;
		boolean allPrintInEndInd = false;
		if ((NbaUtils.isProductCodeCOIL(getNbaTXLife()) || NbaUtils.isAdcApplication(parentCase) || NbaUtils.isSimplifiedIssueCase(getNbaTXLife()))
				&& appInfoExt.hasContractPrintExtractDate()) {
			awdprtStatus = NbaConstants.PRINT_COMPLETED;
		} else {
			EPolicyData ePolicyData = null;
			ePolicyData = NbaUtils.retrieveActiveEPolicyData(holdingInq);
			if (!NbaUtils.isBlankOrNull(ePolicyData) && !NbaUtils.isBlankOrNull(ePolicyData.getPrintStatus())) {
				if (NbaOliConstants.OLI_PRINT_STATUS_IGO == ePolicyData.getPrintStatus()) {
					awdprtStatus = NbaConstants.PRINT_COMPLETED;
				} else if (NbaOliConstants.OLI_PRINT_STATUS_IGO != ePolicyData.getPrintStatus()) {
					awdprtStatus = NbaConstants.PRINT_IN_REVIEW;
				}
			}
			if (NbaOliConstants.OLI_APPSUBTYPE_REISSUE == appInfoExt.getApplicationSubType() || awdprtStatus == NbaConstants.PRINT_IN_REVIEW) {
				NbaSearchResultVO searchResultVO = null;
				NbaSearchVO searchPrintVO = new NbaSearchVO();
				searchPrintVO.setWorkType(NbaConstants.A_WT_CONT_PRINT_EXTRACT);
				searchPrintVO.setContractNumber(getPolicy().getPolNumber());
				searchPrintVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
				searchPrintVO = WorkflowServiceHelper.lookupWork(getUserVO(), searchPrintVO);
				if (!NbaUtils.isBlankOrNull(searchPrintVO) && searchPrintVO.getSearchResults() != null && !searchPrintVO.getSearchResults().isEmpty()) {
					List searchResultList = searchPrintVO.getSearchResults();
					for (int i = 0; i < searchResultList.size(); i++) {
						searchResultVO = (NbaSearchResultVO) searchResultList.get(i);
						if (!searchResultVO.getQueue().equals("END")) {
							allPrintInEndInd = false;
							break;
						}
						allPrintInEndInd = true;
					}

				}
				// } //NBLXA-1782
				if (allPrintInEndInd) {
					awdprtStatus = NbaConstants.PRINT_COMPLETED;
				} else {
					awdprtStatus = NbaConstants.PRINT_IN_REVIEW;
				} // NBLXA-1782
			}
		}
		return awdprtStatus;
	}
	// NBLXA-1782 ENDS
	
	/**
	 * Return the ArrangementProduct from the PolicyProduct for a Arrangement with specific productCode.
	 * @param Arrangement
	 * @return ArrangementProduct
	 */
	// NBLXA-1934 New Method
	protected ArrangementProduct getArrangementProductFor(Arrangement arrangement, String productCode) {
		LifeProductExtension lifeProdExtn = getLifeProductExtensionForPlan();
		if (lifeProdExtn != null) {
			ArrangementProduct arrangementProduct = null;
			ArrangementOptProduct arrangementOptProduct = null;
			for (int i = 0; i < lifeProdExtn.getArrangementProductCount(); i++) {
				arrangementProduct = lifeProdExtn.getArrangementProductAt(i);
				if (arrangementProduct.hasArrType() && arrangementProduct.getArrType() == arrangement.getArrType()) {
					// Begin: P2AXAL027 - Need to check for ArrsubType also, as there can be 2 ArrangementProduct present in PPFL with same ArrType
					ArrangementExtension arrExt = NbaUtils.getFirstArrangementExtension(arrangement);
					if (arrExt != null && arrExt.hasArrSubType() && arrExt.getArrSubType() > -1L) {
						for (int j = 0; j < arrangementProduct.getArrangementOptProductCount(); j++) {
							arrangementOptProduct = arrangementProduct.getArrangementOptProductAt(j);
							if (arrangementOptProduct.hasArrSubType() && arrangementOptProduct.getArrSubType() == arrExt.getArrSubType()
									&& arrangementOptProduct.hasProductCode() && productCode.equalsIgnoreCase(arrangementOptProduct.getProductCode())) {
								return arrangementProduct;
							}
						}
						continue; // ALII872
					}
					// End: P2AXAL027
					return arrangementProduct;
				}
			}
		}
		return null;
	}
	/**
	 * Return the ArrangementOptProduct from the PolicyProduct for a Arrangement for specific productCode.
	 * @param Arrangement
	 * @return ArrangementOptProduct
	 */
	//  NBLXA-1934 New Method
	protected ArrangementOptProduct getArrangementOptProductFor(Arrangement arrangement, String productCode) {
		ArrangementProduct arrangementProduct = getArrangementProductFor(arrangement, productCode);
		ArrangementExtension arrangementExtension = NbaUtils.getFirstArrangementExtension(arrangement);
		if (arrangementProduct != null) {
			ArrangementOptProduct arrangementOptProduct = null;
			for (int i = 0; i < arrangementProduct.getArrangementOptProductCount(); i++) {
				arrangementOptProduct = arrangementProduct.getArrangementOptProductAt(i);
				if (arrangementOptProduct.hasArrSubType() && arrangementExtension != null
						&& arrangementOptProduct.getArrSubType() != arrangementExtension.getArrSubType())
					continue;
				return arrangementOptProduct;
			}

		}
		return null;
	}
	
	/**
	 * @param formName
	 * @param questionNo
	 * @return
	 */
	// New Method NBLXA-1554[NBLXA-1950]
	protected String getFormResponseText(String formName, String questionNo) {
		if (formName != null || questionNo != null) {
			int noOfformInstances = (getNbaTXLife().getOLifE().getFormInstance() == null) ? 0 : getNbaTXLife().getOLifE().getFormInstance().size();
			for (int index = 0; index < noOfformInstances; index++) {
				FormInstance formInstance = getNbaTXLife().getOLifE().getFormInstanceAt(index);
				if (formInstance != null && formName.equalsIgnoreCase(formInstance.getFormName())) {
					int noOfformResponses = (formInstance.getFormResponse() == null) ? 0 : formInstance.getFormResponse().size();
					for (int i = 0; i < noOfformResponses; i++) {
						FormResponse formResponse = formInstance.getFormResponseAt(i);
						if (formResponse != null && questionNo.equalsIgnoreCase(formResponse.getQuestionNumber())) {
							return formResponse.getResponseText();
						}
					}
				}
			}
		}
		return BLANK_STRING;
	}
	
	/**
	 * @param formName
	 * @param questionNo
	 * @return
	 */
	// New Method NBLXA-1554[NBLXA-1969]
	protected long getFormResponseCodeByFormId(String formName, String questionNo, String formInsId) {
		if (formName != null || questionNo != null) {
			int noOfformInstances = (getNbaTXLife().getOLifE().getFormInstance() == null) ? 0 : getNbaTXLife().getOLifE().getFormInstance().size();
			for (int index = 0; index < noOfformInstances; index++) {
				FormInstance formInstance = getNbaTXLife().getOLifE().getFormInstanceAt(index);
				if (formInstance != null && formName.equalsIgnoreCase(formInstance.getFormName()) && formInsId.equalsIgnoreCase(formInstance.getId())) {
					int noOfformResponses = (formInstance.getFormResponse() == null) ? 0 : formInstance.getFormResponse().size();
					for (int i = 0; i < noOfformResponses; i++) {
						FormResponse formResponse = formInstance.getFormResponseAt(i);
						if (formResponse != null && questionNo.equalsIgnoreCase(formResponse.getQuestionNumber())) {
							return formResponse.getResponseCode();
						}
					}
				}
			}
		}
		return LONG_NULL_VALUE;
	}

	// NBLXA-1997 Starts
	protected boolean isReplParty(String partyId) {
		System.out.println("Entering in isReplParty ");
		ArrayList relations = getNbaTXLife().getOLifE().getRelation();
		for (int i = 0; i < relations.size(); i++) {
			Relation relation = (Relation) relations.get(i);
			if (!relation.isActionDelete()) {
				if (relation.getRelatedObjectID().equals(partyId) && relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO
						&& relation.getOriginatingObjectType() != NbaOliConstants.OLI_FORMINSTANCE
						&& relation.getOriginatingObjectType() != NbaOliConstants.OLI_HHFAMILYINS) {
					return true;
				}
				System.out.println("Will Return Relation Object");
			}
		}

		return false;
	}

	// NBLXA-1997 Ends
	
	// New Method NBLXA-1988
	public JurisdictionApproval getValidApplicationJurisdiction(long applicationJurisdiction, List jurisdictionApprovals){		
		for (int i = 0; i < jurisdictionApprovals.size(); i++) {
			JurisdictionApproval jurisdictionApproval = (JurisdictionApproval)jurisdictionApprovals.get(i);
			if (jurisdictionApproval != null) {
				long jurisdiction = jurisdictionApproval.getJurisdiction();
				if (jurisdiction == applicationJurisdiction && isActiveInJurisdiction(jurisdictionApproval)) {		
					return jurisdictionApproval;
				}
			}
		}
		return null;
	}
	
	//NBLXA-1988 New Method
	protected int[] getMinMaxAgesForCovOption(UnderwritingClassProductExtension ext, long distributionChannel, LifeParticipant lifeParticipant) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("IN getMinMaxAgesForCovOption() distributionChannel == "+distributionChannel );
		}
		int ages[] = { Integer.MAX_VALUE, Integer.MIN_VALUE }; // Initialize to MAX_VALUE, MIN_VALUE so that any data from PPfL will overlay it
		AgeAmtProduct ageAmount = null;
		for (int i = 0; ext != null && i < ext.getAgeAmtProductCount(); i++) {
			ageAmount = ext.getAgeAmtProductAt(i);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("IN getMinMaxAges() ageAmount == "+ageAmount );
			}
			if (checkDistributionChannel(ageAmount, distributionChannel) && checkAgebasisType(ageAmount, lifeParticipant)) {// P2AXAL055
				if (ageAmount.hasMinAge() && ageAmount.getMinAge() < ages[0]) {// ALS3033
					ages[0] = ageAmount.getMinAge();
				}
				if (ageAmount.hasMaxAge() && ageAmount.getMaxAge() > ages[1]) {// ALS3033
					ages[1] = ageAmount.getMaxAge();
				}
			}
		}
		return ages;
	}
	
	
	// NBLXA1794 New Method
	protected Party getPartyByID(String partyID){
		if(partyID != null){
			return getNbaTXLife().getParty(partyID).getParty();
		}
		return null;
	}
	
	//NBLXA-2115 New Method
	public boolean hasApplicationJurisduction(UnderwritingClassProduct underwritingClassProduct) {
		long jurisdiction = getApplicationInfo() == null ? -1L : getApplicationInfo().getApplicationJurisdiction();
		boolean jurisdictionPassed = false;
		UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
		if (ext != null) {
			if (ext.getJurisdictionCCCount() > 0) {
				for (int i = 0; i < ext.getJurisdictionCCCount(); i++) {
					JurisdictionCC jurisdictionCC = (JurisdictionCC) ext.getJurisdictionCC().get(i);
					for (int j = 0; j < jurisdictionCC.getJurisdictionCount(); j++) {
						if (jurisdictionCC.getJurisdictionAt(j) == jurisdiction) {
							jurisdictionPassed = true;
							break;
						}
					}
				}
			} else if (ext.getJurisdictionApprovalCount() > 0) {
				for (int i = 0; i < ext.getJurisdictionApprovalCount(); i++) {
					JurisdictionApproval jurisdictionApproval = (JurisdictionApproval) ext.getJurisdictionApproval().get(i); // P2AXAL006
					if (jurisdictionApproval.getJurisdiction() == jurisdiction) {
						jurisdictionPassed = true;
						break;
					}
				}
			} 
		}
		return jurisdictionPassed;
	}

	// BEGIN NBLXA-2132
	public boolean isA4A5FormNumber() throws NbaBaseException {
		if (isA4A5Form == null) {
			isA4A5Form = Boolean.valueOf(NbaVPMSHelper.isA4A5FormNumber(getNbaTXLife()));
		}
		return isA4A5Form.booleanValue();
	}
	// END NBLXA-2132	
	
	/**
	 * Returns true if it is OwnerSuppForm
	 */
	// NBLXA-2132 New Method
	protected boolean isOwnerForm(FormInstance formInstance) {
		if (formInstance != null) {
			if (!formInstance.isActionDelete() && FORM_NAME_OWNER.equalsIgnoreCase(formInstance.getFormName())) {
				return true;
			}
		}
		return false;
	}
	
	//New Method: NBLXA-2303[NBLXA-2454]
	protected Date getLTCReqSourceDate() throws NbaBaseException {
		NbaDst dstWI = null;
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO.setWorkType(A_WT_REQUIREMENT);
		searchVO.setContractNumber(getHolding().getPolicy().getContractKey());
		NbaLob lob = new NbaLob();
		lob.setReqType((int) OLI_REQCODE_1009800084);
		searchVO.setNbaLob(lob);
		searchVO = WorkflowServiceHelper.lookupWork(getUserVO(), searchVO);
		List<NbaSearchResultVO> searchResult = searchVO.getSearchResults();
		if (searchResult != null && searchResult.size() > 0) {
			for (NbaSearchResultVO resultVO : searchResult) {
				if (resultVO != null) {
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					retOpt.setWorkItem(resultVO.getWorkItemID(), true);
					retOpt.requestSources();
					dstWI = WorkflowServiceHelper.retrieveWorkItem(getUserVO(), retOpt);
					if (!NbaUtils.isBlankOrNull(dstWI)) {
						List<WorkItemSource> ltcSources = dstWI.getNbaTransaction().getSources();
						for (WorkItemSource source: ltcSources) {
							NbaSource nbaSource = new NbaSource(source);
							if (nbaSource.getNbaLob().getReqType() == OLI_REQCODE_1009800084) {
								return nbaSource.getCreateDate();
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	//NBLXA-2303[NBLXA-2473]
	protected boolean isResubmitField(Object obj, String fieldName) {
		if (!NbaUtils.isBlankOrNull(obj) && !NbaUtils.isBlankOrNull(fieldName)) {
			NbaContractVO nbaContractVO = (NbaContractVO)obj;
			//Verify that this DOB is of Primary Insured
			if (BIRTH_DATE.equalsIgnoreCase(fieldName)) {
				String partyId = getIdOf(nbaContractVO);
				if (!NbaUtils.isBlankOrNull(NbaUtils.getPrimaryInsured(getNbaTXLife())) &&
						partyId.equalsIgnoreCase(NbaUtils.getPrimaryInsured(getNbaTXLife()).getRelatedObjectID())) {
					return true;
				}
			} else if (PAYMENT_AMT_STR.equalsIgnoreCase(fieldName) ||
					LifeCovOptTypeCode.equalsIgnoreCase(fieldName) ||
					LTCREPLACEMENT_IND_CODE.equalsIgnoreCase(fieldName)) {
				return true;
			}
		}
		return false;
	}
	
	//New Method: NBLXA-2303[NBLXA-2473]
	protected String getBaseCoverageId() {
		Coverage baseCoverage = NbaUtils.getBaseCoverage(getLife());
		if (!NbaUtils.isBlankOrNull(baseCoverage)) {
			return NbaUtils.getBaseCoverage(getLife()).getId();			
		}
		return null;
	}
}