package com.csc.fs.ra.message; 

import java.util.Iterator;
import java.util.List;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.ra.SimpleMappedRecord;
import com.csc.fs.ra.XMLtoMappedRecordParser;

/**
 * Message manager to support transforming an existing XML document for an Accelerator
 * service.  It should be used in conjunction with the <code>accelRAtransform</code>
 * resource adapter.  The XML document is pulled from the interaction map in whole
 * and returned for processing as a response.
 * 
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

public class MessageManagerTransformRsp extends MessageManagerTransformReq  {
	   /**
     * @param responsemessage        Description of Parameter
     * @param messageDef             Description of Parameter
     * @param resultRecord           Description of Parameter
     * @exception ResourceException  Description of Exception
     */
    public void constructResponseRecord(Object request, Object responsemessage, MessageDefinition messageDef, Record resultRecord) throws ResourceException {
        long start = System.currentTimeMillis();
        if (resultRecord != null && messageDef != null && responsemessage != null && !responsemessage.equals("") && messageDef.getMessageType().equals("XML")) {
            List responseBlocks = messageDef.getResponseBlocks();
            if (responseBlocks != null && !responseBlocks.isEmpty()) {
                String processResponse = (String)responsemessage;
                Iterator responseBlockIter = responseBlocks.iterator();
                
                if(processResponse != null && !processResponse.equals("")) {
                    while (responseBlockIter.hasNext()) {
                        MessageDefinition.Block currentBlock = (MessageDefinition.Block) responseBlockIter.next();
                        processMessageBlock(messageDef, currentBlock, resultRecord, processResponse, "");
                    }
                }
            } else {
                XMLtoMappedRecordParser.populateMappedRecordFromXMLString((String)responsemessage, (SimpleMappedRecord) resultRecord);
            }
        }
        long end = System.currentTimeMillis();
        LogHandler.Factory.LogTxnPerf("XML Message Manager Transform Response", "constructResponseRecord performance: {0} ms",
                new Object[]{new Long(end - start)});
    }

    /**
     * Transforms the given message block using the current message definition
     * into the result record provided.
     * <p/>
     * The remaining part of the message is passed back to the caller for
     * further processing.
     *
     * @param messageDef    Current message definition
     * @param currentBlock  Block definition to use
     * @param resultRecord  Record to populate
     * @param input         Message to transform
     * @return              Remaining part of the response message not processed in this block
     */
    protected void processMessageBlock(MessageDefinition messageDef,
                                        MessageDefinition.Block currentBlock,
                                        Record resultRecord,
                                        String input,
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
                LogHandler.Factory.LogError("XML Message Manager Transform Response", "Unable To Load External Block [{0}]", new Object[]{currentBlock.getExternalName()});
                throw new ResourceException("Unable To Load External Block [" + currentBlock.getExternalName() + "] in Message Definition [" + messageDef.getName() + "]");
            }
        }
        
        // Create a block-level record.  It will contain the detail items.
        Record highLevelRecord = new SimpleMappedRecord();
        
        Iterator blockChildIter = currentBlock.getChildren().iterator();
        while (blockChildIter.hasNext()) {
            Object obj = blockChildIter.next();
            if (obj instanceof MessageDefinition.Block) {
                // make recursive call to this method
                processMessageBlock (messageDef, (MessageDefinition.Block)obj, highLevelRecord, input, currentBlock.getXmlPath());
            } else if (obj instanceof MessageDefinition.Item) {
                MessageDefinition.Item currentItem = (MessageDefinition.Item) obj;
                ((MappedRecord) highLevelRecord).put(currentItem.getName(), input);
            }
        }
        highLevelRecord.setRecordName(currentBlock.getAliasName());
        ((MappedRecord) resultRecord).put(currentBlock.getAliasName(), highLevelRecord);
        long end = System.currentTimeMillis();
        LogHandler.Factory.LogTxnPerf("XML Message Manager Transform Response", "processMessageBlock[{0}] performance: {1} ms",
                                        new Object[]{currentBlock.getExternalName(), new Long(end - start)});
    }

}
