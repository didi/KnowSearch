package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ESPlugin;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPluginDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.elasticsearch.Version;
import org.elasticsearch.bootstrap.JarHell;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.ES_CLUSTER_PLUGINS;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;

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

    private static final String DEFAULT_PLUGIN_VERSION = "1.1.1.1000";

    private static final Long MULTI_PART_FILE_SIZE_MAX = 1024 * 1024 * 10L;

    private static final Integer MULTI_PART_FILE_COUNT_MAX = 5;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private ESClusterPhyService esClusterPhyService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public List<ESPluginPO> listESPlugin() {
        List<ESPluginPO> allESPlugins = esPluginDAO.listAll();

        if (CollectionUtils.isEmpty(allESPlugins)) {
            return allESPlugins;
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
    public Result<ESPluginPO> addESPlugin(ESPluginDTO esPluginDTO) {
        //本期先不实现插件的版本，但版本字段先保留
        esPluginDTO.setVersion(DEFAULT_PLUGIN_VERSION);

        List<ESPluginPO> pluginPOs = esPluginDAO.getByNameAndVersion(esPluginDTO.getName(), esPluginDTO.getVersion());

        if (!CollectionUtils.isEmpty(pluginPOs)) {
            return Result.buildFail("插件名称和版本不能重复");
        }

        if (!ESVersionUtil.isValid(esPluginDTO.getVersion())) {
            return Result.buildFail("插件版本号不符合规则，必须是'1.1.1.1'类似的格式");
        }

        if (esPluginDTO.getUploadFile() == null) {
            return Result.buildFail("文件不存在");
        }

        Result response = Result.buildFail();
        try {
            response = fileStorageService.upload(esPluginDTO.getFileName(), esPluginDTO.getMd5(),
                    esPluginDTO.getUploadFile(), null);
            if (response.failed()) {
                return response;
            }
        } catch (Exception e) {
            LOGGER.info("class=ESPluginServiceImpl||method=addESPlugin||pluginName={}||msg=fail to upload the file",
                    esPluginDTO.getName());
        }

        ESPluginPO esPluginPO = ConvertUtil.obj2Obj(esPluginDTO, ESPluginPO.class);
        if (response.success()) {
            esPluginPO.setUrl(esPluginDTO.getFileName());
        } else {
            return Result.buildFail("上传文件失败");
        }

        boolean succ = (1 == esPluginDAO.insert(esPluginPO));
        return Result.build(succ, esPluginPO);
    }

    @Override
    public Result updateESPluginDesc(ESPluginDTO esPluginDTO, String operator) {

        ESPluginPO newPlugin = ConvertUtil.obj2Obj(esPluginDTO, ESPluginPO.class);
        ESPluginPO oldPlugin = esPluginDAO.getById(esPluginDTO.getId());

        // 检验插件信息
        Result checkResult = checkPluginWhenUpdate(newPlugin, oldPlugin);
        if (checkResult.failed()) {
            return checkResult;
        }

        boolean succ = (1 == esPluginDAO.update(newPlugin));

        if (succ) {
            // 操作记录
            operateRecordService.save(ES_CLUSTER_PLUGINS, EDIT, esPluginDTO.getId(), "", operator);
        }
        return Result.build(succ, esPluginDTO);
    }

    @Override
    public ESPluginPO getESPluginById(Long id) {
        return esPluginDAO.getById(id);
    }

    @Override
    public Result<Long> deletePluginById(Long id, String operator) {
        if (!ariusUserInfoService.isOPByDomainAccount(operator)) {
            return Result.buildFail("非运维人员不能删除插件");
        }

        boolean succ = (1 == esPluginDAO.delete(id));

        if (succ) {
            // 操作记录
            operateRecordService.save(ES_CLUSTER_PLUGINS, DELETE, id, "", operator);
        }
        return Result.build(succ, id);
    }

    @Override
    public List<ESPluginPO> listPluginBelongClus(List<String> pluginIds, String pDefault) {
        return esPluginDAO.listPluginBelongClus(pluginIds, pDefault);
    }

    @Override
    public String getAllSysDefaultPlugins() {
        return esPluginDAO.getAllSysDefaultPlugins();
    }

    @Override
    public List<ESPluginPO> listClusterAndDefaultESPlugin(String phyClusterId) {
        List<ESPluginPO> esPluginPOS = esPluginDAO.listbyPhyClusterId(phyClusterId);

        if (CollectionUtils.isEmpty(esPluginPOS)) {
            return new ArrayList<>();
        }
        return esPluginPOS;
    }

    @Override
    public List<ESPlugin> getPluginsByClusterName(String clusterName) {
        ESClusterPhy clusterPhy = esClusterPhyService.getClusterByName(clusterName);
        String plugIds = clusterPhy.getPlugIds();

        if (StringUtils.isEmpty(plugIds)) {
            plugIds = getAllSysDefaultPlugins();
        }
        List<Long> longIds = ListUtils.string2LongList(plugIds);
        List<ESPluginPO> plugsList = esPluginDAO.listByPlugIds(longIds);

        return ConvertUtil.list2List(plugsList, ESPlugin.class);
    }

    @Override
    public Result addESPlugins(List<ESPluginDTO> esPlugins) {
        // 1. 对每个插件的 信息 进行校验
        Result checkPluginsResult = checkPlugins(esPlugins);
        if (checkPluginsResult.failed()) {
            return checkPluginsResult;
        }

        // 2. 对插件 文件 进行有效性和兼容性校验
        Result verifyPluginsResult = verifyPlugins(esPlugins);
        if (verifyPluginsResult.failed()) {
            return verifyPluginsResult;
        }

        // TODO lyn 对插件文件中的jar包进行校验


        // 3. 将插件文件上传到 s3 上
        Result uploadPluginResult = uploadPlugin(esPlugins);
        if (uploadPluginResult.failed()) {
            return uploadPluginResult;
        }

        // 4. 将插件信息存入数据库
        Result savePluginInfoResult = savePluginInfo(esPlugins);
        if (savePluginInfoResult.failed()) {
            return savePluginInfoResult;
        }

        return Result.build(Boolean.TRUE);
    }

    @Override
    // todo 改名
    public Result installESPlugin(Long id) {
        ESPluginPO esPluginPO = esPluginDAO.getById(id);
        if (esPluginPO.getInstallFlag()) {
            return Result.buildFail("该插件已安装");
        }
        if (esPluginDAO.installESPlugin(id) == 1) {
            return Result.buildSucc();
        }
        return Result.buildFail("插件安装失败");
    }

    @Override
    // todo 改名
    public Result uninstallESPlugin(Long id) {
        ESPluginPO esPluginPO = esPluginDAO.getById(id);
        if (!esPluginPO.getInstallFlag()) {
            return Result.buildFail("该插件未安装");
        }
        if (esPluginDAO.uninstallESPlugin(id) == 1) {
            return Result.buildSucc();
        }
        return Result.buildFail("插件卸载失败");
    }

    /**
     * 检查插件是否有效
     *
     * @param esPlugins 上传的插件
     * @return
     */
    private Result checkPlugins(List<ESPluginDTO> esPlugins) {
        if (esPlugins.size() > MULTI_PART_FILE_COUNT_MAX) {
            return Result.buildFail("上传插件的数量超过限制，不能超过" + MULTI_PART_FILE_COUNT_MAX + "个");
        }

        for (ESPluginDTO esPluginDTO : esPlugins) {
            //本期先不实现插件的版本，但版本字段先保留
            esPluginDTO.setVersion(DEFAULT_PLUGIN_VERSION);

            // todo bug:数据库中的插件名称是 plugin-descriptor.properties 文件中的name值(eg:analysis-ik)，
            //  而这里的 esPluginDTO.getName() 是用户上传的插件包的文件名(eg:elasticsearch-analysis-ik-7.6.1)，
            //  无法校验插件名是否重复
            List<ESPluginPO> pluginPos = esPluginDAO.getByNameAndVersionAndPhysicClusterId(esPluginDTO.getName(),
                    esPluginDTO.getVersion(), esPluginDTO.getPhysicClusterId());

            if (!CollectionUtils.isEmpty(pluginPos)) {
                // todo 需要给出明确的唯一的插件信息(eg:analysis-ik)，而不是用户上传的文件名
                return Result.buildFail("插件[" + esPluginDTO.getName() + "]的名称和版本重复");
            }

            if (!ESVersionUtil.isValid(esPluginDTO.getVersion())) {
                return Result.buildFail("插件[" + esPluginDTO.getName() + "]的版本号不符合规则，必须是'1.1.1.1'类似的格式");
            }

            if (esPluginDTO.getUploadFile() == null) {
                return Result.buildFail("插件[" + esPluginDTO.getName() + "]文件不存在");
            }

            if (esPluginDTO.getUploadFile().getSize() > MULTI_PART_FILE_SIZE_MAX) {
                return Result.buildFail(
                        "插件[" + esPluginDTO.getName() + "]文件的大小超过限制，不能超过" + MULTI_PART_FILE_SIZE_MAX / 1024 + "M");
            }
        }
        return Result.buildSucc();
    }

    /**
     * 将插件上传到 S3 上
     *
     * @param esPlugins 待上传插件列表
     * @return
     */
    private Result uploadPlugin(List<ESPluginDTO> esPlugins) {
        for (ESPluginDTO esPlugin : esPlugins) {
            Result isUploadSuccess;
            try {
                // 将插件文件上传到文件系统
                isUploadSuccess = fileStorageService.upload(esPlugin.getFileName(), esPlugin.getMd5(),
                        esPlugin.getUploadFile(), null);

                if (isUploadSuccess.failed()) {
                    return Result.buildFail("插件[" + esPlugin.getName() + "]上传到文件系统失败");
                }
            } catch (Exception e) {
                LOGGER.info("class=ESPluginServiceImpl||method=addESPlugin||msg=fail to upload the file||pluginName={}",
                        esPlugin.getName());
                return Result.buildFail("插件[" + esPlugin.getName() + "]上传到文件系统失败");
            }
        }
        return Result.buildSucc();
    }

    /**
     * 将插件信息保存至数据库
     *
     * @param esPlugins 待保存的插件列表
     * @return
     */
    private Result savePluginInfo(List<ESPluginDTO> esPlugins) {
        ArrayList<ESPluginPO> esPluginPOs = new ArrayList<>();
        for (ESPluginDTO esPlugin : esPlugins) {
            ESPluginPO esPluginPo = ConvertUtil.obj2Obj(esPlugin, ESPluginPO.class);
            // todo 干掉（表中和PO中） S3url 属性，直接用 url 表示仓库地址
            esPluginPo.setUrl(esPlugin.getFileName());
            esPluginPo.setS3url(fileStorageService.getDownloadBaseUrl(null) + "/" + esPlugin.getFileName());
            esPluginPOs.add(esPluginPo);
        }
        try {
            esPluginDAO.insertBatch(esPluginPOs);
        } catch (Exception e) {
            return Result.buildFail("插件信息上传到数据库失败");
        }
        return Result.buildSucc();
    }

    /**
     * 更新插件信息时检验插件参数
     *
     * @param newPlugin 新插件信息
     * @param oldPlugin 原插件信息
     * @return
     */
    private Result checkPluginWhenUpdate(ESPluginPO newPlugin, ESPluginPO oldPlugin) {
        if (oldPlugin == null) {
            return Result.buildFail("当前插件不存在");
        }
        if (newPlugin.getDesc() == null) {
            return Result.buildFail("插件描述不能为 null");
        }

        Field[] fields = ESPluginPO.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if ("serialVersionUID".equals(fieldName) || "id".equals(fieldName) || "desc".equals(fieldName)) {
                continue;
            }
            try {
                PropertyDescriptor oldPd = new PropertyDescriptor(fieldName, oldPlugin.getClass());
                Method getMethod4OldPlugin = oldPd.getReadMethod();

                PropertyDescriptor newPd = new PropertyDescriptor(fieldName, newPlugin.getClass());
                Method getMethod4NewPlugin = newPd.getReadMethod();

                if (getMethod4NewPlugin.invoke(newPlugin) != null
                        && !getMethod4NewPlugin.invoke(newPlugin).equals(getMethod4OldPlugin.invoke(oldPlugin))) {
                    return Result.buildFail("插件信息字段[" + fieldName + "]不能被修改");
                }
            } catch (Exception e) {
                return Result.buildFail("编辑插件信息失败");
            }
        }
        return Result.buildSucc();
    }

    /**
     * 校验插件的合法性
     *
     * @param esPlugins 插件列表
     * @return
     */
    private Result verifyPlugins(List<ESPluginDTO> esPlugins) {
        for (ESPluginDTO esPlugin : esPlugins) {
            Result result = verifyPluginFileAndModifyPluginName(esPlugin);
            if (result.failed()) {
                return result;
            }
        }
        return Result.buildSucc();
    }

    /**
     * 校验某个插件文件的合法性
     *
     * @param esPlugin 插件
     * @return
     */
    private Result verifyPluginFileAndModifyPluginName(ESPluginDTO esPlugin) {
        MultipartFile pluginFile = esPlugin.getUploadFile();
        // 读取插件配置信息
        Map<String, String> propsMap = readFromProperties(pluginFile);

        // 校验 plugin-descriptor.properties 文件是否存在
        if (propsMap == null) {
            return Result.buildFail("插件压缩包中没有 plugin-descriptor.properties 文件！");
        }

        // 校验 name
        final String name = propsMap.remove("name");
        if (name == null || name.isEmpty()) {
            return Result.buildFail("property [name] is missing in [" + pluginFile.getName() + "]");
        }
        // todo 1. 是否在这里改插件名，有待商榷
        esPlugin.setName(name);

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

        // 校验 es.version
        final String esVersionString = propsMap.remove("elasticsearch.version");
        if (esVersionString == null) {
            return Result.buildFail("property [elasticsearch.version] is missing for plugin [" + name + "]");
        }
        final Version esVersion = Version.fromString(esVersionString);
        ESClusterPhy cluster = esClusterPhyService.getClusterById(Integer.parseInt(esPlugin.getPhysicClusterId()));
        String clusterEsVersion = cluster.getEsVersion();
        String currentESVersionString = clusterEsVersion.substring(0, clusterEsVersion.lastIndexOf("."));

        final Version currentESVersion = Version.fromString(currentESVersionString);
        if (!esVersion.equals(currentESVersion)) {
            return Result.buildFail("Plugin [" + name + "] was built for Elasticsearch version "
                    + esVersionString + " but version " + currentESVersion + " is running");
        }

        // 校验 java.version
        final String javaVersionString = propsMap.remove("java.version");
        if (javaVersionString == null) {
            return Result.buildFail("property [java.version] is missing for plugin [" + name + "]");
        }
        JarHell.checkVersionFormat(javaVersionString);
        Result checkJavaVersionResult = checkJavaVersion(javaVersionString);
        if (checkJavaVersionResult.failed()) {
            return checkJavaVersionResult;
        }

        // 校验 classname
        final String classname = propsMap.remove("classname");
        if (classname == null) {
            return Result.buildFail("property [classname] is missing for plugin [" + name + "]");
        }

        // todo 2. 以下校验内容是直接从es源码里搞来的，暂时没有用到
        // 读取 extended.plugins 信息
        final String extendedString = propsMap.remove("extended.plugins");
        final List<String> extendedPlugins;
        if (extendedString == null) {
            extendedPlugins = Collections.emptyList();
        } else {
            extendedPlugins = Arrays.asList(Strings.delimitedListToStringArray(extendedString, ","));
        }

        // 读取 has.native.controller 信息
        final String hasNativeControllerValue = propsMap.remove("has.native.controller");
        final boolean hasNativeController;
        if (hasNativeControllerValue == null) {
            hasNativeController = false;
        } else {
            switch (hasNativeControllerValue) {
                case "true":
                    hasNativeController = true;
                    break;
                case "false":
                    hasNativeController = false;
                    break;
                default:
                    final String message = String.format(
                            Locale.ROOT,
                            "property [%s] must be [%s], [%s], or unspecified but was [%s]",
                            "has_native_controller",
                            "true",
                            "false",
                            hasNativeControllerValue);
                    return Result.buildFail(message);
            }
        }

//        if (esVersion.after(Version.fromString("6_0_0_beta2")) && esVersion.before(Version.fromString("6_3_0"))) {
//            propsMap.remove("requires.keystore");
//        }

        // TODO 3. 干掉
        if (!propsMap.isEmpty()) {
            return Result.buildFail("Unknown properties in plugin descriptor: " + propsMap.keySet());
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
        String fileName = pluginFile.getName();
        Properties props = new Properties();

        try {
            if (fileName.endsWith(".tar")) {
                bis = CommonUtils.unTar(pluginFile.getInputStream(), "plugin-descriptor.properties");
            } else if (fileName.endsWith(".zip")) {
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
            // todo: 打印日志格式不对
            LOGGER.error("读取 plugin-descriptor.properties 文件失败", e);
            return null;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    // todo 没有意义，改为打印日志
                    e.printStackTrace();
                }
            }
        }

        return props.stringPropertyNames().stream().collect(Collectors.toMap(Function.identity(), props::getProperty));
    }

    // TODO LYN 检验jdk版本

    /**
     * 校验插件要求的jdk版本事情与es集群的jdk版本号是否兼容
     *
     * @param javaVersionString 插件要求的jdk版本号
     * @return 校验结果
     */
    private Result checkJavaVersion(String javaVersionString) {
        return Result.buildSucc();
    }
}
