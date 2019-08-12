package com.csc.fs.ra.message; 

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.axis.utils.DOM2Writer;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.ra.SimpleMappedRecord;
import com.csc.fs.ra.XMLtoMappedRecordParser;

/**
 * MessageManagerTransformReq is used by the Transform resource adaptor when processing 
 * an inbound third party XML message.
 * For Request processing the "request" Mapped Record typically contains a single field (i.e. payload) which contains
 * the inbound request XML message from a third party.   
 * For Response processing, the third party XML data returned by the Transform resource adaptor
 * is converted to Mapped records using Message Definitions specific to the third party XML format.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA234</td><td>Version 8</td><td> nbA ACORD Transformation Service Project</td></tr>
 * </table>
 * <p>
 */

public class MessageManagerTransformReq implements MessageManager {

    protected static final String CDATA = "<![CDATA[";  //ACEL3050
    protected static final String ESCAPECHARACTERS_PROPERTIES = "escapecharacters.properties";      //ACEL3050
    private static SAXParserFactory saxParserFactory = null;
    private static Map escapeKeysMap = new LinkedHashMap(5);    //ACEL3050
    private static Map escapeValuesMap = new LinkedHashMap(5);  //ACEL3050
    
    private static class XMLItem {
        
        String originalStream;
        String uri;
        String nodeQName;
        String nodeName;
        String value;
        XMLItem root;
        XMLItem parent;
        Map attributes = new HashMap();
        Map qattributes = new HashMap();
        Map children = new HashMap();
        
        String toXML(){
            StringBuffer buffer = new StringBuffer();
            buffer.append('<').append(nodeQName);
            if(attributes != null && attributes.size() > 0){
                Iterator iter = attributes.keySet().iterator();
                while(iter.hasNext()){
                    String attrName = (String)iter.next();
                    String value = (String)attributes.get(attrName);
                    buffer.append(' ').append(attrName).append('=').append('"').append(value).append('"');
                }
            }
            buffer.append('>');
            if(this.value != null){
                if(value.indexOf('<') > -1){
                    buffer.append(CDATA).append(this.value).append("]]>");      //ACEL3050
                } else {
                    buffer.append(this.value);
                }
            }
            // now do each child
            Iterator iter = children.values().iterator();
            while(iter.hasNext()){
                Object item = iter.next();
                if(item instanceof XMLItem){
                    ((XMLItem)item).toXML(buffer);
                } else if(item instanceof List){
                    Iterator listiter = ((List)item).iterator();
                    while(listiter.hasNext()){
                        XMLItem listitem = (XMLItem)listiter.next();
                        listitem.toXML(buffer);
                    }
                }
            }
            buffer.append('<').append('/').append(nodeQName).append('>');
            return buffer.toString();
        }
        
        void toXML(StringBuffer buffer){
            buffer.append('<').append(nodeQName);
            if(attributes != null && attributes.size() > 0){
                Iterator iter = attributes.keySet().iterator();
                while(iter.hasNext()){
                    String attrName = (String)iter.next();
                    String value = (String)attributes.get(attrName);
                    buffer.append(' ').append(attrName).append('=').append('"').append(value).append('"');
                }
            }
            buffer.append('>');
            if(this.value != null){
                if(value.indexOf('<') > -1){
                    buffer.append(CDATA).append(this.value).append("]]>");      //ACEL3050
                } else {
                    buffer.append(this.value);
                }
            }
            // now do each child
            Iterator iter = children.values().iterator();
            while(iter.hasNext()){
                Object item = iter.next();
                if(item instanceof XMLItem){
                    ((XMLItem)item).toXML(buffer);
                } else if(item instanceof List){
                    Iterator listiter = ((List)item).iterator();
                    while(listiter.hasNext()){
                        XMLItem listitem = (XMLItem)listiter.next();
                        listitem.toXML(buffer);
                    }
                }
            }
            buffer.append('<').append('/').append(nodeQName).append('>');
        }
        
        
        XMLItem getRoot(){
            return root;
        }
        
        String getAttributeValue(String attributeName){
            if(attributes.containsKey(attributeName)){
                return (String)attributes.get(attributeName);
            } else {
                return (String)qattributes.get(attributeName);
            }
        }

        String getAttributeValue(String path, String attributeName){
            if(path == null || path.equals("")){
                return getAttributeValue(attributeName);
            }
            XMLItem item = getItem(path);   //NBA146            
            if(item != null){
                return item.getAttributeValue(attributeName);
            } else {
                return null;
            }
        }

        XMLItem getItem(String path){
            long start = System.currentTimeMillis();
            // extract the path into component pieces...
            XMLItem result = null;
            Stack pathList = parsePathAsStack(path);
            if(pathList.size() > 0){  
                result = getItem(pathList);
            }
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("getItem (top) " + path + " " + (end - start));
//          }
            return result;
        }

        XMLItem getItem(Stack pathList){
            long start = System.currentTimeMillis();
            // extract the path into component pieces...
            XMLItem result = null;
            if(pathList.size() > 0){
                String currentItem = (String)pathList.pop();
                if(currentItem != null && currentItem.equals(nodeName)){
                    if(pathList.size() == 0){
                        // reached the end of the path and the names match...
                        result = this;
                    } else {
                        // go into children...
                        currentItem = (String)pathList.peek();
                        if(children.containsKey(currentItem)){
                            Object item = children.get(currentItem);
                            if(item instanceof XMLItem){
                                // single child
                                XMLItem child = (XMLItem)item;
                                result = child.getItem(pathList);
                            } else if(item instanceof List){
                                // list of children of same node name
                                Iterator iter = ((List)item).iterator();
                                Stack tempPathList = (Stack)pathList.clone();
                                while(iter.hasNext() && result == null){
                                    XMLItem child = (XMLItem)iter.next();
                                    result = child.getItem(pathList);
                                    if(result == null){
                                        pathList = (Stack)tempPathList.clone();
                                    }
                                }
                            }
                        }
                    }
                } // no match top node so does not exist
            }
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("getItem " + (end - start));
//          }
            return result;
        }

        List getItems(Stack pathList){
            long start = System.currentTimeMillis();
            // extract the path into component pieces...
            List result = new ArrayList();
            if(pathList.size() > 0){
                String currentItem = (String)pathList.pop();
                if(currentItem != null && currentItem.equals(nodeName)){
                    if(pathList.size() == 0){
                        // reached the end of the path and the names match...
                        result.add(this);
                    } else {
                        // go into children...
                        currentItem = (String)pathList.peek();
                        if(children.containsKey(currentItem)){
                            Object item = children.get(currentItem);
                            if(pathList.size() == 1){
                                if(item instanceof XMLItem){
                                    // single child
                                    XMLItem child = (XMLItem)item;
                                    result.add(child);
                                } else if(item instanceof List){
                                    // list of children of same node name
                                    result.addAll((List)item);
                                }
                            } else {
                                if(item instanceof XMLItem){
                                    // single child
                                    XMLItem child = (XMLItem)item;
                                    result.addAll(child.getItems(pathList));
                                } else if(item instanceof List){
                                    // list of children of same node name
                                    Iterator iter = ((List)item).iterator();
                                    Stack tempPathList = (Stack)pathList.clone();
                                    while(iter.hasNext()){ 
                                        XMLItem child = (XMLItem)iter.next();
                                        result.addAll(child.getItems(pathList));
                                        pathList = (Stack)tempPathList.clone();
                                    }
                                }
                            }
                        }
                    }
                } // no match top node so does not exist
            }
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("getItems " + (end - start));
//          }
            return result;
        }
        
        
        List getItems(String path){
            long start = System.currentTimeMillis();
            // extract the path into component pieces...
            List result = new ArrayList();
            Stack pathList = parsePathAsStack(path);
            if(pathList.size() > 0){
                result.addAll(getItems(pathList));
            }
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("getItems (top)" + path + " " + (end - start));
//          }
            return result;
        }

        String getValue(){
            return value;
        }
        
        String getValue(String path, String itemName){
            long start = System.currentTimeMillis();
            String value = null;
            String pathToUse = "";
            //vsonone: ACEL2546: Added the condition to check for empty String also
            if(path != null && !path.equals("")){
                pathToUse = path + "/";
            } else {
                pathToUse = nodeName + "/";
            }
            pathToUse += itemName;
            XMLItem item = getItem(pathToUse);
            if(item != null){
                value = item.getValue();
            }
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("getValue " + (end - start));
//          }
            return value;
        }
        /**
         * @param path
         * @param itemName
         * @return
         */
        protected List getValues(String path, String itemName) {
			long start = System.currentTimeMillis();
			String value = null;
			String pathToUse = "";
			List values = new ArrayList();
			if (path != null && !path.equals("")) {
				pathToUse = path + "/";
			} else {
				pathToUse = nodeName + "/";
			}
			pathToUse += itemName;
			List items = getItems(pathToUse);
			Iterator it = items.iterator();
			XMLItem item;
			while (it.hasNext()) {
				item = (XMLItem) it.next();
				values.add(item.getValue());
			}
			return values;
		}
        
        
        String getUnparsedNodeValue(String path, String itemName){
            String value = null;
            String pathToUse = "";
            if(path != null){
                pathToUse = path + "/";
            } else {
                pathToUse = nodeName + "/";
            }
            pathToUse += itemName;
            XMLItem item = getItem(pathToUse);
    
            if(item != null){
                value = item.toXML();
            }
   
            return value;
        }       
        
        //vsonone: ACEL2546: Added to return unparsedNode value for an xmlitem
        String getUnparsedNodeValue() {
            return toXML();
        }
        
