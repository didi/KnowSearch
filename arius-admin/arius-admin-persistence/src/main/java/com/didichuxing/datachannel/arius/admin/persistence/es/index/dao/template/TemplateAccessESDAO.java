package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.access.TemplateAccessHistory;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.TemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils.getBeforeDays;
import static com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil.getDateStr;

@Component
@NoArgsConstructor
public class TemplateAccessESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;
    /**
     * type名称
     */
    private final String typeName = "type";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusTemplateAccess();
    }

    /**
     * 批量保存结果
     *
     * @return
     */
    public boolean batchInsert(List<TemplateAccessCountPO> list) {
        return updateClient.batchInsert(EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, list);
    }

    /**
     * 根据时间范围获取索引模板查询统计次数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public List<TemplateAccessCountPO> getAllTemplateAccessByDateRange(String startDate, String endDate) {
        int scrollSize = 1000;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_GET_TEMPLATE_ACCESS_BY_RANGE, scrollSize, startDate, endDate);

        return getTemplateAccessByDsl(dsl, scrollSize);
    }

    public List<TemplateAccessCountPO> getAllTemplateAccessByDateRange(String cluster, String startDate, String endDate) {
        int scrollSize = 1000;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_GET_TEMPLATE_ACCESS_BY_RANGE_AND_CLUSTER, scrollSize, cluster, startDate, endDate);

        return getTemplateAccessByDsl(dsl, scrollSize);
    }

    /**
     * 获取昨天总的访问次数
     * @return
     */
    public Long getYesterDayAllTemplateAccess(String cluster){
        String startDate = getDateStr(getBeforeDays(new Date(), 1));
        String endDate   = getDateStr(new Date());

        List<TemplateAccessCountPO> list = StringUtils.isBlank(cluster) ?
                getAllTemplateAccessByDateRange(startDate, endDate) :
                getAllTemplateAccessByDateRange(cluster,startDate, endDate);

        Long count = 0L;
        for (TemplateAccessCountPO po : list) {
            count += po.getCount();
        }

        return count;
    }

    /**
     * 根据时间范围获取指定索引模板查询统计次数
     *
     * @param templateName
     * @param startDate
     * @param endDate
     * @return
     */
    public List<TemplateAccessCountPO> getAllTemplateAccessByTemplateDateRange(String templateName, String startDate, String endDate) {
        int scrollSize = 1000;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_GET_TEMPLATE_BY_TEMPLATE_RANGE, scrollSize, templateName, startDate, endDate);

        return getTemplateAccessByDsl(dsl, scrollSize);
    }

    /**
     * 根据索引模板名称获取历史查询次数统计
     *
     * @param templateAccessHistory
     * @return
     */
    public List<TemplateAccessCountPO> getAllTemplateAccessHistoryByTemplate(TemplateAccessHistory templateAccessHistory) {
        int scrollSize = 1000;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_GET_TEMPLATE_HISTORY_BY_TEMPLATE,
                scrollSize, templateAccessHistory.getClusterName(), templateAccessHistory.getTemplateName());

        return getTemplateAccessByDsl(dsl, scrollSize);
    }

    /**
     * 根据索引模板ID获取历史查询次数统计
     *
     * @param templateId
     * @return
     */
    public List<TemplateAccessCountPO> getAllTemplateAccessHistoryByTemplateId(Integer templateId) {
        int scrollSize = 1000;
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.SCROLL_GET_TEMPLATE_HISTORY_BY_ID,
                scrollSize, templateId);

        return getTemplateAccessByDsl(dsl, scrollSize);
    }

    /**
     * 根据索引模板Id获取近7天访问记录
     *
     * @param templateId
     * @return
     */
    public List<TemplateAccessCountPO> getTemplateAccessLast7DayByTemplateId(Integer templateId) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_LAST_7_DAY_ACCESS_BY_TEMPLATE_ID,
                templateId);

        return gatewayClient.performRequest(indexName, typeName, dsl, TemplateAccessCountPO.class);
    }

    /**
     * 根据索引模板Id获取近7天访问记录
     *
     * @param logicTemplateId
     * @return
     */
    public List<TemplateAccessCountPO> getTemplateAccessLastNDayByLogicTemplateId(Integer logicTemplateId, Integer days) {
        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_LAST_N_DAY_ACCESS_BY_LOGIC_TEMPLATE_ID, days,
                logicTemplateId);

        return gatewayClient.performRequest(indexName, typeName, dsl, TemplateAccessCountPO.class);
    }

    /**
     * 获取昨日模板访问总量
     * @param template
     * @return
     */
    public Long getYesterDayTemplateAccessCount(String template){
        String startDate = getDateStr(getBeforeDays(new Date(), 1));
        String endDate   = getDateStr(new Date());

        List<TemplateAccessCountPO> list = getAllTemplateAccessByTemplateDateRange(template, startDate, endDate);
        Long count = 0L;
        for (TemplateAccessCountPO po : list) {
            count += po.getCount();
        }

        return count;
    }

    /**
     * 根据查询语句获取索引模板访问次数统计
     *
     * @param dsl
     * @param scrollSize
     * @return
     */
    private List<TemplateAccessCountPO> getTemplateAccessByDsl(String dsl, int scrollSize) {
        List<TemplateAccessCountPO> list = Lists.newLinkedList();

        gatewayClient.queryWithScroll(indexName, typeName, dsl, scrollSize, null, TemplateAccessCountPO.class,
                resultList -> {
                    if (resultList != null) {
                        list.addAll(resultList);
                    }
                } );

        return list;
    }
}
