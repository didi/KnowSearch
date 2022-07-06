package com.didichuxing.datachannel.arius.admin.common.bean.po.dsl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/11 下午2:51
 * @Modified By
 *
 * 查询模板qps信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslAnalyzeResultQpsPO extends BaseESPO {

    /**
     * projectid
     */
    private Integer projectId;

    /**
     * 查询模板的md5值
     */
    private String dslTemplateMd5;

    /**
     * 查询次数
     */
    private Long searchCount;
    /**
     * 日期
     */
    private String date;
    /**
     * 时间
     */
    private Long timeStamp;
    /**
     * 数据类型
     */
    private String ariusType;


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    /**
     * 获取主键id
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%d_%s_%s", this.projectId, this.dslTemplateMd5, date);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }

    /**
     * 构建ProjectIdTemplateQpsInfo
     *
     * @param maxQpsTimeTuple
     * @param projectId
     * @param dslTemplateMd5
     * @param date
     * @return
     */
    public static DslAnalyzeResultQpsPO buildProjectIdTemplateQpsInfo(Tuple<Long, Long> maxQpsTimeTuple,
                                                                      Integer projectId, String dslTemplateMd5, String date) {
        DslAnalyzeResultQpsPO dslAnalyzeResultQps = new DslAnalyzeResultQpsPO();
        dslAnalyzeResultQps.setProjectId(projectId);
        dslAnalyzeResultQps.setDslTemplateMd5(dslTemplateMd5);
        dslAnalyzeResultQps.setSearchCount(maxQpsTimeTuple.v2());
        dslAnalyzeResultQps.setDate(date);
        dslAnalyzeResultQps.setTimeStamp(maxQpsTimeTuple.v1());
        dslAnalyzeResultQps.setAriusType("qps");

        return dslAnalyzeResultQps;
    }

}