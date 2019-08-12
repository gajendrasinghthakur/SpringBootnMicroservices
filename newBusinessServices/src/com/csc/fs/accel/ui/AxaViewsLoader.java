package com.csc.fs.accel.ui;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.csc.fs.accel.ui.util.XMLUtils;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaSessionUtils;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.utility.AxaExpressionEvaluator;
import com.sun.faces.el.PropertyResolverImpl;
import com.sun.faces.el.VariableResolverImpl;
import com.sun.faces.el.impl.ExpressionEvaluator;
import com.sun.faces.el.impl.ExpressionInfo;
import com.sun.faces.util.Util;

public class AxaViewsLoader {
	private static Map<String, View> viewsMap = new HashMap<String, View>();
	private static Map<String, Criteria> criteriaMap = new HashMap<String, Criteria>();
	private View matchedView;
	private Map<String, String> inputCritMap = new HashMap<String, String>();
	private Map<String, View> evaluatedViewsMap = new HashMap<String, View>();

	//This method is for debugging purpose only. Server managed objects are built up during server startup.
	public static void main(String... args) throws Exception {
		Scanner scan = new Scanner(new File("\\\\127.0.0.1\\nbAWorkspace\\axalifeConfig\\config\\ui\\views.xml"));
		scan.useDelimiter("\\Z");
		String content = scan.next();
		initializeConfiguration(new InputSource(new StringReader(content)));
		System.out.println(viewsMap);
		System.out.println(criteriaMap);
		Map<String, String> critMap = new HashMap<String, String>();
		critMap.put("appFormNumber", "ICC11-AXA-LIFE");
		new AxaViewsLoader().getView("application");
	}
	
	private AxaViewsLoader() {
		
	}
	
