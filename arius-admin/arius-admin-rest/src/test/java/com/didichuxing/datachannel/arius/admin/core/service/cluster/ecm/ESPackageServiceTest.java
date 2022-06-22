package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPackageDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wpk
 * @date 2021/07/05
 */
@Transactional
@Rollback
public class ESPackageServiceTest extends AriusAdminApplicationTest {

    @Autowired
    ESPackageService esPackageService;

    @Autowired
    private ESPackageDAO esPackageDAO;

   

    @MockBean
    private FileStorageService fileStorageService;

    /**
     * catch的异常分支无法进入,因此做了一些修改
     */
    @Test
    public void listESPackageTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        //Mockito.when(ariusUserInfoService.isOPByDomainAccount(CustomDataSource.OPERATOR)).thenReturn(true);
        Long id = esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getData();
        Assertions.assertTrue(esPackageService.listESPackage().stream().anyMatch(esPackage -> esPackage.getId().equals(id)));
    }

    /**
     * 涉及多个私有的校验方法，直接在这里进行分支的覆盖,后续不再额外覆盖分支
     */
    @Test
    public void addESPackageTest() {
        Assertions.assertEquals(Result.buildParamIllegal("安装包为空").getMessage(),
                esPackageService.addESPackage(null, CustomDataSource.OPERATOR).getMessage());
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        //Mockito.when(ariusUserInfoService.isOPByDomainAccount(CustomDataSource.OPERATOR)).thenReturn(false);
        //Assertions.assertEquals(Result.buildFail("非运维人员不能更新ES安装包!").getMessage(),
        //        esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getMessage());
        //Mockito.when(ariusUserInfoService.isOPByDomainAccount(CustomDataSource.OPERATOR)).thenReturn(true);
        Assertions.assertEquals(ResultType.SUCCESS.getCode(),
                esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getCode().intValue());
        Integer manifest = ESClusterTypeEnum.ES_HOST.getCode();
        esPackageDTO.setManifest(manifest);
        Mockito.when(fileStorageService.upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any())).thenReturn(Result.buildFail());
        Assertions.assertEquals(Result.buildFail("上传文件失败").getMessage(),
                esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getMessage());
        Mockito.when(fileStorageService.upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any())).thenReturn(Result.buildSucc("test", ""));
        Assertions.assertTrue(esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).success());
        Assertions.assertEquals(Result.buildParamIllegal("版本号重复").getMessage(),
                esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getMessage());
        esPackageDTO.setEsVersion("1.w");
        Assertions.assertEquals(Result.buildParamIllegal("版本号格式不正确, 必须是'1.1.1.1000'类似的格式").getMessage(),
                esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getMessage());
    }

    @Test
    public void updateESPackageTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        //Mockito.when(ariusUserInfoService.isOPByDomainAccount(CustomDataSource.OPERATOR)).thenReturn(true);
        Long id = esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getData();
        esPackageDTO.setId(id);
        Assertions.assertEquals(id,
                esPackageService.updateESPackage(esPackageDTO, CustomDataSource.OPERATOR).getData().getId());
        esPackageDTO.setId(id + 1);
        Assertions.assertEquals(Result.buildParamIllegal("版本号重复").getMessage(),
                esPackageService.updateESPackage(esPackageDTO, CustomDataSource.OPERATOR).getMessage());
        esPackageDTO.setManifest(ESClusterTypeEnum.ES_HOST.getCode());
        Mockito.when(fileStorageService.upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any())).thenReturn(Result.buildSucc());
        esPackageDTO.setId(id);
        Assertions.assertEquals(ResultType.SUCCESS.getCode(),
                esPackageService.updateESPackage(esPackageDTO, CustomDataSource.OPERATOR).getCode().intValue());
        esPackageDTO.setEsVersion("1.w");
        Assertions.assertEquals(Result.buildParamIllegal("版本号格式不正确, 必须是'1.1.1.1000'类似的格式").getMessage(),
                esPackageService.updateESPackage(esPackageDTO, CustomDataSource.OPERATOR).getMessage());
    }

    @Test
    public void getESPackagePOByIdTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        //Mockito.when(ariusUserInfoService.isOPByDomainAccount(CustomDataSource.OPERATOR)).thenReturn(true);
        Long id = esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getData();
        Assertions.assertNull(esPackageService.getESPackagePOById(id + 1));
        Assertions.assertNotNull(esPackageService.getESPackagePOById(id));

    }

    @Test
    public void deleteESPackageTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Long id = 1234L;
        Assertions.assertEquals(Result.buildFail("非运维人员不能更新ES安装包!").getMessage(),
                esPackageService.deleteESPackage(id, CustomDataSource.OPERATOR).getMessage());
        //Mockito.when(ariusUserInfoService.isOPByDomainAccount(CustomDataSource.OPERATOR)).thenReturn(true);
        // mock一个文件系统
        Mockito.when(fileStorageService.upload(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(Result.buildSucc());
        Mockito.when(fileStorageService.remove(Mockito.anyString())).thenReturn(Result.buildSucc());
        id = esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getData();
        Assertions.assertTrue(esPackageService.deleteESPackage(id, CustomDataSource.OPERATOR).success());
        Assertions.assertNull(esPackageDAO.getById(id));
    }

    @Test
    public void getByVersionAndTypeTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Long id = 1234L;
        Assertions.assertNull(esPackageService.getByVersionAndType(esPackageDTO.getEsVersion(), esPackageDTO.getManifest()));
        //Mockito.when(ariusUserInfoService.isOPByDomainAccount(CustomDataSource.OPERATOR)).thenReturn(true);
        id = esPackageService.addESPackage(esPackageDTO, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(id, esPackageService.getByVersionAndType(esPackageDTO.getEsVersion(), esPackageDTO.getManifest()).getId());
    }
}