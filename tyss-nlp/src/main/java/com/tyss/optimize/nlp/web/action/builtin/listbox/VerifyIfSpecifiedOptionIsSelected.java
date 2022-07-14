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
@Component(value = "VerifyIfSpecifiedOptionIsSelected")
public class VerifyIfSpecifiedOptionIsSelected implements Nlp {

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        IfFailed ifCheckPointIsFailed = null;
        String modifiedFailMessage = null;
        Boolean containsChildNlp = false;
        Boolean isSelected = false;
        Long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> attributes = nlpRequestModel.getAttributes();
            String elementName = (String) attributes.get("elementName");
            String elementType = (String) attributes.get("elementType");
            String expectedText = (String) attributes.get("expectedText");
            Boolean caseSensitive = (Boolean) attributes.get("caseSensitive");
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

            String modifiedPassMessage = passMessage.replace("*expectedText*", expectedText).replace("*elementName*", modElementName)
                    .replace("*elementType*", modElementType);
            modifiedFailMessage = failMessage.replace("*expectedText*", expectedText).replace("*elementName*", modElementName)
                    .replace("*elementType*", modElementType);

            log.info("Verifying ListBox if option with text " + expectedText + " is selected in " + elementName + " " + elementType);
            Select sel = new Select(element);
            List<WebElement> options = sel.getOptions();
            List<WebElement> selectedOption = sel.getAllSelectedOptions();
            String actualText = "";
            Boolean flag = false;
            Boolean match = false;

            for (WebElement we : options) {
                actualText = we.getText();
                if (actualText.equalsIgnoreCase(expectedText)) {
                    match = true;
                }
            }
            if (match) {
                for (WebElement we : selectedOption) {
                    actualText = we.getText();
                    if (caseSensitive) {
                        if (actualText.equals(expectedText)) {
                            flag = true;
                        }
                    } else {
                        if (actualText.equalsIgnoreCase(expectedText)) {
                            flag = true;
                        }
                    }
                }
                if (flag) {
                    log.info("Option with text " + expectedText + " is selected in " + elementName + " " + elementType);
                    isSelected = true;
                    nlpResponseModel.setMessage(modifiedPassMessage);
                    nlpResponseModel.setStatus(CommonConstants.pass);
                } else {
                    log.error("Option with text " + expectedText + " is not selected in " + elementName + " " + elementType);
                    nlpResponseModel.setMessage(modifiedFailMessage);
                    nlpResponseModel.setStatus(CommonConstants.fail);
                    nlpResponseModel.setIfCheckPointIsFailed(ifCheckPointIsFailed);
                }
            } else {
                log.error("Option with text " + expectedText + " is not present in " + elementName + " " + elementType);
                nlpResponseModel.setMessage("Option with text " + expectedText + " is not present in " + elementName + " " + elementType);
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setIfCheckPointIsFailed(ifCheckPointIsFailed);
            }
        } catch (Exception exception) {
            log.error("NLP_EXCEPTION in VerifyIfSpecifiedOptionIsSelected ", exception);
            String exceptionSimpleName = exception.getClass().getSimpleName();
            nlpResponseModel = ExceptionHandlingInfo.exceptionMessageHandler(modifiedFailMessage, ifCheckPointIsFailed, exceptionSimpleName, exception.getStackTrace());
            if (containsChildNlp) {
                throw new NlpException(exceptionSimpleName);
            }
        }
        nlpResponseModel.getAttributes().put("isSelected", isSelected);
        Long endTime = System.currentTimeMillis();
        nlpResponseModel.setExecutionTime((endTime - startTime));
        return nlpResponseModel;
    }

    public StringBuilder getTestCode() throws NlpException {
        StringBuilder sb = new StringBuilder();

        sb.append("int seconds = 1, count = 0;\n");
        sb.append("long sec = Integer.toUnsignedLong(1000 * seconds);\n");
        sb.append("boolean match = false, retry = true;\n");
        sb.append("boolean isSensitive = Boolean.valueOf(caseSensitive);\n");
        sb.append("WebElement element = driver.findElement(ELEMENT);\n");
        sb.append("Select select = new Select(element);\n");
        sb.append("while (retry) {\n");
        sb.append("	count++;\n");
        sb.append("	List<WebElement> allSelectOptions = select.getAllSelectedOptions();\n");
        sb.append("	List<String> textOfAllSelectedOptions = new ArrayList<>();\n");
        sb.append("	for (WebElement options : allSelectOptions) {\n");
        sb.append("		textOfAllSelectedOptions.add(options.getText());\n");
        sb.append("	}\n");
        sb.append("	int index = -1;\n");
        sb.append("	boolean isMatch = false;\n");
        sb.append("	for (int i = 0; i < textOfAllSelectedOptions.size(); i++) {\n");
        sb.append("		String option = textOfAllSelectedOptions.get(i);\n");
        sb.append("		if (isSensitive) {\n");
        sb.append("			isMatch = option.equals(expectedText);\n");
        sb.append("		} else {\n");
        sb.append("			isMatch = option.equalsIgnoreCase(expectedText);\n");
        sb.append("		}\n");
        sb.append("		if (isMatch) {\n");
        sb.append("			index = i;\n");
        sb.append("			break;\n");
        sb.append("		}\n");
        sb.append("	}\n");
        sb.append("	if (index >= 0) {\n");
        sb.append("		match = true;\n");
        sb.append("		break;\n");
        sb.append("	} else {\n");
        sb.append("		if (count < timeoutInSeconds) {\n");
        sb.append("			try{\n");
        sb.append("				Thread.sleep(sec);\n");
        sb.append("			}catch(InterruptedException ie){}\n");
        sb.append("		} else {\n");
        sb.append("			break;\n");
        sb.append("		}\n");
        sb.append("	}\n");
        sb.append("}\n");
        sb.append("if (match) {\n");
        sb.append("	System.out.println(\"Option \" + expectedText + \" with text \" + expectedText + \" is selected\");\n");
        sb.append("} else {\n");
        sb.append("	System.out.println(\"Option \" + expectedText + \" with text \" + expectedText + \" is not selected\");\n");
        sb.append("}\n");

        return sb;
    }

    public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();

        params.add("ELEMENT::By.id(\"cars\")");
        params.add("expectedText::\"xyz\"");
        params.add("timeoutInSeconds::200");
        params.add("caseSensitive::\"true\"");

        return params;
    }
}
