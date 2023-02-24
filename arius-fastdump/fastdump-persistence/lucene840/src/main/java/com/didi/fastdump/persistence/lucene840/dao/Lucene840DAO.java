package com.didi.fastdump.persistence.lucene840.dao;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SoftDeletesDirectoryReaderWrapper;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.didi.cloud.fastdump.common.bean.adapter.FastDumpBulkInfo;
import com.didi.cloud.fastdump.common.bean.adapter.FastDumpDocument;
import com.didi.cloud.fastdump.common.enums.IndexModeEnum;
import com.didi.cloud.fastdump.common.utils.ListUtils;
import com.didi.cloud.fastdump.common.utils.lucene.LuceneReaderUtil;
import com.didi.fastdump.adapter.BaseLuceneClient;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by linyunan on 2022/8/8
 */
public class Lucene840DAO extends BaseLuceneClient {
    /**
     * es7.6 查询端会依赖此过滤项
     */
    public static final String SOFT_DELETES_FIELD = "__soft_deletes";
    private static final Logger LOGGER = LoggerFactory.getLogger(Lucene840DAO.class);

    private static final Cache<String, DirectoryReader> DIRECTORY840_READER_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    @Override
    public Integer getMaxDoc(String shardDataPath) throws ExecutionException {
        DirectoryReader directoryReader = getDirectoryReader(shardDataPath);
        LOGGER.info("class=Lucene840DAO||method=getMaxDoc||maxDoc={}", directoryReader.maxDoc());
        return directoryReader.maxDoc();
    }

    @Override
    public Integer getDocNum(String shardDataPath, String readerIndexType) throws ExecutionException {
        DirectoryReader directoryReader = getDirectoryReader(shardDataPath);
        LOGGER.info("class=Lucene840DAO||method=getDocNum||docNum={}", directoryReader.numDocs());
        return directoryReader.numDocs();
    }

    @Override
    public int[] getSegmentPreSum(String shardDataPath) throws ExecutionException {
        DirectoryReader directoryReader = getDirectoryReader(shardDataPath);

        List<LeafReaderContext> leaves = directoryReader.getContext().leaves();

        int[] preSum = new int[leaves.size() + 1];
        for (int i = 1; i <= leaves.size(); i++) {
            // 注意这里是 maxDoc(连续) ,不能是numDocs(离散)
            preSum[i] = preSum[i - 1] + leaves.get(i - 1).reader().maxDoc();
        }
        return preSum;
    }

