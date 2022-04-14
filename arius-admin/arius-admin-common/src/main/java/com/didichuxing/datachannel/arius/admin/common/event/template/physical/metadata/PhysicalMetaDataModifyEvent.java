package com.didichuxing.datachannel.arius.admin.common.event.template.physical.metadata;

import com.didichuxing.datachannel.arius.admin.common.event.template.TemplateEvent;

/**
 * 物理模板元数据变更事件基本类
 * @author wangshu
 * @date 2020/09/02
 */
public abstract class PhysicalMetaDataModifyEvent extends TemplateEvent {
    protected PhysicalMetaDataModifyEvent(Object source) {
        super(source);
    }
}
