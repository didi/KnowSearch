package com.didichuxing.datachannel.arius.admin.client.bean.vo.config;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Data
@ApiModel(description = "配置信息")
public class AriusConfigInfoVO extends BaseVO {

    /**
     * 主键
     */
    @ApiModelProperty("配置ID")
    private Integer id;

    /**
     * 配置组
     */
    @ApiModelProperty("配置组")
    private String  valueGroup;

    /**
     * 配置项的名称
     */
    @ApiModelProperty("配置名称")
    private String  valueName;

    /**
     * 配置项的值
     */
    @ApiModelProperty("值")
    private String  value;

    /**
     * 配置项维度  1 集群   2 模板
     */
    @ApiModelProperty("维度")
    private Integer dimension;

    /**
     * 1 正常  2 禁用  -1 删除
     */
    @ApiModelProperty("状态(1 正常；2 禁用；-1 删除)")
    private Integer status;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String  memo;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date    createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    private Date    updateTime;

}
