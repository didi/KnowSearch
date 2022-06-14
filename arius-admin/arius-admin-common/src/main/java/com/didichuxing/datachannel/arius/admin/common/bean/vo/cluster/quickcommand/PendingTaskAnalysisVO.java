package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.PendingTaskVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * pending task分析.
 *
 * @ClassName PendingTask
 * @Author gyp
 * @Date 2022/6/7
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingTaskAnalysisVO extends PendingTaskVO {
    @ApiModelProperty("节点名称")
    private String timeInQueueMillis;

}