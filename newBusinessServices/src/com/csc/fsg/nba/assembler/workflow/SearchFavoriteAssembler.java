/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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

package com.csc.fsg.nba.assembler.workflow;

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.ErrorHandler;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.SearchWorkRequest;
import com.csc.fs.accel.workflow.assembler.WorkflowAccelTransformation;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.LookupRequest;
import com.csc.fs.dataobject.accel.workflow.User;
import com.csc.fs.om.ObjectFactory;
import com.csc.fsg.nba.database.NbaSearchFavoriteAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchFavoriteCriteria;

/**
 * SearchFavoriteAssembler is the dis-assembler/assembler for Search Work Business Process.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>        
 * </table>
 * <p>
 */

public class SearchFavoriteAssembler extends WorkflowAccelTransformation {

    /** 
     * Disassemble the input value object and create Data object. 
     * The method processes value object parameters and fetches additional data from search favorites database table
     * The result is used to construct required lookup DO.
     * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
     */
    public Result disassemble(Object input) {
        SearchWorkRequest requestVO = (SearchWorkRequest) input;

        List inputData = new ArrayList();

        String selectedFavorite = requestVO.getFavorite();
        NbaSearchFavoriteCriteria favoriteCriteria = new NbaSearchFavoriteCriteria();
        favoriteCriteria.setName(selectedFavorite);
        try {
            favoriteCriteria = NbaSearchFavoriteAccessor.loadSearchCriteria(favoriteCriteria, requestVO.getUser());
        } catch (NbaBaseException e) {
            ErrorHandler.process(SearchFavoriteAssembler.class, e);
        }

        User userDO = (User) ObjectFactory.create(User.class);
        inputData.add(userDO);
        LookupRequest lookupRequestDO = (LookupRequest) ObjectFactory.create(LookupRequest.class);

        populateSearchDataObject(lookupRequestDO, favoriteCriteria, requestVO);

        inputData.add(lookupRequestDO);
        Result result = new AccelResult();
        result.addResult(inputData);
        return result;
    }

    /**
     * Populate the Lookup request data object for the current favorite criteria. 
     * @param lookupRequestDO
     * @param favoriteCriteria
     * @param requestVO
     */
    protected void populateSearchDataObject(LookupRequest lookupRequestDO, NbaSearchFavoriteCriteria favoriteCriteria, SearchWorkRequest requestVO) {
        lookupRequestDO.setMaxRecords(requestVO.getMaxRecords());
        lookupRequestDO.setLookupType("W"); //Assume a search is always for Work
        lookupRequestDO.setPageNumber(requestVO.getPageNumber()); //APSL5055-NBA331
        List fields = favoriteCriteria.getCriteriaFields();
        List newLOBs = new ArrayList();

        String field = null;
        String value = null;
        int count = fields.size();
        for (int i = 0; i < count; i++) {
            field = (String) fields.get(i);
            value = favoriteCriteria.getCriteriaFieldValue(field);
            if (NbaLob.A_LOB_BUSINESS_AREA.equals(field)) {
                lookupRequestDO.setBusinessArea(value);
            } else if (NbaLob.A_LOB_QUEUE.equals(field)) {
                lookupRequestDO.setQueue(value);
            } else if (NbaLob.A_LOB_WORK_TYPE.equals(field)) {
                lookupRequestDO.setWorkType(value);
            } else if (NbaLob.A_LOB_STATUS.equals(field)) {
                lookupRequestDO.setWorkStatus(value);
            } else if (NbaLob.A_LOB_CREATE_TIME.equals(field)) {
                setCreateDateCriteria(lookupRequestDO, value);
            } else {
                LOB lobDO = new LOB();
                lobDO.setValue(value);
                lobDO.setName(field);
                newLOBs.add(lobDO);
            }
        }
        lookupRequestDO.setLobData(newLOBs);
    }

    
    /**
     * Set date fields on lookup request data object
     * @param lookupRequestDO
     * @param value
     */
    protected void setCreateDateCriteria(LookupRequest lookupRequestDO, String value) {
        int delimeterIndex = value.indexOf(",");
        String fromString = value.substring(0, delimeterIndex);
        String toString = value.substring(delimeterIndex + 1);
        if (fromString.length() > 0) {
            lookupRequestDO.setBeginDateTime(fromString);
        }
        if (toString.length() > 0) {
            lookupRequestDO.setEndDateTime(toString);
        }
    }

}
