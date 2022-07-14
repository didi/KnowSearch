package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.MetricsContentVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didiglobal.logi.elasticsearch.client.response.query.query.aggs.ESBucket;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-08-05
 */
public class MetricsUtils {
    /**
     * 突增定义倍数 上个时间间隔请求数的两倍，
     * 例子: 上一个时间间隔是 1000r/s  当前时间间隔是 2500 r/s 超过2000, 则定义为突增
     */
    private static final double UPRUSH_THRESHOLD = 2.0;

    private static final long   ONE_HOUR         = 60 * 60 * 1000L;
    private static final long   ONE_DAY          = 24 * 60 * 60 * 1000L;
    private static final long   SEVEN_DAY        = 7 * 24 * 60 * 60 * 1000L;
    private static final char   COLON            = ':';
    private static final char   SPACE            = ' ';

    /**
     *   指标看板返回值中的时间间隔:
     *   1小时            1min为一时间分片
     *   1小时 ~ 24小时   20min为一时间分片
     *   1天到7天         1h为一时间分片
     *   7天以后          1h为一时间分片
     * @param intervalTime     起始结束时间差值
     * @return  返回dsl时间分隔值
     */
    public static String getInterval(Long intervalTime) {
        if (intervalTime > 0 && intervalTime <= ONE_HOUR) {
            return Interval.ONE_MIN.getStr();
        } else if (intervalTime > ONE_HOUR && intervalTime <= ONE_DAY) {
            return Interval.TWENTY_MIN.getStr();
        } else if (intervalTime > ONE_DAY && intervalTime <= SEVEN_DAY) {
            return Interval.ONE_HOUR.getStr();
        } else if (intervalTime > SEVEN_DAY) {
            return Interval.ONE_HOUR.getStr();
        } else {
            return Interval.ONE_HOUR.getStr();
        }
    }

    /**
     *   dashboard返回值中的时间间隔:
     *   1小时            5min为一时间分片
     *   1小时 ~ 24小时   20min为一时间分片
     *   1天到7天         1h为一时间分片
     *   7天以后          1h为一时间分片
     * @param intervalTime     起始结束时间差值
     * @return  返回dsl时间分隔值
     */
    public static String getIntervalForDashBoard(Long intervalTime) {
        if (intervalTime > 0 && intervalTime <= ONE_HOUR) {
            return Interval.FIVE_MIN.getStr();
        } else if (intervalTime > ONE_HOUR && intervalTime <= ONE_DAY) {
            return Interval.TWENTY_MIN.getStr();
        } else if (intervalTime > ONE_DAY && intervalTime <= SEVEN_DAY) {
            return Interval.ONE_HOUR.getStr();
        } else if (intervalTime > SEVEN_DAY) {
            return Interval.ONE_HOUR.getStr();
        } else {
            return Interval.ONE_HOUR.getStr();
        }
    }

