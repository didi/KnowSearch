package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.checkitem;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfoWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.HealthCheckType;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health.AbstractCheckerItem;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.ESIndicesStatsResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.stats.IndexNodes;
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

    private static final String REPLACEMENT  = "[0-9]{2}";

    @Override
    public HealthCheckType getType(){
        return HealthCheckType.INDEX_EXPIRE_NOT_DELETE;
    }

    @Override
    protected long getCheckerTotalNu() {
        return checksize;
    }

    @Override
    protected List<HealthCheckErrInfoPO> execCheckRecordErrInfo() {
        Date now = new Date();

        ESClient esClient  = getClusterHealthCheckJobConfig().getEsClient();
        List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

        Map<String, IndexTemplatePhyInfoWithLogic> indexTemplateMap = getClusterHealthCheckJobConfig().getIndexTemplateMap();

        for(Map.Entry<String, IndexTemplatePhyInfoWithLogic> entry : indexTemplateMap.entrySet()){
            String template = entry.getKey();

            if(isWhiteIndex(template) || iskibanaIndex(template)){continue;}

            IndexTemplatePhyInfoWithLogic indexTemplate = indexTemplateMap.get(template);

            if(null == indexTemplate || null == indexTemplate.getLogicTemplate()){continue;}

            String expression   = indexTemplate.getExpression();
            String formatStr    = indexTemplate.getLogicTemplate().getDateFormat();
            long   expireTime   = indexTemplate.getLogicTemplate().getExpireTime();

            //只分析按日创建的
            if (StringUtils.isBlank(formatStr) || !formatStr.endsWith("dd") || expireTime == -1){
                continue;
            }

            // 用于正则表达，要移除＊和添加日期正则表达式
            String accurateIndex = "";
            String expirestring   = "expireTime:" + expireTime;

            // 移除＊
            if (expression.contains("*")) {
                accurateIndex = substringBeforeLast(expression, "*");
            } else {
                accurateIndex = expression;
            }

            String reg = formatStr.replace("YY", REPLACEMENT)
                                  .replace("MM", REPLACEMENT)
                                  .replace("dd", REPLACEMENT);
            accurateIndex += reg;

            formatStr = formatStr.replace("YY", "yy");
            formatStr = formatStr.replace("_", "");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);

            ESIndicesStatsResponse resp = esClient.admin().indices().prepareStats().setIndices(expression).execute().actionGet();
            Map<String, IndexNodes> indexNodesMap = resp.getIndicesMap();

            // 遍历累加符合accurate_index正则的存储大小
            for(String index : indexNodesMap.keySet()){
                if (!index.matches(accurateIndex)){continue;}
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
                        HealthCheckErrInfoPO healthCheckErrInfoPo = new HealthCheckErrInfoPO();
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
                    logger.error("class=IndexExpireNotDeleteItem||method=execCheckRecordErrInfo||errMsg=template:{}", e);
                }
            }
        }

        return checkErrInfoPos;
    }
}
