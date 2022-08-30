package com.didiglobal.logi.op.manager.application.listener;

import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.ComponentHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

import static com.didiglobal.logi.op.manager.infrastructure.common.ResultCode.TASK_EVENT_HANDLE_ERROR;

/**
 * @author didi
 * @date 2022-07-12 8:30 下午
 */
@org.springframework.stereotype.Component
public class ComponentEventListener implements ApplicationListener<ComponentEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentEventListener.class);

    @Autowired
    private ComponentHandlerFactory componentHandlerFactory;

    @Async
    @Override
    public void onApplicationEvent(ComponentEvent componentEvent) {
        try {
            LOGGER.info("监听到事件[{}]", componentEvent.getDescribe());
            componentHandlerFactory.getByType(componentEvent.getOperateType()).eventProcess(componentEvent);
            componentEvent.setValue(Result.success());
        } catch (Exception e) {
            LOGGER.error("处理事件[{}]失败，参数内容[{}], stack:", componentEvent.getDescribe(), componentEvent.getSource(), e);
            componentEvent.setValue(Result.fail(TASK_EVENT_HANDLE_ERROR));
        }
    }
}
