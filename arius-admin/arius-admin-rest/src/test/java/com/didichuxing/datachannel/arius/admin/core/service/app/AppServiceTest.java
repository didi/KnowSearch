package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Rollback
public class AppServiceTest extends AriusAdminApplicationTest {
    private static final ILog LOGGER = LogFactory.getLog(AppServiceTest.class);

    @Autowired
    private AppService service;

    @Autowired
    private AppDAO appDAO;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private AppUserInfoService userInfoService;

    private static final String operator = "System";
    private static final AppConfig defaultConfig = new AppConfig();

    @BeforeAll
    public static void prepareData() {
        defaultConfig.setDslAnalyzeEnable(1);
        defaultConfig.setIsSourceSeparated(0);
        defaultConfig.setAggrAnalyzeEnable(1);
        defaultConfig.setAnalyzeResponseEnable(1);
    }

    @BeforeEach
    public void before() {
        MockitoAnnotations.initMocks(this);
        Result result = Result.buildSucc();
        Mockito.when(employeeService.checkUsers(Mockito.any())).thenReturn(result);
    }

    private AppPO getAppPO() {
        AppPO appPO = new AppPO();
        appPO.setName("test");
        appPO.setDataCenter("");
        appPO.setIsRoot(1);
        appPO.setMemo("");
        appPO.setIp("");
        appPO.setVerifyCode("");
        appPO.setIsActive(1);
        appPO.setQueryThreshold(100);
        appPO.setCluster("");
        appPO.setDepartmentId("");
        appPO.setDepartment("");
        appPO.setResponsible("");
        appPO.setSearchType(0);
        return appPO;
    }

