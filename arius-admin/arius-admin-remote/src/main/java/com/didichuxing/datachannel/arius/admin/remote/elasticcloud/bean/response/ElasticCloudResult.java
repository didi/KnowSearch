package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ElasticCloudResult {

    @ApiModelProperty("异常信息")
    private String            message;

    @ApiModelProperty("字符串返回码：Accepted 接受")
    private String            code;


}
