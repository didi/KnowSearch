package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.MappingOptimize;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateSchemaDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateSchemaOptimizeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateFieldConvertVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateSchemaOptimizeVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateSchemaVO;
import com.didichuxing.datachannel.arius.admin.client.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.client.mapping.Field;
import com.didichuxing.datachannel.arius.admin.client.mapping.SpecialField;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithMapping;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V2_CONSOLE + "/template")
@Api(tags = "Console-用户侧索引模板mapping接口(REST)")
public class ConsoleTemplateSchemaController extends BaseConsoleTemplateController {

    @Autowired
    private TemplateLogicMappingManager templateLogicMappingManager;

    @PutMapping("/schema/convert")
    @ResponseBody
    @ApiOperation(value = "模板field转mapping接口", notes = "")
    public Result<ConsoleTemplateFieldConvertVO> convertSchema(@RequestBody List<Field> fields) {
        AriusTypeProperty typeProperty = templateLogicMappingManager.fields2Mapping(fields);

        SpecialField specialField = SpecialField.analyzeFromFields(fields);
        typeProperty.setIdField(specialField.getIdField());
        typeProperty.setRoutingField(specialField.getRoutingField());

        ConsoleTemplateFieldConvertVO convertVO = new ConsoleTemplateFieldConvertVO();
        convertVO.setDateField(specialField.getDateField());
        convertVO.setDateFieldFormat(specialField.getDateFieldFormat());
        convertVO.setMapping(typeProperty.getProperties().toJSONString());
        convertVO.setIdField(specialField.getIdField());
        convertVO.setRoutingField(specialField.getRoutingField());

        return Result.buildSucc(convertVO);
    }

    @GetMapping("/schema")
    @ResponseBody
    @ApiOperation(value = "获取索引Schema信息接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateSchemaVO> getSchema(@RequestParam("logicId") Integer logicId) {
        return templateLogicMappingManager.getSchema(logicId);
    }

    @PutMapping("/schema")
    @ResponseBody
    @ApiOperation(value = "更新索引Schema信息接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> modifySchema(HttpServletRequest request,
                               @RequestBody ConsoleTemplateSchemaDTO schemaDTO) throws AdminOperateException {

        Result<Void> checkAuthResult = checkAppAuth(schemaDTO.getLogicId(), HttpRequestUtils.getAppId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return templateLogicMappingManager.modifySchema(schemaDTO, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/schema/optimize")
    @ResponseBody
    @ApiOperation(value = "获取索引Schema优化信息接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<ConsoleTemplateSchemaOptimizeVO> getSchemaOptimize(@RequestParam("logicId") Integer logicId){
        Result<List<MappingOptimize>> result = templateLogicMappingManager.getTemplateMappingOptimize(logicId);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        ConsoleTemplateSchemaOptimizeVO optimizeVO = new ConsoleTemplateSchemaOptimizeVO();
        optimizeVO.setId(logicId);
        if (!result.getData().isEmpty()) {
            optimizeVO.setName(result.getData().get(0).getTemplateName());
            optimizeVO.setMappingOptimizeItems(result.getData().get(0).getOptimizeItems());
        }

        return Result.buildSucc(optimizeVO);
    }

    @PutMapping("/schema/optimize")
    @ResponseBody
    @ApiOperation(value = "mapping优化执行", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> modifySchema(HttpServletRequest request, @RequestBody ConsoleTemplateSchemaOptimizeDTO optimizeDTO) {

        Result<Void> checkAuthResult = checkAppAuth(optimizeDTO.getLogicId(), HttpRequestUtils.getAppId(request));
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return templateLogicMappingManager.modifySchemaOptimize(optimizeDTO, HttpRequestUtils.getOperator(request));
    }
}
