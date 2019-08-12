package com.csc.fsg.nba.assembler.requirements;

/*
 * *******************************************************************************<BR>
 * Copyright 2016, Computer Sciences Corporation. All Rights Reserved.<BR>
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaEntityResolver;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaRequirementStatusVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.RequirementStatusTableVO;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.tbf.xml.XmlElement;
import com.tbf.xml.XmlParser;

 /**
 * RetrieveReqStatusAssembler, retrieves requirement status information from the NbaTXLife and populates the
 * NbaRequirementStatusVO value object with the data.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBLXA-1656</td><td>Version NB-1501</td><td>nbA Requirement Order Statuses from Third Party Providers</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1601
 * @since New Business Accelerator - Version NB-1501
 */
public class RetrieveReqStatusAssembler extends AccelTransformation {
	
	protected static final String TXLIFE_START_TAG = "<TXLife";
	protected static final String TXLIFE_END_TAG = "</TXLife>";
	
    /**
     * This method returns the result of the process
     * @param result
     * @return a result object is returned
     * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)
     */
    public Result assemble(Result request) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(retrieveReqStatus((NbaRequirementStatusVO) request.getFirst()));
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
     */
    public Result disassemble(Object request) {
        Result result = new AccelResult();
        result.addResult(request);
        return result;
    }

    /**
     * Calls the retrieveReqStatusTableInfo method and then populates the request with the statusList
     * @param request
     * @return NbaRequirementStatusVO
     * @throws ParseException 
     */
    protected NbaRequirementStatusVO retrieveReqStatus(NbaRequirementStatusVO request) throws ParseException { //NBLXA-1798
        List<RequirementStatusTableVO> status = retrieveReqStatusTableInfo(request);
        request.setReqStatDetails(status);
        return request;
    }


    /**
     * populates the reqStatusVO object by calling the retrieveReqObjects method and then add the VO to the list.
     * the reqStatusVO is populated for the selected requirement and when the attachmentType is 462
     * @param NbaRequirementStatusVO
     * @return ArrayList
     * @throws ParseException 
     */
    protected List<RequirementStatusTableVO> retrieveReqStatusTableInfo(NbaRequirementStatusVO request) throws ParseException { //NBLXA-1798
    	List<RequirementStatusTableVO> reqStatusVO = null;
    	int attachmentCount = 0;
    	StringBuilder attachment;
        List<RequirementStatusTableVO> reqStatusList = new ArrayList<RequirementStatusTableVO>();
        Policy policy = request.getNbaTXLife().getPolicy();
        int count = policy.getRequirementInfoCount();
        RequirementInfo reqInfo = null;
        RequirementInfoExtension reqExtn = null;
        TrackingInfo trackingInfo = null;
        for (int i = 0; i < count; i++) {
        	reqInfo = policy.getRequirementInfoAt(i);
        	//checks if the current requirement id matches with the requirement ID in session.
            if(reqInfo.getRequirementInfoUniqueID().equals(request.getReqUniqueId())) {
            	attachmentCount = reqInfo.getAttachmentCount();
            	for (int j = 0; j < attachmentCount; j++) {
            		//the reqStatusVO will be populated only when attachmentType is 462
            		if (reqInfo.getAttachmentAt(j).getAttachmentType() == NbaOliConstants.OLI_ATTACH_STATUSCHG) {
            			//The reqExtn and trackingInfo are retrieved for getting the translated value from MDB by calling 
            			//appropriate decode method in the service action
            			reqExtn = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
                        trackingInfo = reqExtn.getTrackingInfo();
						attachment = new StringBuilder(reqInfo.getAttachmentAt(j).getAttachmentData().getPCDATA());
						reqStatusVO = retrieveReqObjects(attachment, request, trackingInfo);
						if (reqStatusVO != null) {
	                     	reqStatusList.addAll(reqStatusVO);
	                     }
					}
				}
            }
        }
        return reqStatusList;
    }


