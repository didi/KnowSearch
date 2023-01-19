package com.didi.arius.gateway.common.connectioncontrl;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

@Data
public class ConnectionLimitResult {
		private Long    count;
		private Long    limit;
		private boolean overConnected;
		
		public ConnectionLimitResult(Long count) {
				this.count = count;
				this.limit = Long.MAX_VALUE;
				this.overConnected = false;
		}
		
		public ConnectionLimitResult(Long count, Long limit) {
				this.count = count;
				this.limit = limit;
				this.overConnected = (ObjectUtils.compare(limit, count) <= 0);
		}
		
		public boolean isOverConnected() {
				return overConnected;
		}
}