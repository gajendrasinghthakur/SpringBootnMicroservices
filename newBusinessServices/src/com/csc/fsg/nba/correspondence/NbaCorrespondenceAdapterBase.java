package com.csc.fsg.nba.correspondence;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 * 
 */
import java.io.ByteArrayInputStream;
import java.util.List;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.business.process.NbaAutoProcessProxy;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNoValueException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.CorrMethod;
import com.csc.fsg.nba.vo.configuration.Correspondence;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;

//SPR1906 line deleted

/** 
 * This class is an abstract class that implements the NbaCorrespondence Adapter partially.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr> 
 * <tr><td>NBA050</td><td>Version 3</td><td>Nba Pending Database</td></tr>
 * <tr><td>NBA091</td><td>Version 3</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR1906</td><td>Version 4</td><td>Delete import statement referring to Nba Development module. </td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression integration</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation for Agent Subset</td></tr>
 * <tr><td>ALPC96</td><td>AXA Life Phase 1</td><td>xPression OutBound Email</td></tr>
 * <tr><td>AXAL3.7.13</td><td>AXA Life Phase 1</td><td>Formal Correspondence</td></tr>
 * <tr><td>CR58636</td><td>ADC</td><td>NIGO Correspondence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public abstract class NbaCorrespondenceAdapterBase implements NbaCorrespondenceAdapter {
	protected NbaDst nbaDst;
	protected NbaLogger logger;
	protected Correspondence configInfo; //ACN012
	protected com.csc.fsg.nba.vo.nbaschema.Correspondence corrXML; //ACN012
	protected NbaOinkDataAccess oinkAccess;
	protected NbaTXLife nbaTXLife;                         //NBLXA-2114
	protected NbaOinkRequest oinkRequest;
	protected NbaUserVO user; //NBA129
	protected NbaDst parentDst; //ALS4476
	protected java.lang.String letterType;//AXAL3.7.32
	protected boolean letterImage; // CR58636
	
/**
 * This method does a Holding Inquiry and returns an instance of <code>NbaTXLife</code>.
 * @return com.csc.fsg.nba.vo.NbaTXLife
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever there is an exception.
 */
protected NbaTXLife doHoldingInquiry() throws NbaBaseException {
	//NBA050 code deleted

	NbaAutoProcessProxy proxy = new NbaAutoProcessProxy(new NbaUserVO(NbaConfiguration.getInstance().getCorrespondence().getEventUserName(), ""), nbaDst); //NBA050 NBA146
	NbaTXLife response = proxy.doHoldingInquiry(); //NBA050

	//Handle host response
    List responses = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponse();
    TXLifeResponse theResponse = (TXLifeResponse) responses.get(0);
    if (theResponse.getTransResult().getResultCode() > 1) {
        getLogger(this.getClass().getName()).logError("Invalid host response. Correspondence event can't be evaluated!");
        throw new NbaNoValueException("Host response Invalid!");
    }
	return response;
}
/**
 * This method returns an instance of <code>NbaConfigCorrespondence</code>.
 * @return com.csc.fsg.nba.configuration.NbaConfigCorrespondence
 */
// ACN012 changed signature
protected Correspondence getConfigInfo() throws NbaBaseException {
	if (configInfo == null) {
		configInfo = NbaConfiguration.getInstance().getCorrespondence(); //ACN012
	}
	return configInfo;
}
/**
 * This method retrieves a letter name from an XML Source present on the Correspondence work Item or Requirement work Item.
 * @return java.lang.String
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception when an XML source is not present on the correspondence work item.
 */
