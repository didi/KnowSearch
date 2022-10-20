package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.metrics;

import com.didichuxing.datachannel.arius.admin.biz.metrics.MetricsDictionaryManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricDictionaryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.dictionary.MetricsDictionaryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * 详细介绍类情况.
 *
 * @ClassName MetricsDictionaryController
 * @Author gyp
 * @Date 2022/9/28
 * @Version 1.0
 */
@RestController
@RequestMapping({V3 + "/metrics/dictionary"})
@Api(tags = "指标字典信息")
public class MetricsDictionaryController {
    @Autowired
    private MetricsDictionaryManager metricsDictionaryManager;

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "通过model筛选获取指标字典信息")
    public Result<List<MetricsDictionaryVO>> listMetricsDictionaries(@RequestBody MetricDictionaryDTO param) {
        return metricsDictionaryManager.listByCondition(param);
    }
}