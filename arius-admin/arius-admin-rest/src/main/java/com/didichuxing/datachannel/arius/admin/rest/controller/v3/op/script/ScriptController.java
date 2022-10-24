package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.script;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.ecm.ScriptConditionDTO;
import com.didiglobal.logi.op.manager.interfaces.dto.ScriptDTO;
import com.didiglobal.logi.op.manager.interfaces.vo.ScriptVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@RestController
@RequestMapping(V3_OP + "/script")
@Api(tags = "脚本中心接口(REST)")
public class ScriptController {
    @GetMapping("/page")
    @ApiOperation(value = "获取脚本列表接口")
    public PaginationResult<ScriptVO> pageGetScripts(@RequestBody ScriptConditionDTO conditionDTO) {
        return new PaginationResult<>();
    }
    @GetMapping("/{id}")
    @ApiOperation(value = "根据脚本id获取脚本接口")
    public Result<ScriptVO> getScriptByScriptId(@PathVariable Long id) {
        return new Result<>();
    }
    @PostMapping("")
    @ApiOperation(value = "新增脚本接口", notes = "")
    public Result<Long> saveScript(HttpServletRequest request, ScriptDTO scriptDTO) {
        return new Result<>();
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改脚本接口", notes = "")
    public Result<ScriptVO> updateScript(HttpServletRequest request, @RequestBody ScriptDTO scriptDTO) {
        return new Result<>();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除脚本接口", notes = "")
    public Result<Long> deleteScript(HttpServletRequest request, @PathVariable Long id) {
        return new Result<>();
    }
}

