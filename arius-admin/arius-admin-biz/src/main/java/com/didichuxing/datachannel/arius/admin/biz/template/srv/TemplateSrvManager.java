package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.ColdSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.UnavailableTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatusWithTemplateEnum;
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
     * @param template        @return
     * @param materClusterPhy
     */
    List<UnavailableTemplateSrv> getUnavailableSrvByTemplateAndMasterPhy(IndexTemplate template, String materClusterPhy);

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
    
   
    /**
     * 返回主集群连接的状态
     *
     * @param clusterPhy 集群的名称。
     * @return 主集群连接状态。
     */
    ClusterConnectionStatusWithTemplateEnum getMasterClusterConnectionStatus(String clusterPhy);
}