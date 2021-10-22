package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateHitPO extends BaseESPO {

    private static final ILog LOGGER = LogFactory.getLog(TemplateHitPO.class);

    /**
     * 模版主键id
     */
    private Integer id;
    /**
     * 索引模板名称
     */
    private String name;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 日期
     */
    private String date;

    /**
     * 实际使用周期
     */
    private long useTime;

    private long _1DayCount=0;

    private long _3DayCount=0;

    private long _7DayCount=0;

    private long _30DayCount=0;

    private long _allDayCount=0;

    /**
     * 索引名称命中次数统计
     */
    private Map<String, Long> hitIndexMap = Maps.newHashMap();

    public TemplateHitPO(Integer id, String name, String clusterName, String date) {
        this.id             = id;
        this.name           = name;
        this.clusterName    = clusterName;
        this.date           = date;
    }

    public TemplateHitPO(TemplateHitPO templateHitPO) {
        this.id             = templateHitPO.id;
        this.name           = templateHitPO.name;
        this.clusterName    = templateHitPO.clusterName;
        this.date           = templateHitPO.date;
    }

    public void addCount(String indexName, long count) {
        if (!hitIndexMap.containsKey(indexName)) {
            hitIndexMap.put(indexName, 0L);
        }

        Long value = hitIndexMap.get(indexName);
        hitIndexMap.put(indexName, value + count);
    }

    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%s_%d_%s", EnvUtil.getStr(), id, date.replaceAll(" ", "_"));
    }

    @JSONField(serialize = false)
    public void merge(TemplateHitPO templateHitPO) {
        if (templateHitPO == null) {
            return;
        }

        Map<String, Long> m = templateHitPO.hitIndexMap;
        for (String index : m.keySet()) {
            if (!this.hitIndexMap.containsKey(index)) {
                this.hitIndexMap.put(index, 0L);
            }

            this.hitIndexMap.put(index, this.hitIndexMap.get(index) + m.get(index));
        }
    }

    // 统计当天，3天，7天，30天，全部的查询次数
    private static final Long _ONE_DAY = 24 * 60 * 60 * 1000L;
    public void setSumHits(String express, String timeFormat) {
        try {
            if (!express.endsWith("*") || timeFormat == null || timeFormat.trim().length() == 0) {
                return ;
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

            for (String index : hitIndexMap.keySet()) {
                String dataStr = index.substring(length);
                Long count = hitIndexMap.get(index);
                if(count==null) {
                    continue;
                }

                Long time=null;
                try {
                    time = sdf.parse(dataStr).getTime();
                } catch (Throwable t) {
                    if(extraSdf!=null) {
                        time = extraSdf.parse(dataStr).getTime();
                    }
                }

                if(time==null) {
                    LOGGER.error("parser time error, indexName:" + index + ", express:" + express);
                    continue;
                }

                Long gap = dateTime - time;
                int day = (int) (gap/_ONE_DAY);
                _allDayCount+=count;

                if(day<30) {
                    _30DayCount+=count;
                }

                if(day<7) {
                    _7DayCount+=count;
                }

                if(day<3) {
                    _3DayCount+=count;
                }

                if(day<1) {
                    _1DayCount+=count;
                }
            }

        } catch (Throwable t) {
            LOGGER.error("get used day num error, express:{}, format:{}, hitPO:{}", express, timeFormat, JSON.toJSONString(this), t);
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
            for (String index : hitIndexMap.keySet()) {
                String dataStr = index.substring(length);
                Long count = hitIndexMap.get(index);
                if(count!=null && count<maxQpsPerDay) {
                    continue;
                }

                Long time=null;
                try {
                    time = sdf.parse(dataStr).getTime();
                } catch (Throwable t) {
                    if(extraSdf!=null) {
                        time = extraSdf.parse(dataStr).getTime();
                    }
                }

                if(time==null) {
                    throw new Exception("parse time error");
                }

                Long gap = dateTime - time;
                if (gap > maxGap) {
                    maxGap = gap;
                }
            }

            return maxGap;
        } catch (Throwable t) {
            LOGGER.error("get used day num error, express:{}, format:{}, hitPO:{}", express, timeFormat, JSON.toJSONString(this), t);
            return -1;
        }
    }
}
