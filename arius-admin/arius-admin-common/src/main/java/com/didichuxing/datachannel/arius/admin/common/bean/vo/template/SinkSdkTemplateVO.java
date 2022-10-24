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
@ApiModel(description = "基本信息")
public class SinkSdkTemplateVO extends BaseVO {

    /******************************* 逻辑信息 ********************************/
    @ApiModelProperty("逻辑ID")
    private Integer id;

    @ApiModelProperty("索引模板名称")
    private String  name;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

    @ApiModelProperty("表达式")
    private String  expression;

    @ApiModelProperty("时间字段")
    private String  dateField;

    @ApiModelProperty("索引滚动格式")
    private String  dateFormat;

    @ApiModelProperty("数据保存时长")
    private Integer expireTime;

    @ApiModelProperty("pipeline")
    private String  ingestPipeline;

    /******************************* 物理信息 ********************************/

    @ApiModelProperty("部署状态")
    private Integer deployStatus;

    @ApiModelProperty("版本号")
    private Integer version;

}
