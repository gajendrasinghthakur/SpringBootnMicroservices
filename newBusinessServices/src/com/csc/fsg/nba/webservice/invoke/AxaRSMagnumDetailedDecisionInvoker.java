package com.csc.fsg.nba.webservice.invoke;

import java.util.Date;

import com.csc.fsg.nba.database.AxaMagnumDecisionAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaMagnumUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.AxaMagnumDecisionServiceVO;


//NBLXA-2402(NBLXA-2566) Changed parent class
public class AxaRSMagnumDetailedDecisionInvoker extends AxaRSMagnumInvoker {
	
	private static final String CATEGORY = "Magnum";
	private static final String FUNCTIONID = "GetDetailedDecision";
	
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

	public AxaRSMagnumDetailedDecisionInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setContentType("application/json");		
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
		setCaseId(((AxaMagnumDecisionServiceVO) getObject()).getMagnumHolding().getPolicy().getPolNumber()); //NBLXA-2402 (NBLXA-2586)		
	}

	@Override
	public Object createRequest() throws NbaBaseException {
		return null;
	}
	
    protected void constructURI() throws NbaBaseException {
    	Holding aHolding = ((AxaMagnumDecisionServiceVO) getObject()).getMagnumHolding(); //NBLXA-2402 (NBLXA-2586)
    	getService().addUriParam(aHolding.getPolicy().getPolNumber());
    	if (getLogger().isDebugEnabled()) {
        	getLogger().logDebug("Case ID: " + aHolding.getPolicy().getPolNumber());
        }
    }
    
    protected void handleResponse() throws NbaBaseException {
    	super.handleResponse();   	
    	Holding magnumHolding = ((AxaMagnumDecisionServiceVO) getObject()).getMagnumHolding(); //NBLXA-2402 (NBLXA-2586)
    	AxaMagnumDecisionAccessor.insert(getNbaTXLife().getPolicy().getPolNumber(), magnumHolding.getPolicy().getPolNumber(),
				Long.toString(NbaOliConstants.OLI_HOLDTYPE_MAGNUM_PARAMED_CASE), new Date(), (String)getWebserviceResponse(), AxaMagnumUtils.getpartyType(magnumHolding.getId(), getNbaTXLife()),((AxaMagnumDecisionServiceVO) getObject()).getMagnumProcess());//NBLXA-2402 (NBLXA-2586)
    }    
    
}
