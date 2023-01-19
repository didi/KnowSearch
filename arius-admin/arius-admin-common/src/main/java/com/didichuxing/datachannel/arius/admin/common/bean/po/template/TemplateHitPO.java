package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.BaseException;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateHitPO extends BaseESPO {

    private static final ILog LOGGER      = LogFactory.getLog(TemplateHitPO.class);

    /**
     * 模版主键id
     */
    private Integer           id;
    /**
     * 索引模板名称
     */
    private String            name;
    /**
     * 集群名称
     */
    private String            clusterName;
    /**
     * 日期
     */
    private String            date;

    /**
     * 实际使用周期
     */
    private long              useTime;

    private long              day1Count   = 0;

    private long              day3Count   = 0;

    private long              day7Count   = 0;

    private long              day30Count  = 0;

    private long              dayAllCount = 0;

    /**
     * 索引名称命中次数统计
     */
    private Map<String, Long> hitIndexMap = Maps.newHashMap();

    public TemplateHitPO(Integer id, String name, String clusterName, String date) {
        this.id = id;
        this.name = name;
        this.clusterName = clusterName;
        this.date = date;
    }

    public TemplateHitPO(TemplateHitPO templateHitPO) {
        this.id = templateHitPO.id;
        this.name = templateHitPO.name;
        this.clusterName = templateHitPO.clusterName;
        this.date = templateHitPO.date;
    }

    public void addCount(String indexName, long count) {
        hitIndexMap.putIfAbsent(indexName, 0L);

        Long value = hitIndexMap.get(indexName);
        hitIndexMap.put(indexName, value + count);
    }

    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%s_%d_%s", EnvUtil.getStr(), id, date.replace(" ", "_"));
    }

    @Override
    public String getRoutingValue() {
        return null;
    }

    @JSONField(serialize = false)
    public void merge(TemplateHitPO templateHitPO) {
        if (templateHitPO == null) {
            return;
        }

        Map<String, Long> m = templateHitPO.hitIndexMap;
        for (Map.Entry<String, Long> entry : m.entrySet()) {
            String index = entry.getKey();
            Long count = entry.getValue();

            hitIndexMap.putIfAbsent(index, 0L);

            this.hitIndexMap.put(index, this.hitIndexMap.get(index) + count);
        }
    }

    // 统计当天，3天，7天，30天，全部的查询次数
    private static final Long ONE_DAY = 24 * 60 * 60 * 1000L;

    public void setSumHits(String express, String timeFormat) {
        try {
            if (!express.endsWith("*") || timeFormat == null || timeFormat.trim().length() == 0) {
                return;
            }
            timeFormat = timeFormat.replace("YYYY", "yyyy");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Long dateTime = sdf.parse(date).getTime();

            SimpleDateFormat extraSdf = null;
            if (!timeFormat.contains("dd")) {
                extraSdf = new SimpleDateFormat("_yyyy-MM-dd");
            }

            int length = express.length();
            if (express.endsWith("*")) {
                length--;
            }

            sdf = new SimpleDateFormat(timeFormat);
            handleHitIndexMap(express, sdf, dateTime, extraSdf, length);

        } catch (Exception t) {
            LOGGER.error(
                "class=TemplateHitPO||method=setSumHits||errMsg=get used day num error, express:{}, format:{}, hitPO:{}",
                express, timeFormat, JSON.toJSONString(this), t);
        }
    }

    private void handleHitIndexMap(String express, SimpleDateFormat sdf, Long dateTime, SimpleDateFormat extraSdf,
                                   int length) throws ParseException {
        for (Map.Entry<String, Long> entry : hitIndexMap.entrySet()) {
            String index = entry.getKey();
            String dataStr = index.substring(length);
            Long count = entry.getValue();
            if (count == null) {
                continue;
            }

            Long time = getTime(sdf, extraSdf, dataStr);

            if (time == null) {
                LOGGER.error("class=TemplateHitPO||method=handleHitIndexMap||errMsg=parser time error, indexName:"
                             + index + ", express:" + express);
                continue;
            }

            Long gap = dateTime - time;
            int day = (int) (gap / ONE_DAY);
            dayAllCount += count;

            if (day < 30) {
                day30Count += count;
            }

            if (day < 7) {
                day7Count += count;
            }

            if (day < 3) {
                day3Count += count;
            }

            if (day < 1) {
                day1Count += count;
            }
        }
    }

    // 返回-1表示不处理
    @JSONField(serialize = false)
    public long getUsedDayNum(String express, String timeFormat, long maxQpsPerDay) {
        try {
            if (!express.endsWith("*") || timeFormat == null || timeFormat.trim().length() == 0) {
                return -1;
            }
            timeFormat = timeFormat.replace("YYYY", "yyyy");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Long dateTime = sdf.parse(date).getTime();

            SimpleDateFormat extraSdf = null;
            if (!timeFormat.contains("dd")) {
                extraSdf = new SimpleDateFormat("_yyyy-MM-dd");
            }

            int length = express.length();
            if (express.endsWith("*")) {
                length--;
            }

            sdf = new SimpleDateFormat(timeFormat);
            Long maxGap = 0L;
            for (Map.Entry<String, Long> entry : hitIndexMap.entrySet()) {
                String index = entry.getKey();
                String dataStr = index.substring(length);
                Long count = entry.getValue();
                if (count != null && count < maxQpsPerDay) {
                    continue;
                }

                Long time = getTime(sdf, extraSdf, dataStr);

                if (time == null) {
                    throw new BaseException("parse time error", ResultType.FAIL);
                }

                Long gap = dateTime - time;
                if (gap > maxGap) {
                    maxGap = gap;
                }
            }
            return maxGap;
        } catch (Exception t) {
            LOGGER.error(
                "class=TemplateHitPO||method=getUsedDayNum||errMsg=get used day num error express:{}, format:{}, hitPO:{}",
                express, timeFormat, JSON.toJSONString(this), t);
            return -1;
        }
    }

    private Long getTime(SimpleDateFormat sdf, SimpleDateFormat extraSdf, String dataStr) throws ParseException {
        Long time = null;
        try {
            time = sdf.parse(dataStr).getTime();
        } catch (Exception t) {
            if (extraSdf != null) {
                time = extraSdf.parse(dataStr).getTime();
            }
        }
        return time;
    }
}
