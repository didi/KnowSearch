
package com.didi.arius.gateway.remote.response;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.List;

@Data
public class BaseInfoResponse {

    /**
     * 逻辑模板ID
     */
    @Expose
    private Integer     id;

    @Expose
    private String dateField;
    @Expose
    private String dateFormat;
    @Expose
    private String department;
    @Expose
    private Long expireTime;
    @Expose
    private String expression;
    @Expose
    private Boolean isDefaultRouting;
    @Expose
    private String responsible;
    @Expose
    private Integer version;

    private Integer deployStatus;

    private String ingestPipeline;

    private List<String> aliases;
}
