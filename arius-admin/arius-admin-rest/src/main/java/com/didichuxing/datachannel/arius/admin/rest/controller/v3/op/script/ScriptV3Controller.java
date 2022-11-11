package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.script;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptQueryVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V3_OP + "/script")
@Api(tags = "脚本中心接口(REST)")
public class ScriptV3Controller {
    @PostMapping("/page")
    @ApiOperation(value = "获取脚本分页列表接口")
    public PaginationResult<ScriptQueryVO> pageGetScripts(@RequestBody ScriptQueryDTO conditionDTO) {
        return new PaginationResult<>();
    }
    @GetMapping("/{id}")
    @ApiOperation(value = "根据脚本id获取脚本接口")
    public Result<ScriptVO> getScriptByScriptId(@PathVariable Long id) {
        return new Result<>();
    }
    @GetMapping("/list")
    @ApiOperation(value = "获取脚本list")
    public Result<ScriptListVO> listScript() {
        return new Result<>();
    }
    @PostMapping("")
    @ApiOperation(value = "新增脚本接口", notes = "")
    public Result<Long> saveScript(HttpServletRequest request, @RequestBody ScriptAddDTO scriptAddDTO) {
        return new Result<>();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改脚本接口", notes = "")
    public Result<Long> updateScript(HttpServletRequest request, @RequestBody ScriptUpdateDTO scriptUpdateDTO) {
        return new Result<>();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除脚本接口", notes = "")
    public Result<Long> deleteScript(HttpServletRequest request, @PathVariable Long id) {
        return new Result<>();
    }

    @GetMapping("/{id}/using")
    @ApiOperation(value = "是否正在使用脚本", notes = "")
    public Result<Boolean> isUsingScript(HttpServletRequest request, @PathVariable Long id) {
        return new Result<>();
    }
}