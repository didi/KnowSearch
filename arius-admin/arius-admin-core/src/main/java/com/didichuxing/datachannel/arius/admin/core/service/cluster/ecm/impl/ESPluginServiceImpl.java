package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.DELETE;

import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Plugin;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.FileCompressionType;
import com.didichuxing.datachannel.arius.admin.common.constant.PluginTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPluginDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.UserService;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * ES插件包管理 服务实现类
 *
 * @since 2020-08-24
 */
@Service
public class ESPluginServiceImpl implements ESPluginService {

    private static final ILog LOGGER = LogFactory.getLog(ESPluginServiceImpl.class);

    @Autowired
    private ESPluginDAO esPluginDAO;

    private static final Long MULTI_PART_FILE_SIZE_MAX = 1024 * 1024 * 100L;

    @Autowired
    private UserService userService;

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private FileStorageService fileStorageService;

    private static final String SEPARATOR = "-";
    @Autowired
    private RoleTool roleTool;


    /**
     * 相同名字的ES插件会有不同的版本，始终是显示最新的版本
     * @return 插件类型列表
     */
    @Override
    public List<PluginPO> listESPlugin() {
        // 获取全部的插件列表
        List<PluginPO> allESPlugins = esPluginDAO.listAll();

        if (CollectionUtils.isEmpty(allESPlugins)) {
            return Collections.emptyList();
        }

        // 展示更高版本的插件列表
        Map<String, PluginPO> esPluginPOMap = new HashMap<>();
        for (PluginPO pluginPO : allESPlugins) {
            String pluginName = pluginPO.getName();
            String pluginVersion = pluginPO.getVersion();

            PluginPO esPluginTemp = esPluginPOMap.get(pluginName);
            if (null == esPluginTemp) {
                esPluginPOMap.put(pluginName, pluginPO);
            } else {
                String versionTemp = esPluginTemp.getVersion();
                if (ESVersionUtil.isHigher(pluginVersion, versionTemp)) {
                    esPluginPOMap.put(pluginName, pluginPO);
                }
            }
        }

        return new ArrayList<>(esPluginPOMap.values());
    }

    @Override
    public List<PluginPO> listClusterAndDefaultESPlugin(String phyClusterId) {
        List<PluginPO> pluginPOS = esPluginDAO.listByPhyClusterId(phyClusterId);
        if (CollectionUtils.isEmpty(pluginPOS)) {
            return new ArrayList<>();
        }
        return pluginPOS;
    }

    @Override
    public Result<Long> addESPlugin(PluginDTO pluginDTO) {
        // 1.对插件信息的参数进行校验
        Result<Void> resultCheck = paramCheck(pluginDTO, ADD, null);
        if (resultCheck.failed()) {
            return Result.buildFrom(resultCheck);
        }

        // 2.将对应的插件上传至配置的文件仓库
        Result<String> response = Result.buildFail();
        try {
            response = fileStorageService.upload(getGiftName(ConvertUtil.obj2Obj(pluginDTO, Plugin.class)), pluginDTO.getMd5(),
                    pluginDTO.getUploadFile());
        } catch (Exception e) {
            LOGGER.info("class=ESPluginServiceImpl||method=addESPlugin||pluginName={}||msg=fail to upload the file",
                    pluginDTO.getName());
        }

        // 3.上传成功后返回url，回写到插件信息对象中
        if (response.success()) {
            pluginDTO.setUrl(response.getData());
        } else {
            return Result.buildFail("上传文件失败");
        }

        // 4.插件信息入DB
        boolean succ = false;
        PluginPO pluginPO = ConvertUtil.obj2Obj(pluginDTO, PluginPO.class);
        try {
            succ = (1 == esPluginDAO.insert(pluginPO));
        } catch (Exception e) {
            LOGGER.error("class=ESPluginServiceImpl||method=addESPlugin||pluginName={}||msg=exception",
                    pluginDTO.getName(), e);
        }

        // 5.插件信息入库失败，删除已经上传至文件仓库的文件
        if (!succ) {
            fileStorageService.remove(getGiftName(ConvertUtil.obj2Obj(pluginPO, Plugin.class)));
            LOGGER.info("class=ESPluginServiceImpl||method=addESPlugin||pluginName={}||msg=fail to upload the file",
                    pluginDTO.getName());
        }

        return Result.build(succ, pluginPO.getId());
    }

