package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import static com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.ComponentHandler;
import com.didiglobal.logi.op.manager.infrastructure.common.hander.base.DefaultHandler;

/**
 * @author didi
 * @date 2022-07-16 2:16 下午
 */
@Component
public class ComponentHandlerFactory implements BeanPostProcessor {

    private static final ILog LOGGER = LogFactory.getLog(ComponentHandlerFactory.class);

    private Map<Integer, ComponentHandler> handlerMap = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        /**
         * 默认是@default实现，方便对接，用户可以自定义处理器
         */
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
        return bean;
    }

    public ComponentHandler getByType(int type) {
        if (type == EXPAND.getType() || type == SHRINK.getType()) {
            return handlerMap.get(SCALE.getType());
        }
        return handlerMap.get(type);
    }
}
