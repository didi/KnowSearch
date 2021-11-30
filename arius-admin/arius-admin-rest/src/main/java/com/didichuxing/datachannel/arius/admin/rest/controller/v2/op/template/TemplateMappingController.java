package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author zhonghua
 * @date 2019/5/15
 */
@RestController
@RequestMapping({V2 + "/template/mapping", V2_OP + "/template/mapping"})
@Api(tags = "mapping管理接口(REST)")
public class TemplateMappingController {
    private static final String            CLUSTER_STR       = "cluster";
    private static final String            TEMPLATE_STR      = "template";
    private static final String            MAPPING_STR       = "mapping";
    private static final String            DO_MERGE_STR      = "doMerge";

    @Autowired
    private TemplatePhyMappingManager mappingService;

    @PostMapping("/check")
    @ApiOperation(value = "检查索引mapping", notes = "")
    public Result<Void> checkMapping(@RequestBody JSONObject param) {
        String cluster = param.getString(CLUSTER_STR);
        String template = param.getString(TEMPLATE_STR);
        String mappings = param.getJSONObject(MAPPING_STR).toJSONString();
        boolean doMerge = true;
        if(param.containsKey(DO_MERGE_STR)) {
            doMerge = param.getBoolean(DO_MERGE_STR);
        }
        return mappingService.checkMapping(cluster, template, mappings, doMerge);
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新索引mapping", notes = "")
    public Result<Void> updateMapping(@RequestBody JSONObject param) {
        if (!param.containsKey(CLUSTER_STR) || !param.containsKey(TEMPLATE_STR) || !param.containsKey(MAPPING_STR)) {
            return Result.build(ResultType.FAIL.getCode(), "param is null");
        }

        String cluster = param.getString(CLUSTER_STR);
        String template = param.getString(TEMPLATE_STR);
        String mappings = param.getJSONObject(MAPPING_STR).toJSONString();

        return mappingService.updateMapping(cluster, template, mappings);
    }

    @GetMapping("/getMapping")
    @ApiOperation(value = "获取索引mapping", notes = "")
    public Result<JSONObject> getMapping(@RequestParam(value = "cluster") String cluster,
                                         @RequestParam(value = "template") String name) {
        try {
            MappingConfig mappings = mappingService.getMapping( cluster, name ).getData();
            if (mappings == null) {
                return Result.build( ResultType.FAIL.getCode(), "mapping不存在" );
            }

            mappings.removeDefault();
            return Result.build( true, mappings.toJson() );
        } catch (JSONException e){
            return Result.build(ResultType.FAIL.getCode(), "json解析失败");
        } catch (Exception t) {
            return Result.build(ResultType.FAIL.getCode(), t.getMessage());
        }
    }
}