        private String getCommonPath(String path1, String path2, String blockPath){
            // if anything is relative at this point, assume its relative to the block

            List p1=parsePath(path1);
            List p2=parsePath(path2);
            StringBuffer commonPath=new StringBuffer();
            int indx=(p1.size()<=p2.size())?p1.size():p2.size();
            for (int i=0;i<indx;i++){
                if (p1.get(i).equals(p2.get(i))){
                    commonPath.append('/').append(p1.get(i));
                }
            }
            return commonPath.toString();
        }
        
        /**
         * Resolve criteria
         * 
         */
        private List applyInboundBlockCriteria(List nodes, MessageDefinition.InboundCriteria criteria, String referringBlockPath) {
            // Determine value of <compareto> element.  Store comparetoValues in a List as
            // there can be multiple <value> elements specified  
            List comparetoValues = new ArrayList();
            comparetoValues.addAll(criteria.getCompareToValues());  //APSL4508
            if (comparetoValues.size() == 0) {
                XMLItem comparetoContext = null;
                String comparetoPath = criteria.getCompareToXMLPath(); //APSL4508
                if (comparetoPath != null && referringBlockPath.startsWith(comparetoPath)) { 
                    // this path is identical to, or a parent of, the referring block path
                    comparetoContext = this;        
                    // set the relative path
                    comparetoPath = referringBlockPath.substring(comparetoPath.length());
                } else {
                    comparetoContext = root; // set to global xml context               
                }
                if (criteria.isCompareToAttribute()) {  //APSL4508
                    comparetoValues.add(comparetoContext.getAttributeValue(comparetoPath, criteria.getCompareToXMLItem())); //APSL4508  
                } else {
                    comparetoValues.add(comparetoContext.getValue(comparetoPath, criteria.getCompareToXMLItem())); //APSL4508
                }
            }
            
            //Code Refactoring
            List matchingNodes = getMatchingNodes(nodes, criteria, comparetoValues, null);
        
            return matchingNodes;
        }

        //Code Refactoring
        /**loop through input nodes, comparing the compare element value
         *   with that of the passed compareto values.
         *   return only
         *   the nodes whose compare and compareto values match.
         * @param inputNodes                input nodes
         * @param criteria                  inbound criteria for the item or block
         * @param comparetoValues           compareTo Values to be matched
         * @param passedComparePath         null means we should calculate comparePath in the loop, indicating it is used to find matching nodes for block, 
         *                                  if not null then use it as comparePath, indicating it is used to find matching nodes for item
         * 
         * @return                          the nodes whose compare and compareto values match, this is a subset of inputNodes.
         */
        private List getMatchingNodes(List inputNodes, MessageDefinition.InboundCriteria criteria, List comparetoValues, String passedComparePath) {
            
            List matchingNodes = new ArrayList();
            Iterator nodeIterator = inputNodes.iterator();
            while (nodeIterator.hasNext()) {
                XMLItem currentContext = (XMLItem) nodeIterator.next();
                String comparePath = null; 
                // set the comparePath based the currentContext and compareXMLPath
                if(passedComparePath == null) {
                    comparePath = criteria.getCompareXMLPath(); //APSL4508
                    int nodeLoc = criteria.getCompareXMLPath().indexOf(currentContext.nodeName);  //APSL4508
                    if (nodeLoc > -1) {
                        comparePath = criteria.getCompareXMLPath().substring(nodeLoc); //APSL4508
                    }
                } else {
                    comparePath = passedComparePath;                    
                }
            
                String compareValue = null;
                //vsonone: ACEL2546: compare values can also be multiple
                if(comparePath != null && !comparePath.equals("")) {
                    List xmlItems = currentContext.getItems(comparePath);
                    if(xmlItems != null) {
                        Iterator iterator = xmlItems.iterator();
                        while(iterator.hasNext()) {
                            XMLItem item = (XMLItem) iterator.next();
                            if (criteria.isCompareAttribute()) { //APSL4508
                                compareValue = item.getAttributeValue(criteria.getCompareXMLItem()); //APSL4508
                            } else {
                                compareValue = item.getValue(null, criteria.getCompareXMLItem());  //APSL4508
                            }   
                            // Keep if compare value matches any compareto value.
                            Iterator comparetoIter = comparetoValues.iterator();            
                            boolean matched = false;
                            while (comparetoIter.hasNext() && !matched) {
                                String comparetoValue = (String) comparetoIter.next();  
                                if (compareValue != null && compareValue.equals(comparetoValue)) {
                                    matchingNodes.add(currentContext);                                        
                                    matched = true;
                                }
                            }
                            if(matched)     break;
                        }
                    }
                } else {
                    if (criteria.isCompareAttribute()) { //APSL4508
                        compareValue = currentContext.getAttributeValue(comparePath, criteria.getCompareXMLItem()); //APSL4508
                    } else {
                        compareValue = currentContext.getValue(comparePath, criteria.getCompareXMLItem()); //APSL4508
                    }   
                    // Keep if compare value matches any compareto value.
                    Iterator comparetoIter = comparetoValues.iterator();            
                    boolean matched = false;
                    while (comparetoIter.hasNext() && !matched) {
                        String comparetoValue = (String) comparetoIter.next();  
                        if (compareValue != null && compareValue.equals(comparetoValue)) {
                            matchingNodes.add(currentContext);                                        
                            matched = true;
                        }
                    }
                }
            }
            
            return matchingNodes;
        }

        /**
         * Determine the xml context of a block
         * 
         */
        private List determineBlockContext(MessageDefinition.Block currentBlock, String referringBlockXMLPath) {
            long start = System.currentTimeMillis();
            List results = new ArrayList();
            List tempNodes = new ArrayList();
            String blockPath = currentBlock.getXmlPath();
            MessageDefinition.InboundCriteria criteria = currentBlock.getInboundCriteria();
            // if the criteria compare value comes from an attribute, remove the enclosing element name to
            // get the block path
            // APSL4508 Begin
             String tempCompareXMLPath=criteria.getCompareXMLPath();
             if (criteria.isCompareAttribute()==true){
                 int indx=criteria.getCompareXMLPath().lastIndexOf('/');
                 tempCompareXMLPath=criteria.getCompareXMLPath().substring(0,indx);
            }
            boolean compareNodeIsParent = false;
            if (tempCompareXMLPath!=null && tempCompareXMLPath.length() < blockPath.length()) {
                // compare path is a parent of block path if block path starts with
                // criteria path.
                if (blockPath.startsWith(criteria.getCompareXMLPath())) {
                    compareNodeIsParent = true;
                }
                tempNodes = root.getItems(tempCompareXMLPath);
            } else {
                tempNodes = root.getItems(blockPath); 
            }
            //APSL4508 End
            // loop here when supporting multiple criteria
            tempNodes = applyInboundBlockCriteria(tempNodes, criteria, referringBlockXMLPath);
            
            if (compareNodeIsParent) {
                // get the child nodes of the parent nodes that matched the criteria above
                String relativePath = blockPath.substring(criteria.getCompareXMLPath().length()); //APSL4508
                if(relativePath.equals("") && blockPath.indexOf("/") > -1){
                    String parentPath = blockPath.substring(0, blockPath.lastIndexOf("/"));
                    if(parentPath.indexOf("/") > -1){
                        parentPath = parentPath.substring(parentPath.lastIndexOf("/"));
                    }
                    String nodeName = blockPath.substring(blockPath.lastIndexOf("/"));
                    relativePath = parentPath + nodeName;
                }
                Iterator nodeIterator = tempNodes.iterator();
                while (nodeIterator.hasNext()) {
                    XMLItem currentNode = (XMLItem) nodeIterator.next();                                    
                    List childNodes = currentNode.getItems(relativePath);
                    if (childNodes != null) {
                        Iterator childIterator = childNodes.iterator();
                        while (childIterator.hasNext()) {
                            results.add(childIterator.next());  
                        }
                    }
                }
            } else {
                results = tempNodes;
            }
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("determineBlockContext " + (end - start));
//          }
            return results;
        }
        
