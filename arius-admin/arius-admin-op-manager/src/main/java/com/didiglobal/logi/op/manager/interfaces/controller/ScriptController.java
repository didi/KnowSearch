package com.didiglobal.logi.op.manager.interfaces.controller;

import com.didiglobal.logi.op.manager.application.ScriptService;
import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.infrastructure.common.Constants;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.interfaces.assembler.ScriptAssembler;
import com.didiglobal.logi.op.manager.interfaces.dto.ScriptDTO;
import com.didiglobal.logi.op.manager.interfaces.vo.ScriptVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-06 2:26 下午
 */
@RestController
@Api(value = "脚本中心api")
@RequestMapping(Constants.API_PREFIX_V3 + "/script")
public class ScriptController {

    @Autowired
    private ScriptService scriptService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptController.class);

    @PostMapping("/query")
    @ApiOperation(value = "获取脚本中心列表")
    public Result<List<ScriptVO>> listScript(@RequestBody ScriptDTO queryScriptDTO) {
        Result res = scriptService.listScript(ScriptAssembler.toDO(queryScriptDTO));
        if (res.isSuccess()) {
            res.setData(ScriptAssembler.toVOList((List<Script>) res.getData()));
        }
        return res;
    }

    @PostMapping("")
    @ApiOperation(value = "新建脚本")
    public Result<Void> createScript(ScriptDTO queryScriptDTO) {
        return scriptService.createScript(ScriptAssembler.toDO(queryScriptDTO));
    }

    @PostMapping("/edit")
    @ApiOperation(value = "编辑脚本")
    public Result<Void> editScript(ScriptDTO editScriptDTO) {
        return scriptService.updateScript(ScriptAssembler.toDO(editScriptDTO));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除脚本")
    public Result<Void> deleteScript(@PathVariable Integer id) {
        return scriptService.deleteScript(id);
    }


}
