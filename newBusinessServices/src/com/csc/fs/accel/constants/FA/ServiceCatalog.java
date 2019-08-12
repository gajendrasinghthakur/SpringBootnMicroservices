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
package com.csc.fs.accel.constants.FA;

/**
 * List of all registered base services
 */
public interface ServiceCatalog {
    	
	//File Access

	public static String FILEACCESSASSEMBLER = "FA/assembler/FileAccessAssembler";	//NBA188
	public static String FILEACCESSDISASSEMBLER = "FA/assembler/FileAccessDisAssembler";	//NBA188	
	public static String FILEACCESS = "FA/service/FileAccess";	//NBA188
	
	//NBA237 Policy Product Access
	public static String PRODUCT_INQUIRY_ASSEMBLER = "product/assembler/NbaProductInquiryAssembler"; //NBA237
	public static String PRODUCT_INQUIRY_DISASSEMBLER = "product/assembler/NbaProductInquiryDisAssembler"; //NBA237
	
}