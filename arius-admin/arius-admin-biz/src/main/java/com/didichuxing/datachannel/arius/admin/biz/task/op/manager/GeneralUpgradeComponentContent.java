package com.didichuxing.datachannel.arius.admin.biz.task.op.manager;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralUpgradeComponentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author shizeying
 * @date 2022/11/20
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralUpgradeComponentContent extends GeneralUpgradeComponentDTO {
		private String reason;
}