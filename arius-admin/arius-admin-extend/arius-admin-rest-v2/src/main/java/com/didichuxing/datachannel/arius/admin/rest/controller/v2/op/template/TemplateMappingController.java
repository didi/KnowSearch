package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhonghua
 * @date 2019/5/15
 */
@RestController
@RequestMapping({V2 + "/template/mapping"})
@Api(tags = "mapping管理接口(REST)")
public class TemplateMappingController {
    private static final String            CLUSTER_STR       = "cluster";
    private static final String            TEMPLATE_STR      = "template";
    private static final String            MAPPING_STR       = "mapping";
    private static final String            DO_MERGE_STR      = "doMerge";

    @Autowired
    private TemplatePhyMappingManager mappingService;

    @PostMapping("/check")
    @ApiOperation(value = "检查索引mapping" )

    public Result<Void> checkMapping(@RequestBody JSONObject param) {
        String cluster = param.getString(CLUSTER_STR);
        String template = param.getString(TEMPLATE_STR);
        String mappings = param.getJSONObject(MAPPING_STR).toJSONString();
        boolean doMerge = true;
        if(param.containsKey(DO_MERGE_STR)) {
            doMerge = param.getBoolean(DO_MERGE_STR);
        }
         return Result.buildFail("接口已经下线：迁移到v3");
        //return mappingService.checkMapping(cluster, template, mappings, doMerge);
    }
}