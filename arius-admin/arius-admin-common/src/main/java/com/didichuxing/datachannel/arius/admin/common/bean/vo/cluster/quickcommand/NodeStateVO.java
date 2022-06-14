package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 详细介绍类情况.
 *
 * @ClassName indicesVO
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeStateVO {
    @ApiModelProperty("节点名称")
    private String nodeName;
    @ApiModelProperty("segments大小")
    private Long segmentsMemory;
    @ApiModelProperty("cpu占用")
    private Integer osCpu;
    @ApiModelProperty("load_average_5m")
    private double loadAverage5m;

    @ApiModelProperty("threadPoolVOs")
    private List<ThreadPoolVO> threadPoolVOs;
}