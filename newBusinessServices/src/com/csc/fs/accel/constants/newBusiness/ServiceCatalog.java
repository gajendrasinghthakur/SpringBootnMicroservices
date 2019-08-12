package com.csc.fs.accel.constants.newBusiness;
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
 * 
 * *******************************************************************************<BR>
 */

/**
 * List of all registered new business services
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA188</td><td>Version 7</td><td>XML sources to NBAAUXILIARY</td></tr>
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * <tr><td>NBA234</td><td>Version 8</td><td>ACORD Transformation Service</td></tr>
 * <tr><td>NBA235</td><td>Version 8</td><td>Migrate MIB XML401 and 402 to 2.15.00</td></tr>
 * <tr><td>NBA236</td><td>Version 8</td><td>Migrate 121 and 1122 Transactions to Acord Level 2.15.00</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>APSL4236/QC15038</td><td>Discretionary</td><td>AWD 10 locking issue</td></tr>
 * <tr><td>NBLXA-1656</td><td>Version NB-1501</td><td>nbA Requirement Order Statuses from Third Party Providers</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public interface ServiceCatalog {
    	
	//File Access

	public static String FILEACCESSASSEMBLER = "FA/assembler/FileAccessAssembler";	 
	public static String FILEACCESSDISASSEMBLER = "FA/assembler/FileAccessDisAssembler";	 
	public static String FILEACCESS = "FA/service/FileAccess";	 
	
	//begin NBA234
	//Test Web Service

	public static String TEST_REQUEST_ASSEMBLER = "newBusiness/assembler/TestRequestTransformAssembler";
	public static String TEST_REQUEST_DISASSEMBLER = "newBusiness/assembler/TestRequestTransformDisAssembler";	
	public static String TEST_REQUEST_TRANSFORM = "service/TestRequestTransform";

	public static String TEST_RESPONSE_ASSEMBLER = "newBusiness/assembler/TestResponseTransformAssembler";
	public static String TEST_RESPONSE_DISASSEMBLER = "newBusiness/assembler/TestResponseTransformDisAssembler";	

	//ACORD Transformation
	
	public static String TXLIFE_ASSEMBLER = "ACORD/assembler/TXLifeAssembler";
	public static String TXLIFE_DISASSEMBLER = "ACORD/assembler/TXLifeDisAssembler";
	public static String TXLIFE_TRANSFORM_OUTBOUND = "ACORD/TransformOutbound";
	public static String TXLIFE_TRANSFORM_INBOUND = "ACORD/TransformInbound";
	//end NBA234
	
	//begin NBA236
	//Requirement Request/Results
	public static String SENDTXLIFE121 = "ACORD/service/SendTXLife121";   
	public static String TXLIFE121_ASSEMBLER = "ACORD/assembler/TXLife121Assembler";
	public static String TXLIFE121_DISASSEMBLER = "ACORD/assembler/TXLife121DisAssembler";
	public static String RECEIVETXLIFE1122 = "ACORD/service/ReceiveTXLife1122";	  
	//end NBA236

	//MIB Services
	public static String SEND_TX401 = "ACORD/service/SendTXLife401";  //NBA235
	public static String SEND_TX402 = "ACORD/service/SendTXLife402";  //NBA235

	//Contract Print
	public static String SENDTXLIFE500 = "ACORD/service/SendTXLife500";	//NBA237	

	//begin NBA232
	//Data Feed Web Service
	public static String DATA_FEED_REQUEST_REQUEST_ASSEMBLER = "datafeed/assembler/NbaDataFeedRequestTransformAssembler";
	public static String DATA_FEED_REQUEST_TRANSFORM_DISASSEMBLER = "datafeed/assembler/NbaDataFeedRequestTransformDisAssembler";	
	public static String DATA_FEED_TRANSFORM = "service/DataFeedRequestTransform";
	
	public static String DATA_FEED_WEBSERVICE_ASSEMBLER = "datafeed/assembler/NbaDataFeedWebServiceAssembler";
	public static String DATA_FEED_WEBSERVICE_DISASSEMBLER = "datafeed/assembler/NbaDataFeedWebServiceDisAssembler";
	public static String DATAFEED_ASSEMBLER = "datafeed/assembler/NbaDataFeedAssembler"; 
	public static String DATAFEED_DISASSEMBLER = "datafeed/assembler/NbaDataFeedDisAssembler";
	public static String DATAFEED_SERVICE = "datafeed/service/NbaDataFeedService";
	public static String COMMITDATAFEED_ASSEMBLER = "datafeed/assembler/CommitDataFeedAssembler";
	public static String COMMITDATAFEED_DISASSEMBLER = "datafeed/assembler/CommitDataFeedDisAssembler";
	public static String COMMITDATAFEED_SERVICE = "datafeed/service/CommitDataFeedService";
	public static String SEND_TX1203 = "datafeed/service/SendTXLife1203";  
	// End NBA232
	//Begin APSL2808-NBA-SCOR
	public static String COMMITNBASCORFEED_ASSEMBLER = "nbascorfeed/assembler/CommitNbaScorFeedAssembler";
	public static String COMMITNBASCORFEED_DISASSEMBLER = "nbascorfeed/assembler/CommitNbaScorFeedDisAssembler";
	public static String COMMITNBASCORFEED_SERVICE = "nbascorfeed/service/CommitNbaScorFeedService";
	public static String NBASCORFEED_DISASSEMBLER = "nbascorfeed/assembler/NbaScorFeedDisAssembler";
	public static String NBASCORFEED_SERVICE = "nbascorfeed/service/NbaScorFeedService";
	public static String NBASCORFEED_ASSEMBLER = "nbascorfeed/assembler/NbaScorFeedAssembler"; 
	//End APSL2808-NBA-SCOR
	
    public static String UNLOCKWORKFORUSER = "workflow/service/UnlockWorkForUser"; //APSL4236 AWD10 Lock Issue
    public static String AXARETRIEVEWORKFORQUEUE = "workflow/service/AxaRetrieveWorkForQueue"; //APSL4342
    public static String AXARETRIEVEWORKFORQUEUEANDSTATUS = "workflow/service/AxaRetrieveWorkForQueueAndStatus"; //APSL4768
    
    public static String TX203_WEBSERVICE_ASSEMBLER = "tx203/assembler/TX203WebServiceAssembler";   //APSL4508
    public static String TX203_WEBSERVICE_DISASSEMBLER = "tx203/assembler/TX203WebServiceDisAssembler"; //APSL4508
    public static String TX203_WS_VALIDATION = "tx203/service/RequestValidationTX203WS"; //APSL4508
    public static String TX508_DISASSEMBLER = "tx508/assembler/TX508DisAssembler"; //APSL4508
    public static String TX1122_DISASSEMBLER = "tx1122/assembler/TX1122DisAssembler"; //APSL4508
    public static String TX151_DISASSEMBLER = "tx151/assembler/TX151DisAssembler"; //APSL4508
    public static String TXXMLVALIDATOR_DISASSEMBLER = "txValidator/assembler/TXXMLValidatorDisAssembler"; //APSL4508
 
    public static String TX188LIFE_DISASSEMBLER = "ACORD/assembler/TX188LifeDisAssembler";//APSL5203
    
    public static String RETRIEVE_REQ_STATUS_ASSEMBLER = "newBusiness/assembler/RetrieveReqStatusAssembler"; //NBLXA-1656
    public static String RETRIEVE_REQ_STATUS_DISASSEMBLER = "newBusiness/assembler/RetrieveReqStatusDisAssembler"; //NBLXA-1656
    
    //begin NBA331.1 ,APSL5055
  	public static String COMMIT_CHECK_ALLOCATIONS = "cash/commitCheckAllocations";
  	public static String RETRIEVE_CHECK_ALLOCATIONS = "cash/retrieveCheckAllocations";
  	public static String COMMIT_CHECK_ALLOC_ASSEMBLER = "cashiering/assembler/CommitCheckAllocationsAssembler";
  	public static String COMMIT_CHECK_ALLOC_DISASSEMBLER = "cashiering/assembler/CommitCheckAllocationsDisAssembler";
  	public static String COMMIT_CHECK_CORRECTION_ALLOC_DISASSEMBLER = "cashiering/assembler/CommitCheckCorrectionAddlContractsDisAssembler";
  	public static String RETRIEVE_CHECK_ALLOC_ASSEMBLER = "cashiering/assembler/RetrieveCheckAllocationsAssembler";
  	public static String RETRIEVE_CHECK_ALLOC_DISASSEMBLER = "cashiering/assembler/RetrieveCheckAllocationsDisAssembler";

  	/*
  	 * Work Item Identification
  	 */
  	public static String ADD_UW_REQTYPES_RECEIVED = "identification/service/AddReceivedUnderwritingRequirements";
  	public static String DELETE_UW_REQTYPES_RECEIVED = "identification/deleteUnderwritingReqTypesReceived";
  	public static String RETRIEVE_UW_REQTYPES_RECEIVED = "identification/retrieveUnderwritingReqTypesReceived";
  	public static String UPDATE_UW_REQTYPES_RECEIVED = "identification/updateUnderwritingReqTypesReceived";
  	public static String UW_REQTYPES_RECEIVED_DISASSEMBLER = "identification/assembler/UnderwritingReqTypesReceivedDisAssembler";
  	//end NBA331.1, APSL5055
}