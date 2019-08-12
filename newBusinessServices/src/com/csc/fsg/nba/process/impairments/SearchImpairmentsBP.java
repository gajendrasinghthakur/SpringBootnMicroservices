package com.csc.fsg.nba.process.impairments;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaAcImpSearchServiceBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaImpairmentSearchRequest;
import com.csc.fsg.nba.vo.NbaImpairmentSearchResult;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.txlife.Criteria;
import com.csc.fsg.nba.vo.txlife.CriteriaExpression;
import com.csc.fsg.nba.vo.txlife.CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension;
import com.csc.fsg.nba.vo.txlife.CriteriaOrCriteriaExpression;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.PropertyValue;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * Performs an impairment search based on the information given in a <code>NbaImpairmentSearchRequest<code>.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA151</td><td>Version 6</td><td>UL and VUL Application Entry Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class SearchImpairmentsBP extends NewBusinessAccelBP {

	public static final String IMPAIRMENT = "Impairment";  //NBA213

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			//begin NBA213
			NbaImpairmentSearchRequest request = (NbaImpairmentSearchRequest) input;
		    NbaAcImpSearchServiceBean bean = new NbaAcImpSearchServiceBean();
			NbaTXLife searchResults = bean.getImpSearchWords(createSearchRequest(request));
			result.addResult(processSearch(request, searchResults));
			//end NBA213
			return result;
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}
	//NBA213 deleted code

	/**
     * Creates an impairment search request transaction.
     * @return
     * @throws NbaBaseException
     */
    //NBA213 New Method
    protected NbaTXLife createSearchRequest(NbaImpairmentSearchRequest request) throws NbaBaseException {
        NbaTXRequestVO txRequest = new NbaTXRequestVO();
        txRequest.setTransType(NbaOliConstants.TC_TYPE_IMPAIRMENTSEARCH);
        txRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        txRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(request.getNbaUserVO()));
        txRequest.setNbaLob(request.getNbaLob());
        NbaTXLife searchRequest = new NbaTXLife(txRequest);
        TXLifeRequest txLifeRequest = searchRequest.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
        txLifeRequest.setMaxRecords(NbaConfiguration.getInstance().getMaxRecords().getImpairmentMaxRecords());
        
        CriteriaExpression criteriaExpression = new CriteriaExpression();
        txLifeRequest.setCriteriaExpression(criteriaExpression);
        CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension criteriaOAndCOrC = new CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension();//ALPC7
        criteriaExpression.setCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension(criteriaOAndCOrC);//ALPC7
        CriteriaOrCriteriaExpression criteriaOrCriteriaExpression = new CriteriaOrCriteriaExpression();
        criteriaOAndCOrC.addCriteriaOrCriteriaExpression(criteriaOrCriteriaExpression);
        criteriaOAndCOrC.setCriteriaOperator(NbaOliConstants.LOGICAL_OPERATOR_AND);
        Criteria criteria = new Criteria();
        criteriaOrCriteriaExpression.setCriteria(criteria);
        criteria.setObjectType("10005000007");
        criteria.setOperation(request.getSearchType());
        criteria.setPropertyName(IMPAIRMENT);
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setPCDATA(request.getSearchCriteria());
        criteria.setPropertyValue(propertyValue);
        return searchRequest;
    }

	/**
	 * Process an NbaTXLife containing Search results. Generate a message if the maximum number of rows 
	 * have been exceeded, or if there are no matching rows. Otherwise update the Search results table
	 * with the results.
	 * @param searchResult - an NbaTXLife containing Search results
	 * @return an empty String
	 */
	//NBA213 New Method
	protected NbaImpairmentSearchRequest processSearch(NbaImpairmentSearchRequest request, NbaTXLife searchResult) {
		TransResult transResult = searchResult.getTransResult();
		long resultCode = transResult.getResultCode();
		request.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
		if (resultCode == NbaOliConstants.TC_RESCODE_SUCCESSINFO) {
			if (resultCode == NbaOliConstants.TC_RESCODE_SUCCESSINFO) {
				long resultInfoCode = transResult.getResultInfoAt(0).getResultInfoCode();
				if (resultInfoCode == NbaOliConstants.TC_RESINFO_MAXRECORDS || resultInfoCode == NbaOliConstants.TC_RESINFO_OBJECTNOTFOUND) {
					request.setResultCode(resultInfoCode);
				}
			}
		} else if (resultCode == NbaOliConstants.TC_RESCODE_FAILURE) {
			request.setResultCode(NbaOliConstants.TC_RESCODE_FAILURE);
		}

		request.setResults(createSearchResults(searchResult));
		return request;
	}

    /**
     * Returns a list of <code>NbaImpairmentSearchResult</code> objects for each impairment found.
     * @param results An instance of <code>NbaTXLife</code> 
     */
    //NBA213 New Method
    protected List createSearchResults(NbaTXLife results) {
        List resultList = new ArrayList();
        if (results != null && results.getOLifE() != null && results.getOLifE().getOLifEExtensionCount() > 0) {
            OLifEExtension oLifEExtension = results.getOLifE().getOLifEExtensionAt(0);
            ImpairmentInfo impairmentInfo;
            NbaImpairmentSearchResult result;
            int count = oLifEExtension.getImpairmentInfoCount();
            for (int i = 0; i < count; i++) {
                impairmentInfo = oLifEExtension.getImpairmentInfoAt(i);
                result = new NbaImpairmentSearchResult();
                result.setType(impairmentInfo.getImpairmentType());
                result.setImpairmentClass(impairmentInfo.getImpairmentClass());
                result.setDescription(impairmentInfo.getDescription());
                result.setRestrictApprovalInd(impairmentInfo.getRestrictApprovalInd()); //NBA224
                resultList.add(result);
            }
        }
        return resultList;
    }
}
