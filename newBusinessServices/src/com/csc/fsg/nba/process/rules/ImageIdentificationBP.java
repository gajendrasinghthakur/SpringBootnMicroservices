package com.csc.fsg.nba.process.rules;

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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaImagesVo;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaViewImagesVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * This class takes the NbaViewImagesVO object as an input and then calls the workItemIdentification vp/ms model and gets the 
 * descriptive information of workitems and source and these descriptive information will be set to NbaImagesVo object and these objects 
 * will be returned in the form of Map. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA227</td><td>Version 8</td><td>Selection List of Images to Display</td></tr>
 * <tr><td>SPR1362</td><td>Version 8</td><td>Values used for VPMS models (entry points, attributes) should be constant values.</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 6
 */
public class ImageIdentificationBP extends WorkItemIdentificationBP {
	
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaViewImagesVO viewImageVo = (NbaViewImagesVO) input;
			processSources(viewImageVo);
			result.addResult(viewImageVo);
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}
	
	/**
	 * Set up LOB values to be mapped into VP/MS model workitemidentification, set entry point,
	 * invoke the model, then invoke attribute translation of the sections of the returned 
	 * string that require it, and build return string with translated values.
	 * @param source a WorkItemSource relating to an AWD image
	 * @param vpmsProxy NbaVpmsAdaptor for WorkItemIdentification model
	 * @return a string (delimited by NbaVpmsConstants.VPMS_DELIMITER[1]) containing the translated image attributes 
	 * @throws NbaBaseException
	 */
	protected NbaImagesVo processWorkItemIdentificationModel(WorkItem workItem, WorkItemSource source, NbaVpmsAdaptor vpmsProxy)
			throws NbaBaseException {
		NbaImagesVo imagesVo = null;
		NbaLob lob = new NbaLob(workItem.getLobData());
		NbaLob sourceLob = new NbaLob(source.getLobData());
		lob.setBusinessArea(workItem.getBusinessArea());
		lob.setCreateDate(source.getCreateDateTime());
		String sourceType = source.getSourceType();
		//NBLXA-1328 Starts
		if (!NbaUtils.isBlankOrNull(sourceLob.getLetterType()) && sourceType.equalsIgnoreCase(NbaConstants.A_ST_CORRESPONDENCE_LETTER))
		{
			lob.setLetterType(sourceLob.getLetterType());
		}
		//NBLXA-1328 Starts
		Map deOinkMap = new HashMap();
		if (NbaConstants.A_ST_MISC_MAIL.equals(sourceType) || NbaConstants.A_ST_APPLICATION.equals(sourceType)
				|| NbaConstants.A_ST_REPLACEMENT_EXCHANGE.equals(sourceType) || NbaConstants.A_ST_FORMS.equals(sourceType)) {
			deOinkMap.put("A_FormNumberLOB", sourceLob.getFormNumber() == null ? "" : sourceLob.getFormNumber()); //APSL674
		}
		NbaOinkDataAccess data = new NbaOinkDataAccess();
		data.setLobSource(lob);		
		deOinkMap.put(NbaVpmsConstants.A_SOURCE_TYPE, sourceType); //SPR1362
		deOinkMap.put("A_CaseIDLOB", lob.getProviderOrder() == null ? "" : lob.getProviderOrder()); //ALII1818, APSL3234
		try {
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_IMAGESINFO);
			vpmsProxy.setOinkSurrogate(data);
			vpmsProxy.setSkipAttributesMap(deOinkMap);
			VpmsComputeResult result = vpmsProxy.getResults();
			if (null != result && result.getReturnCode() == 0) {
				imagesVo = processResult(lob, result);
			}
			return imagesVo;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		}
	}
	
	/**
	 * Process the result return from vp/ms model. 
	 * @param lob - workitem lobs
	 * @param result - vpms result
	 * @return value object
	 * @throws NbaBaseException
	 */
	private NbaImagesVo processResult(NbaLob lob, VpmsComputeResult result) throws NbaBaseException {
		NbaImagesVo imagesVo;
		String[] tokens = result.getResult().trim().split(NbaVpmsConstants.VPMS_DELIMITER[1]);
		imagesVo = new NbaImagesVo();
		if(tokens !=null){
			if(tokens.length > 0){
				imagesVo.setImageType(getTranslatedValue(tokens[0], lob, false));
			}
			if(tokens.length > 1){
				imagesVo.setPerson(tokens[1]);
			}
			if(tokens.length > 2){
				imagesVo.setDate(tokens[2]);
			}
			if(tokens.length > 3){
				StringBuffer resultString = new StringBuffer();
				for(int i=3;i<tokens.length;i++){					
					resultString.append(getTranslatedValue(tokens[i], lob, true));					
				}
				imagesVo.setDescription(resultString.toString());				
			}
		}
		return imagesVo;
	}

	/**
	 * Calls the method processWorkIdentification for each source.
	 * @param viewImageVo Value object.
	 * @throws NbaBaseException
	 */
	protected void processSources(NbaViewImagesVO viewImageVo) throws NbaBaseException {
		List sources = viewImageVo.getSourcesList();
		Map workItemMap = viewImageVo.getWorkItemMap();
		Map workItemDescMap = new HashMap();
		NbaVpmsAdaptor vpmsProxy = null;
		WorkItem workItem;
		WorkItemSource source;
		Iterator iterator = sources.iterator();//ALII1388
		try {
		vpmsProxy = new NbaVpmsAdaptor(NbaVpmsAdaptor.WORKITEMIDENTIFICATION);
		while (iterator.hasNext()) {
					source = (WorkItemSource) iterator.next();
					String mapKey = source.getParentWorkItemID() + "#" + source.getItemID();//ALS4809
					workItem = (WorkItem) workItemMap.get(mapKey); //ALS4809
					NbaImagesVo imageVo = processWorkItemIdentificationModel(workItem, source, vpmsProxy);
					//Begin QC#7517
					if (new NbaLob(source.getLobData()).getReqType() == NbaOliConstants.OLI_REQCODE_1009800033
							&& new NbaLob(workItem.getLobData()).getReqType() != NbaOliConstants.OLI_REQCODE_1009800033) {
						iterator.remove(); //ALII1388
						continue;
					}
					//End QC#7517
					//ALS4809 begin
					if (workItemDescMap.containsKey(source.getItemID())) {
						NbaImagesVo imageVoFromMap = (NbaImagesVo) workItemDescMap.get(source.getItemID());
						StringBuffer imageDesc = new StringBuffer(imageVoFromMap.getDescription());
						imageDesc.append("\n");
						imageDesc.append(imageVo.getDescription());
						imageVoFromMap.setDescription(imageDesc.toString());
						iterator.remove();
					} else {
						workItemDescMap.put(source.getItemID(), imageVo);
					}

					//ALS4809 end
			
				}
		
				viewImageVo.setWorkItemDescMap(workItemDescMap);
		
			} finally {
				try {
					if (vpmsProxy != null) {
						vpmsProxy.remove();
						vpmsProxy = null;
					}
				} catch (RemoteException e) {
					getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
	}