    /**
     * This method will firstly populate the keys in the VO object from trackingInfo and WorkValues
     * Then it will populate the VO with the other status Event values
     * @param reqInfoFromAttachment
     * @param reqStatusVO
     * @param request
     * @param trackingInfo 
     * @return RequirementStatusTableVO
     * @throws ParseException 
     */
    private RequirementStatusTableVO populateVOObject(RequirementInfo reqInfoFromAttachment, RequirementStatusTableVO reqStatusVO, NbaRequirementStatusVO request, TrackingInfo trackingInfo, int i, String vendorCode ) throws ParseException { //NBLXA-1798
    	NbaLob nbaLob = request.getNbaDst().getNbaLob();
    	
    	Map<String, String> keys = new HashMap<String, String>();
    	if (trackingInfo != null) {
    		keys.put(NbaTableAccessConstants.C_SYSTEM_ID, trackingInfo.getTrackingServiceProvider());
    	} else {
    		keys.put(NbaTableAccessConstants.C_SYSTEM_ID, request.getNbaTXLife().getBackendSystem());
    	}
 	    
 	    keys.put(NbaTableAccessConstants.C_COMPANY_CODE, nbaLob.getCompany());
 	    keys.put(NbaTableAccessConstants.C_COVERAGE_KEY, nbaLob.getPlan());
 	    reqStatusVO.setKeys(keys);
		reqStatusVO.setStatus(reqInfoFromAttachment.getReqStatus());
		reqStatusVO.setStatusDate(reqInfoFromAttachment.getStatusDate());
		reqStatusVO.setReqSubStatus(reqInfoFromAttachment.getReqSubStatus());
		reqStatusVO.setVendorCode(vendorCode);
		reqStatusVO.setStatusEventCode(reqInfoFromAttachment.getStatusEventAt(i).getProviderEventCode());
		// NBLXA-1798 Starts
		Date statusdate = reqInfoFromAttachment.getStatusEventAt(i).getStatusEventDate();
		String statusdatestring = NbaUtils.getStringFromDate(statusdate);
		NbaTime statustime = reqInfoFromAttachment.getStatusEventAt(i).getStatusEventTime();
		String statustimeString = String.valueOf(statustime);
		String statusEventdatestring = statusdatestring + " " + statustimeString;
		Date statusEventdate = NbaUtils.getDateFromStringDateAndTimeFormat(statusEventdatestring);
		reqStatusVO.setStatusEventDate(statusEventdate);
		// NBLXA-1798 Ends
		reqStatusVO.setStatusEventDetails(reqInfoFromAttachment.getStatusEventAt(i).getStatusEventDetail());
    	return reqStatusVO;
	}

	/**
     * Create the RequirementStatusTableVO Value object
     * @return RequirementStatusTableVO
     */
    protected RequirementStatusTableVO createTableValueObject() {
        return new RequirementStatusTableVO();
    }
    
    /**
     * Retrieves the requirement objects one by one from the attachment and parses the requirementInfo
     * object and then calls the appropriate method populate the VO
     * @param attachment
     * @param request
     * @param trackingInfo 
     * @return RequirementStatusTableVO
     * @throws ParseException 
     */
    protected List<RequirementStatusTableVO> retrieveReqObjects(StringBuilder attachment, NbaRequirementStatusVO request, TrackingInfo trackingInfo) throws ParseException { //NBLXA-1798
    	int reqInfoStart = 0;
    	int reqInfoEnd = 0;
    	String reqInfoString = null;
    	List<RequirementStatusTableVO> reqStatusVOList = new ArrayList<RequirementStatusTableVO>();
    	RequirementStatusTableVO reqStatusVO = null;
    	reqInfoStart = attachment.indexOf(TXLIFE_START_TAG);
		reqInfoEnd = attachment.indexOf(TXLIFE_END_TAG) + TXLIFE_END_TAG.length(); //added 18 to include </RequirementInfo> in substring
		if (reqInfoStart > NbaConstants.LONG_NULL_VALUE) {
			reqInfoString = attachment.substring(reqInfoStart, reqInfoEnd);
			if (!NbaUtils.isBlankOrNull(reqInfoString)) {
				//set the attachment again with the remaining requirementInfo objects removing the current requirementInfo object 
				attachment = new StringBuilder(attachment.substring(reqInfoEnd)); 
								
				NbaTXLife nbaTxLife = new NbaTXLife();
				try {
					nbaTxLife = new NbaTXLife(reqInfoString);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//begin NBLXA-1656 
				RequirementInfo reqInfoFromAttachment =null;
				for(int i= 0; i<nbaTxLife.getPolicy().getRequirementInfoCount(); i++){
					reqInfoFromAttachment =nbaTxLife.getPolicy().getRequirementInfoAt(i) ;
					String vendorCode = nbaTxLife.getUserAuthRequest().getVendorApp().getVendorName().getVendorCode();
					int statusCount =reqInfoFromAttachment.getStatusEventCount();
					//the requirement info Id retrieved from attachment should match the requirement id in request
					//if(!NbaUtils.isBlankOrNull(reqInfoFromAttachment.getReqCode()) && reqInfoFromAttachment.getReqCode()==NbaOliConstants.OLI_REQCODE_PHYSSTMT && reqInfoFromAttachment .getRequirementInfoUniqueID().equals(request.getReqUniqueId())) { //NBLXA-1777
					if(reqInfoFromAttachment .getRequirementInfoUniqueID().equals(request.getReqUniqueId())) { //NBLXA-1777
						for (int j = 0; j < statusCount; j++) {
							reqStatusVO = createTableValueObject();
							reqStatusVO = populateVOObject(reqInfoFromAttachment,reqStatusVO,request,trackingInfo, j, vendorCode);
							if (reqStatusVO != null) {
								reqStatusVOList.add(reqStatusVO);
							}
						}// End NBLXA-1656
						return reqStatusVOList;
					} 
				}					
			}
		}
		return reqStatusVOList;
    }
    
}
