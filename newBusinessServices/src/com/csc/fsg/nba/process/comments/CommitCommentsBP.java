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
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.util.GUIDFactory;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaWorkItem;
import com.csc.fsg.nba.vo.nbaschema.Comments;

/**
 * Stores the comments added to workflow value object <code>NbaDst</code> to Comments table.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-38</td><td>Version 7</td><td>Performance Tuning and Testing - Comments in Database</td></tr>
 * <tr><td>NBA225</td><td>Version 8</td><td>Comments</td></tr>
 * <tr><td>ALS3212</td><td>AXA Life Phase 1</td><td>QC# 1850  - Sort order of comments is unclear and not correct</td></tr>
 * <tr><td>ALS5881</td><td>AXA Life Phase 1</td><td>QC #5053 - Prod issue #3222: Comments different based on work item selected, dates on some comments are changed based on work item selected</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CommitCommentsBP extends NewBusinessAccelBP {
	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    List updateList = new ArrayList(); //NBA225
	List insertList = new ArrayList(); //NBA225
	
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		NbaDst dst = null; 
		try {
			dst = (NbaDst) input; 
			result.merge(update(dst.getNbaUserVO(), dst));
		} catch (Exception e) {
			addExceptionMessage(result, e);
			result.addResult(e);
			return result;			
		} 
		return result;
	}

	/**
	 * Looks for any new comments added to a work item or child work items.  Updates the
	 * comments using the hibernate/UpdateComments service.
	 * @param userVO
	 * @param nbaDst
	 * @return
	 * @throws NbaBaseException
	 */
	protected Result update(NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException {
        try {      
        	// NBA225 Code Deleted        
            if (nbaDst.isCase()) { // if the toplevel workitem is a case
            	// get the toplevel case workitem
                NbaCase nbaCase = nbaDst.getNbaCase(); 
                // get all the transactions
                List listOfNbaTransactions = nbaDst.getNbaTransactions(); 
                // get the list of comments to be stored from case workitem
                addToUpdateOrInsertList(nbaCase, userVO); //NBA225 
                // loop through all the transaction
                Iterator itr = listOfNbaTransactions.iterator(); 
                while (itr.hasNext()) {
                    // get the list of comments to be stored from each of the transaction workitem                	
                    addToUpdateOrInsertList((NbaWorkItem) itr.next(), userVO); //NBA225
                }
            } else if (nbaDst.isTransaction()) { // if the toplevel work item is a transaction
                // get the list of comments to be stored from the transaction workitem            	
                addToUpdateOrInsertList(nbaDst.getWork(), userVO); //NBA225
            }
            // call the UpdateComments Service. This service is hibernate based service.
            Result result = null;
            if(updateList.size() > 0) {
            	result = invoke("hibernate/UpdateComments", updateList);
                if(!result.hasErrors()) {
                	// if commit is successful, clear the results as they are com.csc.fsg.nba.vo.nbaschema.Comments records for the comments just commited. 
                	result.clear();
                }
                return result;

            } 
            //begin NBA225
            //call the InsertComments Service. This service is a hibernate based service.
            if (insertList.size() > 0) {
            	result = invoke("hibernate/InsertComments", insertList);
                if(!result.hasErrors()) {
                	// if commit is successful, clear the results as they are com.csc.fsg.nba.vo.nbaschema.Comments records for the comments just commited. 
                	result.clear();
                }
                return result;
            }            
            //end NBA225
            return new AccelResult();
        } catch (NbaBaseException e) {
	        LogHandler.Factory.LogError(this, e.getMessage());
            e.forceFatalExceptionType();	
            throw e;
        } catch (Throwable t) {             
            NbaBaseException e = new NbaBaseException(NbaBaseException.UPDATE_WORK + " " + t.getClass().getName(), t, NbaExceptionType.FATAL);	//SPR3332 NBA213
         	LogHandler.Factory.LogError(this, e.getMessage());
            throw e;
        }
    }

	//NBA225 Code deleted
	
	/**
	 * Iterates thru the manual comments on the workItem to collect all new comments
	 * and add them to the updateList or insertList.
	 * @param workItem
	 * @param userVO
	 */
    //NBA225 New Method
    protected void addToUpdateOrInsertList(NbaWorkItem workItem, NbaUserVO userVO){
    	// get the list of all the manual comments on the workItem
        List listOfComments = workItem.getManualComments();
        // looping through the comments to find out if they are to be stored        
        Iterator itr = listOfComments.iterator();
        while (itr.hasNext()) {
            Comment comment = (Comment) itr.next();
            // if the comment has Action "A" and the Comments Text is not null and void indicator == 1; add it to the updateList            
            if (Comment.COMMENT_ACTION_ADD.equalsIgnoreCase(comment.getAction()) && comment.getText() != null && 
                comment.getVoidInd() == 1 ) {
            	Comments comments = new Comments();
            	// primary Key is the same as the comment before it was voided to allow an update of the existing comment. 
            	comments.setCommentId(comment.getCommentId());
            	comments.setComments(NbaUtils.filterNonUTF8Chars(comment.getText()));//APSL841
            	// DomainReference Id maps to the workitem id
            	comments.setDomainReferenceId(workItem.getID());
            	comments.setCommentType(comment.getType());
            	comments.setContactName(comment.getContactName());
            	comments.setInstructionType(comment.getInstructionType());
            	comments.setMailTo(comment.getMailTo());
            	comments.setRelationship(comment.getRelationship());
            	comments.setReplyTo(comment.getReplyTo());
            	comments.setSubject(comment.getSubject());
            	// Domain Source is NBA, other Domain Source can be other applications
            	// TODO: should this be stored in some ACCEL Configuration file? 
            	comments.setDomainSource("NBA");
            	comments.setStickyFlag("N");
            	comments.setProcess(comment.getProcess());
            	//begin ALS4752
            	if( !NbaUtils.isBlankOrNull(comment.getUserID())){
            	    comments.setUserEntered(comment.getUserID());
            	}else{
            	    comments.setUserEntered(userVO.getUserID());
            	}    
            	//end ALS4752 
            	comments.setUpdated_by(userVO.getUserID());
            	//TODO: Ask Hibernate to update it with SYSDATE instead of taking the timestamp from JAVA
            	java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());  //ALS3212
            	comments.setUpdated_when(date);
            	//Begin ALS5881
            	if (comment.getDateEntered() == null) {
					comments.setDateEntered(date);
				} else {
					comments.setDateEntered(new java.sql.Timestamp(comment.getDateEntered().getTime()));
				}            	
            	//End ALS5881
            	comments.setUserNameEntered(comment.getUserNameEntered());
            	comments.setVoidInd(comment.getVoidInd());
            	comments.setUserNameVoided(comment.getUserNameVoided());
            	//NBLXA 2204 Sprint 7 One View
            	comments.setHeading(comment.getHeading());
            	comments.setSummary(comment.getSummary());
            	//NBLXA 2204 Sprint 7 One View
            	updateList.add(comments);
            }
            // if the comment has Action "A" and the Comments Text is not null; add it to the insertList.            
            else if (Comment.COMMENT_ACTION_ADD.equalsIgnoreCase(comment.getAction()) && comment.getText() != null && 
                     comment.getVoidInd() != 1 ) {
            	Comments comments = new Comments();
            	// primary Key is GUID since that needs to be unique. 
            	comments.setCommentId(GUIDFactory.getIdHexString());
            	comments.setComments(NbaUtils.filterNonUTF8Chars(comment.getText()));//APSL841
            	// DomainReference Id maps to the workitem id
            	comments.setDomainReferenceId(workItem.getID());
            	comments.setCommentType(comment.getType());
            	comments.setContactName(comment.getContactName());
            	comments.setInstructionType(comment.getInstructionType());
            	comments.setMailTo(comment.getMailTo());
            	comments.setRelationship(comment.getRelationship());
            	comments.setReplyTo(comment.getReplyTo());
            	comments.setSubject(comment.getSubject());
            	// Domain Source is NBA, other Domain Source can be other applications
            	// TODO: should this be stored in some ACCEL Configuration file? 
            	comments.setDomainSource("NBA");
            	comments.setStickyFlag("N");
            	comments.setProcess(comment.getProcess());
            	//begin ALS4752
            	if( !NbaUtils.isBlankOrNull(comment.getUserID())){
            	    comments.setUserEntered(comment.getUserID());
            	}else{
            	    comments.setUserEntered(userVO.getUserID());
            	}    
            	//end ALS4752 
            	if(!NbaUtils.isBlankOrNull(comment.getUserNameEntered())){
            	    comments.setUserNameEntered(comment.getUserNameEntered());
            	}else{
            	    comments.setUserNameEntered(userVO.getFullName());
            	}
            	comments.setUpdated_by(userVO.getUserID());
            	//TODO: Ask Hibernate to update it with SYSDATE instead of taking the timestamp from JAVA
            	java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());  //ALS3212
            	comments.setUpdated_when(date);
            	//Begin ALS5881
            	if (comment.getDateEntered() == null) {
					comments.setDateEntered(date);
				}else{
					comments.setDateEntered(new java.sql.Timestamp(comment.getDateEntered().getTime()));
				}
            	//End ALS5881
            	//NBLXA 2204 Sprint 7 One View
            	comments.setHeading(comment.getHeading());
            	comments.setSummary(comment.getSummary());
            	//NBLXA 2204 Sprint 7 One View
            	insertList.add(comments);
	        }
        }
    }
    
}
