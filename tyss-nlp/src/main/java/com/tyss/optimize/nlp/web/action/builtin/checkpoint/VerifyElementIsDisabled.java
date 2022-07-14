package com.tyss.optimize.nlp.web.action.builtin.checkpoint;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.*;
import com.tyss.optimize.nlp.web.action.natives.IsEnabled;
import com.tyss.optimize.nlp.web.program.browser.Sleep;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component(value = "VerifyElementIsDisabled")
public class VerifyElementIsDisabled implements Nlp {

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        IfFailed ifCheckPointIsFailed = null;
        String modifiedFailMessage = null;
        NlpResponseModel nlpResponseModel = new NlpResponseModel();
        Boolean containsChildNlp = false;
        Long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> attributes = nlpRequestModel.getAttributes();
            String elementName = (String) attributes.get("elementName");
            String elementType = (String) attributes.get("elementType");
            WebElement element = (WebElement) attributes.get("element");
            containsChildNlp = (Boolean) attributes.get("containsChildNlp");
            Long explicitTimeOut = (Long) attributes.get("explicitTimeOut");
            String [] elementSplit=elementName.split(":");
            String modElementName=elementSplit[1];
            String modElementType=elementType.concat(" in "+elementSplit[0] + " page ");
            String passMessage = (String) nlpRequestModel.getPassMessage();
            String failMessage = (String) nlpRequestModel.getFailMessage();
            if (attributes.get("ifCheckPointIsFailed") != null) {
                String ifFailed = attributes.get("ifCheckPointIsFailed").toString();
                ifCheckPointIsFailed = IfFailed.valueOf(ifFailed);
            }
            NlpRequestModel requestEnabled = new NlpRequestModel();
            requestEnabled.getAttributes().put("containsChildNlp", true);
            requestEnabled.getAttributes().put("ifCheckPointIsFailed", ifCheckPointIsFailed);
            requestEnabled.getAttributes().put("elementName", elementName);
            requestEnabled.getAttributes().put("elementType", elementType);
            requestEnabled.getAttributes().put("element", element);
            requestEnabled.setPassMessage("*elementName* *elementType* is enabled");
            requestEnabled.setFailMessage("*elementName* *elementType* is disabled");

            NlpRequestModel requestSleep = new NlpRequestModel();
            requestSleep.getAttributes().put("ifCheckPointIsFailed", ifCheckPointIsFailed);
            requestSleep.getAttributes().put("seconds", 1);
            requestSleep.getAttributes().put("containsChildNlp", true);
            requestSleep.setPassMessage("Waited for *seconds* seconds");
            requestSleep.setFailMessage("Failed to wait for *seconds* seconds");

            modifiedFailMessage = failMessage.replace("*elementName*", modElementName).replace("*elementType*", modElementType).replace("*seconds*", explicitTimeOut.toString());
            String modifiedPassMessage = passMessage.replace("*elementName*", modElementName).replace("*elementType*", modElementType).replace("*seconds*", explicitTimeOut.toString());
            log.info("Verifying if element " + elementName + " " + elementType + " is disabled");
            String details = "";
            Boolean match = false, retry = true;
            int count = 0;
            while (retry) {
                count++;
                Boolean actualEnabledStatus = (Boolean) ((NlpResponseModel) new IsEnabled().execute(requestEnabled)).getAttributes().get("enable");
                details = "Expected value:False \n Actual Value:" + actualEnabledStatus;
                log.info(details);
                match = actualEnabledStatus.equals(true);
                if (match) {
                    log.info("Exit from loop because of match");
                    break;
                } else {
                    if (count < explicitTimeOut) {
                        log.info("Waiting for match \n Iteration:" + count);
                        new Sleep().execute(requestSleep);
                    } else {
                        log.info("Exit from loop because of timeout");
                        break;
                    }
                }
            }
            if (!match) {
                log.info(elementName + " " + elementType + " is disabled");
                nlpResponseModel.setMessage(modifiedPassMessage);
                nlpResponseModel.setStatus(CommonConstants.pass);
            } else {
                log.error(elementName + " " + elementType + " is not disabled");
                nlpResponseModel.setMessage(modifiedFailMessage);
                nlpResponseModel.setStatus(CommonConstants.fail);
                nlpResponseModel.setIfCheckPointIsFailed(ifCheckPointIsFailed);
            }
        } catch (NlpException exception) {
            log.error("NLP_EXCEPTION in VerifyAttributeValue ", exception);
            String exceptionSimpleName = exception.getMessage();
            nlpResponseModel= ExceptionHandlingInfo.exceptionMessageHandler(modifiedFailMessage, ifCheckPointIsFailed, exceptionSimpleName, exception.getStackTrace());
        }
        catch (Exception exception) {
            log.error("NLP_EXCEPTION in VerifyElementIsDisabled ", exception);
            String exceptionSimpleName = exception.getClass().getSimpleName();
            nlpResponseModel= ExceptionHandlingInfo.exceptionMessageHandler(modifiedFailMessage, ifCheckPointIsFailed, exceptionSimpleName, exception.getStackTrace());
            if(containsChildNlp) {
                throw new NlpException(exceptionSimpleName);
            }
        }
        Long endTime = System.currentTimeMillis();
        nlpResponseModel.setExecutionTime(endTime - startTime);
        return nlpResponseModel;
    }

    public StringBuilder getTestCode() throws NlpException {
        StringBuilder sb = new StringBuilder();

        sb.append("boolean match = false, retry = true;\n");
        sb.append("int count = 0, seconds = 1;\n");
        sb.append("long sec = Integer.toUnsignedLong(1000 * seconds);\n");
        sb.append("WebElement element = driver.findElement(ELEMENT);\n");
        sb.append("while (retry) {\n");
        sb.append("	count++;\n");
        sb.append("	Boolean actualEnabledStatus = element.isEnabled();\n");
        sb.append("	match = actualEnabledStatus.equals(true);\n");
        sb.append("	if (match) {\n");
        sb.append("		break;\n");
        sb.append("	} else {\n");
        sb.append("		if (count < explicitTimeOut) {\n");
        sb.append("			try {\n");
        sb.append("				Thread.sleep(sec);\n");
        sb.append("			} catch (InterruptedException ie) {}\n");
        sb.append("		} else {\n");
        sb.append("			break;\n");
        sb.append("		}\n");
        sb.append("	}\n");
        sb.append("}\n");
        sb.append("if (!match) {\n");
        sb.append("	System.out.println(\"The Element is disabled\");\n");
        sb.append("} else {\n");
        sb.append("	System.out.println(\"The Element is not disabled\");\n");
        sb.append("}\n");

        return sb;
    }

    public List<String> getTestParameters() throws NlpException {
        List<String> params = new ArrayList<>();

        params.add("ELEMENT::By.id(\"randomId\")");
        params.add("explicitTimeOut::20");

        return params;
    }
}
