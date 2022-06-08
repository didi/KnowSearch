package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_CLUSTER_HEALTH;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPhyPO;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.PhyClusterDAO;
import com.didiglobal.logi.elasticsearch.client.model.type.ESVersion;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.NoArgsConstructor;

/**
 * @author didi
 */
@Service
@NoArgsConstructor
public class ClusterPhyServiceImpl implements ClusterPhyService {

    private static final ILog        LOGGER = LogFactory.getLog(ClusterPhyServiceImpl.class);

    private static final String CLUSTER_NOT_EXIST = "集群不存在";

    @Value("${es.client.cluster.port}")
    private String                   esClusterClientPort;

    @Autowired
    private PhyClusterDAO clusterDAO;

    @Autowired
    private ESClusterService         esClusterService;

    @Autowired
    private ESPluginService          esPluginService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ClusterRoleService clusterRoleService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    private static final String DEFAULT_WRITE_ACTION = "RestBulkAction,RestDeleteAction,RestIndexAction,RestUpdateAction";

    /**
     * 条件查询
     * @param params 条件
     * @return 集群列表
     */
    @Override
    public List<ClusterPhy> listClustersByCondt(ClusterPhyDTO params) {
        List<ClusterPhyPO> clusterPOs = clusterDAO.listByCondition(ConvertUtil.obj2Obj(params, ClusterPhyPO.class));

        if (CollectionUtils.isEmpty(clusterPOs)) {
            return Lists.newArrayList();
        }

        return ConvertUtil.list2List(clusterPOs, ClusterPhy.class);
    }

    /**
     * 删除集群
     *
     * @param clusterId 集群id
     * @param operator  操作人
     * @return 成功 true 失败 false
     * <p>
     * 集群不存在
     */
    @Override
    public Result<Boolean> deleteClusterById(Integer clusterId, String operator) {
        ClusterPhyPO clusterPO = clusterDAO.getById(clusterId);
        if (clusterPO == null) {
            return Result.buildNotExist(CLUSTER_NOT_EXIST);
        }
        
        return Result.buildBoolen(clusterDAO.delete(clusterId) == 1);
    }

    /**
     * 新建集群
     * @param param    集群信息
     * @param operator 操作人
     * @return 成功 true 失败 false
     * <p>
     * 参数不合理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> createCluster(ClusterPhyDTO param, String operator) {
        Result<Boolean> checkResult = checkClusterParam(param, OperationEnum.ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=addCluster||msg={}", checkResult.getMessage());
            return checkResult;
        }

        initClusterParam(param);

        ClusterPhyPO clusterPO = ConvertUtil.obj2Obj(param, ClusterPhyPO.class);
        boolean succ = (1 == clusterDAO.insert(clusterPO));
        if (succ) {
            param.setId(clusterPO.getId());
        }
        return Result.buildBoolen(succ);
    }

    /**
     * 编辑集群
     * @param param    集群信息
     * @param operator 操作人
     * @return 成功 true 失败 false
     * <p>
     * IllegalArgumentException
     * 参数不合理
     * 集群不存在
     */
    @Override
    public Result<Boolean> editCluster(ClusterPhyDTO param, String operator) {
        Result<Boolean> checkResult = checkClusterParam(param, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=editCluster||msg={}", checkResult.getMessage());
            return checkResult;
        }

        boolean succ = (1 == clusterDAO.update(ConvertUtil.obj2Obj(param, ClusterPhyPO.class)));
        return Result.buildBoolen(succ);
    }

    /**
     * 根据集群名字查询集群
     * @param clusterName 集群名字
     * @return 集群
     */
    @Override
    public ClusterPhy getClusterByName(String clusterName) {
        // 获取物理集群
        ClusterPhyPO clusterPO = clusterDAO.getByName(clusterName);
        if (null == clusterPO) {
            return null;
        }

        // 转换物理集群对象
        ClusterPhy clusterPhy = ConvertUtil.obj2Obj(clusterPO, ClusterPhy.class);

        // 添加角色、机器信息
        List<ClusterRoleInfo> clusterRoleInfos = clusterRoleService.getAllRoleClusterByClusterId(
                clusterPhy.getId());
        if (CollectionUtils.isNotEmpty(clusterRoleInfos)) {
            // 角色信息
            clusterPhy.setClusterRoleInfos(clusterRoleInfos);

            // 机器信息
            List<ClusterRoleHost> clusterRoleHosts = new ArrayList<>();
            Map<Long, List<ClusterRoleHost>> map = clusterRoleHostService.getByRoleClusterIds(clusterRoleInfos.stream().map(ClusterRoleInfo::getId).collect(Collectors.toList()));
            for (ClusterRoleInfo clusterRoleInfo : clusterRoleInfos) {
                List<ClusterRoleHost> esClusterRoleHosts = map.getOrDefault(clusterRoleInfo.getId(), new ArrayList<>());
                clusterRoleHosts.addAll(esClusterRoleHosts);
            }
            clusterPhy.setClusterRoleHosts(clusterRoleHosts);
        }

        return clusterPhy;
    }

