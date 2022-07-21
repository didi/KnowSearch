package com.didiglobal.logi.op.manager.application.listener;

import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.ComponentHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

/**
 * @author didi
 * @date 2022-07-12 8:30 下午
 */
@org.springframework.stereotype.Component
public class ComponentListener implements ApplicationListener<ComponentEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentListener.class);

    @Autowired
    private ComponentHandlerFactory componentHandlerFactory;

    @Override
    public void onApplicationEvent(ComponentEvent componentEvent) {
        try {
            LOGGER.info("监听到事件[{}]", componentEvent.getDescribe());
            componentHandlerFactory.getByType(componentEvent.getOperateType()).eventProcess(componentEvent);
        } catch (Exception e) {
            LOGGER.error("处理事件[{}]失败，参数内容[{}], stack:", componentEvent.getDescribe(), componentEvent.getSource(), e);
        }
    }
}
