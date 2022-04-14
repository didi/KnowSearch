package com.didichuxing.datachannel.arius.admin.task.template;

import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentTemplateTask;

/**
 * Created by linyunan on 2/16/22
 */
@Component
public class DcdrInfoTask extends BaseConcurrentTemplateTask {
    @Override
    public String getTaskName() {
        return "采集dcdr相关数据";
    }

    @Override
    public int poolSize() {
        return 10;
    }

    @Override
    public int current() {
        return 5;
    }

    @Override
    protected boolean executeByLogicTemplate(Integer logicId) throws AdminOperateException {
        return templateLogicManager.updateDCDRInfo(logicId);
    }
}
