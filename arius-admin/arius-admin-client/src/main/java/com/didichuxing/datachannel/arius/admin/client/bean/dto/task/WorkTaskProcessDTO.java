package com.didichuxing.datachannel.arius.admin.client.bean.dto.task;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author d06679
 * @date 2018/10/25
 */
@Data
@ApiModel(description = "任务处理信息")
public class WorkTaskProcessDTO extends BaseDTO {

    /**
     * 业务主键
     * 任务id
     */
    @ApiModelProperty("任务id")
    private Integer              taskId;

    /**
     * 执行状态
     *   success     成功
     *   failed      失败
     *   running    执行中
     *   waiting     等待
     *   cancel      取消
     *   pause      暂停
     */
    @ApiModelProperty("执行状态")
    private String              status;

    /**
     * 任务步骤
     */
    @ApiModelProperty("任务步骤")
    private Integer              taskProgress;

    /**
     * 扩展信息
     */
    @ApiModelProperty("扩展信息")
    private String expandData;
}
