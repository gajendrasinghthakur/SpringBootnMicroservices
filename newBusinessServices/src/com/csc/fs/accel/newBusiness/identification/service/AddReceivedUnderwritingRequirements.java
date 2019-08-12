package com.csc.fs.accel.newBusiness.identification.service;



 import java.util.ArrayList;
 import java.util.List;

 import com.csc.fs.Result;
 import com.csc.fs.accel.AccelService;
 import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
 import com.csc.fs.dataobject.nba.auxiliary.UwReqTypesReceived;
 import com.csc.fs.dataobject.nba.identification.RequirementsReceivedUpdateRequest;
 import com.csc.fs.logging.LogHandler;
 import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaLob;
 import com.csc.fsg.nba.vo.NbaWorkItem;

/**
 * This service adds new requirement types received to a row in the UW_REQTYPES_RECEIVED
 * table.  It first performs a query to find an existing row for the application work
 * item ID, and then either uses the UwReqTypesReceived data object returned or creates
 * one to update the table.  It expects a RequirementsReceivedUpdateRequest data object
 * as input in the Result.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * <tr><td>APSL5055</td><td>Version</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class AddReceivedUnderwritingRequirements extends AccelService {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelService#execute(com.csc.fs.Result)
	 */
	@Override
	public Result execute(Result request) {
		LogHandler.Factory.LogServices(this, "executing service");
		
		RequirementsReceivedUpdateRequest updRequest = (RequirementsReceivedUpdateRequest) request.getFirst();
		UwReqTypesReceived reqTypesRequest = createUwReqTypesReceived();
		reqTypesRequest.setItemID(updRequest.getApplicationWorkItemID());
		List<UwReqTypesReceived> input = new ArrayList<UwReqTypesReceived>(2);
		input.add(reqTypesRequest);
		Result result = invoke(ServiceCatalog.RETRIEVE_UW_REQTYPES_RECEIVED, input);
		if (result.hasErrors()) {
			return result;
		}
		
		UwReqTypesReceived reqTypes = (UwReqTypesReceived) result.getFirst();
		if (reqTypes == null) {
			reqTypes = createUwReqTypesReceived();
			reqTypes.setItemID(updRequest.getApplicationWorkItemID());
			reqTypes.markNew();
		}
		updateUwReqTypesReceived(reqTypes, updRequest);
		input.clear();
		input.add(reqTypes);
		result = invoke(ServiceCatalog.UPDATE_UW_REQTYPES_RECEIVED, input);
		return result;
	}

	/**
	 * Returns a new UwReqTypesReceived instance.
	 * @return
	 */
	protected UwReqTypesReceived createUwReqTypesReceived() {
		return new UwReqTypesReceived();
	}

	/**
	 * This method iterates over the NbaWorkItems that are included in the
	 * RequirementsReceivedUpdateRequest and updates the list of requirement types on
	 * the UwReqTypesReceived data object.
	 * @param reqTypes
	 * @param request
	 */
	protected void updateUwReqTypesReceived(UwReqTypesReceived reqTypes, RequirementsReceivedUpdateRequest request) {
		if (request.getRequirementWorkItems() != null) {
			for (String wi : request.getRequirementWorkItems()) {							
				reqTypes.addReqType(wi);				
			}
		}
	}
}
