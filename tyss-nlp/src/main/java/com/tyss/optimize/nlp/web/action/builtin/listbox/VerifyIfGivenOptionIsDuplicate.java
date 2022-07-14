package com.tyss.optimize.nlp.web.action.builtin.listbox;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component(value = "VerifyIfGivenOptionIsDuplicate")
public class VerifyIfGivenOptionIsDuplicate implements Nlp {

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        IfFailed ifCheckPointIsFailed = null;
        String modifiedFailMessage = null;
        Boolean containsChildNlp = false;
        Boolean isDuplicate = false;
        Long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> attributes = nlpRequestModel.getAttributes();
            String elementName = (String) attributes.get("elementName");
            String elementType = (String) attributes.get("elementType");
            String expectedOption = (String) attributes.get("expectedOption");
            containsChildNlp = (Boolean) attributes.get("containsChildNlp");
            WebElement element = (WebElement) attributes.get("element");
            String[] elementSplit = elementName.split(":");
            String modElementName = elementSplit[1];
            String modElementType = elementType.concat(" in " + elementSplit[0] + " page ");
            String passMessage = nlpRequestModel.getPassMessage();
            String failMessage = nlpRequestModel.getFailMessage();
            if (attributes.get("ifCheckPointIsFailed") != null) {
                String ifFailed = attributes.get("ifCheckPointIsFailed").toString();
                ifCheckPointIsFailed = IfFailed.valueOf(ifFailed);
            }
            NlpRequestModel requestOptions = new NlpRequestModel();
            requestOptions.getAttributes().put("containsChildNlp", true);
            requestOptions.getAttributes().put("elementName", elementName);
            requestOptions.getAttributes().put("elementType", elementType);
            requestOptions.getAttributes().put("element", element);
            requestOptions.getAttributes().put("ifCheckPointIsFailed", ifCheckPointIsFailed);
            requestOptions.setPassMessage("Captured all options from *elementName* *elementType*");
            requestOptions.setFailMessage("Failed to get all options from *elementName* *elementType*");

            log.info("Getting all the options present in the " + elementName + " " + elementType);
            String modifiedPassMessage = passMessage.replace("*expectedOption*", expectedOption).replace("*elementName*", modElementName)
                    .replace("*elementType*", modElementType);
            modifiedFailMessage = failMessage.replace("*expectedOption*", expectedOption).replace("*elementName*", modElementName)
                    .replace("*elementType*", modElementType);

            Select sel = new Select(element);
            int count = 0;
            List<WebElement> list = sel.getOptions();
            log.info("Verifying given option is duplicate in listbox " + elementName + " " + elementType);
            String actualValue = "";
            Boolean match = false;
            List<WebElement> options = sel.getOptions();
            for (WebElement we : options) {
                actualValue = we.getAttribute("value");
                if (actualValue.equals(expectedOption)) {
                    match = true;
                }
            }
            if (match) {
                for (WebElement wb : list) {
                    if (wb.getText().equals(expectedOption)) {
                        count++;
                    }
                }
                if (count > 1) {
                    log.info(expectedOption + " option is duplicate in" + elementName + " " + elementType);
                    isDuplicate = true;
                    nlpResponseModel.setMessage(modifiedPassMessage);
                    nlpResponseModel.setStatus(CommonConstants.pass);
                } else {
                    log.error(expectedOption + " option is not duplicate in" + elementName + " " + elementType);
                    nlpResponseModel.setMessage(modifiedFailMessage);
                    nlpResponseModel.setStatus(CommonConstants.fail);
                    nlpResponseModel.setIfCheckPointIsFailed(ifCheckPointIsFailed);
                }
            } else {
                log.error("Option with value " + expectedOption + " is not present in " + elementName + " " + elementType);
                nlpResponseModel.setMessage("Option with value " + expectedOption + " is not present in " + elementName + " " + elementType);
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setIfCheckPointIsFailed(ifCheckPointIsFailed);
            }
        }
        catch (Exception exception) {
            log.error("NLP_EXCEPTION in VerifyIfGivenOptionIsDuplicate ", exception);
            String exceptionSimpleName = exception.getClass().getSimpleName();
            nlpResponseModel = ExceptionHandlingInfo.exceptionMessageHandler(modifiedFailMessage, ifCheckPointIsFailed, exceptionSimpleName, exception.getStackTrace());
            if (containsChildNlp) {
                throw new NlpException(exceptionSimpleName);
            }
        }
        nlpResponseModel.getAttributes().put("isDuplicate", isDuplicate);
        Long endTime = System.currentTimeMillis();
        nlpResponseModel.setExecutionTime(endTime - startTime);
        return nlpResponseModel;
    }

    public StringBuilder getTestCode() throws NlpException {
        StringBuilder sb = new StringBuilder();

        sb.append("WebElement element = driver.findElement(ELEMENT);\n");
        sb.append("List<WebElement> elementList = new ArrayList<>();\n");
        sb.append("Select select = new Select(element);\n");
        sb.append("elementList = select.getOptions();\n");
        sb.append("int count = 0;\n");
        sb.append("for (WebElement wb : elementList) {\n");
        sb.append("	if (wb.getText().equals(expectedOption)) {\n");
        sb.append("		count++;\n");
        sb.append("	}\n");
        sb.append("}\n");
        sb.append("if (count > 1) {\n");
        sb.append("	System.out.println(expectedOption + \" option is duplicate\");\n");
        sb.append("} else {\n");
        sb.append("	System.out.println(expectedOption + \" option is not duplicate\");\n");
        sb.append("}\n");

        return sb;
    }

    public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();

        params.add("ELEMENT::By.id(\"cars\")");
        params.add("expectedOption::\"xyz\"");

        return params;
    }
}
