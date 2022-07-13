package com.didichuxing.datachannel.arius.admin.task.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRSingleTemplateMasterSlaveSwitchDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.DCDRTasksDetail;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DCDRSwithTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 删除脏dcdrlinks随机任务
 *
 * @author shizeying
 * @date 2022/05/09
 */
@Task(name = "DeleteDirtyDCDRLinksRandomTask", description = "删除成功任务中的dcdr脏链路", cron = "0 0 2 */1 * ?", autoRegister = true)
public class DeleteDirtyDCDRLinksRandomTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDirtyDCDRLinksRandomTask.class);

    @Autowired
    private OpTaskManager       opTaskManager;

    @Autowired
    private TemplateDCDRManager templateDcdrManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=DeleteDirtyDCDRLinksRandomTask||method=execute||msg=start");

        //获取失败的dcdr主从切换任务
        List<OpTask> successDcdrSwitchTaskList = opTaskManager
            .getSuccessTaskByType(OpTaskTypeEnum.TEMPLATE_DCDR.getType());
        if (CollectionUtils.isEmpty(successDcdrSwitchTaskList)) {
            return TaskResult.SUCCESS;
        }

        for (OpTask successDcdrSwitchTask : successDcdrSwitchTaskList) {
            DCDRTasksDetail dcdrTasksDetail = JSON.parseObject(successDcdrSwitchTask.getExpandData(),
                DCDRTasksDetail.class);

            if (null == dcdrTasksDetail) {
                continue;
            }

            List<DCDRSingleTemplateMasterSlaveSwitchDetail> switchDetailList = dcdrTasksDetail
                .getDcdrSingleTemplateMasterSlaveSwitchDetailList();
            if (CollectionUtils.isEmpty(switchDetailList)) {
                continue;
            }

            for (DCDRSingleTemplateMasterSlaveSwitchDetail switchDetail : switchDetailList) {
                //强切任务失败，删除脏链路
                if (DCDRSwithTypeEnum.FORCE.getCode().equals(switchDetail.getSwitchType())
                    && !switchDetail.getDeleteDcdrChannelFlag()) {
                    try {
                        Result<Void> deleteDcdrResult = templateDcdrManager.deleteDCDR(
                            switchDetail.getTemplateId().intValue(), AriusUser.SYSTEM.getDesc(),
                            AuthConstant.SUPER_PROJECT_ID);

                        if (deleteDcdrResult.failed()) {
                            LOGGER.error("class=DeleteDirtyDCDRLinksRandomTask||templateId={}||method=execute||msg={}",
                                switchDetail.getTemplateId(), deleteDcdrResult.getMessage());
                            switchDetail.setDeleteDcdrChannelFlag(false);
                        } else {
                            switchDetail.setDeleteDcdrChannelFlag(true);
                        }
                    } catch (ESOperateException e) {
                        LOGGER.error(
                            "class=DeleteDirtyDCDRLinksRandomTask||templateId={}||method=execute||msg=failed to delete dcdr channel",
                            switchDetail.getTemplateId(), e);
                        switchDetail.setDeleteDcdrChannelFlag(false);
                    }
                }
            }

            successDcdrSwitchTask.setExpandData(ConvertUtil.obj2Json(dcdrTasksDetail));
            opTaskManager.updateTask(successDcdrSwitchTask);
        }

        return TaskResult.SUCCESS;
    }
}