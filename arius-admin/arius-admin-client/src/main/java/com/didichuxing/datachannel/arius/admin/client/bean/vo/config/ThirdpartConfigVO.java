package com.didichuxing.datachannel.arius.admin.client.bean.vo.config;

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
public class ThirdpartConfigVO extends BaseVO {

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
     * 1 正常  2 禁用  -1 删除
     */
    @ApiModelProperty("状态(1 正常；2 禁用；-1 删除)")
    private Integer status;

}
