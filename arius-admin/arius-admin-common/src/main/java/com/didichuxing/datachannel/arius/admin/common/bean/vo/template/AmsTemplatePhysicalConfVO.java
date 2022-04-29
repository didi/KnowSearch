package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "多type索引模板映射信息")
public class AmsTemplatePhysicalConfVO extends BaseVO {

    @ApiModelProperty("逻辑模板ID")
    private Integer logicId;

    @ApiModelProperty("模板名字")
    private String  name;

    /**
     * 用于索引多type改造   是否启用索引名称映射 0 禁用 1 启用
     */
    @ApiModelProperty("是否启用")
    private Boolean mappingIndexNameEnable;

    /**
     * 多type索引type名称到单type索引模板名称的映射
     */
    @ApiModelProperty("映射关系")
    private Map<String/*typeName*/,String/*templateName*/> typeIndexMapping;

}
