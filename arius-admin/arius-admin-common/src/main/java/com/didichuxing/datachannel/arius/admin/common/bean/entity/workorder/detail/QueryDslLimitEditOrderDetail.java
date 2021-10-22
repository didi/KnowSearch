package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

        import lombok.Data;

/**
 * @author zhongyuankai
 * @date 2020/5/18
 */
@Data
public class QueryDslLimitEditOrderDetail extends AbstractOrderDetail {
    /**
     * md5
     */
    private String dslTemplateMd5;

    /**
     * 模板
     */
    private String dslTemplate;

    /**
     * 源限流值
     */
    private Double queryLimit;

    /**
     * 希望限流值
     */
    private Double expectQueryLimit;
}