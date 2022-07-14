package com.tyss.optimize.data.models.dto.results;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
public class ResponseObject {
    public AssertObject assertObject;
    public Object response;
    public String responseTime;
    public String statusCode;
}
