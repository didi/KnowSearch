package com.didichuxing.datachannel.arius.admin.biz.espackage;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.espackage.ESPackageVO;
import com.didichuxing.datachannel.arius.admin.common.constant.espackage.AriusESPackageEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusOptional;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author linyunan
 * @date 2021-05-19
 */
@Component
public class ESPackageManager {

    @Autowired
    private ESPackageService packageService;

    /**
     * 获取所有的package列表
     * @return package列表
     */
    public Result<List<ESPackageVO>> listESPackage() {
        List<ESPackage> esPackageList = packageService.listESPackage();
        if (CollectionUtils.isEmpty(esPackageList)) {
            return Result.buildSucc();
        }

        return Result.buildSucc(esPackageList.stream().map(this::buildESPackageVO).collect(Collectors.toList()));
    }

    /**
     * 根据id获取es package
     * @param id 安装包id
     * @return 安装包
     */
    public Result<ESPackageVO> getESPackageById(Long id) {
        return AriusOptional
                .ofObjNullable(buildESPackageVO(packageService.getESPackagePOById(id)))
                .orGetResult(() -> Result.buildFail("ES安装包不存在"));
    }

    /**
     * 创建一个Package
     *
     * @param esPackageDTO dto
     * @param operator     操作者
     * @param projectId
     * @return 创建数量
     */
    public Result<Long> addESPackage(ESPackageDTO esPackageDTO, String operator, Integer projectId) {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(id -> id, projectId, projectId);
        if (result.failed()){
            return Result.buildFail(result.getMessage());
        }
        return packageService.addESPackage(esPackageDTO, operator);
    }

    /**
     * 修改ES package
     *
     * @param esPackageDTO dto
     * @param operator     操作者
     * @param projectId
     * @return 更新的es package
     */
    public Result<ESPackageVO> updateESPackage(ESPackageDTO esPackageDTO, String operator, Integer projectId) {

        Result<ESPackage> esPackageResult = packageService.updateESPackage(esPackageDTO, operator,projectId);
        if (esPackageResult.failed()) {
            return Result.buildFail(esPackageResult.getMessage());
        }

        return Result.buildSucc(buildESPackageVO(esPackageResult.getData()));
    }

    /**
     * 构建es package vo
     * @param esPackage es安装包
     * @return
     */
    private ESPackageVO buildESPackageVO(ESPackage esPackage) {
        ESPackageVO esPackageVO = ConvertUtil.obj2Obj(esPackage, ESPackageVO.class);

        // 根据es程序包的版本号判断是否为滴滴内部版本，当版本号为四位时，表示为滴滴内部版本，否则为外部开源的版本
        esPackageVO.setPackageType(AriusESPackageEnum.valueOfLength(ESVersionUtil.getVersionLength(esPackage.getEsVersion())).getCode());

        return esPackageVO;
    }

    /**
     * 删除es对应的package
     *
     * @param id        插件包
     * @param operator  操作人
     * @param projectId
     * @return
     */
    public Result<Long> deleteESPackage(Long id, String operator, Integer projectId) throws NotFindSubclassException {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()){
            return Result.buildFail(result.getMessage());
        }
        return packageService.deleteESPackage(id, operator);
    }
}