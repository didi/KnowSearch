package com.didichuxing.datachannel.arius.admin.common.component;

import com.google.common.collect.Maps;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * @author linyunan
 * @date 2021-04-26
 */
@Component
public class HandleFactory extends BaseExtendFactory {

    private Map<String, BaseHandle> baseHandleMap = Maps.newConcurrentMap();

    /**
     * 根据类名前缀获取接口BaseHandle的实现类实例
     */
    public BaseHandle getByHandlerNamePer(String handleNamePre) {
        if (baseHandleMap.containsKey(handleNamePre)) {
            return baseHandleMap.get(handleNamePre);
        } else {
            BaseHandle baseHandle = getByClassNamePer(handleNamePre, BaseHandle.class);
            baseHandleMap.put(handleNamePre, baseHandle);
            return baseHandle;
        }
    }
}
