package com.didiglobal.logi.op.manager.infrastructure.common.hander;

import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.op.manager.domain.component.event.ComponentEvent;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralInstallComponent;
import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralScaleComponent;
import com.didiglobal.logi.op.manager.infrastructure.exception.ComponentHandlerException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author didi
 * @date 2022-07-20 4:01 下午
 */
public class ScaleComponentHandler implements ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleComponentHandler.class);

    @Override
    public void eventProcess(ComponentEvent componentEvent) throws ComponentHandlerException {
        try {
            GeneralScaleComponent installComponent = (GeneralScaleComponent) componentEvent.getSource();

            /*Map<String, List<String>> groupToIpList = new LinkedHashMap<>(16);
            installComponent.getGroupConfigList().forEach(config ->
            {
                if (!StringUtils.isEmpty(config.getHosts())) {
                    groupToIpList.put(config.getGroupName(), Arrays.asList(config.getHosts().split(REX)));
                }
            });
            taskDomainService.createTask(content, componentEvent.getOperateType(),
                    componentEvent.getDescribe(), associationId, groupToIpList);*/
        } catch (Exception e) {
            LOGGER.error("event process error.", e);
            throw new ComponentHandlerException(e);
        }

    }

    @Override
    public void taskFinishProcess(String content) throws ComponentHandlerException {

    }

    @Override
    public Integer getOperationType() throws ComponentHandlerException {
        return null;
    }
}