        private XMLItem applyInboundItemCriteria(MessageDefinition.Block block, MessageDefinition.Item item) {
            long start = System.currentTimeMillis();
            XMLItem retContext = null;
            // Determine value of <compareto> - either a constant specified by the <value>
            // element or derived from xmlitem and xmlpath, relative to current block context.
            List results = new ArrayList();
            MessageDefinition.InboundCriteria criteria = item.getInboundCriteria();
            List comparetoValues = new ArrayList();
            // Save values to use later when evaluating "<compare>" item. 
            if (criteria.compareToValues.size() > 0) {
                comparetoValues.addAll(criteria.compareToValues);
            }
            String comparetoPath = "";
            String blockPath = block.getXmlPath();
            String itemPath = item.getXmlPath();
            // If no <value> elements, evaluate the <compareto> element
            if (comparetoValues.isEmpty()) {
                String comparetoValue=null;
                if (itemPath!=null && itemPath.indexOf(blockPath)>=0){
                    comparetoPath = itemPath.substring(blockPath.length());
                }
                if (criteria.compareToAttribute) {
                    comparetoValue = getAttributeValue(comparetoPath, criteria.compareToXMLItem);
                } else {
                    comparetoValue = getValue(comparetoPath, criteria.compareToXMLItem);
                }
                if (comparetoValue!=null && comparetoValue.length()>0) {
                    comparetoValues.add(comparetoValue);
                }
            }
            
            // The compare path can be equal to, or child, or parent of the item path.
            boolean criteriaNodeIsParent = false;
            //***********DETERMINE THAT PATH TO THE LIST OF NODES WE WILL NEED TO EVALUATE THE CRITERIA
            //***********IT WILL BE EITHER THE CRITERIA XMLPATH, THE COMPARE XMLPATH, OR THE ITEM XMLPATH.
            String criteriaNodePath="";
            if (criteria.XMLPath!=null && criteria.XMLPath.length()>0) {
                if (criteria.compareXMLPath!=null && criteria.compareXMLPath.length()>0){
                    if (criteria.compareXMLPath.startsWith(criteria.XMLPath)){
                        // criteria path same or parent of compare path
                        criteriaNodePath=criteria.XMLPath;
                    } else {
                        // error*********  A/B/C(compare) - A/B/D(crit)
                        LogHandler.Factory.LogError(this,"<inboundcriteria> error for item: {0}", new Object[]{item.getName()});
                    }
                } else {
                    criteriaNodePath=criteria.XMLPath;              
                }
            } else {
                if (criteria.compareXMLPath!=null && criteria.compareXMLPath.length()>0){
                    criteriaNodePath=criteria.compareXMLPath;
                    // get the common path between the item and compare 
                    if (!pathIsAbsolute(criteriaNodePath)){
                        //get a global path and see what our context should be - in case a criteria XML path wasn't specified
                        criteriaNodePath=new StringBuffer(blockPath).append('/').append(criteriaNodePath).toString();
                    }
                    if (!pathIsAbsolute(itemPath)){
                        //get a global path and see what our context should be - in case a criteria XML path wasn't specified
                        StringBuffer sb=new StringBuffer(blockPath);
                        itemPath=new StringBuffer(blockPath).append('/').append(itemPath).toString();
                    }
                    criteriaNodePath=getCommonPath(criteriaNodePath,itemPath,blockPath);
                } else{
                    criteriaNodePath=item.xmlPath;
                    if (item.attribute){
                        criteriaNodePath = criteriaNodePath.substring(0,criteriaNodePath.lastIndexOf('/'));;
                    }
                }
            }
            
            //************DETERMINE THE CONTEXT BASED ON THE PATH: EITHER THE BLOCK CONTEXT OR USE THE GLOBAL CONTEXT
            String relativeCriteriaPath=criteriaNodePath.toString();
            XMLItem compareContext;
            // Compare the path we have against the current block path.  If related, use
            // current block context.  If not, it's a global path, so use global context.
            if (criteriaNodePath.startsWith("/")){
                if (criteriaNodePath.startsWith(blockPath)) { 
                    // <inboundcriteria> path is equal to or a child of current block path
                    // use current block context and determine relative path to the compare item
                    compareContext = this;
                    relativeCriteriaPath = criteriaNodePath.substring(blockPath.lastIndexOf('/'));
                } else {
                    // compare path not related to current block path, use global context
                    compareContext = root;
                }
            } else {
                compareContext = this;          
            }
            //*********GET THE XML NODES WE WANT BASED ON THE CRITERIA PATH AND CONTEXT DETERMINED ABOVE
            List tempNodes=null;
            if (relativeCriteriaPath.equals("")){
                // Node just contains compare context
                tempNodes=new ArrayList();
                tempNodes.add(compareContext);
            } else {
                tempNodes = compareContext.getItems(relativeCriteriaPath);
            }
            
            //*********NOW GET THE <compare> VALUE RELATIVE TO THE context for <inboundcriteria> 
            String comparePath = pathIsAbsolute(criteria.compareXMLPath)? criteria.compareXMLPath.substring(criteriaNodePath.lastIndexOf('/')):criteria.compareXMLPath;
            
            //Code Refactoring
            List matchingNodes = getMatchingNodes(tempNodes, criteria, comparetoValues, comparePath);
            
            // *********IF THE ITEM PATH OF THE COMPARE PATH, GET THE CHILD NODES OF THE PARENT NODES 
            // *********THAT MATCHED THE CRITERIA ABOVE
            if (itemPath.length()>criteriaNodePath.length()) {
                criteriaNodeIsParent = true;
                // get the relative item path within the criteria context
                itemPath=itemPath.substring(criteriaNodePath.lastIndexOf('/'));
            }
            
            if (criteriaNodeIsParent) {
                Iterator matchIterator = matchingNodes.iterator();
                boolean found = false;
                while (matchIterator.hasNext() && !found) {
                    XMLItem currentNode = (XMLItem) matchIterator.next();                                   
                    // assume item (&compare) always relative to criteria path.
                    List childNodes = currentNode.getItems(itemPath);
                    if (childNodes != null) {
                        Iterator childIterator = childNodes.iterator();
                        while (childIterator.hasNext() && !found) {
                            found = true;
                            retContext = (XMLItem)childIterator.next();
                        }
                    }
                }
            } else {
                if(matchingNodes.size() > 0) {
                    retContext = (XMLItem) matchingNodes.get(0);   
                }
            }
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("applyInboundItemCriteria " + (end - start));
//          }
            return retContext;
        }
        
        //vsonone: ACEL2546: to check whether the returned node is parent, i.e. if we need to use xmlpath and xmlitem or just xmlitem
        private boolean isParentNodeReturned(MessageDefinition.Item item) {
            boolean parentNode = true;
            String compareXMLPath = item.getInboundCriteria().compareXMLPath;
            if(compareXMLPath != null && !compareXMLPath.equals("")) {
                StringBuffer itemPath = new StringBuffer("");
                String xmlPath = item.getXmlPath();
                String xmlItem = item.getXmlItem();

                if(xmlPath != null && !xmlPath.equals("")) {
                    itemPath.append(xmlPath);
                }
                
                if(xmlItem != null && !xmlItem.equals("")) {
                    if(itemPath.length() != 0) {
                        itemPath.append("/");   
                    }
                    itemPath.append(xmlItem);
                }
                parentNode = !itemPath.toString().equals(compareXMLPath);           
            }
            return parentNode;
        }
        
        String getItemValue(MessageDefinition.Block block, MessageDefinition.Item item) {
            long start = System.currentTimeMillis();
            // if we have a predefined value, just return it
            if (item.getValue() != null && item.getValue().length() > 0) {
                return item.getValue();
            }
            // otherwise continue and get value from xml
            String result = null;
            XMLItem currentContext = null;
            String itemPath = item.getXmlPath();
            
            if (item.getInboundCriteria() != null) { //APSL4508
                currentContext = applyInboundItemCriteria(block, item);
                if (currentContext != null) {
                    itemPath = ""; // node returned is already narrowed to the item's node; full path not needed
                    //If item is returned which contains the value then use it
                    if(!isParentNodeReturned(item)) {
                        if (item.attribute) {
                            result = currentContext.getAttributeValue(item.getXmlItem());       
                        } else {
                            if (item.valueIsUnparsedNode()) {
                                result = getUnparsedNodeValue();
                            } else {    
                                result = currentContext.getValue();
                            }   
                        }
                        return result;
                    }
                }
            } else {
                if (pathIsAbsolute(itemPath)) {
                    currentContext = root;  // set to global context
                } else {
                    if(this.nodeName != null){
                        if (itemPath == null) {
                            itemPath = nodeName;
                        } else { 
                            if (!itemPath.equals(nodeName)) {
                                itemPath = this.nodeName + "/" + itemPath; 
                            }   
                        }       
                    }
                    currentContext = this;  // set to current block context
                }
            }
            
            if (currentContext == null) {
                return null;
            }
            
            if (item.isAttribute()) { //APSL4508
                result = currentContext.getAttributeValue(itemPath, item.getXmlItem());         
            } else {
                if (item.valueIsUnparsedNode()) {
                    result = getUnparsedNodeValue(itemPath, item.getXmlItem());
                } else {    
                    result = currentContext.getValue(itemPath, item.getXmlItem());
                }   
            }
            
            long end = System.currentTimeMillis();
//          if((end - start) > 0){
//              System.out.println("getItemValue " + (end - start));
//          }
            return result;      
        }       
        /**
         * @param block
         * @param item
         * @return
         */
        protected List getItemValues(MessageDefinition.Block block, MessageDefinition.Item item) {
			List result = new ArrayList();
			XMLItem currentContext = null;
			String itemPath = item.getXmlPath();
			if (pathIsAbsolute(itemPath)) {
				currentContext = root; // set to global context
			} else {
				if (this.nodeName != null) {
					if (itemPath == null) {
						itemPath = nodeName;
					} else {
						if (!itemPath.equals(nodeName)) {
							itemPath = this.nodeName + "/" + itemPath;
						}
					}
				}
				currentContext = this; // set to current block context
			}
			if (currentContext == null) {
				return null;
			}
			result = currentContext.getValues(itemPath, item.getXmlItem());
			return result;
		}         
 
        
        
    }

    
    public static class XMLParseSaxHandler extends DefaultHandler {

        private boolean processingElement = false;
        private StringBuffer tempvalue = null;
        XMLItem rootNode = null;
        XMLItem currentNode = null;

        /**
         * Default Constructor
         */
        public XMLParseSaxHandler() {
            super();
        }

        public void setRootNode(XMLItem item) {
            rootNode = item;
            rootNode.root = rootNode;
            currentNode = item;
        }

        /*
         * @see org.xml.sax.DefaultHandler#startElement(String,String,String,Attributes)
         */
        public void startElement(String uri, String name, String qName, Attributes atts) {
            processingElement = true;
            if(currentNode.nodeName != null){
                // current node already populated so create a new child node
                XMLItem newNode = new XMLItem();
                if(name == null || name.equals("")){
                    if(currentNode.children.containsKey(qName)){
                        Object obj = currentNode.children.get(qName);
                        List list = null;
                        if(obj instanceof XMLItem){
                            list = new ArrayList();
                            list.add(obj);
                        }  else if(obj instanceof List){
                            list = (List)obj;
                        }
                        list.add(newNode);
                        currentNode.children.put(qName, list);
                    } else {
                        currentNode.children.put(qName, newNode);
                    }
                } else {
                    if(currentNode.children.containsKey(name)){
                        Object obj = currentNode.children.get(name);
                        List list = null;
                        if(obj instanceof XMLItem){
                            list = new ArrayList();
                            list.add(obj);
                        }  else if(obj instanceof List){
                            list = (List)obj;
                        }
                        list.add(newNode);
                        currentNode.children.put(name, list);
                    } else {
                        currentNode.children.put(name, newNode);
                    }
                }
                newNode.parent = currentNode;
                newNode.root = rootNode;
                currentNode = newNode;
            }
            if(name == null || name.equals("")){
                currentNode.nodeName = qName;
            } else {
                currentNode.nodeName = name;
            }
            currentNode.nodeQName = qName;
            currentNode.uri = uri;
            int numAtts = atts.getLength();
            for(int i = 0; i < numAtts; i ++){
                String attName = atts.getLocalName(i);
                String attqName = atts.getQName(i);
                String value = atts.getValue(i);
                if(attName == null || attName.equals("")){
                    currentNode.attributes.put(attqName, value);
                } else {
                    currentNode.attributes.put(attName, value);
                }
                currentNode.qattributes.put(attqName, value);
            }
            tempvalue = new StringBuffer("");
        }

