package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.ColdSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.UnavailableTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/17
 */
public interface TemplateSrvManager {

    /**
     * 判断指定逻辑模板是否开启了该模板服务
     * @param logicTemplateId 逻辑模板id
     * @param templateSrvId 模板服务id
     * @return
     */
    boolean isTemplateSrvOpen(Integer logicTemplateId, Integer templateSrvId);

    /**
     * 获取指定模板「开启的」服务
     * @param logicTemplateId 逻辑模板id
     * @return
     */
    Result<List<TemplateSrv>> getTemplateOpenSrv(Integer logicTemplateId);

    /**
     * 获取指定模板「不可用的」服务
     *
     * @param template @return
     */
    List<UnavailableTemplateSrv> getUnavailableSrv(IndexTemplate template);

    /**
     * 分页模糊查询模板服务
     *
     * @param condition
     * @param projectId
     * @return
     */
    PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(TemplateQueryDTO condition, Integer projectId) throws NotFindSubclassException;

    /**
     * 开启模板服务
     *
     * @param srvCode        服务代码
     * @param templateIdList 模板id列表
     * @param operator
     * @param projectId
     * @param data
     * @return
     */
    Result<Void> openSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId,
                         ColdSrvOpenDTO data);

    /**
     * 关闭模板服务
     *
     * @param srvCode        服务代码
     * @param templateIdList 模板id列表
     * @param operator
     * @param projectId
     * @return
     */
    Result<Void> closeSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId);

    List<Integer> getPhyClusterTemplateSrvIds(String clusterPhyName);

    Result<Boolean> replaceTemplateServes(String phyClusterName, List<Integer> clusterTemplateSrvIdList,
                                          String operator);

    /**
     * 清理所有索引服务
     *
     * @param clusterPhy 物理集群名称
     * @param operator   操作人
     * @return {@link Result}<{@link Boolean}>
     */
    Result<Boolean> delAllTemplateSrvByClusterPhy(String clusterPhy, String operator);

    /**
     * 查询开启了某个索引服务的物理集群列表 索引服务不在绑定集群测
     * @param clusterPhies
     * @param srvId
     * @return
     */
    @Deprecated
    List<String> getPhyClusterByOpenTemplateSrv(List<ClusterPhy> clusterPhies, int srvId);

    /**
    * 判断物理集群是否打开了某个索引服务 索引服务不在绑定集群测
    * @param phyCluster        物理集群名称
    * @param srvId
    * @return
    */
    @Deprecated
    boolean isPhyClusterOpenTemplateSrv(String phyCluster, int srvId);

    /**
     * 查询开启了某个索引服务的物理集群列表
     *
     * @param srvId
     * @return
     */
    List<String> getPhyClusterByOpenTemplateSrv(int srvId);
    
    /**
     * 查询开启了某个索引服务的索引模板列表
     *
     * @param srvId
     * @return
     */
    List<String> getIndexTemplateContainsSrv(int srvId);
    
}