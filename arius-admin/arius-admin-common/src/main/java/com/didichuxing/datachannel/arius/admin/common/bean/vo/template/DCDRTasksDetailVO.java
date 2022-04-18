package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "dcdr主从切换任务详情")
public class DCDRTasksDetailVO extends BaseVO {
    @ApiModelProperty("多个模板dcdr主从切换任务详情")
    private List<DCDRSingleTemplateMasterSlaveSwitchDetailVO> dcdrSingleTemplateMasterSlaveSwitchDetailList;

    @ApiModelProperty("任务总数")
    private Integer                                           total;
    @ApiModelProperty("成功数")
    private Integer                                           successNum;
    @ApiModelProperty("失败数")
    private Integer                                           failedNum;
    @ApiModelProperty("正在运行数")
    private Integer                                           runningNum;
    @ApiModelProperty("取消数")
    private Integer                                           cancelNum;
    @ApiModelProperty("等待运行数")
    private Integer                                           waitNum;
    @ApiModelProperty("总状态 0 取消 1 成功 2 运行中 3 失败 4 待运行")
    private Integer                                           state;
    @ApiModelProperty("运行进度")
    private int                                               percent;
}
