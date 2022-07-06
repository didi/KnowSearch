package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@RestController
@RequestMapping(V3 + "/template/srv")
@Api(tags = "模板服务接口")
public class TemplateSrvController {

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页查询模板服务列表")
    public PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(HttpServletRequest request,
                                                                      @RequestBody TemplateQueryDTO condition) throws NotFindSubclassException {
        if (condition.getProjectId() == null) {
            condition.setProjectId(HttpRequestUtil.getProjectId(request));
        }
        return templateSrvManager.pageGetTemplateWithSrv(condition);
    }

    @PutMapping("/{srvCode}/{templateIdList}")
    @ResponseBody
    @ApiOperation(value = "开启模板服务")
    public Result<Void> openTemplateSrv(HttpServletRequest request,@PathVariable("srvCode") Integer srvCode,
                                        @PathVariable("templateIdList") List<Integer> templateIdList) {
        return templateSrvManager.openSrv(srvCode, templateIdList,HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/{srvCode}/{templateIdList}")
    @ResponseBody
    @ApiOperation(value = "关闭模板服务")
    public Result<Void> closeTemplateSrv(HttpServletRequest request,@PathVariable("srvCode") Integer srvCode,
                                         @PathVariable("templateIdList") List<Integer> templateIdList) {
        return templateSrvManager.closeSrv(srvCode, templateIdList,HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
}