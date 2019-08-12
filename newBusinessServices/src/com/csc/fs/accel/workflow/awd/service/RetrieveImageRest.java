package com.csc.fs.accel.workflow.awd.service;

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

import com.csc.fs.Result;
import com.csc.fs.accel.AccelService;

/**
 * Retrieve an Image binary using Rest
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr>
 * <tr><td>NBA331.1</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */
public class RetrieveImageRest extends AccelService {
    /**
     * Retrieve the Image data and apply Base64 encoding
     * 
     * @param request
     * @return a Result containing all the requested objects.
     */
    public Result execute(Result request) {
        return invoke("AWD/GetImage", request.getData(), true);
    }
}
