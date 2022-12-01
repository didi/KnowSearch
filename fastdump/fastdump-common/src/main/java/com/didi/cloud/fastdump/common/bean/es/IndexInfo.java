package com.didi.cloud.fastdump.common.bean.es;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/23
 */
@Data
public class IndexInfo {
    private String index;
    private String uuid;
    private String health;
    private String status;
    private String pri;
    private String rep;
    @JSONField(name = "docs.count")
    private Long   docsCount;
    @JSONField(name = "docs.deleted")
    private Long   docsDeleted;
    @JSONField(name = "store.size")
    private String storeSize;
    @JSONField(name = "pri.store.size")
    private String priStoreSize;

    /**
     * 索引别名
     */
    private String aliases;
}