public String getLetterNameFromSource() throws com.csc.fsg.nba.exception.NbaBaseException { //APSL4270
	String strLetterName = null;
	if (corrXML == null && nbaDst.isTransaction() ){
		if(nbaDst.getTransaction().getWorkType().equals(NbaConstants.A_WT_CORRESPONDENCE)
				||nbaDst.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)) { //ALPC96
		NbaSource source = null; //A CORRXML source      
		int numberOfSources = nbaDst.getNbaSources().size(); //SPR3290
		for (int i = 0; i < numberOfSources ; i++) { //SPR3290
			source = (NbaSource) nbaDst.getNbaSources().get(i);
			if (source.getSource().getSourceType().equalsIgnoreCase(NbaConstants.A_ST_CORRESPONDENCE_XML)) { //Want a CorrXML source
				break; //found a match, so break    
			}
		}
		//APSL4270 start
		if (source == null || !NbaConstants.A_ST_CORRESPONDENCE_XML.equalsIgnoreCase(source.getSource().getSourceType())) {
			return null;
		}
		//APSL4270 end
		try {
			corrXML = com.csc.fsg.nba.vo.nbaschema.Correspondence.unmarshal(new ByteArrayInputStream(source.getText().getBytes())); //ACN012
			strLetterName = corrXML.getLetterName();
			setLetterImage(corrXML.getImage()); // CR58636
		} catch (Exception e) {
			getLogger(this.getClass().getName()).logError(e);
			throw new NbaBaseException("Error in :" + this.getClass().getName(), e);
		}
		}
	}
	if (corrXML != null) { //APSL4270 start
		strLetterName = corrXML.getLetterName();
	} //APSL4270 end
	return strLetterName;
}
/**
 * This method returns an instance of <code>NbaConfigCorrespondence</code>.
 * @return com.csc.fsg.nba.configuration.NbaConfigCorrespondence
 */
//ACN012 New Method
protected CorrMethod getCorrMethod(String aMethod) throws NbaBaseException {
	return NbaConfiguration.getInstance().getCorrMethod(aMethod);
}
/**
 * This method initialises and returns the instance of NbaLogger.
 * 
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected NbaLogger getLogger(String aClass) {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(aClass);
		} catch (Exception e) {
			NbaBootLogger.log(aClass + " could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * This method is used to initialize an <code>NbaDst</code> instance
 * @param newNbaDst an <code>NbaDst</code> instance
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever there is a parsing exception.
 */
protected void initializeNbaDst(NbaDst newNbaDst) throws com.csc.fsg.nba.exception.NbaBaseException {
	//Store the clone of the actual object
	try {
		nbaDst = newNbaDst; //SPR3290
	} catch (Exception e) {
		getLogger(this.getClass().getName()).logError("Invalid NbaDst. Error: " + e);
		throw new NbaBaseException(e.getMessage());
	}
}
/**
 * This method initializes data for oinkAccess to work on.
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever there is an exception.
 */
protected void initializeOinkAccess(NbaUserVO userVO) throws NbaBaseException { //NBA146 Added userVO
	oinkAccess = new NbaOinkDataAccess();
	if (nbaDst.isCase()) {
		oinkAccess.setLobSource(nbaDst.getNbaLob());
	} else if (
		nbaDst.isTransaction()
			&& nbaDst.getTransaction().getWorkType().equals(NbaConstants.A_WT_CORRESPONDENCE)) { //This would be a Correspondence Work Item

		NbaCorrespondenceUtils utils = new NbaCorrespondenceUtils(userVO); //NBA146
		utils.setTransactionID(nbaDst.getID());
		if (corrXML.hasObjectRef()) { //Means that a transaction generated the event
			nbaDst = utils.retrieveCaseAndTransactions();
			NbaTransaction tx = null;
			//if there is no matching Work Item throw an NbaBaseException
			int numberOfTransactions = nbaDst.getNbaTransactions().size(); //SPR3290 
			for (int i = 0; i < numberOfTransactions ; i++) { //SPR3290
				tx = (NbaTransaction) nbaDst.getNbaTransactions().get(i);
				if (tx.getID().equalsIgnoreCase(corrXML.getObjectRef())) {
					break; //found a match, so break    
				}
			}
			oinkAccess.setLobSource(tx.getNbaLob());
		} else {
			nbaDst = utils.retrieveCase();
			oinkAccess.setLobSource(nbaDst.getNbaLob());
		}
	} 
	//NBA091 begin	
	//Begin NBLXA-2114
	if(nbaTXLife == null){
		nbaTXLife = doHoldingInquiry();
	}//End NBLXA-2114
	//AXAL3.7.40 code deleted
	//NBA091 end
	//At this time a Case is always available, otherwise let it break
	try {
		oinkAccess.setContractSource(nbaTXLife); 
		//APSL2808
		oinkAccess.setPlanSource(nbaTXLife,getNbaProduct(nbaTXLife));
	} catch (NullPointerException e) { //Sometimes oinkAccess might break because of invalid XML
		getLogger(this.getClass().getName()).logError(e);
		throw new NbaNoValueException("Invalid Contract data");
	}

	oinkRequest = new NbaOinkRequest();
	oinkRequest.setCount(1);
	oinkRequest.setTableTranslations(true);
	//Begin ALPC96
	if (nbaDst.isTransaction() && nbaDst.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)){
		oinkRequest.setRequirementIdFilter(nbaTXLife.getRequirementInfo(nbaDst.getNbaLob().getReqUniqueID()).getId());
		oinkAccess.setLobSource(nbaDst.getNbaLob());
	}
	//End ALPC96
}
/**
 * This method resolves a variable value using OINK 
 * @param fields A variable whose value needs to be resolved
 * return java.lang.String
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever there is an exception.
 */
