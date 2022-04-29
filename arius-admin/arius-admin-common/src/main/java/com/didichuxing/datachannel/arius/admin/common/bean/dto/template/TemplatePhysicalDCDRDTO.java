package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author d06679
 * @date 2019/4/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(description = "模板dcdr链路信息")
public class TemplatePhysicalDCDRDTO extends BaseDTO {

    @ApiModelProperty("物理模板ID")
    private Long physicalId;

    @ApiModelProperty("目标集群")
    private String replicaCluster;

    @ApiModelProperty("物理模板ID列表")
    private List<Long> physicalIds;

    @ApiModelProperty("目标集群列表")
    private List<String> replicaClusters;

    @ApiModelProperty("模板对应的索引上的dcdr链路是否需要删除")
    private Boolean deleteIndexDcdr;

}
