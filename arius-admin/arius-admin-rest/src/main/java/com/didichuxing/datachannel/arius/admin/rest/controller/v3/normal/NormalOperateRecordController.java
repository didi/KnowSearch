package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.project.OperateRecordManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.UserConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户操作记录接口(REST)
 * @author gyp
 * @date 2022/5/10
 * @version 1.0
 */
@RestController
@RequestMapping(V3 + "/record")
@Api(tags = "用户操作记录接口(REST)")
public class NormalOperateRecordController {

    @Autowired
    private OperateRecordManager operateRecordManager;

    @GetMapping("/module")
    @ResponseBody
    @ApiOperation(value = "获取所有模块")
    public Result<Map<String, Integer>> mapModules() {
        return Result.buildSucc(ModuleEnum.toMap());
    }

    @GetMapping("/operation-type/{moduleCode}")
    @ResponseBody
    @ApiOperation(value = "获取操作类型")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "path", dataType = "String", name = "moduleCode", value = "模块code:为空则会返回全部", required = false) })
    public Result<Map<Integer, String>> listOperationType(@PathVariable(value = "moduleCode", required = false) String moduleCode) {
        return Result.buildSucc(OperateTypeEnum
            .getOperationTypeByModule(StringUtils.isNumeric(moduleCode) ? Integer.valueOf(moduleCode) : null));
    }

    @GetMapping("/trigger-way")
    @ResponseBody
    @ApiOperation(value = "获取触发方式")
    public Result<Map<String, Integer>> listTriggerWay() {
        return Result.buildSucc(TriggerWayEnum.getOperationList());
    }

    @PostMapping("/page")
    @ApiOperation(value = "查询操作日志列表", notes = "分页和条件查询")
    public PaginationResult<OperateRecordVO> page(HttpServletRequest request,
                                                  @RequestBody OperateRecordDTO queryDTO) throws NotFindSubclassException {

        return operateRecordManager.pageOplogPage(queryDTO, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取操作日志详情", notes = "根据操作日志id获取操作日志详情")
    @ApiImplicitParam(name = "id", value = "操作日志id", dataType = "int", required = true)
    public Result<OperateRecordVO> get(@PathVariable Integer id) {
        return operateRecordManager.getOplogDetailByOplogId(id);
    }

    @PostMapping ("/sense-operate-record")
    @ResponseBody
    @ApiOperation(value = "获取sense操作记录列表")
    public Result<List<OperateRecordVO>> listSenseOperateRecord(HttpServletRequest request, @RequestBody OperateRecordDTO queryDTO) {
        return operateRecordManager.listSenseOperateRecord(queryDTO,HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/sense-operate-record")
    @ResponseBody
    @ApiOperation(value = "更新账号应用下sense操作记录")
    public Result<Integer> updateSenseOperateRecord(@RequestBody OperateRecordDTO operateRecordDTO, HttpServletRequest request) {
        return operateRecordManager.updateSenseOperateRecord(operateRecordDTO, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

}