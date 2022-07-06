package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引配额信息")
public class ConsoleTemplateCapacityVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer id;

    @ApiModelProperty("索引名字")
    private String name;

    /**
     * 周期性滚动  1 滚动   0 不滚动
     */
    @ApiModelProperty("是否滚动")
    private Boolean cyclicalRoll;

    @ApiModelProperty("时间字段")
    private String dateField;

    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("保存天数")
    private Integer expireTime;

    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double quota;
}
