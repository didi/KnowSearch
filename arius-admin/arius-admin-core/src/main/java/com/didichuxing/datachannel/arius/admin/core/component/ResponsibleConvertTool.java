package com.didichuxing.datachannel.arius.admin.core.component;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;
import com.didichuxing.datachannel.arius.admin.common.bean.po.DigitResponsible;
import com.didichuxing.datachannel.arius.admin.common.util.AriusUserUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;

/**
 * @author d06679
 * 用于带responsible的类型转换,要求po必须实现DigitResponsible方法;entity类实现StringResponsible方法
 * 数据保存数据库时,目标类类是po,会将责任人编码
 * 获取平台数据时,目标类是entity,会将责任人解码;Service层对外输出的类都必须是entity
 * @date 2019/3/18
 */
@Component
public class ResponsibleConvertTool {

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    /**
     * 将PO中的responsible字段转换为String
     * @param resp pos列表
     */
    private void id2Str(StringResponsible resp) {
        try {
            String str = ariusUserInfoService.getUserByIds(resp.getResponsible());
            if (StringUtils.isNotBlank(str)) {
                resp.setResponsible(str);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 将DTO中的responsible字段转换为id
     * @param resp pos列表
     */
    private void str2Id(DigitResponsible resp) {
        resp.setResponsible(AriusUserUtil.userIds2Str(ariusUserInfoService.saveByUsers(resp.getResponsible())));
    }

    /**
     * 类型转换的同时如果是Responsible类型则需要进行responsible转换,(ids <-> names)
     * @param list
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> List<T> list2List(List list, Class<T> tClass) {
        return ConvertUtil.list2List(list, tClass, (tgtO) -> {
            if (tgtO instanceof StringResponsible) {
                id2Str((StringResponsible) tgtO);
            } else if (tgtO instanceof DigitResponsible) {
                str2Id((DigitResponsible) tgtO);
            }
        });
    }

    /**
     * 类型转换的同时如果是Responsible类型则需要进行responsible转换,(ids <-> names)
     * @param srcObj
     * @param tgtClass
     * @param <T>
     * @return
     */
    public <T> T obj2Obj(final Object srcObj, Class<T> tgtClass) {
        return ConvertUtil.obj2Obj(srcObj, tgtClass, (tgtO) -> {
            if (tgtO instanceof StringResponsible) {
                id2Str((StringResponsible) tgtO);
            } else if (tgtO instanceof DigitResponsible) {
                str2Id((DigitResponsible) tgtO);
            }
        });
    }
}
