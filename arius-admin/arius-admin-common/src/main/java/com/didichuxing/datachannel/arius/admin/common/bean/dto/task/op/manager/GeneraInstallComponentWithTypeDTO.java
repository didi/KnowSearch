package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralScaleComponentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * generas 安装 dto 类型组件
 *
 * @author shizeying
 * @date 2022/12/14
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneraInstallComponentWithTypeDTO extends GeneralScaleComponentDTO {
		private Integer pluginType;
		/**
		 * 依赖组件 id
		 */
		private Integer dependComponentId;
}