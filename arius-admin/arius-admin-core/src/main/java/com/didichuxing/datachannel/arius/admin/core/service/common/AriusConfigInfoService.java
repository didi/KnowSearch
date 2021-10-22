package com.didichuxing.datachannel.arius.admin.core.service.common;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.config.AriusConfigInfo;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

/**
 * @author d06679
 * @date 2019/3/14
 */
public interface AriusConfigInfoService {

    /**
     * 新增配置
     * @param configInfoDTO 配置信息
     * @param operator 操作人
     * @return 成功 true
     */
    Result addConfig(AriusConfigInfoDTO configInfoDTO, String operator);

    /**
     * 删除配置
     * @param configId 配置id
     * @param operator 操作人
     * @return 成功 true  失败 false
     */
    Result delConfig(Integer configId, String operator);

    /**
     * 编辑配置
     * @param configInfoDTO 配置内容
     * @param operator 操作人
     * @return 成功 true  失败 false
     *
     */
    Result editConfig(AriusConfigInfoDTO configInfoDTO, String operator);

    /**
     * 使能配置
     * @param configId 配置id
     * @param status 状态
     * @param operator 操作人
     * @return 成功 true  失败 false
     *
     */
    Result switchConfig(Integer configId, Integer status, String operator);

    /**
     * 根据配置组获取配置项
     * @param group 配置组
     * @return 配置AriusConfigInfoPO列表  项目内部使用
     *
     * 如果配置组不存在 返回空列表
     *
     */
    List<AriusConfigInfo> getConfigByGroup(String group);

    /**
     * 根据查询条件返回AriusConfigInfoVO列表
     * @param param 查询条件
     * @return 配置列表
     *
     * 如果不存在,返回空列表
     */
    List<AriusConfigInfo> queryByCondt(AriusConfigInfoDTO param);

    /**
     * 查询指定配置
     * @param configId 配置id
     * @return 配置信息  不存在返回null
     */
    AriusConfigInfo getConfigById(Integer configId);

    /**
     * 修改一个配置项的值
     * @param group 配置组
     * @param name 配置名字
     * @param value 配置内容
     * @return  成功 true  失败 false
     *
     */
    Result updateValueByGroupAndName(String group, String name, String value);

    /**
     * 修改一个配置项的值, 获取不存在就新增一个
     * @param group 配置组
     * @param name 配置名字
     * @param value 配置内容
     * @return  成功 true  失败 false
     */
    Result upsertValueByGroupAndName(String group, String name, String value);

    /**
     * 获取int类型配置
     * @param group 配置组
     * @param name 配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回,转换报错或者没有查到则返回默认值
     */
    Integer intSetting(String group, String name, Integer defaultValue);

    /**
     * 获取long类型配置
     * @param group 配置组
     * @param name 配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回,转换报错或者没有查到则返回默认值
     */
    Long longSetting(String group, String name, Long defaultValue);

    /**
     * 获取double类型配置
     * @param group 配置组
     * @param name 配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回,转换报错或者没有查到则返回默认值
     */
    Double doubleSetting(String group, String name, Double defaultValue);

    /**
     * 获取String类型配置
     * @param group 配置组
     * @param name 配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回,转换报错或者没有查到则返回默认值
     */
    String stringSetting(String group, String name, String defaultValue);

    /**
     * 获取String类型配置 用字符分割
     * @param group 配置组
     * @param name 配置项
     * @param defaultValue 默认值
     * @param split 分隔符
     * @return 分隔符
     */
    Set<String> stringSettingSplit2Set(String group, String name, String defaultValue, String split);

    /**
     * 获取bool类型配置
     * @param group 配置组
     * @param name 配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回,转换报错或者没有查到则返回默认值
     */
    Boolean booleanSetting(String group, String name, Boolean defaultValue);

    /**
     * 获取Object类型配置
     * @param group 配置组
     * @param name 配置项
     * @param defaultValue 默认值
     * @param clazz 返回类型
     * @return 如果查到转换后返回,转换报错或者没有查到则返回默认值
     */
    <T> T objectSetting(String group, String name, T defaultValue, Class<T> clazz);

}
