package com.didi.arius.gateway.core.service.arius.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.DSLTemplate;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.arius.DslTemplateService;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.DSLTemplateListResponse;
import com.didi.arius.gateway.remote.response.DSLTemplateResponse;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

import lombok.NoArgsConstructor;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Service
@NoArgsConstructor
public class DslTemplateServiceImpl implements DslTemplateService{

    protected static final ILog bootLogger = LogFactory.getLog( QueryConsts.BOOT_LOGGER);

    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Autowired
    private QueryConfig queryConfig;

    @Autowired
    private ThreadPool threadPool;

    private Cache dslTemplateCache;

    private Cache newDslTemplateCache;

    private long lastModifyTime = 0;

    @Value("${arius.gateway.adminSchedulePeriod}")
    private long adminSchedulePeriod;

    @PostConstruct
    public void init(){
        CacheManager manager = CacheManager.create( DslTemplateService.class.getResourceAsStream("/ehcache.xml"));

        dslTemplateCache    = manager.getCache("dslTemplateCache");
        newDslTemplateCache = manager.getCache("newDslTemplateCache");

        threadPool.submitScheduleAtFixTask(this::resetDslInfo, 10, adminSchedulePeriod);
    }

    @Override
    public void putDSLTemplate(String key, DSLTemplate dslTemplate) {
        Element element = new Element(key, dslTemplate);
        dslTemplateCache.put(element);
    }

    @Override
    public void removeDSLTemplate(String key) {
        dslTemplateCache.remove(key);
    }

    @Override
    public DSLTemplate getDSLTemplate(String key) {
        Element element = dslTemplateCache.get(key);
        if (element != null) {
            return (DSLTemplate) element.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public List<String> getDslTemplateKeys() {
        return dslTemplateCache.getKeys();
    }

    @Override
    public void putNewDSLTemplate(String key, DSLTemplate dslTemplate) {
        Element element = new Element(key, dslTemplate);
        newDslTemplateCache.put(element);
    }

    @Override
    public DSLTemplate getNewDSLTemplate(String key) {
        Element element = newDslTemplateCache.get(key);
        if (element != null) {
            return (DSLTemplate) element.getObjectValue();
        } else {
            return null;
        }
    }

    @Override
    public List<String> getNewDslTemplateKeys() {
        return newDslTemplateCache.getKeys();
    }

    @Override
    public void resetDslInfo(){
        resetDslTemplateInfo();
        try {
            resetDslRateLimit(1);
        } catch (Exception e) {
            bootLogger.error("resetDslRateLimit error", e);
        }
    }

    /************************************************************** private method **************************************************************/
    /**
     * 更新dsl模板
     * @return
     */
    private synchronized boolean resetDslTemplateInfo() {
        bootLogger.info("resetDSLInfo begin...");

        try {
            DSLTemplateListResponse dslTemplateListResponse = ariusAdminRemoteService.listDslTemplates(lastModifyTime, null);
            long runTime = System.currentTimeMillis() - QueryConsts.QUERY_DSL_MODIFY_TIME_EARLY;

            bootLogger.info("resetDSLInfo new Dsl");

            int totalCount = 0;

            do {
                totalCount += dslTemplateListResponse.getData().getDslTemplatePoList().size();

                for (DSLTemplateResponse response : dslTemplateListResponse.getData().getDslTemplatePoList()) {
                    try {
                        String key = response.getKey();

                        boolean queryForbidden = isQueryForbidden(response);

                        double queryLimit = queryConfig.getDslQPSLimit();
                        if (response.getQueryLimit() != null) {
                            queryLimit = response.getQueryLimit();
                        }

                        DSLTemplate dslTemplate = getDSLTemplate(key);
                        initailTemplateValue(response, key, queryForbidden, queryLimit, dslTemplate);
                    } catch (Exception e) {
                        bootLogger.error("unexpect_exception||source={}||e={}", response, Convert.logExceptionStack(e));
                    }
                }

                dslTemplateListResponse = ariusAdminRemoteService.listDslTemplates(lastModifyTime, dslTemplateListResponse.getData().getScrollId());

                //Zero hits mark the end of the scroll and the while loop.
            } while(!dslTemplateListResponse.getData().getDslTemplatePoList().isEmpty());

            lastModifyTime = runTime;

            bootLogger.info("resetDSLInfo end successfully! totalCount={}", totalCount);
            return true;
        } catch (Exception e) {
            bootLogger.error("resetDSLInfo_error||e={}", Convert.logExceptionStack(e));
            return false;
        }
    }

    private boolean isQueryForbidden(DSLTemplateResponse response) {
        boolean queryForbidden = response.getEnable() != null && !response.getEnable();
        if (response.getCheckMode() != null && QueryConsts.CHECK_MODE_BLACK.equals(response.getCheckMode())) {
            queryForbidden = true;
        }
        return queryForbidden;
    }

    private void initailTemplateValue(DSLTemplateResponse response, String key, boolean queryForbidden, double queryLimit, DSLTemplate dslTemplate) {
        if (dslTemplate == null) {
            dslTemplate = new DSLTemplate(queryLimit, queryLimit, queryForbidden);
            putDSLTemplate(key, dslTemplate);

            bootLogger.info("new_dsl_add||key={}||dslTemplate={}", key, dslTemplate);
        } else {
            dslTemplate.setQueryForbidden(queryForbidden);
            if (queryLimit != dslTemplate.getQueryLimit()) {
                dslTemplate.setTotalQueryLimit(queryLimit);
                dslTemplate.setQueryLimit(queryLimit);
                dslTemplate.updateRateLimiter(queryLimit);
            }
        }

        if (response.getEsCostAvg() != null) {
            dslTemplate.setEsCostAvg(response.getEsCostAvg());
        }

        if (response.getTotalHitsAvg() != null) {
            dslTemplate.setTotalHitsAvg(response.getTotalHitsAvg());
        }

        if (response.getTotalShardsAvg() != null) {
            dslTemplate.setTotalShardsAvg(response.getTotalShardsAvg());
        }
    }

    /**
     * 更新dsl模板限流值
     * @param activeCount 存活节点数量
     */
    private void resetDslRateLimit(int activeCount) {
        bootLogger.info("resetDslRateLimit start,activeCount={}", activeCount);

        List<String> dslKeys = getDslTemplateKeys();
        for (String dslKey : dslKeys) {
            DSLTemplate dslTemplate = getDSLTemplate(dslKey);
            if (dslTemplate != null) {
                double queryLimit = dslTemplate.getTotalQueryLimit() / activeCount;
                queryLimit = Math.max(queryLimit, 1.0);

                dslTemplate.setQueryLimit(queryLimit);
                dslTemplate.updateRateLimiter(queryLimit);
            }
        }

        bootLogger.info("resetDslRateLimit end...");
    }
}
