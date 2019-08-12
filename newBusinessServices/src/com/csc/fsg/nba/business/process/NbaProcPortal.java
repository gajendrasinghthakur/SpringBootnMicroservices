package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.transaction.NbaClientSearchTransaction;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.exception.NbaParseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaFormsValidationData;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaAgentVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.DistributionChannelInfo;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementInfo;
import com.csc.fsg.nba.vo.txlife.UserAuthRequest;
import com.csc.fsg.nba.vo.txlife.VendorApp;
import com.csc.fsg.nba.vo.txlife.VendorName;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsClientSearchData;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
import com.tbf.xml.XmlValidationError;

/**
 * NbaProcPortal is the class that processes nbAccelerator cases found on the AWD portal queue (NBPORTAL). It performs both Breeze and business
 * validation on the XML103 source
 * 
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead> <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th> </thead>
 * <tr>
 * <td>NBA001</td>
 * <td>Version 1</td>
 * <td>Initial Development</td>
 * </tr>
 * <tr>
 * <td>NBA020</td>
 * <td>Version 2</td>
 * <td>AWD Priority</td>
 * </tr>
 * <tr>
 * <td>NBA023</td>
 * <td>Version 2</td>
 * <td>Forms Tracking and Decisioning</td>
 * </tr>
 * <tr>
 * <td>NBP001</td>
 * <td>Version 3</td>
 * <td>nbProducer Initial Development</td>
 * </tr>
 * <tr>
 * <td>NBA067</td>
 * <td>Version 3</td>
 * <td>Client Search</td>
 * </tr>
 * <tr>
 * <td>ACN012</td>
 * <td>Version 4</td>
 * <td>Architecture Changes</td>
 * </tr>
 * <tr>
 * <td>NBA103</td>
 * <td>Version 4</td>
 * <td>Logging</td>
 * </tr>
 * <tr>
 * <td>SPR2594</td>
 * <td>Version 5</td>
 * <td>Portal process error stops if SourceInfo not present on incoming 103 XML</td>
 * </tr>
 * <tr>
 * <td>SPR2380</td>
 * <td>Version 5</td>
 * <td>General source code clean up</td>
 * </tr>
 * <tr>
 * <td>SPR2602</td>
 * <td>Version 5</td>
 * <td>APPORTAL error Stops when the Ripped 103 XML is not well formed.</td>
 * </tr>
 * <tr>
 * <td>SPR2968</td>
 * <td>Version 6</td>
 * <td>Test web service should determine XML file name dynamically</td>
 * </tr>
 * <tr>
 * <td>NBA187</td>
 * <td>Version 7</td>
 * <td>nbA Trial Application Project</td>
 * </tr>
 * <tr>
 * <td>NBA213</td>
 * <td>Version 7</td>
 * <td>Unified User Interface</td>
 * </tr>
 * <tr>
 * <td>SPR3362</td>
 * <td>Version 7</td>
 * <td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td>
 * </tr>
 * <tr>
 * <td>SPR3349</td>
 * <td>Version 7</td>
 * <td>Address Line 1 Incorrect in nbA Pending Contract Database After Client Search</td>
 * </tr>
 * <tr>
 * <td>SR515492</td>
 * <td>Discretionary</td>
 * <td>E-App Integration</td>
 * </tr>
 * <tr>
 * <td>SR641590</td>
 * <td>Sub-BGA</td>
 * <td>Sub BGA Enhancements</td>
 * </tr>
 * <tr>
 * <td>APSL2735</td>
 * <td>Discretionary</td>
 * <td>Electronic Initial Premium</td>
 * </tr>
 * <tr>
 * <td>APSL3474(APSL3258)</td>
 * <td>Discretionary</td>
 * <td>FATCA</td>
 * </tr>
 * <tr>
 * <td>APSL3447</td>
 * <td>Discretionary</td>
 * <td>HVT</td>
 * </tr>
 * <tr>
 * <td>APSL4057</td>
 * <td>Discretionary</td>
 * <td>SIUL</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcPortal extends NbaAutomatedProcess {
	/** Holds list of validation errors. */
	protected List validationErrors = new Vector();

	// Begin SR515492
	private static final String RETRIEVE_AGENT_SEARCH_BP = "RetrieveAgentSearchTxLifeBP";

	private static final String RETRIEVE_AGENT_DEMOGRAPHICS_BP = "RetrieveAgentDemographicsTxLifeBP";
	
	protected static final String NO_LOGGER = "NbaProcPortal could not get a logger from the factory.";

	protected static Map workTypeMap = new HashMap();
	static {
		workTypeMap.put(NbaConstants.A_ST_APPLICATION, NbaConstants.A_WT_APPLICATION);
		workTypeMap.put(NbaConstants.A_ST_MISC_MAIL, NbaConstants.A_WT_MISC_MAIL);
		workTypeMap.put(NbaConstants.A_ST_CWA_CHECK, NbaConstants.A_WT_CWA); // APSL2735
	}

	NbaXML103SubmitPolicyHelper submitPolicyHelper = new NbaXML103SubmitPolicyHelper();

	// End SR515492
	private String bgaOrSbgaRelationRoleCode = null; // SR641590 SUB-BGA

	/**
	 * NbaProcPortal constructor comment.
	 */
	public NbaProcPortal() {
		super();
	}

	/**
	 * This method validates XML against Breeze objects. The method, getValidationErrors(), is used to validate the XML against breeze. The validation
	 * errors are set to the member variable. Change the status to DTDERRORED and exit
	 */
	protected void doValidationAgainstDTD() {
		// Since the getValidationErrors() method returns XmlValidationError, create a
		// temporary vector and store all the error messages.
		Vector vctrErrors = getNbaTxLife().getTXLife().getValidationErrors(false);
		if (vctrErrors != null && vctrErrors.size() > 0) {
			Vector vcTemp = new Vector();
			for (int i = 0; i < vctrErrors.size(); i++) {
				XmlValidationError temp = (XmlValidationError) vctrErrors.get(i);
				vcTemp.add(temp.getErrorMessage());
			}
			setValidationErrors(vcTemp);
		}
	}

	/**
	 * Do business validation. If validation fails, add the errors as AWD comment. Change the status to INCAPPERRD and exit
	 */
	// NBA023 New Method (Renamed doBusinessValidation)
	protected void doXML103Validation() throws NbaBaseException {
		// NBA023 deleted
		// NBA023 begin
		NbaXML103Validation xml103Validation = new NbaXML103Validation();
		setValidationErrors(xml103Validation.doXML103Validation(getWork().getNbaCase()));
		// NBA023 end
	}

	/**
	 * This method does the following - perform validation of the XML source against DTD. - perform business validation - write validation errors as
	 * AWD comments - if there are no validation errors, updates the LOBs from XML - update the status.
	 * 
	 * @param user
	 *            the user for whom the process is being executed
	 * @param work
	 *            a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws com.csc.fsg.nba.exception.NbaBaseException {
		// Initialize
		if (!initializeFields(user, work)) {
			return statusProcessFailed();
		}
		// Check if XML103 source is available
		List errLst = new ArrayList();
		try {
			if (getXML103() == null) {
				errLst.add(NbaBaseException.XML_SOURCE); // No source. move this case to error queue with proper message
			} else {
				setNbaTxLife(getXML103());
			}
		} catch (NbaParseException nbPEx) {
			errLst.add(nbPEx.getMessage()); // set the encapsulated message to the error list
		}
		if (errLst.size() > 0) {
			setValidationErrors(errLst);
			writeErrorsInAWD();
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, formatErrorText(), getHostErrorStatus()));
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			return getResult();
		}
		// Set the portal indicator to true
		getWork().getNbaLob().setPortalCreated(true);
		translateNbaValues();
		setExchange1035Ind(getNbaTxLife()); // APSL2883
		if (!updateXmlAndLob()) {
			writeErrorsInAWD();
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, formatErrorText(), getFailStatus()));
			}
		} else {
			// Begin SR515492
			// Saving Indexing LOB Data
			updateTxLife(getNbaTxLife()); // QC8410(APSL2011)
			// setAgentData();
			commitIndexingData();
			// SR515492 code deleted
			// Call the routine to update Lob fields
			getWork().updateXML103Source(getNbaTxLife()); // APSL3361
			updateLobFromNbaTxLife(getNbaTxLife());
			// End SR515492
			// code deleted for PERM E-App Changes APSL2296
						
		}
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		// Return the result
		return getResult();
	}

	// NBA103, APSL2808 changed method visibility
	protected String formatErrorText() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = validationErrors.iterator(); iter.hasNext();) {
			if (sb.length() > 0) {
				sb.append("~");
			}
			sb.append(iter.next());
		}
		return sb.toString();
	}

	/**
	 * Returns the validation errors
	 * 
	 * @return java.util.List
	 */
	protected List getValidationErrors() {
		return validationErrors;
	}

	/**
	 * Retrieve and lock the child sources. Perform default initialization and initialize miscellaneous fields.
	 * 
	 * @param newUser
	 *            the AWD User for the process
	 * @param newWork
	 *            the NbaDst value object to be processed
	 * @return boolean
	 */
	protected boolean initializeFields(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException, NbaNetServerDataNotFoundException,
			NbaNetServerException {
		NbaDst expandedWork;
		// Retrieve and lock the child sources.
		NbaAwdRetrieveOptionsVO aNbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
		aNbaAwdRetrieveOptionsVO.setWorkItem(newWork.getID(), true);
		aNbaAwdRetrieveOptionsVO.requestSources();
		aNbaAwdRetrieveOptionsVO.setLockWorkItem();
		expandedWork = retrieveWorkItem(newUser, aNbaAwdRetrieveOptionsVO); // NBA213
		// NBA213 deleted code
		// Now continue with the expanded NbaDst
		return super.initialize(newUser, expandedWork);
	}

	/**
	 * Sets the backend system Id
	 * 
	 */
	protected void setBackEndSystemIdInXml(String strSystemId) {
		// begin SPR2594
		OLifE olife = getNbaTxLife().getOLifE();
		if (!olife.hasSourceInfo()) {
			olife.setSourceInfo(new SourceInfo());
		}
		olife.getSourceInfo().setFileControlID(strSystemId);
		olife.getSourceInfo().setSourceInfoName("nbA_Life");// SR515492
		// end SPR2594
	}

	/**
	 * Sets the portal indicator to True to denote that the case came via portal
	 * 
	 */
	protected void setPortalIndicator() {
		getWork().getNbaLob().setPortalCreated(true);
	}

	/**
	 * Sets the ProductTypeSubType to the member variable
	 * 
	 * @param strProductType
	 * 
	 */
	protected void setProductTypeInXml(String strProductType) {
		if (getNbaTxLife().getPrimaryHolding().hasPolicy()) {
			getNbaTxLife().getPrimaryHolding().getPolicy().setProductType(strProductType);
		}
	}

	/**
	 * Sets the validation errors to the list
	 * 
	 * @param errors
	 *            List of validation errors
	 */
	protected void setValidationErrors(java.util.List errors) {
		validationErrors = errors;
	}

	/**
	 * Retrieves the BackEndSystemId and ProductTypeSubType values from Plan table and updates the Lob Fields and the XML103 souurce with these values
	 */
	protected boolean updateXmlAndLob() throws NbaBaseException {
		try {
			NbaTableAccessor nta = new NbaTableAccessor();
			Map myCaseMap = nta.setupTableMap(getWork());
			// get the plan from the xml
			String productCode = null;
			String strCompany = null;
			List err = null;
			Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
			NbaPlansData planData = null;
			if (policy != null) {
				NbaTable nbaTable = new NbaTable();
				productCode = policy.getProductCode();
				strCompany = policy.getCarrierCode();
				if (strCompany != null) {
					myCaseMap.put("company", strCompany);
					myCaseMap.put("plan", productCode);
					planData = nta.getPlanData(myCaseMap);
					if (planData == null) {
						productCode = getDefaultPermPlanCode();
						myCaseMap.put("company", strCompany);
						myCaseMap.put("plan", productCode);
						planData = nta.getPlanData(myCaseMap);
						policy.setProductCode(productCode);
					}
				}
				if (planData != null) {
					policy.setProductCode(planData.getCoverageKey());
					String strBESystemId = planData.getSystemId();
					String strProductTypeSubType = planData.getProductType();
					// Call methods to update Lobs
					getWork().getNbaLob().setBackendSystem(strBESystemId);
					getWork().getNbaLob().setProductTypSubtyp(strProductTypeSubType);
					// Call methods to update xml
					setBackEndSystemIdInXml(strBESystemId);
					setProductTypeInXml(strProductTypeSubType);
					// Update xml103 source
					getWork().updateXML103Source(getNbaTxLife());
				} else {
					err = new ArrayList();
					err.add(0, "There is no record for the company: " + strCompany + " and ProductCode: " + productCode);
					setValidationErrors(err);
					return false;
				}
			}
			return true;
		} catch (Exception be) {
			throw new NbaBaseException("Could not update SystemId and ProductType");
		}
	}

	protected String getDefaultPermPlanCode() {
		return PLAN_ULBASE;
	}

	/**
	 * This method writes the validations errors as comments in AWD.
	 * 
	 */
	protected void writeErrorsInAWD() {
		addComments(getValidationErrors());
	}

	/**
	 * This method calls the Client Search webservice and gets the details for existing clients
	 * 
	 * @param work
	 *            a DST value object for which the process is to occur
	 * @param user
	 *            the user for whom the process is being executed
	 * @param parties
	 *            Contains list of parties for which the Client details are to be searched in host and added to NbaTxLife.
	 * @param configCategoryValue
	 *            One of the parameters required by VPMS to create Search criterion.
	 * @param backendSystem
	 *            Host on which the search is to be performed.
	 * @throws NbaVpmsException
	 * 
	 */
	// Begin Nba067
	// SPR3362 changed method signature
	protected void getClientId(NbaUserVO user, java.util.List parties, String configCategoryValue, String backendSystem) throws NbaVpmsException {
		String partyType = "";
		NbaTXLife txLifeClientSearchResp = null;
		NbaVpmsAdaptor vpmsProxy = null; // SPR3362
		for (int p = 0; p < parties.size(); p++) { // SPR3362
			try { // SPR3362
				Party party = (Party) parties.get(p);
				partyType = String.valueOf(party.getPartyTypeCode());
				if (party.hasPersonOrOrganization()) {
					if (party.getPersonOrOrganization().getPerson() != null) {
						partyType = "1";
					}
					if (party.getPersonOrOrganization().getOrganization() != null) {
						partyType = "2";
					}
					NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
					Map deOink = new HashMap();
					deOink.put(NbaVpmsConstants.A_PARTY_TYPE, partyType); // ACN012
					deOink.put(NbaVpmsConstants.A_INTEGRATED_CLIENT_SYSTEM, configCategoryValue); // ACN012
					vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CLIENT_SEARCH); // SPR3362
					vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_SEARCH_KEYS); // ACN012
					vpmsProxy.setSkipAttributesMap(deOink);
					NbaVpmsClientSearchData data = new NbaVpmsClientSearchData(vpmsProxy.getResults());
					// SPR3362 code deleted
					/*
					 * CALL the Client Search Web Service and retrieve the Response from it
					 */
					try {
						NbaClientSearchTransaction clientSearch = new NbaClientSearchTransaction();
						NbaTXLife xmlTransaction = clientSearch.createTXLife301(user, party, data, partyType);
						NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(backendSystem, "Client", "ClntSearch");
						txLifeClientSearchResp = service.invokeWebService(xmlTransaction); // SPR2380 SPR2968
					} catch (Exception e) {
						getLogger().logException("Exception in Client Search process", e);
					}
					if (txLifeClientSearchResp != null
							&& txLifeClientSearchResp.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseCount() > 0) {
						TXLifeResponse aTXLifeResponse = txLifeClientSearchResp.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify()
								.getTXLifeResponseAt(0);
						if ((aTXLifeResponse.getTransResult().getResultCode() != 1) || (aTXLifeResponse.getTransResult().getResultInfoCount() > 0)) {
							continue;
						}
						Party csParty = aTXLifeResponse.getOLifE().getPartyAt(0);
						if (csParty.hasGovtID()) {
							party.setGovtID(csParty.getGovtID());
						}
						if (csParty.hasGovtIDTC()) {
							party.setGovtIDTC(csParty.getGovtIDTC());
						}
						if (csParty.hasPartyKey()) {
							party.setPartyKey(csParty.getPartyKey());
						}
						if (partyType == "1") {
							party.getPersonOrOrganization().getPerson().setLastName(csParty.getPersonOrOrganization().getPerson().getLastName());
							party.getPersonOrOrganization().getPerson().setFirstName(csParty.getPersonOrOrganization().getPerson().getFirstName());
							party.getPersonOrOrganization().getPerson().setMiddleName(csParty.getPersonOrOrganization().getPerson().getMiddleName());
							party.getPersonOrOrganization().getPerson().setBirthDate(csParty.getPersonOrOrganization().getPerson().getBirthDate());
						} else {
							party.getPersonOrOrganization().getOrganization().setDBA(party.getPersonOrOrganization().getOrganization().getDBA());
						}
						java.util.List newAddresses = csParty.getAddress();
						java.util.List oldAddresses = party.getAddress();
						if (newAddresses.size() > 0) {
							for (int a = 0; a < newAddresses.size(); a++) {
								Address address = (Address) newAddresses.get(a);
								if (a < oldAddresses.size() && oldAddresses.size() != 0) {
									if (address.hasAddressTypeCode()) {
										party.getAddressAt(a).setAddressTypeCode(address.getAddressTypeCode());
									}
									if (address.hasLine1()) {
										party.getAddressAt(a).setLine1(address.getLine1());
									}
									if (address.hasLine2()) {
										party.getAddressAt(a).setLine2(address.getLine2());// SPR3349
									}
									if (address.hasLine3()) {
										party.getAddressAt(a).setLine3(address.getLine3());// SPR3349
									}
									if (address.hasCity()) {
										party.getAddressAt(a).setCity(address.getCity());
									}
									if (address.hasAddressState()) {
										party.getAddressAt(a).setAddressState(address.getAddressState());
									}
									if (address.hasZip()) {
										party.getAddressAt(a).setZip(address.getZip());
									}
									if (address.hasAddressCountry()) {
										party.getAddressAt(a).setAddressCountry(address.getAddressCountry());
									}
								} else {
									Address newAddress = new Address();
									if (address.hasAddressTypeCode()) {
										newAddress.setAddressTypeCode(address.getAddressTypeCode());
									}
									if (address.hasLine1()) {
										newAddress.setLine1(address.getLine1());
									}
									if (address.hasLine2()) {
										newAddress.setLine2(address.getLine2());
									}
									if (address.hasLine3()) {
										newAddress.setLine3(address.getLine3());
									}
									if (address.hasCity()) {
										newAddress.setCity(address.getCity());
									}
									if (address.hasAddressState()) {
										newAddress.setAddressState(address.getAddressState());
									}
									if (address.hasZip()) {
										newAddress.setZip(address.getZip());
									}
									if (address.hasAddressCountry()) {
										newAddress.setAddressCountry(address.getAddressCountry());
									}
									party.addAddress(newAddress);
								}
							}
						}
						java.util.List newPhones = csParty.getPhone();
						java.util.List oldPhones = party.getPhone();
						if (newPhones.size() > 0) {
							for (int a = 0; a < newPhones.size(); a++) {
								Phone phone = (Phone) newPhones.get(a);
								if (a < oldPhones.size() && oldPhones.size() != 0) {
									if (phone.hasAreaCode()) {
										party.getPhoneAt(a).setAreaCode(phone.getAreaCode());
									}
									if (phone.hasDialNumber()) {
										party.getPhoneAt(a).setDialNumber(phone.getDialNumber());
									}
									if (phone.hasPhoneTypeCode()) {
										party.getPhoneAt(a).setPhoneTypeCode(phone.getPhoneTypeCode());
									}
								} else {
									Phone newPhone = new Phone();
									if (phone.hasAreaCode()) {
										newPhone.setAreaCode(phone.getAreaCode());
									}
									if (phone.hasDialNumber()) {
										newPhone.setDialNumber(phone.getDialNumber());
									}
									if (phone.hasPhoneTypeCode()) {
										newPhone.setPhoneTypeCode(phone.getPhoneTypeCode());
									}
									party.addPhone(newPhone);
								}
							}
						}
						java.util.List newEmails = csParty.getEMailAddress();
						java.util.List oldEmails = party.getEMailAddress();
						if (newEmails.size() > 0) {
							for (int a = 0; a < newEmails.size(); a++) {
								EMailAddress email = (EMailAddress) newEmails.get(a);
								if (a < oldEmails.size() && oldEmails.size() != 0) {
									if (email.hasEMailType()) {
										party.getEMailAddressAt(a).setEMailType(email.getEMailType());
									}
									if (email.hasAddrLine()) {
										party.getEMailAddressAt(a).setAddrLine(email.getAddrLine());
									}
								} else {
									EMailAddress newEmail = new EMailAddress();
									if (email.hasEMailType()) {
										newEmail.setEMailType(email.getEMailType());
									}
									if (email.hasAddrLine()) {
										newEmail.setAddrLine(email.getAddrLine());
									}
									party.addEMailAddress(newEmail);
								}
							}
						}
					}
				}
			} catch (java.rmi.RemoteException re) {
			} catch (NbaBaseException e) { // NBA103
				NbaLogFactory.getLogger(this.getClass()).logException(e);
				// begin SPR3362
			} finally {
				if (vpmsProxy != null) {
					try {
						vpmsProxy.remove();
					} catch (RemoteException re) {
						getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
					}
				}
			}
		}
		// end SPR3362
	}

	/**
	 * Performs presubmit validation default value processing to the work item.
	 * 
	 * @param work
	 * @return
	 */
	// NBA187 New Method
	protected void setXMLPresubmitDefaultValues() throws NbaBaseException {
		NbaXML103SetDefaultValues xmlDefaults = new NbaXML103SetDefaultValues();
		getWork().setNbaUserVO(getUser());
		setWork(xmlDefaults.setPresubmitDefaultValues(getWork()));
	}

	// SR515492 New Method
	protected void setAgentData(NbaLob sourceLob) throws NbaBaseException {
		// code deleted APSL3874
		sourceLob.setMarketingInd(Long.toString(NbaUtils.getMarketingIndicatorCode(getNbaTxLife().getDistributionChannelName())));//APSL4507
		NbaTXLife agentTxLife = retrieveAgentInformation();
		if (agentTxLife != null) {
			setAgentSearchData(sourceLob, agentTxLife);
			NbaTXLife agentDemographicsTxLife = retrieveAgentDemographics();
			setAgentDemographicsData(sourceLob, agentDemographicsTxLife);
		}
		// code deleted APSL3874
	}

	// SR515492 New Method
	protected NbaTXLife retrieveAgentInformation() throws NbaBaseException {
		NbaTXLife txLife = getNbaTxLife();
		NbaDst nbaDst = getWork();
		Map input = new HashMap();
		input.put("TxLife", txLife);
		input.put("NbaDst", nbaDst);

		nbaDst.setNbaUserVO(user);
		AccelResult accelResult = (AccelResult) ServiceHandler.invoke(RETRIEVE_AGENT_SEARCH_BP, ServiceContext.currentContext(), input);
		if (accelResult.hasErrors()) {
			try {// APSL3874
				WorkflowServiceHelper.checkOutcome(accelResult);
			} catch (NbaBaseException ex) {// APSL3874
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, ex.getMessage());// APSL3874
			}// APSL3874
		} else {
			return (NbaTXLife) accelResult.getFirst();
		}
		return null;
	}

	protected NbaTXLife retrieveAgentDemographics() throws NbaBaseException {
		NbaTXLife txLife = getNbaTxLife();
		NbaDst nbaDst = getWork();
		Party agentParty = null;
		String relationRoleCode = null;
		if (txLife.getWritingAgent() != null) {
			NbaParty writingAgent = txLife.getWritingAgent();
			agentParty = writingAgent.getParty();
			relationRoleCode = String.valueOf(NbaOliConstants.OLI_REL_PRIMAGENT);
		} else if (!NbaUtils.isBlankOrNull(txLife.getPartyId(NbaOliConstants.OLI_REL_ADDWRITINGAGENT, true))) {
			NbaParty addlWritingAgent = txLife.getParty(txLife.getPartyId(NbaOliConstants.OLI_REL_ADDWRITINGAGENT, true));
			agentParty = addlWritingAgent.getParty();
			relationRoleCode = String.valueOf(NbaOliConstants.OLI_REL_ADDWRITINGAGENT);
		} else if (txLife.isWholeSale()) { // begin SR641590 SUB-BGA
			NbaParty bgaOrSbgaParty = null;
			bgaOrSbgaParty = txLife.getParty(txLife.getPartyId(NbaOliConstants.OLI_REL_SUBORDAGENT, true));
			if (bgaOrSbgaParty == null) {
				bgaOrSbgaParty = txLife.getParty(txLife.getPartyId(NbaOliConstants.OLI_REL_GENAGENT, true));
			}
			agentParty = bgaOrSbgaParty.getParty();
			relationRoleCode = getBgaOrSbgaRelationRoleCode();
		}
		// End SR641590 SUB-BGA
		AxaProducerVO producer = createAXAProducerSearchVO(agentParty, relationRoleCode, getWork().getNbaLob());
		AxaAgentVO agentVO = new AxaAgentVO(producer);
		agentVO.setNbaUserVO(getUser());
		nbaDst.setNbaUserVO(user);
		AccelResult accelResult = (AccelResult) ServiceHandler.invoke(RETRIEVE_AGENT_DEMOGRAPHICS_BP, ServiceContext.currentContext(), agentVO);
		if (accelResult.hasErrors()) {
			try {// APSL3874
				WorkflowServiceHelper.checkOutcome(accelResult);
			} catch (NbaBaseException ex) {// APSL3874
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, ex.getMessage());// APSL3874
			}// APSL3874
		} else {
			return (NbaTXLife) accelResult.getFirst();
		}
		return null;
	}

	public AxaProducerVO createAXAProducerSearchVO(Party aParty, String relationRoleCode, NbaLob nbaLob) throws NbaBaseException {
		AxaProducerVO producerVO = new AxaProducerVO();
		String distChannel = Long.toString(nbaLob.getDistChannel());
		producerVO.setTransType(NbaOliConstants.TC_TYPE_PARTYSRCH);
		producerVO.setOperator(NbaOliConstants.LOGICAL_OPERATOR_AND);
		producerVO.setTransSubType(NbaOliConstants.TC_SUBTYPE_PRODUCER_SEARCH);
		if (!NbaUtils.isBlankOrNull(aParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID())) {
			producerVO.addCriteria(NbaOliConstants.OLI_CARRIERAPPOINTMENT, CarrierAppointment.$COMPANY_PRODUCER_ID, aParty.getProducer()
					.getCarrierAppointmentAt(0).getCompanyProducerID(), NbaOliConstants.OLI_OP_EQUAL);
		}
		if (!NbaUtils.isBlankOrNull(distChannel)) {
			producerVO.addCriteria(NbaOliConstants.OLI_DISTRIBUTIONCHANNELINFO, DistributionChannelInfo.$DISTRIBUTION_CHANNEL, distChannel,
					NbaOliConstants.OLI_OP_EQUAL);
		}
		producerVO.addCriteria(NbaOliConstants.OLI_RELATION, Relation.$RELATION_ROLE_CODE, String.valueOf(relationRoleCode),
				NbaOliConstants.OLI_OP_EQUAL);

		producerVO.setPartyTypeCode(String.valueOf(aParty.getPartyTypeCode()));
		if (aParty.getProducer().getCarrierAppointmentCount() > 0) {
			producerVO.setCompanyProducerID(aParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID());
			producerVO.setCarrierApptTypeCode(String.valueOf(aParty.getProducer().getCarrierAppointmentAt(0).getCarrierApptTypeCode()));
		}

		producerVO.setOperator(NbaOliConstants.OLI_OPER_AND);
		producerVO.setCarriearAptSysKey(new ArrayList());
		producerVO.setRelationRoleCode(String.valueOf(relationRoleCode));
		producerVO.setDistributionChannel(String.valueOf(nbaLob.getDistChannel()));
		return producerVO;
	}

	protected void setAgentDemographicsData(NbaLob sourceLob, NbaTXLife agentTxLife) {
		Party bgParty = null;
		Party agParty = null;
		String agentPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_PRIMAGENT);
		if (agentPartyId == null) {
			Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
			if (relation != null) {
				agentPartyId = relation.getOriginatingObjectID();
			}

		}
		NbaParty agentParty = agentTxLife.getParty(agentPartyId);
		if (agentParty != null) {
			agParty = agentParty.getParty();

			if (getNbaTxLife().isWholeSale()) {
				if (!NbaUtils.isBlankOrNull(agentParty.getDisplayName())) {
					sourceLob.setWritingAgency(agentParty.getDisplayName());
				} else if (!NbaUtils.isBlankOrNull(getAsuCodeNum(agParty))) {
					sourceLob.setWritingAgency(getAsuCodeNum(agParty));
				} else {
					sourceLob.deleteWritingAgency();
				}
			} else { // ALII1949
				if (!NbaUtils.isBlankOrNull(getAsuCode(agParty))) {
					sourceLob.setWritingAgency(getAsuCode(agParty));
				} else {
					sourceLob.deleteWritingAgency();
				}
			}
			Producer producer = agParty.getProducer();
			if (producer != null && producer.getCarrierAppointmentCount() > 0) {
				CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
				if (carrierAppointment != null) {
					CarrierAppointmentExtension carrierAppointmentExtn = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
					if (carrierAppointmentExtn != null && carrierAppointmentExtn.hasProducerDesignation()) {
						sourceLob.setAgentDesignation(String.valueOf(carrierAppointmentExtn.getProducerDesignation()));
					} else {
						sourceLob.setAgentDesignation(EMPTY_STRING);
					}
				}
			}
		}

		if (getNbaTxLife().isWholeSale()) {

			String bgaPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_GENAGENT);
			if (bgaPartyId == null) {
				Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
				if (relation != null) {
					bgaPartyId = relation.getOriginatingObjectID();
				}

			}
			if (bgaPartyId != null) {
				NbaParty bgaParty = agentTxLife.getParty(bgaPartyId);
				bgParty = bgaParty.getParty();
			}

			if (bgParty != null && bgParty.getProducer().getCarrierAppointmentCount() > 0) {
				CarrierAppointment carrier = bgParty.getProducer().getCarrierAppointmentAt(0);
				if (carrier != null && carrier.getOLifEExtensionCount() > 0) {
					CarrierAppointmentExtension carExtn = carrier.getOLifEExtensionAt(0).getCarrierAppointmentExtension();
					if (carExtn != null && NbaUtils.isBlankOrNull(carExtn.getBGAUWTeam())) {
						sourceLob.deleteBGAUWTeam();
					} else {
						sourceLob.setBGAUWTeam(carExtn.getBGAUWTeam());
					}
				}
			}

			String superiorPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_SUPERIORAGENT);
			if (superiorPartyId != null) {
				NbaParty superiorParty = agentTxLife.getParty(superiorPartyId);
				Party supParty = superiorParty.getParty();
				String superiorId = getAgentId(supParty);

				if (NbaUtils.isBlankOrNull(superiorId)) {
					sourceLob.deleteSuperBGANmbr();
				} else {
					sourceLob.setSuperBGANmbr(superiorId);
				}
			}
		}
	}

	protected void setAgentSearchData(NbaLob sourceLob, NbaTXLife agentTxLife) {
		String brokerGeneralAgency = null;
		String contractFirmAgency = null;
		String processingFirmAgency = null;
		String submittingFirmAgency = null;
		Party bgParty = null;
		Party agParty = null;
		// begin SR641590 SUB-BGA
		String subBrokerGeneralAgency = null;
		Party sBgParty = null;
		// End SR641590 SUB-BGA
		String agentPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_PRIMAGENT);
		if (agentPartyId == null) {
			Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
			if (relation != null) {
				agentPartyId = relation.getOriginatingObjectID();
			}

		}
		NbaParty agentParty = agentTxLife.getParty(agentPartyId);
		if (agentParty != null) {
			agParty = agentParty.getParty();
		}

		if (agentParty != null && !NbaUtils.isBlankOrNull(getAgentId(agentParty.getParty()))) {
			String agentId = getAgentId(agentParty.getParty());
			sourceLob.setAgentID(agentId);
			// Begin QC8346 (APSL1950)
			// UPdate the agent id in TxLife from the Agent Search Response.
			NbaParty agtParty = getNbaTxLife().getWritingAgent();
			if (agtParty.getParty() != null && agtParty.getParty().hasProducer()) {
				List alCarrierAppointment = agtParty.getParty().getProducer().getCarrierAppointment();
				if (!alCarrierAppointment.isEmpty()) {
					CarrierAppointment objCarrierAppointment = (CarrierAppointment) alCarrierAppointment.get(0);
					objCarrierAppointment.setCompanyProducerID(sourceLob.getAgentID());
				}
			}
			// End QC8346(APSL1950)
		}

		if (getNbaTxLife().isWholeSale()) {
			// begin SR641590 SUB-BGA
			String sBgaPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_SUBORDAGENT);
			if (sBgaPartyId == null) {
				Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_SUBORDAGENT);
				if (relation != null) {
					sBgaPartyId = relation.getOriginatingObjectID();
				}
			}
			if (sBgaPartyId != null) {
				NbaParty sbgaParty = agentTxLife.getParty(sBgaPartyId);
				sBgParty = sbgaParty.getParty();
				subBrokerGeneralAgency = getAgentId(sBgParty);
				if (agentParty == null && !NbaUtils.isBlankOrNull(subBrokerGeneralAgency)) {
					sourceLob.setAgentID(subBrokerGeneralAgency);
				}
				setBgaOrSbgaRelationRoleCode(String.valueOf(NbaOliConstants.OLI_REL_SUBORDAGENT));// SR641590 SUB-BGA
			}
			// End SR641590 SUB-BGA
			String bgaPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_GENAGENT);
			if (bgaPartyId == null) {
				Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
				if (relation != null) {
					bgaPartyId = relation.getOriginatingObjectID();
				}
			}
			if (bgaPartyId != null) {
				NbaParty bgaParty = agentTxLife.getParty(bgaPartyId);
				bgParty = bgaParty.getParty();
				brokerGeneralAgency = getAgentId(bgParty);
				// SR641590 SUB-BGA
				if (agentParty == null) {
					if (!NbaUtils.isBlankOrNull(subBrokerGeneralAgency)) {
						sourceLob.setAgentID(subBrokerGeneralAgency);
					} else if (!NbaUtils.isBlankOrNull(brokerGeneralAgency)) {
						sourceLob.setAgentID(brokerGeneralAgency);
					}
				}
				if (NbaUtils.isBlankOrNull(getBgaOrSbgaRelationRoleCode())) {
					setBgaOrSbgaRelationRoleCode(String.valueOf(NbaOliConstants.OLI_REL_GENAGENT));
				}
				// End SR641590 SUB-BGA
			}

			// begin APSL3447
			String contractPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_CONTRACTINGFIRM);
			if (contractPartyId == null) {
				Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_CONTRACTINGFIRM);
				if (relation != null) {
					contractPartyId = relation.getOriginatingObjectID();
				}
			}
			if (contractPartyId != null) {
				NbaParty contractParty = agentTxLife.getParty(contractPartyId);
				Party contractFirmParty = contractParty.getParty();
				contractFirmAgency = getAgentId(contractFirmParty);
			}

			String processPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_PROCESSINGFIRM);
			if (processPartyId == null) {
				Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PROCESSINGFIRM);
				if (relation != null) {
					processPartyId = relation.getOriginatingObjectID();
				}
			}
			if (processPartyId != null) {
				NbaParty processParty = agentTxLife.getParty(processPartyId);
				Party processFirmParty = processParty.getParty();
				processingFirmAgency = getAgentId(processFirmParty);
			}

			String submitPartyId = agentTxLife.getPartyId(NbaOliConstants.OLI_REL_SUBMITTINGFIRM);
			if (submitPartyId == null) {
				Relation relation = agentTxLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_SUBMITTINGFIRM);
				if (relation != null) {
					submitPartyId = relation.getOriginatingObjectID();
				}
			}
			if (submitPartyId != null) {
				NbaParty submitParty = agentTxLife.getParty(submitPartyId);
				Party submitFirmParty = submitParty.getParty();
				submittingFirmAgency = getAgentId(submitFirmParty);
				if (agentParty == null && !NbaUtils.isBlankOrNull(submittingFirmAgency)) {
					sourceLob.setAgentID(submittingFirmAgency);
				}
			}

			Relation primaryPartyRelation = getPrimaryHVTPartyRelation(agentTxLife);
			if (primaryPartyRelation != null && primaryPartyRelation.hasRelationRoleCode()) {
				setBgaOrSbgaRelationRoleCode(String.valueOf(primaryPartyRelation.getRelationRoleCode()));
			}
			// End APSL3447

		}

		// Start APSL3447
		if (NbaUtils.isBlankOrNull(contractFirmAgency)) {
			sourceLob.deleteContractId();
		} else {
			sourceLob.setContractId(contractFirmAgency);
		}
		if (NbaUtils.isBlankOrNull(processingFirmAgency)) { // ALS4598
			sourceLob.deleteProcessingFirmId();
		} else {
			sourceLob.setProcessingFirmId(processingFirmAgency);
		}
		if (NbaUtils.isBlankOrNull(submittingFirmAgency)) { // ALS4598
			sourceLob.deleteSubmittingFirmId();
		} else {
			sourceLob.setSubmittingFirmId(submittingFirmAgency);
		}
		// End APSL3447

		if (NbaUtils.isBlankOrNull(brokerGeneralAgency)) { // ALS4598
			sourceLob.deleteBrokerGeneralAgency();
		} else {
			sourceLob.setBrokerGeneralAgency(brokerGeneralAgency);
		}

		// begin SR641590 SUB-BGA
		if (NbaUtils.isBlankOrNull(subBrokerGeneralAgency)) {
			sourceLob.deleteSubBrokerGeneralAgency();
		} else {
			sourceLob.setSubBrokerGeneralAgency(subBrokerGeneralAgency);
		}
		// End SR641590 SUB-BGA

		if (agParty != null) {
			if (NbaUtils.isBlankOrNull(getAsuCodeNum(agParty))) {
				sourceLob.deleteASUCode();
			} else {
				sourceLob.setASUCode(getAsuCodeNum(agParty));
			}
			Producer producer = agParty.getProducer();
			if (producer != null && producer.getCarrierAppointmentCount() > 0) {
				CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
				if (carrierAppointment != null) {
					CarrierAppointmentExtension carrierAppointmentExtn = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
					if (carrierAppointmentExtn != null && carrierAppointmentExtn.hasProducerDesignation()) {
						sourceLob.setAgentDesignation(String.valueOf(carrierAppointmentExtn.getProducerDesignation()));
					} else {
						sourceLob.setAgentDesignation(EMPTY_STRING);
					}
				}
			}
		} else if (bgParty != null) {
			if (NbaUtils.isBlankOrNull(getAsuCodeNum(bgParty))) {
				sourceLob.deleteASUCode();
			} else {
				sourceLob.setASUCode(getAsuCodeNum(bgParty));
			}
		}
	}

	protected String getAgentId(Party party) {
		Producer producer = party.getProducer();
		if (producer != null && producer.getCarrierAppointmentCount() > 0) {
			CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
			if (carrierAppointment != null) {
				return carrierAppointment.getCompanyProducerID();
			}
		}
		return null;
	}

	protected String getAsuCode(Party party) {
		Producer producer = party.getProducer();
		if (producer != null && producer.getCarrierAppointmentCount() > 0) {
			CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
			CarrierAppointmentExtension carrierAppointmentExtn = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
			if (carrierAppointmentExtn != null && carrierAppointmentExtn.hasASUCodeNum()) {
				return String.valueOf(carrierAppointmentExtn.getASUCode());// ALII1949
			}
		}
		return null;
	}

	/**
	 * Populates all the source lob's of the provided source types and updates them in the Dst It also calls Index VPMS Model to check for Minimum
	 * data validation and to get which all fields to be copied on the work dst.
	 * 
	 * @param indexTableBean
	 *            the indexTableBean
	 */
	protected void commitIndexingData() throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In commitIndexingData");
		}
		NbaLob sourceLob = null;
		NbaSource currentSource = null;
		NbaDst workDst = getWork();
		List nbaSources = workDst.getNbaSources();
		int sourceCount = nbaSources.size();
		boolean appSourceIndexed = false;// ALS5276
		for (int i = 0; i < sourceCount; i++) {
			currentSource = (NbaSource) nbaSources.get(i);
			if (!currentSource.isTextFormat()) {
				sourceLob = currentSource.getNbaLob();
				currentSource.setUpdate();
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("In commitIndexingData SourceType == "+currentSource.getSourceType()+" FormNumber >>> "+sourceLob.getFormNumber());
				}
				// remove all validation messages earlier if present for the current IndexVO
				if (currentSource.getSourceType().equalsIgnoreCase(A_ST_APPLICATION)) {
					// reset the sourceLOB with the updated TrialValues and update the source's field group values
					// APSL2816 if condition
					if (NbaUtils.isBlankOrNull(sourceLob.getFormNumber())) {
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Appliaction Form Number is missing",
								getFailStatus()));
						addComment("Application Form Number is missing");
					} else {
						sourceLob = populateApplicationSource(sourceLob); // NBA187
						appSourceIndexed = true;// ALS5276
						sourceLob.setFormRecivedWithAppInd(true);// ALS5276
						sourceLob.setReceiptDate(new Date());
						currentSource.getSource().setLobData(sourceLob.convertToLobData()); // NBA187
					}
					// end APSL2816
				} else if (currentSource.getSourceType().equalsIgnoreCase(A_ST_MISC_MAIL)) {
					populateMiscMailSource(sourceLob);
					sourceLob.setReceiptDate(new Date());
					sourceLob.setReqReceiptDate(new Date()); // APSL2888
					sourceLob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date())); // QC20240
				} else if (currentSource.getSourceType().equalsIgnoreCase(A_ST_CWA_CHECK)) { // APSL2735
					populateCWASource(sourceLob); // APSL2735
					sourceLob.setReceiptDate(new Date()); // APSL2735
				}
				if (appSourceIndexed) {
					sourceLob.setFormRecivedWithAppInd(true);// ALS5276
				}
				sourceLob.setCreateStation(NbaConstants.SCAN_STATION_ARCIVER);
				currentSource.getNbaLob().setPortalCreated(true);// APSL1794 APSL1954
				// check for minimum data validation
				checkMinimumDataValidation(currentSource);
				if (getResult() == null) {
					String workType = (String) workTypeMap.get(currentSource.getSource().getSourceType());
					// Get the primary source's worktype and if found call the copyLobsToWork method else copy
					// the common fields from the work to the source.
					if (!NbaConstants.A_WT_NBSBQTMAIL.equals(workDst.getWorkType())) { // APSL914 - Do not copy any Lob to and from NBSBQTMAIL
						// worktype
						// as this is just placeholder for subsequent miscmails and cwachecks
						if (workDst.getWorkType().equals(workType)) {
							copyLobsToWork(workDst.getWorkType(), currentSource, workDst.getNbaLob());
						} else {
							copyPolicyAndTaxIds(sourceLob, workDst.getNbaLob());
							if (!NbaConstants.A_WT_PAYMENT.equals(workType) && !NbaConstants.A_WT_CWA.equals(workType)) { // APSL2735 Added A_WT_CWA
								copyDemographicData(sourceLob, workDst.getNbaLob());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Populate Misc Mail source Lob from Index Row bean supplied
	 * 
	 * @param sourceLob
	 *            the NbaLob
	 * @param indexVO
	 *            the input index row bean
	 * @throws NbaBaseException
	 */
	protected void populateMiscMailSource(NbaLob sourceLob) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In populateMiscMailSource");
		}
		Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
		determineRequirementType(sourceLob);// QC8173 Moved up
		setPartyFields(sourceLob, getNbaTxLife());
		setCompanyPolicyAndPlanFields(sourceLob, policy);
		// APSL3361 begin
		if (NbaOliConstants.OLI_REQCODE_EPOLDELSUPP == sourceLob.getReqType()) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
			appInfoExt.setReqPolicyDeliverMethod(NbaOliConstants.OLI_POLDELMETHOD_EMAIL);
		}
		// APSL3361 end
		// updateLobsForJoint(sourceLob);
		processJointInsuredParty(sourceLob);//APSL4057
	}

	protected void determineRequirementType(NbaLob sourceLob) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("determineRequirementType");
		}
		if (sourceLob != null) {
			String reqType = retrieveNbaReqType(sourceLob);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("determineRequirementType reqType >> "+reqType);
			}
			if (reqType != null) {
				sourceLob.setReqType(Integer.parseInt(reqType));
			} else {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("In Else determineRequirementType calling processNonNbaForm");
				}
				processNonNbaForm(sourceLob);
			}
		}
	}

	/**
	 * Retrieves the Nba Requireemnt Type from NBA_FORMS_VALIDATION table.
	 * 
	 * @param currentSource
	 *            the NbaSource object
	 * @throws NbaBaseException
	 */
	protected String retrieveNbaReqType(NbaLob sourceLob) throws NbaBaseException {
		if (!NbaUtils.isBlankOrNull(sourceLob.getFormNumber())) {
			NbaTableAccessor nta = new NbaTableAccessor();
			NbaFormsValidationData tableData = nta.getFormNumberData(sourceLob.getFormNumber());
			if (tableData != null) {
				return tableData.getRequirementType();
			}
		}
		return null;
	}

	/**
	 * Calls VPMS model to get the Requirement Type for non Nba Forms
	 * 
	 * @param currentSource
	 *            the NbaSource object
	 * @throws NbaBaseException
	 */
	protected void processNonNbaForm(NbaLob sourceLob) throws NbaBaseException {
		if (sourceLob != null) {
			// Start APSL2466 (SR514766)
			Map deOinkMap = new HashMap(3, 1);
			Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
			if (!NbaUtils.isBlankOrNull(policy.getProductCode())) {
				String productCode = policy.getProductCode();
				deOinkMap.put("A_ProductCode", productCode);
			}
			if (policy.hasProductType()) {
				String productType = String.valueOf(policy.getProductType());
				deOinkMap.put("A_PRODUCTTYPSUBTYPLOB", productType);
			}
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
			if (policyExt != null) {
				if (policyExt.hasProductSuite()) {
					String productSuite = String.valueOf(policyExt.getProductSuite());
					deOinkMap.put("A_PLANTYPELOB", productSuite);
				}
			}
			// End APSL2466 (SR514766)
			String reqType = invokeVPMSModel(sourceLob, deOinkMap, NbaVpmsConstants.INDEX, EP_GET_REQ_TYPE); // APSL2466 (SR514766)
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("processNonNbaForm reqType >> "+reqType);
			}
			if (!NbaUtils.isBlankOrNull(reqType)) {
				sourceLob.setReqType(Integer.parseInt(reqType));
				if(!(reqType.equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_REQCODE_1009800038)) || reqType.equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_REQCODE_1009800039)))) { //NBLXA-2345
					sourceLob.deleteFormNumber();
				}
			}
		}
	}

	/**
	 * Calls VPMS model and returns the result
	 * 
	 * @param currentSource
	 *            the NbaSource object
	 * @throws NbaBaseException
	 */
	protected String invokeVPMSModel(NbaLob sourceLob, Map deOinkMap, String modelName, String entryPoint) throws NbaBaseException {
		NbaVpmsAdaptor vpmsAdaptor = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(sourceLob);
			vpmsAdaptor = new NbaVpmsAdaptor(data, modelName);
			vpmsAdaptor.setSkipAttributesMap(deOinkMap);
			vpmsAdaptor.setVpmsEntryPoint(entryPoint);
			// get the string out returned by VP / MS Model
			VpmsComputeResult rulesProxyResult = vpmsAdaptor.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List resultData = vpmsResultsData.getResultsData();
				if (!resultData.isEmpty()) {
					String returnStr = (String) resultData.get(0);
					return returnStr;
				}
			}
			return null;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (vpmsAdaptor != null) {
				try {
					vpmsAdaptor.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
	}

	/**
	 * Populate the Application Source Lob
	 * 
	 * @param sourceLob
	 *            the NbaLob
	 * @param indexVO
	 *            the input index row bean
	 * @return the updated nbaLOB
	 * @throws NbaBaseException
	 */
	protected NbaLob populateApplicationSource(NbaLob sourceLob) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In populateApplicationSource");
		}
		Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
		setPartyFields(sourceLob, getNbaTxLife());
		setCompanyPolicyAndPlanFields(sourceLob, policy);
		setApplicationInfoFields(sourceLob, policy.getApplicationInfo());
		setAgentData(sourceLob);
		if (getNbaTxLife().getLife() != null) {
			if (NbaUtils.isBlankOrNull(getNbaTxLife().getLife().getFaceAmt())) {
				sourceLob.deleteFaceAmount();
			} else {
				sourceLob.setFaceAmount(getNbaTxLife().getLife().getFaceAmt());
			}
		}
		String backendSystem = getNbaTxLife().getBackendSystem();
		if (backendSystem != null) {
			sourceLob.setBackendSystem(backendSystem);
		}
		if (policy.hasProductType()) {
			sourceLob.setProductTypSubtyp(String.valueOf(policy.getProductType()));
		}
		sourceLob.setFaxedOrEmailedInd(false);
		policy.getApplicationInfo().setHOAppFormNumber(sourceLob.getFormNumber());
		// APSL4507
		if (getNbaTxLife().getProductCode() != null && getNbaTxLife().getProductCode().trim().equalsIgnoreCase(PRODCODE_ADC)) {
			sourceLob.setAppProdType(APPPROD_TYPE_ADC);
			getWork().getNbaLob().setAppProdType(APPPROD_TYPE_ADC);
		}
		policy.getApplicationInfo().setActionUpdate();
		// APSL3361 Code deleted to update 103 xml from here
		return sourceLob;
	}

	/**
	 * Checks if Requirement Doctor Name is null or empty string, if so delete the Requirement Type LOB else set the value
	 * 
	 * @param sourceLob
	 *            the source NbaLOB
	 * @param indexVO
	 *            the index bean
	 */
	protected void setCompanyPolicyAndPlanFields(NbaLob sourceLob, Policy policy) {
		if (policy != null) {
			if (NbaUtils.isBlankOrNull(policy.getCarrierCode())) {
				sourceLob.deleteCompany();
			} else {
				sourceLob.setCompany(policy.getCarrierCode());
			}
			if (NbaUtils.isBlankOrNull(policy.getPolNumber())) {
				sourceLob.deletePolicyNumber();
			} else {
				sourceLob.setPolicyNumber(policy.getPolNumber());
			}
			if (NbaUtils.isBlankOrNull(policy.getProductCode())) {
				sourceLob.deletePlan();
			} else {
				sourceLob.setPlan(policy.getProductCode());
			}
		}
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExt != null) {
			long productSuite = policyExt.getProductSuite();
			if (NbaUtils.isBlankOrNull(productSuite)) {
				sourceLob.deletePlanType();
			} else {
				sourceLob.setPlanType(String.valueOf(productSuite));
			}
			//APSL4576 begin
			long salesChannel = policyExt.getSalesChannel();
			if (NbaUtils.isBlankOrNull(salesChannel)) {
				sourceLob.deleteSalesChannel();
			} else {
				sourceLob.setSalesChannel(String.valueOf(salesChannel));
			}
			//APSL4576 end
			// Begin NBLXA-1823
			sourceLob.setBusinessStrategyInd(policyExt.getBusinessStrategiesInd());
		   // End NBLXA-1823
		}
	}

	/**
	 * Checks if Requirement Doctor Name is null or empty string, if so delete the Requirement Type LOB else set the value
	 * 
	 * @param sourceLob
	 *            the source NbaLOB
	 * @param indexVO
	 *            the index bean
	 */
	protected void setApplicationInfoFields(NbaLob sourceLob, ApplicationInfo appInfo) {
		if (appInfo != null) {
			if (NbaUtils.isBlankOrNull(appInfo.getApplicationJurisdiction())) {
				sourceLob.deleteAppState();
			} else {
				sourceLob.setAppState(String.valueOf(appInfo.getApplicationJurisdiction()));
			}
			if (NbaUtils.isBlankOrNull(appInfo.getApplicationType())) {
				sourceLob.deleteApplicationType();
			} else {
				sourceLob.setApplicationType(String.valueOf(appInfo.getApplicationType()));
			}
			// Begin QC8282
			if (appInfo.getReplacementInd()) {
				// Indicate a replacemen based on application entry
				if (isExch1035IndCodePresent()) { // Start APSL2436
					sourceLob.setExchangeReplace(2);
					sourceLob.setReplacementIndicator(String.valueOf(2));
				} else {// End APSL2436
					sourceLob.setExchangeReplace(1);
					sourceLob.setReplacementIndicator(String.valueOf(1));
				}
			}
			// End QC8282
		}
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		if (appInfoExt != null) {
			if (NbaUtils.isBlankOrNull(appInfoExt.getApplicationOrigin())) {
				sourceLob.deleteAppOriginType();
			} else {
				sourceLob.setAppOriginType(appInfoExt.getApplicationOrigin());
			}
			if (NbaUtils.isBlankOrNull(appInfoExt.getSpecialCase())) {
				sourceLob.deleteSpecialCase();
			} else {
				sourceLob.setSpecialCase(String.valueOf(appInfoExt.getSpecialCase()));
			}
			// APSL4636 Begin
			if (appInfoExt.getCompanionCaseType() == NbaOliConstants.OLI_TC_NULL) {
				sourceLob.deleteCompanionType();
			} else {
				sourceLob.setCompanionType(String.valueOf(appInfoExt.getCompanionCaseType()));
			}
			// APSL4636 End
		}
		// set ApplicationSgned date LOB(SR515492)
		Date appSignedDate = getApplicationSignatureDate(appInfo);
		if (NbaUtils.isBlankOrNull(appSignedDate)) {
			sourceLob.deleteAppSignedDate();
		} else {
			sourceLob.setAppSignedDate(appSignedDate);
		}
	}

	/**
	 * check if Exch1035IndCode present in XML
	 */
	// APSL2436 new method
	protected boolean isExch1035IndCodePresent() {
		Relation relation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_HOLDINGCO);
		if (relation != null) {
			Holding compHolding = getNbaTxLife().getHolding(relation.getOriginatingObjectID());
			Policy policy = compHolding.getPolicy();
			if (policy != null) {
				if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null) {
					Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
					if (life != null) {
						LifeUSA lifeUSA = life.getLifeUSA();
						if (lifeUSA != null) {
							LifeUSAExtension lifeUSAExtn = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
							if (lifeUSAExtn != null) {
								if (NbaOliConstants.NBA_ANSWERS_YES == lifeUSAExtn.getExch1035IndCode()) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * get Application Signature Date from TxLife
	 * 
	 * @param ApplicationInfo
	 */
	// New method to get Application Signed date(SR515492)
	protected Date getApplicationSignatureDate(ApplicationInfo appInfo) {
		List signList = appInfo.getSignatureInfo();
		Iterator signListItr = signList.iterator();
		while (signListItr.hasNext()) {
			SignatureInfo aSignatureInfo = (SignatureInfo) signListItr.next();
			long signRoleCode = aSignatureInfo.getSignatureRoleCode(); // APSL2650
			/*if ((NbaOliConstants.OLI_PARTICROLE_PRIMARY == signRoleCode || (appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_CONVERSIONNEW && NbaOliConstants.OLI_PARTICROLE_OWNER == signRoleCode))
					&& NbaOliConstants.OLI_SIGTYPE_APPSIG == aSignatureInfo.getSignaturePurpose() && aSignatureInfo.hasSignatureDate()) { // APSL2650*/
			if ((NbaOliConstants.OLI_PARTICROLE_PRIMARY == signRoleCode || ((appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_CONVERSIONNEW || appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_CONVOPAIAD) && NbaOliConstants.OLI_PARTICROLE_OWNER == signRoleCode))
				&& NbaOliConstants.OLI_SIGTYPE_APPSIG == aSignatureInfo.getSignaturePurpose() && aSignatureInfo.hasSignatureDate()) { // APSL4568
					return aSignatureInfo.getSignatureDate();
			}
		}
		return null;
	}

	/**
	 * Checks if Requirement Doctor Name is null or empty string, if so delete the Requirement Type LOB else set the value
	 * 
	 * @param sourceLob
	 *            the source NbaLOB
	 * @param indexVO
	 *            the index bean
	 * @throws NbaBaseException
	 */
	protected void setPartyFields(NbaLob sourceLob, NbaTXLife txLife103) throws NbaBaseException {
		if (sourceLob != null) {
			NbaParty party = getIndexingParty(sourceLob.getReqType(), txLife103);// QC8173
			if (party != null) {
				Person person = party.getPerson();
				if (person != null) {
					if (NbaUtils.isBlankOrNull(person.getFirstName())) {
						sourceLob.deleteFirstName();
					} else {
						sourceLob.setFirstName(person.getFirstName());
					}
					if (NbaUtils.isBlankOrNull(person.getLastName())) {
						sourceLob.deleteLastName();
					} else {
						sourceLob.setLastName(person.getLastName());
					}
					if (NbaUtils.isBlankOrNull(person.getMiddleName())) {
						sourceLob.deleteMiddleInitial();
					} else {
						sourceLob.setMiddleInitial(person.getMiddleName());
					}

					if (NbaUtils.isBlankOrNull(person.getBirthDate())) {
						sourceLob.deleteDOB();
					} else {
						sourceLob.setDOB(person.getBirthDate());
					}
					if (NbaUtils.isBlankOrNull(person.getGender())) {
						sourceLob.deleteGender();
					} else {
						sourceLob.setGender(String.valueOf(person.getGender()));
					}
				} else if (party.isOrganization()) { // start APSL2174
					String fullName = party.getFullName();
					if (NbaUtils.isBlankOrNull(fullName)) {
						sourceLob.deleteLastName();
						sourceLob.deleteEntityName();	//NBLXA-1254
					} else {
						sourceLob.setLastName(fullName);
						sourceLob.setEntityName(fullName);	//NBLXA-1254
					}
				}// end APSL2174
				setGovtIDNumber(sourceLob, party);
			}
		}
	}

	/**
	 * Calls VPMS model to check for Minimum data validation for the current source
	 * 
	 * @param currentSource
	 *            the NbaSource object
	 * @param indexVO
	 *            the Index Row Bean object
	 * @throws NbaBaseException
	 */
	protected void checkMinimumDataValidation(NbaSource currentSource) throws NbaBaseException {
		// Minimum data validation
		if (getNbaTxLife() != null) {// QC8119 SR515492
			Map deOinkMap = new HashMap(2, 1);
			deOinkMap.put("A_SourceTypeLOB", currentSource.getSource().getSourceType());
			deOinkMap.put("A_ApplicationTypeLOB", String.valueOf(getNbaTxLife().getNbaHolding().getApplicationInfo().getApplicationType())); // APSL2808
			String returnStr = processRules(currentSource.getNbaLob(), deOinkMap, NbaVpmsConstants.EP_GET_VALIDATION_MESSAGES, NbaVpmsConstants.INDEX);
			if (!NbaUtils.isBlankOrNull(returnStr)) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFormattedErrorMsg(returnStr), getFailStatus()));
				//APSL4149 code deleted
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_MIN_DATA,returnStr);//APSL4149
			}
		}
	}

	/**
	 * Formats the message by replacing # by comma (,)
	 * 
	 * @param returnStr
	 *            the message string
	 * @return replaced string
	 */
	protected String getFormattedErrorMsg(String returnStr) {
		StringBuffer sb = new StringBuffer();
		NbaStringTokenizer lobNames = new NbaStringTokenizer(returnStr, NbaVpmsConstants.VPMS_DELIMITER[0]); // NBA201
		while (lobNames.hasMoreTokens()) {
			sb.append(lobNames.nextToken());
			if (lobNames.hasMoreTokens()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	/**
	 * This method copies demographic data elements from work item to source LOBs if they are present in work but absent in source
	 * 
	 * @param sourceLob
	 *            LOB data of current source
	 * @param workLob
	 *            LOB data of original work item
	 * @throws NbaBaseException
	 */
	// AXAL3.7.01 : New Method
	protected void copyDemographicData(NbaLob sourceLob, NbaLob workLob) {
		if (NbaUtils.isBlankOrNull(sourceLob.getFirstName()) && !NbaUtils.isBlankOrNull(workLob.getFirstName())) {
			sourceLob.setFirstName(workLob.getFirstName());
		}
		if (NbaUtils.isBlankOrNull(sourceLob.getLastName()) && !NbaUtils.isBlankOrNull(workLob.getLastName())) {
			sourceLob.setLastName(workLob.getLastName());
		}
		if (NbaUtils.isBlankOrNull(sourceLob.getMiddleInitial()) && !NbaUtils.isBlankOrNull(workLob.getMiddleInitial())) {
			sourceLob.setMiddleInitial(workLob.getMiddleInitial());
		}
	}

	/**
	 * This method copies policy number and Tax identifiers from work item to source LOBs if they are present in work but absent in source
	 * 
	 * @param sourceLob
	 *            LOB data of current source
	 * @param workLob
	 *            LOB data of original work item
	 * @throws NbaBaseException
	 */
	// AXAL3.7.01 : New method
	protected void copyPolicyAndTaxIds(NbaLob sourceLob, NbaLob workLob) throws NbaBaseException {
		if (NbaUtils.isBlankOrNull(sourceLob.getPolicyNumber()) && !NbaUtils.isBlankOrNull(workLob.getPolicyNumber())) {
			sourceLob.setPolicyNumber(workLob.getPolicyNumber());
		}
		int taxIDType = sourceLob.getTaxIdType();
		if (NbaOliConstants.OLI_GOVTID_SSN != taxIDType && NbaOliConstants.OLI_GOVTID_TID != taxIDType && NbaOliConstants.OLI_GOVTID_SIN != taxIDType) {
			sourceLob.setTaxIdType(workLob.getTaxIdType());
		}
		if (NbaUtils.isBlankOrNull(sourceLob.getSsnTin()) && !NbaUtils.isBlankOrNull(workLob.getSsnTin())) {
			sourceLob.setSsnTin(workLob.getSsnTin());
		}
	}

	/**
	 * This method copies key data elements from source LOB's item to the work
	 * 
	 * @param workType
	 *            the work type of the work DST
	 * @param sourceAssociatedWithWork
	 *            LOB data of current source
	 * @param workLob
	 *            LOB data of original work item
	 * @throws NbaBaseException
	 */
	protected void copyLobsToWork(String workType, NbaSource sourceAssociatedWithWork, NbaLob workLob) throws NbaBaseException {
		Map deOinkMap = new HashMap(2, 1);
		NbaLob sourceLob = sourceAssociatedWithWork.getNbaLob();
		deOinkMap.put("A_SourceTypeLOB", sourceAssociatedWithWork.getSourceType());
		deOinkMap.put("A_WorkTypeLOB", workType);
		String lobs = processRules(sourceLob, deOinkMap, NbaVpmsConstants.EP_INDEX_PARENT_WORK_ITEM, NbaVpmsConstants.INDEX);
		NbaStringTokenizer lobNames = null;
		if (!NbaUtils.isBlankOrNull(lobs)) {
			List lobList = new ArrayList();
			lobNames = new NbaStringTokenizer(lobs, NbaVpmsConstants.VPMS_DELIMITER[0]); // NBA201
			int nextToken = 0;
			while (lobNames.hasMoreTokens()) {
				lobList.add(nextToken, lobNames.nextToken());
				nextToken++;
			}
			sourceLob.copyLOBsTo(workLob, lobList);
		} else {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Unable to Copy LOB to work", getAWDFailStatus()));
			addComment("Unable to Copy LOB to work");
		}
	}

	protected String processRules(NbaLob nbaLob, Map deOinkMap, String entryPoint, String modelName) throws NbaBaseException {
		NbaVpmsAdaptor rulesProxy = null;
		String returnStr = "";
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(nbaLob);
			rulesProxy = new NbaVpmsAdaptor(data, modelName);
			rulesProxy.setSkipAttributesMap(deOinkMap);
			rulesProxy.setVpmsEntryPoint(entryPoint);

			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			VpmsComputeResult rulesProxyResult = rulesProxy.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List rulesList = vpmsResultsData.getResultsData();
				if (!rulesList.isEmpty()) {
					String xmlString = (String) rulesList.get(0);
					NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
					VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
					List strAttrs = vpmsModelResult.getStandardAttr();

					// Generate delimited string if there are more than one parameters returned
					returnStr = generateDelimitedString(strAttrs);
				}
			}
			return returnStr;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (rulesProxy != null) {
				try {
					rulesProxy.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED); // SPR3362
				}
			}
		}
	}

	/**
	 * Generate delimited string if there are more than one parameters returned in the List strAttrs
	 * 
	 * @param returnStr
	 *            the
	 * @param strAttrs
	 *            attribute list
	 */
	protected String generateDelimitedString(List strAttrs) {
		StringBuffer returnStr = new StringBuffer();
		StandardAttr attr = null;
		int i = 0;
		for (Iterator itr = strAttrs.iterator(); itr.hasNext(); i++) {
			attr = (StandardAttr) itr.next();
			if (i > 0) {
				returnStr.append(NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			}
			returnStr.append(attr.getAttrValue());
		}
		return returnStr.toString();
	}

	/**
	 * set the Tax ID Type and GOvt Id fields
	 * 
	 * @param indexParty
	 *            index party bean
	 * @param sourceLob
	 *            source LOB object
	 */
	protected void setGovtIDNumber(NbaLob sourceLob, NbaParty party) {
		if (party != null) {
			long govtIDTC = party.getParty().getGovtIDTC();
			String govtID = party.getParty().getGovtID();
			if (!NbaUtils.isBlankOrNull(govtIDTC)) {
				sourceLob.setTaxIdType((int) govtIDTC);
			} else {
				sourceLob.deleteTaxIdType();
			}
			if (NbaUtils.isBlankOrNull(govtID)) {
				sourceLob.deleteSsnTin();
			} else {
				sourceLob.setSsnTin(govtID);
			}
			//NBLXA-1254
			if (party.isOrganization()) {
				if (NbaUtils.isBlankOrNull(govtID)) {
					sourceLob.deleteEntityEinTin();
				} else {
					sourceLob.setEntityEinTin(govtID);
				}
			}
			//NBLXA-1254
		}
	}

	/**
	 * Store the default LOBS for a trial application
	 * 
	 * @param sourceLob
	 *            source LOB object
	 * @return updated LOB object
	 */
	// NBA187 New Method
	protected NbaLob setDefaultInformalLobs(NbaLob sourceLob) throws NbaBaseException {
		VpmsModelResult vpmsModelResult = getDefaultLOBFromVPMS(sourceLob);

		if (vpmsModelResult != null) {
			List strAttrs = vpmsModelResult.getStandardAttr();
			Iterator itr = strAttrs.iterator();
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess();
			nbaOinkDataAccess.setLobDest(sourceLob);
			NbaOinkRequest aNbaOinkRequest;
			StandardAttr standardAttr;
			// update the lobs with default values
			while (itr.hasNext()) {
				aNbaOinkRequest = new NbaOinkRequest();
				standardAttr = (StandardAttr) itr.next();
				aNbaOinkRequest.setVariable(standardAttr.getAttrName().substring(2));
				aNbaOinkRequest.setValue(standardAttr.getAttrValue());
				nbaOinkDataAccess.updateValue(aNbaOinkRequest);
			}
		}
		return sourceLob;
	}

	/**
	 * Call Indexing VP/MS model for trial defaults
	 * 
	 * @param lobData
	 *            Lob data which from which OINK auto populated the input vpms values
	 * @return VpmsModelResult
	 * @throws NbaBaseException
	 */
	protected VpmsModelResult getDefaultLOBFromVPMS(NbaLob sourceLob) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(sourceLob);
			proxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.INDEX);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_TRIAL_APP_DEFAULTS);

			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			VpmsComputeResult proxyResult = proxy.getResults();
			if (!proxyResult.isError()) {
				NbaVpmsResultsData resultsData = new NbaVpmsResultsData(proxyResult);
				List rulesList = resultsData.getResultsData();
				if (!rulesList.isEmpty()) {
					String xmlString = (String) rulesList.get(0);
					NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
					return nbaVpmsModelResult.getVpmsModelResult();
				}
			}
			return null;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
	}

	private void updateLobsForJoint(NbaLob nbaLob) throws NbaBaseException {
		nbaLob.setJointDOB(nbaLob.getDOB());
		nbaLob.setJointFirstName(nbaLob.getFirstName());
		nbaLob.setJointLastName(nbaLob.getLastName());
		nbaLob.setJointSsnTin(nbaLob.getSsnTin());
	}

	public void translateNbaValues() throws NbaBaseException {
		Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
		String productCode = null;
		if (policy != null) {
			productCode = getSubmitPolicyHelper().getTranslatedNbaValue(NbaConstants.PRODUCT_CODE, policy.getProductCode());
			if (!NbaUtils.isBlankOrNull(productCode)) {
				policy.setProductCode(productCode);
			}
		}
	}

	// End : EAPP
	/**
	 * @return Returns the submitPolicyHelper.
	 */
	public NbaXML103SubmitPolicyHelper getSubmitPolicyHelper() {
		return submitPolicyHelper;
	}

	/**
	 * @param submitPolicyHelper
	 *            The submitPolicyHelper to set.
	 */
	public void setSubmitPolicyHelper(NbaXML103SubmitPolicyHelper submitPolicyHelper) {
		this.submitPolicyHelper = submitPolicyHelper;
	}

	/**
	 * @param reqType
	 * @return Returns the NbaParty to be indexed
	 */
	// New Method QC8173
	public NbaParty getIndexingParty(long reqType, NbaTXLife nbaTXLife) {
		NbaParty nbAParty = null;
		if (nbaTXLife != null) {
			if (reqType == NbaOliConstants.OLI_REQCODE_OWNSUP || reqType == NbaOliConstants.OLI_REQCODE_528
					|| reqType == NbaOliConstants.OLI_REQCODE_646|| reqType == NbaOliConstants.OLI_REQCODE_SEOSUPP || reqType == NbaOliConstants.OLI_REQCODE_TRUSTEDCONTACT) { // APSL3474// NBLXA-1254// NBLXA-1611
				nbAParty = nbaTXLife.getPrimaryOwner();
			}else if(isRequirementForJointIns(reqType)){
					nbAParty = nbaTXLife.getJointParty();
			} else {
				nbAParty = nbaTXLife.getPrimaryPartyFromPrimHolding();// APSL2814,QC#10694
			}
		}
		return nbAParty;
	}

	/**
	 * Update the TxLife
	 * 
	 * @param nbaTXLife
	 * 
	 */
	// New Method QC8410(APSL2011), APSL3361 method refactored
	public void updateTxLife(NbaTXLife nbaTXLife) {
		if (nbaTXLife != null) {
			Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
			if (policy.getApplicationInfo() != null) {
				ApplicationInfo appInfo = policy.getApplicationInfo();
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if (appInfoExt == null) {
					OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
					appInfo.addOLifEExtension(oliExt);
					appInfoExt = oliExt.getApplicationInfoExtension();
				}
				if (NbaUtils.isNull(appInfoExt.getReqPolicyDeliverMethod())) { //APSL4507
					appInfoExt.setReqPolicyDeliverMethod(NbaOliConstants.OLI_POLDELMETHOD_REGULARMAIL); // APSL3361
				}
				UserAuthRequest userAuthRequest = nbaTXLife.getUserAuthRequest();
				VendorApp vendorApp = userAuthRequest.getVendorApp();
				if (vendorApp != null) {
					VendorName vendorName = vendorApp.getVendorName();
					if (vendorName != null) {
						appInfoExt.setSourceVendorCode(vendorName.getVendorCode());
					}
				}
			}
			/*
			 * APSL5120 Start
			 * After Received mismatched value from iPipeline,case went in error queue without updating 103
			 * AFter updating case by NBCM correct value should be used through 103. In Indexing case has the correct
			 * value with LOB -- Agent details is updating through LOB values.
			 */
			NbaDst workDst = getWork();
			if(workDst !=null)
			{
				NbaHolding nbaHolding = nbaTXLife.getNbaHolding();
				if (nbaHolding.getPolicy().getOLifEExtensionCount() > 0) {
					OLifEExtension olifeEx = nbaHolding.getPolicy().getOLifEExtensionAt(0);
					if (olifeEx.isPolicyExtension()) {
						PolicyExtension polExt = olifeEx.getPolicyExtension();
						try {
							if (workDst.getNbaLob().getDistChannel() != -1) {
								if (polExt.hasDistributionChannel()) {
									polExt.setDistributionChannel(workDst.getNbaLob().getDistChannel());
								}
							}
						} catch (NbaBaseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}

			}
			
			
		}
		
	}

	// deleted methods for PERM E-App Changes APSL2296

	/**
	 * @return Returns the bgaOrSbgaRelationRoleCode.
	 */
	public String getBgaOrSbgaRelationRoleCode() {
		return bgaOrSbgaRelationRoleCode;
	}

	/**
	 * @param bgaOrSbgaRelationRoleCode
	 *            The bgaOrSbgaRelationRoleCode to set.
	 */
	public void setBgaOrSbgaRelationRoleCode(String bgaOrSbgaRelationRoleCode) {
		this.bgaOrSbgaRelationRoleCode = bgaOrSbgaRelationRoleCode;
	}

	/**
	 * Populate CWACHECK source Lob from 103XML supplied
	 * 
	 * @param sourceLob
	 *            the NbaLob
	 * @param indexVO
	 *            the input index row bean
	 * @throws NbaBaseException
	 */
	// APSL2735 New Method
	protected void populateCWASource(NbaLob sourceLob) {
		Banking banking = NbaUtils.getBankingByHoldingSubType(getNbaTxLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		sourceLob.setCompany(getNbaTxLife().getPrimaryHolding().getPolicy().getCarrierCode());
		Holding initialHolding = NbaUtils.getHoldingByTypeAndSubTypeCode(getNbaTxLife().getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING,
				NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		NbaParty payerParty = null;
		if (initialHolding != null) {
			payerParty = getNbaTxLife().getParty(getNbaTxLife().getPayerPartyId(NbaOliConstants.OLI_REL_PAYER, initialHolding.getId()));
			if (payerParty != null && payerParty.isPerson()) {
				Person person = payerParty.getPerson();
				if (!NbaUtils.isBlankOrNull(person.getFirstName())) {
					sourceLob.setFirstName(person.getFirstName());
				}
				if (!NbaUtils.isBlankOrNull(person.getMiddleName())) {
					sourceLob.setMiddleInitial(person.getMiddleName());
				}
				if (!NbaUtils.isBlankOrNull(person.getLastName())) {
					sourceLob.setLastName(person.getLastName());
					sourceLob.setCheckLastName(person.getLastName());
				}
				sourceLob.setCheckIdentity(String.valueOf(payerParty.getPartyTypeCode()));
			}
		}
		if (banking != null) {
			BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);
			if (bankingExt != null && !NbaUtils.isBlankOrNull(bankingExt.getBankName())) {
				sourceLob.setFinancialInstitutionName(bankingExt.getBankName());
			}
			if (payerParty != null && payerParty.isOrganization()) {
				sourceLob.setCheckEntityName(payerParty.getOrganization().getDBA());
				sourceLob.setCheckIdentity(String.valueOf(payerParty.getPartyTypeCode()));
			}
			sourceLob.setAccountOwner(NbaUtils.getFullName(payerParty));
			banking.setAcctHolderName(NbaUtils.getFullName(payerParty));
			if (!NbaUtils.isBlankOrNull(banking.getRoutingNum())) {
				sourceLob.setBankRoutingNumber(banking.getRoutingNum());
			}
			if (!NbaUtils.isBlankOrNull(banking.getAccountNumber())) {
				sourceLob.setBankAccountNumber(banking.getAccountNumber());
			}
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
			if (appInfoExt != null) {
				if (!NbaUtils.isBlankOrNull(appInfoExt.getInitialPremiumPaymentForm())) {
					sourceLob.setPaymentMoneySource(String.valueOf(appInfoExt.getInitialPremiumPaymentForm()));
				}
				TempInsAgreementInfo tempInsAgreementInfo = appInfoExt.getTempInsAgreementInfo();
				if (tempInsAgreementInfo != null && !NbaUtils.isBlankOrNull(tempInsAgreementInfo.getTIACashAmt())) {
					sourceLob.setCheckAmount(tempInsAgreementInfo.getTIACashAmt());
					sourceLob.setCwaAmount(tempInsAgreementInfo.getTIACashAmt());
				}
			}
			if (NbaUtils.isAdcApplication(getWork())) { // APSL4507
				sourceLob.setAppProdType(APPPROD_TYPE_ADC);
				if (!NbaUtils.isBlankOrNull(getNbaTxLife().getPolicy().getPaymentAmt())) {
					sourceLob.setCheckAmount(getNbaTxLife().getPolicy().getPaymentAmt());
				}
			}
			if (!NbaUtils.isBlankOrNull(banking.getBankAcctType())) {
				sourceLob.setPaymentCategory(banking.getBankAcctType());
			}
			// Begin APSL5164 NBLXA-1256 code deleted for variable product 
				NbaDst workDst = getWork();
				List nbaSources = workDst.getNbaSources();
				if (nbaSources != null) {
					for (int j = 0; j < nbaSources.size(); j++) {
						NbaSource source = (NbaSource) nbaSources.get(j);
						if (NbaConstants.A_ST_APPLICATION.equals(source.getSourceType())) {
							Date sourceAppDate = NbaUtils.getDateFromStringInAWDFormat(source.getNbaLob().getCreateDate());
							if (!NbaUtils.isBlankOrNull(sourceAppDate)) {
								sourceAppDate = NbaUtils.convertCstToEst(sourceAppDate);
								if (!NbaUtils.isBlankOrNull(sourceAppDate)) {
									sourceLob.setCwaDate(sourceAppDate);
									sourceLob.setCwaTime(sourceAppDate);
								}
							}
						}
					}

				}
			
		}
	}

	// Begin APSL2883

	public void setExchange1035Ind(NbaTXLife nbaTXLife) {
		List holdingList = nbaTXLife.getHoldingList();
		Holding holding = null;
		if (holdingList != null) {
			for (int i = 0; i < holdingList.size(); i++) {
				holding = (Holding) holdingList.get(i);
				if (holding != null) {
					Policy policy = holding.getPolicy();
					if (policy != null) {
						if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null) {
							Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
							if (life != null) {
								LifeUSA lifeUSA = life.getLifeUSA();
								if (lifeUSA != null) {
									LifeUSAExtension lifeUSAExtn = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
									if (lifeUSAExtn != null) {
										lifeUSAExtn.setExchange1035Ind(lifeUSAExtn.getExch1035IndCode() == NbaOliConstants.NBA_ANSWERS_YES);

									}
								}
							}
						}
					}
				}
			}
		}
	}

	// End APSL2883
	// New method ALII1949
	protected String getAsuCodeNum(Party party) {
		Producer producer = party.getProducer();
		if (producer != null && producer.getCarrierAppointmentCount() > 0) {
			CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
			CarrierAppointmentExtension carrierAppointmentExtn = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
			if (carrierAppointmentExtn != null && carrierAppointmentExtn.hasASUCodeNum()) {
				return String.valueOf(carrierAppointmentExtn.getASUCodeNum());
			}
		}
		return null;
	}

	// APSL3447 New Method
	protected Relation getPrimaryHVTPartyRelation(NbaTXLife nbaTXLife) {
		List relationList = nbaTXLife.getOLifE().getRelation();
		int relationListSize = relationList.size();
		for (int i = 0; i < relationListSize; i++) {
			Relation aRelation = (Relation) relationList.get(i);
			if (NbaUtils.isBlankOrNull(aRelation.getRelatedObjectID()) && !NbaUtils.isBlankOrNull(aRelation.getOriginatingObjectID())) {
				return aRelation;
			}
		}
		return null;
	}
	
	//APSL4057 New Method
	/**
	 * If the passed source from populateMiscMailSource 
	 * is applicable for joint insured
	 * @param sourceLob
	 * @throws NbaBaseException
	 */
	private void processJointInsuredParty(NbaLob sourceLob) throws NbaBaseException {
		if(isRequirementForJointIns(sourceLob.getReqType())){
				sourceLob.setJointInsured(true); 
		}
		
		
	}
	
	//APSL4057 New Method
	/**
	 * 
	 * @param reqType
	 * @return true if reqType is applicable for joint ins only
	 * @throws NbaBaseException 
	 */
	private boolean isRequirementForJointIns(long reqType){
		return (reqType == NbaOliConstants.SIUL_JI_SUPP);
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation.
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	// New Method SR515492
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProcPortal.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log(NO_LOGGER);
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}