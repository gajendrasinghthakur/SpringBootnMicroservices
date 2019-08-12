
package com.csc.fsg.nba.process.contract.planchange;

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

import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.process.contract.MarkupPlanChangeBP;
import com.csc.fsg.nba.tableaccess.NbaAllowablePlanChangeData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaPlanChangeRequest;

/**
 * RetrievePlanType is getting the Product type for all the plans on PlanChange view.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class RetrievePlanTypeBP extends MarkupPlanChangeBP {
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result = getPlanType((NbaPlanChangeRequest) input);
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * This method gets the product type for all the alloable plans on Plan Change View  
     * @param request
     * @return
     */
    private AccelResult getPlanType(NbaPlanChangeRequest request) {
        AccelResult accelResult = new AccelResult();
        try {
            NbaTableAccessor tableAccessor = new NbaTableAccessor();
            Map tblKeys = tableAccessor.setupTableMap(request.getNbaDst());
            tblKeys.put(NbaTableAccessConstants.USAGE, "NbaPlanChange"); //passing the value of USAGE column to the query
            tblKeys.put(NbaTableAccessConstants.C_TABLE_NAME, NbaTableConstants.NBA_ALLOWABLE_PLAN_CHANGES);
            if (NbaConfiguration.getInstance().isGenericPlanImplementation()) {
                if (request.isOverriden()) {
                    tblKeys.put(NbaTableAccessConstants.C_GENERIC_PLAN, NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                            NbaConfigurationConstants.GENERIC_PLAN_OVERRIDE_QUERYKEY));
                } else {
                    tblKeys.put(NbaTableAccessConstants.C_GENERIC_PLAN, NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                            NbaConfigurationConstants.GENERIC_PLAN_QUERYKEY));
                }
            } else {
                tblKeys.put(NbaTableAccessConstants.C_GENERIC_PLAN, NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                        NbaConfigurationConstants.GENERIC_PLAN_OVERRIDE_QUERYKEY));
            }
            NbaAllowablePlanChangeData[] planChangeObj = (NbaAllowablePlanChangeData[]) tableAccessor.getPlansDisplayData(tblKeys);
            accelResult.addResult(planChangeObj);
        } catch (NbaBaseException e) {
            addMessage(accelResult, e.getMessage());
        }
        return accelResult;
    }
}

