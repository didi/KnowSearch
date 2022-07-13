package com.didichuxing.datachannel.arius.admin.biz.zeus;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils.isBlank;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESZeusHostInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;

/**
 * @author linyunan
 * @date 2021-09-14
 */
@Component
public class ZeusCollectManagerImpl implements ZeusCollectManager {

    @Autowired
    private ClusterPhyManager          clusterPhyManager;

    @Autowired
    private ClusterPhyService          clusterPhyService;
    /**
     * Map<clusterPhyName, clientCount>
     */
    private final Map<String, Integer> clusterPhyName2ClientCountMap = new ConcurrentHashMap<>();

    @Override
    public synchronized Result<Boolean> updateHttpAddressFromZeus(ESZeusHostInfoDTO esZeusHostInfoDTO) {

        if (null == esZeusHostInfoDTO) {
            return Result.buildParamIllegal("入参为空");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(esZeusHostInfoDTO.getClusterPhyName());
        if (null == clusterPhy) {
            return Result.buildFail("物理集群不存在");
        }

        if (DATA_NODE.getDesc().equals(esZeusHostInfoDTO.getRole())) {
            return Result.buildSucc();
        }

        ClusterPhyDTO clusterDTO = new ClusterPhyDTO();
        clusterDTO.setId(clusterPhy.getId());

        //如果集群存在client, 用client代替master作为集群读写访问入口
        if (CLIENT_NODE.getDesc().equals(esZeusHostInfoDTO.getRole())) {
            //清空master httpAddress
            if (null == clusterPhyName2ClientCountMap.get(esZeusHostInfoDTO.getClusterPhyName())) {
                clusterDTO.setHttpAddress("");
                clusterDTO.setHttpWriteAddress("");
                Result<Boolean> result = clusterPhyService.editCluster(clusterDTO, null);
                if (result.success()) {
                    //client标识位 + 1
                    clusterPhyName2ClientCountMapAddOne(esZeusHostInfoDTO.getClusterPhyName());

                    //刷新最新物理集群配置
                    clusterPhy = clusterPhyService.getClusterByName(esZeusHostInfoDTO.getClusterPhyName());
                }
            }

            if (clusterPhyName2ClientCountMap.get(esZeusHostInfoDTO.getClusterPhyName()) >= 0) {
                buildESClusterDTOFromZeus(clusterDTO, clusterPhy, esZeusHostInfoDTO);
                clusterPhyManager.editCluster(clusterDTO, null);
                clusterPhyName2ClientCountMapAddOne(esZeusHostInfoDTO.getClusterPhyName());
            }
        }

        if (MASTER_NODE.getDesc().equals(esZeusHostInfoDTO.getRole())
            && null == clusterPhyName2ClientCountMap.get(esZeusHostInfoDTO.getClusterPhyName())) {
            buildESClusterDTOFromZeus(clusterDTO, clusterPhy, esZeusHostInfoDTO);
            clusterPhyManager.editCluster(clusterDTO, null);
        }
        return Result.buildSucc();
    }

    private void buildESClusterDTOFromZeus(ClusterPhyDTO clusterDTO, ClusterPhy clusterPhy,
                                           ESZeusHostInfoDTO esZeusHostInfoDTO) {
        String httpAddress = clusterPhy.getHttpAddress();
        List<String> httpAddressList = ListUtils.string2StrList(httpAddress);
        if (httpAddressList.contains(esZeusHostInfoDTO.getHttpAddress())) {
            return;
        }

        StringBuilder httpAddressStr = new StringBuilder();
        if (isBlank(httpAddress) && !isBlank(esZeusHostInfoDTO.getHttpAddress())) {
            httpAddressStr.append(esZeusHostInfoDTO.getHttpAddress());
        } else if (!isBlank(httpAddress) && !isBlank(esZeusHostInfoDTO.getHttpAddress())) {
            httpAddressStr.append(httpAddress).append(",").append(esZeusHostInfoDTO.getHttpAddress());
        }

        clusterDTO.setHttpAddress(httpAddressStr.toString());
        clusterDTO.setHttpWriteAddress(httpAddressStr.toString());
    }

    private void clusterPhyName2ClientCountMapAddOne(String clusterPhyName) {
        if (null == clusterPhyName) {
            return;
        }
        if (clusterPhyName2ClientCountMap.containsKey(clusterPhyName)) {
            clusterPhyName2ClientCountMap.put(clusterPhyName, clusterPhyName2ClientCountMap.get(clusterPhyName) + 1);
        } else {
            clusterPhyName2ClientCountMap.put(clusterPhyName, 0);
        }
    }
}
