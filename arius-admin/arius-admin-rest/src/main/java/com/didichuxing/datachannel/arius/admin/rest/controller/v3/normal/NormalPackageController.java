package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import com.didichuxing.datachannel.arius.admin.biz.espackage.ESPackageManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.espackage.ESPackageVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V3_NORMAL + "/ecm/package")
@Api(tags = "ECM-ELASTIC接口(REST)")
public class NormalPackageController {

    @Autowired
    private ESPackageManager packageManager;

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取程序包列表接口")
    public Result<List<ESPackageVO>> listPackage() {
        return packageManager.listESPackage();
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "根据ID获取程序包详情接口")
    public Result<ESPackageVO> getPackageDetailInfo(@PathVariable Long id) {
        return packageManager.getESPackageById(id);
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "新增程序包接口", notes = "")
    public Result<Long> savePackage(HttpServletRequest request, ESPackageDTO esPackageDTO) {
        return packageManager.addESPackage(esPackageDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/update")
    @ResponseBody
    @ApiOperation(value = "修改程序包接口", notes = "")
    public Result<ESPackageVO> updatePackage(HttpServletRequest request, @RequestBody ESPackageDTO esPackageDTO) {
        return packageManager.updateESPackage(esPackageDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "删除程序包接口", notes = "")
    public Result<Long> deletePackage(HttpServletRequest request,
                                      @PathVariable Long id) throws NotFindSubclassException {
        return packageManager.deleteESPackage(id, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }
}