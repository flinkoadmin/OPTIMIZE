package com.tyss.optimize.data.models.db.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "program_elements_nlps")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgramElementNlp extends BaseEntity{
	
	@Transient
    public static final String SEQUENCE_NAME = "PE_NLP";
	
	@Id
    public String id;
	String programElementId;
	String projectId;
	String name;
	String desc;
	String nlpName;
	String passMessage;
	String failMessage;
	Boolean isChanged;
	String nlpType;
	String returnType;
	List<StepInput> stepInputs;
	String toolTip;
	boolean isNonPE;
}
