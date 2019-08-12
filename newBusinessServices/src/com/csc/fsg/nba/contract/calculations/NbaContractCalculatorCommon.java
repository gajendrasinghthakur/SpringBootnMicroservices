package com.csc.fsg.nba.contract.calculations;
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
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.csc.dip.jvpms.core.RequestSequence;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.contract.calculations.results.CalcError;
import com.csc.fsg.nba.contract.calculations.results.CalcProduct;
import com.csc.fsg.nba.contract.calculations.results.CalculationResult;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.datamanipulation.SrvNbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
import com.csc.fsg.nba.vpms.results.VpmsProp;

/**
 * NbaContractCalculatorCommon
 * <p>
 * When a class extends NbaContractCalculatorCommon, it must implement a "calculate" method
 * that will control the calculation process for a specific model. Depending on the objects
 * that need to be passed, the subclass can utilize various methods available in this class.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA072</td><td>Version 3</td><td>Contract Calculations - Initial development</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>SPR2590</td><td>Version 6</td><td>Proposed Table Substandard Ratings are not being excluded from premium calculations.</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public abstract class NbaContractCalculatorCommon implements NbaContractCalculator, NbaContractCalculationsConstants, NbaOliConstants {
	protected SrvNbaOinkDataAccess oinkData = null;  //NBA213
	protected NbaOinkRequest oinkRequest;
	protected List covOptions = null; //NBA100
	protected PolicyProduct polProd = null;
	private int attrCount = -1;  //NBA100
	private int riderCount = -1;  //NBA104
	private int benefitCount = -1;  //NBA104
	private int coverageCount = -1;  //NBA104
	protected int primOrJntExtraCount = -1;	//NBA100
	protected NbaVpmsAdaptor calcProxy = null;
	protected String calcType = null;
	protected java.lang.String xmlObject = null;
	protected NbaCalculation calcReturnValues = null;	//NBA100
	protected RequestSequence calcReqSeq = null;
	// NBA104 deleted code
	protected NbaTXLife nbaTxLife = null;
	protected NbaVpmsModelResult nbaResult = null;
	protected String modelName;	//NBA100
	protected String objectDurSuffix; 	//NBA100
	protected String objectId;	//NBA100
	protected String propertySubscript;	//NBA100
	protected List coverages;	//NBA100
	protected List riders;	//NBA100
	protected List primOrJntExtras;	//NBA100
	private RequestSequence dynReqSeq = null;  //NBA104
	private ArrayList dynReqProps = new ArrayList();  // NBA104
	private NbaProductAccessFacadeBean npa = null;  //NBA213
	public final static java.lang.String XML_TAGS[][] = { { TN_ATTRIBUTE, TV_ATTRIBUTE }, {
			TN_PROPERTY, TV_PROPERTY }, {
			TN_MODEL, TV_MODEL }, {
			TN_MODELNAME, TV_MODELNAME }, {
			TN_TABLELOCATION, TV_TABLELOCATION }, {
			TN_DATATYPE, TV_DATATYPE }, {
			TN_SOURCE, TV_SOURCE }, {
			TN_TARGET, TV_TARGET }, {
			TN_MODELRESULT, TV_MODELRESULT }, {
			TN_TRANSTABLE, TV_TRANSTABLE }, {
			TN_TRANSVALUE, TV_TRANSVALUE }, {
			TN_XMLOBJECT, TV_XMLOBJECT }, {
			TN_ATTR_NAME, TV_ATTR_NAME }, {
			TN_ATTR_VALUE, TV_ATTR_VALUE }, {
			//begin NBA100				
			TN_DEBUGINDTAG, TV_DEBUGINDTAG }, {
			TN_CACHESIZETAG, TV_CACHESIZETAG }, {
			TN_KEY1, "" }, {
			TN_KEY2, "" }, {
			TN_KEY3, "" }, {
			TN_MODEL_NAME, "" }, {
			TN_QUOTATIONMARK, "" }, {
			TN_REQCOMMENTTAG, "" }, {
			TN_REQPROVIDERTAG, "" }, {
			TN_REQTYPETAG, "" }, {
			TN_REQUIREMENTTAG, "" }, {
			TN_SPACE, "" }, {
			TN_T, "" }, {
			TN_TRANSLATIONTABLE, "" }, {
			TN_TRANSLATIONVALUE, "" }, {
			TN_XML_OBJECT, "" }, {				
			//end NBA100 								
			TN_STANDARD_ATTR, TV_STANDARD_ATTR }
	};
	/**
	 * This constructor initializes the data members from the parameters and
	 * initializes the <code>NbaOinkDataAccess</code> object with the <code>NbaTXLife</code>
	 * parameter.
	 * @param aCalcType  indicates the calculation type to be performed. This 
	 * determines the type of NbaContractCalculator that will be created.
	 * @param aNbaTxLife contains contract data that will be used to supply values
	 * for VPMS attributes
	 * @throws NbaBaseException
	 */
	public NbaContractCalculatorCommon(String aCalcType, NbaTXLife aNbaTxLife) throws NbaBaseException {
		super();
		setCalcType(aCalcType);
		setNbaTxLife(aNbaTxLife);
		oinkData = new SrvNbaOinkDataAccess(aNbaTxLife);  //NBA213
		oinkData.setPlanSource(aNbaTxLife);
		oinkData.getFormatter().setDateFormat(NbaOinkFormatter.DATE_FORMAT_MMDDYYYY);  //NBA104
		oinkData.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_SLASH);  //NBA104
		setCalcReturnValues(new NbaCalculation());	//NBA100
		getCalcReturnValues().setCalcResultCode(TC_RESCODE_FAILURE);	//NBA100
	}
	protected static com.csc.fsg.nba.foundation.NbaLogger logger = null;
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractCalculatorCommon.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractCalculatorCommon could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * The initializeAttributes method
	 */
	// NBA100 changed to protected method
	protected void initializeAttributes() throws NbaBaseException, NbaVpmsException {
		try {
			VpmsModelResult vpmsModelResult = nbaResult.getVpmsModelResult();	//NBA100
			int count = vpmsModelResult.getVpmsAttrCount();	//NBA100
			NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
			ArrayList attrList = null;
			for (int i = 0; i < count; i++) {	//NBA100
				VpmsAttr oneAttr = vpmsModelResult.getVpmsAttrAt(i);	//NBA100
				if (oneAttr.hasTransTbl()) {
					if (attrList == null) {
						attrList = new ArrayList();
					}
					aNbaOinkRequest.setVariable(oneAttr.getSource());
					calcReqSeq.addSetAttribute(ATR_TRANS_VALUE, oinkData.getStringValueFor(aNbaOinkRequest));
					calcReqSeq.addSetAttribute(ATR_TRANS_TABLE, oneAttr.getTransTbl());
					int computeIndex = calcReqSeq.addCompute(EP_GET_TRANSLATION_VALUE);
					oneAttr.setTransVal(String.valueOf(computeIndex));
					attrList.add(oneAttr);
				}
			}
			if (!(attrList == null)) {
				List resultList = calcProxy.getResults(calcReqSeq);
				for (int i = 0; i < attrList.size(); i++) {
					VpmsAttr anAttr = (VpmsAttr) attrList.get(i);
					VpmsComputeResult aResult = (VpmsComputeResult) resultList.get(Integer.parseInt(anAttr.getTransVal()));
					NbaVpmsModelResult temp = new NbaVpmsModelResult(aResult.getResult());
					anAttr.setTransVal(temp.getVpmsModelResult().getTranslationValue());
				}
			}
			// NBA104 deleted code
			// set the attributes
			vpmsModelResult = nbaResult.getVpmsModelResult();	//NBA100
			count = vpmsModelResult.getVpmsAttrCount();	//NBA100
			for (int i = 0; i < count; i++) {	//NBA100
				VpmsAttr anAttr = vpmsModelResult.getVpmsAttrAt(i);	//NBA100
				if (anAttr.hasTransTbl()) {
					getDynReqSeq().addSetAttribute(anAttr.getName(), anAttr.getTransVal());  //NBA104
				} else {
					try {
						aNbaOinkRequest.setVariable(anAttr.getSource());
						getDynReqSeq().addSetAttribute(anAttr.getName(), oinkData.getStringValueFor(aNbaOinkRequest));  //NBA104
					} catch (NbaBaseException nbe) {
						getDynReqSeq().addSetAttribute(anAttr.getName(), "");  //NBA104
					}
				}
			}
		} catch (RemoteException e) {
			throw new NbaBaseException(e);
		}
	}
	/**
	 * For Properties for which the duration values are not applicable,  
	 * add a compute for each Property for the object.  
	 * @param objectID unique identifier of object needing calculation
	 * @param subscript object sequence
	 * @return NbaCalculation contains the results of the calculation
	 */
	// NBA104 NBA100 New Method
	protected void initializeProperties(String objectID, int subscript) throws NbaBaseException, RemoteException, NbaVpmsException {
		setObjectId(objectID);
		setObjectDurSuffix("");
		setPropertySubscript(subscript);
		initializeProperties();
	}
	/**
	 * For Properties for which the duration and object sequence values are not applicable,  
	 * add a compute for each Property for the object.  
	 * @param objectID unique identifier of object needing calculation 
	 * @return NbaCalculation contains the results of the calculation
	 */
	// NBA104 NBA100 New Method
	protected void initializeProperties(String objectID) throws NbaBaseException, RemoteException, NbaVpmsException {
		setObjectId(objectID);
		setObjectDurSuffix("");
		setPropertySubscript("");
		initializeProperties();
	}
	/**
	 * Adds a compute for each Property for the object.  
	 * Assumes the setObjectID, setObjectDurSuffix, and setPropertySubscript properties have been
	 * appropriately set before calling this method. 
	 * @return NbaCalculation contains the results of the calculation
	 */
	// NBA100 changed to protected method
	protected void initializeProperties() throws NbaBaseException, RemoteException, NbaVpmsException {
		// NBA104 deleted code
		for (int i = 0; i < nbaResult.getVpmsModelResult().getVpmsPropCount(); i++) {
			VpmsProp prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
			//begin NBA104
			if (includeProperty(prop)) {
				addComputeForProperty(prop);  //NBA100
			}
			// end NBA104
		}
	}
	/** 
	 * Determine if the property is specified for the current calculation object.
	 * @param prop VPMS calculation property
	 * @return true if specified, otherwise false
	 */
	// NBA104 New Method
	protected boolean includeProperty(VpmsProp prop) {
		if (PROP_ALL.equals(prop.getDataType()) ||
			(XO_POLICY.equals(getXmlObject()) && PROP_POLICY.equals(prop.getDataType())) ||
			(XO_COVERAGE.equals(getXmlObject()) && PROP_COV.equals(prop.getDataType()))  ||
			(XO_RIDER.equals(getXmlObject()) && PROP_RDR.equals(prop.getDataType()))) {
				return true;
		}
		return false;
	}
	/**
	 * Answers the calcProxy
	 * @return NbaVpmsAdaptor 
	 */
	// NBA100 changed to protected method
	protected NbaVpmsAdaptor getCalcProxy() {
		return calcProxy;
	}
	/**
	 * The calculateResults method
	 */
	// NBA104 removed objectID parameter
	protected void calculateResults() throws NbaBaseException, NbaVpmsException {
	    NbaVpmsAdaptor dynamicProxy = null; //SPR3362
		try {
			dynamicProxy = new NbaVpmsAdaptor(nbaResult.getVpmsModelResult().getModelName()); //SPR3362
			// begin NBA104
			List resultList = translatePropertyResults(dynamicProxy.getResults(getDynReqSeq()));
			for (int j = 0; j < dynReqProps.size(); j++) {
				NbaContractCalculatorProperty nbaProperty = (NbaContractCalculatorProperty)dynReqProps.get(j);
				VpmsComputeResult propResult = (VpmsComputeResult) resultList.get(nbaProperty.getResultIndex());
				// NBA104 deleted code
				processResult(nbaProperty, propResult);
			}
			//begin NBA100
			if (getLogger().isDebugEnabled()) {
				int resultCnt = getCalcReturnValues().getCalculationResultCount();
				for (int i = 0; i < resultCnt; i++) {
					CalculationResult calculationResult = getCalcReturnValues().getCalculationResultAt(i);
					getLogger().logDebug("CalculationResult.ObjectID=\"" + calculationResult.getObjectId() + "\"");
					int prodCnt = calculationResult.getCalcProductCount();
					calculationResult.getCalcError();
					for (int j = 0; j < prodCnt; j++) {
						CalcProduct calcProduct = calculationResult.getCalcProductAt(j);
						getLogger().logDebug("     PropertyName=" + calcProduct.getType() + ", Value=" + calcProduct.getValue());
					}
				}
			}
			//end NBA100
			//SPR3362 code deleted
			// end NBA104
		} catch (RemoteException e) {
			throw new NbaBaseException(e);
		//begin SPR3362
        } finally {
            if (dynamicProxy != null) {
                try {
                    dynamicProxy.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        //end SPR3362
		}
	}
	/**
	 * The calculate method directs the processing of the calculator to obtain
	 * the values from the VPMS model.
	 * @return NbaCalculation contains the results of the calculation
	 */
	public abstract NbaCalculation calculate() throws NbaBaseException, NbaVpmsException;
	/**
	 * This method clears the calculator after use.
	 */
	// NBA104 New Method
	public void clear() throws NbaBaseException {
		try {
			if (getCalcProxy() != null) {
				// turn debug & trace off before exiting
				resetDynReqSeq();
				getDynReqSeq().addSetAttribute(ATR_DEBUG, DEBUG_OFF);
				getDynReqSeq().addSetAttribute(ATR_TRACE, TRACE_OFF);
				getCalcProxy().getResults(getDynReqSeq());

				//SPR3362 code deleted
			}
		} catch (Exception e) {
			throw new NbaBaseException(e);
		//begin SPR3362
		} finally {
		    if(getCalcProxy() != null){
		        try {
		            getCalcProxy().remove();
                } catch (Exception e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		        
		    }
		//end SPR3362
		}
	}
	/**
	 * The processResult method
	 */
	// NBA104 removed objectID and prop parameters, added nbaProperty
	protected void processResult(NbaContractCalculatorProperty nbaProperty, VpmsComputeResult propResult) throws NbaBaseException, NbaVpmsException {
		if (getCalcReturnValues() == null) {	//NBA100
			setCalcReturnValues(new NbaCalculation());	//NBA100
			getCalcReturnValues().setCalcResultCode(TC_RESCODE_FAILURE);	//NBA100
		}
		CalcError anError = null;
		CalcProduct aProduct = null;
		if (propResult.getReturnCode() == 0) {
			aProduct = new CalcProduct();
			aProduct.setType(nbaProperty.getTargetName());  //NBA104
			aProduct.setValue(propResult.getResult());
		} else {
			anError = new CalcError();
			if (propResult.getMessage() != null && propResult.getMessage().length() > 0) {
				anError.setMessage(propResult.getMessage());
			}
			if (propResult.getName() != null && propResult.getName().length() > 0) {
				anError.setPropertyName(propResult.getName());
			}
			if (propResult.getRefField() != null && propResult.getRefField().length() > 0) {
				anError.setRefField(propResult.getRefField());
			}
			//begin NBA100
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug(
					"VPMS Error ObjectID=\""
						+ nbaProperty.getObjectID()
						+ "\", PropertyName="
						+ anError.getPropertyName()
						+ ", Error="
						+ anError.getMessage());
			}
			//end NBA100 
		}

		int count = getCalcReturnValues().getCalculationResultCount();	//NBA100
		CalculationResult aResult = null;
		for (int i = 0; i < count; i++) {
			aResult = getCalcReturnValues().getCalculationResultAt(i);	//NBA100
			if (aResult.getObjectId().equalsIgnoreCase(nbaProperty.getObjectID())) {  //NBA104
				if (aProduct != null) {
					aResult.addCalcProduct(aProduct);
					aResult.setCalcResultCode(TC_RESCODE_SUCCESS);
				} else if (anError != null) {  //NBA104
					aResult.setCalcError(anError);
					aResult.setCalcResultCode(TC_RESCODE_FAILURE);
				}
				return;
			}
		}
		aResult = new CalculationResult();
		// begin NBA104
		aResult.setObjectId(nbaProperty.getObjectID());
		if (aProduct != null) {
			aResult.addCalcProduct(aProduct);
			aResult.setCalcResultCode(TC_RESCODE_SUCCESS);
		} else {
			aResult.setCalcError(anError);
			aResult.setCalcResultCode(TC_RESCODE_FAILURE);
		}
		// end NBA104
		getCalcReturnValues().addCalculationResult(aResult); //NBA100
	}
	/**
	 * This method calls the VPMS Calculations model to get specific attributes that 
	 * may be needed by the calculator for its model.  This should only include
	 * attributes that are not available via the OINK process.
	 *
	 */
	// NBA100 changed to protected method
	protected void setSpecificModelAttributes() throws NbaBaseException {
		// begin NBA104
		if (getLogger().isInfoEnabled()) {
			getLogger().logInfo("VPMS call to " + CALCULATIONS_MODEL + ", entry point is " + EP_GET_MODEL_SPECIFIC_ATTRIBUTES);
		}
		// end NBA104
		calcReqSeq.addSetAttribute(ATR_MODEL_NAME, nbaResult.getModelName());
		int modAttrInd = calcReqSeq.addCompute(EP_GET_MODEL_SPECIFIC_ATTRIBUTES);
		try {
			List resultList = calcProxy.getResults(calcReqSeq);
			VpmsComputeResult attrResult = (VpmsComputeResult) resultList.get(modAttrInd);
			if (attrResult.getReturnCode() == 0) {
				nbaResult.merge(attrResult.getResult());
				// for every item we got, set the attribute
				int count = nbaResult.getVpmsModelResult().getStandardAttrCount();
				if (count > 0) {
					//NBA104 deleted code
					for (int i = 0; i < count; i++) {
						StandardAttr stan = nbaResult.getVpmsModelResult().getStandardAttrAt(i);
						// begin NBA104
						if (stan.getAttrName() != null && stan.getAttrName().length() > 0) {	//APSL4508
						if (includeAttribute(stan)) {
							String name = createSubscriptedAttributeName(stan);
							getDynReqSeq().addSetAttribute(name, stan.getAttrValue());
							if (getLogger().isDebugEnabled()) {
								getLogger().logDebug("Resolved " + name + " = " + stan.getAttrValue());
							}
						}
						nbaResult.removeStandardAttributeFromVpmsAttr(stan.getAttrName());
						// end NBA104
					}
				}
			}
			}
		} catch (RemoteException e) {
			throw new NbaBaseException(e);
		}
	}
	/**
	 * @return
	 */
	// NBA100 changed to protected method
	protected RequestSequence getCalcReqSeq() {
		return calcReqSeq;
	}
	/**
	 * Returns the RequestSequence for the dynamic call to the calculation model.
	 * @return
	 */
	// NBA104 New Method
	protected RequestSequence getDynReqSeq() {
		if (dynReqSeq == null) {
			dynReqSeq = new RequestSequence();
		}
		return dynReqSeq;
	}
	/**
	 * Returns the RequestSequence for the dynamic call to the calculation model.
	 * @return
	 */
	// NBA104 New Method
	protected void resetDynReqSeq() {
		dynReqSeq = null;
		dynReqProps = new ArrayList();
	}
	/**
	 * Returns the number of benefits on a contract.
	 * @return
	 */
	// NBA104 New Method
	protected int getBenefitCount() {
		if (benefitCount == -1) {
			benefitCount = getCount("BENEFIT");
		}
		return benefitCount;
	}
	/**
	 * Returns the number of coverages on a contract.
	 * @return
	 */
	// NBA104 New Method
	protected int getCoverageCount() {
		if (coverageCount == -1) {
			coverageCount = getCount("COVERAGE");
		}
		return coverageCount;
	}
	/**
	 * Returns the number of riders on a contract.
	 * @return
	 */
	// NBA104 New Method
	protected int getRiderCount() {
		if (riderCount == -1) {
			riderCount = getCount("RIDER");
		}
		return riderCount;
	}
	/**
	 * This method counts the number of coverages and returns that value.
	 * @return an int indicating the count of coverages on the contract
	 */
	// NBA100 changed to protected method
	protected int getCount(String type) {
		int count = 0;
		if (nbaTxLife.isLife()) {  //NBA104
			Life life = nbaTxLife.getLife();  //NBA104
			int covs = life.getCoverageCount();
			if (type.equalsIgnoreCase("COVERAGE")) {				
				return getCoverages().size();	//NBA100
			} else if (type.equalsIgnoreCase("RIDER")) {				 
				return getRiders().size();	//NBA100
			} else if (type.equalsIgnoreCase("BENEFIT")) {
				return getCovOptions().size();	//NBA100
			} else if (type.equalsIgnoreCase("EXTRA") || type.equalsIgnoreCase("SUBSTANDARDRATINGS")) {
				for (int index = 0; index < covs; index++) {
					// begin NBA104
					Coverage coverage = life.getCoverageAt(index);
					if (!coverage.isActionDelete()) {
						int lifePartCount = coverage.getLifeParticipantCount();
						for (int inner = 0; inner < lifePartCount; inner++) {
							LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(inner);
							if (!lifeParticipant.isActionDelete()) {
								int countSR = lifeParticipant.getSubstandardRatingCount();
								for (int i = 0; i < countSR; i++) {
									if (NbaUtils.isValidRating(lifeParticipant.getSubstandardRatingAt(i))) { //SPR2590
										count++;
									}
								}
							}
						}
					}
					// end NBA104
				}
				return count;
			} else if (type.equalsIgnoreCase("PNTEXTRA")){	//NBA100
				return getPrimaryOrJointSubstandard().size();	//NBA100
			}
		//begin NBA100
		} else if (nbaTxLife.isAnnuity()) {
			if (type.equalsIgnoreCase("COVERAGE")) {
				count = 1;}
		//end NBA100
		}
		return count;
	}
	/**
	 * @return
	 */
	// NBA100 changed to protected method
	protected NbaTXLife getNbaTxLife() {
		return nbaTxLife;
	}
	/**
	 * This abstract method allows a calulator to set any specific attributes
	 * that it might need to set to performs its calculations.
	 * @return
	 */
	// NBA104 changed method signature to protected
	protected abstract void setCalculatorAttributes();
	/**
	 * @param adaptor
	 */
	// NBA100 changed to protected method
	protected void setCalcProxy(NbaVpmsAdaptor adaptor) {
		calcProxy = adaptor;
	}
	/**
	 * This method initializes the skip attributes map for the VPMS model and
	 * then calls the NbaVpmsAdaptor.setAttribute() method to initialize the
	 * OINK variables.  That method returns a <code>RequestSequence</code> object
	 * with all the attributes set.
	 */
	// NBA100 changed to protected method
	protected void setCalculationModelAttributes() throws NbaBaseException {
		try {
			HashMap deOink = new HashMap();
			deOink.put(ATR_CALLING_SYSTEM, CALLING_SYSTEM);
			deOink.put(ATR_CALC_FUNCTION_TYPE, getCalcType());
			deOink.put(ATR_XML_RESPONSE, String.valueOf(true));
			for (int i = 0; i < XML_TAGS.length; i++) {
				deOink.put(XML_TAGS[i][0], XML_TAGS[i][1]);
			}
			calcProxy.setSkipAttributesMap(deOink);
			calcReqSeq = calcProxy.setAttributes();
		} catch (RemoteException re) {
			throw new NbaBaseException(re);
		} 
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (2/6/2002 5:03:59 PM)
	 */
	// NBA100 changed to protected method
	protected void processPolicyObject() throws NbaBaseException, NbaVpmsException {
		try {
			setXmlObject(XO_POLICY);  //NBA104
			setCalculatorAttributes();
			setSpecificModelAttributes();
			initializeAttributesForObject();
			initializeProperties(getNbaTxLife().getPolicy().getId());  //NBA104
			// NBA104 deleted code
		} catch (Exception e) {
			NbaLogFactory.getLogger(this.getClass()).logException(e); //NBA103
		}
	}
	/**
	 * @param life
	 */
	// NBA100 changed to protected method
	protected void setNbaTxLife(NbaTXLife life) {
		nbaTxLife = life;
	}
	/**
	 * The initializeAttributesForObject method
	 */
	// NBA100 changed to protected method
	protected void initializeAttributesForObject() throws NbaBaseException, NbaVpmsException {
		if (oinkData == null) {
			oinkData = new SrvNbaOinkDataAccess(nbaTxLife);  //NBA213
			oinkData.setPlanSource(nbaTxLife);
		}
		if (oinkRequest == null) {
			oinkRequest = new NbaOinkRequest();
		}
		// NBA104 deleted code
		// set the attributes
		VpmsModelResult vpmsModelResult = nbaResult.getVpmsModelResult();	//NBA100
		int count = vpmsModelResult.getVpmsAttrCount();	//NBA100
		String value = null;
		String name = null;  //NBA104
		String[] values = null;
		VpmsAttr anAttr;
		for (int cnt = 0; cnt < count; cnt++) {	//NBA100
			anAttr = vpmsModelResult.getVpmsAttrAt(cnt);	//NBA100
			value = DEFAULT_VALUE;
			try {
				// begin NBA104
				if (!includeAttribute(anAttr)){
					continue;
				}
				if (getLogger().isDebugEnabled()) {
					if (anAttr.getSource().equals("-")) {
						getLogger().logDebug("Resolving " + anAttr.getName() + ", OINK name is '-'");
					}
				}
				// end NBA104
				oinkRequest.initFilters();	//NBA100
				oinkRequest.setVariable(anAttr.getSource());
				oinkRequest.setCount(getAttrCount());	//NBA100				 
				values = oinkData.getStringValuesFor(oinkRequest);
				if (values.length > 0) {
					for (int i = 0; i < values.length; i++) {
						value = values[i];
						if (anAttr.hasTransTbl()) {
							value = getTranslationValueForAttribute(anAttr, value);
						}
						if (value.equals("") || value.equals("-1")) {
							value = DEFAULT_VALUE;
						}
						// begin NBA104
						name = createSubscriptedAttributeName(anAttr, i);
						getDynReqSeq().addSetAttribute(name, value);
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("Resolved " + name + " = " + value);
						}
						// end NBA104
					}
				} else {
					// begin NBA104
					for (int i=0; i<getAttrCount(); i++) {
						name = createSubscriptedAttributeName(anAttr, i);
						getDynReqSeq().addSetAttribute(name, value);
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("Resolved using default value " + name + " = " + value);
						}
					}
					// end NBA104
				}
			// NBA104 deleted code
			} catch (NbaBaseException nbe) {
				value = DEFAULT_VALUE;
				// NBA104 deleted code
				// begin NBA104
				name = createSubscriptedAttributeName(anAttr, 0);
				getDynReqSeq().addSetAttribute(name, value);
				if (getLogger().isInfoEnabled()) {
					getLogger().logInfo("Exception caught resolving (" + name + "): " + nbe.getMessage());
				}
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Resolved using default value " + name + " = " + value);
				}
				// end NBA104
			}
		}
	}
	/**
	 * The initializeKeyAttributes method
	 */
	// NBA100 changed to protected method
	protected void initializeKeyAttributes(VpmsComputeResult aResult) throws NbaBaseException, NbaVpmsException {
		if (aResult.getResult() == null || aResult.getResult().length() <= 0) {
			return;
		}
		NbaVpmsModelResult keyResults = new NbaVpmsModelResult(aResult.getResult());
		ListIterator list = keyResults.getVpmsModelResult().getStandardAttr().listIterator();
		while (list.hasNext()) {
			StandardAttr anAttr = (StandardAttr) list.next();
			String value = NbaTableAccessConstants.WILDCARD;
			if (!anAttr.getAttrValue().equals(NbaTableAccessConstants.WILDCARD)) {
				try {
					value = getValue(anAttr);  //NBA104
				} catch (NbaDataException nde) {
				}
			}
			// begin NBA104
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("NbaContractCalculatorCommon resolved " + anAttr.getAttrName() + " = " + value);
			}
			// end NBA104
			calcReqSeq.addSetAttribute(anAttr.getAttrName(), value);
		}
	}
	/**
	 * @param sequence
	 */
	// NBA100 changed to protected method
	protected void setCalcReqSeq(RequestSequence sequence) {
		calcReqSeq = sequence;
	}
	/**
	 * @return
	 */
	// NBA100 changed to protected method
	protected String getCalcType() {
		return calcType;
	}
	/**
	 * This method initializes the NbaVpmsAdaptor object and calls the 
	 * VPMS Calculations model to get the model name for the calculator
	 * as well as a list of attributes that need to be populated and a 
	 * list of properties that will need to be computed by the calculator.
	 * These values are added to the NbaVpmsModelResult object for later
	 * use by the calculator.
	 */
	public void initialize() throws NbaBaseException {
		try {
			setCalcProxy(new NbaVpmsAdaptor(oinkData, CALCULATIONS_MODEL));	//NBA100
			setCalcReqSeq(new RequestSequence());  //NBA104
			setCalculationModelAttributes();
			// begin NBA104
			if (getLogger().isInfoEnabled()) {
				getLogger().logInfo("VPMS call to " + CALCULATIONS_MODEL + ", entry point is " + EP_GET_MODEL_KEYS);
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Turning on VPMS logging");
				getCalcReqSeq().addSetAttribute(ATR_DEBUG, DEBUG_ON);
				getCalcReqSeq().addSetAttribute(ATR_TRACE, TRACE_OFF);
				getCalcReqSeq().addCompute(EP_VERSION);
				getCalcReqSeq().addCompute(EP_DATE);
				getCalcReqSeq().addCompute(EP_COMPILER_VERSION);
				getCalcReqSeq().addCompute(EP_VPM_DATE);
				getCalcReqSeq().addCompute(EP_VPM_VERSION);
				getCalcReqSeq().addCompute(EP_PMS_VERSION_NUMBER);
			}
			// end NBA104
			int modelKeyNdx = calcReqSeq.addCompute(EP_GET_MODEL_KEYS);
			List resultList = calcProxy.getResults(calcReqSeq);
			setCalcReqSeq(new RequestSequence());  //NBA104
			initializeKeyAttributes((VpmsComputeResult) resultList.get(modelKeyNdx));
			//begin NBA100
			if (!noModelDefined()) {
				// begin NBA104			
				if (getLogger().isInfoEnabled()) {
					getLogger().logInfo(
						"VPMS call to " + CALCULATIONS_MODEL + ", entry point is " + EP_GET_MODEL_INFORMATION);
				}
				// end NBA104
				int modelInfoNdx = calcReqSeq.addCompute(EP_GET_MODEL_INFORMATION);
				resultList = calcProxy.getResults(calcReqSeq);
				VpmsComputeResult modelResult = (VpmsComputeResult) resultList.get(modelInfoNdx);
				try {
					nbaResult = new NbaVpmsModelResult(modelResult.getResult());
				} catch (NbaVpmsException nve) {
					// begin NBA104
					StringBuffer sb = new StringBuffer("Unable to load ");
					sb.append(getModelName());
					sb.append(" calculation model for calculation type ");
					sb.append(getCalcType());
					sb.append(". VPMS response is: ");
					sb.append(nve.getMessage());
					// NBA103 code deleted
					throw new NbaVpmsException(sb.toString(), nve);
					// end NBA104
				}
				// NBA104 deleted code
				// begin NBA104
				setModelDebugAttributes();
				setDynamicAttribute(ATR_MINCACHESIZE, nbaResult.getCacheSize());
				setDynamicAttribute(ATR_CALLING_SYSTEM, CALLING_SYSTEM);
				// end NBA104
			}
			//end NBA100
		} catch (RemoteException e) {
			throw new NbaBaseException(e);
		}
	}
	/**
	 * @param string
	 */
	// NBA100 changed to protected method
	protected void setCalcType(String string) {
		calcType = string;
	}
	/**
	 * The getTranslationValueForAttribute method
	 */
	// NBA100 changed to protected method
	protected String getTranslationValueForAttribute(VpmsAttr anAttr, String aValue) throws NbaBaseException, NbaVpmsException {
		try {
			RequestSequence trxReqSeq = new RequestSequence();
			trxReqSeq.addSetAttribute(ATR_TRANS_VALUE, aValue);
			trxReqSeq.addSetAttribute(ATR_TRANS_TABLE, anAttr.getTransTbl());
			// begin NBA104
			if (getLogger().isInfoEnabled()) {
				getLogger().logInfo("VPMS call to " + CALCULATIONS_MODEL + ", entry point is " + EP_GET_TRANSLATION_VALUE);
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Translating '" + aValue + "' using table " + anAttr.getTransTbl());
			}
			// end NBA104
			int computeIndex = trxReqSeq.addCompute(EP_GET_TRANSLATION_VALUE);
			List resultList = calcProxy.getResults(trxReqSeq);
			VpmsComputeResult aResult = (VpmsComputeResult) resultList.get(computeIndex);
			NbaVpmsModelResult temp = new NbaVpmsModelResult(aResult.getResult());
			if (temp.getVpmsModelResult().getTranslationValue().length() > 0) {
				return temp.getVpmsModelResult().getTranslationValue();
			} else {
				return DEFAULT_VALUE;
			}
		} catch (RemoteException e) {
			throw new NbaBaseException(e);
		}
	}
	
	/**
	 * Translates the calculated property results using the specified translation table assigned in the 
	 * T_model_properties_table.
	 * @param resultList
	 * @return
	 * @throws NbaVpmsException
	 */
	// NBA104 New Method
	protected List translatePropertyResults(List resultList) throws NbaBaseException, NbaVpmsException {
		List transProps = new ArrayList();
		RequestSequence trxReqSeq = new RequestSequence();

		// Looking for properties that need to be translated.  Set them all up for one call to the VPMS model
		for (int i = 0; i < dynReqProps.size(); i++) {
			NbaContractCalculatorProperty nbaProperty = (NbaContractCalculatorProperty)dynReqProps.get(i);
			if (nbaProperty.getTranslationTable() != null && nbaProperty.getTranslationTable().trim().length() > 0) {
				VpmsComputeResult propResult = (VpmsComputeResult) resultList.get(nbaProperty.getResultIndex());
				if (propResult.getReturnCode() == 0) {
					trxReqSeq.addSetAttribute(ATR_TRANS_VALUE, propResult.getResult());
					trxReqSeq.addSetAttribute(ATR_TRANS_TABLE, nbaProperty.getTranslationTable());
					nbaProperty.setTranslationIndex(trxReqSeq.addCompute(EP_GET_TRANSLATION_VALUE));
					transProps.add(nbaProperty);
				}
			}
		}
		int count = transProps.size();
		if (count > 0) {
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("VPMS call to " + CALCULATIONS_MODEL + ", entry point is " + EP_GET_TRANSLATION_VALUE);
			}
			try {
				List transList = calcProxy.getResults(trxReqSeq);		// call the model to translate the values 
				for (int i = 0; i < count; i++) {
					NbaContractCalculatorProperty nbaProperty = (NbaContractCalculatorProperty)transProps.get(i);
					VpmsComputeResult propResult = (VpmsComputeResult) resultList.get(nbaProperty.getResultIndex());
				
					VpmsComputeResult aResult = (VpmsComputeResult) transList.get(nbaProperty.getTranslationIndex());
					NbaVpmsModelResult temp = new NbaVpmsModelResult(aResult.getResult());
					if (temp.getVpmsModelResult().getTranslationValue().length() > 0) {
						propResult.setResult(temp.getVpmsModelResult().getTranslationValue());
					}
				}
			} catch (RemoteException e) {
				throw new NbaBaseException(e);
			}
		}
		return resultList;
	}
	// NBA104 New Method
	protected String[] getValues(VpmsAttr anAttr) throws NbaBaseException {
		if (oinkData == null) {
			oinkData = new SrvNbaOinkDataAccess(nbaTxLife);  //NBA213
			oinkData.setPlanSource(nbaTxLife);
		}
		if (oinkRequest == null) {
			oinkRequest = new NbaOinkRequest();
		}
		oinkRequest.initFilters();
		oinkRequest.setVariable(anAttr.getSource());
		oinkRequest.setCount(getAttrCount());				 
		return oinkData.getStringValuesFor(oinkRequest);
	}
	
	// NBA104 New Method
	protected String getValue(StandardAttr anAttr) throws NbaBaseException {
		String[] values = getValues(anAttr);
		if (values.length > 0) {
			return values[0];
		}
		return "";
	}

	// NBA104 New Method
	protected String[] getValues(StandardAttr anAttr) throws NbaBaseException {
		if (oinkData == null) {
			oinkData = new SrvNbaOinkDataAccess(nbaTxLife);  //NBA213
			oinkData.setPlanSource(nbaTxLife);
		}
		if (oinkRequest == null) {
			oinkRequest = new NbaOinkRequest();
		}
		oinkRequest.initFilters();
		oinkRequest.setVariable(anAttr.getAttrValue());
		return oinkData.getStringValuesFor(oinkRequest);
	}

	/**
	 * @return
	 */
	// NBA100 changed to protected method
	protected java.lang.String getXmlObject() {
		return xmlObject;
	}
	/**
	 * @param string
	 */
	// NBA100 changed to protected method
	protected void setXmlObject(java.lang.String string) {
		xmlObject = string;
	}
	/**
	 * Sets a dynamic attribute and removes it from the result. 
	 * @param 
	 */
	// NBA104 New Method
	protected void setDynamicAttribute(String attrName, String attrValue) throws NbaBaseException {
		getDynReqSeq().addSetAttribute(attrName, attrValue);
		nbaResult.removeStandardAttributeFromVpmsAttr(attrName);
	}
	/**
	 * Sets the appropriate debug attributes for the calculation model as specified
	 * by the control model. 
	 * @param 
	 */
	// NBA104 New Method
	protected void setModelDebugAttributes() {
		if (nbaResult.getDebugInd().equalsIgnoreCase(DBG_DEBUG)) {
			getDynReqSeq().addSetAttribute(ATR_DEBUG, DEBUG_ON);
			getDynReqSeq().addSetAttribute(ATR_TRACE, TRACE_OFF);
			getDynReqSeq().addCompute(EP_VERSION);
			getDynReqSeq().addCompute(EP_DATE);
			getDynReqSeq().addCompute(EP_COMPILER_VERSION);
			getDynReqSeq().addCompute(EP_VPM_DATE);
			getDynReqSeq().addCompute(EP_VPM_VERSION);
			getDynReqSeq().addCompute(EP_PMS_VERSION_NUMBER);
		} else if (nbaResult.getDebugInd().equalsIgnoreCase(DBG_TRACE)) {
			getDynReqSeq().addSetAttribute(ATR_DEBUG, DEBUG_ON);
			getDynReqSeq().addSetAttribute(ATR_TRACE, TRACE_ON);
			getDynReqSeq().addCompute(EP_VERSION);
			getDynReqSeq().addCompute(EP_DATE);
			getDynReqSeq().addCompute(EP_COMPILER_VERSION);
			getDynReqSeq().addCompute(EP_VPM_DATE);
			getDynReqSeq().addCompute(EP_VPM_VERSION);
			getDynReqSeq().addCompute(EP_PMS_VERSION_NUMBER);
		} else {
			getDynReqSeq().addSetAttribute(ATR_DEBUG, DEBUG_OFF);
			getDynReqSeq().addSetAttribute(ATR_TRACE, TRACE_OFF);
		}
	}
	/**
	 * Creates a subscripted attribute name if required.  The control model specifies
	 * the subscript requirements of the calculation model.
	 * @param vAttr attribute definition from the control model
	 * @param subscript iteration of resolved value
	 * @return attribute name to be passed to the calculation model
	 */
	// NBA104 New Method
	protected String createSubscriptedAttributeName(StandardAttr sAttr) {
		VpmsAttr vAttr = null;
		VpmsModelResult vpmsModelResult = nbaResult.getVpmsModelResult();

		// find corresponding vpmsAttr
		if (vpmsModelResult != null) {
			int count = vpmsModelResult.getVpmsAttrCount();
			for (int i = 0; i < count; i++) {
				vAttr = vpmsModelResult.getVpmsAttrAt(i);
				if (vAttr != null && sAttr.getAttrName().equalsIgnoreCase(vAttr.getName())) {
					return createSubscriptedAttributeName(vAttr, 0);
				}
			}
		}
		return sAttr.getAttrName();
	}
	/**
	 * Creates a subscripted attribute name if required.  The control model specifies
	 * the subscript requirements of the calculation model.
	 * @param vAttr attribute definition from the control model
	 * @param subscript iteration of resolved value
	 * @return attribute name to be passed to the calculation model
	 */
	// NBA104 New Method
	protected String createSubscriptedAttributeName(VpmsAttr vAttr, int subscript) {
		if (isAttributeSubscripted(vAttr)) {
			StringBuffer name = new StringBuffer(vAttr.getName());
			name.append("[");
			name.append(String.valueOf(subscript));
			name.append("]");
			return name.toString();
		}
		return vAttr.getName();
	}
	/**
	 * Determines if an attribute should be subscripted.
	 * @param vAttr attribute definition from the control model
	 * @return true if attribute datatype is defined as subscripted
	 */
	// NBA104 New Method
	protected boolean isAttributeSubscripted(VpmsAttr vAttr) {
		String dataType = vAttr.getDataType();
		if (dataType != null) {
			dataType.toUpperCase();
			if (dataType.equals(TYP_BENEFIT_S) || 
				dataType.equals(TYP_COVERAGE_S) ||
				dataType.equals(TYP_FUND_S) ||
				dataType.equals(TYP_MTHLY_PUA_S) ||
				dataType.equals(TYP_PREMPAY_S) ||
				dataType.equals(TYP_WTHDRWL_S) ||
				dataType.equals(TYP_PREMPU_S) ||
				dataType.equals(TYP_RIDER_S) ||
				dataType.equals(TYP_LOAN_S) ||
				dataType.equals(TYP_CURRINT_TIER_S) ||
				dataType.equals(TYP_VALUEPU_S) ||
				dataType.equals(TYP_POLYRVAL_S) ||
				dataType.equals(TYP_TARGET_S) ||
				dataType.equals(TYP_CALENDARYR_S) ||
				dataType.equals(TYP_PRIMARY_OR_JNT_EXTRA_S) ||	//NBA100
				dataType.equals(TYP_VALUEPHASE_S)) {
					return true;
				}
		}
		return false;
	}
	/**
	 * Determines if an attribute should be included.  For example, if there are no
	 * benefits, then don't send any of the benefit attributes.
	 * @param vAttr attribute definition from the control model
	 * @return true if attribute datatype is defined as subscripted
	 */
	// NBA104 New Method
	protected boolean includeAttribute(VpmsAttr vAttr) {
		String dataType = vAttr.getDataType();
		setAttrCount(1);	//NBA100
		if (dataType != null) {
			dataType.toUpperCase();
			if (dataType.equals(TYP_BENEFIT) || dataType.equals(TYP_BENEFIT_S)) {
				if (setAttrCount(getBenefitCount()) == 0) {	//NBA100
					return false;
				}
			}
			if (dataType.equals(TYP_COVERAGE) || dataType.equals(TYP_COVERAGE_S)) {
				if (setAttrCount(getCoverageCount()) == 0) {	//NBA100
					return false;
				}
			}
			if (dataType.equals(TYP_RIDER) || dataType.equals(TYP_RIDER_S)) {
				if (setAttrCount(getRiderCount()) == 0) {	//NBA100
					return false;
				}
			}
			if (dataType.equals(TYP_PRIMARY_OR_JNT_EXTRA_S)) {	//NBA100
				if (setAttrCount(getPrimOrJntExtraCount()) == 0) {	//NBA100
					return false;	//NBA100
				}	//NBA100
			}	//NBA100
		}
		return true;
	}
	/**
	 * Determines if an attribute should be included.  For example, if there are no
	 * benefits, then don't send any of the benefit attributes.
	 * @param sAttr attribute definition from the control model
	 * @return true if attribute datatype is defined as subscripted
	 */
	// NBA104 New Method
	protected boolean includeAttribute(StandardAttr sAttr) {
		VpmsAttr vAttr = null;
		VpmsModelResult vpmsModelResult = nbaResult.getVpmsModelResult();

		// find corresponding vpmsAttr
		if (vpmsModelResult != null) {
			int count = vpmsModelResult.getVpmsAttrCount();
			for (int i = 0; i < count; i++) {
				vAttr = vpmsModelResult.getVpmsAttrAt(i);
				if (vAttr != null && sAttr.getAttrName().equalsIgnoreCase(vAttr.getName())) {
					return includeAttribute(vAttr);
				}
			}
		}
		return true;
	}
	/**
	 * Determine if a VPMS model has been specifically omitted for a calculation type.
	 * If the Model Name is <code>NONE</code>, there is no model defined and the
	 * calculation is not applicable.
	 * @return true if the model name is <code>NONE</code>
	 * @throws RemoteException
	 * @throws NbaVpmsException if the model name is missing
	 */
	// NBA100 New Method
	protected boolean noModelDefined() throws RemoteException, NbaVpmsException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(
				"VPMS call to " + CALCULATIONS_MODEL + ", entry point is " + EP_GET_MODEL_NAME);
		}
		int modelNameNdx = calcReqSeq.addCompute(EP_GET_MODEL_NAME);
		List resultList = calcProxy.getResults(calcReqSeq);
		VpmsComputeResult computeResult = (VpmsComputeResult) resultList.get(modelNameNdx);
		NbaVpmsModelResult nbaResult = new NbaVpmsModelResult(computeResult.getResult());
		setModelName(nbaResult.getModelName());
		if (getModelName() == null || getModelName().length() <= 0) {
			StringBuffer sb = new StringBuffer("Unable to determine Calculation Model for ");
			sb.append(getCalcType()); 
			throw new NbaVpmsException(sb.toString());
		}
		return NO_MODEL.equals(getModelName());
	}
	/**
	 * Retrieve the name of the model for the current calculation type.
	 * @return the name of the model for the current calculation type
	 */
	// NBA100 New Method
	protected String getModelName() {
		return modelName;
	}
	/**
	 * Store the name of the model for the current calculation type.
	 * @param string
	 */
	// NBA100 New Method
	protected void setModelName(String string) {
		modelName = string;
	}
	/**
	 * Retrieve the NbaCalculation object containing the CalculationResult objects which 
	 * contain the values to be returned to the caller. 
	 * @return NbaCalculation
	 */
	// NBA100 New Method	
	protected NbaCalculation getCalcReturnValues() {
		return calcReturnValues;
	}
	/**
	 * Set the NbaCalculation object containing the CalculationResult objects which 
	 * contain the values to be returned to the caller. 
	 * @param calculation
	 */
	// NBA100 New Method
	protected void setCalcReturnValues(NbaCalculation calculation) {
		calcReturnValues = calculation;
	}
	/**
	 * Return true if a "no model" condition was returned from the Calculations Control
	 * model. This condition is true if the entry matching the supplied keys in the 
	 * Calculations Control model <code>T_model_control_table</code> table contains
	 * a value of <code>NONE</code>.
	 * @return boolean
	 */
	// NBA100 New Method
	protected boolean noModelForCalc() {
		return NO_MODEL.equals(getModelName());
	}
	/**
	 * Update the NbaCalculation to indicate that the no model found condition is an 
	 * error.
	 */
	// NBA100 New Method
	protected void createErrorResultForNoModel() {
		getCalcReturnValues().setCalcResultCode(TC_RESCODE_FAILURE);
		CalculationResult calculationResult = new CalculationResult();
		CalcError calcError = new CalcError();
		calcError.setMessage("No model defined for calculation: " + getCalcType());
		calculationResult.setCalcError(calcError);
		getCalcReturnValues().addCalculationResult(calculationResult);
	}
	/**
	 * Retrieve the collection of NbaContractCalculatorProperty objects.
	 * The NbaContractCalculatorProperty links objects and their properties 
	 * to compute entries in a com.csc.dip.jvpms.core.RequestSequence
	 * @return dynReqProps
	 */
	// NBA100 New Method
	protected ArrayList getDynReqProps() {
		return dynReqProps;
	}
	/**
	 * The calculate method directs the processing of the calculator to obtain
	 * the values from the VPMS model.
	 * @param objectID unique identifier of object needing calculation 
	 * @return NbaCalculation contains the results of the calculation
	 */
	// NBA100 New Method
	protected void addComputeForProperty(VpmsProp prop) throws NbaBaseException, RemoteException, NbaVpmsException {
		String objectName = getObjectId() + getObjectDurSuffix();
		String propertyName = prop.getName() + getPropertySubscript();
		// NBA104 deleted code
		NbaContractCalculatorProperty nbaProperty = new NbaContractCalculatorProperty();
		nbaProperty.setObjectID(objectName);
		nbaProperty.setTargetName(prop.getTarget());
		nbaProperty.setTranslationTable(prop.getTransTbl());  //NBA104
		nbaProperty.setResultIndex(getDynReqSeq().addCompute(propertyName));
		getDynReqProps().add(nbaProperty);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Added compute for: " + objectName + ", Property: " + propertyName);
		}
	}
	/**
	 * Add the properties to be computed by the model.
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void addPropertiesToBeComputed() throws RemoteException, NbaVpmsException, NbaBaseException {
		policyLevelCurrentValues(); //Policy level properties to be calculated.
		policyLevelValuesByDuration(); //Policy level properties by duration to be calculated.
		coverageLevelCurrentValues(); //Coverage level properties to be calculated.
		coverageLevelValuesByDuration(); //Coverage level properties by duration to be calculated.
		riderLevelCurrentValues(); //Rider level properties to be calculated.
		riderLevelValuesByDuration(); //Rider level properties by duration to be calculated.
		covOptionLevelValuesByDuration(); //Covoption level properties by duration to be calculated.
		covOptionLevelCurrentValues();//Covoption level properties to be calculated.
	}
	/**
	 * Add Computes for Coverage Level current value Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void coverageLevelCurrentValues() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop;		
		Coverage cov;
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int covIdx = 0; covIdx < getCoverages().size(); covIdx++) {
			cov = (Coverage) getCoverages().get(covIdx);
			setPropertySubscript(covIdx);
			setObjectId(cov.getId());
			setObjectDurSuffix("");
			
			for (int i = 0; i < propCnt; i++) {
				prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
				if (prop.getDataType().equals(PROP_COV)) {
					addComputeForProperty(prop);
				}
			}
		}
	}
	/**
	 * Add Computes for Coverage by Duration Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void coverageLevelValuesByDuration() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop; 
		Coverage cov;
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int covIdx = 0; covIdx < getCoverages().size(); covIdx++) {
			cov = (Coverage) getCoverages().get(covIdx);
			setObjectId(cov.getId());
			int maxDur = NbaUtils.calcYearsDiff(cov.getTermDate(), cov.getEffDate()) + 1;
			for (int dur = 1; dur < maxDur; dur++) {
				for (int i = 0; i < propCnt; i++) {
					setPropertySubscript(covIdx, dur);
					setObjectDurSuffix(dur);
					prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
					if (prop.getDataType().equals(PROP_COV_DUR)) {
						addComputeForProperty(prop);
					}
				}
			}
		}
	}
	/**
	 * Add Computes for CovOption Level current value Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA104 New Method
	protected void covOptionLevelCurrentValues() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop;
		CovOption covOption;
		setObjectDurSuffix("");
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int covOptIdx = 0; covOptIdx < getCovOptions().size(); covOptIdx++) {
			covOption = (CovOption) getCovOptions().get(covOptIdx);
			setObjectId(covOption.getId());
			setPropertySubscript(covOptIdx);
			for (int i = 0; i < propCnt; i++) {
				prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
				if (prop.getDataType().equals(PROP_COVOPT)) {
					addComputeForProperty(prop);
				}
			}
		}
	}
	/**
	 * Add Computes for CovOption by Duration Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void covOptionLevelValuesByDuration() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop;
		CovOption covOption;
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int covOptIdx = 0; covOptIdx < getCovOptions().size(); covOptIdx++) {
			covOption = (CovOption) getCovOptions().get(covOptIdx);
			setObjectId(covOption.getId());
			int maxDur = NbaUtils.calcYearsDiff(covOption.getTermDate(), covOption.getEffDate()) + 1;
			for (int dur = 1; dur < maxDur; dur++) {
				for (int i = 0; i < propCnt; i++) {
					setPropertySubscript(covOptIdx, dur);
					setObjectDurSuffix(dur);
					prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
					if (prop.getDataType().equals(PROP_COVOPT_DUR)) {
						addComputeForProperty(prop);
					}
				}
			}
		}
	}
	/**
	 * Retrieve the Coverages
	 * @return a List containing the Base Coverage and Increase Riders
	 */
	// NBA100 New Method
	protected List getCoverages() {
		if (coverages == null) {
			coverages = getNbaTxLife().getNbaHolding().getNonRiders();
		}
		return coverages;
	}
	/**
	 * Retrieve the CovOptions for all Coverages
	 * @return a List containing the CovOptions
	 */
	// NBA100 New Method
	protected List getCovOptions() {
		ArrayList dpwFirstBenefitList = new ArrayList(); //ALII1454
		
		if (covOptions == null) {
			covOptions = new ArrayList();
			Life life = getNbaTxLife().getLife();
			if (life != null) {
				int covCount = life.getCoverageCount();
				Coverage cov;
				CovOption covOption;
				for (int i = 0; i < covCount; i++) {
					cov = life.getCoverageAt(i);
					if (!cov.isActionDelete()) {
						int covOptionCount = cov.getCovOptionCount();
						for (int j = 0; j < covOptionCount; j++) {
							covOption = cov.getCovOptionAt(j);
							if (!covOption.isActionDelete()) {
								covOptions.add(covOption);
								if(covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_WP){ //ALII1454
									dpwFirstBenefitList.add(covOption);
								}
							}
						}
					}
				}
			}
		}
		
		//ALII1454 start
		if(dpwFirstBenefitList.size() > 0) {
			for (int j = 0; j < covOptions.size(); j++) {
				CovOption covOption = (CovOption) covOptions.get(j);
				if(covOption.getLifeCovOptTypeCode() != NbaOliConstants.OLI_OPTTYPE_WP){ 
					dpwFirstBenefitList.add(covOption);
				}
			}
			covOptions = dpwFirstBenefitList;
			return dpwFirstBenefitList;
		}
		//ALII1454 end
		
		return covOptions;
	}
	/**
	 * Get the value for the Object subscript string. This is a duration.
	 * @return objectDurSuffix
	 */
	// NBA100 New Method
	protected String getObjectDurSuffix() {
		return objectDurSuffix;
	}
	/**
	 * Set the value for the id of the object
	 * @return objectId
	 */
	// NBA100 New Method
	protected String getObjectId() {
		return objectId;
	}
	/**
	 * Get the value for the property subscript string. This is either 
	 * a duration or a sequence and duration
	 * @return propertySubscript
	 */
	// NBA100 New Method
	protected String getPropertySubscript() {
		return propertySubscript;
	}
	/**
	 * Retrieve the Riders
	 * @return a List containing the non-Increase Riders
	 */
	// NBA100 New Method
	protected List getRiders() {
		if (riders == null) {
			riders = getNbaTxLife().getNbaHolding().getRiders();
		}
		return riders;
	}
	/**
	 * Add Computes for Policy Level current value Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void policyLevelCurrentValues() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop;
		setObjectId(getNbaTxLife().getPolicy().getId());
		setPropertySubscript("");
		setObjectDurSuffix("");
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int i = 0; i < propCnt; i++) {
			prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
			if (prop.getDataType().equals(PROP_POLICY)) {
				addComputeForProperty(prop);
			}
		}
	}
	/**
	 * Add Computes for Policy by Duration Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void policyLevelValuesByDuration() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop;
		Policy policy = getNbaTxLife().getPolicy();
		setObjectId(policy.getId());
		int maxDur = NbaUtils.calcYearsDiff(policy.getTermDate(), policy.getEffDate()) + 1;
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int dur = 1; dur < maxDur; dur++) {
			for (int i = 0; i < propCnt; i++) {
				setPropertySubscript(dur);
				setObjectDurSuffix(dur);
				prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
				if (prop.getDataType().equals(PROP_POLICY_DUR)) {
					addComputeForProperty(prop);
				}
			}
		}
	}
	/**
	 * Add Computes for Rider Level current value Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void riderLevelCurrentValues() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop;
		Coverage rider;
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int riderIdx = 0; riderIdx < getRiders().size(); riderIdx++) {
			rider = (Coverage) getRiders().get(riderIdx);
			setPropertySubscript(riderIdx);
			setObjectId(rider.getId());
			setObjectDurSuffix("");
			for (int i = 0; i < propCnt; i++) {
				prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
				if (prop.getDataType().equals(PROP_RDR)) {
					addComputeForProperty(prop);
				}
			}
		}
	}
	/**
	 * Add Computes for Rider by Duration Properties 
	 * @throws RemoteException
	 * @throws NbaVpmsException
	 * @throws NbaBaseException
	 */
	// NBA100 New Method
	protected void riderLevelValuesByDuration() throws RemoteException, NbaVpmsException, NbaBaseException {
		VpmsProp prop;
		Coverage rider;
		int propCnt = nbaResult.getVpmsModelResult().getVpmsPropCount();
		for (int riderIdx = 0; riderIdx < getRiders().size(); riderIdx++) {
			rider = (Coverage) getRiders().get(riderIdx);
			setObjectId(rider.getId());
			int maxDur = NbaUtils.calcYearsDiff(rider.getTermDate(), rider.getEffDate()) + 1;
			for (int dur = 1; dur < maxDur; dur++) {
				for (int i = 0; i < propCnt; i++) {
					setPropertySubscript(riderIdx, dur);
					setObjectDurSuffix(dur);
					prop = nbaResult.getVpmsModelResult().getVpmsPropAt(i);
					if (prop.getDataType().equals(PROP_RDR_DUR)) {
						addComputeForProperty(prop);
					}
				}
			}
		}
	}
	/**
	* Set the attributes requested for the model. Process the Model specific (non-OINK)
	* attributes and properties. Resolve the OINK attribute values. Set the Properties to cause 
	* premiums to be calculated for the standard modes
	*/
	// NBA100 New Method
	protected void setModelAttributes() throws NbaVpmsException, NbaBaseException {
		setSpecificModelAttributes(); //Retrieve and set the Model specific attributes and properties
		initializeAttributesForObject(); //Set the attributes for the contract
	}
	/**
	 * Format the string to be used to identify an object by duration
	 * @param dur
	 */
	// NBA100 New Method
	protected void setObjectDurSuffix(int dur) {
		StringBuffer buff = new StringBuffer();
		buff.append("[");
		buff.append(String.valueOf(dur));
		buff.append("]");
		setObjectDurSuffix(buff.toString());
	}
	/**
	 * Set the value for the Object subscript string. This is a duration.
	 * @param string
	 */
	// NBA100 New Method
	protected void setObjectDurSuffix(String string) {
		objectDurSuffix = string;
	}
	/**
	 * Get the value for the id of the object
	 * @param string
	 */
	// NBA100 New Method
	protected void setObjectId(String string) {
		objectId = string;
	}
	/**
	 * Format the string to be used to set a property by sequence
	 * @param objSubscript
	 */
	// NBA100 New Method
	protected void setPropertySubscript(int objSubscript) {
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		buff.append(String.valueOf(objSubscript));
		buff.append(")");
		setPropertySubscript(buff.toString());
	}
	/**
	 * Format the string to be used to set a property by sequence and duration
	 * @param objSubscript
	 * @param durSubscript
	 */
	// NBA100 New Method
	protected void setPropertySubscript(int objSubscript, int durSubscript) {
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		buff.append(String.valueOf(objSubscript));
		buff.append("; ");
		buff.append(String.valueOf(durSubscript));
		buff.append(")");
		setPropertySubscript(buff.toString());
	}
	/**
	 * Set the value for the property subscript string. This is either 
	 * a duration or a sequence and duration
	 * @param string
	 */
	// NBA100 New Method
	protected void setPropertySubscript(String string) {
		propertySubscript = string;
	}
 
	/**
	 * Returns the number of SubStandard Ratings for the Primary or Joint Insured.
	 * @return
	 */
	// NBA100 New Method
	protected int getPrimOrJntExtraCount() {
		if (primOrJntExtraCount == -1) {
			primOrJntExtraCount = getCount("PNTEXTRA");
		}
		return primOrJntExtraCount;
	}	
	/**
	 * Retieve the SubStandardRatings for the Primary or Joint Insured
	 * @return
	 */
	// NBA100 New Method	
	protected List getPrimaryOrJointSubstandard() {
		if (primOrJntExtras == null) {
			primOrJntExtras = new ArrayList();
			Coverage coverage;
			LifeParticipant lifeParticipant;
			SubstandardRating substandardRating;
			for (int i = 0; i < getCoverages().size(); i++) {
				coverage = (Coverage) getCoverages().get(i);
				for (int j = 0; j < coverage.getLifeParticipantCount(); j++) {
					lifeParticipant = coverage.getLifeParticipantAt(j);
					if (!lifeParticipant.isActionDelete() && NbaUtils.isInsuredParticipant(lifeParticipant)) {
						for (int k = 0; k < lifeParticipant.getSubstandardRatingCount(); k++) {
							substandardRating = lifeParticipant.getSubstandardRatingAt(k);
							if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
								primOrJntExtras.add(substandardRating);
							}
						}
					}
				}
			}
		}
		return primOrJntExtras;
	}
	/**
	 * Retrieve the number of attribute values to be calculated
	 * @return the number of attribute values to be calculated
	 */
	// NBA100 New Method		
	protected int getAttrCount() {
		return attrCount;
	}

	/**
	 * Set the number of attribute values to be calculated
	 * @param i
	 */
	// NBA100 New Method	
	protected int setAttrCount(int i) {
		attrCount = i;
		return i;
	}

}