    @Override
    public List<FastDumpDocument> getDocumentInfo(String shardDataPath,
                                                  Integer start,
                                                  Integer end,
                                                  int[] segmentPreSumArr,
                                                  String index,
                                                  String type,
                                                  Boolean ignoreVersion,
                                                  Boolean ignoreId) throws ExecutionException, IOException {
        DirectoryReader directoryReader = getDirectoryReader(shardDataPath);

        List<LeafReaderContext> leaves = directoryReader.getContext().leaves();

        List<FastDumpDocument> fastDumpDocumentList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            // 获取当前文档所在segment
            int segmentIndex  = LuceneReaderUtil.subIndex(i, segmentPreSumArr);
            LeafReader reader = leaves.get(segmentIndex).reader();

            // 计算单个segment 中的docId, docId 在segment中是连续的
            int docId = i - segmentPreSumArr[segmentIndex];

            //非存活的doc过滤掉
            Bits liveDocs = reader.getLiveDocs();

            if (null != liveDocs && !liveDocs.get(docId)) {
                continue;
            }

            Document document = reader.document(docId);

            //对于嵌套类文档，是不存储的，将其过滤掉
            if (0 == document.getFields().size()) { continue;}

            // 构建Document数据
            FastDumpDocument fastDumpDocument = new FastDumpDocument();
            fastDumpDocument.setIndex(index);

            fastDumpDocument.setType(type);
            if (!ignoreId) {
                String id = Lucene840UidUtil.decodeId(document.getField("_id").binaryValue().bytes);
                fastDumpDocument.setId(id);
            }
            byte[] sources = document.getField("_source").binaryValue().bytes;
            JSONObject source = (JSONObject) JSON.parse(
                    new String(sources),
                    Feature.config(JSON.DEFAULT_PARSER_FEATURE, Feature.UseBigDecimal, false));
            fastDumpDocument.setSource(source);

            // 设置route
            IndexableField route = document.getField("_routing");
            fastDumpDocument.setRouting(route == null ? null : route.stringValue());

            // 设置版本
            if (!ignoreVersion) {
                NumericDocValues numericDocValues = reader.getNumericDocValues("_version");
                numericDocValues.advanceExact(docId);
                fastDumpDocument.setVersion(numericDocValues.longValue());
            }

            fastDumpDocumentList.add(fastDumpDocument);
        }
        return fastDumpDocumentList;
    }

    @Override
    public FastDumpBulkInfo getFastDumpBulkInfo(String shardDataPath,
                                                Integer start,
                                                Integer end,
                                                int[] segmentPreSumArr,
                                                String index,
                                                String readerType,
                                                String sinkerType,
                                                Boolean ignoreVersion,
                                                String  versionType,
                                                Boolean ignoreId,
                                                String  indexMode,
                                                String  targetClusterVersion) throws ExecutionException, IOException {

        DirectoryReader directoryReader = getDirectoryReader(shardDataPath);

        List<LeafReaderContext> leaves = directoryReader.getContext().leaves();

        // 判断是否要带上 type
        boolean ignoreType  = targetClusterVersion.startsWith("7.") || targetClusterVersion.startsWith("8.");
        boolean editRouting = targetClusterVersion.startsWith("7.") || targetClusterVersion.startsWith("8.");

        StringBuilder documentBuilder = new StringBuilder();
        int invalidCount = 0;
        for (int i = start; i < end; i++) {
            // 获取当前文档所在segment
            int segmentIndex  = LuceneReaderUtil.subIndex(i, segmentPreSumArr);
            LeafReader reader = leaves.get(segmentIndex).reader();

            // 计算单个segment 中的docId, docId 在segment中是连续的
            int docId = i - segmentPreSumArr[segmentIndex];

            //非存活的doc过滤掉
            Bits liveDocs = reader.getLiveDocs();

            if (null != liveDocs && !liveDocs.get(docId)) {
                invalidCount++;
                continue;
            }

            Document document = reader.document(docId);

            //对于嵌套类文档，是不存储的，将其过滤掉
            if (0 == document.getFields().size()) {
                invalidCount++;
                continue;
            }

            // 构建Document bulk 写数据, 自底向上构建文档信息
            // 1. 构建二级文档信息
            JSONObject childJsonObj = new JSONObject();
            childJsonObj.put("_index", index);
            if (!ignoreId) {
                String id = Lucene840UidUtil.decodeId(document.getField("_id").binaryValue().bytes);
                childJsonObj.put("_id", id);
            }

            // 设置route
            IndexableField route = document.getField("_routing");
            if (editRouting) {
                childJsonObj.put("routing", route == null ? null : route.stringValue());
            } else {
                childJsonObj.put("_routing", route == null ? null : route.stringValue());
            }

            // 设置type
            if (!ignoreType) {
                childJsonObj.put("_type", sinkerType == null ? "_doc" : sinkerType);
            }

            // 2. 构建一级文档信息, 且把二级文档信息添加入内
            JSONObject parentJsonObj = new JSONObject();
            if (indexMode == null || IndexModeEnum.CREATE.getMode().equals(indexMode)) {
                parentJsonObj.put("create", childJsonObj);
            } else if (IndexModeEnum.INSERT.getMode().equals(indexMode)) {
                parentJsonObj.put("index", childJsonObj);
            } else if (IndexModeEnum.UPDATE.getMode().equals(indexMode)) {
                parentJsonObj.put("index", childJsonObj);
                // 设置版本
                if (!ignoreVersion) {
                    NumericDocValues numericDocValues = reader.getNumericDocValues("_version");
                    numericDocValues.advanceExact(docId);
                    childJsonObj.put("version", numericDocValues.longValue());
                    childJsonObj.put("version_type", versionType);
                }
            }

            byte[] sources = document.getField("_source").binaryValue().bytes;

            JSONObject source = (JSONObject) JSON.parse(new String(sources),
                    Feature.config(JSON.DEFAULT_PARSER_FEATURE, Feature.UseBigDecimal, false));

            documentBuilder.append(parentJsonObj.toJSONString()).append("\n");
            documentBuilder.append(source).append("\n");
        }

        FastDumpBulkInfo fastDumpBulkInfo = new FastDumpBulkInfo();
        fastDumpBulkInfo.setBulkDocFlatToString(documentBuilder.toString());
        int documents = end - start - invalidCount;
        fastDumpBulkInfo.setBulkDocNum(documents);
        return fastDumpBulkInfo;
    }

    @Override
    public String getSingleDocInfo(String indexPath,
                                   String  targetClusterVersion,
                                   String type,
                                   String index,
                                   Boolean ignoreVersion,
                                   String versionType,
                                   Boolean ignoreId,
                                   String indexMode)
            throws ExecutionException, IOException {

        DirectoryReader directoryReader = getDirectoryReader(indexPath);
        StringBuilder documentBuilder = new StringBuilder();
        List<LeafReaderContext> leaves = directoryReader.leaves();
        LeafReader reader = leaves.get(0).reader();

        int maxDoc = reader.maxDoc();
        if (0 == maxDoc) { return documentBuilder.toString();}

        // 判断是否要带上 type
        boolean ignoreType  = targetClusterVersion.startsWith("7.") || targetClusterVersion.startsWith("8.");
        boolean editRouting = targetClusterVersion.startsWith("7.") || targetClusterVersion.startsWith("8.");

        Document document = reader.document(0);

        // 构建Document bulk 写数据, 自底向上构建文档信息
        // 1. 构建二级文档信息
        JSONObject childJsonObj = new JSONObject();
        childJsonObj.put("_index", index);
        if (!ignoreId) {
            String id = Lucene840UidUtil.decodeId(document.getField("_id").binaryValue().bytes);
            childJsonObj.put("_id", id);
        }

        // 设置route
        IndexableField route = document.getField("_routing");
        if (editRouting) {
            childJsonObj.put("routing", route == null ? null : route.stringValue());
        } else {
            childJsonObj.put("_routing", route == null ? null : route.stringValue());
        }

        // 设置type
        if (!ignoreType) {
            childJsonObj.put("_type", type == null ? "_doc" : type);
        }

        // 2. 构建一级文档信息, 且把二级文档信息添加入内
        JSONObject parentJsonObj = new JSONObject();
        if (indexMode == null || IndexModeEnum.CREATE.getMode().equals(indexMode)) {
            parentJsonObj.put("create", childJsonObj);
        } else if (IndexModeEnum.INSERT.getMode().equals(indexMode)) {
            parentJsonObj.put("index", childJsonObj);
        } else if (IndexModeEnum.UPDATE.getMode().equals(indexMode)) {
            parentJsonObj.put("index", childJsonObj);
            // 设置版本
            if (!ignoreVersion) {
                childJsonObj.put("version", "1");
                childJsonObj.put("version_type", versionType);
            }
        }

        byte[] sources = document.getField("_source").binaryValue().bytes;
        JSONObject source = (JSONObject) JSON.parse(new String(sources),
                Feature.config(JSON.DEFAULT_PARSER_FEATURE, Feature.UseBigDecimal, false));

        documentBuilder.append(parentJsonObj.toJSONString()).append("\n");
        documentBuilder.append(source).append("\n");
        return documentBuilder.toString();
    }

    private DirectoryReader getDirectoryReader(String shardDataPath) throws ExecutionException {
        return DIRECTORY840_READER_CACHE.get(shardDataPath,
                () -> new SoftDeletesDirectoryReaderWrapper(
                        DirectoryReader.open(FSDirectory.open(Paths.get(shardDataPath))), SOFT_DELETES_FIELD));
    }

    public static void main(String[] args) throws Exception {
        Lucene840DAO lucene840DAO = new Lucene840DAO();
        String shardDataPaths = "/Users/didi/es-package/elasticsearch-7.6.0/data/nodes/0/indices/dxEscVvuSNmiCByFlht9hQ/0/index," +
                "/Users/didi/es-package/elasticsearch-7.6.0/data/nodes/0/indices/dxEscVvuSNmiCByFlht9hQ/1/index," +
                "/Users/didi/es-package/elasticsearch-7.6.0/data/nodes/0/indices/dxEscVvuSNmiCByFlht9hQ/2/index," +
                "/Users/didi/es-package/elasticsearch-7.6.0/data/nodes/0/indices/dxEscVvuSNmiCByFlht9hQ/3/index," +
                "/Users/didi/es-package/elasticsearch-7.6.0/data/nodes/0/indices/dxEscVvuSNmiCByFlht9hQ/4/index";
        long maxNum = 0L;
        long docNum = 0L;
        long deleteNum = 0L;
        long finalDocSize = 0L;
        List<FastDumpDocument> documentInfoList = new ArrayList<>();
        for (String shardDataPath : ListUtils.string2StrList(shardDataPaths)) {
            maxNum += lucene840DAO.getMaxDoc(shardDataPath);

            int[] segmentPreSum = lucene840DAO.getSegmentPreSum(shardDataPath);

            Integer singleDocNum = lucene840DAO.getDocNum(shardDataPath, null);
            docNum += singleDocNum;

            Integer singleMaxDoc = lucene840DAO.getMaxDoc(shardDataPath);

            List<FastDumpDocument> documentInfos = lucene840DAO.getDocumentInfo(shardDataPath, 0, 10, segmentPreSum,
                    "fast-dump-es-7.6.0-7.6.0_2022-09-14",
                    null, false, false);

            FastDumpBulkInfo fastDumpBulkInfo = lucene840DAO.getFastDumpBulkInfo(
                    shardDataPath, 0, 1, segmentPreSum,
                    "fast-dump-es-7.6.0-7.6.0_2022-09-14",
                    null, "sinkerType", false, "version", false, "create", "7.6.0");

            finalDocSize += documentInfos.size();
            documentInfoList.addAll(documentInfos);

            List<FastDumpDocument> deDocumentInfoList = new ArrayList<>();
            for (FastDumpDocument fastDumpDocument : documentInfos) {
                if ("El5pQIMB--ToVP0NjwIq".equals(fastDumpDocument.getId())) {
                    deDocumentInfoList.add(fastDumpDocument);
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
