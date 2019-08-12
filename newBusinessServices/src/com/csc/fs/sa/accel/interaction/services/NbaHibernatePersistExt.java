package com.csc.fs.sa.accel.interaction.services;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.hibernate.Query;

import com.csc.fs.DataObject;
import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.ResultImpl;
import com.csc.fs.accel.NbaMessages;
import com.csc.fs.accel.valueobject.AccelValueDataObject;
import com.csc.fs.accel.valueobject.Inquiry;
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fs.dataobject.accel.AccelPK;
import com.csc.fs.logging.Level;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.logging.Logger;
import com.csc.fs.util.GUIDFactory;
import com.csc.fsg.nba.vo.nbaschema.Comments;

/**
 * NbaHibernatePersistExt extends NbaHibernatePersist and provides a way to retrieve by 
 * generating queries using "OR" operator for all the keys specified 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-38</td><td>Version 7</td><td>Performance Tuning and Testing - Comments in Database</td></tr>
 * <tr><td>PERF-APSL696</td><td>AXA Life Phase1</td><td>PERF - Delay automated comment retrieval</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaHibernatePersistExt extends NbaHibernatePersist {

	private static Logger logger = Logger.getLogger(NbaHibernatePersistExt.class);

	/*
	 * (non-Javadoc)
	 * @see com.csc.fs.sa.accel.interaction.InteractionService#invoke(java.util.List)
	 */
	public Result invoke(List dataObjects) {
		logger.log(Level.DEBUG, "NbaHibernatePersist invoke()");
		Result result = new ResultImpl();
		if (!configuration.getMessage().equals(RETRIEVE)) {
			result = super.invoke(dataObjects);  //NBA330, APSL5055
		} else {
			try {
				if (dataObjects != null) {
					Query query = getQuery(dataObjects);
					List results = query.list();
					Iterator iterator = results.iterator();
					int count = 0;
					while (iterator.hasNext()) {
                    	//begin NBA330, APSL5055
                    	DataObject dataObj = (DataObject) iterator.next();
                    	if (dataObj instanceof AccelDataObject) {
                    		AccelDataObject acceldo =  (AccelDataObject) dataObj;
							acceldo.markOriginal();
							acceldo.setIdentifier(GUIDFactory.create().getKeyString());
							acceldo.getPrimaryKey();
                    	} else {
	                    	AccelValueDataObject acceldo = (AccelValueDataObject) dataObj;
							acceldo.markOriginal();
							acceldo.setIdentifier(GUIDFactory.create().getKeyString());
							acceldo.getPrimaryKey();
						}
                        result.addResult(dataObj);
                        //end NBA330, APSL5055
						count++;
					}
					// clear hibernate session to prevent an immediate persit of retrieved objects on
					// session close
					session.clear();
				} else {
    				//begin NBA330, APSL5055
    				Message errMsg = NbaMessages.ERR_MSG_HBRNATE_INV_INPUT.setVariableData(new Object[] { configuration.getName() });
    				result.addMessage(errMsg);
    				LogHandler.Factory.LogError(this, errMsg.format());
    				//end NBA330, APSL5055
				}
			} catch (Exception ex) {
    			//begin NBA330, APSL5055
    			Message errMsg = NbaMessages.ERR_MSG_HBRNATE_EXCEPTION.setVariableData(new Object[] { configuration.getName(), ex.getMessage() });
    			result.addMessage(errMsg);
    			LogHandler.Factory.LogError(this, errMsg.format());
    			//end NBA330, APSL5055
			}
		}
		return result;
	}
	
    /**
	 * Constructs the Query using the getPrimaryKey information from the AccelValueDataObject passed as an input parameter.
	 * 
	 * @param dataObject
	 * @return
	 */
    protected Query getQuery(List dataObjects) {
    	Iterator itr = dataObjects.iterator();
    	List inquiryList = new ArrayList(dataObjects.size()+1);
        DataObject dataObject = null;  //NBA330, APSL5055
    	Inquiry inquiry = null;
    	String objectClassName = null;
    	String additionalCriteria = null; //PERF-APSL696
    	while(itr.hasNext()) {
    		dataObject = (DataObject)itr.next();  //NBA330
    		//begin PERF-APSL696
    		if (isCommentTypes(dataObject)) {
    			additionalCriteria = ((Comments) dataObject).getCommentType();
    			continue;
    		}
    		//end PERF-APSL696
    		inquiry = new Inquiry(dataObject);
    		inquiryList.add(inquiry);
    		if (objectClassName == null) {
    			objectClassName = (String) configuration.getInteractionSpecProperties().get("objectClass");
    			if (objectClassName == null || objectClassName.length() == 0) {
    				objectClassName = dataObject.getClassName().substring(dataObject.getClassName().lastIndexOf(".") + 1);
    			}
    		}
    	}
		String objectKeyField = (String) configuration.getInteractionSpecProperties().get("objectField");
		String queryString = constructQuery(objectClassName, inquiryList, objectKeyField, additionalCriteria); //PERF-APSL696
		Query query = session.createQuery(queryString);
		populateQueryParams(inquiryList, objectKeyField, query, additionalCriteria); //PERF-APSL696
        if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
            LogHandler.Factory.LogDebug("NbaHibernateInteraction", query.getQueryString());
        }
		return query;
	}

    /*
     * Determines if the input object is for the type of comments to retrieve
     */
    //PERF-APSL696 new method
  //NBA330 changed parameter to use DataObject interface
    protected boolean isCommentTypes(DataObject dataObject) {
    	if (dataObject instanceof Comments && hasCommentsType((Comments) dataObject)) {
    		return true;
    	}
    	return false;
    }
    /*
     * returns true if the commentType field is populated
     */
    protected boolean hasCommentsType(Comments comment) {
    	return null != comment.getCommentType() && comment.getCommentType().trim().length() > 0;
    }
    /**
     * Populates the Query Parameters using the values in the Inquiry Object
     * @param inquiry
     * @param fields
     * @param query
     * @param additionalFields
     */
    //PERF-APSL696 new method
    protected void populateQueryParams(List inquiry, String field, Query query, String additionalFields) {
        if (field == null) {
            return;
        }
		// get the primary key of the Inquiry Object and search for the keys in System API
		for (int i = 0; i < inquiry.size(); i++) {
			AccelPK key = (AccelPK) ((Inquiry)inquiry.get(i)).getPrimaryKey();
			Map content = key.getContent();
			if(content.containsKey(field)){
		        // see if we have a named value in the content map
				Object idValue = content.get(field);
				if (idValue instanceof Integer) {
					query.setInteger(i, ((Integer) idValue).intValue());
				} else if(idValue instanceof Date){
					query.setDate(i, (Date)idValue);
				} else if(idValue instanceof BigDecimal){
					query.setBigDecimal(i, (BigDecimal)idValue);
				} else {
				    query.setString(i, idValue.toString());
				}
		    }
		}
		if (null != additionalFields) {
			StringTokenizer st = new StringTokenizer(additionalFields, "#");
			int inquirySize = inquiry.size();
			while (st.hasMoreTokens()) {
				query.setString(inquirySize++, st.nextToken());
			}
		}
    }

    /**
     * This method constructs the Query String.
     * @param objectClassName
     * @param inquiry
     * @param fields
     * @return
     */
    private String constructQuery(String objectClassName, List inquiry, String field) {
        String whereClause = constructWhereClause(inquiry, field);
        StringBuffer queryStr = new StringBuffer();
        queryStr.append("from");
        queryStr.append(" ");
        queryStr.append(objectClassName);
        if (!whereClause.equals("")) {
            queryStr.append(" ");
            queryStr.append("where");
            queryStr.append(" ");
            queryStr.append(whereClause);
        }
        
        return queryStr.toString();
    }


    /*
     * Method populates originaly query and adds the additional criteria to it
     */
    //PERF-APSL696 new method
    private String constructQuery(String objectClassName, List inquiry, String field, String additionalCriteria) {
    	
    	String query = constructQuery(objectClassName, inquiry, field);
    	if (null == additionalCriteria) {
    		return query;
    	}
        String whereClause = constructAdditionalCriteria(additionalCriteria); 
                
        return query.concat(whereClause);
    }
    /*
     * Create additional 'commentTypes' criteria
     */
    //PERF-APSL696 new method
    protected String constructAdditionalCriteria(String field) {

        StringTokenizer st = new StringTokenizer(field,"#");
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(" and ( ");
        while (st.hasMoreTokens()) {
        	st.nextToken();
        	whereClause.append("commentType");
			whereClause.append(" = ? ");
			if (0 < st.countTokens()) {
				whereClause.append(" or ");
			}	
		}
        whereClause.append(")");
        return whereClause.toString();
    }
    /**
     * This method constructs the where clause on the basis of the field array by orring all the fields
     * @param inquiry
     * @param field
     * @return
     */
    protected String constructWhereClause(List inquiry, String field) {
        if (field == null) {
            return "";
        }
        StringBuffer whereClause = new StringBuffer();
        
		for (int i = 0; i < inquiry.size(); i++) {
				if (!whereClause.toString().equals("")) {
					whereClause.append(" or ");
				} else {					//PERF-APSL696
					whereClause.append("("); //PERF-APSL696
				}
			whereClause.append(field);
			whereClause.append(" = ? ");
		}
		whereClause.append(")"); //PERF-APSL696
        return whereClause.toString();
    }
}