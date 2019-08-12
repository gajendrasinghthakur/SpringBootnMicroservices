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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaContractDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * This class is responsible for creating request for SCOR webservice .
 * <p>
 * <b>Modifications: </b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSSCORInvoker extends AxaWS1203Invoker {

	private static final String CATEGORY = "SCOR";

	//FUNCTIONID could be "submitUWRequestSCOR", "submitUpdateRequestSCOR", "retrieveUWResultSCOR"

	/**
	 * constructor from superclass
	 * 
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSSCORInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setCategory(CATEGORY);
		setFunctionId(operation);
	}

	/**
	 * This method first calls the superclass createRequest() and then set the request specefic attribute.
	 * 
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXLife nbaTXLife = null;
		if (WS_OP_RETRIEVE_SCOR.equalsIgnoreCase(getOperation())) {
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			nbaTXRequest.setNbaUser(getUserVO());
			nbaTXLife = new NbaTXLife(nbaTXRequest);
			Policy aPolicy = new Policy();
			aPolicy.setPolNumber((String) getObject());			
			//Begin APSL4660
			Policy policy = selectPolicy(aPolicy.getPolNumber());
			List policyExtList = NbaContractDataBaseAccessor.getInstance().selectPolicyExtension(getArgs(policy));
			PolicyExtension policyExt = (PolicyExtension) policyExtList.get(0);
			OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			aPolicy.addOLifEExtension(olifeExtension);
			PolicyExtension aPolicyExtension = olifeExtension.getPolicyExtension();
			aPolicyExtension.setSalesChannel(policyExt.getSalesChannel());
			//End APSL4660			
			Holding aHolding = new Holding();
			aHolding.setId("Holding_1");
			aHolding.setPolicy(aPolicy);
			nbaTXLife.getOLifE().addHolding(aHolding);
		} else {
			nbaTXLife = super.createRequest();
		}
		nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransSubType(
				NbaOliConstants.TC_SUBTYPE_RTS_HOLDING_TRANSMIT);
		return nbaTXLife;
	}
	
	public String getBackEnd() {
		String backEnd;
		if (WS_OP_RETRIEVE_SCOR.equalsIgnoreCase(getOperation())) {
			backEnd = ADMIN_ID;
		} else {
			backEnd = super.getBackEnd();
		}
		return backEnd;
	}	

	/**
	 * Override method for handling validation resopnse as its handeled by calling class
	 * 
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 */

	public void handleResponse() throws NbaBaseException {
		if (WS_OP_RETRIEVE_SCOR.equalsIgnoreCase(getOperation())) {
			handleRetrieveUWResultSCORResponse();
		} else {
			super.handleResponse();
		}
	}

	protected void handleRetrieveUWResultSCORResponse() throws NbaBaseException {
		TransResult transResult = ((NbaTXLife) getWebserviceResponse()).getTransResult();
		if (transResult != null && transResult.getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE) {
			StringBuffer errorString = new StringBuffer();
			List resultInfoList = transResult.getResultInfo();
			if (resultInfoList != null && resultInfoList.size() > 0) {
				for (int i = 0; i < resultInfoList.size(); i++) {
					ResultInfo resultInfo = (ResultInfo) resultInfoList.get(i);
					if (i > 0)
						errorString.append(" Error count : " + i);
					long resultInfoCode = resultInfo.getResultInfoCode();
					if (!NbaUtils.isBlankOrNull(resultInfoCode))
						errorString.append(" Error Code : (" + resultInfoCode + ") "
								+ NbaTransOliCode.lookupText(NbaOliConstants.RESULT_INFO_CODES, resultInfoCode) + "\n");
					if (!NbaUtils.isBlankOrNull(resultInfo.getResultInfoDesc()))
						errorString.append(" Error Desc : " + resultInfo.getResultInfoDesc());
					errorString.append("\n");
				}
				getLogger().logError("Error Messages in Response for " + getOperation() + ":" + errorString.toString());
				throw new NbaBaseException("Failure response : " + errorString.toString());
			}
		}
	}
	//New Method APSL4660
	public Policy selectPolicy(String poln) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Preparing to execute SELECT Query for Policy");
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Policy valueObject = new Policy(true);
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfiguration.PENDING_CONTRACT);
			stmt = conn.prepareStatement("SELECT ID, CONTRACTKEY, COMPANYKEY,BACKENDKEY FROM \"POLICY\" WHERE PARENTIDKEY = 'Holding_1' AND CONTRACTKEY = ? ");
			stmt.setString(1, poln);
			rs = stmt.executeQuery();
			while (rs.next()) {
				valueObject.setId(rs.getString("ID"));
				valueObject.setContractKey(rs.getString("CONTRACTKEY"));
				valueObject.setCompanyKey(rs.getString("COMPANYKEY"));
				valueObject.setBackendKey(rs.getString("BACKENDKEY"));
			}
		} catch (Throwable t) {
			getLogger().logException("Exception during SELECT for Policy", t);
			return null;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				NbaConnectionManager.returnConnection(conn);
			} catch (Throwable t) {
				getLogger().logException("Exception during SELECT for Policy", t);
				return null;
			}
		}
		return valueObject;
	}
	
	//New Method APSL4660
	/**
	 * Return array of keys for query
	 * @param party
	 * @return query object array
	 */
	private Object[] getArgs(Policy policy) {
		Object args[] = new Object[4];
		args[0] = policy.getId();
		args[1] = policy.getContractKey();
		args[2] = policy.getCompanyKey();
		args[3] = policy.getBackendKey();
		return args;
	}
}