package com.didichuxing.datachannel.arius.admin.common.bean.vo.task;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("任务中心信息")
public class OpTaskVO  extends BaseVO {
    @ApiModelProperty("id")
    private Integer           id;

    @ApiModelProperty("标题")
    private String            title;

    @ApiModelProperty("任务类型")
    private Integer           taskType;

    @ApiModelProperty("业务数据主键")
    private String            businessKey;

    @ApiModelProperty("任务状态，success:成功 failed:失败 running:执行中 waiting:等待 cancel:取消 pause:暂停")
    private String            status;

    @ApiModelProperty("创建人")
    private String            creator;

    @ApiModelProperty("标记删除")
    private Boolean           deleteFlag;

    @ApiModelProperty("expandData")
    private String            expandData;
}
