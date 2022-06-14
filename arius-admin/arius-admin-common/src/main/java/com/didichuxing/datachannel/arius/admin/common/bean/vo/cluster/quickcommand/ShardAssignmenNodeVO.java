package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详细介绍类情况.
 *
 * @ClassName ShardAssignmenNodeVO
 * @Author gyp
 * @Date 2022/6/7
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardAssignmenNodeVO {
    @ApiModelProperty("节点决策")
    private String nodeDecide;
    @ApiModelProperty("节点名称")
    private String nodeName;
    @ApiModelProperty("解释")
    private String explanation;
}