        /*
         * @see org.xml.sax.DefaultHandler#endElement(String,String,String)
         */
        public void endElement(String uri, String name, String qName) {
            processingElement = false;
            if(tempvalue != null && !tempvalue.equals("")){
                currentNode.value = tempvalue.toString();
                tempvalue = new StringBuffer("");
            }
            // pop to parent node...
            currentNode = currentNode.parent;
        }

        /*
         * @see org.xml.sax.DefaultHandler#characters(char[],int,int)
         */
        public void characters(char ch[], int start, int length) {
            if (processingElement) {
                String processItem = new String(ch, start, length);
                if (processingElement && processItem != null && !processItem.equals("")) {
                    tempvalue.append(processItem);
                } else if (processItem != null && !processItem.equals("")) {
                    tempvalue = new StringBuffer(processItem);
                }
            }
        }
    }    
    
    public XMLItem loadXML(String content){
        long start = System.currentTimeMillis();
        XMLItem result = new XMLItem();
        if (content != null && content.length() > 0) {
            // parse
            try {
                XMLParseSaxHandler saxHandler = new XMLParseSaxHandler();                   
                saxHandler.setRootNode(result);
                parse(saxHandler, content);
            } catch (Exception ex) {
                LogHandler.Factory.LogError("XML Message Manager", "Unable to parse response XML [{0}]", new Object[]{content});
                LogHandler.Factory.LogError("XML Message Manager", "parsing error: {0}", ex, new Object[]{ex.getMessage()});            
            }
        }
        long end = System.currentTimeMillis();
//      if((end - start) > 0){
//          System.out.println("loadXML " + (end - start));
//      }
        return result;
    }
    
    /**
     * This method is responsible for making the map of values that needs to be escaped. These values are picked from escapecharacters properties file.
     * Placed in the config folder.
     *
     */
    private static void buildEscapeMaps() {
        try {
            //ACEL3050 code deleted
            OrderedPropertiesLoader.load(ESCAPECHARACTERS_PROPERTIES, escapeKeysMap, escapeValuesMap);  //ACEL3050
            //ACEL3050 code deleted
        } catch (Exception ex) {
            LogHandler.Factory.LogError("XML Message Manager", "Unable To Load Properties file for Escape characters");
        }
    }

    static {
        saxParserFactory = SAXParserFactory.newInstance();
        // We are using the parser to parse only XML fragments so
        // 1. disable validation
        saxParserFactory.setValidating(false);
        // 2. disable namespace support
        saxParserFactory.setNamespaceAware(false);
        buildEscapeMaps();  //ACEL3050
    }
	public String userName;
	public String executionName; 
	
	public void initialize(String userName, String execution){
		this.userName = userName;
		this.executionName = execution;
	}
    /**
     * @param requestRecord          Description of Parameter
     * @param messageDef             Description of Parameter
     * @return                       Description of the Returned Value
     * @exception ResourceException  Description of Exception
     */
    public Object constructRequestMessage(Record requestRecord, MessageDefinition messageDef) throws ResourceException {
		long start = System.currentTimeMillis();
		String request = "";
		if (messageDef != null && messageDef.getMessageType().equals("XML")) {
			List requestBlocks = messageDef.getRequestBlocks();
			request = constructOutboundMessage(requestBlocks, requestRecord, messageDef);
		}
		long end = System.currentTimeMillis();
		return request;
	}

