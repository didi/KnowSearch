package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置操作记录
 *
 * @author shizeying
 * @date 2022/12/19
 * @since 0.3.2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigOperateRecord {
		
		private String name;
		
		private GeneralGroupConfigDTO source;
		private GeneralGroupConfigDTO target;
		
		@Override
		public String toString() {
				return JSON.toJSONString(this);
		}
}