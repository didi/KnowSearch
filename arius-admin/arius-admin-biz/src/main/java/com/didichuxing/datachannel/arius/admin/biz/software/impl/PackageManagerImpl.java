package com.didichuxing.datachannel.arius.admin.biz.software.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.PACKAGE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.page.PackagePageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.software.PackageManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.software.PackageUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.software.*;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.FileCompressionType;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.software.SoftwarePackageTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.application.PackageService;
import com.didiglobal.logi.op.manager.domain.packages.entity.Package;
import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;

@Component
public class PackageManagerImpl implements PackageManager {
    private static final ILog LOGGER = LogFactory.getLog(PackageManagerImpl.class);
    private static final Long MULTI_PART_FILE_SIZE_MAX = 1024 * 1024 * 500L;
    @Autowired
    private HandleFactory handleFactory;
    @Autowired
    private PackageService packageService;
    @Autowired
    private RoleTool roleTool;
    @Autowired
    private OperateRecordService operateRecordService;
    @Autowired
    private ProjectService projectService;

    @Override
    public PaginationResult<PackagePageVO> pageGetPackages(PackageQueryDTO packageDTO, Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(PACKAGE.getPageSearchType());
        if (baseHandle instanceof PackagePageSearchHandle) {
            PackagePageSearchHandle pageSearchHandle = (PackagePageSearchHandle) baseHandle;
            return pageSearchHandle.doPage(packageDTO, projectId);
        }
        LOGGER.warn(
                "class=PackageManagerImpl||method=pageGetPackages||msg=failed to get the PackagePageSearchHandle");
        return PaginationResult.buildFail("分页获取软件包信息失败");
    }

    @Override
    public Result<PackageQueryVO> getPackageById(Long id) {
        Package packageId = new Package();
        packageId.setId(Math.toIntExact(id));
        com.didiglobal.logi.op.manager.infrastructure.common.Result<List<Package>> packageByIdResult = packageService.queryPackage(packageId);
        if (packageByIdResult.getData() == null) {
            return Result.buildNotExist(ResultType.NOT_EXIST.getMessage());
        }
        Package queryPackageById = packageByIdResult.getData().stream().findFirst().get();
        PackageQueryVO packageQueryVO = ConvertUtil.obj2Obj(queryPackageById, PackageQueryVO.class);
        packageQueryVO.setIsEnginePlugin(queryPackageById.getType());
        return Result.buildSucc(packageQueryVO);
    }

