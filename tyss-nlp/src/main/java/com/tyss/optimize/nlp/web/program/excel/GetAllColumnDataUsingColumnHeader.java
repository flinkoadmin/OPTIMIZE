package com.tyss.optimize.nlp.web.program.excel;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.data.models.dto.StorageInfo;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.util.storage.StorageConfigFactoryBuilder;
import com.tyss.optimize.nlp.util.storage.StorageManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component(value =  "GetAllColumnDataUsingColumnHeader")
public class GetAllColumnDataUsingColumnHeader implements Nlp {
    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Long startTime=System.currentTimeMillis();
        NlpResponseModel nlpResponseModel =  new NlpResponseModel();
        String passMessage, failMessage = null;
        IfFailed ifCheckPointIsFailed = null;
        Boolean containsChildNlp = false;
        String[] columnData = null;
        String columnHeader = null;
        try {
            Map<String, Object> attributes = nlpRequestModel.getAttributes();
            String filePath=(String) attributes.get("filePath");
            String sheetName=(String) attributes.get("sheetName");
            columnHeader=(String) attributes.get("columnHeader");
            passMessage=(String) nlpRequestModel.getPassMessage();
            failMessage=(String) nlpRequestModel.getFailMessage();
            containsChildNlp = (Boolean) attributes.get("containsChildNlp");
            StorageInfo storageInfo = nlpRequestModel.getStorageInfo();
            if(attributes.get("ifCheckPointIsFailed")!=null)
            {
                String ifFailed = attributes.get("ifCheckPointIsFailed").toString();
                ifCheckPointIsFailed = IfFailed.valueOf(ifFailed);
            }
            StorageManager storageManager = StorageConfigFactoryBuilder.getStorageManager(storageInfo.getType());
            InputStream ip =  storageManager.getObject(storageInfo, filePath);

            Workbook wb = WorkbookFactory.create(ip);
            Sheet sh = wb.getSheet(sheetName);
            Row row = sh.getRow(0);
            int firstRowCellCount = row.getPhysicalNumberOfCells();
            int rowCount = sh.getPhysicalNumberOfRows();
            String columnValue = "";
            columnData = new String[rowCount-1] ;
            for(int i=0; i<firstRowCellCount; i++)
            {
                Cell columnCell = sh.getRow(0).getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                columnValue = columnCell.toString();
                if(columnValue.equals(columnHeader))
                {
                    int columnIndex = i;
                    for(int j=0; j<rowCount-1; j++)
                    {
                        Cell cellData = sh.getRow(j+1).getCell(columnIndex, MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        columnData[j] = cellData.toString();
                    }
                    break;
                }
            }
            log.info("Column data of column "+columnHeader+" is "+columnData);
            nlpResponseModel.setMessage(passMessage.replace("*columnHeader*",columnHeader).replace("*returnValue*",columnData.toString()));
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.getAttributes().put("columnData",columnData);
        }
        catch(Exception exception)
        {
            log.error("NLP_EXCEPTION in GetAllColumnDataUsingColumnHeader ", exception);
            failMessage = failMessage.replace("*columnHeader*",columnHeader);
            String exceptionSimpleName = exception.getClass().getSimpleName();
            if (containsChildNlp) {
                throw new NlpException(exceptionSimpleName);
            }
            nlpResponseModel = ExceptionHandlingInfo.exceptionMessageHandler(failMessage, ifCheckPointIsFailed, exceptionSimpleName, exception.getStackTrace());
        }
        Long endTime=System.currentTimeMillis();
        nlpResponseModel.setExecutionTime(endTime-startTime);
        return nlpResponseModel;
    }

    public StringBuilder getTestCode() throws NlpException {
        StringBuilder sb = new StringBuilder();

        sb.append("try {\n");
        sb.append("	String[] columnData = null;\n");
        sb.append("	FileInputStream ip = new FileInputStream(filePath);\n");
        sb.append("	Workbook wb = WorkbookFactory.create(ip);\n");
        sb.append("	Sheet sh = wb.getSheet(sheetName);\n");
        sb.append("	Row row = sh.getRow(0);\n");
        sb.append("	int firstRowCellCount = row.getPhysicalNumberOfCells();\n");
        sb.append("	int rowCount = sh.getPhysicalNumberOfRows();\n");
        sb.append("	String columnValue = \"\";\n");
        sb.append("	columnData = new String[rowCount-1] ;\n");
        sb.append("	for(int i=0; i<firstRowCellCount; i++) {\n");
        sb.append("		Cell columnCell = sh.getRow(0).getCell(i, MissingCellPolicy.CREATE_NULL_AS_BLANK);\n");
        sb.append("		columnValue = columnCell.toString();\n");
        sb.append("		if(columnValue.equals(columnHeader)) {\n");
        sb.append("			int columnIndex = i;\n");
        sb.append("			for(int j=0; j<rowCount-1; j++) {\n");
        sb.append("				Cell cellData = sh.getRow(j+1).getCell(columnIndex, MissingCellPolicy.CREATE_NULL_AS_BLANK);\n");
        sb.append("				columnData[j] = cellData.toString();\n");
        sb.append("			}\n");
        sb.append("			break;\n");
        sb.append("		}\n");
        sb.append("	}\n");
        sb.append("} catch (Exception exception) {}\n");

        return sb;
    }

    public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();

        params.add("filePath::\"/home/tyss/File1.xlsx\"");
        params.add("sheetName::\"xyz\"");
        params.add("columnHeader::\"abc\"");

        return params;
    }
}
