package com.didi.cloud.fastdump.common.bean.reader.es;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.didi.cloud.fastdump.common.bean.reader.BaseReader;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/24
 */
@Data
public class LuceneReader extends BaseReader {
    /**
     * 任务id
     */
    private String       taskId;

    /**
     * reader所在节点ip
     */
    private String       ip;

    /**
     * 源索引名称
     */
    private String       sourceIndex;

    /**
     * es index type
     */
    private String       type;

    /**
     * 源索引所在集群名称
     */
    private String       sourceCluster;

    /**
     * 对应底层lucene版本
     */
    private String       esVersion;

    /**
     * dataPathList, eg.. /Users/didi/es-package/elasticsearch-6.6.1/data/nodes/0/indices/PkYCfxjpScWXbX_s2GVVlA/2/index
     */
    private List<String> shardDataPathList;

    /**
     * 单次读segment中的文件数, 这批文件作用于bulk写入
     */
    private Integer      singleReadBulkSize;

    /**
     * 读取文件限流值
     */
    private Long         readFileRateLimit;

    /**
     * 是否需要忽略版本
     */
    private Boolean      ignoreVersion = true;

    /**
     * 是否需要忽略id
     */
    private Boolean      ignoreId      = false;
}
