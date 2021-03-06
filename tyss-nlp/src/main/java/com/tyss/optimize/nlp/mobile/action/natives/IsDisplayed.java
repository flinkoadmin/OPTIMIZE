package com.tyss.optimize.nlp.mobile.action.natives;

import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component(value =  "MOB_IsDisplayed")
public class IsDisplayed implements Nlp {

	@Override
	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		Long startTime = System.currentTimeMillis();
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		String passMessage = null, failMessage = null, modifiedPassMessage = null, modifiedFailMessage = null;
		IfFailed ifCheckPointIsFailed = null;
		Boolean containsChildNlp = false;
		boolean displayed = false;
		try {
			Map<String, Object> attributes = nlpRequestModel.getAttributes();
			String elementName = (String) attributes.get("elementName");
			WebElement element = (WebElement) attributes.get("element");
			String elementType = (String) attributes.get("elementType");
			String elementScreen = (String) attributes.get("elementScreen");
			containsChildNlp = (Boolean) attributes.get("containsChildNlp");
			String [] elementSplit=elementName.split(":");
			elementName=elementSplit[1];
			elementType=elementType.concat(" in "+elementSplit[0] + " screen ");
			passMessage = nlpRequestModel.getPassMessage();
			failMessage = nlpRequestModel.getFailMessage();
			if(attributes.get("ifCheckPointIsFailed")!=null) {
				String ifFailed = attributes.get("ifCheckPointIsFailed").toString();
				ifCheckPointIsFailed = IfFailed.valueOf(ifFailed);
			}
			log.info("Getting the Display status of " + elementName + " " + elementType);
			modifiedPassMessage = passMessage.replace("*elementName*", elementName)
					.replace("*elementType*", elementType).replace("*elementScreen*", elementScreen);
			modifiedFailMessage = failMessage.replace("*elementName*", elementName)
					.replace("*elementType*", elementType).replace("*elementScreen*", elementScreen);
			displayed = element.isDisplayed();
			log.info(elementName + " " + elementType + " is displayed");
			nlpResponseModel.setMessage(modifiedPassMessage);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.getAttributes().put("displayed", displayed);
		} catch(Throwable e) {
         	log.error("NLP_EXCEPTION in IsDisplayed ", e);
			String exceptionSimpleName = e.getClass().getSimpleName();
			if (containsChildNlp)
				throw new NlpException(exceptionSimpleName);
			nlpResponseModel = ExceptionHandlingInfo.exceptionMessageHandler(modifiedFailMessage, ifCheckPointIsFailed, exceptionSimpleName, e.getStackTrace());
		}
		Long endTime =  System.currentTimeMillis();
		nlpResponseModel.setExecutionTime(endTime-startTime);
		return nlpResponseModel;
	}

	public StringBuilder getTestCode() throws NlpException {
		StringBuilder sb = new StringBuilder();

		sb.append("WebElement element = androidDriver.findElement(ELEMENT);\n");
		sb.append("boolean displayed = element.isDisplayed();\n");

		return sb;
	}

	public List<String> getTestParameters() throws NlpException {
		List<String> params = new ArrayList<>();

		params.add("ELEMENT::By.id(\"randomId\")");

		return params;
	}

}
