package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import java.util.List;

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
public class GatewayTemplateVO extends BaseVO {

    /******************************* 逻辑信息 ********************************/
    @ApiModelProperty("逻辑ID")
    private Integer      id;

    /**
     * 索引模板名称
     */
    @ApiModelProperty("索引模板名称")
    private String       name;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String       dataCenter;

    /**
     * 表达式
     */
    @ApiModelProperty("表达式")
    private String       expression;

    /**
     * 时间字段
     */
    @ApiModelProperty("时间字段")
    private String       dateField;

    /**
     * 时间字段的格式
     */
    @ApiModelProperty("时间字段的格式")
    private String       dateFieldFormat;

    /**
     * id字段
     */
    @ApiModelProperty("id字段")
    private String       idField;

    /**
     * routing字段
     */
    @ApiModelProperty("routing字段")
    private String       routingField;

    /**
     * 索引滚动格式
     */
    @ApiModelProperty("索引滚动格式")
    private String       dateFormat;

    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("数据保存时长")
    private Integer      expireTime;

    /**
     * pipeline
     */
    @ApiModelProperty("pipeline")
    private String       ingestPipeline;

    /******************************* 物理信息 ********************************/

    /**
     * 部署
     */
    @ApiModelProperty("部署状态")
    private Integer      deployStatus;

    /**
     * 版本号
     */
    @ApiModelProperty("版本号")
    private Integer      version;

    /******************************* 别名 ********************************/

    /**
     * 别名
     */
    @ApiModelProperty("别名列表")
    private List<String> aliases;

    /**
     * 是否禁止读
     */
    private Boolean      blockRead;

    /**
     * 是否禁止写
     */
    private Boolean      blockWrite;

}
