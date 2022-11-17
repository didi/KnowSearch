package com.didichuxing.datachannel.arius.admin.biz.listener;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.common.event.template.DCDRLinkAbnormalIndicesRebuildEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 这个类是监听 `DCDRLinkAbnormalIndicesRebuildEvent` 事件的监听器
 *
 * @author shizeying
 * @date 2022/09/26
 */
@Component
public class DCDRLinkAbnormalIndicesRebuildListener
        implements ApplicationListener<DCDRLinkAbnormalIndicesRebuildEvent> {
    private static final ILog                LOGGER = LogFactory.getLog(DCDRLinkAbnormalIndicesRebuildListener.class);
    @Autowired
    private              TemplateDCDRManager templateDCDRManager;
    
    /**
     * @param event
     */
    @Override
    public void onApplicationEvent(DCDRLinkAbnormalIndicesRebuildEvent event) {
        try {
            
            templateDCDRManager.rebuildDCDRLinkAbnormalIndices(event.getCluster(), event.getTargetCluster(), event.getIndices());
        } catch (ESOperateException e) {
            LOGGER.error(
                    "class={}||method=onApplicationEvent||cluster={}||targetCluster={}||indices={}",
                    getClass().getSimpleName(), event.getCluster(), event.getTargetCluster(),
                    String.join(",", event.getIndices()));
        }
    }
}