	public static AxaViewsLoader getInstance() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext extContext = context.getExternalContext();
		HttpSession session = (HttpSession)extContext.getSession(false);
		AxaViewsLoader aLoader = (AxaViewsLoader) session.getAttribute(NbaSessionUtils.S_KEY_VIEW_LOADER);
		if (aLoader == null) {
			aLoader = new AxaViewsLoader();
			session.setAttribute(NbaSessionUtils.S_KEY_VIEW_LOADER, aLoader);
		}
		return aLoader;
	}
	
	public static Map<String, View> getViewsMap() {
		return viewsMap;
	}
	
	public boolean evaluateVisibility(View aView) {
		return checkCondition(aView.visible);
	}
	
	public View getView(String key) throws NbaBaseException {
		if (viewsMap.get(key) == null) {
			throw new NbaBaseException("View for the key " + key + "has not been configured");
		}
		View aView = evaluatedViewsMap.get(key);
		if (aView == null) {
			aView = viewsMap.get(key).clone();
		}
		
		if (!NbaUtils.isBlankOrNull(aView.visible)) {
			if (aView.visible.equalsIgnoreCase("false")) {
				return aView;
			}
			if (!aView.visible.equalsIgnoreCase("true") && !checkCondition(aView.visible)) {
				aView.visible = "false";
				evaluatedViewsMap.put(aView.fullKey, aView);
				return aView;
			}
			aView.visible = "true";
		}
		if ("true".equalsIgnoreCase(aView.disable)) {
			return aView;
		}
		matchedView = aView;
		if (evaluatedViewsMap.get(key) == null) {
			evaluatedViewsMap.put(aView.fullKey, aView);
			if (!NbaUtils.isBlankOrNull(aView.disable) && checkCondition(aView.disable)) {
				aView.disable = "true";
				return aView;
			}
			resolveDefaultPage(aView);
		}
		Iterator<View> itr =  aView.getChildren().iterator();
		while (itr.hasNext()) {	
			View bView = itr.next();
			if (evaluatedViewsMap.get(bView.fullKey) != null) {
				continue;
			}
			if (!NbaUtils.isBlankOrNull(bView.visible)) {
				if (!checkCondition(bView.visible)) {
					bView.visible = "false";
					evaluatedViewsMap.put(bView.fullKey, bView);
					continue;
				}
				bView.visible = "true";
			}
			Condition pageCondition = getCondition(bView.page);
			if (pageCondition != null) {
				bView.page = pageCondition.returnValue;
			}
			if (!NbaUtils.isBlankOrNull(bView.disable) && checkCondition(bView.disable)) {
				bView.disable = "true";
			}
			evaluatedViewsMap.put(bView.fullKey, bView);
		}
		return aView;
	}
	
	protected Condition getCondition(String aText) {
		if (!aText.startsWith("$")) {
			return null;
		}
		String criteria = aText.substring(aText.startsWith("${!") ? (aText.indexOf("${") + 3) : (aText.indexOf("${") + 2), aText.indexOf("}"));
		Condition defaultCondition = null;
		if ((criteriaMap.containsKey(criteria))) {
			Criteria aCriteria = criteriaMap.get(criteria);
			int condListSize = aCriteria.condList.size();
			for (int j = 0; j < condListSize; j++) {
				Condition aCondition = aCriteria.condList.get(j);
				if ((inputCritMap.containsKey(aCondition.key)) && aCondition.value.equalsIgnoreCase(inputCritMap.get(aCondition.key))) {
					return aCondition;
				}
				if ((inputCritMap.containsKey(aCondition.key)) && aCondition.value.contains(inputCritMap.get(aCondition.key))) {
					StringTokenizer aTokenizer = new StringTokenizer(aCondition.value, ",");
					while (aTokenizer.hasMoreTokens()) {
						String aToken = aTokenizer.nextToken();
						if (aToken.equalsIgnoreCase(inputCritMap.get(aCondition.key))) {
							return aCondition;
						}
					}
				}
				if (aCondition.key.equals("_DEFAULT")) {
					defaultCondition = aCondition;
				}
			}
		}
		return defaultCondition;
	}
	
	protected boolean checkCondition(String aText) {
		return evaluateBooleanExpression(aText);
	}
	
	protected void resolveDefaultPage(View aView) {
		if (NbaUtils.isBlankOrNull(aView.defaultPage)) {
			return;
		}
		aView.defaultPage = String.valueOf(evaluateBooleanExpression(aView.defaultPage));
		int childrenSize = aView.getChildren().size();
		for (int i = 0; i < childrenSize; i++) {
			View bView = aView.getChildren().get(i);
			resolveDefaultPage(bView);
		}
	}
	
	protected boolean evaluateBooleanExpression(String expr) {
		boolean evaluatedValue = false;
		if (expr.startsWith("$")) {
			String criteria = expr.substring(expr.startsWith("${!") ? (expr.indexOf("${") + 3) : (expr.indexOf("${") + 2), expr.indexOf("}"));
			if (criteria.startsWith("_")) {
				evaluatedValue = Boolean.valueOf(resolveQualifier(criteria.substring(1)));
				if (expr.startsWith("${!")) {
					evaluatedValue = !evaluatedValue;
				}
			} else {
				if (criteria.contains("!") || criteria.contains("&") || criteria.contains("|")) {
					return evaluateBooleanInfixExpression(criteria);
				}
				if ((criteriaMap.containsKey(criteria))) {
					Criteria aCriteria = criteriaMap.get(criteria);
					int condListSize = aCriteria.condList.size();
					for (int j = 0; j < condListSize; j++) {
						Condition aCondition = aCriteria.condList.get(j);
						String condValPattern = aCondition.value.replaceAll("\\*", "(.*)");
						if ((inputCritMap.containsKey(aCondition.key)) && !NbaUtils.isBlankOrNull(inputCritMap.get(aCondition.key))) {
							StringTokenizer aTokenizer = new StringTokenizer(condValPattern, ",");
							while (aTokenizer.hasMoreTokens()) {
								String aToken = aTokenizer.nextToken();
								Pattern aPattern = Pattern.compile(aToken);
								Matcher matcher = aPattern.matcher(inputCritMap.get(aCondition.key));
								if ((expr.startsWith("${!") && !matcher.matches()) || (expr.startsWith("${") && matcher.matches())) {
									evaluatedValue = true;
								}
							}
						}
					}
				}
			}
		} else if (expr.startsWith("#")) {
			String criteria = expr.substring((expr.indexOf("#{") + 2), expr.indexOf("}"));
			ExpressionEvaluator evaluator = Util.getExpressionEvaluator();
			ExpressionInfo exprInfo = new ExpressionInfo();
			exprInfo.setFacesContext(FacesContext.getCurrentInstance());
			exprInfo.setExpressionString(criteria);
			exprInfo.setPropertyResolver(new PropertyResolverImpl());
			exprInfo.setVariableResolver(new VariableResolverImpl());
			exprInfo.setExpectedType(Boolean.class);
			try {
				evaluatedValue = ((Boolean) evaluator.evaluate(exprInfo));
			} catch (Exception ex) {
				ex.printStackTrace();
				evaluatedValue = false;
			}
		} else {
			evaluatedValue = Boolean.valueOf(expr);
		}
		return evaluatedValue;
	}
	
	protected boolean evaluateBooleanInfixExpression(String expr) {
		AxaExpressionEvaluator evaluator = new AxaExpressionEvaluator() {
			protected boolean evaluateOperand(String criteria) {
				return AxaViewsLoader.this.evaluateOperand(criteria);
			}
		};
		return evaluator.evaluateInfixExpression(expr);
	}
	
	protected boolean evaluateOperand(String criteria) {
		if (NbaUtils.isBlankOrNull(criteria)) {
			return false;
		}
		if (criteria.equalsIgnoreCase("true")) {
			return true;
		}
		if (criteria.equalsIgnoreCase("false")) {
			return false;
		}
		if ((criteriaMap.containsKey(criteria))) {
			Criteria aCriteria = criteriaMap.get(criteria);
			int condListSize = aCriteria.condList.size();
			for (int j = 0; j < condListSize; j++) {
				Condition aCondition = aCriteria.condList.get(j);
				String condValPattern = aCondition.value.replaceAll("\\*", "(.*)");
				if ((inputCritMap.containsKey(aCondition.key)) && !NbaUtils.isBlankOrNull(inputCritMap.get(aCondition.key))) {
					StringTokenizer aTokenizer = new StringTokenizer(condValPattern, ",");
					while (aTokenizer.hasMoreTokens()) {
						String aToken = aTokenizer.nextToken();
						Pattern aPattern = Pattern.compile(aToken);
						Matcher matcher = aPattern.matcher(inputCritMap.get(aCondition.key));
						if (matcher.matches()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	protected String resolveQualifier(String qualifier) {
		if (qualifier.equals("WORKFLOWCONFIG")) {
			return String.valueOf(resolveWorkflowConfigQualifier());
		}
		return "";
	}
	
	protected boolean resolveWorkflowConfigQualifier() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Map sessionScope = (Map) facesContext.getApplication().createValueBinding("#{sessionScope}").getValue(facesContext);
		BeanBase currentBean = (BeanBase) sessionScope.get("pc_WorkflowFile");
		if (currentBean != null) {
			String defaultTabIndex = currentBean.getCurrentContextTargetFileIndex();
			if ((defaultTabIndex != null) && defaultTabIndex.contains("_")) {
				StringTokenizer aTokenizer = new StringTokenizer(defaultTabIndex, "_");
				String aToken = "";
				for (int i = 0; i < matchedView.level; i++) {
					aToken = aTokenizer.nextToken();
				}
				if (aToken.equalsIgnoreCase(matchedView.index)) {
					return true;
				}
			} else {
				if (matchedView.getName().equals(defaultTabIndex)) {
					return true;
				}
				int childrenSize = matchedView.getChildren().size();
				for (int i = 0; i < childrenSize; i++) {
					View bView = matchedView.getChildren().get(i);
					if (bView.getName().equals(defaultTabIndex)) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public static boolean initializeConfiguration(InputSource is) {
		try {
			if (is != null) {
				ConfigurationHandler handler = new ConfigurationHandler();
				XMLReader xr = null;
				try {
					xr = XMLUtils.createXMLReader();
				} catch (Exception e) {
					LogHandler.Factory.LogError("AxaViewsLoader", "Error creating XML Reader [{0}]", e, new Object[] { e.getMessage() });
				}
				if (xr != null) {
					xr.setContentHandler(handler);
					xr.setErrorHandler(handler);
					xr.parse(is);
				}
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			LogHandler.Factory.LogError("AxaViewsLoader", "Error Loading Views [{0}]", ex, new Object[] { ex.getMessage() });
			return false;
		}
	}
	public static class ConfigurationHandler extends DefaultHandler {
		private static String VIEW_TAG = "View";
		private static String CRITERIA_TAG = "Criteria";
		private static String CONDITION_TAG = "Condition";
		private static String NAME_ATTRIBUTE = "name";
		private static String KEY_ATTRIBUTE = "key";
		private static String VISIBLE_ATTRIBUTE = "visible";
		private static String DISABLE_ATTRIBUTE = "disable";
		private static String PAGE_ATTRIBUTE = "page";
		private static String ID_ATTRIBUTE = "id";
		private static String VALUE_ATTRIBUTE = "value";
		private static String RETURN_ATTRIBUTE = "return";
		private static String DEFAULT_PAGE_ATTRIBUTE = "defaultPage";
		private static String INDEX_ATTRIBUTE = "index";
		private static String LAYOUT_ATTRIBUTE = "layout";
		private Stack<View> viewStack = new Stack<View>();
		private Criteria currentCriteria = null;
		private Condition currentCondition = null;		
		
		public void startElement(String uri, String name, String qName, Attributes atts) {
			if (name.equals(VIEW_TAG)) {
				View aView = new View();
				aView.key = atts.getValue(KEY_ATTRIBUTE);
				aView.name = atts.getValue(NAME_ATTRIBUTE);
				aView.visible = atts.getValue(VISIBLE_ATTRIBUTE);
				if (aView.visible == null) {
					aView.visible = "true";
				}
				aView.disable = atts.getValue(DISABLE_ATTRIBUTE);
				aView.page = atts.getValue(PAGE_ATTRIBUTE);
				aView.defaultPage = atts.getValue(DEFAULT_PAGE_ATTRIBUTE);
				aView.index = atts.getValue(INDEX_ATTRIBUTE);
				aView.layout = atts.getValue(LAYOUT_ATTRIBUTE);
				int size = viewStack.size();
				String tempKey = "";
				int level = 1;
				for (int i = 0; i < size; i++) {
					tempKey += (viewStack.get(i).key + ".");
					++level;
				}
				if (tempKey.equals("")) {
					aView.fullKey = atts.getValue(KEY_ATTRIBUTE);
				} else {
					aView.fullKey += (tempKey + atts.getValue(KEY_ATTRIBUTE));
				}
				aView.level = level;
				viewStack.push(aView);
			} else if (name.equals(CRITERIA_TAG)) {
				currentCriteria = new Criteria();
				currentCriteria.id = atts.getValue(ID_ATTRIBUTE);
			} else if (name.equals(CONDITION_TAG)) {
				currentCondition = new Condition();
				currentCondition.key = atts.getValue(KEY_ATTRIBUTE);
				currentCondition.value = atts.getValue(VALUE_ATTRIBUTE);
				currentCondition.returnValue = atts.getValue(RETURN_ATTRIBUTE);
			}
		}

		public void endElement(String uri, String name, String qName) {
			if (name.equals(VIEW_TAG)) {
				View aView = viewStack.pop();
				viewsMap.put(aView.fullKey, aView);
				if (viewStack.size() > 0) { 
					View parentView = viewStack.get(viewStack.size() - 1);
					parentView.children.add(aView);
				}
			} else if (name.equals(CRITERIA_TAG)) {
				criteriaMap.put(currentCriteria.id, currentCriteria);
				currentCriteria = null;
			} else if (name.equals(CONDITION_TAG)) {
				currentCriteria.addCondition(currentCondition);
				currentCondition = null;
			}
		}

		public void characters(char ch[], int start, int length) {

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
	
	public static class View {
		private String key = "";
		private String name;
		private String visible;
		private String disable;
		private String page;
		private String fullKey = "";
		private String defaultPage = "";
		private int level;
		private String index;
		private String layout;
		private List<View> children = new ArrayList<View>();

		public String toString() {
			return (" key = " + key + " name = " + name + " visible = " + visible + " disable = " + disable + " page = " + page + " fullKey = "
					+ fullKey + " childern = " + children);
		}
		public View clone() {
			View newObj = new View();
			newObj.key = key;
			newObj.name = name;
			newObj.visible = visible;
			newObj.disable = disable;
			newObj.page = page;
			newObj.fullKey = fullKey;
			newObj.defaultPage = defaultPage;
			newObj.level = level;
			newObj.index = index;
			newObj.layout = layout;
			for (View aView : children) {
				newObj.children.add(aView.clone());
			}
			return newObj;
		}
		public List<View> getChildren() {
			return children;
		}
		public String getName() {
			return name;
		}
		public boolean isDefaultPage() {
			return (!NbaUtils.isBlankOrNull(defaultPage) && defaultPage.equalsIgnoreCase("true"));
		}
		public int getLevel() {
			return level;
		}
		public String getIndex() {
			return index;
		}
		public String getLayout() {
			return layout;
		}
		public boolean isVerticalLayout() {
			return "V".equalsIgnoreCase(layout);
		}
		public String getKey() {
			return key;
		}
		public String getVisible() {
			return visible;
		}
		public String getPage() {
			return page;
		}
		public boolean isVisible() {
			return Boolean.valueOf(visible);
		}
		public boolean isDisable() {
			return Boolean.valueOf(disable);
		}
		public String getDisable() {
			return disable;
		}
	}
	public static class Criteria {
		private String id;
		private List<Condition> condList = new ArrayList<Condition>();
		
		private void addCondition(Condition aCond) {
			condList.add(aCond);
		}

		public String toString() {
			return (" id = " + id + " condList = " + condList);
		}
	}
	public static class Condition {
		private String key;
		private String value;
		private String returnValue;
		
		public String toString() {
			return (" key = " + key + " value = " + value + " returnValue = " + returnValue);
		}
	}
	public Map<String, String> getInputCritMap() {
		return inputCritMap;
	}
}
