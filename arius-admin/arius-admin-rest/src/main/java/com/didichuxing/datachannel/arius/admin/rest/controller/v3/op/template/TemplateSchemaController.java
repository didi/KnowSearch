package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSchemaDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateFieldConvertVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateMappingVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.mapping.Field;
import com.didichuxing.datachannel.arius.admin.common.mapping.SpecialField;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3 + "/template")
@Api(tags = "Console-用户侧索引模板mapping接口(REST)")
public class TemplateSchemaController extends BaseTemplateController {

    @Autowired
    private TemplateLogicMappingManager templateLogicMappingManager;

    @PutMapping("/schema/convert")
    @ResponseBody
    @ApiOperation(value = "模板field转mapping接口【三方接口】", tags = "【三方接口】")
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
    @ApiOperation(value = "获取索引Schema信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "logicId", value = "索引ID", required = true) })
    public Result<TemplateMappingVO> getSchema(@RequestParam("logicId") Integer logicId) {
        return templateLogicMappingManager.getSchema(logicId);
    }

    @PutMapping("/schema")
    @ResponseBody
    @ApiOperation(value = "更新索引Schema信息接口【三方接口】", tags = "【三方接口】")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-APP-ID", value = "应用ID", required = true) })
    public Result<Void> modifySchema(HttpServletRequest request,
                                     @RequestBody ConsoleTemplateSchemaDTO schemaDTO) throws AdminOperateException {

        Result<Void> checkAuthResult = checkProjectAuth(schemaDTO.getLogicId());
        if (checkAuthResult.failed()) {
            return checkAuthResult;
        }

        return templateLogicMappingManager.editMapping(schemaDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

}