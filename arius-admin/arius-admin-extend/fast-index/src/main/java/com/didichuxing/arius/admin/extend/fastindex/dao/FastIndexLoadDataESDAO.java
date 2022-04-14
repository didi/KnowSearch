package com.didichuxing.arius.admin.extend.fastindex.dao;

import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import org.springframework.stereotype.Component;

@Component
public class FastIndexLoadDataESDAO extends BaseESDAO {

    /**
     * 查询模板聚合数据的索引名称
     */
    private String indexName;

    /**
     * type名称，主键id是topic_partition_offset
     */
    private String typeName = "type";


}
