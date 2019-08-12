package com.csc.fsg.nba.backendadapter.cyberlifeInforce;

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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.SystemAccess;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.AnnuityProduct;
import com.csc.fs.dataobject.accel.product.AnnuityProductExtension;
import com.csc.fs.dataobject.accel.product.CommOptionAvailable;
import com.csc.fs.dataobject.accel.product.CovOptionProduct;
import com.csc.fs.dataobject.accel.product.CovOptionProductExtension;
import com.csc.fs.dataobject.accel.product.CoverageProduct;
import com.csc.fs.dataobject.accel.product.CoverageProductExtension;
import com.csc.fs.dataobject.accel.product.FeatureOptProduct;
import com.csc.fs.dataobject.accel.product.FeatureOptProductExtension;
import com.csc.fs.dataobject.accel.product.FeatureProduct;
import com.csc.fs.dataobject.accel.product.FeatureProductExtension;
import com.csc.fs.dataobject.accel.product.Fee;
import com.csc.fs.dataobject.accel.product.FeeExtension;
import com.csc.fs.dataobject.accel.product.FeeTableRef;
/*import com.csc.fs.dataobject.accel.product.FinancialStatement; NBA237 deleted*/
import com.csc.fs.dataobject.accel.product.InvestProductInfo;
import com.csc.fs.dataobject.accel.product.InvestProductInfoExtension;
import com.csc.fs.dataobject.accel.product.LifeProduct;
import com.csc.fs.dataobject.accel.product.LifeProductExtension;
import com.csc.fs.dataobject.accel.product.LifeProductOrAnnuityProduct;
import com.csc.fs.dataobject.accel.product.LoanProvision;
import com.csc.fs.dataobject.accel.product.MinBalanceCalcRule;
import com.csc.fs.dataobject.accel.product.NonForProvision;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fs.dataobject.accel.product.PolicyProductInfo;
import com.csc.fs.dataobject.accel.product.PolicyProductInfoExtension;
import com.csc.fs.dataobject.accel.product.RateVariation;
import com.csc.fs.dataobject.accel.product.RateVariationByDuration;
import com.csc.fs.dataobject.accel.product.RateVariationExtension;
import com.csc.fs.dataobject.accel.product.SubstandardRisk;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.sa.accel.interaction.services.AccelCyberLifeDXEDataTransformationIntf;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberConstants;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberRequests;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaMortalityData;
import com.csc.fsg.nba.tableaccess.NbaPlansRidersData;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.AnnuityRiderExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ArrDestination;
import com.csc.fsg.nba.vo.txlife.ArrSource;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.ArrangementExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.MaxDisbursePctOrMaxDisburseAmt;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.PaymentFees;
import com.csc.fsg.nba.vo.txlife.PaymentModeAssembly;
import com.csc.fsg.nba.vo.txlife.PaymentModeMethods;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.PayoutExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TaxReporting;
import com.csc.fsg.nba.vo.txlife.TaxWithholding;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.tbf.xml.XmlValidationError;


/**
 * Class to parse inforce XML103 request and generate a IS00 dxe request to be sent to the host
 * <p>
 *  <b>Modifications:</b><br>
 *  <table border=0 cellspacing=5 cellpadding=5>
 *  <thead>
 *  <th align=left>Project</th>
 *  <th align=left>Release</th>
 *  <th align=left>Description</th>
 *  </thead>
 *  <tr><td>NBA076</td><td>Version 3</td><td>Initial Development</td></tr>
 *  <tr><td>SPR1659</td><td>Version 3</td><td>Corrected logic to send Commission Plan Code</td></tr>
 *  <tr><td>SPR1696</td><td>Version 3</td><td>Added logic to send Endorsement tags</td></tr>
 *  <tr><td>SPR1791</td><td>Version 4</td><td>FBRSTAT needs to always be sent as "no money" value "12"</td></tr>
 *  <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 *  <tr><td>SPR1633</td><td>Version 4</td><td>Policy product field translations</td></tr>
 *  <tr><td>SPR1731</td><td>Version 4</td><td>Multiple field updates</td></tr>
 *  <tr><td>LOG15</td><td>Version 4</td><td>FBRFLAGE bits 2 and 5 not getting set correctly on 01 segment</td></tr>
 *  <tr><td>SPR1878</td><td>Version 4</td><td>Corrected code to send right Initial premium amount DXE(FULFLGD6).</td></tr>
 *  <tr><td>SPR1879</td><td>Version 4</td><td>Changed code so that FSBBCOM dxe won't be sent for riders.</td></tr>
 *  <tr><td>SPR2035</td><td>Version 4</td><td>send default(deposite) Systemactic activity if it is not input.</td></tr>
 *  <tr><td>SPR1886</td><td>Version 4</td><td>Wrong value sent to the CyberLife Admin system in FCWCHGTP when CWA is sent along.</td></tr> 
 *  <tr><td>SPR1906</td><td>Version 4</td><td>General source code clean up</td></tr> 
 *  <tr><td>SPR1970</td><td>Version 4</td><td>Correct error in issue automatic process for Coverage SubstandardRatings.</td></tr>
 *  <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 *  <tr><td>SPR1999</td><td>Version 4</td><td>DXEs for Restriction and Special Handling are not carrying any values to CyberLife Admin System</td></tr>
 *  <tr><td>SPR1950</td><td>Version 4</td><td>Issue to Admin Adapter Fields Not Mapped</td></tr>
 *  <tr><td>SPR1946</td><td>Version 4</td><td>CoverageOption as a Percent Indicator Set Wrong in Contract Validation Insurance.P118 and P909</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Insured</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>SPR2050</td><td>Version 4</td><td>FCVPRIN and FCVINTRW are not set correctly for SST plan</td></tr>
 * <tr><td>SPR2062</td><td>Version 4</td><td>Value for FCVMAJLI is incorrect</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>SPR2082</td><td>Version 4</td><td>Benefit segments created incorrectly</td></tr>
 * <tr><td>SPR1186</td><td>Version 4</td><td>Incorrect table used for Roles for Annuities</td></tr>
 * <tr><td>SPR2095</td><td>Version 4</td><td>FBRDBIND not sent in IS00</td></tr>
 * <tr><td>SPR2003</td><td>Version 4</td><td>In SPDA policy, the values are not sent correctly in some DXEs.</td></tr>
 * <tr><td>SPR2114</td><td>Version 4</td><td>Add Loan Interest Type and Rate to IS00 transaction</td></tr>
 * <tr><td>SPR2115</td><td>Version 4</td><td>OLife values are not translated to BES values </td></tr>
 * <tr><td>SPR2134</td><td>Version 4</td><td>51 Segment Flag Byte B Set Incorrectly by Issue to Admin Adapter </td></tr>
 * <tr><td>SPR2141</td><td>Version 4</td><td>The DXE FBCCEXPR for Credit Care expiry date is not constructed correctly and hence issue transaction fails</td></tr>
 * <tr><td>SPR1707</td><td>Version 4</td><td>Severe errors are generated for Substandard Extras</td></tr> 
 * <tr><td>SPR2219</td><td>Version 5</td><td>BillingNotifyOptionType not being sent to backend correctly.</td></tr> 
 * <tr><td>SPR2289</td><td>Version 5</td><td>CLIF Some DXEs Null Values Sent 55 Seg and 66 Seg.</td></tr>
 * <tr><td>SPR2287</td><td>Version 5</td><td>Ownership details are not built in 89 segment of CyberLife Admin system</td></tr>
 * <tr><td>SPR2582</td><td>Version 5</td><td>CLIF Some DXE Missing - 55 Seg</td></tr>
 * <tr><td>SPR2229</td><td>Version 5</td><td>Add contract level benefit indicator to holding inquiry.</td></tr>
 * <tr><td>SPR2341</td><td>Version 5</td><td>Issues  with segment 04.</td></tr>
 * <tr><td>SPR2202</td><td>Version 5</td><td>For JL product, the FSBPIDNT DXE is carrying an incorrect value for Joint Benefit attached to the JL coverage.</td></tr>
 * <tr><td>SPR2200</td><td>Version 5</td><td>For Joint Life cases, rating added to the Coverage by selecting the Joint Insured check box is not carried to the Host.</td></tr>
 * <tr><td>SPR2319</td><td>Version 5</td><td>Issue to Admin the DXE - FCVNCTYP and FCVFLGB5 are not sent and the status of coverage is displayed as "Unknown" in 62D2 screen.</td></tr>
 * <tr><td>SPR2657</td><td>Version 5</td><td>DXE FBCCTYPE carries the Olife value instead of the BES value.</td></tr>
 * <tr><td>SPR2020</td><td>Version 5</td><td>Age Use Code of Age Information in 02 Segment is incorrectly populated as 0 for SST</td></tr>
 * <tr><td>SPR2099</td><td>Version 5</td><td>MEC processing is incorrect.</td></tr>
 * <tr><td>SPR2724</td><td>Version 5</td><td>FCVFACE Should Not Be Sent on IS00 Transaction</td></tr>
 * <tr><td>SPR2760</td><td>Version 5</td><td>Underwriter note text: DXE FNPVNOTE is truncated to 8 positions in the IS00 Issue to admin adapter.</td></tr>
 * <tr><td>SPR2953</td><td>Version 6</td><td>FLCLVERB is being sent twice for organization party type,once with correct BES value and once with Olife value.</td></tr>
 * <tr><td>SPR1929</td><td>Version 6</td><td>Allocation (57) segment constructed incorrectly.</td></tr>
 * <tr><td>SPR2970</td><td>Version 6</td><td>IFC (55) segment built incorrectly.</td></tr>
 * <tr><td>SPR3036</td><td>Version 6</td><td>Temporary Rating on Joint Insured for First to Die Joint Life is not being sent to backend system.</td></tr>
 * <tr><td>NBA143</td><td>version 6</td><td>Inherent benefits processing</td></tr>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>SPR3148</td><td>Version 6</td><td>Guaranteed Premium Period End Date Not Sent in IS00 for COI Rate Guarantee Rule 2 Causes Abend in Offline (CKDCARTH) </td></tr>
 * <tr><td>SPR3162</td><td>Version 6</td><td>FCVRDEXP Not Set Correctly and Should Only Be Sent for AP Riders  </td></tr>
 * <tr><td>SPR3161</td><td>Version 6</td><td>Issue to Admin Adapter Should Not Set FULFLGB0 - Causes Offline Abend   </td></tr>
 * <tr><td>SPR3191</td><td>Version 6</td><td>Required Fields Not Set for Guideline Calculations in Issue to Admin </td></tr>
 * <tr><td>SPR3197</td><td>Version 6</td><td>Adapter Sends Incorrect Advanced Product Control (FUL) Variables</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA140</td><td>Version 7</td><td>Indeterminate Premium Product</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * <tr><td>SPR3418</td><td>Version 7</td><td>Incorrect Value Sent in Issue to Admin (IS00) Transaction for Investment Method Type (FIFFITYP) Causes Offline Error Interest Rate Record Not Found</td></tr>
 * <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card Information is not saved</td></tr>
 * <tr><td>SPR2151</td><td>Version 8</td><td>Correct the Contract Validation edits and Adaptor logic for EFT, PAC and Credit Card Billing</td></tr>
 * <tr><td>SPR1738</td><td>AXA Life Phase 1</td><td>Substandard Rating Should be Arranged Correctly In ACORD XML Message Out from nbA</td></tr>
 * <tr><td>NBA234</td><td>Version 8</td><td>ACORD Transformation project</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * </table>
 * <p>
 *  @author CSC FSG Developer
 * @version 8.0.0
 *  @since New Business Accelerator - Version 3
 */
// NBA195 signature modified
public class NbaCyberInforceAppAdapter extends NbaCyberRequests implements NbaCyberConstants, NbaCyberInforceConstants, AccelCyberLifeDXEDataTransformationIntf {
	
	private static NbaLogger logger = null;
	
	protected String hostRequest = null;
	public static final long TABLEREF_TABLETYPE_62 = 62; //SPR3162
	public static final String TRANS_INFORCE_APP = "6PCYWHATTODO=IS00,2;";
	public static final String TRANS_NEWAPP_CWA = "WHATTODO=4000,1;";
	public static final String POL_AUTO = "AUTO";
	public static final String CYB_INSURED = "00";
	public static final int UNDERWRITER_NOTE = 12;
	protected static final String ANNUITY_PROD = "F";	//NBA104
	protected static final String LIFE_PROD = "L";	//NBA104
	public static final String RESOLVE = "RESOLVE"; // NBA195

	// SPR3290 code deleted

	// Class Variables
	private String planType = "";
	private boolean isTrad = false;
	private boolean isAP = false;
	
	// Class Variables
	private String productCode = "";
	private String benefitCode = "";
	private Date termDate = null; 
	// SPR3290 code deleted
	private int FAU_Seq;
	// SPR3290 code deleted
	private Date matDate;
	private String servicingAgent;
	private NbaPlansRidersData[] plansRidersData;
	private NbaMortalityData[] mortalityData; //SPR1633
	private boolean classSeriesFound = false;
	// NBA104 deleted code
	// SPR3290 code deleted
	private long valuationFreq = 0;

	// DataExchange 
	private String addressDXE;
	private String writingAgentDXE;
	private String servicingAgentDXE;
	private String servicingAgencyDXE;
	private String beneficiaryDXE;
	private String benefitDXE;
	private String payoutDXE;
	private String basicDXE;
	private String APDXE; 
	// NBA104 code deleted
	protected AccelProduct nbaProduct; //NBA104, NBA237
	protected Policy policy; 	//NBA104
	protected Holding holding;	//NBA104
	protected NbaTXLife nbaTXLife;	//NBA104	
	private HashMap lifeParticipantRoleTypeMap=null;//NBA111 //LifeParticipantRoleType HashMap.Keeps count of lifeparticipant:role code combination.
	
