package com.didichuxing.datachannel.arius.admin.core.service.common.impl;

import static com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.list2List;
import static com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.obj2Obj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.login.Login;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.AriusUserInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLogicPO;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.arius.AriusUserInfoDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateLogicDAO;
import com.didichuxing.datachannel.arius.admin.remote.protocol.LoginProtocolHandle;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

@Service
public class AriusUserInfoServiceImpl implements AriusUserInfoService {

    private static final ILog                             LOGGER                = LogFactory
        .getLog(AriusUserInfoServiceImpl.class);

    @Autowired
    private AriusUserInfoDAO                              ariusUserInfoDAO;

    @Autowired
    private IndexTemplateLogicDAO                         templateLogicDAO;

    @Autowired
    private LogicClusterDAO                               logicClusterDAO;

    @Autowired
    private HandleFactory                                 handleFactory;

    @Autowired
    private AppDAO                                        appDAO;

    /**
     * 缓存每个user的id和名字
     * id和user的映射关系一旦生成,就不会变;
     */
    private Map<Long, String>                             idNameCache           = Maps.newConcurrentMap();

    /**
     * 定期的从数据库中查询所有的用户信息 缓存
     */
    @PostConstruct
    public void refreshCache() {
        LOGGER.info("class=AriusUserInfoServiceImpl||method=refreshCache||AriusUserInfoServiceImpl refreshCache start.");
        idNameCache.clear();

        try {
            List<AriusUserInfoPO> infoPOs = ariusUserInfoDAO.listAllEnable();

            infoPOs.forEach(infoPO -> idNameCache.put(infoPO.getId(), infoPO.getDomainAccount()));
        } catch (Exception e) {
            LOGGER.error("class=AriusUserInfoServiceImpl||method=refreshCache||msg={}", e.getMessage());
        }

        LOGGER.info(
            "class=AriusUserInfoServiceImpl||method=refreshCache||AriusUserInfoServiceImpl refreshCache finished.");
    }

    /**
     * 通过域账号查询
     * @param domainAccount 域账号
     * @return 用户信息 不存在返回null
     */
    @Override
    public AriusUserInfo getByDomainAccount(String domainAccount) {
        return obj2Obj(ariusUserInfoDAO.getByDomainAccount(domainAccount), AriusUserInfo.class);
    }

    /**
     * 保存一个用户信息
     * @param userInfoDTO 用户信息
     * @return userId
     */
    @Override
    public Result<Long> save(AriusUserInfoDTO userInfoDTO) {
        Result<Void> checkParamResult = checkParam(userInfoDTO);
        if(checkParamResult.failed()) {
            return Result.buildFrom(checkParamResult);
        }

        // 已经保存过直接返回id
        AriusUserInfo oldUserInfo = getByDomainAccount(userInfoDTO.getDomainAccount());
        if (oldUserInfo != null) {
            LOGGER.info("class=AriusUserInfoServiceImpl||method=save||domainAccout={}||msg=domain account has exist!",
                userInfoDTO.getDomainAccount());
            return Result.buildSucc(oldUserInfo.getId());
        }

        AriusUserInfoPO ariusUserInfoPO = obj2Obj(userInfoDTO, AriusUserInfoPO.class);
        ariusUserInfoDAO.insert(ariusUserInfoPO);

        return Result.buildSucc(ariusUserInfoPO.getId());
    }

    /**
     * 保存一波用户信息
     * @param userNames 用,间隔的用户名
     * @return userIds
     */
    @Override
    public List<Long> saveByUsers(String userNames) {
        if (StringUtils.isBlank(userNames)) {
            return Lists.newArrayList();
        }

        List<Long> userIds = Lists.newArrayList();
        for (String user : userNames.split(",")) {
            AriusUserInfoDTO infoDTO = new AriusUserInfoDTO();
            infoDTO.setName(user);
            infoDTO.setPassword(VerifyCodeFactory.get(user.length()));
            infoDTO.setDomainAccount(user);
            infoDTO.setEmail("");
            infoDTO.setMobile("");
            infoDTO.setStatus(AriusUserStatusEnum.NORMAL.getCode());
            infoDTO.setRole(AriusUserRoleEnum.NORMAL.getRole());

            Result<Long> ret = save(infoDTO);
            if (ret.success()) {
                userIds.add(ret.getData());
            }
        }

        return userIds;
    }

