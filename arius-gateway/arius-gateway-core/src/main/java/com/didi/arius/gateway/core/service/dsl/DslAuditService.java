package com.didi.arius.gateway.core.service.dsl;

import com.didi.arius.gateway.common.metadata.BaseContext;
import org.elasticsearch.common.bytes.BytesReference;

/**
 * @author fitz
 * @date 2021/5/25 7:38 下午
 * dsl模板解析，把dsl语句解析成dsl模板
 */
public interface DslAuditService {
    /**
     * dsl模板解析，把dsl语句解析成dsl模板, 并做访问和限流校验
     * @param baseContext
     * @param source
     * @param indices
     * @return
     */
    String auditDSL(BaseContext baseContext, BytesReference source, String[] indices);

    /**
     *
     * 同上
     * @param baseContext
     * @param sql
     * @param indices
     * @return
     */
    String auditSQL(BaseContext baseContext, String sql, String[] indices);
}
