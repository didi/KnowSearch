package com.didichuxing.datachannel.arius.admin.biz.script.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.SCRIPT;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;

import java.util.List;
import java.util.Objects;

import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.knowframework.security.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.page.ScriptPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.script.ScriptManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptNameListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptPageVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptQueryVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.application.ScriptService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;

@Component
public class ScriptManagerImpl implements ScriptManager {
    private static final ILog LOGGER = LogFactory.getLog(ScriptManagerImpl.class);
    private static final Long MULTI_PART_FILE_SIZE_MAX = 1024 * 1024 * 500L;
    @Autowired
    private HandleFactory handleFactory;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private RoleTool roleTool;
    @Autowired
    private OperateRecordService operateRecordService;
    @Autowired
    private ProjectService projectService;
    @Override
    public Result<ScriptQueryVO> getScriptByScriptId(Long id) {
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Script> scriptByIdResult = scriptService.getScriptById(id);
        if (scriptByIdResult.getData() == null) {
            return Result.buildNotExist(ResultType.NOT_EXIST.getMessage());
        }
        return Result.buildSucc(ConvertUtil.obj2Obj(scriptByIdResult.getData(),ScriptQueryVO.class));
    }

    @Override
    public Result<Boolean> addScript(ScriptAddDTO scriptAddDTO, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(id -> id, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        Script script = ConvertUtil.obj2Obj(scriptAddDTO, Script.class);
        Result<Void> checkResult = checkValid(script, operator, ADD);
        if (checkResult.failed()) {
            return Result.buildFrom(checkResult);
        }
        script.setCreator(operator);
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> addScriptResult = scriptService.createScript(script);
        //新增操作记录
        if (addScriptResult.isSuccess()) {
            operateRecordService.save(new OperateRecord.Builder().userOperation(operator).operationTypeEnum(OperateTypeEnum.SCRIPT_ADD)
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID)).content(scriptAddDTO.getName())
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build());
        }
        return Result.buildFrom(addScriptResult);
    }

    @Override
    public Result<Boolean> editScript(ScriptUpdateDTO scriptUpdateDTO, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(id -> id, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        Script script = ConvertUtil.obj2Obj(scriptUpdateDTO, Script.class);
        Result<Void> checkResult = checkValid(script, operator, EDIT);
        if (checkResult.failed()) {
            return Result.buildFrom(checkResult);
        }
        script.setCreator(operator);
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Script> oldScript = scriptService
                .getScriptById(Long.valueOf(scriptUpdateDTO.getId()));
        if(Objects.isNull(oldScript.getData())){
            return Result.buildParamIllegal("输入的id参数有问题，请核对");
        }
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> editScriptResult = scriptService.updateScript(script);
        //操作记录
        if (editScriptResult.isSuccess()) {
            operateRecordService.save(new OperateRecord.Builder().userOperation(operator).operationTypeEnum(OperateTypeEnum.SCRIPT_EDIT)
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                    .content(String.format("%s脚本修改描述信息。%s-->%s", script.getName(),
                            oldScript.getData().getDescribe(), scriptUpdateDTO.getDescribe()))
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build());
        }
        return Result.buildFrom(editScriptResult);
    }

    @Override
    public Result<Long> deleteScript(Long id, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Script> oldScript = scriptService.getScriptById(id);
        if(Objects.isNull(oldScript.getData())){
            return Result.buildParamIllegal("输入的id参数有问题，请核对");
        }
        com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> deleteScriptResult = scriptService.deleteScript(Math.toIntExact(id));
        //操作记录
        if (deleteScriptResult.isSuccess()) {
            operateRecordService.save(new OperateRecord.Builder().userOperation(operator).operationTypeEnum(OperateTypeEnum.SCRIPT_DELETE)
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID)).content(oldScript.getData().getName())
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).build());
        }
        return Result.buildFrom(deleteScriptResult);
    }

    @Override
    public Result<Boolean> usingScript(Long id, String operator, Integer projectId) {
        Result<Void> result = ProjectUtils.checkProjectCorrectly(retId -> retId, projectId, projectId);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        return Result.buildSucc(scriptService.usingScript(Math.toIntExact(id)));
    }

    @Override
    public Result<List<ScriptNameListVO>> listScriptName() {
        com.didiglobal.logi.op.manager.infrastructure.common.Result<List<Script>> listResult = scriptService.listScript(new Script());
        return Result.buildSucc(ConvertUtil.list2List(listResult.getData(), ScriptNameListVO.class));
    }

    @Override
    public PaginationResult<ScriptPageVO> pageGetScripts(ScriptQueryDTO conditionDTO, Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(SCRIPT.getPageSearchType());
        if (baseHandle instanceof ScriptPageSearchHandle) {
            ScriptPageSearchHandle pageSearchHandle = (ScriptPageSearchHandle) baseHandle;
            return pageSearchHandle.doPage(conditionDTO, projectId);
        }
        LOGGER.warn(
                "class=ScriptManagerImpl||method=pageGetScripts||msg=failed to get the ScriptPageSearchHandle");
        return PaginationResult.buildFail("分页获取脚本信息失败");
    }

    /*************************************************private**********************************************************/
    /**
     * 校验
     *
     * @param script
     * @param operator
     * @param operation
     * @return
     */
    private Result<Void> checkValid(Script script, String operator, OperationEnum operation) {
        if (AriusObjUtils.isNull(script)) {
            return Result.buildParamIllegal("脚本为空");
        }
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("非运维人员不能操作脚本!");
        }
        if (operation.equals(UNKNOWN)) {
            return Result.buildParamIllegal("操作类型未知");
        }
        if (operation.getCode() == ADD.getCode()) {
            com.didiglobal.logi.op.manager.infrastructure.common.Result<Void> checkCreateParam = script.checkCreateParam();
            if (checkCreateParam.failed()) {
                return Result.buildFrom(checkCreateParam);
            }
            if (script.getUploadFile().getSize() > MULTI_PART_FILE_SIZE_MAX) {
                return Result.buildFail("脚本[" + script.getName() + "]文件的大小超过限制，不能超过"
                        + MULTI_PART_FILE_SIZE_MAX / 1024 / 1024 + "M");
            }
            if (!script.getUploadFile().getOriginalFilename().endsWith(".sh")) {
                return Result.buildFail("必须上传sh格式文件");
            }
        } else if (operation.getCode() == EDIT.getCode()) {
            if (Objects.nonNull(script.getUploadFile()) && script.getUploadFile().getSize() > MULTI_PART_FILE_SIZE_MAX) {
                return Result.buildFail("脚本[" + script.getName() + "]文件的大小超过限制，不能超过"
                        + MULTI_PART_FILE_SIZE_MAX / 1024 / 1024 + "M");
            }
            if (Objects.nonNull(script.getUploadFile()) && !script.getUploadFile().getOriginalFilename().endsWith(".sh")) {
                return Result.buildFail("必须上传sh格式文件");
            }
        }
        return Result.buildSucc();
    }
}
