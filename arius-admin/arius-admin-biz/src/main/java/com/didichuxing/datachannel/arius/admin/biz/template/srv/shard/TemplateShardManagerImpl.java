package com.didichuxing.datachannel.arius.admin.biz.template.srv.shard;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.BYTE_TO_G;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.G_PER_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_SHARD;

/**
 * 索引shard服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateShardManagerImpl extends BaseTemplateSrv implements TemplateShardManager {

    @Autowired
    private ESIndexService           esIndexService;

    @Autowired
    private IndexTemplatePhysicalDAO indexTemplatePhysicalDAO;

    @Autowired
    private TemplatePhyManager templatePhyManager;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_SHARD;
    }

    /**
     * 调整集群shard个数
     *
     * @param phyClusterName    集群
     * @param retryCount 重试次数
     * @return true/false
     */
    @Override
    public boolean adjustShardCount(String phyClusterName, int retryCount) {
        if (!isTemplateSrvOpen(phyClusterName)) {
            return false;
        }

        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getNormalTemplateByCluster(phyClusterName);

        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                Result result = adjustShardCount(templatePhysical.getId(), retryCount);
                if (result.failed()) {
                    LOGGER.warn("method=adjustShardCount||template={}||result={}", templatePhysical.getName(), result);
                }
            } catch (Exception e) {
                LOGGER.warn("method=adjustShardCount||template={}||errMsg=", templatePhysical.getName(), e.getMessage(),
                    e);
            }
        }

        return true;
    }

    /**
     * shard个数调整
     *
     * @param physicalId physicalId
     * @param retryCount retryCount
     * @return result
     */
    @Override
    public Result adjustShardCount(Long physicalId, int retryCount) throws ESOperateException {
        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(physicalId);

        if (templatePhysical == null) {
            return Result.buildNotExist("模板不存在");
        }

        if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
            return Result.buildFail(templatePhysical.getCluster() + "没有开启" + templateServiceName());
        }

        int shardCount = calcuShardCount(physicalId);
        if (shardCount < 1) {
            return Result.buildFail("计算shard个数失败");
        }

        if (templatePhysical.getShard() < shardCount) {
            Result result = updateTemplateShardNumIfGreater(physicalId, shardCount, retryCount);
            if (result.success()) {
                LOGGER.info("method=adjustShardCount||template={}||shardCount={}->{}", templatePhysical.getName(),
                    templatePhysical.getShard(), shardCount);
            }
        }

        return Result.buildSucc();
    }

    /**
     * 计算shard个数
     *
     * @param physical
     * @return
     */
    @Override
    public int calcuShardCount(Long physical) {
        IndexTemplatePhy templatePhysical = templatePhyService.getTemplateById(physical);

        if (!isTemplateSrvOpen(templatePhysical.getCluster())) {
            return -1;
        }

        Map<String, IndexNodes> indices = esIndexService.syncGetIndexByExpression(templatePhysical.getCluster(),
            templatePhysical.getExpression());
        if (indices == null) {
            return -1;
        }

        long sizeInBytesMax = 0;
        for (IndexNodes indexNode : indices.values()) {
            long indexSizeInBytes = indexNode.getPrimaries().getStore().getSizeInBytes();
            if (indexSizeInBytes > sizeInBytesMax) {
                sizeInBytesMax = indexSizeInBytes;
            }
        }

        return (int) (sizeInBytesMax * BYTE_TO_G / G_PER_SHARD) + 1;

    }

    @Override
    public Result updateTemplateShardNumIfGreater(Long physicalId, Integer shardNum,
                                                  int retryCount) throws ESOperateException {
        TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getById(physicalId);
        if (physicalPO == null) {
            return Result.buildNotExist("模板不存在");
        }

        if (!isTemplateSrvOpen(physicalPO.getCluster())) {
            return Result.buildFail(physicalPO.getCluster() + "没有开启" + templateServiceName());
        }

        if (physicalPO.getShard() >= shardNum) {
            return Result.buildSucc("无需处理");
        }

        IndexTemplatePhysicalDTO updateParam = new IndexTemplatePhysicalDTO();
        updateParam.setId(physicalId);
        updateParam.setShard(shardNum);
        return templatePhyManager.editTemplateWithoutCheck(updateParam, AriusUser.CAPACITY_PLAN.getDesc(),
            retryCount);
    }

    @Override
    public void initShardRoutingAndAdjustShard(IndexTemplatePhysicalDTO param) {
        int shard = param.getShard();
        int srcShard = shard;
        if (shard >= 320) {
            param.setShardRouting(32);
            param.setShard(calcuShardByShardRouting(shard, 32));
        } else if (shard >= 80) {
            param.setShardRouting(16);
            param.setShard(calcuShardByShardRouting(shard, 16));
        } else if (shard >= 16) {
            param.setShardRouting(8);
            param.setShard(calcuShardByShardRouting(shard, 8));
        } else if (shard >= 4) {
            param.setShardRouting(4);
            param.setShard(calcuShardByShardRouting(shard, 4));
        } else {
            param.setShardRouting(1);
        }

        LOGGER.info("method=initShardRoutingAndAdjustShard||name={}||srcShard={}||shard={}||shardRouting={}", srcShard,
            param.getName(), param.getShard(), param.getShardRouting());
    }

    /**************************************** private method ****************************************************/
    private Integer calcuShardByShardRouting(int shard, int shardRouting) {
        if (shard % shardRouting == 0) {
            return shard;
        }
        return (shard / shardRouting + 1) * shardRouting;
    }
}
