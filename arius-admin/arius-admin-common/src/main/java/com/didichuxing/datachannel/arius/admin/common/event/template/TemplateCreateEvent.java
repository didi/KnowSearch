package com.didichuxing.datachannel.arius.admin.common.event.template;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import org.springframework.context.ApplicationEvent;

/**
 * @author chengxiang
 * @date 2022/5/27
 */
public class TemplateCreateEvent extends ApplicationEvent {

    private IndexTemplateDTO indexTemplateDTO;

    public TemplateCreateEvent(Object source, IndexTemplateDTO indexTemplateDTO) {
        super(source);
        this.indexTemplateDTO = indexTemplateDTO;
    }

    public IndexTemplateDTO getIndexTemplateDTO() {
        return indexTemplateDTO;
    }
}
