/*************************************************************************
*
* Copyright Notice (2006)
* (c) CSC Financial Services Limited 1996-2006.
* All rights reserved. The software and associated documentation
* supplied hereunder are the confidential and proprietary information
* of CSC Financial Services Limited, Austin, Texas, USA and
* are supplied subject to licence terms. In no event may the Licensee
* reverse engineer, decompile, or otherwise attempt to discover the
* underlying source code or confidential information herein.
*
*************************************************************************/

package com.csc.fsg.nba.process.product;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.constants.FA.ServiceCatalog;
;

public class NbaProductInquiryBP extends AccelBP {

	public Result process(Object request) {
		Result result =
			callService(ServiceCatalog.PRODUCT_INQUIRY_DISASSEMBLER, request);
	
		if (!result.hasErrors()) {	
			result = callService(com.csc.fs.accel.constants.product.ServiceCatalog.PRODUCT_INQUIRY, result);
		}
		if (!result.hasErrors()) {
			result = callService(ServiceCatalog.PRODUCT_INQUIRY_ASSEMBLER, result);
		} else {
			result.getData().clear();
		}

		return result;
	
	}
}