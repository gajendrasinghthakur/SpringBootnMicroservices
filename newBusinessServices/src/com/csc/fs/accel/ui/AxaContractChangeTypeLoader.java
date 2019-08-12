package com.csc.fs.accel.ui;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import com.csc.fs.accel.ui.util.XMLUtils;
import com.csc.fs.logging.LogHandler;


/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL5128</td><td>Discretionary</td><td></td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 
 * @since New Business Accelerator - Version 
 */

public class AxaContractChangeTypeLoader {
	 private static List options = new ArrayList();
	
	
	public static boolean initializeConfiguration(InputSource is) {
		try {
			if (is != null) {
				ConfigurationHandler handler = new ConfigurationHandler();
				XMLReader xr = null;
				try {
					xr = XMLUtils.createXMLReader();
				} catch (Exception e) {
					LogHandler.Factory.LogError("AxaContractChangeTypeLoader", "Error creating XML Reader [{0}]", e, new Object[] { e.getMessage() });
				}
				if (xr != null) {
					xr.setContentHandler(handler);
					xr.setErrorHandler(handler);
					xr.parse(is);
				}
			}
			return true;
		} catch (Exception ex) {
			LogHandler.Factory.LogError("AxaContractChangeTypeLoader", "Error Loading ContractChangeType Configuration [{0}]", ex,
					new Object[] { ex.getMessage() });
			return false;
		}
	}

	public static class ConfigurationHandler extends DefaultHandler {
		private static String OPTION_TAG = "Option";
		private static String SUB_OPTION_TAG = "SubOption";
		private static String LINK_OPTION_TAG = "LinkOption";
		private static String TC_ATTRIBUTE = "tc";
		private static String TRANS_ATTRIBUTE = "translation";
		private static String VIEW_ATTRIBUTE = "view";
	
		private Option currentOption = null;
		private SubOption currentSubOption = null;
		private LinkOption currentLinkOption = null;

		public void startElement(String uri, String name, String qName, Attributes atts) {
         
			if(name.equalsIgnoreCase(OPTION_TAG)){
				currentOption=new Option();
				currentOption.tc=atts.getValue(TC_ATTRIBUTE);
				currentOption.translation=atts.getValue(TRANS_ATTRIBUTE);
				currentOption.view=atts.getValue(VIEW_ATTRIBUTE);
			}
			if(name.equalsIgnoreCase(SUB_OPTION_TAG)){
				currentSubOption=new SubOption();
				currentSubOption.tc=atts.getValue(TC_ATTRIBUTE);
				currentSubOption.translation=atts.getValue(TRANS_ATTRIBUTE);
				currentOption.subOptions.add(currentSubOption);
			}
			if(name.equalsIgnoreCase(LINK_OPTION_TAG)){
				currentLinkOption=new LinkOption();
				currentLinkOption.tc=atts.getValue(TC_ATTRIBUTE);
				currentLinkOption.translation=atts.getValue(TRANS_ATTRIBUTE);
				currentOption.linkOptions.add(currentLinkOption);
				
			}
		}

		public void endElement(String uri, String name, String qName) {
			if (name.equalsIgnoreCase(OPTION_TAG)) {
				options.add(currentOption);
				currentOption = null;
			}
			if (name.equalsIgnoreCase(SUB_OPTION_TAG)) {
				currentSubOption = null;
			}
			if (name.equalsIgnoreCase(LINK_OPTION_TAG)) {
				currentLinkOption = null;
			}
		}
	}

	public static class Option {
		private String tc = "";
		private String translation;
		private String view;
		private List<SubOption> subOptions = new ArrayList<SubOption>();
		private List<LinkOption> linkOptions = new ArrayList<LinkOption>();
		/**
		 * @return the tc
		 */
		public String getTc() {
			return tc;
		}
		/**
		 * @param tc the tc to set
		 */
		public void setTc(String tc) {
			this.tc = tc;
		}
		/**
		 * @return the translation
		 */
		public String getTranslation() {
			return translation;
		}
		/**
		 * @param translation the translation to set
		 */
		public void setTranslation(String translation) {
			this.translation = translation;
		}
		/**
		 * @return the view
		 */
		public String getView() {
			return view;
		}
		/**
		 * @param view the view to set
		 */
		public void setView(String view) {
			this.view = view;
		}
		/**
		 * @return the subOptions
		 */
		public List<SubOption> getSubOptions() {
			return subOptions;
		}
		/**
		 * @param subOptions the subOptions to set
		 */
		public void setSubOptions(List<SubOption> subOptions) {
			this.subOptions = subOptions;
		}
		/**
		 * @return the subOptions
		 */
		public List<LinkOption> getLinkOptions() {
			return linkOptions;
		}
		/**
		 * @param subOptions the subOptions to set
		 */
		public void setLinkOptions(List<LinkOption> linkOptions) {
			this.linkOptions = linkOptions;
		}
	}
	
	public static class SubOption {
		private String tc;
		private String translation;
		/**
		 * @return the tc
		 */
		public String getTc() {
			return tc;
		}
		/**
		 * @param tc the tc to set
		 */
		public void setTc(String tc) {
			this.tc = tc;
		}
		/**
		 * @return the translation
		 */
		public String getTranslation() {
			return translation;
		}
		/**
		 * @param translation the translation to set
		 */
		public void setTranslation(String translation) {
			this.translation = translation;
		}		
	}
	
	public static class LinkOption {
		private String tc;
		private String translation;
		/**
		 * @return the tc
		 */
		public String getTc() {
			return tc;
		}
		/**
		 * @param tc the tc to set
		 */
		public void setTc(String tc) {
			this.tc = tc;
		}
		/**
		 * @return the translation
		 */
		public String getTranslation() {
			return translation;
		}
		/**
		 * @param translation the translation to set
		 */
		public void setTranslation(String translation) {
			this.translation = translation;
		}		
	}
	
    public static List getOptions() {
        return options;
    } 
    
   
}