    /**
     * 校验唯一性的过程就和名称的唯一性结合
     * @param pluginDTO ES插件
     * @param operator  操作人
     * @return
     */
    @Override
    public Result<Void> updateESPluginDesc(PluginDTO pluginDTO, String operator) {
        PluginPO oldPlugin = esPluginDAO.getById(pluginDTO.getId());

        // 检验插件信息
        if (oldPlugin == null) {
            return Result.buildFail("当前插件不存在");
        }
        boolean succ = (1 == esPluginDAO.updateDesc(oldPlugin.getId(), pluginDTO.getDesc()));

        if (succ) {
            PluginPO newPluginPo = esPluginDAO.getById(oldPlugin.getId());
            PluginVO oldPluginVO = ConvertUtil.obj2Obj(oldPlugin, PluginVO.class);
            PluginVO newPluginVO = ConvertUtil.obj2Obj(newPluginPo, PluginVO.class);
            Map</*apiModelPropertyValue*/String,/*修改后的apiModelPropertyValue*/String> apiModelPropertyValueModify=
                    Maps.newHashMap();
            apiModelPropertyValueModify.put("上传插件类型: 0 系统默认插件, 1 ES能力插件, 2 平台能力插件","上传插件类型");
            operateRecordService.save(new OperateRecord.Builder().bizId(pluginDTO.getId()).userOperation(operator)
                    .operationTypeEnum(OperateTypeEnum.ES_CLUSTER_PLUGINS_EDIT)
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                    .content(AriusObjUtils.findChangedWithClearByBeanVo(oldPluginVO, newPluginVO,apiModelPropertyValueModify)).build()
                    
                    
            );
        }
        return Result.build(succ);
    }
    @Override
    public PluginPO getESPluginById(Long id) {
        return esPluginDAO.getById(id);
    }

    @Override
    public Result<Long> deletePluginById(Long id, String operator) {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setId(id);

        // 插件删除操作的参数校验
        Result<Void> result = paramCheck(pluginDTO, DELETE, operator);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        // 文件系统删除插件
        PluginPO pluginPO = esPluginDAO.getById(id);
        Result<Void> response = fileStorageService.remove(getGiftName(ConvertUtil.obj2Obj(pluginPO, Plugin.class)));
        if (response.failed()) {
            return Result.buildFail("删除文件失败");
        }

        // 删除DB插件信息
        boolean succ = (1 == esPluginDAO.delete(id));
        if (succ) {
            operateRecordService.save(new OperateRecord.Builder()
                            .bizId(pluginDTO.getId())
                            .userOperation(operator)
                            .operationTypeEnum(OperateTypeEnum.ES_CLUSTER_PLUGINS_DELETE)
                            .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                            .build()
                    );
        }

        return Result.build(succ, id);
    }

    @Override
    public String getAllSysDefaultPluginIds() {
        List<PluginPO> list = esPluginDAO.getAllSysDefaultPlugins();
        List<Long> idList = list.stream().map(PluginPO::getId).collect(Collectors.toList());
        return ListUtils.longList2String(idList);
    }

    @Override
    public List<Plugin> getPluginsByClusterName(String clusterName) {
        ClusterPhy clusterPhy = esClusterPhyService.getClusterByName(clusterName);
        if (clusterPhy == null) {
            return Collections.emptyList();
        }

        String plugIds = clusterPhy.getPlugIds();

        List<Long> longIds = ListUtils.string2LongList(plugIds);
        List<PluginPO> plugsList = esPluginDAO.listByPlugIds(longIds);

        return ConvertUtil.list2List(plugsList, Plugin.class);
    }

    @Override
    public Result<String> addESPlugins(List<PluginDTO> pluginDTOS) {
        List<String> addFail = Lists.newArrayList();

        // 分批次上传插件，并记录上传失败的插件名称
        for (PluginDTO pluginDTO : pluginDTOS) {
            Result<Long> result = addESPlugin(pluginDTO);
            if (result.failed()) {
                addFail.add(pluginDTO.getFileName());
            }
        }

        return Result.build(addFail.isEmpty(), "上传插件失败,失败的插件名有：" + addFail);
    }

