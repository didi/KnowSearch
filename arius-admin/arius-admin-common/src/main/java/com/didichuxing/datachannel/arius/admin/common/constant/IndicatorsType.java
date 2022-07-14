package com.didichuxing.datachannel.arius.admin.common.constant;

/**
 * @author qiuziyang @date：2017年11月23日
 */
public enum IndicatorsType {
                            /**
                             * 健康分基数
                             */
                            BASE_OF_HEALTH_DEGREES(1, 50, "健康分基数", "健康分基数是历史成本和天访问量综合得分"),
                            /**
                             * 实时写入量
                             */
                            REAL_TIME_WRITE(2, 5, "实时写入量", "写入量与历史同一时间(前后10分钟)写入率比值k作为评分标准"),
                            /**
                             * 实时查询量
                             */
                            REAL_TIME_SEARCH(3, 10, "实时查询量", "一段时间查询数量(历史一个小时为单位同期对比)"),
                            /**
                             * 实时JVM内存使用
                             */
                            REAL_TIME_JVM(4, 5, "实时JVM内存使用", "10分钟内full gc次数"),
                            /**
                             * 实时查询时间耗时
                             */
                            REAL_TIME_SEARCH_COST(5, 10, "实时查询时间耗时", "查询语句索引模板级别的"),
                            /**
                             * 实时磁盘利用率
                             **/
                            REAL_TIME_DISK_RATE(6, 10, "实时磁盘利用率", "索引所在节点实时平均磁盘利用率"),
                            /**
                             * 实时CPU利用率
                             **/
                            REAL_TIME_CPU_USE(7, 10, "实时CPU利用率", "平均节点cpu利用率"),
                            /**
                             * 未知指标
                             **/
                            UNKNOWN(-1, 0, "", "");

    private int    code;
    private int    weight;
    private String name;
    private String desc;

    IndicatorsType(int code, int weight, String name, String desc) {
        this.code = code;
        this.weight = weight;
        this.name = name;
        this.desc = desc;
    }

    public static double getWeightRate(IndicatorsType indicatorsType) {
        double totalWeight = BASE_OF_HEALTH_DEGREES.getWeight() + REAL_TIME_WRITE.getWeight()
                             + REAL_TIME_SEARCH.getWeight() + REAL_TIME_JVM.getWeight()
                             + REAL_TIME_SEARCH_COST.getWeight() + REAL_TIME_DISK_RATE.getWeight()
                             + REAL_TIME_CPU_USE.getWeight();

        return Double.valueOf(indicatorsType.getWeight()) / totalWeight;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getWeight() {
        return weight;
    }
}
