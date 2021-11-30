package com.didi.arius.gateway.rest.controller.stat;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.DSLTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metadata.RateLimitStat;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.service.arius.DslTemplateService;
import com.didi.arius.gateway.rest.controller.StatController;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author fitz
 * @date 2021/5/25 5:16 下午
 */
@Controller
public class RateLimitInfoController extends StatController {
    public static final String NAME = "rateLimitInfo";
    @Autowired
    private DslTemplateService dslTemplateService;

    public RateLimitInfoController() {
        // pass
    }

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/rateLimitInfo", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {

        Map<Integer, RateLimitStat> appidStats = new TreeMap<>();

        List<String> dslKeys = dslTemplateService.getDslTemplateKeys();
        for (String dslKey : dslKeys) {
            try {
                int appid = Integer.parseInt(dslKey.split("_")[0]);
                if (appidStats.containsKey(appid)) {
                    RateLimitStat rateLimitStat = appidStats.get(appid);
                    rateLimitStat.setDslCount(rateLimitStat.getDslCount() + 1);
                } else {
                    RateLimitStat rateLimitStat = new RateLimitStat();
                    rateLimitStat.setDslCount(1);
                    appidStats.put(appid, rateLimitStat);
                }

                DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate(dslKey);
                if (dslTemplate != null && dslTemplate.isQueryForbidden()) {
                    if (appidStats.containsKey(appid)) {
                        RateLimitStat rateLimitStat = appidStats.get(appid);
                        rateLimitStat.setDslForbiddenCount(rateLimitStat.getDslForbiddenCount() + 1);
                    } else {
                        RateLimitStat rateLimitStat = new RateLimitStat();
                        rateLimitStat.setDslForbiddenCount(1);
                        appidStats.put(appid, rateLimitStat);
                    }
                }
            } catch (Exception e) {
                logger.error("unexpect_exception||dslKey={}||e={}", dslKey, Convert.logExceptionStack(e));
            }
        }

        List<String> newDslKeys = dslTemplateService.getNewDslTemplateKeys();
        for (String dslKey : newDslKeys) {
            try {
                int appid = Integer.parseInt(dslKey.split("_")[0]);
                if (appidStats.containsKey(appid)) {
                    RateLimitStat rateLimitStat = appidStats.get(appid);
                    rateLimitStat.setNewDslCount(rateLimitStat.getNewDslCount() + 1);
                } else {
                    RateLimitStat rateLimitStat = new RateLimitStat();
                    rateLimitStat.setNewDslCount(1);
                    appidStats.put(appid, rateLimitStat);
                }

                DSLTemplate dslTemplate = dslTemplateService.getNewDSLTemplate(dslKey);
                if (dslTemplate != null && dslTemplate.isQueryForbidden()) {
                    if (appidStats.containsKey(appid)) {
                        RateLimitStat rateLimitStat = appidStats.get(appid);
                        rateLimitStat.setNewDslForbiddenCount(rateLimitStat.getNewDslForbiddenCount() + 1);
                    } else {
                        RateLimitStat rateLimitStat = new RateLimitStat();
                        rateLimitStat.setNewDslForbiddenCount(1);
                        appidStats.put(appid, rateLimitStat);
                    }
                }
            } catch (Exception e) {
                logger.error("unexpect_exception||dslKey={}||e={}", dslKey, Convert.logExceptionStack(e));
            }
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, JSON.toJSONString(appidStats)));

    }
}
