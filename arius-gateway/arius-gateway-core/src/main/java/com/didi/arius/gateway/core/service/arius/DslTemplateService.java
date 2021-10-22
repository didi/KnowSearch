package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.metadata.DSLTemplate;

import java.util.List;

public interface DslTemplateService {

    /**
     * 缓存dslTemplate
     * @param key
     * @param dslTemplate
     */
    void putDSLTemplate(String key, DSLTemplate dslTemplate);

    /**
     * 删除key的dslTemplate缓存
     * @param key
     */
    void removeDSLTemplate(String key);

    /**
     * 获取key的dslTemplate缓存
     * @param key
     * @return
     */
    DSLTemplate getDSLTemplate(String key);

    /**
     * 获取dslTemplate缓存的所有key
     * @return
     */
    List<String> getDslTemplateKeys();

    /**
     * 缓存新的dslTemplate
     * @param key
     * @param dslTemplate
     */
    void putNewDSLTemplate(String key, DSLTemplate dslTemplate);

    /**
     * 获取key的新的dslTemplate缓存
     * @param key
     * @return
     */
    DSLTemplate getNewDSLTemplate(String key);

    /**
     * 获取新的dslTemplate缓存的所有key
     * @return
     */
    List<String> getNewDslTemplateKeys();

    /**
     * 更新dsl模板，更新dsl模板限流值
     */
    void resetDslInfo();
}