    @Override
    public Result<Boolean> addPackage(PackageAddDTO packageAddDTO, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(id -> id, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        List<PackageGroupConfig> packageGroupConfigs = ConvertUtil.str2ObjArrayByJson(packageAddDTO.getGroupConfigList()
                , PackageGroupConfig.class);
        Package addPackage = ConvertUtil.obj2Obj(packageAddDTO, Package.class);
        Result<Void> checkResult = checkPackage(addPackage, packageGroupConfigs, operator, ADD);
        if (checkResult.failed()) {
            return Result.buildFrom(checkResult);
        }
        addPackage.setCreator(operator);
        addPackage.setType(packageAddDTO.getIsEnginePlugin());
        addPackage.setGroupConfigList(packageGroupConfigs);
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> addPackageResult = packageService.createPackage(addPackage);
        //新增操作记录
        if (addPackageResult.isSuccess()) {
            operateRecordService.save(new OperateRecord.Builder().userOperation(operator).operationTypeEnum(OperateTypeEnum.SOFTWARE_ADD)
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID)).content(packageAddDTO.getName())
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build());
        }
        return Result.buildFrom(addPackageResult);
    }

    @Override
    public Result<Boolean> updatePackage(PackageUpdateDTO packageUpdateDTO, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(id -> id, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        List<PackageGroupConfig> packageGroupConfigs = ConvertUtil.str2ObjArrayByJson(packageUpdateDTO.getGroupConfigList()
                , PackageGroupConfig.class);
        Package editPackage = ConvertUtil.obj2Obj(packageUpdateDTO, Package.class);
        Result<Void> checkResult = checkPackage(editPackage, packageGroupConfigs, operator, EDIT);
        if (checkResult.failed()) {
            return Result.buildFrom(checkResult);
        }
        //查询软件包变更前信息
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> oldPackageResult = packageService
                .getPackageById(Long.valueOf(packageUpdateDTO.getId()));
        if (Objects.isNull(oldPackageResult.getData())) {
            return Result.buildParamIllegal("输入的id参数有问题，请核对");
        }
        //更新软件包
        editPackage.setCreator(operator);
        editPackage.setType(packageUpdateDTO.getIsEnginePlugin());
        editPackage.setGroupConfigList(packageGroupConfigs);
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> editPackageResult = packageService.updatePackage(editPackage);
        //操作记录
        if (editPackageResult.isSuccess()) {
            Package oldPackage = oldPackageResult.getData();
            operateRecordService.save(new OperateRecord.Builder().userOperation(operator).operationTypeEnum(OperateTypeEnum.SOFTWARE_EDIT)
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                    .content(String.format("%s软件信息变更。软件名称：%s，版本：%s，脚本ID：%s，软件包类型：%s，描述：%s" +
                                    "-->软件名称：%s，版本：%s，脚本ID：%s，软件包类型：%s，描述：%s", oldPackage.getName(),
                            oldPackage.getName(), oldPackage.getVersion(), oldPackage.getScriptId(),
                            SoftwarePackageTypeEnum.getPackageTypeDesc(oldPackage.getPackageType()), oldPackage.getDescribe(),
                            packageUpdateDTO.getName(), packageUpdateDTO.getVersion(),
                            packageUpdateDTO.getScriptId(), SoftwarePackageTypeEnum.getPackageTypeDesc(packageUpdateDTO.getPackageType()),
                            packageUpdateDTO.getDescribe()))
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build());
        }
        return Result.buildFrom(editPackageResult);
    }

    @Override
    public Result<Long> deletePackage(Long id, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> oldpackage = packageService.getPackageById(id);
        if (Objects.isNull(oldpackage.getData())) {
            return Result.buildParamIllegal("输入的id参数有问题，请核对");
        }
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> deletePackageResult = packageService.deletePackage(Math.toIntExact(id));
        //操作记录
        if (deletePackageResult.isSuccess()) {
            operateRecordService.save(new OperateRecord.Builder().userOperation(operator).operationTypeEnum(OperateTypeEnum.SOFTWARE_DELETE)
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID)).content(oldpackage.getData().getName())
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build());
        }
        return Result.buildFrom(deletePackageResult);
    }

    @Override
    public Result<Boolean> isUsingPackage(Long id, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        return Result.buildFrom(packageService.usingPackage(Math.toIntExact(id)));
    }

    @Override
    public Result<List<PackageBriefVO>> listPackageWithHigherVersion(Integer projectId, String currentVersion, Integer packageId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        //获取当前版本的插件名称
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Package> packageRet = packageService.getPackageById(packageId.longValue());
        if (packageRet.failed()) {
            return Result.buildSucc(Lists.newArrayList());
        }
        //获取该插件的所有版本
        List<Package> packageList = packageService.queryPackageByName(packageRet.getData().getName());
        List<Package> listPackage = packageList.stream().filter(aPackage -> ESVersionUtil.compareVersion(aPackage.getVersion(), currentVersion) > 0).collect(Collectors.toList());
        return Result.buildSucc(ConvertUtil.list2List(listPackage, PackageBriefVO.class));
    }

    @Override
    public Result<List<PackageGroupConfigQueryVO>> listPackageGroupConfigByVersion(String version, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        Integer packageType = SoftwarePackageTypeEnum.ES_INSTALL_PACKAGE.getPackageType();
        List<PackageGroupConfig> packageGroupConfigs = packageService.listPackageGroupConfigByVersion(version, packageType);
        return Result.buildSucc(ConvertUtil.list2List(packageGroupConfigs, PackageGroupConfigQueryVO.class));
    }

    @Override
    public Result<List<PackageGroupConfigQueryVO>> listPackageGroupConfigByKey(String name, Integer projectId, String version, String packageTypeDesc) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        List<PackageGroupConfig> packageGroupConfigs = packageService.listPackageGroupConfigByKey(name, version, SoftwarePackageTypeEnum.getPackageTypeByDesc(packageTypeDesc));
        return Result.buildSucc(ConvertUtil.list2List(packageGroupConfigs, PackageGroupConfigQueryVO.class));
    }

    @Override
    public Result<List<PackageBriefVO>> listPackageVersionByPackageType(String packageTypeDesc, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        List<Package> packageList = packageService.listPackageByPackageType(SoftwarePackageTypeEnum.getPackageTypeByDesc(packageTypeDesc));
        return Result.buildSucc(ConvertUtil.list2List(packageList, PackageBriefVO.class));
    }

    /*************************************************private**********************************************************/
    /**
     * 校验
     *
     * @param checkPackage
     * @param packageGroupConfigs
     * @param operator
     * @param operation
     * @return
     */
    private Result<Void> checkPackage(Package checkPackage, List<PackageGroupConfig> packageGroupConfigs, String operator, OperationEnum operation) {
        if (AriusObjUtils.isNull(checkPackage)) {
            return Result.buildParamIllegal("软件包为空");
        }
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("非运维人员不能操作软件包!");
        }
        if (operation.equals(UNKNOWN)) {
            return Result.buildParamIllegal("操作类型未知");
        }
        if (!ESVersionUtil.isValid(checkPackage.getVersion())) {
            return Result.buildParamIllegal("版本号格式不正确, 必须是'1.1.1.1000'类似的格式");
        }
        //安装目录和用户名配置要必填
        Boolean checkRequired = packageGroupConfigs.stream().anyMatch(packageGroupConfig -> {
            String systemConfig = packageGroupConfig.getSystemConfig();
            JSONObject jsonObject = JSON.parseObject(systemConfig);
            if (AriusObjUtils.isNull(jsonObject)) {
                return true;
            }
            if (AriusObjUtils.isNull(jsonObject.get("installDirector"))) {
                return true;
            }
            if (AriusObjUtils.isNull(jsonObject.get("user"))) {
                return true;
            }
            return false;
        });
        if (checkRequired) {
            return Result.buildParamIllegal("安装目录和用户名配置必填");
        }
        if (operation.getCode() == ADD.getCode()) {
            com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> checkCreateParam = checkPackage.checkCreateParam();
            if (checkCreateParam.failed()) {
                return Result.buildFrom(checkCreateParam);
            }
            if (checkPackage.getUploadFile().getSize() > MULTI_PART_FILE_SIZE_MAX) {
                return Result.buildFail("软件包[" + checkPackage.getName() + "]文件的大小超过限制，不能超过"
                        + MULTI_PART_FILE_SIZE_MAX / 1024 / 1024 + "M");
            }
            String originalFilename = checkPackage.getUploadFile().getOriginalFilename();
            if (!(originalFilename.endsWith(FileCompressionType.TAR_GZ) || originalFilename.endsWith(FileCompressionType.ZIP) ||
                            originalFilename.endsWith(FileCompressionType.TAR))) {
                return Result.buildFail("必须上传gz|zip|tar格式文件");
            }
        } else if (operation.getCode() == EDIT.getCode()) {
            if (Objects.nonNull(checkPackage.getUploadFile()) && checkPackage.getUploadFile().getSize() > MULTI_PART_FILE_SIZE_MAX) {
                return Result.buildFail("软件包[" + checkPackage.getName() + "]文件的大小超过限制，不能超过"
                        + MULTI_PART_FILE_SIZE_MAX / 1024 / 1024 + "M");
            }
            if(Objects.nonNull(checkPackage.getUploadFile())) {
                String originalFilename = checkPackage.getUploadFile().getOriginalFilename();
                if (!(originalFilename.endsWith(FileCompressionType.TAR_GZ) || originalFilename.endsWith(FileCompressionType.ZIP) ||
                        originalFilename.endsWith(FileCompressionType.TAR))) {
                    return Result.buildFail("必须上传gz|zip|tar格式文件");
                }
            }
        }
        return Result.buildSucc();
    }
}
