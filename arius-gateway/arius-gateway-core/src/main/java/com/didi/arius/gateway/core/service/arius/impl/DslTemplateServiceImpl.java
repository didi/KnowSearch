package com.didi.arius.gateway.core.service.arius.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.DSLTemplate;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.arius.DslTemplateService;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.DSLTemplateListResponse;
import com.didi.arius.gateway.remote.response.DSLTemplateResponse;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class DslTemplateServiceImpl implements DslTemplateService{

    protected static final Logger bootLogger = LoggerFactory.getLogger( QueryConsts.BOOT_LOGGER);

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

        threadPool.submitScheduleAtFixTask( () -> resetDslInfo(), 0, adminSchedulePeriod );
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
        resetDslRateLimit(1);
    }

    /************************************************************** private method **************************************************************/
    /**
     * 更新dsl模板
     * @return
     */
    synchronized private boolean resetDslTemplateInfo() {
        bootLogger.info("resetDSLInfo begin...");

        try {
            DSLTemplateListResponse dslTemplateListResponse = ariusAdminRemoteService.listDslTemplates(lastModifyTime, null);
            long runTime = System.currentTimeMillis() - QueryConsts.QUERY_DSL_MODIFY_TIME_EARLY;

            bootLogger.info("resetDSLInfo new Dsl");

            int totalCount = 0;

            do {
                totalCount += dslTemplateListResponse.getData().getDslTemplatePoList().size();

                //for (Hit hit : scrollResp.getHits().getHits()) {
                for (DSLTemplateResponse response : dslTemplateListResponse.getData().getDslTemplatePoList()) {
                    try {
                        String key = response.getKey();

                        if (response.getEnable() != null) {
                            if (false == response.getEnable()) {
                                removeDSLTemplate(key);

                                bootLogger.info("dsl_remove||key={}", key);
                                continue ;
                            }
                        }

                        boolean queryForbidden = false;
                        if (response.getCheckMode() != null) {
                            if (QueryConsts.CHECK_MODE_BLACK.equals(response.getCheckMode())) {
                                queryForbidden = true;
                            }
                        }

                        double queryLimit = queryConfig.getDslQPSLimit();
                        if (response.getQueryLimit() != null) {
                            queryLimit = response.getQueryLimit();
                        }

                        DSLTemplate dslTemplate = getDSLTemplate(key);
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
                    } catch (Throwable e) {
                        bootLogger.error("unexpect_exception||source={}||e={}", response, Convert.logExceptionStack(e));
                    }
                }

                dslTemplateListResponse = ariusAdminRemoteService.listDslTemplates(lastModifyTime, dslTemplateListResponse.getData().getScrollId());

                //Zero hits mark the end of the scroll and the while loop.
            } while(dslTemplateListResponse.getData().getDslTemplatePoList().size() != 0);

            lastModifyTime = runTime;

            bootLogger.info("resetDSLInfo end successfully! totalCount={}", totalCount);
            return true;
        } catch (Throwable e) {
            bootLogger.error("resetDSLInfo_error||e={}", Convert.logExceptionStack(e));
            return false;
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
