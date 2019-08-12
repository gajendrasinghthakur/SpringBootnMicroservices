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

import java.util.Iterator;
import java.util.List;

import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.ResultImpl;
import com.csc.fs.dataobject.accel.workflow.FileItem;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * GetItemPostProcessor contains Image Retrieval post-process logic 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */
 
public class GetImagePostProcessor extends RestPostProcessor {
    /**
     * System API post-process logic for Create Work responses
     * 
     */
    public Result systemApi(List input, Result result, SystemService service, SystemAPI api, ObjectRepository or) {
        Iterator it = result.getData().iterator();
        while (it.hasNext()){
            Object obj = it.next();
            if (obj instanceof ResultImpl){
                ResultImpl resultImpl = (ResultImpl) obj;
                Iterator it2 = resultImpl.getData().iterator();
                while (it2.hasNext()){
                    Object obj2 = it2.next();
                    if (obj2 instanceof FileItem){
                        FileItem fileItem = (FileItem) obj2;
                        if (fileItem.getImage() != null){
                            String image = fileItem.getImage();
                            if (image.startsWith("<Error><code>")){
                                result.setErrors(true);
                                
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Post-process logic for system service.
     *
     */
    public Result systemService(List input, Result result, SystemService service, ObjectRepository or) {
        return Result.Factory.create();
    }
}
