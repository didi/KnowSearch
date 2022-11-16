package com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据迁移任务详情
 *
 * @author didi
 * @date 2022/10/23
 */
@ApiModel("数据迁移任务VO")
@Data
public class FastIndexDetailVO extends OpTask {
    @ApiModelProperty("迁移任务状态")
    private FastIndexStats fastIndexStats;
}
