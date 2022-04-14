package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板扩缩容
 * @author d06679
 * @date 2019/5/7
 */
@Data
@NoArgsConstructor
public class QueryDslLimitEditContent extends BaseContent {

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
