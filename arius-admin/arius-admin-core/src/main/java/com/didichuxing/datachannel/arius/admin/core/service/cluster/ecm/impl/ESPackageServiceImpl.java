package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage.ESPackage;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esPackage.ESPackagePO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPackageDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;

/**
 * @author linyunan
 * @date 2021-05-19
 */
@Service
public class ESPackageServiceImpl implements ESPackageService {
    private static final ILog    LOGGER = LogFactory.getLog(ESPackageServiceImpl.class);

    @Autowired
    private ESPackageDAO         esPackageDAO;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private FileStorageService   fileStorageService;

    @Override
    public List<ESPackage> listESPackage() {
        return ConvertUtil.list2List(esPackageDAO.listAll(), ESPackage.class);
    }

    @Override
    public Result<Long> addESPackage(ESPackageDTO esPackageDTO, String operator) {
        Result checkResult = checkValid(esPackageDTO, operator, ADD);
        if (checkResult.failed()) {
            return checkResult;
        }

        if (isHostType(esPackageDTO)) {
            Result uploadResult = upload(esPackageDTO);
            if (uploadResult.failed()) {
                return uploadResult;
            }
        }

        return saveESPackageToDB(esPackageDTO);
    }

    @Override
    public Result<ESPackage> updateESPackage(ESPackageDTO esPackageDTO, String operator) {
        Result checkResult = checkValid(esPackageDTO, operator, EDIT);
        if (checkResult.failed()) {
            return Result.buildFail(checkResult.getMessage());
        }

        if (isHostType(esPackageDTO)) {
            Result uploadResult = upload(esPackageDTO);
            if (uploadResult.failed()) {
                return Result.buildFail(uploadResult.getMessage());
            }
        }

        return updatePackageToDB(esPackageDTO);
    }

    @Override
    public ESPackage getESPackagePOById(Long id) {
        return ConvertUtil.obj2Obj(esPackageDAO.getById(id), ESPackage.class);
    }

    @Override
    public Result<Long> deleteESPackage(Long id, String operator) {
        if (!ariusUserInfoService.isOPByDomainAccount(operator)) {
            return Result.buildFail("非运维人员不能删除ES安装包!");
        }

        boolean succ = (1 == esPackageDAO.delete(id));
        return Result.build(succ, id);
    }

    @Override
    public ESPackage getByVersionAndType(String esVersion, Integer manifest) {
        return ConvertUtil.obj2Obj(esPackageDAO.getByVersionAndType(esVersion, manifest), ESPackage.class);
    }

    /*************************************************private**********************************************************/
    private Result upload(ESPackageDTO esPackageDTO) {
        Result<String> response = Result.buildFail();
        try {
            if (esPackageDTO.getUploadFile() != null) {
                response = fileStorageService.upload(esPackageDTO.getFileName(), esPackageDTO.getMd5(),
                    esPackageDTO.getUploadFile(), null);
                if (response.success()) {
                    esPackageDTO.setUrl(esPackageDTO.getFileName());
                } else {
                    return Result.buildFail("上传文件失败");
                }
            }
        } catch (Exception e) {
            LOGGER.info("class=ESPackageServiceImpl||method=addESPlugin||uploadResponse={}||pluginName={}||exception={}",
                response.getMessage(), esPackageDTO.getFileName(), e);
        }

        return Result.buildSucc();
    }

    private boolean isHostType(ESPackageDTO esPackageDTO) {
        return esPackageDTO.getManifest() == ESClusterTypeEnum.ES_HOST.getCode();
    }

    private Result checkValid(ESPackageDTO esPackageDTO, String operator, OperationEnum operation) {
        if (AriusObjUtils.isNull(esPackageDTO)) {
            return Result.buildParamIllegal("安装包为空");
        }

        if (!ariusUserInfoService.isOPByDomainAccount(operator)) {
            return Result.buildFail("非运维人员不能更新ES安装包!");
        }

        if (!ESVersionUtil.isValid(esPackageDTO.getEsVersion())) {
            return Result.buildParamIllegal("版本号格式不正确, 必须是'1.1.1.1000'类似的格式");
        }

        ESPackagePO packageByVersion = null;
        if (operation.getCode() == ADD.getCode()) {
            packageByVersion = esPackageDAO.getByVersionAndType(esPackageDTO.getEsVersion(),
                esPackageDTO.getManifest());
        } else if (operation.getCode() == EDIT.getCode()) {
            packageByVersion = esPackageDAO.getByVersionAndManifestNotSelf(esPackageDTO.getEsVersion(),
                esPackageDTO.getManifest(), esPackageDTO.getId());
        }

        if (!AriusObjUtils.isNull(packageByVersion)) {
            return Result.buildParamIllegal("版本号重复");
        }

        return Result.buildSucc();
    }

    private Result<Long> saveESPackageToDB(ESPackageDTO esPackageDTO) {
        ESPackagePO esPackagePO = ConvertUtil.obj2Obj(esPackageDTO, ESPackagePO.class);

        boolean succ = Boolean.FALSE;
        try {
            succ = (1 == esPackageDAO.insert(esPackagePO));
        } catch (Exception e) {
            LOGGER.info("class=ESPackageServiceImpl||method=saveESPackageToDB||msg={}", e.getMessage());
        }

        return Result.build(succ, esPackagePO.getId());
    }

    private Result<ESPackage> updatePackageToDB(ESPackageDTO esPackageDTO) {
        ESPackagePO esPackagePO = ConvertUtil.obj2Obj(esPackageDTO, ESPackagePO.class);
        boolean succ = Boolean.FALSE;
        try {
            succ = (1 == esPackageDAO.update(esPackagePO));
        } catch (Exception e) {
            LOGGER.info("class=ESPackageServiceImpl||method=updatePackageToDB||msg={}", e.getMessage());
        }

        return Result.build(succ, ConvertUtil.obj2Obj(esPackagePO, ESPackage.class));
    }
}
