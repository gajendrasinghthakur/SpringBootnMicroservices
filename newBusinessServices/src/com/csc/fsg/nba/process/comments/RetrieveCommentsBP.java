package com.csc.fsg.nba.process.comments;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaRetrieveCommentsRequest;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.Comments;

/**
 * Retrieves the comments added to workflow value object <code>NbaDst</code> from Comments table.
 * The incoming supported request is the <code>NbaRetrieveCommentsRequest</code> value object.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-38</td><td>Version 7</td><td>Performance Tuning and Testing - Comments in Database</td></tr>
 * <tr><td>NBA225</td><td>Version 8</td><td>Comments</td></tr>
 * <tr><td>ALS3212</td><td>AXA Life Phase 1</td><td>QC# 1850  - Sort order of comments is unclear and not correct</td></tr>
 * <tr><td>PERF-APSL324</td><td>AXA Life Phase1</td><td>PERF - Optimize RetrieveComments</td></tr>
 * <tr><td>PERF-APSL696</td><td>AXA Life Phase1</td><td>PERF - Delay automated comment retrieval</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class RetrieveCommentsBP extends NewBusinessAccelBP {   
	
	private String defaultWorkType; //PERF-APSL324
	private String defaultId;  //PERF-APSL324
	
	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
        	//begin NBA208-36 
            if (input instanceof NbaRetrieveCommentsRequest) {
				NbaRetrieveCommentsRequest commentsRequest = (NbaRetrieveCommentsRequest) input;
				NbaDst nbaDst = commentsRequest.getNbaDst();
				//PERF-APSL324 begin
				if (nbaDst.isCase()) {
					defaultWorkType = nbaDst.getWorkType();
					defaultId = nbaDst.getID();  
				}
				//PERF-APSL324 end
				if (commentsRequest.isRetrieveChildren() && nbaDst.isCase()) {
					result = getChildrenWorkItems(nbaDst);
					if (result.hasErrors()) {
						return result;
					}
					nbaDst = (NbaDst) result.getFirst();
				}
				result.addResult(retrieveComments(nbaDst,commentsRequest.getCommentTypes())); //PERF-APSL696
            //end NBA208-36
            } else {
                throw new NbaBaseException("Unsupported input");
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
            return result;
        }
        return result;
    }

    /**
     * Retrieve history information for complete NbaDst hierarchy. Retrieves history for parent case, and each transaction and source in the hierarchy
     * @param origNbaDst
     * @return
     * @throws NbaBaseException
     */
    //PERF-APSL696 changed method signature
    protected NbaDst retrieveComments(NbaDst origNbaDst, String commentTypes) throws NbaBaseException {
    	// hashmap to store the Work Items for which we are retrieving comments.
    	// This hashmap will help us to merge back the comment and save us from reiterating through the whole Workflow hierarchy
    	Map workItemMap = new HashMap();
    	// get list of all workItem ID's for which we need to retrieve comments
    	List commentsRequest = prepareCommentsRequest(origNbaDst, workItemMap, commentTypes); //PERF-APSL696
        Result commentsResult = null;
        if (commentsRequest.size() > 0) {
        	commentsResult = invoke("hibernate/RetrieveComments", commentsRequest);
            mergeComments(commentsResult.getData(), workItemMap);
        }
        return origNbaDst;
    }
    /**
     * Merge the retrieved comments from database to workflow hierarchy. 
     * @param workItemMap containing the workitems for which request was sent
     */
    protected void mergeComments(List responseList, Map workItemMap) {
    	// if no comments are retrieved return
    	if(responseList == null || responseList.size() == 0) {
    		return;
    	}
    	// loop through the retrieved comments
		Iterator it = responseList.iterator();
		List commentList = null;
		while (it.hasNext()) {
			Object response = it.next();
			// if the returned object is a comment
			if(response != null && response instanceof Comments) {
				Comments comments = (Comments) response;
				// get the domainReferenceID of the comment, this maps to workitem id
				// get the workitem from the workItemMap for this domainReferenceID
				Object obj = workItemMap.get(comments.getDomainReferenceId());
				if (obj != null) {
					if (obj instanceof WorkItem) { // it should be a workItem
						WorkItem workobj = (WorkItem) obj; 
						if (null!= defaultId && defaultId.equalsIgnoreCase(workobj.getItemID())) { //PERF-APSL324
							workobj.setWorkType(defaultWorkType); //PERF-APSL324
						} //PERF-APSL324
						commentList = workobj.getComments(); // get the comments list for the workitem
						if (commentList == null) {
							// if the list is null
							commentList = new ArrayList();
						} 
						Comment commentVO = new Comment();
						commentVO.setType(comments.getCommentType());
						commentVO.setCommentType(Comment.COMMENT_TYPE_MANUAL);
						commentVO.setUserID(comments.getUserEntered());
						commentVO.setText(comments.getComments());
						//Begin: NBLXA 2204 One View Sprint 7 change for Bug 301904
						commentVO.setHeading(comments.getHeading());
						commentVO.setSummary(comments.getSummary());
						//End: NBLXA 2204 One View Sprint 7 change for Bug 301904
						commentVO.setContactName(comments.getContactName());
						commentVO.setInstructionType(comments.getInstructionType());
						commentVO.setMailTo(comments.getMailTo());
						commentVO.setRelationship(comments.getRelationship());
						commentVO.setReplyTo(comments.getReplyTo());
						commentVO.setSubject(comments.getSubject());
						commentVO.setDateTime(NbaUtils.getStringFromDate(comments.getDateEntered()));
						commentVO.setDateEntered(comments.getDateEntered());  //ALS3212
						commentVO.setRecordType(comments.getDomainReferenceId().substring(26, 27));
						commentVO.setProcess(comments.getProcess());
						//begin NBA225
						commentVO.setCommentId(comments.getCommentId());
						if (null == commentVO.getCommentId()) {
							commentVO.setCommentId("");
						}	
						commentVO.setUserNameEntered(comments.getUserNameEntered());
						if (null == commentVO.getUserNameEntered()) {
							commentVO.setUserNameEntered("");
						}
						commentVO.setVoidInd(comments.getVoidInd());
						commentVO.setUserNameVoided(comments.getUserNameVoided());
						if (null == commentVO.getUserNameVoided()) {
							commentVO.setUserNameVoided("");
						} 
						commentVO.setUpdated_by(comments.getUpdated_by());
						commentVO.setUpdated_when(NbaUtils.getStringInDateTimeFormatFromDate(comments.getUpdated_when()));//APSL3520(QC13503)						
						//end NBA225
						commentList.add(commentVO);
						workobj.setComments(commentList);
					} 
				}
			}
		}
	}
 
    /**
	 * Prepare the request for retrieving the comments
	 * @param originalDst WorkFlow Hierarchy for which we need to retrieve comments
	 * @param workItemRequest map to cash the workflow Hierarchy
	 * @return List of com.csc.fsg.nba.vo.nbaschema.Comments with domainReferenceID popuated with workItemID
	 * @throws NbaBaseException
	 */
    //PERF-APSL696 changed method signature
    protected List prepareCommentsRequest(NbaDst originalDst, Map workItemMap, String commentTypes) throws NbaBaseException {
        String id; // workItemID (domainReferenceID)
        List commentsRequestList = new ArrayList(); // request List
        Comments comments = new Comments(); //PERF-APSL696
        comments.setCommentType(commentTypes); //PERF-APSL696
        commentsRequestList.add(comments); //PERF-APSL696 
        if (originalDst.isCase()) { // if toplevel workItem is a case
        	// get the topolevel work item
            WorkItem origCase = originalDst.getCase();
            id = originalDst.getID(); // get its workITem ID
            if (id != null && id.length() > 0) {
            	comments = new Comments(); // instantiate new request object
            	// set the domainReference ID key, this will be used in the where clause
            	comments.setDomainReferenceId(id);
            	// add it to the request list
            	commentsRequestList.add(comments);
            	// if there any existing comments cleaning them as they will be retrieved fresh from DB
            	if(origCase.getComments() != null ) {
            		origCase.getComments().clear();
            	}
            	// add this work item to workItemMap, this map will be used for merging the retrieved comments
            	workItemMap.put(id,origCase);
            	// get the child transactions
                Iterator tit = origCase.getWorkItemChildren().iterator();
                while (tit.hasNext()) {
                	// looping through the child transactions
                    WorkItem transaction = (WorkItem) tit.next();
                    // Get the workItem id of the transaction
                   	id = transaction.getItemID();
                    if (id != null && id.length() > 0) { // if id exists 
                    	// instantiate new request object
                    	comments = new Comments();
                    	// set the domainreferenceid as workitem id
                    	comments.setDomainReferenceId(id);
                    	// add the requst object to thelist
                    	commentsRequestList.add(comments);
                    	// removing any preexisting comments from the transaction workitem
                    	if( transaction.getComments() != null) {
                    		transaction.getComments().clear();
                    	}
                    	// add transaction to the workItem map, this map will be used for merging the retrieved comments
                    	workItemMap.put(id,transaction);
                    }
                }
            }
        } else { // if toplevel workItem is a transaction
            id = originalDst.getID();
            // get the id of the transaction
            if (id != null && id.length() > 0) {
            	// if id exists prepare request
            	comments = new Comments();
            	comments.setDomainReferenceId(id);
            	commentsRequestList.add(comments);
            	// cleaning all the existing comments on the transaction as they will be retrieved fresh from DB
            	if( originalDst.getTransaction().getComments() != null ) {
            		originalDst.getTransaction().getComments().clear();
            	}
            	workItemMap.put(id,originalDst.getTransaction());
            }
        }
        return commentsRequestList;
    }
    /**
     * Retrieve the children for a work item. This will be read only and a lock is not obtained
     * @param nbaDst
     * @return the updated DST
     */
    //NBA208-36 New Method
    protected AccelResult getChildrenWorkItems(NbaDst nbaDst){
    	
    	NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.setWorkItem(nbaDst.getID(), true);
		options.requestTransactionAsChild();
		NbaUserVO user = new NbaUserVO();
		user.setUserID(getCurrentUserId());
		options.setRetrieveLOBIndicator(false);  //PERF-APSL324
		options.setNbaUserVO(user);
    	AccelResult result =  (AccelResult) callBusinessService("NbaRetrieveWorkBP", options);
    	return result;
    }
}
