package com.csc.fsg.nba.business.transaction;

import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaGIAppSystemDataVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;


/**
* 
* This class encapsulates checks whenever following changes are made to the Print Together Indicator. - Value
* 
* <p>
* <b>Modifications: </b> <br>
* <table border=0 cellspacing=5 cellpadding=5> <thead>
* <th align=left>Project</th>
* <th align=left>Release</th>
* <th align=left>Description</th>
* </thead>
* <tr>
* <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
* <td>NBLXA-188</td><td>AXA Life Phase 2</td><td>(APSL5318) Legacy Decommissioning</td>
* </tr>
* </table>
* <p>
* @author CSC FSG Developer
* @version 7.0.0
* @since New Business Accelerator - Version 7
*/

public class AxaUpdatePrintTogetherForGI extends AxaDataChangeTransaction implements NbaOliConstants {

	protected NbaLogger logger = null;

	protected static long[] changeTypes = { DC_PRINT_TOGETHER_UPDATED };

	@Override
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()) {
			Policy policy = nbaTxLife.getPolicy();
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
			if (null != policy && null != policyExt) {
				boolean isPrintTogetherIndChecked = false;
				isPrintTogetherIndChecked = NbaUtils.isPrintTogetherChecked(nbaTxLife);
				if (!isPrintTogetherIndChecked) {
					NbaSystemDataDatabaseAccessor.deleteGIAppSystemData(policy.getPolNumber());
				} else {
					if (nbaTxLife != null && policyExt.hasGIBatchID()) {
						NbaParty nbaParty = nbaTxLife.getPrimaryParty();
						String ownerName = nbaTxLife.getOwnerParty().getFullName();
						AxaGIAppSystemDataVO appSystemDataVO = new AxaGIAppSystemDataVO();
						appSystemDataVO.setBatchID(policyExt.getGIBatchID());
						appSystemDataVO.setPolicynumber(policy.getPolNumber());
						appSystemDataVO.setEmployerName(nbaDst.getNbaLob().getEmployerName());
						appSystemDataVO.setPrefix(nbaParty.getPrefix());
						appSystemDataVO.setFirstName(nbaParty.getFirstName());
						appSystemDataVO.setLastName(nbaParty.getLastName());
						appSystemDataVO.setSuffix(nbaParty.getSuffix());
						appSystemDataVO.setSsn(nbaParty.getSSN());
						appSystemDataVO.setContractPrintExtractDate(null);
						appSystemDataVO.setPDR_Update_ind(0);
						appSystemDataVO.setPrintPassInd(0);
						appSystemDataVO.setReleaseBatchID("");
						appSystemDataVO.setReleaseBatchInd(0);
						appSystemDataVO.setOwnFullName(ownerName);
						appSystemDataVO.setMdrConsentInd(policyExt.getMDRConsentIND());
						appSystemDataVO.setCompanyCode(policy.getCarrierCode());//NBLXA-1680
						NbaSystemDataDatabaseAccessor.insertGIAppSystemData(appSystemDataVO);
					}
				}
			}
		}
		return nbaDst;
	}

	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
			}
		}
		return logger;
	}

	@Override
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	@Override
	protected boolean isTransactionAlive() {
		return true;
	}

}
