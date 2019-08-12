package com.csc.fsg.nba.webservice.invoke;

import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

//NBLXA-2402(NBLXA-2566)
public class AxaRSMagnumInvoker extends AxaRSInvokerBase {
	private String caseId;

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public AxaRSMagnumInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
	}

	@Override
	public Object createRequest() throws NbaBaseException {
		return null;
	}
	
	@Override
	public Object execute() throws NbaBaseException {
		try {
			return super.execute();
		} catch (NbaBaseException ex) {
			ex.printStackTrace();
			AxaErrorStatusException errorStatusException = new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_MGNM_FAILURE,
					"Webservice invocation for " + getOperation() + " failed while executing " + this.getClass().getName() + ": Case ID - " + caseId
							+ ", Response Code - " + getRespMap().get("responseCode") + ", Response Message - " + getRespMap().get("responseObject"));
			if (ex.isFatal()) {
				errorStatusException.forceFatalExceptionType();
			}
			throw errorStatusException;
		}
	}

}
