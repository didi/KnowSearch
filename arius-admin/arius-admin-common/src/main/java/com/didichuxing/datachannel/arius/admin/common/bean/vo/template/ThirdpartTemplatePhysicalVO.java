package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "物理模板信息")
public class ThirdpartTemplatePhysicalVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Long                        id;

    @ApiModelProperty("逻辑模板ID")
    private Integer                     logicId;

    @ApiModelProperty("模板名字")
    private String                      name;

    @ApiModelProperty("表达式")
    private String                      expression;

    @ApiModelProperty("所属物理集群")
    private String                      cluster;

    @ApiModelProperty("rack")
    private String                      rack;

    @ApiModelProperty("shard")
    private Integer                     shard;

    @ApiModelProperty("版本")
    private Integer                     version;

    @ApiModelProperty("角色(1:主；2:从)")
    private Integer                     role;

    @ApiModelProperty("状态(1:常规；-1:索引删除中；-2:删除)")
    private Integer                     status;

    @ApiModelProperty("配置")
    private IndexTemplatePhysicalConfig configObj;

}
