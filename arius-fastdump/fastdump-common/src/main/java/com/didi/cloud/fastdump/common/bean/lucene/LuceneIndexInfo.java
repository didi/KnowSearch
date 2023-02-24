package com.didi.cloud.fastdump.common.bean.lucene;

import java.util.List;

import com.didi.cloud.fastdump.common.bean.BaseEntity;

import lombok.Builder;
import lombok.Data;

/**
 * Created by linyunan on 2022/9/22
 */
@Data
public class LuceneIndexInfo extends BaseEntity {
    /**
     * lucene index 所在文件目录
     */
    private String                      luceneIndexDataPath;

    /**
     * index type
     */
    private String                      type;

    /**
     * 任务id
     */
    private String                      taskId;
    /**
     * bulk 所在 lucene index 中的总文档数(包含删除文档)
     */
    private Integer                     maxDoc;
    /**
     * bulk 所在 lucene index 中的有效文档数
     */
    private Integer                     validDoc;
    /**
     * bulk 所在 lucene index 目录
     */
    private String                      shardDataPath;

    /**
     * segment前缀和数组, 用于根据文档游标获取指定segment
     */
    private int[]                       segmentPreSumArr;

    /**
     * 当前lucene index 中的segment信息, 把segment 中的文档进行bulk划分
     */
    private List<LuceneSegmentBulkInfo> luceneSegmentBulkInfos;

    @Data
    @Builder
    public static class LuceneSegmentBulkInfo {
        /**
         * bulk 的起始点
         */
        private Integer startPointer;
        /**
         * bulk 的终止点
         */
        private Integer endPointer;
    }
}
