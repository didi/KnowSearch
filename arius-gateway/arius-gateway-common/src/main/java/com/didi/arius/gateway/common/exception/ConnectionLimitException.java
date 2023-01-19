package com.didi.arius.gateway.common.exception;

import java.io.IOException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

/**
 * 连接限制异常
 *
 * @author shizeying
 * @date 2022/12/15
 * @since 0.3.2
 */
public class ConnectionLimitException extends QueryException {
		public ConnectionLimitException(String msg) {
				super(msg);
		}
		
		public ConnectionLimitException(String msg, Throwable cause) {
				super(msg, cause);
		}
		
		public ConnectionLimitException(StreamInput in) throws IOException {
				super(in);
		}
		
		@Override
		public RestStatus status() {
				return RestStatus.TOO_MANY_REQUESTS;
		}
}