	/**
	 * NbaCyberInforceAppAdapter constructor 
	 */
	public NbaCyberInforceAppAdapter() {
		super();
	}
	/**
	 * Main external method used to create the DXE for the inforce IS00 transaction
	 * @param txlife com.csc.fsg.nba.vo.txlife.TXLife
	 * @return java.lang.String The IS00 DXE stream
	 * @exception throws NbaBaseException
	 */
	public String createIS00Request(TXLife tXLife) throws NbaBaseException {	//NBA104 changed method name

		try {
			setNbaTXLife(new NbaTXLife()); //NBA104 
			getNbaTXLife().setTXLife(tXLife);	//NBA104 
			tblMap = new HashMap();
			partyMap = new HashMap();
			OLifE olife = nbaTXLife.getOLifE();	//NBA104 
			setNbaProduct(new AccelProduct(olife.getPolicyProduct())); //NBA237
			setHolding(nbaTXLife.getPrimaryHolding()); //NBA104
			setPolicy(holding.getPolicy());	//NBA104
			// NBA104 code deleted
			plansRidersData = null;
			FAU_Seq = 0; 

			if (nbaTXLife.isAnnuity()) {	//NBA104 
				setPlanType(ANNUITY_PROD);	//NBA104 
			} else if (nbaTXLife.isLife()) {	//NBA104 
				setPlanType(LIFE_PROD);	//NBA104 
			}

			isTrad= NbaUtils.isTraditional(getPolicy()); //NBA077 NBA104
			isAP = NbaUtils.isAdvProd(getPolicy()); //NBA077 NBA104

			// NBA104 deleted code

			// Initialize request string
			hostRequest = new String();

			// Initialize dxe holders
			writingAgentDXE = "";
			servicingAgentDXE = "";
			servicingAgencyDXE = "";
			benefitDXE = "";
			beneficiaryDXE = "";
			basicDXE = ""; 
			APDXE = ""; 
			// NBA104 code deleted

			servicingAgent = "";
			
			// Company Code
			compCode = getPolicy().getCarrierCode(); //NBA104
			// SPR3290 code deleted
			hostRequest = TRANS_INFORCE_APP + COMP_CODE + "=" + compCode + ";" + POL_NUM + "=" + getPolicy().getPolNumber() + ";"+"AEPSTPDS=2;"; //NBA104
			hostRequest = hostRequest + getPolicyLocation(); //NBA133 Required for backend calculations and print
			String partyRequest = createParties(olife);
			hostRequest = hostRequest + createBasicInfo(olife); //create RC and 01 segments
			hostRequest = hostRequest + createLife(olife);//create 02,03,04, and 10 segments
			hostRequest = hostRequest + createBillingInfo(getHolding()); //create 33 and 35 segment //NBA104
			hostRequest = hostRequest + createCWA(getPolicy());//create 47 segment used to build 69 segments //NBA104
			hostRequest = hostRequest + createTargetInfo(); //create 58 segment	//NBA104 
			if (isAP)
				hostRequest = hostRequest + createAPInfo(getPolicy(),olife);  // create 66 segment //NBA104
			else
				hostRequest = hostRequest + createTradInfo(getPolicy()); // create 51 segment //NBA104
			if (getHolding().getArrangementCount() > 0) //NBA104
				hostRequest = hostRequest + createAutomaticTransactions(olife);	
			if (getHolding().hasInvestment()) //NBA104
				hostRequest = hostRequest + createInvestment(olife); //create 55 and 57 segments
			if (isAP)
				hostRequest = hostRequest + createPolicyProduct(olife,getPolicy().getProductCode(), 7,""); // create 56 segment if data is present //NBA104
			if (isLife()) { 	//NBA104
				hostRequest = hostRequest + createGuidelineInfo();	//NBA104
			}				
			hostRequest = hostRequest + createNotes(getHolding()); //create 72 segment //NBA104
			hostRequest = hostRequest + createTaxInfo(getHolding());  //NBA104
			hostRequest = hostRequest + createEndorsementInfo(olife); //create 82 segment //SPR1696 
			hostRequest = hostRequest + partyRequest; // create 89, 90, 95 and 96 segments
			hostRequest = hostRequest + benefitDXE + beneficiaryDXE + writingAgentDXE + servicingAgentDXE + servicingAgencyDXE;
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
		return hostRequest;

	}
	/**
	 * Create address (90 segment) data for CyberLife host
	 * @param party The current party XML object
	 * @param sPersonId The current person id
	 * @return java.lang.String The address DXE information 
	 * @exception throws NbaBaseException
	 */
	protected String createAddressFromParty(Party party, String sPersonId) throws NbaBaseException {
		Address partyAddress = null;
		String addressDXE = "";
		// SPR3290 code deleted
		try {
			// Address Info
			for (int i = 0; i < party.getAddressCount(); i++) {
				partyAddress = party.getAddressAt(i);
				// Address ID
				addressDXE = addressDXE + formatDataExchange(sPersonId, ADDR_PERS_SEQ_ID, CT_CHAR, 10);

				addressDXE = addressDXE + formatDataExchange(String.valueOf(i + 1), FNAOCCUR, CT_CHAR, 10);

				// Address Effective Date
				if (partyAddress.hasStartDate())
					addressDXE = addressDXE + createDataExchange(partyAddress.getStartDate(), STARTDATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);

				// Address Type
				if (partyAddress.hasAddressTypeCode())
					addressDXE =
						addressDXE
							+ createDataExchange(partyAddress.getAddressTypeCode(), ADDR_TYPE, NbaTableConstants.OLI_LU_ADTYPE, CYBTBL_UCT, CT_CHAR, 5);

				// Country
				if (partyAddress.hasAddressCountryTC()) 
					addressDXE =
						addressDXE
							+ createDataExchange(partyAddress.getAddressCountryTC(), COUNTRY, NbaTableConstants.OLI_LU_NATION, CYBTBL_UCT, CT_CHAR, 46);
				
				// Addendum
				
				if (partyAddress.hasAttentionLine())
					addressDXE =
						addressDXE + createDataExchange(partyAddress.getAttentionLine(), ATTEN_LINE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 46);

				// Address Line 1
				if (partyAddress.hasLine1())
					addressDXE = addressDXE + createDataExchange(partyAddress.getLine1(), ADDR_LINE1, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 46);

				// Address Line 2
				if (partyAddress.hasLine2()) {
					if (partyAddress.hasLine3())
						addressDXE =
							addressDXE
								+ createDataExchange(
									partyAddress.getLine2() + " " + partyAddress.getLine3(),
									ADDR_LINE2,
									CYBTRANS_NONE,
									CYBTBL_NONE,
									CT_CHAR,
									46);
					else
						addressDXE = addressDXE + createDataExchange(partyAddress.getLine2(), ADDR_LINE2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 46);
				}

				// City
				if (partyAddress.hasCity())
					addressDXE = addressDXE + createDataExchange(partyAddress.getCity(), CITY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 31);

				// State
				// Typecode  OLI_LU_STATE
				if (partyAddress.hasAddressStateTC()) //NBA093
					addressDXE = addressDXE + createDataExchange(partyAddress.getAddressStateTC(), STATE, "NBA_STATES", CYBTBL_STATE, CT_CHAR, 3);
				//NBA093

				// Zip/Postal Code
				if (partyAddress.hasZip())
					addressDXE =
						addressDXE + createDataExchange(formatCyberZipCode(partyAddress.getZip()), ZIPCODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
			}
			// End Address Info
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ADDRESSES, e);
		}
		return addressDXE;

	}
	/**
	 * Create Annuity DXE tags 
	 * 
	 * @param olife The OLifE XML object
	 * @param annuity The Annuity XML object
	 * @param policy The Policy XML object
	 * @return java.lang.String The Annuity DXE infromation
	 * @exception throws NbaBaseException
	 */
	protected String createAnnuity(OLifE olife,Annuity annuity, Policy policy) throws NbaBaseException {
		String AnnuDXE = "";
		CovOption covoption = null;
		AnnuityExtension annuityEx = null;
		AnnuityRiderExtension riderEx = null;
		Payout payout = null;
		PayoutExtension payoutEx = null;
		Participant participant = null;
		// SPR3290 code deleted
		OLifEExtension olifeEx = null;
		Rider rider = null;
		payoutDXE = "";
		// NBA104 code deleted
		Date issueDate = null;

		try {
			if (annuity.getPayoutCount() > 0) {
				payout = annuity.getPayoutAt(0);
				if (payout.getParticipantCount() > 0) {
					participant = payout.getParticipantAt(0);
				}
				for (int i = 0; i < payout.getOLifEExtensionCount(); i++) {
					olifeEx = payout.getOLifEExtensionAt(i);
					if (olifeEx.isPayoutExtension()) {
						payoutEx = olifeEx.getPayoutExtension();
						break;
					}
				}
			}
			annuityEx = NbaUtils.getFirstAnnuityExtension(annuity); //SPR3290
			// SPR3290 code deleted

			//  (Holding.Policy.Annuity) DXE: FCVPHASE
			AnnuDXE = AnnuDXE + createDataExchange(annuity.getAnnuityKey(), COV_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// NBA104 code deleted			
			// Type of relation the party has to the annuity. (Holding.Policy.Annuity.Payout) DXE: FCVPIDNT
			// Typecode UCT (CLPCTB11)  OLI_LU_PARTICROLE
			String relRoleCode = getCyberValue(participant.getParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE_F, compCode, DEFAULT_COVID);
			AnnuDXE =
				AnnuDXE
					+ createDataExchange(getPartyId(participant.getPartyID(), relRoleCode), COVERAGE_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// NBA104 code deleted
			//DXE FCVFLAGA
			AnnuDXE = AnnuDXE + createDataExchange("1XXXXXXX", INF_COV_FLAG_A, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
				
			//DXE:FCVPROD
			AnnuDXE = AnnuDXE + createDataExchange(policy.getProductType(), COV_TYPE_CODE_1, "OLI_LU_POLPROD", CYBTBL_UCT, CT_CHAR, 1);
			// Policy / coverage / option plan code.  Assigned by the Carrier administration system -  OR as issued by carrier -  (Holding.Policy) DXE: FCVPDSKY
			// String UCT (PLN_MNE)  nbA Table = NBA_PLANS
			productCode = policy.getProductCode();
			if (validPlanRider(productCode)){
				AnnuDXE = AnnuDXE + createDataExchange(policy.getProductCode(), COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
				// NBA104 code deleted
				//DXE: FCVRATBK 
				AnnuDXE = AnnuDXE + createDataExchange(policy.getProductCode(), INF_COV_RATE_BOOK, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
			}else
				AnnuDXE = AnnuDXE + createDataExchange("", COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
				
			// Qualification Type (Holding.Policy.Annuity) DXE: FCVFQTYP
			// Typecode UCT (CLAPTB08)  OLI_LU_QUALPLAN
			if (annuity.hasQualPlanType()) {
				AnnuDXE =
					AnnuDXE
						+ createDataExchange(annuity.getQualPlanType(), ANNU_QUAL_TYPE, NbaTableConstants.OLI_LU_QUALPLAN, CYBTBL_UCT, CT_CHAR, 1);
				// SPR1731 - if non-qualified plan, initialize these fields to zero		
				if (annuity.getQualPlanType() == 1) {
					AnnuDXE =
						AnnuDXE
							+ createDataExchange("0","FCVRMDPC",CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
					AnnuDXE =
						AnnuDXE
							+ createDataExchange("0","FCVRMDCC",CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				}			
				// End SPR1731
			}
			// NBA104 deleted code
			//DXE: FCVSEX
			AnnuDXE =
					AnnuDXE
						+ createDataExchange(getSex(participant.getPartyID(),olife),INF_COV_SEX,CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
			// NBA104 code deleted		
			//The following  coverage dates are needed for a record to be build on CyberLife
			//Issue date DXE:FCVISSDF
			if (policy.hasEffDate()){
				issueDate = policy.getEffDate();
			}else{
				issueDate = new Date();
			} 
			AnnuDXE = AnnuDXE + createDataExchange(issueDate,COV_EFF_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			//Check and make sure that there is a good maturity date avaiable. 
			if (participant != null){				 
				matDate = policy.getTermDate();	//SPR1986
				//DXE: FCVPAYDF
				AnnuDXE = AnnuDXE + createDataExchange(matDate,INF_COV_PAYUP_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				//DXE: FCVMATDF		
				AnnuDXE = AnnuDXE + createDataExchange(matDate,INF_COV_MAT_EXP_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				
				// SPR3290 code deleted
				
				//Age of participant when coverage was issued. DXE:FCVAGEIS - For specific person
				// SPR3290 code deleted
				AnnuDXE = AnnuDXE + createDataExchange(participant.getIssueAge(),COV_ISSUE_AGE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				//DXE: FCVAGADM 
				//Age Admitted. If FCVAGEIS is null then FCVAGADM should be set to 2 for not admitted
				if (participant.hasIssueAge())
					AnnuDXE = AnnuDXE + createDataExchange("0",INF_COV_AGE_ADMITTED,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				else
					AnnuDXE = AnnuDXE + createDataExchange("2",INF_COV_AGE_ADMITTED,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			}
			//True Age DXE: FCVAGETR
				AnnuDXE =
					AnnuDXE
						+ createDataExchange(getAge(participant.getPartyID(),olife),INF_COV_TRUE_AGE,CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);

			//Status of base plan.   DXE:FCVNCTYP + FCVFLGB5
			//begin SPR2319
			if ((annuityEx != null) && (annuityEx.hasAnnuityStatus())) { //NBA234
				String status = getCyberValue(annuityEx.getStatus(), NbaTableConstants.NBA_COVERAGE_STATUS, CYBTBL_UCT, compCode, DEFAULT_COVID); //NBA234
				if (status.length() < 2) { //if status BES value less than 2 then invalid status is 21
					status = INVALID_COVERAGE_STATUS;
				}
				AnnuDXE = AnnuDXE + createDataExchange(status.substring(0, 1), COV_STATUS, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				AnnuDXE = AnnuDXE + createDataExchange(status.substring(1, 2), COV_STATUS_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			}
			//end SPR2319
			
			//SPR1731
			if (annuity.getAnnuityKey().equals("1"))
				// FCVFLGD0
				AnnuDXE = AnnuDXE + createDataExchange("1",COV_IND_CODE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			//End SPR1731
			//Class, Series and SubSeries must be present for the a good inforce contract to go onto CyberLife
			if ((annuityEx != null) && (annuityEx.hasValuationClassType())){
				classSeriesFound = true;
				if (annuityEx.hasValuationClassType()){
					AnnuDXE = AnnuDXE + createDataExchange(annuityEx.getValuationClassType(),INF_COV_CLASS,"OLI_LU_VALCLASS",CYBTBL_UCT,CT_DEFAULT,0);
				}
				if (annuityEx.hasValuationBaseSeries()){
					AnnuDXE = AnnuDXE + createDataExchange(annuityEx.getValuationBaseSeries(),INF_COV_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				}
				if (annuityEx.hasValuationSubSeries()){
					AnnuDXE = AnnuDXE + createDataExchange(annuityEx.getValuationSubSeries(),INF_COV_SUB_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				}
			}
			if (policy.hasTermDate()){	//SPR1986
				// SPR1986 code deleted
					AnnuDXE = AnnuDXE + createDataExchange(policy.getTermDate(),COV_TERM_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0); //SPR1986
				// SPR1986 code deleted
			}
			// NBA104 deleted code
		
			// Annuity Settlement Option (Holding.Policy.Annuity.Payout) DXE: FCVFSOPT
			// Typecode UCT (CLAPTB50)  OLI_LU_INCOPTION
			if (payout != null)
				if (payout.hasIncomeOption())
					AnnuDXE =
						AnnuDXE
							+ createDataExchange(
								payout.getIncomeOption(),
								ANNU_PAYOUT_SETTLEMENT_OPT,
								NbaTableConstants.OLI_LU_INCOPTION,
								CYBTBL_UCT,
								CT_CHAR,
								3);
			// RMD Calculation Indicator (Initial or Recalculate) (Holding.Policy.Annuity) DXE: FDENRMDC
			// VT_I4 (Typecode) UCT (CLDET02B)  OLIEXT_LU_RMDCALCIND DXE: FDENRMDC
			//SPR1659
			if (annuityEx != null){
				if (annuityEx.hasRMDCalcInd())
					AnnuDXE =
						AnnuDXE
							+ createDataExchange(
								annuityEx.getRMDCalcInd(),
								ANNU_RMD_CALC_IND_INIT,
								NbaTableConstants.OLIEXT_LU_RMDCALCIND,
								CYBTBL_UCT,
								CT_CHAR,
								9);
				if (annuityEx.hasCommissionPlanCode()){
					AnnuDXE = AnnuDXE + createDataExchange(annuityEx.getCommissionPlanCode(),INF_COV_COMM_CODE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);			
				}
				
			}
			//end SPR1659
			/*
			 * Building 53 segment 
			 */
			//if (payout != null) {
			//NBA077 - Follwoing payout section is creating 53 segment which is not supported by nbA. 
			if(false){ //NBA077
				//transaction type - set to W DXE:FAUTXNTP
				payoutDXE = payoutDXE + createDataExchange("W", INF_AUTO_TRANS_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				//Auto trans seq DXE: FAUATSEQ
				payoutDXE = payoutDXE + createDataExchange(++FAU_Seq, INF_AUTO_TRANS_SEQ, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				//Exclusion Ratio (Holding.Policy.Annuity.Payout) DXE: FAUEXRAT
				if (payout.hasExclusionRatio())
					payoutDXE = payoutDXE + createDataExchange(payout.getExclusionRatio(), INF_PAYOUT_EXCLUSION_RATIO, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				// Certain Period (Holding.Policy.Annuity.Payout) DXE: FAUCERTP
				if (payout.hasNumModalPayouts())
					payoutDXE = payoutDXE + createDataExchange(payout.getNumModalPayouts(), INF_PAYOUT_NUM_PAYMENTS, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);			
				// Payout Amount (Holding.Policy.Annuity.Payout) DXE: FAUAWDAM
				if (payout.hasPayoutAmt())
					payoutDXE = payoutDXE + createDataExchange(payout.getPayoutAmt(), INF_PAYOUT_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//Payout Percent (Holding.Policy.annuity.Payout) DXE:FAUAWDPC
				if (payout.hasPayoutPct())
					payoutDXE = payoutDXE + createDataExchange(payout.getPayoutPct(), INF_PAYOUT_PERCENT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//Payout Start Date (Holding.Policy.Annuity.Payout) DXE: FAUSTDTE
				if (payout.hasStartDate())
					payoutDXE = payoutDXE + createDataExchange(payout.getStartDate(), INF_PAYOUT_START_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12);
				//Payout form (Holding.Policy.Annuity.Payout) DXE: FAUPAYPT
				if (payout.hasPayoutForm())
					payoutDXE = payoutDXE + createDataExchange(payout.getPayoutForm(), INF_PAYOUT_FORM, "OLI_LU_PAYMENTFORM", CYBTBL_UCT, CT_DATE, 12);
				//Payout End Date (Holding.Policy.Annuity.Payout) DXE: FAUCSDTE
				if (payout.hasPayoutEndDate())
					payoutDXE = payoutDXE + createDataExchange(payout.getPayoutEndDate(), INF_PAYOUT_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12);
				// PayoutMode (Holding.Policy.Annuity.Payout) DXE: FAUFREQ
				if (payout.hasPayoutMode())
					payoutDXE = payoutDXE + createDataExchange(payout.getPayoutMode(), INF_PAYOUT_MODE, "OLI_LU_PAYMODE", CYBTBL_UCT, CT_DEFAULT, 0);			
				// Primary Percent (Holding.Policy.Annuity.Payout) DXE: FAUJSPC1
				if (payout.hasPrimaryReductionPct())
					payoutDXE = payoutDXE + createDataExchange(payout.getPrimaryReductionPct(), INF_PAYOUT_PRIMARY_REDUCTION_PERCENT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				// Secondary Percent (Holding.Policy.Annuity.Payout) DXE: FAUJSPC2
				if (payout.hasSecondaryReductionPct())
					payoutDXE = payoutDXE + createDataExchange(payout.getSecondaryReductionPct(), INF_PAYOUT_SEC_REDUCTION_PERCENT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				// Adjusted Investment (Holding.Policy.Annuity.Payout) DXE: FAUADJIV		
				if (payout.hasAdjInvestedAmt())
					payoutDXE = payoutDXE + createDataExchange(payout.getAdjInvestedAmt(), INF_PAYOUT_ADJ_INVESTED_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				//Assumed Int. Rate  (Holding.Policy.Annuity.Payout) DXE: FAUASMIR		
				if (payout.hasAssumedInterestRate()) 
					payoutDXE = payoutDXE + createDataExchange(payout.getAssumedInterestRate(), INF_PAYOUT_ASSUMED_INTEREST_RATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				//Exclusion Amt  (Holding.Policy.Annuity.Payout) DXE: FAUEXAMT		
				if (payoutEx != null && payoutEx.hasExclusionAmt()) 
					payoutDXE = payoutDXE + createDataExchange(payoutEx.getExclusionAmt(), INF_PAYOUT_EXCLUSION_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				
			}
			AnnuDXE = AnnuDXE + createPolicyProduct(olife,productCode,2,"");
			//Default values from the middle tier spreadsheet for class and series
			//Once all plans have policy product data available this should not be needed in future revisions
			if (!classSeriesFound){
				AnnuDXE = AnnuDXE + createDataExchange("9",INF_COV_CLASS,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				AnnuDXE = AnnuDXE + createDataExchange("",INF_COV_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				AnnuDXE = AnnuDXE + createDataExchange("",INF_COV_SUB_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			}
			
			/*
			 *  Annuity rider section
			 * 
			 */
			 
			for (int i = 0; i < annuity.getRiderCount(); i++) {
				rider = annuity.getRiderAt(i);
				Participant riderParticipant = null;

				if (rider.getParticipantCount() > 0) {  
					riderParticipant = rider.getParticipantAt(0); 
				}
				
				for (int j = 0; j < rider.getOLifEExtensionCount(); j++) {
					olifeEx = rider.getOLifEExtensionAt(j);
					if (olifeEx.isAnnuityRiderExtension()) {
						riderEx = olifeEx.getAnnuityRiderExtension();
						break;
					}
				}
				// Phase Code (Holding.Policy.Annuity) DXE: FCVPHASE
				AnnuDXE = AnnuDXE + createDataExchange(rider.getRiderKey(), COV_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);

				// Type of relation the party has to the annuity. (Holding.Policy.Annuity.Payout) DXE: FCVPIDNT
				// Typecode UCT (CLPCTB11)  OLI_LU_PARTICROLE
				if (riderParticipant != null) {
					relRoleCode =
						getCyberValue(riderParticipant.getParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
					//This DXE creates a link between the party and the coverage within CyberLife
					AnnuDXE = AnnuDXE + createDataExchange(getPartyId(riderParticipant.getPartyID(), relRoleCode),ANNU_RID_KEY,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
					AnnuDXE = AnnuDXE + createDataExchange(getPartyId(riderParticipant.getPartyID(), relRoleCode),COVERAGE_ID,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
					AnnuDXE = AnnuDXE + createDataExchange(riderParticipant.getIssueAge(), COV_ISSUE_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					AnnuDXE = AnnuDXE + createDataExchange(riderParticipant.getIssueGender(), INF_COV_SEX, "OLI_LU_GENDER", CYBTBL_UCT, CT_DEFAULT, 0);
					
				}

				// Administration provided code to identify rider (Holding.Policy.Annuity) DXE: FCVPDSKY
				if (rider.hasRiderCode()) {
					if (validPlanRider(rider.getRiderCode()))
						AnnuDXE = AnnuDXE + createDataExchange(rider.getRiderCode(), COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					else
						AnnuDXE = AnnuDXE + createDataExchange("", COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}

				//SPR2724 code deleted
				
				AnnuDXE = AnnuDXE + createDataExchange(rider.getRiderTypeCode(), COV_TYPE_CODE_1, "OLI_LU_RIDERTYPE", CYBTBL_UCT, CT_DEFAULT, 1);
				AnnuDXE = AnnuDXE + createDataExchange(rider.getEffDate(), COV_EFF_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				AnnuDXE = AnnuDXE + createDataExchange(rider.getTermDate(), COV_TERM_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				AnnuDXE = AnnuDXE + createDataExchange(rider.getTermDate(), INF_COV_MAT_EXP_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				
				AnnuDXE = AnnuDXE + createPolicyProduct(olife,productCode,2,"");
				// Rider number of Units (Holding.Policy.Annuity.Rider) DXE: FCVUNITS
				if (rider.hasNumberOfUnits())
					AnnuDXE = AnnuDXE + createDataExchange(rider.getNumberOfUnits(), COV_CURR_AMT_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				// NBA104 deleted code
		
				//FCVPAYDF
				if (riderEx!= null) {
					if (riderEx.hasPayUpDate())
						AnnuDXE = AnnuDXE + createDataExchange(riderEx.getPayUpDate(), INF_COV_PAYUP_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					else
						AnnuDXE = AnnuDXE + createDataExchange(rider.getTermDate(), INF_COV_PAYUP_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					AnnuDXE = AnnuDXE + createDataExchange(riderEx.getLivesType(), COV_LIVES_TYPE, "OLI_LU_LIVESTYPE", CYBTBL_UCT, CT_DEFAULT, 0);
					AnnuDXE = AnnuDXE + createDataExchange(riderEx.getCommissionPlanCode(), INF_COV_COMM_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					AnnuDXE = AnnuDXE + createDataExchange(riderEx.getValuationClassType(), INF_COV_CLASS, "OLI_LU_VALCLASS",CYBTBL_UCT, CT_DEFAULT, 0);
					AnnuDXE = AnnuDXE + createDataExchange(riderEx.getValuationBaseSeries(), INF_COV_SERIES, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					AnnuDXE = AnnuDXE + createDataExchange(riderEx.getValuationSubSeries(), INF_COV_SUB_SERIES, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					
				}

				for (int m = 0; m < rider.getCovOptionCount(); m++) { 
					covoption = rider.getCovOptionAt(m);

					// Coverage Option Key (Holding.Policy.Life.Coverage.CovOption) DXE: FSBBPHS
					AnnuDXE =
						AnnuDXE + createDataExchange(covoption.getCovOptionKey(), COV_OPT_BENE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);

					// Type of benefit (Holding.Policy.Annuiity.AnnuityExtension.CovOption) DXE: FSBBENEF
					// Typecode UCT (CLUDT135)  OLI_LU_OPTTYPE
					AnnuDXE =
						AnnuDXE + createDataExchange(covoption.getLifeCovOptTypeCode(),COV_OPT_TYPE,NbaTableConstants.OLI_LU_OPTTYPE,CYBTBL_UCT,CT_CHAR,3);
				}	
			}

			AnnuDXE = AnnuDXE + payoutDXE;

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ANNUITY, e);
		}

		return AnnuDXE ; //NBA104
		
	}
	/**
	 * Create beneficiary DXE
	 * 
	 * @param party The Party XML object
	 * @param relation The current Relation XML object
	 * @param roleTypeMap A hash map for the role codes
	 * @return java.lang.String The beneficiary DXE information
	 * @exception throws NbaBaseException
	 */
	protected String createBeneficiary(Party party, Relation relation, HashMap roleTypeMap) throws NbaBaseException {
		OLifEExtension olifeEx;
		// SPR3290 code deleted
		String sPersonId;
		// SPR3290 code deleted
		RelationExtension relationEx;
		String beneDXE = "";
		double dIntPer = 100.0;

		try {

			String relRoleCode =
				getCyberValue(Long.toString(relation.getRelationRoleCode()), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE, compCode, DEFAULT_COVID);

			sPersonId = getPartyId(party.getId(), relRoleCode);

			// Handle Beneficiary Information if rolecode is 34, 35 or 55
			if (relation.getRelationRoleCode() == OLI_REL_BENEFICIARY
				|| relation.getRelationRoleCode() == OLI_REL_CONTGNTBENE
				|| relation.getRelationRoleCode() == OLI_REL_ASSIGNBENE) { 
				for (int i = 0; i < relation.getOLifEExtensionCount(); i++) {
					olifeEx = relation.getOLifEExtensionAt(i);
					if (olifeEx.isRelationExtension()) {
						relationEx = olifeEx.getRelationExtension();
						
						if (relation.hasBeneficiaryDesignation() 
						&& relationEx.hasBeneficiaryDistributionOption()) {

							beneDXE = beneDXE + formatDataExchange(sPersonId, BENE_SUB_ID, CT_CHAR, 12);

							// Beneficiary Type (Prim., Contingent, Assignee) (Relation) DXE: FBDBTYPE
							//  UCT (CLPCTB17)  OLI_LU_PARTICROLE nbA table = NBA_Beneficiary_Type
							beneDXE = beneDXE + createDataExchange(relation.getRelationRoleCode(),
									BENEF_TYPE, NbaTableConstants.NBA_BENEFICIARY_TYPE, CYBTBL_UCT, CT_CHAR, 1);

							// If the following fields required to build the 96 segment are not present, DO NOT send this information to the host:FBDBDIST, FBDBNPCT, and FBDBNAMT,  - Description code to further define the role of the relationship I.e., the relationrolecode would contain a value of spouse the relation description would contain a value for husband or wife. (Relation) 
							// DXE: FBDBNREL
							// VT_I4 UCT (CLUDT108)  OLI_LU_BENEDESIGNATION
							if (relation.hasBeneficiaryDesignation()) {
								beneDXE =
									beneDXE
										+ createDataExchange(
											relation.getBeneficiaryDesignation(),
											BENEF_REL_ROLE,
											NbaTableConstants.OLI_LU_BENEDESIGNATION,
											CYBTBL_UCT,
											CT_CHAR,
											1);
							}

							// Amount of distribution for beneficiary (Relation) DXE: FBDBNAMT
							if (relationEx.hasInterestAmount())
								beneDXE =
									beneDXE
										+ createDataExchange(
											relationEx.getInterestAmount(),
											BENE_INTEREST_AMT,
											CYBTRANS_NONE,
											CYBTBL_NONE,
											CT_DOUBLE,
											0);

							// If the interest percent for a beneficiary equals 100% the Beneficiary Distribution Option should be set to Balance and the Interest Percent amount should not be sent to the host. - Percentage of distribution for beneficiary (Relation) DXE: FBDBNPCT
							if (relation.hasInterestPercent()) {
								dIntPer = relation.getInterestPercent();
								if (dIntPer != 100.0)
									beneDXE =
										beneDXE
											+ createDataExchange(
												relation.getInterestPercent(),
												PCTG_DIST_FOR_BENEF,
												CYBTRANS_NONE,
												CYBTBL_NONE,
												CT_DOUBLE,
												0);
							}

							// Distribution type (Relation) DXE: FBDBDIST
							//  UCT (CLUDT107)  OLI_LU_DISTOPTION
							if (relationEx.hasBeneficiaryDistributionOption())
								beneDXE =
									beneDXE
										+ createDataExchange(
											relationEx.getBeneficiaryDistributionOption(),
											DIST_TYPE,
											NbaTableConstants.OLI_LU_DISTOPTION,
											CYBTBL_UCT,
											CT_CHAR,
											1);

							// Joint Beneficiary Indicator (Relation) DXE: FLCFLGA2
							// VT_I4 (Typecode)  OLIEXT_LU_BOOLEAN Current Extension
							if (relationEx.hasJointBenInd())
								beneDXE =
									beneDXE + createDataExchange(relationEx.getJointBenInd(), LC_FLAGA_2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
						}
					}
				}

			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_PARTIES, e);
		}
		return beneDXE;

	}
	
	/**
	 * Create basic segment (01) for inforce build.
	 *
	 * @param olife The OLifE XML object
	 * @return java.lang.String The basic segment DXE information
	 */
	protected String createBasicInfo(OLifE olife) { 
		// SPR3290 code deleted
		HoldingExtension holdingEx = null; 
		Policy policy = null; 
		ApplicationInfo applicationinfo = null;
		ApplicationInfoExtension applicationinfoEx = null;
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null; 
		Life life = null;
		LifeExtension lifeEx = null;
		LifeProduct lifeProduct = null; //SPR2099
		LifeProductExtension lifeProductExtension = null; //SPR2099
		Annuity annuity = null;
		PolicyExtension policyEx = null;
		String recControlDXE = "";
		// SPR3290 code deleted
		
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); 
		if (holding != null) {
			policy = holding.getPolicy();
			holdingEx = NbaUtils.getFirstHoldingExtension(holding); //SPR3290
		}
		
		if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) { 
			lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); 
			if (lifeOrAnnuityOrDisabilityHealth.isLife()) {
				life = lifeOrAnnuityOrDisabilityHealth.getLife();
				//LOG15
				if (life.getOLifEExtensionCount() > 0 ){	
					lifeEx = NbaUtils.getFirstLifeExtension(life);
				}
				//End LOG15
			} else if (lifeOrAnnuityOrDisabilityHealth.isAnnuity()) {
				annuity = lifeOrAnnuityOrDisabilityHealth.getAnnuity();
			} else {
				if (getLogger().isWarnEnabled())
					getLogger().logWarn("Policy XML has no Life information");
				return "";
			}
		}
		if (policy.hasApplicationInfo()) {
			applicationinfo = policy.getApplicationInfo();
			applicationinfoEx = NbaUtils.getFirstApplicationInfoExtension(applicationinfo); //SPR3290
		}
		
		//Turn on flag D bit 4 in the Basic (01) Segment for Indeterminate Premium contracts
		if (policy.getProductType() == OLI_PRODTYPE_INDETERPREM) { //NBA140
            basicDXE = basicDXE + createDataExchange("1", INF_BASIC_FLAGD_4, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9); //NBA140
        } //NBA140
		
		//set 0 and 1 bits for flag E for 01 segment
		//FBRFLGE0 & FBRFLGE1
		if (isAP)
			basicDXE = basicDXE + createDataExchange("1", INF_BASIC_FLAGE_0, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		//bit 1
		basicDXE = basicDXE + createDataExchange("1", INF_BASIC_FLAGE_1, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		// LOG15
		if (lifeEx != null && lifeEx.hasPremType())
			if (lifeEx.getPremType() == OLI_ANNPREM_FIXED) {
				//bit 2
				basicDXE = basicDXE + createDataExchange("1", INF_BASIC_FLAGE_2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			}
		if (annuity != null && annuity.hasPremType())
			if (annuity.getPremType() == OLI_ANNPREM_FIXED){
				//bit 2
				basicDXE = basicDXE + createDataExchange("1", INF_BASIC_FLAGE_2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			}
		if (isTrad) {	
			basicDXE = basicDXE + createDataExchange("1", INF_BASIC_FLAGE_5, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		}
		
		//begin SPR3191
		lifeProduct = getNbaProduct().getLifeProduct(); //LifeProduct for primary coverage
        if (lifeProduct != null) {
       	 	lifeProductExtension = AccelProduct.getFirstLifeProductExtension(lifeProduct); //NBA237
            if (lifeProductExtension != null) {
                //FBRFLGE3
                if (lifeProductExtension.getPremType() == OLI_ANNPREM_FLEX || lifeProductExtension.hasDefLifeInsMethod()) {
                    basicDXE = basicDXE + createDataExchange(FLAG_BIT_ON, INF_BASIC_FLAGE_3, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
                }
                //FBRFLGE7
                if (lifeProductExtension.getGuidelinePremCalcRule() == OLIEXT_LU_SEVENPAYCALRULE_CALCULATED) {
                    basicDXE = basicDXE + createDataExchange(FLAG_BIT_ON, INF_BASIC_FLAGE_7, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
                }
            }
        }
		
		//end SPR3191
		
		//End LOG15	
		basicDXE = basicDXE + createDataExchange(servicingAgent,SERVICE_AGENT,CYBTRANS_NONE,CYBTBL_NONE,CT_CHAR,11);
		if (policy != null) {
			// Master Company (Holding.Policy) DXE: FBRISSCO
			basicDXE = basicDXE + createDataExchange(policy.getCarrierCode(), ISSUE_COMPANY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			// Admin Company (Holding.Policy) DXE: FBRADMCO & FBRFINCO
			basicDXE = basicDXE + createDataExchange(policy.getAdministeringCarrierCode(), ADMIN_COMPANY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			basicDXE = basicDXE + createDataExchange(policy.getAdministeringCarrierCode(), INF_FINANCIAL_CO, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			
		
			// The date to which the contract is currently funded.  (Holding.Policy) DXE: FBRPDTDT
			//If paid to date not in XML then move issue date to paidtodate
			if (policy.hasPaidToDate()){
				basicDXE = basicDXE +  createDataExchange(policy.getPaidToDate(), INF_PAID_TO_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
			}else{
				basicDXE = basicDXE +  createDataExchange(policy.getEffDate(), INF_PAID_TO_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
			}
			// Indicates that the risk of this policy is shared in whole or in part by another carrier.  TRUE if risk is shared, FALSE if not.
			// (Holding.Policy) DXE: FBRREINS
			if (policy.hasReinsuranceInd())
				basicDXE = basicDXE + createDataExchange(policy.getReinsuranceInd(),REINSUR_IND,CYBTRANS_NONE, CYBTBL_NONE,CT_CHAR, 1);
			// (Holding.Policy)- next monthliversary date. DXE: FBRNMVDT
			basicDXE = basicDXE + createDataExchange(policy.getEffDate(),INF_NEXT_MV,CYBTRANS_NONE, CYBTBL_NONE,CT_CHAR,8);
			// Only applicable if Product Type = 'U' - Mode Premium Amount (Holding.Policy) DXE: FBRMPREM, DXE Master(s): FBRBMODE
			basicDXE = basicDXE + createDataExchange(policy.getPaymentAmt(), PAYMENT_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			//Non Standard mode DXE: FBRMDNST				
			basicDXE = basicDXE	+ createDataExchange(getSpcMode(policy), INF_SP_FREQ_NONSTANDARD_MODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);  //NBA104
			// Billing Form (Holding.Policy) DXE: FBRFORM, DXE Master(s): FBRBMODE, FBRMPREM
			// Typecode UCT (CLUDT831)  OLI_LU_PAYMETHOD
			basicDXE = basicDXE + createDataExchange(policy.getPaymentMethod(), PAYMENT_METH, "OLI_LU_PAYMETHOD", CYBTBL_UCT, CT_DEFAULT, 0);
			// Last Billing Date (Holding.Policy) DXE: FBRBILDT
			basicDXE = basicDXE + createDataExchange(policy.getLastNoticeDate(), LST_BILL_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			// Last Billing Kind (Holding.Policy) DXE: FBRBILKD
			//  UCT (CLPCTB15)  OLI_LU_NOTICETYPE
			basicDXE = basicDXE + createDataExchange(policy.getLastNoticeType(), LST_BILL_KIND, "OLI_LU_NOTICETYPE", CYBTBL_UCT, CT_CHAR, 1);
			// Billing Mode  (Holding.Policy) DXE: FBRBMODE
			// Typecode UCT (CLDET0L2)  OLI_LU_PAYMODE	
			if (getBillingType(policy) == 0)
				basicDXE = basicDXE +  createDataExchange(policy.getPaymentMode(), PAYMENT_MODE, "OLI_LU_PAYMODE", CYBTBL_UCT, CT_DEFAULT, 0);
			else
				basicDXE = basicDXE + createDataExchange("01",PAYMENT_MODE, CYBTRANS_NONE, CYBTBL_NONE,CT_DEFAULT, 0);
			// First Notice Extract Day (Holding.Policy) DXE: FBRDUEDY
			basicDXE = basicDXE + createDataExchange(policy.getPaymentDraftDay(), FRST_NOTICE_EXTR_DAY, CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0);

			// Handling (Holding.Policy) DXE: FBRHANDL
			//  UCT (CLDET019)  OLI_LU_SPECIALHANDLING
			if (policy.hasSpecialHandling())
				basicDXE = basicDXE + createDataExchange(policy.getSpecialHandling(), HANDLING, NbaTableConstants.OLI_LU_SPECIALHANDLING, CYBTBL_UCT, CT_CHAR, 1);//SPR1999				
			
			// State (jurisdiction) policy will be issued.. (Holding.Policy) DXE: FBRISTAT
			//  UCT (ST_CTL)  OLI_LU_STATE
			basicDXE = basicDXE + createDataExchange(policy.getJurisdiction(), JURISDICTION, "NBA_STATES", CYBTBL_STATE_TC, CT_CHAR, 3);
			// Billed To Date (Holding.Policy) DXE: FBRBILTD
			if (policy.hasBilledToDate())
				basicDXE = basicDXE + createDataExchange(policy.getBilledToDate(), BILLED_TO_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			else
				basicDXE = basicDXE + createDataExchange(policy.getEffDate(), BILLED_TO_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		}
		

		policyEx = null;
		if (policy.getOLifEExtensionCount() > 0 ){	
			policyEx = NbaUtils.getFirstPolicyExtension(policy);	
		}		
		if (policyEx != null){
			if (policyEx.hasContractChangeType()){
				//entry code DXE: FBRENTCD 
				basicDXE = basicDXE + createDataExchange(policyEx.getContractChangeType(), INF_ENTRY_CODE, "OLIEXT_LU_CHANGETYPE", CYBTBL_UCT,CT_DEFAULT, 0);
				String changeType = getCyberValue(policyEx.getContractChangeType(), "OLIEXT_LU_CHANGETYPE", CYBTBL_UCT, compCode, DEFAULT_COVID);
				//record status - DXE:FBRRECST
				char[] record_status = changeType.toCharArray();
				if (record_status[1] < 'J')
					basicDXE = basicDXE + createDataExchange("0", INF_REC_STATUS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0);
				else
					basicDXE = basicDXE + createDataExchange("1", INF_REC_STATUS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0); 
			}
			// Timing for Notice (Holding.Policy) DXE: FBRDUEUS
			// Typecode  OLIEXT_LU_TIMING
			basicDXE = basicDXE + createDataExchange(policyEx.getTiming(), TIMING_FOR_NOTICE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);			

		}
			
		if (holding != null) { 
			// Pension Code (Holding) DXE: FBRPENSN
			// VT_I4 UCT (CEUDT113)  OLIEXT_LU_PENSIONCODE
			if (holding.hasQualifiedCode()) 
				basicDXE = basicDXE + createDataExchange(holding.getQualifiedCode(),APP_PENSION_CD,NbaTableConstants.OLI_LU_QUALIFIED,CYBTBL_UCT,CT_CHAR,1);
			// Assignment Code (Holding) DXE: FBRRESTA
			// VT_I4 UCT (CLDET015)  OLIEXT_LU_ASSIGNCODE
			if (holding.hasAssignmentCode())
				basicDXE = basicDXE + createDataExchange(
							holding.getAssignmentCode(),
							INF_ASSIGNMENT_CODE,
							NbaTableConstants.OLI_LU_ASSIGNED,
							CYBTBL_UCT,
							CT_CHAR,
							1);
			// Restriction Code (Holding) DXE: FBRRESTR
			// VT_I4 UCT (CLDET016)  OLI_LU_RESTRICT
			if (holding.hasRestrictionCode())
				basicDXE = basicDXE + createDataExchange(
							holding.getRestrictionCode(),
							APP_RESTRICT_CD,
							NbaTableConstants.OLI_LU_RESTRICT, //SPR1999
							CYBTBL_UCT,
							CT_CHAR,
							1);
			// Last Ann. Processed (Holding) DXE: FBRLSTDF
			if (holding.hasLastAnniversaryDate()) 
				basicDXE = basicDXE + createDataExchange(holding.getLastAnniversaryDate(), A_LAST_ANNIV_PROC, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);

		}
		
		if (policy.hasApplicationInfo()) {
			applicationinfo = policy.getApplicationInfo();
			//set FBRSTAT to 12 if CWA is on the contract. If no money is present then status is 11
			//Begin SPR1791 Always send a 12.  Host will update status if CWA.
			//if (applicationinfo.hasCWAAmt() && applicationinfo.getCWAAmt() > 0)
				basicDXE = basicDXE + createDataExchange("12", INF_PREMIUM_STATUS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0); 
			//else
			//	basicDXE = basicDXE + createDataExchange("11", INF_PREMIUM_STATUS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0); 	
			// End SPR1791
			
			if (applicationinfo.hasApplicationType()){
				recControlDXE = recControlDXE + createDataExchange(applicationinfo.getApplicationType(), APPLICATION_TYPE,"OLI_LU_APPTYPE", CYBTBL_UCT, CT_CHAR, 0);
			}
			//set FRCFLGA5 to 1 for traditional products, 0 for advanced
			if (isTrad)
				recControlDXE = recControlDXE + createDataExchange("1", RC_FLAGA_5, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			else
				recControlDXE = recControlDXE + createDataExchange("0", RC_FLAGA_5, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);

			// Date app was signed. (Holding.Policy.ApplicationInfo) DXE: FBRAPPDT
			basicDXE = basicDXE + createDataExchange(applicationinfo.getSignedDate(), SIGN_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			
			//Indicates whether or not the MIB Authorization has already been received. (Holding.Policy.ApplicationInfo) DXE: FRCFLGA7
			if (applicationinfoEx != null && applicationinfoEx.hasMIBAuthorization()) {
				recControlDXE = recControlDXE + createDataExchange(applicationinfoEx.getMIBAuthorization(), RC_FLAGA_7, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
			}
		}
		
		if (life !=null) {
			// NFO Option  (Holding.Life) DXE: FBROPT
			if (life.hasNonFortProv())
				basicDXE = basicDXE + createDataExchange(life.getNonFortProv(), NFO_OPTION_A, "OLI_LU_NONFORTPROV", CYBTBL_UCT, CT_CHAR, 9);
			// Dividend Option  (Holding.Life) DXE: FBRPROPD
			if (life.hasDivType())
				basicDXE = basicDXE + createDataExchange(life.getDivType(), DIVIDEND_OPTION, "OLI_LU_DIVTYPE", CYBTBL_UCT, CT_CHAR, 9);
		}	
		
		if (lifeEx != null) {
			// Only applicable if Product Type = 'I', 'N' or 'O' - Secondary Div. Option (Holding.Policy.Life) DXE: FBRPROSD
			// Typecode UCT (CLDET020)  OLI_LU_DIVTYPE
			if (lifeEx.hasSecondaryDividendType())
				basicDXE = basicDXE + createDataExchange(lifeEx.getSecondaryDividendType(), SEC_DIV_OPT, "OLI_LU_DIVTYPE", CYBTBL_UCT, CT_CHAR, 1);
		}
		
		if (holdingEx != null) { 
			// Last Accounting Date (Holding) DXE: FBRPAYDT
			if (holdingEx.hasLastAccountingDate()) 
				basicDXE = basicDXE + createDataExchange(holdingEx.getLastAccountingDate(), APP_LST_ACCT_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			else
				basicDXE = basicDXE + createDataExchange(policy.getEffDate(), APP_LST_ACCT_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		}
		//Previous premium method DXE: FBRHOWPD
		basicDXE = basicDXE + createDataExchange("1",INF_PREV_PREM_METH , CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0); 				
		
		//EIL indicator DXE: FBREIL
		if (policy.getProductType() == OLI_PRODTYPE_EXINTL){
			basicDXE = basicDXE + createDataExchange("1",INF_EIL_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0); 				
		}else{
			basicDXE = basicDXE + createDataExchange("0",INF_EIL_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0); 				
		
		}
		//begin SPR2099
		String mecInd = "0";
		//Override MEC Indicator: 0-Obtained from PDF, 1-Entered by user
		if (life != null && life.hasLifeUSA() && life.getLifeUSA().getMEC1035()) {
			//MEC Indicator entered by the user
			mecInd = formatCyberBoolean(life.getLifeUSA().getMECInd());
		} else {
			//MEC Indicator obtained from PDF
		    //SPR3191 code deleted
			if (lifeProductExtension != null) {
				mecInd =
					getCyberValue(
						lifeProductExtension.getMECIssueType(),
						NbaTableConstants.OLIEXT_LU_MECISSUETYPE,
						CYBTBL_UCT,
						compCode,
						DEFAULT_COVID);
			}
		}
		basicDXE = basicDXE + createDataExchange(mecInd, POL_MEC_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		//end SPR2099
		// begin NBA104
		if (policyEx != null && !NbaUtils.isFlexiblePrem(olife)) {
			/*
			 * Mode factors
			 * 
			 * semi-annual DXE: FBRMCALS
			 * quarterly DXE: FBRMCALQ
			 * monthly DXE: FBRMCALM
			 * 
			 */
			int count = policyEx.getPaymentModeMethodsCount();
			for (int i=0; i < count; i++) {
				PaymentModeMethods paymentModeMethods = policyEx.getPaymentModeMethodsAt(i);
				switch ((int)paymentModeMethods.getPaymentMode()){
					case (int)OLI_PAYMODE_BIANNUAL:
						basicDXE = basicDXE + createDataExchange(paymentModeMethods.getModeFactor(),INF_SEMI_ANN_MODE_FACTOR,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						break;
					case (int)OLI_PAYMODE_QUARTLY:
						basicDXE = basicDXE + createDataExchange(paymentModeMethods.getModeFactor(),INF_QUARTERLY_MODE_FACTOR,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						break;
					case (int)OLI_PAYMODE_MNTHLY:
						basicDXE = basicDXE + createDataExchange(paymentModeMethods.getModeFactor(),INF_MONTHLY_MODE_FACTOR,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						basicDXE = basicDXE + createDataExchange(paymentModeMethods.getExceptionModeRule(),INF_MODE_FACTOR_MODIFY,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						break;
				}
	
			}

			/*
			 * Multiply and rating order rules from PDF
			 * 
			 * Multiply order rule DXE: FBRORDCD
			 * Rating Order Rule DXE: FBRRORCD			 
			 * Rounding Rule DXE:  FBRROUND
			 * 
			 */
			if (policyEx.hasPaymentModeAssembly()){
				PaymentModeAssembly paymentModeAssembly = policyEx.getPaymentModeAssembly();
				basicDXE = basicDXE + createDataExchange(paymentModeAssembly.getMultipleOrder(),INF_MULTIPLY_ORDER_RULE,NbaTableConstants.OLI_LU_MULTORDTYP,CYBTBL_UCT, CT_DEFAULT,0); 
				basicDXE = basicDXE + createDataExchange(paymentModeAssembly.getRatingOrder(),INF_RATING_ORDER_RULE,NbaTableConstants.OLI_LU_RATEORDTYP,CYBTBL_UCT, CT_DEFAULT,0);
				basicDXE = basicDXE + createDataExchange(paymentModeAssembly.getFirstRoundingRule(),INF_ROUNDING_ORDER_RULE,NbaTableConstants.OLI_LU_ROUNDTYP,CYBTBL_UCT, CT_DEFAULT,0);
			}
			
			count = policyEx.getPaymentFeesCount();
			for (int i=0; i < count; i++) {
				PaymentFees fee = policyEx.getPaymentFeesAt(i);
				if (fee != null) {
					switch ((int)fee.getFeeType()){
						/*
						 * Policy Fee Data 
						 * Policy Fee Factors Table Code DXE: FBRPFEEF
						 * Policy Fee Add Code DXE: FBRPFEEA
						 * Policy Fee Commissionable indictor DXE: FBRPFEEC
						 * Policy Fee User-Defined Rule DXE: FBRPFUSR
						 * Policy Fee amount DXE: FBRFEE
						 * 
						 */
						case (int)OLI_FEE_POLICYFEE:
							basicDXE = basicDXE + createDataExchange(fee.getFeeTableIdentity(),INF_POLFEE_FACTOR_TBL_CODE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
							basicDXE = basicDXE + createDataExchange(fee.getFeeAddRule(),INF_POLFEE_ADD_CODE,NbaTableConstants.OLI_LU_FEEADDRUL,CYBTBL_UCT, CT_DEFAULT,0);
							basicDXE = basicDXE + createDataExchange(fee.getCommissionablePremCalcInd(),INF_POLFEE_COMM_IND,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
							basicDXE = basicDXE + createDataExchange(fee.getFeeCalcRule(),INF_POLFEE_USER, NbaTableConstants.OLI_LU_FEECALCRUL,CYBTBL_UCT, CT_DEFAULT,0);
							basicDXE = basicDXE + createDataExchange(fee.getFeeAmt(),INF_POLFEE_AMT, CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
							break;
						/*
						 * Collection Fee Data
						 * 
						 * Collection Fee Add Code DXE:FBRCFEEA
						 * Collection Fee Commissionable Indicator DXE: FBRCFEEC
						 * Collection Fee Amt DXE: FBRMCALC
						 * 
						 */
						case (int)OLI_FEE_COLLECTION:
							basicDXE = basicDXE + createDataExchange(fee.getFeeAddRule(),INF_COLLECTION_FEE_ADD_RULE,NbaTableConstants.OLI_LU_FEEADDRUL,CYBTBL_UCT, CT_DEFAULT,0);
							basicDXE = basicDXE + createDataExchange(fee.getCommissionablePremCalcInd(),INF_COLLECTION_FEE_COMM_IND, CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
							basicDXE = basicDXE + createDataExchange(fee.getFeeAmt(),INF_COLLECTION_FEE_AMT, CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
							break;	
					}
				}
			}
		}
		// end NBA104
		return recControlDXE + basicDXE + createPolicyProduct(olife,policy.getProductCode(),1,"");
		}

	/**
	 * Create the basic segment data from the policy product information
	 *
	 * @param policyProduct The current PolicyProduct XML object
	 * @return java.lang.String The basic segment DXE information obtained from the policy product information
	 */
	protected String createPolicyProductBasic(PolicyProduct policyProduct) { 
		
		// SPR3290 code deleted
		PolicyProductExtension polProdEx = null;
		// SPR3290 code deleted
		NonForProvision nonForProvision = null; 
		RateVariation rateVariation = null;
		RateVariationExtension rateVariationEx = null;
		// SPR3290 code deleted
		// SPR2114 code deleted
		LifeProductOrAnnuityProduct lifeProductOrAnnuityProduct = null;
		LifeProduct lifeProduct = null;
		CoverageProduct coverageProduct = null;
		CoverageProductExtension coverageProductEx = null;
		CommOptionAvailable commOptionAvailable  = null;
		String DXE = "";
		// SPR3290 code deleted
		
		polProdEx = AccelProduct.getFirstPolicyProductExtension(policyProduct);	//SPR2114 NBA237
				
		// NBA104 deleted code
		if (polProdEx != null) {			
			for(int i=0 ;i < polProdEx.getNonForProvisionCount(); i++){
				/*
				 * Loan information
				 * 
				 * Automatic Premium Loan DXE:FBRAPL
				 * APL Stp limitations  DXE:FBRLIM 
				 * Interest Type DXE: FBRLTYPE
				 * 
				 */
				nonForProvision = polProdEx.getNonForProvisionAt(i);
				switch ((int)nonForProvision.getNonFortProv()){
					case 4: 
					case 5:
						DXE = DXE + createDataExchange(nonForProvision.getNFOModeMethods(),INF_AUTO_PREM_LOAN,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(nonForProvision.getLimitations(),INF_APL_STOP,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						break;
					
				}
			}
			//begin SPR2114
			LoanProvision loanProvision;
			String loanIntType = "9"; //no loans allowed is CPD default for no tags  
			double loanRate = 0;
			for (int i = 0; i < polProdEx.getLoanProvisionCount(); i++) {
				loanProvision = polProdEx.getLoanProvisionAt(i);
				if (loanProvision.getLoanType() == OLI_LNTYPE_NONLIFEREG) {
					if (loanProvision.getLoanIntTiming() == OLI_INTTIME_ADVANCE) {
						if (loanProvision.getLoanIntType() == OLI_INTTYPE_FIXED) {
							loanIntType = "0"; //Advanced, fixed interest
						} else if (loanProvision.getLoanIntType() == OLI_INTTYPE_VAR) {
							loanIntType = "6"; //Advanced, variable interest
						}
					} else if (loanProvision.getLoanIntTiming() == OLI_INTTIME_ARREARS) {
						if (loanProvision.getLoanIntType() == OLI_INTTYPE_FIXED) {
							loanIntType = "1"; //Arrears, fixed interest
						} else if (loanProvision.getLoanIntType() == OLI_INTTYPE_VAR) {
							loanIntType = "7"; //Arrears, variable interest
						}
					}
					for (int j = 0; j < loanProvision.getRateVariationCount(); j++) {
						rateVariation = loanProvision.getRateVariationAt(j);
						rateVariationEx = AccelProduct.getFirstRateVariationExtension(rateVariation); //NBA237
						if (rateVariationEx.getInterestUseType() == OLI_INTERESTUSE_CHARGERATE) {
							loanRate = rateVariation.getInvestRate();
							break;
						}
					}
					break;
				}
			}
			//Loan Interest Type DXE: FBRLTYPE
			DXE = DXE + createDataExchange(loanIntType, INF_LOAN_INTEREST_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//Loan Interest Rate DXE: FBRLINTR
			DXE = DXE + createDataExchange(loanRate, INF_LOAN_INTEREST_RATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//end SPR2114
			// begin SPR1950
			// Mode Premium Table Code DXE:FBRMPCOD
			if (isFixedPrem(policyProduct) && polProdEx.getPaymentAssemblyCount() > 0) {
				DXE = DXE + createDataExchange(polProdEx.getPaymentAssemblyAt(0).getModePremTableIdentity(),INF_MODE_PREM_TABLE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			}
			// end SPR1950
			//begin SPR2115
			//Statement Frequency DXE: FULASFRQ
/*			NBA237 deleted
 * 			FinancialStatement financialStatement;
			for (int i = 0; i < polProdEx.getFinancialStatementCount(); i++) {
				financialStatement = polProdEx.getFinancialStatementAt(i);
				if (financialStatement.getStatementType() == OLI_STMTTYPE_POLANNUAL) {
					DXE = DXE + createDataExchange(financialStatement.getStatementMode(), STATEMENT_FREQ, "NBA_PP_STMTBASIS", CYBTBL_UCT, CT_DEFAULT, 0); 
					break;
				}
			}*/
			//end SPR2115 
		}
		
		//NBA104 deleted code		
		// SPR2114 code deleted
		lifeProductOrAnnuityProduct = policyProduct.getLifeProductOrAnnuityProduct();
		//begin SPR2114
		if (lifeProductOrAnnuityProduct.isLifeProduct()) {
			lifeProduct = lifeProductOrAnnuityProduct.getLifeProduct();
			if (lifeProduct != null) {
				//begin SPR2095
				String policyProductCode = policy.getProductCode();
				for (int i = 0; i < lifeProduct.getCoverageProductCount(); i++) {
					coverageProduct = lifeProduct.getCoverageProductAt(i);
					if (policyProductCode.equals(coverageProduct.getProductCode())) {
						coverageProductEx = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA237
						if (coverageProductEx != null) {
							//Death Benefit Indicator DXE: FBRDBIND
							DXE = DXE + createDataExchange(coverageProductEx.getDBCalcMethodType(), INF_DEATH_BEN_IND, NbaTableConstants.OLIEXT_LU_DBCALCMETHODTYPE, CYBTBL_UCT, CT_DEFAULT, 0);
						}
						break;
					}
				}
			}
		}				
		//end SPR2095 SPR2114
		if ((policyProduct.getPolicyProductInfoCount() > 0) && (policyProduct.getPolicyProductInfoAt(0).getCommOptionAvailableCount() > 0))
			commOptionAvailable  = policyProduct.getPolicyProductInfoAt(0).getCommOptionAvailableAt(0);
		if ((commOptionAvailable != null) && (commOptionAvailable.getCommOption()== 11))
			//Commission Chargeback Code DXE: FBRCMCCD
			DXE = DXE + createDataExchange(commOptionAvailable.getCarrierCommCode(),INF_COMM_CHARGE_BACK,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			
		return DXE;
	}
	/**
	 * Creat Advanced Product (66 segment) DXE information for inforce build.
	 *
	 * @param policy The Policy XML object
	 * @param olife The OLifE XML object
	 * @return java.lang.String The advanced product DXE information obtained from the policy product information
	 */
	protected String createAPInfo(Policy policy, OLifE olife) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null;
		Life life = null;
		// SPR3290 code deleted
		Annuity annuity = null;
		PolicyExtension policyEx = null;
		String targetDXE = "";

		if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) {
			lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			if (lifeOrAnnuityOrDisabilityHealth.isLife())
				life = lifeOrAnnuityOrDisabilityHealth.getLife();
			else if (lifeOrAnnuityOrDisabilityHealth.isAnnuity()) {
				annuity = lifeOrAnnuityOrDisabilityHealth.getAnnuity();
			} else {
				if (getLogger().isWarnEnabled())
					getLogger().logWarn("Policy XML has no Life information");
				return "";
			}
		}
		//DXE: FULFLAGA & FULFLAGB
		if (policy.getFinancialActivityCount() > 0)
			APDXE = APDXE + createDataExchange("1", "FULFLGA1", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		//SPR3161 code deleted

		policyEx = null;
		if (policy.getOLifEExtensionCount() > 0) {
			policyEx = NbaUtils.getFirstPolicyExtension(policy);
		}
		if (policyEx != null) {
			// Billing Option DXE:FULBILLC
			if (policyEx.hasBillingOption())
				APDXE = APDXE + createDataExchange(policyEx.getBillingOption(), INF_BILLING_OPTION, "OLIEXT_LU_BILLNOTOPTTYPE", CYBTBL_UCT, CT_DEFAULT, 0);//SPR2003
			// SPR2115 code deleted
			//Statement Frequency DXE: FULCNFRQ
			if (policyEx.hasConfirmationFreq())
				APDXE = APDXE + createDataExchange(policyEx.getConfirmationFreq(), CONFIRMATION_FREQ, "OLI_LU_PAYMODE", CYBTBL_UCT, CT_DEFAULT, 0);//NBA104
		}
		//DXE: FULMTDAT		
		APDXE = APDXE + createDataExchange(policy.getTermDate(), INF_AP_MATURITY_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8); //SPR1986

		if (life != null) {
			//begin SPR1878
			//DXE: FULFLGD6
			if(life.hasInitialPremAmt() && life.getInitialPremAmt() > 0){
				APDXE = APDXE + createDataExchange("1", "FULFLGD6", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			}
			//end SPR1878
			// Guaranteed interest rate used for projection purposes DXE: FULIGIR
			if (life.hasProjectedGuarIntRate())
				APDXE = APDXE + createDataExchange(life.getProjectedGuarIntRate(), GUAR_INT_RATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);
			//begin SPR3191
			LifeProduct lifeProduct = getNbaProduct().getLifeProduct(); //LifeProduct for primary coverage
            if (lifeProduct != null) {
                LifeProductExtension lifeProductExtension = AccelProduct.getFirstLifeProductExtension(lifeProduct); //NBA237
                if (lifeProductExtension != null) {
                    APDXE = APDXE + createDataExchange(lifeProductExtension.getDefLifeInsMethod(), INF_TAX_LAW_QUAL_TEST, NbaTableConstants.OLI_LU_LIFETEST, CYBTBL_UCT, CT_CHAR, 12);
                }
            }
			//end SPR3191
		// SPR1986 code deleted
		}

		if (annuity != null) {
			//begin SPR1878
			//DXE: FULFLGD6
			if(annuity.hasInitPaymentAmt() && annuity.getInitPaymentAmt() > 0){
				APDXE = APDXE + createDataExchange("1", "FULFLGD6", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			}	
			//end SPR1878		
			// Guaranteed interest rate used for projection purposes DXE: FULIGIR
			APDXE = APDXE + createDataExchange(annuity.getGuarIntRate(), GUAR_INT_RATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);
			// NBA104 code deleted
			// SPR1986 code deleted
			//DXE: FULSPFRA
			//Indicates if there is a pro rata refund of the cost of insurance on a full surrender.
			APDXE = APDXE + createDataExchange("0", INF_AP_PRO_RATA_REFUND, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);//SPR2003
		}

		return targetDXE + APDXE + createPolicyProduct(olife, policy.getProductCode(), 6, ""); //SPR3197		
	}

	
	/**
	 * Created tradional information (51 segment) DXE for the inforce submit
	 *
	 * @param policy The Policy XML object
	 * @return java.lang.String The traditional DXE information
	 */
	protected String createTradInfo(Policy policy) { 

		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null; 
		// SPR3290 code deleted
		String termDXE = "";
		// SPR3290 code deleted
		

		if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) {
			lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			// begin SPR3290
			if (!lifeOrAnnuityOrDisabilityHealth.isLife()) {
				if (getLogger().isWarnEnabled())
					getLogger().logWarn("Policy XML has no Life information");
				return "";
			}
			// end SPR3290
		}
		/*
		 * Last monthly anniversary. Set to Zero. DXE: FPCLMAP
		 * Last Dividend update duration. Set to Zero. Dxe: FPCLDVUP
		 * Latest history duration. Set to Zero. DXE: FPCHSDUR
		 * Low history duration. Set to Zero. DXE: FPCLHDUR
		 * Net cost accumulated premiums. Set to Zero. DXE: FPCNCACP
		 * Net cost accumulated dividends. Set to Zero DXE: FPCNCACD
		 * Net Cost content indicator. Set to Zero. DXE:FPCNCCNV
		 * Direct Recognition Acceptance Date, Set to issue date DXE: FPCDRDTE
		 * 
		 */

		termDXE = termDXE + createDataExchange("XX1XXXXX", "FPCFLAGB", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 8);	//SPR2134
		
		termDXE = termDXE + createDataExchange(termDate, INF_TRAD_MATURITY_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);
		//DXE: FPCBLDDT
		termDXE = termDXE + createDataExchange(new Date(), INF_TRAD_BUILD_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);		
		termDXE = termDXE + createDataExchange(0, INF_TRAD_LAST_MNTHLY_ANN, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		termDXE = termDXE + createDataExchange(0, INF_TRAD_LAST_DIV_UPDT_DUR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		
		
		//DXE: FPCPBDAT & FPCLANNV		
		termDXE = termDXE + createDataExchange(policy.getEffDate(), INF_TRAD_PROCESS_BACK_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);	
		termDXE = termDXE + createDataExchange(0, INF_TRAD_LATEST_HIST_DUR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		termDXE = termDXE + createDataExchange(0, INF_TRAD_LOWEST_HIST_DUR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);

		if (policy.hasPaidToDate()){	
			termDXE = termDXE + createDataExchange(policy.getPaidToDate(), INF_TRAD_PAID_TO_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);	
		}else{
			termDXE = termDXE + createDataExchange(policy.getEffDate(), INF_TRAD_PAID_TO_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
		}
		termDXE = termDXE + createDataExchange(policy.getEffDate(), INF_TRAD_LAST_ANNIVERSARY, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);	
		termDXE = termDXE + createDataExchange(0, INF_TRAD_NET_COST_ACC_PREM, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		termDXE = termDXE + createDataExchange(0, INF_TRAD_NET_COST_ACC_DIV, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		termDXE = termDXE + createDataExchange(0, INF_TRAD_NET_COST_CONTENT_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);	
		termDXE = termDXE + createDataExchange(policy.getEffDate(), INF_TRAD_DIR_RECOG_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);	

		return termDXE;
	}

	/**
	 * Create Guideline information
	 * 
	 * @param life The Life XML object
	 * @return java.lang.String The guideline DXE information
	 */
	protected String createGuidelineInfo() {
		StringBuffer gDXE = new StringBuffer();
		LifeProduct lifeProduct = getNbaProduct().getLifeProduct(); //LifeProduct for primary coverage
		Life life = getNbaTXLife().getLife();
		LifeUSA lifeUSA = life.getLifeUSA();
		LifeProductExtension lifeProductExtension = AccelProduct.getFirstLifeProductExtension(lifeProduct); //NBA237
		LifeUSAExtension lifeUSAExtension = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
		if (lifeProductExtension != null) {
			if (lifeProductExtension.getMECIssueType() == OLIX_MECISSUETYPE_7PAY) {
				// Type 1
				gDXE.append(createDataExchange(TAMRA_TYPE_1, TAMRA_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				Date grandFathered7payDate = NbaUtils.getDateFromStringInUSFormat("06/21/1988");
				Date grandFatheredGuidelineDate = NbaUtils.getDateFromStringInUSFormat("10/21/1988");
				Date issueDate = getPolicy().getEffDate();
				String flag;
				long code;
				flag = (NbaUtils.compare(grandFathered7payDate, issueDate) > 0) ? FLAG_BIT_ON : FLAG_BIT_OFF; // less than 06211988 = "1"
				gDXE.append(createDataExchange(flag, TAMRA_FLAGA_BIT0, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				flag = (NbaUtils.compare(grandFatheredGuidelineDate, issueDate) > 0) ? FLAG_BIT_ON : FLAG_BIT_OFF; // less than 10211988 = "1"
				gDXE.append(createDataExchange(flag, TAMRA_FLAGA_BIT1, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				flag = (lifeProductExtension.getSevenPayCalcRule() == OLIEXT_LU_SEVENPAYCALRULE_CALCULATED) ? FLAG_BIT_ON : FLAG_BIT_OFF;
				//calculated = "1"
				gDXE.append(createDataExchange(flag, TAMRA_FLAGA_BIT3, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				String mecInd = "0";
				//begin SPR2099
				//Override MEC Indicator: 0-Obtained from PDF, 1-Entered by user			
				flag = (lifeUSA.getMEC1035()) ? FLAG_BIT_ON : FLAG_BIT_OFF;
				gDXE.append(createDataExchange(flag, TAMRA_FLAGA_BIT5, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				if (lifeUSA.getMEC1035()) {
					mecInd = formatCyberBoolean(lifeUSA.getMECInd());
				} else {
					mecInd = "2"; //7-Pay Test from PDF (lifeProductExtension.getMECIssueType() == OLIX_MECISSUETYPE_7PAY) 
				}
				//end SPR2099
				gDXE.append(createDataExchange(mecInd, TAMRA_MEC_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				if (lifeUSA.getMECInd() && lifeUSA.hasMECDate()) {
					gDXE.append(createDataExchange(lifeUSA.getMECDate(), TAMRA_MEC_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
				} else {
					gDXE.append(createDataExchange(ZERO_DATE, TAMRA_MEC_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				}
				String rateInd = "0";
				if (lifeProductExtension.hasTAMRASubRateRule()) {
					code = lifeProductExtension.getTAMRASubRateRule();
					if (code == OLIX_TAMRASUBRATERULE_GREATER) {
						rateInd = "1";
					} else if (code == OLIX_TAMRASUBRATERULE_GUARPLUSCURR) {
						rateInd = "2";
					}
				}
				gDXE.append(createDataExchange(rateInd, TAMRA_RATES_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				gDXE.append(createDataExchange(issueDate, TAMRA_7PAY_START_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
				gDXE.append(createDataExchange(ZERO_DATE, TAMRA_7PAY_CHANGE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				gDXE.append(createDataExchange(lifeUSA.getSevenPayPrem(), TAMRA_7PAY_LEVEL_PREM, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				if (lifeUSAExtension != null) {
					gDXE.append(
						createDataExchange(lifeUSAExtension.getCurrIntRate7Pay(), TAMRA_7PAY_CURR_RATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 6));
					gDXE.append(
						createDataExchange(lifeUSAExtension.getGuarPeriod7Pay(), TAMRA_7PAY_CURR_GUAR, CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				}
				gDXE.append(createDataExchange(lifeUSA.getDeemedFaceAmt(), TAMRA_7PAY_SPEC_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
			}
			//Type 2
			String baseGTI = "";
			String base7PTI = "";
			if (lifeProductExtension.hasTAMRAGuidelineTableIdentity()) {
				baseGTI = lifeProductExtension.getTAMRAGuidelineTableIdentity();
			}
			if (lifeProductExtension.hasTAMRASevenPayTableIdentity()) {
				base7PTI = lifeProductExtension.getTAMRASevenPayTableIdentity();
			}
			ArrayList riderPhaseArray = new ArrayList();
			ArrayList riderGTIArray = new ArrayList();
			ArrayList rider7PTIArray = new ArrayList();
			int count = life.getCoverageCount();
			Coverage cov;
			String ridGTI;
			String rid7PTI;
			for (int i = 0; i < count; i++) {
				cov = life.getCoverageAt(i);
				lifeProduct = getNbaProduct().getLifeProduct(cov.getProductCode());
				if (lifeProduct != null) {
					lifeProductExtension = AccelProduct.getFirstLifeProductExtension(lifeProduct); //NBA237
					ridGTI = "";
					rid7PTI = "";
					if (lifeProductExtension != null) {
						if (lifeProductExtension.hasTAMRAGuidelineTableIdentity()) {
							ridGTI = lifeProductExtension.getTAMRAGuidelineTableIdentity();
						}
						if (lifeProductExtension.hasTAMRASevenPayTableIdentity()) {
							rid7PTI = lifeProductExtension.getTAMRASevenPayTableIdentity();
						}
						if ((ridGTI.length() > 0 || rid7PTI.length() > 0)
							&& !(baseGTI == ridGTI && base7PTI == rid7PTI)) { //Add if different than base
							riderPhaseArray.add(cov.getCoverageKey());
							riderGTIArray.add(ridGTI);
							rider7PTIArray.add(rid7PTI);
						}
					}
				}
			}
			if (baseGTI.length() > 0 || base7PTI.length() > 0 || riderPhaseArray.size() > 0) {
				gDXE.append(createDataExchange(TAMRA_TYPE_2, TAMRA_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				gDXE.append(createDataExchange(baseGTI, TAMRA_GUIDELINE_SRCH_KEY_BASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				gDXE.append(createDataExchange(base7PTI, TAMRA_7PAY_SRCH_KEY_BASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				for (int i = 0; i < riderPhaseArray.size(); i++) {
					gDXE.append(
						createDataExchange((String) riderPhaseArray.get(i), TAMRA_SRCH_KEY_PHASE_RDR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
					gDXE.append(
						createDataExchange((String) riderGTIArray.get(i), TAMRA_GUIDELINE_SRCH_KEY_RDR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
					gDXE.append(
						createDataExchange((String) rider7PTIArray.get(i), TAMRA_7PAY_SRCH_KEY_RDR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				}
			}
		}
		return gDXE.toString(); //59 segment	//NBA104
	}
	/**
	 * Create the inforce target segment
	 * 
	 * @param holding The Holding XML object
	 * @return java.lang.String The target DXE information
	 */
	// NBA104 New Method
	protected String createTargetInfo() {
		StringBuffer targetDXE = new StringBuffer();
		PolicyExtension policyEx = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyEx != null) {
			//Additional single premium
			if (policyEx.hasPlannedAdditionalPremium() && policyEx.getPlannedAdditionalPremium() > 0) {
				if (getPolicy().getProductType() == OLI_PRODTYPE_INTWL) {
					targetDXE.append(createDataExchange(TARGET_CODE_ADDL_SINGLE_PREM, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(
						createDataExchange(policyEx.getPlannedAdditionalPremium(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
			}
			//Initial net annual premium surrender target
			if (policyEx.hasInitialAnnualPremiumAmt() && policyEx.getInitialAnnualPremiumAmt() > 0) {
				targetDXE.append(createDataExchange(TARGET_INAP_SURR, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
				targetDXE.append(createDataExchange(getPolicy().getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
				targetDXE.append(createDataExchange(policyEx.getInitialAnnualPremiumAmt(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
			}
		}
		if (isLife()) {
			Life life = getNbaTXLife().getLife();
			LifeUSA lifeUSA = life.getLifeUSA();
			if (lifeUSA != null) {
				//Planned 1035 exchange premium
				if (lifeUSA.hasAmount1035() && lifeUSA.getAmount1035() > 0) {
					targetDXE.append(createDataExchange(TARGET_CODE_1035_PREM, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange(getPolicy().getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
					targetDXE.append(createDataExchange(lifeUSA.getAmount1035(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
				//Total TEFRA/DEFRA guideline level annual premium
				if (lifeUSA.hasGuidelineAnnPrem() && lifeUSA.getGuidelineAnnPrem() > 0) {
					targetDXE.append(createDataExchange(TARGET_TOTAL_GLP, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange("00", TARGET_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange(getPolicy().getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
					targetDXE.append(createDataExchange(lifeUSA.getGuidelineAnnPrem(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
				//Total TEFRA/DEFRA guideline single premium
				if (lifeUSA.hasGuidelineSinglePrem() && lifeUSA.getGuidelineSinglePrem() > 0) {
					targetDXE.append(createDataExchange(TARGET_TOTAL_GSP, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange("00", TARGET_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange(getPolicy().getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
					targetDXE.append(createDataExchange(lifeUSA.getGuidelineSinglePrem(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
			}
			LifeExtension lifeExt = NbaUtils.getFirstLifeExtension(life);
			if (lifeExt != null) {
				//Premium target
				if (lifeExt.hasPremLoadTargetAmt() && lifeExt.getPremLoadTargetAmt() > 0) {
					targetDXE.append(createDataExchange(TARGET_PREMIUM, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange("00", TARGET_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange(getPolicy().getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
					targetDXE.append(createDataExchange(lifeExt.getPremLoadTargetAmt(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
				//Monthly minimum annual premium
				if (life.hasMinPremAmt() && life.getMinPremAmt() > 0) {
					targetDXE.append(createDataExchange(TARGET_MAP, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange("01", TARGET_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange(lifeExt.getMapTargetEndDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
					targetDXE.append(createDataExchange(life.getMinPremAmt(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
			}
			//Coverage and CovOption targets
			targetDXE.append(createCoverageTargetInfo(life));
		}
		if (isAnnuity()) {
			Annuity annuity = nbaTXLife.getAnnuity();
			//ROTH conversion control
			if (annuity.hasRothIraNetContributionAmt() && annuity.getRothIraNetContributionAmt() > 0) {
				targetDXE.append(createDataExchange(TARGET_ROTH_CONVERSION, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
				targetDXE.append(createDataExchange(annuity.getFirstTaxYear(), TARGET_ROTH_YEAR, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4));
				targetDXE.append(
					createDataExchange(annuity.getRothIraNetContributionAmt(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
			}
			HoldingExtension holdExt = NbaUtils.getFirstHoldingExtension(getHolding());
			if (holdExt != null) {
				TaxReporting taxReporting;
				int taxCnt = holdExt.getTaxReportingCount();
				for (int i = 0; i < taxCnt; i++) {
					taxReporting = holdExt.getTaxReportingAt(i);
					//IRA tax control
					if (taxReporting.hasTaxableAmt() && taxReporting.getTaxableAmt() > 0) {
						targetDXE.append(createDataExchange(TARGET_CODE_TAX_CONTROL, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
						targetDXE.append(createDataExchange(taxReporting.getTaxYear(), TARGET_TX_YEAR, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4));
						targetDXE.append(createDataExchange(taxReporting.getTaxableAmt(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
					}
				}
			}
		}
		return targetDXE.toString();
	}
	/**
	 * Create the inforce target segment information for Coverages and CovOptions
	 * @return java.lang.String The target DXE information
	 */
	// NBA104 New Method
	protected String createCoverageTargetInfo(Life life) {
		StringBuffer targetDXE = new StringBuffer();
		int covcnt = life.getCoverageCount();
		int covOptcnt;
		Coverage cov;
		CoverageExtension covExt;
		CovOption covOpt;
		CovOptionExtension covOptExt;
		for (int i = 0; i < covcnt; i++) {
			cov = life.getCoverageAt(i);
			String covPhase = NbaUtils.addLeadingZeros(cov.getCoverageKey(), 2);
			covExt = NbaUtils.getFirstCoverageExtension(cov);
			if (covExt != null) {
				//Commission target
				if (covExt.hasCommTargetPrem() && covExt.getCommTargetPrem() > 0) {
					targetDXE.append(createDataExchange(TARGET_COMMIS, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange("01", TARGET_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange(cov.getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
					targetDXE.append(createDataExchange(covExt.getCommTargetPrem(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
				//Death benefit
				if (covExt.hasBlendedInsTargetDBAmtPct() && covExt.getBlendedInsTargetDBAmtPct() > 0) {
					targetDXE.append(createDataExchange(TARGET_DEATH_BENEFIT, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
					targetDXE.append(createDataExchange(cov.getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
					targetDXE.append(
						createDataExchange(covExt.getBlendedInsTargetDBAmtPct(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
				}
			}
			//Surrender target
			if (cov.getIndicatorCode() == OLI_COVIND_BASE && cov.hasSurrTargetPrem() && cov.getSurrTargetPrem() > 0) {
				targetDXE.append(createDataExchange(TARGET_SURRENDER, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
				targetDXE.append(createDataExchange(covPhase, TARGET_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
				targetDXE.append(createDataExchange(cov.getEffDate(), TARGET_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8));
				targetDXE.append(createDataExchange(cov.getSurrTargetPrem(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12));
			}
			covOptcnt = cov.getCovOptionCount();
			for (int j = 0; j < covOptcnt; j++) {
				covOpt = cov.getCovOptionAt(j);
				if (covOpt.getLifeCovOptTypeCode() == OLI_OPTTYPE_ABE) {
					covOptExt = NbaUtils.getFirstCovOptionExtension(covOpt);
					if (covOptExt != null) {
						//Increasing death benefit
						targetDXE.append(createDataExchange(TARGET_IDB, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
						targetDXE.append(createDataExchange(covPhase, TARGET_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
						targetDXE.append(
							createDataExchange(covOptExt.getIncAnnPercentage(), TARGET_IDB_PCT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 6));
						targetDXE.append(createDataExchange(covOptExt.getIncNoOfYears(), TARGET_IDB_YEARS, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 3));
					}
				}
			}
		}
		return targetDXE.toString();
	}
	/**
	 * Create billing (segments 33 and 35) DXE for the inforce submit
	 * 
	 * @param holding The Holding XML object
	 * @return java.lang.String The billing DXE information
	 */
	protected String createBillingInfo(Holding holding) { 
		// SPR3290 code deleted
		Policy policy = null; 
		// SPR3290 code deleted
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null; 
		Life life = null;
		LifeExtension lifeEx = null;
		Annuity annuity = null;
		PolicyExtension policyEx = null;
		
		String billingDXE = "";
		
		if (holding != null) {
			policy = holding.getPolicy();
			// SPR3290 code deleted
			policyEx = NbaUtils.getFirstPolicyExtension(policy);  //NBA104
			// NBA104 deleted code
		}
		
		if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) {
			lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); 
			if (lifeOrAnnuityOrDisabilityHealth.isLife())
				life = lifeOrAnnuityOrDisabilityHealth.getLife();
			else if (lifeOrAnnuityOrDisabilityHealth.isAnnuity()) {
				annuity = lifeOrAnnuityOrDisabilityHealth.getAnnuity();
			} else {
				if (getLogger().isWarnEnabled())
					getLogger().logWarn("Policy XML has no Life information");
				return "";
			}
		}
		
		if (life != null) {  //NBA104
			lifeEx = NbaUtils.getFirstLifeExtension(life);  //NBA104
		}
		
		if (policy != null) {
			//Special Freq extract mode DXE: FFCEXTMD
			//Table OLI_LU_PAYMODE
			if (policy.hasPaymentMode())
				if (getBillingType(policy) != 0){
					billingDXE = billingDXE + createDataExchange(policy.getPaymentMode(), INF_SP_FREQ_EXTRACT_MODE, NbaTableConstants.OLI_LU_PAYMODE, CYBTBL_UCT, CT_DEFAULT, 0);  //NBA104
				}
		}

		if (policyEx != null) {
			// NBA104 code deleted
			//DXE: FFCSFPDT
			if (policyEx.hasNonStandardPaidToDate())  
				billingDXE =
					billingDXE
						+ createDataExchange(policyEx.getNonStandardPaidToDate(), INF_SP_FREQ_PAID_TO_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12);
			//DXE : FFCPYDFR
			if (policyEx.hasPayrollFrequency())  
				billingDXE =
					billingDXE
						+ createDataExchange(policyEx.getPayrollFrequency(), SF_PAYROLL_FREQ, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12);
			//DXE: FBRSTSKM
			if (policyEx.hasFirstSkipMonth())  
				billingDXE =
					billingDXE
						+ createDataExchange(policyEx.getFirstSkipMonth(), INF_SP_FREQ_SKIP_MONTH, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12);
			//DXE: FFCQPBAS
			if (policyEx.hasQuotedPremiumBasisFrequency())  
				billingDXE =
					billingDXE
						+ createDataExchange(policyEx.getQuotedPremiumBasisFrequency(), SF_QUOTED_PREM_BASIS_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12);
			//DXE:FFCINBDT
			if (policyEx.hasInitialBillToDate())  
				billingDXE =
					billingDXE
						+ createDataExchange(policyEx.getInitialBillToDate(), SF_BILL_TO_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12);
			//DXE:FFCLDEDT
			if (policyEx.hasFirstMonthlyDate())  
				billingDXE =
					billingDXE
						+ createDataExchange(policyEx.getFirstMonthlyDate(), SF_FIRST_MONTHLY_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 12); 
			//begin NBA104
			if (policyEx.hasNonStandardBillAmt()) {
				if ((lifeEx != null && lifeEx.getPremType() == OLI_ANNPREM_FIXED)
					|| (annuity != null && annuity.getPremType() == OLI_ANNPREM_FIXED)) {
					//DXE:FFCSPREM
					billingDXE =
						billingDXE
							+ createDataExchange(policyEx.getNonStandardBillAmt(), SFC_SPECIAL_FREQ_PREM, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);

				} else if (
					(lifeEx != null && lifeEx.getPremType() == OLI_ANNPREM_FLEX) || (annuity != null && annuity.getPremType() == OLI_ANNPREM_FLEX)) {
					//DXE:FFCMPREM
					billingDXE =
						billingDXE
							+ createDataExchange(policyEx.getNonStandardBillAmt(), SFC_MONTHLY_PREM, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
					//DXE:FFCQPAMT
					if (policyEx.hasQuotedPremiumBasisAmt()) {
						billingDXE = billingDXE + createDataExchange(policyEx.getQuotedPremiumBasisAmt(), QUOTED_PREM_BASIS_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
					}
				}
			}
			//DXE:FFCTPAMT
			if (policyEx.hasQuotedPremiumBasisAmt()) {
				billingDXE = billingDXE + createDataExchange(policyEx.getQuotedPremiumBasisAmt(), SFC_TOTAL_PREM_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			}
			//end NBA104
		}
		billingDXE = billingDXE + createBillingControl(nbaTXLife, policy);		//SPR2151
		return billingDXE;
	}
	/**
	 * Create coverage (segment 02) DXE for the inforce submit
	 * 
	 * @param olife The OLifE XML object
	 * @param coverage The current Coverage XML object
	 * @param policy The Policy XML object
	 * @param life The Life XML object
	 * @return java.lang.String The coverage DXE for a life contract
	 * @exception throws NbaBaseException
	 */
	protected String createCoverage(OLifE olife, Coverage coverage, Policy policy, Life life) throws NbaBaseException {
		String covDXE = "";
		CoverageExtension coverageEx = null;
		// SPR3290 code deleted
		LifeParticipant lifeParticipant = null;
		String sPartyId = "";
		// SPR3290 code deleted
		Date issueDate = null;
		String relRoleCode = null;//SPR2200
		LifeParticipant jointLifePar = null;//SPR2200
		
		try {
			String phase = coverage.getCoverageKey().trim();
			Integer iPhase = new Integer(phase);

			coverageEx = NbaUtils.getFirstCoverageExtension(coverage); //SPR3290
			if (coverage.getLifeParticipantCount() > 0) {
				lifeParticipant = coverage.getLifeParticipantAt(0);
			}
			// SPR3290 code deleted

			// Coverage Key (Holding.Policy.Life.Coverage) DXE: FCVPHASE  
			covDXE = covDXE + createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), ANNU_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// NBA104 code deleted

			// Role of participant in coverage. (Holding.Policy.Life.Coverage.LifeParticipant) DXE: FCVPIDNT
			// Typecode UCT (CLPCTB11)  OLI_LU_PARTICROLE
			if (lifeParticipant != null) {
				relRoleCode =//SPR2200
					getCyberValue(lifeParticipant.getLifeParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
				sPartyId = getPartyId(lifeParticipant.getPartyID(), relRoleCode);
				// NBA104 deleted code
				covDXE = covDXE + createDataExchange(sPartyId, COVERAGE_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				// NBA104 code deleted
				//Age of participant when coverage was issued. DXE:FCVAGEIS - For specific person
				if (lifeParticipant.hasIssueAge()){
					covDXE = covDXE + createDataExchange(lifeParticipant.getIssueAge(),COV_ISSUE_AGE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
					covDXE = covDXE + createDataExchange("0",INF_COV_AGE_ADMITTED,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				}else{
					covDXE = covDXE + createDataExchange("2",INF_COV_AGE_ADMITTED,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				}

				//True Age DXE: FCVAGETR
				//begin NBA111 
				if (coverage.hasLivesType() && NbaUtils.isJointLife(coverage.getLivesType())) {
					if (lifeParticipant.hasIssueAge())
						covDXE =
							covDXE + createDataExchange(lifeParticipant.getIssueAge(), INF_COV_TRUE_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				} else { //end NBA111
					covDXE =
						covDXE
							+ createDataExchange(getAge(lifeParticipant.getPartyID(), olife), INF_COV_TRUE_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
				}// NBA111
 			}
			//begin NBA111
			if (coverage.hasLivesType() && NbaUtils.isJointLife(coverage.getLivesType())) { //SPR3036
				String prodCode = coverage.getProductCode();
				PolicyProduct policyProduct = null;
				PolicyProductExtension polProdEx = null;
				//SPR2200 deleted code
 				// SPR3290 code deleted
				jointLifePar=NbaUtils.getLifeParticipantWithRoleCode(coverage,NbaOliConstants.OLI_PARTICROLE_JOINT);
		
				//Joint True Age FCVJTAGE
				covDXE =
					covDXE
						+ createDataExchange(getAge(jointLifePar.getPartyID(), olife), INF_COV_JOINT_TRUE_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);

				//Joint Mortality Table FCVJTMTB
				
				policyProduct = getNbaProduct().getPolicyProduct(prodCode); //NBA237
				
				polProdEx = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
				if (coverageEx != null && coverageEx.hasRateClass() && jointLifePar != null && polProdEx != null) {
					long lifePartGender = getParticipantGender(jointLifePar.getPartyID(), olife);
					if (polProdEx.hasMortalityOrMorbidityTable()) {
						String ageCalc;
						String rateClass = coverageEx.getRateClass();
						if (policyProduct.hasAgeCalculationType()) {
							ageCalc = String.valueOf(policyProduct.getAgeCalculationType());
						} else {
							ageCalc = "-";
						}
						String sGender = String.valueOf(lifePartGender);
						String gMortality =
							getCyberValue(polProdEx.getMortalityOrMorbidityTable(), NbaTableConstants.OLI_LU_MORTALITYTBL, CYBTBL_UCT, compCode, DEFAULT_COVID); //SPR1633
						mortalityData = (NbaMortalityData[]) getMortalityTable("*", gMortality, ageCalc, sGender, rateClass);
						if (mortalityData.length == 1) {
							covDXE =
								covDXE
									+ createDataExchange(
										mortalityData[0].getSpecificMortality(),
										INF_COV_JOINT_MORTALITY_TABLE,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_DEFAULT,
										0);
						} else {
							covDXE =
								covDXE
									+ createDataExchange(
										polProdEx.getMortalityOrMorbidityTable(),
										INF_COV_JOINT_MORTALITY_TABLE,
										NbaTableConstants.OLI_LU_MORTALITYTBL,	//SPR1633
										CYBTBL_UCT,
										CT_DEFAULT,
										0);
						}
					}
				}
			}
			//end NBA111
		
			//set flag bytes
			//Indicator that the coverage is expressed in a number of units instead of an amount.  Used to display the returned value differently. DXE:FCVFLGB0
			if (isAP){
				covDXE = covDXE + createDataExchange("1","FCVFLGA0",CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0); 
			}
		  	
			if ((coverageEx !=null) && (coverageEx.hasUnitTypeInd())){
				covDXE = covDXE + createDataExchange("1","FCVFLGB0",CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			}
			//SPR1731
			if (iPhase.intValue() == 1) {
				// FCVFLGD0
				covDXE = covDXE + createDataExchange("1",COV_IND_CODE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			}
			//End SPR1731
			if (policy.hasApplicationInfo())
				if (policy.getApplicationInfo().hasHOAppFormNumber())
					// Number assigned to application form type. (Holding.Policy.ApplicationInfo) DXE: FCVPLFNO
					covDXE =
						covDXE
							+ createDataExchange(
								policy.getApplicationInfo().getHOAppFormNumber(),
								APP_FORM_TYPE_NUM_ASSN,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_CHAR,
								10);

			// Coverage number of Units (Holding.Policy.Life.Coverage) DXE: FCVUNITS
			// FCVSPAMT for UL
			if (coverage != null) { 
				covDXE = covDXE + createDataExchange(coverage.getCurrentNumberOfUnits(), COV_CURR_AMT_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (policy.getProductType() == OLI_PRODTYPE_VUL || policy.getProductType() == OLI_PRODTYPE_UL) {
					covDXE = covDXE + createDataExchange(coverage.getCurrentNumberOfUnits(), "FCVSPAMT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					covDXE = covDXE + createDataExchange("0", INF_COV_CORRIDOR_RIDER_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
			}

			//SPR2724 code deleted
			
			if (coverage.hasPremiumPerUnit())
				covDXE = covDXE + createDataExchange(coverage.getPremiumPerUnit(), INF_COV_ANN_PREM, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			// NBA104 code deleted
			//FCVNFOPT				
			covDXE = covDXE + createDataExchange(life.getNonFortProv(), "FCVNFOPT", CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			// begin NBA104
			//FCVCVYR
			covDXE = covDXE + createDataExchange("0", INF_COV_CASH_VAL_YEAR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// end NBA104
			
			if (coverage != null){
				//begin SPR2319
				//Status of Coverage DXE:FCVFLGB5 and FCVNCTYP
				if (coverage.hasLifeCovStatus()) {
					String status =
						getCyberValue(coverage.getLifeCovStatus(), NbaTableConstants.NBA_COVERAGE_STATUS, CYBTBL_UCT, compCode, DEFAULT_COVID);
					if (status.length() < 2) { //if status BES value less than 2 then invalid status is 21
						status = INVALID_COVERAGE_STATUS;
					}
					covDXE = covDXE + createDataExchange(status.substring(0, 1), COV_STATUS, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					covDXE = covDXE + createDataExchange(status.substring(1, 2), COV_STATUS_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
				//end SPR2319
				//Type code indicating description of the relation between the participants and the coverage DXE:FCVLIVES
				if (coverage.hasLivesType())
					covDXE = covDXE + createDataExchange(coverage.getLivesType(),COV_LIVES_TYPE,"OLI_LU_LIVESTYPE",CYBTBL_UCT,CT_DEFAULT,0);
				//FCVSEX
				covDXE = covDXE + createDataExchange(getSex(lifeParticipant.getPartyID(),olife), INF_COV_SEX,CYBTRANS_NONE,CYBTBL_NONE, CT_CHAR, 1); 
				// NBA104 code deleted 
				
				if (coverage.hasEffDate()){
					issueDate = coverage.getEffDate();
				}else{
					issueDate = new Date();
				} 
				
				//Maturity date is needed to put contract on CyberLife host
				matDate = coverage.getTermDate();	//SPR1986
				//SPR1986 code deleted
				//FCVTCGDT next change or cease date.
				covDXE = covDXE + createDataExchange(matDate, COV_TERM_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//FCVMATDT date the coverage will mature or expire. For Renewable Term, contains the expiry date of the current renewal period.				
				covDXE = covDXE + createDataExchange(matDate, INF_COV_MAT_EXP_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//SPR1986 code deleted
					
				//The effective start date of the contract. 
				// Date at which an insurance policy goes into force.  This date may be different from the Date of Issue. DXE:FCVISSDF
				//These coverage dates are needed for a record to be build on CyberLife
				//Issue date DXE:FCVISSDF
				if (coverage.hasEffDate()){
					covDXE = covDXE + createDataExchange(coverage.getEffDate(),COV_EFF_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				}else {
					covDXE = covDXE + createDataExchange(issueDate,COV_EFF_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				}
			}

			// For coverage phase 1.  Only applicable if Product Type = 'U' - Death Benefit Option (Holding.Policy.Life.Coverage) DXE: FULPLOPT
			// Typecode UCT (CLPCT214)  OLI_LU_DTHBENETYPE
			if (iPhase.intValue() == 1 && (policy.getProductType() == VAR_UNIV_LIFE || policy.getProductType() == UNIV_LIFE)) //SPR1058 //SPR1018
				APDXE =
					APDXE
						+ createDataExchange(
							coverage.getDeathBenefitOptType(),
							DEATH_BENEFIT_OPT,
							NbaTableConstants.OLI_LU_DTHBENETYPE,
							CYBTBL_UCT,
							CT_CHAR,
							1);

			// Form Number (Holding.Policy.Life.Coverage) DXE: FCVPLFNO
			if (coverage != null) { 
				covDXE = covDXE + createDataExchange(coverage.getFormNo(), APP_FORM_TYPE_NUM_ASSN, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 10); //NBA093
			}

			// FCVFLAGA bit 3  ISS045 Exempt from Guideline XXX (Reentry) - Guideline xxx Exempt Indicator (Holding.Policy.Life.Coverage) DXE: REENTRY
			//covDXE = covDXE + createDataExchange( coverageEx.getGuidelineExemptInd(), GUIDELINE_XXX_EXEMPT_INDICATOR, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1 ); 

			// Coverage Product Code (Holding.Policy.Life.Coverage) DXE: FCVPDSKY
			// Character UCT (PLN_NME)  nbA Table = NBA_PLANS
			productCode = coverage.getProductCode();
			if (validPlanRider(coverage.getProductCode())){
				//DXE:FCVPROD
				covDXE = covDXE + createDataExchange(coverage.getLifeCovTypeCode(), COV_TYPE_CODE_1, "OLI_LU_COVTYPE", CYBTBL_UCT, CT_CHAR, 1);
				covDXE = covDXE + createDataExchange(coverage.getProductCode(), COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
				covDXE = covDXE + createDataExchange(coverage.getProductCode(), INF_COV_RATE_BOOK, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
			}else
				covDXE = covDXE + createDataExchange("", COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);

			// save product code for processing investments
			if (iPhase.intValue() == 1) {
				//productCode = getCyberValue(coverage.getProductCode(), CYBTRANS_PLAN, CYBTBL_PLAN_COV_KEY, compCode, DEFAULT_COVID);
				productCode = coverage.getProductCode();
				termDate = coverage.getTermDate();
			}

			if (coverageEx != null) {

				// Unisex Override Indicator (Holding.Policy.Life.Coverage) DXE: FCVUOIND
				// VT_I4 (Typecode)  OLIEXT_LU_BOOLEAN Current Extension
				covDXE = covDXE + createDataExchange(coverageEx.getUnisexOverride(), UNISEX_OVERRIDE_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);

				// Unisex Override Code (Holding.Policy.Life.Coverage) DXE: FCVUSEXC
				// VT_I4 (Typecode) UCT (CLPCT209)  OLIEXT_LU_UNISEXCODE
				covDXE =
					covDXE
						+ createDataExchange(
							coverageEx.getUnisexCode(),
							UNISEX_OVERRIDE_CD,
							NbaTableConstants.OLIEXT_LU_UNISEXCODE,
							CYBTBL_UCT,
							CT_CHAR,
							1);

				// Unisex Override Subseries (Holding.Policy.Life.Coverage) DXE: FCVUSUBC
				// VT_I4 (Typecode) UCT (CLPCTB85)  OLIEXT_LU_UNISEXSUBSER
				covDXE =
					covDXE
						+ createDataExchange(
							coverageEx.getUnisexSubseries(),
							UNISEX_OVERRIDE_SUBSERIES,
							NbaTableConstants.OLIEXT_LU_UNISEXSUB,
							CYBTBL_UCT,
							CT_CHAR,
							3);
				
				//Commission Plan Code DXE:FCVCMCCD
				if (coverageEx.hasCommissionPlanCode())
					covDXE = covDXE + createDataExchange(coverageEx.getCommissionPlanCode(),INF_COV_COMM_CODE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);//SPR1659
				
				//Rate class DXE:FCVRTCLS  
				if (coverageEx.hasRateClass()){
					covDXE = covDXE + createDataExchange(coverageEx.getRateClass(),INF_COV_RATE_CLASS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1); //SPR1778 removed translation
					// NBA104 code deleted
				}
				
				if (coverageEx.hasValuationClassType()){
					classSeriesFound = true;
					if (coverageEx.getValuationClassType()!=7){
						covDXE = covDXE + createDataExchange(coverageEx.getValuationClassType(),INF_COV_CLASS,"OLI_LU_VALCLASS",CYBTBL_UCT,CT_DEFAULT,1);
					}
					covDXE = covDXE + createDataExchange(coverageEx.getValuationBaseSeries(),INF_COV_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
					covDXE = covDXE + createDataExchange(coverageEx.getValuationSubSeries(),INF_COV_SUB_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0); 
				}
				//FCVPAYDF - payup date
				//If pay up date is not available move default date from middle tier spreadsheet. Should not be neededin future revisions
				if (coverageEx.hasPayUpDate())
					covDXE = covDXE + createDataExchange(coverageEx.getPayUpDate(),INF_COV_PAYUP_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0); 
				else
					covDXE = covDXE + createDataExchange(matDate,INF_COV_PAYUP_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0); 
				// NBA104 code deleted	
			}else{
				covDXE = covDXE + createDataExchange(matDate,INF_COV_PAYUP_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0); 
			}	

			// Only applicable if Product Type Code = 'I', 'N' or 'O' - Primary Dividend Option (Holding.Policy.Life) DXE: FCVRODIV 
			// Typecode UCT (CEUDT123)  OLI_LU_DIVTYPE
			if (life.hasDivType())
				covDXE = covDXE + createDataExchange(life.getDivType(), PRIM_DIV_OPT, NbaTableConstants.OLI_LU_DIVTYPE, CYBTBL_UCT, CT_CHAR, 1);

			// 1035 Exchange Indicator (Holding.Policy.Life.LifeUSA) DXE: FCV1035X
			if (life.hasLifeUSA())
				if (life.getLifeUSA().hasInternal1035())
					covDXE =
						covDXE + createDataExchange(life.getLifeUSA().getInternal1035(), POL_1035_EXCH_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);

			//			begin SPR1878
			//Deleted two lines
					  //DXE: FULFLGD6
					  if(life.hasInitialPremAmt() && life.getInitialPremAmt() > 0){
						covDXE = covDXE + createDataExchange("1", "FULFLGD6", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
					  }
					  //end SPR1878
			
			// NFO Option (Top Level) DXE: FCVRONFO
			// Typecode UCT (CLDET014)  OLI_LU_NONFORTPROV
			if (life.hasNonFortProv())
				covDXE =
					covDXE + createDataExchange(life.getNonFortProv(), NFO_OPTION_B, NbaTableConstants.OLI_LU_NONFORTPROV, CYBTBL_UCT, CT_DEFAULT, 0);
			//SPR1633
			if (coverageEx != null && coverageEx.hasRateClass() && lifeParticipant != null) {
				long partGender = getParticipantGender(lifeParticipant.getPartyID(),olife);
				covDXE = covDXE + createPolicyProduct(olife,productCode,2,"", true, partGender, coverageEx.getRateClass(), coverage); //SPR3162
			} else
				covDXE = covDXE + createPolicyProduct(olife,productCode,2,"");
			/* decreasing term
			 * For Olife Class 7, Adapter must also look at PolicyProduct.ValuationClassFreq 
			 * if 1 set FCVCLASS TO BES=6 ELSE BES=7
			 */
	
			if (( coverageEx != null) && (coverageEx.getValuationClassType()==7)){
				if (valuationFreq == 1){
					covDXE = covDXE + createDataExchange(6,INF_COV_CLASS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
				}else{
					covDXE = covDXE + createDataExchange(7,INF_COV_CLASS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
				}
			}
			// begin NBA104
			// FCVREFAG
			if (policy.getProductType() == OLI_PRODTYPE_INDETERPREM) {
				covDXE = covDXE + createDataExchange("0", INF_COV_REFRESH_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			} else if (coverage.getRenewableInd() && coverageEx != null) {
				covDXE = covDXE + createDataExchange(coverageEx.getRenewalAge(), INF_COV_REFRESH_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			} else if (lifeParticipant != null) {
				covDXE = covDXE + createDataExchange(lifeParticipant.getIssueAge(), INF_COV_REFRESH_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			}
			// end NBA104
			//Default values from middle tier spreadsheet. Will not be needed when all products have CPD information
			if (!classSeriesFound){
				covDXE = covDXE + createDataExchange("1",INF_COV_CLASS,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				covDXE = covDXE + createDataExchange("",INF_COV_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
				covDXE = covDXE + createDataExchange("",INF_COV_SUB_SERIES,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			}
			//begin SPR1738
			if (lifeParticipant != null) {
				covDXE = covDXE
						+ createRatingForLifeParticipant(lifeParticipant, coverage.getTermDate(), sPartyId, zeroPadString(coverage.getCoverageKey(),
								2)); 
			}
			//end SPR1738

			covDXE = covDXE + createSubstandardRating(lifeParticipant, zeroPadString(coverage.getCoverageKey(), 2), sPartyId, coverage.getTermDate(), olife); //SPR1970  //NBA104
			//begin SPR2200
			if (coverage.hasLivesType() && NbaUtils.isJointLife(coverage.getLivesType())) { //SPR3036
				relRoleCode = getCyberValue(jointLifePar.getLifeParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
				sPartyId = getPartyId(jointLifePar.getPartyID(), relRoleCode);
				covDXE = covDXE
				+ createRatingForLifeParticipant(jointLifePar, coverage.getTermDate(), sPartyId, zeroPadString(coverage.getCoverageKey(), 2)); //SPR1738

				covDXE =
					covDXE
						+ createSubstandardRating(jointLifePar, zeroPadString(coverage.getCoverageKey(), 2), sPartyId, coverage.getTermDate(), olife);
				
			}//end SPR2200
	
			// NBA104 code deleted			
				
			for (int i = 0; i < coverage.getCovOptionCount(); i++) {
				CovOption covOption = coverage.getCovOptionAt(i);
				benefitDXE = benefitDXE + createCovOption(covOption, olife,coverage); //NBA111
				if (lifeParticipant != null)
					//DXE: FSBBAGE
				benefitDXE = benefitDXE + createDataExchange(lifeParticipant.getIssueAge(), INF_COV_OPT_ISSUE_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); 
			// SPR1986 code deleted
			// NBA104 code deleted
			}
			
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_COVERAGES, e);
		}

		return covDXE; 
	}

	/**
	 * Create Tax information for inforce build.
	 * 
	 * @param holding The Holding XML object
	 * @return java.lang.String The tax related DXE information
	 */
	protected String createTaxInfo(Holding holding) { 
	
		String[][] taxDxeTags = {{"FAUWHIND","FAUSWHND"},{"FAUWHPCT","FAUSWHPC"},{"FAUWHPAF","FAUSWAGF"}};
		String taxDXE = "";

		for (int i = 0; i < holding.getArrangementCount(); i++) {
			Arrangement arrangement = holding.getArrangementAt(i);
	 
			for (int j = 0; j < arrangement.getTaxWithholdingCount(); j++) {
				TaxWithholding taxWithholding = arrangement.getTaxWithholdingAt(j);
				int taxId =  (int) taxWithholding.getTaxWithholdingPlace() - 1 ;
				
				if (taxId < 0)
					break;
		
				//Federal/State Withholding Indicator DXE:FAUWHIND or FAUSWHND 
				if (taxWithholding.hasTaxWithholdingType())
			
					taxDXE = taxDXE + createDataExchange(taxWithholding.getTaxWithholdingType(),taxDxeTags[0][taxId],"OLI_LU_WITHCALCMTH",CYBTBL_UCT,CT_DEFAULT,0);
		
				if (taxWithholding.hasTaxWithheldPct())
			
					taxDXE = taxDXE + createDataExchange(taxWithholding.getTaxWithheldPct(),taxDxeTags[1][taxId],CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
		
				//Federal/State Withholding Flat Amount DXE:FAUWHPAF OR FAUSWAGF 
				if (taxWithholding.hasTaxWithheldAmt())
					taxDXE = taxDXE + createDataExchange(taxWithholding.getTaxWithheldAmt(),taxDxeTags[2][taxId],CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0);
			}
		}
		
		return taxDXE;
	}

	//SPR1970 Method createFirstRatingInfo deleted
	/**
	 * Create DXE for second occurrence of a rating dxe
	 * 
	 * @param lifeParticipant The LifeParticipant XML oject
	 * @param phase The current coverage phase code
	 * @param PersonCode The current coverage person code
	 * @param termDate The current coverage termination date
	 * @param olife The OLifE XML object
	 * @return java.lang.String The rating DXE information
	 */
	//SPR1970 Changed method name to createSubstandardRating
	// NBA104 added termDate parameter
	protected String createSubstandardRating(LifeParticipant lifeParticipant,String phase,String personCode,Date termDate,OLifE olife) throws NbaBaseException { 
	
	SubstandardRating substandardRating = null;
	
	String ratingDXE = "";
	//begin SPR1879
	if (lifeParticipant.getSubstandardRatingCount() <= 0){
		return "";
	}
	//end SPR1879
	
	//SPR1731 Need to loop to pick up 3-n
	for (int i=0; i < lifeParticipant.getSubstandardRatingCount(); i++ ) {
		if (lifeParticipant.getSubstandardRatingCount() > 0){
			substandardRating = lifeParticipant.getSubstandardRatingAt(i); 
		}else {
			return "";
		}
		
		String ratingType = getRatingType(substandardRating, true);  //SPR1549
		//ratingType = getCyberValue(ratingType,NbaTableConstants.OLIEXT_LU_RATINGTYPE, CYBTBL_UCT, compCode, DEFAULT_COVID);
							
		char ratingChar = ratingType.charAt(0);
		SubstandardRatingExtension substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating); 
		if (substandardRatingExt == null) // there is no rating extension so return to coverage
			return "";

		// begin NBA104
		ratingDXE = ratingDXE + createDataExchange(personCode, POLICY_PERS_SEQ, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		ratingDXE = ratingDXE + createDataExchange(phase, SUB_STAND_COV_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		ratingDXE =
			ratingDXE
				+ createDataExchange(formatCyberDate(substandardRatingExt.getEffDate()), SUB_STAND_START_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		ratingDXE = ratingDXE + createDataExchange(ratingType, SUB_STAND_RATE_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
		// The value of the custom property DXE:FELSBTYP
		if (substandardRatingExt.hasExtraPremSubtype()) {
			ratingDXE =
				ratingDXE
					+ createDataExchange(substandardRatingExt.getExtraPremSubtype(), SUB_STAND_RATE_SUBTYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		}
		// end NBA104
		switch (ratingChar) {
			case SUB_STAND_TYPE_PERM_TABLE :
				// permanent table
				ratingDXE = ratingDXE + createDataExchange(substandardRating.getPermTableRating(),SUB_STAND_TABLE_RATE, NbaTableConstants.OLI_LU_RATINGS, CYBTBL_UCT, CT_CHAR, 2);
				// NBA104 deleted code
				// begin NBA104
				ratingDXE = ratingDXE + createDataExchange(substandardRatingExt.getPermPercentageLoading(), SUB_STAND_PERCENT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				ratingDXE = ratingDXE + createDataExchange(formatCyberDate(termDate),SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				ratingDXE = ratingDXE + createDataExchange(formatCyberDate(termDate),SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				// end NBA104	
				break;
			case SUB_STAND_TYPE_PERM_FLAT :
				// permanent flat extra
				ratingDXE = ratingDXE + createDataExchange(formatCyberDouble(substandardRatingExt.getPermFlatExtraAmt()),SUB_STAND_FLAT_EXTRA, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				// NBA104 deleted code
				// begin NBA104
				if (substandardRatingExt.hasExtraPremPerUnit()) { 
					ratingDXE = ratingDXE + createDataExchange(formatCyberDouble(substandardRatingExt.getExtraPremPerUnit()),SUB_STAND_EXTRA_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				} else {
					ratingDXE = ratingDXE + createDataExchange(formatCyberDouble(substandardRatingExt.getPermFlatExtraAmt()),SUB_STAND_EXTRA_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				}
				ratingDXE = ratingDXE + createDataExchange(formatCyberDate(termDate),SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				ratingDXE = ratingDXE + createDataExchange(formatCyberDate(termDate),SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				// end NBA104
				break;
			case SUB_STAND_TYPE_TEMP_TABLE :
				// temporary table
				ratingDXE = ratingDXE + createDataExchange(substandardRating.getTempTableRating(),SUB_STAND_TABLE_RATE, NbaTableConstants.OLI_LU_RATINGS, CYBTBL_UCT, CT_CHAR, 2);
				// NBA104 deleted code
				ratingDXE = ratingDXE + createDataExchange(substandardRatingExt.getTempPercentageLoading(), SUB_STAND_PERCENT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);  //NBA104
				if (substandardRating.hasTempTableRatingEndDate()) { 
					ratingDXE = ratingDXE + createDataExchange(formatCyberDate(substandardRating.getTempTableRatingEndDate()),SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
					ratingDXE = ratingDXE + createDataExchange(formatCyberDate(substandardRating.getTempTableRatingEndDate()),SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);  //NBA104
					// NBA104 deleted code
				}
				break;
			case SUB_STAND_TYPE_TEMP_FLAT :
				// temporary flat extra
				ratingDXE = ratingDXE + createDataExchange(formatCyberDouble(substandardRating.getTempFlatExtraAmt()),SUB_STAND_FLAT_EXTRA, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				// NBA104 deleted code
				if (substandardRating.hasTempFlatEndDate()) {
					ratingDXE = ratingDXE + createDataExchange(formatCyberDate(substandardRating.getTempFlatEndDate()),SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
					ratingDXE = ratingDXE + createDataExchange(formatCyberDate(substandardRating.getTempFlatEndDate()),SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);  //NBA104
					// NBA104 deleted code
				}
				// begin NBA104
				if (substandardRatingExt.hasExtraPremPerUnit()) { 
					ratingDXE = ratingDXE + createDataExchange(formatCyberDouble(substandardRatingExt.getExtraPremPerUnit()),SUB_STAND_EXTRA_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				} else {
					ratingDXE = ratingDXE + createDataExchange(formatCyberDouble(substandardRating.getTempFlatExtraAmt()),SUB_STAND_EXTRA_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				}
				// end NBA104
				break;
			default :
				throw new NbaBaseException("Invalid rating type selected");
			} //end switch

			// NBA104 deleted code	
			ratingDXE = ratingDXE + createDataExchange(substandardRating.getRatingReason(),SUB_STAND_REASON, NbaTableConstants.NBA_RATING_REASON, CYBTBL_UCT, CT_CHAR, 1);
			ratingDXE = ratingDXE +createDataExchange(substandardRating.getRatingCommissionRule(),SUB_STAND_COMM, NbaTableConstants.OLIEXT_LU_COMMISCODE, CYBTBL_UCT, CT_CHAR, 1);
			ratingDXE = ratingDXE + createDataExchange(substandardRatingExt.getRatingStatus(),SUB_STAND_RATE_STATUS, NbaTableConstants.NBA_ACTIVE_SEGMENT_STATUS, CYBTBL_UCT, CT_CHAR, 1);
			// NBA104 deleted code	

			ratingDXE = ratingDXE + createPolicyProduct(olife,productCode,3,""); //SPR1879  //NBA104
			
		} //SPR1731
		return ratingDXE;  //NBA104
	}

	/**
	 * Create reinsurance information
	 * 
	 * @param coverage The current Coverage XML object
	 * @return java.lang.String The reinsurance DXE information
	 */
	protected String createReinsuranceInfo(Coverage coverage) { 
		String reinsuranceDXE = "";;
		ReinsuranceInfo reinsuranceInfo = null;
		
		for (int i = 0;i < coverage.getReinsuranceInfoCount();i++){;
			coverage.getReinsuranceInfoAt(i);
			//DXE: FRECPHS
			reinsuranceDXE = reinsuranceDXE + createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), INF_REINS_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//DXE: FRECEXP
			if (reinsuranceInfo.hasRetentionPct())
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getRetentionPct(),INF_REINS_PCT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//DXE: FRECRID
			if (reinsuranceInfo.hasReinsurersTreatyIdent())
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getReinsurersTreatyIdent(),INF_REINS_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//DXE: FRECRTY
			if (reinsuranceInfo.hasReinsuranceRiskBasis())
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getReinsuranceRiskBasis(),INF_REINS_RISKBASIS, "OLI_LU_REINRISKBASE", CYBTBL_UCT, CT_CHAR, 2);
			//DXE: FREEFFDT
			if (reinsuranceInfo.hasReinsuranceEffDate())
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getReinsuranceEffDate(),INF_REINS_EFF_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//DXE: FRECESDT
			if (reinsuranceInfo.hasReinsurancePaidUpToDate())
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getReinsurancePaidUpToDate(),INF_REINS_PAID_UP_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//DXE: FRECAMT & FRECRUSE
			if (reinsuranceInfo.hasRetentionAmt()){
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getRetentionAmt(),INF_REINS_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				reinsuranceDXE = reinsuranceDXE + createDataExchange("2",INF_REINS_USE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			}else if (reinsuranceInfo.hasReinsuredAmt()){
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getReinsuredAmt(),INF_REINS_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				reinsuranceDXE = reinsuranceDXE + createDataExchange("1",INF_REINS_USE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			}
			//DXE: FRECEXAL
			if (reinsuranceInfo.hasModalExpenseAllowanceAmt())
				reinsuranceDXE = reinsuranceDXE + createDataExchange(reinsuranceInfo.getModalExpenseAllowanceAmt(),INF_REINS_EXP_ALLOWANCE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			
		}
		return reinsuranceDXE;
	}

	/**
	 * Create the fund data from the policy product information
	 *
	 * @param policyProduct The current PolicyProduct XML object
	 * @param fundType The current Fund id to retrieve from the policy product object
	 * @return java.lang.String The fund DXE information obtained from the policy product information
	 */
	protected String createPolicyProductFunds(PolicyProduct policyProduct, String fundType) { 
		
		OLifEExtension olifeEx = null;
		// SPR3290 code deleted
		String DXE = "";
		// SPR3290 code deleted
		InvestProductInfo investProductInfo = null;
		InvestProductInfoExtension investProductInfoEx = null;
		boolean fundFound = false;
		MinBalanceCalcRule minBalanceCalcRule = null;
		RateVariation rateVariation = null;
		RateVariationByDuration rateVariationByDuration = null;
		//begin SPR2582
		Object[] featureProducts= null;
		FeatureProduct featureProduct= null;
		FeatureProductExtension featureProductEx= null;
		// SPR3290 code deleted
		int featureProductCount = 0;
		//end SPR2582
		int investCount = policyProduct.getInvestProductInfoCount();
		for (int i = 0; i < investCount; i++){
			investProductInfo = policyProduct.getInvestProductInfoAt(i);
			if (investProductInfo.hasProductCode() && investProductInfo.getProductCode().equals(fundType)){
				fundFound = true;
				break;
			}
		}
		if (!fundFound){
			return "";
		}
		investProductInfoEx  = AccelProduct.getFirstInvestProductInfoEx(investProductInfo); //NBA237
			
		//begin SPR2582
		if (investProductInfoEx != null) {
			featureProductCount = investProductInfoEx.getFeatureProductCount();
			if (featureProductCount > 0)
				featureProducts = investProductInfoEx.getFeatureProduct().toArray();
		}
		//begin SPR2970
		//Fund id DXE: FIFFNDID
		DXE = DXE + createDataExchange(investProductInfo.getProductCode(),INF_FUND_ID,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);		 
		//Coverage Phase DXE: FIFCOVPH
		DXE = DXE + createDataExchange("01",INF_FUND_COV_PHASE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
		//end SPR2970

		for (int i = 0; i < featureProductCount; i++) {
			featureProduct = (FeatureProduct) featureProducts[i];
			featureProductEx = AccelProduct.getFirstFeatureProductExtension(featureProduct); //NBA237

			// SPR3290 code deleted
			if (featureProductEx != null) { //all conditions of case are dependent on featureProductEx being present.
				switch ((int) featureProduct.getArrType()) {
					case 1 :
						/* FIFDTRNF value of '0' if ArrType tc = "1"
						AllowedOptionInd tc = "0" for do not allow
						
						FIFDTRNF value of '1' if ArrType tc = "1".
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "1" for LIFO
						
						FIFDTRNF value of '2' if ArrType tc = "1"
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "5" for FIFO
						
						FIFDTRNF value of '3' if ArrType tc = "1".
						AllowedInd tc = "1" for allow 
						InterestInvestCreditType tc = "100050001" transfers to Internal Account
						
						FIFDTRNF value of '4' if ArrType tc = "1"
						AllowedInd tc = "1" for allow 
						InterestInvestCreditType tc = "100050002" transfers to New Money*/

						String transferCode = "0"; //not allowed
						if (featureProductEx.getAllowedInd()) {
							if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_LIFO_FUND) { //LIFO
								transferCode = "1";
							} else if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_FIFO) { //FIFO
								transferCode = "2";
							} else if (
								featureProductEx.getInterestInvestCreditType() == OLIEXT_LU_INTINVCREDITTYP_INTERNAL) { //Xfer to internal account
								transferCode = "3";
							} else if (featureProductEx.getInterestInvestCreditType() == OLIEXT_LU_INTINVCREDITTYP_NEW_MONEY) { //New money
								transferCode = "4";
							}
						}
						DXE = DXE + createDataExchange(transferCode, INF_FUND_ALLOW_TRANSFER_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);

						break;
					case 49 :
						/*FIFDWDRW value of '0' if ArrType tc = "49" for withdrawals
						AllowedOptionInd tc = "0" for do not allow
							
						FIFDWDRW value of '1'if ArrType tc = "49" 
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "6" for LIFO Post-Tefra
						DebtAllowedInd tc = "1" for debt allowed
						
						FIFDWDRW value of '2' if ArrType tc = "49" 
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "7" for FIFO post-TEFRA
						DebtAllowedInd tc = "1" for debt allowed
						
						FIFDWDRW value of 'A' if ArrType tc = "49" 
						AllowedInd tc = "1" for allow 
						WithdrawalOrder tc = "6" for LIFO Post-Tefra
						DebtAllowedInd tc = "0" no debt allowed
						
						FIFDWDRW value of 'B' if ArrType tc = "49" 
						AllowedInd tc = "1" for allow activity type
						WithdrawalOrder tc = "7" for FIFO post-TEFRA
						DebtAllowedInd tc = "0" no debt allowed*/
						String withdrawCode = "0"; //not allowed
						if (featureProductEx.getAllowedInd()) {
							if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_LIFO_POST_TEFRA && featureProductEx.getDebtAllowedInd()) {
								withdrawCode = "1";
							} else if (
								featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_FIFO_POST_TEFRA && featureProductEx.getDebtAllowedInd()) {
								withdrawCode = "2";
							} else if (
								featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_LIFO_POST_TEFRA && !featureProductEx.getDebtAllowedInd()) {
								withdrawCode = "3";
							} else if (
								featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_FIFO_POST_TEFRA && !featureProductEx.getDebtAllowedInd()) {
								withdrawCode = "4";
							}
						}
						DXE = DXE + createDataExchange(withdrawCode, INF_FUND_ALLOW_WITHDRAW_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);

						break;
					case 50 :
						/*FIFDLOAN value of '0' if ArrType tc = "50" for loans
						AllowedInd tc = "0" for do not allow
						
						FIFDLOAN value of '1' if ArrType tc = "50" for loans
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "1" for LIFO
						
						FIFDLOAN value of '2' if ArrType tc = "50" for loans
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "5" for FIFO*/
						String loanCode = "0"; //not allowed
						if (featureProductEx.getAllowedInd()) {
							if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_LIFO_FUND) {
								loanCode = "1";
							} else if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_FIFO) {
								loanCode = "2";
							}
						}
						DXE = DXE + createDataExchange(loanCode, INF_FUND_ALLOW_LOAN_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
						break;
					case 51 :
						/*FIFDLINT value of '0' if ArrType tc = "51" for cap loan interest,
						AllowedInd tc = "0" for do not allow
						
						FIFDLINT value of '1' if ArrType tc = "51".
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "1" LIFO
						
						FIFDLINT value of '2' if ArrType tc = "51"
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "5" for FIFO*/
						String loanIntCode = "0"; //not allowed
						if (featureProductEx.getAllowedInd()) {
							if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_LIFO_FUND) {
								loanIntCode = "1";
							} else if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_FIFO) {
								loanIntCode = "2";
							}
						}
						DXE = DXE + createDataExchange(loanIntCode, INF_FUND_ALLOW_LOAN_INT_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
						break;
					case 52 :
						/*FIFDCHRG value of '0' if ArrType tc = "52" for charge deduction,
						AllowedOptionInd tc = "0" for do not allow
						
						FIFDCHRG value of '1' if ArrType tc = "52".
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "1" for LIFO
						AllowedOverrides tc = "0" No Overrides Allowed
						
						FIFDCHRG value of '2' if ArrType tc = "52"
						AllowedInd tc = "1" for allow
						WithdrawalOrder tc = "5" for FIFO
						AllowedOverrides tc = "0" No Overrides Allowed
						
						FIFDCHRG value of '3' if ArrType tc = "52"
						AllowedInd tc = "1" for allow 
						WithdrawalOrder tc = "1" for LIFO
						AllowedOverrides tc = "1" Requested by Policyholder
						
						FIFDCHRG value of '4' if ArrType tc = "52"
						AllowedInd tc = "1" for allow activity type
						WithdrawalOrder  tc = "5" for FIFO 
						AllowedOverrides tc = "1" Requested by Policyholder*/
						String chargeCode = "0"; //not allowed
						if (featureProductEx.getAllowedInd()) {
							if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_LIFO_FUND && featureProductEx.getAllowedOverrides()) {
								chargeCode = "3";
							} else if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_FIFO && featureProductEx.getAllowedOverrides()) {
								chargeCode = "4";
							} else if (
								featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_LIFO_FUND && !featureProductEx.getAllowedOverrides()) {
								chargeCode = "1";
							} else if (featureProductEx.getWithdrawalOrder() == OLI_LU_WDORDER_FIFO && !featureProductEx.getAllowedOverrides()) {
								chargeCode = "2";
							}
						}
						DXE = DXE + createDataExchange(chargeCode, INF_FUND_ALLOW_CHARGES_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
						break;
				}
			}
		} //end for
		//end SPR2582	
		// SPR2970 code deleted
		if (investProductInfoEx != null){
			//Fund type DXE: FIFFNDTY
			if (investProductInfoEx.hasRateType())  //SPR1633
				DXE = DXE + createDataExchange(investProductInfoEx.getRateType(),INF_FUND_TYPE,"OLI_LU_RATETYPE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
			//Fund qualification DXE: FIFFNDQL
			DXE = DXE + createDataExchange(investProductInfoEx.getQualifiedCode(), INF_FUND_QUALIFICATION, NbaTableConstants.OLI_LU_QUALIFIED, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
			if (investProductInfoEx.getMinBalanceCalcRuleCount() > 0){
				minBalanceCalcRule = investProductInfoEx.getMinBalanceCalcRuleAt(0);
				//Min Bal table code DXE: FIFMBTBL
				DXE = DXE + createDataExchange(minBalanceCalcRule.getTableIdentity(),INF_FUND_MIN_BAL_TBL_CODE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
				//Min Bal initial Rule id DXE: FIFMBINL
				DXE = DXE + createDataExchange(minBalanceCalcRule.getMinBalanceCalcRuleType(), INF_FUND_MIN_BAL_INIT_RULE, NbaTableConstants.OLI_LU_MINBALCALCTYPE, CYBTBL_UCT, CT_DEFAULT, 0); //SPR1633 SPR2115
				//Fund id DXE: FIFMBRMN
				//DXE = DXE + createDataExchange(minBalanceCalcRule.getRemainMinBalanceCalcRuleType(),INF_FUND_REMAIN_MIN_BAL_INIT_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			}
			//Fund purchase rule DXE: FIFPURCH
			DXE = DXE + createDataExchange(investProductInfoEx.getMoneyMovementCode(),INF_FUND_PURCHASE_RULE, NbaTableConstants.OLI_LU_MONEY, CYBTBL_UCT, CT_DEFAULT,0);//NBA104
			//Fund investment type DXE: FIFFITYP
			DXE = DXE + createDataExchange(investProductInfoEx.getInvestRateType(),INF_FUND_INVEST_TYPE, NbaTableConstants.NBA_INVEST_METHOD_TYPE, CYBTBL_UCT, CT_DEFAULT,0); //SPR3418
			//Fund investment subtype DXE: FIFFISTP
			DXE = DXE + createDataExchange(investProductInfoEx.getInvestRateMethod(),INF_FUND_INVEST_SUBTYPE, "OLIEXT_LU_INVRATEMETH", CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
			//Fund interest frequency DXE: FIFFIFRQ
			DXE = DXE + createDataExchange(investProductInfoEx.getInterestMode(),INF_FUND_INTEREST_FREQ,"NBA_PP_INTERESTMODE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
			//Fund interest compound rule DXE: FIFFCMPD
			DXE = DXE + createDataExchange(investProductInfoEx.getCompoundMode(),INF_FUND_INT_COMPOUND_RULE,"NBA_PP_COMPOUNDMODE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633

			for (int i=0; i < investProductInfoEx.getRateVariationCount(); i++){
				rateVariation = investProductInfoEx.getRateVariationAt(i);
				rateVariationByDuration = rateVariation.getRateVariationByDurationAt(0);
				switch ((int)rateVariation.getRateType()){
					case 7:
					//Guaranteed
						//Fund guaranteed interest rate DXE: FIFFGIRT
						DXE = DXE + createDataExchange(rateVariation.getInvestRate(),INF_FUND_GUAR_INT_RATE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);				
						//Fund guraranteed interest rule DXE: FIFFGRUL
					DXE = DXE + createDataExchange(investProductInfoEx.getIntGuarRule(), INF_FUND_GUAR_INT_RULE, NbaTableConstants.OLIEXT_LU_INTGUARRULE, CYBTBL_UCT, CT_DEFAULT,0);//NBA104
					if (rateVariation.getDurQualifier()== 4){
							//Fund guaranteed interest period DXE: FIFFGRUL
							DXE = DXE + createDataExchange(rateVariationByDuration.getHighDur(),INF_FUND_GUAR_INT_PERIOD,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						}
						break;
					case 10:
					//Assumed
						//Fund assumed interest rate DXE: FIFVAIRT
						DXE = DXE + createDataExchange(rateVariation.getInvestRate(),INF_FUND_ASSUMED_INT_RATE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);				
			
				}
				
							}
			//Fund Rate File search key DXE: FIFFKEY
			DXE = DXE + createDataExchange(investProductInfoEx.getTableIdentity(),INF_FUND_RATE_FILE_SRCH_KEY,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			
		}
		
		//default values coded from middle tier spreadsheet.Will not be needed once CPD values are available.
		//FIFPTBLE
		DXE = DXE + createDataExchange("00","FIFPTBLE",CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);					
		//FIFPRUL1
		DXE = DXE + createDataExchange("0","FIFPRUL1",CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);					
		//FIFPRUL2
		DXE = DXE + createDataExchange("0","FIFPRUL2",CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);				
			
		
		return DXE;
	}

	/**
	 * Create the multiple fund data from the policy product information
	 * 
	 * @param policyProduct The current PolicyProduct XML object
	 * @return java.lang.String The multiple fund DXE information 
	 */
	protected String createPolicyProductMultipleFunds(PolicyProduct policyProduct) { 
		
		OLifEExtension olifeEx = null;
		LifeProductOrAnnuityProduct lifeProductOrAnnuityProduct = null;
		PolicyProductExtension polProdEx = null;
		AnnuityProduct annuityProduct = null;
		LifeProduct lifeProduct = null;
		LifeProductExtension lifeProductEx = null;
		Object[] featureProducts= null;
		FeatureProduct featureProduct= null;
		FeatureProductExtension featureProductEx= null;
		FeatureOptProduct featureOptProduct = null;
		FeatureOptProductExtension featureOptProductEx = null;
		int featureProductCount = 0;
		Fee fee = null;
		FeeExtension feeEx = null;
		String DXE = "";
		
		lifeProductOrAnnuityProduct = policyProduct.getLifeProductOrAnnuityProduct();
		if (lifeProductOrAnnuityProduct.isLifeProduct()){
			lifeProduct = lifeProductOrAnnuityProduct.getLifeProduct();
			lifeProductEx = AccelProduct.getFirstLifeProductExtension(lifeProduct); //NBA237
			featureProductCount = lifeProductEx.getFeatureProductCount();
			if (featureProductCount > 0)
				featureProducts = lifeProductEx.getFeatureProduct().toArray();
		}else{
			annuityProduct = lifeProductOrAnnuityProduct.getAnnuityProduct();
			featureProductCount = annuityProduct.getFeatureProductCount();
			if (featureProductCount > 0)
				featureProducts = annuityProduct.getFeatureProduct().toArray();
		}

		for (int i=0; i < featureProductCount; i++){
			featureProduct = (FeatureProduct) featureProducts[i];
			featureProductEx = AccelProduct.getFirstFeatureProductExtension(featureProduct);	//SPR2115 NBA237
			 
			if (featureProduct.getFeatureOptProductCount() >0 ){
				featureOptProduct = featureProduct.getFeatureOptProductAt(0);
				featureOptProductEx = AccelProduct.getFirstFeatureOptProductExtension(featureOptProduct);	//NBA237	
			}
			switch ((int) featureProduct.getArrType()){
				case 1:

					if (featureProductEx != null){
						/*
						 * Transfer Rules
						 * Allowed Transfer code DXE:FMFTALOW
						 * Transfer Charge Table DXE:FMFTCTBL
						 * Primary charge rule DXE: FMFTCRU1
						 * Secondary charge rule DXE: FMFTCRU2
						 * Min Transfer Amt DXE: FMFTMAMT
						 * Transfer Frequency Rule DXE:FMFTFRUL
						 * Transfer min day intervals DXE:FMFTFINT
						 * Maxiumum number of transfers per year  DXE:FMFTNMYR
						 */ 
						//begin SPR2115	
						String allowedCode = "0";
						if (featureProductEx.getAllowedInd()) {
							if (featureProductEx.getWithdrawalOrder() == 1) { //LIFO
								allowedCode = "1";
							} else if (featureProductEx.getWithdrawalOrder() == 5) { //LIFO
								allowedCode = "2";
							} else if (featureProductEx.getInterestInvestCreditType() == 100050001) { //Xfer to internal account
								allowedCode = "3";
							} else if (featureProductEx.getInterestInvestCreditType() == 100050002) { //New money
								allowedCode = "4";
							}
						}
						DXE = DXE + createDataExchange(allowedCode, INF_MF_ALLOW_TRANSFER_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
						//end SPR2115
						DXE = DXE + createDataExchange(featureProductEx.getFeatureArrTableIdentity(),INF_MF_TRANSFER_CHARGE_TBL,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureProductEx.getPrimaryChargeMethod(), INF_MF_PRIMARY_TRANS_RULE, NbaTableConstants.OLIEXT_LU_FEATUREARRCALCMETH, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
						DXE = DXE + createDataExchange(featureProductEx.getSecondaryChargeMethod(), INF_MF_SEC_TRANS_RULE, NbaTableConstants.OLIEXT_LU_FEATUREARRCALCMETH, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
						if (featureProduct.getFeatureOptProductCount() >0 ){
							featureOptProduct = featureProduct.getFeatureOptProductAt(0);
							featureOptProductEx = AccelProduct.getFirstFeatureOptProductExtension(featureOptProduct); //NBA237
							DXE = DXE + createDataExchange(featureOptProduct.getMinTransactionAmt(),INF_MF_MIN_TRANS_AMT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
							if (featureOptProductEx != null){
								DXE = DXE + createDataExchange(featureOptProductEx.getArrFrequencyChargeMethod(), INF_MF_TRANS_FREQ_RULE, NbaTableConstants.OLIEXT_LU_ARRFREQCHMETH, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115	
								DXE = DXE + createDataExchange(featureOptProductEx.getArrFrequencyDays(),INF_MF_TRANS_MIN_DAYS_INTERVALS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			
							}
						}
						DXE = DXE + createDataExchange(featureProduct.getMaxNumInstances(),INF_MF_MAX_NO_TRANS_PER_YEAR,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					
					}
					break;
				case 36:
				/*
				 * MVA Plan Level Rules
				 * Death Benefit Rule DXE:FMFMVDBR
				 * Loan Rule DXE: FMFMVLNR
				 * Annuity Purchase Rule DXE:FMFMVAPR 
				 * Fund maturity beyond the maturity date Rule DXE:FMFMVFMB
				 * Default reinvestment Option DXE:FMFMVDRE
				 * Adjust Calculation Code DXE: FMFMVACC
				 * Adjust Calculation Rule DXE:FMFMVACR
				 * Negative Limit Rule DXE: FMFMVNLR
				 * Negative Limit Percent DXE: FMFMVNLP
				 * Postive Limit Rule DXE: FMFMVPRR
				 * True Up Ooption DXE: FMFMVTUO
				 * Alternate Index Key DXE: FMFMVAIK
				 */
					if (featureOptProductEx != null){
						DXE = DXE + createDataExchange("X1XXXXXX","FMFFLAGA",CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVADeathBenefitType(),INF_MF_MVA_DB_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVALoanInd(),INF_MF_MVA_LOAN_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVAAnnuityPurchaseInd(),INF_MF_MVA_ANNUITY_PURCHASE_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVADateRuleInd(),INF_MF_MVA_FUND_MAT_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVAReinvestType(),INF_MF_MVA_DEFAULT_REINVEST_OPT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getInterestCalcType(),INF_MF_MVA_ADJ_CALC_CODE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getPeriodAdjCalcMethod(),INF_MF_MVA_ADJ_CALC_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVANegLimitType(),INF_MF_MVA_NEG_LIMIT_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVANegLimitPct(),INF_MF_MVA_NEG_LIMIT_PCT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVAPositiveLimitType(),INF_MF_MVA_POS_LIMIT_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						//DXE = DXE + createDataExchange(featureOptProductEx.getMVAPositiveLimitPct(),"FMFMVPLP",CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getMVATrueUpInd(),INF_MF_MVA_TRUE_UP, CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProductEx.getTableIdentity(),INF_MF_MVA_ALT_INDEX_KEY,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
		
					}
					break;
				case 37:		
				/*
				 * Allocation payment data
				 * Maximum allocation changes per year DXE: FMFANCYR
				 * Minimum days interval DXE: FMFAFREQ
				 * Minimum percent allocated DXE: FMFAMPCT
				 * Minimum balance rule DXE: FMFALMIN
				 */	
					DXE = DXE + createDataExchange(featureProduct.getMaxNumInstances(),INF_MF_ALLOC_MAX_CHANGES_PER_YR,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					if (featureOptProductEx != null){
						DXE = DXE + createDataExchange(featureOptProductEx.getArrFrequencyDays(),INF_MF_ALLOC_MIN_DAYS_INTERVAL,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						DXE = DXE + createDataExchange(featureOptProduct.getMinPct(),INF_MF_ALLOC_MIN_PCT ,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
						DXE = DXE + createDataExchange(featureOptProductEx.getOverrideStandAllocInd(),INF_MF_ALLOC_MIN_BAL_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
			
					}
					break;
				case 52:
					//Disbursement code DXE: FMFCDSBR
					if (featureProductEx != null){
						DXE = DXE + createDataExchange(featureProductEx.getChargeAllocationType(), INF_MF_DISBURSEMENT_CODE, NbaTableConstants.OLI_LU_POLICYCHARGE, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
					}
					break;
			}

		}
		int feeCount = policyProduct.getFeeCount();
		
		for(int i = 0;i<  feeCount;i++){
			fee = policyProduct.getFeeAt(i);
			feeEx = AccelProduct.getFirstFeeExtension(fee); //SPR3290, NBA237
			switch ((int)fee.getFeeType()){
				case 15:
				/*
				 * MVA full surrender
				 * 
				 * Full Surrender free rule DXE:FMFMVAFS
				 * full surrender percent DXE: FMFMVFSU
				 * full surender months DXE: FMFMVFSM
				 */
					if (feeEx.hasMVACalcMethod())
						DXE = DXE + createDataExchange(feeEx.getMVACalcMethod(),INF_MF_MVA_FULL_SURR_FREE_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					if (feeEx.hasMVAChargeFreePct())
						DXE = DXE + createDataExchange(feeEx.getMVAChargeFreePct(),INF_MF_MVA_FULL_SURR_PCT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
					if (feeEx.hasMVAChargeFreeMonths()) 
						DXE = DXE + createDataExchange(feeEx.getMVAChargeFreeMonths(),INF_MF_MVA_FULL_SURR_MONTHS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
					break;		

				case 16:
				/*
				 * MVA partial withdrawl
				 * 
				 * Partial withdrawl free rule DXE: FMFMVAPW
				 * Partial withdrawl percent DXE: FMFMVPWP
				 * Partial withdrawl months DXE: FMFMVPWM
				 * 
				 */
					if (feeEx.hasMVACalcMethod())
						DXE = DXE + createDataExchange(feeEx.getMVACalcMethod(),INF_MF_MVA_PART_WITHDRAWAL_FREE_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					if (feeEx.hasMVAChargeFreePct())
						DXE = DXE + createDataExchange(feeEx.getMVAChargeFreePct(),INF_MF_MVA_PART_WITHDRAWAL_PCT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
					if (feeEx.hasMVAChargeFreeMonths()) 
						DXE = DXE + createDataExchange(feeEx.getMVAChargeFreeMonths(),INF_MF_MVA_PART_WITHDRAWAL_MONTHS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	

					break;	
			}
		}
		polProdEx = AccelProduct.getFirstPolicyProductExtension(policyProduct);  //NBA104, NBA237

		//Maximum number of allocations DXE: FMFANFUN
		if (polProdEx != null && polProdEx.hasMaxNumConcurAlloc() )				
			DXE = DXE + createDataExchange(polProdEx.getMaxNumConcurAlloc(),INF_MF_ALLOC_MAX_NUM,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	

		
		return DXE;
	}

	/**
	 * Create the benefit segment data from the policy product information
	 * 
	 * @param policyProduct The current PolicyProduct XML object
	 * @param covType The type of benefit
	 * @return java.lang.String The benefit DXE information obtained from the policy product information
	 */
	protected String createPolicyProductCovOpt(PolicyProduct policyProduct, String covType) { 
		
		// SPR3290 code deleted
		String covDXE = "";
		// SPR3290 code deleted
		LifeProductOrAnnuityProduct lifeProductOrAnnuityProduct = null;
		LifeProduct lifeProduct = null;
		CoverageProduct coverageProduct = null;
		// SPR3290 code deleted
		CovOptionProduct covOptionProduct = null;
		CovOptionProductExtension covOptionProductEx = null;
		
		boolean valueFound = false;
		// SPR3290 code deleted
		lifeProductOrAnnuityProduct = policyProduct.getLifeProductOrAnnuityProduct();
		if (lifeProductOrAnnuityProduct.isLifeProduct())
			lifeProduct = lifeProductOrAnnuityProduct.getLifeProduct();
		if (lifeProduct == null){
			return"";
		}
		
		for(int i = 0; i < lifeProduct.getCoverageProductCount(); i++){
			coverageProduct = lifeProduct.getCoverageProductAt(i);
			if (covType.equals(coverageProduct.getProductCode())) {
				for(int j = 0; j < coverageProduct.getCovOptionProductCount(); j++){
					covOptionProduct = coverageProduct.getCovOptionProductAt(j);
					if (benefitCode.equals(covOptionProduct.getProductCode())){ 
						valueFound = true;
						break;		
					}
				}
			}
		}
		
		if (!valueFound){
			return "";		
		}
		covOptionProductEx = AccelProduct.getFirstCovOptionProductExtension(covOptionProduct); //SPR3290, NBA237			
		
		/*
		 * Benefit commisionable ind DXE: FSBBCOM
		 * Benefit reserve plan DXE: FSBBPLN
		 * Benefit renewable rate ind DXE: FSBRENRT
		 */
		if (covOptionProductEx != null) {
			covDXE = covDXE + createDataExchange(covOptionProductEx.getCommissionablePremCalcInd(), INF_COV_OPT_COMM_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			covDXE = covDXE + createDataExchange(covOptionProductEx.getRenewableInd(), INF_COV_OPT_RESERVE_PLAN, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);	
			//begin SPR2341
			if(covOptionProductEx.getRenewableInd()){
				covDXE = covDXE + createDataExchange("", INF_COV_OPT_RENEW_RATE_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			}else{
				covDXE = covDXE + createDataExchange(RENEWABLE_IND, INF_COV_OPT_RENEW_RATE_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			}
			//end SPR2341
		}
		/*
		 * Benefit value per unit DXE: FSBBVPU
		 * Benefit form number DXE: FSBFRMNO
		 */
		covDXE = covDXE + createDataExchange(covOptionProduct.getValuePerUnit(), INF_COV_OPT_VPU, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
		covDXE = covDXE + createDataExchange(covOptionProduct.getFiledFormNumber(), INF_COV_OPT_FORM_NO, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
	
		return covDXE;
	}

	/**
	 * Create the coverage segment data from the policy product information
	 * 
	 * @param policyProduct The current PolicyProduct XML object
	 * @param covType The type of product the coverage represents
	 * @param checkMort if specific mortality needs to be checked
	 * @param covGender the gender of relation to the coverage
	 * @param rateClass the rateClass in Coverage.Extension
	 * @param coverage the coverage object
	 * @return java.lang.String The coverage DXE information obtained from the policy product information
	 */
	//SPR1633 added new parameters, checkMort, covGender, and rateClass
	//SPR3162 Added new parameter coverage
	protected String createPolicyProductCov(PolicyProduct policyProduct, String covType, boolean checkMort, long covGender, String rateClass, Coverage coverage) {
		
		OLifEExtension olifeEx = null;
		PolicyProductExtension polProdEx = null;
		String covDXE = "";
		Fee fee = null;
		FeeExtension feeEx = null;
		LifeProductOrAnnuityProduct lifeProductOrAnnuityProduct = null;
		LifeProduct lifeProduct = null;
		AnnuityProduct annuityProduct = null;
		CoverageProduct coverageProduct = null;
		NonForProvision nonForProvision = null; 
		AnnuityProductExtension annuityProductEx = null;
		CoverageProductExtension coverageProductEx = null;
		LifeProductExtension lifeProductEx = null;
        polProdEx = AccelProduct.getFirstPolicyProductExtension(policyProduct); //SPR3290, NBA237
		lifeProductOrAnnuityProduct = policyProduct.getLifeProductOrAnnuityProduct();
		if (lifeProductOrAnnuityProduct.isLifeProduct())
			lifeProduct = lifeProductOrAnnuityProduct.getLifeProduct();
		else
			annuityProduct = lifeProductOrAnnuityProduct.getAnnuityProduct();
		
		/* 
		 * For Olife Class 7, Adapter must also look at PolicyProduct.ValuationClassFreq 
		 * if 1 set FCVCLASS TO BES=6 ELSE BES=7
		 * 
		 */
		 
		if (polProdEx != null){
			valuationFreq = polProdEx.getValuationClassFreq();	
			if (!classSeriesFound){
				//DXE: FCVCLASS
				if (polProdEx.getValuationClassType() != 7){
					covDXE = covDXE + createDataExchange(polProdEx.getValuationClassType(),INF_COV_CLASS,"OLI_LU_VALCLASS",CYBTBL_UCT, CT_DEFAULT,0);
				}else if (valuationFreq == 1){
					covDXE = covDXE + createDataExchange(6,INF_COV_CLASS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
				}else{
					covDXE = covDXE + createDataExchange(7,INF_COV_CLASS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
				}
					
				//DXE: FCVBASE
				covDXE = covDXE + createDataExchange(polProdEx.getValuationBaseSeries(),INF_COV_SERIES,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
				//DXE: FCVSBASE
				covDXE = covDXE + createDataExchange(polProdEx.getValuationSubSeries(),INF_COV_SUB_SERIES,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
				classSeriesFound = true;
			}
		}
		covDXE =
			covDXE + createDataExchange(policyProduct.getPolicyProductForm(), INF_COV_MAJOR_LINE, "OLI_LU_HOLDINGFORM", CYBTBL_UCT, CT_DEFAULT, 0);	//SPR2062
	
		//Age use code DXE: FCVAGEUS : Mortality table code DXE: FCVTABLE : Mortality function DXE:FCVVFNCT
		//Valuation interest rate DXE:FCVVINTR
		//Modification valuation method code DXE:FCVMOD
		//Person Covered contained in the PDF DXE:FCVPERCV
		if (polProdEx != null){
			//begin SPR2020
			//Age use code DXE: FCVAGEUS - Concatenate the values of ReserveAgeCalcType, CVAgeCalcMethod, MatExpireAgeCalcMethod
			//Initialize the default values
			String reserve = "4";
			String cashValue = "6";
			String benefit = "6";
			if (polProdEx.hasReserveAgeCalcType()) {
				reserve = String.valueOf(polProdEx.getReserveAgeCalcType());
			}
			if (polProdEx.hasCVAgeCalcMethod()) {
				cashValue = String.valueOf(polProdEx.getCVAgeCalcMethod());
			}
			if (polProdEx.hasMatExpireAgeCalcMethod()) {
				benefit = String.valueOf(polProdEx.getMatExpireAgeCalcMethod());
			}		
			covDXE = covDXE + createDataExchange(reserve + cashValue + benefit,INF_COV_AGE_USE_CODE, NbaTableConstants.NBA_AGEUSE, CYBTBL_UCT, CT_DEFAULT,0);		
			//end SPR2020
			
			// begin SPR1633
			if (checkMort) {
				if (polProdEx.hasMortalityOrMorbidityTable()) {
					String ageCalc;
					if (policyProduct.hasAgeCalculationType()) {
						ageCalc = String.valueOf(policyProduct.getAgeCalculationType());
					} else {
						ageCalc = "-";
					}
					String sGender = String.valueOf(covGender);
					String gMortality = getCyberValue( polProdEx.getMortalityOrMorbidityTable(), NbaTableConstants.OLI_LU_MORTALITYTBL, CYBTBL_UCT, compCode, DEFAULT_COVID);
					mortalityData = (NbaMortalityData[]) getMortalityTable("*", gMortality, ageCalc, sGender, rateClass);
					if (mortalityData.length == 1) {
						covDXE = covDXE + createDataExchange(mortalityData[0].getSpecificMortality(),INF_COV_MORT_TABLE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					} else {
						covDXE = covDXE + createDataExchange(polProdEx.getMortalityOrMorbidityTable(),INF_COV_MORT_TABLE, NbaTableConstants.OLI_LU_MORTALITYTBL,CYBTBL_UCT, CT_DEFAULT,0); 
					}
				}			
			} else {
				covDXE = covDXE + createDataExchange(polProdEx.getMortalityOrMorbidityTable(),INF_COV_MORT_TABLE, NbaTableConstants.OLI_LU_MORTALITYTBL,CYBTBL_UCT, CT_DEFAULT,0); 
			}
			// end SPR1633
			covDXE = covDXE + createDataExchange(polProdEx.getReserveFunction(),INF_COV_MORT_FUNCTION,NbaTableConstants.OLI_LU_RESERVEFUNCTION, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115		
			covDXE = covDXE + createDataExchange(polProdEx.getReserveIntRate(),INF_COV_VAL_INTEREST_RATE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			covDXE = covDXE + createDataExchange(polProdEx.getReserveMethod(), INF_COV_MODIFICATION_CODE,"OLI_LU_RESVMETH",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
			covDXE = covDXE + createDataExchange(polProdEx.getParticipantPremVarInd(),INF_COV_PERSON_COVERED,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			for(int i=0 ;i < polProdEx.getNonForProvisionCount(); i++){
				nonForProvision = polProdEx.getNonForProvisionAt(i);
				switch ((int)nonForProvision.getNonFortProv()){
					case 2: 
					case 5:
						/* 
						 * ETI coverage fields 
						 * 
						 * EI Mortality Table DXE:FCVNSPTB 
						 * Describes the effect of impairments on the death benefit of ETI. DXE: FCVADJ
						 * initial unadjusted ETI death benefit DXE: FCVEI
						 * ETI benefit DXE: FCVBEN
						 */ 
						covDXE = covDXE + createDataExchange(nonForProvision.getMortalityOrMorbidityTable(),INF_COV_EI_MORTALITY_TABLE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						covDXE = covDXE + createDataExchange(nonForProvision.getLoanAdjType(),INF_COV_ETI_DEATH_BENEFIT,"OLI_LU_LOANADJCODE",CYBTBL_UCT, CT_DEFAULT,0);  //SPR1633
						covDXE = covDXE + createDataExchange(nonForProvision.getInitialAmtType(),INF_COV_ETI_INITIAL_UNADJ_DB,"OLI_LU_AMTCALCMETHOD",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
						covDXE = covDXE + createDataExchange(nonForProvision.getBenefitType(),INF_COV_ETI_BENEFIT ,"OLI_LU_BENEFITCODE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633			
						break;
					case 3:
					case 6:
					/* 
					 * RPU coverage fields 
					 * 
					 * RPU Mortality Table DXE:FCVNSPRP 
					 * Interest rate to be used in nonforfeiture benefit calculations. DXE: FCVNSPI
					 * RPU benefit DXE: FCVRPU
					 */
						covDXE = covDXE + createDataExchange(nonForProvision.getMortalityOrMorbidityTable(),INF_COV_RPU_MORTALITY_TABLE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						covDXE = covDXE + createDataExchange(nonForProvision.getNetSinglePremIntRate(),INF_COV_NONFORFEITURE_INT_RATE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						covDXE = covDXE + createDataExchange(nonForProvision.getBenefitType(),INF_COV_RPU_BENEFIT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						break;
				}
			}
		}
		//Indicates whether deficiency reserves (for excess of net premium over gross premium) can
		//be required during the premium paying period of the coverage.
		//DXE:FCVDEF - assume 0 
		covDXE = covDXE + createDataExchange("0",INF_COV_DEFICIENT_CODE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
		//covDXE = covDXE + createDataExchange(policyProduct.getAgeCalculationType(),"FCVAGUS",CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
		if (isAnnuity()) {	//SPR2095
			covDXE = covDXE + createDataExchange(policyProduct.getFiledFormNumber(), APP_FORM_TYPE_NUM_ASSN, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//begin SPR2095
			int fpCnt = annuityProduct.getFeatureProductCount();
			FeatureProduct featureProduct;
			FeatureProductExtension featureProductExtension;
			for (int j = 0; j < fpCnt; j++) {
				featureProduct = annuityProduct.getFeatureProductAt(j);
                featureProductExtension = AccelProduct.getFirstFeatureProductExtension(featureProduct); //NBA237
				if (featureProductExtension != null && featureProductExtension.hasDBCalcMethodType()) {
					//Death Benefit Indicator DXE: FBRDBIND
					covDXE =
						covDXE
							+ createDataExchange(
								featureProductExtension.getDBCalcMethodType(),
								INF_DEATH_BEN_IND,
								NbaTableConstants.OLIEXT_LU_DBCALCMETHODTYPE,
								CYBTBL_UCT,
								CT_DEFAULT,
								0);
					break;
				}
			}
			//end SPR2095
		}
		if (lifeProduct != null){
			boolean valueFound = false;
			for(int i = 0; i < lifeProduct.getCoverageProductCount(); i++){
				coverageProduct = lifeProduct.getCoverageProductAt(i);
				if (covType.equals(coverageProduct.getProductCode()) && OLI_COVIND_BASE == coverageProduct.getIndicatorCode()) { //NBA104
					covDXE = covDXE + createDataExchange("1000.00", INF_COV_VALUE_PER_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0); //SPR1633 default it to 1000.00
					valueFound = true;
                    coverageProductEx = AccelProduct.getFirstCoverageProductExtension(coverageProduct); //NBA104, NBA237
					/*
					 * Renewable Premium ind DXE: FCVRPRIN
					 * Initial Renewable Period DXE: FCVINTRW
					 * Subsequent Renewal Start duration DXE: FCVSRDUR
					 * Subsequent Renewal Period DXE: FCVSRPRD
					 * Indeterminate Premium Guaranteed Renewal Period DXE:FCVIPGUR
					 * Band code from the basic segment of the PDF DXE: FCVBNDSC
					 * Coverage Band Rule Code DXE: FCVRBAND
					 * If the premium type is UltimateRenewRates then values for FCVINTRW, FCVSRDUR, FCVSRPRD are added to DXE.
					 * If the premium type is SelectRenewRates then value for FCVINTRW is added to DXE.
					 */
					if (coverageProductEx != null) {
						covDXE =
							covDXE
								+ createDataExchange(
									coverageProductEx.getRenewPremType(),
									INF_COV_RENEWAL_PREMIUM_TYPE,
									"OLIEXT_LU_RENEWPREMTYPE",
									CYBTBL_UCT,
									CT_DOUBLE,
									0);
						//SPR1633
						// begin SPR2050
						if (coverageProductEx.getRenewPremType()
							== NbaOliConstants
								.OLIX_RENEWPREMTYPE_ULTIMATERENEWRATES) {
							covDXE =
								covDXE
									+ createDataExchange(
										coverageProductEx
											.getInitialPremRenewPeriod(),
										INF_COV_INITIAL_RENEWAL_PERIOD,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_DOUBLE,
										3);
							covDXE =
								covDXE
									+ createDataExchange(
										coverageProductEx
											.getSubseqRenewStartDur(),
										INF_COV_SUBSEQUENT_RENEWAL_ST_DUR,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_DOUBLE,
										3);
							covDXE =
								covDXE
									+ createDataExchange(
										coverageProductEx
											.getSubseqPremRenewPeriod(),
										INF_COV_SUBSEQUENT_RENEWAL_PERIOD,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_DOUBLE,
										3);
						} else if(
							coverageProductEx.getRenewPremType()
								== NbaOliConstants
									.OLIX_RENEWPREMTYPE_SELECTRENEWRATES) {
							covDXE =
								covDXE
									+ createDataExchange(
										coverageProductEx
											.getSelectPeriodYears(),
										INF_COV_INITIAL_RENEWAL_PERIOD,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_DOUBLE,
										3);
						}
						// end SPR2050
						// SPR2050 code deleted
						// SPR3290 code deleted
						// SPR2050 code deleted
						covDXE = covDXE + createDataExchange(coverageProductEx.getIndeterPremRenewPeriod(),INF_COV_IND_PREM_GUAR_PERIOD, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
						covDXE = covDXE + createDataExchange(coverageProductEx.getJurisdictionPremInd(), INF_COV_BAND_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
						//begin NBA104
						covDXE =
							covDXE
								+ createDataExchange(
									coverageProductEx.getCovBandRuleCode(),
									INF_COV_BAND_CODE_RULE,
									NbaTableConstants.OLIEXT_LU_COVBANDRULECOD,
									CYBTBL_UCT,
									CT_DEFAULT,
									0);	
						//end NBA104

					}	
					break;	//NBA104
				}
			}//end coverageProduct Loop
			if (!valueFound){
				covDXE = covDXE + createDataExchange("1000.00", INF_COV_VALUE_PER_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			}
			//begin NBA104
            lifeProductEx = AccelProduct.getFirstLifeProductExtension(lifeProduct); //NBA237
			if (lifeProductEx != null) {
				//MEC indicator DXE:FCVMECIN
				covDXE =
					covDXE
						+ createDataExchange(
							lifeProductEx.getMECIssueType(),
							INF_COV_MEC_IND,
							NbaTableConstants.OLIEXT_LU_MECISSUETYPE,
							CYBTBL_UCT,
							CT_DEFAULT,
							0);
			}
			//end NBA104
		}else if (!(planType.equals("F"))){
			//FCVUNVAL
			covDXE = covDXE + createDataExchange("1000.00", INF_COV_VALUE_PER_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
		}
	
		if (annuityProduct != null){
            annuityProductEx = AccelProduct.getFirstAnnuityProductExtension(annuityProduct); //NBA237
			//Annuity Purchase Value Rule from PDF DXE:FCVFAPVR
			if (annuityProductEx != null){
				covDXE = covDXE + createDataExchange(annuityProductEx.getAnnPurchaseCalcRule(), INF_COV_ANNUITY_PUCHASE_VAL_RULE, NbaTableConstants.OLIEXT_LU_ANNPURCHASECALCRULE, CYBTBL_UCT, CT_DOUBLE, 0);	//NBA104
				}
		}

		//begin SPR3162
        String chargeExpenseRule = "0"; //default value 
        String backendChargeMethod = null;
        int feeTableRefCount = 0; 
        FeeTableRef feeTableRef = null;
        //end SPR3162
		int feeCount = policyProduct.getFeeCount();
		
		for(int i = 0;i<  feeCount;i++){
			fee = policyProduct.getFeeAt(i);
            feeEx = AccelProduct.getFirstFeeExtension(fee); //SPR3290, NBA237
			//begin SPR3162
            if (feeEx != null && fee.getFeeType() == OLI_FEE_EXPENSE) {
                feeTableRefCount = feeEx.getFeeTableRefCount();
                for (int k = 0; k < feeTableRefCount; k++) {
                    feeTableRef = feeEx.getFeeTableRefAt(k);
                    if (feeTableRef.getTableType() == TABLEREF_TABLETYPE_62) { 
                        if (isAP && coverage.getIndicatorCode() == OLI_COVIND_RIDER && coverage.getLifeCovTypeCode() == OLI_COVTYPE_UNIVLIFE) {
                            backendChargeMethod = getCyberValue(feeTableRef.getChargeMethodTC(), NbaTableConstants.OLIEXT_LU_CHARGERULECALCMETHOD,
                                    CYBTBL_UCT, compCode, DEFAULT_COVID);
                            if (feeTableRef.getChargeMethodTC() == OLIX_CHARGERULECALCMETHOD_DEDUCTAMTK || backendChargeMethod == null
                                    || backendChargeMethod.trim().length() == 0) {
                                chargeExpenseRule = feeTableRef.getRuleKeyDef();
                            } else {
                                chargeExpenseRule = backendChargeMethod;
                            }
                        }
                        covDXE = covDXE + createDataExchange(chargeExpenseRule, INF_COV_AP_RIDER_EXPENSE_RULE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
                        break;
                    }
                }
                break;
            }
            //end SPR3162
		}
		
		// NBA104 deleted code
		
		return covDXE;
	}

	/**
	 * Create the advanced product data from the policy product information
	 * 
	 * @param policyProduct The current PolicyProduct XML object
	 * @param covType The type of product the coverage represents
	 * @return java.lang.String The advanced product DXE information obtained from policy product
	 */
	protected String createPolicyProductAP(PolicyProduct policyProduct, String covType) { 
		
		// SPR3290 code deleted
		PolicyProductExtension polProdEx = null;
		// SPR3290 code deleted
		Fee fee = null;
		FeeExtension feeEx = null;
		LoanProvision loanProvision = null;
		LifeProductOrAnnuityProduct lifeProductOrAnnuityProduct = null;
		LifeProduct lifeProduct = null;
		// SPR3290 code deleted
		CoverageProduct coverageProduct = null;
		// SPR3290 code deleted
		CoverageProductExtension coverageProductEx = null;
		// SPR3290 code deleted
		RateVariation rateVariation = null;
		RateVariationExtension rateVariationEx = null;
		PolicyProductInfo policyProductInfo = null;
		PolicyProductInfoExtension policyProductInfoEx = null;
		String DXE = "";
		MinBalanceCalcRule minBalanceCalcRule = null;

		polProdEx = AccelProduct.getFirstPolicyProductExtension(policyProduct);  //NBA104, NBA237
		// NBA104 deleted code
		lifeProductOrAnnuityProduct = policyProduct.getLifeProductOrAnnuityProduct();
		if (lifeProductOrAnnuityProduct.isLifeProduct())
			lifeProduct = lifeProductOrAnnuityProduct.getLifeProduct();
		// SPR3290 code deleted
	
		if (lifeProduct != null){
			// SPR3290 code deleted
			for(int i = 0; i < lifeProduct.getCoverageProductCount(); i++){
				coverageProduct = lifeProduct.getCoverageProductAt(i);
				if (covType.equals(coverageProduct.getProductCode())) {
					// SPR3290 code deleted
					// NBA104 deleted code
					coverageProductEx = AccelProduct.getFirstCoverageProductExtension(coverageProduct);  //NBA10, NBA2374
					if (coverageProductEx != null) {
						/*
						 * Decrease/increase  rules 
						 * 
						 * decrease lifo/fifo rule DXE: FULDLIFI
						 * decrease rule DXE: FULDRULE
						 * increase lifo/fifo rule DXE:FULILIFI
						 * increase rule DXE: FULIRULE
						 */
						DXE = DXE + createDataExchange(coverageProductEx.getDBDecreaseOrderType(), INF_AP_DECREASE_LIFO_FIFO, "OLI_LU_WDORDER", CYBTBL_UCT, CT_DOUBLE, 0); //SPR1633
						DXE = DXE + createDataExchange(coverageProductEx.getDBDecreaseMethod(), INF_AP_DECREASE_RULE, "OLIEXT_LU_DBCHANGEMETHOD", CYBTBL_UCT, CT_DOUBLE, 0); //SPR1633
						DXE = DXE + createDataExchange(coverageProductEx.getDBIncreaseOrderType(), INF_AP_INCREASE_LIFO_FIFO, "OLI_LU_WDORDER", CYBTBL_UCT, CT_DOUBLE, 0); //SPR1633
						DXE = DXE + createDataExchange(coverageProductEx.getDBIncreaseMethod(), INF_AP_INCREASE_RULE, "OLIEXT_LU_DBCHANGEMETHOD", CYBTBL_UCT, CT_DOUBLE, 0); //SPR1633
						/*
						 * Cost of Insurance
						 * 
						 * COI Guaranteed rule DXE: FULCGRUL
						 * COI Guaranteed period DXE: FULCGPER
						 * COI frequency DXE: FULCFREQ
						 * COI Calc rule DXE: FULCCALC
						 * COI NAR rule DXE: FULCNARC
						 */
						long coiGaurType = coverageProductEx.getCOIGuarType(); //SPR3148
						DXE = DXE + createDataExchange(coiGaurType, INF_AP_COI_GUAR_RULE, "OLIEXT_LU_COIGUARTYPE", CYBTBL_UCT, CT_DOUBLE, 0); //SPR1633 SPR3148
						DXE = DXE + createDataExchange(coverageProductEx.getCOIGuarMonths(), INF_AP_COI_PERIOD, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
						DXE = DXE + createDataExchange(coverageProductEx.getCOIChargeFreq(), INF_AP_COI_FREQ, "OLI_LU_PAYMODE", CYBTBL_UCT, CT_DOUBLE, 0); //SPR1633
						if (policyProduct.getPolicyProductTypeCode() == OLI_PRODTYPE_INTWL)
							DXE = DXE + createDataExchange(coverageProductEx.getCOIRateRule(), INF_AP_COI_CALC_RULE, "OLIEXT_LU_COIRATERULE", CYBTBL_UCT, CT_DOUBLE, 0); //SPR1633
						else if (policyProduct.getPolicyProductTypeCode() == OLI_PRODTYPE_UL || policyProduct.getPolicyProductTypeCode() == OLI_PRODTYPE_VUL)
							DXE = DXE + createDataExchange("2", INF_AP_COI_CALC_RULE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
						if (planType.equals("F")){
							DXE = DXE + createDataExchange("0", INF_AP_COI_NAR_RULE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
						}else{					
							DXE = DXE + createDataExchange(coverageProductEx.getNARRiskType(), INF_AP_COI_NAR_RULE, NbaTableConstants.OLIEXT_LU_NARRISKTYPE, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
						}
						//begin SPR3148
						if (coiGaurType == OLIX_COIGUARTYPE_SINGLE || coiGaurType == OLIX_COIGUARTYPE_PERIOD) {
                            Date gaurEndDate = NbaUtils.calcDayFotFutureDate(getPolicy().getEffDate(), coverageProductEx.getCOIGuarMonths());
                            DXE = DXE + createDataExchange(gaurEndDate, INF_AP_COI_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
                        }
						//end SPR3148
					}	
				}
			}//end coverageProduct Loop
		}

		/*		  
		 * Billing code DXE: FULBILLC 
		 * Premium credit interest DXE:FULPRCRD 
		 * Premium grace period DXE:FULPRGPR
		 * Freeze period DXE: FULPRFRZ
		 * Premium grace period DXE: FULPRGPR
		 * 
		 * Grace Period info
		 * Grace Period Days DXE: FULGRCDY
		 * Grace Period credit code DXE: FULGRICD
		 * Grace Period interest rate DXE: FULGRIRT
		 */
		if (polProdEx != null){
			if (polProdEx.hasBillingNotifyOptionType()){ // SPR1633//SPR2219
				DXE = DXE + createDataExchange(polProdEx.getBillingNotifyOptionType(),INF_BILLING_OPTION,"OLIEXT_LU_BILLNOTOPTTYPE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633//SPR2219
			}//SPR2219
			
			DXE = DXE + createDataExchange(polProdEx.getPayEarnIntType(),INF_AP_PREM_CREDIT_INT,"OLIEXT_LU_PAYEARNINT",CYBTBL_UCT, CT_DEFAULT,0);
			DXE = DXE + createDataExchange(polProdEx.getGracePeriodLatePay(),INF_AP_PREM_GRACE_PERIOD,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			DXE = DXE + createDataExchange(polProdEx.getPayFreezePeriod(),INF_AP_FREEZE_PERIOD,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			DXE = DXE + createDataExchange(polProdEx.getGracePeriodLatePay(),INF_AP_GRACE_PERIOD,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			// SPR2115 code deleted
			
			DXE = DXE + createDataExchange(polProdEx.getGracePeriodDays(),INF_AP_GRACE_DAYS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			DXE = DXE + createDataExchange(polProdEx.getGracePeriodEarnInterestInd(),INF_AP_GRACE_CREDIT_CODE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			DXE = DXE + createDataExchange(polProdEx.getGracePeriodAltInterestRate(),INF_AP_GRACE_INT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			if (polProdEx.getLapseProvisionCount() > 0) { //SPR2115
				DXE = DXE + createDataExchange(polProdEx.getLapseProvisionAt(0).getGracePeriodEntryType(), INF_AP_GRACE_RULE, NbaTableConstants.OLI_LU_GPENTRY, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
			} //SPR2115					

			//Reinstatement rule  DXE: FULRRULE
			if (polProdEx.getReinstateInd())
				DXE = DXE + createDataExchange(polProdEx.getReinstateEffectType(),INF_AP_REINSTATEMENT_RULE,"OLI_LU_REINSTEFFTYPE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
		}
		
		if (policyProduct.getPolicyProductInfoCount() > 0) {
            policyProductInfo = policyProduct.getPolicyProductInfoAt(0);
            //begin SPR3290
            policyProductInfoEx = AccelProduct.getFirstPolicyProductInfoExtension(policyProductInfo); //NBA237
            //Moved this code to inside the if statement////SPR1633
            //	Commision extract code DXE: FULCRULE
            DXE = DXE
                    + createDataExchange(policyProductInfoEx.getCommExtractCode(), INF_AP_COMM_EXTRACT_RULE, "OLI_LU_PAYRATECAT", CYBTBL_UCT,
                            CT_DEFAULT, 0); //SPR1633
            //end SPR3290                
        }
		//if product is not ISL move 0 to FULSAMIN
		//Specified minimum amount
		if (policyProduct.getPolicyProductTypeCode() != OLI_PRODTYPE_INTWL){
			DXE = DXE + createDataExchange("0",INF_AP_SPC_MIN_AMT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);		
		}		
		
		int feeCount = policyProduct.getFeeCount();
		
		for(int i = 0;i<  feeCount;i++){
			fee = policyProduct.getFeeAt(i);
			// NBA104 deleted code
			feeEx = AccelProduct.getFirstFeeExtension(fee);  //NBA104, NBA237
			switch ((int)fee.getFeeType()){
				
				case 8:
				//Expense Charges - need fee type
				/*
				 * Min Cash Val for guaranteed interest rate
				 * cash val expenese freq DXE: FULCVEFR
				 * cash val expense basis DXE: FULCVEBA
				 * cash val expense table DXEL FULCVETB
				 * Cash val expense rules 1,2, and 3 DXE:FULCVER1, FULCVER2, FULCVER3
				 */
					DXE = DXE + createDataExchange(fee.getFeeMode(),INF_AP_CV_EXP_FREQ,"NBA_PP_FEEMODE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
					DXE = DXE + createDataExchange(feeEx.getChargeBasis(),INF_AP_CV_EXP_BASIS,"NBA_PP_STMTBASIS",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633	
					DXE = DXE + createDataExchange(feeEx.getFeeTableIdentity(),INF_AP_CV_EXP_TBL,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_EXPENSE, CONTENTTYPE_PRIMARY), INF_AP_CV_EXP_RULE1, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);	//SPR2115
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_EXPENSE, CONTENTTYPE_SECONDARY), INF_AP_CV_EXP_RULE2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);	//SPR2115
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_EXPENSE, CONTENTTYPE_TERTIARY), INF_AP_CV_EXP_RULE3, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);	//SPR2115
			
					break;					
				case 15:
				//Partial Surrender
					//Contains the codes for allowing or disallowing partial surrenders. DXE: FULSPALW
					DXE = DXE + createDataExchange(feeEx.getProcessTimingOption(),INF_AP_PARTIAL_SURR_ALLOWED,"OLIEXT_LU_PROCESSTIMETYPE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
					//DXE: FULSPCTB
					DXE = DXE + createDataExchange(feeEx.getFeeTableIdentity(),INF_AP_PARTIAL_SURR_TABLE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					//partial surrender rules 1 & 2 DXE: FULSPCR1 FULSPCR2
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_WITHDRAWAL, CONTENTTYPE_PRIMARY), INF_AP_PARTIAL_SURR_RULE1, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2115
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_WITHDRAWAL, CONTENTTYPE_SECONDARY), INF_AP_PARTIAL_SURR_RULE2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2115
					//DXE: FULSPMIN
					DXE = DXE + createDataExchange(feeEx.getMinSingleWithdrawal(),INF_AP_PARTIAL_SURR_MIN,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					//DXE: FULSPNUM
					DXE = DXE + createDataExchange(feeEx.getMaxWithdrawalPerYear(),INF_AP_PARTIAL_SURR_NUM,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					if (feeEx.getMinBalanceCalcRuleCount() > 0){
						minBalanceCalcRule = feeEx.getMinBalanceCalcRuleAt(0);
						//DXE: FULSPBRU
						DXE = DXE + createDataExchange(minBalanceCalcRule.getMinBalanceCalcRuleType(), INF_AP_PART_SURR_MIN_BAL_CALC_RULE, NbaTableConstants.OLI_LU_MINBALCALCTYPE, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
						//DXE: FULSPBMT
						DXE = DXE + createDataExchange(minBalanceCalcRule.getMinMonthCOI(),INF_AP_PART_SURR_MIN_MON_COI,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						//DXE: FULSPBAM
						DXE = DXE + createDataExchange(minBalanceCalcRule.getFlatAmount(),INF_AP_PART_SURR_MIN_FLAT_AMT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					}
					break;
				case 16:
				//Full Surrender
					//DXE: FULSFALW; FULSFPRA; FULSFCTB; FULFLRUL
					DXE = DXE + createDataExchange(feeEx.getProcessTimingOption(),INF_AP_FULL_SURR_ALLOWED,"OLIEXT_LU_PROCESSTIMETYPE",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
					DXE = DXE + createDataExchange(feeEx.getRefundProRataInd(),INF_AP_FULL_SURR_PRORATA,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	 
					DXE = DXE + createDataExchange(feeEx.getFeeTableIdentity(),INF_AP_FULL_SURR_TABLE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
					//DXE: FULSFCR1 & FULSFCR2
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_SURRCHG, CONTENTTYPE_PRIMARY), INF_AP_FULL_SURR_RULE1, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2115
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_SURRCHG, CONTENTTYPE_SECONDARY), INF_AP_FULL_SURR_RULE2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2115
					DXE = DXE + createDataExchange(feeEx.getFreeLookRefundMethod(),INF_AP_FULL_SURR_RULE,"OLIEXT_LU_FREELOOKREFUNDMETHOD",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
					//DXE: FULGPRUL
					DXE = DXE + createDataExchange(feeEx.getChargeFreeWindowInd(),INF_AP_GUAR_PERIOD_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					//DXE: FULDPDYS
					DXE = DXE + createDataExchange(feeEx.getChargeFreeWindowDays(),INF_AP_GUAR_PERIOD_DAYS,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
					break;
				case 22:	//SPR2115
				//Non-traditonal Product Premium -- need fee type
				/*
				 * Premium Loads 
				 * Premium Load rules 1-3  DXE: FULPLRU1, FULPLRU2, & FULPLRU3
				 * Premium Load table DXE: FULPLTBL
				 * Premium Tax indicator DXE: FULPRMTX
				 */
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_PREMLOAD, CONTENTTYPE_PRIMARY), INF_AP_PREM_LOAD_RULE1, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2115
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_PREMLOAD, CONTENTTYPE_SECONDARY), INF_AP_PREM_LOAD_RULE2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2115
					DXE = DXE + createDataExchange(getRule(feeEx, OLI_FEE_PREMLOAD, CONTENTTYPE_TERTIARY), INF_AP_PREM_LOAD_RULE3, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2115
					DXE = DXE + createDataExchange(feeEx.getFeeTableIdentity(),INF_AP_PREM_LOAD_TBL,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);	
					DXE = DXE + createDataExchange(feeEx.getTaxCalcBasis(),INF_AP_PREM_TAX_IND,"OLIEXT_LU_TAXCALCBASIS",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633	
					break;
			}
		}
		if (polProdEx != null && polProdEx.getLoanProvisionCount() > 0){						
			for(int i = 0;i <  polProdEx.getLoanProvisionCount(); i++){
				loanProvision = polProdEx.getLoanProvisionAt(i);//SPR2289
				switch ((int)loanProvision.getLoanType()){
					case 1:
						//preferred credit adjustment amt DXE: 
						DXE = DXE + createDataExchange(loanProvision.getCreditIntAdjRate(),INF_AP_LOAN_PREF_CREDIT_ADJ_AMT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);			 
						//DXE: FULLPPYA
						DXE = DXE + createDataExchange(loanProvision.getFirstYearAvailable(),INF_AP_LOAN_PREF_YR_AVAIL,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);//NBA104
						//DXE: FULLPOPT
						DXE = DXE + createDataExchange(loanProvision.getMaxAvailableCalculation(),INF_AP_LOAN_PREF_OPTION,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);//SPR2289
						break;
					case 2:
						//Loan interest disbursement DXE : FULLINTD
						DXE = DXE + createDataExchange(loanProvision.getInterestAllocationRule(),INF_AP_LOAN_INT_DISB,"OLI_LU_ALLOCATIONOPTION",CYBTBL_UCT, CT_DEFAULT,0); //SPR1633
						//Loan int credit code DXE : FULLINTC
						DXE = DXE + createDataExchange(loanProvision.getCreditIntAdjRateRule(),INF_AP_LOAN_INT_CREDIT_CODE, NbaTableConstants.OLI_LU_CREDITADJ, CYBTBL_UCT, CT_DEFAULT,0); //SPR1633 SPR2115
						//Loan amt int rate DXE : FULLINRT	
						DXE = DXE + createDataExchange(loanProvision.getCreditIntAdjRate(),INF_AP_LOAN_INT_RATE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						//DXE : FULLMDUR
						DXE = DXE + createDataExchange(loanProvision.getFirstYearAvailable(),INF_AP_LOAN_MIN_DUR,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						//Loan min amount DXE: FULLMAMT
						DXE = DXE + createDataExchange(loanProvision.getMinSingleLoan(),INF_AP_LOAN_MIN_AMT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						//DXE: FULLPOPT
						DXE = DXE + createDataExchange(loanProvision.getMaxAvailableCalculation(),INF_AP_LOAN_PREF_OPTION,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						//DXE: FULLPACP
						DXE = DXE + createDataExchange(loanProvision.getMaxAvailablePercent(),INF_AP_LOAN_PREF_AMT_PERC,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
						//Deleted 2 lines NBA104
						for (int j =0; j < loanProvision.getRateVariationCount(); j++){
							rateVariation = loanProvision.getRateVariationAt(j);
							rateVariationEx = AccelProduct.getFirstRateVariationExtension(rateVariation); //SPR3290, NBA237
							if (rateVariationEx != null  && rateVariationEx.hasInterestUseType()){ //SPR1633
								//Pref. int. rate DXE:FULLPCRT
								DXE = DXE + createDataExchange(rateVariation.getInvestRate(),INF_AP_PREF_INT_RATE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0); //SPR1633//SPR2289
								break;	
							}			
						}//end for
						//DXE: FULLPCDR
						DXE = DXE + createDataExchange(loanProvision.getCreditIntAdjRateRule(), INF_AP_LOAN_PREF_CHARGE_RATE, NbaTableConstants.OLI_LU_CREDITADJ, CYBTBL_UCT, CT_DEFAULT, 0); //SPR2115
						//Begin NBA104
						//FULLMBTB
					MinBalanceCalcRule minBalCalcRule = null;
					long minBalCalcRuleType = 0;
					String tableIdentity = "";
					for (int j =0; j < loanProvision.getMinBalanceCalcRuleCount(); j++){
						minBalCalcRule = loanProvision.getMinBalanceCalcRuleAt(j);
						if(minBalCalcRule.getBalanceType() == 1){
							if(minBalCalcRule.getMinBalanceCalcRuleType() > minBalCalcRuleType){
								minBalCalcRuleType = minBalCalcRule.getMinBalanceCalcRuleType();
								tableIdentity = minBalCalcRule.getTableIdentity();
							}
						}
					}
					if(minBalCalcRuleType > 0){
						DXE = DXE + createDataExchange(tableIdentity, INF_AP_LOAN_MIN_CALC_TABLE, CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0); 
						//FULLMBRU
						DXE = DXE + createDataExchange(minBalCalcRuleType, INF_AP_LOAN_MIN_CALC_RULE, NbaTableConstants.OLI_LU_MINBALCALCTYPE, CYBTBL_UCT, CT_DEFAULT, 0); 
							
					}
						//End NBA104
						break;
				}//end switch
			}//end for
		}//end if			
		
		// begin NBA104
		if (polProdEx != null && isLife()) {
			//FULCVMAM
			if (polProdEx.hasMinCashValAmt()) {
				DXE = DXE + createDataExchange(polProdEx.getMinCashValAmt(),INF_AP_MIN_CV_BAL_AMT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			}
			//FULCVMIR
			if (polProdEx.hasMinCashValIntRate()) {
				DXE = DXE + createDataExchange(polProdEx.getMinCashValIntRate(),INF_AP_MIN_CV_BAL_INT,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			}
			//FULPANO
			if (polProdEx.hasMaxAddlPymtNum()) {   
				DXE = DXE + createDataExchange(polProdEx.getMaxAddlPymtNum(),INF_AP_ADDL_PAY_1YR,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);//SPR2003
			}
			//FULPADB
			DXE = DXE + createDataExchange(polProdEx.getAddlPymtDebtCodeInd()?"Y":"N",INF_AP_ADDL_PAY_DB_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			//FULPALMI
			if (polProdEx.hasMinAddlPymtAmt()) {
				DXE = DXE + createDataExchange(polProdEx.getMinAddlPymtAmt(),INF_AP_ADDL_PAY_LOW,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			}
			//FULPALMX
			if (polProdEx.hasMaxAddlPymtAmt()) {
				DXE = DXE + createDataExchange(polProdEx.getMaxAddlPymtAmt(),INF_AP_ADDL_PAY_HI,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			}
			//FULPALRU
			if (polProdEx.hasAddlPayMaxAmtRule()) {
				DXE = DXE + createDataExchange(polProdEx.getAddlPayMaxAmtRule(),INF_AP_ADDL_PAY_RULE,NbaTableConstants.OLIEXT_LU_ADDLPAYMAXAMTRULE,CYBTBL_UCT, CT_DEFAULT,0);
			} else {
				DXE = DXE + createDataExchange("0",INF_AP_ADDL_PAY_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			}
		}
		// end NBA104

		/*
		 * Set the corridor rule.  The host will set the amount or percentage.
		 * DXE: FULCRRUL
		 */
		if (ANNUITY_PROD.equals(planType)) {  //NBA104
			// NBA104 deleted code
			DXE = DXE + createDataExchange("0",INF_AP_CORR_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
		} else {
			DXE = DXE + createDataExchange("1",INF_AP_CORR_RULE,CYBTRANS_NONE,CYBTBL_NONE, CT_DEFAULT,0);
			// NBA104 deleted code
		}
		
		return DXE;
	}

	/**
	 * Create the rating segment data from the policy product information
	 * 
	 * @param policyProduct The current PolicyProduct XML object
	 * @param covType The type of product the coverage represents
	 * @return java.lang.String The rating DXE information obtained from the policy product information
	 */
	protected String createPolicyProductRatings(PolicyProduct policyProduct, String covType) { 
		
		// SPR3290 code deleted
		String ratingDXE = "";
		// SPR3290 code deleted
		LifeProductOrAnnuityProduct lifeProductOrAnnuityProduct = null;
		LifeProduct lifeProduct = null;
		CoverageProduct coverageProduct = null;
		CoverageProductExtension coverageProductEx = null;
		SubstandardRisk substandardRisk = null;
		boolean valueFound = false;

		// SPR3290 code deleted
		// NBA104 deleted code		
		lifeProductOrAnnuityProduct = policyProduct.getLifeProductOrAnnuityProduct();
		if (lifeProductOrAnnuityProduct.isLifeProduct())
			lifeProduct = lifeProductOrAnnuityProduct.getLifeProduct();
		if (lifeProduct == null){
			return"";
		}
		
		for(int i = 0; i < lifeProduct.getCoverageProductCount(); i++){
			coverageProduct = lifeProduct.getCoverageProductAt(i);
			if (covType.equals(coverageProduct.getProductCode())) {
				valueFound = true;
				break;		
			}
		}
		
		if (!valueFound){
			return "";		
		}

		coverageProductEx = AccelProduct.getFirstCoverageProductExtension(coverageProduct);  //NBA104, NBA237
		// NBA104 deleted code					
		if (coverageProductEx == null) {
			return "";
		}
		
		if (coverageProductEx.getSubstandardRiskCount() > 0){ 
			substandardRisk = coverageProductEx.getSubstandardRiskAt(0);
			// Begin SPR1707 
			String tempDXE = createDataExchange(substandardRisk.getSubstdPolicyReserveMethod(), SUB_STAND_RESERVES_PLAN, "OLI_LU_SUBSTDRESV", CYBTBL_UCT, CT_CHAR, 0); //SPR1633  //NBA104
			if (substandardRisk.getSubstdPolicyReserveMethod() == OLI_SUBSTDRESV_PP) {
				if (substandardRisk.getReserveTableIdentity() != null ) {
					tempDXE = tempDXE.substring(0, tempDXE.length() - 1) + substandardRisk.getReserveTableIdentity() + ";";
				}
			} else if (substandardRisk.getSubstdPolicyReserveMethod() == OLI_SUBSTDRESV_FP) {
				if (substandardRisk.hasSubstdPolicyReservePct()) {
					try {
						String reservePct = Double.toString(substandardRisk.getSubstdPolicyReservePct());
						reservePct = reservePct.substring(0, reservePct.indexOf("."));
						if (reservePct.length() == 1) {
							reservePct = "0" + reservePct; 
						}
						tempDXE = tempDXE.substring(0, tempDXE.length() - 1) + reservePct + ";";
					} catch (NumberFormatException e) {
						if (getLogger().isErrorEnabled()) {
							getLogger().logError("Problem generating " + SUB_STAND_RESERVES_PLAN + " SubstdPolicyReservePct = " + substandardRisk.getSubstdPolicyReservePct());
						}
					}
				}
			}
			ratingDXE = ratingDXE + tempDXE; 
			// End SPR1707
			
		}

		return ratingDXE;
	}
	/**
	 * Get the PolicyProduct object and decide what segment to build based on type passed in. 
	 * This method doesn't include Mortality table parameters and defaults them to none in
	 * the subsequent call to createPolicyProduct.
	 * 
	 * @param olife The OLifE XML object
	 * @param prodCode The type of product 
	 * @param type The type of segment needing policy product information
	 * @param fundType If called from the fund method, the type of fund information needed
	 * @return java.lang.String The DXE stream for the type of segment requested
	 */
	// SPR1633 New Method
	protected String createPolicyProduct(OLifE olife, String prodCode, int type, String fundType) {
		return createPolicyProduct(olife, prodCode, type, fundType, false, -1, "", null); //SPR3162
	}
	/**
	 * Get the PolicyProduct object and decide what segment to build based on type passed in
	 * 
	 * @param olife The OLifE XML object
	 * @param prodCode The type of product 
	 * @param type The type of segment needing policy product information
	 * @param fundType If called from the fund method, the type of fund information needed
	 * @param checkMort if specific mortality needs to be checked
	 * @param covGender the gender of relation to the coverage
	 * @param rateClass the rateClass in Coverage.Extension
	 * @param baseObject the object of entity for which the policy product fields to be processed
	 * @return java.lang.String The DXE stream for the type of segment requested
	 */
	// SPR1633 added new parameters, checkMort, covGender, and rateClass
	// SPR3162 added new parameter baseObject
	protected String createPolicyProduct(OLifE olife, String prodCode, int type, String fundType, boolean checkMort, long covGender, String rateClass, Object baseObject) { 
		
		PolicyProduct policyProduct = null;
		
		//This may need to be looped
		if (olife.getPolicyProductCount() < 1){
			//Default value per unit coded if policy product information does not exist
			if (type!=2){
				return "";
			}else if (planType.equals("F")){
				return "";	
			}else{ 
				return("FCVUNVAL=1000.00;");
			}
		}
		policyProduct = getNbaProduct().getPolicyProduct(prodCode); //NBA237
		if (policyProduct == null){
			if (type!=2){
				return "";
			}else if (planType.equals("F")){
				return "";	
			}else{ 
				return("FCVUNVAL=1000.00;");
			}
		}
	
		switch (type){
			case 1:
				return createPolicyProductBasic(policyProduct);
			case 2:
				return createPolicyProductCov(policyProduct, prodCode, checkMort, covGender, rateClass, (Coverage)baseObject); //SPR1633 //SPR3162
			case 3:
				return createPolicyProductRatings(policyProduct, prodCode);
			case 4:
				return createPolicyProductCovOpt(policyProduct, prodCode);
			case 5:
				return createPolicyProductFunds(policyProduct,fundType);
			case 6:
				return createPolicyProductAP(policyProduct, prodCode);
			case 7:
				return createPolicyProductMultipleFunds(policyProduct);
		
		}
		return "";
	}
	
	/**
	 * Create benefit (04 segment) DXE for inforce submittal.
	 * 
	 * @param covoption The current CovOption XML object
	 * @param olife The OLifE XML object
	 * @param coverage The Coverage object this covOption is tied to 
	 * @return java.lang.String The benefit DXE information
	 * @exception throws NbaBaseException
	 */
	protected String createCovOption(CovOption covoption, OLifE olife,Coverage coverage) throws NbaBaseException {
		String covOptDXE = "";
		CovOptionExtension covoptionEx = NbaUtils.getFirstCovOptionExtension(covoption); //SPR1986
		// SPR1986 code deleted
		// Coverage Option Key (Holding.Policy.Life.Coverage.CovOption) DXE: FSBBPHS
		//begin	SPR2082
		String covPhase = "";
		//if this is a policy level benefit override then override the phase code to be 01 
		if (covoptionEx != null) {
			if (covoptionEx.hasPolicyLevelBenefitInd() && covoptionEx.getPolicyLevelBenefitInd()) {
				covPhase = createDataExchange("01", COV_OPT_BENE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);//SPR2229
			}			
		}
		if (covPhase.length() == 0) {
			covPhase = createDataExchange(covoption.getCovOptionKey(), COV_OPT_BENE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		}
		covOptDXE = covOptDXE + covPhase;
		//end SPR2082		
		// NBA104 code deleted
		// Type of benefit (Holding.Policy.Life.Coverage.CovOption.LifeCovOptTypeCode) DXE: FSBBENEF
		// Typecode UCT (CLUDT135)  OLI_LU_OPTTYPE
		covOptDXE = covOptDXE + createDataExchange(covoption.getLifeCovOptTypeCode(), COV_OPT_TYPE, NbaTableConstants.OLI_LU_OPTTYPE, CYBTBL_UCT, CT_CHAR, 3); //SPR2082		
		// SPR2082 code deleted
		//set flagA bit 4 on if option amount is a pct not an amt
		if (covoption.getCovOptionPctInd()) {
			covOptDXE = covOptDXE + createDataExchange("1", COV_OPT_PERC_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		} else { //SPR1946
			covOptDXE = covOptDXE + createDataExchange("0", COV_OPT_PERC_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR1946
		}
		//begin NBA143
		//set CovOption Inherent Benefit Indicator DXE:FSBFLGB05
	    if (covoptionEx != null){
	        long selectionRule = covoptionEx.getSelectionRule();
		    if (selectionRule == NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM || selectionRule == NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM){
		        covOptDXE = covOptDXE + createDataExchange(INHERENT_COVOPTION_IND, INF_COV_OPT_SELECTRULE_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		    } 
	    }		
	    //end NBA143
		//Coverage Option Key (Holding.Policy.Life.Coverage.CovOption) DXE: FSBFLGB7
		String benefitStatus = getCyberValue( covoption.getCovOptionStatus(), NbaTableConstants.NBA_BENEFIT_STATUS, CYBTBL_UCT, compCode, DEFAULT_COVID);
		if (benefitStatus.equals("1")){
			covOptDXE = covOptDXE + createDataExchange("1", COV_OPT_STATUS, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			covOptDXE = covOptDXE + createDataExchange("0", "FSBBCUSE", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
		}else{
			covOptDXE = covOptDXE + createDataExchange("1", "FSBBCUSE", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
		}
			
		
		//coverage option effective date
		covOptDXE = covOptDXE + createDataExchange(covoption.getEffDate(), COV_OPT_EFFDATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);	
		//Coverage Option Cease Date DXE: FSBBCDTE
		covOptDXE = covOptDXE + createDataExchange(covoption.getTermDate(), COV_OPT_TERMDATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		//begin SPR1986
		if (covoptionEx != null) {
			//Coverage Option paidup date DXE:FSBPUDAT
			covOptDXE = covOptDXE + createDataExchange(covoptionEx.getPayUpDate(), INF_COV_OPT_PAYUP_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		} else {
			covOptDXE = covOptDXE + createDataExchange(covoption.getTermDate(), INF_COV_OPT_PAYUP_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		}
		//end SPR1986
	
		//SPR2082 code deleted
		// NBA104 code deleted	
		//FSBBUNT
		// NBA104 deleted code
		if (covoption.hasOptionNumberOfUnits()) {  //NBA104
			covOptDXE = covOptDXE + createDataExchange(covoption.getOptionNumberOfUnits(), COV_OPT_OPTION_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
		}
		// NBA104 code deleted
			
		//FSBPRMCP
		covOptDXE =covOptDXE + createDataExchange(covoption.getOptionPct(), INF_COV_OPT_PREM_CALC_PERC, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
		//FSBBRAT
		covOptDXE =covOptDXE + createDataExchange(covoption.getPermPercentageLoading(), COV_OPT_PERC, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
		//begin NBA111
		if (covoption.hasLivesType() && covoption.getLivesType() == NbaOliConstants.OLI_COVLIVES_JOINTFTD) {
			//FSBFLGA3 This DEX is used to determine the Lives Type of Coverage Option
			covOptDXE = covOptDXE + createDataExchange(FLAG_BIT_ON, COV_OPT_LIVES_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//FSBBAGE Benefit Issue Age
			if (coverage != null) {
				if (coverage.getLifeParticipantCount() > 0) {
					LifeParticipant lifePar = coverage.getLifeParticipantAt(0);
					if (lifePar.hasIssueAge()) {
						covOptDXE =
							covOptDXE + createDataExchange(lifePar.getIssueAge(), INF_COV_TRUE_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 3);
					}
				}
			}
		} else if (covoption.hasLivesType() && covoption.getLivesType() == NbaOliConstants.OLI_COVLIVES_SINGLE) {
			//FSBFLGA3
			covOptDXE = covOptDXE + createDataExchange(FLAG_BIT_OFF, COV_OPT_LIVES_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		}
		if (coverage.hasLivesType() && NbaUtils.isJointLife(coverage.getLivesType())) {
			//FSBPIDNT
			String relRoleCode = "";
			String sPersonID;
			int roleTot;
			String roleTypeKey;
			if (covoption.hasLivesType()) {
				if (covoption.getLivesType() != NbaOliConstants.OLI_COVLIVES_JOINTFTD) {
					//Gets the life participant associated with cov Option
					LifeParticipant lifeParticipant = NbaUtils.getLifeParticipantReference(covoption, coverage);
					if (lifeParticipant != null) {
						if (planType.equals("F")) {
							relRoleCode =
								getCyberValue(
									lifeParticipant.getLifeParticipantRoleCode(),
									CYBTRANS_ROLES,
									CYBTBL_PARTIC_ROLE_F,
									compCode,
									DEFAULT_COVID);

						} else {
							relRoleCode =
								getCyberValue(
									lifeParticipant.getLifeParticipantRoleCode(),
									CYBTRANS_ROLES,
									CYBTBL_PARTIC_ROLE,
									compCode,
									DEFAULT_COVID);

						}
					}
				} else if (covoption.getLivesType() != NbaOliConstants.OLI_COVLIVES_JOINTFTD) {
					if (planType.equals("F")) {
						relRoleCode =
							getCyberValue(NbaOliConstants.OLI_PARTICROLE_PRIMARY, CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE_F, compCode, DEFAULT_COVID);

					} else {

						relRoleCode =
							getCyberValue(NbaOliConstants.OLI_PARTICROLE_PRIMARY, CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
					}
				}
				//Joint Coverage Option (Holding.Policy.Life.CovOption.LivesType OLife Value=2) on Joint Coverage
				//Default covered person identifier as primary insured (FSBPIDNT=0001).
				if (NbaOliConstants.OLI_COVLIVES_JOINTFTD != covoption.getLivesType()) { //SPR2202
					//Concatenating coverage Id with relation Code .As counting has to be restarted with each  coverage
					roleTypeKey = coverage.getId() + ":" + relRoleCode;
					if (lifeParticipantRoleTypeMap == null) {
						lifeParticipantRoleTypeMap = new HashMap(3, 0.9F);
					}

					if (lifeParticipantRoleTypeMap.containsKey(roleTypeKey)) {
						roleTot = ((Integer) lifeParticipantRoleTypeMap.get(roleTypeKey)).intValue() + 1;
					} else {
						roleTot = 1;
					}
					lifeParticipantRoleTypeMap.put(roleTypeKey, new Integer(roleTot));
					sPersonID = relRoleCode + formatCyberInt(roleTot);
					covOptDXE = covOptDXE + createDataExchange(sPersonID, COV_OPT_PERSON_IDREF, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);
				} else { //SPR2202
					covOptDXE = covOptDXE + createDataExchange(JOINT_PRIMARY_INS, COV_OPT_PERSON_IDREF, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);//SPR2202
				}//SPR2202

			}
		}
		//end NBA111		
		benefitCode = covoption.getProductCode();
		
		return covOptDXE + createPolicyProduct(olife,productCode,4,"");
	}

	/**
	 * Create CWA (47 Segment) DXE for inforce submittal
	 * 
	 * @param policy com.csc.fsg.nba.vo.txlife.Policy
	 * @return java.lang.String
	 */
	protected String createCWA(Policy policy) throws NbaBaseException {
		String cwaDXE = "";
		FinancialActivity financialactivity;
		// SPR3290 code deleted

		for (int i = 0; i < policy.getFinancialActivityCount(); i++) {
			financialactivity = policy.getFinancialActivityAt(i);
			// SPR3290 code deleted

			//SPR3290 code deleted

			// CWA Amount (Holding.Policy.FinancialActivity) FCWAMONT
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberCurrency(financialactivity.getFinActivityGrossAmt()),
						FIN_GROSS_AMT,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DOUBLE,
						0);
		

			// CWA Date (Holding.Policy.FinancialActivity) FCWASOF
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberDate(financialactivity.getFinActivityDate()),
						FIN_ACT_DATE,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DEFAULT,
						0);

			// CWA Type (Holding.Policy.FinancialActivity) FCWCHGTP
			// VT_I4 (Typecode) UCT (CLCWATP1)  OLIEXT_LU_CWATYPE
			//VT_I4 (Typecode) UCT (CLCWATP1)  OLI_LU_FINACTTYPE  SPR1886 
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberLong(financialactivity.getFinActivityType()),
						CWA_TYP_CODE,
						NbaTableConstants.OLI_LU_FINACTTYPE,  //SPR1886
						CYBTBL_UCT,
						CT_CHAR,
						1);
		
			// CWA Entry Date (Holding.Policy.FinancialActivity) FCWENDAT
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberDate(financialactivity.getFinActivityDate()),
						CWA_ENT_DATE,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DEFAULT,
						0);
	
	
			// User costAdj to send to host for dtrain - Cost Basis Amount (Holding.Policy.FinancialActivity) COSTBASE
			if (financialactivity.hasCostBasisAdjAmt()) 
				cwaDXE =
					cwaDXE
						+ createDataExchange(
							formatCyberCurrency(financialactivity.getCostBasisAdjAmt()),
							COST_BASIS_ADJ_AMT,
							CYBTRANS_NONE,
							CYBTBL_NONE,
							CT_DOUBLE,
							0);

			// If the asof date of  the CWA payment is to be the issue date, turn on bit in FCWFLAGB. - Set CWA Date to Issue Indicator (Holding.Policy.FinancialActivity.CWAActivity) FCWFLAGB
			// VT_I4 (Typecode)  OLI_LU_BOOLEAN Current Extension
			cwaDXE =
				cwaDXE + createDataExchange(formatCyberBoolean(financialactivity.getIntTreatmentInd()), CW_FLAGB_3, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);

			// Override Interest (Holding.Policy.FinancialActivity) FCWOVINT
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberDouble(financialactivity.getIntPostingRate()),
						OVERRIDE_INT,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DOUBLE,
						0);

			// Pre-TEFRA Indicator (Holding.Policy.Life.LifeUSA) FCWFLAGB
			if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) 
				if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) { 
					Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife(); 
					if (life.hasLifeUSA())
						if (life.getLifeUSA().hasTaxGrandfatheredType())
							cwaDXE =
								cwaDXE
									+ createDataExchange(
										formatCyberLong(life.getLifeUSA().getTaxGrandfatheredType()),
										CW_FLAGB_6,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_CHAR,
										8);

				}
		
			// CWA Originator (Defaults to Dept./Desk Code) (Holding.Policy.FinancialActivity.CWAActivity) FCWORGNT
			cwaDXE = cwaDXE + createDataExchange(financialactivity.getUserCode(), CWA_DPT_DESK, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 6);  //NBA093
		}
		return cwaDXE;
	}
	/**
	 * Set the value for planType from an OLifE
	 * @param olife
	 */
	// SPR1186 New Method
	protected void setPlanType(OLifE olife) {
		planType = "";
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		Policy policy = holding.getPolicy();
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (ladh != null && ladh.isAnnuity()) {
			setPlanType(ANNUITY_PROD);
		} else if (nbaTXLife.isLife()) {
			setPlanType(LIFE_PROD);
		}
	}
	/**
	 * Create the Automatic transaction segment
	 * 
	 * @param olife The OLifE XML object
	 * @return java.lang.String The automatic transaction DXE information
	 * @exception throws NbaBaseException
	 */
	protected String createAutomaticTransactions(OLifE olife) throws NbaBaseException {
		setPlanType(olife);//SPR1186 
		int relTbl = getPlanType().equals(ANNUITY_PROD) ? CYBTBL_RELATION_ROLE_F : CYBTBL_RELATION_ROLE; //SPR1186
		Holding holding = null;
		Relation relation = null;
		Arrangement arrangement = null;
		ArrangementExtension arrangementEx= null;
		// SPR3290 code deleted
		MaxDisbursePctOrMaxDisburseAmt maxDisbursePctOrMaxDisburseAmt = null;
		ArrDestination arrDestination = null;
		ArrSource arrSource = null;
		int arrCount = 0; // SPR3290
		String arrDXE = "";
		String autxDXE = "";
		String arrTranslation = "";

		try {
			holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); 
			arrCount = holding.getArrangementCount(); 

			for (int i = 0; i < arrCount; i++) {
				arrangement = holding.getArrangementAt(i);
				//Set automatic trasaction indicator DXE:FDEATACT
				//autxDXE = autxDXE + createDataExchange("1", "FDEATACT", CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				// frequency of automatic transaction DXE:FAUTXNTP
				// OLI_LU_ARRTYPE
				//Determine the arrangement type for BES
				//autxDXE = autxDXE + createDataExchange(arrangement.getArrType(),INF_AUTO_TRANS_TYPE,"OLI_LU_ARRTYPE", CYBTBL_UCT, CT_DEFAULT, 0);
				//arrDXE = arrDXE + createDataExchange(//autxDXE = autxDXE + createDataExchange(arrangement.getArrType(),INF_AUTO_TRANS_TYPE,"OLI_LU_ARRTYPE", CYBTBL_UCT, CT_DEFAULT, 0);
				//arrDXE = arrDXE + createDataExchange(arrangement.getArrType(),"FALTYPE","OLI_LU_ARRTYPE", CYBTBL_UCT, CT_DEFAULT, 0);
			
				switch ((int) arrangement.getArrType()){
					case 2:
						arrTranslation = "A";
						break;
					case 3:
						arrTranslation = "R";
						break;
				}
				autxDXE = autxDXE + createDataExchange(arrTranslation,INF_AUTO_TRANS_TYPE,"OLI_LU_ARRTYPE", CYBTBL_UCT, CT_DEFAULT, 0);
				arrDXE = arrDXE + createDataExchange(arrTranslation,"FALTYPE","OLI_LU_ARRTYPE", CYBTBL_UCT, CT_DEFAULT, 0);
				
				autxDXE = autxDXE + createDataExchange(++FAU_Seq, INF_AUTO_TRANS_SEQ, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				// frequency of automatic transaction DXE:FAUFREQ
				// OLI_LU_PAYMODE
				autxDXE = autxDXE + createDataExchange(arrangement.getArrMode(),INF_PAYOUT_MODE,"OLI_LU_PAYMODE", CYBTBL_UCT, CT_DEFAULT, 0);
				
				//Surrender Charge Amount automatic transaction DXE:FAUOVRAM
				autxDXE = autxDXE + createDataExchange(arrangement.getSurrenderChargeAmt(),INF_AUTO_TRANS_OVRD_AMT,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 12);
				arrangementEx = NbaUtils.getFirstArrangementExtension(arrangement); //SPR3290
				//End Date of automatic transaction DXE:FAUMAXDP
				if (arrangementEx !=null){
					maxDisbursePctOrMaxDisburseAmt = arrangementEx.getMaxDisbursePctOrMaxDisburseAmt();
					//DXE FAUMAXDP or FAUMAXDA
					if (maxDisbursePctOrMaxDisburseAmt != null){
						if (maxDisbursePctOrMaxDisburseAmt.isMaxDisbursePct())
							autxDXE = autxDXE + createDataExchange(maxDisbursePctOrMaxDisburseAmt.getMaxDisbursePct(),INF_AUTO_TRANS_MAX_DISB_PCT,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 12);
						else if (maxDisbursePctOrMaxDisburseAmt.isMaxDisburseAmt())
							autxDXE = autxDXE + createDataExchange(maxDisbursePctOrMaxDisburseAmt.getMaxDisburseAmt(),INF_AUTO_TRANS_MAX_DISB_AMT,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 12);
					}
					//DXE:FAUYRIND
					if (arrangementEx.hasWithdrawalBasis())
						autxDXE = autxDXE + createDataExchange(arrangementEx.getWithdrawalBasis(),INF_AUTO_TRANS_WD_BASIS,"OLI_LU_STMTBASIS", CYBTBL_UCT, CT_DEFAULT, 12);
					//DXE:FAUFNDAL
					autxDXE = autxDXE + createDataExchange(arrangementEx.getWithdrawalAllocationRule(),INF_AUTO_TRANS_WD_ALLOC_RULE,"OLIEXT_LU_FUNDALLOCRULE", CYBTBL_UCT, CT_DEFAULT, 12);
					
				}
				
				//Start Date of automatic transaction DXE:FAUSTDTE
				if ((arrangementEx !=null)&& (arrangementEx.getSetToIssDateInd()))
					autxDXE = autxDXE + createDataExchange("19000000",INF_PAYOUT_START_DATE,CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
				else
					autxDXE = autxDXE + createDataExchange(arrangement.getStartDate(),INF_PAYOUT_START_DATE,CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
				//End Date/Suspend Date of automatic transaction DXE:FAUCSDTE/FAUSUSDT
				if (arrangement.hasEndDate())
					autxDXE = autxDXE + createDataExchange(arrangement.getEndDate(),INF_PAYOUT_END_DATE,CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
				else if((arrangementEx !=null) &&(arrangementEx.hasSuspendDate()))
					autxDXE = autxDXE + createDataExchange(arrangementEx.getSuspendDate(),INF_AUTO_TRANS_SUSPEND_DATE,CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);

	
				if (arrangementEx !=null){
					autxDXE = autxDXE + createDataExchange(arrangementEx.getMinBalOvrdInd(),INF_AUTO_TRANS_MINBALOVRD,CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
					autxDXE = autxDXE + createDataExchange(arrangementEx.getSurrChgOvrdInd(),INF_AUTO_TRANS_SURRGHROVRD,CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
				}
				
				int arrSourceCount = arrangement.getArrSourceCount();
				for (int j = 0; j < arrSourceCount; j++) {
					arrSource = arrangement.getArrSourceAt(j);
					arrDXE = arrDXE + createDataExchange(getSubAcctID(holding.getInvestment(),arrSource.getSubAcctID()),FUND_ID,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					arrDXE = arrDXE + createDataExchange(arrSource.getTransferAmtType(),FUND_ALLOC_TYPE,"OLI_LU_TRNSFRAMTTYPE", CYBTBL_UCT, CT_DEFAULT, 0);	
					if (arrSource.hasTransferAmt()){	
						arrDXE = arrDXE + createDataExchange(arrSource.getTransferAmt(), FUND_ALLOC_VAL,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					}	
					if (arrSource.hasTransferPct()){
						arrDXE = arrDXE + createDataExchange(arrSource.getTransferPct(),FUND_ALLOC_PCT,CYBTRANS_NONE, CYBTBL_NONE,CT_DEFAULT, 0);	
					}
					//DXE: FALFRMTO DXE:FALEXCLD
					arrDXE = arrDXE + createDataExchange("F", INF_ALLOC_FROM_TO_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					arrDXE = arrDXE + createDataExchange("0", INF_ALLOC_FUND_EXCLUSION_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
				
				int arrDestCount = arrangement.getArrDestinationCount();
				for (int j = 0; j < arrDestCount; j++) {
					arrDestination = arrangement.getArrDestinationAt(j);
					if (arrDestination.hasPolNumber()){
						//get relation for the payment party
						//DXE: FAUPAYEI
						Object[] relations = olife.getRelation().toArray();
						relation = NbaUtils.getRelationForParty(arrDestination.getPaymentPartyID(), relations);
						String relRoleCode =
								getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, relTbl, compCode, DEFAULT_COVID);	//SPR1186
						String sPartyId = getPartyId(arrDestination.getPaymentPartyID(), relRoleCode);
						
						autxDXE = autxDXE + createDataExchange(sPartyId,INF_AUTO_TRANS_PAYEE_INDV,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
						//DXE: FAUPOLNO	
						autxDXE = autxDXE + createDataExchange(arrDestination.getPolNumber(),INF_AUTO_TRANS_POLNUM,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);	
					}
					arrDXE = arrDXE + createDataExchange(getSubAcctID(holding.getInvestment(),arrDestination.getSubAcctID()),FUND_ID,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					arrDXE = arrDXE + createDataExchange(arrDestination.getTransferAmtType(),FUND_ALLOC_TYPE,"OLI_LU_TRNSFRAMTTYPE", CYBTBL_UCT, CT_DEFAULT, 0);	
					if (arrDestination.hasTransferAmt()){	
						arrDXE = arrDXE + createDataExchange(arrDestination.getTransferAmt(),FUND_ALLOC_VAL,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					}	
					if (arrDestination.hasTransferPct()){	
						arrDXE = arrDXE + createDataExchange(arrDestination.getTransferPct(),FUND_ALLOC_PCT,CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					}			
					//arrDXE = arrDXE + createDataExchange(arrDestination.getPaymentForm(),"FAUPAYPT","OLI_LU_PAYMENTFORM", CYBTBL_UCT, CT_DEFAULT, 0);
					arrDXE = arrDXE + createDataExchange("T", INF_ALLOC_FROM_TO_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					arrDXE = arrDXE + createDataExchange("0", INF_ALLOC_FUND_EXCLUSION_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);

				}
			}
		} catch (Exception e) {
			
			throw new NbaBaseException("Error parsing arrangements", e);
		}

		return autxDXE + arrDXE;
	}
	/**
	 * Method to create coverage (02 segment) values. If contract is a life contract the coverage method is called. If contract is an
	 * annuity then the annuity method is called.
	 * @param olife The OLifE XML object
	 * @return The coverage DXE information
	 * @exception throws NbaBaseException 
	 */
	protected String createLife(OLifE olife) throws NbaBaseException {
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); 
		Policy policy = holding.getPolicy();
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null; 
		Life life = null;
		Annuity annuity = null;
		String dxe = "";
		int iCovTot;

		if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) { 
			lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); 
			if (lifeOrAnnuityOrDisabilityHealth.isLife()){
				life = lifeOrAnnuityOrDisabilityHealth.getLife();
			}else if (lifeOrAnnuityOrDisabilityHealth.isAnnuity()){
				annuity = lifeOrAnnuityOrDisabilityHealth.getAnnuity();
			}else {
				if (getLogger().isWarnEnabled()){
					getLogger().logWarn("Error Policy XML has no Life information");
				}
				return "";
			}
		} else {
			if (getLogger().isInfoEnabled()){
				getLogger().logInfo("Error Policy XML has no life, annuity, or disability health information");
			}
			return "";
		}

		if (life != null) {
			// Individual Life fields
			//

			// Coverages
			iCovTot = life.getCoverageCount();
			for (int i = 0; i < iCovTot; i++) {
				Coverage coverage = life.getCoverageAt(i);
				dxe = dxe + createCoverage(olife, coverage, policy, life);
				classSeriesFound = false;
			}
		}
		if (annuity != null) {
			dxe = dxe + createAnnuity(olife, annuity, policy);
		}
		
		return dxe;
	}

	/**
	 * Create Allocation (57) and Individual Fund Control (55) segments.
	 * 
	 * @param olife The OLifE XML object
	 * @return java.lang.String The investment DXE
	 */
	protected String createInvestment(OLifE olife) throws NbaBaseException {	
		//begin SPR1929
		StringBuffer invDXE = new StringBuffer(); //SPR1929
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		productCode = holding.getPolicy().getProductCode();
		Investment investment = holding.getInvestment();
		int count = investment.getSubAccountCount();
		//Create payment, withdrawal, and charge allocations
		invDXE.append(createAllocations(investment, count, OLI_SYSACTTYPE_DEPT)); //payments
		invDXE.append(createAllocations(investment, count, OLI_SYSACTTYPE_WTHDRW)); //withdrawals	
		invDXE.append(createAllocations(investment, count, OLI_SYSACTTYPE_CHARGES)); //charges
		// SPR3290 code deleted
		Map funds = new HashMap();
		SubAccount subaccount;
		//Create Individual Fund Control segments for each unique fund (SubAccount.ProductCode)
		for (int i = 0; i < count; i++) {
			subaccount = investment.getSubAccountAt(i);
			if (subaccount.hasProductCode() && !funds.containsKey(subaccount.getProductCode())) {
				funds.put(subaccount.getProductCode(), "");
				invDXE.append(createPolicyProduct(olife, holding.getPolicy().getProductCode(), 5, subaccount.getProductCode()));
			}
		}
		return invDXE.toString();
		//end SPR1929
	}
	/**
	 * Create the people DXE 
	 * 
	 * @param The OLifE XML object
	 * @return java.lang.String The DXE for the party objects
	 */
	protected String createParties(OLifE olife) throws NbaBaseException {
		// This map will help determine how many of each role type we have
		HashMap roleTypeMap = new HashMap();
		Party party;
		Relation relation;
		int i = 0, j, pTot, rTot;
		String partyId;
		String partyDXE;
		int partyOcc = 0;
		String relPID = new String();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); 

		// Initialize result strings
		partyDXE = "";
		addressDXE = "";

		pTot = olife.getPartyCount();
		rTot = olife.getRelationCount();
		// for each of our party objects
		for (i = 0; i < pTot; i++) {
			party = olife.getPartyAt(i);
			partyId = party.getId();
			partyOcc = getOccurance(partyId);
			if (partyOcc == -1) {
				// This is an error condition.  We cannot process this party
				continue;
			}
			// for each of our relation objects
			for (j = 0; j < rTot; j++) {
				relation = olife.getRelationAt(j);
				String relatedObjectID = relation.getRelatedObjectID();
				// see if this a relationship for our party
				if (relatedObjectID.compareTo(partyId) == 0) {
					//
					// process Party/Relationship
					//
					partyDXE = partyDXE + createPartyWithRelation(party, relation, roleTypeMap, holding);
				}
			}
		}
		//
		// Repeat loop to build beneficiary information
		//
		for (i = 0; i < pTot; i++) {
			party = olife.getPartyAt(i);
			partyId = party.getId();
			partyOcc = getOccurance(partyId);
			if (partyOcc == -1) {
				// This is an error condition.  We cannot process this party
				continue;
			}
			// for each of our relation objects
			for (j = 0; j < rTot; j++) {
				relation = olife.getRelationAt(j);
				String relatedObjectID = relation.getRelatedObjectID();
				// see if this a relationship for our party
				if (relatedObjectID.compareTo(partyId) == 0) {
					//
					// process Party/Relationship
					//
					if (relation.getRelationRoleCode() == 34) {
						if (getPartyId(relation.getOriginatingObjectID(), "00") != "") {
							relPID = getPartyId(relation.getOriginatingObjectID(), "00");
						} else if (getPartyId(relation.getOriginatingObjectID(), "01") != "") {
							relPID = getPartyId(relation.getOriginatingObjectID(), "01");
						} else if (getPartyId(relation.getOriginatingObjectID(), "60") != "") {
							relPID = getPartyId(relation.getOriginatingObjectID(), "60");
						} else if (getPartyId(relation.getOriginatingObjectID(), "40") != "") {
							relPID = getPartyId(relation.getOriginatingObjectID(), "40");
						} else if (getPartyId(relation.getOriginatingObjectID(), "50") != "") {
							relPID = getPartyId(relation.getOriginatingObjectID(), "50");
						}
						if (beneficiaryDXE.indexOf(relPID) == -1) {
							beneficiaryDXE = beneficiaryDXE + formatDataExchange(relPID, BENE_ORIG_ID, CT_CHAR, 12);
						}

						beneficiaryDXE = beneficiaryDXE + createBeneficiary(party, relation, roleTypeMap);
					}
				}
			}
		}
		return partyDXE + addressDXE;
	}
	/**
	 * Match party with relation 
	 * 
	 * @param party The current Party XML object
	 * @param relation The current Relation XML object
	 * @param roleTypemap A HashMap of Relation role codes
	 * @param olife The OLifE XML object
	 * @return java.lang.String The party DXE information
	 * @exception throws NbaBaseException
	 */
	protected String createPartyWithRelation(Party party, Relation relation, HashMap roleTypeMap, Holding holding) throws NbaBaseException {
		OLifEExtension olifeEx;
		int roleTot;
		String sPersonId;
		Organization organization;
		PersonOrOrganization perOrOrg;
		Person person;
		// SPR3290 code deleted
		RelationProducerExtension relprodEx;
		PartyExtension partyEx;
		// SPR3290 code deleted
		String partyDXE = "";
		boolean setServicingAgent = false;
		try {
			// 
			switch ((int) relation.getRelationRoleCode()) {
				// FAGAGTID=CK101;FAGUSECD=;FAGSITCD=;FSPTRSEQ=0;FAGPCTSH=100;FAGPCTSV=100;
				case (int)NbaOliConstants.OLI_REL_PRIMAGENT : // writing agent  NBA093
					if (party.hasProducer()) {
						Producer producer = party.getProducer();
						if (producer.getCarrierAppointmentCount() > 0) {
							CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
							// Servicing Agency
							writingAgentDXE =
								writingAgentDXE
									+ createDataExchange(
										carrierAppointment.getCompanyProducerID(),
										WRITING_AGT,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_CHAR,
										11);
							writingAgentDXE =
								writingAgentDXE
									+ createDataExchange(
										"01",
										"FAGAGTLV",
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_CHAR,
										11);
							//set servicing Agent to writing agent unless a servicing agent is present then override
							if (!setServicingAgent){
								servicingAgent = carrierAppointment.getCompanyProducerID();
							}
						}
					}
					for (int i = 0; i < relation.getOLifEExtensionCount(); i++) {
						olifeEx = relation.getOLifEExtensionAt(i);
						if (olifeEx.isRelationProducerExtension()) {
							relprodEx = olifeEx.getRelationProducerExtension();
							// Situation Code (Relation.RelationProducer)
							//  UCT (CLCMTB02)  nbA table = OLIEXT_LU_SITCODE
							writingAgentDXE =
								writingAgentDXE
									+ createDataExchange(relprodEx.getSituationCode(), SITUATION_CD, "OLIEXT_LU_SITCODE", CYBTBL_UCT_BY_INDEX_TRANS, CT_CHAR, 0);
							
							// Volumn Share Percent (Relation.RelationProducer)
							writingAgentDXE =
								writingAgentDXE
									+ createDataExchange(
										formatCyberDouble(relation.getVolumeSharePct()),
										VOL_SHARE_PCT,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_DEFAULT,
										0);
							//NBA093
						}
						// client OLifeExtention not sent to host
					}
					// If the interest percent for a beneficiary equals 100% the Beneficiary Distribution Option should be set to Balance and the Interest Percent amount should not be sent to the host. - Percentage of distribution for beneficiary (Relation)
					writingAgentDXE =
						writingAgentDXE
							+ createDataExchange(
								formatCyberDouble(relation.getInterestPercent()),
								COM_SHARE_PCT,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_DOUBLE,
								0);
					return "";
					// Once the program locates that role it will then it will go to that Party ID within the 
					// Party object to find the agent/agency ID
					// FBRAGENT=;FBRAGNCY=;
				case (int)NbaOliConstants.OLI_REL_SERVAGENT : // servicing agent NBA093
					// Producer Info
					if (party.hasProducer()) {
						Producer producer = party.getProducer();
						if (producer.getCarrierAppointmentCount() > 0) {
							// Servicing Agency
							//Override what the writing agent if servicing agent is present
							CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
							servicingAgent = carrierAppointment.getCompanyProducerID();	
						}
					}
					return "";
				case (int)NbaOliConstants.OLI_REL_SERVAGENCY : // Servicing Agency  NBA093
					perOrOrg = party.getPersonOrOrganization();
					person = perOrOrg.getPerson();
					if (person != null){
						basicDXE =
							basicDXE
								+ createDataExchange(
									person.getLastName(),
									AGENCY_NAME,
									CYBTRANS_NONE,
									CYBTBL_NONE,
									CT_CHAR,
									11);
					}
					return "";
			}
			String relRoleCode;
			if (planType.compareTo("F") == 0)
				relRoleCode = getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE_F, compCode, DEFAULT_COVID);
			else
				relRoleCode = getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE, compCode, DEFAULT_COVID);
			if (relRoleCode.compareTo("") == 0) {
				if (getLogger().isWarnEnabled())
					getLogger().logWarn(
						"Could not process Party: '"
							+ party.getId()
							+ "' Relation: '"
							+ relation.getId()
							+ ", no translation provided for relRolCode - Table 'NBA_ROLES', relRoleCode '"
							+ Long.toString(relation.getRelationRoleCode())
							+ "', Company Code = '"
							+ compCode
							+ "', Cov Key = '"
							+ DEFAULT_COVID
							+ "'.");
				return "";
			}
			if (roleTypeMap.containsKey(relRoleCode))
				roleTot = ((Integer) roleTypeMap.get(relRoleCode)).intValue() + 1;
			else
				roleTot = 1;
			roleTypeMap.put(relRoleCode, new Integer(roleTot));
			sPersonId = relRoleCode + formatCyberInt(roleTot);
			partyDXE = partyDXE + formatDataExchange(sPersonId, PERS_SEQ_ID, CT_CHAR, 12);
			//
			// Add this user id to the partyMap for later
			addPartyId(party.getId(), relRoleCode, sPersonId);
			// initialize fields
			//
			person = null;
			organization = null;
			if (party.hasPersonOrOrganization()) {
				perOrOrg = party.getPersonOrOrganization();
				if (perOrOrg.isPerson()) {
					person = perOrOrg.getPerson();
				} else {
					organization = perOrOrg.getOrganization();
				}
			}
			//
			// Person Info
			if (person != null) {
				// First Name
				if (person.hasFirstName())
					partyDXE = partyDXE + createDataExchange(person.getFirstName(), FIRST_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 20);
				// Middle Name or initial
				if (person.hasMiddleName())
					partyDXE = partyDXE + createDataExchange(person.getMiddleName(), MIDDLE_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
				// Last Name
				if (person.hasLastName())
					partyDXE = partyDXE + createDataExchange(person.getLastName(), LAST_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 20);
				// Prefix
				// UCT (CLUDT150) OLIEXT_LU_PREFIX
				if (person.hasPrefix())
					partyDXE = partyDXE + createDataExchange(person.getPrefix(), PREFIX, NbaTableConstants.OLIEXT_LU_PREFIX, CYBTBL_UCT, CT_CHAR, 9);
				// Suffix
				if (person.hasSuffix())
					partyDXE = partyDXE + createDataExchange(person.getSuffix(), SUFFIX, NbaTableConstants.OLIEXT_LU_SUFFIX, CYBTBL_UCT, CT_CHAR, 9);
				// Age (Party.Person)
				if (person.hasAge())
					partyDXE = partyDXE + createDataExchange(formatCyberInt(person.getAge()), AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0);
				// Date of Birth (Party.Person)
				if (person.hasBirthDate())
					partyDXE =
						partyDXE + createDataExchange(formatCyberDate(person.getBirthDate()), BIRTH_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				// Gender Code (Party.Person) DXE: FLCSEXCD
				// Typecode UCT (CLSEXCD)  OLI_LU_GENDER
				if (person.hasGender())
					partyDXE = partyDXE + createDataExchange(person.getGender(), GENDER, "OLI_LU_GENDER", CYBTBL_UCT, CT_CHAR, 1);
				// Marital Status (Party.Person) DXE: FLCMARST
				// Typecode UCT (CLPCTB27)  OLI_LU_MARSTAT
				if (person.hasMarStat())
					partyDXE = partyDXE + createDataExchange(person.getMarStat(), MAR_ST, "OLI_LU_MARSTAT", CYBTBL_UCT, CT_CHAR, 1);
				
				PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);	//SPR1778
				if (personExtension != null && personExtension.hasRateClass())//SPR1778  
							partyDXE =
								partyDXE
									+ createDataExchange(
										personExtension.getRateClass(), //SPR1778 
										SMOKE_STATUS,
										CYBTRANS_NONE, //SPR1778  
										CYBTBL_NONE, //SPR1778  							
										CT_CHAR,
										1); //SPR1778 
				
				// Birth Place Code (Party.Person)
				// Typecode UCT (ST_CTL)  OLI_LU_STATE
				if (person.hasBirthJurisdictionTC()) //NBA093
					partyDXE =
						partyDXE
							+ createDataExchange(
								formatCyberLong(person.getBirthJurisdictionTC()), 
								BIRTH_STATE,
								NbaTableConstants.NBA_STATES,
								CYBTBL_STATE_TC,
								CT_CHAR,
								3);
				
				// Typecode UCT (CEUDT121)  nbA table = NBA_CITIZENSHIP
				if (person.hasCitizenship())
					partyDXE = partyDXE + createDataExchange(person.getCitizenship(), CLIENT_CITIZENSHIP, "NBA_Citizenship", CYBTBL_UCT, CT_CHAR, 1);
			}
			//End Person Info
		
			//If is insured
			if (relRoleCode.compareTo("00") == 0) {
				//  FBRRCTRY
				// Should this stay party of become basic DXE
				if (party.hasResidenceCountry())
					basicDXE =
						basicDXE +  createDataExchange(party.getResidenceCountry(), RESIDENCE_CNTRY, "OLI_LU_NATION", CYBTBL_UCT, CT_CHAR, 2);
				if (party.hasResidenceCounty())
					basicDXE =
						basicDXE +  createDataExchange(party.getResidenceCounty(), APP_RESIDENT_AREA, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 6);
				if (party.hasResidenceState())
					basicDXE =
						basicDXE + createDataExchange(party.getResidenceState(), RESIDENCE_ST, "NBA_STATES", CYBTBL_STATE_TC, CT_CHAR, 3);
			}
			
			// If Is Owner
			//begin SPR2287
			if (NbaOliConstants.OLI_REL_OWNER == relation.getRelationRoleCode()) {
				// Interest Percent (Relation) DXE: FLCOWNER
				if (relation.hasInterestPercent()){
					partyDXE =
						partyDXE + createDataExchange(INTEREST_PERCENT_OWNER, INTEREST_PERCENT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
				}
			}
			//Should be '0' for the insured/annuitant for qualified plans. Otherwise blank.
			if(NbaOliConstants.OLI_REL_INSURED == relation.getRelationRoleCode() || NbaOliConstants.OLI_REL_ANNUITANT == relation.getRelationRoleCode()){
				partyDXE =	partyDXE + createDataExchange("0", OWNER_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
			}else{
				partyDXE =	partyDXE + createDataExchange("", OWNER_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
			}
			//end SPR2287
			// Tax Identification Type (Party) DXE: FLCTAXCD
			//  UCT (??)  OLI_LU_GOVTIDTC 
			if (party.hasGovtIDTC()) { 
				partyDXE = partyDXE + createDataExchange(party.getGovtIDTC(), GOV_ID_TC, "OLI_LU_GOVTIDTC", CYBTBL_UCT, CT_CHAR, 1); //NBA093
			}
			
			else {
				if (party.hasPersonOrOrganization() && party.hasGovtID()) {
					if (party.getPersonOrOrganization().isPerson()) {
						partyDXE = partyDXE + createDataExchange(SOC_SEC_TAXID, GOV_ID_TC, "OLI_LU_GOVTIDTC", CYBTBL_UCT, CT_CHAR, 1);
					} else {
						partyDXE = partyDXE + createDataExchange(CORP_TAXID, GOV_ID_TC, "OLI_LU_GOVTIDTC", CYBTBL_UCT, CT_CHAR, 1);

					}
				}
			}
			// Tax Identification Number (Party) DXE: FLCTAXNO
			if (party.hasGovtID())
				partyDXE = partyDXE + createDataExchange(party.getGovtID(), GOV_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 10);
			//SPR2953 code deleted
			for (int i = 0; i < party.getOLifEExtensionCount(); i++) {
				olifeEx = party.getOLifEExtensionAt(i);
				if (olifeEx.isPartyExtension()) {
					partyEx = olifeEx.getPartyExtension();
					// State Withholding (None/Standard) (Party)
					//  UCT (CLULT015)  OLI_LU_WITHCALCMTH 
					
					int count = partyEx.getTaxWithholdingCount();
					for (int j = 0; j < count; j++) {
						TaxWithholding taxWithholding = partyEx.getTaxWithholdingAt(j);
						if ((taxWithholding.getTaxWithholdingPlace() == OLI_TAXPLACE_JURISDICTION) && (taxWithholding.hasTaxWithholdingType())) {
				
							partyDXE =
								partyDXE
									+ createDataExchange(
										formatCyberLong(taxWithholding.getTaxWithholdingType()),  
										ST_WITHHOLDING,
										NbaTableConstants.OLI_LU_WITHCALCMTH,
										CYBTBL_UCT,
										CT_CHAR,
										1);
						}
					}  
					
				} // client OLifeExtention not sent to host
			} //
			// VT_I4 UCT (CLULT015)  OLI_LU_WITHCALCMTH
			
			if (planType.compareTo("F") == 0) {
				Annuity annuity = holding.getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();
				Payout payout = NbaUtils.getFirstPayout(annuity);
				if (payout != null && payout.hasTaxWithheldInd()) {
					// SPR1794
					if (payout.getTaxWithheldInd()) {
						TaxWithholding taxWithHolding = NbaUtils.getFirstTaxWithholding(payout);
						if (taxWithHolding != null) {
							partyDXE =
								partyDXE
									+ createDataExchange(
										formatCyberLong(taxWithHolding.getTaxWithholdingType()),
										FEDERAL_WITHHOLDING_IND,
										NbaTableConstants.OLI_LU_WITHCALCMTH,
										CYBTBL_UCT,
										CT_CHAR,
										1);
						}
					}
				}
			}
			
			// Organization Info
			if (organization != null) { // Corporation Name (Party.Organization) DXE: FLCCORP
				//begin SPR2953
				if (organization.hasDBA()){
					partyDXE = partyDXE + createDataExchange(organization.getDBA(), CORPNAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 40);
				}
				if (organization.hasOrgForm()){ // Legal Entity DXE:FLCLVERB
					partyDXE =
						partyDXE
							+ createDataExchange(formatCyberLong(organization.getOrgForm()), PARTY_TYPE_CODE, NbaTableConstants.OLI_LU_ORGFORM, CYBTBL_UCT, CT_CHAR, 1);
				} 
			}else{
				//If the party is not an organization set the legal verbage indicator from the party object.
				//Party type indicator -  (Party) DXE: FLCLVERB
				// VT_I4  OLIEXT_LU_PARTYTYPE
				 if (party.hasPartyTypeCode()){
					 partyDXE =
						 partyDXE + createDataExchange(party.getPartyTypeCode(), PARTY_TYPE_CODE, NbaTableConstants.OLI_LU_PARTY, CYBTBL_UCT, CT_CHAR, 1);
				 }
			//end SPR2953
			} // End Organization Info
			// Producer Info
			if (party.hasProducer()) {
				Producer producer = party.getProducer();
				if (producer.getCarrierAppointmentCount() > 0) {
					CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
					// Servicing Agency
					partyDXE =
						partyDXE
							+ createDataExchange(carrierAppointment.getCompanyProducerID(), AGENCY_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 11);
				}
			} // End Producer Info
			for (int i = 0; i < party.getPhoneCount(); i++) {
				Phone phone = party.getPhoneAt(i);
				// Phone types = 'B' or 'F' or 'H'  (BUSPHN and FAX) - Phone Type (Party.Phone) DXE: FLC2PHNT, DXE Master(s): FLCPIDNT
				// Typecode  OLI_LU_PHONETYPE
				if (phone.getPhoneTypeCode() != OLI_PHONETYPE_HOME) {
					partyDXE = partyDXE 
						+ createDataExchange(phone.getPhoneTypeCode(), PHONE_ID, NbaTableConstants.OLI_LU_PHONETYPE,CYBTBL_UCT,CT_CHAR, 12);
				} 
				String fullphone = "";
				if (phone.hasDialNumber()) {
					if (phone.hasAreaCode())
						fullphone = phone.getAreaCode();
					fullphone = fullphone + phone.getDialNumber();
					// Set concatenation of AreaCode and DialNumber. 
					// To FLCPHONE or FLC2PHN depending on the value of PhoneTypeCode(FLC2PHNT)
					// Dial Number (Party.Phone) DXE: FLCPHONE
					if (phone.getPhoneTypeCode() == OLI_PHONETYPE_HOME) {
						partyDXE = partyDXE + createDataExchange(fullphone, PHONE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
					} else {
							partyDXE = 
								partyDXE 
									+ createDataExchange(fullphone, PHONE2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
					} 
				}
			} //Build email address DXE
			for (int i = 0; i < party.getEMailAddressCount(); i++) {
				EMailAddress email = party.getEMailAddressAt(i);
				if (email.hasAddrLine()) {
					if (email.hasEMailType()) {
						partyDXE =
							partyDXE
								+ createDataExchange(
									formatCyberLong(email.getEMailType()),
									EMAIL_TYPE,
									NbaTableConstants.OLI_LU_EMAILTYPE,
									CYBTBL_UCT,
									CT_CHAR,
									3);
					} else {
						//if type is missing set default to Personal type
						partyDXE = partyDXE + createDataExchange(PERSONAL_EMAIL_TYPE, EMAIL_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
					}

					partyDXE = partyDXE + createDataExchange(sPersonId, EMAIL_PERS_SEQ, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4);
					partyDXE = partyDXE + createDataExchange("E", ELECT_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
					partyDXE = partyDXE + createDataExchange(email.getAddrLine(), EMAIL_ADDRESS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 137);
				}
			} //
			// Build address DXE
			//
			addressDXE = addressDXE + createAddressFromParty(party, sPersonId);
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_PARTIES, e);
		} 
		
		return partyDXE;
	}

	/** 
	 * Create the DXE for notes 
	 * @param holding The Holding XML object
	 * @return java.lang.String The Notes DXE information
	 * @exception com.csc.fsg.nba.exception.NbaBaseException 
	 */
	protected String createNotes(Holding holding) throws com.csc.fsg.nba.exception.NbaBaseException {
		Attachment attachment = null;;
	
		String notesDXE = "";
		// SPR3290 code deleted
	
		
	
		int attachmentTot = holding.getAttachmentCount();
		if (attachmentTot == 0)
			return "";
			
   
	
		for (int i = 0; i < attachmentTot; i++) {
			attachment = holding.getAttachmentAt(i); 
			int a = (int) attachment.getAttachmentType();	
			//UNDERWRITER_NOTE = 12 
			if (a == 12) {
				notesDXE = notesDXE + createDataExchange("UND", "FNPMPCDE", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
				//need to hardcode 2 spaces for sub purpose, createDataExchange strings the leading spaces				
				notesDXE = notesDXE + "FNPSPCDE=  ;";	
				notesDXE = notesDXE + createDataExchange("P", "FNPSOURC", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
				notesDXE = notesDXE + createDataExchange("1", "FNPRICDE", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
				if (attachment.hasUserCode())
					//DXE: FNPNDEPT
					notesDXE = notesDXE + createDataExchange(attachment.getUserCode(), INF_NOTE_PAD_DEPT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
				if (attachment.hasDateCreated())
					//DXE: FNPNODAT
					notesDXE = notesDXE + createDataExchange(attachment.getDateCreated(), INF_NOTE_PAD_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
				//host change, we are not sending up
				//notesDXE = notesDXE + createDataExchange("~", "FNPTIME", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
				if (attachment.hasAttachmentData()){
					AttachmentData attachmentData = attachment.getAttachmentData();
					//DXE: FNPVNOTE
					notesDXE = notesDXE + createDataExchange(attachmentData.getPCDATA(), INF_NOTE_PAD_DATA, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 230); //SPR2760
				}
	
			} // end if
		} // end for
	
	    
	
		return notesDXE;
	}
	/**
	 * Create endorsement (82) segment information 
	 * 
	 * @return The DXE stream for the the endorsement information
	 * @param policy com.csc.fsg.nba.vo.txlife.Policy
	 */
	protected String createEndorsementInfo(OLifE olife) { 
		//new method SPR1696
		
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); 
		Policy policy = holding.getPolicy();
		Endorsement endorsement = null;
		String DXE = "";

		for (int i=0; i<policy.getEndorsementCount(); i++){
			endorsement = policy.getEndorsementAt(i);
			/*
			 * if the endorsement is for the entire policy (related Obj type of 18) then send 0000 in person id
			 * if the endorsement applies to the coverage, set person id and phase code
			 * DXE: FRDPIDNT (master), FRDPHASE (master)
			 */
			if (endorsement.getRelatedObjectType() == 18){
				DXE = DXE + createDataExchange("0000", INF_ENDORSE_PERS_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 4);
			}else{
				Object[] relations = olife.getRelation().toArray();
				Relation relation = NbaUtils.getRelationForParty(endorsement.getAppliesToPartyID(), relations);
				String relRoleCode =
					getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE, compCode, DEFAULT_COVID);
				String sPartyId = getPartyId(endorsement.getAppliesToPartyID(), relRoleCode);
				DXE = DXE + createDataExchange(sPartyId, INF_ENDORSE_PERS_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 4);
				
				DXE = 
					DXE 
						+ createDataExchange(
							getPhaseCode(policy,endorsement.getRelatedObjectID(),
							endorsement.getRelatedObjectType()), 
							INF_ENDORSE_PHASE, 
							CYBTRANS_NONE, 
							CYBTBL_NONE, 
							CT_DEFAULT, 
							2);
				
			}
			/*
			 * Benefit Type DXE: FRDBTYPE (master)	Default is '*'
			 * Benefit subtype DXE: FRDBSUBT (master)	Default is '*'
			 * This code indicates the specific type of endorsement attached to the policy. DXE: FRDENDCD (master)
			 * FRDENDCD must be the last master keyword listed and must have a value in it. 
			 * The expiry date of a temporary endorsement. DXE: FRDERDAT
			 * Indicates that additional information about the restrictive endorsement is contained in the policy file. DXE: FRDCAUTN
			 */
			DXE = DXE + createDataExchange("*", INF_ENDORSE_BEN_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 1);
			DXE = DXE + createDataExchange("*", INF_ENDORSE_BEN_SUBTYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 1); 
			DXE = DXE + createDataExchange(endorsement.getEndorsementCode(), INF_ENDORSE_TYPE, "NBA_AmendEndorseType", CYBTBL_UCT, CT_DEFAULT, 0);
			DXE = DXE + createDataExchange(endorsement.getEndDate(), INF_ENDORSE_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 8);
			DXE = DXE + createDataExchange(endorsement.getEndorsementInfo(), INF_ENDORSE_DETAIL_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			
			//Use code DXE:FRDBUSE
			DXE = DXE + createDataExchange("0", INF_ENDORSE_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 1);
			
		
		}
		
		return DXE;
	}

	
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() { 
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCyberInforceAppAdapter.class.getName()); 
			} catch (Exception e) {
				NbaBootLogger.log("NbaCyberNewAppAdapter could not get a logger from the factory."); 
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * Fetch a row from the NBA_MORTALITY table
	 * 
	 * @param compCode The company code
	 * @param gMortality The generic mortality
	 * @param ageRule Age rule key
	 * @param gender Gender key
	 * @param rateClass rate class key
	 * @return com.csc.fsg.nba.tableaccess.NbaTableData[]
	 */
	// SPR1633 New method
	protected NbaTableData[] getMortalityTable(String compCode, String gMortality, String ageRule, String gender, String rateClass) {

		// SPR3290 code deleted
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); 
		aCase.put("GENERIC_MORTALITY", gMortality); 
		aCase.put("AGE_RULE", ageRule); 
		aCase.put("GENDER", gender); 
		aCase.put("RATE_CLASS", rateClass); 
		if(getLogger().isDebugEnabled()) { //SPR3290	
		    getLogger().logDebug("Loading NBA_MORTALITY");
		}//SPR3290

		NbaTableData[] tArray = null;

		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_MORTALITY");
		} catch (NbaDataAccessException e) {
			getLogger().logWarn("NbaDataAccessException Loading NBA_MORTALITY");
		}
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("Loaded NBA_MORTALITY");
		}

		return (tArray);
	}

	/**
	 * Create a table of plans for base coverages and riders
	 * 
	 * @param compCd The company code
	 * @return com.csc.fsg.nba.tableaccess.NbaTableData[]
	 */
	protected NbaTableData[] getPlansRidersTable(String compCode) {

		Date currentDate = new Date();
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("appState", "*");
		aCase.put("appDate", currentDate);
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); 
		//NBA044 code deleted
		if(getLogger().isDebugEnabled()) { //SPR3290
		    getLogger().logDebug("Loading NBA_PLANS_RIDERS");
		} //SPR3290

		NbaTableData[] tArray = null;

		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_PLANS_RIDERS");
		} catch (NbaDataAccessException e) {
			getLogger().logWarn("NbaDataAccessException Loading NBA_PLANS_RIDERS");
		}
		if(getLogger().isDebugEnabled()) { //SPR3290		
		    getLogger().logDebug("Loaded NBA_PLANS_RIDERS");
		} //SPR3290

		return (tArray);
	}

	/**
	 * Obtain the rule value for the feeType and tableType combination.  
	 * If TableRef.ChargeMethodTC = 1000500009, the rule is is an amount per 
	 * thousand and is stored in TableRef.RuleKeyDef. Otherwise the rule is
	 * in TableRef.ChargeMethodTC.
	 * @param feeExtension - FeeExtension
	 * @param feeType - 8 = Expense, 22 = Premium Load, 16 = Full Surrender
	 * @param tableType - 59 = first rule, 60 = second rule, 61 = third rule
	 */
	// SPR2115 New Method
	protected String getRule(FeeExtension feeExtension, long feeType, long tableType) {
		String rule = "";
		FeeTableRef feeTableRef = null;
		if (feeExtension != null) {
			int count = feeExtension.getFeeTableRefCount();
			for (int i = 0; i < count; i++) {
				FeeTableRef tempFeeTableRef = feeExtension.getFeeTableRefAt(i);
				if (tempFeeTableRef.getTableType() == tableType) {
					feeTableRef = tempFeeTableRef;
					if (feeType == OLI_FEE_EXPENSE) { //Expense rules are weird
						if (feeTableRef.getChargeMethodTC() == OLIX_CHARGERULECALCMETHOD_DEDUCTAMTK) {
							if (feeTableRef.hasRuleKeyDef()) {
								rule = feeTableRef.getRuleKeyDef(); //rule is stored in RuleKeyDef 
							}
						}else {//begin NBA104
							long code = feeTableRef.getChargeMethodTC();
							rule = getCyberValue(formatCyberLong(code), NbaTableConstants.OLIEXT_LU_CHARGERULECALCMETHOD, CYBTBL_UCT, compCode, DEFAULT_COVID, "");
						}//NBA104

					} else {
						long code = feeTableRef.getChargeMethodTC();
						rule = getCyberValue(formatCyberLong(code), NbaTableConstants.OLIEXT_LU_CHARGERULECALCMETHOD, CYBTBL_UCT, compCode, DEFAULT_COVID, "");
					}
					break;
				}
			}
		}
		return rule;
	}
	/**
	 * Main method for testing the inforce submission adapter
	 * @param args java.lang.String[]
	 * @exception throws NbaBaseException
	 */
	public static void main(String[] args) throws NbaBaseException, IOException {
	
		
		//FileReader inputFile = new FileReader("c:\\nba\\dxe\\end.xml");
		//FileReader inputFile = new FileReader("c:\\nba\\dxe\\holding.xml");
		//FileReader inputFile = new FileReader("d:\\Nba Docs\\XML0402\\Test XML\\XML App 9 Variable Annuity.xml");
		String filename = "c:\\Nbadoc\\PL045.xml";
		FileWriter outputFile = new FileWriter("c:\\Nbadoc\\dxe\\generatedDXE.txt");
		// SPR3290 code deleted
		TXLife txLife = null;
		
		
		String xmlResponse = " ";
		// SPR3290 code deleted
		//NbaBackEndAdapterFacade messAdapt = new NbaBackEndAdapterFacade();
		//NbaCyberAdapter messAdapt = new NbaCyberAdapter();
		try {
			
				NbaCyberInforceAppAdapter testAdapt = new NbaCyberInforceAppAdapter();
				// SPR3290 code deleted
				txLife = TXLife.unmarshal( new FileInputStream(filename) );
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("About to call adapter");
				} //SPR3290
				xmlResponse = testAdapt.createIS00Request(txLife);	//NBA104
				if(getLogger().isDebugEnabled()) { //SPR3290
					getLogger().logDebug(xmlResponse); 
				} //SPR3290
				outputFile.write(xmlResponse);
				outputFile.close();
			
		} catch (Exception e) {
			//
			// Check for any validation errors.
			//
			if (txLife != null) {
				java.util.Vector v = txLife.getValidationErrors();
				if (v != null) {
					for (int ndx = 0; ndx < v.size(); ndx++) {
						XmlValidationError error = (XmlValidationError) v.get(ndx);
						System.out.print("\tError(" + ndx + "): ");
						if (error != null)
							if (getLogger().isErrorEnabled()) {
								getLogger().logError(error.getErrorMessage());
							}
						else
							getLogger().logError("A problem occurred retrieving the validation error."); //NBA044
					}
				}
			}
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
	}
	/**
	 * Routine to make sure plan is a valid plan
	 * 
	 * @param planRider The plan or rider key
	 * @return boolean
	 */
	protected boolean validPlanRider(String planRider) {
		if (plansRidersData == null)
			plansRidersData = ((NbaPlansRidersData[]) getPlansRidersTable(compCode));
		if (plansRidersData == null) {
			getLogger().logWarn("Error: Could not load NBA_PLANS_RIDERS");
			return false;
		}
		int iTot = plansRidersData.length;
		for (int i = 0; i < iTot; i++) {
			if (plansRidersData[i].getPlanRiderKey().compareTo(planRider) == 0) {
				if (getLogger().isDebugEnabled())
					getLogger().logDebug(planRider + " is a valid plan.");
				return true;
			}
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug(planRider + " is not a valid plan.");
		return false;
	}

	/**
	 * Check to see if a contract has a rating
	 * @param lifeParticipant The LifeParticipant XML object
	 * @return the substandardRating type
	 */
	protected boolean hasRating(LifeParticipant lifeParticipant) {
		
		if (lifeParticipant.hasPermFlatExtraAmt()
		  || lifeParticipant.hasTempTableRating()	
		  || lifeParticipant.hasTempTableRatingEndDate ()
		  ||lifeParticipant.hasTempFlatExtraAmt()		
		  ||lifeParticipant.hasTempFlatEndDate())
			return true;
		else
			return false;
	
	}
	/**
	 * Find if the billing type is standard
	 * @param policy The Policy XML object
	 * @return billing mode
	 */

	protected int getBillingType(Policy policy) {
	
		switch ((int) policy.getPaymentMode()) {
			case 1:
			case 2:
			case 3:
			case 4:
				return(0);
			case 6:
			case 7:
			case 10:
			case 12:
			case 13:
				return(1);
			default:
				return(1);
		} 
		

	}
	/**
	 * Find the subAccount product code
	 * @param investment The current Investment XML object
	 * @param subAcctID The subAccount id
	 * @return java.lang.String The product code for the subaccount
	 */

	protected String getSubAcctID(Investment investment, String subAcctID) {
		
		SubAccount subAccount = null;
		
		for (int i=0; i < investment.getSubAccountCount(); i++){
			subAccount = investment.getSubAccountAt(i);
			if (subAccount.getId().equals(subAcctID)){
				return subAccount.getProductCode();
			}
		}
		
		return "";

	}
	// NBA077 code deleted
	/**
	 * get the sex of the person that the coverage is related too
	 * @param id The ID of the person that coverage is relation too
	 * @param olife The OLifE XML object
	 * @return gender of the party 
	 */
	protected long getSex(String id, OLifE olife) {
		
		Party party = null;
		int pTot = olife.getPartyCount();
		// for each of our party objects
		for (int i = 0; i < pTot; i++) {	
			party = olife.getPartyAt(i);
			//String partyId = party.getId();
			if (id.equals(party.getId())){
				if (party.hasPersonOrOrganization()) {
					PersonOrOrganization perOrOrg = party.getPersonOrOrganization();
					if (perOrOrg.isPerson()) {
						Person person = perOrOrg.getPerson();
						return person.getGender();
					} 
				}
			}
		}
		return -1;		
	
	}

	/**
	 * get the age of the person that the coverage is related too
	 * @param id The ID of the person that coverage is relation too
	 * @param olife The OLifE XML object
	 * @return Age of the person 
	 */
	protected long getAge(String id, OLifE olife) {
		
		Party party = null;
		int pTot = olife.getPartyCount();
		// for each of our party objects
		for (int i = 0; i < pTot; i++) {	
			party = olife.getPartyAt(i);
			//String partyId = party.getId();
			if (id.equals(party.getId())){
				if (party.hasPersonOrOrganization()) {
					PersonOrOrganization perOrOrg = party.getPersonOrOrganization();
					if (perOrOrg.isPerson()) {
						Person person = perOrOrg.getPerson();
						return person.getAge();
					} 
				}
			}
		}
		return -1;		
	
	}

	/**
	 * get the gender of the person that the coverage is related too
	 * @param id The ID of the person that coverage is relation too
	 * @param olife The OLifE XML object
	 * @return Gender of the person 
	 */
	//SPR1633 New method
	protected long getParticipantGender(String id, OLifE olife) {
		Party party = null;
		int pTot = olife.getPartyCount();
		// for each of our party objects
		for (int i = 0; i < pTot; i++) {	
			party = olife.getPartyAt(i);
			//String partyId = party.getId();
			if (id.equals(party.getId())){
				if (party.hasPersonOrOrganization()) {
					PersonOrOrganization perOrOrg = party.getPersonOrOrganization();
					if (perOrOrg.isPerson()) {
						Person person = perOrOrg.getPerson();
						if (person.hasGender())
							return person.getGender();
					} 
				}
			}
		}
		return -1;		
	}

	/**
	 * Return the requested Phase code
	 * @param policy The Policy XML object
	 * @param id the XML object id for coverage, covOption or annuity rider
	 * @param type The XML object type - coverage (20), covOption(21), or rider(82)
	 * @return The phase code
	 */

	protected String getPhaseCode(Policy policy, String id, long type) {
		//new method SPR1696
		
		Life life = null;
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null;
		Coverage coverage = null;
		CovOption covOption = null;
		Annuity annuity = null;
		Rider rider = null;
		
		if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) { 
			lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); 
			if (lifeOrAnnuityOrDisabilityHealth.isLife()){
				life = lifeOrAnnuityOrDisabilityHealth.getLife();
			}else if (lifeOrAnnuityOrDisabilityHealth.isAnnuity()){
				annuity = lifeOrAnnuityOrDisabilityHealth.getAnnuity();
			}else{
				if (getLogger().isWarnEnabled()){
					getLogger().logWarn("Error Policy XML has no Life information");
				}
				return "";
			}
		}else{
			if (getLogger().isInfoEnabled()){
				getLogger().logInfo("Error Policy XML has no life, annuity, or disability health information");
			}
			return "";
		}
			
		switch ((int)type) {
			case (int)OLI_LIFECOVERAGE: //20
				if (life == null){
					break;
				}
				for (int i=0; i < life.getCoverageCount(); i++){
					coverage = life.getCoverageAt(i);
					if (coverage.getId().equals(id)){
						return coverage.getCoverageKey();
					}
				}
				break;
			case (int) OLI_COVOPTION: //21
				if (life == null){
					break;
				}
				for (int i=0; i < life.getCoverageCount(); i++){
					coverage = life.getCoverageAt(i);
					for (int j = 0; j < coverage.getCovOptionCount(); j++) {
						covOption = coverage.getCovOptionAt(j);
						if (covOption.getId().equals(id)){
							return covOption.getCovOptionKey();
						}
					}
				}
				break;
			case (int)OLI_ANNRIDER: //86
				if (annuity == null){
					break;
				}
				for (int i=0; i < annuity.getRiderCount(); i++){
					rider = annuity.getRiderAt(i);
					if (rider.getId().equals(id)){
						return rider.getRiderKey();
					}
				}				
				break;
		}
		
		return "";
	}	

	/**
	 * Return true if this is a fixed premium product. 
	 * @param policyProduct
	 * @return
	 */
	// NBA104 New Method
	protected boolean isFixedPrem(PolicyProduct policyProduct) {
		LifeProductOrAnnuityProduct lifeOrAnn = policyProduct.getLifeProductOrAnnuityProduct();
		if (lifeOrAnn != null && lifeOrAnn.hasContents()) {
			if (lifeOrAnn.isLifeProduct()) {
				LifeProductExtension lifeExt = AccelProduct.getFirstLifeProductExtension(lifeOrAnn.getLifeProduct()); //NBA237
				if (lifeExt != null) {
					return lifeExt.getPremType() == OLI_ANNPREM_FIXED;
				}
			} else {  //AnnuityProduct
				AnnuityProduct product = lifeOrAnn.getAnnuityProduct();
				if (product != null) {
					return product.getPremType() == OLI_ANNPREM_FIXED;
				}
			}
		}
		
		return false;
	}
	/**
	 * Answer the plan type
	 */
	// NBA104 New Method
	protected String getPlanType() {
		return planType;
	}

	/**
	 * Set the plan type
	 */
	// NBA104 New Method	
	protected void setPlanType(String string) {
		planType = string;
	}

	/**
	 * Return true if the product type is Annuity
	 */
	// NBA104 New Method	
	protected boolean isAnnuity() {
		return ANNUITY_PROD.equals(getPlanType());
	}
	/**
	 * Return true if the product type is Life
	 */
	// NBA104 New Method	
	protected boolean isLife() {
		return LIFE_PROD.equals(getPlanType());
	}

	/**
	 * @return
	 */
	// NBA104 New Method, NBA237 changed method signature	
	protected AccelProduct getNbaProduct() {
		return nbaProduct;
	}

	/**
	 * @param product
	 */
	// NBA104 New Method, NBA237 changed method signature	
	protected void setNbaProduct(AccelProduct product) {
		nbaProduct = product;
	}

	/**
	 * @return
	 */
	// NBA104 New Method
	protected Policy getPolicy() {
		return policy;
	}

	/**
	 * @param policy
	 */
	// NBA104 New Method
	protected void setPolicy(Policy policy) {
		this.policy = policy;
	}

	/**
	 * @return
	 */
	// NBA104 New Method	
	protected Holding getHolding() {
		return holding;
	}

	/**
	 * @param holding
	 */
	// NBA104 New Method
	protected void setHolding(Holding holding) {
		this.holding = holding;
	}

	/**
	 * @return
	 */
	// NBA104 New Method	
	protected NbaTXLife getNbaTXLife() {
		return nbaTXLife;
	}

	/**
	 * @param life
	 */
	// NBA104 New Method	
	protected void setNbaTXLife(NbaTXLife life) {
		nbaTXLife = life;
	}
	/**
	 * Create a Payment, Withdrawal, or Charge Allocation (57) segment from Investment.SubAccount objects
	 * based on the allocation type requested. If there are no SubAccounts for which SubAccount.SystematicActivityType
	 * matches the allocation type requested, a segment is not created for that type. 
	 * @param investment
	 * @param allocationType
	 */
	// SPR1929 New Method
	protected StringBuffer createAllocations(Investment investment,int count, long allocationType) {
		long sysActivityType; 
		StringBuffer buff = new StringBuffer();
		SubAccount subaccount;
		for (int i = 0; i < count; i++) {
			subaccount = investment.getSubAccountAt(i);
			if (subaccount.hasSystematicActivityType()) {
				sysActivityType = subaccount.getSystematicActivityType();
			} else {
				sysActivityType = OLI_SYSACTTYPE_DEPT; //default is payment allocation
			}
			if (sysActivityType == allocationType && subaccount.hasProductCode()) { //Process only the requested type 
				if (buff.length() == 0) { //Allocation type is sent once per Allocation and precedes the funds and their values
					buff.append(createDataExchange(sysActivityType, INF_FUND_SYSACT_TYPE, NbaTableConstants.OLI_LU_SYSTEMATIC, CYBTBL_UCT, CT_CHAR, 1));
				}
				buff.append(createDataExchange(subaccount.getProductCode(), FUND_ID, CYBTRANS_FUNDS, CYBTBL_FUNDS, CT_CHAR, 3, productCode));
				if (subaccount.hasAllocPercent()) {
					buff.append(formatDataExchange(PERCENTAGE, FUND_ALLOC_TYPE, CT_CHAR, 12));
					buff.append(createDataExchange(subaccount.getAllocPercent(), FUND_ALLOC_PCT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));
				} else if (subaccount.hasCurrNumberUnits()) {
					buff.append(formatDataExchange(UNITS, FUND_ALLOC_TYPE, CT_CHAR, 12));
					buff.append(createDataExchange(subaccount.getCurrNumberUnits(), FUND_ALLOC_UNITS, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));
				} else if (subaccount.hasTotValue()) {
					buff.append(formatDataExchange(DOLLARS, FUND_ALLOC_TYPE, CT_CHAR, 12));
					buff.append(createDataExchange(subaccount.getTotValue(), FUND_ALLOC_VAL, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));
				} else if (subaccount.hasAllocationAmt()) {
					buff.append(formatDataExchange(DOLLARS, FUND_ALLOC_TYPE, CT_CHAR, 12));
					buff.append(createDataExchange(subaccount.getAllocationAmt(), FUND_ALLOC_VAL, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));
				}
			}
		}
		return buff;
	}

	/**
	 * Returns DXE for special variable for policy location so backend will not create pending record for calc and print.
	 * @return DEX for the policy location special variable 
	 */
	//NBA133 New Method
	protected String getPolicyLocation(){
	    String polLoc = "";
	    long transSubTyep = getNbaTXLife().getTransSubType();
	    if(transSubTyep == NbaOliConstants.TC_SUBTYPE_BACKEND_CALCULATIONS){
	        polLoc = "AEPPOLOC=Z;";
	    }else if(transSubTyep == NbaOliConstants.TC_SUBTYPE_BACKEND_PRINT){
	        polLoc = "AEPPOLOC=Y;";
	    }
	    return polLoc;
	}

	   /**
	 * Parses DXE response from host and creates XML Transaction
	 * @param nbATxLife XML document Contains the transaction request for the host
	 * @param hostResponse host response DXE
	 * @return nbaTxlifeResponse XML reponse from the host
	 * @exception throws NbaBaseException and java.rmi.RemoteException
	 */
	// NBA195 new method
	public Object parseHostResponse(Object obj, String hostResponse) {
		NbaTXLife nbaTxlifeResponse = new NbaTXLife();
		try {
			if (hostResponse.substring(0, 2).compareTo(String.valueOf(NbaOliConstants.OLI_TC_NULL)) == 0) {
				throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_HOST_UNAVAILABLE + "  " + hostResponse, NbaExceptionType.FATAL);
			}
			TXLife txLife = null;
			if (obj instanceof NbaTXLife) {
				txLife = ((NbaTXLife) obj).getTXLife();
			} else {
				//if our object is not a NbaTxLife, then we want to just return the host DXE string
				return hostResponse;
			}
			//create the XML response
			NbaCyberInforceParser iParser = new NbaCyberInforceParser();
			iParser.setTransType(getTransType(txLife));
			iParser.setTransSubType(getTransSubType(txLife));
			iParser.setChangeSubType(getChangeSubType(txLife));
			iParser.setHostResponse(hostResponse);
			nbaTxlifeResponse.setTXLife(iParser.createXmlResponse(txLife));
		} catch (NbaBaseException e) {
			throw new RuntimeException(e);
		}
		LogHandler.Factory.LogResourceAdapter("CYBERDXE Response XML", nbaTxlifeResponse.toXmlString());
		return nbaTxlifeResponse;
	}

	/**
	 * Prepares DXE request for CyberLife from Accord XML transaction passed as request
	 * @param request the NnaTXLife request 
	 * @return DXE Request for CyberLife
	 * @throws NbaBaseException if no response from host
	 * @see com.csc.fsg.nba.backendadapter.NbaBackEndAdapter#prepareRequestToHost(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	//NBA195 new method
	public String prepareRequestToHost(Object obj) {
		String request = null;
		try {
			NbaTXLife txLifeRequest = null;
			//Do conversion of xmlDoc to text for DXE
			if (obj instanceof NbaTXLife) {
				txLifeRequest = (NbaTXLife) obj;
			} else {
				//we are going to assume we were passed a DXE stream and can just return it.
				return (String) obj;
			}
			if (LogHandler.Factory.isLogging(LogHandler.LOG_RESOURCE_ADAPTER)) {
				LogHandler.Factory.LogResourceAdapter("CYBERDXE Request XML", txLifeRequest.toXmlString());
			}
			TXLife txLife = txLifeRequest.getTXLife();
			int transType = getTransType(txLife);
			int transSubType = getTransSubType(txLife);
			long changeSubType = getChangeSubType(txLife);
			//set the company code
			NbaCyberInforceRequests requests = new NbaCyberInforceRequests();
			requests.compCode = txLifeRequest.getPrimaryHolding().getPolicy().getCarrierCode();

			switch (transType) {
			case HOLDING:
				if (transSubType == NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT) {
					request = requests.create203RequestForReinstatement(txLife);
				} else {
					request = requests.create203Request(txLife);
				}
				break;
			case NEW_APP:
				if (transSubType == NbaOliConstants.TC_SUBTYPE_NEWBUSSUBMISSION) {
					if (changeSubType == NbaOliConstants.NBA_CHNGTYPE_INCREASE) {
						request = requests.createIncreaseRequest(txLife);
					} else {
						NbaCyberInforceAppAdapter newApp = new NbaCyberInforceAppAdapter();
						request = newApp.createIS00Request(txLife);
					}
				} else
					throw new NbaBaseException("Invalid Transaction SubType Requested For TransType 103");
				break;
			case CWA:
				String baseProd = getProductCode(requests.compCode, txLifeRequest.getPrimaryHolding().getPolicy().getPolNumber());
				request = requests.create508Request(txLife, baseProd);
				break;
			case (int) NbaOliConstants.TC_TYPE_PRODUCER:
				if (transSubType == NbaOliConstants.TC_TYPE_PRODUCERSUBTYPE_AGENTRETRIEVE) {
					request = requests.createAgentInfoRequest(txLife);
				} else if (transSubType == NbaOliConstants.TC_TYPE_PRODUCERSUBTYPE_AGENTVALIDATION) {
					request = requests.createAgentValidationRequest(txLife);
				} else {
					throw new NbaBaseException("Invalid Transaction SubType Requested For TransType " + NbaOliConstants.TC_TYPE_PRODUCER);
				}
				break;
			case (int) NbaOliConstants.TC_TYPE_PARTYINQ:
				if (transSubType == NbaOliConstants.TC_TYPE_UWRSKSUBTYPE) {
					request = requests.createPartyInqRequest(txLife);
				} else {
					throw new NbaBaseException("Invalid Transaction SubType Requested For TransType " + NbaOliConstants.TC_TYPE_PARTYINQ);
				}
				break;
			default:
				throw new NbaBaseException("Invalid Transaction Type Requested");
			}
		} catch (NbaBaseException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e));
		}
		return request;
	}

	/**
	 * Get the transaction type coming in for current request
	 * @param txLife Current txLife transaction request
	 * @return transType
	 * @exception throws NbaBaseException
	 */
	//NBA195 new method
	protected int getTransType(TXLife txLife) throws NbaBaseException {
		UserAuthRequestAndTXLifeRequest request = txLife.getUserAuthRequestAndTXLifeRequest();
		if (request == null) {
			throw new NbaBaseException("ERROR: Could not locate a UserAuthRequestAndTXLifeRequest object");
		}
		TXLifeRequest txlife = request.getTXLifeRequestAt(0);
		if (txlife.hasTransType()) {
			// Cast to int for use with NbaCyberInforceParser
			return ((int) txlife.getTransType());
		} else {
			return -1;
		}
	}

	/**
	 * Get the transaction type coming in for current request
	 * @param txLife Current txLife transaction request
	 * @return transSubType
	 * @exception throws NbaBaseException
	 */
	//NBA195 new method
	protected int getTransSubType(TXLife txLife) throws NbaBaseException {
		UserAuthRequestAndTXLifeRequest request = txLife.getUserAuthRequestAndTXLifeRequest();
		if (request == null) {
			throw new NbaBaseException("ERROR: Could not locate a UserAuthRequestAndTXLifeRequest object");
		}
		TXLifeRequest txlife = request.getTXLifeRequestAt(0);
		if (txlife.hasTransSubType()) {
			// Cast to int for use with NbaCyberInforceParser
			return ((int) txlife.getTransSubType());
		} else {
			return -1;
		}
	}

	/**
	 * Get the transaction type coming in for current request
	 * @param txLife Current txLife transaction request
	 * @return transType
	 * @exception throws NbaBaseException
	 */
	//NBA195 new method
	protected long getChangeSubType(TXLife txLife) throws NbaBaseException {
		UserAuthRequestAndTXLifeRequest request = txLife.getUserAuthRequestAndTXLifeRequest();
		if (request == null) {
			throw new NbaBaseException("ERROR: Could not locate a UserAuthRequestAndTXLifeRequest object");
		}
		TXLifeRequest txlife = request.getTXLifeRequestAt(0);
		if (txlife.getChangeSubTypeCount() > 0 && txlife.getChangeSubTypeAt(0).hasChangeTC()) {
			return (txlife.getChangeSubTypeAt(0).getChangeTC());
		} else {
			return -1;
		}
	}

	/**
	 * Message the administrative system to get the product code for the Base Coverage
	 * @param compCode - company code
	 * @param polNumber - policy number
	 * @return the product code for the Base Coverage
	 * @throws NbaBaseException is the administrative system is not available
	 */
	//NBA195 new method
	protected String getProductCode(String compCode, String polNumber) throws NbaBaseException {
		StringBuffer buff = new StringBuffer();
		buff.append(NbaCyberConstants.HOLDING_TYPE);
		buff.append(NbaCyberConstants.COMP_CODE);
		buff.append("=");
		buff.append(compCode);
		buff.append(";");
		buff.append(NbaCyberConstants.POL_NUM);
		buff.append("=");
		buff.append(polNumber);
		buff.append(";");
		buff.append(RESOLVE);
		buff.append("=");
		buff.append(NbaCyberConstants.PROD_TYPE);
		buff.append(";");

		//Send Disp 
		SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
		List list = new ArrayList(1);
		list.add(buff.toString());
		Result result = sysAccess.invoke("CyberLifeDXE/CyberInforceAdapter", list);
		String response = null;
		if (!result.hasErrors()) {
			response = (String) result.getData().get(0);
		} else {
			throw new NbaBaseException("Error invoking CyberDXE");
		}
		if (response.substring(0, 2).compareTo(String.valueOf(NbaOliConstants.OLI_TC_NULL)) == 0) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_HOST_UNAVAILABLE + "  " + response);
		}
		return response.substring(25, 26);
	}
	
	/**
	 * Pull data for the 1st rating from given life participant and create DXE string. 
	 * @param lifeParticipant the life participant to be processed.
	 * @param termDate term date for coverage
	 * @param personCode person code for current participant
	 * @param phase coverage phase code
	 * @return the DXE string for rating
	 */
	//SPR1738 New Method
	protected String createRatingForLifeParticipant(LifeParticipant lifeParticipant, Date termDate, String personCode, String phase) {
		StringBuffer ratingDXE = new StringBuffer();
		LifeParticipantExtension lifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
		String ratingType = getRatingType(lifeParticipant);
		if (ratingType != null) {

			ratingDXE.append(createDataExchange(personCode, POLICY_PERS_SEQ, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			ratingDXE.append(createDataExchange(phase, SUB_STAND_COV_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			
			ratingDXE.append(createDataExchange(ratingType, SUB_STAND_RATE_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
			char ratingChar = ratingType.charAt(0);
			switch (ratingChar) {
			case SUB_STAND_TYPE_PERM_TABLE:
				ratingDXE.append(createDataExchange(lifeParticipant.getPermTableRating(), SUB_STAND_TABLE_RATE, NbaTableConstants.OLI_LU_RATINGS,
						CYBTBL_UCT, CT_CHAR, 2));
				if (lifeParticipantExtension != null) {
					ratingDXE.append(createDataExchange(lifeParticipantExtension.getPermPercentageLoading(), SUB_STAND_PERCENT, CYBTRANS_NONE,
							CYBTBL_NONE, CT_DEFAULT, 0));
				}
				ratingDXE.append(createDataExchange(formatCyberDate(termDate),SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				ratingDXE.append(createDataExchange(formatCyberDate(termDate),SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				break;
			case SUB_STAND_TYPE_PERM_FLAT:
				ratingDXE.append(createDataExchange(formatCyberDouble(lifeParticipant.getPermFlatExtraAmt()), SUB_STAND_FLAT_EXTRA, CYBTRANS_NONE,
						CYBTBL_NONE, CT_CHAR, 9));
				ratingDXE.append(createDataExchange(formatCyberDate(termDate),SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				ratingDXE.append(createDataExchange(formatCyberDate(termDate),SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				break;
			case SUB_STAND_TYPE_TEMP_TABLE:
				ratingDXE.append(createDataExchange(lifeParticipant.getTempTableRating(), SUB_STAND_TABLE_RATE, NbaTableConstants.OLI_LU_RATINGS,
						CYBTBL_UCT, CT_CHAR, 2));
				ratingDXE.append(createDataExchange(formatCyberDate(lifeParticipant.getTempTableRatingEndDate()), SUB_STAND_END_DATE, CYBTRANS_NONE,
						CYBTBL_NONE, CT_CHAR, 9));
				ratingDXE.append(createDataExchange(formatCyberDate(lifeParticipant.getTempTableRatingEndDate()), SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE,
						CYBTBL_NONE, CT_CHAR, 9));
				break;
			case SUB_STAND_TYPE_TEMP_FLAT:
				ratingDXE.append(createDataExchange(formatCyberDouble(lifeParticipant.getTempFlatExtraAmt()), SUB_STAND_FLAT_EXTRA, CYBTRANS_NONE,
						CYBTBL_NONE, CT_CHAR, 9));
				if (lifeParticipant.hasTempFlatEndDate())
					ratingDXE.append(createDataExchange(formatCyberDate(lifeParticipant.getTempFlatEndDate()), SUB_STAND_END_DATE, CYBTRANS_NONE,
							CYBTBL_NONE, CT_CHAR, 9));
				ratingDXE.append(createDataExchange(formatCyberDate(lifeParticipant.getTempFlatEndDate()), SUB_STAND_ORIG_CSE_DATE, CYBTRANS_NONE,
						CYBTBL_NONE, CT_CHAR, 9));
				break;
			}

			ratingDXE.append(createDataExchange(lifeParticipant.getRatingReason(), SUB_STAND_REASON, NbaTableConstants.NBA_RATING_REASON, CYBTBL_UCT,
					CT_CHAR, 1));
			ratingDXE.append(createDataExchange(lifeParticipant.getRatingCommissionRule(), SUB_STAND_COMM, NbaTableConstants.OLIEXT_LU_COMMISCODE,
					CYBTBL_UCT, CT_CHAR, 1));
			if (lifeParticipantExtension != null) {
				ratingDXE.append(createDataExchange(formatCyberDate(lifeParticipantExtension.getEffDate()), SUB_STAND_START_DATE, CYBTRANS_NONE,
						CYBTBL_NONE, CT_CHAR, 9));
			}
		}
		return ratingDXE.toString();
	}
	
}
