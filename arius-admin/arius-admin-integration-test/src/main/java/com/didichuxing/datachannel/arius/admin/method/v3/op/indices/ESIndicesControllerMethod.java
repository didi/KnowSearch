package com.didichuxing.datachannel.arius.admin.method.v3.op.indices;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesOpenOrCloseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * @author chengxiang
 */
public class ESIndicesControllerMethod {
    public static final String ESIndices = V3 +  "/indices";

    public static PaginationResult<IndexCatCellVO> pageGetIndexCatInfoVO(IndicesConditionDTO indicesConditionDTO) throws IOException {
        String path = String.format("%s/page", ESIndices);
        return JSON.parseObject(AriusClient.post(path, indicesConditionDTO), new TypeReference<PaginationResult<IndexCatCellVO>>(){});
    }

    public static PaginationResult<IndexCatCellVO> getIndexCatInfoVO(String clusterPhyName, String indexName) throws IOException {
        String path = String.format("%s/select", ESIndices);
        Map<String, Object> params = new HashMap<>();
        params.put("clusterPhyName", clusterPhyName);
        params.put("indexName", indexName);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<PaginationResult<IndexCatCellVO>>(){});
    }

    public static Result<Boolean> delete(List<IndicesClearDTO> params) throws IOException{
        String path = String.format("%s", ESIndices);
        return JSON.parseObject(AriusClient.delete(path, null, params), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> close(List<IndicesOpenOrCloseDTO> params) throws IOException {
        String path = String.format("%s/close", ESIndices);
        return JSON.parseObject(AriusClient.put(path, params), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> open(List<IndicesOpenOrCloseDTO> params) throws IOException {
        String path = String.format("%s/open", ESIndices);
        return JSON.parseObject(AriusClient.put(path, params), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> editIndexBlockSetting(List<IndicesBlockSettingDTO> params) throws IOException {
        String path = String.format("%s/block", ESIndices);
        return JSON.parseObject(AriusClient.put(path, params), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<List<IndexShardInfoVO>> getIndexShard(String clusterPhyName, String indexName) throws IOException{
        String path = String.format("%s/%s/%s/shard", ESIndices, clusterPhyName, indexName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<IndexShardInfoVO>>>(){});
    }

    public static Result<IndexMappingVO> mapping(String clusterPhyName, String indexName) throws IOException {
        String path = String.format("%s/%s/%s/mapping", ESIndices, clusterPhyName, indexName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<IndexMappingVO>>(){});
    }

    public static Result<IndexSettingVO> setting(String clusterPhyName, String indexName) throws IOException {
        String path = String.format("%s/%s/%s/setting", ESIndices, clusterPhyName, indexName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<IndexSettingVO>>(){});
    }

}
