package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-10-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "条件查询索引模板信息")
public class TemplateConditionDTO extends IndexTemplateLogicDTO {
    /**
     * @see AppTemplateAuthEnum
     */
    @ApiModelProperty("当前项目拥有的权限（1:管理；2:读写；3:读; -1无权限）")
    private Integer      authType;

    @ApiModelProperty("所属集群")
    private List<String> clusterPhies;

    @ApiModelProperty("是否具备DCDR")
    private Boolean      hasDCDR;
}
