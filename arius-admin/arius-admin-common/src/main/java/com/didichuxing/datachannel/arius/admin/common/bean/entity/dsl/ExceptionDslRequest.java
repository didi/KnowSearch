package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;


import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


/**
 * 异常分析请求类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionDslRequest {

    private static final String TIME_FORMAT = "yyyy-MM-dd-HH:mm:ss";

    /**
     * 查询异常检测时间段结束时刻, 单位毫秒
     */
    private Long end;

    @ApiModelProperty(value = "查询异常检测时间段长度, 单位毫秒", example = "10")
    private Long gap;

    @ApiModelProperty(value = "查询异常检测时间段中，总请求数据小于minCheckQps的查询MD5，不做分析", example = "10")
    private Long minCheckQps;

    @ApiModelProperty(value = "QPS小于minQps的请求，不用于检测异常", example = "10")
    private Long minQps;

    @ApiModelProperty(value = "如果是60*1000,则统计每分钟的请求数目，如果是1000则统计每条的请求数目，结果用于异常分析", example = "10")
    private String interval;

    @ApiModelProperty(value = "请求的查询耗时超过该值，才对查询进行分析", example = "5000")
    private Long esCost;

    @ApiModelProperty(value = "如果历史qps*factor < 当前qps，则说明查询请求涨幅过大，判断为异常信息", example = "5000")
    private Float factor;

    @ApiModelProperty(value = "对查询请求数据增加过滤条件，DSL语句的形式，如term,range", example = "term,range")
    private JSONObject filterDsl;

    @ApiModelProperty(value = "检测类型", example = "esCost只分析耗时超过阈值的查询, qps分析索引查询请求, timedOut只分析超时的查询请求")
    private String type;

    @ApiModelProperty(value = "只分析indices指定的索引相关的查询查询请求", example = "dos_order_201909")
    private String indices;

    @ApiModelProperty(value = "查询异常开始时刻", example = "1550160000000")
    private String gte;

    @ApiModelProperty(value = "查询异常结束时刻", example = "1550246399999")
    private String lte;


    private static final Long  ONE_HOUR = 60 * 60 * 1000L;
    private static final String TYPE_ESCOST_STR = "esCost";
    private static final String TYPE_QPS_STR = "qps";
    private static final String TYPE_TIMEDOUT_STR = "timedOut";

    public void check() {

        // 如果大于 小于值存在
        if (StringUtils.isNotBlank(lte) && StringUtils.isNotBlank(gte)) {
            end = Long.valueOf(lte);
            gap = end - Long.valueOf(gte);
        } else {
            if (end == null) {
                end = System.currentTimeMillis();
            }

            if (gap == null) {
                gap = ONE_HOUR;
            }
        }

        if (minQps == null) {
            minQps = 60L;
        }

        if (interval == null) {
            interval = "1m";
        }

        if (factor == null) {
            factor = 1.2f;
        }

        if (type == null) {
            type = TYPE_ESCOST_STR;
        }
    }

}