    /**
     * fixme 增加时要求一些字段非空，但修改时不要求
     */
    @Test
    public void validateAppAddTest() {
        AppDTO appDTO = new AppDTO();
        Assertions.assertFalse(service.validateApp(null, OperationEnum.ADD).success());
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.ADD).success());
        appDTO.setName("1");
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.ADD).success());
        appDTO.setResponsible("1");
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.ADD).success());
        appDTO.setMemo("1");
        Assertions.assertTrue(service.validateApp(appDTO, OperationEnum.ADD).success());
    }

    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void appDuplicateTest(AppDTO appDTO) {
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        appDTO.setId(appPO.getId() + 1);
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.ADD).success());
    }

    @Test
    public void validateAppEditTest() {
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        AppDTO appDTO = new AppDTO();

        // 要修改的app不存在
        appDTO.setId(-1);
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());

        appDTO.setId(appPO.getId());
        service.validateApp(appDTO, OperationEnum.EDIT);
        Assertions.assertFalse(service.validateApp(null, OperationEnum.EDIT).success());
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setName("1");
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setDepartment("1");
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setDepartmentId("1");
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setResponsible("1");
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setMemo("1");
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setIsRoot(1);
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setSearchType(0);
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
        appDTO.setVerifyCode("XXXX");
        Assertions.assertTrue(service.validateApp(appDTO, OperationEnum.EDIT).success());

        // 修改时名称与其他app重复的情况
        appDTO.setName(appPO.getName());
        appDTO.setId(appPO.getId() + 1);
        Assertions.assertFalse(service.validateApp(appDTO, OperationEnum.EDIT).success());
    }

    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void registerAppTest(AppDTO appDTO) {
        String operator = "System";
        Result<Integer> result = service.registerApp(appDTO, operator);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void appNullTest() {
        String operator = "System";
        Result<Integer> result = service.registerApp(null, operator);
        Result result1 = service.editApp(null, operator);
        Assertions.assertFalse(result.success());
        Assertions.assertFalse(result1.success());
    }

    /**
     * 名称重复
     *
     * @param appDTO
     */
    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void registerAppDuplicateTest(AppDTO appDTO) {
        String operator = "System";
        Result<Integer> result = service.registerApp(appDTO, operator);
        Result<Integer> result1 = service.registerApp(appDTO, operator);
        Assertions.assertFalse(result1.success());
    }

    /**
     * 修改
     *
     * @param appDTO
     */
    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void editAppTest(AppDTO appDTO) {
        Result<Integer> result = service.registerApp(appDTO, operator);
        Integer id = result.getData();
        appDTO.setId(id);
        String newName = "test2";
        appDTO.setName(newName);
        Result result1 = service.editApp(appDTO, operator);
        Assertions.assertTrue(result1.success());
        App app = service.getAppById(id);
        Assertions.assertEquals(newName, app.getName());
    }

    /**
     * 修改
     *
     * @param appDTO
     */
    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void editAppDuplicateTest(AppDTO appDTO) {
        Result<Integer> result = service.registerApp(appDTO, operator);
        Integer id = result.getData();
        appDTO.setId(id + 1);
        Assertions.assertFalse(service.editApp(appDTO, operator).success());
    }

    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void deleteAppTest(AppDTO appDTO) {
        Result<Integer> result = service.registerApp(appDTO, operator);
        Integer id = result.getData();
        Result<Void> result1 = service.deleteAppById(id, operator);
        Assertions.assertTrue(result1.success());
        AppPO app = appDAO.getById(id);
        Assertions.assertNull(app);
    }

    @Test
    public void appExistTest() {
        AppPO appPO = getAppPO();
        AppPO appPO1 = getAppPO();
        AppPO appPO2 = getAppPO();
        appDAO.insert(appPO);
        appDAO.insert(appPO1);
        appDAO.insert(appPO2);
        appDAO.delete(appPO2.getId());

        List<App> appList = service.listApps();
        List<Integer> idList = appList.stream().map(App::getId).collect(Collectors.toList());
        Assertions.assertTrue(idList.contains(appPO.getId()));
        Assertions.assertTrue(idList.contains(appPO1.getId()));
        Assertions.assertFalse(idList.contains(appPO2.getId()));
        Map<Integer, App> appsMap = service.getAppsMap();
        Assertions.assertTrue(appsMap.containsKey(appPO.getId()));
        Assertions.assertTrue(appsMap.containsKey(appPO1.getId()));
        Assertions.assertFalse(appsMap.containsKey(appPO2.getId()));
        Assertions.assertTrue(service.isAppExists(appPO.getId()));
        Assertions.assertTrue(service.isAppExists(appPO1.getId()));
        Assertions.assertFalse(service.isAppExists(appPO2.getId()));
    }

    @Test
    public void getAppTest() {
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        Integer id = appPO.getId();
        App app = service.getAppById(id);
        Assertions.assertNotNull(app);
        String name = service.getAppName(id);
        Assertions.assertEquals(appPO.getName(), name);
    }

    @Test
    public void getAppConfigTest() {
        AppConfig config = service.getAppConfig(-1);
        Assertions.assertNull(config);
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        AppConfig config1 = service.getAppConfig(appPO.getId());
        Assertions.assertNotNull(config1);
        Assertions.assertEquals(defaultConfig.getAggrAnalyzeEnable(), config1.getAggrAnalyzeEnable());
        Assertions.assertEquals(defaultConfig.getDslAnalyzeEnable(), config1.getDslAnalyzeEnable());
        Assertions.assertEquals(defaultConfig.getAnalyzeResponseEnable(), config1.getAnalyzeResponseEnable());
        Assertions.assertEquals(defaultConfig.getIsSourceSeparated(), config1.getIsSourceSeparated());
    }

    @Test
    public void updateAppConfigTest() {
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        AppConfig config = service.getAppConfig(appPO.getId());
        AppConfigDTO configDTO = ConvertUtil.obj2Obj(config, AppConfigDTO.class);
        configDTO.setAggrAnalyzeEnable(0);
        Assertions.assertTrue(service.updateAppConfig(configDTO, operator).success());
        AppConfig config1 = service.getAppConfig(appPO.getId());
        Assertions.assertEquals(0, config1.getAggrAnalyzeEnable());
    }

    @Test
    public void isSuperAppTest() {
        AppPO appPO = getAppPO();
        appPO.setIsRoot(1);
        appDAO.insert(appPO);
        Integer id = appPO.getId();
        Assertions.assertTrue(service.isSuperApp(id));
        AppPO appPO1 = getAppPO();
        appPO1.setIsRoot(0);
        appDAO.insert(appPO1);
        Integer id1 = appPO1.getId();
        Assertions.assertFalse(service.isSuperApp(id1));
        Assertions.assertFalse(service.isSuperApp(-1));
    }

    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void verifyTest(AppDTO appDTO) {
        final String code = "123";
        appDTO.setVerifyCode(code);
        Integer id = service.registerApp(appDTO, operator).getData();
        Result<Void> result = service.verifyAppCode(null, code);
        Assertions.assertFalse(result.success());
        Result<Void> result1 = service.verifyAppCode(id, "000");
        Assertions.assertFalse(result1.success());
        Result<Void> result2 = service.verifyAppCode(id, code);
        Assertions.assertTrue(result2.success());
    }

    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void loginTest(AppDTO appDTO) {
        final String code = "123";
        appDTO.setVerifyCode(code);
        Integer id = service.registerApp(appDTO, operator).getData();
        Result<Void> result = service.login(null, code, operator);
        Assertions.assertFalse(result.success());
        Result<Void> result1 = service.login(id + 1, "000", operator);
        Assertions.assertFalse(result1.success());
        Result<Void> result2 = service.login(id + 1, code, "");
        Assertions.assertFalse(result2.success());
        Result<Void> result3 = service.login(id + 1, code, operator);
        Assertions.assertFalse(result3.success());
    }

    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void getUserAppsTest(AppDTO appDTO) {
        String user = "user   1";
        List<App> appList = service.getUserLoginWithoutCodeApps(user);
        Assertions.assertTrue(appList.isEmpty());
        Integer id1 = service.registerApp(appDTO, user).getData();
        appDTO.setName("test_wpk");
        Integer id2 = service.registerApp(appDTO, user).getData();
        AppUserInfo appUserInfo1 = new AppUserInfo();
        appUserInfo1.setAppId(id1);
        appUserInfo1.setUserName(user);
        appUserInfo1.setLastLoginTime(new Date());
        AppUserInfo appUserInfo2 = new AppUserInfo();
        appUserInfo2.setAppId(id2);
        appUserInfo2.setUserName(user);
        appUserInfo2.setLastLoginTime(new Date());
        Mockito.when(userInfoService.getByUser(Mockito.anyString())).thenReturn(Arrays.asList(appUserInfo1, appUserInfo2));
        List<App> appList1 = service.getUserLoginWithoutCodeApps(user);
        Assertions.assertEquals(appList1.get(0).getId(), id1);
        Assertions.assertEquals(appList1.get(1).getId(), id2);
    }

    @ParameterizedTest
    @MethodSource("com.didichuxing.datachannel.arius.admin.util.CustomDataSource#appDTOSource")
    public void getAppByNameTest(AppDTO appDTO) {
        String name = "#name1";
        appDTO.setName(name);
        Integer id = service.registerApp(appDTO, operator).getData();
        App app = service.getAppByName(name + "1");
        Assertions.assertNull(app);
        App app1 = service.getAppByName(name);
        Assertions.assertEquals(id, app1.getId());
    }

}