package com.didichuxing.datachannel.arius.admin.common.bean.common.op.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ip 端口号
 *
 * @author shizeying
 * @date 2023/01/03
 * @since 0.3.2
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpPort {
		
		private String  ip;
		private Integer minPort;
		private Integer maxPort;
		
}