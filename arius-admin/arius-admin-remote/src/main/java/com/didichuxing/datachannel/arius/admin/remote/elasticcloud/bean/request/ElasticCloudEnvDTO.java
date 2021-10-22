package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "环境变量DTO")
public class ElasticCloudEnvDTO extends BaseDTO {

    /**
     * 变量名
     */
    private String name;
    /**
     * 变量值
     */
    private String value;
    }

