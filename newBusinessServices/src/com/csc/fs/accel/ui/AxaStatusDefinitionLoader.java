package com.csc.fs.accel.ui;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.csc.fs.accel.ui.util.XMLUtils;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.statusDefinitions.Status;

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

public class AxaStatusDefinitionLoader {
	private static Map statusDefinitions = new HashMap();
	private static Map passStatusMap = new HashMap();
	private static Map errorStatusMap = new HashMap();
	
	public static Status determinePassStatus(String processId, String variance) {
		StatusDefinition statusDef = (StatusDefinition) statusDefinitions.get(processId);
		if (!NbaUtils.isBlankOrNull(statusDef.passStatusMap.get(variance))) {
			return (Status) statusDef.passStatusMap.get(variance);
		}
		return null;
	}
	
	public static Status determineError(String processId, String variance) {
		if (!NbaUtils.isBlankOrNull(statusDefinitions.get(processId))) {
			StatusDefinition statusDef = (StatusDefinition) statusDefinitions.get(processId);
			if (!NbaUtils.isBlankOrNull(statusDef.errorStatusMap.get(variance))) {
				return (Status) statusDef.errorStatusMap.get(variance);
			}
		}
		return (Status) AxaStatusDefinitionLoader.errorStatusMap.get(variance);
	}

	public static boolean initializeConfiguration(InputSource is) {
		try {
            if(is != null){
                ConfigurationHandler handler = new ConfigurationHandler();
                XMLReader xr = null;
                try {
                    xr = XMLUtils.createXMLReader();
                } catch (Exception e) {
                    LogHandler.Factory.LogError("AxaStatusDefinitionLoader", "Error creating XML Reader [{0}]", e, new Object[]{e.getMessage()});
                }
                if (xr != null) {
                    xr.setContentHandler(handler);
                    xr.setErrorHandler(handler);
                    xr.parse(is);
                }
            }
            return true;
        } catch (Exception ex) {
            LogHandler.Factory.LogError("AxaStatusDefinitionLoader", "Error Loading Status Definition Configuration [{0}]", ex, new Object[]{ex.getMessage()});
            return false;
        }
    }
	
	public static class ConfigurationHandler extends DefaultHandler{
		private static String STATUS_DEF_TAG = "StatusDefinition";
		private static String PASS_STATUS_TAG = "PassStatus";
		private static String ERROR_STATUS_TAG = "ErrorStatus";
		private static String STATUS_TAG = "StatusCode";
		private static String COMMENT_TAG = "Comment";
		private static String ROUTING_REASON_TAG = "RoutingReason";
		private static String PROCESS_ATTRIBUTE = "process";
		private static String VARIANCE_ATTRIBUTE = "variance";
		private StatusDefinition currentStatusDefinition = null;
		private Status currentPassStatus = null;
		private Status currentErrorStatus = null;		
		private boolean statusFlag = false;
		private boolean commentFlag = false;
		private boolean routingReasonFlag = false;
		private StringBuffer tempValue; // APSL4716
		private boolean processingElement = false; // APSL4716
		
		public void startElement(String uri, String name, String qName, Attributes atts) {
			processingElement = true; // APSL4716
			tempValue = new StringBuffer(); // APSL4716
			if (name.equals(STATUS_DEF_TAG)) {
				currentStatusDefinition = new StatusDefinition();
				currentStatusDefinition.process = atts.getValue(PROCESS_ATTRIBUTE);
			} else if (name.equals(PASS_STATUS_TAG)) {
				currentPassStatus = new Status();
				currentPassStatus.setVariance(atts.getValue(VARIANCE_ATTRIBUTE));
			} else if (name.equals(ERROR_STATUS_TAG)) {
				currentErrorStatus = new Status();
				currentErrorStatus.setVariance(atts.getValue(VARIANCE_ATTRIBUTE));
			} else if (name.equals(STATUS_TAG)) {
				statusFlag = true;
			} else if (name.equals(COMMENT_TAG)) {
				commentFlag = true;
			} else if (name.equals(ROUTING_REASON_TAG)) {
				routingReasonFlag = true;
			}
		}

		// APSL4716 Method refactored
		public void endElement(String uri, String name, String qName) {
			processingElement = false; 
			if (name.equals(STATUS_DEF_TAG)){
				statusDefinitions.put(currentStatusDefinition.process, currentStatusDefinition);
				currentStatusDefinition = null;
			} else if (name.equals(PASS_STATUS_TAG)){
				currentStatusDefinition.addPass(currentPassStatus);
				currentPassStatus = null;
			} else if (name.equals(ERROR_STATUS_TAG)){
				if(currentStatusDefinition != null){
					currentStatusDefinition.addError(currentErrorStatus);
				} else {
					errorStatusMap.put(currentErrorStatus.getVariance(), currentErrorStatus);
				}
				currentErrorStatus = null;
			} else if (name.equals(STATUS_TAG)) {
				statusFlag = false;
				if (currentPassStatus != null) {
					currentPassStatus.setStatusCode(tempValue.toString()); 
				} else {
					currentErrorStatus.setStatusCode(tempValue.toString());
				}
			} else if (name.equals(COMMENT_TAG)) {
				commentFlag = false;
				if (currentPassStatus != null) {
					currentPassStatus.setComment(tempValue.toString()); 
				} else {
					currentErrorStatus.setComment(tempValue.toString());
				}
			} else if (name.equals(ROUTING_REASON_TAG)) {
				routingReasonFlag = false;
				if (currentPassStatus != null) {
					currentPassStatus.setRoutingReason(tempValue.toString());
				} else {
					currentErrorStatus.setRoutingReason(tempValue.toString());
				}
			}
			tempValue = null;
		}

		// APSL4716 Method refactored
		public void characters(char ch[], int start, int length) {
			if (statusFlag || commentFlag || routingReasonFlag) {
				String processItem = new String(ch, start, length);
				if (processingElement) {
					tempValue.append(processItem);				
				} else {
					tempValue.append(processItem);				
				}
				processingElement = true;
			}
		}
		
		public void error(SAXParseException e) throws SAXException {
			System.err.println(e);
			throw e;
		}
		
		public void fatalError(SAXParseException e) throws SAXException {
			System.err.println(e);
			throw e;
		}
	}
	
	public static class StatusDefinition {
		private String process;
		private Map passStatusMap = new HashMap();
		private Map errorStatusMap = new HashMap();
		
		private void addError(Status error) {
			errorStatusMap.put(error.getVariance(), error);
		}
		
		private void addPass(Status pass) {
			passStatusMap.put(pass.getVariance(), pass);
		}
	}
	
}
