package com.didichuxing.datachannel.arius.admin.metadata.job.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * @author cjm
 * 计算最近一天生成的的DSL模版限流值大小
 * 根据：totalCostAvg（查询耗时）、totalShardsAvg（查询总shard个数）、totalHitsAvg（查询命中记录数）、responseLenAvg（查询响应长度）
 * 这四个指标的大小，获取权重信息，然后取权重信息平均值，再用默认查询限流值 * 该平均值
 */
@Component
@NoArgsConstructor
public class DslTemplateUpdateNearestQueryLimitJob extends AbstractMetaDataJob {

    @Autowired
    private DslTemplateESDAO dslTemplateESDAO;

    /**
     * 默认的查询限流值
     */
    @Value("${default.query.limit}")
    private Integer          defaultQueryLimit;

    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=DslTemplateUpdateNearestQueryLimitJob||method=handleJobTask||params={}", params);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("set arius create time");

        // 获取到缺少ariusCreateTime字段的文档，手动设置该字段
        List<DslTemplatePO> dslTemplates = dslTemplateESDAO.getMissingAriusCreateTme();

        String ariusCreateTime = DateTimeUtil.getCurrentFormatDateTime();
        for (DslTemplatePO dslTemplatePO : dslTemplates) {
            dslTemplatePO.setAriusCreateTime(ariusCreateTime);
        }
        boolean updateResult = dslTemplateESDAO.updateTemplates(dslTemplates);

        stopWatch.stop().start("get near dsl template");
        // 获取一天内的查询模板信息
        List<DslTemplatePO> dslTemplatePOList = dslTemplateESDAO.getNearestDslTemplate();

        if (CollectionUtils.isEmpty(dslTemplatePOList)) {
            LOGGER.info(
                "class=DslTemplateUpdateNearestQueryLimitJob||method=handleJobTask||msg=nearest dsl template is empty, cost {}",
                dslTemplatePOList.size(), stopWatch.stop().toString());
            return JOB_SUCCESS;
        }

        stopWatch.stop().start("cal query limit and save");
        String ariusModifyTime = DateTimeUtil.getCurrentFormatDateTime();

        Iterator<DslTemplatePO> iterator = dslTemplatePOList.iterator();
        while (iterator.hasNext()) {
            DslTemplatePO dslTemplatePO = iterator.next();
            if (null == dslTemplatePO || null == dslTemplatePO.getTotalCostAvg()
                || null == dslTemplatePO.getTotalShardsAvg() || null == dslTemplatePO.getTotalHitsAvg()
                || null == dslTemplatePO.getResponseLenAvg()) {
                continue;
            }
            //计算其限流值
            double queryLimitValue = calQueryLimit(dslTemplatePO.getTotalCostAvg(), dslTemplatePO.getTotalShardsAvg(),
                dslTemplatePO.getTotalHitsAvg(), dslTemplatePO.getResponseLenAvg());

            if (dslTemplatePO.getQueryLimit() != null
                && 0 == Double.compare(dslTemplatePO.getQueryLimit(), queryLimitValue)) {
                // 如果查询限流值没发生变化，就不需要更新es
                iterator.remove();
            } else {
                // 保留两位小数
                queryLimitValue = (double) Math.round(queryLimitValue * 100) / 100;
                dslTemplatePO.setQueryLimit(queryLimitValue);
                dslTemplatePO.setAriusModifyTime(ariusModifyTime);
            }
        }

        boolean operatorResult = dslTemplateESDAO.updateTemplates(dslTemplatePOList);

        LOGGER.info(
            "class=DslTemplateUpdateNearestQueryLimitJob||method=handleJobTask||msg=set arius create time {}, result {}, update nearest dsl template {}, result {}, cost {}",
            dslTemplates.size(), updateResult, dslTemplatePOList.size(), operatorResult, stopWatch.stop().toString());
        return JOB_SUCCESS;
    }

    /**
     * 根据查询响应信息计算限流值
     *
     * @param totalCostAvg   查询耗时
     * @param totalShardsAvg 查询总shard个数
     * @param totalHitsAvg   查询命中记录数
     * @param responseLenAvg 查询响应长度
     * @return
     */
    private double calQueryLimit(double totalCostAvg, double totalShardsAvg, double totalHitsAvg,
                                 double responseLenAvg) {
        //计算查询耗时比重
        double queryWeight = getQueryWeight(totalCostAvg);

        //计算查询总shard数比重
        double shardWeight;
        if (totalShardsAvg < 10) {
            shardWeight = 1.0;
        } else if (totalShardsAvg < 50) {
            shardWeight = 0.6;
        } else if (totalShardsAvg < 100) {
            shardWeight = 0.2;
        } else {
            shardWeight = 0.1;
        }

        //计算查询命令记录数比重
        double hitsWeight;
        if (totalHitsAvg < 100) {
            hitsWeight = 1.0;
        } else if (totalHitsAvg < 5000) {
            hitsWeight = 0.6;
        } else if (totalHitsAvg < 10000) {
            hitsWeight = 0.2;
        } else {
            hitsWeight = 0.1;
        }

        //计算查询响应长度比重
        double lengthWeight;
        if (responseLenAvg < 1000) {
            lengthWeight = 1.0;
        } else if (responseLenAvg < 10000) {
            lengthWeight = 0.6;
        } else if (responseLenAvg < 50000) {
            lengthWeight = 0.2;
        } else {
            lengthWeight = 0.1;
        }

        double avgWeight = (queryWeight + shardWeight + hitsWeight + lengthWeight) / 4.0;

        return defaultQueryLimit * avgWeight;
    }

    private double getQueryWeight(double totalCostAvg) {
        double queryWeight;
        if (totalCostAvg < 100) {
            queryWeight = 1.0;
        } else if (totalCostAvg < 500) {
            queryWeight = 0.6;
        } else if (totalCostAvg < 1000) {
            queryWeight = 0.2;
        } else {
            queryWeight = 0.1;
        }
        return queryWeight;
    }
}