protected String resolveVariableValue(String aVariable) throws NbaBaseException {
	oinkRequest.setVariable(aVariable);
	return oinkAccess.getStringValueFor(oinkRequest);
}
/**
 * This method resolves a variable value using OINK 
 * @param fields A variable whose value needs to be resolved
 * return java.lang.String
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception whenever there is an exception.
 */
//AXAL3.7.10A
protected String[] resolveVariableValues(String aVariable) throws NbaBaseException {
	oinkRequest.setVariable(aVariable);
	return oinkAccess.getStringValuesFor(oinkRequest);
}
/**
 * @return
 */
//NBA129 New Method
public NbaUserVO getUser() {
	return user;
}

/**
 * @param userVO
 */
//NBA129 New Method
public void setUser(NbaUserVO userVO) {
	user = userVO;
}


//AXAL3.7.13 - New method
public NbaDst getNbaDst() {
	return nbaDst;
}

//AXAL3.7.13 - New method
public void setNbaDst(NbaDst nbaDst) {
	this.nbaDst = nbaDst;
}

//ALS4476 - New method
public NbaDst getParentDst() {
	return parentDst;
}

//ALS4476 - New method
public void setParentDst(NbaDst nbaParentDst) {
	this.parentDst = nbaParentDst;
}
//CR58636 new Method
/**
 * @return Returns the letterImage.
 */
public boolean isLetterImage() {
	return letterImage;
}

//CR58636 new Method
/**
 * @param letterImage The letterImage to set.
 */
public void setLetterImage(boolean letterImage) {
	this.letterImage = letterImage;
}
/**
 * This method setst he letter type
 * @param string
 */
//AXAL3.7.32 New Method
public void setLetterType(java.lang.String string) {
	letterType = string;
}

/**
 * This method returns the letter type
 * @return java.lang.String
 */
//AXAL3.7.32 New Method
public java.lang.String getLetterType() {
	return letterType;
}


/**
 * Returns the NbaProduct.
 * @return NbaProduct
 */
//APSL2808 Method changed
public AccelProduct getNbaProduct(NbaTXLife nbaTXLife) {
	AccelProduct nbaProduct = null;
	if (nbaProduct == null) {
		NbaProductAccessFacadeBean nbaProductAccessFacade = new NbaProductAccessFacadeBean(); 
		try {
			nbaProduct = nbaProductAccessFacade.doProductInquiry(nbaTXLife);
		} catch (NbaBaseException e) {
			getLogger(this.getClass().getName()).logError(e);
		}
	}
	return nbaProduct;
}
/**
 * @return the nbaTXLife
 */ //NBLXA-2114
public NbaTXLife getNbaTXLife() {
	return nbaTXLife;
}
/**
 * @param nbaTXLife the nbaTXLife to set
 */ //NBLXA-2114
public void setNbaTXLife(NbaTXLife nbaTXLife) {
	this.nbaTXLife = nbaTXLife;
}
}
