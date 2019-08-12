package com.csc.fs.accel.ui;

import com.csc.fs.svcloc.ServiceLocator;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL3874</td><td>Discretionary</td><td></td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 
 * @since New Business Accelerator - Version 
 */

public class AxaInitializer extends Initializer {

	private static final long serialVersionUID = 5155986538274025712L;

	protected void reloadConfigData() {
		super.reloadConfigData();
		AxaStatusDefinitionLoader.initializeConfiguration(ServiceLocator.getConfigurationSource("\\workflow\\statusDefinitions.xml"));
		AxaContractChangeTypeLoader.initializeConfiguration(ServiceLocator.getConfigurationSource("\\contractChangeTypes.xml"));
		AxaViewsLoader.initializeConfiguration(ServiceLocator.getConfigurationSource("\\ui\\views.xml")); //APSL5093
	}

}