    /**
     * 检验需要进行 delete add updateDsc 传递的插件的参数的基本信息
     * @param pluginDTO 上传的插件
     * @return 校验结果
     */
    private Result<Void> paramCheck(PluginDTO pluginDTO, OperationEnum operationEnum, String operator) {
        if (ADD.equals(operationEnum)) {
            Result<Void> result = handleAdd(pluginDTO);
            if (result.failed()) {
                return result;
            }
        } else if (DELETE.equals(operationEnum)) {
            Result<Void> result = handleDelete(pluginDTO, operator);
            if (result.failed()) {
                return result;
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> handleDelete(PluginDTO pluginDTO, String operator) {
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("非运维人员不能删除插件");
        }

        Long id = pluginDTO.getId();
        if (null == id) {
            return Result.buildFail("插件id为空");
        }

        PluginPO pluginPO = getESPluginById(id);
        if (null == pluginPO) {
            return Result.buildFail("对应的插件不存在");
        }

        // 校验集群是否已经安装了指定的插件，如已安装，则首先需要卸载插件
        List<Long> pluginIds = ListUtils.string2LongList(esClusterPhyService
                .getClusterById(Integer.valueOf(pluginPO.getPhysicClusterId()))
                .getPlugIds());
        if (pluginIds.contains(id)) {
            return Result.buildFail("该插件已安装，请先卸载");
        }

        return Result.buildSucc();
    }

    private Result<Void> handleAdd(PluginDTO pluginDTO) {
        if (AriusObjUtils.isNull(pluginDTO)) {
            return Result.buildFail("插件为空");
        }

        if (pluginDTO.getPhysicClusterId() == null) {
            return Result.buildFail("物理集群id为空");
        }

        if (null == esClusterPhyService.getClusterById(Integer.valueOf(pluginDTO.getPhysicClusterId()))) {
            return Result.buildFail("物理集群id不存在");
        }

        if (pluginDTO.getUploadFile() == null) {
            return Result.buildFail("文件不存在");
        }

        // 根据插件的类型进行对应的插件文件校验
        switch (PluginTypeEnum.valueOf(pluginDTO.getPDefault())) {
            case ES_PLUGIN:
                return verifyESPluginFileAndModifyPluginName(pluginDTO);
            case ADMIN_PLUGIN:
                return verifyAdminPluginFileAndModifyPluginVersion(pluginDTO);
            case DEFAULT_PLUGIN:
                return Result.buildFail("不允许上传系统默认插件");
            default:
                return Result.buildFail("上传插件类型未知");
        }
    }

    /**
     * 对于平台能力的插件进行内容的校验
     * @param pluginDTO 插件
     * @return 校验结果
     */
    private Result<Void> verifyAdminPluginFileAndModifyPluginVersion(PluginDTO pluginDTO) {
        if(null == pluginDTO.getVersion()) {
            pluginDTO.setVersion(AdminConstant.DEFAULT_PLUGIN_VERSION);
        }

        return Result.buildSucc();
    }

    /**
     * 校验某个ES能力插件文件的合法性
     * @param pluginDTO 插件
     * @return 校验结果
     */
    private Result<Void> verifyESPluginFileAndModifyPluginName(PluginDTO pluginDTO) {
        MultipartFile pluginFile = pluginDTO.getUploadFile();
        if (pluginDTO.getUploadFile().getSize() > MULTI_PART_FILE_SIZE_MAX) {
            return Result.buildFail("插件[" + pluginDTO.getName() + "]文件的大小超过限制，不能超过" + MULTI_PART_FILE_SIZE_MAX / 1024 / 1024 + "M");
        }
        // 读取插件配置信息
        Map<String, String> propsMap = readFromProperties(pluginFile);

        // 校验 plugin-descriptor.properties 文件是否存在
        if (MapUtils.isEmpty(propsMap)) {
            return Result.buildFail("插件压缩包中没有 plugin-descriptor.properties 文件！");
        }

        // 校验 name
        final String name = propsMap.remove("name");
        if (name == null || name.isEmpty()) {
            return Result.buildFail("property [name] is missing in [" + pluginFile.getName() + "]");
        }
        pluginDTO.setName(name);

        // 校验 description
        final String description = propsMap.remove("description");
        if (description == null) {
            return Result.buildFail("property [description] is missing for plugin [" + name + "]");
        }

        // 校验 version
        final String version = propsMap.remove("version");
        if (version == null) {
            return Result.buildFail("property [version] is missing for plugin [" + name + "]");
        }
        if (!ESVersionUtil.isValid(version)) {
            return Result.buildFail("插件[" + pluginDTO.getName() + "]的版本号不符合规则，必须是'1.1.1.1'类似的格式");
        }
        // 插件的版本格式是什么样的
        pluginDTO.setVersion(version);

        // 校验 elasticsearch.version
        final String esVersion = propsMap.get("elasticsearch.version");
        if (esVersion == null) {
            return Result.buildFail("property [version] is missing for plugin [" + name + "]");
        }

        // 获取当前部署集群的es版本
        ClusterPhy clusterPhy = esClusterPhyService.getClusterById(Integer.valueOf(pluginDTO.getPhysicClusterId()));
        if(AriusObjUtils.isNull(clusterPhy) || AriusObjUtils.isNull(clusterPhy.getCluster())) {
            return Result.buildFail("插件上传的物理集群不存在");
        }

        // 从es集群获取实时生效的版本号
        String esVersionFromESClient = esClusterService.synGetESVersionByCluster(clusterPhy.getCluster());
        if (esVersionFromESClient == null) {
            return Result.buildFail("无法从物理集群获取版本");
        }

        // 这里兼容内部4位版本
        if (!esVersion.startsWith(esVersionFromESClient)) {
            return Result.buildFail("插件适配的es版本和当前运行的es集群版本号不匹配");
        }

        // 校验 classname
        final String classname = propsMap.remove("classname");
        if (classname == null) {
            return Result.buildFail("property [classname] is missing for plugin [" + name + "]");
        }

        return Result.buildSucc();
    }

    /**
     * 从插件中的 plugin-descriptor.properties 文件中读取插件配置信息
     * @param pluginFile 插件压缩包
     * @return 插件配置信息
     */
    private Map<String, String> readFromProperties(MultipartFile pluginFile) {
        InputStream bis = null;
        String fileName = pluginFile.getOriginalFilename();
        if (null == fileName) {
            LOGGER.warn("class=ESPluginServiceImpl||method=readFromProperties||errmsg = MultipartFile.getOriginalFilename() is null ");
            return null;
        }
        Properties props = new Properties();

        try {
            if (fileName.endsWith(FileCompressionType.TAR) || fileName.endsWith(FileCompressionType.TAR_GZ)) {
                bis = CommonUtils.unTar(pluginFile.getInputStream(), "plugin-descriptor.properties");
            } else if (fileName.endsWith(FileCompressionType.ZIP)) {
                bis = CommonUtils.unZip(pluginFile.getInputStream(), "plugin-descriptor.properties");
            } else {
                throw new IllegalStateException("插件文件类型不合法！");
            }

            // 说明压缩包中不存在 plugin-descriptor.properties 文件
            if (bis == null) {
                return null;
            }

            // 将插件的 plugin-descriptor.properties 文件加载到 Properties 对象中
            props.load(bis);
        } catch (Exception e) {
            LOGGER.info("class=ESPluginServiceImpl||method=readFromProperties||exception={}" + "msg = 读取 plugin-descriptor.properties 文件失败", e);
            return null;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    LOGGER.info("class=ESPluginServiceImpl||method=readFromProperties||exception={}" + "msg = 读取 plugin-descriptor.properties 文件失败", e);
                }
            }
        }

        return props.stringPropertyNames().stream().collect(Collectors.toMap(Function.identity(), props::getProperty));
    }

    /**
     * 重构上传至文件仓库系统的名称，作为文件存储的唯一键
     * @param plugin 插件信息
     * @return 唯一区别的名称
     */
    private String getGiftName(Plugin plugin) {
        return plugin.getName() + SEPARATOR + plugin.getVersion()
                + SEPARATOR + plugin.getPhysicClusterId() + SEPARATOR + plugin.getPDefault() + FileCompressionType.TAR_GZ;
    }
}