    /**
     * 获取时间点对应聚合时间段
     * @param intervalTime
     * @param timePoint
     * @return
     */
    public static Tuple<Long, Long> getSortInterval(Long intervalTime, long timePoint) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timePoint);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (intervalTime > 0 && intervalTime <= ONE_HOUR) {
            long tuple1 = cal.getTimeInMillis();
            return new Tuple<>(tuple1 - 60 * 1000, tuple1);
        } else if (intervalTime > ONE_HOUR && intervalTime <= ONE_DAY) {
            int minute = cal.get(Calendar.MINUTE);
            int start = 0;
            int end = 20;
            if (minute > 20 && minute <= 40) {
                start = 20;
                end = 40;
            } else if (minute > 40 && minute <= 60) {
                start = 40;
                end = 60;
            }
            cal.set(Calendar.MINUTE, start);
            long tuple1 = cal.getTimeInMillis();
            cal.set(Calendar.MINUTE, end);
            long tuple2 = cal.getTimeInMillis();
            return new Tuple<>(tuple1, tuple2);
        } else {
            cal.set(Calendar.MINUTE, 0);
            long tuple1 = cal.getTimeInMillis();
            return new Tuple<>(tuple1, tuple1 + 60 * 60 * 1000);
        }
    }

    public static Double getDoubleValuePerMin(String interval, String value) {
        if (Interval.ONE_MIN.getStr().equals(interval)) {
            return Double.valueOf(value);
        } else if (Interval.TWENTY_MIN.getStr().equals(interval)) {
            return Double.valueOf(value) / 20.00;
        } else if (Interval.ONE_HOUR.getStr().equals(interval)) {
            return Double.valueOf(value) / 60.00;
        }
        return Double.valueOf(value);
    }

    /**
     * 判断是否需要单位转化，一般以_count结尾的指标都是累加的结果，需要单位转化。
     * eg: 20分钟聚合的指标数据/20 = 个/min
     * @return
     */
    public static boolean needConvertUnit(String aggKey) {
        return aggKey.endsWith("_count");
    }

    /**
     * 计算一段时间内，时间分片
     * @param startTime
     * @param endTime
     * @param interval
     * @param dateUnit   Calendar.MINUTE||Calendar.SECOND||Calendar.HOUR
     * @return
     */
    public static List<Long> timeRange(long startTime, long endTime, long interval, int dateUnit) {
        long step = interval * 1000 * 60;
        if (dateUnit == Calendar.HOUR) {
            step = interval * 1000 * 60 * 60;
        } else if (dateUnit == Calendar.SECOND) {
            step = interval * 1000;
        }
        startTime = startTime / 1000 * 1000;
        endTime = endTime / 1000 * 1000;
        if (endTime < startTime) {
            return new ArrayList<>(0);
        }

        List<Long> list = Lists.newArrayList();
        while (endTime >= startTime) {
            list.add(endTime);
            endTime -= step;
        }

        return list;
    }

    public static double getAggMapDoubleValue(ESBucket esBucket, String key) {
        return Optional.ofNullable(esBucket.getAggrMap().get(key)).map(x -> x.getUnusedMap().get("value"))
            .map(d -> Double.valueOf(d.toString())).orElse(0.0);
    }

    public static List<VariousLineChartMetricsVO> joinDuplicateTypeVOs(List<VariousLineChartMetricsVO> duplicatedVOs) {
        List<VariousLineChartMetricsVO> result = new ArrayList<>();
        Map<String, List<MetricsContentVO>> chartMetricsMap = new HashMap<>();
        for (VariousLineChartMetricsVO vo : duplicatedVOs) {
            if (!chartMetricsMap.containsKey(vo.getType())) {
                chartMetricsMap.put(vo.getType(), vo.getMetricsContents());
            } else {
                chartMetricsMap.get(vo.getType()).addAll(vo.getMetricsContents());
            }
        }
        for (Map.Entry<String, List<MetricsContentVO>> entry : chartMetricsMap.entrySet()) {
            VariousLineChartMetricsVO variousLineChartMetricsVO = new VariousLineChartMetricsVO();
            variousLineChartMetricsVO.setType(entry.getKey());
            variousLineChartMetricsVO.setMetricsContents(entry.getValue());
            result.add(variousLineChartMetricsVO);
        }
        return result;
    }

    public enum Interval {
                          ONE_MIN("1m"), FIVE_MIN("5m"), TWENTY_MIN("20m"), ONE_HOUR("1h");

        private String str;

        private Interval(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }

        public static MetricsUtils.Interval getByStr(String str) {
            for (MetricsUtils.Interval type : MetricsUtils.Interval.values()) {
                if (type.str.equalsIgnoreCase(str)) {
                    return type;
                }
            }

            return null;
        }
    }

    /**
     * 具体计算突增逻辑处
     * @param currentTimeValue      当前采集值
     * @param lastTimeValue         上次采集值
     * @return
     */
    public static Double computerUprushNum(Double currentTimeValue, Double lastTimeValue) {
        if (null == lastTimeValue || null == currentTimeValue) {
            return 0d;
        }

        if (0 == lastTimeValue && 0 > currentTimeValue) {
            return currentTimeValue;
        }
        if (0 == lastTimeValue && 0 < currentTimeValue) {
            return 0d;
        }

        return (currentTimeValue / lastTimeValue) >= UPRUSH_THRESHOLD ? currentTimeValue : 0d;
    }
}