    void addItemValue(MessageDefinition messageDef, MessageDefinition.Block block, MessageDefinition.Item currentItem, XMLItem currentBlockContext, Record subRecord) throws ResourceException{
        long start = System.currentTimeMillis();
        if (currentItem != null && currentBlockContext != null) {
            if (!processForRedefinableItem(messageDef, currentItem, currentBlockContext, subRecord)) {
                String itemValue = null;
                itemValue = currentBlockContext.getItemValue(block, currentItem);
                if (itemValue==null || itemValue.length()==0){
                    if(currentItem.getDefaultValue()!= null){
                        itemValue = currentItem.getDefaultValue();
                    } else if(currentItem.getValue() != null){
                        itemValue = currentItem.getValue(); 
                    }
                } else {    //ACEL3050
                    itemValue = getEscapeValue(itemValue, false); //ACEL3050
                }
                if (!processForSpecialItem(currentItem, itemValue)) {
                    // convert itemValue to the proper format and type, then add to record
                    Object currentObj = currentItem.getformattedValueFromString(itemValue);
                    if (currentObj!=null){
                        ((MappedRecord) subRecord).put(currentItem.getName(), currentObj);
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
//      if((end - start) > 0){
//          System.out.println("addItemValue " + (end - start));
//      }
    }

    protected void addItemValues(MessageDefinition messageDef, MessageDefinition.Block block, MessageDefinition.Item currentItem,
			XMLItem currentBlockContext, Record subRecord) throws ResourceException {
		long start = System.currentTimeMillis();
		if (currentItem != null && currentBlockContext != null) {
			if (!processForRedefinableItem(messageDef, currentItem, currentBlockContext, subRecord)) {
				List itemValues = null;
				itemValues = currentBlockContext.getItemValues(block, currentItem);
				int count = itemValues.size();
				String itemValue;
				for (int i = 0; i < count; i++) {
					itemValue = (String) itemValues.get(i);
					if (itemValue == null || itemValue.length() == 0) {
						if (currentItem.getDefaultValue() != null) {
							itemValue = currentItem.getDefaultValue();
						} else if (currentItem.getValue() != null) {
							itemValue = currentItem.getValue();
						}
					} else {
						itemValue = getEscapeValue(itemValue, false);
					}
					Object currentValue = currentItem.getformattedValueFromString(itemValue);
					if (currentValue != null) {
						MappedRecord itemRecord = new SimpleMappedRecord();
						itemRecord.put(currentItem.getName(), currentValue);
						((MappedRecord) subRecord).put(new Integer(i), itemRecord);
					}
				}
			}
		}
		long end = System.currentTimeMillis();
	}
    
    /**
     * Transforms the given message block using the current message definition
     * into the result record provided.
     * <p/>
     * The remaining part of the message is passed back to the caller for
     * further processing.
     *
     * @param messageDef    Current message defnition
     * @param currentBlock  Block definition to use
     * @param resultRecord  Record to populate
     * @param input         Message to transform
     * @return              Remaining part of the response message not processed in this block
     */
    protected void processMessageBlock(MessageDefinition messageDef,
                                        MessageDefinition.Block currentBlock,
                                        Record resultRecord,
                                        XMLItem referringBlockContext,
                                        String referringBlockPath) throws ResourceException {
        long start = System.currentTimeMillis();
        
        // Override fields in current block with fields from referring block.  This allows
        // a block def to be more reusable.
        if (currentBlock.getExternalName() != null) {
            MessageDefinition.Block tempBlock = currentBlock;
            MessageDefinition.Block newBlock = MessageLoader.getBlock(currentBlock.getExternalName());
            if (newBlock != null) {
                currentBlock = newBlock.copy();
                currentBlock.setInboundCriteria(tempBlock.getInboundCriteria());
                String alias = tempBlock.getAlias();
                if (alias != null && alias.length() > 0) {
                    currentBlock.setAlias(alias);
                }
                // currentBlock.setBlockRepeats(tempBlock.isRepeatingBlock());
                String xmlPath = tempBlock.getXmlPath();
                if (xmlPath != null && xmlPath.length() > 0) {
                    currentBlock.setXmlPath(xmlPath);
                }
            } else {
                LogHandler.Factory.LogError("XML Message Manager", "Unable To Load External Block [{0}]", new Object[]{currentBlock.getExternalName()});
                throw new ResourceException("Unable To Load External Block [" + currentBlock.getExternalName() + "] in Message Definition [" + messageDef.getName() + "]");
            }
        }
        
        /*
         * if block path is relative to referring path, get the whole path and save it. if there is no xml path, use the
         * referring block path.
         */
        String currentBlockXmlPath = currentBlock.getXmlPath();
        if (currentBlockXmlPath != null && !currentBlockXmlPath.startsWith("/")) {
            currentBlock.setXmlPath(referringBlockPath + '/' + currentBlockXmlPath);
        } else {
            if (currentBlockXmlPath == null || currentBlockXmlPath.length() == 0) {
                if (referringBlockPath != null && referringBlockPath.length() > 0) {
                    currentBlock.setXmlPath(referringBlockPath);
                }
            }
        }
        List blockChildren = currentBlock.getChildren();
        XMLItem currentBlockContext = null;
        
        // Create a block-level record. For a repeating block, it will contain the
        // sub-records.  For a non-repeating block, it will contain the detail items.
        Record highLevelRecord = new SimpleMappedRecord();
        
        // gets all of the full node strings based on the block's xmlpath
        // for a repeating block, will return 0 - n nodes
        // for a non-repeating block, will return 0 or 1 nodes
        List nodes = new ArrayList();
        if (currentBlock.getInboundCriteria() == null) {
            if (referringBlockPath != null && (!referringBlockPath.equals("")) && currentBlock.getXmlPath().startsWith(referringBlockPath) && !currentBlock.getXmlPath().equals(referringBlockPath)) {
                // nodes = getXMLNodeList(currentBlock.getXmlPath().substring(referringBlockPath.length() + 1),
                // referringBlockContext);
                // make sure the path we pass in contains the begining element in the referring block context otherwise
                // we can't match up the path in getXMLNodeList()
                int x1 = referringBlockPath.lastIndexOf('/');
                String s1 = referringBlockPath.substring(0, x1);
                nodes = referringBlockContext.getItems(currentBlock.getXmlPath().substring(s1.length() + 1));
            } else {
                nodes = referringBlockContext.getRoot().getItems(currentBlock.getXmlPath());
            }
        } else {
            nodes = referringBlockContext.determineBlockContext(currentBlock, referringBlockPath);
        }
        
        Iterator nodeIterator = nodes.iterator();
        if (currentBlock.isRepeatingBlock()) {
            int i = 0;
            while (nodeIterator.hasNext()) {
                currentBlockContext = (XMLItem) nodeIterator.next();
                Iterator blockChildIter = blockChildren.iterator();
                // create record containing the detail items
                Record subRecord = new SimpleMappedRecord();
                while (blockChildIter.hasNext() && currentBlockContext != null) {
                    Object obj = blockChildIter.next();
                    if (obj instanceof MessageDefinition.Block) { 
                        // make recursive call to this method
                        processMessageBlock (messageDef, (MessageDefinition.Block)obj, subRecord, currentBlockContext, currentBlock.getXmlPath());
                    } else if (obj instanceof MessageDefinition.Item) {
                    	 MessageDefinition.Item currentItem = (MessageDefinition.Item) obj;
                    	if ("PCDATA".equals(currentItem.getAlias())) {
                    		addItemValues(messageDef, currentBlock, currentItem, currentBlockContext, highLevelRecord);
                    	} else {                       
                    		addItemValue(messageDef, currentBlock, currentItem, currentBlockContext, subRecord);
                    	}
                    }       
                }
                if (!((MappedRecord) highLevelRecord).containsKey(new Integer(i))) {
					((MappedRecord) highLevelRecord).put(new Integer(i), subRecord);
				}
                i++;
            }
        } else {
            // non-repeating block
            if (nodes != null && nodes.size() > 0) {
                currentBlockContext = (XMLItem) nodes.get(0);   
            }
            if(currentBlockContext != null){
                Iterator blockChildIter = blockChildren.iterator();
                while (blockChildIter.hasNext()) {
                    Object obj = blockChildIter.next();
                    if (obj instanceof MessageDefinition.Block) {
                        // make recursive call to this method
                        processMessageBlock (messageDef, (MessageDefinition.Block)obj, highLevelRecord, currentBlockContext, currentBlock.getXmlPath());
                    } else if (obj instanceof MessageDefinition.Item) {
                        MessageDefinition.Item currentItem = (MessageDefinition.Item) obj;
                        addItemValue(messageDef, currentBlock, currentItem, currentBlockContext, highLevelRecord);
                    }
                }
            }
        }
        highLevelRecord.setRecordName(currentBlock.getAliasName());
        ((MappedRecord) resultRecord).put(currentBlock.getAliasName(), highLevelRecord);
        long end = System.currentTimeMillis();
//      if((end - start) > 0){
//          System.out.println("processMessageBlock " + (end - start));
//      }
    }
	public void constructResponseRecord(Object request, Object response, MessageDefinition messageDef, Record resultRecord) throws ResourceException {
		constructResponseRecord(response, messageDef, resultRecord);
	
}
 
    /**
     * @param response
     * @param messageDef
     * @param resultRecord
     * @throws ResourceException
     */
    public void constructResponseRecord(Object response, MessageDefinition messageDef, Record resultRecord) throws ResourceException {
		long start = System.currentTimeMillis();
		String responsemessage;
		try {
			if (response instanceof String) {
				responsemessage = (String) response;
			} else {
				Set set = ((SimpleMappedRecord) response).keySet();
				Object key = set.iterator().next();
				Element element = (Element) ((SimpleMappedRecord) response).get(key);
				responsemessage = dumpDOM(element);
			}
			LogHandler.Factory.LogXML("XMLMessageManager", "Construct Response [{0}] [{1}] XML [{2}]", new Object[] { userName, executionName, responsemessage }); 
			if (resultRecord != null && messageDef != null && responsemessage != null && !responsemessage.equals("") 	&& messageDef.getMessageType().equals("XML")) {
				List responseBlocks = messageDef.getResponseBlocks();
				if (responseBlocks != null && !responseBlocks.isEmpty()) {
					String processResponse = (String) responsemessage;
					// Load XML parse Structure using SAX Handler..
					XMLItem rootElement = loadXML(processResponse);
					Iterator responseBlockIter = responseBlocks.iterator();					
					if (processResponse != null && !processResponse.equals("")) {
						while (responseBlockIter.hasNext()) {
							MessageDefinition.Block currentBlock = (MessageDefinition.Block) responseBlockIter.next();
							processMessageBlock(messageDef, currentBlock, resultRecord, rootElement, "");
						}
					}
				} else {
					XMLtoMappedRecordParser.populateMappedRecordFromXMLString((String) responsemessage, (SimpleMappedRecord) resultRecord);
				}
			}
		} catch (Exception ex) {
			ResourceException rex = new ResourceException("Failed to build response record in Message Manager");
			rex.setLinkedException(ex);
			long end = System.currentTimeMillis();
			LogHandler.Factory.LogError("XMLMessageManager", "Construct Response Record [{0}] [{1}] error [{2}-{3}]", ex, new Object[] { userName,
					executionName, ex.getClass().getName(), ex.getMessage() });
			LogHandler.Factory.LogTxnPerf("XMLMessageManager", "Construct Response Record [{0}] [{1}] completed unsuccessfully :{2}", new Object[] {
					userName, executionName, new Long(end - start) });
			throw rex;
		}
		long end = System.currentTimeMillis();
		LogHandler.Factory.LogTxnPerf("XMLMessageManager", "Construct Response Record [{0}] [{1}] completed successfully :{2}", new Object[] {
				userName, executionName, new Long(end - start) });
	}
	 /**
	 * @param root
	 * @return
	 */
	public String dumpDOM(Element root) {
		//		DOM2Writer writer = new DOM2Writer();
		if (root != null) {
			return DOM2Writer.nodeToString(root, true);
		}
		return "";
	}
    /**
     * processes the current item for any special items, such as num of repeating blocks,
     * fixed block length etc.
     *
     * @param currentItem  Description of Parameter
     * @param itemValue    Description of Parameter
     * @return             indicator to show that this was a special item and that no further processing is required
     */
    private boolean processForSpecialItem(MessageDefinition.Item currentItem, String itemValue) {
        boolean result = true;
        if (currentItem.getName().equals("DUMMY")) {
            // Do nothing
        } else {
            result = false;
        }
        return result;
    }

    /**
     * processes the current item redefinable items.  This checks to see if the
     * current items definition specifies any redefinable clauses.  If found the items value
     * will be treated as a speparate input to the redefined block.  This will be parsed into the
     * main result record.
     *
     * @param messageDef         Description of Parameter
     * @param currentItem        Description of Parameter
     * @param itemValue          Description of Parameter
     * @param resultRecord       Description of Parameter
     * @return                   boolean - indicator to show that this was a redefinable item and that no further processing is required
     */
    private boolean processForRedefinableItem(MessageDefinition messageDef, MessageDefinition.Item currentItem, XMLItem context, Record resultRecord) throws ResourceException {
        boolean result = false;
        // check to see if we are in a redefining block...
        List redefineItems = currentItem.getRedefineItems();
        if (redefineItems != null) {
            result = true;
            // this is a redefinable item
            Iterator iter = redefineItems.iterator();
            MessageDefinition.Block redefineBlock = null;
            while (iter.hasNext() && redefineBlock == null) {
                MessageDefinition.Item.RedefineItem redefineItem = (MessageDefinition.Item.RedefineItem) iter.next();
                String checkItemValue = null;
                checkItemValue = (String) ((MappedRecord) resultRecord).get(redefineItem.itemName);
                if (checkItemValue != null && checkItemValue.equals(redefineItem.value)) {
                    redefineBlock = messageDef.getRedefineBlock(redefineItem.blockName);
                }
            }
            if (redefineBlock != null) {
                processMessageBlock(messageDef, redefineBlock, resultRecord, context, "");
            }
        }
        return result;
    }

    /**
     * @param requestRecord          Description of Parameter
     * @param messageDef             Description of Parameter
     * @return                       Description of the Returned Value
     * @exception ResourceException  Description of Exception
     */
    public Object constructPreSendMessage(Record requestRecord, MessageDefinition messageDef) throws ResourceException {
        String request = null;
        if (messageDef != null && messageDef.getMessageType().equals("XML")) {
            List requestBlocks = messageDef.getPresendBlocks();
            if (requestBlocks!=null && requestBlocks.size()>0){
                request = constructOutboundMessage(requestBlocks, requestRecord, messageDef);
            }
        }
        return request;
    }

    /**
     * @param currentBlock           Description of Parameter
     * @param requestRecord          Description of Parameter
     * @param messageDef             Description of Parameter
     * @return                       Description of the Returned Value
     */

    protected String getBlockXML(MessageDefinition.Block currentBlock, Record requestRecord, MessageDefinition messageDef, Record originalRecord) {
        String result = "";
        if (currentBlock.getExternalName() != null) {
            currentBlock = MessageLoader.getBlock(currentBlock.getExternalName());
        }
        List children = currentBlock.getChildren();
        Iterator blockChildIter = children.iterator();
        StringBuffer blockBuffer=new StringBuffer();
        String openString ="<" + currentBlock.getName();
        blockBuffer.append(openString); 
        
        boolean startOfBlock = true;
        String lastElementName = "";
        String lastElementValue = "";
        StringBuffer itemBuffer=new StringBuffer();
        while (blockChildIter.hasNext()) {
            Object obj = blockChildIter.next();
            if (obj instanceof MessageDefinition.Block) {
                MessageDefinition.Block block = (MessageDefinition.Block)obj; 
                // close existing block tag if necessary
                if (startOfBlock) {
                    startOfBlock = false;
                }
                // write existing element tag and value if necessary
                if (lastElementName != null && !lastElementName.equals("")) {                   
                    appendItem(blockBuffer,itemBuffer,lastElementName,lastElementValue);
                    itemBuffer=new StringBuffer();
                    lastElementName = null;
                }
                // process this block
                if (block.getRecordPath()!=null){
                    if (!block.isRepeatingBlock()){
                        //If there is a recordpath at the block level, it should point to a item.
                        // Only include the block if the record path can be resolved.
                        Object pathValue=getValueFromRecord(block.getRecordPath(),requestRecord);
                        if (pathValue==null){
                            continue;
                        }
                    }
                }
                if (block.isRepeatingBlock()) { //APSL4508
                    Iterator repeatingRecordIter = null;
                    // need to retrieve the List/Map for this repeating Block
                    // use the recordpath to get the correct record to use
                    Map repeatingRecords = null;
                    if (block.getRecordPath()!=null){
                        Record subRecord=getSubRecord(block.getRecordPath(),requestRecord);
                        repeatingRecords = (Map) ((MappedRecord) subRecord).get(block.getAliasName());
                    } else {
                        repeatingRecords = (Map) ((MappedRecord) requestRecord).get(block.getAliasName());
                    }
                    if (repeatingRecords != null) {
                        Set repeatingRecordSet = new TreeSet(repeatingRecords.keySet());
                        repeatingRecordIter = repeatingRecordSet.iterator();
                    }
                    while ((repeatingRecordIter != null && repeatingRecordIter.hasNext())) {
                        Record currentRepRec = null;
                        if (repeatingRecordIter != null) {
                        	Object xx = repeatingRecordIter.next();
                            currentRepRec = (Record) repeatingRecords.get(xx);
                        }
                        if (!checkOutboundCriteria(block, currentRepRec)) {
                            continue;
                        }
                        appendBlock(blockBuffer,getBlockXML(block, currentRepRec, messageDef, originalRecord));
                    }
                } else {
                    if (!checkOutboundCriteria(block, requestRecord)) {
                        continue;
                    }
                    appendBlock(blockBuffer,getBlockXML(block, requestRecord, messageDef, originalRecord));
                }
            } else if (obj instanceof MessageDefinition.Item) {
                MessageDefinition.Item currentItem = (MessageDefinition.Item)obj;
                Object value = null;
                if (currentItem.isDontProcessItem()) {
                    value = currentItem.getValue();
                } else {
                    String recordPath;      
                    if (currentItem.getRecordPath() != null) {
                        recordPath = currentItem.getRecordPath();
                        if (recordPath.startsWith("/")){
                            value = getValueFromRecord(recordPath, originalRecord, currentItem.getName());                          
                        } else {
                            value = getValueFromRecord(recordPath, requestRecord, currentItem.getName());
                        }
                    } else {
                        value = ((MappedRecord) requestRecord).get(currentItem.getName());
                        //recordPath = currentBlock.getRecordPath();    
                    }
                    //value = getValueFromRecord(recordPath, requestRecord, currentItem.getName());
                    if (value == null) {
                        value = currentItem.getDefaultValue();
                        //begin NBA234
//                    } else {    //ACEL3050
//                        if (value instanceof String ){
//                            value = getEscapeValue((String) value, true); //ACEL3050                            
//                        }
                        //end NBA234
                    }
                }   
                List redefItems = currentItem.getRedefineItems();
                if ((redefItems != null) && (redefItems.size() > 0)) {
                    // sub nodes
                    if (startOfBlock) {
                        blockBuffer.append(">");                        
                        startOfBlock = false;
                    }
                    Iterator iter = redefItems.iterator();
                    if (lastElementName != null && !lastElementName.equals("")) {
                        blockBuffer.append(">").append(lastElementValue).append( "</").append(lastElementName).append(">"); //ACEL3050
                        lastElementName = null;
                    }
                    boolean processed = false;
                    while (iter.hasNext() && !processed) {
                        MessageDefinition.Item.RedefineItem redefItem = (MessageDefinition.Item.RedefineItem) iter.next();
                        if (redefItem.value == null) {
                            // Process this block
                            MessageDefinition.Block block = messageDef.getRedefineBlock(redefItem.blockName);
                            processBlock(block, messageDef, requestRecord, blockBuffer, originalRecord);
                            processed = true;
                        }
                    }
                } else {
                    if (currentItem.isAttribute()) {  
                        if (!isEmptyValue(value)) {
                            if (itemBuffer.length()==0){
                                blockBuffer.append(" ").append(currentItem.getXmlItem()).append("=\"").append(currentItem.getFormattedValue(value)).append("\" ");  //ACEL3050
                            }
                            else {
                                itemBuffer.append(" ").append(currentItem.getXmlItem()).append("=\"").append(currentItem.getFormattedValue(value)).append("\" ");  //ACEL3050
                            }                           
                        }
                    } else if("PCDATA".equals(currentItem.getAliasName())) {
                        if (!isEmptyValue(value)) {
                        	lastElementName = currentItem.getAliasName();
                        	blockBuffer.append(">").append(value);
                        }
                    } else {  // not an attribute                   

                        if (itemBuffer.length()>0) {
                            // if element has value or attributes write XML
                            appendItem(blockBuffer,itemBuffer,lastElementName,lastElementValue);
                            itemBuffer=new StringBuffer();                          
                        }
                        
                        itemBuffer.append("<" + currentItem.getXmlItem());

                        lastElementValue = currentItem.getFormattedValue(value);
                        lastElementName = currentItem.getXmlItem();
                        startOfBlock = false;                   
                    }
                }
            }
        }   
        if (lastElementName != null && !lastElementName.equals("")) {           
            appendItem(blockBuffer,itemBuffer,lastElementName,lastElementValue);
        }
        // don't add block if all empty tags
        if (blockBuffer.length()>openString.length()){
        	if ("PCDATA".equals(lastElementName)){
        		blockBuffer.append("</").append(currentBlock.getName()).append(">");
        	} else {
        		appendBlock(blockBuffer,"</" + currentBlock.getName() + ">");
        	}
            result=blockBuffer.toString();
        } else {
            result="";
        }
        return result;
    }

    private boolean checkOutboundCriteria(MessageDefinition.Block block, Record requestRecord) {
        if (block.getOutboundCriteria() != null) {
            MessageDefinition.OutboundCriteria outboundCriteria = block.getOutboundCriteria();
            //APSL4508 Begin
            if (outboundCriteria.getCompareRecordItem()!= null) {
                Object compValue = getValueFromRecord((outboundCriteria.getCompareRecordPath()!= null) ? outboundCriteria.getCompareRecordPath(): block.getRecordPath(), requestRecord,
                        outboundCriteria.getCompareRecordItem());
                if (compValue != null) {
                    String compareValue = String.valueOf(compValue);    //TODO-vikrant//TypeCast to String
                    if (outboundCriteria.getCompareToRecordItem() != null) {
                        Object compToValue = getValueFromRecord((outboundCriteria.getCompareToRecordPath()!= null) ? outboundCriteria.getCompareToRecordPath() : block.getRecordPath(), requestRecord,
                                outboundCriteria.getCompareToRecordItem());
                        if (compToValue != null) {
                            // TODO-add support for other condtions, ne,gt,lt, etc.?
                            if (!compareValue.equals(String.valueOf(compToValue))) {//TODO-vikrant
                                return false;
                            }
                        }
                    } else {
                        boolean match = false;
                        if (outboundCriteria.getCompareToValues()!= null && outboundCriteria.getCompareToValues().size() > 0) {
                            for (int i = 0; i < outboundCriteria.getCompareToValues().size() && !match; i++) {
                                Object value = outboundCriteria.getCompareToValues().get(i);
                                if (compareValue.equals(String.valueOf(value))) {//TODO-vikrant
                                    match = true;
                                }
                            }
                            if (!match) {
                                return false;
                            }
                        }
                    }
                } else {
                	return false; 
                }
            }
        }
        return true;        
    }
   //APSL4508 End
    /*
     * Make sure we close xml tags if necessary when adding to blocks
     */
    private void appendBlock(StringBuffer blockBuffer, String block){
        if (block!=null && block.length()>0){
            if (!blockBuffer.toString().endsWith(">")){
                blockBuffer.append(">");
            }
            blockBuffer.append(block);
        }
    }
    /*
     * Close the xml element for an item if it has attributes and/or a value and append it to the block xml
     */
    private boolean appendItem(StringBuffer blockBuffer, StringBuffer itemBuffer, String lastElementName, String lastElementValue){
        if (itemBuffer.length()>("<" + lastElementName).length() || !isEmptyValue(lastElementValue)) {
            itemBuffer.append(">").append(lastElementValue).append("</").append(lastElementName).append(">");       //ACEL3050
            if (!blockBuffer.toString().endsWith(">")){
                blockBuffer.append(">");                            
            }
            blockBuffer.append(itemBuffer);
            return true;
        }
        else {
            return false;
        }
    }   
    /**
     * For this method, record path should be the path down to the item name
     */
    private Object getValueFromRecord(String recordPath, Record record) {
        Object value = null;
        Object subRecord = null;
        String itemName=null;
        List pathNodes = parsePath(recordPath);
        // take the item name out of the path variable leaving just the path
        // to the item.
        if (pathNodes.size()>1){
            itemName=(String)pathNodes.get(pathNodes.size()-1);
            pathNodes.remove(pathNodes.size()-1);
        }
        else {
            return null;
        }
        Iterator nodes = pathNodes.iterator();
        subRecord = record;
        try {
            while (nodes.hasNext()) {
                subRecord = ((MappedRecord) subRecord).get(nodes.next());
            }
            // get item value from record
            value = ((MappedRecord) subRecord).get(itemName);
        } catch (Exception e) {
            return null;    
        }
        
        return value;
    }
    
    private boolean isEmptyValue(Object value){
        if (value instanceof String){
            String str=(String)value;
            if (str==null || str.length()==0){
                return true;
            }
        } 
        else {
            if (value==null){
                return true;
            }
        }
        return false;
    }
    /**
     * internal method to create an outbound message from the given list of blocks
     *
     * @param requestBlocks  Description of Parameter
     * @param requestRecord  Description of Parameter
     * @param messageDef     Description of Parameter
     * @return               Description of the Returned Value
     */
    private String constructOutboundMessage(List requestBlocks, Record requestRecord, MessageDefinition messageDef) {
        StringBuffer requestMessage = new StringBuffer(5000);
        Iterator requestBlockIter = requestBlocks.iterator();
          while (requestBlockIter.hasNext()) {
            MessageDefinition.Block currentBlock = (MessageDefinition.Block) requestBlockIter.next();
            requestMessage.append(getBlockXML(currentBlock, requestRecord, messageDef, requestRecord));
        }
  
        return requestMessage.toString();
    }


    /**
     * 
     */
    private void processBlock(MessageDefinition.Block block,
                              MessageDefinition messageDef,
                              Record requestRecord,
                              StringBuffer result,
                              Record originalRecord) {
        if (block.isRepeatingBlock()) {   //APSL4508
            Iterator repeatingRecordIter = null;
            // need to retrieve the List/Map for this repeating Block
            // use the recordpath to get the correct record to use
            Map repeatingRecords = (Map) ((MappedRecord) requestRecord).get(block.getAliasName());
            if (repeatingRecords != null) {
                Set repeatingRecordSet = new TreeSet(repeatingRecords.keySet());
                repeatingRecordIter = repeatingRecordSet.iterator();
            }
            while ((repeatingRecordIter != null && repeatingRecordIter.hasNext())) {
                Record currentRepRec = null;
                if (repeatingRecordIter != null) {
                    currentRepRec = (Record) repeatingRecordIter.next();
                }
                result.append(getBlockXML(block, currentRepRec, messageDef, originalRecord));
            }
        } else {
            result.append(getBlockXML(block, requestRecord, messageDef, originalRecord));
        }
    }   

    /**
     * 
     */
    protected Object getValueFromRecord(String recordPath, Record record, String itemName) {
        Object value = null;
        Object subRecord = null;
        List pathNodes = parsePath(recordPath);
        Iterator nodes = pathNodes.iterator();
        subRecord = record;
        try {
            while (nodes.hasNext()) {
                subRecord = ((MappedRecord) subRecord).get(nodes.next());
            }
            // get item value from record
            value = ((MappedRecord) subRecord).get(itemName);
        } catch (Exception e) {
            return null;    
        }
        
        return value;
    }

    /**
     * 
     */
    private Record getSubRecord(String recordPath, Record record) {
        Record subRecord = null;
        List pathNodes = parsePath(recordPath);
        Iterator nodes = pathNodes.iterator();
        subRecord = record;
        try {
            while (nodes.hasNext()) {
                subRecord = (Record)((MappedRecord) subRecord).get(nodes.next());
            }
        } catch (Exception e) {
            return null;    
        }
        
        return subRecord;
    }

    /**
     * @param source           Description of Parameter
     * @return                 Description of the Returned Value
     * @exception IOException  Description of Exception
     */
    public Object compressStream(Object source) throws IOException {
        return source;
    }

    /**
     * @param response               Description of Parameter
     * @param messageDef             Description of Parameter
     * @param resultRecord           Description of Parameter
     * @param responseName           Description of Parameter
     * @exception ResourceException  Description of Exception
     */
    public void constructResponseRecord(Object request, Object response, MessageDefinition messageDef, Record resultRecord, String responseName) throws ResourceException {
        constructResponseRecord(request, response, messageDef, resultRecord);
    }

    /**
     * @param source           Description of Parameter
     * @return                 Description of the Returned Value
     * @exception IOException  Description of Exception
     */
    public Object uncompressStream(Object source) throws IOException {
        return source;
    }

    
    // JAXP 3.0 specification supports reuse of SAX parser instances by utilizing reset() function
    // However, theis version does not support this functionality 
    private DefaultHandler parse(DefaultHandler handler, String xmlContent) throws Exception {
        if (handler != null) {
            SAXParser saxParser = null;
            try {
                saxParser = saxParserFactory.newSAXParser();
                if (saxParser != null) {
                    InputSource is = new InputSource(new StringReader(xmlContent));
                    // ensure fast parsing with 'US-ASCII' encoding
                    is.setEncoding("US-ASCII");
                    saxParser.getXMLReader().setErrorHandler(handler);
                    try {
                        saxParser.parse(is, handler);
                    } catch (Throwable th) {}
                }                           
            } finally {
                // improve gc
                saxParser = null;
            }
        }
        return handler;
    }

    /**
     * Parses a path expression and returns the individual node names
     * in a list.
     * 
     */
    private static List parsePath(String path) {
        if (path == null || path.equals(""))  {
            return Collections.EMPTY_LIST;
        }
        // if path is the root '/', just pass back an empty list
        List nodes = new ArrayList();
        if (path.trim().equals("/")) {
            return nodes;
        }
        String currentNode = "";
        boolean parsed = false;
        int beginIndex = 0;
        if (pathIsAbsolute(path)) {
            beginIndex = 1;
        }
        while (!parsed) {
            int endIndex = path.indexOf('/', beginIndex);
            if (endIndex < 0) {
                // end of expression has been reached
                parsed = true;
                currentNode = path.substring(beginIndex);
            } else {
                currentNode = path.substring(beginIndex, endIndex);     
                beginIndex = endIndex + 1;
            }
            nodes.add(currentNode);
        }       
        
        return nodes;
    }

    private static Stack parsePath(List path) {
        if (path == null)  {
            return new Stack();
        }
        Stack nodes = new Stack();
        int beginIndex = 0;
        for(int i = path.size(); i >=0; i --){
            nodes.push(path.get(i));
        }
        return nodes;
    }

    private static Stack parsePathAsStack(String strpath) {
        List path = parsePath(strpath);
        if (path == null)  {
            return new Stack();
        }
        Stack nodes = new Stack();
        int beginIndex = 0;
        for(int i = path.size()-1; i >=0; i --){
            nodes.push(path.get(i));
        }
        return nodes;
    }

    
    private static boolean pathIsAbsolute(String path) {
        if (path==null || path.length()==0){
            return false;
        }
        return path.charAt(0) == '/';
    }
    
    /**
     * If the value is not CDATA, replace any escape characters in the value
     * @param value
     * @param outbound - true for outbound data, false for inbound
     * @return the updated value
     */
    //ACEL3050 New Method
    private static String getEscapeValue(String value, boolean outbound) {
        Map mapToUse = outbound ? escapeKeysMap : escapeValuesMap;
        if (value.indexOf(CDATA) < 0) {
            Iterator keyIterator = mapToUse.keySet().iterator();
            String key;
            while (keyIterator.hasNext()) {
                key = (String) keyIterator.next();
                value = value.replaceAll(key, (String) mapToUse.get(key));
            }
        }
        return value;
    }

    
    // JK - Test Harness Code Below ********************************************

    public static void main(String[] args){
        
        List steps = new LinkedList();
        setupTestData(steps);
        
        int numThreads = 50;
        int numRuns = 30;
        for(int i = 0; i < numThreads; i++){
            TestRunner runner = new TestRunner(numRuns, steps);
            Thread thread = new Thread(runner);
            thread.setName("Run " + i);
            thread.run();
        }
    }
    
    public static class TestRunner implements Runnable{
        
        int numRuns = 10;
        List steps;
        
        public TestRunner(int numRuns, List steps){
            this.numRuns = numRuns;
            this.steps = steps;
        }
        
            /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
//          MessageManager mm = new MessageManagerAWDNetServerOriginal();
//          executeRun(mm, steps, Thread.currentThread().getName() + " " + "Control / Initialization");
//
//          MessageManager mmnew = new MessageManagerAWDNetServer();
//          executeRun(mm, steps, Thread.currentThread().getName() +" " + "Control / Initialization (New Handler)");
            
            System.out.println(Thread.currentThread().getName() + "****************** Start with NEW Handler");

            for(int i = 0; i < numRuns; i++){
                MessageManager mmnew = new MessageManagerAWDNetServer();
                executeRun(mmnew, steps, Thread.currentThread().getName() +" " + "Test Run (New Handler) " + i);
            }
            
//          System.out.println(Thread.currentThread().getName() +" " + "****************** Start with OLD Handler");
//
//          for(int i = 0; i < numRuns; i++){
//              MessageManager mm = new MessageManagerAWDNetServerOriginal();
//              executeRun(mm, steps, Thread.currentThread().getName() + " " + "Test Run (Old Handler) " + i);
//          }

        }
}
    
    public static void executeRun(MessageManager mm, List tests, String testOutputPrefix){

        System.out.println(Thread.currentThread().getName() +" " + "****************** START " + testOutputPrefix + " ******************");

        int total = 0;
        Iterator iter = tests.iterator();
        while(iter.hasNext()){
            TestStep step = (TestStep)iter.next(); 
            Record record = new SimpleMappedRecord();
            long start = System.currentTimeMillis();
            try{
                long startdefload = System.currentTimeMillis();
                MessageDefinition def = MessageLoader.get(step.defName);
                long enddefload = System.currentTimeMillis();
                total += (enddefload - startdefload);
                System.out.println(testOutputPrefix + "Def Load Time [" + step.defName + "] " + (enddefload - startdefload));
                mm.constructResponseRecord(null, step.stream, def, record);
            }catch(ResourceException ex){
                System.out.println(Thread.currentThread().getName() +" " + "Resource Exception occured during execution " + ex.getMessage());
            }
            long end = System.currentTimeMillis();
            total += (end - start);
            //System.out.println( mm.getClass().getName() + " " + step.def.getName() + " Time :" + (end - start));
        }

        System.out.println(Thread.currentThread().getName() +" " + "_________________________________________________________");
        System.out.println(testOutputPrefix + " Total Time for " + tests.size() + " runs : " + total);
        System.out.println(Thread.currentThread().getName() +" " + "_________________________________________________________");
        System.out.println(testOutputPrefix + " AVERAGE Time " + (total/tests.size()));
        System.out.println(Thread.currentThread().getName() +" " + "_________________________________________________________");
        System.out.println(Thread.currentThread().getName() +" " + "****************** END   " + testOutputPrefix + " ******************");
    }
    
    public static void setupTestData(List steps){
        
        TestStep step = new TestStep();
        
        step.stream = loadFile("Logon");
        step.defName = "AWD/netserver/Logon";
        steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("GetSecurity");
        step.defName = "AWD/netserver/GetSecurity";
        steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("Lookup");
        step.defName = "AWD/netserver/Lookup";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetLOBData1");
        step.defName = "AWD/netserver/GetLOBData";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetWorkItem");
        step.defName = "AWD/netserver/GetWorkItem";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("Lock");
        step.defName = "AWD/netserver/Lock";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetWorkItem1");
        step.defName = "AWD/netserver/GetWorkItem";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetWorkItem2");
        step.defName = "AWD/netserver/GetWorkItem";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetLOBData2");
        step.defName = "AWD/netserver/GetLOBData";
        steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("FindSources");
        step.defName = "AWD/netserver/FindSources";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetLOBData3");
        step.defName = "AWD/netserver/GetLOBData";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetHistory");
        step.defName = "AWD/netserver/GetHistory";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetHistory1");
        step.defName = "AWD/netserver/GetHistory";
        steps.add(step);
        
//      step = new TestStep();
//      step.stream = loadFile("TextSource1");
//      MessageDefinition getImagedef = MessageLoader.get("AWD/netserver/GET_IMAGE");
//      step.def = getImagedef;
//      steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("FindChildren");
        step.defName = "AWD/netserver/FindChildren";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("FindParents");
        step.defName = "AWD/netserver/FindParents";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetWorkItem3");
        step.defName = "AWD/netserver/GetWorkItem";
        steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("GetLOBData4");
        step.defName = "AWD/netserver/GetLOBData";
        steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("FindSources1");
        step.defName = "AWD/netserver/FindSources";
        steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("GetLOBData5");
        step.defName = "AWD/netserver/GetLOBData";
        steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("GetHistory2");
        step.defName = "AWD/netserver/GetHistory";
        steps.add(step);

        step = new TestStep();
        step.stream = loadFile("GetHistory3");
        step.defName = "AWD/netserver/GetHistory";
        steps.add(step);
        
//      step = new TestStep();
//      step.stream = loadFile("TextSource2");
//      step.def = getImagedef;
//      steps.add(step);
        
        step = new TestStep();
        step.stream = loadFile("Logoff");
        step.defName = "AWD/netserver/Logoff";
        steps.add(step);
    }

    public static String loadFile(String fileName){
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        BufferedInputStream bs = new BufferedInputStream(is);
        byte[] bytes = new byte[100];
        StringBuffer sb = new StringBuffer();
        try{
            int bytesReceived = 0;
            int i = 0;
            do {
                i = bs.read(bytes);
                sb.append(new String(bytes));
                if (i >= 0) {
                    bytesReceived += i;
                }
            } while (i != -1);
        }catch(Exception ex){
        }
        return sb.toString();
    }
    
    public static class TestStep {
        String name;
        String stream;
        String defName;
    }

    
    /**
     * Load  a properties file and store the values in the Maps. This class is used instead the Properties class
     * because the Properties class stores the values internally in a Hashtable. A Hashtable is not ordered. Because
     * the values for the escape cjharacters may themselves include escape characters, e.g. the escape value for ">"
     * is "&gt;", order is important. The & escape character must be processed first. Therefore, instead of loading
     * the properties into a Hashtable, this class stores the results directly into the Maps provided by the caller, 
     * which are hopefully Maps which preserve order, i.e. a LinkedHashMap.
     */
    //ACEL3050 new class
    private static class OrderedPropertiesLoader {
        /**
         * Load a properties file and store the values in the Maps. 
         * @param propertyFile - the properties file name
         * @param escapeKeysMap - Key/Value Map
         * @param escapeValuesMap - Value/Key Map
         * @throws IOException
         */
        private static void load(String propertyFile, Map escapeKeysMap, Map escapeValuesMap) throws IOException {
            final String keyValueSeparators = "=: \t\r\n\f";
            final String strictKeyValueSeparators = "=:";
            final String whiteSpaceChars = " \t\r\n\f";
            InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
            String line = "";
            while (line != null) {
                // Get next line
                line = in.readLine();
                if (line != null) {
                    if (line.length() > 0) {
                        // Find start of key
                        int len = line.length();
                        int keyStart;
                        for (keyStart = 0; keyStart < len; keyStart++)
                            if (whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                                break;
                        // Blank lines are ignored
                        if (keyStart == len)
                            continue;
                        // Continue lines that end in slashes if they are not comments
                        char firstChar = line.charAt(keyStart);
                        if ((firstChar != '#') && (firstChar != '!')) {
                            while (continueLine(line)) {
                                String nextLine = in.readLine();
                                if (nextLine == null)
                                    nextLine = "";
                                String loppedLine = line.substring(0, len - 1);
                                // Advance beyond whitespace on new line
                                int startIndex;
                                for (startIndex = 0; startIndex < nextLine.length(); startIndex++)
                                    if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)
                                        break;
                                nextLine = nextLine.substring(startIndex, nextLine.length());
                                line = new String(loppedLine + nextLine);
                                len = line.length();
                            }
                            // Find separation between key and value
                            int separatorIndex;
                            for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
                                char currentChar = line.charAt(separatorIndex);
                                if (currentChar == '\\')
                                    separatorIndex++;
                                else if (keyValueSeparators.indexOf(currentChar) != -1)
                                    break;
                            }
                            // Skip over whitespace after key if any
                            int valueIndex;
                            for (valueIndex = separatorIndex; valueIndex < len; valueIndex++)
                                if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                                    break;
                            // Skip over one non whitespace key value separators if any
                            if (valueIndex < len)
                                if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                                    valueIndex++;
                            // Skip over white space after other separators if any
                            while (valueIndex < len) {
                                if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                                    break;
                                valueIndex++;
                            }
                            String key = line.substring(keyStart, separatorIndex);
                            String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";
                            // Convert then store key and value
                            key = loadConvert(key);
                            value = loadConvert(value);
                            escapeKeysMap.put(key, value);
                            escapeValuesMap.put(value, key);
                        }
                    }
                }
            }
            in.close(); //ALII959
        }
        /*
         * Returns true if the given line is a line that must
         * be appended to the next line
         */
        private static  boolean continueLine(String line) {
            int slashCount = 0;
            int index = line.length() - 1;
            while ((index >= 0) && (line.charAt(index--) == '\\'))
                slashCount++;
            return (slashCount % 2 == 1);
        }
        /*
         * Converts encoded &#92;uxxxx to unicode chars
         * and changes special saved chars to their original forms
         */
        private static String loadConvert(String theString) {
            char aChar;
            int len = theString.length();
            StringBuffer outBuffer = new StringBuffer(len);

            for (int x=0; x<len; ) {
                aChar = theString.charAt(x++);
                if (aChar == '\\') {
                    aChar = theString.charAt(x++);
                    if (aChar == 'u') {
                        // Read the xxxx
                        int value=0;
                for (int i=0; i<4; i++) {
                    aChar = theString.charAt(x++);
                    switch (aChar) {
                      case '0': case '1': case '2': case '3': case '4':
                      case '5': case '6': case '7': case '8': case '9':
                         value = (value << 4) + aChar - '0';
                     break;
                  case 'a': case 'b': case 'c':
                              case 'd': case 'e': case 'f':
                     value = (value << 4) + 10 + aChar - 'a';
                     break;
                  case 'A': case 'B': case 'C':
                              case 'D': case 'E': case 'F':
                     value = (value << 4) + 10 + aChar - 'A';
                     break;
                  default:
                                  throw new IllegalArgumentException(
                                               "Malformed \\uxxxx encoding.");
                            }
                        }
                        outBuffer.append((char)value);
                    } else {
                        if (aChar == 't')
                            outBuffer.append('\t');          
                        else if (aChar == 'r')
                            outBuffer.append('\r');        
                        else if (aChar == 'n') {
                            outBuffer.append('\n');         
                        } else if (aChar == 'f')
                            outBuffer.append('\f');         
                        else                               
                            outBuffer.append(aChar);
                    }
                } else
                    outBuffer.append(aChar);
            }
            return outBuffer.toString();
        }

    }
}
