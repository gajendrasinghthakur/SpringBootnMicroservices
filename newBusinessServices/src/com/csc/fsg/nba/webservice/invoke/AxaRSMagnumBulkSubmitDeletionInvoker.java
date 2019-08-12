package com.csc.fsg.nba.webservice.invoke;

import static com.csc.fsg.nba.foundation.AxaMagnumUtils.*;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.google.gson.Gson;
import com.swissre.magnum.client.BulkSubmitObject;

public class AxaRSMagnumBulkSubmitDeletionInvoker extends AxaRSMagnumInvoker /*NBLXA-2402(NBLXA-2566)*/  {
	
	private static final String CATEGORY = "Magnum";
	private static final String FUNCTIONID = "BulkDeletion";

	public AxaRSMagnumBulkSubmitDeletionInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setContentType("application/json");
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
		RequirementInfo reqInfo = (RequirementInfo) getObject(); //NBLXA-2402(NBLXA-2566)
		setCaseId(getParamedCaseId(nbaTXLife, reqInfo)); //NBLXA-2402(NBLXA-2566)
	}
	
	@Override
	public Object createRequest() throws NbaBaseException {

		BulkSubmitObject bulkSubmitObject = new BulkSubmitObject();
		RequirementInfo reqInfo = (RequirementInfo) getObject();
		constructDeleteRequestBody(bulkSubmitObject, reqInfo);
		
		return new Gson().toJson(bulkSubmitObject);
		
	}
	
	protected void constructURI() throws NbaBaseException {

		NbaTXLife nbaTXLife = getNbaTXLife();
		RequirementInfo reqInfo = (RequirementInfo) getObject();
		String caseId = getParamedCaseId(nbaTXLife, reqInfo);

		getService().addUriParam(caseId);
		getService().addUriParam(getOperation());
		
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Case ID: " + caseId);
		}
	}

	protected NbaLogger getLogger() {
		NbaLogger logger = null;
		try {
			logger = NbaLogFactory.getLogger(this.getClass().getName());
		} catch (Exception e) {
			NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}

		return logger;
	}
	
	protected void constructDeleteRequestBody(BulkSubmitObject bulkSubmitObject, RequirementInfo reqInfo) throws NbaBaseException {
		boolean vitalsRequired = isVitalsRequired(reqInfo.getReqCode());
		String requirementName = getMagnumTranslation(NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(reqInfo.getReqCode()));
		int partyIndex = 0;
		constructAttribute(bulkSubmitObject, "case.life[" + partyIndex + "]." + requirementName);
		if (vitalsRequired) {
			constructAttribute(bulkSubmitObject, "case.life[" + partyIndex + "]" + ".Requirements.Vitals");
		}
	}
	
}
