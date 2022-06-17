package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import com.didichuxing.datachannel.arius.admin.biz.app.OperateRecordManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.NewModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didiglobal.logi.security.common.PagingResult;
import com.didiglobal.logi.security.common.dto.oplog.OplogQueryDTO;
import com.didiglobal.logi.security.common.vo.oplog.OplogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户操作记录接口(REST)
 * @author gyp
 * @date 2022/5/10
 * @version 1.0
 */
@RestController
@RequestMapping(V3_NORMAL + "/record")
@Api(tags = "用户操作记录接口(REST)")
public class NormalOperateRecordController {
    
    @Autowired
    private OperateRecordManager operateRecordManager;
  
    
    @GetMapping("/module")
    @ResponseBody
    @ApiOperation(value = "获取所有模块")
    public Result<Map<Integer, String>> mapModules() {
        return Result.buildSucc(NewModuleEnum.toMap());
    }
    
    @GetMapping("/operation-type/{moduleCode}")
    @ResponseBody
    @ApiOperation(value = "获取操作类型")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "moduleCode", value = "模块code:为空则会返回全部", required = true) })
    public Result<List<String>> listOperationType(@PathVariable("moduleCode") Integer moduleCode) {
        return Result.buildSucc(OperationTypeEnum.getOperationTypeByModule(moduleCode));
    }
    
    @GetMapping("/trigger-way")
    @ResponseBody
    @ApiOperation(value = "获取触发方式")
    public Result<List<String>> listTriggerWay() {
        return Result.buildSucc(TriggerWayEnum.getOperationList());
    }
    
    @PostMapping("/page")
    @ApiOperation(value = "查询操作日志列表", notes = "分页和条件查询")
    public PagingResult<OplogVO> page(@RequestBody OplogQueryDTO queryDTO) {
         
        return operateRecordManager.pageOplogPage(queryDTO);
    }
    
    @GetMapping("/{id}")
    @ApiOperation(value = "获取操作日志详情", notes = "根据操作日志id获取操作日志详情")
    @ApiImplicitParam(name = "id", value = "操作日志id", dataType = "int", required = true)
    public Result<OplogVO> get(@PathVariable Integer id) {
        return operateRecordManager.getOplogDetailByOplogId(id);
    }
    
   

}