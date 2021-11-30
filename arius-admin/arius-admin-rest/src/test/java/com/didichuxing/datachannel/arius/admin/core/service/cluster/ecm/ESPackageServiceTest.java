package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPackageDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPackageDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.Assert;
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
public class ESPackageServiceTest extends AriusAdminApplicationTests {

    @Autowired
    ESPackageService esPackageService;

    @Autowired
    private ESPackageDAO esPackageDAO;

    @MockBean
    private AriusUserInfoService ariusUserInfoService;

    @MockBean
    private FileStorageService fileStorageService;

    private static final String OPERATOR = "wpk";

    /**
     * catch的异常分支无法进入,因此做了一些修改
     */
    @Test
    public void listESPackageTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Mockito.when(ariusUserInfoService.isOPByDomainAccount(OPERATOR)).thenReturn(true);
        Long id = esPackageService.addESPackage(esPackageDTO, OPERATOR).getData();
        Assert.assertTrue(esPackageService.listESPackage().stream().anyMatch(esPackage -> esPackage.getId().equals(id)));
    }

    /**
     * 涉及多个私有的校验方法，直接在这里进行分支的覆盖,后续不再额外覆盖分支
     */
    @Test
    public void addESPackageTest() {
        Assert.assertEquals(Result.buildParamIllegal("安装包为空").getMessage(),
                esPackageService.addESPackage(null, OPERATOR).getMessage());
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Mockito.when(ariusUserInfoService.isOPByDomainAccount(OPERATOR)).thenReturn(false);
        Assert.assertEquals(Result.buildFail("非运维人员不能更新ES安装包!").getMessage(),
                esPackageService.addESPackage(esPackageDTO, OPERATOR).getMessage());
        Mockito.when(ariusUserInfoService.isOPByDomainAccount(OPERATOR)).thenReturn(true);
        Assert.assertEquals(ResultType.SUCCESS.getCode(),
                esPackageService.addESPackage(esPackageDTO, OPERATOR).getCode().intValue());
        Integer manifest = ESClusterTypeEnum.ES_HOST.getCode();
        esPackageDTO.setManifest(manifest);
        Mockito.when(fileStorageService.upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any())).thenReturn(Result.buildFail());
        Assert.assertEquals(Result.buildFail("上传文件失败").getMessage(),
                esPackageService.addESPackage(esPackageDTO, OPERATOR).getMessage());
        Mockito.when(fileStorageService.upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any())).thenReturn(Result.buildSucc());
        Assert.assertEquals(ResultType.SUCCESS.getCode(),
                esPackageService.addESPackage(esPackageDTO, OPERATOR).getCode().intValue());
        Assert.assertEquals(Result.buildParamIllegal("版本号重复").getMessage(),
                esPackageService.addESPackage(esPackageDTO, OPERATOR).getMessage());
        esPackageDTO.setEsVersion("1");
        Assert.assertEquals(Result.buildParamIllegal("版本号格式不正确, 必须是'1.1.1.1000'类似的格式").getMessage(),
                esPackageService.addESPackage(esPackageDTO, OPERATOR).getMessage());
    }

    @Test
    public void updateESPackageTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Mockito.when(ariusUserInfoService.isOPByDomainAccount(OPERATOR)).thenReturn(true);
        Long id = esPackageService.addESPackage(esPackageDTO, OPERATOR).getData();
        esPackageDTO.setId(id);
        Assert.assertEquals(id,
                esPackageService.updateESPackage(esPackageDTO, OPERATOR).getData().getId());
        esPackageDTO.setId(id + 1);
        Assert.assertEquals(Result.buildParamIllegal("版本号重复").getMessage(),
                esPackageService.updateESPackage(esPackageDTO, OPERATOR).getMessage());
        esPackageDTO.setManifest(ESClusterTypeEnum.ES_HOST.getCode());
        Mockito.when(fileStorageService.upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any())).thenReturn(Result.buildSucc());
        esPackageDTO.setId(id);
        Assert.assertEquals(ResultType.SUCCESS.getCode(),
                esPackageService.updateESPackage(esPackageDTO, OPERATOR).getCode().intValue());
        esPackageDTO.setEsVersion("1");
        Assert.assertEquals(Result.buildParamIllegal("版本号格式不正确, 必须是'1.1.1.1000'类似的格式").getMessage(),
                esPackageService.updateESPackage(esPackageDTO, OPERATOR).getMessage());
    }

    @Test
    public void getESPackagePOByIdTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Mockito.when(ariusUserInfoService.isOPByDomainAccount(OPERATOR)).thenReturn(true);
        Long id = esPackageService.addESPackage(esPackageDTO, OPERATOR).getData();
        Assert.assertNull(esPackageService.getESPackagePOById(id + 1));
        Assert.assertNotNull(esPackageService.getESPackagePOById(id));

    }

    @Test
    public void deleteESPackageTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Long id = 1234l;
        Assert.assertEquals(Result.buildFail("非运维人员不能删除ES安装包!").getMessage(),
                esPackageService.deleteESPackage(id, OPERATOR).getMessage());
        Mockito.when(ariusUserInfoService.isOPByDomainAccount(OPERATOR)).thenReturn(true);
        id = esPackageService.addESPackage(esPackageDTO, OPERATOR).getData();
        Assert.assertTrue(esPackageService.deleteESPackage(id, OPERATOR).success());
        Assert.assertNull(esPackageDAO.getById(id));
    }

    @Test
    public void getByVersionAndTypeTest() {
        ESPackageDTO esPackageDTO = CustomDataSource.esPackageDTOFactory();
        Long id = 1234l;
        Assert.assertNull(esPackageService.getByVersionAndType(esPackageDTO.getEsVersion(), esPackageDTO.getManifest()));
        Mockito.when(ariusUserInfoService.isOPByDomainAccount(OPERATOR)).thenReturn(true);
        id = esPackageService.addESPackage(esPackageDTO, OPERATOR).getData();
        Assert.assertEquals(id, esPackageService.getByVersionAndType(esPackageDTO.getEsVersion(), esPackageDTO.getManifest()).getId());
    }
}
