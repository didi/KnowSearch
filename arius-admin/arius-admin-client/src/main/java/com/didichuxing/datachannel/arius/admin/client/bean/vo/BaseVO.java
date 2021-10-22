package com.didichuxing.datachannel.arius.admin.client.bean.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author d06679
 * @date 2019/3/13
 */
@ToString
@Data
public class BaseVO implements Serializable {
    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    protected Date createTime;

    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    protected Date updateTime;
}
