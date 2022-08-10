package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author didi
 * @date 2022-07-16 2:16 下午
 */
@Component
public class ComponentHandlerFactory implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentHandlerFactory.class);

    private Map<Integer, ComponentHandler> handlerMap = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (bean instanceof ComponentHandler) {
                ComponentHandler handler = (ComponentHandler) bean;
                if (handlerMap.containsKey(handler.getOperationType())) {
                    if (null != bean.getClass().getAnnotation(DefaultHandler.class)) {
                        handlerMap.put(handler.getOperationType(), handler);
                    }
                } else {
                    handlerMap.put(handler.getOperationType(), handler);
                }
            }
        } catch (ComponentHandlerException e) {
            LOGGER.error("component handler post processor", e);
        }
        return bean;
    }

    public ComponentHandler getByType(int type) {
        return handlerMap.get(type);
    }
}
