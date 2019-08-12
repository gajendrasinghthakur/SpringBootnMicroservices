package com.csc.fsg.nba.process.requirements;

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

import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fsg.nba.process.NewBusinessAccelBP;

/**
 * Retrieves requirement Status details from TXLife.
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

public class RetrieveReqStatusBP extends NewBusinessAccelBP {

    public Result process(Object input) {
        Result result = callService(ServiceCatalog.RETRIEVE_REQ_STATUS_DISASSEMBLER, input);
        if (!result.hasErrors()) {
            result = callService(ServiceCatalog.RETRIEVE_REQ_STATUS_ASSEMBLER, result);
        } else {
            result.getData().clear();
        }
        return result;
    }
}
