package com.didichuxing.datachannel.arius.admin.biz.task.op.manager;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralConfigChangeComponentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置变更
 *
 * @author shizeying
 * @date 2022/11/20
 * @since 0.3.2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigChangeComponentContent extends GeneralConfigChangeComponentDTO {
	
		/**
     * 原因
     */
    private String reason;
}