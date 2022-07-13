package com.didichuxing.datachannel.arius.admin.core.component;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;

import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * 从SpringContent中获取指定接口的类的实例
 * 使用例子参考 HandleFactory
 *
 * @author linyunan
 * @date 2021-04-25
 */
public abstract class BaseExtendFactory {

    private static final ILog LOGGER = LogFactory.getLog(BaseExtendFactory.class);

    public <T> T getByClassNamePer(String classNamePre, Class<T> clazz) throws NotFindSubclassException {
        T handler = null;
        handler = doGet(classNamePre, clazz);

        return handler;
    }

    private <T> T doGet(String classNamePre, Class<T> clazz) throws NotFindSubclassException {
        Map<String, T> beans = null;
        try {
            beans = SpringTool.getBeansOfType(clazz);
        } catch (BeansException e) {
            LOGGER.error("class=BaseExtendFactory||method=findFromSpringContext||handleNamePre={}||msg={}",
                classNamePre, e.getMessage());
        }

        if (beans == null || beans.isEmpty()) {
            throw new NotFindSubclassException(String.format("找不到【%s】的具体处理器【%s】", clazz.getSimpleName(), classNamePre));
        }

        for (Map.Entry<String, T> bean : beans.entrySet()) {
            if (StringUtils.startsWith(bean.getKey(), classNamePre)) {
                return bean.getValue();
            }
        }

        throw new NotFindSubclassException(String.format("找不到【%s】的具体处理器【%s】", clazz.getSimpleName(), classNamePre));
    }
}
