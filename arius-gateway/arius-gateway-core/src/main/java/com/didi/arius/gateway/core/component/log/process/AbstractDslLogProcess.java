package com.didi.arius.gateway.core.component.log.process;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.core.service.ESRestClientService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author didi
 * @date 2021-09-23 3:41 下午
 */
public abstract class AbstractDslLogProcess implements LogProcess<List<JSONObject>> {

    protected static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);

    public static final String TYPE = "_doc";

    protected ESRestClientService esRestClientService;

    protected IndexTemplateService indexTemplateService;

    protected AbstractDslLogProcess(ESRestClientService esRestClientService, IndexTemplateService indexTemplateService) {
        this.esRestClientService = esRestClientService;
        this.indexTemplateService = indexTemplateService;
    }


    public IndexTemplate getTemplate(String template) {
        IndexTemplate indexTemplate = indexTemplateService.getIndexTemplate(template);
        if (null == indexTemplate) {
            bootLogger.warn("can not find template[{}]", template);
        }
        return indexTemplate;
    }
}
