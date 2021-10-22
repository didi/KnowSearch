package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.CheckItem;

import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPo;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.elasticsearch.client.ESClient;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

/**
 * 检查每个索引模板，对于有过期时间的模板，是否有过期的索引未删除
 */
public class IndexExpireNotDeleteItem extends AbstractCheckerItem {
    private int checksize = 0;

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_EXPIRE_NOT_DELETE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return checksize;
    }

    @Override
    protected List<HealthCheckErrInfoPo> execCheckRecordErrInfo() {
        Date now = new Date();

        ESClient esClient  = getClusterHealthCheckJobConfig().getEsClient();
        List<HealthCheckErrInfoPo> checkErrInfoPos = new ArrayList<>();

        Map<String, IndexTemplatePhyWithLogic> indexTemplateMap = getClusterHealthCheckJobConfig().getIndexTemplateMap();
        for(String template : indexTemplateMap.keySet()){
            if(isWhiteIndex(template) || iskibanaIndex(template)){continue;}

            IndexTemplatePhyWithLogic indexTemplate = indexTemplateMap.get(template);

            if(null == indexTemplate || null == indexTemplate.getLogicTemplate()){continue;}

            String expression   = indexTemplate.getExpression();
            String formatStr    = indexTemplate.getLogicTemplate().getDateFormat();
            long   expireTime   = indexTemplate.getLogicTemplate().getExpireTime();

            //只分析按日创建的
            if (StringUtils.isBlank(formatStr) || !formatStr.endsWith("dd") || expireTime == -1){
                continue;
            }

            // 用于正则表达，要移除＊和添加日期正则表达式
            String accurate_index = "";
            String expirestring   = "expireTime:" + expireTime;

            // 移除＊
            if (expression.contains("*")) {
                accurate_index = substringBeforeLast(expression, "*");
            } else {
                accurate_index = expression;
            }

            String reg = formatStr.replaceAll("YY", "[0-9]{2}")
                                  .replaceAll("MM", "[0-9]{2}")
                                  .replaceAll("dd", "[0-9]{2}");
            accurate_index += reg;

            formatStr = formatStr.replaceAll("YY", "yy");
            formatStr = formatStr.replace("_", "");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);

            ESIndicesStatsResponse resp = esClient.admin().indices().prepareStats().setIndices(expression).execute().actionGet();
            Map<String, IndexNodes> indexNodesMap = resp.getIndicesMap();

            // 遍历累加符合accurate_index正则的存储大小
            for(String index : indexNodesMap.keySet()){
                if (!index.matches(accurate_index)){continue;}
                String datestring = "";
                if (reg.contains("_")) {
                    String[] res = index.split("_");
                    datestring = res[res.length - 1];
                } else {
                    datestring = index.substring(index.length() - formatStr.length(), index.length());
                }

                try {
                    Date date = simpleDateFormat.parse(datestring);
                    String nowString = simpleDateFormat.format(now);
                    Date temp = simpleDateFormat.parse(nowString);
                    checksize++;
                    if ((temp.getTime() - date.getTime()) / (24 * 3600 * 1000) > expireTime) {
                        HealthCheckErrInfoPo healthCheckErrInfoPo = new HealthCheckErrInfoPo();
                        healthCheckErrInfoPo.setCheckTypeName(getType().getName());
                        healthCheckErrInfoPo.setTemplate(template);
                        healthCheckErrInfoPo.setIdx(index);
                        healthCheckErrInfoPo.setShard(indexTemplate.getShard().toString());
                        healthCheckErrInfoPo.setRack(indexTemplate.getRack());
                        healthCheckErrInfoPo.setExtendInfo(expirestring + "|" + index);
                        healthCheckErrInfoPo.setValue(String.valueOf(expireTime));
                        healthCheckErrInfoPo.setStatus(1);

                        checkErrInfoPos.add(healthCheckErrInfoPo);
                    }
                }catch (Exception e){
                    LOGGER.error("IndexExpireNotDeleteItem.execCheckRecordErrInfo template:{}", template, e);
                }
            }
        }

        return checkErrInfoPos;
    }
}
