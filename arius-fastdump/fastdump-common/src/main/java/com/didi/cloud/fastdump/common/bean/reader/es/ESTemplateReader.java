package com.didi.cloud.fastdump.common.bean.reader.es;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/24
 */
@Data
public class ESTemplateReader extends BaseESReader {
    /**
     * 源模板名称
     */
    private String  sourceTemplate;

    /**
     * 对应底层lucene版本
     */
    private String  esVersion;

    /**
     * 索引写入映射类型
     * @see com.didi.cloud.fastdump.common.enums.IndexMapTypeEnum
     */
    private String  indexMapType;

    /**
     * 单次读segment中的文件数, 这批文件作用于bulk写入
     */
    private Integer singleReadBulkSize = 5000;

    /**
     * 是否需要忽略版本
     */
    private Boolean ignoreVersion      = true;

    /**
     * 是否需要忽略id
     */
    private Boolean ignoreId           = true;

    private Long    readFileRateLimit;

}
