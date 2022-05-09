package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didiglobal.logi.elasticsearch.client.model.type.ESVersion;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COLD_RACK_PREFER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_CLUSTER_HEALTH;

@Service
@NoArgsConstructor
public class ClusterPhyServiceImpl implements ClusterPhyService {

    private static final ILog        LOGGER = LogFactory.getLog(ClusterPhyServiceImpl.class);

    private static final String CLUSTER_NOT_EXIST = "集群不存在";

    @Value("${es.client.cluster.port}")
    private String                   esClusterClientPort;

    @Autowired
    private ClusterDAO               clusterDAO;

    @Autowired
    private ESClusterService         esClusterService;

    @Autowired
    private ESPluginService          esPluginService;

    @Autowired
    private TemplatePhyService       templatePhyService;

    @Autowired
    private IndexTemplateInfoService indexTemplateInfoService;

    @Autowired
    private ClusterRoleInfoService clusterRoleInfoService;

    @Autowired
    private ClusterRoleHostInfoService clusterRoleHostInfoService;

    private static final String DEFAULT_WRITE_ACTION = "RestBulkAction,RestDeleteAction,RestIndexAction,RestUpdateAction";

    /**
     * 条件查询
     * @param params 条件
     * @return 集群列表
     */
    @Override
    public List<ClusterPhy> listClustersByCondt(ESClusterDTO params) {
        List<ClusterPO> clusterPOs = clusterDAO.listByCondition(ConvertUtil.obj2Obj(params, ClusterPO.class));

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
     * NotExistException
     * 集群不存在
     */
    @Override
    public Result<Boolean> deleteClusterById(Integer clusterId, String operator) {
        ClusterPO clusterPO = clusterDAO.getById(clusterId);
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
     * DuplicateException
     * 集群已经存在(用名字校验)
     * IllegalArgumentException
     * 参数不合理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> createCluster(ESClusterDTO param, String operator) {
        Result<Boolean> checkResult = checkClusterParam(param, OperationEnum.ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=addCluster||msg={}", checkResult.getMessage());
            return checkResult;
        }

        initClusterParam(param);

        ClusterPO clusterPO = ConvertUtil.obj2Obj(param, ClusterPO.class);
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
     * NotExistException
     * 集群不存在
     */
    @Override
    public Result<Boolean> editCluster(ESClusterDTO param, String operator) {
        Result<Boolean> checkResult = checkClusterParam(param, OperationEnum.EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=editCluster||msg={}", checkResult.getMessage());
            return checkResult;
        }

        boolean succ = (1 == clusterDAO.update(ConvertUtil.obj2Obj(param, ClusterPO.class)));
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
        ClusterPO clusterPO = clusterDAO.getByName(clusterName);
        if (null == clusterPO) {
            return null;
        }

        // 转换物理集群对象
        ClusterPhy clusterPhy = ConvertUtil.obj2Obj(clusterPO, ClusterPhy.class);

        // 添加角色、机器信息
        List<ClusterRoleInfo> clusterRoleInfos = clusterRoleInfoService.getAllRoleClusterByClusterId(
                clusterPhy.getId());
        if (CollectionUtils.isNotEmpty(clusterRoleInfos)) {
            // 角色信息
            clusterPhy.setClusterRoleInfos(clusterRoleInfos);

            // 机器信息
            List<ClusterRoleHostInfo> clusterRoleHostInfos = new ArrayList<>();
            Map<Long, List<ClusterRoleHostInfo>> map = clusterRoleHostInfoService.getByRoleClusterIds(clusterRoleInfos.stream().map(ClusterRoleInfo::getId).collect(Collectors.toList()));
            for (ClusterRoleInfo clusterRoleInfo : clusterRoleInfos) {
                List<ClusterRoleHostInfo> esClusterRoleHostInfos = map.getOrDefault(clusterRoleInfo.getId(), new ArrayList<>());
                clusterRoleHostInfos.addAll(esClusterRoleHostInfos);
            }
            clusterPhy.setClusterRoleHostInfos(clusterRoleHostInfos);
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
    public boolean isClusterExistsByList(List<ClusterPhy> list,String clusterName) {
        return list.stream().map(ClusterPhy::getCluster).anyMatch(cluster->cluster.equals(clusterName));
    }
    /**
     * rack是否存在
     * @param cluster 集群名字
     * @param racks   rack名字
     * @return true 存在
     */
    @Override
    public boolean isRacksExists(String cluster, String racks) {
        Set<String> rackSet = getClusterRacks(cluster);
        if (CollectionUtils.isEmpty(rackSet)) {
            LOGGER.warn("class=ESClusterPhyServiceImpl||method=rackExist||cluster={}||msg=can not get rack set!",
                cluster);
            return false;
        }

        for (String r : racks.split(AdminConstant.RACK_COMMA)) {
            if (!rackSet.contains(r)) {
                LOGGER.warn(
                    "class=ESClusterPhyServiceImpl||method=rackExist||cluster={}||rack={}||msg=can not get rack!",
                    cluster, r);
                return false;
            }
        }

        return true;
    }

    /**
     * 获取集群全部的rack
     * @param cluster cluster
     * @return set
     */
    @Override
    public Set<String> getClusterRacks(String cluster) {
        List<ClusterRoleHostInfo> nodes = clusterRoleHostInfoService.getNodesByCluster(cluster);
        if (CollectionUtils.isEmpty(nodes)) {
            return Sets.newHashSet();
        }

        Set<String> rackSet = new HashSet<>();
        // 只有datanode才有rack
        for (ClusterRoleHostInfo clusterRoleHostInfo : nodes) {
            if (ESClusterNodeRoleEnum.DATA_NODE.getCode() == clusterRoleHostInfo.getRole()) {
                rackSet.add(clusterRoleHostInfo.getRack());
            }
        }

        return rackSet;
    }

    @Override
    public Set<String> listHotRacks(String cluster) {
        // 冷存的rack以c开头，排除冷存即为热存
        return getClusterRacks(cluster).stream().filter(rack -> !rack.toLowerCase().startsWith(COLD_RACK_PREFER))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> listColdRacks(String cluster) {
        // 冷存的rack以c开头
        return getClusterRacks(cluster).stream().filter(rack -> rack.toLowerCase().startsWith(COLD_RACK_PREFER))
            .collect(Collectors.toSet());
    }

    /**
     * 根据集群名称解析获取对应的插件列表
     * @param cluster 集群名称
     * @return
     */
    @Override
    public List<Plugin> listClusterPlugins(String cluster) {
        ClusterPO clusterPhy = clusterDAO.getByName(cluster);
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
        ClusterPO clusterPO = clusterDAO.getById(phyClusterId);
        ClusterPhy clusterPhy = ConvertUtil.obj2Obj(clusterPO, ClusterPhy.class);
        return clusterPhy;
    }

    /**
     * 获取写节点的个数
     * @param cluster 集群
     * @return count
     */
    @Override
    public int getWriteClientCount(String cluster) {
        ClusterPO clusterPO = clusterDAO.getByName(cluster);

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
    public boolean ensureDcdrRemoteCluster(String cluster, String remoteCluster) throws ESOperateException {

        ClusterPhy clusterPhy = getClusterByName(cluster);
        if (clusterPhy == null) {
            return false;
        }

        ClusterPhy remoteClusterPhy = getClusterByName(remoteCluster);
        if (remoteClusterPhy == null) {
            return false;
        }

        if (esClusterService.hasSettingExist(cluster,
            String.format(ESOperateContant.REMOTE_CLUSTER_FORMAT, remoteCluster))) {
            return true;
        }

        return esClusterService.syncPutRemoteCluster(cluster, remoteCluster,
            genTcpAddr(remoteClusterPhy.getHttpWriteAddress(), 9300), 3);
    }

    @Override
    public List<ClusterRoleInfo> listPhysicClusterRoles(Integer clusterId) {
        return clusterRoleInfoService.getAllRoleClusterByClusterId(clusterId);
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
        List<ClusterPO> clusterPOS = Lists.newArrayList();
        try {
            clusterPOS = clusterDAO.pagingByCondition(param.getCluster(), param.getHealth(),
                    param.getEsVersion(), (param.getPage() - 1) * param.getSize(), param.getSize(), sortTerm, sortType);
        } catch (Exception e) {
            LOGGER.error("class=ClusterPhyServiceImpl||method=pagingGetClusterPhyByCondition||msg={}", e.getMessage(), e);
        }
        return ConvertUtil.list2List(clusterPOS, ClusterPhy.class);
    }

    @Override
    public Long fuzzyClusterPhyHitByCondition(ClusterPhyConditionDTO param) {
        return clusterDAO.getTotalHitByCondition(ConvertUtil.obj2Obj(param, ClusterPO.class));
    }

    @Override
    public Result<Set<String>> getClusterRackByHttpAddress(String addresses, String password) {
        if (StringUtils.isBlank(addresses)) {
            return Result.buildFail("连接到es集群的地址信息为空");
        }

        return esClusterService.getClusterRackByHttpAddress(addresses, password);
    }

    @Override
    public Float getSurplusDiskSizeOfRacks(String clusterPhyName, String racks, Map<String, Float> allocationInfoOfRack) {
        List<String> rackList = ListUtils.string2StrList(racks);

        //获取region下的rack列表对应的总的磁盘空间
        Float regionDiskSize = 0F;
        for (String rack : rackList) {
            if (allocationInfoOfRack.containsKey(rack)) regionDiskSize += allocationInfoOfRack.get(rack);
        }

        //获取存储在region上的物理模板列表
        Float templateOnRegionDiskSize = 0F;
        List<IndexTemplatePhy> normalTemplateOnPhyCluster = templatePhyService.getNormalTemplateByCluster(clusterPhyName);
        if (CollectionUtils.isEmpty(normalTemplateOnPhyCluster)) {
            return regionDiskSize;
        }

        //获取在region上创建的模板的总的设置空间
        for (IndexTemplatePhy indexTemplatePhy : normalTemplateOnPhyCluster) {
            if (!rackList.containsAll(ListUtils.string2StrList(indexTemplatePhy.getRack()))) {
                continue;
            }

            //根据物理模板获取对应的逻辑模板中的quota参数
            IndexTemplateInfo logicTemplate = indexTemplateInfoService.getLogicTemplateById(indexTemplatePhy.getLogicId());
            if (AriusObjUtils.isNull(logicTemplate)) {
                continue;
            }

            //数据库中quota保存的单位是gb，这里需要统一转化为字节数目
            templateOnRegionDiskSize += Float.valueOf(SizeUtil.getUnitSize(logicTemplate.getQuota() + "gb"));
        }

        //返回在指定region上可以利用的剩余磁盘大小
        return regionDiskSize - templateOnRegionDiskSize;
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


    private Result<Boolean> checkClusterParam(ESClusterDTO param, OperationEnum operation) {
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

    private Result<Boolean> handleEdit(ESClusterDTO param) {
        if (AriusObjUtils.isNull(param.getId())) {
            return Result.buildParamIllegal("集群ID为空");
        }

        ClusterPO oldClusterPO = clusterDAO.getById(param.getId());
        if (oldClusterPO == null) {
            return Result.buildNotExist(CLUSTER_NOT_EXIST);
        }
        return Result.buildSucc();
    }

    private Result<Boolean> handleAdd(ESClusterDTO param) {
        Result<Boolean> isFieldNullResult = isFieldNull(param);
        if (isFieldNullResult.failed()) {
            return isFieldNullResult;
        }

        if (param.getCluster() != null) {
            ClusterPO clusterPO = clusterDAO.getByName(param.getCluster());
            if (clusterPO != null && clusterPO.getId().equals(param.getId())) {
                return Result.buildDuplicate("集群重复");
            }
        }
        return Result.buildSucc();
    }

    private Result<Boolean> isIllegal(ESClusterDTO param) {
        if (param.getDataCenter() != null && !DataCenterEnum.validate(param.getDataCenter())) {
            return Result.buildParamIllegal("数据中心非法");
        }

        if (param.getEsVersion() != null && ESVersion.valueBy(param.getEsVersion()) == null) {
            return Result.buildParamIllegal("es版本号非法");
        }
        return Result.buildSucc();
    }

    private Result<Boolean> isFieldNull(ESClusterDTO param) {
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

    private void initClusterParam(ESClusterDTO param) {
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
