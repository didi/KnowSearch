package com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail;

import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DCDRStatusEnum;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dcdr tasks Detail
 *
 * @author
 * @date 2022/05/09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DCDRTasksDetail extends AbstractTaskDetail {
    private List<DCDRSingleTemplateMasterSlaveSwitchDetail> dcdrSingleTemplateMasterSlaveSwitchDetailList;

    private int                    total;
    private int                    successNum;
    private int                    failedNum;
    private int                    runningNum;
    private int                    cancelNum;
    private int                    waitNum;

    /**
     * 0 取消 1 成功 2 运行中 3 失败 4 待运行
     */
    private int                    state;

    private int                    percent;

    public void calculateProcess() {
        int successNum    = 0;
        int failedNum     = 0;
        int runningNum    = 0;
        int cancelNum     = 0;
        int waitNum       = 0;

        for (DCDRSingleTemplateMasterSlaveSwitchDetail dcdrSingleTemplateMasterSlaveSwitchDetail : this.dcdrSingleTemplateMasterSlaveSwitchDetailList) {
            if (DCDRStatusEnum.SUCCESS.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())) {
                successNum++;
            }
            if (DCDRStatusEnum.CANCELLED.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())) {
                cancelNum++;
            }
            if (DCDRStatusEnum.RUNNING.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())) {
                runningNum++;
            }
            if (DCDRStatusEnum.FAILED.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())) {
                failedNum++;
            }
            if (DCDRStatusEnum.WAIT.getCode().equals(dcdrSingleTemplateMasterSlaveSwitchDetail.getTaskStatus())) {
                waitNum++;
            }
        }

        this.total      = this.dcdrSingleTemplateMasterSlaveSwitchDetailList.size();
        this.successNum = successNum;
        this.failedNum  = failedNum;
        this.runningNum = runningNum;
        this.cancelNum  = cancelNum;
        this.waitNum    = waitNum;
        this.percent    = successNum * 100 / this.total;

        if (runningNum > 0) {
            this.state = DCDRStatusEnum.RUNNING.getCode(); return;
        }
        if (failedNum > 0) {
            this.state = DCDRStatusEnum.FAILED.getCode(); return;
        }
        if (cancelNum == this.dcdrSingleTemplateMasterSlaveSwitchDetailList.size()) {
            this.state = DCDRStatusEnum.CANCELLED.getCode(); return;
        }
        if (cancelNum > 0 && (cancelNum + successNum) == this.dcdrSingleTemplateMasterSlaveSwitchDetailList.size()) {
            this.state = DCDRStatusEnum.CANCELLED.getCode(); return;
        }
        if (successNum > 0 && successNum == this.dcdrSingleTemplateMasterSlaveSwitchDetailList.size()) {
            this.state = DCDRStatusEnum.SUCCESS.getCode();
        }
    }
}