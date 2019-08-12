package com.csc.fsg.nba.process.correspondence;

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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapter;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapterFactory;
import com.csc.fsg.nba.correspondence.docprintschema_extract.Correspondence;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaCorrespondenceRequestVO;
import com.csc.fsg.nba.vo.NbaCorrespondenceResponseVO;


/**
 * Creates the correspondence extract and the keys to the extract
 * Accepts a <code>NbaCorrespondenceRequestVO</code> as input to create a correspondence extract
 * The dst, letter, and NbaUserVO are required on the value object.
 * Returns a <code>NbaCorrespondenceResponseVO </code> with the extract and keys poulated
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CreateCorrespondenceExtractBP extends NewBusinessAccelBP {
	private NbaLogger logger;
    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            NbaCorrespondenceRequestVO corrRequestVO = (NbaCorrespondenceRequestVO) input;
            NbaCorrespondenceResponseVO corrResponseVO = new NbaCorrespondenceResponseVO();
            NbaCorrespondenceAdapter adapter = new NbaCorrespondenceAdapterFactory().getAdapterInstance();
          //APSL5200 : Start
          //  adapter.initializeObjects(corrRequestVO.getDst(), corrRequestVO.getNbaUserVO());
            adapter.initializeObjects(corrRequestVO);
          //APSL5200 :End
            adapter.setParentDst(corrRequestVO.getDst()); //ALS4476
            //Add the correspondence extract as the first result back
            String extract = adapter.createExtract(corrRequestVO.getLetter());
            corrResponseVO.setXmlExtract(extract);
            Correspondence corrExtract = Correspondence.unmarshal(new ByteArrayInputStream(extract.getBytes()));
            corrResponseVO.setKeys(createKeys(corrExtract));
            result.addResult(corrResponseVO);
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }
	/**
	 * This method returns the xPression customer Keys
	 * @return a string of key/value pairs similar to &Custkeys1=DVU00000000190V&Custkeys2=VCS
	 * @param Correspondence extract 
	 */
  	protected String createKeys(Correspondence corrExtract) {
  		StringBuffer strBuffer = new StringBuffer();
        try {

            List keys = NbaConfiguration.getInstance().getXPression().getCustKey();
            Class[] emptyParms = {}; //Empty parms for reflexion call
            for (int i = 0; i < keys.size(); i++) {
                strBuffer.append("&Custkeys");
                strBuffer.append(i + 1);
                strBuffer.append("=");
                String methodName = keys.get(i).toString();
                //Pointer to the method
                Method method = corrExtract.getClass().getDeclaredMethod(("get" + methodName), emptyParms);
               //Execute the method
                strBuffer.append(method.invoke(corrExtract, emptyParms));
            }
        } catch (Exception e) {
            getLogger().logError("Error setting up xPression keys " + e);
        }
        return strBuffer.toString();
    }
  	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private NbaLogger getLogger() {
		//private NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger("NbaTestWebService");
			} catch (Exception e) {
				NbaBootLogger.log("NbaTestWebService could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
