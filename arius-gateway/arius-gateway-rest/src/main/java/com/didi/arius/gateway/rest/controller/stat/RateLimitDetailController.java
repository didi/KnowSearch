package com.didi.arius.gateway.rest.controller.stat;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.DSLTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metadata.RateLimitDetail;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author fitz
 * @date 2021/5/25 5:05 下午
 */
@Controller
public class RateLimitDetailController extends StatController {

    public static final String NAME = "rateLimitDetail";
    @Autowired
    private DslTemplateService dslTemplateService;

    @Override
    protected void register() {
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/rateLimitDetail", this);
        controller.registerHandler(RestRequest.Method.GET, "/_gwstat/rateLimitDetail/{appid}", this);
    }

    @Override
    protected String name() {
        return NAME;
    }

    @Override
    protected void handleAriusRequest(QueryContext queryContext, RestRequest request, RestChannel channel, ESClient client) throws Exception {
        int rAppid = request.paramAsInt("appid", QueryConsts.TOTAL_APPId_ID);
        Map<Integer, List<RateLimitDetail>> appidDetail = new TreeMap<>();

        List<String> dslKeys = dslTemplateService.getDslTemplateKeys();
        for (String dslKey : dslKeys) {
            try {
                String[] keyArr = dslKey.split("_");
                int appid = Integer.valueOf(keyArr[0]);
                if (rAppid != QueryConsts.TOTAL_APPId_ID && rAppid != appid) {
                    continue;
                }

                DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate(dslKey);

                RateLimitDetail rateLimitDetail = new RateLimitDetail();
                rateLimitDetail.setAppid(appid);
                rateLimitDetail.setDslMd5(dslKey);

                if (dslTemplate != null) {
                    rateLimitDetail.setQueryLimit(dslTemplate.getQueryLimit());
                    rateLimitDetail.setQueryForbidden(dslTemplate.isQueryForbidden());
                    rateLimitDetail.setEsCostAvg(dslTemplate.getEsCostAvg());
                    rateLimitDetail.setTotalHitsAvg(dslTemplate.getTotalHitsAvg());
                    rateLimitDetail.setTotalShardsAvg(dslTemplate.getTotalShardsAvg());
                }

                if (appidDetail.containsKey(appid)) {
                    List<RateLimitDetail> rateLimitDetails = appidDetail.get(appid);
                    rateLimitDetails.add(rateLimitDetail);
                } else {
                    List<RateLimitDetail> rateLimitDetails = new ArrayList<>();
                    rateLimitDetails.add(rateLimitDetail);
                    appidDetail.put(appid, rateLimitDetails);
                }
            } catch (Throwable e) {
                logger.error("unexpect_exception||dslKey={}||e={}", dslKey, Convert.logExceptionStack(e));
            }
        }

        sendDirectResponse(queryContext, new BytesRestResponse(RestStatus.OK, JSON.toJSONString(appidDetail)));

    }
}
