package com.didichuxing.datachannel.arius.admin.biz.extend.intfc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ExtendServiceNotSupportException;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 获取扩展服务接口的实现类
 *
 * 配置文件中配置的是扩展服务的前缀，查找时就按着前缀匹配去找实现类，如果没有找到就找default开头的；如果都没有就报错
 *
 * @author d06679
 * @date 2019/4/29
 */
@Component
@NoArgsConstructor
public class ExtendServiceFactory {

    private static final String DEFAULT = "default";

    @Value("${extend.service}")
    private String              extendService;

    public <T> Result<T> getExtend(Class<T> tClass) {
        Map<String, T> beans = SpringTool.getBeansOfType(tClass);

        if (beans.isEmpty()) {
            throw new ExtendServiceNotSupportException("扩展接口无任何实现: " + tClass.getSimpleName());
        }

        Set<String> extendServiceSet = Sets.newHashSet(extendService.split(","));

        for (Map.Entry<String, T> bean : beans.entrySet()) {
            for (String namePre : extendServiceSet) {
                if (StringUtils.isNotBlank(namePre) && StringUtils.startsWith(bean.getKey(), namePre)) {
                    return Result.buildSucc(bean.getValue());
                }
            }
        }

        return Result.buildNotExist("扩展服务不存在");
    }

    public <T> T getDefault(Class<T> tClass) {
        Map<String, T> beans = SpringTool.getBeansOfType(tClass);

        if (beans.isEmpty()) {
            throw new ExtendServiceNotSupportException("扩展服务不存在: " + tClass.getSimpleName());
        }

        T first = null;
        for (Map.Entry<String, T> entry : beans.entrySet()) {
            if (first == null) {
                first = entry.getValue();
            }
            if (StringUtils.startsWith(entry.getKey(), DEFAULT)) {
                return entry.getValue();
            }
        }

        return first;
    }

    public <T> List<T> getAll(Class<T> tClass) {
        Map<String, T> beans = SpringTool.getBeansOfType(tClass);

        if (beans.isEmpty()) {
            throw new ExtendServiceNotSupportException("扩展服务不存在: " + tClass.getSimpleName());
        }

        Set<String> extendServiceSet = Sets.newHashSet(extendService.split(","));
        extendServiceSet.add(DEFAULT);

        List<T> result = Lists.newArrayList();
        for (Map.Entry<String, T> entry : beans.entrySet()) {
            for (String namePre : extendServiceSet) {
                if (StringUtils.isNotBlank(namePre) && StringUtils.startsWith(entry.getKey(), namePre)) {
                    result.add(entry.getValue());
                }
            }
        }

        return result;
    }
}
