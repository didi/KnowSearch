package com.didi.fastdump.adapter.lucene;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.didi.cloud.fastdump.common.bean.adapter.FastDumpBulkInfo;
import com.didi.cloud.fastdump.common.bean.adapter.FastDumpDocument;
import com.didi.fastdump.adapter.ClientAdapter;

/**
 * Created by linyunan on 2022/8/8
 *
 * Lucene文件读取适配器, 提供读取文件相关的能力
 * 底层对应多个Lucene jar 版本: 2.3.0 5.5.0 7.6.0 8.4.0
 */
public interface LuceneClientAdapter extends ClientAdapter<FastDumpBulkInfo> {
    Integer getMaxDoc(String indexPath) throws ExecutionException, IOException;

    /**
     * 获取有效的文档数(未被删除、更新)
     * @param indexPath         lucene 索引路径
     * @param readerIndexType   读取索引的type
     */
    Integer getDocNum(String indexPath, String readerIndexType) throws ExecutionException, IOException;

    /**
     * 获取segment列表文档数的前缀和
     * 多个 segment 的文档总数(包含删除的文档) [1,3,4,5] ——> [0, 1, 4, 8, 13] 注意这里第一步个元素为0, 应该从第二个元素读起
     *
     * @param indexPath        shard路径
     */
    int[] getSegmentPreSum(String indexPath) throws ExecutionException, IOException;

    /**
     * @param indexPath        lucene-index路径
     * @param start            读取文档开始点
     * @param end              读取文档结束点
     * @param segmentPreSumArr segment前缀和, 用于记录当前游标读取文档达到的位置
     * @param index            索引名称
     * @param type             索引type
     * @param ignoreVersion    是否忽略版本
     * @param ignoreId         是否忽略
     * @return List<Document>
     */
    List<FastDumpDocument> getDocumentInfo(String   indexPath,
                                           Integer  start,
                                           Integer  end,
                                           int[]    segmentPreSumArr,
                                           String   index,
                                           String   type,
                                           Boolean  ignoreVersion,
                                           Boolean  ignoreId) throws ExecutionException, IOException;

    /**
     *
     * @param indexPath             lucene-index路径
     * @param start                 读取文档开始点
     * @param end                   读取文档结束点
     * @param segmentPreSumArr      segment前缀和, 用于记录当前游标读取文档达到的位置
     * @param index                 索引名称
     * @param readerType            reader index type
     * @param sinkerType            sinker index type
     * @param ignoreVersion         是否忽略版本
     * @param versionType           版本类型
     * @param ignoreId              是否忽略id
     * @param indexMode             写入模式
     * @param targetClusterVersion  目标集群版本
     * @return                      FastDumpBulkInfo
     * @throws ExecutionException
     * @throws IOException
     */
    FastDumpBulkInfo getFastDumpBulkInfo(String   indexPath,
                                         Integer  start,
                                         Integer  end,
                                         int[]    segmentPreSumArr,
                                         String   index,
                                         String   readerType,
                                         String   sinkerType,
                                         Boolean  ignoreVersion,
                                         String   versionType,
                                         Boolean  ignoreId,
                                         String   indexMode,
                                         String   targetClusterVersion) throws ExecutionException, IOException;

    String getSingleDocInfo(String   indexPath,
                            String   targetClusterVersion,
                            String   type,
                            String   index,
                            Boolean  ignoreVersion,
                            String   versionType,
                            Boolean  ignoreId,
                            String   indexMode) throws ExecutionException, IOException;
}
