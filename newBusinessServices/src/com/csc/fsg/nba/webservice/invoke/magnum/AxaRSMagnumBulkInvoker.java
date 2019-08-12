package com.csc.fsg.nba.webservice.invoke.magnum;

import static com.csc.fsg.nba.foundation.AxaMagnumUtils.SIMPLE_VALUE;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.constructAttribute;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.getMagnumTranslation;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.getParamedCaseId;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.getValObjectAdapterFactory;
import static com.csc.fsg.nba.foundation.AxaMagnumUtils.isVitalsRequired;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.webservice.invoke.AxaRSInvokerBase;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
import com.google.gson.GsonBuilder;
import com.swissre.magnum.client.BulkSubmitObject;

public class AxaRSMagnumBulkInvoker extends AxaRSInvokerBase implements NbaOliConstants {
	
	private static final String CATEGORY = "Magnum";
	private static final String FUNCTIONID = "Bulk";
	
	public AxaRSMagnumBulkInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setContentType("application/json");
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	protected void constructURI() throws NbaBaseException {
		String casedId = getParamedCaseId(getNbaTXLife(), (RequirementInfo) getObject());
		getService().addUriParam(casedId);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Case ID: " + casedId);
		}
		getService().addUriParam(getOperation());
	}

	/**
	 * New Method created for NBLXA-2402 (NBLXA-2602) US#297686
	 */
	@Override
	public Object execute() throws NbaBaseException {
		RequirementInfo requirementInfo = (RequirementInfo) getObject();
		AxaWSInvoker webServiceDeleteInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_BULK_SUBMIT_DELETION, getUserVO(), getNbaTXLife(), null,
				requirementInfo);
		webServiceDeleteInvoker.execute();
		
		return super.execute();
	}
	
	public Object createRequest() throws NbaBaseException {
		BulkSubmitObject bulkSubmitObj = new BulkSubmitObject();
		RequirementInfo aReqInfo = (RequirementInfo) getObject();
		int partyIndex = 0;
		String reqName = getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(aReqInfo.getReqCode()));
		constructReqStatus(aReqInfo, reqName, bulkSubmitObj, isVitalsRequired(aReqInfo.getReqCode()), partyIndex);
		AxaMagnumBulkBuilderFactory.getBuilder(aReqInfo.getReqCode()).constructReqData(aReqInfo, bulkSubmitObj, partyIndex);
		return new GsonBuilder().registerTypeAdapterFactory(getValObjectAdapterFactory()).create().toJson(bulkSubmitObj);
	}

	protected void constructReqStatus(RequirementInfo aReqInfo, String reqName, BulkSubmitObject bulkSubmitObj, boolean vitalsRequired, int partyIndex) {
		RequirementInfoExtension aReqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(aReqInfo);
		GregorianCalendar gc = new GregorianCalendar();
		// Requirement Date
		if (!NbaUtils.isBlankOrNull(aReqInfo.getStatusDate())) {
			gc.setTime(aReqInfo.getStatusDate());
			try {
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "].Requirements." + reqName + ".RequirementDate", DatatypeFactory
						.newInstance().newXMLGregorianCalendar(gc), SIMPLE_VALUE);
			} catch (DatatypeConfigurationException ex) {

			}
		}
		// Requirement ID
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "].Requirements." + reqName + ".RequirementID", aReqInfo.getId(), SIMPLE_VALUE);
		// Requirement Status
		constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "].Requirements." + reqName + ".Status", "Received", SIMPLE_VALUE);
		// Requirement Vendor
		if (!NbaUtils.isBlankOrNull(aReqInfoExtn)) {
			if (!NbaUtils.isBlankOrNull(aReqInfoExtn.getTrackingInfo())) {
				constructAttribute(bulkSubmitObj, "case.life[" + partyIndex + "].Requirements." + reqName + ".Vendor", aReqInfoExtn.getTrackingInfo()
						.getTrackingServiceProvider(), SIMPLE_VALUE);
			}
		}
		// Vitals
		if (vitalsRequired) {
			constructReqStatus(aReqInfo, "Vitals", bulkSubmitObj, false, partyIndex);
		}
	}
}