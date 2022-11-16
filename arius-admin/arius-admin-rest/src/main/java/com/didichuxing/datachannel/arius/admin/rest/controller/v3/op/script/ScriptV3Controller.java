package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.script;

import com.didichuxing.datachannel.arius.admin.biz.script.ScriptManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptNameListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptPageVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptQueryVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@RestController
@RequestMapping(V3_OP + "/script")
@Api(tags = "脚本中心接口(REST)")
public class ScriptV3Controller {
    @Autowired
    private ScriptManager scriptManager;
    @PostMapping("/page")
    @ApiOperation(value = "获取脚本分页列表接口")
    public PaginationResult<ScriptPageVO> pageGetScripts(@RequestBody ScriptQueryDTO conditionDTO
            , HttpServletRequest request)  throws NotFindSubclassException {
        return scriptManager.pageGetScripts(conditionDTO, HttpRequestUtil.getProjectId(request));
    }
    @GetMapping("/{id}")
    @ApiOperation(value = "根据脚本id获取脚本接口")
    public Result<ScriptQueryVO> getScriptByScriptId(@PathVariable Long id) {
        return scriptManager.getScriptByScriptId(id);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取脚本名称list")
    public Result<List<ScriptNameListVO>> listScriptName() {
        return scriptManager.listScriptName();
    }

    @PostMapping("")
    @ApiOperation(value = "新增脚本接口", notes = "")
    public Result<Boolean> addScript(HttpServletRequest request, ScriptAddDTO scriptAddDTO) {
        return scriptManager.addScript(scriptAddDTO, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改脚本接口", notes = "")
    public Result<Boolean> editScript(HttpServletRequest request, ScriptUpdateDTO scriptUpdateDTO) {
        return scriptManager.editScript(scriptUpdateDTO, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除脚本接口", notes = "")
    public Result<Long> deleteScript(HttpServletRequest request, @PathVariable Long id) {
        return scriptManager.deleteScript(id, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{id}/using")
    @ApiOperation(value = "是否正在使用脚本", notes = "")
    public Result<Boolean> usingScript(HttpServletRequest request, @PathVariable Long id) {
        return scriptManager.usingScript(id, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
}

