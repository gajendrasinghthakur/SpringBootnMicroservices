package com.csc.fsg.nba.process.mib.planF;

/*
 * *******************************************************************************<BR>
 * Copyright 2015, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.Inquiry;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaMibPlanFDatabaseRequestVO;

/**
 * Invoke the appropriate Service action for MIB Plan F database access:
 *   
 *    RETRIEVE_CONTROL - hibernate/RetrievePlanFControl
 *    UPDATE_CONTROL - hibernate/CommitPlanFControl
 *    RETRIEVE_RESPONSES  - hibernate/RetrievePlanFResponses
 *    RETRIEVE_TEST_WEB_SERVICE_RESPONSES -   hibernate/RetrievePlanFTestWebServiceResponses
 *    UPDATE_RESPONSES  - hibernate/CommitPlanFResponses
 *    DELETE_RESPONSES - hibernate/DeletePlanFResponses       
 *   
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 
 * <tr><td>NBA308</td><td>Version NB-1301</td><td>MIB Follow Ups</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version 7
 */
public class MibPlanFDatabaseAccessBP extends NewBusinessAccelBP {
    /**
     * Determine the operation requested and invoke the appropriate Service action for MIB Plan F database access.
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO = (NbaMibPlanFDatabaseRequestVO) input;
            if (NbaMibPlanFDatabaseRequestVO.RETRIEVE_CONTROL.equals(nbaMibPlanFDatabaseRequestVO.getOperation())) {
                result = retrieveControlSources(result, nbaMibPlanFDatabaseRequestVO);
            } else if (NbaMibPlanFDatabaseRequestVO.UPDATE_CONTROL.equals(nbaMibPlanFDatabaseRequestVO.getOperation())) {
                result = commitControlSources(result, nbaMibPlanFDatabaseRequestVO);
            } else if (NbaMibPlanFDatabaseRequestVO.RETRIEVE_RESPONSES.equals(nbaMibPlanFDatabaseRequestVO.getOperation())) {
                result = retrieveResponses(result, nbaMibPlanFDatabaseRequestVO);
            } else if (NbaMibPlanFDatabaseRequestVO.RETRIEVE_TEST_WEB_SERVICE_RESPONSES.equals(nbaMibPlanFDatabaseRequestVO.getOperation())) {
                result = retrieveTestWebServiceResponses(result, nbaMibPlanFDatabaseRequestVO);
            } else if (NbaMibPlanFDatabaseRequestVO.UPDATE_RESPONSES.equals(nbaMibPlanFDatabaseRequestVO.getOperation())) {
                result = commitResponses(result, nbaMibPlanFDatabaseRequestVO);
            } else if (NbaMibPlanFDatabaseRequestVO.DELETE_RESPONSES.equals(nbaMibPlanFDatabaseRequestVO.getOperation())) {
                result = deleteResponses(result, nbaMibPlanFDatabaseRequestVO);
            } else {
                throw new NbaBaseException("Unsupported input");
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Retrieve Plan F Records
     * @param result
     * @param nbaMibPlanFDatabaseRequestVO
     * @return AccelResult
     */
    protected AccelResult retrieveResponses(AccelResult result, NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO) {
        List requestList = new ArrayList();
        Inquiry inquiry = new Inquiry();
        requestList.add(inquiry);
        inquiry.setIDs(nbaMibPlanFDatabaseRequestVO.getKeyMap());
        AccelResult planFResponseResult = invoke("hibernate/RetrievePlanFResponses", requestList);
        if (planFResponseResult.hasErrors()) {
            return planFResponseResult;
        }
        nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFResponseList().addAll(planFResponseResult.getData());
        result.addResult(nbaMibPlanFDatabaseRequestVO);
        return result;
    }

    /**
     * Retrieve Records created from the Test Web Service for the XML 401 transaction 
     * @param result
     * @param nbaMibPlanFDatabaseRequestVO
     * @return AccelResult
     */
    protected AccelResult retrieveTestWebServiceResponses(AccelResult result, NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO) {
        List requestList = new ArrayList();
        Inquiry inquiry = new Inquiry();
        requestList.add(inquiry);
        inquiry.setIDs(nbaMibPlanFDatabaseRequestVO.getKeyMap());
        AccelResult planFResponseResult = invoke("hibernate/RetrievePlanFTestWebServiceResponses", requestList);
        if (planFResponseResult.hasErrors()) {
            return planFResponseResult;
        }
        nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFResponseList().addAll(planFResponseResult.getData());
        result.addResult(nbaMibPlanFDatabaseRequestVO);
        return result;
    }

    /**
     * Retrieve Plan F Control Records
     * @param result
     * @param nbaMibPlanFDatabaseRequestVO
     * @return AccelResult
     */
    protected AccelResult retrieveControlSources(AccelResult result, NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO) {
        List requestList = new ArrayList();
        requestList.add(new Inquiry());
        AccelResult planFControlResult = invoke("hibernate/RetrievePlanFControl", requestList);
        if (planFControlResult.hasErrors()) {
            return planFControlResult;
        }
        nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFControlList().addAll(planFControlResult.getData());
        result.addResult(nbaMibPlanFDatabaseRequestVO);
        return result;
    }

    /**
     * Commit  Plan F Control Records
     * @param result
     * @param nbaMibPlanFDatabaseRequestVO
     * @return AccelResult
     */
    protected AccelResult commitControlSources(AccelResult result, NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO) {
        AccelResult planFControlResult = invoke("hibernate/CommitPlanFControl", nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFControlList());
        return planFControlResult;
    }

    /**
     * Commit Plan F Records
     * @param result
     * @param nbaMibPlanFDatabaseRequestVO
     * @return AccelResult
     */
    protected AccelResult commitResponses(AccelResult result, NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO) {
        AccelResult planFControlResult = invoke("hibernate/CommitPlanFResponses", nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFResponseList());
        return planFControlResult;
    }

    /**
     * Delete Plan F Records
     * @param result
     * @param nbaMibPlanFDatabaseRequestVO
     * @return AccelResult
     */
    protected AccelResult deleteResponses(AccelResult result, NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO) {
        AccelResult planFControlResult = invoke("hibernate/DeletePlanFResponses", nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFResponseList());
        return planFControlResult;
    }
}
