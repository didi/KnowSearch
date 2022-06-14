package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详细介绍类情况.
 *
 * @ClassName ThreadPoolVO
 * @Author gyp
 * @Date 2022/6/14
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolVO {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("queue")
    private Long queue;
    @ApiModelProperty("active")
    private Long active;
    @ApiModelProperty("reject")
    private Long reject;
}