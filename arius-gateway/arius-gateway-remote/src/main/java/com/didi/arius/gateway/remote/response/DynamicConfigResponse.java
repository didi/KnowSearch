
package com.didi.arius.gateway.remote.response;

import com.google.gson.annotations.Expose;
import lombok.Data;

import javax.annotation.Generated;

@Data
@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class DynamicConfigResponse {

    @Expose
    private Long createTime;
    @Expose
    private Long dimension;
    @Expose
    private Long edit;
    @Expose
    private Long id;
    @Expose
    private String memo;
    @Expose
    private Long status;
    @Expose
    private Long updateTime;
    @Expose
    private String value;
    @Expose
    private String valueGroup;
    @Expose
    private String valueName;

}
