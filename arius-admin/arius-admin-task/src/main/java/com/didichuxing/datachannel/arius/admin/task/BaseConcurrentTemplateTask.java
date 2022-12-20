package com.didichuxing.datachannel.arius.admin.task;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * base任务 模板并发处理
 * @author d06679
 * @date 2019/3/21
 */
public abstract class BaseConcurrentTemplateTask extends BaseConcurrentTask<IndexTemplatePO> {

    private static final ILog      LOGGER = LogFactory.getLog(BaseConcurrentClusterTask.class);

    @Autowired
    private IndexTemplateDAO       templateLogicDAO;

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

        List<String> succeedTemplateNameList = Lists.newArrayList();
        List<String> failedTemplateNameList = Lists.newArrayList();

        LOGGER.info(
                "class=BaseConcurrentTemplateTask||method=executeByBatch||taskBatch executeByLogicTemplate begin||task={}||templateSize={}||template={}",
                getTaskName(), items.size(), items.stream().map(IndexTemplatePO::getName).collect(Collectors.joining(",")));
        for (IndexTemplatePO item : items) {
            IndexTemplatePO logicPO = item;
            try {
                if (executeByLogicTemplate(logicPO.getId())) {
                    succeedTemplateNameList.add(logicPO.getName());
                } else {
                    succeed = false;
                    failedTemplateNameList.add(logicPO.getName());
                }

                Thread.sleep(TimeUnit.SECONDS.toMillis(TaskConcurrentConstants.SLEEP_SECONDS_PER_EXECUTE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn(
                    "class=BaseConcurrentTemplateTask||method=executeByBatch||BaseConcurrentTemplateTask Interrupted||task={}",
                    getTaskName(), e);
            } catch (Exception e) {
                succeed = false;
                LOGGER.error("class=BaseConcurrentTemplateTask||method=executeByBatch||errMsg={}||template={}||task={}",
                    e.getMessage(), logicPO.getName(), getTaskName(), e);
            }
        }

        if (succeed) {
            LOGGER.info(
                    "class=BaseConcurrentTemplateTask||method=executeByBatch||taskBatch executeByLogicTemplate succ||task={}||templateSize={}||template={}",
                    getTaskName(), succeedTemplateNameList.size(), String.join(",", succeedTemplateNameList));
        } else {
            LOGGER.info(
                    "class=BaseConcurrentTemplateTask||method=executeByBatch||taskBatch executeByLogicTemplate fail||task={}||templateSize={}||succeedTemplateSize={}||failedTemplateSize={}||succeedTemplate={}||failedTemplate={}",
                    getTaskName(), items.size(), succeedTemplateNameList.size(), failedTemplateNameList.size(), StringUtils.join(succeedTemplateNameList, ","), StringUtils.join(failedTemplateNameList, ","));
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