    @Override
    public Result<Void> updatePluginIdsById(String pluginIds, Integer phyClusterId) {
        boolean succ = (1 == clusterDAO.updatePluginIdsById(pluginIds, phyClusterId));
        return Result.build(succ);
    }

    @Override
    public List<ClusterPhy> listAllClusters() {
        return ConvertUtil.list2List(clusterDAO.listAll(), ClusterPhy.class);
    }

    @Override
    public List<String> listAllClusterNameList() {
        List<String> clusterNameList = Lists.newArrayList();
        try {
            clusterNameList.addAll(clusterDAO.listAllName());
        } catch (Exception e) {
            LOGGER.error("class=ESClusterPhyServiceImpl||method=listAllClusterNameList||errMsg={}",e.getMessage(), e);
        }
        return clusterNameList;
    }

    @Override
    public List<ClusterPhy> listClustersByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return new ArrayList<>();
        }
        return ConvertUtil.list2List(clusterDAO.listByNames(names), ClusterPhy.class);
    }

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */
    @Override
    public boolean isClusterExists(String clusterName) {
        return clusterDAO.getByName(clusterName) != null;
    }

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */
    @Override
    public boolean isClusterExistsByList(List<ClusterPhy> list, String clusterName) {
        return list.stream().map(ClusterPhy::getCluster).anyMatch(cluster->cluster.equals(clusterName));
    }

    /**
     * 根据集群名称解析获取对应的插件列表
     * @param cluster 集群名称
     * @return
     */
    @Override
    public List<Plugin> listClusterPlugins(String cluster) {
        ClusterPhyPO clusterPhy = clusterDAO.getByName(cluster);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return new ArrayList<>();
        }

        List<Plugin> pluginList = ConvertUtil.list2List(esPluginService.listClusterAndDefaultESPlugin(clusterPhy.getId().toString()), Plugin.class);

        // 将从插件列表获得的所有的插件(系统默认以及自定义)安装状态设置为FALSE
        Map<Long, Plugin> pluginMap = new HashMap<>(0);
        for (Plugin esPlugin : pluginList) {
            esPlugin.setInstalled(Boolean.FALSE);
            pluginMap.put(esPlugin.getId(), esPlugin);
        }

        // 获取集群对应的已安装的插件列表，将对应的已安装的插件的安装状态设置为TRUE
        List<Long> pluginIds = parsePluginIds(clusterPhy.getPlugIds());
        for (Long pluginId : pluginIds) {
            Plugin phyPlugin = pluginMap.get(pluginId);
            if (AriusObjUtils.isNull(phyPlugin)) {
                continue;
            }
            phyPlugin.setInstalled(true);
        }

        return new ArrayList<>(pluginMap.values());
    }

    /**
     * 查询指定集群
     * @param phyClusterId 集群id
     * @return 集群  不存在返回null
     */
    @Override
    public ClusterPhy getClusterById(Integer phyClusterId) {
        ClusterPhyPO clusterPO = clusterDAO.getById(phyClusterId);
        return ConvertUtil.obj2Obj(clusterPO, ClusterPhy.class);
    }

    /**
     * 获取写节点的个数
     * @param cluster 集群
     * @return count
     */
    @Override
    public int getWriteClientCount(String cluster) {
        ClusterPhyPO clusterPO = clusterDAO.getByName(cluster);

        if (StringUtils.isBlank(clusterPO.getHttpWriteAddress())) {
            return 1;
        }

        return clusterPO.getHttpWriteAddress().split(",").length;
    }

    /**
     * 确保集群配置了DCDR的远端集群地址，如果没有配置尝试配置
     * @param cluster       集群
     * @param remoteCluster 远端集群
     * @return
     */
    @Override
    public boolean ensureDCDRRemoteCluster(String cluster, String remoteCluster) throws ESOperateException {

        ClusterPhy clusterPhy = getClusterByName(cluster);
        if (clusterPhy == null) {
            return false;
        }

        ClusterPhy remoteClusterPhy = getClusterByName(remoteCluster);
        if (remoteClusterPhy == null) {
            return false;
        }

        if (esClusterService.hasSettingExist(cluster,
            String.format(ESOperateConstant.REMOTE_CLUSTER_FORMAT, remoteCluster))) {
            return true;
        }

        return esClusterService.syncPutRemoteCluster(cluster, remoteCluster,
            genTcpAddr(remoteClusterPhy.getHttpWriteAddress(), 9300), 3);
    }

    @Override
    public Result<Boolean> updatePhyClusterDynamicConfig(ClusterSettingDTO param) {
        Result<ClusterDynamicConfigsEnum> result = checkClusterDynamicType(param);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        Map<String, Object> persistentConfig = Maps.newHashMap();
        persistentConfig.put(param.getKey(), param.getValue());
        return Result.buildBoolen(esClusterService.syncPutPersistentConfig(param.getClusterName(), persistentConfig));
    }

    @Override
    public Set<String> getRoutingAllocationAwarenessAttributes(String cluster) {
        if(!isClusterExists(cluster)) {
            return Sets.newHashSet();
        }

        return esClusterService.syncGetAllNodesAttributes(cluster);
    }

    @Override
    public List<ClusterPhy> pagingGetClusterPhyByCondition(ClusterPhyConditionDTO param) {
        String sortTerm = null == param.getSortTerm() ? SortConstant.ID : param.getSortTerm();
        String sortType = param.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        param.setSortTerm(sortTerm);
        param.setSortType(sortType);
        param.setFrom((param.getPage() - 1) * param.getSize());
        List<ClusterPhyPO> clusters = Lists.newArrayList();
        try {
            clusters = clusterDAO.pagingByCondition(param);
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyServiceImpl||method=pagingGetClusterPhyByCondition||msg={}", e.getMessage(), e);
        }
        return ConvertUtil.list2List(clusters, ClusterPhy.class);
    }

    @Override
    public Long fuzzyClusterPhyHitByCondition(ClusterPhyConditionDTO param) {
        return clusterDAO.getTotalHitByCondition(param);
    }

    /**
     * 安装包是否绑定集群
     * @param packageId 安装包名
     * @return true or false
     */
    @Override
    public boolean isClusterExistsByPackageId(Long packageId) {
        return clusterDAO.getTotalHitByPackageId(packageId) > 0;
    }

    /**************************************** private method ***************************************************/
    private List<String> genTcpAddr(String httpAddress, int tcpPort) {
        try {
            String[] httpAddrArr = httpAddress.split(",");
            List<String> result = Lists.newArrayList();
            for (String httpAddr : httpAddrArr) {
                result.add(httpAddr.split(":")[0] + ":" + tcpPort);
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("method=genTcpAddr||httpAddress={}||errMsg={}", httpAddress, e.getMessage(), e);
        }

        return Lists.newArrayList();
    }

    private Result<ClusterDynamicConfigsEnum> checkClusterDynamicType(ClusterSettingDTO param) {
        if(!isClusterExists(param.getClusterName())) {
            return Result.buildFail(CLUSTER_NOT_EXIST);
        }

        ClusterDynamicConfigsEnum clusterSettingEnum = ClusterDynamicConfigsEnum.valueCodeOfName(param.getKey());
        if(clusterSettingEnum.equals(ClusterDynamicConfigsEnum.UNKNOWN)) {
            return Result.buildFail("传入的字段类型未知");
        }

        if (!clusterSettingEnum.getCheckFun().apply(String.valueOf(param.getValue())).booleanValue()) {
            return Result.buildFail("传入的字段参数格式有误");
        }

        if (clusterSettingEnum == ClusterDynamicConfigsEnum.CLUSTER_ROUTING_ALLOCATION_AWARENESS_ATTRIBUTES
                && !getRoutingAllocationAwarenessAttributes(param.getClusterName())
                .containsAll((JSONArray) JSON.toJSON(param.getValue()))) {
            return Result.buildFail("传入的attributes字段参数有误");
        }
        return Result.buildSucc();
    }


    private Result<Boolean> checkClusterParam(ClusterPhyDTO param, OperationEnum operation) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("集群信息为空");
        }

        if (OperationEnum.ADD.equals(operation)) {
            Result<Boolean> result = handleAdd(param);
            if (result.failed()) {
                return result;
            }
        } else if (OperationEnum.EDIT.equals(operation)) {
            Result<Boolean> result = handleEdit(param);
            if (result.failed()) {
                return result;
            }
        }

        Result<Boolean> isIllegalResult = isIllegal(param);
        if (isIllegalResult.failed()) {
            return isIllegalResult;
        }

        return Result.buildSucc();
    }

    private Result<Boolean> handleEdit(ClusterPhyDTO param) {
        if (AriusObjUtils.isNull(param.getId())) {
            return Result.buildParamIllegal("集群ID为空");
        }

        ClusterPhyPO oldClusterPO = clusterDAO.getById(param.getId());
        if (oldClusterPO == null) {
            return Result.buildNotExist(CLUSTER_NOT_EXIST);
        }
        return Result.buildSucc();
    }

    private Result<Boolean> handleAdd(ClusterPhyDTO param) {
        Result<Boolean> isFieldNullResult = isFieldNull(param);
        if (isFieldNullResult.failed()) {
            return isFieldNullResult;
        }

        if (param.getCluster() != null) {
            ClusterPhyPO clusterPO = clusterDAO.getByName(param.getCluster());
            if (clusterPO != null && clusterPO.getId().equals(param.getId())) {
                return Result.buildDuplicate("集群重复");
            }
        }
        return Result.buildSucc();
    }

    private Result<Boolean> isIllegal(ClusterPhyDTO param) {
        if (param.getDataCenter() != null && !DataCenterEnum.validate(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心非法");
        }

        if (param.getEsVersion() != null && ESVersion.valueBy(param.getEsVersion()) == null) {
            return Result.buildParamIllegal("es版本号非法");
        }
        return Result.buildSucc();
    }

    private Result<Boolean> isFieldNull(ClusterPhyDTO param) {
        if (AriusObjUtils.isNull(param.getCluster())) {
            return Result.buildParamIllegal("集群名称为空");
        }
        if (AriusObjUtils.isNull(param.getHttpAddress())) {
            return Result.buildParamIllegal("集群HTTP地址为空");
        }
        if (AriusObjUtils.isNull(param.getType())) {
            return Result.buildParamIllegal("集群类型为空");
        }
        if (AriusObjUtils.isNull(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心为空");
        }
        if (AriusObjUtils.isNull(param.getIdc())) {
            return Result.buildParamIllegal("机房信息为空");
        }
        if (AriusObjUtils.isNull(param.getEsVersion())) {
            return Result.buildParamIllegal("es版本为空");
        }
        return Result.buildSucc();
    }

    private void initClusterParam(ClusterPhyDTO param) {
        if (param.getWriteAddress() == null) {
            param.setWriteAddress("");
        }

        if (param.getReadAddress() == null) {
            param.setReadAddress("");
        }

        if (param.getHttpWriteAddress() == null) {
            param.setHttpWriteAddress("");
        }

        if (param.getPassword() == null) {
            param.setPassword("");
        }

        if(param.getImageName() == null) {
            param.setImageName("");
        }

        if(param.getLevel() == null) {
            param.setLevel(1);
        }

        if(param.getCreator() == null) {
            param.setCreator("");
        }

        if(param.getNsTree() == null) {
            param.setNsTree("");
        }

        if(param.getDesc() == null) {
            param.setDesc("");
        }

        if (param.getWriteAction() == null) {
            param.setWriteAction(DEFAULT_WRITE_ACTION);
        }

        if (param.getTemplateSrvs() == null){
            param.setTemplateSrvs(TemplateServiceEnum.getDefaultSrvs());
        }

        if (null == param.getHealth()) {
            param.setHealth(DEFAULT_CLUSTER_HEALTH);
        }

        if(null == param.getActiveShardNum()) {
            param.setActiveShardNum(0L);
        }

        if (null == param.getDiskTotal()) {
            param.setDiskTotal(0L);
        }

        if (null == param.getDiskUsage()) {
            param.setDiskUsage(0L);
        }

        if (null == param.getDiskUsagePercent()) {
            param.setDiskUsagePercent(0D);
        }

        if (null == param.getPlatformType()) {
            param.setPlatformType("");
        }
        if (null == param.getResourceType()) {
            param.setResourceType(ClusterResourceTypeEnum.UNKNOWN.getCode());
        }
        if (null == param.getGatewayUrl()) {
            param.setGatewayUrl("");
        }
    }

    /**
     * 解析插件ID列表
     *
     * @param pluginIdsStr 插件ID格式化字符串
     * @return
     */
    private List<Long> parsePluginIds(String pluginIdsStr) {
        List<Long> pluginIds = new ArrayList<>();
        if (StringUtils.isNotBlank(pluginIdsStr)) {
            String[] arr = StringUtils.split(pluginIdsStr, ",");
            for (int i = 0; i < arr.length; ++i) {
                pluginIds.add(Long.parseLong(arr[i]));
            }
        }
        return pluginIds;
    }
}