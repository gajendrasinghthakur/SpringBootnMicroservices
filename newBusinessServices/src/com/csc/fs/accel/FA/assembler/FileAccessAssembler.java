/*************************************************************************
 *
 * Copyright Notice (2006)
 * (c) CSC Financial Services Limited 1996-2006.
 * All rights reserved. The software and associated documentation
 * supplied hereunder are the confidential and proprietary information
 * of CSC Financial Services Limited, Austin, Texas, USA and
 * are supplied subject to licence terms. In no event may the Licensee
 * reverse engineer, decompile, or otherwise attempt to discover the
 * underlying source code or confidential information herein.
 *
 *************************************************************************/
package com.csc.fs.accel.FA.assembler;

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.FileAccess;
import com.csc.fs.accel.valueobject.FileItem;
import com.csc.fs.accel.valueobject.FileMessage;
import com.csc.fs.accel.valueobject.FolderAccessItem;
import com.csc.fs.dataobject.accel.FA.FileAccessDO;
import com.csc.fs.dataobject.accel.FA.FileItemDO;
import com.csc.fs.dataobject.accel.FA.FileMessageDO;
import com.csc.fs.dataobject.accel.FA.FolderAccessItemDO;

/**
 * FileAccessAssembler is the dis-assembler/assembler for the File Access Business Process.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA188</td><td>Version 7</td><td>XML sources to NBAAUXILIARY</td></tr>
 * </table>
 * <p>
 */
public class FileAccessAssembler extends AccelTransformation {
    protected final static String ZEROES = "00000000000000000000000000000000";
    /**
     * Create a FileAccess value object from the List of the FileAccessDO, FolderAccessItemDO, FileMessageDO, and FileItemDO data objects contained in
     * the result. Return the a FileAccess value object in an AccelResult. object.
     * @param result - containing a List of data objects
     * @return an AccelResult containing the FileAccess value object
     * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)
     */ 
    public Result assemble(Result result) {
        List data = result.getReturnData();
        Object dataObj;
        List folderAccessItems = new ArrayList();
        List fileItems = new ArrayList();
        FileAccessDO fileAccessDO;
        FileAccess fileAccessVO = new FileAccess();
        FolderAccessItem folderAccessItemVO;
        FolderAccessItemDO folderAccessItemDO;
        FileItem fileItemVO;
        FileItemDO fileItemDO;
        FileMessageDO fileMessageDO;
        FileMessage fileMessageVO;
        for (int i = 0; i < data.size(); i++) {
            dataObj = data.get(i);
            if (dataObj instanceof FileAccessDO) {
                fileAccessDO = (FileAccessDO) dataObj;
                fileAccessVO.setRequestCode(fileAccessDO.getRequestCode());
                 
            } else if (dataObj instanceof FolderAccessItemDO) {
                folderAccessItems.add(dataObj);
            } else if (dataObj instanceof FileItemDO) {
                fileItems.add(dataObj);
            } else if (dataObj instanceof FileMessageDO) {
                fileMessageDO = (FileMessageDO) dataObj;
                fileMessageVO = new FileMessage();
                fileMessageVO.setSeverity(fileMessageDO.getSeverity());
                fileMessageVO.setMsg(fileMessageDO.getMsg());
                fileAccessVO.getMessages().add(fileMessageVO);
            }
        }
        for (int i = 0; i < folderAccessItems.size(); i++) {
            folderAccessItemDO = (FolderAccessItemDO) folderAccessItems.get(i);
            folderAccessItemVO = new FolderAccessItem();
            folderAccessItemVO.setFoldername(folderAccessItemDO.getFoldername());
            fileAccessVO.getFolderAccessItems().add(folderAccessItemVO);
            for (int j = 0; j < fileItems.size(); j++) {
                fileItemDO = (FileItemDO) fileItems.get(j);
                if (folderAccessItemDO.getOid().equals(fileItemDO.getParentOid())) {
                    fileItemVO = new FileItem();
                    fileItemVO.setFilename(fileItemDO.getFilename());
                    fileItemVO.setFileContents(fileItemDO.getFileContents());
                    folderAccessItemVO.getFileItems().add(fileItemVO); 
                }
            }
        }
        return new AccelResult().addResult(fileAccessVO);
    }
    /**
     * Disassemble the FileAccess value object into data objects.
     * @param input - the FileAccess value object
     * @return a ResultImpl containing a List of the FileAccessDO, FolderAccessItemDO, and FileItemDO data objects
     * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
     */
    public Result disassemble(Object input) {
        FileAccess documentAccessVO = (FileAccess) input;
        List inputData = new ArrayList();
        FileAccessDO documentAccessDO = new FileAccessDO();
        documentAccessDO.setRequestCode(documentAccessVO.getRequestCode());
 
        inputData.add(documentAccessDO);
        FolderAccessItem folderAccessItem;
        FolderAccessItemDO folderAccessItemDO;
        FileItem fileItem;
        FileItemDO fileItemDO;
        int faCnt = documentAccessVO.getFolderAccessItems().size();
        int fiCnt;
        for (int i = 0; i < faCnt; i++) {
            folderAccessItem = (FolderAccessItem) documentAccessVO.getFolderAccessItems().get(i);
            folderAccessItemDO = new FolderAccessItemDO();
            folderAccessItemDO.setIdentifier(get32Chars(i));
            folderAccessItemDO.setFoldername(folderAccessItem.getFoldername());
            inputData.add(folderAccessItemDO);
            fiCnt = folderAccessItem.getFileItems().size();
            for (int j = 0; j < fiCnt; j++) {
                fileItem = (FileItem) folderAccessItem.getFileItems().get(j);
                fileItemDO = new FileItemDO();
                fileItemDO.setParentIdentifier(folderAccessItemDO);
                fileItemDO.setFilename(fileItem.getFilename());
                fileItemDO.setFileContents(fileItem.getFileContents());
                inputData.add(fileItemDO);
            }
        }
        return Result.Factory.create().addResults(inputData);
    }

    /**
     * Construct a 32 character String representation of an int value.
     * @param value
     * @return
     */
    protected String get32Chars(int value) {
        String temp = ZEROES + String.valueOf(value);
        return temp.substring(temp.length() - 32);
    }
 
    
    
    
    
    
    
    
    
    
    
}