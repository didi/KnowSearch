package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(description = "容器存储卷列表DTO")
public class ElasticCloudVolumeDTO extends BaseDTO {
    /**
    * 存储卷大小
    */
    private int Size ;
    /**
    * 存储卷类型
    */
    private String Type;
    /**
    * 容器内挂载目录
    */
    private String Path;

}
