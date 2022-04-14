package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.app;

import com.didichuxing.datachannel.arius.admin.common.bean.po.query.AppIdTemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESConstant;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggr;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESAggrMap;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

import static com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil.getDateStr;

@Component
@NoArgsConstructor
public class AppIdTemplateAccessESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;
    /**
     * type名称
     */
    private String typeName = "type";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusAppidTemplateAccess();
    }

    /**
     * 批量保存结果
     *
     * @return
     */
    public boolean batchInsert(List<AppIdTemplateAccessCountPO> list) {
        return updateClient.batchInsert( EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 根据索引模板获取最近7天访问appid列表
     *
     * @param templateName
     * @return
     */
    public List<Integer> getAccessAppidsByTemplateName(String templateName) {
        List<Integer> appids = Lists.newArrayList();

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ACCESS_APPIDS_BY_TEMPLATE_NAME, templateName);

        ESAggrMap esAggrMap = gatewayClient.performAggRequest(this.indexName, typeName, dsl);
        if (esAggrMap == null) {
            return appids;
        }

        Set<Integer> appidSets = Sets.newTreeSet();
        String key = null;
        ESAggr esAggr = esAggrMap.getEsAggrMap().get("appId");
        List<ESBucket> esBucketList = esAggr.getBucketList();
        if (esBucketList != null) {
            for (ESBucket esBucket : esBucketList) {
                if (esBucket.getUnusedMap() == null || esBucket.getUnusedMap().isEmpty()) {
                    continue;
                }

                key = esBucket.getUnusedMap().get(ESConstant.AGG_KEY).toString();
                appidSets.add(Integer.valueOf(key));
            }
        }

        appids.addAll(appidSets);

        return appids;
    }

    /**
     * 根据索引Id获取最近days天访问appid详细信息
     *
     * @param logicTemplateId
     * @return
     */
    public List<AppIdTemplateAccessCountPO> getAccessAppidsInfoByTemplateId(int logicTemplateId, int days){
        final int scrollSize = 5000;

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ACCESS_APPIDS_INFO_BY_LOGIC_EMPLATE_ID, scrollSize, logicTemplateId, days);

        List<AppIdTemplateAccessCountPO> accessCountPos = Lists.newLinkedList();
        gatewayClient.queryWithScroll(indexName,
                typeName, dsl, scrollSize, null, AppIdTemplateAccessCountPO.class, resultList -> {
                    if (resultList != null) {
                        accessCountPos.addAll(resultList);
                    }
                });

        return accessCountPos;
    }

    /**
     * 根据索引Id获取最近days天访问appid详细信息
     *
     * @param logicTemplateId
     * @return
     */
    public List<AppIdTemplateAccessCountPO> getAccessAppidsInfoByTemplateId(int logicTemplateId, Long startDate, Long endDate){
        final int scrollSize = 5000;

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_ACCESS_APPIDS_INFO_BY_LOGIC_TEMPLATE_ID_AND_DATE_RANGE, scrollSize, logicTemplateId,
                getDateStr(startDate), getDateStr(endDate));

        List<AppIdTemplateAccessCountPO> accessCountPos = Lists.newLinkedList();
        gatewayClient.queryWithScroll(indexName,
                typeName, dsl, scrollSize, null, AppIdTemplateAccessCountPO.class, resultList -> {
                    if (resultList != null) {
                        accessCountPos.addAll(resultList);
                    }
                });

        return accessCountPos;
    }
}
