package com.didiglobal.logi.op.manager.domain.component.service.handler;

import com.didiglobal.logi.op.manager.infrastructure.common.enums.OperationEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author didi
 * @date 2022-08-24 10:33
 */
@Component
public class ScaleHandlerFactory {

    private Map<Integer, ScaleHandler> scaleHandlerMap = new HashMap<>();

    @Autowired
    private ExpandHandler expandHandler;

    @Autowired
    private ShrinkHandler shrinkHandler;

    @PostConstruct
    private void init() {
        scaleHandlerMap.put(OperationEnum.EXPAND.getType(), expandHandler);
        scaleHandlerMap.put(OperationEnum.SHRINK.getType(), shrinkHandler);
    }

    public ScaleHandler getScaleHandler(int operationType) {
        return scaleHandlerMap.get(operationType);
    }
}
