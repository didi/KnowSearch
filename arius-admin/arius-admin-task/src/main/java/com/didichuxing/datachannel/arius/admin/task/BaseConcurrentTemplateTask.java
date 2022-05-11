package com.didichuxing.datachannel.arius.admin.task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * base任务 模板并发处理
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentTemplateTask extends BaseConcurrentTask<IndexTemplatePO> {

    private static final ILog     LOGGER = LogFactory.getLog(BaseConcurrentClusterTask.class);

    @Autowired
    private IndexTemplateDAO templateLogicDAO;

    @Autowired
    protected TemplateLogicManager templateLogicManager;

    /**
     * 任务全集
     *
     * @return
     */
    @Override
    protected List<IndexTemplatePO> getAllItems() {
        return templateLogicDAO.listAll();
    }

    /**
     * 处理一个批次任务
     *
     * @param taskBatch
     */
    @Override
    protected boolean executeByBatch(TaskBatch<IndexTemplatePO> taskBatch) throws AdminOperateException {
        List<IndexTemplatePO> items = taskBatch.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return true;
        }

        boolean succeed = true;

        for (IndexTemplatePO item : items) {
            IndexTemplatePO logicPO = item;
            try {
                LOGGER.info("class=BaseConcurrentTemplateTask||method=executeByBatc||executeByLogicTemplate begin||template={}||task={}", logicPO.getName(), getTaskName());
                if (executeByLogicTemplate(logicPO.getId())) {
                    LOGGER.info("class=BaseConcurrentTemplateTask||method=executeByBatc||executeByLogicTemplate succ||template={}||task={}", logicPO.getName(), getTaskName());
                } else {
                    succeed = false;
                    LOGGER.warn("class=BaseConcurrentTemplateTask||method=executeByBatc||executeByLogicTemplate fail||template={}||task={}", logicPO.getName(), getTaskName());
                }

                Thread.sleep(TimeUnit.SECONDS.toMillis(TaskConcurrentConstants.SLEEP_SECONDS_PER_EXECUTE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("class=BaseConcurrentTemplateTask||method=executeByBatc||BaseConcurrentTemplateTask Interrupted||task={}", getTaskName(), e);
            } catch (Exception e) {
                succeed = false;
                LOGGER.error("class=BaseConcurrentTemplateTask||method=executeByBatch||errMsg={}||template={}||task={}",
                    e.getMessage(), logicPO.getName(), getTaskName(), e);
            }
        }

        return succeed;
    }

    /**
     * 处理
     * @param logicId
     * @return
     */
    protected abstract boolean executeByLogicTemplate(Integer logicId) throws AdminOperateException;
}
