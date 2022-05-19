package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "「模板服务」分页查询条件")
public class TemplateSrvQueryDTO extends IndexTemplateDTO {

    @ApiModelProperty("所属集群")
    private String cluster;
}
