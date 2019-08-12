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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.csc.fsg.nba.contract.validation.NbaContractValidationConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaValidationMessageData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Criteria;
import com.csc.fsg.nba.vo.txlife.CriteriaExpression;
import com.csc.fsg.nba.vo.txlife.CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension;
import com.csc.fsg.nba.vo.txlife.CriteriaOrCriteriaExpression;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PropertyValue;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * This class is responsible for creating request for TAI Retreive webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.22</td>
 * <td>AXA Life Phase 1</td>
 * <td>TAIRetreive Interface</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AXAWSTAIRetrieveInvoker extends AxaWSInvokerBase {
	
	private static final String CATEGORY = "TAIRETRIEVE";

	private static final String FUNCTIONID = "TAIServicesRetrieve";
	
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AXAWSTAIRetrieveInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}
	
	/**
	 * This method first calls the superclass createRequest() and then set the request specefic attribute.
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		Party party = (Party) getObject();
		NbaUserVO userVO = getUserVO();
		int numOfCriteria = 0;
		NbaTXLife nbaReqTXLife = null;
		Person person = party.getPersonOrOrganization().getPerson();
		if (person != null) {
			Object[] partyInfo = getPersonDetails(person);
			List cs_ParamNames = new ArrayList();
			cs_ParamNames.add("LastName");
			cs_ParamNames.add("FirstName");
			cs_ParamNames.add("MiddleName");
			cs_ParamNames.add("Gender");
			cs_ParamNames.add("BirthDate");
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGSRCH);
			nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_GET_TAI_HOLDING_SEARCH);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			nbaTXRequest.setNbaUser(userVO);
			nbaReqTXLife = new NbaTXLife(nbaTXRequest);
			TXLife reqTXLife = nbaReqTXLife.getTXLife();
			TXLifeRequest aNbaTXLifeRequest = reqTXLife.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
			// code deleted ALS5661			
			CriteriaExpression aCriteriaExpression = new CriteriaExpression();
			CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension aCriteriaExpOrOpr = new CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension();
			numOfCriteria = setCriteriaInRequest(numOfCriteria, partyInfo, cs_ParamNames, aCriteriaExpOrOpr);
			if (numOfCriteria++ > 1) {
				aCriteriaExpOrOpr.setCriteriaOperator(NbaOliConstants.LOGICAL_OPERATOR_AND);
			}
			aCriteriaExpression.setCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension(aCriteriaExpOrOpr);
			aNbaTXLifeRequest.setCriteriaExpression(aCriteriaExpression);
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Request for TAI Service 1009830204 = " + nbaReqTXLife.toXmlString());
		}
		return nbaReqTXLife;
	}	
	
	/**
	 * @param person
	 * @return
	 */
	//AXAL3.7.16 New method
	private Object[] getPersonDetails(Person person) {
		Object partyInfo[] = new Object[5];
		partyInfo[0] = person.getLastName();
		partyInfo[1] = person.getFirstName();
		partyInfo[2] = person.getMiddleName();
		partyInfo[3] = String.valueOf(person.getGender());
		partyInfo[4] = person.getBirthDate();
		return partyInfo;
	}	
	
	/**
	 * @param numOfCriteria
	 * @param partyInfo
	 * @param cs_ParamNames
	 * @param aCriteriaExpOrOpr
	 * @return
	 */
	//AXAL3.7.16 New Method
	private int setCriteriaInRequest(int numOfCriteria, Object[] partyInfo, List cs_ParamNames,
			CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension aCriteriaExpOrOpr) {
		for (int i = 0; i < cs_ParamNames.size(); i++) {
			long searchOperator = NbaOliConstants.OLI_OP_EQUAL;
			//String paramValue = cs_Params.get(i).toString();
			if (!NbaUtils.isBlankOrNull(partyInfo[i])) {
				CriteriaOrCriteriaExpression aCriteriaOrCriteriaExpression = new CriteriaOrCriteriaExpression();
				Criteria aCriteria = new Criteria();
				aCriteria.setPropertyName(cs_ParamNames.get(i).toString());
				PropertyValue aPropertyValue = new PropertyValue();
				aCriteria.setObjectType(NbaOliConstants.OLI_POLICY);
				if (i != 4) {
					aPropertyValue.setPCDATA((String) partyInfo[i]);
				} else {
					String dateOfBirth = NbaUtils.getStringFromDate((Date) partyInfo[i]);
					aPropertyValue.setPCDATA(dateOfBirth);
				}

				aCriteria.setPropertyValue(aPropertyValue);
				aCriteria.setOperation(searchOperator);
				aCriteriaOrCriteriaExpression.setCriteria(aCriteria);
				aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(aCriteriaOrCriteriaExpression);
				numOfCriteria++;
			}
		}
		return numOfCriteria;
	}
	
	/**
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 */
	protected void handleResponse() throws NbaBaseException {
		try {
			super.handleResponse();

		} catch (NbaBaseException e) {
			Party orgParty = (Party) getObject();
			addSystemMessage((NbaTXLife) getWebserviceResponse(), orgParty.getId());
		}

	}
	
    /**
	 * Add a system message to original contract based on the type of failure
	 * @param txLifeResponse an instance of <code>TXLifeResponse</code> object containing service response
	 * @param partyId ID of party being processed
	 */
	//NAB124 New Method
	protected void addSystemMessage(NbaTXLife txLifeResponse, String partyId) {
		NbaTXLife holdingInq = getNbaTXLife();
		SystemMessage msg = new SystemMessage();
		long messageCode = -1L;
		StringBuffer messageDesc = new StringBuffer(); //SPR2737
		Holding holding = holdingInq.getPrimaryHolding();
		List messageList = holding.getSystemMessage();
		ListIterator messageIterator = messageList.listIterator();
		SystemMessage currentMessage = null;
		while (messageIterator.hasNext()) {
			currentMessage = (SystemMessage) messageIterator.next();
			if (partyId.equalsIgnoreCase(currentMessage.getRelatedObjectID())) {
				currentMessage.setActionDelete();
			}
		}

		TransResult transResult = txLifeResponse.getTransResult();
		long resultCode = transResult.getResultCode();
		//begin SPR2737
		if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
			if (transResult.getResultInfoCount() > 0) {
				ResultInfo resultInfo = transResult.getResultInfoAt(0);
				if (resultInfo.hasResultInfoCode() && resultInfo.hasResultInfoDesc()) {
					messageDesc.append(resultInfo.getResultInfoCode());
					messageDesc.append(", ");
					messageDesc.append(resultInfo.getResultInfoDesc().trim());
				}
			}
			messageCode = NbaConstants.UW_RISK_REQUEST_ERROR;
		}
		//end SPR2737
		NbaOLifEId nbaOLifEId = new NbaOLifEId(holdingInq);
		nbaOLifEId.setId(msg);
		msg.setMessageCode((int) messageCode);
		msg.setRelatedObjectID(partyId);
		msg.setActionAdd();
		msg.setMessageStartDate(new Date(System.currentTimeMillis()));
		msg.setSequence("0");
		//Add the SystemMessageExtension
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
		msg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(msg);
		systemMessageExtension.setMsgOverrideInd(false);
		systemMessageExtension.setMsgValidationType(NbaContractValidationConstants.SUBSET_UW_RISK);
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		NbaValidationMessageData[] nbaTableData;
		try {
			nbaTableData = (NbaValidationMessageData[]) nbaTableAccessor.getValidationMessages(String.valueOf(messageCode));
			if (nbaTableData.length > 0) {
				//trim if message length is greater then 100
				messageDesc.insert(0, nbaTableData[0].getMsgDescription() + " "); //SPR2737
				msg.setMessageDescription(messageDesc.length() > 100 ? messageDesc.substring(0, 100) : messageDesc.toString()); //SPR2737
				msg.setMessageSeverityCode(nbaTableData[0].getMsgSeverityTypeCode());
			}
		} catch (NbaDataAccessException e) {
			getLogger().logException(e);
		}

		holding.addSystemMessage(msg);
		holding.setActionUpdate();
	}
}
