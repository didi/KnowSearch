package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.AmsTemplatePhysicalConfVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

/**
 *
 *
 * @author d06679
 * @date 2019/3/13
 */
@Deprecated
@RestController
@RequestMapping(V2_THIRD_PART + "/ams")
@Api(value = "第三方Ams接口(REST)")
public class ThirdpartAmsController {

    private static final ILog                   LOGGER = LogFactory.getLog(ThirdpartAmsController.class);

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private TemplateLogicService                templateLogicService;

    @DeleteMapping("/template/deletePhysical")
    @ResponseBody
    @ApiOperation(value = "删除物理模板信息，配额减半", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "physicalId", value = "物理模板ID", required = true) })
    public Result delPhysicalTemplatesById(HttpServletRequest request,
                                     @RequestParam("physicalId") Long physicalId) throws AdminOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = templatePhyService.getTemplateWithLogicById(physicalId);

        if (physicalWithLogic == null) {
            return Result.buildNotExist("模板不存在");
        }

        Result delResult = templatePhyService.delTemplate(physicalId, HttpRequestUtils.getOperatorFromHeader(request));

        if (delResult.success()) {
            IndexTemplateLogicDTO param = new IndexTemplateLogicDTO();
            param.setId(physicalWithLogic.getLogicId());
            param.setQuota(physicalWithLogic.getLogicTemplate().getQuota() / 2);
            return templateLogicService.editTemplate(param, HttpRequestUtils.getOperatorFromHeader(request));
        } else {
            return delResult;
        }
    }

    @GetMapping("/listTypeMappingIndex")
    @ResponseBody
    @ApiOperation(value = "获取多type索引模板的映射信息", notes = "多type索引模板的映射信息")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "dataCenter", value = "数据中心", required = true) })
    public Result<Map<String/*templateName*/, AmsTemplatePhysicalConfVO>> listTypeMappingIndex(@RequestParam(value = "dataCenter") String dataCenter) {
        if (!DataCenterEnum.validate(dataCenter)) {
            return Result.buildParamIllegal("数据中心非法");
        }

        Map<String/*templateName*/, AmsTemplatePhysicalConfVO> resultMap = Maps.newHashMap();
        List<IndexTemplateLogicWithPhyTemplates> logicWithPhysicals = templateLogicService
                .getTemplateWithPhysicalByDataCenter(dataCenter);

        String templateConfig = null;
        AmsTemplatePhysicalConfVO item = null;
        for (IndexTemplateLogicWithPhyTemplates logicWithPhysical : logicWithPhysicals) {
            if (logicWithPhysical.hasPhysicals()) {
                try {
                    templateConfig = logicWithPhysical.getAnyOne().getConfig();
                    if (StringUtils.isNotBlank(templateConfig)) {
                        IndexTemplatePhysicalConfig config = JSON.parseObject(templateConfig,
                                IndexTemplatePhysicalConfig.class);

                        if (MapUtils.isNotEmpty(config.getTypeIndexMapping())) {
                            item = new AmsTemplatePhysicalConfVO();
                            item.setLogicId(logicWithPhysical.getId());
                            item.setName(logicWithPhysical.getName());
                            item.setMappingIndexNameEnable(config.getMappingIndexNameEnable());
                            item.setTypeIndexMapping(config.getTypeIndexMapping());

                            resultMap.put(logicWithPhysical.getName(), item);
                        }
                    }

                } catch (Exception e) {
                    LOGGER.warn("method=listTypeMappingIndex||dataCenter={}||errMsg={}", dataCenter, e.getMessage(), e);
                }
            }
        }

        return Result.buildSucc(resultMap);
    }

}
