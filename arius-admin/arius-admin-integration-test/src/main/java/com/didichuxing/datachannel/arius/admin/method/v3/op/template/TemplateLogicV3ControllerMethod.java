package com.didichuxing.datachannel.arius.admin.method.v3.op.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateSettingVO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class TemplateLogicV3ControllerMethod {

    public static final String TEMPLATE_LOGIC = V3_OP + "/template/logic";

    public static Result<List<String>> listTemplateLogicNames() throws IOException {
        String path = String.format("%s/listNames", TEMPLATE_LOGIC);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>(){});
    }

    public static PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(TemplateConditionDTO condition) throws IOException {
        String path = String.format("%s/page", TEMPLATE_LOGIC);
        return JSON.parseObject(AriusClient.post(path, condition), new TypeReference<PaginationResult<ConsoleTemplateVO>>(){});
    }

    public static Result<Void> checkTemplateValidForCreate(String templateName) throws IOException {
        String path = String.format("%s/%s/nameCheck", TEMPLATE_LOGIC, templateName);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Void>>(){});
    }

    public static Result<Boolean> checkTemplateEditMapping(Integer templateId) throws IOException {
        String path = String.format("%s/%d/checkEditMapping/", TEMPLATE_LOGIC, templateId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> checkTemplateEditService(Integer templateId, Integer templateSrvId) throws IOException {
        String path = String.format("%s/%d/%d/checkEditTemplateSrv/", TEMPLATE_LOGIC, templateId, templateSrvId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Void> switchRolloverStatus(Integer templateLogicId, Integer status) throws IOException {
        String path = String.format("%s/rollover/switch/%d/%d", TEMPLATE_LOGIC, templateLogicId, status);
        return JSON.parseObject(AriusClient.put(path), new TypeReference<Result<Void>>(){});
    }

    public static Result<Void> customizeSetting(TemplateSettingDTO settingDTO) throws IOException {
        String path = String.format("%s/setting", TEMPLATE_LOGIC);
        return JSON.parseObject(AriusClient.put(path, settingDTO), new TypeReference<Result<Void>>(){});
    }

    public static Result<TemplateSettingVO> getTemplateSettings(Integer logicId) throws IOException {
        String path = String.format("%s/setting", TEMPLATE_LOGIC);
        Map<String, Object> params = new HashMap<>();
        params.put("logicId", logicId);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<TemplateSettingVO>>(){});
    }

    public static Result<Void> deleteTemplate(Integer logicId) throws IOException {
        String path = "/v2/console/template/deleteInfo";
        Map<String, Object> params = new HashMap<>();
        params.put("logicId", logicId);
        return JSON.parseObject(AriusClient.delete(path, params, null), new TypeReference<Result<Void>>(){});
    }

    public static Result<ConsoleTemplateDetailVO> getConsoleTemplateDetail(Integer logicId) throws IOException {
        String path = "/v2/console/template/get";
        Map<String, Object> params = new HashMap<>();
        params.put("logicId", logicId);
        return JSON.parseObject(AriusClient.get(path, params), new TypeReference<Result<ConsoleTemplateDetailVO>>(){});
    }
}
