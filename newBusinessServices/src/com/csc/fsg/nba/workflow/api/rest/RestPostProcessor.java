package com.csc.fsg.nba.workflow.api.rest;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.ResultImpl;
import com.csc.fs.accel.ui.log.LogHandler;
import com.csc.fs.accel.util.ServiceHelper;
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.Link;
import com.csc.fs.dataobject.accel.workflow.Response;
import com.csc.fs.dataobject.accel.workflow.SourceItem;
import com.csc.fs.dataobject.accel.workflow.WorkField;
import com.csc.fs.dataobject.accel.workflow.WorkItem;
import com.csc.fs.sa.SystemAPI;

/**
 * RestPostProcessor contains post-process logic for Rest Post Processors
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL5055 - NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */
 
public class RestPostProcessor extends com.csc.fs.accel.workflow.api.rest.RestPostProcessor {	//APSL5055 - NBA331.1
    protected static final String CASE = "case";
    protected static final String CASE_RECORD_TYPE = "C";
    protected static final String CREATE_DATE_TIME = "Create Date/Time: ";
    protected static final String EMPTY_STRING = "";
    protected static final String FORWARD_SLASH = "/";
    protected static final String LEFT_BRACKET = "(";
    protected static final String N = "N";
    protected static final String NEXT = "next";
    protected static final String ONE = "1";
    protected static final String PAGE = "?page=";
    protected static final String REQUEST_FAILED = "Request Failed: ";
    private static final String RIGHT_BRACKET = ")";
    protected static final String SEMI_COLON = ";";
    protected static final String SOURCE_RECORD_TYPE = "O";
    protected static final String SUSPEND = "suspend";
    protected static final String CHILDREN = "children";	//APSL5055 - NBA331.1
    protected static final String PARENTS = "parents";  //APSL5055 - NBA331.1
    protected static final String CHILD_TRANSACTIONS = "childTransactions"; //APSL5055 - NBA331.1    
    protected static final String TRANSACTION = "transaction";
    protected static final String TRANSACTION_RECORD_TYPE = "T";
    protected static final String Y = "Y";
    //APSL5055 - NBA331.1 code deleted

