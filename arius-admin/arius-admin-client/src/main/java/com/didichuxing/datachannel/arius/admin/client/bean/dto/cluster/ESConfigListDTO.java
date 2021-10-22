package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lyn
 * @date 2020-12-31
 */
@Data
@ApiModel(description = "ES集群配置列表")
public class ESConfigListDTO extends BaseDTO {

    @ApiModelProperty("集群配置列表")
    List<ESConfigDTO> esConfigs;
}
