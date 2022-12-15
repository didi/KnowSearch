package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager;

import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralScaleComponentDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * escluster 扩展插件 dto
 *
 * @author shizeying
 * @date 2022/12/14
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterExpandWithPluginDTO extends GeneralScaleComponentDTO {
		private List<GeneraInstallComponentWithTypeDTO> installPlugins;
		
}