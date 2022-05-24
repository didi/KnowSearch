package com.didichuxing.datachannel.arius.admin.core.service.common;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;

/**
 *
 *
 * @author d06679
 * @date 2018/8/27
 * @deprecated 引入多租户体系后续进行下线删除
 */
@Deprecated
public interface AriusUserInfoService {

    /**
     * 通过域账号查询
     * @param domainAccount 域账号
     * @return 用户信息  不存在返回null
     */
    AriusUserInfo getByDomainAccount(String domainAccount);

    /**
     * 保存一个用户信息
     * @param userInfoDTO 用户信息
     * @return userId
     */
    Result<Long> save(AriusUserInfoDTO userInfoDTO);

    /**
     * 保存一波用户信息
     * @param responsible 用,间隔的用户名
     * @return userIds
     */
    List<Long> saveByUsers(String responsible);

    /**
     * 根据id获取用户名
     * 这里做一个缓存,不用每次都去数据里取
     * @param ids  可以是多个id英文逗号拼接起来的
     * @return 用户名
     */
    String getUserByIds(String ids);

    /**
     * 获取索引用户
     * @return list
     */
    List<AriusUserInfo> listAllEnable();

    /**
     * 删除用户
     * @param  id id
     * @return true/false
     */
    boolean delete(Long id);

    /**
     * 处理重复用户
     * @return true/false
     */
    boolean processUserDuplicate();

    /**
     * 是否管理员
     * @return true/false
     */
    boolean isOPByDomainAccount(String domainAccount);

    /**
     * 是否管理员
     * @return true/false
     */
    boolean isRDByDomainAccount(String domainAccount);

    /**
     * 是否存在
     */
    boolean isExist(String userName);

    /**
     * 根据用户名获取用户信息
     * @return AriusUserInfo
     */
    AriusUserInfo getByName(String userName);

    /**
     * 获取维护人员列表
      * @return
     */
    List<AriusUserInfo> listByRoles(List<Integer> roles);

    /**
     * 根据ID批量查找人员列表
     * @return
     */
    List<AriusUserInfo> listByIds(List<Long> ids);

    /**
     * 根据关键字搜索用户信息
     * @param keyWord
     * @return
     */
    List<AriusUserInfo> searchOnJobStaffByKeyWord(String keyWord);

    /**
     * deleteUserRole
     * @param name
     * @return
     */
    Boolean deleteUserRole(String name);

    /**
     * addUserRole
     * @param dto
     * @return
     */
    Boolean addUserRole(AriusUserInfoDTO dto);

    /**
     * updateUserRole
     * @param dto
     * @return
     */
    Boolean updateUserRole(AriusUserInfoDTO dto);

    /**
     * 更新用户信息
     */
    Boolean updateUserInfo(AriusUserInfoDTO ariusUserInfoDTO);

    /**
     * 同步登录协议中的用户信息到本地用户表
     * @param protocolType 登录协议类型
     */
    Result<Void> syncUserInfoToDbFromLoginProtocol(LoginDTO loginDTO, String protocolType);
}