    /**
     * 根据id获取用户名
     * 这里做一个缓存,不用每次都去数据里取
     *
     * @param ids 可以是多个id拼接起来的
     * @return 用户名
     */
    @Override
    public String getUserByIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            return "";
        }

        Set<String> names = new HashSet<>();
        String[] idArr = ids.split(",");
        for (String id : idArr) {
            Long idLong = Long.valueOf(id);
            String name = getUserById(idLong);
            if (StringUtils.isNotBlank(name)) {
                names.addAll(parseArrays(name));
            }
        }

        return String.join(",", names);
    }

    /**
     * 获取索引用户
     *
     * @return list
     */
    @Override
    public List<AriusUserInfo> listAllEnable() {
        return list2List(ariusUserInfoDAO.listAllEnable(), AriusUserInfo.class);
    }

    /**
     * 删除用户
     *
     * @param id id
     * @return true/false
     */
    @Override
    public boolean delete(Long id) {
        AriusUserInfoPO param = new AriusUserInfoPO();
        param.setId(id);
        param.setStatus(AriusUserStatusEnum.DISABLE.getCode());
        return 1 == ariusUserInfoDAO.update(param);
    }

    /**
     * 处理重复用户
     *
     * @return true/false
     */
    @Override
    public boolean processUserDuplicate() {
        List<AriusUserInfo> userInfos = listAllEnable();

        Multimap<String, AriusUserInfo> name2AriusUserInfoMultiMap = ConvertUtil.list2MulMap(userInfos,
            AriusUserInfo::getName);

        boolean succ = true;
        for (String name : name2AriusUserInfoMultiMap.keySet()) {
            try {
                Collection<AriusUserInfo> infos = name2AriusUserInfoMultiMap.get(name);
                if (infos.size() == 1) {
                    continue;
                }
                List<AriusUserInfo> dupUserInfos = Lists.newArrayList(infos);

                LOGGER.info("class=AriusUserInfoServiceImpl||method=processUserDuplicate||name={}||size={}", name, dupUserInfos.size());

                AriusUserInfo finalUserInfo = dupUserInfos.get(0);
                List<AriusUserInfo> shouldDelUserInfos = dupUserInfos.subList(1, dupUserInfos.size());
                for (AriusUserInfo userInfo : shouldDelUserInfos) {
                    processApp(userInfo.getId(), finalUserInfo.getId());
                    processResource(userInfo.getId(), finalUserInfo.getId());
                    processTemplate(userInfo.getId(), finalUserInfo.getId());

                    delete(userInfo.getId());
                    LOGGER.info("class=AriusUserInfoServiceImpl||method=processUserDuplicate||name={}||id={}||msg=deleted", name, userInfo.getId());
                }
            } catch (Exception e) {
                succ = false;
                LOGGER.info("class=AriusUserInfoServiceImpl||method=processUserDuplicate||name={}||errMsg={}", name, e.getMessage(), e);
            }
        }

        return succ;
    }

    @Override
    public boolean isOPByDomainAccount(String domainAccount) {
        AriusUserInfoPO userInfoPO = ariusUserInfoDAO.getByDomainAccount(domainAccount);
        if (userInfoPO == null) {
            return false;
        }
        return AriusUserRoleEnum.getUserRoleEnum(userInfoPO.getRole()).equals(AriusUserRoleEnum.OP);
    }

    @Override
    public boolean isExist(String userName) {
        return ariusUserInfoDAO.getByName(userName) != null;
    }

    @Override
    public boolean isRDByDomainAccount(String domainAccount) {
        AriusUserInfoPO userInfoPO = ariusUserInfoDAO.getByDomainAccount(domainAccount);
        if (userInfoPO == null) {
            return false;
        }
        return AriusUserRoleEnum.getUserRoleEnum(userInfoPO.getRole()).equals(AriusUserRoleEnum.RD);
    }


    @Override
    public AriusUserInfo getByName(String userName) {

        AriusUserInfoPO userInfoPo = null;
        try {
            userInfoPo = ariusUserInfoDAO.getByName(userName);
        } catch (Exception e) {
            LOGGER.error("class=AriusUserInfoServiceImpl||method=getByName||msg={}", e);
        }

        return obj2Obj(userInfoPo, AriusUserInfo.class);
    }

    @Override
    public List<AriusUserInfo> listByRoles(List<Integer> roles) {
        return list2List(ariusUserInfoDAO.listByRoles(roles), AriusUserInfo.class);
    }

    @Override
    public List<AriusUserInfo> listByIds(List<Long> ids) {
        return list2List(ariusUserInfoDAO.listByIds(ids), AriusUserInfo.class);
    }

    @Override
    public List<AriusUserInfo> searchOnJobStaffByKeyWord(String keyWord) {
        return Lists.newArrayList(getByDomainAccount(keyWord));
    }

    @Override
    public Boolean deleteUserRole(String userName) {
        AriusUserInfoPO ariusUserInfoPO = ariusUserInfoDAO.getByName(userName);
        if (null != ariusUserInfoPO) {
            ariusUserInfoPO.setRole(AriusUserRoleEnum.NORMAL.getRole());

            return ariusUserInfoDAO.update(ariusUserInfoPO) > 0;
        }

        return true;
    }

    @Override
    public Boolean addUserRole(AriusUserInfoDTO dto) {
        AriusUserInfoPO ariusUserInfoPO = ariusUserInfoDAO.getByName(dto.getName());
        if (null != ariusUserInfoPO) {
            ariusUserInfoPO.setRole(dto.getRole());
            ariusUserInfoPO.setStatus(AriusUserStatusEnum.NORMAL.getCode());
            return ariusUserInfoDAO.update(ariusUserInfoPO) > 0;
        }

        if (StringUtils.isBlank(dto.getEmail())) {
            dto.setEmail("");
        }
        if (StringUtils.isBlank(dto.getMobile())) {
            dto.setMobile("");
        }

        dto.setStatus(AriusUserStatusEnum.NORMAL.getCode());

        return ariusUserInfoDAO.insert(ConvertUtil.obj2Obj(dto, AriusUserInfoPO.class)) > 0;
    }

    @Override
    public Boolean updateUserRole(AriusUserInfoDTO dto) {
        AriusUserInfoPO ariusUserInfoPO = ariusUserInfoDAO.getByName(dto.getName());
        if (null != ariusUserInfoPO) {
            ariusUserInfoPO.setRole(dto.getRole());

            return ariusUserInfoDAO.update(ariusUserInfoPO) > 0;
        }

        return false;
    }

    @Override
    public Boolean updateUserInfo(AriusUserInfoDTO ariusUserInfoDTO) {
        if(!AriusObjUtils.isNull(ariusUserInfoDTO)) {
            return ariusUserInfoDAO.update(obj2Obj(ariusUserInfoDTO, AriusUserInfoPO.class)) > 0;
        }

        return Boolean.FALSE;
    }

    @Override
    public Result<Void> syncUserInfoToDbFromLoginProtocol(LoginDTO loginDTO, String protocolType) {

        LoginProtocolHandle loginProtocolHandle = (LoginProtocolHandle) handleFactory.getByHandlerNamePer(protocolType);

        AriusUserInfo userInfo = loginProtocolHandle.getUserInfoFromLoginProtocol(obj2Obj(loginDTO, Login.class));

        Result<Long> ret = save(obj2Obj(userInfo, AriusUserInfoDTO.class));
        if (ret.failed()) {
            LOGGER.warn(
                "class=AriusUserInfoServiceImpl||method=syncUserInfoToDbFromLoginProtocol||loginUserName={}||msg=fail to sync user info",
                loginDTO.getDomainAccount());
            return Result.buildFail();
        }

        return Result.buildSucc();
    }

    /**************************************** private method *************************************************/
    private void processTemplate(Long srcRespId, Long tgtRespId) {
        String srcRespIdStr = String.valueOf(srcRespId);
        String tgtRespIdStr = String.valueOf(tgtRespId);

        List<TemplateLogicPO> templateLogicPOS = templateLogicDAO.likeByResponsible(srcRespIdStr);

        if (CollectionUtils.isNotEmpty(templateLogicPOS)) {
            templateLogicPOS.forEach(po -> {
                List<String> respList = Lists.newArrayList(po.getResponsible().split(","));
                for (int i = 0; i < respList.size(); i++) {
                    if (srcRespIdStr.equals(respList.get(i))) {
                        respList.set(i, tgtRespIdStr);
                    }
                }

                TemplateLogicPO param = new TemplateLogicPO();
                param.setId(po.getId());
                param.setResponsible(String.join(",", respList));
                templateLogicDAO.update(param);

                LOGGER.info("class=AriusUserInfoServiceImpl||method=processTemplate||template={}||srcResp={}||tgtResp={}", po.getName(),
                    po.getResponsible(), param.getResponsible());
            });
        }
    }

    private void processResource(Long srcRespId, Long tgtRespId) {
        String srcRespIdStr = String.valueOf(srcRespId);
        String tgtRespIdStr = String.valueOf(tgtRespId);

        List<ClusterLogicPO> resourcePOs = logicClusterDAO.listByResponsible(srcRespIdStr);

        if (CollectionUtils.isNotEmpty(resourcePOs)) {
            resourcePOs.forEach(po -> {
                List<String> respList = Lists.newArrayList(po.getResponsible().split(","));
                for (int i = 0; i < respList.size(); i++) {
                    if (srcRespIdStr.equals(respList.get(i))) {
                        respList.set(i, tgtRespIdStr);
                    }
                }

                ClusterLogicPO param = new ClusterLogicPO();
                param.setId(po.getId());
                param.setResponsible(String.join(",", respList));
                logicClusterDAO.update(param);

                LOGGER.info("class=AriusUserInfoServiceImpl||method=processResource||cluster={}||srcResp={}||tgtResp={}", po.getName(),
                    po.getResponsible(), param.getResponsible());
            });
        }
    }

    private void processApp(Long srcRespId, Long tgtRespId) {

        String srcRespIdStr = String.valueOf(srcRespId);
        String tgtRespIdStr = String.valueOf(tgtRespId);

        List<AppPO> appPOS = appDAO.listByResponsible(srcRespIdStr);

        if (CollectionUtils.isNotEmpty(appPOS)) {
            appPOS.forEach(po -> {
                List<String> respList = Lists.newArrayList(po.getResponsible().split(","));
                for (int i = 0; i < respList.size(); i++) {
                    if (srcRespIdStr.equals(respList.get(i))) {
                        respList.set(i, tgtRespIdStr);
                    }
                }

                AppPO param = new AppPO();
                param.setId(po.getId());
                param.setResponsible(String.join(",", respList));
                appDAO.update(param);

                LOGGER.info("class=AriusUserInfoServiceImpl||method=processApp||app={}||srcResp={}||tgtResp={}", po.getName(), po.getResponsible(),
                    param.getResponsible());
            });
        }
    }

    private Result<Void> checkParam(AriusUserInfoDTO userInfoDTO) {
        if (AriusObjUtils.isNull(userInfoDTO)) {
            return Result.buildParamIllegal("用户信息为空");
        }
        if (AriusObjUtils.isNull(userInfoDTO.getName())) {
            return Result.buildParamIllegal("名字为空");
        }
        if (AriusObjUtils.isNull(userInfoDTO.getPassword())) {
            return Result.buildParamIllegal("密码为空");
        }
        if (AriusObjUtils.isNull(userInfoDTO.getDomainAccount())) {
            return Result.buildParamIllegal("域账号为空");
        }
        if (AriusObjUtils.isNull(userInfoDTO.getStatus())) {
            return Result.buildParamIllegal("状态为空");
        }

        return Result.buildSucc();
    }

    private String getUserById(Long userId) {
        if (idNameCache.containsKey(userId)) {
            return idNameCache.get(userId);
        } else {
            AriusUserInfoPO userInfoPO = ariusUserInfoDAO.getById(userId);
            if (userInfoPO != null) {
                idNameCache.put(userId, userInfoPO.getDomainAccount());
                return userInfoPO.getDomainAccount();
            } else {
                idNameCache.put(userId, "");
            }
        }

        return "";
    }

    /**
     * 解析格式化的字符串为字符串列表
     * @param formattedData 格式化的字符串
     * @return
     */
    private List<String> parseArrays(String formattedData) {
        List<String> toAddressList = new ArrayList<>();
        if (StringUtils.isNotBlank(formattedData)) {
            if (formattedData.contains("[")) {
                try {
                    toAddressList = JSON.parseArray(formattedData, String.class);
                } catch (Exception e) {
                    LOGGER.info("class=AriusUserInfoServiceImpl||method=parseArrays||errMsg=invalidUserAccount||formattedData={}", formattedData);
                }
            } else {
                handleFormattedData(formattedData, toAddressList);
            }

        }
        return toAddressList;
    }

    private void handleFormattedData(String formattedData, List<String> toAddressList) {
        for (String address : formattedData.split(",")) {
            if (StringUtils.isNotBlank(address)) {
                if (address.contains("\"")) {
                    address = address.replace("\"", "");
                }
                toAddressList.add(address);
            }
        }
    }
}
