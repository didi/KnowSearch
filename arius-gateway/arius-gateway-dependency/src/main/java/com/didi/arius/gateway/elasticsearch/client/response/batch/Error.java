package com.didi.arius.gateway.elasticsearch.client.response.batch;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/18 下午2:29
 * @modified By D10865
 *

"error": {
"type": "mapper_parsing_exception",
"reason": "failed to parse [date]",
"caused_by": {
"type": "illegal_argument_exception",
"reason": "Invalid format: \"exception_test\""
}
}

 */
public class Error {

    @JSONField(name = "type")
    private String type;

    @JSONField(name = "reason")
    private String reason;

    @JSONField(name = "caused_by")
    private Cause cause;

    public Error() {
    }

    public String getType() {
        return type;
    }

    public Error setType(String type) {
        this.type = type;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public Error setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public Cause getCause() {
        return cause;
    }

    public Error setCause(Cause cause) {
        this.cause = cause;
        return this;
    }

    class Cause {

        private String type;

        private String reason;

        public Cause() {
        }

        public String getType() {
            return type;
        }

        public Cause setType(String type) {
            this.type = type;
            return this;
        }

        public String getReason() {
            return reason;
        }

        public Cause setReason(String reason) {
            this.reason = reason;
            return this;
        }
    }

}
