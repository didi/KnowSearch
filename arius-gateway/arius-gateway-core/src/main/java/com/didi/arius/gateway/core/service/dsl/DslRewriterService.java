package com.didi.arius.gateway.core.service.dsl;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.common.metadata.BaseContext;
import org.elasticsearch.common.bytes.BytesReference;

/**
 * @author fitz
 * @date 2021/5/25 7:44 下午
 * 做dsl兼容，兼容高低版本的dsl语法差异
 */
public interface DslRewriterService {

    /**
     * dsl兼容
     * @param context
     * @param esVersion
     * @param source
     * @return
     * @throws Exception
     */
    BytesReference rewriteRequest(BaseContext context, String esVersion, JSONObject source) throws Exception;

    /**
     *
     * 执行typed_key
     * @param context
     * @param source
     */
    void doTypedKey(BaseContext context, JSONObject source);

    /**
     * dsl兼容
     * @param context
     * @param esVersion
     * @param source
     * @return
     */
    BytesReference rewriteRequest(BaseContext context, String esVersion, BytesReference source);
}
