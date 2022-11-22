package com.didichuxing.datachannel.arius.admin.biz.task.op.manager;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralConfigChangeComponentDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 配置变更
 *
 * @author shizeying
 * @date 2022/11/20
 * @since 0.3.2
 */
@Getter
@Setter
public class ConfigChangeComponentContent extends GeneralConfigChangeComponentDTO {
		  public ConfigChangeComponentContent(Integer componentId) {
        super(componentId);
    }
		/**
     * 原因
     */
    private String reason;
}