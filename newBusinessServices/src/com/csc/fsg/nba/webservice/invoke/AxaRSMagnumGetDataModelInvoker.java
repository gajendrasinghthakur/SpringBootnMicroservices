package com.csc.fsg.nba.webservice.invoke;

import java.util.Date;

import com.csc.fsg.nba.database.AxaMagnumGetDataModelAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;

/*
 * NBLXA-2402(NBLXA-2550)
 * 
 */

public class AxaRSMagnumGetDataModelInvoker extends AxaRSMagnumInvoker {
	
	private static final String CATEGORY = "Magnum";
	private static final String FUNCTIONID = "GetCaseData";
	
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

	public AxaRSMagnumGetDataModelInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setContentType("application/json");
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
		setCaseId(((Holding) getObject()).getPolicy().getPolNumber());
	}

	@Override
	public Object createRequest() throws NbaBaseException {
		return null;
	}
	
    protected void constructURI() throws NbaBaseException {
    	Holding aHolding = (Holding) getObject();
    	getService().addUriParam(aHolding.getPolicy().getPolNumber());
    	getService().addUriParam(getOperation());
    	
    	if (getLogger().isDebugEnabled()) {
        	getLogger().logDebug("Case ID: " + aHolding.getPolicy().getPolNumber());
        }
    }
    
    protected void handleResponse() throws NbaBaseException {
    	super.handleResponse();
    	AxaMagnumGetDataModelAccessor.insert(getNbaTXLife().getPolicy().getPolNumber(), ((Holding) getObject()).getPolicy().getPolNumber(),
				Long.toString(NbaOliConstants.OLI_HOLDTYPE_MAGNUM_PARAMED_CASE), new Date(), (String)getWebserviceResponse());
    }
}
