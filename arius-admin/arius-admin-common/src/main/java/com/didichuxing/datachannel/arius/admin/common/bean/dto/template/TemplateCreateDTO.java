package com.didichuxing.datachannel.arius.admin.common.bean.dto.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author chengxiang
 * @date 2022/5/26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "创建逻辑模板DTO")
public class TemplateCreateDTO extends BaseDTO {

    @ApiModelProperty("索引模板名称")
    private String name;

    @ApiModelProperty("数据中心")
    private String dataCenter;

    @ApiModelProperty("用户数据类型")
    private Integer dataType;

    @ApiModelProperty("责任人")
    private String responsible;

    @ApiModelProperty("备注")
    private String desc;

    @ApiModelProperty("逻辑集群id")
    private Long resourceId;

    @ApiModelProperty("数据保存时长 单位天")
    private Integer expireTime;

    @ApiModelProperty("数据总量 单位G")
    private Double diskQuota;

    @ApiModelProperty("是否分区")
    private Boolean cyclicalRoll;

    @ApiModelProperty("时间字段")
    private String dateField;

    @ApiModelProperty("时间字段格式")
    private String dateFieldFormat;

    @ApiModelProperty("mapping")
    private String mapping;

    @ApiModelProperty("settings信息")
    private String settings;

    @ApiModelProperty("模板服务等级")
    private Integer level;
}