    /**
     * Return a List containing all the objects in all the ResultImpls of the return data.
     * @param returnData
     * @return List
     */
    protected List aggregate(List returnData) {
        Iterator it = returnData.iterator();
        List aggregatedResults = new ArrayList();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof ResultImpl) {
                aggregatedResults.addAll(((ResultImpl) obj).getData());
            }
        }
        return aggregatedResults;
    }

    //APSL5055 - NBA331.1 code deleted

    /**
     * Create WorkFields from the LOBs in  the Input.
     * @param workItem
     * @param input
     * @return
     */
    protected List copyFieldValues(WorkItem workItem, List input) {
        List origFieldValues = ServiceHelper.getObjectOfType(input, LOB.class);
        List newFieldValues = new ArrayList(origFieldValues.size());
        String parentIdentifier = workItem.getIdentifier();
        String parentItemId = workItem.getItemID();
        Iterator it = origFieldValues.iterator();
        while (it.hasNext()) {
            LOB oldFieldValue = (LOB) it.next();
            WorkField newFieldValue = new WorkField();
            newFieldValue.setIdentifier(oldFieldValue.getIdentifier());
            newFieldValue.setParentIdentifier(parentIdentifier);
            newFieldValue.setParentID(parentItemId);
            newFieldValue.setFieldName(oldFieldValue.getName());
            newFieldValue.setFieldValue(oldFieldValue.getValue());
            newFieldValue.setSystemName(oldFieldValue.getSystemName());
            newFieldValue.markTransient();
            newFieldValues.add(newFieldValue);
        }
        return newFieldValues;
    }

    /**
     * Examine the Response.ResponseCode to determine if processing should continue 
     * @param result
     * @param response
     */
    protected void examineResponse(Result result, Response response) {
        try {
            int responseCode = Integer.parseInt(response.getCode());
            if (responseCode >= 300 && responseCode <= 500) {
            	//APSL5055 - NBA331.1 code deleted
                result.setErrors(true);
            	//APSL5055 - NBA331.1 code deleted                 
            }
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    /**
     * Parse the summaryString to get the Create Date/Time String
     * @param summaryString
     * @return the Create Date/Time String
     */
    protected String getCreateDateFromSummary(String summaryString) {
        String dateTime = EMPTY_STRING;
        try {
            if (summaryString == null || summaryString.isEmpty()) {
                LogHandler.Factory.LogError(this, "Unable to determine Create Date/Time from missing  summaryString");
            } else {
                int idx = summaryString.indexOf(CREATE_DATE_TIME);
                if (idx < 0) { //Bypass if it has already been processed
                    dateTime = summaryString;
                } else {
                    dateTime = summaryString.substring(idx + CREATE_DATE_TIME.length());
                }
            }
        } catch (Exception e) {
            LogHandler.Factory.LogError(this, "Unable to determine Create Date/Time from " + summaryString, e);
        }
        return dateTime;
    }

    /**
     * Retrieve the WorkItem from the return data
     * @param currentData
     * @return theWorkItem
     */
    protected WorkItem getCurrentWorkItem(List returnData) {
        Iterator rit = returnData.iterator();
        while (rit.hasNext()) {
            Result currentResult = (Result) rit.next(); //Assume multiple Results within the current Result
            List currentData = currentResult.getReturnData();
            Iterator dataObjects = currentData.iterator();
            while (dataObjects.hasNext()) {
                Object currentObj = dataObjects.next();
                if (currentObj instanceof WorkItem) {
                    return (WorkItem) currentObj;
                }
            }
        }
        return null;
    }

    /**
     * Return the ItemID (if present) from the input Array.
     * @param input
     * @return ItemID
     */
    protected ItemID getItemID(List input) {
        ItemID itemID;
        Iterator it = input.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof ItemID) {
                itemID = (ItemID) obj;
                if (itemID.getPageNo() == null){
                    itemID.setPageNo("1");
                }
                return itemID;
            }
        }
        itemID = new ItemID();
        itemID.setPageNo("1");
        input.add(itemID);
        return itemID;
    }

    /**
     * Parse the String and return the Item ID
     * @param string
     * @return the Item ID
     */
    protected String getItemIdFromHref(String string) {
        String itemId = EMPTY_STRING;
        if (string != null) {
            int end = string.lastIndexOf(FORWARD_SLASH);
            if (end > -1) {
                int begin = string.lastIndexOf(FORWARD_SLASH, end - 1);
                if (begin > -1) {
                    itemId = string.substring(begin + 1, string.lastIndexOf(FORWARD_SLASH));
                }
            }
        }
        return itemId;
    }

    //APSL5055 - NBA331.1 code deleted

    /**
     * Locate and return the next page number by examining the Links in the input List
     * @param data
     * @return the next page number or an empty Sting
     */
    protected String getNextPageNo(List data) {
        String nextPageNo = EMPTY_STRING;
        String pageString = PAGE;
        Iterator it = data.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Link) {
                Link aLink = (Link) obj;
                if (NEXT.equals(aLink.getLinkType())) {
                    String href = aLink.getLinkHref();
                    int start = href.indexOf(pageString);
                    if (start > -1) {
                        start += pageString.length();
                        int end = href.indexOf(SEMI_COLON, start);
                        if (end < 0) {
                            end = aLink.getLinkHref().length();
                        }
                        if (start < end) {
                            nextPageNo = href.substring(start, end);
                        }
                    }
                    break;
                }
            }
        }
        return nextPageNo;
    }

    //APSL5055 - NBA331.1 code deleted

    /**
     * Return the value between the beginText and endText 
     * @param text
     * @param beginText
     * @param endText
     * @return
     */
    protected String getValue(String text, String beginText, String endText) {
        String value = EMPTY_STRING;
        if (text != null) {
            int indexOfLastOpenPeren = text.lastIndexOf(beginText);
            int indexOfLastClosePeren = text.lastIndexOf(endText);
            if (indexOfLastOpenPeren != -1 && indexOfLastClosePeren != -1) {
                value = text.substring(indexOfLastOpenPeren + 1, indexOfLastClosePeren);
            } else {
                value = text;
            }
        }
        return value;
    }

    //APSL5055 - NBA331.1 code deleted

    /**
     * Process a Source Item by:
     * - setting the CreateTime value
     * - setting the SystemName value
     * - setting the LockedBy value
     *- setting the RecordType value
     *- setting the CreateUser value    
     * @param api
     * @param sourceItem
     */
    protected void processSourceItem(SystemAPI api, SourceItem sourceItem) {
        sourceItem.setCreateTime(sourceItem.getItemID().substring(0,26));   //APSL5055 - NBA331.1
        sourceItem.setSystemName(api.getSystemName());
        sourceItem.setLockedBy(getValue(sourceItem.getLockedBy(), LEFT_BRACKET, RIGHT_BRACKET));
        sourceItem.setRecordType(SOURCE_RECORD_TYPE);
        sourceItem.setCreateUserID(getValue(sourceItem.getCreateUserID(), LEFT_BRACKET, RIGHT_BRACKET));
        sourceItem.markTransient();
    }

    /**
     * Process a Work Field by
     * - setting the System Name value
     * - setting the Parent ID value
     * - setting the Parent Identifier value
     * @param api
     * @param sourceItem
     */
    protected void processWorkField(SystemAPI api, WorkField workField, String parentId, String parentIdentifier) {
        workField.setSystemName(api.getSystemName());
        workField.setParentID(parentId);
        workField.setParentIdentifier(parentIdentifier);
        workField.markTransient();
    }

    /**
     * Process a Work Item by:
     * - setting the CreateTime value
     * - setting the SystemName value
     * - setting the LockedBy value
     * - setting the AssignedTo value
     *- setting the RecordType value
     *- setting the CreateNode value
     *- setting the Suspended  value
     * @param api
     * @param workItem
     */
    protected void processWorkItem(SystemAPI api, WorkItem workItem, List<Link> selectedLinks) {   //APSL5055 - NBA331.1
        workItem.setCreateTime(workItem.getItemID().substring(0,26));   //APSL5055 - NBA331.1
        workItem.setSystemName(api.getSystemName());
        workItem.setLockedBy(getValue(workItem.getLockedBy(), LEFT_BRACKET, RIGHT_BRACKET));
        workItem.setAssignedTo(getValue(workItem.getAssignedTo(), LEFT_BRACKET, RIGHT_BRACKET));
        if (TRANSACTION.equalsIgnoreCase(workItem.getRecordType())) {
            workItem.setRecordType(TRANSACTION_RECORD_TYPE);
        } else if (CASE.equalsIgnoreCase(workItem.getRecordType())) {
            workItem.setRecordType(CASE_RECORD_TYPE);
        }
        workItem.setCreateNode(workItem.getItemID().substring(27));
        //begin APSL5055 - NBA331.1
        if (workItem.getSuspended() == null) {
            workItem.setSuspended(N);
        }
        if (workItem.getHasChildren() == null) {
            workItem.setHasChildren(N);
        }
        if (workItem.getHasParent() == null) {
            workItem.setHasParent(N);
        }
        //end APSL5055 - NBA331.1
        Iterator it = selectedLinks.iterator();
        while (it.hasNext()) {
            Link link = (Link) it.next();
            String href = link.getLinkHref();
            String itemId = getItemIdFromHref(href);
            if (workItem.getItemID().equals(itemId)) {
                // begin APSL5055 - NBA331.1
                if (CHILDREN.equalsIgnoreCase(link.getLinkType()) || CHILD_TRANSACTIONS.equalsIgnoreCase(link.getLinkType())) {
                    workItem.setHasChildren(Y);
                } else if (PARENTS.equalsIgnoreCase(link.getLinkType())) {
                    workItem.setHasParent(Y);
                } else if (SUSPEND.equalsIgnoreCase(link.getLinkType())) {
                    workItem.setSuspended(Y);
                }
                // end APSL5055 - NBA331.1
            }
        }
        workItem.markTransient();
    }

    //APSL5055 - NBA331.1 code deleted

    /**
     * Update the ItemID with the Work Item ID. Initialize the PageNo in the ItemID to "1". 
     * @param input
     * @param currentItemID
     * @param workItem
     */
    protected void updateItemID(ItemID currentItemID, WorkItem workItem) {
        currentItemID.setItemID(workItem.getItemID());
        currentItemID.setPageNo(ONE);
        currentItemID.setRecordType(workItem.getRecordType());
    }

    /**
     * Return a Date/Time string adjusted by the time zone offset
     * @param dateString
     * @param timeString
     * @return
     */
    //APSL5055 - NBA331.1 New Method
    protected String formatDateTimeWithOffset(String dateString, String timeString) {
        String resultString = null;
        try {
            int count = timeString.indexOf("-");
            int hh = 0;
            int mm = 0;
            String newTimeString = timeString;
            if (count > 0) {
                String offset = timeString.substring(count);
                newTimeString = timeString.substring(0, count);
                count = offset.indexOf(":");
                if (!(count < 0)) {
                    hh = Integer.valueOf(offset.substring(1, count)).intValue();
                    mm = Integer.valueOf(offset.substring(count + 1)).intValue();
                }
            }
            resultString = new StringBuffer().append(dateString).append(" ").append(newTimeString).toString();
            SimpleDateFormat sdf_Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date newDate = sdf_Date.parse(resultString);
            Calendar cal = new GregorianCalendar();
            cal.setTime(newDate);
            //cal.add(Calendar.HOUR_OF_DAY, hh);	//NBLXA-2264
            //cal.add(Calendar.MINUTE, mm);			//NBLXA-2264
            resultString = sdf_Date.format(cal.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }
    /**
     * Examine a Link and add it to the Links array if it contain Parent, Child, or Suspend Information.
     * If it contains Child Information, also add it to the input to allow it to be processed by a subsequent System API.
     * @param currentObj
     * @param link
     */
    //APSL5055 - NBA331.1 New Method
    protected boolean processLink(List input, Link link, List<Link> selectedLinks) {                 
        if (!link.isTransient()) { // Ignore if already processed
            link.markTransient();
            if (includeLink(link.getLinkType())) {
                selectedLinks.add(link);
            }
            if (CHILDREN.equalsIgnoreCase(link.getLinkType())) {
                input.add(link);
            }
        }
        if (NEXT.equalsIgnoreCase(link.getLinkType())) {
            return true;
        }
        return false;
    }    
    /**
     * Examine the Links and isolate those which contain Parent, Child, or Suspend Information.
     * @param returnData
     */
    //APSL5055 - NBA331.1 New Method
    protected List processLinks(List input, List returnData) {
        List<Link> selectedLinks = new ArrayList<Link>();
        Iterator rit = returnData.iterator();
        while (rit.hasNext()) {
            Object obj = rit.next();
            if (obj instanceof Result) { // Results have not been aggregated
                Result currentResult = (Result) obj;
                List currentData = currentResult.getReturnData();
                Iterator dataObjects = currentData.iterator();
                while (dataObjects.hasNext()) {
                    Object currentObj = dataObjects.next();
                    if (currentObj instanceof Link) {
                        Link link = (Link) currentObj;
                        boolean keep = processLink(input, link, selectedLinks);
                        if (!keep) {
                            dataObjects.remove();
                        }
                    }
                }
            } else if (obj instanceof Link) { // Results have been aggregated
                Link link = (Link) obj;
                boolean keep = processLink(input, link, selectedLinks);
                if (!keep) {
                    rit.remove();
                }
            }
        }
        return selectedLinks;
    }
    
    /**
     * Determine if the linkType is Parent, Child, or Suspend Information
     * @param linkType
     * @return true if the linkType is Parent, Child, or Suspend Information
     */
    //APSL5055 - NBA331.1 New Method
    protected boolean includeLink(String linkType) {
        return SUSPEND.equalsIgnoreCase(linkType) 
            || CHILDREN.equalsIgnoreCase(linkType) 
            || CHILD_TRANSACTIONS.equalsIgnoreCase(linkType)
            || PARENTS.equalsIgnoreCase(linkType);
    }
}
