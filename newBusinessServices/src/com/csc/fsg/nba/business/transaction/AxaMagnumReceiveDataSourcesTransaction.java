package com.csc.fsg.nba.business.transaction;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;

import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaMagnumUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaMagnumDecisionServiceVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NBLXA-2402 (NBLXA-2602) US#297686 | New Transaction Class 
 * @author nsharda2
 *
 */
public class AxaMagnumReceiveDataSourcesTransaction extends AxaDataChangeTransaction implements NbaConstants {

protected NbaLogger logger = null;
	
	protected static long[] changeTypes = {
			DC_MAGNUM_DATASOURCE_RECIEVED
	};

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	@Override
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()) {
			Holding magHolding = null;
			Iterator registerChangesItr = registeredChanges.iterator();
			while (registerChangesItr.hasNext()) {
				AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
				if (DC_MAGNUM_DATASOURCE_RECIEVED == change.getChangeType()) {
					//Begin NBLXA-2402(NBLXA-2470)
					RequirementInfo requirementInfo = nbaTxLife.getRequirementInfoById(change.getChangedObjectId());
					// Create Magnum Holding
					if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED) {
						if (!NbaUtils.isBlankOrNull(requirementInfo.getAttachment())) {
							Attachment attachment = requirementInfo.getAttachmentAt(0);
							if (!NbaUtils.isBlankOrNull(attachment) && !NbaUtils.isBlankOrNull(attachment.getAttachmentData())) {
								try {
									Holding magnumHoldingResponse = AxaMagnumUtils.isMagnumHoldingPresent(new NbaTXLife(attachment.getAttachmentData().getPCDATA()));
									magHolding = AxaMagnumUtils.createHoldingAndRelation(nbaTxLife, magnumHoldingResponse, requirementInfo);
								} catch (Exception ex) {
									throw new NbaBaseException(ex);
								}
							}
						}
					}
					if (magHolding == null) {
						magHolding = AxaMagnumUtils.getLatestParamedMagnumHolding(nbaTxLife);
					}
					//End NBLXA-2402(NBLXA-2470)
					if (magHolding == null) {
						return nbaDst;
					}

					if ((Arrays.asList(ArrayUtils.toObject(MAGNUM_DATA_SOURCES))).contains(requirementInfo.getReqCode())) {

						// NBLXA-2402 (NBLXA-2602) US#297686
						AxaWSInvoker webServiceBulkInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_BULK, user,
								nbaTxLife, null, requirementInfo);
						webServiceBulkInvoker.execute();
						// NBLXA-2402 (NBLXA-2602) US#297686

						AxaMagnumDecisionServiceVO magnumDetailDataVO = new AxaMagnumDecisionServiceVO();
						magnumDetailDataVO.setMagnumHolding(magHolding);
						magnumDetailDataVO
								.setMagnumProcess(AxaMagnumUtils.getMagnumProcessForRequirement(requirementInfo.getReqCode(), user.getUserID()));

						try {
							AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_DETAILED_DECISION,
									user, nbaTxLife, null, magnumDetailDataVO);
							webServiceInvoker.execute();
						} catch (NbaBaseException ex) {
							throw ex;
						}
					}
				}
				break; //NBLXA-2402(NBLXA-2470)
			}
		}
		return nbaDst;
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
