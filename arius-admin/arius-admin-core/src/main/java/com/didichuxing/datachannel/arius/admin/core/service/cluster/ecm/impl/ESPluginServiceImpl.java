package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPlugin;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.common.constant.FileCompressionType;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPluginDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.ES_CLUSTER_PLUGINS;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.*;

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
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private FileStorageService fileStorageService;

    private static final String SEPARATOR = "-";


    /**
     * 相同名字的ES插件会有不同的版本，始终是显示最新的版本
     *
     * @return
     */
    @Override
    public List<ESPluginPO> listESPlugin() {
        List<ESPluginPO> allESPlugins = esPluginDAO.listAll();

        if (CollectionUtils.isEmpty(allESPlugins)) {
            return Collections.emptyList();
        }

        Map<String, ESPluginPO> esPluginPOMap = new HashMap<>();
        for (ESPluginPO esPluginPO : allESPlugins) {
            String pluginName = esPluginPO.getName();
            String pluginVersion = esPluginPO.getVersion();

            ESPluginPO esPluginTemp = esPluginPOMap.get(pluginName);
            if (null == esPluginTemp) {
                esPluginPOMap.put(pluginName, esPluginPO);
            } else {
                String versionTemp = esPluginTemp.getVersion();
                if (ESVersionUtil.isHigher(pluginVersion, versionTemp)) {
                    esPluginPOMap.put(pluginName, esPluginPO);
                }
            }
        }

        return new ArrayList<>(esPluginPOMap.values());
    }

    @Override
    public List<ESPluginPO> listClusterAndDefaultESPlugin(String phyClusterId) {
        List<ESPluginPO> esPluginPOS = esPluginDAO.listByPhyClusterId(phyClusterId);
        if (CollectionUtils.isEmpty(esPluginPOS)) {
            return new ArrayList<>();
        }
        return esPluginPOS;
    }

    @Override
    public Result<Long> addESPlugin(ESPluginDTO esPluginDTO) {
        Result<Void> resultCheck = paramCheck(esPluginDTO,ADD,null);
        if (resultCheck.failed()) {
            return Result.buildFrom(resultCheck);
        }

        Result<Void> result = verifyPluginFileAndModifyPluginName(esPluginDTO);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        Result<String> response = Result.buildFail();

        ESPluginPO esPluginPO = ConvertUtil.obj2Obj(esPluginDTO, ESPluginPO.class);
        try {
            response = fileStorageService.upload(getGiftName(esPluginPO), esPluginPO.getMd5(),
                    esPluginDTO.getUploadFile());
        } catch (Exception e) {
            LOGGER.info("class=ESPluginServiceImpl||method=addESPlugin||pluginName={}||msg=fail to upload the file",
                    esPluginPO.getName());
        }
        if (response.success()) {
            esPluginPO.setUrl(response.getData());
        } else {
            return Result.buildFail("上传文件失败");
        }

        boolean succ = false;
        try {
            succ = (1 == esPluginDAO.insert(esPluginPO));
        } catch (Exception e) {
            LOGGER.error("class=ESPluginServiceImpl||method=addESPlugin||pluginName={}||msg=exception",
                    esPluginPO.getName(), e);
        }

        if (!succ) {
            fileStorageService.remove(getGiftName(esPluginPO));
            LOGGER.info("class=ESPluginServiceImpl||method=addESPlugin||pluginName={}||msg=fail to upload the file",
                    esPluginPO.getName());
        }

        return Result.build(succ, esPluginPO.getId());
    }

    /**
     * 校验唯一性的过程就和名称的唯一性结合
     *
     * @param esPluginDTO ES插件
     * @param operator    操作人
     * @return
     */
    @Override
    public Result updateESPluginDesc(ESPluginDTO esPluginDTO, String operator) {
        ESPluginPO oldPlugin = esPluginDAO.getById(esPluginDTO.getId());

        // 检验插件信息
        if (oldPlugin == null) {
            return Result.buildFail("当前插件不存在");
        }
        boolean succ = (1 == esPluginDAO.updateDesc(oldPlugin.getId(), esPluginDTO.getDesc()));

        if (succ) {
            operateRecordService.save(ES_CLUSTER_PLUGINS, EDIT, esPluginDTO.getId(), "", operator);
        }
        return Result.build(succ);
    }

    @Override
    public ESPluginPO getESPluginById(Long id) {
        return esPluginDAO.getById(id);
    }

    @Override
    public Result<Long> deletePluginById(Long id, String operator) {
        ESPluginDTO esPluginDTO = new ESPluginDTO();
        esPluginDTO.setId(id);

        Result<Void> result = paramCheck(esPluginDTO, DELETE, operator);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        ESPluginPO esPluginPO = esPluginDAO.getById(id);
        Result<Void> response = fileStorageService.remove(getGiftName(esPluginPO));
        if (response.failed()) {
            return Result.buildFail("删除文件失败");
        }

        boolean succ = (1 == esPluginDAO.delete(id));
        if (succ) {
            operateRecordService.save(ES_CLUSTER_PLUGINS, DELETE, id, "", operator);
        }
        return Result.build(succ, id);
    }

    @Override
    public String getAllSysDefaultPluginIds() {
        List<ESPluginPO> list = esPluginDAO.getAllSysDefaultPlugins();
        List<Long> idList = list.stream().map(ESPluginPO::getId).collect(Collectors.toList());
        return ListUtils.longList2String(idList);
    }

    @Override
    public List<ESPlugin> getPluginsByClusterName(String clusterName) {
        ClusterPhy clusterPhy = esClusterPhyService.getClusterByName(clusterName);
        if (clusterPhy == null) {
            return Collections.emptyList();
        }

        String plugIds = clusterPhy.getPlugIds();

        List<Long> longIds = ListUtils.string2LongList(plugIds);
        List<ESPluginPO> plugsList = esPluginDAO.listByPlugIds(longIds);

        return ConvertUtil.list2List(plugsList, ESPlugin.class);
    }

    @Override
    public Result<Void> addESPlugins(List<ESPluginDTO> esPluginDTOs) {
        List<String> addFail = Lists.newArrayList();

        for (ESPluginDTO esPluginDTO : esPluginDTOs) {
            Result<Long> result = addESPlugin(esPluginDTO);
            if (result.failed()) {
                addFail.add(esPluginDTO.getFileName());
            }
        }
        if (addFail.isEmpty()) {
            return Result.buildSucc();
        } else {
            return Result.buildFail("上传插件失败，失败的插件名有：" + addFail);
        }
    }

    /**
     * 检验需要进行 delete add updateDsc 传递的插件的参数的基本信息
     *
     * @param esPluginDTO 上传的插件
     * @return
     */
    private Result<Void> paramCheck(ESPluginDTO esPluginDTO, OperationEnum operationEnum, String operator) {
        if(ADD.equals(operationEnum)) {
            Result<Void> result = handleAdd(esPluginDTO);
            if (result.failed()) {
                return result;
            }
        } else if (DELETE.equals(operationEnum)) {
            Result<Void> result = handleDelete(esPluginDTO, operator);
            if (result.failed()) {
                return result;
            }
        }
        return Result.buildSucc();
    }

    private Result<Void> handleDelete(ESPluginDTO esPluginDTO, String operator) {
        if (!ariusUserInfoService.isOPByDomainAccount(operator)) {
            return Result.buildFail("非运维人员不能删除插件");
        }
        Long id = esPluginDTO.getId();
        if (AriusObjUtils.isNull(id)) {
            return Result.buildFail("插件id为空");
        }

        ESPluginPO esPluginPO = getESPluginById(id);
        if (null == esPluginPO) {
            return Result.buildFail("对应的插件不存在");
        }

        List<Long> pluginIds = ListUtils.string2LongList(esClusterPhyService
                .getClusterById(Integer.valueOf(esPluginPO.getPhysicClusterId()))
                .getPlugIds());
        if (pluginIds.contains(id)) {
            return Result.buildFail("该插件已安装，请先卸载");
        }
        return Result.buildSucc();
    }

    private Result<Void> handleAdd(ESPluginDTO esPluginDTO) {
        if (AriusObjUtils.isNull(esPluginDTO)) {
            return Result.buildFail("插件为空");
        }

        if (esPluginDTO.getPhysicClusterId() == null) {
            return Result.buildFail("物理集群id为空");
        }

        if (null == esClusterPhyService.getClusterById(Integer.valueOf(esPluginDTO.getPhysicClusterId()))) {
            return Result.buildFail("物理集群id不存在");
        }

        if (esPluginDTO.getUploadFile() == null) {
            return Result.buildFail("文件不存在");
        }
        return Result.buildSucc();
    }

    /**
     * 校验某个插件文件的合法性
     *
     * @param esPluginDTO 插件
     * @return
     */
    private Result<Void> verifyPluginFileAndModifyPluginName(ESPluginDTO esPluginDTO) {
        MultipartFile pluginFile = esPluginDTO.getUploadFile();
        if (esPluginDTO.getUploadFile().getSize() > MULTI_PART_FILE_SIZE_MAX) {
            return Result.buildFail("插件[" + esPluginDTO.getName() + "]文件的大小超过限制，不能超过" + MULTI_PART_FILE_SIZE_MAX / 1024 / 1024 + "M");
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
        esPluginDTO.setName(name);

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
            return Result.buildFail("插件[" + esPluginDTO.getName() + "]的版本号不符合规则，必须是'1.1.1.1'类似的格式");
        }
        // 插件的版本格式是什么样的
        esPluginDTO.setVersion(version);

        // 校验 elasticsearch.version
        final String esVersion = propsMap.get("elasticsearch.version");
        if (esVersion == null) {
            return Result.buildFail("property [version] is missing for plugin [" + name + "]");
        }
        // 获取当前部署集群的es版本
        String phyClusterName = esClusterPhyService.getClusterById(Integer.valueOf(esPluginDTO.getPhysicClusterId())).getCluster();
        String esVersionByESClient = esClusterService.synGetESVersionByCluster(phyClusterName);

        if (esVersionByESClient == null) {
            return Result.buildFail("无法从物理集群获取版本");
        }
        if (!esVersionByESClient.equals(esVersion)) {
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
     *
     * @param pluginFile 插件压缩包
     * @return 插件配置信息
     * @throws IOException
     */
    private Map<String, String> readFromProperties(MultipartFile pluginFile) {
        InputStream bis = null;
        String fileName = pluginFile.getOriginalFilename();
        if(null == fileName) {
            LOGGER.warn("class=ESPluginServiceImpl||method=readFromProperties||errmsg = MultipartFile.getOriginalFilename() is null ");
            return null;
        }
        Properties props = new Properties();

        try {
            if (fileName.endsWith(FileCompressionType.TAR) || fileName.endsWith(FileCompressionType.TAR_GZ)) {
                bis = CommonUtils.unTar(pluginFile.getInputStream(), "plugin-descriptor.properties");
            } else if (fileName.endsWith(FileCompressionType.ZIP)) {
                // 解析有问题
                bis = CommonUtils.unZip(pluginFile.getInputStream(), "plugin-descriptor.properties");
            } else {
                throw new IllegalStateException("插件文件类型不合法！");
            }

            if (bis == null) {
                // 说明压缩包中不存在 plugin-descriptor.properties 文件
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

    private String getGiftName(ESPluginPO esPluginPO) {
        return esPluginPO.getName() + SEPARATOR + esPluginPO.getVersion() + SEPARATOR + esPluginPO.getPhysicClusterId() + FileCompressionType.TAR_GZ;
    }
}
