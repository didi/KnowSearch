package com.didichuxing.datachannel.arius.admin.method.v3.white;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESZeusHostInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.indices.IndicesConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm.ESConfigVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_WHITE_PART;

/**
 * @author wuxuan
 * @Date 2022/3/28
 */
public class ESPhyClusterWhiteControllerMethodTest {

    public static final String CLUSTER_WHITE = V3_WHITE_PART + "/phy/cluster";

    public static Result<Void> deleteClusterJoin(Long clusterId) throws IOException{
        String path = String.format("%s/%d/deleteClusterJoin", CLUSTER_WHITE, clusterId);
        return JSON.parseObject(AriusClient.delete(path), new TypeReference<Result<Void>>(){});
    }

    public static Result<Boolean> updateHttpAddress(ESZeusHostInfoDTO esZeusHostInfoDTO) throws IOException{
        String path=String.format("%s/updateHttpAddress",CLUSTER_WHITE);
        return JSON.parseObject(AriusClient.post(path,esZeusHostInfoDTO),new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> checkClusterHealth(String clusterPhyName) throws IOException{
        String path=String.format("%s/%s/checkHealth",CLUSTER_WHITE,clusterPhyName);
        return JSON.parseObject(AriusClient.get(path),new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> checkClusterIsExit(String clusterPhyName) throws IOException{
        String path=String.format("%s/%s/isExit",CLUSTER_WHITE,clusterPhyName);
        return JSON.parseObject(AriusClient.get(path),new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> deleteClusterExit(String clusterPhyName) throws IOException{
        String path=String.format("%s/%s/del",CLUSTER_WHITE,clusterPhyName);
        return JSON.parseObject(AriusClient.delete(path),new TypeReference<Result<Boolean>>(){});
    }
}
