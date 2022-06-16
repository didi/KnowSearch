package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 详细介绍类情况.
 *
 * @ClassName ShardAssignmentDescriptionVO
 * @Author gyp
 * @Date 2022/6/7
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardAssignmentDescriptionVO {
    @ApiModelProperty("是否为主")
    private boolean primary;
    @ApiModelProperty("索引")
    private String index;
    @ApiModelProperty("分片")
    private Integer shard;
    @ApiModelProperty("当前状态")
    private String currentState;

    @ApiModelProperty("节点信息")
    private List<ShardAssignmenNodeVO> decisions;

}