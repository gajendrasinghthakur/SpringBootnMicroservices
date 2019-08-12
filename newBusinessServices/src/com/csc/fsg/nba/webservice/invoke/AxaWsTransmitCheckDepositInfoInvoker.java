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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.AxaCheckDepositAccountingExtractVO;
import com.csc.fsg.nba.vo.AxaCheckExtractVO;
import com.csc.fsg.nba.vo.AxaContractsCheckExtractVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.AccountingActivity;
import com.csc.fsg.nba.vo.txlife.AccountingStatement;
import com.csc.fsg.nba.vo.txlife.AccountingStatementExtension;
import com.csc.fsg.nba.vo.txlife.CheckDepositInfo;
import com.csc.fsg.nba.vo.txlife.CheckPolNumberCC;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.FinancialStatement;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * This class is responsible for creating request for TAI Transmit webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.23</td><td>AXA Life Phase 1</td><td>Accounting Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWsTransmitCheckDepositInfoInvoker extends AxaWSInvokerBase {
    
    private static final String CATEGORY = "AccountingService";

    private static final String FUNCTIONID = "AccountingInformation";
    
    private int checkDepInfoIdCount = 1; //AXAL3.7.23

    private String checkDepositInfoIDConstant = "CheckDepositInfo_"; //AXAL3.7.23
    
    private List holdingList = new ArrayList();  //AXAL3.7.23
    
    /**
     * constructor from superclass
     * @param userVO
     * @param nbaTXLife
     */
    
    public AxaWsTransmitCheckDepositInfoInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation, userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);
    }
    
    /**
     * This method first calls the superclass createRequest() and then set the request specefic attribute.
     * @return nbaTXLife
     */
    public NbaTXLife createRequest() throws NbaBaseException {

        AxaCheckDepositAccountingExtractVO extractVO = null;
        //Create a Txlife request object for createDeposit
        NbaTXRequestVO nbaTXRequestVO = createNbaRequestVO();
        //create txlife with default request fields
        NbaTXLife createDeposit1225 = new NbaTXLife(nbaTXRequestVO);
        List depositExtractsList = (List) getObject();
        try {
            for (int j = 0; j < depositExtractsList.size(); j++) {
                extractVO = (AxaCheckDepositAccountingExtractVO) depositExtractsList.get(j);
                updateTxlifeFinancialStatement(createDeposit1225.getOLifE(), extractVO);
            }
            createDeposit1225.getOLifE().setHolding((ArrayList)holdingList);
            new NbaOLifEId(createDeposit1225).assureId(createDeposit1225);
        } catch (NbaBaseException e) {
            getLogger().logException(e);
            e.forceFatalExceptionType();
            throw e;
        } catch (Throwable t) {
            NbaBaseException e = new NbaBaseException(t);
            getLogger().logException(e);
            e.forceFatalExceptionType();
            throw e;
        }
        return createDeposit1225;
    }

    /**
     * @return
     */
    protected NbaTXRequestVO createNbaRequestVO() {
        NbaTXRequestVO nbaTXRequestVO = new NbaTXRequestVO();
        nbaTXRequestVO.setNbaUser(getUserVO());  //AXAL3.7.23 
        nbaTXRequestVO.setTransType(NbaOliConstants.TC_TYPE_CREATE_DEPOSIT);
        nbaTXRequestVO.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        nbaTXRequestVO.setBusinessProcess("");
        return nbaTXRequestVO;
    }
    
    /**
     * This method create the financial statement for a bundle and adds that to Olife.
     * @param oLife OLifE for the txLife 
     * @param extractVO the bundle bean containing all the data
     * @throws NbaBaseException
     */
    //AXAL3.7.23 new method 
    private void updateTxlifeFinancialStatement(OLifE oLife, AxaCheckDepositAccountingExtractVO extractVO) throws NbaBaseException {
        try {

            FinancialStatement finStat = new FinancialStatement();
            finStat.setStatementType(NbaOliConstants.TC_TYPE_ACCOUNTINGSTMTEXTRACT);
            finStat.setCarrierCode(extractVO.getCarrierCode());

            AccountingStatement accStat = new AccountingStatement();
            accStat.setTotalCreditAmt(extractVO.getBundleAmount().doubleValue());
            accStat.setTotalDebitAmt(extractVO.getBundleAmount().doubleValue());
            accStat.setAccountingActivityDate(new Date());

            AccountingActivity accActDebit = new AccountingActivity();
            accActDebit.setAccountAmount(extractVO.getBundleAmount().doubleValue());
            accActDebit.setAccountDebitCreditType(NbaOliConstants.OLI_ACCTDBCRTYPE_DEBIT);

            AccountingActivity accActCredit = new AccountingActivity();
            accActCredit.setAccountAmount(extractVO.getBundleAmount().doubleValue());
            accActCredit.setAccountDebitCreditType(NbaOliConstants.OLI_ACCTDBCRTYPE_CREDIT);

            OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaTXLife.EXTCODE_ACCOUNTINGSTATEMENT);
            AccountingStatementExtension accStatExt = olifeExt.getAccountingStatementExtension();
            accStat.addOLifEExtension(olifeExt);

            ArrayList chkLst = (ArrayList) extractVO.getCheckList();
            AxaCheckExtractVO checkVO = null;
            String checkID = null;
            for (int i = 0; i < chkLst.size(); i++) {
                checkVO = (AxaCheckExtractVO) chkLst.get(i);
                CheckDepositInfo chkDepInfo = new CheckDepositInfo();
                checkID = checkDepositInfoIDConstant + String.valueOf(checkDepInfoIdCount++);
                chkDepInfo.setId(checkID);
                chkDepInfo.setCheckDepositInfoKey(String.valueOf(checkVO.getTechnicalKey()));
                chkDepInfo.setReferenceID(checkVO.getBundleID());
                chkDepInfo.setScanStation(checkVO.getScanStationID());
                chkDepInfo.setCheckAmt(checkVO.getCheckAmount().doubleValue());
                accStatExt.addCheckDepositInfo(chkDepInfo);

                CheckPolNumberCC chkPolNumCC = new CheckPolNumberCC();
                //now again iterate over the contract list and add contract numbers to CheckPolNumber
                ArrayList contractLst = (ArrayList) checkVO.getContractAppliedList();
                AxaContractsCheckExtractVO contractCheckVO = null;
                for (int j = 0; j < contractLst.size(); j++) {
                    contractCheckVO = (AxaContractsCheckExtractVO) contractLst.get(j);
                    chkPolNumCC.addCheckPolNumber(contractCheckVO.getContractNumber());
                    // create the Holding object for this policy and save that in a list.
                    addHoldingForCheck(checkVO, contractCheckVO, checkID);
                }
                chkDepInfo.setCheckPolNumberCC(chkPolNumCC);
            }

            ArrayList accActlist = new ArrayList();
            accActlist.add(accActCredit);
            accActlist.add(accActDebit);

            accStat.setAccountingActivity(accActlist);
            finStat.setAccountingStatement(accStat);
            oLife.addFinancialStatement(finStat);

        } catch (Exception e) {
            e.printStackTrace();//ALS4266
            throw new NbaBaseException("not able to create 1225", NbaExceptionType.FATAL);
        }
    }
    
    /**
     * This method is responsible for creating the holding objects per contract and store them in the list.
     * @param checkVO AxaCheckExtractVO
     * @param contractCheckVO AxaContractsCheckExtractVO
     * @param checkID String reference of the check
     * @throws NbaBaseException
     */
    //AXAL3.7.23 new method
    private void addHoldingForCheck( AxaCheckExtractVO checkVO, AxaContractsCheckExtractVO contractCheckVO, String checkID) throws NbaBaseException{
        
        Holding holding = new Holding();
        holding.setId(holding.getId());
        Policy pol = new Policy();
        pol.setPolNumber(contractCheckVO.getContractNumber());
        pol.setProductCode(contractCheckVO.getProductCode());
        pol.setCarrierCode(contractCheckVO.getCompany());
        pol.setCarrierAdminSystem(contractCheckVO.getBackendSystem()); //ALII1895
        
        FinancialActivity finAct = new FinancialActivity();
        Payment pymnt = new Payment();
        pymnt.setPaymentForm(checkVO.getPaymentForm());
        pymnt.setPaymentAmt(contractCheckVO.getAmountApplied().doubleValue());
        
        OLifEExtension olifExt = new OLifEExtension();
        olifExt.setVendorCode(NbaOliConstants.CSC_VENDOR_CODE);
        FinancialActivityExtension finActExt = new FinancialActivityExtension();
        finActExt.setActionAdd();
        finActExt.setCheckID(checkID);
        try{
            finActExt.setLocationID(NbaUtils.getLocationFromScanStation(checkVO.getScanStationID()));
        }catch (Exception e ){
            // do nothing if sacn station is not found
        }
        olifExt.setFinancialActivityExtension(finActExt);
        
        finAct.addOLifEExtension(olifExt);
        finAct.addPayment(pymnt);
        
        //Begin APSL3410        
        
        long distChannel = contractCheckVO.getDistributionChannel();         
            //searchWI(contractCheckVO.getContractNumber());
        /*if(distChannel == -1) {
            NbaTXLife nbaTxLifeRes = processValidateClientPolicy(contractCheckVO.getContractNumber(), contractCheckVO.getBackendSystem());
            System.out.println(nbaTxLifeRes.toXmlString());
            if(nbaTxLifeRes != null && NbaUtils.getFirstPolicyExtension(nbaTxLifeRes.getPolicy()) != null) {
                PolicyExtension policyext = NbaUtils.getFirstPolicyExtension(nbaTxLifeRes.getPolicy());
                
                distChannel = policyext.getDistributionChannel();
            }
        }*/
        if(distChannel != -1) {
            OLifEExtension olifeExt =  NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
            PolicyExtension polExt = new PolicyExtension();
            polExt.setDistributionChannel(distChannel);
            olifeExt.setPolicyExtension(polExt);
            pol.addOLifEExtension(olifeExt);
        }    
        // End APSL3410
        
        pol.addFinancialActivity(finAct);
        
        holding.setPolicy(pol);
        holdingList.add(holding);
    }
    
    
    // New method APSL3410
    public long searchWI(String polNumber) throws NbaBaseException {
        NbaSearchResultVO resultVO = null;
        NbaDst cntDst = null;
        NbaSearchVO searchVO = new NbaSearchVO();
        searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
        NbaLob lob = new NbaLob();
        lob.setPolicyNumber(polNumber);
        searchVO.setNbaLob(lob);
        searchVO.setResultClassName("NbaSearchResultVO");
        searchVO = WorkflowServiceHelper.lookupWork(getUserVO(), searchVO);
        List searchResult = searchVO.getSearchResults();
        if (searchResult != null && searchResult.size() > 0) {
            resultVO = (NbaSearchResultVO) searchResult.get(searchResult.size() - 1);
            if (resultVO != null) {
                NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
                retOpt.setWorkItem(resultVO.getWorkItemID(), true);
                cntDst = WorkflowServiceHelper.retrieveWorkItem(getUserVO(), retOpt);
            }
        }
        if(cntDst != null && cntDst.getNbaLob() != null) {
            return cntDst.getNbaLob().getDistChannel();
        }
        return -1;
    }
    
    //New method APSL3410
    protected NbaTXLife processValidateClientPolicy(String polNumber, String backEndSystem) throws NbaBaseException {
        // 1. Validate the non nbA contract number - Call the CIF Client Webservice
        NbaTXLife txlife =  new NbaTXLife();
        OLifE olife = new OLifE();
        olife.setVersion(NbaOliConstants.OLIFE_VERSION);        
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setSourceInfoName(SOURCEINFO);
        sourceInfo.setFileControlID(backEndSystem);  
        olife.setSourceInfo(sourceInfo);
        txlife.setOLifE(olife);
        
        AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_VALIDATE_POLICY, getUserVO(), txlife,
                getNbaDst(), polNumber); 
        NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
        return nbaTxLifeRes;
    }
    
}
