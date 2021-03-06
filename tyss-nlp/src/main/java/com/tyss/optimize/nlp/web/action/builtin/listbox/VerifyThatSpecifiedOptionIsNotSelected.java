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
@Component(value = "VerifyThatSpecifiedOptionIsNotSelected")
public class VerifyThatSpecifiedOptionIsNotSelected implements Nlp {

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        IfFailed ifCheckPointIsFailed = null;
        String modifiedFailMessage = null;
        Boolean containsChildNlp = false;
        Boolean isNotSelected = false;
        Long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> attributes = nlpRequestModel.getAttributes();
            String elementName = (String) attributes.get("elementName");
            String elementType = (String) attributes.get("elementType");
            String expectedValue = (String) attributes.get("expectedValue");
            Boolean caseSensitive = (Boolean) attributes.get("caseSensitive");
            WebElement element = (WebElement) attributes.get("element");
            String[] elementSplit = elementName.split(":");
            String modElementName = elementSplit[1];
            String modElementType = elementType.concat(" in " + elementSplit[0] + " page");
            String passMessage = nlpRequestModel.getPassMessage();
            String failMessage = nlpRequestModel.getFailMessage();
            if (attributes.get("ifCheckPointIsFailed") != null) {
                String ifFailed = attributes.get("ifCheckPointIsFailed").toString();
                ifCheckPointIsFailed = IfFailed.valueOf(ifFailed);
            }
            String modifiedPassMessage = passMessage.replace("*expectedValue*", expectedValue).replace("*elementName*", modElementName)
                    .replace("*elementType*", modElementType);
            modifiedFailMessage = failMessage.replace("*expectedValue*", expectedValue).replace("*elementName*", modElementName)
                    .replace("*elementType*", modElementType);
            containsChildNlp = (Boolean) attributes.get("containsChildNlp");
            Select sel = new Select(element);
            List<WebElement> selectedOption = sel.getAllSelectedOptions();
            String actualValue = "";
            Boolean flag = false;
            Boolean match = false;
            List<WebElement> options = sel.getOptions();
            for (WebElement we : options) {
                actualValue = we.getAttribute("value");
                if (actualValue.equalsIgnoreCase(expectedValue)) {
                    match = true;
                }
            }

            if (match) {
                options.removeAll(selectedOption);
                for (WebElement we : options) {
                    actualValue = we.getAttribute("value");
                    if (caseSensitive) {
                        if (actualValue.equals(expectedValue)) {
                            flag = true;
                        }
                    } else {
                        if (actualValue.equalsIgnoreCase(expectedValue)) {
                            flag = true;
                        }
                    }
                }
                if (flag) {
                    log.info("Option with value " + expectedValue + " is deselected in " + elementName + " " + elementType);
                    isNotSelected = true;
                    nlpResponseModel.setMessage(modifiedPassMessage);
                    nlpResponseModel.setStatus(CommonConstants.pass);
                } else {
                    log.error("Option with value " + expectedValue + " is selected in " + elementName + " " + elementType);
                    nlpResponseModel.setMessage(modifiedFailMessage);
                    nlpResponseModel.setStatus(CommonConstants.fail);
                    nlpResponseModel.setIfCheckPointIsFailed(ifCheckPointIsFailed);
                }
            } else {
                log.error("Option with value " + expectedValue + " is not present in " + elementName + " " + elementType);
                nlpResponseModel.setMessage("Option with value " + expectedValue + " is not present in " + elementName + " " + elementType);
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setIfCheckPointIsFailed(ifCheckPointIsFailed);
            }
        } catch (Exception exception) {
            log.error("NLP_EXCEPTION in VerifyThatSpecifiedOptionIsNotSelected ", exception);
            String exceptionSimpleName = exception.getClass().getSimpleName();
            nlpResponseModel= ExceptionHandlingInfo.exceptionMessageHandler(modifiedFailMessage, ifCheckPointIsFailed, exceptionSimpleName, exception.getStackTrace());
            if(containsChildNlp) {
                throw new NlpException(exceptionSimpleName);
            }
        }
        nlpResponseModel.getAttributes().put("isNotSelected", isNotSelected);
        Long endTime = System.currentTimeMillis();
        nlpResponseModel.setExecutionTime((endTime - startTime));
        return nlpResponseModel;
    }

    private int getIndexOfOption(List<String> textOfAllSelectedOptions, String expectedText,
                                 Boolean caseSensitive) {
        int index = -1;
        boolean match;
        log.info("Expected option to be Selected:" + expectedText);
        for (int i = 0; i < textOfAllSelectedOptions.size(); i++) {
            String option = textOfAllSelectedOptions.get(i);
            log.info("Actual Selected options:" + option);
            if (caseSensitive) {
                match = option.equals(expectedText);
                log.info("Case Sensitive comparison, result is:" + match);
            } else {
                match = option.equalsIgnoreCase(expectedText);
                log.info("Case Insensitive comparison, result is:" + match);
            }

            if (match) {
                index = i;
                break;
            }
        }
        return index;
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
        sb.append("			isMatch = option.equals(expectedValue);\n");
        sb.append("		} else {\n");
        sb.append("			isMatch = option.equalsIgnoreCase(expectedValue);\n");
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
        sb.append("	System.out.println(\"Option \" + expectedValue + \" with text \" + expectedValue + \" is not deselected\");\n");
        sb.append("} else {\n");
        sb.append("	System.out.println(\"Option \" + expectedValue + \" with text \" + expectedValue + \" is deselected\");\n");
        sb.append("}\n");

        return sb;
    }

    public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();

        params.add("ELEMENT::By.id(\"cars\")");
        params.add("expectedValue::\"xyz\"");
        params.add("timeoutInSeconds::200");
        params.add("caseSensitive::\"true\"");

        return params;
    }
}
