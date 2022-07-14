package com.tyss.optimize.data.models.db.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgramElement extends BaseEntity {

    @Transient
    public static final String SEQUENCE_NAME = "PROGRAM_ELEMENT";

    private String id;

    @NotNull(message = "name is mandatory")
    @NotBlank(message = "name must not be blank")
    @Size(min = 3, max = 100, message
            = "Name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "desc is mandatory")
    @Size(max = 200,message ="Description must be 200 characters" )
    private String desc;

    @NotNull(message = "packageName is mandatory")
    private String packageName;

    @NotNull(message = "nlpName is mandatory")
    @Size(min = 3, max = 100, message
            = "nlpName must be between 3 and 100 characters")
    @NotBlank(message = "nlpName must not be blank")
    private String nlpName;

    @NotNull(message = "code is mandatory")
    private String code;

    @NotNull(message = "passMessage is mandatory")
    @NotBlank(message = "passMessage must not be blank")
    @Size(max =110,message = "passMessage must be 110 characters")
    private String passMessage;

    @NotNull(message = "failMessage is mandatory")
    @NotBlank(message = "failMessage must not be blank")
    @Size(max =110,message = "failMessage must be 110 characters")
    private String failMessage;

    @NotNull(message = "language is mandatory")
    private String language;
    private String langVersion;
    private boolean folder;
    double executionOrder;
    @Transient
    public String compilationError;
    private String toolTip;
    private boolean nonPE;

}
